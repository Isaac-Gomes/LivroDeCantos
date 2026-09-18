package com.example.livrodecantos.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.livrodecantos.model.Canto
import com.example.livrodecantos.model.Ficha
import com.example.livrodecantos.ui.viewmodel.AudioViewModel
import com.example.livrodecantos.ui.viewmodel.CantoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.TextButton
import java.io.File


// ====================================================
// TELA DA FICHA
// ====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaFicha(

    canto: Canto,

    cantoViewModel: CantoViewModel,

    audioViewModel: AudioViewModel,

    onVoltar: () -> Unit

) {

    // ------------------------------------------------
    // FICHAS DO CANTO
    // ------------------------------------------------

    val fluxoFichas =
        remember(canto.id) {

            cantoViewModel
                .fichasDoCanto(
                    canto.id
                )
        }


    val fichas by
    fluxoFichas.collectAsState(
        initial = emptyList()
    )
    val cantosAtualizados by
    cantoViewModel.cantos
        .collectAsState()


    val cantoAtual =

        cantosAtualizados
            .firstOrNull {

                it.id == canto.id
            }
            ?: canto

    // ------------------------------------------------
    // ZOOM
    // ------------------------------------------------

    var escala by
    remember(canto.id) {

        mutableFloatStateOf(
            1f
        )
    }


    val scrollHorizontal =
        rememberScrollState()


    val scrollVertical =
        rememberScrollState()


    // Quando voltar para 1x,
    // volta também para a posição horizontal inicial.

    LaunchedEffect(
        escala
    ) {

        if (
            escala <= 1.01f
        ) {

            scrollHorizontal
                .scrollTo(0)
        }
    }


    // ------------------------------------------------
    // TELA
    // ------------------------------------------------

    Scaffold(

        containerColor =
            Color(0xFF181818),


        // --------------------------------------------
        // TOPO
        // --------------------------------------------

        topBar = {

            TopAppBar(

                title = {

                    Text(

                        text =
                            canto.titulo,

                        color =
                            Color.White,

                        fontSize =
                            18.sp,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
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
        },


        // --------------------------------------------
        // PLAYER
        // --------------------------------------------

        bottomBar = {

            if (
                cantoAtual.possuiAudio
            ) {

                PlayerAudio(
                    canto = cantoAtual,
                    audioViewModel = audioViewModel
                )
            }
        }

    ) { innerPadding ->


        // --------------------------------------------
        // CARREGANDO
        // --------------------------------------------

        if (
            fichas.isEmpty()
        ) {

            Box(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    ),

                contentAlignment =
                    Alignment.Center

            ) {

                CircularProgressIndicator()
            }

        } else {


            // ========================================
            // FICHÁRIO COM ZOOM DIRETO
            // ========================================

            BoxWithConstraints(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )

            ) {

                val larguraNormal =
                    maxWidth


                // ------------------------------------
                // ÁREA QUE DETECTA A PINÇA
                // ------------------------------------

                Box(

                    modifier = Modifier
                        .fillMaxSize()

                        .pointerInput(
                            canto.id
                        ) {

                            awaitEachGesture {

                                var terminou =
                                    false


                                while (
                                    !terminou
                                ) {

                                    val evento =
                                        awaitPointerEvent(
                                            pass =
                                                PointerEventPass.Initial
                                        )


                                    val dedosAtivos =
                                        evento
                                            .changes
                                            .filter {
                                                it.pressed
                                            }


                                    // ----------------
                                    // SOMENTE 2 DEDOS
                                    // CONTROLAM O ZOOM
                                    // ----------------

                                    if (
                                        dedosAtivos.size >= 2
                                    ) {

                                        val fatorZoom =
                                            evento
                                                .calculateZoom()


                                        if (
                                            fatorZoom.isFinite() &&
                                            fatorZoom > 0f
                                        ) {

                                            escala =
                                                (
                                                        escala *
                                                                fatorZoom
                                                        )
                                                    .coerceIn(
                                                        1f,
                                                        5f
                                                    )
                                        }


                                        // Enquanto há dois dedos,
                                        // evitamos que o scroll
                                        // interprete a pinça.

                                        evento
                                            .changes
                                            .forEach { mudanca ->

                                                if (
                                                    mudanca.pressed
                                                ) {

                                                    mudanca.consume()
                                                }
                                            }
                                    }


                                    terminou =
                                        evento
                                            .changes
                                            .none {
                                                it.pressed
                                            }
                                }
                            }
                        }


                        // ----------------------------
                        // MOVIMENTO HORIZONTAL
                        // QUANDO ESTÁ AMPLIADO
                        // ----------------------------

                        .horizontalScroll(
                            scrollHorizontal
                        )

                ) {


                    // --------------------------------
                    // COLUNA CONTÍNUA
                    // --------------------------------

                    Column(

                        modifier = Modifier

                            // O zoom altera o tamanho
                            // REAL da ficha.
                            //
                            // Assim uma página não
                            // sobrepõe a outra.

                            .width(
                                larguraNormal *
                                        escala
                            )

                            .fillMaxHeight()

                            // Scroll vertical continua
                            // funcionando normalmente.

                            .verticalScroll(
                                scrollVertical
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                2.dp
                            )

                    ) {


                        // ----------------------------
                        // TODAS AS PÁGINAS
                        // UMA DEPOIS DA OUTRA
                        // ----------------------------

                        fichas.forEach { ficha ->

                            PaginaFicha(
                                ficha =
                                    ficha
                            )
                        }
                    }
                }
            }
        }
    }
}


// ====================================================
// UMA PÁGINA DA FICHA
// ====================================================

@Composable
private fun PaginaFicha(
    ficha: Ficha
) {

    val context =
        LocalContext.current


    val resultadoImagem by
    produceState<Result<ImageBitmap>?>(
        initialValue = null,
        key1 = ficha.arquivo
    ) {

        value =
            withContext(
                Dispatchers.IO
            ) {

                runCatching {

                    context
                        .assets
                        .open(
                            ficha.arquivo
                        )
                        .use { inputStream ->


                            val bitmap =

                                BitmapFactory
                                    .decodeStream(
                                        inputStream
                                    )

                                    ?: error(
                                        "Não foi possível abrir ${ficha.arquivo}"
                                    )


                            bitmap
                                .asImageBitmap()
                        }
                }
            }
    }


    when {


        // --------------------------------------------
        // CARREGANDO
        // --------------------------------------------

        resultadoImagem == null -> {

            Box(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        40.dp
                    ),

                contentAlignment =
                    Alignment.Center

            ) {

                CircularProgressIndicator()
            }
        }


        // --------------------------------------------
        // IMAGEM
        // --------------------------------------------

        resultadoImagem!!
            .isSuccess -> {


            val imagem =

                resultadoImagem!!
                    .getOrThrow()


            val proporcao =

                imagem.width
                    .toFloat() /
                        imagem.height
                            .toFloat()


            Image(

                bitmap =
                    imagem,

                contentDescription =
                    null,

                modifier = Modifier

                    .fillMaxWidth()

                    .aspectRatio(
                        proporcao
                    ),

                contentScale =
                    ContentScale.FillWidth
            )
        }


        // --------------------------------------------
        // ERRO
        // --------------------------------------------

        else -> {

            Text(

                text =
                    "Não foi possível carregar esta ficha.",

                color =
                    Color.White,

                modifier =
                    Modifier.padding(
                        24.dp
                    )
            )
        }
    }
}


