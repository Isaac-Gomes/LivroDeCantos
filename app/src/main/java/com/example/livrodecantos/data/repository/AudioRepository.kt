package com.example.livrodecantos.data.repository

import android.content.Context
import com.example.livrodecantos.data.dao.CantoDao
import com.example.livrodecantos.model.Canto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


class AudioRepository(


    private val context: Context,

    private val cantoDao: CantoDao

) {


    // ------------------------------------------------
    // PREPARAR ÁUDIO
    // ------------------------------------------------
    private fun normalizarUrl(
        url: String
    ): String {

        val limpa =
            url.trim()

        // Se a URL já estiver corretamente codificada,
        // simplesmente preservamos os %20 etc.
        try {

            return URI(limpa)
                .toASCIIString()

        } catch (_: Exception) {

            // Se houver espaços ou outros caracteres
            // ainda não codificados, fazemos a conversão.
            val original =
                URL(limpa)

            return URI(
                original.protocol,
                original.userInfo,
                original.host,
                original.port,
                original.path,
                original.query,
                original.ref
            ).toASCIIString()
        }
    }

    suspend fun prepararAudio(
        canto: Canto
    ): Result<File> {

        return withContext(
            Dispatchers.IO
        ) {

            val resultado = runCatching {

                // ------------------------------------
                // 1. JÁ EXISTE LOCALMENTE?
                // ------------------------------------

                val caminhoSalvo =
                    canto.audioLocalPath


                if (
                    !caminhoSalvo.isNullOrBlank()
                ) {

                    val arquivo =
                        File(caminhoSalvo)


                    if (arquivo.exists()) {

                        return@runCatching arquivo
                    }
                }


                // ------------------------------------
                // 2. OBTER URL DO MP3
                // ------------------------------------

                val audioUrl =

                    if (
                        !canto.audioUrl
                            .isNullOrBlank()
                    ) {

                        canto.audioUrl

                    } else {

                        val paginaUrl =
                            canto.paginaUrl
                                ?: error(
                                    "Este canto não possui página de origem."
                                )


                        val urlEncontrada =
                            descobrirMp3(
                                paginaUrl
                            )


                        cantoDao
                            .atualizarAudioUrl(
                                cantoId =
                                    canto.id,

                                audioUrl =
                                    urlEncontrada
                            )


                        urlEncontrada
                    }


                // ------------------------------------
                // 3. CRIAR PASTA DE ÁUDIOS
                // ------------------------------------

                val pastaAudios =
                    File(
                        context.filesDir,
                        "audios"
                    )


                if (
                    !pastaAudios.exists()
                ) {

                    check(pastaAudios.mkdirs() || pastaAudios.exists()) {
                        "Não foi possível criar a pasta de áudios."
                    }
                }


                // ------------------------------------
                // 4. ARQUIVO FINAL
                // ------------------------------------

                val arquivoFinal =
                    File(
                        pastaAudios,
                        "canto_${canto.id}.mp3"
                    )


                // Arquivo temporário:
                val arquivoTemp =
                    File(
                        pastaAudios,
                        "canto_${canto.id}.download"
                    )


                // ------------------------------------
                // 5. BAIXAR
                // ------------------------------------

                baixarArquivo(
                    url = audioUrl,
                    destino = arquivoTemp
                )


                // ------------------------------------
                // 6. RENOMEAR SOMENTE DEPOIS
                //    DO DOWNLOAD COMPLETO
                // ------------------------------------

                if (
                    arquivoFinal.exists()
                ) {
                    arquivoFinal.delete()
                }


                val renomeou =
                    arquivoTemp.renameTo(
                        arquivoFinal
                    )


                if (!renomeou) {

                    arquivoTemp.copyTo(
                        target = arquivoFinal,
                        overwrite = true
                    )

                    arquivoTemp.delete()
                }


                // ------------------------------------
                // 7. SALVAR CAMINHO NO ROOM
                // ------------------------------------

                cantoDao
                    .atualizarAudioLocalPath(

                        cantoId =
                            canto.id,

                        caminho =
                            arquivoFinal.absolutePath
                    )


                arquivoFinal
            }

            resultado.exceptionOrNull()?.let { erro ->
                if (erro is CancellationException) {
                    throw erro
                }
            }

            resultado
        }
    }


    // ------------------------------------------------
    // ENCONTRAR URL .MP3 NA PÁGINA
    // ------------------------------------------------

    private fun descobrirMp3(
        paginaUrl: String
    ): String {

        val connection =
            URL(paginaUrl)
                .openConnection()
                    as HttpURLConnection


        try {

            connection.requestMethod =
                "GET"

            connection.instanceFollowRedirects =
                true

            connection.connectTimeout =
                15_000

            connection.readTimeout =
                20_000

            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 Android Ressuscitou"
            )


            val codigo =
                connection.responseCode


            if (
                codigo !in 200..299
            ) {

                error(
                    "Erro ao abrir página: HTTP $codigo"
                )
            }


            val html =
                connection.inputStream
                    .bufferedReader()
                    .use {
                        it.readText()
                    }


            // Primeiro procura URLs completas.

            val regexUrlCompleta =
                Regex(
                    """https?://[^"' <>\s]+\.mp3(?:\?[^"' <>\s]*)?""",
                    RegexOption.IGNORE_CASE
                )


            val completa =
                regexUrlCompleta
                    .find(html)
                    ?.value
                    ?.replace(
                        "&amp;",
                        "&"
                    )


            if (
                !completa.isNullOrBlank()
            ) {

                return normalizarUrl(
                    completa
                )
            }


            // Se o site estiver usando caminho relativo:
            // href="/uploads/audio/canto.mp3"

            val regexRelativa =
                Regex(
                    """(?:href|src)\s*=\s*["']([^"']+\.mp3(?:\?[^"']*)?)["']""",
                    RegexOption.IGNORE_CASE
                )


            val caminhoRelativo =
                regexRelativa
                    .find(html)
                    ?.groupValues
                    ?.getOrNull(1)
                    ?.replace(
                        "&amp;",
                        "&"
                    )
                    ?: error(
                        "Nenhum arquivo MP3 encontrado nesta página."
                    )


            val urlCompleta =
                URL(
                    URL(paginaUrl),
                    caminhoRelativo
                ).toString()

            return normalizarUrl(
                urlCompleta
            )

        } finally {

            connection.disconnect()
        }
    }


    // ------------------------------------------------
    // DOWNLOAD
    // ------------------------------------------------

    private fun baixarArquivo(

        url: String,

        destino: File

    ) {

        val urlSegura =
            normalizarUrl(url)

        val connection =
            URL(urlSegura)
                .openConnection()
                    as HttpURLConnection


        try {

            connection.requestMethod =
                "GET"

            connection.instanceFollowRedirects =
                true

            connection.connectTimeout =
                20_000

            connection.readTimeout =
                60_000

            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 Android Ressuscitou"
            )


            val codigo =
                connection.responseCode


            if (
                codigo !in 200..299
            ) {

                error(
                    "Erro ao baixar áudio: HTTP $codigo"
                )
            }


            connection
                .inputStream
                .use { entrada ->

                    destino
                        .outputStream()
                        .buffered()
                        .use { saida ->

                            entrada.copyTo(
                                saida
                            )
                        }
                }


            if (
                !destino.exists() ||
                destino.length() == 0L
            ) {

                error(
                    "O arquivo de áudio baixado está vazio."
                )
            }

        } catch (
            erro: Exception
        ) {

            if (
                destino.exists()
            ) {

                destino.delete()
            }

            throw erro

        } finally {

            connection.disconnect()
        }
    }

    suspend fun excluirAudioOffline(
        canto: Canto
    ): Result<Unit> {

        return withContext(
            Dispatchers.IO
        ) {

            runCatching {

                val caminho =
                    canto.audioLocalPath

                if (
                    !caminho.isNullOrBlank()
                ) {

                    val arquivo =
                        File(caminho)

                    if (
                        arquivo.exists()
                    ) {

                        val apagou =
                            arquivo.delete()

                        if (
                            !apagou &&
                            arquivo.exists()
                        ) {

                            error(
                                "Não foi possível excluir o arquivo de áudio."
                            )
                        }
                    }
                }


                cantoDao
                    .limparAudioLocalPath(
                        canto.id
                    )
            }
        }
    }

}


