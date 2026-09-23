package com.example.livrodecantos.model.cifra

import java.io.Serializable

/**
 * Estrutura própria para JSON: campos simples, listas e enum pelo nome.
 * cantoId associa a cifra ao catálogo. Não é uma entidade Room.
 * tomOriginal permanece como referência mesmo numa cópia transposta.
 */
data class CifraCanto(
    val cantoId: Int,
    val tomOriginal: String,
    val preferenciaAcidentes: PreferenciaAcidentes,
    val linhas: List<LinhaCifra>
) : Serializable