// ====================================================
// PLAYER DE ÁUDIO
// ====================================================

@Composable
private fun PlayerAudio(

    canto: Canto,

    audioViewModel: AudioViewModel

) {

    val estado by
    audioViewModel
        .estado
        .collectAsState()
    val disponivelOffline =

        canto.audioLocalPath
            ?.let { caminho ->

                File(caminho)
                    .exists()
            }
            ?: false


    val baixandoEsteCanto =

        estado.cantoBaixandoId ==
                canto.id


    val esteCanto =
        estado.cantoId ==
                canto.id


    val posicao =

        if (esteCanto) {

            estado.posicao

        } else {

            0L
        }


    val duracao =

        if (esteCanto) {

            estado.duracao

        } else {

            0L
        }


    Column(

        modifier =
            Modifier.fillMaxWidth()

    ) {


        HorizontalDivider(

            color =
                Color(0xFF333333)
        )


        Column(

            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF181818)
                )
                .padding(
                    horizontal = 14.dp,
                    vertical = 8.dp
                )

        ) {


            // ----------------------------------------
            // TÍTULO + PLAY
            // ----------------------------------------

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically

            ) {


                Icon(

                    imageVector =
                        Icons.Default.MusicNote,

                    contentDescription =
                        null,

                    tint =
                        Color(0xFFBDBDBD)
                )


                Text(

                    text =
                        canto.titulo,

                    color =
                        Color.White,

                    fontSize =
                        14.sp,

                    fontWeight =
                        FontWeight.Medium,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis,

                    modifier = Modifier

                        .weight(1f)

                        .padding(
                            start = 8.dp
                        )
                )


                if (
                    esteCanto &&
                    estado.carregando
                ) {

                    CircularProgressIndicator(

                        modifier =
                            Modifier.size(
                                30.dp
                            ),

                        strokeWidth =
                            3.dp
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
                                    esteCanto &&
                                    estado.tocando
                                ) {

                                    Icons.Default.Pause

                                } else {

                                    Icons.Default.PlayArrow
                                },


                            contentDescription =

                                if (
                                    esteCanto &&
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
            }


            // ----------------------------------------
            // PROGRESSO
            // ----------------------------------------

            Slider(

                value =

                    if (
                        duracao > 0L
                    ) {

                        posicao
                            .toFloat()
                            .coerceIn(
                                0f,
                                duracao.toFloat()
                            )

                    } else {

                        0f
                    },


                onValueChange = { valor ->

                    if (
                        esteCanto &&
                        duracao > 0L
                    ) {

                        audioViewModel
                            .buscarPosicao(
                                valor.toLong()
                            )
                    }
                },


                valueRange =

                    0f..(
                            if (
                                duracao > 0L
                            ) {

                                duracao.toFloat()

                            } else {

                                1f
                            }
                            )
            )


            // ----------------------------------------
            // TEMPOS
            // ----------------------------------------

            Row(

                modifier =
                    Modifier.fillMaxWidth()

            ) {


                Text(

                    text =
                        formatarTempo(
                            posicao
                        ),

                    color =
                        Color(0xFFAAAAAA),

                    fontSize =
                        11.sp
                )


                Text(

                    text =
                        formatarTempo(
                            duracao
                        ),

                    color =
                        Color(0xFFAAAAAA),

                    fontSize =
                        11.sp,

                    modifier =
                        Modifier.weight(1f),

                    textAlign =
                        TextAlign.End
                )
            }
// ----------------------------------------
// DISPONIBILIDADE OFFLINE
// ----------------------------------------

            if (
                disponivelOffline
            ) {

                Row(

                    verticalAlignment =
                        Alignment.CenterVertically,

                    modifier =
                        Modifier.padding(
                            top = 4.dp
                        )

                ) {

                    Icon(

                        imageVector =
                            Icons.Default.CheckCircle,

                        contentDescription =
                            null,

                        tint =
                            Color(0xFF81C784),

                        modifier =
                            Modifier.size(
                                18.dp
                            )
                    )


                    Text(

                        text =
                            "Disponível offline",

                        color =
                            Color(0xFF81C784),

                        fontSize =
                            12.sp,

                        modifier =
                            Modifier.padding(
                                start = 6.dp
                            )
                    )
                }

            } else if (
                baixandoEsteCanto
            ) {

                Row(

                    verticalAlignment =
                        Alignment.CenterVertically,

                    modifier =
                        Modifier.padding(
                            top = 6.dp
                        )

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
                            "Baixando para uso offline...",

                        color =
                            Color(0xFFBDBDBD),

                        fontSize =
                            12.sp,

                        modifier =
                            Modifier.padding(
                                start = 8.dp
                            )
                    )
                }

            } else {

                TextButton(

                    onClick = {

                        audioViewModel
                            .baixarParaOffline(
                                canto
                            )
                    }

                ) {

                    Icon(

                        imageVector =
                            Icons.Default.Download,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(
                                18.dp
                            )
                    )


                    Text(

                        text =
                            "Baixar para ouvir offline",

                        modifier =
                            Modifier.padding(
                                start = 6.dp
                            )
                    )
                }
            }
            if (
                estado.erroDownload != null &&
                estado.cantoBaixandoId == null &&
                !disponivelOffline
            ) {

                Text(

                    text =
                        estado.erroDownload!!,

                    color =
                        Color(0xFFFF8A80),

                    fontSize =
                        12.sp,

                    modifier =
                        Modifier.padding(
                            top = 4.dp
                        )
                )
            }

            // ----------------------------------------
            // ERRO
            // ----------------------------------------

            if (
                esteCanto &&
                !estado.erro
                    .isNullOrBlank()
            ) {

                Text(

                    text =
                        estado.erro!!,

                    color =
                        Color(0xFFFF8A80),

                    fontSize =
                        12.sp,

                    modifier =
                        Modifier.padding(
                            top = 6.dp
                        )
                )
            }
        }
    }
}


// ====================================================
// FORMATAR TEMPO
// ====================================================

private fun formatarTempo(
    milissegundos: Long
): String {

    val totalSegundos =

        milissegundos
            .coerceAtLeast(
                0L
            ) /
                1000L


    val minutos =
        totalSegundos /
                60L


    val segundos =
        totalSegundos %
                60L


    return "%d:%02d".format(
        minutos,
        segundos
    )
}