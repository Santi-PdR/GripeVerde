package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import net.minecraft.client.gui.GuiGraphics;

/**
 * DiarySeparator: separador visual reutilizable estilo pergamino.
 * Linea de tinta con fade a los costados + rombo central opcional.
 *
 * Uso directo (no widget, solo render helper):
 *   DiarySeparator.draw(g, x, y, width);
 *   DiarySeparator.drawWithLozenge(g, x, y, width);
 */
public final class DiarySeparator {

    private DiarySeparator() {}

    /** Linea horizontal con fade lateral. */
    public static void draw(GuiGraphics g, int x, int y, int width) {
        int inkFull = 0x88422810;
        int inkClear = 0x00422810;
        int mid = x + width / 2;
        g.fillGradient(x, y, mid, y + 1, inkClear, inkFull);
        g.fillGradient(mid, y, x + width, y + 1, inkFull, inkClear);
    }

    /** Linea horizontal con fade lateral + rombo central de tinta. */
    public static void drawWithLozenge(GuiGraphics g, int x, int y, int width) {
        int cx = x + width / 2;
        // Dos medias lineas dejando espacio para el rombo
        int inkFull = 0x88422810;
        int inkClear = 0x00422810;
        g.fillGradient(x, y, cx - 6, y + 1, inkClear, inkFull);
        g.fillGradient(cx + 6, y, x + width, y + 1, inkFull, inkClear);
        // Rombo central via DiaryChrome
        DiaryChrome.drawDivisorLozenge(g, cx, y);
    }

    /** Linea horizontal doble (arriba y abajo con espacio, tipo cabecera de documento). */
    public static void drawDouble(GuiGraphics g, int x, int y, int width) {
        draw(g, x, y, width);
        draw(g, x, y + 3, width);
    }

    /** Linea vertical de tinta con fade (para columnas). */
    public static void drawVertical(GuiGraphics g, int x, int y, int height) {
        int inkFull = 0x88422810;
        int inkClear = 0x00422810;
        int mid = y + height / 2;
        g.fillGradient(x, y, x + 1, mid, inkClear, inkFull);
        g.fillGradient(x, mid, x + 1, y + height, inkFull, inkClear);
    }
}