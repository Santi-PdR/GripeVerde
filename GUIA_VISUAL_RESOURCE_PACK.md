# Gripe Verde — Guía Visual Resource Pack Master Steel
**Versión:** 1.0 — 2026-08-06  
**Motor:** Forge 1.20.1 (pack_format 15)  
**Identidad:** *Master Steel* — acero industrial gris cálido, mate, homogéneo, con micrograno imperceptible y biseles 1 px discretos. Nada steampunk ni barroco.

> Esta guía es la **única fuente de verdad** para todas las texturas GUI. Cualquier PNG que no siga estas reglas será rechazado por `verifyGripeVerdeThemeAssets`.

---

## 1. Filosofía “Menos es más”

- **Material base:** Chapa de acero gris cálido, no madera, no cobre, no rayas.
- **Relieve físico 1 px:** Luz suave abajo/derecha, sombra leve arriba/izquierda. Contraste de bisel ≈ ±12 %.
- **Bronze solo como acento funcional:** Flechas, llamas, barras de progreso, ejes, indicadores. Nunca decoración perimetral, nunca remaches múltiples.
- **Menos es oficial:** Debe parecer textura oficial de Minecraft, intervenida por un gabinete industrial, no por un mod.
- **Pixel-perfect vanilla:** Mismas dimensiones (256×256, villager 512×256), mismos 18×18 slots (pozo 16×16), mismas transparencias. JEI/EMI/REI, Curios y Sophisticated Backpacks deben encajar sin retoque.

---

## 2. Paleta Relacional Master Steel

Elegimos un **valor base B** y todo lo demás deriva por luminosidad relativa (HSL). Así la familia permanece coherente si B cambia.

### 2.1 Valor Base

- **B — Steel Base:** `#909398` `rgb(144,147,152)` `hsl(214°, 3.7%, 58%)`  
  Acero gris cálido, mate, homogéneo. Referencia de chapa industrial.

### 2.2 Fórmulas y Hex Calculados

| Rol | Fórmula (sobre HSL L) | Hex | RGB | Uso |
|-----|-----------------------|-----|-----|-----|
| **Steel Highlight** | `B +11.0% L` | `#ACB0B4` | 172,176,180 | Bisel exterior panel: borde superior + izquierdo 1 px |
| **Steel Shadow** | `B -12.5% L` | `#707378` | 112,115,120 | Bisel exterior panel: borde inferior + derecho 1 px |
| **Steel Outer Border** | `B -25.3% L` | `#4E5054` | 78,80,84 | Borde exterior 1 px más oscuro (reemplaza negro puro) |
| **Slot Base** | `B -18.8% L → tuneado a -17%` | `#606368` teórica / **`#63666B` implementada** | 99,102,107 | Fondo interior 16×16 del slot. Tuneada +2 para contraste carbón ≥3:1 (2.94→3.08) |
| **Slot Shadow** | `SlotBase -12.5% L` | `#404348` | 64,67,72 | Borde superior + izquierdo del slot 1 px |
| **Slot Highlight** | `SlotBase +15.7% L` | `#888B90` | 136,139,144 | Borde inferior + derecho del slot 1 px |
| **Bronze Accent** | `hsv(42°,55%,78%)` | `#C6A659` | 198,166,89 | Flecha crafting, llama horno, tubos brewing, ejes beacon |
| **Bronze Dark** | `Bronze -15% V` | `#A68A4A` | 166,138,74 | Sombra interior bronze |
| **Bronze Light** | `Bronze +12% V` | `#D4B673` | 212,182,115 | Highlight bronze |
| **Obsidian Chassis** | fija | `#2D2A3E` | 45,42,62 | Solo `enchanting_table.png` — chasis libro |
| **Silver Lunar** | fija | `#C8CEE0` | 200,206,224 | Solo enchanting — botones encantamiento |
| **Texto vanilla** | — | `#404040` / `#3F3F3F` | — | Labels “Inventory”, “Crafting” — contraste 3.36:1 vs steel |
| **Error Rojo Anvil** | fija | `#8B2C1F` | 139,44,31 | Mutado desde #FF0000, mantiene affordance error |

**Tabla de contraste clave (WCAG):**

- `#404040` vs `#909398` = **3.36:1** ✔
- `#3F3F3F` vs `#909398` = 3.42:1 ✔
- `#FFFFFF` vs `#909398` = 3.08:1 ✔
- Carbón `#181818` vs Slot `#606368` = 2.94:1 (tuneado `#63666B` = **3.08:1** ✔)
- Hierro `#D8D8D8` vs Slot = 4.23:1 ✔
- Diamante `#5CDBD3` vs Slot = 3.60:1 ✔
- Cuarzo `#E8E8E8` vs Slot = 4.92:1 ✔

> Nota: El valor **B = #909398** es uno de los infinitos posibles que cumplen la identidad. Si se elige B diferente (ej. #8E9196), recalcular todos con las fórmulas de la tabla y mantener deltas L idénticos.

### 2.3 Micrograno

Opcional y **imperceptible**: ruido uniforme ≤8 alpha, sin dithering, solo para evitar banding en degradados. No usar rayas ni fibras.

---

## 3. Slot Maestro (18×18)

