# Piano di Sviluppo: Dama AI 🐴

Questo documento delinea le fasi di sviluppo per l'applicazione Android "Dama AI", un gioco di dama open-source focalizzato sull'apprendimento di Kotlin e Jetpack Compose.

## Obiettivi Principali del Progetto:
* Creare un gioco di dama funzionante e divertente.
* Utilizzare Kotlin e Jetpack Compose per l'intero sviluppo.
* Implementare un'interfaccia utente moderna con Material 3.
* Offrire opzioni di personalizzazione per pedine e tavola.
* Fornire un'esperienza educativa per lo sviluppatore.
* Mantenere l'app leggera e performante.

---

## Fase 1: Struttura UI di Base & Setup Iniziale

* [ ] **Configurazione Progetto:**
    * [ ] Creare un nuovo progetto Android Studio.
    * [ ] Nome app: `damaAI` (configurare in `strings.xml`).
    * [ ] Linguaggio: Kotlin.
    * [ ] Abilitare Jetpack Compose e dipendenze Material 3.
    * [ ] Configurare il tema Material 3 di base (`DamaAITheme` in `ui.theme`).
* [ ] **Schermata Principale (`MainActivity.kt` -> `DamaAIScreen`):**
    * [ ] Utilizzare `Scaffold` di Material 3.
    * [ ] Aggiungere una `TopAppBar` con il titolo "damaAI".
    * [ ] Creare `Column` principale per disporre gli elementi verticalmente.
* [ ] **Aree Segnaposto Composable:**
    * [ ] `GameBoardArea`: Un `Box` quadrato segnaposto per la tavola da gioco (es. 90-95% larghezza schermo, aspect ratio 1:1).
    * [ ] `ChatDisplayArea`: Un `Box` o `Column` segnaposto sotto la tavola per visualizzare i messaggi di chat.
    * [ ] `ChatInputArea`: Un `Row` segnaposto in fondo con un `TextField` e un `Button` per scrivere e inviare messaggi.
* [ ] **Testare Layout di Base:** Verificare che la struttura appaia correttamente su emulatore/dispositivo.

---

## Fase 2: Rappresentazione Visiva della Tavola e delle Pedine

* [ ] **`GameBoardArea` - Disegno Tavola:**
    * [ ] Implementare la griglia 8x8 della dama (es. usando Compose `Canvas` per disegnare quadrati, o `LazyVerticalGrid`).
    * [ ] Applicare colori alternati per le caselle.
