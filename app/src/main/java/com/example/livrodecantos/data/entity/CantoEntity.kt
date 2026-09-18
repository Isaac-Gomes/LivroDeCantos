package com.example.livrodecantos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "cantos"
)
data class CantoEntity(

    @PrimaryKey
    val id: Int,

    val numero: String,

    val titulo: String,

    val etapa: String,

    val possuiAudio: Boolean,

    val paginaUrl: String? = null,

    val audioArquivoOriginal: String? = null,

    val audioUrl: String? = null,

    val audioLocalPath: String? = null
)