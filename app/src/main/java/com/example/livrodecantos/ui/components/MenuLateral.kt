package com.example.livrodecantos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QueueMusic



@Composable
fun MenuLateral(
    onTodosCantos: () -> Unit,
    onOrdemAlfabetica: () -> Unit,
    onIndiceLiturgico: () -> Unit,
    onIndiceBiblico: () -> Unit,
    onMinhasListas: () -> Unit,
    onPreCatecumenato: () -> Unit,
    onLiturgicos: () -> Unit,
    onCatecumenato: () -> Unit,
    onEleicao: () -> Unit,
    onAudiosOffline: () -> Unit,

) {

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.82f)
            .background(Color(0xFF212121))
    ) {

        // Cabeçalho

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF151515))
                .padding(
                    start = 24.dp,
                    top = 48.dp,
                    bottom = 24.dp
                )
        ) {

            Text(
                text = "RESSUSCITOU",
                color = Color(0xFFD32F2F),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Livro de Cantos",
                color = Color(0xFFAAAAAA),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        ItemMenu(
            texto = "Todos os Cantos",
            icone = {
                Icon(
                    imageVector = Icons.Default.LibraryMusic,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            onClick = onTodosCantos
        )

        ItemMenu(
            texto = "Ordem Alfabética",
            icone = {
                Icon(
                    imageVector = Icons.Default.SortByAlpha,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            onClick = onOrdemAlfabetica
        )

        ItemMenu(
            texto = "Índice Litúrgico",
            icone = {
                Icon(
                    imageVector = Icons.Default.FormatListBulleted,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            onClick = onIndiceLiturgico
        )

        ItemMenu(
            texto = "Índice Bíblico",
            icone = {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            onClick = onIndiceBiblico
        )
        ItemMenu(

            texto =
                "Minhas Listas",

            icone = {

                Icon(

                    imageVector =
                        Icons.Default.QueueMusic,

                    contentDescription =
                        null,

                    tint =
                        Color.White
                )
            },

            onClick =
                onMinhasListas
        )
        ItemMenu(
            texto = "Áudios Offline",
            icone = {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = Color.White
                )
            },
            onClick = onAudiosOffline
        )
        HorizontalDivider(
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 16.dp
            ),
            color = Color(0xFF3A3A3A)
        )

        Text(
            text = "FILTRAR ETAPA",
            color = Color(0xFF9E9E9E),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                start = 24.dp,
                bottom = 8.dp
            )
        )

        ItemEtapa(
            texto = "Pré-Catecumenato",
            cor = Color(0xFFF5F5F5),
            onClick = onPreCatecumenato
        )

        ItemEtapa(
            texto = "Litúrgicos",
            cor = Color(0xFFFFD54F),
            onClick = onLiturgicos
        )

        ItemEtapa(
            texto = "Catecumenato",
            cor = Color(0xFF42A5F5),
            onClick = onCatecumenato
        )

        ItemEtapa(
            texto = "Eleição",
            cor = Color(0xFF43A047),
            onClick = onEleicao
        )

            }


    }




@Composable
private fun ItemMenu(
    texto: String,
    icone: @Composable () -> Unit,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 24.dp,
                vertical = 15.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            icone()
        }

        Text(
            text = texto,
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 20.dp)
        )
    }

}


@Composable
private fun ItemEtapa(
    texto: String,
    cor: Color,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 24.dp,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(cor)
        )

        Text(
            text = texto,
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 20.dp)
        )
    }
}

