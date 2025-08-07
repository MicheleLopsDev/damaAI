package io.github.luposolitario.damaai.game_logic
// File: PartitaDiDama.kt

/**
 * La classe principale della libreria, che gestisce una partita completa di Dama.
 * Questa è l'interfaccia pubblica che utilizzerai nel tuo progetto.
 */
class PartitaDiDama(difficoltaIA: Difficolta) {

    // La partita contiene al suo interno il motore di gioco.
    private val motore = MotoreDiGioco()

    // E contiene anche il giocatore IA.
    private val giocatoreIA = GiocatoreIA(difficoltaIA, motore)

    /**
     * Restituisce lo stato attuale della scacchiera in formato testuale.
     */
    fun getStatoScacchiera(): String {
        return motore.scacchiera.toString()
    }

    /**
     * Restituisce la lista di tutte le mosse valide per il giocatore corrente.
     * Utile per mostrare a un utente umano le sue opzioni.
     */
    fun getMosseValide(): List<Mossa> {
        return motore.mosseValideDisponibili()
    }

    /**
     * Permette a un giocatore umano di tentare di eseguire una mossa.
     * @param mossa La mossa che il giocatore vuole eseguire.
     * @return true se la mossa era valida ed è stata eseguita, false altrimenti.
     */
    fun eseguiMossaUmano(mossa: Mossa): Boolean {
        // L'IA gioca solo con il NERO. Se è il turno del nero, non permettere mossa umana.
        if (motore.turnoCorrente == Colore.NERO) return false

        return motore.eseguiMossa(mossa)
    }

    /**
     * Fa scegliere e giocare una mossa all'IA.
     * @return La mossa eseguita dall'IA, o null se non ha potuto muovere.
     */
    fun faiMossaIA(): Mossa? {
        if (motore.turnoCorrente == Colore.BIANCO) return null

        val mossaScelta = giocatoreIA.scegliMossa()
        mossaScelta?.let {
            motore.eseguiMossa(it)
        }
        return mossaScelta
    }

    /**
     * Controlla se la partita è finita.
     * La partita finisce se il giocatore di turno non ha mosse valide.
     * @return Il colore del vincitore, o null se la partita è ancora in corso.
     */
    fun getVincitore(): Colore? {
        if (getMosseValide().isEmpty()) {
            // Se non ci sono mosse, il giocatore di turno ha perso. Vince l'altro.
            return if (motore.turnoCorrente == Colore.BIANCO) Colore.NERO else Colore.BIANCO
        }
        return null
    }
}