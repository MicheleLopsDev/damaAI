// michelelopsdev/damaai/damaAI-4607344960c2303f34c37dc7e118d6e6fbf7c21e/app/src/main/java/io/github/luposolitario/damaai/game_logic/OpeningBook.kt
package io.github.luposolitario.damaai.game_logic

object OpeningBook {

    private val book = mutableMapOf<Long, List<String>>()

    init {
        popolaLibro()
    }

    private fun popolaLibro() {
        // --- POSIZIONE INIZIALE (Turno del Bianco) ---
        // Hash: 0L
        book[0L] = listOf(
            "C3 B4", // Apertura Britannica
            "F3 E4", // Apertura Spagnola
            "C3 D4"  // Apertura del Centro
        )

        // --- RISPOSTE DEL NERO all'Apertura Britannica (C3 B4) ---
        // Hash dopo "C3 B4": 1583969215038038800L (calcolato con Zobrist)
        book[1583969215038038800L] = listOf(
            "F6 E5", // Risposta simmetrica
            "F6 G5"
        )

        // --- RISPOSTE DEL NERO all'Apertura Spagnola (F3 E4) ---
        // Hash dopo "F3 E4": -7944722512643388700L
        book[-7944722512643388700L] = listOf(
            "C6 D5", // Risposta simmetrica
            "C6 B5"
        )

        // --- CONTROMOSSA DEL BIANCO dopo C3 B4, F6 E5 ---
        // Hash: -2188686948509012500L
        book[-2188686948509012500L] = listOf(
            "D2 C3",
            "B4 A5"
        )
    }

    fun getMove(hash: Long): String? {
        return book[hash]?.randomOrNull()
    }
}