package io.github.luposolitario.damaai.utils

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

class MusicManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun start(@RawRes musicResId: Int) {
        // Se c'è già una musica in riproduzione, fermala prima di avviarne una nuova.
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        mediaPlayer = MediaPlayer.create(context, musicResId)

        // Imposta il volume al 25%
        mediaPlayer?.setVolume(0.25f, 0.25f)

        // Imposta il loop
        mediaPlayer?.isLooping = true

        // Avvia la riproduzione
        mediaPlayer?.start()
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
