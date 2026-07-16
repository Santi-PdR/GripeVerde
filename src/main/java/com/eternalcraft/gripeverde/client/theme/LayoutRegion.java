package com.eternalcraft.gripeverde.client.theme;

/**
 * LayoutRegion — region rectangular inmutable para calcular layout de pantalla.
 *
 * Representa una zona del espacio de pantalla. No renderiza nada.
 * Se construye una vez por frame o en resize(), nunca se modifica.
 *
 * Ejemplo de uso en un ScreenTheme:
 *
 *   LayoutRegion screen = LayoutRegion.screen(w, h);
 *   LayoutRegion panel  = screen.inset(UIScale.PAD_XL);
 *   LayoutRegion title  = panel.top(UIScale.ROW_HERO);
 *   LayoutRegion body   = panel.inset(0, UIScale.ROW_HERO + UIScale.PAD_MD);
 *
 *   int[] btnPos = body.row(0, UIScale.BTN_H_LG, UIScale.PAD_SM)
 *                      .anchor(Anchor.MID_CENTER, UIScale.BTN_W_LG, UIScale.BTN_H_LG);
 *
 * Dos paletas disponibles — elegir segun contexto:
 *   DiaryTheme  (client.render) — pantallas propias del mod (papel, cuaderno)
 *   GhoulPalette (client.theme) — overlays sobre pantallas vanilla (oscuro)
 */
public final class LayoutRegion {

    /** Coordenada X del borde izquierdo. */
    public final int x;
    /** Coordenada Y del borde superior. */
    public final int y;
    /** Ancho total de la region. */
    public final int w;
    /** Alto total de la region. */
    public final int h;

    // ----------------------------------------------------------------
    // Constructor base
    // ----------------------------------------------------------------

    public LayoutRegion(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = Math.max(0, w);
        this.h = Math.max(0, h);
    }

    // ----------------------------------------------------------------
    // Fabricas estaticas
    // ----------------------------------------------------------------

    /** Region que cubre toda la pantalla. */
    public static LayoutRegion screen(int screenW, int screenH) {
        return new LayoutRegion(0, 0, screenW, screenH);
    }

    /**
     * Region con margen uniforme desde los bordes de pantalla.
     * mx = margen horizontal (izquierda y derecha), my = margen vertical (arriba y abajo).
     */
    public static LayoutRegion withMargin(int screenW, int screenH, int mx, int my) {
        return new LayoutRegion(mx, my, screenW - mx * 2, screenH - my * 2);
    }

    /**
     * Region centrada de tamano fijo, calculada sobre la pantalla completa.
     */
    public static LayoutRegion centered(int screenW, int screenH, int regionW, int regionH) {
        int rx = (screenW - regionW) / 2;
        int ry = (screenH - regionH) / 2;
        return new LayoutRegion(rx, ry, regionW, regionH);
    }

    // ----------------------------------------------------------------
    // Sub-regiones
    // ----------------------------------------------------------------

    /** Sub-region con el mismo padding en todos los lados. */
    public LayoutRegion inset(int pad) {
        return new LayoutRegion(x + pad, y + pad, w - pad * 2, h - pad * 2);
    }

    /** Sub-region con padding horizontal y vertical independientes. */
    public LayoutRegion inset(int px, int py) {
        return new LayoutRegion(x + px, y + py, w - px * 2, h - py * 2);
    }

    /** Banda superior de altura fija. */
    public LayoutRegion top(int height) {
        int clamped = Math.min(height, h);
        return new LayoutRegion(x, y, w, clamped);
    }

    /** Banda inferior de altura fija. */
    public LayoutRegion bottom(int height) {
        int clamped = Math.min(height, h);
        return new LayoutRegion(x, y + h - clamped, w, clamped);
    }

    /** Banda izquierda de ancho fijo. */
    public LayoutRegion left(int width) {
        int clamped = Math.min(width, w);
        return new LayoutRegion(x, y, clamped, h);
    }

    /** Banda derecha de ancho fijo. */
    public LayoutRegion right(int width) {
        int clamped = Math.min(width, w);
        return new LayoutRegion(x + w - clamped, y, clamped, h);
    }

    /**
     * Region inferior excluyendo una banda superior de altura topHeight.
     * Util para separar zona de titulo de zona de contenido.
     */
    public LayoutRegion below(int topHeight) {
        int topClamped = Math.min(topHeight, h);
        return new LayoutRegion(x, y + topClamped, w, h - topClamped);
    }

    /**
     * Region superior excluyendo una banda inferior de altura botHeight.
     */
    public LayoutRegion above(int botHeight) {
        int botClamped = Math.min(botHeight, h);
        return new LayoutRegion(x, y, w, h - botClamped);
    }

    /**
     * Region centrada de tamano fijo dentro de esta region.
     */
    public LayoutRegion center(int regionW, int regionH) {
        int rx = x + (w - regionW) / 2;
        int ry = y + (h - regionH) / 2;
        return new LayoutRegion(rx, ry, regionW, regionH);
    }

    /**
     * Fila N dentro de esta region. index=0 es la primera fila.
     * rowHeight = alto de cada fila. gap = separacion entre filas.
     */
    public LayoutRegion row(int index, int rowHeight, int gap) {
        int ry = y + index * (rowHeight + gap);
        return new LayoutRegion(x, ry, w, rowHeight);
    }

    /**
     * Columna N dentro de esta region. index=0 es la primera columna.
     * colWidth = ancho de cada columna. gap = separacion entre columnas.
     */
    public LayoutRegion col(int index, int colWidth, int gap) {
        int rx = x + index * (colWidth + gap);
        return new LayoutRegion(rx, y, colWidth, h);
    }

    // ----------------------------------------------------------------
    // Posicionamiento con Anchor
    // ----------------------------------------------------------------

    /**
     * Calcula la posicion [x, y] de un elemento de tamano elemW x elemH
     * dentro de esta region segun el anclaje dado.
     *
     * @return int[2] donde [0] = x, [1] = y del elemento
     */
    public int[] anchor(Anchor a, int elemW, int elemH) {
        return new int[]{ anchorX(a, elemW), anchorY(a, elemH) };
    }

    /** Coordenada X del elemento segun anclaje horizontal. */
    public int anchorX(Anchor a, int elemW) {
        if (a.isLeft())   return x;
        if (a.isCenter()) return x + (w - elemW) / 2;
        if (a.isRight())  return x + w - elemW;
        return x;
    }

    /** Coordenada Y del elemento segun anclaje vertical. */
    public int anchorY(Anchor a, int elemH) {
        if (a.isTop()) return y;
        if (a.isMid()) return y + (h - elemH) / 2;
        if (a.isBot()) return y + h - elemH;
        return y;
    }

    // ----------------------------------------------------------------
    // Utilidades de consulta
    // ----------------------------------------------------------------

    /** Centro horizontal de la region. */
    public int cx() { return x + w / 2; }

    /** Centro vertical de la region. */
    public int cy() { return y + h / 2; }

    /** Borde derecho (x + w). */
    public int x2() { return x + w; }

    /** Borde inferior (y + h). */
    public int y2() { return y + h; }

    /** true si el punto (mx, my) esta dentro de la region. */
    public boolean contains(double mx, double my) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    /** true si la region tiene area mayor que cero. */
    public boolean isValid() { return w > 0 && h > 0; }

    @Override
    public String toString() {
        return String.format("LayoutRegion[x=%d y=%d w=%d h=%d]", x, y, w, h);
    }
}