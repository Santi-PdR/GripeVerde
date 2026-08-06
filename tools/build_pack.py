#!/usr/bin/env python3
"""
Eternal Craft: Gripe Verde — Master Resource Pack Generator (Design System Edition)
Reconstructs all 21 vanilla container textures based on the validated Master Steel Design System.

Design Principles:
  - Lighter, clean, modern-industrial rolled steel (#909398) with subtle 1px bevels
  - Relational palette with perfect text readability (#404040 labels have 3.33:1 contrast)
  - 100% pristine clean 3D stamped-metal slots (#606368) without dirty shadows
  - Bronze ONLY on functional moving indicators (arrows, needles, capillary pipes)
  - Zero fake drawings, zero artificial clutter, zero scanlines
  - 100% vanilla coordinates, dimensions, and alpha channel preservation
"""

import os
import sys
import shutil
from png_codec import decode_png, encode_png_rgba

VANILLA_DIR = os.path.join(os.path.dirname(__file__), 'vanilla')
OUTPUT_DIR = os.path.join(
    os.path.dirname(__file__),
    '..', 'src', 'main', 'resources', 'resourcepacks', 'gripeverde_theme',
    'assets', 'minecraft', 'textures', 'gui', 'container'
)
SOPHISTICATED_OUTPUT_DIR = os.path.join(
    os.path.dirname(__file__),
    '..', 'src', 'main', 'resources', 'resourcepacks', 'gripeverde_theme',
    'assets', 'sophisticatedcore'
)

# ── Master Relational Palette (Design System) ──────────────────────────────
# Base Steel (B): Clean, lighter, elegant rolled industrial steel plate
C_BASE        = (0x90, 0x93, 0x98)  # #909398 (V: ~60%, S: ~5%, H: 218°)
C_LGT         = (0xAC, 0xB0, 0xB4)  # B + 12% (1px Top/Left highlight bevel)
C_DRK         = (0x70, 0x73, 0x78)  # B - 14% (1px Bottom/Right shadow bevel)
C_OUTER       = (0x24, 0x26, 0x2A)  # B - 45% (1px Outer iron silhouette)

# Master Slot (S = B - 22%): Calibrated for optimal dark & bright item contrast
C_SLOT_BG     = (0x60, 0x63, 0x68)  # #606368 (Fondo limpio de slot 16x16)
C_SLOT_SHAD   = (0x40, 0x43, 0x48)  # S - 16% (1px Sombra interior sup/izq)
C_SLOT_LGT    = (0x88, 0x8B, 0x90)  # S + 18% (1px Luz interior inf/der)

# Output Slot (Reinforced 26x26)
C_OUT_LGT     = (0xB8, 0xBC, 0xC0)  # Output slot bright metallic collar
C_OUT_SHAD    = (0x38, 0x3B, 0x40)  # Output slot outer shadow

# Deep Wells / Player Preview Backing
C_DEEP_RECESS = (0x24, 0x26, 0x2A)  # #24262A (Dark well backing)

# Functional Bronze (ONLY for moving/progress mechanisms)
C_BRONZE      = (0xCC, 0xA8, 0x58)  # #CCA858 (Warm antique brass/bronze)
C_BRONZE_DK   = (0x64, 0x50, 0x24)  # Sombra de flecha/mecanismo
C_BRONZE_LT   = (0xEA, 0xC6, 0x74)  # Luz de flecha/mecanismo

# Occult / Arcane Palette (Enchanting Table)
C_ARCANE_BASE = (0x32, 0x2C, 0x3C)  # Darkened violet obsidian iron
C_ARCANE_EDGE = (0x20, 0x1A, 0x2A)  # Deep arcane shadow
C_ARCANE_SILV = (0x94, 0x8C, 0xA4)  # Occult moon-silver filigree
C_ARCANE_GOLD = (0xBA, 0xA4, 0x68)  # Arcane brass border
C_ARCANE_HV_B = (0x4A, 0x3E, 0x5C)  # Hovered spell bar violet glow
C_ARCANE_HV_L = (0xB4, 0xA8, 0xD0)  # Hovered spell bar highlight

# Drafting Navigation Slate (Cartography Table)
C_DRAFT_BG    = (0x44, 0x48, 0x50)  # Dark blue-gray navigation steel plate
C_DRAFT_EDGE  = (0x2C, 0x30, 0x36)  # Viewport dark border

# Slate Plate (Loom & Stonecutter)
C_SLATE_BG    = (0x48, 0x4C, 0x54)  # Slate steel background
C_SLATE_EDGE  = (0x30, 0x34, 0x3C)  # Slate dark bevel

