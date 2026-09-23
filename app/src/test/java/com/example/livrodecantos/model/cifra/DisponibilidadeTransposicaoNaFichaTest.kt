package com.example.livrodecantos.model.cifra

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DisponibilidadeTransposicaoNaFichaTest {
    private val cifra = CifraCanto(
        cantoId = 10,
        tomOriginal = "Re-",
        preferenciaAcidentes = PreferenciaAcidentes.BEMOIS,
        linhas = emptyList()
    )
    private val multipagina = MapeamentoAcordesFicha(
        cantoId = 10,
        completo = true,
        acordes = listOf(
            AcordeNaFicha(1, "Re-", .1f, .1f, .04f, .02f),
            AcordeNaFicha(2, "Sol", .2f, .2f, .04f, .02f)
        )
    )

    @Test
    fun habilitaSomenteMapaCompletoCobrindoPaginasDaFicha() {
        assertTrue(DisponibilidadeTransposicaoNaFicha.estaPronta(cifra, multipagina, listOf(1, 2)))
        assertFalse(DisponibilidadeTransposicaoNaFicha.estaPronta(cifra, multipagina, listOf(1)))
        assertFalse(DisponibilidadeTransposicaoNaFicha.estaPronta(cifra, multipagina.copy(completo = false), listOf(1, 2)))
        assertFalse(DisponibilidadeTransposicaoNaFicha.estaPronta(cifra, null, listOf(1, 2)))
    }

    @Test
    fun selecionaSomenteAcordesDaPaginaCorrespondente() {
        assertEquals(listOf("Re-"), multipagina.acordesDaPagina(1).map { it.acorde })
        assertEquals(listOf("Sol"), multipagina.acordesDaPagina(2).map { it.acorde })
        assertTrue(multipagina.acordesDaPagina(3).isEmpty())
    }
}
