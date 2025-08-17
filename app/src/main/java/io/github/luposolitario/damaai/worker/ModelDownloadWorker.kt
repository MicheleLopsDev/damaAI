package io.github.luposolitario.damaai.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.github.luposolitario.damaai.datastore.ModelSettingsManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicLong

class ModelDownloadWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_URL = "key_url"
        const val KEY_DESTINATION = "key_destination"
        const val KEY_BYTES_DOWNLOADED = "key_bytes_downloaded"
        const val KEY_TOTAL_BYTES = "key_total_bytes"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "download_channel"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Download Modello In Corso")
            .setContentText("Preparazione...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setProgress(100, 0, true)
            .build()
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Canale Download",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifiche per i download in corso"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        val urlString = inputData.getString(KEY_URL) ?: return@coroutineScope Result.failure()
        val destinationPath = inputData.getString(KEY_DESTINATION) ?: return@coroutineScope Result.failure()

        val accessToken = ModelSettingsManager.getHuggingFaceToken(context) ?: return@coroutineScope Result.failure()
        if (accessToken.isEmpty()) {
            Log.e("ModelDownloadWorker", "Token di accesso non trovato.")
            return@coroutineScope Result.failure()
        }

        val finalFile = File(destinationPath)
        val tempFile = File("$destinationPath.tmp")

        try {
            val url = URL(urlString)
            val headConnection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "HEAD"
                setRequestProperty("Authorization", "Bearer $accessToken")
                connectTimeout = 10_000
                readTimeout = 15_000
                connect()
            }

            if (headConnection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("ModelDownloadWorker", "Errore Server: ${headConnection.responseCode}")
                return@coroutineScope Result.failure()
            }

            val totalSize = headConnection.contentLengthLong
            headConnection.disconnect()

            val totalDownloaded = AtomicLong(0)
            val jobs = mutableListOf<Deferred<Unit>>()

            // Numero thread dinamico (max 8 o numero CPU)
            val numThreads = minOf(8, Runtime.getRuntime().availableProcessors() * 2)
            val partSize = totalSize / numThreads

            for (i in 0 until numThreads) {
                val start = i * partSize
                val end = if (i == numThreads - 1) totalSize - 1 else (start + partSize - 1)

                jobs.add(async(Dispatchers.IO) {
                    var attempt = 0
                    var success = false
                    while (attempt < 3 && !success) {
                        attempt++
                        try {
                            val connection = (url.openConnection() as HttpURLConnection).apply {
                                setRequestProperty("Range", "bytes=$start-$end")
                                setRequestProperty("Authorization", "Bearer $accessToken")
                                connectTimeout = 10_000
                                readTimeout = 15_000
                            }

                            connection.inputStream.use { input ->
                                RandomAccessFile(tempFile, "rw").use { raf ->
                                    raf.seek(start)
                                    val buffer = ByteArray(64 * 1024)
                                    var bytes = input.read(buffer)
                                    var written = 0L
                                    while (bytes != -1) {
                                        raf.write(buffer, 0, bytes)
                                        written += bytes
                                        val downloaded = totalDownloaded.addAndGet(bytes.toLong())
                                        setProgress(workDataOf(KEY_BYTES_DOWNLOADED to downloaded, KEY_TOTAL_BYTES to totalSize))
                                        bytes = input.read(buffer)
                                    }

                                    // Validazione: chunk scaricato completamente
                                    if (written == (end - start + 1)) {
                                        success = true
                                    } else {
                                        Log.w("ModelDownloadWorker", "Chunk incompleto ($start-$end), retry...")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.w("ModelDownloadWorker", "Errore chunk $i tentativo $attempt: ${e.message}")
                            delay(1000L * attempt) // backoff progressivo
                        }
                    }
                    if (!success) throw IOException("Impossibile scaricare chunk $i dopo 3 tentativi")
                })
            }

            jobs.awaitAll()

            finalFile.delete()
            if (!tempFile.renameTo(finalFile)) throw IOException("Impossibile rinominare il file temporaneo.")

            ModelSettingsManager.updateDmModelFilePath(destinationPath, context)
            Result.success()
        } catch (e: Exception) {
            Log.e("ModelDownloadWorker", "Errore durante il download: ${e.message}", e)
            tempFile.delete()
            Result.failure()
        }
    }
}
