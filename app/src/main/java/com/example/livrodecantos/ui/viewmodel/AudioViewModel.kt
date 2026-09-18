package com.example.livrodecantos.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.livrodecantos.data.database.RessuscitouDatabase
import com.example.livrodecantos.data.repository.AudioRepository
import com.example.livrodecantos.model.Canto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File


// ====================================================
// ESTADO DO PLAYER
// ====================================================

data class AudioUiState(

    // Canto atualmente carregado
    val cantoId: Int? = null,

    // Player preparando / baixando
    val carregando: Boolean = false,

    // Está tocando neste momento?
    val tocando: Boolean = false,

    // Posição atual em milissegundos
    val posicao: Long = 0L,

    // Duração em milissegundos
    val duracao: Long = 0L,

    // Erro de reprodução
    val erro: String? = null,


    // ====================================================
    // DOWNLOAD OFFLINE
    // ====================================================

    // ID do canto que está sendo baixado manualmente
    val cantoBaixandoId: Int? = null,

    // Erro de download
    val erroDownload: String? = null,
    // ====================================================
// DOWNLOAD EM LOTE
// ====================================================

    val baixandoEmLote: Boolean = false,

    val totalDownloadLote: Int = 0,

    val processadosDownloadLote: Int = 0,

    val falhasDownloadLote: Int = 0,

    val tituloDownloadAtual: String? = null,


    // ====================================================
    // FILA / PLAYLIST
    // ====================================================

    // Indica se estamos reproduzindo uma lista
    val reproduzindoLista: Boolean = false,

    // Índice atual da fila.
    // Começa em 0.
    val indiceFila: Int? = null,

    // Quantidade de cantos da fila
    val totalFila: Int = 0,

    // A fila foi iniciada em modo aleatório?
    val modoAleatorio: Boolean = false
)


// ====================================================
// AUDIO VIEWMODEL
// ====================================================

