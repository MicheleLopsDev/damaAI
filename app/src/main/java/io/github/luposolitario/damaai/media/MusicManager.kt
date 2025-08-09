package io.github.luposolitario.damaai.media

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

object MusicManager {

    private var mediaPlayer: MediaPlayer? = null
    private var currentVolume: Float = 0.5f // Default volume

    fun play(context: Context, @RawRes trackId: Int) {
        // Stop and release any existing player
        stop()

        // Create a new media player instance
        mediaPlayer = MediaPlayer.create(context, trackId).apply {
            isLooping = true // Loop the music
            setVolume(currentVolume, currentVolume)
            start()
        }
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun setVolume(volume: Float) {
        currentVolume = volume
        // Apply volume to the currently playing media player, if it exists
        mediaPlayer?.setVolume(volume, volume)
    }
}
