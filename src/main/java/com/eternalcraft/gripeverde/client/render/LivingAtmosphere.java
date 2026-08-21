package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LivingAtmosphere {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    private static final boolean DEBUG_EVENTS = false;

    // v0.21.3: RNG compartido con seed fija (sin Math.random en el loop caliente).
    private static final java.util.Random RND = new java.util.Random(0xC10E5EL);

    /** v0.21.3: accesibilidad - movimiento reducido del ambiente (ClientConfig). */
    private static boolean reducedMotion() {
        return com.eternalcraft.gripeverde.config.ClientConfig.REDUCED_MOTION.get();
    }

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

    // ── v0.16.0: la avenida interactiva ─────────────────────────────────────
    // Faroles que el lector puede apagar/encender (por sesion).
    private static final boolean[] lanternOut = new boolean[8];
    private static long lastStreetLevelRenderMs = 0L;

    // La rata que a veces se detiene.
    private static boolean ratWillStand = false;
    private static boolean ratStanding = false;
    private static float ratStandTimer = 0f;

    // El coche del sepulturero (sale cuando doblan las campanas).
    private static boolean hearseActive = false;
    private static float hearseX = -0.2f;
    private static long lastHearseMs = 0L;

    public static long streetLevelRenderMs() { return lastStreetLevelRenderMs; }

    public static float getWindGustIntensity() { return windGustIntensity; }

    /** Alterna un farol. Devuelve true si ahora estan TODOS apagados. */
    public static boolean toggleLantern(int index, int count) {
        if (index < 0 || index >= lanternOut.length) return false;
        lanternOut[index] = !lanternOut[index];
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                com.eternalcraft.gripeverde.client.audio.ModSounds.UI_CLICK_HEAVY.get(),
                lanternOut[index] ? 0.16f : 0.14f,
                lanternOut[index] ? 0.55f : 0.75f);
        } catch (Throwable ignored) {}
        if (count <= 0) return false;
        for (int i = 0; i < count; i++) {
            if (!lanternOut[i]) return false;
        }
        return true;
    }

    public static boolean isLanternOut(int index) {
        return index >= 0 && index < lanternOut.length && lanternOut[index];
    }

    /** Hit-test del farol i (poste completo). */
    public static boolean lanternHit(int i, double mx, double my, int w, int h) {
        if (lanternPositions == null || i < 0 || i >= lanternPositions.length) return false;
        int lx = lanternPositions[i][0];
        int ly = lanternPositions[i][1];
        int postH = (int)(h * 0.13f);
        return mx >= lx - 7 && mx <= lx + 8 && my >= ly - postH - 12 && my <= ly + 3;
    }

    /** Devuelve el indice del farol bajo el cursor, o -1. */
    public static int lanternIndexAt(double mx, double my, int w, int h) {
        if (lanternPositions == null) return -1;
        for (int i = 0; i < lanternPositions.length; i++) {
            if (lanternHit(i, mx, my, w, h)) return i;
        }
        return -1;
    }

    /** Hit-test de la rata (solo si esta activa). */
    public static boolean ratHit(double mx, double my, int w, int h) {
        if (!ratActive) return false;
        int rx = (int)(ratX * w);
        int ry = (int)(h * 0.90f);
        return mx >= rx - 4 && mx <= rx + 12 && my >= ry - (ratStanding ? 8 : 2) && my <= ry + 6;
    }

    /** La rata se asusta y huye (tras ser descubierta). */
    public static void scareRat() {
        ratStanding = false;
        ratSpeed = ratSpeed >= 0 ? 0.55f : -0.55f;
    }

    public static boolean isRatStanding() { return ratStanding; }

    // ── Kiosco del Heraldo ──
    public static int heraldKioskX(int w) { return (int)(w * 0.345f); }
    public static int heraldKioskY(int h) { return (int)(h * 0.842f); }

    public static boolean heraldHit(double mx, double my, int w, int h) {
        if (h < 300) return false;
        int kx = heraldKioskX(w);
        int ky = heraldKioskY(h);
        return mx >= kx - 10 && mx <= kx + 14 && my >= ky - 24 && my <= ky + 2;
    }

    public static void triggerLightningFlash(float intensity) {
        lightningTimer = 0f;
        lightningFlash = Math.max(lightningFlash, Math.max(0.15f, Math.min(1.0f, intensity)));
        nextLightning = 8f + (float)(RND.nextFloat() * 12f);
    }

    public static void tick(float deltaTime) {
        time += deltaTime;
        initAll();
        boolean reduced = reducedMotion();

        if (!reduced) {
            for (int i = 0; i < FOG_OFFSETS.length; i++) {
                FOG_OFFSETS[i] += FOG_SPEEDS[i] * deltaTime * 60f;
                if (FOG_OFFSETS[i] > 300f) FOG_OFFSETS[i] = -200f;
            }
        }
        for (int i = 0; i < SPORE_COUNT; i++) {
            spores[i][1] -= spores[i][2] * deltaTime * 60f;
            if (spores[i][1] < -0.05f) {
                spores[i][0] = (float) RND.nextFloat();
                spores[i][1] = 1.05f;
                spores[i][2] = 0.001f + (float)(RND.nextFloat() * 0.003f);
                spores[i][3] = 1f + (float)(RND.nextFloat() * 2f);
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
                crows[i][1] = 0.1f + (float)(RND.nextFloat() * 0.3f);
                crows[i][2] = 0.001f + (float)(RND.nextFloat() * 0.002f);
                crows[i][3] = 0f;
            }
        }
        for (int i = 0; i < RAIN_COUNT; i++) {
            rain[i][1] += rain[i][2] * deltaTime * 60f;
            if (rain[i][1] > 1.1f) {
                rain[i][0] = (float) RND.nextFloat();
                rain[i][1] = -0.05f;
            }
        }
        for (int i = 0; i < SPLASH_COUNT; i++) {
            splashes[i][2] += deltaTime * (0.9f + i * 0.03f);
            if (splashes[i][2] > 1f) {
                splashes[i][0] = (float) RND.nextFloat();
                splashes[i][1] = 0.84f + (float)(RND.nextFloat() * 0.12f);
                splashes[i][2] = 0f;
            }
        }
        if (reduced) {
            // v0.21.3: movimiento reducido - las polillas se quedan quietas.
        } else {
            for (int i = 0; i < MOTH_COUNT; i++) {
                moths[i][1] += moths[i][3] * deltaTime;
                if (moths[i][1] > Math.PI * 2f) moths[i][1] -= (float)(Math.PI * 2f);
            }
        }
        for (int i = 0; i < RIPPLE_COUNT; i++) {
            ripples[i][2] += deltaTime * ripples[i][3];
            if (ripples[i][2] > 1f) {
                resetRipple(i);
            }
        }
        if (!reduced) {
            for (int i = 0; i < PAPER_SCRAP_COUNT; i++) {
                paperScraps[i][0] += (paperScraps[i][2] + windGustIntensity * 0.018f) * deltaTime;
                paperScraps[i][3] += deltaTime * (5f + windGustIntensity * 6f);
                if (paperScraps[i][0] < -0.08f || paperScraps[i][0] > 1.08f) {
                    resetPaperScrap(i);
                }
            }
        }
        if (reduced) {
            windGustIntensity = 0f;
            windGustDuration = 0f;
        } else {
            windGustTimer += deltaTime;
            if (windGustDuration > 0f) {
                windGustDuration -= deltaTime;
                windGustIntensity = Math.max(0f, windGustDuration / 3.2f);
            } else if (windGustTimer >= windGustNext) {
                windGustTimer = 0f;
                windGustDuration = 2.4f + (float)(RND.nextFloat() * 1.8f);
                windGustNext = 18f + (float)(RND.nextFloat() * 28f);
                windGustIntensity = 1f;
            }
            branchSway += deltaTime * (0.55f + windGustIntensity * 0.8f);
        }
        lightningTimer += deltaTime;
        if (lightningFlash > 0f) {
            lightningFlash -= deltaTime * 3f;
            lightningFlash  = Math.max(0f, lightningFlash);
        }
        if (lightningTimer >= nextLightning) {
            lightningTimer = 0f;
            lightningFlash = 1f;
            nextLightning  = 8f + (float)(RND.nextFloat() * 12f);
            if (DEBUG_EVENTS) LOGGER.info("[GripeVerde] Relampago DISPARADO, proximo en {}s", nextLightning);
        }
        vignettePulse = reduced ? 0.5f : (float)(Math.sin(time * 0.5f) * 0.5f + 0.5f);

        // Erratic lantern timer
        if (reduced) {
            erraticIntensity = 0f;
            erraticTimer = 0f;
        } else {
            erraticTimer += deltaTime;
            if (erraticIntensity > 0f) {
                erraticDuration -= deltaTime;
                if (erraticDuration <= 0f) {
                    erraticIntensity = 0f;
                    erraticNextEvent = 30f + (float)(RND.nextFloat() * 30.0);
                    erraticTimer = 0f;
                }
            } else if (erraticTimer >= erraticNextEvent) {
                erraticIntensity = 1f;
                erraticDuration = 3f + (float)(RND.nextFloat() * 2.0);
            }
        }

        // Street rat: crossing logic (+ v0.16.0: la rata que a veces se detiene)
        if (reduced) {
            ratActive = false;
            ratStanding = false;
        } else if (ratActive) {
            if (ratStanding) {
                ratStandTimer -= deltaTime;
                if (ratStandTimer <= 0f) {
                    ratStanding = false;
                    ratSpeed = ratSpeed >= 0 ? 0.30f : -0.30f; // huye mas rapido
                }
            } else {
                ratX += ratSpeed * deltaTime;
                if (ratWillStand && ratX > 0.42f && ratX < 0.58f) {
                    ratStanding = true;
                    ratStandTimer = 2.6f;
                    ratWillStand = false;
                }
            }
            if (ratX < -0.1f || ratX > 1.1f) {
                ratActive = false;
                ratStanding = false;
                ratTimer = 0f;
                ratNextEvent = 40f + (float)(RND.nextFloat() * 40.0);
            }
        } else {
            ratTimer += deltaTime;
            if (ratTimer >= ratNextEvent) {
                ratActive = true;
                ratWillStand = RND.nextFloat() < 0.12; // solo a veces se detiene
                if (RND.nextFloat() < 0.5) {
                    ratX = -0.05f;
                    ratSpeed = 0.15f + (float)(RND.nextFloat() * 0.10);
                } else {
                    ratX = 1.05f;
                    ratSpeed = -(0.15f + (float)(RND.nextFloat() * 0.10));
                }
            }
        }

        // v0.16.0: el coche del sepulturero. Sale unos segundos despues de que
        // doblen las campanas, solo si el lector esta mirando la calle.
        if (!hearseActive) {
            long bellMs = com.eternalcraft.gripeverde.client.audio.MenuAmbience.lastBellMs();
            long nowMs = System.currentTimeMillis();
            if (bellMs > lastHearseMs && nowMs - bellMs > 5000L
                    && nowMs - lastStreetLevelRenderMs < 600L) {
                lastHearseMs = nowMs;
                hearseActive = true;
                hearseX = -0.18f;
                try {
                    com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                        com.eternalcraft.gripeverde.client.lore.Discoveries.COFFIN_CART);
                } catch (Throwable ignored) {}
            }
        } else {
            hearseX += 0.048f * deltaTime;
            if (hearseX > 1.2f) hearseActive = false;
        }

    }

    private static void initAll() {
        if (!sporesInit) {
            sporesInit = true;
            for (int i = 0; i < SPORE_COUNT; i++) {
                spores[i][0] = (float) RND.nextFloat();
                spores[i][1] = (float) RND.nextFloat();
                spores[i][2] = 0.001f + (float)(RND.nextFloat() * 0.003f);
                spores[i][3] = 1f + (float)(RND.nextFloat() * 2f);
            }
        }
        if (!crowsInit) {
            crowsInit = true;
            for (int i = 0; i < CROW_COUNT; i++) {
                crows[i][0] = (float) RND.nextFloat();
                crows[i][1] = 0.1f + (float)(RND.nextFloat() * 0.3f);
                crows[i][2] = 0.001f + (float)(RND.nextFloat() * 0.002f);
                crows[i][3] = (float)(RND.nextFloat() * Math.PI * 2);
                crows[i][4] = 0.6f + (float)(RND.nextFloat() * 0.8f);
            }
        }
        if (!rainInit) {
            rainInit = true;
            for (int i = 0; i < RAIN_COUNT; i++) {
                rain[i][0] = (float) RND.nextFloat();
                rain[i][1] = (float) RND.nextFloat();
                rain[i][2] = 0.008f + (float)(RND.nextFloat() * 0.006f);
            }
        }
        if (!splashesInit) {
            splashesInit = true;
            for (int i = 0; i < SPLASH_COUNT; i++) {
                splashes[i][0] = (float) RND.nextFloat();
                splashes[i][1] = 0.84f + (float)(RND.nextFloat() * 0.12f);
                splashes[i][2] = (float) RND.nextFloat();
            }
        }
        if (!mothsInit) {
            mothsInit = true;
            for (int i = 0; i < MOTH_COUNT; i++) {
                moths[i][0] = i;
                moths[i][1] = (float)(RND.nextFloat() * Math.PI * 2.0);
                moths[i][2] = 5f + (float)(RND.nextFloat() * 8f);
                moths[i][3] = 0.7f + (float)(RND.nextFloat() * 1.3f);
                moths[i][4] = (float)RND.nextFloat();
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
        ripples[i][0] = (float)RND.nextFloat();
        ripples[i][1] = 0.84f + (float)(RND.nextFloat() * 0.12f);
        ripples[i][2] = (float)(RND.nextFloat() * 0.15f);
        ripples[i][3] = 0.35f + (float)(RND.nextFloat() * 0.45f);
    }

    private static void resetPaperScrap(int i) {
        boolean leftToRight = RND.nextFloat() < 0.5;
        paperScraps[i][0] = leftToRight ? -0.05f : 1.05f;
        paperScraps[i][1] = 0.84f + (float)(RND.nextFloat() * 0.12f);
        paperScraps[i][2] = (leftToRight ? 1f : -1f) * (0.035f + (float)(RND.nextFloat() * 0.05f));
        paperScraps[i][3] = (float)(RND.nextFloat() * Math.PI * 2.0);
        paperScraps[i][4] = 0.5f + (float)RND.nextFloat();
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
        lastStreetLevelRenderMs = System.currentTimeMillis();
        renderStreet(g, w, h);
        renderPuddleRipples(g, w, h);
        renderLanternReflections(g, w, h);
        renderPaperScraps(g, w, h);
        renderGroundFog(g, w, h);
        renderHeraldKiosk(g, w, h);
        for (int i = 0; i < lanternPositions.length; i++) {
            renderGasLantern(g, lanternPositions[i][0], lanternPositions[i][1], i, w, h);
        }
        renderMoths(g, w, h);
        renderSpores(g, w, h);
        renderRat(g, w, h);
        renderHearse(g, w, h);
    }

    /** Kiosco de madera con el Heraldo del dia (si nadie lo recogio aun). */
    private static void renderHeraldKiosk(GuiGraphics g, int w, int h) {
        if (h < 300) return;
        int kx = heraldKioskX(w);
        int ky = heraldKioskY(h);
        boolean taken = false;
        try {
            long today = java.time.LocalDate.now().toEpochDay();
            taken = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("herald_day") == today;
        } catch (Throwable ignored) {}

        // Patas y caja del kiosco
        g.fill(kx - 8, ky - 14, kx + 12, ky - 2, 0xFF1F1408);
        g.fill(kx - 7, ky - 13, kx + 11, ky - 3, 0xFF2A1C10);
        g.fill(kx - 7, ky - 2, kx - 5, ky + 2, 0xFF140C06);
        g.fill(kx + 9, ky - 2, kx + 11, ky + 2, 0xFF140C06);
        // Tejadillo
        g.fill(kx - 10, ky - 17, kx + 14, ky - 14, 0xFF120A05);
        g.fill(kx - 9, ky - 18, kx + 13, ky - 17, 0xFF241608);
        if (!taken) {
            // Pila de periodicos
            g.fill(kx - 5, ky - 8, kx + 9, ky - 6, 0xFFC9B78A);
            g.fill(kx - 4, ky - 10, kx + 8, ky - 8, 0xFFD8CAB0);
            g.fill(kx - 3, ky - 11, kx + 7, ky - 10, 0xFFE6D2A6);
            g.fill(kx - 2, ky - 10, kx + 5, ky - 9, 0xFF3D2A18); // titular
            // El de arriba, ligeramente desplegado
            float lift = (float)(Math.sin(time * 1.3f) * 0.5f + 0.5f);
            int la = (int)(120 + lift * 60);
            g.fill(kx - 1, ky - 13, kx + 5, ky - 11, AtmosphereRenderer.withAlpha(0x00E6D2A6, la));
        }
    }

    /** El coche del sepulturero: carretilla lenta con ataud, tras las campanas. */
    private static void renderHearse(GuiGraphics g, int w, int h) {
        if (!hearseActive || h < 260) return;
        int hx = (int)(hearseX * w);
        int hy = (int)(h * 0.885f);
        int dark = 0xFF0C0704;
        int darker = 0xFF070402;
        // El tirador: figura encorvada
        int lean = (int)(Math.sin(time * 2.2f) * 1f);
        g.fill(hx - 14 + lean, hy - 10, hx - 11 + lean, hy - 3, dark);   // cuerpo
        g.fill(hx - 13 + lean, hy - 13, hx - 11 + lean, hy - 10, dark);  // cabeza
        g.fill(hx - 13, hy - 3, hx - 12, hy, dark);                      // piernas
        g.fill(hx - 11, hy - 3, hx - 10, hy, dark);
        g.fill(hx - 11 + lean, hy - 8, hx - 6, hy - 7, dark);            // brazos al carro
        // El carro
        g.fill(hx - 6, hy - 8, hx + 12, hy - 2, darker);
        g.fill(hx - 6, hy - 9, hx + 12, hy - 8, dark);
        // El ataud encima, con tapa
        g.fill(hx - 4, hy - 13, hx + 10, hy - 9, 0xFF1A0F08);
        g.fill(hx - 3, hy - 14, hx + 9, hy - 13, 0xFF241608);
        g.fill(hx + 2, hy - 13, hx + 3, hy - 9, 0xFF3D2E1A); // cruz de la tapa
        g.fill(hx + 1, hy - 12, hx + 4, hy - 11, 0xFF3D2E1A);
        // Ruedas
        g.fill(hx - 3, hy - 2, hx + 1, hy + 2, dark);
        g.fill(hx + 6, hy - 2, hx + 10, hy + 2, dark);
        g.fill(hx - 2, hy - 1, hx, hy + 1, darker);
        g.fill(hx + 7, hy - 1, hx + 9, hy + 1, darker);
        // Farolillo del carro: ambar muy tenue
        float fl = (float)(Math.sin(time * 5.1f) * 0.25f + 0.75f);
        int la = (int)(90 * fl);
        g.fill(hx + 11, hy - 11, hx + 13, hy - 8, AtmosphereRenderer.withAlpha(0x00FFAA33, la));
        g.fill(hx + 10, hy - 12, hx + 14, hy - 7, AtmosphereRenderer.withAlpha(0x00FFAA33, la / 4));
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
            int nightOverlayAlpha = 32;
            g.fill(0, 0, w, h,
                AtmosphereRenderer.withAlpha(0x00000C08, nightOverlayAlpha));
        }

        int vigAlpha = Math.min(200, (int)(160 + vignettePulse * 24));
        int margin = (int)(Math.min(w, h) * 0.18f);
        if (margin <= 0) return;
        // Optimized: use gradient fills (4 rects per layer, 2 layers) instead of hundreds of 2px slices
        int outer = AtmosphereRenderer.withAlpha(0x00000000, vigAlpha);
        int inner = AtmosphereRenderer.withAlpha(0x00000000, 0);
        // Top / Bottom / Left / Right with gradient fade inward
        g.fillGradient(0, 0, w, margin, outer, inner);
        g.fillGradient(0, h - margin, w, h, inner, outer);
        g.fillGradient(0, margin, margin, h - margin, outer, inner);
        g.fillGradient(w - margin, margin, w, h - margin, inner, outer);
        // Corner soft blend with small overlay
        int cornerAlpha = vigAlpha / 3;
        g.fill(0, 0, margin, margin, AtmosphereRenderer.withAlpha(0x00000000, cornerAlpha));
        g.fill(w - margin, 0, w, margin, AtmosphereRenderer.withAlpha(0x00000000, cornerAlpha));
        g.fill(0, h - margin, margin, h, AtmosphereRenderer.withAlpha(0x00000000, cornerAlpha));
        g.fill(w - margin, h - margin, w, h, AtmosphereRenderer.withAlpha(0x00000000, cornerAlpha));
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
            if (isLanternOut(i)) continue; // apagado: sin reflejo
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
        // Optimized: 3 layers, 1.6x width instead of 3x, gradient for softness
        for (int layer = 0; layer < 3; layer++) {
            int yBase = fogY + layer * (int)(h * 0.025f);
            float alphaF = 0.28f + layer * 0.09f;
            float pulse = (float)(Math.sin(time * 0.52f + layer * 0.9f) * 0.08f + alphaF);
            int alpha = Math.max(0, Math.min(120, (int)(pulse * 110)));
            int offset = (int)(FOG_OFFSETS[layer % FOG_OFFSETS.length] * 0.45f);
            int fogColor = AtmosphereRenderer.withAlpha(0x003D2E1A, alpha);
            int bandH = Math.max(3, (int)(h * 0.035f));
            int startX = offset - w / 3;
            // Use gradient edges for softness instead of huge flat rect
            g.fill(startX, yBase, startX + (int)(w * 1.7f), yBase + bandH, fogColor);
        }
    }

    /**
     * Farol de gas victoriano: poste ornamental + cabezal 4 vidrios + llama danzante
     * Optimized: halo con gradient, no 12+ fills por farol
     * v0.16.0: el lector puede apagarlos; el farol enfermo no se recupera igual.
     */
    private static void renderGasLantern(GuiGraphics g, int lx, int ly, int index, int w, int h) {
        int postH = (int)(h * 0.13f);

        // Base ornamental
        g.fill(lx - 3, ly - 2,     lx + 4, ly,        0xFF1A1108);
        g.fill(lx - 4, ly,         lx + 5, ly + 2,    0xFF0A0503);
        g.fill(lx - 1, ly - postH, lx + 2, ly - 2,    0xFF2A1810);
        g.fill(lx - 2, ly - postH/2, lx + 3, ly - postH/2 + 3, 0xFF3D2E1A);

        boolean out = isLanternOut(index);

        float flicker = (float)(
            Math.sin(time * 3.7f + index * 1.3f) * 0.25f +
            Math.sin(time * 7.1f + index * 2.7f) * 0.15f +
            0.85f
        );
        if (index == ERRATIC_LANTERN_INDEX && erraticIntensity > 0f) {
            float chaos = (float)(
                Math.sin(time * 23f) * 0.3f +
                Math.sin(time * 37f + 1.7f) * 0.2f +
                Math.sin(time * 53f + 3.1f) * 0.15f
            );
            flicker = flicker + chaos * erraticIntensity;
        }
        // Consecuencia permanente: tras apagar la avenida, el farol enfermo
        // nunca vuelve a arder del todo.
        boolean sickDim = false;
        if (index == ERRATIC_LANTERN_INDEX) {
            try {
                sickDim = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("avenue_darkened") > 0;
            } catch (Throwable ignored) {}
        }
        if (sickDim && !out) flicker = Math.min(flicker, 0.38f);
        flicker = Math.max(0.15f, Math.min(1.2f, flicker));
        if (out) flicker = 0f;

        int lanternY = ly - postH;

        // Sombrero
        g.fill(lx - 6, lanternY - 8, lx + 7, lanternY - 6, 0xFF1A1108);
        g.fill(lx - 5, lanternY - 6, lx + 6, lanternY - 5, 0xFF2A1810);
        g.fill(lx - 4, lanternY - 5, lx + 5, lanternY - 4, 0xFF3D2E1A);

        int boxSize = 5;
        g.fill(lx - boxSize - 1, lanternY - 4, lx + boxSize + 1, lanternY + boxSize + 1, 0xFF1F1408);
        int lightAlpha = (int)(flicker * 200);
        g.fill(lx - boxSize, lanternY - 3, lx + boxSize, lanternY + boxSize,
            AtmosphereRenderer.withAlpha(0x00FFAA33, lightAlpha));
        int coreAlpha = (int)(flicker * 235);
        g.fill(lx - 2, lanternY, lx + 3, lanternY + 3,
            AtmosphereRenderer.withAlpha(0x00FFEE88, coreAlpha));
        g.fill(lx, lanternY - 3, lx + 1, lanternY + boxSize, 0xFF2A1810);
        g.fill(lx - boxSize, lanternY + 1, lx + boxSize, lanternY + 2, 0xFF2A1810);
        g.fill(lx - boxSize - 1, lanternY + boxSize + 1, lx + boxSize + 1, lanternY + boxSize + 3, 0xFF1A1108);

        if (out) {
            // Vidrio frio y un hilo de humo apagandose
            g.fill(lx - boxSize, lanternY - 3, lx + boxSize, lanternY + boxSize,
                AtmosphereRenderer.withAlpha(0x00101820, 60));
            float wispA = (float)(Math.sin(time * 0.9f + index) * 0.5f + 0.5f) * 26f;
            g.fill(lx, lanternY - 10, lx + 1, lanternY - 6, AtmosphereRenderer.withAlpha(0x00909090, (int)wispA));
            return;
        }

        // Optimized halos: 2 gradients instead of 12+ fills
        int haloR = (int)(28 * flicker);
        int haloAlpha = (int)(flicker * 32);
        g.fill(lx - haloR, ly - haloR / 4, lx + haloR, ly + haloR / 4,
            AtmosphereRenderer.withAlpha(0x00FFAA33, haloAlpha));
        int floorR = (int)(48 * flicker);
        int floorA = (int)(flicker * 16);
        g.fill(lx - floorR, ly - 1, lx + floorR, ly + 6,
            AtmosphereRenderer.withAlpha(0x00D4941E, floorA));
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
            if (isLanternOut(idx)) continue; // sin llama: las polillas se dispersan
            int lx = lanternPositions[idx][0];
            int ly = lanternPositions[idx][1] - postH - 2;
            float angle = moths[i][1];
            float radius = moths[i][2];
            int mx = lx + (int)(Math.cos(angle) * radius);
            int my = ly + (int)(Math.sin(angle * 1.7f + moths[i][4] * 6f) * radius * 0.45f);
            int alpha = reducedMotion() ? 130 : 120 + (int)(Math.sin(time * 4.0f + i) * 45f);
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

        if (ratStanding) {
            // La rata que se detuvo: erguida, mirando alrededor.
            int twitch = (int)(Math.sin(time * 6.3f) * 1f);
            g.fill(rx + 2, ry - 5, rx + 6, ry + 3, body);          // cuerpo erguido
            g.fill(rx + 2, ry - 8, rx + 6, ry - 5, body);          // cabeza arriba
            g.fill(rx + 3, ry - 9, rx + 4, ry - 8, body);          // oreja
            g.fill(rx + 5, ry - 9, rx + 6, ry - 8, body);          // oreja
            g.fill(rx + 3 + twitch, ry - 7, rx + 4 + twitch, ry - 6, 0xFFD4941E); // ojo
            g.fill(rx + 1, ry + 1, rx + 3, ry + 3, body);          // patas
            g.fill(rx + 5, ry + 1, rx + 7, ry + 3, body);
            g.fill(rx + 6, ry - 3, rx + 9, ry - 2, body);          // cola apoyada
            g.fill(rx, ry - 4 + twitch, rx + 1, ry - 3 + twitch, body); // bigote/mano
            return;
        }

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
