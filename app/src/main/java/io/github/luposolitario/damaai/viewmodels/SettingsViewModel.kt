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

    // StateFlow per il tema scuro (invariato)
    val isDarkModeEnabled: StateFlow<Boolean> = settingsManager.isDarkModeEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Funzione per il tema scuro (invariata)
    fun setDarkMode(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setDarkMode(isEnabled)
        }
    }

    // --- NUOVO: StateFlow per leggere lo stile scelto ---
    val playerTeamStyleId: StateFlow<String> = settingsManager.playerTeamStyleIdFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "default" // Valore di partenza
        )

    // --- NUOVO: Funzione per salvare lo stile scelto ---
    fun setPlayerTeamStyle(styleId: String) {
        viewModelScope.launch {
            settingsManager.setPlayerTeamStyle(styleId)
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