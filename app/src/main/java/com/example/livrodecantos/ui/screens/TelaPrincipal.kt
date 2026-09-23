package com.example.livrodecantos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.livrodecantos.data.entity.ListaEntity
import com.example.livrodecantos.model.Canto
import com.example.livrodecantos.model.Etapa
import com.example.livrodecantos.ui.components.MenuLateral
import com.example.livrodecantos.ui.components.MiniPlayerAudio
import com.example.livrodecantos.ui.components.NumeracaoCanto
import com.example.livrodecantos.ui.viewmodel.AudioViewModel
import com.example.livrodecantos.ui.viewmodel.CantoViewModel
import com.example.livrodecantos.ui.viewmodel.DownloadTodosViewModel
import com.example.livrodecantos.ui.viewmodel.ListaViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.Collator
import java.text.Normalizer
import java.util.Locale


// ====================================================
// TELAS DO APLICATIVO
// ====================================================

private enum class TelaApp {

    PRINCIPAL,

    INDICE_LITURGICO,

    INDICE_BIBLICO,

    MINHAS_LISTAS,

    DETALHE_LISTA,

    EDITAR_LISTA,

    FILA_REPRODUCAO,

    AUDIOS_OFFLINE,

    FICHA
}


// ====================================================
// TELA PRINCIPAL
// ====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPrincipal(

    cantoViewModel: CantoViewModel = viewModel()

) {

    // ====================================================
    // VIEWMODELS
    // ====================================================

    val audioViewModel: AudioViewModel =
        viewModel()


    val listaViewModel: ListaViewModel =
        viewModel()


    val downloadTodosViewModel: DownloadTodosViewModel =
        viewModel()


    // ====================================================
    // CANTOS DO ROOM
    // ====================================================

    val cantosBanco by
    cantoViewModel
        .cantos
        .collectAsState()


    // ====================================================
    // NAVEGAÇÃO
    // ====================================================

    // Mantém a rolagem mesmo quando a lista sai da composição ao abrir uma ficha.
    val estadoListaPrincipal = rememberLazyListState()
    val estadoListaNumerica = rememberLazyListState()

    var ordenarPorNumero by remember { mutableStateOf(false) }

    var telaAtual by remember {

        mutableStateOf(
            TelaApp.PRINCIPAL
        )
    }


    var telaAnteriorFicha by remember {

        mutableStateOf(
            TelaApp.PRINCIPAL
        )
    }


    var cantoSelecionado by remember {

        mutableStateOf<Canto?>(
            null
        )
    }


    var listaSelecionada by remember {

        mutableStateOf<ListaEntity?>(
            null
        )
    }


    // ====================================================
    // ÍNDICES
    // ====================================================

    var subcategoriaLiturgicaSelecionada by remember {

        mutableStateOf<String?>(
            null
        )
    }


    var testamentoBiblicoSelecionado by remember {

        mutableStateOf<String?>(
            null
        )
    }


    // ====================================================
    // FILTRO DE ETAPA
    // ====================================================

    var etapaSelecionada by remember {

        mutableStateOf<Etapa?>(
            null
        )
    }


    // ====================================================
    // BUSCA
    // ====================================================

    var buscando by remember {

        mutableStateOf(
            false
        )
    }


    var textoBusca by remember {

        mutableStateOf(
            ""
        )
    }


    // ====================================================
    // DRAWER
    // ====================================================

    val drawerState =
        rememberDrawerState(

            initialValue =
                DrawerValue.Closed
        )


    val scope =
        rememberCoroutineScope()


    // ====================================================
    // ABRIR FICHA
    // ====================================================

    fun abrirFicha(
        canto: Canto
    ) {

        telaAnteriorFicha =
            telaAtual


        cantoSelecionado =
            canto


        telaAtual =
            TelaApp.FICHA
    }


    // ====================================================
    // VOLTAR DA FICHA
    // ====================================================

    fun voltarDaFicha() {

        cantoSelecionado =
            null


        telaAtual =
            telaAnteriorFicha
    }


    // ====================================================
    // BOTÃO / GESTO VOLTAR DO ANDROID
    // ====================================================

    BackHandler(

        enabled =

            drawerState.isOpen ||

                    buscando ||

                    telaAtual !=
                    TelaApp.PRINCIPAL

    ) {

        when {


            // ============================================
            // DRAWER
            // ============================================

            drawerState.isOpen -> {

                scope.launch {

                    drawerState.close()
                }
            }


            // ============================================
            // FICHA
            // ============================================

            telaAtual ==
                    TelaApp.FICHA -> {

                voltarDaFicha()
            }


            // ============================================
            // FILA DE REPRODUÇÃO
            // ============================================

            telaAtual ==
                    TelaApp.FILA_REPRODUCAO -> {

                if (
                    listaSelecionada != null
                ) {

                    telaAtual =
                        TelaApp.DETALHE_LISTA

                } else {

                    telaAtual =
                        TelaApp.PRINCIPAL
                }
            }


            // ============================================
            // EDITAR LISTA
            // ============================================

            telaAtual ==
                    TelaApp.EDITAR_LISTA -> {

                telaAtual =
                    TelaApp.DETALHE_LISTA
            }


            // ============================================
            // DETALHE DA LISTA
            // ============================================

            telaAtual ==
                    TelaApp.DETALHE_LISTA -> {

                listaSelecionada =
                    null


                telaAtual =
                    TelaApp.MINHAS_LISTAS
            }


            // ============================================
            // MINHAS LISTAS
            // ============================================

            telaAtual ==
                    TelaApp.MINHAS_LISTAS -> {

                telaAtual =
                    TelaApp.PRINCIPAL
            }


            // ============================================
            // SUBCATEGORIA LITÚRGICA
            // ============================================

            telaAtual ==
                    TelaApp.INDICE_LITURGICO &&

                    subcategoriaLiturgicaSelecionada !=
                    null -> {

                subcategoriaLiturgicaSelecionada =
                    null
            }


            // ============================================
            // ÍNDICE LITÚRGICO
            // ============================================

            telaAtual ==
                    TelaApp.INDICE_LITURGICO -> {

                telaAtual =
                    TelaApp.PRINCIPAL
            }


            // ============================================
            // TESTAMENTO
            // ============================================

            telaAtual ==
                    TelaApp.INDICE_BIBLICO &&

                    testamentoBiblicoSelecionado !=
                    null -> {

                testamentoBiblicoSelecionado =
                    null
            }


            // ============================================
            // ÍNDICE BÍBLICO
            // ============================================

            telaAtual ==
                    TelaApp.INDICE_BIBLICO -> {

                telaAtual =
                    TelaApp.PRINCIPAL
            }


            // ============================================
            // ÁUDIOS OFFLINE
            // ============================================

            telaAtual ==
                    TelaApp.AUDIOS_OFFLINE -> {

                telaAtual =
                    TelaApp.PRINCIPAL
            }


            // ============================================
            // BUSCA
            // ============================================

            buscando -> {

                textoBusca =
                    ""


                buscando =
                    false
            }
        }
    }


    // ====================================================
    // FICHA
    // ====================================================

    if (
        telaAtual ==
        TelaApp.FICHA &&

        cantoSelecionado !=
        null
    ) {

        TelaFicha(

            canto =
                cantoSelecionado!!,

            cantoViewModel =
                cantoViewModel,

            audioViewModel =
                audioViewModel,

            onVoltar = {

                voltarDaFicha()
            }
        )


        return
    }


    // ====================================================
    // FILA DE REPRODUÇÃO
    // ====================================================

    if (
        telaAtual ==
        TelaApp.FILA_REPRODUCAO
    ) {

        TelaFilaReproducao(

            audioViewModel =
                audioViewModel,

            onVoltar = {

                if (
                    listaSelecionada != null
                ) {

                    telaAtual =
                        TelaApp.DETALHE_LISTA

                } else {

                    telaAtual =
                        TelaApp.PRINCIPAL
                }
            },

            onFilaLimpa = {

                if (
                    listaSelecionada != null
                ) {

                    telaAtual =
                        TelaApp.DETALHE_LISTA

                } else {

                    telaAtual =
                        TelaApp.PRINCIPAL
                }
            }
        )


        return
    }


    // ====================================================
    // DETALHE DA LISTA
    // ====================================================

    if (
        telaAtual ==
        TelaApp.DETALHE_LISTA &&

        listaSelecionada !=
        null
    ) {

        TelaDetalheLista(

            lista =
                listaSelecionada!!,

            todosCantos =
                cantosBanco,

            listaViewModel =
                listaViewModel,

            audioViewModel =
                audioViewModel,

            onVoltar = {

                listaSelecionada =
                    null


                telaAtual =
                    TelaApp.MINHAS_LISTAS
            },

            onEditar = {

                telaAtual =
                    TelaApp.EDITAR_LISTA
            },

            onCantoClick = { canto ->

                abrirFicha(
                    canto
                )
            },

            onReproduzir = { cantos ->

                audioViewModel
                    .reproduzirListaEmOrdem(
                        cantos
                    )


                telaAtual =
                    TelaApp.FILA_REPRODUCAO
            },

            onAleatorio = { cantos ->

                audioViewModel
                    .reproduzirListaAleatoria(
                        cantos
                    )


                telaAtual =
                    TelaApp.FILA_REPRODUCAO
            }
        )


        return
    }


    // ====================================================
    // EDITAR LISTA
    // ====================================================

    if (
        telaAtual ==
        TelaApp.EDITAR_LISTA &&

        listaSelecionada !=
        null
    ) {

        TelaEditarLista(

            lista =
                listaSelecionada!!,

            cantos =
                cantosBanco,

            listaViewModel =
                listaViewModel,

            onVoltar = {

                telaAtual =
                    TelaApp.DETALHE_LISTA
            },

            onSalvo = {

                telaAtual =
                    TelaApp.DETALHE_LISTA
            }
        )


        return
    }


    // ====================================================
    // MINHAS LISTAS
    // ====================================================

    if (
        telaAtual ==
        TelaApp.MINHAS_LISTAS
    ) {

        TelaMinhasListas(

            listaViewModel =
                listaViewModel,

            onVoltar = {

                telaAtual =
                    TelaApp.PRINCIPAL
            },

            onListaClick = { lista ->

                listaSelecionada =
                    lista


                telaAtual =
                    TelaApp.DETALHE_LISTA
            }
        )


        return
    }


    // ====================================================
    // ÍNDICE LITÚRGICO
    // ====================================================

    if (
        telaAtual ==
        TelaApp.INDICE_LITURGICO
    ) {

        TelaIndiceLiturgico(

            cantoViewModel =
                cantoViewModel,

            subcategoriaSelecionada =
                subcategoriaLiturgicaSelecionada,

            onSubcategoriaSelecionada = {

                subcategoriaLiturgicaSelecionada =
                    it
            },

            onVoltar = {

                subcategoriaLiturgicaSelecionada =
                    null


                telaAtual =
                    TelaApp.PRINCIPAL
            },

            onCantoClick = { canto ->

                abrirFicha(
                    canto
                )
            }
        )


        return
    }


    // ====================================================
    // ÍNDICE BÍBLICO
    // ====================================================

    if (
        telaAtual ==
        TelaApp.INDICE_BIBLICO
    ) {

        TelaIndiceBiblico(

            cantoViewModel =
                cantoViewModel,

            testamentoSelecionado =
                testamentoBiblicoSelecionado,

            onTestamentoSelecionado = {

                testamentoBiblicoSelecionado =
                    it
            },

            onVoltar = {

                testamentoBiblicoSelecionado =
                    null


                telaAtual =
                    TelaApp.PRINCIPAL
            },

            onCantoClick = { canto ->

                abrirFicha(
                    canto
                )
            }
        )


        return
    }


    // ====================================================
    // ÁUDIOS OFFLINE
    // ====================================================

    if (
        telaAtual ==
        TelaApp.AUDIOS_OFFLINE
    ) {

        TelaAudiosOffline(

            cantos =
                cantosBanco,

            downloadTodosViewModel =
                downloadTodosViewModel,

            onVoltar = {

                telaAtual =
                    TelaApp.PRINCIPAL
            },

            onCantoClick = { canto ->

                abrirFicha(
                    canto
                )
            },

            onExcluirAudio = { canto ->

                audioViewModel
                    .excluirAudioOffline(
                        canto
                    )
            },

            onExcluirTodosAudios = { cantos ->

                audioViewModel
                    .excluirTodosAudiosOffline(
                        cantos
                    )
            }
        )


        return
    }


    // ====================================================
    // COMPARADOR ALFABÉTICO PT-BR
    // ====================================================

    val comparadorAlfabetico = remember {

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
    // ITENS ESPECIAIS QUE DEVEM FICAR NO FINAL
    // ====================================================

    val ordemAnexos = remember {

        mapOf(

            "Prontuário De Acordes" to 0,

            "Arpejos" to 1,

            "Tabela Para Transportar" to 2
        )
    }


    // ====================================================
    // COMPARADOR DOS CANTOS
    // ====================================================

    val usarOrdemNumerica = ordenarPorNumero && etapaSelecionada == null

    val comparadorCantos =
        remember(
            comparadorAlfabetico,
            ordemAnexos,
            usarOrdemNumerica
        ) {

            Comparator<Canto> {

                    canto1,
                    canto2 ->


                val anexo1 =
                    ordemAnexos[
                        canto1.titulo
                    ]


                val anexo2 =
                    ordemAnexos[
                        canto2.titulo
                    ]


                when {


                    // ------------------------------------
                    // OS DOIS SÃO ANEXOS
                    // ------------------------------------

                    anexo1 != null &&
                            anexo2 != null -> {

                        anexo1.compareTo(
                            anexo2
                        )
                    }


                    // ------------------------------------
                    // PRIMEIRO É ANEXO
                    // ------------------------------------

                    anexo1 != null -> {

                        1
                    }


                    // ------------------------------------
                    // SEGUNDO É ANEXO
                    // ------------------------------------

                    anexo2 != null -> {

                        -1
                    }


                    usarOrdemNumerica -> {
                        val numero1 = canto1.numero.substringBefore(',').trim().toIntOrNull() ?: Int.MAX_VALUE
                        val numero2 = canto2.numero.substringBefore(',').trim().toIntOrNull() ?: Int.MAX_VALUE

                        numero1.compareTo(numero2)
                    }

                    // ------------------------------------
                    // ORDEM ALFABÉTICA NORMAL
                    // ------------------------------------

                    else -> {

                        comparadorAlfabetico
                            .compare(

                                canto1.titulo,

                                canto2.titulo
                            )
                    }
                }
            }
        }


    // ====================================================
    // NORMALIZAR TEXTO DA BUSCA
    // ====================================================

    fun normalizarTexto(
        texto: String
    ): String {

        return Normalizer

            .normalize(
                texto,
                Normalizer.Form.NFD
            )

            .replace(
                "\\p{Mn}+".toRegex(),
                ""
            )

            .lowercase(
                Locale.forLanguageTag(
                    "pt-BR"
                )
            )
    }


    // ====================================================
    // CANTOS EXIBIDOS
    // ====================================================

    val cantosExibidos =

        cantosBanco


            // ----------------------------------------
            // FILTRO DE ETAPA
            // ----------------------------------------

            .filter { canto ->

                etapaSelecionada ==
                        null ||

                        canto.etapa ==
                        etapaSelecionada
            }


            // ----------------------------------------
            // BUSCA
            // ----------------------------------------

            .filter { canto ->

                if (
                    textoBusca.isBlank()
                ) {

                    true

                } else {

                    normalizarTexto(
                        canto.titulo
                    )
                        .contains(

                            normalizarTexto(
                                textoBusca
                            )
                        )
                }
            }


            // ----------------------------------------
            // ORDENAÇÃO
            // ----------------------------------------

            .sortedWith(
                comparadorCantos
            )


    // ====================================================
    // ESTADO DO PLAYER
    // ====================================================

    val estadoAudio by

    audioViewModel
        .estado
        .collectAsState()


    // ====================================================
    // CANTO EM REPRODUÇÃO
    // ====================================================

    val cantoEmReproducao =

        cantosBanco
            .firstOrNull { canto ->

                canto.id ==
                        estadoAudio.cantoId
            }


    // ====================================================
    // DRAWER
    // ====================================================

    ModalNavigationDrawer(

        drawerState =
            drawerState,

        drawerContent = {

            ModalDrawerSheet(

                drawerContainerColor =
                    Color.Transparent

            ) {

                MenuLateral(


                    // ====================================
                    // TODOS OS CANTOS
                    // ====================================

                    onTodosCantos = {

                        ordenarPorNumero = true

                        etapaSelecionada =
                            null


                        textoBusca =
                            ""


                        buscando =
                            false


                        telaAtual =
                            TelaApp.PRINCIPAL


                        scope.launch {

                            drawerState.close()
                        }
                    },


                    // ====================================
                    // ORDEM ALFABÉTICA
                    // ====================================

                    onOrdemAlfabetica = {

                        ordenarPorNumero = false

                        etapaSelecionada =
                            null


                        textoBusca =
                            ""


                        buscando =
                            false


                        telaAtual =
                            TelaApp.PRINCIPAL


                        scope.launch {

                            drawerState.close()
                        }
                    },


                    // ====================================
                    // ÍNDICE LITÚRGICO
                    // ====================================

                    onIndiceLiturgico = {

                        subcategoriaLiturgicaSelecionada =
                            null


                        telaAtual =
                            TelaApp.INDICE_LITURGICO


                        scope.launch {

                            drawerState.close()
                        }
                    },


                    // ====================================
                    // ÍNDICE BÍBLICO
                    // ====================================

                    onIndiceBiblico = {

                        testamentoBiblicoSelecionado =
                            null


                        telaAtual =
                            TelaApp.INDICE_BIBLICO


                        scope.launch {

                            drawerState.close()
                        }
                    },


                    // ====================================
                    // MINHAS LISTAS
                    // ====================================

                    onMinhasListas = {

                        listaSelecionada =
                            null


                        telaAtual =
                            TelaApp.MINHAS_LISTAS


                        scope.launch {

                            drawerState.close()
                        }
                    },


                    // ====================================
                    // ÁUDIOS OFFLINE
                    // ====================================

                    onAudiosOffline = {

                        telaAtual =
                            TelaApp.AUDIOS_OFFLINE


                        scope.launch {

                            drawerState.close()
                        }
                    },


                    // ====================================
                    // PRÉ-CATECUMENATO
                    // ====================================

                    onPreCatecumenato = {

                        etapaSelecionada =
                            Etapa.PRE_CATECUMENATO


                        telaAtual =
                            TelaApp.PRINCIPAL


                        scope.launch {

                            drawerState.close()
                        }
                    },


                    // ====================================
                    // LITÚRGICOS
                    // ====================================

                    onLiturgicos = {

                        etapaSelecionada =
                            Etapa.LITURGICOS


                        telaAtual =
                            TelaApp.PRINCIPAL


                        scope.launch {

                            drawerState.close()
                        }
                    },


                    // ====================================
                    // CATECUMENATO
                    // ====================================

                    onCatecumenato = {

                        etapaSelecionada =
                            Etapa.CATECUMENATO


                        telaAtual =
                            TelaApp.PRINCIPAL


                        scope.launch {

                            drawerState.close()
                        }
                    },


                    // ====================================
                    // ELEIÇÃO
                    // ====================================

                    onEleicao = {

                        etapaSelecionada =
                            Etapa.ELEICAO


                        telaAtual =
                            TelaApp.PRINCIPAL


                        scope.launch {

                            drawerState.close()
                        }
                    }
                )
            }
        }

    ) {


        // ====================================================
        // TELA PRINCIPAL
        // ====================================================

        Scaffold(

            modifier =
                Modifier.fillMaxSize(),

            containerColor =
                Color(0xFF1E1E1E),


            // =================================================
            // TOPO
            // =================================================

            topBar = {

                TopAppBar(

                    title = {


                        // ------------------------------------
                        // BUSCA
                        // ------------------------------------

                        if (
                            buscando
                        ) {

                            TextField(

                                value =
                                    textoBusca,

                                onValueChange = {

                                    textoBusca =
                                        it
                                },

                                placeholder = {

                                    Text(
                                        text =
                                            "Buscar canto..."
                                    )
                                },

                                singleLine =
                                    true,

                                modifier =
                                    Modifier.fillMaxWidth(),

                                colors =
                                    TextFieldDefaults.colors(

                                        focusedContainerColor =
                                            Color.Transparent,

                                        unfocusedContainerColor =
                                            Color.Transparent,

                                        focusedTextColor =
                                            Color.White,

                                        unfocusedTextColor =
                                            Color.White,

                                        focusedPlaceholderColor =
                                            Color.Gray,

                                        unfocusedPlaceholderColor =
                                            Color.Gray,

                                        focusedIndicatorColor =
                                            Color.Transparent,

                                        unfocusedIndicatorColor =
                                            Color.Transparent
                                    )
                            )

                        } else {


                            // --------------------------------
                            // TÍTULO
                            // --------------------------------

                            Text(

                                text =
                                    "RESSUSCITOU",

                                color =
                                    Color(0xFFD32F2F),

                                fontWeight =
                                    FontWeight.Bold,

                                fontSize =
                                    20.sp
                            )
                        }
                    },


                    // =================================================
                    // MENU
                    // =================================================

                    navigationIcon = {

                        if (
                            !buscando
                        ) {

                            IconButton(

                                onClick = {

                                    scope.launch {

                                        if (
                                            drawerState.isClosed
                                        ) {

                                            drawerState.open()

                                        } else {

                                            drawerState.close()
                                        }
                                    }
                                }

                            ) {

                                Icon(

                                    imageVector =
                                        Icons.Default.Menu,

                                    contentDescription =
                                        "Abrir menu",

                                    tint =
                                        Color.White
                                )
                            }
                        }
                    },


                    // =================================================
                    // BUSCA / FECHAR
                    // =================================================

                    actions = {

                        if (
                            buscando
                        ) {

                            IconButton(

                                onClick = {

                                    textoBusca =
                                        ""


                                    buscando =
                                        false
                                }

                            ) {

                                Icon(

                                    imageVector =
                                        Icons.Default.Close,

                                    contentDescription =
                                        "Fechar busca",

                                    tint =
                                        Color.White
                                )
                            }

                        } else {

                            IconButton(

                                onClick = {

                                    buscando =
                                        true
                                }

                            ) {

                                Icon(

                                    imageVector =
                                        Icons.Default.Search,

                                    contentDescription =
                                        "Pesquisar",

                                    tint =
                                        Color.White
                                )
                            }
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


            // =================================================
            // MINI PLAYER
            // =================================================

            bottomBar = {

                cantoEmReproducao
                    ?.let { canto ->

                        MiniPlayerAudio(

                            canto =
                                canto,

                            audioViewModel =
                                audioViewModel,

                            onAbrirFicha = {

                                abrirFicha(
                                    canto
                                )
                            },

                            onAbrirFila = {

                                telaAtual =
                                    TelaApp.FILA_REPRODUCAO
                            }
                        )
                    }
            }

        ) { innerPadding ->


            Column(

                modifier = Modifier

                    .fillMaxSize()

                    .padding(
                        innerPadding
                    )

            ) {


                // =================================================
                // TÍTULO DA LISTA
                // =================================================

                Text(

                    text =

                        when {


                            textoBusca
                                .isNotBlank() -> {

                                "RESULTADOS DA BUSCA"
                            }


                            etapaSelecionada !=
                                    null -> {

                                etapaSelecionada!!
                                    .nomeExibicao
                                    .uppercase()
                            }


                            else -> {

                                if (usarOrdemNumerica) "TODOS OS CANTOS" else "ORDEM ALFABÉTICA"
                            }
                        },

                    color =
                        Color(0xFFBDBDBD),

                    fontSize =
                        13.sp,

                    fontWeight =
                        FontWeight.Bold,

                    modifier =
                        Modifier.padding(

                            start =
                                20.dp,

                            top =
                                20.dp,

                            bottom =
                                10.dp
                        )
                )


                HorizontalDivider(

                    color =
                        Color(0xFF333333)
                )


                // =================================================
                // LISTA
                // =================================================

                if (
                    cantosExibidos.isEmpty()
                ) {

                    Text(

                        text =
                            "Nenhum canto encontrado.",

                        color =
                            Color(0xFFBDBDBD),

                        modifier =
                            Modifier.padding(
                                24.dp
                            )
                    )

                } else {

                    ListaDeCantos(

                        estadoLista = if (usarOrdemNumerica) {
                            estadoListaNumerica
                        } else {
                            estadoListaPrincipal
                        },

                        cantos =
                            cantosExibidos,

                        onCantoClick = { canto ->

                            abrirFicha(
                                canto
                            )
                        }
                    )
                }
            }
        }
    }
}


// ====================================================
// LISTA DE CANTOS
// ====================================================

@Composable
private fun ListaDeCantos(

    estadoLista: LazyListState,

    cantos: List<Canto>,

    onCantoClick: (Canto) -> Unit

) {

    LazyColumn(

        state = estadoLista,

        modifier =
            Modifier.fillMaxSize()

    ) {

        items(

            items =
                cantos,

            key = { canto ->

                canto.id
            }

        ) { canto ->


            ItemCanto(

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


// ====================================================
// ITEM DO CANTO
// ====================================================

@Composable
private fun ItemCanto(

    canto: Canto,

    onClick: () -> Unit

) {

    val disponivelOffline =

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

                    horizontal =
                        16.dp,

                    vertical =
                        14.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically

        ) {


            // =================================================
            // NUMERAÇÃO COLORIDA
            // =================================================

            NumeracaoCanto(

                canto =
                    canto,

                modifier =
                    Modifier.padding(
                        end =
                            12.dp
                    )
            )


            // =================================================
            // TÍTULO
            // =================================================

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


            // =================================================
            // ÁUDIO
            // =================================================

            if (
                canto.possuiAudio
            ) {

                Row(

                    verticalAlignment =
                        Alignment.CenterVertically

                ) {


                    // ----------------------------------------
                    // DISPONÍVEL OFFLINE
                    // ----------------------------------------

                    if (
                        disponivelOffline
                    ) {

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
                    }


                    // ----------------------------------------
                    // NOTA MUSICAL
                    // ----------------------------------------

                    Icon(

                        imageVector =
                            Icons.Default.MusicNote,

                        contentDescription =

                            if (
                                disponivelOffline
                            ) {

                                "Áudio disponível offline"

                            } else {

                                "Possui áudio"
                            },

                        tint =

                            if (
                                disponivelOffline
                            ) {

                                Color(0xFF81C784)

                            } else {

                                Color(0xFFBDBDBD)
                            },

                        modifier = Modifier

                            .padding(

                                start =

                                    if (
                                        disponivelOffline
                                    ) {

                                        4.dp

                                    } else {

                                        0.dp
                                    }
                            )

                            .size(
                                22.dp
                            )
                    )
                }
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