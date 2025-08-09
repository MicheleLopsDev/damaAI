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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.luposolitario.damaai.data.*
import io.github.luposolitario.damaai.engine.DamaEngine
import io.github.luposolitario.damaai.engine.DamaEngineImpl
import io.github.luposolitario.damaai.engine.GemmaEngine
import io.github.luposolitario.damaai.game_logic.Colore
import io.github.luposolitario.damaai.game_logic.Difficolta
import io.github.luposolitario.damaai.game_logic.Posizione
import io.github.luposolitario.damaai.screen.*
import io.github.luposolitario.damaai.ui.theme.DamaAITheme
import io.github.luposolitario.damaai.viewmodels.SettingsViewModel
import io.github.luposolitario.damaai.viewmodels.SettingsViewModelFactory
import io.github.luposolitario.lonewolfredux.datastore.ModelSettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onCompletion
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
                    val isDama = symbol.isUpperCase()
                    pieces.add(Piece(row = rowIndex, col = colIndex, color = color, isDama = isDama))
                }
            }
        }
    }
    return pieces
}

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
    var chatMessages by remember { mutableStateOf<List<String>>(emptyList()) }
    var finalAiComment by remember { mutableStateOf<String?>(null) }
    var isAiThinking by remember { mutableStateOf(false) }

    val damaEngine: DamaEngine = remember { DamaEngineImpl() }
    val gemmaEngine = remember { GemmaEngine() }

    val aiOpponent: AiOpponent? = remember(playerTeamStyle.id) {
        availableOpponents.find { it.teamStyleId == playerTeamStyle.id }
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalView.current.context

    fun generateComment(prompt: String, onResult: (String) -> Unit) {
        aiOpponent ?: return
        isAiThinking = true
        coroutineScope.launch {
            val fullPrompt = "${aiOpponent.chatStylePrompt}\n\n$prompt"
            val responseBuilder = StringBuilder()
            try {
                gemmaEngine.generateMove(fullPrompt, damaEngine.getStatoScacchiera())
                    .onCompletion {
                        isAiThinking = false
                        if (responseBuilder.isNotEmpty()) {
                            onResult(responseBuilder.toString())
                        }
                    }
                    .collect { partialResponse -> responseBuilder.append(partialResponse) }
            } catch (e: Exception) {
                Log.e("GemmaIntegration", "Errore durante la generazione della risposta", e)
                isAiThinking = false
            }
        }
    }

    suspend fun handleCaptureAnimation(capturedPieceToAnimate: Piece?) {
        if (capturedPieceToAnimate == null) return
        gameState = gameState.copy(capturedPiece = capturedPieceToAnimate)
        delay(800)
        gameState = gameState.copy(capturedPiece = null)
    }

    fun getValidMovesForSelectedPiece() {
        if (selectedPieceCoords == null) {
            validMoveDestinations = emptyList()
            return
        }
        val allValidMovesStr = damaEngine.getMosseValide()
        val movesForPiece = allValidMovesStr
            .filter { it.startsWith(selectedPieceCoords!!.toNotazioneAlgebrica()) }
            .mapNotNull { fromNotazioneAlgebrica(it.split(" ")[1]) }
        validMoveDestinations = movesForPiece
    }

    LaunchedEffect(aiOpponent) {
        chatMessages = emptyList()
        winner = null
        finalAiComment = null
        damaEngine.nuovaPartita(Difficolta.FACILE)
        val initialPieces = parseBoardState(damaEngine.getStatoScacchiera())
        val initialMandatory = damaEngine.getPezziConCatturaObbligatoria()
        gameState = gameState.copy(pieces = initialPieces, mandatoryCapturePieces = initialMandatory)

        if (aiOpponent != null) {
            try {
                val modelPath = ModelSettingsManager.getDmModelFilePath(context)
                if (modelPath.isNotBlank()) {
                    gemmaEngine.load(context, modelPath)
                    generateComment(aiOpponent.openingPrompt) { comment ->
                        chatMessages = chatMessages + "${aiOpponent.name}: \"$comment\""
                    }
                } else {
                    chatMessages = chatMessages + "ERRORE: Modello IA non trovato. Scaricalo da 'Gestione IA'."
                }
            } catch (e: Exception) {
                Log.e("GemmaIntegration", "Errore caricamento modello Gemma", e)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            coroutineScope.launch { gemmaEngine.unload() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("damaAI") },
                actions = {
                    IconButton(onClick = { navController.navigate("settings_screen") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Impostazioni")
                    }
                }
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
                                    if (selectedPieceCoords != null) {
                                        val startNotation = selectedPieceCoords!!.toNotazioneAlgebrica()
                                        val endNotation = clickedPos.toNotazioneAlgebrica()
                                        val moveString = "$startNotation $endNotation"
                                        val mossaUmano = damaEngine.muoviPezzoUmano(moveString)

                                        if (mossaUmano != null) {
                                            chatMessages = chatMessages + "Tua mossa: $moveString"
                                            val pedinaCatturataUmano = damaEngine.trovaPedinaCatturata(mossaUmano)
                                            val piecesAfterHumanMove = parseBoardState(damaEngine.getStatoScacchiera())
                                            gameState = gameState.copy(pieces = piecesAfterHumanMove)
                                            handleCaptureAnimation(pedinaCatturataUmano)

                                            if (aiOpponent != null) {
                                                val winnerAfterHumanMove = damaEngine.getVincitore()
                                                if (winnerAfterHumanMove == null) {
                                                    val mossaIA = damaEngine.faiMossaIA()
                                                    if (mossaIA != null) {
                                                        chatMessages = chatMessages + "Mossa IA: ${mossaIA.toString()}"
                                                        val pedinaCatturataIA = damaEngine.trovaPedinaCatturata(mossaIA)
                                                        if (pedinaCatturataIA != null) {
                                                            generateComment(aiOpponent.capturePrompt) { comment ->
                                                                chatMessages = chatMessages + "${aiOpponent.name}: \"$comment\""
                                                            }
                                                        }
                                                        val finalPieces = parseBoardState(damaEngine.getStatoScacchiera())
                                                        val mandatoryForHuman = damaEngine.getPezziConCatturaObbligatoria()
                                                        gameState = gameState.copy(pieces = finalPieces, mandatoryCapturePieces = mandatoryForHuman)
                                                        handleCaptureAnimation(pedinaCatturataIA)
                                                    }
                                                }
                                            }
                                        }

                                        selectedPieceCoords = null
                                        validMoveDestinations = emptyList()

                                        damaEngine.getVincitore()?.let { vincitore ->
                                            winner = vincitore
                                            if (aiOpponent != null) {
                                                val finalPrompt = if (vincitore == Colore.NERO) aiOpponent.victoryPrompt else aiOpponent.defeatPrompt
                                                generateComment(finalPrompt) { comment ->
                                                    finalAiComment = comment
                                                }
                                            }
                                        }

                                    } else {
                                        if (pieceAtPos != null && pieceAtPos.color == PlayerColor.WHITE) {
                                            selectedPieceCoords = clickedPos
                                            getValidMovesForSelectedPiece()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        winner?.let { vincitore ->
                            VictoryScreen(
                                winner = vincitore,
                                opponentName = aiOpponent?.name,
                                finalComment = finalAiComment,
                                onPlayAgain = {
                                    winner = null
                                    finalAiComment = null
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                }
                Spacer(Modifier.height(16.dp))
                AIOpponentHeader(
                    name = aiOpponent?.name ?: "Partita Classica",
                    isThinking = isAiThinking
                )
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                if (aiOpponent != null) {
                    ChatDisplayArea(messages = chatMessages, modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f))
                    ChatInputArea(modifier = Modifier.fillMaxWidth())
                } else {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Modalità Classica.\nSeleziona una nazione nelle impostazioni per sfidare un avversario.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ---- FUNZIONI DI SUPPORTO PER LA UI ----

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
        if (messages.isNotEmpty()) {
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