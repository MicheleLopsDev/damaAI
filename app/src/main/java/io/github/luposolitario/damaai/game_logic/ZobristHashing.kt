// File: ZobristHashing.kt
package io.github.luposolitario.damaai.game_logic

import android.util.Log
import kotlin.random.Random

/**
 * Gestisce la logica di Zobrist Hashing per creare una "firma" quasi unica
 * per ogni possibile configurazione della scacchiera.
 */
object ZobristHashing {
    // Dimensioni: 8x8 scacchiera, 2 tipi di pezzo (Pedina, Damone), 2 colori (Bianco, Nero)
    // L'array avrà una dimensione di 8 * 8 * 2 * 2 = 256
    private val zobristTable = Array(8) { Array(8) { Array(2) { LongArray(2) } } }
    private val TAG = "GiocatoreIA_Debug"

    // Un numero casuale per indicare di chi è il turno (Nero)
    private val blackTurnHash: Long

    init {
        val random = Random(123456789L) // Usiamo un seme fisso per la riproducibilità

        // Inizializza la tabella con numeri casuali a 64 bit
        for (riga in 0..7) {
            for (colonna in 0..7) {
                for (tipoPezzo in TipoPezzo.values()) {
                    for (colore in Colore.values()) {
                        zobristTable[riga][colonna][tipoPezzo.ordinal][colore.ordinal] = random.nextLong()
                    }
                }
            }
        }
        blackTurnHash = random.nextLong()
        Log.d(TAG, "blackTurnHash: $blackTurnHash")
    }

    /**
     * Calcola l'hash Zobrist iniziale per una data scacchiera.
     */
    fun computeHash(scacchiera: Scacchiera, turno: Colore): Long {
        var hash = 0L
        for (riga in 0..7) {
            for (colonna in 0..7) {
                val pezzo = scacchiera.pezzoA(Posizione(riga, colonna))
                if (pezzo != null) {
                    hash = hash xor getPieceHash(riga, colonna, pezzo.tipo, pezzo.colore)
                }
            }
        }

        if (turno == Colore.NERO) {
            hash = hash xor blackTurnHash
        }
        //Log.d(TAG, "hash: $hash")
        return hash
    }

    private fun getPieceHash(riga: Int, colonna: Int, tipo: TipoPezzo, colore: Colore): Long {
        return zobristTable[riga][colonna][tipo.ordinal][colore.ordinal]
    }
}