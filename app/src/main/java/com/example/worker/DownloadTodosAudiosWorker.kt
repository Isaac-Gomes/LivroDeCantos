package com.example.livrodecantos.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.ServiceInfo
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import com.example.livrodecantos.data.database.RessuscitouDatabase
import com.example.livrodecantos.data.entity.CantoEntity
import com.example.livrodecantos.data.repository.AudioRepository
import com.example.livrodecantos.model.Canto
import com.example.livrodecantos.model.Etapa
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CancellationException


class DownloadTodosAudiosWorker(

    appContext: Context,

    workerParams: WorkerParameters

) : CoroutineWorker(
    appContext,
    workerParams
) {


    companion object {

        const val NOME_TRABALHO =
            "download_todos_audios"


        const val CHAVE_TOTAL =
            "total"


        const val CHAVE_PROCESSADOS =
            "processados"


        const val CHAVE_FALHAS =
            "falhas"


        const val CHAVE_TITULO =
            "titulo"

        const val CHAVE_ERRO =
            "erro"

        private const val TAG =
            "DownloadTodosWorker"


        private const val CANAL_ID =
            "download_audios"


        private const val NOTIFICACAO_ID =
            1001
    }


    // ====================================================
    // EXECUTAR
    // ====================================================

    override suspend fun doWork():
            Result {

        return try {
            executarDownloads()
        } catch (erro: CancellationException) {
            Log.i(TAG, "Download em massa cancelado pelo WorkManager (motivo: $stopReason).", erro)
            throw erro
        } catch (erro: IOException) {
            Log.e(TAG, "Falha temporária fora do download individual.", erro)
            if (runAttemptCount < 2) {
                Result.retry()
            } else {
                resultadoFalhaGlobal()
            }
        } catch (erro: Exception) {
            Log.e(TAG, "Falha irrecuperável no download em massa.", erro)
            resultadoFalhaGlobal()
        }
    }


    private suspend fun executarDownloads():
            Result {

        criarCanalNotificacao()


        val database =
            RessuscitouDatabase
                .getDatabase(
                    applicationContext
                )


        val cantoDao =
            database.cantoDao()


        val repository =
            AudioRepository(

                context =
                    applicationContext,

                cantoDao =
                    cantoDao
            )


        // =================================================
        // TODOS OS CANTOS COM ÁUDIO
        // =================================================

        val entidades =
            cantoDao
                .listarCantosComAudioAgora()


        // =================================================
        // IGNORAR OS QUE JÁ ESTÃO OFFLINE
        // =================================================

        val paraBaixar =

            entidades
                .filter { entidade ->

                    val caminho =
                        entidade.audioLocalPath


                    caminho.isNullOrBlank() ||
                            !File(caminho).exists()
                }


        val total =
            paraBaixar.size


        // =================================================
        // NADA PARA FAZER
        // =================================================

        if (
            total == 0
        ) {

            return Result.success(

                Data.Builder()

                    .putInt(
                        CHAVE_TOTAL,
                        0
                    )

                    .putInt(
                        CHAVE_PROCESSADOS,
                        0
                    )

                    .putInt(
                        CHAVE_FALHAS,
                        0
                    )

                    .build()
            )
        }


        // =================================================
        // TRANSFORMAR EM FOREGROUND
        // =================================================

        setForeground(

            criarForegroundInfo(

                processados =
                    0,

                total =
                    total,

                titulo =
                    "Preparando downloads..."
            )
        )


        var processados =
            0


        var falhas =
            0


        // =================================================
        // BAIXAR UM POR UM
        // =================================================

        for (
        entidade in paraBaixar
        ) {

            if (
                isStopped
            ) {
                Log.i(TAG, "Worker interrompido antes de concluir o lote (motivo: $stopReason).")
                return Result.retry()
            }


            val canto =
                entidade.toCanto()


            // ---------------------------------------------
            // PROGRESSO
            // ---------------------------------------------

            setProgress(

                criarDadosProgresso(

                    total =
                        total,

                    processados =
                        processados,

                    falhas =
                        falhas,

                    titulo =
                        canto.titulo
                )
            )


            setForeground(

                criarForegroundInfo(

                    processados =
                        processados,

                    total =
                        total,

                    titulo =
                        canto.titulo
                )
            )


            // ---------------------------------------------
            // DOWNLOAD
            // ---------------------------------------------

            val resultado =
                repository
                    .prepararAudio(
                        canto
                    )


            if (
                resultado.isFailure
            ) {

                falhas++

                Log.e(
                    TAG,
                    "Falha ao baixar o áudio de ${canto.titulo}.",
                    resultado.exceptionOrNull()
                )
            }


            processados++


            // ---------------------------------------------
            // ATUALIZAR PROGRESSO
            // ---------------------------------------------

            setProgress(

                criarDadosProgresso(

                    total =
                        total,

                    processados =
                        processados,

                    falhas =
                        falhas,

                    titulo =
                        canto.titulo
                )
            )


        }


        // =================================================
        // RESULTADO
        // =================================================

        val resultadoFinal =

            Data.Builder()

                .putInt(
                    CHAVE_TOTAL,
                    total
                )

                .putInt(
                    CHAVE_PROCESSADOS,
                    processados
                )

                .putInt(
                    CHAVE_FALHAS,
                    falhas
                )

                .build()


        return Result.success(
            resultadoFinal
        )
    }


    private fun resultadoFalhaGlobal(): Result {

        return Result.failure(
            Data.Builder()
                .putString(
                    CHAVE_ERRO,
                    "Não foi possível concluir o download dos áudios."
                )
                .build()
        )
    }


    // ====================================================
    // DADOS DE PROGRESSO
    // ====================================================

    private fun criarDadosProgresso(

        total: Int,

        processados: Int,

        falhas: Int,

        titulo: String

    ): Data {

        return Data.Builder()

            .putInt(
                CHAVE_TOTAL,
                total
            )

            .putInt(
                CHAVE_PROCESSADOS,
                processados.coerceIn(0, total)
            )

            .putInt(
                CHAVE_FALHAS,
                falhas.coerceIn(0, processados.coerceIn(0, total))
            )

            .putString(
                CHAVE_TITULO,
                titulo
            )

            .build()
    }


    // ====================================================
    // NOTIFICAÇÃO
    // ====================================================

    private fun criarForegroundInfo(

        processados: Int,

        total: Int,

        titulo: String

    ): ForegroundInfo {

        val totalSeguro = total.coerceAtLeast(1)
        val processadosSeguros = processados.coerceIn(0, totalSeguro)
        val percentual = if (total > 0) {
            (processadosSeguros * 100) / total
        } else {
            0
        }

        val textoProgresso =
            if (total > 0) {
                "$processadosSeguros de $total • $percentual%"
            } else {
                "Preparando downloads..."
            }

        val notification =

            NotificationCompat
                .Builder(
                    applicationContext,
                    CANAL_ID
                )

                .setSmallIcon(
                    android.R.drawable.stat_sys_download
                )

                .setContentTitle(
                    "${applicationContext.getString(com.example.livrodecantos.R.string.app_name)} — Baixando áudios"
                )

                .setContentText(
                    textoProgresso
                )

                .setSubText(
                    "Agora: $titulo"
                )

                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("$textoProgresso\nAgora: $titulo")
                )

                .setOnlyAlertOnce(
                    true
                )

                .setOngoing(
                    true
                )

                .setProgress(
                    totalSeguro,
                    processadosSeguros,
                    false
                )

                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Cancelar",
                    WorkManager
                        .getInstance(applicationContext)
                        .createCancelPendingIntent(id)
                )

                .build()


        return ForegroundInfo(

            NOTIFICACAO_ID,

            notification,

            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }


    // ====================================================
    // CANAL DA NOTIFICAÇÃO
    // ====================================================

    private fun criarCanalNotificacao() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val manager =
                applicationContext
                    .getSystemService(
                        Context.NOTIFICATION_SERVICE
                    ) as NotificationManager


            val channel =

                NotificationChannel(

                    CANAL_ID,

                    "Download de áudios",

                    NotificationManager
                        .IMPORTANCE_LOW
                )


            channel.description =
                "Downloads offline do ${applicationContext.getString(com.example.livrodecantos.R.string.app_name)}"


            manager.createNotificationChannel(
                channel
            )
        }
    }
}


// ====================================================
// ENTITY → MODEL
// ====================================================

private fun CantoEntity.toCanto():
        Canto {

    return Canto(

        id =
            id,

        numero =
            numero,

        titulo =
            titulo,

        etapa =
            Etapa.valueOf(
                etapa
            ),

        possuiAudio =
            possuiAudio,

        paginaUrl =
            paginaUrl,

        audioArquivoOriginal =
            audioArquivoOriginal,

        audioUrl =
            audioUrl,

        audioLocalPath =
            audioLocalPath
    )
}
