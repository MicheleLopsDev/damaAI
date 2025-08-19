// michelelopsdev/damaai/damaAI-4607344960c2303f34c37dc7e118d6e6fbf7c21e/app/src/main/java/io/github/luposolitario/damaai/game_logic/Mossa.kt
package io.github.luposolitario.damaai.game_logic

/**
 * Rappresenta una mossa. Ora contiene tutte le informazioni necessarie
 * per essere eseguita e annullata, rendendola una struttura dati completa.
 */
data class Mossa(
    val partenza: Posizione,
    val arrivo: Posizione,
    val posizionePezzoCatturato: Posizione? = null,
    // --- NUOVI CAMPI PER ANNULLAMENTO MOSSA ---
    val pezzoCatturato: Pezzo? = null, // Quale pezzo è stato catturato
    val isPromozione: Boolean = false  // La mossa ha causato una promozione?
) {
    fun isCattura(): Boolean = posizionePezzoCatturato != null

    override fun toString(): String {
        return "${partenza.toNotazioneAlgebrica()} ${arrivo.toNotazioneAlgebrica()}"
    }
}