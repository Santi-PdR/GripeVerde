package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.Minecraft;
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
    public static final int INK_TITLE     = 0xFF241608;
    public static final int INK_SUBTITLE  = 0xFF654025;
    public static final int INK_FLAVOR    = 0xB864482C;
    public static final int INK_DIVIDER   = 0x7A422810;
    public static final int SHADOW_COL    = 0x20201409;
    public static final int OVERLAY_COL   = 0x5E000000;

    // ── Divisor horizontal con fade + rombo central ─────────────────────────
    // Firma centrada en (x, y, w): divisor de ancho w dibujado desde x.
    public static void drawInkDivider(GuiGraphics gfx, int x, int y, int w) {
        int fade = w / 6;
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
        gfx.fill(cx,     cy,     cx + 1, cy + 1, 0x70F3E9CE);
    }

    // ── Esquineros victorianos + herrajes de acero ─────────────────────────
    // El gabinete del cronista se ata al acero de cuarentena: cada esquina del
    // pergamino lleva una escuadra de acero (placa en L con remache) y la tinta
    // interior queda enmarcada por ella. Misma familia visual que el resource
    // pack "gripeverde_steel" (acero cepillado #9A9EA8, remaches, verde plaga).
    public static final int STEEL_LIGHT = 0xFFB6BAC4;
    public static final int STEEL_MID   = 0xFF9A9EA8;
    public static final int STEEL_NEAR  = 0xFFA6AAB4;
    public static final int STEEL_DARK  = 0xFF7C808A;
    public static final int STEEL_DEEP  = 0xFF5E626C;
    public static final int STEEL_EDGE  = 0xFF464A52;
    public static final int PLAGUE      = 0xFF6A8A48;

    public static void drawCornerOrnaments(GuiGraphics gfx, int x, int y, int w, int h) {
        int colIn = 0x80422810;
        int sOut = 12, sIn = 5, off = 12;

        // ── Escuadras de acero (4 esquinas) ──
        // Cuerpo oscuro en L + bisel claro interior + remache 2x2.
        drawSteelBracket(gfx, x, y, 1, 1);
        drawSteelBracket(gfx, x + w - sOut, y, -1, 1);
        drawSteelBracket(gfx, x, y + h - sOut, 1, -1);
        drawSteelBracket(gfx, x + w - sOut, y + h - sOut, -1, -1);

        // ── Marco interno de tinta (5px, tenue) desplazado 12px ──
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

    /** Escuadra de acero de 12x12 en una esquina (sx/sy = direccion: ±1). */
    private static void drawSteelBracket(GuiGraphics gfx, int x, int y, int sx, int sy) {
        int len = 12;
        int b = 2;
        // Normalizar a coordenadas top-left del bracket (la esquina dada es la
        // punta exterior; el bracket crece hacia el interior del panel).
        int tlX = sx > 0 ? x : x - len + 1;
        int tlY = sy > 0 ? y : y - len + 1;
        // Cuerpo en L: 2px de grosor, 12px de brazo
        gfx.fill(tlX, tlY, tlX + len, tlY + b, STEEL_DARK);
        gfx.fill(tlX, tlY, tlX + b, tlY + len, STEEL_DARK);
        // Bisel interior 1px (luz de cepillado)
        gfx.fill(tlX + b, tlY + b, tlX + len - b + 1, tlY + b + 1, STEEL_NEAR);
        gfx.fill(tlX + b, tlY + b, tlX + b + 1, tlY + len - b + 1, STEEL_NEAR);
        // Remache 2x2 (cabeza clara, sombra abajo-derecha)
        int rx = tlX + 4;
        int ry = tlY + 4;
        gfx.fill(rx, ry, rx + 1, ry + 1, STEEL_LIGHT);
        gfx.fill(rx + 1, ry, rx + 2, ry + 1, STEEL_EDGE);
        gfx.fill(rx, ry + 1, rx + 1, ry + 2, STEEL_EDGE);
        gfx.fill(rx + 1, ry + 1, rx + 2, ry + 2, STEEL_DEEP);
    }

    // ── Chapa de registro (identidad del gabinete) ─────────────────────────
    // Pequena placa de acero con el codigo de registro del expediente,
    // colocada arriba a la derecha del panel. Texto en tinta oscura sobre
    // acero cepillado, con dos remaches y una marca de cuarentena.
    public static void drawRegistryTag(GuiGraphics gfx, int panelX, int panelY, int panelW,
                                       int panelH, String regCode) {
        if (panelW < 330) return; // paneles muy chicos: se omite para no chocar
        drawRegistryTagAt(gfx, panelX + panelW - 66, panelY + 6, regCode);
    }

    /** Variante para paneles cuyo borde derecho ya tiene contenido (ej: Skin). */
    public static void drawRegistryTagLeft(GuiGraphics gfx, int panelX, int panelY, String regCode) {
        drawRegistryTagAt(gfx, panelX + 8, panelY + 6, regCode);
    }

    /** Chapa de acero con codigo de registro, en coordenadas absolutas. */
    public static void drawRegistryTagAt(GuiGraphics gfx, int x, int y, String regCode) {
        int w = 58;
        int h = 13;

        // base de acero cepillado
        for (int i = 0; i < h; i++) {
            int v = STEEL_MID + (i % 3 == 0 ? 4 : (i % 3 == 1 ? -2 : 0));
            gfx.fill(x + 1, y + i, x + w - 1, y + i + 1, v);
        }
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

    // Barra de scroll victoriana profesional: pergamino tapado + thumb bronce
    public static void drawScrollThumb(GuiGraphics gfx, int scrollLeft,
                                       int trackY1, int trackY2,
                                       double scrollAmount, int maxScroll) {
        if (maxScroll <= 0) return;

        int scrollRight = scrollLeft + 6;
        int trackH = trackY2 - trackY1;
        gfx.fill(scrollLeft, trackY1, scrollRight, trackY2, 0xFFF0E5C8);
        gfx.fill(scrollLeft + 2, trackY1, scrollRight - 2, trackY2, 0x30422810);

        double scrollFrac = scrollAmount / (double) maxScroll;
        int thumbH = Math.max(28, Math.min(trackH - 8, (int)(trackH * trackH / (double)(trackH + maxScroll * 1.2))));
        int thumbY = trackY1 + (int)((trackH - thumbH) * scrollFrac);

        // Thumb: steel + parchment highlight + bronze rivets
        gfx.fill(scrollLeft, thumbY, scrollRight, thumbY + thumbH, 0xBB6B4A2E);
        gfx.fill(scrollLeft, thumbY, scrollLeft + 2, thumbY + thumbH, 0xD0A67A5A);
        gfx.fill(scrollRight - 2, thumbY, scrollRight, thumbY + thumbH, 0x702E1E12);
        gfx.fill(scrollLeft, thumbY, scrollRight, thumbY + 1, 0xD08E6A35);
        gfx.fill(scrollLeft, thumbY + thumbH - 1, scrollRight, thumbY + thumbH, 0x70200E05);

        int midY = thumbY + thumbH / 2;
        int midX = scrollLeft + 3;
        // Lozenge central parchment
        gfx.fill(midX, midY - 2, midX + 1, midY - 1, 0xE8F3E9CE);
        gfx.fill(midX - 1, midY - 1, midX + 2, midY, 0xE8F3E9CE);
        gfx.fill(midX - 2, midY, midX + 3, midY + 1, 0xE8F3E9CE);
        gfx.fill(midX - 1, midY + 1, midX + 2, midY + 2, 0xE8F3E9CE);
        gfx.fill(midX, midY + 2, midX + 1, midY + 3, 0xE8F3E9CE);
        // Rivet top/bottom
        gfx.fill(midX, thumbY + 3, midX + 1, thumbY + 4, DiaryTheme.withAlpha(0x00FFFFFF, 48));
        gfx.fill(midX, thumbY + thumbH - 4, midX + 1, thumbY + thumbH - 3, DiaryTheme.withAlpha(0x00000000, 60));
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

    // ---------------------------------------------------------------
    // Fondo cuero oscuro para pantallas de trabajo (Grupo B v0.11.7):
    // KeyBinds (Atajos), PackSelection (Paquetes de recursos) y
    // Embeddium (via mixin, v0.11.8). Reemplaza el dirt vanilla por
    // una tapa de libro sepia con vinieta arriba y abajo.
    //
    // Sin escena viva detras: en pantallas densas la escena distrae.
    // Barato: 1 fill base + 2 gradientes de vinieta.
    // ---------------------------------------------------------------
    public static final int LEATHER_BASE = 0xFF1F1408;

    public static void drawDarkLeatherBackground(GuiGraphics gfx, int screenW, int screenH) {
        // Base uniforme: cuero sepia muy oscuro
        gfx.fill(0, 0, screenW, screenH, LEATHER_BASE);

        // Vinieta superior e inferior (fade a negro semitransparente)
        int fade = Math.max(60, Math.min(screenW, screenH) / 4);
        gfx.fillGradient(0, 0, screenW, fade, 0x60000000, 0x00000000);
        gfx.fillGradient(0, screenH - fade, screenW, screenH, 0x00000000, 0x60000000);
    }

    // Sombra proyectada profesional: 3 capas soft + inner bevel
    public static void drawPanelShadow(GuiGraphics gfx, int x, int y, int w, int h) {
        int offset = 7;
        // Outer soft penumbra
        gfx.fill(x + offset - 4, y + offset - 4, x + w + offset + 4, y + h + offset + 4, 0x18100600);
        gfx.fill(x + offset - 2, y + offset - 2, x + w + offset + 2, y + h + offset + 2, 0x3A100600);
        gfx.fill(x + offset, y + offset, x + w + offset, y + h + offset, 0x6E100600);
        // Inner contact shadow darker
        gfx.fill(x + offset + 1, y + offset + 1, x + w + offset - 1, y + h + offset - 1, 0x8C080300);
        // Subtle highlight top-left of shadow to simulate paper lift
        gfx.fill(x + 2, y + 2, x + w - 2, y + 3, DiaryTheme.withAlpha(0x00FFFFFF, 10));
    }

    // ---------------------------------------------------------------
    // Scrollbar estilizada alineada al borde derecho del panel.
    // Tapa la barra vanilla (que vive en width/2+156) con pergamino
    // antes de dibujar la nuestra en la posicion del panel.
    // panelRightEdge = panelX + panelW.
    // ---------------------------------------------------------------
    public static void drawStyledScrollThumbInPanel(GuiGraphics gfx,
                                                     AbstractSelectionList<?> list,
                                                     int panelRightEdge) {
        if (list == null) return;

        int trackY1 = list.getTop();
        int trackY2 = list.getBottom();

        // Tapar la scrollbar vanilla solo si cae dentro del panel visible.
        int vanillaBarX = (net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2) + 156;
        int scrollLeft = panelRightEdge - 18;
        if (vanillaBarX < panelRightEdge - 4) {
            gfx.fill(vanillaBarX - 2, trackY1, vanillaBarX + 8, trackY2, 0xFFE8DCB4);
        }

        int maxScroll = list.getMaxScroll();
        if (maxScroll <= 0) return;

        drawScrollThumb(gfx, scrollLeft, trackY1, trackY2, list.getScrollAmount(), maxScroll);
    }


    /** Cursor de pluma pixelart al lado del mouse.
     *  Llamar al FINAL del render() de una Diary screen cuando el mouse
     *  este sobre el panel de pergamino. */
    public static void drawQuillCursor(net.minecraft.client.gui.GuiGraphics g, int mouseX, int mouseY) {
        // Pluma diagonal de arriba-derecha a abajo-izquierda, ~10x10
        // Solo dibujamos si el mouse esta en pantalla
        int inkDark = 0xFF1A0F08;
        int inkLight = 0xFF3D2E1A;
        int feather = 0xFF6B4226;

        // Vastago (linea diagonal 8px)
        int sx = mouseX + 6;
        int sy = mouseY + 2;
        for (int i = 0; i < 8; i++) {
            g.fill(sx + i, sy + i, sx + i + 1, sy + i + 1, inkDark);
        }
        // Punta (abajo izquierda)
        g.fill(sx - 1, sy + 8, sx, sy + 9, inkDark);
        g.fill(sx, sy + 9, sx + 1, sy + 10, inkDark);
        // Plumas (arriba derecha, 3 pequenas)
        g.fill(sx + 6, sy - 1, sx + 8, sy + 1, feather);
        g.fill(sx + 8, sy - 2, sx + 10, sy, feather);
        g.fill(sx + 7, sy + 1, sx + 9, sy + 2, inkLight);
    }
}
