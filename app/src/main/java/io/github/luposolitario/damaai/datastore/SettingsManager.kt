package io.github.luposolitario.damaai.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        val IS_DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val PLAYER_TEAM_STYLE_ID = stringPreferencesKey("player_team_style_id")

        // --- NUOVA CHIAVE PER LO STILE DELLA SCACCHIERA ---
        val BOARD_STYLE_ID = stringPreferencesKey("board_style_id")
    }

    // --- Gestione Tema Scuro (invariata) ---
    val isDarkModeEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[IS_DARK_MODE_ENABLED] ?: false }
    suspend fun setDarkMode(isEnabled: Boolean) {
        context.dataStore.edit { it[IS_DARK_MODE_ENABLED] = isEnabled }
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
}