// In MotoreDiGiocoTest.kt

package io.github.luposolitario.damaai.game_logic // Assicurati che il package sia corretto

import io.github.luposolitario.damaai.game_logic.*
// Gli import di Assertions cambiano
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

internal class MotoreDiGiocoTest {

    private lateinit var motore: MotoreDiGioco

    // @BeforeEach diventa @Before
    @Before
    fun setUp() {
        motore = MotoreDiGioco()
    }

    // I nomi dei test ora sono in camelCase senza spazi
    @Test
    fun testStatoInizialeScacchiera() {
        // Arrange
        // Act
        val pezzoInA1 = motore.scacchiera.pezzoA(Posizione(0, 0))
        val pezzoInC7 = motore.scacchiera.pezzoA(Posizione(6, 2))
        val casellaVuota = motore.scacchiera.pezzoA(Posizione(4, 3))

        // Assert
        assertEquals("Il primo turno deve essere del BIANCO", Colore.BIANCO, motore.turnoCorrente)
        assertNull("La casella A1 deve essere vuota", pezzoInA1)
        assertNotNull("La casella C7 deve contenere un pezzo", pezzoInC7)
        assertEquals("Il pezzo in C7 deve essere BIANCO", Colore.BIANCO, pezzoInC7?.colore)
        assertNull("La casella D5 deve essere vuota", casellaVuota)
    }

    @Test
    fun testRegolaCatturaObbligatoria() {
        // Arrange
        motore.scacchiera.impostaScacchieraPerTest(mapOf(
            Posizione(4, 3) to Pezzo(Colore.BIANCO, TipoPezzo.PEDINA), // Pedina bianca in D5
            Posizione(3, 4) to Pezzo(Colore.NERO, TipoPezzo.PEDINA),  // Pedina nera in E4
            Posizione(4, 1) to Pezzo(Colore.BIANCO, TipoPezzo.PEDINA)  // Pedina bianca in B5 con mossa semplice
        ))
        motore.turnoCorrente = Colore.BIANCO

        // Act
        val mosse = motore.mosseValideDisponibili()
        println("Mosse valide trovate dal motore: $mosse")

        // Assert
        assertEquals("Deve esserci solo una mossa disponibile (la cattura)", 1, mosse.size)
        val mossaAttesa = Mossa(Posizione(4, 3), Posizione(2, 5), Posizione(3, 4))
        assertEquals("La mossa disponibile deve essere la cattura D5->F3", mossaAttesa, mosse.first())
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
}