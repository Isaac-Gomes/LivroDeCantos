package com.example.livrodecantos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.livrodecantos.data.entity.CantoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CantoDao {

    @Query("""
    UPDATE cantos
    SET numero = :numero
    WHERE id = :cantoId
""")
    suspend fun atualizarNumero(
        cantoId: Int,
        numero: String
    )@Query("""
    SELECT *
    FROM cantos
    WHERE possuiAudio = 1
    ORDER BY id ASC
""")
    suspend fun listarCantosComAudioAgora(): List<CantoEntity>@Query(
        """
    UPDATE cantos
    SET audioLocalPath = NULL
    WHERE id = :cantoId
    """
    )
    suspend fun limparAudioLocalPath(
        cantoId: Int
    ) @Query(
        """
    UPDATE cantos
    SET audioUrl = :audioUrl
    WHERE id = :cantoId
    """
    )
    suspend fun atualizarAudioUrl(
        cantoId: Int,
        audioUrl: String
    )


    @Query(
        """
    UPDATE cantos
    SET audioLocalPath = :caminho
    WHERE id = :cantoId
    """
    )
    suspend fun atualizarAudioLocalPath(
        cantoId: Int,
        caminho: String
    )
    @Query(
        """
        SELECT *
        FROM cantos
        ORDER BY titulo COLLATE NOCASE ASC
        """
    )
    fun listarTodos(): Flow<List<CantoEntity>>


    @Query(
        """
        SELECT *
        FROM cantos
        WHERE etapa = :etapa
        ORDER BY titulo COLLATE NOCASE ASC
        """
    )
    fun listarPorEtapa(
        etapa: String
    ): Flow<List<CantoEntity>>


    @Query(
        """
        SELECT *
        FROM cantos
        WHERE titulo LIKE '%' || :texto || '%'
        ORDER BY titulo COLLATE NOCASE ASC
        """
    )
    fun pesquisar(
        texto: String
    ): Flow<List<CantoEntity>>


    @Query(
        """
        SELECT *
        FROM cantos
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun buscarPorId(
        id: Int
    ): CantoEntity?


    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun inserir(
        canto: CantoEntity
    )


    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun inserirTodos(
        cantos: List<CantoEntity>
    )


    @Query(
        "SELECT COUNT(*) FROM cantos"
    )
    suspend fun contarCantos(): Int
}