* [ ] **`Piece` Composable - Disegno Pedine:**
    * [ ] Creare un Composable per rappresentare una singola pedina.
    * [ ] Disegno iniziale: cerchi colorati semplici (es. bianchi/neri o altri due colori distinti) usando `Canvas`.
    * [ ] Applicare un effetto "leggermente 3D" (es. usando `Modifier.shadow()` o disegnando un'ombra/sfumatura direttamente su `Canvas`).
* [ ] **Posizionamento Statico Pedine:** Mostrare alcune pedine in posizioni fisse sulla tavola per test visivi.

---

## Fase 3: Logica di Gioco di Base (Locale, vs Giocatore "Fantasma")

* [ ] **Strutture Dati per lo Stato del Gioco:**
    * [ ] Definire una struttura dati per rappresentare lo stato della tavola (es. una lista di liste o un array 2D) che tenga traccia di cosa c'è in ogni casella.
    * [ ] Definire classi/oggetti per le Pedine (colore, tipo - normale/dama).
* [ ] **Interazione Utente di Base:**
    * [ ] Logica per selezionare una pedina sulla tavola (al tocco).
    * [ ] Evidenziare visivamente la pedina selezionata.
* [ ] **Movimento Base Pedine:**
    * [ ] Logica per muovere la pedina selezionata in una casella vuota valida (solo movimenti diagonali semplici, senza catture o promozioni a dama per ora).
    * [ ] Aggiornare lo stato della tavola e la UI di conseguenza.
* [ ] **Gestione Turni (Semplice):** Alternare il turno tra "Bianco" e "Nero" (inizialmente entrambi controllati dall'utente).

---

## Fase 4: Menù di Base e Navigazione

* [ ] **Setup Navigazione (Compose Navigation):**
    * [ ] Integrare `NavHostController` e definire le rotte per le schermate principali (Gioco, Impostazioni).
* [ ] **Schermata Impostazioni (Placeholder):**
    * [ ] Creare una nuova Composable `SettingsScreen`.
    * [ ] Aggiungere un modo per navigare alla `SettingsScreen` (es. un'icona `IconButton` nella `TopAppBar` della schermata di gioco).
* [ ] **Opzioni Generali nel Menù Impostazioni:**
    * [ ] **Cambio Tema (Light/Dark):**
        * [ ] Implementare un interruttore (`Switch`) o opzioni per cambiare dinamicamente tra tema chiaro e scuro.
        * [ ] Salvare la preferenza del tema (es. usando `SharedPreferences` o `DataStore`).
    * [ ] **Schermata/Dialogo "Aiuto" (Placeholder):**
        * [ ] Creare una semplice schermata o dialogo con testo di aiuto statico.
    * [ ] **Schermata/Dialogo "Crediti" (Placeholder):**
        * [ ] Creare una semplice schermata o dialogo con i crediti dell'app.
    * [ ] **Opzione "Esci dall'App":**
        * [ ] Aggiungere un pulsante. Discutere/implementare il comportamento (chiusura attività o `System.exit(0)` con cautela).

---

## Fase 5: Personalizzazione Pedine e Tavola

* [ ] **Struttura Menù Personalizzazione:** (Può essere una sezione della `SettingsScreen` o una schermata dedicata).
* [ ] **Personalizzazione Pedine:**
    * [ ] **Selezione Texture/Colore Pedine:**
        * [ ] Preparare alcune immagini PNG (colori base, semplici pattern, 1-2 bandiere esempio) e includerle nelle risorse `drawable`.
        * [ ] Creare una UI (es. griglia di immagini selezionabili) per scegliere la texture/colore per le pedine bianche e nere.
        * [ ] Applicare la selezione dinamicamente alle pedine nel `Piece` Composable.
        * [ ] Salvare le preferenze di personalizzazione.
* [ ] **Personalizzazione Tavola:**
    * [ ] **Selezione Texture Tavola:**
        * [ ] Preparare alcune immagini PNG per diversi stili di tavola.
        * [ ] Creare una UI per scegliere la texture della tavola.
        * [ ] Applicare la selezione dinamicamente allo sfondo della `GameBoardArea`.
        * [ ] Salvare le preferenze di personalizzazione.

---

## Fase 6: Logica di Gioco Avanzata e Interazioni Chat

* [ ] **Logica di Gioco Completa (vs Giocatore "Fantasma"):**
    * [ ] Implementare la logica per la cattura delle pedine avversarie.
    * [ ] Implementare la promozione a "Dama" (e distinguere visivamente le Dame).
    * [ ] Implementare le regole di movimento per le Dame.
    * [ ] Implementare la cattura multipla.
* [ ] **Rilevamento Fine Partita:**
    * [ ] Logica per determinare la vittoria/sconfitta (es. un giocatore non ha più pedine o non ha mosse valide).
    * [ ] Mostrare un messaggio di fine partita.
* [ ] **Funzionalità Chat (Locale):**
    * [ ] Collegare l'input dell'`EditText` in `ChatInputArea` all'invio.
    * [ ] Aggiungere i messaggi inviati a una lista locale.
    * [ ] Visualizzare la lista dei messaggi (scambiati localmente, senza vera comunicazione esterna) nella `ChatDisplayArea` (usare `LazyColumn`).

---

## Fase 7: Esperienza Utente - Funzionalità Aggiuntive (Idee Utente)

* [ ] **Salvataggio e Riproduzione Partite:**
    * [ ] **Setup Room DB:**
        * [ ] Aggiungere dipendenze Room.
        * [ ] Definire `Entity` per la partita (ID, data, giocatori, sequenza mosse).
        * [ ] Definire `DAO` (Data Access Object).
        * [ ] Creare istanza `Database`.
    * [ ] **Logica di Salvataggio:**
        * [ ] Opzione per salvare la partita corrente (es. dopo la fine).
        * [ ] Salvare la sequenza delle mosse nel database.
    * [ ] **UI per Partite Salvate:**
        * [ ] Schermata per elencare le partite salvate (letta da Room).
        * [ ] Opzione per selezionare una partita da rivedere.
    * [ ] **Logica di Riproduzione:**
        * [ ] Caricare la sequenza di mosse e visualizzarle una per una sulla tavola (con pulsanti avanti/indietro).
* [ ] **Dichiarazione Vocale Mosse (TTS - per futuro AI):**
    * [ ] Integrare il motore Text-to-Speech di Android.
    * [ ] Creare una funzione che accetti una stringa (mossa) e la pronunci.
    * [ ] (Opzionale) Aggiungere un'impostazione per abilitare/disabilitare il TTS.
* [ ] **Menù Scelta Avversario (UI e Logica Base):**
    * [ ] Aggiungere una sezione nelle impostazioni o prima di iniziare una nuova partita.
    * [ ] UI per selezionare un "tipo" di avversario (es. "Umano", "AI Facile" - per ora l'AI Facile sarà solo un placeholder o il giocatore "fantasma").
    * [ ] Salvare la scelta.

---

## Fase 8: Personalizzazione Avanzata e Rifiniture

* [ ] **Sfondo Tavola da Galleria Dispositivo:**
    * [ ] Implementare la selezione di un'immagine dalla galleria del telefono (`ActivityResultContracts.PickVisualMedia`).
    * [ ] Gestire i permessi necessari (se richiesto).
    * [ ] Usare l'immagine selezionata come sfondo per la `GameBoardArea`.
    * [ ] Salvare il riferimento all'immagine scelta (es. URI come stringa).

---

## Fase 9: Integrazione IA (Futuro - Logica Avanzata)

* [ ] **Definizione Interfaccia Modulo IA:** Creare un'interfaccia o classe base per la logica dell'IA.
* [ ] **Sviluppo IA Locale (Semplice):** Implementare una prima versione di IA molto semplice (es. mosse casuali valide).
* [ ] **Studio API Gemini (o simili):**
    * [ ] Ricerca e comprensione di come interagire con le API scelte per l'IA.
    * [ ] Setup chiamate di rete (es. con Ktor o Retrofit).
    * [ ] Gestione risposte e integrazione nel gioco per:
        * [ ] Validazione mosse (se desiderato).
        * [ ] Generazione mosse AI.
        * [ ] (Molto futuro) Simulazione commenti giocatori.

---

## Fase 10: Funzionalità Aggiuntive (Idee Proposte da Gemini - Per Apprendimento e Divertimento)

* [ ] **Animazioni Avanzate:** Migliorare le animazioni delle pedine, transizioni tra schermate.
* [ ] **State Management Avanzato:** Rifinire la gestione dello stato con `Flows`, `Sealed Classes` se non già pienamente adottati.
* [ ] **Coroutines:** Ottimizzare l'uso di coroutines per operazioni asincrone.
* [ ] **Sistema di Suggerimenti Mosse:** Evidenziare le mosse possibili per la pedina selezionata.
* [ ] **Modalità "Pass and Play" (Due Giocatori Umani):** Se non già coperta dalla gestione turni base.
* [ ] **Effetti Sonori:** Aggiungere suoni per movimenti, catture, ecc.
* [ ] **Internazionalizzazione (i18n):** Supporto per più lingue (es. Inglese).
* [ ] **Statistiche di Gioco Semplici:** Tracciare vittorie/sconfitte.

---

Questo piano è una guida; sentiti libero di adattarlo man mano che procedi e acquisisci nuove conoscenze! In bocca al lupo con lo sviluppo di `damaAI`!