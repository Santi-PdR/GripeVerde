# Eternal Craft: Gripe Verde

Mod de interfaz para Minecraft Forge 1.20.1.
Ambientacion victoriana bajo cuarentena, epidemia Green Flu.

## Version actual

**v0.25.0 - El que mira la tormenta**

Tres easter eggs nuevos en la vigilia (archivo: 52 fichas): en noche de
tormenta, quedarse a ver tres relámpagos suma ficha; la lluvia se deja
atrapar con un clic (la gota vuelve al cielo y los botones nunca se
bloquean); y escribir DOCTOR en el telégrafo oculto trae un susurro, una
campana baja y una firma que sigue llegando.

Incluye la serie 0.24.x completa — **v0.24.1 - La errata del cronista**
(hotfix de build: la 0.24.0 no compilaba por una P ilegal en un literal
hexadecimal; ya corregida, con un sello nuevo en el espejo que lintea
literales numéricos Java) sobre **v0.24.0 - El viento vuelve a soplar**:

El ambiente del gabinete por fin se escucha: el loop de viento estaba
grabado a -64 dB RMS (físicamente inaudible) y la lluvia a -32 dB. Se
regeneran por síntesis procedural (`tools/audio/regenerate_menu_ambience.py`,
reproducible) y se refuerzan ciudad, susurros y pasos; el slider de ambiente
ya manda sobre algo y la calle deja de sonar al entrar a un mundo. Visibles
nuevas: la Crónica se abre en la entrada de la versión instalada con sello de
lacre "NUEVO", ciertas sesiones (~14 %) arrancan en Noche de tormenta (lluvia
doble, truenos frecuentes, ficha propia) y la lluvia resbala en gotas por los
pergaminos. Easter eggs: el lacre del telegrama se rompe a golpes y un
diapasón oculto en Opciones de sonido afina la sala (archivo: 50 fichas).

**v0.23.2 - El telegrama llega intacto**

Hotfix de crash en campo reportado con registro completo desde una
instancia real (v0.22.0): el telegrama del easter egg del título podía
dibujarse con 0 letras visibles y la lista vacía de renglones detenía el
cliente con `ArrayIndexOutOfBoundsException`. El conteo de líneas queda
acotado por el tamaño real de la lista y el overlay decorativo blindado.

**v0.23.1 - Sellos y relojería**

Parche de mantenimiento duro sobre v0.23.0: dos sellos de build nuevos
(subtítulos resueltos en los 25 idiomas y ningún evento de sonido registrado
sin uso fuera del allowlist legacy documentado), memoria del cronista con
migración versionada + sanitize de bounds, y la capa de ambiente migrada al
mismo reloj monotónico que la música (toda la relojería del mod es ya
inmune a ajustes de hora del SO). La CI se activa en un paso con
`tools/ci/activar-ci.ps1` (la sesión no pudo subir el workflow por permisos).

**v0.23.0 - El gramófono guarda la palabra**

Pasada transversal autorizada tras la revisión técnica completa del mod
(hallazgos en `REVISION_MOD.md`). El gramófono ya no comparte la noche con
la música vanilla del menú: se silencia el `MusicManager` mientras el
gabinete está abierto, y un watchdog reanuda la playlist tras recargas del
motor de sonido (F3+T, cambio de packs) en vez de quedar muda para siempre.

Accesibilidad nueva: Destellos reducidos (fotosensibilidad, separada de
Movimiento reducido), subtítulos reales para los ocho eventos ambientales en
los 25 idiomas y la etiqueta del slider de ambiente por fin traducida. El mod
queda inerte en servidores dedicados (eliminado el registro manual que
duplicaba eventos y podía crashear un server), se despoja de la
infraestructura vacía de Mixins, y solo reemplaza Title/Pause cuando son
clases exactamente vanilla, con apagado por config para convivir con otros
mods. La memoria del cronista se escribe fuera del hilo de render y los
temporizadores usan reloj monotónico.

El build añade dos sellos (sincronía de versión y duraciones reales de las
pistas parseadas como Vorbis) y un **workflow de CI listo para activar**
(`tools/ci/build.yml` → `.github/workflows/`): compila cada push con JDK 17
y publica el jar como artifact.

### Estado de entrega

- Implementación vigente: rama `arena/01a02f45-gripeverde` (v0.25.0).
- Base: commit `4ca038d` (merge de la PR 14, v0.22.0 ya mergeada).
- Validación estática y documental completada; árbol local y remoto limpio.
- La compilación semántica contra Forge corre en el PC del owner y, una vez
  activada, en la CI (JDK 17); la validación dentro de Minecraft sigue bajo
  KI-53.
