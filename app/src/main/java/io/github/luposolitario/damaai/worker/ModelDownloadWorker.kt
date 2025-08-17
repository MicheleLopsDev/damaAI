package io.github.luposolitario.damaai.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.github.luposolitario.damaai.datastore.ModelSettingsManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicLong

class ModelDownloadWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_URL = "key_url"
        const val KEY_DESTINATION = "key_destination"
        const val KEY_BYTES_DOWNLOADED = "key_bytes_downloaded"
        const val KEY_TOTAL_BYTES = "key_total_bytes"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "download_channel"
        private const val ACTION_CANCEL = "action_cancel"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var lastProgress = 0

    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        val cancelIntent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)
        val notification = buildNotification(0, cancelIntent).build()
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Canale Download Modello",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifiche per download modelli IA"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(progress: Int, cancelIntent: PendingIntent?): NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Download Modello IA")
            .setContentText("Progresso: $progress% (4.3 GB)")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setProgress(100, progress, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .apply {
                cancelIntent?.let {
                    addAction(android.R.drawable.ic_menu_close_clear_cancel, "Annulla", it)
                }
            }
    }

    override suspend fun doWork(): Result = coroutineScope {
        val urlString = inputData.getString(KEY_URL) ?: return@coroutineScope Result.failure()
        val destinationPath = inputData.getString(KEY_DESTINATION) ?: return@coroutineScope Result.failure()

        val accessToken = runCatching { ModelSettingsManager.getHuggingFaceToken(context) }
            .getOrNull() ?: run {
            Log.e("ModelDownloadWorker", "Token di accesso non trovato.")
            return@coroutineScope Result.failure()
        }

        val finalFile = File(destinationPath)
        val tempFile = File("$destinationPath.tmp")
        val downloadedSoFar = if (tempFile.exists()) tempFile.length() else 0L

        try {
            val url = URL(urlString)
            val headConnection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "HEAD"
                setRequestProperty("Authorization", "Bearer $accessToken")
                connectTimeout = 10000
                readTimeout = 15000
            }

            if (headConnection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("ModelDownloadWorker", "Errore Server: ${headConnection.responseCode}")
                headConnection.disconnect()
                return@coroutineScope Result.failure()
            }

            val totalSize = headConnection.contentLengthLong
            headConnection.disconnect()

            if (totalSize <= 0) {
                Log.e("ModelDownloadWorker", "Dimensione file non valida: $totalSize")
                return@coroutineScope Result.failure()
            }

            if (downloadedSoFar >= totalSize) {
                tempFile.renameTo(finalFile)
                ModelSettingsManager.updateDmModelFilePath(destinationPath, context)
                val completedNotification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setContentTitle("Download Completato")
                    .setContentText("Modello pronto per l'uso.")
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .build()
                notificationManager.notify(NOTIFICATION_ID, completedNotification)
                return@coroutineScope Result.success()
            }

            val totalDownloaded = AtomicLong(downloadedSoFar)
            val jobs = mutableListOf<Deferred<Unit>>()

            val numThreads = minOf(8, Runtime.getRuntime().availableProcessors() * 2)
            val partSize = totalSize / numThreads

            val cancelIntent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

            for (i in 0 until numThreads) {
                val start = i * partSize
                val end = if (i == numThreads - 1) totalSize - 1 else (start + partSize - 1)

                // Salta chunk già completati
                if (downloadedSoFar > end) continue

                jobs.add(async(Dispatchers.IO) {
                    var attempt = 0
                    var success = false
                    var written = 0L // Dichiarata qui per essere visibile fuori dal blocco use
                    while (attempt < 3 && !success && isActive) {
                        attempt++
                        try {
                            val connection = (url.openConnection() as HttpURLConnection).apply {
                                setRequestProperty("Range", "bytes=$start-$end")
                                setRequestProperty("Authorization", "Bearer $accessToken")
                                connectTimeout = 10000
                                readTimeout = 15000
                            }

                            if (connection.responseCode != HttpURLConnection.HTTP_OK &&
                                connection.responseCode != HttpURLConnection.HTTP_PARTIAL
                            ) {
                                Log.w("ModelDownloadWorker", "Errore chunk $i: ${connection.responseCode}")
                                connection.disconnect()
                                throw IOException("Risposta server non valida")
                            }

                            connection.inputStream.use { input ->
                                RandomAccessFile(tempFile, "rw").use { raf ->
                                    raf.seek(start)
                                    val buffer = ByteArray(64 * 1024)
                                    var bytesRead = input.read(buffer)
                                    written = 0L // Resettata per ogni tentativo
                                    while (bytesRead != -1 && isActive) {
                                        raf.write(buffer, 0, bytesRead)
                                        written += bytesRead
                                        val downloaded = totalDownloaded.addAndGet(bytesRead.toLong())
                                        val currentProgress = ((downloaded * 100) / totalSize).toInt()

                                        if (currentProgress > lastProgress) {
                                            lastProgress = currentProgress
                                            val notification = buildNotification(currentProgress, cancelIntent).build()
                                            notificationManager.notify(NOTIFICATION_ID, notification)
                                            setProgress(workDataOf(KEY_BYTES_DOWNLOADED to downloaded, KEY_TOTAL_BYTES to totalSize))
                                        }

                                        bytesRead = input.read(buffer)
                                    }
                                }
                            }

                            if (written == (end - start + 1)) {
                                success = true
                            } else {
                                Log.w("ModelDownloadWorker", "Chunk incompleto ($start-$end), retry...")
                            }
                        } catch (e: Exception) {
                            Log.w("ModelDownloadWorker", "Errore chunk $i tentativo $attempt: ${e.message}")
                            delay(1000L * attempt) // Backoff progressivo
                        } finally {
                            try {
                                (url.openConnection() as HttpURLConnection).disconnect()
                            } catch (e: Exception) {
                                Log.w("ModelDownloadWorker", "Errore chiusura connessione: ${e.message}")
                            }
                        }
                    }
                    if (!success && isActive) {
                        throw IOException("Impossibile scaricare chunk $i dopo 3 tentativi")
                    }
                })
            }

            jobs.awaitAll()

            if (!isActive) {
                tempFile.delete()
                return@coroutineScope Result.failure()
            }

            finalFile.delete()
            if (!tempFile.renameTo(finalFile)) {
                Log.e("ModelDownloadWorker", "Impossibile rinominare il file temporaneo.")
                throw IOException("Impossibile rinominare il file temporaneo.")
            }

            ModelSettingsManager.updateDmModelFilePath(destinationPath, context)

            val completedNotification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle("Download Completato")
                .setContentText("Modello pronto per l'uso.")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .build()
            notificationManager.notify(NOTIFICATION_ID, completedNotification)

            Result.success()
        } catch (e: Exception) {
            Log.e("ModelDownloadWorker", "Errore durante il download: ${e.message}", e)
            tempFile.delete()
            return@coroutineScope Result.failure()
        }
    }
}