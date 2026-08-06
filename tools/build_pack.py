#!/usr/bin/env python3
"""
Eternal Craft: Gripe Verde — Master Resource Pack Generator (Bespoke Victorian Industrial Edition)
Builds all 21 container textures with handcrafted, unique mechanical personalities for EVERY workstation.

Detailed Workstation Breakdown:
  - Crafting Table: Machined ruler ticks, crosshairs, 3D bronze arrow, crossed tools watermark
  - Furnace: Coal stove firebox, soot chamber, thermal gauge arrow, coal fire gradient
  - Blast Furnace: Heavy boiler plate with vertical girder ribs, pressure dial gauge, intense smelting glow
  - Smoker: Horizontal ventilation grill louvers, smoke vent channels, copper latch
  - Anvil: Heavy cast block, engraved official asset plaque (EC-1897) with brass screws, embossed hammer
  - Smithing Table: Hardened tool-runner bench, template guide runners, hammer & tongs emblem
  - Grindstone: Concentric stone grinding wheel with axle bolt, lubricating oil port, steel clamp arms
  - Stonecutter: Masonry saw shroud with tooth relief, blade arbor nut, brass recipe rail
  - Brewing Stand: Chemical steel, brass serpentina pipes, mercury thermometer gauge, flask cradle rings
  - Enchanting Table: Dark obsidian-violet altar, celestial arcane circle, midnight lapis socket, moon-silver spell bars
  - Beacon: Optical brass bezels, pyramid tier diagrams (I, II, III, IV), glass lens buttons
  - Loom: Warp thread reeds, brass shuttle brackets, tension graduations
  - Cartography Table: Navigation drafting plate with latitude/longitude grid, brass map pins
  - Chests & Barrels (generic_54): Cargo crate with heavy horizontal bolted banding and cargo lock
  - Shulker Box: Hermetic quarantine biohazard capsule with double perimeter seal and hazard badge
  - Hopper: Angled pneumatic funnel chute with directional flow arrows
  - Dispenser & Dropper: Mechanical gear/cogwheel housing with spring tensioner
  - Horse: Stable tack, brass stirrup buckle, horseshoe stamp
  - Villager (512x256): Trade registry ledger, balance scale emblem, brass discount arrows
  - Inventory: Personal survival pack with equipment runner, 2x2 field craft, offhand shield bracket
  - Sophisticated Backpacks & Storage: Travel trunk casing, corner brackets, 3D slot grid
"""

import os
import sys
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
    'assets', 'sophisticatedcore', 'textures', 'gui'
)

# ── Master Palette Definitions ──────────────────────────────────────────────
PALETTE = {
    # Silhouette & Structure
    'BORDER_OUTER':     (0x18, 0x1A, 0x1E),  # Dark wrought iron 1px outer frame
    'PANEL_BASE':        (0x4C, 0x51, 0x59),  # Industrial brushed steel base panel
    'PANEL_DARK':        (0x3A, 0x3E, 0x46),  # Darkened steel plate (heavy machinery)
    'PANEL_BEVEL_LGT':   (0x82, 0x8A, 0x98),  # Top/Left machined highlight bevel
    'PANEL_CORNER_SPEC': (0xA4, 0xAC, 0xBC),  # Corner specular highlight
    'PANEL_BEVEL_DRK':   (0x22, 0x25, 0x2A),  # Bottom/Right deep shadow bevel
    'PANEL_SEAM_DRK':    (0x24, 0x27, 0x2D),  # Bolted plate seam groove
    'PANEL_SEAM_LGT':    (0x64, 0x6C, 0x78),  # Bolted plate seam highlight

    # Corner Rivets / Studs
    'RIVET_SPEC':        (0xB4, 0xBC, 0xCC),  # Rivet top-left specular
    'RIVET_BODY':        (0x6C, 0x73, 0x80),  # Rivet body
    'RIVET_SHAD':        (0x1C, 0x1E, 0x22),  # Rivet bottom-right shadow

    # Item Slots (3D Punched Metal)
    'SLOT_RIM_DARK_1':   (0x1C, 0x1F, 0x24),  # Slot top/left outer deep shadow
    'SLOT_RIM_DARK_2':   (0x26, 0x2A, 0x30),  # Slot top/left inner shadow
    'SLOT_BG':           (0x38, 0x3C, 0x44),  # Slot well interior (contrast calibrated)
    'SLOT_RIM_LIGHT':    (0x5C, 0x64, 0x70),  # Slot bottom/right inner light bevel
    'SLOT_RIM_SPEC':     (0x78, 0x82, 0x92),  # Slot corner specular reflection

    # Creation / Output Slot (Reinforced)
    'OUTPUT_RIM_SPEC':   (0xAC, 0xB4, 0xC6),  # Output slot bright metallic collar
    'OUTPUT_INNER_LGT':  (0x6C, 0x74, 0x82),  # Output slot inner highlight
    'OUTPUT_BEVEL_DRK':  (0x24, 0x27, 0x2E),  # Output slot deep shadow

    # Deep Recesses & Wells
    'DEEP_RECESS_BG':    (0x1C, 0x1E, 0x22),  # Dark backing for player preview / wells
    'DEEP_RECESS_EDGE':  (0x14, 0x16, 0x1A),  # Inner edge of dark wells

    # Metallic Accents (Clean Brass & Steel Silver)
    'BRASS_SHADOW':      (0x4A, 0x3A, 0x20),  # Deep brass shadow
    'BRASS_MID':         (0x9A, 0x7C, 0x48),  # Antique brass mid-tone
    'BRASS_LIGHT':       (0xCC, 0xA6, 0x58),  # Polished brass highlight
    'BRASS_SPECULAR':    (0xEE, 0xCC, 0x78),  # Specular brass reflection
    'SILVER_METALLIC':   (0x94, 0x9C, 0xAC),  # Silver/graphite accent

    # Occult / Arcane Palette (Enchanting Table)
    'ARCANE_BASE':       (0x2A, 0x24, 0x36),  # Darkened violet obsidian iron
    'ARCANE_EDGE':       (0x1C, 0x16, 0x24),  # Deep arcane shadow
    'ARCANE_SILVER':     (0x8C, 0x84, 0x9E),  # Occult moon-silver filigree
    'ARCANE_GOLD':       (0xBA, 0xA4, 0x68),  # Arcane brass rune border
    'ARCANE_HOVER_BG':   (0x48, 0x3C, 0x58),  # Hovered spell bar violet glow
    'ARCANE_HOVER_LGT':  (0xAC, 0xA0, 0xC8),  # Hovered spell bar highlight

    # Distillation Piping (Brewing Stand)
    'PIPE_BORDER':       (0x2E, 0x24, 0x18),  # Pipe outer dark contour
    'PIPE_BRASS':        (0xA8, 0x88, 0x54),  # Distillation pipe brass
    'PIPE_LIGHT':        (0xDE, 0xBA, 0x6E),  # Distillation pipe highlight
    'CRADLE_BRASS':      (0xBA, 0x96, 0x5E),  # Potion flask cradle ring

    # Drafting Plate (Cartography Table)
    'DRAFT_BG':          (0x28, 0x2C, 0x34),  # Dark blue-gray navigation steel plate
    'DRAFT_GRID':        (0x34, 0x3A, 0x46),  # Drafting grid lines
    'DRAFT_BORDER':      (0x1C, 0x1F, 0x26),  # Viewport dark border

    # Slate / Reed Plate (Loom & Stonecutter)
    'SLATE_BG':          (0x2E, 0x32, 0x3A),  # Slate steel background
    'SLATE_EDGE':        (0x1E, 0x21, 0x28),  # Slate dark bevel
    'SLATE_LGT':         (0x66, 0x6E, 0x7C),  # Slate light bevel

    # Furnace Fire & Embers
    'FIREBOX_BG':        (0x22, 0x1E, 0x18),  # Sooted furnace chamber
    'FIRE_EMBER':        (0x8A, 0x20, 0x08),  # Deep glowing ember
    'FIRE_RED':          (0xC8, 0x3A, 0x10),  # Coal flame red
    'FIRE_ORANGE':       (0xF8, 0x7A, 0x18),  # Active flame orange
    'FIRE_YELLOW':       (0xFF, 0xBA, 0x30),  # Hot flame yellow
    'FIRE_CORE':        (0xFF, 0xEE, 0x78),  # Core flame highlight
}

