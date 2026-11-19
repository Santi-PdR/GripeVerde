package com.eternalcraft.gripeverde.client.hud;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * v0.295.0: el reloj de bolsillo, en partida.
 *
 * Una tira de papel rasgada en la esquina con lo que el cuaderno no puede
 * callar mientras se juega: la hora de verdad, el dia de la semana, el dia y
 * la hora del mundo, el clima, las coordenadas y el rato que se lleva dentro.
 *
 * No dibuja nada en menus, ni con F3 abierto, ni antes de que el mundo exista.
 */
public final class WatchHud {

    private static final int SLIP_W = 96;
    private static final int SLIP_H = 42;
    private static final int MARGIN = 6;

    private static long worldSinceMs = 0L;
    private static long lastWorldId = Long.MIN_VALUE;
    private static String cachedLine1 = "";
    private static String cachedLine2 = "";
    private static String cachedLine3 = "";
    private static long cachedAtMs = -1L;

    private WatchHud() {
    }

    /** Enganche desde RegisterGuiOverlaysEvent. */
    public static void render(GuiGraphics g, int screenW, int screenH) {
        if (!enabled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) return;
        if (mc.level == null || mc.player == null) return;
        if (mc.screen != null) return;                 // en un menu no hay reloj
        if (mc.options != null && mc.options.renderDebug) return; // F3 manda
        if (screenW < 200 || screenH < 120) return;

        long now = System.currentTimeMillis();
        long worldId = mc.level.hashCode();
        if (worldId != lastWorldId || worldSinceMs <= 0L) {
            lastWorldId = worldId;
            worldSinceMs = now;
        }
        refresh(now, mc);

        int x = MARGIN;
        int y = MARGIN;
        int corner = corner();
        if (corner == 1) x = screenW - SLIP_W - MARGIN;
        else if (corner == 2) x = MARGIN;

        y = corner == 2 ? screenH - SLIP_H - MARGIN : y;

        int alpha = fadeIn(now);
        if (alpha <= 4) return;

        PaperTexture.drawTornSlip(g, x, y, SLIP_W, SLIP_H, 0xE6D2A6, 0x422810, alpha);
        Font font = mc.font;
        int ink = (Math.min(255, alpha + 20) << 24) | 0x241608;
        int soft = (Math.min(200, alpha) << 24) | 0x654025;

        g.drawString(font, cachedLine1, x + 7, y + 6, ink, false);
        g.drawString(font, cachedLine2, x + 7, y + 17, soft, false);
        g.drawString(font, cachedLine3, x + 7, y + 28, soft, false);

        // v0.301.0: con mapa arriba, las coordenadas van en su propio papelito
        // abajo a la derecha, como el renglon que el mapa escribe bajo si mismo.
        if (minimapPresent() && coords() && cachedLine3.length() > 0) {
            int cw = Math.min(SLIP_W, Math.max(52, font.width(cachedLine3) + 16));
            int cx2 = screenW - cw - MARGIN;
            int cy2 = screenH - 22 - MARGIN;
            PaperTexture.drawTornSlip(g, cx2, cy2, cw, 20, 0xE6D2A6, 0x422810, alpha);
            g.drawString(font, cachedLine3, cx2 + 5, cy2 + 6, ink, false);
        }

        // El lacre: un cuadradito rojo que late una vez por minuto, sin destellos.
        // v0.296.0: con movimiento reducido el lacre se queda quieto.
        int pulse = reducedMotion()
            ? 200
            : (int) (255 * (0.55 + 0.45 * Math.sin(now % 60_000L / 60_000.0 * Math.PI * 2)));
        int red = (Math.min(255, Math.max(90, pulse * alpha / 255)) << 24) | 0x6E1F16;
        g.fill(x + SLIP_W - 12, y + 6, x + SLIP_W - 7, y + 11, red);
    }

    private static boolean enabled() {
        try {
            return ClientConfig.HUD_WATCH_ENABLED.get();
        } catch (Throwable ignored) {
            return true;
        }
    }

