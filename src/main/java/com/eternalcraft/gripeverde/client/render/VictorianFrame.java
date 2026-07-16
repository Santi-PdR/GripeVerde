package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * VictorianFrame v2 - Sistema adaptativo de decoración.
 *
 * Diseñado sobre una base virtual de 1920x1080. Todos los tamaños de
 * ornamentos, sellos y marcos se escalan proporcionalmente para NO
 * interferir con el HUD, hotbar, botones vanilla ni información crítica.
 *
 * R13 aplicado: los sellos NUNCA aparecen cerca del área central inferior
 * (donde vive la hotbar, HUD y botones de menús vanilla).
 */
@OnlyIn(Dist.CLIENT)
public class VictorianFrame {

    // Base virtual para el escalado
    private static final float BASE_W = 1920f;
    private static final float BASE_H = 1080f;

    // ═══════════════════════════════════════════════════════════════════════
    // OVERLAY IN-GAME (R13): decoración solo en bordes, sin invadir centro
    // ═══════════════════════════════════════════════════════════════════════
    public static void renderInGame(GuiGraphics g, int w, int h) {
        float scale = getScale(w, h);
        renderVignette(g, w, h, 50);
        renderThinBorder(g, w, h);
        renderCornerBrackets(g, w, h, scale);
        // NO sellos in-game (interfieren con HUD/hotbar)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OVERLAY FRONT-END: más presencia, sin oscurecer excesivamente
    // ═══════════════════════════════════════════════════════════════════════
    public static void renderFrontEnd(GuiGraphics g, int w, int h) {
        float scale = getScale(w, h);
        // Tinte sepia sutil (era 130, ahora 60)
        g.fill(0, 0, w, h, DiaryTheme.withAlpha(0x1A0F08, 60));
        renderVignette(g, w, h, 80);
        renderThinBorder(g, w, h);
        renderCornerBrackets(g, w, h, scale);
        // Sellos SOLO en esquinas superiores (evitar botones inferiores)
        drawSmallSeal(g, sc(24, scale), sc(24, scale), scale);
        drawSmallSeal(g, w - sc(24, scale), sc(24, scale), scale);
    }

    // ── Utilidades de escalado ──────────────────────────────────────────────
    private static float getScale(int w, int h) {
        // Método 3: escala uniforme basada en el mínimo
        // Cap entre 0.5f y 1.5f para evitar sellos gigantes en 4K
        float s = Math.min(w / BASE_W, h / BASE_H);
        return Math.max(0.5f, Math.min(1.5f, s));
    }

    private static int sc(int value, float scale) {
        return Math.max(1, Math.round(value * scale));
    }

    // ── Viñeta radial suave ─────────────────────────────────────────────────
    private static void renderVignette(GuiGraphics g, int w, int h, int maxAlpha) {
        int margin = (int)(Math.min(w, h) * 0.15f);
        for (int i = 0; i < margin; i++) {
            float t = 1f - (float) i / margin;
            int a = (int)(t * t * maxAlpha);
            int c = DiaryTheme.withAlpha(0x000000, a);
            g.fill(i, 0, i + 1, h, c);
            g.fill(w - i - 1, 0, w - i, h, c);
            g.fill(0, i, w, i + 1, c);
            g.fill(0, h - i - 1, w, h - i, c);
        }
    }

    // ── Marco fino perimetral (no invasivo) ─────────────────────────────────
    private static void renderThinBorder(GuiGraphics g, int w, int h) {
        int outer = DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 160);
        int gold  = DiaryTheme.withAlpha(DiaryTheme.GOLD_TARNISHED, 130);

        // Línea externa (2px)
        g.fill(0, 0, w, 2, outer);
        g.fill(0, h - 2, w, h, outer);
        g.fill(0, 0, 2, h, outer);
        g.fill(w - 2, 0, w, h, outer);

        // Línea dorada interior (1px)
        g.fill(4, 4, w - 4, 5, gold);
        g.fill(4, h - 5, w - 4, h - 4, gold);
        g.fill(4, 4, 5, h - 4, gold);
        g.fill(w - 5, 4, w - 4, h - 4, gold);
    }

    // ── Escuadras ornamentales en las 4 esquinas ────────────────────────────
    private static void renderCornerBrackets(GuiGraphics g, int w, int h, float scale) {
        int size = sc(28, scale);
        drawBracket(g, 0, 0, size, 1, 1);
        drawBracket(g, w, 0, size, -1, 1);
        drawBracket(g, 0, h, size, 1, -1);
        drawBracket(g, w, h, size, -1, -1);
    }

    private static void drawBracket(GuiGraphics g, int x, int y, int size, int dx, int dy) {
        int gold = DiaryTheme.withAlpha(DiaryTheme.GOLD_TARNISHED, 220);
        int bronze = DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 240);

        // Placa base pequeña
        int p = size / 3;
        g.fill(x, y, x + p * dx, y + p * dy, bronze);

        // Líneas en L doradas
        g.fill(x, y, x + size * dx, y + 1 * dy, gold);
        g.fill(x, y, x + 1 * dx, y + size * dy, gold);
        g.fill(x, y + 2 * dy, x + (size - 2) * dx, y + 3 * dy, gold);
        g.fill(x + 2 * dx, y, x + 3 * dx, y + (size - 2) * dy, gold);

        // Remache central
        int cx = x + (p / 2) * dx;
        int cy = y + (p / 2) * dy;
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, gold);
    }

    // ── Sello de cera compacto (2/3 del tamaño anterior) ────────────────────
    private static void drawSmallSeal(GuiGraphics g, int cx, int cy, float scale) {
        int r = sc(6, scale);
        int red = DiaryTheme.WAX_SEAL;
        int redDark = DiaryTheme.BLOOD_DRIED;

        // Círculo simple
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt(r * r - dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, red);
        }
        // Cruz en el centro (marca de sello)
        g.fill(cx - r + 2, cy - 1, cx + r - 1, cy + 1, redDark);
        g.fill(cx - 1, cy - r + 2, cx + 1, cy + r - 1, redDark);
    }
}