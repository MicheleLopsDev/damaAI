package io.github.luposolitario.damaai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController // <-- NUOVO IMPORT
import androidx.navigation.compose.NavHost // <-- NUOVO IMPORT
import androidx.navigation.compose.composable // <-- NUOVO IMPORT
import androidx.navigation.compose.rememberNavController // <-- NUOVO IMPORT
import io.github.luposolitario.damaai.data.GameState
import io.github.luposolitario.damaai.data.Piece
import io.github.luposolitario.damaai.data.PlayerColor
import io.github.luposolitario.damaai.ui.theme.DamaAITheme
import io.github.luposolitario.damaai.utils.formatTime
import io.github.luposolitario.damaai.utils.isValidMove
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowBack
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DamaAITheme {
                // --- NUOVO: Chiamiamo il nostro gestore di navigazione ---
                AppNavigation()
            }
        }
    }
}

// --- NUOVO: Definiamo la posizione iniziale di tutte le pedine ---
val initialPieces: List<Piece> = listOf(
    // Pedine Nere
    Piece(0, 1, PlayerColor.BLACK), Piece(0, 3, PlayerColor.BLACK), Piece(0, 5, PlayerColor.BLACK), Piece(0, 7, PlayerColor.BLACK),
    Piece(1, 0, PlayerColor.BLACK), Piece(1, 2, PlayerColor.BLACK), Piece(1, 4, PlayerColor.BLACK), Piece(1, 6, PlayerColor.BLACK),
    Piece(2, 1, PlayerColor.BLACK), Piece(2, 3, PlayerColor.BLACK), Piece(2, 5, PlayerColor.BLACK), Piece(2, 7, PlayerColor.BLACK),
    // Pedine Bianche
    Piece(5, 0, PlayerColor.WHITE), Piece(5, 2, PlayerColor.WHITE), Piece(5, 4, PlayerColor.WHITE), Piece(5, 6, PlayerColor.WHITE),
    Piece(6, 1, PlayerColor.WHITE), Piece(6, 3, PlayerColor.WHITE), Piece(6, 5, PlayerColor.WHITE), Piece(6, 7, PlayerColor.WHITE),
    Piece(7, 0, PlayerColor.WHITE), Piece(7, 2, PlayerColor.WHITE), Piece(7, 4, PlayerColor.WHITE), Piece(7, 6, PlayerColor.WHITE),
)
// --- FINE PARTE NUOVA ---


