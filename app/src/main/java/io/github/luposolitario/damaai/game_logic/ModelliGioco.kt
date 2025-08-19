package io.github.luposolitario.damaai.game_logic

enum class Colore {
    BIANCO,
    NERO
}

enum class TipoPezzo {
    PEDINA,
    DAMONE
}

data class Pezzo(val colore: Colore, val tipo: TipoPezzo = TipoPezzo.PEDINA)

/**
 * Rappresenta una sequenza di catture, anche multiple.
 * Contiene i dati necessari per applicare le regole di priorità della Dama Italiana.
 */
data class SequenzaCattura(
    val mossaIniziale: Mossa,
    val pezziCatturati: List<Pezzo>,
    val pezzoCatturante: Pezzo
) {
    val numeroPezziCatturati: Int = pezziCatturati.size
    val numeroDameCatturate: Int = pezziCatturati.count { it.tipo == TipoPezzo.DAMONE }
}


class MotoreDiGioco() {

    var scacchiera: Scacchiera = Scacchiera()
        internal set

    var turnoCorrente: Colore = Colore.BIANCO
        internal set

    init {
        // Calcola l'hash iniziale quando il motore viene creato
        scacchiera.zobristHash = ZobristHashing.computeHash(scacchiera, turnoCorrente)
    }


    /**
     * MODIFICATO per Dama Italiana:
     * Ora calcola tutte le sequenze di cattura e restituisce solo quelle
     * che rispettano le regole di priorità.
     */
    fun mosseValideDisponibili(): List<Mossa> {
        val tutteLeSequenze = trovaTutteLeSequenzeDiCattura(turnoCorrente)

        if (tutteLeSequenze.isNotEmpty()) {
            // Applica le regole di priorità della Dama Italiana
            // 1. Priorità per numero di pezzi catturati
            val maxPezziCatturati = tutteLeSequenze.maxOf { it.numeroPezziCatturati }
            var sequenzeFiltrate = tutteLeSequenze.filter { it.numeroPezziCatturati == maxPezziCatturati }

            // 2. Priorità per pezzo catturante (Dama > Pedina)
            if (sequenzeFiltrate.any { it.pezzoCatturante.tipo == TipoPezzo.DAMONE }) {
                sequenzeFiltrate = sequenzeFiltrate.filter { it.pezzoCatturante.tipo == TipoPezzo.DAMONE }
            }

            // 3. Priorità per numero di Dame catturate
            val maxDameCatturate = sequenzeFiltrate.maxOfOrNull { it.numeroDameCatturate } ?: 0
            if (maxDameCatturate > 0) {
                sequenzeFiltrate = sequenzeFiltrate.filter { it.numeroDameCatturate == maxDameCatturate }
            }

            // Restituisce la mossa iniziale di ogni sequenza valida, evitando duplicati
            return sequenzeFiltrate.map { it.mossaIniziale }.distinct()
        }

        // Se non ci sono catture, restituisce le mosse semplici
        return trovaTutteLeMosseSemplici(turnoCorrente)
    }

    // Dentro la classe MotoreDiGioco
    fun eseguiMossa(mossa: Mossa): Boolean {
        val mosseValide = mosseValideDisponibili()
        if (!mosseValide.any { it.partenza == mossa.partenza && it.arrivo == mossa.arrivo }) {
            return false
        }

        val pezzoMosso = scacchiera.pezzoA(mossa.partenza) ?: return false

        mossa.posizionePezzoCatturato?.let {
            scacchiera.rimuoviPezzoA(it)
        }
        scacchiera.eseguiMossa(mossa)

        if (mossa.posizionePezzoCatturato != null) {
            val pezzoDopoMossa = scacchiera.pezzoA(mossa.arrivo) ?: pezzoMosso
            val mossePostCattura = if (pezzoDopoMossa.tipo == TipoPezzo.PEDINA) {
                trovaMosseDiCatturaPerPedina(mossa.arrivo, pezzoDopoMossa, scacchiera)
            } else {
                trovaMosseDiCatturaPerDamone(mossa.arrivo, pezzoDopoMossa, scacchiera)
            }

            if (mossePostCattura.isEmpty()) {
                cambiaTurno()
            }
        } else {
            cambiaTurno()
        }

        scacchiera.zobristHash = ZobristHashing.computeHash(scacchiera, turnoCorrente)

        return true
    }

