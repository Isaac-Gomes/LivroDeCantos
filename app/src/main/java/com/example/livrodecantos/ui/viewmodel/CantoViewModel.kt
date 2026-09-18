package com.example.livrodecantos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.livrodecantos.data.database.RessuscitouDatabase
import com.example.livrodecantos.data.repository.CantoRepository
import com.example.livrodecantos.model.Canto
import com.example.livrodecantos.model.Ficha
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.livrodecantos.model.ItemIndiceBiblico
import com.example.livrodecantos.data.repository.NumeracaoV2Updater


class CantoViewModel(
    application: Application
) : AndroidViewModel(application) {


    private val database =
        RessuscitouDatabase
            .getDatabase(application)


    private val repository =
        CantoRepository(

            context = application,

            cantoDao =
                database.cantoDao(),

            fichaDao =
                database.fichaDao(),

            indiceLiturgicoDao =
                database.indiceLiturgicoDao(),

            indiceBiblicoDao =
                database.indiceBiblicoDao()
        )


    val cantos: StateFlow<List<Canto>> =

        repository.cantos.stateIn(

            scope = viewModelScope,

            started =
                SharingStarted
                    .WhileSubscribed(5000),

            initialValue =
                emptyList()
        )


    fun fichasDoCanto(
        cantoId: Int
    ): Flow<List<Ficha>> {

        return repository
            .fichasDoCanto(cantoId)
    }


    fun subcategoriasLiturgicas():
            Flow<List<String>> {

        return repository
            .subcategoriasLiturgicas()
    }


    fun cantosPorSubcategoriaLiturgica(
        subcategoria: String
    ): Flow<List<Canto>> {

        return repository
            .cantosPorSubcategoriaLiturgica(
                subcategoria
            )
    }
    fun testamentosBiblicos():
            Flow<List<String>> {

        return repository
            .testamentosBiblicos()
    }


    fun itensBiblicosPorTestamento(
        testamento: String
    ): Flow<List<ItemIndiceBiblico>> {

        return repository
            .itensBiblicosPorTestamento(
                testamento
            )
    }

    init {
        viewModelScope.launch {

            NumeracaoV2Updater.aplicar(

                context =
                    getApplication(),

                cantoDao =
                    database.cantoDao()
            )
        }
        viewModelScope.launch {

            repository
                .importarCatalogoSeNecessario()
        }
    }
}