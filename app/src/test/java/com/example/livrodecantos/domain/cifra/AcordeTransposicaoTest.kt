package com.example.livrodecantos.domain.cifra

import com.example.livrodecantos.model.cifra.PreferenciaAcidentes
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class AcordeTransposicaoTest(
    private val acorde: String,
    private val semitons: Int,
    private val preferencia: PreferenciaAcidentes,
    private val esperado: String
) {
    @Test
    fun transpoeFundamentalEBaixoPreservandoSufixo() {
        assertEquals(esperado, CifraTranspositor.transpor(acorde, semitons, preferencia))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} + {1} ({2}) = {3}")
        fun casos(): List<Array<Any>> {
            val s = PreferenciaAcidentes.SUSTENIDOS
            val b = PreferenciaAcidentes.BEMOIS
            return listOf(
                arrayOf("C", 2, s, "D"),
                arrayOf("Am", 2, s, "Bm"),
                arrayOf("F", 2, s, "G"),
                arrayOf("G7", 2, s, "A7"),
                arrayOf("C#", 1, s, "D"),
                arrayOf("C#m", 1, s, "Dm"),
                arrayOf("Bb", 1, s, "B"),
                arrayOf("Bb", 2, s, "C"),
                arrayOf("Am7", 2, s, "Bm7"),
                arrayOf("Cmaj7", 2, s, "Dmaj7"),
                arrayOf("Csus4", 2, s, "Dsus4"),
                arrayOf("F#m7", 2, s, "G#m7"),
                arrayOf("D/F#", 2, s, "E/G#"),
                arrayOf("C/E", 2, s, "D/F#"),
                arrayOf("Bb/D", 2, s, "C/E"),
                arrayOf("C/E", -2, b, "Bb/D"),
                arrayOf("C", -2, b, "Bb"),
                arrayOf("A", -2, s, "G"),
                arrayOf("C", -1, s, "B"),
                arrayOf("B", 1, s, "C"),
                arrayOf("C", 14, s, "D"),
                arrayOf("C", -13, s, "B"),
                arrayOf("C", 1, s, "C#"),
                arrayOf("C", 1, b, "Db"),
                arrayOf("C/E", 1, b, "Db/F"),
                arrayOf("D/F#", 2, b, "E/Ab"),
                arrayOf("C7M(9)", 2, s, "D7M(9)"),
                arrayOf("C7M(9)/E", 2, b, "D7M(9)/Gb"),
                arrayOf("C6/9", 2, s, "D6/9"),
                arrayOf("C", Int.MAX_VALUE, s, "G"),
                arrayOf("C", Int.MIN_VALUE, s, "E"),
                arrayOf("Db", 0, s, "C#"),
                arrayOf("C#", 12, b, "Db")
            )
        }
    }
}
