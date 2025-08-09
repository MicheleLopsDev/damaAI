package io.github.luposolitario.damaai.data

// Aggiungi questo codice in fondo al file GameModel.kt

import androidx.annotation.DrawableRes
import io.github.luposolitario.damaai.R
import androidx.compose.ui.graphics.Color
import io.github.luposolitario.damaai.game_logic.Posizione

enum class PlayerColor {
    WHITE, BLACK
}

data class Piece(
    val row: Int,
    val col: Int,
    val color: PlayerColor,
    val isDama: Boolean = false // <-- AGGIUNGI QUESTA RIGA
)

data class GameState(
    val pieces: List<Piece>,
    val selectedPiece: Piece? = null,
    val currentPlayer: PlayerColor = PlayerColor.WHITE,
    val turnElapsedTimeInSeconds: Long = 0L,
    val capturedPiece: Piece? = null,
    val mandatoryCapturePieces: List<Posizione> = emptyList() // <-- NUOVA RIGA
)

// Aggiungi questo codice in fondo al file GameModel.kt

data class TeamStyle(
    val id: String, // Un ID univoco, es. "italy"
    val nationName: String, // Il nome da mostrare, es. "Italia"
    @DrawableRes val flagResId: Int, // L'ID della risorsa immagine in drawable
    val opponentName: String // Il nome da mostrare, es. "it_leonardo"
)

// Creiamo una lista degli stili che la nostra app supporterà
val availableTeamStyles = listOf(
    // Usiamo il tuo dama_icon.png come opzione di default
    TeamStyle("default", "Modalità Classica", R.drawable.dama_icon, "Classico"),

    // Italia
    TeamStyle(id = "it_leonardo", nationName = "it", flagResId = R.drawable.flag_italy, "Leonardo da Vinci"),
    TeamStyle(id = "it_artemisia", nationName = "it", flagResId = R.drawable.flag_italy, "Artemisia Gentileschi"),

    // Regno Unito
    TeamStyle(id = "uk_shakespeare", nationName = "uk", flagResId = R.drawable.flag_uk, "William Shakespeare"),
    TeamStyle(id = "uk_elisabetta_i", nationName = "uk", flagResId = R.drawable.flag_uk, "Regina Elisabetta I"),

    // Germania
    TeamStyle(id = "de_goethe", nationName = "de", flagResId = R.drawable.flag_germany, "Johann W. von Goethe"),
    TeamStyle(id = "de_dietrich", nationName = "de", flagResId = R.drawable.flag_germany, "Marlene Dietrich"),

    // Stati Uniti
    TeamStyle(id = "us_twain", nationName = "us", flagResId = R.drawable.flag_usa, "Mark Twain"),
    TeamStyle(id = "us_franklin", nationName = "us", flagResId = R.drawable.flag_usa, "Aretha Franklin"),

    // Francia
    TeamStyle(id = "fr_napoleon", nationName = "fr", flagResId = R.drawable.flag_france, "Napoleone Bonaparte"),
    TeamStyle(id = "fr_curie", nationName = "fr", flagResId = R.drawable.flag_france, "Marie Curie"),

    // Spagna
    TeamStyle(id = "es_cervantes", nationName = "es", flagResId = R.drawable.flag_spain, "Miguel de Cervantes"),
    TeamStyle(id = "es_isabella_i", nationName = "es", flagResId = R.drawable.flag_spain, "Isabella I di Castiglia")
)

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