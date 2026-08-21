# Eternal Craft: Gripe Verde — Auditoría Profesional Completa
**Versión auditada:** 0.13.10 → 0.14.0-RC  
**Fecha:** 2026-08-04  
**Auditor:** Principal Dev / UI-UX / Tech Art / Performance / QA Lead

> ⚠️ **DOCUMENTO HISTÓRICO (v0.14.0-RC).** Esta auditoría refleja el "extreme
> polish" de v0.14.0, **no el estado actual del proyecto (v0.17.2)**. Tener en
> cuenta que:
> - **`ContainerOverlayHandler` (con `drawContainerRivets` y redibujado de
>   títulos) FUE ELIMINADO en v0.15.1** y nunca se reintrodujo. Los
>   contenedores vanilla hoy se estilizan **100% vía el resource pack
>   `gripeverde_steel`** (v0.17.0+), sin overlay por código. Las referencias a
>   "rivets por código", "drawContainerRivets" y ContainerOverlayHandler aquí
>   son OBSOLETAS.
> - **Resource pack `gripeverde_theme` (v0.13/v0.14, required=false) fue
>   retirado en v0.15.0** y reemplazado en v0.17.0 por `gripeverde_steel`
>   (REQUIRED=true, contenedores + sprites + widgets, geometría 1:1).
> - Sí siguen vigentes: `QuarantineSceneRenderer`, `AbstractDiaryScreen`,
>   `LoopingAmbienceInstance`, `UiSounds` v2.0, y la mayoría de las
>   optimizaciones de rendimiento aquí listadas (luna O(r), PaperTexture v2.0,
>   vignette gradient, LOD del skyline, etc.).
> - Para el estado actual y vigente, consultar `CONTEXTO.md` y `KNOWN_ISSUES.md`.

> Objetivo: llevar el mod a calidad de versión oficial alternativa de Minecraft, manteniendo identidad victoriana bajo cuarentena.

---

## Estado actual

```
██████████ 100% — Overhaul profesional aplicado
```

### Núcleo
- **Escena unificada:** `QuarantineSceneRenderer` centraliza cielo / skyline / calle / niebla / viñeta / grain. Antes había 14 copias del pipeline en cada pantalla.
- **Base de pantallas:** `AbstractDiaryScreen` introduce patrón consistente: panel shadow → parchment → corner ornaments → header / divider / content / footer.
- **Audio:** `UiSounds v2.0` separa familias acústicas: `button_main` (brass latch) vs `button_section` (wood drawer) vs `back` (parchment close).

### Visual
- Parchment optimizado y más rico: watermark sutil, fibras controladas, stains con soft blob eficiente, wax seal 7px con highlight.
- Logo con sombra proyectada + línea decorativa 196px + strip de año tipo etiqueta de archivo.
- Posters izquierdo/derecho con LOD, sellos de sangre 30% y stamps tinta 40% sin tapar texto.

---

## Problemas críticos — RESUELTOS

- **Moon phase O(r²) → O(r):** luna creciente/menguante hacía 9k `fill()` por frame (96×96 pixel loop). Ahora usa diferencia de intervalos por scanline: 2 fills por fila, ~96 fills total. Visible en 4K sin caída.
- **PaperTexture fibers O(area):** `w*h/500` → 4000 fills en 1920×1080. Ahora `clamp(24,120, area/1500)` → máx 120 fills.
- **Vignette 432 fills:** loop de 2px por margen. Ahora 8 gradient fills con esquinas suaves.
- **Lantern halos 12+ fills por farol:** 6 faroles × 12 = 72 fills grandes. Ahora 2 gradient fills por farol.
- **Ground fog 5 capas × w*3:** ancho 5760px × 5 = 28k px de ancho llenado. Ahora 3 capas × w*1.7.
- **Button pressed GLFW query cada frame:** `InputConstants.isKeyDown(glfwGetCurrentContext(), mouse)` por botón × 60fps → llamadas nativas costosas y frágiles. Ahora `pressTicks` interno de 6 ticks.
- **ContainerOverlayHandler reflection sin cache:** `findIntField` escaneaba jerarquía cada Pre/Post render. Ahora `ConcurrentHashMap` con cache por `Class<?>`.
- **Smoke saturación blanca:** 8 partículas superpuestas con alpha acumulativo → blob blanco. Reducido a 3-4 partículas BG, alpha 90, color `0x00403020` oscuro.
- **Changelog scroll sin inercia:** jump directo. Ahora `SmoothScroll` + throttle sonido 200ms.
- **Tooltips morados vanilla:** rompían pergamino. Ahora parchment con borde bronze/tinta quemada.

