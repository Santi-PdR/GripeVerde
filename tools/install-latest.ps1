$ErrorActionPreference = 'Stop'
$mods = 'D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods'
$repo = 'Santi-PdR/GripeVerde'
$api = "https://api.github.com/repos/$repo/releases/latest"
$dest = $null
$temp = Join-Path $mods ('.gripeverde-download-' + [guid]::NewGuid().ToString('N') + '.tmp')
$moved = @()

try {
    if (-not (Test-Path -LiteralPath $mods -PathType Container)) {
        throw "No existe la carpeta de mods: $mods"
    }

    # El repo es privado: sin credenciales la API responde 404 aunque todo exista.
    # Se reusa lo que ya haya en la maquina: GITHUB_TOKEN, la sesion de gh o la de git.
    $token = $env:GITHUB_TOKEN
    if (-not $token) {
        if (Get-Command gh -ErrorAction SilentlyContinue) {
            $token = (& gh auth token 2>$null | Select-Object -First 1)
        }
    }
    if (-not $token) {
        if (Get-Command git -ErrorAction SilentlyContinue) {
            $payload = "protocol=https`nhost=github.com`n`n"
            $lines = ($payload | & git credential fill 2>$null)
            $line = @($lines) | Where-Object { $_ -like 'password=*' } | Select-Object -First 1
            if ($line) { $token = [string]$line -replace '^password=', '' }
        }
    }
    if ($token) { $token = ([string]$token).Trim() }
    if (-not $token) {
        throw "Hace falta un token de GitHub. Opciones: definir `$env:GITHUB_TOKEN, loguear 'gh auth login', o pedir que el repo pase a publico."
    }

    $headers = @{ Authorization = "Bearer $token"; Accept = 'application/vnd.github+json' }
    $release = Invoke-RestMethod -Uri $api -Headers $headers -UseBasicParsing
    $asset = @($release.assets) | Where-Object { $_.name -like 'gripeverde-*.jar' } | Select-Object -First 1
    if (-not $asset) { throw "El release $($release.tag_name) no tiene un JAR de GripeVerde." }

    $dest = Join-Path $mods $asset.name
    $expectedSize = [int64]$asset.size
    $expectedHash = ([string]$asset.digest) -replace '^sha256:', ''
    if (-not $expectedHash) { throw 'GitHub no publico el digest del asset; no puedo verificar la descarga.' }

    # Si no hay JAR anterior (primera instalacion) se instala igual, sin respaldo.
    $old = @(Get-ChildItem -LiteralPath $mods -File -Filter '*gripeverde*.jar' -ErrorAction SilentlyContinue)

    $assetUrl = "https://api.github.com/repos/$repo/releases/assets/$($asset.id)"
    $dlHeaders = @{ Authorization = "Bearer $token"; Accept = 'application/octet-stream' }
    Invoke-WebRequest -Uri $assetUrl -Headers $dlHeaders -OutFile $temp -UseBasicParsing

    $file = Get-Item -LiteralPath $temp
    if ($file.Length -ne $expectedSize) {
        throw "Tamano de descarga inesperado: $($file.Length) bytes (esperaba $expectedSize)."
    }

    $hash = (Get-FileHash -LiteralPath $temp -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($hash -ne $expectedHash) {
        throw "El SHA-256 no coincide con el que publica GitHub; se cancelo la instalacion."
    }

    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [System.IO.Compression.ZipFile]::OpenRead($temp)
    try {
        $entries = @($zip.Entries | ForEach-Object { $_.FullName })
        $hasManifest = $entries -contains 'META-INF/MANIFEST.MF'
        $hasModsToml = @($entries | Where-Object { $_ -like '*mods.toml' }).Count -gt 0
        if (-not $hasManifest -or -not $hasModsToml) {
            throw 'El archivo descargado no parece ser el JAR del mod (falta MANIFEST o mods.toml).'
        }
    }
    finally {
        $zip.Dispose()
    }

    $backupDir = Join-Path (Split-Path -Parent $mods) 'gripeverde-jar-backups'
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'

    foreach ($jar in $old) {
        $backup = Join-Path $backupDir ($jar.Name + '.' + $stamp + '.bak')
        Move-Item -LiteralPath $jar.FullName -Destination $backup
        $moved += [pscustomobject]@{ Name = $jar.Name; Backup = $backup }
    }

    Move-Item -LiteralPath $temp -Destination $dest
    Write-Host "GripeVerde $($release.tag_name) instalado en: $dest" -ForegroundColor Green
    if ($moved.Count -gt 0) { Write-Host "JAR anterior respaldado en: $backupDir" }
    else { Write-Host "Primera instalacion: no habia JAR anterior que respaldar." }
    Write-Host "SHA-256 verificado: $hash"
}
catch {
    if ($moved.Count -gt 0 -and $dest -and (Test-Path -LiteralPath $dest -PathType Leaf)) {
        Remove-Item -LiteralPath $dest -Force
    }
    foreach ($item in $moved) {
        $from = $item.Backup
        if (Test-Path -LiteralPath $from -PathType Leaf) {
            Move-Item -LiteralPath $from -Destination (Join-Path $mods $item.Name) -Force
        }
    }
    Write-Host "No se completo la instalacion; se intento restaurar la version anterior." -ForegroundColor Red
    throw
}
finally {
    if (Test-Path -LiteralPath $temp -PathType Leaf) {
        Remove-Item -LiteralPath $temp -Force
    }
}