    fun getPezziConCatturaObbligatoria(): List<Posizione> {
        // Usa la nuova logica di mosseValideDisponibili che già contiene le priorità
        return mosseValideDisponibili()
            .filter { it.posizionePezzoCatturato != null }
            .map { it.partenza }
            .distinct()
    }

    private fun cambiaTurno() {
        turnoCorrente = if (turnoCorrente == Colore.BIANCO) Colore.NERO else Colore.BIANCO
    }

    // --- LOGICA DI RICERCA SEQUENZE (CORRETTA) ---

    private fun trovaTutteLeSequenzeDiCattura(colore: Colore): List<SequenzaCattura> {
        val sequenze = mutableListOf<SequenzaCattura>()
        forEachPezzoDelGiocatore(colore) { pos, pezzo ->
            // Avvia la ricerca ricorsiva per ogni pezzo
            trovaSequenzeRicorsive(
                posAttuale = pos,
                pezzoCatturante = pezzo,
                scacchieraCorrente = scacchiera,
                pezziCatturatiPrecedenti = emptyList(),
                mossaIniziale = null,
                listaSequenzeTrovate = sequenze,
                caselleVisitate = setOf(pos) // --- NUOVO: Inizia con la posizione di partenza
            )
        }
        return sequenze
    }

    /**
     * VERSIONE CORRETTA con controllo anti-ciclo infinito.
     */
    private fun trovaSequenzeRicorsive(
        posAttuale: Posizione,
        pezzoCatturante: Pezzo,
        scacchieraCorrente: Scacchiera,
        pezziCatturatiPrecedenti: List<Pezzo>,
        mossaIniziale: Mossa?,
        listaSequenzeTrovate: MutableList<SequenzaCattura>,
        caselleVisitate: Set<Posizione> // --- NUOVO PARAMETRO ---
    ) {
        val mosseCatturaPossibili = if (pezzoCatturante.tipo == TipoPezzo.PEDINA) {
            trovaMosseDiCatturaPerPedina(posAttuale, pezzoCatturante, scacchieraCorrente)
        } else {
            trovaMosseDiCatturaPerDamone(posAttuale, pezzoCatturante, scacchieraCorrente)
        }

        if (mosseCatturaPossibili.isEmpty()) {
            if (pezziCatturatiPrecedenti.isNotEmpty()) {
                listaSequenzeTrovate.add(
                    SequenzaCattura(
                        mossaIniziale = mossaIniziale!!,
                        pezziCatturati = pezziCatturatiPrecedenti,
                        pezzoCatturante = pezzoCatturante
                    )
                )
            }
        } else {
            for (mossa in mosseCatturaPossibili) {
                // --- CONTROLLO ANTI-CICLO ---
                // Se la casella di arrivo è già stata visitata in questa sequenza,
                // ignoriamo questa mossa per evitare loop infiniti.
                if (mossa.arrivo in caselleVisitate) {
                    continue // Salta al prossimo ciclo del for
                }
                // --- FINE CONTROLLO ---

                val pezzoCatturatoPos = mossa.posizionePezzoCatturato!!
                val pezzoCatturato = scacchieraCorrente.pezzoA(pezzoCatturatoPos)!!
                val nuoviPezziCatturati = pezziCatturatiPrecedenti + pezzoCatturato
                val primaMossaDellaSequenza = mossaIniziale ?: mossa

                listaSequenzeTrovate.add(
                    SequenzaCattura(
                        mossaIniziale = primaMossaDellaSequenza,
                        pezziCatturati = nuoviPezziCatturati,
                        pezzoCatturante = pezzoCatturante
                    )
                )

                val scacchieraDopoMossa = scacchieraCorrente.copia()
                scacchieraDopoMossa.rimuoviPezzoA(pezzoCatturatoPos)
                scacchieraDopoMossa.eseguiMossa(Mossa(mossa.partenza, mossa.arrivo))

                trovaSequenzeRicorsive(
                    posAttuale = mossa.arrivo,
                    pezzoCatturante = pezzoCatturante,
                    scacchieraCorrente = scacchieraDopoMossa,
                    pezziCatturatiPrecedenti = nuoviPezziCatturati,
                    mossaIniziale = primaMossaDellaSequenza,
                    listaSequenzeTrovate = listaSequenzeTrovate,
                    caselleVisitate = caselleVisitate + mossa.arrivo // Aggiunge la nuova casella al set di quelle visitate
                )
            }
        }
    }

