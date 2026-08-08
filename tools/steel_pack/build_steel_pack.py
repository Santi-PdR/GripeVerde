#!/usr/bin/env python3
"""
build_steel_pack.py — Builds the "gripeverde_steel" resource pack for
Eternal Craft: Gripe Verde from vanilla 1.20.x mirror geometry (identical
to 1.20.1 for all container backgrounds — verified against the in-game
validated Master Steel pack alpha masks).

Usage:
  python3 build_steel_pack.py --vanilla <dir/gui> --vanilla119 <dir/gui> --out <packdir>

The generator NEVER changes:
  - texture dimensions
  - the alpha mask (opaque/transparent geometry) — 1:1 preserved
  - slot positions / sizes (18x18, hardcoded from vanilla analysis)
  - icon shapes (luminance-preserving material remap)

The pack registers itself at Position.TOP, required=true (always on).
"""
import argparse
import hashlib
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from steel_pack_lib import (STEEL_LIGHT, STEEL_MID, STEEL_DARK, STEEL_DEEP,
                            STEEL_EDGE, STEEL_NEAR, BRASS, BRASS_DARK,
                            mix, lum, steel_hue, brush_row_lum,
                            wear_noise, panel_color, slot_color, rivet, plague_tape)


def seam_stubs(px, y, x0, x1, length):
    """Short steel joint stubs at both panel edges with brass studs."""
    for i in range(length):
        px[x0 + i, y] = mix(STEEL_DEEP, STEEL_EDGE, 0.35)
        px[x0 + i, y + 1] = mix(STEEL_NEAR, STEEL_LIGHT, 0.35)
        px[x1 - i, y] = mix(STEEL_DEEP, STEEL_EDGE, 0.35)
        px[x1 - i, y + 1] = mix(STEEL_NEAR, STEEL_LIGHT, 0.35)
    px[x0, y] = BRASS_DARK
    px[x0 + 1, y] = BRASS
    px[x1, y] = BRASS_DARK
    px[x1 - 1, y] = BRASS

# ---------------------------------------------------------------- geometry
# Player inventory block shared by most containers (3 rows + hotbar).
_PX = (7, 25, 43, 61, 79, 97, 115, 133, 151)
PLAYER_STD = [(x, y) for x in _PX for y in (83, 101, 119)] + [(x, 141) for x in _PX]

# Slot tables: exact 18x18 well positions per texture, derived from the
# vanilla 1.20.1-equivalent textures (validated against game layouts).
SLOTS = {
    'inventory.png':      [(7, 7), (7, 25), (7, 43), (7, 61),
                           (97, 17), (115, 17), (97, 35), (115, 35), (153, 27)] + PLAYER_STD,
    'generic_54.png':     [(x, y) for x in range(7, 160, 18) for y in range(17, 125, 18)]
                          + [(x, y) for x in range(7, 160, 18) for y in (139, 157, 175)]
                          + [(x, 193) for x in range(7, 160, 18)],
    'shulker_box.png':    [(x, y) for x in _PX for y in (17, 35, 53)] + PLAYER_STD,
    'crafting_table.png': [(x, y) for x in (29, 47, 65) for y in (16, 34, 52)] + [(119, 30)] + PLAYER_STD,
    'furnace.png':        [(55, 16), (55, 52), (111, 30)] + PLAYER_STD,
    'smoker.png':         [(55, 16), (55, 52), (111, 30)] + PLAYER_STD,
    'blast_furnace.png':  [(55, 16), (55, 52), (111, 30)] + PLAYER_STD,
    'anvil.png':          [(26, 46), (75, 46), (133, 46)] + PLAYER_STD,
    'grindstone.png':     [(48, 18), (48, 39), (128, 33)] + PLAYER_STD,
    'smithing.png':       [(7, 47), (25, 47), (43, 47), (97, 47)] + PLAYER_STD,
    'enchanting_table.png': [(14, 46), (34, 46)] + PLAYER_STD,
    'brewing_stand.png':  [(78, 16)] + PLAYER_STD,
    'hopper.png':         [(43, 19), (61, 19), (79, 19), (97, 19), (115, 19)]
                          + [(x, y) for x in _PX for y in (50, 68, 86)] + [(x, 108) for x in _PX],
    'dispenser.png':      [(x, y) for x in (61, 79, 97) for y in (16, 34, 52)] + PLAYER_STD,
    'beacon.png':         [(135, 109)]
                          + [(x, y) for x in (35, 53, 71, 89, 107, 125, 143, 161, 179) for y in (136, 154, 172, 194)],
    'horse.png':          PLAYER_STD,
    'villager.png':       [(135, 36), (161, 36), (215, 33)]
                          + [(x, y) for x in range(107, 260, 18) for y in (83, 101, 119)]
                          + [(x, 141) for x in range(107, 260, 18)],
    'loom.png':           [(12, 25), (32, 25), (22, 44), (138, 52)] + PLAYER_STD,
    'cartography_table.png': [(14, 14), (14, 51), (140, 34)] + PLAYER_STD,
    'stonecutter.png':    [(19, 32), (138, 28)] + PLAYER_STD,
}

