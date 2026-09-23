Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/FuncoesOcr.ps1"
$acordes = Get-Content -Raw -Encoding UTF8 (Join-Path $PSScriptRoot 'ocr_acordes_resultado.json') | ConvertFrom-Json
$letras = Get-Content -Raw -Encoding UTF8 (Join-Path $PSScriptRoot 'letras_candidatas.json') | ConvertFrom-Json
$porCanto = @{}; foreach ($canto in $letras) { $porCanto[[int]$canto.cantoId] = $canto }
$associacoes = 0
foreach ($grupo in @($acordes | Where-Object classificacao -eq 'CONFIRMADO')) {
    foreach ($ocorrencia in $grupo.ocorrencias) {
        $canto = $porCanto[[int]$ocorrencia.cantoId]
        if ($null -eq $canto) { continue }
        $pagina = @($canto.paginas | Where-Object pagina -eq $ocorrencia.pagina | Select-Object -First 1)
        if ($pagina.Count -eq 0) { continue }
        $linha = Associar-AcordeALinhaOcr $ocorrencia @($pagina[0].linhas)
        if ($null -eq $linha) { continue }
        $relativa = if ([double]$linha.largura -gt 0) { ([double]$ocorrencia.x - [double]$linha.x) / [double]$linha.largura } else { 0.0 }
        $linha.acordesRelacionados += [pscustomobject][ordered]@{ acorde=$grupo.acordeNormalizadoProposto; grupoId=$grupo.grupoId; confianca=$grupo.confianca; posicaoHorizontalRelativa=[math]::Round($relativa, 4) }
        $associacoes++
    }
}
$letras | ConvertTo-Json -Depth 10 | Set-Content -Encoding UTF8 (Join-Path $PSScriptRoot 'letras_candidatas.json')
$pendentes = @($letras | ForEach-Object { $_.paginas } | ForEach-Object { $_.linhas } | Where-Object revisaoNecessaria)
('window.dadosLetrasRevisao=' + ($pendentes | ConvertTo-Json -Depth 6 -Compress) + ';') | Set-Content -Encoding UTF8 (Join-Path $PSScriptRoot 'revisao/dados_letras_revisao.js')
Add-Content -Encoding UTF8 (Join-Path $PSScriptRoot 'relatorio_letras.txt') "Associações conservadoras acorde confirmado -> linha: $associacoes"
Write-Output "Associações geradas: $associacoes; linhas para revisão: $($pendentes.Count)."
