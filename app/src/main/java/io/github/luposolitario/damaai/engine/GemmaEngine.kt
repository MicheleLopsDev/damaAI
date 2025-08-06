package io.github.luposolitario.damaai.engine

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder

import com.google.mediapipe.tasks.genai.llminference.GraphOptions
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

class GemmaEngine : InferenceEngine {
    private val tag = "GemmaEngine"
    private var llmInference: LlmInference? = null
    private var session: LlmInferenceSession? = null

    companion object {
        private const val MAX_TOKENS = 2048
        private const val TOP_K = 40
        private const val TEMPERATURE = 0.8f
        private const val TOP_P = 1.0f
    }

    override suspend fun load(context: Context, modelPath: String) {
        if (!File(modelPath).exists()) {
            throw IllegalStateException("Modello Gemma non trovato al percorso: $modelPath")
        }

        try {
            // Configurazione per l'inferenza, abilitando il supporto per 1 immagine.
            val inferenceOptions = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(MAX_TOKENS)
                .setMaxNumImages(1) // Abilita il supporto per le immagini
                .build()

            llmInference = LlmInference.createFromOptions(context, inferenceOptions)

            // Configurazione per la sessione, abilitando la modalità "vision".
            val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
                .setTopK(TOP_K)
                .setTemperature(TEMPERATURE)
                .setTopP(TOP_P)
                .setGraphOptions(
                    GraphOptions.builder()
                        .setEnableVisionModality(true) // Cruciale per l'analisi delle immagini
                        .build()
                )
                .build()

            session = LlmInferenceSession.createFromOptions(llmInference, sessionOptions)
            Log.d(tag, "Motore e sessione Gemma (multimodale) caricati con successo.")

        } catch (e: Exception) {
            Log.e(tag, "Errore durante il caricamento del motore Gemma.", e)
            unload() // Pulisce le risorse in caso di fallimento
            throw e
        }
    }

    override suspend fun unload() {
        try {
            session?.close()
            llmInference?.close()
        } catch (e: Exception) {
            Log.e(tag, "Errore durante il rilascio delle risorse.", e)
        } finally {
            session = null
            llmInference = null
            Log.d(tag, "Motore e sessione Gemma rilasciati.")
        }
    }

    override fun generateMove(prompt: String, bitmap: Bitmap): Flow<String> = callbackFlow {
        if (session == null) {
            val errorMsg = "[ERRORE: Sessione Gemma non inizializzata]"
            Log.e(tag, errorMsg)
            trySend(errorMsg).isSuccess
            close()
            return@callbackFlow
        }

        try {
            // 1. Aggiungi il prompt di testo
            session?.addQueryChunk(prompt)

            // 2. Converte e aggiunge il Bitmap
            val mediapipeImage = BitmapImageBuilder(bitmap).build()
            session?.addImage(mediapipeImage)

            // 3. Avvia la generazione della risposta asincrona
            session?.generateResponseAsync { partialResponse, done ->
                partialResponse?.let {
                    trySend(it).isSuccess
                }
                if (done) {
                    close()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Errore durante la generazione della mossa.", e)
            close(e)
        }

        awaitClose { Log.d(tag, "Flow per generateMove chiuso.") }
    }
}