package com.eternalcraft.gripeverde.client.lore;

import com.eternalcraft.gripeverde.client.audio.ModSounds;
import com.eternalcraft.gripeverde.client.audio.UiSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * El telegrafo oculto.
 *
 * El lector puede "hablar" en voz baja desde la vigilia inicial: las letras
 * que escribe se acumulan como tinta tenue abajo a la izquierda. Ciertas
 * palabras y cifras, dichas en el lugar correcto, abren puertas que el
 * gabinete creia cerradas.
 *
 * v0.16.0
 */
public final class HiddenCipher {

    private HiddenCipher() {}

    private static final StringBuilder buffer = new StringBuilder();
    private static long lastTypeMs = 0L;

    // Secuencia de campanadas (codigo 1152)
    private static int chimesLeft = 0;
    private static long nextChimeMs = 0L;
    private static int chimeIndex = 0;

    /** Llamado por DiaryTitleScreen.charTyped. Devuelve true si consumio la letra. */
    public static boolean onCharTyped(char c) {
        char u = Character.toUpperCase(c);
        boolean valid = (u >= 'A' && u <= 'Z') || (u >= '0' && u <= '9');
        if (!valid) return false;
        long now = System.currentTimeMillis();
        if (now - lastTypeMs > 6000L) buffer.setLength(0);
        lastTypeMs = now;
        if (buffer.length() < 12) buffer.append(u);
        try {
            // Clac de telegrafo
            UiSounds.playCustom(ModSounds.MENU_TICK.get(), 0.12f, 1.35f);
        } catch (Throwable ignored) {}
        checkCodes();
        return true;
    }

    private static void checkCodes() {
        String s = buffer.toString();
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        if (s.endsWith("1897")) {
            buffer.setLength(0);
            if (Discoveries.discover(Discoveries.CODE_1897)) {
                Marginalia.queueOnce(Marginalia.N_CORRECTION);
            }
            telegramFlashUntilMs = System.currentTimeMillis() + 5200L;
            telegramFlashStartMs = System.currentTimeMillis();
            try {
                UiSounds.playCustom(ModSounds.MENU_DOOR.get(), 0.20f, 0.80f);
                UiSounds.playCustom(ModSounds.UI_NOTIFICATION.get(), 0.15f, 0.70f);
            } catch (Throwable ignored) {}
        } else if (s.endsWith("1152")) {
            buffer.setLength(0);
            boolean first = Discoveries.discover(Discoveries.CODE_1152);
            // La campana cuenta trece, aunque nadie lo haya pedido.
            chimesLeft = 13;
            chimeIndex = 0;
            nextChimeMs = System.currentTimeMillis() + 400L;
            if (first) {
                ChroniclerMemory.setCounter("thirteenth_window", 1);
            }
        } else if (s.endsWith("HOLLOWAY")) {
            buffer.setLength(0);
            boolean first = Discoveries.discover(Discoveries.CODE_HOLLOWAY);
            if (first) {
                ChroniclerMemory.setCounter("fraude_stamp", 1);
                Marginalia.queueOnce(Marginalia.N_HOLLOWAY);
            }
            try {
                com.eternalcraft.gripeverde.client.screens.DiaryArchiveScreen.setInitialDoc(
                    com.eternalcraft.gripeverde.client.screens.DiaryArchiveScreen.DOC_HOLLOWAY);
                mc.setScreen(new com.eternalcraft.gripeverde.client.screens.DiaryArchiveScreen(null));
            } catch (Throwable ignored) {}
        } else if (s.endsWith("PEREIRA")) {
            buffer.setLength(0);
            boolean first = Discoveries.discover(Discoveries.CODE_PEREIRA);
            if (first) {
                ChroniclerMemory.setCounter("pereira_offended", 1);
                Marginalia.queueOnce(Marginalia.N_PEREIRA);
            }
            pereiraFlashUntilMs = System.currentTimeMillis() + 900L;
            try {
                // Susurro ofendido
                UiSounds.playCustom(ModSounds.MENU_AMBIENT_OCCASIONAL.get(), 0.30f, 0.60f);
                UiSounds.playCustom(ModSounds.MENU_CHAIN.get(), 0.18f, 0.42f);
            } catch (Throwable ignored) {}
        }
    }

    /** Tick llamado desde DiaryTitleScreen: secuencia de campanadas. */
    public static void tick() {
        if (chimesLeft > 0 && System.currentTimeMillis() >= nextChimeMs) {
            chimesLeft--;
            chimeIndex++;
            float pitch = 1.05f - chimeIndex * 0.028f;
            float vol = 0.34f - chimeIndex * 0.012f;
            try {
                UiSounds.playCustom(ModSounds.MENU_BELL.get(), Math.max(0.12f, vol), Math.max(0.55f, pitch));
            } catch (Throwable ignored) {}
            nextChimeMs = System.currentTimeMillis() + 620L;
        }
        // limpiar buffer por inactividad
        if (buffer.length() > 0 && System.currentTimeMillis() - lastTypeMs > 6000L) {
            buffer.setLength(0);
        }
    }

