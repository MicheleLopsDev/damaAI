package io.github.luposolitario.damaai.data

// Aggiungi questo codice in fondo al file GameModel.kt

import androidx.annotation.DrawableRes
import io.github.luposolitario.damaai.R
import androidx.compose.ui.graphics.Color

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

// Aggiungi questo codice in fondo al file GameModel.kt

data class TeamStyle(
    val id: String, // Un ID univoco, es. "italy"
    val nationName: String, // Il nome da mostrare, es. "Italia"
    @DrawableRes val flagResId: Int // L'ID della risorsa immagine in drawable
)

// Creiamo una lista degli stili che la nostra app supporterà
val availableTeamStyles = listOf(
    // Usiamo il tuo dama_icon.png come opzione di default
    TeamStyle(id = "default", nationName = "Classico", flagResId = R.drawable.dama_icon),
    // Nazioni
    TeamStyle(id = "italy", nationName = "Italia", flagResId = R.drawable.flag_italy),
    TeamStyle(id = "france", nationName = "Francia", flagResId = R.drawable.flag_france),
    TeamStyle(id = "germany", nationName = "Germania", flagResId = R.drawable.flag_germany),
    TeamStyle(id = "spain", nationName = "Spagna", flagResId = R.drawable.flag_spain),
    TeamStyle(id = "uk", nationName = "Regno Unito", flagResId = R.drawable.flag_uk),
    TeamStyle(id = "usa", nationName = "Stati Uniti", flagResId = R.drawable.flag_usa)
)

// Aggiungi questo codice in fondo al file GameModel.kt



// ... (il codice esistente rimane invariato)

// --- NUOVO: Definiamo gli stili per la scacchiera ---

data class BoardStyle(
    val id: String,
    val name: String,
    val lightSquareColor: Color,
    val darkSquareColor: Color
)

val availableBoardStyles = listOf(
    BoardStyle(
        id = "wood",
        name = "Legno",
        lightSquareColor = Color(0xFFF0D9B5),
        darkSquareColor = Color(0xFFB58863)
    ),
    BoardStyle(
        id = "marble",
        name = "Marmo",
        lightSquareColor = Color(0xFFFFFFFF),
        darkSquareColor = Color(0xFF9E9E9E)
    ),
    BoardStyle(
        id = "modern",
        name = "Moderno",
        lightSquareColor = Color(0xFFE1F5FE), // Azzurro chiaro
        darkSquareColor = Color(0xFF0277BD)  // Blu scuro
    )
)
// --- FINE PARTE NUOVA ---