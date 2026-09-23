package com.example.livrodecantos.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.livrodecantos.data.repository.PreferenciasCifraRepository
import com.example.livrodecantos.domain.cifra.CifraTranspositor
import com.example.livrodecantos.model.cifra.CifraCanto
import com.example.livrodecantos.model.cifra.ConfiguracaoCifra
import java.util.Locale
import kotlinx.coroutines.launch

/** Diálogo complementar: a ficha continua composta por baixo, preservando seus gestos e estado. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCifra(
    titulo: String,
    cifra: CifraCanto,
    onFechar: () -> Unit,
    preferenciasCifraRepository: PreferenciasCifraRepository? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repositorioPadrao = remember(context) { PreferenciasCifraRepository(context) }
    val repositorio = preferenciasCifraRepository ?: repositorioPadrao
    val configuracao by repositorio.observarConfiguracao(cifra.cantoId)
        .collectAsState(initial = ConfiguracaoCifra())
    val escopo = rememberCoroutineScope()
    val formas = remember(cifra, configuracao) {
        CifraTranspositor.transporParaFormas(cifra, configuracao.semitons, configuracao.capotraste)
    }
    val tomAtual = CifraTranspositor.tomAtual(
        cifra.tomOriginal, configuracao.semitons, cifra.preferenciaAcidentes
    )
    val formasNaFicha = CifraTranspositor.transporParaForma(
        cifra.tomOriginal,
        configuracao.semitons,
        configuracao.capotraste,
        cifra.preferenciaAcidentes
    )

    Dialog(onDismissRequest = onFechar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color(0xFF181818),
            contentColor = Color(0xFFF5F5F5),
            topBar = {
                TopAppBar(
                    title = { Text(titulo.uppercase(Locale.forLanguageTag("pt-BR")), fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = onFechar) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar à ficha")
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
            Column(Modifier.fillMaxSize().padding(padding)) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Tom original: ${cifra.tomOriginal}", fontSize = 16.sp)
                    Text("Tom que soa: $tomAtual", fontSize = 16.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            escopo.launch { repositorio.ajustarSemitons(cifra.cantoId, -1) }
                        }) {
                            Icon(Icons.Default.Remove, contentDescription = "Descer um semitom", tint = Color.White)
                        }
                        Text(
                            "${formatarDeslocamento(configuracao.semitons)}  $tomAtual",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = {
                            escopo.launch { repositorio.ajustarSemitons(cifra.cantoId, 1) }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Subir um semitom", tint = Color.White)
                        }
                    }
                    Text("Capotraste", fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            enabled = configuracao.capotraste > 0,
                            onClick = { escopo.launch { repositorio.ajustarCapotraste(cifra.cantoId, -1) } }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Diminuir capotraste", tint = Color.White)
                        }
                        Text("Casa ${configuracao.capotraste}", fontSize = 18.sp)
                        IconButton(
                            enabled = configuracao.capotraste < 11,
                            onClick = { escopo.launch { repositorio.ajustarCapotraste(cifra.cantoId, 1) } }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Aumentar capotraste", tint = Color.White)
                        }
                    }
                    Text("Formas tocadas: $formasNaFicha", fontSize = 16.sp)
                    TextButton(
                        enabled = configuracao.capotraste != 0,
                        onClick = { escopo.launch { repositorio.salvarCapotraste(cifra.cantoId, 0) } }
                    ) { Text("Sem capotraste") }
                    TextButton(
                        enabled = configuracao != ConfiguracaoCifra(),
                        onClick = { escopo.launch { repositorio.restaurarConfiguracao(cifra.cantoId) } }
                    ) { Text("Restaurar ficha original") }
                }
                HorizontalDivider(color = Color(0xFF333333))
                // Rolagem horizontal compartilhada evita quebrar e desalinhar linhas longas.
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    formas.linhas.forEach { linha ->
                        if (linha.texto.isEmpty() && linha.acordes.isEmpty()) {
                            Spacer(Modifier.height(16.dp))
                        } else {
                            Column {
                                if (linha.acordes.isNotEmpty()) {
                                    val acordes = buildString {
                                        linha.acordes.sortedBy { it.posicao }.forEach {
                                            append(" ".repeat((it.posicao - length).coerceAtLeast(0)))
                                            append(it.acorde)
                                        }
                                    }
                                    Text(
                                        acordes, color = Color(0xFFFF6B6B),
                                        fontFamily = FontFamily.Monospace, fontSize = 18.sp,
                                        lineHeight = 26.sp, softWrap = false
                                    )
                                }
                                Text(
                                    linha.texto, color = Color(0xFFF5F5F5),
                                    fontFamily = FontFamily.Monospace, fontSize = 18.sp,
                                    lineHeight = 26.sp, softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatarDeslocamento(semitons: Int): String = when {
    semitons > 0 -> "+$semitons"
    else -> semitons.toString()
}
