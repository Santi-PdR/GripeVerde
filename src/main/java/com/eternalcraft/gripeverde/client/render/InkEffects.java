package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import java.util.Random;

public final class InkEffects {

    private InkEffects() {}

    public static void bloodSplatter(GuiGraphics g, int cx, int cy, int size, long seed) {
        Random rnd = new Random(seed);
        PaperTexture.drawSoftBlob(g, cx, cy, size + 2, DiaryTheme.BLOOD_DRIED);
        PaperTexture.drawSoftBlob(g, cx, cy, size, DiaryTheme.BLOOD_FRESH);

        int bigDrops = 5 + rnd.nextInt(5);
        for (int i = 0; i < bigDrops; i++) {
            double angle = rnd.nextDouble() * Math.PI * 2;
            int dist = size + rnd.nextInt(size * 2);
            int dx = (int)(Math.cos(angle) * dist);
            int dy = (int)(Math.sin(angle) * dist);
            int dropSize = 2 + rnd.nextInt(size / 2 + 2);
            int col = rnd.nextInt(2) == 0 ? DiaryTheme.BLOOD_FRESH : DiaryTheme.BLOOD_DRIED;
            PaperTexture.drawSoftBlob(g, cx + dx, cy + dy, dropSize, col);
        }

        int fineDrops = 15 + rnd.nextInt(10);
        for (int i = 0; i < fineDrops; i++) {
            double angle = rnd.nextDouble() * Math.PI * 2;
            int dist = size + rnd.nextInt(size * 4);
            int dx = (int)(Math.cos(angle) * dist);
            int dy = (int)(Math.sin(angle) * dist);
            int fineSize = 1 + rnd.nextInt(2);
            PaperTexture.drawSoftBlob(g, cx + dx, cy + dy, fineSize, DiaryTheme.BLOOD_DRIED);
        }

        double mainAngle = rnd.nextDouble() * Math.PI * 2;
        int trailLen = size + rnd.nextInt(size * 2);
        for (int t = 1; t < trailLen; t++) {
            int tx = cx + (int)(Math.cos(mainAngle) * t);
            int ty = cy + (int)(Math.sin(mainAngle) * t);
            int trailSize = Math.max(1, (trailLen - t) / 4);
            int alpha = 200 - (t * 200 / trailLen);
            int col = DiaryTheme.withAlpha(DiaryTheme.BLOOD_DRIED, alpha);
            PaperTexture.drawSoftBlob(g, tx, ty, trailSize, col);
        }
    }

    /** Mancha de tinta pequenia y densa. */
    public static void inkBlot(GuiGraphics g, int cx, int cy, int size) {
        PaperTexture.drawSoftBlob(g, cx, cy, size + 1, DiaryTheme.INK);
        PaperTexture.drawSolidBlob(g, cx, cy, Math.max(1, size / 2), DiaryTheme.INK);
    }

    /** Mancha de tinta CORRIDA con rastro descendente visible. Size recomendado: 4-8. */
    public static void smearedInk(GuiGraphics g, int cx, int cy, int size) {
        // Mancha principal solida
        PaperTexture.drawSolidBlob(g, cx, cy, size, DiaryTheme.INK);
        // Halo suave alrededor
        PaperTexture.drawSoftBlob(g, cx, cy, size + 3, DiaryTheme.withAlpha(DiaryTheme.INK, 120));

        // Rastro descendente grueso al principio, delgado al final
        int trailLen = size * 4;
        for (int i = 1; i < trailLen; i++) {
            float pct = 1f - ((float)i / trailLen);
            int trailSize = Math.max(1, (int)(size * pct * 0.8f));
            int alpha = (int)(220 * pct);
            PaperTexture.drawSoftBlob(g, cx, cy + i, trailSize, DiaryTheme.withAlpha(DiaryTheme.INK, alpha));
        }

        // Gotitas laterales del rastro
        Random rnd = new Random(cx * 17L + cy * 31L);
        for (int i = 0; i < 4; i++) {
            int ox = (rnd.nextInt(size * 2) - size);
            int oy = size + rnd.nextInt(trailLen - size);
            int dotSize = 1 + rnd.nextInt(2);
            PaperTexture.drawSoftBlob(g, cx + ox, cy + oy, dotSize, DiaryTheme.withAlpha(DiaryTheme.INK, 180));
        }
    }

