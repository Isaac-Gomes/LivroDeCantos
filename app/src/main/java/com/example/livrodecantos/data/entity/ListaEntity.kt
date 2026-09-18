package com.example.livrodecantos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(
    tableName = "listas"
)
data class ListaEntity(

    @PrimaryKey(
        autoGenerate = true
    )
    val id: Long = 0,

    val nome: String,

    val criadoEm: Long =
        System.currentTimeMillis()
)