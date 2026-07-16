package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Reloj de bolsillo victoriano dibujado con primitivas.
 * Muestra la "hora del brote" (11:52) como detalle atmosferico.
 * Debajo del reloj se muestra la version del mod.
 */
public class PocketWatchRenderer {

    private static final int COLOR_BRASS       = 0xFFB8862B;
    private static final int COLOR_BRASS_DARK  = 0xFF5A3E12;
    private static final int COLOR_FACE        = 0xFFEDE0C4;
    private static final int COLOR_MARK        = 0xFF3B2A16;
    private static final int COLOR_HAND        = 0xFF1A0F05;
    private static final int COLOR_TEXT        = 0xFF6B4A20;

    public static void draw(GuiGraphics g, int cx, int cy, int radius, String version) {
        // Cadena decorativa arriba (3 eslabones)
        drawChain(g, cx, cy - radius);

        // Anillo exterior (marco dorado)
        fillCircle(g, cx, cy, radius, COLOR_BRASS);
        fillCircle(g, cx, cy, radius - 2, COLOR_BRASS_DARK);
        fillCircle(g, cx, cy, radius - 4, COLOR_BRASS);

        // Cara del reloj
        fillCircle(g, cx, cy, radius - 7, COLOR_FACE);

        // 12 marcas horarias
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 - 90);
            int outerR = radius - 9;
            int innerR = radius - 13;
            int x1 = (int)(cx + Math.cos(angle) * outerR);
            int y1 = (int)(cy + Math.sin(angle) * outerR);
            int x2 = (int)(cx + Math.cos(angle) * innerR);
            int y2 = (int)(cy + Math.sin(angle) * innerR);
            drawThickLine(g, x1, y1, x2, y2, COLOR_MARK);
        }

        // Numeros romanos cardinales
        Font font = Minecraft.getInstance().font;
        int textR = radius - 20;
        drawRomanNumeral(g, font, cx, cy - textR, "XII");
        drawRomanNumeral(g, font, cx + textR, cy, "III");
        drawRomanNumeral(g, font, cx, cy + textR, "VI");
        drawRomanNumeral(g, font, cx - textR, cy, "IX");

        // Manecillas fijas en 11:52 (la hora del brote)
        // Hora 11 -> angulo (11*30 + 52*0.5) = 356 grados aprox
        // Minuto 52 -> angulo (52*6) = 312 grados
        double hourAngle = Math.toRadians(11 * 30 + 52 * 0.5 - 90);
        double minAngle  = Math.toRadians(52 * 6 - 90);

        int hourLen = radius - 18;
        int minLen  = radius - 12;

        // Manecilla de horas (mas corta y gruesa)
        drawThickLine(g,
            cx, cy,
            (int)(cx + Math.cos(hourAngle) * hourLen),
            (int)(cy + Math.sin(hourAngle) * hourLen),
            COLOR_HAND);

        // Manecilla de minutos (mas larga)
        drawThickLine(g,
            cx, cy,
            (int)(cx + Math.cos(minAngle) * minLen),
            (int)(cy + Math.sin(minAngle) * minLen),
            COLOR_HAND);

        // Punto central
        g.fill(cx - 2, cy - 2, cx + 2, cy + 2, COLOR_HAND);

        // Version debajo del reloj
        String label = "v" + version;
        int lw = font.width(label);
        g.drawString(font, label, cx - lw / 2, cy + radius + 6, COLOR_TEXT, false);
    }

    // Cadena estilizada arriba del reloj
    private static void drawChain(GuiGraphics g, int cx, int topY) {
        for (int i = 0; i < 3; i++) {
            int y = topY - 4 - i * 4;
            g.fill(cx - 2, y, cx + 2, y + 2, COLOR_BRASS);
        }
        // Corona (boton de cuerda)
        g.fill(cx - 3, topY - 3, cx + 3, topY, COLOR_BRASS_DARK);
    }

    private static void drawRomanNumeral(GuiGraphics g, Font font, int cx, int cy, String txt) {
        int w = font.width(txt);
        g.drawString(font, txt, cx - w / 2, cy - 4, COLOR_MARK, false);
    }

    // Circulo relleno usando algoritmo simple
    private static void fillCircle(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt(r * r - dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    // Linea gruesa (2px) usando Bresenham simplificado
    private static void drawThickLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int x = x1, y = y1;
        while (true) {
            g.fill(x, y, x + 2, y + 2, color);
            if (x == x2 && y == y2) break;
            int e2 = err * 2;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 <  dx) { err += dx; y += sy; }
        }
    }
}