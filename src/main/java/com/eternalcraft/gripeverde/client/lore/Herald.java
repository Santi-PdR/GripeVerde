package com.eternalcraft.gripeverde.client.lore;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.time.LocalDate;
import java.util.Random;

/**
 * El Heraldo de la Cuarentena.
 *
 * Cada dia real aparece un periodico nuevo en el kiosco de la calle. Sus
 * titulares se componen con piezas sueltas, sembradas con el dia: nunca dos
 * amaneceres iguales. Leerlo lo marca como leido hasta el alba siguiente.
 *
 * v0.16.0
 */
public final class Herald {

    private Herald() {}

    private static long viewStartMs = 0L;
    private static long viewUntilMs = 0L;

    public static boolean isViewing() {
        return viewUntilMs > System.currentTimeMillis();
    }

    /** Pliega el periodico antes de tiempo. */
    public static void close() {
        viewUntilMs = 0L;
    }

    /** Abre el Heraldo del dia (llamado desde el click en el kiosco). */
    public static void openToday() {
        long today = LocalDate.now().toEpochDay();
        boolean firstRead = ChroniclerMemory.counter("herald_day") != today;
        ChroniclerMemory.setCounter("herald_day", (int)today);
        viewStartMs = System.currentTimeMillis();
        viewUntilMs = viewStartMs + 9000L;
        if (firstRead) {
            Discoveries.discover(Discoveries.HERALD);
        }
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                com.eternalcraft.gripeverde.client.audio.ModSounds.UI_SCROLL.get(), 0.18f, 0.72f);
        } catch (Throwable ignored) {}
    }

    private static final String[] DISTRICTS_ES = {
        "Whitechapel", "el barrio del rio", "la parroquia de San Judas",
        "el distrito de los faroles", "la calle del Reloj Viejo", "los muelles"
    };
    private static final String[] EVENTS_ES = {
        "amanecio sin niebla por primera vez en semanas",
        "contaron trece campanadas y nadie las pidio",
        "un medico cruzo la linea con la maleta vacia",
        "las ratas abandonaron tres casas a la vez",
        "se apago un farol que nadie habia tocado",
        "un telegrama llego sin remitente ni destino",
        "el gabinete del distrito fue vaciado de papeles viejos",
        "un farol del tejado parpadeo tres veces y se apago solo",
        "el gato del tejado bajo al kiosco y no robo nada"
    };
    private static final String[] SECOND_ES = {
        "El precio de las velas volvio a subir. El campanario lo niega.",
        "Se dice que el reloj de bolsillo marco las once y cincuenta y dos tres veces.",
        "Nadie firma los edictos. Todos reconocen la letra.",
        "Un papel cayo del cielo y nadie lo recogio."
    };
    private static final String[] EDICTS_ES = {
        "La Junta Sanitaria recuerda: ninguna luz despues del crepusculo.",
        "Se prohibe nombrar a los ausentes en voz alta.",
        "El registro de enfermos cierra hoy antes de tiempo.",
        "Nadie entra. Nadie sale. Nadie pregunta.",
        "Se ruega no alimentar a los cuervos del campanario."
    };
    private static final String[] DISTRICTS_EN = {
        "Whitechapel", "the river district", "St. Jude's parish",
        "the lantern district", "Old Clock Street", "the docks"
    };
    private static final String[] EVENTS_EN = {
        "woke without fog for the first time in weeks",
        "counted thirteen bell strikes that no one asked for",
        "a doctor crossed the line with an empty bag",
        "the rats left three houses at once",
        "a lamp went out that no one had touched",
        "a telegram arrived with no sender and no address",
        "the district cabinet was emptied of old papers",
        "a roof lantern flickered three times and went out on its own",
        "the roof cat came down to the kiosk and stole nothing"
    };
    private static final String[] SECOND_EN = {
        "The price of candles rose again. The bell tower denies it.",
        "The pocket watch is said to have struck eleven fifty-two three times.",
        "No one signs the edicts. Everyone recognises the hand.",
        "A paper fell from the sky and no one picked it up."
    };
    private static final String[] EDICTS_EN = {
        "The Sanitary Board reminds: no lights after dusk.",
        "Naming the missing aloud is hereby forbidden.",
        "The registry of the sick closes early today.",
        "No one in. No one out. No one asks.",
        "Please do not feed the crows of the bell tower."
    };

    /** v0.21.9: titular del dia (misma semilla que el periodico del kiosco). */
    public static String headlineToday() {
        boolean en = Discoveries.isEnglish();
        long today = LocalDate.now().toEpochDay();
        Random rnd = new Random(today * 7919L + (en ? 1L : 0L));
        String[] districts = en ? DISTRICTS_EN : DISTRICTS_ES;
        String[] events = en ? EVENTS_EN : EVENTS_ES;
        return (en ? "IN " : "EN ")
            + districts[rnd.nextInt(districts.length)].toUpperCase()
            + ": " + events[rnd.nextInt(events.length)];
    }

    public static void draw(GuiGraphics g, int w, int h) {
        if (!isViewing()) return;
        long now = System.currentTimeMillis();
        long age = now - viewStartMs;
        long remain = viewUntilMs - now;
        int alpha = 240;
        if (age < 380L) alpha = (int)(240 * age / 380L);
        else if (remain < 800L) alpha = (int)(240 * remain / 800L);
        alpha = Math.max(0, Math.min(240, alpha));

        boolean en = Discoveries.isEnglish();
        long today = LocalDate.now().toEpochDay();
        Random rnd = new Random(today * 7919L + (en ? 1L : 0L));
        String[] edicts = en ? EDICTS_EN : EDICTS_ES;
        String headline = headlineToday();
        String edict = edicts[(int)(Math.abs(today) % edicts.length)];
        String[] seconds = en ? SECOND_EN : SECOND_ES;
        String second = seconds[rnd.nextInt(seconds.length)];
        String name = en ? "THE QUARANTINE HERALD" : "EL HERALDO DE LA CUARENTENA";
        String motto = en ? "printed under seal - price: one breath" : "impreso bajo sello - precio: un aliento";

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) return;

        int bw = Math.min(330, w - 70);
        int bh = 118;
        int x = (w - bw) / 2;
        int y = Math.max(50, h / 2 - bh - 24);

        int paper = (alpha << 24) | 0xE6DCC0;
        int ink = (alpha << 24) | 0x241608;
        int bronze = (Math.min(200, alpha) << 24) | 0x8E6A35;
        int red = (Math.min(210, alpha) << 24) | 0x6E1F16;

        g.fill(x + 3, y + 4, x + bw + 3, y + bh + 4, (Math.min(70, alpha / 3) << 24) | 0x000000);
        g.fill(x, y, x + bw, y + bh, paper);
        g.fill(x, y, x + bw, y + 1, ink);
        g.fill(x, y + bh - 1, x + bw, y + bh, ink);
        g.fill(x, y, x + 1, y + bh, ink);
        g.fill(x + bw - 1, y, x + bw, y + bh, ink);
        g.fill(x + 2, y + 2, x + bw - 2, y + 3, bronze);

        int cx = x + bw / 2;
        DiaryChrome.drawCenteredString(g, mc.font, name, cx, y + 10, ink);
        DiaryChrome.drawCenteredString(g, mc.font, motto, cx, y + 24, bronze);
        g.fill(x + 10, y + 36, x + bw - 10, y + 37, (Math.min(140, alpha) << 24) | 0x422810);

        // Titular con tipewriter
        int chars = (int)Math.min(headline.length(), Math.max(0, (now - viewStartMs - 200L) / 26L));
        String shown = headline.substring(0, Math.max(0, chars));
        java.util.List<net.minecraft.util.FormattedCharSequence> lines =
            mc.font.split(net.minecraft.network.chat.Component.literal(shown), bw - 30);
        int ty = y + 46;
        for (int i = 0; i < Math.min(2, lines.size()); i++) {
            int lw = mc.font.width(lines.get(i));
            g.drawString(mc.font, lines.get(i), cx - lw / 2, ty, red, false);
            ty += 11;
        }
        DiaryChrome.drawCenteredString(g, mc.font, second, cx, y + 74, ink);
        g.fill(x + 10, y + 90, x + bw - 10, y + 91, (Math.min(110, alpha) << 24) | 0x422810);
        DiaryChrome.drawCenteredString(g, mc.font, edict, cx, y + 98, bronze);
    }
}
