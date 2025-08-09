package io.github.luposolitario.damaai.ui.screen

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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.luposolitario.damaai.R
import io.github.luposolitario.damaai.data.availableOpponents
import io.github.luposolitario.damaai.data.availableTeamStyles
import io.github.luposolitario.damaai.media.MusicManager
import io.github.luposolitario.damaai.viewmodels.SettingsViewModel

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

    val classicSongs = remember {
        listOf(
            "classic_1" to "Canzone Classica 1",
            "classic_2" to "Canzone Classica 2",
            "classic_3" to "Canzone Classica 3",
            "classic_4" to "Canzone Classica 4",
            "classic_5" to "Canzone Classica 5"
        )
    }

    fun getTrackIdByName(name: String): Int? {
        return when (name.split("_")[0]) {
            "italy" -> R.raw.anthem_italy
            "france" -> R.raw.anthem_france
            "germany" -> R.raw.anthem_germany
            "spain" -> R.raw.anthem_spain
            "uk" -> R.raw.anthem_uk
            "usa" -> R.raw.anthem_usa
            "classic_1" -> R.raw.classic_1
            "classic_2" -> R.raw.classic_2
            "classic_3" -> R.raw.classic_3
            "classic_4" -> R.raw.classic_4
            "classic_5" -> R.raw.classic_5
            else -> null
        }
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
                title = { Text("Opzioni") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
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
            // Section for Team Style
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
                        .clickable {
                            settingsViewModel.setPlayerTeamStyle(style.id)
                            if (style.id != "default") {
                                getTrackIdByName(style.id)?.let { trackId ->
                                    MusicManager.play(context, trackId)
                                }
                            } else {
                                // If classic is selected, play the currently selected classic song
                                getTrackIdByName(selectedClassicSongId)?.let { trackId ->
                                    MusicManager.play(context, trackId)
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
                        Text(text = style.nationName, style = MaterialTheme.typography.bodyLarge)
                        val opponent = availableOpponents.find { it.name == style.opponentName }
                        val opponentText = if (opponent != null) {
                            "Avversario: ${opponent.name}"
                        } else {
                            "Modalità Classica"
                        }
                        Text(
                            text = opponentText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Light
                        )
                    }
                }
            }

            item { Divider(modifier = Modifier.padding(vertical = 16.dp)) }

            // Section for Music and Sounds
            item {
                Text(
                    "Musica e Suoni",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text("Volume", style = MaterialTheme.typography.bodyLarge)
                Slider(
                    value = musicVolume,
                    onValueChange = { newVolume ->
                        settingsViewModel.setMusicVolume(newVolume)
                        MusicManager.setVolume(newVolume)
                    },
                    valueRange = 0f..1f
                )
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
                                            MusicManager.play(context, trackId)
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