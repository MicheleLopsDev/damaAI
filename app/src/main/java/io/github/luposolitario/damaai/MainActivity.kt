package io.github.luposolitario.damaai

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.luposolitario.damaai.data.*
import io.github.luposolitario.damaai.ui.theme.DamaAITheme
import io.github.luposolitario.damaai.utils.formatTime
import io.github.luposolitario.damaai.utils.isValidMove
import io.github.luposolitario.damaai.viewmodels.SettingsViewModel
import io.github.luposolitario.damaai.viewmodels.SettingsViewModelFactory
import kotlinx.coroutines.delay

val initialPieces: List<Piece> = listOf(
    Piece(0, 1, PlayerColor.BLACK), Piece(0, 3, PlayerColor.BLACK), Piece(0, 5, PlayerColor.BLACK), Piece(0, 7, PlayerColor.BLACK),
    Piece(1, 0, PlayerColor.BLACK), Piece(1, 2, PlayerColor.BLACK), Piece(1, 4, PlayerColor.BLACK), Piece(1, 6, PlayerColor.BLACK),
    Piece(2, 1, PlayerColor.BLACK), Piece(2, 3, PlayerColor.BLACK), Piece(2, 5, PlayerColor.BLACK), Piece(2, 7, PlayerColor.BLACK),
    Piece(5, 0, PlayerColor.WHITE), Piece(5, 2, PlayerColor.WHITE), Piece(5, 4, PlayerColor.WHITE), Piece(5, 6, PlayerColor.WHITE),
    Piece(6, 1, PlayerColor.WHITE), Piece(6, 3, PlayerColor.WHITE), Piece(6, 5, PlayerColor.WHITE), Piece(6, 7, PlayerColor.WHITE),
    Piece(7, 0, PlayerColor.WHITE), Piece(7, 2, PlayerColor.WHITE), Piece(7, 4, PlayerColor.WHITE), Piece(7, 6, PlayerColor.WHITE),
)

