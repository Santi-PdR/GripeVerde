#!/usr/bin/env python3
"""
Master Steel — build_pack.py
Genera 21 texturas container Master Steel desde vanilla (/tmp/vanilla_gui)
Preserva dimensiones y alfa vanilla 1:1, slots 18x18 con pozo 16x16, bordes 1px.
Uso: python3 tools/build_pack.py
Salida: src/main/resources/resourcepacks/gripeverde_theme/assets/minecraft/textures/gui/container/*.png
        + docs/slot_master.png + docs/master_gui_template.png + pack.png
"""
import os, sys
from PIL import Image

VANILLA_DIR = "/tmp/vanilla_gui"
OUT_ROOT = "src/main/resources/resourcepacks/gripeverde_theme"
OUT_CONTAINER = os.path.join(OUT_ROOT, "assets/minecraft/textures/gui/container")
DOCS_DIR = "docs"

# Paleta Master Steel
STEEL_BASE = (144,147,152)      # #909398
STEEL_HIGHLIGHT = (172,176,180) # #ACB0B4
STEEL_SHADOW = (112,115,120)    # #707378
STEEL_OUTER = (78,80,84)        # #4E5054
SLOT_BASE = (99,102,107)        # #63666B tuned (theoretical #606368 96,99,104)
SLOT_SHADOW = (64,67,72)        # #404348
SLOT_HIGHLIGHT = (136,139,144) # #888B90
BRONZE = (198,166,89)           # #C6A659
BRONZE_DARK = (166,138,74)      # #A68A4A
BRONZE_LIGHT = (212,182,115)    # #D4B673
OBSIDIAN = (45,42,62)           # #2D2A3E
SILVER_LUNAR = (200,206,224)    # #C8CEE0
MUTED_RED = (139,44,31)         # #8B2C1F

VANILLA_MAP = {
    (198,198,198): STEEL_BASE,
    (85,85,85): STEEL_SHADOW,
    (255,255,255): None,  # context dependent
    (0,0,0): STEEL_OUTER,
    (55,55,55): SLOT_SHADOW,
    # (139,139,139): handled via slot/components
}

def detect_slots(px, w, h):
    """Detect slot origins via interior 16x16 #8B8B8B blocks."""
    slots=[]
    for y in range(h-16):
        for x in range(w-16):
            if px[x,y][:3] != (139,139,139):
                continue
            if px[x+15,y+15][:3] != (139,139,139):
                continue
            if x>0 and px[x-1,y][:3]==(139,139,139):
                continue
            if y>0 and px[x,y-1][:3]==(139,139,139):
                continue
            ok=True
            for dy in range(16):
                for dx in range(16):
                    if px[x+dx,y+dy][:3] != (139,139,139):
                        ok=False
                        break
                if not ok:
                    break
            if ok:
                slots.append((x-1,y-1))
    # deduplicate and sort
    slots = sorted(set(slots))
    # filter to valid slot origins inside image
    filtered=[]
    for sx,sy in slots:
        if 0 <= sx < w and 0 <= sy < h and sx+17 < w and sy+17 < h:
            filtered.append((sx,sy))
    return filtered

def detect_components(px, w, h, slots):
    """Detect non-slot 's' components (arrow, flame) as sets of pixels."""
    # Build slot mask
    slot_mask = [[False]*w for _ in range(h)]
    for sx,sy in slots:
        for dy in range(18):
            for dx in range(18):
                x=sx+dx; y=sy+dy
                if 0 <= x < w and 0 <= y < h:
                    slot_mask[y][x]=True
    visited=[[False]*w for _ in range(h)]
    components=[]
    from collections import deque
    for y in range(h):
        for x in range(w):
            if visited[y][x] or slot_mask[y][x]:
                continue
            if px[x,y][:3] != (139,139,139):
                continue
            # BFS for connected s
            q=deque([(x,y)])
            visited[y][x]=True
            comp=[]
            minx=maxx=x; miny=maxy=y
            while q:
                cx,cy=q.popleft()
                comp.append((cx,cy))
                minx=min(minx,cx); maxx=max(maxx,cx); miny=min(miny,cy); maxy=max(maxy,cy)
                for nx,ny in [(cx+1,cy),(cx-1,cy),(cx,cy+1),(cx,cy-1)]:
                    if 0 <= nx < w and 0 <= ny < h and not visited[ny][nx] and not slot_mask[ny][nx] and px[nx,ny][:3]==(139,139,139):
                        visited[ny][nx]=True
                        q.append((nx,ny))
            if len(comp) > 20:
                components.append(set(comp))
    return components