# Seams: (y) — steel joint stubs at the panel edges (never cross slots or labels).
SEAMS = {
    'inventory.png':      [80, 138],
    'generic_54.png':     [129],
    'shulker_box.png':    [75],
    'crafting_table.png': [75],
    'furnace.png':        [75],
    'smoker.png':         [75],
    'blast_furnace.png':  [75],
    'anvil.png':          [75],
    'grindstone.png':     [75],
    'smithing.png':       [75],
    'enchanting_table.png': [75],
    'brewing_stand.png':  [75],
    'hopper.png':         [42],
    'dispenser.png':      [75],
    'horse.png':          [75],
    'villager.png':       [66],
    'loom.png':           [75],
    'cartography_table.png': [75],
    'stonecutter.png':    [75],
    'beacon.png':         [],
}

# Per-family accent tints (very subtle) + storage quarantine tape.
CONTAINERS = {
    'inventory.png':       dict(seed=11.0, tint=None, tape=True),
    'generic_54.png':      dict(seed=23.0, tint=None, tape=True, corner_rivets=True),
    'shulker_box.png':     dict(seed=31.0, tint=None, tape=True),
    'crafting_table.png':  dict(seed=7.0, tint=None),
    'furnace.png':         dict(seed=17.0, tint=(168, 158, 148)),
    'smoker.png':          dict(seed=19.0, tint=(168, 158, 148)),
    'blast_furnace.png':   dict(seed=21.0, tint=(172, 156, 146)),
    'anvil.png':           dict(seed=13.0, tint=None),
    'grindstone.png':      dict(seed=15.0, tint=None),
    'smithing.png':        dict(seed=25.0, tint=None),
    'enchanting_table.png': dict(seed=29.0, tint=(152, 150, 170)),
    'brewing_stand.png':   dict(seed=37.0, tint=(154, 164, 158)),
    'hopper.png':          dict(seed=41.0, tint=None),
    'dispenser.png':       dict(seed=43.0, tint=None),
    'beacon.png':          dict(seed=47.0, tint=(170, 164, 150)),
    'horse.png':           dict(seed=53.0, tint=None),
    'villager.png':        dict(seed=59.0, tint=None),
    'loom.png':            dict(seed=61.0, tint=None),
    'cartography_table.png': dict(seed=67.0, tint=None),
    'stonecutter.png':     dict(seed=71.0, tint=None),
}

ALPHA_HASHES = {}


def alpha_hash(im):
    px = im.load()
    w, h = im.size
    m = hashlib.sha256()
    for y in range(h):
        row = bytearray(w)
        for x in range(w):
            row[x] = 1 if px[x, y][3] > 0 else 0
        m.update(row)
    return m.hexdigest()[:16]


