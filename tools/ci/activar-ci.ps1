# tools/ci/activar-ci.ps1 - Activa la CI de Gripe Verde en un paso.
#
# ¿Por que existe? La sesion de Arena del 2026-08-23 no pudo subir el archivo
# .github/workflows/build.yml porque el token de la app de GitHub no tiene el
# permiso "workflows" (git push y API, ambos 403/rejected). Tu git local si
# tiene credenciales completas, asi que este script mueve la plantilla a su
# sitio, commitea y pushea con TU cuenta.
#
# Uso (desde cualquier carpeta):
#   powershell -ExecutionPolicy Bypass -File "C:\Users\santi\Desktop\GripeVerde\tools\ci\activar-ci.ps1"

$ErrorActionPreference = "Stop"

# Raiz del repo = dos niveles arriba de este script (tools/ci/activar-ci.ps1)
$Repo = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
Set-Location $Repo

$Src    = Join-Path $Repo "tools\ci\build.yml"
$DstDir = Join-Path $Repo ".github\workflows"
$Dst    = Join-Path $DstDir "build.yml"

if (-not (Test-Path $Src)) { throw "No existe la plantilla $Src" }

if (Test-Path $Dst) {
    $igual = (Get-FileHash $Src).Hash -eq (Get-FileHash $Dst).Hash
    if ($igual) {
        Write-Host "La CI ya estaba activada (contenido identico). Nada que hacer."
        git log --oneline -1
        exit 0
    }
    Write-Host "Existe una CI distinta; se sobrescribira con la plantilla vigente."
}

New-Item -ItemType Directory -Force $DstDir | Out-Null
Copy-Item $Src $Dst -Force

git add .github/workflows/build.yml
$pending = git status --porcelain .github/workflows/build.yml
if ([string]::IsNullOrWhiteSpace($pending)) {
    Write-Host "Nada que commitear."
    exit 0
}

git commit -m "ci: activar workflow de build (JDK 17, sellos de verificacion, jar como artifact)"
if ($LASTEXITCODE -ne 0) { throw "git commit fallo" }

git push
if ($LASTEXITCODE -ne 0) { throw "git push fallo; revisa credenciales de git" }

Write-Host ""
Write-Host "CI ACTIVADA. Mira la pestaña Actions del repo: el primer build con JDK 17"
Write-Host "arranca solo con este push y publica el jar como artifact 'gripeverde-jar'."
