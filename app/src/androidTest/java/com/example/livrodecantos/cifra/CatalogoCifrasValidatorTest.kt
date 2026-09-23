package com.example.livrodecantos.cifra

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.livrodecantos.data.repository.AcordesFichaRepository
import com.example.livrodecantos.data.repository.CifraRepository
import com.example.livrodecantos.domain.cifra.CifraTranspositor
import com.example.livrodecantos.model.cifra.DisponibilidadeTransposicaoNaFicha
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Valida o catálogo empacotado sem depender de Room ou de uma tela Compose. */
@RunWith(AndroidJUnit4::class)
class CatalogoCifrasValidatorTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun catalogoCompletoTemReferenciasEMapasDeCifraValidos() = runBlocking {
        val cantos = lerArray("data/cantos.json")
        val fichas = lerArray("data/fichas.json")
        val idsCantos = (0 until cantos.length()).map { cantos.getJSONObject(it).getInt("id") }.toSet()
        assertEquals(idsCantos.size, cantos.length())

        val paginasPorCanto = mutableMapOf<Int, MutableSet<Int>>()
        for (indice in 0 until fichas.length()) {
            val ficha = fichas.getJSONObject(indice)
            val cantoId = ficha.getInt("cantoId")
            assertTrue("Ficha com cantoId inexistente: $cantoId", cantoId in idsCantos)
            context.assets.open(ficha.getString("arquivo")).close()
            paginasPorCanto.getOrPut(cantoId) { mutableSetOf() }.add(ficha.getInt("ordem"))
        }

        val cifras = CifraRepository(context).buscarTodos()
        val cifrasPorId = cifras.associateBy { it.cantoId }
        cifras.forEach { cifra ->
            assertTrue("Cifra para canto inexistente: ${cifra.cantoId}", cifra.cantoId in idsCantos)
            CifraTranspositor.transpor(cifra.tomOriginal, 0, cifra.preferenciaAcidentes)
            cifra.linhas.forEach { linha ->
                linha.acordes.forEach { acorde ->
                    assertTrue(acorde.posicao in 0..linha.texto.length)
                    CifraTranspositor.transpor(acorde.acorde, 0, cifra.preferenciaAcidentes)
                }
            }
        }

        val mapeamentos = AcordesFichaRepository(context).buscarTodosMapeamentos()
        val chaves = mutableSetOf<String>()
        mapeamentos.forEach { mapa ->
            assertTrue("Mapa para canto inexistente: ${mapa.cantoId}", mapa.cantoId in idsCantos)
            assertTrue("Mapa sem páginas de ficha: ${mapa.cantoId}", mapa.cantoId in paginasPorCanto)
            assertTrue("Mapa completo sem cifra: ${mapa.cantoId}", !mapa.completo || mapa.cantoId in cifrasPorId)
            mapa.acordes.forEach { acorde ->
                assertTrue("Página inválida no canto ${mapa.cantoId}",
                    acorde.pagina in paginasPorCanto.getValue(mapa.cantoId))
                assertTrue(acorde.x in 0f..1f && acorde.y in 0f..1f)
                assertTrue(acorde.largura > 0f && acorde.altura > 0f)
                assertTrue(acorde.x + acorde.largura <= 1f && acorde.y + acorde.altura <= 1f)
                CifraTranspositor.transpor(acorde.acorde, 0, cifrasPorId[mapa.cantoId]!!.preferenciaAcidentes)
                assertTrue(chaves.add("${mapa.cantoId}:${acorde.pagina}:${acorde.acorde}:${acorde.x}:${acorde.y}:${acorde.largura}:${acorde.altura}"))
            }
        }

        val piloto = cifrasPorId[1]
        val mapaPiloto = AcordesFichaRepository(context).buscarMapeamentoPorCanto(1)
        assertNotNull(piloto)
        assertNotNull(mapaPiloto)
        assertTrue(DisponibilidadeTransposicaoNaFicha.estaPronta(
            piloto,
            mapaPiloto,
            paginasPorCanto.getValue(1)
        ))
        assertTrue(DisponibilidadeTransposicaoNaFicha.estaPronta(
            null,
            mapaPiloto,
            paginasPorCanto.getValue(1)
        ).not())
    }

    private fun lerArray(caminho: String): JSONArray = context.assets.open(caminho)
        .bufferedReader(Charsets.UTF_8)
        .use { JSONArray(it.readText()) }
}
