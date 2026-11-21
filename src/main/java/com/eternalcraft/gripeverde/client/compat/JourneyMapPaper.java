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

    private static final int PAPER = 0xF2E8DCB4;
    private static final int PAPER_EDGE = 0xB8D7C79B;
    private static final int INK_HAIR = 0x552A1C12;

    /**
     * v0.378.0: una sola linea de diario la primera vez que se sabe algo. Si el
     * tema no se aplica en la partida del jugador, con esto se ve el motivo sin
     * tener que adivinar.
     */
    private static void note(String why) {
        // v0.390.0: antes solo se anotaba la PRIMERA linea, y esa solia ser
        // "el minimapa aun no tiene medidas" -que pasa en el primer cuadro-.
        // Las lineas utiles -tema aplicado, forma, anillo- quedaban sin escribir
        // y desde el diario no habia como saber que fallaba. Ahora se anota cada
        // mensaje distinto una vez, con un tope para no llenar el archivo.
        if (why == null || noted.contains(why) || noted.size() >= 12) return;
        noted.add(why);
        try {
            org.apache.logging.log4j.LogManager.getLogger("gripeverde/journeymap")
                .info("[GripeVerde] JourneyMap: {}", why);
        } catch (Throwable ignored) {
        }
    }

    private static final java.util.Set<String> noted = new java.util.HashSet<>();

    private static boolean probed;
    private static boolean present;

    private static Object uiManager;
    private static java.lang.reflect.Method getMiniMap;
    private static java.lang.reflect.Method isMiniMapEnabled;
    private static java.lang.reflect.Method getDisplayVars;

    private static boolean themeTried;
    private static Object patchedTheme;
    private static int frameCounter;
    private static long themeRetryAtMs;

    private JourneyMapPaper() {
    }

    /**
     * v0.369.0: sonda tolerante. Antes, si UNA sola busqueda fallaba -por
     * ejemplo un metodo que no es publico-, se daba el mod por ausente y no se
     * dibujaba nada: el minimapa seguia con su marco azul y su letra blanca.
     * Ahora cada miembro se busca por separado y, si no es publico, se abre con
     * setAccessible.
     */
    private static void probe() {
        if (probed) return;
        probed = true;
        try {
            Class<?> miniMapClass = Class.forName("journeymap.client.ui.minimap.MiniMap");
            Class<?> uiManagerClass = Class.forName("journeymap.client.ui.UIManager");
            getDisplayVars = methodOf(miniMapClass, "getDisplayVars");
            getMiniMap = methodOf(uiManagerClass, "getMiniMap");
            isMiniMapEnabled = methodOf(uiManagerClass, "isMiniMapEnabled");
            try {
                // reflexion-ok: `INSTANCE` es campo de JourneyMap, no del juego;
                // sus nombres no cambian entre versiones.
                java.lang.reflect.Field inst = uiManagerClass.getDeclaredField("INSTANCE");
                inst.setAccessible(true);
                uiManager = inst.get(null);
            } catch (Throwable ignored) {
                uiManager = null;
            }
            present = uiManager != null && getMiniMap != null && getDisplayVars != null;
            if (!present) note("no se pudo enganchar al minimapa");
        } catch (Throwable ignored) {
            present = false;
            note("JourneyMap no esta");
        }
    }

    private static java.lang.reflect.Method methodOf(Class<?> type, String name) {
        try {
            java.lang.reflect.Method m = type.getMethod(name);
            m.setAccessible(true);
            return m;
        } catch (Throwable ignored) {
        }
        try {
            java.lang.reflect.Method m = type.getDeclaredMethod(name);
            m.setAccessible(true);
            return m;
        } catch (Throwable ignored) {
            return null;
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
            if (isMiniMapEnabled != null) {
                Object on = isMiniMapEnabled.invoke(uiManager);
                if (Boolean.FALSE.equals(on)) return;
            }

            // Cada segundo se comprueba que JourneyMap siga con el tema de la
            // casa: al entrar y salir de mundos lo recarga y se perdia.
            if (themeTried && patchedTheme != null && (frameCounter++ & 0x3F) == 0) {
                try {
                    Object now = Class.forName("journeymap.client.io.ThemeLoader")
                        .getMethod("getCurrentTheme").invoke(null);
                    if (now != patchedTheme) themeTried = false;
                } catch (Throwable ignored) {
                }
            }
            patchTheme();

            Object miniMap = getMiniMap.invoke(uiManager);
            if (miniMap == null) return;
            Object dv = getDisplayVars.invoke(miniMap);
            if (dv == null) {
                note("el minimapa aun no tiene medidas");
                return;
            }

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
        if (width < 24 || height < 24) {
            note("sin tamano de minimapa: " + width + "x" + height);
            return;
        }
        note("forma=" + shape);

        if (shape.contains("circle") && dv != null) {
            // v0.378.0: centerPoint NO esta en pixeles de pantalla -es el centro
            // del mapa en su propio espacio-, asi que el anillo se dibujaba
            // fuera de sitio y no se veia nunca. La posicion buena es la que da
            // el propio JourneyMap para trasladar el mapa: translateX/translateY.
            double cx;
            double cy;
            int tx = intOf(dv, "translateX");
            int ty = intOf(dv, "translateY");
            if (tx > 0 || ty > 0) {
                cx = tx + width / 2.0;
                cy = ty + height / 2.0;
            } else {
                Object center = read(dv, "centerPoint");
                if (center instanceof java.awt.geom.Point2D p) {
                    cx = p.getX();
                    cy = p.getY();
                } else {
                    cx = width / 2.0;
                    cy = height / 2.0;
                }
            }
            note("anillo r=" + Math.max(12, Math.max(width, height) / 2)
                + " en " + (int) cx + "," + (int) cy + " (" + width + "x" + height + ")");
            int r = Math.max(12, Math.max(width, height) / 2);
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

    /**
     * v0.367.0: anillo de papel desgarrado alrededor del minimapa circular.
     * v0.368.0: la geometria se calcula una sola vez por tamano y posicion, y
     * se dibuja en dos tandas (papel y filete) con un solo envio a la tarjeta.
     * Antes eran mas de cuatrocientas llamadas de relleno y otros tantos senos
     * y cosenos por cuadro; ahora, cero cuentas y dos envios.
     */
    private static void drawRing(GuiGraphics g, int cx, int cy, int r) {
        buildRing(cx, cy, r);
        if (ringPaper == null || ringInk == null) {
            fillRingFallback(g, cx, cy, r);
            return;
        }
        if (!drawQuads(g, ringPaper, ringPaperQuads, PAPER)
                || !drawQuads(g, ringInk, ringInkQuads, INK_HAIR)) {
            fillRingFallback(g, cx, cy, r);
        }
        // v0.376.0: cuatro marcas de brujula en los diagonales del canto, como
        // las de un marco de lente; son cuatro rellenos y no cuestan nada.
        int tab = Math.max(3, r / 12);
        for (int k = 0; k < 4; k++) {
            double a = Math.PI / 4.0 + k * (Math.PI / 2.0);
            int x = (int) Math.round(cx + Math.cos(a) * (r + 2));
            int y = (int) Math.round(cy + Math.sin(a) * (r + 2));
            g.fill(x - tab / 2, y - tab / 2, x + tab / 2 + 1, y + tab / 2 + 1, INK_HAIR);
        }
    }

    private static void fillRingFallback(GuiGraphics g, int cx, int cy, int r) {
        int steps = 160;
        for (int i = 0; i < steps; i++) {
            double a = (Math.PI * 2.0) * i / steps;
            int bite = (i * 1103515245 + 12345) >> 28 & 1;
            int rr = r + 2 + bite;
            int x = (int) Math.round(cx + Math.cos(a) * rr);
            int y = (int) Math.round(cy + Math.sin(a) * rr);
            g.fill(x - 1, y - 1, x + 2, y + 2, PAPER);
            int xf = (int) Math.round(cx + Math.cos(a) * (rr + 2));
            int yf = (int) Math.round(cy + Math.sin(a) * (rr + 2));
            g.fill(xf, yf, xf + 1, yf + 1, PAPER_EDGE);
        }
        for (int i = 0; i < steps; i += 2) {
            double a = (Math.PI * 2.0) * i / steps;
            int x = (int) Math.round(cx + Math.cos(a) * (r + 1));
            int y = (int) Math.round(cy + Math.sin(a) * (r + 1));
            g.fill(x, y, x + 1, y + 1, INK_HAIR);
        }
    }

    private static final int RING_STEPS = 160;
    private static float[] ringPaper;
    private static float[] ringInk;
    private static int ringPaperQuads;
    private static int ringInkQuads;
    private static int geomCx = Integer.MIN_VALUE;
    private static int geomCy;
    private static int geomR = -1;

    private static void buildRing(int cx, int cy, int r) {
        if (ringPaper != null && ringInk != null && cx == geomCx && cy == geomCy && r == geomR) {
            return;
        }
        geomCx = cx;
        geomCy = cy;
        geomR = r;
        float[] paper = new float[RING_STEPS * 4];
        int pn = 0;
        for (int i = 0; i < RING_STEPS; i++) {
            double a = (Math.PI * 2.0) * i / RING_STEPS;
            int bite = (i * 1103515245 + 12345) >> 28 & 1;
            // Banda de papel que pisa el marco del mapa y sobresale un poco,
            // con mordiscos para que el canto se lea desgarrado.
            double rin = r - 1;
            double rout = r + 4 + bite;
            int xi = (int) Math.round(cx + Math.cos(a) * rin);
            int yi = (int) Math.round(cy + Math.sin(a) * rin);
            int xo = (int) Math.round(cx + Math.cos(a) * rout);
            int yo = (int) Math.round(cy + Math.sin(a) * rout);
            paper[pn * 4] = Math.min(xi, xo) - 1;
            paper[pn * 4 + 1] = Math.min(yi, yo) - 1;
            paper[pn * 4 + 2] = Math.max(xi, xo) + 1;
            paper[pn * 4 + 3] = Math.max(yi, yo) + 1;
            pn++;
        }
        float[] ink = new float[(RING_STEPS / 2 + 1) * 4];
        int inn = 0;
        for (int i = 0; i < RING_STEPS; i += 2) {
            double a = (Math.PI * 2.0) * i / RING_STEPS;
            // v0.370.0: la raya iba por dentro de la banda de papel y no se
            // veia; ahora va por fuera, como el canto de una hoja pegada.
            int x = (int) Math.round(cx + Math.cos(a) * (r + 6));
            int y = (int) Math.round(cy + Math.sin(a) * (r + 6));
            ink[inn * 4] = x;
            ink[inn * 4 + 1] = y;
            ink[inn * 4 + 2] = x + 1;
            ink[inn * 4 + 3] = y + 1;
            inn++;
        }
        ringPaper = paper;
        ringPaperQuads = pn;
        ringInk = ink;
        ringInkQuads = inn;
    }

    /** Una sola tanda de quads con color plano. */
    private static boolean drawQuads(GuiGraphics g, float[] data, int quads, int color) {
        if (quads <= 0) return true;
        try {
            org.joml.Matrix4f matrix = g.pose().last().pose();
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
            com.mojang.blaze3d.systems.RenderSystem.setShader(
                net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
            com.mojang.blaze3d.vertex.BufferBuilder buf =
                com.mojang.blaze3d.vertex.Tesselator.getInstance().getBuilder();
            buf.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
                com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR);
            float a = ((color >> 24) & 0xFF) / 255f;
            float r = ((color >> 16) & 0xFF) / 255f;
            float gc = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            for (int i = 0; i < quads; i++) {
                float x1 = data[i * 4];
                float y1 = data[i * 4 + 1];
                float x2 = data[i * 4 + 2];
                float y2 = data[i * 4 + 3];
                buf.vertex(matrix, x1, y2, 0).color(r, gc, b, a).endVertex();
                buf.vertex(matrix, x2, y2, 0).color(r, gc, b, a).endVertex();
                buf.vertex(matrix, x2, y1, 0).color(r, gc, b, a).endVertex();
                buf.vertex(matrix, x1, y1, 0).color(r, gc, b, a).endVertex();
            }
            com.mojang.blaze3d.vertex.Tesselator.getInstance().end();
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    // ── el tema entero ──────────────────────────────────────────────────────

    /**
     * v0.379.0: el tema entero de JourneyMap pasa a papel y tinta, no solo el
     * minimapa.
     *
     * Antes se pintaba la etiqueta de estado y las del minimapa, y el fondo de
     * pantalla completa se ponia marron oscuro -que es justo lo que se veia: la
     * rejilla oscura-. Los botones seguian azules porque JourneyMap los dibuja
     * con sus PROPIAS texturas del tema, y esas no se pueden recolorear desde
     * fuera. Ahora, a cada boton se le apaga `useThemeImages`, de modo que el mod
     * los pinta con sus colores planos, y esos colores pasan a ser papel con la
     * tinta de la casa. Se tocan tambien la mira y el canto cuadrado.
     *
     * Solo en memoria: la configuracion del jugador no se toca.
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

            // Pantalla completa: papel claro en vez del marron oscuro.
            Object full = read(theme, "fullscreen");
            if (full != null) {
                safeWrite(full, "background", spec("#E8DCB4", 0.96f));
                paintLabel(read(full, "statusLabel"));
            }

            // Botones y conmutadores: sin texturas del tema y en papel.
            Object control = read(theme, "control");
            if (control != null) {
                paintButton(read(control, "button"));
                paintButton(read(control, "toggle"));
            }

            // Barras de herramientas: sin textura, que el mod no pinte su cinta.
            Object container = read(theme, "container");
            if (container != null) {
                Object toolbar = read(container, "toolbar");
                if (toolbar != null) {
                    paintToolbar(read(toolbar, "horizontal"));
                    paintToolbar(read(toolbar, "vertical"));
                }
            }

            Object minimap = read(theme, "minimap");
            if (minimap == null) return;
            for (String shape : new String[] {"circle", "square"}) {
                Object shapeSpec = read(minimap, shape);
                if (shapeSpec == null) continue;
                paintLabel(read(shapeSpec, "labelTop"));
                paintLabel(read(shapeSpec, "labelBottom"));
                paintLabel(read(shapeSpec, "compassLabel"));
                // El marco del mapa es un ColorSpec completo, no una cadena.
                safeWrite(shapeSpec, "frame", spec("#2A1C12", 1.0f));
                safeWrite(shapeSpec, "reticle", spec("#2A1C12", 0.70f));
                safeWrite(shapeSpec, "reticleHeading", spec("#8C2A1E", 0.85f));
            }

            Class<?> themeClass = Class.forName("journeymap.client.ui.theme.Theme");
            loader.getMethod("setCurrentTheme", themeClass).invoke(null, theme);
            themeTried = true;
            patchedTheme = theme;
            note("tema en papel aplicado");
        } catch (Throwable ignored) {
        }
    }

    /** v0.379.0: un ColorSpec nuevo con el color de la casa. */
    private static Object spec(String hex, float alpha) {
        try {
            Class<?> colorSpec = Class.forName("journeymap.client.ui.theme.Theme$ColorSpec");
            return colorSpec.getConstructor(String.class, float.class).newInstance(hex, alpha);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** v0.379.0: etiqueta de papel con letra de tinta y sin sombra. */
    private static void paintLabel(Object labelSpec) {
        if (labelSpec == null) return;
        safeWrite(labelSpec, "margin", Integer.valueOf(2));
        safeWrite(labelSpec, "background", spec("#E6D2A6", 0.92f));
        safeWrite(labelSpec, "foreground", spec("#2A1C12", 1.0f));
        safeWrite(labelSpec, "highlight", spec("#8C2A1E", 1.0f));
        safeWrite(labelSpec, "shadow", Boolean.FALSE);
    }

    /**
     * v0.379.0: boton de papel. Apagar `useThemeImages` es lo que quita el azul:
     * con texturas, JourneyMap dibuja su PNG y los colores no se usan.
     */
    private static void paintButton(Object buttonSpec) {
        if (buttonSpec == null) return;
        safeWrite(buttonSpec, "useThemeImages", Boolean.FALSE);
        safeWrite(buttonSpec, "buttonOn", spec("#DCCB9F", 0.95f));
        safeWrite(buttonSpec, "buttonOff", spec("#D3C193", 0.88f));
        safeWrite(buttonSpec, "buttonHoverOn", spec("#F3E7C8", 0.97f));
        safeWrite(buttonSpec, "buttonHoverOff", spec("#E8DAB4", 0.95f));
        safeWrite(buttonSpec, "buttonDisabled", spec("#BFB08A", 0.70f));
        safeWrite(buttonSpec, "iconOn", spec("#2A1C12", 1.0f));
        safeWrite(buttonSpec, "iconOff", spec("#4A3A22", 0.95f));
        safeWrite(buttonSpec, "iconHoverOn", spec("#8C2A1E", 1.0f));
        safeWrite(buttonSpec, "iconHoverOff", spec("#5C3A16", 1.0f));
        safeWrite(buttonSpec, "iconDisabled", spec("#8A7A5A", 0.80f));
    }

    /** v0.379.0: la cinta de botones del mapa, sin textura. */
    private static void paintToolbar(Object toolbarSpec) {
        if (toolbarSpec == null) return;
        safeWrite(toolbarSpec, "useThemeImages", Boolean.FALSE);
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

    /**
     * Como write, pero un miembro que no cuadre no tumba el resto del tema.
     *
     * v0.390.0: si el valor viene nulo NO se escribe. `spec(...)` devuelve null
     * cuando el ColorSpec de JourneyMap no tiene el constructor que esperamos, y
     * escribir ese nulo dejaba el marco del minimapa sin color: peor que no
     * tocar nada.
     */
    private static void safeWrite(Object target, String name, Object value) {
        if (value == null) return;
        try {
            write(target, name, value);
        } catch (Throwable ignored) {
        }
    }

    private static int intOf(Object target, String name) {
        Object v = read(target, name);
        return v instanceof Number n ? n.intValue() : 0;
    }
}
