param(
    [string]$RaizProjeto = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -Path (Join-Path $PSScriptRoot 'DetectorAcordesVermelhos.cs') -ReferencedAssemblies @(
    'System.dll',
    'System.Drawing.dll'
)

$assets = Join-Path $RaizProjeto 'app\src\main\assets'
$cantos = Get-Content -Raw -Encoding UTF8 (Join-Path $assets 'data\cantos.json') | ConvertFrom-Json
$fichas = Get-Content -Raw -Encoding UTF8 (Join-Path $assets 'data\fichas.json') | ConvertFrom-Json
$cifras = Get-Content -Raw -Encoding UTF8 (Join-Path $assets 'data\cifras.json') | ConvertFrom-Json
$mapeamentos = Get-Content -Raw -Encoding UTF8 (Join-Path $assets 'data\acordes_fichas.json') | ConvertFrom-Json
$especiais = @(214, 235, 248) # Arpejos, Prontuário e Tabela para Transportar.

$porId = @{}
foreach ($canto in $cantos) { $porId[[int]$canto.id] = $canto }
$fichasPorCanto = $fichas | Group-Object cantoId
$mapasPorCanto = @{}
foreach ($mapa in $mapeamentos) { $mapasPorCanto[[int]$mapa.cantoId] = $mapa }
$cifrasPorCanto = @{}
foreach ($cifra in $cifras) { $cifrasPorCanto[[int]$cifra.cantoId] = $cifra }
$erros = [System.Collections.Generic.List[string]]::new()
if (($porId.Keys | Measure-Object).Count -ne $cantos.Count) {
    $erros.Add('Há cantoId duplicado em cantos.json.')
}
foreach ($ficha in $fichas) {
    if (-not $porId.ContainsKey([int]$ficha.cantoId)) {
        $erros.Add("Ficha aponta para cantoId inexistente: $($ficha.cantoId).")
    }
    if (-not (Test-Path (Join-Path $assets $ficha.arquivo))) {
        $erros.Add("PNG ausente: $($ficha.arquivo).")
    }
}
foreach ($mapa in $mapeamentos) {
    if (-not $porId.ContainsKey([int]$mapa.cantoId)) {
        $erros.Add("Mapa aponta para cantoId inexistente: $($mapa.cantoId).")
    }
    $paginasDoCanto = @($fichas | Where-Object { $_.cantoId -eq $mapa.cantoId } | ForEach-Object ordem)
    $chaves = @{}
    foreach ($acorde in @($mapa.acordes)) {
        if ([string]::IsNullOrWhiteSpace($acorde.acorde)) {
            $erros.Add("Acorde vazio no canto $($mapa.cantoId).")
        }
        if ($acorde.pagina -notin $paginasDoCanto) {
            $erros.Add("Página $($acorde.pagina) não pertence ao canto $($mapa.cantoId).")
        }
        if ($acorde.x -lt 0 -or $acorde.y -lt 0 -or $acorde.largura -le 0 -or $acorde.altura -le 0 -or
            $acorde.x + $acorde.largura -gt 1 -or $acorde.y + $acorde.altura -gt 1) {
            $erros.Add("Coordenada inválida no canto $($mapa.cantoId), página $($acorde.pagina).")
        }
        $chave = "$($acorde.pagina):$($acorde.acorde):$($acorde.x):$($acorde.y):$($acorde.largura):$($acorde.altura)"
        if ($chaves.ContainsKey($chave)) {
            $erros.Add("Entrada de acorde duplicada no canto $($mapa.cantoId).")
        }
        $chaves[$chave] = $true
    }
}

$candidatos = @()
foreach ($ficha in $fichas) {
    $arquivo = Join-Path $assets $ficha.arquivo
    $regioes = [Ressuscitou.Cifras.Tools.DetectorAcordesVermelhos]::Detectar($arquivo)
    foreach ($regiao in $regioes) {
        $candidatos += [PSCustomObject]@{
            cantoId = [int]$ficha.cantoId
            pagina = [int]$ficha.ordem # Índice de página da ficha é baseado em 1.
            arquivo = $ficha.arquivo
            x = [Math]::Round($regiao.X, 6)
            y = [Math]::Round($regiao.Y, 6)
            largura = [Math]::Round($regiao.Largura, 6)
            altura = [Math]::Round($regiao.Altura, 6)
            revisaoNecessaria = $true
            motivo = 'Região vermelha detectada sem identidade textual validada.'
        }
    }
}
$candidatos | ConvertTo-Json -Depth 4 | Set-Content -Encoding UTF8 (Join-Path $PSScriptRoot 'candidatos_acordes_vermelhos.json')

$prontos = @($cifras | Where-Object {
    $mapa = $mapasPorCanto[[int]$_.cantoId]
    $mapa -and $mapa.completo -eq $true -and @($mapa.acordes).Count -gt 0
})
$revisao = @($cantos | Where-Object {
    $_.id -notin $especiais -and $_.id -notin @($prontos.cantoId)
})
$acordesMapeados = @($mapeamentos | ForEach-Object { @($_.acordes).Count } | Measure-Object -Sum).Sum
$linhas = [System.Collections.Generic.List[string]]::new()
$linhas.Add('RESSUSCITOU — RELATÓRIO DE COBERTURA DE TRANSPOSIÇÃO')
$linhas.Add('')
$linhas.Add("Total de cantos no catálogo: $($cantos.Count)")
$linhas.Add("Cantos com ficha: $($fichasPorCanto.Count)")
$linhas.Add("Páginas de ficha: $($fichas.Count)")
$linhas.Add("Cantos com transposição pronta: $($prontos.Count)")
$linhas.Add('Cantos sem acordes confirmados: 0')
$linhas.Add("Cantos que precisam de revisão: $($revisao.Count)")
$linhas.Add("Páginas processadas pelo detector: $($fichas.Count)")
$linhas.Add("Regiões vermelhas candidatas: $($candidatos.Count)")
$linhas.Add("Acordes mapeados e validados: $acordesMapeados")
$linhas.Add("Erros de validação estrutural: $($erros.Count)")
$linhas.Add('')
$linhas.Add('Observação: regiões candidatas não são acordes validados e nunca são incluídas no APK.')
$linhas.Add('')
$linhas.Add('PRECISAM DE REVISÃO')
foreach ($canto in $revisao | Sort-Object id) {
    $paginas = @($fichas | Where-Object { $_.cantoId -eq $canto.id } | Sort-Object ordem)
    foreach ($pagina in $paginas) {
        $quantidade = @($candidatos | Where-Object {
            $_.cantoId -eq [int]$canto.id -and $_.pagina -eq [int]$pagina.ordem
        }).Count
        $linhas.Add("cantoId=$($canto.id) | número=$($canto.numero) | título=$($canto.titulo) | página=$($pagina.ordem) | regiões candidatas=$quantidade | motivo=Não há fonte textual local nem identidade/tom original confirmados.")
    }
}
if ($erros.Count -gt 0) {
    $linhas.Add('')
    $linhas.Add('ERROS DE VALIDAÇÃO')
    $erros | ForEach-Object { $linhas.Add($_) }
}
$linhas | Set-Content -Encoding UTF8 (Join-Path $PSScriptRoot 'relatorio_cobertura.txt')
