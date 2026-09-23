package com.example.livrodecantos.model.cifra

/** Configura\u00e7\u00e3o persistida por canto; o tom \u00e9 sempre derivado do original. */
data class ConfiguracaoCifra(
    val semitons: Int = 0,
    val capotraste: Int = 0
) {
    companion object {
        /**
         * Evita deslocamentos acumulados fora de uma faixa musical leg\u00edvel.
         * Doze semitons voltam ao tom original, sem impedir subir ou descer
         * naturalmente a partir do zero.
         */
        fun normalizarSemitons(valor: Int): Int = when {
            valor in -11..11 -> valor
            else -> Math.floorMod(valor, 12).let { resto ->
                if (resto > 6) resto - 12 else resto
            }
        }
    }
}
