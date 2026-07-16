package com.eternalcraft.gripeverde.client.theme.components;

import com.eternalcraft.gripeverde.client.theme.GhoulPalette;
import com.eternalcraft.gripeverde.client.theme.UIScale;
import net.minecraft.client.gui.GuiGraphics;

/**
 * SeparatorRenderer — líneas divisorias decorativas.
 * Responsabilidad única: separadores.
 */
public final class SeparatorRenderer {

    private SeparatorRenderer() {}

    /** Línea horizontal simple centrada en cx. */
    public static void drawLine(GuiGraphics g, int cx, int y, int width, int color) {
        int half = width / 2;
        g.fill(cx - half, y, cx + half, y + 1, color);
    }

    /** Línea horizontal con hueco central y adorno tipo rombo. */
    public static void drawOrnamental(GuiGraphics g, int cx, int y, int width,
                                        int lineColor, int accentColor) {
        int half = width / 2;
        int gap = UIScale.PAD_MD;
        // Líneas laterales
        g.fill(cx - half, y, cx - gap, y + 1, lineColor);
        g.fill(cx + gap, y, cx + half, y + 1, lineColor);
        // Rombo central (pequeño, 5x5)
        g.fill(cx - 2, y - 1, cx + 3, y + 2, accentColor);
        g.fill(cx - 1, y - 2, cx + 2, y + 3, accentColor);
    }
}