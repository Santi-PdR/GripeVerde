package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.GripeVerdeMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Reemplaza los colores morados vanilla de los tooltips por una paleta
 * pergamino victoriana. Usa el evento RenderTooltipEvent.Color de Forge
 * (mucho mas limpio que un mixin sobre TooltipRenderUtil).
 *
 * Se aplica GLOBALMENTE a todos los tooltips (Chat, Sound, Inventario,
 * botones Diary, etc.).
 */
@Mod.EventBusSubscriber(modid = GripeVerdeMod.MOD_ID, value = Dist.CLIENT)
public class TooltipEventHandler {

    // Paleta pergamino victoriana profesional: mejor contraste y profundidad
    private static final int BG_TOP    = 0xF8EFE0C8; // ligeramente mas claro para legibilidad
    private static final int BG_BOTTOM = 0xF0D4A98A; // mas cálido
    private static final int BORDER_TOP    = 0xFF7A5A32; // bronze medio
    private static final int BORDER_BOTTOM = 0xFF3D2310; // tinta quemada

    /**
     * v0.382.0: la ficha se dibuja al final del cuadro, no en el momento del
     * evento. En el inventario y en JEI los iconos de las casillas se pintan
     * DESPUES del cartel, y salian encima de la ficha: se leia la descripcion
     * mezclada con los objetos. Guardando los renglones y pintandolos en
     * ScreenEvent.Render.Post queda siempre arriba de todo.
     */
    private static java.util.List<String> pendingRows;
    private static int pendingTooltipX;
    private static int pendingTooltipY;
    private static Font pendingFont;

    /** Tinta de la casa para el texto de los tooltips. */
    private static final int INK_TITLE = 0xFF1A1108;
    private static final int INK_BODY = 0xFF2A1C10;

    /**
     * v0.375.0: el fondo del tooltip era pergamino, pero el texto seguia
     * siendo el blanco y el gris del juego, que sobre papel no se leen. Aqui se
     * pasa todo el texto a tinta: el primer renglon (el titulo) un punto mas
     * oscuro y el resto en sepia de tinta. Se hace renglon por renglon y en
     * profundidad, para que los textos compuestos no dejen hijos en blanco.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltipGather(RenderTooltipEvent.GatherComponents event) {
        try {
            List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
            for (int i = 0; i < elements.size(); i++) {
                var left = elements.get(i).left();
                if (left.isEmpty()) continue;
                if (!(left.get() instanceof Component component)) continue;
                elements.set(i, Either.left(recolor(component, i == 0 ? INK_TITLE : INK_BODY)));
            }
        } catch (Throwable ignored) {
        }
    }

    /** Copia el texto en tinta, renglon a renglon y con sus hijos. */
    private static Component recolor(Component in, int rgb) {
        MutableComponent out;
        try {
            out = in.plainCopy().withStyle(style -> style.withColor(TextColor.fromRgb(rgb)));
        } catch (Throwable ignored) {
            return in;
        }
        try {
            for (Component sibling : in.getSiblings()) {
                out.append(recolor(sibling, rgb));
            }
        } catch (Throwable ignored) {
        }
        return out;
    }

