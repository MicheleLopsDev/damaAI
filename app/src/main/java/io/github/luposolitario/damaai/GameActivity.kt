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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.luposolitario.damaai.data.*
import io.github.luposolitario.damaai.engine.DamaEngine
import io.github.luposolitario.damaai.engine.DamaEngineImpl
import io.github.luposolitario.damaai.game_logic.Colore
import io.github.luposolitario.damaai.game_logic.Difficolta
import io.github.luposolitario.damaai.game_logic.Posizione
import io.github.luposolitario.damaai.screen.CreditsScreen
import io.github.luposolitario.damaai.screen.CustomizationScreen
import io.github.luposolitario.damaai.screen.GameBoardArea
import io.github.luposolitario.damaai.screen.HelpScreen
import io.github.luposolitario.damaai.screen.SettingsScreen
import io.github.luposolitario.damaai.screen.VictoryScreen
import io.github.luposolitario.damaai.ui.theme.DamaAITheme
import io.github.luposolitario.damaai.viewmodels.SettingsViewModel
import io.github.luposolitario.damaai.viewmodels.SettingsViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

private fun parseBoardState(boardState: String): List<Piece> {
    val pieces = mutableListOf<Piece>()
    val rows = boardState.trim().lines().drop(1)

    if (rows.size != 8) {
        Log.e("ParseError", "Numero di righe non corretto! Trovate ${rows.size}, attese 8.")
        return emptyList()
    }

    rows.forEachIndexed { rowIndex, rowString ->
        for (colIndex in 0..7) {
            val charIndex = 2 + colIndex * 2
            if (charIndex < rowString.length) {
                val symbol = rowString[charIndex]
                val color = when (symbol.lowercaseChar()) {
                    'b' -> PlayerColor.WHITE
                    'n' -> PlayerColor.BLACK
                    else -> null
                }
                if (color != null) {
                    pieces.add(Piece(row = rowIndex, col = colIndex, color = color))
                }
            }
        }
    }
    return pieces
}

