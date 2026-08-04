package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * DiaryProgressBar: barra de progreso estilo pergamino con tinta.
 * No es un widget interactivo, solo se renderiza. Usar draw() cuando quieras.
 *
 * Ejemplo en render():
 *   DiaryProgressBar.draw(g, x, y, w, h, 0.75f, "Cargando...");
 */
public final class DiaryProgressBar {

    private DiaryProgressBar() {}

    /** Dibuja una barra de progreso pergamino. progress = 0.0 a 1.0 */
    public static void draw(GuiGraphics g, int x, int y, int w, int h, float progress) {
        draw(g, x, y, w, h, progress, null);
    }

    /** Dibuja una barra de progreso con label opcional centrado. */
    public static void draw(GuiGraphics g, int x, int y, int w, int h, float progress, String label) {
        // Clamp
        progress = Math.max(0f, Math.min(1f, progress));

        // Fondo del track (pergamino oscuro)
        g.fill(x, y, x + w, y + h, 0xFFA89468);
        // Borde tinta
        g.fill(x, y, x + w, y + 1, 0xB0422810);
        g.fill(x, y + h - 1, x + w, y + h, 0xB0422810);
        g.fill(x, y, x + 1, y + h, 0xB0422810);
        g.fill(x + w - 1, y, x + w, y + h, 0xB0422810);

        // Zona rellena de progreso (tinta viva)
        int fillW = (int)((w - 4) * progress);
        if (fillW > 0) {
            g.fill(x + 2, y + 2, x + 2 + fillW, y + h - 2, 0xFF6B4226);
            // Highlight arriba
            g.fill(x + 2, y + 2, x + 2 + fillW, y + 3, 0x80A0704C);
            // Sombra abajo
            g.fill(x + 2, y + h - 3, x + 2 + fillW, y + h - 2, 0x80301608);
        }

        for (int i = 1; i < 5; i++) {
            int tx = x + 2 + (w - 4) * i / 5;
            g.fill(tx, y + 2, tx + 1, y + h - 2, DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 45));
        }

        // Label centrado sobre la barra
        if (label != null && !label.isEmpty()) {
            int lw = Minecraft.getInstance().font.width(label);
            int lx = x + (w - lw) / 2;
            int ly = y + (h - 8) / 2;
            g.drawString(Minecraft.getInstance().font, label, lx, ly, DiaryTheme.INK, false);
        }
    }
}