def generate_one(input_path, output_path):
    im = Image.open(input_path).convert("RGBA")
    w,h = im.size
    px = im.load()
    # detect slots
    slots = detect_slots(px, w, h)
    components = detect_components(px, w, h, slots)
    # For fast check, create set of component pixels
    comp_set=set()
    for c in components:
        comp_set.update(c)
    # Create output image
    out = Image.new("RGBA", (w,h), (0,0,0,0))
    opx = out.load()
    for y in range(h):
        for x in range(w):
            r,g,b,a = px[x,y]
            if a==0:
                opx[x,y]=(0,0,0,0)
                continue
            # Check if inside slot
            inside=False
            slot_origin=None
            for sx,sy in slots:
                if sx <= x <= sx+17 and sy <= y <= sy+17:
                    inside=True
                    slot_origin=(sx,sy)
                    break
            if inside:
                sx,sy = slot_origin
                relx = x - sx
                rely = y - sy
                if relx==0 and rely==0:
                    c=SLOT_SHADOW
                elif relx==17 and rely==0:
                    c=SLOT_BASE
                elif relx==0 and rely==17:
                    c=SLOT_BASE
                elif relx==17 and rely==17:
                    c=SLOT_HIGHLIGHT
                elif rely==0 and 0 <= relx <=16:
                    c=SLOT_SHADOW
                elif relx==0 and 1 <= rely <=16:
                    c=SLOT_SHADOW
                elif relx==17 and 1 <= rely <=16:
                    c=SLOT_HIGHLIGHT
                elif rely==17 and 1 <= relx <=17:
                    c=SLOT_HIGHLIGHT
                elif 1 <= relx <=16 and 1 <= rely <=16:
                    c=SLOT_BASE
                else:
                    c=SLOT_BASE
                opx[x,y]=(*c,255)
                continue
            if (x,y) in comp_set:
                # Functional component -> bronze
                # Original s -> bronze, but keep variation: if it's arrow shape, use bronze
                opx[x,y]=(*BRONZE,255)
                continue
            # Panel area
            src = (r,g,b)
            if src == (198,198,198):
                c=STEEL_BASE
            elif src == (85,85,85):
                c=STEEL_SHADOW
            elif src == (0,0,0):
                c=STEEL_OUTER
            elif src == (55,55,55):
                # Should have been in slot, but if outside, treat as outer
                c=STEEL_OUTER
            elif src == (255,255,255):
                # Determine if this white is panel highlight (near border) vs slot highlight (already handled)
                # Panel white -> steel highlight
                c=STEEL_HIGHLIGHT
            elif src == (139,139,139):
                # Remaining s outside slots/components -> map to steel base (should not happen)
                c=STEEL_BASE
            elif src == (255,0,0):
                c=MUTED_RED
            elif src == (160,145,114):
                # enchanting beige -> obsidian or steel? For enchanting, map to silver lunar if near center? Simple: map to obsidian for now
                # Heuristic: if file is enchanting_table, map beige to obsidian interior
                if "enchanting" in input_path:
                    c=OBSIDIAN
                else:
                    c=STEEL_BASE
            elif src == (81,73,58):
                # wood brown -> steel shadow
                c=STEEL_SHADOW
            elif src == (107,97,76):
                c=STEEL_SHADOW
            else:
                # Fallback by luminance
                lum = 0.299*r+0.587*g+0.114*b
                if r>150 and g<80 and b<80:
                    c=MUTED_RED
                elif lum > 180:
                    c=STEEL_HIGHLIGHT
                elif lum < 60:
                    c=STEEL_OUTER
                elif lum < 110:
                    c=STEEL_SHADOW
                else:
                    c=STEEL_BASE
            opx[x,y]=(*c,255)
    # Ensure we preserve exact alpha for transparent pixels already done
    # For enchanting_table: apply special interior obsidian for center area? The vanilla enchanting has dark central rectangle for book.
    # We will do extra pass for enchanting: recolor central book area (approx 30,14, 118, 68) to obsidian gradient? Simplified: keep as steel but add obsidian overlay for those pixels that were originally beige?
    # Our mapping above already maps beige to obsidian, so central area will be obsidian.

    # Similarly for beacon: beacon has dark interior #212121? In vanilla beacon, interior is #212121? Check top colors beacon had (33,33,33) etc. That maps via luminance fallback to outer.
    # That will become steel outer, but we want beacon interior to be steel base with bronze buttons. It's okay.

    # Save optimized
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    out.save(output_path, "PNG", optimize=True)

    return slots, components