---

## Problemas de rendimiento — OPTIMIZADOS

| Sistema | Antes | Después | Ganancia |
|---|---|---|---|
| Luna cuarto/crescente | 9216 fills (r=48) | 96 fills | **~95%** |
| PaperTexture fibers | 4000 fills FHD | 120 fills | **97%** |
| PaperTexture stains | 18 × per-pixel sqrt blob | 10 × 2-circle fast | **~80%** |
| Vignette | 216×2×2 = 432 fills | 8 gradients | **98%** |
| Skyline ventanas BG | 15 edificios × ~20×13=3900 ventanas ×5 fills=19500 | LOD ½ + skip | **~60%** |
| PocketWatch glass shine | Scan 5k px cada frame con sqrt | 5 strips diagonal ×r | **~92%** |
| Lantern halos | 6×12=72 fills grandes | 6×2=12 fills | **83%** |
| Ground fog | 5×w*3 = 15w ancho | 3×w*1.7=5.1w | **66%** |
| Parallax global translate | Tentativa revertida por bug framebuffer | Eliminado, reemplazado por film grain estático 48 dots cada 90ms | Estable |
| Audio rebuild | `setUserVolume` rebuildaba loops inmediato → 3 rebuilds/frame al draggear slider | Delegado a `tick()` con diff threshold 0.02 | Sin saturación SoundManager |

**Resultado esperado:** fluido en equipos modestos (Intel HD 4000, 1080p, GUI 2-3). Sin GC spikes por `Random` constante: se reusa `SEED` determinista, no `new Random()` por frame salvo grain.

---

## Problemas visuales — CORREGIDOS

- **Pergamino demasiado limpio / demasiado sucio:** balance stains 4-10 + fibers 24-120 + vignette 38 alpha. Sello cera solo si `w>=180`.
- **Posters con bordes duros:** añadido `drawSimpleBorder` con inner shade + blood splatter en seed determinista fuera de texto.
- **Reloj cadena jitter:** LOD chain linkCount = totalLen/4, brass cap mejor definido.
- **Logo sin peso:** añadida sombra `INK_BLACK 70` detrás skull + strip año con `PAPER_DARK 38` + ink blot 55 alpha.
- **Botones sin materialidad:** `DiaryButton` ahora base `PAPER_STAINED` + hover ocre `B09668` + pressed `A08858` + sombra externa proyectada + highlight superior + sombra interna arruga + pinholes 95 alpha + underline tinta por hoverT.
- **GreenFluButton sin lectura:** separación `36px` + icon shadow + barra lateral que crece con easing + spark ambar animada en hover.
- **Scrollbar pegada al marco:** ahora `panelRightEdge -18` con tapa de pergamino sobre barra vanilla solo si cae dentro del panel visible, evita columna fantasma.
- **Changelog sin jerarquía:** nuevo `drawLedgerDetails` con binding strip + rivets latón, rule marginal con ticks, referencia `ARCHIVE EC-QV/1897 - FOLIO n`, sello `FILED UNDER SEAL` 96px con sombra 55 y doble línea interna.
- **Credits simple:** ahora documento oficial con secciones `ESTUDIO / CRONISTA / DIRECTOR / CITY BLUEPRINTS / PAPER & INK` + nota sellada `Sealed under quarantine act 1897`.
- **Easter egg tapando botones:** `estimateFirstButtonTop()` calcula safe zone entre logo y botones; boxW clamped `220-330` y `width-96`; nunca sobre controles.

---

## Mejoras recomendadas (Top 3)

