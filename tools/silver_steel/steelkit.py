"""
Eternal Craft — Silver Steel UI Studio
=======================================
Reconstruye las texturas de contenedores de Minecraft 1.20.1 con un acabado
de acero gris plateado, preservando EXACTAMENTE:
  - dimensiones, canal alfa, geometria de slots e iconos funcionales vanilla
  - posiciones de todos los elementos funcionales (por construccion)
Los iconos funcionales (llama, flechas, burbujas, check, libro, patrones,
recetas, espada...) se copian VERBATIM desde vanilla.
"""
from PIL import Image
import random, math, os

import os
VANILLA = os.environ.get('STEEL_VANILLA', '/tmp/minecraft-assets-1.20.1/assets/minecraft/textures/gui/container')
OUT = os.environ.get('STEEL_OUT', '/tmp/steel/out')

# ============ PALETA "SILVER STEEL" ============
# Acero gris plateado: frio, limpio, baja saturacion. Sin marrones, sin azules intensos.
P = {
    'ED':  (0x23, 0x2A, 0x33),   # borde exterior (esquina mas oscura)
    'S2':  (0x2E, 0x35, 0x40),   # sombra profunda / hueco
    'S1':  (0x3B, 0x44, 0x50),   # estructura oscura (marcos de slot vanilla)
    'M2':  (0x4C, 0x55, 0x62),   # interior de slot rehundido
    'M1':  (0x5F, 0x69, 0x76),   # lado sombra de panel
    'B':   (0x8A, 0x94, 0xA2),   # acero base (reemplaza 139)
    'B2':  (0x99, 0xA3, 0xB1),   # base claro
    'L1':  (0xAB, 0xB5, 0xC2),   # luz 1
    'L2':  (0xC2, 0xCB, 0xD6),   # luz 2 (reemplaza 198)
    'L3':  (0xD6, 0xDD, 0xE6),   # luz 3
    'HL':  (0xE9, 0xEF, 0xF6),   # brillo (reemplaza 255)
    'SI':  (0x64, 0x6E, 0x7B),   # interior de slot rehundido
}
# Acentos por contenedor (MUY desaturados, solo detalles)
ACCENT = {
    'enchanting': (0x8E, 0x7F, 0xAD),  # amatista apagada
    'beacon':     (0xBC, 0xAB, 0x84),  # dorado palido
    'brewing':    (0x86, 0xA2, 0xA0),  # teal apagado
    'furnace':    (0xC8, 0x68, 0x3E),  # ascua apagada (naranja mate)
    'smoker':     (0xC8, 0x68, 0x3E),
    'blast':      (0xC8, 0x68, 0x3E),
    'villager':   (0xBC, 0xAB, 0x84),
}

# ============ NOISE (grano de metal cepillado, sin patron repetitivo) ============
class Noise:
    def __init__(self, seed):
        self.perm = list(range(256))
        rng = random.Random(seed)
        rng.shuffle(self.perm)
        self.perm += self.perm
    def _hash(self, x, y):
        return self.perm[(self.perm[x & 255] + y) & 255]
    def _smooth(self, t):
        return t * t * (3 - 2 * t)
    def value(self, x, y):
        x0, y0 = int(x), int(y)
        fx, fy = x - x0, y - y0
        v00 = self._hash(x0, y0); v10 = self._hash(x0+1, y0)
        v01 = self._hash(x0, y0+1); v11 = self._hash(x0+1, y0+1)
        sx, sy = self._smooth(fx), self._smooth(fy)
        return (v00*(1-sx)+v10*sx)*(1-sy) + (v01*(1-sx)+v11*sx)*sy  # 0..255

