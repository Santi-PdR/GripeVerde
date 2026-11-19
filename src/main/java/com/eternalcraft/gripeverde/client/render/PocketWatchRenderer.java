package com.eternalcraft.gripeverde.client.render;

import com.eternalcraft.gripeverde.client.audio.MenuAmbience;
import net.minecraft.client.Minecraft;
import com.eternalcraft.gripeverde.client.util.MotionPolicy;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.RandomSource;

/**
 * Reloj de bolsillo victoriano dibujado con primitivas.
 * Cuelga de una cadena larga desde el borde superior de la pantalla.
 * Oscila suavemente como pendulo.
 * Manecillas de hora y minuto fijas en 11:52 (la hora del brote).
 * Segundero animado con glitches ocasionales (el tiempo esta enfermo).
 * Brillo de cristal deslizante para sensacion de vidrio pulido.
 * Notifica a MenuAmbience cada vez que el segundero cambia de posicion.
 */
public class PocketWatchRenderer {

    private static final int COLOR_BRASS       = 0xFFB8862B;
    private static final int COLOR_BRASS_DARK  = 0xFF5A3E12;
    private static final int COLOR_FACE        = 0xFFEDE0C4;
    private static final int COLOR_MARK        = 0xFF3B2A16;
    private static final int COLOR_HAND        = 0xFF1A0F05;
    private static final int COLOR_SECOND      = 0xFF6B4A20;
    private static final int COLOR_TEXT        = 0xFF6B4A20;
    private static final int COLOR_CHAIN       = 0xFF8A6420;
    private static final int COLOR_CHAIN_DARK  = 0xFF3E2A10;

    private static final RandomSource RNG = RandomSource.create();
    private static final long START_TIME = System.currentTimeMillis();

    /**
     * v0.252.0: la hora de las agujas ahora es la hora de verdad.
     *
     * Antes el reloj sacaba una "hora del brote" de una lista y la cambiaba
     * cada cinco minutos: las agujas saltaban mientras uno las miraba y la
     * hora no servia para nada. Ahora marca la hora local del lector con la
     * misma aguja corta y el mismo minutero; la inscripcion de la tapa sigue
     * diciendo 11:52, que es cuando empezo todo, pero el reloj funciona.
     */
    /**
     * v0.360.0: el calendario se pide prestado, no se fabrica.
     *
     * Este reloj se dibuja en cada cuadro de la portada y armaba hasta cuatro
     * objetos Calendar (y tres arrays nuevos para la hora) por cuadro. Ahora se
     * lee uno solo, compartido, y se refresca cada 200 ms: la aguja de los
     * segundos no necesita mas resolucion que esa.
     */
    private static java.util.Calendar borrowed;

    private static java.util.Calendar clock() {
        long now = System.currentTimeMillis();
        if (borrowed == null || now - borrowedAt > 200L) {
            borrowed = java.util.Calendar.getInstance();
            borrowedAt = now;
        }
        return borrowed;
    }

    private static long borrowedAt = 0L;

    private static final int[] HOUR_MIN = new int[2];
    private static long hourMinuteStamp = Long.MIN_VALUE;

    private static int[] getCurrentHour() {
        long minute = System.currentTimeMillis() / 60_000L;
        if (minute != hourMinuteStamp) {
            hourMinuteStamp = minute;
            java.util.Calendar now = clock();
            HOUR_MIN[0] = now.get(java.util.Calendar.HOUR_OF_DAY) % 12;
            HOUR_MIN[1] = now.get(java.util.Calendar.MINUTE);
        }
        return HOUR_MIN;
    }

    // Estado del glitch actual
    private static long nextGlitchAt = 0L;
    private static int glitchType = 0;
    private static long glitchStartMs = 0L;
    private static boolean glitchActive = false;

    // Tracking del segundero para notificar cambios
    private static int lastSecondPos = -1;

    // ── v0.16.0: el reloj en la mano ────────────────────────────────────────
    private static boolean inspectionMode = false;
    private static long inspectionStartMs = 0L;
    private static final long SESSION_START = System.currentTimeMillis();

    public static boolean isInspecting() { return inspectionMode; }

    /** Abre o cierra la inspeccion del reloj. */
    public static void toggleInspection() {
        inspectionMode = !inspectionMode;
        inspectionStartMs = System.currentTimeMillis();
    }

