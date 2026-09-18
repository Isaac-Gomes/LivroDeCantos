package com.example.livrodecantos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.example.livrodecantos.model.Canto
import com.example.livrodecantos.model.Etapa


@Composable
fun NumeracaoCanto(

    canto: Canto,

    modifier: Modifier =
        Modifier

) {

    val numeros =

        canto.numero

            .split(",")

            .map {
                it.trim()
            }

            .filter {
                it.isNotBlank()
            }


    if (
        numeros.isEmpty()
    ) {
        return
    }


    val corFundo =

        when (
            canto.etapa
        ) {

            Etapa.PRE_CATECUMENATO ->
                Color(0xFFF5F5F5)

            Etapa.LITURGICOS ->
                Color(0xFFFFD54F)

            Etapa.CATECUMENATO ->
                Color(0xFF42A5F5)

            Etapa.ELEICAO ->
                Color(0xFF66BB6A)
        }


    val corTexto =

        when (
            canto.etapa
        ) {

            Etapa.PRE_CATECUMENATO,
            Etapa.LITURGICOS ->

                Color(0xFF202020)


            Etapa.CATECUMENATO,
            Etapa.ELEICAO ->

                Color.White
        }


    Row(

        modifier =
            modifier,

        horizontalArrangement =
            Arrangement.spacedBy(
                3.dp
            )

    ) {

        numeros.forEach { numero ->

            Box(

                modifier = Modifier

                    .size(
                        29.dp
                    )

                    .clip(
                        CircleShape
                    )

                    .background(
                        corFundo
                    )

                    .border(

                        width =
                            1.dp,

                        color =

                            if (
                                canto.etapa ==
                                Etapa.PRE_CATECUMENATO
                            ) {

                                Color(0xFFBDBDBD)

                            } else {

                                corFundo
                            },

                        shape =
                            CircleShape
                    ),

                contentAlignment =
                    Alignment.Center

            ) {

                Text(

                    text =
                        numero,

                    color =
                        corTexto,

                    fontSize =
                        9.sp,

                    fontWeight =
                        FontWeight.Bold,

                    maxLines =
                        1
                )
            }
        }
    }
}