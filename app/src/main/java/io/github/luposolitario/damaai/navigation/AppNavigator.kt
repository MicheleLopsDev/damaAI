package io.github.luposolitario.damaai.navigation

import android.content.Context
import android.content.Intent
import io.github.luposolitario.damaai.CreditsActivity
import io.github.luposolitario.damaai.GameActivity
import io.github.luposolitario.damaai.HelpActivity
import io.github.luposolitario.damaai.LlmManagerActivity
import io.github.luposolitario.damaai.OptionsActivity
import io.github.luposolitario.damaai.RulesActivity

object AppNavigator {

    fun navigateToGameActivity(context: Context) {
        context.startActivity(Intent(context, GameActivity::class.java))
    }

    fun navigateToOptionsActivity(context: Context) {
        context.startActivity(Intent(context, OptionsActivity::class.java))
    }

    fun navigateToLlmManager(context: Context) {
        context.startActivity(Intent(context, LlmManagerActivity::class.java))
    }

    fun navigateToRulesActivity(context: Context) {
        context.startActivity(Intent(context, RulesActivity::class.java))
    }

    fun navigateToHelpActivity(context: Context) {
        context.startActivity(Intent(context, HelpActivity::class.java))
    }

    fun navigateToCreditsActivity(context: Context) {
        context.startActivity(Intent(context, CreditsActivity::class.java))
    }
}