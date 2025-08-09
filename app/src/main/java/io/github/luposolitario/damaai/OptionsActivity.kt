package io.github.luposolitario.damaai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import io.github.luposolitario.damaai.ui.screen.OptionsScreen
import io.github.luposolitario.damaai.ui.theme.DamaAITheme
import io.github.luposolitario.damaai.viewmodels.SettingsViewModel
import io.github.luposolitario.damaai.viewmodels.SettingsViewModelFactory


class OptionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val application = application as DamaAIApplication
        val settingsViewModel: SettingsViewModel = viewModels<SettingsViewModel> {
            SettingsViewModelFactory(application.settingsManager)
        }.value

        setContent {
            DamaAITheme {
                val navController = rememberNavController()
                OptionsScreen(navController = navController, settingsViewModel = settingsViewModel)
            }
        }
    }
}