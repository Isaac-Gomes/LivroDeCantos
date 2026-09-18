package com.example.livrodecantos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.livrodecantos.data.entity.ListaEntity
import com.example.livrodecantos.ui.viewmodel.ListaViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaMinhasListas(

    listaViewModel: ListaViewModel,

    onVoltar: () -> Unit,

    onListaClick: (ListaEntity) -> Unit

) {

    val listas by
    listaViewModel
        .listas
        .collectAsState()


    var mostrarNovaLista by remember {

        mutableStateOf(
            false
        )
    }


    var nomeNovaLista by remember {

        mutableStateOf(
            ""
        )
    }


    Scaffold(

        containerColor =
            Color(0xFF1E1E1E),

        topBar = {

            TopAppBar(

                title = {

                    Text(

                        text =
                            "MINHAS LISTAS",

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


            Button(

                onClick = {

                    nomeNovaLista =
                        ""

                    mostrarNovaLista =
                        true
                },

                modifier =
                    Modifier.padding(
                        horizontal = 20.dp,
                        vertical = 16.dp
                    )

            ) {

                Icon(

                    imageVector =
                        Icons.Default.Add,

                    contentDescription =
                        null
                )


                Text(

                    text =
                        "Nova lista",

                    modifier =
                        Modifier.padding(
                            start = 8.dp
                        )
                )
            }


            HorizontalDivider(
                color =
                    Color(0xFF333333)
            )


            if (
                listas.isEmpty()
            ) {

                Column(

                    modifier =
                        Modifier.padding(
                            24.dp
                        )

                ) {

                    Icon(

                        imageVector =
                            Icons.Default.QueueMusic,

                        contentDescription =
                            null,

                        tint =
                            Color(0xFF9E9E9E)
                    )


                    Text(

                        text =
                            "Você ainda não criou nenhuma lista.",

                        color =
                            Color(0xFFBDBDBD),

                        fontSize =
                            15.sp,

                        modifier =
                            Modifier.padding(
                                top = 12.dp
                            )
                    )


                    Text(

                        text =
                            "Crie uma lista e selecione os cantos que deseja utilizar.",

                        color =
                            Color(0xFF8E8E8E),

                        fontSize =
                            13.sp,

                        modifier =
                            Modifier.padding(
                                top = 6.dp
                            )
                    )
                }

            } else {

                LazyColumn(

                    modifier =
                        Modifier.fillMaxSize()

                ) {

                    items(

                        items =
                            listas,

                        key = { lista ->

                            lista.id
                        }

                    ) { lista ->

                        ItemLista(

                            lista =
                                lista,

                            onClick = {

                                onListaClick(
                                    lista
                                )
                            }
                        )
                    }
                }
            }
        }
    }


    // ====================================================
    // NOVA LISTA
    // ====================================================

    if (
        mostrarNovaLista
    ) {

        AlertDialog(

            onDismissRequest = {

                mostrarNovaLista =
                    false
            },

            title = {

                Text(
                    text =
                        "Nova lista"
                )
            },

            text = {

                OutlinedTextField(

                    value =
                        nomeNovaLista,

                    onValueChange = {

                        nomeNovaLista =
                            it
                    },

                    label = {

                        Text(
                            text =
                                "Nome da lista"
                        )
                    },

                    placeholder = {

                        Text(
                            text =
                                "Ex.: Celebração de sábado"
                        )
                    },

                    singleLine =
                        true,

                    modifier =
                        Modifier.fillMaxWidth()
                )
            },

            confirmButton = {

                TextButton(

                    enabled =
                        nomeNovaLista
                            .isNotBlank(),

                    onClick = {

                        listaViewModel
                            .criarLista(
                                nomeNovaLista
                            )


                        mostrarNovaLista =
                            false


                        nomeNovaLista =
                            ""
                    }

                ) {

                    Text(
                        text =
                            "Criar"
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        mostrarNovaLista =
                            false

                        nomeNovaLista =
                            ""
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
// ITEM
// ====================================================

@Composable
private fun ItemLista(

    lista: ListaEntity,

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

            Icon(

                imageVector =
                    Icons.Default.QueueMusic,

                contentDescription =
                    null,

                tint =
                    Color(0xFFBDBDBD)
            )


            Text(

                text =
                    lista.nome,

                color =
                    Color.White,

                fontSize =
                    16.sp,

                fontWeight =
                    FontWeight.Medium,

                modifier =
                    Modifier
                        .weight(1f)
                        .padding(
                            start = 16.dp
                        )
            )


            Icon(

                imageVector =
                    Icons.Default.ChevronRight,

                contentDescription =
                    "Abrir lista",

                tint =
                    Color(0xFF777777)
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