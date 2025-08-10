package io.github.luposolitario.damaai.utils

import io.github.luposolitario.damaai.R
import io.github.luposolitario.damaai.data.Piece
import io.github.luposolitario.damaai.data.PlayerColor
import kotlin.math.abs

fun getTrackIdByName(name: String?): Int? {
    if (name != null) {
        if(name.contains("classic")){
            return when (name) {
                "classic_1" -> R.raw.classic_1
                "classic_2" -> R.raw.classic_2
                "classic_3" -> R.raw.classic_3
                "classic_4" -> R.raw.classic_4
                "classic_5" -> R.raw.classic_5
                else -> null
            }
        }else{
            val nation= name.split("_")[0]
            return when (nation) {
                "it" -> R.raw.anthem_italy
                "fr" -> R.raw.anthem_france
                "de" -> R.raw.anthem_germany
                "es" -> R.raw.anthem_spain
                "uk" -> R.raw.anthem_uk
                "us" -> R.raw.anthem_usa
                else -> null
            }
        }
    }
    return null
}