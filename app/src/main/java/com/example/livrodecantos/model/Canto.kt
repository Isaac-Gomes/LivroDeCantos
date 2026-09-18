package com.example.livrodecantos.model

data class Canto(
    val id: Int,
    val numero: String,
    val titulo: String,
    val etapa: Etapa,
    val possuiAudio: Boolean,
    val paginaUrl: String? = null,
    val audioArquivoOriginal: String? = null,
    val audioUrl: String? = null,
    val audioLocalPath: String? = null
)