/**
 * RINOMINATA: DamaAIScreen -> GameScreen per maggiore chiarezza.
 * Ora accetta un NavController per poter navigare verso altre schermate.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: NavController) { // <-- NUOVO PARAMETRO
    var gameState by remember {
        mutableStateOf(GameState(pieces = initialPieces))
    }
    // --- NUOVO: Effetto per gestire il timer ---
    // Questo blocco di codice lancia una coroutine. Viene eseguito ogni volta
    // che la sua "chiave" (key1) cambia. Usiamo gameState.currentPlayer come chiave.
    LaunchedEffect(key1 = gameState.currentPlayer) {
        // Quando il turno cambia, la vecchia coroutine viene cancellata e ne parte una nuova.
        while (true) {
            delay(1000L) // Aspetta 1 secondo (è una funzione di coroutine)
            // Aggiorna lo stato incrementando il tempo trascorso
            gameState = gameState.copy(
                turnElapsedTimeInSeconds = gameState.turnElapsedTimeInSeconds + 1
            )
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("damaAI") },
                // --- NUOVO: Aggiungiamo un pulsante per andare alle impostazioni ---
                actions = {
                    IconButton(onClick = {
                        // Usiamo il navController per navigare alla schermata delle impostazioni
                        navController.navigate("settings_screen")
                    }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Impostazioni")
                    }
                },
                // --- FINE PARTE NUOVA ---
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GameBoardArea(
                gameState = gameState,
                // --- QUESTA È LA LOGICA COMPLETAMENTE AGGIORNATA ---
                onSquareClick = { row, col ->
                    val selected = gameState.selectedPiece

                    if (selected != null) {
                        // Se una pedina è già selezionata, proviamo a muoverla.
                        // ... dentro la logica onSquareClick
                        if (isValidMove(selected, row, col, gameState.pieces)) {
                            // Mossa valida! Aggiorniamo lo stato del gioco.
                            val newPieces = gameState.pieces.map {
                                if (it == selected) it.copy(row = row, col = col) else it
                            }
                            gameState = gameState.copy(
                                pieces = newPieces,
                                selectedPiece = null,
                                currentPlayer = if (gameState.currentPlayer == PlayerColor.WHITE) PlayerColor.BLACK else PlayerColor.WHITE,
                                turnElapsedTimeInSeconds = 0L // <-- NUOVO: Resetta il timer a 0!
                            )
                        } else {
                            // Mossa non valida: deselezioniamo.
                            gameState = gameState.copy(selectedPiece = null)
                        }
                    } else {
                        // Se nessuna pedina è selezionata, proviamo a selezionarne una.
                        val clickedPiece = gameState.pieces.find { it.row == row && it.col == col }

                        // NUOVO CONTROLLO: la pedina cliccata deve essere del giocatore corrente!
                        if (clickedPiece != null && clickedPiece.color == gameState.currentPlayer) {
                            gameState = gameState.copy(selectedPiece = clickedPiece)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .aspectRatio(1f)
            )

            // Sostituisci il vecchio Composable Text con questa Row
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Tocca a: ${gameState.currentPlayer}",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    // Usiamo una funzione per formattare i secondi in MM:SS
                    text = "⏳ ${formatTime(gameState.turnElapsedTimeInSeconds)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            // ... (il resto della Column è invariato) ...
            ChatDisplayArea(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            ChatInputArea(
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * NUOVA FUNZIONE: Un segnaposto per la nostra futura schermata delle impostazioni
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impostazioni") },
                navigationIcon = {
                    // Aggiungiamo un'icona per tornare indietro
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Qui ci saranno le impostazioni!")
        }
    }
}

// Sostituisci la vecchia GameBoardArea con questa
@Composable
fun GameBoardArea(
    gameState: GameState,
    onSquareClick: (row: Int, col: Int) -> Unit, // <-- NUOVO PARAMETRO LAMBDA
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            // --- NUOVO: Rileviamo il tocco dell'utente ---
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val squareSize = size.width / 8f
                    // Convertiamo le coordinate del tocco (offset) in riga e colonna
                    val row = (offset.y / squareSize).toInt()
                    val col = (offset.x / squareSize).toInt()
                    // Notifichiamo il click al componente genitore
                    onSquareClick(row, col)
                }
            }
        // --- FINE NUOVA PARTE ---
    ) {
        val squareSize = size.width / 8f

        // Disegno scacchiera (invariato)
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                // ... (codice disegno scacchiera è identico) ...
                val isLightSquare = (row + col) % 2 == 0
                val squareColor = if (isLightSquare) Color(0xFFF0D9B5) else Color(0xFFB58863)
                drawRect(
                    color = squareColor,
                    topLeft = Offset(x = col * squareSize, y = row * squareSize),
                    size = Size(width = squareSize, height = squareSize)
                )
            }
        }

        // Disegno pedine (con modifica per l'evidenziazione)
        gameState.pieces.forEach { piece ->
            // --- NUOVO: Disegniamo l'evidenziazione se la pedina è selezionata ---
            if (piece == gameState.selectedPiece) {
                drawCircle(
                    color = Color.Yellow.copy(alpha = 0.5f), // Colore dell'evidenziazione
                    radius = squareSize / 2, // Grande quanto la casella
                    center = Offset(
                        x = piece.col * squareSize + squareSize / 2,
                        y = piece.row * squareSize + squareSize / 2
                    )
                )
            }
            // --- FINE NUOVA PARTE ---

            // ... (il codice per disegnare le pedine e la loro ombra rimane identico a prima) ...
            val pieceRadius = squareSize * 0.38f
            val center = Offset(
                x = piece.col * squareSize + squareSize / 2,
                y = piece.row * squareSize + squareSize / 2
            )
            drawCircle(color = Color.Black.copy(alpha = 0.3f), radius = pieceRadius, center = center.copy(y = center.y + 4f))
            drawCircle(color = if (piece.color == PlayerColor.WHITE) Color(0xFFFFFFFF) else Color(0xFF222222), radius = pieceRadius, center = center)
            drawCircle(color = if (piece.color == PlayerColor.WHITE) Color(0xFFBBBBBB) else Color.Black, radius = pieceRadius, center = center, style = Stroke(width = squareSize * 0.04f))
        }
    }
}
/**
 * 1.3.2: Segnaposto per l'Area Visualizzazione Chat (invariato)
 */
@Composable
fun ChatDisplayArea(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Text("I messaggi della chat appariranno qui...")
    }
}

/**
 * 1.3.3: Segnaposto per l'Area di Input della Chat (invariato)
 */
@Composable
fun ChatInputArea(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = { /* Non fa nulla per ora */ },
            label = { Text("Scrivi un messaggio...") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = { /* Non fa nulla per ora */ }) {
            Text("Invia")
        }
    }
}


// ... (Le altre funzioni Composable rimangono invariate) ...

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DamaAITheme {
        // Per l'anteprima, mostriamo direttamente la nostra GameScreen.
        // Le passiamo un NavController "finto" che non fa nulla,
        // serve solo per far compilare l'anteprima.
        GameScreen(navController = rememberNavController())
    }
}

/**
 * NUOVA FUNZIONE: Il cuore della nostra navigazione
 */
@Composable
fun AppNavigation() {
    // 1. Crea un NavController: è il "cervello" che gestisce lo stack delle schermate.
    val navController = rememberNavController()

    // 2. NavHost è il contenitore che mostra la schermata corrente.
    NavHost(navController = navController, startDestination = "game_screen") {
        // 3. Definiamo le nostre "rotte" (le schermate)
        composable(route = "game_screen") {
            // Quando la rotta è "game_screen", mostra la nostra schermata di gioco.
            GameScreen(navController = navController)
        }
        composable(route = "settings_screen") {
            // Quando la rotta è "settings_screen", mostra la schermata delle impostazioni.
            SettingsScreen(navController = navController)
        }
    }
}