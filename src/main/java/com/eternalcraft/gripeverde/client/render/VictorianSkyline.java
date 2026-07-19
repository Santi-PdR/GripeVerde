package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VictorianSkyline {

    private static float time = 0f;

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

    private static final int TOTAL_CHIMNEYS = CHIMNEYS_BG.length + CHIMNEYS_FG.length;
    private static final float[][] smokeParticles = new float[TOTAL_CHIMNEYS * 4][4];
    private static boolean smokeInit = false;

    public static void tick(float deltaTime) {
        time += deltaTime;
        initSmoke();
        tickSmoke(deltaTime);
    }

    public static void renderBackground(GuiGraphics g, int w, int h) {
        int colorBg = 0xFF1A1208;
        for (float[] b : BG_BUILDINGS) {
            int x1 = (int)(w * b[0]);
            int y2 = (int)(h * b[1]);
            int x2 = x1 + (int)(w * b[2]);
            int y1 = y2 - (int)(h * b[3]);
            g.fill(x1, y1, x2, y2, colorBg);
            int roofColor = 0xFF2A1E10;
            for (int px = x1; px < x2; px += 5) {
                g.fill(px, y1 - 2, px + 3, y1, roofColor);
            }
        }
        renderWindowsOnBuildings(g, w, h, BG_BUILDINGS, true);
        renderChimneys(g, w, h, CHIMNEYS_BG, BG_BUILDINGS, false);
    }

    public static void renderForeground(GuiGraphics g, int w, int h) {
        int colorFg = 0xFF0F0805;
        for (float[] b : FG_BUILDINGS) {
            int x1 = (int)(w * b[0]);
            int y2 = (int)(h * b[1]);
            int x2 = x1 + (int)(w * b[2]);
            int y1 = y2 - (int)(h * b[3]);
            g.fill(x1, y1, x2, y2, colorFg);
            int roofColor = 0xFF1F1408;
            for (int px = x1; px < x2; px += 4) {
                g.fill(px, y1 - 3, px + 3, y1, roofColor);
            }
            g.fill(x1 - 2, y1, x2 + 2, y1 + 2, 0xFF2A1E10);
            int doorW = Math.max(3, (int)(w * b[2] * 0.15f));
            int doorH = Math.max(6, (int)(h * b[3] * 0.15f));
            int doorX = x1 + (x2 - x1) / 2 - doorW / 2;
            int doorY = y2 - doorH;
            g.fill(doorX, doorY, doorX + doorW, y2, 0xFF080503);
            g.fill(doorX - 1, y2 - 2, doorX + doorW + 1, y2,
                AtmosphereRenderer.withAlpha(0x00D4941E, 60));
        }
        renderWindowsOnBuildings(g, w, h, FG_BUILDINGS, false);
        renderChimneys(g, w, h, CHIMNEYS_FG, FG_BUILDINGS, true);
        renderSmoke(g, w, h);
    }

    private static void renderWindowsOnBuildings(GuiGraphics g, int w, int h, float[][] buildings, boolean isBg) {
        long seed = isBg ? WINDOW_SEED_BG : WINDOW_SEED_FG;
        java.util.Random rng = new java.util.Random(seed);
        int offOnThreshold = isBg ? 8 : 7;
        int amberThreshold  = 9;
        for (int bi = 0; bi < buildings.length; bi++) {
            float[] b = buildings[bi];
            int bx1 = (int)(w * b[0]);
            int by2 = (int)(h * b[1]);
            int bx2 = bx1 + (int)(w * b[2]);
            int by1 = by2 - (int)(h * b[3]);
            int bWidth  = bx2 - bx1;
            int bHeight = by2 - by1;
            int cols = Math.max(1, bWidth  / (isBg ? 10 : 14));
            int rows = Math.max(1, bHeight / (isBg ? 12 : 16));

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
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

                    if (roll < offOnThreshold) {
                        g.fill(wx, wy, wx + winW, wy + winH, 0xFF050302);
                        if (!isBg) {
                            g.fill(wx, wy, wx + winW, wy + 1, 0xFF1A1008);
                        }
                    } else if (roll < amberThreshold) {
                        if (isBg) {
                            g.fill(wx, wy, wx + winW, wy + winH,
                                AtmosphereRenderer.withAlpha(0x00A8781E, 110));
                        } else {
                            float flicker = (float)(Math.sin(time * (0.5f + bi*0.13f + col*0.17f)) * 0.3f + 0.7f);
                            int amber = Math.max(40, (int)(flicker * 140));
                            g.fill(wx - 1, wy - 1, wx + winW + 1, wy + winH + 1, 0xFF1A0F05);
                            g.fill(wx, wy, wx + winW, wy + winH,
                                AtmosphereRenderer.withAlpha(0x00D4941E, amber));
                            g.fill(wx + winW/2, wy, wx + winW/2 + 1, wy + winH, 0xFF3D2E1A);
                            g.fill(wx, wy + winH/2, wx + winW, wy + winH/2 + 1, 0xFF3D2E1A);
                            g.fill(wx - 2, wy - 2, wx + winW + 2, wy + winH + 2,
                                AtmosphereRenderer.withAlpha(0x00D4941E, amber / 6));
                        }
                    } else {
                        if (isBg) {
                            g.fill(wx, wy, wx + winW, wy + winH,
                                AtmosphereRenderer.withAlpha(0x004A6028, 80));
                        } else {
                            float sickFlicker = (float)(Math.sin(time * (0.8f + bi*0.2f)) * 0.4f + 0.6f);
                            int sickAlpha = Math.max(30, (int)(sickFlicker * 100));
                            g.fill(wx - 1, wy - 1, wx + winW + 1, wy + winH + 1, 0xFF1A0F05);
                            g.fill(wx, wy, wx + winW, wy + winH,
                                AtmosphereRenderer.withAlpha(0x005A7038, sickAlpha));
                            g.fill(wx + winW/2, wy, wx + winW/2 + 1, wy + winH, 0xFF1A2810);
                            g.fill(wx, wy + winH/2, wx + winW, wy + winH/2 + 1, 0xFF1A2810);
                            g.fill(wx - 2, wy - 2, wx + winW + 2, wy + winH + 2,
                                AtmosphereRenderer.withAlpha(0x003A5028, sickAlpha / 5));
                        }
                    }
                }
            }
        }
    }

    private static void renderChimneys(GuiGraphics g, int w, int h, float[][] chimneys, float[][] parents, boolean isFg) {
        int chimneyColor = isFg ? 0xFF150C05 : 0xFF1F1208;
        int brickColor   = isFg ? 0xFF1F1408 : 0xFF2A1810;
        for (float[] c : chimneys) {
            int idx = (int) c[1];
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
        java.util.Random rng = new java.util.Random(0x53330333L);
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
                smokeParticles[i][0] = (float)(Math.random() * 0.8f + 0.1f);
                smokeParticles[i][1] = 0f;
                smokeParticles[i][2] = 0.3f + (float)(Math.random() * 0.3f);
                smokeParticles[i][3] = 3 + (float)(Math.random() * 5);
            }
        }
    }

    private static void renderSmoke(GuiGraphics g, int w, int h) {
        float windX = (float)(Math.sin(time * 0.2f) * 8f);
        renderSmokeGroup(g, w, h, CHIMNEYS_BG, BG_BUILDINGS, 0, windX, 160);
    }

    private static void renderSmokeGroup(GuiGraphics g, int w, int h, float[][] chimneys, float[][] parents,
                                      int baseIdx, float windX, int maxAlpha) {
        for (int ci = 0; ci < chimneys.length; ci++) {
            float[] c = chimneys[ci];
            int idx = (int) c[1];
            if (idx >= parents.length) continue;
            float[] parent = parents[idx];
            int px1 = (int)(w * parent[0]);
            int pw  = (int)(w * parent[2]);
            int py2 = (int)(h * parent[1]);
            int ph  = (int)(h * parent[3]);
            int py1 = py2 - ph;
            int cx1 = px1 + (int)(pw * c[0]);
            int cw  = (int)(w * c[2]);
            int cy1 = py1 - (int)(h * c[3]);
            for (int pi = 0; pi < 4; pi++) {
                int pIdx = (baseIdx + ci) * 4 + pi;
                if (pIdx >= smokeParticles.length) break;
                float[] p = smokeParticles[pIdx];
                int sx = cx1 + (int)(cw * p[0]) + (int)(windX * Math.abs(p[1]) * 2f);
                int sy = cy1 + (int)(h * p[1]);
                int sz = (int) p[3];
                int sA = Math.max(0, Math.min(255, (int)(p[2] * maxAlpha)));
                g.fill(sx - sz, sy - sz, sx + sz, sy + sz,
                    AtmosphereRenderer.withAlpha(0x00705F45, sA));
            }
        }
    }

    public static int[][] getLanternPositions(int w, int h) {
        return new int[0][0];
    }
}
