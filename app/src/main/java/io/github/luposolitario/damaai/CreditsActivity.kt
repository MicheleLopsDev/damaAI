package io.github.luposolitario.damaai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.luposolitario.damaai.ui.screen.CreditsScreen
import io.github.luposolitario.damaai.ui.theme.DamaAITheme
import io.github.luposolitario.damaai.viewmodels.SettingsViewModel
import io.github.luposolitario.damaai.viewmodels.SettingsViewModelFactory

class CreditsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val application = application as DamaAIApplication
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(application.settingsManager)
            )
            val useDarkTheme by settingsViewModel.isDarkModeEnabled.collectAsState(initial = isSystemInDarkTheme())

            DamaAITheme(darkTheme = useDarkTheme) {
                CreditsScreen(onBackPressed = { finish() })
            }
        }
    }
}