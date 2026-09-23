package com.example.livrodecantos.model.cifra

import org.junit.Assert.assertEquals
import org.junit.Test

class ConfiguracaoCifraTest {
    @Test
    fun normalizaDeslocamentosSemAcumularValoresAbsurdos() {
        assertEquals(2, ConfiguracaoCifra.normalizarSemitons(14))
        assertEquals(-1, ConfiguracaoCifra.normalizarSemitons(-25))
        assertEquals(0, ConfiguracaoCifra.normalizarSemitons(12))
        assertEquals(0, ConfiguracaoCifra.normalizarSemitons(-12))
        assertEquals(11, ConfiguracaoCifra.normalizarSemitons(11))
        assertEquals(-11, ConfiguracaoCifra.normalizarSemitons(-11))
    }
}
