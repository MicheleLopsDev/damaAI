package io.github.luposolitario.damaai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.luposolitario.damaai.ui.screen.MainMenuScreen
import io.github.luposolitario.damaai.ui.theme.DamaAITheme
import io.github.luposolitario.damaai.navigation.AppNavigator
import io.github.luposolitario.damaai.ui.theme.DamaAITheme
import io.github.luposolitario.damaai.viewmodels.SettingsViewModel
import io.github.luposolitario.damaai.viewmodels.SettingsViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val application = application as DamaAIApplication
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(application.settingsManager)
            )
            val useDarkTheme by settingsViewModel.isDarkModeEnabled.collectAsState(initial = isSystemInDarkTheme())

            DamaAITheme(darkTheme = useDarkTheme) {
                MainMenuScreen(
                    onNavigateToGameActivity = {
                        // Naviga alla libreria usando il nostro Navigator
                        AppNavigator.navigateToGameActivity(this)
                    }, onNavigateToOptions = {
                        AppNavigator.navigateToOptionsActivity(this)
                    },
                    // Lasciamo questo vuoto per ora, come da piano
                    onNavigateToLlmManager = {
                        startActivity(Intent(this, LlmManagerActivity::class.java))
                    }
                )
            }
        }
    }
}