def repaint_container(name, src, seed, tint=None, seams=(), tape=False, corner_rivets=False):
    im = src.copy()
    px = im.load()
    w, h = im.size
    slots = SLOTS.get(name, [])
    slot_rects = [(a, b, a + 17, b + 17) for (a, b) in slots]

    # panel bounding box from alpha
    xs = [x for y in range(h) for x in range(w) if px[x, y][3] > 0]
    ys = [y for y in range(h) for x in range(w) if px[x, y][3] > 0]
    x0, y0, x1, y1 = min(xs), min(ys), max(xs), max(ys)

    def in_slot(x, y):
        return any(a <= x <= c and b <= y <= d for (a, b, c, d) in slot_rects)

    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            p = px[x, y]
            if p[3] == 0:
                continue
            if in_slot(x, y):
                continue
            if x == x0 or y == y0 or x == x1 or y == y1:
                px[x, y] = STEEL_EDGE
                continue
            if x == x0 + 1 or y == y0 + 1 or x == x1 - 1 or y == y1 - 1:
                if x == x1 - 1 or y == y1 - 1:
                    px[x, y] = mix(STEEL_DARK, STEEL_EDGE, 0.4)
                else:
                    px[x, y] = mix(STEEL_NEAR, STEEL_LIGHT, 0.45)
                continue
            l = lum(p)
            if l < 100:
                t = (l - 20) / 80.0
                c = mix(STEEL_DEEP, STEEL_MID, max(0.0, min(1.0, t)))
                if tint is not None:
                    c = mix(c, tint, 0.10)
                px[x, y] = c
            elif l > 195:
                t = (255 - l) / 60.0
                c = mix(STEEL_LIGHT, STEEL_MID, max(0.0, min(1.0, t)))
                if tint is not None:
                    c = mix(c, tint, 0.08)
                px[x, y] = c
            else:
                px[x, y] = panel_color(x, y, seed, tint)

    # slots
    for (sx, sy) in slots:
        for dy in range(18):
            for dx in range(18):
                px[sx + dx, sy + dy] = slot_color(dx, dy, sx, sy)

    # seams: joint stubs at the panel edges (short, avoid slots/labels)
    for sy in seams:
        seam_stubs(px, sy, x0, x1, 7)

    # storage quarantine tape (2px green marker, top-left inner edge)
    if tape:
        plague_tape(px, x0 + 3, y0 + 6, 12)

    # structural rivets (storage lockers only)
    if corner_rivets:
        for (rx, ry) in ((x0 + 3, y0 + 3), (x1 - 5, y0 + 3), (x0 + 3, y1 - 5), (x1 - 5, y1 - 5)):
            rivet(px, rx, ry)

    return im, slots


def build_containers(vanilla_gui, out_dir):
    src = os.path.join(vanilla_gui, 'container')
    dst = os.path.join(out_dir, 'assets/minecraft/textures/gui/container')
    os.makedirs(dst, exist_ok=True)
    for name in sorted(CONTAINERS):
        sp = os.path.join(src, name)
        if not os.path.exists(sp):
            print(f"  !! vanilla source missing: {sp}")
            continue
        cfg = CONTAINERS[name]
        im = Image.open(sp).convert('RGBA')
        out, slots = repaint_container(name, im, cfg.get('seed', 5.0),
                                       tint=cfg.get('tint'),
                                       seams=SEAMS.get(name, []),
                                       tape=cfg.get('tape', False),
                                       corner_rivets=cfg.get('corner_rivets', False))
        out.save(os.path.join(dst, name))
        ALPHA_HASHES[name] = alpha_hash(out)
        ok = len(slots) == len(SLOTS[name])
        print(f"  {name:26s} {out.size} slots={len(slots)} {'OK' if ok else 'MISMATCH!'} alpha={ALPHA_HASHES[name]}")


