# CONTEXTO COMPLETO — Eternal Craft: Gripe Verde

> Documento maestro autocontenido. Pensado para entregarle a otra IA (o a un
> nuevo desarrollador) todo lo necesario para entender el proyecto y retomarlo
> sin repetir errores. Estado verificado al momento de escribir: **v0.17.2**.

---

## 1. Qué es el proyecto

**Eternal Craft: Gripe Verde** es un **mod de interfaz y audio client-side para
Minecraft Forge 1.20.1**. No agrega gameplay (aún): convierte todos los menús
del juego en un **gabinete victoriano bajo cuarentena** con ambientación de
epidemia *Green Flu* (Green Flu). El menu principal muestra una ciudad oscura
con lluvia, niebla, luna, skyline victoriano, reloj de bolsillo animado,
carteles oficiales, música y ambiente sonoro propios. El gabinete de opciones
organiza las secciones como registros del cronista. Los contenedores del juego
(inventario, hornos, yunques, etc.) llevan un resource pack built-in de acero.

- **Versión actual:** `0.17.2` — *"Las bandejas de la mesa y la moneda del trato"*
- **Namespace / mod id:** `com.eternalcraft.gripeverde` / `gripeverde`
- **Autor:** Santipdr (usuario GitHub: Santi-PdR)
- **Repositorio:** `https://github.com/Santi-PdR/GripeVerde` (privado)
- **Rama de trabajo de esta sesión:** `arena/01a025ab-gripeverde` (Arena.ai)
- **Discord del proyecto:** `https://discord.gg/qrBCgYuwWb`

---

## 2. Stack técnico y build

| Componente | Valor |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.20 (loaderVersion `[47,)`) |
| Java | 17 (toolchain) |
| Gradle | 8.8 (ForgeGradle 6.0.16+, plugin Mixin 0.7+) |
| Mappings | `official` 1.20.1 |
| Mixin | 0.8.5 (config `gripeverde.mixins.json`, compatibilityLevel JAVA_8) |

**Build:**
```
./gradlew build   →  build/libs/gripeverde-0.17.2.jar
```
- `processResources` depende de 3 tasks de verificación:
  `verifyGripeVerdeLocalization` (25 lang + 25 changelog), `verifyGripeVerdeSoundAssets`
  (sounds.json ↔ OGG), `verifyGripeVerdeThemeAssets` (pack `gripeverde_steel`:
  20 contenedores + 4 creativos + sprites + pack.png 128x128 + pack_format 15
  + ausencia de huerfanas dropper/villager2/tabs.png).
- Deploy: ruta configurable en archivo `.deploy` (clave `DEPLOY_MODS_PATH`).
  Regla: build SIEMPRE acompañado de deploy; limpiar `gripeverde-*.jar` viejos antes.

⚠️ **Restricción de entorno (importante):** El sandbox de desarrollo no tiene
JDK ni acceso al maven de Forge, así que el build y la validación visual se
hacen en la máquina del usuario. Cualquier código nuevo debe limitarse a APIs
ya verificadas en el repo (regla E125).

---

## 3. Estructura del repositorio

```
GripeVerde/
├── build.gradle, settings.gradle, gradle.properties, gradlew
├── .deploy                          # ruta de deploy (DEPLOY_MODS_PATH=...)
├── README.md                        # portada pública (v0.17.2)
├── CONTEXTO.md                      # contexto maestro de sesiones (v0.17.2, actualizado)
├── CHANGELOG.md                     # historial narrativo v0.1.0 → v0.17.2
├── KNOWN_ISSUES.md                  # estado técnico, KIs, reglas, errores (actualizado)
├── AUDITORIA_PROFESIONAL.md         # ⚠️ HISTÓRICO v0.14.0 (ver banner interno)
├── MICRODETALLES_EXTREMO.md         # ⚠️ HISTÓRICO v0.14.0 (ver banner interno)
├── VALIDACION_FASE4.md              # ⚠️ HISTÓRICO v0.14.1 (ver banner interno)
├── sound-tests/                     # candidatos de sonido de botón (1 aceptado)
└── src/
    ├── main/java/com/eternalcraft/gripeverde/   # 83 clases Java
    └── main/resources/
        ├── META-INF/mods.toml
        ├── pack.mcmeta
        ├── gripeverde.mixins.json    # config mixin (client:[] vacío, sin mixins activos)
        ├── assets/gripeverde/
        │   ├── lang/*.json           # 25 idiomas (5 en_*, 20 es_*)
        │   ├── changelog/*.md        # 25 crónicas in-game
        │   ├── sounds.json           # 34 eventos
        │   └── sounds/               # 42 OGG (music/ui/menu)
        └── resourcepacks/gripeverde_steel/   # PACK BUILT-IN REQUIRED
            ├── pack.mcmeta (format 15), pack.png (128x128)
            └── assets/minecraft/textures/gui/
                ├── container/        # 20 contenedores + creative_inventory/
                └── sprites/          # container/* + widget/* + recipe_book/*
```

