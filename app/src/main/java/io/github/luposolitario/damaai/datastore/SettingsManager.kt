package io.github.luposolitario.damaai.datastore

import android.content.Context
import android.content.SharedPreferences
import io.github.luposolitario.damaai.game_logic.Difficolta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    companion object {
        const val IS_DARK_MODE_ENABLED = "dark_mode_enabled"
        const val PLAYER_TEAM_STYLE_ID = "player_team_style_id"
        const val BOARD_STYLE_ID = "board_style_id"
        const val DIFFICULTY_LEVEL = "difficulty_level"
        const val MUSIC_VOLUME = "music_volume"
        const val CLASSIC_SONG_ID = "classic_song_id"
        const val IS_MUSIC_ENABLED = "is_music_enabled"
        const val IS_TTS_ENABLED = "is_tts_enabled"
        const val PLAYER_NAME = "player_name"
    }

    private val _isDarkModeEnabledFlow = MutableStateFlow(false)
    val isDarkModeEnabledFlow: StateFlow<Boolean> = _isDarkModeEnabledFlow.asStateFlow()

    private val _playerTeamStyleIdFlow = MutableStateFlow("default")
    val playerTeamStyleIdFlow: StateFlow<String> = _playerTeamStyleIdFlow.asStateFlow()

    private val _boardStyleIdFlow = MutableStateFlow("wood")
    val boardStyleIdFlow: StateFlow<String> = _boardStyleIdFlow.asStateFlow()

    private val _difficultyLevelFlow = MutableStateFlow(Difficolta.ESPERTO.name)
    val difficultyLevelFlow: StateFlow<String> = _difficultyLevelFlow.asStateFlow()

    private val _musicVolumeFlow = MutableStateFlow(0.05f)
    val musicVolumeFlow: StateFlow<Float> = _musicVolumeFlow.asStateFlow()

    private val _classicSongIdFlow = MutableStateFlow("classic_1")
    val classicSongIdFlow: StateFlow<String> = _classicSongIdFlow.asStateFlow()

    private val _isMusicEnabledFlow = MutableStateFlow(true)
    val isMusicEnabledFlow: StateFlow<Boolean> = _isMusicEnabledFlow.asStateFlow()

    private val _isTtsEnabledFlow = MutableStateFlow(true)
    val isTtsEnabledFlow: StateFlow<Boolean> = _isTtsEnabledFlow.asStateFlow()

    private val _playerNameFlow = MutableStateFlow("Giocatore")
    val playerNameFlow: StateFlow<String> = _playerNameFlow.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            IS_DARK_MODE_ENABLED -> _isDarkModeEnabledFlow.value = sharedPreferences.getBoolean(IS_DARK_MODE_ENABLED, false)
            PLAYER_TEAM_STYLE_ID -> _playerTeamStyleIdFlow.value = sharedPreferences.getString(PLAYER_TEAM_STYLE_ID, "default") ?: "default"
            BOARD_STYLE_ID -> _boardStyleIdFlow.value = sharedPreferences.getString(BOARD_STYLE_ID, "wood") ?: "wood"
            DIFFICULTY_LEVEL -> _difficultyLevelFlow.value = sharedPreferences.getString(DIFFICULTY_LEVEL, Difficolta.ESPERTO.name) ?: Difficolta.ESPERTO.name
            MUSIC_VOLUME -> _musicVolumeFlow.value = sharedPreferences.getFloat(MUSIC_VOLUME, 0.05f)
            CLASSIC_SONG_ID -> _classicSongIdFlow.value = sharedPreferences.getString(CLASSIC_SONG_ID, "classic_1") ?: "classic_1"
            IS_MUSIC_ENABLED -> _isMusicEnabledFlow.value = sharedPreferences.getBoolean(IS_MUSIC_ENABLED, true)
            IS_TTS_ENABLED -> _isTtsEnabledFlow.value = sharedPreferences.getBoolean(IS_TTS_ENABLED, true)
            PLAYER_NAME -> _playerNameFlow.value = sharedPreferences.getString(PLAYER_NAME, "Giocatore") ?: "Giocatore"
        }
    }

    init {
        // Load initial values
        _isDarkModeEnabledFlow.value = sharedPreferences.getBoolean(IS_DARK_MODE_ENABLED, false)
        _playerTeamStyleIdFlow.value = sharedPreferences.getString(PLAYER_TEAM_STYLE_ID, "default") ?: "default"
        _boardStyleIdFlow.value = sharedPreferences.getString(BOARD_STYLE_ID, "wood") ?: "wood"
        _difficultyLevelFlow.value = sharedPreferences.getString(DIFFICULTY_LEVEL, Difficolta.ESPERTO.name) ?: Difficolta.ESPERTO.name
        _musicVolumeFlow.value = sharedPreferences.getFloat(MUSIC_VOLUME, 0.05f)
        _classicSongIdFlow.value = sharedPreferences.getString(CLASSIC_SONG_ID, "classic_1") ?: "classic_1"
        _isMusicEnabledFlow.value = sharedPreferences.getBoolean(IS_MUSIC_ENABLED, true)
        _isTtsEnabledFlow.value = sharedPreferences.getBoolean(IS_TTS_ENABLED, true)
        _playerNameFlow.value = sharedPreferences.getString(PLAYER_NAME, "Giocatore") ?: "Giocatore"

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun setDarkMode(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(IS_DARK_MODE_ENABLED, isEnabled).apply()
    }

    fun setPlayerTeamStyle(styleId: String) {
        sharedPreferences.edit().putString(PLAYER_TEAM_STYLE_ID, styleId).apply()
    }

    fun setBoardStyle(styleId: String) {
        sharedPreferences.edit().putString(BOARD_STYLE_ID, styleId).apply()
    }

    fun setDifficultyLevel(level: String) {
        sharedPreferences.edit().putString(DIFFICULTY_LEVEL, level).apply()
    }

    fun setMusicVolume(volume: Float) {
        sharedPreferences.edit().putFloat(MUSIC_VOLUME, volume).apply()
    }

    fun setClassicSongId(songId: String) {
        sharedPreferences.edit().putString(CLASSIC_SONG_ID, songId).apply()
    }

    fun setMusicEnabled(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(IS_MUSIC_ENABLED, isEnabled).apply()
    }

    fun setTtsEnabled(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(IS_TTS_ENABLED, isEnabled).apply()
    }

    fun setPlayerName(name: String) {
        sharedPreferences.edit().putString(PLAYER_NAME, name).apply()
    }
}