# REVISIÓN TÉCNICA - Eternal Craft: Gripe Verde v0.22.0

> **ADDENDUM 2026-08-23 (crash en campo):** llegó un crash-log real de un
> jugador con la edición v0.22.0 — `ArrayIndexOutOfBoundsException` en
> `DiaryTitleScreen.drawEasterEggMessage` (la lista de `font.split("")`
> vacía se indexaba con `lineCount` forzado a ≥1 en el primer fotograma de
> la máquina de escribir). Es un bug que esta revisión no detectó; quedó
> corregido y auditado en **v0.23.2 - El telegrama llega intacto**
> (validación pendiente como KI-55, jar por reenviar al jugador).

> **ESTADO AL CIERRE DE LA SESIÓN (2026-08-23):** todos los hallazgos A1-A6,
> C1-C4, D1-D2, F1-F2, G1-G3 (G3 verificado como ya cubierto: los sliders
> narran su etiqueta + porcentaje por defecto), H1-H2 fueron implementados en
> la rama `arena/01a02f45-gripeverde` como **v0.23.0**, con las cinco
> verificaciones de build espejadas en Python pasando al 100 % (25 lang × 93
> claves, 25 changelogs, 35 eventos, 42 refs/42 OGG, version sync, duraciones
> Vorbis, lista negra de código muerto). Bonus descubierto durante la
> implementación: la etiqueta de `DiaryAmbienceSlider` estaba hardcodeada en
> castellano pese a existir su clave traducida — corregida. Lo que queda sin
> implementar intencionalmente está en "PROPUESTAS FUTURAS" al final.

Auditoría de código + verificaciones ejecutadas el 2026-08-23 sobre la rama
`arena/01a02f45-gripeverde` (commit `4ca038d`, tras merge de PR 14).

Método: revisión manual de las 60 clases Java, `build.gradle`, `mods.toml`,
`sounds.json`, documentación operativa; parseo real de los 42 OGG con un
lector Vorbis (granule final / sample rate); contraste con prácticas
oficiales de Forge (Sides wiki) y comportamiento del `MusicManager` vanilla.

Resumen de salud del proyecto: **muy bueno**. Los validadores de build son de
nivel profesional (paridad 25x83, cobertura OGG exacta, lista negra de
código muerto). Las mejoras abajo son refinamiento, no rescate.

---

## A. HALLAZGOS (posibles bugs reales)

### A1. Registro de eventos duplicado y peligroso en servidores — PRIORIDAD ALTA

`GripeVerdeMod.java:29`:

```java
MinecraftForge.EVENT_BUS.register(new ScreenEventHandler());
```

`ScreenEventHandler` **ya** está anotado con
`@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)` (línea 29) y
sus métodos `@SubscribeEvent` son estáticos. Consecuencias:

1. **Ruta doble en cliente**: la anotación autorregistra la clase; el
   registro manual la inscribe otra vez en el mismo bus FORGE. El guard
   `redirecting` y los cooldowns de `UiSounds` (80-500 ms) enmascaran hoy
   los efectos (doble sonido/doble redirect), pero cualquier futura
   suscripción nueva heredará el doble disparo.
2. **Crash en servidor dedicado**: el constructor del mod corre en AMBOS
   lados. Instanciar y registrar `ScreenEventHandler` fuerza la carga de
   `net.minecraftforge.client.event.ScreenEvent$*`, clases que no existen en
   el servidor dedicado → `NoClassDefFoundError` al arrancar. La wiki de
   Forge lo prescribe explícitamente: en mods de un solo lado hay que
   registrar handlers con `@Mod.EventBusSubscriber(value = Dist.CLIENT)` o
   `DistExecutor`, nunca manualmente en el constructor.

**Fix propuesto**: borrar la línea 29 del constructor (la anotación basta).
El resto del constructor (`ModSounds`, `ClientConfig`, shutdown hook de
`ChroniclerMemory`) solo toca clases comunes (GSON, FMLPaths), así que el
mod queda inerte y seguro en servidor. Complemento: `displayTest` en
mods.toml (ver C2).

### A2. La música vanilla del menú NO está suprimida — PRIORIDAD ALTA (verificar en cliente)

