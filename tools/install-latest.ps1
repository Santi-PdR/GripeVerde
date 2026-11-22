$ErrorActionPreference = 'Stop'
$mods = 'D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods'
$repo = 'Santi-PdR/GripeVerde'
$api = "https://api.github.com/repos/$repo/releases/latest"
$temp = Join-Path $mods ('.gripeverde-download-' + [guid]::NewGuid().ToString('N') + '.tmp')
$legacyBackupDir = Join-Path (Split-Path -Parent $mods) 'gripeverde-jar-backups'
$token = $null

function Get-GitHubToken {
    if ($env:GITHUB_TOKEN) { return ([string]$env:GITHUB_TOKEN).Trim() }

    if (Get-Command gh -ErrorAction SilentlyContinue) {
        $candidate = (& gh auth token 2>$null | Select-Object -First 1)
        if ($candidate) { return ([string]$candidate).Trim() }
    }

    if (Get-Command git -ErrorAction SilentlyContinue) {
        $payload = "protocol=https`nhost=github.com`n`n"
        $credentials = @($payload | & git credential fill 2>$null)
        $passwordLine = $credentials |
            Where-Object { $_ -like 'password=*' } |
            Select-Object -First 1
        if ($passwordLine) { return ([string]$passwordLine -replace '^password=', '').Trim() }
    }

    return $null
}

