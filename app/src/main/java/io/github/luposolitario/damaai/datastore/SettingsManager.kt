package io.github.luposolitario.damaai.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creiamo un'istanza di DataStore legata al Context dell'applicazione
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    // Definiamo una "chiave" per la nostra preferenza. Sarà un valore booleano
    // che ci dice se il tema scuro è abilitato.
    companion object {
        val IS_DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
    }

    // Creiamo un Flow per "ascoltare" i cambiamenti di questa preferenza.
    // Un Flow è un flusso di dati che emette un nuovo valore ogni volta che la preferenza cambia.
    val isDarkModeEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            // Leggiamo il valore booleano. Se non esiste ancora, usiamo 'false' come default.
            preferences[IS_DARK_MODE_ENABLED] ?: false
        }

    // Creiamo una funzione "suspend" per salvare la preferenza.
    // "suspend" significa che questa funzione può essere messa in pausa e ripresa,
    // ed è sicura da chiamare da una Coroutine senza bloccare l'app.
    suspend fun setDarkMode(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_ENABLED] = isEnabled
        }
    }
}