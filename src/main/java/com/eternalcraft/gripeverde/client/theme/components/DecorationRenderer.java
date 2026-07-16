package com.eternalcraft.gripeverde.client.theme.components;

import net.minecraft.client.gui.GuiGraphics;

/**
 * DecorationRenderer — sistema de decoraciones ancladas al marco.
 *
 * Este archivo define la INTERFAZ del sistema. Las decoraciones concretas
 * (zombis, cuervos, ornamentos, símbolos, etc.) se implementan en clases
 * separadas del paquete decorations/ (por crear más adelante).
 *
 * Uso previsto:
 *   FrameDecoration zombie = new ZombieDecoration();
 *   DecorationRenderer.render(g, screen.width, screen.height,
 *                              zombie, FramePosition.RIGHT_CENTER);
 */
public final class DecorationRenderer {

    private DecorationRenderer() {}

    /** Posiciones ancladas al marco donde puede ir una decoración. */
    public enum FramePosition {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        LEFT_CENTER, RIGHT_CENTER,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT,
        LEFT_QUARTER, RIGHT_QUARTER
    }

    /**
     * Contrato de una decoración de marco.
     * Cada decoración concreta dibuja su propio arte en las coordenadas
     * que le pasa el framework.
     */
    public interface FrameDecoration {
        /**
         * Dibuja la decoración anclada en (anchorX, anchorY).
         * La decoración decide cómo posicionar su arte respecto al ancla.
         */
        void draw(GuiGraphics g, int anchorX, int anchorY, FramePosition anchor);
    }

    /**
     * Renderiza una decoración en la posición del marco especificada.
     * El framework calcula el punto de anclaje según la posición.
     */
    public static void render(GuiGraphics g, int screenW, int screenH,
                                FrameDecoration decoration, FramePosition position) {
        int anchorX = 0;
        int anchorY = 0;
        switch (position) {
            case TOP_LEFT:      anchorX = 0;              anchorY = 0;              break;
            case TOP_CENTER:    anchorX = screenW / 2;    anchorY = 0;              break;
            case TOP_RIGHT:     anchorX = screenW;        anchorY = 0;              break;
            case LEFT_CENTER:   anchorX = 0;              anchorY = screenH / 2;    break;
            case RIGHT_CENTER:  anchorX = screenW;        anchorY = screenH / 2;    break;
            case BOTTOM_LEFT:   anchorX = 0;              anchorY = screenH;        break;
            case BOTTOM_CENTER: anchorX = screenW / 2;    anchorY = screenH;        break;
            case BOTTOM_RIGHT:  anchorX = screenW;        anchorY = screenH;        break;
            case LEFT_QUARTER:  anchorX = 0;              anchorY = screenH / 4;    break;
            case RIGHT_QUARTER: anchorX = screenW;        anchorY = screenH / 4;    break;
        }
        decoration.draw(g, anchorX, anchorY, position);
    }
}