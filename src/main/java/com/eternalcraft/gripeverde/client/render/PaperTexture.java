package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;

/**
 * El papel de las pantallas: un velo, un filete y nada mas.
 *
 * v0.184.0 dejo de dibujar la hoja con adornos (marca de agua, sello de cera,
 * gotas, fibras, manchas) porque tapaba el fondo del menu y porque el lector no
 * queria dibujos en el papel.
 *
 * v0.185.0 remata la faena: fuera todo el codigo de adornos, que ya no se
 * llamaba desde ninguna pantalla. La clase queda en lo que se usa —el velo, su
 * filete y la paleta por tipo de papel— y el verificador prohibe los nombres
 * viejos para que nadie los reviva por costumbre.
 */
public final class PaperTexture {

    /** Opacidad del velo de las pantallas (0-255). Mas bajo deja ver mas fondo. */
    // v0.184.0: 172 de 255 (67 %). Con 196 el papel tapaba el fondo casi como una
    // hoja; con 172 la tinta oscura se lee igual y la escena se adivina debajo.
    private static final int VEIL_ALPHA = 172;

    /** Opacidad de la hoja de los paneles (v0.187.0). */
    private static final int SHEET_ALPHA = 243;

    /** Filete del borde del velo. */
    private static final int VEIL_EDGE = 0x70422810;

    public enum PaperKind {
        LEDGER, CORRESPONDENCE, ARCHIVE, TECHNICAL, MEDICAL, OFFICIAL, DEFAULT
    }

    private PaperTexture() {
    }

    /** Color base de cada stock de papel (sin alfa: el velo le pone el suyo). */
    private static int baseColorFor(PaperKind kind) {
        if (kind == null) return DiaryTheme.PAPER;
        return switch (kind) {
            case LEDGER -> 0xFFE9DDC2;         // librador, algo mas oscuro
            case CORRESPONDENCE -> 0xFFEFEBD0; // carta suelta
            case ARCHIVE -> 0xFFE2CF9E;        // archivo envejecido
            case TECHNICAL -> 0xFFE6DBC0;      // ficha tecnica
            case MEDICAL -> 0xFFECE8C8;        // parte clinico
            case OFFICIAL -> 0xFFE8D7B0;       // oficio con sello
            case DEFAULT -> DiaryTheme.PAPER;
        };
    }

    /**
     * v0.187.0: la hoja de papel de verdad, para los paneles de las pantallas.
     *
     * El lector aclaro que cuando pidio sacar "las hojas" hablaba de los
     * periodicos de la portada, no de esto: los paneles del diario vuelven a ser
     * papel. Se pinta opaco (243 de 255), con su filete interior, tres manchas y
     * dos fibras deterministas —las mismas siempre para el mismo panel, sin RNG
     * por cuadro— y sin marca de agua, sello ni gotas, que eso si sobraba.
     */
    public static void drawSheet(GuiGraphics g, int x, int y, int w, int h) {
        drawSheet(g, x, y, w, h, PaperKind.DEFAULT);
    }

    public static void drawSheet(GuiGraphics g, int x, int y, int w, int h, PaperKind kind) {
        if (g == null || w <= 0 || h <= 0) return;
        int paper = baseColorFor(kind);
        g.fill(x, y, x + w, y + h, DiaryTheme.withAlpha(paper, SHEET_ALPHA));

        // Filete interior: la hoja se apoya en el borde, no flota.
        int inner = DiaryTheme.withAlpha(0x00422A12, 70);
        g.fill(x, y, x + w, y + 1, inner);
        g.fill(x, y + h - 1, x + w, y + h, DiaryTheme.withAlpha(0x00422A12, 110));
        g.fill(x, y, x + 1, y + h, DiaryTheme.withAlpha(0x00422A12, 60));
        g.fill(x + w - 1, y, x + w, y + h, DiaryTheme.withAlpha(0x00422A12, 60));

        // Manchas y fibras: posiciones derivadas del panel, sin sortear nada.
        int seed = (x * 73856093) ^ (y * 19349663) ^ (w * 83492791) ^ (h * 297121507);
        for (int i = 0; i < 3; i++) {
            int r = mix(seed + i * 7919);
            int size = 6 + Math.floorMod(r >>> 5, 10);
            int sx = x + 6 + Math.floorMod(r, Math.max(1, w - size - 12));
            int sy = y + 6 + Math.floorMod(r >>> 11, Math.max(1, h - size - 12));
            g.fill(sx, sy, sx + size, sy + size, DiaryTheme.withAlpha(0x006B5533, 16 + Math.floorMod(r >>> 17, 10)));
        }
        for (int i = 0; i < 2; i++) {
            int r = mix(seed ^ (i * 0x9E3779B9));
            int fy = y + 8 + Math.floorMod(r, Math.max(1, h - 16));
            int fx = x + 8 + Math.floorMod(r >>> 7, Math.max(1, w / 3));
            int fw = Math.max(6, Math.floorMod(r >>> 13, Math.max(7, w / 2)));
            g.fill(fx, fy, Math.min(x + w - 8, fx + fw), fy + 1, DiaryTheme.withAlpha(0x008A6A2D, 22));
        }
        // v0.221.0: la hoja envejece en todas las pantallas.
        drawAge(g, x, y, w, h, seed ^ 0x27D4EB2D);
    }