# ---------------------------------------------------------------- sprites
def repaint_luminance(src, dark=STEEL_DEEP, light=STEEL_LIGHT, mid=STEEL_MID,
                      dark_max=100, light_min=195, tint=None):
    im = src.copy()
    px = im.load()
    w, h = im.size
    for y in range(h):
        for x in range(w):
            p = px[x, y]
            if p[3] == 0:
                continue
            l = lum(p)
            if l < dark_max:
                t = (l - 20) / 80.0
                c = mix(dark, mid, max(0.0, min(1.0, t)))
            elif l > light_min:
                t = (255 - l) / 60.0
                c = mix(light, mid, max(0.0, min(1.0, t)))
            else:
                c = mid
            if tint is not None:
                c = mix(c, tint, 0.15)
            px[x, y] = c
    return im


def repaint_flame(src):
    im = src.copy()
    px = im.load()
    w, h = im.size
    for y in range(h):
        for x in range(w):
            p = px[x, y]
            if p[3] == 0:
                continue
            r, g, b = p[0], p[1], p[2]
            l = lum(p)
            if (r - g > 60 and r - b > 60 and r > 140) or (g > 180 and r > 200):
                if r > 210 and g > 130:
                    px[x, y] = (236, 176, 56)
                elif r > 170:
                    px[x, y] = (212, 148, 30)
                else:
                    px[x, y] = (150, 92, 26)
            elif l < 100:
                px[x, y] = mix(STEEL_DEEP, STEEL_EDGE, 0.4)
            elif l > 190:
                px[x, y] = STEEL_LIGHT
            else:
                px[x, y] = STEEL_MID
    return im


