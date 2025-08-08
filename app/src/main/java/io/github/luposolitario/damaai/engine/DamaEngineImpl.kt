package io.github.luposolitario.damaai.engine

import android.util.Log
import io.github.luposolitario.damaai.game_logic.*

class DamaEngineImpl : DamaEngine {

    private var partita: PartitaDiDama? = null
    private val TAG = "DamaEngineImpl"

    override fun nuovaPartita(difficolta: Difficolta) {
        Log.d(TAG, "nuovaPartita chiamata con difficoltà: $difficolta")
        partita = PartitaDiDama(difficolta)
        Log.d(TAG, "Partita creata. Stato iniziale scacchiera:\n${partita?.getStatoScacchiera()}")
    }

    override fun muoviPezzo(mossaGiocatore: String): String? {
        Log.d(TAG, "muoviPezzo chiamato con mossa: '$mossaGiocatore'")
        val partitaCorrente = partita ?: run {
            Log.e(TAG, "Errore: la partita non è stata inizializzata.")
            return null
        }

        if (partitaCorrente.getVincitore() != null) {
            Log.w(TAG, "La partita è già terminata. Impossibile muovere.")
            return null
        }

        // --- INIZIO LOGICA MODIFICATA ---

        // 1. Analizziamo la mossa dell'utente per ottenere partenza e arrivo
        val mossaUtente = parseMossa(mossaGiocatore)
        if (mossaUtente == null) {
            Log.w(TAG, "Formato mossa non valido: '$mossaGiocatore'")
            return null
        }

        // 2. Prendiamo TUTTE le mosse valide calcolate dal motore
        val mosseValidePossibili = partitaCorrente.getMosseValide()

        // 3. Cerchiamo tra le mosse valide quella che corrisponde a quella dell'utente
        val mossaCompletaDaEseguire = mosseValidePossibili.find {
            it.partenza == mossaUtente.partenza && it.arrivo == mossaUtente.arrivo
        }

        if (mossaCompletaDaEseguire == null) {
            Log.w(TAG, "Mossa umana non valida: $mossaGiocatore")
            return null
        }

        // 4. Eseguiamo la mossa COMPLETA, che ora contiene anche le info sulla cattura
        Log.d(TAG, "Stato scacchiera PRIMA della mossa umana:\n${partitaCorrente.getStatoScacchiera()}")
        val mossaUmanaRiuscita = partitaCorrente.eseguiMossaUmano(mossaCompletaDaEseguire)
        if (!mossaUmanaRiuscita) {
            // Questo non dovrebbe accadere se la logica è corretta, ma è una sicurezza in più
            Log.e(TAG, "Errore imprevisto durante l'esecuzione della mossa: $mossaCompletaDaEseguire")
            return null
        }
        // --- FINE LOGICA MODIFICATA ---

        Log.d(TAG, "Mossa umana ESEGUITA. Stato scacchiera DOPO:\n${partitaCorrente.getStatoScacchiera()}")


        if (partitaCorrente.getVincitore() != null) {
            Log.i(TAG, "L'umano ha vinto!")
            return null
        }

        Log.d(TAG, "Tocca all'IA. Faccio giocare l'IA...")
        val mossaIA = partitaCorrente.faiMossaIA()
        Log.d(TAG, "Mossa IA eseguita. Stato scacchiera FINALE:\n${partitaCorrente.getStatoScacchiera()}")

        return mossaIA?.toString()
    }

    override fun getVincitore(): Colore? {
        val vincitore = partita?.getVincitore()
        Log.d(TAG, "getVincitore chiamato. Vincitore: $vincitore")
        return vincitore
    }

    override fun getStatoScacchiera(): String {
        val stato = partita?.getStatoScacchiera() ?: "Partita non iniziata."
        Log.d(TAG, "getStatoScacchiera chiamato.")
        return stato
    }

    override fun getMosseValide(): List<String> {
        val mosse = partita?.getMosseValide()?.map { it.toString() } ?: emptyList()
        Log.d(TAG, "getMosseValide chiamato. Trovate ${mosse.size} mosse valide: $mosse")
        Log.d(TAG, "Stato scacchiera attuale per cui sono state calcolate le mosse:\n${partita?.getStatoScacchiera()}")
        return mosse
    }

    // Questa funzione ora serve solo a estrarre le coordinate di partenza e arrivo
    private fun parseMossa(notazione: String): Mossa? {
        val parti = notazione.trim().uppercase().split(" ")
        if (parti.size != 2) return null

        val partenza = fromNotazioneAlgebrica(parti[0])
        val arrivo = fromNotazioneAlgebrica(parti[1])

        return if (partenza != null && arrivo != null) {
            // Creiamo una mossa "temporanea" senza info sulla cattura
            Mossa(partenza, arrivo)
        } else {
            null
        }
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