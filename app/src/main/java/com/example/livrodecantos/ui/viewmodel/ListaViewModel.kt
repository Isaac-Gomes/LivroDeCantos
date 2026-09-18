package com.example.livrodecantos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.livrodecantos.data.database.RessuscitouDatabase
import com.example.livrodecantos.data.entity.ListaEntity
import com.example.livrodecantos.data.repository.ListaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class ListaViewModel(
    application: Application
) : AndroidViewModel(application) {


    private val database =
        RessuscitouDatabase
            .getDatabase(application)


    private val repository =
        ListaRepository(
            listaDao =
                database.listaDao()
        )


    // ====================================================
    // LISTAS
    // ====================================================

    val listas: StateFlow<List<ListaEntity>> =

        repository
            .listarListas()
            .stateIn(

                scope =
                    viewModelScope,

                started =
                    SharingStarted
                        .WhileSubscribed(
                            5_000
                        ),

                initialValue =
                    emptyList()
            )


    // ====================================================
    // SELEÇÃO ATUAL
    // null = ainda carregando
    // ====================================================

    private val _idsListaEdicao =
        MutableStateFlow<List<Int>?>(
            null
        )


    val idsListaEdicao:
            StateFlow<List<Int>?> =
        _idsListaEdicao
            .asStateFlow()


    // ====================================================
    // CRIAR LISTA
    // ====================================================

    fun criarLista(
        nome: String
    ) {

        val nomeLimpo =
            nome.trim()


        if (
            nomeLimpo.isBlank()
        ) {
            return
        }


        viewModelScope.launch {

            repository
                .criarLista(
                    nomeLimpo
                )
        }
    }


    // ====================================================
    // CARREGAR CANTOS DA LISTA
    // ====================================================

    fun carregarCantosDaLista(
        listaId: Long
    ) {

        _idsListaEdicao.value =
            null


        viewModelScope.launch {

            val ids =
                repository
                    .buscarIdsDaLista(
                        listaId
                    )


            _idsListaEdicao.value =
                ids
        }
    }


    // ====================================================
    // SALVAR CANTOS
    // ====================================================

    fun salvarCantosDaLista(

        listaId: Long,

        cantoIds: List<Int>,

        onConcluido: () -> Unit

    ) {

        viewModelScope.launch {

            repository
                .salvarCantosDaLista(

                    listaId =
                        listaId,

                    cantoIds =
                        cantoIds
                )


            _idsListaEdicao.value =
                cantoIds


            onConcluido()
        }
    }
}