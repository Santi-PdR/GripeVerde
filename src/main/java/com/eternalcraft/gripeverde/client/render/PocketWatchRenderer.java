package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.RandomSource;

/**
 * Reloj de bolsillo victoriano dibujado con primitivas.
 * Cuelga de una cadena larga desde el borde superior de la pantalla.
 * Oscila suavemente como pendulo.
 * Manecillas de hora y minuto fijas en 11:52 (la hora del brote).
 * Segundero animado con glitches ocasionales (el tiempo esta enfermo).
 * Brillo de cristal deslizante para sensacion de vidrio pulido.
 */
public class PocketWatchRenderer {

    private static final int COLOR_BRASS       = 0xFFB8862B;
    private static final int COLOR_BRASS_DARK  = 0xFF5A3E12;
    private static final int COLOR_FACE        = 0xFFEDE0C4;
    private static final int COLOR_MARK        = 0xFF3B2A16;
    private static final int COLOR_HAND        = 0xFF1A0F05;
    private static final int COLOR_SECOND      = 0xFF6B4A20;
    private static final int COLOR_TEXT        = 0xFF6B4A20;
    private static final int COLOR_CHAIN       = 0xFF8A6420;
    private static final int COLOR_CHAIN_DARK  = 0xFF3E2A10;

    // Estado interno para animaciones
    private static final RandomSource RNG = RandomSource.create();
    private static final long START_TIME = System.currentTimeMillis();

    // Estado del glitch actual
    private static long nextGlitchAt = 0L;
    private static int glitchType = 0;      // 0=stutter, 1=skip, 2=rewind
    private static long glitchStartMs = 0L;
    private static boolean glitchActive = false;

    /**
     * Dibuja el reloj colgando desde topY, oscilando alrededor de cxAnchor.
     * @param g          GuiGraphics
     * @param cxAnchor   posicion X del punto de anclaje (borde superior)
     * @param cy         posicion Y del centro del reloj (referencia sin oscilacion)
     * @param topY       Y del borde superior donde nace la cadena (normalmente 0)
     * @param radius     radio del reloj
     * @param version    string de version a mostrar debajo
     */
    public static void draw(GuiGraphics g, int cxAnchor, int cy, int topY, int radius, String version) {
        long now = System.currentTimeMillis();
        long elapsed = now - START_TIME;

        // Oscilacion pendular sutil: +/- 3 pixels en X, periodo 4 segundos
        double swingT = (elapsed % 4000L) / 4000.0 * 2.0 * Math.PI;
        int swingOffset = (int) Math.round(Math.sin(swingT) * 3.0);
        int cx = cxAnchor + swingOffset;

        // Cadena larga desde topY hasta el borde superior del reloj
        drawLongChain(g, cxAnchor, topY, cx, cy - radius);

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

        // ── BRILLO DEL CRISTAL (antes de manecillas para quedar debajo) ─────
        drawGlassShine(g, cx, cy, radius - 7, elapsed);

        // ── SEGUNDERO ANIMADO CON GLITCH ────────────────────────────────────
        int secondPos = computeSecondPosition(now);
        double secAngle = Math.toRadians(secondPos * 6 - 90);
        int secLen = radius - 10;
        drawThinLine(g,
            cx, cy,
            (int)(cx + Math.cos(secAngle) * secLen),
            (int)(cy + Math.sin(secAngle) * secLen),
            COLOR_SECOND);

        // ── MANECILLAS FIJAS EN 11:52 ───────────────────────────────────────
        double hourAngle = Math.toRadians(11 * 30 + 52 * 0.5 - 90);
        double minAngle  = Math.toRadians(52 * 6 - 90);

        int hourLen = radius - 18;
        int minLen  = radius - 12;

        drawThickLine(g,
            cx, cy,
            (int)(cx + Math.cos(hourAngle) * hourLen),
            (int)(cy + Math.sin(hourAngle) * hourLen),
            COLOR_HAND);

        drawThickLine(g,
            cx, cy,
            (int)(cx + Math.cos(minAngle) * minLen),
            (int)(cy + Math.sin(minAngle) * minLen),
            COLOR_HAND);

        // Punto central
        g.fill(cx - 2, cy - 2, cx + 2, cy + 2, COLOR_HAND);

        // Version debajo del reloj (sigue la oscilacion)
        String label = "v" + version;
        int lw = font.width(label);
        g.drawString(font, label, cx - lw / 2, cy + radius + 6, COLOR_TEXT, false);
    }

