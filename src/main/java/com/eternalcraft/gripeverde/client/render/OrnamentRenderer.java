package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * OrnamentRenderer v0.4.0
 * Marcos y ornamentos victorianos.
 * Reemplaza los corner brackets tecnologicos por filigranas.
 */
@OnlyIn(Dist.CLIENT)
public class OrnamentRenderer {

    /**
     * Panel de papel envejecido con sello de cera.
     */
    public static void drawPaperPanel(GuiGraphics g, int x, int y, int w, int h) {
        // The sheet has hand-cut corners rather than a perfect machine rectangle.
        g.fill(x + 2, y + 2, x + w + 2, y + h + 2,
            DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 72));
        if (w < 14 || h < 14) {
            g.fill(x, y, x + w, y + h, DiaryTheme.PAPER);
        } else {
            int[] cornerInset = {7, 4, 2, 1, 0, 1, 2};
            for (int row = 0; row < cornerInset.length; row++) {
                int inset = cornerInset[row];
                g.fill(x + inset, y + row, x + w - inset, y + row + 1, DiaryTheme.PAPER);
                g.fill(x + inset, y + h - row - 1, x + w - inset, y + h - row, DiaryTheme.PAPER);
            }
            g.fill(x, y + 7, x + w, y + h - 7, DiaryTheme.PAPER);
        }

        // Manchas deterministas: se calculan una vez por panel y se reutilizan.
        int[] stains = stainPattern(x, y, w, h);
        for (int i = 0; i < 8 && stains.length > 0; i++) {
            int offset = i * 4;
            int size = stains[offset + 2];
            int sx = Math.max(x + 4, Math.min(x + w - size - 4, x + stains[offset]));
            int sy = Math.max(y + 4, Math.min(y + h - size - 4, y + stains[offset + 1]));
            int stainAlpha = stains[offset + 3];
            g.fill(sx, sy, sx + size, sy + size,
                AtmosphereRenderer.withAlpha(0x006B5533, stainAlpha));
        }

        // Bordes desgastados (mas oscuros)
        if (w < 14 || h < 14) {
            g.fill(x, y,         x + w, y + 1,     DiaryTheme.PAPER_DARK);
            g.fill(x, y + h - 1, x + w, y + h,     DiaryTheme.PAPER_DARK);
            g.fill(x, y,         x + 1, y + h,     DiaryTheme.PAPER_DARK);
            g.fill(x + w - 1, y, x + w, y + h,     DiaryTheme.PAPER_DARK);
        } else {
            int[] cornerInset = {7, 4, 2, 1, 0, 1, 2};
            for (int row = 0; row < cornerInset.length; row++) {
                int inset = cornerInset[row];
                g.fill(x + inset, y + row, x + w - inset, y + row + 1, DiaryTheme.PAPER_DARK);
                g.fill(x + inset, y + h - row - 1, x + w - inset, y + h - row, DiaryTheme.PAPER_DARK);
            }
            g.fill(x, y + 7, x + 1, y + h - 7, DiaryTheme.PAPER_DARK);
            g.fill(x + w - 1, y + 7, x + w, y + h - 7, DiaryTheme.PAPER_DARK);
        }

