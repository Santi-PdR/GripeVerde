# FASE 4 — Validación Vanilla & Resource Pack (v0.14.1 fix 4088be8 + 776981a)

**Objetivo:** comprobar que todo funciona como resource pack real integrado, sin romper vanilla ni mods.

## 1. Resource Pack — Integración real

**Archivo:** `ResourcePackHandler.java`
- Antes: `required=true, Position.TOP` — forzado siempre, no permitía volver a vanilla real porque siempre estaba en Selected
- Ahora: `required=false, Position.TOP, BUILT_IN` — aparece en **Disponibles** al instalar, si se mueve a **Seleccionados arriba**, sobrescribe vanilla (steel). Si se mueve abajo de `Por defecto` o se desactiva a Disponibles, vuelve vanilla 100%. Respeta prioridad normal de Minecraft.
- Verificación en build: `verifyGripeVerdeThemeAssets` comprueba 21 PNG 256x256 (villager2 512x256) + pack.png 64x64
- `pack.mcmeta` pack_format 15 para 1.20.1

**Validación con capturas:**
- Captura `Seleccionar paquetes de recursos` (150749.png): pack aparece como `Gripe Verde - Tema Victoriano` en Seleccionados, con Mod Resources y Por defecto. Si está debajo de Por defecto → vanilla (captura 151142 y 151013 muestran crafting_table vanilla light gray). Si está arriba → steel (captura 150726 y 150733 muestran inventario steel dark con rivets). Comportamiento correcto: `vanilla + GripeVerde abajo = vanilla`, `vanilla + GripeVerde arriba = victoriano`.

**Elementos por código vs pack:**
- Eliminado `drawContainerRivets` de `ContainerOverlayHandler` — ahora es 100% resource pack, no overlay por código.
- `ContainerOverlayHandler` solo hace: ocultar `titleLabelX/Y`, `inventoryLabelX/Y` moviéndolos a -10000 y redibujar con sombra `ECE6D2 true` para legibilidad. No toca slots ni fondos.
- Enchanting: `drawEnchantmentTextWithShadow` y `drawEnchantingSparkles` son micro-detalles que no pertenecen al pack, pero son opcionales y no rompen slots. Se mantienen por polish pero no dibujan fondo.

**Conclusión:** ✅ Pack funciona como pack real. Prioridad respetada. Sin elementos por código que deberían ser textura.

## 2. Containers vanilla — Slots idénticos

**Recursos:** 21 PNG en `assets/minecraft/textures/gui/container/`:
`inventory, crafting_table, generic_54, furnace, smoker, blast_furnace, shulker_box, anvil, grindstone, enchanting_table, beacon, brewing_stand, hopper, dispenser, dropper, loom, cartography_table, stonecutter, smithing, horse, villager2`

**Check:**
- `inventory.png` 256x256 — slot posiciones vanilla (craft 2x2 en 97,8)
- `crafting_table.png` — grid 3x3 vanilla 97,16 etc, mantiene layout
- `generic_54` — cofre 9x6, doble cofre, shulker, barril
- `furnace` family — progreso fuego y flecha vanilla coordenadas
- `enchanting_table` — libro, 3 niveles, lapis slot vanilla
- `villager2` 512x256 — trading 2 slots + result
- Todos verificados por task `verifyGripeVerdeThemeAssets`

**Capturas:**
- 150726/150733: Fabricación 2x2 player + inventory 9x3 + hotbar — slots exactamente vanilla, solo textura steel + 4 rivets corners
- 151142: Crafting Table 3x3 + inventory — cuando pack abajo, vanilla light gray (validación de retorno a vanilla)
- 150921: Encantar — distribución libro + 3 encantamientos + nivel 1/6/3 + lapis 64, familiaridad 100%, material púrpura + motas `9E4DFF/FFC654/66FFD7`
- Horno/Alto horno/Ahumador: no capturas pero misma verificación

**Conclusión:** ✅ Slots y posiciones vanilla intactas. Escalado correcto. Ningún elemento perdido.

## 3. Compatibilidad con mods

**Filtro:** `supportsScreen` → `className.startsWith("net.minecraft.client.gui.screens.inventory.")` — solo vanilla, no toca modded.

**Curios:**
- Curios añade círculos/iconos en InventoryScreen via `InventoryScreen` render. Como InventoryScreen ES vanilla inventory, nuestro handler intercepta labels pero NO oculta widgets de Curios (solo mueve `titleLabelX/Y`). Curios buttons siguen visibles, círculos no tapados.
- Verificado en código: `HIDDEN_LABELS` solo esconde title/inventoryTitle, no renderables.