    /**
     * v0.221.0: la hoja envejece.
     *
     * El lector pidio papel de verdad: hojas rasgadas, bordes desgastados,
     * manchas de humedad y suciedad en todas las pantallas. Esto se suma a la
     * hoja base de cada panel, siempre derivado de su geometria (mismas manchas
     * para el mismo panel, sin RNG por cuadro) y con un puñado de rellenos.
     */
    /**
     * v0.222.0: el envejecido, para las hojas que se pintan a mano (la pausa
     * tiene su propio pincel de papel).
     */
    public static void drawWear(GuiGraphics g, int x, int y, int w, int h) {
        if (g == null || w <= 0 || h <= 0) return;
        int seed = (x * 73856093) ^ (y * 19349663) ^ (w * 83492791) ^ (h * 297121507);
        drawAge(g, x, y, w, h, seed ^ 0x27D4EB2D);
    }

    private static void drawAge(GuiGraphics g, int x, int y, int w, int h, int seed) {
        // Envejecido general: la hoja se oscurece hacia los bordes.
        g.fill(x, y, x + w, y + 2, DiaryTheme.withAlpha(0x006B5533, 16));
        g.fill(x, y + h - 2, x + w, y + h, DiaryTheme.withAlpha(0x006B5533, 22));
        g.fill(x, y, x + 2, y + h, DiaryTheme.withAlpha(0x006B5533, 12));
        g.fill(x + w - 2, y, x + w, y + h, DiaryTheme.withAlpha(0x006B5533, 12));

        // Dos manchas de humedad: un centro y su halo irregular.
        for (int i = 0; i < 2; i++) {
            int r = mix(seed + i * 6151);
            int size = 10 + Math.floorMod(r >>> 7, 16);
            int sx = x + 8 + Math.floorMod(r, Math.max(1, w - size - 16));
            int sy = y + 10 + Math.floorMod(r >>> 12, Math.max(1, h - size - 20));
            int damp = DiaryTheme.withAlpha(0x005A4A28, 9);
            int dampEdge = DiaryTheme.withAlpha(0x005A4A28, 15);
            g.fill(sx, sy, sx + size, sy + size, damp);
            g.fill(sx - 2, sy + 2, sx + size + 2, sy + size - 2, damp);
            g.fill(sx + 2, sy - 2, sx + size - 2, sy + size + 2, damp);
            g.fill(sx + 2, sy + size - 2, sx + size - 2, sy + size, dampEdge);
        }

        // Suciedad: motas sueltas y un arrastre corto.
        for (int i = 0; i < 5; i++) {
            int r = mix(seed ^ (i * 0x2545F491));
            int dx = x + 4 + Math.floorMod(r, Math.max(1, w - 10));
            int dy = y + 4 + Math.floorMod(r >>> 9, Math.max(1, h - 10));
            int len = 1 + Math.floorMod(r >>> 17, 3);
            g.fill(dx, dy, dx + len, dy + 1, DiaryTheme.withAlpha(0x00402A12, 20 + Math.floorMod(r >>> 21, 16)));
        }
        int trail = mix(seed ^ 0x5BD1E995);
        int tx = x + 6 + Math.floorMod(trail, Math.max(1, w / 2));
        int ty = y + 12 + Math.floorMod(trail >>> 11, Math.max(1, h - 24));
        g.fill(tx, ty, tx + 7 + Math.floorMod(trail >>> 5, 10), ty + 1, DiaryTheme.withAlpha(0x00483018, 14));
    }

