package com.example.livrodecantos.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "indice_biblico",

    foreignKeys = [
        ForeignKey(
            entity = CantoEntity::class,
            parentColumns = ["id"],
            childColumns = ["cantoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["cantoId"]),
        Index(value = ["testamento"])
    ]
)
data class IndiceBiblicoEntity(

    @PrimaryKey
    val id: Int,

    val cantoId: Int?,

    val testamento: String,

    val referencia: String,

    val tituloOriginal: String,

    val paginaFichaOriginal: String
)