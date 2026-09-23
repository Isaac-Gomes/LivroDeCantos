param(
    [string]$Arquivo = (Join-Path $PSScriptRoot 'crops/agrupados/canto_001_p01_0002.png'),
    [string]$Log = (Join-Path $PSScriptRoot 'ocr_debug/teste_winrt.log')
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
New-Item -ItemType Directory -Force -Path (Split-Path $Log) | Out-Null
Set-Content -Encoding UTF8 -Path $Log 'Início do teste WinRT OCR.'
function Registrar([string]$Mensagem) { Add-Content -Encoding UTF8 -Path $Log $Mensagem }

Add-Type -AssemblyName System.Runtime.WindowsRuntime
$storageFile = [type]'Windows.Storage.StorageFile,Windows.Storage,ContentType=WindowsRuntime'
$fileAccess = [type]'Windows.Storage.FileAccessMode,Windows.Storage,ContentType=WindowsRuntime'
$streamType = [type]'Windows.Storage.Streams.IRandomAccessStream,Windows.Storage.Streams,ContentType=WindowsRuntime'
$decoderType = [type]'Windows.Graphics.Imaging.BitmapDecoder,Windows.Graphics.Imaging,ContentType=WindowsRuntime'
$softwareBitmapType = [type]'Windows.Graphics.Imaging.SoftwareBitmap,Windows.Graphics.Imaging,ContentType=WindowsRuntime'
$ocrResultType = [type]'Windows.Media.Ocr.OcrResult,Windows.Media.Ocr,ContentType=WindowsRuntime'
$ocrEngineType = [type]'Windows.Media.Ocr.OcrEngine,Windows.Media.Ocr,ContentType=WindowsRuntime'
$asTask = [System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object {
    $_.Name -eq 'AsTask' -and $_.IsGenericMethodDefinition -and $_.GetGenericArguments().Count -eq 1 -and
    $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.IsGenericType -and
    $_.GetParameters()[0].ParameterType.GetGenericTypeDefinition().FullName -eq 'Windows.Foundation.IAsyncOperation`1'
} | Select-Object -First 1
function Esperar($Operacao, [type]$Tipo) {
    $task = $asTask.MakeGenericMethod($Tipo).Invoke($null, @(,$Operacao))
    return $task.GetAwaiter().GetResult()
}

try {
    $arquivoResolvido = (Resolve-Path $Arquivo).Path
    Registrar "Abrindo: $arquivoResolvido"
    $arquivoStorage = Esperar ($storageFile::GetFileFromPathAsync($arquivoResolvido)) $storageFile
    Registrar 'StorageFile obtido.'
    $stream = Esperar ($arquivoStorage.OpenAsync($fileAccess::Read)) $streamType
    Registrar 'Stream obtido.'
    $decoder = Esperar ($decoderType::CreateAsync($stream)) $decoderType
    Registrar 'Decoder obtido.'
    $bitmap = Esperar ($decoder.GetSoftwareBitmapAsync()) $softwareBitmapType
    Registrar 'SoftwareBitmap obtido.'
    $engine = $ocrEngineType::TryCreateFromUserProfileLanguages()
    if ($null -eq $engine) { throw 'OcrEngine não foi criado para o idioma do perfil.' }
    Registrar 'OcrEngine criado.'
    $resultado = Esperar ($engine.RecognizeAsync($bitmap)) $ocrResultType
    Registrar "RESULTADO: $($resultado.Text)"
    $stream.Dispose()
    Write-Output $resultado.Text
} catch {
    Registrar "FALHA: $($_.Exception.GetType().FullName): $($_.Exception.Message)"
    throw
}
