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
                title = { Text("Guida Completa") },
//                navigationIcon = {
//                    IconButton(onClick = onBackPressed) {
//                        Icon(Icons.Filled.ArrowBack, contentDescription = "Indietro")
//                    }
//                }
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
                content = "In questa sezione puoi decidere contro chi giocare:\n\n" +
                        "• **Modalità due giocatori (Classic)**: Seleziona questa opzione per giocare contro un'altra persona sullo stesso dispositivo.\n\n" +
                        "• **Avversari IA**: Scegli uno degli avversari controllati dall'intelligenza artificiale. Ogni avversario ha una sua personalità e una musica a tema."
            )

            // --- NUOVA SEZIONE ---
            HelpSection(
                title = "Condividi Partita",
                content = "Durante una partita, troverai un'icona di condivisione. Premendola, potrai inviare uno screenshot della scacchiera attuale a un amico, ad esempio su WhatsApp. Questa funzione è utile per due motivi:\n\n" +
                        "1. **Gioco Asincrono**: Puoi giocare una partita con un amico a distanza. Dopo la tua mossa, condividi la scacchiera, lui fa la sua mossa e ti rimanda la nuova situazione.\n\n" +
                        "2. **Chiedere un Consiglio**: Sei in difficoltà contro l'IA? Condividi la partita con un giocatore più esperto per farti suggerire la prossima mossa!"
            )

            HelpSection(
                title = "Musica e Suoni",
                content = "Personalizza l'esperienza sonora del gioco. Puoi regolare il volume generale della musica. Se hai scelto la modalità 'Classic', puoi anche selezionare quale brano classico ascoltare durante la partita."
            )

            HelpSection(
                title = "Livello Difficoltà IA",
                content = "Questa impostazione è cruciale quando giochi contro l'IA. Determina quanto sarà abile il tuo avversario nel calcolare le mosse.\n\n" +
                        "• **Facile**: L'IA giocherà in modo più semplice, ideale per i principianti.\n" +
                        "• **Medio**: Un buon compromesso per giocatori occasionali.\n" +
                        "• **Difficile**: L'IA utilizzerà una strategia più avanzata per metterti alla prova."
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Gestione IA (Funzionalità Avanzate)", style = MaterialTheme.typography.headlineSmall)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            HelpSection(
                title = "Come funziona l'IA in damaAI?",
                content = "È importante capire che l'app usa due sistemi di IA separati:\n\n" +
                        "1. **IA di Gioco (Logica Locale)**: È il cervello che decide le mosse. Funziona sempre, **interamente sul tuo dispositivo**, e la sua abilità dipende solo dal **Livello Difficoltà** che hai impostato.\n\n" +
                        "2. **IA Narrativa (Personalità con Gemma)**: Questo è un sistema **opzionale**. Il suo unico scopo è generare i commenti che danno una 'personalità' agli avversari. **Non influenza le mosse del gioco**."
            )

            HelpSection(
                title = "Configurazione ('Modello LLM')",
                content = "Questa schermata serve per scaricare l'IA Narrativa (Gemma):\n\n" +
                        "• **Token Hugging Face**: È una chiave per scaricare in modo sicuro il modello Gemma.\n\n" +
                        "• **Modello 'gemma-3n-E4B-it-int4'**: Per usare il modello hai due possibilità:\n" +
                        "  - **Scarica**: Per scaricare il modello tramite l'app.\n" +
                        "  - **Carica da SD**: Se hai già il modello sul telefono, puoi caricarlo dalla memoria locale."
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