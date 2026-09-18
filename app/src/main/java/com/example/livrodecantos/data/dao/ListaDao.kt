package com.example.livrodecantos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.livrodecantos.data.entity.CantoEntity
import com.example.livrodecantos.data.entity.ListaCantoCrossRef
import com.example.livrodecantos.data.entity.ListaEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ListaDao {

    // ====================================================
    // LISTAS
    // ====================================================

    @Query(
        """
        SELECT *
        FROM listas
        ORDER BY nome COLLATE NOCASE
        """
    )
    fun listarListas():
            Flow<List<ListaEntity>>


    @Insert
    suspend fun criarLista(
        lista: ListaEntity
    ): Long


    @Update
    suspend fun atualizarLista(
        lista: ListaEntity
    )


    @Delete
    suspend fun excluirLista(
        lista: ListaEntity
    )


    @Query(
        """
        SELECT *
        FROM listas
        WHERE id = :listaId
        LIMIT 1
        """
    )
    suspend fun buscarLista(
        listaId: Long
    ): ListaEntity?


    // ====================================================
    // CANTOS DA LISTA
    // ====================================================

    @Query(
        """
        SELECT c.*
        FROM cantos AS c
        INNER JOIN lista_cantos AS lc
            ON lc.cantoId = c.id
        WHERE lc.listaId = :listaId
        ORDER BY lc.ordem ASC
        """
    )
    fun listarCantosDaLista(
        listaId: Long
    ): Flow<List<CantoEntity>>


    @Query(
        """
        SELECT cantoId
        FROM lista_cantos
        WHERE listaId = :listaId
        ORDER BY ordem ASC
        """
    )
    fun listarIdsDaLista(
        listaId: Long
    ): Flow<List<Int>>


    // Usado para carregar a seleção inicial da tela de edição.
    @Query(
        """
        SELECT cantoId
        FROM lista_cantos
        WHERE listaId = :listaId
        ORDER BY ordem ASC
        """
    )
    suspend fun buscarIdsDaListaAgora(
        listaId: Long
    ): List<Int>


    // ====================================================
    // RELAÇÕES
    // ====================================================

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun inserirRelacoes(
        relacoes: List<ListaCantoCrossRef>
    )


    @Query(
        """
        DELETE FROM lista_cantos
        WHERE listaId = :listaId
        """
    )
    suspend fun limparLista(
        listaId: Long
    )


    // ====================================================
    // SALVAR SELEÇÃO COMPLETA
    // ====================================================

    @Transaction
    suspend fun salvarCantosDaLista(
        listaId: Long,
        cantoIds: List<Int>
    ) {

        limparLista(
            listaId
        )


        if (
            cantoIds.isEmpty()
        ) {
            return
        }


        val relacoes =

            cantoIds.mapIndexed {

                    indice,
                    cantoId ->

                ListaCantoCrossRef(

                    listaId =
                        listaId,

                    cantoId =
                        cantoId,

                    ordem =
                        indice
                )
            }


        inserirRelacoes(
            relacoes
        )
    }
}