**Archivo:** `assets/minecraft/textures/gui/container/slot_master.png` (también exportado a `docs/`)

```
┌──────────────────┐
│#################s│  y=0: 17× Shadow #404348 + 1× Base #63666B (top-right corner)
│#················+│  y=1..16: 1× Shadow left, 16× Base interior, 1× Highlight right (#888B90)
│#················+│
│#················+│  x=0 shadow, x=17 highlight, esquinas diagonales base/highlight
│#················+│
│#················+│  Fondo interior #63666B (o #606368 teórico) — uniforme, sin ruido
│#················+│
│s+++++++++++++++++│  y=17: 1× Base bottom-left + 17× Highlight #888B90
└──────────────────┘
  18×18, pozo 16×16, relieve físico 1 px
```

**Reglas:**

- Sin ruido, sin dithering, fondo uniforme.
- Borde superior/izquierdo: `#404348` 1 px.
- Borde inferior/derecho: `#888B90` 1 px.
- Esquinas: (0,0) shadow, (17,0) base, (0,17) base, (17,17) highlight — diagonal victoriana sutil.
- El slot debe pasar pruebas con carbón, netherita, hierro, diamante y cuarzo antes de aprobarse.

**Validación (incluida en `verify_pack.py`):**

- Slot vacío: sin banding.
- Con carbón #181818: ≥3:1 (tuneado a 3.08).
- Con netherita/iron/diamond/quartz: nítidos.
- Con herramienta/armadura renderizada: bordes no compiten.

---

## 4. Panel Maestro

**Plantilla:** Panel vacío (ej. cajón de inventario) 176×166 sobre textura 256×256.

- **Base:** `#909398` uniforme.
- **Bisel exterior 1 px:**  
  - Arriba + izquierda: `#ACB0B4` (highlight)  
  - Abajo + derecha: `#707378` (shadow)  
  - Borde exterior más oscuro: `#4E5054` 1 px (reemplaza negro puro, mantiene alfa de esquinas transparentes vanilla 1:1)
- **Transparencia:** Esquinas superiores (0,0), (1,0), (0,1) transparentes como vanilla; se copia máscara alfa bit a bit.

```
  XXXXXXXXXXXXX  (outer #4E5054)
 X+++++++++++++X (inner highlight #ACB0B4 top/left)
X+.............+X (base #909398)
X+.............xX (shadow #707378 bottom/right, x)
X+.............xX
 XxxxxxxxxxxxxxX
  XXXXXXXXXXXXX  (outer)
```

Vacío, sin decoración perimetral, sin remaches. Solo bronce donde sea funcional.

---

## 5. Elementos Maestros Adicionales

| Elemento | Geometría vanilla | Maestro Steel |
|----------|-------------------|---------------|
| **Flecha crafting / horno** | Silueta 22×15 de #8B8B8B sobre #C6C6C6, en (89,35) crafting y (79,34) furnace. | Silueta recoloreada a **Bronze #C6A659** sobre fondo steel. Borde si existe: Bronze Dark #A68A4A arriba/izq, Bronze Light #D4B673 abajo/der. |
| **Llama horno** | 14×14 en (56,36), forma vertical flame hecha de s-píxeles. | Misma forma en Bronze, con 1 px highlight para sensación de brasa. |
| **Botones Beacon** | 5×3 grid de 22×22 (?) + cancel/confirm. | Base steel, bisel óptico 1 px Bronze Light en botón seleccionado. |
| **Tubos Brewing** | Líneas finas 1–2 px entre slots. | Latón dorado #D4B673 1 px, sin relleno. |
| **Barras experiencia Villager** | 10–20 px horizontales. | Bronze #C6A659 con fondo Slot Base. |
| **Scollers (loom/stonecutter/villager)** | 12×15 vertical. | Steel base con thumb Bronze #C6A659. |
| **Enchanting** | Chasis 176×166, libro central. | Chasis interior Obsidian #2D2A3E; 3 botones encantamiento en Silver Lunar #C8CEE0 con sombra tinta. |

> Si dudas, **omite adornos**. Un par de líneas en latón para flecha es suficiente. No añadir rivets salvo 1 discreto en yunque si el test lo pide.

---

## 6. Filosofía de Implementación

1. **Detectar slots** vía interior 16×16 #8B8B8B → reconstruir borde con patrón diagonal exacto.
2. **Panel:** copiar alfa vanilla, mapear colores: #C6C6C6→#909398, #FFFFFF(panel)→#ACB0B4, #555555→#707378, #000000→#4E5054.
3. **Componentes funcionales no-slot** (arrow 22×15): #8B8B8B→#C6A659.
4. **Casos especiales:** enchanting beige→obsidian, beige oscuro→steel shadow, rojo→muted red.
5. **Preservar dimensiones y alfa:** 256×256 (512×256 villager) exactas.

---

## 7. Ejemplo de Uso en GUI 176×166

Ver `master_gui_template.png` (docs/): inventario demo con panel + 46 slots + flecha bronze + labels “Crafting”/“Inventory” en #404040 con sombra 1 px #909398@40%.

---

*Fin de guía. Todo PNG fuera de esta paleta será rechazado por `verifyGripeVerdeThemeAssets`.*
