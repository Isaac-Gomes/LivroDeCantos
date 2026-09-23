package com.example.livrodecantos.data.repository

import android.content.Context
import com.example.livrodecantos.model.cifra.AcordeNaFicha
import com.example.livrodecantos.model.cifra.MapeamentoAcordesFicha
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray

/** Catálogo estático das posições visuais dos acordes; não faz parte do Room. */
class AcordesFichaRepository(context: Context) {
    private val assets = context.applicationContext.assets

    suspend fun buscarPorCanto(cantoId: Int): List<AcordeNaFicha> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val catalogo = cache ?: carregarCatalogo().also { cache = it }
            catalogo[cantoId]?.acordes.orEmpty()
        }
    }

    suspend fun buscarMapeamentoPorCanto(cantoId: Int): MapeamentoAcordesFicha? =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val catalogo = cache ?: carregarCatalogo().also { cache = it }
                catalogo[cantoId]
            }
        }

    suspend fun buscarTodosMapeamentos(): List<MapeamentoAcordesFicha> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val catalogo = cache ?: carregarCatalogo().also { cache = it }
            catalogo.values.toList()
        }
    }

    private fun carregarCatalogo(): Map<Int, MapeamentoAcordesFicha> {
        val registros = assets.open("data/acordes_fichas.json")
            .bufferedReader(Charsets.UTF_8).use { JSONArray(it.readText()) }
        return buildMap {
            for (indice in 0 until registros.length()) {
                val registro = registros.getJSONObject(indice)
                val cantoId = registro.getInt("cantoId")
                require(cantoId !in this) { "Mapa de acordes duplicado para o canto $cantoId" }
                val acordes = registro.getJSONArray("acordes")
                val mapeamento = MapeamentoAcordesFicha(
                    cantoId = cantoId,
                    completo = registro.optBoolean("completo", false),
                    acordes = List(acordes.length()) { numeroAcorde ->
                    val acorde = acordes.getJSONObject(numeroAcorde)
                    AcordeNaFicha(
                        pagina = acorde.getInt("pagina"),
                        acorde = acorde.getString("acorde"),
                        x = acorde.getDouble("x").toFloat(),
                        y = acorde.getDouble("y").toFloat(),
                        largura = acorde.getDouble("largura").toFloat(),
                        altura = acorde.getDouble("altura").toFloat()
                    ).also { posicao ->
                        require(posicao.pagina > 0) { "Página inválida no canto $cantoId" }
                        require(posicao.x >= 0f && posicao.y >= 0f) {
                            "Posição inválida no canto $cantoId"
                        }
                        require(posicao.largura > 0f && posicao.altura > 0f) {
                            "Tamanho inválido no canto $cantoId"
                        }
                        require(posicao.x + posicao.largura <= 1f && posicao.y + posicao.altura <= 1f) {
                            "Acorde fora da ficha no canto $cantoId"
                        }
                    }
                })
                require(!mapeamento.completo || mapeamento.acordes.isNotEmpty()) {
                    "Mapa completo sem acordes no canto $cantoId"
                }
                put(cantoId, mapeamento)
            }
        }
    }

    private companion object {
        val mutex = Mutex()
        var cache: Map<Int, MapeamentoAcordesFicha>? = null
    }
}
