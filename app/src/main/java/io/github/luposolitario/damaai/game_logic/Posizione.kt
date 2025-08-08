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
        // La colonna 0 diventa 'A', la 1 'B', e così via. (Corretto)
        val colonnaChar = 'A' + colonna
        // La riga 0 del modello dati (in alto) deve diventare '8'. (Modificato)
        val rigaChar = '8' - riga
        return "$colonnaChar$rigaChar"
    }
}