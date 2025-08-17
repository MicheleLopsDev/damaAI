package io.github.luposolitario.damaai

import android.app.Application
import io.github.luposolitario.damaai.datastore.SettingsManager
import io.github.luposolitario.damaai.media.MusicManager
import io.github.luposolitario.damaai.media.TtsManager

class DamaAIApplication : Application() {
    lateinit var settingsManager: SettingsManager
        private set

    override fun onCreate() {
        super.onCreate()
        settingsManager = SettingsManager(this)

        // ==== AGGIUNTA QUI ====
        // Inizializza il Text-to-Speech Manager
        TtsManager.init(this)
    }
}