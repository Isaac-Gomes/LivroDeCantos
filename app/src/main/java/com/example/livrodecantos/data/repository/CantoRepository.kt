package com.example.livrodecantos.data.repository

import android.content.Context
import com.example.livrodecantos.data.dao.CantoDao
import com.example.livrodecantos.data.dao.FichaDao
import com.example.livrodecantos.data.dao.IndiceBiblicoDao
import com.example.livrodecantos.data.dao.IndiceLiturgicoDao
import com.example.livrodecantos.data.entity.CantoEntity
import com.example.livrodecantos.data.entity.FichaEntity
import com.example.livrodecantos.data.entity.IndiceBiblicoEntity
import com.example.livrodecantos.data.entity.IndiceLiturgicoEntity
import com.example.livrodecantos.model.Canto
import com.example.livrodecantos.model.Etapa
import com.example.livrodecantos.model.Ficha
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import com.example.livrodecantos.model.ItemIndiceBiblico


class CantoRepository(

    private val context: Context,

    private val cantoDao: CantoDao,

    private val fichaDao: FichaDao,

    private val indiceLiturgicoDao:
    IndiceLiturgicoDao,

    private val indiceBiblicoDao:
    IndiceBiblicoDao

) {


    // ------------------------------------------------
    // CANTOS
    // ------------------------------------------------

    val cantos: Flow<List<Canto>> =

        cantoDao
            .listarTodos()
            .map { entidades ->

                entidades.map {
                    entidadeParaCanto(it)
                }
            }


    // ------------------------------------------------
    // FICHAS DE UM CANTO
    // ------------------------------------------------

    fun fichasDoCanto(
        cantoId: Int
    ): Flow<List<Ficha>> {

        return fichaDao
            .listarPorCanto(cantoId)
            .map { entidades ->

                entidades.map { entidade ->

                    Ficha(
                        id = entidade.id,
                        cantoId = entidade.cantoId,
                        ordem = entidade.ordem,
                        arquivo = entidade.arquivo
                    )
                }
            }
    }


    // ------------------------------------------------
    // ÍNDICE LITÚRGICO
    // ------------------------------------------------

    fun subcategoriasLiturgicas():
            Flow<List<String>> {

        return indiceLiturgicoDao
            .listarSubcategorias()
    }


    fun cantosPorSubcategoriaLiturgica(
        subcategoria: String
    ): Flow<List<Canto>> {

        return indiceLiturgicoDao
            .listarCantosPorSubcategoria(
                subcategoria
            )
            .map { entidades ->

                entidades.map {
                    entidadeParaCanto(it)
                }
            }
    }

// ------------------------------------------------
// ÍNDICE BÍBLICO
// ------------------------------------------------

    fun testamentosBiblicos():
            Flow<List<String>> {

        return indiceBiblicoDao
            .listarTestamentos()
    }


    fun itensBiblicosPorTestamento(
        testamento: String
    ): Flow<List<ItemIndiceBiblico>> {

        return indiceBiblicoDao
            .listarPorTestamento(testamento)
            .map { referencias ->

                val resultado =
                    mutableListOf<ItemIndiceBiblico>()

                for (referencia in referencias) {

                    val cantoId =
                        referencia.cantoId
                            ?: continue

                    val cantoEntity =
                        cantoDao.buscarPorId(
                            cantoId
                        )
                            ?: continue


                    resultado.add(

                        ItemIndiceBiblico(

                            id = referencia.id,

                            testamento =
                                referencia.testamento,

                            referencia =
                                referencia.referencia,

                            canto =
                                entidadeParaCanto(
                                    cantoEntity
                                )
                        )
                    )
                }

                resultado
            }
    }
    // ------------------------------------------------
    // IMPORTAÇÃO DOS ASSETS
    // ------------------------------------------------

    suspend fun importarCatalogoSeNecessario() {

        if (
            cantoDao.contarCantos() == 0
        ) {

            cantoDao.inserirTodos(
                carregarCantosDosAssets()
            )
        }


        if (
            fichaDao.contarFichas() == 0
        ) {

            fichaDao.inserirTodos(
                carregarFichasDosAssets()
            )
        }


        if (
            indiceLiturgicoDao
                .contarRegistros() == 0
        ) {

            indiceLiturgicoDao
                .inserirTodos(
                    carregarIndiceLiturgico()
                )
        }


        if (
            indiceBiblicoDao
                .contarRegistros() == 0
        ) {

            indiceBiblicoDao
                .inserirTodos(
                    carregarIndiceBiblico()
                )
        }
    }


    // ------------------------------------------------
    // CONVERTER ENTITY -> MODEL
    // ------------------------------------------------

    private fun entidadeParaCanto(
        entidade: CantoEntity
    ): Canto {

        return Canto(

            id = entidade.id,

            numero = entidade.numero,

            titulo = entidade.titulo,

            etapa =
                Etapa.valueOf(
                    entidade.etapa
                ),

            possuiAudio =
                entidade.possuiAudio,

            paginaUrl =
                entidade.paginaUrl,

            audioArquivoOriginal =
                entidade.audioArquivoOriginal,

            audioUrl =
                entidade.audioUrl,

            audioLocalPath =
                entidade.audioLocalPath
        )
    }


    // ------------------------------------------------
    // CANTOS.JSON
    // ------------------------------------------------

    private fun carregarCantosDosAssets():
            List<CantoEntity> {

        val array =
            JSONArray(
                lerAsset(
                    "data/cantos.json"
                )
            )

        val resultado =
            mutableListOf<CantoEntity>()


        for (
        i in 0 until array.length()
        ) {

            val obj =
                array.getJSONObject(i)


            resultado.add(

                CantoEntity(

                    id =
                        obj.getInt("id"),

                    numero =
                        obj.optString(
                            "numero",
                            ""
                        ),

                    titulo =
                        obj.getString(
                            "titulo"
                        ),

                    etapa =
                        obj.getString(
                            "etapa"
                        ),

                    possuiAudio =
                        obj.optBoolean(
                            "possuiAudio",
                            false
                        ),

                    paginaUrl =
                        stringOuNull(
                            obj,
                            "paginaUrl"
                        ),

                    audioArquivoOriginal =
                        stringOuNull(
                            obj,
                            "audioArquivoOriginal"
                        ),

                    audioUrl =
                        stringOuNull(
                            obj,
                            "audioUrl"
                        ),

                    audioLocalPath =
                        stringOuNull(
                            obj,
                            "audioLocalPath"
                        )
                )
            )
        }


        return resultado
    }


    // ------------------------------------------------
    // FICHAS.JSON
    // ------------------------------------------------

    private fun carregarFichasDosAssets():
            List<FichaEntity> {

        val array =
            JSONArray(
                lerAsset(
                    "data/fichas.json"
                )
            )

        val resultado =
            mutableListOf<FichaEntity>()


        for (
        i in 0 until array.length()
        ) {

            val obj =
                array.getJSONObject(i)


            resultado.add(

                FichaEntity(

                    id =
                        obj.getInt("id"),

                    cantoId =
                        obj.getInt(
                            "cantoId"
                        ),

                    ordem =
                        obj.getInt(
                            "ordem"
                        ),

                    arquivo =
                        obj.getString(
                            "arquivo"
                        )
                )
            )
        }


        return resultado
    }


    // ------------------------------------------------
    // ÍNDICE LITÚRGICO.JSON
    // ------------------------------------------------

    private fun carregarIndiceLiturgico():
            List<IndiceLiturgicoEntity> {

        val array =
            JSONArray(
                lerAsset(
                    "data/indice_liturgico.json"
                )
            )

        val resultado =
            mutableListOf<
                    IndiceLiturgicoEntity
                    >()


        for (
        i in 0 until array.length()
        ) {

            val obj =
                array.getJSONObject(i)


            resultado.add(

                IndiceLiturgicoEntity(

                    id =
                        obj.getInt("id"),

                    cantoId =
                        intOuNull(
                            obj,
                            "cantoId"
                        ),

                    subcategoria =
                        obj.optString(
                            "subcategoria",
                            ""
                        ),

                    tituloOriginal =
                        obj.optString(
                            "tituloOriginal",
                            ""
                        ),

                    paginaFichaOriginal =
                        obj.optString(
                            "paginaFichaOriginal",
                            ""
                        )
                )
            )
        }


        return resultado
    }


    // ------------------------------------------------
    // ÍNDICE BÍBLICO.JSON
    // ------------------------------------------------

    private fun carregarIndiceBiblico():
            List<IndiceBiblicoEntity> {

        val array =
            JSONArray(
                lerAsset(
                    "data/indice_biblico.json"
                )
            )

        val resultado =
            mutableListOf<
                    IndiceBiblicoEntity
                    >()


        for (
        i in 0 until array.length()
        ) {

            val obj =
                array.getJSONObject(i)


            resultado.add(

                IndiceBiblicoEntity(

                    id =
                        obj.getInt("id"),

                    cantoId =
                        intOuNull(
                            obj,
                            "cantoId"
                        ),

                    testamento =
                        obj.optString(
                            "testamento",
                            ""
                        ),

                    referencia =
                        obj.optString(
                            "referencia",
                            ""
                        ),

                    tituloOriginal =
                        obj.optString(
                            "tituloOriginal",
                            ""
                        ),

                    paginaFichaOriginal =
                        obj.optString(
                            "paginaFichaOriginal",
                            ""
                        )
                )
            )
        }


        return resultado
    }


    // ------------------------------------------------
    // ASSETS
    // ------------------------------------------------

    private fun lerAsset(
        caminho: String
    ): String {

        return context.assets
            .open(caminho)
            .bufferedReader()
            .use {
                it.readText()
            }
    }


    private fun stringOuNull(
        obj: JSONObject,
        chave: String
    ): String? {

        if (
            !obj.has(chave) ||
            obj.isNull(chave)
        ) {
            return null
        }


        return obj
            .optString(chave)
            .takeIf {
                it.isNotBlank() &&
                        it != "null"
            }
    }


    private fun intOuNull(
        obj: JSONObject,
        chave: String
    ): Int? {

        if (
            !obj.has(chave) ||
            obj.isNull(chave)
        ) {
            return null
        }


        return obj.getInt(chave)
    }
}