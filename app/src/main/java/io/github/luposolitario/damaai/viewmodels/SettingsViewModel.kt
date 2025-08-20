package io.github.luposolitario.damaai.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.luposolitario.damaai.datastore.SettingsManager
import io.github.luposolitario.damaai.game_logic.Difficolta
import io.github.luposolitario.damaai.media.MusicManager
import io.github.luposolitario.damaai.media.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val settingsManager: SettingsManager
) : AndroidViewModel(application) {

    val musicManager = MusicManager(application.applicationContext, settingsManager, viewModelScope)
    val ttsManager = TtsManager(application.applicationContext, settingsManager, viewModelScope)

    private val _isGlobalMuteOn = MutableStateFlow(false)
    val isGlobalMuteOn = _isGlobalMuteOn.asStateFlow()

    fun toggleGlobalMute() {
        _isGlobalMuteOn.update { !it }
        musicManager.setGlobalMute(_isGlobalMuteOn.value)
        ttsManager.setGlobalMute(_isGlobalMuteOn.value)
    }

    val isDarkModeEnabled: StateFlow<Boolean> = settingsManager.isDarkModeEnabledFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = false)
    fun setDarkMode(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setDarkMode(isEnabled)
        }
    }

    val playerTeamStyleId: StateFlow<String?> = settingsManager.playerTeamStyleIdFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)
    fun setPlayerTeamStyle(styleId: String) {
        viewModelScope.launch { settingsManager.setPlayerTeamStyle(styleId) }
    }

    val boardStyleId: StateFlow<String> = settingsManager.boardStyleIdFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "wood"
        )
    fun setBoardStyle(styleId: String) {
        viewModelScope.launch {
            settingsManager.setBoardStyle(styleId)
        }
    }

    // --- MODIFICA QUI ---
    // Il valore iniziale ora è null per rappresentare lo stato di "caricamento".
    // Il tipo diventa nullabile (String?).
    val difficultyLevel: StateFlow<String?> = settingsManager.difficultyLevelFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue =  null
        )
    fun setDifficultyLevel(level: String) {
        viewModelScope.launch {
            settingsManager.setDifficultyLevel(level)
        }
    }

    val musicVolume: StateFlow<Float> = settingsManager.musicVolumeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.05f
        )
    fun setMusicVolume(volume: Float) {
        viewModelScope.launch {
            settingsManager.setMusicVolume(volume)
        }
    }

    val classicSongId: StateFlow<String> = settingsManager.classicSongIdFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "classic_1"
        )
    fun setClassicSongId(songId: String) {
        viewModelScope.launch {
            Log.d("MusicDebug", "SALVATAGGIO: L'utente ha selezionato la canzone con ID: $songId")
            settingsManager.setClassicSongId(songId)
        }
    }

    val isMusicEnabled: StateFlow<Boolean> = settingsManager.isMusicEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    fun setMusicEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setMusicEnabled(isEnabled)
        }
    }

    val isTtsEnabled: StateFlow<Boolean> = settingsManager.isTtsEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    fun setTtsEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setTtsEnabled(isEnabled)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicManager.release()
        ttsManager.shutdown()
    }
}

class SettingsViewModelFactory(
    private val application: Application,
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(application, settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}