package com.example.livrodecantos.domain.cifra

import com.example.livrodecantos.model.cifra.AcordePosicionado
import com.example.livrodecantos.model.cifra.CifraCanto
import com.example.livrodecantos.model.cifra.LinhaCifra
import com.example.livrodecantos.model.cifra.PreferenciaAcidentes.BEMOIS
import com.example.livrodecantos.model.cifra.PreferenciaAcidentes.SUSTENIDOS
import org.junit.Assert.*
import org.junit.Test

class CifraLatinaTest {
    @Test
    fun reconheceTodasAsNotasLatinasNasDuasPreferencias() {
        val notas = listOf("Do", "Do#", "Reb", "Re", "Re#", "Mib", "Mi", "Fa",
            "Fa#", "Solb", "Sol", "Sol#", "Lab", "La", "La#", "Sib", "Si")
        val sustenidos = listOf("Do", "Do#", "Do#", "Re", "Re#", "Re#", "Mi", "Fa",
            "Fa#", "Fa#", "Sol", "Sol#", "Sol#", "La", "La#", "La#", "Si")
        val bemois = listOf("Do", "Reb", "Reb", "Re", "Mib", "Mib", "Mi", "Fa",
            "Solb", "Solb", "Sol", "Lab", "Lab", "La", "Sib", "Sib", "Si")
        notas.indices.forEach { i ->
            assertEquals(sustenidos[i], CifraTranspositor.transpor(notas[i], 0, SUSTENIDOS))
            assertEquals(bemois[i], CifraTranspositor.transpor(notas[i], 0, BEMOIS))
        }
    }

    @Test
    fun preservaSufixosSemConverterMenor() {
        listOf("-", "-7", "m", "7", "m7", "maj7", "7M", "9", "m9", "sus2",
            "sus4", "dim", "aug", "add9", "°", "º", "+", "7M(9)", "7(b9,#11)", "6/9").forEach {
            assertEquals("Re$it", CifraTranspositor.transpor("Do$it", 2))
            assertEquals("Re$it/Fa#", CifraTranspositor.transpor("Do$it/Mi", 2))
        }
        assertEquals("Em", CifraTranspositor.transpor("Dm", 2))
        assertEquals("Mi-", CifraTranspositor.transpor("Re-", 2))
    }

    @Test
    fun cifraPreservaLetraPosicoesOriginalENotacaoPorAcorde() {
        val linha = LinhaCifra("  Glória!\tAleluia 🙏  ", listOf(
            AcordePosicionado(2, "Re-"),
            AcordePosicionado(10, "Sib/Re"),
            AcordePosicionado(18, "Dm")
        ))
        val original = CifraCanto(1, "Re-", SUSTENIDOS, listOf(linha))
        val resultado = CifraTranspositor.transpor(original, 2)
        assertNotSame(original, resultado)
        assertEquals(original.tomOriginal, resultado.tomOriginal)
        assertEquals(original.cantoId, resultado.cantoId)
        assertEquals(original.preferenciaAcidentes, resultado.preferenciaAcidentes)
        assertArrayEquals(linha.texto.toByteArray(Charsets.UTF_8),
            resultado.linhas.single().texto.toByteArray(Charsets.UTF_8))
        assertEquals(linha.acordes.map { it.posicao }, resultado.linhas.single().acordes.map { it.posicao })
        assertEquals(listOf("Mi-", "Do/Mi", "Em"), resultado.linhas.single().acordes.map { it.acorde })
        assertEquals(listOf("Re-", "Sib/Re", "Dm"), original.linhas.single().acordes.map { it.acorde })
    }

    @Test
    fun tomAtualMantemNotacaoLatina() {
        assertEquals("Mi-", CifraTranspositor.tomAtual("Re-", 2))
        assertEquals("Reb", CifraTranspositor.tomAtual("Do", 1, BEMOIS))
    }

    @Test
    fun capotrasteMantemNotacaoLatinaESufixos() {
        assertEquals("Re-", CifraTranspositor.formaComCapotraste("Mi-", 2))
        assertEquals("Sib", CifraTranspositor.formaComCapotraste("Do", 2, BEMOIS))
        assertEquals("Re/Fa#", CifraTranspositor.formaComCapotraste("Mi/Sol#", 2))
        for (casa in 0..24) {
            val forma = CifraTranspositor.formaComCapotraste("Mi-7/Sol#", casa)
            assertEquals("Mi-7/Sol#", CifraTranspositor.transpor(forma, casa))
        }
    }

    @Test
    fun formasNaFichaCombinamTomEscolhidoECapotraste() {
        assertEquals("Mi-", CifraTranspositor.transporParaForma("Re-", 2, 0, BEMOIS))
        assertEquals("Re-", CifraTranspositor.transporParaForma("Re-", 2, 2, BEMOIS))
        assertEquals("Sib", CifraTranspositor.transporParaForma("Sib", 2, 2, BEMOIS))
        assertEquals("La", CifraTranspositor.transporParaForma("La", 2, 2, BEMOIS))
        assertEquals("Do-", CifraTranspositor.transporParaForma("Re-", 0, 2, BEMOIS))
        assertEquals("Lab", CifraTranspositor.transporParaForma("Sib", 0, 2, BEMOIS))
        assertEquals("Sol", CifraTranspositor.transporParaForma("La", 0, 2, BEMOIS))
        assertEquals("Re/Fa#", CifraTranspositor.transporParaForma("Re/Fa#", 2, 2, BEMOIS))
        assertEquals("Mib/Sol", CifraTranspositor.transporParaForma("Re/Fa#", 3, 2, BEMOIS))
    }
}