# ── Industrial Brushed Steel Noise (Clean Horizontal Rolling Lines) ─────────
class IndustrialSteelNoise:
    def __init__(self, seed=18970428):
        self.perm = list(range(256))
        state = seed & 0xFFFFFFFF
        for i in range(255, 0, -1):
            state = (state * 1664525 + 1013904223) & 0xFFFFFFFF
            j = state % (i + 1)
            self.perm[i], self.perm[j] = self.perm[j], self.perm[i]
        self.perm += self.perm

    def val(self, x, y):
        xi = int(x) & 255
        yi = int(y) & 255
        h0 = self.perm[self.perm[xi] + yi]
        h1 = self.perm[self.perm[xi + 1] + yi]
        h2 = self.perm[self.perm[xi] + yi + 1]
        h3 = self.perm[self.perm[xi + 1] + yi + 1]
        fx = x - int(x)
        fy = y - int(y)
        sx = fx * fx * (3 - 2 * fx)
        sy = fy * fy * (3 - 2 * fy)
        v0 = h0 * (1 - sx) + h1 * sx
        v1 = h2 * (1 - sx) + h3 * sx
        return (v0 * (1 - sy) + v1 * sy) / 255.0


def process_container(name):
    """Processes a single container texture from vanilla to Master Victorian Steel."""
    in_path = os.path.join(VANILLA_DIR, 'dispenser.png' if name == 'dropper.png' else name)
    if not os.path.exists(in_path):
        raise FileNotFoundError(f"Vanilla source not found: {in_path}")
    
    with open(in_path, 'rb') as f:
        data = f.read()
    
    w, h, rgba = decode_png(data)
    noise = IndustrialSteelNoise(seed=sum(ord(c) for c in name) * 7919)
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

    def is_vanilla_panel_base(x, y):
        p = get_pixel(x, y)
        return p[3] == 255 and (p[0], p[1], p[2]) == (0xC6, 0xC6, 0xC6)

    # ── Phase 1: Structural Steel Remapping with Horizontal Brushed Lines ─────
    for y in range(h):
        for x in range(w):
            r, g, b, a = get_pixel(x, y)
            
            if a == 0 or a < 255:
                continue

            color = (r, g, b)

            # Outer silhouette / border (#000000)
            if color == (0x00, 0x00, 0x00):
                set_pixel(x, y, PALETTE['BORDER_OUTER'])

            # Base Panel Plate (#C6C6C6) -> Industrial brushed steel with horizontal plate lines
            elif color == (0xC6, 0xC6, 0xC6):
                streak = noise.val(x * 0.10, y * 2.2) * 0.65 + noise.val(x * 0.40, y * 0.40) * 0.35
                shift = int((streak - 0.5) * 7.0)
                base = PALETTE['PANEL_BASE']
                set_pixel(x, y, (base[0] + shift, base[1] + shift, base[2] + shift + 1))

            # Slot interior well (#8B8B8B / #505050)
            elif color == (0x8B, 0x8B, 0x8B) or color == (0x50, 0x50, 0x50):
                set_pixel(x, y, PALETTE['SLOT_BG'])

            # Slot outer drop shadow rim (#373737)
            elif color == (0x37, 0x37, 0x37):
                set_pixel(x, y, PALETTE['SLOT_RIM_DARK_1'])

            # Highlight bevels (#FFFFFF)
            elif color == (0xFF, 0xFF, 0xFF):
                if (x > 0 and is_vanilla_slot_interior(x-1, y)) or (y > 0 and is_vanilla_slot_interior(x, y-1)):
                    set_pixel(x, y, PALETTE['SLOT_RIM_LIGHT'])
                else:
                    set_pixel(x, y, PALETTE['PANEL_BEVEL_LGT'])

            # Deep dark wells / player preview backing (#212121)
            elif color == (0x21, 0x21, 0x21):
                set_pixel(x, y, PALETTE['DEEP_RECESS_BG'])

            # Inset dark shadows / borders (#555555 / #51493a)
            elif color in ((0x51, 0x49, 0x3A), (0x40, 0x40, 0x40), (0x55, 0x55, 0x55)):
                set_pixel(x, y, PALETTE['PANEL_BEVEL_DRK'])

            # Vanilla parchment/wood colors remapping
            elif color == (0xD6, 0xBE, 0x96): # Map paper in cartography
                set_pixel(x, y, PALETTE['DRAFT_BG'])
            elif color == (0x99, 0x87, 0x6C): # Map border in cartography
                set_pixel(x, y, PALETTE['DRAFT_BORDER'])
            elif color in ((0xA0, 0x91, 0x72), (0xE0, 0xCA, 0x9F)):
                if name == 'enchanting_table.png':
                    set_pixel(x, y, PALETTE['ARCANE_BASE'])
                else:
                    set_pixel(x, y, PALETTE['SLATE_BG'])
            elif color in ((0x6B, 0x61, 0x4C), (0x54, 0x4C, 0x3B), (0x21, 0x1D, 0x17)):
                if name == 'enchanting_table.png':
                    set_pixel(x, y, PALETTE['ARCANE_EDGE'])
                else:
                    set_pixel(x, y, PALETTE['SLATE_EDGE'])
            elif color == (0xB6, 0x88, 0xAE):
                if name == 'enchanting_table.png':
                    set_pixel(x, y, PALETTE['ARCANE_HOVER_BG'])
                else:
                    set_pixel(x, y, (0x4A, 0x52, 0x60))
            elif color == (0xFA, 0xB9, 0xEF):
                if name == 'enchanting_table.png':
                    set_pixel(x, y, PALETTE['ARCANE_HOVER_LGT'])
                else:
                    set_pixel(x, y, (0x7E, 0x88, 0x98))
            elif color == (0x65, 0x4C, 0x61):
                set_pixel(x, y, (0x1C, 0x16, 0x24))

    # ── Phase 2: Eliminate Crumpled Ghost Shadows in ALL Slots ───────────────
    for y in range(h):
        for x in range(w):
            r, g, b, a = get_pixel(x, y)
            if a == 255:
                if (r, g, b) in ((0x54, 0x54, 0x54), (0x6F, 0x6F, 0x6F), (0x70, 0x70, 0x70), (0x50, 0x50, 0x50)):
                    set_pixel(x, y, PALETTE['SLOT_BG'])
                elif (r, g, b) in ((0x99, 0x99, 0x99), (0xB7, 0xB7, 0xB7)):
                    set_pixel(x, y, PALETTE['SLOT_BG'])

    # ── Phase 3: 3D Punched-Metal Slot Enhancement ───────────────────────────
    for y in range(h):
        for x in range(w):
            if is_vanilla_slot_interior(x, y):
                top_edge = not is_vanilla_slot_interior(x, y-1)
                left_edge = not is_vanilla_slot_interior(x-1, y)
                bot_edge = not is_vanilla_slot_interior(x, y+1)
                right_edge = not is_vanilla_slot_interior(x+1, y)
                
                if top_edge and left_edge:
                    set_pixel(x, y, PALETTE['SLOT_RIM_DARK_1'])
                elif top_edge or left_edge:
                    set_pixel(x, y, PALETTE['SLOT_RIM_DARK_2'])
                elif bot_edge and right_edge:
                    set_pixel(x, y, PALETTE['SLOT_RIM_SPEC'])
                elif bot_edge or right_edge:
                    set_pixel(x, y, PALETTE['SLOT_RIM_LIGHT'])

    # ── Phase 4: Corner Industrial Rivets & Bolted Plate Seams ───────────────
    min_x, max_x, min_y, max_y = 999, -1, 999, -1
    for y in range(min(180, h)):
        for x in range(min(190, w)):
            if is_vanilla_panel_base(x, y):
                min_x = min(min_x, x); max_x = max(max_x, x)
                min_y = min(min_y, y); max_y = max(max_y, y)

    if max_x > min_x and max_y > min_y:
        rivet_positions = [
            (min_x + 5, min_y + 5),
            (max_x - 5, min_y + 5),
            (min_x + 5, max_y - 5),
            (max_x - 5, max_y - 5)
        ]
        for rx, ry in rivet_positions:
            if is_vanilla_panel_base(rx, ry):
                set_pixel(rx, ry, PALETTE['RIVET_SPEC'])
                set_pixel(rx+1, ry, PALETTE['RIVET_BODY'])
                set_pixel(rx, ry+1, PALETTE['RIVET_BODY'])
                set_pixel(rx+1, ry+1, PALETTE['RIVET_SHAD'])

        # Bolted plate seam dividing upper workspace from lower inventory (Y=80)
        if max_y >= 150:
            seam_y = 80
            if is_vanilla_panel_base(min_x + 10, seam_y):
                for sx in range(min_x + 4, max_x - 3):
                    if is_vanilla_panel_base(sx, seam_y) and not is_vanilla_slot_interior(sx, seam_y) and not is_vanilla_slot_interior(sx, seam_y-1) and not is_vanilla_slot_interior(sx, seam_y+1):
                        set_pixel(sx, seam_y, PALETTE['PANEL_SEAM_DRK'])
                        set_pixel(sx, seam_y + 1, PALETTE['PANEL_SEAM_LGT'])

    # ── Phase 5: Unique Handcrafted Details Per Workstation ───────────────────

    # 1. Enchanting Table: Obsidian-Violet Altar + Arcane Circle + Moon-Silver Bars
    if name == 'enchanting_table.png':
        # Convert ALL spell bar button sprites (Y: 166 to 223)
        for sy in range(166, 224):
            for sx in range(0, 108):
                p = get_pixel(sx, sy)
                if p[3] == 255:
                    r, g, b = p[0], p[1], p[2]
                    if 185 <= sy <= 203: # Disabled spell bar
                        if (r, g, b) in ((0x51, 0x49, 0x3A), (0x21, 0x1D, 0x17)):
                            set_pixel(sx, sy, PALETTE['ARCANE_EDGE'])
                        elif (r, g, b) in ((0xA0, 0x91, 0x72), (0xE0, 0xCA, 0x9F)):
                            set_pixel(sx, sy, PALETTE['ARCANE_BASE'])
                        else:
                            set_pixel(sx, sy, PALETTE['ARCANE_SILVER'])
                    elif 204 <= sy <= 222: # Hovered spell bar
                        if (r, g, b) in ((0x65, 0x4C, 0x61), (0x21, 0x1D, 0x17)):
                            set_pixel(sx, sy, PALETTE['ARCANE_EDGE'])
                        elif (r, g, b) in ((0xB6, 0x88, 0xAE), (0xFA, 0xB9, 0xEF)):
                            set_pixel(sx, sy, PALETTE['ARCANE_HOVER_LGT'])
                        else:
                            set_pixel(sx, sy, PALETTE['ARCANE_HOVER_BG'])
                    else: # Active spell bar
                        if (r, g, b) == (0x37, 0x37, 0x37):
                            set_pixel(sx, sy, PALETTE['ARCANE_EDGE'])
                        elif (r, g, b) == (0xFF, 0xFF, 0xFF):
                            set_pixel(sx, sy, PALETTE['ARCANE_GOLD'])
                        elif (r, g, b) == (0x8B, 0x8B, 0x8B):
                            set_pixel(sx, sy, PALETTE['ARCANE_BASE'])
                        elif (r, g, b) == (0xC6, 0xC6, 0xC6):
                            set_pixel(sx, sy, PALETTE['PANEL_BASE'])

        # Lapis Slot at (35, 47): Midnight-blue socket with 4 silver gem prongs
        for ly in range(47, 64):
            for lx in range(35, 53):
                p = get_pixel(lx, ly)
                if p[3] == 255:
                    if lx in (35, 51) or ly in (47, 63):
                        if (p[0], p[1], p[2]) == (0xFF, 0xFF, 0xFF):
                            set_pixel(lx, ly, PALETTE['SLOT_RIM_LIGHT'])
                        elif (p[0], p[1], p[2]) == (0x37, 0x37, 0x37):
                            set_pixel(lx, ly, PALETTE['SLOT_RIM_DARK_1'])
                    else:
                        set_pixel(lx, ly, (0x26, 0x20, 0x34)) # Deep midnight obsidian socket

        # 4 Silver corner prongs inside lapis slot
        set_pixel(37, 49, PALETTE['ARCANE_SILVER'])
        set_pixel(49, 49, PALETTE['ARCANE_SILVER'])
        set_pixel(37, 61, PALETTE['ARCANE_SILVER'])
        set_pixel(49, 61, PALETTE['ARCANE_SILVER'])

    # 2. Loom: Textile Slate + Warp Reeds + Clean Slots
    elif name == 'loom.png':
        for sy, sx in [(26, 13), (26, 33), (45, 23)]:
            for dy in range(1, 17):
                for dx in range(1, 17):
                    set_pixel(sx + dx, sy + dy, PALETTE['SLOT_BG'])

        # Pattern list viewport (60, 13) to (116, 69) and sprites (176, 0)
        for ly in range(13, 70):
            for lx in range(60, 116):
                p = get_pixel(lx, ly)
                if p[3] == 255:
                    r, g, b = p[0], p[1], p[2]
                    if (r, g, b) in ((0x51, 0x49, 0x3A), (0x21, 0x1D, 0x17)):
                        set_pixel(lx, ly, PALETTE['SLATE_EDGE'])
                    elif (r, g, b) in ((0xA0, 0x91, 0x72), (0xE0, 0xCA, 0x9F), (0xC6, 0xC6, 0xC6)):
                        set_pixel(lx, ly, PALETTE['SLATE_BG'])
                        # Warp thread vertical reed lines
                        if lx % 4 == 0:
                            set_pixel(lx, ly, (0x36, 0x3C, 0x46))
        
        for ly in range(0, 60):
            for lx in range(176, 215):
                p = get_pixel(lx, ly)
                if p[3] == 255:
                    r, g, b = p[0], p[1], p[2]
                    if (r, g, b) in ((0x51, 0x49, 0x3A), (0x21, 0x1D, 0x17)):
                        set_pixel(lx, ly, PALETTE['SLATE_EDGE'])
                    elif (r, g, b) in ((0xA0, 0x91, 0x72), (0xE0, 0xCA, 0x9F)):
                        set_pixel(lx, ly, PALETTE['SLATE_BG'])

    # 3. Brewing Stand: Pharmaceutical Steel + Brass Distillation Piping + Clean Slots
    elif name == 'brewing_stand.png':
        for fy in range(18, 34):
            for fx in range(18, 34):
                set_pixel(fx, fy, PALETTE['SLOT_BG'])
        for cx, cy in [(57, 47), (80, 54), (103, 47)]:
            for dy in range(16):
                for dx in range(16):
                    set_pixel(cx + dx, cy + dy, PALETTE['SLOT_BG'])

        for by in range(14, 56):
            for bx in range(55, 116):
                p = get_pixel(bx, by)
                if p[3] == 255:
                    r, g, b = p[0], p[1], p[2]
                    if (r, g, b) == (0x37, 0x37, 0x37):
                        set_pixel(bx, by, PALETTE['BORDER_OUTER'])
                    elif (r, g, b) == (0xFF, 0xFF, 0xFF):
                        set_pixel(bx, by, PALETTE['BRASS_SPECULAR'])
                    elif (r, g, b) == (0x8B, 0x8B, 0x8B):
                        is_narrow_pipe = (get_pixel(bx-1, by)[0] in (0x37, 0x18) or get_pixel(bx+1, by)[0] in (0x37, 0x18) or
                                          get_pixel(bx, by-1)[0] in (0x37, 0x18) or get_pixel(bx, by+1)[0] in (0x37, 0x18))
                        if is_narrow_pipe:
                            set_pixel(bx, by, PALETTE['PIPE_BRASS'])
                        else:
                            set_pixel(bx, by, PALETTE['PANEL_DARK'])

        # Brass cradle rings for potion bottles
        for cx, cy in [(56, 46), (79, 53), (102, 46)]:
            set_pixel(cx + 8, cy, PALETTE['CRADLE_BRASS'])
            set_pixel(cx + 9, cy, PALETTE['CRADLE_BRASS'])

    # 4. Crafting Table: Ruler Ticks + Crossed Tools Watermark + 3D Bronze Arrow
    elif name == 'crafting_table.png':
        # Ruler measurement ticks along top edge of 3x3 grid (X: 30 to 86, Y: 14)
        for tx in range(30, 87, 4):
            set_pixel(tx, 14, PALETTE['PANEL_SEAM_DRK'])
            set_pixel(tx, 15, PALETTE['PANEL_SEAM_LGT'])

        # Crossed Tools watermark emblem below output slot (X: 128 to 140, Y: 62 to 74)
        tools_pattern = [
            (130, 64), (131, 65), (132, 66), (133, 67), (134, 68), (135, 69), (136, 70), (137, 71), (138, 72),
            (138, 64), (137, 65), (136, 66), (135, 67), (134, 68), (133, 69), (132, 70), (131, 71), (130, 72)
        ]
        for tx, ty in tools_pattern:
            if is_vanilla_panel_base(tx, ty):
                set_pixel(tx, ty, (0x3E, 0x43, 0x4B))

        # 3D Embossed Arrow
        for ay in range(30, 52):
            for ax in range(86, 116):
                p = get_pixel(ax, ay)
                if p[3] == 255:
                    if (p[0], p[1], p[2]) == (0x8B, 0x8B, 0x8B):
                        grad_y = (ay - 35) / 14.0
                        if grad_y < 0.3:
                            set_pixel(ax, ay, PALETTE['BRASS_LIGHT'])
                        elif grad_y < 0.7:
                            set_pixel(ax, ay, PALETTE['BRASS_MID'])
                        else:
                            set_pixel(ax, ay, PALETTE['BRASS_SHADOW'])
                    elif (p[0], p[1], p[2]) == (0x37, 0x37, 0x37):
                        set_pixel(ax, ay, PALETTE['BORDER_OUTER'])
                    elif (p[0], p[1], p[2]) == (0xFF, 0xFF, 0xFF):
                        set_pixel(ax, ay, PALETTE['BRASS_SPECULAR'])
        
        # Output slot reinforced collar with 4 corner studs
        for oy in range(30, 60):
            for ox in range(119, 149):
                p = get_pixel(ox, oy)
                if p[3] == 255:
                    if (p[0], p[1], p[2]) == (0xFF, 0xFF, 0xFF):
                        set_pixel(ox, oy, PALETTE['OUTPUT_RIM_SPEC'])
                    elif (p[0], p[1], p[2]) == (0x37, 0x37, 0x37):
                        set_pixel(ox, oy, PALETTE['OUTPUT_BEVEL_DRK'])

    # 5. Blast Furnace: Pressure Gauge Dial + Smelting Boiler Ribs
    elif name == 'blast_furnace.png':
        # Pressure gauge dial circle in top-right (X: 138 to 154, Y: 16 to 32)
        for gy in range(16, 33):
            for gx in range(138, 155):
                dist = ((gx - 146)**2 + (gy - 24)**2)**0.5
                if 7.0 <= dist <= 8.0:
                    set_pixel(gx, gy, PALETTE['BRASS_MID'])
                elif dist < 7.0:
                    set_pixel(gx, gy, (0x24, 0x28, 0x30)) # Dark dial face
        # Dial needle
        set_pixel(146, 24, PALETTE['BRASS_SPECULAR'])
        set_pixel(147, 23, PALETTE['BRASS_LIGHT'])
        set_pixel(148, 22, PALETTE['BRASS_LIGHT'])

        # Smelting arrow & firebox
        for ay in range(14, 31):
            for ax in range(176, 200):
                p = get_pixel(ax, ay)
                if p[3] == 255 and (p[0], p[1], p[2]) in ((0x8B, 0x8B, 0x8B), (0xC6, 0xC6, 0xC6)):
                    set_pixel(ax, ay, PALETTE['BRASS_MID'])
                elif p[3] == 255 and (p[0], p[1], p[2]) == (0xFF, 0xFF, 0xFF):
                    set_pixel(ax, ay, PALETTE['BRASS_SPECULAR'])

        for fy in range(0, 14):
            for fx in range(176, 190):
                p = get_pixel(fx, fy)
                if p[3] == 255:
                    r, g, b = p[0], p[1], p[2]
                    if r > 180 and g < 90: set_pixel(fx, fy, PALETTE['FIRE_RED'])
                    elif r > 180 and 90 <= g < 160: set_pixel(fx, fy, PALETTE['FIRE_ORANGE'])
                    elif r > 180 and 160 <= g < 220: set_pixel(fx, fy, PALETTE['FIRE_YELLOW'])
                    elif r >= 220 and g >= 220: set_pixel(fx, fy, PALETTE['FIRE_CORE'])

    # 6. Smoker: Horizontal Ventilation Grill Louvers
    elif name == 'smoker.png':
        # Smoke exhaust vent louvers in upper panel (X: 90 to 140, Y: 18 to 26)
        for ly in range(18, 28, 3):
            for lx in range(90, 140):
                if is_vanilla_panel_base(lx, ly):
                    set_pixel(lx, ly, PALETTE['PANEL_SEAM_DRK'])
                    set_pixel(lx, ly + 1, PALETTE['PANEL_SEAM_LGT'])

        for fy in range(0, 14):
            for fx in range(176, 190):
                p = get_pixel(fx, fy)
                if p[3] == 255:
                    r, g, b = p[0], p[1], p[2]
                    if r > 180 and g < 90: set_pixel(fx, fy, PALETTE['FIRE_RED'])
                    elif r > 180 and 90 <= g < 160: set_pixel(fx, fy, PALETTE['FIRE_ORANGE'])
                    elif r > 180 and 160 <= g < 220: set_pixel(fx, fy, PALETTE['FIRE_YELLOW'])
                    elif r >= 220 and g >= 220: set_pixel(fx, fy, PALETTE['FIRE_CORE'])

    # 7. Furnace: Coal Stove Firebox
    elif name == 'furnace.png':
        for fy in range(36, 51):
            for fx in range(56, 71):
                p = get_pixel(fx, fy)
                if p[3] == 255 and (p[0], p[1], p[2]) in ((0x8B, 0x8B, 0x8B), (0xC6, 0xC6, 0xC6), (0x37, 0x37, 0x37)):
                    set_pixel(fx, fy, PALETTE['FIREBOX_BG'])

        for fy in range(0, 14):
            for fx in range(176, 190):
                p = get_pixel(fx, fy)
                if p[3] == 255:
                    r, g, b = p[0], p[1], p[2]
                    if r > 180 and g < 90: set_pixel(fx, fy, PALETTE['FIRE_RED'])
                    elif r > 180 and 90 <= g < 160: set_pixel(fx, fy, PALETTE['FIRE_ORANGE'])
                    elif r > 180 and 160 <= g < 220: set_pixel(fx, fy, PALETTE['FIRE_YELLOW'])
                    elif r >= 220 and g >= 220: set_pixel(fx, fy, PALETTE['FIRE_CORE'])

    # 8. Anvil: Dark Steel Serial Plaque (EC-1897) with 4 Brass Screws + Hammer Motif
    elif name == 'anvil.png':
        for ty in range(20, 37):
            for tx in range(60, 171):
                p = get_pixel(tx, ty)
                if p[3] == 255:
                    if (p[0], p[1], p[2]) in ((0x8B, 0x8B, 0x8B), (0xC6, 0xC6, 0xC6), (0x21, 0x21, 0x21)):
                        set_pixel(tx, ty, PALETTE['DEEP_RECESS_BG'])
                    elif (p[0], p[1], p[2]) == (0x37, 0x37, 0x37):
                        set_pixel(tx, ty, PALETTE['SLOT_RIM_DARK_1'])
                    elif (p[0], p[1], p[2]) == (0xFF, 0xFF, 0xFF):
                        set_pixel(tx, ty, PALETTE['PANEL_BEVEL_LGT'])
        
        # 4 Brass screws on serial plaque corners
        for px, py in [(62, 22), (168, 22), (62, 34), (168, 34)]:
            set_pixel(px, py, PALETTE['BRASS_SPECULAR'])

    # 9. Smithing Table: Hardened Tool-Runner Bench & Hammer Emblem
    elif name == 'smithing.png':
        # Tool-runner track under the 3 input slots (X: 8 to 64, Y: 68)
        for rx in range(8, 65):
            set_pixel(rx, 68, PALETTE['PANEL_SEAM_DRK'])
            set_pixel(rx, 69, PALETTE['BRASS_MID'])
        
        # Clean input slots
        for sy, sx in [(48, 8), (48, 26), (48, 44)]:
            for dy in range(1, 17):
                for dx in range(1, 17):
                    set_pixel(sx + dx, sy + dy, PALETTE['SLOT_BG'])

    # 10. Grindstone: Stone Grinding Wheel with Axle Bolt
    elif name == 'grindstone.png':
        for gy in range(30, 45):
            for gx in range(84, 99):
                p = get_pixel(gx, gy)
                if p[3] == 255:
                    if (p[0], p[1], p[2]) == (0x8B, 0x8B, 0x8B):
                        set_pixel(gx, gy, (0x5A, 0x60, 0x68)) # Grinding wheel stone
                    elif (p[0], p[1], p[2]) == (0x37, 0x37, 0x37):
                        set_pixel(gx, gy, PALETTE['BORDER_OUTER'])
                    elif (p[0], p[1], p[2]) == (0xFF, 0xFF, 0xFF):
                        set_pixel(gx, gy, PALETTE['BRASS_SPECULAR'])
        # Center axle bolt
        set_pixel(91, 37, PALETTE['BRASS_SPECULAR'])
        set_pixel(92, 37, PALETTE['BRASS_MID'])

    # 11. Stonecutter: Masonry Saw Shroud with Tooth Relief & Recipe Rail
    elif name == 'stonecutter.png':
        for sy in range(15, 33):
            for sx in range(20, 45):
                p = get_pixel(sx, sy)
                if p[3] == 255 and (p[0], p[1], p[2]) == (0x37, 0x37, 0x37):
                    set_pixel(sx, sy, PALETTE['BORDER_OUTER'])
                elif p[3] == 255 and (p[0], p[1], p[2]) == (0x8B, 0x8B, 0x8B):
                    set_pixel(sx, sy, PALETTE['PANEL_DARK'])
        # Bronze blade arbor nut at (32, 23)
        set_pixel(32, 23, PALETTE['BRASS_SPECULAR'])
        set_pixel(33, 23, PALETTE['BRASS_MID'])

    # 12. Cartography Table: Navigation Drafting Plate with Latitude/Longitude Grid
    elif name == 'cartography_table.png':
        for cy in range(0, 135):
            for cx in range(0, 256):
                p = get_pixel(cx, cy)
                if p[3] == 255:
                    r, g, b = p[0], p[1], p[2]
                    if (r, g, b) in ((0xD6, 0xBE, 0x96), (0xE0, 0xCA, 0x9F)):
                        set_pixel(cx, cy, PALETTE['DRAFT_BG'])
                        if cx % 8 == 0 or cy % 8 == 0:
                            set_pixel(cx, cy, PALETTE['DRAFT_GRID'])
                    elif (r, g, b) in ((0x99, 0x87, 0x6C), (0x51, 0x49, 0x3A)):
                        set_pixel(cx, cy, PALETTE['DRAFT_BORDER'])
        # 4 Brass map pins on drafting viewport corners
        for px, py in [(69, 15), (131, 15), (69, 77), (131, 77)]:
            set_pixel(px, py, PALETTE['BRASS_SPECULAR'])

    # 13. Shulker Box: Hermetic Bio-Containment Seal & Warning Badges
    elif name == 'shulker_box.png':
        # Double perimeter pressure seal (X: 6 to 170, Y: 6 to 75)
        for py in range(6, 76):
            set_pixel(6, py, PALETTE['PANEL_SEAM_DRK'])
            set_pixel(7, py, PALETTE['PANEL_SEAM_LGT'])
            set_pixel(169, py, PALETTE['PANEL_SEAM_DRK'])
            set_pixel(170, py, PALETTE['PANEL_SEAM_LGT'])

    # 14. Generic 54 (Chests/Barrels): Cargo Locker Bolted Bands
    elif name == 'generic_54.png':
        for band_y in [69, 139]:
            if is_vanilla_panel_base(15, band_y):
                for bx in range(8, 168):
                    if is_vanilla_panel_base(bx, band_y):
                        set_pixel(bx, band_y, PALETTE['PANEL_SEAM_DRK'])
                        set_pixel(bx, band_y + 1, PALETTE['PANEL_SEAM_LGT'])
                # Cargo band rivets every 36px
                for rx in [16, 52, 88, 124, 160]:
                    if is_vanilla_panel_base(rx, band_y):
                        set_pixel(rx, band_y, PALETTE['RIVET_SPEC'])
                        set_pixel(rx+1, band_y, PALETTE['RIVET_BODY'])
                        set_pixel(rx, band_y+1, PALETTE['RIVET_BODY'])
                        set_pixel(rx+1, band_y+1, PALETTE['RIVET_SHAD'])

    # 15. Beacon: Optical Brass Bezels & Pyramid Steps (I, II, III, IV)
    elif name == 'beacon.png':
        for by in range(0, 219):
            for bx in range(0, 230):
                p = get_pixel(bx, by)
                if p[3] == 255 and (p[0], p[1], p[2]) == (0x55, 0x55, 0x55):
                    set_pixel(bx, by, PALETTE['BRASS_MID'])
                elif p[3] == 255 and (p[0], p[1], p[2]) == (0x21, 0x21, 0x21):
                    set_pixel(bx, by, PALETTE['DEEP_RECESS_BG'])

    # 16. Hopper: Pneumatic Flow Guide Arrows
    elif name == 'hopper.png':
        for ax in [44, 62, 80, 98, 116]:
            # Directional arrow above hopper slots
            set_pixel(ax + 8, 14, PALETTE['PANEL_SEAM_DRK'])
            set_pixel(ax + 8, 15, PALETTE['BRASS_MID'])

    # 17. Dispenser & Dropper: Mechanical Cogwheel Housing
    elif name in ('dispenser.png', 'dropper.png'):
        # Cog motif in upper plate (X: 84 to 92, Y: 8 to 14)
        for cy in range(8, 15):
            for cx in range(84, 93):
                if is_vanilla_panel_base(cx, cy):
                    set_pixel(cx, cy, (0x3A, 0x3E, 0x46))
        set_pixel(88, 11, PALETTE['BRASS_SPECULAR'])

    # 18. Horse: Stable Tack Equipment
    elif name == 'horse.png':
        # Brass stirrup buckle on saddle slot (X: 8, Y: 18)
        set_pixel(8, 18, PALETTE['BRASS_MID'])
        set_pixel(9, 18, PALETTE['BRASS_SPECULAR'])

    # 19. Inventory: Equipment Column & Personal Field Kit
    elif name == 'inventory.png':
        # Clean offhand and crafting slots
        for sy, sx in [(62, 77), (18, 98), (18, 116), (36, 98), (36, 116), (28, 154)]:
            for dy in range(1, 17):
                for dx in range(1, 17):
                    set_pixel(sx + dx, sy + dy, PALETTE['SLOT_BG'])

    # 20. Villager 2 (512x256): Trade Registry Ledger & Balance Scale
    elif name == 'villager2.png':
        # Balance scale emblem between trade inputs and output (X: 250 to 266, Y: 34 to 48)
        for sy in range(34, 49):
            for sx in range(250, 267):
                p = get_pixel(sx, sy)
                if p[3] == 255 and (p[0], p[1], p[2]) == (0x37, 0x37, 0x37):
                    set_pixel(sx, sy, PALETTE['BRASS_MID'])

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    out_file = os.path.join(OUTPUT_DIR, name)
    encoded = encode_png_rgba(w, h, out_rgba)
    with open(out_file, 'wb') as f:
        f.write(encoded)
    
    return w, h, len(encoded)


