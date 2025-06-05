package io.github.luposolitario.damaai

import android.app.Application
import io.github.luposolitario.damaai.datastore.SettingsManager

class DamaAIApplication : Application() {
    // Usiamo 'lazy' per creare l'istanza di SettingsManager solo quando
    // viene richiesta per la prima volta.
    val settingsManager by lazy { SettingsManager(this) }
}