No existe ninguna referencia a `MusicManager` / `getMusicManager` /
`stopPlaying` en todo el código. En 1.20.1, `Minecraft.getSituationalMusic()`
devuelve `Musics.MENU` siempre que `player == null`, y `MusicManager.tick()`
la dispara tras 20-600 ticks (1-30 s) en **cualquier** pantalla de menú,
incluida `DiaryTitleScreen`. Es decir: pasado un rato en el Title, la
bandeja vanilla ("Subwoofer Lullaby" y cía.) suena **encima** de las 4
pistas del mod, salvo que el usuario tenga el slider vanilla de Música a 0.
(Mismo problema histórico documentado por FancyMenu y The Betweenlands,
que tuvieron que añadir control explícito.)

**Fix propuesto** (sin mixins): en `MenuMusic.start()` y como guardia en
`QuarantineSceneRenderer.tick()`:

```java
var mm = mc.getMusicManager();
if (mm.isPlayingMusic(mc.getSituationalMusic())) mm.stopPlaying();
```

Alternativa más fina: apagar solo `Musics.MENU`. Validar en cliente real si
hoy se percibe el solape (podría explicar reportes de "música rara").

### A3. Watchdog de música: muerte definitiva tras F3+T — PRIORIDAD MEDIA

`FadingMusicInstance.tick()` es quien pone `done = true` al expirar, pero
ese tick lo ejecuta el SoundEngine. Si el motor se reinicia (F3+T, cambio
de resourcepack, recarga) con la instancia activa, la instancia queda
huérfana con `done = false`. Entonces en `MenuMusic.tick()`:

- `currentInstance.isStopped()` → false (nunca más se tiquea),
- `getRemainingMs()` → 0, pero el crossfade exige `remaining > 0`,

resultado: **la música del menú nunca vuelve** hasta cambiar de pantalla.
`DiaryLanguageScreen` cubre solo el caso de cambio de idioma llamando
`recoverAfterResourceReload()`.

**Fix propuesto**: en `MenuMusic.tick()`, tratar como cadáver también
`!mc.getSoundManager().isActive(currentInstance)` o
`elapsed > duration + 2000` → `queueNextWithGap()`. Tres líneas.

### A4. Infraestructura de Mixins vacía y mal configurada — PRIORIDAD MEDIA

`gripeverde.mixins.json` declara `client: []`, `mixins: []` (cero mixins)
pero `required: true` y `compatibilityLevel: JAVA_8` (el mod es Java 17).
Además `build.gradle` carga el plugin `org.spongepowered.mixin`, el
`annotationProcessor` de mixin y genera refmap para nada, y `mods.toml`
declara `[[mixins]]`.

**Fix propuesto**: eliminar el JSON, la entrada `[[mixins]]` de mods.toml,
el bloque `mixin {...}`, el plugin y el annotationProcessor. Build más
rápido, jar más limpio y un punto menos de fallo (`required: true` con
config vacía puede fallar en entornos estrictos). Si se prefiere conservarlo
"por si acaso": al menos `JAVA_17` + `required: false`.

### A5. Atajo oculto Ctrl+0 en producción — PRIORIDAD MEDIA (decisión de diseño)

`ScreenEventHandler.onClientTick` implementa `leaveWorldSilently`: Ctrl+0
desconecta del mundo instantáneamente, sin menú ni confirmación. Es un
atajo de administrador/debug no documentado que se distribuye al jugador
final. Riesgo: desconexión accidental (Ctrl+0 existe como combinación de
zoom en varios SO/launchers) y código de depuración en release.

**Opciones**: borrarlo, o gatearlo detrás de una clave de config
(`debugShortcut`, default `false`).

### A6. API muerta en `UiSounds` — PRIORIDAD BAJA

`setHoverSound(...)`, `setClickSound(...)` son cuerpos vacíos; `hover()` es
un no-op "legacy". Nada las llama. Borrarlas (y añadirlas a la lista negra
de `verifyGripeVerdeNoDeadCode` para que no vuelvan).

---

## B. VERIFICACIONES AUTOMÁTICAS EJECUTADAS AHORA

