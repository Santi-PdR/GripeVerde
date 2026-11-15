package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.network.chat.Component;
import com.eternalcraft.gripeverde.client.render.DiaryTypography;


/**
 * Helpers cosmeticos compartidos por todas las pantallas Diary.
 *
 * Centraliza:
 *  - paleta canonica de tinta / sombra / metales
 *  - sistema de paneles, tarjetas, separadores y capsulas de estado
 *  - scrollbars, sombras y pequenas piezas de interfaz reutilizables
 */
public final class DiaryChrome {

    private DiaryChrome() {}

    // ── Paleta canonica ─────────────────────────────────────────────────────
    public static final int INK_TITLE     = 0xFF241608;
    public static final int INK_SUBTITLE  = 0xFF654025;
    public static final int INK_FLAVOR    = 0xF0604028;
    public static final int INK_DIVIDER   = 0x7A422810;
    public static final int OVERLAY_COL   = 0x5E000000;

    public static final int STEEL_MID   = 0xFF9A9EA8;
    public static final int STEEL_NEAR  = 0xFFA6AAB4;
    public static final int STEEL_DEEP  = 0xFF5E626C;
    public static final int STEEL_EDGE  = 0xFF464A52;
    public static final int PLAGUE      = 0xFF6A8A48;


    // ── Interpolacion / reveal ──────────────────────────────────────────────
    public static float reveal(long openedAtMs, long durationMs) {
        if (openedAtMs <= 0L || durationMs <= 0L) return 1f;
        long elapsed = Math.max(0L, System.currentTimeMillis() - openedAtMs);
        if (elapsed >= durationMs) return 1f;
        float t = elapsed / (float) durationMs;
        return 1f - (float) Math.pow(1f - t, 3);
    }

    public static int fadeColor(int color, float factor) {
        factor = Math.max(0f, Math.min(1f, factor));
        int a = (int) (((color >>> 24) & 0xFF) * factor);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    public static int lerpColor(int colorA, int colorB, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = lerpChannel((colorA >>> 24) & 0xFF, (colorB >>> 24) & 0xFF, t);
        int r = lerpChannel((colorA >>> 16) & 0xFF, (colorB >>> 16) & 0xFF, t);
        int g = lerpChannel((colorA >>> 8) & 0xFF, (colorB >>> 8) & 0xFF, t);
        int b = lerpChannel(colorA & 0xFF, colorB & 0xFF, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lerpChannel(int a, int b, float t) {
        return (int) (a + (b - a) * t);
    }

    // ── Divisor horizontal con fade + rombo central ─────────────────────────
    public static void drawRegistryTag(GuiGraphics gfx, int panelX, int panelY, int panelW,
                                       int panelH, String regCode) {
        if (panelW < 330) return; // paneles muy chicos: se omite para no chocar
        drawRegistryTagAt(gfx, panelX + panelW - 66, panelY + 6, regCode);
    }


    public static void drawRegistryTagLeft(GuiGraphics gfx, int panelX, int panelY, String regCode) {
        drawRegistryTagAt(gfx, panelX + 8, panelY + 6, regCode);
    }


    public static void drawRegistryTagAt(GuiGraphics gfx, int x, int y, String regCode) {
        int w = 58;
        int h = 13;

        // Base de acero cepillado.
        // v0.189.0: eran trece rellenos de una fila cada uno para simular el
        // cepillado; con la base y dos lineas de brillo se ve igual.
        gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, STEEL_MID);
        gfx.fill(x + 1, y + 2, x + w - 1, y + 3, STEEL_MID + 4);
        gfx.fill(x + 1, y + h - 4, x + w - 1, y + h - 3, STEEL_MID - 2);
        // borde
        gfx.fill(x, y, x + w, y + 1, STEEL_EDGE);
        gfx.fill(x, y + h - 1, x + w, y + h, STEEL_EDGE);
        gfx.fill(x, y, x + 1, y + h, STEEL_EDGE);
        gfx.fill(x + w - 1, y, x + w, y + h, STEEL_EDGE);
        // biseles interiores
        gfx.fill(x + 1, y + 1, x + w - 1, y + 2, STEEL_NEAR);
        gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, STEEL_DEEP);
        // marca de cuarentena (verde plaga) en el borde izquierdo
        gfx.fill(x + 1, y + 2, x + 2, y + h - 2, PLAGUE);
        // remaches
        gfx.fill(x + 4, y + 2, x + 6, y + 4, STEEL_DEEP);
        gfx.fill(x + w - 6, y + 2, x + w - 4, y + 4, STEEL_DEEP);
        // texto del registro
        int tw = gfx.drawString(Minecraft.getInstance().font, regCode, x + 2, y + 3,
                DiaryTheme.withAlpha(0x002A2620, 235), false);
        gfx.fill(x + 2 + tw + 2, y + 4, x + 2 + tw + 4, y + 5, STEEL_EDGE);
    }

    /**
     * v0.195.0: traducciones de pantalla memorizadas.
     *
     * Las cabeceras de las pantallas resolvian su `Component.translatable(...)
     * .getString()` en cada cuadro, y eso arma un componente y recorre la tabla
     * de la lengua sesenta veces por segundo para el mismo texto. Aca se guarda
     * el resultado y se tira entero cuando cambia la lengua elegida, asi que
     * nunca queda una traduccion vieja.
     */
    private static String cachedLangCode;
    private static final java.util.HashMap<String, String> TRANSLATION_CACHE = new java.util.HashMap<>();

    public static String tr(String key) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        String code = "";
        try {
            if (mc != null && mc.getLanguageManager() != null) {
                code = mc.getLanguageManager().getSelected();
            }
        } catch (Throwable ignored) {}
        if (!code.equals(cachedLangCode)) {
            cachedLangCode = code;
            TRANSLATION_CACHE.clear();
            FIT_CACHE.clear();
            WIDTH_CACHE.clear();
        }
        String value = TRANSLATION_CACHE.get(key);
        if (value == null) {
            value = Component.translatable(key).getString();
            if (TRANSLATION_CACHE.size() > 512) TRANSLATION_CACHE.clear();
            TRANSLATION_CACHE.put(key, value);
        }
        return value;
    }

