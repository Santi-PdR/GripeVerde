package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;

public final class CandleLight {

    private CandleLight() {}

    public static void draw(GuiGraphics g, int cx, int cy, int baseRadius, long ticks) {
        double t = ticks * 0.15;
        int flicker = (int)(Math.sin(t) * 4 + Math.sin(t * 2.7) * 2 + Math.sin(t * 5.1) * 1);
        int radius = baseRadius + flicker;

        // Halo exterior mas grande y visible
        for (int r = radius; r > 0; r -= 1) {
            float pct = (float)(radius - r) / radius;
            int alpha = (int)(220 * pct * pct);
            int col = DiaryTheme.withAlpha(DiaryTheme.CANDLE_WAX, alpha);
            drawRing(g, cx, cy, r, col);
        }

        // Nucleo brillante
        int coreR = 4 + Math.abs(flicker / 2);
        PaperTexture.drawSolidBlob(g, cx, cy, coreR, DiaryTheme.withAlpha(DiaryTheme.BONE_WHITE, 250));

        // Punto blanco central
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFFFFFFFF);
    }

    public static void drawStatic(GuiGraphics g, int cx, int cy, int radius) {
        for (int r = radius; r > 0; r -= 1) {
            float pct = (float)(radius - r) / radius;
            int alpha = (int)(160 * pct * pct);
            int col = DiaryTheme.withAlpha(DiaryTheme.CANDLE_WAX, alpha);
            drawRing(g, cx, cy, r, col);
        }
        PaperTexture.drawSolidBlob(g, cx, cy, 3, DiaryTheme.BONE_WHITE);
    }

    private static void drawRing(GuiGraphics g, int cx, int cy, int radius, int col) {
        int r2 = radius * radius;
        int rInner2 = (radius - 1) * (radius - 1);
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int d2 = dx * dx + dy * dy;
                if (d2 <= r2 && d2 >= rInner2) {
                    g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, col);
                }
            }
        }
    }
}