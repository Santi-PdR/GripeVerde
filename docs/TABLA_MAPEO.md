# Tabla de Mapeo — 21 GUIs Vanilla → Resource Pack Master Steel

| # | GUI Vanilla (1.20.1) | Ruta Resource Pack | Dim. | Notas de diseño Master Steel |
|---|---------------------|--------------------|------|------------------------------|
| 1 | `inventory.png` | `assets/minecraft/textures/gui/container/inventory.png` | 256×256 | 46 slots (4 armadura + 1 mano, crafting 2×2 +1, 27 inv +9 hotbar). Panel steel #909398, slots #63666B/#404348/#888B90. Sin arrow. Offhand slot idéntico. |
| 2 | `generic_54.png` | `assets/minecraft/textures/gui/container/generic_54.png` | 256×256 | Cofre 9×3/9×6 (27/54) + inventario. Reutilizado por `shulker_box` y barril. Player inv abajo integrado. |
| 3 | `crafting_table.png` | `assets/minecraft/textures/gui/container/crafting_table.png` | 256×256 | Grid 3×3 (29,16), resultado (124,35), **flecha bronze #C6A659 24×17 en (89,35)**. |
| 4 | `furnace.png` | `assets/minecraft/textures/gui/container/furnace.png` | 256×256 | Input (56,17), fuel (56,53), result (116,35). **Flecha bronze (79,34) + llama bronze (56,36) 14×14**. |
| 5 | `blast_furnace.png` | `assets/minecraft/textures/gui/container/blast_furnace.png` | 256×256 | Idem furnace, icono horno superior diferenciado por contexto. |
| 6 | `smoker.png` | `assets/minecraft/textures/gui/container/smoker.png` | 256×256 | Idem furnace. |
| 7 | `shulker_box.png` | `assets/minecraft/textures/gui/container/shulker_box.png` | 256×256 | 27 slots + inventario. Color base steel, sin tinte púrpura vanilla. |
| 8 | `anvil.png` | `assets/minecraft/textures/gui/container/anvil.png` | 256×256 | 2→1 slots, barra coste, **error rojo mutado #8B2C1F**, sin rivets. |
| 9 | `grindstone.png` | `assets/minecraft/textures/gui/container/grindstone.png` | 256×256 | 2→1, error X sprite en bronze oscuro. |
|10 | `enchanting_table.png` | `assets/minecraft/textures/gui/container/enchanting_table.png` | 256×256 | Libro central, 3×encantamientos. **Chasis obsidiana #2D2A3E**, **botones plata lunar #C8CEE0**. |
|11 | `beacon.png` | `assets/minecraft/textures/gui/container/beacon.png` | 256×256 | Botones 5×3 + confirm/cancel. **Biseles ópticos bronze #D4B673 en botones**. |
|12 | `brewing_stand.png` | `assets/minecraft/textures/gui/container/brewing_stand.png` | 256×256 | 3 pociones + ingrediente + fuel. **Tubos latón dorado #D4B673 1 px**, bubbles/bronze sprites. |
|13 | `hopper.png` | `assets/minecraft/textures/gui/container/hopper.png` | 256×256 | 5 slots (43,20) + inventario. Altura 133 px. |
|14 | `dispenser.png` | `assets/minecraft/textures/gui/container/dispenser.png` | 256×256 | 3×3 grid (61,16). |
|15 | `dropper.png` | `assets/minecraft/textures/gui/container/dropper.png` | 256×256 | Copia 1:1 de dispenser (vanilla idéntico). |
|16 | `loom.png` | `assets/minecraft/textures/gui/container/loom.png` | 256×256 | Banner + dye + pattern, preview, scroller. Wood → steel. **Pattern highlight bronze**. |
|17 | `cartography_table.png` | `assets/minecraft/textures/gui/container/cartography_table.png` | 256×256 | Map + paper, preview, result. |
|18 | `stonecutter.png` | `assets/minecraft/textures/gui/container/stonecutter.png` | 256×256 | Input, result, lista recetas + scroller. Thumb bronze. |
|19 | `smithing.png` | `assets/minecraft/textures/gui/container/smithing.png` | 256×256 | 3 slots (8,48 / 48,48 /68,48 approx). Template netherite. **Flecha bronze**. |
|20 | `horse.png` | `assets/minecraft/textures/gui/container/horse.png` | 256×256 | 2 + saddle/armor + chest 5×3 + inv. |
|21 | `villager.png` / `villager2.png` | `assets/minecraft/textures/gui/container/villager.png` (y alias `villager2.png`) | **512×256** | Trading 2+1, flecha bronze, barra XP bronze #C6A659, scroller bronze. Alias `villager2.png` para compat con prompt (copia 1:1). |

**Sprites adicionales (no contados como GUIs pero incluidos para coherencia):**

| Sprite | Ruta | Nota |
|--------|------|------|
| `furnace/burn_progress` | `textures/gui/sprites/container/furnace/burn_progress.png` | 24×16 flecha fill bronze |
| `furnace/lit_progress` | `textures/gui/sprites/container/furnace/lit_progress.png` | 14×14 llama bronze |
| `blast_furnace/*` | idem | variantes horno |
| `smoker/*` | idem |  |
| `brewing_stand/brew_progress` | `.../brewing_stand/brew_progress.png` | 9×28 barra vertical bronze |
| `brewing_stand/bubbles` | `.../bubbles.png` | 12×29 burbujas bronze light |
| `brewing_stand/fuel_length` | `.../fuel_length.png` | 18×4 fuel bronze |

**Total entregado:** 22 PNG container (21 + alias villager2) + 1 slot_master + 1 pack.png + 8 sprites = 32 imágenes, todas 1:1 dimensiones vanilla.

**Verificación:** `verifyGripeVerdeThemeAssets` (Gradle) y `tools/verify_pack.py` comprueban existencia, dimensiones, alfa, pack_format 15, 64×64 icono.
