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
    fun eseguiMossaUmano(mossa: Mossa): Boolean {
        // **FIX:** Rimossa la logica che bloccava il giocatore Nero.
        // Ora il controllo del turno è gestito correttamente dalla UI.
        return motore.eseguiMossa(mossa)
    }

    fun faiMossaIA(): Mossa? {
        if (motore.turnoCorrente == Colore.BIANCO) return null

        val mossaScelta = giocatoreIA.scegliMossa()
        mossaScelta?.let {
            motore.eseguiMossa(it)
        }
        return mossaScelta
    }

    fun getVincitore(): Colore? {
        if (getMosseValide().isEmpty()) {
            return if (motore.turnoCorrente == Colore.BIANCO) Colore.NERO else Colore.BIANCO
        }
        return null
    }
}