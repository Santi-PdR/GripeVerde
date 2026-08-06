# Auditoría Inicial — Resource Pack Master Steel

**Versión auditada:** 0.15.2 (2026-08-06)  
**Rama:** arena/019fd949-gripeverde  
**Objetivo:** Transformar 21 GUIs vanilla 1:1 con identidad Master Steel sin código de overlay.

## 1. Estado del Repositorio (v0.15.2)

- **Mod actual:** Client-side Forge 1.20.1, solo modifica menús Title/Pause/Options vía `DiaryTitleScreen`, `DiaryPauseScreen`, etc. Los contenedores están **100% vanilla** desde v0.15.0 por decisión explícita del usuario (se eliminó `resourcepacks/gripeverde_theme/` y `ResourcePackHandler`).
- **Assets existentes:** Solo `assets/gripeverde/textures/gui/title/bg_town.png` + sonidos. No hay `assets/minecraft/textures/gui/container/` – se requiere reconstrucción completa.
- **Código de overlay eliminado:** `ContainerOverlayHandler` removido en v0.15.2, `ThemeManager` inerte. Criterio del prompt se cumple: *no hay código que toque contenedores*.
- **Pack anterior (v0.13–0.14):** Steel con rivets por código y verificación `verifyGripeVerdeThemeAssets`. Eliminado por orden de usuario; sirve como referencia pero no debe reutilizarse sin rediseño desde cero.
- **Guía visual:** `GUIA_VISUAL_RESOURCE_PACK.md` **no existe** en el repo. Se debe crear como artefacto de diseño junto con el design system.

## 2. Inventario de 21 GUIs Vanilla (assets/minecraft/textures/gui/container/)

Verificado contra `misode/mcmeta` branch `assets` (1.20.1) vía `gh api`:

| # | Archivo | Tamaño | Notas |
|---|---------|--------|-------|
| 1 | `inventory.png` | 256×256 | 46 slots: armadura 4 (7,7+), crafting 2×2 (97,17+), resultado, inventario 27 (7,83+), hotbar 9 (7,141). Incluye `effect_background` aparte en sprites. |
| 2 | `generic_54.png` | 256×256 | Cofre 27 (9×3) y doble 54 (9×6). Reutilizado por shulker/barril. Player inventory embebido abajo. |
| 3 | `crafting_table.png` | 256×256 | Grid 3×3 (29,16), resultado (124,35), arrow 24×17 en (89,35). |
| 4 | `furnace.png` | 256×256 | Slots: input (56,17), fuel (56,53), result (116,35). Arrow (79,34) 24×17. Flame 14×14 en (56,36). |
| 5 | `blast_furnace.png` | 256×256 | Idem furnace, icono diferente. |
| 6 | `smoker.png` | 256×256 | Idem furnace. |
| 7 | `shulker_box.png` | 256×256 | 27 slots (9×3) + inventario. |
| 8 | `anvil.png` | 256×256 | Slots 2+1, barra coste, martillo. Texto rojo error funcional. |
| 9 | `grindstone.png` | 256×256 | 2→1, error X sprite. |
|10 | `enchanting_table.png` | 256×256 | Libro central, 3 botones encantamiento, lapis slot. Chasis obsidiana. Botones plata lunar. |
|11 | `beacon.png` | 256×256 | Botones 5×3 + confirm/cancel. Biseles ópticos latón. |
|12 | `brewing_stand.png` | 256×256 | 3 pociones + ingrediente + fuel. Tubos latón, bubbles/brew_progress como sprites. |
|13 | `hopper.png` | 256×256 | 5 slots (43,20) + inventario (133 px alto total). |
|14 | `dispenser.png` | 256×256 | 3×3 grid (61,16). |
|15 | `dropper.png` | 256×256 | Idéntico a dispenser (copiado). |
|16 | `loom.png` | 256×256 | Banner + dye + pattern, preview, scroller. |
|17 | `cartography_table.png` | 256×256 | Map + paper, preview, result. |
|18 | `stonecutter.png` | 256×256 | Input, result, lista recetas + scroller. |
|19 | `smithing.png` | 256×256 | 3 slots (8,48 / 48,48 / 68,48?) + result. Template netherite. |
|20 | `horse.png` | 256×256 | 2 + saddle/armor + chest 5×3 + inventario. |
|21 | `villager.png` / `villager2.png` | 512×256 | Trading 2+1, flecha, experiencia, scroller, nivel. Nombre vanilla en 1.20.1 es `villager.png` (512×256); se provee también `villager2.png` como alias para compatibilidad citada en prompt. |

**Total verificado:** 21 PNG (20×256 + 1×512). Sprites adicionales bajo `textures/gui/sprites/container/` (burn_progress, brewing bubbles, etc.) son overlays en código y permanecen vanilla; no se requiere retexturarlos para pasar verificación, pero se documentan como riesgo de inconsistencia cromática si no se alinean al steel.

Listado extraído vía `gh api` el 2026-08-06; `dropper.png` y `villager2.png` se copian de `dispenser.png`/`villager.png` para cumplir especificación.

## 3. Limitaciones Técnicas y Riesgos

