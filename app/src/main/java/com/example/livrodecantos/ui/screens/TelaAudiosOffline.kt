package com.example.livrodecantos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.livrodecantos.model.Canto
import com.example.livrodecantos.ui.viewmodel.DownloadTodosViewModel
import java.io.File
import java.text.Collator
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaAudiosOffline(

    cantos: List<Canto>,

    downloadTodosViewModel: DownloadTodosViewModel,

    onVoltar: () -> Unit,

    onCantoClick: (Canto) -> Unit,

    onExcluirAudio: (Canto) -> Unit,

    onExcluirTodosAudios: (List<Canto>) -> Unit

) {

    // ====================================================
    // ESTADOS
    // ====================================================

    var cantoParaExcluir by remember {
        mutableStateOf<Canto?>(null)
    }


    var confirmarExcluirTodos by remember {
        mutableStateOf(false)
    }


    val estadoDownload by
    downloadTodosViewModel
        .estado
        .collectAsState()


    // ====================================================
    // ORDENAÇÃO EM PORTUGUÊS
    // ====================================================

    val comparador = remember {

        Collator.getInstance(
            Locale.forLanguageTag(
                "pt-BR"
            )
        ).apply {

            strength =
                Collator.PRIMARY
        }
    }


    // ====================================================
    // ÁUDIOS REALMENTE OFFLINE
    // ====================================================

    val cantosOffline =

        cantos

            .filter { canto ->

                canto.audioLocalPath
                    ?.let { caminho ->

                        File(caminho)
                            .exists()
                    }
                    ?: false
            }

            .sortedWith { canto1, canto2 ->

                comparador.compare(
                    canto1.titulo,
                    canto2.titulo
                )
            }


    // ====================================================
    // TODOS QUE POSSUEM ÁUDIO
    // ====================================================

    val cantosComAudio =

        cantos.filter { canto ->

            canto.possuiAudio
        }


    // ====================================================
    // QUANTOS AINDA PRECISAM SER BAIXADOS
    // ====================================================

    val quantidadeParaBaixar =

        cantosComAudio.count { canto ->

            canto.audioLocalPath
                ?.let { caminho ->

                    !File(caminho)
                        .exists()
                }
                ?: true
        }


    // ====================================================
    // ESPAÇO TOTAL UTILIZADO
    // ====================================================

    val bytesUtilizados =

        cantosOffline.sumOf { canto ->

            canto.audioLocalPath
                ?.let { caminho ->

                    val arquivo =
                        File(caminho)


                    if (
                        arquivo.exists()
                    ) {

                        arquivo.length()

                    } else {

                        0L
                    }
                }
                ?: 0L
        }


    // ====================================================
    // TELA
    // ====================================================

    Scaffold(

        containerColor =
            Color(0xFF1E1E1E),

        topBar = {

            TopAppBar(

                title = {

                    Text(

                        text =
                            "ÁUDIOS OFFLINE",

                        color =
                            Color.White,

                        fontSize =
                            18.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick =
                            onVoltar
                    ) {

                        Icon(

                            imageVector =
                                Icons
                                    .AutoMirrored
                                    .Filled
                                    .ArrowBack,

                            contentDescription =
                                "Voltar",

                            tint =
                                Color.White
                        )
                    }
                },

                colors =
                    TopAppBarDefaults
                        .topAppBarColors(

                            containerColor =
                                Color(0xFF121212)
                        )
            )
        }

    ) { innerPadding ->


        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(
                    innerPadding
                )

        ) {


            // ====================================================
            // CABEÇALHO
            // ====================================================

            Column(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 16.dp
                    )

            ) {


                // ------------------------------------------------
                // OFFLINE
                // ------------------------------------------------

                Text(

                    text =

                        if (
                            cantosOffline.size == 1
                        ) {

                            "1 áudio disponível offline"

                        } else {

                            "${cantosOffline.size} áudios disponíveis offline"
                        },

                    color =
                        Color(0xFF9E9E9E),

                    fontSize =
                        12.sp,

                    fontWeight =
                        FontWeight.Bold
                )


                Text(

                    text =
                        "Espaço utilizado: ${formatarTamanho(bytesUtilizados)}",

                    color =
                        Color(0xFF9E9E9E),

                    fontSize =
                        12.sp,

                    modifier =
                        Modifier.padding(
                            top = 4.dp
                        )
                )


                // ====================================================
                // RESUMO GLOBAL
                // ====================================================

                Text(

                    text =
                        "${cantosComAudio.size} áudios disponíveis no aplicativo",

                    color =
                        Color(0xFFBDBDBD),

                    fontSize =
                        13.sp,

                    modifier =
                        Modifier.padding(
                            top = 14.dp
                        )
                )


                Text(

                    text =

                        if (
                            quantidadeParaBaixar == 0
                        ) {

                            "Todos os áudios estão disponíveis offline"

                        } else {

                            "$quantidadeParaBaixar ainda não baixados"
                        },

                    color =

                        if (
                            quantidadeParaBaixar == 0
                        ) {

                            Color(0xFF81C784)

                        } else {

                            Color(0xFF9E9E9E)
                        },

                    fontSize =
                        13.sp,

                    modifier =
                        Modifier.padding(
                            top = 3.dp
                        )
                )


                // ====================================================
                // BAIXAR TODOS
                // ====================================================

                Button(

                    enabled =
                        quantidadeParaBaixar > 0 &&
                                !estadoDownload.executando,

                    onClick = {

                        downloadTodosViewModel
                            .baixarTodos()
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 12.dp
                        )

                ) {

                    Icon(

                        imageVector =
                            Icons.Default.Download,

                        contentDescription =
                            null
                    )


                    Text(

                        text =

                            if (
                                estadoDownload.executando
                            ) {

                                "Baixando..."

                            } else if (
                                quantidadeParaBaixar == 0
                            ) {

                                "Todos baixados"

                            } else {

                                "Baixar todos os áudios"
                            },

                        modifier =
                            Modifier.padding(
                                start = 8.dp
                            )
                    )
                }


                // ====================================================
                // PROGRESSO
                // ====================================================

                if (
                    estadoDownload.executando
                ) {

                    val total =
                        estadoDownload.total


                    val processados =
                        estadoDownload.processados


                    val progresso =

                        if (
                            total > 0
                        ) {

                            processados.toFloat() /
                                    total.toFloat()

                        } else {

                            0f
                        }


                    LinearProgressIndicator(

                        progress = {

                            progresso.coerceIn(
                                0f,
                                1f
                            )
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 12.dp
                            )
                    )


                    Text(

                        text =

                            if (
                                total > 0
                            ) {

                                "$processados de $total processados"

                            } else {

                                "Preparando downloads..."
                            },

                        color =
                            Color(0xFFBDBDBD),

                        fontSize =
                            12.sp,

                        modifier =
                            Modifier.padding(
                                top = 6.dp
                            )
                    )


                    estadoDownload
                        .tituloAtual
                        ?.let { titulo ->

                            Text(

                                text =
                                    titulo,

                                color =
                                    Color(0xFF888888),

                                fontSize =
                                    12.sp,

                                maxLines =
                                    1,

                                modifier =
                                    Modifier.padding(
                                        top = 2.dp
                                    )
                            )
                        }


                    if (
                        estadoDownload.falhas > 0
                    ) {

                        Text(

                            text =
                                "${estadoDownload.falhas} falha(s) até agora",

                            color =
                                Color(0xFFFFB74D),

                            fontSize =
                                12.sp,

                            modifier =
                                Modifier.padding(
                                    top = 4.dp
                                )
                        )
                    }
                }


                // ====================================================
                // DOWNLOAD FINALIZADO
                // ====================================================

                if (
                    estadoDownload.concluido &&
                    !estadoDownload.executando
                ) {

                    Text(

                        text =

                            if (
                                estadoDownload.falhas == 0
                            ) {

                                "Download concluído."

                            } else {

                                "Download concluído com ${estadoDownload.falhas} falha(s)."
                            },

                        color =

                            if (
                                estadoDownload.falhas == 0
                            ) {

                                Color(0xFF81C784)

                            } else {

                                Color(0xFFFFB74D)
                            },

                        fontSize =
                            13.sp,

                        modifier =
                            Modifier.padding(
                                top = 10.dp
                            )
                    )
                }


                // ====================================================
                // EXCLUIR TODOS
                // ====================================================

                if (
                    cantosOffline.isNotEmpty()
                ) {

                    TextButton(

                        onClick = {

                            confirmarExcluirTodos =
                                true
                        },

                        modifier =
                            Modifier.padding(
                                top = 6.dp
                            )

                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.Delete,

                            contentDescription =
                                null,

                            tint =
                                Color(0xFFFF8A80),

                            modifier =
                                Modifier.size(
                                    18.dp
                                )
                        )


                        Text(

                            text =
                                "Excluir todos os áudios",

                            color =
                                Color(0xFFFF8A80),

                            modifier =
                                Modifier.padding(
                                    start = 6.dp
                                )
                        )
                    }
                }
            }


            HorizontalDivider(

                color =
                    Color(0xFF333333)
            )


            // ====================================================
            // LISTA / ESTADO VAZIO
            // ====================================================

            if (
                cantosOffline.isEmpty()
            ) {

                Text(

                    text =
                        "Nenhum áudio foi baixado ainda.",

                    color =
                        Color(0xFFBDBDBD),

                    fontSize =
                        15.sp,

                    modifier =
                        Modifier.padding(
                            24.dp
                        )
                )

            } else {

                LazyColumn(

                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()

                ) {

                    items(

                        items =
                            cantosOffline,

                        key = { canto ->

                            canto.id
                        }

                    ) { canto ->

                        ItemAudioOffline(

                            canto =
                                canto,

                            onClick = {

                                onCantoClick(
                                    canto
                                )
                            },

                            onExcluir = {

                                cantoParaExcluir =
                                    canto
                            }
                        )
                    }
                }
            }
        }
    }


    // ====================================================
    // DIÁLOGO — EXCLUIR UM
    // ====================================================

    cantoParaExcluir
        ?.let { canto ->

            AlertDialog(

                onDismissRequest = {

                    cantoParaExcluir =
                        null
                },

                title = {

                    Text(
                        text =
                            "Excluir áudio?"
                    )
                },

                text = {

                    Text(

                        text =
                            "O áudio de \"${canto.titulo}\" será removido do aparelho. Você poderá baixá-lo novamente depois."
                    )
                },

                confirmButton = {

                    TextButton(

                        onClick = {

                            onExcluirAudio(
                                canto
                            )

                            cantoParaExcluir =
                                null
                        }

                    ) {

                        Text(

                            text =
                                "Excluir",

                            color =
                                Color(0xFFFF6B6B)
                        )
                    }
                },

                dismissButton = {

                    TextButton(

                        onClick = {

                            cantoParaExcluir =
                                null
                        }

                    ) {

                        Text(
                            text =
                                "Cancelar"
                        )
                    }
                }
            )
        }


    // ====================================================
    // DIÁLOGO — EXCLUIR TODOS
    // ====================================================

    if (
        confirmarExcluirTodos
    ) {

        AlertDialog(

            onDismissRequest = {

                confirmarExcluirTodos =
                    false
            },

            title = {

                Text(
                    text =
                        "Excluir todos os áudios?"
                )
            },

            text = {

                Text(

                    text =
                        "Serão removidos ${cantosOffline.size} áudios baixados, ocupando ${formatarTamanho(bytesUtilizados)}. As fichas e os cantos continuarão disponíveis normalmente."
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        val audiosParaExcluir =
                            cantosOffline.toList()


                        confirmarExcluirTodos =
                            false


                        onExcluirTodosAudios(
                            audiosParaExcluir
                        )
                    }

                ) {

                    Text(

                        text =
                            "Excluir todos",

                        color =
                            Color(0xFFFF6B6B)
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        confirmarExcluirTodos =
                            false
                    }

                ) {

                    Text(
                        text =
                            "Cancelar"
                    )
                }
            }
        )
    }
}


