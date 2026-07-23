package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;

/**
 * Helpers cosmeticos compartidos por todas las pantallas Diary (Options,
 * Chat, Accessibility, Language, SkinCustomization, ...).
 *
 * Centraliza:
 *  - Paleta de tinta victoriana (INK_*, SHADOW_COL, OVERLAY_COL)
 *  - Divisor horizontal con fade a los costados + rombo central
 *  - Marco de esquineros dobles (externo grueso + interno fino)
 *
 * Cualquier tweak visual (color, grosor, forma del rombo) se hace UNA VEZ
 * aqui y se propaga a las 5 pantallas.
 */
public final class DiaryChrome {

    private DiaryChrome() {}

    // ── Paleta canonica ─────────────────────────────────────────────────────
    public static final int INK_TITLE     = 0xFF2A1A0A;
    public static final int INK_SUBTITLE  = 0xFF6B4226;
    public static final int INK_FLAVOR    = 0xC06B4A2E;
    public static final int INK_DIVIDER   = 0x88422810;
    public static final int SHADOW_COL    = 0x20201409;
    public static final int OVERLAY_COL   = 0x66000000;

    // ── Divisor horizontal con fade + rombo central ─────────────────────────
    // Firma centrada en (x, y, w): divisor de ancho w dibujado desde x.
    public static void drawInkDivider(GuiGraphics gfx, int x, int y, int w) {
        int fade = w / 6;
        int cx   = x + w / 2;
        gfx.fillGradient(x,            y, x + fade,     y + 1, 0x00000000, INK_DIVIDER);
        gfx.fill        (x + fade,     y, cx - 6,       y + 1, INK_DIVIDER);
        gfx.fill        (cx + 6,       y, x + w - fade, y + 1, INK_DIVIDER);
        gfx.fillGradient(x + w - fade, y, x + w,        y + 1, INK_DIVIDER, 0x00000000);
        drawDivisorLozenge(gfx, cx, y);
    }

    // Firma alternativa (x1, x2, y): divisor entre dos coordenadas absolutas.
    // Usada por pantallas cuyo layout piensa en bordes izq/der del panel.
    public static void drawInkDividerBetween(GuiGraphics gfx, int x1, int x2, int y) {
        drawInkDivider(gfx, x1, y, x2 - x1);
    }

    // Rombo tipografico victoriano centrado sobre un divisor (5x5)
    public static void drawDivisorLozenge(GuiGraphics gfx, int cx, int cy) {
        int col = 0xC0422810;
        gfx.fill(cx,     cy - 2, cx + 1, cy - 1, col);
        gfx.fill(cx - 1, cy - 1, cx + 2, cy,     col);
        gfx.fill(cx - 2, cy,     cx + 3, cy + 1, col);
        gfx.fill(cx - 1, cy + 1, cx + 2, cy + 2, col);
        gfx.fill(cx,     cy + 2, cx + 1, cy + 3, col);
    }

    // ── Esquineros victorianos dobles ───────────────────────────────────────
    // Marco externo (12px, opaco) + marco interno (5px, tenue) desplazado 3px.
    public static void drawCornerOrnaments(GuiGraphics gfx, int x, int y, int w, int h) {
        int colOut = 0xC0422810;
        int colIn  = 0x80422810;
        int sOut = 12, sIn = 5, off = 3;

        // Marco EXTERNO
        gfx.fill(x,             y,             x + sOut,    y + 1,    colOut);
        gfx.fill(x,             y,             x + 1,       y + sOut, colOut);
        gfx.fill(x + w - sOut,  y,             x + w,       y + 1,    colOut);
        gfx.fill(x + w - 1,     y,             x + w,       y + sOut, colOut);
        gfx.fill(x,             y + h - 1,     x + sOut,    y + h,    colOut);
        gfx.fill(x,             y + h - sOut,  x + 1,       y + h,    colOut);
        gfx.fill(x + w - sOut,  y + h - 1,     x + w,       y + h,    colOut);
        gfx.fill(x + w - 1,     y + h - sOut,  x + w,       y + h,    colOut);

        // Marco INTERNO
        int ix = x + off, iy = y + off, iw = w - off * 2, ih = h - off * 2;
        gfx.fill(ix,             iy,             ix + sIn,   iy + 1,   colIn);
        gfx.fill(ix,             iy,             ix + 1,     iy + sIn, colIn);
        gfx.fill(ix + iw - sIn,  iy,             ix + iw,    iy + 1,   colIn);
        gfx.fill(ix + iw - 1,    iy,             ix + iw,    iy + sIn, colIn);
        gfx.fill(ix,             iy + ih - 1,    ix + sIn,   iy + ih,  colIn);
        gfx.fill(ix,             iy + ih - sIn,  ix + 1,     iy + ih,  colIn);
        gfx.fill(ix + iw - sIn,  iy + ih - 1,    ix + iw,    iy + ih,  colIn);
        gfx.fill(ix + iw - 1,    iy + ih - sIn,  ix + iw,    iy + ih,  colIn);
    }