| Comprobación | Resultado |
|---|---|
| Duración real OGG vs `TRACK_DURATIONS_MS` (parseo Vorbis: último granule / sample rate) | ✅ pista1 160.34s vs 160 (+344 ms), pista2 90.09, pista3 177.01 (+6 ms), pista4 160.06 — todas dentro de holgura del crossfade de 4 s |
| 42 OGG presentes, sin huérfanos (según sounds.json) | ✅ total 8.1 MB |
| Paridad lang 25 x 83 claves (vía validador de build existente) | ✅ (confirma el validator) |
| Sample rates | ⚠️ pista4 = 48 kHz (resto 44.1 kHz), `menu/tick` = 22 050 Hz — funcional pero heterogéneo; normalizar ahorraría ~0.5-1 MB y evita remuestreo en runtime |

Detalle útil: los diffs (+6..+344 ms) nunca producen corte audible porque el
crossfade empieza 4 000 ms antes del "fin" declarado.

---

## C. MEJORAS DE EMPAQUETADO / METADATOS

- **C1.** `forge.enabledGameTestNamespaces = 'gripeverde'` en `build.gradle`
  y en ambos runs, pero **no existe ningún GameTest**. Quitarlo (o usarlo
  de verdad, ver E3).
- **C2.** `mods.toml`: `issueTrackerURL=""` vacío → poner
  `https://github.com/Santi-PdR/GripeVerde/issues`. Añadir
  `displayTest="IGNORE_ALL_VERSION"` (mod 100 % cliente, sin componente de
  servidor; es el caso de uso documentado por Forge) y un `logoFile`
  (`logo.png` del sello EC-QV/1897) para que la ficha del mod en Mod List
  tenga identidad visual.
- **C3.** Versión en 3 sitios (`build.gradle`, `GripeVerdeMod.VERSION`,
  changelogs). Añadir task `verifyGripeVerdeVersionSync` que falle si
  `build.gradle version != GripeVerdeMod.VERSION`; es Regex de 5 líneas y
  cierra una clase entera de errores de release.
- **C4.** README "Deploy": rutas y jar hardcodeados a `0.22.0`. Un
  `deploy.ps1` parametrizado (lee la versión de gradle) elimina la edición
  manual del README por release.

---

## D. INFRAESTRUCTURA (las dos de mayor impacto)

- **D1. GitHub Actions CI — resuelve la raíz de KI-52.**
  El bloqueo actual es "el sandbox no tiene Java 17". Un workflow
  `.github/workflows/build.yml` con `actions/setup-java` (temurin 17) +
  cache de Gradle ejecutando `./gradlew clean build` compila
  semánticamente contra Forge y corre los 3 validadores en cada push/PR,
  subiendo `gripeverde-*.jar` como artifact. Coste: un archivo YAML de ~30
  líneas. KI-52 pasaría de "imposible aquí" a "automático".
- **D2. Task `verifyGripeVerdeMusicDurations`.**
  Mover la verificación que hice a mano (parser Vorbis granule/rate) a una
  tarea Gradle en Groovy (~40 líneas): falla el build si un OGG de música
  se desvía de `TRACK_DURATIONS_MS` más de ±1 s. Protege el crossfade la
  próxima vez que alguien reemplace una pista.

---

## E. TESTS Y CALIDAD CONTINUA

- **E1.** Suite de tests sin Minecraft (JUnit 5 solo con Gson en
  `testImplementation`): `HiddenCipher`, el `weightedRandom` de
  `MenuAmbience` (distribución con pesos), `ChroniclerMemory.sanitize` y
  la cadena de descubrimientos de `Discoveries`. Hoy cero test automatizados.
- **E2.** Spotless (ratchetFrom o reglas fijas) para hacer cumplir
  `.editorconfig` (UTF-8, LF, sin trailing whitespace) en `check`.
- **E3.** Si se desea, un GameTest real que instancie el mod y valide que
  `ModSounds` registra los 35 eventos — justificaría el namespace que hoy
  está vacío (C1).

---

## F. COMPATIBILIDAD CON OTROS MODS

