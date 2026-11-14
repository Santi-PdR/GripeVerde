<#
.SYNOPSIS
    Instala un JAR de GripeVerde descargado del CI en la carpeta de mods.

.DESCRIPTION
    Toma el ZIP del artefacto "GripeVerde-jar" de GitHub Actions, comprueba su
    integridad y deja el JAR en la carpeta de mods de la instancia.

    Reglas que respeta:
      - Solo reemplaza los gripeverde-*.jar anteriores; conserva los demas mods.
      - No borra CEDA Dispatch ni ningun otro archivo .jar.
      - Informa (y opcionalmente aparta) cualquier archivo que no sea .jar en la
        carpeta de mods, para que quede solo con archivos .jar.

.PARAMETER ArtifactZip
    Ruta del ZIP descargado de Actions (por ejemplo GripeVerde-jar.zip).

.PARAMETER Mods
    Carpeta de mods de la instancia. Por defecto la del SKLauncher del owner.

.PARAMETER ExpectedSha256
    Opcional. Si se indica, el JAR debe coincidir o el script falla sin tocar nada.

.PARAMETER MoveNonJarTo
    Opcional. Carpeta a la que se mueven los archivos que no son .jar. Si se
    omite, solo se informan y la carpeta de mods queda sin cambios en ese aspecto.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File tools\install-artifact.ps1 `
        -ArtifactZip "$env:USERPROFILE\Downloads\GripeVerde-jar.zip"
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string] $ArtifactZip,

    [string] $Mods = 'D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods',

    [string] $ExpectedSha256 = '',

    [string] $MoveNonJarTo = ''
)

$ErrorActionPreference = 'Stop'

function Fail([string] $message) {
    Write-Error $message
    exit 1
}

if (-not (Test-Path -LiteralPath $ArtifactZip -PathType Leaf)) {
    Fail "No existe el ZIP del artefacto: $ArtifactZip"
}
if (-not (Test-Path -LiteralPath $Mods -PathType Container)) {
    Fail "No existe la carpeta de mods: $Mods"
}

# El nombre del mod debe seguir el patron del build: gripeverde-<version>.jar
$pattern = '^gripeverde-(?<version>\d+\.\d+\.\d+)\.jar$'

$temp = Join-Path ([System.IO.Path]::GetTempPath()) ("gripeverde-install-" + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $temp | Out-Null

try {
    Expand-Archive -LiteralPath $ArtifactZip -DestinationPath $temp -Force

    $jars = @(Get-ChildItem -LiteralPath $temp -Recurse -File -Filter '*.jar')
    if ($jars.Count -eq 0) { Fail 'El artefacto no contiene ningun JAR.' }

    $candidates = @($jars | Where-Object { $_.Name -match $pattern })
    if ($candidates.Count -eq 0) {
        Fail ('Ningun JAR coincide con gripeverde-<version>.jar. Encontrados: ' +
              (($jars | ForEach-Object { $_.Name }) -join ', '))
    }
    if ($candidates.Count -gt 1) {
        Fail ('El artefacto contiene varios JAR de GripeVerde: ' +
              (($candidates | ForEach-Object { $_.Name }) -join ', ') +
              '. Deja uno solo para evitar versiones mezcladas.')
    }

    $jar = $candidates[0]
    $version = [regex]::Match($jar.Name, $pattern).Groups['version'].Value

    $hash = (Get-FileHash -LiteralPath $jar.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    Write-Host "JAR:        $($jar.Name)"
    Write-Host "Version:    $version"
    Write-Host "SHA-256:    $hash"

    if ($ExpectedSha256) {
        $expected = $ExpectedSha256.Trim().ToLowerInvariant()
        if ($hash -ne $expected) {
            Fail "El SHA-256 no coincide con el esperado ($expected). No se instalo nada."
        }
        Write-Host 'SHA-256 verificado.'
    }

    # Integridad interna: mods.toml presente y version del manifiesto igual al nombre.
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [System.IO.Compression.ZipFile]::OpenRead($jar.FullName)
    try {
        $entries = @($archive.Entries | ForEach-Object { $_.FullName })
        if ($entries -notcontains 'META-INF/mods.toml') {
            Fail 'El JAR no contiene META-INF/mods.toml; no parece un mod de Forge.'
        }
        $tomlEntry = $archive.GetEntry('META-INF/mods.toml')
        $reader = New-Object System.IO.StreamReader($tomlEntry.Open())
        try { $toml = $reader.ReadToEnd() } finally { $reader.Dispose() }
        if ($toml -notmatch 'modId\s*=\s*"gripeverde"') {
            Fail 'mods.toml no declara modId="gripeverde"; se aborta la instalacion.'
        }
        if ($toml -notmatch ('version\s*=\s*"' + [regex]::Escape($version) + '"')) {
            Write-Warning "mods.toml no declara la version $version; revisa el artefacto."
        }
    } finally {
        $archive.Dispose()
    }

    # Retirar solo las versiones anteriores de GripeVerde.
    $previous = @(Get-ChildItem -LiteralPath $Mods -File -Filter 'gripeverde-*.jar')
    foreach ($old in $previous) {
        Remove-Item -LiteralPath $old.FullName -Force
        Write-Host "Retirado:   $($old.Name)"
    }

    $destination = Join-Path $Mods $jar.Name
    Copy-Item -LiteralPath $jar.FullName -Destination $destination -Force

    $installedHash = (Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($installedHash -ne $hash) {
        Fail 'La copia instalada no coincide con el JAR del artefacto.'
    }

    # La carpeta debe quedar con archivos .jar unicamente.
    $others = @(Get-ChildItem -LiteralPath $Mods -File | Where-Object { $_.Extension -ne '.jar' })
    if ($others.Count -gt 0) {
        if ($MoveNonJarTo) {
            if (-not (Test-Path -LiteralPath $MoveNonJarTo)) {
                New-Item -ItemType Directory -Path $MoveNonJarTo -Force | Out-Null
            }
            foreach ($file in $others) {
                Move-Item -LiteralPath $file.FullName -Destination $MoveNonJarTo -Force
                Write-Host "Apartado:   $($file.Name)"
            }
        } else {
            Write-Warning ('Hay archivos que no son .jar en la carpeta de mods: ' +
                (($others | ForEach-Object { $_.Name }) -join ', ') +
                '. Usa -MoveNonJarTo <carpeta> para apartarlos.')
        }
    }

    $nonJarLeft = @(Get-ChildItem -LiteralPath $Mods -File | Where-Object { $_.Extension -ne '.jar' }).Count
    $total = @(Get-ChildItem -LiteralPath $Mods -File -Filter '*.jar').Count

    Write-Host ''
    Write-Host 'Contenido final de la carpeta de mods:'
    Get-ChildItem -LiteralPath $Mods -File | Sort-Object Name | ForEach-Object {
        Write-Host ("  {0,-44} {1,10:N0} bytes" -f $_.Name, $_.Length)
    }
    Write-Host ''
    Write-Host "Instalado GripeVerde $version. JAR en la carpeta: $total. Archivos que no son .jar: $nonJarLeft."

    if ($version -ne '0.173.0') {
        Write-Warning "La version instalada no es la esperada (0.173.0)."
    }
} finally {
    Remove-Item -LiteralPath $temp -Recurse -Force -ErrorAction SilentlyContinue
}