    // ── Barra de scroll estilizada en tinta victoriana ──────────────────────
    // Repinta encima de la barra vanilla. Requiere pasar geometria explicita
    // porque las listas vanilla exponen distinta API segun subclase.
    // Para OptionsList existe drawStyledScrollThumbForOptionsList() abajo.
    //
    // Parametros:
    //  scrollLeft: X izquierda de la barra (6px de ancho)
    //  trackY1..trackY2: rango vertical del track (top/bottom de la lista)
    //  scrollAmount: valor actual de scroll (0 a maxScroll)
    //  maxScroll: cuanto puede scrollear la lista (0 = todo visible)
    public static void drawScrollThumb(GuiGraphics gfx, int scrollLeft,
                                       int trackY1, int trackY2,
                                       double scrollAmount, int maxScroll) {
        if (maxScroll <= 0) return;

        int scrollRight = scrollLeft + 6;
        int trackH = trackY2 - trackY1;

        // Tapar barra vanilla con pergamino
        gfx.fill(scrollLeft, trackY1, scrollRight, trackY2, 0xFFEFE3C2);

        // Track: linea tenue color pergamino oscuro
        gfx.fill(scrollLeft + 1, trackY1, scrollRight - 1, trackY2, 0x40422810);

        // Thumb: posicion y altura proporcional al scroll
        double scrollFrac = scrollAmount / (double) maxScroll;
        int thumbH = Math.max(32, (int)(trackH * trackH / (double)(trackH + maxScroll)));
        int thumbY = trackY1 + (int)((trackH - thumbH) * scrollFrac);

        // Thumb tinta victoriana: relleno + relieve claro izq + relieve oscuro der
        gfx.fill(scrollLeft,     thumbY, scrollRight,     thumbY + thumbH, 0xC0422810);
        gfx.fill(scrollLeft,     thumbY, scrollLeft + 2,  thumbY + thumbH, 0xA0703020);
        gfx.fill(scrollRight - 2, thumbY, scrollRight,    thumbY + thumbH, 0x60200E05);

        // Marca central (rombo pergamino)
        int midY = thumbY + thumbH / 2;
        int midX = scrollLeft + 3;
        gfx.fill(midX,     midY - 2, midX + 1, midY - 1, 0xE0F3E9CE);
        gfx.fill(midX - 1, midY - 1, midX + 2, midY,     0xE0F3E9CE);
        gfx.fill(midX - 2, midY,     midX + 3, midY + 1, 0xE0F3E9CE);
        gfx.fill(midX - 1, midY + 1, midX + 2, midY + 2, 0xE0F3E9CE);
        gfx.fill(midX,     midY + 2, midX + 1, midY + 3, 0xE0F3E9CE);
    }
    // -- Scroll estilizado especifico para OptionsList vanilla ------------
    // OptionsList es subclase de ContainerObjectSelectionList y su scrollbar
    // vive en X = width/2 + 156 (formula fija del bytecode 1.20.1):
    //   AbstractSelectionList.getScrollbarPosition() = width/2 + 124
    //   OptionsList.getScrollbarPosition()           = super + 32
    // La barra es de 6px de ancho, alineada al centro del screen.
    // Ver ADDENDUM v0.11.5 y KI-09 en CONTEXTO.md.
    //
    // Uso: llamar DESPUES de list.render() en el render del screen.
    public static int getOptionsListScrollbarX(int screenWidth) {
        return screenWidth / 2 + 156;
    }

    public static void drawStyledScrollThumbForOptionsList(GuiGraphics gfx,
                                                            AbstractSelectionList<?> list,
                                                            int screenWidth) {
        if (list == null) return;
        int maxScroll = list.getMaxScroll();
        if (maxScroll <= 0) return;

        int scrollLeft = getOptionsListScrollbarX(screenWidth);
        int trackY1 = list.getTop();
        int trackY2 = list.getBottom();

        drawScrollThumb(gfx, scrollLeft, trackY1, trackY2, list.getScrollAmount(), maxScroll);
    }
}