    /**
     * Calcula la posicion del segundero (0-59) aplicando glitches ocasionales.
     * Glitches cada 8-12s, uno de: stutter (se traba 800ms), skip (+3 pos), rewind (retrocede 2 por 800ms).
     */
    private static int computeSecondPosition(long now) {
        int baseSec = (int)((now / 1000L) % 60L);

        if (nextGlitchAt == 0L) {
            nextGlitchAt = now + 8000L + RNG.nextInt(4000); // 8-12s
        }

        if (!glitchActive && now >= nextGlitchAt) {
            glitchActive = true;
            glitchStartMs = now;
            glitchType = RNG.nextInt(3);
        }

        if (glitchActive) {
            long dt = now - glitchStartMs;
            switch (glitchType) {
                case 0: // STUTTER: se queda quieto 800ms
                    if (dt < 800L) {
                        return (int)((glitchStartMs / 1000L) % 60L);
                    }
                    break;
                case 1: // SKIP: salta 3 posiciones instantaneamente
                    if (dt < 200L) {
                        return (baseSec + 3) % 60;
                    }
                    break;
                case 2: // REWIND: retrocede 2 ticks durante 800ms
                    if (dt < 800L) {
                        return (baseSec + 58) % 60;
                    }
                    break;
            }
            if (dt > 900L) {
                glitchActive = false;
                nextGlitchAt = now + 8000L + RNG.nextInt(4000);
            }
        }

        return baseSec;
    }

    /**
     * Brillo de cristal: banda diagonal semi-transparente que cruza la cara.
     * Loop de 8 segundos.
     */
    private static void drawGlassShine(GuiGraphics g, int cx, int cy, int faceRadius, long elapsed) {
        float t = (elapsed % 8000L) / 8000f;
        float bandCenter = t * 2f - 1f;
        float bandHalfWidth = 0.22f;

        int r = faceRadius;
        int r2 = r * r;

        for (int dy = -r; dy <= r; dy++) {
            int dxMax = (int) Math.sqrt(r2 - dy * dy);
            for (int dx = -dxMax; dx <= dxMax; dx++) {
                float proj = (dx + dy) / (float)(r * 2);
                float dist = Math.abs(proj - bandCenter);

                if (dist < bandHalfWidth) {
                    float intensity = 1f - (dist / bandHalfWidth);
                    // Peak visible pero elegante: alpha maximo 90
                    int alpha = (int)(intensity * 90);
                    if (alpha > 4) {
                        int color = (alpha << 24) | 0x00FFF5DC;
                        g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                    }
                }
            }
        }
    }

    /**
     * Cadena larga desde el anclaje superior (fixed) hasta la corona del reloj (con swing).
     * Los eslabones se dibujan como pequeños rectangulos alternados.
     * La curva se hace interpolando linealmente entre los dos puntos.
     */
    private static void drawLongChain(GuiGraphics g, int xTop, int yTop, int xBottom, int yBottom) {
        int totalLen = yBottom - yTop;
        if (totalLen < 8) return;

        int linkCount = Math.max(3, totalLen / 4);

        for (int i = 0; i < linkCount; i++) {
            float t = i / (float)(linkCount - 1);
            int x = (int) (xTop + (xBottom - xTop) * t);
            int y = yTop + (int)(t * totalLen);

            // Eslabones alternados: verticales y horizontales
            if (i % 2 == 0) {
                g.fill(x - 1, y, x + 2, y + 3, COLOR_CHAIN);
                g.fill(x - 1, y + 1, x, y + 2, COLOR_CHAIN_DARK);
            } else {
                g.fill(x - 2, y + 1, x + 3, y + 2, COLOR_CHAIN);
                g.fill(x - 1, y + 1, x + 2, y + 2, COLOR_CHAIN_DARK);
            }
        }

        // Corona (boton de cuerda) sobre el reloj
        g.fill(xBottom - 3, yBottom - 3, xBottom + 3, yBottom, COLOR_BRASS_DARK);
        g.fill(xBottom - 2, yBottom - 4, xBottom + 2, yBottom - 3, COLOR_BRASS);
    }

    private static void drawRomanNumeral(GuiGraphics g, Font font, int cx, int cy, String txt) {
        int w = font.width(txt);
        g.drawString(font, txt, cx - w / 2, cy - 4, COLOR_MARK, false);
    }

    private static void fillCircle(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt(r * r - dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

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

    private static void drawThinLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int x = x1, y = y1;
        while (true) {
            g.fill(x, y, x + 1, y + 1, color);
            if (x == x2 && y == y2) break;
            int e2 = err * 2;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 <  dx) { err += dx; y += sy; }
        }
    }
}