    /**
     * v0.377.0: la ficha del objeto (su tooltip) se dibuja a mano.
     *
     * El juego escribe siempre CON SOMBRA. Con letra blanca la sombra sirve de
     * contorno, pero con tinta oscura sobre papel la sombra es otra mancha
     * oscura al lado y la letra parece mas gruesa y borrosa: era lo que se veia
     * en los nombres de los items. Aqui se cancela el del juego y se pega una
     * ficha de papel con la misma informacion en tinta y SIN sombra.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onTooltipPre(RenderTooltipEvent.Pre event) {
        try {
            ItemStack stack = event.getItemStack();
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) return;
            List<Component> lines;
            if (stack != null && !stack.isEmpty()) {
                if (mc.player == null) return;
                TooltipFlag flag = mc.options != null && mc.options.advancedItemTooltips
                    ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
                lines = stack.getTooltipLines(mc.player, flag);
            } else {
                // v0.391.0: los carteles que no son de objetos -botones, fichas,
                // botones de la casa- se quedaban con el fondo de papel pero con
                // la letra del juego, que se escribe CON SOMBRA: sobre papel la
                // sombra es una segunda mancha al lado de cada letra. Se pasan a
                // la misma ficha que los objetos, en tinta y sin sombra.
                lines = plainLinesFromComponents(event.getComponents());
            }
            if (lines == null || lines.isEmpty()) return;
            Font font = event.getFont();
            if (font == null) return;
            List<String> rows = buildRows(font, lines);
            if (rows.isEmpty()) return;
            if (mc.screen != null) {
                // Dentro de una pantalla, se guarda la misma colocacion que
                // usaria el juego y se pinta al final del cuadro, por encima de
                // los iconos. No se vuelve a calcular desde el raton: el ancla
                // del evento y la esquina final del cartel no son lo mismo.
                int[] pos = tooltipPosition(event, font, rows);
                if (event.getGraphics() == null) return;
                event.setCanceled(true);
                pendingRows = rows;
                pendingTooltipX = pos[0];
                pendingTooltipY = pos[1];
                pendingFont = font;
            } else if (drawPaperTooltip(event, font, rows)) {
                event.setCanceled(true);
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * v0.391.0: el texto plano de un cartel ya armado por el juego.
     *
     * Devuelve null si alguno de los pedazos no se deja leer (por ejemplo un
     * dibujo), y en ese caso no se toca el cartel: se prefiere el del juego
     * antes que uno a medias.
     */
    private static List<Component> plainLinesFromComponents(
            List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> components) {
        if (components == null || components.isEmpty()) return null;
        List<Component> out = new java.util.ArrayList<>();
        boolean hasText = false;
        for (var c : components) {
            if (c == null) return null;
            String plain = plainOfComponent(c);
            if (plain == null) return null;
            if (!plain.isBlank()) hasText = true;
            // Se conservan los renglones vacios interiores: en un tooltip son
            // separadores deliberados, no componentes graficos.
            out.add(Component.literal(plain));
        }
        return hasText ? out : null;
    }

