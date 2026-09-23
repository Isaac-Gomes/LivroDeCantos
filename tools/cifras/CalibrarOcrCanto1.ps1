param([string]$RaizProjeto = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/OcrWinRt.ps1"
Add-Type -AssemblyName System.Drawing
Add-Type -Path (Join-Path $PSScriptRoot 'ImagemCifra.cs') -ReferencedAssemblies System.Drawing
$saida = Join-Path $PSScriptRoot 'ocr_debug/calibracao_canto_1'
New-Item -ItemType Directory -Force -Path $saida | Out-Null
$mapeamentos = Get-Content -Raw -Encoding UTF8 (Join-Path $RaizProjeto 'app/src/main/assets/data/acordes_fichas.json') | ConvertFrom-Json
$mapeamento = foreach ($mapa in $mapeamentos) { if ($mapa.cantoId -eq 1) { $mapa } }
$ficha = Join-Path $RaizProjeto 'app/src/main/assets/fichas/PRE-CATECUMENATO/A_cabana_dos_pastores_01.png'
$amostras = [System.Collections.Generic.List[object]]::new()
$indice = 0
foreach ($item in $mapeamento.acordes) {
    $indice++
    $base = 'canto_001_{0:D2}_{1}' -f $indice, $item.acorde.Replace('/','_')
    $raw = Join-Path $saida ($base + '_raw.png')
    [Ressuscitou.Cifras.Tools.ImagemCifra]::RecortarNormalizado($ficha, $raw, $item.x, $item.y, $item.largura, $item.altura, 0.35)
    $variantes = [System.Collections.Generic.List[object]]::new()
    foreach ($escala in @(4, 6, 8)) {
        $preparada = Join-Path $saida ($base + "_x$escala.png")
        [Ressuscitou.Cifras.Tools.ImagemCifra]::PrepararAcorde($raw, $preparada, $escala, 24)
        $ocr = Invocar-OcrWinRt $preparada
        $variantes.Add([pscustomobject][ordered]@{ escala=$escala; arquivo=('ocr_debug/calibracao_canto_1/' + [IO.Path]::GetFileName($preparada)); textoOCR=$ocr.texto })
    }
    $amostras.Add([pscustomobject][ordered]@{ esperado=$item.acorde; raw=('ocr_debug/calibracao_canto_1/' + [IO.Path]::GetFileName($raw)); variantes=@($variantes) })
}
$amostras | ConvertTo-Json -Depth 6 | Set-Content -Encoding UTF8 (Join-Path $saida 'resultado_calibracao.json')
$acertos = 0
foreach ($amostra in $amostras) {
    if (@($amostra.variantes | Where-Object { ($_.textoOCR -replace '\s','') -eq $amostra.esperado }).Count -gt 0) { $acertos++ }
}
Write-Output "Calibração concluída: $acertos/$($amostras.Count) amostras tiveram OCR literal igual ao acorde esperado."
