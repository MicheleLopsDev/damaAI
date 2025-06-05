# 🗺️ Roadmap del Progetto: damaAI

Questo documento delinea le funzionalità pianificate e le fasi di sviluppo per il progetto `damaAI`. Gli elementi verranno spuntati man mano che vengono completati.

---

**1.0 Fase 1: Struttura UI di Base & Setup Iniziale**

* **1.1 Configurazione Progetto:**
    * [x] 1.1.1 Creare un nuovo progetto Android Studio.
    * [x] 1.1.2 Nome app: `damaAI`.
    * [x] 1.1.3 Linguaggio: Kotlin.
    * [x] 1.1.4 Abilitare Jetpack Compose e Material 3.
    * [x] 1.1.5 Configurare il tema di base.
* **1.2 Schermata Principale:**
    * [x] 1.2.1 Utilizzare `Scaffold`.
    * [x] 1.2.2 Aggiungere `TopAppBar`.
    * [x] 1.2.3 Creare `Column` principale.
* **1.3 Aree Segnaposto Composable:**
    * [x] 1.3.1 `GameBoardArea`.
    * [x] 1.3.2 `ChatDisplayArea`.
    * [x] 1.3.3 `ChatInputArea`.
* **1.4 Testare Layout di Base:**
    * [x] Verificare che la struttura appaia correttamente.

**2.0 Fase 2: Rappresentazione Visiva della Tavola e delle Pedine**

* **2.1 `GameBoardArea` - Disegno Tavola:**
    * [x] 2.1.1 Implementare la griglia 8x8.
    * [x] 2.1.2 Applicare colori alternati per le caselle.
* **2.2 `Piece` Composable - Disegno Pedine:**
    * [x] 2.2.1 Creare un Composable/logica per una singola pedina.
    * [x] 2.2.2 Disegno iniziale: cerchi colorati.
    * [x] 2.2.3 Applicare un effetto "leggermente 3D".
* **2.3 Posizionamento Statico Pedine:**
    * [x] Mostrare le pedine per test visivi.

**3.0 Fase 3: Logica di Gioco di Base**

* **3.1 Strutture Dati per lo Stato del Gioco:**
    * [x] 3.1.1 Definire una struttura dati (`GameState`).
    * [x] 3.1.2 Definire classi/oggetti per le Pedine.
* **3.2 Interazione Utente di Base:**
    * [x] 3.2.1 Logica per selezionare una pedina.
    * [x] 3.2.2 Evidenziare visivamente la pedina selezionata.
* **3.3 Movimento Base Pedine:**
    * [x] 3.3.1 Logica per muovere la pedina (passo singolo in avanti).
    * [x] 3.3.2 Aggiornare lo stato della tavola e la UI.
* **3.4 Gestione Turni (Semplice):**
    * [x] Alternare il turno tra BIANCO e NERO.

**4.0 Fase 4: Menù di Base e Navigazione**

* [ ] **4.1 Setup Navigazione (Compose Navigation):**
    * [ ] 4.1.1 Integrare `NavHostController` e definire le rotte.
* [ ] **4.2 Schermata Impostazioni:**
    * [ ] 4.2.1 Creare `SettingsScreen` Composable.
    * [ ] 4.2.2 Aggiungere navigazione alla `SettingsScreen`.
* [ ] **4.3 Opzioni Generali nel Menù Impostazioni:**
    * [ ] 4.3.1 Cambio Tema (Light/Dark) e salvataggio preferenza.
    * [ ] 4.3.2 Schermata/Dialogo "Aiuto".
    * [ ] 4.3.3 Schermata/Dialogo "Crediti".
    * [ ] 4.3.4 Opzione "Esci dall'App".

**5.0 Fase 5: Personalizzazione Pedine e Tavola**

* [ ] **5.1 Struttura Menù Personalizzazione.**
* [ ] **5.2 Personalizzazione Pedine:**
    * [ ] 5.2.1 Selezione Texture/Colore Pedine (da risorse `drawable`).
* [ ] **5.3 Personalizzazione Tavola:**
    * [ ] 5.3.1 Selezione Texture Tavola (da risorse `drawable`).

**6.0 Fase 6: Logica di Gioco Avanzata e Interazioni Chat**

* [ ] **6.1 Logica di Gioco Completa:**
    * [ ] 6.1.1 Implementare cattura pedine.
    * [ ] 6.1.2 Implementare promozione a "Dama" e movimento Dame.
* [ ] **6.2 Rilevamento Fine Partita.**
* [ ] **6.3 Funzionalità Chat (Locale).**

**7.0 Fase 7: Esperienza Utente - Funzionalità Aggiuntive**

* [ ] **7.1 Salvataggio e Riproduzione Partite:**
    * [ ] 7.1.1 Setup Room DB.
* [ ] **7.2 Dichiarazione Vocale Mosse (TTS).**
* [ ] **7.3 Menù Scelta Avversario.**

**8.0 Fase 8: Personalizzazione Avanzata e Rifiniture**

* [ ] **8.1 Sfondo Tavola da Galleria Dispositivo.**

**9.0 Fase 9: Integrazione IA (Futuro)**

* [ ] **9.1 Definizione Interfaccia Modulo IA.**
* [ ] **9.2 Sviluppo IA Locale (Semplice).**
* [ ] **9.3 Studio API Gemini.**

**10.0 Fase 10: Funzionalità Aggiuntive (Post-Lancio)**

* [ ] 10.1 Animazioni Avanzate.
* [ ] 10.4 Sistema di Suggerimenti Mosse.
* [ ] 10.5 Modalità "Pass and Play".
* [ ] 10.6 Effetti Sonori.
* [ ] 10.7 Internazionalizzazione (i18n).
* [ ] 10.8 Statistiche di Gioco.