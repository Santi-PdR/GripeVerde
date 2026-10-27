package com.eternalcraft.gripeverde.client.render;

/**
 * DiaryTheme v0.4.1 — Guts & Blackpowder tone
 * Rojo britanico + sepia + negro. Menos dorado.
 */
public class DiaryTheme {

    // ─── PAPEL ─────────────────────────────────
    public static final int PAPER          = 0xFFE8DCB4;
    public static final int PAPER_STAINED  = 0xFFC9B78A;
    public static final int PAPER_DARK     = 0xFF8A7A52;

    // ─── TINTA ─────────────────────────────────
    public static final int INK            = 0xFF1A1108;
    public static final int INK_FADED      = 0xFF3D2E1F;
    public static final int INK_BLACK      = 0xFF0A0503;

    // ─── SEPIA/OCRE ────────────────────────────
    public static final int SEPIA_LIGHT    = 0xFFA08A5C;
    public static final int SEPIA_MID      = 0xFF6B5533;
    public static final int SEPIA_DARK     = 0xFF3D2E1A;
    public static final int OCHRE          = 0xFF8A6A2D;

    // ─── METALES (menos dorado) ────────────────
    public static final int BRONZE         = 0xFF6B5028;
    public static final int BRONZE_DARK    = 0xFF3D2E14;
    public static final int GOLD_TARNISHED = 0xFF8A6A2D;
    // Aged brass that stays legible as the title button darkens on hover.
    public static final int FIELD_SUBTITLE = 0xFFB89F70;

    // ─── ROJO BRITANICO (nuevo acento principal) ─
    public static final int REDCOAT        = 0xFF8B1A15;
    public static final int REDCOAT_DARK   = 0xFF5C0A08;
    public static final int REDCOAT_BRIGHT = 0xFFB8281F;

    // ─── AMBAR ─────────────────────────────────
    public static final int AMBER_FLAME    = 0xFFD4941E;
    public static final int AMBER_GLOW     = 0xFFFFAA33;
    public static final int AMBER_WARM     = 0xFF8B5A14;
    public static final int CANDLE_WAX     = 0xFFEED3A5;

    // ─── PLAGA ─────────────────────────────────
    public static final int PLAGUE_GREEN   = 0xFF3A5028;
    public static final int PLAGUE_SICK    = 0xFF5A7038;
    public static final int PLAGUE_TOXIC   = 0xFF7A9A48;
 
    // ─── OJOS DEL GHOUL ────────────────────────
    public static final int GHOUL_EYE        = 0xFF72BFD6;
    public static final int GHOUL_EYE_GLOW   = 0xFFB6EFFA;
    public static final int GHOUL_INK        = 0xFF285A66; // dark teal ink for text on paper
    public static final int GHOUL_INK_ACTIVE = 0xFF1D4D59; // focused/hovered paper heading

    // ─── SANGRE ────────────────────────────────
    public static final int BLOOD_FRESH    = 0xFF8B1F0F;
    public static final int BLOOD_DRIED    = 0xFF5C1508;
    public static final int WAX_SEAL       = 0xFF6B1F0F;

    // ─── VARIOS ────────────────────────────────
    public static final int BONE_WHITE     = 0xFFDDD3B8;
    public static final int MOON_WHITE     = 0xFFE8DCB8;
    public static final int RAIN_LINE      = 0x22B8A88C;

    // ─── FONDO NOCHE ───────────────────────────
    public static final int NIGHT_TOP      = 0xFF0A0705;
    public static final int NIGHT_UPPER    = 0xFF1A0F08;
    public static final int NIGHT_MID      = 0xFF2A1E10;
    public static final int NIGHT_HORIZON  = 0xFF3D2A15;

    public static int withAlpha(int color, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}