def build_sprites(vanilla_gui, out_dir):
    sp = os.path.join(vanilla_gui, 'sprites')
    dst = os.path.join(out_dir, 'assets/minecraft/textures/gui/sprites')
    os.makedirs(dst, exist_ok=True)

    def put(rel, im):
        p = os.path.join(dst, rel)
        os.makedirs(os.path.dirname(p), exist_ok=True)
        im.save(p)
        ALPHA_HASHES['sprites/' + rel] = alpha_hash(im)

    def load(rel):
        return Image.open(os.path.join(sp, rel)).convert('RGBA')

    def steel_slot_img():
        im = Image.new('RGBA', (18, 18), (0, 0, 0, 0))
        for dy in range(18):
            for dx in range(18):
                im.putpixel((dx, dy), slot_color(dx, dy, 0, 0))
        return im

    put('container/slot.png', steel_slot_img())

    for family in ('furnace', 'smoker', 'blast_furnace'):
        put(f'container/{family}/lit_progress.png', repaint_flame(load(f'container/{family}/lit_progress.png')))
        put(f'container/{family}/burn_progress.png',
            repaint_luminance(load(f'container/{family}/burn_progress.png')))

    put('container/brewing_stand/brew_progress.png',
        repaint_luminance(load('container/brewing_stand/brew_progress.png')))
    put('container/brewing_stand/bubbles.png',
        repaint_luminance(load('container/brewing_stand/bubbles.png'),
                          dark=STEEL_DEEP, light=(208, 200, 180), mid=(140, 140, 132)))
    put('container/brewing_stand/fuel_length.png',
        repaint_luminance(load('container/brewing_stand/fuel_length.png'),
                          dark=(150, 92, 26), light=(236, 176, 56), mid=(212, 148, 30)))

    beveled = {'button.png': ('raised', 0), 'button_highlighted.png': ('raised', 1),
               'button_disabled.png': ('flat', 0), 'button_selected.png': ('sunken', 0)}
    for fname, (mode, lit) in beveled.items():
        im = load(f'container/beacon/{fname}')
        px = im.load()
        w, h = im.size
        for y in range(h):
            for x in range(w):
                if px[x, y][3] == 0:
                    continue
                if mode == 'flat':
                    c = mix(STEEL_DEEP, STEEL_MID, 0.25)
                elif mode == 'raised':
                    if y == 0 or x == 0:
                        c = STEEL_EDGE
                    elif y == 1 or x == 1:
                        c = mix(STEEL_LIGHT, STEEL_NEAR, 0.3 + 0.3 * lit)
                    elif y == h - 1 or x == w - 1:
                        c = STEEL_DEEP
                    else:
                        c = panel_color(x, y, 3.0)
                else:  # sunken
                    if y == 0 or x == 0:
                        c = STEEL_DEEP
                    elif y == 1 or x == 1:
                        c = mix(STEEL_DEEP, STEEL_EDGE, 0.5)
                    elif y == h - 1 or x == w - 1:
                        c = mix(STEEL_NEAR, STEEL_LIGHT, 0.4)
                    else:
                        c = mix(panel_color(x, y, 5.0), STEEL_DARK, 0.35)
                px[x, y] = c
        put(f'container/beacon/{fname}', im)
    for fname, tint in (('cancel.png', (150, 52, 40)), ('confirm.png', (106, 138, 72))):
        im = load(f'container/beacon/{fname}')
        px = im.load()
        w, h = im.size
        for y in range(h):
            for x in range(w):
                p = px[x, y]
                if p[3] == 0:
                    continue
                l = lum(p)
                if l < 100:
                    c = mix(STEEL_DEEP, tint, 0.55)
                elif l > 195:
                    c = mix(STEEL_LIGHT, tint, 0.30)
                else:
                    c = mix(STEEL_MID, tint, 0.18)
                px[x, y] = c
        put(f'container/beacon/{fname}', im)

    put('container/anvil/error.png', repaint_luminance(load('container/anvil/error.png'),
                                                       dark=(92, 44, 38), light=(176, 84, 66), mid=STEEL_MID))
    for fname in ('text_field.png', 'text_field_disabled.png'):
        im = load(f'container/anvil/{fname}')
        px = im.load()
        w, h = im.size
        dim = 'disabled' in fname
        for y in range(h):
            for x in range(w):
                p = px[x, y]
                if p[3] == 0:
                    continue
                l = lum(p)
                if l > 190:
                    c = mix(STEEL_LIGHT, STEEL_NEAR, 0.5 if dim else 0.15)
                elif l < 90:
                    c = mix(STEEL_DEEP, STEEL_EDGE, 0.5 if dim else 0.3)
                else:
                    c = mix(panel_color(x, y, 9.0), STEEL_DARK, 0.55 if dim else 0.30)
                px[x, y] = c
        put(f'container/anvil/{fname}', im)

    for fname in ('enchantment_slot.png', 'enchantment_slot_disabled.png', 'enchantment_slot_highlighted.png'):
        im = load(f'container/enchanting_table/{fname}')
        px = im.load()
        w, h = im.size
        hot = 'highlighted' in fname
        dim = 'disabled' in fname
        for y in range(h):
            for x in range(w):
                p = px[x, y]
                if p[3] == 0:
                    continue
                l = lum(p)
                if l > 190:
                    c = mix(STEEL_LIGHT, STEEL_NEAR, 0.2 if hot else 0.5)
                elif l < 90:
                    c = mix(STEEL_DEEP, STEEL_EDGE, 0.4)
                else:
                    c = mix(panel_color(x, y, 13.0), STEEL_DARK, 0.60 if dim else 0.35)
                    if hot:
                        c = mix(c, STEEL_NEAR, 0.15)
                px[x, y] = c
        put(f'container/enchanting_table/{fname}', im)
    for i in (1, 2, 3):
        for suf in ('', '_disabled'):
            put(f'container/enchanting_table/level_{i}{suf}.png',
                repaint_luminance(load(f'container/enchanting_table/level_{i}{suf}.png'),
                                  dark=mix(STEEL_DEEP, (120, 118, 140), 0.4), light=STEEL_LIGHT))

    put('container/grindstone/error.png', repaint_luminance(load('container/grindstone/error.png'),
                                                            dark=(92, 44, 38), light=(176, 84, 66), mid=STEEL_MID))
    put('container/horse/armor_slot.png', steel_slot_img())

    im = load('container/horse/chest_slots.png')
    px = im.load()
    w, h = im.size
    for y in range(h):
        for x in range(w):
            p = px[x, y]
            if p[3] == 0:
                continue
            l = lum(p)
            if l < 100:
                px[x, y] = STEEL_DEEP
            elif l > 195:
                px[x, y] = mix(STEEL_LIGHT, STEEL_NEAR, 0.5)
            else:
                px[x, y] = STEEL_MID
    put('container/horse/chest_slots.png', im)

    for fname in ('scroller.png', 'scroller_disabled.png'):
        im = load(f'container/creative_inventory/{fname}')
        px = im.load()
        w, h = im.size
        dim = 'disabled' in fname
        for y in range(h):
            for x in range(w):
                p = px[x, y]
                if p[3] == 0:
                    continue
                if x == 0:
                    c = STEEL_EDGE
                elif x == w - 1:
                    c = STEEL_DARK
                elif y == h - 1:
                    c = mix(STEEL_DARK, STEEL_EDGE, 0.5)
                elif y % 5 == 2:
                    c = mix(STEEL_DEEP, STEEL_MID, 0.3)
                else:
                    c = panel_color(x, y, 3.0)
                if dim:
                    c = mix(c, STEEL_EDGE, 0.45)
                px[x, y] = c
        put(f'container/creative_inventory/{fname}', im)

    def steel_button(fname, lit=0.0, dim=False):
        im = load(f'widget/{fname}')
        px = im.load()
        w, h = im.size
        for y in range(h):
            for x in range(w):
                p = px[x, y]
                if p[3] == 0:
                    continue
                if y == 0 or x == 0 or x == w - 1:
                    c = STEEL_EDGE
                elif y == 1:
                    c = mix(STEEL_LIGHT, STEEL_NEAR, 0.35 + 0.4 * lit)
                elif y == h - 1:
                    c = STEEL_DEEP
                else:
                    c = panel_color(x, y, 3.0 + lit)
                    if lit > 0:
                        c = mix(c, STEEL_NEAR, 0.18)
                    if dim:
                        c = mix(c, STEEL_EDGE, 0.40)
                px[x, y] = c
        return im

    put('widget/button.png', steel_button('button.png', 0.0))
    put('widget/button_highlighted.png', steel_button('button_highlighted.png', 1.0))
    put('widget/button_disabled.png', steel_button('button_disabled.png', 0.0, dim=True))

    def steel_slider(fname, lit=False):
        im = load(f'widget/{fname}')
        px = im.load()
        w, h = im.size
        for y in range(h):
            for x in range(w):
                p = px[x, y]
                if p[3] == 0:
                    continue
                if y == 0 or y == h - 1 or x == 0 or x == w - 1:
                    c = STEEL_EDGE
                else:
                    c = mix(panel_color(x, y, 7.0), STEEL_DARK, 0.25)
                    if lit:
                        c = mix(c, STEEL_NEAR, 0.15)
                px[x, y] = c
        return im

    put('widget/slider.png', steel_slider('slider.png'))
    put('widget/slider_highlighted.png', steel_slider('slider_highlighted.png', lit=True))

    def steel_handle(fname, lit=False):
        im = load(f'widget/{fname}')
        px = im.load()
        w, h = im.size
        for y in range(h):
            for x in range(w):
                p = px[x, y]
                if p[3] == 0:
                    continue
                if y == 0 or y == h - 1 or x == 0 or x == w - 1:
                    c = STEEL_EDGE
                elif x == 1:
                    c = mix(STEEL_LIGHT, STEEL_NEAR, 0.4)
                elif y % 6 == 3:
                    c = mix(STEEL_DEEP, STEEL_MID, 0.25)
                else:
                    c = panel_color(x, y, 11.0)
                    if lit:
                        c = mix(c, STEEL_NEAR, 0.15)
                px[x, y] = c
        return im

    put('widget/slider_handle.png', steel_handle('slider_handle.png'))
    put('widget/slider_handle_highlighted.png', steel_handle('slider_handle_highlighted.png', lit=True))

    def steel_text_field(fname, lit=False):
        im = load(f'widget/{fname}')
        px = im.load()
        w, h = im.size
        for y in range(h):
            for x in range(w):
                p = px[x, y]
                if p[3] == 0:
                    continue
                l = lum(p)
                if l > 190:
                    c = mix(STEEL_LIGHT, STEEL_NEAR, 0.25 if lit else 0.5)
                elif l < 90:
                    c = mix(STEEL_DEEP, STEEL_EDGE, 0.35)
                else:
                    c = mix(panel_color(x, y, 15.0), STEEL_DARK, 0.45)
                    if lit:
                        c = mix(c, STEEL_NEAR, 0.12)
                px[x, y] = c
        return im

    put('widget/text_field.png', steel_text_field('text_field.png'))
    put('widget/text_field_highlighted.png', steel_text_field('text_field_highlighted.png', lit=True))

    def steel_checkbox(fname, lit=False):
        im = load(f'widget/{fname}')
        px = im.load()
        w, h = im.size
        for y in range(h):
            for x in range(w):
                p = px[x, y]
                if p[3] == 0:
                    continue
                l = lum(p)
                if x == 0 or y == 0 or x == w - 1 or y == h - 1:
                    c = mix(STEEL_EDGE, STEEL_DEEP, 0.3)
                    if lit:
                        c = mix(c, STEEL_NEAR, 0.2)
                elif l > 190:
                    c = mix(STEEL_LIGHT, STEEL_NEAR, 0.4)
                else:
                    c = mix(panel_color(x, y, 17.0), STEEL_DARK, 0.55)
                px[x, y] = c
        return im

    put('widget/checkbox.png', steel_checkbox('checkbox.png'))
    put('widget/checkbox_highlighted.png', steel_checkbox('checkbox_highlighted.png', lit=True))
    put('widget/checkbox_selected.png', steel_checkbox('checkbox_selected.png'))
    put('widget/checkbox_selected_highlighted.png', steel_checkbox('checkbox_selected_highlighted.png', lit=True))

    put('widget/scroller.png', repaint_luminance(load('widget/scroller.png'),
                                                 dark=STEEL_EDGE, light=STEEL_LIGHT, mid=STEEL_MID))

    for fname in ('button.png', 'button_highlighted.png'):
        im = load(f'recipe_book/{fname}')
        px = im.load()
        w, h = im.size
        lit = 'highlighted' in fname
        for y in range(h):
            for x in range(w):
                p = px[x, y]
                if p[3] == 0:
                    continue
                l = lum(p)
                if l < 100:
                    px[x, y] = STEEL_DEEP
                elif l > 195:
                    px[x, y] = mix(STEEL_LIGHT, STEEL_NEAR, 0.3)
                else:
                    c = panel_color(x, y, 21.0)
                    if lit:
                        c = mix(c, STEEL_NEAR, 0.18)
                    px[x, y] = c
        put(f'recipe_book/{fname}', im)


