# KNOWN_ISSUES - Eternal Craft: Gripe Verde

La primera seccion describe el estado operativo vigente. Todas las secciones
de versiones anteriores son instantaneas historicas: sus listas de "riesgos
activos" solo conservan lo que se conocia entonces y no reabren un KI salvo
que una seccion posterior lo mencione expresamente.

## v0.26.0 - Las hojas y los faroles (2026-08-24)

### Cambios cerrados
- **Backup previo** (pedido expreso del owner, nueva regla):
  `/home/user/GripeVerde-backup-pre-v0.26.0-18e178d.bundle` (--all,
  verificado con `git bundle verify`).
- **Transicion entre interfaces** ("efectos visuales al cambiar de
  pantalla"): `DiaryTransitions` (nueva) sobre `ScreenEvent.Render.Post`:
  cortina de tinta que revela la hoja nueva de arriba abajo en ~260 ms
  (velo sepia + franja con filo bronce); con movimiento reducido, fundido
  breve sin barrido; solo pantallas del gabinete (paquete
  `client.screens`), jamas vanilla.
- **Faroles reactivos**: la mano del lector (30 px) ensancha el halo x1.5
  y aviva la llama con ease en tick; topes 255 porque `withAlpha` no
  clampea; respeta movimiento reducido y todas las escalas de GUI.
- **Crónicas reescritas** (0.24.1/0.25.0/0.25.1 x25): vocabulario de
  documento de la vida real, sin terminos de taller (hexadecimal,
  crossfade, literal, clic).

### Riesgos activos / KI nuevos
- **KI-61 (v0.26.0)**: validar en cliente real: (a) la transicion se ve al
  abrir TODA pantalla del gabinete (titulo ↔ opciones ↔ cronica ↔
  archivo ↔ sonido ↔ pausa) en GUI scale 2/3/4 y en minimal mode, sin
  velo trabado ni parpadeo raro al encadenar pantallas rapido; (b) con
  movimiento reducido solo fundido; (c) los faroles del titulo respiran
  bajo la mano sin apagarse nunca; (d) las crónicas 0.24.1-0.26.0 se leen
  naturales en ES y EN; (e) regresion nula: entrada a mundo sigue
  silenciando el ambiente; (f) los sonidos recableados (V2-resto): abrir
  multijugador suena distinto a abrir cronica/idiomas o la sala de sonido,
  el boton ">>" del gramofo no suena a pestillo, tocar el escritorio sin
  susurro devuelve un golpe sordo quedo y contenido (no se apila con
  clics rapidos), y volver al titulo desde una subpantalla suena a
  "sentarse de nuevo" (la primera apertura del arranque NO debe sonar).
- KI-53 a KI-60 siguen abiertos; el jar candidato para Agus pasa a ser
  **0.26.0**.

## v0.25.2 - El telegrama no pisa la mesa (2026-08-23)

### Cambios cerrados
- **Cartel del easter egg ilegible en GUI scale 3-4** (reporte owner): los
  clamps de Y se anulaban (uno esquivaba la mesa, el siguiente re-clavaba
  en `safeTop`); reordenados: jamas pisar botones, jamas salir de pantalla,
  y si no hay banda libre el cartel sube sobre la franja del logo. `boxW`
  con techo en el ancho real de pantalla.
- **Sello FRAUDE solapado en escala 3-4**: estaba fijado a 46 px del borde
  inferior del poster del elixir; ahora se estampa DESPUES del texto
  (`cursorY + 6`), nunca encima, con tope dentro del papel y clamp
  horizontal.
- **Docs/tooling**: deploy vigente = comandos PowerShell INLINE (sin .ps1
  nuevos); regla permanente registrada: cada entrega termina con el bloque
  PW inline de actualizar+compilar+desplegar.
- **Espejo Python reconstruido dentro del repo** (`tools/verify_mirrors.py`,
  antes vivia fuera y se perdia entre snapshots). Confesion: el sello 8
  "lint de literales numericos" anunciado en v0.24.1 quedo documentado pero
  NUNCA se escribio — entra en vigor ahora, dentro del repo, con dos
  errores propios ya corregidos (guardas de sufijo donde `"" in "fFdD"` es
  True en Python y F/D finales de literales hex son digitos, no sufijos).
  Auto-test: 25/25 casos (0xD20PL y familia rechazados); 8/8 sellos +
  balance sobre 8741 literales y los 60 Java.

### Riesgos activos / KI nuevos
- **KI-60 (v0.25.2)**: validar en cliente real con GUI scale 2, 3, 4 y
  Auto: (a) disparar un easter egg del titulo (DOCTOR, PEREIRA, sello) y
  comprobar que el telegrama NUNCA toca la mesa de botones ni se sale por
  arriba a cualquier alto de ventana; (b) con `fraude_stamp` activo (tras
  HOLLOWAY), el sello FRAUDE queda dentro del cartel del elixir sin tapar
  sus lineas en todas las escalas; (c) regresion visual nula en escala 2
  (el layout clasico queda igual).
- KI-53/54/55/56/57/58/59 siguen abiertos; el jar candidato para Agus pasa
  a ser **0.25.2**.

## v0.25.1 - El cilindro original (2026-08-23)

### Cambios cerrados
- **Lluvia sintetizada "bugeada" (reporte owner en cliente)**: el loop
  regenerado por sintesis en 0.24.0 no convencio al oido. El owner pidio
  usar "los sonidos que ya existian": se restauran los masters originales
  de lluvia (recuperado del commit 447c290) y viento, amplificados en vez
  de regrabados.
- **Lluvia**: ganancia pura, normalizada a pico ~0.83 conservando la
  cresta (~19); sin limitadores (descartado un soft-knee con NaN y picos
  >1.0; rehecho desde el master). Mezcla del loop 0.30 -> 0.45.
- **Viento**: master original amplificado a RMS ~0.085 (pico 0.80).
- **Costuras**: medidas 0.0076 (lluvia) y 0.0000 (viento): empalmaban
  limpios; no se añadio crossfade; duraciones y nombres intactos (30 s).
- `tools/audio/regenerate_menu_ambience.py` archivado con nota (ya no es
  fuente de esos loops).

### Riesgos activos / KI nuevos
- **KI-59 (v0.25.1)**: validar en cliente real: (a) la lluvia es la
  grabacion original — gotas separadas, sin estatica ni chasquidos, loop
  sin clicks en el empalme; (b) el viento es el original, presente de
  fondo sin silbidos raros; (c) el slider de ambiente mueve ambos en vivo
  y la musica sigue sin ser tapada; (d) en tormenta forzada
  (`STORM_CHANCE = 1.0f` temporal) la lluvia toma protagonismo (x1.8, osea
  0.81 de ganancia) SIN distorsionar; (e) la ciudad, los susurros y los
  pasos (contenido original x3 de 0.24.0) suenan naturales.
- KI-53/54/55/56/57/58 siguen abiertos; el jar candidato para Agus pasa a
  ser **0.25.1**.

## v0.25.0 - El que mira la tormenta (2026-08-23)

### Cambios cerrados
- **Tres easter eggs nuevos** (pedido del owner): `storm_watcher`
  (`LivingAtmosphere.flashCount()` nuevo, incrementado en ambos puntos donde
  nace un destello; la vigilia marca linea base al abrirse en noche de
  tormenta y otorga la ficha al tercero; con destellos reducidos no se
  regala), `drop_catcher` (hit-test `rainDropAt`/`scareDrop` sobre las 40
  gotas en vuelo; plink de aguja; zona de botones excluida para no tragar
  clics) y `code_doctor` (palabra DOCTOR en el telegrafo oculto: susurro +
  campana baja + contador `doctor_signed`). Archivo: 52 fichas; notas
  N_FLASH3/N_DROP/N_DOCTOR; nombres y pistas bilingues en el registry (sin
  claves lang nuevas: 93).
- **Tooling (deploy.ps1)**: el script leia la version de `build.gradle`
  ANTES de sincronizar la rama; cuando la version cambiaba entre local y
  remoto, gradle compilaba el jar nuevo pero la copia buscaba el del release
  anterior (`No se genero gripeverde-0.24.0.jar` tras un BUILD SUCCESSFUL
  al desplegar 0.25.0). Corregido el archivo existente: la version se lee
  despues del reset y el jar se localiza por glob. Sin .ps1 nuevos: el
  owner opera con comandos inline.

### Riesgos activos / KI nuevos
- **KI-58 (v0.25.0)**: validar en cliente real: (a) con tormenta forzada
  (`MenuAmbience.STORM_CHANCE = 1.0f` temporal) quedarse tres relampagos en
  el titulo otorga `storm_watcher` y su nota; que NO se otorgue con
  destellos reducidos; (b) clic sobre una gota en vuelo la devuelve al cielo
  con plink y otorga `drop_catcher`; los clics en botones nunca se tragan;
  (c) escribir DOCTOR en la vigilia otorga `code_doctor` con susurro y
  campana; (d) la Sala de consulta muestra 52 fichas y las tres nuevas
  tienen nombre y pista en ES y EN; (e) los huevos anteriores (sello roto,
  diapason, telegramas) siguen desbloqueandose igual.
- KI-53/54/55/56/57 siguen abiertos. Candidata a distribuir a Agus: la
  **0.25.0**, que incluye el crash fix, el ambiente reparado, lo visible y
  estos tres huevos.

## v0.24.1 - La errata del cronista (2026-08-23)

### Cambios cerrados
- **Build roto en 0.24.0 (errata propia)**: `PaperTexture.java:95` declaro
  `new java.util.Random(0xD20PL)` con una P —no es digito hexadecimal— y
  `compileJava` fallo con `malformed floating-point literal` (3 errores) en
  el primer `deploy.ps1` del owner. El sandbox no tiene JDK y el espejo
  solo verificaba balance de llaves: por eso salio sellada. Semilla corregida
  a `0xCA1DAL`; comportamiento identico (gotas deterministas, otra
  secuencia) y sin ningun otro cambio funcional respecto a 0.24.0.
- **Sello 8 del espejo Python**: nuevo `verifyJavaLiterals` —lint de todos
  los literales numericos de los 60 .java (hex/binario/octal/decimal/
  flotante, sufijos fFdDlL, octales con 8/9, floats hex con sufijo ilegal)
  tras retirar comentarios y cadenas preservando numeros de linea.
  Demostrado: detecta `0xD20PL` y rechaza la errata antes de sellar.

### Riesgos activos / KI nuevos
- **KI-57 (v0.24.1)**: validar en cliente real: (a) `deploy.ps1` compila y
  copia `gripeverde-0.24.1.jar`; (b) el reloj del Title muestra v0.24.1;
  (c) las gotas siguen resbalando por los pergaminos (patron distinto,
  misma densidad) y se aquietan con movimiento reducido; (d) repetir
  INTEGRO el checklist de KI-56, que quedo pendiente porque la 0.24.0
  nunca llego a existir como jar.
- KI-53/KI-54/KI-55/KI-56 siguen abiertos y sin cambios. Para Agus: enviar
  **0.24.1** (incluye el hotfix del crash, el ambiente reparado y todo lo
  visible; la 0.24.0 no compilo jamas).

## v0.24.0 - El viento vuelve a soplar (2026-08-23)

### Cambios cerrados
- **Ambiente mudo (diagnostico)**: no era el motor ni los relojes — eran los
  archivos. `wind_urban_loop.ogg` media RMS 0.0006 (-64 dB; a la ganancia
  0.35 del codigo salia a ~-73 dB, inaudible), `rain_...` 0.026, `city_...`
  0.034, `ambient_occasional` 0.0056, `footsteps_distant_1` 0.0077. Musica,
  UI y raros siempre se oyeron: por eso el sintoma era "sin ambiente" con
  clics y truenos presentes.
- **Activos regenerados** con `tools/audio/regenerate_menu_ambience.py`
  (nuevo; sintesis procedural con semilla fija, idempotente por umbral RMS;
  crossfade de 2.5 s inicio/fin para loops sin chasquido; picos con margen
  para el sobresalto del codificador Vorbis): viento y lluvia sintetizados a
  RMS ~0.12-0.13; ciudad x3; susurro x3; paso 1 x3.
- **V1**: Changelog salta a la entrada de la version instalada la primera
  vez que se abre tras actualizar y la sello de lacre "NUEVO/NEW" la marca
  durante la visita (`ChroniclerMemory.lastSeenVersion`, campo tolerante con
  memorias viejas: null -> "" en accesores).
- **V3**: Noche de tormenta (~14% de sesiones; sorteo por JVM): lluvia
  visual x2 con mas viento y brillo, relampagos cada 3.5-9.5 s, mezcla
  ambiente lluvia x1.8 / viento x1.25 / ciudad x0.8, capa rara cada 14-26 s
  con trueno casi garantizado, velo oscuro en titulo y pergaminos; ficha
  `storm_night` + marginalia. Respeta reduceFlashes y reducedMotion.
- **V5**: 7 gotas (14 en tormenta) resbalan por todos los pergaminos via
  `PaperTexture`; quietas con movimiento reducido.
- **Easter eggs nuevos**: `seal_breaker` (9 toques rapidos al sello ->
  esquirlas de lacre + `ui.paper_fall`, que sale del allowlist del sello de
  uso) y `sound_keeper` (diapason al margen del regulador de ambiente ->
  `ui.success`, segundo evento que sale del allowlist). El archivo pasa a
  50 fichas; `N_MANY_CARDS`/`N_ARCHIVE_COMPLETE` computan la cifra desde
  `Discoveries.ALL.length`.
- **Higiene**: `MenuAmbience.stop()` al entrar a un mundo (los loops ya no
  siguen sonando bajo el gameplay) y `start()` se niega con nivel cargado.

### Riesgos activos / KI nuevos
- **KI-56 (v0.24.0)**: validar en cliente real: (a) el ambiente se oye con
  claridad sin tapar la musica (viento presente, lluvia de fondo, campanas
  de la ciudad), y el slider de ambiente los mueve en vivo; (b) una sesion
  forzada de tormenta (cambiar temporalmente `STORM_CHANCE` a 1.0f en
  pruebas o repetir aperturas) muestra lluvia doble, relampagos frecuentes
  y la ficha `storm_night`; (c) la Cronica se abre en 0.24.0 con sello
  NUEVO solo en la primera visita; (d) las gotas resbalan por los
  pergaminos y se quedan quietas con movimiento reducido; (e) romper el
  sello a golpes suena a papel y suma ficha; el diapason suena y suma
  ficha; (f) al entrar a un mundo la calle calla y al volver reaparece;
  (g) las voces/sliders vanilla de la lista heredada siguen intactos.
- KI-53/KI-54/KI-55 siguen abiertos y sin cambios (cliente real + reenvio
  del jar corregido a Agus, que sigue en 0.22.0: conviene enviarle 0.24.0,
  que incluye el hotfix del crash y el ambiente reparado).

## v0.23.1 - Sellos y relojería (2026-08-23)

### Cambios cerrados
- Nuevo sello `verifyGripeVerdeSubtitles`: toda clave `subtitle` no vacia de
  sounds.json debe existir, no vacia, en los 25 lang.
- Nuevo sello `verifyGripeVerdeSoundUsage`: falla si un SoundEvent
  registrado no aparece en el codigo. Los 10 eventos legacy sin uso
  (censo del informe) quedan en allowlist grandfathered, documentados en
  la javadoc de ModSounds, hasta decidir conexion o retirada; el sello
  avisa si alguno deja de ser legacy.
- ChroniclerMemory: SCHEMA_VERSION + pipeline migrate() documentado (pasos
  N→N+1 idempotentes; esquema mas nuevo se conserva con aviso) y sanitize
  de bounds (contadores negativos, firstVisitDay incoherente, nulls en
  map, caps de listas).
- MenuAmbience migra a reloj monotonico nanoTime junto con el rastreo de
  calle de LivingAtmosphere (coche del sepulturero): sus unicos puntos de
  contacto para `lastBellMs` ya usan el mismo reloj.
- CI: `tools/ci/activar-ci.ps1` confirma 2 vias bloqueadas en sesion (git
  push y API, 403/workflows-permission) y activa la CI con credenciales
  locales del owner en un paso.
- Bump: 0.23.0 -> 0.23.1. Conteos sin cambios: 60 Java, 25 lang (93
  claves), 25 cronicas, 35 eventos, 42 OGG.

### Riesgos activos / KI nuevos
- **KI-55 (v0.23.2)**: validar en cliente real el hotfix del crash del
  easter egg: clic repetido en el sello del titulo con movimiento normal
  (maquina de escribir) ya no produce `ArrayIndexOutOfBoundsException` en
  ningun fotograma; el telegrama aparece y desaparece con normalidad.
  Pendiente operativo: reenviar el jar v0.23.2 al jugador que reporto el
  crash (su instancia sigue en v0.22.0) y confirmar que su sesion arranca.
  El reporte original (2026-08-23): 4 crashes consecutivos en
  `DiaryTitleScreen.drawEasterEggMessage`, Forge 47.4.10, Java 17.0.8.
- **KI-54 (v0.23.1)**: con CI activada (activar-ci.ps1) confirmar build
  verde en la rama y luego en cliente: hearse/cuervos/campana se comportan
  igual tras la migracion a nanoTime; la memoria recuperada sigue
  escribiendose y recuperandose (sanitize/migrate intactos); los 7 sellos
  pasan locales.
- KI-53 sigue abierto para la parte de cliente de v0.23.0 (audio vanilla
  callado, watchdog F3+T, toggle destellos, subtitulos, Ctrl+0 off,
  reemplazos exactos).

---

# HISTORIAL DE VERSIONES E INSTANTANEAS (NO OPERATIVO)

Todo lo que sigue se conserva para trazabilidad. Los estados, riesgos, tags,
rutas y planes llamados "actuales", "activos" o "vigentes" pertenecen a la
version bajo la que fueron escritos; no sustituyen el estado v0.23.1 de la
primera seccion.

## v0.23.0 - El gramófono guarda la palabra (2026-08-23)

## v0.23.0 - El gramófono guarda la palabra (2026-08-23)

Entrega transversal pedida expresamente por el owner tras la revision
tecnica completa del mod (hallazgos en REVISION_MOD.md). Backup externo
previo: `/home/user/GripeVerde-backup-pre-v0.23.0-4ca038d.bundle`.

### Cambios cerrados
- Se elimina el registro manual de `ScreenEventHandler` (doble suscripcion al
  bus FORGE y crash latente en servidores dedicados por carga de clases
  client-only); la clase se autorregistra solo en cliente y el mod queda
  inerte en servidor. `mods.toml` suma `displayTest="IGNORE_ALL_VERSION"` e
  `issueTrackerURL` con la URL real del repo.
- Se suprime la musica vanilla de menu mientras el gabinete esta abierto
  (guardia por tick sobre MusicManager) y se anade un watchdog: si el motor
  de sonido mata la instancia (F3+T, recarga de packs), la playlist se
  reanuda sola en lugar de quedar muda hasta el cambio de pantalla.
- Nueva ayuda de accesibilidad "Destellos reducidos" (fotosensibilidad,
  independiente de Movimiento reducido) y subtitulos reales para los ocho
  eventos ambientales en los 25 idiomas. La etiqueta del slider de ambiente
  deja de estar hardcodeada en castellano.
- Escritura de la memoria del cronista fuera del hilo de render (executor
  monohilo, cierre ordenado con awaitTermination en el shutdown hook) y
  temporizadores con reloj monotonico (nanoTime) en musica, cooldowns UI y
  instancias de sonido.
- El reemplazo de Title/Pause solo ocurre con clase exactamente vanilla
  (otros mods de menu ya no son barridos) y ambos reemplazos tienen apagado
  por config (`integration.*`); el atajo oculto Ctrl+0 pasa a ser opt-in
  (`debug.debugShortcut`, por defecto off).
- Retirados: infraestructura de Mixins vacia (JSON, plugin, annotation
  processor, refmap, entrada mods.toml, repo sponge), namespace GameTest sin
  tests, y los metodos muertos de UiSounds (setHoverSound/setClickSound/
  hover).
- Dos sellos nuevos de build: `verifyGripeVerdeVersionSync` (VERSION ==
  project.version) y `verifyGripeVerdeMusicDurations` (parser Vorbis propio:
  granule/rate real de cada OGG vs TRACK_DURATIONS_MS, tolerancia 1.5 s).
  Conteo de claves lang: 83 -> 93 (10 nuevas: toggle destellos + tooltip +
  8 subtitulos).
- Nueva CI lista para activar en `tools/ci/build.yml`: JDK 17 Temurin +
  Gradle cacheado, `./gradlew clean build` en cada push/PR y el jar publicado
  como artifact. Queda como plantilla porque el token de la sesion no tiene
  permiso `workflows`; se activa moviendola a `.github/workflows/build.yml`.
- Deploy parametrizado en `tools/deploy.ps1` (lee la version de
  build.gradle; documentado en README).
- Conteos: 60 clases Java, 25 lang (93 claves), 25 cronicas, 35 eventos,
  42 OGG. Bump: 0.22.0 -> 0.23.0.

### Riesgos activos / KI nuevos
- **KI-53 (v0.23.0)**: validar en cliente real con Java 17: (a) la musica
  vanilla del menu ya no suena bajo el gramofono (esperar >60 s en Title);
  (b) tras F3+T la musica propia vuelve sola; (c) el toggle Destellos
  reducidos apaga los relampagos sin congelar la lluvia; (d) los subtitulos
  aparecen con los subtitulos de Minecraft activos; (e) Ctrl+0 ya no
  desconecta por defecto; (f) Title/Pause se siguen reemplazando y ceden si
  otro mod los ocupa; (g) la memoria se sigue escribiendo y recuperando bien
  en uso intenso (executor async); (h) el build pasa los cinco sellos.
- El sandbox local sigue sin Java 17: la compilacion semantica queda
  delegada al PC del owner y, cuando se active, a la CI de `tools/ci/`;
  `java-parser` solo valida sintaxis. KI-52 queda absorbido por KI-53 para la parte de cliente;
  el tag/release v0.23.0 (y el pendiente de v0.22.0) siguen bloqueados hasta
  validar en Minecraft real.

---

# HISTORIAL DE VERSIONES E INSTANTANEAS (NO OPERATIVO)

Todo lo que sigue se conserva para trazabilidad. Los estados, riesgos, tags,
rutas y planes llamados "actuales", "activos" o "vigentes" pertenecen a la
version bajo la que fueron escritos; no sustituyen el estado v0.23.0 de la
primera seccion.

## v0.22.0 - La vigilia queda sellada (2026-08-23)

### Cambios cerrados
- Polvo atmosférico trasladado de render por FPS al tick fijo de 20 Hz.
- Música y ambiente consolidados en una única llamada por tick para todas
  las pantallas del gabinete.
- Movimiento reducido expuesto en Accesibilidad y ampliado al cielo, humo,
  clima, partículas, grano y destellos, sin retirar detalles permanentes.
- Toggles con narración de etiqueta y estado.
- Memoria JSON con temporal durable, backup de última versión válida,
  reemplazo atómico, recuperación y preservación del archivo corrupto.
- RNG determinista reutilizado en papel y ventanas; mismo resultado visual
  sin nueve asignaciones de `Random` por render de papel ni una por capa de
  ventanas.
- Verificación de 25 JSON/83 claves/25 changelogs y registro sonoro completo.
- Version bump: 0.21.31 -> 0.22.0. Conteo Java sin cambios: 60 clases.

### Estado de entrega
- Implementacion final: `a1decf304b0c7236d624d4637bd3b5dcaa3961db`,
  publicada en `arena/01a02cee-gripeverde` para revision mediante la PR 14.
- Validacion estatica completada: 60 Java sin errores sintacticos segun
  `java-parser`, 25 locales x 83 claves, 25 changelogs, 35 eventos, 42
  referencias sonoras y 42 OGG.
- `java-parser` no sustituye la compilacion semantica de Gradle/Forge.
- No se creo tag ni release v0.22.0; ambos quedan bloqueados por KI-52.

### Riesgos activos / KI nuevos
- **KI-52 (v0.22.0)**: el sandbox no dispone de JDK/JRE 17, por lo que el
  build Forge y la prueba dentro de Minecraft deben ejecutarse en cliente
  real. Validar Title y subpantallas, escalas GUI 1-4, toggle de Movimiento
  reducido, narración, continuidad de audio y recuperación controlada de
  `gripeverde-memory.json` tanto desde `gripeverde-memory.tmp` como desde
  `gripeverde-memory.backup.json`.
- KI-51 queda absorbido por KI-52; siguen siendo obligatorias la escena
  completa, todos los faroles visibles y la ausencia del kiosco/Heraldo a
  FPS altos y bajos.

---

# HISTORIAL DE VERSIONES E INSTANTANEAS (NO OPERATIVO)

Todo lo que sigue se conserva para trazabilidad. Los estados, riesgos, tags,
rutas y planes llamados "actuales", "activos" o "vigentes" pertenecen a la
version bajo la que fueron escritos; no sustituyen el estado v0.22.0 de la
primera seccion.

## v0.21.31 - La vigilia permanece entera (2026-08-23)

### Cambios cerrados
- Eliminado, no solo desactivado, el modo de pocos FPS y todo su muestreo.
- El Title dibuja siempre niebla, calle completa, faroles, reflejos,
  particulas y utileria; el grano ya no cambia segun los FPS.
- Eliminados los restos del apagado de faroles, su ciclo erratico de
  atenuacion y el debilitamiento persistido de `avenue_darkened`; las llamas,
  reflejos y polillas ya no se omiten.
- Identificado el supuesto canasto como el kiosco del Heraldo y eliminado por
  completo: dibujo, hitbox, click, overlay, clase y ficha de descubrimiento.
- El archivo pasa de 47 a 46 fichas y su contador ignora ids retirados que
  puedan seguir guardados en memorias de versiones anteriores.
- Version bump: 0.21.30 -> 0.21.31. Conteo Java: 61 -> 60 clases.

### Riesgos activos / KI nuevos
- **KI-51 (v0.21.31)**: validar en cliente real el Title con FPS por encima y
  debajo de 30 y escalas GUI 1-4. La escena debe permanecer completa, todos
  los faroles deben seguir encendidos y el kiosco/Heraldo no debe aparecer
  ni responder a clicks.
- KI-50 queda reemplazado por KI-51: ya no existe un umbral low-end que
  comparar.

---

## v0.21.30 - La calle permanece (2026-08-23)

### Cambios cerrados
- Corregido el modo low-end del menu principal: ya no omite toda la capa
  `renderStreetLevel` cuando los FPS bajan de 30.
- En low-end se dibuja siempre la base barata `renderStreet`; solo se
  omiten la utileria y los efectos costosos del nivel de calle.
- Las subpantallas Diary tambien conservan siempre la calle base,
  independientemente del muestreo de FPS.
- Version bump: 0.21.29 -> 0.21.30.

### Riesgos activos / KI nuevos
- **KI-50 (v0.21.30)**: validar en cliente real el Title por encima y por
  debajo de 30 FPS y en las cuatro escalas GUI; la calzada debe permanecer
  visible mientras los efectos costosos cambian segun el modo low-end.

---

## v0.21.24 - Ajustes pedidos en cliente (2026-08-22)

### Cambios cerrados
- Sonido de madera/puerta al abrir Options: eliminado (UiSounds.close).
- Cajon del gabinete (llave amarilla + vitrina) eliminado por completo.
- Easter egg de los tres golpes (knock) eliminado.
- Easter egg del doblado del poster de CUARENTENA eliminado (poster
  estatico).
- Titulo CUARENTENA y sellos de tinta: sin rojo sombreado (tinta normal).
- Libro de visitas de Multiplayer: mas visible y hitbox mayor.
- Creditos: etiquetas en titulo normal; contenido de atribuciones
  pendiente de definicion por el usuario.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Version bump: 0.21.23 -> 0.21.24.

### Riesgos activos / KI nuevos
- **KI-49 (v0.21.24)**: validar en cliente — gabinete sin cajon ni sonido,
  poster estatico, libro de visitas clicable, creditos legibles.
- **PENDIENTE USUARIO**: textos definitivos de atribuciones (Cronista,
  Director, Planos) y mejoras nuevas (preguntar antes de interfaces).
- KI-48, KI-47, KI-46, KI-45, KI-44, KI-41/43, KI-39/40 pendientes.
  KI-29 (pistas MenuMusic) requiere OGGs del usuario.

---

## v0.21.23 - Se reconstruye desde el registro previo (2026-08-22)

### Cambios cerrados
- Backup localizado: commit `39e4720` (v0.17.2, previo a la alteracion
  v0.19.0). Excluidas v0.19.0/v0.20.0/v0.21.0.
- 17 pantallas Diary restauradas a su forma pre-alteracion (cabeceras
  propias, sin carteles/rails).
- Restaurados: DiaryMusicSlider, DiaryAmbienceSlider, DiaryPackSelectionScreen.
- Eliminada: DiaryVideoSettingsScreen (nacio en la alteracion).
- DiaryChrome: anadidos drawRegistryTag* + STEEL_NEAR/DEEP/EDGE.
- Lang: packs.subtitle restaurada en los 25 idiomas.
- Re-aplicado lore: triggers Title, cadena de fichas Archive, sello
  SIGILLUM + campana.
- Re-eliminados: drawGreeting, drawLedgerDetails/scrollToSection,
  drawRowBands/openTelemetry.
- Lista negra del build ajustada (DiaryMusicSlider/DiaryAmbienceSlider
  fuera, DiaryVideoSettingsScreen dentro).
- Conteos: 61 clases, 25 lang (81 claves), 35 eventos, 42 OGG.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Version bump: 0.21.22 -> 0.21.23.

### Riesgos activos / KI nuevos
- **KI-48 (v0.21.23)**: validar en cliente — recorrer todas las salas con
  la identidad v0.17.2 + lore v0.21.x (especialmente Options, Sound,
  Multiplayer, Archive con SIGILLUM, Title con fichas).
- KI-47, KI-46, KI-45, KI-44, KI-41/43, KI-39/40 pendientes. KI-29
  (pistas MenuMusic) requiere OGGs del usuario.

---

## v0.21.21 - Se termina la deuda de los no-op (2026-08-22)

### Cambios cerrados
- 30 llamadas no-op a drawInsetCard/drawInnerRail/drawValueRow + 3 a
  drawOptionsGroup eliminadas (11 pantallas).
- DiaryChrome: eliminados los 3 helpers no-op + CARD_BASE/CARD_BORDER.
- Variables/bloques muertos fuera: Language frame*, Controls leftX/railX/
  workW, Options content*, Sound rail*/listFrame*, RAIL_W, STATION_W.
- Changelog: drawRevisionFooter (pie de registro v0.21.0) eliminado.
- Fix scrollbars Chat/Online/Sound -> borde derecho del panel (referencias
  a variables eliminadas).
- 14 imports muertos + newline-final + trailing whitespace.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Version bump: 0.21.20 -> 0.21.21.

### Riesgos activos / KI nuevos
- **KI-47 (v0.21.21)**: validar en cliente — Chat/Online/Sound con
  scrollbar en el borde derecho y listas intactas.
- KI-46, KI-45, KI-44, KI-41/43, KI-39/40 pendientes. KI-29 (pistas
  MenuMusic) requiere OGGs del usuario.

---

## v0.21.20 - La tinta pesa menos (2026-08-22)

### Cambios cerrados
- `ExpedientHeader`: sobrecarga simple; 16 pantallas migradas; ~150 lineas
  de argumentos muertos eliminadas.
- Metodos muertos fuera: computeControlDiscipline, normalizeFov,
  computeVisibleRatio, getVisualMixLevel, getCrossfadeVisual.
- Variables muertas fuera: rail completo de Options (drawOptionsRail +
  campos), bloques no-op de Online/Chat, headerBottom sin asignacion en
  Language/Multiplayer/Options/Chat, ready (DirectJoin/EditServer), barra
  stillness + highlight + haloC (Pause; se restauro el cierre de metodo),
  lineH/line (Credits), angle (Poster), h3 (Skyline).
- Options gridTop 77 -> 66.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Version bump: 0.21.19 -> 0.21.20 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-46 (v0.21.20)**: validar en cliente — salas con titulo + contenido
  intactos tras la limpieza (especialmente Pause, Options, Chat y Online).
- KI-42 (deuda): quedan call sites no-op de drawInsetCard/drawInnerRail/
  drawValueRow en varias pantallas (no dibujan, compilan); limpieza total
  en pasada futura.
- KI-45, KI-44, KI-41/43, KI-39/40 pendientes. KI-29 (pistas MenuMusic)
  requiere OGGs del usuario.

---

## v0.21.19 - Se corrigen scrolls y posiciones tras la reversion (2026-08-22)

### Cambios cerrados
- Multiplayer: LIST_TOP_OFFSET 84 -> 52 (lista debajo de la cabecera).
- Sound: listTop 45 -> 58 (la lista ya no tapa el titulo).
- Chat: listTop 88 -> 62. Online: listTop 92 -> 62.
- Controls: gridTop 92 -> 62.
- Archive: campo de busqueda a la esquina superior derecha (panelY+18) y
  contador "N / 47" debajo (panelY+38) — antes pasaba sobre la grilla.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Version bump: 0.21.18 -> 0.21.19 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-45 (v0.21.19)**: validar en cliente — listas/scrolls y botones
  alineados en Multiplayer, Sound, Chat, Online, Controls y Archive (4
  escalas GUI).
- KI-44 (v0.21.18), KI-42 (deuda no-op), KI-41/43, KI-39/40 pendientes.
  KI-29 (pistas MenuMusic) requiere OGGs del usuario.

---

## v0.21.18 - Se restaura lo que no era de la version revertida (2026-08-22)

### Cambios cerrados
- Pickaxe con historia completa: se fecho cada elemento sacado en
  v0.21.14-v0.21.17.
- Restaurado: `DiaryProgressBar.draw` (cuerpo vivo, v0.12.2), medidores
  Pacing (Video) y registries (Language) v0.21.4, sello SIGILLUM +
  `drawCompletionSeal` (v0.21.13), `drawOfficialFrame` de Credits (v0.14.1).
- Eliminadas las llamadas de medidores v0.18-v0.21.0 (Options, Sound,
  Controls, Video FOV, Accessibility, Pause, Skin) para que no dibujen con
  el cuerpo restaurado.
- Confirmado fuera (v0.18-v0.21.0 o pedido explicito): header completo,
  tarjetas/railes, RECOVERED PAPERS, footer changelog, racks Chat/Online,
  HOSPEDAJE ELEGIDO, saludo Title, ticker Herald.
- Lista negra: quitados SIGILLUM/drawCompletionSeal (vuelven a existir).
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Version bump: 0.21.17 -> 0.21.18 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-44 (v0.21.18)**: validar en cliente — sello al completar 47 fichas,
  medidor Pacing en Video, registros en Lenguas; resto sin carteles.
- KI-42 (deuda): limpiar call sites no-op restantes (drawInsetCard/
  drawInnerRail/drawValueRow).
- KI-41/43, KI-39/40, KI-38... pendientes. KI-29 (pistas MenuMusic)
  requiere OGGs del usuario.

---

## v0.21.17 - Se retiran los medidores y el resto de carteles (2026-08-21)

### Cambios cerrados
- `DiaryProgressBar.draw` -> no-op (medidores con etiqueta fuera en 8
  pantallas; firma intacta).
- Archive: fuera "RECOVERED PAPERS" + divisor, y el sello SIGILLUM
  (v0.21.13) + `drawCompletionSeal`.
- Changelog: fuera `drawLedgerDetails` (footer + sello + referencia).
- Options: `drawOptionsGroup` -> no-op. Credits: `drawOfficialFrame` ->
  no-op.
- Grep global de textos hardcodeados directos = 0.
- Lista negra del build ampliada.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Version bump: 0.21.16 -> 0.21.17.

### Riesgos activos / KI nuevos
- **KI-41/43 (v0.21.16/17)**: validar en cliente — pantallas con titulo +
  contenido + mandos, sin medidores ni carteles.
- **KI-42 (deuda)**: limpiar los call sites no-op restantes
  (drawInsetCard/drawInnerRail/drawValueRow/DiaryProgressBar/
  drawOptionsGroup/drawOfficialFrame).
- KI-39/40 pendientes; KI-38, KI-37, KI-36, KI-35, KI-34, KI-32/33,
  KI-27/28/30/31 pendientes. KI-29 (pistas MenuMusic) requiere OGGs del
  usuario.

---

## v0.21.16 - Se retiran los carteles de interfaz (2026-08-21)

### Cambios cerrados
- BACKUP: tag `backup-pre-reversion-v0.21.15` (GitHub) + tar local.
- `ExpedientHeader`: solo titulo + divisor (expediente/referencia/subtitulo/
  flavor/estado fuera; firma intacta).
- `DiaryChrome.drawInsetCard`/`drawInnerRail`/`drawValueRow`: no-op (firma
  intacta; llamadas restantes compilan sin dibujar — deuda de limpieza).
- Textos/metodos de cartel eliminados en Chat, Online, Archive, Sound,
  Controls, Language y Skin (lista completa en CONTEXTO.md addendum).
- Conservado: todo lo funcional y el lore v0.16.
- Lista negra del build ampliada.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Version bump: 0.21.15 -> 0.21.16 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-41 (v0.21.16)**: validar en cliente — las 16 pantallas con titulo
  simple, sin carteles ni solapes; botones/listas intactos.
- **KI-42 (deuda)**: limpiar las llamadas no-op restantes a
  drawInsetCard/drawInnerRail/drawValueRow en las pantallas.
- KI-39/40 (v0.21.14/15) pendientes; KI-38, KI-37, KI-36, KI-35, KI-34,
  KI-32/33, KI-27/28/30/31 pendientes. KI-29 (pistas MenuMusic) requiere
  OGGs del usuario.

---

## v0.21.15 - Se retiran los sellos de estado (2026-08-21)

### Cambios cerrados
- `ExpedientHeader`: pill de estado eliminado en todas las pantallas Diary
  (status conservado en la firma). Flavor a ancho completo con truncado.
- `DiaryOptionsScreen`: pill "FIELD READY / LISTO PARA CAMPO" eliminado.
- `DiaryChrome.drawStatusPill` eliminado (0 usos); lista negra ampliada.
- Confirmado por pickaxe: los sellos/elementos venian de v0.18.0/v0.21.0;
  el unico reciente era el ticker (v0.21.9, revertido).
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Version bump: 0.21.14 -> 0.21.15 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-39 (v0.21.14+15)**: validar en cliente — menu limpio, formularios sin
  sellos ni solapes, lista de servidores debajo de la cabecera, guestbook
  clickeable.
- **KI-40 (pendiente de captura)**: "errores en el gabinete" (Options) sin
  detalle; se quito el sello READY que se solapaba. Si persiste, pedir
  descripcion puntual.
- KI-38, KI-37, KI-36, KI-35, KI-34, KI-32/33, KI-27/28/30/31 pendientes.
  KI-29 (pistas MenuMusic) requiere OGGs del usuario.

---

## v0.21.14 - El gabinete vuelve a respirar (2026-08-21)

### Cambios cerrados
- Title: eliminado el saludo de sesion que solapaba los botones
  (`drawGreeting` + campos + asignaciones; marginalias/descubrimientos
  intactos).
- Multiplayer: `LIST_TOP_OFFSET` 50 -> 84 (el server ya no tapa la
  cabecera); eliminadas la tarjeta HOSPEDAJE ELEGIDO y el ticker del
  Heraldo.
- Confirm: eliminados el cartel ORDER/ORDEN, la mano autorizante y el pill
  PENDIENTE; mensaje de confirmacion en tinta normal.
- ExpedientHeader: flavor truncado con ellipsis antes del pill de estado
  (arregla solapes REGISTRABLE/INCOMPLETO/LISTO/READY).
- Lista negra del build ampliada (`drawGreeting`, `drawSelectionLedger`,
  `drawHeraldTicker`).
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Version bump: 0.21.13 -> 0.21.14 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-39 (v0.21.14)**: validar en cliente — menu limpio, lista de
  servidores debajo de la cabecera, confirmacion sin cartel/orden/mano,
  formularios sin solape de sellos, y el libro de visitas clickeable.
- **KI-40 (pendiente de captura)**: el usuario reporto "el libro de
  visitas dejo de funcionar" y "errores en el gabinete" (Options) sin que
  las capturas llegaran al sandbox. Se limpiaron superposiciones; si
  persiste, pedir captura por otra via o descripcion puntual.
- KI-38, KI-37, KI-36, KI-35, KI-34, KI-32/33, KI-27/28/30/31 pendientes.
  KI-29 (pistas MenuMusic) requiere OGGs del usuario.

---

## v0.21.13 - El expediente se cierra (2026-08-21)

### Cambios cerrados
- Al completar las 47 fichas: footer del Archive cambia a "el archivo esta
  completo" y se dibuja un sello de cera dorado (SIGILLUM) procedural.
- Disparo unico persistente `cabinet_complete` + marginalia final
  `N_ARCHIVE_COMPLETE` + campana grave.
- Changelog in-game (25 .md): SI (manifestacion observable: el archivo se
  sella).
- Version bump: 0.21.12 -> 0.21.13 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-38 (v0.21.13)**: validar en cliente — completar 47/47 (o forzar el
  json de memoria) y ver el sello dorado + marginalia + campana.
- KI-37, KI-36, KI-35, KI-34, KI-32/33, KI-27/28/30/31 pendientes.
  KI-29 (pistas MenuMusic) requiere OGGs del usuario. KI-20/21/24/25 sin
  cambios.

---

## v0.21.12 - El marco de la pausa queda en su sitio (2026-08-21)

### Cambios cerrados
- `DiaryPauseScreen`: metodo privado `drawCornerOrnaments` inline en su
  unico uso y eliminado (marco de oro identico visualmente).
- Audit global: cero campos/metodos privados sin uso en src/main/java.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Version bump: 0.21.11 -> 0.21.12 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-37 (v0.21.12)**: validar en cliente — marco de oro del diario de
  pausa identico (solo refactor interno).
- KI-36, KI-35, KI-34, KI-32/33, KI-27/28/30/31 pendientes. KI-29 (pistas
  MenuMusic) requiere OGGs del usuario. KI-20/21/24/25 sin cambios.

---

## v0.21.11 - La sala respira bajo la mano (2026-08-21)

### Cambios cerrados
- Micro-animacion de fichas en la Sala de consulta (`EasedValue`: alza 2px
  + aclarado, hover suavizado entre fichas).
- `DiaryProgressBar` ALERT con pulso suave 0.8-1.0 (solo ese estilo).
- `ChroniclerMemory.save()` diferido (1 escritura/2s) + `flushForShutdown()`
  via `Runtime.addShutdownHook` en `GripeVerdeMod`.
- Grano de pelicula low-end: intervalo 90ms -> 300ms cuando FPS < 30.
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Version bump: 0.21.10 -> 0.21.11 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-36 (v0.21.11)**: validar en cliente — alza suave de fichas, pulso
  ALERT sutil, y que la memoria sobreviva al cerrar el juego (flush en
  shutdown).
- KI-35, KI-34, KI-32/33, KI-27/28/30/31 pendientes. KI-29 (pistas
  MenuMusic) requiere OGGs del usuario. KI-20/21/24/25 sin cambios.

---

## v0.21.10 - La estacion parpadea al cambiar (2026-08-21)

### Cambios cerrados
- 15 constantes de color muertas eliminadas (8 de `DiaryTheme`, 7 de
  `DiaryChrome`) tras audit de uso global.
- `DiaryOnlineOptionsScreen`: medidor de alcance con transicion suave
  (interpolacion en tick; antes el valor saltaba al togglear opciones).
- Lista negra de `verifyGripeVerdeNoDeadCode` ampliada (13 simbolos de
  v0.21.7-0.21.9 + 15 constantes de color).
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Version bump: 0.21.9 -> 0.21.10 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-35 (v0.21.10)**: validar en cliente — el medidor de la estacion
  transiciona suave al togglear "avisos"/"listado".
- KI-34 (v0.21.9), KI-32/33, KI-27/28/30/31 pendientes. KI-29 (pistas
  MenuMusic) requiere OGGs del usuario. KI-20/21/24/25 sin cambios.

---

## v0.21.9 - La tinta pesa menos por frame (2026-08-21)

### Cambios cerrados
- `Discoveries.isEnglish()` con cache de 250ms (antes: consulta al
  LanguageManager decenas de veces por frame). El cambio de idioma recarga
  resources y el cache expira solo.
- `DiaryChrome.drawCenteredString` nuevo; eliminados los helpers privados
  duplicados en Archive, HiddenCipher, Herald y PosterRenderer (26
  llamadas unificadas).
- `Herald.headlineToday()` extraido; `DiaryMultiplayerScreen` muestra el
  titular del dia junto al libro de visitas (ticker simetrico).
- Eliminados campos muertos: `BLOOD_RNG`/`STAMP_RNG` (PosterRenderer),
  `PAD_Y` (Changelog), `LOGGER`/`frameLogCounter` (Multiplayer),
  `PANEL_MIN_H`/`MARGIN_BOTTOM_IDEAL`/`ORN_TITLE_TOP`/`ORN_SUBTITLE_TOP`
  (Pause).
- Eliminados metodos muertos: `scrollToSection` (Changelog),
  `drawRowBands`/`drawCornerOrnaments`/`openTelemetry` (Options),
  `drawOrnamentalDivider` (LogoRenderer), `TextureAssets.exists`.
- Nuevo `CONTRIBUTING.md` (reglas branch fija, no gameplay, 4 escalas GUI,
  3 tasks de verificacion).
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Version bump: 0.21.8 -> 0.21.9 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-34 (v0.21.9)**: validar en cliente — ticker del Heraldo en
  Multiplayer (ES/EN, sin pisar el guestbook), y cache de idioma (deberia
  reflejar el cambio al recargar resources).
- KI-32/33 (v0.21.7/8) y KI-27/28/30/31 pendientes de validacion.
  KI-29 (2 pistas MenuMusic) requiere OGGs del usuario. KI-20/21/24/25 sin
  cambios.

---

## v0.21.8 - Una sola mano para el idioma (2026-08-21)

### Cambios cerrados
- 7 pantallas unificadas en `Discoveries.isEnglish()`: eliminadas las
  definiciones privadas de `isEnglish()`/`isEnglishSelected()` en Changelog,
  Credits, Language, Options, Pause, Title y Video (92 llamadas reescritas).
  Sin cambio de comportamiento (todas leen el LanguageManager activo).
- `OrnamentRenderer`: eliminados `drawVictorianFrame`, `drawDoubleBorder`,
  `drawFiligreeCorner` y `drawFleuron` (0 referencias). Quedan
  `drawPaperPanel` y `drawWaxSeal` (PosterRenderer).
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL.
- Version bump: 0.21.7 -> 0.21.8 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-33 (v0.21.8)**: validar en cliente — las 7 pantallas en ES y EN
  siguen mostrando el idioma correcto (la unificacion no altero el criterio
  `startsWith("en_")`).
- KI-32 (v0.21.7), KI-27, KI-28, KI-30, KI-31 pendientes de validacion.
  KI-29 (2 pistas MenuMusic) requiere OGGs del usuario. KI-20/21/24/25 sin
  cambios.

---

## v0.21.7 - El gabinete sin peso muerto (2026-08-21)

### Cambios cerrados
- 9 clases eliminadas: `VictorianFrame`, `AbstractDiaryScreen`,
  `DebugCanvasScreen`, `SoundLabScreen`, `DiaryDecorator`, `CandleLight`,
  `InkEffects`, `DiaryMusicSlider`, `DiaryAmbienceSlider` (los dos sliders
  combinados en `DiarySlider` generico con label/callback).
- `DiaryChrome.renderPanelBase` nuevo y aplicado en 14 pantallas; eliminados
  `drawQuillCursor`, `drawVerticalDivider`, `drawDarkLeatherBackground` y
  `LEATHER_BASE` (muertos). Conservadas las decoraciones propias de
  Changelog (encuadernado) y Credits (marco oficial).
- Eliminados metodos muertos: `PaperTexture.getOpenCount`/`drawSolidBlob`,
  `AtmosphereRenderer.getMoonPhaseIndex`, `PosterRenderer.drawVersionPanel`.
- Lang: 57 claves huerfanas eliminadas de los 25 idiomas (137 -> 80);
  nueva clave `gripeverde.options.sound.ambience_slider` en los 25 (slider
  de ambiente localizado, antes hardcodeado en espanol).
- Conteos: 59 clases Java, 25 lang (80 claves), 35 eventos, 42 OGG,
  0 PNG. sounds.json <-> ModSounds <-> OGG consistentes (35/35).
- Changelog in-game (25 .md): OMITIDO por R-CHANGELOG-DISCRECIONAL (hito
  tecnico; unico cambio visible: look unificado del regulador de ambiente).
- Version bump: 0.21.6 -> 0.21.7 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-32 (v0.21.7)**: validar en cliente — sala de sonido con los dos
  reguladores unificados (funcion y aspecto), resto de pantallas Diary sin
  cambios visuales, y que nada referencie las clases eliminadas (el build
  con `verifyGripeVerdeNoDeadCode` no falla).
- KI-27 (v0.21.3), KI-28 (v0.21.4), KI-30 (v0.21.5) y KI-31 (v0.21.6)
  siguen pendientes de validacion en cliente. KI-29 (2 pistas MenuMusic)
  requiere OGGs del usuario. KI-20/21/24/25 sin cambios.

---

## v0.21.6 - El heraldo cuenta la limpieza (2026-08-21)

### Cambios cerrados
- `Herald`: 3 noticias nuevas (limpieza del gabinete, farol del tejado,
  gato del kiosco) en ES/EN + segundas lineas variables (SECOND_ES/EN, 4
  variantes, antes fija). Semilla del dia real intacta.
- `HiddenCipher`: codigo `ARCHIVE` abre la Sala de consulta (patron
  HOLLOWAY). Sin fichas nuevas.
- Version bump: 0.21.5 -> 0.21.6 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-31 (v0.21.6)**: validar en cliente — Heraldo con noticias nuevas
  (cambiar la fecha del sistema para ver variantes) y codigo ARCHIVE.
- KI-27 (v0.21.3), KI-28 (v0.21.4) y KI-30 (v0.21.5) siguen pendientes de
  validacion en cliente. KI-29 (2 pistas MenuMusic) requiere OGGs del
  usuario. KI-20/21/24/25 sin cambios.

---

## v0.21.5 - La sala responde a la consulta (2026-08-21)

### Cambios cerrados
- Sala de consulta: busqueda por nombre/pista/id con `DiaryTextField`
  (contador "N / 47", aviso de sin coincidencias, campo oculto al abrir
  documentos).
- Ficha 47 `BUTTONS_NEVER_PRESSED` ("Los botones que nunca se pulsaron"):
  cadena cuando las 3 confesiones de limpieza de v0.21.3 estan descubiertas.
- Marginalia `N_OPEN_COUNT` (openCount >= 100) y `N_MANY_CARDS`
  (discoveredCount >= 44).
- Ducking de la musica al descubrir (`Discoveries.discover` ->
  `MenuMusic.duckFor(2500L)`).
- Modo low-end en `QuarantineSceneRenderer.isLowEnd()` (FPS < 30, muestreo
  1s, defensivo): omite niebla y faroles en Title y fondos Diary.
- Version bump: 0.21.4 -> 0.21.5 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-30 (v0.21.5)**: validar en cliente real — busqueda en la sala (4
  escalas GUI), cadena de la ficha 47, ducking al hallar, y low-end en
  maquinas lentas (o forzando FPS bajo).
- KI-27 (v0.21.3) y KI-28 (v0.21.4) siguen pendientes de validacion en
  cliente. KI-29 (2 pistas MenuMusic nuevas) requiere OGGs del usuario.
- KI-20, KI-21, KI-24, KI-25 siguen abiertos sin cambios.

---

## v0.21.4 - La tinta se asienta (2026-08-21)

### Cambios cerrados
- Busqueda del manual en `DiaryControlsScreen` (`DiaryTextField` filtra los 6
  puntos de mando por nombre traducido; hint y aviso de "ningun punto coincide";
  oculto en paneles < 220px para no invadir el divisor).
- `DiaryVideoSettingsScreen` reagrupado: secciones RENDER (FOV + Canvas) /
  QUALITY (escala GUI) / PERFORMANCE (medidor de FPS con `minecraft.getFps()`
  defensivo, oculto en paneles < 330px). No toca la logica vanilla.
- `DiaryLanguageScreen`: hover muestra el registro del consulado (136/136 para
  las 25 lenguas del gabinete; 0/136 para lenguas vanilla no soportadas).
- Nuevo evento de sonido `ui.paper_fall` (34 -> 35 eventos) reutilizando
  `ui/scroll1` + `ui/back2` (42 OGG intactos); suena al abrir la Sala de
  consulta.
- Higiene: 54 imports no usados eliminados; task Gradle
  `verifyGripeVerdeNoDeadCode` conectada a `processResources` (falla si
  reaparecen ThemeManager, gripeverde_steel, bg_town, IconButton, etc. fuera
  de comentarios); `.editorconfig` (utf-8, LF, trim trailing whitespace).
- Version bump: 0.21.3 -> 0.21.4 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-27 (v0.21.3)**: sigue pendiente la validacion en cliente de RNG seed
  fija, REDUCED_MOTION y las 3 fichas de limpieza.
- **KI-28 (v0.21.4)**: validar en cliente real — 4 escalas GUI (filtro del
  manual, secciones del aparato, hover de registro, papel en la sala), que el
  filtro no rompa clicks ni el smugglers signal, y que el FPS meter no se vea
  fragil en pantallas pequenas.
- **KI-29 (deuda audio)**: las 2 pistas MenuMusic nuevas del brief requieren
  OGGs reales del usuario (no generables en sandbox). Evento "papel cayendo"
  ya cubierto por `ui.paper_fall` con reuso.
- KI-20, KI-21, KI-24, KI-25 siguen abiertos sin cambios.

---

## v0.21.3 - El gabinete respira mas ligero (2026-08-21)

### Cambios cerrados
- RNG con seed fija en los 3 renderers del Title (`VictorianSkyline`,
  `LivingAtmosphere`, `QuarantineSceneRenderer`): eliminado `Math.random`
  del loop caliente. Escena determinista por sesion.
- `ClientConfig.REDUCED_MOTION` (boolean, seccion `visual`, default false):
  aquieta en `LivingAtmosphere` el farol erratico, las polillas, la rata,
  los papeles, las rafagas y el pulso de niebla. No toca widgets ni
  `EasedValue` (riesgo UI evitado).
- 3 fichas nuevas de la limpieza (43 -> 46): `ARCHIVE_THINNING`
  (openCount >= 50, en `DiaryTitleScreen.init`), `THEME_NEVER_PLAYED`
  (codigo `CLEANSE` en `HiddenCipher` + telegrama), `BG_TOWN_MEMORY`
  (cadena en `DiaryArchiveScreen` con las otras dos). Nombres/pistas ES/EN
  hardcodeados; sin lang keys nuevas.
- `DiaryOptionsScreen`: progreso del archivo con `Discoveries.ALL.length`
  (era `43` fijo).
- Version bump: 0.21.2 -> 0.21.3 (build.gradle + GripeVerdeMod.VERSION).

### Riesgos activos / KI nuevos
- **KI-27 (validacion v0.21.3)**: validar en cliente real — 4 escalas GUI
  (fichas/toast/CLEANSE/REDUCED_MOTION), que la escena con seed fija no se
  sienta repetida tras varias sesiones, y que el toml nuevo (seccion
  `visual`) no rompa configs existentes (Forge agrega la clave con default).
- KI-20, KI-21, KI-24, KI-25 y KI-26 siguen abiertos sin cambios.

---

## v0.21.2 - Limpieza profunda del proyecto (2026-08-21) + hotfix previo v0.21.1

### Cambios cerrados (hotfix)
- Corregido `client/audio/ModSounds.java`: el registro ya no depende
  ciegamente de `SoundEvent.createVariableRangeEvent(...)`, que en el runtime
  real estaba provocando `NoSuchMethodError` durante el arranque del cliente.
- Hotfix sin cambios visuales ni de gameplay. Solo arranque/compat runtime.
- Version bump: 0.21.0 -> 0.21.1.

### Cambios cerrados (limpieza 2026-08-21)
- **Docs históricos eliminados:** `AUDITORIA_PROFESIONAL.md` (17K), `MICRODETALLES_EXTREMO.md` (9K), `VALIDACION_FASE4.md` (9K) — eran referencia de polish v0.14.0 con steel pack, ya revertido y sin uso desde v0.15.0/v0.19.0.
- **Assets sin uso:** `sound-tests/` (wav fuente + README) y `bg_town.png` (2.1M, Title usa render procedural, no textura estática). Directorios vacíos `textures/gui/title` y `textures/gui` eliminados.
- **Código muerto - theme framework:** eliminados `ThemeManager` (inerte, sin themes desde v0.6.0), `GhoulPalette`, `ScreenLayout`, `ScreenTheme`, `AbstractScreenTheme`, `UIComponent`, `UIScale`, `components/*` (BackgroundRenderer, DecorationRenderer, FrameRenderer, SeparatorRenderer, TitleRenderer). Conservados solo `Anchor`, `LayoutRegion`, `Spacing` por uso activo en `DiaryTitleScreen`.
- **Código muerto - widgets:** eliminados `IconButton`, `SilentButton`, `DiarySeparator` (definidos pero nunca instanciados fuera de su archivo).
- **TextureAssets:** eliminado `BG_TOWN` y `hasBgTown()`, solo quedan `ICON_BTN_MP` y `ICON_BTN_MODS` como identificadores lógicos para `GreenFluButton` (dibujo procedural).
- **ScreenEventHandler:** eliminado import y llamada a `ThemeManager.render()` (no-op), simplificado a intercept Title/Pause + feedback sonoro + atajo Ctrl+0.
- **Docs actualizados:** `README.md` (v0.21.1 + nota limpieza + lista docs sin _backups), `CONTEXTO_PARA_IA.md` (reescrito a v0.21.1, 68 clases, 0 PNG, sin steel), `CHANGELOG.md` (añadidos v0.19.0, v0.17.2, v0.17.1, v0.17.0), 25 `changelog/*.md` in-game (entrada v0.21.1 EN/ES), `CONTEXTO.md` (nuevo addendum limpieza + nota ThemeManager).
- **Conteos post-limpieza:** 68 clases Java (vs 83 en v0.17.2), 25 lang, 25 changelog, 42 OGG, 0 PNG contenedores. Working tree limpio.
- **Post-cierre (docs):** `CONTEXTO_PARA_IA.md` retirado por redundancia con `CONTEXTO.md` (habia quedado desincronizado en v0.21.1). `CONTEXTO.md` queda como unica fuente de verdad. Sin impacto en build ni runtime, sin bump.

### Riesgos activos / KI
- **KI-25 (build local del usuario)**: si reaparece un crash distinto al
  iniciar, revisar primero el nuevo jar `gripeverde-0.21.2.jar` y confirmar
  que no quede mezclado con jars viejos del mod en `mods/`.
- **KI-26 (limpieza validación)**: tras eliminar theme framework y widgets muertos, validar en cliente real que `DiaryTitleScreen` sigue anclando bien con `Anchor`/`LayoutRegion`/`Spacing` en 4 escalas GUI. Sin JDK en sandbox, pendiente de validación visual local.

---

## v0.21.0 - Cohesion y cierre de interfaces secundarias

### Cambios cerrados
- `DiaryChatOptionsScreen` deja la logica correcta pero gana cierre visual de
  oficina de correspondencia: casillero, correo del distrito y tablero publico
  mejor separados.
- `DiaryOnlineOptionsScreen` pasa de hoja con opciones a estacion de
  comunicaciones: tablero, rele y trafico del distrito con identidad propia.
- `DiarySkinCustomizationScreen` recibe un polish final y se acerca al estado B
  sin otra reconstruccion completa.
- `DiaryArchiveScreen` gana mejor lectura de catalogo y preview del contenido,
  reforzando la jerarquia de consulta.
- `ChangelogScreen` se acerca a Archive como libro de revisiones archivado,
  con footer y lectura mas consistente.
- `DiaryDirectJoinScreen`, `DiaryEditServerScreen` y `DiaryConfirmScreen`
  reciben refinamiento adicional de formulario/permiso y quedan mas cerca del
  estado B que del D original.
- Version bump: 0.20.0 -> 0.21.0.

### Riesgos activos / KI nuevos
- **KI-20 (runtime GUI scale)**: sigue pendiente validacion real en GUI Scale
  1-4. Esta vez las pantallas afectadas fueron sobre todo Chat, Online,
  Archive, Changelog y formularios.
- **KI-21 (Video + mods externos)**: sin cambios en esta sesion; sigue la
  necesidad de validar `DiaryVideoSettingsScreen` con mods externos.
- **KI-24 (formularios chicos)**: `DirectJoin`, `EditServer` y `Confirm`
  quedaron mas compuestos visualmente; verificar en ventanas pequenas que los
  labels y botones no respiren de menos.

### Reglas nuevas
- **R-SUBSCREEN-CLOSURE**: cuando las pantallas principales ya estan fuertes,
  los siguientes pases deben concentrarse en las salas secundarias para evitar
  que el sistema parezca inconsistente por periferia debil.
- **R-SIMPLIFY-BEFORE-ADDING**: si una pantalla ya esta en B, primero se prueba
  simplificar o reordenar antes de agregar nuevas piezas visuales.

---

## v0.20.0 - Identidad de interfaz / pase de consolidacion

### Cambios cerrados
- Auditoria del pase documental anterior: se conservaron `ExpedientHeader`,
  `DiaryProgressBar` y la linea de railes/cabeceras, pero se refinaron para
  evitar que la UI se convierta en un dashboard de rectangulos.
- `DiaryChrome` evolucionado: tarjetas menos rigidas, capsulas mas sobrias,
  railes incompletos con marcas de registro, scrollbar mas fisica y scaffolds
  con mejor lectura editorial.
- `DiarySlider`, `DiaryMusicSlider` y `DiaryAmbienceSlider` redibujados como
  instrumentos de medicion (regla/aguja/escala) en vez de simples barras.
- `DiaryToggle` rediseñado como placa mecanica con compuerta de estado.
- `DiaryOptionsScreen`, `DiarySoundOptionsScreen`, `DiaryControlsScreen`,
  `DiaryChatOptionsScreen`, `DiaryAccessibilityOptionsScreen`,
  `DiaryLanguageScreen`, `DiaryOnlineOptionsScreen`, `DiaryCreditsScreen`,
  `DiaryMultiplayerScreen`, `DiaryPauseScreen` y `DiaryTitleScreen` recibieron
  nuevo pulido de composicion, menos rigidez de tarjeta y mejor jerarquia.
- `DiaryVideoSettingsScreen` se mantiene como pantalla propia, pero refinada
  dentro del nuevo tono mas editorial.
- Sigue prohibido y ausente todo lo relacionado a resourcepacks. No se
  reintrodujo ningun asset vanilla modificado.
- Version bump: 0.18.0 -> 0.19.0.

### Riesgos activos / KI nuevos
- **KI-20 (runtime GUI scale)**: sigue pendiente validacion real en GUI Scale
  1-4. El pase se penso con clamps y railes mas sobrios, pero sin Java local no
  se pudo probar en cliente.
- **KI-21 (Video + mods externos)**: `DiaryVideoSettingsScreen` preserva la
  lista vanilla por reflection. Hay que verificar en runtime con mods que
  extiendan VideoSettingsScreen.
- **KI-22 (pantallas secundarias)**: `Skin`, `Archive`, `DirectJoin`,
  `EditServer` y `Confirm` mantienen parte de la linea visual anterior. No
  quedaron rotas, pero su refinamiento podria profundizarse en una fase futura.

### Reglas nuevas
- **R-LESS-DASHBOARD**: si una mejora de UI se limita a sumar mas tarjetas,
  railes o capsulas, se reconsidera. La prioridad es composicion editorial,
  profundidad y jerarquia, no cantidad de cajas.
- **R-INSTRUMENT-SLIDER**: sliders propios de Gripe Verde deben leerse como
  instrumentos fisicos de medicion, no como barras modernas de videojuego.
- **R-MECHANICAL-TOGGLE**: toggles propios deben comunicar ON/OFF mediante una
  placa mecanica o documental clara, nunca como switch movil moderno.

---

## v0.18.0 - El gabinete toma forma definitiva (pase mayor de interfaces)

### Cambios cerrados
- Nuevo sistema de cabeceras documentales reutilizable (`ExpedientHeader`) con
  expediente, referencia, estado y flavor, aplicado a Options, Sound, Video,
  Controls, Chat, Accessibility, Language, Online, Credits y Multiplayer.
- `DiaryChrome` ampliado como libreria de interfaz: reveal temporal de panel,
  capsulas de estado, tarjetas laterales, railes internos, scaffolding de
  profundidad y utilidades de color.
- `DiaryProgressBar` ampliado con estilos (`INK`, `PLAGUE`, `ALERT`, `AMBER`,
  `STEEL`) y marca opcional; usado como medidor documental y no solo como
  barra generica.
- Nueva `DiaryVideoSettingsScreen`: el aparato visual deja de abrir la pantalla
  vanilla directa y recibe presentacion tecnica propia sin alterar los widgets.
- `DiaryOptionsScreen` rehecha como mesa central: agrupacion por bloques,
  rail lateral de estado, resumen de idioma/audio/archivo y revelado de panel.
- `DiarySoundOptionsScreen` gana carril acustico, medidores de musica/ambiente,
  lectura de crossfade y estado de sala.
- `DiaryControlsScreen` gana composicion de manual operativo con resumen de
  postura, disciplina y protocolo.
- `DiaryChatOptionsScreen`, `DiaryAccessibilityOptionsScreen`,
  `DiaryLanguageScreen`, `DiaryOnlineOptionsScreen`, `DiaryCreditsScreen` y
  `DiaryMultiplayerScreen` reciben cabeceras, railes, tarjetas y jerarquia
  coherentes con el nuevo sistema.
- `DiaryTitleScreen` gana mazos de mando, franja inferior de estado y mejor
  continuidad visual con el gabinete.
- `DiaryPauseScreen` gana lectura documental de estado y barra de quietud.
- **No se reintrodujo ningun resource pack**. Siguen ausentes:
  `gripeverde_steel`, `tools/steel_pack`, `ResourcePackHandler`,
  `DiaryPackSelectionScreen` y cualquier textura vanilla modificada.
- Version bump: 0.16.0 -> 0.18.0.

### Riesgos activos / KI nuevos
- **KI-20 (runtime GUI scale)**: el sandbox no tiene Java; queda pendiente
  validar en cliente real GUI Scale 1-4, sobre todo en las pantallas que ahora
  usan rail lateral (`Sound`, `Video`, `Chat`, `Accessibility`, `Language`).
- **KI-21 (mods externos en Video)**: `DiaryVideoSettingsScreen` envuelve la
  consola vanilla. Si algun mod inyecta opciones en `VideoSettingsScreen`, la
  logica deberia seguir viva, pero hay que comprobarlo con Embeddium y otros
  mods de video en runtime.
- **KI-22 (timing de reveal)**: el revelado por scissor es puramente visual.
  Si el usuario hace click durante los primeros ~250 ms, puede intentar pulsar
  un control aun parcialmente oculto. Riesgo bajo, pero conviene validarlo.

### Reglas nuevas
- **R-NO-RESOURCEPACK-UI**: todo pase visual de interfaces debe resolverse en
  Java/render/widgets. Prohibido reintroducir resourcepacks para identidad UI.
- **R-DOC-HUB-IDENTITY**: cada pantalla Diary puede compartir sistema visual,
  pero debe tener al menos un bloque propio de composicion (rail, tarjeta,
  resumen o medidor) que la diferencie del resto.
- **R-REVEAL-SCISSOR**: para animar entrada sin romper hitboxes vanilla,
  preferir reveal por scissor antes que trasladar widgets interactivos.

---

## v0.16.0 - La sala de consulta (pase profundo)

### Cambios cerrados
- Sistema de memoria persistente nuevo: `config/gripeverde-memory.json`
  (descubrimientos, objetos, notas pendientes, contadores, libro de
  visitas). Independiente del TOML de audio; tolerante a corrupcion.
- 43 descubrimientos registrados con fichas, pistas y sello de toast.
- Sala de consulta (DiaryArchiveScreen) accesible desde el folio escondido
  del historial y desde el codigo HOLLOWAY.
- Marginalia contextual del cronista (19 notas, todas con causa).
- Telegrafo oculto del Title con 4 codigos (1897, 1152, HOLLOWAY, PEREIRA).
- 7 consecuencias permanentes por contador de memoria (ventana del piso 13,
  sello FRAUDE, cortinas del atico, farol enfermo, sello roto, firma
  ofendida, ojos del retrato).
- Cada pantalla del gabinete recibio al menos una interaccion propia nueva
  (ver CONTEXTO.md ADDENDUM v0.16.0).
- Sin cambios en contenedores, slots, layouts vanilla ni resource packs.
- Sin mixins nuevos (client:[] vacio). Sin assets nuevos (sonidos reusados
  con pitch/volumen).

### Riesgos activos / KI nuevos
- **KI-17 (validacion de build) - CERRADO**: el sandbox del pase no tiene JDK
  ni acceso a maven; el build se ejecuto en la maquina del usuario.
  Primer build: 1 error (duckFor llamado en la clase equivocada, fix
  db61d86). Segundo build: BUILD SUCCESSFUL (57s, solo 4 warnings de
  deprecaciones preexistentes). Deployado a Test como gripeverde-0.16.0.jar.
- **KI-18 (escala 4)**: la Sala de consulta usa panel de 440x404 con
  clamp a pantalla-32; en GUI 4 puede quedar justa de alto. El clamp
  minimo es 280x240; las fichas se dibujan igualmente. Validar.
- **KI-19 (lista de lenguas)**: la lengua de la niebla se inserta via
  `children().add(pos, entry)` en la ObjectSelectionList propia. El
  scrollbar y el smooth scroll usan getMaxScroll, que se recalcula;
  validar que no haya salto al aparecer la entrada.
- El Heraldo dibuja sobre la calle; en modo minimal (altura < 290) el
  kiosco no se dibuja ni se puede tocar (chequeo h < 300).
- El coche del sepulturero solo aparece si el lector esta mirando el
  Title (chequeo de render reciente); en subpantallas no aparece.

### Reglas nuevas
- **R-LORE-DISJOINT**: la memoria narrativa vive en su propio JSON; nunca
  guardar descubrimientos en el TOML de audio.
- **R-CONSEQUENCE-COUNTER**: toda consecuencia permanente se implementa
  como contador entero en memoria leido por el renderer; nunca como flag
  estatica en el renderer (se perderia entre sesiones).
- **E125**: sin javac disponible en el entorno, solo se usan APIs ya
  verificadas en el codigo del repo; nada de firmas recordadas de memoria.

---

## v0.17.0 - Acero de cuarentena (pase de interfaz e identidad visual)

### Cambios cerrados
- Nuevo resource pack built-in `gripeverde_steel` (REQUIRED=true, siempre
  activo): 20 contenedores + 4 fondos creativos + sprites de contenedores
  (hornos, alambique, baliza, yunque, encantamientos, caballos, creativo) +
  widgets vanilla (button/slider/text_field/checkbox/scroller) + boton del
  libro de recetas. Todo 1:1 sobre geometria vanilla (alpha identica,
  slots 18x18 en posiciones vanilla exactas).
- Nuevo `client/ResourcePackHandler.java` (registro via AddPackFindersEvent,
  `Pack.readMetaAndCreate` con required=true — patron v0.13.2, no el de
  Master Steel required=false).
- Nueva task `verifyGripeVerdeThemeAssets` en build.gradle (dimensiones,
  pack_format 15, pack.png 128x128, ausencia de huerfanas dropper/villager2,
  .mcmeta de 9-slices presentes). Dependencia de processResources.
- Nuevo pipeline `tools/steel_pack/` (build_steel_pack.py + steel_pack_lib.py):
  regenera el pack desde vanilla y valida alpha/slots.
- DiaryChrome: esquineros ahora son escuadras de acero con remache (todas
  las pantallas Diary); nueva chapa de registro `drawRegistryTag`
  (REG. I..XV) arriba a la derecha de cada panel; variante izquierda en
  Skin (el espejo ocupa la derecha).
- Nueva `DiaryPackSelectionScreen` (archivador de materiales): envuelve la
  pantalla vanilla de Resource Packs — escena viva + pergamino de archivo +
  marco de acero + sello EC-QV/1897 + chapa REG. XV. Las listas vanilla
  conservan su logica; se les quita el fondo dirt via reflexion acotada
  (mismo patron que DiarySoundOptionsScreen). Scrollbars quedan en acero
  via el pack (widget/scroller.png).
- Nueva clave `gripeverde.options.packs.subtitle` en los 25 lang files.
- Changelogs in-game (25) + CHANGELOG.md raiz con entrada narrativa v0.17.
- Version bump: 0.16.0 -> 0.17.0.

### Riesgos activos / KI nuevos
- **KI-20 (validacion en cliente real) - ABIERTO**: el sandbox no tiene JDK
  ni maven de Forge; no se pudo compilar ni ejecutar. Pendiente en la
  maquina del usuario: build, deploy y revision visual en 4 escalas GUI.
  Puntos de atencion: (1) el pack REQUIRED no aparece para desactivar
  (a proposito); (2) la chapa de registro se omite si panelW<330 (escala 4
  en paneles chicos); (3) la pantalla de paquetes: verificar que las listas
  no dibujen doble fondo si la reflexion falla (fallback = fondo vanilla);
  (4) en pantallas de mods que usan botones vanilla, el pack los estiliza
  en acero (comportamiento esperado).
- **KI-21 (sprites no cubiertos)**: quedan vanilla por no confirmarse en
  1.20.1: sprites de villager (flechas de comercio, barras de experiencia),
  loom (patrones), stonecutter (recetas), cartography (mapas), smithing
  (error). Si en 1.20.1 existen, se ven vanilla sobre el panel de acero;
  extension futura opcional.
- **KI-22 (scrollbars Diary)**: DiaryChrome.drawScrollThumb ya usaba acero
  + bronce (v0.11.x); sin cambios.
- El pack no toca: hotbar (widgets.png), HUD, tooltips, ni contenedores de
  mods (Curios, mochilas) — esos usan sus propias texturas; la estetica de
  slots se hereda del pack solo cuando el container dibuja las de vanilla.

### Reglas nuevas
- **R-STEEL-GEOMETRY**: el resource pack NUNCA puede cambiar dimensiones,
  alpha, posiciones de slots ni proporciones vanilla. Verificado por
  build_steel_pack.py (hash de alpha) y por verifyGripeVerdeThemeAssets.
- **R-PACK-REQUIRED**: el pack de identidad del mod es REQUIRED=true;
  no depende de activacion manual. Si en el futuro se quiere opcional,
  cambiar a required=false y documentar que hay que activarlo en pantalla.

---

## v0.17.1 - Remaches bien puestos (mantenimiento del acero)

### Cambios cerrados
- Parche de mantenimiento sobre el resource pack `gripeverde_steel`. Repaso
  cosmetico de las planchas de acero (remaches, cepillado, juntas) para
  igualar el sello visual entre contenedores. Sin cambios observables para
  el jugador.
- Sin cambios de codigo Java, sin cambios de geometria ni de slots.
- Bump de version: 0.17.0 -> 0.17.1 (build.gradle + GripeVerdeMod.VERSION).
- CHANGELOG.md raiz: entrada narrativa. Changelog in-game OMITIDO
  (R-CHANGELOG-DISCRECIONAL — pulido interno, sin manifestacion observable).

### Riesgos activos / KI
- Ninguno nuevo. No abre ni cierra KIs.
- Refuerza KI-20 (validacion del pack en cliente real sigue pendiente).

### Reglas nuevas
- Ninguna.

---

## v0.17.2 - Las bandejas de la mesa y la moneda del trato (correccion del acero)

### Cambios cerrados
- Pase de correccion geometrica/visual sobre el acero de cuarentena. Todo es
  assets del resource pack; sin cambios de codigo Java.
- Sprite universal `sprites/container/slot.png`: 18px -> 26px (bandeja con
  holgura en lugar de cuadrado estrecho).
- Oficios restaurados al arreglo de acero correcto: telar (loom), cantera
  (stonecutter), cartografia (cartography_table), trato con forasteros
  (villager). Monedas y sellos del trato (trade_arrow, discount_strikethrough,
  experience_bar_*, out_of_stock, scroller*) devueltos al acero.
- Transparencias repasadas una a una (ningun velo se vuelve muro): widgets
  (button/slider/text_field/scroller/checkbox), sprites de contenedores,
  recipe_book.
- Nuevos sellos del archivo de telas (loom: pattern/banner_slot/dye_slot/
  pattern_slot/error/scroller) y de la sala de mapas (cartography: map/
  scaled_map/duplicated_map/locked/error).
- Creative inventory: `tabs.png` (tapa muerta) REMOVIDO. Reemplazado por
  sprites tab_top/tab_bottom_selected/unselected_1..7.
- Task `verifyGripeVerdeThemeAssets`: la lista de huerfanas prohibidas ahora
  incluye `creative_inventory/tabs.png` (ademas de dropper.png, villager2.png,
  inventory_backup.png).
- Bump de version: 0.17.1 -> 0.17.2.
- CHANGELOG.md raiz: entrada narrativa. Changelog in-game OMITIDO
  (R-CHANGELOG-DISCRECIONAL).

### Riesgos activos / KI
- KI-20 (validacion en cliente real) sigue ABIERTO: sin JDK ni maven en el
  sandbox. Validado programaticamente via verifyGripeVerdeThemeAssets
  (dimensiones, alpha, .mcmeta, ausencia de huerfanas). Pendiente build +
  revision visual en la maquina del usuario.
- KI-21 (sprites no cubiertos): muy avanzado en v0.17.2 (loom, cartography,
  stonecutter, villager trading ahora tienen tratamiento de acero). Revisar
  si quedan holes tras validar en cliente real.

### Reglas nuevas
- Ninguna. Refuerza R-STEEL-GEOMETRY (el pack nunca cambia dimensiones/alpha/
  posiciones de slots; solo presentacion) y R-CHANGELOG-DISCRECIONAL.

---

## Estado actual de pulido RC

### Criterios activos
- Sonidos de boton separados por categoria funcional.
- No conservar archivos temporales de traspaso dentro del repo.
- No reintroducir bandejas o sombras que ensucien listas de opciones.
- Mantener layouts vanilla de containers y pantallas densas.
- Revisar cada nuevo recurso contra `sounds.json` antes de compilar.


## v0.15.0 - El oficio vuelve a su madera (retiro del resource pack)

### Cambios cerrados
- Eliminado `src/main/resources/resourcepacks/gripeverde_theme/` completo
  (21 texturas de contenedores, pack.mcmeta, pack.png, COMPATIBILITY.md).
- Eliminado `client/ResourcePackHandler.java` (registro via AddPackFindersEvent).
- Eliminada la task `verifyGripeVerdeThemeAssets` y su dependsOn en
  `processResources` (build.gradle).
- Eliminado `tools/` (pipeline de generacion del pack: kit.py, build_*.py,
  verify.py, vanilla/ y export/).
- Los contenedores del juego vuelven a ser 100% vanilla.
- Version bump: 0.14.4 -> 0.15.0.

### Riesgos activos
- Validar en cliente real que ningun container muestre texturas residuales
  del pack (el zip de prueba in-game debe retirarse de resourcepacks).
- KIs del pack (steel/silver steel/fresadora): CERRADOS. No retomar sin
  instruccion explicita del usuario.


## v0.15.1 - La segunda mano (restos del retiro)

### Cambios cerrados
- Eliminado `client/ContainerOverlayHandler.java`: re-dibujaba los titulos
  de los contenedores con color pergamino y sombra ("letras personalizadas"
  que el usuario seguia viendo). Titulos ahora vanilla puro.
- Agregado slider de campo de vision (FOV) al gabinete de opciones
  (DiaryOptionsScreen, fila 6 del grid, estilo DiarySlider, rango 30-110,
  default 70, vinculado a `options.fov()`). Vanilla lo tiene en OptionsScreen
  y habia quedado fuera del reemplazo victoriano.
- Version bump: 0.15.0 -> 0.15.1.

### v0.15.2 - Fix label del slider FOV
- La clave vanilla `options.fov` es solo "FOV" (sin placeholder %s), por lo
  que `Component.translatable("options.fov", valor)` devolvia "FOV" sin el
  numero. Ahora el label se construye explicitamente: "FOV: N" /
  "Campo de vision: N" (se agrega ": " + numero a la clave traducida).
- Version bump: 0.15.1 -> 0.15.2.

### Riesgos activos
- Validar en cliente real: (1) el FOV se ajusta desde Opciones; (2) los
  titulos de contenedores son blancos vanilla sin sombra pergamino.
- `TooltipEventHandler` sigue recoloreando tooltips (pergamino); si el
  usuario pide vanilla total, revisar tambien.
- `ThemeManager` no dibuja nada (sin themes registrados) - inerte.


## Estado actual para continuidad

### Cerrado en esta linea de trabajo
- Sonidos UI categorizados por uso (`ui.button_main`, `ui.button_section`, `ui.back`).
- `sounds.json` no referencia los OGG rechazados por el usuario.
- Carpeta de pruebas de botones reducida al candidato aceptado y notas de seleccion.

### Riesgos activos
- Validar auditivamente en cliente real que `modal.ogg` funcione bien como sonido de apertura de seccion.
- Si se agregan sonidos nuevos, primero colocarlos en carpeta de prueba y pedir seleccion del usuario antes de conectarlos a `sounds.json`.
- No tocar layouts vanilla de containers salvo paleta/estetica; slots y zonas interactivas deben permanecer familiares.
- Mantener `ContainerOverlayHandler` restringido a pantallas vanilla para no romper mochilas o containers de mods.

### Regla operativa nueva
- Luego de cualquier cambio, entregar PowerShell completo de build/deploy a Test; no entregar solo el hash.


## v0.13.10 - Acero bajo la lluvia

### Cambios cerrados
- Resource pack steel extendido y verificado para containers menores:
  smoker, blast furnace, shulker box, brewing stand, hopper, dispenser,
  dropper, loom, cartography table, stonecutter, smithing, horse y
  villager trading.
- ContainerOverlayHandler agrega sombra a titulos principales y oculta
  labels vanilla conflictivos cuando es posible.
- Enchanting table gana tratamiento arcano: fondo purpura, lineas de
  lectura y particulas discretas.
- Title gana polish atmosferico: polillas, salpicaduras, ondas en charcos,
  reflejos de farol, papeles al viento, rafagas, lluvia diagonal, sombras
  de ramas, relampagos ramificados e icono nuevo del resource pack.
- Nueva task Gradle `verifyGripeVerdeThemeAssets` para detectar assets
  faltantes antes de empaquetar.

### Estado / deuda
- Las pantallas vanilla muy complejas pueden requerir ajustes visuales
  puntuales por idioma o por mods externos que alteren layout.
- La familia de containers queda lista para validacion de Alex antes de
  cerrar un ciclo mayor.

## v0.13.9 - Hotfixes y optimizaciones

Fixes criticos post-v0.13.9:
- **FIX**: MenuAmbience.stop() removido de Title.removed() - ambiente
  persiste al navegar entre pantallas (como musica).
- **FIX**: MenuAmbience.tick() agregado en 13 subpantallas Diary.
- **FIX**: setUserVolume no reconstruye loops (delegado a
  updateLoopVolumes en tick, evita saturacion del SoundManager).
- **FIX**: updateLoopVolumes no reconstruye en primera llamada
  (evita matar loops recien creados por start()).
- **FIX**: renderStreet publico + llamado en 13 subpantallas
  (calle visible sin faroles/esporas/rata).
- **FIX**: Easter egg texto escala adaptativa (1.0x-1.8x segun
  ancho de pantalla).
- **FIX**: Panel sonidos PANEL_H 340->380.
- **PERF**: Subpantallas sin renderFogLow/renderStreetLevel/renderAir.
- **PERF**: Estrellas 60->40, brillantes 15->10.
- **PERF**: Nubes 7->5.
- **PERF**: RAIN_COUNT 80->40, SPORE_COUNT 30->15.
- **PERF**: Humo FG desactivado (solo BG).
- **PERF**: Vignette margin 0.28->0.20, step 2px.
- **PERF**: drawSoftBlob menos steps + alpha threshold.
- **PERF**: Stains/fibers/coffee reducidos en PaperTexture.

## v0.12.1 - Detalles vivos del gabinete

Version con 16 mejoras de UI y atmosfera, distribuidas en 6 bloques
sucesivos de desarrollo. Sin nuevos KIs criticos. Codigo estable.

### Mejoras aplicadas
- **Estrellas parpadeantes** (AtmosphereRenderer): 15 de 60 estrellas
  brillantes 2x2 con borde suave; frecuencia y fase propias por estrella.
- **Ventana animada** (VictorianSkyline): una ventana FG alterna on/off
  con transicion suave cada 8-15s.
- **Farol erratico** (LivingAtmosphere): farol index 2 oscila
  caoticamente 3-5s cada 30-60s.
- **Rata cruzando** (LivingAtmosphere): sprite 8x4 cruza la calle
  cada 40-80s, direccion aleatoria.
- **DiaryButton pressed + hover suave**: offset Y+1 al click, sombra
  reducida, bg mas oscuro; EasedValue de 6->4 para hover organico.
- **DiaryToggle sonido distinto**: clickHeavy en vez de click.
- **Frases del cronista** en LogoRenderer: pool de 12 apuntes ("Dia 47.
  Aun no encuentro respuestas.", etc.) rotando aleatoriamente cada
  30-120s, con Random.nextInt (no incremental).
- **Firma rotativa** en DiaryTitleScreen: pool de 4 nombres (S. Pereira,
  R. Da Rosa, G. Bandeira, A. Focco).
- **Citas rotativas** en DiaryOptionsScreen: pool de 8 frases flavor
  bajo el subtitulo, cambia al abrir la pantalla.
- **Separadores con rombo** en ChangelogScreen: lineas con fade lateral
  y lozenge central 5x5 entre secciones.
- **Capitular decorativa** en ChangelogScreen: primera letra de H1
  escalada 1.6x en tinta oscura.
- **Scroll recordado** en DiaryLanguageScreen y ChangelogScreen (campos
  estaticos lastScrollAmount / lastScrollY).
- **Campanilla en DiaryConfirmScreen** al init: UiSounds.notification().
- **Divisor de Options movido debajo del flavor** (bug fix v0.12.1).
- **Fill negro inicial** en DiaryChat/Controls/Credits/Language/
  Options/Sound: para evitar residuo del framebuffer.

### Bugs identificados y revertidos
- **Fade de transicion** (2 bloques atras): overlay negro 200ms al
  entrar. Revertido por feedback del usuario: se percibia como flash
  no como transicion suave.
- **UiSounds.open() al abrir subpantallas**: revertido por sonar
  "pesimo" segun feedback.
- **Parallax con el mouse**: revertido tras causar 2 bugs criticos
  (piso desaparecia por translate excesivo, y pergamino residual al
  volver de Options por bordes sin cubrir).
- **Fill negro en DiaryTitleScreen**: revertido - oscurecia las
  ventanas amarillas del skyline.
- **YEAR_POOL con romanos** (Anno Domini MDCCCXCVII): revertido por
  feedback, reemplazado por apuntes del cronista.

### Errores catalogados
- **E113**: parallax con `pushPose/translate` en render() causa que
  elementos se desplacen fuera del area visible cuando el mouse esta
  lejos del centro. Ademas rompe el ciclo de framebuffer cuando la
  pantalla cambia. Regla derivada: R-NO-GLOBAL-TRANSLATE-RENDER.
- **E114**: `g.fill(0,0,w,h, 0xFF000000)` al inicio del Title
  oscurece las ventanas iluminadas del skyline aunque teoricamente
  renderSky las tape. Blend issue con capas transparentes. Fix:
  no usar fill negro en el Title, aceptar el residuo minimo.
- **E115**: Cloth Config v11.1.136 + IMPR 3.23.3 tuvieron un crash
  esporadico ("Config IMPConfig has not been registered"). No es
  culpa del mod nuestro. Fix: borrar
  config/iammusicplayer.toml y reiniciar. Documentado por si vuelve.

### Reglas nuevas
- **R-NO-GLOBAL-TRANSLATE-RENDER**: nunca aplicar `pushPose/translate`
  sobre bloques de render que dibujen escena viva. El offset saca
  contenido del framebuffer visible y causa bugs de superposicion
  cuando cambia la pantalla.
- **R-FRAMEBUFFER-CLEAR-TITLE**: NO llamar `g.fill(0,0,w,h, opaco)`
  en el render del Title. Los blends de capas dependen del framebuffer
  base. Si hay residuo del frame anterior, se acepta.
- **R-CONFIG-RESIDUAL-EXTMOD**: cuando otro mod (IMP, Cloth Config,
  etc.) falla al arranque con "Config not registered", la solucion
  primera es borrar su .toml del folder config/ y reiniciar. No es
  bug nuestro.

## v0.12.0 - Cierre del ciclo del gabinete (administrativo)

Version sin cambios de codigo. Solo:
- Reagrupamiento del changelog in-game: se borran las entradas sueltas
  de v0.10.4, v0.11.0, v0.11.1, v0.11.10-13 y v0.11.14. Se reemplazan
  por UNA sola entrada "Version 0.10 - 0.11 - Una casa de papel" que
  narra el ciclo completo del gabinete pergamino.
- Salto de version v0.11.14 -> v0.12.0 para senalar cierre de fase UI
  y arranque de fase de gameplay.

### Errores catalogados durante v0.11.14 -> v0.12.0
- **E111**: Intento de mixin en v0.11.14 sobre MouseSettingsScreen fallo
  4 iteraciones consecutivas (Redirect no matcheaba porque el metodo
  esta heredado de Screen, no definido en la subclase). Fix definitivo:
  mixin sobre Screen con filtro instanceof - postpuesto a hito dedicado.
  Los cambios se revirtieron; v0.11.14 quedo estable con solo banderas.
  Refuerza R-ITER-VISUAL-STRICT: cambio de estrategia obligatorio en
  iteracion 3.

### Reglas nuevas
- **R-MIXIN-INHERITED-METHODS**: @Inject y @Redirect NO funcionan
  directamente sobre metodos heredados. Para interceptar renderBackground
  en una subclase (MouseSettingsScreen) hay que hacer mixin sobre la
  clase que define el metodo (Screen) y filtrar con instanceof.

### KI-12 (sigue ABIERTO)
- Dirt bypass en MouseSettingsScreen, KeyBindsScreen, PackSelectionScreen,
  OnlineOptionsScreen. Ruta correcta identificada (mixin sobre Screen +
  instanceof) pero pendiente hito dedicado. Prioridad baja - no bloquea
  el arranque de gameplay.

---
## v0.12.0 - Cierre del ciclo del gabinete (administrativo)

Version sin cambios de codigo. Solo:
- Reagrupamiento del changelog in-game: se borran las entradas sueltas
  de v0.10.4, v0.11.0, v0.11.1, v0.11.10-13 y v0.11.14. Se reemplazan
  por UNA sola entrada "Version 0.10 - 0.11 - Una casa de papel" que
  narra el ciclo completo del gabinete pergamino.
- Correccion R-DIEGETICO: primera version de la entrada agrupada usaba
  palabras que rompian la ambientacion (panel, cursor, scroll,
  multijugador, pantallas, regulador). Reescrita con vocabulario 100%
  diegetico (despacho, cintas laterales, registro de hospederias,
  clavija, tiradores).
- Salto de version v0.11.14 -> v0.12.0 para senalar cierre de fase UI
  y arranque de fase de gameplay.

### Errores catalogados durante v0.11.14 -> v0.12.0
- **E111**: Intento de mixin en v0.11.14 sobre MouseSettingsScreen fallo
  4 iteraciones consecutivas. Raiz: @Redirect y @Inject NO alcanzan
  metodos heredados; renderBackground esta definido en Screen, no en
  la subclase. Los cambios se revirtieron; v0.11.14 quedo estable con
  solo banderas. Refuerza R-ITER-VISUAL-STRICT.
- **E112**: Primera version de la entrada agrupada in-game contenia
  palabras UI/gaming que violaban R-DIEGETICO: "panel", "cursor",
  "scroll", "multijugador", "pantallas", "regulador", "auxilios". El
  cronista victoriano no conoce esos terminos. Fix: reescritura completa
  con vocabulario de epoca ("despacho", "cintas laterales", "registro
  de hospederias", "clavija", "tiradores", "ayudas a la vista y al
  oido").

### Reglas nuevas
- **R-MIXIN-INHERITED-METHODS**: @Inject y @Redirect NO alcanzan
  metodos heredados por defecto. Para interceptar renderBackground en
  una subclase (MouseSettingsScreen) hay que: (a) mixin sobre la
  clase que define el metodo (Screen) con filtro instanceof; (b)
  @Redirect con owner concreto para interceptar llamada especifica;
  (c) @Inject sobre metodo que la subclase SI overridea.
- **R-DIEGETICO-VOCAB**: Formalizacion de vocabulario prohibido en
  changelog in-game. Rechazadas: pantalla/screen, cursor, scroll,
  panel, boton/button, menu, click, multijugador/multiplayer, ventana,
  interfaz, sistema, mod, version, update, patch, GUI, HUD. Aceptadas:
  papel, pergamino, tinta, despacho, cuaderno, registro, edicto,
  cintas, cinta lateral, clavija, aguja, cronista, mano, palabra,
  renglon, hoja, folio. Verificar con audit automatico antes de
  publicar cada entrada.

### KI-12 (sigue ABIERTO)
- Dirt bypass pendiente. Ruta correcta identificada: mixin sobre
  Screen.renderBackground + filtro instanceof por las 4 pantallas
  target. Postpuesto a hito dedicado. No bloquea el arranque de
  gameplay.

---
## v0.11.14 - Banderas pixel-art (KI-08 CERRADO)

Se agrega bandera 16x11 px a la izquierda de cada entrada en DiaryLanguageScreen.
Idiomas sin bandera mapeada muestran rombo tinta como fallback.

### Cambios
- **Nuevo**: client/render/FlagRenderer.java (~700 lineas). Registro Map<String, int[][]>
  con ~90 codigos de idioma. Helpers hBands, vBands, h2, nordicCross. API publica draw() + has().
- **Modificado**: client/screens/DiaryLanguageScreen.java. FLAG_SLOT=22 reservados a la
  izquierda. Bandera en left+3, y centrada. Texto centrado en zona derecha.

### Cobertura
- Ingles: 7 variantes (US, UK, CA, AU, NZ, Pirate, upside-down).
- Espanol: 22 variantes (todas las hispanas americanas + ES + AN + 419).
- Portugues, Frances, Aleman (3), Italiano, Neerlandes, Belga, Irlandes.
- Nordicos completos, Este europeo, Baltico, Griego, Turco.
- Asia oriental (JA/KO/ZH x3), Sudeste (VI/TH/ID/FIL/MS).
- Israel, Arabia, Iran, Sudafrica, Georgia, Armenia, Azerbaiyan, Kazajistan, Mongolia.
- Regionales: Cataluna, Vasco, Galicia, Wales, Scotland.

### Errores catalogados
- **E108**: PowerShell interactivo pego "pri" suelto + basura "───────" en here-string
  largo. Similar a E39. Fix: pos-limpieza puntual con .Replace().
- **E109**: Ancla asumida "## Version 0.11.13" no existia; el CHANGELOG usa "## v0.11.13"
  (sin la palabra "Version"). El throw dentro de if en PS interactivo no aborta.
  Refuerza R-PS-THROW y R-VERIFY-DISK.
- **E110**: client.jar de 1.20.1 solo trae en_us.json. Los demas idiomas se descargan
  como assets con hash. No se pueden auditar los codigos por bytecode.

### Reglas nuevas
- **R-CHANGELOG-ANCHOR-CONVENTION**: verificar la convencion real del archivo antes de
  asumir el ancla. CHANGELOG usa "## v0.X.Y" para v0.10+ y "## Version 0.X.Y" para v0.9-.
  Preferir IndexOf() con validacion explicita.
- **R-VANILLA-LANGS-1.20**: en Minecraft 1.20+ los idiomas NO estan en el client.jar.
  Solo en_us. Para auditar codigos usar reflection en runtime.

### KI-08 (CERRADO en v0.11.14)
- Banderas pixel-art hardcoded implementadas. Fallback rombo tinta para no mapeados.
- Deuda visual menor resuelta en el mismo release (Uruguay, Mexico, Ecuador, Vasco).
- Cerrada tras 3 iteraciones, respetando R-ITER-VISUAL.

---
## v0.11.10 a v0.11.13 - Cierre del ciclo del gabinete

Cinco versiones cortas encadenadas cierran los ultimos hitos visuales del gabinete pergamino. Se agrupa aqui porque forman un mismo ciclo narrativo.

### v0.11.10 - Polish: fades, scrollbar y multiplayer
- KI-15 (fades inferiores) CERRADO. Helper de fades retirado en RC para evitar velos sobre listas
  aplicado en Accessibility, Chat y Sound. Fade pergamino->transparente en top y
  bottom de la lista suaviza el corte duro.
- KI-16 (scrollbar respirada) CERRADO. drawStyledScrollThumbInPanel ahora
  usa panelRightEdge-18 (antes 14). Ademas la tapa sobre la barra vanilla es
  condicional: solo se dibuja si la barra cae dentro del panel visible, para no
  generar columnas fantasma en paneles anchos.
- Multiplayer: banda sepia detras de los servers atenuada de 0x70 a 0x58 alpha.
  El rojo neon "Offline" se lee mejor sin agredir al pergamino.

### v0.11.11 - DiaryTextField (KI nuevo M-widget)
- Nuevo widget: DiaryTextField extiende EditBox vanilla. Preserva TODA la
  logica de edicion (cursor, seleccion, Ctrl+C/V, filter, responder, formatter,
  hint) y solo overridea renderWidget para dibujar sobre pergamino.
- Fondo PAPER_STAINED con borde tinta que se aviva a rojo victoriano al focus.
  Cursor tinta parpadeante. Seleccion en banda sepia semi. Hint sepia claro
  cuando el campo esta vacio.
- Aplicado en DiaryEditServerScreen y DiaryDirectJoinScreen.
- Reflection defensiva con scan por tipo dentro de EditBox.class (sin herencia)
  para descubrir highlightPos, frame, displayPos, isEditable, hint, formatter.

### v0.11.12 - DiaryToggle
- Nuevo widget: DiaryToggle envuelve un OptionInstance<Boolean>. Persiste
  inmediatamente al cambiar via Options.save().
- Layout: label izquierda + separador + valor derecha + circulillo indicador
  (relleno si ON, hueco si OFF).
- Layout adaptativo: si el texto excede el ancho, primero omite el indicador,
  luego trunca el label con puntos suspensivos.
- Aplicado en DiaryControlsScreen para los 4 toggles booleanos: Sneak (Hold/
  Toggle), Sprint (Hold/Toggle), Auto-Jump (On/Off), Operator Items Tab (On/Off).
- Formatter custom via Function<Boolean, Component>: Sneak/Sprint muestran
  Hold/Toggle en vez de On/Off.

### v0.11.13 - Tooltips estilizados (KI-10 CERRADO)
- KI-10 CERRADO. Fondo pergamino con borde tinta en TODOS los tooltips
  del juego: Chat, Options, Inventario, botones Diary, item tooltips, etc.
- Estrategia final: TooltipEventHandler suscrito a RenderTooltipEvent.Color
  de Forge, con EventPriority.LOWEST para ejecutar despues de otros mods.
- NO usa mixin. Forge expone hook nativo que reemplaza los 4 colores del
  tooltip (backgroundStart, backgroundEnd, borderStart, borderEnd).
- Camino previo (mixin sobre TooltipRenderUtil.renderTooltipBackground)
  fue descartado tras confirmar que ese metodo NO es el que dibuja los
  tooltips reales de Forge en 1.20.1 - lo hace un lambda interno en
  GuiGraphics.renderTooltipInternal.
- Infraestructura mixin queda armada aunque sin mixins activos, lista para
  futuros hitos (ej: KI-12).

### Errores nuevos catalogados

- **E101**: Mixin 0.8.5 rechaza silenciosamente compatibilityLevel JAVA_17.
  Log dice "Compatibility level JAVA_17 specified by X.mixins.json is higher
  than the maximum level supported by this version of mixin (JAVA_13)". El
  config se ignora pero no crashea. Fix: usar JAVA_8 en gripeverde.mixins.json.

- **E102**: Aunque el compatLevel sea JAVA_8, si el .class del mixin se compila
  como Java 17 target (class version 61), Mixin lo rechaza con "Class version
  61 required is higher than the class version supported by the current version
  of Mixin (JAVA_8 supports class version 52)". Requiere compilar el mixin
  aparte con --release 8. Se intento con task mixinCompatibility en build.gradle
  pero se abandono al descubrir la solucion via event bus.

- **E103**: TooltipRenderUtil.renderTooltipBackground NO es el metodo real que
  dibuja tooltips en Forge 1.20.1. GuiGraphics.renderTooltipInternal tiene un
  lambda propio que llama fillGradient con colores tomados del
  RenderTooltipEvent.Color de Forge. Un mixin sobre TooltipRenderUtil se aplica
  sin errores pero no tiene efecto visual.

- **E104**: mods.toml requiere seccion [[mixins]] explicita en Forge 1.20.1
  para que el config sea reconocido por Mixin loader. El atributo MixinConfigs
  en MANIFEST.MF NO alcanza. Sin [[mixins]] en toml, Mixin ni siquiera menciona
  el config en logs.

- **E105**: readField<T> generico con cast implicito puede tirar
  ClassCastException si el field name pega en un field del tipo equivocado
  (ej: FIELD_VALUE "value" mapeo a un Boolean vez de String). Fix: usar
  reflection tipada (findIntField, findBoolField, findObjField con
  expected.isAssignableFrom check).

- **E106**: DiaryToggle iteracion 1 no chequeaba si el texto entraba en la
  caja. Textos largos ("Pestana de administrador: NO") se salian del boton.
  Fix: layout adaptativo de 3 niveles (con indicador -> sin indicador ->
  label truncado con ellipsis).

- **E107**: build.gradle task JavaCompile custom no incluye el output de
  compileJava en su classpath por defecto. Si el mixin importa clases del
  propio mod (DiaryTheme), la task de recompile aislada falla con "package
  does not exist". Fix: eliminar dependencias del mixin al codigo propio
  (hardcodear colores).

### Reglas nuevas

- **R-MIXIN-COMPAT**: Con Mixin 0.8.5 usar SIEMPRE compatibilityLevel JAVA_8.
  Los levels JAVA_16/17 son rechazados. Si el mod usa Java 17 features en el
  resto del codigo, el mixin especificamente debe estar limitado a sintaxis
  Java 8 O compilarse con --release 8.

- **R-MODS-TOML-MIXIN**: En Forge 1.20.1 es OBLIGATORIO declarar el bloque
  [[mixins]] config="X.mixins.json" en META-INF/mods.toml. El atributo
  MixinConfigs del manifest NO alcanza para que Forge se lo pase al Mixin
  subsystem. Sin ese bloque, el config es silenciosamente ignorado.

- **R-FORGE-EVENTS-FIRST**: Antes de escribir un mixin sobre codigo vanilla,
  verificar si Forge expone un evento propio para lo mismo. Los eventos son
  hooks oficiales, no rompen con updates, y no requieren refmap/compat level.
  RenderTooltipEvent.Color es un ejemplo perfecto: retematiza tooltips sin
  tocar bytecode.

- **R-WIDGET-OVERFLOW-3TIER**: Widgets con contenido dinamico (labels, valores,
  iconos) deben tener layout adaptativo de 3 niveles: (1) todo el contenido
  ideal, (2) omitir elementos opcionales si no cabe, (3) truncar texto con
  ellipsis si sigue sin caber. Usar font.plainSubstrByWidth() para truncar.

- **R-REFLECTION-TYPED**: Reflection para leer fields privados debe SIEMPRE
  filtrar por tipo esperado, no solo por nombre. findIntField, findBoolField,
  findObjField con expected.isAssignableFrom check evita colisiones cuando
  distintos fields comparten nombre en la jerarquia.

- **R-MIXIN-SELF-CONTAINED**: Las clases mixin no deben importar codigo del
  propio mod. Hardcodear constantes/colores directamente en la clase. Esto
  permite recompilar el mixin aislado con --release 8 sin arrastrar
  dependencias.

- **R-MIXIN-VERIFY-TARGET**: Antes de escribir un mixin, VERIFICAR con javap
  y grep del bytecode del caller que el metodo target sea realmente el que
  se ejecuta en el path de interes. TooltipRenderUtil existe pero no es
  usado por Forge; GuiGraphics.renderTooltipInternal usa su propio lambda.

### KIs cerrados en este ciclo
- KI-10 (tooltips morados) CERRADO en v0.11.13.
- KI-15 (fade inferior listas) CERRADO en v0.11.10.
- KI-16 (scrollbar respirada) CERRADO en v0.11.10.

### KIs abiertos que siguen
- KI-07 (parcial): sub-hits vanilla restantes (MouseSettings, KeyBinds,
  PackSelection, Online). Cubiertos parcialmente por KI-12.
- KI-08 (banderas pixel-art en Language). No abordado en este ciclo.
- KI-12 (dirt bypass en 4 pantallas de trabajo). Requiere mixin, ahora
  posible con infraestructura preparada.
- KI-13 (server pinneado sin proteccion). Prioridad baja.
- KI-14 (banda multiplayer vs faviconones brillantes). Prioridad baja.

Ultima actualizacion: v0.12.0
Estado: EN DESARROLLO
Version estable actual: v0.12.0
Ultimo tag publicado: v0.11.13

---

# KNOWN_ISSUES - Eternal Craft: Gripe Verde

## v0.11.10 a v0.11.13 - Cierre del ciclo del gabinete

Cinco versiones cortas encadenadas cierran los ultimos hitos visuales del gabinete pergamino. Se agrupa aqui porque forman un mismo ciclo narrativo.

### v0.11.10 - Polish: fades, scrollbar y multiplayer
- KI-15 (fades inferiores) CERRADO. Helper de fades retirado en RC para evitar velos sobre listas
  aplicado en Accessibility, Chat y Sound. Fade pergamino->transparente en top y
  bottom de la lista suaviza el corte duro.
- KI-16 (scrollbar respirada) CERRADO. drawStyledScrollThumbInPanel ahora
  usa panelRightEdge-18 (antes 14). Ademas la tapa sobre la barra vanilla es
  condicional: solo se dibuja si la barra cae dentro del panel visible, para no
  generar columnas fantasma en paneles anchos.
- Multiplayer: banda sepia detras de los servers atenuada de 0x70 a 0x58 alpha.
  El rojo neon "Offline" se lee mejor sin agredir al pergamino.

### v0.11.11 - DiaryTextField (KI nuevo M-widget)
- Nuevo widget: DiaryTextField extiende EditBox vanilla. Preserva TODA la
  logica de edicion (cursor, seleccion, Ctrl+C/V, filter, responder, formatter,
  hint) y solo overridea renderWidget para dibujar sobre pergamino.
- Fondo PAPER_STAINED con borde tinta que se aviva a rojo victoriano al focus.
  Cursor tinta parpadeante. Seleccion en banda sepia semi. Hint sepia claro
  cuando el campo esta vacio.
- Aplicado en DiaryEditServerScreen y DiaryDirectJoinScreen.
- Reflection defensiva con scan por tipo dentro de EditBox.class (sin herencia)
  para descubrir highlightPos, frame, displayPos, isEditable, hint, formatter.

### v0.11.12 - DiaryToggle
- Nuevo widget: DiaryToggle envuelve un OptionInstance<Boolean>. Persiste
  inmediatamente al cambiar via Options.save().
- Layout: label izquierda + separador + valor derecha + circulillo indicador
  (relleno si ON, hueco si OFF).
- Layout adaptativo: si el texto excede el ancho, primero omite el indicador,
  luego trunca el label con puntos suspensivos.
- Aplicado en DiaryControlsScreen para los 4 toggles booleanos: Sneak (Hold/
  Toggle), Sprint (Hold/Toggle), Auto-Jump (On/Off), Operator Items Tab (On/Off).
- Formatter custom via Function<Boolean, Component>: Sneak/Sprint muestran
  Hold/Toggle en vez de On/Off.

### v0.11.13 - Tooltips estilizados (KI-10 CERRADO)
- KI-10 CERRADO. Fondo pergamino con borde tinta en TODOS los tooltips
  del juego: Chat, Options, Inventario, botones Diary, item tooltips, etc.
- Estrategia final: TooltipEventHandler suscrito a RenderTooltipEvent.Color
  de Forge, con EventPriority.LOWEST para ejecutar despues de otros mods.
- NO usa mixin. Forge expone hook nativo que reemplaza los 4 colores del
  tooltip (backgroundStart, backgroundEnd, borderStart, borderEnd).
- Camino previo (mixin sobre TooltipRenderUtil.renderTooltipBackground)
  fue descartado tras confirmar que ese metodo NO es el que dibuja los
  tooltips reales de Forge en 1.20.1 - lo hace un lambda interno en
  GuiGraphics.renderTooltipInternal.
- Infraestructura mixin queda armada aunque sin mixins activos, lista para
  futuros hitos (ej: KI-12).

### Errores nuevos catalogados

- **E101**: Mixin 0.8.5 rechaza silenciosamente compatibilityLevel JAVA_17.
  Log dice "Compatibility level JAVA_17 specified by X.mixins.json is higher
  than the maximum level supported by this version of mixin (JAVA_13)". El
  config se ignora pero no crashea. Fix: usar JAVA_8 en gripeverde.mixins.json.

- **E102**: Aunque el compatLevel sea JAVA_8, si el .class del mixin se compila
  como Java 17 target (class version 61), Mixin lo rechaza con "Class version
  61 required is higher than the class version supported by the current version
  of Mixin (JAVA_8 supports class version 52)". Requiere compilar el mixin
  aparte con --release 8. Se intento con task mixinCompatibility en build.gradle
  pero se abandono al descubrir la solucion via event bus.

- **E103**: TooltipRenderUtil.renderTooltipBackground NO es el metodo real que
  dibuja tooltips en Forge 1.20.1. GuiGraphics.renderTooltipInternal tiene un
  lambda propio que llama fillGradient con colores tomados del
  RenderTooltipEvent.Color de Forge. Un mixin sobre TooltipRenderUtil se aplica
  sin errores pero no tiene efecto visual.

- **E104**: mods.toml requiere seccion [[mixins]] explicita en Forge 1.20.1
  para que el config sea reconocido por Mixin loader. El atributo MixinConfigs
  en MANIFEST.MF NO alcanza. Sin [[mixins]] en toml, Mixin ni siquiera menciona
  el config en logs.

- **E105**: readField<T> generico con cast implicito puede tirar
  ClassCastException si el field name pega en un field del tipo equivocado
  (ej: FIELD_VALUE "value" mapeo a un Boolean vez de String). Fix: usar
  reflection tipada (findIntField, findBoolField, findObjField con
  expected.isAssignableFrom check).

- **E106**: DiaryToggle iteracion 1 no chequeaba si el texto entraba en la
  caja. Textos largos ("Pestana de administrador: NO") se salian del boton.
  Fix: layout adaptativo de 3 niveles (con indicador -> sin indicador ->
  label truncado con ellipsis).

- **E107**: build.gradle task JavaCompile custom no incluye el output de
  compileJava en su classpath por defecto. Si el mixin importa clases del
  propio mod (DiaryTheme), la task de recompile aislada falla con "package
  does not exist". Fix: eliminar dependencias del mixin al codigo propio
  (hardcodear colores).

### Reglas nuevas

- **R-MIXIN-COMPAT**: Con Mixin 0.8.5 usar SIEMPRE compatibilityLevel JAVA_8.
  Los levels JAVA_16/17 son rechazados. Si el mod usa Java 17 features en el
  resto del codigo, el mixin especificamente debe estar limitado a sintaxis
  Java 8 O compilarse con --release 8.

- **R-MODS-TOML-MIXIN**: En Forge 1.20.1 es OBLIGATORIO declarar el bloque
  [[mixins]] config="X.mixins.json" en META-INF/mods.toml. El atributo
  MixinConfigs del manifest NO alcanza para que Forge se lo pase al Mixin
  subsystem. Sin ese bloque, el config es silenciosamente ignorado.

- **R-FORGE-EVENTS-FIRST**: Antes de escribir un mixin sobre codigo vanilla,
  verificar si Forge expone un evento propio para lo mismo. Los eventos son
  hooks oficiales, no rompen con updates, y no requieren refmap/compat level.
  RenderTooltipEvent.Color es un ejemplo perfecto: retematiza tooltips sin
  tocar bytecode.

- **R-WIDGET-OVERFLOW-3TIER**: Widgets con contenido dinamico (labels, valores,
  iconos) deben tener layout adaptativo de 3 niveles: (1) todo el contenido
  ideal, (2) omitir elementos opcionales si no cabe, (3) truncar texto con
  ellipsis si sigue sin caber. Usar font.plainSubstrByWidth() para truncar.

- **R-REFLECTION-TYPED**: Reflection para leer fields privados debe SIEMPRE
  filtrar por tipo esperado, no solo por nombre. findIntField, findBoolField,
  findObjField con expected.isAssignableFrom check evita colisiones cuando
  distintos fields comparten nombre en la jerarquia.

- **R-MIXIN-SELF-CONTAINED**: Las clases mixin no deben importar codigo del
  propio mod. Hardcodear constantes/colores directamente en la clase. Esto
  permite recompilar el mixin aislado con --release 8 sin arrastrar
  dependencias.

- **R-MIXIN-VERIFY-TARGET**: Antes de escribir un mixin, VERIFICAR con javap
  y grep del bytecode del caller que el metodo target sea realmente el que
  se ejecuta en el path de interes. TooltipRenderUtil existe pero no es
  usado por Forge; GuiGraphics.renderTooltipInternal usa su propio lambda.

### KIs cerrados en este ciclo
- KI-10 (tooltips morados) CERRADO en v0.11.13.
- KI-15 (fade inferior listas) CERRADO en v0.11.10.
- KI-16 (scrollbar respirada) CERRADO en v0.11.10.

### KIs abiertos que siguen
- KI-07 (parcial): sub-hits vanilla restantes (MouseSettings, KeyBinds,
  PackSelection, Online). Cubiertos parcialmente por KI-12.
- KI-08 (banderas pixel-art en Language). No abordado en este ciclo.
- KI-12 (dirt bypass en 4 pantallas de trabajo). Requiere mixin, ahora
  posible con infraestructura preparada.
- KI-13 (server pinneado sin proteccion). Prioridad baja.
- KI-14 (banda multiplayer vs faviconones brillantes). Prioridad baja.

Ultima actualizacion: v0.12.0
Estado: EN DESARROLLO
Version estable actual: v0.12.0
Ultimo tag publicado: v0.11.13

---

# KNOWN_ISSUES - Eternal Craft: Gripe Verde

## v0.11.9 - Coherencia del gabinete (KI-11 cerrado)

### Cambios
- MenuMusic.tick() agregado en 6 pantallas Diary que no lo tenian
  (Accessibility, Chat, Language, Skin, Sound, Options). La musica
  ya no se congela al navegar por las subpantallas.
- Clamp adaptativo en 4 paneles hardcoded (Confirm, DirectJoin,
  EditServer, Skin): Math.min(PANEL_MAX_W, this.width - N) evita
  que el pergamino se salga en GuiScale 3/4.
- DiaryLanguageScreen.onClose() ahora CANCELA en vez de aplicar.
  ESC descarta el idioma pendiente, coherente con la distincion
  entre appliedLanguageCode y pendingLanguageCode.
- drawCenteredString reemplazado por drawString(..., false) con
  centrado manual en 5 pantallas (Access, Chat, Controls, Credits,
  Sound). Elimina el doble contorno negro vanilla sobre pergamino.
- 25 lang files normalizados: 9 claves *.subtitle sin guiones
  decorativos ("- valor -" -> "valor"). La decoracion vive en el
  codigo, no en los strings.
- 7 pantallas migradas para envolver el subtitle con "- ... -" a
  la hora de renderizar. Todas las Diary respiran hoy el mismo
  patron de cabecera (title / "- subtitle -" / flavor).
- DiaryLanguageScreen: el fondo interno de la lista de idiomas
  ahora usa PaperTexture.drawClean(). Antes era un fill oscuro
  0x24100804 que se veia como cartulina pegada.
- DiaryMultiplayerScreen: banda tras el server suavizada de
  0xB0281608 (69% marron opaco) a 0x703A2416 (44% sepia claro).
  Contrasta con el texto blanco vanilla sin agredir al pergamino.
- DiaryChrome.drawPanelShadow(): nuevo helper. Sombra proyectada
  del panel abajo-derecha con offset 6px, color sepia oscuro y
  4 capas concentricas. Aplicado en 12 pantallas Diary.
- DiaryButton: fondo diferenciado del panel. Base PAPER_STAINED
  (mas oscuro que el pergamino PAPER del panel), hover ocre sucio,
  sombra externa proyectada, highlight superior. Los botones se
  leen ahora como tiras de papel apoyadas, no como parte del panel.
- DiaryChrome.drawStyledScrollThumbInPanel(): nuevo helper que
  ancla la scrollbar al borde derecho del panel y TAPA la barra
  vanilla (width/2+156) con pergamino antes de dibujar la nuestra.
  Migradas Chat, Accessibility, Sound.

### Errores nuevos catalogados
- E100: return dentro de if en PowerShell interactivo NO aborta
  el script; solo aborta la subexpresion. Combinado con IndexOf
  que devuelve -1, terminamos calculando bodyStart = -1 + longitud
  del string y reemplazamos un rango arbitrario, corrompiendo
  DiaryChrome.java (queda a 1562 bytes de 11627). Fix: restaurar
  con git checkout HEAD -- <archivo> y reinsertar. Refuerza
  R-PS-THROW: usar throw en vez de return, y validar IndexOf con
  guard antes de calcular offsets.

### Reglas reforzadas
- R-PS-THROW: reforzada. Cuando IndexOf/LastIndexOf puede devolver
  -1, chequear con if ($idx -lt 0) { throw "..." } explicito. No
  confiar en return dentro de bloques interactivos.
- R-INSERT-BEFORE-CLOSE: para agregar metodos al final de una
  clase Java, usar $c.LastIndexOf('}') y reemplazar solo el
  ultimo caracter. Robusto contra cualquier EOL/whitespace,
  a diferencia de bloques multilinea que fallan por diferencias
  sutiles de indentacion.

### KI-11 (CERRADO en v0.11.9)
- Slider "Musica del Cronista" ya aplica volumen en tiempo real
  durante el arrastre.
- Causa raiz: no era del slider ni del SoundManager. Era que
  DiarySoundOptionsScreen.tick() NO llamaba MenuMusic.tick(), asi
  que MenuMusic.updateVolumes() nunca corria mientras el slider
  se movia. Un fix de una linea (mas 5 pantallas hermanas que
  tenian el mismo problema silencioso: la musica se congelaba
  al navegar por las subpantallas).

### KI-15 (ABIERTO en v0.11.9)
- Bandas inferiores de las listas vanilla (Accesibilidad, Chat,
  Sonido) recortan el contenido de la ultima fila visible sin
  fade suave, dando efecto de "corte duro". La ausencia de fade
  se notaba menos antes cuando habia doble scrollbar; ahora al
  quedar sola una barra estilizada, el corte es mas visible.
- Solucion futura: fade blur inferior en el area de la lista
  simulando que el contenido se sale de la vista, similar al
  gradient de fade que ya usa DiaryLanguageScreen.
- Prioridad baja. Cosmetico.

### KI-16 (ABIERTO en v0.11.9)
- Barra sepia estilizada esta pegada al MARCO exterior del panel
  en Accesibilidad/Chat/Sonido (0px de margen interno). Podria
  respirar 4-6px hacia adentro.
- Prioridad muy baja. Cosmetico.
## v0.11.8 - Scroll estilizado en OptionsList (KI-09 cerrado)

### Cambios
- DiaryChrome: nuevos helpers publicos getOptionsListScrollbarX() y drawStyledScrollThumbForOptionsList().
- DiaryChrome: thumb minimo aumentado de 20 a 32px.
- DiaryChrome: comentario obsoleto sobre "OptionsList final" corregido.
- DiaryAccessibilityOptionsScreen: panel ampliado a 380px, scroll estilizado activo.
- DiaryChatOptionsScreen: scroll estilizado activo.
- Intento de Hito H (DiaryControlsScreen via router + Controlling compileOnly) revertido por decision de scope. Codigo aprendido queda en backup.

### Errores nuevos catalogados
- E81: Comentario en DiaryChrome afirmaba "OptionsList es final" pero era falso. Verificar con javap antes de aceptar comentarios como verdad.
- E82: renderTooltip NO existe en Screen 1.20.1. Sistema deferred con renderWithTooltip final. Interceptar requiere mixin.
- E83: .Replace multilinea con \r\n explicitos puede fallar por diferencias de EOL sutiles. Fallback: ReadAllLines + Where-Object filter por linea.
- E84: Nombres de artifacts en maven BlameJared son case-sensitive (Searchables con S mayuscula, no searchables).

### Reglas nuevas
- R-DOC-VERIFY: comentarios que documentan limitaciones deben verificarse contra bytecode antes de aceptarse.
- R-VERIFY-JAR-CASE: nombres de artifacts maven case-sensitive; verificar via HTTP GET al maven repo.

### KI-10 (ABIERTO en v0.11.8)
- Tooltips vanilla morados sobre pergamino rompen la estetica.
- En 1.20.1 el sistema deferred (Screen.renderWithTooltip es final) impide override directo desde subclase.
- Solucion futura: mixin sobre Screen.renderWithTooltip o TooltipRenderUtil.
- Aplicaria globalmente a todas las Diary screens con tooltips.
- No bloqueante.

### Hito H (DiaryControlsScreen) - postpuesto
- Estrategia E (router + Controlling compileOnly + wrapper reflection) funciono tecnicamente.
- Revertido antes de validar visual por decision de scope: no depender de Controlling ni siquiera opcionalmente.
- Codigo en `_backups\pre_v0.11.8_20260722_155547\`.
- Retomar en hito futuro con decision de scope tomada de entrada.

### Deuda tecnica actualizada
- Widget "Duracion de las notificaciones" en Accesibilidad sigue con texto cortado. Widget vanilla 150px hardcoded. Panel a 380px alivia pero no resuelve. Limitacion vanilla ya catalogada, no se corrige.
- Tooltips morados vanilla (KI-10).

---

## v0.11.3 - DiaryAccessibilityOptionsScreen + pulido de Chat y Lenguas (KI-07 sigue avanzando)

### Cambios
- Nueva clase DiaryAccessibilityOptionsScreen que hereda de AccessibilityOptionsScreen.
- Chat y Lenguas ganan: subtitulo, flavor text, esquineros dobles, divisores con rombo tipografico central.
- Accesibilidad ampliada a PANEL_H=395 para mostrar las 20 opciones sin scroll.
- Emblemas figurativos (ojo, sobre, fleur, estrella, cruz de Malta) probados y descartados: no leen a escala pixel-art vanilla; se removieron.
- Sombras vanilla de drawCenteredString eliminadas via drawString con dropShadow=false y centrado manual.

### Errores nuevos catalogados
- E66: SimpleOptionsSubScreen.render() pinta fondo dirt + titulo vanilla. Al ambientar hay que hacer bypass: NO llamar super.render(), dibujar this.list.render() manualmente y ocultar footer widgets.
- E67: DiaryButton con Icon+Variant espera Runnable, no Consumer<Button>. Usar this::onClose o () -> ..., no b -> ....
- E68: list.render() tapa cosmeticos previos dibujados en el area de la lista. Los divisores/esquineros deben ir DESPUES de list.render().
- E69: [System.IO.File]::WriteAllText con [System.Text.Encoding]::UTF8 escribe BOM en PowerShell 5. Compilar .java con BOM da error illegal character '\ufeff'. Usar new UTF8Encoding(False).
- E70: drawCenteredString fuerza dropShadow=true de la fuente vanilla. Genera doble contorno feo en subtitulos sobre pergamino claro. Alternativa: drawString(..., dropShadow=false) con centrado manual x - font.width(text)/2.
- E71: PowerShell 5 queda esperando cierre (>>) cuando hay subexpresiones anidadas $(( ... )) con parentesis + comillas dentro de Write-Host multilinea. Calcular en variables antes de interpolar.

### Reglas nuevas
- R-NO-DROPSHADOW-CENTERED: para textos en pergamino, usar drawString(..., dropShadow=false) con centrado manual en vez de drawCenteredString.
- R-EMBLEM-SKIP: no intentar emblemas figurativos (ojo, sobre, flor, estrella) con gfx.fill() a menos de 20px de ancho. A esa escala solo funcionan simbolos geometricos abstractos simples (rombo, cruz, punto).
- R-PS-NOSUBEXPR-MULTILINE: evitar $((...)) anidados en Write-Host cuando el bloque tiene multiples lineas seguidas. Calcular en variables primero.
- R-UTF8-NOBOM: siempre usar New-Object System.Text.UTF8Encoding $false para escribir .java. [System.Text.Encoding]::UTF8 incluye BOM y rompe compilacion.
- R-SUB-SCREEN-STRATEGY: al ambientar una subpantalla vanilla que extiende SimpleOptionsSubScreen, NO llamar super.render(). Dibujar fondo propio, luego this.list.render(), luego cosmeticos superpuestos, luego boton Volver propio. Widgets vanilla del footer (narratorButton, done) se ocultan via visible=false iterando children() en init().
- R-COSMETIC-ORDER: elementos cosmeticos sobre areas donde luego se dibuja una lista vanilla deben renderizarse DESPUES de la lista.

### KI-07 estado
- Sub-pantallas Diary implementadas: Idioma (v0.11.1), Chat (v0.11.2), Accesibilidad (v0.11.3).
- Restantes vanilla: Controles, Mi aspecto, Musica y sonidos, Telemetria, En linea, Paquetes de recursos, Creditos.

### Deuda tecnica
- Barra de scroll vanilla gris en DiaryLanguageScreen rompe estetica pergamino. Reemplazo por thumb tinta victoriana queda para Fase 6 en hito futuro.
- Slider vanilla "Duracion de las notificaciones" en Accesibilidad tiene texto que se corta a la derecha por ancho fijo 150px del widget. Limitacion vanilla. Igual que en Chat (Duracion notificaciones), no se corrige.


## ARCHIVO HISTORICO: INSTANTANEA v0.11.8 (NO VIGENTE)

Los campos siguientes se conservan como evidencia de aquella etapa. No
representan la version, el tag, el estado ni las instrucciones actuales; el
estado operativo vigente esta al comienzo de este documento.

Ultima actualizacion de la instantanea: v0.11.8
Estado archivado: EN DESARROLLO
Version estable de la instantanea: v0.11.8
Ultimo tag conocido en la instantanea: v0.11.8
Hitos recientes de la instantanea: v0.11.0 (DiaryOptionsScreen sobre pergamino con Title vivo detras, KI-06 cerrado extendido, sub-pantallas vanilla integradas); v0.10.4 (bump administrativo, changelog in-game intencionalmente sin entrada); v0.10.1 (persistencia del volumen de musica); v0.10.0 (ChangelogScreen sobre pergamino + reescritura diegetica de las 25 cronicas, KI-06 cerrado); v0.9.2 (fix humo/nubes, KI-05 cerrado); v0.9.1 (faroles reactivados, KI-03 cerrado completo); v0.9.0 hito visual del skyline; sistema de musica (v0.8.x) estable.

---

## 1. Version archivada en esta instantanea

- Proyecto: Eternal Craft: Gripe Verde
- Base: Minecraft Forge 1.20.1
- Version desplegada: 0.11.1
- Tipo: mod de interfaz + audio
- Ambientacion: ciudad victoriana bajo cuarentena, Green Flu
- Estado general: compilando limpio. Sistema de musica del Title con persistencia (v0.10.1). ChangelogScreen sobre pergamino y cronicas diegeticas (v0.10.0). Mejoras visuales previas de v0.9.x estables.

---

## 2. Sistemas activos

### Title / DiaryTitleScreen
Pipeline de fondo (estructura estable desde v0.7.1; ajustes visuales en v0.9.0):
1. AtmosphereRenderer.renderSky
2. VictorianSkyline.renderBackground
3. LivingAtmosphere.renderFogLow
4. VictorianSkyline.renderForeground
5. LivingAtmosphere.renderStreetLevel
6. LivingAtmosphere.renderAir
7. LivingAtmosphere.renderFlash
8. LivingAtmosphere.renderVignette

### MenuMusic (v0.8.3)
- 4 pistas registradas: music_title_1..4 (OGG stream)
- Playback aleatorio (shuffle), sin repeticion hasta completar ciclo
- Crossfade de 4s entre pistas
- Ducking automatico (35% durante 2.5s) al reproducir SFX RARE
- Volumen ajustable en tiempo real via MusicVolumeSlider
- Idempotente: start() no reinicia si ya suena
- Auto-stop al detectar mc.level != null (jugador en mundo)
- Persistencia: sobrevive a apertura de Options y subpantallas
- FadingMusicInstance: soporta volumen dinamico sin recrear el sample

### MenuAmbience
- Sin cambios funcionales; integracion nueva: llama a MenuMusic.duckFor(2500L) al disparar sonidos RARE

### MusicVolumeSlider
- Widget nuevo en client/widgets
- Se inyecta en OptionsScreen via ScreenEventHandler.onScreenInitPost
- Posicion: centrado horizontal, y = screen.height - 25 (pie de pagina, seguro para todas las escalas)

### UiSounds
- Version defensiva con safePlay() que tolera RegistryObject vacio
- Workaround para incompatibilidad con ModernFix RegistryObjectMixin

### LivingAtmosphere
- Sin cambios desde v0.7.0 (calle adaptativa)

### VictorianSkyline
- Sin cambios desde v0.7.1 (postes ocultos)

---

## 3. Problemas conocidos activos

### KI-01 - Freeze al presionar Leave (RESUELTO)
Sintoma original: podia quedar en "Saving world".
Estado: no reproducible en v0.9.x. Se marca como cerrado.
Sospecha historica: entorno, RAM, GPU, Embeddium.

### KI-02 - Crash al hacer screenshots F2 (RESUELTO)
Sintoma original: NVAPI + Embeddium + NVIDIA.
Estado: no reproducible en v0.9.x. Se marca como cerrado.
Buena practica sugerida: usar Windows + Shift + S como alternativa segura.

### KI-03 - Fondo del Title con defectos visuales (RESUELTO COMPLETO en v0.9.1)
- Calle definida: RESUELTO en v0.7.0.
- Postes/faroles flotantes: RESUELTO en v0.9.1 (reactivados con anclaje a la calle, densidad por escala GUI).
- Vacio entre skyline FG y calle: ATENUADO por jerarquia visual de v0.9.0.
- Skyline demasiado poblado / sin jerarquia: RESUELTO en v0.9.0 (BG/FG diferenciados).
- Luna solitaria: contexto atmosferico mas rico alrededor de la luna queda como mejora futura opcional (no es bug).

### KI-04 - Incompatibilidad con ModernFix RegistryObjectMixin
Sintoma: NullPointerException en RegistryObject.get() al hoverear botones.
Causa: mixin de ModernFix sobre net.minecraftforge.registries.RegistryObject.
Workaround: UiSounds.safePlay() tolera excepcion y omite el sonido sin crashear.
Estado: MITIGADO.

### Deuda tecnica activa
- ICON_BTN_MP e ICON_BTN_MODS sin PNG real.
- Framework UI subutilizado (solo Title lo consume).
- ThemeManager sin themes registrados.
- Slider de musica no persiste en disco (se pierde al cerrar juego).

---

## 4. Historial relevante reciente

### v0.7.0 - Sub-hito 1 KI-03
- renderStreet() adaptativo agregado.

### v0.7.1 - Sub-hito 2 KI-03
- getLanternPositions() devuelve int[0][0].

### v0.8.0 - Sistema de musica base
- 4 pistas OGG registradas.
- MenuMusic con playback secuencial shuffle.
- Fix critico: sounds.json corrupto por append incorrecto.
- Fix critico: mod_version hardcodeado en build.gradle (E32).
- Fix critico: UiSounds defensivo contra ModernFix (KI-04).

### v0.8.1 - Slider de volumen
- MusicVolumeSlider widget.
- Inyeccion en OptionsScreen via ScreenEventHandler.
- Reubicado 3 veces por problemas de escala; posicion final: pie de pagina.

### v0.8.2 - Crossfade
- FadingMusicInstance creado.
- MenuMusic reescrito para soportar dos instancias simultaneas.

### v0.8.3 - Ducking + persistencia
- MenuAmbience llama duckFor() en RARE.
- MenuMusic.start() idempotente.
- Auto-stop cuando jugador entra a un mundo.
- Se detecta perdida de instancia al salir de servidor y se reinicia.

---

## 5. Reglas vigentes

### Desarrollo
- UN sistema por version.
- UN cambio visual sensible por bump.
- Validacion visual/auditiva en 4 escalas cuando se toque UI/audio.
- No modificar mas de un renderer por sesion sin plan escrito previo.
- Auditar estado real antes de proponer cambios.
- Java UTF-8 sin BOM (UTF8Encoding false).
- Commit + push tras cada version estable.

### Build y Deploy
- R-BUILD: Build siempre acompanado de deploy.
- R-JAR: Limpiar jars gripeverde-*.jar viejos del deploy path antes de validar.
- R-VER: Validar la version visible en el reloj antes de aprobar o rechazar.
- R-GRADLE-VER: mod_version en build.gradle debe usar project.version.toString(), nunca hardcodear.

### PowerShell
- R-CD: Todo bloque de comandos debe iniciar con Set-Location al repositorio para evitar errores de ruta (E35).
- R-PS: Minimizar cantidad de bloques PowerShell.
- R-PATH: Usar rutas absolutas si no se esta parado en la raiz del repo.
- R-PATH2: gradlew y git tambien requieren estar en la raiz del repo. Si el bloque falla y estamos en system32, Set-Location primero.

### Validacion
- R-CACHE: LivingAtmosphere cachea lanternPositions; reiniciar cliente al cambiar GuiScale si hay duda.
- R-CAP: Capturas pre/post en 4 escalas antes de validar cambios visuales.
- R-JSON: Validar JSON con ConvertFrom-Json tras cualquier append/edit a sounds.json u otros JSONs criticos.

### Java
- R-DUP: Cuando se agregan bloques nuevos que declaran variables locales, verificar que no existan ya en scope superior del metodo.

---

## 6. Errores historicos importantes

### E20 - Escritura con BOM rompio compilacion
Solucion: usar UTF8Encoding(false) al escribir Java desde PowerShell.

### E21/E23 - Parseo ingenuo de .deploy
Solucion: filtrar por DEPLOY_MODS_PATH= y no asumir primera linea.

### E27 - Replaces multi-cambio simultaneos
Regla: un replace por vez, verificacion inmediata.

### E28 - Formulas cross-file
Regla: no crear dependencias silenciosas entre renderers.

### E29 - Sin capturas de referencia
Regla: capturas pre/post en 4 escalas.

### E30 - Extrapolar entre escalas
Regla: validar escala por escala.

### E31 - Sistemas interdependientes en mismo bump
Regla absoluta: un sistema por version.

### E32 - mod_version hardcodeado en build.gradle
Sintoma: jar dice v0.7.1 en el nombre pero mods.toml interno decia v0.6.1.
Causa: build.gradle tenia mod_version: '0.6.1' hardcodeado en vez de project.version.toString().
Solucion: usar project.version.toString() para que Gradle expanda dinamicamente.

### E33 - JSON append rompio sounds.json
Sintoma: WARN Invalid sounds.json, todos los sonidos del mod silenciados.
Causa: append manual con string dejo coma huerfana antes del cierre del JSON.
Solucion: regenerar el JSON completo desde plantilla + validar con ConvertFrom-Json.

### E34 - Redefinicion de variable local en bloque agregado
Sintoma: BUILD FAILED, variable mc is already defined in method tick().
Causa: se agrego "Minecraft mc = Minecraft.getInstance()" al inicio de tick() sin verificar que otro bloque interior ya la definia.
Solucion: R-DUP.

### E35 - PowerShell corriendo desde system32
Sintoma: .\gradlew no se encuentra, git fatal not a git repository.
Causa: bloque no hizo Set-Location al repo antes de comandos de build/git.
Solucion: R-PATH2. Siempre Set-Location $repo al inicio de bloques con gradlew/git.

---

## 7. Plan futuro inmediato

- v0.9.1: COMPLETADO (faroles reactivados).
- v0.9.2: COMPLETADO (humo/nubes con blobs suaves, KI-05 cerrado).
- (Opcional) v0.10.0: Persistencia del volumen en disco / mejoras de calidad de vida del sistema de musica.
- Deuda visual pendiente: contexto atmosferico mas rico alrededor de la luna (nubes, halo mayor).

## 8. Estado tecnico

- Repo local: C:\Users\santi\Desktop\EternalCraft_Mod\GripeVerde
- Remoto: https://github.com/Santi-PdR/GripeVerde
- Deploy: D:\Juegos\Minecraft\Instances\Test\mods
- Ultimo tag: v0.11.4
- Build limpio.
- Working tree esperado: limpio antes de proximas sesiones.

### KI-06 - Menu del Historial de Cambios sin ambientacion victoriana (RESUELTO en v0.10.0)
- ChangelogScreen ya no usa UI vanilla: panel de pergamino, boton al estilo DiaryButton y cronicas reescritas en tono diegetico.

### KI-07 - Subpantallas de Opciones siguen usando UI vanilla (PARCIAL)
- v0.11.3: Idioma cerrado (DiaryLanguageScreen sobre pergamino, con lista limpia, sin fondo dirt ni barra de seleccion vanilla).
- Siguen vanilla: Controles, Chat, Accesibilidad, Mi aspecto, Musica y sonidos, Telemetria, En linea, Paquetes de recursos, Creditos.
- Estado: funcional (se abren y vuelven sin crash), pero rompe la estetica en las que aun no fueron ambientadas.
- Plan: continuar de a una en v0.11.x. Musica y sonidos como proximo candidato natural.

### KI-05 (RESUELTO en v0.9.2)
- Humo de chimeneas, nubes del skyline y nube frente a la luna se dibujaban como rectangulos por g.fill.
- Solucion aplicada: AtmosphereRenderer.drawSoftBlob con 6 anillos concentricos y alpha cuadratico decreciente.
- Nubes reducidas a 2 blobs con radio capado a 20px para evitar gasto excesivo en radios grandes.

---

## E43 - System.IO.File con ruta relativa

Sintoma: [System.IO.File]::ReadAllText / WriteAllText con ruta relativa fallan si el CWD del proceso .NET no coincide con Set-Location de PowerShell (ocurre al abrir nueva instancia PS que arranca en C:\\Users\\<user>).
Solucion: siempre pasar ruta absoluta con $PWD\\... explicito. Cubierto por R-DOTNET-PATH y R-CD.


### KI-08 (ABIERTO)
- Deuda visual: banderas / emojis por idioma en DiaryLanguageScreen.
- La font vanilla de Minecraft no renderiza regional indicators (secuencias tipo U+1F1EA U+1F1F8 = ES) ni emojis a color.
- Force Unicode Font tampoco resuelve: renderiza como cuadros o glifos genericos.
- Solucion futura: font provider propio del mod (assets/gripeverde/font/*.json) mapeando code points custom a PNGs con banderas dibujadas manualmente en pixel-art victoriano (no fotorealistas).
- Alcance estimado: sub-hito propio, no bloqueante.

### KI-09 (CERRADO en v0.11.8)
- Scroll estilizado en pantallas que heredan de SimpleOptionsSubScreen (principalmente Accesibilidad).
- OptionsList vanilla NO puede recibir tratamiento visual completo:
  - OptionsList.Entry es protected y no se puede calificar desde otro paquete
  - En runtime PROD los campos y metodos tienen nombres SRG (f_XXXXX_, m_XXXXX_) que dificultan reflection
- Impacto: Accesibilidad muestra scroll vanilla gris. Funcional, feo.
- Solucion futura: reimplementar OptionsList como widget propio (DiaryOptionsList) que NO herede de vanilla, componga entries manualmente desde OptionInstance<?>.
- Sub-hito propio cuando se planifique (Candidato M).


### Errores post-v0.11.4

- **E72**: HumanoidArm.getName() no existe en 1.20.1. Correcto: getCaption() via OptionEnum.
- **E73**: DiarySkinCustomizationScreen iteracion 1 con super.init() vanilla mostro CycleButtons feos tapando titulo. Fix: reimplementar desde cero.
- **E74**: PowerShell interpreto lineas de Select-String como cmdlets. No es bug de codigo, es ruido de PS. Solucion: capturar output en variable, inspeccionar con .Contains().
- **E75**: renderDecorations() no tapa scroll vanilla (vanilla lo dibuja despues). Fix: override render() completo.
- **E76**: Thumb 3px sobre barra vanilla de 6px dejo lineas grises visibles. Fix: tapar 6px enteros con pergamino primero.
- **E77**: Reflection getDeclaredField("width") falla en runtime porque Forge sirve SRG (f_93388_), no official. Debug via log lo confirmo.
- **E78**: Subclasear OptionsList con extends fallo por OptionsList.Entry protected. Cast (Entry) desde otro paquete no compila.
- **E79**: Refuerzo R-UTF8-NOBOM. UTF8Encoding(False) siempre.
- **E80**: Copy-Item con $jar null tira excepcion. Fix: if ($jar) { Copy-Item }.

### Reglas nuevas post-v0.11.4

- **R-SRG-RUNTIME**: Reflection contra vanilla debe probar nombres official (dev) y SRG (prod) en cascada + fallback siempre + log de resolucion.
- **R-VANILLA-INNER-PROTECTED**: Subclasear vanilla con inner types protected desde otro paquete es problematico. Alternativas: dibujar encima, reimplementar completo, marcar como deuda.
- **R-DEBUG-LOG-FIRST**: Antes de reflection compleja, hacer pasada de debug con LogUtils para descubrir nombres reales en runtime. Ahorra iteraciones.
- **R-ITER-VISUAL-STRICT**: Refuerzo. Si iteracion 3 tampoco resuelve un bloqueo tecnico, revertir sin excepciones. En esta sesion se violo (5+ iteraciones en Accesibilidad scroll) antes de aceptar reversion.


---

## KI-11 (CERRADO en v0.11.9) - Slider del Cronista no aplica en vivo

**Estado**: activo. Impacto menor.

**Sintoma**: al arrastrar el slider "Musica del Cronista" dentro de DiarySoundOptionsScreen, el volumen no cambia audiblemente durante el drag. El cambio se percibe al soltar el mouse (o al proximo tick de MenuMusic, tipicamente <=50ms despues).

**Comportamiento correcto**: el valor SI se persiste inmediatamente en ClientConfig.MUSIC_VOLUME (el override de onDrag llama applyValue en cada frame de arrastre). El bug es solamente que el audible no se sincroniza con el visual.

**Causa probable**:
- AbstractSliderButton.onDrag() llama applyValue() correctamente.
- applyValue() persiste via MenuMusic.setUserVolume().
- MenuMusic.updateVolumes() lee getUserVolume() y aplica al currentInstance via setDynamicVolume().
- Pero updateVolumes() solo se llama desde MenuMusic.tick(), y aunque la screen tiquea MenuMusic (v0.11.8 fix), SoundManager de MC puede estar cacheando el volumen del SoundInstance hasta el proximo audio buffer swap.

**Plan**: sub-hito K3 en v0.11.x. Investigar si hay que forzar refresh del SoundManager o si MenuMusic necesita un pulso adicional en cada frame (no solo en tick).

**No bloqueante**.

## KI-12 (ABIERTO en v0.11.8)

Fondos dirt vanilla persisten en pantallas de trabajo:
- MouseSettingsScreen (Controles -> Mouse)
- KeyBindsScreen (Controles -> Atajos)
- PackSelectionScreen (Options -> Paquetes de recursos)
- OnlineOptionsScreen (Options -> En linea)

Causa: en 1.20.1, estas pantallas pintan renderDirtBackground
DIRECTAMENTE desde su render() sin pasar por el hook renderBackground.
Un override simple en subclase no funciona (ver E89).

Solucion: mixin @Redirect o @Inject sobre esas pantallas.

Plan: v0.11.8 hito mixin dedicado. Investigar previamente por que
fallo el refmap en el intento de v0.11.8 (E90).

No bloqueante. Las pantallas funcionan, solo lucen vanilla.


---
## v0.11.8 (Multiplayer completo)

### KIs abiertos nuevos
- **KI-13**: Server pinneado editable/borrable sin bloqueo visual explicito.
  ensurePinnedServer() lo restaura en el proximo init, pero seria mas
  claro mostrar ConfirmScreen antes de borrarlo o desactivar el boton.
  Prioridad: baja.
- **KI-14**: Banda sepia bajo el server puede verse muy oscura contra
  faviconones brillantes. Adaptacion dinamica de opacidad seria mejor.
  Prioridad: baja.

### KIs que siguen abiertos
- KI-07 (parcial): faltan MouseSettings, KeyBinds, PackSelection, Online
- KI-08: banderas pixel-art
- KI-10: tooltips morados vanilla
- KI-12: 4 pantallas con dirt bypass (Mouse, KeyBinds, PackSelection, Online)
