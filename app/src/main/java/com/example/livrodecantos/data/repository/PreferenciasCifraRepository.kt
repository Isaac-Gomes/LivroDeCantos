package com.example.livrodecantos.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.livrodecantos.model.cifra.ConfiguracaoCifra
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.preferenciasCifraDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "preferencias_cifra"
)

/**
 * Fonte persistente compartilhada das escolhas de cifra. Cada chave cont\u00e9m o
 * cantoId, portanto a configura\u00e7\u00e3o de um canto n\u00e3o interfere nos demais.
 */
class PreferenciasCifraRepository(private val dataStore: DataStore<Preferences>) {
    constructor(context: Context) : this(context.applicationContext.preferenciasCifraDataStore)

    fun observarConfiguracao(cantoId: Int): Flow<ConfiguracaoCifra> = dataStore.data.map { preferencias ->
        ConfiguracaoCifra(
            semitons = ConfiguracaoCifra.normalizarSemitons(
                preferencias[chaveSemitons(cantoId)] ?: 0
            ),
            capotraste = (preferencias[chaveCapotraste(cantoId)] ?: 0).coerceIn(0, 11)
        )
    }

    fun observarSemitons(cantoId: Int): Flow<Int> =
        observarConfiguracao(cantoId).map { it.semitons }

    fun observarCapotraste(cantoId: Int): Flow<Int> =
        observarConfiguracao(cantoId).map { it.capotraste }

    suspend fun salvarSemitons(cantoId: Int, valor: Int) {
        dataStore.edit { preferencias ->
            preferencias[chaveSemitons(cantoId)] = ConfiguracaoCifra.normalizarSemitons(valor)
        }
    }

    suspend fun ajustarSemitons(cantoId: Int, variacao: Int) {
        dataStore.edit { preferencias ->
            val atual = preferencias[chaveSemitons(cantoId)] ?: 0
            preferencias[chaveSemitons(cantoId)] =
                ConfiguracaoCifra.normalizarSemitons(atual + variacao)
        }
    }

    suspend fun salvarCapotraste(cantoId: Int, casa: Int) {
        dataStore.edit { preferencias ->
            preferencias[chaveCapotraste(cantoId)] = casa.coerceIn(0, 11)
        }
    }

    suspend fun ajustarCapotraste(cantoId: Int, variacao: Int) {
        dataStore.edit { preferencias ->
            val atual = preferencias[chaveCapotraste(cantoId)] ?: 0
            preferencias[chaveCapotraste(cantoId)] = (atual + variacao).coerceIn(0, 11)
        }
    }

    suspend fun restaurarConfiguracao(cantoId: Int) {
        dataStore.edit { preferencias ->
            preferencias.remove(chaveSemitons(cantoId))
            preferencias.remove(chaveCapotraste(cantoId))
        }
    }

    private fun chaveSemitons(cantoId: Int) = intPreferencesKey("canto_${cantoId}_semitons")
    private fun chaveCapotraste(cantoId: Int) = intPreferencesKey("canto_${cantoId}_capotraste")
}
