package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/**
 * v0.26.0: el pasar de las hojas.
 *
 * Al abrir cualquier pantalla del gabinete (paquete client.screens), una
 * cortina de tinta baja revelando la pagina nueva (~260 ms): la hoja se
 * descubre de arriba abajo, como quien pasa pagina con la mano seca. Con
 * movimiento reducido la transicion es solo un fundido breve, sin barrido.
 * Pantallas vanilla y ajenas quedan intactas.
 */
public final class DiaryTransitions {

    private DiaryTransitions() {}

    private static final String PREFIX = "com.eternalcraft.gripeverde.client.screens.";
    private static final long DUR_MS = 260L;
    private static final long DUR_REDUCED_MS = 130L;

    private static Screen current = null;
    private static long openedAtMs = 0L;

    private static boolean reducedMotion() {
        try { return com.eternalcraft.gripeverde.config.ClientConfig.REDUCED_MOTION.get(); }
        catch (Throwable t) { return false; }
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
        }
        boolean reduced = reducedMotion();
        long dur = reduced ? DUR_REDUCED_MS : DUR_MS;
        long age = now - openedAtMs;
        if (age < 0 || age >= dur) return;

        float p = age / (float) dur;
        float ease = 1f - (1f - p) * (1f - p); // salida suave
        int w = g.guiWidth(), h = g.guiHeight();

        // Velo sepia que se disuelve sobre toda la hoja.
        int veil = (int)((1f - ease) * 150);
        if (veil > 0) {
            g.fill(0, 0, w, h, (veil << 24) | 0x1A1108);
        }

        // Franja de tinta que revela la pagina de arriba abajo.
        if (!reduced) {
            int lineY = (int)(ease * (h + 18)) - 9;
            int bandA = (int)((1f - ease) * 120);
            if (bandA > 0 && lineY > -16) {
                int y0 = Math.max(0, lineY - 14);
                int y1 = Math.min(h, lineY + 16);
                if (y1 > y0) {
                    g.fill(0, y0, w, y1, (Math.min(160, bandA + 40) << 24) | 0x241608);
                    int ly = Math.min(h - 2, Math.max(0, lineY));
                    g.fill(0, ly, w, ly + 2, (Math.min(220, bandA + 80) << 24) | 0x8E6A35);
                }
            }
        }
    }
}