try {
    if (-not (Test-Path -LiteralPath $mods -PathType Container)) {
        throw "No existe la carpeta de mods: $mods"
    }

    # Windows PowerShell 5.1 puede usar TLS antiguo por defecto; GitHub requiere TLS 1.2.
    if ($PSVersionTable.PSVersion.Major -lt 6) {
        [System.Net.ServicePointManager]::SecurityProtocol =
            [System.Net.ServicePointManager]::SecurityProtocol -bor
            [System.Net.SecurityProtocolType]::Tls12
    }

    # Los releases son públicos: consulta primero sin credenciales. Solo recurre a
    # una sesión ya existente de gh/git si el API responde como para un repo privado.
    $headers = @{ Accept = 'application/vnd.github+json' }
    try {
        $release = Invoke-RestMethod -Uri $api -Headers $headers -UseBasicParsing -ErrorAction Stop
    }
    catch {
        $token = Get-GitHubToken
        if (-not $token) {
            throw "No pude leer el último release ($($_.Exception.Message)). Si el repositorio es privado, iniciá sesión con gh o definí GITHUB_TOKEN."
        }
        $headers.Authorization = "Bearer $token"
        $release = Invoke-RestMethod -Uri $api -Headers $headers -UseBasicParsing -ErrorAction Stop
    }

    $asset = @($release.assets) |
        Where-Object { $_.name -match '^gripeverde-[0-9]+\.[0-9]+\.[0-9]+\.jar$' } |
        Select-Object -First 1
    if (-not $asset) { throw "El release $($release.tag_name) no tiene un JAR versionado de GripeVerde." }

    $nameMatch = [regex]::Match([string]$asset.name, '^gripeverde-(?<version>[0-9]+\.[0-9]+\.[0-9]+)\.jar$')
    $version = $nameMatch.Groups['version'].Value
    if (-not $nameMatch.Success -or $release.tag_name -ne "v$version") {
        throw "El nombre del JAR y el tag del release no coinciden; se canceló la instalación."
    }

    $dest = Join-Path $mods $asset.name
    $expectedSize = [int64]$asset.size
    $expectedHash = ([string]$asset.digest) -replace '^sha256:', ''
    if ($expectedSize -le 0 -or $expectedHash -notmatch '^[0-9a-fA-F]{64}$') {
        throw 'GitHub no publicó un tamaño o SHA-256 válido para el asset; no puedo verificar la descarga.'
    }

    # Intenta el enlace público directo primero. Si falla, prueba el endpoint de
    # assets sin credenciales y recién después reutiliza una sesión existente.
    $downloaded = $false
    try {
        Invoke-WebRequest -Uri $asset.browser_download_url -OutFile $temp -UseBasicParsing -ErrorAction Stop
        $downloaded = $true
    }
    catch {
        # Continúa con el endpoint de API; no se toca ningún JAR instalado.
    }

    if (-not $downloaded) {
        $assetUrl = "https://api.github.com/repos/$repo/releases/assets/$($asset.id)"
        $assetHeaders = @{ Accept = 'application/octet-stream' }
        try {
            Invoke-WebRequest -Uri $assetUrl -Headers $assetHeaders -OutFile $temp -UseBasicParsing -ErrorAction Stop
        }
        catch {
            if (-not $token) { $token = Get-GitHubToken }
            if (-not $token) { throw }
            $assetHeaders.Authorization = "Bearer $token"
            Invoke-WebRequest -Uri $assetUrl -Headers $assetHeaders -OutFile $temp -UseBasicParsing -ErrorAction Stop
        }
    }

    $download = Get-Item -LiteralPath $temp
    if ($download.Length -ne $expectedSize) {
        throw "Tamaño inesperado: $($download.Length) bytes; se esperaban $expectedSize."
    }
    $hash = (Get-FileHash -LiteralPath $temp -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($hash -ne $expectedHash.ToLowerInvariant()) {
        throw 'El SHA-256 no coincide con el publicado por GitHub; se canceló la instalación.'
    }

    # Además del hash, confirma que sea el JAR del mod y que su versión interna
    # coincida con el tag antes de sustituir cualquier archivo de la instancia.
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [System.IO.Compression.ZipFile]::OpenRead($temp)
    try {
        $manifestEntry = $zip.GetEntry('META-INF/MANIFEST.MF')
        $modsEntry = $zip.GetEntry('META-INF/mods.toml')
        if ($null -eq $manifestEntry -or $null -eq $modsEntry) {
            throw 'El archivo no contiene MANIFEST.MF y mods.toml; no parece ser el JAR del mod.'
        }

        $manifestReader = [System.IO.StreamReader]::new($manifestEntry.Open())
        try { $manifestText = $manifestReader.ReadToEnd() }
        finally { $manifestReader.Dispose() }

        $modsReader = [System.IO.StreamReader]::new($modsEntry.Open())
        try { $modsText = $modsReader.ReadToEnd() }
        finally { $modsReader.Dispose() }

        $manifestVersion = [regex]::Match(
            $manifestText, '(?im)^Implementation-Version:\s*([0-9]+\.[0-9]+\.[0-9]+)\s*$')
        $modIdPresent = [regex]::IsMatch($modsText, '(?m)^\s*modId\s*=\s*"gripeverde"\s*$')
        $modVersion = [regex]::Match(
            $modsText, '(?m)^\s*version\s*=\s*"([0-9]+\.[0-9]+\.[0-9]+)"\s*$')
        if (
            -not $manifestVersion.Success -or
            $manifestVersion.Groups[1].Value -ne $version -or
            -not $modIdPresent -or
            -not $modVersion.Success -or
            $modVersion.Groups[1].Value -ne $version
        ) {
            throw "La versión interna del JAR no coincide con v$version; se canceló la instalación."
        }
    }
    finally {
        $zip.Dispose()
    }

    # El temporal está dentro de mods, en el mismo volumen. Primero instala el
    # archivo ya verificado; solo después borra versiones anteriores. Si el
    # reemplazo falla, el JAR anterior permanece intacto.
    Move-Item -LiteralPath $temp -Destination $dest -Force -ErrorAction Stop
    $installedHash = (Get-FileHash -LiteralPath $dest -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($installedHash -ne $expectedHash.ToLowerInvariant()) {
        throw 'Falló la verificación posterior a la instalación; no se borraron otros JAR.'
    }

    $cleanupFailures = @()
    try {
        $oldJars = @(Get-ChildItem -LiteralPath $mods -File -Filter 'gripeverde-*.jar' -ErrorAction Stop |
            Where-Object { $_.FullName -ne $dest })
        foreach ($jar in $oldJars) {
            try { Remove-Item -LiteralPath $jar.FullName -Force -ErrorAction Stop }
            catch { $cleanupFailures += $jar.Name }
        }
    }
    catch {
        Write-Warning "La instalación quedó verificada, pero no pude enumerar todos los JAR viejos: $($_.Exception.Message)"
    }

    if (Test-Path -LiteralPath $legacyBackupDir -PathType Container) {
        try { Remove-Item -LiteralPath $legacyBackupDir -Recurse -Force -ErrorAction Stop }
        catch { Write-Warning "No pude borrar la carpeta heredada de respaldos: $legacyBackupDir" }
    }

    Write-Host "GripeVerde $($release.tag_name) instalado y verificado en: $dest" -ForegroundColor Green
    Write-Host "Tamaño: $expectedSize bytes  |  SHA-256: $hash"
    if ($cleanupFailures.Count -gt 0) {
        Write-Warning ('Cerrá Minecraft y quitá estos JAR anteriores para evitar duplicados: ' + ($cleanupFailures -join ', '))
    }
}
catch {
    Write-Host "No se completó la instalación: $($_.Exception.Message)" -ForegroundColor Red
    throw
}
finally {
    if (Test-Path -LiteralPath $temp -PathType Leaf) {
        Remove-Item -LiteralPath $temp -Force -ErrorAction SilentlyContinue
    }
}
