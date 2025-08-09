package io.github.luposolitario.damaai.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.luposolitario.damaai.datastore.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {

    // --- Gestione Tema Scuro (invariata) ---
    val isDarkModeEnabled: StateFlow<Boolean> = settingsManager.isDarkModeEnabledFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = false)
    fun setDarkMode(isEnabled: Boolean) {
        viewModelScope.launch { settingsManager.setDarkMode(isEnabled) }
    }

    // --- Gestione Stile Pedine (invariata) ---
    val playerTeamStyleId: StateFlow<String> = settingsManager.playerTeamStyleIdFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = "default")
    fun setPlayerTeamStyle(styleId: String) {
        viewModelScope.launch { settingsManager.setPlayerTeamStyle(styleId) }
    }

    // --- NUOVO: StateFlow per leggere lo stile della scacchiera ---
    val boardStyleId: StateFlow<String> = settingsManager.boardStyleIdFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "wood" // Valore di partenza
        )

    // --- NUOVO: Funzione per salvare lo stile della scacchiera ---
    fun setBoardStyle(styleId: String) {
        viewModelScope.launch {
            settingsManager.setBoardStyle(styleId)
        }
    }

    // --- NUOVO: StateFlow per la difficoltà ---
    val difficultyLevel: StateFlow<String> = settingsManager.difficultyLevelFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "FACILE" // Valore di partenza
        )

    // --- NUOVO: Funzione per salvare la difficoltà ---
    fun setDifficultyLevel(level: String) {
        viewModelScope.launch {
            settingsManager.setDifficultyLevel(level)
        }
    }

    val classicMusicId: StateFlow<String> = settingsManager.classicMusicIdFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "classic_1"
        )

    fun setClassicMusicId(musicId: String) {
        viewModelScope.launch {
            settingsManager.setClassicMusicId(musicId)
        }
    }
}


// La factory rimane invariata
class SettingsViewModelFactory(private val settingsManager: SettingsManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}