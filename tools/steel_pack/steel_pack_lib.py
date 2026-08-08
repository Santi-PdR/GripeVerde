"""
steel_pack_lib.py — Design system "Acero de Cuarentena" (Quarantine Steel)
for Eternal Craft: Gripe Verde (Minecraft 1.20.1, Forge).

Generates the built-in resource pack textures from the vanilla 1.20.x GUI
geometry, preserving 1:1 alpha masks (geometry untouched), slot positions,
and icon shapes — only the MATERIAL changes (cold brushed steel).

Palette core (reference: #9A9EA8):
  STEEL_LIGHT  #B6BAC4  polished highlights
  STEEL_MID    #9A9EA8  base cold steel
  STEEL_DARK   #7C808A  shadowed zones
  STEEL_DEEP   #5E626C  recessed metal
  STEEL_EDGE   #464A52  outer structural edge
  SLOT_*               sunken slot well (dark, metallic)
  BRASS/PLAGUE/AMBER   small accents only (never dominant)

v0.17.2 — bandejas 26x26, bucket remap, R-ALPHA-1TO1
"""
import math
import random

# ---------------------------------------------------------------- palette
STEEL_LIGHT = (182, 186, 196)
STEEL_MID   = (154, 158, 168)
STEEL_DARK  = (124, 128, 138)
STEEL_DEEP  = (94, 98, 108)
STEEL_EDGE  = (70, 74, 82)
STEEL_NEAR  = (166, 170, 180)

SLOT_OUT    = (58, 62, 70)
SLOT_DEEP   = (36, 40, 48)
SLOT_MID    = (44, 48, 56)
SLOT_LIP    = (92, 96, 106)

BRASS       = (176, 141, 62)
BRASS_DARK  = (132, 104, 44)
PLAGUE      = (106, 138, 72)
PLAGUE_DARK = (70, 94, 52)
AMBER       = (212, 148, 30)
AMBER_HOT   = (236, 176, 56)
EMBER_DEEP  = (150, 92, 26)
BLOOD       = (150, 52, 40)
BLOOD_DEEP  = (110, 34, 26)

# ---------------------------------------------------------------- helpers
def clamp(v, lo=0, hi=255):
    return lo if v < lo else hi if v > hi else int(v)

def mix(c1, c2, t):
    return tuple(clamp(c1[i] + (c2[i] - c1[i]) * t) for i in range(3))

def lum(p):
    return (p[0] + p[1] + p[2]) // 3

def steel_hue(v):
    """Map a luminance value onto the cold steel hue (r,g,b)."""
    return (clamp(v + 0), clamp(v + 4), clamp(v + 14))

def brush_row_lum(y, seed, base=154):
    v = (base
         + 5.0 * math.sin(y * 0.55 + seed)
         + 3.0 * math.sin(y * 0.173 + seed * 2.7)
         + 1.6 * math.sin(y * 0.043 + seed * 1.31))
    if (y * 7 + int(seed * 10)) % 13 == 0:
        v -= 6.0
    if (y * 11 + int(seed * 10)) % 17 == 0:
        v += 5.0
    return v

def wear_noise(x, y, seed):
    h = (x * 73856093) ^ (y * 19349663) ^ (int(seed) * 83492791)
    h = (h ^ (h >> 13)) * 1274126177
    return ((h & 0xFFFF) / 0xFFFF - 0.5) * 8.0

def panel_color(x, y, seed, tint=None):
    v = brush_row_lum(y, seed)
    v += wear_noise(x, y, seed) * 0.55
    c = steel_hue(v)
    if tint is not None:
        c = mix(c, tint, 0.10)
    return c

def well_color(x, y, w, h):
    """Interior de bandeja 26x26: base 121 lum + gradiente vertical suave."""
    base = 121
    # gradiente: mas oscuro arriba, mas claro abajo
    grad = (y - h * 0.5) * 0.35
    v = base + grad + wear_noise(x, y, 9.1) * 0.35
    return steel_hue(v)

def slot_color(x, y, sx, sy):
    """18x18 sunken steel slot. Coordinates are local 0..17."""
    if x == 0 or y == 0 or x == 17 or y == 17:
        if x == 17 and y == 17:
            return SLOT_LIP
        if (x == 0 and y == 17) or (x == 17 and y == 0):
            return (78, 82, 92)
        return SLOT_OUT
    if x == 1 or y == 1:
        return SLOT_DEEP
    if x == 16 or y == 16:
        return SLOT_LIP
    base = SLOT_MID
    d = (8 - y) * 0.55
    base = tuple(clamp(base[i] - d) for i in range(3))
    b = brush_row_lum(y + sx, 3.1, base[0])
    c = tuple(clamp(base[i] + (b - base[0])) for i in range(3))
    return c

def rivet(px, x, y, seed=0.5):
    px[x, y] = STEEL_DEEP
    px[x + 1, y] = mix(STEEL_DEEP, STEEL_MID, 0.4)
    px[x, y + 1] = mix(STEEL_DEEP, STEEL_EDGE, 0.5)
    px[x + 1, y + 1] = STEEL_EDGE

def seam(px, x0, x1, y, tint=None):
    for xx in range(x0, x1):
        col = mix(STEEL_DEEP, STEEL_EDGE, 0.35)
        if tint is not None:
            col = mix(col, tint, 0.12)
        px[xx, y] = col
        px[xx, y + 1] = mix(STEEL_NEAR, STEEL_LIGHT, 0.35)
    px[x0, y - 1] = BRASS_DARK
    px[x0 + 1, y - 1] = BRASS_DARK
    px[x0, y] = BRASS
    px[x0 + 1, y] = BRASS
    px[x1 - 2, y - 1] = BRASS_DARK
    px[x1 - 1, y - 1] = BRASS_DARK
    px[x1 - 2, y] = BRASS
    px[x1 - 1, y] = BRASS

def plague_tape(px, x, y, h):
    for yy in range(h):
        px[x, y + yy] = PLAGUE
        px[x + 1, y + yy] = PLAGUE_DARK

# v0.17.2 helpers — bucket remap & alpha
def bucket_remap_color(p, seed, x, y, tint=None):
    """Remap por buckets de luminancia (panel): 30/105/175/225."""
    l = lum(p)
    if l <= 30:
        c = mix(STEEL_DEEP, STEEL_EDGE, 0.45)
    elif l <= 105:
        t = (l - 30) / 75.0
        c = mix(STEEL_DEEP, STEEL_MID, t)
        if tint is not None:
            c = mix(c, tint, 0.10)
    elif l <= 175:
        c = panel_color(x, y, seed, tint)
    elif l <= 225:
        t = (225 - l) / 50.0
        c = mix(STEEL_LIGHT, STEEL_MID, t)
        if tint is not None:
            c = mix(c, tint, 0.08)
    else:
        c = mix(STEEL_LIGHT, STEEL_NEAR, 0.35)
    return c

def restore_all_alphas(src_im, dst_im):
    """R-ALPHA-1TO1: restaura alpha EXACTO de la fuente vanilla."""
    sw, sh = src_im.size
    dw, dh = dst_im.size
    if sw != dw or sh != dh:
        return dst_im
    spx = src_im.load()
    dpx = dst_im.load()
    for y in range(sh):
        for x in range(sw):
            a = spx[x, y][3]
            r, g, b, _ = dpx[x, y]
            dpx[x, y] = (r, g, b, a)
    return dst_im
