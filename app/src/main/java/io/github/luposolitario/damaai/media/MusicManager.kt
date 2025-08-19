package io.github.luposolitario.damaai.media

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

object MusicManager {

    private var mediaPlayer: MediaPlayer? = null
    private val musicScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var currentVolume: Float = 0.5f // Default volume

    private var appContext: Context? = null
    private var isInitialized = false

    var isMuted: Boolean = false
        private set // Leggibile pubblicamente, ma modificabile solo dall'interno

    fun play(context: Context, @RawRes trackId: Int, isMusicEnabled: Boolean) {
        // Controlla sempre che tutto sia pronto prima di agire
        if (isMusicEnabled) {
            if (!isInitialized) return
            // Stop and release any existing player
            stop()

            // Create a new media player instance
            mediaPlayer = MediaPlayer.create(context, trackId).apply {
                isLooping = true // Loop the music
                val volumeToApply = if (isMuted) 0f else currentVolume
                setVolume(volumeToApply, volumeToApply)
                start()
            }
        } else {
            stop()
        }
    }

    fun stop() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
        }
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun release() {
        stop()
        musicScope.cancel()
        isInitialized = false // Permette una reinizializzazione se necessario
    }
    fun setVolume(volume: Float) {
        currentVolume = volume
        // Applica il volume solo se non è attivo il muto
        if (!isMuted) {
            mediaPlayer?.setVolume(volume, volume)
        }
    }

    /**
     * Inverte lo stato di muto (on/off) e ritorna il nuovo stato.
     * @return Il nuovo stato di isMuted (true se muto, false altrimenti).
     */
    fun toggleMute(): Boolean {
        isMuted = !isMuted
        if (isMuted) {
            mediaPlayer?.setVolume(0f, 0f)
        } else {
            mediaPlayer?.setVolume(currentVolume, currentVolume)
        }
        return isMuted
    }

    fun setEnabled(isEnabled: Boolean) {
        isInitialized = isEnabled
    }

}