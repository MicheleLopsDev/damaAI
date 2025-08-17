package io.github.luposolitario.damaai.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guida Completa") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Funzionalità Principali", style = MaterialTheme.typography.headlineSmall)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            HelpSection(
                title = "Scelta Avversario",
                content = "Qui puoi decidere contro chi giocare:\n\n" +
                        "• **Modalità due giocatori (Classic)**: Per giocare contro un'altra persona sullo stesso dispositivo.\n\n" +
                        "• **Avversari IA**: Per sfidare uno dei bot controllati dal computer."
            )

            HelpSection(
                title = "Condividi Partita",
                content = "Durante una partita, puoi premere l'icona di condivisione per inviare uno screenshot della scacchiera a un amico. Utile per giocare a distanza o per chiedere un consiglio sulla mossa successiva."
            )

            HelpSection(
                title = "Livello Difficoltà IA",
                content = "Questa impostazione influisce sull'abilità dell'IA che calcola le mosse di gioco. Scegli un livello più alto per una sfida maggiore."
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Gestione IA (Funzionalità Avanzate)", style = MaterialTheme.typography.headlineSmall)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // --- SEZIONE MODIFICATA ---
            HelpSection(
                title = "Due tipi di Intelligenza Artificiale",
                content = "È importante sapere che puoi giocare con i bot **senza scaricare nulla**. L'app usa due IA separate:\n\n" +
                        "1. **IA per le Mosse**: È il vero motore di gioco. Funziona sempre, è integrata nell'app e si occupa di calcolare le mosse dell'avversario. La sua bravura dipende dalla difficoltà che imposti.\n\n" +
                        "2. **IA per il Testo (LLM Gemma)**: Questa è una funzionalità **opzionale**. Serve solo a generare frasi e commenti per dare una personalità ai bot. Sebbene possa rendere il gioco più simpatico, **rallenta e appesantisce l'app**. Puoi tranquillamente giocare senza attivarla."
            )

            // --- SEZIONE MODIFICATA ---
            HelpSection(
                title = "Configurazione ('Gestione Modello IA')",
                content = "Questa schermata serve **solo se vuoi attivare l'IA per il testo**, quella opzionale.\n\n" +
                        "Qui puoi scaricare il modello LLM (Gemma) dai server di Hugging Face (inserendo un token) o caricarlo dalla memoria del telefono se lo hai già."
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HelpSection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}