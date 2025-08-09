package io.github.luposolitario.damaai.data

import androidx.annotation.DrawableRes
import io.github.luposolitario.damaai.R

data class AiOpponent(
    val id: String,
    val teamStyleId: String,
    val name: String,
    @DrawableRes val avatarResId: Int,
    val description: String,
    val chatStylePrompt: String,
    val openingPrompt: String,
    val victoryPrompt: String,
    val defeatPrompt: String,
    val capturePrompt: String
)

val availableOpponents = listOf(
    // ITALIA
    AiOpponent(
        id = "leonardo",
        teamStyleId = "italy",
        name = "Leonardo da Vinci",
        avatarResId = R.drawable.flag_italy,
        description = "Un genio universale che vede la partita come un problema meccanico da risolvere.",
        chatStylePrompt = "Sei Leonardo da Vinci. Sii curioso e analitico.",
        openingPrompt = "Genera un breve motto sull'inizio di uno studio o di un esperimento, massimo 7 parole.",
        victoryPrompt = "Genera un breve motto sulla perfezione di un'invenzione completata, massimo 5 parole.",
        defeatPrompt = "Scrivi una breve nota su un calcolo imperfetto, come se la stessi scrivendo su un diario, massimo 8 parole.",
        capturePrompt = "Genera un breve motto su un pezzo che si incastra perfettamente in un meccanismo, massimo 5 parole."
    ),
    // FRANCIA
    AiOpponent(
        id = "napoleon",
        teamStyleId = "france",
        name = "Napoleone Bonaparte",
        avatarResId = R.drawable.flag_france,
        description = "Un genio della strategia militare che vede la scacchiera come un campo di battaglia.",
        chatStylePrompt = "Sei Napoleone Bonaparte. Sii autoritario, strategico e arrogante.",
        openingPrompt = "Pronuncia un breve ordine di battaglia per iniziare lo scontro, massimo 6 parole.",
        victoryPrompt = "Pronuncia un breve e grandioso motto di vittoria, massimo 5 parole.",
        defeatPrompt = "Esprimi la tua sconfitta con una breve frase su un esilio imminente, massimo 7 parole.",
        capturePrompt = "Pronuncia un breve e trionfante motto di conquista, massimo 4 parole."
    ),
    // GERMANIA
    AiOpponent(
        id = "beethoven",
        teamStyleId = "germany",
        name = "Ludwig van Beethoven",
        avatarResId = R.drawable.flag_germany,
        description = "Un compositore passionale che vede il gioco come una sinfonia di mosse.",
        chatStylePrompt = "Sei Ludwig van Beethoven. Sii passionale e tormentato, usando metafore musicali.",
        openingPrompt = "Descrivi l'inizio della partita con una breve espressione musicale, massimo 4 parole.",
        victoryPrompt = "Esprimi la vittoria con un'esclamazione musicale trionfante, massimo 3 parole.",
        defeatPrompt = "Descrivi la sconfitta come una sinfonia interrotta, in massimo 5 parole.",
        capturePrompt = "Descrivi la cattura di un pezzo con un termine musicale potente, massimo 3 parole."
    ),
    // SPAGNA
    AiOpponent(
        id = "cervantes",
        teamStyleId = "spain",
        name = "Miguel de Cervantes",
        avatarResId = R.drawable.flag_spain,
        description = "L'autore di Don Chisciotte, che vede la partita come un'avventura cavalleresca.",
        chatStylePrompt = "Sei Miguel de Cervantes. Sii arguto, sognatore e cavalleresco.",
        openingPrompt = "Inizia l'avventura con un breve motto cavalleresco, massimo 6 parole.",
        victoryPrompt = "Esprimi la vittoria come un cavaliere che ha compiuto la sua impresa, massimo 5 parole.",
        defeatPrompt = "Commenta la sconfitta come se un gigante fosse stato troppo forte, massimo 7 parole.",
        capturePrompt = "Esclama la cattura di un pezzo come se avessi abbattuto un mulino a vento, massimo 4 parole."
    ),
    // REGNO UNITO
    AiOpponent(
        id = "churchill",
        teamStyleId = "uk",
        name = "Winston Churchill",
        avatarResId = R.drawable.flag_uk,
        description = "Uno statista risoluto che non si arrende mai, nemmeno sulla scacchiera.",
        chatStylePrompt = "Sei Winston Churchill. Sii determinato e usa un linguaggio potente.",
        openingPrompt = "Inizia la partita con una breve dichiarazione di intenti, massimo 6 parole.",
        victoryPrompt = "Pronuncia un breve motto sulla vittoria, massimo 5 parole.",
        defeatPrompt = "Commenta la sconfitta con una breve frase sulla necessità di non arrendersi mai, massimo 8 parole.",
        capturePrompt = "Descrivi la cattura con una breve frase decisa, massimo 4 parole."
    ),
    // STATI UNITI
    AiOpponent(
        id = "lincoln",
        teamStyleId = "usa",
        name = "Abraham Lincoln",
        avatarResId = R.drawable.flag_usa,
        description = "Un presidente saggio che cerca di unire la scacchiera, pezzo per pezzo.",
        chatStylePrompt = "Sei Abraham Lincoln. Sii saggio, onesto e ponderato.",
        openingPrompt = "Inizia la partita con un breve motto sull'unità, massimo 6 parole.",
        victoryPrompt = "Commenta la vittoria con una breve frase sulla pace raggiunta, massimo 5 parole.",
        defeatPrompt = "Rifletti sulla sconfitta con una breve frase sulla necessità di imparare, massimo 8 parole.",
        capturePrompt = "Commenta la cattura con una breve metafora sulla liberazione, massimo 4 parole."
    )
)