# Pipeline del resource pack "gripeverde_steel" (Acero de Cuarentena)

Este directorio contiene el generador del resource pack built-in de
contenedores de Gripe Verde. El pack se genera desde la geometria vanilla
y NUNCA cambia: dimensiones, alpha (transparencia), posiciones de slots
18x18 ni proporciones. Solo cambia el MATERIAL: acero frio cepillado
(#9A9EA8), slots hundidos, juntas con remaches de laton, cinta verde de
cuarentena y tintes por familia de contenedor.

## Requisitos

- Python 3 + Pillow.
- Espejo de assets vanilla 1.20.2 con `gui/container/` y `gui/sprites/`
  (identico a 1.20.1 en los contenedores; verificado contra el pack
  Master Steel validado en juego).
- Espejo de assets vanilla 1.19.1 con `gui/container/creative_inventory/`
  (tabs.png, tab_inventory.png, tab_items.png, tab_item_search.png —
  geometria identica a 1.20.1 para el inventario creativo).

Los espejos se obtienen de repositorios publicos que versionan los assets
de Minecraft, p.ej. `PrismarineJS/minecraft-assets` (ramas/data por
version). Extraer el directorio `gui` de cada version.

## Uso

```bash
python3 tools/steel_pack/build_steel_pack.py \
    --vanilla /ruta/a/1.20.2/gui \
    --vanilla119 /ruta/a/1.19.1/gui \
    --out src/main/resources/resourcepacks/gripeverde_steel
```

El script imprime, por textura, la cantidad de slots y el hash de alpha
de la salida; regenera `pack.mcmeta`, `pack.png` (128x128) y todo el
arbol `assets/minecraft/textures/gui/...`.

## Validacion

- La task Gradle `verifyGripeVerdeThemeAssets` (hookeada en
  `processResources`) verifica: existencia y dimensiones de los PNG,
  `pack_format` 15, `pack.png` 128x128, presencia de los `.mcmeta` de
  9-slices, y ausencia de texturas huerfanas (`dropper.png`,
  `villager2.png` — no existen en 1.20.1).
- La geometria (alpha 1:1 y slots en posiciones vanilla) se verifico
  programaticamente durante la generacion de v0.17.0; los hashes de alpha
  quedan impresos por el script para auditoria.

## Reglas del sistema visual

- Paleta unica: acero #9A9EA8 (claro/medio/oscuro), sin marron dominante,
  sin bronce excesivo, sin azul saturado, sin negro absoluto.
- Cepillado horizontal controlado (patron de seno + rayas ocasionales),
  no ruido aleatorio.
- Slots 18x18 hundidos (pozo oscuro con labio inferior claro), siempre en
  las coordenadas vanilla.
- Juntas (seam stubs) solo en los bordes del panel, sin cruzar slots.
- Cinta verde de cuarentena solo en almacenes (inventory, generic_54,
  shulker_box); remaches de esquina solo en el almacen grande.
- Iconos (flechas, llamas, figuras) conservan su forma vanilla via remap
  de luminancia a acero grabado.
