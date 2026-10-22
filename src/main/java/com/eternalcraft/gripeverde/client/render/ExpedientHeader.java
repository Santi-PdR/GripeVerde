package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Cabecera reutilizable para las interfaces de Gripe Verde.
 *
 * v0.21.16: revertida al titulo simple (la estructura documental de
 * v0.18.0/v0.21.0 fue retirada por pedido del usuario).
 * v0.21.20: sobrecarga simple; la firma larga delega en ella.
 */
public final class ExpedientHeader {

    private ExpedientHeader() {}

    public static int draw(GuiGraphics gfx, Font font,
                           int panelX, int panelY, int panelW,
                           String expedient,
                           Component title,
                           String subtitle,
                           String reference,
                           String flavor,
                           String status,
                           int accentColor,
                           float reveal) {
        // v0.21.20: los parametros documentales se ignoran; titulo simple.
        return draw(gfx, font, panelX, panelY, panelW, title, reveal);
    }

    /** v0.21.20: cabecera simple (titulo + divisor). */
    public static int draw(GuiGraphics gfx, Font font,
                           int panelX, int panelY, int panelW,
                           Component title, float reveal) {
        reveal = Math.max(0f, Math.min(1f, reveal));
        int cx = panelX + panelW / 2;
        int topY = panelY + 12;
        int slide = (int) ((1f - reveal) * 10f);
        Component displayTitle = DiaryTypography.display(title);
        int titleW = font.width(displayTitle);
        gfx.drawString(font, displayTitle, cx - titleW / 2, topY + 12 - slide,
            DiaryChrome.fadeColor(DiaryChrome.INK_TITLE, reveal), false);
        int dividerY = topY + 30 - slide;
        DiaryChrome.drawInkDivider(gfx, panelX + 18, dividerY, panelW - 36);
        return dividerY + 10;
    }
}
