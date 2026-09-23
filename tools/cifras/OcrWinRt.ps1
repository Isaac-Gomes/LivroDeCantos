Set-StrictMode -Version Latest

function Inicializar-OcrWinRt {
    if ((Get-Variable -Name ocrWinRtInicializado -Scope Script -ErrorAction SilentlyContinue) -and $script:ocrWinRtInicializado) { return }
    Add-Type -AssemblyName System.Runtime.WindowsRuntime
    $script:storageFileType = [type]'Windows.Storage.StorageFile,Windows.Storage,ContentType=WindowsRuntime'
    $script:fileAccessType = [type]'Windows.Storage.FileAccessMode,Windows.Storage,ContentType=WindowsRuntime'
    $script:streamType = [type]'Windows.Storage.Streams.IRandomAccessStream,Windows.Storage.Streams,ContentType=WindowsRuntime'
    $script:decoderType = [type]'Windows.Graphics.Imaging.BitmapDecoder,Windows.Graphics.Imaging,ContentType=WindowsRuntime'
    $script:softwareBitmapType = [type]'Windows.Graphics.Imaging.SoftwareBitmap,Windows.Graphics.Imaging,ContentType=WindowsRuntime'
    $script:ocrResultType = [type]'Windows.Media.Ocr.OcrResult,Windows.Media.Ocr,ContentType=WindowsRuntime'
    $script:ocrEngineType = [type]'Windows.Media.Ocr.OcrEngine,Windows.Media.Ocr,ContentType=WindowsRuntime'
    $script:asTask = [System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object {
        $_.Name -eq 'AsTask' -and $_.IsGenericMethodDefinition -and $_.GetGenericArguments().Count -eq 1 -and
        $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.IsGenericType -and
        $_.GetParameters()[0].ParameterType.GetGenericTypeDefinition().FullName -eq 'Windows.Foundation.IAsyncOperation`1'
    } | Select-Object -First 1
    $script:ocrEngine = $script:ocrEngineType::TryCreateFromUserProfileLanguages()
    if ($null -eq $script:ocrEngine) { throw 'Windows.Media.Ocr não possui reconhecedor para o idioma do perfil.' }
    $script:ocrWinRtInicializado = $true
}

function Esperar-OcrWinRt($Operacao, [type]$Tipo) {
    $task = $script:asTask.MakeGenericMethod($Tipo).Invoke($null, @(,$Operacao))
    return $task.GetAwaiter().GetResult()
}

function Invocar-OcrWinRt {
    param([Parameter(Mandatory)][string]$Arquivo)
    Inicializar-OcrWinRt
    $arquivoResolvido = (Resolve-Path -LiteralPath $Arquivo).Path
    $arquivoStorage = Esperar-OcrWinRt ($script:storageFileType::GetFileFromPathAsync($arquivoResolvido)) $script:storageFileType
    $stream = Esperar-OcrWinRt ($arquivoStorage.OpenAsync($script:fileAccessType::Read)) $script:streamType
    try {
        $decoder = Esperar-OcrWinRt ($script:decoderType::CreateAsync($stream)) $script:decoderType
        $bitmap = Esperar-OcrWinRt ($decoder.GetSoftwareBitmapAsync()) $script:softwareBitmapType
        $resultado = Esperar-OcrWinRt ($script:ocrEngine.RecognizeAsync($bitmap)) $script:ocrResultType
        $linhas = foreach ($linha in $resultado.Lines) {
            $palavras = @($linha.Words)
            $retangulos = @($palavras | ForEach-Object { $_.BoundingRect })
            $minX = if ($retangulos.Count) { @($retangulos | ForEach-Object X | Measure-Object -Minimum).Minimum } else { 0 }
            $minY = if ($retangulos.Count) { @($retangulos | ForEach-Object Y | Measure-Object -Minimum).Minimum } else { 0 }
            $maxX = if ($retangulos.Count) { @($retangulos | ForEach-Object { $_.X + $_.Width } | Measure-Object -Maximum).Maximum } else { 0 }
            $maxY = if ($retangulos.Count) { @($retangulos | ForEach-Object { $_.Y + $_.Height } | Measure-Object -Maximum).Maximum } else { 0 }
            [pscustomobject][ordered]@{
                texto = [string]$linha.Text
                x = [int]$minX; y = [int]$minY
                largura = [int]($maxX - $minX); altura = [int]($maxY - $minY)
            }
        }
        return [pscustomobject][ordered]@{ texto=[string]$resultado.Text; linhas=@($linhas) }
    } finally { $stream.Dispose() }
}
