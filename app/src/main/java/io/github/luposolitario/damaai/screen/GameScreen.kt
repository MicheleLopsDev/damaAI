package io.github.luposolitario.damaai.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.luposolitario.damaai.DamaAIApplication
import io.github.luposolitario.damaai.data.availableBoardStyles
import io.github.luposolitario.damaai.data.availableOpponents
import io.github.luposolitario.damaai.data.availableTeamStyles
import io.github.luposolitario.damaai.viewmodels.SettingsViewModel
import io.github.luposolitario.damaai.viewmodels.SettingsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
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
            // --- VOCE PER IL NUOVO SOTTOMENÙ OPZIONI ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("options_screen") } // Naviga al sottomenù
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune, // Icona adatta per le opzioni
                        contentDescription = "Opzioni",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Opzioni", style = MaterialTheme.typography.bodyLarge)
                }
            }

            item { Divider() }

            // Le altre voci rimangono qui
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("customization_screen") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Personalizza",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Personalizza", style = MaterialTheme.typography.bodyLarge)
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
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Aiuto",
                        modifier = Modifier.size(24.dp)
                    )
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
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Crediti",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Crediti", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}


// --- NUOVA SCHERMATA PER IL SOTTOMENÙ "OPZIONI" ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val isDarkMode by settingsViewModel.isDarkModeEnabled.collectAsState()
    val selectedDifficulty by settingsViewModel.difficultyLevel.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Opzioni") },
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
            // Sezione Modalità Scura
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "Modalità Scura", style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { settingsViewModel.setDarkMode(it) }
                    )
                }
            }

            item { Divider() }

            // Sezione Difficoltà IA
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Livello Difficoltà IA",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { settingsViewModel.setDifficultyLevel("FACILE") }
                    ) {
                        RadioButton(selected = selectedDifficulty == "FACILE", onClick = { settingsViewModel.setDifficultyLevel("FACILE") })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Facile")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { settingsViewModel.setDifficultyLevel("MEDIO") }
                    ) {
                        RadioButton(selected = selectedDifficulty == "MEDIO", onClick = { settingsViewModel.setDifficultyLevel("MEDIO") })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Medio")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { settingsViewModel.setDifficultyLevel("DIFFICILE") }
                    ) {
                        RadioButton(selected = selectedDifficulty == "DIFFICILE", onClick = { settingsViewModel.setDifficultyLevel("DIFFICILE") })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Difficile")
                    }
                }
            }
        }
    }
}


// ... (Le altre schermate come HelpScreen, CreditsScreen, CustomizationScreen rimangono invariate) ...

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
            modifier = Modifier.Companion.fillMaxSize().padding(paddingValues).padding(16.dp),
            contentAlignment = Alignment.Companion.Center
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
            modifier = Modifier.Companion.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("damaAI", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Sviluppata da Michele Lops (luposolitario)",
                style = MaterialTheme.typography.bodyLarge
            )
            Text("sentieroluminoso@gmail.com", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationScreen(navController: NavController) {
    val application = (LocalView.current.context.applicationContext as DamaAIApplication)
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(application.settingsManager)
    )

    val selectedTeamStyleId by settingsViewModel.playerTeamStyleId.collectAsState()
    val selectedBoardStyleId by settingsViewModel.boardStyleId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalizza") }, // <-- **FIX 1: Titolo modificato**
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
            item {
                Text(
                    text = "Scegli lo stile per le tue pedine:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
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
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { settingsViewModel.setPlayerTeamStyle(style.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = style.flagResId),
                        contentDescription = "Bandiera ${style.nationName}",
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    // --- **FIX 2**: Aggiunto testo descrittivo per l'avversario ---
                    Column {
                        Text(text = style.nationName, style = MaterialTheme.typography.bodyLarge)

                        // Trova l'avversario corrispondente allo stile
                        val opponent = availableOpponents.find { it.teamStyleId == style.id }
                        val opponentText = if (opponent != null) {
                            "Avversario: ${opponent.name}"
                        } else {
                            "Modalità Classica (umano vs umano)"
                        }

                        Text(
                            text = opponentText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Light
                        )
                    }
                    // --- Fine FIX 2 ---
                }
            }
            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    text = "Scegli lo stile della scacchiera:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
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
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        )
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
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .background(style.lightSquareColor)
                        )
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .background(style.darkSquareColor)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = style.name, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}