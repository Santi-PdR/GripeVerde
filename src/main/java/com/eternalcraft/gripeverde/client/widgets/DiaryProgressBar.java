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
        progress = Math.max(0f, Math.min(1f, progress));

        g.fill(x + 1, y + 1, x + w + 1, y + h + 1, 0x18000000);
        g.fill(x, y, x + w, y + h, 0xFFA89468);
        g.fill(x, y, x + w, y + 1, 0xB0422810);
        g.fill(x, y + h - 1, x + w, y + h, 0xB0422810);
        g.fill(x, y, x + 1, y + h, 0xB0422810);
        g.fill(x + w - 1, y, x + w, y + h, 0xB0422810);

        int innerX = x + 2;
        int innerY = y + 2;
        int innerW = Math.max(1, w - 4);
        int innerH = Math.max(1, h - 4);
        g.fill(innerX, innerY, innerX + innerW, innerY + innerH, DiaryTheme.withAlpha(style.border, 24));

        // v0.21.11: el estilo ALERT late suavemente (pulso 0.8-1.0, sin parpadeo)
        int fillW = (int) (innerW * progress);
        if (fillW > 0) {
            int pulse = 255;
            if (style == Style.ALERT) {
                pulse = 200 + (int) (55 * (0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 620.0)));
            }
            g.fill(innerX, innerY, innerX + fillW, innerY + innerH, DiaryTheme.withAlpha(style.fill, pulse));
            g.fill(innerX, innerY, innerX + fillW, innerY + 1, style.highlight);
            g.fill(innerX, innerY + innerH - 1, innerX + fillW, innerY + innerH, DiaryTheme.withAlpha(style.border, 150));
        }

        for (int i = 1; i < 5; i++) {
            int tx = innerX + innerW * i / 5;
            g.fill(tx, innerY, tx + 1, innerY + innerH, DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 34));
        }

        if (marker >= 0f && marker <= 1f) {
            int mx = innerX + (int) (innerW * marker);
            g.fill(mx, y - 1, mx + 1, y + h + 1, DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 120));
            DiaryChrome.drawDivisorLozenge(g, mx, y + h / 2);
        }

        if (label != null && !label.isEmpty()) {
            int lw = Minecraft.getInstance().font.width(label);
            int lx = x + (w - lw) / 2;
            int ly = y + (h - 8) / 2;
            g.drawString(Minecraft.getInstance().font, label, lx, ly, DiaryTheme.INK, false);
        }
    }
}
