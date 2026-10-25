package com.eternalcraft.gripeverde.client.render;

import com.eternalcraft.gripeverde.client.audio.MenuAmbience;
import net.minecraft.client.Minecraft;
import com.eternalcraft.gripeverde.client.util.MotionPolicy;
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
 * Notifica a MenuAmbience cada vez que el segundero cambia de posicion.
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

    private static final RandomSource RNG = RandomSource.create();
    private static final long START_TIME = System.currentTimeMillis();

    // Rotating "hour of the outbreak" pool - rotates on session start + every 5 min
    private static final int[][] HOUR_POOL = {
        {11, 52},  // The original
        { 3, 17},  // Dead of night
        { 4, 44},  // Cursed number
        { 2, 33},  // Predawn
        {23, 11},  // Just before midnight
        { 5, 27},  // Grey dawn
        { 1, 13},  // Bad luck hour
        {22, 48}   // Late shift
    };
    private static int hourIndex = -1;
    private static long lastHourRotateMs = 0L;
    private static long HOUR_ROTATE_MS = 300_000L;  // 5 min

    private static int[] getCurrentHour() {
        long now = System.currentTimeMillis();
        if (hourIndex < 0 || (now - lastHourRotateMs) >= HOUR_ROTATE_MS) {
            int prev = hourIndex;
            int next;
            if (HOUR_POOL.length <= 1) {
                next = 0;
            } else {
                do {
                    next = RNG.nextInt(HOUR_POOL.length);
                } while (next == prev);
            }
            hourIndex = next;
            lastHourRotateMs = now;
            HOUR_ROTATE_MS = 180_000L + (long)(RNG.nextDouble() * 300_000L);
        }
        return HOUR_POOL[hourIndex];
    }

    // Estado del glitch actual
    private static long nextGlitchAt = 0L;
    private static int glitchType = 0;
    private static long glitchStartMs = 0L;
    private static boolean glitchActive = false;

    // Tracking del segundero para notificar cambios
    private static int lastSecondPos = -1;

    // ── v0.16.0: el reloj en la mano ────────────────────────────────────────
    private static boolean inspectionMode = false;
    private static long inspectionStartMs = 0L;
    private static final long SESSION_START = System.currentTimeMillis();

    public static boolean isInspecting() { return inspectionMode; }

    /** Abre o cierra la inspeccion del reloj. */
    public static void toggleInspection() {
        inspectionMode = !inspectionMode;
        inspectionStartMs = System.currentTimeMillis();
    }

    public static void closeInspection() { inspectionMode = false; }

    /** El tiempo de sesion enferma al segundero: glitches mas frecuentes. */
    private static long sessionMinutes() {
        return (System.currentTimeMillis() - SESSION_START) / 60_000L;
    }

    public static boolean isTimeSick() { return sessionMinutes() >= 8L; }

    /** Dibuja el reloj sostenido en la mano, con su inscripcion. */
    public static void drawInspection(GuiGraphics g, int w, int h, String version) {
        // Fondo: la vigilia se atenúa
        g.fill(0, 0, w, h, 0xB0060402);
        int cx = w / 2;
        int cy = Math.max(120, h / 2 - 30);
        int radius = Math.min(72, Math.min(w, h) / 3);
        // Cadena corta hacia arriba
        drawLongChain(g, cx, Math.max(0, cy - radius - 90), cx, cy - radius);
        fillCircle(g, cx, cy, radius, COLOR_BRASS);
        fillCircle(g, cx, cy, radius - 3, COLOR_BRASS_DARK);
        fillCircle(g, cx, cy, radius - 5, COLOR_BRASS);
        fillCircle(g, cx, cy, radius - 9, COLOR_FACE);
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 - 90);
            int outerR = radius - 12;
            int innerR = radius - 17;
            int x1 = (int)(cx + Math.cos(angle) * outerR);
            int y1 = (int)(cy + Math.sin(angle) * outerR);
            int x2 = (int)(cx + Math.cos(angle) * innerR);
            int y2 = (int)(cy + Math.sin(angle) * innerR);
            drawThickLine(g, x1, y1, x2, y2, COLOR_MARK);
        }
        Font font = Minecraft.getInstance().font;
        int textR = radius - 26;
        drawRomanNumeral(g, font, cx, cy - textR, "XII");
        drawRomanNumeral(g, font, cx + textR, cy, "III");
        drawRomanNumeral(g, font, cx, cy + textR, "VI");
        drawRomanNumeral(g, font, cx - textR, cy, "IX");
        // Manecillas clavadas en la hora del brote
        int[] hour = getCurrentHour();
        double hourAngle = Math.toRadians(hour[0] * 30 + hour[1] * 0.5 - 90);
        double minAngle = Math.toRadians(hour[1] * 6 - 90);
        drawThickLine(g, cx, cy, (int)(cx + Math.cos(hourAngle) * (radius - 26)), (int)(cy + Math.sin(hourAngle) * (radius - 26)), COLOR_HAND);
        drawThickLine(g, cx, cy, (int)(cx + Math.cos(minAngle) * (radius - 17)), (int)(cy + Math.sin(minAngle) * (radius - 17)), COLOR_HAND);
        g.fill(cx - 2, cy - 2, cx + 2, cy + 2, COLOR_HAND);

        // La inscripcion grabada en la tapa
        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        long age = System.currentTimeMillis() - inspectionStartMs;
        int chars = MotionPolicy.isReduced() ? 40 : (int)Math.min(40, age / 60L);
        String l1 = en ? "S.P. - 11:52" : "S.P. - 11:52";
        String l2 = en ? "may time be gentle with you" : "que el tiempo te sea leve";
        int a1 = Math.min(200, chars * 8);
        if (a1 > 8) {
            int w1 = font.width(l1);
            g.drawString(font, l1, cx - w1 / 2, cy + radius + 14, (a1 << 24) | 0xB89A6E, false);
        }
        if (chars > 14) {
            int shown = Math.min(l2.length(), chars - 14);
            String part = l2.substring(0, shown);
            int w2 = font.width(part);
            g.drawString(font, part, cx - w2 / 2, cy + radius + 28, (Math.min(160, (chars - 14) * 8) << 24) | 0x8E7A55, false);
        }
        String hint = en ? "~ click anywhere to let it swing again ~" : "~ pulsa en cualquier parte para que vuelva a oscilar ~";
        int wh2 = font.width(hint);
        g.drawString(font, hint, cx - wh2 / 2, h - 26, 0x60B89A6E, false);
    }

    public static void draw(GuiGraphics g, int cxAnchor, int cy, int topY, int radius, String version) {
        long now = System.currentTimeMillis();
        long elapsed = now - START_TIME;
        boolean reducedMotion = MotionPolicy.isReduced();

        double swingT = (elapsed % 5000L) / 5000.0 * 2.0 * Math.PI;
        int swingOffset = reducedMotion ? 0 : (int) Math.round(Math.sin(swingT) * 6.0);
        // v0.16.0: cuando la campana cuenta trece, el reloj tiembla.
        if (!reducedMotion && com.eternalcraft.gripeverde.client.lore.HiddenCipher.isChiming()) {
            swingOffset += (int)(Math.sin(now / 34.0) * 2.4);
        }
        int cx = cxAnchor + swingOffset;

        drawLongChain(g, cxAnchor, topY, cx, cy - radius);

        fillCircle(g, cx, cy, radius, COLOR_BRASS);
        fillCircle(g, cx, cy, radius - 2, COLOR_BRASS_DARK);
        fillCircle(g, cx, cy, radius - 4, COLOR_BRASS);

        fillCircle(g, cx, cy, radius - 7, COLOR_FACE);

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

        Font font = Minecraft.getInstance().font;
        int textR = radius - 20;
        drawRomanNumeral(g, font, cx, cy - textR, "XII");
        drawRomanNumeral(g, font, cx + textR, cy, "III");
        drawRomanNumeral(g, font, cx, cy + textR, "VI");
        drawRomanNumeral(g, font, cx - textR, cy, "IX");

        if (!reducedMotion) drawGlassShine(g, cx, cy, radius - 7, elapsed);

        // Segundero animado con glitch + notificacion de audio
        int secondPos = computeSecondPosition(now);

        if (secondPos != lastSecondPos) {
            // Tick solo cuando cruza una marca horaria (cada 5 segundos)
            if (lastSecondPos != -1 && secondPos % 5 == 0) {
                MenuAmbience.onSecondTick();
            }
            lastSecondPos = secondPos;
        }

        double secAngle = Math.toRadians(secondPos * 6 - 90);
        int secLen = radius - 10;
        drawThinLine(g,
            cx, cy,
            (int)(cx + Math.cos(secAngle) * secLen),
            (int)(cy + Math.sin(secAngle) * secLen),
            COLOR_SECOND);

        int[] currentHour = getCurrentHour();
        int hh = currentHour[0];
        int mm = currentHour[1];
        double hourAngle = Math.toRadians(hh * 30 + mm * 0.5 - 90);
        double minAngle  = Math.toRadians(mm * 6 - 90);

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

        g.fill(cx - 2, cy - 2, cx + 2, cy + 2, COLOR_HAND);

        String label = "v" + version;
        int lw = font.width(label);
        g.drawString(font, label, cx - lw / 2, cy + radius + 6, COLOR_TEXT, false);
    }

    private static int computeSecondPosition(long now) {
        int baseSec = (int)((now / 1000L) % 60L);
        if (MotionPolicy.isReduced()) {
            glitchActive = false;
            return baseSec;
        }

        // v0.16.0: el tiempo enferma con la sesion: glitches cada vez mas cerca.
        long minGap = Math.max(2800L, 9000L - sessionMinutes() * 600L);
        long maxGap = minGap + 4000L;

        if (nextGlitchAt == 0L) {
            nextGlitchAt = now + minGap + RNG.nextInt((int)(maxGap - minGap));
        }

        if (!glitchActive && now >= nextGlitchAt) {
            glitchActive = true;
            glitchStartMs = now;
            glitchType = RNG.nextInt(3);
        }

        if (glitchActive) {
            long dt = now - glitchStartMs;
            switch (glitchType) {
                case 0:
                    if (dt < 800L) {
                        return (int)((glitchStartMs / 1000L) % 60L);
                    }
                    break;
                case 1:
                    if (dt < 200L) {
                        return (baseSec + 3) % 60;
                    }
                    break;
                case 2:
                    if (dt < 800L) {
                        return (baseSec + 58) % 60;
                    }
                    break;
            }
            if (dt > 900L) {
                glitchActive = false;
                nextGlitchAt = now + minGap + RNG.nextInt((int)(maxGap - minGap));
            }
        }

        return baseSec;
    }

    private static void drawGlassShine(GuiGraphics g, int cx, int cy, int faceRadius, long elapsed) {
        // Optimized: diagonal reflective band via 5 translucent strips instead of per-pixel scan
        float t = (elapsed % 8000L) / 8000f;
        // Band position from -1 to 1 diagonally
        float bandCenter = t * 2.4f - 1.2f;
        if (bandCenter < -1.1f || bandCenter > 1.1f) return; // off-screen, skip

        int r = faceRadius;
        // Convert bandCenter (-1..1) to offset in pixels along diagonal
        int offset = (int)(bandCenter * r * 1.2f);
        // Draw 5 parallel thin strips with fading alpha to simulate soft reflection
        // Strip direction: diagonal top-left to bottom-right, thickness 2px each
        int baseAlpha = 70;
        for (int strip = -2; strip <= 2; strip++) {
            int stripOffset = offset + strip * 3;
            int alpha = baseAlpha - Math.abs(strip) * 14;
            if (alpha <= 6) continue;
            int color = (alpha << 24) | 0xFFF5DC;
            // For each y in circle, x = y + stripOffset (diagonal)
            for (int dy = -r; dy <= r; dy++) {
                int dxMax = (int)Math.sqrt(Math.max(0, r * r - dy * dy));
                int xLine = dy + stripOffset; // diagonal: x proportional to y
                if (xLine >= -dxMax && xLine <= dxMax) {
                    // Draw 2px thick along X
                    g.fill(cx + xLine, cy + dy, cx + xLine + 2, cy + dy + 1, color);
                }
            }
        }
    }

    private static void drawLongChain(GuiGraphics g, int xTop, int yTop, int xBottom, int yBottom) {
        int totalLen = yBottom - yTop;
        if (totalLen < 8) return;

        int linkCount = Math.max(3, totalLen / 4);

        for (int i = 0; i < linkCount; i++) {
            float t = i / (float)(linkCount - 1);
            int x = (int) (xTop + (xBottom - xTop) * t);
            int y = yTop + (int)(t * totalLen);

            if (i % 2 == 0) {
                g.fill(x - 1, y, x + 2, y + 3, COLOR_CHAIN);
                g.fill(x - 1, y + 1, x, y + 2, COLOR_CHAIN_DARK);
            } else {
                g.fill(x - 2, y + 1, x + 3, y + 2, COLOR_CHAIN);
                g.fill(x - 1, y + 1, x + 2, y + 2, COLOR_CHAIN_DARK);
            }
        }

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

    /** Devuelve true si la hora actual del reloj esta cerca de las 12 (11:52-12:08).
     *  Usado por LivingAtmosphere/AtmosphereRenderer para efecto vela. */
    public static boolean isNearNoon() {
        if (hourIndex < 0) return false;
        int[] hour = HOUR_POOL[hourIndex];
        int hh = hour[0];
        int mm = hour[1];
        // Convertir a minutos totales
        int totalMin = (hh % 12) * 60 + mm;
        int distanceToNoon = Math.min(totalMin, 720 - totalMin);
        // Cerca de las 12 = <=10 minutos de diferencia (real time o high noon)
        return distanceToNoon <= 10;
    }
}
