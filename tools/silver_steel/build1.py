"""Batch 1: inventory, crafting_table, generic_54 — acero plateado base."""
import os, sys, os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from steelkit import SteelCanvas, P

def build_inventory():
    c = SteelCanvas('inventory.png', seed=1101)
    # 1) grano cepillado vertical sobre todo el panel
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    # 2) costuras de placa
    #    - costura horizontal entre zona de crafting y grid del jugador
    c.seam(3, 173, 79)
    #    - costura vertical entre columna de armadura y zona de crafting
    c.vseam(88, 5, 76)
    # 3) remaches en esquinas del panel (panel ~ x=2..174, y=2..163)
    for (rx, ry) in [(7, 7), (168, 7), (7, 157), (168, 157)]:
        c.rivet(rx, ry, 3)
    # remaches sobre la costura vertical
    c.rivet(88, 14, 3); c.rivet(88, 68, 3)
    # 3b) interiores de slot rehundidos (posiciones vanilla exactas)
    cells = [(8+18*c, 8+18*r) for r in range(4) for c in range(1)] +             [(98+18*(c%2), 18+18*(c//2)) for c in range(4)] +             [(8+18*c, 84+18*r) for r in range(3) for c in range(9)] +             [(8+18*c, 142) for c in range(9)]
    c.recess_slots(cells)
    # 4) estructura vanilla recoloreada (slots, marcos) en posiciones exactas
    c.apply_structure()
    c.save()

def build_crafting():
    c = SteelCanvas('crafting_table.png', seed=2202)
    # grano HORIZONTAL (identidad: banco de trabajo)
    c.brushed(0, 0, 255, 255, base_key='B', vertical=False, strength=7)
    # esquinero L grabado alrededor de la cuadricula 3x3 (x=30..83, y=17..70)
    # esquina sup-izq
    c.etch([(27, 13), (36, 13)])
    c.etch([(27, 13), (27, 22)])
    # esquina sup-der
    c.etch([(77, 13), (86, 13)])
    c.etch([(86, 13), (86, 22)])
    # esquina inf-izq
    c.etch([(27, 75), (36, 75)])
    c.etch([(27, 75), (27, 66)])
    # esquina inf-der
    c.etch([(77, 75), (86, 75)])
    c.etch([(86, 75), (86, 66)])
    # regla grabada sobre el borde superior (ticks cada 4px)
    for i in range(8):
        tx = 42 + i * 4
        c.etch([(tx, 10), (tx, 12)])
    # remaches en las 4 esquinas del panel
    for (rx, ry) in [(7, 7), (168, 7), (7, 157), (168, 157)]:
        c.rivet(rx, ry, 3)
    # 3b) interiores de slot rehundidos (3x3 + salida + grid + hotbar)
    cells = [(30+18*(c%3), 17+18*(c//3)) for c in range(9)] + [(124,35)] + \
            [(8+18*c, 84+18*r) for r in range(3) for c in range(9)] + \
            [(8+18*c, 142) for c in range(9)]
    c.recess_slots(cells)
    c.apply_structure()
    c.save()

def build_generic54():
    c = SteelCanvas('generic_54.png', seed=3303)
    c.brushed(0, 0, 255, 255, base_key='B', vertical=True, strength=7)
    # cofre/camara: linea de etiqueta bajo el titulo + costuras entre secciones
    c.seam(4, 172, 15)          # bajo el titulo (el texto va en y=6..13)
    c.seam(4, 172, 127)         # entre caja principal y grid del jugador
    c.seam(4, 172, 199)         # entre grid y hotbar
    # remaches en esquinas + en los extremos de las costuras
    for (rx, ry) in [(7, 7), (168, 7), (7, 215), (168, 215)]:
        c.rivet(rx, ry, 3)
    c.rivet(7, 129, 3); c.rivet(168, 129, 3)
    c.rivet(7, 201, 3); c.rivet(168, 201, 3)
    # 3b) interiores rehundidos: 54 + grid + hotbar
    cells = [(8+18*c, 17+18*r) for r in range(6) for c in range(9)] + \
            [(8+18*c, 143+18*r) for r in range(3) for c in range(9)] + \
            [(8+18*c, 201) for c in range(9)]
    c.recess_slots(cells)
    c.apply_structure()
    c.save()

build_inventory()
build_crafting()
build_generic54()
print("batch 1 done")
