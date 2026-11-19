package com.eternalcraft.gripeverde.client.render;

import com.eternalcraft.gripeverde.client.lore.Discoveries;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;

/**
 * v0.240.0: el telegrama de defuncion.
 *
 * Antes la pantalla de muerte se forraba con dos franjas de papel de borde a
 * borde: quedaba aparatoso y tapaba el mundo. Ahora es un solo papel chico,
 * apoyado en la esquina superior izquierda, con el aviso de condolencias tal
 * como lo recibian los familiares: membrete, dos renglones y el sello. El
 * centro de la pantalla queda libre para el titulo y el puntaje del juego.
 * Nada mas se toca: ni velo, ni oscurecido, ni animacion.
 *
 * Los botones del juego se visten de papel sin duplicar su logica: el original
 * se oculta y en su lugar se pone un boton de la casa que dispara su misma
 * accion (reaparecer, menu principal, espectador en modo extremo).
 */
public final class DeathPaper {

    // v0.248.0: el aviso va en la esquina, chico, para no comerse el titulo
    // del juego ni el puntaje. Antes se centraba arriba y tapaba "Estas muerto".
    private static final int PAPER_W_MAX = 296;
    // v0.279.0: el aviso crece un poco para dar cabida al parte del deceso.
    private static final int PAPER_H = 92;
    private static final int PAPER_MARGIN = 8;

    private DeathPaper() {
    }

    /** Enganche desde ScreenEvent.Init.Post: solo para la pantalla de muerte. */
    public static void onInit(Screen screen) {
        if (!(screen instanceof DeathScreen)) return;
        try {
            skinButtons(screen);
        } catch (Throwable ignored) {
        }
    }

    private static void skinButtons(Screen screen) {
        PaperButtons.dressAll(screen);
    }

    /** v0.279.0: lo que el libro de la aduana escribe como causa. */
    private static String deathCause(Screen screen, boolean english) {
        try {
            for (Class<?> c = screen.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                    if (f.getType() != net.minecraft.network.chat.Component.class) continue;
                    f.setAccessible(true);
                    Object v = f.get(screen);
                    if (v instanceof net.minecraft.network.chat.Component comp) {
                        String text = comp.getString();
                        if (text != null && !text.isBlank()) return text;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return english ? "cause not noted" : "causa sin anotar";
    }

    /** v0.279.0: donde quedo el cuerpo, en la avenida o donde sea. */
    private static String deathWhere(boolean english) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null || mc.level == null) return "";
            String dim = mc.level.dimension().location().getPath();
            String place;
            switch (dim) {
                case "the_nether" -> place = english ? "the lower ward" : "la sala baja";
                case "the_end" -> place = english ? "the far shore" : "la orilla lejana";
                default -> place = english ? "the avenue" : "la avenida";
            }
            String coords = mc.player.getBlockX() + ", " + mc.player.getBlockY()
                + ", " + mc.player.getBlockZ();
            return (english ? "at " : "en ") + place + "  ·  " + coords;
        } catch (Throwable ignored) {
            return "";
        }
    }

