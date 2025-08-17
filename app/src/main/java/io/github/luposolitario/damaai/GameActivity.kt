package io.github.luposolitario.damaai

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.luposolitario.damaai.data.*
import io.github.luposolitario.damaai.datastore.ModelSettingsManager
import io.github.luposolitario.damaai.engine.DamaEngine
import io.github.luposolitario.damaai.engine.DamaEngineImpl
import io.github.luposolitario.damaai.engine.GemmaEngine
import io.github.luposolitario.damaai.game_logic.Colore
import io.github.luposolitario.damaai.game_logic.Difficolta
import io.github.luposolitario.damaai.game_logic.Posizione
import io.github.luposolitario.damaai.media.MusicManager
import io.github.luposolitario.damaai.media.TtsManager
import io.github.luposolitario.damaai.screen.*
import io.github.luposolitario.damaai.ui.screen.HelpScreen
import io.github.luposolitario.damaai.ui.screen.OptionsScreen
import io.github.luposolitario.damaai.ui.theme.*
import io.github.luposolitario.damaai.utils.getTrackIdByName
import io.github.luposolitario.damaai.viewmodels.SettingsViewModel
import io.github.luposolitario.damaai.viewmodels.SettingsViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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

private fun saveBitmapAndGetUri(context: android.content.Context, bitmap: Bitmap): Uri? {
    val imagesFolder = File(context.cacheDir, "images")
    return try {
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "shared_image.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        stream.flush()
        stream.close()
        FileProvider.getUriForFile(context, context.packageName + ".provider", file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun AppNavigation(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val playerStyleId by settingsViewModel.playerTeamStyleId.collectAsState()
    val playerTeamStyle = availableTeamStyles.find { it.id == playerStyleId } ?: availableTeamStyles.first()
    val boardStyleId by settingsViewModel.boardStyleId.collectAsState()
    val boardStyle = availableBoardStyles.find { it.id == boardStyleId } ?: availableBoardStyles.first()
    val selectedStyleId by settingsViewModel.playerTeamStyleId.collectAsState()

    NavHost(navController = navController, startDestination = "game_screen") {
        composable(route = "game_screen") {
            if (selectedStyleId != null) {
                GameScreen(
                    navController = navController,
                    playerTeamStyle = playerTeamStyle,
                    boardStyle = boardStyle,
                    settingsViewModel = settingsViewModel
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        composable(route = "settings_screen") {
            SettingsScreen(
                navController = navController
            )
        }
        composable(route = "options_screen") {
            OptionsScreen(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }
        composable(route = "help_screen") {
            HelpScreen()
        }
        composable(route = "credits_screen") {
            CreditsScreen()
        }
        composable(route = "customization_screen") {
            CustomizationScreen()
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
    boardStyle: BoardStyle,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalView.current.context
    val view = LocalView.current

    val musicVolume by settingsViewModel.musicVolume.collectAsState()
    val classicSongId by settingsViewModel.classicSongId.collectAsState()
    val teamStyleId by settingsViewModel.playerTeamStyleId.collectAsState()

    var gameState by remember { mutableStateOf(GameState(pieces = emptyList())) }
    var selectedPieceCoords by remember { mutableStateOf<Posizione?>(null) }
    var validMoveDestinations by remember { mutableStateOf<List<Posizione>>(emptyList()) }
    var winner by remember { mutableStateOf<Colore?>(null) }
    var chatMessages by remember { mutableStateOf<List<String>>(emptyList()) }
    var llmChatMessages by remember { mutableStateOf<List<String>>(emptyList()) }
    var finalAiComment by remember { mutableStateOf<String?>(null) }
    var isAiThinking by remember { mutableStateOf(false) }
    var turnoCorrente by remember { mutableStateOf(Colore.BIANCO) }
    var boardCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var mossaTestuale by remember { mutableStateOf("") }

    val damaEngine: DamaEngine = remember { DamaEngineImpl() }
    val gemmaEngine = remember { GemmaEngine() }

    val selectedDifficulty by settingsViewModel.difficultyLevel.collectAsState()

    val aiOpponent: AiOpponent? = remember(playerTeamStyle.id) {
        availableOpponents.find {
            it.id == playerTeamStyle.id
        }
    }

    val aiTeamStyle: TeamStyle? = remember(aiOpponent) {
        aiOpponent?.let { opponent ->
            availableTeamStyles.find {
                it.nationName == opponent.teamStyleId
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    fun generateComment(prompt: String) {
        aiOpponent ?: return
        isAiThinking = true

        val initialMessage = "${aiOpponent.name}: \"\""
        Log.d("LlmChatLog", "Adding initial placeholder: $initialMessage")
        llmChatMessages = llmChatMessages + initialMessage

        coroutineScope.launch {
            val fullPrompt = "${aiOpponent.chatStylePrompt}\n\n$prompt"

            try {
                gemmaEngine.generateMove(fullPrompt, damaEngine.getStatoScacchiera())
                    .onCompletion {
                        isAiThinking = false
                        if (llmChatMessages.isNotEmpty()) {
                            Log.d("LlmChatLog", "Generation complete. Final message: ${llmChatMessages.last()}")
                            // ==== AGGIUNTA QUI ====
                            // Pulisce il nome del bot e fa parlare il TTS con il messaggio completo
                            val textToSpeak = llmChatMessages.last().substringAfter(":").trim().replace("\"", "")
                            TtsManager.speak(textToSpeak)
                        }
                    }
                    .collect { partialResponse ->
                        val lastMessageIndex = llmChatMessages.lastIndex
                        if (lastMessageIndex >= 0) {
                            val updatedMessage = llmChatMessages[lastMessageIndex] + partialResponse
                            llmChatMessages = llmChatMessages.toMutableList().also { it[lastMessageIndex] = updatedMessage }
                        }
                    }
            } catch (e: Exception) {
                Log.e("GemmaIntegration", "Errore durante la generazione della risposta", e)
                isAiThinking = false
                llmChatMessages = llmChatMessages.dropLast(1)
            }
        }
    }

    suspend fun handleCaptureAnimation(capturedPieceToAnimate: Piece?) {
        if (capturedPieceToAnimate == null) return
        gameState = gameState.copy(capturedPiece = capturedPieceToAnimate)
        delay(800)
        gameState = gameState.copy(capturedPiece = null)
    }

    fun eseguiMossaTestuale() {
        if (mossaTestuale.isNotBlank()) {
            coroutineScope.launch {
                val mossaEseguita = damaEngine.muoviPezzoUmano(mossaTestuale)
                if (mossaEseguita != null) {
                    val playerColorText = if (turnoCorrente == Colore.BIANCO) "Bianco" else "Nero"
                    val logMessage = "Mossa $playerColorText: $mossaTestuale"
                    Log.d("ChatLog", "Adding move log: $logMessage")
                    chatMessages = chatMessages + logMessage

                    val pedinaCatturata = damaEngine.trovaPedinaCatturata(mossaEseguita)
                    val piecesAfterMove = parseBoardState(damaEngine.getStatoScacchiera())
                    gameState = gameState.copy(pieces = piecesAfterMove)
                    handleCaptureAnimation(pedinaCatturata)

                    turnoCorrente = damaEngine.getTurnoCorrente()
                    gameState = gameState.copy(mandatoryCapturePieces = damaEngine.getPezziConCatturaObbligatoria())

                    mossaTestuale = ""

                    damaEngine.getVincitore()?.let { vincitore ->
                        winner = vincitore
                    }
                } else {
                    val logMessage = "Mossa non valida: $mossaTestuale"
                    Log.d("ChatLog", "Adding invalid move log: $logMessage")
                    chatMessages = chatMessages + logMessage
                }
            }
        }
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

    LaunchedEffect(teamStyleId, classicSongId) {
        MusicManager.setVolume(musicVolume)
        val songToPlayId = if (teamStyleId == "default") {
            classicSongId
        } else {
            teamStyleId
        }

        getTrackIdByName(songToPlayId)?.let { trackId ->
            MusicManager.setVolume(musicVolume)
            MusicManager.play(context, trackId)
        }
    }

    LaunchedEffect(playerTeamStyle.id) {
        Log.d("GameDebug", "LaunchedEffect di inizializzazione eseguito con difficoltà: $selectedDifficulty")
        chatMessages = emptyList()
        llmChatMessages = emptyList()
        winner = null
        finalAiComment = null
        val difficoltaAttuale = try {
            Difficolta.valueOf(selectedDifficulty)
        } catch (e: IllegalArgumentException) {
            Difficolta.FACILE
        }
        damaEngine.nuovaPartita(difficoltaAttuale)
        val initialPieces = parseBoardState(damaEngine.getStatoScacchiera())
        val initialMandatory = damaEngine.getPezziConCatturaObbligatoria()
        gameState = gameState.copy(pieces = initialPieces, mandatoryCapturePieces = initialMandatory)
        turnoCorrente = damaEngine.getTurnoCorrente()

        if (aiOpponent != null) {
            try {
                val modelPath = ModelSettingsManager.getDmModelFilePath(context)
                if (modelPath.isNotBlank()) {
                    gemmaEngine.load(context, modelPath)
                    generateComment(aiOpponent.openingPrompt)
                } else {
                    val errorMessage = "ERRORE: Modello LLM non trovato."
                    Log.e("ChatLog", errorMessage)
                    chatMessages = chatMessages + errorMessage
                }
            } catch (e: Exception) {
                Log.e("GemmaIntegration", "Errore caricamento modello Gemma", e)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            coroutineScope.launch { gemmaEngine.unload() }
            MusicManager.stop()
            TtsManager.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("damaAI") },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            boardCoordinates?.let { coordinates ->
                                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                                val canvas = android.graphics.Canvas(bitmap)
                                view.draw(canvas)

                                val offset = coordinates.positionInWindow()
                                val size = coordinates.size
                                val croppedBitmap = Bitmap.createBitmap(
                                    bitmap,
                                    offset.x.toInt(),
                                    offset.y.toInt(),
                                    size.width,
                                    size.height
                                )

                                val uri = saveBitmapAndGetUri(context, croppedBitmap)
                                uri?.let {
                                    val riepilogoMosse = chatMessages.takeLast(4).joinToString("\n")
                                    val callToAction = if (turnoCorrente == Colore.BIANCO) {
                                        "È il tuo turno, Bianco! Fai la tua mossa."
                                    } else {
                                        "È il tuo turno, Nero! Dimmi le tue mosse."
                                    }
                                    val testoCompleto = "$riepilogoMosse\n\n$callToAction"

                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, it)
                                        putExtra(Intent.EXTRA_TEXT, testoCompleto)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Condividi partita con..."))
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Condividi Partita")
                    }
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
                    .weight(1f)
                    .onGloballyPositioned { boardCoordinates = it },
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
                            aiTeamStyle = aiTeamStyle,
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

                                        val mossaEseguita = damaEngine.muoviPezzoUmano(moveString)

                                        if (mossaEseguita != null) {
                                            val playerColorText = if (turnoCorrente == Colore.BIANCO) "Bianco" else "Nero"
                                            val playerMoveLog = "Mossa $playerColorText: $moveString"
                                            Log.d("ChatLog", "Adding player move: $playerMoveLog")
                                            chatMessages = chatMessages + playerMoveLog

                                            val pedinaCatturata = damaEngine.trovaPedinaCatturata(mossaEseguita)
                                            val piecesAfterMove = parseBoardState(damaEngine.getStatoScacchiera())
                                            gameState = gameState.copy(pieces = piecesAfterMove)
                                            handleCaptureAnimation(pedinaCatturata)

                                            if (aiOpponent != null && damaEngine.getVincitore() == null) {
                                                val mossaIA = damaEngine.faiMossaIA()
                                                if (mossaIA != null) {
                                                    val aiMoveLog = "Mossa IA: ${mossaIA.toString()}"
                                                    Log.d("ChatLog", "Adding AI move: $aiMoveLog")
                                                    chatMessages = chatMessages + aiMoveLog
                                                    val pedinaCatturataIA = damaEngine.trovaPedinaCatturata(mossaIA)
                                                    if (pedinaCatturataIA != null) {
                                                        generateComment(aiOpponent.capturePrompt)
                                                    }
                                                    val finalPieces = parseBoardState(damaEngine.getStatoScacchiera())
                                                    gameState = gameState.copy(pieces = finalPieces)
                                                    handleCaptureAnimation(pedinaCatturataIA)
                                                }
                                            }
                                            turnoCorrente = damaEngine.getTurnoCorrente()
                                            gameState = gameState.copy(mandatoryCapturePieces = damaEngine.getPezziConCatturaObbligatoria())
                                        }

                                        selectedPieceCoords = null
                                        validMoveDestinations = emptyList()

                                    } else {
                                        val pezzoColoreCorretto = when (turnoCorrente) {
                                            Colore.BIANCO -> pieceAtPos?.color == PlayerColor.WHITE
                                            Colore.NERO -> pieceAtPos?.color == PlayerColor.BLACK
                                        }
                                        if (pieceAtPos != null && pezzoColoreCorretto) {
                                            selectedPieceCoords = clickedPos
                                            getValidMovesForSelectedPiece()
                                        }
                                    }

                                    damaEngine.getVincitore()?.let { vincitore ->
                                        winner = vincitore
                                        if (aiOpponent != null) {
                                            val finalPrompt = if (vincitore == Colore.NERO) aiOpponent.victoryPrompt else aiOpponent.defeatPrompt
                                            generateComment(finalPrompt)
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
                                    navController.navigate("game_screen") {
                                        popUpTo("game_screen") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                }
                Spacer(Modifier.height(16.dp))

                if (aiOpponent != null) {
                    AIOpponentHeader(name = aiOpponent.name, isThinking = isAiThinking)
                } else {
                    TurnoGiocatoreHeader(turnoCorrente = turnoCorrente)
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Column(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)) {
                    ChatDisplayArea(
                        messages = llmChatMessages,
                        backgroundColor = BotChatBackground,
                        textStyle = TextStyle(
                            fontFamily = NotoSerif,
                            fontSize = 16.sp,
                            color = BotTextColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    Spacer(Modifier.height(8.dp))
                    ChatDisplayArea(
                        messages = chatMessages,
                        backgroundColor = LogChatBackground,
                        textStyle = TextStyle(
                            fontFamily = RobotoMono,
                            fontSize = 14.sp,
                            color = LogTextColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    )
                }

                if (aiOpponent == null) {
                    ChatInputArea(
                        testoMossa = mossaTestuale,
                        onTestoMossaChange = { mossaTestuale = it },
                        onInviaClick = { eseguiMossaTestuale() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun TurnoGiocatoreHeader(turnoCorrente: Colore, modifier: Modifier = Modifier) {
    val testoTurno = if (turnoCorrente == Colore.BIANCO) "Turno del Bianco" else "Turno del Nero"
    val coloreIcona = if (turnoCorrente == Colore.BIANCO) Color.White else Color.Black

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, coloreIcona, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = testoTurno, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
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
fun ChatDisplayArea(
    messages: List<String>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
) {
    val listState = rememberLazyListState()

    // ===============================================================
    // ==== FIX: SCROLL AUTOMATICO DISABILITATO ====
    // Ho commentato il LaunchedEffect per darti il pieno
    // controllo manuale dello scroll, come richiesto.
    // ===============================================================
    /*
    LaunchedEffect(messages.size, messages.lastOrNull()) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    */

    LazyColumn(
        state = listState,
        modifier = modifier
            .background(
                backgroundColor,
                RoundedCornerShape(8.dp)
            )
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages) { message ->
            Text(
                text = message,
                style = textStyle
            )
        }
    }
}

@Composable
fun ChatInputArea(
    testoMossa: String,
    onTestoMossaChange: (String) -> Unit,
    onInviaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = testoMossa,
            onValueChange = onTestoMossaChange,
            label = { Text("Inserisci mossa (es. A3 B4)") },
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onInviaClick) {
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