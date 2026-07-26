package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LivingAtmosphere {

    private static float time = 0f;
    private static int[][] lanternPositions = null;
    private static int lastLanternW = 0, lastLanternH = 0;

    private static final float[] FOG_OFFSETS = {0f, 0f, 0f};
    private static final float[] FOG_SPEEDS  = {0.3f, 0.2f, 0.25f};

    private static final int SPORE_COUNT = 30;
    private static final float[][] spores = new float[SPORE_COUNT][4];
    private static boolean sporesInit = false;

    private static final int CROW_COUNT = 3;
    private static final float[][] crows = new float[CROW_COUNT][5];
    private static boolean crowsInit = false;

    private static final int RAIN_COUNT = 80;
    private static final float[][] rain = new float[RAIN_COUNT][3];
    private static boolean rainInit = false;

    private static float lightningTimer = 20f;
    private static float lightningFlash = 0f;
    private static float nextLightning  = 25f;

    private static float vignettePulse = 0f;

    // Street rat: sprite 8x4 crossing horizontally every 40-80s
    private static float ratX = -0.2f;       // 0..1 normalized screen X
    private static float ratSpeed = 0f;       // per-second, negative = right-to-left
    private static float ratTimer = 10f;
    private static float ratNextEvent = 30f;
    private static boolean ratActive = false;

    // Erratic lantern: one specific lantern flickers wildly every 30-60s
    private static final int ERRATIC_LANTERN_INDEX = 2;
    private static float erraticTimer = 20f;
    private static float erraticNextEvent = 30f;
    private static float erraticIntensity = 0f;  // 0=normal, 1=full erratic
    private static float erraticDuration = 0f;

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
        for (int i = 0; i < CROW_COUNT; i++) {
            crows[i][0] += crows[i][2] * deltaTime * 60f;
            crows[i][3] += deltaTime * 8f;
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
        lightningTimer += deltaTime;
        if (lightningFlash > 0f) {
            lightningFlash -= deltaTime * 3f;
            lightningFlash  = Math.max(0f, lightningFlash);
        }
        if (lightningTimer >= nextLightning) {
            lightningTimer = 0f;
            lightningFlash = 1f;
            nextLightning  = 15f + (float)(Math.random() * 25f);
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
            // Off-screen either side
            if (ratX < -0.1f || ratX > 1.1f) {
                ratActive = false;
                ratTimer = 0f;
                ratNextEvent = 40f + (float)(Math.random() * 40.0);
            }
        } else {
            ratTimer += deltaTime;
            if (ratTimer >= ratNextEvent) {
                ratActive = true;
                // 50/50 direction
                if (Math.random() < 0.5) {
                    ratX = -0.05f;
                    ratSpeed = 0.15f + (float)(Math.random() * 0.10);  // left to right
                } else {
                    ratX = 1.05f;
                    ratSpeed = -(0.15f + (float)(Math.random() * 0.10));  // right to left
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
    }

    public static void renderFogLow(GuiGraphics g, int w, int h) {
        // Niebla OCRE (no verde)
        float[][] bands = {
            {0.68f, 0.06f, 0.45f},
            {0.72f, 0.05f, 0.60f},
            {0.76f, 0.04f, 0.35f},
        };
        for (int i = 0; i < bands.length; i++) {
            int y1 = (int)(h * bands[i][0]);
            int y2 = (int)(h * (bands[i][0] + bands[i][1]));
            int offset = (int)(FOG_OFFSETS[i % FOG_OFFSETS.length]);
            float pulse = (float)(Math.sin(time * 0.4f + i * 1.2f) * 0.1f + bands[i][2]);
            int alpha = Math.max(0, Math.min(255, (int)(pulse * 160)));
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
        renderGroundFog(g, w, h);
        for (int i = 0; i < lanternPositions.length; i++) {
            renderGasLantern(g, lanternPositions[i][0], lanternPositions[i][1], i, w, h);
        }
        renderSpores(g, w, h);
        renderRat(g, w, h);
    }

    public static void renderAir(GuiGraphics g, int w, int h) {
        renderRain(g, w, h);
        renderCrows(g, w, h);
    }

    public static void renderFlash(GuiGraphics g, int w, int h) {
        if (lightningFlash <= 0f) return;
        int alpha = (int)(lightningFlash * 50);
        // Flash calido (no verde)
        g.fill(0, 0, w, h, AtmosphereRenderer.withAlpha(0x00FFEECC, alpha));
    }

    public static void renderVignette(GuiGraphics g, int w, int h) {
        int vigAlpha = Math.min(255, (int)(200 + vignettePulse * 30));
        int margin = (int)(Math.min(w, h) * 0.28f);
        for (int x = 0; x < margin; x++) {
            float t = 1f - (float) x / margin;
            int a = (int)(t * t * vigAlpha);
            g.fill(x,         0, x + 1, h, AtmosphereRenderer.withAlpha(0x00000000, a));
            g.fill(w - x - 1, 0, w - x, h, AtmosphereRenderer.withAlpha(0x00000000, a));
        }
        for (int y = 0; y < margin; y++) {
            float t = 1f - (float) y / margin;
            int a = (int)(t * t * vigAlpha);
            g.fill(0, y,         w, y + 1, AtmosphereRenderer.withAlpha(0x00000000, a));
            g.fill(0, h - y - 1, w, h - y, AtmosphereRenderer.withAlpha(0x00000000, a));
        }
    }

    private static void renderStreet(GuiGraphics g, int w, int h) {
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
            g.fill(rx, ry, rx + 1, ry + 6, DiaryTheme.RAIN_LINE);
        }
    }
}