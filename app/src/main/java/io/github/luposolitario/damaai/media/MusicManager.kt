package io.github.luposolitario.damaai.media

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import io.github.luposolitario.damaai.datastore.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MusicManager(
    private val context: Context,
    settingsManager: SettingsManager,
    externalScope: CoroutineScope
) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentVolume: Float = 0.5f
    private var isMusicEnabled: Boolean = true
    private var isGloballyMuted: Boolean = false

    init {
        externalScope.launch {
            combine(
                settingsManager.musicVolumeFlow,
                settingsManager.isMusicEnabledFlow
            ) { volume, isEnabled ->
                currentVolume = volume
                isMusicEnabled = isEnabled
                if (!isEnabled) {
                    stop()
                } else {
                    updateVolume()
                }
            }.collect {} // Terminal operator to start the flow collection
        }
    }

    fun play(@RawRes trackId: Int) {
        if (!isMusicEnabled) {
            stop()
            return
        }

        stop()

        mediaPlayer = MediaPlayer.create(context, trackId).apply {
            isLooping = true
            updateVolume()
            start()
        }
    }

    fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    fun setGlobalMute(isMuted: Boolean) {
        isGloballyMuted = isMuted
        updateVolume()
    }

    private fun updateVolume() {
        val volumeToApply = if (isGloballyMuted) 0f else currentVolume
        mediaPlayer?.setVolume(volumeToApply, volumeToApply)
    }

    fun release() {
        stop()
    }
}