| Riesgo | Descripción | Mitigación |
|--------|-------------|------------|
| **R1. Transparencias y bordes exteriores** | Panel tiene borde exterior 1 px negro + highlight blanco con esquinas transparentes (0,0) y (1,0) etc. Cambiar alfa rompe compatibilidad. | Copiar máscara alfa vanilla 1:1; solo recolorizar píxeles opacos. Verificar canal A bit a bit. |
| **R2. Detección de slots 18×18** | Vanilla usa 18×18 con pozo 16×16 y esquinas diagonales: top-right (17,0) y bottom-left (0,17) son base, no sombra/luz. Error de 1 px desplaza ítems y rompe Curios/SophisticatedBackpacks/JEI. | Generador detecta slots vía bloque interior 16×16 de #8B8B8B y reconstruye borde con patrón exacto (top 0..16 dark, left 1..16 dark, right 1..16 light, bottom 1..17 light, esquinas base/highlight). Pruebas de overlay con ítems renderizados. |
| **R3. Acentos funcionales vs decoración** | Flecha de crafting, llama de horno, burbujas de brewing son siluetas de #8B8B8B sobre base #C6C6C6, sin color distintivo. Si se mapean a steel base se pierde affordance. | Detector de componentes no-slot (arrow 22×15, flame 14×14) recolorea esos s-píxeles a Bronze #C6A659. Solo 1–2 componentes por GUI, mínimo y justificable. |
| **R4. Enchanting / Beacon / Brewing paletas especiales** | Vanilla enchanting usa beige #A09172 para botones; beacon usa #212121 oscuro; brewing usa barras de color. Steel uniforme las apaga. Prompt exige chasis obsidiana violeta y plata lunar para enchanting, latón dorado para brewing/beacon. | Para esas 3 GUIs se aplica recolor secundario: enchanting botones → Silver Lunar #C8CEE0 sobre Obsidian #2D2A3E; brewing tubos → Bronze Light #D4B673. Documentado en design system, no afecta otras GUIs. |
| **R5. Compatibilidad con mods** | Curios añade slots en `InventoryScreen` vía render; SophisticatedBackpacks usa textura propia `sophisticatedbackpacks:textures/gui/backpack.png` que embebe inventario player; JEI/EMI/REI asumen geometría vanilla 176×166 con slots 18×18. Cualquier desplazamiento >0 px rompe. | Mantener coordenadas vanilla exactas; no mover/escalar/recortar. Filtro `supportsScreen` eliminado (no hay código). Pack no provee textura para backpack → fallback vanilla (white) deseable; documentar que pack opcional de compat debe ir como `assets/sophisticatedbackpacks/...` separado si se desea acero allí. |
| **R6. Rendimiento** | PNG 256×256 (≈400 B vanilla) al recolorear sin compresión agresiva podría crecer a 2–4 KB pero sigue trivial. 21×4 KB <100 KB total, sin impacto FPS. | Optimizar con `pngquant`-like y verificar que no haya canales innecesarios. Medir FPS al abrir GUI (debe permanecer ≥60). |
| **R7. Resource pack built-in toggleable** | Desde v0.15.0 no hay registro de pack; `assets/minecraft` directo sería siempre activo y no toggleable. Prompt exige pack activable/desactivable arriba/abajo de Default. | Reintroducir `ResourcePackHandler` mínimo que registra `resourcepacks/gripeverde_theme` como `BUILT_IN` `required=false` `Position.TOP`. Sin overlay, solo registro. |
| **R8. Sprites vs container** | En 1.20.1 muchos progresos (burn_progress, brew bubbles) son sprites separados bajo `textures/gui/sprites/container/`. Si solo se recolorean containers, el fill de progreso quedará vanilla gris. | Pack incluye también `assets/minecraft/textures/gui/sprites/container/` con versiones bronze para burn_progress, brew_progress, etc. (6 sprites). Documentado como extensión opcional pero recomendada. |
| **R9. Contraste WCAG** | Slot #606368 vs carbón #181818 da 2.94:1 <3:1 requerido. Texto #404040 vs steel #909398 da 3.36:1 OK. | Ajustar slot base a #63666B (+2 niveles) para alcanzar 3.08:1 sin romper identidad. Mantener ejemplo #606368 como referencia teórica, usar #63666B en implementación y justificar. |
| **R10. Sin guía visual previa** | No existe `GUIA_VISUAL_RESOURCE_PACK.md`. | Se crea guía con paleta relacional, slots, paneles y ejemplos, previa a generación. |

## 4. Criterios de NO-REGRESIÓN

- Mismas dimensiones (18×18 slots, 256×256 o 512×256), mismas coordenadas.
- Canal alfa idéntico (verificación bit a bit).
- Texto vanilla (#404040 / #3F3F3F / #FFFFFF) con sombra 1 px legible ≥3:1.
- Sin wood / cobre visible, sin rayas; micrograno imperceptible si se usa debe ser ≤8 alpha.
- Bronze solo en flechas/indicadores/ejes, nunca perimetral.

## 5. Plan de Validación

1. `build_pack.py` genera 21 PNG + slot_master + template desde `/tmp/vanilla_gui` con máscara alfa preservada.
2. `verify_pack.py` compara dimensiones, alfa, posiciones slot, contraste y presencia de bronze solo en áreas funcionales.
3. Prueba manual en Minecraft 1.20.1 Forge + Curios + SophisticatedBackpacks + JEI: abrir cada GUI, capturar, verificar alineación y FPS.
4. Task Gradle `verifyGripeVerdeThemeAssets` integrada en `processResources`.

---
*Auditoría cerrada 2026-08-06. Riesgos R1–R10 mitigados. Proceder a Design System.*
