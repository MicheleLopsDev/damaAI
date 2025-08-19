package io.github.luposolitario.damaai.game_logic

/**
 * Rappresenta l'intera scacchiera 8x8 e la posizione dei pezzi.
 */
class Scacchiera {

    var zobristHash: Long = 0L

    // Usiamo una Mappa per memorizzare i pezzi. È efficiente perché
    // associa una Posizione a un Pezzo, solo per le caselle occupate.
    private val mappaPezzi: MutableMap<Posizione, Pezzo> = mutableMapOf()

    // Il blocco 'init' viene eseguito appena creiamo un oggetto Scacchiera.
    // È perfetto per preparare il gioco.
    init {
        impostaScacchieraIniziale()
    }

    /**
     * Svuota la scacchiera e imposta una serie di pezzi.
     * Metodo di supporto creato appositamente per i test.
     * @param pezzi La mappa delle posizioni e dei pezzi da impostare.
     */
    fun impostaScacchieraPerTest(pezzi: Map<Posizione, Pezzo>) {
        mappaPezzi.clear()
        mappaPezzi.putAll(pezzi)
    }

    /**
     * Crea una copia esatta di questa scacchiera.
     * Fondamentale per l'IA per simulare mosse senza alterare lo stato reale del gioco.
     */
    fun copia(): Scacchiera {
        val nuovaScacchiera = Scacchiera()
        // Copiamo la mappa dei pezzi pezzo per pezzo.
        nuovaScacchiera.mappaPezzi.clear()
        nuovaScacchiera.mappaPezzi.putAll(this.mappaPezzi)
        return nuovaScacchiera
    }

    /**
     * Restituisce il pezzo presente in una data posizione.
     * Se la casella è vuota, restituisce null.
     */
    fun pezzoA(posizione: Posizione): Pezzo? {
        return mappaPezzi[posizione]
    }

    /**
     * Esegue una mossa sulla scacchiera. Sposta un pezzo e gestisce la promozione.
     * NOTA: Questo metodo non controlla se la mossa è valida! Presume che la
     * validità sia già stata controllata da un'altra parte del codice (il Game Engine).
     */
    fun eseguiMossa(mossa: Mossa) {
        val pezzo = pezzoA(mossa.partenza) ?: return // Se non c'è un pezzo, non fare nulla

        // 1. Rimuovi il pezzo dalla posizione di partenza
        mappaPezzi.remove(mossa.partenza)

        // 2. Controlla se una pedina è arrivata alla fine e deve diventare Damone
        val pezzoMosso = if (deveEsserePromosso(pezzo, mossa.arrivo)) {
            Pezzo(pezzo.colore, TipoPezzo.DAMONE)
        } else {
            pezzo
        }

        // 3. Aggiungi il pezzo (originale o promosso) alla posizione di arrivo
        mappaPezzi[mossa.arrivo] = pezzoMosso
    }

    /**
     * Rimuove un pezzo da una data posizione (usato per le catture).
     */
    fun rimuoviPezzoA(posizione: Posizione) {
        mappaPezzi.remove(posizione)
    }

    /**
     * Popola la scacchiera con i pezzi nella loro configurazione iniziale.
     */
    private fun impostaScacchieraIniziale() {
        mappaPezzi.clear() // Pulisci la scacchiera prima di impostarla

        // Posiziona i pezzi NERI nelle prime 3 righe (0, 1, 2)
        for (riga in 0..2) {
            for (colonna in 0..7) {
                // Nella dama si gioca solo sulle caselle scure
                if ((riga + colonna) % 2 != 0) {
                    mappaPezzi[Posizione(riga, colonna)] = Pezzo(Colore.NERO)
                }
            }
        }

        // Posiziona i pezzi BIANCHI nelle ultime 3 righe (5, 6, 7)
        for (riga in 5..7) {
            for (colonna in 0..7) {
                if ((riga + colonna) % 2 != 0) {
                    mappaPezzi[Posizione(riga, colonna)] = Pezzo(Colore.BIANCO)
                }
            }
        }
    }

    /**
     * Controlla se una pedina raggiunge il lato opposto per la promozione.
     */
    private fun deveEsserePromosso(pezzo: Pezzo, arrivo: Posizione): Boolean {
        if (pezzo.tipo == TipoPezzo.DAMONE) return false // Un damone non può essere promosso

        return (pezzo.colore == Colore.BIANCO && arrivo.riga == 0) ||
                (pezzo.colore == Colore.NERO && arrivo.riga == 7)
    }

    /**
     * Crea una rappresentazione testuale della scacchiera.
     * Utile per vedere lo stato del gioco durante lo sviluppo.
     */
    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("  A B C D E F G H\n")
        for (riga in 0..7) {
            builder.append("${riga + 1} ")
            for (colonna in 0..7) {
                val pezzo = pezzoA(Posizione(riga, colonna))
                val simbolo = when (pezzo?.colore) {
                    Colore.BIANCO -> if (pezzo.tipo == TipoPezzo.PEDINA) "b" else "B"
                    Colore.NERO -> if (pezzo.tipo == TipoPezzo.PEDINA) "n" else "N"
                    null -> if ((riga + colonna) % 2 != 0) "." else " "
                }
                builder.append("$simbolo ")
            }
            builder.append("\n")
        }
        return builder.toString()
    }

    fun hasPezzi(colore: Colore): Boolean {
        return mappaPezzi.values.any { it.colore == colore }
    }
}