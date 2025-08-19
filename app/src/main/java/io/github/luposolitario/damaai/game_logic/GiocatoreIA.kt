package io.github.luposolitario.damaai.game_logic

import android.util.Log

// Livelli di difficoltà aggiornati con il nuovo livello MAESTRO
enum class Difficolta(val profonditaMinimax: Int) {
    PRINCIPIANTE(0),
    NOVIZIO(1),
    INTERMEDIO(3),
    AVANZATO(5),
    ESPERTO(8),
    MAESTRO(10)
}

class GiocatoreIA(
    private val difficolta: Difficolta,
    private val motore: MotoreDiGioco
) {

    private val TAG = "GiocatoreIA_Debug"

    // La logica di scegliMossa e minimax rimane invariata, userà la nuova `calcolaPunteggio`.
    fun scegliMossa(): Mossa? {
        val mosseDisponibili = motore.mosseValideDisponibili()

        // --- NUOVO LOG [1] ---
        Log.d(TAG, "===== Inizio Turno IA =====")
        Log.d(TAG, "Difficoltà: $difficolta")
        Log.d(TAG, "Mosse disponibili (${mosseDisponibili.size}): ${mosseDisponibili.joinToString()}")


        if (mosseDisponibili.isEmpty()) {
            // --- NUOVO LOG [2] ---
            Log.w(TAG, "Nessuna mossa disponibile per l'IA. Ritorno null.")
            return null
        }

        if (difficolta == Difficolta.PRINCIPIANTE) {
            val mossaScelta = mosseDisponibili.random()
            // --- NUOVO LOG [3] ---
            Log.d(TAG, "Livello PRINCIPIANTE: Mossa scelta casualmente -> $mossaScelta")
            return mossaScelta
        }

        return trovaMossaMiglioreConMinimax(mosseDisponibili)
    }

    // In GiocatoreIA.kt
    private fun trovaMossaMiglioreConMinimax(mosse: List<Mossa>): Mossa? {
        val coloreIA = motore.turnoCorrente
        var migliorMossa: Mossa? = null
        var migliorPunteggio = Int.MIN_VALUE

        try {
            migliorMossa = mosse.firstOrNull() // Imposta una mossa di default

            for (mossa in mosse) {
                val scacchieraSimulata = simulaMossa(mossa, motore.scacchiera)
                val punteggio = minimax(
                    scacchiera = scacchieraSimulata,
                    profondita = difficolta.profonditaMinimax,
                    alpha = Int.MIN_VALUE,
                    beta = Int.MAX_VALUE,
                    isMaximizing = false,
                    coloreIA = coloreIA
                )

                Log.i("GiocatoreIA_Debug", "Mossa valutata: $mossa -> Punteggio: $punteggio")

                if (punteggio > migliorPunteggio) {
                    migliorPunteggio = punteggio
                    migliorMossa = mossa
                    Log.d("GiocatoreIA_Debug", "!!! Nuova mossa migliore trovata: $migliorMossa (Punteggio: $migliorPunteggio)")
                }
            }
        } catch (e: Exception) {
            Log.e("GiocatoreIA_Debug", "ERRORE CRITICO in Minimax! Si procede con una mossa casuale.", e)
        }

        // CONTROLLO DI SICUREZZA FINALE: se `migliorMossa` è null, ne sceglie una a caso tra quelle valide.
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
        if (profondita == 0) {
            return calcolaPunteggio(scacchiera, coloreIA)
        }

        val motoreSimulato = MotoreDiGioco().apply {
            this.scacchiera = scacchiera
            this.turnoCorrente = if (isMaximizing) coloreIA else coloreIA.opposto()
        }

        val mossePossibili = motoreSimulato.mosseValideDisponibili()
        if (mossePossibili.isEmpty()) {
            // Se non ci sono mosse, è una condizione terminale (vittoria o sconfitta netta)
            val vincitore = if (isMaximizing) coloreIA.opposto() else coloreIA
            return if (vincitore == coloreIA) 10000 else -10000
        }

        var currentAlpha = alpha
        var currentBeta = beta

        if (isMaximizing) {
            var migliorPunteggio = Int.MIN_VALUE
            for (mossa in mossePossibili) {
                val scacchieraFiglio = simulaMossa(mossa, scacchiera)
                val punteggio = minimax(scacchieraFiglio, profondita - 1, currentAlpha, currentBeta, false, coloreIA)
                migliorPunteggio = maxOf(migliorPunteggio, punteggio)
                currentAlpha = maxOf(currentAlpha, migliorPunteggio)
                if (currentBeta <= currentAlpha) break
            }
            return migliorPunteggio
        } else {
            var peggiorPunteggio = Int.MAX_VALUE
            for (mossa in mossePossibili) {
                val scacchieraFiglio = simulaMossa(mossa, scacchiera)
                val punteggio = minimax(scacchieraFiglio, profondita - 1, currentAlpha, currentBeta, true, coloreIA)
                peggiorPunteggio = minOf(peggiorPunteggio, punteggio)
                currentBeta = minOf(currentBeta, peggiorPunteggio)
                if (currentBeta <= currentAlpha) break
            }
            return peggiorPunteggio
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

    /**
     * NUOVA FUNZIONE DI VALUTAZIONE POTENZIATA
     * Assegna un punteggio a una configurazione della scacchiera tenendo conto di più fattori strategici.
     */
    private fun calcolaPunteggio(scacchiera: Scacchiera, coloreIA: Colore): Int {
        var punteggio = 0
        val valorePedina = 100 // Aumentiamo i valori base per dare più spazio ai bonus/malus
        val valoreDamone = 300

        for (riga in 0..7) {
            for (colonna in 0..7) {
                val pos = Posizione(riga, colonna)
                val pezzo = scacchiera.pezzoA(pos)
                if (pezzo != null) {
                    var valoreBase = if (pezzo.tipo == TipoPezzo.DAMONE) valoreDamone else valorePedina

                    // 1. Bonus Posizionale (invariato, ma più impattante con i nuovi valori)
                    var bonusPosizionale = 0
                    if (pezzo.tipo == TipoPezzo.PEDINA) {
                        bonusPosizionale = if (pezzo.colore == Colore.BIANCO) (7 - riga) * 5 else riga * 5
                    }

                    // 2. Bonus Sicurezza e Controllo del Centro
                    var bonusSicurezza = 0
                    // Le pedine sulle prime/ultime due righe sono "ancore" difensive importanti
                    if (pezzo.tipo == TipoPezzo.PEDINA) {
                        if ((pezzo.colore == Colore.BIANCO && riga > 5) || (pezzo.colore == Colore.NERO && riga < 2)) {
                            bonusSicurezza += 10
                        }
                    }
                    // I pezzi che controllano il centro sono più potenti
                    if (colonna in 2..5) {
                        bonusSicurezza += 5
                    }

                    val valoreTotale = valoreBase + bonusPosizionale + bonusSicurezza

                    if (pezzo.colore == coloreIA) {
                        punteggio += valoreTotale
                    } else {
                        punteggio -= valoreTotale
                    }
                }
            }
        }
        return punteggio
    }

    private fun Colore.opposto() = if (this == Colore.BIANCO) Colore.NERO else Colore.BIANCO
}