package com.example.livrodecantos.model.cifra

/**
 * Mapa validado de acordes desenhados em uma ficha. A página segue a mesma
 * numeração baseada em 1 de [com.example.livrodecantos.model.Ficha.ordem].
 */
data class MapeamentoAcordesFicha(
    val cantoId: Int,
    val completo: Boolean,
    val acordes: List<AcordeNaFicha>
) {
    fun acordesDaPagina(pagina: Int): List<AcordeNaFicha> =
        acordes.filter { it.pagina == pagina }
}
