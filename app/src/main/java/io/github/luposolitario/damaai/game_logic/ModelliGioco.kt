package io.github.luposolitario.damaai.game_logic

/**
 * Rappresenta il colore di un giocatore o di un pezzo.
 * Usiamo un 'enum' perché i valori possibili sono solo due e ben definiti.
 */
enum class Colore {
    BIANCO, // Il giocatore che solitamente inizia
    NERO
}

/**
 * Rappresenta il tipo di pezzo.
 * Una pedina può essere promossa a Damone.
 */
enum class TipoPezzo {
    PEDINA,
    DAMONE
}

/**
 * Rappresenta un pezzo sulla scacchiera.
 * È una 'data class' perché il suo unico scopo è contenere dati: colore e tipo.
 */
data class Pezzo(val colore: Colore, val tipo: TipoPezzo = TipoPezzo.PEDINA)

/**
 * Rappresenta una mossa, da una posizione di partenza a una di arrivo.
 */
// File: ModelliGioco.kt

// ... (tutto il resto del file rimane uguale)

// Aggiungi questo codice sotto le classi che abbiamo scritto prima,
// sempre nel file ModelliGioco.kt

class MotoreDiGioco() {

    var scacchiera: Scacchiera = Scacchiera()
        internal set // Cambiamo in 'internal' per permettere la modifica nel test

    var turnoCorrente: Colore = Colore.BIANCO
        internal set // Anche qui

    fun mosseValideDisponibili(): List<Mossa> {
        val mosseDiCattura = trovaTutteLeMosseDiCattura(turnoCorrente)
        if (mosseDiCattura.isNotEmpty()) {
            return mosseDiCattura
        }
        return trovaTutteLeMosseSemplici(turnoCorrente)
    }

    fun eseguiMossa(mossa: Mossa): Boolean {
        val mosseValide = mosseValideDisponibili()
        if (mossa !in mosseValide) {
            return false
        }

        // Grazie alla modifica su Mossa, ora la rimozione è semplicissima!
        mossa.posizionePezzoCatturato?.let {
            scacchiera.rimuoviPezzoA(it)
        }

        scacchiera.eseguiMossa(mossa)
        cambiaTurno()
        return true
    }

    // Aggiungi questa funzione dentro la classe MotoreDiGioco

    fun getPezziConCatturaObbligatoria(): List<Posizione> {
        val mosseDiCattura = trovaTutteLeMosseDiCattura(turnoCorrente)
        if (mosseDiCattura.isNotEmpty()) {
            // Usiamo distinct() per evitare duplicati se un pezzo ha più catture
            return mosseDiCattura.map { it.partenza }.distinct()
        }
        return emptyList()
    }

    private fun cambiaTurno() {
        turnoCorrente = if (turnoCorrente == Colore.BIANCO) Colore.NERO else Colore.BIANCO
    }

    // --- METODI DI RICERCA MOSSE AGGIORNATI ---

    private fun trovaTutteLeMosseSemplici(colore: Colore): List<Mossa> {
        val mosse = mutableListOf<Mossa>()
        forEachPezzoDelGiocatore(colore) { pos, pezzo ->
            val mosseTrovate = if (pezzo.tipo == TipoPezzo.PEDINA) {
                trovaMosseSempliciPerPedina(pos, pezzo)
            } else {
                trovaMosseSempliciPerDamone(pos)
            }
            mosse.addAll(mosseTrovate)
        }
        return mosse
    }

    private fun trovaTutteLeMosseDiCattura(colore: Colore): List<Mossa> {
        val mosse = mutableListOf<Mossa>()
        forEachPezzoDelGiocatore(colore) { pos, pezzo ->
            val mosseTrovate = if (pezzo.tipo == TipoPezzo.PEDINA) {
                trovaMosseDiCatturaPerPedina(pos, pezzo)
            } else {
                trovaMosseDiCatturaPerDamone(pos, pezzo)
            }
            mosse.addAll(mosseTrovate)
        }
        return mosse
    }

    // --- LOGICA PEDINA (INVARIATA) ---
    private fun trovaMosseSempliciPerPedina(pos: Posizione, pezzo: Pezzo): List<Mossa> {
        val mosse = mutableListOf<Mossa>()
        val direzione = if (pezzo.colore == Colore.BIANCO) -1 else 1

        val posArrivoSinistra = Posizione(pos.riga + direzione, pos.colonna - 1)
        if (isCasellaVuota(posArrivoSinistra)) mosse.add(Mossa(pos, posArrivoSinistra))

        val posArrivoDestra = Posizione(pos.riga + direzione, pos.colonna + 1)
        if (isCasellaVuota(posArrivoDestra)) mosse.add(Mossa(pos, posArrivoDestra))

        return mosse
    }

