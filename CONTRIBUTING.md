# Contributing - Eternal Craft: Gripe Verde

Guia corta para tocar el mod sin romper las reglas del proyecto.

## Estado vigente de la entrega

- Version preparada: **v0.23.1**.
- PR 14 (v0.22.0) ya mergeada en main (`4ca038d`).
- Rama de trabajo vigente: `arena/01a02f45-gripeverde`.
- Baseline: 60 Java, 25 locales con 95 claves cada uno, 25 cronicas, 35
  eventos de sonido, 42 referencias OGG y 42 archivos OGG.
- El tag/release v0.23.0 no debe crearse hasta cerrar KI-53 con Java 17 y
  una prueba completa en Minecraft (queda tambien el pendiente v0.22.0).
- Ruta local vigente del owner: `C:\Users\santi\Desktop\GripeVerde`.
- Deploy vigente: `tools\deploy.ps1` hacia
  `C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`.
- Las rutas antiguas y `.deploy` que sobreviven en documentacion historica
  no son instrucciones de trabajo.

## Reglas base

1. **Rama fija:** el trabajo de cada sesion vive en su rama `arena/*-gripeverde`.
   No cambiar de branch ni mergear ramas viejas sin verificar `git log` y
   `build.gradle version` (ver E-STEEL-REGRESSION).
2. **PROHIBIDO gameplay:** no agregar sistemas de enfermedad, cuarentena
   jugable, NPCs, items, bloques, recetas ni efectos que alteren la
   supervivencia. El mod es solo interfaz + audio + memoria narrativa.
3. **Sin resourcepacks:** nada de `gripeverde_steel`, `tools/steel_pack` ni
   texturas vanilla modificadas (R-NO-RESOURCEPACK). La identidad visual se
   hace con Java/render/widgets.
4. **No tocar logica vanilla:** solo presentacion (R-VANILLA-LOGIC). Los
   contenedores son 100% vanilla (0 PNG).
5. **Alcance coherente por version:** evitar mezclar cambios no relacionados.
   Una entrega transversal solo procede cuando fue pedida expresamente y
   cuenta con backup previo verificable, como v0.22.0.

## Antes de proponer un cambio

- Leer primero el addendum operativo al comienzo de `CONTEXTO.md`; consultar
  el archivo historico solo para reglas R-*, errores E-* y decisiones pasadas.
- Auditar el estado real: `git status`, `find src -name "*.java" | wc -l`,
  `cat build.gradle | grep version`.
- Si el entorno no dispone de JDK 17 (E125), limitarse a validaciones
  estaticas y dejar explicitamente pendiente la compilacion semantica.

## Validacion

- Usar **Java 17** para Forge 1.20.1.
- **4 escalas GUI** para cualquier cambio de UI (scale 1-4; reiniciar cliente
  al cambiar escala si hay duda).
- Validar JSON tras editar lang/sounds (`python3 -c "import json; json.load(...)"`).
- Build local limpio: `.\gradlew.bat clean build` ->
  `build\libs\gripeverde-<VERSION>.jar`.
- Deploy: limpiar primero los `gripeverde-*.jar` antiguos tanto en
  `build\libs` como en el directorio de SKLauncher, copiar el jar de la
  version exacta y confirmarla en el reloj del Title.
- Un parser externo puede comprobar sintaxis, pero no sustituye el build
  contra Forge/Minecraft ni la prueba en cliente.

## Verificaciones de build activas (processResources)

- `verifyGripeVerdeLocalization` — conjunto exacto de 25 locales, JSON
  valido, exactamente 95 claves no vacias con paridad frente a `en_us` y
  presencia de la version actual en las 25 cronicas in-game.
- `verifyGripeVerdeSoundAssets` — estructura e identificadores de eventos,
  listas no vacias, referencias existentes y cobertura OGG exacta, sin
  archivos huerfanos.
- `verifyGripeVerdeNoDeadCode` — falla si reaparecen simbolos de codigo
  desterrado fuera de comentarios (ThemeManager, steel pack, bg_town,
  IconButton, dev screens, `drawHeraldTicker` y otros retirados).
- `verifyGripeVerdeVersionSync` — `GripeVerdeMod.VERSION` debe coincidir con
  la version de `build.gradle`.
- `verifyGripeVerdeMusicDurations` — la duracion real Vorbis de cada OGG de
  musica debe estar dentro de 1.5 s de `TRACK_DURATIONS_MS`.
- `verifyGripeVerdeSubtitles` — toda clave de subtitulo no vacia (8 eventos
  ambientales) debe existir en los 25 idiomas.
- `verifyGripeVerdeSoundUsage` — ningun SoundEvent registrado sin uso fuera
  del allowlist legacy (10 eventos devengados, ver javadoc de ModSounds).

Ademas, hay un workflow de CI listo en `tools/ci/build.yml` (compila todo
con JDK 17 en cada push/PR); se activa en un paso con
`tools/ci/activar-ci.ps1` (la sesion no pudo subirlo directamente por
permisos; el script usa las credenciales locales del owner).

## Codigo

- UTF-8 sin BOM, LF, sin trailing whitespace (ver `.editorconfig`).
- Commits descriptivos: `feat:` / `fix:` / `docs:` / `chore:` + version.
- No dejar clases ni metodos sin uso; si algo ya no sirve, borrarlo y
  documentarlo en CONTEXTO.md y KNOWN_ISSUES.md.

## Cierre de hito (R-CIERRE)

Al bumpear version, sincronizar en la misma sesion: CONTEXTO.md,
CHANGELOG.md, KNOWN_ISSUES.md, README.md y los 25 changelog in-game (solo
si el hito tiene manifestacion observable, ver R-CHANGELOG-DISCRECIONAL).
Luego commit + push (los tags se publican cuando el usuario valida en
cliente real).