# Furnace Fire & Embers
C_FIRE_EMBER  = (0x8A, 0x20, 0x08)  # Deep glowing ember
C_FIRE_RED    = (0xC8, 0x3A, 0x10)  # Coal flame red
C_FIRE_ORANGE = (0xF8, 0x7A, 0x18)  # Active flame orange
C_FIRE_YELLOW = (0xFF, 0xBA, 0x30)  # Hot flame yellow
C_FIRE_CORE   = (0xFF, 0xEE, 0x78)  # Core flame highlight


def process_container(name):
    """Processes a single container texture to Master Steel Design System standards."""
    in_path = os.path.join(VANILLA_DIR, 'dispenser.png' if name == 'dropper.png' else name)
    if not os.path.exists(in_path):
        raise FileNotFoundError(f"Vanilla source not found: {in_path}")
    
    with open(in_path, 'rb') as f:
        data = f.read()
    
    w, h, rgba = decode_png(data)
    out_rgba = bytearray(rgba)

    def get_pixel(x, y):
        if 0 <= x < w and 0 <= y < h:
            idx = (y * w + x) * 4
            return (rgba[idx], rgba[idx+1], rgba[idx+2], rgba[idx+3])
        return (0, 0, 0, 0)

    def set_pixel(x, y, rgb, a=255):
        if 0 <= x < w and 0 <= y < h:
            idx = (y * w + x) * 4
            out_rgba[idx] = max(0, min(255, rgb[0]))
            out_rgba[idx+1] = max(0, min(255, rgb[1]))
            out_rgba[idx+2] = max(0, min(255, rgb[2]))
            out_rgba[idx+3] = max(0, min(255, a))

    def is_vanilla_slot_interior(x, y):
        p = get_pixel(x, y)
        return p[3] == 255 and (p[0], p[1], p[2]) in ((0x8B, 0x8B, 0x8B), (0x50, 0x50, 0x50))

    # ── Phase 1: Structural Steel Remapping ───────────────────────────────────
    for y in range(h):
        for x in range(w):
            r, g, b, a = get_pixel(x, y)
            
            if a == 0 or a < 255:
                continue

            color = (r, g, b)

            # Outer silhouette / border (#000000)
            if color == (0x00, 0x00, 0x00):
                set_pixel(x, y, C_OUTER)

            # Base Panel Plate (#C6C6C6) -> Clean, light, modern-industrial steel
            elif color == (0xC6, 0xC6, 0xC6):
                set_pixel(x, y, C_BASE)

            # Slot interior well (#8B8B8B / #505050)
            elif color == (0x8B, 0x8B, 0x8B) or color == (0x50, 0x50, 0x50):
                set_pixel(x, y, C_SLOT_BG)

            # Slot outer drop shadow rim (#373737)
            elif color == (0x37, 0x37, 0x37):
                set_pixel(x, y, C_SLOT_SHAD)

            # Highlight bevels (#FFFFFF)
            elif color == (0xFF, 0xFF, 0xFF):
                if (x > 0 and is_vanilla_slot_interior(x-1, y)) or (y > 0 and is_vanilla_slot_interior(x, y-1)):
                    set_pixel(x, y, C_SLOT_LGT)
                else:
                    set_pixel(x, y, C_LGT)

            # Deep dark wells / player preview backing (#212121)
            elif color == (0x21, 0x21, 0x21):
                set_pixel(x, y, C_DEEP_RECESS)

            # Inset dark shadows / borders (#555555 / #51493a)
            elif color in ((0x51, 0x49, 0x3A), (0x40, 0x40, 0x40), (0x55, 0x55, 0x55)):
                set_pixel(x, y, C_DRK)

            # Vanilla parchment/wood colors remapping
            elif color == (0xD6, 0xBE, 0x96): # Map paper in cartography
                set_pixel(x, y, C_DRAFT_BG)
            elif color == (0x99, 0x87, 0x6C):
                set_pixel(x, y, C_DRAFT_EDGE)
            elif color in ((0xA0, 0x91, 0x72), (0xE0, 0xCA, 0x9F)):
                if name == 'enchanting_table.png':
                    set_pixel(x, y, C_ARCANE_BASE)
                else:
                    set_pixel(x, y, C_SLATE_BG)
            elif color in ((0x6B, 0x61, 0x4C), (0x54, 0x4C, 0x3B), (0x21, 0x1D, 0x17)):
                if name == 'enchanting_table.png':
                    set_pixel(x, y, C_ARCANE_EDGE)
                else:
                    set_pixel(x, y, C_SLATE_EDGE)
            elif color == (0xB6, 0x88, 0xAE):
                if name == 'enchanting_table.png':
                    set_pixel(x, y, C_ARCANE_HV_B)
                else:
                    set_pixel(x, y, (0x5A, 0x60, 0x6C))
            elif color == (0xFA, 0xB9, 0xEF):
                if name == 'enchanting_table.png':
                    set_pixel(x, y, C_ARCANE_HV_L)
                else:
                    set_pixel(x, y, (0x8A, 0x92, 0xA0))
            elif color == (0x65, 0x4C, 0x61):
                set_pixel(x, y, (0x1C, 0x16, 0x24))

    # ── Phase 2: Eliminate Crumpled Ghost Shadows in ALL Slots ───────────────
    for y in range(h):
        for x in range(w):
            r, g, b, a = get_pixel(x, y)
            if a == 255:
                if (r, g, b) in ((0x54, 0x54, 0x54), (0x6F, 0x6F, 0x6F), (0x70, 0x70, 0x70), (0x50, 0x50, 0x50)):
                    set_pixel(x, y, C_SLOT_BG)
                elif (r, g, b) in ((0x99, 0x99, 0x99), (0xB7, 0xB7, 0xB7)):
                    set_pixel(x, y, C_SLOT_BG)

    # ── Phase 3: Clean Functional Enhancements ────────────────────────────────

    # 1. Enchanting Table: Spell Button Sprites & Clean Lapis Socket
    if name == 'enchanting_table.png':
        for sy in range(166, 224):
            for sx in range(0, 108):
                p = get_pixel(sx, sy)
                if p[3] == 255:
                    r, g, b = p[0], p[1], p[2]
                    if 185 <= sy <= 203: # Disabled spell bar
                        if (r, g, b) in ((0x51, 0x49, 0x3A), (0x21, 0x1D, 0x17)):
                            set_pixel(sx, sy, C_ARCANE_EDGE)
                        elif (r, g, b) in ((0xA0, 0x91, 0x72), (0xE0, 0xCA, 0x9F)):
                            set_pixel(sx, sy, C_ARCANE_BASE)
                        else:
                            set_pixel(sx, sy, C_ARCANE_SILV)
                    elif 204 <= sy <= 222: # Hovered spell bar
                        if (r, g, b) in ((0x65, 0x4C, 0x61), (0x21, 0x1D, 0x17)):
                            set_pixel(sx, sy, C_ARCANE_EDGE)
                        elif (r, g, b) in ((0xB6, 0x88, 0xAE), (0xFA, 0xB9, 0xEF)):
                            set_pixel(sx, sy, C_ARCANE_HV_L)
                        else:
                            set_pixel(sx, sy, C_ARCANE_HV_B)
                    else: # Active spell bar
                        if (r, g, b) == (0x37, 0x37, 0x37):
                            set_pixel(sx, sy, C_ARCANE_EDGE)
                        elif (r, g, b) == (0xFF, 0xFF, 0xFF):
                            set_pixel(sx, sy, C_ARCANE_GOLD)
                        elif (r, g, b) == (0x8B, 0x8B, 0x8B):
                            set_pixel(sx, sy, C_ARCANE_BASE)
                        elif (r, g, b) == (0xC6, 0xC6, 0xC6):
                            set_pixel(sx, sy, C_BASE)

        # Lapis Slot at (35, 47)
        for ly in range(47, 64):
            for lx in range(35, 53):
                p = get_pixel(lx, ly)
                if p[3] == 255:
                    if lx in (35, 51) or ly in (47, 63):
                        if (p[0], p[1], p[2]) == (0xFF, 0xFF, 0xFF):
                            set_pixel(lx, ly, C_SLOT_LGT)
                        elif (p[0], p[1], p[2]) == (0x37, 0x37, 0x37):
                            set_pixel(lx, ly, C_SLOT_SHAD)
                    else:
                        set_pixel(lx, ly, (0x30, 0x2A, 0x38))

    # 2. Brewing Stand: Clean Distillation Pipes & Clean Flask Slots
    elif name == 'brewing_stand.png':
        for fy in range(18, 34):
            for fx in range(18, 34):
                set_pixel(fx, fy, C_SLOT_BG)
        for cx, cy in [(57, 47), (80, 54), (103, 47)]:
            for dy in range(16):
                for dx in range(16):
                    set_pixel(cx + dy, cy + dx, C_SLOT_BG)

        for by in range(14, 56):
            for bx in range(55, 116):
                p = get_pixel(bx, by)
                if p[3] == 255:
                    r, g, b = p[0], p[1], p[2]
                    if (r, g, b) == (0x37, 0x37, 0x37):
                        set_pixel(bx, by, C_OUTER)
                    elif (r, g, b) == (0xFF, 0xFF, 0xFF):
                        set_pixel(bx, by, C_BRONZE_LT)
                    elif (r, g, b) == (0x8B, 0x8B, 0x8B):
                        is_narrow_pipe = (get_pixel(bx-1, by)[0] in (0x37, 0x18, 0x24) or get_pixel(bx+1, by)[0] in (0x37, 0x18, 0x24) or
                                          get_pixel(bx, by-1)[0] in (0x37, 0x18, 0x24) or get_pixel(bx, by+1)[0] in (0x37, 0x18, 0x24))
                        if is_narrow_pipe:
                            set_pixel(bx, by, C_BRONZE)
                        else:
                            set_pixel(bx, by, C_BASE)

    # 3. Crafting Table: Clean Functional Bronze Arrow
    elif name == 'crafting_table.png':
        for ay in range(30, 52):
            for ax in range(86, 116):
                p = get_pixel(ax, ay)
                if p[3] == 255:
                    if (p[0], p[1], p[2]) == (0x8B, 0x8B, 0x8B):
                        set_pixel(ax, ay, C_BRONZE)
                    elif (p[0], p[1], p[2]) == (0x37, 0x37, 0x37):
                        set_pixel(ax, ay, C_OUTER)
                    elif (p[0], p[1], p[2]) == (0xFF, 0xFF, 0xFF):
                        set_pixel(ax, ay, C_BRONZE_LT)

    # 4. Furnace Family: Firebox & Coal Flame Gradients
    elif name in ('furnace.png', 'blast_furnace.png', 'smoker.png'):
        for ay in range(14, 31):
            for ax in range(176, 200):
                p = get_pixel(ax, ay)
                if p[3] == 255:
                    if (p[0], p[1], p[2]) in ((0x8B, 0x8B, 0x8B), (0xC6, 0xC6, 0xC6)):
                        set_pixel(ax, ay, C_BRONZE)
                    elif (p[0], p[1], p[2]) == (0x37, 0x37, 0x37):
                        set_pixel(ax, ay, C_OUTER)
                    elif (p[0], p[1], p[2]) == (0xFF, 0xFF, 0xFF):
                        set_pixel(ax, ay, C_BRONZE_LT)
        
        for fy in range(0, 14):
            for fx in range(176, 190):
                p = get_pixel(fx, fy)
                if p[3] == 255:
                    r, g, b = p[0], p[1], p[2]
                    if r > 180 and g < 90: set_pixel(fx, fy, C_FIRE_RED)
                    elif r > 180 and 90 <= g < 160: set_pixel(fx, fy, C_FIRE_ORANGE)
                    elif r > 180 and 160 <= g < 220: set_pixel(fx, fy, C_FIRE_YELLOW)
                    elif r >= 220 and g >= 220: set_pixel(fx, fy, C_FIRE_CORE)

    # 5. Loom: Clean Input Slots
    elif name == 'loom.png':
        for sy, sx in [(26, 13), (26, 33), (45, 23)]:
            for dy in range(1, 17):
                for dx in range(1, 17):
                    set_pixel(sx + dx, sy + dy, C_SLOT_BG)

    # 6. Smithing Table: Clean Input Slots
    elif name == 'smithing.png':
        for sy, sx in [(48, 8), (48, 26), (48, 44)]:
            for dy in range(1, 17):
                for dx in range(1, 17):
                    set_pixel(sx + dx, sy + dy, C_SLOT_BG)

    # 7. Inventory: Clean Equipment Slots
    elif name == 'inventory.png':
        for sy, sx in [(62, 77), (18, 98), (18, 116), (36, 98), (36, 116), (28, 154)]:
            for dy in range(1, 17):
                for dx in range(1, 17):
                    set_pixel(sx + dx, sy + dy, C_SLOT_BG)

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    out_file = os.path.join(OUTPUT_DIR, name)
    encoded = encode_png_rgba(w, h, out_rgba)
    with open(out_file, 'wb') as f:
        f.write(encoded)
    
    return w, h, len(encoded)


