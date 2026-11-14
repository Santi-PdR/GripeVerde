# tools/deploy.ps1 - Compila y despliega Gripe Verde en la instancia SKLauncher.
# v0.23.0: la version se lee de build.gradle (ya no se edita el script por release).
# v0.25.0: la version se lee DESPUES del git reset — antes se leia antes de
# sincronizar y, cuando la version cambiaba entre local y remoto, el build
# generaba el jar nuevo pero la copia buscaba el del release anterior.
#
# Uso:  powershell -ExecutionPolicy Bypass -File tools\deploy.ps1
# Requiere: Java 17 en PATH, repo clonado en $Repo.

$ErrorActionPreference = "Stop"

$Repo    = "C:\Users\santi\Desktop\GripeVerde"
$Mods    = "D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods"
# v0.173.0: la rama del PR vigente; antes apuntaba a una sesion ya cerrada.
$Branch  = "codex/v0.104-ghoul-warning-poster"

Set-Location $Repo

git fetch origin $Branch
if ($LASTEXITCODE -ne 0) { throw "git fetch fallo" }
git reset --hard FETCH_HEAD
if ($LASTEXITCODE -ne 0) { throw "git reset fallo" }

# Leer la version con el repo ya sincronizado (contenido remoto vigente).
$gradle = Get-Content (Join-Path $Repo "build.gradle") -Raw
if ($gradle -notmatch "version\s*=\s*'([^']+)'") { throw "No se encontro 'version' en build.gradle" }
$Version = $Matches[1]

Write-Host "Desplegando Gripe Verde v$Version desde la rama $Branch"

Get-ChildItem (Join-Path $Repo "build\libs") -Filter "gripeverde-*.jar" -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem $Mods -Filter "gripeverde-*.jar" -ErrorAction SilentlyContinue | Remove-Item -Force

.\gradlew.bat clean build
if ($LASTEXITCODE -ne 0) { throw "Gradle fallo" }

# Localizar el jar por glob: el nombre exacto sale de la version recien leida,
# pero esto vuelve el script inmune a cualquier desfase de nomenclatura.
$JarFile = Get-ChildItem (Join-Path $Repo "build\libs") -Filter "gripeverde-*.jar" -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $JarFile) { throw "No se genero el jar en build\libs" }

Copy-Item $JarFile.FullName $Mods -Force
Get-Item (Join-Path $Mods $JarFile.Name)
Write-Host "OK: comprobar en el cliente que el reloj del Title muestra v$Version"
Write-Host "Si solo quieres instalar el JAR del CI, usa tools\install-artifact.ps1."