- **F1.** `onScreenOpening` reemplaza por `instanceof TitleScreen`, lo que
  barre cualquier Title custom de otro mod (FancyMenu y derivadas son
  subclases). Cambiar a `newScreen.getClass() == TitleScreen.class` (y lo
  mismo para `PauseScreen`): si otro mod ya personalizó el menú, Gripe
  Verde cede en lugar de pelearse por la pantalla (comportamiento
  determinista y conficto cero).
- **F2.** Clave de config `client.overrides.replaceTitle` /
  `replacePause` (default true) para que packmakers/usuarios desactiven el
  reemplazo sin desinstalar el mod (mantiene audio/memoria).

---

## G. ACCESIBILIDAD Y PULIDO DE EXPERIENCIA

- **G1. Toggle "Reducir destellos" (fotosensibilidad), separado de
  Movimiento reducido.** El relámpago (`LivingAtmosphere.renderFlash`,
  disparado por el trueno) solo se congela con REDUCED_MOTION completo;
  WCAG 2.3.1 recomienda un control específico de flashes. Un segundo toggle
  en Accesibilidad (`visual.reduceFlashes`, default true si reducedMotion).
- **G2. Subtítulos de sonido reales.** Los 35 eventos llevan
  `"subtitle": ""`. Rellenarlos (35 claves x 25 idiomas, siguiendo el
  pipeline ya validado de lang) hace que con subtítulos de Minecraft
  activos se lea "Campanada lejana", "Trueno", "Pasos". Accesibilidad
  auditiva real, no cosmética.
- **G3.** Narración de % en los sliders propios (`DiaryMusicSlider` /
  `DiaryAmbienceSlider`) al arrastrar, en la línea del trabajo ya hecho
  con los toggles en v0.22.0.

---

## H. RENDIMIENTO / ROBUSTEZ RUNTIME

- **H1. Sacar la escritura de memoria del hilo de render.**
  `ChroniclerMemory.writeNow()` hace write + `force(true)` (fsync) en el
  hilo del cliente cada ≥2 s cuando hay interacción (cada click que hace
  `bump()`). Micro-tirones posibles en discos lentos. Fix: executor
  monohilo serializado (`Executors.newSingleThreadExecutor`) con cola de
  "última escritura gana"; `flushForShutdown()` espera
  `awaitTermination`. ~30 líneas, misma garantía de durabilidad.
- **H2.** Temporizadores con `System.currentTimeMillis()` →
  `System.nanoTime()` (monotónico) en crossfade/ducking/cooldowns; evita
  artefactos si el SO ajusta el reloj (NTP, suspensión del equipo).
- **H3.** Normalización de OGG (44.1 kHz uniforme, ~96-112 kbps en música /
  64-80 en SFX): -0.5-1 MB de jar y menos remuestreo del motor de audio.

---

## I. MANTENIBILIDAD DE CONTENIDO (25 idiomas)

- **I1.** Los 25 lang y los 25 changelogs son casi clones: generar variantes
  desde 2 bases (`en`/`es`) + overrides por región con una task
  `generateGripeVerdeLocales` (los ficheros generados pueden seguir
  versionados). Los validadores existentes ya garantizan paridad, así que
  el riesgo es bajo y el ahorro de trabajo por release es grande (hoy un
  entry nuevo = 25 ediciones manuales).
- **I2.** Deriva documental leve detectada: `CONTEXTO.md` y `CONTRIBUTING.md`
  aún nombran como rama fija `arena/01a02cee-gripeverde` y "PR 14 abierta",
  pero la PR 14 ya está mergeada (HEAD = `4ca038d`) y la sesión actual
  trabaja en `arena/01a02f45-gripeverde`. Actualizar el addendum.

---

## PLAN SUGERIDO (lotes implementables)

| Lote | Contenido | Riesgo | Validación |
|---|---|---|---|
| **L1 "Seguridad one-sided"** | A1 (quitar registro manual) + C2 (displayTest/issueURL) + A5 (config para Ctrl+0) | Bajo, 3 archivos | Compila en CI; servidor dedicado arranca sin crash |
| **L2 "Audio blindado"** | A2 (silenciar vanilla music), A3 (watchdog), cierra solapes y F3+T | Bajo | Cliente real: Title 2 min, F3+T, música continúa |
| **L3 "Build/CI"** | D1 CI + D2 durations + C3 version sync + A4 limpieza mixins + C1 | Cero impacto runtime | CI verde en la propia PR |
| **L4 "Accesibilidad"** | G1 flashes + G2 subtítulos + G3 narración sliders | Medio (toca 25 lang) | Validadores lang existentes |
| **L5 "Robustez"** | H1 escritura async + H2 nanoTime + F1/F2 compat | Medio | Tests E1 + cliente real |

