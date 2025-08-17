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
                title = { Text("Guida Completa")
                }
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
                title = "Livello Difficoltà IA",
                content = "Questa impostazione influisce sull'abilità dell'IA che calcola le mosse di gioco. Scegli un livello più alto per una sfida maggiore."
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Durante la Partita", style = MaterialTheme.typography.headlineSmall)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // --- NUOVA SEZIONE ---
            HelpSection(
                title = "Casella di Testo e Mosse",
                content = "Sotto la scacchiera trovi una casella di testo che ha una doppia funzione:\n\n" +
                        "1. **Visualizzazione**: Qui compaiono le mosse effettuate e, se hai attivato l'IA Narrativa, anche i commenti e le frasi del tuo avversario.\n\n" +
                        "2. **Inserimento Mossa (solo due giocatori)**: Quando giochi in modalità 'due giocatori', puoi inserire la tua mossa manualmente in questa casella. Scrivi la mossa nel formato classico (es: **A3 B4**) e premi 'Invia' per muovere la pedina."
            )

            HelpSection(
                title = "Condividi Partita",
                content = "Durante una partita, puoi premere l'icona di condivisione per inviare uno screenshot della scacchiera a un amico. Utile per giocare a distanza o per chiedere un consiglio sulla mossa successiva."
            )


            Spacer(modifier = Modifier.height(24.dp))
            Text("Gestione IA (Funzionalità Avanzate)", style = MaterialTheme.typography.headlineSmall)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            HelpSection(
                title = "Due tipi di Intelligenza Artificiale",
                content = "È importante sapere che puoi giocare con i bot **senza scaricare nulla**. L'app usa due IA separate:\n\n" +
                        "1. **IA per le Mosse**: È il vero motore di gioco. Funziona sempre, è integrata nell'app e si occupa di calcolare le mosse dell'avversario.\n\n" +
                        "2. **IA per il Testo (LLM Gemma)**: Questa è una funzionalità **opzionale**. Serve solo a generare frasi per dare una personalità ai bot. Sebbene sia divertente, **rallenta e appesantisce l'app**."
            )

            HelpSection(
                title = "Configurazione ('Gestione Modello IA')",
                content = "Questa schermata serve **solo se vuoi attivare l'IA per il testo**. Qui puoi scaricare il modello LLM (Gemma) o caricarlo dalla memoria del telefono."
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