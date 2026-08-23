package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Centralized scene renderer - Single source of truth for Title and all Diary screens.
 * Professional overhaul:
 *  - LOD aware: reduces effects on small heights / high GUI scale
 *  - Eliminates duplicated pipeline code across 14 screens
 *  - Provides consistent overlay darkness and film grain
 */
@OnlyIn(Dist.CLIENT)
public final class QuarantineSceneRenderer {

    private QuarantineSceneRenderer() {}

    private static boolean reducedMotion() {
        try {
            return com.eternalcraft.gripeverde.config.ClientConfig.REDUCED_MOTION.get();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    // ── Tick ──
    public static void tick(float delta) {
        AtmosphereRenderer.tick(delta);
        VictorianSkyline.tick(delta);
        LivingAtmosphere.tick(delta);
        tickDust(delta);
        try {
            com.eternalcraft.gripeverde.client.audio.MenuMusic.tick();
            com.eternalcraft.gripeverde.client.audio.MenuAmbience.tick();
        } catch (Throwable ignored) {}
    }

    // ── Full scene for Title ──
    public static void renderTitleScene(GuiGraphics g, int w, int h) {
        if (w <= 0 || h <= 0) return;
        com.eternalcraft.gripeverde.client.audio.MenuAmbience.setPocketWatchVisible(h >= 290);

        AtmosphereRenderer.renderSky(g, w, h);
        VictorianSkyline.renderBackground(g, w, h);
        LivingAtmosphere.renderFogLow(g, w, h);
        VictorianSkyline.renderForeground(g, w, h);
        LivingAtmosphere.renderStreetLevel(g, w, h);
        LivingAtmosphere.renderAir(g, w, h);
        LivingAtmosphere.renderFlash(g, w, h);
        LivingAtmosphere.renderVignette(g, w, h);
        renderFilmGrain(g, w, h);
        renderAtmosphericDust(g, w, h);
        renderDistantFactoryGlow(g, w, h);
    }

    // ── Light scene for Diary screens (parchment overlay, street only, no fog/air heavy) ──
    public static void renderDiaryBackground(GuiGraphics g, int w, int h) {
        if (w <= 0 || h <= 0) return;
        g.fill(0, 0, w, h, 0xFF060401);
        AtmosphereRenderer.renderSky(g, w, h);
        VictorianSkyline.renderBackground(g, w, h);
        VictorianSkyline.renderForeground(g, w, h);
        LivingAtmosphere.renderStreet(g, w, h);
        LivingAtmosphere.renderStormVeil(g, w, h);
        LivingAtmosphere.renderVignette(g, w, h);
        // Overlay oscuro del gabinete
        g.fill(0, 0, w, h, DiaryChrome.OVERLAY_COL);
    }

    // ── Film grain subtle to add depth without texture ──
    private static final java.util.Random GRAIN_RNG = new java.util.Random(0x46494C4C);
    private static long lastGrainMs = 0L;
    private static int[] grainX = new int[48];
    private static int[] grainY = new int[48];
    private static int[] grainCol = new int[48];
    private static int grainW = -1;
    private static int grainH = -1;

    private static void renderFilmGrain(GuiGraphics g, int w, int h) {
        long now = System.currentTimeMillis();
        if (lastGrainMs == 0L || w != grainW || h != grainH
                || (!reducedMotion() && now - lastGrainMs > 90L)) {
            lastGrainMs = now;
            grainW = w;
            grainH = h;
            for (int i = 0; i < grainX.length; i++) {
                grainX[i] = GRAIN_RNG.nextInt(Math.max(1, w));
                grainY[i] = GRAIN_RNG.nextInt(Math.max(1, h));
                int v = GRAIN_RNG.nextInt(18) + 8;
                int alpha = GRAIN_RNG.nextInt(10) + 3;
                grainCol[i] = (alpha << 24) | (v << 16) | (v << 8) | v;
            }
        }
        for (int i = 0; i < grainX.length; i++) {
            int x = grainX[i];
            int y = grainY[i];
            if (x < w && y < h) {
                g.fill(x, y, x + 1, y + 1, grainCol[i]);
            }
        }
    }

    // ── Microdust floating in amber light (title only) ──
    private static final int DUST_COUNT = 18;
    // v0.21.3: RNG compartido con seed fija (sin Math.random en el loop caliente).
    private static final java.util.Random RND = new java.util.Random(0xD05E);
    private static final float[][] dust = new float[DUST_COUNT][4];

    static {
        for (int i = 0; i < DUST_COUNT; i++) {
            dust[i][0] = RND.nextFloat();
            dust[i][1] = RND.nextFloat();
            dust[i][2] = 0.0006f + RND.nextFloat() * 0.0012f;
            dust[i][3] = RND.nextFloat() * 6.28f;
        }
    }

    private static void tickDust(float delta) {
        if (reducedMotion()) return;

        for (int i = 0; i < DUST_COUNT; i++) {
            dust[i][1] -= dust[i][2] * delta * 60f;
            dust[i][3] += delta * 0.7f;
            if (dust[i][1] < -0.05f) {
                dust[i][0] = RND.nextFloat();
                dust[i][1] = 1.05f;
            }
        }
    }

    private static void renderAtmosphericDust(GuiGraphics g, int w, int h){
        if (h<320) return;
        for(int i=0;i<DUST_COUNT;i++){
            int dx = (int)(dust[i][0]*w);
            int dy = (int)(dust[i][1]*h*0.55f);
            int alpha = 18 + (int)(Math.sin(dust[i][3])*10+10);
            int col = (alpha<<24) | 0xC9B78A;
            int sz = (i%3==0)?2:1;
            g.fill(dx,dy,dx+sz,dy+sz,col);
        }
    }

    private static void renderDistantFactoryGlow(GuiGraphics g, int w, int h){
        // Subtle amber glow on horizon, breathing 0.22Hz
        float breath = reducedMotion()
            ? 0.85f
            : (float)(Math.sin(System.currentTimeMillis()/1000.0*0.22*6.28)*0.15+0.85);
        int alpha = (int)(16*breath);
        int top = (int)(h*0.60f);
        int bot = top + Math.max(10, h/18);
        g.fill(0, top, w, bot, AtmosphereRenderer.withAlpha(0x004A2E18, alpha));
        // Second lower band faint
        g.fill(0, bot, w, bot+Math.max(4,h/40), AtmosphereRenderer.withAlpha(0x002A1810, alpha/2));
    }
}
