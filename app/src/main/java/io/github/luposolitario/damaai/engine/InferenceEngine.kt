package io.github.luposolitario.damaai.engine

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Interfaccia generica per un motore di inferenza.
 * Definisce il contratto per caricare modelli, generare risposte
 * e gestire le risorse.
 */
interface InferenceEngine {

    /**
     * Carica il modello di inferenza dalla memoria del dispositivo.
     *
     * @param context Il contesto dell'applicazione.
     * @param modelPath Il percorso assoluto del file del modello.
     * @throws Exception se il caricamento fallisce.
     */
    suspend fun load(context: Context, modelPath: String)

    /**
     * Rilascia tutte le risorse utilizzate dal motore di inferenza (modello, sessione).
     */
    suspend fun unload()

    /**
     * Genera una mossa analizzando un'immagine della scacchiera e un prompt testuale.
     *
     * @param prompt Il prompt di testo che guida l'IA.
     * @param bitmap L'immagine della scacchiera da analizzare.
     * @return Un Flow che emette la risposta dell'IA in chunk.
     */
//    fun generateMove(prompt: String, bitmap: Bitmap): Flow<String>

    suspend fun resetSession()

    fun generateMove(prompt: String, boardState: String): Flow<String>
}