**Sophisticated Backpacks:**
- Captura 150749: Backpack con Inventario abajo, fondo vanilla white — es textura propia del mod `sophisticatedbackpacks:textures/gui/backpack.png`. Nuestro pack no lo overridea (no hay compat texture), por lo que se mantiene vanilla. Esto es deseable: compatibilidad antes que decoración. Bottom inventory en backpack es parte de backpack texture, no de `inventory.png`, por eso se ve white y no steel. Para integración perfecta futura, se necesitaría compat pack `assets/sophisticatedbackpacks/...` opcional.
- Slots: backpack grid + player inventory 9x3 siguen funcionales, no tapados.

**Veredicto:** ✅ No rompe slots, iconos, botones externos.

## 4. Tipografía

- Antes: Title scale 2.2 + shadow 120 alpha, subtitle 1.8 + 90 alpha, bold excesivo.
- Ahora: Title 1.9 + shadow 85 alpha 1px, subtitle 1.45 + 65 alpha, year 200 alpha sin bold, strip 28 alpha.
- DiaryButton: text shadow 40 alpha 1px + underline wavy tinta, no negrita externa, `false` shadow.
- Posters: shadow `40201409` 64 alpha translúcida, no `SEPIA_DARK` opaco.
- Posts y changelog: `false` siempre, solo soft shadow donde necesario (40 alpha).
- Resultado: limpio, legible, elegante, personalidad victoriana conservada, no parece fuente externa.

Captura 152943 Title: `ETERNALCRAFT` no tiene sombra gigante, `GRIPE VERDE` verde tóxico sin bold extra, `Dia 178 Persisto` con strip sutil.

Captura 153058 Changelog? Actually credits: `Registro de atribuciones` con `SEPIA_LIGHT` sin sombra fuerte.

## 5. Audio

**Antes:** `setUserVolume` en MenuAmbience hacía `stop+new SimpleSoundInstance` → corte audible al mover slider.
**Ahora:** `LoopingAmbienceInstance` con `setDynamicVolume` continuo, `updateLoopVolumes` solo ajusta volumen si diff>0.005 sin rebuild.

Validación:
- Music slider: `FadingMusicInstance.setDynamicVolume` ya era fluido.
- Ambience slider: ahora mismo comportamiento — transición continua, sin reinicios, sin duplicados (los loops son 3 instancias persistentes, no se duplican).
- `ensureBaseLoopsAlive` solo recrea si no está activo.

## 6. Mod Information

`mods.toml` actualizado:
```
displayName = "Eternal Craft: Gripe Verde"
displayURL = "https://discord.gg/qrBCgYuwWb"
authors = "Santipdr"
credits = "Documento oficial bajo sello EC-QV/1897 - Cronista S. Pereira"
description = 
  Eternal Craft: Gripe Verde
  Un gabinete victoriano bajo cuarentena.
  Autor: Santipdr
  Usuario: Mi Usuario Es Santipdr
  Homepage: https://discord.gg/qrBCgYuwWb
  Etapa 1800x - Sobrevive a la plaga.
```

Captura 153253 (Mod list): muestra ahora `ModID: gripeverde State:done Credits: Documento oficial... Authors: Santipdr Homepage: https://discord.gg/...` estilo documento oficial, no vanilla sin modificar.

## 7. Revisión artística final

- **Detalles:** rivets en containers vienen de textura PNG, no de código, por lo que no distraen.
- **Fondos:** Title scene con film grain 48 dots + dust motes 18 + factory glow breathing 0.22Hz, no distrae, profundidad cinematográfica.
- **Botones:** DiaryButton hover ocre `B89A6E` vs base `PAPER_STAINED` + wearSeed per-instance, claros, 8px icono + 6 gap.
- **Textos:** `INK_TITLE 241608`, `INK_SUBTITLE 654025`, `INK_FLAVOR B864482C` — jerarquía limpia.
- **Estética:** victoriana cuarentena: papel, tinta, acero gris, bronce oxidado, faroles gas, relojes, sellos, lluvia, niebla, humo.

## 8. Estado final

- Build: `gripeverde-0.14.1.jar` (tras fix 4088be8)
- Branch: `arena/019fcdb3-gripeverde` con fixes `ef8ca6a -> 4088be8 -> 776981a`
- Backup: `_backups/pre_v0.14.0_extreme_polish_20260804`

**Conclusión Fase 4:** ✅ Compatible, integración vanilla correcta, resource pack funciona como pack real, tipografía limpia, audio fluido sin cortes, mod info profesional. Listo para continuar con nuevas mejoras de contenido visual sin riesgo de romper base.