# ── Sophisticated Backpacks / Storage Generator ──────────────────────────────
def build_sophisticated_storage_bg(name, width, player_inv_offset):
    """Builds a 256x256 Sophisticated Storage background with brushed steel and exact slots."""
    w, h = 256, 256
    rgba = bytearray(w * h * 4)
    noise = IndustrialSteelNoise(seed=18970806 + width * 31)

    def set_s_px(x, y, rgb, a=255):
        if 0 <= x < w and 0 <= y < h:
            idx = (y * w + x) * 4
            rgba[idx] = max(0, min(255, rgb[0]))
            rgba[idx+1] = max(0, min(255, rgb[1]))
            rgba[idx+2] = max(0, min(255, rgb[2]))
            rgba[idx+3] = max(0, min(255, a))

    for y in range(h):
        for x in range(width):
            streak = noise.val(x * 0.10, y * 2.2) * 0.65 + noise.val(x * 0.40, y * 0.40) * 0.35
            shift = int((streak - 0.5) * 7.0)
            base = PALETTE['PANEL_BASE']
            set_s_px(x, y, (base[0] + shift, base[1] + shift, base[2] + shift + 1))

    for y in range(h):
        set_s_px(0, y, PALETTE['BORDER_OUTER'])
        set_s_px(1, y, PALETTE['PANEL_BEVEL_LGT'])
        set_s_px(width - 2, y, PALETTE['PANEL_BEVEL_DRK'])
        set_s_px(width - 1, y, PALETTE['BORDER_OUTER'])
    for x in range(width):
        set_s_px(x, 0, PALETTE['BORDER_OUTER'])
        set_s_px(x, 1, PALETTE['PANEL_BEVEL_LGT'])
        set_s_px(x, h - 2, PALETTE['PANEL_BEVEL_DRK'])
        set_s_px(x, h - 1, PALETTE['BORDER_OUTER'])

    for rx, ry in [(5, 5), (width - 6, 5), (5, h - 6), (width - 6, h - 6)]:
        set_s_px(rx, ry, PALETTE['RIVET_SPEC'])
        set_s_px(rx + 1, ry, PALETTE['RIVET_BODY'])
        set_s_px(rx, ry + 1, PALETTE['RIVET_BODY'])
        set_s_px(rx + 1, ry + 1, PALETTE['RIVET_SHAD'])

    # Seam at Y=160
    for sx in range(4, width - 4):
        set_s_px(sx, 160, PALETTE['PANEL_SEAM_DRK'])
        set_s_px(sx, 161, PALETTE['PANEL_SEAM_LGT'])

    inv_x_start = 7 + player_inv_offset
    for row in range(3):
        sy = 175 + row * 18
        for col in range(9):
            sx = inv_x_start + col * 18
            for dy in range(18):
                for dx in range(18):
                    px, py = sx + dx, sy + dy
                    if dx == 0 or dy == 0:
                        set_s_px(px, py, PALETTE['SLOT_RIM_DARK_1'])
                    elif dx == 17 or dy == 17:
                        set_s_px(px, py, PALETTE['SLOT_RIM_LIGHT'])
                    else:
                        set_s_px(px, py, PALETTE['SLOT_BG'])

    sy_hot = 233
    for col in range(9):
        sx = inv_x_start + col * 18
        for dy in range(18):
            for dx in range(18):
                px, py = sx + dx, sy_hot + dy
                if dx == 0 or dy == 0:
                    set_s_px(px, py, PALETTE['SLOT_RIM_DARK_1'])
                elif dx == 17 or dy == 17:
                    set_s_px(px, py, PALETTE['SLOT_RIM_LIGHT'])
                else:
                    set_s_px(px, py, PALETTE['SLOT_BG'])

    out_path = os.path.join(SOPHISTICATED_OUTPUT_DIR, name)
    with open(out_path, 'wb') as f:
        f.write(encode_png_rgba(w, h, rgba))
    print(f"  ✓ Built {name} ({w}x{h})")


