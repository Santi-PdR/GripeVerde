package com.eternalcraft.gripeverde.client.theme;

/**
 * UIScale - Constantes semÃ¡nticas de dimensiones.
 *
 * Todos los valores estÃ¡n en pÃ­xeles VIRTUALES del GUI de Minecraft.
 * NO hacemos escalado propio. Confiamos en el GuiScale del jugador.
 */
public final class UIScale {

    private UIScale() {}

    // Padding / mÃ¡rgenes
    public static final int PAD_XS  = 2;
    public static final int PAD_SM  = 4;
    public static final int PAD_MD  = 8;
    public static final int PAD_LG  = 16;
    public static final int PAD_XL  = 24;
    public static final int PAD_XXL = 32;

    // Bordes / lÃ­neas
    public static final int BORDER_THIN  = 1;
    public static final int BORDER_MED   = 2;
    public static final int BORDER_THICK = 3;

    // Alpha estandarizado
    public static final int ALPHA_FULL   = 255;
    public static final int ALPHA_STRONG = 220;
    public static final int ALPHA_MEDIUM = 180;
    public static final int ALPHA_LIGHT  = 120;
    public static final int ALPHA_SUBTLE = 80;
    public static final int ALPHA_GHOST  = 40;

    // Escalas de texto (para pose().scale())
    public static final float TEXT_SMALL  = 0.85f;
    public static final float TEXT_NORMAL = 1.0f;
    public static final float TEXT_MEDIUM = 1.25f;
    public static final float TEXT_LARGE  = 1.5f;
    public static final float TEXT_HUGE   = 2.0f;
    public static final float TEXT_TITLE  = 2.5f;

    // Anclajes
    public static int centerX(int screenW, int elementW) {
        return (screenW - elementW) / 2;
    }

    public static int centerY(int screenH, int elementH) {
        return (screenH - elementH) / 2;
    }

    // ----------------------------------------------------------------
    // Ritmo vertical — separacion entre filas y secciones
    // ----------------------------------------------------------------
    public static final int ROW_TIGHT   = 20;  // filas juntas
    public static final int ROW_NORMAL  = 24;  // ritmo estandar
    public static final int ROW_LOOSE   = 30;  // espacio comodo
    public static final int ROW_SECTION = 40;  // separacion de seccion
    public static final int ROW_HERO    = 60;  // elemento destacado

    // ----------------------------------------------------------------
    // Margenes de pantalla — distancia al borde exterior
    // ----------------------------------------------------------------
    public static final int MARGIN_X    = 20;  // margen horizontal
    public static final int MARGIN_Y    = 16;  // margen vertical
    public static final int SAFE_TOP    = 12;  // zona segura superior
    public static final int SAFE_BOTTOM = 12;  // zona segura inferior

    // ----------------------------------------------------------------
    // Alturas de boton estandar
    // ----------------------------------------------------------------
    public static final int BTN_H_SM    = 20;
    public static final int BTN_H_MD    = 24;
    public static final int BTN_H_LG    = 28;

    // ----------------------------------------------------------------
    // Anchos de boton estandar
    // ----------------------------------------------------------------
    public static final int BTN_W_SM    = 80;
    public static final int BTN_W_MD    = 120;
    public static final int BTN_W_LG    = 160;
    public static final int BTN_W_FULL  = 200;
}