    public static void handwrittenUnderline(GuiGraphics g, int x1, int y, int x2, int color) {
        int len = x2 - x1;
        if (len <= 0) return;
        Random rnd = new Random(x1 * 31L + y * 17L);
        for (int i = 0; i < len; i++) {
            int py = y + (rnd.nextInt(3) - 1);
            g.fill(x1 + i, py, x1 + i + 1, py + 1, color);
        }
    }

    public static void quillStroke(GuiGraphics g, int x1, int y, int x2, int color) {
        int len = x2 - x1;
        if (len <= 0) return;
        Random rnd = new Random(x1 * 41L + y * 23L);
        for (int i = 0; i < len; i++) {
            int py = y + (rnd.nextInt(3) - 1);
            float centerPct = 1f - Math.abs((float)i - len / 2f) / (len / 2f);
            int thickness = 1 + (int)(centerPct * 2);
            for (int t = 0; t < thickness; t++) {
                g.fill(x1 + i, py + t, x1 + i + 1, py + t + 1, color);
            }
        }
    }

    public static void waxSeal(GuiGraphics g, int cx, int cy, int radius, String text) {
        PaperTexture.drawSoftBlob(g, cx + 2, cy + 2, radius + 1, DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 100));
        PaperTexture.drawSolidBlob(g, cx, cy, radius, DiaryTheme.WAX_SEAL);
        PaperTexture.drawSolidBlob(g, cx, cy, radius - 2, DiaryTheme.BLOOD_FRESH);
        PaperTexture.drawSoftBlob(g, cx - radius / 3, cy - radius / 3, radius / 3,
                DiaryTheme.withAlpha(DiaryTheme.BONE_WHITE, 60));

        if (text != null && !text.isEmpty()) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            int tw = mc.font.width(text);
            int tx = cx - tw / 2;
            int ty = cy - 3;
            g.drawString(mc.font, text, tx + 1, ty + 1, DiaryTheme.INK_BLACK, false);
            g.drawString(mc.font, text, tx, ty, DiaryTheme.BONE_WHITE, false);
        }
    }

    public static void ornateSeparator(GuiGraphics g, int x1, int y, int x2, int color) {
        int cx = (x1 + x2) / 2;
        g.fill(x1, y, cx - 10, y + 1, color);
        g.fill(cx + 10, y, x2, y + 1, color);
        int fadeCol = DiaryTheme.withAlpha(color, 100);
        g.fill(x1 + 20, y + 2, cx - 15, y + 3, fadeCol);
        g.fill(cx + 15, y + 2, x2 - 20, y + 3, fadeCol);
        drawDiamond(g, cx, y, 4, color);
        drawDiamond(g, cx, y, 2, DiaryTheme.withAlpha(color, 180));
    }

    public static void drawDiamond(GuiGraphics g, int cx, int cy, int size, int color) {
        for (int i = 0; i < size; i++) {
            int w = size - i;
            g.fill(cx - w, cy - i, cx + w + 1, cy - i + 1, color);
            g.fill(cx - w, cy + i, cx + w + 1, cy + i + 1, color);
        }
    }

    /** Borde rasgado con dientes GRANDES tipo papel roto. */
    public static void tornEdge(GuiGraphics g, int x1, int y, int x2, int color, long seed) {
        Random rnd = new Random(seed);
        int i = x1;
        while (i < x2) {
            // Ancho de diente: 3-8 pixels
            int toothWidth = 3 + rnd.nextInt(6);
            // Altura del diente: 2-6 pixels
            int toothHeight = 2 + rnd.nextInt(5);

            // Rellenar el diente triangular hacia arriba
            for (int dx = 0; dx < toothWidth && i + dx < x2; dx++) {
                // Forma triangular: mas alto en el centro
                float pct = 1f - Math.abs(dx - toothWidth / 2f) / (toothWidth / 2f);
                int h = (int)(toothHeight * pct);
                g.fill(i + dx, y - h, i + dx + 1, y + 1, color);
            }

            // Rellenar linea base
            g.fill(i, y, i + toothWidth, y + 1, color);

            i += toothWidth;
        }
    }
}