"""Batch 4: beacon, loom, cartography_table, horse, shulker_box, villager2."""
import os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from steelkit import SteelCanvas, P, ACCENT

def player_cells(y0=84, hotbar_y=142):
    return [(8+18*c, y0+18*r) for r in range(3) for c in range(9)] + [(8+18*c, hotbar_y) for c in range(9)]

def corners(c, y_bottom=157):
    for (rx, ry) in [(7, 7), (168, 7), (7, y_bottom), (168, y_bottom)]:
        c.rivet(rx, ry, 3)

# ---------- BALIZA ----------
def build_beacon():
    c = SteelCanvas('beacon.png', seed=6666, accent=ACCENT['beacon'])
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    # tira de sprites (botones + check) verbatim
    c.preserve(176, 0, 201, 48)
    # ranura central + grid de jugador (9 columnas, 4 filas)
    cells = [(136,107)] + [(36+18*c, 137+18*r) for r in range(3) for c in range(9)] + \
            [(36+18*c, 195) for c in range(9)]
    c.recess_slots(cells)
    # rombo grabado sutil (arriba, zona libre)
    dx, dy = 118, 5
    c.etch([(dx+7, dy), (dx+14, dy+7), (dx+7, dy+14), (dx, dy+7), (dx+7, dy)])
    # acento dorado palido en el rombo
    c.put(dx+7, dy+7, ACCENT['beacon'])
    c.put(dx+7, dy+6, ACCENT['beacon'])
    c.seam(4, 172, 132)
    corners(c, y_bottom=213)
    c.apply_structure()
    c.save()

# ---------- TELAR ----------
def build_loom():
    c = SteelCanvas('loom.png', seed=7777)
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    # iconos de patron y lanzadera vanilla verbatim
    c.preserve(60, 14, 116, 56)
    c.preserve(141, 10, 159, 51)
    # pozo de vista previa del estandarte + celdas
    c.recess_rect(8, 15, 51, 63)
    c.recess_slots([(118,13),(133,53),(21,45)] + player_cells())
    # hilos de urdimbre grabados (zona superior)
    for wx in (64, 72, 80, 88, 96):
        c.etch([(wx, 8), (wx, 13)])
    corners(c)
    c.apply_structure()
    c.save()

# ---------- MESA DE CARTOGRAFIA ----------
def build_cartography():
    c = SteelCanvas('cartography_table.png', seed=8888)
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    # celdas de entrada (mapa y papel) + pozo de salida
    c.recess_slots([(15,15),(15,52)] + player_cells())
    c.recess_rect(141, 35, 164, 50)
    # rosa de los vientos grabada (arriba derecha)
    rx, ry = 118, 6
    c.etch([(rx+5, ry), (rx+5, ry+2), (rx+5, ry+8), (rx+7, ry+10),
            (rx+5, ry+12), (rx+5, ry+8), (rx+3, ry+10), (rx+5, ry+8)])
    c.line(rx+5, ry+2, rx+9, ry+6, P['S1']); c.line(rx+9, ry+6, rx+5, ry+10, P['S1'])
    c.line(rx+1, ry+6, rx+5, ry+2, P['S1']); c.line(rx+5, ry+2, rx+5, ry+8, P['S1'])
    corners(c)
    c.apply_structure()
    c.save()

# ---------- CABALLERIZA ----------
def build_horse():
    c = SteelCanvas('horse.png', seed=9999)
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    # bahia de 15 ranuras rehundida (panel claro -> incluye clase H)
    c.recess_rect(80, 18, 166, 69)
    # herradura grabada (arriba, zona libre)
    hx, hy = 118, 5
    c.etch([(hx+3, hy), (hx+11, hy)])
    c.line(hx+3, hy, hx+3, hy+10, P['S1']); c.line(hx+4, hy, hx+4, hy+10, P['L3'])
    c.line(hx+11, hy, hx+11, hy+10, P['S1']); c.line(hx+12, hy, hx+12, hy+10, P['L3'])
    c.etch([(hx+3, hy+10), (hx+11, hy+10)])
    for (nx, ny) in [(hx+4, hy+1), (hx+7, hy+1), (hx+10, hy+1), (hx+4, hy+9), (hx+10, hy+9)]:
        c.put(nx, ny, P['L3'])
    # remaches en esquinas del panel
    for (rx, ry) in [(7, 7), (168, 7), (7, 157), (168, 157)]:
        c.rivet(rx, ry, 3)
    c.apply_structure()
    c.save()

# ---------- SHULKER ----------
def build_shulker():
    c = SteelCanvas('shulker_box.png', seed=1010)
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    cells = [(8+18*c, 17+18*r) for r in range(3) for c in range(9)] + player_cells()
    c.recess_slots(cells)
    # costuras diagonales de "caparazon" en las esquinas
    for (ax, ay, bx, by) in [(8, 4, 14, 10), (162, 4, 168, 10),
                             (8, 150, 14, 156), (162, 150, 168, 156)]:
        c.line(ax, ay, bx, by, P['S1'])
        c.line(ax+1, ay+1, bx+1, by+1, P['L3'])
    # diamante central grabado
    dx, dy = 84, 6
    c.etch([(dx+5, dy), (dx+10, dy+5), (dx+5, dy+10), (dx, dy+5), (dx+5, dy)])
    for (rx, ry) in [(7, 7), (168, 7), (7, 157), (168, 157)]:
        c.rivet(rx, ry, 3)
    c.apply_structure()
    c.save()

# ---------- ALDEANO ----------
def build_villager():
    c = SteelCanvas('villager2.png', seed=1212, accent=ACCENT['villager'])
    c.brushed(0, 0, 511, 255, base_key='B', vertical=True, strength=7)
    # celdas de intercambio: entradas, salida (pozo grande) + grid del jugador
    c.recess_slots([(137,38),(163,38)] + [(108+18*c, 84+18*r) for r in range(3) for c in range(9)] + \
                   [(108+18*c, 142) for c in range(9)])
    c.recess_rect(221, 34, 245, 58)
    # moneda grabada (esquina superior del listado)
    cx, cy = 86, 7
    c.etch([(cx+3, cy), (cx+9, cy), (cx+9, cy+3), (cx+3, cy+3), (cx+3, cy)])
    c.put(cx+6, cy+1, ACCENT['villager'])
    # remaches: panel izquierdo (visible) + derecho
    for (rx, ry) in [(7, 7), (249, 7), (7, 217), (249, 217)]:
        c.rivet(rx, ry, 3)
    for (rx, ry) in [(261, 7), (505, 7), (261, 217), (505, 217)]:
        c.rivet(rx, ry, 3)
    c.apply_structure()
    c.save()

build_beacon()
build_loom()
build_cartography()
build_horse()
build_shulker()
build_villager()
print("batch 4 done")
