param(
    [string]$RaizProjeto = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/FuncoesProcessamento.ps1"

$destino = Join-Path $PSScriptRoot 'crops'
$destinoAgrupado = Join-Path $destino 'agrupados'
New-Item -ItemType Directory -Force -Path $destino, $destinoAgrupado | Out-Null
Add-Type -AssemblyName System.Drawing
Add-Type -Path (Join-Path $PSScriptRoot 'ImagemCifra.cs') -ReferencedAssemblies System.Drawing

$cantos = Get-Content -Raw -Encoding UTF8 (Join-Path $RaizProjeto 'app/src/main/assets/data/cantos.json') | ConvertFrom-Json
$candidatos = Get-Content -Raw -Encoding UTF8 (Join-Path $PSScriptRoot 'candidatos_acordes_vermelhos.json') | ConvertFrom-Json
$porId = @{}
foreach ($canto in $cantos) { $porId[[int]$canto.id] = $canto }

$manifest = [System.Collections.Generic.List[object]]::new()
$regioes = [System.Collections.Generic.List[object]]::new()
$ordemPorPagina = @{}
$indiceOriginal = 0
foreach ($candidato in @($candidatos | Sort-Object cantoId, pagina, y, x, largura, altura)) {
    $indiceOriginal++
    $chave = "$($candidato.cantoId):$($candidato.pagina)"
    if (-not $ordemPorPagina.ContainsKey($chave)) { $ordemPorPagina[$chave] = 0 }
    $ordemPorPagina[$chave]++
    $ocorrencia = $ordemPorPagina[$chave]
    $canto = $porId[[int]$candidato.cantoId]
    if ($null -eq $canto) { throw "cantoId inválido no candidato: $($candidato.cantoId)" }
    if (-not (Testar-CoordenadasNormalizadas $candidato.x $candidato.y $candidato.largura $candidato.altura)) {
        throw "Coordenada inválida no candidato $indiceOriginal"
    }
    $arquivoOrigem = Join-Path $RaizProjeto ('app/src/main/assets/' + $candidato.arquivo)
    if (-not (Test-Path -LiteralPath $arquivoOrigem)) { throw "Ficha ausente: $arquivoOrigem" }
    $nomeCrop = 'canto_{0:D3}_p{1:D2}_{2:D4}.png' -f [int]$candidato.cantoId, [int]$candidato.pagina, $ocorrencia
    $arquivoCrop = Join-Path $destino $nomeCrop
    $bitmap = [System.Drawing.Bitmap]::FromFile($arquivoOrigem)
    try {
        $x = [math]::Floor([double]$candidato.x * $bitmap.Width)
        $y = [math]::Floor([double]$candidato.y * $bitmap.Height)
        $largura = [math]::Max(1, [math]::Ceiling([double]$candidato.largura * $bitmap.Width))
        $altura = [math]::Max(1, [math]::Ceiling([double]$candidato.altura * $bitmap.Height))
        $margem = [math]::Max(6, [math]::Min(24, [math]::Ceiling([math]::Max($largura, $altura) * 0.25)))
        $cropX = [math]::Max(0, $x - $margem); $cropY = [math]::Max(0, $y - $margem)
        $cropDireita = [math]::Min($bitmap.Width, $x + $largura + $margem); $cropBaixo = [math]::Min($bitmap.Height, $y + $altura + $margem)
        [Ressuscitou.Cifras.Tools.ImagemCifra]::Recortar($arquivoOrigem, $arquivoCrop, $cropX, $cropY, $cropDireita - $cropX, $cropBaixo - $cropY)
    } finally { $bitmap.Dispose() }
    $registro = [pscustomobject][ordered]@{
        cantoId=[int]$candidato.cantoId; numero=[string]$canto.numero; titulo=[string]$canto.titulo; pagina=[int]$candidato.pagina
        arquivoFicha=[string]$candidato.arquivo; arquivoCrop=('crops/' + $nomeCrop); x=[double]$candidato.x; y=[double]$candidato.y
        largura=[double]$candidato.largura; altura=[double]$candidato.altura; indiceOcorrencia=$ocorrencia; indiceOriginal=$indiceOriginal
    }
    $manifest.Add($registro)
    $regioes.Add([pscustomobject][ordered]@{ cantoId=$registro.cantoId; pagina=$registro.pagina; arquivo=$registro.arquivoFicha; x=$registro.x; y=$registro.y; largura=$registro.largura; altura=$registro.altura; indiceOriginal=$indiceOriginal })
}
$manifest | ConvertTo-Json -Depth 5 | Set-Content -Encoding UTF8 (Join-Path $PSScriptRoot 'crops_manifest.json')

$agrupadasBase = @(Agrupar-RegioesCandidatas @($regioes))
$agrupadas = [System.Collections.Generic.List[object]]::new()
$ordemAgrupada = @{}
foreach ($grupo in $agrupadasBase) {
    $chave = "$($grupo.cantoId):$($grupo.pagina)"
    if (-not $ordemAgrupada.ContainsKey($chave)) { $ordemAgrupada[$chave] = 0 }; $ordemAgrupada[$chave]++
    $ocorrencia = $ordemAgrupada[$chave]; $canto = $porId[[int]$grupo.cantoId]
    $nomeCrop = 'canto_{0:D3}_p{1:D2}_{2:D4}.png' -f [int]$grupo.cantoId, [int]$grupo.pagina, $ocorrencia
    $arquivoOrigem = Join-Path $RaizProjeto ('app/src/main/assets/' + $grupo.arquivo)
    $arquivoCrop = Join-Path $destinoAgrupado $nomeCrop
    $bitmap = [System.Drawing.Bitmap]::FromFile($arquivoOrigem)
    try {
        $x = [math]::Floor([double]$grupo.x * $bitmap.Width); $y = [math]::Floor([double]$grupo.y * $bitmap.Height)
        $largura = [math]::Max(1, [math]::Ceiling([double]$grupo.largura * $bitmap.Width)); $altura = [math]::Max(1, [math]::Ceiling([double]$grupo.altura * $bitmap.Height))
        $margem = [math]::Max(6, [math]::Min(24, [math]::Ceiling([math]::Max($largura, $altura) * 0.25)))
        $cropX = [math]::Max(0, $x - $margem); $cropY = [math]::Max(0, $y - $margem)
        $cropDireita = [math]::Min($bitmap.Width, $x + $largura + $margem); $cropBaixo = [math]::Min($bitmap.Height, $y + $altura + $margem)
        [Ressuscitou.Cifras.Tools.ImagemCifra]::Recortar($arquivoOrigem, $arquivoCrop, $cropX, $cropY, $cropDireita - $cropX, $cropBaixo - $cropY)
    } finally { $bitmap.Dispose() }
    $hash = [Ressuscitou.Cifras.Tools.ImagemCifra]::HashVisualVermelho($arquivoCrop)
    $agrupadas.Add([pscustomobject][ordered]@{
        cantoId=[int]$grupo.cantoId; numero=[string]$canto.numero; titulo=[string]$canto.titulo; pagina=[int]$grupo.pagina; arquivoFicha=[string]$grupo.arquivo
        arquivoCrop=('crops/agrupados/' + $nomeCrop); x=[double]$grupo.x; y=[double]$grupo.y; largura=[double]$grupo.largura; altura=[double]$grupo.altura
        indiceOcorrencia=$ocorrencia; regioesOriginais=@($grupo.regioesOriginais); hashVisual=$hash; classificacao='DUVIDOSO'; textoReconhecido=$null; confianca=0
        motivo='Não há OCR local disponível; não foi inferido acorde a partir da imagem.'
    })
}
$agrupadas | ConvertTo-Json -Depth 6 | Set-Content -Encoding UTF8 (Join-Path $PSScriptRoot 'candidatos_acordes_agrupados.json')

$gruposVisuais = foreach ($grupoHash in @($agrupadas | Group-Object hashVisual | Sort-Object Name)) {
    $itens = @($grupoHash.Group)
    [pscustomobject][ordered]@{ hashVisual=$grupoHash.Name; ocorrencias=$itens.Count; textoReconhecido=$null; confianca=0; classificacao='DUVIDOSO'; exemplos=@($itens | Select-Object -First 5 cantoId, pagina, indiceOcorrencia, arquivoCrop) }
}
$gruposVisuais | ConvertTo-Json -Depth 6 | Set-Content -Encoding UTF8 (Join-Path $PSScriptRoot 'grupos_visuais_acordes.json')

$agrupadas | Select-Object cantoId, numero, titulo, pagina, indiceOcorrencia, arquivoCrop, classificacao, textoReconhecido, confianca, motivo | ConvertTo-Json -Depth 5 | Set-Content -Encoding UTF8 (Join-Path $PSScriptRoot 'reconhecimento_acordes.json')
('window.dadosRevisao=' + ($agrupadas | ConvertTo-Json -Depth 6 -Compress) + ';') | Set-Content -Encoding UTF8 (Join-Path $PSScriptRoot 'revisao/dados_revisao.js')
Set-Content -Encoding UTF8 (Join-Path $PSScriptRoot 'revisao/revisoes_manuais.json') '[]'

$linhaRelatorio = @"

PROCESSAMENTO OFFLINE DE RECONHECIMENTO
Candidatos vermelhos originais: $($manifest.Count)
Agrupamentos formados: $($agrupadas.Count)
Regiões reconhecidas automaticamente: 0
Acordes confirmados automaticamente: 0
Prováveis: 0
Duvidosos: $($agrupadas.Count)
Inválidos automaticamente: 0
Grupos visuais (hash de máscara vermelha): $(@($gruposVisuais).Count)
Observação: nenhum OCR local utilizável foi encontrado. A classificação é conservadora; todos os grupos sem decisão humana permanecem fora dos dados do app.
"@
$linhaRelatorio | Set-Content -Encoding UTF8 (Join-Path $PSScriptRoot 'relatorio_reconhecimento.txt')
Add-Content -Encoding UTF8 (Join-Path $PSScriptRoot 'relatorio_cobertura.txt') $linhaRelatorio
Write-Output "Dataset criado: $($manifest.Count) crops originais; $($agrupadas.Count) agrupados; $(@($gruposVisuais).Count) grupos visuais."
