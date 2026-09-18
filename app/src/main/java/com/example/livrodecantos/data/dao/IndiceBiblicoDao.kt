package com.example.livrodecantos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.livrodecantos.data.entity.IndiceBiblicoEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface IndiceBiblicoDao {


    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun inserirTodos(
        itens: List<IndiceBiblicoEntity>
    )


    @Query(
        """
        SELECT *
        FROM indice_biblico
        WHERE cantoId IS NOT NULL
        ORDER BY id ASC
        """
    )
    fun listarVinculados():
            Flow<List<IndiceBiblicoEntity>>


    @Query(
        """
        SELECT *
        FROM indice_biblico
        WHERE testamento = :testamento
        AND cantoId IS NOT NULL
        ORDER BY id ASC
        """
    )
    fun listarPorTestamento(
        testamento: String
    ): Flow<List<IndiceBiblicoEntity>>


    @Query(
        """
        SELECT testamento
        FROM indice_biblico
        WHERE cantoId IS NOT NULL
        AND TRIM(testamento) != ''
        GROUP BY testamento
        ORDER BY MIN(id) ASC
        """
    )
    fun listarTestamentos():
            Flow<List<String>>


    @Query(
        "SELECT COUNT(*) FROM indice_biblico"
    )
    suspend fun contarRegistros(): Int
}