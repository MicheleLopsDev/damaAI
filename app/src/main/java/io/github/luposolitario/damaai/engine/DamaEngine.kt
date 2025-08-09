package io.github.luposolitario.damaai.engine

import io.github.luposolitario.damaai.data.Piece
import io.github.luposolitario.damaai.game_logic.Colore
import io.github.luposolitario.damaai.game_logic.Difficolta
import io.github.luposolitario.damaai.game_logic.Mossa
import io.github.luposolitario.damaai.game_logic.Posizione

/**
 * L'interfaccia pubblica principale per interagire con il motore di gioco della Dama.
 */
interface DamaEngine {

    // Aggiungi questa funzione all'interfaccia DamaEngine

    /**
     * Restituisce una lista delle posizioni dei pezzi che hanno una cattura obbligatoria.
     * La lista è vuota se non ci sono catture obbligatorie.
     */
    fun getPezziConCatturaObbligatoria(): List<Posizione>

    /**
     * Inizia una nuova partita, resettando la scacchiera e impostando la difficoltà dell'IA.
     * @param difficolta Il livello di abilità dell'avversario IA.
     */
    fun nuovaPartita(difficolta: Difficolta)

    /**
     * Esegue SOLO la mossa del giocatore.
     * Restituisce la mossa eseguita se valida, altrimenti null.
     */
    fun muoviPezzoUmano(mossaGiocatore: String): Mossa?


    /**
     * Trova la pedina catturata confrontando lo stato attuale con quello precedente.
     */
    fun trovaPedinaCatturata(mossa: Mossa): Piece?

    /**
     * Esegue SOLO la mossa dell'IA.
     * Restituisce la mossa eseguita, o null se non può muovere.
     */
    fun faiMossaIA(): Mossa?

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

    /**
     * Restituisce il colore del giocatore di turno.
     */
    fun getTurnoCorrente(): Colore
}