### Conteos
- **83 clases Java**
- **25 archivos de idioma** (`lang/*.json`)
- **25 crónicas in-game** (`changelog/*.md`)
- **42 sonidos OGG** + `sounds.json` con **34 eventos**
- **138 texturas PNG**

---

## 4. Arquitectura del código

Paquete raíz: `com.eternalcraft.gripeverde`. Punto de entrada: `GripeVerdeMod`
(`@Mod("gripeverde")`, registra sonidos, config cliente, ScreenEventHandler,
y —vía `@Mod.EventBusSubscriber` en `ResourcePackHandler`— el resource pack).

### `client/audio/` — Sistema de audio
- **`ModSounds`**: DeferredRegister de SoundEvents (34 eventos).
- **`MenuMusic`**: 4 pistas con shuffle, crossfade 4s, ducking 35%/2.5s al
  dispararse RARE, volumen persistente, idempotente, auto-stop al entrar a mundo.
- **`FadingMusicInstance`**: AbstractSoundInstance tickable con volumen dinámico.
- **`MenuAmbience`**: ambiente por capas (3 loops: wind/rain/city + 4 eventos
  ocasionales + 6 raros: bell/thunder/crows/chain/whispers/cough + tick reloj).
  Llama `MenuMusic.duckFor()` en RARE.
- **`LoopingAmbienceInstance`**: loop con volumen dinámico continuo (sin rebuild).
- **`UiSounds`**: familias acústicas `button_main` (brass latch) /
  `button_section` (modal) / `back` (parchment) + hover/scroll. `safePlay()`
  defensivo (workaround ModernFix RegistryObjectMixin, KI-04).

### `client/render/` — Renderizado de la escena
- **`QuarantineSceneRenderer`**: pipeline unificado (cielo/skyline/calle/niebla/
  viñeta/grain). Usado por Title y pantallas Diary (2 modos).
- **`AtmosphereRenderer`**: cielo, luna con 8 fases (O(r) scanline), estrellas,
  halo lunar, estrellas fugaces, factory glow.
- **`VictorianSkyline`**: edificios BG/FG con LOD, ventanas animadas, chimeneas, humo.
- **`LivingAtmosphere`**: niebla que respira, calle, faroles (halos 2 fills),
  polillas, salpicaduras, ondas en charcos, papeles al viento, rata, relámpagos.
- **`PaperTexture`**: pergamino v2.0 (stains/fibers/wax seal/watermark/tear edges).
- **`DiaryChrome`**: helpers cosméticos compartidos (esquineros de acero con
  remache, divisores con rombo, sombra de panel, scrollbars estilizadas,
  chapa de registro `drawRegistryTag` REG. I..XV).
- **`LogoRenderer`**: logo skull con pulso, pool de frases del cronista.
- **`PocketWatchRenderer`**: reloj de bolsillo con manecillas, tic-tac, glitch.
- **`PosterRenderer`**: carteles Quarantine / Wanted / Royal Decree + sellos sangre/tinta.
- **`FlagRenderer`**: ~90 banderas pixel-art 16x11 para el selector de idiomas.
- Otros: `VictorianFrame`, `OrnamentRenderer`, `CandleLight`, `InkEffects`,
  `DiaryDecorator`, `DiaryTheme`, `TextureAssets`.

