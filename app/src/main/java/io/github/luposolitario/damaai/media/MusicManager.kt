package io.github.luposolitario.damaai.media

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

object MusicManager {

    private var mediaPlayer: MediaPlayer? = null
    private var currentVolume: Float = 0.5f // Default volume
    var isMuted: Boolean = false
        private set // Leggibile pubblicamente, ma modificabile solo dall'interno

    fun play(context: Context, @RawRes trackId: Int) {
        // Stop and release any existing player
        stop()

        // Create a new media player instance
        mediaPlayer = MediaPlayer.create(context, trackId).apply {
            isLooping = true // Loop the music
            val volumeToApply = if (isMuted) 0f else currentVolume
            setVolume(volumeToApply, volumeToApply)
            start()
        }
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
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
}