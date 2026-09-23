package com.example.livrodecantos.data.repository

import android.content.Context
import com.example.livrodecantos.model.Canto
import com.example.livrodecantos.model.Etapa
import com.example.livrodecantos.model.Monicao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * Catálogo estático associado pelo cantoId, sem modificar o conteúdo editorial.
 * O cache é compartilhado entre instâncias e dura enquanto o processo estiver ativo.
 */
class MonicaoRepository(context: Context) {
    private val assets = context.applicationContext.assets

    suspend fun buscarPorCanto(canto: Canto): Monicao? {
        if (canto.etapa == Etapa.LITURGICOS) return null

        return withContext(Dispatchers.IO) {
            mutex.withLock {
                val catalogo = cache ?: carregarCatalogo().also { cache = it }
                catalogo[canto.id]
            }
        }
    }

    private fun carregarCatalogo(): Map<Int, Monicao> {
        val registros = assets.open("data/monicoes_ressuscitou_app.json")
            .bufferedReader(Charsets.UTF_8)
            .use { JSONArray(it.readText()) }
        val monicoes = mutableMapOf<Int, Monicao>()
        for (indice in 0 until registros.length()) {
            val registro = registros.getJSONObject(indice)
            val cantoId = registro.getInt("cantoId")
            require(cantoId !in monicoes) { "Monição duplicada para o canto $cantoId" }
            val referencias = registro.optJSONArray("referencias")
            monicoes[cantoId] = Monicao(
                cantoId = cantoId,
                resumoTelegrafico = registro.getString("resumoTelegrafico"),
                referencias = if (referencias == null) emptyList() else
                    List(referencias.length()) { referencias.getString(it) },
                monicao = registro.getString("monicao")
            )
        }
        return monicoes
    }

    private companion object {
        val mutex = Mutex()
        var cache: Map<Int, Monicao>? = null
    }
}
