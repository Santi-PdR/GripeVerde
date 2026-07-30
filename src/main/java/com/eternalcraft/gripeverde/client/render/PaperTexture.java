package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import java.util.Random;

public final class PaperTexture {

    private static final long SEED = 189011974L;
    private static int openCount = 0;
    public static void incrementOpenCount() { openCount++; }
    public static int getOpenCount() { return openCount; }
    private static final long BLOOD_SEED = 66613337L;

    private PaperTexture() {}

    public static void draw(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, DiaryTheme.PAPER);
        drawVignette(g, x, y, w, h);
        drawStains(g, x, y, w, h);
        drawWrinkles(g, x, y, w, h);
        drawBloodMist(g, x, y, w, h);
    }

    /** Variante sin wrinkles: para paneles con contenido scrolleable donde
     *  las lineas horizontales se confunden con el texto. */
    public static void drawClean(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, DiaryTheme.PAPER);
        drawWatermark(g, x, y, w, h);
        drawPaperFibers(g, x, y, w, h);
        drawVignette(g, x, y, w, h);
        drawStains(g, x, y, w, h);
        drawCoffeeStain(g, x, y, w, h);
        drawBloodMist(g, x, y, w, h);
        drawTornEdges(g, x, y, w, h);
        drawWaxSeal(g, x, y, w, h);
    }

    /** Fondo bloody: menos salpicaduras y mas chicas para no lagear. */
    public static void drawBloody(GuiGraphics g, int x, int y, int w, int h) {
        draw(g, x, y, w, h);
        // Solo 3-4 salpicaduras MEDIANAS con seed fijo (deterministas)
        Random rnd = new Random(BLOOD_SEED + 42);
        int splatters = 3 + rnd.nextInt(2);
        for (int i = 0; i < splatters; i++) {
            int cx = x + 50 + rnd.nextInt(Math.max(1, w - 100));
            int cy = y + 50 + rnd.nextInt(Math.max(1, h - 100));
            int size = 5 + rnd.nextInt(4);
            drawSimpleBloodBlob(g, cx, cy, size);
        }
    }

    /** Salpicadura ligera para el modo bloody - sin trails. */
    private static void drawSimpleBloodBlob(GuiGraphics g, int cx, int cy, int size) {
        drawSoftBlob(g, cx, cy, size + 1, DiaryTheme.BLOOD_DRIED);
        drawSoftBlob(g, cx, cy, size, DiaryTheme.BLOOD_FRESH);
        // Solo 4-5 gotitas
        Random rnd = new Random((long)cx * 31 + cy * 17);
        for (int i = 0; i < 5; i++) {
            double angle = rnd.nextDouble() * Math.PI * 2;
            int dist = size + rnd.nextInt(size);
            int dx = (int)(Math.cos(angle) * dist);
            int dy = (int)(Math.sin(angle) * dist);
            drawSoftBlob(g, cx + dx, cy + dy, 1 + rnd.nextInt(2), DiaryTheme.BLOOD_DRIED);
        }
    }

    private static void drawVignette(GuiGraphics g, int x, int y, int w, int h) {
        int band = 20;
        for (int i = 0; i < band; i++) {
            int alpha = (int)(80 * (1f - (float)i / band));
            int col = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, alpha);
            g.fill(x, y + i, x + w, y + i + 1, col);
            g.fill(x, y + h - i - 1, x + w, y + h - i, col);
        }
        for (int i = 0; i < band; i++) {
            int alpha = (int)(80 * (1f - (float)i / band));
            int col = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, alpha);
            g.fill(x + i, y, x + i + 1, y + h, col);
            g.fill(x + w - i - 1, y, x + w - i, y + h, col);
        }
    }

    private static void drawStains(GuiGraphics g, int x, int y, int w, int h) {
        Random rnd = new Random(SEED);
        int stainCount = Math.max(12, (w * h) / 15000) + Math.min(30, openCount);
        for (int i = 0; i < stainCount; i++) {
            if (w <= 0 || h <= 0) continue;
            int cx = x + rnd.nextInt(w);
            int cy = y + rnd.nextInt(h);
            int radius = 4 + rnd.nextInt(14);
            int alpha = 15 + rnd.nextInt(50);
            int roll = rnd.nextInt(5);
            int col;
            if (roll == 0) col = DiaryTheme.withAlpha(DiaryTheme.INK_FADED, alpha);
            else if (roll == 1) col = DiaryTheme.withAlpha(DiaryTheme.INK, alpha / 2);
            else col = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, alpha);
            drawSoftBlob(g, cx, cy, radius, col);
        }
    }

    private static void drawWrinkles(GuiGraphics g, int x, int y, int w, int h) {
        Random rnd = new Random(SEED + 1);
        int lines = Math.max(5, h / 40);
        for (int i = 0; i < lines; i++) {
            if (w < 6 || h <= 0) continue;
            int wy = y + rnd.nextInt(h);
            int w3 = Math.max(1, w / 3);
            int w2 = Math.max(1, w / 2);
            int startX = x + rnd.nextInt(w3);
            int endX = startX + w3 + rnd.nextInt(w2);
            if (endX > x + w) endX = x + w;
            int alpha = 30 + rnd.nextInt(35);
            int col = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, alpha);
            g.fill(startX, wy, endX, wy + 1, col);
            g.fill(startX, wy + 1, endX, wy + 2, DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, alpha / 3));
        }
    }

    private static void drawBloodMist(GuiGraphics g, int x, int y, int w, int h) {
        Random rnd = new Random(BLOOD_SEED);
        int drops = Math.max(4, (w * h) / 50000);
        for (int i = 0; i < drops; i++) {
            if (w <= 0 || h <= 0) continue;
            int cx = x + rnd.nextInt(w);
            int cy = y + rnd.nextInt(h);
            int size = 1 + rnd.nextInt(3);
            int alpha = 40 + rnd.nextInt(60);
            int col = rnd.nextInt(2) == 0
                    ? DiaryTheme.withAlpha(DiaryTheme.BLOOD_DRIED, alpha)
                    : DiaryTheme.withAlpha(DiaryTheme.WAX_SEAL, alpha);
            drawSoftBlob(g, cx, cy, size, col);
        }
    }

    public static void drawSoftBlob(GuiGraphics g, int cx, int cy, int radius, int col) {
        if (radius <= 0) return;
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int distSq = dx * dx + dy * dy;
                if (distSq <= radius * radius) {
                    float dist = (float) Math.sqrt(distSq);
                    float pct = 1f - (dist / radius);
                    int a = (int)((col >>> 24) * pct * pct);
                    int c = (a << 24) | (col & 0x00FFFFFF);
                    g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, c);
                }
            }
        }
    }

    public static void drawSolidBlob(GuiGraphics g, int cx, int cy, int radius, int col) {
        if (radius <= 0) return;
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, col);
                }
            }
        }
    }

    // ============================================================
    // Nuevos helpers de textura del pergamino (v0.12.2)
    // ============================================================

    private static final long COFFEE_SEED = 0xC0FFEE420L;
    private static final long WAX_SEED    = 0xF11EEDCEL;
    private static final long TORN_SEED   = 0x707E12345L;
    private static final long FIBER_SEED  = 0xF1BE12L;

    /** Mancha de cafe: circulo marron claro semi-transparente con
     *  ligera irregularidad. Posicion determinista por sesion (SEED). */
    public static void drawCoffeeStain(GuiGraphics g, int x, int y, int w, int h) {
        if (w < 60 || h < 60) return;
        Random rnd = new Random(COFFEE_SEED);
        // 1-2 manchas grandes + halo
        int count = 1 + rnd.nextInt(2);
        for (int i = 0; i < count; i++) {
            int cx = x + 20 + rnd.nextInt(Math.max(1, w - 40));
            int cy = y + 20 + rnd.nextInt(Math.max(1, h - 40));
            int size = 12 + rnd.nextInt(10);
            // Base de la mancha (mas oscuro en el centro)
            int coffeeDark  = DiaryTheme.withAlpha(0x00543818, 55);
            int coffeeLight = DiaryTheme.withAlpha(0x00A8783C, 30);
            drawSoftBlob(g, cx, cy, size, coffeeDark);
            drawSoftBlob(g, cx + 1, cy + 1, size + 3, coffeeLight);
            // Halo exterior mas claro y transparente
            int halo = DiaryTheme.withAlpha(0x00C8A868, 15);
            drawSoftBlob(g, cx, cy, size + 8, halo);
        }
    }

    /** Sello de cera roja en la esquina inferior derecha del panel.
     *  Circulo rojo oscuro con brillo, sombra e irregularidad. */
    public static void drawWaxSeal(GuiGraphics g, int panelX, int panelY, int panelW, int panelH) {
        if (panelW < 80 || panelH < 80) return;
        int cx = panelX + panelW - 24;
        int cy = panelY + panelH - 24;
        int r = 8;

        // Sombra bajo el sello
        int shadow = DiaryTheme.withAlpha(0x00000000, 90);
        drawSoftBlob(g, cx + 2, cy + 2, r + 2, shadow);

        // Cuerpo del sello: gradiente radial rojo oscuro
        int waxDark  = DiaryTheme.WAX_SEAL;
        int waxLight = DiaryTheme.BLOOD_FRESH;
        drawSolidBlob(g, cx, cy, r, waxDark);
        drawSolidBlob(g, cx - 1, cy - 1, r - 2, waxLight);

        // Highlight superior izquierdo (brillo cera)
        int hl = DiaryTheme.withAlpha(0x00FFAA88, 120);
        g.fill(cx - 3, cy - 3, cx - 1, cy - 2, hl);
        g.fill(cx - 2, cy - 4, cx, cy - 3, hl);

        // Cruz central (marca del sello) muy sutil
        int mark = DiaryTheme.withAlpha(0x00000000, 100);
        g.fill(cx - 2, cy, cx + 3, cy + 1, mark);
        g.fill(cx, cy - 2, cx + 1, cy + 3, mark);
    }

    /** Bordes rasgados sutiles: irregularidades en las 4 esquinas del panel. */
    public static void drawTornEdges(GuiGraphics g, int x, int y, int w, int h) {
        Random rnd = new Random(TORN_SEED);
        int col = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, 140);

        // Esquina superior izquierda: 3-5 muescas
        int notches = 3 + rnd.nextInt(3);
        for (int i = 0; i < notches; i++) {
            int px = x + rnd.nextInt(8);
            int py = y + rnd.nextInt(4);
            int sz = 1 + rnd.nextInt(2);
            g.fill(px, py, px + sz, py + sz, col);
        }
        // Esquina superior derecha
        for (int i = 0; i < notches; i++) {
            int px = x + w - 8 + rnd.nextInt(8);
            int py = y + rnd.nextInt(4);
            int sz = 1 + rnd.nextInt(2);
            g.fill(px, py, px + sz, py + sz, col);
        }
        // Esquina inferior izquierda
        for (int i = 0; i < notches; i++) {
            int px = x + rnd.nextInt(8);
            int py = y + h - 4 + rnd.nextInt(4);
            int sz = 1 + rnd.nextInt(2);
            g.fill(px, py, px + sz, py + sz, col);
        }
        // Esquina inferior derecha
        for (int i = 0; i < notches; i++) {
            int px = x + w - 8 + rnd.nextInt(8);
            int py = y + h - 4 + rnd.nextInt(4);
            int sz = 1 + rnd.nextInt(2);
            g.fill(px, py, px + sz, py + sz, col);
        }
    }

    /** Textura de fibras sutiles: puntos sepia dispersos sobre el pergamino. */
    public static void drawPaperFibers(GuiGraphics g, int x, int y, int w, int h) {
        Random rnd = new Random(FIBER_SEED);
        int fiberCount = Math.max(30, (w * h) / 500);
        int col1 = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, 20);
        int col2 = DiaryTheme.withAlpha(DiaryTheme.SEPIA_LIGHT, 12);
        for (int i = 0; i < fiberCount; i++) {
            int px = x + rnd.nextInt(w);
            int py = y + rnd.nextInt(h);
            // Fibras cortas horizontales de 2-4px
            int len = 2 + rnd.nextInt(3);
            int col = rnd.nextInt(3) == 0 ? col2 : col1;
            g.fill(px, py, px + len, py + 1, col);
        }
    }

    /** Marca de agua "EC" (EternalCraft) muy sutil en el centro del pergamino. */
    public static void drawWatermark(GuiGraphics g, int x, int y, int w, int h) {
        if (w < 100 || h < 100) return;
        int cx = x + w / 2;
        int cy = y + h / 2;
        int col = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, 25);

        // Letra E (7x9 escalado x2 = 14x18)
        int letterSize = 12;
        int gap = 4;
        int ex = cx - letterSize - gap / 2;
        int ey = cy - letterSize;
        // E: 3 barras horizontales + una vertical
        g.fill(ex, ey, ex + letterSize, ey + 2, col);
        g.fill(ex, ey + letterSize - 1, ex + letterSize, ey + letterSize + 1, col);
        g.fill(ex, ey + letterSize * 2 - 2, ex + letterSize, ey + letterSize * 2, col);
        g.fill(ex, ey, ex + 2, ey + letterSize * 2, col);

        // C
        int cxL = cx + gap / 2;
        int cyL = cy - letterSize;
        g.fill(cxL, cyL, cxL + letterSize, cyL + 2, col);
        g.fill(cxL, cyL + letterSize * 2 - 2, cxL + letterSize, cyL + letterSize * 2, col);
        g.fill(cxL, cyL, cxL + 2, cyL + letterSize * 2, col);
    }
}
