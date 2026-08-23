# tools/deploy.ps1 - Compila y despliega Gripe Verde en la instancia SKLauncher.
# v0.23.0: la version se lee de build.gradle (ya no se edita el script por release).
#
# Uso:  powershell -ExecutionPolicy Bypass -File tools\deploy.ps1
# Requiere: Java 17 en PATH, repo clonado en $Repo.

$ErrorActionPreference = "Stop"

$Repo    = "C:\Users\santi\Desktop\GripeVerde"
$Mods    = "C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods"
$Branch  = "arena/01a02f45-gripeverde"

Set-Location $Repo

$gradle = Get-Content (Join-Path $Repo "build.gradle") -Raw
if ($gradle -notmatch "version\s*=\s*'([^']+)'") { throw "No se encontro 'version' en build.gradle" }
$Version = $Matches[1]
$Jar = Join-Path $Repo "build\libs\gripeverde-$Version.jar"

Write-Host "Desplegando Gripe Verde v$Version desde la rama $Branch"

git fetch origin $Branch
if ($LASTEXITCODE -ne 0) { throw "git fetch fallo" }
git reset --hard FETCH_HEAD
if ($LASTEXITCODE -ne 0) { throw "git reset fallo" }

Get-ChildItem (Join-Path $Repo "build\libs") -Filter "gripeverde-*.jar" -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem $Mods -Filter "gripeverde-*.jar" -ErrorAction SilentlyContinue | Remove-Item -Force

.\gradlew.bat clean build
if ($LASTEXITCODE -ne 0) { throw "Gradle fallo" }
if (-not (Test-Path $Jar)) { throw "No se genero $Jar" }

Copy-Item $Jar $Mods -Force
Get-Item (Join-Path $Mods "gripeverde-$Version.jar")
Write-Host "OK: comprobar en el cliente que el reloj del Title muestra v$Version"
