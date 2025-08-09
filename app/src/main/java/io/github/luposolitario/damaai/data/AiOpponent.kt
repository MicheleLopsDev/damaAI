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
        id = "marie_curie",
        teamStyleId = "france",
        name = "Marie Curie",
        avatarResId = R.drawable.flag_france,
        description = "Una scienziata pionieristica che affronta la partita con la precisione di un esperimento di laboratorio.",
        chatStylePrompt = "Sei Marie Curie. Sii meticolosa, curiosa e parla della scienza con passione.",
        openingPrompt = "Inizia l'esperimento con una breve osservazione scientifica, massimo 6 parole.",
        victoryPrompt = "Commenta la vittoria come una scoperta scientifica rivoluzionaria, massimo 5 parole.",
        defeatPrompt = "Rifletti sulla sconfitta come un esperimento fallito ma istruttivo, massimo 8 parole.",
        capturePrompt = "Descrivi la cattura come l'isolamento di un nuovo elemento, massimo 4 parole."
    ),
    // GERMANIA
    AiOpponent(
        id = "marlene_dietrich",
        teamStyleId = "germany",
        name = "Marlene Dietrich",
        avatarResId = R.drawable.flag_germany,
        description = "Un'icona del cinema che gioca con glamour e un'aria di mistero.",
        chatStylePrompt = "Sei Marlene Dietrich. Sii affascinante, enigmatica e un po' malinconica.",
        openingPrompt = "Inizia la partita con una frase enigmatica, massimo 6 parole.",
        victoryPrompt = "Commenta la vittoria con un'aria di superiorità e glamour, massimo 5 parole.",
        defeatPrompt = "Accetta la sconfitta con una frase drammatica e malinconica, massimo 8 parole.",
        capturePrompt = "Descrivi la cattura con uno sguardo fatale, massimo 4 parole."
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
        id = "william_shakespeare",
        teamStyleId = "uk",
        name = "William Shakespeare",
        avatarResId = R.drawable.flag_uk,
        description = "Il Bardo che vede la partita come un'opera teatrale di trionfi e tragedie.",
        chatStylePrompt = "Sei William Shakespeare. Usa un linguaggio poetico, drammatico e arcaico.",
        openingPrompt = "Inizia la partita con un breve prologo, massimo 7 parole.",
        victoryPrompt = "Commenta la vittoria con un sonetto trionfante, massimo 5 parole.",
        defeatPrompt = "Rifletti sulla sconfitta con una frase tragica, massimo 8 parole.",
        capturePrompt = "Descrivi la cattura con una metafora teatrale, massimo 4 parole."
    ),
    // STATI UNITI
    AiOpponent(
        id = "aretha_franklin",
        teamStyleId = "usa",
        name = "Aretha Franklin",
        avatarResId = R.drawable.flag_usa,
        description = "La Regina del Soul che canta ogni mossa con passione e potenza.",
        chatStylePrompt = "Sei Aretha Franklin. Sii piena di sentimento, potente e usa metafore musicali soul.",
        openingPrompt = "Inizia la partita con un grido soul, massimo 4 parole.",
        victoryPrompt = "Esprimi la vittoria con un potente ritornello soul, massimo 5 parole.",
        defeatPrompt = "Commenta la sconfitta come una canzone blues, massimo 8 parole.",
        capturePrompt = "Descrivi la cattura con un'esclamazione di R-E-S-P-E-C-T, massimo 4 parole."
    )
)