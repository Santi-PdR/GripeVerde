package com.eternalcraft.gripeverde.client.compat;

import com.eternalcraft.gripeverde.client.render.PaperTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * v0.367.0: compatibilidad con JourneyMap 5.10.3.
 *
 * El mapa lo dibuja el mod de JourneyMap y su API publica no expone el minimapa
 * ni su parte de coordenadas, asi que aqui se trabaja por dentro con su propia
 * libreria, sin tocar su JAR:
 *
 *  - El canto del minimapa circular pasa a ser una hoja de la casa: se dibuja
 *    un anillo de papel desgarrado por fuera del radio del mapa, de modo que
 *    nunca tapa el terreno ni la aguja.
 *  - La letra de la informacion (coordenadas, bioma, hora) se viste con el
 *    sistema de temas que el propio JourneyMap trae: se copia el tema en curso
 *    y se le pone fondo de papel y tinta en vez del negro con letra blanca.
 *    Se hace en memoria, sin escribir nada en la configuracion del jugador.
 *
 * Todo va envuelto en try/catch y, si JourneyMap no esta, no hace nada.
 */
public final class JourneyMapPaper {

    private static final int PAPER = 0xE6E8DCB4;
    private static final int PAPER_EDGE = 0xB8D7C79B;
    private static final int INK_HAIR = 0x552A1C12;

    private static boolean probed;
    private static boolean present;

    private static Object uiManager;
    private static java.lang.reflect.Method getMiniMap;
    private static java.lang.reflect.Method isMiniMapEnabled;
    private static java.lang.reflect.Method getDisplayVars;

    private static boolean themeTried;
    private static long themeRetryAtMs;

    private JourneyMapPaper() {
    }

    /** Una sola vez: se busca el mod y se guardan los metodos. */
    private static void probe() {
        if (probed) return;
        probed = true;
        try {
            Class<?> miniMapClass = Class.forName("journeymap.client.ui.minimap.MiniMap");
            Class<?> uiManagerClass = Class.forName("journeymap.client.ui.UIManager");
            getDisplayVars = miniMapClass.getMethod("getDisplayVars");
            getMiniMap = uiManagerClass.getMethod("getMiniMap");
            isMiniMapEnabled = uiManagerClass.getMethod("isMiniMapEnabled");
            uiManager = uiManagerClass.getField("INSTANCE").get(null);
            present = uiManager != null;
        } catch (Throwable ignored) {
            present = false;
        }
    }

