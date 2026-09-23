package com.example.livrodecantos.data.repository

import android.content.Context
import com.example.livrodecantos.model.cifra.AcordePosicionado
import com.example.livrodecantos.model.cifra.CifraCanto
import com.example.livrodecantos.model.cifra.LinhaCifra
import com.example.livrodecantos.model.cifra.PreferenciaAcidentes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray

/** Catálogo estático: uma leitura bem-sucedida por processo, sem persistência no Room. */
class CifraRepository(context: Context) {
    private val assets = context.applicationContext.assets

    suspend fun buscarPorCanto(cantoId: Int): CifraCanto? = withContext(Dispatchers.IO) {
        mutex.withLock {
            val catalogo = cache ?: carregarCatalogo().also { cache = it }
            catalogo[cantoId]
        }
    }

    suspend fun buscarTodos(): List<CifraCanto> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val catalogo = cache ?: carregarCatalogo().also { cache = it }
            catalogo.values.toList()
        }
    }

    private fun carregarCatalogo(): Map<Int, CifraCanto> {
        val registros = assets.open("data/cifras.json")
            .bufferedReader(Charsets.UTF_8).use { JSONArray(it.readText()) }
        return buildMap {
            for (indice in 0 until registros.length()) {
                val registro = registros.getJSONObject(indice)
                val cantoId = registro.getInt("cantoId")
                require(cantoId !in this) { "Cifra duplicada para o canto $cantoId" }
                val linhas = registro.getJSONArray("linhas")
                put(cantoId, CifraCanto(
                    cantoId = cantoId,
                    tomOriginal = registro.getString("tomOriginal"),
                    preferenciaAcidentes = PreferenciaAcidentes.valueOf(registro.getString("preferenciaAcidentes")),
                    linhas = List(linhas.length()) { numeroLinha ->
                        val linha = linhas.getJSONObject(numeroLinha)
                        val texto = linha.getString("texto")
                        val acordes = linha.getJSONArray("acordes")
                        LinhaCifra(texto, List(acordes.length()) { numeroAcorde ->
                            val acorde = acordes.getJSONObject(numeroAcorde)
                            val posicao = acorde.getInt("posicao")
                            require(posicao in 0..texto.length) { "Posição inválida no canto $cantoId" }
                            AcordePosicionado(posicao, acorde.getString("acorde"))
                        })
                    }
                ))
            }
        }
    }

    private companion object {
        val mutex = Mutex()
        var cache: Map<Int, CifraCanto>? = null
    }
}
