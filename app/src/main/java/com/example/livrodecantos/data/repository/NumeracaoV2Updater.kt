package com.example.livrodecantos.data.repository

import android.content.Context
import com.example.livrodecantos.data.dao.CantoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray


object NumeracaoV2Updater {

    private const val PREFS =
        "ressuscitou_catalogo"

    private const val CHAVE_VERSAO =
        "versao_numeracao"


    suspend fun aplicar(

        context: Context,

        cantoDao: CantoDao

    ) {

        val preferencias =
            context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )


        val versaoAtual =
            preferencias.getInt(
                CHAVE_VERSAO,
                0
            )


        if (
            versaoAtual >= 2
        ) {
            return
        }


        withContext(
            Dispatchers.IO
        ) {

            val texto =

                context.assets
                    .open(
                        "data/numeracao_v2.json"
                    )
                    .bufferedReader()
                    .use {
                        it.readText()
                    }


            val array =
                JSONArray(
                    texto
                )


            for (
            indice in 0 until array.length()
            ) {

                val item =
                    array.getJSONObject(
                        indice
                    )


                val cantoId =
                    item.getInt(
                        "id"
                    )


                val numero =
                    item.getString(
                        "numero"
                    )


                cantoDao
                    .atualizarNumero(

                        cantoId =
                            cantoId,

                        numero =
                            numero
                    )
            }


            preferencias
                .edit()
                .putInt(
                    CHAVE_VERSAO,
                    2
                )
                .apply()
        }
    }
}