#!/usr/bin/env python3
"""Compara máscaras alfa entre texturas y contra las del PR3 (base vanilla)."""
import sys
from PIL import Image

def mask(path):
    img = Image.open(path).convert("RGBA")
    a = img.split()[3]
    return list(a.getdata())

def diff_masks(m1, m2, label):
    d = sum(1 for x, y in zip(m1, m2) if x != y)
    print(f"{label}: {d} px distintos de {len(m1)}")

if __name__ == "__main__":
    base = "src/main/resources/resourcepacks/gripeverde_theme/assets/minecraft/textures/gui/container"
    pr3 = "/tmp/pr3tex/src/main/resources/resourcepacks/gripeverde_theme/assets/minecraft/textures/gui/container"
    pairs = [
        ("anvil", "furnace"),
        ("inventory", "crafting_table"),
        ("anvil", "inventory"),
        ("dispenser", "dropper"),
        ("generic_54", "inventory"),
    ]
    for a, b in pairs:
        m1 = mask(f"{base}/{a}.png")
        m2 = mask(f"{base}/{b}.png")
        diff_masks(m1, m2, f"main {a} vs main {b}")

    # main vs pr3 (misma textura)
    for name in ["anvil", "furnace", "inventory", "crafting_table", "villager2", "generic_54"]:
        m1 = mask(f"{base}/{name}.png")
        m2 = mask(f"{pr3}/{name}.png")
        diff_masks(m1, m2, f"main {name} vs pr3 {name}")
