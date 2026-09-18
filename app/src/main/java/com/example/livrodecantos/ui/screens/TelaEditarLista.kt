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
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.text.Collator
import java.text.Normalizer
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaEditarLista(

    lista: ListaEntity,

    cantos: List<Canto>,

    listaViewModel: ListaViewModel,

    onVoltar: () -> Unit,

    onSalvo: () -> Unit

) {

    // ====================================================
    // IDS SALVOS NO BANCO
    // ====================================================

    val idsBanco by
    listaViewModel
        .idsListaEdicao
        .collectAsState()


    // ====================================================
    // SELEÇÃO LOCAL
    // ====================================================

    val selecionados =
        remember(lista.id) {

            mutableStateListOf<Int>()
        }


    var selecaoCarregada by
    remember(lista.id) {

        mutableStateOf(false)
    }


    // ====================================================
    // CARREGAR LISTA
    // ====================================================

    LaunchedEffect(lista.id) {

        listaViewModel
            .carregarCantosDaLista(
                lista.id
            )
    }


    LaunchedEffect(idsBanco) {

        val ids =
            idsBanco


        if (
            ids != null &&
            !selecaoCarregada
        ) {

            selecionados.clear()

            selecionados.addAll(
                ids
            )

            selecaoCarregada =
                true
        }
    }


    // ====================================================
    // BUSCA
    // ====================================================

    var buscando by remember {

        mutableStateOf(false)
    }


    var textoBusca by remember {

        mutableStateOf("")
    }


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
    // ORDENAÇÃO
    // ====================================================

    val comparador =
        remember {

            Collator.getInstance(

                Locale.forLanguageTag(
                    "pt-BR"
                )

            ).apply {

                strength =
                    Collator.PRIMARY
            }
        }


    val cantosExibidos =

        cantos

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

            .sortedWith {

                    canto1,
                    canto2 ->

                comparador.compare(
                    canto1.titulo,
                    canto2.titulo
                )
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

                        Column {

                            Text(

                                text =
                                    "EDITAR LISTA",

                                color =
                                    Color.White,

                                fontSize =
                                    17.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )


                            Text(

                                text =
                                    lista.nome,

                                color =
                                    Color(0xFFAAAAAA),

                                fontSize =
                                    12.sp,

                                maxLines =
                                    1
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
                                    "Buscar",

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


        // ====================================================
        // RODAPÉ
        // ====================================================

        bottomBar = {

            if (
                selecaoCarregada
            ) {

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 10.dp
                        )

                ) {

                    Row(

                        modifier =
                            Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.CenterVertically

                    ) {

                        Text(

                            text =

                                if (
                                    selecionados.size == 1
                                ) {

                                    "1 canto selecionado"

                                } else {

                                    "${selecionados.size} cantos selecionados"
                                },

                            color =
                                Color(0xFFBDBDBD),

                            fontSize =
                                13.sp,

                            modifier =
                                Modifier.weight(1f)
                        )


                        if (
                            selecionados.isNotEmpty()
                        ) {

                            TextButton(

                                onClick = {

                                    selecionados.clear()
                                }

                            ) {

                                Text(
                                    text =
                                        "Limpar"
                                )
                            }
                        }
                    }


                    Button(

                        onClick = {

                            listaViewModel
                                .salvarCantosDaLista(

                                    listaId =
                                        lista.id,

                                    cantoIds =
                                        selecionados.toList(),

                                    onConcluido =
                                        onSalvo
                                )
                        },

                        modifier =
                            Modifier.fillMaxWidth()

                    ) {

                        Text(
                            text =
                                "Salvar lista"
                        )
                    }
                }
            }
        }

    ) { innerPadding ->


        // ====================================================
        // CARREGANDO
        // ====================================================

        if (
            !selecaoCarregada
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
                            top = 40.dp
                        )
                )
            }

        } else {


            // ====================================================
            // LISTA DE CANTOS
            // ====================================================

            Column(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )

            ) {

                Text(

                    text =
                        "TOQUE NOS CANTOS QUE DESEJA ADICIONAR",

                    color =
                        Color(0xFFAAAAAA),

                    fontSize =
                        12.sp,

                    fontWeight =
                        FontWeight.Bold,

                    modifier =
                        Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        )
                )


                HorizontalDivider(
                    color =
                        Color(0xFF333333)
                )


                LazyColumn(

                    modifier =
                        Modifier.fillMaxSize()

                ) {

                    items(

                        items =
                            cantosExibidos,

                        key = { canto ->

                            canto.id
                        }

                    ) { canto ->


                        ItemCantoSelecionavel(

                            canto =
                                canto,

                            selecionado =
                                selecionados
                                    .contains(
                                        canto.id
                                    ),

                            onAlterar = { marcado ->


                                if (
                                    marcado
                                ) {

                                    if (
                                        !selecionados
                                            .contains(
                                                canto.id
                                            )
                                    ) {

                                        selecionados.add(
                                            canto.id
                                        )
                                    }

                                } else {

                                    selecionados.remove(
                                        canto.id
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}


// ====================================================
// ITEM SELECIONÁVEL
// ====================================================

@Composable
private fun ItemCantoSelecionavel(

    canto: Canto,

    selecionado: Boolean,

    onAlterar: (Boolean) -> Unit

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

                onAlterar(
                    !selecionado
                )
            }

    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically

        ) {


            // ====================================================
            // CAIXA DE SELEÇÃO VISÍVEL
            // ====================================================

            Icon(

                imageVector =

                    if (
                        selecionado
                    ) {

                        Icons.Default.CheckBox

                    } else {

                        Icons.Default.CheckBoxOutlineBlank
                    },

                contentDescription =

                    if (
                        selecionado
                    ) {

                        "Selecionado"

                    } else {

                        "Não selecionado"
                    },

                tint =

                    if (
                        selecionado
                    ) {

                        Color(0xFFD32F2F)

                    } else {

                        Color(0xFFE0E0E0)
                    },

                modifier = Modifier
                    .size(
                        30.dp
                    )
                    .clickable {

                        onAlterar(
                            !selecionado
                        )
                    }
            )


            // ====================================================
            // CANTO
            // ====================================================

            Column(

                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = 14.dp
                    )

            ) {

                Text(

                    text =
                        canto.titulo,

                    color =
                        Color.White,

                    fontSize =
                        15.sp
                )


                Text(

                    text =
                        canto.etapa.nomeExibicao,

                    color =
                        Color(0xFF888888),

                    fontSize =
                        11.sp,

                    modifier =
                        Modifier.padding(
                            top = 2.dp
                        )
                )
            }


            // ====================================================
            // ÁUDIO
            // ====================================================

            if (
                canto.possuiAudio
            ) {

                Row(

                    verticalAlignment =
                        Alignment.CenterVertically

                ) {

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
                                    17.dp
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
                                disponivelOffline
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
                                21.dp
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