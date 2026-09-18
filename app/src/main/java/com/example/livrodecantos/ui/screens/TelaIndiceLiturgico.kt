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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.livrodecantos.model.Canto
import com.example.livrodecantos.ui.viewmodel.CantoViewModel
import java.io.File
import java.text.Collator
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaIndiceLiturgico(

    cantoViewModel: CantoViewModel,

    subcategoriaSelecionada: String?,

    onSubcategoriaSelecionada: (String?) -> Unit,

    onVoltar: () -> Unit,

    onCantoClick: (Canto) -> Unit

) {

    // ====================================================
    // TELA 1 — LISTA DE SUBCATEGORIAS
    // ====================================================

    if (
        subcategoriaSelecionada == null
    ) {

        val fluxoSubcategorias = remember {

            cantoViewModel
                .subcategoriasLiturgicas()
        }


        val subcategorias by
        fluxoSubcategorias
            .collectAsState(
                initial = emptyList()
            )


        Scaffold(

            containerColor =
                Color(0xFF1E1E1E),

            topBar = {

                TopAppBar(

                    title = {

                        Text(

                            text =
                                "ÍNDICE LITÚRGICO",

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


            LazyColumn(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )

            ) {

                items(

                    items =
                        subcategorias,

                    key = {
                        it
                    }

                ) { subcategoria ->


                    ItemSubcategoriaLiturgica(

                        nome =
                            subcategoria,

                        onClick = {

                            onSubcategoriaSelecionada(
                                subcategoria
                            )
                        }
                    )
                }
            }
        }

        return
    }


    // ====================================================
    // TELA 2 — CANTOS DA SUBCATEGORIA
    // ====================================================

    val subcategoria =
        subcategoriaSelecionada


    val fluxoCantos = remember(
        subcategoria
    ) {

        cantoViewModel
            .cantosPorSubcategoriaLiturgica(
                subcategoria
            )
    }


    val cantos by
    fluxoCantos.collectAsState(
        initial = emptyList()
    )


    // ------------------------------------------------
    // ORDENAÇÃO PT-BR
    // ------------------------------------------------

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


    val cantosOrdenados = remember(
        cantos
    ) {

        cantos.sortedWith {

                canto1,
                canto2 ->

            comparadorAlfabetico.compare(
                canto1.titulo,
                canto2.titulo
            )
        }
    }


    Scaffold(

        containerColor =
            Color(0xFF1E1E1E),

        topBar = {

            TopAppBar(

                title = {

                    Text(

                        text =
                            subcategoria.uppercase(),

                        color =
                            Color.White,

                        fontSize =
                            17.sp,

                        maxLines =
                            1
                    )
                },

                navigationIcon = {

                    IconButton(

                        onClick = {

                            onSubcategoriaSelecionada(
                                null
                            )
                        }

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
            cantosOrdenados.isEmpty()
        ) {

            Text(

                text =
                    "Nenhum canto encontrado.",

                color =
                    Color(0xFFBDBDBD),

                modifier = Modifier
                    .padding(
                        innerPadding
                    )
                    .padding(
                        24.dp
                    )
            )

        } else {

            LazyColumn(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )

            ) {

                items(

                    items =
                        cantosOrdenados,

                    key = { canto ->
                        canto.id
                    }

                ) { canto ->


                    ItemCantoIndice(

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

} // ← FIM DE TelaIndiceLiturgico


// ====================================================
// ITEM — SUBCATEGORIA
// ====================================================

@Composable
private fun ItemSubcategoriaLiturgica(

    nome: String,

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
                    horizontal = 20.dp,
                    vertical = 18.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically

        ) {


            Text(

                text =
                    nome,

                color =
                    Color.White,

                fontSize =
                    16.sp,

                modifier =
                    Modifier.weight(
                        1f
                    )
            )


            Icon(

                imageVector =
                    Icons.Default.ChevronRight,

                contentDescription =
                    null,

                tint =
                    Color(0xFF888888)
            )
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
// ITEM — CANTO DO ÍNDICE
// ====================================================

@Composable
private fun ItemCantoIndice(

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
                    horizontal = 20.dp,
                    vertical = 17.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically

        ) {


            // ----------------------------------------
            // NÚMERO
            // ----------------------------------------

            Text(

                text =
                    canto.numero,

                color =
                    Color(0xFFBDBDBD),

                fontSize =
                    14.sp,

                modifier =
                    Modifier.fillMaxWidth(
                        0.15f
                    )
            )


            // ----------------------------------------
            // TÍTULO
            // ----------------------------------------

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


            // ----------------------------------------
            // STATUS DO ÁUDIO
            // ----------------------------------------

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
                                    18.dp
                                )
                        )
                    }


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

                        modifier =
                            Modifier
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