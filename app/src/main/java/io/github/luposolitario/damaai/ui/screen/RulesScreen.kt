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
fun RulesScreen(onBackPressed: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Regole della Dama Italiana") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            InfoCard(
                title = "Un po' di storia",
                content = "La Dama è uno dei giochi da tavolo più antichi e diffusi al mondo. Si pensa che le sue origini risalgano all'antico Egitto. La versione italiana, giocata su una scacchiera 8x8, si è consolidata nel corso dei secoli, diventando un classico del passatempo e un vero e proprio sport della mente."
            )
            Spacer(modifier = Modifier.height(16.dp))
            InfoCard(
                title = "Regolamento Ufficiale",
                content = """
                1.  **Scopo del gioco**: Eliminare tutte le pedine dell'avversario o bloccarlo, impedendogli qualsiasi mossa.

                2.  **La Damiera**: Si gioca su una scacchiera di 64 caselle, 32 bianche e 32 scure. Le pedine si muovono solo sulle caselle scure.

                3.  **Movimento della Pedina**: La pedina si muove sempre in avanti in diagonale di una casella. Non può muovere all'indietro.

                4.  **Presa (o Mangiare)**: La presa è obbligatoria. Se una pedina si trova vicino a una pedina avversaria con la casella successiva libera, deve "soffiarla" e occupare quella casella. Se ci sono più opzioni di presa, è obbligatorio scegliere la presa che cattura più pezzi. Se le prese catturano lo stesso numero di pezzi, si deve scegliere quella con la Dama di maggior valore.

                5.  **Diventare Dama**: Quando una pedina raggiunge l'ultima riga del campo avversario, diventa "Dama". La Dama si muove avanti e indietro in diagonale di una casella e può mangiare sia in avanti che all'indietro.

                6.  **Vittoria**: Vince chi cattura tutti i pezzi dell'avversario o lo mette in una condizione in cui non può più muovere.
                """.trimIndent()
            )
        }
    }
}

@Composable
private fun InfoCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
}