package io.github.luposolitario.damaai.engine

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean // <-- IMPORTA QUESTO

class GemmaEngine : InferenceEngine {
    private val tag = "GemmaEngine"
    private var llmInference: LlmInference? = null
    private var session: LlmInferenceSession? = null
    private var sessionOptions: LlmInferenceSession.LlmInferenceSessionOptions? = null

    // --- NUOVA PARTE: "SICURA" PER LE RICHIESTE CONCORRENTI ---
    // Usiamo AtomicBoolean per la sicurezza tra thread.
    // Impedirà di chiamare Gemma mentre è già occupato.
    private val isGenerating = AtomicBoolean(false)

    companion object {
        private const val MAX_TOKENS = 4096
        private const val TOP_K = 10
        private const val TEMPERATURE = 1.0f
        private const val TOP_P = 1.0f
    }

    override suspend fun load(context: Context, modelPath: String) {
        if (!File(modelPath).exists()) {
            throw IllegalStateException("Modello Gemma non trovato al percorso: $modelPath")
        }

        try {
            val inferenceOptions = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(MAX_TOKENS)
                .build()

            llmInference = LlmInference.createFromOptions(context, inferenceOptions)

            sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
                .setTopK(TOP_K)
                .setTemperature(TEMPERATURE)
                .setTopP(TOP_P)
                .build()

            session = LlmInferenceSession.createFromOptions(llmInference, sessionOptions)
            Log.d(tag, "Motore e sessione Gemma caricati con successo.")

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
            // Aspetta che qualsiasi generazione in corso finisca prima di resettare
            while (isGenerating.get()) {
                kotlinx.coroutines.delay(100)
            }
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

    // --- METODO DI GENERAZIONE MODIFICATO ---
    override fun generateMove(prompt: String, boardState: String): Flow<String> = callbackFlow {
        // Controlla se il motore è già occupato.
        // compareAndSet è un'operazione atomica: imposta a true solo se il valore attuale è false.
        if (!isGenerating.compareAndSet(false, true)) {
            Log.w(tag, "Il motore è occupato. La nuova richiesta verrà ignorata.")
            close(IllegalStateException("Il motore è già occupato con una richiesta precedente."))
            return@callbackFlow
        }

        if (session == null) {
            trySend("[ERRORE: Sessione non inizializzata]").isSuccess
            close()
            isGenerating.set(false) // Sblocca la sicura in caso di errore
            return@callbackFlow
        }

        try {
            val fullPrompt = "$prompt\n\nHere is the chessboard:\n$boardState"
            session!!.addQueryChunk(fullPrompt)
            session!!.generateResponseAsync { partialResponse, done ->
                if (isActive) {
                    partialResponse?.let { trySend(it) }
                }
                if (done) {
                    close() // Questo triggererà awaitClose
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Errore in generateResponseAsync (testuale).", e)
            close(e) // Questo triggererà awaitClose
        }

        // Questo blocco viene eseguito SEMPRE, sia che la generazione finisca
        // con successo, sia che venga cancellata o che vada in errore.
        awaitClose {
            Log.d(tag, "Flow chiuso. Rilascio della sicura.")
            isGenerating.set(false) // Fondamentale: sblocca la sicura!
        }
    }
}