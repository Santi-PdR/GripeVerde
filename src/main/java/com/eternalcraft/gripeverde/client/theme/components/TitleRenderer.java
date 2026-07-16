package com.eternalcraft.gripeverde.client.theme.components;

import com.eternalcraft.gripeverde.client.theme.GhoulPalette;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * TitleRenderer — texto grande centrado con sombra.
 * Responsabilidad única: títulos.
 *
 * NOTA: En esta primera versión no se usa (el título vanilla se mantiene).
 * Queda listo para cuando se decida activar títulos custom por pantalla.
 */
public final class TitleRenderer {

    private TitleRenderer() {}

    /** Texto centrado horizontalmente con sombra. Escala configurable. */
    public static void drawCentered(GuiGraphics g, Font font, String text,
                                      int cx, int y, float scale, int color) {
        int tw = font.width(text);
        g.pose().pushPose();
        g.pose().translate(cx, y, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, text, -tw / 2 + 1, 1, GhoulPalette.BG_INK_DEEP, false);
        g.drawString(font, text, -tw / 2, 0, color, false);
        g.pose().popPose();
    }

    /** Subtítulo sin escalar. */
    public static void drawSubtitle(GuiGraphics g, Font font, String text,
                                      int cx, int y, int color) {
        int tw = font.width(text);
        g.drawString(font, text, cx - tw / 2, y, color, false);
    }
}