### `client/screens/` — Pantallas (19 Diary + 2 internas)
- **`AbstractDiaryScreen`**: base responsive (panel clamp, header `- subtitle -` + flavor).
- **`DiaryTitleScreen`**: menu principal (escena viva + música + ambiente + easter eggs + telegrafo oculto).
- **`DiaryPauseScreen`**: diario del superviviente (vela apagable, cuervo).
- **`DiaryOptionsScreen`**: gabinete central (grid 2x5, cajón con llave, slider FOV).
- **`DiarySoundOptionsScreen`**: cámara de música (slider Musica + Ambiente del Cronista).
- **`DiaryLanguageScreen`**: lenguas conocidas (banderas, lengua de la niebla).
- **`DiaryChat/Accessibility/Skin/Controls/Credits/OnlineOptions`**: subpantallas sobre pergamino.
- **`DiaryMultiplayer/EditServer/DirectJoin/Confirm`**: flujo multijugador completo (server pinneado "Ghoul Outbreak Official").
- **`DiaryPackSelectionScreen`**: archivador de materiales (envuelve Resource Packs vanilla).
- **`DiaryArchiveScreen`**: la Sala de consulta (43 fichas de descubrimientos).
- **`ChangelogScreen`**: historial narrativo sobre pergamino (capitulares, divisores con rombo).
- **`DebugCanvasScreen`**, **`SoundLabScreen`**: herramientas internas de dev.

### `client/widgets/` — Widgets propios
`DiaryButton`, `DiarySlider`, `DiaryMusicSlider`, `DiaryAmbienceSlider`,
`DiaryTextField` (extiende EditBox), `DiaryToggle` (envuelve OptionInstance<Boolean>),
`DiaryProgressBar`, `DiarySeparator`, `GreenFluButton`, `SlimButton`,
`IconButton`, `SilentButton`.

### `client/theme/` — Framework UI adaptativo
`AbstractScreenTheme`, `ScreenTheme`, `ThemeManager` (inerte, sin themes registrados),
`ScreenLayout`, `LayoutRegion`, `Anchor`, `Spacing`, `UIScale`, `UIComponent`,
`GhoulPalette`, `components/` (Background/Decoration/Frame/Separator/Title Renderer).

### `client/lore/` — Sistema de memoria y descubrimiento (v0.16.0)
- **`ChroniclerMemory`**: memoria persistente JSON en
  `config/gripeverde-memory.json` (descubrimientos, objetos, noches seguidas,
  ausencias, contadores, libro de visitas). Tolerante a corrupción.
- **`Discoveries`**: 43 descubrimientos con fichas, pistas ES/EN, sello toast.
- **`Marginalia`**: cola persistente de 19 notas contextuales del cronista.
- **`HiddenCipher`**: telegrafo oculto del Title (códigos 1897, 1152, HOLLOWAY, PEREIRA).
- **`Herald`**: periódico diario procedural (semilla por día real).

### `client/` (raíz) y otros
- **`ResourcePackHandler`**: registra el pack `gripeverde_steel` via
  `AddPackFindersEvent` con `required=true` (siempre activo).
- **`ScreenEventHandler`**: eventos de pantalla (Forge EVENT_BUS).
- **`TooltipEventHandler`**: recolorea tooltips a pergamino via `RenderTooltipEvent.Color`.
- **`config/ClientConfig`**: ForgeConfigSpec → `config/gripeverde-client.toml`
  (MUSIC_VOLUME, AMBIENCE_VOLUME, AMBIENCE_ENABLED, OPEN_COUNT).

---

## 5. Sistemas clave activos

1. **Title victoriano vivo:** cielo nocturno, luna de 8 fases, lluvia, niebla,
   skyline, calle mojada, reloj de bolsillo (detenido en 11:52), faroles,
   posters, ventana del piso 13, gato en tejado, rata, veleta, kiosco/Heraldo,
   coche del sepulturero, telegrafo oculto, easter egg doble-click en logo.
2. **Audio del menú:** 4 pistas (shuffle + crossfade 4s + ducking) + ambiente
   por capas (viento/lluvia/ciudad + eventos + raros). Volúmenes persistidos.
3. **Gabinete de opciones:** subpantallas sobre pergamino con la ciudad viva
   atenuada de fondo (Title vivo detras). Música y ambiente sobreviven a la
   navegación (tick en cada pantalla).
4. **Resource pack `gripeverde_steel` (REQUIRED):** 20 contenedores + 4
   creativos + slot universal 26px + sprites de oficio + widgets vanilla en
   acero de cuarentena. Geometría 1:1 vanilla (alpha + slots exactos). **Pura
   presentación: sin cambios de comportamiento ni geometría.**
5. **Memoria del gabinete (v0.16.0):** 43 descubrimientos → fichas → toast →
   memoria. 7 consecuencias permanentes por contador. Marginalia contextual.
   Sala de consulta (`DiaryArchiveScreen`) detrás del último folio del changelog.
6. **Localización:** 25 variantes (5 en_*, 20 es_*), narrativa diegetica.

---

## 6. Historial de versiones (condensado)

