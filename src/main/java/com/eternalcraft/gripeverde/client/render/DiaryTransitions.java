package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/**
 * v0.26.0: el pasar de las hojas.
 * v0.27.0: el pliegue — la cortina tiene sentido (avanzar baja la hoja,
 * regresar la sube), el filo de tinta salpica al pasar y, una vez destapada,
 * la hoja nueva "se acuesta" sobre la mesa con una sombra interior breve.
 * v0.28.0: la tinta aun fresca gotea — del filo se desprenden gotas que
 * resbalan un trecho en el sentido del pliegue dejando un rastro fino.
 *
 * Al abrir cualquier pantalla del gabinete (paquete client.screens), una
 * cortina de tinta recorre la pagina revelando la hoja nueva (~260 ms). Con
 * movimiento reducido la transicion es solo un fundido breve, sin barrido,
 * salpicaduras ni gotas. Pantallas vanilla y ajenas quedan intactas.
 */
public final class DiaryTransitions {

    private DiaryTransitions() {}

    private static final String PREFIX = "com.eternalcraft.gripeverde.client.screens.";
    private static final long DUR_MS = 260L;
    private static final long DUR_REDUCED_MS = 130L;
    private static final long SETTLE_MS = 450L;   // la hoja se acuesta
    private static final long SPLASH_TAIL_MS = 500L;

    // v0.28.0: tres gotas que se desprenden del filo en distintos momentos
    // del pliegue (30/55/80%) y resbalan un trecho breve con rastro fino.
    private static final int DROP_COUNT = 3;
    private static final float[] DROP_DETACH = { 0.30f, 0.55f, 0.80f };
    private static final long DROP_SLIDE_MS = 550L;
    private static final int DROP_SLIDE_MAX = 26;

    private static Screen current = null;
    private static long openedAtMs = 0L;

    // v0.27.0: direccion decidida por la pila de mesas visitadas.
    // Avanzar (mesa nueva) -> la cortina baja; regresar (mesa ya visitada)
    // -> la cortina sube. Se alimenta desde ScreenEvent.Opening.
    private static final java.util.ArrayDeque<Class<?>> NAV = new java.util.ArrayDeque<>();
    private static boolean directionForward = true;
    private static boolean pendingHasDirection = false;

    // Salpicaduras del filo (semilla por apertura, estable durante la transicion)
    private static long splashSeed = 0L;
    private static boolean splashForward = true;

    private static boolean reducedMotion() {
        try { return com.eternalcraft.gripeverde.config.ClientConfig.REDUCED_MOTION.get(); }
        catch (Throwable t) { return false; }
    }

    /**
     * Llamado desde ScreenEventHandler.onScreenOpening con la pantalla que se
     * va y la que llega (ya con el redirect aplicado). Decide el sentido del
     * pliegue segun si la mesa de destino estaba abierta antes.
     */
    public static void noteOpening(Screen from, Screen to) {
        if (to == null || !to.getClass().getName().startsWith(PREFIX)) {
            pendingHasDirection = false;
            return;
        }
        Class<?> tc = to.getClass();
        boolean forward = true;
        try {
            if (NAV.contains(tc)) {
                // Regreso: desapilar hasta dejar la mesa de destino al tope.
                while (!NAV.isEmpty() && NAV.peekLast() != tc) NAV.pollLast();
                if (NAV.isEmpty() || NAV.peekLast() != tc) NAV.addLast(tc);
                forward = false;
            } else {
                NAV.addLast(tc);
                while (NAV.size() > 16) NAV.pollFirst();
            }
        } catch (Throwable ignored) {
            forward = true;
        }
        directionForward = forward;
        pendingHasDirection = true;
    }