def build_all():
    if os.path.exists(SOPHISTICATED_OUTPUT_DIR):
        shutil.rmtree(SOPHISTICATED_OUTPUT_DIR)
        print("  ✓ Ensured no third-party mod overrides exist.")

    containers = [
        'anvil.png', 'beacon.png', 'blast_furnace.png', 'brewing_stand.png',
        'cartography_table.png', 'crafting_table.png', 'dispenser.png', 'dropper.png',
        'enchanting_table.png', 'furnace.png', 'generic_54.png', 'grindstone.png',
        'hopper.png', 'horse.png', 'inventory.png', 'loom.png', 'shulker_box.png',
        'smithing.png', 'smoker.png', 'stonecutter.png', 'villager2.png'
    ]

    print("=================================================================")
    print(" Eternal Craft: Gripe Verde — Master Steel Design System Build")
    print("=================================================================")
    
    count = 0
    for c in containers:
        try:
            w, h, sz = process_container(c)
            print(f"  ✓ {c:22s} ({w:3d}x{h:3d}) -> {sz:6d} bytes")
            count += 1
        except Exception as e:
            print(f"  ✗ {c:22s} FAILED: {e}")
            import traceback
            traceback.print_exc()

    print(f"\nSuccessfully generated {count}/{len(containers)} containers in: {OUTPUT_DIR}")


if __name__ == '__main__':
    build_all()
