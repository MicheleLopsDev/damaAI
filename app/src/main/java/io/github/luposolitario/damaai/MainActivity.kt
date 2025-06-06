package io.github.luposolitario.damaai

// E, se non l'avevi già, l'import per lo stato:
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.luposolitario.damaai.data.GameState
import io.github.luposolitario.damaai.data.Piece
import io.github.luposolitario.damaai.data.PlayerColor
import io.github.luposolitario.damaai.ui.theme.DamaAITheme
import io.github.luposolitario.damaai.utils.formatTime
import io.github.luposolitario.damaai.utils.isValidMove
import io.github.luposolitario.damaai.viewmodels.SettingsViewModel
import io.github.luposolitario.damaai.viewmodels.SettingsViewModelFactory
import kotlinx.coroutines.delay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import io.github.luposolitario.damaai.data.availableTeamStyles
import androidx.compose.foundation.lazy.items


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Recuperiamo l'istanza di DamaAIApplication per accedere al nostro SettingsManager
            val application = application as DamaAIApplication
            // 2. Usiamo la factory per creare (o recuperare) il nostro SettingsViewModel
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(application.settingsManager)
            )

            // 3. Usiamo collectAsState per convertire il Flow<Boolean> del ViewModel
            //    in uno State<Boolean> che Compose può usare per aggiornare la UI.
            val useDarkTheme by settingsViewModel.isDarkModeEnabled.collectAsState(initial = isSystemInDarkTheme())

            // 4. Passiamo lo stato del tema al nostro Composable principale
            DamaAITheme(
                darkTheme = useDarkTheme
            ) {
                AppNavigation(settingsViewModel = settingsViewModel)
            }
        }
    }
}

// Aggiungi questa nuova funzione nel file MainActivity.kt
@Composable
fun AIOpponentHeader(
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp), // Aggiungiamo un po' di padding laterale
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Usiamo un'icona come segnaposto per l'avatar
        Image(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar dell'avversario AI",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape) // Ritagliamo l'immagine in un cerchio
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.width(12.dp)) // Spazio tra avatar e nome

        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sta scrivendo...", // Un piccolo tocco di realismo
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            horizontalAlignment = Alignment.CenterHorizontally

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

            Spacer(modifier = Modifier.height(16.dp))

            // Sostituisci la vecchia Row del turno/timer con questa
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- TESTO DEL TURNO MODIFICATO ---
                // Usiamo 'when' per decidere quale testo mostrare
                val turnText = when (gameState.currentPlayer) {
                    PlayerColor.BLACK -> "Tocca a: Wialiam Sheaskeper" // Turno dell'AI
                    PlayerColor.WHITE -> "Tocca a te"                   // Turno del giocatore umano
                }
                Text(
                    text = turnText,
                    style = MaterialTheme.typography.titleLarge
                )
                // --- FINE MODIFICA ---

                Text(
                    // Il timer rimane invariato
                    text = "⏳ ${formatTime(gameState.turnElapsedTimeInSeconds)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // Aggiungiamo un po' più di spazio

            AIOpponentHeader(name = "Wialiam Sheaskeper")

            Spacer(modifier = Modifier.height(8.dp)) // Aggiungiamo un po' più di spazio

            ChatDisplayArea(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Fai in modo che la chat occupi lo spazio rimanente
            )

            Spacer(modifier = Modifier.height(8.dp)) // Aggiungiamo un po' più di spazio

            ChatInputArea(
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

// Sostituisci la vecchia SettingsScreen con questa
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val isDarkMode by settingsViewModel.isDarkModeEnabled.collectAsState(initial = isSystemInDarkTheme())

    Scaffold(
        topBar = { /* ... (TopAppBar invariata) ... */ }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Riga per la Modalità Scura (invariata)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Modalità Scura",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { nuovoValore ->
                        settingsViewModel.setDarkMode(nuovoValore)
                    }
                )
            }

            Divider() // Aggiungiamo un divisore

            // --- NUOVA VOCE: PERSONALIZZA ASPETTO ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("customization_screen") }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Palette, // Una bella icona a forma di tavolozza
                    contentDescription = "Personalizza Aspetto",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Personalizza Aspetto", style = MaterialTheme.typography.bodyLarge)
            }

            Divider() // Aggiungiamo un divisore

            // --- NUOVA VOCE: AIUTO ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("help_screen") } // Naviga al click
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Aiuto",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Aiuto", style = MaterialTheme.typography.bodyLarge)
            }
            // --- FINE NUOVA VOCE ---

            Divider()

            // --- NUOVA VOCE: CREDITI ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("credits_screen") } // Naviga al click
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Crediti",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Crediti", style = MaterialTheme.typography.bodyLarge)
            }
            // --- FINE NUOVA VOCE ---
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
 * MODIFICATO: AppNavigation ora accetta il ViewModel per passarlo alle schermate
 */
@Composable
fun AppNavigation(settingsViewModel: SettingsViewModel) { // <-- NUOVO PARAMETRO
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "game_screen") {
        composable(route = "game_screen") {
            GameScreen(navController = navController)
        }
        composable(route = "settings_screen") {
            // Passiamo l'istanza del ViewModel alla schermata delle impostazioni
            SettingsScreen(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }
        // --- NUOVE ROTTE ---
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

// Aggiungi queste due nuove funzioni nel file

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

// Sostituisci la vecchia CustomizationScreen con questa
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationScreen(navController: NavController) {
    // Per questa schermata, abbiamo bisogno di accedere al nostro SettingsViewModel.
    // Lo recuperiamo esattamente come abbiamo fatto nella MainActivity.
    val application = LocalContext.current.applicationContext as DamaAIApplication
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(application.settingsManager)
    )

    // Osserviamo qual è l'ID dello stile attualmente selezionato.
    val selectedStyleId by settingsViewModel.playerTeamStyleId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalizza Pedine") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Usiamo una LazyColumn per mostrare la lista di stili.
        // È più efficiente di una Column se la lista diventa molto lunga.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Spazio tra gli elementi
        ) {
            item {
                Text(
                    text = "Scegli lo stile per le tue pedine:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Creiamo un item per ogni stile disponibile nella nostra lista
            items(items = availableTeamStyles, key = { it.id }) { style ->
                // Determiniamo se questo è lo stile attualmente selezionato
                val isSelected = style.id == selectedStyleId

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 3.dp,
                            // Se l'item è selezionato, mostriamo un bordo colorato, altrimenti trasparente.
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)) // Aggiungiamo il clip per rendere il clickable arrotondato
                        .clickable {
                            // Al click, chiamiamo il ViewModel per salvare il nuovo stile
                            settingsViewModel.setPlayerTeamStyle(style.id)
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = style.flagResId),
                        contentDescription = "Bandiera ${style.nationName}",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = style.nationName, style = MaterialTheme.typography.bodyLarge)
                }
            }
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