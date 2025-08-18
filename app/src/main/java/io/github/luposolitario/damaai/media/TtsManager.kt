package io.github.luposolitario.damaai.media

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.LinkedList
import java.util.Locale
import java.util.Queue

object TtsManager : TextToSpeech.OnInitListener {
    // --- NUOVO: Tag per filtrare facilmente i log ---
    private const val TAG = "TTS_DEBUG"

    private var tts: TextToSpeech? = null
    private var isReady = false
    private val speechQueue: Queue<String> = LinkedList()

    fun initialize(context: Context) {
        Log.d(TAG, "initialize_CALL: Chiamato metodo initialize.")
        if (isReady) {
            Log.d(TAG, "initialize_SKIP: TTS è già pronto e inizializzato.")
            return
        }
        if (tts == null) {
            Log.d(TAG, "initialize_ACTION: Creo una nuova istanza di TextToSpeech.")
            tts = TextToSpeech(context.applicationContext, this)
        } else {
            Log.w(TAG, "initialize_WARN: Esiste un'istanza TTS ma non è pronta. L'inizializzazione potrebbe essere ancora in corso.")
        }
    }

    override fun onInit(status: Int) {
        Log.d(TAG, "onInit_CALLBACK: Ricevuto callback con status code: $status")
        when (status) {
            TextToSpeech.SUCCESS -> {
                val result = tts?.setLanguage(Locale.ITALIAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "onInit_ERROR: Lingua Italiana non supportata.")
                    isReady = false
                } else {
                    Log.d(TAG, "onInit_SUCCESS: TTS inizializzato con successo.")
                    isReady = true
                    processQueue()
                }
            }
            else -> {
                Log.e(TAG, "onInit_FAILURE: Inizializzazione TTS fallita. Status code: $status")
                isReady = false
            }
        }
    }

    fun speak(text: String) {
        Log.d(TAG, "speak_CALL: Chiamato metodo speak. Stato 'isReady': $isReady")
        if (text.isEmpty()) {
            Log.w(TAG, "speak_SKIP: Testo vuoto.")
            return
        }

        if (isReady) {
            Log.d(TAG, "speak_ACTION: Parlo immediatamente.")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.w(TAG, "speak_QUEUE: TTS non pronto. Aggiungo alla coda. Dimensione attuale coda: ${speechQueue.size}")
            speechQueue.add(text)
        }
    }

    private fun processQueue() {
        Log.d(TAG, "processQueue_CALL: Chiamato. Dimensione coda: ${speechQueue.size}")
        if (speechQueue.isEmpty()) {
            Log.d(TAG, "processQueue_SKIP: La coda è vuota.")
            return
        }
        Log.d(TAG, "processQueue_ACTION: Processo ${speechQueue.size} elementi dalla coda.")
        while (speechQueue.isNotEmpty()) {
            val textToSpeak = speechQueue.poll()
            if (textToSpeak != null) {
                tts?.speak(textToSpeak, TextToSpeech.QUEUE_ADD, null, null)
            }
        }
    }

    fun shutdown() {
        Log.d(TAG, "shutdown_CALL: Chiamato metodo shutdown.")
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
        speechQueue.clear()
        Log.d(TAG, "shutdown_SUCCESS: TTS spento e coda pulita.")
    }
}