Cada lote es un commit independiente respetando R-CIERRE (sync de docs al
bump). A5, F1/F2 y G2 alteran superficie visible → según las reglas del
proyecto convendría confirmación del owner antes de implementar.

---

## PROPUESTAS FUTURAS (descubiertas durante la implementación — PENDIENTES de autorización del owner, ordenadas por valor/esfuerzo)

1. ~~**Censo de eventos de sonido: 10 de 35 registrados nunca se usan.**~~
   **IMPLEMENTADA v0.23.1** (opción conservadora: allowlist grandfathered +
   sello `verifyGripeVerdeSoundUsage` + censo documentado en la javadoc de
   ModSounds). La decisión visible pendiente — conectar esos 10 sonidos a
   momentos de UI o retirarlos — se mueve a PROPUESTAS VISIBLES (ítem V2).
   `UI_BUTTON_CLEAN, UI_OPEN, UI_CLOSE, UI_MODAL, UI_ERROR, UI_TAB,
   UI_SUCCESS, UI_PAPER_FALL, UI_CLICK_CREATE, UI_RESPAWN` están en el
   registro pero ninguna clase los referencia (sus OGG viven por otros
   eventos, así que el sello de huérfanos pasa igual). Dos opciones: (a)
   conectarlos a momentos UI que hoy comparten sonido (error, crear/eliminar
   mundo, pestañas), o (b) desregistrarlos y limpiar `sounds.json`. Medir:
   sello nuevo `verifyGripeVerdeSoundUsage` que falle si un evento
   registrado no aparece en el código.
2. ~~**Sello `verifyGripeVerdeSubtitles`.**~~ **IMPLEMENTADA v0.23.1**. Hacer permanente la comprobación
   que corrí en Python: toda clave `subtitle` no vacía de `sounds.json` debe
   existir en los 25 lang. ~25 líneas de Groovy; impide regresiones al
   tocar sonidos.
3. **Resaltado de "crónica nueva" en el Changelog in-game.** Persistir
   `lastSeenVersion` en la memoria del cronista y al abrir el historial
   desplazarse a la entrada de la versión instalada con un sello "Nuevo"
   si el jugador actualizó desde una versión anterior. UX + diegesis.
4. ~~**Migración versionada + sanitize de bounds de la memoria.**~~
   **IMPLEMENTADA v0.23.1** (`SCHEMA_VERSION`, `migrate()` con pasos N→N+1
   documentados, clamps y caps; esquema futuro se conserva con aviso). El campo
   `Data.version = 1` ya existe pero no hay pipeline de migración; añadir
   `migrate(Data)` documentada y clamps (visitCount/streak/counters
   negativos → 0) por si el JSON se edita a mano o se corrompe
   lógicamente (válido pero absurdo).
5. **Cache de pergamino como textura.** `PaperTexture` se dibuja
   procedural por cuadro con RNG determinista; cachear cada (kind,w,h) a
   una `DynamicTexture` al hacer resize y solo blittear → ahorro de CPU
   por cuadro en todas las pantallas Diary. Requiere validar paridad
   visual pixel-por-pixel en las 4 escalas GUI.
6. ~~**MenuAmbience a reloj monotónico.**~~ **IMPLEMENTADA v0.23.1**
   (migración completa incluyendo `lastBellMs` y su único consumidor,
   `LivingAtmosphere`: hearse/cuervos/campanas ahora en nanoTime). Quedó fuera de la migración a
   `nanoTime` porque sus APIs públicas (`lastBellMs`,
   `isCrowsRecentlyTriggered`, `isWhisperOpen`) se comparan con
   `currentTimeMillis` en pantallas; migrar API y call sites juntos.
