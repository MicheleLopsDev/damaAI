package io.github.luposolitario.damaai.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.luposolitario.damaai.DamaAIApplication
import io.github.luposolitario.damaai.data.availableBoardStyles
import io.github.luposolitario.damaai.data.availableTeamStyles
import io.github.luposolitario.damaai.viewmodels.SettingsViewModel
import io.github.luposolitario.damaai.viewmodels.SettingsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val isDarkMode by settingsViewModel.isDarkModeEnabled.collectAsState(initial = isSystemInDarkTheme())
    val selectedTeamStyleId by settingsViewModel.playerTeamStyleId.collectAsState()
    val selectedBoardStyleId by settingsViewModel.boardStyleId.collectAsState()
// --- LEGGIAMO LO STATO DELLA DIFFICOLTÀ ---
    val selectedDifficulty by settingsViewModel.difficultyLevel.collectAsState()



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
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // --- NUOVA SEZIONE PER LA DIFFICOLTÀ ---
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Livello Difficoltà IA",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Opzione "Facile"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { settingsViewModel.setDifficultyLevel("FACILE") }
                    ) {
                        RadioButton(
                            selected = selectedDifficulty == "FACILE",
                            onClick = { settingsViewModel.setDifficultyLevel("FACILE") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Facile")
                    }

                    // Opzione "Medio"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { settingsViewModel.setDifficultyLevel("MEDIO") }
                    ) {
                        RadioButton(
                            selected = selectedDifficulty == "MEDIO",
                            onClick = { settingsViewModel.setDifficultyLevel("MEDIO") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Medio")
                    }

                    // Opzione "Difficile"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { settingsViewModel.setDifficultyLevel("DIFFICILE") }
                    ) {
                        RadioButton(
                            selected = selectedDifficulty == "DIFFICILE",
                            onClick = { settingsViewModel.setDifficultyLevel("DIFFICILE") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Difficile")
                    }
                }
            }


            item {
                Row(
                    verticalAlignment = Alignment.Companion.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.Companion.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "Modalità Scura", style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { settingsViewModel.setDarkMode(it) })
                }
            }

            item { Divider() }

            item {
                Row(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .clickable { navController.navigate("customization_screen") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Personalizza Aspetto",
                        modifier = Modifier.Companion.size(24.dp)
                    )
                    Spacer(modifier = Modifier.Companion.width(16.dp))
                    Text("Personalizza Aspetto", style = MaterialTheme.typography.bodyLarge)
                }
            }

            item { Divider() }

            item {
                Row(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .clickable { navController.navigate("help_screen") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Aiuto",
                        modifier = Modifier.Companion.size(24.dp)
                    )
                    Spacer(modifier = Modifier.Companion.width(16.dp))
                    Text("Aiuto", style = MaterialTheme.typography.bodyLarge)
                }
            }

            item { Divider() }

            item {
                Row(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .clickable { navController.navigate("credits_screen") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Crediti",
                        modifier = Modifier.Companion.size(24.dp)
                    )
                    Spacer(modifier = Modifier.Companion.width(16.dp))
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
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Scegli lo stile per le tue pedine:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.Companion.padding(bottom = 8.dp)
                )
            }
            items(items = availableTeamStyles, key = { it.id }) { style ->
                val isSelected = style.id == selectedTeamStyleId
                Row(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .border(
                            width = 3.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Companion.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .clickable { settingsViewModel.setPlayerTeamStyle(style.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = style.flagResId),
                        contentDescription = "Bandiera ${style.nationName}",
                        modifier = Modifier.Companion.size(40.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.Companion.width(16.dp))
                    Text(text = style.nationName, style = MaterialTheme.typography.bodyLarge)
                }
            }
            item {
                Divider(modifier = Modifier.Companion.padding(vertical = 16.dp))
                Text(
                    text = "Scegli lo stile della scacchiera:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.Companion.padding(bottom = 8.dp)
                )
            }
            items(items = availableBoardStyles, key = { it.id }) { style ->
                val isSelected = style.id == selectedBoardStyleId
                Row(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .border(
                            width = 3.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Companion.Transparent,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .clickable { settingsViewModel.setBoardStyle(style.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.Companion
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    ) {
                        Box(
                            modifier = Modifier.Companion.weight(1f).fillMaxHeight()
                                .background(style.lightSquareColor)
                        )
                        Box(
                            modifier = Modifier.Companion.weight(1f).fillMaxHeight()
                                .background(style.darkSquareColor)
                        )
                    }
                    Spacer(modifier = Modifier.Companion.width(16.dp))
                    Text(text = style.name, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}