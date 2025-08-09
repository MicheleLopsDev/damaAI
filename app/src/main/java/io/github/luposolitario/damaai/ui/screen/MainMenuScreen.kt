package io.github.luposolitario.damaai.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    onNavigateToGameActivity: () -> Unit,
    onNavigateToOptions: () -> Unit,
    onNavigateToLlmManager: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("damaAI") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ) {
                MenuIcon(
                    icon = Icons.Default.MenuBook,
                    label = "Gioca",
                    onClick = onNavigateToGameActivity
                )
                MenuIcon(
                    icon = Icons.Default.Psychology,
                    label = "Gestione IA",
                    onClick = onNavigateToLlmManager
                )
            }
            Spacer(modifier = Modifier.height(32.dp)) // Spazio aggiuntivo

            // --- NUOVA RIGA PER LE IMPOSTAZIONI ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center, // Centriamo l'icona
                verticalAlignment = Alignment.Top
            ) {
                MenuIcon(
                    icon = Icons.Default.Settings,
                    label = "Opzioni",
                    onClick = onNavigateToOptions // Usa la nuova funzione di navigazione
                )
            }
        }
    }
}

@Composable
private fun MenuIcon(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}