package com.example.livrodecantos.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.livrodecantos.data.repository.CifraRepository
import com.example.livrodecantos.data.repository.AcordesFichaRepository
import com.example.livrodecantos.data.repository.PreferenciasCifraRepository
import com.example.livrodecantos.domain.cifra.CifraTranspositor
import com.example.livrodecantos.model.cifra.AcordeNaFicha
import com.example.livrodecantos.model.cifra.CifraCanto
import com.example.livrodecantos.model.cifra.PreferenciaAcidentes
import com.example.livrodecantos.model.cifra.ConfiguracaoCifra
import com.example.livrodecantos.model.cifra.DisponibilidadeTransposicaoNaFicha
import com.example.livrodecantos.model.cifra.MapeamentoAcordesFicha
import com.example.livrodecantos.data.repository.MonicaoRepository
import com.example.livrodecantos.model.Etapa
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CancellationException
import com.example.livrodecantos.model.Canto
import com.example.livrodecantos.model.Monicao
import com.example.livrodecantos.model.Ficha
import com.example.livrodecantos.ui.viewmodel.AudioViewModel
import com.example.livrodecantos.ui.viewmodel.CantoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.TextButton
import java.io.File
import kotlinx.coroutines.launch


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


    var mostrarFerramentas by rememberSaveable(canto.id) { mutableStateOf(false) }
    var mostrarMonicao by rememberSaveable(canto.id) { mutableStateOf(false) }

    var mostrarCifra by remember(canto.id) { mutableStateOf(false) }
    val contextCifra = LocalContext.current
    val cifraRepository = remember(contextCifra) { CifraRepository(contextCifra) }
    val acordesFichaRepository = remember(contextCifra) { AcordesFichaRepository(contextCifra) }
    val preferenciasCifraRepository = remember(contextCifra) {
        PreferenciasCifraRepository(contextCifra)
    }
    val configuracaoCifra by preferenciasCifraRepository.observarConfiguracao(canto.id)
        .collectAsState(initial = ConfiguracaoCifra())
    val escopoCifra = rememberCoroutineScope()
    val resultadoCifra by produceState<Result<CifraCanto?>?>(null, canto.id) {
        value = null
        value = try {
            Result.success(cifraRepository.buscarPorCanto(canto.id))
        } catch (erro: CancellationException) {
            throw erro
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }
    val cifraDisponivel = resultadoCifra?.getOrNull()?.takeIf { it.cantoId == canto.id }
    val resultadoAcordesFicha by produceState<Result<MapeamentoAcordesFicha?>?>(null, canto.id) {
        value = null
        value = try {
            Result.success(acordesFichaRepository.buscarMapeamentoPorCanto(canto.id))
        } catch (erro: CancellationException) {
            throw erro
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }
    val mapeamentoAcordes = resultadoAcordesFicha?.getOrNull()
    val transposicaoVisualDisponivel = DisponibilidadeTransposicaoNaFicha.estaPronta(
        cifra = cifraDisponivel,
        mapeamento = mapeamentoAcordes,
        paginasDaFicha = fichas.map { it.ordem }
    )
    val deslocamentoDaForma = CifraTranspositor.deslocamentoDaForma(
        configuracaoCifra.semitons,
        configuracaoCifra.capotraste
    )
    val tomAtual = cifraDisponivel?.let {
        CifraTranspositor.tomAtual(
            it.tomOriginal,
            configuracaoCifra.semitons,
            it.preferenciaAcidentes
        )
    }
    val formasNaFicha = cifraDisponivel?.let {
        CifraTranspositor.transporParaForma(
            it.tomOriginal,
            configuracaoCifra.semitons,
            configuracaoCifra.capotraste,
            it.preferenciaAcidentes
        )
    }

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
        // FERRAMENTAS
        // --------------------------------------------

        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarFerramentas = true },
                containerColor = Color(0xFF333333),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Ferramentas do canto"
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
                    )
                    // Reserva espaço para o botão sem cobrir a partitura.
                    .padding(bottom = 80.dp),

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
                    // Reserva espaço para o botão sem cobrir a partitura.
                    .padding(bottom = 80.dp)

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
                                    ficha,
                                deslocamentoDaForma = if (transposicaoVisualDisponivel) {
                                    deslocamentoDaForma
                                } else {
                                    0
                                },
                                preferenciaAcidentes = cifraDisponivel?.preferenciaAcidentes
                                    ?: PreferenciaAcidentes.BEMOIS,
                                acordes = mapeamentoAcordes?.acordesDaPagina(ficha.ordem).orEmpty()
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarFerramentas) {
        ModalBottomSheet(
            onDismissRequest = { mostrarFerramentas = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF181818),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Ferramentas do canto",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Text(
                    text = "Áudio",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(16.dp)
                )
                if (cantoAtual.possuiAudio) {
                    PlayerAudio(
                        canto = cantoAtual,
                        audioViewModel = audioViewModel
                    )
                } else {
                    Text(
                        text = "Áudio indisponível para este canto",
                        color = Color(0xFFBDBDBD),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    TextButton(onClick = {}, enabled = false) {
                        Text("Baixar áudio offline", color = Color(0xFFBDBDBD))
                    }
                }
                HorizontalDivider(color = Color(0xFF333333))
                if (cantoAtual.etapa != Etapa.LITURGICOS) {
                    TextButton(
                        onClick = {
                            mostrarFerramentas = false
                            mostrarMonicao = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Monição",
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        )
                    }
                }
                if (transposicaoVisualDisponivel) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Tom e capotraste", fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
                        Text("Tom original: ${cifraDisponivel!!.tomOriginal}", fontSize = 14.sp)
                        Text(
                            "Tom que soa: $tomAtual • " +
                                if (configuracaoCifra.capotraste == 0) "Sem capotraste"
                                else "Capo ${configuracaoCifra.capotraste}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("Transposição", modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    escopoCifra.launch {
                                        preferenciasCifraRepository.ajustarSemitons(canto.id, -1)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Descer tom da ficha")
                            }
                            Text(
                                formatarDeslocamentoFicha(configuracaoCifra.semitons),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(
                                onClick = {
                                    escopoCifra.launch {
                                        preferenciasCifraRepository.ajustarSemitons(canto.id, 1)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Subir tom da ficha")
                            }
                        }
                        Text("Capotraste", modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                enabled = configuracaoCifra.capotraste > 0,
                                onClick = {
                                    escopoCifra.launch {
                                        preferenciasCifraRepository.ajustarCapotraste(canto.id, -1)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Diminuir capotraste")
                            }
                            Text("Casa ${configuracaoCifra.capotraste}", fontSize = 20.sp)
                            IconButton(
                                enabled = configuracaoCifra.capotraste < 11,
                                onClick = {
                                    escopoCifra.launch {
                                        preferenciasCifraRepository.ajustarCapotraste(canto.id, 1)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Aumentar capotraste")
                            }
                        }
                        Text("Formas na ficha: $formasNaFicha", fontSize = 16.sp)
                        TextButton(
                            enabled = configuracaoCifra.capotraste != 0,
                            onClick = {
                                escopoCifra.launch {
                                    preferenciasCifraRepository.salvarCapotraste(canto.id, 0)
                                }
                            }
                        ) { Text("Sem capotraste") }
                        TextButton(
                            enabled = configuracaoCifra != ConfiguracaoCifra(),
                            onClick = {
                                escopoCifra.launch {
                                    preferenciasCifraRepository.restaurarConfiguracao(canto.id)
                                }
                            }
                        ) { Text("Restaurar ficha original") }
                        TextButton(onClick = {
                            mostrarFerramentas = false
                            mostrarCifra = true
                        }) {
                            Text("Ver cifra estruturada")
                        }
                    }
                } else {
                    TextButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when {
                                resultadoCifra == null || resultadoAcordesFicha == null ->
                                    "Tom e transposição — Carregando..."
                                resultadoCifra?.isFailure == true || resultadoAcordesFicha?.isFailure == true ->
                                    "Tom e transposição — Indisponível"
                                else -> "Tom e transposição — Indisponível"
                            },
                            color = Color(0xFFBDBDBD),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        )
                    }
                }

            }
        }
    }

    if (mostrarCifra && cifraDisponivel != null && transposicaoVisualDisponivel) {
        TelaCifra(
            titulo = canto.titulo,
            cifra = cifraDisponivel,
            onFechar = { mostrarCifra = false },
            preferenciasCifraRepository = preferenciasCifraRepository
        )
    }


    if (mostrarMonicao && cantoAtual.etapa != Etapa.LITURGICOS) {
        VisualizacaoMonicao(
            canto = cantoAtual,
            onFechar = { mostrarMonicao = false }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisualizacaoMonicao(canto: Canto, onFechar: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { MonicaoRepository(context) }
    val resultado by produceState<Result<Monicao?>?>(
        initialValue = null,
        key1 = canto.id,
        key2 = canto.etapa
    ) {
        value = try {
            Result.success(repository.buscarPorCanto(canto))
        } catch (erro: CancellationException) {
            throw erro
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }

    Dialog(
        onDismissRequest = onFechar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color(0xFF181818),
            contentColor = Color.White,
            topBar = {
                TopAppBar(
                    title = { Text("MONIÇÃO") },
                    navigationIcon = {
                        IconButton(onClick = onFechar) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar à ficha"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF121212),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(canto.titulo, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                val carregado = resultado
                when {
                    carregado == null -> CircularProgressIndicator()
                    carregado.isFailure -> Text("Não foi possível carregar a monição")
                    else -> {
                        val monicao = carregado.getOrNull()
                        if (monicao == null) {
                            Text("Monição ainda não disponível", fontSize = 18.sp)
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF282828))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Resumo", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = monicao.resumoTelegrafico,
                                    fontSize = 18.sp,
                                    lineHeight = 28.sp
                                )
                            }
                            if (monicao.referencias.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "Referências bíblicas",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = monicao.referencias.joinToString(" • "),
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp
                                    )
                                }
                            }
                            HorizontalDivider(color = Color(0xFF333333))
                            Text(
                                "Monição catequética",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = monicao.monicao,
                                fontSize = 18.sp,
                                lineHeight = 28.sp
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
    ficha: Ficha,
    deslocamentoDaForma: Int,
    preferenciaAcidentes: PreferenciaAcidentes,
    acordes: List<AcordeNaFicha>
) {

    val context =
        LocalContext.current


    val resultadoImagem by
    produceState<Result<Bitmap>?>(
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


            val imagemOriginal =

                resultadoImagem!!
                    .getOrThrow()

            val imagem = remember(imagemOriginal, deslocamentoDaForma, preferenciaAcidentes, acordes) {
                criarFichaComAcordesTranspostos(
                    imagemOriginal,
                    acordes,
                    deslocamentoDaForma,
                    preferenciaAcidentes
                ).asImageBitmap()
            }


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

private fun formatarDeslocamentoFicha(semitons: Int): String = when {
    semitons > 0 -> "+$semitons"
    else -> semitons.toString()
}


/**
 * Compõe uma cópia descartável do bitmap. O asset continua intacto e, em 0,
 * a própria instância original segue para a Image sem qualquer processamento.
 */
private fun criarFichaComAcordesTranspostos(
    original: Bitmap,
    acordes: List<AcordeNaFicha>,
    deslocamentoDaForma: Int,
    preferenciaAcidentes: PreferenciaAcidentes
): Bitmap {
    if (deslocamentoDaForma == 0 || acordes.isEmpty()) return original

    val ficha = original.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(ficha)
    val pincel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(211, 47, 47)
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }

    acordes.forEach { posicao ->
        val esquerda = (posicao.x * ficha.width).toInt().coerceIn(0, ficha.width - 1)
        val topo = (posicao.y * ficha.height).toInt().coerceIn(0, ficha.height - 1)
        val direita = ((posicao.x + posicao.largura) * ficha.width).toInt()
            .coerceIn(esquerda + 1, ficha.width)
        val base = ((posicao.y + posicao.altura) * ficha.height).toInt()
            .coerceIn(topo + 1, ficha.height)
        val corDeFundo = corClaraDaRegiao(ficha, esquerda, topo, direita, base)

        for (y in topo until base) {
            for (x in esquerda until direita) {
                if (ehVermelhoDoAcorde(ficha.getPixel(x, y))) {
                    ficha.setPixel(x, y, corDeFundo)
                }
            }
        }

        pincel.textSize = (base - topo) * 0.95f
        val acordeTransposto = CifraTranspositor.transpor(
            posicao.acorde,
            deslocamentoDaForma,
            preferenciaAcidentes
        )
        canvas.drawText(acordeTransposto, esquerda.toFloat(), base - 2f, pincel)
    }
    return ficha
}

private fun ehVermelhoDoAcorde(cor: Int): Boolean =
    AndroidColor.red(cor) > AndroidColor.green(cor) + 30 &&
        AndroidColor.red(cor) > AndroidColor.blue(cor) + 20 &&
        AndroidColor.red(cor) > 120

/** Usa o próprio fundo claro da região, sem cobrir letra ou outros elementos. */
private fun corClaraDaRegiao(
    bitmap: Bitmap,
    esquerda: Int,
    topo: Int,
    direita: Int,
    base: Int
): Int {
    var vermelho = 0L
    var verde = 0L
    var azul = 0L
    var total = 0
    for (y in topo until base) {
        for (x in esquerda until direita) {
            val cor = bitmap.getPixel(x, y)
            val r = AndroidColor.red(cor)
            val g = AndroidColor.green(cor)
            val b = AndroidColor.blue(cor)
            if (r > 225 && g > 225 && b > 225 &&
                kotlin.math.abs(r - g) < 10 && kotlin.math.abs(r - b) < 10
            ) {
                vermelho += r
                verde += g
                azul += b
                total++
            }
        }
    }
    return if (total == 0) AndroidColor.WHITE else AndroidColor.rgb(
        (vermelho / total).toInt(),
        (verde / total).toInt(),
        (azul / total).toInt()
    )
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
                            "Baixar áudio offline",

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
