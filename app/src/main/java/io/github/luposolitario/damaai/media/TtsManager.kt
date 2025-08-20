package io.github.luposolitario.damaai.media

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import io.github.luposolitario.damaai.data.Gender
import io.github.luposolitario.damaai.datastore.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Locale
import java.util.Queue

class TtsManager(
    private val context: Context,
    settingsManager: SettingsManager,
    externalScope: CoroutineScope
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var isTtsEnabled: Boolean = true
    private var isGloballyMuted: Boolean = false

    private val speechQueue: Queue<SpeechRequest> = LinkedList()
    private var availableVoices: List<Voice> = emptyList()

    private data class SpeechRequest(val text: String, val gender: Gender)

    companion object {
        private const val TAG = "TTS_DEBUG"
        private const val FEMALE_VOICE_NAME = "it-it-x-kda-network"
        private const val MALE_VOICE_NAME = "it-it-x-itd-network"
    }

    init {
        tts = TextToSpeech(context.applicationContext, this)

        externalScope.launch {
            settingsManager.isTtsEnabledFlow.collect { enabled ->
                isTtsEnabled = enabled
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            availableVoices = tts?.voices?.toList() ?: emptyList()
            isReady = true
            processQueue()
        } else {
            Log.e(TAG, "TTS initialization failed. Status code: $status")
        }
    }

    fun speak(text: String, gender: Gender) {
        if (!isTtsEnabled || isGloballyMuted) {
            return
        }

        if (isReady) {
            setVoiceAndSpeak(text, gender)
        } else {
            speechQueue.add(SpeechRequest(text, gender))
        }
    }

    fun setGlobalMute(isMuted: Boolean) {
        isGloballyMuted = isMuted
        if(isMuted){
            tts?.stop()
        }
    }

    private fun setVoiceAndSpeak(text: String, gender: Gender) {
        val targetVoiceName = if (gender == Gender.FEMALE) FEMALE_VOICE_NAME else MALE_VOICE_NAME
        val selectedVoice = availableVoices.firstOrNull { it.name == targetVoiceName }

        if (selectedVoice != null) {
            tts?.voice = selectedVoice
        } else {
            Log.w(TAG, "Voice '$targetVoiceName' not found! Using default for Italian.")
            tts?.voice = availableVoices.firstOrNull { it.locale == Locale.ITALIAN }
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun processQueue() {
        while (speechQueue.isNotEmpty()) {
            speechQueue.poll()?.let {
                setVoiceAndSpeak(it.text, it.gender)
            }
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
        speechQueue.clear()
    }
}