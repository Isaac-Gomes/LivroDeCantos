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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.livrodecantos.model.Canto
import com.example.livrodecantos.ui.viewmodel.AudioViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaFilaReproducao(

    audioViewModel: AudioViewModel,

    onVoltar: () -> Unit,

    onFilaLimpa: () -> Unit

) {

    val estado by
    audioViewModel
        .estado
        .collectAsState()


    val fila by
    audioViewModel
        .filaReproducao
        .collectAsState()


    val cantoAtual =

        fila.firstOrNull { canto ->

            canto.id ==
                    estado.cantoId
        }


    Scaffold(

        containerColor =
            Color(0xFF1E1E1E),

        topBar = {

            TopAppBar(

                title = {

                    Column {

                        Text(
                            text = "FILA DE REPRODUÇÃO",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )


                        if (
                            fila.isNotEmpty()
                        ) {

                            Text(

                                text = buildString {

                                    if (
                                        estado.modoAleatorio
                                    ) {

                                        append("Aleatório • ")
                                    }

                                    val indice =
                                        estado.indiceFila

                                    if (
                                        indice != null
                                    ) {

                                        append(
                                            "${indice + 1} de ${fila.size}"
                                        )

                                    } else {

                                        append(
                                            "${fila.size} cantos"
                                        )
                                    }
                                },

                                color =
                                    Color(0xFFAAAAAA),

                                fontSize =
                                    12.sp
                            )
                        }
                    }
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
        },

        bottomBar = {

            if (
                fila.isNotEmpty() &&
                cantoAtual != null
            ) {

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 10.dp
                        )

                ) {

                    Text(

                        text =
                            cantoAtual.titulo,

                        color =
                            Color.White,

                        fontWeight =
                            FontWeight.Medium,

                        fontSize =
                            14.sp,

                        maxLines =
                            1,

                        modifier =
                            Modifier.fillMaxWidth()
                    )


                    if (
                        estado.modoAleatorio
                    ) {

                        Row(

                            verticalAlignment =
                                Alignment.CenterVertically,

                            modifier =
                                Modifier.padding(
                                    top = 3.dp
                                )

                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.Shuffle,

                                contentDescription =
                                    null,

                                tint =
                                    Color(0xFFBDBDBD),

                                modifier =
                                    Modifier.size(
                                        15.dp
                                    )
                            )


                            Text(

                                text =
                                    " Ordem aleatória",

                                color =
                                    Color(0xFF999999),

                                fontSize =
                                    11.sp
                            )
                        }
                    }


                    Row(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 6.dp
                            ),

                        horizontalArrangement =
                            Arrangement.SpaceEvenly,

                        verticalAlignment =
                            Alignment.CenterVertically

                    ) {

                        IconButton(

                            onClick = {

                                audioViewModel
                                    .anteriorDaFila()
                            }

                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.SkipPrevious,

                                contentDescription =
                                    "Anterior",

                                tint =
                                    Color.White,

                                modifier =
                                    Modifier.size(
                                        32.dp
                                    )
                            )
                        }


                        Button(

                            onClick = {

                                audioViewModel
                                    .alternarReproducao(
                                        cantoAtual
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
                                    }
                            )
                        }


                        IconButton(

                            onClick = {

                                audioViewModel
                                    .proximoDaFila()
                            }

                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.SkipNext,

                                contentDescription =
                                    "Próximo",

                                tint =
                                    Color.White,

                                modifier =
                                    Modifier.size(
                                        32.dp
                                    )
                            )
                        }
                    }
                }
            }
        }

    ) { innerPadding ->


        if (
            fila.isEmpty()
        ) {

            Column(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )
                    .padding(
                        24.dp
                    )

            ) {

                Text(

                    text =
                        "Nenhuma fila de reprodução ativa.",

                    color =
                        Color(0xFFBDBDBD),

                    fontSize =
                        16.sp
                )


                Text(

                    text =
                        "Abra uma lista e toque em Reproduzir ou Aleatório.",

                    color =
                        Color(0xFF888888),

                    fontSize =
                        13.sp,

                    modifier =
                        Modifier.padding(
                            top = 8.dp
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
                // CABEÇALHO
                // ====================================================

                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 10.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically

                ) {

                    Text(

                        text =

                            if (
                                fila.size == 1
                            ) {

                                "1 canto na fila"

                            } else {

                                "${fila.size} cantos na fila"
                            },

                        color =
                            Color(0xFFBDBDBD),

                        fontSize =
                            13.sp,

                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )


                    TextButton(

                        onClick = {

                            audioViewModel
                                .limparFila()

                            onFilaLimpa()
                        }

                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.DeleteSweep,

                            contentDescription =
                                null,

                            modifier =
                                Modifier.size(
                                    18.dp
                                )
                        )


                        Text(

                            text =
                                "Limpar fila",

                            modifier =
                                Modifier.padding(
                                    start = 4.dp
                                )
                        )
                    }
                }


                HorizontalDivider(
                    color =
                        Color(0xFF333333)
                )


                // ====================================================
                // FILA
                // ====================================================

                LazyColumn(

                    modifier =
                        Modifier.fillMaxSize()

                ) {

                    itemsIndexed(

                        items =
                            fila,

                        key = {
                                _,
                                canto ->

                            canto.id
                        }

                    ) {
                            indice,
                            canto ->


                        ItemFila(

                            numero =
                                indice + 1,

                            canto =
                                canto,

                            atual =
                                canto.id ==
                                        estado.cantoId,

                            tocando =
                                canto.id ==
                                        estado.cantoId &&
                                        estado.tocando,

                            onClick = {

                                audioViewModel
                                    .reproduzirItemDaFila(
                                        indice
                                    )
                            }
                        )
                    }
                }
            }
        }
    }
}


// ====================================================
// ITEM DA FILA
// ====================================================

@Composable
private fun ItemFila(

    numero: Int,

    canto: Canto,

    atual: Boolean,

    tocando: Boolean,

    onClick: () -> Unit

) {

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

                    if (
                        atual
                    ) {

                        Color(0xFFD32F2F)

                    } else {

                        Color(0xFF888888)
                    },

                fontSize =
                    14.sp,

                fontWeight =

                    if (
                        atual
                    ) {

                        FontWeight.Bold

                    } else {

                        FontWeight.Normal
                    },

                modifier =
                    Modifier.padding(
                        end = 14.dp
                    )
            )


            Column(

                modifier =
                    Modifier.weight(
                        1f
                    )

            ) {

                Text(

                    text =
                        canto.titulo,

                    color =

                        if (
                            atual
                        ) {

                            Color.White

                        } else {

                            Color(0xFFE0E0E0)
                        },

                    fontSize =
                        15.sp,

                    fontWeight =

                        if (
                            atual
                        ) {

                            FontWeight.Bold

                        } else {

                            FontWeight.Normal
                        }
                )


                if (
                    atual
                ) {

                    Text(

                        text =

                            if (
                                tocando
                            ) {

                                "Reproduzindo agora"

                            } else {

                                "Pausado"
                            },

                        color =
                            Color(0xFFD32F2F),

                        fontSize =
                            11.sp,

                        modifier =
                            Modifier.padding(
                                top = 2.dp
                            )
                    )
                }
            }


            if (
                atual
            ) {

                Icon(

                    imageVector =

                        if (
                            tocando
                        ) {

                            Icons.Default.MusicNote

                        } else {

                            Icons.Default.PlayArrow
                        },

                    contentDescription =
                        null,

                    tint =
                        Color(0xFFD32F2F),

                    modifier =
                        Modifier.size(
                            24.dp
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