package io.github.luposolitario.damaai

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.luposolitario.damaai.data.*
import io.github.luposolitario.damaai.engine.GemmaEngine
import io.github.luposolitario.damaai.game_logic.Posizione
import io.github.luposolitario.damaai.screen.CreditsScreen
import io.github.luposolitario.damaai.screen.CustomizationScreen
import io.github.luposolitario.damaai.screen.GameBoardArea
import io.github.luposolitario.damaai.screen.HelpScreen
import io.github.luposolitario.damaai.screen.SettingsScreen
import io.github.luposolitario.damaai.screen.captureViewAsBitmap
import io.github.luposolitario.damaai.ui.theme.DamaAITheme
import io.github.luposolitario.damaai.utils.formatTime
import io.github.luposolitario.damaai.utils.isValidMove
import io.github.luposolitario.damaai.viewmodels.SettingsViewModel
import io.github.luposolitario.damaai.viewmodels.SettingsViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


val initialPieces: List<Piece> = listOf(
    Piece(0, 1, PlayerColor.BLACK), Piece(0, 3, PlayerColor.BLACK), Piece(0, 5, PlayerColor.BLACK), Piece(0, 7, PlayerColor.BLACK),
    Piece(1, 0, PlayerColor.BLACK), Piece(1, 2, PlayerColor.BLACK), Piece(1, 4, PlayerColor.BLACK), Piece(1, 6, PlayerColor.BLACK),
    Piece(2, 1, PlayerColor.BLACK), Piece(2, 3, PlayerColor.BLACK), Piece(2, 5, PlayerColor.BLACK), Piece(2, 7, PlayerColor.BLACK),
    Piece(5, 0, PlayerColor.WHITE), Piece(5, 2, PlayerColor.WHITE), Piece(5, 4, PlayerColor.WHITE), Piece(5, 6, PlayerColor.WHITE),
    Piece(6, 1, PlayerColor.WHITE), Piece(6, 3, PlayerColor.WHITE), Piece(6, 5, PlayerColor.WHITE), Piece(6, 7, PlayerColor.WHITE),
    Piece(7, 0, PlayerColor.WHITE), Piece(7, 2, PlayerColor.WHITE), Piece(7, 4, PlayerColor.WHITE), Piece(7, 6, PlayerColor.WHITE),
)

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val application = application as DamaAIApplication
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(application.settingsManager)
            )
            val useDarkTheme by settingsViewModel.isDarkModeEnabled.collectAsState(initial = isSystemInDarkTheme())

            DamaAITheme(darkTheme = useDarkTheme) {
                AppNavigation(settingsViewModel = settingsViewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val playerStyleId by settingsViewModel.playerTeamStyleId.collectAsState()
    val playerTeamStyle = availableTeamStyles.find { it.id == playerStyleId } ?: availableTeamStyles.first()
    val boardStyleId by settingsViewModel.boardStyleId.collectAsState()
    val boardStyle = availableBoardStyles.find { it.id == boardStyleId } ?: availableBoardStyles.first()

    NavHost(navController = navController, startDestination = "game_screen") {
        composable(route = "game_screen") {
            GameScreen(
                navController = navController,
                playerTeamStyle = playerTeamStyle,
                boardStyle = boardStyle
            )
        }
        composable(route = "settings_screen") {
            SettingsScreen(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }
        composable(route = "help_screen") {
            HelpScreen(navController = navController)
        }
        composable(route = "credits_screen") {
            CreditsScreen(navController = navController)
        }
        composable(route = "customization_screen") {
            CustomizationScreen(navController = navController)
        }
    }
}

/**
 * Converte la lista di pedine della UI in una rappresentazione testuale
 * simile a quella del motore di gioco, per passarla a Gemma.
 */
private fun convertPiecesToString(pieces: List<Piece>): String {
    val board = Array(8) { Array<String?>(8) { null } }
    pieces.forEach { piece ->
        val symbol = when (piece.color) {
            PlayerColor.WHITE -> "b" // "b" per bianco (bottom)
            PlayerColor.BLACK -> "n" // "n" per nero (north)
        }
        board[piece.row][piece.col] = symbol
    }

    val builder = StringBuilder()
    builder.append("  A B C D E F G H\n")
    for (riga in 0..7) {
        builder.append("${riga + 1} ")
        for (colonna in 0..7) {
            val pezzo = board[riga][colonna]
            val simbolo = pezzo ?: if ((riga + colonna) % 2 != 0) "." else " "
            builder.append("$simbolo ")
        }
        builder.append("\n")
    }
    return builder.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navController: NavController,
    playerTeamStyle: TeamStyle,
    boardStyle: BoardStyle
) {
    var gameState by remember { mutableStateOf(GameState(pieces = initialPieces)) }
    var boardCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val view = LocalView.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var chatMessages by remember { mutableStateOf(listOf<String>()) }
    var isAiThinking by remember { mutableStateOf(false) }

    // --- NUOVO: Istanza del motore e gestione del suo ciclo di vita ---
    val gemmaEngine = remember { GemmaEngine() }

    LaunchedEffect(Unit) {
        // Carica il modello quando il Composable entra nella composizione
        try {
            val modelFile = File(context.filesDir, "gemma-3n-E4B-it-int4.task")
            gemmaEngine.load(context, modelFile.absolutePath)
        } catch (e: Exception) {
            Log.e("GameScreen", "Errore durante il caricamento del modello IA: ${e.message}")
            // Potresti mostrare un messaggio all'utente qui
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Rilascia le risorse quando il Composable viene rimosso
            coroutineScope.launch {
                gemmaEngine.unload()
            }
        }
    }

    LaunchedEffect(key1 = gameState.currentPlayer) {
        while (true) {
            delay(1000L)
            gameState = gameState.copy(turnElapsedTimeInSeconds = gameState.turnElapsedTimeInSeconds + 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("damaAI") },
                actions = {


                    // NUOVO CODICE PER L'ICONBUTTON
                    IconButton(onClick = {
                        if (isAiThinking) return@IconButton

                        // Controlliamo che le coordinate della scacchiera siano state registrate
                        boardCoordinates?.let { coords ->
                            // Calcoliamo il rettangolo esatto da catturare
                            val rectInWindow = Rect(
                                offset = coords.positionInWindow(),
                                size = coords.size.toSize()
                            )

                            // Avviamo la logica di cattura e analisi
                            captureViewAsBitmap(view, rectInWindow) { bitmap ->
                                if (bitmap != null) {
                                    coroutineScope.launch {
                                        isAiThinking = true
                                        val analysisPrompt = "L'immagine che stai vedendo contiene una scacchiera di dama , ci sono delle etichette numeriche a sinistra " +
                                                "e delle etichette alfanumeriche in alto leggile ed elencale, incrocia righe e colonne e dimmi se c'è una pedina e di che colore è "

                                        try {
                                            val fullResponse = StringBuilder()
                                            // --- USIAMO LA FUNZIONE CORRETTA CON IL BITMAP ---
//                                            gemmaEngine.generateMove(analysisPrompt, bitmap)
//                                                .collect { responseChunk ->
//                                                    fullResponse.append(responseChunk)
//                                                }

                                            if (fullResponse.isNotBlank()) {
                                                chatMessages = chatMessages + fullResponse.toString()
                                            }
                                            Log.d("GameScreen", chatMessages.toString())
                                        } catch (e: Exception) {
                                            Log.e("GameScreen", "Errore durante l'analisi di Gemma: ${e.message}", e)
                                            chatMessages = chatMessages + "Errore del motore IA: ${e.message}"
                                        } finally {
                                            isAiThinking = false
                                        }
                                    }
                                } else {
                                    Log.e("Capture", "Impossibile catturare il bitmap della scacchiera.")
                                    chatMessages = chatMessages + "Errore: Impossibile analizzare la scacchiera."
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Analizza Partita con IA")
                    }


                    IconButton(onClick = {
                        navController.navigate("settings_screen")
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Impostazioni")
                    }
                },
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
                .padding(horizontal = 8.dp, vertical = 16.dp), // Leggera modifica al padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Contenitore per la scacchiera e le etichette
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Assegna più spazio possibile alla scacchiera
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BoardFiles() // Lettere in alto
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BoardRanks(modifier = Modifier.width(24.dp)) // Numeri a sinistra

                    Box(
                        modifier = Modifier
                            .weight(1f) // La scacchiera occupa lo spazio rimanente
                            .aspectRatio(1f)
                            .onGloballyPositioned { coordinates ->
                                boardCoordinates = coordinates
                            }
                    ) {
                        GameBoardArea(
                            gameState = gameState,
                            playerTeamStyle = playerTeamStyle,
                            boardStyle = boardStyle,
                            onSquareClick = { row, col ->
                                val selected = gameState.selectedPiece
                                if (selected != null) {
                                    if (isValidMove(selected, row, col, gameState.pieces)) {
                                        val newPieces = gameState.pieces.map {
                                            if (it == selected) it.copy(row = row, col = col) else it
                                        }
                                        val startPos = Posizione(selected.row, selected.col).toNotazioneAlgebrica()
                                        val endPos = Posizione(row, col).toNotazioneAlgebrica()
                                        chatMessages = chatMessages + "Mossa: ${startPos} -> ${endPos}"
                                        gameState = gameState.copy(
                                            pieces = newPieces,
                                            selectedPiece = null,
                                            currentPlayer = if (gameState.currentPlayer == PlayerColor.WHITE) PlayerColor.BLACK else PlayerColor.WHITE,
                                            turnElapsedTimeInSeconds = 0L
                                        )
                                    } else {
                                        gameState = gameState.copy(selectedPiece = null)
                                    }
                                } else {
                                    val clickedPiece =
                                        gameState.pieces.find { it.row == row && it.col == col }
                                    if (clickedPiece != null && clickedPiece.color == gameState.currentPlayer) {
                                        gameState = gameState.copy(selectedPiece = clickedPiece)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Abbiamo sostituito i numeri a destra con uno spacer
                    // per mantenere la scacchiera centrata.
                    Spacer(modifier = Modifier.width(24.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))

            Spacer(Modifier.height(16.dp))
            AIOpponentHeader(
                name = "Wialiam Sheaskeper",
                isThinking = isAiThinking // Passiamo lo stato di "pensiero"
            )
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            // NUOVO ChatDisplayArea
            ChatDisplayArea(
                messages = chatMessages, // Passiamo la lista dei messaggi
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            ChatInputArea(modifier = Modifier.fillMaxWidth())
        }


        }
    }
}

@Composable
fun AIOpponentHeader(name: String, isThinking: Boolean, modifier: Modifier = Modifier) { // Aggiunto isThinking
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar dell'avversario AI",
            modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            // Mostra "Sta pensando..." o "Online" in base allo stato
            val statusText = if (isThinking) "Sta pensando..." else "Online"
            val statusColor = if (isThinking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            Text(text = statusText, style = MaterialTheme.typography.bodySmall, color = statusColor)
        }
    }
}

@Composable
fun ChatDisplayArea(messages: List<String>, modifier: Modifier = Modifier) { // Aggiunto messages
    LazyColumn( // Usiamo LazyColumn per mostrare una lista di messaggi scorrevole
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (messages.isEmpty()) {
            item {
                Text(
                    text = "Tocca l'icona 📷 in alto per chiedere un'analisi della partita all'IA.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(messages) { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ChatInputArea(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = { },
            label = { Text("Scrivi un messaggio...") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = { }) {
            Text("Invia")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DamaAITheme {
        GameScreen(
            navController = rememberNavController(),
            playerTeamStyle = availableTeamStyles.first(),
            boardStyle = availableBoardStyles.first()
        )
    }
}

/**
 * Mostra le etichette delle colonne (A-H) sopra e sotto la scacchiera.
 */
@Composable
private fun BoardFiles(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp), // Aggiungiamo un padding per allineare le lettere con la scacchiera
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ('A'..'H').forEach { file ->
            Text(
                text = file.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Mostra le etichette delle righe (8-1) ai lati della scacchiera.
 */
@Composable
private fun BoardRanks(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        (8 downTo 1).forEach { rank ->
            Text(
                text = rank.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}