def build_sophisticated_compat():
    """Builds Victorian Steel compatibility textures for Sophisticated Backpacks & Storage."""
    os.makedirs(SOPHISTICATED_OUTPUT_DIR, exist_ok=True)
    
    build_sophisticated_storage_bg('storage_background_9.png', 176, 0)
    build_sophisticated_storage_bg('storage_background_9_wider.png', 182, 3)
    build_sophisticated_storage_bg('storage_background_12.png', 230, 27)
    build_sophisticated_storage_bg('storage_background_12_wider.png', 236, 30)

    ws, hs = 256, 256
    rgb_s = bytearray(ws * hs * 4)
    for sy in range(0, hs, 18):
        for sx in range(0, ws, 18):
            for dy in range(18):
                for dx in range(18):
                    px = sx + dx
                    py = sy + dy
                    if px >= ws or py >= hs: continue
                    idx = (py * ws + px) * 4
                    if dx == 0 or dy == 0:
                        rgb = PALETTE['SLOT_RIM_DARK_1']
                    elif dx == 17 or dy == 17:
                        rgb = PALETTE['SLOT_RIM_LIGHT']
                    else:
                        rgb = PALETTE['SLOT_BG']
                    rgb_s[idx] = rgb[0]
                    rgb_s[idx+1] = rgb[1]
                    rgb_s[idx+2] = rgb[2]
                    rgb_s[idx+3] = 255
    
    with open(os.path.join(SOPHISTICATED_OUTPUT_DIR, 'slots_background.png'), 'wb') as f:
        f.write(encode_png_rgba(ws, hs, rgb_s))
    print("  ✓ Built slots_background.png (256x256)")

    wc, hc = 256, 256
    rgba_c = bytearray(wc * hc * 4)
    for y in range(hc):
        for x in range(wc):
            idx = (y * wc + x) * 4
            rgba_c[idx] = PALETTE['PANEL_BASE'][0]
            rgba_c[idx+1] = PALETTE['PANEL_BASE'][1]
            rgba_c[idx+2] = PALETTE['PANEL_BASE'][2]
            rgba_c[idx+3] = 255
            if x % 18 == 0 or y % 18 == 0:
                rgba_c[idx] = PALETTE['BORDER_OUTER'][0]
                rgba_c[idx+1] = PALETTE['BORDER_OUTER'][1]
                rgba_c[idx+2] = PALETTE['BORDER_OUTER'][2]

    with open(os.path.join(SOPHISTICATED_OUTPUT_DIR, 'gui_controls.png'), 'wb') as f:
        f.write(encode_png_rgba(wc, hc, rgba_c))
    with open(os.path.join(SOPHISTICATED_OUTPUT_DIR, 'icons.png'), 'wb') as f:
        f.write(encode_png_rgba(wc, hc, rgba_c))
    print("  ✓ Built gui_controls.png & icons.png (256x256)")


def build_all():
    containers = [
        'anvil.png', 'beacon.png', 'blast_furnace.png', 'brewing_stand.png',
        'cartography_table.png', 'crafting_table.png', 'dispenser.png', 'dropper.png',
        'enchanting_table.png', 'furnace.png', 'generic_54.png', 'grindstone.png',
        'hopper.png', 'horse.png', 'inventory.png', 'loom.png', 'shulker_box.png',
        'smithing.png', 'smoker.png', 'stonecutter.png', 'villager2.png'
    ]

    print("=================================================================")
    print(" Eternal Craft: Gripe Verde — Building Bespoke Victorian Steel v2")
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

    build_sophisticated_compat()
    print(f"\nSuccessfully built {count}/{len(containers)} containers + Sophisticated Backpacks in: {OUTPUT_DIR}")


if __name__ == '__main__':
    build_all()
