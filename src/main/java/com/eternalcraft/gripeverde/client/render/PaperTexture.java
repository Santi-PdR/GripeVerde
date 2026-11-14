package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;

/**
 * El papel de las pantallas: un velo, un filete y nada mas.
 *
 * v0.184.0 dejo de dibujar la hoja con adornos (marca de agua, sello de cera,
 * gotas, fibras, manchas) porque tapaba el fondo del menu y porque el lector no
 * queria dibujos en el papel.
 *
 * v0.185.0 remata la faena: fuera todo el codigo de adornos, que ya no se
 * llamaba desde ninguna pantalla. La clase queda en lo que se usa —el velo, su
 * filete y la paleta por tipo de papel— y el verificador prohibe los nombres
 * viejos para que nadie los reviva por costumbre.
 */
public final class PaperTexture {

    /** Opacidad del velo de las pantallas (0-255). Mas bajo deja ver mas fondo. */
    // v0.184.0: 172 de 255 (67 %). Con 196 el papel tapaba el fondo casi como una
    // hoja; con 172 la tinta oscura se lee igual y la escena se adivina debajo.
    private static final int VEIL_ALPHA = 172;

    /** Filete del borde del velo. */
    private static final int VEIL_EDGE = 0x70422810;

    public enum PaperKind {
        LEDGER, CORRESPONDENCE, ARCHIVE, TECHNICAL, MEDICAL, OFFICIAL, DEFAULT
    }

    private PaperTexture() {
    }

    /** Color base de cada stock de papel (sin alfa: el velo le pone el suyo). */
    private static int baseColorFor(PaperKind kind) {
        if (kind == null) return DiaryTheme.PAPER;
        return switch (kind) {
            case LEDGER -> 0xFFE9DDC2;         // librador, algo mas oscuro
            case CORRESPONDENCE -> 0xFFEFEBD0; // carta suelta
            case ARCHIVE -> 0xFFE2CF9E;        // archivo envejecido
            case TECHNICAL -> 0xFFE6DBC0;      // ficha tecnica
            case MEDICAL -> 0xFFECE8C8;        // parte clinico
            case OFFICIAL -> 0xFFE8D7B0;       // oficio con sello
            case DEFAULT -> DiaryTheme.PAPER;
        };
    }

    public static void drawVeil(GuiGraphics g, int x, int y, int w, int h) {
        drawVeil(g, x, y, w, h, PaperKind.DEFAULT);
    }

    /**
     * El velo de un panel: un relleno translucido y cuatro filetes de un pixel.
     * Se ve el fondo de la escena, la tinta oscura se lee encima, y cada panel
     * cuesta cinco fills en vez de cuarenta.
     */
    public static void drawVeil(GuiGraphics g, int x, int y, int w, int h, PaperKind kind) {
        if (g == null || w <= 0 || h <= 0) return;
        g.fill(x, y, x + w, y + h, DiaryTheme.withAlpha(baseColorFor(kind), VEIL_ALPHA));
        g.fill(x, y, x + w, y + 1, VEIL_EDGE);
        g.fill(x, y + h - 1, x + w, y + h, VEIL_EDGE);
        g.fill(x, y, x + 1, y + h, VEIL_EDGE);
        g.fill(x + w - 1, y, x + w, y + h, VEIL_EDGE);
    }
}
