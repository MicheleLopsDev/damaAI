package io.github.luposolitario.damaai.utils

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

class MusicManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentVolume: Float = 0.25f // Default volume as per user request

    fun playTrack(@RawRes musicResId: Int) {
        // If music is already playing, stop and release it before starting a new track.
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
        }
        // Release the previous instance regardless of its state
        mediaPlayer?.release()

        mediaPlayer = MediaPlayer.create(context, musicResId).apply {
            // Set the volume to the current stored level
            setVolume(currentVolume, currentVolume)

            // The preview should not loop, as requested
            isLooping = false

            // Start playback
            start()

            // Set a completion listener to release the player when the track finishes
            setOnCompletionListener {
                it.release()
                mediaPlayer = null
            }
        }
    }

    fun setVolume(volume: Float) {
        // Clamp the volume between 0.0 and 1.0
        currentVolume = volume.coerceIn(0.0f, 1.0f)
        // Apply the volume to the currently playing media player, if it exists
        mediaPlayer?.setVolume(currentVolume, currentVolume)
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
