package io.github.luposolitario.damaai.game_logic
// File: GiocatoreIA.kt

enum class Difficolta {
    FACILE,
    MEDIO,
    DIFFICILE
}

class GiocatoreIA(
    private val difficolta: Difficolta,
    private val motore: MotoreDiGioco
) {
    // Definiamo quanto in profondità l'IA difficile deve "pensare".
    // Un valore più alto è più forte ma richiede più calcoli. 3 o 4 è un buon inizio.
    private val PROFONDITA_MINIMAX = 3

    fun scegliMossa(): Mossa? {
        val mosseDisponibili = motore.mosseValideDisponibili()
        if (mosseDisponibili.isEmpty()) return null

        val coloreIA = motore.turnoCorrente

        return when (difficolta) {
            Difficolta.FACILE -> mosseDisponibili.random()

            Difficolta.MEDIO -> {
                mosseDisponibili.maxByOrNull { mossa ->
                    simulaEMisuraPunteggio(mossa, coloreIA)
                }
            }

            Difficolta.DIFFICILE -> {
                // LOGICA LIVELLO DIFFICILE: Scegli la mossa con il miglior punteggio futuro.
                var migliorMossa: Mossa? = null
                var migliorPunteggio = Int.MIN_VALUE

                for (mossa in mosseDisponibili) {
                    val scacchieraSimulata = simulaMossa(mossa)
                    val punteggio = minimax(scacchieraSimulata, PROFONDITA_MINIMAX, false, coloreIA)

                    if (punteggio > migliorPunteggio) {
                        migliorPunteggio = punteggio
                        migliorMossa = mossa
                    }
                }
                migliorMossa
            }
        }
    }

    private fun minimax(scacchiera: Scacchiera, profondita: Int, isMaximizing: Boolean, coloreIA: Colore): Int {
        // Caso base: se abbiamo raggiunto la profondità massima o la partita è finita,
        // restituiamo il punteggio statico della scacchiera.
        if (profondita == 0 /* Aggiungeremo qui il controllo di fine partita */) {
            return calcolaPunteggio(scacchiera, coloreIA)
        }

        // Creiamo un motore temporaneo per trovare le mosse valide nello stato simulato.
        val motoreSimulato = MotoreDiGioco()
        motoreSimulato.scacchiera = scacchiera
        motoreSimulato.turnoCorrente = if(isMaximizing) coloreIA else (if (coloreIA == Colore.BIANCO) Colore.NERO else Colore.BIANCO)

        val mossePossibili = motoreSimulato.mosseValideDisponibili()
        if(mossePossibili.isEmpty()){
            return calcolaPunteggio(scacchiera, coloreIA)
        }

        if (isMaximizing) { // Turno dell'IA (Max)
            var migliorPunteggio = Int.MIN_VALUE
            for (mossa in mossePossibili) {
                val scacchieraFiglio = motoreSimulato.simulaMossa(mossa)
                val punteggio = minimax(scacchieraFiglio, profondita - 1, false, coloreIA)
                migliorPunteggio = maxOf(migliorPunteggio, punteggio)
            }
            return migliorPunteggio
        } else { // Turno dell'avversario (Min)
            var peggiorPunteggio = Int.MAX_VALUE
            for (mossa in mossePossibili) {
                val scacchieraFiglio = motoreSimulato.simulaMossa(mossa)
                val punteggio = minimax(scacchieraFiglio, profondita - 1, true, coloreIA)
                peggiorPunteggio = minOf(peggiorPunteggio, punteggio)
            }
            return peggiorPunteggio
        }
    }

    private fun simulaMossa(mossa: Mossa, scacchieraOriginale: Scacchiera = motore.scacchiera): Scacchiera {
        val scacchieraSimulata = scacchieraOriginale.copia()
        mossa.posizionePezzoCatturato?.let { scacchieraSimulata.rimuoviPezzoA(it) }
        scacchieraSimulata.eseguiMossa(mossa)
        return scacchieraSimulata
    }

    // Per il livello MEDIO
    private fun simulaEMisuraPunteggio(mossa: Mossa, coloreIA: Colore): Int {
        val scacchieraSimulata = simulaMossa(mossa)
        return calcolaPunteggio(scacchieraSimulata, coloreIA)
    }

    private fun calcolaPunteggio(scacchiera: Scacchiera, coloreIA: Colore): Int {
        var punteggio = 0
        // ... (il resto della funzione è invariato)
        val valorePedina = 1
        val valoreDamone = 3

        for (riga in 0..7) {
            for (colonna in 0..7) {
                val pezzo = scacchiera.pezzoA(Posizione(riga, colonna))
                if (pezzo != null) {
                    val valore = if (pezzo.tipo == TipoPezzo.DAMONE) valoreDamone else valorePedina
                    if (pezzo.colore == coloreIA) punteggio += valore
                    else punteggio -= valore
                }
            }
        }
        return punteggio
    }

    // Funzione di supporto per minimax
    private fun MotoreDiGioco.simulaMossa(mossa: Mossa): Scacchiera {
        val scacchieraCopiata = this.scacchiera.copia()
        mossa.posizionePezzoCatturato?.let { scacchieraCopiata.rimuoviPezzoA(it) }
        scacchieraCopiata.eseguiMossa(mossa)
        return scacchieraCopiata
    }
}