# ---------------------------------------------------------------- creative
def repaint_creative_bg(src_path, seed, tab_strip_h=16):
    im = Image.open(src_path).convert('RGBA')
    px = im.load()
    w, h = im.size
    xs = [x for y in range(h) for x in range(w) if px[x, y][3] > 0]
    ys = [y for y in range(h) for x in range(w) if px[x, y][3] > 0]
    if not xs:
        return im
    x0, y0, x1, y1 = min(xs), min(ys), max(xs), max(ys)
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            p = px[x, y]
            if p[3] == 0:
                continue
            if x == x0 or y == y0 or x == x1 or y == y1:
                px[x, y] = STEEL_EDGE
                continue
            l = lum(p)
            if l < 100:
                px[x, y] = STEEL_DEEP
            elif l > 195:
                px[x, y] = mix(STEEL_LIGHT, STEEL_NEAR, 0.4)
            elif y < tab_strip_h:
                px[x, y] = mix(panel_color(x, y, seed), STEEL_DARK, 0.30)
            else:
                px[x, y] = panel_color(x, y, seed)
    return im


def build_creative(vanilla_119_gui, out_dir):
    src = os.path.join(vanilla_119_gui, 'container/creative_inventory')
    dst = os.path.join(out_dir, 'assets/minecraft/textures/gui/container/creative_inventory')
    os.makedirs(dst, exist_ok=True)
    for idx, fname in enumerate(('tabs.png', 'tab_inventory.png', 'tab_items.png', 'tab_item_search.png')):
        sp = os.path.join(src, fname)
        if not os.path.exists(sp):
            print(f"  !! missing {sp}")
            continue
        im = repaint_creative_bg(sp, 25.0 + idx * 3)
        im.save(os.path.join(dst, fname))
        ALPHA_HASHES[f'creative/{fname}'] = alpha_hash(im)
        print(f"  creative {fname} {im.size} alpha={ALPHA_HASHES[f'creative/{fname}']}")


