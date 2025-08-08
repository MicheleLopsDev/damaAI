package io.github.luposolitario.damaai.engine

import android.util.Log
import io.github.luposolitario.damaai.data.Piece // Assicurati che l'import sia corretto
import io.github.luposolitario.damaai.data.PlayerColor
import io.github.luposolitario.damaai.game_logic.*

class DamaEngineImpl : DamaEngine {

    private var partita: PartitaDiDama? = null
    private val TAG = "DamaEngineImpl"
    // Memorizziamo lo stato precedente per trovare la pedina catturata
    private var statoPrecedente: String = ""

    override fun nuovaPartita(difficolta: Difficolta) {
        Log.d(TAG, "nuovaPartita chiamata con difficoltà: $difficolta")
        partita = PartitaDiDama(difficolta)
        statoPrecedente = partita?.getStatoScacchiera() ?: ""
        Log.d(TAG, "Partita creata. Stato iniziale scacchiera:\n${partita?.getStatoScacchiera()}")
    }

    /**
     * Esegue SOLO la mossa del giocatore.
     * Restituisce la mossa eseguita se valida, altrimenti null.
     */
    override fun muoviPezzoUmano(mossaGiocatore: String): Mossa? {
        val partitaCorrente = partita ?: return null
        if (partitaCorrente.getVincitore() != null) return null

        val mosseValide = partitaCorrente.getMosseValide()
        val mossaUtente = parseMossa(mossaGiocatore) ?: return null

        val mossaCompletaDaEseguire = mosseValide.find {
            it.partenza == mossaUtente.partenza && it.arrivo == mossaUtente.arrivo
        }

        if (mossaCompletaDaEseguire != null) {
            statoPrecedente = partitaCorrente.getStatoScacchiera() // Salviamo lo stato PRIMA della mossa
            partitaCorrente.eseguiMossaUmano(mossaCompletaDaEseguire)
            return mossaCompletaDaEseguire
        }
        return null
    }

    /**
     * Esegue SOLO la mossa dell'IA.
     * Restituisce la mossa eseguita, o null se non può muovere.
     */
    override fun faiMossaIA(): Mossa? {
        val partitaCorrente = partita ?: return null
        if (partitaCorrente.getVincitore() != null) return null

        statoPrecedente = partitaCorrente.getStatoScacchiera() // Salviamo lo stato PRIMA della mossa
        val mossaIA = partitaCorrente.faiMossaIA()
        Log.d(TAG, "Mossa IA eseguita. Stato scacchiera FINALE:\n${partitaCorrente.getStatoScacchiera()}")
        return mossaIA
    }

    /**
     * Trova la pedina catturata confrontando lo stato attuale con quello precedente.
     */
    override fun trovaPedinaCatturata(mossa: Mossa): Piece? {
        if (mossa.posizionePezzoCatturato == null) return null

        // Troviamo quale pedina si trovava in quella posizione nello stato *precedente*
        val righePrecedenti = statoPrecedente.trim().lines().drop(1)
        val pos = mossa.posizionePezzoCatturato!!

        if (pos.riga >= righePrecedenti.size) return null
        val rigaString = righePrecedenti[pos.riga]
        val charIndex = 2 + pos.colonna * 2
        if (charIndex >= rigaString.length) return null

        val simbolo = rigaString[charIndex]
        val colore = when (simbolo.lowercaseChar()) {
            'b' -> PlayerColor.WHITE
            'n' -> PlayerColor.BLACK
            else -> null
        }
        return colore?.let { Piece(row = pos.riga, col = pos.colonna, color = it) }
    }


    override fun getVincitore(): Colore? {
        return partita?.getVincitore()
    }

    override fun getStatoScacchiera(): String {
        return partita?.getStatoScacchiera() ?: "Partita non iniziata."
    }

    override fun getMosseValide(): List<String> {
        return partita?.getMosseValide()?.map { it.toString() } ?: emptyList()
    }

    private fun parseMossa(notazione: String): Mossa? {
        val parti = notazione.trim().uppercase().split(" ")
        if (parti.size != 2) return null
        val partenza = fromNotazioneAlgebrica(parti[0])
        val arrivo = fromNotazioneAlgebrica(parti[1])
        return if (partenza != null && arrivo != null) Mossa(partenza, arrivo) else null
    }

    private fun fromNotazioneAlgebrica(notazione: String): Posizione? {
        if (notazione.length != 2) return null
        val colonnaChar = notazione[0]
        val rigaChar = notazione[1]
        if (colonnaChar !in 'A'..'H' || rigaChar !in '1'..'8') return null
        val colonna = colonnaChar - 'A'
        val riga = '8' - rigaChar
        return Posizione(riga, colonna)
    }
}