// ====================================================
// ITEM DE ÁUDIO OFFLINE
// ====================================================

@Composable
private fun ItemAudioOffline(

    canto: Canto,

    onClick: () -> Unit,

    onExcluir: () -> Unit

) {

    Column(

        modifier =
            Modifier.fillMaxWidth()

    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .clickable {

                    onClick()
                }
                .padding(
                    start = 20.dp,
                    top = 10.dp,
                    bottom = 10.dp,
                    end = 6.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically

        ) {

            Text(

                text =
                    canto.titulo,

                color =
                    Color.White,

                fontSize =
                    15.sp,

                modifier =
                    Modifier.weight(
                        1f
                    )
            )


            Icon(

                imageVector =
                    Icons.Default.CheckCircle,

                contentDescription =
                    "Disponível offline",

                tint =
                    Color(0xFF81C784),

                modifier =
                    Modifier.size(
                        18.dp
                    )
            )


            Icon(

                imageVector =
                    Icons.Default.MusicNote,

                contentDescription =
                    "Áudio disponível offline",

                tint =
                    Color(0xFF81C784),

                modifier =
                    Modifier
                        .padding(
                            start = 4.dp
                        )
                        .size(
                            22.dp
                        )
            )


            IconButton(

                onClick =
                    onExcluir

            ) {

                Icon(

                    imageVector =
                        Icons.Default.Delete,

                    contentDescription =
                        "Excluir áudio",

                    tint =
                        Color(0xFFBDBDBD)
                )
            }
        }


        HorizontalDivider(

            color =
                Color(0xFF303030),

            thickness =
                0.5.dp
        )
    }
}


// ====================================================
// FORMATAR TAMANHO
// ====================================================

private fun formatarTamanho(
    bytes: Long
): String {

    val kb =
        bytes / 1024.0

    val mb =
        kb / 1024.0

    val gb =
        mb / 1024.0


    return when {

        gb >= 1.0 ->

            String.format(
                Locale.forLanguageTag(
                    "pt-BR"
                ),
                "%.2f GB",
                gb
            )


        mb >= 1.0 ->

            String.format(
                Locale.forLanguageTag(
                    "pt-BR"
                ),
                "%.1f MB",
                mb
            )


        kb >= 1.0 ->

            String.format(
                Locale.forLanguageTag(
                    "pt-BR"
                ),
                "%.0f KB",
                kb
            )


        else ->

            "$bytes bytes"
    }
}