7. **Normalización de OGG (H3).** La pista 4 va a 48 kHz y el tick a
   22 050 Hz; uniformar a 44.1 kHz y bitrates coherentes (música
   ~96-112 kbps, SFX ~64-80) con ffmpeg en el PC del owner: ~1 MB menos
   de jar y sin remuestreo en runtime. No automatizable sin el binario.
8. **Tests unitarios reales (E1) ahora que hay CI.** Extraer lógica pura
   (`HiddenCipher`, `weightedRandom`, sanitize/migrate de memoria,
   cadenas de `Discoveries`) a clases sin dependencias Minecraft y
   correrlas con JUnit 5 en `check`; la CI las ejecutaría gratis.
9. **Generación de locales (I1).** Los 25 lang y los 25 changelogs son
   clones regionales: bases `en`/`es` + overrides por país generadas con
   `generateGripeVerdeLocales` (salida versionada; las paridades las
   sigue vigilando el sello existente). Un entry nuevo pasaría de 25
   ediciones manuales a 1-2.
10. **Job de release-candidate en CI (manual).** `workflow_dispatch` que
    suba el jar a una Release *draft* sin publicarla, respetando la regla
    "tags solo tras validación del owner".

## PROPUESTAS VISIBLES / EFECTOS (presentadas al owner el 2026-08-23 — NO implementar sin autorización)