    /** Igual que {@link #tr(String)} pero recortado al ancho pedido. */
    public static String trFit(String key, net.minecraft.client.gui.Font font, int maxWidth) {
        return fitText(font, tr(key), maxWidth);
    }

    public static void drawInkDivider(GuiGraphics gfx, int x, int y, int w) {
        int fade = Math.max(8, w / 6);
        int cx   = x + w / 2;
        gfx.fillGradient(x,            y, x + fade,     y + 1, 0x00000000, INK_DIVIDER);
        gfx.fill        (x + fade,     y, cx - 6,       y + 1, INK_DIVIDER);
        gfx.fill        (cx + 6,       y, x + w - fade, y + 1, INK_DIVIDER);
        gfx.fillGradient(x + w - fade, y, x + w,        y + 1, INK_DIVIDER, 0x00000000);
        int cap = 0x60422810;
        gfx.fill(x + fade - 2, y - 1, x + fade, y + 2, cap);
        gfx.fill(x + w - fade, y - 1, x + w - fade + 2, y + 2, cap);
        gfx.fill(cx - w / 4, y - 1, cx - w / 4 + 1, y + 2, 0x48422810);
        gfx.fill(cx + w / 4, y - 1, cx + w / 4 + 1, y + 2, 0x48422810);
        drawDivisorLozenge(gfx, cx, y);
    }

    public static void drawInkDividerBetween(GuiGraphics gfx, int x1, int x2, int y) {
        drawInkDivider(gfx, x1, y, x2 - x1);
    }

    public static void drawDivisorLozenge(GuiGraphics gfx, int cx, int cy) {
        int col = 0xC0422810;
        gfx.fill(cx,     cy - 2, cx + 1, cy - 1, col);
        gfx.fill(cx - 1, cy - 1, cx + 2, cy,     col);
        gfx.fill(cx - 2, cy,     cx + 3, cy + 1, col);
        gfx.fill(cx - 1, cy + 1, cx + 2, cy + 2, col);
        gfx.fill(cx,     cy + 2, cx + 1, cy + 3, col);
        gfx.fill(cx,     cy,     cx + 1, cy + 1, 0x70F3E9CE);
    }

