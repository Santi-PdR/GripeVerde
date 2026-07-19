package com.eternalcraft.gripeverde.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class AtmosphereRenderer {

    private static float time = 0f;
    private static final float[] CLOUD_OFFSETS = {0f, 0f, 0f, 0f, 0f, 0f, 0f};
    private static final float[] CLOUD_SPEEDS  = {0.008f, 0.005f, 0.012f, 0.007f, 0.009f, 0.003f, 0.004f};
    private static float moonGlowPulse = 0f;

    public static void tick(float deltaTime) {
        time += deltaTime;
        moonGlowPulse = (float)(Math.sin(time * 0.4f) * 0.5f + 0.5f);
        for (int i = 0; i < CLOUD_OFFSETS.length; i++) {
            CLOUD_OFFSETS[i] += CLOUD_SPEEDS[i] * deltaTime * 60f;
            if (CLOUD_OFFSETS[i] > 400f) CLOUD_OFFSETS[i] = -300f;
        }
    }

    public static void renderSky(GuiGraphics g, int w, int h) {
        // Guard: cuando la ventana pierde foco o se minimiza (Alt+Tab, F11),
        // w o h pueden ser 0. Random.nextInt(0) lanza IllegalArgumentException
        // y crashea el juego.
        if (w <= 0 || h <= 0) return;
        renderSkyGradient(g, w, h);
        renderStars(g, w, h);
        renderMoon(g, w, h);
        renderClouds(g, w, h);
    }

    private static void renderSkyGradient(GuiGraphics g, int w, int h) {
        int stop1 = (int)(h * 0.30f);
        int stop2 = (int)(h * 0.65f);
        fillGradientV(g, 0, 0,     w, stop1, DiaryTheme.NIGHT_TOP,   DiaryTheme.NIGHT_UPPER);
        fillGradientV(g, 0, stop1, w, stop2, DiaryTheme.NIGHT_UPPER, DiaryTheme.NIGHT_MID);
        fillGradientV(g, 0, stop2, w, h,     DiaryTheme.NIGHT_MID,   DiaryTheme.NIGHT_HORIZON);
    }

    private static void renderStars(GuiGraphics g, int w, int h) {
        if (w <= 0 || h <= 0) return;
        int halfH = Math.max(1, (int)(h * 0.5f));
        java.util.Random rng = new java.util.Random(0xA57C5L);
        for (int i = 0; i < 60; i++) {
            int sx = rng.nextInt(w);
            int sy = rng.nextInt(halfH);
            float twinkle = (float)(Math.sin(time * (0.5f + rng.nextFloat()) + i) * 0.4f + 0.6f);
            int alpha = (int)(twinkle * 120);
            int color = (alpha << 24) | 0xE8DCB4;
            g.fill(sx, sy, sx + 1, sy + 1, color);
        }
    }

    private static void renderMoon(GuiGraphics g, int w, int h) {
        int cx = (int)(w * 0.75f);
        int cy = (int)(h * 0.18f);
        int radius = (int)(w * 0.025f);

        int glowAlpha = (int)(30 + moonGlowPulse * 20);
        fillCircle(g, cx, cy, radius + 18, withAlpha(0x00D4941E, glowAlpha + 5));
        fillCircle(g, cx, cy, radius + 12, withAlpha(0x00B8863D, glowAlpha + 10));
        fillCircle(g, cx, cy, radius + 7,  withAlpha(0x00E8DCB4, glowAlpha + 20));
        fillCircle(g, cx, cy, radius,       DiaryTheme.MOON_WHITE);

        // Manchas de crateres (dan textura de luna real)
        g.fill(cx - 3, cy - 2, cx - 1, cy,     0xFFB8AB88);
        g.fill(cx + 1, cy + 1, cx + 3, cy + 3, 0xFFB8AB88);
        g.fill(cx - 1, cy + 2, cx,     cy + 3, 0xFFB8AB88);

        // Nube que la tapa parcialmente (blob suave)
        float cloudCover = (float)(Math.sin(time * 0.15f) * 0.5f + 0.5f);
        int cloudAlpha = (int)(cloudCover * 140);
        drawSoftBlob(g, cx - 2, cy + 1, radius + 6, cloudAlpha, 0x001A0F08);
    }

    private static void renderClouds(GuiGraphics g, int w, int h) {
        float[][] clouds = {
            {0.08f, 0.06f, 0.0f,  0.45f, 0.7f},
            {0.15f, 0.05f, 0.3f,  0.55f, 0.6f},
            {0.05f, 0.04f, 0.55f, 0.40f, 0.5f},
            {0.22f, 0.07f, 0.1f,  0.50f, 0.65f},
            {0.12f, 0.05f, 0.7f,  0.35f, 0.55f},
        };
        int cloudColor = 0x001A0F08;
        for (int i = 0; i < clouds.length; i++) {
            int cy    = (int)(h * (clouds[i][0] + clouds[i][1] * 0.5f));
            int xBase = (int)(w * clouds[i][2]) + (int)(CLOUD_OFFSETS[i]) - 100;
            int cw    = (int)(w * clouds[i][3]);
            int ch    = (int)(h * clouds[i][1]);
            int radius = Math.max(8, ch);
            float pulse = (float)(Math.sin(time * 0.3f + i * 1.3f) * 0.1f + clouds[i][4]);
            int alpha = Math.max(0, Math.min(255, (int)(pulse * 180)));
            int blobCount = 2;
            int cappedRadius = Math.min(radius, 20);
            for (int b = 0; b < blobCount; b++) {
                float bt = (float) b / (float)(blobCount - 1);
                int bx = xBase + (int)(cw * bt);
                int br = cappedRadius + (int)(Math.sin(bt * 3.14159f) * cappedRadius * 0.3f);
                drawSoftBlob(g, bx, cy, br, alpha, cloudColor);
            }
        }
    }

    public static void fillGradientV(GuiGraphics g, int x1, int y1, int x2, int y2,
                                      int colorTop, int colorBottom) {
        Matrix4f matrix = g.pose().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float aT = ((colorTop    >> 24) & 0xFF) / 255f;
        float rT = ((colorTop    >> 16) & 0xFF) / 255f;
        float gT = ((colorTop    >>  8) & 0xFF) / 255f;
        float bT = ( colorTop          & 0xFF)  / 255f;
        float aB = ((colorBottom >> 24) & 0xFF) / 255f;
        float rB = ((colorBottom >> 16) & 0xFF) / 255f;
        float gB = ((colorBottom >>  8) & 0xFF) / 255f;
        float bB = ( colorBottom       & 0xFF)  / 255f;
        buf.vertex(matrix, x1, y2, 0).color(rB, gB, bB, aB).endVertex();
        buf.vertex(matrix, x2, y2, 0).color(rB, gB, bB, aB).endVertex();
        buf.vertex(matrix, x2, y1, 0).color(rT, gT, bT, aT).endVertex();
        buf.vertex(matrix, x1, y1, 0).color(rT, gT, bT, aT).endVertex();
        Tesselator.getInstance().end();
        RenderSystem.disableBlend();
    }

    /** Blob suave: 6 anillos concentricos con alpha decreciente (optimizado). */
    public static void drawSoftBlob(GuiGraphics g, int cx, int cy, int radius, int coreAlpha, int colorNoAlpha) {
        if (radius <= 0 || coreAlpha <= 0) return;
        int steps = radius < 6 ? radius : 6;
        for (int s = steps; s >= 1; s--) {
            int r = Math.max(1, radius * s / steps);
            float t = (float) s / (float) steps;
            int a = (int) (coreAlpha * (1f - t) * (1f - t) + coreAlpha * 0.15f * (1f - t));
            if (a <= 0) continue;
            fillCircle(g, cx, cy, r, withAlpha(colorNoAlpha, a));
        }
    }

    private static void fillCircle(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt((double)(r * r - dy * dy));
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    public static int withAlpha(int colorNoAlpha, int alpha) {
        return (alpha << 24) | (colorNoAlpha & 0x00FFFFFF);
    }
}