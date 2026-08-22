package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Cabecera reutilizable para las interfaces de Gripe Verde.
 *
 * v0.21.16: revertida al titulo simple (la estructura documental de
 * v0.18.0/v0.21.0 — expediente, referencia, subtitulo, flavor y estado —
 * fue retirada por pedido del usuario; los parametros se conservan en la
 * firma para no tocar los ~16 call sites).
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
        reveal = Math.max(0f, Math.min(1f, reveal));
        int cx = panelX + panelW / 2;
        int topY = panelY + 12;
        int slide = (int) ((1f - reveal) * 10f);

        // v0.21.16: solo el titulo (los carteles documentales ya no se dibujan).
        int titleW = font.width(title);
        gfx.drawString(font, title, cx - titleW / 2, topY + 12 - slide,
            DiaryChrome.fadeColor(DiaryChrome.INK_TITLE, reveal), false);

        int dividerY = topY + 30 - slide;
        DiaryChrome.drawInkDivider(gfx, panelX + 18, dividerY, panelW - 36);
        return dividerY + 10;
    }
}
