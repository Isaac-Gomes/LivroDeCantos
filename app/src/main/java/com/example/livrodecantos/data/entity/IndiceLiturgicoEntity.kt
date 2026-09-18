package com.example.livrodecantos.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "indice_liturgico",

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
        Index(value = ["subcategoria"])
    ]
)
data class IndiceLiturgicoEntity(

    @PrimaryKey
    val id: Int,

    val cantoId: Int?,

    val subcategoria: String,

    val tituloOriginal: String,

    val paginaFichaOriginal: String
)