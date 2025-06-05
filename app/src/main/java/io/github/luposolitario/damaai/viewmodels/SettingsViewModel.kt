package io.github.luposolitario.damaai.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.luposolitario.damaai.datastore.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {

    // Esponiamo il Flow dal SettingsManager come uno StateFlow, che la nostra UI può osservare.
    // Uno StateFlow ha sempre un valore iniziale e "ricorda" l'ultimo valore emesso.
    val isDarkModeEnabled: StateFlow<Boolean> = settingsManager.isDarkModeEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Funzione che la UI chiamerà per cambiare l'impostazione del tema.
    fun setDarkMode(isEnabled: Boolean) {
        // Usiamo viewModelScope per lanciare una coroutine in modo sicuro.
        // Questo scope viene cancellato automaticamente quando il ViewModel non serve più.
        viewModelScope.launch {
            settingsManager.setDarkMode(isEnabled)
        }
    }
}


/**
 * Poiché il nostro ViewModel ha bisogno del SettingsManager per essere creato,
 * dobbiamo creare una "Factory" per insegnare al sistema come costruirlo.
 */
class SettingsViewModelFactory(private val settingsManager: SettingsManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}