package com.example.livrodecantos.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index


@Entity(
    tableName = "lista_cantos",

    primaryKeys = [
        "listaId",
        "cantoId"
    ],

    foreignKeys = [

        ForeignKey(
            entity = ListaEntity::class,
            parentColumns = ["id"],
            childColumns = ["listaId"],
            onDelete = ForeignKey.CASCADE
        ),

        ForeignKey(
            entity = CantoEntity::class,
            parentColumns = ["id"],
            childColumns = ["cantoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index("listaId"),
        Index("cantoId")
    ]
)
data class ListaCantoCrossRef(

    val listaId: Long,

    val cantoId: Int,

    val ordem: Int
)