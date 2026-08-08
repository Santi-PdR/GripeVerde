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

SLOT_OUT    = (58, 62, 70)    # slot outer ring
SLOT_DEEP   = (36, 40, 48)    # slot interior top / left shadow
SLOT_MID    = (44, 48, 56)    # slot interior base
SLOT_LIP    = (92, 96, 106)   # slot bottom-right lip (light catch)

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
    """Controlled horizontal brushing: smooth sine variation + sparse streaks.
    Not random noise: a deterministic, low-frequency pattern."""
    v = (base
         + 5.0 * math.sin(y * 0.55 + seed)
         + 3.0 * math.sin(y * 0.173 + seed * 2.7)
         + 1.6 * math.sin(y * 0.043 + seed * 1.31))
    if (y * 7 + int(seed * 10)) % 13 == 0:
        v -= 6.0   # subtle darker brush streak
    if (y * 11 + int(seed * 10)) % 17 == 0:
        v += 5.0   # subtle lighter polish line
    return v

def wear_noise(x, y, seed):
    """Very subtle wear: deterministic ±4 speckles in a few patches."""
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

def slot_color(x, y, sx, sy):
    """18x18 sunken steel slot. Coordinates are local 0..17."""
    if x == 0 or y == 0 or x == 17 or y == 17:
        # outer ring
        if x == 17 and y == 17:
            return SLOT_LIP
        if (x == 0 and y == 17) or (x == 17 and y == 0):
            return (78, 82, 92)
        return SLOT_OUT
    # interior well
    if x == 1 or y == 1:
        return SLOT_DEEP
    if x == 16 or y == 16:
        return SLOT_LIP
    base = SLOT_MID
    # vertical falloff: slightly darker at the top of the well
    d = (8 - y) * 0.55
    base = tuple(clamp(base[i] - d) for i in range(3))
    # subtle brushing inside the well
    b = brush_row_lum(y + sx, 3.1, base[0])
    c = steel_hue(b - 8) if False else tuple(clamp(base[i] + (b - base[0])) for i in range(3))
    return c

def rivet(px, x, y, seed=0.5):
    """2x2 rivet: dark head with a light top-left pixel."""
    px[x, y] = STEEL_DEEP
    px[x + 1, y] = mix(STEEL_DEEP, STEEL_MID, 0.4)
    px[x, y + 1] = mix(STEEL_DEEP, STEEL_EDGE, 0.5)
    px[x + 1, y + 1] = STEEL_EDGE

def seam(px, x0, x1, y, tint=None):
    """Steel section seam: 1px dark + 1px light, with a brass stud at both ends."""
    for xx in range(x0, x1):
        col = mix(STEEL_DEEP, STEEL_EDGE, 0.35)
        if tint is not None:
            col = mix(col, tint, 0.12)
        px[xx, y] = col
        px[xx, y + 1] = mix(STEEL_NEAR, STEEL_LIGHT, 0.35)
    # brass studs
    px[x0, y - 1] = BRASS_DARK
    px[x0 + 1, y - 1] = BRASS_DARK
    px[x0, y] = BRASS
    px[x0 + 1, y] = BRASS
    px[x1 - 2, y - 1] = BRASS_DARK
    px[x1 - 1, y - 1] = BRASS_DARK
    px[x1 - 2, y] = BRASS
    px[x1 - 1, y] = BRASS

def plague_tape(px, x, y, h):
    """Small quarantine tape marker (2px wide, green/dark) — subtle."""
    for yy in range(h):
        px[x, y + yy] = PLAGUE
        px[x + 1, y + yy] = PLAGUE_DARK