| Versión | Hito |
|---|---|
| v0.1.0–v0.6.x | Base: city bg, vela/pergamino, reloj de bolsillo (11:52). |
| v0.7.x | Calle adaptativa, faroles (ocultos luego reactivados). |
| v0.8.x | Sistema de música (4 pistas, crossfade, ducking, slider, persistencia). |
| v0.9.x | Profundidad del skyline, halo lunar, fix humo/nubes (KI-05). |
| v0.10.x | ChangelogScreen victoriano, persistencia del volumen. |
| v0.11.0–0.11.14 | **Ciclo del gabinete pergamino:** Options, Language, Chat, Accessibility, Skin, Sound, Controls, Credits, Multiplayer, banderas pixel-art, tooltips estilizados. |
| v0.12.x | Detalles vivos del gabinete (luna 8 fases, estrellas fugaces, widgets nuevos, easter eggs, paper envejece). Hotfixes de audio/performance. |
| v0.13.0–0.13.10 | Resource pack `gripeverde_theme` (pergamino→acero) sobre contenedores. |
| **v0.14.0–0.14.1** | **"Extreme polish" + Fase 4 (AUDITORIA/MICRODETALLES/VALIDACION). ⚠️ PARCIALMENTE REVERTIDO después.** |
| **v0.15.0–0.15.2** | **Retiro total del resource pack** (`gripeverde_theme`) + `ContainerOverlayHandler`. Contenedores 100% vanilla. Slider FOV + fix label. |
| v0.16.0 | **La sala de consulta:** memoria persistente, 43 descubrimientos, consecuencias permanentes, marginalia, telegrafo oculto, Herald. |
| **v0.17.0** | **Acero de cuarentena:** NUEVO pack `gripeverde_steel` (REQUIRED), 20 contenedores + sprites + widgets, geometría 1:1. Esquineros de acero + chapa de registro en el gabinete. `DiaryPackSelectionScreen`. |
| v0.17.1 | Remaches bien puestos (mantenimiento del acero, sin cambios visibles). |
| **v0.17.2** | **Las bandejas de la mesa:** slot 18→26px, oficios (loom/stonecutter/cartography/villager) restaurados al acero, transparencias repasadas, `tabs.png` removido (sprites tab_* 1-7). |

- **Último tag publicado:** `v0.13.10` (no hay tags desde el ciclo v0.13.x).
- **Cambios revertidos históricamente (NO reintroducir sin orden):** parallax
  con mouse, cursor de pluma custom, fade de transición 200ms, carruaje,
  humo verde intermitente, ropa colgada, subtitulo rotativo del logo "GRIPE
  VERDE", FPS indicator, UiSounds.open() al abrir subpantallas.

---

## 7. Issues conocidos (KIs) abiertos

| KI | Descripción | Estado | Prioridad |
|---|---|---|---|
| **KI-04** | Incompatibilidad con ModernFix RegistryObjectMixin | MITIGADO (`safePlay`) | — |
| **KI-12** | 4 pantallas vanilla con fondo *dirt* sin ambientar (MouseSettings, KeyBinds, PackSelection, OnlineOptions). Requieren mixin sobre `Screen.renderBackground` + filtro instanceof. Ruta identificada; infra mixin armada pero sin mixins activos. | ABIERTO | Media-baja |
| **KI-13** | Server pinneado editable/borrable sin bloqueo visual | ABIERTO | Baja |
| **KI-14** | Banda sepia del multiplayer vs favicons brillantes | ABIERTO | Baja |
| **KI-20** | Validación del pack `gripeverde_steel` en cliente real (build + 4 escalas GUI). Sin JDK/maven en sandbox. | ABIERTO | Alta (validación) |
| **KI-21** | Sprites vanilla no confirmados en 1.20.1 — muy avanzado en v0.17.2 | ABIERTO | Baja |

KIs resueltos destacados: KI-01/02/03/05 (Title/escena), KI-06 (Changelog),
KI-07 (subpantallas — parcial, resto en KI-12), KI-08 (banderas), KI-09 (scroll),
KI-10 (tooltips), KI-11 (slider en vivo), KI-15/16 (fades/scrollbar), KI-13/15/16 (containers).

---

## 8. Reglas operativas críticas (lo que NO hay que romper)