def draw_slot_master(path):
    im=Image.new("RGBA",(18,18),(0,0,0,0))
    px=im.load()
    for y in range(18):
        for x in range(18):
            if x==0 and y==0:
                c=SLOT_SHADOW
            elif x==17 and y==0:
                c=SLOT_BASE
            elif x==0 and y==17:
                c=SLOT_BASE
            elif x==17 and y==17:
                c=SLOT_HIGHLIGHT
            elif y==0 and 0 <= x <=16:
                c=SLOT_SHADOW
            elif x==0 and 1 <= y <=16:
                c=SLOT_SHADOW
            elif x==17 and 1 <= y <=16:
                c=SLOT_HIGHLIGHT
            elif y==17 and 1 <= x <=17:
                c=SLOT_HIGHLIGHT
            elif 1 <= x <=16 and 1 <= y <=16:
                c=SLOT_BASE
            else:
                c=SLOT_BASE
            px[x,y]=(*c,255)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path, "PNG", optimize=True)
    # Also save copy to container for reference
    im.save(os.path.join(OUT_CONTAINER, "slot_master.png"), "PNG", optimize=True)
    print(f"slot_master {path} ok")

def draw_master_gui_template(path):
    # 176x166 panel demo within 256x256
    W,H=256,256
    im=Image.new("RGBA",(W,H),(0,0,0,0))
    px=im.load()
    # Fill panel area (0,0)-(175,165) with same logic as vanilla panel: outer border etc.
    # We'll generate panel using same mapping as generate_one but for empty panel
    # Load vanilla inventory as reference for alpha mask
    ref = Image.open(os.path.join(VANILLA_DIR,"inventory.png")).convert("RGBA")
    rpx=ref.load()
    # Generate steel panel with slots demo (46 slots layout as inventory)
    # Slots pos for demo: replicate inventory slots (46)
    # Use detected slots from inventory
    inv_slots = detect_slots(rpx, W, H)
    # But for template we want few slots: hotbar 9 + inventory 9 + crafting 4 + result
    # Simpler: use full inventory slots for template (46)
    for y in range(H):
        for x in range(W):
            r,g,b,a=rpx[x,y]
            if a==0:
                px[x,y]=(0,0,0,0)
                continue
            # If inside any slot (use inv_slots)
            inside=False
            so=None
            for sx,sy in inv_slots:
                if sx <= x <= sx+17 and sy <= y <= sy+17:
                    inside=True; so=(sx,sy); break
            if inside:
                sx,sy=so
                relx=x-sx; rely=y-sy
                if relx==0 and rely==0: c=SLOT_SHADOW
                elif relx==17 and rely==0: c=SLOT_BASE
                elif relx==0 and rely==17: c=SLOT_BASE
                elif relx==17 and rely==17: c=SLOT_HIGHLIGHT
                elif rely==0 and 0<=relx<=16: c=SLOT_SHADOW
                elif relx==0 and 1<=rely<=16: c=SLOT_SHADOW
                elif relx==17 and 1<=rely<=16: c=SLOT_HIGHLIGHT
                elif rely==17 and 1<=relx<=17: c=SLOT_HIGHLIGHT
                elif 1<=relx<=16 and 1<=rely<=16: c=SLOT_BASE
                else: c=SLOT_BASE
                px[x,y]=(*c,255)
            else:
                # panel
                src=(r,g,b)
                if src==(198,198,198): c=STEEL_BASE
                elif src==(85,85,85): c=STEEL_SHADOW
                elif src==(0,0,0): c=STEEL_OUTER
                elif src==(55,55,55): c=STEEL_OUTER
                elif src==(255,255,255): c=STEEL_HIGHLIGHT
                elif src==(139,139,139): c=STEEL_BASE
                else: c=STEEL_BASE
                px[x,y]=(*c,255)
    # Overlay bronze arrow at (89,35) for demo crafting? But inventory doesn't have arrow; keep as steel.
    # For template we want to show Master Steel with labels: we can't render text, but we can add subtle bronze arrow demo
    # Draw arrow silhouette at (89,35) 22x15 as bronze example
    arrow_x, arrow_y = 89, 35
    # Arrow shape from earlier: s pattern
    arrow_shape = [
        (0,1),(1,1),
        (0,2),(1,2),(2,2),
        (0,3),(1,3),(2,3),(3,3),
        (0,4),(1,4),(2,4),(3,4),(4,4),
        (0,5),(1,5),(2,5),(3,5),(4,5),(5,5),
        (2,6),(3,6),(4,6),(5,6),(6,6),(7,6),(8,6),(9,6),(10,6),(11,6),(12,6),(13,6),(14,6),(15,6),(16,6),(17,6),(18,6),
        (2,7),(3,7),(4,7),(5,7),(6,7),(7,7),(8,7),(9,7),(10,7),(11,7),(12,7),(13,7),(14,7),(15,7),(16,7),(17,7),(18,7),(19,7),
        (2,8),(3,8),(4,8),(5,8),(6,8),(7,8),(8,8),(9,8),(10,8),(11,8),(12,8),(13,8),(14,8),(15,8),(16,8),(17,8),(18,8),
        (0,9),(1,9),(2,9),(3,9),(4,9),(5,9),
        (0,10),(1,10),(2,10),(3,10),(4,10),
        (0,11),(1,11),(2,11),(3,11),
        (0,12),(1,12),(2,12),
        (0,13),(1,13),
        (0,14),
    ]
    # Actually arrow shape is more precise; we'll use simplified set from earlier s cluster at (90,35) but shift
    # The shape we have is 22x15 arrow: use the component set from vanilla crafting arrow (90,35) size 22x15
    # For template, draw that shape in bronze at (90,35) if within panel
    for dx,dy in arrow_shape:
        x=arrow_x+dx; y=arrow_y+dy
        if 0<=x<W and 0<=y<H:
            px[x,y]=(*BRONZE,255)

    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path, "PNG", optimize=True)
    # also save as template in docs and maybe in container
    print(f"master template {path} ok")

