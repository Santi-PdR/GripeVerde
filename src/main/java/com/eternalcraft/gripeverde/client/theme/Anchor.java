package com.eternalcraft.gripeverde.client.theme;

/**
 * Anchor — punto de anclaje para posicionar un elemento dentro de una LayoutRegion.
 *
 * Uso:
 *   int[] pos = region.anchor(Anchor.MID_CENTER, elemW, elemH);
 *   int x = pos[0]; int y = pos[1];
 *
 * Nomenclatura: FILA_COLUMNA
 *   TOP = borde superior | MID = centro vertical | BOT = borde inferior
 *   LEFT = borde izquierdo | CENTER = centro horizontal | RIGHT = borde derecho
 */
public enum Anchor {

    // Fila superior
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,

    // Fila central
    MID_LEFT,
    MID_CENTER,
    MID_RIGHT,

    // Fila inferior
    BOT_LEFT,
    BOT_CENTER,
    BOT_RIGHT;

    /** true si el anclaje esta en el borde superior. */
    public boolean isTop()    { return this == TOP_LEFT || this == TOP_CENTER || this == TOP_RIGHT; }

    /** true si el anclaje esta en el centro vertical. */
    public boolean isMid()    { return this == MID_LEFT || this == MID_CENTER || this == MID_RIGHT; }

    /** true si el anclaje esta en el borde inferior. */
    public boolean isBot()    { return this == BOT_LEFT || this == BOT_CENTER || this == BOT_RIGHT; }

    /** true si el anclaje esta en el borde izquierdo. */
    public boolean isLeft()   { return this == TOP_LEFT  || this == MID_LEFT  || this == BOT_LEFT; }

    /** true si el anclaje esta centrado horizontalmente. */
    public boolean isCenter() { return this == TOP_CENTER || this == MID_CENTER || this == BOT_CENTER; }

    /** true si el anclaje esta en el borde derecho. */
    public boolean isRight()  { return this == TOP_RIGHT || this == MID_RIGHT || this == BOT_RIGHT; }
}