#!/usr/bin/env python3
"""Montaje comparativo: vanilla 1.20.1 vs nueva muestra, para revision visual."""
from PIL import Image, ImageDraw

V = "/tmp/mc-assets/assets/minecraft/textures/gui/container/inventory.png"
NEW = "tools/newpack/export/inventory.png"
OUT = "tools/newpack/export/comparativa_inventory.png"

SCALE = 3
van = Image.open(V).convert("RGBA")
new = Image.open(NEW).convert("RGBA")
w, h = van.size

van_bg = Image.new("RGBA", (w, h), (0, 0, 0, 255))
van_bg.alpha_composite(van)
new_bg = Image.new("RGBA", (w, h), (0, 0, 0, 255))
new_bg.alpha_composite(new)

van_s = van_bg.resize((w * SCALE, h * SCALE), Image.NEAREST)
new_s = new_bg.resize((w * SCALE, h * SCALE), Image.NEAREST)

gap = 24
label_h = 48
canvas = Image.new("RGBA", (w * SCALE * 2 + gap, h * SCALE + label_h), (16, 18, 22, 255))
canvas.paste(van_s, (0, label_h))
canvas.paste(new_s, (w * SCALE + gap, label_h))

d = ImageDraw.Draw(canvas)
try:
    font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 22)
except Exception:
    font = None
d.text((10, 10), "VANILLA 1.20.1", fill=(200, 210, 220, 255), font=font)
d.text((w * SCALE + gap + 10, 10), "NUEVO ESTILO v1 (muestra)", fill=(200, 210, 220, 255), font=font)
d.line([(w * SCALE + gap // 2, label_h), (w * SCALE + gap // 2, canvas.size[1])], fill=(80, 90, 100, 255))

canvas.convert("RGB").save(OUT)
print(f"OK -> {OUT}")
