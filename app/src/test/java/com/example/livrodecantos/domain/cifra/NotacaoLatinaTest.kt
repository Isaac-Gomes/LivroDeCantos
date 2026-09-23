package com.example.livrodecantos.domain.cifra

import com.example.livrodecantos.model.cifra.PreferenciaAcidentes
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class NotacaoLatinaTest(
    private val acorde: String,
    private val semitons: Int,
    private val preferencia: PreferenciaAcidentes,
    private val esperado: String
) {
    @Test
    fun transpoeMantendoNotacaoLatina() {
        assertEquals(esperado, CifraTranspositor.transpor(acorde, semitons, preferencia))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} + {1} ({2}) = {3}")
        fun casos(): List<Array<Any>> {
            val s = PreferenciaAcidentes.SUSTENIDOS
            val b = PreferenciaAcidentes.BEMOIS
            return listOf(
                arrayOf("Re-", 2, s, "Mi-"),
                arrayOf("Mi-", -2, s, "Re-"),
                arrayOf("La-", 2, s, "Si-"),
                arrayOf("Sib", 2, s, "Do"),
                arrayOf("La", 2, s, "Si"),
                arrayOf("Do", 1, s, "Do#"),
                arrayOf("Do", 1, b, "Reb"),
                arrayOf("Si", 1, s, "Do"),
                arrayOf("Do", -1, s, "Si"),
                arrayOf("Re/Fa#", 2, s, "Mi/Sol#"),
                arrayOf("Do/Mi", 2, s, "Re/Fa#"),
                arrayOf("Sib/Re", 2, s, "Do/Mi"),
                arrayOf("Fa#-", 2, s, "Sol#-"),
                arrayOf("Sol7", 2, s, "La7"),
                arrayOf("Re-7", 2, s, "Mi-7"),
                arrayOf("Do7M", 2, s, "Re7M"),
                arrayOf("Fa#-7", 2, s, "Sol#-7"),
                arrayOf("Re/Fa#", 2, b, "Mi/Lab"),
                arrayOf("Do/Mi", -2, b, "Sib/Re"),
                arrayOf("Do7M(9)/Mi", 2, b, "Re7M(9)/Solb"),
                arrayOf("Do6/9", 2, s, "Re6/9"),
                arrayOf("Do", 14, s, "Re"),
                arrayOf("Do", -13, s, "Si"),
                arrayOf("Reb", 0, s, "Do#"),
                arrayOf("Sol#", 12, b, "Lab"),
                arrayOf("Do", Int.MAX_VALUE, s, "Sol"),
                arrayOf("Do", Int.MIN_VALUE, s, "Mi")
            )
        }
    }
}