    /**
     * v0.221.0: hoja rasgada. El borde superior y el inferior se comen a
     * mordiscos irregulares, y el resto es la misma hoja envejecida.
     */
    public static void drawTornSheet(GuiGraphics g, int x, int y, int w, int h, PaperKind kind) {
        if (g == null || w <= 0 || h <= 0) return;
        int paper = baseColorFor(kind);
        int seed = (x * 73856093) ^ (y * 19349663) ^ (w * 83492791) ^ (h * 297121507);
        // Cuerpo, un poco mas corto arriba y abajo para dejar el desgarro.
        g.fill(x, y + 3, x + w, y + h - 3, DiaryTheme.withAlpha(paper, SHEET_ALPHA));
        drawAge(g, x, y + 3, w, h - 6, seed);
        drawTornEdge(g, x, y, w, paper, true, seed);
        drawTornEdge(g, x, y + h - 4, w, paper, false, seed ^ 0x9E3779B9);
        // Filete interior tenue, para que la hoja no quede suelta.
        int inner = DiaryTheme.withAlpha(0x00422A12, 40);
        g.fill(x + 4, y + 6, x + w - 4, y + 7, inner);
        g.fill(x + 4, y + h - 7, x + w - 4, y + h - 6, inner);
    }

    /** Un borde rasgado: picos de ancho variable y alguna esquirla desprendida. */
    private static void drawTornEdge(GuiGraphics g, int x, int y, int w, int paper, boolean top, int seed) {
        int step = Math.max(6, w / 26);
        int offset = 0;
        int i = 0;
        while (offset < w) {
            int r = mix(seed + i * 7919);
            int len = Math.min(w - offset, step + Math.floorMod(r, step));
            int notch = 1 + Math.floorMod(r >>> 6, 3);
            int col = DiaryTheme.withAlpha(paper, 236);
            if (top) {
                g.fill(x + offset, y + 3 - notch, x + offset + len, y + 3, col);
            } else {
                g.fill(x + offset, y + 1, x + offset + len, y + 1 + notch, col);
            }
            // Esquirla que se desprende cada tanto.
            if (Math.floorMod(r >>> 13, 5) == 0) {
                int chipX = x + offset + len / 2;
                if (top) g.fill(chipX, y - 1, chipX + 2, y + 2, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 90));
                else    g.fill(chipX, y + 3, chipX + 2, y + 5, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 90));
            }
            offset += len;
            i++;
        }
    }

    /** Costura entre dos fragmentos de hoja: un desgarro a lo ancho. */
    public static void drawTornSeam(GuiGraphics g, int x, int y, int w, int seed) {
        int step = Math.max(5, w / 30);
        g.fill(x, y, x + w, y + 1, DiaryTheme.withAlpha(0x00422810, 26));
        int offset = 0;
        int i = 0;
        while (offset < w) {
            int r = mix(seed + i * 3571);
            int len = Math.min(w - offset, step + Math.floorMod(r, step));
            int notch = 1 + Math.floorMod(r >>> 8, 2);
            g.fill(x + offset, y + 1, x + offset + len, y + 1 + notch, DiaryTheme.withAlpha(0x00422810, 18));
            offset += len;
            i++;
        }
    }

    /** Mezcla entera barata para derivar posiciones deterministas. */
    private static int mix(int value) {
        int h = value * 0x27D4EB2D;
        h ^= h >>> 15;
        h *= 0x85EBCA6B;
        h ^= h >>> 13;
        return h;
    }

    public static void drawVeil(GuiGraphics g, int x, int y, int w, int h) {
        drawVeil(g, x, y, w, h, PaperKind.DEFAULT);
    }

    /**
     * El velo de un panel: un relleno translucido y cuatro filetes de un pixel.
     * Se ve el fondo de la escena, la tinta oscura se lee encima, y cada panel
     * cuesta cinco fills en vez de cuarenta.
     */
    public static void drawVeil(GuiGraphics g, int x, int y, int w, int h, PaperKind kind) {
        if (g == null || w <= 0 || h <= 0) return;
        g.fill(x, y, x + w, y + h, DiaryTheme.withAlpha(baseColorFor(kind), VEIL_ALPHA));
        g.fill(x, y, x + w, y + 1, VEIL_EDGE);
        g.fill(x, y + h - 1, x + w, y + h, VEIL_EDGE);
        g.fill(x, y, x + 1, y + h, VEIL_EDGE);
        g.fill(x + w - 1, y, x + w, y + h, VEIL_EDGE);
    }
}
