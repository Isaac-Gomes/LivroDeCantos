package com.example.livrodecantos.model.cifra

import java.io.Serializable

/**
 * Posição baseada em zero, em unidades UTF-16 (índices de String no Kotlin).
 * Indica a âncora na letra, não pixels nem bytes; texto.length permite acorde ao final.
 */
data class AcordePosicionado(
    val posicao: Int,
    val acorde: String
) : Serializable
