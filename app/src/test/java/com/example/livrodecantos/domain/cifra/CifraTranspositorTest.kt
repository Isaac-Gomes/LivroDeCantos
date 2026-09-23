package com.example.livrodecantos.domain.cifra

import com.example.livrodecantos.model.cifra.AcordePosicionado
import com.example.livrodecantos.model.cifra.CifraCanto
import com.example.livrodecantos.model.cifra.LinhaCifra
import com.example.livrodecantos.model.cifra.PreferenciaAcidentes.BEMOIS
import com.example.livrodecantos.model.cifra.PreferenciaAcidentes.SUSTENIDOS
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class CifraTranspositorTest {
    @Test
    fun preservaTodosOsSufixosLiteralmente() {
        listOf("m", "7", "m7", "maj7", "7M", "9", "m9", "sus2", "sus4",
            "dim", "aug", "add9", "°", "º", "+", "7M(9)", "7(b9,#11)", "6/9").forEach {
            assertEquals("D$it", CifraTranspositor.transpor("C$it", 2))
            assertEquals("D$it/F#", CifraTranspositor.transpor("C$it/E", 2))
        }
    }

    @Test
    fun aceitaTodasAsNotasEEnarmonias() {
        val notas = listOf("C", "C#", "Db", "D", "D#", "Eb", "E", "F", "F#",
            "Gb", "G", "G#", "Ab", "A", "A#", "Bb", "B")
        val esperados = listOf("C", "C#", "C#", "D", "D#", "D#", "E", "F", "F#",
            "F#", "G", "G#", "G#", "A", "A#", "A#", "B")
        notas.zip(esperados).forEach { (nota, esperado) ->
            assertEquals(esperado, CifraTranspositor.transpor(nota, 0))
        }
    }

    @Test
    fun cifraCompletaPreservaBytesPosicoesMetadadosEOriginal() {
        val original = exemplo()
        val copiaAnterior = original.copy(linhas = original.linhas.map {
            it.copy(acordes = it.acordes.map { acorde -> acorde.copy() })
        })
        val transposta = CifraTranspositor.transpor(original, 2)
        assertNotSame(original, transposta)
        assertNotSame(original.linhas, transposta.linhas)
        assertEquals(copiaAnterior, original)
        assertEquals(original.cantoId, transposta.cantoId)
        assertEquals(original.tomOriginal, transposta.tomOriginal)
        assertEquals(original.preferenciaAcidentes, transposta.preferenciaAcidentes)
        original.linhas.zip(transposta.linhas).forEach { (antes, depois) ->
            assertArrayEquals(antes.texto.toByteArray(Charsets.UTF_8), depois.texto.toByteArray(Charsets.UTF_8))
            assertEquals(antes.acordes.map { it.posicao }, depois.acordes.map { it.posicao })
            assertNotSame(antes, depois)
            assertNotSame(antes.acordes, depois.acordes)
        }
        assertEquals(listOf("D", "A"), transposta.linhas[0].acordes.map { it.acorde })
        assertEquals(listOf("E/G#"), transposta.linhas[2].acordes.map { it.acorde })
        assertEquals(listOf("C", "G"), original.linhas[0].acordes.map { it.acorde })
    }

    @Test
    fun cifraUsaSuaPreferenciaDeAcidentes() {
        val transposta = CifraTranspositor.transpor(exemplo().copy(preferenciaAcidentes = BEMOIS), -2)
        assertEquals(listOf("Bb", "F"), transposta.linhas[0].acordes.map { it.acorde })
    }

    @Test
    fun cifraVaziaPodeSerTransposta() {
        val original = CifraCanto(1, "C", SUSTENIDOS, emptyList())
        val resultado = CifraTranspositor.transpor(original, 5)
        assertEquals(original, resultado)
        assertNotSame(original, resultado)
    }

    @Test
    fun calculaTomAtualSemPerderModoMenor() {
        assertEquals("E", CifraTranspositor.tomAtual("D", 2))
        assertEquals("Bm", CifraTranspositor.tomAtual("Am", 2))
        assertEquals("Db", CifraTranspositor.tomAtual("C", 1, BEMOIS))
    }

    @Test
    fun calculaFormaComCapotraste() {
        assertEquals("D", CifraTranspositor.formaComCapotraste("E", 2))
        assertEquals("Bm7", CifraTranspositor.formaComCapotraste("C#m7", 2))
        assertEquals("D/F#", CifraTranspositor.formaComCapotraste("E/G#", 2))
        assertEquals("Bb", CifraTranspositor.formaComCapotraste("C", 2, BEMOIS))
    }

    @Test
    fun capotrasteZeroEOitavas() {
        assertEquals("E", CifraTranspositor.formaComCapotraste("E", 0))
        assertEquals("E", CifraTranspositor.formaComCapotraste("E", 12))
        assertEquals("D", CifraTranspositor.formaComCapotraste("E", 14))
    }

    @Test
    fun formaMaisCapotrasteProduzSomDesejado() {
        for (casa in 0..24) {
            val forma = CifraTranspositor.formaComCapotraste("E7/G#", casa)
            assertEquals("E7/G#", CifraTranspositor.transpor(forma, casa))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejeitaCasaNegativa() {
        CifraTranspositor.formaComCapotraste("E", -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejeitaAcordeSemFundamental() {
        CifraTranspositor.transpor("H7", 2)
    }

    @Test
    fun modeloPodeSerSerializadoSemAndroid() {
        val original = exemplo()
        val bytes = ByteArrayOutputStream()
        ObjectOutputStream(bytes).use { it.writeObject(original) }
        val restaurado = ObjectInputStream(ByteArrayInputStream(bytes.toByteArray())).use { it.readObject() }
        assertEquals(original, restaurado)
    }

    private fun exemplo() = CifraCanto(
        cantoId = 1,
        tomOriginal = "C",
        preferenciaAcidentes = SUSTENIDOS,
        linhas = listOf(
            LinhaCifra("Ressuscitou", listOf(AcordePosicionado(0, "C"), AcordePosicionado(6, "G"))),
            LinhaCifra(""),
            LinhaCifra("  Glória!\tAleluia — a\u0301 🙏  ", listOf(AcordePosicionado(2, "D/F#")))
        )
    )
}
