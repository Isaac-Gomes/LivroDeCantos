package com.example.livrodecantos.model.cifra

/** Mantém a UI independente de IDs e assegura que todo overlay aponta para uma página real. */
object DisponibilidadeTransposicaoNaFicha {
    fun estaPronta(
        cifra: CifraCanto?,
        mapeamento: MapeamentoAcordesFicha?,
        paginasDaFicha: Collection<Int>
    ): Boolean = cifra != null &&
        mapeamento?.completo == true &&
        mapeamento.acordes.isNotEmpty() &&
        paginasDaFicha.isNotEmpty() &&
        mapeamento.acordes.all { it.pagina in paginasDaFicha }
}