class SteelCanvas:
    """Lienzo que parte de vanilla: alfa e iconos preservados por construccion."""
    def __init__(self, name, seed, accent=None, base='B', light='L2'):
        self.name = name
        self.van = Image.open(os.path.join(VANILLA, name)).convert('RGBA')
        self.im = self.van.copy()
        self.px = self.im.load()
        self.w, self.h = self.im.size
        self.noise = Noise(seed)
        self.rng = random.Random(seed)
        self.accent = accent
        self.base_key = base
        self.light_key = light
        self.panel_families = []   # (rgb, clase, tolerancia) tratados como panel
        self._preserves = []

    # ---- helpers de dibujo ----
    def put(self, x, y, color):
        if 0 <= x < self.w and 0 <= y < self.h:
            a = self.px[x, y][3]
            if a == 255:
                self.px[x, y] = color + (a,)

    def rect(self, x0, y0, x1, y1, color):
        for y in range(y0, y1+1):
            for x in range(x0, x1+1):
                self.put(x, y, color)

    def hline(self, x0, x1, y, color):
        for x in range(x0, x1+1):
            self.put(x, y, color)

    def vline(self, x, y0, y1, color):
        for y in range(y0, y1+1):
            self.put(x, y, color)

    def line(self, x0, y0, x1, y1, color):
        """Bresenham 1px."""
        dx, dy = abs(x1-x0), abs(y1-y0)
        sx = 1 if x0 < x1 else -1
        sy = 1 if y0 < y1 else -1
        err = dx - dy
        x, y = x0, y0
        while True:
            self.put(x, y, color)
            if x == x1 and y == y1: break
            e2 = 2 * err
            if e2 > -dy: err -= dy; x += sx
            if e2 < dx: err += dx; y += sy

    def circle(self, cx, cy, r, color):
        for dy in range(-r, r+1):
            for dx in range(-r, r+1):
                if dx*dx + dy*dy <= r*r:
                    self.put(cx+dx, cy+dy, color)

    def rivet(self, x, y, size=3):
        """Remache: anillo oscuro + centro brillante."""
        r = size // 2
        for dy in range(-r, r+1):
            for dx in range(-r, r+1):
                if abs(dx) == r or abs(dy) == r:
                    self.put(x+dx, y+dy, P['S1'])
        self.put(x, y, P['HL'])
        if size >= 3:
            self.put(x-1, y-1, P['L3'])
            self.put(x+1, y+1, P['S1'])

    def screw(self, x, y):
        """Tornillo con ranura."""
        self.rect(x-1, y-1, x+1, y+1, P['M1'])
        self.put(x, y-1, P['L2']); self.put(x, y+1, P['S1'])
        self.put(x-1, y, P['L2']); self.put(x+1, y, P['S1'])

    def recess_slots(self, cells, color=None):
        """Pinta el interior rehundido de cada celda de slot (posiciones vanilla)."""
        color = color or P['SI']
        for (x, y) in cells:
            for yy in range(y+1, y+17):
                for xx in range(x+1, x+17):
                    # respeta acentos ya pintados (clase I en el lienzo actual)
                    if self.classify(self.px[xx, yy]) == 'l':
                        self.px[xx, yy] = color + (255,)

    def recess_rect(self, x0, y0, x1, y1, color=None):
        """Rehunde un area rectangular (clase l -> SI)."""
        color = color or P['SI']
        for yy in range(y0, y1+1):
            for xx in range(x0, x1+1):
                if self.classify(self.px[xx, yy]) == 'l':
                    self.px[xx, yy] = color + (255,)

    def seam(self, x0, x1, y, color=P['S1']):
        """Junta de placa: linea oscura + brillo inferior."""
        self.hline(x0, x1, y, color)
        self.hline(x0, x1, y+1, P['L3'])

    def vseam(self, x, y0, y1, color=P['S1']):
        self.vline(x, y0, y1, color)
        self.vline(x+1, y0, y1, P['L3'])

    def etch(self, pts, dark=None, light=None):
        """Grabado: linea oscura + 1px de luz abajo-derecha (efecto inciso)."""
        dark = dark or P['S1']; light = light or P['L3']
        prev = None
        for p in pts:
            if prev:
                self.line(prev[0], prev[1], p[0], p[1], dark)
            prev = p
        prev = None
        for p in pts:
            if prev:
                self.line(prev[0]+1, prev[1]+1, p[0]+1, p[1]+1, light)
            prev = p

    # ---- grano cepillado ----
    def brushed(self, x0, y0, x1, y1, base_key='B', vertical=False, strength=6):
        """Grano de acero cepillado: vetas finas 1px, no repetitivas."""
        base = P[base_key]
        for y in range(y0, y1+1):
            for x in range(x0, x1+1):
                a = self.px[x, y][3]
                if a < 255: continue
                if self.classify(self.van.getpixel((x, y))) not in ('l', 'H'):
                    continue  # nunca pintar sobre iconos ni estructura
                # ruido de baja frecuencia (placa) + veta fina (cepillo)
                n = self.noise.value(x*0.35, y*0.35) / 255.0
                if vertical:
                    streak = self.noise.value(x*0.05, y*3.7) / 255.0
                else:
                    streak = self.noise.value(x*3.7, y*0.05) / 255.0
                v = int((streak - 0.5) * strength * 2 + (n - 0.5) * strength * 1.2)
                r = max(0, min(255, base[0] + v))
                g = max(0, min(255, base[1] + v))
                b = max(0, min(255, base[2] + v + 2))  # frio sutil
                self.px[x, y] = (r, g, b, a)

    # ---- clasificacion de pixeles vanilla ----
    def classify(self, p):
        """Devuelve la clase estructural de un pixel vanilla."""
        a = p[3]
        if a < 255: return 'T'   # transparencia parcial: preservar verbatim
        r, g, b = p[0], p[1], p[2]
        if (r,g,b) == (0,0,0): return 'K'
        for (fr, fg, fb), cls, tol in self.panel_families:
            if abs(r-fr) <= tol and abs(g-fg) <= tol and abs(b-fb) <= tol:
                return cls
        sat = max(r,g,b) - min(r,g,b)
        if sat > 26: return 'I'          # icono de color -> preservar verbatim
        lum = (r+g+b)//3
        if lum < 90: return 'D'
        if lum >= 235: return 'W'
        if 128 <= lum < 160: return 'l'
        if 160 <= lum < 215: return 'H'
        if lum < 128: return 'D'
        return 'l'

    def repaint(self, x0, y0, x1, y1, base_key='B', light_key=None, accent=None):
        """Pinta el panel de acero SOLO sobre pixeles de clase panel (l/H)."""
        base = P[base_key]
        light = P[light_key or 'L2']
        for y in range(y0, y1+1):
            for x in range(x0, x1+1):
                cls = self.classify(self.van.getpixel((x, y)))
                if cls == 'l':
                    self.px[x, y] = base + (255,)
                elif cls == 'H':
                    self.px[x, y] = light + (255,)

    def apply_structure(self, x0=0, y0=0, x1=None, y1=None, accent=None):
        """Reaplica la estructura vanilla recoloreada a acero (posiciones identicas)."""
        x1 = x1 or self.w; y1 = y1 or self.h
        for y in range(y0, y1):
            for x in range(x0, x1):
                cls = self.classify(self.van.getpixel((x, y)))
                if cls == 'D':
                    self.px[x, y] = P['S1'] + (255,)
                elif cls == 'K':
                    self.px[x, y] = P['S2'] + (255,)
                elif cls == 'W':
                    self.px[x, y] = P['HL'] + (255,)
                # l y H ya fueron repintadas por el diseno

    def preserve(self, x0, y0, x1, y1):
        """Copia verbatim regiones funcionales desde vanilla (iconos).
        Se aplican al final (save), despues de apply_structure."""
        self._preserves.append((x0, y0, x1, y1))

    def save(self, fname=None):
        for (x0, y0, x1, y1) in self._preserves:
            for y in range(y0, y1+1):
                for x in range(x0, x1+1):
                    self.px[x, y] = self.van.getpixel((x, y))
        out = os.path.join(OUT, fname or self.name)
        self.im.save(out)
        return out
