#!/usr/bin/env python3
"""Analiza las texturas de un pack: dimensiones, alfa, paleta, estilo."""
import sys, os, math
from collections import Counter
from PIL import Image

def analyze(path):
    img = Image.open(path).convert("RGBA")
    w, h = img.size
    px = img.load()
    n = w * h
    opaque = 0
    semi = 0
    transparent = 0
    colors = Counter()
    hue_counter = Counter()
    sat_sum = 0.0
    val_sum = 0.0
    sat_pix = 0
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            colors[(r >> 4, g >> 4, b >> 4, a >> 4)] += 1
            if a == 0:
                transparent += 1
            elif a == 255:
                opaque += 1
                # hue/sat only on fully opaque pixels
                mx = max(r, g, b); mn = min(r, g, b)
                sat = 0 if mx == 0 else (mx - mn) / mx
                val = mx / 255
                sat_sum += sat; val_sum += val; sat_pix += 1
                if mx == mn:
                    hue = -1
                else:
                    d = mx - mn
                    if mx == r: hue = ((g - b) / d) % 6
                    elif mx == g: hue = (b - r) / d + 2
                    else: hue = (r - g) / d + 4
                    hue = hue * 60
                hue_counter[round(hue / 15) * 15] += 1
            else:
                semi += 1
    sat_avg = sat_sum / sat_pix if sat_pix else 0
    val_avg = val_sum / sat_pix if sat_pix else 0
    top_hues = hue_counter.most_common(4)
    top_colors = colors.most_common(5)
    print(f"{os.path.basename(path)}: {w}x{h} | opaco {opaque} ({opaque/n*100:.0f}%) semi {semi} trans {transparent} | "
          f"colores_unicos {len(colors)} | sat_media {sat_avg:.3f} val_media {val_avg:.3f} | "
          f"huesos_top {top_hues}")
    print(f"   colores_top: {[(f'#{c[0]:02x}{c[1]:02x}{c[2]:02x}a{c[3]:02x}', n) for c, n in top_colors]}")

if __name__ == "__main__":
    for p in sys.argv[1:]:
        analyze(p)
