package io.github.luposolitario.damaai.media

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

object TtsManager : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isReady = false

    fun init(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.ITALIAN
            isReady = true
            Log.d("TtsManager", "TTS Engine inizializzato con successo in italiano.")
        } else {
            Log.e("TtsManager", "Errore durante l'inizializzazione del TTS Engine.")
        }
    }

    fun speak(text: String) {
        if (isReady && text.isNotBlank()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.w("TtsManager", "TTS non pronto o testo vuoto. Impossibile parlare.")
        }
    }

    fun shutdown() {
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isReady = false
            Log.d("TtsManager", "TTS Engine rilasciato.")
        }
    }
}