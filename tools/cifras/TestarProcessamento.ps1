Set-StrictMode -Version Latest
. "$PSScriptRoot/FuncoesProcessamento.ps1"
Add-Type -AssemblyName System.Drawing
Add-Type -Path (Join-Path $PSScriptRoot 'ImagemCifra.cs') -ReferencedAssemblies System.Drawing
function Assert-That { param([bool]$Condicao, [string]$Mensagem) if (-not $Condicao) { throw $Mensagem } }

Assert-That (Testar-CoordenadasNormalizadas 0 0 1 1) 'Coordenada válida rejeitada.'
Assert-That (-not (Testar-CoordenadasNormalizadas 0.9 0 0.2 0.1)) 'Coordenada fora da página aceita.'
$partes = @(
    [pscustomobject]@{ cantoId=1; pagina=1; arquivo='x'; x=0.10; y=0.10; largura=0.02; altura=0.01; indiceOriginal=1 },
    [pscustomobject]@{ cantoId=1; pagina=1; arquivo='x'; x=0.125; y=0.10; largura=0.01; altura=0.01; indiceOriginal=2 },
    [pscustomobject]@{ cantoId=1; pagina=1; arquivo='x'; x=0.30; y=0.10; largura=0.02; altura=0.01; indiceOriginal=3 }
)
$agrupadas = @(Agrupar-RegioesCandidatas $partes)
Assert-That ($agrupadas.Count -eq 2) 'Agrupamento uniu acordes distantes ou separou componentes próximos.'
Assert-That ((Testar-FormatoConservadorDeAcorde 'Re/Fa#')) 'Acorde invertido válido rejeitado.'
Assert-That (-not (Testar-FormatoConservadorDeAcorde 'Título vermelho')) 'Texto não musical aceito.'
$img1 = Join-Path ([IO.Path]::GetTempPath()) 'hash_cifra_1.png'; $img2 = Join-Path ([IO.Path]::GetTempPath()) 'hash_cifra_2.png'
$bitmap = [System.Drawing.Bitmap]::new(20, 20)
try { $bitmap.SetPixel(5, 5, [System.Drawing.Color]::Red); $bitmap.Save($img1); $bitmap.Save($img2) } finally { $bitmap.Dispose() }
Assert-That ([Ressuscitou.Cifras.Tools.ImagemCifra]::HashVisualVermelho($img1) -eq [Ressuscitou.Cifras.Tools.ImagemCifra]::HashVisualVermelho($img2)) 'Deduplicação visual não é determinística.'
Remove-Item -LiteralPath $img1, $img2 -Force
$tmp = Join-Path ([IO.Path]::GetTempPath()) 'revisoes_cifra_teste.json'
'[{"decisao":"corrigir","acorde":"Sib"},{"decisao":"corrigir","acorde":"texto"}]' | Set-Content -Encoding UTF8 $tmp
Assert-That (@(Importar-RevisoesManuais $tmp).Count -eq 1) 'Importação de revisões não filtrou dado inválido.'
Remove-Item -LiteralPath $tmp -Force
$grupoCompleto = [pscustomobject]@{ hashVisual='abc' }
$decisoes = @{ abc = [pscustomobject]@{ decisao='confirmar'; acorde='Re-' } }
Assert-That (Pode-MarcarCantoCompleto @($grupoCompleto) $decisoes 'Re-') 'Canto completo válido não foi aceito.'
Assert-That (-not (Pode-MarcarCantoCompleto @($grupoCompleto) $decisoes '')) 'Canto sem tom original foi aceito como completo.'
Write-Output 'Testes do pipeline de cifras: 9 aprovados.'
