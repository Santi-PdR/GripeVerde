"""Batch 2: furnace, smoker, blast_furnace, brewing_stand, dispenser, hopper."""
import os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from steelkit import SteelCanvas, P, ACCENT

def player_cells(x0=8, y0=84, hotbar_y=142):
    return [(x0+18*c, y0+18*r) for r in range(3) for c in range(9)] + [(x0+18*c, hotbar_y) for c in range(9)]

def common(c):
    for (rx, ry) in [(7, 7), (168, 7), (7, 157), (168, 157)]:
        c.rivet(rx, ry, 3)

# ---------- HORNO ----------
def build_furnace():
    c = SteelCanvas('furnace.png', seed=4404, accent=ACCENT['furnace'])
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    # caja de fuego rehundida detras de la llama (56,36)-(77,59)
    c.recess_rect(56, 36, 77, 59)
    # ascuas apagadas en la base de la caja (visibles alrededor de la llama)
    for x in range(58, 76, 2):
        c.put(x, 57, ACCENT['furnace'])
    c.put(60, 55, ACCENT['furnace']); c.put(72, 55, ACCENT['furnace'])
    # rejilla de ventilacion vertical a la izquierda
    for vx in (14, 21, 28):
        for vy in range(38, 46):
            c.put(vx, vy, P['S1'])
            c.put(vx+1, vy, P['M1'])
        c.put(vx, 46, P['L3']); c.put(vx+1, 46, P['L3'])
    # riel rehundido para la flecha de progreso (79,34)-(103,50)
    c.recess_rect(79, 34, 103, 50)
    # costura bajo el titulo
    c.seam(4, 172, 15)
    c.recess_slots([(56,17),(56,53),(116,35)] + player_cells())
    common(c)
    c.preserve(176, 0, 201, 32)   # llama + flecha vanilla verbatim
    c.apply_structure()
    c.save()

# ---------- AHUMADOR ----------
def build_smoker():
    c = SteelCanvas('smoker.png', seed=5505, accent=ACCENT['smoker'])
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    c.recess_rect(56, 36, 77, 59)
    for x in range(58, 76, 2):
        c.put(x, 57, ACCENT['smoker'])
    # persianas horizontales de extraccion (lado izquierdo)
    for i, sy in enumerate((22, 27, 32)):
        c.hline(12, 40, sy, P['S1'])
        c.hline(13, 39, sy+1, P['M1'])
        c.hline(12, 40, sy+2, P['L3']) if i == 2 else None
    # linea de chimenea: costura vertical corta sobre la caja
    c.vseam(66, 18, 34)
    c.recess_rect(79, 34, 103, 50)
    c.seam(4, 172, 15)
    c.recess_slots([(56,17),(56,53),(116,35)] + player_cells())
    common(c)
    c.preserve(176, 0, 201, 32)
    c.apply_structure()
    c.save()

# ---------- ALTO HORNO ----------
def build_blast():
    c = SteelCanvas('blast_furnace.png', seed=6606, accent=ACCENT['blast'])
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    c.recess_rect(55, 35, 78, 60)
    for x in range(57, 77, 2):
        c.put(x, 58, ACCENT['blast'])
    # rejillas en cheuron (industrial)
    for i, vy in enumerate((36, 42, 48)):
        cx0 = 12 + i*7
        c.line(cx0, vy+6, cx0+6, vy, P['S1'])
        c.line(cx0+6, vy, cx0+12, vy+6, P['S1'])
    # pernos grandes
    for (bx, by) in [(10, 20), (10, 60), (44, 20), (44, 60)]:
        c.screw(bx, by)
    c.recess_rect(79, 34, 103, 50)
    c.seam(4, 172, 15)
    c.recess_slots([(56,17),(56,53),(116,35)] + player_cells())
    common(c)
    c.preserve(176, 0, 201, 32)
    c.apply_structure()
    c.save()

# ---------- SOPORTE DE POCIONES ----------
def build_brewing():
    c = SteelCanvas('brewing_stand.png', seed=7707, accent=ACCENT['brewing'])
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    c.recess_slots([(17,17),(79,17),(56,51),(102,51),(79,58)] + player_cells())
    # frasco alquimico grabado (lado derecho, zona libre)
    fx, fy = 138, 24
    c.etch([(fx, fy), (fx+8, fy), (fx+8, fy+2), (fx+6, fy+8), (fx+6, fy+12),
            (fx+2, fy+12), (fx+2, fy+8), (fx, fy+2), (fx, fy)])
    c.put(fx+4, fy+6, ACCENT['brewing'])   # liquido teal apagado
    c.put(fx+4, fy+7, ACCENT['brewing'])
    # burbuja pequena grabada junto al frasco
    c.etch([(fx+11, fy+1), (fx+12, fy+2), (fx+11, fy+3), (fx+10, fy+2), (fx+11, fy+1)])
    c.seam(4, 172, 15)
    common(c)
    c.preserve(176, 0, 201, 70)   # burbujas + flecha verbatim
    c.apply_structure()
    c.save()

# ---------- DISPENSADOR ----------
def build_dispenser():
    c = SteelCanvas('dispenser.png', seed=8808)
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    # tobera del dispensador: cuadrado grabado con orificio
    nx, ny = 97, 8
    c.etch([(nx, ny), (nx+6, ny), (nx+6, ny+6), (nx, ny+6), (nx, ny)])
    c.rect(nx+2, ny+2, nx+4, ny+4, P['S2'])
    c.put(nx+3, ny+3, P['S1'])
    # remaches en las esquinas del panel de la rejilla
    for (rx, ry) in [(30, 8), (130, 8), (30, 72), (130, 72)]:
        c.rivet(rx, ry, 3)
    cells = [(62+18*(c%3), 17+18*(c//3)) for c in range(9)] + player_cells()
    c.recess_slots(cells)
    c.seam(4, 172, 15)
    common(c)
    c.apply_structure()
    c.save()

# ---------- TOLVA ----------
def build_hopper():
    c = SteelCanvas('hopper.png', seed=9909)
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    # embudo grabado centrado arriba
    fx0, fy0 = 80, 7
    c.etch([(fx0, fy0), (fx0+8, fy0), (fx0+8, fy0+2), (fx0+4, fy0+7), (fx0+4, fy0+9),
            (fx0+3, fy0+9), (fx0+3, fy0+7), (fx0, fy0+2), (fx0, fy0)])
    # nervaduras diagonales a los lados de la fila de slots
    for i in range(3):
        c.line(32+i*3, 24+i*3, 40+i*3, 16+i*3, P['S1'])
        c.line(116+i*3, 16+i*3, 124+i*3, 24+i*3, P['S1'])
    cells = [(44+18*c, 20) for c in range(5)] + \
            [(8+18*c, 51+18*r) for r in range(3) for c in range(9)] + \
            [(8+18*c, 109) for c in range(9)]
    c.recess_slots(cells)
    c.seam(4, 172, 15)
    for (rx, ry) in [(7, 7), (168, 7), (7, 126), (168, 126)]:
        c.rivet(rx, ry, 3)
    c.apply_structure()
    c.save()

build_furnace()
build_smoker()
build_blast()
build_brewing()
build_dispenser()
build_hopper()
print("batch 2 done")
