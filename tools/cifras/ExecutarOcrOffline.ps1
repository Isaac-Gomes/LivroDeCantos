param([string]$RaizProjeto = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/OcrWinRt.ps1"
. "$PSScriptRoot/FuncoesOcr.ps1"
Add-Type -AssemblyName System.Drawing
Add-Type -Path (Join-Path $PSScriptRoot 'ImagemCifra.cs') -ReferencedAssemblies System.Drawing
$debugAcordes = Join-Path $PSScriptRoot 'ocr_debug/acordes'
$debugLetras = Join-Path $PSScriptRoot 'ocr_debug/letras'
$cropsLetras = Join-Path $PSScriptRoot 'revisao/crops_letras'
New-Item -ItemType Directory -Force -Path $debugAcordes, $debugLetras, $cropsLetras | Out-Null

$grupos = Get-Content -Raw -Encoding UTF8 (Join-Path $PSScriptRoot 'grupos_visuais_acordes.json') | ConvertFrom-Json
$agrupadas = Get-Content -Raw -Encoding UTF8 (Join-Path $PSScriptRoot 'candidatos_acordes_agrupados.json') | ConvertFrom-Json
$resultadoAcordes = [System.Collections.Generic.List[object]]::new()
$numeroGrupo = 0
foreach ($grupo in $grupos) {
    $numeroGrupo++
    $hash = [string]$grupo.hashVisual
    $representante = $grupo.exemplos[0]
    $origem = Join-Path $PSScriptRoot $representante.arquivoCrop
    $variantes = [System.Collections.Generic.List[object]]::new()
    foreach ($escala in @(4, 8)) {
        $arquivoPreparado = Join-Path $debugAcordes ("{0}_x{1}.png" -f $hash, $escala)
        [Ressuscitou.Cifras.Tools.ImagemCifra]::PrepararAcorde($origem, $arquivoPreparado, $escala, 24)
        $ocr = Invocar-OcrWinRt $arquivoPreparado
        $variantes.Add([pscustomobject][ordered]@{ escala=$escala; textoOCR=$ocr.texto; arquivo=('ocr_debug/acordes/' + [IO.Path]::GetFileName($arquivoPreparado)) })
    }
    $classificacao = Classificar-ResultadoOcrAcorde @($variantes)
    $ocorrencias = @($agrupadas | Where-Object hashVisual -eq $hash)
    $resultadoAcordes.Add([pscustomobject][ordered]@{
        grupoId=('grupo_{0:D4}' -f $numeroGrupo); hashVisual=$hash; textoOCRBruto=@($variantes | Select-Object -ExpandProperty textoOCR)
        acordeNormalizadoProposto=$classificacao.acorde; confianca=$classificacao.confianca; classificacao=$classificacao.classificacao
        variantes=@($variantes); quantidadeOcorrencias=$ocorrencias.Count
        ocorrencias=@($ocorrencias | Select-Object cantoId, pagina, indiceOcorrencia, arquivoCrop, x, y, largura, altura)
    })
}
$resultadoAcordes | ConvertTo-Json -Depth 8 | Set-Content -Encoding UTF8 (Join-Path $PSScriptRoot 'ocr_acordes_resultado.json')

$cantos = Get-Content -Raw -Encoding UTF8 (Join-Path $RaizProjeto 'app/src/main/assets/data/cantos.json') | ConvertFrom-Json
$fichas = Get-Content -Raw -Encoding UTF8 (Join-Path $RaizProjeto 'app/src/main/assets/data/fichas.json') | ConvertFrom-Json
$porCanto = @{}; foreach ($canto in $cantos) { $porCanto[[int]$canto.id] = $canto }
$letras = [System.Collections.Generic.List[object]]::new()
foreach ($ficha in @($fichas | Sort-Object cantoId, ordem)) {
    $canto = $porCanto[[int]$ficha.cantoId]
    $origem = Join-Path $RaizProjeto ('app/src/main/assets/' + $ficha.arquivo)
    $preparadaNome = 'canto_{0:D3}_p{1:D2}.png' -f [int]$ficha.cantoId, [int]$ficha.ordem
    $preparada = Join-Path $debugLetras $preparadaNome
    [Ressuscitou.Cifras.Tools.ImagemCifra]::PrepararLetra($origem, $preparada)
    $ocr = Invocar-OcrWinRt $preparada
    $bitmap = [System.Drawing.Bitmap]::FromFile($preparada)
    try {
        $linhas = [System.Collections.Generic.List[object]]::new(); $indiceLinha = 0
        foreach ($linha in @(Ordenar-LinhasOcr @($ocr.linhas))) {
            $indiceLinha++; $confianca = Obter-ConfiancaLinhaOcr $linha.texto
            $cropNome = 'canto_{0:D3}_p{1:D2}_l{2:D3}.png' -f [int]$ficha.cantoId, [int]$ficha.ordem, $indiceLinha
            $crop = Join-Path $cropsLetras $cropNome
            [Ressuscitou.Cifras.Tools.ImagemCifra]::Recortar($preparada, $crop, [math]::Max(0,$linha.x-8), [math]::Max(0,$linha.y-8), [math]::Min($bitmap.Width-$linha.x+8,$linha.largura+16), [math]::Min($bitmap.Height-$linha.y+8,$linha.altura+16))
            $linhas.Add([pscustomobject][ordered]@{ pagina=[int]$ficha.ordem; indice=$indiceLinha; texto=$linha.texto; confianca=$confianca; revisaoNecessaria=($confianca -lt 0.85); x=($linha.x / $bitmap.Width); y=($linha.y / $bitmap.Height); largura=($linha.largura / $bitmap.Width); altura=($linha.altura / $bitmap.Height); arquivoCrop=('revisao/crops_letras/' + $cropNome); acordesRelacionados=@() })
        }
        $letras.Add([pscustomobject][ordered]@{ cantoId=[int]$canto.id; numero=[string]$canto.numero; titulo=[string]$canto.titulo; tituloOCR=$null; referenciaBiblica=$null; paginas=@([pscustomobject]@{ pagina=[int]$ficha.ordem; arquivoFicha=$ficha.arquivo; linhas=@($linhas) }); revisaoNecessaria=(@($linhas | Where-Object revisaoNecessaria).Count -gt 0) })
    } finally { $bitmap.Dispose() }
}
# Reagrupa páginas do mesmo canto mantendo a ordem vertical original.
$letrasPorCanto = foreach ($grupo in @($letras | Group-Object cantoId | Sort-Object Name)) {
    $partes = @($grupo.Group | Sort-Object { $_.paginas[0].pagina })
    [pscustomobject][ordered]@{ cantoId=$partes[0].cantoId; numero=$partes[0].numero; titulo=$partes[0].titulo; tituloOCR=$null; referenciaBiblica=$null; paginas=@($partes | ForEach-Object { $_.paginas } | Sort-Object pagina); revisaoNecessaria=(@($partes | Where-Object revisaoNecessaria).Count -gt 0) }
}
$letrasPorCanto | ConvertTo-Json -Depth 9 | Set-Content -Encoding UTF8 (Join-Path $PSScriptRoot 'letras_candidatas.json')
$todasLinhas = @($letrasPorCanto | ForEach-Object { $_.paginas } | ForEach-Object { $_.linhas })
$alta = @($todasLinhas | Where-Object { $_.confianca -ge 0.85 }).Count; $media = @($todasLinhas | Where-Object { $_.confianca -ge 0.60 -and $_.confianca -lt 0.85 }).Count; $pendentes = @($todasLinhas | Where-Object revisaoNecessaria).Count
$relatorio = @"
OCR utilizado: Windows.Media.Ocr (pt-BR), execução offline WinRT/MTA.
Páginas processadas: $(@($fichas).Count)
Linhas reconhecidas: $($todasLinhas.Count)
Linhas de alta confiança: $alta
Linhas de média confiança: $media
Linhas pendentes: $pendentes
Cantos processados: $(@($letrasPorCanto).Count)
Cantos com letra aparentemente completa: 0
Cantos ainda incompletos/revisão necessária: $(@($letrasPorCanto | Where-Object revisaoNecessaria).Count)
Observação: Windows.Media.Ocr não expõe confiança lexical; a confiança registrada é heurística e nenhuma letra foi corrigida ou importada para o APK.
"@
$relatorio | Set-Content -Encoding UTF8 (Join-Path $PSScriptRoot 'relatorio_letras.txt')
Write-Output "OCR concluído: $($resultadoAcordes.Count) grupos de acordes e $(@($fichas).Count) páginas de letras."
