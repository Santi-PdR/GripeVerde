package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import java.util.Random;

public final class PaperTexture {

    private static final long SEED = 189011974L;
    private static final long BLOOD_SEED = 66613337L;

    private PaperTexture() {}

    public static void draw(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, DiaryTheme.PAPER);
        drawVignette(g, x, y, w, h);
        drawStains(g, x, y, w, h);
        drawWrinkles(g, x, y, w, h);
        drawBloodMist(g, x, y, w, h);
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
        int stainCount = Math.max(12, (w * h) / 15000);
        for (int i = 0; i < stainCount; i++) {
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
            int wy = y + rnd.nextInt(h);
            int startX = x + rnd.nextInt(w / 3);
            int endX = startX + w / 3 + rnd.nextInt(w / 2);
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
}