package io.github.luposolitario.damaai.game_logic

/**
 * Rappresenta una posizione (casella) sulla scacchiera.
 * Contiene le coordinate numeriche (riga, colonna) da 0 a 7.
 */
data class Posizione(val riga: Int, val colonna: Int) {
    /**
     * Converte le coordinate numeriche (es: riga 0, colonna 0)
     * nella notazione algebrica standard (es: "A1").
     */
    fun toNotazioneAlgebrica(): String {
        // La colonna 0 diventa 'A', la 1 'B', e così via.
        val colonnaChar = 'A' + colonna
        // La riga 0 diventa '1', la 1 '2', e così via.
        val rigaChar = '1' + riga
        return "$colonnaChar$rigaChar"
    }
}