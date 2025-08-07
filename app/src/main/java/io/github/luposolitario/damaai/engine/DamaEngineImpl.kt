package io.github.luposolitario.damaai.engine

import io.github.luposolitario.damaai.game_logic.*

class DamaEngineImpl : DamaEngine {

    // La nostra classe usa internamente la PartitaDiDama che abbiamo già creato.
    // Viene inizializzata come 'null' e creata quando si chiama nuovaPartita.
    private var partita: PartitaDiDama? = null

    override fun nuovaPartita(difficolta: Difficolta) {
        partita = PartitaDiDama(difficolta)
    }

    override fun muoviPezzo(mossaGiocatore: String): String? {
        val partitaCorrente = partita ?: return null // Se la partita non è iniziata, non fare nulla.

        // 1. Controlla se c'è già un vincitore
        if (partitaCorrente.getVincitore() != null) return null

        // 2. Prova a "capire" la mossa dalla stringa
        val mossa = parseMossa(mossaGiocatore) ?: return null // Formato mossa non valido

        // 3. Esegui la mossa umana
        val mossaUmanaRiuscita = partitaCorrente.eseguiMossaUmano(mossa)
        if (!mossaUmanaRiuscita) {
            return null // Mossa non valida secondo le regole
        }

        // 4. Controlla se l'umano ha vinto
        if (partitaCorrente.getVincitore() != null) return null

        // 5. Fai giocare l'IA
        val mossaIA = partitaCorrente.faiMossaIA()

        // 6. Controlla se l'IA ha vinto e restituisci la sua mossa come stringa
        return mossaIA?.toString()
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

    /**
     * Funzione privata per convertire una stringa come "C7 B6" in un oggetto Mossa.
     */
    private fun parseMossa(notazione: String): Mossa? {
        val parti = notazione.trim().uppercase().split(" ")
        if (parti.size != 2) return null // Il formato deve essere "PARTENZA ARRIVO"

        val partenza = fromNotazioneAlgebrica(parti[0])
        val arrivo = fromNotazioneAlgebrica(parti[1])

        return if (partenza != null && arrivo != null) {
            // Per ora non conosciamo il pezzo catturato, il MotoreDiGioco lo troverà
            // confrontando questa mossa con la lista di quelle valide.
            Mossa(partenza, arrivo)
        } else {
            null
        }
    }

    /**
     * Funzione privata per convertire una notazione come "A1" in un oggetto Posizione.
     */
    private fun fromNotazioneAlgebrica(notazione: String): Posizione? {
        if (notazione.length != 2) return null
        val colonnaChar = notazione[0]
        val rigaChar = notazione[1]

        if (colonnaChar !in 'A'..'H' || rigaChar !in '1'..'8') return null

        val colonna = colonnaChar - 'A'
        val riga = rigaChar - '1'
        return Posizione(riga, colonna)
    }
}