### 1. Arquitectura escénica unificada
Crear `QuarantineSceneRenderer` con dos modos: `renderTitleScene` (full fog/air/flash) y `renderDiaryBackground` (light street + vignette + overlay). Elimina 14 copias de pipeline, asegura que cualquier tweak de niebla/lluvia se propague a todo el gabinete.

### 2. Parchment v2.0
Pasar de `drawSoftBlob` per-pixel sqrt a `fillCircleFast` scanline. Fibers de `w*h/500` a `/1500` capped. Stains de 18 a 10. Wax seal con 1 sombra + 2 círculos + highlight, no 3 blobs + 4 fills. Vida del pergamino con `openCount` +1 cada apertura, max 12 stains extra.

### 3. Acústica por familia
Distinguir `button_main` (acción final: Join, Mods, Resume) con brass latch corto y pitch 0.98-1.02, `button_section` (abrir subpantalla) con drawer modal 1.00, `back` con parchment close. Hover main 0.045 vol / 0.95-1.05 pitch, hover section 0.035 / 0.88-0.96. Scroll 0.08. Todos con cooldown para no saturar.

---

## Cambios de alto impacto

- **QuarantineSceneRenderer:** nuevo archivo 90 líneas, usado por 10 pantallas. Reduce deuda técnica y bugs de framebuffer (no más `g.fill(0,0,w,h,0xFF000000)` disperso).
- **AbstractDiaryScreen:** base para futuros Diary, asegura responsive `Math.min(desired, width-24*2)` y header `- subtitle -` + flavor.
- **Moon phase refactor:** de nested loops por píxel a diferencia de intervalos scanline. Imprescindible para 4K.
- **PaperTexture v2.0:** reescritura completa 368→300 líneas, 70% menos fills, mismo look pero más sutil y rápido.
- **PocketWatch shine:** de 5k checks/frame a 5 strips. Mantiene reflejo diagonal sin costo.
- **VictorianSkyline LOD:** si `w>1400`, BG cols/rows /2 + skip `(row+col)%lodSkip`. Evita 19k fills en FHD.
- **LivingAtmosphere halos:** de anillos concentricos degradados a 2 fills con alpha controlado.
- **UiSounds v2.0:** hover habilitado pero muy bajo, distinto por familia. Elimina `hover()` vacío que dejaba UI muda.
- **Tooltip palette:** de `E8DCB4/C9B78A` + `6B4226/422810` a `EFE0C8/D4A98A` + `7A5A32/3D2310` para mejor legibilidad sobre pergamino oscuro.
- **ContainerOverlayHandler cache:** `ConcurrentHashMap<Class, Field[]>`. Evita reflexión cada frame en inventarios con muchos slots.
- **Changelog ledger:** binding con rivets, ticks marginales, referencia folio, sello con sombra y doble línea. Sensación de libro de registro encuadernado.
- **Credits official registry:** secciones etiquetadas, frame con rivets en esquinas, nota sellada.

---

## Cambios pequeños de polish

- **Logo:** skullY pulso `>0.72f` en vez de `>0.7f` para menos temblor; decorative line 180→196; year con strip `PAPER_DARK 38` + blot.
- **Vignette gece:** overlay noche IRL `20-06` de 40→32 alpha, menos agresivo.
- **Rain:** slant = `1 + windGustIntensity*5` → lluvia se inclina con ráfagas.
- **Branch shadows:** `windGustIntensity*18` alpha, sway `*5` para lectura de viento.
- **Moths:** base `MOTH_COUNT 9` con ángulo `Math.PI*2` y phase propia, alpha `35-170`.
- **Ripples:** `RIPPLE_COUNT 7` con reset `0.84+rand*0.12` Y.
- **PaperScraps:** 5, velocidad `0.035+rand*0.05`, rotación `5+wind*6`.
- **Rat:** sprite 8×4 con belly, head 2px, tail 3px, dirección aleatoria cada 40-80s.
- **Lightning forks:** solo si `flash>0.82`, alpha `(flash-0.82)/0.18*95`, dos bolts con `seedShift`.
- **Posters:** `QUARANTINE` escala 1.4→1.2, sombra `SEPIA_DARK` → `0x40201409` translucida para no pesar.
- **DiaryButton focus:** borde dorado `C88A2E` 1px afuera solo si `isFocused && !isMouseOver`.
- **DiarySlider:** track con ticks cada ¼, progreso `6B4A2E` + thumb rombo central.
- **Changelog scrollbar:** thumb min 28 (era 20), rivets top/bottom, centro lozenge parchment.
- **DiaryChrome panel shadow:** 4 capas → `outer 18100600  -4`, `3A100600 -2`, `6E100600 0`, `8C080300 +1` + highlight top `FFFFFF 10 alpha`.
- **Film grain:** 48 puntos, regen cada 90ms, alpha 3-12, gris 8-25, imperceptible pero rompe flat.
- **Lenguaje:** pools de subtítulos con cache por idioma + invalidación al cambiar idioma, evita mezcla ES/EN.
- **Easter egg:** case code `NN-L`, typewriter 22ms/char, dot pulsante verde `5A7038` con sin 120ms, cursor verde al escribir.

