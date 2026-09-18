package com.example.livrodecantos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.livrodecantos.model.Canto
import com.example.livrodecantos.ui.viewmodel.AudioViewModel


@Composable
fun MiniPlayerAudio(

    canto: Canto,

    audioViewModel: AudioViewModel,

    onAbrirFicha: () -> Unit,

    onAbrirFila: () -> Unit

) {

    // ====================================================
    // ESTADO DO PLAYER
    // ====================================================

    val estado by
    audioViewModel
        .estado
        .collectAsState()


    // ====================================================
    // FILA
    // ====================================================

    val fila by
    audioViewModel
        .filaReproducao
        .collectAsState()


    // ====================================================
    // SEGURANÇA
    // ====================================================

    if (
        estado.cantoId !=
        canto.id
    ) {
        return
    }


    // ====================================================
    // PROGRESSO
    // ====================================================

    val progresso =

        if (
            estado.duracao >
            0L
        ) {

            (
                    estado.posicao.toFloat() /
                            estado.duracao.toFloat()
                    )
                .coerceIn(
                    0f,
                    1f
                )

        } else {

            0f
        }


    // ====================================================
    // FILA ATIVA
    // ====================================================

    val filaAtiva =

        estado.reproduzindoLista &&
                fila.isNotEmpty() &&
                estado.indiceFila != null


    val indiceAtual =
        estado.indiceFila


    val podeIrAnterior =

        filaAtiva &&
                indiceAtual != null


    val podeIrProximo =

        filaAtiva &&
                indiceAtual != null &&
                indiceAtual <
                fila.lastIndex


    // ====================================================
    // MINI PLAYER
    // ====================================================

    Column(

        modifier =
            Modifier.fillMaxWidth()

    ) {

        HorizontalDivider(

            color =
                Color(0xFF3A3A3A)
        )


        // =================================================
        // LINHA DO CANTO
        // =================================================

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 14.dp,
                    end = 6.dp,
                    top = 5.dp,
                    bottom = 2.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically

        ) {


            // ------------------------------------------------
            // ÍCONE
            // ------------------------------------------------

            Icon(

                imageVector =
                    Icons.Default.MusicNote,

                contentDescription =
                    null,

                tint =

                    if (
                        estado.tocando
                    ) {

                        Color(0xFFD32F2F)

                    } else {

                        Color(0xFFBDBDBD)
                    },

                modifier =
                    Modifier.size(
                        20.dp
                    )
            )


            // ------------------------------------------------
            // TÍTULO + POSIÇÃO DA FILA
            // ------------------------------------------------

            Column(

                modifier = Modifier
                    .weight(1f)
                    .clickable {

                        onAbrirFicha()
                    }
                    .padding(
                        horizontal = 10.dp,
                        vertical = 7.dp
                    )

            ) {

                Text(

                    text =
                        canto.titulo,

                    color =
                        Color.White,

                    fontSize =
                        13.sp,

                    fontWeight =
                        FontWeight.Medium,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )


                // ----------------------------------------
                // INFORMAÇÃO DA PLAYLIST
                // ----------------------------------------

                if (
                    filaAtiva &&
                    indiceAtual != null
                ) {

                    Text(

                        text =
                            buildString {

                                append(
                                    "${indiceAtual + 1} de ${fila.size}"
                                )


                                if (
                                    estado.modoAleatorio
                                ) {

                                    append(
                                        " • Aleatório"
                                    )
                                }
                            },

                        color =
                            Color(0xFF9E9E9E),

                        fontSize =
                            11.sp,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
                    )

                } else {

                    Text(

                        text =

                            if (
                                estado.tocando
                            ) {

                                "Reproduzindo"

                            } else {

                                "Pausado"
                            },

                        color =
                            Color(0xFF888888),

                        fontSize =
                            11.sp
                    )
                }
            }


            // ------------------------------------------------
            // ABRIR FILA
            // ------------------------------------------------

            if (
                filaAtiva
            ) {

                IconButton(

                    onClick = {

                        onAbrirFila()
                    }

                ) {

                    Icon(

                        imageVector =
                            Icons.Default.QueueMusic,

                        contentDescription =
                            "Ver fila de reprodução",

                        tint =
                            Color.White,

                        modifier =
                            Modifier.size(
                                25.dp
                            )
                    )
                }
            }
        }


        // =================================================
        // CONTROLES
        // =================================================

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 4.dp
                ),

            horizontalArrangement =
                Arrangement.Center,

            verticalAlignment =
                Alignment.CenterVertically

        ) {


            // ------------------------------------------------
            // ANTERIOR
            // ------------------------------------------------

            if (
                filaAtiva
            ) {

                IconButton(

                    enabled =
                        podeIrAnterior &&
                                !estado.carregando,

                    onClick = {

                        audioViewModel
                            .anteriorDaFila()
                    }

                ) {

                    Icon(

                        imageVector =
                            Icons.Default.SkipPrevious,

                        contentDescription =
                            "Canto anterior",

                        tint =

                            if (
                                podeIrAnterior &&
                                !estado.carregando
                            ) {

                                Color.White

                            } else {

                                Color(0xFF555555)
                            },

                        modifier =
                            Modifier.size(
                                29.dp
                            )
                    )
                }
            }


            // ------------------------------------------------
            // PLAY / PAUSE / CARREGANDO
            // ------------------------------------------------

            if (
                estado.carregando
            ) {

                CircularProgressIndicator(

                    modifier =
                        Modifier
                            .size(
                                42.dp
                            )
                            .padding(
                                7.dp
                            ),

                    strokeWidth =
                        2.dp
                )

            } else {

                IconButton(

                    onClick = {

                        audioViewModel
                            .alternarReproducao(
                                canto
                            )
                    }

                ) {

                    Icon(

                        imageVector =

                            if (
                                estado.tocando
                            ) {

                                Icons.Default.Pause

                            } else {

                                Icons.Default.PlayArrow
                            },

                        contentDescription =

                            if (
                                estado.tocando
                            ) {

                                "Pausar"

                            } else {

                                "Reproduzir"
                            },

                        tint =
                            Color.White,

                        modifier =
                            Modifier.size(
                                32.dp
                            )
                    )
                }
            }


            // ------------------------------------------------
            // PRÓXIMO
            // ------------------------------------------------

            if (
                filaAtiva
            ) {

                IconButton(

                    enabled =
                        podeIrProximo &&
                                !estado.carregando,

                    onClick = {

                        audioViewModel
                            .proximoDaFila()
                    }

                ) {

                    Icon(

                        imageVector =
                            Icons.Default.SkipNext,

                        contentDescription =
                            "Próximo canto",

                        tint =

                            if (
                                podeIrProximo &&
                                !estado.carregando
                            ) {

                                Color.White

                            } else {

                                Color(0xFF555555)
                            },

                        modifier =
                            Modifier.size(
                                29.dp
                            )
                    )
                }
            }
        }


        // =================================================
        // PROGRESSO
        // =================================================

        LinearProgressIndicator(

            progress = {
                progresso
            },

            modifier =
                Modifier.fillMaxWidth()
        )
    }
}