- No existe tag/release v0.23.0: debe publicarse solo después de validar el
  cliente real (junto con el pendiente de v0.22.0).

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
- 50 descubrimientos con fichas, sellos y pistas encadenadas; algunas consecuencias son permanentes.
- Marginalia contextual del cronista: notas que responden a lo que el lector hace.
- La Sala de consulta: detras del ultimo folio de la cronica, un archivo con las fichas halladas y documentos recuperados.

### Otros
- Historial de cambios in-game con word-wrap real, accesible desde el boton "?".
- Soporte para 25 variantes de idioma (5 de ingles + 20 de espanol).
- v0.23.0: subtitulos de los eventos ambientales en los 25 idiomas, ayuda
  Destellos reducidos (fotosensibilidad), apagado del gramofono vanilla del
  menu, convivencia configurable con otros mods de menu y CI con Java 17.

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
- **Archive** - sala de consulta (50 fichas).
- **DirectJoin / EditServer / Confirm** - formularios y autorizaciones pulidos en v0.21.0.

## Build

Con Java 17, ejecutar: `.\gradlew.bat clean build`

El jar se genera en `build\libs\gripeverde-VERSION.jar`

Verificaciones activas en `processResources`:
- `verifyGripeVerdeLocalization` (JSON válido, 93 claves en paridad y
  v0.23.0 presente en los 25 changelogs).
- `verifyGripeVerdeSoundAssets` (eventos completos, referencias existentes y ningún OGG huérfano).
- `verifyGripeVerdeNoDeadCode` (símbolos retirados fuera del código productivo).
- `verifyGripeVerdeVersionSync` (`GripeVerdeMod.VERSION` == version Gradle).
- `verifyGripeVerdeMusicDurations` (duración real Vorbis de cada OGG de
  música vs `TRACK_DURATIONS_MS`, tolerancia 1.5 s).
- `verifyGripeVerdeSubtitles` (toda clave de subtítulo no vacía existe en
  los 25 idiomas).
- `verifyGripeVerdeSoundUsage` (ningún SoundEvent registrado sin uso fuera
  del allowlist legacy documentado en ModSounds).

Ademas, `tools/ci/build.yml` es un workflow de CI listo: al moverlo a
`.github/workflows/build.yml` compila cada push/PR con JDK 17 y publica el
jar como artifact. La sesion no pudo colocarlo directamente porque el token
de GitHub no tiene permiso `workflows`.

## Deploy

Repositorio local actual: `C:\Users\santi\Desktop\GripeVerde`.

Instancia de prueba de SKLauncher:
`C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`.

Antes de cada validacion se eliminan los jars anteriores de Gripe Verde, se
compila y se copia exactamente el jar de la version vigente. La version
mostrada por el reloj debe comprobarse en pantalla antes de aprobar la
entrega.

Flujo vigente para actualizar, compilar y desplegar: `tools\deploy.ps1`.
El script lee la version de `build.gradle`, hace fetch/reset de la rama de
sesion, limpia los jars antiguos, compila y copia el jar exacto.

## Idiomas soportados

**Ingles:** en_us, en_gb, en_ca, en_au, en_nz

**Espanol:** es_es, es_mx, es_ar, es_419, es_bo, es_cl, es_co, es_cr, es_do, es_ec, es_gt, es_hn, es_ni, es_pa, es_pe, es_pr, es_py, es_sv, es_uy, es_ve

## Documentacion

- `CONTRIBUTING.md` - guia corta de contribucion (branch fija, no gameplay, validacion).
- `CONTEXTO.md` - contexto maestro actual para retomar sesiones v0.23.0.
- `CHANGELOG.md` - historial narrativo de versiones orientado a jugadores (v0.10.0 → v0.23.0).
- `KNOWN_ISSUES.md` - estado tecnico, decisiones activas, errores documentados, KIs.
- `REVISION_MOD.md` - revision tecnica completa del 2026-08-23 (origen de v0.23.0) junto con las propuestas futuras pendientes de autorizacion.
- `README.md` - esta portada.

Históricos eliminados en v0.21.2: `AUDITORIA_PROFESIONAL.md`, `MICRODETALLES_EXTREMO.md`, `VALIDACION_FASE4.md` (eran referencia de polish v0.14.0, ya revertido) y `CONTEXTO_PARA_IA.md` (resumen redundante de `CONTEXTO.md`).

## Requisitos

- Minecraft 1.20.1
- Forge 47.4.20 o superior
- Java 17
- Gradle 8.8
