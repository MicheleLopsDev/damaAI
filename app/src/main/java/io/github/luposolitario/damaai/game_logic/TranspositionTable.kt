// File: TranspositionTable.kt
package io.github.luposolitario.damaai.game_logic

/**
 * Una "memoria cache" per l'IA, che memorizza le valutazioni delle posizioni già analizzate.
 */
class TranspositionTable {

    // Struttura che memorizza le informazioni di una posizione
    data class Entry(
        val score: Int,      // Il punteggio calcolato
        val depth: Int       // La profondità a cui è stato calcolato
    )

    // Usiamo una mappa per associare l'hash di una posizione alla sua Entry
    private val table = mutableMapOf<Long, Entry>()

    /**
     * Cerca una posizione nella tabella.
     * Restituisce il punteggio se la posizione è stata trovata e analizzata
     * a una profondità uguale o maggiore a quella richiesta.
     */
    fun probe(hash: Long, depth: Int): Int? {
        val entry = table[hash]
        return if (entry != null && entry.depth >= depth) {
            entry.score
        } else {
            null
        }
    }

    /**
     * Salva una nuova valutazione nella tabella.
     */
    fun store(hash: Long, score: Int, depth: Int) {
        table[hash] = Entry(score, depth)
    }

    /**
     * Svuota la tabella, da chiamare all'inizio di ogni nuova mossa dell'IA.
     */
    fun clear() {
        table.clear()
    }
}