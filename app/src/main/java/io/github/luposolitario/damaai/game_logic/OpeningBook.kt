// File: OpeningBook.kt
package io.github.luposolitario.damaai.game_logic

/**
 * Un semplice "libro di aperture" che associa l'hash di una posizione
 * a una lista di mosse raccomandate in notazione algebrica (es. "C3 B4").
 */
object OpeningBook {

    // La mappa che contiene la nostra conoscenza. La chiave è l'hash Zobrist della posizione.
    private val book = mutableMapOf<Long, List<String>>()

    init {
        // Popoliamo il libro con alcune aperture standard.
        // L'hash della posizione iniziale è 0L se il Bianco muove.

        // Posizione Iniziale (Turno del Bianco)
        book[0L] = listOf(
            "C3 B4", // Apertura più comune
            "F3 E4",
            "C3 D4"
        )

        // Aggiungeremo altre posizioni man mano che le studiamo.
        // Esempio: dopo che il Bianco ha giocato "C3 B4", a chi tocca? Al Nero.
        // Dovremmo calcolare l'hash di quella posizione e aggiungere le risposte del Nero.
    }

    /**
     * Cerca una mossa nel libro per la posizione corrente.
     * @param hash L'hash Zobrist della posizione attuale.
     * @return Una mossa consigliata in formato stringa, o null se la posizione non è nel libro.
     */
    fun getMove(hash: Long): String? {
        return book[hash]?.randomOrNull()
    }
}