#!/usr/bin/env python3
"""
build_steel_pack.py — Builds the "gripeverde_steel" resource pack for
Eternal Craft: Gripe Verde from vanilla 1.20.x mirror geometry.
v0.17.2 — detect_wells 26x26 bandejas, bucket remap, R-ALPHA-1TO1.

Usage:
  python3 build_steel_pack.py --vanilla <dir/gui> --vanilla119 <dir/gui> --out <packdir>
  (sin args: usa src/main/resources/resourcepacks/gripeverde_steel como out y
   regenera placeholders in-place si no hay mirrors vanilla)
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
                            wear_noise, panel_color, slot_color, well_color,
                            rivet, plague_tape, bucket_remap_color, restore_all_alphas)

# ---------------------------------------------------------------- geometry
_PX = (7, 25, 43, 61, 79, 97, 115, 133, 151)
PLAYER_STD = [(x, y) for x in _PX for y in (83, 101, 119)] + [(x, 141) for x in _PX]

# Fallback slot tables (usados solo si detect_wells no encuentra nada)
SLOTS_FALLBACK = {
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
    'beacon.png':         [(135, 109)] + [(x, y) for x in (35, 53, 71, 89, 107, 125, 143, 161, 179) for y in (136, 154, 172, 194)],
    'horse.png':          PLAYER_STD,
    'villager.png':       [(135, 36), (161, 36), (215, 33)] + [(x, y) for x in range(107, 260, 18) for y in (83, 101, 119)] + [(x, 141) for x in range(107, 260, 18)],
    'loom.png':           [(12, 25), (32, 25), (22, 44), (138, 52)] + PLAYER_STD,
    'cartography_table.png': [(14, 14), (14, 51), (140, 34)] + PLAYER_STD,
    'stonecutter.png':    [(19, 32), (138, 28)] + PLAYER_STD,
}

def seam_stubs(px, y, x0, x1, length):
    for i in range(length):
        px[x0 + i, y] = mix(STEEL_DEEP, STEEL_EDGE, 0.35)
        px[x0 + i, y + 1] = mix(STEEL_NEAR, STEEL_LIGHT, 0.35)
        px[x1 - i, y] = mix(STEEL_DEEP, STEEL_EDGE, 0.35)
        px[x1 - i, y + 1] = mix(STEEL_NEAR, STEEL_LIGHT, 0.35)
    px[x0, y] = BRASS_DARK
    px[x0 + 1, y] = BRASS
    px[x1, y] = BRASS_DARK
    px[x1 - 1, y] = BRASS

SEAMS = {
    'inventory.png': [80, 138], 'generic_54.png': [129], 'shulker_box.png': [75],
    'crafting_table.png': [75], 'furnace.png': [75], 'smoker.png': [75],
    'blast_furnace.png': [75], 'anvil.png': [75], 'grindstone.png': [75],
    'smithing.png': [75], 'enchanting_table.png': [75], 'brewing_stand.png': [75],
    'hopper.png': [42], 'dispenser.png': [75], 'horse.png': [75],
    'villager.png': [66], 'loom.png': [75], 'cartography_table.png': [75],
    'stonecutter.png': [75], 'beacon.png': [],
}

CONTAINERS = {
    'inventory.png': dict(seed=11.0, tint=None, tape=True),
    'generic_54.png': dict(seed=23.0, tint=None, tape=True, corner_rivets=True),
    'shulker_box.png': dict(seed=31.0, tint=None, tape=True),
    'crafting_table.png': dict(seed=7.0, tint=None),
    'furnace.png': dict(seed=17.0, tint=(168, 158, 148)),
    'smoker.png': dict(seed=19.0, tint=(168, 158, 148)),
    'blast_furnace.png': dict(seed=21.0, tint=(172, 156, 146)),
    'anvil.png': dict(seed=13.0, tint=None),
    'grindstone.png': dict(seed=15.0, tint=None),
    'smithing.png': dict(seed=25.0, tint=None),
    'enchanting_table.png': dict(seed=29.0, tint=(152, 150, 170)),
    'brewing_stand.png': dict(seed=37.0, tint=(154, 164, 158)),
    'hopper.png': dict(seed=41.0, tint=None),
    'dispenser.png': dict(seed=43.0, tint=None),
    'beacon.png': dict(seed=47.0, tint=(170, 164, 150)),
    'horse.png': dict(seed=53.0, tint=None),
    'villager.png': dict(seed=59.0, tint=None),
    'loom.png': dict(seed=61.0, tint=None),
    'cartography_table.png': dict(seed=67.0, tint=None),
    'stonecutter.png': dict(seed=71.0, tint=None),
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

# --- v0.17.2 detect_wells
def detect_wells(im):
    """Detecta pozos vanilla por firma: anillo oscuro 35-90, interior 115-170, labio >=205."""
    px = im.load()
    w, h = im.size
    # recolecta candidatos: centros de interiores claros rodeados de anillo oscuro
    candidates = []
    for y in range(2, h-2):
        for x in range(2, w-2):
            p = px[x, y]
            if p[3] == 0:
                continue
            l = lum(p)
            if 115 <= l <= 170:
                # verifica anillo oscuro en vecindad 9x9 borde y labio claro
                dark = 0; light = 0; interior = 0
                for dy in range(-9, 10):
                    for dx in range(-9, 10):
                        if abs(dx) < 7 and abs(dy) < 7:
                            continue
                        nx, ny = x+dx, y+dy
                        if 0 <= nx < w and 0 <= ny < h and px[nx, ny][3] > 0:
                            ll = lum(px[nx, ny])
                            if 35 <= ll <= 90:
                                dark += 1
                            if ll >= 205:
                                light += 1
                # heurística: debe haber anillo oscuro consistente
                if dark >= 12 and light >= 2:
                    # tamaño interior estimado 13-40 px continuos
                    cnt = 0
                    for yy in range(max(0, y-8), min(h, y+9)):
                        for xx in range(max(0, x-8), min(w, x+9)):
                            if px[xx, yy][3] > 0 and 115 <= lum(px[xx, yy]) <= 170:
                                cnt += 1
                    if 13 <= cnt <= 90:
                        candidates.append((x, y))
    # dedupe por separación >=8
    wells = []
    for cx, cy in candidates:
        if any(abs(cx-wx) < 8 and abs(cy-wy) < 8 for wx, wy in wells):
            continue
        # estima bounding box del pozo
        # busca extents donde alpha>0 y lum en rango interior+anillo
        x0 = cx; x1 = cx; y0 = cy; y1 = cy
        for d in range(1, 20):
            if cx-d >= 0 and px[cx-d, cy][3] > 0: x0 = cx-d
            else: break
        for d in range(1, 20):
            if cx+d < w and px[cx+d, cy][3] > 0: x1 = cx+d
            else: break
        for d in range(1, 20):
            if cy-d >= 0 and px[cx, cy-d][3] > 0: y0 = cy-d
            else: break
        for d in range(1, 20):
            if cy+d < h and px[cx, cy+d][3] > 0: y1 = cy+d
            else: break
        bw = x1 - x0 + 1; bh = y1 - y0 + 1
        # clasifica: 18x18 slot vs 26x26 bandeja (horno 27x26)
        if 24 <= bw <= 28 and 24 <= bh <= 28:
            wells.append((x0, y0, bw, bh, 'tray'))
        elif 16 <= bw <= 20 and 16 <= bh <= 20:
            wells.append((x0, y0, bw, bh, 'slot'))
    # fallback: si detectó muy pocos, completa con tabla fallback (evita huecos)
    if len(wells) < 3:
        return None
    # convierte a top-left precisos restando borde 1px
    result = []
    for x0, y0, bw, bh, kind in wells:
        result.append((x0, y0, bw, bh, kind))
    return result

def repaint_container(name, src, seed, tint=None, seams=(), tape=False, corner_rivets=False):
    # R-ALPHA-1TO1: guarda alpha original
    src_alpha = src.copy()
    im = src.copy()
    px = im.load()
    w, h = im.size

    # detecta pozos dinámicamente
    wells = detect_wells(src)
    if wells is None:
        # fallback a tabla hardcodeada
        slots = SLOTS_FALLBACK.get(name, [])
        wells = [(x, y, 18, 18, 'slot') for x, y in slots]
        # añade bandeja conocida del resultado donde corresponde
        tray_map = {'crafting_table.png': (119, 30, 26, 26), 'furnace.png': (111, 30, 26, 26),
                    'smoker.png': (111, 30, 26, 26), 'blast_furnace.png': (111, 30, 26, 26),
                    'loom.png': (138, 52, 26, 26), 'stonecutter.png': (138, 28, 26, 26),
                    'cartography_table.png': (140, 34, 26, 26), 'villager.png': (215, 33, 26, 26)}
        if name in tray_map:
            tx, ty, tw, th = tray_map[name]
            # reemplaza el slot de resultado por bandeja
            wells = [(tx, ty, tw, th, 'tray') if (x == tx and y == ty) or (abs(x-tx)<2 and abs(y-ty)<2) else (x,y,bw,bh,kind) for x,y,bw,bh,kind in wells]
    else:
        # wells ya detectados; normaliza
        pass

    # slot rects para evitar pintar panel sobre pozos
    well_rects = [(x, y, x+bw-1, y+bh-1) for x, y, bw, bh, _ in wells]

    xs = [x for y in range(h) for x in range(w) if px[x, y][3] > 0]
    ys = [y for y in range(h) for x in range(w) if px[x, y][3] > 0]
    if not xs:
        return restore_all_alphas(src_alpha, im), wells
    x0, y0, x1, y1 = min(xs), min(ys), max(xs), max(ys)

    def in_well(x, y):
        return any(a <= x <= c and b <= y <= d for (a, b, c, d) in well_rects)

    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            p = px[x, y]
            if p[3] == 0: continue
            if in_well(x, y): continue
            if x == x0 or y == y0 or x == x1 or y == y1:
                px[x, y] = STEEL_EDGE
                continue
            if x == x0+1 or y == y0+1 or x == x1-1 or y == y1-1:
                if x == x1-1 or y == y1-1:
                    px[x, y] = mix(STEEL_DARK, STEEL_EDGE, 0.4)
                else:
                    px[x, y] = mix(STEEL_NEAR, STEEL_LIGHT, 0.45)
                continue
            # bucket remap v0.17.2
            c = bucket_remap_color(p, seed, x, y, tint)
            px[x, y] = c

    # pinta pozos
    for (wx, wy, bw, bh, kind) in wells:
        if kind == 'tray':
            # bandeja 26x26 / 27x26: anillo 64, interior well_color, labio 197
            for dy in range(bh):
                for dx in range(bw):
                    if dx==0 or dy==0 or dx==bw-1 or dy==bh-1:
                        px[wx+dx, wy+dy] = steel_hue(64)
                    elif dx==1 or dy==1:
                        px[wx+dx, wy+dy] = steel_hue(48)
                    elif dx==bw-2 or dy==bh-2:
                        px[wx+dx, wy+dy] = steel_hue(197)
                    else:
                        px[wx+dx, wy+dy] = well_color(dx, dy, bw, bh)
        else:
            for dy in range(18):
                for dx in range(18):
                    if 0 <= wx+dx < w and 0 <= wy+dy < h:
                        px[wx+dx, wy+dy] = slot_color(dx, dy, wx, wy)

    for sy in seams:
        seam_stubs(px, sy, x0, x1, 7)
    if tape:
        plague_tape(px, x0+3, y0+6, 12)
    if corner_rivets:
        for (rx, ry) in ((x0+3, y0+3), (x1-5, y0+3), (x0+3, y1-5), (x1-5, y1-5)):
            rivet(px, rx, ry)

    # R-ALPHA-1TO1
    restore_all_alphas(src_alpha, im)
    return im, wells

def build_containers(vanilla_gui, out_dir, src_alpha_map=None):
    src = os.path.join(vanilla_gui, 'container') if vanilla_gui and os.path.isdir(os.path.join(vanilla_gui, 'container')) else None
    # si no hay mirror vanilla, usa el pack existente como fuente (in-place improve)
    if src is None:
        src = os.path.join(out_dir, 'assets/minecraft/textures/gui/container')
        vanilla_mode = False
    else:
        vanilla_mode = True
    dst = os.path.join(out_dir, 'assets/minecraft/textures/gui/container')
    os.makedirs(dst, exist_ok=True)
    for name in sorted(CONTAINERS):
        sp = os.path.join(src, name)
        if not os.path.exists(sp):
            print(f"  !! missing {sp}")
            continue
        cfg = CONTAINERS[name]
        im = Image.open(sp).convert('RGBA')
        out, wells = repaint_container(name, im, cfg.get('seed', 5.0), tint=cfg.get('tint'), seams=SEAMS.get(name, []), tape=cfg.get('tape', False), corner_rivets=cfg.get('corner_rivets', False))
        out.save(os.path.join(dst, name))
        ALPHA_HASHES[name] = alpha_hash(out)
        print(f"  {name:26s} {out.size} wells={len(wells)} alpha={ALPHA_HASHES[name]}")


# ---------------------------------------------------------------- sprites
def repaint_luminance(src, dark=STEEL_DEEP, light=STEEL_LIGHT, mid=STEEL_MID, dark_max=100, light_min=195, tint=None):
    im = src.copy()
    px = im.load()
    w, h = im.size
    for y in range(h):
        for x in range(w):
            p = px[x, y]
            if p[3]==0: continue
            l = lum(p)
            if l < dark_max:
                t = (l-20)/80.0; c = mix(dark, mid, max(0,min(1,t)))
            elif l > light_min:
                t = (255-l)/60.0; c = mix(light, mid, max(0,min(1,t)))
            else:
                c = mid
            if tint is not None: c = mix(c, tint, 0.15)
            px[x, y] = c
    return im

def repaint_flame(src):
    im = src.copy(); px=im.load(); w,h=im.size
    for y in range(h):
        for x in range(w):
            p=px[x,y]
            if p[3]==0: continue
            r,g,b=p[0],p[1],p[2]; l=lum(p)
            if (r-g>60 and r-b>60 and r>140) or (g>180 and r>200):
                if r>210 and g>130: px[x,y]=(236,176,56)
                elif r>170: px[x,y]=(212,148,30)
                else: px[x,y]=(150,92,26)
            elif l<100: px[x,y]=mix(STEEL_DEEP, STEEL_EDGE, 0.4)
            elif l>190: px[x,y]=STEEL_LIGHT
            else: px[x,y]=STEEL_MID
    return im

def build_sprites(vanilla_gui, out_dir):
    # sprites: si no hay mirror vanilla, genera placeholders steel
    sp_base = os.path.join(vanilla_gui, 'sprites') if vanilla_gui and os.path.isdir(os.path.join(vanilla_gui, 'sprites')) else None
    dst = os.path.join(out_dir, 'assets/minecraft/textures/gui/sprites')
    os.makedirs(dst, exist_ok=True)
    def put(rel, im):
        p=os.path.join(dst, rel); os.makedirs(os.path.dirname(p), exist_ok=True); im.save(p); ALPHA_HASHES['sprites/'+rel]=alpha_hash(im)
    def load(rel):
        if sp_base and os.path.exists(os.path.join(sp_base, rel)):
            return Image.open(os.path.join(sp_base, rel)).convert('RGBA')
        return None
    def placeholder(w,h):
        im=Image.new('RGBA',(w,h),(0,0,0,0))
        px=im.load()
        for y in range(h):
            for x in range(w):
                if x==0 or y==0 or x==w-1 or y==h-1: px[x,y]=STEEL_EDGE
                else: px[x,y]=panel_color(x,y,5.0)
        return im
    def steel_slot_img():
        im=Image.new('RGBA',(18,18),(0,0,0,0))
        for dy in range(18):
            for dx in range(18):
                im.putpixel((dx,dy), slot_color(dx,dy,0,0))
        return im
    # slot
    put('container/slot.png', steel_slot_img())
    # furnace families
    for family in ('furnace','smoker','blast_furnace'):
        src = load(f'container/{family}/lit_progress.png')
        put(f'container/{family}/lit_progress.png', repaint_flame(src) if src else placeholder(14,14))
        src2 = load(f'container/{family}/burn_progress.png')
        put(f'container/{family}/burn_progress.png', repaint_luminance(src2) if src2 else placeholder(24,16))
    for name, args in [('container/brewing_stand/brew_progress.png', {}),
                       ('container/brewing_stand/bubbles.png', dict(dark=STEEL_DEEP, light=(208,200,180), mid=(140,140,132))),
                       ('container/brewing_stand/fuel_length.png', dict(dark=(150,92,26), light=(236,176,56), mid=(212,148,30)))]:
        src=load(name)
        if src: put(name, repaint_luminance(src, **args) if args else repaint_luminance(src))
        else: put(name, placeholder(16,16))
    # beacon beveled
    beveled={'button.png':('raised',0),'button_highlighted.png':('raised',1),'button_disabled.png':('flat',0),'button_selected.png':('sunken',0)}
    for fname,(mode,lit) in beveled.items():
        src=load(f'container/beacon/{fname}')
        if src is None: put(f'container/beacon/{fname}', placeholder(20,20)); continue
        im=src.copy(); px=im.load(); w,h=im.size
        for y in range(h):
            for x in range(w):
                if px[x,y][3]==0: continue
                if mode=='flat': c=mix(STEEL_DEEP, STEEL_MID,0.25)
                elif mode=='raised':
                    if y==0 or x==0: c=STEEL_EDGE
                    elif y==1 or x==1: c=mix(STEEL_LIGHT,STEEL_NEAR,0.3+0.3*lit)
                    elif y==h-1 or x==w-1: c=STEEL_DEEP
                    else: c=panel_color(x,y,3.0)
                else:
                    if y==0 or x==0: c=STEEL_DEEP
                    elif y==1 or x==1: c=mix(STEEL_DEEP,STEEL_EDGE,0.5)
                    elif y==h-1 or x==w-1: c=mix(STEEL_NEAR,STEEL_LIGHT,0.4)
                    else: c=mix(panel_color(x,y,5.0),STEEL_DARK,0.35)
                px[x,y]=c
        put(f'container/beacon/{fname}', im)
    for fname,tint in (('cancel.png',(150,52,40)),('confirm.png',(106,138,72))):
        src=load(f'container/beacon/{fname}')
        if src is None: put(f'container/beacon/{fname}', placeholder(18,18)); continue
        im=src.copy(); px=im.load(); w,h=im.size
        for y in range(h):
            for x in range(w):
                p=px[x,y]
                if p[3]==0: continue
                l=lum(p)
                if l<100: c=mix(STEEL_DEEP,tint,0.55)
                elif l>195: c=mix(STEEL_LIGHT,tint,0.30)
                else: c=mix(STEEL_MID,tint,0.18)
                px[x,y]=c
        put(f'container/beacon/{fname}', im)
    # anvil
    src=load('container/anvil/error.png')
    put('container/anvil/error.png', repaint_luminance(src, dark=(92,44,38), light=(176,84,66), mid=STEEL_MID) if src else placeholder(16,16))
    for fname in ('text_field.png','text_field_disabled.png'):
        src=load(f'container/anvil/{fname}')
        if src is None: put(f'container/anvil/{fname}', placeholder(40,16)); continue
        im=src.copy(); px=im.load(); w,h=im.size; dim='disabled' in fname
        for y in range(h):
            for x in range(w):
                p=px[x,y]
                if p[3]==0: continue
                l=lum(p)
                if l>190: c=mix(STEEL_LIGHT,STEEL_NEAR,0.5 if dim else 0.15)
                elif l<90: c=mix(STEEL_DEEP,STEEL_EDGE,0.5 if dim else 0.3)
                else: c=mix(panel_color(x,y,9.0),STEEL_DARK,0.55 if dim else 0.30)
                px[x,y]=c
        put(f'container/anvil/{fname}', im)
    # enchanting
    for fname in ('enchantment_slot.png','enchantment_slot_disabled.png','enchantment_slot_highlighted.png'):
        src=load(f'container/enchanting_table/{fname}')
        if src is None: put(f'container/enchanting_table/{fname}', placeholder(16,16)); continue
        im=src.copy(); px=im.load(); w,h=im.size; hot='highlighted' in fname; dim='disabled' in fname
        for y in range(h):
            for x in range(w):
                p=px[x,y]
                if p[3]==0: continue
                l=lum(p)
                if l>190: c=mix(STEEL_LIGHT,STEEL_NEAR,0.2 if hot else 0.5)
                elif l<90: c=mix(STEEL_DEEP,STEEL_EDGE,0.4)
                else:
                    c=mix(panel_color(x,y,13.0),STEEL_DARK,0.60 if dim else 0.35)
                    if hot: c=mix(c,STEEL_NEAR,0.15)
                px[x,y]=c
        put(f'container/enchanting_table/{fname}', im)
    for i in (1,2,3):
        for suf in ('','_disabled'):
            rel=f'container/enchanting_table/level_{i}{suf}.png'
            src=load(rel)
            if src: put(rel, repaint_luminance(src, dark=mix(STEEL_DEEP,(120,118,140),0.4), light=STEEL_LIGHT))
            else: put(rel, placeholder(16,16))
    src=load('container/grindstone/error.png')
    put('container/grindstone/error.png', repaint_luminance(src, dark=(92,44,38), light=(176,84,66), mid=STEEL_MID) if src else placeholder(16,16))
    put('container/horse/armor_slot.png', steel_slot_img())
    src=load('container/horse/chest_slots.png')
    if src:
        im=src.copy(); px=im.load(); w,h=im.size
        for y in range(h):
            for x in range(w):
                p=px[x,y]
                if p[3]==0: continue
                l=lum(p)
                if l<100: px[x,y]=STEEL_DEEP
                elif l>195: px[x,y]=mix(STEEL_LIGHT,STEEL_NEAR,0.5)
                else: px[x,y]=STEEL_MID
        put('container/horse/chest_slots.png', im)
    else:
        put('container/horse/chest_slots.png', placeholder(18,18))
    for fname in ('scroller.png','scroller_disabled.png'):
        src=load(f'container/creative_inventory/{fname}')
        if src is None: put(f'container/creative_inventory/{fname}', placeholder(12,15)); continue
        im=src.copy(); px=im.load(); w,h=im.size; dim='disabled' in fname
        for y in range(h):
            for x in range(w):
                p=px[x,y]
                if p[3]==0: continue
                if x==0: c=STEEL_EDGE
                elif x==w-1: c=STEEL_DARK
                elif y==h-1: c=mix(STEEL_DARK,STEEL_EDGE,0.5)
                elif y%5==2: c=mix(STEEL_DEEP,STEEL_MID,0.3)
                else: c=panel_color(x,y,3.0)
                if dim: c=mix(c,STEEL_EDGE,0.45)
                px[x,y]=c
        put(f'container/creative_inventory/{fname}', im)
    # widget
    def steel_button(fname, lit=0.0, dim=False):
        src=load(f'widget/{fname}')
        if src is None: return placeholder(200,20)
        im=src.copy(); px=im.load(); w,h=im.size
        for y in range(h):
            for x in range(w):
                p=px[x,y]
                if p[3]==0: continue
                if y==0 or x==0 or x==w-1: c=STEEL_EDGE
                elif y==1: c=mix(STEEL_LIGHT,STEEL_NEAR,0.35+0.4*lit)
                elif y==h-1: c=STEEL_DEEP
                else:
                    c=panel_color(x,y,3.0+lit)
                    if lit>0: c=mix(c,STEEL_NEAR,0.18)
                    if dim: c=mix(c,STEEL_EDGE,0.40)
                px[x,y]=c
        return im
    put('widget/button.png', steel_button('button.png',0.0))
    put('widget/button_highlighted.png', steel_button('button_highlighted.png',1.0))
    put('widget/button_disabled.png', steel_button('button_disabled.png',0.0,dim=True))
    def steel_slider(fname, lit=False):
        src=load(f'widget/{fname}')
        if src is None: return placeholder(200,20)
        im=src.copy(); px=im.load(); w,h=im.size
        for y in range(h):
            for x in range(w):
                p=px[x,y]
                if p[3]==0: continue
                if y==0 or y==h-1 or x==0 or x==w-1: c=STEEL_EDGE
                else:
                    c=mix(panel_color(x,y,7.0),STEEL_DARK,0.25)
                    if lit: c=mix(c,STEEL_NEAR,0.15)
                px[x,y]=c
        return im
    put('widget/slider.png', steel_slider('slider.png'))
    put('widget/slider_highlighted.png', steel_slider('slider_highlighted.png',lit=True))
    def steel_handle(fname, lit=False):
        src=load(f'widget/{fname}')
        if src is None: return placeholder(8,20)
        im=src.copy(); px=im.load(); w,h=im.size
        for y in range(h):
            for x in range(w):
                p=px[x,y]
                if p[3]==0: continue
                if y==0 or y==h-1 or x==0 or x==w-1: c=STEEL_EDGE
                elif x==1: c=mix(STEEL_LIGHT,STEEL_NEAR,0.4)
                elif y%6==3: c=mix(STEEL_DEEP,STEEL_MID,0.25)
                else:
                    c=panel_color(x,y,11.0)
                    if lit: c=mix(c,STEEL_NEAR,0.15)
                px[x,y]=c
        return im
    put('widget/slider_handle.png', steel_handle('slider_handle.png'))
    put('widget/slider_handle_highlighted.png', steel_handle('slider_handle_highlighted.png',lit=True))
    def steel_text_field(fname, lit=False):
        src=load(f'widget/{fname}')
        if src is None: return placeholder(200,20)
        im=src.copy(); px=im.load(); w,h=im.size
        for y in range(h):
            for x in range(w):
                p=px[x,y]
                if p[3]==0: continue
                l=lum(p)
                if l>190: c=mix(STEEL_LIGHT,STEEL_NEAR,0.25 if lit else 0.5)
                elif l<90: c=mix(STEEL_DEEP,STEEL_EDGE,0.35)
                else:
                    c=mix(panel_color(x,y,15.0),STEEL_DARK,0.45)
                    if lit: c=mix(c,STEEL_NEAR,0.12)
                px[x,y]=c
        return im
    put('widget/text_field.png', steel_text_field('text_field.png'))
    put('widget/text_field_highlighted.png', steel_text_field('text_field_highlighted.png',lit=True))
    def steel_checkbox(fname, lit=False):
        src=load(f'widget/{fname}')
        if src is None: return placeholder(20,20)
        im=src.copy(); px=im.load(); w,h=im.size
        for y in range(h):
            for x in range(w):
                p=px[x,y]
                if p[3]==0: continue
                l=lum(p)
                if x==0 or y==0 or x==w-1 or y==h-1:
                    c=mix(STEEL_EDGE,STEEL_DEEP,0.3)
                    if lit: c=mix(c,STEEL_NEAR,0.2)
                elif l>190: c=mix(STEEL_LIGHT,STEEL_NEAR,0.4)
                else: c=mix(panel_color(x,y,17.0),STEEL_DARK,0.55)
                px[x,y]=c
        return im
    put('widget/checkbox.png', steel_checkbox('checkbox.png'))
    put('widget/checkbox_highlighted.png', steel_checkbox('checkbox_highlighted.png',lit=True))
    put('widget/checkbox_selected.png', steel_checkbox('checkbox_selected.png'))
    put('widget/checkbox_selected_highlighted.png', steel_checkbox('checkbox_selected_highlighted.png',lit=True))
    put('widget/scroller.png', repaint_luminance(load('widget/scroller.png'), dark=STEEL_EDGE, light=STEEL_LIGHT, mid=STEEL_MID) if load('widget/scroller.png') else placeholder(6,20))
    for fname in ('button.png','button_highlighted.png'):
        src=load(f'recipe_book/{fname}')
        if src is None: put(f'recipe_book/{fname}', placeholder(20,20)); continue
        im=src.copy(); px=im.load(); w,h=im.size; lit='highlighted' in fname
        for y in range(h):
            for x in range(w):
                p=px[x,y]
                if p[3]==0: continue
                l=lum(p)
                if l<100: px[x,y]=STEEL_DEEP
                elif l>195: px[x,y]=mix(STEEL_LIGHT,STEEL_NEAR,0.3)
                else:
                    c=panel_color(x,y,21.0)
                    if lit: c=mix(c,STEEL_NEAR,0.18)
                    px[x,y]=c
        put(f'recipe_book/{fname}', im)
    # --- v0.17.2 new sprites (placeholders steel if no vanilla)
    extra = [
        'container/villager/trade_arrow.png','container/villager/trade_arrow_out_of_stock.png','container/villager/out_of_stock.png',
        'container/villager/experience_bar_background.png','container/villager/experience_bar_current.png','container/villager/experience_bar_result.png',
        'container/villager/discount_strikethrough.png','container/villager/scroller.png','container/villager/scroller_disabled.png',
        'container/loom/pattern.png','container/loom/banner_slot.png','container/loom/dye_slot.png','container/loom/pattern_slot.png','container/loom/error.png','container/loom/scroller.png','container/loom/scroller_disabled.png',
        'container/stonecutter/recipe.png','container/stonecutter/recipe_selected.png','container/stonecutter/recipe_highlighted.png','container/stonecutter/scroller.png','container/stonecutter/scroller_disabled.png',
        'container/cartography_table/error.png','container/cartography_table/locked.png','container/cartography_table/map.png','container/cartography_table/scaled_map.png','container/cartography_table/duplicated_map.png',
        'container/smithing/error.png',
        'recipe_book/tab.png','recipe_book/tab_selected.png','recipe_book/filter_enabled.png','recipe_book/filter_disabled.png','recipe_book/filter_enabled_highlighted.png','recipe_book/filter_disabled_highlighted.png','recipe_book/overlay_recipe.png',
        'container/inventory/effect_background_small.png','container/inventory/effect_background_large.png',
    ]
    # creative tabs 1.20.x — 28 sprites
    for i in range(1,8):
        for pos in ('top','bottom'):
            for sel in ('selected','unselected'):
                extra.append(f'container/creative_inventory/tab_{pos}_{sel}_{i}.png')
    for rel in extra:
        src=load(rel)
        if src:
            # remap luminance to steel
            put(rel, repaint_luminance(src))
        else:
            # placeholder dims: 16x16 default, creative tabs 26x32 etc.
            if 'creative_inventory/tab_' in rel: put(rel, placeholder(26,32))
            elif 'effect_background' in rel: put(rel, placeholder(32,32))
            elif 'overlay_recipe' in rel: put(rel, placeholder(26,16))
            else: put(rel, placeholder(16,16))
        # ensure mcmeta for 9-slice where needed
        if rel=='recipe_book/overlay_recipe.png':
            mc = os.path.join(dst, rel+'.mcmeta')
            if not os.path.exists(mc):
                with open(mc,'w') as f: f.write('{"gui":{"scaling":"nine_slice","width":26,"height":16,"border":{"left":4,"top":4,"right":4,"bottom":4}}}')

# ---------------------------------------------------------------- creative
def repaint_creative_bg(src_path, seed):
    im=Image.open(src_path).convert('RGBA')
    px=im.load(); w,h=im.size
    xs=[x for y in range(h) for x in range(w) if px[x,y][3]>0]
    ys=[y for y in range(h) for x in range(w) if px[x,y][3]>0]
    if not xs: return im
    x0,y0,x1,y1=min(xs),min(ys),max(xs),max(ys)
    for y in range(y0,y1+1):
        for x in range(x0,x1+1):
            p=px[x,y]
            if p[3]==0: continue
            if x==x0 or y==y0 or x==x1 or y==y1: px[x,y]=STEEL_EDGE; continue
            l=lum(p)
            if l<100: px[x,y]=STEEL_DEEP
            elif l>195: px[x,y]=mix(STEEL_LIGHT,STEEL_NEAR,0.4)
            else: px[x,y]=panel_color(x,y,seed)
    return im

def build_creative(vanilla_119_gui, out_dir):
    # v0.17.2: ya no existe tabs.png en 1.20.1 — no generar
    candidates = ['tab_inventory.png','tab_items.png','tab_item_search.png']
    for idx, fname in enumerate(candidates):
        sp = None
        if vanilla_119_gui and os.path.exists(os.path.join(vanilla_119_gui, 'container/creative_inventory', fname)):
            sp = os.path.join(vanilla_119_gui, 'container/creative_inventory', fname)
        elif os.path.exists(os.path.join(out_dir, 'assets/minecraft/textures/gui/container/creative_inventory', fname)):
            sp = os.path.join(out_dir, 'assets/minecraft/textures/gui/container/creative_inventory', fname)
        if sp and os.path.exists(sp):
            im=repaint_creative_bg(sp, 25.0+idx*3)
            dst=os.path.join(out_dir, 'assets/minecraft/textures/gui/container/creative_inventory', fname)
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            im.save(dst)
            ALPHA_HASHES[f'creative/{fname}']=alpha_hash(im)
            print(f"  creative {fname} {im.size} alpha={ALPHA_HASHES[f'creative/{fname}']}")
    # elimina tabs.png huérfano si existe
    orphan=os.path.join(out_dir, 'assets/minecraft/textures/gui/container/creative_inventory/tabs.png')
    if os.path.exists(orphan):
        os.remove(orphan)
        print("  removed orphan creative/tabs.png")

PACK_MCMETA = """{
    "pack": {
        "pack_format": 15,
        "description": "Acero de cuarentena \\u2014 contenedores e interfaces para Eternal Craft: Gripe Verde. EC-QV/1897"
    }
}
"""

def build_pack_icon(out_dir):
    im=Image.new('RGBA',(128,128),(0,0,0,0)); px=im.load()
    for y in range(16,112):
        for x in range(16,112):
            if x in (16,111) or y in (16,111): c=STEEL_EDGE
            elif x in (17,110) or y in (17,110): c=mix(STEEL_LIGHT,STEEL_NEAR,0.4) if (x==17 or y==17) else STEEL_DARK
            else: c=steel_hue(brush_row_lum(y,3.0,150))
            px[x,y]=c
    for yy in range(34,78):
        px[24,yy]=(106,138,72); px[25,yy]=(70,94,52)
    for (rx,ry) in ((24,24),(104,24),(24,104),(104,104)):
        px[rx,ry]=STEEL_LIGHT; px[rx+1,ry]=STEEL_DEEP; px[rx,ry+1]=STEEL_DEEP; px[rx+1,ry+1]=STEEL_EDGE
    for x in range(60,92): px[x,60]=STEEL_DEEP; px[x,90]=STEEL_DEEP
    for y in range(60,91): px[60,y]=STEEL_DEEP; px[91,y]=STEEL_DEEP
    for y in range(66,88): px[75,y]=STEEL_LIGHT
    for x in range(66,86): px[x,76]=STEEL_LIGHT
    im.save(os.path.join(out_dir,'pack.png'))
    print("  pack.png 128x128")

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('--vanilla', required=False, default=None)
    ap.add_argument('--vanilla119', required=False, default=None)
    ap.add_argument('--out', required=True)
    args=ap.parse_args()
    os.makedirs(args.out, exist_ok=True)
    with open(os.path.join(args.out,'pack.mcmeta'),'w',encoding='utf-8') as f: f.write(PACK_MCMETA)
    print("== containers ==")
    build_containers(args.vanilla, args.out)
    print("== sprites ==")
    build_sprites(args.vanilla, args.out)
    print("== creative ==")
    build_creative(args.vanilla119, args.out)
    print("== icon ==")
    build_pack_icon(args.out)
    print("\nDONE. Output:", args.out)

if __name__=='__main__':
    main()
