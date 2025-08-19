// michelelopsdev/damaai/damaAI-4607344960c2303f34c37dc7e118d6e6fbf7c21e/app/src/main/java/io/github/luposolitario/damaai/game_logic/GiocatoreIA.kt
package io.github.luposolitario.damaai.game_logic

import android.util.Log
import kotlin.math.max
import kotlin.math.min

// Livelli di difficoltà aggiornati con il nuovo livello MAESTRO
enum class Difficolta(val profonditaMinimax: Int) {
    PRINCIPIANTE(0),
    NOVIZIO(2),
    INTERMEDIO(4),
    AVANZATO(6),
    ESPERTO(8),
    MAESTRO(10) // Profondità massima ridotta a un valore più gestibile
}

class GiocatoreIA(
    private val difficolta: Difficolta,
    private val motore: MotoreDiGioco
) {

    private val TAG = "GiocatoreIA_Debug"
    private val transpositionTable = TranspositionTable()

    // --- NUOVE MAPPE POSIZIONALI ---
    // Bonus per le pedine in base alla loro posizione. Più alto è il valore, migliore è la casella.
    // I valori sono pensati per il Bianco (che avanza verso la riga 0). Verranno invertiti per il Nero.
    private val mappaPunteggioPedina = arrayOf(
        intArrayOf(100, 100, 100, 100, 100, 100, 100, 100), // Riga di promozione
        intArrayOf( 40,  45,  50,  55,  55,  50,  45,  40),
        intArrayOf( 30,  35,  40,  45,  45,  40,  35,  30),
        intArrayOf( 20,  25,  30,  35,  35,  30,  25,  20), // Centro
        intArrayOf( 15,  20,  25,  30,  30,  25,  20,  15), // Centro
        intArrayOf( 10,  15,  20,  20,  20,  20,  15,  10),
        intArrayOf(  5,  10,  10,  10,  10,  10,  10,   5),
        intArrayOf(  0,   0,   0,   0,   0,   0,   0,   0)  // Base
    )

    // Per i damoni, il centro è ancora più importante.
    private val mappaPunteggioDamone = arrayOf(
        intArrayOf(60, 65, 70, 75, 75, 70, 65, 60),
        intArrayOf(65, 70, 75, 80, 80, 75, 70, 65),
        intArrayOf(70, 75, 80, 85, 85, 80, 75, 70), // Cuore della scacchiera
        intArrayOf(75, 80, 85, 90, 90, 85, 80, 75), // Cuore della scacchiera
        intArrayOf(75, 80, 85, 90, 90, 85, 80, 75), // Cuore della scacchiera
        intArrayOf(70, 75, 80, 85, 85, 80, 75, 70), // Cuore della scacchiera
        intArrayOf(65, 70, 75, 80, 80, 75, 70, 65),
        intArrayOf(60, 65, 70, 75, 75, 70, 65, 60)
    )

    // Versione corretta del metodo scegliMossa in GiocatoreIA.kt

    fun scegliMossa(): Mossa? {
        // 1. Creiamo UNA SOLA copia della scacchiera da usare per tutte le simulazioni
        val scacchieraDiSimulazione = motore.scacchiera.copia()

        val mosseDisponibili = motore.mosseValideDisponibili()
        if (mosseDisponibili.isEmpty()) return null

        // Logica del libro di aperture e difficoltà principiante...
        val numeroPezzi = motore.scacchiera.contaPezzi()
        if (numeroPezzi > 20 && !mosseDisponibili.any { it.isCattura() }) {
            val bookMoveString = OpeningBook.getMove(motore.scacchiera.zobristHash)
            if (bookMoveString != null) {
                Log.d(TAG, "MOSSA DAL LIBRO: $bookMoveString")
                return parseMossaFromString(bookMoveString)
            }
        }
        if (difficolta == Difficolta.PRINCIPIANTE) {
            return mosseDisponibili.random()
        }

        transpositionTable.clear()
        Log.d(TAG, "===== Inizio Turno IA / Difficoltà: $difficolta =====")

        val coloreIA = motore.turnoCorrente
        var migliorMossaTrovata: Mossa? = mosseDisponibili.firstOrNull()
        var mosseOrdinate = mosseDisponibili

        // Ciclo di Approfondimento Iterativo
        for (depth in 1..difficolta.profonditaMinimax) {
            var migliorPunteggioInQuestoCiclo = Int.MIN_VALUE
            val mosseValutateInQuestoCiclo = mutableListOf<Pair<Mossa, Int>>()

            // 2. Usiamo il nuovo sistema anche qui
            for (mossa in mosseOrdinate) {
                // --- INIZIO MODIFICA ---
                // NIENTE PIÙ scacchieraFiglio = simulaMossa(...)

                // Applichiamo la mossa alla nostra scacchiera di simulazione
                scacchieraDiSimulazione.eseguiMossa(mossa)

                // Chiamiamo minimax sulla scacchiera modificata
                val punteggio = minimax(scacchieraDiSimulazione, depth - 1, Int.MIN_VALUE, Int.MAX_VALUE, false, coloreIA)

                // Annulliamo la mossa per preparare la scacchiera al ciclo successivo
                scacchieraDiSimulazione.annullaMossa(mossa)
                // --- FINE MODIFICA ---

                mosseValutateInQuestoCiclo.add(Pair(mossa, punteggio))

                if (punteggio > migliorPunteggioInQuestoCiclo) {
                    migliorPunteggioInQuestoCiclo = punteggio
                    migliorMossaTrovata = mossa
                }
            }
            mosseOrdinate = mosseValutateInQuestoCiclo.sortedByDescending { it.second }.map { it.first }
            Log.d(TAG, "Miglior mossa a profondità $depth: $migliorMossaTrovata (Punteggio: $migliorPunteggioInQuestoCiclo)")
        }

        Log.d(TAG, "===== Fine Turno IA / Mossa finale scelta: $migliorMossaTrovata =====")
        return migliorMossaTrovata ?: mosseDisponibili.randomOrNull()
    }

    // ... Le funzioni parseMossaFromString, minimax, quiescenceSearch, simulaMossa e opposto rimangono INVARIATE ...
    private fun parseMossaFromString(moveString: String): Mossa? {
        val parts = moveString.split(" ")
        if (parts.size != 2) return null
        val from = Posizione.fromAlgebraic(parts[0])
        val to = Posizione.fromAlgebraic(parts[1])
        return if (from != null && to != null) Mossa(from, to) else null
    }

    /**
     * FUNZIONE MINIMAX COMPLETA E OTTIMIZZATA
     * Utilizza esegui/annulla mossa per la massima performance.
     */
    private fun minimax(
        scacchiera: Scacchiera,
        profondita: Int,
        alpha: Int,
        beta: Int,
        isMaximizing: Boolean,
        coloreIA: Colore
    ): Int {
        val hash = scacchiera.zobristHash
        val cachedScore = transpositionTable.probe(hash, profondita)
        if (cachedScore != null) {
            return cachedScore
        }

        if (profondita == 0) {
            return quiescenceSearch(scacchiera, alpha, beta, isMaximizing, coloreIA)
        }

        val motoreSimulato = MotoreDiGioco().apply {
            this.scacchiera = scacchiera
            this.turnoCorrente = if (isMaximizing) coloreIA else coloreIA.opposto()
        }

        // Cambia il turno per il calcolo dell'hash
        scacchiera.zobristHash = scacchiera.zobristHash xor ZobristHashing.blackTurnHash

        val mossePossibili = motoreSimulato.mosseValideDisponibili()

        // Ripristina il turno nell'hash
        scacchiera.zobristHash = scacchiera.zobristHash xor ZobristHashing.blackTurnHash

        if (mossePossibili.isEmpty()) {
            return if (scacchiera.haVinto(coloreIA)) 100000 else -100000
        }

        var currentAlpha = alpha
        var currentBeta = beta
        var finalScore: Int

        if (isMaximizing) {
            var migliorPunteggio = Int.MIN_VALUE
            for (mossa in mossePossibili) {
                scacchiera.eseguiMossa(mossa)
                val punteggio = minimax(scacchiera, profondita - 1, currentAlpha, currentBeta, false, coloreIA)
                scacchiera.annullaMossa(mossa)

                migliorPunteggio = max(migliorPunteggio, punteggio)
                currentAlpha = max(currentAlpha, migliorPunteggio)
                if (currentBeta <= currentAlpha) break
            }
            finalScore = migliorPunteggio
        } else { // Minimizing
            var peggiorPunteggio = Int.MAX_VALUE
            for (mossa in mossePossibili) {
                scacchiera.eseguiMossa(mossa)
                val punteggio = minimax(scacchiera, profondita - 1, currentAlpha, currentBeta, true, coloreIA)
                scacchiera.annullaMossa(mossa)

                peggiorPunteggio = min(peggiorPunteggio, punteggio)
                currentBeta = min(currentBeta, peggiorPunteggio)
                if (currentBeta <= currentAlpha) break
            }
            finalScore = peggiorPunteggio
        }

        transpositionTable.store(hash, finalScore, profondita)
        return finalScore
    }

    /**
     * FUNZIONE QUIESCENCESEARCH COMPLETA E OTTIMIZZATA
     * Estende la ricerca per le sole mosse di cattura.
     */
    private fun quiescenceSearch(
        scacchiera: Scacchiera,
        alpha: Int,
        beta: Int,
        isMaximizing: Boolean,
        coloreIA: Colore
    ): Int {
        var currentAlpha = alpha
        var currentBeta = beta

        val standingPat = calcolaPunteggio(scacchiera, coloreIA)

        if (isMaximizing) {
            currentAlpha = max(currentAlpha, standingPat)
        } else {
            currentBeta = min(currentBeta, standingPat)
        }
        if (currentBeta <= currentAlpha) {
            return if (isMaximizing) currentAlpha else currentBeta
        }

        val motoreSimulato = MotoreDiGioco().apply {
            this.scacchiera = scacchiera
            this.turnoCorrente = if (isMaximizing) coloreIA else coloreIA.opposto()
        }

        scacchiera.zobristHash = scacchiera.zobristHash xor ZobristHashing.blackTurnHash
        val mosseCattura = motoreSimulato.mosseValideDisponibili().filter { it.isCattura() }
        scacchiera.zobristHash = scacchiera.zobristHash xor ZobristHashing.blackTurnHash

        if (mosseCattura.isEmpty()) {
            return standingPat
        }

        if (isMaximizing) {
            for (mossa in mosseCattura) {
                scacchiera.eseguiMossa(mossa)
                val punteggio = quiescenceSearch(scacchiera, currentAlpha, currentBeta, false, coloreIA)
                scacchiera.annullaMossa(mossa)
                currentAlpha = max(currentAlpha, punteggio)
                if (currentBeta <= currentAlpha) break
            }
            return currentAlpha
        } else {
            for (mossa in mosseCattura) {
                scacchiera.eseguiMossa(mossa)
                val punteggio = quiescenceSearch(scacchiera, currentAlpha, currentBeta, true, coloreIA)
                scacchiera.annullaMossa(mossa)
                currentBeta = min(currentBeta, punteggio)
                if (currentBeta <= currentAlpha) break
            }
            return currentBeta
        }
    }


    private fun Colore.opposto() = if (this == Colore.BIANCO) Colore.NERO else Colore.BIANCO

    /**
     * FUNZIONE DI VALUTAZIONE "GRANDMASTER" - VERSIONE 2.0
     * Calcola il punteggio totale per l'IA, considerando la differenza con l'avversario.
     */
    private fun calcolaPunteggio(scacchiera: Scacchiera, coloreIA: Colore): Int {
        val coloreAvversario = coloreIA.opposto()

        if (!scacchiera.hasPezzi(coloreAvversario)) return 100000
        if (!scacchiera.hasPezzi(coloreIA)) return -100000

        val punteggioIA = calcolaPunteggioPerColore(scacchiera, coloreIA)
        val punteggioAvversario = calcolaPunteggioPerColore(scacchiera, coloreAvversario)

        val bonusTurno = if (motore.turnoCorrente == coloreIA) 10 else 0

        return (punteggioIA - punteggioAvversario) + bonusTurno
    }

    /**
     * VERSIONE RIVISTA E POTENZIATA
     * Calcola il punteggio per un singolo colore, includendo materiale, posizione e struttura.
     */
    private fun calcolaPunteggioPerColore(scacchiera: Scacchiera, colore: Colore): Int {
        // Pesi base
        val valorePedina = 200
        val valoreDamone = 500

        var punteggioMateriale = 0
        var punteggioPosizionale = 0
        var punteggioStrutturale = 0

        for (riga in 0..7) {
            for (colonna in 0..7) {
                val pezzo = scacchiera.pezzoA(Posizione(riga, colonna))
                if (pezzo != null && pezzo.colore == colore) {

                    val rigaPerMappa = if (colore == Colore.BIANCO) riga else 7 - riga

                    if (pezzo.tipo == TipoPezzo.DAMONE) {
                        punteggioMateriale += valoreDamone
                        punteggioPosizionale += mappaPunteggioDamone[rigaPerMappa][colonna]
                    } else { // PEDINA
                        punteggioMateriale += valorePedina
                        punteggioPosizionale += mappaPunteggioPedina[rigaPerMappa][colonna]

                        // --- Analisi Strutturale ---

                        // 1. Pedina arretrata (sulla base): bonus difensivo
                        if ((colore == Colore.BIANCO && riga == 7) || (colore == Colore.NERO && riga == 0)) {
                            punteggioStrutturale += 15 // Bonus per la "base line"
                        }

                        // 2. Pedina isolata: penalità
                        val haSupportoSinistro = scacchiera.pezzoA(Posizione(riga, colonna - 1))?.colore == colore
                        val haSupportoDestro = scacchiera.pezzoA(Posizione(riga, colonna + 1))?.colore == colore
                        if (!haSupportoSinistro && !haSupportoDestro) {
                            punteggioStrutturale -= 10 // Penalità per pedina isolata
                        }

                        // 3. Coppia di pedine: bonus
                        if (haSupportoDestro) { // Contiamo solo a destra per non contare due volte
                            punteggioStrutturale += 8 // Bonus per pedine affiancate
                        }
                    }
                }
            }
        }

        // Bonus di mobilità (numero di mosse disponibili)
        val motoreSimulato = MotoreDiGioco().apply {
            this.scacchiera = scacchiera
            this.turnoCorrente = colore
        }
        val bonusMobilita = motoreSimulato.mosseValideDisponibili().size * 2

        return punteggioMateriale + punteggioPosizionale + punteggioStrutturale + bonusMobilita
    }
}