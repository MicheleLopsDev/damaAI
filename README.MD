# **damaAI**

### **La Dama Classica con un'Anima d'Intelligenza Artificiale**

**damaAI** non è il solito gioco della Dama. È un'esperienza che unisce la strategia senza tempo di uno dei giochi da tavolo più amati al mondo con la potenza dell'intelligenza artificiale generativa. Sfida una galleria di avversari unici, basati su celebri figure storiche, ognuno con un proprio stile di gioco, una personalità definita e un modo di dialogare unico.

## **🌟 Feature Principali**

* **Motore di Gioco Robusto**: Gioca alla Dama Italiana con regole complete, incluse le catture obbligatorie e la promozione a Damone.  
* **Modalità di Gioco Flessibili**:  
  * **Giocatore vs IA**: Metti alla prova le tue abilità contro avversari controllati dall'IA.  
  * **Giocatore vs Giocatore**: Gioca una partita classica contro un amico sullo stesso dispositivo, con la possibilità di inserire le mosse anche testualmente.  
* **Avversari IA con Personalità Uniche**: La vera anima di **damaAI**. Invece di un bot generico, affronterai:  
  * **Leonardo da Vinci**, che analizza il gioco come un'opera d'arte.  
  * **William Shakespeare**, che commenta la partita con linguaggio poetico.  
  * **Napoleone Bonaparte**, che tratta la scacchiera come un campo di battaglia.  
  * **Marie Curie**, che affronta la sfida con rigore scientifico.  
  * ...e molte altre figure storiche, ciascuna con uno stile di dialogo e una strategia unici\!  
* **IA Generativa Streaming**: Grazie all'integrazione con un modello linguistico avanzato (basato su **Google Gemma**), i commenti degli avversari appaiono in tempo reale, carattere per carattere, creando un'esperienza di chat dinamica e coinvolgente.  
* **Ampia Personalizzazione**:  
  * Scegli il tuo avversario da una galleria organizzata per nazione.  
  * Personalizza l'aspetto della scacchiera con diversi temi.  
  * Regola il livello di difficoltà dell'IA.  
* **Interfaccia Moderna**: L'app è costruita interamente con **Jetpack Compose** e **Material 3**, offrendo un'esperienza utente pulita, reattiva e gradevole, con pieno supporto al tema scuro.

## **🛠️ Tech Stack & Architettura**

**damaAI** è costruito seguendo le più moderne best practice per lo sviluppo Android, garantendo un'applicazione robusta, manutenibile e performante.

* **Linguaggio**: 100% **Kotlin**.  
* **UI**: **Jetpack Compose** per un'interfaccia utente dichiarativa e reattiva, con **Material 3**.  
* **Architettura**: Basata su **MVVM** (Model-View-ViewModel) per una chiara separazione delle responsabilità.  
* **Asincronicità**: Utilizzo intensivo di **Kotlin Coroutines** e **Flow** per gestire le operazioni in background e i flussi di dati in tempo reale (come lo streaming della chat).  
* **Persistenza Dati**: **Proto DataStore** per salvare le preferenze dell'utente in modo efficiente e sicuro.  
* **Gestione Task in Background**: **WorkManager** per gestire il download del modello di intelligenza artificiale.  
* **Iniezione delle Dipendenze**: Gestita tramite **ViewModelFactory** manuali per mantenere il codice semplice e controllato.

## **🚀 Come Eseguire il Progetto**

Per compilare ed eseguire il progetto localmente, segui questi semplici passaggi:

1. **Clona il Repository**  
   Bash  
   git clone https://github.com/TUO\_USERNAME/damaAI.git

2. **Apri in Android Studio**  
   * Avvia Android Studio (versione Hedgehog o successiva consigliata).  
   * Seleziona "Open an existing Project".  
   * Naviga fino alla cartella del progetto clonata e aprila.  
3. **Sincronizza Gradle**  
   * Attendi che Android Studio completi la sincronizzazione dei file Gradle e scarichi tutte le dipendenze necessarie.  
4. **Esegui l'Applicazione**  
   * Seleziona un emulatore o un dispositivo fisico e clicca sul pulsante "Run 'app'".

## **📄 Licenza**

Questo progetto è distribuito sotto la licenza MIT. Vedi il file LICENSE per maggiori dettagli.