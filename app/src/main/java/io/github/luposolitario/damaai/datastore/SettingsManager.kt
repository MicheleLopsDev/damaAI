package io.github.luposolitario.damaai.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import android.util.Log
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        val IS_DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val PLAYER_TEAM_STYLE_ID = stringPreferencesKey("player_team_style_id")

        // --- NUOVA CHIAVE PER LO STILE DELLA SCACCHIERA ---
        val BOARD_STYLE_ID = stringPreferencesKey("board_style_id")
        // --- NUOVA CHIAVE PER LA DIFFICOLTÀ ---
        val DIFFICULTY_LEVEL = stringPreferencesKey("difficulty_level")

        // --- NUOVE CHIAVI PER LA MUSICA ---
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val CLASSIC_SONG_ID = stringPreferencesKey("classic_song_id")
    }

    // --- Gestione Tema Scuro (invariata) ---
    val isDarkModeEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_DARK_MODE_ENABLED] ?: false }
    suspend fun setDarkMode(isEnabled: Boolean) {
        context.dataStore.edit {
            it[IS_DARK_MODE_ENABLED] = isEnabled
        }
    }

    // --- Gestione Stile Pedine (invariata) ---
    val playerTeamStyleIdFlow: Flow<String> = context.dataStore.data.map { it[PLAYER_TEAM_STYLE_ID] ?: "default" }
    suspend fun setPlayerTeamStyle(styleId: String) {
        context.dataStore.edit { it[PLAYER_TEAM_STYLE_ID] = styleId }
    }

    // --- NUOVO: Flow per leggere lo stile della scacchiera ---
    val boardStyleIdFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // Leggiamo l'ID dello stile. Se non esiste, usiamo "wood" come valore iniziale.
            preferences[BOARD_STYLE_ID] ?: "wood"
        }

    // --- NUOVO: Funzione per salvare lo stile della scacchiera ---
    suspend fun setBoardStyle(styleId: String) {
        context.dataStore.edit { preferences ->
            preferences[BOARD_STYLE_ID] = styleId
        }
    }

    // --- NUOVO: Flow per leggere la difficoltà ---
    val difficultyLevelFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // Se non c'è un valore salvato, usiamo "FACILE" come default.
            val difficulty = preferences[DIFFICULTY_LEVEL] ?: "FACILE"
            Log.d("SettingsDebug", "DataStore ha letto la difficoltà: $difficulty") // <-- AGGIUNGI QUESTO LOG
            difficulty
        }

    // --- NUOVO: Funzione per salvare la difficoltà ---
    suspend fun setDifficultyLevel(level: String) {
        context.dataStore.edit { preferences ->
            preferences[DIFFICULTY_LEVEL] = level
        }
    }

    // --- NUOVO: Flow per leggere il volume della musica ---
    val musicVolumeFlow: Flow<Float> = context.dataStore.data
        .map { preferences ->
            // Se non c'è un valore salvato, usiamo 0.5f come default.
            preferences[MUSIC_VOLUME] ?: 0.5f
        }

    // --- NUOVO: Funzione per salvare il volume della musica ---
    suspend fun setMusicVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[MUSIC_VOLUME] = volume
        }
    }

    // --- NUOVO: Flow per leggere la canzone classica selezionata ---
    val classicSongIdFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // Se non c'è un valore salvato, usiamo "classic_1" come default.
            preferences[CLASSIC_SONG_ID] ?: "classic_1"
        }

    // --- NUOVO: Funzione per salvare la canzone classica selezionata ---
    suspend fun setClassicSongId(songId: String) {
        context.dataStore.edit { preferences ->
            preferences[CLASSIC_SONG_ID] = songId
        }
    }
}