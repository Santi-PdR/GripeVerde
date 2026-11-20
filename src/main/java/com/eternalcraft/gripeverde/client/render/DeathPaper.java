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
 * borde: quedaba aparatoso y tapaba el mundo. Ahora es un solo papel con el
 * aviso de condolencias tal como lo recibian los familiares: membrete, los
 * renglones del parte y el sello.
 *
 * v0.357.0: el papel se planta en el centro, sobre el rotulo del juego
 * ("Estas muerto"), la causa y el puntaje, que dejan de verse: el parte es lo
 * unico que se lee. Las fichas del pie (reaparecer, menu) quedan debajo, con
 * su aire. Nada mas se toca: ni velo, ni oscurecido, ni animacion.
 *
 * Los botones del juego se visten de papel sin duplicar su logica: el original
 * se oculta y en su lugar se pone un boton de la casa que dispara su misma
 * accion (reaparecer, menu principal, espectador en modo extremo).
 */
public final class DeathPaper {

    // v0.357.0: el aviso pasa al centro, en el sitio del rotulo del juego.
    // Es el unico parte de la pantalla: el juego ya no escribe nada arriba.
    private static final int PAPER_W_MAX = 320;
    // v0.279.0: el aviso crece un poco para dar cabida al parte del deceso.
    private static final int PAPER_H = 104;
    // Aire entre el papel y la primera ficha del pie.
    private static final int PAPER_GAP = 8;

    private DeathPaper() {
    }

    /** Enganche desde ScreenEvent.Init.Post: solo para la pantalla de muerte. */
    public static void onInit(Screen screen) {
        if (!(screen instanceof DeathScreen)) return;
        try {
            skinButtons(screen);
        } catch (Throwable ignored) {
        }
        // v0.345.0: el juego escribe la causa debajo del rotulo y nuestro parte
        // la repite. Se guarda una vez y se calla la del juego, sin tocar el
        // rotulo grande ni el puntaje.
        captureCause(screen);
        // v0.358.0: el lugar y el momento se leen una vez, al abrirse la hoja.
        // Antes se armaban en cada cuadro, con un Calendar y un monton de
        // cadenas nuevas por segundo.
        try {
            boolean english = Discoveries.isEnglish();
            cachedWhere = deathWhere(english);
            cachedWhen = deathWhen(english);
            cachedDated = buildDated(english);
        } catch (Throwable ignored) {
            cachedWhere = "";
            cachedWhen = "";
            cachedDated = "";
        }
    }

    private static String cachedWhere = "";
    private static String cachedWhen = "";
    private static String cachedDated = "";

    /** v0.345.0: la causa que traia el juego, guardada antes de callarla. */
    private static String capturedCause = "";

    /**
     * v0.366.0: se guarda la causa del juego y se apagan TODOS sus rotulos.
     *
     * Antes se buscaba el campo por nombre ("causeOfDeath" / "f_95858_") y en el
     * juego real ese nombre no coincide, asi que no se apagaba nada: el rotulo
     * grande, la causa y el puntaje seguian ahi y se dibujaban por encima del
     * papel. Ahora los campos se reconocen por su tipo -Component-, que el
     * compilador reescribe solo y no depende de la ofuscacion.
     */
    private static void captureCause(Screen screen) {
        capturedCause = "";
        java.util.List<java.lang.reflect.Field> fields = componentFields(screen);
        String titleText = "";
        try {
            titleText = screen.getTitle().getString();
        } catch (Throwable ignored) {
        }
        String best = "";
        for (java.lang.reflect.Field f : fields) {
            try {
                f.setAccessible(true);
                Object v = f.get(screen);
                if (!(v instanceof net.minecraft.network.chat.Component comp)) continue;
                String t = comp.getString();
                if (t == null || t.isBlank()) continue;
                if (t.equals(titleText)) continue;
                if (t.startsWith("Puntuaci\u00f3n") || t.startsWith("Score")) continue;
                if (t.length() > best.length()) best = t;
            } catch (Throwable ignored) {
            }
        }
        capturedCause = best;
        for (java.lang.reflect.Field f : fields) {
            try {
                f.setAccessible(true);
                f.set(screen, net.minecraft.network.chat.Component.empty());
            } catch (Throwable ignored) {
            }
        }
    }

