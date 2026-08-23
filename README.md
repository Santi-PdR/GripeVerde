# Eternal Craft: Gripe Verde

Mod de interfaz para Minecraft Forge 1.20.1.
Ambientacion victoriana bajo cuarentena, epidemia Green Flu.

## Version actual

**v0.22.0 - La vigilia queda sellada**

La entrega final mantiene completa la escena del Title y unifica su
simulación, música y ambiente en un tick estable de 20 Hz. La pantalla de
Accesibilidad expone ahora Movimiento reducido, que congela el movimiento
atmosférico, el grano y los destellos sin retirar la calle, los faroles ni la
utilería; los toggles anuncian también su estado al narrador.

La memoria persistente se escribe mediante un temporal durable y reemplazo
atómico, conserva la última copia válida y recupera un temporal interrumpido
o el respaldo si el JSON principal se daña. El render reutiliza generadores
deterministas para evitar asignaciones por cuadro sin modificar su aspecto.
Los validadores integrados en el build comprueban la paridad de 25 idiomas
(83 claves), los 25 changelogs, 35 eventos y 42 OGG. La sintaxis de las 60
clases Java se comprobó por separado con `java-parser`; la compilación
semántica contra Forge requiere todavía Java 17.

### Estado de entrega

- Implementación final: commit `a1decf304b0c7236d624d4637bd3b5dcaa3961db`.
- Revisión del owner: [PR 14](https://github.com/Santi-PdR/GripeVerde/pull/14).
- Validación estática y documental completada; árbol local y remoto limpio.
- El build Forge y la prueba en Minecraft continúan pendientes bajo KI-52
  porque el sandbox no dispone de Java 17.
- No existe tag/release v0.22.0: debe publicarse solo después de validar el
  cliente real.

## Resumen visible del Mod Menu

Eternal Craft: Gripe Verde convierte los menus de Minecraft en un gabinete victoriano bajo cuarentena. El menu principal muestra una ciudad oscura con lluvia, niebla, reloj de bolsillo, carteles oficiales, musica y ambiente propio. Las salas personalizadas conservan cabeceras y funciones diferenciadas sobre pergamino; Video abre la interfaz de Embeddium cuando esta disponible y usa la pantalla vanilla como fallback. El conjunto tematiza la navegacion sin alterar la logica del juego.

Los contenedores del juego conservan su aspecto vanilla original; el mod no altera las texturas de los contenedores ni usa resourcepacks. El mod es client-side y no agrega gameplay.

## Caracteristicas principales

### Menu principal victoriano
- Layout responsivo para pantallas pequenas, decidido por espacio disponible y
  nunca por FPS; la calle y los faroles permanentes no dependen del rendimiento.
- Fondo atmosferico del Title con cielo nocturno, luna, lluvia, niebla ocre, skyline victoriano y calle mojada.
- Calle decorativa adaptativa por escala (v0.7.0).
- Faroles de gas reactivados en v0.9.1 con anclaje a la calle y densidad adaptativa por escala GUI.
- Reloj de bolsillo animado con manecillas detenidas en 11:52, segundero mecanico con glitches, oscilacion pendular y tic-tac sincronizado con las marcas horarias.
- Posters institucionales (Quarantine, Wanted: Patient Zero).
- Firma easter egg de S. Pereira, MDCCCXCVII.
- Gabinete de ajustes (Opciones) sobre pergamino con Title vivo detras, alcanzable desde el Title y desde la pausa (v0.11.0).

### Sistema de audio del menu (hito v0.8)
- 4 pistas de musica del menu con shuffle aleatorio.
- Crossfade de 4 segundos entre pistas consecutivas.
- Ducking automatico: la musica baja cuando suenan campanas, truenos, cadenas o cuervos.
- Slider de volumen "Menu Music" en la pantalla de Opciones, ajustable en tiempo real.
- Persistencia: la musica sobrevive a Options y subpantallas, se detiene sola al entrar a un mundo, y arranca de nuevo al volver.
- Sistema de ambiente sonoro por capas: viento, lluvia, ciudad, eventos ocasionales, presagios raros.

### La memoria del gabinete (v0.16.0)
- Memoria persistente propia (descubrimientos, objetos, noches seguidas, ausencias) separada del ajuste de audio.
- 46 descubrimientos con fichas, sellos y pistas encadenadas; algunas consecuencias son permanentes.
- Marginalia contextual del cronista: notas que responden a lo que el lector hace.
- La Sala de consulta: detras del ultimo folio de la cronica, un archivo con las fichas halladas y documentos recuperados.

### Otros
- Historial de cambios in-game con word-wrap real, accesible desde el boton "?".
- Soporte para 25 variantes de idioma (5 de ingles + 20 de espanol).

## Pantallas implementadas

- **Title** - menu principal victoriano completo con musica y ambiente sonoro.
- **Pause** - diario del superviviente (R14, aprobada).
- **Changelog** - historial narrativo en pergamino.
- **Options** - gabinete central con accesos propios y slider FOV integrado.
- **Sound** - sala acustica sobre pergamino y sliders propios del mod.
- **Video** - Embeddium si esta instalado; pantalla de video vanilla como fallback.
- **Controls** - manual operativo con toggles sobre pergamino.
- **Language** - archivo linguistico con lista y seleccion localizadas.
- **Chat** - protocolos de correspondencia sobre pergamino.
- **Accessibility** - opciones vanilla mas el toggle persistente de Movimiento reducido.
- **Online / Multiplayer** - registros de conexion y hospedaje con presentacion propia.
- **Credits** - ultimo archivo del gabinete.
- **Archive** - sala de consulta (46 fichas).
- **DirectJoin / EditServer / Confirm** - formularios y autorizaciones pulidos en v0.21.0.

## Build

Con Java 17, ejecutar: `.\gradlew.bat clean build`

El jar se genera en `build\libs\gripeverde-VERSION.jar`

Verificaciones activas en `processResources`:
- `verifyGripeVerdeLocalization` (JSON válido, 83 claves en paridad y
  v0.22.0 presente en los 25 changelogs).
- `verifyGripeVerdeSoundAssets` (eventos completos, referencias existentes y ningún OGG huérfano).
- `verifyGripeVerdeNoDeadCode` (símbolos retirados fuera del código productivo).

## Deploy

Repositorio local actual: `C:\Users\santi\Desktop\GripeVerde`.

Instancia de prueba de SKLauncher:
`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`.

Antes de cada validacion se eliminan los jars anteriores de Gripe Verde, se
compila y se copia exactamente `gripeverde-0.22.0.jar`. La version mostrada
por el reloj debe comprobarse en pantalla antes de aprobar la entrega.

Flujo PowerShell vigente para actualizar, compilar y desplegar:

```powershell
$ErrorActionPreference = "Stop"
$Repo = "C:\Users\santi\Desktop\GripeVerde"
$Mods = "C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods"
$Jar = Join-Path $Repo "build\libs\gripeverde-0.22.0.jar"

Set-Location $Repo
git fetch origin arena/01a02cee-gripeverde
if ($LASTEXITCODE -ne 0) { throw "git fetch fallo" }
git reset --hard FETCH_HEAD
if ($LASTEXITCODE -ne 0) { throw "git reset fallo" }

Get-ChildItem (Join-Path $Repo "build\libs") -Filter "gripeverde-*.jar" -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem $Mods -Filter "gripeverde-*.jar" -ErrorAction SilentlyContinue | Remove-Item -Force

.\gradlew.bat clean build
if ($LASTEXITCODE -ne 0) { throw "Gradle fallo" }
if (-not (Test-Path $Jar)) { throw "No se genero $Jar" }

Copy-Item $Jar $Mods -Force
Get-Item (Join-Path $Mods "gripeverde-0.22.0.jar")
```

## Idiomas soportados

**Ingles:** en_us, en_gb, en_ca, en_au, en_nz

**Espanol:** es_es, es_mx, es_ar, es_419, es_bo, es_cl, es_co, es_cr, es_do, es_ec, es_gt, es_hn, es_ni, es_pa, es_pe, es_pr, es_py, es_sv, es_uy, es_ve

## Documentacion

- `CONTRIBUTING.md` - guia corta de contribucion (branch fija, no gameplay, validacion).
- `CONTEXTO.md` - contexto maestro actual para retomar sesiones v0.22.0.
- `CHANGELOG.md` - historial narrativo de versiones orientado a jugadores (v0.10.0 → v0.22.0).
- `KNOWN_ISSUES.md` - estado tecnico, decisiones activas, errores documentados, KIs.
- `README.md` - esta portada.

Históricos eliminados en v0.21.2: `AUDITORIA_PROFESIONAL.md`, `MICRODETALLES_EXTREMO.md`, `VALIDACION_FASE4.md` (eran referencia de polish v0.14.0, ya revertido) y `CONTEXTO_PARA_IA.md` (resumen redundante de `CONTEXTO.md`).

## Requisitos

- Minecraft 1.20.1
- Forge 47.4.20 o superior
- Java 17
- Gradle 8.8
