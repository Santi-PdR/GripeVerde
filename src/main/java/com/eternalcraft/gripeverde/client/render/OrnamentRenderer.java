package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * OrnamentRenderer v0.4.0
 * Marcos y ornamentos victorianos.
 * Reemplaza los corner brackets tecnologicos por filigranas.
 */
@OnlyIn(Dist.CLIENT)
public class OrnamentRenderer {

    /**
     * Marco ornamental victoriano: doble borde con esquinas decoradas.
     */
    public static void drawVictorianFrame(GuiGraphics g, int x, int y, int w, int h,
                                           int fillColor, int borderColor, int accentColor) {
        // Fondo
        g.fill(x, y, x + w, y + h, fillColor);

        // Borde exterior doble
        drawDoubleBorder(g, x, y, w, h, borderColor);

        // Esquinas ornamentales (filigrana en angulo)
        drawFiligreeCorner(g, x, y,             1,  1, accentColor);
        drawFiligreeCorner(g, x + w - 1, y,     -1, 1, accentColor);
        drawFiligreeCorner(g, x, y + h - 1,     1,  -1, accentColor);
        drawFiligreeCorner(g, x + w - 1, y + h - 1, -1, -1, accentColor);

        // Fleuron central en los bordes horizontales
        int cx = x + w / 2;
        drawFleuron(g, cx, y - 1,     accentColor);
        drawFleuron(g, cx, y + h - 2, accentColor);
    }

    /**
     * Doble borde: linea exterior + linea interior con espacio en medio.
     */
    private static void drawDoubleBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        // Linea exterior
        g.fill(x, y,             x + w, y + 1,         color);
        g.fill(x, y + h - 1,     x + w, y + h,         color);
        g.fill(x, y,             x + 1, y + h,         color);
        g.fill(x + w - 1, y,     x + w, y + h,         color);
        // Linea interior (a 3px)
        g.fill(x + 3, y + 3,     x + w - 3, y + 4,     color);
        g.fill(x + 3, y + h - 4, x + w - 3, y + h - 3, color);
        g.fill(x + 3, y + 3,     x + 4, y + h - 3,     color);
        g.fill(x + w - 4, y + 3, x + w - 3, y + h - 3, color);
    }

    /**
     * Filigrana en esquina: una espiral/curva ornamental.
     * dx/dy indican direccion (1 o -1) para orientar la esquina.
     */
    private static void drawFiligreeCorner(GuiGraphics g, int x, int y, int dx, int dy, int color) {
        // Punto central de la esquina
        int px = (dx > 0) ? x + 4 : x - 4;
        int py = (dy > 0) ? y + 4 : y - 4;

        // Curva ornamental (pixel art de filigrana)
        // Linea horizontal corta
        int hStart = (dx > 0) ? px + 2 : px - 4;
        int hEnd   = (dx > 0) ? px + 6 : px - 2;
        g.fill(hStart, py, hEnd, py + 1, color);

        // Linea vertical corta
        int vStart = (dy > 0) ? py + 2 : py - 4;
        int vEnd   = (dy > 0) ? py + 6 : py - 2;
        g.fill(px, vStart, px + 1, vEnd, color);

        // Punto ornamental en la interseccion
        g.fill(px + (dx > 0 ? 1 : -1), py + (dy > 0 ? 1 : -1),
               px + (dx > 0 ? 3 : -2), py + (dy > 0 ? 3 : -2), color);

        // Diagonal decorativa
        int diagX = px + (dx > 0 ? 2 : -2);
        int diagY = py + (dy > 0 ? 2 : -2);
        g.fill(diagX, diagY, diagX + 1, diagY + 1, color);
    }

    /**
     * Fleuron: adorno floral central en los bordes.
     * Un rombo pequeno con puntos decorativos.
     */
    private static void drawFleuron(GuiGraphics g, int cx, int cy, int color) {
        // Rombo central
        g.fill(cx - 1, cy,     cx + 2, cy + 1,  color);
        g.fill(cx,     cy - 1, cx + 1, cy + 2,  color);
        // Puntos laterales
        g.fill(cx - 3, cy,     cx - 2, cy + 1,  color);
        g.fill(cx + 3, cy,     cx + 4, cy + 1,  color);
    }

    /**
     * Panel de papel envejecido con sello de cera.
     */
    public static void drawPaperPanel(GuiGraphics g, int x, int y, int w, int h) {
        // Papel base
        g.fill(x, y, x + w, y + h, DiaryTheme.PAPER);

        // Manchas de humedad (puntos aleatorios oscuros)
        java.util.Random rng = new java.util.Random((long)(x * 31L + y * 17L + w));
        for (int i = 0; i < 8; i++) {
            if (w <= 4 || h <= 4) continue;
            int sx = x + rng.nextInt(w - 4);
            int sy = y + rng.nextInt(h - 4);
            int size = 2 + rng.nextInt(3);
            int stainAlpha = 30 + rng.nextInt(40);
            g.fill(sx, sy, sx + size, sy + size,
                AtmosphereRenderer.withAlpha(0x006B5533, stainAlpha));
        }

        // Bordes desgastados (mas oscuros)
        g.fill(x, y,         x + w, y + 1,     DiaryTheme.PAPER_DARK);
        g.fill(x, y + h - 1, x + w, y + h,     DiaryTheme.PAPER_DARK);
        g.fill(x, y,         x + 1, y + h,     DiaryTheme.PAPER_DARK);
        g.fill(x + w - 1, y, x + w, y + h,     DiaryTheme.PAPER_DARK);
    }

    /**
     * Sello de cera rojo (para posters oficiales).
     */
    public static void drawWaxSeal(GuiGraphics g, int cx, int cy, int radius) {
        // Cuerpo del sello (rojo sangre)
        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int) Math.sqrt(radius * radius - dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, DiaryTheme.WAX_SEAL);
        }
        // Highlight superior izquierdo
        for (int dy = -radius; dy <= 0; dy++) {
            int dx = (int) Math.sqrt(radius * radius - dy * dy);
            g.fill(cx - dx, cy + dy, cx, cy + dy + 1,
                AtmosphereRenderer.withAlpha(0x008B3020, 80));
        }
        // Sombra inferior derecha
        for (int dy = 0; dy <= radius; dy++) {
            int dx = (int) Math.sqrt(radius * radius - dy * dy);
            g.fill(cx, cy + dy, cx + dx + 1, cy + dy + 1,
                AtmosphereRenderer.withAlpha(0x00000000, 60));
        }
        // Iniciales en el centro
        int crossSize = radius / 2;
        g.fill(cx - crossSize, cy - 1, cx + crossSize + 1, cy + 1, 0xFF3D0A05);
        g.fill(cx - 1, cy - crossSize, cx + 1, cy + crossSize + 1, 0xFF3D0A05);
    }
}