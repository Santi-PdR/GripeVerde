"""
Eternal Craft — New Pack UI Studio (estilo "fresadora": gris azulado neutro,
monocromo, sin remaches ni juntas decorativas).

Reconstruye las texturas de contenedores de Minecraft 1.20.1 preservando
EXACTAMENTE:
  - dimensiones y canal alfa (por construccion: solo se repinta alfa == 255)
  - posiciones de slots (derivadas de la vanilla, nunca hardcodeadas)
  - iconos funcionales verbatim (botones, tabs, colores no estructurales)
  - la figura del jugador / iconos de armadura (regiones verbatim)

A diferencia de Silver Steel: paleta NEUTRA gris-azulada (mas clara, menos
fria), sin remaches, sin costuras, sin acentos por contenedor.
"""
import os
import random
from PIL import Image

VANILLA = os.environ.get(
    'NC_VANILLA',
    os.path.join(os.path.dirname(os.path.abspath(__file__)), 'vanilla'))
OUT = os.environ.get('NC_OUT', os.path.join(os.path.dirname(os.path.abspath(__file__)), 'export'))

# ================= PALETA "FRESADORA" v1 =================
# Gris azulado neutro, medio-claro, baja saturacion, monocromo.
# Hue ~218, sat ~6-9%. Sin acentos por contenedor.
P = {
    'ED': (0x3E, 0x44, 0x4E),   # borde exterior / silueta (reemplaza #000000)
    'S2': (0x51, 0x58, 0x62),   # sombra profunda (reemplaza #212121)
    'S1': (0x63, 0x6A, 0x75),   # estructura / marco de slot (reemplaza #373737)
    'SI': (0x7B, 0x83, 0x8F),   # interior de slot rehundido (reemplaza #8B8B8B)
    'SI2': (0x84, 0x8C, 0x98),  # interior de slot, lado luz
    'B':  (0x99, 0xA1, 0xAD),   # base de panel (reemplaza #C6C6C6)
    'B2': (0xA6, 0xAE, 0xB9),   # base claro (grano)
    'L1': (0xB8, 0xBF, 0xC9),   # luz 1
    'L2': (0xCD, 0xD3, 0xDC),   # luz 2 (bevel claro)
    'HL': (0xF0, 0xF3, 0xF7),   # brillo (reemplaza #FFFFFF)
}

# Mapa exacto vanilla -> clave de paleta (solo alfa == 255)
REPAINT = {
    (0xC6, 0xC6, 0xC6): 'B',   # panel
    (0x8B, 0x8B, 0x8B): 'SI',  # interior de slot
    (0x37, 0x37, 0x37): 'S1',  # marco de slot (top/left)
    (0x21, 0x21, 0x21): 'S2',  # estructura oscura / panel offhand
    (0x50, 0x50, 0x50): 'SI',  # interior de slot offhand (vanilla oscuro)
    (0x00, 0x00, 0x00): 'ED',  # borde exterior / silueta
    (0xFF, 0xFF, 0xFF): 'HL',  # bevel claro / contorno
}

# Colores vanilla que se conservan VERBATIM (grises de figura/iconos)
PRESERVE_GRAYS = {
    (0x55, 0x55, 0x55), (0xB7, 0xB7, 0xB7), (0x99, 0x99, 0x99),
    (0xD6, 0xD6, 0xD6), (0x5B, 0x5B, 0x5B),
}

# ================= NOISE =================
class Noise:
    """Ruido deterministico de baja frecuencia (grano de maquina)."""
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
        v00 = self._hash(x0, y0); v10 = self._hash(x0 + 1, y0)
        v01 = self._hash(x0, y0 + 1); v11 = self._hash(x0 + 1, y0 + 1)
        sx, sy = self._smooth(fx), self._smooth(fy)
        return (v00 * (1 - sx) + v10 * sx) * (1 - sy) + (v01 * (1 - sx) + v11 * sx) * sy


class Canvas:
    """Lienzo que parte de vanilla: alfa e iconos preservados por construccion."""

    def __init__(self, name, seed):
        self.name = name
        self.van = Image.open(os.path.join(VANILLA, name)).convert('RGBA')
        self.im = self.van.copy()
        self.px = self.im.load()
        self.w, self.h = self.im.size
        self.noise = Noise(seed)
        self.preserve_regions = []   # (x0, y0, x1, y1) copiados verbatim al final
        self.van_px = self.van.load()

    # ---------- utilidades ----------
    def is_slot(self, x, y):
        """True si (x,y) es interior de slot (#8B8B8B) o slot offhand (#505050)."""
        p = self.van_px[x, y]
        return (p[0], p[1], p[2]) in ((0x8B, 0x8B, 0x8B), (0x50, 0x50, 0x50))

    # ---------- repintado base ----------
    def repaint(self):
        """Repinta SOLO pixeles opacos con color en REPAINT, en su posicion exacta."""
        for y in range(self.h):
            for x in range(self.w):
                p = self.van_px[x, y]
                if p[3] != 255:
                    continue
                key = REPAINT.get((p[0], p[1], p[2]))
                if key is not None:
                    self.px[x, y] = P[key] + (255,)

    # ---------- grano de maquina ----------
    def grain(self, strength=4):
        """Vetas verticales sutiles sobre la base del panel."""
        base = P['B']
        for y in range(self.h):
            for x in range(self.w):
                p = self.van_px[x, y]
                if p[3] != 255 or (p[0], p[1], p[2]) != (0xC6, 0xC6, 0xC6):
                    continue
                n = self.noise.value(x * 0.35, y * 0.35) / 255.0
                streak = self.noise.value(x * 0.08, y * 2.8) / 255.0
                v = int((streak - 0.5) * strength * 2 + (n - 0.5) * strength * 1.1)
                r = max(0, min(255, base[0] + v))
                g = max(0, min(255, base[1] + v))
                b = max(0, min(255, base[2] + v))
                self.px[x, y] = (r, g, b, 255)

    # ---------- bevel de slots ----------
    def slot_bevel(self):
        """Rehundido limpio: sombra top/left + luz bottom/right en el anillo
        interior de cada slot (1px), sobre el SI ya pintado."""
        for y in range(self.h):
            for x in range(self.w):
                if not self.is_slot(x, y):
                    continue
                # bordes del interior: vecino no-slot en esa direccion
                left = not (x > 0 and self.is_slot(x - 1, y))
                right = not (x < self.w - 1 and self.is_slot(x + 1, y))
                up = not (y > 0 and self.is_slot(x, y - 1))
                down = not (y < self.h - 1 and self.is_slot(x, y + 1))
                if up or left:
                    self.px[x, y] = P['S2'] + (255,)
                elif down or right:
                    self.px[x, y] = P['L2'] + (255,)

    # ---------- regiones verbatim ----------
    def preserve(self, x0, y0, x1, y1):
        self.preserve_regions.append((x0, y0, x1, y1))

    def save(self, fname=None):
        for (x0, y0, x1, y1) in self.preserve_regions:
            for y in range(max(0, y0), min(self.h, y1 + 1)):
                for x in range(max(0, x0), min(self.w, x1 + 1)):
                    self.px[x, y] = self.van_px[x, y]
        os.makedirs(OUT, exist_ok=True)
        out = os.path.join(OUT, fname or self.name)
        self.im.save(out)
        return out