    /**
     * v0.183.0: true si el puntero esta sobre un widget visible de la pantalla.
     *
     * Los adornos con clic (marcas del encuadernador, folio escondido, titulos de
     * seccion) se consultaban antes que los botones y se quedaban con el gesto.
     * Ahora cada pantalla pregunta primero por sus widgets con este helper, que
     * usa children() y no el campo protegido.
     */
    public static boolean overAnyWidget(net.minecraft.client.gui.screens.Screen screen,
                                        double mouseX, double mouseY) {
        if (screen == null) return false;
        for (var child : screen.children()) {
            if (child instanceof net.minecraft.client.gui.components.AbstractWidget widget
                    && widget.visible && widget.isActive()) {
                if (mouseX >= widget.getX() && mouseX <= widget.getX() + widget.getWidth()
                        && mouseY >= widget.getY() && mouseY <= widget.getY() + widget.getHeight()) {
                    return true;
                }
            }
        }
        return false;
    }

    // ── Esquineros victorianos dobles ───────────────────────────────────────
    public static void drawCornerOrnaments(GuiGraphics gfx, int x, int y, int w, int h) {
        int colOut = 0xC0422810;
        int colIn  = 0x80422810;
        int sOut = 12, sIn = 5, off = 3;

        gfx.fill(x,             y,             x + sOut,    y + 1,    colOut);
        gfx.fill(x,             y,             x + 1,       y + sOut, colOut);
        gfx.fill(x + w - sOut,  y,             x + w,       y + 1,    colOut);
        gfx.fill(x + w - 1,     y,             x + w,       y + sOut, colOut);
        gfx.fill(x,             y + h - 1,     x + sOut,    y + h,    colOut);
        gfx.fill(x,             y + h - sOut,  x + 1,       y + h,    colOut);
        gfx.fill(x + w - sOut,  y + h - 1,     x + w,       y + h,    colOut);
        gfx.fill(x + w - 1,     y + h - sOut,  x + w,       y + h,    colOut);

        int ix = x + off, iy = y + off, iw = w - off * 2, ih = h - off * 2;
        gfx.fill(ix,             iy,             ix + sIn,   iy + 1,   colIn);
        gfx.fill(ix,             iy,             ix + 1,     iy + sIn, colIn);
        gfx.fill(ix + iw - sIn,  iy,             ix + iw,    iy + 1,   colIn);
        gfx.fill(ix + iw - 1,    iy,             ix + iw,    iy + sIn, colIn);
        gfx.fill(ix,             iy + ih - 1,    ix + sIn,   iy + ih,  colIn);
        gfx.fill(ix,             iy + ih - sIn,  ix + 1,     iy + ih,  colIn);
        gfx.fill(ix + iw - sIn,  iy + ih - 1,    ix + iw,    iy + ih,  colIn);
        gfx.fill(ix + iw - 1,    iy + ih - sIn,  ix + iw,    iy + ih,  colIn);

        int dot = 0x70422810;
        gfx.fill(x + 5, y + 5, x + 7, y + 7, dot);
        gfx.fill(x + w - 7, y + 5, x + w - 5, y + 7, dot);
        gfx.fill(x + 5, y + h - 7, x + 7, y + h - 5, dot);
        gfx.fill(x + w - 7, y + h - 7, x + w - 5, y + h - 5, dot);
    }

    // ── Capsulas y tarjetas de sistema ──────────────────────────────────────



