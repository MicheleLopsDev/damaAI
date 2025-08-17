package io.github.luposolitario.damaai.game_logic

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

internal class MotoreDiGiocoTest {

    private lateinit var motore: MotoreDiGioco

    @Before
    fun setUp() {
        motore = MotoreDiGioco()
    }

    @Test
    fun testStatoInizialeScacchiera() {
        // Assert
        assertEquals("Il primo turno deve essere del BIANCO", Colore.BIANCO, motore.turnoCorrente)
        // Aggiungi altri controlli se necessario
    }

    @Test
    fun testPromozioneADamone() {
        // Arrange
        motore.scacchiera.impostaScacchieraPerTest(mapOf(
            Posizione(1, 0) to Pezzo(Colore.BIANCO, TipoPezzo.PEDINA) // Pedina bianca in A2
        ))
        motore.turnoCorrente = Colore.BIANCO
        val mossaDiPromozione = Mossa(Posizione(1, 0), Posizione(0, 1)) // A2 -> B1

        // Act
        val mossaRiuscita = motore.eseguiMossa(mossaDiPromozione)
        val pezzoPromosso = motore.scacchiera.pezzoA(Posizione(0, 1))

        // Assert
        assertTrue("La mossa di promozione dovrebbe essere valida", mossaRiuscita)
        assertNotNull("Deve esserci un pezzo nella casella di arrivo", pezzoPromosso)
        assertEquals("Il pezzo promosso deve essere un DAMONE", TipoPezzo.DAMONE, pezzoPromosso?.tipo)
    }

    // --- NUOVI TEST PER LE REGOLE DELLA DAMA ITALIANA ---

    @Test
    fun testRegolaCatturaSempliceObbligatoria() {
        // Arrange: Una pedina bianca può catturare o fare una mossa semplice.
        motore.scacchiera.impostaScacchieraPerTest(mapOf(
            Posizione(4, 3) to Pezzo(Colore.BIANCO), // D5: Pedina bianca che può catturare
            Posizione(3, 4) to Pezzo(Colore.NERO),   // E4: Pedina nera da catturare
            Posizione(6, 1) to Pezzo(Colore.BIANCO)  // B7: Pedina bianca con mossa semplice
        ))
        motore.turnoCorrente = Colore.BIANCO

        // Act
        val mosse = motore.mosseValideDisponibili()

        // Assert
        assertEquals("Deve esserci solo una mossa disponibile (la cattura)", 1, mosse.size)
        val mossaAttesa = Mossa(Posizione(4, 3), Posizione(2, 5), Posizione(3, 4))
        assertTrue("La mossa disponibile deve essere la cattura D5->F3", mosse.contains(mossaAttesa))
    }

    @Test
    fun testCatturaObbligatoria_PedinaDeveCatturarePiuPezzi() {
        // Arrange: Una pedina bianca può catturare 1 pezzo o 2 pezzi in sequenza.
        motore.scacchiera.impostaScacchieraPerTest(mapOf(
            Posizione(5, 0) to Pezzo(Colore.BIANCO), // A6: Pedina bianca
            Posizione(4, 1) to Pezzo(Colore.NERO),   // B5: Nera 1
            Posizione(2, 3) to Pezzo(Colore.NERO),   // D3: Nera 2
            Posizione(4, 5) to Pezzo(Colore.NERO)    // F5: Nera 3 (cattura singola alternativa)
        ))
        motore.turnoCorrente = Colore.BIANCO

        // La pedina in A6 può fare A6->C4 (mangiando B5) e poi C4->E2 (mangiando D3).
        // In alternativa, un'altra pedina potrebbe mangiare F5, ma è una cattura singola.

        // Act
        val mosse = motore.mosseValideDisponibili()
        println("Mosse valide trovate: $mosse")

        // Assert
        assertEquals("Deve esserci solo la mossa che inizia la cattura multipla", 1, mosse.size)
        val mossaCorretta = mosse.first()
        // La mossa iniziale della sequenza migliore è A6 -> C4
        assertEquals("La mossa deve partire da A6", Posizione(5, 0), mossaCorretta.partenza)
    }

    @Test
    fun testCatturaObbligatoria_DamaHaPrioritaSullaPedina() {
        // Arrange: Una Dama e una Pedina possono entrambe catturare 1 pezzo.
        motore.scacchiera.impostaScacchieraPerTest(mapOf(
            // La Dama bianca può catturare
            Posizione(4, 3) to Pezzo(Colore.BIANCO, TipoPezzo.DAMONE), // D5: Dama
            Posizione(3, 4) to Pezzo(Colore.NERO),                     // E4: Nera da catturare per la Dama
            // La Pedina bianca può catturare
            Posizione(6, 1) to Pezzo(Colore.BIANCO),                   // B7: Pedina
            Posizione(5, 2) to Pezzo(Colore.NERO)                      // C6: Nera da catturare per la Pedina
        ))
        motore.turnoCorrente = Colore.BIANCO

        // Act
        val mosse = motore.mosseValideDisponibili()

        // Assert
        assertEquals("Deve esserci solo una mossa disponibile (quella della Dama)", 1, mosse.size)
        val mossaDellaDama = Mossa(Posizione(4, 3), Posizione(2, 5), Posizione(3, 4))
        assertTrue("La mossa valida deve essere quella eseguita dalla Dama", mosse.contains(mossaDellaDama))
    }

    @Test
    fun testCatturaObbligatoria_PedinaNonCatturaDama() {
        // Arrange: Una pedina bianca è in presa di una Dama Nera.
        motore.scacchiera.impostaScacchieraPerTest(mapOf(
            Posizione(4, 3) to Pezzo(Colore.BIANCO),                   // D5: Pedina bianca
            Posizione(3, 4) to Pezzo(Colore.NERO, TipoPezzo.DAMONE)    // E4: Dama Nera
        ))
        motore.turnoCorrente = Colore.BIANCO

        // Act
        val mosse = motore.mosseValideDisponibili()

        // Assert
        assertTrue("Non devono esserci mosse di cattura per la pedina bianca",
            mosse.none { it.posizionePezzoCatturato != null })
    }
}