    private fun trovaMosseDiCatturaPerPedina(pos: Posizione, pezzo: Pezzo): List<Mossa> {
        val mosse = mutableListOf<Mossa>()
        val direzione = if (pezzo.colore == Colore.BIANCO) -1 else 1

        for (dirColonna in listOf(-1, 1)) {
            val posAvversario = Posizione(pos.riga + direzione, pos.colonna + dirColonna)
            val posArrivo = Posizione(pos.riga + direzione * 2, pos.colonna + dirColonna * 2)

            if (isPezzoAvversario(posAvversario, pezzo.colore) && isCasellaVuota(posArrivo)) {
                mosse.add(Mossa(pos, posArrivo, posAvversario)) // Ora salviamo il pezzo catturato
            }
        }
        return mosse
    }

    // --- NUOVA LOGICA PER IL DAMONE ---

    private fun trovaMosseSempliciPerDamone(pos: Posizione): List<Mossa> {
        val mosse = mutableListOf<Mossa>()
        // Controlla le 4 direzioni diagonali (su-dx, su-sx, giu-dx, giu-sx)
        for (dirRiga in listOf(-1, 1)) {
            for (dirColonna in listOf(-1, 1)) {
                var ampiezza = 1
                while (true) {
                    val posArrivo = Posizione(pos.riga + dirRiga * ampiezza, pos.colonna + dirColonna * ampiezza)
                    if (isCasellaVuota(posArrivo)) {
                        mosse.add(Mossa(pos, posArrivo))
                        ampiezza++
                    } else {
                        break // La diagonale è bloccata da un pezzo o dal bordo
                    }
                }
            }
        }
        return mosse
    }

    private fun trovaMosseDiCatturaPerDamone(pos: Posizione, pezzo: Pezzo): List<Mossa> {
        val mosse = mutableListOf<Mossa>()
        // Controlla le 4 direzioni diagonali
        for (dirRiga in listOf(-1, 1)) {
            for (dirColonna in listOf(-1, 1)) {
                var posPezzoDaCatturare: Posizione? = null
                var ampiezza = 1
                while (true) {
                    val posCorrente = Posizione(pos.riga + dirRiga * ampiezza, pos.colonna + dirColonna * ampiezza)
                    if (!isCasellaValida(posCorrente)) break // Fuori dalla scacchiera

                    if (posPezzoDaCatturare == null) { // Non abbiamo ancora trovato un pezzo da mangiare
                        if (isPezzoAvversario(posCorrente, pezzo.colore)) {
                            posPezzoDaCatturare = posCorrente // Trovato!
                        } else if (scacchiera.pezzoA(posCorrente) != null) {
                            break // Trovato un pezzo nostro, blocca la diagonale
                        }
                    } else { // Abbiamo già trovato un avversario, cerchiamo caselle vuote per atterrare
                        if (isCasellaVuota(posCorrente)) {
                            mosse.add(Mossa(pos, posCorrente, posPezzoDaCatturare))
                        } else {
                            break // La casella di atterraggio è occupata, stop
                        }
                    }
                    ampiezza++
                }
            }
        }
        return mosse
    }

    // --- FUNZIONI DI SUPPORTO ---

    private fun forEachPezzoDelGiocatore(colore: Colore, action: (Posizione, Pezzo) -> Unit) {
        for (riga in 0..7) {
            for (colonna in 0..7) {
                val pos = Posizione(riga, colonna)
                val pezzo = scacchiera.pezzoA(pos)
                if (pezzo != null && pezzo.colore == colore) {
                    action(pos, pezzo)
                }
            }
        }
    }

    private fun isCasellaValida(pos: Posizione): Boolean = pos.riga in 0..7 && pos.colonna in 0..7
    private fun isCasellaVuota(pos: Posizione): Boolean = isCasellaValida(pos) && scacchiera.pezzoA(pos) == null
    private fun isPezzoAvversario(pos: Posizione, coloreProprio: Colore): Boolean {
        if (!isCasellaValida(pos)) return false
        val pezzo = scacchiera.pezzoA(pos)
        return pezzo != null && pezzo.colore != coloreProprio
    }
}