# CONTEXTO COMPLETO — Eternal Craft: Gripe Verde

> Documento maestro autocontenido. Pensado para entregarle a otra IA (o a un
> nuevo desarrollador) todo lo necesario para entender el proyecto y retomarlo
> sin repetir errores. Estado verificado al momento de escribir: **v0.21.1**.

---

## 1. Qué es el proyecto

**Eternal Craft: Gripe Verde** es un **mod de interfaz y audio client-side para
Minecraft Forge 1.20.1**. No agrega gameplay (aún): convierte todos los menús
del juego en un **gabinete victoriano bajo cuarentena** con ambientación de
epidemia *Green Flu*. El menu principal muestra una ciudad oscura con lluvia,
niebla, luna, skyline victoriano, reloj de bolsillo animado, carteles oficiales,
música y ambiente sonoro propios. El gabinete de opciones organiza las secciones
como registros del cronista. Los contenedores del juego son **100% vanilla** desde
v0.15.0 (y reafirmado en v0.19.0-v0.21.1): no hay resource pack built-in.

- **Versión actual:** `0.21.1` — *"Corrección del registro sonoro + limpieza de proyecto"*
- **Namespace / mod id:** `com.eternalcraft.gripeverde` / `gripeverde`
- **Autor:** Santipdr (GitHub: Santi-PdR)
- **Repositorio:** `https://github.com/Santi-PdR/GripeVerde` (privado)
- **Rama de trabajo de esta sesión:** `arena/01a025bc-gripeverde` (Arena.ai)
- **Discord:** `https://discord.gg/qrBCgYuwWb`

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
./gradlew build   →  build/libs/gripeverde-0.21.1.jar
```
- `processResources` depende de 2 tasks de verificación:
  `verifyGripeVerdeLocalization` (25 lang + 25 changelog),
  `verifyGripeVerdeSoundAssets` (sounds.json ↔ OGG existentes).
- La task `verifyGripeVerdeThemeAssets` (steel pack) fue eliminada en v0.15.0 y no volvió.
- Deploy: ruta configurable en archivo `.deploy` (clave `DEPLOY_MODS_PATH`).
  Regla: build SIEMPRE acompañado de deploy; limpiar `gripeverde-*.jar` viejos antes.

⚠️ **Restricción de entorno:** El sandbox de Arena no tiene JDK ni maven de Forge, así que el build y la validación visual se hacen en la máquina del usuario. Código nuevo debe limitarse a APIs ya verificadas en el repo (regla E125).

---

## 3. Estructura del repositorio (v0.21.1)

```
GripeVerde/
├── build.gradle, settings.gradle, gradle.properties, gradlew
├── .deploy                          # ruta de deploy (DEPLOY_MODS_PATH=...)
├── README.md                        # portada pública (v0.21.1)
├── CONTEXTO.md                      # contexto maestro de sesiones (v0.21.1)
├── CONTEXTO_PARA_IA.md              # este archivo (v0.21.1)
├── CHANGELOG.md                     # historial narrativo v0.10.0 → v0.21.1
├── KNOWN_ISSUES.md                  # estado técnico, KIs, reglas, errores
└── src/
    ├── main/java/com/eternalcraft/gripeverde/   # 68 clases Java (post-limpieza v0.21.1)
    │   ├── GripeVerdeMod.java
    │   ├── client/
    │   │   ├── ScreenEventHandler.java (solo Title/Pause intercept, sin ThemeManager)
    │   │   ├── TooltipEventHandler.java
    │   │   ├── audio/ (ModSounds, MenuMusic, MenuAmbience, FadingMusicInstance, LoopingAmbienceInstance, UiSounds)
    │   │   ├── lore/ (ChroniclerMemory, Discoveries, Marginalia, HiddenCipher, Herald)
    │   │   ├── render/ (QuarantineSceneRenderer, AtmosphereRenderer, VictorianSkyline, LivingAtmosphere, DiaryChrome, ExpedientHeader, etc + TextureAssets solo con ICONs lógicos)
    │   │   ├── screens/ (19 Diary + 2 dev: DebugCanvasScreen, SoundLabScreen)
    │   │   ├── theme/ (solo Anchor, LayoutRegion, Spacing — usados por DiaryTitleScreen)
    │   │   ├── util/ (EasedValue, SmoothScroll)
    │   │   └── widgets/ (DiaryButton, DiarySlider, DiaryMusicSlider, DiaryAmbienceSlider, DiaryTextField, DiaryToggle, DiaryProgressBar, GreenFluButton, SlimButton)
    │   └── config/ClientConfig.java
    └── main/resources/
        ├── META-INF/mods.toml
        ├── pack.mcmeta (format 15)
        ├── gripeverde.mixins.json (client:[] vacío, sin mixins activos)
        └── assets/gripeverde/
            ├── lang/*.json (25 idiomas)
            ├── changelog/*.md (25 crónicas in-game)
            ├── sounds.json (34 eventos)
            └── sounds/ (42 OGG: music/ui/menu)
```

**Eliminados en v0.21.1:**
- `AUDITORIA_PROFESIONAL.md`, `MICRODETALLES_EXTREMO.md`, `VALIDACION_FASE4.md` (históricos v0.14.x, ya revertidos)
- `sound-tests/` (wav fuente ya integrado como ogg)
- `src/main/resources/assets/gripeverde/textures/gui/title/bg_town.png` (2.1M sin uso, Title usa render procedural)
- `client/theme/` parcial: `ThemeManager`, `GhoulPalette`, `ScreenLayout`, `ScreenTheme`, `AbstractScreenTheme`, `UIComponent`, `UIScale`, `components/*` (framework inerte sin themes registrados desde v0.6.0)
- `client/widgets/`: `IconButton`, `SilentButton`, `DiarySeparator` (definidos pero nunca usados)
- `client/ResourcePackHandler.java` y `resourcepacks/gripeverde_steel/` (eliminados desde v0.15.0, reintroducidos temporalmente en v0.17.0-v0.17.2, retirados de nuevo en v0.19.0)

### Conteos actuales
- **68 clases Java** (83 en v0.17.2 → 68 tras limpieza)
- **25 archivos de idioma**
- **25 crónicas in-game**
- **42 sonidos OGG** + `sounds.json` con **34 eventos**
- **0 texturas PNG** de contenedores (vanilla puro)

---

## 4. Arquitectura del código

### `GripeVerdeMod`
`@Mod("gripeverde")`, registra sonidos, config cliente, `ScreenEventHandler`, `TooltipEventHandler`. Versión `0.21.1`.

### `client/audio/`
- `ModSounds`: DeferredRegister de SoundEvents (34 eventos) con fix v0.21.1 para compatibilidad `SoundEvent.createVariableRangeEvent` vs constructor directo en runtime Forge.
- `MenuMusic`: 4 pistas shuffle, crossfade 4s, ducking 35%/2.5s en RARE, volumen persistente.
- `MenuAmbience`: 3 loops (wind/rain/city) + 4 ocasionales + 6 raros (bell/thunder/crows/chain/whispers/cough) + tick reloj. Llama `MenuMusic.duckFor()` en RARE.
- `UiSounds`: familias `button_main` (brass latch) / `button_section` (modal) / `back` (parchment) + hover/scroll. `safePlay()` defensivo.

### `client/render/`
- `QuarantineSceneRenderer`: pipeline unificado (cielo/skyline/calle/niebla/viñeta/grain). Usado por Title y pantallas Diary.
- `AtmosphereRenderer`, `VictorianSkyline`, `LivingAtmosphere`: cielo, luna 8 fases, estrellas, halo, skyline, calle, faroles, niebla, etc.
- `DiaryChrome`: helpers cosméticos (esquineros de acero con remache, divisores, sombra, scrollbars, chapa REG. I..XV).
- `ExpedientHeader`: cabecera reutilizable con expediente, referencia, estado, flavor.
- `PaperTexture`, `DiaryDecorator`, `DiaryTheme`, `PocketWatchRenderer`, `PosterRenderer`, `FlagRenderer` (90 banderas 16x11), etc.
- `TextureAssets`: solo `ICON_BTN_MP` y `ICON_BTN_MODS` como identificadores lógicos para `GreenFluButton` (dibujo procedural, no PNG).

### `client/screens/` (19 Diary + 2 dev)
- `AbstractDiaryScreen`: base responsive (panel clamp, header - subtitle - + flavor).
- `DiaryTitleScreen`: menú principal (escena viva + música + ambiente + easter eggs + telégrafo oculto). Usa `Anchor`, `LayoutRegion`, `Spacing` del paquete `theme` (único uso activo de ese paquete).
- `DiaryPauseScreen`: diario del superviviente (vela apagable, cuervo).
- `DiaryOptionsScreen`: gabinete central (grid 2x5, cajón con llave, slider FOV).
- `DiarySoundOptionsScreen`, `DiaryVideoSettingsScreen`, `DiaryControlsScreen`, `DiaryLanguageScreen`, `DiaryChatOptionsScreen`, `DiaryOnlineOptionsScreen`, `DiaryMultiplayerScreen`, `DiaryArchiveScreen`, `ChangelogScreen`, etc.
- En v0.19.0-v0.21.1: `DiaryChatOptionsScreen` → oficina de correspondencia, `DiaryOnlineOptionsScreen` → estación de comunicaciones, `DiaryArchiveScreen` y `ChangelogScreen` → mejor cierre, `DirectJoin`, `EditServer`, `Confirm`, `Skin` → pulidos sin tocar lógica vanilla.
- `DebugCanvasScreen`, `SoundLabScreen`: herramientas internas de dev (no en producción, accesibles solo si se abren manualmente).

### `client/widgets/`
`DiaryButton` (uso principal), `DiarySlider`, `DiaryMusicSlider`, `DiaryAmbienceSlider`, `DiaryTextField`, `DiaryToggle`, `DiaryProgressBar`, `GreenFluButton`, `SlimButton`.

### `client/theme/` (reducido)
Solo 3 clases útiles conservadas porque `DiaryTitleScreen` las usa:
- `Anchor`, `LayoutRegion`, `Spacing`.
Eliminados: `ThemeManager` (inerte, sin themes), `GhoulPalette`, `ScreenLayout`, `ScreenTheme`, `AbstractScreenTheme`, `UIComponent`, `UIScale`, `components/*`.

### `client/lore/`
- `ChroniclerMemory`: memoria JSON en `config/gripeverde-memory.json`.
- `Discoveries`: 43 descubrimientos con fichas, pistas ES/EN, toast.
- `Marginalia`, `HiddenCipher`, `Herald`.

### `client/` raíz y `config/`
- `ScreenEventHandler`: intercepta `TitleScreen` → `DiaryTitleScreen`, `PauseScreen` → `DiaryPauseScreen`, maneja atajo Ctrl+0 para salir del mundo, y feedback sonoro por familia de pantalla. Ya no delega a ThemeManager.
- `TooltipEventHandler`: recolorea tooltips a pergamino.
- `ClientConfig`: `MUSIC_VOLUME`, `AMBIENCE_VOLUME`, `AMBIENCE_ENABLED`, `OPEN_COUNT`.

---

## 5. Sistemas clave activos

1. **Title victoriano vivo:** cielo nocturno, luna 8 fases, lluvia, niebla, skyline, calle mojada, reloj 11:52, faroles, posters, gato, rata, veleta, Heraldo, coche sepulturero, telégrafo oculto, easter eggs.
2. **Gabinete de opciones:** Options como mesa central, Sound como sala instrumental, Video como aparato de observación, Controls como manual operativo, Language como archivo lingüístico, Chat como oficina de correspondencia, Accessibility como expediente de percepción, Online como estación de comunicaciones, Multiplayer como registro de hospedajes, Credits como acta final, Archive como sala de consulta, DirectJoin/EditServer/Confirm como documentos oficiales.
3. **Audio:** 4 músicas con crossfade y ducking, ambiente por capas, sliders propios del mod con persistencia.
4. **Memoria del gabinete (v0.16.0):** 43 descubrimientos, marginalia contextual, Heraldo procedural, cifrado oculto, consecuencias permanentes.
5. **Contenedores:** 100% vanilla desde v0.15.0 (reafirmado v0.19.0-v0.21.1). Sin resource pack.

---

## 6. Historial de versiones recientes (resumen)

| Versión | Nombre | Nota clave |
|---|---|---|
| **v0.21.1** | Corrección del registro sonoro + limpieza | Hotfix SoundEvent runtime + limpieza de docs históricos, sound-tests, bg_town.png, theme inerte, widgets muertos |
| **v0.21.0** | La oficina aprende a cerrar filas | Cierre de pantallas secundarias débiles (Chat, Online, Archive, Changelog, DirectJoin, EditServer, Confirm, Skin) sin tocar lógica vanilla, sin resourcepacks |
| **v0.20.0** | El gabinete encuentra su propia mano | Identidad de interfaz, consolidación de DiaryChrome, sliders como instrumentos, composición editorial |
| **v0.19.0** | Interface overhaul / visual polish | Pase mayor de interfaces, retiro definitivo de `gripeverde_steel`, design system de cabeceras documentales |
| **v0.17.2** | Las bandejas de la mesa y la moneda del trato | Parche de mantenimiento sobre acero de cuarentena (slot 26px, transparencias, sprites tab_top/tab_bottom) |
| **v0.17.1** | Remaches bien puestos | Parche remaches y cepillado steel |
| **v0.17.0** | El acero vuelve a la cuarentena | Reintroducción temporal de `gripeverde_steel` REQUIRED (20 contenedores + sprites), luego retirada en v0.19.0 |
| **v0.16.0** | La sala de consulta | Memoria persistente, 43 descubrimientos, marginalia, sala de consulta |
| **v0.15.2** | La cifra del campo | Fix label slider FOV muestra número |
| **v0.15.1** | La segunda mano | Slider FOV + eliminación ContainerOverlayHandler |
| **v0.15.0** | El oficio vuelve a su madera | Retiro completo del resource pack, contenedores vanilla |

Ver `CHANGELOG.md` para narrativa completa y `CONTEXTO.md` para addendums por versión.

---

## 7. Reglas operativas vigentes (resumen)

### Build / deploy
- Build siempre acompañado de deploy, limpiar jars viejos antes.
- Validar versión mostrada por el reloj en cliente real.
- Tasks de verificación: localización y sonidos (no theme assets).

### Código / UI
- **R-NO-RESOURCEPACK:** desde v0.19.0, nada de `gripeverde_steel`, nada de `tools/steel_pack`, nada de texturas vanilla modificadas. UI se hace con Java/render/widgets.
- **R-VANILLA-LOGIC:** no tocar lógica de slots/inventarios/opciones vanilla, solo presentación.
- **R-SIMPLIFY-BEFORE-ADD:** si una pantalla ya está en B, primero simplificar/reordenar antes de agregar capa visual.
- **R-UTF8-NOBOM:** siempre UTF-8 sin BOM.
- **R-SRG-RUNTIME:** reflection debe probar nombres official y SRG + fallback.

### Docs
- **R-CIERRE:** al cerrar hito, sincronizar README, CHANGELOG, CONTEXTO, KNOWN_ISSUES, 25 changelog in-game.
- **R-REPLACE-VERSION:** nunca `.Replace(versionAnterior, versionNueva)` en docs con historial. Reemplazar solo en anclas únicas.
- **R-DOC-VERIFY:** comentarios que documentan limitaciones envejecen mal; verificar contra bytecode.

---

## 8. Errores históricos a NO repetir (selección)

- **E32:** `mod_version` hardcodeado → usar `project.version.toString()`.
- **E37:** mojibake por reeditar con encoding erróneo. Reescribir completo UTF-8 sin BOM.
- **E90/E111:** mixin sobre `renderBackground` falló porque método está heredado de `Screen`. Solución: mixin sobre `Screen` + filtro instanceof.
- **E125:** sin javac en entorno, solo usar APIs verificadas en repo.
- **E-STEEL-REGRESSION:** merge de PR #11 (v0.17.2 docs) sobre main sin incluir v0.19-v0.21 hizo que main retrocediera a v0.17.2 con steel pack. Fix: merge main → PR #10 branch resolviendo CONTEXTO/KNOWN_ISSUES, luego merge PR #10 → main. Lección: siempre verificar `git log --oneline --graph --all` y `build.gradle version` antes de mergear docs antiguos.

---

## 9. Estado actual y próximo hito

### Estado (v0.21.1)
- **Fase de interfaz CERRADA y pulida:** gabinete completo, sin resource packs, con memoria/descubrimientos y audio.
- Working tree: limpio tras limpieza profunda v0.21.1.
- Documentación sincronizada a v0.21.1, sin históricos obsoletos.
- 68 clases Java, 25 lang, 25 changelog, 42 OGG, 0 PNG de contenedores.
- No hay PRs abiertos.

### Próximo hito planificado
- **Candidato C (v0.22.0+): primer sistema REAL de gameplay** — Gripe Verde como enfermedad. Requiere diseño previo escrito (síntomas, contagio, cuarentena, NPCs, items, mecánicas ambientales). Riesgo ALTO.
- **Candidato O:** KI-12 mixin dirt bypass (baja prioridad).
- **Deuda menor:** tagear v0.21.1 cuando se valide en cliente real (no hay tags desde v0.13.10).

---

## 10. Documentación del proyecto (qué leer y en qué orden)

1. **`CONTEXTO.md`** — punto único de entrada. Addendums por versión + reglas + historial + plan. Leer COMPLETO antes de proponer cambios.
2. **`KNOWN_ISSUES.md`** — estado técnico, KIs abiertos/cerrados, reglas, errores.
3. **`CHANGELOG.md`** — historial narrativo completo (tono cronista).
4. **`README.md`** — portada pública + lista de pantallas + idiomas + requisitos.
5. Los 25 `changelog/*.md` in-game — crónicas diegéticas por idioma.

Históricos eliminados en v0.21.1: `AUDITORIA_PROFESIONAL.md`, `MICRODETALLES_EXTREMO.md`, `VALIDACION_FASE4.md` — ver git history si se necesitan.

---

## 11. Notas para otra IA que retome el proyecto

1. **No cambiar de branch** (sesión fijada a `arena/01a025bc-gripeverde`, ahora en sync con `main` v0.21.1).
2. **No agregar gameplay** salvo hito planificado con diseño previo escrito.
3. Validar JSON y referencias de sonidos antes de cada commit (`verifyGripeVerdeLocalization`, `verifyGripeVerdeSoundAssets`).
4. Tras cualquier cambio, entregar pipeline completo de build/deploy.
5. El sandbox no tiene JDK ni maven de Forge: build se hace en máquina del usuario.
6. Antes de escribir contra API vanilla, verificar firmas reales con `javap -p` sobre jar SRG.
7. Los documentos marcados como HISTÓRICOS fueron eliminados en v0.21.1; consultar git log si hace falta.
8. Mantener coherencia narrativa (R-DIEGETICO) y sincronización de docs (R-CIERRE) al cerrar cualquier hito.
9. Respetar R-NO-RESOURCEPACK: nada de steel pack sin instrucción explícita del usuario.