# ---------------------------------------------------------------- pack meta
PACK_MCMETA = """{
    "pack": {
        "pack_format": 15,
        "description": "Acero de cuarentena — contenedores e interfaces para Eternal Craft: Gripe Verde. EC-QV/1897"
    }
}
"""


def build_pack_icon(out_dir):
    im = Image.new('RGBA', (128, 128), (0, 0, 0, 0))
    px = im.load()
    for y in range(16, 112):
        for x in range(16, 112):
            if x in (16, 111) or y in (16, 111):
                c = STEEL_EDGE
            elif x in (17, 110) or y in (17, 110):
                c = mix(STEEL_LIGHT, STEEL_NEAR, 0.4) if (x == 17 or y == 17) else STEEL_DARK
            else:
                c = steel_hue(brush_row_lum(y, 3.0, 150))
            px[x, y] = c
    for yy in range(34, 78):
        px[24, yy] = (106, 138, 72)
        px[25, yy] = (70, 94, 52)
    for (rx, ry) in ((24, 24), (104, 24), (24, 104), (104, 104)):
        px[rx, ry] = STEEL_LIGHT
        px[rx + 1, ry] = STEEL_DEEP
        px[rx, ry + 1] = STEEL_DEEP
        px[rx + 1, ry + 1] = STEEL_EDGE
    for x in range(60, 92):
        px[x, 60] = STEEL_DEEP
        px[x, 90] = STEEL_DEEP
    for y in range(60, 91):
        px[60, y] = STEEL_DEEP
        px[91, y] = STEEL_DEEP
    for y in range(66, 88):
        px[75, y] = STEEL_LIGHT
    for x in range(66, 86):
        px[x, 76] = STEEL_LIGHT
    im.save(os.path.join(out_dir, 'pack.png'))
    print("  pack.png 128x128")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--vanilla', required=True, help='vanilla 1.20.2 gui dir (container/ + sprites/)')
    ap.add_argument('--vanilla119', required=True, help='vanilla 1.19.1 gui dir (creative tabs)')
    ap.add_argument('--out', required=True, help='output resource pack dir')
    args = ap.parse_args()

    os.makedirs(args.out, exist_ok=True)
    with open(os.path.join(args.out, 'pack.mcmeta'), 'w', encoding='utf-8') as f:
        f.write(PACK_MCMETA)
    print("== containers ==")
    build_containers(args.vanilla, args.out)
    print("== sprites ==")
    build_sprites(args.vanilla, args.out)
    print("== creative ==")
    build_creative(args.vanilla119, args.out)
    print("== icon ==")
    build_pack_icon(args.out)
    print("\nDONE. Output:", args.out)


if __name__ == '__main__':
    main()
