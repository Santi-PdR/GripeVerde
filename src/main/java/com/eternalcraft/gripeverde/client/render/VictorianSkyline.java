package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VictorianSkyline {

    private static float time = 0f;

    /**
     * v0.188.0: la tercera capa, la mas lejana: tejados y torres apenas insinuados
     * sobre la niebla. Da profundidad a la ciudad que queda a la vista ahora que
     * la portada no tiene periodicos encima. Formato {x, baseY, ancho, alto}.
     */
    private static final float[][] FAR_BUILDINGS = {
        {-0.02f, 0.60f, 0.07f, 0.14f}, {0.05f, 0.62f, 0.05f, 0.10f},
        {0.11f, 0.58f, 0.06f, 0.17f}, {0.19f, 0.61f, 0.08f, 0.12f},
        {0.28f, 0.59f, 0.05f, 0.15f}, {0.35f, 0.63f, 0.09f, 0.10f},
        {0.45f, 0.57f, 0.06f, 0.18f}, {0.52f, 0.61f, 0.07f, 0.13f},
        {0.60f, 0.58f, 0.05f, 0.16f}, {0.67f, 0.62f, 0.08f, 0.11f},
        {0.76f, 0.57f, 0.06f, 0.19f}, {0.83f, 0.60f, 0.07f, 0.14f},
        {0.91f, 0.58f, 0.07f, 0.16f}, {0.97f, 0.62f, 0.06f, 0.12f},
    };

    private static final float[][] BG_BUILDINGS = {
        {0.00f, 0.70f, 0.08f, 0.22f}, {0.07f, 0.75f, 0.06f, 0.15f},
        {0.12f, 0.65f, 0.07f, 0.26f}, {0.18f, 0.72f, 0.09f, 0.18f},
        {0.26f, 0.68f, 0.06f, 0.22f}, {0.31f, 0.74f, 0.10f, 0.16f},
        {0.40f, 0.66f, 0.07f, 0.24f}, {0.46f, 0.71f, 0.08f, 0.19f},
        {0.53f, 0.67f, 0.06f, 0.23f}, {0.58f, 0.73f, 0.09f, 0.17f},
        {0.66f, 0.69f, 0.07f, 0.21f}, {0.72f, 0.74f, 0.08f, 0.16f},
        {0.79f, 0.65f, 0.06f, 0.25f}, {0.84f, 0.71f, 0.10f, 0.19f},
        {0.93f, 0.68f, 0.07f, 0.22f},
    };

    private static final float[][] FG_BUILDINGS = {
        {0.00f, 0.82f, 0.14f, 0.18f}, {0.13f, 0.79f, 0.10f, 0.21f},
        {0.22f, 0.84f, 0.16f, 0.16f}, {0.37f, 0.80f, 0.12f, 0.20f},
        {0.48f, 0.83f, 0.09f, 0.17f}, {0.56f, 0.78f, 0.14f, 0.22f},
        {0.69f, 0.82f, 0.12f, 0.18f}, {0.80f, 0.80f, 0.10f, 0.20f},
        {0.89f, 0.84f, 0.11f, 0.16f},
    };

    private static final float[][] CHIMNEYS_BG = {
        {0.3f,2,0.011f,0.09f}, {0.7f,2,0.009f,0.07f},
        {0.4f,4,0.012f,0.08f}, {0.2f,6,0.010f,0.10f},
        {0.6f,6,0.008f,0.06f}, {0.3f,8,0.011f,0.08f},
        {0.5f,10,0.009f,0.07f},{0.7f,12,0.012f,0.09f},
        {0.4f,14,0.010f,0.07f},
    };

    private static final float[][] CHIMNEYS_FG = {
        {0.2f, 1, 0.014f, 0.11f}, {0.7f, 1, 0.012f, 0.09f},
        {0.3f, 3, 0.015f, 0.12f}, {0.5f, 5, 0.013f, 0.10f},
        {0.8f, 5, 0.011f, 0.08f}, {0.4f, 7, 0.014f, 0.11f},
    };

    private static final long WINDOW_SEED_FG = 0xDEADBEEFL;
    private static final long WINDOW_SEED_BG = 0xCAFEBABEL;

    // Reused deterministic generators avoid per-frame allocations without changing the scene.
    private static final java.util.Random RND = new java.util.Random(0x5EEDF00DL);
    private static final java.util.Random WINDOW_RNG = new java.util.Random(WINDOW_SEED_FG);

    private static boolean reducedMotion() {
        try {
            return com.eternalcraft.gripeverde.config.ClientConfig.REDUCED_MOTION.get();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    private static final int TOTAL_CHIMNEYS = CHIMNEYS_BG.length + CHIMNEYS_FG.length;
    private static final float[][] smokeParticles = new float[TOTAL_CHIMNEYS * 8][4];
    private static boolean smokeInit = false;

    // Animated windows: 5 FG windows toggling on/off with distinct timers
    private static final int ANIM_COUNT = 5;
    private static final float[] animPhase = new float[ANIM_COUNT];
    private static final float[] animTimer = new float[ANIM_COUNT];
    private static final boolean[] animOn = new boolean[ANIM_COUNT];
    // Positions: [building][row][col] for each animated window
    private static final int[][] ANIM_WINDOWS = {
        {2, 1, 1},  // original
        {0, 2, 0},
        {1, 0, 1},
        {3, 1, 2},
        {2, 3, 0}
    };
    private static boolean animWindowsInit = false;

    public static void tick(float deltaTime) {
        initSmoke();

        // Animate multiple windows with distinct timers
        if (!animWindowsInit) {
            for (int i = 0; i < ANIM_COUNT; i++) {
                animTimer[i] = (float)(RND.nextFloat() * 15.0);
                animOn[i] = RND.nextFloat() > 0.5;
                animPhase[i] = animOn[i] ? 1f : 0f;
            }
            animWindowsInit = true;
        }
        if (reducedMotion()) return;

        time += deltaTime;
        tickSmoke(deltaTime);
        float speed = 2f * deltaTime;
        for (int i = 0; i < ANIM_COUNT; i++) {
            animTimer[i] -= deltaTime;
            if (animTimer[i] <= 0f) {
                animOn[i] = !animOn[i];
                animTimer[i] = 6f + (float)(RND.nextFloat() * 12.0);
            }
            float target = animOn[i] ? 1f : 0f;
            if (animPhase[i] < target) {
                animPhase[i] = Math.min(target, animPhase[i] + speed);
            } else {
                animPhase[i] = Math.max(target, animPhase[i] - speed);
            }
        }
    }

    // v0.206.0: las fachadas -posicion, alto y ancho escalados- solo cambian con
    // el tamano de la pantalla. Se resuelven una vez por resolucion y los tejados
    // dejan de rehacer las multiplicaciones en cada cuadro.
    private static int facadeLayoutW = -1;
    private static int facadeLayoutH = -1;
    private static int[] bgX1 = new int[0];
    private static int[] bgX2 = new int[0];
    private static int[] bgY1 = new int[0];
    private static int[] bgY2 = new int[0];
    private static int[] fgX1 = new int[0];
    private static int[] fgX2 = new int[0];
    private static int[] fgY1 = new int[0];
    private static int[] fgY2 = new int[0];
    private static float[] fgScaledW = new float[0];
    private static float[] fgScaledH = new float[0];

    private static void ensureFacadeLayout(int w, int h) {
        if (facadeLayoutW == w && facadeLayoutH == h) return;
        facadeLayoutW = w;
        facadeLayoutH = h;
        if (bgX1.length != BG_BUILDINGS.length) {
            bgX1 = new int[BG_BUILDINGS.length];
            bgX2 = new int[BG_BUILDINGS.length];
            bgY1 = new int[BG_BUILDINGS.length];
            bgY2 = new int[BG_BUILDINGS.length];
            fgX1 = new int[FG_BUILDINGS.length];
            fgX2 = new int[FG_BUILDINGS.length];
            fgY1 = new int[FG_BUILDINGS.length];
            fgY2 = new int[FG_BUILDINGS.length];
            fgScaledW = new float[FG_BUILDINGS.length];
            fgScaledH = new float[FG_BUILDINGS.length];
        }
        for (int i = 0; i < BG_BUILDINGS.length; i++) {
            float[] b = BG_BUILDINGS[i];
            bgX1[i] = (int)(w * b[0]);
            bgY2[i] = (int)(h * b[1]);
            bgX2[i] = bgX1[i] + (int)(w * b[2]);
            bgY1[i] = bgY2[i] - (int)(h * b[3]);
        }
        for (int i = 0; i < FG_BUILDINGS.length; i++) {
            float[] b = FG_BUILDINGS[i];
            fgScaledW[i] = w * b[2];
            fgScaledH[i] = h * b[3];
            fgX1[i] = (int)(w * b[0]);
            fgY2[i] = (int)(h * b[1]);
            fgX2[i] = fgX1[i] + (int)fgScaledW[i];
            fgY1[i] = fgY2[i] - (int)fgScaledH[i];
        }
    }

    public static void renderBackground(GuiGraphics g, int w, int h) {
        renderFarSkyline(g, w, h);
        ensureFacadeLayout(w, h);
        int colorBg = 0xFF1A1208;
        for (int i = 0; i < bgX1.length; i++) {
            int x1 = bgX1[i];
            int y2 = bgY2[i];
            int x2 = bgX2[i];
            int y1 = bgY1[i];
            g.fill(x1, y1, x2, y2, colorBg);
            int roofColor = 0xFF2A1E10;
            for (int px = x1; px < x2; px += 5) {
                g.fill(px, y1 - 2, px + 3, y1, roofColor);
            }
        }
        renderWindowsOnBuildings(g, w, h, BG_BUILDINGS, true);
        renderChimneys(g, w, h, CHIMNEYS_BG, BG_BUILDINGS, false);
    }

    /**
     * v0.188.0: capa lejana de la ciudad. Dos fills por tejado y seis ventanas
     * fijas: cuesta casi nada y da la sensacion de que la avenida sigue mas alla
     * de la niebla.
     */
    private static void renderFarSkyline(GuiGraphics g, int w, int h) {
        int farWall = 0xFF2A2116;
        int farEdge = 0xFF332A1C;
        for (float[] b : FAR_BUILDINGS) {
            int x1 = (int) (w * b[0]);
            int y2 = (int) (h * b[1]);
            int x2 = x1 + (int) (w * b[2]);
            int y1 = y2 - (int) (h * b[3]);
            if (x2 <= 0 || x1 >= w) continue;
            g.fill(x1, y1, x2, Math.min(y2, (int) (h * 0.72f)), farWall);
            g.fill(x1, y1, x2, y1 + 1, farEdge);
        }
        // Ventanas lejanas: puntos fijos, sin animacion (no compiten con las de
        // la ciudad cercana).
        int lit = 0x3A4A2E14;
        for (int i = 0; i < 6; i++) {
            int x = (int) (w * (0.09f + i * 0.155f));
            int y = (int) (h * (0.53f + (i % 3) * 0.02f));
            g.fill(x, y, x + 2, y + 1, lit);
        }
    }

    /**
     * A single, fixed pair of Ghoul eyes watches from a distant upper window.
     * Called only by the title scene; it never overlays a live world.
     */
    public static void renderGhoulWatcher(GuiGraphics g, int w, int h) {
        if (w < 160 || h < 120) return;
        float[] b = BG_BUILDINGS[1];
        int bx1 = (int)(w * b[0]);
        int by2 = (int)(h * b[1]);
        int bx2 = bx1 + (int)(w * b[2]);
        int by1 = by2 - (int)(h * b[3]);
        int bWidth = bx2 - bx1;
        int bHeight = by2 - by1;
        int cols = Math.max(1, bWidth / 10);
        int rows = Math.max(1, bHeight / 12);
        if (w > 1400) {
            cols = Math.max(1, cols / 2);
            rows = Math.max(1, rows / 2);
        }

        int cellW = bWidth / cols;
        int cellH = bHeight / rows;
        int eyeW = Math.max(2, Math.min(3, cellW - 4));
        int eyeH = Math.max(2, Math.min(3, cellH - 5));
        int wx = bx1 + (cellW - eyeW) / 2;
        int wy = by1 + (cellH - eyeH) / 2 + 3;

        g.fill(wx - 1, wy - 1, wx + eyeW + 1, wy + eyeH + 1, 0xFF130D08);
        g.fill(wx, wy, wx + eyeW, wy + eyeH, 0xFF030405);
        int eyeY = wy + eyeH / 2;
        g.fill(wx, eyeY, wx + 1, eyeY + 1, DiaryTheme.GHOUL_EYE);
        g.fill(wx + eyeW - 1, eyeY, wx + eyeW, eyeY + 1, DiaryTheme.GHOUL_EYE_GLOW);
    }

    // ── v0.16.0: la ventana que responde (atico del edificio 5) ────────────
    private static int signalResponseCount = 0;
    private static long signalRespondUntilMs = 0L;
    private static boolean signalMemoryFlagsLoaded = false;
    private static boolean signalCurtainsClosed = false;
    private static boolean thirteenthWindowLit = false;

    /** Lee una sola vez por sesion los dos recuerdos usados por el cielo animado. */
    private static void loadSignalMemoryFlags() {
        if (signalMemoryFlagsLoaded) return;
        try {
            signalCurtainsClosed = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("curtains_closed") > 0;
            thirteenthWindowLit = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("thirteenth_window") > 0;
            signalMemoryFlagsLoaded = true;
        } catch (Throwable ignored) {}
    }

    private static boolean areSignalCurtainsClosed() {
        loadSignalMemoryFlags();
        return signalCurtainsClosed;
    }

    private static boolean isThirteenthWindowLit() {
        loadSignalMemoryFlags();
        return thirteenthWindowLit;
    }

    /** Invalida la copia en memoria cuando un acertijo cambia el recuerdo del cielo. */
    public static void invalidateSignalMemoryFlags() {
        signalMemoryFlagsLoaded = false;
    }

    /** Rect de la ventana-senal del atico (edificio FG 5). */
    public static int[] signalWindowRect(int w, int h) {
        float[] b = FG_BUILDINGS[5];
        int bx1 = (int)(w * b[0]);
        int by2 = (int)(h * b[1]);
        int bw = (int)(w * b[2]);
        int bh = (int)(h * b[3]);
        int by1 = by2 - bh;
        int wx = bx1 + (int)(bw * 0.28f);
        int wy = by1 + Math.max(3, bh / 9);
        return new int[]{ wx, wy, 6, 8 };
    }

    public static boolean isSignalWindowLit(float t) {
        // Reduced motion keeps this clue steady; it must not use the wall-clock blink.
        if (reducedMotion()) return (t % 4.2f) < 1.35f;

        long now = System.currentTimeMillis();
        if (now < signalRespondUntilMs) {
            // Doble parpadeo rapido de respuesta
            return ((now / 160L) % 2L) == 0L;
        }
        return (t % 4.2f) < 1.35f;
    }

    /** El lector senala la ventana. Devuelve true si la ventana respondio. */
    public static boolean signalWindowClicked() {
        long now = System.currentTimeMillis();
        if (areSignalCurtainsClosed()) return false;
        if (!isSignalWindowLit(time)) return false;
        signalResponseCount++;
        signalRespondUntilMs = now + 1500L;
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_TICK.get(), 0.10f, 1.8f);
        } catch (Throwable ignored) {}
        if (signalResponseCount >= 3) {
            try {
                com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.setCounter("curtains_closed", 1);
                signalCurtainsClosed = true;
                signalMemoryFlagsLoaded = true;
                com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                    com.eternalcraft.gripeverde.client.lore.Discoveries.SIGNAL_WINDOW);
            } catch (Throwable ignored) {}
        }
        return true;
    }

    /** Rect del gato del tejado (edificio FG 5). */
    public static int[] catRect(int w, int h) {
        float[] b = FG_BUILDINGS[5];
        int bx1 = (int)(w * b[0]);
        int by2 = (int)(h * b[1]);
        int bw = (int)(w * b[2]);
        int bh = (int)(h * b[3]);
        int by1 = by2 - bh;
        int cx = bx1 + (int)(bw * 0.65f);
        int cy = by1 - 3;
        return new int[]{ cx - 3, cy - 4, 10, 7 };
    }

    public static void renderForeground(GuiGraphics g, int w, int h) {
        int colorFg = 0xFF0F0805;
        ensureFacadeLayout(w, h);
        for (int i = 0; i < fgX1.length; i++) {
            int x1 = fgX1[i];
            int y2 = fgY2[i];
            int x2 = fgX2[i];
            int y1 = fgY1[i];
            g.fill(x1, y1, x2, y2, colorFg);
            int roofColor = 0xFF1F1408;
            for (int px = x1; px < x2; px += 4) {
                g.fill(px, y1 - 3, px + 3, y1, roofColor);
            }
            g.fill(x1 - 2, y1, x2 + 2, y1 + 2, 0xFF2A1E10);
            int doorW = Math.max(3, (int)(fgScaledW[i] * 0.15f));
            int doorH = Math.max(6, (int)(fgScaledH[i] * 0.15f));
            int doorX = x1 + (x2 - x1) / 2 - doorW / 2;
            int doorY = y2 - doorH;
            g.fill(doorX, doorY, doorX + doorW, y2, 0xFF080503);
            g.fill(doorX - 1, y2 - 2, doorX + doorW + 1, y2,
                AtmosphereRenderer.withAlpha(0x00D4941E, 60));
        }
        renderWindowsOnBuildings(g, w, h, FG_BUILDINGS, false);
        renderSignalWindow(g, w, h);
        renderThirteenthFloor(g, w, h);
        renderChimneys(g, w, h, CHIMNEYS_FG, FG_BUILDINGS, true);
        renderSmoke(g, w, h);
        renderCrack(g, w, h);
        renderRoofCat(g, w, h);
        renderWeathervane(g, w, h);
    }

    /** La ventana del atico que parpadea esperando respuesta. */
    private static void renderSignalWindow(GuiGraphics g, int w, int h) {
        float[] b = FG_BUILDINGS[5];
        int bx1 = (int)(w * b[0]);
        int by2 = (int)(h * b[1]);
        int bw = (int)(w * b[2]);
        int bh = (int)(h * b[3]);
        int by1 = by2 - bh;
        int wx = bx1 + (int)(bw * 0.28f);
        int wy = by1 + Math.max(3, bh / 9);
        int ww = 6, wh = 8;
        if (areSignalCurtainsClosed()) {
            // Cortinas echadas para siempre
            g.fill(wx - 1, wy - 1, wx + ww + 1, wy + wh + 1, 0xFF1A0F05);
            g.fill(wx, wy, wx + ww, wy + wh, 0xFF141008);
            g.fill(wx, wy + wh / 2, wx + ww, wy + wh / 2 + 1, 0xFF2A1E10);
            g.fill(wx + ww / 2, wy, wx + ww / 2 + 1, wy + wh, 0xFF2A1E10);
            return;
        }
        boolean lit = isSignalWindowLit(time);
        g.fill(wx - 1, wy - 1, wx + ww + 1, wy + wh + 1, 0xFF1A0F05);
        if (lit) {
            int amber = 170;
            g.fill(wx, wy, wx + ww, wy + wh, AtmosphereRenderer.withAlpha(0x00D4941E, amber));
            g.fill(wx + ww / 2, wy, wx + ww / 2 + 1, wy + wh, 0xFF3D2E1A);
            g.fill(wx, wy + wh / 2, wx + ww, wy + wh / 2 + 1, 0xFF3D2E1A);
            g.fill(wx - 2, wy - 2, wx + ww + 2, wy + wh + 2,
                AtmosphereRenderer.withAlpha(0x00D4941E, amber / 7));
        } else {
            g.fill(wx, wy, wx + ww, wy + wh, 0xFF050302);
            g.fill(wx, wy, wx + ww, wy + 1, 0xFF1A1008);
        }
    }

    /** Consecuencia permanente: la ventana del piso que no existe. */
    private static void renderThirteenthFloor(GuiGraphics g, int w, int h) {
        if (!isThirteenthWindowLit()) return;
        float[] b = FG_BUILDINGS[1];
        int bx1 = (int)(w * b[0]);
        int by2 = (int)(h * b[1]);
        int bw = (int)(w * b[2]);
        int bh = (int)(h * b[3]);
        int by1 = by2 - bh;
        // Un piso mas arriba de la cornisa, donde no hay pared.
        int wx = bx1 + (int)(bw * 0.55f);
        int wy = by1 - 12;
        float fl = (float)(Math.sin(time * 0.9f) * 0.2f + 0.8f);
        int amber = (int)(120 * fl);
        g.fill(wx - 1, wy - 1, wx + 6, wy + 8, AtmosphereRenderer.withAlpha(0x001A0F05, 180));
        g.fill(wx, wy, wx + 5, wy + 7, AtmosphereRenderer.withAlpha(0x00C8A83E, amber));
        g.fill(wx + 2, wy, wx + 3, wy + 7, 0xFF1A1008);
        g.fill(wx - 3, wy - 3, wx + 8, wy + 10, AtmosphereRenderer.withAlpha(0x00C8A83E, amber / 8));
    }

    /** Veleta sobre el edificio mas alto: obedece al viento. */
    private static void renderWeathervane(GuiGraphics g, int w, int h) {
        float[] b = FG_BUILDINGS[5];
        int bx1 = (int)(w * b[0]);
        int by2 = (int)(h * b[1]);
        int bw = (int)(w * b[2]);
        int bh = (int)(h * b[3]);
        int by1 = by2 - bh;
        int vx = bx1 + (int)(bw * 0.12f);
        int vy = by1 - 4;
        // Mástil
        g.fill(vx, vy - 12, vx + 1, vy, 0xFF1F1408);
        g.fill(vx - 2, vy - 1, vx + 3, vy + 1, 0xFF1F1408);
        // La flecha gira con el viento (mas rapido durante las rafagas)
        float gust = LivingAtmosphere.getWindGustIntensity();
        double angle = time * (0.25f + gust * 2.6f);
        int dx = (int)(Math.cos(angle) * 5);
        int dy = (int)(Math.sin(angle) * 1.6);
        int ay = vy - 12;
        g.fill(vx - dx, ay - dy, vx + dx + 1, ay - dy + 1, 0xFF3D2E1A);
        g.fill(vx + dx, ay - dy - 2, vx + dx + 2, ay - dy, 0xFF5A4028); // punta
        g.fill(vx, ay - 1, vx + 1, ay + 1, 0xFF8E6A35); // eje
        // Descubrimiento: ver la veleta obedecer a una rafaga fuerte
        if (gust > 0.62f) {
            long nowMs = System.currentTimeMillis();
            if (nowMs - LivingAtmosphere.streetLevelRenderMs() < 400L) {
                try {
                    com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                        com.eternalcraft.gripeverde.client.lore.Discoveries.WEATHERVANE);
                } catch (Throwable ignored) {}
            }
        }
    }

    // v0.197.0: el trazado de las ventanas (posicion, tamano y suerte de cada
    // una) solo depende del tamano de la pantalla. Se resuelve una vez por
    // resolucion y se reutiliza: fuera el RNG, las divisiones y el reparto de
    // filas y columnas de cada cuadro. El dibujo queda en un fill por ventana.
    private static final int WIN_OFF = 0;
    private static final int WIN_LIT = 1;
    private static final int WIN_OTHER = 2;

    private static int layoutW = -1;
    private static int layoutH = -1;

    private static int[] winFgX = new int[0];
    private static int[] winFgY = new int[0];
    private static int[] winFgW = new int[0];
    private static int[] winFgH = new int[0];
    private static int[] winFgKind = new int[0];
    private static int[] winFgAnim = new int[0];
    private static float[] winFgFreqAmber = new float[0];
    private static float[] winFgFreqSick = new float[0];
    private static int winFgCount = 0;

    private static int[] winBgX = new int[0];
    private static int[] winBgY = new int[0];
    private static int[] winBgW = new int[0];
    private static int[] winBgH = new int[0];
    private static int[] winBgColor = new int[0];
    private static int winBgCount = 0;

    private static void renderWindowsOnBuildings(GuiGraphics g, int w, int h, float[][] buildings, boolean isBg) {
        ensureWindowLayout(w, h);
        if (isBg) {
            // Las ventanas del fondo no parpadean: su color ya viene resuelto.
            for (int i = 0; i < winBgCount; i++) {
                int wx = winBgX[i];
                int wy = winBgY[i];
                g.fill(wx, wy, wx + winBgW[i], wy + winBgH[i], winBgColor[i]);
            }
            return;
        }
        for (int i = 0; i < winFgCount; i++) {
            int wx = winFgX[i];
            int wy = winFgY[i];
            int ww = winFgW[i];
            int wh = winFgH[i];
            int anim = winFgAnim[i];
            if (anim >= 0) {
                int amber = (int)(animPhase[anim] * 160);
                if (amber > 2) {
                    g.fill(wx - 1, wy - 1, wx + ww + 1, wy + wh + 1, 0xFF1A0F05);
                    g.fill(wx, wy, wx + ww, wy + wh,
                        AtmosphereRenderer.withAlpha(0x00D4941E, amber));
                    g.fill(wx + ww / 2, wy, wx + ww / 2 + 1, wy + wh, 0xFF3D2E1A);
                    g.fill(wx - 2, wy - 2, wx + ww + 2, wy + wh + 2,
                        AtmosphereRenderer.withAlpha(0x00D4941E, amber / 6));
                } else {
                    g.fill(wx, wy, wx + ww, wy + wh, 0xFF050302);
                    g.fill(wx, wy, wx + ww, wy + 1, 0xFF1A1008);
                }
                continue;
            }
            int kind = winFgKind[i];
            if (kind == WIN_OFF) {
                g.fill(wx, wy, wx + ww, wy + wh, 0xFF050302);
                g.fill(wx, wy, wx + ww, wy + 1, 0xFF1A1008);
            } else if (kind == WIN_LIT) {
                float flicker = (float)(Math.sin(time * winFgFreqAmber[i]) * 0.3f + 0.7f);
                int amber = Math.max(40, (int)(flicker * 140));
                g.fill(wx - 1, wy - 1, wx + ww + 1, wy + wh + 1, 0xFF1A0F05);
                g.fill(wx, wy, wx + ww, wy + wh,
                    AtmosphereRenderer.withAlpha(0x00D4941E, amber));
                g.fill(wx + ww / 2, wy, wx + ww / 2 + 1, wy + wh, 0xFF3D2E1A);
                g.fill(wx, wy + wh / 2, wx + ww, wy + wh / 2 + 1, 0xFF3D2E1A);
                g.fill(wx - 2, wy - 2, wx + ww + 2, wy + wh + 2,
                    AtmosphereRenderer.withAlpha(0x00D4941E, amber / 6));
            } else {
                float sickFlicker = (float)(Math.sin(time * winFgFreqSick[i]) * 0.4f + 0.6f);
                int sickAlpha = Math.max(30, (int)(sickFlicker * 100));
                g.fill(wx - 1, wy - 1, wx + ww + 1, wy + wh + 1, 0xFF1A0F05);
                g.fill(wx, wy, wx + ww, wy + wh,
                    AtmosphereRenderer.withAlpha(0x005A7038, sickAlpha));
                g.fill(wx + ww / 2, wy, wx + ww / 2 + 1, wy + wh, 0xFF1A2810);
                g.fill(wx, wy + wh / 2, wx + ww, wy + wh / 2 + 1, 0xFF1A2810);
                g.fill(wx - 2, wy - 2, wx + ww + 2, wy + wh + 2,
                    AtmosphereRenderer.withAlpha(0x003A5028, sickAlpha / 5));
            }
        }
    }

    private static void ensureWindowLayout(int w, int h) {
        if (layoutW == w && layoutH == h) return;
        layoutW = w;
        layoutH = h;
        java.util.ArrayList<int[]> bg = new java.util.ArrayList<>();
        java.util.ArrayList<int[]> fg = new java.util.ArrayList<>();
        java.util.ArrayList<float[]> fgFreq = new java.util.ArrayList<>();
        collectWindows(w, h, BG_BUILDINGS, true, bg, null);
        collectWindows(w, h, FG_BUILDINGS, false, fg, fgFreq);
        bg.trimToSize();
        fg.trimToSize();
        fgFreq.trimToSize();

        winBgCount = bg.size();
        winBgX = new int[winBgCount];
        winBgY = new int[winBgCount];
        winBgW = new int[winBgCount];
        winBgH = new int[winBgCount];
        winBgColor = new int[winBgCount];
        for (int i = 0; i < winBgCount; i++) {
            int[] e = bg.get(i);
            winBgX[i] = e[0];
            winBgY[i] = e[1];
            winBgW[i] = e[2];
            winBgH[i] = e[3];
            winBgColor[i] = e[4];
        }

        winFgCount = fg.size();
        winFgX = new int[winFgCount];
        winFgY = new int[winFgCount];
        winFgW = new int[winFgCount];
        winFgH = new int[winFgCount];
        winFgKind = new int[winFgCount];
        winFgAnim = new int[winFgCount];
        winFgFreqAmber = new float[winFgCount];
        winFgFreqSick = new float[winFgCount];
        for (int i = 0; i < winFgCount; i++) {
            int[] e = fg.get(i);
            winFgX[i] = e[0];
            winFgY[i] = e[1];
            winFgW[i] = e[2];
            winFgH[i] = e[3];
            winFgKind[i] = e[4];
            winFgAnim[i] = e[5];
            float[] f = fgFreq.get(i);
            winFgFreqAmber[i] = f[0];
            winFgFreqSick[i] = f[1];
        }
    }

    private static void collectWindows(int w, int h, float[][] buildings, boolean isBg,
            java.util.ArrayList<int[]> out, java.util.ArrayList<float[]> freqs) {
        long seed = isBg ? WINDOW_SEED_BG : WINDOW_SEED_FG;
        java.util.Random rng = WINDOW_RNG;
        rng.setSeed(seed);
        int offOnThreshold = isBg ? 8 : 7;
        int amberThreshold  = 9;
        // LOD: reduce density on large resolutions to save fill calls.
        // v0.191.0: las ventanas son el gasto mas grande que quedaba en el fondo
        // (mas de mil rellenos por cuadro a 1080p). A partir de 1600 px de ancho
        // se dibuja una de cada tres: la fachada se lee igual y cuesta un tercio.
        int lodSkip = 1;
        if (w > 1600) lodSkip = 3;
        else if (w > 1200) lodSkip = 2;
        else if (h < 400) lodSkip = 2;
        for (int bi = 0; bi < buildings.length; bi++) {
            float[] b = buildings[bi];
            int bx1 = (int)(w * b[0]);
            int by2 = (int)(h * b[1]);
            int bx2 = bx1 + (int)(w * b[2]);
            int by1 = by2 - (int)(h * b[3]);
            int bWidth  = bx2 - bx1;
            int bHeight = by2 - by1;
            int cols = Math.max(1, bWidth  / (isBg ? 10 : 16));
            int rows = Math.max(1, bHeight / (isBg ? 12 : 18));
            if (isBg && w > 1400) {
                cols = Math.max(1, cols / 2);
                rows = Math.max(1, rows / 2);
            }

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    // Resolve once; animated windows bypass LOD and reuse the same index below.
                    int animIdx = -1;
                    if (!isBg) {
                        for (int aw = 0; aw < ANIM_COUNT; aw++) {
                            if (ANIM_WINDOWS[aw][0] == bi
                                && ANIM_WINDOWS[aw][1] == row
                                && ANIM_WINDOWS[aw][2] == col) {
                                animIdx = aw;
                                break;
                            }
                        }
                    }
                    if (animIdx < 0 && lodSkip > 1 && ((row + col) % lodSkip != 0)) {
                        // Still consume RNG to keep determinism
                        rng.nextInt(2); // winW
                        rng.nextInt(2); // winH
                        rng.nextInt(10); // roll
                        if (!isBg) { rng.nextInt(3); rng.nextInt(3); }
                        continue;
                    }
                    int winW, winH;
                    if (isBg) {
                        winW = 2 + rng.nextInt(2);
                        winH = 2 + rng.nextInt(2);
                    } else {
                        winW = 4 + rng.nextInt(3);
                        winH = 5 + rng.nextInt(3);
                    }
                    int wx = bx1 + (col * bWidth / cols) + (bWidth / cols / 2) - winW / 2;
                    int wy = by1 + (row * bHeight / rows) + (bHeight / rows / 2) - winH / 2 + 3;
                    int roll = rng.nextInt(10);

                    if (isBg) {
                        int color;
                        if (roll < offOnThreshold) {
                            color = 0xFF050302;
                        } else if (roll < amberThreshold) {
                            // v0.191.0: tres tonos de luz segun el sitio en la
                            // fachada. Cuesta lo mismo y la ciudad deja de tener
                            // todas las ventanas del mismo ambar.
                            int tone = Math.floorMod(row * 7 + col * 13 + bi * 5, 6);
                            int tint;
                            int toneAlpha;
                            if (tone == 0) {
                                tint = 0x00C8B060; toneAlpha = 96;      // gas frio
                            } else if (tone == 1) {
                                tint = 0x00E0A83C; toneAlpha = 128;     // ambar pleno
                            } else if (tone == 2) {
                                tint = 0x008A6A20; toneAlpha = 88;      // vela vieja
                            } else {
                                tint = 0x00A8781E; toneAlpha = 110;
                            }
                            color = AtmosphereRenderer.withAlpha(tint, toneAlpha);
                        } else {
                            color = AtmosphereRenderer.withAlpha(0x004A6028, 80);
                        }
                        out.add(new int[] { wx, wy, winW, winH, color });
                        continue;
                    }

                    int kind;
                    if (animIdx >= 0) {
                        kind = WIN_LIT;
                    } else if (roll < offOnThreshold) {
                        kind = WIN_OFF;
                    } else if (roll < amberThreshold) {
                        kind = WIN_LIT;
                    } else {
                        kind = WIN_OTHER;
                    }
                    out.add(new int[] { wx, wy, winW, winH, kind, animIdx });
                    if (freqs != null) {
                        freqs.add(new float[] { 0.5f + bi * 0.13f + col * 0.17f, 0.8f + bi * 0.2f });
                    }
                }
            }
        }
    }

    // v0.205.0: el cuerpo de cada chimenea se resolvia en cada cuadro (padre,
    // escalas y restas). Depende solo del tamano de la pantalla, asi que se
    // guarda por resolucion igual que las bocas de humo.
    private static int chimLayoutW = -1;
    private static int chimLayoutH = -1;
    private static int[] chimBgX1 = new int[0];
    private static int[] chimBgX2 = new int[0];
    private static int[] chimBgY1 = new int[0];
    private static int[] chimBgY2 = new int[0];
    private static int[] chimFgX1 = new int[0];
    private static int[] chimFgX2 = new int[0];
    private static int[] chimFgY1 = new int[0];
    private static int[] chimFgY2 = new int[0];

    private static void ensureChimneyLayout(int w, int h) {
        if (chimLayoutW == w && chimLayoutH == h) return;
        chimLayoutW = w;
        chimLayoutH = h;
        if (chimBgX1.length != CHIMNEYS_BG.length) {
            chimBgX1 = new int[CHIMNEYS_BG.length];
            chimBgX2 = new int[CHIMNEYS_BG.length];
            chimBgY1 = new int[CHIMNEYS_BG.length];
            chimBgY2 = new int[CHIMNEYS_BG.length];
            chimFgX1 = new int[CHIMNEYS_FG.length];
            chimFgX2 = new int[CHIMNEYS_FG.length];
            chimFgY1 = new int[CHIMNEYS_FG.length];
            chimFgY2 = new int[CHIMNEYS_FG.length];
        }
        fillChimneyRects(w, h, CHIMNEYS_BG, BG_BUILDINGS, chimBgX1, chimBgX2, chimBgY1, chimBgY2);
        fillChimneyRects(w, h, CHIMNEYS_FG, FG_BUILDINGS, chimFgX1, chimFgX2, chimFgY1, chimFgY2);
    }

    private static void fillChimneyRects(int w, int h, float[][] chimneys, float[][] parents,
            int[] x1s, int[] x2s, int[] y1s, int[] y2s) {
        for (int ci = 0; ci < chimneys.length; ci++) {
            float[] c = chimneys[ci];
            int idx = (int) c[1];
            x1s[ci] = NO_ANCHOR;
            if (idx >= parents.length) continue;
            float[] parent = parents[idx];
            int px1 = (int)(w * parent[0]);
            int py2 = (int)(h * parent[1]);
            int pw  = (int)(w * parent[2]);
            int ph  = (int)(h * parent[3]);
            int py1 = py2 - ph;
            int cx1 = px1 + (int)(pw * c[0]);
            int cx2 = cx1 + (int)(w * c[2]);
            int cy2 = py1;
            int cy1 = cy2 - (int)(h * c[3]);
            x1s[ci] = cx1;
            x2s[ci] = cx2;
            y1s[ci] = cy1;
            y2s[ci] = cy2;
        }
    }

    private static void renderChimneys(GuiGraphics g, int w, int h, float[][] chimneys, float[][] parents, boolean isFg) {
        ensureChimneyLayout(w, h);
        int chimneyColor = isFg ? 0xFF150C05 : 0xFF1F1208;
        int brickColor   = isFg ? 0xFF1F1408 : 0xFF2A1810;
        int[] x1s = isFg ? chimFgX1 : chimBgX1;
        int[] x2s = isFg ? chimFgX2 : chimBgX2;
        int[] y1s = isFg ? chimFgY1 : chimBgY1;
        int[] y2s = isFg ? chimFgY2 : chimBgY2;
        for (int ci = 0; ci < x1s.length; ci++) {
            int cx1 = x1s[ci];
            if (cx1 == NO_ANCHOR) continue;
            int cx2 = x2s[ci];
            int cy1 = y1s[ci];
            int cy2 = y2s[ci];
            g.fill(cx1, cy1, cx2, cy2, chimneyColor);
            g.fill(cx1 - 2, cy1 - 2, cx2 + 2, cy1 + 2, chimneyColor);
            for (int y = cy1 + 3; y < cy2; y += 3) {
                g.fill(cx1, y, cx2, y + 1, brickColor);
            }
        }
    }

    private static void initSmoke() {
        if (smokeInit) return;
        smokeInit = true;
        java.util.Random rng = RND;
        for (int i = 0; i < smokeParticles.length; i++) {
            smokeParticles[i][0] = rng.nextFloat();
            smokeParticles[i][1] = rng.nextFloat();
            smokeParticles[i][2] = rng.nextFloat();
            smokeParticles[i][3] = 3 + rng.nextFloat() * 5;
        }
    }

    private static void tickSmoke(float deltaTime) {
        for (int i = 0; i < smokeParticles.length; i++) {
            smokeParticles[i][1] -= 0.3f * deltaTime * 60f;
            smokeParticles[i][2] -= 0.008f * deltaTime * 60f;
            if (smokeParticles[i][2] <= 0f || smokeParticles[i][1] < -0.15f) {
                smokeParticles[i][0] = (float)(RND.nextFloat() * 0.8f + 0.1f);
                smokeParticles[i][1] = 0f;
                smokeParticles[i][2] = 0.3f + (float)(RND.nextFloat() * 0.3f);
                smokeParticles[i][3] = 3 + (float)(RND.nextFloat() * 5);
            }
        }
    }

    // v0.198.0: la boca de cada chimenea (x, ancho del penacho y altura) solo
    // depende del tamano de la pantalla, como el trazado de las ventanas. Se
    // resuelve una vez por resolucion y el humo se ahorra el reparto de padres.
    private static final int NO_ANCHOR = Integer.MIN_VALUE;
    private static int smokeLayoutW = -1;
    private static int smokeLayoutH = -1;
    private static int[] smokeBgX = new int[0];
    private static int[] smokeBgW = new int[0];
    private static int[] smokeBgTop = new int[0];
    private static int[] smokeFgX = new int[0];
    private static int[] smokeFgW = new int[0];
    private static int[] smokeFgTop = new int[0];

    private static void ensureSmokeAnchors(int w, int h) {
        if (smokeLayoutW == w && smokeLayoutH == h) return;
        smokeLayoutW = w;
        smokeLayoutH = h;
        if (smokeBgX.length != CHIMNEYS_BG.length) {
            smokeBgX = new int[CHIMNEYS_BG.length];
            smokeBgW = new int[CHIMNEYS_BG.length];
            smokeBgTop = new int[CHIMNEYS_BG.length];
            smokeFgX = new int[CHIMNEYS_FG.length];
            smokeFgW = new int[CHIMNEYS_FG.length];
            smokeFgTop = new int[CHIMNEYS_FG.length];
        }
        fillSmokeAnchors(w, h, CHIMNEYS_BG, BG_BUILDINGS, smokeBgX, smokeBgW, smokeBgTop);
        fillSmokeAnchors(w, h, CHIMNEYS_FG, FG_BUILDINGS, smokeFgX, smokeFgW, smokeFgTop);
    }

    private static void fillSmokeAnchors(int w, int h, float[][] chimneys, float[][] parents,
            int[] xs, int[] widths, int[] tops) {
        for (int ci = 0; ci < chimneys.length; ci++) {
            float[] c = chimneys[ci];
            int idx = (int) c[1];
            xs[ci] = NO_ANCHOR;
            if (idx >= parents.length) continue;
            float[] parent = parents[idx];
            int px1 = (int)(w * parent[0]);
            int pw  = (int)(w * parent[2]);
            int py2 = (int)(h * parent[1]);
            int ph  = (int)(h * parent[3]);
            int py1 = py2 - ph;
            xs[ci] = px1 + (int)(pw * c[0]);
            widths[ci] = (int)(w * c[2]);
            tops[ci] = py1 - (int)(h * c[3]);
        }
    }

    private static void renderSmoke(GuiGraphics g, int w, int h) {
        ensureSmokeAnchors(w, h);
        // Wind variable: sinusoidal principal + segunda componente lenta
        float windX = (float)(Math.sin(time * 0.2f) * 8f + Math.sin(time * 0.07f) * 4f);
        renderSmokeGroup(g, w, h, smokeBgX, smokeBgW, smokeBgTop, 0, windX, 90);
        renderSmokeGroup(g, w, h, smokeFgX, smokeFgW, smokeFgTop, CHIMNEYS_BG.length, windX * 1.5f, 110);
    }

    private static void renderSmokeGroup(GuiGraphics g, int w, int h, int[] xs, int[] widths, int[] tops,
                                      int baseIdx, float windX, int maxAlpha) {
        for (int ci = 0; ci < xs.length; ci++) {
            int cx1 = xs[ci];
            if (cx1 == NO_ANCHOR) continue;
            int cw = widths[ci];
            int cy1 = tops[ci];
            // v0.198.0: la deriva de esta chimenea se calculaba por particula;
            // solo depende de la chimenea, asi que sale del bucle interior.
            float windDeep = (float)(Math.sin(time * 0.13f + ci * 0.5f) * 3f);
            // v0.191.0: tres puffs por chimenea en vez de cuatro y un 10% mas
            // grandes: la pluma se lee igual y cuesta un cuarto menos.
            for (int pi = 0; pi < 3; pi++) {
                int pIdx = (baseIdx + ci) * 8 + pi;
                if (pIdx >= smokeParticles.length) break;
                float[] p = smokeParticles[pIdx];
                int sx = cx1 + (int)(cw * p[0]) + (int)(windX * Math.abs(p[1]) * 2f) + (int)(windDeep * p[1] * 4f);
                int sy = cy1 + (int)(h * p[1]);
                int sz = (int)(p[3] * 1.35f);  // v0.191.0: 10% mas que antes
                // v0.198.0: un penacho que salio por arriba de la pantalla (o
                // quedo por debajo de la calle) no se dibuja: el scissor ya lo
                // tapaba, pero el pincel cobraba igual.
                if (sy + sz < 0 || sy - sz > h) continue;
                int sA = Math.max(0, Math.min(255, (int)(p[2] * maxAlpha)));
                if (sA < 6) continue;
                // Color mas oscuro: 0x00403020 en vez de 0x00705F45 (evita blanco por saturacion)
                AtmosphereRenderer.drawSoftBlob(g, sx, sy, sz, sA, 0x00403020);
            }
        }
    }

    public static int[][] getLanternPositions(int w, int h) {
        int streetTop;
        float[] xs;
        if (h >= 800) {
            streetTop = (int)(h * 0.839f);
            xs = new float[]{0.09f, 0.24f, 0.40f, 0.57f, 0.73f, 0.89f};
        } else if (h >= 400) {
            streetTop = (int)(h * 0.841f);
            xs = new float[]{0.12f, 0.30f, 0.49f, 0.69f, 0.87f};
        } else if (h >= 260) {
            streetTop = (int)(h * 0.844f);
            xs = new float[]{0.16f, 0.40f, 0.66f, 0.86f};
        } else {
            streetTop = (int)(h * 0.848f);
            xs = new float[]{0.22f, 0.50f, 0.80f};
        }

        int[][] positions = new int[xs.length][2];
        for (int i = 0; i < xs.length; i++) {
            int lx = Math.max(8, Math.min(w - 8, (int)(w * xs[i])));
            positions[i][0] = lx;
            positions[i][1] = streetTop;
        }
        return positions;
    }

    // ============================================================
    // Decoraciones del skyline (v0.12.2)
    // ============================================================

    /** Gato sobre tejado del edificio FG index 5. Version chica (4x2). */
    private static void renderRoofCat(GuiGraphics g, int w, int h) {
        float[] b = FG_BUILDINGS[5];
        int bx1 = (int)(w * b[0]);
        int by2 = (int)(h * b[1]);
        int bw = (int)(w * b[2]);
        int bh = (int)(h * b[3]);
        int by1 = by2 - bh;
        int cx = bx1 + (int)(bw * 0.65f);
        int cy = by1 - 3;

        int catColor = 0xFF0A0503;
        int eyeColor = 0xFFD4941E;

        double headTimer = Math.floor(time / 5.0);
        boolean headRight = (((long)headTimer) % 2L) == 0L;

        // Cuerpo: 4x2
        g.fill(cx, cy - 1, cx + 4, cy + 1, catColor);
        // Patas
        g.fill(cx, cy + 1, cx + 1, cy + 2, catColor);
        g.fill(cx + 3, cy + 1, cx + 4, cy + 2, catColor);
        // Cola larga curva
        g.fill(cx - 1, cy - 2, cx, cy - 1, catColor);
        g.fill(cx - 2, cy - 3, cx - 1, cy - 2, catColor);
        // Cabeza
        if (headRight) {
            g.fill(cx + 4, cy - 2, cx + 6, cy, catColor);
            g.fill(cx + 4, cy - 3, cx + 5, cy - 2, catColor);
            g.fill(cx + 5, cy - 3, cx + 6, cy - 2, catColor);
            g.fill(cx + 5, cy - 1, cx + 6, cy, eyeColor);
        } else {
            g.fill(cx - 2, cy - 2, cx, cy, catColor);
            g.fill(cx - 2, cy - 3, cx - 1, cy - 2, catColor);
            g.fill(cx - 1, cy - 3, cx, cy - 2, catColor);
            g.fill(cx - 2, cy - 1, cx - 1, cy, eyeColor);
        }
    }

    /** Grieta pixel-art en el edificio FG index 5 (mas ancho, mas visible). */
    private static void renderCrack(GuiGraphics g, int w, int h) {
        // Edificio 5: {0.56f, 0.78f, 0.14f, 0.22f} - el mas ancho y alto
        // Pero ahi esta el gato. Uso el 1: {0.13f, 0.79f, 0.10f, 0.21f}
        float[] b = FG_BUILDINGS[1];
        int bx1 = (int)(w * b[0]);
        int by2 = (int)(h * b[1]);
        int bw = (int)(w * b[2]);
        int bh = (int)(h * b[3]);
        int by1 = by2 - bh;

        // Highlight de color mas claro para destacar sobre el edificio oscuro
        int crackColor = 0xFF2A1608;   // marron oscuro visible
        int highlight = 0xFF5A3820;    // borde ambar claro

        // Grieta zigzag mas grande, 2px de ancho
        int startX = bx1 + (int)(bw * 0.55f);
        int startY = by1 + (int)(bh * 0.15f);
        int endY = by1 + (int)(bh * 0.75f);
        int totalH = endY - startY;
        int h1 = totalH / 3;
        int h2 = totalH / 3;

        // v0.188.0: los tres tramos tienen x fija, asi que cada uno es un solo
        // rectangulo. Antes eran mas de cien fills de un pixel por cuadro.
        int x2 = startX + 3;
        int y2start = startY + h1;
        int x3 = x2 - 4;
        int y3start = y2start + h2;
        g.fill(startX, startY, startX + 2, startY + h1, crackColor);
        g.fill(x2, y2start, x2 + 2, y2start + h2, crackColor);
        g.fill(x3, y3start, x3 + 2, endY, crackColor);

        // Ramificaciones diagonales pequenas (grietas menores)
        for (int i = 0; i < 3; i++) {
            int by = startY + (i * totalH / 3) + 3;
            int bxOffset = (i % 2 == 0) ? -2 : 2;
            int bxSeg = startX + (i * 3 * (i % 2 == 0 ? -1 : 1) / 3);
            g.fill(startX + bxOffset, by, startX + bxOffset + 1, by + 1, crackColor);
            g.fill(startX + bxOffset * 2, by + 1, startX + bxOffset * 2 + 1, by + 2, crackColor);
        }

        // Highlight sutil al lado derecho de la grieta (efecto 3D)
        g.fill(startX + 2, startY, startX + 3, startY + h1, highlight);
        g.fill(x2 + 2, y2start, x2 + 3, y2start + h2, highlight);
        g.fill(x3 + 2, y3start, x3 + 3, endY, highlight);

        // Vela dentro del edificio: ventana amber tenue que parpadea suave
        // Posicion: cerca de la parte inferior del edificio, lado opuesto a la grieta
        float candleFlicker = (float)(Math.sin(time * 3.7f) * 0.15f + 0.85f);
        int candleAlpha = (int)(candleFlicker * 100);
        int candleColor = AtmosphereRenderer.withAlpha(0x00FFB84C, candleAlpha);
        int candleX = bx1 + (int)(bw * 0.25f);
        int candleY = by1 + (int)(bh * 0.55f);
        g.fill(candleX, candleY, candleX + 3, candleY + 4, candleColor);
        // Halo suave alrededor de la vela
        int haloColor = AtmosphereRenderer.withAlpha(0x00D4941E, candleAlpha / 3);
        g.fill(candleX - 1, candleY - 1, candleX + 4, candleY + 5, haloColor);
    }

}