    /** v0.391.0: el texto de un pedazo de cartel, buscado por forma. */
    private static String plainOfComponent(Object c) {
        if (c == null) return null;
        for (Class<?> type = c.getClass(); type != null && type != Object.class;
             type = type.getSuperclass()) {
            for (java.lang.reflect.Field f : type.getDeclaredFields()) {
                if (f.getType() != FormattedCharSequence.class
                        || java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                try {
                    f.setAccessible(true);
                    Object value = f.get(c);
                    if (value instanceof FormattedCharSequence seq) return toPlain(seq);
                } catch (Throwable ignored) {
                }
            }
        }
        return null;
    }

    /** v0.377.0: el texto de un renglon ya recortado, sin formato. */
    private static String toPlain(FormattedCharSequence seq) {
        StringBuilder sb = new StringBuilder();
        try {
            seq.accept((index, style, codepoint) -> {
                sb.appendCodePoint(codepoint);
                return true;
            });
        } catch (Throwable ignored) {
        }
        return sb.toString();
    }

    /** v0.382.0: los renglones de la ficha, ya recortados al ancho de la casa. */
    private static java.util.List<String> buildRows(Font font, List<Component> lines) {
        java.util.List<String> rows = new java.util.ArrayList<>();
        if (font == null || lines == null || lines.isEmpty()) return rows;
        int maxW = 210;
        linesLoop:
        for (Component line : lines) {
            java.util.List<FormattedCharSequence> pieces;
            try {
                pieces = font.split(line, maxW);
            } catch (Throwable ignored) {
                pieces = null;
            }
            if (pieces == null || pieces.isEmpty()) {
                String plain = line == null ? "" : line.getString();
                rows.add(plain);
                if (rows.size() >= 40) break;
                continue;
            }
            for (FormattedCharSequence piece : pieces) {
                rows.add(piece == null ? "" : toPlain(piece));
                if (rows.size() >= 40) break linesLoop;
            }
        }
        return rows;
    }

    private static boolean drawPaperTooltip(RenderTooltipEvent.Pre event, Font font,
                                            java.util.List<String> rows) {
        GuiGraphics g = event.getGraphics();
        if (font == null || g == null || rows == null || rows.isEmpty()) return false;
        int[] pos = tooltipPosition(event, font, rows);
        drawRows(g, font, rows, pos[0], pos[1]);
        return true;
    }

    /**
     * Coloca la ficha con el mismo posicionador que Minecraft. Forge documenta
     * getX/getY como la posicion del cartel (por defecto, el raton), pero esa
     * posicion es solo el ancla: el posicionador la convierte en la esquina
     * final y la voltea si hace falta.
     */
    private static int[] tooltipPosition(RenderTooltipEvent.Pre event, Font font,
                                         java.util.List<String> rows) {
        int w = tooltipWidth(font, rows);
        int h = tooltipHeight(rows);
        int sw = Math.max(0, event.getScreenWidth());
        int sh = Math.max(0, event.getScreenHeight());
        int x = event.getX();
        int y = event.getY();
        try {
            var positioner = event.getTooltipPositioner();
            if (positioner != null && sw > 0 && sh > 0) {
                org.joml.Vector2ic pos = positioner.positionTooltip(sw, sh, x, y, w, h);
                if (pos != null) {
                    x = pos.x();
                    y = pos.y();
                }
            }
        } catch (Throwable ignored) {
        }
        if (sw > 0) x = Math.max(4, Math.min(x, sw - w - 4));
        if (sh > 0) y = Math.max(4, Math.min(y, sh - h - 4));
        return new int[] {x, y};
    }

    private static int tooltipWidth(Font font, java.util.List<String> rows) {
        int w = 0;
        for (String row : rows) w = Math.max(w, font.width(row));
        return w + 12;
    }

    private static int tooltipHeight(java.util.List<String> rows) {
        return rows.size() * 10 + 8;
    }

    private static void drawRows(GuiGraphics g, Font font, java.util.List<String> rows,
                                 int x, int y) {
        if (g == null || font == null || rows == null || rows.isEmpty()) return;
        int w = tooltipWidth(font, rows);
        int h = tooltipHeight(rows);
        com.eternalcraft.gripeverde.client.render.PaperTexture.drawTornSlip(
            g, x, y, w, h, 0xE6D2A6, 0x422810, 255);
        for (int i = 0; i < rows.size(); i++) {
            int color = i == 0 ? INK_TITLE : INK_BODY;
            g.drawString(font, rows.get(i), x + 6, y + 5 + i * 10, color, false);
        }
    }

    /** v0.382.0: al abrir o cerrar una pantalla no queda ficha pendiente. */
    @SubscribeEvent
    public static void onScreenChange(net.minecraftforge.client.event.ScreenEvent.Init.Post event) {
        pendingRows = null;
        pendingFont = null;
        pendingTooltipX = 0;
        pendingTooltipY = 0;
    }

    /**
     * v0.382.0: pinta la ficha guardada cuando la pantalla ya termino de
     * dibujarse. Se hace con prioridad LOWEST para quedar por encima de los
     * iconos, de JEI y de cualquier capa que se sume despues de la nuestra.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onScreenRenderPost(
            net.minecraftforge.client.event.ScreenEvent.Render.Post event) {
        java.util.List<String> rows = pendingRows;
        Font font = pendingFont;
        int x = pendingTooltipX;
        int y = pendingTooltipY;
        pendingRows = null;
        pendingFont = null;
        pendingTooltipX = 0;
        pendingTooltipY = 0;
        if (rows == null || rows.isEmpty()) return;
        try {
            GuiGraphics g = event.getGuiGraphics();
            if (g == null || font == null) return;
            drawRows(g, font, rows, x, y);
        } catch (Throwable ignored) {
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltipColor(RenderTooltipEvent.Color event) {
        event.setBackgroundStart(BG_TOP);
        event.setBackgroundEnd(BG_BOTTOM);
        event.setBorderStart(BORDER_TOP);
        event.setBorderEnd(BORDER_BOTTOM);
    }
}
