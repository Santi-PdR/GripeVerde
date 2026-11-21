$ErrorActionPreference = 'Stop'
$mods = 'D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods'
$repo = 'Santi-PdR/GripeVerde'
$api = "https://api.github.com/repos/$repo/releases/latest"
$dest = $null
$temp = Join-Path $mods ('.gripeverde-download-' + [guid]::NewGuid().ToString('N') + '.tmp')
# 2026-11-13: el script ya no guarda copias del JAR anterior. La carpeta de
# respaldos que dejaban las versiones viejas se borra aqui, en la proxima pasada.
$legacyBackupDir = Join-Path (Split-Path -Parent $mods) 'gripeverde-jar-backups'

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

    # JAR de GripeVerde que ya estuvieran puestos: se borran, no se respaldan.
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

    # Se borra el JAR viejo ANTES de poner el nuevo: nunca puede quedar la carpeta
    # con dos gripeverde a la vez, que impediria abrir el juego.
    $removed = @()
    foreach ($jar in $old) {
        if ($jar.FullName -eq $dest) { continue }
        Remove-Item -LiteralPath $jar.FullName -Force
        $removed += $jar.Name
    }

    Move-Item -LiteralPath $temp -Destination $dest -Force
    Write-Host "GripeVerde $($release.tag_name) instalado en: $dest" -ForegroundColor Green
    if ($removed.Count -gt 0) { Write-Host ("JAR anterior borrado: " + ($removed -join ', ')) }
    else { Write-Host "No habia JAR anterior en la carpeta de mods." }
    Write-Host "SHA-256 verificado: $hash"

    # Limpieza de la carpeta de respaldos que dejaban las versiones anteriores.
    if (Test-Path -LiteralPath $legacyBackupDir -PathType Container) {
        Remove-Item -LiteralPath $legacyBackupDir -Recurse -Force
        Write-Host "Carpeta de respaldos vieja borrada: $legacyBackupDir"
    }
}
catch {
    # Sin respaldos no hay nada que restaurar: si el fallo ocurre antes de tocar la
    # carpeta de mods, la version que habia sigue tal cual.
    Write-Host "No se completo la instalacion; revisa la carpeta de mods." -ForegroundColor Red
    throw
}
finally {
    if (Test-Path -LiteralPath $temp -PathType Leaf) {
        Remove-Item -LiteralPath $temp -Force
    }
}