    /** v0.366.0: los campos de texto de la pantalla, sin mirar sus nombres. */
    private static java.util.List<java.lang.reflect.Field> componentFields(Screen screen) {
        java.util.List<java.lang.reflect.Field> out = new java.util.ArrayList<>(4);
        for (Class<?> c = screen.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                if (f.getType() != net.minecraft.network.chat.Component.class) continue;
                out.add(f);
            }
        }
        return out;
    }

    private static void skinButtons(Screen screen) {
        PaperButtons.dressAll(screen);
    }

    /** v0.279.0: lo que el libro de la aduana escribe como causa. */
    private static String deathCause(Screen screen, boolean english) {
        // v0.345.0: la causa ya se leyo al abrir la pantalla; el juego ya no la
        // escribe, asi que se devuelve la guardada.
        if (capturedCause != null && !capturedCause.isBlank()) return capturedCause;
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

        // v0.357.0: el papel se planta en el centro, sobre el rotulo grande del
        // juego ("Estas muerto"), la causa y el puntaje, que quedan tapados. Se
        // mide contra la primera ficha del pie para no tocar los botones.
        int buttonsTop = Integer.MAX_VALUE;
        try {
            for (var child : screen.children()) {
                if (child instanceof DiaryButton b && b.visible) {
                    buttonsTop = Math.min(buttonsTop, b.getY());
                }
            }
        } catch (Throwable ignored) {
        }
        // v0.364.0: la franja que ocupa el juego con su rotulo (escala doble,
        // desde y=60), la causa (y=85) y el puntaje (y=100). El papel se
        // centra en el hueco libre, pero nunca deja esa franja al aire.
        int zoneBottom = 112;
        int top = 8;
        int limit = buttonsTop == Integer.MAX_VALUE ? h - 40 : buttonsTop - PAPER_GAP;
        int room = limit - top;
        if (room < 56) {
            room = Math.max(56, h - 16 - top);
        }
        int pw = Math.min(PAPER_W_MAX, Math.max(180, w - 48));
        int ph = Math.max(56, Math.min(PAPER_H, room));
        int px = (w - pw) / 2;
        // v0.366.0: centrado en el hueco. Los rotulos del juego ya estan
        // apagados, asi que no hay que empujar el papel hacia arriba; solo se
        // deja el resguardo de que su pie no caiga por encima de la franja que
        // ocupaban, por si algun dia no se pudieran apagar.
        int py = top + Math.max(0, (room - ph) / 2);
        if (py + ph < zoneBottom) py = zoneBottom - ph;
        py = Math.max(top, Math.min(py, Math.max(top, h - 16 - ph)));
        // v0.364.0: el papel es translucido (243 de alfa) y el rotulo blanco del
        // juego se transparentaba debajo como un fantasma. Ademas la letra del
        // juego se dibuja en el mismo cuadro y el lienzo la tenia en cola: sin
        // vaciar esa cola, el papel salia por debajo del texto. Se vuelca lo
        // pendiente, se asienta un fondo opaco y encima va la hoja.
        try {
            g.flush();
        } catch (Throwable ignored) {
        }
        g.fill(px, py + 1, px + pw, py + ph - 1, 0xFFE4D6AC);
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
        String where = cachedWhere != null && !cachedWhere.isEmpty()
            ? cachedWhere : deathWhere(english);
        ledger = writeRow(g, font, where, px, pw, ledger, bodyLimit, DiaryChrome.INK_SUBTITLE);
        String when = cachedWhen != null && !cachedWhen.isEmpty()
            ? cachedWhen : deathWhen(english);
        ledger = writeRow(g, font, when, px, pw, ledger, bodyLimit, DiaryChrome.INK_FLAVOR);
        String courtesy = english ? "The port notes it in its ledger."
            : "La aduana lo anota en su libro.";
        ledger = writeRow(g, font, courtesy, px, pw, ledger, bodyLimit, DiaryChrome.INK_FLAVOR);

        // v0.360.0: la fecha se arma una vez, al abrirse la hoja.
        String dated = cachedDated != null && !cachedDated.isEmpty()
            ? cachedDated : buildDated(english);
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

    /** v0.360.0: la fecha del parte, en numeros romanos como el sello. */
    private static String buildDated(boolean english) {
        try {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            String[] romanMonths = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII" };
            return (english ? "dated " : "fechado ")
                + cal.get(java.util.Calendar.DAY_OF_MONTH) + " \u00b7 "
                + romanMonths[cal.get(java.util.Calendar.MONTH)] + " \u00b7 1897";
        } catch (Throwable ignored) {
            return "";
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
