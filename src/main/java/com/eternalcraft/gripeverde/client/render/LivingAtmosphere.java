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

    /**
     * v0.23.0: ayuda de fotosensibilidad separada de Movimiento reducido
     * (WCAG 2.3.1). Apaga relampagos y destellos sin congelar niebla,
     * lluvia, faroles ni ningun otro movimiento de la escena.
     */
    private static boolean reduceFlashes() {
        try {
            return com.eternalcraft.gripeverde.config.ClientConfig.REDUCE_FLASHES.get();
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * v0.23.1: reloj monotonico compartido con MenuAmbience (lastBellMs).
     * El coche del sepulturero y el seguimiento de la calle dejan de depender
     * del reloj de pared del SO.
     */
    private static long nowMs() { return System.nanoTime() / 1_000_000L; }

    /** Accesibilidad: congela la animacion ambiental sin retirar la composicion. */
    private static boolean reducedMotion() {
        try {
            return com.eternalcraft.gripeverde.config.ClientConfig.REDUCED_MOTION.get();
        } catch (IllegalStateException ignored) {
            return false;
        }
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


    // ── v0.16.0: la avenida interactiva ─────────────────────────────────────
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

    // v0.25.0: tres relampagos para quien se queda mirando (storm_watcher).
    private static int flashCount = 0;
    public static int flashCount() { return flashCount; }

    // v0.26.0: los faroles reconocen la mano del lector (hover -> halo respira).
    private static int mousePx = -9999, mousePy = -9999;
    private static float[] lanternHover = null;
    public static void setMouse(double mx, double my) { mousePx = (int) mx; mousePy = (int) my; }

    // v0.25.0: la lluvia se deja atrapar (drop_catcher).
    public static int rainDropAt(double mx, double my, int w, int h) {
        if (!rainInit) return -1;
        int best = -1; double bestD2 = Double.MAX_VALUE;
        for (int i = 0; i < RAIN_COUNT; i++) {
            double rx = rain[i][0] * w, ry = rain[i][1] * h;
            if (ry < 0 || ry > h) continue;
            double dx = mx - rx;          if (dx < -5 || dx > 6) continue;
            double dy = my - (ry + 3);    if (dy < -6 || dy > 8) continue;
            double d2 = dx * dx + dy * dy;
            if (d2 < bestD2) { bestD2 = d2; best = i; }
        }
        return best;
    }
    /** La gota atrapada vuelve al cielo. */
    public static void scareDrop(int i) {
        if (i < 0 || i >= RAIN_COUNT) return;
        rain[i][0] = (float) RND.nextFloat();
        rain[i][1] = -0.06f - (float)(RND.nextFloat() * 0.12f);
    }

    public static void triggerLightningFlash(float intensity) {
        if (reducedMotion() || reduceFlashes()) return;
        lightningTimer = 0f;
        lightningFlash = Math.max(lightningFlash, Math.max(0.15f, Math.min(1.0f, intensity)));
        nextLightning = 8f + (float)(RND.nextFloat() * 12f);
        flashCount++;
    }

    public static void tick(float deltaTime) {
        initAll();
        // v0.26.0: halo de los faroles siguiendo la mano (tambien con movimiento
        // reducido: no hay movimiento nuevo, solo una respiracion del brillo).
        if (lanternPositions != null) {
            if (lanternHover == null || lanternHover.length != lanternPositions.length) {
                lanternHover = new float[lanternPositions.length];
            }
            for (int i = 0; i < lanternPositions.length; i++) {
                int dx = mousePx - lanternPositions[i][0];
                int dy = mousePy - (lanternPositions[i][1] - 18);
                float target = (dx * dx + dy * dy) < 30 * 30 ? 1f : 0f;
                lanternHover[i] += (target - lanternHover[i]) * Math.min(1f, deltaTime * 9f);
                if (lanternHover[i] < 0.001f && target == 0f) lanternHover[i] = 0f;
            }
        }
        boolean reduced = reducedMotion();
        if (reduced) {
            windGustIntensity = 0f;
            windGustDuration = 0f;
            lightningFlash = 0f;
            vignettePulse = 0.5f;
            return;
        }
        // v0.23.0: con destellos reducidos el temporizador queda congelado en
        // cero, de modo que ni el relampago ambiental programado ni un flash
        // externo ya en curso llegan a dibujarse; el resto de la escena sigue viva.
        if (reduceFlashes()) {
            lightningFlash = 0f;
            lightningTimer = 0f;
        }

        time += deltaTime;
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
            flashCount++;
            // v0.24.0: en noche de tormenta el cielo no descansa.
            boolean storm = com.eternalcraft.gripeverde.client.audio.MenuAmbience.isStormNight();
            nextLightning  = storm ? 3.5f + (float)(RND.nextFloat() * 6f)
                                   : 8f + (float)(RND.nextFloat() * 12f);
            if (DEBUG_EVENTS) LOGGER.info("[GripeVerde] Relampago DISPARADO, proximo en {}s", nextLightning);
        }
        vignettePulse = reduced ? 0.5f : (float)(Math.sin(time * 0.5f) * 0.5f + 0.5f);

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
            long nowMs = nowMs();
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
        lastStreetLevelRenderMs = nowMs();
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
        renderHearse(g, w, h);
        renderStormVeil(g, w, h);
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
     * Farol de gas victoriano: poste ornamental + cabezal 4 vidrios + llama danzante.
     * Siempre encendido; el parpadeo ambiental conserva un minimo visible.
     */
    private static void renderGasLantern(GuiGraphics g, int lx, int ly, int index, int w, int h) {
        int postH = (int)(h * 0.13f);

        // Base ornamental
        g.fill(lx - 3, ly - 2,     lx + 4, ly,        0xFF1A1108);
        g.fill(lx - 4, ly,         lx + 5, ly + 2,    0xFF0A0503);
        g.fill(lx - 1, ly - postH, lx + 2, ly - 2,    0xFF2A1810);
        g.fill(lx - 2, ly - postH/2, lx + 3, ly - postH/2 + 3, 0xFF3D2E1A);

        float flicker = (float)(
            Math.sin(time * 3.7f + index * 1.3f) * 0.25f +
            Math.sin(time * 7.1f + index * 2.7f) * 0.15f +
            0.85f
        );
        // Los faroles permanecen encendidos: ningun estado persistido, evento
        // erratico ni bajada de FPS puede anular la llama o su reflejo.
        flicker = Math.max(0.45f, Math.min(1.2f, flicker));

        int lanternY = ly - postH;

        // v0.26.0: la mano del lector acerca la llama (suavizado en tick).
        float hover = (lanternHover != null && index < lanternHover.length) ? lanternHover[index] : 0f;
        if (hover < 0f) hover = 0f;

        // Sombrero
        g.fill(lx - 6, lanternY - 8, lx + 7, lanternY - 6, 0xFF1A1108);
        g.fill(lx - 5, lanternY - 6, lx + 6, lanternY - 5, 0xFF2A1810);
        g.fill(lx - 4, lanternY - 5, lx + 5, lanternY - 4, 0xFF3D2E1A);

        int boxSize = 5;
        g.fill(lx - boxSize - 1, lanternY - 4, lx + boxSize + 1, lanternY + boxSize + 1, 0xFF1F1408);
        // v0.26.0: alphas con tope 255 (withAlpha no clampea; el hover suma).
        int lightAlpha = Math.min(255, (int)(flicker * (200 + 50 * hover)));
        g.fill(lx - boxSize, lanternY - 3, lx + boxSize, lanternY + boxSize,
            AtmosphereRenderer.withAlpha(0x00FFAA33, lightAlpha));
        int coreAlpha = Math.min(255, (int)(flicker * 235));
        g.fill(lx - 2, lanternY, lx + 3, lanternY + 3,
            AtmosphereRenderer.withAlpha(0x00FFEE88, coreAlpha));
        g.fill(lx, lanternY - 3, lx + 1, lanternY + boxSize, 0xFF2A1810);
        g.fill(lx - boxSize, lanternY + 1, lx + boxSize, lanternY + 2, 0xFF2A1810);
        g.fill(lx - boxSize - 1, lanternY + boxSize + 1, lx + boxSize + 1, lanternY + boxSize + 3, 0xFF1A1108);

        // Optimized halos: 2 gradients instead of 12+ fills
        // v0.26.0: el halo respira mas amplio bajo la mano del lector.
        int haloR = (int)(28 * flicker * (1f + 0.5f * hover));
        int haloAlpha = Math.min(255, (int)(flicker * 32 + hover * 34));
        g.fill(lx - haloR, ly - haloR / 4, lx + haloR, ly + haloR / 4,
            AtmosphereRenderer.withAlpha(0x00FFAA33, haloAlpha));
        int floorR = (int)(48 * flicker * (1f + 0.35f * hover));
        int floorA = Math.min(255, (int)(flicker * 16 + hover * 14));
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
        // v0.24.0: noche de tormenta - segunda pasada de gotas, inclinacion y brillo.
        boolean storm = com.eternalcraft.gripeverde.client.audio.MenuAmbience.isStormNight();
        int passes = storm ? 2 : 1;
        int color = storm ? 0x36B8A88C : DiaryTheme.RAIN_LINE;
        float gust = Math.max(windGustIntensity, storm ? 0.45f : 0f);
        int slant = 1 + (int)(gust * 5f);
        for (int pass = 0; pass < passes; pass++) {
            for (int i = 0; i < RAIN_COUNT; i++) {
                int idx = (i + pass * 17) % RAIN_COUNT;
                float ox = pass == 0 ? 0f : 0.011f;
                int rx = (int)((rain[idx][0] + ox) * w);
                int ry = (int)(rain[idx][1] * h);
                g.fill(rx, ry, rx + 1, ry + 4, color);
                g.fill(rx + slant, ry + 4, rx + slant + 1, ry + 8, color);
            }
        }
        renderRainSplashes(g, w, h);
    }

    /** v0.24.0: velo oscuro de tormenta sobre la calle (titulo y pergaminos). */
    public static void renderStormVeil(GuiGraphics g, int w, int h) {
        if (!com.eternalcraft.gripeverde.client.audio.MenuAmbience.isStormNight()) return;
        g.fill(0, 0, w, h, 0x15070A16);
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