// NUOVA funzione per convertire la notazione (es. "A3") in Posizione
fun fromNotazioneAlgebrica(notazione: String): Posizione? {
    if (notazione.length != 2) return null
    val colonnaChar = notazione.getOrNull(0)?.uppercaseChar() ?: return null
    val rigaChar = notazione.getOrNull(1) ?: return null

    if (colonnaChar !in 'A'..'H' || rigaChar !in '1'..'8') return null

    val colonna = colonnaChar - 'A'
    val riga = '8' - rigaChar
    return Posizione(riga, colonna)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navController: NavController,
    playerTeamStyle: TeamStyle,
    boardStyle: BoardStyle
) {
    var gameState by remember { mutableStateOf(GameState(pieces = emptyList())) }
    var selectedPieceCoords by remember { mutableStateOf<Posizione?>(null) }
    var validMoveDestinations by remember { mutableStateOf<List<Posizione>>(emptyList()) }
    var winner by remember { mutableStateOf<Colore?>(null) }
    val damaEngine: DamaEngine = remember { DamaEngineImpl() }
    val coroutineScope = rememberCoroutineScope()
    var chatMessages by remember { mutableStateOf<List<String>>(emptyList()) }

    // Funzione helper per gestire l'animazione della pedina catturata.
    // Si occupa solo dell'animazione, senza modificare lo stato principale del gioco.
    suspend fun handleCaptureAnimation(capturedPieceToAnimate: Piece?) {
        if (capturedPieceToAnimate == null) return

        // Imposta lo stato per far iniziare l'animazione di dissolvenza
        gameState = gameState.copy(capturedPiece = capturedPieceToAnimate)
        delay(800) // Durata dell'animazione

        // Pulisce lo stato per terminare l'animazione, facendo sparire la pedina
        gameState = gameState.copy(capturedPiece = null)
    }

    // Funzione per ottenere le mosse valide per il pezzo attualmente selezionato.
    fun getValidMovesForSelectedPiece() {
        if (selectedPieceCoords == null) {
            validMoveDestinations = emptyList()
            return
        }
        val allValidMovesStr = damaEngine.getMosseValide()
        // Filtra le mosse che partono dalla casella selezionata
        val movesForPiece = allValidMovesStr
            .filter { it.startsWith(selectedPieceCoords!!.toNotazioneAlgebrica()) }
            .mapNotNull { fromNotazioneAlgebrica(it.split(" ")[1]) } // Converte la destinazione in Posizione
        validMoveDestinations = movesForPiece
    }

    // Questo `LaunchedEffect` viene eseguito una sola volta quando il Composable appare sullo schermo.
    // È il posto giusto per inizializzare la partita.
    LaunchedEffect(Unit) {
        damaEngine.nuovaPartita(Difficolta.FACILE)
        Log.d("GameFlow", "--- NUOVA PARTITA ---")
        val initialPieces = parseBoardState(damaEngine.getStatoScacchiera())
        val initialMandatory = damaEngine.getPezziConCatturaObbligatoria()
        gameState = gameState.copy(pieces = initialPieces, mandatoryCapturePieces = initialMandatory)
        Log.d("GameFlow", "Stato iniziale. Catture obbligatorie per: $initialMandatory")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("damaAI") },
                actions = {
                    IconButton(onClick = { navController.navigate("settings_screen") }) {
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
                .padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BoardFiles()
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BoardRanks(modifier = Modifier.width(24.dp))
                    Box(modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)) {
                        GameBoardArea(
                            gameState = gameState,
                            playerTeamStyle = playerTeamStyle,
                            boardStyle = boardStyle,
                            selectedSquare = selectedPieceCoords,
                            validMoveSquares = validMoveDestinations,
                            onSquareClick = { row, col ->
                                val clickedPos = Posizione(row, col)
                                val pieceAtPos = gameState.pieces.find { it.row == row && it.col == col }

                                coroutineScope.launch {
                                    // CASO 1: Un pezzo è già selezionato, quindi l'utente sta cercando di muovere.
                                    if (selectedPieceCoords != null) {
                                        val startNotation = selectedPieceCoords!!.toNotazioneAlgebrica()
                                        val endNotation = clickedPos.toNotazioneAlgebrica()
                                        val moveString = "$startNotation $endNotation"

                                        val mossaUmano = damaEngine.muoviPezzoUmano(moveString)

                                        // Se la mossa è valida, procedi.
                                        if (mossaUmano != null) {
                                            Log.i("GameFlow", "Mossa umana ACCETTATA: $moveString")
                                            chatMessages = chatMessages + "Tua mossa: $moveString"
                                            val pedinaCatturataUmano = damaEngine.trovaPedinaCatturata(mossaUmano)

                                            // Aggiorniamo subito lo stato della scacchiera dopo la mossa umana.
                                            val piecesAfterHumanMove = parseBoardState(damaEngine.getStatoScacchiera())
                                            gameState = gameState.copy(pieces = piecesAfterHumanMove)
                                            handleCaptureAnimation(pedinaCatturataUmano) // Avvia l'animazione della cattura (se c'è).

                                            val winnerAfterHumanMove = damaEngine.getVincitore()
                                            if (winnerAfterHumanMove == null) {
                                                // Ora tocca all'IA.
                                                val mossaIA = damaEngine.faiMossaIA()
                                                if (mossaIA != null) {
                                                    Log.i("GameFlow", "IA risponde con: ${mossaIA.toString()}")
                                                    chatMessages = chatMessages + "Mossa IA: ${mossaIA.toString()}"
                                                    val pedinaCatturataIA = damaEngine.trovaPedinaCatturata(mossaIA)

                                                    // Aggiorniamo lo stato finale, incluse le catture obbligatorie, PRIMA dell'animazione.
                                                    val finalPieces = parseBoardState(damaEngine.getStatoScacchiera())
                                                    val mandatoryForHuman = damaEngine.getPezziConCatturaObbligatoria()
                                                    gameState = gameState.copy(pieces = finalPieces, mandatoryCapturePieces = mandatoryForHuman)
                                                    Log.d("GameFlow", "Stato aggiornato. Catture obbligatorie per umano: $mandatoryForHuman")

                                                    handleCaptureAnimation(pedinaCatturataIA) // Avvia l'animazione della cattura dell'IA.
                                                }
                                            }
                                            damaEngine.getVincitore()?.let {
                                                winner = it // <-- Imposta il vincitore
                                                val winnerMessage = "Partita finita! Vince ${it.name}"
                                                if (!chatMessages.contains(winnerMessage)) {
                                                    chatMessages = chatMessages + winnerMessage
                                                }
                                            }
                                        } else {
                                            Log.w("GameFlow", "Mossa umana RIFIUTATA: $moveString")
                                        }

                                        // Resetta la selezione dopo il tentativo di mossa.
                                        selectedPieceCoords = null
                                        validMoveDestinations = emptyList()

                                        // Controlla il vincitore alla fine del turno.
                                        val winner = damaEngine.getVincitore()
                                        if (winner != null) {
                                            val winnerMessage = "Partita finita! Vince il ${winner.name}"
                                            if (!chatMessages.contains(winnerMessage)) {
                                                chatMessages = chatMessages + winnerMessage
                                            }
                                        }

                                    } else {
                                        // CASO 2: Nessun pezzo selezionato. L'utente sta selezionando un pezzo.
                                        if (pieceAtPos != null && pieceAtPos.color == PlayerColor.WHITE) {
                                            selectedPieceCoords = clickedPos
                                            getValidMovesForSelectedPiece() // Calcola e mostra le mosse valide.
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        winner?.let { vincitore ->
                            VictoryScreen(
                                winner = vincitore,
                                onPlayAgain = {
                                    // Resetta lo stato per una nuova partita
                                    damaEngine.nuovaPartita(Difficolta.FACILE)
                                    val initialPieces = parseBoardState(damaEngine.getStatoScacchiera())
                                    val initialMandatory = damaEngine.getPezziConCatturaObbligatoria()
                                    gameState = gameState.copy(pieces = initialPieces, mandatoryCapturePieces = initialMandatory)
                                    chatMessages = emptyList()
                                    winner = null // Nasconde la schermata di vittoria
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                }
                Spacer(Modifier.height(16.dp))
                AIOpponentHeader(name = "Wialiam Sheaskeper", isThinking = false)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                ChatDisplayArea(messages = chatMessages, modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f))
                ChatInputArea(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun AIOpponentHeader(name: String, isThinking: Boolean, modifier: Modifier = Modifier) {
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
            val statusText = if (isThinking) "Sta pensando..." else "Online"
            val statusColor = if (isThinking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            Text(text = statusText, style = MaterialTheme.typography.bodySmall, color = statusColor)
        }
    }
}

@Composable
fun ChatDisplayArea(messages: List<String>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if(messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages) { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
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
            label = { Text("Chat disabilitata") },
            modifier = Modifier.weight(1f),
            enabled = false
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = { }, enabled = false) {
            Text("Invia")
        }
    }
}

@Composable
private fun BoardFiles(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp),
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