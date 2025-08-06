package io.github.luposolitario.damaai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import io.github.luposolitario.damaai.ui.theme.DamaAITheme
import io.github.luposolitario.damaai.ui.screen.LlmManagerScreen
import io.github.luposolitario.damaai.viewmodel.LlmManagerViewModel
import io.github.luposolitario.damaai.viewmodel.LlmManagerViewModelFactory

class LlmManagerActivity : ComponentActivity() {

    private val viewModel: LlmManagerViewModel by viewModels {
        LlmManagerViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DamaAITheme {
                LlmManagerScreen(viewModel = viewModel)
            }
        }
    }
}