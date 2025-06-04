# Piano di Sviluppo: Dama AI 🐴 (Versione con Numerazione)

Questo documento delinea le fasi di sviluppo per l'applicazione Android "Dama AI", un gioco di dama open-source focalizzato sull'apprendimento di Kotlin e Jetpack Compose.

## Obiettivi Principali del Progetto:
* Creare un gioco di dama funzionante e divertente.
* Utilizzare Kotlin e Jetpack Compose per l'intero sviluppo.
* Implementare un'interfaccia utente moderna con Material 3.
* Offrire opzioni di personalizzazione per pedine e tavola.
* Fornire un'esperienza educativa per lo sviluppatore.
* Mantenere l'app leggera e performante.

---

**1.0 Fase 1: Struttura UI di Base & Setup Iniziale**

* **1.1 Configurazione Progetto:**
    * [ ] 1.1.1 Creare un nuovo progetto Android Studio.
    * [ ] 1.1.2 Nome app: `damaAI` (configurare in `strings.xml`).
    * [ ] 1.1.3 Linguaggio: Kotlin.
    * [ ] 1.1.4 Abilitare Jetpack Compose e dipendenze Material 3.
    * [ ] 1.1.5 Configurare il tema Material 3 di base (`DamaAITheme` in `ui.theme`).
* **1.2 Schermata Principale (`MainActivity.kt` -> `DamaAIScreen`):**
    * [ ] 1.2.1 Utilizzare `Scaffold` di Material 3.
    * [ ] 1.2.2 Aggiungere una `TopAppBar` con il titolo "damaAI".
    * [ ] 1.2.3 Creare `Column` principale per disporre gli elementi verticalmente.
* **1.3 Aree Segnaposto Composable:**
    * [ ] 1.3.1 `GameBoardArea`: Un `Box` quadrato segnaposto.
    * [ ] 1.3.2 `ChatDisplayArea`: Un `Box` o `Column` segnaposto.
    * [ ] 1.3.3 `ChatInputArea`: Un `Row` segnaposto con `TextField` e `Button`.
* **1.4 Testare Layout di Base:** Verificare che la struttura appaia correttamente.

**2.0 Fase 2: Rappresentazione Visiva della Tavola e delle Pedine**

* **2.1 `GameBoardArea` - Disegno Tavola:**
    * [ ] 2.1.1 Implementare la griglia 8x8 della dama (es. usando Compose `Canvas` o `LazyVerticalGrid`).
    * [ ] 2.1.2 Applicare colori alternati per le caselle.
* **2.2 `Piece` Composable - Disegno Pedine:**
    * [ ] 2.2.1 Creare un Composable per rappresentare una singola pedina.
    * [ ] 2.2.2 Disegno iniziale: cerchi colorati semplici (es. bianchi/neri) usando `Canvas`.
    * [ ] 2.2.3 Applicare un effetto "leggermente 3D" (es. usando `Modifier.shadow()` o disegnando un'ombra/sfumatura direttamente su `Canvas`).
* **2.3 Posizionamento Statico Pedine:** Mostrare alcune pedine in

## Licenza

Distribuito con licenza [MIT](https://opensource.org/licenses/MIT).