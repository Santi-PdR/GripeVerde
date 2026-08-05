#!/usr/bin/env python3
"""Render ASCII de la máscara alfa + comparación RGBA completa."""
import sys
from PIL import Image

def ascii_alpha(path, w_out=96, label=""):
    img = Image.open(path).convert("RGBA")
    a = img.split()[3].resize((w_out, int(w_out * img.size[1] / img.size[0] / 2)))
    px = a.load()
    chars = " .:-=+*#%@"
    print(f"--- {label} ({path}) ---")
    for y in range(a.size[1]):
        row = ""
        for x in range(a.size[0]):
            v = px[x, y]
            row += chars[min(9, v * 10 // 256)]
        print(row)
    print()

def rgba_equal(p1, p2):
    im1 = Image.open(p1).convert("RGBA")
    im2 = Image.open(p2).convert("RGBA")
    d1 = im1.tobytes()
    d2 = im2.tobytes()
    return d1 == d2, sum(1 for a, b in zip(d1, d2) if a != b)

if __name__ == "__main__":
    base = "src/main/resources/resourcepacks/gripeverde_theme/assets/minecraft/textures/gui/container"
    pr3 = "/tmp/pr3tex/src/main/resources/resourcepacks/gripeverde_theme/assets/minecraft/textures/gui/container"
    eq, ndiff = rgba_equal(f"{base}/anvil.png", f"{base}/furnace.png")
    print(f"anvil.png == furnace.png (RGBA): {eq}, {ndiff} bytes distintos")
    eq2, ndiff2 = rgba_equal(f"{base}/inventory.png", f"{base}/crafting_table.png")
    print(f"inventory.png == crafting_table.png (RGBA): {eq2}, {ndiff2} bytes distintos")
    ascii_alpha(f"{base}/inventory.png", label="MAIN inventory")
    ascii_alpha(f"{pr3}/inventory.png", label="PR3 inventory (silver steel, base vanilla)")
    ascii_alpha(f"{pr3}/furnace.png", label="PR3 furnace")
