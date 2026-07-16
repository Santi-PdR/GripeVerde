package com.eternalcraft.gripeverde.client.theme.components;

import com.eternalcraft.gripeverde.client.theme.GhoulPalette;
import com.eternalcraft.gripeverde.client.theme.UIScale;
import net.minecraft.client.gui.GuiGraphics;

/**
 * FrameRenderer — dibuja marcos perimetrales y esquinas ornamentales.
 * Responsabilidad única: marcos.
 */
public final class FrameRenderer {

    private FrameRenderer() {}

    /** Borde rectangular hueco de grosor variable. */
    public static void drawBorder(GuiGraphics g, int x, int y, int w, int h,
                                    int thickness, int color) {
        g.fill(x, y, x + w, y + thickness, color);
        g.fill(x, y + h - thickness, x + w, y + h, color);
        g.fill(x, y, x + thickness, y + h, color);
        g.fill(x + w - thickness, y, x + w, y + h, color);
    }

    /** Borde doble: línea externa + línea interna separadas por PAD_XS. */
    public static void drawDoubleBorder(GuiGraphics g, int x, int y, int w, int h,
                                          int outerColor, int innerColor) {
        drawBorder(g, x, y, w, h, UIScale.BORDER_MED, outerColor);
        int inset = UIScale.BORDER_MED + UIScale.PAD_XS;
        drawBorder(g, x + inset, y + inset, w - inset * 2, h - inset * 2,
            UIScale.BORDER_THIN, innerColor);
    }

    /** Esquinas ornamentales cuadradas en las 4 esquinas del rectángulo. */
    public static void drawCorners(GuiGraphics g, int x, int y, int w, int h,
                                     int size, int color) {
        int t = UIScale.BORDER_MED;
        // TL - horizontal + vertical
        g.fill(x, y, x + size, y + t, color);
        g.fill(x, y, x + t, y + size, color);
        // TR
        g.fill(x + w - size, y, x + w, y + t, color);
        g.fill(x + w - t, y, x + w, y + size, color);
        // BL
        g.fill(x, y + h - t, x + size, y + h, color);
        g.fill(x, y + h - size, x + t, y + h, color);
        // BR
        g.fill(x + w - size, y + h - t, x + w, y + h, color);
        g.fill(x + w - t, y + h - size, x + w, y + h, color);
    }

    /** Marco completo: borde doble + esquinas ornamentales. */
    public static void drawFullFrame(GuiGraphics g, int w, int h) {
        int outer = GhoulPalette.withAlpha(GhoulPalette.FRAME_OUTER, UIScale.ALPHA_STRONG);
        int inner = GhoulPalette.withAlpha(GhoulPalette.FRAME_INNER, UIScale.ALPHA_MEDIUM);
        int accent = GhoulPalette.withAlpha(GhoulPalette.FRAME_ACCENT, UIScale.ALPHA_STRONG);
        drawDoubleBorder(g, 0, 0, w, h, outer, inner);
        drawCorners(g, 0, 0, w, h, UIScale.PAD_LG, accent);
    }
}