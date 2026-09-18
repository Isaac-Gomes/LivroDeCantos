package com.example.livrodecantos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.livrodecantos.data.entity.CantoEntity
import com.example.livrodecantos.data.entity.IndiceLiturgicoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IndiceLiturgicoDao {

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun inserirTodos(
        itens: List<IndiceLiturgicoEntity>
    )


    @Query(
        """
        SELECT DISTINCT subcategoria
        FROM indice_liturgico
        WHERE cantoId IS NOT NULL
        AND TRIM(subcategoria) != ''
        ORDER BY subcategoria COLLATE NOCASE
        """
    )
    fun listarSubcategorias():
            Flow<List<String>>


    @Query(
        """
        SELECT DISTINCT c.*
        FROM cantos AS c
        INNER JOIN indice_liturgico AS i
            ON i.cantoId = c.id
        WHERE i.subcategoria = :subcategoria
        ORDER BY c.titulo COLLATE NOCASE
        """
    )
    fun listarCantosPorSubcategoria(
        subcategoria: String
    ): Flow<List<CantoEntity>>


    @Query(
        "SELECT COUNT(*) FROM indice_liturgico"
    )
    suspend fun contarRegistros(): Int
}