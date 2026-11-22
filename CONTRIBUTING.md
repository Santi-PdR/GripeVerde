# Contributing - Eternal Craft: Gripe Verde

Guia corta para tocar el mod sin romper las reglas del proyecto.

## Estado vigente de la entrega

- Última publicación verificada: **v0.401.0**, tag dirigido al commit construido
  `e0f845007681e92e747e04127169f483e159456b`; JAR de 10.855.632 bytes, SHA-256
  `dd37d443b7307c1b3d0d09fc5befdaee6d1dd887bfca002d6e7bc3670ef0ee88`.
  El CI de publicación `44997077455` pasó en `build` y `publish`.
- La próxima versión requiere sincronizar versión, marcador, CHANGELOG y las 25
  notas dentro del juego; el workflow publica automáticamente solo tras pasar
  todas las comprobaciones.
- Rama de trabajo vigente: `arena/48be547c-gripeverde`, un solo push por entrega.
- Baseline medida por las herramientas: **72 archivos Java**, **25 locales con
  192 claves** cada uno, 35 eventos de sonido, 42 referencias OGG y 42 archivos
  OGG, 4 piezas de música y **8/8 sellos espejo**.
- Cada publicación es: `python3 tools/verify_mirrors.py` -> `python3 tools/lint_java.py`
  -> `python3 tools/test_list_render_guards.py` ->
  `python3 tools/test_smooth_scroll.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_language_screen.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/test_dispatch_save.py --require-java` (en CI, con JDK 17) ->
  `python3 tools/ci/test_release_gate.py` ->
  `python3 tools/ci/test_installer.py` -> commit + push -> esperar el CI ->
  comprobar el release y el SHA-256. El job `publish` compara `release-tag.txt`
  con `build.gradle`, crea el tag sobre `$GITHUB_SHA` (el commit que produjo el
  JAR) y no sobrescribe un release existente.
- `./gradlew check` y `build` incluyen `verifyServerAddressRegression`:
  38 comprobaciones ejecutan los helpers de direcciones contra el parser real
  de Minecraft sin red ni cliente gráfico. Puede correrse esa tarea sola.
- También incluyen `verifyTextFieldGeometryRegression`: 12 comprobaciones
  del margen lógico con widgets reales y sin rendering ni eventos de mouse.
- `verifyDispatchPersistenceRegression` también es obligatoria en check/build:
  usa DispatchLibrary real y un config temporal de Forge para comprobar JSON,
  edición sin E/S, fallo de reemplazo, limpieza y recuperación. La suite Python
  de CEDA ejecuta los métodos de pantalla con reloj/almacén dobles, sin GUI.
- **Nunca dos pushes seguidos**: el CI cancela el run anterior por concurrencia.
- Sin JDK local: la compilación semántica la hace el CI. No instalar Java 17
  en la máquina para publicar.
- Deploy del owner: la carpeta
  `D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods`, con JAR
  solamente y una sola versión del mod. El instalador verificado está en
  `tools/install-latest.ps1` y enlazado desde el README; no se ejecuta desde el
  sandbox en la PC del owner.
- Las rutas antiguas que sobreviven en documentación histórica no son
  instrucciones de trabajo.

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
  valido, exactamente 191 claves no vacias con paridad frente a `en_us` y
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
- `verifyGripeVerdeSoundUsage` — ningún SoundEvent registrado sin uso; el
  allowlist legacy está vacío desde v0.26.0.

La CI activa está en `.github/workflows/build.yml`: verifica espejos,
lint Java, la compuerta de publicación y compila con JDK 17. El job que ejecuta
Gradle solo tiene `contents: read`; los comentarios de fallo se publican desde
un job separado con permisos de escritura. Los PR nunca publican;
`workflow_dispatch` exige activar explícitamente `publish_release`.
`tools/ci/build.yml` es solo una plantilla de build + artifact, alineada con
los checks del workflow activo, sin la etapa de publicación.

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
