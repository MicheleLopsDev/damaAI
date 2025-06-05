package io.github.luposolitario.damaai.data

//@Entity(tableName = "ai_opponents")
data class AiOpponent(
    //@PrimaryKey
    val id: String, // Un ID unico, es. "shakespeare"
    val name: String,           // Il nome visualizzato, es. "William Shakespeare"
    val avatarResourceName: String, // Il nome dell'immagine da trovare nelle risorse drawable
    val gameStylePrompt: String,    // Il prompt per lo stile di gioco da passare a me
    val chatStylePrompt: String,    // Il prompt per lo stile di chat e le frasi preferite
    val description: String         // Una breve descrizione del personaggio
)