### Narrativa
- **R-DIEGETICO:** toda narrativa de cara al usuario (CHANGELOG raíz, los 25
  changelog in-game, README "Versión actual") se escribe en voz del **cronista
  victoriano**. Prohibido mencionar: bloques, píxeles, cuadrados, escalas GUI,
  sliders, opciones, config, versiones, "el observador percibe que…",
  comparaciones antes/después, ajustes técnicos. Vocabulario permitido:
  papel, pergamino, tinta, despacho, cuaderno, registro, folio, renglón, vela.
  Test rápido: si suena a devlog de videojuego, está mal; si suena a entrada
  de diario a la luz de una vela, está bien.

### Cierre de hito
- **R-CIERRE:** al cerrar cualquier hito (bump de versión) es OBLIGATORIO
  actualizar en la MISMA sesión, antes del commit:
  1. `CONTEXTO.md` (versión, tag, addendum, próximo hito)
  2. `CHANGELOG.md` (entrada narrativa bajo R-DIEGETICO)
  3. `KNOWN_ISSUES.md` (metadatos de versión/tag, cerrar KIs, catalogar nuevos)
  4. `README.md` (bloque "Versión actual" narrativo)
  5. Los 25 `changelog/*.md` in-game **SOLO si hay manifestación observable**
     (ver R-CHANGELOG-DISCRECIONAL de abajo)
  Luego: commit + tag + push. **NUNCA cerrar un chat con docs desincronizados.**
- **R-CHANGELOG-DISCRECIONAL:** no toda versión del código requiere entrada en
  los 25 changelog in-game. Si el hito es puramente técnico/administrativo sin
  manifestación observable en la ciudad, se OMITE. El cronista solo narra
  noches significativas.
- **R-CHANGELOG-AGRUPADO:** una serie técnica del mismo tema (ej: ambientar N
  menús) lleva UNA SOLA entrada agrupada in-game, no una por versión.

### Build / Deploy
- **R-BUILD-DEPLOY:** todo `gradlew build` va acompañado de deploy en el mismo
  bloque. **R-JAR2:** limpiar `build/libs/gripeverde-*.jar` ANTES de compilar.
- **R-VER:** confirmar la versión visible en el reloj antes de aprobar.
- **R-GRADLE-VER:** `mod_version` en build.gradle debe usar
  `project.version.toString()` (nunca hardcodear — error histórico E32).

### Resource pack
- **R-STEEL-GEOMETRY:** el pack NUNCA cambia dimensiones, alpha, posiciones de
  slots ni proporciones vanilla. Solo presentación. Verificado por
  `verifyGripeVerdeThemeAssets`.
- **R-PACK-REQUIRED:** el pack de identidad es `required=true` (siempre activo).
- **R-RESOURCEPACK-BUILTIN:** para sobrescribir texturas vanilla desde un mod,
  usar SIEMPRE resource pack built-in via `AddPackFindersEvent`. Poner
  `assets/minecraft/` en el jar NO sobrescribe vanilla.

### Java / encoding
- **R-UTF8-NOBOM:** siempre `new UTF8Encoding(false)` para escribir .java. El
  BOM rompe compilación. Java UTF-8 sin BOM.
- **R-DUP:** verificar que no se redefinan variables en scope superior.
- **R-SRG-RUNTIME:** reflection contra vanilla debe probar nombres official
  (dev) y SRG (prod, `f_XXXXX_`/`m_XXXXX_`) en cascada + fallback siempre.
  El jar `_mapped_official` es solo dev; runtime usa SRG.
- **R-REFLECTION-TYPED:** reflection filtra por tipo esperado, no solo por nombre.

### Desarrollo
- Un sistema por versión. Un cambio visual/audio sensible por bump.
- Validación visual/auditiva en 4 escalas GUI cuando se toque UI/audio.
- No modificar más de un renderer por sesión sin plan escrito.
- Auditar estado real antes de proponer cambios (R-DOC-VERIFY: los comentarios
  que documentan limitaciones envejecen mal; verificar contra bytecode).
- **No agregar gameplay** salvo hito planificado. **No cambiar de branch.**

### Mixins (si se retoman)
- **R-MIXIN-COMPAT:** con Mixin 0.8.5 usar `compatibilityLevel JAVA_8`.
- **R-MODS-TOML-MIXIN:** el bloque `[[mixins]]` en `mods.toml` es obligatorio.
- **R-FORGE-EVENTS-FIRST:** buscar evento Forge nativo antes de escribir mixin.
- **R-MIXIN-INHERITED-METHODS:** `@Inject`/`@Redirect` NO alcanzan métodos
  heredados por defecto (causa del fallo histórico E90/E111 en dirt bypass).
- **R-MIXIN-SELF-CONTAINED:** las clases mixin no importan código del propio mod.

