package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LivingAtmosphere {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    private static final boolean DEBUG_EVENTS = false;

    private static float time = 0f;
    private static int[][] lanternPositions = null;
    private static int lastLanternW = 0, lastLanternH = 0;

    private static final float[] FOG_OFFSETS = {0f, 0f, 0f};
    private static final float[] FOG_SPEEDS  = {0.3f, 0.2f, 0.25f};

    private static final int SPORE_COUNT = 15;
    private static final float[][] spores = new float[SPORE_COUNT][4];
    private static boolean sporesInit = false;

    private static final int CROW_COUNT = 3;
    private static final float[][] crows = new float[CROW_COUNT][5];
    private static boolean crowsInit = false;

    private static final int RAIN_COUNT = 40;
    private static final float[][] rain = new float[RAIN_COUNT][3];
    private static boolean rainInit = false;

    private static final int SPLASH_COUNT = 14;
    private static final float[][] splashes = new float[SPLASH_COUNT][3];
    private static boolean splashesInit = false;

    private static final int MOTH_COUNT = 9;
    private static final float[][] moths = new float[MOTH_COUNT][5];
    private static boolean mothsInit = false;

    private static final int RIPPLE_COUNT = 7;
    private static final float[][] ripples = new float[RIPPLE_COUNT][4];
    private static boolean ripplesInit = false;

    private static final int PAPER_SCRAP_COUNT = 5;
    private static final float[][] paperScraps = new float[PAPER_SCRAP_COUNT][5];
    private static boolean paperScrapsInit = false;

    private static float windGustTimer = 0f;
    private static float windGustNext = 22f;
    private static float windGustDuration = 0f;
    private static float windGustIntensity = 0f;
    private static float branchSway = 0f;

    private static float lightningTimer = 20f;
    private static float lightningFlash = 0f;
    private static float nextLightning  = 12f;

    private static float vignettePulse = 0f;

    // Street rat: sprite 8x4 crossing horizontally every 40-80s
    private static float ratX = -0.2f;       // 0..1 normalized screen X
    private static float ratSpeed = 0f;       // per-second, negative = right-to-left
    private static float ratTimer = 10f;
    private static float ratNextEvent = 30f;
    private static boolean ratActive = false;

    // Lightning silhouette flag: durante flash inicial, dibujar silueta
    // (usa lightningFlash como fuente)


    // Erratic lantern: one specific lantern flickers wildly every 30-60s
    private static final int ERRATIC_LANTERN_INDEX = 2;
    private static float erraticTimer = 20f;
    private static float erraticNextEvent = 30f;
    private static float erraticIntensity = 0f;  // 0=normal, 1=full erratic
    private static float erraticDuration = 0f;

    public static void triggerLightningFlash(float intensity) {
        lightningTimer = 0f;
        lightningFlash = Math.max(lightningFlash, Math.max(0.15f, Math.min(1.0f, intensity)));
        nextLightning = 8f + (float)(Math.random() * 12f);
    }

    public static void tick(float deltaTime) {
        time += deltaTime;
        initAll();

        for (int i = 0; i < FOG_OFFSETS.length; i++) {
            FOG_OFFSETS[i] += FOG_SPEEDS[i] * deltaTime * 60f;
            if (FOG_OFFSETS[i] > 300f) FOG_OFFSETS[i] = -200f;
        }
        for (int i = 0; i < SPORE_COUNT; i++) {
            spores[i][1] -= spores[i][2] * deltaTime * 60f;
            if (spores[i][1] < -0.05f) {
                spores[i][0] = (float) Math.random();
                spores[i][1] = 1.05f;
                spores[i][2] = 0.001f + (float)(Math.random() * 0.003f);
                spores[i][3] = 1f + (float)(Math.random() * 2f);
            }
        }
        // Boost de velocidad si acaban de sonar los cuervos (coordinacion visual/audio)
        float crowSpeedMult = com.eternalcraft.gripeverde.client.audio.MenuAmbience.isCrowsRecentlyTriggered() ? 3.5f : 1f;
        float crowWingMult = com.eternalcraft.gripeverde.client.audio.MenuAmbience.isCrowsRecentlyTriggered() ? 2.5f : 1f;
        for (int i = 0; i < CROW_COUNT; i++) {
            crows[i][0] += crows[i][2] * deltaTime * 60f * crowSpeedMult;
            crows[i][3] += deltaTime * 8f * crowWingMult;
            if (crows[i][0] > 1.1f) {
                crows[i][0] = -0.1f;
                crows[i][1] = 0.1f + (float)(Math.random() * 0.3f);
                crows[i][2] = 0.001f + (float)(Math.random() * 0.002f);
                crows[i][3] = 0f;
            }
        }
        for (int i = 0; i < RAIN_COUNT; i++) {
            rain[i][1] += rain[i][2] * deltaTime * 60f;
            if (rain[i][1] > 1.1f) {
                rain[i][0] = (float) Math.random();
                rain[i][1] = -0.05f;
            }
        }
        for (int i = 0; i < SPLASH_COUNT; i++) {
            splashes[i][2] += deltaTime * (0.9f + i * 0.03f);
            if (splashes[i][2] > 1f) {
                splashes[i][0] = (float) Math.random();
                splashes[i][1] = 0.84f + (float)(Math.random() * 0.12f);
                splashes[i][2] = 0f;
            }
        }
        for (int i = 0; i < MOTH_COUNT; i++) {
            moths[i][1] += moths[i][3] * deltaTime;
            if (moths[i][1] > Math.PI * 2f) moths[i][1] -= (float)(Math.PI * 2f);
        }
        for (int i = 0; i < RIPPLE_COUNT; i++) {
            ripples[i][2] += deltaTime * ripples[i][3];
            if (ripples[i][2] > 1f) {
                resetRipple(i);
            }
        }
        for (int i = 0; i < PAPER_SCRAP_COUNT; i++) {
            paperScraps[i][0] += (paperScraps[i][2] + windGustIntensity * 0.018f) * deltaTime;
            paperScraps[i][3] += deltaTime * (5f + windGustIntensity * 6f);
            if (paperScraps[i][0] < -0.08f || paperScraps[i][0] > 1.08f) {
                resetPaperScrap(i);
            }
        }
        windGustTimer += deltaTime;
        if (windGustDuration > 0f) {
            windGustDuration -= deltaTime;
            windGustIntensity = Math.max(0f, windGustDuration / 3.2f);
        } else if (windGustTimer >= windGustNext) {
            windGustTimer = 0f;
            windGustDuration = 2.4f + (float)(Math.random() * 1.8f);
            windGustNext = 18f + (float)(Math.random() * 28f);
            windGustIntensity = 1f;
        }
        branchSway += deltaTime * (0.55f + windGustIntensity * 0.8f);
        lightningTimer += deltaTime;
        if (lightningFlash > 0f) {
            lightningFlash -= deltaTime * 3f;
            lightningFlash  = Math.max(0f, lightningFlash);
        }
        if (lightningTimer >= nextLightning) {
            lightningTimer = 0f;
            lightningFlash = 1f;
            nextLightning  = 8f + (float)(Math.random() * 12f);
            if (DEBUG_EVENTS) LOGGER.info("[GripeVerde] Relampago DISPARADO, proximo en {}s", nextLightning);
        }
        vignettePulse = (float)(Math.sin(time * 0.5f) * 0.5f + 0.5f);

        // Erratic lantern timer
        erraticTimer += deltaTime;
        if (erraticIntensity > 0f) {
            erraticDuration -= deltaTime;
            if (erraticDuration <= 0f) {
                erraticIntensity = 0f;
                erraticNextEvent = 30f + (float)(Math.random() * 30.0);
                erraticTimer = 0f;
            }
        } else if (erraticTimer >= erraticNextEvent) {
            erraticIntensity = 1f;
            erraticDuration = 3f + (float)(Math.random() * 2.0);
        }

        // Street rat: crossing logic
        if (ratActive) {
            ratX += ratSpeed * deltaTime;
            if (ratX < -0.1f || ratX > 1.1f) {
                ratActive = false;
                ratTimer = 0f;
                ratNextEvent = 40f + (float)(Math.random() * 40.0);
            }
        } else {
            ratTimer += deltaTime;
            if (ratTimer >= ratNextEvent) {
                ratActive = true;
                if (Math.random() < 0.5) {
                    ratX = -0.05f;
                    ratSpeed = 0.15f + (float)(Math.random() * 0.10);
                } else {
                    ratX = 1.05f;
                    ratSpeed = -(0.15f + (float)(Math.random() * 0.10));
                }
            }
        }

    }

    private static void initAll() {
        if (!sporesInit) {
            sporesInit = true;
            for (int i = 0; i < SPORE_COUNT; i++) {
                spores[i][0] = (float) Math.random();
                spores[i][1] = (float) Math.random();
                spores[i][2] = 0.001f + (float)(Math.random() * 0.003f);
                spores[i][3] = 1f + (float)(Math.random() * 2f);
            }
        }
        if (!crowsInit) {
            crowsInit = true;
            for (int i = 0; i < CROW_COUNT; i++) {
                crows[i][0] = (float) Math.random();
                crows[i][1] = 0.1f + (float)(Math.random() * 0.3f);
                crows[i][2] = 0.001f + (float)(Math.random() * 0.002f);
                crows[i][3] = (float)(Math.random() * Math.PI * 2);
                crows[i][4] = 0.6f + (float)(Math.random() * 0.8f);
            }
        }
        if (!rainInit) {
            rainInit = true;
            for (int i = 0; i < RAIN_COUNT; i++) {
                rain[i][0] = (float) Math.random();
                rain[i][1] = (float) Math.random();
                rain[i][2] = 0.008f + (float)(Math.random() * 0.006f);
            }
        }
        if (!splashesInit) {
            splashesInit = true;
            for (int i = 0; i < SPLASH_COUNT; i++) {
                splashes[i][0] = (float) Math.random();
                splashes[i][1] = 0.84f + (float)(Math.random() * 0.12f);
                splashes[i][2] = (float) Math.random();
            }
        }
        if (!mothsInit) {
            mothsInit = true;
            for (int i = 0; i < MOTH_COUNT; i++) {
                moths[i][0] = i;
                moths[i][1] = (float)(Math.random() * Math.PI * 2.0);
                moths[i][2] = 5f + (float)(Math.random() * 8f);
                moths[i][3] = 0.7f + (float)(Math.random() * 1.3f);
                moths[i][4] = (float)Math.random();
            }
        }
        if (!ripplesInit) {
            ripplesInit = true;
            for (int i = 0; i < RIPPLE_COUNT; i++) resetRipple(i);
        }
        if (!paperScrapsInit) {
            paperScrapsInit = true;
            for (int i = 0; i < PAPER_SCRAP_COUNT; i++) resetPaperScrap(i);
        }
    }

    private static void resetRipple(int i) {
        ripples[i][0] = (float)Math.random();
        ripples[i][1] = 0.84f + (float)(Math.random() * 0.12f);
        ripples[i][2] = (float)(Math.random() * 0.15f);
        ripples[i][3] = 0.35f + (float)(Math.random() * 0.45f);
    }

    private static void resetPaperScrap(int i) {
        boolean leftToRight = Math.random() < 0.5;
        paperScraps[i][0] = leftToRight ? -0.05f : 1.05f;
        paperScraps[i][1] = 0.84f + (float)(Math.random() * 0.12f);
        paperScraps[i][2] = (leftToRight ? 1f : -1f) * (0.035f + (float)(Math.random() * 0.05f));
        paperScraps[i][3] = (float)(Math.random() * Math.PI * 2.0);
        paperScraps[i][4] = 0.5f + (float)Math.random();
    }

    public static void renderFogLow(GuiGraphics g, int w, int h) {
        // Niebla OCRE (no verde) que RESPIRA a 0.3Hz
        float[][] bands = {
            {0.68f, 0.06f, 0.45f},
            {0.72f, 0.05f, 0.60f},
            {0.76f, 0.04f, 0.35f},
        };
        // Respiracion lenta global: 0.3Hz = periodo ~3.3s
        // Amplitud +-25% del alpha base
        float breath = (float)(Math.sin(time * 0.3f * 2.0 * Math.PI) * 0.25f + 1f);
        for (int i = 0; i < bands.length; i++) {
            int y1 = (int)(h * bands[i][0]);
            int y2 = (int)(h * (bands[i][0] + bands[i][1]));
            int offset = (int)(FOG_OFFSETS[i % FOG_OFFSETS.length]);
            // Pulse local (variacion rapida) + breath global (respiracion lenta)
            float pulse = (float)(Math.sin(time * 0.4f + i * 1.2f) * 0.1f + bands[i][2]);
            float finalPulse = pulse * breath;
            int alpha = Math.max(0, Math.min(255, (int)(finalPulse * 160)));
            // Niebla marron-ocre (miasma victoriano)
            int fogColor = AtmosphereRenderer.withAlpha(0x004A3820, alpha);
            int startX = -100 + (offset % w) - w;
            int bandW = (int)(w * 1.5f);
            g.fill(startX,         y1, startX + bandW,     y2, fogColor);
            g.fill(startX + bandW, y1, startX + bandW * 2, y2, fogColor);
        }
    }

    public static void renderStreetLevel(GuiGraphics g, int w, int h) {
        if (lanternPositions == null || w != lastLanternW || h != lastLanternH) {
            lanternPositions = VictorianSkyline.getLanternPositions(w, h);
            lastLanternW = w; lastLanternH = h;
        }
        renderStreet(g, w, h);
        renderPuddleRipples(g, w, h);
        renderLanternReflections(g, w, h);
        renderPaperScraps(g, w, h);
        renderGroundFog(g, w, h);
        for (int i = 0; i < lanternPositions.length; i++) {
            renderGasLantern(g, lanternPositions[i][0], lanternPositions[i][1], i, w, h);
        }
        renderMoths(g, w, h);
        renderSpores(g, w, h);
        renderRat(g, w, h);
    }

    public static void renderAir(GuiGraphics g, int w, int h) {
        renderBranchShadows(g, w, h);
        renderRain(g, w, h);
        renderCrows(g, w, h);
    }

    public static void renderFlash(GuiGraphics g, int w, int h) {
        if (lightningFlash <= 0f) return;
        int alpha = (int)(lightningFlash * 50);
        // Flash calido (no verde)
        g.fill(0, 0, w, h, AtmosphereRenderer.withAlpha(0x00FFEECC, alpha));
        // Silueta de chimeneas en el pico del flash
        renderLightningSilhouette(g, w, h);
        renderLightningForks(g, w, h);
    }

    public static void renderVignette(GuiGraphics g, int w, int h) {
        // Real system hour: if between 20-06, add extra dark overlay
        int realHour = java.time.LocalTime.now().getHour();
        boolean isNightIRL = realHour >= 20 || realHour < 6;
        if (isNightIRL) {
            // Overlay sepia muy oscuro sobre todo
            int nightOverlayAlpha = 40;
            g.fill(0, 0, w, h,
                AtmosphereRenderer.withAlpha(0x00000C08, nightOverlayAlpha));
        }

        int vigAlpha = Math.min(255, (int)(200 + vignettePulse * 30));
        int margin = (int)(Math.min(w, h) * 0.20f);  // reducido para performance
        // Step de 2 pixeles para reducir fills a la mitad (imperceptible visualmente)
        for (int x = 0; x < margin; x += 2) {
            float t = 1f - (float) x / margin;
            int a = (int)(t * t * vigAlpha);
            g.fill(x,         0, x + 2, h, AtmosphereRenderer.withAlpha(0x00000000, a));
            g.fill(w - x - 2, 0, w - x, h, AtmosphereRenderer.withAlpha(0x00000000, a));
        }
        for (int y = 0; y < margin; y += 2) {
            float t = 1f - (float) y / margin;
            int a = (int)(t * t * vigAlpha);
            g.fill(0, y,         w, y + 2, AtmosphereRenderer.withAlpha(0x00000000, a));
            g.fill(0, h - y - 2, w, h - y, AtmosphereRenderer.withAlpha(0x00000000, a));
        }
    }

    public static void renderStreet(GuiGraphics g, int w, int h) {
        int streetTop;
        int rowCount;
        int topCols;
        int bottomCols;
        int curbHighlightH;
        int curbShadowH;
        int sheenAlpha;
        int seamAlpha;

        if (h >= 800) {
            streetTop = (int)(h * 0.839f);
            rowCount = 6;
            topCols = 24;
            bottomCols = 11;
            curbHighlightH = 2;
            curbShadowH = 7;
            sheenAlpha = 38;
            seamAlpha = 74;
        } else if (h >= 400) {
            streetTop = (int)(h * 0.841f);
            rowCount = 5;
            topCols = 20;
            bottomCols = 10;
            curbHighlightH = 2;
            curbShadowH = 5;
            sheenAlpha = 34;
            seamAlpha = 68;
        } else if (h >= 260) {
            streetTop = (int)(h * 0.844f);
            rowCount = 4;
            topCols = 16;
            bottomCols = 9;
            curbHighlightH = 1;
            curbShadowH = 4;
            sheenAlpha = 30;
            seamAlpha = 62;
        } else {
            streetTop = (int)(h * 0.846f);
            rowCount = 3;
            topCols = 12;
            bottomCols = 8;
            curbHighlightH = 1;
            curbShadowH = 3;
            sheenAlpha = 26;
            seamAlpha = 56;
        }

        int streetBottom = h;
        int streetH = streetBottom - streetTop;
        if (streetH <= 0) return;

        // Masa principal de calle mojada
        g.fill(0, streetTop, w, streetBottom, 0xFF25180F);

        // Acera lejana / borde de encuentro con el skyline
        int curbLightEnd = Math.min(streetBottom, streetTop + curbHighlightH);
        int curbShadowEnd = Math.min(streetBottom, curbLightEnd + curbShadowH);
        g.fill(0, streetTop, w, curbLightEnd, 0xFF725739);
        g.fill(0, curbLightEnd, w, curbShadowEnd, 0xFF433020);

        // Banda superior para que la calle no lea como bloque plano
        int upperBandTop = streetTop + Math.max(2, streetH / 8);
        int upperBandBot = Math.min(streetBottom, upperBandTop + Math.max(2, streetH / 12));
        g.fill(0, upperBandTop, w, upperBandBot,
            AtmosphereRenderer.withAlpha(0x00523A25, 96));

        // Filas de adoquines en perspectiva
        for (int row = 0; row < rowCount; row++) {
            int y1 = streetTop + row * streetH / rowCount;
            int y2 = streetTop + (row + 1) * streetH / rowCount;
            if (y2 <= y1) continue;

            if (row > 0) {
                g.fill(0, y1, w, y1 + 1, 0xFF342116);
            }

            int cols;
            if (rowCount <= 1) {
                cols = bottomCols;
            } else {
                cols = topCols - ((topCols - bottomCols) * row / (rowCount - 1));
            }
            cols = Math.max(6, cols);

            int blockW = Math.max(8, w / cols);
            int offset = (row % 2 == 0) ? 0 : blockW / 2;

            for (int x = -offset; x < w; x += blockW) {
                g.fill(x, y1 + 1, x + 1, y2,
                    AtmosphereRenderer.withAlpha(0x0024140B, seamAlpha));
            }
        }

        // Reflejo humedo central
        int sheenTop = streetTop + streetH / 3;
        int sheenBottom = Math.min(streetBottom, sheenTop + Math.max(2, streetH / 9));
        int sheenInset = Math.max(10, w / 9);
        g.fill(sheenInset, sheenTop, w - sheenInset, sheenBottom,
            AtmosphereRenderer.withAlpha(0x00927A55, sheenAlpha));

        // Cunetas laterales mas oscuras
        int gutterW = Math.max(4, w / 18);
        g.fill(0, curbLightEnd, gutterW, streetBottom,
            AtmosphereRenderer.withAlpha(0x00000000, 28));
        g.fill(w - gutterW, curbLightEnd, w, streetBottom,
            AtmosphereRenderer.withAlpha(0x00000000, 28));

        // Peso visual en la base para rematar el plano
        int bottomBandTop = streetBottom - Math.max(4, streetH / 7);
        g.fill(0, bottomBandTop, w, streetBottom,
            AtmosphereRenderer.withAlpha(0x00130C07, 52));
    }

    private static void renderPuddleRipples(GuiGraphics g, int w, int h) {
        if (h < 260) return;
        for (int i = 0; i < RIPPLE_COUNT; i++) {
            float age = ripples[i][2];
            int x = (int)(ripples[i][0] * w);
            int y = (int)(ripples[i][1] * h);
            int rx = 3 + (int)(age * 18f);
            int alpha = (int)((1f - age) * 34f);
            if (alpha <= 2) continue;
            int col = AtmosphereRenderer.withAlpha(0x00B8B0A0, alpha);
            g.fill(x - rx, y, x + rx, y + 1, col);
            g.fill(x - rx / 2, y - 1, x + rx / 2, y, AtmosphereRenderer.withAlpha(0x00E0D8C8, alpha / 2));
        }
    }

    private static void renderLanternReflections(GuiGraphics g, int w, int h) {
        if (lanternPositions == null) return;
        for (int i = 0; i < lanternPositions.length; i++) {
            int lx = lanternPositions[i][0];
            int ly = lanternPositions[i][1];
            float flicker = (float)(Math.sin(time * 3.2f + i * 1.5f) * 0.2f + 0.8f);
            int alpha = (int)(22 * flicker);
            int col = AtmosphereRenderer.withAlpha(0x00D49A3A, alpha);
            int top = Math.min(h, ly + 6);
            int bot = Math.min(h, ly + Math.max(12, h / 14));
            for (int r = 0; r < 4; r++) {
                int ww = 10 + r * 8;
                int yy = top + r * 5;
                if (yy > bot) break;
                g.fill(lx - ww, yy, lx + ww, yy + 1, col);
            }
        }
    }

    private static void renderPaperScraps(GuiGraphics g, int w, int h) {
        for (int i = 0; i < PAPER_SCRAP_COUNT; i++) {
            int x = (int)(paperScraps[i][0] * w);
            int baseY = (int)(paperScraps[i][1] * h);
            int y = baseY + (int)(Math.sin(paperScraps[i][3]) * 4f);
            int tone = (i & 1) == 0 ? 0x00B29A75 : 0x00836C4A;
            int alpha = (int)(90 * paperScraps[i][4]);
            int col = AtmosphereRenderer.withAlpha(tone, alpha);
            if (((int)(paperScraps[i][3] * 10) & 1) == 0) {
                g.fill(x, y, x + 5, y + 2, col);
            } else {
                g.fill(x, y, x + 3, y + 4, col);
            }
        }
    }

    private static void renderGroundFog(GuiGraphics g, int w, int h) {
        int fogY = (int)(h * 0.78f);
        for (int layer = 0; layer < 5; layer++) {
            int yBase = fogY + layer * (int)(h * 0.02f);
            float alphaF = 0.3f + layer * 0.1f;
            float pulse = (float)(Math.sin(time * 0.6f + layer * 0.8f) * 0.1f + alphaF);
            int alpha = Math.max(0, Math.min(255, (int)(pulse * 180)));
            int offset = (int)(FOG_OFFSETS[layer % FOG_OFFSETS.length] * 0.5f);
            // Niebla del suelo: sepia oscuro
            int fogColor = AtmosphereRenderer.withAlpha(0x003D2E1A, alpha);
            int startX = offset - w;
            g.fill(startX, yBase, startX + w * 3, yBase + (int)(h * 0.04f), fogColor);
        }
    }

    /**
     * Farol de gas victoriano: poste ornamental + cabezal 4 vidrios + llama danzante
     */
    private static void renderGasLantern(GuiGraphics g, int lx, int ly, int index, int w, int h) {
        int postH = (int)(h * 0.13f);

        // Base ornamental (mas ancha en la base)
        g.fill(lx - 3, ly - 2,     lx + 4, ly,        0xFF1A1108);
        g.fill(lx - 4, ly,         lx + 5, ly + 2,    0xFF0A0503);

        // Poste principal (mas grueso)
        g.fill(lx - 1, ly - postH, lx + 2, ly - 2,    0xFF2A1810);
        // Detalle central del poste (nudo ornamental)
        g.fill(lx - 2, ly - postH/2, lx + 3, ly - postH/2 + 3, 0xFF3D2E1A);

        // Brazo horizontal opcional (algunos faroles lo tienen)
        // g.fill(lx - 8, ly - postH + 2, lx - 2, ly - postH + 4, 0xFF2A1810);

        float flicker = (float)(
            Math.sin(time * 3.7f + index * 1.3f) * 0.25f +
            Math.sin(time * 7.1f + index * 2.7f) * 0.15f +
            0.85f
        );
        // Erratic override for one lantern: wild high-frequency oscillation
        if (index == ERRATIC_LANTERN_INDEX && erraticIntensity > 0f) {
            float chaos = (float)(
                Math.sin(time * 23f) * 0.3f +
                Math.sin(time * 37f + 1.7f) * 0.2f +
                Math.sin(time * 53f + 3.1f) * 0.15f
            );
            flicker = flicker + chaos * erraticIntensity;
        }
        flicker = Math.max(0.15f, Math.min(1.2f, flicker));

        int lanternY = ly - postH;

        // Sombrero superior (piramide)
        g.fill(lx - 6, lanternY - 8, lx + 7, lanternY - 6, 0xFF1A1108);
        g.fill(lx - 5, lanternY - 6, lx + 6, lanternY - 5, 0xFF2A1810);
        g.fill(lx - 4, lanternY - 5, lx + 5, lanternY - 4, 0xFF3D2E1A);

        // Caja del farol (4 vidrios)
        int boxSize = 5;
        // Marco exterior (bronce oscuro)
        g.fill(lx - boxSize - 1, lanternY - 4, lx + boxSize + 1, lanternY + boxSize + 1, 0xFF1F1408);
        // Vidrios (llama)
        int lightAlpha = (int)(flicker * 220);
        g.fill(lx - boxSize, lanternY - 3, lx + boxSize, lanternY + boxSize,
            AtmosphereRenderer.withAlpha(0x00FFAA33, lightAlpha));

        // Corazon de la llama (amarillo brillante)
        int coreAlpha = (int)(flicker * 255);
        g.fill(lx - 2, lanternY, lx + 3, lanternY + 3,
            AtmosphereRenderer.withAlpha(0x00FFEE88, coreAlpha));

        // Marcos verticales entre vidrios (cruz de la caja)
        g.fill(lx,     lanternY - 3, lx + 1, lanternY + boxSize, 0xFF2A1810);
        g.fill(lx - boxSize, lanternY + 1, lx + boxSize, lanternY + 2, 0xFF2A1810);

        // Base del cabezal
        g.fill(lx - boxSize - 1, lanternY + boxSize + 1, lx + boxSize + 1, lanternY + boxSize + 3, 0xFF1A1108);

        // Halo de luz en el suelo (ambar calido, no verde)
        int haloRadius = (int)(35 * flicker);
        for (int r = haloRadius; r > 0; r -= 3) {
            float t = (float) r / haloRadius;
            int haloA = (int)((1f - t) * flicker * 50);
            g.fill(lx - r, ly - r / 3, lx + r, ly + r / 5,
                AtmosphereRenderer.withAlpha(0x00FFAA33, haloA));
        }

        // Halo grande sobre la calle
        int floorHaloR = (int)(60 * flicker);
        for (int r = floorHaloR; r > 20; r -= 5) {
            float t = (float) r / floorHaloR;
            int haloA = (int)((1f - t) * flicker * 25);
            g.fill(lx - r, ly - 2, lx + r, ly + 8,
                AtmosphereRenderer.withAlpha(0x00D4941E, haloA));
        }
    }

    private static void renderBranchShadows(GuiGraphics g, int w, int h) {
        if (w < 320 || h < 240) return;
        int alpha = 34 + (int)(windGustIntensity * 18f);
        int col = AtmosphereRenderer.withAlpha(0x00000000, alpha);
        int sway = (int)(Math.sin(branchSway) * (5 + windGustIntensity * 5));
        drawBranch(g, -20 + sway, 34, 155 + sway, 16, col, 2);
        drawBranch(g, -10 + sway / 2, 62, 115 + sway, 49, col, 1);
        drawBranch(g, w + 18 - sway, 38, w - 125 - sway, 24, col, 2);
        drawBranch(g, w + 12 - sway / 2, 78, w - 92 - sway, 67, col, 1);
        // Twigs: short broken lines so they read like moving tree shadows.
        drawBranch(g, 48 + sway, 28, 70 + sway, 45, col, 1);
        drawBranch(g, w - 60 - sway, 30, w - 82 - sway, 48, col, 1);
    }

    private static void drawBranch(GuiGraphics g, int x1, int y1, int x2, int y2, int color, int thickness) {
        int steps = Math.max(1, Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)) / 4);
        for (int i = 0; i <= steps; i++) {
            float t = (float)i / (float)steps;
            int x = x1 + (int)((x2 - x1) * t);
            int y = y1 + (int)((y2 - y1) * t);
            g.fill(x, y, x + thickness + 1, y + thickness, color);
        }
    }

    private static void renderMoths(GuiGraphics g, int w, int h) {
        if (lanternPositions == null || lanternPositions.length == 0) return;
        int postH = (int)(h * 0.13f);
        for (int i = 0; i < MOTH_COUNT; i++) {
            int idx = ((int)moths[i][0]) % lanternPositions.length;
            int lx = lanternPositions[idx][0];
            int ly = lanternPositions[idx][1] - postH - 2;
            float angle = moths[i][1];
            float radius = moths[i][2];
            int mx = lx + (int)(Math.cos(angle) * radius);
            int my = ly + (int)(Math.sin(angle * 1.7f + moths[i][4] * 6f) * radius * 0.45f);
            int alpha = 120 + (int)(Math.sin(time * 4.0f + i) * 45f);
            alpha = Math.max(35, Math.min(170, alpha));
            int color = AtmosphereRenderer.withAlpha(0x00FFD06A, alpha);
            g.fill(mx, my, mx + 1, my + 1, color);
            if ((i & 1) == 0) {
                g.fill(mx + 1, my, mx + 2, my + 1, AtmosphereRenderer.withAlpha(0x00FFE8A6, alpha / 2));
            }
        }
    }

    private static void renderSpores(GuiGraphics g, int w, int h) {
        for (int i = 0; i < SPORE_COUNT; i++) {
            int sx = (int)(spores[i][0] * w);
            int sy = (int)(spores[i][1] * h);
            int sz = (int) spores[i][3];
            float yPct = spores[i][1];
            float pulse = (float)(Math.sin(time * 2f + i * 0.7f) * 0.2f + 0.8f);
            int alpha = Math.max(0, Math.min(255, (int)(Math.min(1f, yPct * 2f) * pulse * 120)));
            // Esporas verde-oliva desaturado
            g.fill(sx - sz, sy - sz, sx + sz, sy + sz,
                AtmosphereRenderer.withAlpha(0x005A7038, alpha));
        }
    }

    private static void renderRat(GuiGraphics g, int w, int h) {
        if (!ratActive) return;
        int rx = (int)(ratX * w);
        // Street level Y: use same reference as lanterns (h*0.84 approx)
        int ry = (int)(h * 0.90f);
        // Body: 8x4 dark grey
        int body = 0xFF0F0805;
        int belly = 0xFF1A0F08;
        // Direction determines orientation
        boolean rightward = ratSpeed > 0;
        if (rightward) {
            g.fill(rx, ry, rx + 8, ry + 3, body);       // body
            g.fill(rx, ry + 3, rx + 7, ry + 4, belly);  // belly
            g.fill(rx + 7, ry + 1, rx + 9, ry + 2, body); // head
            g.fill(rx + 8, ry + 3, rx + 9, ry + 4, body); // front leg
            g.fill(rx + 1, ry + 3, rx + 2, ry + 4, body); // back leg
            g.fill(rx - 3, ry + 1, rx, ry + 2, body);     // tail
        } else {
            g.fill(rx, ry, rx + 8, ry + 3, body);
            g.fill(rx + 1, ry + 3, rx + 8, ry + 4, belly);
            g.fill(rx - 1, ry + 1, rx + 1, ry + 2, body); // head
            g.fill(rx, ry + 3, rx + 1, ry + 4, body);
            g.fill(rx + 6, ry + 3, rx + 7, ry + 4, body);
            g.fill(rx + 8, ry + 1, rx + 11, ry + 2, body); // tail
        }
    }

    private static void renderLightningForks(GuiGraphics g, int w, int h) {
        if (lightningFlash <= 0.82f || w < 300 || h < 220) return;
        int alpha = (int)((lightningFlash - 0.82f) / 0.18f * 95f);
        int col = AtmosphereRenderer.withAlpha(0x00F8F0D0, Math.max(0, Math.min(95, alpha)));
        int seedShift = ((int)(time * 10f)) & 3;
        drawBolt(g, (int)(w * 0.72f), 0, (int)(w * 0.63f) + seedShift, (int)(h * 0.24f), col);
        drawBolt(g, (int)(w * 0.72f), 0, (int)(w * 0.80f) - seedShift, (int)(h * 0.19f), col);
    }

    private static void drawBolt(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int segments = 7;
        int px = x1;
        int py = y1;
        for (int i = 1; i <= segments; i++) {
            float t = (float)i / (float)segments;
            int nx = x1 + (int)((x2 - x1) * t) + (int)(Math.sin(time * 11f + i * 2.1f) * 6f);
            int ny = y1 + (int)((y2 - y1) * t);
            drawJaggedLine(g, px, py, nx, ny, color);
            px = nx;
            py = ny;
        }
    }

    private static void drawJaggedLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int steps = Math.max(1, Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)) / 3);
        for (int i = 0; i <= steps; i++) {
            float t = (float)i / (float)steps;
            int x = x1 + (int)((x2 - x1) * t);
            int y = y1 + (int)((y2 - y1) * t);
            g.fill(x, y, x + 2, y + 2, color);
        }
    }

    private static void renderLightningSilhouette(GuiGraphics g, int w, int h) {
        if (lightningFlash <= 0.7f) return;  // solo pico del flash
        // Silueta blanca semi-transparente sobre las chimeneas FG
        float intensity = (lightningFlash - 0.7f) / 0.3f;  // 0..1 en pico
        int alpha = (int)(intensity * 60);
        int silhouette = AtmosphereRenderer.withAlpha(0x00FFEEDD, alpha);
        // Aproximacion: rectangulos delgados sobre las 3 chimeneas FG
        float[] cxs = {0.25f, 0.55f, 0.80f};
        for (float cxF : cxs) {
            int cx = (int)(w * cxF);
            int cy = (int)(h * 0.63f);
            g.fill(cx - 3, cy - 20, cx + 3, cy, silhouette);
            // Puff de humo iluminado tambien
            g.fill(cx - 6, cy - 24, cx + 6, cy - 20, silhouette);
        }
    }

    private static void renderCrows(GuiGraphics g, int w, int h) {
        for (int i = 0; i < CROW_COUNT; i++) {
            int cx = (int)(crows[i][0] * w);
            int cy = (int)(crows[i][1] * h);
            float wingAng = (float) Math.sin(crows[i][3]);
            float scale = crows[i][4];
            int baseSize = (int)(4 * scale);
            int wingH = (int)(wingAng * 3 * scale);
            g.fill(cx - baseSize,     cy - 1,               cx + baseSize,     cy + 1,    0xFF080503);
            g.fill(cx - baseSize * 2, cy - baseSize/2 + wingH, cx,             cy,        0xFF080503);
            g.fill(cx,                cy,                    cx + baseSize * 2, cy - baseSize/2 + wingH, 0xFF080503);
        }
    }

    private static void renderRain(GuiGraphics g, int w, int h) {
        for (int i = 0; i < RAIN_COUNT; i++) {
            int rx = (int)(rain[i][0] * w);
            int ry = (int)(rain[i][1] * h);
            int slant = 1 + (int)(windGustIntensity * 5f);
            g.fill(rx, ry, rx + 1, ry + 4, DiaryTheme.RAIN_LINE);
            g.fill(rx + slant, ry + 4, rx + slant + 1, ry + 8, DiaryTheme.RAIN_LINE);
        }
        renderRainSplashes(g, w, h);
    }

    private static void renderRainSplashes(GuiGraphics g, int w, int h) {
        if (h < 260) return;
        for (int i = 0; i < SPLASH_COUNT; i++) {
            float age = splashes[i][2];
            if (age < 0.15f || age > 0.92f) continue;
            int sx = (int)(splashes[i][0] * w);
            int sy = (int)(splashes[i][1] * h);
            int half = 1 + (int)(age * 4f);
            int alpha = (int)((1f - age) * (70f + windGustIntensity * 25f));
            int color = AtmosphereRenderer.withAlpha(0x00A0A8B0, alpha);
            g.fill(sx - half, sy, sx + half, sy + 1, color);
            if (age > 0.45f) {
                g.fill(sx - half / 2, sy - 1, sx + half / 2, sy, AtmosphereRenderer.withAlpha(0x00D0D8D8, alpha / 2));
            }
        }
    }
}
