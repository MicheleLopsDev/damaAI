package io.github.luposolitario.damaai.navigation

import android.content.Context
import android.content.Intent
import io.github.luposolitario.damaai.GameActivity
import io.github.luposolitario.damaai.LlmManagerActivity

object AppNavigator {

    fun navigateToGameActivity(context: Context) {
        context.startActivity(Intent(context, GameActivity::class.java))
    }

    fun navigateToLlmManager(context: Context) {
        context.startActivity(Intent(context, LlmManagerActivity::class.java))
    }

}