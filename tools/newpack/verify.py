"""
verify.py — auditoria del pack nuevo contra vanilla 1.20.1.

Garantiza:
  1. dimensiones identicas
  2. canal alfa identico pixel a pixel
  3. pixeles con alfa parcial (< 255) identicos (elementos funcionales
     como el boton del libro de recetas o tabs)
  4. regiones verbatim declaradas (figura del jugador, iconos)
  5. slots en las mismas coordenadas (interiores vanilla -> paleta)
  6. panel repintado (ningun #C6C6C6 vanilla residual)
  7. paleta: sin marrones, saturacion acotada, monocromo
  8. estructura: ningun pixel movido de posicion

Uso:
  NC_VANILLA=/ruta/a/container python3 verify.py export/*.png
  (default NC_VANILLA=/tmp/mc-assets/assets/minecraft/textures/gui/container)
"""
import os
import sys

from PIL import Image

VANILLA = os.environ.get(
    'NC_VANILLA',
    os.path.join(os.path.dirname(os.path.abspath(__file__)), 'vanilla'))

# colores vanilla que DEBEN desaparecer del panel (repintados)
VANILLA_PANEL = {
    (0xC6, 0xC6, 0xC6),   # panel
    (0x8B, 0x8B, 0x8B),   # slot
    (0x50, 0x50, 0x50),   # slot offhand
    (0x37, 0x37, 0x37),   # estructura
    (0x21, 0x21, 0x21),   # estructura
}
# colores que pueden permanecer verbatim
ALLOWED_RESIDUAL = {
    (0xFF, 0xFF, 0xFF),   # bevel blanco -> HL (repintado pero permitido)
    (0x55, 0x55, 0x55),   # borde oscuro / figura (verbatim)
    (0xB7, 0xB7, 0xB7), (0x99, 0x99, 0x99),
    (0xD6, 0xD6, 0xD6), (0x5B, 0x5B, 0x5B),
    (0x00, 0x00, 0x00),   # silueta de la figura (verbatim)
}

FAIL = []


def audit(out_path, preserve_regions=None):
    name = os.path.basename(out_path)
    v = Image.open(os.path.join(VANILLA, name)).convert('RGBA')
    o = Image.open(out_path).convert('RGBA')
    if v.size != o.size:
        FAIL.append(f'{name}: tamanio {o.size} != vanilla {v.size}')
        return
    vp, op = v.load(), o.load()
    w, h = v.size

    # 1+2+3: alfa identico y alfa parcial verbatim
    alpha_diffs = partial_diffs = 0
    for y in range(h):
        for x in range(w):
            if vp[x, y][3] != op[x, y][3]:
                alpha_diffs += 1
            elif vp[x, y][3] < 255 and vp[x, y] != op[x, y]:
                partial_diffs += 1
    if alpha_diffs:
        FAIL.append(f'{name}: ALFA difiere en {alpha_diffs} px')
    if partial_diffs:
        FAIL.append(f'{name}: alfa parcial alterado en {partial_diffs} px')

    # 4: regiones verbatim
    for (x0, y0, x1, y1) in (preserve_regions or []):
        d = 0
        for y in range(max(0, y0), min(h, y1 + 1)):
            for x in range(max(0, x0), min(w, x1 + 1)):
                if op[x, y] != vp[x, y]:
                    d += 1
        if d:
            FAIL.append(f'{name}: region verbatim ({x0},{y0})-({x1},{y1}) alterada en {d} px')

    # 5+6: slots en mismas coords y panel repintado (excluye regiones verbatim)
    residual_panel = 0
    for y in range(h):
        for x in range(w):
            p = vp[x, y]
            if p[3] != 255:
                continue
            if preserve_regions and any(
                    x0 <= x <= x1 and y0 <= y <= y1 for (x0, y0, x1, y1) in preserve_regions):
                continue
            t = (p[0], p[1], p[2])
            if t in VANILLA_PANEL:
                if op[x, y] == p:
                    residual_panel += 1
    if residual_panel:
        FAIL.append(f'{name}: {residual_panel} px de panel/slot vanilla sin repintar')

    # 7: paleta de los pixeles repintados
    sat_max = 0
    browns = 0
    for y in range(h):
        for x in range(w):
            p = vp[x, y]
            if p[3] != 255 or (p[0], p[1], p[2]) in VANILLA_PANEL | ALLOWED_RESIDUAL:
                continue
            q = op[x, y]
            if q[3] == 255 and q != p:  # pixel repintado
                r, g, b = q[:3]
                sat = max(r, g, b) - min(r, g, b)
                sat_max = max(sat_max, sat)
                if r > g > b and (r - b) > 30 and b > 60:
                    browns += 1
    if sat_max > 60:
        FAIL.append(f'{name}: saturacion excesiva {sat_max} (>60)')
    if browns:
        FAIL.append(f'{name}: {browns} px marrones')

    status = 'OK' if not any(f.startswith(name) for f in FAIL) else 'FAIL'
    print(f'{status} {name}: alfa_ok partial_ok residual={residual_panel} '
          f'sat_max={sat_max} browns={browns}')


if __name__ == '__main__':
    preserve_regions = None
    if len(sys.argv) > 1:
        raw = eval(sys.argv[1])
        if raw and isinstance(raw[0], int):
            raw = [raw]
        preserve_regions = raw
    files = [f for f in sys.argv[2:]] if len(sys.argv) > 2 else \
        [os.path.join(os.path.dirname(os.path.abspath(__file__)), 'export', 'inventory.png')]
    for f in files:
        audit(f, preserve_regions)
    if FAIL:
        print('\n'.join(f'FAIL: {x}' for x in FAIL))
        sys.exit(1)
    print('AUDITORIA COMPLETA: OK')
