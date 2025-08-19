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
    // La logica di scegliMossa e minimax rimane invariata, userà la nuova `calcolaPunteggio`.
    fun scegliMossa(): Mossa? {

        transpositionTable.clear()

        val mosseDisponibili = motore.mosseValideDisponibili()
        Log.d(TAG, "===== Inizio Turno IA =====")
        Log.d(TAG, "Difficoltà: $difficolta")
        Log.d(TAG, "Mosse disponibili (${mosseDisponibili.size}): ${mosseDisponibili.joinToString()}")

        if (mosseDisponibili.isEmpty()) {
            Log.w(TAG, "Nessuna mossa disponibile per l'IA. Ritorno null.")
            return null
        }

        if (difficolta == Difficolta.PRINCIPIANTE) {
            val mossaScelta = mosseDisponibili.random()
            Log.d(TAG, "Livello PRINCIPIANTE: Mossa scelta casualmente -> $mossaScelta")
            return mossaScelta
        }

        return trovaMossaMiglioreConRicercaIterativa(mosseDisponibili)
    }


    /**
     * NUOVA STRATEGIA DI RICERCA: ITERATIVE DEEPENING
     * Esegue ricerche a profondità crescente per ottimizzare la potatura Alfa-Beta.
     */
    private fun trovaMossaMiglioreConRicercaIterativa(mosse: List<Mossa>): Mossa? {
        val coloreIA = motore.turnoCorrente
        var migliorMossaTrovata: Mossa? = mosse.firstOrNull()

        // Cicliamo aumentando la profondità ad ogni passo
        for (depth in 1..difficolta.profonditaMinimax) {
            var migliorMossaInQuestoCiclo: Mossa? = null
            var migliorPunteggio = Int.MIN_VALUE

            for (mossa in mosse) {
                val scacchieraFiglio = simulaMossa(mossa, motore.scacchiera)
                val turnoFiglio = coloreIA.opposto()
                scacchieraFiglio.zobristHash = ZobristHashing.computeHash(scacchieraFiglio, turnoFiglio)

                val punteggio = minimax(scacchieraFiglio, depth - 1, Int.MIN_VALUE, Int.MAX_VALUE, false, coloreIA)

                if (punteggio > migliorPunteggio) {
                    migliorPunteggio = punteggio
                    migliorMossaInQuestoCiclo = mossa
                }
            }
            // La mossa migliore trovata a questa profondità diventa la candidata attuale
            migliorMossaTrovata = migliorMossaInQuestoCiclo
            Log.d(TAG, "Miglior mossa a profondità $depth: $migliorMossaTrovata (Punteggio: $migliorPunteggio)")
        }

        Log.d(TAG, "===== Fine Turno IA / Mossa finale scelta: $migliorMossaTrovata =====")
        return migliorMossaTrovata ?: mosse.randomOrNull()
    }

    // In GiocatoreIA.kt
    /**
     * NUOVA VERSIONE OTTIMIZZATA
     * Ora include l'ordinamento delle mosse per rendere la ricerca Alfa-Beta più veloce.
     */
    private fun trovaMossaMiglioreConMinimax(mosse: List<Mossa>): Mossa? {
        val coloreIA = motore.turnoCorrente
        var migliorMossa: Mossa? = mosse.firstOrNull()
        var migliorPunteggio = Int.MIN_VALUE

        // --- INIZIO OTTIMIZZAZIONE: ORDINAMENTO MOSSE ---
        // 1. Eseguiamo una ricerca veloce e superficiale (profondità 2) per "intuire" le mosse migliori.
        val mosseConPunteggioPreliminare = mosse.map { mossa ->
            val punteggioPreliminare = minimax(
                simulaMossa(mossa, motore.scacchiera), 2, Int.MIN_VALUE, Int.MAX_VALUE, false, coloreIA
            )
            Pair(mossa, punteggioPreliminare)
        }

        // 2. Ordiniamo le mosse in base a questa prima valutazione, dalla migliore alla peggiore.
        val mosseOrdinate = mosseConPunteggioPreliminare.sortedByDescending { it.second }.map { it.first }
        Log.d(TAG, "Mosse ordinate per priorità: ${mosseOrdinate.joinToString()}")
        // --- FINE OTTIMIZZAZIONE ---

        Log.d(TAG, "Inizio calcolo Minimax PROFONDO per ${mosseOrdinate.size} mosse.")

        // 3. Eseguiamo la ricerca profonda sulla lista ordinata.
        for (mossa in mosseOrdinate) {
            val scacchieraSimulata = simulaMossa(mossa, motore.scacchiera)
            val punteggio = minimax(
                scacchiera = scacchieraSimulata,
                profondita = difficolta.profonditaMinimax,
                alpha = Int.MIN_VALUE,
                beta = Int.MAX_VALUE,
                isMaximizing = false,
                coloreIA = coloreIA
            )

            Log.i(TAG, "Mossa valutata (profonda): $mossa -> Punteggio: $punteggio")

            if (punteggio > migliorPunteggio) {
                migliorPunteggio = punteggio
                migliorMossa = mossa
                Log.d(TAG, "!!! Nuova mossa migliore trovata: $migliorMossa (Punteggio: $migliorPunteggio)")
            }
        }

        Log.d(TAG, "===== Fine Turno IA =====")
        Log.d(TAG, "Mossa finale scelta: $migliorMossa")

        return migliorMossa ?: mosse.randomOrNull()
    }

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

        val mossePossibili = motoreSimulato.mosseValideDisponibili()
        if (mossePossibili.isEmpty()) {
            return if (isMaximizing) -10000 else 10000
        }

        var currentAlpha = alpha
        var currentBeta = beta
        var finalScore: Int

        if (isMaximizing) {
            var migliorPunteggio = Int.MIN_VALUE
            for (mossa in mossePossibili) {
                val scacchieraFiglio = simulaMossa(mossa, scacchiera)
                // --- CORREZIONE CHIAVE ---
                val turnoFiglio = coloreIA.opposto()
                scacchieraFiglio.zobristHash = ZobristHashing.computeHash(scacchieraFiglio, turnoFiglio)

                val punteggio = minimax(scacchieraFiglio, profondita - 1, currentAlpha, currentBeta, false, coloreIA)
                migliorPunteggio = maxOf(migliorPunteggio, punteggio)
                currentAlpha = maxOf(currentAlpha, migliorPunteggio)
                if (currentBeta <= currentAlpha) break
            }
            finalScore = migliorPunteggio
        } else {
            var peggiorPunteggio = Int.MAX_VALUE
            for (mossa in mossePossibili) {
                val scacchieraFiglio = simulaMossa(mossa, scacchiera)
                // --- CORREZIONE CHIAVE ---
                val turnoFiglio = coloreIA
                scacchieraFiglio.zobristHash = ZobristHashing.computeHash(scacchieraFiglio, turnoFiglio)

                val punteggio = minimax(scacchieraFiglio, profondita - 1, currentAlpha, currentBeta, true, coloreIA)
                peggiorPunteggio = minOf(peggiorPunteggio, punteggio)
                currentBeta = minOf(currentBeta, peggiorPunteggio)
                if (currentBeta <= currentAlpha) break
            }
            finalScore = peggiorPunteggio
        }

        transpositionTable.store(hash, finalScore, profondita)
        return finalScore
    }

    /**
     * NUOVA FUNZIONE: RICERCA DELLA QUIESCENZA
     * Estende la ricerca per le sole mosse di cattura per evitare l'effetto orizzonte.
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

        // La valutazione della posizione "tranquilla" attuale è il nostro punto di partenza.
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

        // Consideriamo SOLO le mosse di cattura
        val mosseCattura = motoreSimulato.mosseValideDisponibili().filter { it.isCattura() }

        // Se non ci sono catture, la posizione è tranquilla, restituiamo la valutazione.
        if (mosseCattura.isEmpty()) {
            return standingPat
        }

        if (isMaximizing) {
            for (mossa in mosseCattura) {
                val scacchieraFiglio = simulaMossa(mossa, scacchiera)
                val turnoFiglio = coloreIA.opposto()
                scacchieraFiglio.zobristHash = ZobristHashing.computeHash(scacchieraFiglio, turnoFiglio)
                val punteggio = quiescenceSearch(scacchieraFiglio, currentAlpha, currentBeta, false, coloreIA)
                currentAlpha = max(currentAlpha, punteggio)
                if (currentBeta <= currentAlpha) break
            }
            return currentAlpha
        } else {
            for (mossa in mosseCattura) {
                val scacchieraFiglio = simulaMossa(mossa, scacchiera)
                val turnoFiglio = coloreIA
                scacchieraFiglio.zobristHash = ZobristHashing.computeHash(scacchieraFiglio, turnoFiglio)
                val punteggio = quiescenceSearch(scacchieraFiglio, currentAlpha, currentBeta, true, coloreIA)
                currentBeta = min(currentBeta, punteggio)
                if (currentBeta <= currentAlpha) break
            }
            return currentBeta
        }
    }

    private fun simulaMossa(mossa: Mossa, scacchieraOriginale: Scacchiera): Scacchiera {
        val scacchieraSimulata = scacchieraOriginale.copia()
        mossa.posizionePezzoCatturato?.let {
            scacchieraSimulata.rimuoviPezzoA(it)
        }
        scacchieraSimulata.eseguiMossa(mossa)
        return scacchieraSimulata
    }


    private fun Colore.opposto() = if (this == Colore.BIANCO) Colore.NERO else Colore.BIANCO

    /**
     * FUNZIONE DI VALUTAZIONE "GRANDMASTER"
     * Include una comprensione più profonda della strategia della Dama.
     */
    private fun calcolaPunteggio(scacchiera: Scacchiera, coloreIA: Colore): Int {
        val coloreAvversario = coloreIA.opposto()
        if (!scacchiera.hasPezzi(coloreAvversario)) return 100000
        if (!scacchiera.hasPezzi(coloreIA)) return -100000

        val punteggioIA = calcolaPunteggioPerColore(scacchiera, coloreIA)
        val punteggioAvversario = calcolaPunteggioPerColore(scacchiera, coloreAvversario)

        return punteggioIA - punteggioAvversario
    }


    private fun calcolaPunteggioPerColore(scacchiera: Scacchiera, colore: Colore): Int {
        // Pesi per la valutazione
        val valorePedina = 200
        val valoreDamone = 600

        var punteggioMateriale = 0
        var punteggioPosizionale = 0
        var numeroPedine = 0
        var numeroDame = 0

        for (riga in 0..7) {
            for (colonna in 0..7) {
                val pos = Posizione(riga, colonna)
                val pezzo = scacchiera.pezzoA(pos)

                if (pezzo != null && pezzo.colore == colore) {
                    if (pezzo.tipo == TipoPezzo.DAMONE) {
                        numeroDame++
                        punteggioMateriale += valoreDamone
                        // Bonus per le dame centralizzate
                        if (riga in 2..5 && colonna in 2..5) punteggioPosizionale += 20
                    } else {
                        numeroPedine++
                        punteggioMateriale += valorePedina

                        // Bonus di struttura e posizione per le pedine
                        val bonusAvanzamento = if (colore == Colore.BIANCO) (7 - riga) else riga
                        punteggioPosizionale += bonusAvanzamento * 5

                        // Pedine sulla riga di fondo sono cruciali per la difesa
                        if ((colore == Colore.BIANCO && riga == 7) || (colore == Colore.NERO && riga == 0)) {
                            punteggioPosizionale += 25
                        }

                        // Pedine "a cuneo" che controllano il centro
                        if ((colore == Colore.BIANCO && riga in 4..5) || (colore == Colore.NERO && riga in 2..3)) {
                            if (colonna in 3..4) punteggioPosizionale += 15
                        }
                    }
                }
            }
        }

        // Modificatore di fase di gioco: più il gioco è avanzato (meno pezzi), più le dame sono importanti
        val faseDiGioco = (numeroPedine + numeroDame) / 24.0 // Valore da 1.0 (inizio) a ~0 (fine)
        if (faseDiGioco < 0.4) { // Finale di partita
            punteggioMateriale += numeroDame * 100 // Le dame diventano ancora più decisive
        }

        // Il punteggio finale è una combinazione di materiale e posizione
        return punteggioMateriale + punteggioPosizionale
    }
}