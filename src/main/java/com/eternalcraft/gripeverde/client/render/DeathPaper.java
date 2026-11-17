package com.eternalcraft.gripeverde.client.render;

import com.eternalcraft.gripeverde.client.lore.Discoveries;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;

/**
 * v0.240.0: el telegrama de defuncion.
 *
 * Antes la pantalla de muerte se forraba con dos franjas de papel de borde a
 * borde: quedaba aparatoso y tapaba el mundo. Ahora es un solo papel chico,
 * apoyado en la esquina superior izquierda, con el aviso de condolencias tal
 * como lo recibian los familiares: membrete, dos renglones y el sello. El
 * centro de la pantalla queda libre para el titulo y el puntaje del juego.
 * Nada mas se toca: ni velo, ni oscurecido, ni animacion.
 *
 * Los botones del juego se visten de papel sin duplicar su logica: el original
 * se oculta y en su lugar se pone un boton de la casa que dispara su misma
 * accion (reaparecer, menu principal, espectador en modo extremo).
 */
public final class DeathPaper {

    // v0.248.0: el aviso va en la esquina, chico, para no comerse el titulo
    // del juego ni el puntaje. Antes se centraba arriba y tapaba "Estas muerto".
    private static final int PAPER_W_MAX = 296;
    private static final int PAPER_H = 64;
    private static final int PAPER_MARGIN = 8;

    private DeathPaper() {
    }

    /** Enganche desde ScreenEvent.Init.Post: solo para la pantalla de muerte. */
    public static void onInit(Screen screen) {
        if (!(screen instanceof DeathScreen)) return;
        try {
            skinButtons(screen);
        } catch (Throwable ignored) {
        }
    }

    private static void skinButtons(Screen screen) {
        PaperButtons.dressAll(screen);
    }

    public static void render(GuiGraphics g, Screen screen) {
        if (g == null || !(screen instanceof DeathScreen)) return;
        int w = screen.width;
        int h = screen.height;
        if (w < 220 || h < 200) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc != null ? mc.font : null;
        if (font == null) return;
        boolean english = Discoveries.isEnglish();

        int pw = Math.min(PAPER_W_MAX, w - PAPER_MARGIN * 2 - 8);
        int px = PAPER_MARGIN;
        int py = PAPER_MARGIN;
        PaperTexture.drawTornSheet(g, px, py, pw, PAPER_H, PaperTexture.PaperKind.OFFICIAL);

        String head = english ? "CEDA - BUREAU OF PORTS" : "CEDA - SANIDAD DE PUERTOS";
        String line1 = english
            ? "Deepest regrets: the bearer is deceased."
            : "Lamento informar el deceso del portador.";
        String line2 = english
            ? "The port notes it in its ledger."
            : "La aduana lo anota en su libro.";

        int hy = py + 9;
        g.drawString(font, DiaryChrome.fitCached(font, head, pw - 16), px + 8, hy,
            DiaryChrome.INK_TITLE, false);
        DiaryChrome.drawPencilRule(g, px + 8, hy + 10, pw - 16, px * 31 + py * 17 + 5);

        int l1 = py + 24;
        g.drawString(font, DiaryChrome.fitCached(font, line1, pw - 16), px + 8, l1,
            DiaryChrome.INK_SUBTITLE, false);
        g.drawString(font, DiaryChrome.fitCached(font, line2, pw - 16), px + 8, l1 + 11,
            DiaryChrome.INK_SUBTITLE, false);

        // Pie del papel: registro a la izquierda y sello a la derecha.
        int footY = py + PAPER_H - 16;
        DiaryChrome.drawPencilRule(g, px + 8, footY - 4, pw - 16, px * 17 + py * 13 + 3);
        String reg = "EC-QV/1897";
        g.drawString(font, reg, px + 8, footY, DiaryChrome.INK_FLAVOR, false);

        String word = english ? "DECEASED" : "DECESO";
        int ww = DiaryChrome.widthCached(font, word);
        int sx = px + pw - 8 - ww;
        int stamp = DiaryTheme.withAlpha(0x005C0A08, 190);
        int stampInk = DiaryTheme.withAlpha(0x005C0A08, 215);
        g.fill(sx - 3, footY - 2, sx + ww + 3, footY - 1, stamp);
        g.fill(sx - 3, footY + 9, sx + ww + 3, footY + 10, stamp);
        g.fill(sx - 3, footY - 2, sx - 2, footY + 10, stamp);
        g.fill(sx + ww + 2, footY - 2, sx + ww + 3, footY + 10, stamp);
        g.drawString(font, word, sx, footY, stampInk, false);
    }
}
