package com.example.livrodecantos.model.cifra

/**
 * Região normalizada de um acorde desenhado numa página da ficha.
 *
 * Os valores são relativos ao bitmap original e, portanto, não dependem do
 * tamanho em que a ficha é exibida ou do nível de zoom.
 */
data class AcordeNaFicha(
    val pagina: Int,
    val acorde: String,
    val x: Float,
    val y: Float,
    val largura: Float,
    val altura: Float
)
