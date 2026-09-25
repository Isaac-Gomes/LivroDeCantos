package com.example.livrodecantos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.livrodecantos.worker.DownloadTodosAudiosWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class DownloadTodosUiState(

    val executando: Boolean = false,

    val concluido: Boolean = false,

    val estadoTrabalho: WorkInfo.State? = null,

    val mensagemFinal: String? = null,

    val total: Int = 0,

    val processados: Int = 0,

    val falhas: Int = 0,

    val tituloAtual: String? = null
)


class DownloadTodosViewModel(
    application: Application
) : AndroidViewModel(application) {


    private val workManager =
        WorkManager.getInstance(
            application
        )


    private val _estado =
        MutableStateFlow(
            DownloadTodosUiState()
        )


    val estado:
            StateFlow<DownloadTodosUiState> =
        _estado.asStateFlow()


    init {

        observarTrabalho()
    }


    // ====================================================
    // INICIAR DOWNLOAD
    // ====================================================

    fun baixarTodos() {

        val constraints =

            Constraints.Builder()

                .setRequiredNetworkType(
                    NetworkType.CONNECTED
                )

                .setRequiresStorageNotLow(true)

                .build()


        val request =

            OneTimeWorkRequestBuilder<
                    DownloadTodosAudiosWorker
                    >()

                .setConstraints(
                    constraints
                )

                .build()


        workManager
            .enqueueUniqueWork(

                DownloadTodosAudiosWorker
                    .NOME_TRABALHO,

                ExistingWorkPolicy.KEEP,

                request
            )
    }


    // ====================================================
    // OBSERVAR
    // ====================================================

    private fun observarTrabalho() {

        viewModelScope.launch {

            workManager
                .getWorkInfosForUniqueWorkFlow(

                    DownloadTodosAudiosWorker
                        .NOME_TRABALHO
                )
                .collect { trabalhos ->


                    val info =
                        trabalhos
                            .firstOrNull { !it.state.isFinished }
                            ?: trabalhos.firstOrNull()
                            ?: return@collect


                    val dados =

                        if (
                            info.state ==
                            WorkInfo.State.SUCCEEDED
                        ) {

                            info.outputData

                        } else {

                            info.progress
                        }


                    _estado.value =

                        DownloadTodosUiState(

                            executando =
                                info.state ==
                                        WorkInfo.State.RUNNING ||

                                        info.state ==
                                        WorkInfo.State.ENQUEUED,

                            concluido =
                                info.state ==
                                        WorkInfo.State.SUCCEEDED,

                            estadoTrabalho = info.state,

                            mensagemFinal = when (info.state) {
                                WorkInfo.State.FAILED ->
                                    info.outputData.getString(
                                        DownloadTodosAudiosWorker.CHAVE_ERRO
                                    ) ?: "Não foi possível concluir o download dos áudios."

                                WorkInfo.State.CANCELLED ->
                                    "Download cancelado. Os áudios já concluídos foram preservados."

                                else -> null
                            },

                            total =
                                dados.getInt(
                                    DownloadTodosAudiosWorker
                                        .CHAVE_TOTAL,

                                    0
                                ),

                            processados =
                                dados.getInt(
                                    DownloadTodosAudiosWorker
                                        .CHAVE_PROCESSADOS,

                                    0
                                ),

                            falhas =
                                dados.getInt(
                                    DownloadTodosAudiosWorker
                                        .CHAVE_FALHAS,

                                    0
                                ),

                            tituloAtual =
                                dados.getString(
                                    DownloadTodosAudiosWorker
                                        .CHAVE_TITULO
                                )
                        )
                }
        }
    }
}
