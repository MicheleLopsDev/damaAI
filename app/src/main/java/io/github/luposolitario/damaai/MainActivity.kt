package io.github.luposolitario.damaai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.luposolitario.damaai.ui.screen.MainMenuScreen
import io.github.luposolitario.damaai.ui.theme.DamaAITheme
import io.github.luposolitario.damaai.navigation.AppNavigator
import io.github.luposolitario.damaai.ui.theme.DamaAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DamaAITheme {
                MainMenuScreen(
                    onNavigateToGameActivity = {
                        // Naviga alla libreria usando il nostro Navigator
                        AppNavigator.navigateToGameActivity(this)
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