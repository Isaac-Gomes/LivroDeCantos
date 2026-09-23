Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/FuncoesOcr.ps1"
Add-Type -AssemblyName System.Drawing
Add-Type -Path (Join-Path $PSScriptRoot 'ImagemCifra.cs') -ReferencedAssemblies System.Drawing
function Assert-That { param([bool]$Condicao, [string]$Mensagem) if (-not $Condicao) { throw $Mensagem } }

$tmp = Join-Path ([IO.Path]::GetTempPath()) ('ocr_cifra_' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $tmp | Out-Null
try {
    $origem = Join-Path $tmp 'origem.png'; $acorde = Join-Path $tmp 'acorde.png'; $letra = Join-Path $tmp 'letra.png'
    $imagem = [System.Drawing.Bitmap]::new(4, 2)
    try { $imagem.SetPixel(0,0,[System.Drawing.Color]::Red); $imagem.SetPixel(1,0,[System.Drawing.Color]::Black); $imagem.Save($origem) } finally { $imagem.Dispose() }
    [Ressuscitou.Cifras.Tools.ImagemCifra]::PrepararAcorde($origem, $acorde, 4, 4)
    [Ressuscitou.Cifras.Tools.ImagemCifra]::PrepararLetra($origem, $letra)
    $a = [System.Drawing.Bitmap]::FromFile($acorde); $l = [System.Drawing.Bitmap]::FromFile($letra)
    try {
        Assert-That ($a.GetPixel(4,4).R -eq 0) 'Pré-processamento não isolou o pixel vermelho como preto.'
        Assert-That ($l.GetPixel(0,0).R -eq 255) 'Extração de letra não removeu a região vermelha.'
        Assert-That ($l.GetPixel(1,0).R -eq 0) 'Extração de letra removeu texto preto.'
    } finally { $a.Dispose(); $l.Dispose() }
    Assert-That ((Normalizar-AcordeOcr 'Re/Fa#').acorde -eq 'Re/Fa#') 'Acorde OCR válido não foi normalizado.'
    Assert-That ((Normalizar-AcordeOcr 'S1b').acorde -eq 'Sib') 'Hipótese óptica Sib não foi registrada.'
    Assert-That ($null -eq (Normalizar-AcordeOcr 'Título longo').acorde) 'Texto não musical foi aceito como acorde.'
    $confirmado = Classificar-ResultadoOcrAcorde @([pscustomobject]@{textoOCR='Sib'},[pscustomobject]@{textoOCR='Sib'})
    Assert-That ($confirmado.classificacao -eq 'CONFIRMADO') 'Acorde estável entre variantes não foi confirmado.'
    $ordenadas = @(Ordenar-LinhasOcr @([pscustomobject]@{x=20;y=40},[pscustomobject]@{x=5;y=10}))
    Assert-That ($ordenadas[0].y -eq 10) 'Linhas OCR não foram ordenadas verticalmente.'
    $ocorrencias = @([pscustomobject]@{hashVisual='h1'},[pscustomobject]@{hashVisual='h1'},[pscustomobject]@{hashVisual='h2'}) | Group-Object hashVisual
    Assert-That (($ocorrencias | Where-Object Name -eq 'h1').Count -eq 2) 'Rastreabilidade grupo -> ocorrências falhou.'
    $linhaAbaixo = Associar-AcordeALinhaOcr ([pscustomobject]@{x=.20;y=.10;largura=.03}) @([pscustomobject]@{x=.10;y=.12;largura=.40;texto='Linha cantada'})
    Assert-That ($linhaAbaixo.texto -eq 'Linha cantada') 'Acorde confirmado não foi associado à linha abaixo.'
    Write-Output 'Testes OCR do pipeline: 10 aprovados.'
} finally { if (Test-Path $tmp) { Remove-Item -LiteralPath $tmp -Recurse -Force } }
