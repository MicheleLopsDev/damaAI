package io.github.luposolitario.damaai.data

enum class PlayerColor {
    WHITE, BLACK
}

data class Piece(
    val row: Int,
    val col: Int,
    val color: PlayerColor,
)

// Sostituisci la vecchia GameState con questa
data class GameState(
    val pieces: List<Piece>,
    val selectedPiece: Piece? = null,
    val currentPlayer: PlayerColor = PlayerColor.WHITE,
    val turnElapsedTimeInSeconds: Long = 0L // <-- NUOVA RIGA
)