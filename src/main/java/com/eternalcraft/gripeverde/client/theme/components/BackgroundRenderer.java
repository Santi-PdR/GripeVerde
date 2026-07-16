package com.eternalcraft.gripeverde.client.theme.components;

import com.eternalcraft.gripeverde.client.theme.GhoulPalette;
import net.minecraft.client.gui.GuiGraphics;

/**
 * BackgroundRenderer — dibuja fondos de pantalla completos.
 * Responsabilidad única: fondo.
 */
public final class BackgroundRenderer {

    private BackgroundRenderer() {}

    /** Fondo sólido plano. */
    public static void drawSolid(GuiGraphics g, int w, int h, int color) {
        g.fill(0, 0, w, h, color);
    }

    /** Fondo base + tinte temático superpuesto. */
    public static void drawTinted(GuiGraphics g, int w, int h, int baseColor, int tintRGB, int tintAlpha) {
        g.fill(0, 0, w, h, baseColor);
        if (tintAlpha > 0 && (tintRGB & 0x00FFFFFF) != 0) {
            g.fill(0, 0, w, h, GhoulPalette.withAlpha(tintRGB, tintAlpha));
        }
    }

    /** Viñeta radial oscura hacia los bordes. */
    public static void drawVignette(GuiGraphics g, int w, int h, int maxAlpha, int tintRGB) {
        int margin = Math.min(w, h) / 6;
        for (int i = 0; i < margin; i++) {
            float t = 1f - (float) i / margin;
            int a = (int) (t * t * maxAlpha);
            int c = GhoulPalette.withAlpha(tintRGB, a);
            g.fill(i, 0, i + 1, h, c);
            g.fill(w - i - 1, 0, w - i, h, c);
            g.fill(0, i, w, i + 1, c);
            g.fill(0, h - i - 1, w, h - i, c);
        }
    }
}