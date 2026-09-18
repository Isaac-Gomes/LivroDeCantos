package com.example.livrodecantos.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.livrodecantos.data.database.RessuscitouDatabase
import com.example.livrodecantos.data.entity.CantoEntity
import com.example.livrodecantos.data.repository.AudioRepository
import com.example.livrodecantos.model.Canto
import com.example.livrodecantos.model.Etapa
import java.io.File


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
                break
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
                processados
            )

            .putInt(
                CHAVE_FALHAS,
                falhas
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
                    "Ressuscitou"
                )

                .setContentText(

                    if (
                        total > 0
                    ) {

                        "Baixando $processados de $total • $titulo"

                    } else {

                        "Preparando downloads..."
                    }
                )

                .setOnlyAlertOnce(
                    true
                )

                .setOngoing(
                    true
                )

                .setProgress(

                    total.coerceAtLeast(
                        1
                    ),

                    processados.coerceAtMost(
                        total
                    ),

                    false
                )

                .build()


        return ForegroundInfo(

            NOTIFICACAO_ID,

            notification
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
                "Downloads offline do Ressuscitou"


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