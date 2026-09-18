package com.example.livrodecantos.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fichas",

    foreignKeys = [
        ForeignKey(
            entity = CantoEntity::class,
            parentColumns = ["id"],
            childColumns = ["cantoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(
            value = ["cantoId"]
        ),

        Index(
            value = ["cantoId", "ordem"],
            unique = true
        )
    ]
)
data class FichaEntity(

    @PrimaryKey
    val id: Int,

    val cantoId: Int,

    val ordem: Int,

    val arquivo: String
)