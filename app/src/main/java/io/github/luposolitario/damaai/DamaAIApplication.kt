package io.github.luposolitario.damaai

import android.app.Application
import io.github.luposolitario.damaai.datastore.SettingsManager

class DamaAIApplication : Application() {
    lateinit var settingsManager: SettingsManager
        private set

    override fun onCreate() {
        super.onCreate()
        settingsManager = SettingsManager(this)
    }
}