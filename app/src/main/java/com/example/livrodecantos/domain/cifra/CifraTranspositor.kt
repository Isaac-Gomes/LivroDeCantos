package com.example.livrodecantos.domain.cifra

import com.example.livrodecantos.model.cifra.CifraCanto
import com.example.livrodecantos.model.cifra.PreferenciaAcidentes

/** Motor puro, independente de Android, UI e persistência. */
object CifraTranspositor {
    private val sustenidos = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val bemois = listOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")
    private val sustenidosLatinos = listOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")
    private val bemoisLatinos = listOf("Do", "Reb", "Re", "Mib", "Mi", "Fa", "Solb", "Sol", "Lab", "La", "Sib", "Si")
    private val naturais = mapOf(
        "C" to 0, "D" to 2, "E" to 4, "F" to 5, "G" to 7, "A" to 9, "B" to 11,
        "Do" to 0, "Re" to 2, "Mi" to 4, "Fa" to 5, "Sol" to 7, "La" to 9, "Si" to 11
    )
    // Nomes completos vêm antes das letras isoladas (Do antes de D, Fa antes de F).
    private const val NOTA = "(?:Do|Re|Mi|Fa|Sol|La|Si|[A-G])[#b]?"
    private val fundamental = Regex("^($NOTA)(.*)$", RegexOption.DOT_MATCHES_ALL)
    private val baixoFinal = Regex("/($NOTA)$")

    /**
     * Preserva o sufixo literalmente. Só uma nota após a última barra, no fim,
     * é tratada como baixo; por exemplo, a extensão 6/9 permanece intacta.
     * A preferência normaliza a enarmonia inclusive quando semitons é zero.
     * Detecta e preserva a notação de cada nota (anglo-saxônica ou latina).
     * O sinal de menor "-" é um sufixo e permanece intacto.
     * Entradas sem fundamental reconhecida lançam IllegalArgumentException.
     */
    fun transpor(
        acorde: String,
        semitons: Int,
        preferencia: PreferenciaAcidentes = PreferenciaAcidentes.SUSTENIDOS
    ): String {
        val partes = requireNotNull(fundamental.matchEntire(acorde)) {
            "Acorde sem nota fundamental válida: $acorde"
        }
        val sufixo = partes.groupValues[2]
        val baixo = baixoFinal.find(sufixo)
        val sufixoTransposto = if (baixo == null) sufixo else
            sufixo.substring(0, baixo.range.first) + "/" +
                transporNota(baixo.groupValues[1], semitons, preferencia)
        return transporNota(partes.groupValues[1], semitons, preferencia) + sufixoTransposto
    }

    /** Retorna outra cifra e outras listas, mantendo letra, posições e metadados. */
    fun transpor(cifra: CifraCanto, semitons: Int): CifraCanto =
        cifra.copy(
            linhas = cifra.linhas.map { linha ->
                linha.copy(
                    acordes = linha.acordes.map { posicionado ->
                        posicionado.copy(
                            acorde = transpor(posicionado.acorde, semitons, cifra.preferenciaAcidentes)
                        )
                    }
                )
            }
        )

    fun tomAtual(
        tomOriginal: String,
        semitons: Int,
        preferencia: PreferenciaAcidentes = PreferenciaAcidentes.SUSTENIDOS
    ): String = transpor(tomOriginal, semitons, preferencia)

    /** Forma tocada = som desejado - casas. Zero equivale a tocar sem capotraste. */
    fun formaComCapotraste(
        somDesejado: String,
        casa: Int,
        preferencia: PreferenciaAcidentes = PreferenciaAcidentes.SUSTENIDOS
    ): String {
        require(casa >= 0) { "A casa do capotraste não pode ser negativa" }
        return transpor(somDesejado, -(casa % 12), preferencia)
    }

    /** A forma escrita parte do acorde original, do tom desejado e do capo. */
    fun deslocamentoDaForma(semitonsDoTom: Int, casaCapotraste: Int): Int {
        require(casaCapotraste in 0..11) { "A casa do capotraste deve estar entre 0 e 11" }
        return semitonsDoTom - casaCapotraste
    }

    fun transporParaForma(
        acordeOriginal: String,
        semitonsDoTom: Int,
        casaCapotraste: Int,
        preferencia: PreferenciaAcidentes = PreferenciaAcidentes.SUSTENIDOS
    ): String {
        val deslocamento = deslocamentoDaForma(semitonsDoTom, casaCapotraste)
        return if (deslocamento == 0) acordeOriginal
        else transpor(acordeOriginal, deslocamento, preferencia)
    }

    fun transporParaFormas(
        cifra: CifraCanto,
        semitonsDoTom: Int,
        casaCapotraste: Int
    ): CifraCanto {
        val deslocamento = deslocamentoDaForma(semitonsDoTom, casaCapotraste)
        return if (deslocamento == 0) cifra else transpor(cifra, deslocamento)
    }

    private fun transporNota(
        nota: String,
        semitons: Int,
        preferencia: PreferenciaAcidentes
    ): String {
        val nome = nota.removeSuffix("#").removeSuffix("b")
        val latina = nome.length > 1
        val acidente = when (nota.last()) {
            '#' -> 1
            'b' -> -1
            else -> 0
        }
        // Reduzir antes da soma evita overflow mesmo com Int.MIN_VALUE/MAX_VALUE.
        val indice = Math.floorMod(naturais.getValue(nome) + acidente + semitons % 12, 12)
        return when (preferencia) {
            PreferenciaAcidentes.SUSTENIDOS -> if (latina) sustenidosLatinos[indice] else sustenidos[indice]
            PreferenciaAcidentes.BEMOIS -> if (latina) bemoisLatinos[indice] else bemois[indice]
        }
    }
}
