// File: GiocatoreIA.kt
package io.github.luposolitario.damaai.game_logic

enum class Difficolta(val profonditaMinimax: Int) {
    PRINCIPIANTE(0),
    NOVIZIO(1),
    INTERMEDIO(3),
    AVANZATO(5),
    ESPERTO(7)
}

class GiocatoreIA(
    private val difficolta: Difficolta,
    private val motore: MotoreDiGioco
) {

    fun scegliMossa(): Mossa? {
        val mosseDisponibili = motore.mosseValideDisponibili()
        if (mosseDisponibili.isEmpty()) return null

        if (difficolta == Difficolta.PRINCIPIANTE) {
            return mosseDisponibili.random()
        }

        return trovaMossaMiglioreConMinimax(mosseDisponibili)
    }

    private fun trovaMossaMiglioreConMinimax(mosse: List<Mossa>): Mossa? {
        val coloreIA = motore.turnoCorrente
        var migliorMossa: Mossa? = mosse.firstOrNull()
        var migliorPunteggio = Int.MIN_VALUE

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

            if (punteggio > migliorPunteggio) {
                migliorPunteggio = punteggio
                migliorMossa = mossa
            }
        }
        return migliorMossa
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
            return calcolaPunteggio(scacchiera, coloreIA)
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

    private fun calcolaPunteggio(scacchiera: Scacchiera, coloreIA: Colore): Int {
        var punteggio = 0
        val valorePedina = 10
        val valoreDamone = 30

        for (riga in 0..7) {
            for (colonna in 0..7) {
                val pezzo = scacchiera.pezzoA(Posizione(riga, colonna))
                if (pezzo != null) {
                    val valoreBase = if (pezzo.tipo == TipoPezzo.DAMONE) valoreDamone else valorePedina
                    var bonusPosizionale = 0
                    if (pezzo.tipo == TipoPezzo.PEDINA) {
                        if (pezzo.colore == Colore.BIANCO) {
                            bonusPosizionale = (7 - riga)
                        } else {
                            bonusPosizionale = riga
                        }
                    }
                    val valoreTotale = valoreBase + bonusPosizionale
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