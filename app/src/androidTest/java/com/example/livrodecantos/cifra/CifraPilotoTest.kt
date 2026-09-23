package com.example.livrodecantos.cifra

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.livrodecantos.data.repository.AcordesFichaRepository
import com.example.livrodecantos.data.repository.CifraRepository
import com.example.livrodecantos.data.repository.PreferenciasCifraRepository
import com.example.livrodecantos.domain.cifra.CifraTranspositor
import com.example.livrodecantos.ui.screens.TelaCifra
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CifraPilotoTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun carregaSomenteCantoUmEReutilizaCache() = runBlocking {
        val repository = CifraRepository(context)
        val cifra = requireNotNull(repository.buscarPorCanto(1))
        assertEquals(1, cifra.cantoId)
        assertEquals("Re-", cifra.tomOriginal)
        val json = context.assets.open("data/cifras.json").bufferedReader().use { JSONArray(it.readText()) }
        assertEquals(1, json.length())
        assertSame(cifra, CifraRepository(context).buscarPorCanto(1))
        assertNull(repository.buscarPorCanto(2))
        assertNull(repository.buscarPorCanto(Int.MAX_VALUE))
        cifra.linhas.forEach { linha ->
            linha.acordes.forEach { assertTrue(it.posicao in 0..linha.texto.length) }
        }
    }

    @Test
    fun letraDoAssetExataAntesEDepoisDeTranspor() = runBlocking {
        val original = requireNotNull(CifraRepository(context).buscarPorCanto(1))
        assertEquals(LETRA, original.linhas.joinToString("\n") { it.texto })
        val transposta = CifraTranspositor.transpor(original, 2)
        assertArrayEquals(LETRA.toByteArray(Charsets.UTF_8),
            transposta.linhas.joinToString("\n") { it.texto }.toByteArray(Charsets.UTF_8))
        assertEquals(listOf("Mi-", "Do", "Si"),
            transposta.linhas.flatMap { it.acordes }.map { it.acorde }.distinct())
        assertEquals(listOf("Re-", "Sib", "La"),
            original.linhas.flatMap { it.acordes }.map { it.acorde }.distinct())
        assertEquals(original.linhas.map { l -> l.acordes.map { it.posicao } },
            transposta.linhas.map { l -> l.acordes.map { it.posicao } })
        assertEquals("Re-", transposta.tomOriginal)
    }

    @Test
    fun mapaDaFichaDoPilotoCobreTodosOsDezesseteAcordes() = runBlocking {
        val acordes = AcordesFichaRepository(context).buscarPorCanto(1)
        assertEquals(17, acordes.size)
        assertEquals(listOf("Re-", "Sib", "La"), acordes.map { it.acorde }.distinct())
        assertTrue(acordes.all { posicao ->
            posicao.pagina == 1 &&
                posicao.x in 0f..1f && posicao.y in 0f..1f &&
                posicao.largura > 0f && posicao.altura > 0f &&
                posicao.x + posicao.largura <= 1f &&
                posicao.y + posicao.altura <= 1f
        })
        assertTrue(AcordesFichaRepository(context).buscarPorCanto(2).isEmpty())
    }

    @Test
    fun controlesTranspoemEPersistemAoReabrir() {
        val cifra = runBlocking { requireNotNull(CifraRepository(context).buscarPorCanto(1)) }
        val preferencias = PreferenciasCifraRepository(context)
        runBlocking { preferencias.restaurarConfiguracao(1) }
        try {
            compose.setContent {
                MaterialTheme {
                    var aberto by remember { mutableStateOf(true) }
                    if (aberto) {
                        TelaCifra(
                            titulo = "A cabana dos pastores",
                            cifra = cifra,
                            onFechar = { aberto = false }
                        )
                    } else {
                        TextButton(onClick = { aberto = true }) { Text("Abrir cifra") }
                    }
                }
            }
            compose.onNodeWithText("Tom que soa: Re-").assertIsDisplayed()
            compose.onNodeWithContentDescription("Subir um semitom").performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithText("Tom que soa: Mib-").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithContentDescription("Subir um semitom").performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithText("Tom que soa: Mi-").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Que me beijes com beijos de sua boca!").assertExists()
            compose.onNodeWithContentDescription("Descer um semitom").performClick()
            compose.waitUntil(5_000) {
                compose.onAllNodesWithText("Tom que soa: Mib-").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithContentDescription("Voltar à ficha").performClick()
            compose.onNodeWithText("Abrir cifra").performClick()
            compose.onNodeWithText("Tom que soa: Mib-").assertIsDisplayed()
        } finally {
            runBlocking { preferencias.restaurarConfiguracao(1) }
        }
    }

    companion object {
        private val LETRA = """
            Que me beijes com beijos de sua boca!
            Melhores que o vinho são teus amores;
            
            o teu nome é perfume derramado,
            por isso te amam as donzelas.
            
            LEVA-ME ATRÁS DE TI: SAIAMOS!
            LEVA-ME ATRÁS DE TI: CORRAMOS!
            CELEBRAREMOS OS TEUS AMORES MAIS QUE O VINHO;
            COM QUANTA RAZÃO TU ÉS AMADO!
            
            FAZ-ME SABER, AMADO DA MINHA ALMA,
            ONDE APASCENTAS O REBANHO,
            PARA QUE EU NÃO ANDE VAGUEANDO
            ATRÁS DE OUTROS COMPANHEIROS.
            
            Se não sabes, ó mais bela das mulheres,
            segue o caminho das minhas ovelhas,
            
            e leva por aí os teus cabritos,
            até a cabana dos pastores.
            
            LEVA-ME ATRÁS DE TI: SAIAMOS! ...
        """.trimIndent()
    }
}
