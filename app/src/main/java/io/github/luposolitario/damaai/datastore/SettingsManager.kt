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

        // --- NUOVA CHIAVE PER LO STILE DELLE PEDINE ---
        val PLAYER_TEAM_STYLE_ID = stringPreferencesKey("player_team_style_id")
    }

    // Flow per il tema scuro (invariato)
    val isDarkModeEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE_ENABLED] ?: false
        }

    // Funzione per salvare il tema scuro (invariata)
    suspend fun setDarkMode(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_ENABLED] = isEnabled
        }
    }

    // --- NUOVO: Flow per leggere lo stile delle pedine ---
    val playerTeamStyleIdFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // Leggiamo l'ID dello stile. Se non esiste, usiamo "default" come valore iniziale.
            preferences[PLAYER_TEAM_STYLE_ID] ?: "default"
        }

    // --- NUOVO: Funzione per salvare lo stile delle pedine ---
    suspend fun setPlayerTeamStyle(styleId: String) {
        context.dataStore.edit { preferences ->
            preferences[PLAYER_TEAM_STYLE_ID] = styleId
        }
    }
}