    public static void drawPanelScaffold(GuiGraphics gfx, int x, int y, int w, int h, int accent) {        drawPanelShadow(gfx, x, y, w, h);
        gfx.fill(x + 6, y + 6, x + w - 6, y + h - 6, DiaryTheme.withAlpha(0x00FFFFFF, 4));
        gfx.fill(x + 10, y + 10, x + w - 10, y + h - 10, DiaryTheme.withAlpha(accent, 7));
        gfx.fill(x + 14, y + 14, x + w - 14, y + 15, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 24));
        gfx.fill(x + 14, y + h - 15, x + w - 14, y + h - 14, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 24));
        // guias editoriales sutiles, no marco cerrado
        gfx.fill(x + 18, y + 18, x + 32, y + 19, DiaryTheme.withAlpha(accent, 46));
        gfx.fill(x + w - 32, y + 18, x + w - 18, y + 19, DiaryTheme.withAlpha(accent, 46));
        gfx.fill(x + 18, y + h - 19, x + 32, y + h - 18, DiaryTheme.withAlpha(accent, 30));
        gfx.fill(x + w - 32, y + h - 19, x + w - 18, y + h - 18, DiaryTheme.withAlpha(accent, 30));
    }

    /** v0.21.7: base comun del panel (scaffold + pergamino + esquineros). */
    public static void renderPanelBase(GuiGraphics gfx, int x, int y, int w, int h, int accent,
                                       PaperTexture.PaperKind kind) {
        drawPanelScaffold(gfx, x, y, w, h, accent);
        PaperTexture.drawSheet(gfx, x, y, w, h, kind);
        drawCornerOrnaments(gfx, x, y, w, h);
    }

    /** v0.21.9: texto centrado con la font del gabinete (consolidacion). */
    public static void drawCenteredString(GuiGraphics gfx, Font font, String s, int cx, int y, int color) {
        if (s == null) return;
        int w = DiaryTypography.width(font, s);
        DiaryTypography.draw(gfx, font, s, cx - w / 2, y, color);
    }

    /**
     * Keeps localized single-line labels inside their panel while preserving
     * the start of the text and marking any clipped tail.
     */
    public static String fitText(Font font, String text, int maxWidth) {
        if (font == null || text == null || text.isEmpty()) return "";
        int limit = Math.max(0, maxWidth);
        if (font.width(text) <= limit) return text;
        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        if (ellipsisWidth > limit) return font.plainSubstrByWidth(text, limit);
        return font.plainSubstrByWidth(text, limit - ellipsisWidth) + ellipsis;
    }

    // v0.203.0: casi todas las pantallas ajustan las mismas tres lineas de
    // cabecera -titulo, subtitulo y sentencia- en cada cuadro, y luego las
    // vuelven a medir para centrarlas. Ambas cosas se guardan aqui: por el
    // texto de entrada (que incluye el idioma ya resuelto), el ancho disponible
    // y la fuente. El cache se vacia cuando cambia el idioma.
    private static final java.util.HashMap<String, String> FIT_CACHE = new java.util.HashMap<>();
    private static final java.util.HashMap<String, Integer> WIDTH_CACHE = new java.util.HashMap<>();

    private static String textKey(Font font, int width, String text) {
        return System.identityHashCode(font) + "/" + width + "/" + text;
    }

    /** Igual que {@link #fitText} pero memorizado (seguro por cuadro). */
    public static String fitCached(Font font, String text, int maxWidth) {
        if (font == null || text == null || text.isEmpty()) return "";
        int limit = Math.max(0, maxWidth);
        String key = textKey(font, limit, text);
        String cached = FIT_CACHE.get(key);
        if (cached != null) return cached;
        String fitted = fitText(font, text, limit);
        if (FIT_CACHE.size() >= 256) FIT_CACHE.clear();
        FIT_CACHE.put(key, fitted);
        return fitted;
    }

    /** Ancho de un texto ya resuelto, medido una sola vez por fuente. */
    public static int widthCached(Font font, String text) {
        if (font == null || text == null || text.isEmpty()) return 0;
        String key = textKey(font, 0, text);
        Integer cached = WIDTH_CACHE.get(key);
        if (cached != null) return cached;
        int width = font.width(text);
        if (WIDTH_CACHE.size() >= 256) WIDTH_CACHE.clear();
        WIDTH_CACHE.put(key, width);
        return width;
    }

    /** Traduccion memorizada y ajustada de una clave. */
    public static String trFitCached(String key, Font font, int maxWidth) {
        return fitCached(font, tr(key), maxWidth);
    }


    // ── Scrollbars ───────────────────────────────────────────────────────────
    public static void drawScrollThumb(GuiGraphics gfx, int scrollLeft,
                                       int trackY1, int trackY2,
                                       double scrollAmount, int maxScroll) {
        if (maxScroll <= 0) return;

        int scrollRight = scrollLeft + 6;
        int trackH = trackY2 - trackY1;
        gfx.fill(scrollLeft, trackY1, scrollRight, trackY2, 0xFFF0E5C8);
        gfx.fill(scrollLeft + 2, trackY1, scrollRight - 2, trackY2, 0x30422810);
        gfx.fill(scrollLeft - 1, trackY1 - 1, scrollRight + 1, trackY1, DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 90));
        gfx.fill(scrollLeft - 1, trackY2, scrollRight + 1, trackY2 + 1, DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 90));

        double scrollFrac = scrollAmount / (double) maxScroll;
        int thumbH = Math.max(28, Math.min(trackH - 8, (int) (trackH * trackH / (double) (trackH + maxScroll * 1.2))));
        int thumbY = trackY1 + (int) ((trackH - thumbH) * scrollFrac);

        gfx.fill(scrollLeft, thumbY, scrollRight, thumbY + thumbH, 0xBB6B4A2E);
        gfx.fill(scrollLeft, thumbY, scrollLeft + 2, thumbY + thumbH, 0xD0A67A5A);
        gfx.fill(scrollRight - 2, thumbY, scrollRight, thumbY + thumbH, 0x702E1E12);
        gfx.fill(scrollLeft, thumbY, scrollRight, thumbY + 1, 0xD08E6A35);
        gfx.fill(scrollLeft, thumbY + thumbH - 1, scrollRight, thumbY + thumbH, 0x70200E05);
        gfx.fill(scrollLeft - 1, thumbY + 1, scrollLeft, thumbY + thumbH - 1, DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 70));
        gfx.fill(scrollRight, thumbY + 1, scrollRight + 1, thumbY + thumbH - 1, DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 40));

        int midY = thumbY + thumbH / 2;
        int midX = scrollLeft + 3;
        drawDivisorLozenge(gfx, midX, midY);
        gfx.fill(midX, thumbY + 3, midX + 1, thumbY + 4, DiaryTheme.withAlpha(0x00FFFFFF, 48));
        gfx.fill(midX, thumbY + thumbH - 4, midX + 1, thumbY + thumbH - 3, DiaryTheme.withAlpha(0x00000000, 60));
        for (int i = thumbY + 7; i < thumbY + thumbH - 6; i += 8) {
            gfx.fill(scrollLeft + 1, i, scrollRight - 1, i + 1, DiaryTheme.withAlpha(DiaryTheme.PAPER, 48));
        }
    }

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

    // ── Sombra proyectada profesional ───────────────────────────────────────
    public static void drawPanelShadow(GuiGraphics gfx, int x, int y, int w, int h) {
        int offset = 7;
        gfx.fill(x + offset - 4, y + offset - 4, x + w + offset + 4, y + h + offset + 4, 0x18100600);
        gfx.fill(x + offset - 2, y + offset - 2, x + w + offset + 2, y + h + offset + 2, 0x3A100600);
        gfx.fill(x + offset, y + offset, x + w + offset, y + h + offset, 0x6E100600);
        gfx.fill(x + offset + 1, y + offset + 1, x + w + offset - 1, y + h + offset - 1, 0x8C080300);
        gfx.fill(x + 2, y + 2, x + w - 2, y + 3, DiaryTheme.withAlpha(0x00FFFFFF, 10));
    }

    public static void drawStyledScrollThumbInPanel(GuiGraphics gfx,
                                                    AbstractSelectionList<?> list,
                                                    int panelRightEdge) {
        if (list == null) return;

        int trackY1 = list.getTop();
        int trackY2 = list.getBottom();
        int vanillaBarX = (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2) + 156;
        int scrollLeft = panelRightEdge - 18;
        if (vanillaBarX < panelRightEdge - 4) {
            gfx.fill(vanillaBarX - 2, trackY1, vanillaBarX + 8, trackY2, 0xFFE8DCB4);
        }

        int maxScroll = list.getMaxScroll();
        if (maxScroll <= 0) return;
        drawScrollThumb(gfx, scrollLeft, trackY1, trackY2, list.getScrollAmount(), maxScroll);
    }

}
