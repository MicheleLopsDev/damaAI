package io.github.luposolitario.damaai.game_logic

class PartitaDiDama(difficoltaIA: Difficolta) {

    private val motore = MotoreDiGioco()
    private val giocatoreIA = GiocatoreIA(difficoltaIA, motore)

    fun getPezziConCatturaObbligatoria(): List<Posizione> {
        return motore.getPezziConCatturaObbligatoria()
    }

    fun getTurno(): Colore {
        return motore.turnoCorrente
    }

    fun getStatoScacchiera(): String {
        return motore.scacchiera.toString()
    }

    fun getMosseValide(): List<Mossa> {
        return motore.mosseValideDisponibili()
    }

    /**
     * Permette a un giocatore umano di tentare di eseguire una mossa.
     * @param mossa La mossa che il giocatore vuole eseguire.
     * @return true se la mossa era valida ed è stata eseguita, false altrimenti.
     */
    fun eseguiMossaUmano(mossa: Mossa): MossaResult {
        // Prima, verifica se è il turno del giocatore umano. In questo caso è il Bianco.
        if (motore.turnoCorrente == Colore.NERO) {
            return MossaResult(isSuccess = false, isTurnoTerminato = false)
        }

        // Esegui la mossa e ottieni il risultato dettagliato
        val result = motore.eseguiMossa(mossa)

        // Se la mossa ha avuto successo ma il turno non è terminato (cattura multipla),
        // dobbiamo trovare le prossime mosse di cattura valide per il pezzo che ha appena mosso.
        if (result.isSuccess && !result.isTurnoTerminato) {
            // La logica di trovaMosseValideDisponibili() si occuperà di trovare solo le catture
            // per il pezzo corretto, dato che il turno non è cambiato.
        }

        // Restituisci il risultato completo per permettere alla UI di gestire
        // correttamente il cambio turno o le mosse successive.
        return result
    }

    fun faiMossaIA(): Mossa? {
        if (motore.turnoCorrente == Colore.BIANCO) return null

        var isTurnoCompletato = false
        var mossaIniziale: Mossa? = null
        var mossaCorrente: Mossa? = null

        // Loop per gestire mosse multiple (catture multiple)
        do {
            // Trova la mossa ottimale per il turno corrente
            // In un ciclo di catture multiple, la mossa sarà sempre una cattura obbligatoria
            val mossaScelta = giocatoreIA.scegliMossa()
            if (mossaScelta == null) {
                isTurnoCompletato = true
                break
            }

            if (mossaIniziale == null) {
                mossaIniziale = mossaScelta // Memorizza la prima mossa della sequenza
            }

            val result = motore.eseguiMossa(mossaScelta)
            if (result.isSuccess) {
                isTurnoCompletato = result.isTurnoTerminato
                mossaCorrente = result.mossaEseguita
            } else {
                // Mossa non valida, esci dal ciclo per sicurezza
                isTurnoCompletato = true
            }

        } while (!isTurnoCompletato)

        return mossaIniziale
    }

    fun getVincitore(): Colore? {
        if (getMosseValide().isEmpty()) {
            return if (motore.turnoCorrente == Colore.BIANCO) Colore.NERO else Colore.BIANCO
        }
        return null
    }
}