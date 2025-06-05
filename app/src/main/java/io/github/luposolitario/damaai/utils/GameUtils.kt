package io.github.luposolitario.damaai.utils

import io.github.luposolitario.damaai.data.Piece
import io.github.luposolitario.damaai.data.PlayerColor
import kotlin.math.abs

/**
 * Controlla se una mossa è valida secondo le regole di base.
 * Per ora: solo passo singolo in diagonale in avanti su casella vuota.
 */
fun isValidMove(
    piece: Piece,
    targetRow: Int,
    targetCol: Int,
    allPieces: List<Piece>
): Boolean {
    // 1. La casella di destinazione deve essere vuota.
    if (allPieces.any { it.row == targetRow && it.col == targetCol }) {
        return false // C'è già una pedina in quella casella
    }

    // 2. Calcoliamo la differenza di righe e colonne.
    val rowDiff = targetRow - piece.row
    val colDiff = abs(targetCol - piece.col)

    // 3. La mossa deve essere di una sola casella in diagonale.
    if (colDiff != 1) {
        return false
    }

    // 4. La mossa deve essere in avanti.
    return when (piece.color) {
        PlayerColor.WHITE -> rowDiff == -1
        PlayerColor.BLACK -> rowDiff == 1
    }
}

/**
 * Funzione helper per formattare il tempo in MM:SS.
 */
fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}