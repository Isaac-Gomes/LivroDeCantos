Set-StrictMode -Version Latest
. "$PSScriptRoot/FuncoesProcessamento.ps1"

function Normalizar-AcordeOcr {
    param([AllowNull()][string]$Texto)
    if ([string]::IsNullOrWhiteSpace($Texto)) { return [pscustomobject]@{ acorde=$null; ambiguidade=$false } }
    $limpo = ($Texto -replace '\s','').Replace('º','°')
    $candidatos = [System.Collections.Generic.List[object]]::new()
    $candidatos.Add([pscustomobject]@{ valor=$limpo; ambiguidade=$false })
    # Correções ópticas pontuais são apenas hipóteses: nunca bastam para CONFIRMADO.
    if ($limpo -match '^[Ss][1lI]b') { $candidatos.Add([pscustomobject]@{ valor=('Sib' + $limpo.Substring(3)); ambiguidade=$true }) }
    foreach ($candidato in $candidatos) {
        $valor = $candidato.valor
        if ($valor -match '^(do|re|mi|fa|sol|la|si)(.*)$') { $valor = $matches[1].Substring(0,1).ToUpperInvariant() + $matches[1].Substring(1).ToLowerInvariant() + $matches[2] }
        elseif ($valor -match '^([cdefgab])(.*)$') { $valor = $matches[1].ToUpperInvariant() + $matches[2] }
        if (Testar-FormatoConservadorDeAcorde $valor) { return [pscustomobject]@{ acorde=$valor; ambiguidade=[bool]$candidato.ambiguidade } }
    }
    return [pscustomobject]@{ acorde=$null; ambiguidade=$false }
}

function Classificar-ResultadoOcrAcorde {
    param([object[]]$Variantes)
    $normalizados = foreach ($variante in $Variantes) {
        $normalizado = Normalizar-AcordeOcr $variante.textoOCR
        if ($null -ne $normalizado.acorde) { [pscustomobject]@{ acorde=$normalizado.acorde; ambiguidade=$normalizado.ambiguidade } }
    }
    $melhor = @($normalizados | Group-Object acorde | Sort-Object @{ Expression = 'Count'; Descending = $true }, Name | Select-Object -First 1)
    if ($melhor.Count -eq 1) {
        $amostras = @($melhor[0].Group)
        if ($amostras.Count -ge 2 -and -not @($amostras | Where-Object ambiguidade).Count) {
            return [pscustomobject]@{ acorde=$melhor[0].Name; classificacao='CONFIRMADO'; confianca=0.95 }
        }
        return [pscustomobject]@{ acorde=$melhor[0].Name; classificacao='PROVAVEL'; confianca=0.65 }
    }
    $brutos = @($Variantes | ForEach-Object textoOCR | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    if ($brutos.Count -ge 2 -and @($brutos | Where-Object { $_.Length -ge 8 }).Count -ge 2) {
        return [pscustomobject]@{ acorde=$null; classificacao='NAO_ACORDE'; confianca=0.80 }
    }
    return [pscustomobject]@{ acorde=$null; classificacao='DUVIDOSO'; confianca=0.0 }
}

function Ordenar-LinhasOcr {
    param([object[]]$Linhas)
    return @($Linhas | Sort-Object @{ Expression = { [int]$_.y } }, @{ Expression = { [int]$_.x } })
}

function Obter-ConfiancaLinhaOcr {
    param([string]$Texto)
    $letras = @([regex]::Matches($Texto, '[\p{L}]')).Count
    if ($letras -ge 18) { return 0.85 }
    if ($letras -ge 6) { return 0.70 }
    return 0.45
}

function Associar-AcordeALinhaOcr {
    param([object]$Ocorrencia, [object[]]$Linhas)
    $candidatas = @($Linhas | Where-Object {
        $delta = [double]$_.y - [double]$Ocorrencia.y
        $dentroDaLargura = [double]$Ocorrencia.x -le ([double]$_.x + [double]$_.largura + 0.05) -and
            ([double]$Ocorrencia.x + [double]$Ocorrencia.largura) -ge ([double]$_.x - 0.05)
        $delta -ge 0 -and $delta -le 0.08 -and $dentroDaLargura -and -not [string]::IsNullOrWhiteSpace($_.texto)
    } | Sort-Object y, x)
    if ($candidatas.Count -eq 0) { return $null }
    return $candidatas[0]
}
