package io.github.luposolitario.damaai.media

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import io.github.luposolitario.damaai.data.Gender
import java.util.LinkedList
import java.util.Locale
import java.util.Queue

object TtsManager : TextToSpeech.OnInitListener {
    private const val TAG = "TTS_DEBUG"
    private data class SpeechRequest(val text: String, val gender: Gender)

    // --- PROVA QUESTE VOCI ---
    // Ho fatto un'ipotesi. Se non sono corrette,
    // sostituisci queste stringhe con altri nomi dalla tua lista log.
    private const val FEMALE_VOICE_NAME = "it-it-x-kda-network"
    private const val MALE_VOICE_NAME = "it-it-x-itd-network"
    // -------------------------

    private var tts: TextToSpeech? = null
    private var isReady = false
    private val speechQueue: Queue<SpeechRequest> = LinkedList()
    private var availableVoices: List<Voice> = emptyList()

    fun initialize(context: android.content.Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            availableVoices = tts?.voices?.toList() ?: emptyList()
            // Log per vedere tutte le voci (lo lascio per riferimento)
            availableVoices.forEach { voice ->
                Log.i(TAG, "Voce disponibile: Nome='${voice.name}', Lingua='${voice.locale}'")
            }
            isReady = true
            processQueue()
        } else {
            Log.e(TAG, "Inizializzazione TTS fallita. Status code: $status")
        }
    }

    fun speak(text: String, gender: Gender) {
        if (isReady) {
            setVoiceAndSpeak(text, gender)
        } else {
            speechQueue.add(SpeechRequest(text, gender))
        }
    }

    private fun setVoiceAndSpeak(text: String, gender: Gender) {
        // --- LOGICA MODIFICATA ---
        // Ora cerca il nome esatto specificato nelle costanti.
        val targetVoiceName = if (gender == Gender.FEMALE) FEMALE_VOICE_NAME else MALE_VOICE_NAME
        Log.d(TAG, "Cerco la voce con nome esatto: '$targetVoiceName'")

        val selectedVoice = availableVoices.firstOrNull { it.name == targetVoiceName }

        if (selectedVoice != null) {
            tts?.voice = selectedVoice
            Log.d(TAG, "Voce impostata a: ${selectedVoice.name}")
        } else {
            Log.w(TAG, "Voce '$targetVoiceName' non trovata! Uso la predefinita per l'italiano.")
            // Fallback alla prima voce italiana disponibile se quella specificata non esiste
            tts?.voice = availableVoices.firstOrNull { it.locale == Locale.ITALIAN }
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun processQueue() {
        while (speechQueue.isNotEmpty()) {
            val request = speechQueue.poll()
            if (request != null) {
                setVoiceAndSpeak(request.text, request.gender)
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