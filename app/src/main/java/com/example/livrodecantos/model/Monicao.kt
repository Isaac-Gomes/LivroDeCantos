package com.example.livrodecantos.model

data class Monicao(
    val cantoId: Int,
    val resumoTelegrafico: String,
    val referencias: List<String>,
    val monicao: String
)