class MainActivity : ComponentActivity() {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navController: NavController,
    playerTeamStyle: TeamStyle,
    boardStyle: BoardStyle
) {
    var gameState by remember { mutableStateOf(GameState(pieces = initialPieces)) }

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
                    IconButton(onClick = {
                        Log.d("NAV_TEST", "Pulsante impostazioni cliccato! Navigo a 'settings_screen'.")
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                        val clickedPiece = gameState.pieces.find { it.row == row && it.col == col }
                        if (clickedPiece != null && clickedPiece.color == gameState.currentPlayer) {
                            gameState = gameState.copy(selectedPiece = clickedPiece)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .aspectRatio(1f)
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val turnText = when (gameState.currentPlayer) {
                    PlayerColor.BLACK -> "Tocca a: Wialiam Sheaskeper"
                    PlayerColor.WHITE -> "Tocca a te"
                }
                Text(text = turnText, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "⏳ ${formatTime(gameState.turnElapsedTimeInSeconds)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))
            AIOpponentHeader(name = "Wialiam Sheaskeper")
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            ChatDisplayArea(modifier = Modifier.fillMaxWidth().weight(1f))
            ChatInputArea(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun GameBoardArea(
    gameState: GameState,
    playerTeamStyle: TeamStyle,
    boardStyle: BoardStyle,
    onSquareClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val playerPainter: Painter? = if (playerTeamStyle.id != "default") {
        painterResource(id = playerTeamStyle.flagResId)
    } else {
        null
    }

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                val squareSize = size.width / 8f
                val row = (offset.y / squareSize).toInt().coerceIn(0, 7)
                val col = (offset.x / squareSize).toInt().coerceIn(0, 7)
                onSquareClick(row, col)
            }
        }
    ) {
        val squareSize = size.width / 8f
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val isLightSquare = (row + col) % 2 == 0
                val squareColor = if (isLightSquare) boardStyle.lightSquareColor else boardStyle.darkSquareColor
                drawRect(
                    color = squareColor,
                    topLeft = Offset(x = col * squareSize, y = row * squareSize),
                    size = Size(width = squareSize, height = squareSize)
                )
            }
        }

        gameState.pieces.forEach { piece ->
            val center = Offset(x = piece.col * squareSize + squareSize / 2, y = piece.row * squareSize + squareSize / 2)
            val pieceRadius = squareSize * 0.38f

            if (piece == gameState.selectedPiece) {
                drawCircle(color = Color.Yellow.copy(alpha = 0.5f), radius = squareSize / 2, center = center)
            }

            drawCircle(color = Color.Black.copy(alpha = 0.3f), radius = pieceRadius, center = center.copy(y = center.y + 4f))

            if (piece.color == PlayerColor.WHITE) {
                if (playerPainter != null) {
                    drawCircle(color = Color.White, radius = pieceRadius, center = center)
                    val clipPath = Path().apply { addOval(Rect(center = center, radius = pieceRadius)) }
                    clipPath(path = clipPath) {
                        translate(left = center.x - pieceRadius, top = center.y - pieceRadius) {
                            with(playerPainter) { draw(size = Size(pieceRadius * 2, pieceRadius * 2)) }
                        }
                    }
                    drawCircle(color = Color(0xFFBBBBBB), radius = pieceRadius, center = center, style = Stroke(width = squareSize * 0.04f))
                } else {
                    drawCircle(color = Color.White, radius = pieceRadius, center = center)
                    drawCircle(color = Color(0xFFBBBBBB), radius = pieceRadius, center = center, style = Stroke(width = squareSize * 0.04f))
                }
            } else {
                drawCircle(color = Color(0xFF222222), radius = pieceRadius, center = center)
                drawCircle(color = Color.Black, radius = pieceRadius, center = center, style = Stroke(width = squareSize * 0.04f))
            }
        }
    }
}

@Composable
fun AIOpponentHeader(name: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(imageVector = Icons.Default.AccountCircle, contentDescription = "Avatar dell'avversario AI", modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "Sta scrivendo...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val isDarkMode by settingsViewModel.isDarkModeEnabled.collectAsState(initial = isSystemInDarkTheme())
    val selectedTeamStyleId by settingsViewModel.playerTeamStyleId.collectAsState()
    val selectedBoardStyleId by settingsViewModel.boardStyleId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impostazioni") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "Modalità Scura", style = MaterialTheme.typography.titleMedium)
                    Switch(checked = isDarkMode, onCheckedChange = { settingsViewModel.setDarkMode(it) })
                }
            }

            item { Divider() }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("customization_screen") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = "Personalizza Aspetto", modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Personalizza Aspetto", style = MaterialTheme.typography.bodyLarge)
                }
            }

            item { Divider() }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("help_screen") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.HelpOutline, contentDescription = "Aiuto", modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Aiuto", style = MaterialTheme.typography.bodyLarge)
                }
            }

            item { Divider() }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("credits_screen") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Crediti", modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Crediti", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aiuto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Qui ci saranno le istruzioni del gioco.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crediti") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("damaAI", style = MaterialTheme.typography.headlineMedium)
            Text("Sviluppata da Michele Lops (luposolitario)", style = MaterialTheme.typography.bodyLarge)
            Text("sentieroluminoso@gmail.com", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationScreen(navController: NavController) {
    val application = LocalContext.current.applicationContext as DamaAIApplication
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(application.settingsManager)
    )

    val selectedTeamStyleId by settingsViewModel.playerTeamStyleId.collectAsState()
    val selectedBoardStyleId by settingsViewModel.boardStyleId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalizza Aspetto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text(text = "Scegli lo stile per le tue pedine:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp)) }
            items(items = availableTeamStyles, key = { it.id }) { style ->
                val isSelected = style.id == selectedTeamStyleId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 3.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { settingsViewModel.setPlayerTeamStyle(style.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(painter = painterResource(id = style.flagResId), contentDescription = "Bandiera ${style.nationName}", modifier = Modifier.size(40.dp).clip(CircleShape))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = style.nationName, style = MaterialTheme.typography.bodyLarge)
                }
            }
            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text(text = "Scegli lo stile della scacchiera:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            }
            items(items = availableBoardStyles, key = { it.id }) { style ->
                val isSelected = style.id == selectedBoardStyleId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 3.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { settingsViewModel.setBoardStyle(style.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(style.lightSquareColor))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(style.darkSquareColor))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = style.name, style = MaterialTheme.typography.bodyLarge)
                }
            }
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