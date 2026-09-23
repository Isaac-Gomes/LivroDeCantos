package com.example.livrodecantos.model.cifra

import java.io.Serializable

/** Letra literal, independente dos acordes; uma linha vazia pode separar estrofes. */
data class LinhaCifra(
    val texto: String,
    val acordes: List<AcordePosicionado> = emptyList()
) : Serializable
