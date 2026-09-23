Set-StrictMode -Version Latest

function Testar-CoordenadasNormalizadas {
    param([double]$X, [double]$Y, [double]$Largura, [double]$Altura)
    return $X -ge 0 -and $Y -ge 0 -and $Largura -gt 0 -and $Altura -gt 0 -and
        $X -le 1 -and $Y -le 1 -and $Largura -le 1 -and $Altura -le 1 -and
        ($X + $Largura) -le 1.0001 -and ($Y + $Altura) -le 1.0001
}

function Agrupar-RegioesCandidatas {
    param([object[]]$Regioes)
    $resultado = [System.Collections.Generic.List[object]]::new()
    foreach ($pagina in @($Regioes | Group-Object cantoId, pagina)) {
        $ordenadas = @($pagina.Group | Sort-Object @{ Expression = { [double]$_.y } }, @{ Expression = { [double]$_.x } }, indiceOriginal)
        $grupo = $null
        foreach ($regiao in $ordenadas) {
            $x = [double]$regiao.x; $y = [double]$regiao.y
            $largura = [double]$regiao.largura; $altura = [double]$regiao.altura
            if ($null -eq $grupo) {
                $grupo = [ordered]@{ cantoId=$regiao.cantoId; pagina=$regiao.pagina; arquivo=$regiao.arquivo; x=$x; y=$y; largura=$largura; altura=$altura; regioesOriginais=@($regiao.indiceOriginal) }
                continue
            }
            $centroY = $y + $altura / 2
            $centroGrupo = [double]$grupo.y + [double]$grupo.altura / 2
            $direita = [double]$grupo.x + [double]$grupo.largura
            $razaoAltura = $altura / [math]::Max([double]$grupo.altura, 0.00001)
            # Limiar pequeno: une partes de uma palavra, não acordes vizinhos.
            $mesmaLinha = [math]::Abs($centroY - $centroGrupo) -le [math]::Max(0.004, 0.60 * [math]::Max($altura, [double]$grupo.altura))
            $proximo = $x - $direita -le 0.008
            $alturaCompativel = $razaoAltura -ge 0.50 -and $razaoAltura -le 2.00
            if ($mesmaLinha -and $proximo -and $alturaCompativel) {
                $fimX = [math]::Max($direita, $x + $largura); $fimY = [math]::Max([double]$grupo.y + [double]$grupo.altura, $y + $altura)
                $grupo.x = [math]::Min([double]$grupo.x, $x); $grupo.y = [math]::Min([double]$grupo.y, $y)
                $grupo.largura = $fimX - [double]$grupo.x; $grupo.altura = $fimY - [double]$grupo.y
                $grupo.regioesOriginais += $regiao.indiceOriginal
            } else {
                $resultado.Add([pscustomobject]$grupo)
                $grupo = [ordered]@{ cantoId=$regiao.cantoId; pagina=$regiao.pagina; arquivo=$regiao.arquivo; x=$x; y=$y; largura=$largura; altura=$altura; regioesOriginais=@($regiao.indiceOriginal) }
            }
        }
        if ($null -ne $grupo) { $resultado.Add([pscustomobject]$grupo) }
    }
    return @($resultado | Sort-Object cantoId, pagina, y, x)
}

function Testar-FormatoConservadorDeAcorde {
    param([string]$Texto)
    # Validador de apoio para decisões humanas/OCR: não substitui CifraTranspositor.
    return $Texto -match '^(Do|Re|Mi|Fa|Sol|La|Si|C|D|E|F|G|A|B)(#|b)?([+\-]|dim|°|º|sus[0-9]*|[0-9]+M?)?([0-9+\-#/()]*)?(\/(Do|Re|Mi|Fa|Sol|La|Si|C|D|E|F|G|A|B)(#|b)?)?$'
}

function Importar-RevisoesManuais {
    param([string]$Arquivo)
    if (-not (Test-Path -LiteralPath $Arquivo)) { return @() }
    $itens = Get-Content -Raw -Encoding UTF8 -LiteralPath $Arquivo | ConvertFrom-Json
    return @($itens | Where-Object {
        if ($_.decisao -notin @('confirmar','corrigir','nao_e_acorde','pular')) { return $false }
        if ($_.decisao -in @('confirmar','corrigir')) { return Testar-FormatoConservadorDeAcorde $_.acorde }
        return $true
    })
}

function Pode-MarcarCantoCompleto {
    param([object[]]$GruposDoCanto, [hashtable]$Decisoes, [string]$TomOriginal)
    if ([string]::IsNullOrWhiteSpace($TomOriginal) -or $GruposDoCanto.Count -eq 0) { return $false }
    foreach ($grupo in $GruposDoCanto) {
        $decisao = $Decisoes[$grupo.hashVisual]
        if ($null -eq $decisao -or $decisao.decisao -notin @('confirmar','corrigir') -or
            -not (Testar-FormatoConservadorDeAcorde $decisao.acorde)) { return $false }
    }
    return $true
}