    /** Enganche desde la capa de interfaz del mod, arriba de todo. */
    public static void render(GuiGraphics g) {
        try {
            probe();
            if (!present || g == null) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || mc.screen != null) return;
            if (mc.options != null && mc.options.hideGui) return;
            if (!Boolean.TRUE.equals(isMiniMapEnabled.invoke(uiManager))) return;

            patchTheme();

            Object miniMap = getMiniMap.invoke(uiManager);
            if (miniMap == null) return;
            Object dv = getDisplayVars.invoke(miniMap);
            if (dv == null) return;

            drawPaperEdge(g, dv);
        } catch (Throwable ignored) {
        }
    }

    // ── el canto de hoja ────────────────────────────────────────────────────

    private static void drawPaperEdge(GuiGraphics g, Object dv) {
        String shape = String.valueOf(read(dv, "shape"));
        if (shape == null) shape = "";
        shape = shape.toLowerCase(java.util.Locale.ROOT);
        int width = intOf(dv, "minimapWidth");
        int height = intOf(dv, "minimapHeight");
        if (width < 24 || height < 24) return;

        if (shape.contains("circle") && dv != null) {
            double cx;
            double cy;
            Object center = read(dv, "centerPoint");
            if (center instanceof java.awt.geom.Point2D p) {
                cx = p.getX();
                cy = p.getY();
            } else {
                cx = intOf(dv, "translateX") + width / 2.0;
                cy = intOf(dv, "translateY") + height / 2.0;
            }
            int r = Math.max(12, Math.min(width, height) / 2);
            drawRing(g, (int) Math.round(cx), (int) Math.round(cy), r);
            return;
        }
        // Cuadrado: el canto va por fuera del marco, sin tocar el mapa.
        int x = intOf(dv, "translateX") - 2;
        int y = intOf(dv, "translateY") - 2;
        int w = width + 4;
        int h = height + 4;
        if (w < 24 || h < 24) return;
        PaperTexture.drawWornBorder(g, x, y, w, h, 0x66E8DCB4, x * 31 + y * 17);
        g.fill(x, y - 1, x + w, y, INK_HAIR);
        g.fill(x, y + h, x + w, y + h + 1, INK_HAIR);
        g.fill(x - 1, y, x, y + h, INK_HAIR);
        g.fill(x + w, y, x + w + 1, y + h, INK_HAIR);
    }

    /** Anillo de papel desgarrado alrededor del minimapa circular. */
    private static void drawRing(GuiGraphics g, int cx, int cy, int r) {
        int steps = 180;
        for (int i = 0; i < steps; i++) {
            double a = (Math.PI * 2.0) * i / steps;
            double cos = Math.cos(a);
            double sin = Math.sin(a);
            // Canto irregular, siempre igual para el mismo angulo.
            int bite = (i * 1103515245 + 12345) >> 28 & 1;
            int rr = r + 2 + bite;
            int x = (int) Math.round(cx + cos * rr);
            int y = (int) Math.round(cy + sin * rr);
            g.fill(x - 1, y - 1, x + 2, y + 2, PAPER);
            int xf = (int) Math.round(cx + cos * (rr + 2));
            int yf = (int) Math.round(cy + sin * (rr + 2));
            g.fill(xf, yf, xf + 1, yf + 1, PAPER_EDGE);
        }
        // Filete de tinta pegado al mapa, para asentar la hoja sobre la imagen.
        for (int i = 0; i < steps; i += 2) {
            double a = (Math.PI * 2.0) * i / steps;
            int x = (int) Math.round(cx + Math.cos(a) * (r + 1));
            int y = (int) Math.round(cy + Math.sin(a) * (r + 1));
            g.fill(x, y, x + 1, y + 1, INK_HAIR);
        }
    }

    // ── la letra de la informacion ──────────────────────────────────────────

    /**
     * Copia el tema en curso del mapa y le cambia los colores de la letra y su
     * fondo por papel y tinta. Solo en memoria: la configuracion del jugador no
     * se toca, y al cerrar el juego vuelve a como estaba.
     */
    private static void patchTheme() {
        if (themeTried) return;
        long now = System.currentTimeMillis();
        if (now < themeRetryAtMs) return;
        themeRetryAtMs = now + 5000L;   // si aun no hay tema, se reintenta luego
        try {
            Class<?> loader = Class.forName("journeymap.client.io.ThemeLoader");
            Object theme = loader.getMethod("getCurrentTheme").invoke(null);
            if (theme == null) return;
            Class<?> colorSpec = Class.forName("journeymap.client.ui.theme.Theme$ColorSpec");
            var ctor = colorSpec.getConstructor(String.class, float.class);
            Object paperBg = ctor.newInstance("#E3D5AB", 0.88f);
            Object inkFg = ctor.newInstance("#2A1C12", 1.0f);
            Object inkHi = ctor.newInstance("#5C3A16", 1.0f);
            Object minimap = read(theme, "minimap");
            if (minimap == null) return;
            for (String shape : new String[] {"circle", "square"}) {
                Object spec = read(minimap, shape);
                if (spec == null) continue;
                for (String label : new String[] {"labelTop", "labelBottom", "compassLabel"}) {
                    Object labelSpec = read(spec, label);
                    if (labelSpec == null) continue;
                    write(labelSpec, "background", paperBg);
                    write(labelSpec, "foreground", inkFg);
                    write(labelSpec, "highlight", inkHi);
                    write(labelSpec, "shadow", Boolean.FALSE);
                }
                Object frame = read(spec, "frame");
                if (frame != null) write(frame, "color", "#2A1C12");
            }
            Class<?> themeClass = Class.forName("journeymap.client.ui.theme.Theme");
            loader.getMethod("setCurrentTheme", themeClass).invoke(null, theme);
            themeTried = true;
        } catch (Throwable ignored) {
        }
    }

    // ── plomeria de reflexion, sin nombres ofuscados ────────────────────────

    private static Object read(Object target, String name) {
        if (target == null) return null;
        for (Class<?> c = target.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                java.lang.reflect.Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(target);
            } catch (NoSuchFieldException ignored) {
            } catch (Throwable ignored) {
                return null;
            }
        }
        return null;
    }

    private static void write(Object target, String name, Object value) {
        if (target == null) return;
        for (Class<?> c = target.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                java.lang.reflect.Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
            } catch (Throwable ignored) {
                return;
            }
        }
    }

    private static int intOf(Object target, String name) {
        Object v = read(target, name);
        return v instanceof Number n ? n.intValue() : 0;
    }
}
