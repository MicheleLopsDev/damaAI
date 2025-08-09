package io.github.luposolitario.damaai

import android.app.Application
import io.github.luposolitario.damaai.datastore.SettingsManager
import io.github.luposolitario.damaai.utils.MusicManager

class DamaAIApplication : Application() {
    // Using 'lazy' to create manager instances only when first requested.
    val settingsManager by lazy { SettingsManager(this) }
    val musicManager by lazy { MusicManager(this) }
}