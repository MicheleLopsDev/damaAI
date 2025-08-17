package io.github.luposolitario.damaai.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.luposolitario.damaai.viewmodel.DownloadState
import io.github.luposolitario.damaai.viewmodel.LlmManagerViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlmManagerScreen(
    viewModel: LlmManagerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()
    val isModelDownloaded by viewModel.isModelDownloaded.collectAsState()
    val isTokenPresent by viewModel.isTokenPresent.collectAsState()
    val scope = rememberCoroutineScope()

    // Launcher per selezionare file da SD
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                viewModel.loadModelFromLocal(uri)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gestione Modello LLM") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configurazione Globale",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = uiState.huggingFaceToken,
                onValueChange = viewModel::onTokenChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Token Hugging Face") },
                singleLine = true
            )

            if (isTokenPresent) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Modello LLM Avanzato",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("gemma-3n-E4B-it-int4", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Modello di llm ottimizzato.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    AnimatedContent(targetState = if (isModelDownloaded) DownloadState.Completed else downloadState, label = "DownloadButtonAnimation") { state ->
                        when (state) {
                            is DownloadState.Idle -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Button(onClick = { viewModel.startModelDownload() }) {
                                        Icon(Icons.Default.Download, contentDescription = "Download")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Scarica")
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = { filePickerLauncher.launch("*/*") }) {  // Seleziona qualsiasi file
                                        Icon(Icons.Default.Folder, contentDescription = "Carica da SD")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Carica da SD")
                                    }
                                }
                            }

                            is DownloadState.Downloading -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(progress = { state.progress / 100f })
                                    Text(
                                        "${state.progress}%",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = { viewModel.cancelDownload() }) {
                                        Text("Annulla")
                                    }
                                }
                            }

                            is DownloadState.Completed -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completato",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            is DownloadState.Failed -> {
                                Button(onClick = { viewModel.startModelDownload() }) {
                                    Text("Riprova")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}