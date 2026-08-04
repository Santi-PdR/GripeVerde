# tools/silver_steel — Generador del resource pack "Silver Steel"

Scripts Python con los que se reconstruyo el resource pack de contenedores
(`src/main/resources/resourcepacks/gripeverde_theme/`).

## Requisitos
- Python 3.10+
- Pillow: `pip install pillow`

## Uso (reproducir / regenerar las 20 texturas)

```bash
# 1) Descargar las texturas vanilla base de Minecraft 1.20.1
#    (rama 1.20.1 del repo https://github.com/InventivetalentDev/minecraft-assets)
#    y extraer en /tmp/minecraft-assets-1.20.1 (o indicar otra ruta abajo).

# 2) Generar los 4 lotes (vuelca los PNG en la carpeta de salida)
python3 build1.py   # inventory, crafting_table, generic_54
python3 build2.py   # furnace, smoker, blast_furnace, brewing_stand, dispenser, hopper
python3 build3.py   # enchanting_table, anvil, grindstone, smithing, stonecutter
python3 build4.py   # beacon, loom, cartography_table, horse, shulker_box, villager2

# 3) Verificar contra vanilla (alfa, iconos, estructura, paleta)
python3 verify.py

# 4) Copiar a out/ al resource pack del repo
cp out/*.png ../../src/main/resources/resourcepacks/gripeverde_theme/assets/minecraft/textures/gui/container/
```

Rutas configurables por variables de entorno (fallback a /tmp):
- `STEEL_VANILLA` = dir de las texturas vanilla (default `/tmp/minecraft-assets-1.20.1/assets/minecraft/textures/gui/container`)
- `STEEL_OUT` = dir de salida (default `/tmp/steel/out`)

## Qué hace cada archivo
- `steelkit.py` — nucleo: paleta Silver Steel, lienzo que parte de vanilla,
  grano cepillado, remaches/tornillos/juntas, rehundido de slots,
  recolor de estructura en las MISMAS coordenadas vanilla, preservacion
  verbatim de iconos (diferida al save) y de alfa parcial.
- `build1..4.py` — diseno de cada contenedor (detalles unicos por bloque).
- `verify.py` — auditoria pixel a pixel vs vanilla + paleta.
- `cells_report.txt` — escaneo de celdas/slots de todas las texturas vanilla
  (output de utilidades de analisis; util para consultar coordenadas).

## Notas
- Los PNG resultantes son DETERMINISTICOS (semillas fijas por textura).
- No modificar layouts: la regla es que alfa, dimensiones, iconos y
  geometria de slots queden identicos a vanilla 1.20.1 (verify.py lo exige).
