"""Batch 3: enchanting_table, anvil, grindstone, smithing, stonecutter."""
import os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from steelkit import SteelCanvas, P, ACCENT

def player_cells(y0=84, hotbar_y=142):
    return [(8+18*c, y0+18*r) for r in range(3) for c in range(9)] + [(8+18*c, hotbar_y) for c in range(9)]

def corners(c, y_bottom=157):
    for (rx, ry) in [(7, 7), (168, 7), (7, y_bottom), (168, y_bottom)]:
        c.rivet(rx, ry, 3)

# ---------- MESA DE ENCANTAMIENTOS ----------
def build_enchanting():
    c = SteelCanvas('enchanting_table.png', seed=1111, accent=ACCENT['enchanting'])
    # el librero tan es PANEL (no icono): familia de colores -> clases estructurales
    c.panel_families = [((160,145,114), 'l', 22), ((224,202,159), 'H', 22),
                        ((84,76,59), 'D', 20)]
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    # estanteria con tinte amatista (sutil): repintar el area del librero
    for y in range(14, 72):
        for x in range(78, 168):
            cls = c.classify(c.van.getpixel((x, y)))
            if cls == 'l':
                c.px[x, y] = (140, 146, 164, 255)
            elif cls == 'H':
                c.px[x, y] = (198, 201, 213, 255)
    # destellos de amatista en la estanteria (1px, muy sutil)
    for (gx, gy) in [(92, 22), (128, 40), (150, 58), (104, 62)]:
        c.put(gx, gy, (150, 140, 185))
        c.put(gx+1, gy+1, (196, 190, 220))
    # libro vanilla verbatim
    c.preserve(58, 13, 78, 71)
    # celdas: 2 ranuras + grid + hotbar
    cells = [(15,47),(35,47)] + player_cells()
    c.recess_slots(cells)
    c.seam(4, 172, 78)
    corners(c)
    c.apply_structure()
    c.save()

# ---------- YUNQUE ----------
def build_anvil():
    c = SteelCanvas('anvil.png', seed=2222)
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    # contenido funcional/iconico vanilla verbatim:
    # grieta roja de dano, espada diagonal decorativa, zona tan inferior, chispas
    c.preserve(59, 20, 168, 35)
    c.preserve(16, 10, 43, 37)
    c.preserve(1, 167, 108, 196)
    c.preserve(182, 3, 196, 17)
    # remaches pesados alrededor de la zona de trabajo
    for (rx, ry) in [(10, 12), (50, 12), (140, 12), (168, 12),
                     (10, 42), (50, 42), (140, 42), (168, 42)]:
        c.rivet(rx, ry, 3)
    # tornillos junto a las ranuras
    for (sx, sy) in [(20, 68), (120, 68)]:
        c.screw(sx, sy)
    c.seam(4, 172, 15)
    cells = [(27,47),(76,47),(134,47)] + player_cells()
    c.recess_slots(cells)
    corners(c)
    c.apply_structure()
    c.save()

# ---------- AFILADORA ----------
def build_grindstone():
    c = SteelCanvas('grindstone.png', seed=3333)
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    # icono de la afiladora vanilla verbatim (incluye marco de madera)
    c.preserve(54, 14, 113, 56)
    cells = [(50,19),(50,40),(130,34)] + player_cells()
    c.recess_slots(cells)
    c.seam(4, 172, 15)
    corners(c)
    c.apply_structure()
    c.save()

# ---------- MESA DE HERRERIA ----------
def build_smithing():
    c = SteelCanvas('smithing.png', seed=4444)
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    # icono de espada/plantilla vanilla verbatim
    c.preserve(10, 6, 40, 38)
    # diamante grabado (derecha del icono)
    dx, dy = 118, 18
    c.etch([(dx+8, dy), (dx+16, dy+8), (dx+8, dy+16), (dx, dy+8), (dx+8, dy)])
    c.put(dx+8, dy+8, P['L3'])
    cells = [(8,48),(26,48),(44,48),(98,48)] + player_cells()
    c.recess_slots(cells)
    for (sx, sy) in [(12, 40), (108, 40)]:
        c.screw(sx, sy)
    corners(c)
    c.apply_structure()
    c.save()

# ---------- CORTAPIEDRA ----------
def build_stonecutter():
    c = SteelCanvas('stonecutter.png', seed=5555)
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    # iconos de recetas vanilla verbatim
    c.preserve(52, 13, 118, 69)
    # celda de entrada + pozo de salida
    c.recess_slots([(20,33)] + player_cells())
    c.recess_rect(139, 29, 162, 52)
    # hoja de sierra grabada (semicirculo con dientes)
    bx, by = 120, 6
    c.etch([(bx, by+8), (bx+2, by+6), (bx+4, by+8), (bx+6, by+5), (bx+8, by+8),
            (bx+10, by+6), (bx+12, by+8), (bx+14, by+7), (bx+15, by+8)])
    c.arc = None
    corners(c)
    c.apply_structure()
    c.save()

build_enchanting()
build_anvil()
build_grindstone()
build_smithing()
build_stonecutter()
print("batch 3 done")