> **ADDENDUM 2026-08-23 (v0.24.0 - El viento vuelve a soplar):** autorizadas
> e implementadas **V1, V3 y V5** junto con tres easter eggs nuevos
> (`storm_night`, `seal_breaker`, `sound_keeper`; el archivo pasa a 50 fichas)
> y la reparación del ambiente mudo — era contenido, no código: el loop de
> viento medía -64 dB RMS y la lluvia -32 dB; regenerados por síntesis
> procedural con `tools/audio/regenerate_menu_ambience.py`. V2 queda
> parcialmente cubierta (2 de 10: `ui.paper_fall` y `ui.success` conectados;
> restan 8 en el allowlist). **Pendientes: V2 (resto), V4, V6, V7, V8.**
> Diagnóstico completo del silencio en KNOWN_ISSUES (sección v0.24.0).
>
> **ADDENDUM 2026-08-23 (v0.24.1 - La errata del cronista):** hotfix de
> build — la semilla de las gotas (V5) se escribió con una P en un literal
> hexadecimal (`0xD20PL`) y la 0.24.0 nunca compiló fuera del sandbox; lo
> detectó el owner en el primer `deploy.ps1`. Errata corregida
> (`0xCA1DAL`) y el espejo Python suma su octavo sello: lint de literales
> numéricos Java sobre los 60 archivos. Contenido de 0.24.0 intacto;
> para distribuir (incluido Agus): usar **0.24.1**.
>
> **ADDENDUM 2026-08-24 (REPOSO — plan anotado por el owner):** el proyecto
> queda en pausa en la v0.30.0. Próxima iteración autorizada de palabra:
> **(a)** pluma de la crónica coherente — rasguño solo si la fila que se
> escribe está a la vista y la tinta ya no corre por zonas fuera de
> pantalla; **(b)** rediseño **cuaderno doble faz**: dos hojas abiertas,
> 1 versión por hoja, animación de hoja doblando (render Java puro), con
> lupa/folio/lacre/huevos adaptados (validar wrap ~170 px en GUI 4;
> fallback a hoja simple si no entra; reducedMotion = giro instantáneo);
> **(c)** segunda bajada de volumen de botones (clicks −30 % aprox).
> Detalle completo en CONTEXTO (decimocuarta vuelta). No empezar (b) sin
> visto bueno al retomar.
>
> **ADDENDUM 2026-08-24 (v0.30.0 - La hoja entera):** feedback del
> owner — la pluma ahora escribe la bitácora entera al ritmo del lector
> (regla de piso: sin huecos al bajar de golpe), la lupa se arregló
> (papel claro, letra tinta — era tinta sobre tinta), hovers −40 % y
> cuatro solapes de texto corregidos (firma/dayMsg del título, línea fix
> de Holloway en el gabinete, flavor de sonido, elipsis al ancho).
>
> **ADDENDUM 2026-08-24 (v0.29.0 - La vuelta y la lupa):** feedback del
> owner — la transición de regreso ceremonial no gustaba. Rediseño:
> vuelta cortés (170 ms, banda superior que se repliega; la ceremonia
> completa queda solo para avanzar) + optimización del velo (cache por
> clase, fast path costo cero) + retirado el golpe sordo al tocar
> (`silenceKnock` fuera, a pedido expreso). La crónica gana lupa
> (búsqueda por palabra con recorrido Enter/Shift+Enter).
>
> **ADDENDUM 2026-08-24 (v0.28.0 - Las gotas y los folios):** el pliegue
> gana peso físico — gotas que resbalan del filo de tinta con rastro fino
> — y la crónica gana folio al pie (hoja actual / total, viva con el
> scroll) y lacre en TODAS las entradas saltadas cuando se actualiza más
> de una edición. Subtítulos del tablón ampliados (9 → 12 por pool).
> Pendiente única: V7 (bloqueada por identidad render-Java-puro).
>
> **ADDENDUM 2026-08-24 (v0.27.0 - El pliegue y la pluma):** **V4
> implementada** (la crónica escribe a pluma la entrada recién llegada,
> solo en su primera visita; click la termina; movimiento reducido la
> entrega entera) y la transición de 0.26.0 madura: la cortina tiene
> sentido (baja al avanzar, sube al regresar, según pila de mesas), el
> filo salpica gotas diminutas y la hoja nueva se acuesta con una sombra
> breve. Con V1/V2/V3/V4/V5/V6 cerradas, **pendiente solo V7** (bloqueada
> por la identidad render-Java-puro: un logo.png sería textura; la
> alternativa es tipografía procedural, a evaluar). Backup bundle previo
> registrado.
>
> **ADDENDUM 2026-08-24 (v0.26.0 - Las hojas y los faroles):** transición
> entre interfaces (cortina de tinta al abrir cualquier pantalla del
> gabinete; fundido breve con movimiento reducido), **V6 implementada**
> (faroles reactivos al cursor) y **V2 completada** — los 8 sonidos legacy
> cableados a sus eventos (cada mesa suena con su toque; el escritorio en
> silencio responde con golpe sordo; el gramófono anuncia pieza nueva;
> volver a la vigilia vuelve a sentarte; mapeo adaptado: `ui.tab` cubre
> crónica/idiomas, `ui.modal` la sala de sonido, `ui.error` dobla como
> golpe del escritorio, `ui.click_create` es el gramófono y `ui.respawn`
> el regreso a la vigilia). Allowlist del sello vacía: 35/35 en uso.
> Crónicas reescritas con vocabulario de documento real. Backup bundle
> previo registrado. **Pendientes: V4, V7, V8.**
>
> **ADDENDUM 2026-08-23 (v0.25.2 - El telegrama no pisa la mesa):** hotfix
> de layout en GUI scale 3-4 — el cartel del easter egg caía sobre los
> botones (clamps de Y que se anulaban) y el sello FRAUDE se montaba sobre
> el texto del cartel. Ambos re-acotados; además queda documentado el
> deploy inline como vigente y la regla permanente del bloque PowerShell.
>
> **ADDENDUM 2026-08-23 (v0.25.1 - El cilindro original):** la lluvia
> sintetizada de 0.24.0 sonaba "bugeada" en cliente (owner). Se retira la
> síntesis: lluvia y viento vuelven a los masters originales amplificados
> (ganancia pura, cresta intacta; mezcla lluvia 0.30→0.45). El script de
> regeneración queda archivado. Lección: restaurar y amplificar antes que
> regrabar contenido que el owner reconoce.
>
> **ADDENDUM 2026-08-23 (v0.25.0 - El que mira la tormenta):** tres easter
> eggs nuevos autorizados por el owner — `storm_watcher` (quedarse tres
> relámpagos frente a la vigilia en noche de tormenta; contador de destellos
> en `LivingAtmosphere`), `drop_catcher` (la lluvia es tocable:
> `rainDropAt`/`scareDrop`, botones excluidos) y `code_doctor` (palabra
> DOCTOR en el telégrafo oculto). Archivo: 52 fichas; notas N_FLASH3,
> N_DROP, N_DOCTOR; cero claves lang nuevas. Incluye todo lo de 0.24.x.
> **Pendientes: V2 (resto), V4, V6, V7, V8.**