    public static void closeInspection() { inspectionMode = false; }

    /** El tiempo de sesion enferma al segundero: glitches mas frecuentes. */
    private static long sessionMinutes() {
        return (System.currentTimeMillis() - SESSION_START) / 60_000L;
    }

    public static boolean isTimeSick() { return sessionMinutes() >= 8L; }

    /** Dibuja el reloj sostenido en la mano, con su inscripcion. */
    public static void drawInspection(GuiGraphics g, int w, int h, String version) {
        // Fondo: la vigilia se atenúa
        g.fill(0, 0, w, h, 0xB0060402);
        int cx = w / 2;
        int cy = Math.max(120, h / 2 - 30);
        int radius = Math.min(72, Math.min(w, h) / 3);
        // Cadena corta hacia arriba
        drawLongChain(g, cx, Math.max(0, cy - radius - 90), cx, cy - radius);
        fillCircle(g, cx, cy, radius, COLOR_BRASS);
        fillCircle(g, cx, cy, radius - 3, COLOR_BRASS_DARK);
        fillCircle(g, cx, cy, radius - 5, COLOR_BRASS);
        fillCircle(g, cx, cy, radius - 9, COLOR_FACE);
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 - 90);
            int outerR = radius - 12;
            int innerR = radius - 17;
            int x1 = (int)(cx + Math.cos(angle) * outerR);
            int y1 = (int)(cy + Math.sin(angle) * outerR);
            int x2 = (int)(cx + Math.cos(angle) * innerR);
            int y2 = (int)(cy + Math.sin(angle) * innerR);
            drawThickLine(g, x1, y1, x2, y2, COLOR_MARK);
        }
        // v0.288.0: las sesenta marcas de minuto, menudas, para que la esfera
        // no parezca vacia entre las horas.
        if (radius >= 54) {
            for (int m = 0; m < 60; m++) {
                if (m % 5 == 0) continue;
                double a = Math.toRadians(m * 6 - 90);
                int mx = (int) (cx + Math.cos(a) * (radius - 14));
                int my = (int) (cy + Math.sin(a) * (radius - 14));
                g.fill(mx, my, mx + 1, my + 1, 0xFF6E5A38);
            }
        }
        Font font = Minecraft.getInstance().font;
        int textR = radius - 26;
        drawRomanNumeral(g, font, cx, cy - textR, "XII");
        drawRomanNumeral(g, font, cx + textR, cy, "III");
        drawRomanNumeral(g, font, cx, cy + textR, "VI");
        drawRomanNumeral(g, font, cx - textR, cy, "IX");
        // Manecillas clavadas en la hora del brote
        int[] hour = getCurrentHour();
        double hourAngle = Math.toRadians(hour[0] * 30 + hour[1] * 0.5 - 90);
        double minAngle = Math.toRadians(hour[1] * 6 - 90);
        drawThickLine(g, cx, cy, (int)(cx + Math.cos(hourAngle) * (radius - 26)), (int)(cy + Math.sin(hourAngle) * (radius - 26)), COLOR_HAND);
        drawThickLine(g, cx, cy, (int)(cx + Math.cos(minAngle) * (radius - 17)), (int)(cy + Math.sin(minAngle) * (radius - 17)), COLOR_HAND);
        g.fill(cx - 2, cy - 2, cx + 2, cy + 2, COLOR_HAND);

        // La inscripcion grabada en la tapa
        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        long age = System.currentTimeMillis() - inspectionStartMs;
        int chars = MotionPolicy.isReduced() ? 40 : (int)Math.min(40, age / 60L);
        // v0.342.0: la inscripcion no puede llegar al aviso del pie ni a la
        // version. Se mide el alto util y cada renglon se calla si no cabe.
        int stackBottom = h - 34;
        String l1 = en ? "S.P. - 11:52" : "S.P. - 11:52";
        String l2 = en ? "may time be gentle with you" : "que el tiempo te sea leve";
        int a1 = Math.min(200, chars * 8);
        if (a1 > 8) {
            int w1 = font.width(l1);
            g.drawString(font, l1, cx - w1 / 2, cy + radius + 14, (a1 << 24) | 0xB89A6E, false);
        }
        if (chars > 14) {
            int shown = Math.min(l2.length(), chars - 14);
            String part = l2.substring(0, shown);
            int w2 = font.width(part);
            g.drawString(font, part, cx - w2 / 2, cy + radius + 28, (Math.min(160, (chars - 14) * 8) << 24) | 0x8E7A55, false);
        }
        // v0.248.0: la tapa cuenta las noches que lleva el lector, no fichas.
        if (chars > 26) {
            int nights;
            try {
                nights = com.eternalcraft.gripeverde.config.ClientConfig.OPEN_COUNT.get();
            } catch (Throwable ignored) {
                nights = 0;
            }
            String l3 = en
                ? (nights <= 1 ? "the first night" : nights + " nights on the avenue")
                : (nights <= 1 ? "la primera noche" : nights + " noches en la avenida");
            int w3 = font.width(l3);
            g.drawString(font, l3, cx - w3 / 2, cy + radius + 42,
                (Math.min(130, (chars - 26) * 6) << 24) | 0x6B5533, false);
        }
        // v0.250.0: la luna de esa noche, dicha con la palabra que le toca.
        if (chars > 34 && cy + radius + 56 + 9 <= stackBottom) {
            double phase = moonPhase(System.currentTimeMillis());
            String name = moonName(phase, en);
            String l4 = (en ? "moon: " : "luna: ") + name;
            int w4 = font.width(l4);
            g.drawString(font, l4, cx - w4 / 2, cy + radius + 56,
                (Math.min(120, (chars - 34) * 6) << 24) | 0x5C4A2E, false);
        }
        // v0.260.0: la fecha de hoy, en numeral romano como el cuadrante.
        if (chars > 38 && cy + radius + 68 + 9 <= stackBottom) {
            java.util.Calendar cal = clock();
            String roman = ROMAN_MONTHS[cal.get(java.util.Calendar.MONTH)];
            String today = (en ? "today: " : "hoy: ")
                + cal.get(java.util.Calendar.DAY_OF_MONTH) + " · " + roman;
            int wT = font.width(today);
            g.drawString(font, today, cx - wT / 2, cy + radius + 68,
                (Math.min(120, (chars - 38) * 6) << 24) | 0x51402A, false);
        }
        // v0.274.0: el segundero, la hora escrita, el dia y el atraso del reloj.
        long nowMs = System.currentTimeMillis();
        java.util.Calendar now = clock();
        double secAngle = Math.toRadians(now.get(java.util.Calendar.SECOND) * 6 - 90);
        drawThinLine(g, cx, cy,
            (int)(cx + Math.cos(secAngle) * (radius - 14)),
            (int)(cy + Math.sin(secAngle) * (radius - 14)), 0xFF7A1010);

        // v0.274.0: la muesca de las 11:52, la hora que nadie del barrio olvida.
        double outbreakAngle = Math.toRadians((11 * 30 + 52 * 0.5) - 90);
        int ox1 = (int)(cx + Math.cos(outbreakAngle) * (radius - 6));
        int oy1 = (int)(cy + Math.sin(outbreakAngle) * (radius - 6));
        int ox2 = (int)(cx + Math.cos(outbreakAngle) * (radius - 16));
        int oy2 = (int)(cy + Math.sin(outbreakAngle) * (radius - 16));
        drawThickLine(g, ox1, oy1, ox2, oy2, 0xFF8E2A20);

        // v0.274.0: a la derecha del bisel, lo que las agujas no dicen.
        int infoX = cx + radius + 18;
        int infoMaxW = w - infoX - 12;
        if (infoMaxW >= 70 && chars > 20) {
            int alpha = Math.min(190, (chars - 20) * 8);
            String clock = String.format(java.util.Locale.ROOT, "%02d:%02d",
                now.get(java.util.Calendar.HOUR_OF_DAY), now.get(java.util.Calendar.MINUTE));
            String[] daysEs = {"domingo","lunes","martes","miercoles","jueves","viernes","sabado"};
            String[] daysEn = {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
            String day = (en ? daysEn : daysEs)[Math.max(0, Math.min(6,
                now.get(java.util.Calendar.DAY_OF_WEEK) - 1))];
            int y0 = cy - 10;
            g.drawString(font, clock, infoX, y0, (alpha << 24) | 0xE8D9B4, false);
            g.drawString(font, day, infoX, y0 + 12, (Math.min(150, alpha) << 24) | 0xB89A6E, false);
            // v0.274.0: este reloj nunca anda del todo bien; lo dice sin drama.
            String drift = en ? "runs 3 min behind" : "atrasa 3 min";
            String cut = font.plainSubstrByWidth(drift, infoMaxW);
            g.drawString(font, cut, infoX, y0 + 24, (Math.min(130, alpha) << 24) | 0x8E7A55, false);
        }

        // v0.288.0: el relojero que lo firmo, bajo la inscripcion de la tapa.
        // v0.342.0: antes se leia casi negro sobre la ciudad. Ahora la tinta es
        // mas clara y, si el alto no da, el renglon no se escribe.
        if (chars > 30 && radius >= 50 && cy + radius + 91 + 9 <= stackBottom) {
            String maker = en ? "J. Vigne  ·  Montevideo" : "J. Vigne  ·  Montevideo";
            int mw = font.width(maker);
            g.drawString(font, maker, cx - mw / 2, cy + radius + 80,
                (Math.min(150, (chars - 30) * 6) << 24) | 0xB89A6E, false);
            String wind = en ? "wound: full" : "cuerda: llena";
            int ww2 = font.width(wind);
            g.drawString(font, wind, cx - ww2 / 2, cy + radius + 91,
                (Math.min(130, (chars - 30) * 6) << 24) | 0x8E7A55, false);
        }

        // La version del gabinete, al pie y en chico.
        if (version != null && !version.isBlank()) {
            String ver = "v" + version;
            g.drawString(font, ver, 10, h - 16, 0x50B89A6E, false);
        }

        String hint = en ? "~ click anywhere to let it swing again ~" : "~ pulsa en cualquier parte para que vuelva a oscilar ~";
        int wh2 = font.width(hint);
        g.drawString(font, hint, cx - wh2 / 2, h - 26, 0x60B89A6E, false);
    }

    public static void draw(GuiGraphics g, int cxAnchor, int cy, int topY, int radius, String version) {
        long now = System.currentTimeMillis();
        long elapsed = now - START_TIME;
        boolean reducedMotion = MotionPolicy.isReduced();

        double swingT = (elapsed % 5000L) / 5000.0 * 2.0 * Math.PI;
        int swingOffset = reducedMotion ? 0 : (int) Math.round(Math.sin(swingT) * 6.0);
        int cx = cxAnchor + swingOffset;

        drawLongChain(g, cxAnchor, topY, cx, cy - radius);

        fillCircle(g, cx, cy, radius, COLOR_BRASS);
        fillCircle(g, cx, cy, radius - 2, COLOR_BRASS_DARK);
        fillCircle(g, cx, cy, radius - 4, COLOR_BRASS);

        fillCircle(g, cx, cy, radius - 7, COLOR_FACE);
        // v0.261.0: la marca del taller, grabada en el bisel como en los relojes
        // de epoca: dos letras chiquitas a las once, siempre en el mismo sitio.
        if (radius >= 34) {
            Font markFont = Minecraft.getInstance().font;
            String mark = "S.P.";
            int mw = markFont.width(mark);
            g.drawString(markFont, mark, cx - radius / 2 - mw / 2, cy - radius / 2 - 3,
                COLOR_BRASS_DARK, false);
        }

        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 - 90);
            int outerR = radius - 9;
            int innerR = radius - 13;
            int x1 = (int)(cx + Math.cos(angle) * outerR);
            int y1 = (int)(cy + Math.sin(angle) * outerR);
            int x2 = (int)(cx + Math.cos(angle) * innerR);
            int y2 = (int)(cy + Math.sin(angle) * innerR);
            drawThickLine(g, x1, y1, x2, y2, COLOR_MARK);
        }

        Font font = Minecraft.getInstance().font;
        int textR = radius - 20;
        drawRomanNumeral(g, font, cx, cy - textR, "XII");
        // v0.257.0: a las tres va la ventanilla de la fecha, como en los relojes
        // de bolsillo que la llevaban: el numero del dia, de verdad.
        if (radius < 30) drawRomanNumeral(g, font, cx + textR, cy, "III");
        // v0.301.0: el VI vuelve a ser un numero. La ventanilla de luna parecia
        // un espejo pegado en la esfera y el lector la mando quitar.
        drawRomanNumeral(g, font, cx, cy + textR, "VI");
        drawRomanNumeral(g, font, cx - textR, cy, "IX");

        // v0.250.0: la ventanilla de la luna. No es adorno: la fase se calcula
        // con el calendario real -mes sinodico de 29,530588 dias desde la luna
        // nueva del 6 de enero de 2000-, asi que el reloj dice la luna de esa
        // noche, la mire quien la mire.
        if (radius >= 30) {
            int day = clock().get(java.util.Calendar.DAY_OF_MONTH);
            drawDateAperture(g, font, cx + (int) (radius * 0.52), cy, day);
        }

        if (!reducedMotion) drawGlassShine(g, cx, cy, radius - 7, elapsed);

        // Segundero animado con glitch + notificacion de audio
        int secondPos = computeSecondPosition(now);

        if (secondPos != lastSecondPos) {
            // Tick solo cuando cruza una marca horaria (cada 5 segundos)
            if (lastSecondPos != -1 && secondPos % 5 == 0) {
                MenuAmbience.onSecondTick();
            }
            lastSecondPos = secondPos;
        }

        double secAngle = Math.toRadians(secondPos * 6 - 90);
        int secLen = radius - 10;
        // v0.250.0: la punta del segundero lleva su punto, para leerla de lejos.
        int tipX = (int)(cx + Math.cos(secAngle) * (secLen - 2));
        int tipY = (int)(cy + Math.sin(secAngle) * (secLen - 2));
        g.fill(tipX - 1, tipY - 1, tipX + 1, tipY + 1, COLOR_SECOND);
        drawThinLine(g,
            cx, cy,
            (int)(cx + Math.cos(secAngle) * secLen),
            (int)(cy + Math.sin(secAngle) * secLen),
            COLOR_SECOND);

        int[] currentHour = getCurrentHour();
        int hh = currentHour[0];
        int mm = currentHour[1];
        double hourAngle = Math.toRadians(hh * 30 + mm * 0.5 - 90);
        double minAngle  = Math.toRadians(mm * 6 - 90);

        int hourLen = radius - 18;
        int minLen  = radius - 12;

        drawThickLine(g,
            cx, cy,
            (int)(cx + Math.cos(hourAngle) * hourLen),
            (int)(cy + Math.sin(hourAngle) * hourLen),
            COLOR_HAND);

        drawThickLine(g,
            cx, cy,
            (int)(cx + Math.cos(minAngle) * minLen),
            (int)(cy + Math.sin(minAngle) * minLen),
            COLOR_HAND);

        g.fill(cx - 2, cy - 2, cx + 2, cy + 2, COLOR_HAND);

        // v0.288.0: la misma muesca de las 11:52 que lleva la esfera grande.
        if (radius >= 36) {
            double oa = Math.toRadians((11 * 30 + 52 * 0.5) - 90);
            int x1 = (int) (cx + Math.cos(oa) * (radius - 5));
            int y1 = (int) (cy + Math.sin(oa) * (radius - 5));
            int x2 = (int) (cx + Math.cos(oa) * (radius - 11));
            int y2 = (int) (cy + Math.sin(oa) * (radius - 11));
            drawThickLine(g, x1, y1, x2, y2, 0xFF8E2A20);
        }

        // v0.288.0: fuera la leyenda de version bajo el bisel de la portada;
        // el cronista ya no rotula su reloj como un cartel de feria.

    }

    /**
     * v0.257.0: la ventanilla de la fecha, a las tres. Un rectangulo de laton
     * con su papel adentro y el numero del dia; si el dia trae dos cifras, el
     * vidrio crece un pixel a cada lado en vez de recortar el numero.
     */
    /** v0.260.0: los meses en numeral romano, como el cuadrante del reloj. */
    private static final String[] ROMAN_MONTHS = {
        "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"
    };

    private static void drawDateAperture(GuiGraphics g, Font font, int dx, int dy, int day) {
        String text = Integer.toString(day);
        int tw = font.width(text);
        int w = Math.max(11, tw + 4);
        int h = 9;
        int x0 = dx - w / 2;
        int y0 = dy - h / 2;
        g.fill(x0 - 1, y0 - 1, x0 + w + 1, y0 + h + 1, COLOR_BRASS_DARK);
        g.fill(x0, y0, x0 + w, y0 + h, 0xFFEFE2C6);
        g.drawString(font, text, x0 + (w - tw) / 2, y0 + 1, COLOR_HAND, false);
    }

    /** v0.250.0: luna nueva de referencia, 2000-01-06 18:14 UTC. */
    private static final long NEW_MOON_EPOCH_MS = 947182440000L;
    private static final double SYNODIC_DAYS = 29.530588853;

    /** v0.250.0: el nombre de la fase, en los dos idiomas del gabinete. */
    private static String moonName(double phase, boolean en) {
        if (phase < 0.03 || phase > 0.97) return en ? "new" : "nueva";
        if (phase < 0.47) return en ? "waxing" : "creciente";
        if (phase < 0.53) return en ? "full" : "llena";
        return en ? "waning" : "menguante";
    }

    /** v0.250.0: fase lunar real, 0 = nueva, 0.5 = llena. */
    public static double moonPhase(long nowMs) {
        double days = (nowMs - NEW_MOON_EPOCH_MS) / 86_400_000.0;
        double phase = (days / SYNODIC_DAYS) % 1.0;
        return phase < 0 ? phase + 1.0 : phase;
    }

    /**
     * v0.250.0: la ventanilla de la luna, con el terminador donde le toca.
     * Se dibuja fila por fila para que el borde quede redondo y barato.
     */
    private static void drawMoonAperture(GuiGraphics g, int mcxc, int mcy, int r, long now) {
        double phase = moonPhase(now);
        double ang = phase * 2.0 * Math.PI;
        int term = (int) Math.round(Math.cos(ang) * r);
        boolean waxing = phase < 0.5;
        int dark = 0xFF2A1E10;
        int lit = 0xFFE7D6AC;
        for (int dy = -r; dy <= r; dy++) {
            int half = (int) Math.floor(Math.sqrt(Math.max(0, r * r - dy * dy)));
            int y = mcy + dy;
            g.fill(mcxc - half, y, mcxc + half, y + 1, dark);
            int from;
            int to;
            if (waxing) {
                from = Math.max(-half, term);
                to = half;
            } else {
                from = -half;
                to = Math.min(half, -term);
            }
            if (to > from) g.fill(mcxc + from, y, mcxc + to, y + 1, lit);
        }
        // Aro de laton de la ventanilla.
        for (int dy = -r - 1; dy <= r + 1; dy++) {
            int half = (int) Math.floor(Math.sqrt(Math.max(0, (r + 1) * (r + 1) - dy * dy)));
            int y = mcy + dy;
            g.fill(mcxc - half, y, mcxc - half + 1, y + 1, COLOR_BRASS_DARK);
            g.fill(mcxc + half - 1, y, mcxc + half, y + 1, COLOR_BRASS_DARK);
        }
        g.fill(mcxc - r - 1, mcy - r - 1, mcxc + r + 2, mcy - r, COLOR_BRASS_DARK);
        g.fill(mcxc - r - 1, mcy + r + 1, mcxc + r + 2, mcy + r + 2, COLOR_BRASS_DARK);
    }

    private static int computeSecondPosition(long now) {
        int baseSec = (int)((now / 1000L) % 60L);
        if (MotionPolicy.isReduced()) {
            glitchActive = false;
            return baseSec;
        }

        // v0.16.0: el tiempo enferma con la sesion: glitches cada vez mas cerca.
        long minGap = Math.max(2800L, 9000L - sessionMinutes() * 600L);
        long maxGap = minGap + 4000L;

        if (nextGlitchAt == 0L) {
            nextGlitchAt = now + minGap + RNG.nextInt((int)(maxGap - minGap));
        }

        if (!glitchActive && now >= nextGlitchAt) {
            glitchActive = true;
            glitchStartMs = now;
            glitchType = RNG.nextInt(3);
        }

        if (glitchActive) {
            long dt = now - glitchStartMs;
            switch (glitchType) {
                case 0:
                    if (dt < 800L) {
                        return (int)((glitchStartMs / 1000L) % 60L);
                    }
                    break;
                case 1:
                    if (dt < 200L) {
                        return (baseSec + 3) % 60;
                    }
                    break;
                case 2:
                    if (dt < 800L) {
                        return (baseSec + 58) % 60;
                    }
                    break;
            }
            if (dt > 900L) {
                glitchActive = false;
                nextGlitchAt = now + minGap + RNG.nextInt((int)(maxGap - minGap));
            }
        }

        return baseSec;
    }

    private static void drawGlassShine(GuiGraphics g, int cx, int cy, int faceRadius, long elapsed) {
        // Optimized: diagonal reflective band via 5 translucent strips instead of per-pixel scan
        float t = (elapsed % 8000L) / 8000f;
        // Band position from -1 to 1 diagonally
        float bandCenter = t * 2.4f - 1.2f;
        if (bandCenter < -1.1f || bandCenter > 1.1f) return; // off-screen, skip

        int r = faceRadius;
        // Convert bandCenter (-1..1) to offset in pixels along diagonal
        int offset = (int)(bandCenter * r * 1.2f);
        // Draw 5 parallel thin strips with fading alpha to simulate soft reflection
        // Strip direction: diagonal top-left to bottom-right, thickness 2px each
        int baseAlpha = 70;
        for (int strip = -2; strip <= 2; strip++) {
            int stripOffset = offset + strip * 3;
            int alpha = baseAlpha - Math.abs(strip) * 14;
            if (alpha <= 6) continue;
            int color = (alpha << 24) | 0xFFF5DC;
            // For each y in circle, x = y + stripOffset (diagonal)
            for (int dy = -r; dy <= r; dy++) {
                int dxMax = (int)Math.sqrt(Math.max(0, r * r - dy * dy));
                int xLine = dy + stripOffset; // diagonal: x proportional to y
                if (xLine >= -dxMax && xLine <= dxMax) {
                    // Draw 2px thick along X
                    g.fill(cx + xLine, cy + dy, cx + xLine + 2, cy + dy + 1, color);
                }
            }
        }
    }

    private static void drawLongChain(GuiGraphics g, int xTop, int yTop, int xBottom, int yBottom) {
        int totalLen = yBottom - yTop;
        if (totalLen < 8) return;

        int linkCount = Math.max(3, totalLen / 4);

        for (int i = 0; i < linkCount; i++) {
            float t = i / (float)(linkCount - 1);
            int x = (int) (xTop + (xBottom - xTop) * t);
            int y = yTop + (int)(t * totalLen);

            if (i % 2 == 0) {
                g.fill(x - 1, y, x + 2, y + 3, COLOR_CHAIN);
                g.fill(x - 1, y + 1, x, y + 2, COLOR_CHAIN_DARK);
            } else {
                g.fill(x - 2, y + 1, x + 3, y + 2, COLOR_CHAIN);
                g.fill(x - 1, y + 1, x + 2, y + 2, COLOR_CHAIN_DARK);
            }
        }

        g.fill(xBottom - 3, yBottom - 3, xBottom + 3, yBottom, COLOR_BRASS_DARK);
        g.fill(xBottom - 2, yBottom - 4, xBottom + 2, yBottom - 3, COLOR_BRASS);
    }

    private static void drawRomanNumeral(GuiGraphics g, Font font, int cx, int cy, String txt) {
        int w = font.width(txt);
        g.drawString(font, txt, cx - w / 2, cy - 4, COLOR_MARK, false);
    }

    private static void fillCircle(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt(r * r - dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    private static void drawThickLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int x = x1, y = y1;
        while (true) {
            g.fill(x, y, x + 2, y + 2, color);
            if (x == x2 && y == y2) break;
            int e2 = err * 2;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 <  dx) { err += dx; y += sy; }
        }
    }

    private static void drawThinLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int x = x1, y = y1;
        while (true) {
            g.fill(x, y, x + 1, y + 1, color);
            if (x == x2 && y == y2) break;
            int e2 = err * 2;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 <  dx) { err += dx; y += sy; }
        }
    }

    /** Devuelve true si la hora actual del reloj esta cerca de las 12 (11:52-12:08).
     *  Usado por LivingAtmosphere/AtmosphereRenderer para efecto vela. */
    public static boolean isNearNoon() {
        // v0.252.0: ahora depende de la hora de verdad, no de una lista.
        int[] hour = getCurrentHour();
        int hh = hour[0];
        int mm = hour[1];
        // Convertir a minutos totales
        int totalMin = (hh % 12) * 60 + mm;
        int distanceToNoon = Math.min(totalMin, 720 - totalMin);
        // Cerca de las 12 = <=10 minutos de diferencia (real time o high noon)
        return distanceToNoon <= 10;
    }
}
