package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Cabecera documental reutilizable para las interfaces de Gripe Verde.
 *
 * Estructura:
 *   EXPEDIENTE / referencia
 *   titulo principal
 *   subtitulo
 *   flavor / nota operativa
 *   capsula de estado opcional
 *   divisor
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

        if (expedient != null && !expedient.isEmpty()) {
            gfx.drawString(font, expedient, panelX + 18, topY - slide,
                DiaryChrome.fadeColor(DiaryTheme.SEPIA_MID, 0.92f * reveal), false);
        }
        if (reference != null && !reference.isEmpty()) {
            int rw = font.width(reference);
            gfx.drawString(font, reference, panelX + panelW - rw - 18, topY - slide,
                DiaryChrome.fadeColor(DiaryTheme.SEPIA_MID, 0.86f * reveal), false);
        }

        int titleW = font.width(title);
        gfx.drawString(font, title, cx - titleW / 2, topY + 12 - slide,
            DiaryChrome.fadeColor(DiaryChrome.INK_TITLE, reveal), false);

        if (subtitle != null && !subtitle.isEmpty()) {
            int sw = font.width(subtitle);
            gfx.drawString(font, subtitle, cx - sw / 2, topY + 25 - slide,
                DiaryChrome.fadeColor(DiaryChrome.INK_SUBTITLE, reveal), false);
        }

        if (flavor != null && !flavor.isEmpty()) {
            int fw = font.width(flavor);
            gfx.drawString(font, flavor, cx - fw / 2, topY + 39 - slide,
                DiaryChrome.fadeColor(DiaryChrome.INK_FLAVOR, reveal), false);
        }

        if (status != null && !status.isEmpty()) {
            int alphaAccent = DiaryChrome.fadeColor(accentColor, 0.20f + 0.80f * reveal);
            int border = DiaryChrome.fadeColor(accentColor, reveal);
            int ink = DiaryChrome.fadeColor(DiaryTheme.INK_BLACK, reveal);
            DiaryChrome.drawStatusPill(gfx, font, panelX + panelW - font.width(status) - 34,
                topY + 37 - slide, status, alphaAccent, border, ink);
        }

        int dividerY = topY + 54 - slide;
        DiaryChrome.drawInkDivider(gfx, panelX + 18, dividerY, panelW - 36);
        return dividerY + 12;
    }
}