    /** Llamado desde ScreenEvent.Render.Post. Dibuja encima de todo (es un velo). */
    public static void render(Screen screen, GuiGraphics g) {
        if (screen == null || !screen.getClass().getName().startsWith(PREFIX)) {
            current = null;
            return;
        }
        long now = System.currentTimeMillis();
        if (screen != current) {
            current = screen;
            openedAtMs = now;
            if (!pendingHasDirection) directionForward = true;
            pendingHasDirection = false;
            splashForward = directionForward;
            splashSeed = now * 1099511628211L ^ (long) screen.hashCode();
        }
        boolean reduced = reducedMotion();
        long dur = reduced ? DUR_REDUCED_MS : DUR_MS;
        long age = now - openedAtMs;
        if (age < 0) return;

        int w = g.guiWidth(), h = g.guiHeight();

        // ── La hoja se acuesta: sombra interior que decae tras el destape ──
        // Es solo opacidad (sin movimiento): tambien con movimiento reducido.
        long settleAge = age - dur;
        if (settleAge >= 0 && settleAge < SETTLE_MS) {
            float q = 1f - settleAge / (float) SETTLE_MS;
            int a = (int)(q * q * 20);
            if (a > 0) {
                int shade = (a << 24) | 0x140C05;
                g.fill(0, 0, w, 6, shade);
                g.fill(0, h - 6, w, h, shade);
                g.fill(0, 0, 6, h, shade);
                g.fill(w - 6, 0, w, h, shade);
            }
        }

        if (age >= dur) {
            // Cola de las salpicaduras: un respiro tras el paso del filo.
            if (!reduced && age < dur + SPLASH_TAIL_MS) {
                float tail = 1f - (age - dur) / (float) SPLASH_TAIL_MS;
                drawSplashes(g, w, h, splashForward ? h : 0, tail * 0.55f, true);
            }
            // Las gotas siguen resbalando un trecho tras el destape.
            if (!reduced) drawDrops(g, w, h, dur, age);
            return;
        }

        float p = age / (float) dur;
        float ease = 1f - (1f - p) * (1f - p); // salida suave

        // Velo sepia que se disuelve sobre toda la hoja (simetrico en ambos sentidos).
        int veil = (int)((1f - ease) * 150);
        if (veil > 0) {
            g.fill(0, 0, w, h, (veil << 24) | 0x1A1108);
        }

        // Franja de tinta que revela la pagina. Avanzar: de arriba abajo.
        // Regresar: de abajo arriba, la hoja vuelve al pliegue.
        if (!reduced) {
            int travel = (int)(ease * (h + 18)) - 9;
            int lineY = splashForward ? travel : h - travel;
            int bandA = (int)((1f - ease) * 120);
            if (bandA > 0 && lineY > -16 && lineY < h + 16) {
                int y0 = Math.max(0, lineY - 14);
                int y1 = Math.min(h, lineY + 16);
                if (y1 > y0) {
                    g.fill(0, y0, w, y1, (Math.min(160, bandA + 40) << 24) | 0x241608);
                    int ly = Math.min(h - 2, Math.max(0, lineY));
                    g.fill(0, ly, w, ly + 2, (Math.min(220, bandA + 80) << 24) | 0x8E6A35);
                }
                // El filo salpica: gotas diminutas detras del borde de tinta.
                drawSplashes(g, w, h, lineY, (1f - ease) * 0.9f, false);
            }
            // La tinta fresca gotea en el sentido del pliegue.
            drawDrops(g, w, h, dur, age);
        }
    }

    /**
     * v0.28.0: gotas que se desprenden del filo en tres momentos del pliegue
     * y resbalan un trecho breve dejando un rastro fino. Cada apertura tiene
     * sus posiciones propias (semilla estable durante la transicion) y los
     * alfas son bajos: jamas un destello.
     */
    private static void drawDrops(GuiGraphics g, int w, int h, long dur, long age) {
        int dir = splashForward ? 1 : -1; // la gota sigue el sentido del pliegue
        long s = splashSeed ^ 0x9E3779B97F4A7C15L;
        for (int i = 0; i < DROP_COUNT; i++) {
            s = s * 6364136223846793005L + 1442695040888963407L;
            int rx = (int)((s >>> 33) & 0x7FFFFFFF);
            long tBirth = (long)(DROP_DETACH[i] * dur);
            long slideAge = age - tBirth;
            if (slideAge < 0 || slideAge > DROP_SLIDE_MS) continue;
            int x = 8 + rx % Math.max(1, w - 16);
            // Filo en el momento del desprendimiento (misma curva de salida).
            float pb = DROP_DETACH[i];
            float easeB = 1f - (1f - pb) * (1f - pb);
            int edgeAtBirth = splashForward
                ? (int)(easeB * (h + 18)) - 9
                : h - ((int)(easeB * (h + 18)) - 9);
            float sq = slideAge / (float) DROP_SLIDE_MS;
            int yDrop = edgeAtBirth + dir * (int)(Math.pow(sq, 0.8) * DROP_SLIDE_MAX);
            float fade = 1f - sq;
            // Rastro fino desde el nacimiento hasta la posicion actual.
            int aTrail = (int)(fade * 40);
            if (aTrail > 4) {
                int y0 = Math.max(0, Math.min(edgeAtBirth, yDrop));
                int y1 = Math.min(h, Math.max(edgeAtBirth, yDrop));
                if (y1 > y0) g.fill(x, y0, x + 1, y1, (aTrail << 24) | 0x2E1C0C);
            }
            int aDrop = Math.min(85, (int)(fade * 95));
            if (aDrop > 6 && yDrop >= 0 && yDrop < h - 1) {
                g.fill(x - 1, yDrop, x + 1, yDrop + 2, (aDrop << 24) | 0x241608);
            }
        }
    }

    /**
     * Gotas de tinta detras del filo de la cortina. Pocas, pequenitas y sin
     * destellos (alfas bajas); la posicion es estable para cada apertura.
     */
    private static void drawSplashes(GuiGraphics g, int w, int h, int edgeY,
                                     float strength, boolean settled) {
        if (strength <= 0.02f) return;
        int dir = splashForward ? -1 : 1; // atras del filo segun el sentido
        long s = splashSeed;
        for (int i = 0; i < 5; i++) {
            s = s * 6364136223846793005L + 1442695040888963407L;
            int rx = (int)((s >>> 33) & 0x7FFFFFFF);
            s = s * 6364136223846793005L + 1442695040888963407L;
            int ry = (int)((s >>> 33) & 0x7FFFFFFF);
            int x = 6 + rx % Math.max(1, w - 12);
            int off = 3 + ry % 13; // 3..15 px detras del filo
            int y = edgeY + dir * off;
            if (y < 1 || y > h - 2) continue;
            float drop = strength * (1f - off / 18f);
            if (settled) drop *= 0.8f;
            int a = (int)(drop * 95);
            if (a < 8) continue;
            int size = (ry % 3 == 0) ? 2 : 1;
            g.fill(x, y, Math.min(w, x + size), Math.min(h, y + size), (a << 24) | 0x241608);
        }
    }
}
