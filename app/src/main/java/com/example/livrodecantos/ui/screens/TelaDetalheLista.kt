package com.example.livrodecantos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.livrodecantos.data.entity.ListaEntity
import com.example.livrodecantos.model.Canto
import com.example.livrodecantos.ui.viewmodel.ListaViewModel
import java.io.File
import com.example.livrodecantos.ui.viewmodel.AudioViewModel
import androidx.compose.material3.LinearProgressIndicator


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalheLista(

    lista: ListaEntity,

    todosCantos: List<Canto>,

    listaViewModel: ListaViewModel,
    audioViewModel: AudioViewModel,
    onVoltar: () -> Unit,

    onEditar: () -> Unit,

    onCantoClick: (Canto) -> Unit,

    onReproduzir: (List<Canto>) -> Unit,

    onAleatorio: (List<Canto>) -> Unit,


) {

    // ====================================================
    // IDS SALVOS NA LISTA
    // ====================================================
    val estadoAudio by
    audioViewModel
        .estado
        .collectAsState()

    val idsLista by
    listaViewModel
        .idsListaEdicao
        .collectAsState()


    LaunchedEffect(
        lista.id
    ) {

        listaViewModel
            .carregarCantosDaLista(
                lista.id
            )
    }


    // ====================================================
    // CANTOS NA ORDEM DA LISTA
    // ====================================================

    val cantosDaLista =

        idsLista
            ?.mapNotNull { cantoId ->

                todosCantos
                    .firstOrNull { canto ->

                        canto.id ==
                                cantoId
                    }
            }
            ?: emptyList()


    val cantosComAudio =

        cantosDaLista
            .filter { canto ->

                canto.possuiAudio
            }


    val quantidadeOffline =

        cantosComAudio
            .count { canto ->

                canto.audioLocalPath
                    ?.let { caminho ->

                        File(caminho)
                            .exists()
                    }
                    ?: false
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
                            lista.nome,

                        color =
                            Color.White,

                        fontSize =
                            18.sp,

                        fontWeight =
                            FontWeight.Bold,

                        maxLines =
                            1
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


        if (
            idsLista == null
        ) {

            Column(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    ),

                horizontalAlignment =
                    Alignment.CenterHorizontally

            ) {

                CircularProgressIndicator(

                    modifier =
                        Modifier.padding(
                            top = 48.dp
                        )
                )
            }

        } else {


            Column(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )

            ) {


                // ====================================================
                // RESUMO
                // ====================================================

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 16.dp
                        )

                ) {

                    Text(

                        text =

                            if (
                                cantosDaLista.size == 1
                            ) {

                                "1 canto"

                            } else {

                                "${cantosDaLista.size} cantos"
                            },

                        color =
                            Color.White,

                        fontSize =
                            17.sp,

                        fontWeight =
                            FontWeight.Medium
                    )


                    if (
                        cantosComAudio.isNotEmpty()
                    ) {

                        Text(

                            text =
                                "$quantidadeOffline de ${cantosComAudio.size} áudios disponíveis offline",

                            color =
                                Color(0xFF9E9E9E),

                            fontSize =
                                13.sp,

                            modifier =
                                Modifier.padding(
                                    top = 4.dp
                                )
                        )
                    }
                }


                // ====================================================
                // REPRODUÇÃO
                // ====================================================

                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp
                        ),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )

                ) {

                    Button(

                        enabled =
                            cantosComAudio.isNotEmpty(),

                        onClick = {

                            onReproduzir(
                                cantosComAudio
                            )
                        },

                        modifier =
                            Modifier.weight(
                                1f
                            )

                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.PlayArrow,

                            contentDescription =
                                null
                        )


                        Text(

                            text =
                                "Reproduzir",

                            modifier =
                                Modifier.padding(
                                    start = 6.dp
                                )
                        )
                    }


                    OutlinedButton(

                        enabled =
                            cantosComAudio.isNotEmpty(),

                        onClick = {

                            onAleatorio(
                                cantosComAudio
                            )
                        },

                        modifier =
                            Modifier.weight(
                                1f
                            )

                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.Shuffle,

                            contentDescription =
                                null
                        )


                        Text(

                            text =
                                "Aleatório",

                            modifier =
                                Modifier.padding(
                                    start = 6.dp
                                )
                        )
                    }
                }


                // ====================================================
                // DOWNLOAD / EDIÇÃO
                // ====================================================

                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp
                        ),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )

                ) {

                    OutlinedButton(

                        enabled =

                            cantosComAudio.isNotEmpty() &&
                                    !estadoAudio.baixandoEmLote &&
                                    quantidadeOffline < cantosComAudio.size,

                        onClick = {

                            audioViewModel
                                .baixarTodosParaOffline(
                                    cantosComAudio
                                )
                        },

                        modifier =
                            Modifier.weight(1f)

                    ) {

                        if (
                            estadoAudio.baixandoEmLote
                        ) {

                            CircularProgressIndicator(

                                modifier =
                                    Modifier.size(
                                        18.dp
                                    ),

                                strokeWidth =
                                    2.dp
                            )


                            Text(

                                text =
                                    "${estadoAudio.processadosDownloadLote}/${estadoAudio.totalDownloadLote}",

                                modifier =
                                    Modifier.padding(
                                        start = 7.dp
                                    )
                            )

                        } else {

                            Icon(

                                imageVector =
                                    Icons.Default.Download,

                                contentDescription =
                                    null
                            )


                            Text(

                                text =

                                    if (
                                        cantosComAudio.isNotEmpty() &&
                                        quantidadeOffline ==
                                        cantosComAudio.size
                                    ) {

                                        "Tudo offline"

                                    } else {

                                        "Baixar todos"
                                    },

                                modifier =
                                    Modifier.padding(
                                        start = 6.dp
                                    )
                            )
                        }
                    }


                    OutlinedButton(

                        onClick =
                            onEditar,

                        modifier =
                            Modifier.weight(
                                1f
                            )

                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.Edit,

                            contentDescription =
                                null
                        )


                        Text(

                            text =
                                "Editar",

                            modifier =
                                Modifier.padding(
                                    start = 6.dp
                                )
                        )
                    }
                }


                HorizontalDivider(

                    color =
                        Color(0xFF333333),

                    modifier =
                        Modifier.padding(
                            top = 16.dp
                        )
                )


                // ====================================================
                // LISTA VAZIA
                // ====================================================

                if (
                    cantosDaLista.isEmpty()
                ) {

                    Column(

                        modifier =
                            Modifier.padding(
                                24.dp
                            )

                    ) {

                        Text(

                            text =
                                "Esta lista ainda não possui cantos.",

                            color =
                                Color(0xFFBDBDBD),

                            fontSize =
                                15.sp
                        )


                        Text(

                            text =
                                "Toque em Editar para selecionar os cantos.",

                            color =
                                Color(0xFF888888),

                            fontSize =
                                13.sp,

                            modifier =
                                Modifier.padding(
                                    top = 6.dp
                                )
                        )
                    }

                } else {
                    if (
                        estadoAudio.baixandoEmLote
                    ) {

                        Column(

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 20.dp,
                                    vertical = 10.dp
                                )

                        ) {

                            val total =
                                estadoAudio.totalDownloadLote


                            val processados =
                                estadoAudio.processadosDownloadLote


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

                                modifier =
                                    Modifier.fillMaxWidth()
                            )


                            Text(

                                text =
                                    "Baixando ${processados + 1} de $total",

                                color =
                                    Color(0xFFBDBDBD),

                                fontSize =
                                    12.sp,

                                modifier =
                                    Modifier.padding(
                                        top = 6.dp
                                    )
                            )


                            estadoAudio
                                .tituloDownloadAtual
                                ?.let { titulo ->

                                    Text(

                                        text =
                                            titulo,

                                        color =
                                            Color(0xFF888888),

                                        fontSize =
                                            12.sp,

                                        maxLines =
                                            1
                                    )
                                }
                        }
                    }

                    // ====================================================
                    // CANTOS
                    // ====================================================

                    LazyColumn(

                        modifier =
                            Modifier.fillMaxSize()

                    ) {

                        itemsIndexed(

                            items =
                                cantosDaLista,

                            key = {
                                    _,
                                    canto ->

                                canto.id
                            }

                        ) {
                                indice,
                                canto ->


                            ItemCantoDaLista(

                                numero =
                                    indice + 1,

                                canto =
                                    canto,

                                onClick = {

                                    onCantoClick(
                                        canto
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


// ====================================================
// ITEM DE CANTO
// ====================================================

@Composable
private fun ItemCantoDaLista(

    numero: Int,

    canto: Canto,

    onClick: () -> Unit

) {

    val offline =

        canto.audioLocalPath
            ?.let { caminho ->

                File(caminho)
                    .exists()
            }
            ?: false


    Column(

        modifier = Modifier
            .fillMaxWidth()
            .clickable {

                onClick()
            }

    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp,
                    vertical = 15.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically

        ) {


            Text(

                text =
                    "$numero.",

                color =
                    Color(0xFF888888),

                fontSize =
                    14.sp,

                modifier =
                    Modifier.padding(
                        end = 14.dp
                    )
            )


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


            if (
                canto.possuiAudio
            ) {

                if (
                    offline
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.CheckCircle,

                        contentDescription =
                            "Áudio disponível offline",

                        tint =
                            Color(0xFF81C784),

                        modifier =
                            Modifier.size(
                                18.dp
                            )
                    )
                }


                Icon(

                    imageVector =
                        Icons.Default.MusicNote,

                    contentDescription =
                        "Possui áudio",

                    tint =

                        if (
                            offline
                        ) {

                            Color(0xFF81C784)

                        } else {

                            Color(0xFFBDBDBD)
                        },

                    modifier = Modifier
                        .padding(
                            start = 4.dp
                        )
                        .size(
                            22.dp
                        )
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