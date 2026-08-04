"""Verificacion rigurosa del pack steel vs vanilla."""
from PIL import Image
import os

import os
VANILLA = os.environ.get('STEEL_VANILLA', '/tmp/minecraft-assets-1.20.1/assets/minecraft/textures/gui/container')
OUT = os.environ.get('STEEL_OUT', '/tmp/steel/out')

def is_icon(p):
    return p[3] > 0 and (max(p[:3]) - min(p[:3])) > 26

FAMILIES = {
    'enchanting_table.png': [((160,145,114), 'l', 22), ((224,202,159), 'H', 22),
                             ((84,76,59), 'D', 20)],
}

def fam_class(families, p):
    for (fr, fg, fb), cls, tol in families:
        if abs(p[0]-fr) <= tol and abs(p[1]-fg) <= tol and abs(p[2]-fb) <= tol:
            return cls
    return None

def audit(name):
    families = FAMILIES.get(name, [])
    v = Image.open(os.path.join(VANILLA, name)).convert('RGBA')
    o = Image.open(os.path.join(OUT, name)).convert('RGBA')
    issues = []
    if v.size != o.size:
        return 'FAIL %s: size %s != %s' % (name, v.size, o.size)
    vp, op = v.load(), o.load()
    w, h = v.size
    # 1. alpha identico
    for y in range(h):
        for x in range(w):
            if vp[x, y][3] != op[x, y][3]:
                issues.append('alpha diff at (%d,%d)' % (x, y))
                if len(issues) > 5:
                    break
        if len(issues) > 5:
            break
    if issues:
        return 'FAIL %s: alfa %s' % (name, issues[:3])
    # 2. iconos vanilla preservados verbatim (una direccion)
    n_icons = 0
    for y in range(h):
        for x in range(w):
            p = vp[x, y]
            if fam_class(families, p) is not None:
                continue
            if is_icon(p):
                n_icons += 1
                if op[x, y] != p:
                    issues.append('icon diff at (%d,%d) %s vs %s' % (x, y, p, op[x, y]))
                    if len(issues) > 5:
                        break
        if len(issues) > 5:
            break
    if issues:
        return 'FAIL %s: iconos %s' % (name, issues[:3])
    # 3. estructura oscura/blanca en posiciones identicas (recoloreada a acero)
    n_dark = n_white = 0
    for y in range(h):
        for x in range(w):
            p = vp[x, y]
            if p[3] == 0 or is_icon(p):
                continue
            lum = sum(p[:3]) // 3
            if lum < 90:
                n_dark += 1
                q = op[x, y]
                if q[3] == 0 or sum(q[:3]) // 3 > 100:
                    issues.append('dark moved at (%d,%d): %s -> %s' % (x, y, p, q))
                    if len(issues) > 5:
                        break
            elif lum >= 235:
                n_white += 1
                q = op[x, y]
                if q[3] == 0 or sum(q[:3]) // 3 < 190:
                    issues.append('white moved at (%d,%d): %s -> %s' % (x, y, p, q))
                    if len(issues) > 5:
                        break
        if len(issues) > 5:
            break
    if issues:
        return 'FAIL %s: estructura %s' % (name, issues[:3])
    # 4. auditoria de paleta (solo pixeles NO-icono vanilla)
    browns = sats = 0
    for y in range(h):
        for x in range(w):
            p = op[x, y]
            if p[3] == 0 or is_icon(vp[x, y]) or fam_class(families, vp[x, y]) is not None:
                continue
            r, g, b = p[:3]
            sat = max(r, g, b) - min(r, g, b)
            if sat > 40:
                sats += 1
            if r > g > b and (r - b) > 25 and b > 70:
                browns += 1
    return ('OK %s: dark=%d white=%d icons=%d saturated>40=%d brownish=%d'
            % (name, n_dark, n_white, n_icons, sats, browns))

if __name__ == '__main__':
    for f in sorted(os.listdir(OUT)):
        print(audit(f))
