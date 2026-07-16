package com.eternalcraft.gripeverde.client.theme;

/**
 * GhoulPalette - Paleta de colores del proyecto.
 *
 * Todos los valores son provisionales y se refinarán con pruebas visuales.
 * Separada de UIScale para que un cambio de paleta no toque dimensiones.
 */
public final class GhoulPalette {

    private GhoulPalette() {}

    // ── FONDOS (sólidos, opacidad total) ─────────────────────────────
    public static final int BG_PAPER       = 0xFF2A1E10;  // marrón papel oscuro
    public static final int BG_PAPER_DEEP  = 0xFF1F1408;  // más oscuro para paneles
    public static final int BG_INK_DEEP    = 0xFF120A05;  // tinta muy oscura

    // ── TINTES TEMÁTICOS (para superponer sobre fondo con alpha bajo) ─
    public static final int TINT_PLAGUE = 0x1A2818;   // verde enfermo
    public static final int TINT_BLOOD  = 0x2A0808;   // rojo sangre seca
    public static final int TINT_WOOD   = 0x1A0F05;   // marrón madera
    public static final int TINT_NONE   = 0x000000;   // sin tinte

    // ── MARCOS Y BORDES ──────────────────────────────────────────────
    public static final int FRAME_OUTER  = 0xFF3D2E1A;  // sepia oscuro
    public static final int FRAME_INNER  = 0xFF6B5028;  // bronce
    public static final int FRAME_ACCENT = 0xFF8A6A2D;  // ocre

    // ── TEXTOS ───────────────────────────────────────────────────────
    public static final int TEXT_TITLE    = 0xFFC9B78A;  // papel manchado
    public static final int TEXT_SUBTITLE = 0xFFA08A5C;  // sepia claro
    public static final int TEXT_BODY     = 0xFF8A7A52;  // papel oscuro
    public static final int TEXT_MUTED    = 0xFF6B5533;  // sepia medio

    // ── ACENTOS (para elementos temáticos por pantalla) ──────────────
    public static final int ACCENT_PLAGUE = 0xFF7A9A48;  // verde tóxico
    public static final int ACCENT_BLOOD  = 0xFF8B1F0F;  // sangre fresca
    public static final int ACCENT_AMBER  = 0xFFD4941E;  // ámbar
    public static final int ACCENT_RUST   = 0xFF6B4028;  // óxido

    /** Aplica un alpha custom a un color RGB (ignora el alpha original). */
    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}