---

## 9. Errores históricos a NO repetir (selección)

- **E32:** `mod_version` hardcodeado → usar `project.version.toString()`.
- **E37:** mojibake acumulado por reeditar archivos con tildes en encoding
  erróneo. Se corrompe geométricamente. Reescribir completo con UTF-8 sin BOM.
- **E90 / E111:** mixin sobre `renderBackground` falló porque el método está
  heredado de `Screen`, no en la subclase. 4 iteraciones ciegas. Ruta correcta:
  mixin sobre `Screen` + filtro instanceof.
- **E125:** sin javac en el entorno, solo usar APIs verificadas en el repo.
- **R-ITER-VISUAL-STRICT:** si tras 3 iteraciones un problema visual/técnico no
  cede, es problema de técnica: revertir y cambiar de estrategia.

---

## 10. Estado actual y próximo hito

### Estado (v0.17.2)
- **Fase de interfaz/identidad CERRADA y pulida:** pergamino + acero de
  cuarentena + lore (memoria/descubrimientos) + audio + atmósfera.
- Working tree: limpio. Repo remoto sincronizado.
- La documentación (CONTEXTO, KNOWN_ISSUES, README, CHANGELOG) está
  sincronizada a v0.17.2 tras un pase de revisión.
- Los 3 docs de auditoría (`AUDITORIA_PROFESIONAL.md`, `MICRODETALLES_EXTREMO.md`,
  `VALIDACION_FASE4.md`) describen v0.14.0 (parcialmente revertido) y están
  marcados como HISTÓRICOS con un banner al inicio. **Leer su banner antes de
  usarlos como referencia de estado actual.**

### Próximo hito planificado
- **Candidato C (v0.18.0+): primer sistema REAL de gameplay** — la Gripe Verde
  en sí misma. La casa está lista para tener contenido adentro. Sub-hitos a
  definir en diseño previo escrito (ANTES de tocar código):
  - Sistema de enfermedad: síntomas, progreso, contagio.
  - Cuarentena: zonas seguras vs infectadas.
  - NPCs: enfermos, sanos, personal médico.
  - Items: máscaras, medicinas, alcohol, agua limpia.
  - Mecánicas ambientales: humo verde, ratas, cadáveres.
  - **Riesgo: ALTO. Requiere diseño previo escrito.**
- **Candidato O (sin urgencia):** KI-12 mixin dirt bypass.
- **Deuda menor:** tagear la RC actual cuando se valide en cliente real
  (no hay tags desde v0.13.10).

---

## 11. Documentación del proyecto (qué leer y en qué orden)

1. **`CONTEXTO.md`** — punto único de entrada. Addendums por versión + reglas +
   historial + plan. **Leer COMPLETO antes de proponer cambios.**
2. **`KNOWN_ISSUES.md`** — estado técnico, KIs abiertos/cerrados, reglas, errores.
3. **`CHANGELOG.md`** — historial narrativo completo (tono cronista).
4. **`README.md`** — portada pública + lista de pantallas + idiomas + requisitos.
5. **`AUDITORIA_PROFESIONAL.md` / `MICRODETALLES_EXTREMO.md` / `VALIDACION_FASE4.md`**
   — ⚠️ referencia histórica del nivel de polish perseguido en v0.14.0. Verificar
   qué sigue vigente vs. qué fue revertido (banner al inicio de cada uno).
6. Los 25 `changelog/*.md` in-game — crónicas diegeticas por idioma.

---

## 12. Notas para otra IA que retome el proyecto

1. **No cambiar de branch** (la sesión va fijada a `arena/01a025ab-gripeverde`).
2. **No agregar gameplay** salvo hito planificado con diseño previo escrito.
3. Validar JSON y referencias de sonidos antes de cada commit.
4. Tras cualquier cambio, entregar el pipeline completo de build/deploy.
5. El sandbox no tiene JDK ni maven de Forge: el build se hace en la máquina
   del usuario. Validar programáticamente con las tasks Gradle de verificación.
6. Antes de escribir nada contra la API vanilla, verificar firmas reales con
   `javap -p` sobre el jar SRG (no fiarse de la memoria ni de comentarios).
7. Los documentos marcados como **HISTÓRICOS** describen un estado revertido:
   no tomarlos como verdad actual sin contrastar con `CONTEXTO.md`.
8. Mantener la coherencia narrativa (R-DIEGETICO) y la sincronización de docs
   (R-CIERRE) al cerrar cualquier hito.
