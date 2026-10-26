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
    private static final float[] CLOUD_SPEEDS = new float[7];
    private static boolean cloudSpeedsInit = false;

    // Fixed title-sky layout; reuse it instead of allocating six arrays per frame.
    private static final float[][] CLOUDS = {
        {0.08f, 0.06f, 0.0f,  0.45f, 0.7f},
        {0.15f, 0.05f, 0.3f,  0.55f, 0.6f},
        {0.05f, 0.04f, 0.55f, 0.40f, 0.5f},
        {0.22f, 0.07f, 0.1f,  0.50f, 0.65f},
        {0.12f, 0.05f, 0.7f,  0.35f, 0.55f},
    };

    private static void initCloudSpeeds() {
        if (cloudSpeedsInit) return;
        // Velocidades base con variacion +-30% por sesion
        float[] base = {0.008f, 0.005f, 0.012f, 0.007f, 0.009f, 0.003f, 0.004f};
        java.util.Random rng = new java.util.Random();
        for (int i = 0; i < base.length; i++) {
            float variation = 0.7f + rng.nextFloat() * 0.6f;  // 0.7 .. 1.3
            CLOUD_SPEEDS[i] = base[i] * variation;
        }
        cloudSpeedsInit = true;
    }
    private static float moonGlowPulse = 0f;

    private static final int STAR_COUNT = 60;
    private static final int[] STAR_X = new int[STAR_COUNT];
    private static final int[] STAR_Y = new int[STAR_COUNT];
    private static final float[] STAR_FREQ = new float[STAR_COUNT];
    private static final float[] STAR_PHASE = new float[STAR_COUNT];
    private static int starCacheW = -1;
    private static int starCacheH = -1;

    // Moon phases: 0=new, 1=waxing crescent, 2=first quarter, 3=waxing gibbous,
    // 4=full, 5=waning gibbous, 6=last quarter, 7=waning crescent
    private static int moonPhaseIndex = -1;
    private static long lastMoonPhaseRotateMs = 0L;
    private static long MOON_PHASE_ROTATE_MS = 300_000L;
    private static final java.util.Random MOON_RNG = new java.util.Random();

    // Shooting star: single star crossing every 30-90s
    private static float shootStarX = -0.2f;
    private static float shootStarY = 0.1f;
    private static float shootStarSpeedX = 0f;
    private static float shootStarSpeedY = 0f;
    private static float shootStarTimer = 20f;
    private static float shootStarNextEvent = 40f;
    private static boolean shootStarActive = false;
    /** Mensaje breve visible cuando aparece una estrella fugaz. Se limpia automaticamente. */
    public static String shootStarMessage = null;
    public static long shootStarMessageUntilMs = 0L;

    private static final String[] SHOOT_STAR_MESSAGES_ES = {
        "Un deseo cae del cielo",
        "Algo cruzo la noche",
        "Una senal fugaz",
        "El cielo parpadea",
        "Una brasa cruza el firmamento",
        "La noche firma con fuego",
        "El cronista pidio un augurio",
        "La peste tambien mira hacia arriba",
        "Una cicatriz breve en las nubes",
        "El cielo deja caer una astilla"
    };
    private static final String[] SHOOT_STAR_MESSAGES_EN = {
        "A wish falls from the sky",
        "Something crossed the night",
        "A fleeting omen",
        "The sky blinks",
        "An ember crosses the firmament",
        "The night signs with fire",
        "The chronicler asked for an omen",
        "The plague looks upward too",
        "A brief scar among the clouds",
        "The sky drops a splinter"
    };

    private static String pickShootStarMessage() {
        boolean english = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        String[] pool = english ? SHOOT_STAR_MESSAGES_EN : SHOOT_STAR_MESSAGES_ES;
        return pool[MOON_RNG.nextInt(pool.length)];
    }

    // Trail: guardamos las ultimas 8 posiciones para la estela
    private static final int TRAIL_LEN = 12;
    private static final float[][] shootStarTrail = new float[12][2];

    // Birds flock: 3-4 siluetas cruzando el cielo cada 90-180s
    private static final int BIRDS_COUNT = 4;
    private static final float[][] birds = new float[BIRDS_COUNT][4];  // x, y, wingPhase, active
    private static float birdsTimer = 30f;
    private static float birdsNextEvent = 90f;
    private static boolean birdsActive = false;
    private static float birdsSpeedX = 0f;

    private static boolean reducedMotion() {
        try {
            return com.eternalcraft.gripeverde.config.ClientConfig.REDUCED_MOTION.get();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    public static void tick(float deltaTime) {
        initCloudSpeeds();
        if (reducedMotion()) {
            moonGlowPulse = 0.5f;
            shootStarActive = false;
            shootStarTimer = 0f;
            shootStarMessage = null;
            shootStarMessageUntilMs = 0L;
            birdsActive = false;
            birdsTimer = 0f;
            if (moonPhaseIndex < 0) {
                moonPhaseIndex = MOON_RNG.nextInt(8);
                lastMoonPhaseRotateMs = System.currentTimeMillis();
            }
            return;
        }

        time += deltaTime;
        moonGlowPulse = (float)(Math.sin(time * 0.4f) * 0.5f + 0.5f);
        for (int i = 0; i < CLOUD_OFFSETS.length; i++) {
            CLOUD_OFFSETS[i] += CLOUD_SPEEDS[i] * deltaTime * 60f;
            if (CLOUD_OFFSETS[i] > 400f) CLOUD_OFFSETS[i] = -300f;
        }

        // Moon phase rotation
        long now = System.currentTimeMillis();
        if (moonPhaseIndex < 0 || (now - lastMoonPhaseRotateMs) >= MOON_PHASE_ROTATE_MS) {
            int prev = moonPhaseIndex;
            int next;
            do { next = MOON_RNG.nextInt(8); } while (next == prev);
            moonPhaseIndex = next;
            lastMoonPhaseRotateMs = now;
            MOON_PHASE_ROTATE_MS = 240_000L + (long)(MOON_RNG.nextDouble() * 240_000L);
        }

        // Shooting star
        if (shootStarActive) {
            // Shift trail
            for (int i = TRAIL_LEN - 1; i > 0; i--) {
                shootStarTrail[i][0] = shootStarTrail[i-1][0];
                shootStarTrail[i][1] = shootStarTrail[i-1][1];
            }
            shootStarTrail[0][0] = shootStarX;
            shootStarTrail[0][1] = shootStarY;
            shootStarX += shootStarSpeedX * deltaTime;
            shootStarY += shootStarSpeedY * deltaTime;
            if (shootStarX < -0.15f || shootStarX > 1.15f
                || shootStarY < -0.05f || shootStarY > 0.6f) {
                shootStarActive = false;
                shootStarTimer = 0f;
                shootStarNextEvent = 60f + (float)(MOON_RNG.nextDouble() * 120.0);
            }
        } else {
            shootStarTimer += deltaTime;
            if (shootStarTimer >= shootStarNextEvent) {
                shootStarActive = true;
                // Frase al aparecer
                shootStarMessage = pickShootStarMessage();
                shootStarMessageUntilMs = System.currentTimeMillis() + 3000L;
                // Sonido sutil al pasar
                try {
                    net.minecraft.client.Minecraft.getInstance().getSoundManager().play(
                        net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                            com.eternalcraft.gripeverde.client.audio.ModSounds.UI_SCROLL.get(),
                            0.8f, 0.15f));
                } catch (Throwable ignored) {}
                // Start desde borde superior aleatorio
                if (MOON_RNG.nextBoolean()) {
                    shootStarX = -0.1f;
                    shootStarSpeedX = 0.4f + (float)(MOON_RNG.nextDouble() * 0.3);
                } else {
                    shootStarX = 1.1f;
                    shootStarSpeedX = -(0.4f + (float)(MOON_RNG.nextDouble() * 0.3));
                }
                shootStarY = 0.05f + (float)(MOON_RNG.nextDouble() * 0.15);
                shootStarSpeedY = 0.1f + (float)(MOON_RNG.nextDouble() * 0.15);
                // Reset trail
                for (int i = 0; i < TRAIL_LEN; i++) {
                    shootStarTrail[i][0] = shootStarX;
                    shootStarTrail[i][1] = shootStarY;
                }
            }
        }

        // Birds flock
        if (birdsActive) {
            for (int i = 0; i < BIRDS_COUNT; i++) {
                if (birds[i][3] > 0f) {  // active
                    birds[i][0] += birdsSpeedX * deltaTime;
                    birds[i][2] += deltaTime * 6f;  // wing phase
                }
            }
            // Check if flock is off-screen
            boolean anyOnScreen = false;
            for (int i = 0; i < BIRDS_COUNT; i++) {
                if (birds[i][3] > 0f && birds[i][0] > -0.1f && birds[i][0] < 1.1f) {
                    anyOnScreen = true; break;
                }
            }
            if (!anyOnScreen) {
                birdsActive = false;
                birdsTimer = 0f;
                birdsNextEvent = 70f + (float)(MOON_RNG.nextDouble() * 90.0);
            }
        } else {
            birdsTimer += deltaTime;
            if (birdsTimer >= birdsNextEvent) {
                birdsActive = true;
                boolean rightward = MOON_RNG.nextBoolean();
                birdsSpeedX = rightward ? 0.06f + (float)(MOON_RNG.nextDouble() * 0.04)
                                        : -(0.06f + (float)(MOON_RNG.nextDouble() * 0.04));
                float startX = rightward ? -0.1f : 1.1f;
                float baseY = 0.08f + (float)(MOON_RNG.nextDouble() * 0.15);
                // Formacion en V o linea
                int count = 3 + MOON_RNG.nextInt(2);  // 3 o 4
                for (int i = 0; i < BIRDS_COUNT; i++) {
                    if (i < count) {
                        // Spacing
                        float offset = (float)i * 0.035f * (rightward ? -1 : 1);
                        birds[i][0] = startX + offset;
                        birds[i][1] = baseY + (float)i * 0.008f;  // ligera diferencia en Y
                        birds[i][2] = MOON_RNG.nextFloat() * 6.28f;
                        birds[i][3] = 1f;  // active
                    } else {
                        birds[i][3] = 0f;
                    }
                }
            }
        }
    }

    public static void renderSky(GuiGraphics g, int w, int h) {
        // Guard: cuando la ventana pierde foco o se minimiza (Alt+Tab, F11),
        // w o h pueden ser 0. Random.nextInt(0) lanza IllegalArgumentException
        // y crashea el juego.
        if (w <= 0 || h <= 0) return;
        renderSkyGradient(g, w, h);
        renderStars(g, w, h);
        renderShootingStar(g, w, h);
        renderMoon(g, w, h);
        renderBirds(g, w, h);
        renderClouds(g, w, h);
    }

    private static void renderSkyGradient(GuiGraphics g, int w, int h) {
        int stop1 = (int)(h * 0.30f);
        int stop2 = (int)(h * 0.65f);
        fillGradientV(g, 0, 0,     w, stop1, DiaryTheme.NIGHT_TOP,   DiaryTheme.NIGHT_UPPER);
        fillGradientV(g, 0, stop1, w, stop2, DiaryTheme.NIGHT_UPPER, DiaryTheme.NIGHT_MID);
        fillGradientV(g, 0, stop2, w, h,     DiaryTheme.NIGHT_MID,   DiaryTheme.NIGHT_HORIZON);

        // Resplandor industrial bajo: chimeneas y gas de la ciudad respirando en el horizonte.
        int glowTop = Math.max(0, (int)(h * 0.58f));
        int glowBot = Math.min(h, glowTop + Math.max(12, h / 12));
        int glowAlpha = 18 + (int)(Math.sin(time * 0.22f) * 5f);
        g.fill(0, glowTop, w, glowBot, withAlpha(0x004A2E18, glowAlpha));

        // Efecto vela al marcar las 12: overlay muy sutil que parpadea
        try {
            if (com.eternalcraft.gripeverde.client.render.PocketWatchRenderer.isNearNoon()) {
                float flicker = (float)(Math.sin(time * 4.0f) * 0.5f + 0.5f);
                float chaos = (float)(Math.sin(time * 11.3f) * 0.4f + 0.6f);
                float intensity = flicker * chaos;
                int alpha = (int)(intensity * 15);
                if (alpha > 1) {
                    g.fill(0, 0, w, h, (alpha << 24) | 0x2A1608);
                }
            }
        } catch (Throwable ignored) {}
    }

    private static void renderStars(GuiGraphics g, int w, int h) {
        if (w <= 0 || h <= 0) return;
        ensureStarCache(w, h);
        for (int i = 0; i < STAR_COUNT; i++) {
            int sx = STAR_X[i];
            int sy = STAR_Y[i];
            float twinkle = (float)(Math.sin(time * STAR_FREQ[i] + STAR_PHASE[i]) * 0.5f + 0.5f);
            boolean bright = (i < 15);
            int baseAlpha = bright ? 60 : 30;
            int peakAlpha = bright ? 200 : 120;
            int alpha = baseAlpha + (int)(twinkle * (peakAlpha - baseAlpha));
            int color = (alpha << 24) | 0xE8DCB4;
            if (bright) {
                int edgeAlpha = Math.max(0, alpha / 3);
                int edgeColor = (edgeAlpha << 24) | 0xE8DCB4;
                g.fill(sx, sy, sx + 2, sy + 2, color);
                g.fill(sx - 1, sy, sx, sy + 2, edgeColor);
                g.fill(sx + 2, sy, sx + 3, sy + 2, edgeColor);
                g.fill(sx, sy - 1, sx + 2, sy, edgeColor);
                g.fill(sx, sy + 2, sx + 2, sy + 3, edgeColor);
            } else {
                g.fill(sx, sy, sx + 1, sy + 1, color);
            }
        }
    }

    private static void ensureStarCache(int w, int h) {
        if (starCacheW == w && starCacheH == h) return;
        starCacheW = w;
        starCacheH = h;
        int halfH = Math.max(1, (int)(h * 0.5f));
        java.util.Random rng = new java.util.Random(0xA57C5L ^ ((long)w << 16) ^ h);
        for (int i = 0; i < STAR_COUNT; i++) {
            STAR_X[i] = rng.nextInt(Math.max(1, w));
            STAR_Y[i] = rng.nextInt(halfH);
            STAR_FREQ[i] = 0.3f + rng.nextFloat() * 1.2f;
            STAR_PHASE[i] = rng.nextFloat() * 6.28f;
        }
    }


    private static void renderMoon(GuiGraphics g, int w, int h) {
        int cx = (int)(w * 0.75f);
        int cy = (int)(h * 0.18f);
        int radius = (int)(w * 0.025f);

        int phase = moonPhaseIndex < 0 ? 4 : moonPhaseIndex;  // fallback full

        // Halo: solo en fases con iluminacion significativa (>50%)
        float phaseIntensity = getPhaseIlluminationFactor(phase);
        if (phaseIntensity > 0.4f) {
            int haloAlpha = (int)((12 + (int)(moonGlowPulse * 10)) * phaseIntensity);
            drawSoftBlob(g, cx, cy, radius + 24, haloAlpha,       0x00E8DCB4);
            drawSoftBlob(g, cx, cy, radius + 14, haloAlpha + 5,   0x00D4C089);
            drawSoftBlob(g, cx, cy, radius + 8,  haloAlpha + 8,   0x00F0E5C0);
        }

        // Luna: solo dibujar la parte iluminada segun la fase.
        // NO dibujamos disco oscuro base (queda transparente sobre el cielo).
        if (phase == 0) {
            // Nueva: solo halo muy tenue
            int newMoonHint = AtmosphereRenderer.withAlpha(0x00E8DCB4, 8);
            fillCircle(g, cx, cy, radius, newMoonHint);
        } else {
            // Recorte iluminado sin disco oscuro atras
            renderMoonPhase(g, cx, cy, radius, phase);
        }

        // Jirones finos (solo si hay algo iluminado)
        if (phase != 0) {  // no en luna nueva
            int wispAlphaA = 40 + (int)(moonGlowPulse * 15);
            int wispAlphaB = 32 + (int)(moonGlowPulse * 12);
            int wispAlphaC = 24 + (int)(moonGlowPulse * 10);

            float driftA = (float)(Math.sin(time * 0.06f) * (radius * 2.5f));
            float driftB = (float)(Math.sin(time * 0.045f + 1.7f) * (radius * 3.0f));
            float driftC = (float)(Math.sin(time * 0.035f + 3.2f) * (radius * 2.2f));

            drawMoonWisp(g, cx - radius * 3 + (int)driftA, cy - radius / 2, 3, wispAlphaA, 6, 5, 0x00201810);
            drawMoonWisp(g, cx - radius * 3 + (int)driftB, cy + 1,           3, wispAlphaB, 7, 5, 0x00241C12);
            drawMoonWisp(g, cx - radius * 3 + (int)driftC, cy + radius / 2, 3, wispAlphaC, 6, 5, 0x001E1610);
        }
    }

    /** Factor de iluminacion de la luna segun fase (0..1). */
    private static float getPhaseIlluminationFactor(int phase) {
        switch (phase) {
            case 0: return 0.15f;  // new
            case 1: case 7: return 0.30f;  // crescent
            case 2: case 6: return 0.55f;  // quarter
            case 3: case 5: return 0.80f;  // gibbous
            case 4: return 1.0f;   // full
            default: return 1.0f;
        }
    }

    /** Dibuja SOLO la parte iluminada del disco lunar segun la fase.
     *  Optimizado: usa intervalos por scanline en vez de loops por pixel (reduce de O(r^2) a O(r)). */
    private static void renderMoonPhase(GuiGraphics g, int cx, int cy, int radius, int phase) {
        int moonLight = DiaryTheme.MOON_WHITE;

        switch (phase) {
            case 0: // new: nada iluminado
                break;
            case 4: // full: disco completo + crateres
                fillCircle(g, cx, cy, radius, moonLight);
                g.fill(cx - 3, cy - 2, cx - 1, cy,     0xFFB8AB88);
                g.fill(cx + 1, cy + 1, cx + 3, cy + 3, 0xFFB8AB88);
                g.fill(cx - 1, cy + 2, cx,     cy + 3, 0xFFB8AB88);
                break;
            case 2: // first quarter: mitad derecha
                for (int dy = -radius; dy <= radius; dy++) {
                    int dx = (int) Math.sqrt((double)radius * radius - (double)dy * dy);
                    if (dx < 0) continue;
                    g.fill(cx, cy + dy, cx + dx + 1, cy + dy + 1, moonLight);
                }
                break;
            case 6: // last quarter: mitad izquierda
                for (int dy = -radius; dy <= radius; dy++) {
                    int dx = (int) Math.sqrt((double)radius * radius - (double)dy * dy);
                    if (dx < 0) continue;
                    g.fill(cx - dx, cy + dy, cx, cy + dy + 1, moonLight);
                }
                break;
            case 1: // waxing crescent: arco derecho
                renderMoonPhaseDifference(g, cx, cy, radius, cx - radius / 2, radius);
                break;
            case 7: // waning crescent: arco izquierdo
                renderMoonPhaseDifference(g, cx, cy, radius, cx + radius / 2, radius);
                break;
            case 3: // waxing gibbous: casi llena, sombra izquierda
                renderMoonPhaseDifference(g, cx, cy, radius, cx - radius - 1, radius);
                break;
            case 5: // waning gibbous: casi llena, sombra derecha
                renderMoonPhaseDifference(g, cx, cy, radius, cx + radius + 1, radius);
                break;
        }
    }

    /** Dibuja full circle minus shadow circle usando intervalos scanline (O(r) fills). */
    private static void renderMoonPhaseDifference(GuiGraphics g, int cx, int cy, int radius, int cxShadow, int rShadow) {
        int moonLight = DiaryTheme.MOON_WHITE;
        int r2 = radius * radius;
        int rs2 = rShadow * rShadow;
        for (int dy = -radius; dy <= radius; dy++) {
            int dy2 = dy * dy;
            if (dy2 > r2) continue;
            int dxOuter = (int) Math.sqrt(r2 - dy2);
            int fullL = cx - dxOuter;
            int fullR = cx + dxOuter;

            // Shadow interval on this scanline
            if (dy2 > rs2) {
                // No shadow overlap in Y, draw full
                g.fill(fullL, cy + dy, fullR + 1, cy + dy + 1, moonLight);
                continue;
            }
            int dxShadow = (int) Math.sqrt(rs2 - dy2);
            int shL = cxShadow - dxShadow;
            int shR = cxShadow + dxShadow;

            if (fullR < shL || fullL > shR) {
                // No overlap
                g.fill(fullL, cy + dy, fullR + 1, cy + dy + 1, moonLight);
            } else if (shL <= fullL && shR >= fullR) {
                // Fully shadowed
                continue;
            } else if (shL <= fullL) {
                // Shadow covers left part
                int nl = shR + 1;
                if (nl <= fullR) g.fill(nl, cy + dy, fullR + 1, cy + dy + 1, moonLight);
            } else if (shR >= fullR) {
                // Shadow covers right part
                int nr = shL - 1;
                if (fullL <= nr) g.fill(fullL, cy + dy, nr + 1, cy + dy + 1, moonLight);
            } else {
                // Shadow in middle -> two caps
                if (fullL <= shL - 1) g.fill(fullL, cy + dy, shL, cy + dy + 1, moonLight);
                if (shR + 1 <= fullR) g.fill(shR + 1, cy + dy, fullR + 1, cy + dy + 1, moonLight);
            }
        }
    }

    /** Renderiza la estrella fugaz con trail (llamado desde renderSky). */
    private static void renderShootingStar(GuiGraphics g, int w, int h) {
        if (!shootStarActive) return;
        int sx = (int)(shootStarX * w);
        int sy = (int)(shootStarY * h);

        // Trail: puntos con alpha decreciente
        for (int i = 0; i < TRAIL_LEN; i++) {
            int tx = (int)(shootStarTrail[i][0] * w);
            int ty = (int)(shootStarTrail[i][1] * h);
            int alpha = (int)(160 * (1f - (float)i / TRAIL_LEN));
            if (alpha > 5) {
                int col = (alpha << 24) | 0xE8DCB4;
                g.fill(tx, ty, tx + 1, ty + 1, col);
            }
        }

        // Cabeza brillante (mas discreta)
        g.fill(sx, sy, sx + 2, sy + 2, 0xE0F0E5C0);
        g.fill(sx - 1, sy, sx, sy + 2, 0x80D4C089);
        g.fill(sx + 2, sy, sx + 3, sy + 2, 0x80D4C089);
    }

    /** Renderiza la bandada de pajaros. */
    private static void renderBirds(GuiGraphics g, int w, int h) {
        if (!birdsActive) return;
        int birdColor = 0xFF0A0705;
        for (int i = 0; i < BIRDS_COUNT; i++) {
            if (birds[i][3] <= 0f) continue;
            int bx = (int)(birds[i][0] * w);
            int by = (int)(birds[i][1] * h);
            float wingAng = (float) Math.sin(birds[i][2]);
            int wingH = (int)(wingAng * 2);
            // Silueta minima: 3px cuerpo + 2 alas
            g.fill(bx - 1, by, bx + 2, by + 1, birdColor);         // cuerpo
            g.fill(bx - 3, by - wingH, bx - 1, by - wingH + 1, birdColor);  // ala izq
            g.fill(bx + 2, by + wingH, bx + 4, by + wingH + 1, birdColor);  // ala der
        }
    }

    private static void renderClouds(GuiGraphics g, int w, int h) {
        int cloudColor = 0x001A0F08;
        for (int i = 0; i < CLOUDS.length; i++) {
            int cy    = (int)(h * (CLOUDS[i][0] + CLOUDS[i][1] * 0.5f));
            int xBase = (int)(w * CLOUDS[i][2]) + (int)(CLOUD_OFFSETS[i]) - 100;
            int cw    = (int)(w * CLOUDS[i][3]);
            int ch    = (int)(h * CLOUDS[i][1]);
            int radius = Math.max(8, ch);
            float pulse = (float)(Math.sin(time * 0.3f + i * 1.3f) * 0.1f + CLOUDS[i][4]);
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

    private static void drawMoonWisp(GuiGraphics g, int startX, int cy, int radius,
                                     int alpha, int blobs, int spacing, int colorNoAlpha) {
        if (radius <= 0 || alpha <= 0 || blobs <= 0) return;

        for (int i = 0; i < blobs; i++) {
            float t = blobs <= 1 ? 0f : (float) i / (float) (blobs - 1);
            int bx = startX + i * spacing;
            int by = cy + (int)(Math.sin(time * 0.35f + t * 2.8f) * 2f);
            int br = Math.max(2, radius + (int)(Math.sin(t * 3.14159f) * radius * 0.35f));
            int ba = Math.max(4, (int)(alpha * (0.55f + 0.45f * Math.sin(t * 3.14159f))));
            drawSoftBlob(g, bx, by, br, ba, colorNoAlpha);
        }
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
