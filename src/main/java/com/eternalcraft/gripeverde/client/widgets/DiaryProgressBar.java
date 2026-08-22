package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Barra documental reutilizable para progreso, volumen y estados continuos.
 */
public final class DiaryProgressBar {

    public enum Style {
        INK(0xFF6B4226, 0xFF2E1A0F, 0x80A0704C),
        PLAGUE(0xFF5A7038, 0xFF2A3820, 0x809AB86A),
        ALERT(0xFF8B1F0F, 0xFF4A1008, 0x80D47A5A),
        AMBER(0xFFD4941E, 0xFF6B5028, 0x80FFE2A0),
        STEEL(0xFF7C808A, 0xFF464A52, 0x80D0D4DE);

        final int fill;
        final int border;
        final int highlight;

        Style(int fill, int border, int highlight) {
            this.fill = fill;
            this.border = border;
            this.highlight = highlight;
        }
    }

    private DiaryProgressBar() {}

    public static void draw(GuiGraphics g, int x, int y, int w, int h, float progress) {
        draw(g, x, y, w, h, progress, null, Style.INK, -1f);
    }

    public static void draw(GuiGraphics g, int x, int y, int w, int h, float progress, String label) {
        draw(g, x, y, w, h, progress, label, Style.INK, -1f);
    }

    public static void draw(GuiGraphics g, int x, int y, int w, int h, float progress,
                            String label, Style style) {
        draw(g, x, y, w, h, progress, label, style, -1f);
    }

    /**
     * marker = 0..1 para dibujar una marca vertical (ej: umbral de alerta).
     * Pasa -1 para omitirla.
     */
    public static void draw(GuiGraphics g, int x, int y, int w, int h, float progress,
                            String label, Style style, float marker) {
        // v0.21.17: medidores retirados por pedido del usuario (carteles de v0.18/0.21).
        // La firma se conserva para no tocar los call sites.
    }
}