    // I metodi di ricerca mosse ora accettano una scacchiera per la simulazione
    private fun trovaMosseDiCatturaPerPedina(pos: Posizione, pezzo: Pezzo, scacchieraData: Scacchiera): List<Mossa> {
        val mosse = mutableListOf<Mossa>()
        val direzione = if (pezzo.colore == Colore.BIANCO) -1 else 1

        for (dirColonna in listOf(-1, 1)) {
            val posAvversario = Posizione(pos.riga + direzione, pos.colonna + dirColonna)
            val posArrivo = Posizione(pos.riga + direzione * 2, pos.colonna + dirColonna * 2)

            val pezzoAvversario = scacchieraData.pezzoA(posAvversario)
            if (pezzoAvversario != null &&
                pezzoAvversario.colore != pezzo.colore &&
                pezzoAvversario.tipo == TipoPezzo.PEDINA &&
                isCasellaVuota(posArrivo, scacchieraData)) {
                mosse.add(Mossa(pos, posArrivo, posAvversario))
            }
        }
        return mosse
    }

    private fun trovaMosseDiCatturaPerDamone(pos: Posizione, pezzo: Pezzo, scacchieraData: Scacchiera): List<Mossa> {
        val mosse = mutableListOf<Mossa>()
        for (dirRiga in listOf(-1, 1)) {
            for (dirColonna in listOf(-1, 1)) {
                val posAvversario = Posizione(pos.riga + dirRiga, pos.colonna + dirColonna)
                val posArrivo = Posizione(pos.riga + dirRiga * 2, pos.colonna + dirColonna * 2)

                if (isPezzoAvversario(posAvversario, pezzo.colore, scacchieraData) && isCasellaVuota(posArrivo, scacchieraData)) {
                    mosse.add(Mossa(pos, posArrivo, posAvversario))
                }
            }
        }
        return mosse
    }

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

    private fun trovaMosseSempliciPerPedina(pos: Posizione, pezzo: Pezzo): List<Mossa> {
        val mosse = mutableListOf<Mossa>()
        val direzione = if (pezzo.colore == Colore.BIANCO) -1 else 1
        val posArrivoSinistra = Posizione(pos.riga + direzione, pos.colonna - 1)
        if (isCasellaVuota(posArrivoSinistra, this.scacchiera)) mosse.add(Mossa(pos, posArrivoSinistra))
        val posArrivoDestra = Posizione(pos.riga + direzione, pos.colonna + 1)
        if (isCasellaVuota(posArrivoDestra, this.scacchiera)) mosse.add(Mossa(pos, posArrivoDestra))
        return mosse
    }

    private fun trovaMosseSempliciPerDamone(pos: Posizione): List<Mossa> {
        val mosse = mutableListOf<Mossa>()
        for (dirRiga in listOf(-1, 1)) {
            for (dirColonna in listOf(-1, 1)) {
                val posArrivo = Posizione(pos.riga + dirRiga, pos.colonna + dirColonna)
                if (isCasellaVuota(posArrivo, this.scacchiera)) {
                    mosse.add(Mossa(pos, posArrivo))
                }
            }
        }
        return mosse
    }

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
    private fun isCasellaVuota(pos: Posizione, scacchiera: Scacchiera): Boolean = isCasellaValida(pos) && scacchiera.pezzoA(pos) == null
    private fun isPezzoAvversario(pos: Posizione, coloreProprio: Colore, scacchiera: Scacchiera): Boolean {
        if (!isCasellaValida(pos)) return false
        val pezzo = scacchiera.pezzoA(pos)
        return pezzo != null && pezzo.colore != coloreProprio
    }
}