    /** v0.279.0: el dia de la partida y la hora del reloj, que no perdonan. */
    private static String deathWhen(boolean english) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || mc.player == null) return "";
            long day = mc.level.getDayTime() / 24000L + 1L;
            java.util.Calendar cal = java.util.Calendar.getInstance();
            String clock = String.format(java.util.Locale.ROOT, "%02d:%02d",
                cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE));
            int level = mc.player.experienceLevel;
            return (english ? "day " : "dia ") + day + "  ·  " + clock
                + "  ·  " + (english ? "level " : "nivel ") + level;
        } catch (Throwable ignored) {
            return "";
        }
    }

    public static void render(GuiGraphics g, Screen screen) {
        if (g == null || !(screen instanceof DeathScreen)) return;
        int w = screen.width;
        int h = screen.height;
        if (w < 220 || h < 200) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc != null ? mc.font : null;
        if (font == null) return;
        boolean english = Discoveries.isEnglish();

        // v0.340.0: el parte nacia encima del "estas muerto" y del puntaje.
        // Ahora se queda en su columna: nunca pasa de la mitad del ancho (donde
        // el juego centra sus renglones) ni de la cuarta parte del alto.
        int pw = Math.min(PAPER_W_MAX, Math.max(140, (w - 210) / 2 - 6));
        int ph = Math.min(PAPER_H, Math.max(60, h / 4 - 12));
        int px = PAPER_MARGIN;
        int py = PAPER_MARGIN;
        PaperTexture.drawTornSheet(g, px, py, pw, ph, PaperTexture.PaperKind.OFFICIAL);

        String head = english ? "CEDA - BUREAU OF PORTS" : "CEDA - SANIDAD DE PUERTOS";
        String line1 = english
            ? "Deepest regrets: the bearer is deceased."
            : "Lamento informar el deceso del portador.";

        int hy = py + 6;
        g.drawString(font, DiaryChrome.fitCached(font, head, pw - 16), px + 8, hy,
            DiaryChrome.INK_TITLE, false);
        DiaryChrome.drawPencilRule(g, px + 8, hy + 10, pw - 16, px * 31 + py * 17 + 5);

        // Pie: filete, registro a la izquierda y sello a la derecha, cada uno
        // con su sitio. Antes compartian renglon con la fecha y se pisaban.
        int footY = py + ph - 15;
        int bodyLimit = footY - 12;
        DiaryChrome.drawPencilRule(g, px + 8, footY - 5, pw - 16, px * 17 + py * 13 + 3);

        int ledger = py + 21;
        ledger = writeRow(g, font, line1, px, pw, ledger, bodyLimit, DiaryChrome.INK_SUBTITLE);
        String cause = deathCause(screen, english);
        ledger = writeRow(g, font, cause, px, pw, ledger, bodyLimit, DiaryChrome.INK_TITLE);
        String where = deathWhere(english);
        ledger = writeRow(g, font, where, px, pw, ledger, bodyLimit, DiaryChrome.INK_SUBTITLE);
        String when = deathWhen(english);
        ledger = writeRow(g, font, when, px, pw, ledger, bodyLimit, DiaryChrome.INK_FLAVOR);
        String courtesy = english ? "The port notes it in its ledger."
            : "La aduana lo anota en su libro.";
        ledger = writeRow(g, font, courtesy, px, pw, ledger, bodyLimit, DiaryChrome.INK_FLAVOR);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        String[] romanMonths = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII" };
        String dated = (english ? "dated " : "fechado ")
            + cal.get(java.util.Calendar.DAY_OF_MONTH) + " \u00b7 "
            + romanMonths[cal.get(java.util.Calendar.MONTH)] + " \u00b7 1897";
        writeRow(g, font, dated, px, pw, ledger, bodyLimit, DiaryChrome.INK_FLAVOR);

        String reg = "EC-QV/1897";
        String word = english ? "DECEASED" : "DECESO";
        int ww = DiaryChrome.widthCached(font, word);
        int rw = DiaryChrome.widthCached(font, reg);
        int sx = px + pw - 8 - ww;
        if (sx > px + 10) {
            int stamp = DiaryTheme.withAlpha(0x005C0A08, 190);
            int stampInk = DiaryTheme.withAlpha(0x005C0A08, 215);
            g.fill(sx - 3, footY - 2, sx + ww + 3, footY - 1, stamp);
            g.fill(sx - 3, footY + 9, sx + ww + 3, footY + 10, stamp);
            g.fill(sx - 3, footY - 2, sx - 2, footY + 10, stamp);
            g.fill(sx + ww + 2, footY - 2, sx + ww + 3, footY + 10, stamp);
            g.drawString(font, word, sx, footY, stampInk, false);
            // El registro solo si de verdad queda hueco a la izquierda del sello.
            if (px + 10 + rw + 8 <= sx - 4) {
                g.drawString(font, reg, px + 8, footY, DiaryChrome.INK_FLAVOR, false);
            }
        } else {
            g.drawString(font, DiaryChrome.fitCached(font, reg, pw - 16), px + 8, footY,
                DiaryChrome.INK_FLAVOR, false);
        }
    }

    /** v0.340.0: escribe un renglon si cabe; devuelve el siguiente cursor. */
    private static int writeRow(GuiGraphics g, Font font, String text, int px, int pw,
                                int y, int limit, int color) {
        if (text == null || text.isEmpty()) return y;
        if (y + 9 > limit) return y;
        g.drawString(font, DiaryChrome.fitCached(font, text, pw - 16), px + 8, y, color, false);
        return y + 10;
    }
}