    private static boolean reducedMotion() {
        try {
            return com.eternalcraft.gripeverde.client.util.MotionPolicy.isReduced();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean coords() {
        try {
            return ClientConfig.HUD_WATCH_COORDS.get();
        } catch (Throwable ignored) {
            return true;
        }
    }

    private static int corner() {
        int corner;
        try {
            corner = Math.max(0, Math.min(2, ClientConfig.HUD_WATCH_CORNER.get()));
        } catch (Throwable ignored) {
            corner = 0;
        }
        // v0.301.0: si hay mapa en la esquina (JourneyMap y compania), el reloj
        // se aparta abajo a la izquierda en vez de taparlo.
        if (corner != 2 && minimapPresent()) return 2;
        return corner;
    }

    /** v0.301.0: hay un mapa ocupando las esquinas de arriba. */
    private static boolean minimapPresent() {
        try {
            return net.minecraftforge.fml.ModList.get().isLoaded("journeymap")
                || net.minecraftforge.fml.ModList.get().isLoaded("xaerominimap")
                || net.minecraftforge.fml.ModList.get().isLoaded("xaeroworldmap");
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** Entrada suave de medio segundo; con movimiento reducido aparece de golpe. */
    private static int fadeIn(long now) {
        boolean reduced;
        try {
            reduced = com.eternalcraft.gripeverde.client.util.MotionPolicy.isReduced();
        } catch (Throwable ignored) {
            reduced = false;
        }
        if (reduced) return 232;
        long age = now - worldSinceMs;
        if (age >= 500L) return 232;
        return (int) Math.max(0, Math.min(232, 232 * age / 500L));
    }

    /** Las tres lineas se rehacen dos veces por segundo, no en cada cuadro. */
    private static void refresh(long now, Minecraft mc) {
        if (cachedAtMs > 0 && now - cachedAtMs < 500L) return;
        cachedAtMs = now;
        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        java.util.Calendar cal = java.util.Calendar.getInstance();

        String[] daysEs = {"dom", "lun", "mar", "mie", "jue", "vie", "sab"};
        String[] daysEn = {"sun", "mon", "tue", "wed", "thu", "fri", "sat"};
        int dow = Math.max(0, Math.min(6, cal.get(java.util.Calendar.DAY_OF_WEEK) - 1));
        String clock = String.format(java.util.Locale.ROOT, "%02d:%02d",
            cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE));
        cachedLine1 = clock + "  " + (en ? daysEn : daysEs)[dow];

        long day = 1L;
        long worldClock = 0L;
        boolean rain = false;
        boolean storm = false;
        try {
            day = mc.level.getDayTime() / 24000L + 1L;
            worldClock = mc.level.getDayTime() % 24000L;
            rain = mc.level.isRaining();
            storm = mc.level.isThundering();
        } catch (Throwable ignored) {
        }
        int hours = (int) ((worldClock / 1000L + 6L) % 24L);
        int minutes = (int) ((worldClock % 1000L) * 60L / 1000L);
        String weather = storm ? (en ? "storm" : "tormenta")
            : (rain ? (en ? "rain" : "lluvia") : (en ? "clear" : "despejado"));
        cachedLine2 = (en ? "day " : "dia ") + day + "  "
            + String.format(java.util.Locale.ROOT, "%02d:%02d", hours, minutes)
            + "  " + weather;

        if (coords() && mc.player != null) {
            try {
                cachedLine3 = mc.player.getBlockX() + " " + mc.player.getBlockY()
                    + " " + mc.player.getBlockZ();
            } catch (Throwable ignored) {
                cachedLine3 = "";
            }
        } else {
            long mins = Math.max(0L, (now - worldSinceMs) / 60_000L);
            cachedLine3 = (en ? "" : "") + mins + (en ? " min in" : " min aqui");
        }
        cachedLine1 = DiaryChrome.fitText(mc.font, cachedLine1, SLIP_W - 22);
        cachedLine2 = DiaryChrome.fitText(mc.font, cachedLine2, SLIP_W - 14);
        cachedLine3 = DiaryChrome.fitText(mc.font, cachedLine3, SLIP_W - 14);
    }
}
