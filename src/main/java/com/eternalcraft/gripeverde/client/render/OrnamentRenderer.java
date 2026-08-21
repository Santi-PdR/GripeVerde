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
        // Papel base
        g.fill(x, y, x + w, y + h, DiaryTheme.PAPER);

        // Manchas de humedad (puntos aleatorios oscuros)
        java.util.Random rng = new java.util.Random((long)(x * 31L + y * 17L + w));
        for (int i = 0; i < 8; i++) {
            if (w <= 4 || h <= 4) continue;
            int sx = x + rng.nextInt(w - 4);
            int sy = y + rng.nextInt(h - 4);
            int size = 2 + rng.nextInt(3);
            int stainAlpha = 30 + rng.nextInt(40);
            g.fill(sx, sy, sx + size, sy + size,
                AtmosphereRenderer.withAlpha(0x006B5533, stainAlpha));
        }

        // Bordes desgastados (mas oscuros)
        g.fill(x, y,         x + w, y + 1,     DiaryTheme.PAPER_DARK);
        g.fill(x, y + h - 1, x + w, y + h,     DiaryTheme.PAPER_DARK);
        g.fill(x, y,         x + 1, y + h,     DiaryTheme.PAPER_DARK);
        g.fill(x + w - 1, y, x + w, y + h,     DiaryTheme.PAPER_DARK);
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