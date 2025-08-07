package io.github.luposolitario.damaai.game_logic

/**
 * Rappresenta una mossa, da una posizione di partenza a una di arrivo.
 * Può anche contenere la posizione del pezzo catturato, se presente.
 */
data class Mossa(
    val partenza: Posizione,
    val arrivo: Posizione,
    val posizionePezzoCatturato: Posizione? = null // La nostra aggiunta!
) {
    /**
     * Restituisce la mossa nel formato che hai richiesto, es: "D4 E5".
     */
    override fun toString(): String {
        return "${partenza.toNotazioneAlgebrica()} ${arrivo.toNotazioneAlgebrica()}"
    }
}