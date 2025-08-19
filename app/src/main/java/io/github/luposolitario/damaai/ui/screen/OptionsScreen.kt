package io.github.luposolitario.damaai.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.github.luposolitario.damaai.data.availableOpponents
import io.github.luposolitario.damaai.data.availableTeamStyles
import io.github.luposolitario.damaai.game_logic.Difficolta
import io.github.luposolitario.damaai.media.MusicManager
import io.github.luposolitario.damaai.utils.getTrackIdByName
import io.github.luposolitario.damaai.viewmodels.SettingsViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val selectedDifficulty by settingsViewModel.difficultyLevel.collectAsState()
    val selectedTeamStyleId by settingsViewModel.playerTeamStyleId.collectAsState()
    val musicVolume by settingsViewModel.musicVolume.collectAsState()
    val selectedClassicSongId by settingsViewModel.classicSongId.collectAsState()
    val isDarkMode by settingsViewModel.isDarkModeEnabled.collectAsState(initial = isSystemInDarkTheme())
    val isMusicEnabled by settingsViewModel.isMusicEnabled.collectAsState()

    val classicSongs = remember {
        listOf(
            "classic_1" to "Canzone Classica 1",
            "classic_2" to "Canzone Classica 2",
            "classic_3" to "Canzone Classica 3",
            "classic_4" to "Canzone Classica 4",
            "classic_5" to "Canzone Classica 5"
        )
    }

    // Stop music when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            MusicManager.stop()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Opzioni") }
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
            // Section for Team Style
            item {
                Text(
                    text = "Scegli il tuo avversario:",
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
                        .clickable {
                            settingsViewModel.setPlayerTeamStyle(style.id)
                            if (style.id != "default") {
                                getTrackIdByName(style.id)?.let { trackId ->
                                    MusicManager.play(context, trackId, isMusicEnabled)
                                }
                            } else {
                                // If classic is selected, play the currently selected classic song
                                getTrackIdByName(selectedClassicSongId)?.let { trackId ->
                                    MusicManager.play(context, trackId, isMusicEnabled)
                                }
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = style.flagResId),
                        contentDescription = "Bandiera ${style.nationName}",
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        val opponent = availableOpponents.find { it.name == style.opponentName }
                        val opponentText = if (opponent != null) {
                            "Avversario: ${opponent.name}"
                        } else {
                            "Modalità due giocatori"
                        }
                        Text(text = opponentText, style = MaterialTheme.typography.bodyLarge, fontFamily = MaterialTheme.typography.bodyLarge.fontFamily)
                        Text(text = opponent?.description ?: "", style = MaterialTheme.typography.bodyLarge, fontFamily = MaterialTheme.typography.bodyLarge.fontFamily)
                    }
                }
            }

            item { Divider(modifier = Modifier.padding(vertical = 16.dp)) }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Modalità Scura")
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { settingsViewModel.setDarkMode(it) })
                }
            }

            item {  // --- 2. AGGIUNGIAMO IL NUOVO SWITCH QUI ---
//                Text(
//                    "Musica e Suoni",
//                    style = MaterialTheme.typography.titleMedium,
//                    modifier = Modifier.padding(bottom = 8.dp)
//                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Musica di sottofondo")
                    Switch(
                        checked = isMusicEnabled,
                        onCheckedChange = { settingsViewModel.setMusicEnabled(it) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Section for Music and Sounds
            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text("Volume", style = MaterialTheme.typography.bodyLarge)

                    // --- NUOVO CONTROLLO VOLUME ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val volumePercentage = (musicVolume * 100).roundToInt()

                        // Pulsante Meno
                        IconButton(onClick = {
                            val newPercentage = (volumePercentage - 1).coerceIn(0, 100)
                            val newVolume = newPercentage / 100f
                            settingsViewModel.setMusicVolume(newVolume)
                            MusicManager.setVolume(newVolume)
                        }) {
                            Icon(Icons.Default.Remove, contentDescription = "Diminuisci volume")
                        }

                        // Testo Percentuale
                        Text(
                            text = "$volumePercentage%",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.width(60.dp),
                            textAlign = TextAlign.Center
                        )

                        // Pulsante Più
                        IconButton(onClick = {
                            val newPercentage = (volumePercentage + 1).coerceIn(0, 100)
                            val newVolume = newPercentage / 100f
                            settingsViewModel.setMusicVolume(newVolume)
                            MusicManager.setVolume(newVolume)
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Aumenta volume")
                        }
                    }
                }
            }


            // Conditional Dropdown for Classic Music
            if (selectedTeamStyleId == "default") {
                item {
                    var isDropdownExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                    ) {
                        TextField(
                            value = classicSongs.find { it.first == selectedClassicSongId }?.second ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Canzone Classica") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            classicSongs.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        settingsViewModel.setClassicSongId(id)
                                        isDropdownExpanded = false
                                        getTrackIdByName(id)?.let { trackId ->
                                            MusicManager.play(context, trackId, isMusicEnabled)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }


            item { Divider(modifier = Modifier.padding(vertical = 16.dp)) }

            // Section for AI Difficulty
            item {
                Text(
                    "Livello Difficoltà IA",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { settingsViewModel.setDifficultyLevel(Difficolta.PRINCIPIANTE.name) }
                    ) {
                        RadioButton(selected = selectedDifficulty == Difficolta.PRINCIPIANTE.name, onClick = { settingsViewModel.setDifficultyLevel(Difficolta.PRINCIPIANTE.name) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Principiante")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { settingsViewModel.setDifficultyLevel(Difficolta.NOVIZIO.name) }
                    ) {
                        RadioButton(selected = selectedDifficulty == Difficolta.NOVIZIO.name, onClick = { settingsViewModel.setDifficultyLevel(Difficolta.NOVIZIO.name) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Novizio")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { settingsViewModel.setDifficultyLevel(Difficolta.INTERMEDIO.name) }
                    ) {
                        RadioButton(selected = selectedDifficulty == Difficolta.INTERMEDIO.name, onClick = { settingsViewModel.setDifficultyLevel(Difficolta.INTERMEDIO.name) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Intermedio")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { settingsViewModel.setDifficultyLevel(Difficolta.AVANZATO.name) }
                    ) {
                        RadioButton(selected = selectedDifficulty == Difficolta.AVANZATO.name, onClick = { settingsViewModel.setDifficultyLevel(Difficolta.AVANZATO.name) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Avanzato")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { settingsViewModel.setDifficultyLevel(Difficolta.ESPERTO.name) }
                    ) {
                        RadioButton(selected = selectedDifficulty == Difficolta.ESPERTO.name, onClick = { settingsViewModel.setDifficultyLevel(Difficolta.ESPERTO.name) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Esperto")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { settingsViewModel.setDifficultyLevel(Difficolta.MAESTRO.name) }
                    ) {
                        RadioButton(selected = selectedDifficulty == Difficolta.MAESTRO.name, onClick = { settingsViewModel.setDifficultyLevel(Difficolta.MAESTRO.name) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Maestro")
                    }
                }
            }
        }
    }
}