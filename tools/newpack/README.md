# tools/newpack — Pipeline del pack nuevo ("estilo fresadora")

Genera las texturas de contenedores con gris azulado neutro, monocromo,
sin remaches, preservando EXACTO de la vanilla 1.20.1:
  - canal alfa (por construccion: solo se repinta alfa == 255)
  - posiciones de slots (derivadas de vanilla, nunca hardcodeadas)
  - iconos funcionales verbatim (boton de recetas, tabs, figura del jugador)

## Estructura
- `vanilla/` — las 20 texturas vanilla 1.20.1 (base de construccion; mirror
  de InventivetalentDev/minecraft-assets, rama 1.20.1)
- `kit.py` — nucleo: paleta, repintado por clase, grano, bevel de slots,
  regiones verbatim
- `build_inventory.py` — construye la muestra inventory.png
- `verify.py` — auditoria: alfa identico, alfa parcial verbatim, regiones
  verbatim, slots en mismas coords, panel sin residual vanilla, sat <= 60
- `make_comparison.py` — montaje visual vanilla vs nuevo
- `export/` — salidas generadas (inventory.png, comparativa, zip de prueba)

## Uso
    python3 build_inventory.py        # genera export/inventory.png
    python3 verify.py "(24,6,76,79)" export/inventory.png
    python3 make_comparison.py        # montaje visual

VANILLA externa: NC_VANILLA=/ruta python3 build_*.py
Salida alterna:  NC_OUT=/ruta python3 build_*.py

## Estado
- Muestra inventory.png generada y auditada (OK). Estilo v1 pendiente de
  aprobacion del usuario (prueba in-game via ZIP en resourcepacks).
- Las 20 texturas steel actuales de src/ NO se tocan hasta aprobar.
