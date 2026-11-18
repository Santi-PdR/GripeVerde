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

        int pw = Math.min(PAPER_W_MAX, w - PAPER_MARGIN * 2 - 8);
        int px = PAPER_MARGIN;
        int py = PAPER_MARGIN;
        PaperTexture.drawTornSheet(g, px, py, pw, PAPER_H, PaperTexture.PaperKind.OFFICIAL);

        String head = english ? "CEDA - BUREAU OF PORTS" : "CEDA - SANIDAD DE PUERTOS";
        String line1 = english
            ? "Deepest regrets: the bearer is deceased."
            : "Lamento informar el deceso del portador.";
        String line2 = english
            ? "The port notes it in its ledger."
            : "La aduana lo anota en su libro.";

        int hy = py + 7;
        g.drawString(font, DiaryChrome.fitCached(font, head, pw - 16), px + 8, hy,
            DiaryChrome.INK_TITLE, false);
        DiaryChrome.drawPencilRule(g, px + 8, hy + 10, pw - 16, px * 31 + py * 17 + 5);

        int l1 = py + 22;
        g.drawString(font, DiaryChrome.fitCached(font, line1, pw - 16), px + 8, l1,
            DiaryChrome.INK_SUBTITLE, false);

        // v0.279.0: el parte del deceso, en el orden en que se lee.
        boolean en2 = english;
        int ledger = l1 + 11;
        String cause = deathCause(screen, en2);
        if (!cause.isEmpty()) {
            g.drawString(font, DiaryChrome.fitCached(font, cause, pw - 16), px + 8, ledger,
                DiaryChrome.INK_TITLE, false);
            ledger += 11;
        }
        String where = deathWhere(en2);
        if (!where.isEmpty()) {
            g.drawString(font, DiaryChrome.fitCached(font, where, pw - 16), px + 8, ledger,
                DiaryChrome.INK_SUBTITLE, false);
            ledger += 11;
        }
        String when = deathWhen(en2);
        if (!when.isEmpty()) {
            g.drawString(font, DiaryChrome.fitCached(font, when, pw - 16), px + 8, ledger,
                DiaryChrome.INK_FLAVOR, false);
            ledger += 11;
        }
        // v0.281.0: el renglon de cortesia solo si queda sitio antes del pie.
        if (ledger + 10 <= py + PAPER_H - 22) {
            g.drawString(font, DiaryChrome.fitCached(font, line2, pw - 16), px + 8, ledger,
                DiaryChrome.INK_FLAVOR, false);
        }

        // Pie del papel: registro a la izquierda y sello a la derecha.
        int footY = py + PAPER_H - 16;
        DiaryChrome.drawPencilRule(g, px + 8, footY - 4, pw - 16, px * 17 + py * 13 + 3);
        // v0.262.0: la fecha del deceso, en numeral romano, y la firma de la
        // casa, que responde por el parte como cualquier otro.
        String[] romanMonths = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII" };
        java.util.Calendar cal = java.util.Calendar.getInstance();
        String date = (english ? "dated " : "fechado ")
            + cal.get(java.util.Calendar.DAY_OF_MONTH) + " \u00b7 "
            + romanMonths[cal.get(java.util.Calendar.MONTH)] + " \u00b7 1897";
        if (ledger + 12 <= footY - 6) {
            g.drawString(font, DiaryChrome.fitCached(font, date, pw - 16), px + 8, ledger + 3,
                DiaryChrome.INK_FLAVOR, false);
        }
        String by = english ? "for the house, S.P." : "por la casa, S.P.";
        int bw = DiaryChrome.widthCached(font, by);
        g.drawString(font, by, px + (pw - bw) / 2, footY,
            DiaryTheme.withAlpha(0x006B5533, 150), false);
        try { DiaryChrome.drawInkBlot(g, px + pw - 26, l1 + 12, 1, false); } catch (Throwable ignored) {}

        String reg = "EC-QV/1897";
        g.drawString(font, reg, px + 8, footY, DiaryChrome.INK_FLAVOR, false);

        String word = english ? "DECEASED" : "DECESO";
        int ww = DiaryChrome.widthCached(font, word);
        int sx = px + pw - 8 - ww;
        int stamp = DiaryTheme.withAlpha(0x005C0A08, 190);
        int stampInk = DiaryTheme.withAlpha(0x005C0A08, 215);
        g.fill(sx - 3, footY - 2, sx + ww + 3, footY - 1, stamp);
        g.fill(sx - 3, footY + 9, sx + ww + 3, footY + 10, stamp);
        g.fill(sx - 3, footY - 2, sx - 2, footY + 10, stamp);
        g.fill(sx + ww + 2, footY - 2, sx + ww + 3, footY + 10, stamp);
        g.drawString(font, word, sx, footY, stampInk, false);
    }
}