    public static boolean isChiming() { return chimesLeft > 0; }

    // ── Telegrama (codigo 1897) ──
    private static long telegramFlashUntilMs = 0L;
    private static long telegramFlashStartMs = 0L;

    /** Dibuja el telegrama recibido en el centro de la vigilia. */
    public static void drawTelegram(GuiGraphics g, int w, int h) {
        long now = System.currentTimeMillis();
        if (now >= telegramFlashUntilMs) return;
        long age = now - telegramFlashStartMs;
        long remain = telegramFlashUntilMs - now;
        int alpha = 235;
        if (age < 420L) alpha = (int)(235 * age / 420L);
        else if (remain < 900L) alpha = (int)(235 * remain / 900L);
        alpha = Math.max(0, Math.min(235, alpha));
        boolean en = Discoveries.isEnglish();

        int bw = Math.min(340, w - 60);
        int bh = 92;
        int x = (w - bw) / 2;
        int y = Math.max(60, h / 2 - bh - 30);

        int paper = (alpha << 24) | 0xDED0AC;
        int ink = (alpha << 24) | 0x241608;
        int bronze = (Math.min(200, alpha) << 24) | 0x8E6A35;
        int red = (Math.min(210, alpha) << 24) | 0x6E1F16;

        g.fill(x + 3, y + 4, x + bw + 3, y + bh + 4, (Math.min(70, alpha / 3) << 24) | 0x000000);
        g.fill(x, y, x + bw, y + bh, paper);
        g.fill(x, y, x + bw, y + 1, bronze);
        g.fill(x, y + bh - 1, x + bw, y + bh, bronze);
        g.fill(x, y, x + 1, y + bh, bronze);
        g.fill(x + bw - 1, y, x + bw, y + bh, bronze);
        // perforado de telegrafo
        for (int px = x + 8; px < x + bw - 8; px += 10) {
            g.fill(px, y + 3, px + 2, y + 5, (Math.min(120, alpha / 2) << 24) | 0x6B4A2E);
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) return;
        String l1 = en ? "TELEGRAM - 23 MARCH 1897 - 11:52 PM" : "TELEGRAMA - 23 MARZO 1897 - 11:52 PM";
        String l2 = en ? "LAST REPORT FROM THE WATCH POST:" : "ULTIMO PARTE DEL PUESTO DE VIGIA:";
        String l3 = en ? "The bell counted thirteen. The watchman did not answer." : "La campana conto trece. El sereno no respondio.";
        String l4 = en ? "Close the line. No one in. No one out." : "Cierren la linea. Nadie entra. Nadie sale.";
        String l5 = en ? "Signed: no one." : "Firmado: nadie.";
        drawC(g, mc, l1, x + bw / 2, y + 14, red, alpha);
        drawC(g, mc, l2, x + bw / 2, y + 30, ink, alpha);
        drawC(g, mc, l3, x + bw / 2, y + 44, ink, alpha);
        drawC(g, mc, l4, x + bw / 2, y + 58, ink, alpha);
        drawC(g, mc, l5, x + bw / 2, y + 74, bronze, alpha);
    }

    private static void drawC(GuiGraphics g, Minecraft mc, String s, int cx, int y, int color, int alpha) {
        if (s == null) return;
        int wtxt = mc.font.width(s);
        g.drawString(mc.font, s, cx - wtxt / 2, y, color, false);
    }

    // ── Flash verde del codigo PEREIRA ──
    private static long pereiraFlashUntilMs = 0L;

    public static void drawPereiraFlash(GuiGraphics g, int w, int h) {
        long now = System.currentTimeMillis();
        if (now >= pereiraFlashUntilMs) return;
        long rem = pereiraFlashUntilMs - now;
        int a = (int)(46 * rem / 900L);
        if (a > 2) g.fill(0, 0, w, h, (a << 24) | 0x2A3A18);
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) return;
        boolean en = Discoveries.isEnglish();
        String msg = en ? "...who is reading?" : "...quien esta leyendo?";
        int mw = mc.font.width(msg);
        int malpha = Math.min(200, (int)(a * 4.5));
        g.drawString(mc.font, msg, w / 2 - mw / 2, h / 2, (malpha << 24) | 0xC8D8A8, false);
    }

    /** Tinta tenue abajo a la izquierda mostrando lo que la vigilia oye. */
    public static void drawBuffer(GuiGraphics g, int w, int h) {
        if (buffer.length() == 0) return;
        long now = System.currentTimeMillis();
        if (now - lastTypeMs > 6000L) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) return;
        String s = buffer.toString().toLowerCase();
        int alpha = 74;
        // el ultimo caracter parpadea
        g.drawString(mc.font, s, 10, h - 44, (alpha << 24) | 0x8E7A55, false);
        if ((now / 400L) % 2L == 0L) {
            int cx = 10 + mc.font.width(s);
            g.fill(cx + 1, h - 43, cx + 3, h - 35, (alpha << 24) | 0x8E7A55);
        }
    }
}