Cosas que SE NOTAN en pantalla u oído. Cada una respeta las reglas madre
(sin gameplay, sin resourcepacks, render Java puro).

- **V1. "Crónica nueva" en el Changelog** (visible): al abrir el historial,
  salto automático a la entrada de la versión instalada con sello "Nuevo" si
  el jugador actualizó desde otra versión (persiste `lastSeenVersion` en la
  memoria). UX real, esfuerzo medio.
- **V2. Conectar los 10 sonidos legacy a momentos de UI** (audible): propuesta
  de mapeo — `ui.error` para formularios inválidos (DirectJoin/EditServer),
  `ui.click_create`/`ui.click_destroy` para crear/borrar mundos o servidores,
  `ui.tab` para Archive/Changelog, `ui.paper_fall` al cerrar pantallas largas,
  `ui.open`/`ui.close` para Multiplayer y Packs. Cierra el censo retirando el
  allowlist del sello.
- **V3. "Noche de tormenta"** (visible + audible + lore): algunas sesiones
  (baja probabilidad, semilla del día real) la vigilia arranca con tormenta
  intensa — lluvia al 150 %, truenos más frecuentes, relámpago con silueta
  recortada — y descubrimiento propio #47 con ficha en la Sala de consulta.
  Respeta Destellos reducidos/Movimiento reducido de v0.23.0.
- **V4. Escritura de pluma en Changelog** (visible, diegético): las entradas
  "se escriben" letra a letra con leve grano de tinta la primera vez que se
  abren por sesión (solo al scroll, respetando Movimiento reducido), con el
  scratch de `ui.scroll` muy bajo.
- **V5. Gotas de lluvia en el pergamino** (visible): 2-3 gotas que resbalan
  por los bordes de la hoja desde el borde superior en pantallas Diary, con
  estela de tinta diluida. Barato (partículas capadas) y muy atmosférico.
- **V6. Faroles reactivos al cursor** (visible): acercar el mouse a un farol
  inclina la llama lejos del cursor y dispersa las polillas; sin coste de
  gameplay, reutiliza el sistema hover existente.
- **V7. Logo del mod (logo.png)** (visible): sello EC-QV/1897 en la ficha de
  la lista de Mods (`logoFile` en mods.toml). Necesita arte aprobado.
- **V8. Transición de tinta entre pantallas** (visible): wipe de tinta breve
  (300 ms) al pasar Title → Options o abrir el Archive, imitando una pluma
  que cruza la hoja. Efecto de firma para la identidad del mod.

Mi recomendación dentro de este lote: **V1 + V5 + V3** (la más barata de
alto impacto, la atmosférica pura y la que da ficha nueva de
descubrimiento). Ninguna se tocará hasta que el owner elija.

## Referencias externas usadas en la búsqueda

- Forge Community Wiki, "Sides": registro one-sided con
  `@Mod.EventBusSubscriber(Dist.CLIENT)` / `DistExecutor` y por qué el
  registro manual en el constructor crashea servidores dedicados;
  `displayTest="IGNORE_ALL_VERSION"` para mods sin componente servidor.
  <https://forge.gemwire.uk/wiki/Sides>
- FancyMenu Docs, "Menu Background Music": los mods de menú necesitan
  control explícito sobre la música vanilla (sistema separado
  `MusicManager`, delay 20-600 ticks para `Musics.MENU`).
  <https://docs.fancymenu.net/docs/en-US/background-music>
- The Betweenlands #324: caso real de música vanilla solapándose con música
  custom de menú ("it ruins the ... ogg sounds that plays over").
  <https://github.com/Angry-Pixel/The-Betweenlands/issues/324>
- Parseo propio de OGG Vorbis (cabecera de identificación + granule de la
  última página) para verificar `TRACK_DURATIONS_MS` contra los 4 OGG y
  medir las 42 pistas (sin `ffprobe`: lector manual en Python).
