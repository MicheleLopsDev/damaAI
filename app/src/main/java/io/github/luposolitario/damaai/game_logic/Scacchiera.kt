// michelelopsdev/damaai/damaAI-4607344960c2303f34c37dc7e118d6e6fbf7c21e/app/src/main/java/io/github/luposolitario/damaai/game_logic/Scacchiera.kt
package io.github.luposolitario.damaai.game_logic

class Scacchiera {

    var zobristHash: Long = 0L
    private val mappaPezzi: MutableMap<Posizione, Pezzo> = mutableMapOf()

    init {
        impostaScacchieraIniziale()
    }

    fun impostaScacchieraPerTest(pezzi: Map<Posizione, Pezzo>) {
        mappaPezzi.clear()
        mappaPezzi.putAll(pezzi)
    }

    fun contaPezzi(): Int = mappaPezzi.size

    fun copia(): Scacchiera {
        val nuovaScacchiera = Scacchiera()
        nuovaScacchiera.mappaPezzi.clear()
        nuovaScacchiera.mappaPezzi.putAll(this.mappaPezzi)
        nuovaScacchiera.zobristHash = this.zobristHash
        return nuovaScacchiera
    }

    fun pezzoA(posizione: Posizione): Pezzo? = mappaPezzi[posizione]

    /**
     * Esegue una mossa e aggiorna l'hash in modo incrementale.
     * Presume che la mossa sia valida.
     */
    fun eseguiMossa(mossa: Mossa) {
        val pezzo = pezzoA(mossa.partenza) ?: return

        // 1. Rimuovi il pezzo dalla partenza
        rimuoviPezzoA(mossa.partenza)

        // 2. Rimuovi il pezzo catturato, se esiste
        mossa.pezzoCatturato?.let {
            rimuoviPezzoA(mossa.posizionePezzoCatturato!!)
        }

        // 3. Aggiungi il pezzo a destinazione (promosso o meno)
        val pezzoMosso = if (mossa.isPromozione) {
            Pezzo(pezzo.colore, TipoPezzo.DAMONE)
        } else {
            pezzo
        }
        aggiungiPezzoA(pezzoMosso, mossa.arrivo)
    }

    /**
     * NUOVO: Annulla una mossa e ripristina l'hash.
     */
    fun annullaMossa(mossa: Mossa) {
        val pezzoMosso = pezzoA(mossa.arrivo) ?: return

        // 1. Rimuovi il pezzo dall'arrivo
        rimuoviPezzoA(mossa.arrivo)

        // 2. Ripristina il pezzo alla partenza (gestendo il de-promoting)
        val pezzoOriginale = if (mossa.isPromozione) {
            Pezzo(pezzoMosso.colore, TipoPezzo.PEDINA)
        } else {
            pezzoMosso
        }
        aggiungiPezzoA(pezzoOriginale, mossa.partenza)

        // 3. Ripristina il pezzo catturato, se esisteva
        mossa.pezzoCatturato?.let {
            aggiungiPezzoA(it, mossa.posizionePezzoCatturato!!)
        }
    }

    fun rimuoviPezzoA(posizione: Posizione) {
        val pezzo = mappaPezzi.remove(posizione)
        if (pezzo != null) {
            zobristHash = zobristHash xor ZobristHashing.getPieceHash(posizione.riga, posizione.colonna, pezzo.tipo, pezzo.colore)
        }
    }

    private fun aggiungiPezzoA(pezzo: Pezzo, posizione: Posizione) {
        mappaPezzi[posizione] = pezzo
        zobristHash = zobristHash xor ZobristHashing.getPieceHash(posizione.riga, posizione.colonna, pezzo.tipo, pezzo.colore)
    }

    // ... il resto della classe (impostaScacchieraIniziale, toString, etc) rimane invariato ...
    private fun impostaScacchieraIniziale() {
        mappaPezzi.clear()
        for (riga in 0..2) {
            for (colonna in 0..7) {
                if ((riga + colonna) % 2 != 0) {
                    mappaPezzi[Posizione(riga, colonna)] = Pezzo(Colore.NERO)
                }
            }
        }
        for (riga in 5..7) {
            for (colonna in 0..7) {
                if ((riga + colonna) % 2 != 0) {
                    mappaPezzi[Posizione(riga, colonna)] = Pezzo(Colore.BIANCO)
                }
            }
        }
    }
    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("  A B C D E F G H\n")
        for (riga in 0..7) {
            builder.append("${riga + 1} ")
            for (colonna in 0..7) {
                val pezzo = pezzoA(Posizione(riga, colonna))
                val simbolo = when (pezzo?.colore) {
                    Colore.BIANCO -> if (pezzo.tipo == TipoPezzo.PEDINA) "b" else "B"
                    Colore.NERO -> if (pezzo.tipo == TipoPezzo.PEDINA) "n" else "N"
                    null -> if ((riga + colonna) % 2 != 0) "." else " "
                }
                builder.append("$simbolo ")
            }
            builder.append("\n")
        }
        return builder.toString()
    }
    fun hasPezzi(colore: Colore): Boolean {
        return mappaPezzi.values.any { it.colore == colore }
    }

    /**
     * NUOVO METODO
     * Controlla se un dato colore ha vinto la partita.
     * La vittoria si verifica quando l'avversario non ha più pezzi.
     */
    fun haVinto(colore: Colore): Boolean {
        val coloreAvversario = if (colore == Colore.BIANCO) Colore.NERO else Colore.BIANCO
        return !hasPezzi(coloreAvversario)
    }
}