package com.example.livrodecantos.data.repository

import com.example.livrodecantos.data.dao.ListaDao
import com.example.livrodecantos.data.entity.CantoEntity
import com.example.livrodecantos.data.entity.ListaEntity
import kotlinx.coroutines.flow.Flow


class ListaRepository(

    private val listaDao: ListaDao

) {

    // ====================================================
    // LISTAS
    // ====================================================

    fun listarListas():
            Flow<List<ListaEntity>> {

        return listaDao
            .listarListas()
    }


    suspend fun criarLista(
        nome: String
    ): Long {

        val nomeLimpo =
            nome.trim()


        require(
            nomeLimpo.isNotBlank()
        ) {
            "O nome da lista não pode ficar vazio."
        }


        return listaDao
            .criarLista(

                ListaEntity(
                    nome = nomeLimpo
                )
            )
    }


    // ====================================================
    // CANTOS
    // ====================================================

    fun listarCantosDaLista(
        listaId: Long
    ): Flow<List<CantoEntity>> {

        return listaDao
            .listarCantosDaLista(
                listaId
            )
    }


    suspend fun buscarIdsDaLista(
        listaId: Long
    ): List<Int> {

        return listaDao
            .buscarIdsDaListaAgora(
                listaId
            )
    }


    suspend fun salvarCantosDaLista(
        listaId: Long,
        cantoIds: List<Int>
    ) {

        listaDao
            .salvarCantosDaLista(
                listaId = listaId,
                cantoIds = cantoIds
            )
    }
}