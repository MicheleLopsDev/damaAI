package io.github.luposolitario.damaai.game_logic

/**
 * Rappresenta una posizione (casella) sulla scacchiera.
 * Contiene le coordinate numeriche (riga, colonna) da 0 a 7.
 */
data class Posizione(val riga: Int, val colonna: Int) {
    /**
     * Converte le coordinate numeriche (es: riga 0, colonna 0)
     * nella notazione algebrica standard (es: "A8").
     * La riga 0 nel modello dati corrisponde alla riga '8' sulla scacchiera.
     */
    fun toNotazioneAlgebrica(): String {
        val colonnaChar = 'A' + colonna
        val rigaChar = '8' - riga
        return "$colonnaChar$rigaChar"
    }

    // --- NUOVA AGGIUNTA ---
    companion object {
        /**
         * Converte la notazione algebrica (es: "A8") nelle coordinate
         * numeriche interne del modello (es: Posizione(0, 0)).
         * @param notazione La stringa in formato algebrico (es. "B4").
         * @return L'oggetto Posizione corrispondente, o null se la notazione non è valida.
         */
        fun fromAlgebraic(notazione: String): Posizione? {
            if (notazione.length != 2) return null

            val colonnaChar = notazione.getOrNull(0)?.uppercaseChar() ?: return null
            val rigaChar = notazione.getOrNull(1) ?: return null

            if (colonnaChar !in 'A'..'H' || rigaChar !in '1'..'8') return null

            val colonna = colonnaChar - 'A'
            val riga = '8' - rigaChar

            return Posizione(riga, colonna)
        }
    }
    // --- FINE AGGIUNTA ---
}