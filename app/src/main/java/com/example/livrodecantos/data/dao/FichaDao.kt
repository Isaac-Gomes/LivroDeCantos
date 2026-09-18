package com.example.livrodecantos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.livrodecantos.data.entity.FichaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FichaDao {

    @Query(
        """
        SELECT *
        FROM fichas
        WHERE cantoId = :cantoId
        ORDER BY ordem ASC
        """
    )
    fun listarPorCanto(
        cantoId: Int
    ): Flow<List<FichaEntity>>


    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun inserirTodos(
        fichas: List<FichaEntity>
    )


    @Query(
        "SELECT COUNT(*) FROM fichas"
    )
    suspend fun contarFichas(): Int
}