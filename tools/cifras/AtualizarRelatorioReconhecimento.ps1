param([string]$Diretorio = $PSScriptRoot)
Set-StrictMode -Version Latest
$agrupadas = Get-Content -Raw -Encoding UTF8 (Join-Path $Diretorio 'candidatos_acordes_agrupados.json') | ConvertFrom-Json
$gruposVisuais = Get-Content -Raw -Encoding UTF8 (Join-Path $Diretorio 'grupos_visuais_acordes.json') | ConvertFrom-Json
$coberturaExistente = @($agrupadas | Where-Object cantoId -eq 1)
foreach ($item in $coberturaExistente) {
    # O piloto já possui posições estruturadas validadas; os candidatos brutos
    # não são uma segunda fonte de verdade e não devem voltar à fila humana.
    $item.classificacao = 'COBERTURA_EXISTENTE'
    $item.motivo = 'Canto piloto já possui mapeamento estruturado completo e validado.'
}
$materiaisReferencia = @($agrupadas | Where-Object cantoId -in @(214, 235, 248))
foreach ($item in $materiaisReferencia) {
    $item.classificacao = 'MATERIAL_REFERENCIA'
    $item.motivo = 'Página utilitária/de referência; não participa de transposição de cantos.'
}
$agrupadas | ConvertTo-Json -Depth 6 | Set-Content -Encoding UTF8 (Join-Path $Diretorio 'candidatos_acordes_agrupados.json')
$emRevisao = @($agrupadas | Where-Object classificacao -in @('DUVIDOSO','PROVAVEL'))
$reconhecimento = $agrupadas | Select-Object cantoId, numero, titulo, pagina, indiceOcorrencia, arquivoCrop, classificacao, textoReconhecido, confianca, motivo
$reconhecimento | ConvertTo-Json -Depth 5 | Set-Content -Encoding UTF8 (Join-Path $Diretorio 'reconhecimento_acordes.json')
('window.dadosRevisao=' + ($emRevisao | ConvertTo-Json -Depth 6 -Compress) + ';') | Set-Content -Encoding UTF8 (Join-Path $Diretorio 'revisao/dados_revisao.js')
$confirmados = @($agrupadas | Where-Object classificacao -eq 'CONFIRMADO').Count
$provaveis = @($agrupadas | Where-Object classificacao -eq 'PROVAVEL').Count
$duvidosos = @($agrupadas | Where-Object classificacao -eq 'DUVIDOSO').Count
$invalidos = @($agrupadas | Where-Object classificacao -eq 'INVALIDO').Count
$linhas = [System.Collections.Generic.List[string]]::new()
$linhas.Add('PROCESSAMENTO OFFLINE DE RECONHECIMENTO')
$linhas.Add("Candidatos vermelhos originais: 4.387")
$linhas.Add("Agrupamentos formados: $(@($agrupadas).Count)")
$linhas.Add("Regiões reconhecidas automaticamente: $($confirmados + $provaveis)")
$linhas.Add("Acordes confirmados automaticamente: $confirmados")
$linhas.Add("Prováveis: $provaveis")
$linhas.Add("Duvidosos: $duvidosos")
$linhas.Add("Inválidos automaticamente: $invalidos")
$linhas.Add("Grupos visuais (hash de máscara vermelha): $(@($gruposVisuais).Count)")
$linhas.Add("Candidatos excluídos da revisão por cobertura estruturada existente (canto 1): $($coberturaExistente.Count)")
$linhas.Add("Candidatos de materiais de referência fora da fila: $($materiaisReferencia.Count)")
$linhas.Add('Cantos completamente resolvidos nesta etapa: 0')
$linhas.Add('Cantos parcialmente reconhecidos nesta etapa: 0')
$linhas.Add('Cantos sem reconhecimento automático (em revisão): 235')
$linhas.Add('OCR local: não encontrado. Nenhum texto foi inferido ou adicionado ao aplicativo.')
$linhas.Add('')
$linhas.Add('PRECISAM DE REVISÃO')
foreach ($item in @($agrupadas | Where-Object classificacao -in @('DUVIDOSO','PROVAVEL') | Sort-Object cantoId, pagina, indiceOcorrencia)) {
    $possibilidades = if ($null -eq $item.textoReconhecido) { 'nenhuma' } else { [string]$item.textoReconhecido }
    $linhas.Add(('cantoId={0}; número={1}; título={2}; página={3}; crop={4}; possibilidades={5}; confiança={6}; motivo={7}' -f $item.cantoId, $item.numero, $item.titulo, $item.pagina, $item.arquivoCrop, $possibilidades, $item.confianca, $item.motivo))
}
$linhas | Set-Content -Encoding UTF8 (Join-Path $Diretorio 'relatorio_reconhecimento.txt')
$coberturaArquivo = Join-Path $Diretorio 'relatorio_cobertura.txt'
$cobertura = Get-Content -Raw -Encoding UTF8 $coberturaArquivo
$inicio = $cobertura.IndexOf('PROCESSAMENTO OFFLINE DE RECONHECIMENTO')
if ($inicio -ge 0) { $cobertura = $cobertura.Substring(0, $inicio).TrimEnd() + [Environment]::NewLine + [Environment]::NewLine }
$resumoCobertura = @"
PROCESSAMENTO OFFLINE DE RECONHECIMENTO
Candidatos vermelhos originais: 4387
Agrupamentos formados: $(@($agrupadas).Count)
Candidatos em revisão humana: $($emRevisao.Count)
Grupos visuais para revisão: $(@($emRevisao | Select-Object -ExpandProperty hashVisual -Unique).Count)
Cobertura estruturada existente (canto 1, fora da fila): $($coberturaExistente.Count)
Materiais de referência fora da fila: $($materiaisReferencia.Count)
Cantos com transposição adicional completa nesta etapa: 0
Cantos ainda em revisão: 235
Nenhum acorde adicional foi adicionado ao aplicativo sem revisão e validação.
"@
($cobertura + $resumoCobertura) | Set-Content -Encoding UTF8 $coberturaArquivo
Write-Output "Relatório atualizado com $duvidosos itens para revisão."
