package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/**
 * v0.26.0: el pasar de las hojas.
 * v0.27.0: el pliegue — la cortina tiene sentido (avanzar baja, volver sube).
 * v0.28.0: la tinta fresca gotea (gotas con rastro fino).
 * v0.29.0: LA VUELTA ES CORTES (pedido del owner). Al REGRESAR de una mesa
 * ya no corre el barrido completo al reves: la hoja cae de arriba con un
 * gesto breve (170 ms, banda superior que se repliega) y listo. Toda la
 * ceremonia (barrido, salpicaduras, gotas) queda reservada para AVANZAR.
 * Ademas el velo se optimizo: fuera de la ventana del efecto no se hace
 * ninguna cuenta por frame (fast path), la elegibilidad de la pantalla se
 * cachea por clase y el modo reducido se resuelve una vez por apertura.
 *
 * Al abrir cualquier pantalla del gabinete (paquete client.screens), fuera
 * de una sesion de juego, una cortina de tinta recorre la pagina revelando la
 * hoja nueva. Las interfaces abiertas durante una partida no reciben esta
 * transicion. Con movimiento reducido la transicion es solo un fundido breve.
 */
public final class DiaryTransitions {

    private DiaryTransitions() {}

    private static final String PREFIX = "com.eternalcraft.gripeverde.client.screens.";
    private static final long DUR_MS = 260L;          // avanzar: ceremonia completa
    private static final long BACK_DUR_MS = 170L;     // regresar: gesto breve
    private static final long DUR_REDUCED_MS = 130L;
    private static final long SETTLE_MS = 450L;       // la hoja se acuesta
    private static final long SPLASH_TAIL_MS = 500L;

    // Tres gotas que se desprenden del filo en momentos del pliegue (30/55/80%)
    // — solo al avanzar; la vuelta cortes no tiene tiempo ni ganas de gotear.
    private static final int DROP_COUNT = 3;
    private static final float[] DROP_DETACH = { 0.30f, 0.55f, 0.80f };
    private static final long DROP_SLIDE_MS = 550L;
    private static final int DROP_SLIDE_MAX = 26;

    private static Screen current = null;
    private static long openedAtMs = 0L;
    // v0.29.0: caches por apertura — el frame caliente no toca config ni
    // cadenas: elegibilidad por clase, modo reducido y fin del efecto.
    private static Class<?> lastCheckedClass = null;
    private static boolean lastCheckedCabinet = false;
    private static boolean reducedNow = false;
    private static long durNowMs = DUR_MS;
    private static long fxEndMs = DUR_MS;

    // Direccion decidida por la pila de mesas visitadas. Avanzar (mesa
    // nueva) -> la cortina baja; regresar (mesa ya visitada) -> la hoja cae
    // de arriba con gesto breve. Se alimenta desde ScreenEvent.Opening.
    private static final java.util.ArrayDeque<Class<?>> NAV = new java.util.ArrayDeque<>();
    private static boolean directionForward = true;
    private static boolean pendingHasDirection = false;

    // Salpicaduras y gotas del filo (semilla por apertura, estable)
    private static long splashSeed = 0L;
    private static boolean splashForward = true;

    private static boolean reducedMotionConfig() {
        return com.eternalcraft.gripeverde.client.util.ClientAccessibility.reducedMotion();
    }

    private static boolean isCabinetScreen(Screen screen) {
        Class<?> cls = screen.getClass();
        if (cls != lastCheckedClass) {
            lastCheckedClass = cls;
            lastCheckedCabinet = cls.getName().startsWith(PREFIX);
        }
        return lastCheckedCabinet;
    }

    /**
     * Llamado desde ScreenEventHandler.onScreenOpening con la pantalla que se
     * va y la que llega (ya con el redirect aplicado). Decide el sentido del
     * pliegue segun si la mesa de destino estaba abierta antes.
     */
    public static void noteOpening(Screen from, Screen to) {
        if (com.eternalcraft.gripeverde.client.util.ClientAccessibility.hasActiveGameSession()) {
            current = null;
            NAV.clear();
            pendingHasDirection = false;
            return;
        }
        if (to == null || !isCabinetScreen(to)) {
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
        if (screen == null || !isCabinetScreen(screen)) {
            current = null;
            return;
        }
        if (com.eternalcraft.gripeverde.client.util.ClientAccessibility.hasActiveGameSession()) {
            current = null;
            NAV.clear();
            pendingHasDirection = false;
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
            // v0.29.0: todo lo dependiente de config y duraciones se resuelve
            // una sola vez por apertura.
            reducedNow = reducedMotionConfig();
            durNowMs = reducedNow ? DUR_REDUCED_MS
                : (splashForward ? DUR_MS : BACK_DUR_MS);
            long tailMs = (reducedNow || !splashForward) ? 0L
                : Math.max(SPLASH_TAIL_MS,
                    (long)(DROP_DETACH[DROP_COUNT - 1] * durNowMs) + DROP_SLIDE_MS);
            fxEndMs = durNowMs + Math.max(SETTLE_MS, tailMs);
        }
        long age = now - openedAtMs;
        if (age < 0 || age >= fxEndMs) return; // nada que dibujar: costo cero

        int w = g.guiWidth(), h = g.guiHeight();
        long dur = durNowMs;
        boolean reduced = reducedNow;

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
            // Cola solo al avanzar: salpicaduras que mueren y gotas que
            // terminan de resbalar. La vuelta cortes termina con el gesto.
            if (!reduced && splashForward) {
                if (age < dur + SPLASH_TAIL_MS) {
                    float tail = 1f - (age - dur) / (float) SPLASH_TAIL_MS;
                    drawSplashes(g, w, h, h, tail * 0.55f, true);
                }
                drawDrops(g, w, h, dur, age);
            }
            return;
        }

        float p = age / (float) dur;
        float ease = 1f - (1f - p) * (1f - p); // salida suave

        // Velo sepia que se disuelve sobre toda la hoja. La vuelta es un
        // velo mas tenue: gesto, no ceremonia.
        int veilBase = splashForward ? 150 : 110;
        int veil = (int)((1f - ease) * veilBase);
        if (veil > 0) {
            g.fill(0, 0, w, h, (veil << 24) | 0x1A1108);
        }
        if (reduced) return;

        if (!splashForward) {
            // ── LA VUELTA CORTES (v0.29.0) ──
            // La hoja cae de arriba: una banda de tinta en el borde superior
            // se repliega y se disuelve en ~170 ms. Sin salpicaduras, sin
            // gotas, sin barrido de pantalla entera.
            float inv = 1f - ease;
            int bandH = (int)(46 * inv);
            int bandA = (int)(120 * inv);
            if (bandH > 0 && bandA > 0) {
                g.fill(0, 0, w, bandH, (Math.min(150, bandA) << 24) | 0x241608);
                int ly = Math.min(h - 1, Math.max(0, bandH));
                g.fill(0, ly, w, ly + 1, (Math.min(200, bandA + 60) << 24) | 0x8E6A35);
            }
            return;
        }

        // ── AVANZAR: franja de tinta que revela la pagina de arriba abajo ──
        int lineY = (int)(ease * (h + 18)) - 9;
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

    /**
     * Gotas que se desprenden del filo en tres momentos del pliegue y
     * resbalan un trecho breve dejando un rastro fino. Solo al avanzar: la
     * vuelta cortes ni las llama. Alfas bajas: jamas un destello.
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
}
