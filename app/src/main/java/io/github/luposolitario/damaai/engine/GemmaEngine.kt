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
import kotlinx.coroutines.isActive
import java.io.File

class GemmaEngine : InferenceEngine {
    private val tag = "GemmaEngine"
    private var llmInference: LlmInference? = null
    private var session: LlmInferenceSession? = null
    private var sessionOptions: LlmInferenceSession.LlmInferenceSessionOptions? = null

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
            unload()
            throw e
        }
    }

    override suspend fun resetSession() {
        if (llmInference == null || sessionOptions == null) {
            Log.e(tag, "Impossibile resettare: motore non inizializzato.")
            return
        }
        try {
            session?.close()
            session = LlmInferenceSession.createFromOptions(llmInference, sessionOptions)
            Log.d(tag, "Sessione resettata.")
        } catch (e: Exception) {
            Log.e(tag, "Errore durante il reset della sessione.", e)
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
        Log.d(tag, "Chiamata a generateMove con Bitmap (compatibilità).")
        if (session == null) {
            trySend("[ERRORE: Sessione non inizializzata]").isSuccess
            close()
            return@callbackFlow
        }
        try {
            session?.addQueryChunk(prompt)
            val mediapipeImage = BitmapImageBuilder(bitmap).build()
            session?.addImage(mediapipeImage)
            session?.generateResponseAsync { partialResponse, done ->
                partialResponse?.let { trySend(it).isSuccess }
                if (done) close()
            }
        } catch (e: Exception) { close(e) }
        awaitClose { Log.d(tag, "Flow (Bitmap) chiuso.") }
    }

    // NUOVO metodo testuale
    override fun generateMove(prompt: String, boardState: String): Flow<String> = callbackFlow {
        if (session == null) {
            trySend("[ERRORE: Sessione non inizializzata]").isSuccess
            close()
            return@callbackFlow
        }
        val fullResponse = StringBuilder()
        try {
            // Unisce il prompt principale con lo stato della scacchiera
            val fullPrompt = "$prompt\n\nHere is the chessboard:\n$boardState"
            session!!.addQueryChunk(fullPrompt)
            session!!.generateResponseAsync { partialResponse, done ->
                if (isActive) {
                partialResponse?.let {
                        fullResponse.append(it)
                        trySend(it)
                    }
                }
                if (done) {
                    close()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Errore in generateResponseAsync (testuale).", e)
            close(e)
        }
        awaitClose { Log.d(tag, "Flow (Testuale) chiuso.") }
    }
}