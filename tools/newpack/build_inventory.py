"""
Muestra del pack nuevo: inventory.png
Estilo "fresadora" v1 — gris azulado neutro, monocromo, sin remaches.

Preserva verbatim:
  - canal alfa (por construccion)
  - figura del jugador (silueta + contorno, region derivada de vanilla)
  - boton del libro de recetas y tab teal (alfa parcial 0xF0, intactos)
  - grises de figura/iconos (#555555, #B7B7B7, #999999, #D6D6D6, #5B5B5B)

Repinta en la MISMA posicion:
  - panel #C6C6C6 -> base fresadora + grano vertical sutil
  - slots #8B8B8B / offhand #505050 -> rehundido SI + bevel
  - estructura #373737/#212121/#000000/#FFFFFF -> rampa de la paleta
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from kit import Canvas


def figure_bbox(c):
    """Bbox de la silueta del jugador: pixeles negros del panel (excluye
    borde exterior del panel y zonas de slots), expandido 2px para incluir
    el contorno blanco/gris."""
    xs, ys = [], []
    # La silueta vive en x=25..87, y=7..78; el rango evita los bordes
    # negros del panel (x=0..1, x=173..197) y las filas del grid (y>=83).
    for y in range(4, 90):
        for x in range(20, 121):
            p = c.van_px[x, y]
            if p[3] == 255 and (p[0], p[1], p[2]) == (0x00, 0x00, 0x00):
                xs.append(x)
                ys.append(y)
    if not xs:
        return None
    return (min(xs) - 2, min(ys) - 2, max(xs) + 2, max(ys) + 2)


def build_inventory(seed=4101):
    c = Canvas('inventory.png', seed=seed)
    # 1) repintado base (mismas coordenadas vanilla, solo alfa 255)
    c.repaint()
    # 2) grano de maquina sobre el panel
    c.grain(strength=4)
    # 3) bevel de slots (sombra top/left, luz bottom/right)
    c.slot_bevel()
    # 4) figura del jugador verbatim (derivada de la vanilla)
    fb = figure_bbox(c)
    if fb is None:
        raise SystemExit('ERROR: no se encontro la silueta del jugador')
    print(f'figura bbox: {fb}')
    c.preserve(*fb)
    # 5) guardar
    out = c.save()
    print(f'OK -> {out}')
    return out


if __name__ == '__main__':
    build_inventory()
