package io.github.luposolitario.damaai.engine

import io.github.luposolitario.damaai.game_logic.Colore
import io.github.luposolitario.damaai.game_logic.Difficolta

/**
 * L'interfaccia pubblica principale per interagire con il motore di gioco della Dama.
 */
interface DamaEngine {

    /**
     * Inizia una nuova partita, resettando la scacchiera e impostando la difficoltà dell'IA.
     * @param difficolta Il livello di abilità dell'avversario IA.
     */
    fun nuovaPartita(difficolta: Difficolta)

    /**
     * Esegue la mossa del giocatore (BIANCO) e, se la partita non è finita,
     * calcola ed esegue la contromossa dell'IA (NERO).
     *
     * @param mossaGiocatore Una stringa che rappresenta la mossa, es. "C7 B6".
     * @return La mossa dell'IA in formato stringa, o `null` se la mossa del giocatore
     * non è valida o se uno dei due giocatori ha vinto.
     */
    fun muoviPezzo(mossaGiocatore: String): String?

    /**
     * Restituisce il vincitore della partita, se ce n'è uno.
     * @return Il [Colore] del vincitore, o `null` se la partita è ancora in corso.
     */
    fun getVincitore(): Colore?

    /**
     * Metodo di supporto per ottenere una rappresentazione testuale della scacchiera.
     * Utile per visualizzare lo stato del gioco.
     */
    fun getStatoScacchiera(): String

    /**
     * Metodo di supporto per ottenere la lista di tutte le mosse valide per il giocatore
     * umano nel turno corrente.
     * @return Una lista di stringhe, dove ogni stringa è una mossa valida (es. "C7 B6").
     */
    fun getMosseValide(): List<String>
}