package com.eternalcraft.gripeverde.client.render;

import com.eternalcraft.gripeverde.client.lore.Discoveries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;

/**
 * v0.237.0: el parte de defuncion.
 *
 * La pantalla de muerte es la ultima del juego que seguia siendo negra y
 * vanilla. No se reemplaza la pantalla entera -eso pondria en riesgo los
 * botones de reaparecer y de volver al titulo-, se le pega encima el formulario
 * de CEDA: dos franjas de papel rasgado, arriba el encabezado del parte y abajo
 * el pie con el registro y el sello.
 *
 * El centro queda oscuro a proposito: ahi van el titulo, la causa y el puntaje
 * del juego, y tienen que seguir leyendose. Todo es estatico: ni una animacion
 * nueva dentro de la partida.
 */
public final class DeathPaper {

    private DeathPaper() {
    }

    public static void render(GuiGraphics g, Screen screen) {
        if (g == null || !(screen instanceof DeathScreen)) return;
        int w = screen.width;
        int h = screen.height;
        if (w < 180 || h < 170) return;

        boolean english = Discoveries.isEnglish();
        Font font = Minecraft.getInstance() != null ? Minecraft.getInstance().font : null;
        if (font == null) return;

        drawHead(g, font, w, english);
        if (h >= 300) drawFoot(g, font, w, h, english);
    }

    /** Encabezado: el papel entra desde el borde y el desgarro queda abajo. */
    private static void drawHead(GuiGraphics g, Font font, int w, boolean english) {
        PaperTexture.drawTornSheet(g, -2, -8, w + 4, 42, PaperTexture.PaperKind.OFFICIAL);

        String title = english ? "DEATH REPORT" : "PARTE DE DEFUNCION";
        String sub = english ? "CEDA - Bureau of Ports" : "CEDA - Sanidad de Puertos";
        int tw = DiaryChrome.widthCached(font, title);
        int sw = DiaryChrome.widthCached(font, sub);
        int cx = w / 2;
        g.drawString(font, title, cx - tw / 2, 7, DiaryChrome.INK_TITLE, false);
        DiaryChrome.drawPencilRule(g, cx - tw / 2 - 4, 17, tw + 8, w * 31 + 7);
        g.drawString(font, sub, cx - sw / 2, 20, DiaryChrome.INK_SUBTITLE, false);

        // Chapa de registro a la derecha, solo si el titulo deja hueco.
        if (w >= 360 && cx + tw / 2 < w - 84) {
            DiaryChrome.drawRegistryTag(g, -2, -2, w + 4, 36, "REG. M.");
        }
    }

    /** Pie: registro a la izquierda, sello de lacre a la derecha. */
    private static void drawFoot(GuiGraphics g, Font font, int w, int h, boolean english) {
        int top = h - 30;
        PaperTexture.drawTornSheet(g, -2, top, w + 4, 38, PaperTexture.PaperKind.OFFICIAL);

        String reg = "EC-QV/1897";
        String word = english ? "DECEASED" : "DECESO";
        int rw = DiaryChrome.widthCached(font, reg);
        int ww = DiaryChrome.widthCached(font, word);
        int lineY = top + 8;
        g.drawString(font, reg, 12, lineY, DiaryChrome.INK_FLAVOR, false);

        // Sello rojo: marco de dos filetes y la palabra, como el de las fichas.
        int sx = w - ww - 26;
        int sy = lineY - 2;
        int stamp = DiaryTheme.withAlpha(0x005C0A08, 190);
        g.fill(sx - 3, sy - 1, sx + ww + 3, sy, stamp);
        g.fill(sx - 3, sy + 9, sx + ww + 3, sy + 10, stamp);
        g.fill(sx - 3, sy - 1, sx - 2, sy + 10, stamp);
        g.fill(sx + ww + 2, sy - 1, sx + ww + 3, sy + 10, stamp);
        g.drawString(font, word, sx, sy, DiaryTheme.withAlpha(0x005C0A08, 210), false);

        // La firma del cronista, a mano y corrida, entre el registro y el sello.
        int sigW = Math.max(0, sx - 24 - (12 + rw));
        if (sigW > 40) {
            DiaryChrome.drawPencilRule(g, 12 + rw + 12, lineY + 7, Math.min(sigW, 120), w * 17 + h);
        }
    }
}
