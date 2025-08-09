package io.github.luposolitario.damaai.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.luposolitario.damaai.datastore.SettingsManager
import io.github.luposolitario.damaai.utils.MusicManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Constants for music selection mode
const val MUSIC_MODE_ANTHEM = "anthem"
const val MUSIC_MODE_CLASSIC = "classic"

class SettingsViewModel(
    private val settingsManager: SettingsManager,
    private val musicManager: MusicManager // Inject MusicManager
) : ViewModel() {

    // --- Music Selection Mode ---
    private val _musicSelectionMode = MutableStateFlow(MUSIC_MODE_ANTHEM)
    val musicSelectionMode: StateFlow<String> = _musicSelectionMode

    fun setMusicMode(mode: String) {
        _musicSelectionMode.value = mode
    }

    // --- Music Volume ---
    val musicVolume: StateFlow<Float> = settingsManager.musicVolumeFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0.25f)

    fun setMusicVolume(volume: Float) {
        viewModelScope.launch {
            val clampedVolume = volume.coerceIn(0f, 1f)
            settingsManager.setMusicVolume(clampedVolume)
            musicManager.setVolume(clampedVolume)
        }
    }

    // --- Music/Anthem Selection ---
    val classicMusicId: StateFlow<String> = settingsManager.classicMusicIdFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = "classic_1")

    fun onMusicSelected(musicResId: Int, musicId: String? = null) {
        musicManager.playTrack(musicResId)
        if (musicId != null) {
            viewModelScope.launch {
                settingsManager.setClassicMusicId(musicId)
            }
        }
    }

    // --- Dark Mode ---
    val isDarkModeEnabled: StateFlow<Boolean> = settingsManager.isDarkModeEnabledFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = false)
    fun setDarkMode(isEnabled: Boolean) {
        viewModelScope.launch { settingsManager.setDarkMode(isEnabled) }
    }

    // --- Player Team Style ---
    val playerTeamStyleId: StateFlow<String> = settingsManager.playerTeamStyleIdFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = "default")
    fun setPlayerTeamStyle(styleId: String) {
        viewModelScope.launch { settingsManager.setPlayerTeamStyle(styleId) }
    }

    // --- Board Style ---
    val boardStyleId: StateFlow<String> = settingsManager.boardStyleIdFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = "wood")
    fun setBoardStyle(styleId: String) {
        viewModelScope.launch { settingsManager.setBoardStyle(styleId) }
    }

    // --- Difficulty ---
    val difficultyLevel: StateFlow<String> = settingsManager.difficultyLevelFlow
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = "FACILE")
    fun setDifficultyLevel(level: String) {
        viewModelScope.launch { settingsManager.setDifficultyLevel(level) }
    }
}

// --- Factory needs to be updated to provide MusicManager ---
class SettingsViewModelFactory(
    private val settingsManager: SettingsManager,
    private val musicManager: MusicManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsManager, musicManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}