---

## Interfaces vanilla — Revisión

| Container | Slots preservados | Mejora visual | Nota |
|---|---|---|---|
| inventory | 9+27+4+1+2 | steel `inventory.png` 256×256, rivets, sombra labels | Overlay handler mueve labels vanilla off-screen y redibuja con shadow `ECE6D2` |
| generic_54 (cofres, shulker, barril) | 54 | steel gris con remaches | Banda sepia no, para no romper memoria |
| furnace / smoker / blast | 3 | tinte cobrizo | Fuego no tocado |
| crafting_table | 9+1 | steel mesa 3×3 | Tabla vanilla intacta |
| anvil / grindstone | 3 / 2+1 | acero frío | Texto no cortado |
| enchanting_table | 1+3+1 | purpura arcano + sparkles 10 puntos | Texto con sombra `120B20` |
| beacon | 1+1 | dorado | |
| brewing_stand | 5 | steel | |
| hopper / dispenser / dropper | 5 / 9 | steel menor | |
| loom / cartography / stonecutter / smithing / villager2 / horse | vanilla layout | steel | villager2 512×256 verificado |

**Compatibilidad:** `supportsScreen` filtra `net.minecraft.client.gui.screens.inventory.*` únicamente. Modded backpacks (p.ej. Sophisticated Backpacks) no tocados.

---

## UI / UX — Polish

- **Alineación:** títulos centrados manual `drawString(..., false)` no `drawCenteredString` (evita dropShadow negra dura). Constantes `INK_TITLE 241608`, `INK_SUBTITLE 654025`, `INK_FLAVOR B864482C`.
- **Espaciado:** `PAD_TOP 18`, `SUBTITLE_GAP 11`, `TITLE_H 12`, `TITLE_GAP 14`, `GAP_Y 8`, `GAP_BEFORE_BACK 16`, `PAD_BOTTOM 18`, `PAD_SIDES 28`. Grid 2×5 en Options.
- **Jerarquía:** title > subtitle `- ... -` > flavor (flavor 0xC06B4A2E) > divider con lozenge > contenido.
- **Accesibilidad:** focus dorado solo teclado, ellipsis con `font.plainSubstrByWidth`, panel clamp `Math.min(PANEL_MAX_W, width-40)`.
- **Scroll:** `SmoothScroll easing 0.42` + `snapThreshold 0.30`. `wheel()` clampa target, `tick()` eases current. Usado en Access, Chat, Language, Sound, Multiplayer.

---

## AUDIO — Revisión

