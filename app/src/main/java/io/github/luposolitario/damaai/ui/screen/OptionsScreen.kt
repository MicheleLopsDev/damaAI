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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.luposolitario.damaai.data.availableClassicMusic
import io.github.luposolitario.damaai.data.availableOpponents
import io.github.luposolitario.damaai.data.availableTeamStyles
import io.github.luposolitario.damaai.viewmodels.SettingsViewModel
import io.github.luposolitario.damaai.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val selectedDifficulty by settingsViewModel.difficultyLevel.collectAsState()
    val selectedTeamStyleId by settingsViewModel.playerTeamStyleId.collectAsState()
    val selectedClassicMusicId by settingsViewModel.classicMusicId.collectAsState()
    val musicVolume by settingsViewModel.musicVolume.collectAsState()

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
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Difficulty Section
            item {
                Text("Livello Difficoltà IA", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                Column {
                    listOf("FACILE", "MEDIO", "DIFFICILE").forEach { level ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { settingsViewModel.setDifficultyLevel(level) }
                        ) {
                            RadioButton(selected = selectedDifficulty == level, onClick = { settingsViewModel.setDifficultyLevel(level) })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(level.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }

            item { Divider(modifier = Modifier.padding(vertical = 16.dp)) }

            // Volume Slider Section
            item {
                Text("Volume Musica", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                Slider(
                    value = musicVolume,
                    onValueChange = { newVolume -> settingsViewModel.setMusicVolume(newVolume) },
                    valueRange = 0f..1f,
                    steps = 100
                )
            }

            item { Divider(modifier = Modifier.padding(vertical = 16.dp)) }

            // Team/Anthem Selection
            item {
                Text("Scegli Inno Nazionale o Musica Classica", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            }
            items(items = availableTeamStyles, key = { it.id }) { style ->
                val isSelected = style.id == selectedTeamStyleId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 3.dp, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, shape = RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            settingsViewModel.setPlayerTeamStyle(style.id)
                            style.anthemResId?.let { anthem ->
                                settingsViewModel.onMusicSelected(anthem)
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(painter = painterResource(id = style.flagResId), contentDescription = "Bandiera ${style.nationName}", modifier = Modifier.size(40.dp).clip(CircleShape))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = style.nationName, style = MaterialTheme.typography.bodyLarge)
                        val opponentText = if (style.id != "default") "Avversario: ${availableOpponents.find { it.teamStyleId == style.id }?.name ?: ""}" else "Modalità Classica (umano vs umano)"
                        Text(text = opponentText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Light)
                    }
                }
            }

            // Classic Music Dropdown Section
            if (selectedTeamStyleId == "default") {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Scegli la musica per la modalità Classica:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

                    var expanded by remember { mutableStateOf(false) }
                    val selectedMusic = availableClassicMusic.find { it.id == selectedClassicMusicId } ?: availableClassicMusic.first()

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = selectedMusic.name,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            availableClassicMusic.forEach { music ->
                                DropdownMenuItem(
                                    text = { Text(music.name) },
                                    onClick = {
                                        settingsViewModel.onMusicSelected(music.musicResId, music.id)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}