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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val selectedDifficulty by settingsViewModel.difficultyLevel.collectAsState()
    val selectedTeamStyleId by settingsViewModel.playerTeamStyleId.collectAsState()
    val selectedClassicMusicId by settingsViewModel.classicMusicId.collectAsState()

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

            item { Divider(modifier = Modifier.padding(vertical = 16.dp)) }

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
                    Column {
                        Text(text = style.nationName, style = MaterialTheme.typography.bodyLarge)
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
                }
            }

            if (selectedTeamStyleId == "default") {
                item {
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                }
                item {
                    Text(
                        text = "Scegli la musica per la modalità Classica:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(availableClassicMusic) { music ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { settingsViewModel.setClassicMusicId(music.id) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedClassicMusicId == music.id,
                            onClick = { settingsViewModel.setClassicMusicId(music.id) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(music.name)
                    }
                }
            }
        }
    }
}