        // A faint inset rule and broken fibers make the paper feel handled, not cut by a GUI frame.
        if (w > 28 && h > 28) {
            int fiber = DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 48);
            g.fill(x + 5, y + 6, x + w - 5, y + 7, fiber);
            g.fill(x + 5, y + h - 7, x + w - 5, y + h - 6, fiber);
            g.fill(x + 5, y + 8, x + 6, y + h - 8, fiber);
            g.fill(x + w - 6, y + 8, x + w - 5, y + h - 8, fiber);
            g.fill(x + 9, y + h - 3, x + 15, y + h - 2, DiaryTheme.PAPER_DARK);
            g.fill(x + w - 18, y + 2, x + w - 12, y + 3, DiaryTheme.PAPER_STAINED);
        }
    }

    private static final int STAIN_CACHE_SIZE = 64;
    private static final int STAIN_CACHE_MASK = STAIN_CACHE_SIZE - 1;
    private static final long[] STAIN_CACHE_KEYS = new long[STAIN_CACHE_SIZE];
    private static final int[][] STAIN_CACHE_VALUES = new int[STAIN_CACHE_SIZE][];
    private static final int[] EMPTY_STAIN_PATTERN = new int[0];

    /** Render-thread cache; avoids Random and key allocations on every frame. */
    private static int[] stainPattern(int x, int y, int w, int h) {
        if (w <= 4 || h <= 4) return EMPTY_STAIN_PATTERN;

        long key = ((long)(x & 0xFFFF) << 48)
            | ((long)(y & 0xFFFF) << 32)
            | ((long)(w & 0xFFFF) << 16)
            | (long)(h & 0xFFFF);
        long seed = x * 31L + y * 17L + w;
        long mixed = key;
        mixed ^= mixed >>> 33;
        mixed *= 0xff51afd7ed558ccdL;
        mixed ^= mixed >>> 33;
        mixed *= 0xc4ceb9fe1a85ec53L;
        mixed ^= mixed >>> 33;
        int start = (int)(mixed ^ (mixed >>> 32)) & STAIN_CACHE_MASK;
        int slot = start;

        for (int probe = 0; probe < STAIN_CACHE_SIZE; probe++) {
            if (STAIN_CACHE_KEYS[slot] == key) return STAIN_CACHE_VALUES[slot];
            if (STAIN_CACHE_KEYS[slot] == 0L) {
                int[] pattern = createStainPattern(seed, w, h);
                STAIN_CACHE_KEYS[slot] = key;
                STAIN_CACHE_VALUES[slot] = pattern;
                return pattern;
            }
            slot = (slot + 1) & STAIN_CACHE_MASK;
        }

        int[] pattern = createStainPattern(seed, w, h);
        STAIN_CACHE_KEYS[start] = key;
        STAIN_CACHE_VALUES[start] = pattern;
        return pattern;
    }

    /** Keeps the same seeded pattern and draw order as the original renderer. */
    private static int[] createStainPattern(long seed, int w, int h) {
        java.util.Random rng = new java.util.Random(seed);
        int[] pattern = new int[8 * 4];
        for (int i = 0; i < 8; i++) {
            int offset = i * 4;
            pattern[offset] = rng.nextInt(w - 4);
            pattern[offset + 1] = rng.nextInt(h - 4);
            pattern[offset + 2] = 2 + rng.nextInt(3);
            pattern[offset + 3] = 30 + rng.nextInt(40);
        }
        return pattern;
    }

    /**
     * Sello de cera rojo (para posters oficiales).
     */
    public static void drawWaxSeal(GuiGraphics g, int cx, int cy, int radius) {
        // Cuerpo del sello (rojo sangre)
        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int) Math.sqrt(radius * radius - dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, DiaryTheme.WAX_SEAL);
        }
        // Highlight superior izquierdo
        for (int dy = -radius; dy <= 0; dy++) {
            int dx = (int) Math.sqrt(radius * radius - dy * dy);
            g.fill(cx - dx, cy + dy, cx, cy + dy + 1,
                AtmosphereRenderer.withAlpha(0x008B3020, 80));
        }
        // Sombra inferior derecha
        for (int dy = 0; dy <= radius; dy++) {
            int dx = (int) Math.sqrt(radius * radius - dy * dy);
            g.fill(cx, cy + dy, cx + dx + 1, cy + dy + 1,
                AtmosphereRenderer.withAlpha(0x00000000, 60));
        }
        // Iniciales en el centro
        int crossSize = radius / 2;
        g.fill(cx - crossSize, cy - 1, cx + crossSize + 1, cy + 1, 0xFF3D0A05);
        g.fill(cx - 1, cy - crossSize, cx + 1, cy + crossSize + 1, 0xFF3D0A05);
    }
}