class AudioViewModel(
    application: Application
) : AndroidViewModel(application) {


    // ====================================================
    // BANCO
    // ====================================================

    private val database =
        RessuscitouDatabase
            .getDatabase(application)


    // ====================================================
    // REPOSITÓRIO
    // ====================================================

    private val repository =
        AudioRepository(

            context =
                application,

            cantoDao =
                database.cantoDao()
        )


    // ====================================================
    // EXOPLAYER
    // ====================================================

    private val player =
        ExoPlayer.Builder(
            application
        ).build()


    // ====================================================
    // ESTADO DO PLAYER
    // ====================================================

    private val _estado =
        MutableStateFlow(
            AudioUiState()
        )


    val estado:
            StateFlow<AudioUiState> =
        _estado.asStateFlow()


    // ====================================================
    // FILA INTERNA
    // ====================================================

    private var filaAtual:
            List<Canto> =
        emptyList()


    private var indiceFilaAtual:
            Int =
        -1


    // ====================================================
    // FILA OBSERVÁVEL PELA INTERFACE
    // ====================================================

    private val _filaReproducao =
        MutableStateFlow<List<Canto>>(
            emptyList()
        )


    val filaReproducao:
            StateFlow<List<Canto>> =
        _filaReproducao
            .asStateFlow()


    // ====================================================
    // INICIALIZAÇÃO
    // ====================================================

    init {

        // ------------------------------------------------
        // ESCUTAR O EXOPLAYER
        // ------------------------------------------------

        player.addListener(

            object : Player.Listener {


                // ========================================
                // PLAY / PAUSE
                // ========================================

                override fun onIsPlayingChanged(
                    isPlaying: Boolean
                ) {

                    _estado.update {

                        it.copy(
                            tocando =
                                isPlaying
                        )
                    }
                }


                // ========================================
                // ESTADO DE REPRODUÇÃO
                // ========================================

                override fun onPlaybackStateChanged(
                    playbackState: Int
                ) {

                    atualizarTempo()


                    // ------------------------------------
                    // CANTO TERMINOU
                    // ------------------------------------

                    if (
                        playbackState ==
                        Player.STATE_ENDED
                    ) {

                        // Se estivermos dentro de uma fila,
                        // toca automaticamente o próximo.

                        if (
                            filaAtual.isNotEmpty() &&
                            indiceFilaAtual in
                            filaAtual.indices
                        ) {

                            tocarProximoDaFila()

                        } else {

                            _estado.update {

                                it.copy(
                                    tocando =
                                        false
                                )
                            }
                        }
                    }
                }
            }
        )


        // ------------------------------------------------
        // ATUALIZAR POSIÇÃO DO PLAYER
        // ------------------------------------------------

        viewModelScope.launch {

            while (
                isActive
            ) {

                atualizarTempo()

                delay(
                    500
                )
            }
        }
    }


    // ====================================================
    // PLAY / PAUSE DE UM CANTO INDIVIDUAL
    // ====================================================

    fun alternarReproducao(
        canto: Canto
    ) {

        // Não inicia outro carregamento
        // enquanto houver um em andamento.

        if (
            _estado.value.carregando
        ) {
            return
        }


        // ------------------------------------------------
        // JÁ É O CANTO ATUAL
        // ------------------------------------------------

        if (
            _estado.value.cantoId ==
            canto.id &&

            player.mediaItemCount >
            0
        ) {

            // Se já terminou, começa novamente.

            if (
                player.playbackState ==
                Player.STATE_ENDED
            ) {

                player.seekTo(
                    0L
                )

                player.play()

                return
            }


            if (
                player.isPlaying
            ) {

                player.pause()

            } else {

                player.play()
            }


            return
        }


        // ------------------------------------------------
        // TOCAR UM CANTO MANUALMENTE ENCERRA A FILA
        // ------------------------------------------------

        limparFilaInterna()


        carregarAudio(

            canto =
                canto,

            pertenceAFila =
                false
        )
    }


    // ====================================================
    // REPRODUZIR LISTA EM ORDEM
    // ====================================================

    fun reproduzirListaEmOrdem(
        cantos: List<Canto>
    ) {

        iniciarFila(

            cantos =
                cantos,

            aleatorio =
                false
        )
    }


    // ====================================================
    // REPRODUZIR LISTA ALEATÓRIA
    // ====================================================

    fun reproduzirListaAleatoria(
        cantos: List<Canto>
    ) {

        iniciarFila(

            cantos =
                cantos,

            aleatorio =
                true
        )
    }


    // ====================================================
    // CRIAR E INICIAR FILA
    // ====================================================

    private fun iniciarFila(

        cantos: List<Canto>,

        aleatorio: Boolean

    ) {

        // ------------------------------------------------
        // SOMENTE CANTOS QUE POSSUEM ÁUDIO
        // ------------------------------------------------

        val cantosComAudio =

            cantos

                .filter { canto ->

                    canto.possuiAudio
                }

                .distinctBy { canto ->

                    canto.id
                }


        // ------------------------------------------------
        // NENHUM ÁUDIO
        // ------------------------------------------------

        if (
            cantosComAudio.isEmpty()
        ) {

            _estado.update {

                it.copy(

                    erro =
                        "Esta lista não possui cantos com áudio."
                )
            }

            return
        }


        // ------------------------------------------------
        // PARAR O QUE ESTIVER TOCANDO
        // ------------------------------------------------

        player.stop()

        player.clearMediaItems()


        // ------------------------------------------------
        // MONTAR FILA
        // ------------------------------------------------

        filaAtual =

            if (
                aleatorio
            ) {

                cantosComAudio
                    .shuffled()

            } else {

                cantosComAudio
            }


        // A interface consegue observar
        // a fila por este StateFlow.

        _filaReproducao.value =
            filaAtual


        // Primeiro item.

        indiceFilaAtual =
            0


        _estado.update {

            it.copy(

                reproduzindoLista =
                    true,

                indiceFila =
                    indiceFilaAtual,

                totalFila =
                    filaAtual.size,

                modoAleatorio =
                    aleatorio,

                erro =
                    null
            )
        }


        // ------------------------------------------------
        // TOCAR PRIMEIRO CANTO
        // ------------------------------------------------

        carregarAudio(

            canto =
                filaAtual[
                    indiceFilaAtual
                ],

            pertenceAFila =
                true
        )
    }


    // ====================================================
    // TOCAR UM ITEM ESPECÍFICO DA FILA
    // ====================================================

    fun reproduzirItemDaFila(
        indice: Int
    ) {

        if (
            indice !in
            filaAtual.indices
        ) {
            return
        }


        indiceFilaAtual =
            indice


        _estado.update {

            it.copy(

                reproduzindoLista =
                    true,

                indiceFila =
                    indiceFilaAtual,

                totalFila =
                    filaAtual.size,

                erro =
                    null
            )
        }


        carregarAudio(

            canto =
                filaAtual[
                    indiceFilaAtual
                ],

            pertenceAFila =
                true
        )
    }


    // ====================================================
    // PRÓXIMO
    // ====================================================

    fun proximoDaFila() {

        if (
            filaAtual.isEmpty()
        ) {
            return
        }


        tocarProximoDaFila()
    }


    // ====================================================
    // PRÓXIMO — INTERNO
    // ====================================================

    private fun tocarProximoDaFila() {

        if (
            filaAtual.isEmpty()
        ) {
            return
        }


        val proximoIndice =
            indiceFilaAtual + 1


        // ------------------------------------------------
        // ACABOU A FILA
        // ------------------------------------------------

        if (
            proximoIndice >=
            filaAtual.size
        ) {

            finalizarFila()

            return
        }


        // ------------------------------------------------
        // AVANÇAR
        // ------------------------------------------------

        indiceFilaAtual =
            proximoIndice


        _estado.update {

            it.copy(

                indiceFila =
                    indiceFilaAtual,

                totalFila =
                    filaAtual.size,

                reproduzindoLista =
                    true,

                erro =
                    null
            )
        }


        carregarAudio(

            canto =
                filaAtual[
                    indiceFilaAtual
                ],

            pertenceAFila =
                true
        )
    }


    // ====================================================
    // ANTERIOR
    // ====================================================

    fun anteriorDaFila() {

        if (
            filaAtual.isEmpty()
        ) {
            return
        }


        val indiceAnterior =
            indiceFilaAtual - 1


        // ------------------------------------------------
        // JÁ ESTAMOS NO PRIMEIRO
        // ------------------------------------------------

        if (
            indiceAnterior <
            0
        ) {

            if (
                player.mediaItemCount >
                0
            ) {

                player.seekTo(
                    0L
                )

                player.play()
            }

            return
        }


        // ------------------------------------------------
        // VOLTAR UM CANTO
        // ------------------------------------------------

        indiceFilaAtual =
            indiceAnterior


        _estado.update {

            it.copy(

                indiceFila =
                    indiceFilaAtual,

                totalFila =
                    filaAtual.size,

                reproduzindoLista =
                    true,

                erro =
                    null
            )
        }


        carregarAudio(

            canto =
                filaAtual[
                    indiceFilaAtual
                ],

            pertenceAFila =
                true
        )
    }


    // ====================================================
    // EXISTE PRÓXIMO?
    // ====================================================

    fun possuiProximoNaFila():
            Boolean {

        return (
                filaAtual.isNotEmpty() &&
                        indiceFilaAtual >= 0 &&
                        indiceFilaAtual <
                        filaAtual.lastIndex
                )
    }


    // ====================================================
    // EXISTE ANTERIOR?
    // ====================================================

    fun possuiAnteriorNaFila():
            Boolean {

        return (
                filaAtual.isNotEmpty() &&
                        indiceFilaAtual >
                        0
                )
    }


    // ====================================================
    // FINALIZAR FILA
    // ====================================================

    private fun finalizarFila() {

        filaAtual =
            emptyList()


        _filaReproducao.value =
            emptyList()


        indiceFilaAtual =
            -1


        _estado.update {

            it.copy(

                reproduzindoLista =
                    false,

                indiceFila =
                    null,

                totalFila =
                    0,

                modoAleatorio =
                    false,

                tocando =
                    false
            )
        }
    }


    // ====================================================
    // LIMPAR FILA
    // ====================================================

    fun limparFila() {

        player.stop()

        player.clearMediaItems()


        limparFilaInterna()


        _estado.update {

            it.copy(

                cantoId =
                    null,

                tocando =
                    false,

                carregando =
                    false,

                posicao =
                    0L,

                duracao =
                    0L
            )
        }
    }


    // ====================================================
    // LIMPAR FILA — INTERNO
    // ====================================================

    private fun limparFilaInterna() {

        filaAtual =
            emptyList()


        _filaReproducao.value =
            emptyList()


        indiceFilaAtual =
            -1


        _estado.update {

            it.copy(

                reproduzindoLista =
                    false,

                indiceFila =
                    null,

                totalFila =
                    0,

                modoAleatorio =
                    false
            )
        }
    }


    // ====================================================
    // DOWNLOAD MANUAL PARA OFFLINE
    // ====================================================

    fun baixarParaOffline(
        canto: Canto
    ) {

        // Já está baixando este canto.

        if (
            _estado.value.cantoBaixandoId ==
            canto.id
        ) {
            return
        }


        viewModelScope.launch {

            _estado.update {

                it.copy(

                    cantoBaixandoId =
                        canto.id,

                    erroDownload =
                        null
                )
            }


            val resultado =

                repository
                    .prepararAudio(
                        canto
                    )


            resultado.onSuccess {

                _estado.update {

                    it.copy(

                        cantoBaixandoId =
                            null,

                        erroDownload =
                            null
                    )
                }
            }


            resultado.onFailure { erro ->

                _estado.update {

                    it.copy(

                        cantoBaixandoId =
                            null,

                        erroDownload =
                            erro.message
                                ?: "Erro ao baixar o áudio."
                    )
                }
            }
        }
    }
// ====================================================
// BAIXAR VÁRIOS ÁUDIOS PARA OFFLINE
// ====================================================

    fun baixarTodosParaOffline(
        cantos: List<Canto>
    ) {

        if (
            _estado.value.baixandoEmLote
        ) {
            return
        }


        // ------------------------------------------------
        // SOMENTE CANTOS QUE:
        // 1. POSSUEM ÁUDIO
        // 2. AINDA NÃO ESTÃO OFFLINE
        // ------------------------------------------------

        val paraBaixar =

            cantos

                .filter { canto ->

                    canto.possuiAudio
                }

                .distinctBy { canto ->

                    canto.id
                }

                .filter { canto ->

                    val caminho =
                        canto.audioLocalPath


                    caminho.isNullOrBlank() ||
                            !File(caminho).exists()
                }


        // ------------------------------------------------
        // NADA PARA BAIXAR
        // ------------------------------------------------

        if (
            paraBaixar.isEmpty()
        ) {

            _estado.update {

                it.copy(

                    baixandoEmLote =
                        false,

                    totalDownloadLote =
                        0,

                    processadosDownloadLote =
                        0,

                    falhasDownloadLote =
                        0,

                    tituloDownloadAtual =
                        null,

                    erroDownload =
                        null
                )
            }

            return
        }


        // ------------------------------------------------
        // DOWNLOAD
        // ------------------------------------------------

        viewModelScope.launch {

            var processados =
                0

            var falhas =
                0


            _estado.update {

                it.copy(

                    baixandoEmLote =
                        true,

                    totalDownloadLote =
                        paraBaixar.size,

                    processadosDownloadLote =
                        0,

                    falhasDownloadLote =
                        0,

                    tituloDownloadAtual =
                        null,

                    erroDownload =
                        null
                )
            }


            for (
            canto in paraBaixar
            ) {

                _estado.update {

                    it.copy(

                        tituloDownloadAtual =
                            canto.titulo
                    )
                }


                val resultado =

                    repository
                        .prepararAudio(
                            canto
                        )


                if (
                    resultado.isFailure
                ) {

                    falhas++
                }


                processados++


                _estado.update {

                    it.copy(

                        processadosDownloadLote =
                            processados,

                        falhasDownloadLote =
                            falhas
                    )
                }
            }


            // ------------------------------------------------
            // TERMINOU
            // ------------------------------------------------

            _estado.update {

                it.copy(

                    baixandoEmLote =
                        false,

                    tituloDownloadAtual =
                        null,

                    erroDownload =

                        if (
                            falhas > 0
                        ) {

                            if (
                                falhas == 1
                            ) {

                                "1 áudio não pôde ser baixado."

                            } else {

                                "$falhas áudios não puderam ser baixados."
                            }

                        } else {

                            null
                        }
                )
            }
        }
    }

    // ====================================================
    // CARREGAR E TOCAR
    // ====================================================

    private fun carregarAudio(

        canto: Canto,

        pertenceAFila: Boolean

    ) {

        _estado.update {

            it.copy(

                cantoId =
                    canto.id,

                carregando =
                    true,

                tocando =
                    false,

                posicao =
                    0L,

                duracao =
                    0L,

                erro =
                    null
            )
        }


        viewModelScope.launch {

            val resultado =

                repository
                    .prepararAudio(
                        canto
                    )


            resultado.onSuccess { arquivo ->

                val mediaItem =

                    MediaItem.fromUri(

                        Uri.fromFile(
                            arquivo
                        )
                    )


                player.setMediaItem(
                    mediaItem
                )


                player.prepare()


                player.playWhenReady =
                    true


                _estado.update {

                    it.copy(

                        carregando =
                            false,

                        erro =
                            null
                    )
                }
            }


            resultado.onFailure { erro ->

                _estado.update {

                    it.copy(

                        carregando =
                            false,

                        tocando =
                            false,

                        erro =
                            erro.message
                                ?: "Erro ao carregar o áudio."
                    )
                }


                // Se um item da fila falhar,
                // tenta automaticamente o próximo.

                if (
                    pertenceAFila &&
                    filaAtual.isNotEmpty()
                ) {

                    tocarProximoDaFila()
                }
            }
        }
    }


    // ====================================================
    // ALTERAR POSIÇÃO
    // ====================================================

    fun buscarPosicao(
        posicao: Long
    ) {

        if (
            player.mediaItemCount ==
            0
        ) {
            return
        }


        val duracao =
            _estado.value.duracao


        val destino =

            if (
                duracao >
                0L
            ) {

                posicao.coerceIn(
                    0L,
                    duracao
                )

            } else {

                posicao.coerceAtLeast(
                    0L
                )
            }


        player.seekTo(
            destino
        )


        atualizarTempo()
    }


    // ====================================================
    // ATUALIZAR TEMPO
    // ====================================================

    private fun atualizarTempo() {

        if (
            player.mediaItemCount ==
            0
        ) {
            return
        }


        val posicao =

            player.currentPosition
                .coerceAtLeast(
                    0L
                )


        val duracaoPlayer =
            player.duration


        val duracao =

            if (
                duracaoPlayer >
                0L
            ) {

                duracaoPlayer

            } else {

                0L
            }


        _estado.update {

            it.copy(

                posicao =
                    posicao,

                duracao =
                    duracao,

                tocando =
                    player.isPlaying
            )
        }
    }


    // ====================================================
    // EXCLUIR UM ÁUDIO OFFLINE
    // ====================================================

    fun excluirAudioOffline(
        canto: Canto
    ) {

        viewModelScope.launch {

            // ------------------------------------------------
            // SE ESTIVER TOCANDO ESTE CANTO
            // ------------------------------------------------

            if (
                _estado.value.cantoId ==
                canto.id
            ) {

                player.stop()

                player.clearMediaItems()


                limparFilaInterna()


                _estado.update {

                    it.copy(

                        cantoId =
                            null,

                        tocando =
                            false,

                        posicao =
                            0L,

                        duracao =
                            0L,

                        carregando =
                            false
                    )
                }
            }


            repository
                .excluirAudioOffline(
                    canto
                )
                .onFailure { erro ->

                    _estado.update {

                        it.copy(

                            erroDownload =
                                erro.message
                                    ?: "Não foi possível excluir o áudio."
                        )
                    }
                }
        }
    }


    // ====================================================
    // EXCLUIR TODOS OS ÁUDIOS OFFLINE
    // ====================================================

    fun excluirTodosAudiosOffline(
        cantos: List<Canto>
    ) {

        viewModelScope.launch {

            // ------------------------------------------------
            // PARAR PLAYER
            // ------------------------------------------------

            player.stop()

            player.clearMediaItems()


            limparFilaInterna()


            _estado.update {

                it.copy(

                    cantoId =
                        null,

                    tocando =
                        false,

                    posicao =
                        0L,

                    duracao =
                        0L,

                    carregando =
                        false,

                    erro =
                        null,

                    erroDownload =
                        null
                )
            }


            // ------------------------------------------------
            // APAGAR ARQUIVOS
            // ------------------------------------------------

            for (
            canto in cantos
            ) {

                val resultado =

                    repository
                        .excluirAudioOffline(
                            canto
                        )


                resultado.onFailure { erro ->

                    _estado.update {

                        it.copy(

                            erroDownload =
                                erro.message
                                    ?: "Não foi possível excluir todos os áudios."
                        )
                    }


                    return@launch
                }
            }
        }
    }


    // ====================================================
    // LIBERAR PLAYER
    // ====================================================

    override fun onCleared() {

        player.release()

        super.onCleared()
    }
}