def draw_pack_png(path):
    # 64x64 pack icon: steel base with bronze accent diagonal and slot demo
    im=Image.new("RGBA",(64,64),(0,0,0,0))
    px=im.load()
    # Fill with steel base
    for y in range(64):
        for x in range(64):
            # border
            if x==0 or y==0:
                px[x,y]=(*STEEL_HIGHLIGHT,255)
            elif x==63 or y==63:
                px[x,y]=(*STEEL_SHADOW,255)
            elif x==1 or y==1:
                px[x,y]=(*STEEL_HIGHLIGHT,255)
            elif x==62 or y==62:
                px[x,y]=(*STEEL_SHADOW,255)
            else:
                px[x,y]=(*STEEL_BASE,255)
    # Center slot 32x? Actually draw 18x18 slot centered
    sx,sy=23,16
    for y in range(18):
        for x in range(18):
            dx,dy=sx+x,sy+y
            if x==0 and y==0: c=SLOT_SHADOW
            elif x==17 and y==0: c=SLOT_BASE
            elif x==0 and y==17: c=SLOT_BASE
            elif x==17 and y==17: c=SLOT_HIGHLIGHT
            elif y==0 and 0<=x<=16: c=SLOT_SHADOW
            elif x==0 and 1<=y<=16: c=SLOT_SHADOW
            elif x==17 and 1<=y<=16: c=SLOT_HIGHLIGHT
            elif y==17 and 1<=x<=17: c=SLOT_HIGHLIGHT
            elif 1<=x<=16 and 1<=y<=16: c=SLOT_BASE
            else: c=SLOT_BASE
            px[dx,dy]=(*c,255)
    # Add bronze arrow small at bottom
    for x in range(20,44):
        px[x,44]=(*BRONZE,255)
        px[x,45]=(*BRONZE_DARK,255)
    # Triangle head
    for dy in range(5):
        for dx in range(5-dy):
            px[44+dx,42+dy]=(*BRONZE,255)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path, "PNG", optimize=True)
    print(f"pack.png {path} ok")

def main():
    os.makedirs(OUT_CONTAINER, exist_ok=True)
    os.makedirs(DOCS_DIR, exist_ok=True)
    # Generate each container
    files = [f for f in os.listdir(VANILLA_DIR) if f.endswith(".png")]
    # Ensure we have villager2 alias
    if "villager2.png" not in files and "villager.png" in files:
        files.append("villager2.png")
    # Ensure dropper alias already exists
    total=0
    for fname in sorted(set(files)):
        in_path=os.path.join(VANILLA_DIR, fname)
        # if villager2 alias, copy from villager
        if fname=="villager2.png" and not os.path.exists(in_path):
            # copy from villager.png
            import shutil
            shutil.copy(os.path.join(VANILLA_DIR,"villager.png"), in_path)
        if not os.path.exists(in_path):
            print(f"skip missing {fname}")
            continue
        out_path=os.path.join(OUT_CONTAINER, fname)
        slots, comps = generate_one(in_path, out_path)
        print(f"{fname}: slots={len(slots)} comps={len(comps)} -> {out_path}")
        total+=1
    print(f"Generated {total} containers")
    # Slot master
    draw_slot_master(os.path.join(DOCS_DIR,"slot_master.png"))
    # Master template
    draw_master_gui_template(os.path.join(DOCS_DIR,"master_gui_template.png"))
    # Pack icon
    draw_pack_png(os.path.join(OUT_ROOT,"pack.png"))
    # Also copy pack.png to docs for reference
    import shutil
    shutil.copy(os.path.join(OUT_ROOT,"pack.png"), os.path.join(DOCS_DIR,"pack.png"))
    print("Done build_pack")

if __name__=="__main__":
    main()