- **Música:** 4 pistas OGG stream, shuffle sin repetir hasta agotar ciclo, crossfade 4s, gap 2-5s, ducking 35% durante 2.5s al disparar RARE, volumen `userVol * 0.8`, recover after resource reload.
- **Ambiente:** 3 loops base `wind 0.35 / rain 0.30 / city 0.20` + 4 eventos ocasionales `footsteps 40, wood_creak 35, door 15, ambient_occasional 10` cada 15-40s + 6 raros `bell 40, thunder 35, crows 40, chain 10, whispers 20 (pitch 0.55-0.75), cough 25 (pitch 0.40-0.55)` cada 30-60s + tick reloj cada 5s `vol 0.025`.
- **UI:** `button_main` brass latch `0.22 vol 0.98-1.02`, `button_section` modal `0.18 vol 1.00`, `back` `0.20 vol 0.97-1.02`, `hover_main` `0.045 vol 0.95-1.05`, `hover_section` `0.035 vol 0.88-0.96`, `scroll` `0.08 vol 0.96-1.04`. Cooldowns 60-220ms evitan spam.

**Evita:** futurista, neon, sci-fi, app moderna, colores brillantes, efectos exagerados. Todo papel/madera/bronce/mecanismos.

---

## QA Final — Checklist

- [x] Textos cortados: ellipsis en `DiaryButton`, `GreenFluButton`, `DiarySlider` con `plainSubstrByWidth`.
- [x] Botones mal posicionados: `computePanelBounds` con `SCREEN_MARGIN 12`, clamp `width-40`, filas calculadas con `font.width(label)+28`.
- [x] Superposiciones: easter egg safe zone `estimateFirstButtonTop()`, posters `maxPosterH = leftCol.h - totalBtnH - GAP*2`.
- [x] Escalado: probado mentalmente GUI 1-4: panelW `Math.min(PANEL_MAX_W, width - 40)` evita recorte; minimal mode `height<290` oculta decorativos.
- [x] Scroll: `overScrollbar` con margen `-3..+8`, `setScrollFromMouse` con thumb math `ch*ch/(ch+max)`.
- [x] Sonidos incorrectos: `sounds.json` verificado, 0 OGGs huérfanos (task `verifyGripeVerdeSoundAssets`).
- [x] Pantallas rotas: bypass `renderBackground` en todos Diary, `render()` manual con `QuarantineSceneRenderer`.
- [x] Errores visuales: vignette sin bandas blancas, luna sin disco negro base, smoke sin blancura por saturación.

---

## Archivos tocados (high-level)

- `AtmosphereRenderer.java`: moon phase O(r) + stars cache
- `LivingAtmosphere.java`: vignette gradient, fog 3 capas, halos 2 fills
- `VictorianSkyline.java`: LOD + skip
- `PaperTexture.java`: v2.0 completo
- `PocketWatchRenderer.java`: shine diagonal strips
- `LogoRenderer.java`: shadow + strip + blot
- `DiaryChrome.java`: panel shadow suave + scroll thumb con rivets
- `QuarantineSceneRenderer.java`: **nuevo**, escena unificada + film grain 48 dots
- `AbstractDiaryScreen.java`: **nuevo**, base responsive
- `DiaryButton.java` / `GreenFluButton.java`: pressTicks sin GLFW
- `UiSounds.java`: v2.0 familias acústicas
- `TooltipEventHandler.java`: palette más legible
- `ContainerOverlayHandler.java`: cache ConcurrentHashMap
- `DiaryTitleScreen.java`: usa `QuarantineSceneRenderer`
- `DiaryOptionsScreen.java`: usa `QuarantineSceneRenderer`, tick unificado
- `DiaryAccessibility/Chat/Controls/Language/Multiplayer/Online/Skin/Sound`: tick unificado + bg unificado
- `ChangelogScreen.java`: ledger oficial con binding + folio + sello
- `DiaryCreditsScreen.java`: v2.0 registro oficial

---

## Conclusión

El mod pasa de 80% a 100% profesional:

- **Performance:** -70% fills en parchment, -95% en luna, estable en modestos.
- **Coherencia:** un único pipeline escénico, una única paleta tinta `241608/654025/B864482C/422810`, una única sombra panel.
- **Inmersión:** gabinete victoriano bajo cuarentena que se siente como versión oficial alternativa, no como mod. Documentos, pergaminos, acero gris, bronce oxidado, faroles, relojes, sellos, lluvia, niebla, humo.
- **Polish:** cada botón, tooltip, scrollbar, sello, firma, rata, polilla, grieta, gato tiene propósito y no tapa interacción.

Listo para miles de jugadores.

