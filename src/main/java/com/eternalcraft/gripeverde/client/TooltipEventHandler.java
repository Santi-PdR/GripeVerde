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
            if (stack == null || stack.isEmpty() || mc == null || mc.player == null) return;
            TooltipFlag flag = mc.options != null && mc.options.advancedItemTooltips
                ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
            List<Component> lines = stack.getTooltipLines(mc.player, flag);
            if (lines == null || lines.isEmpty()) return;
            event.setCanceled(true);
            drawPaperTooltip(event, lines);
        } catch (Throwable ignored) {
        }
    }

    /** v0.378.0: la ficha va pegada al cursor, como la del juego. */
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

    private static void drawPaperTooltip(RenderTooltipEvent.Pre event,
                                         List<Component> lines) {
        Font font = event.getFont();
        GuiGraphics g = event.getGraphics();
        if (font == null || g == null) return;
        int maxW = 210;
        java.util.List<String> rows = new java.util.ArrayList<>();
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
                continue;
            }
            for (FormattedCharSequence piece : pieces) {
                rows.add(piece == null ? "" : toPlain(piece));
            }
            if (rows.size() > 40) break;
        }
        if (rows.isEmpty()) return;
        int w = 0;
        for (String row : rows) w = Math.max(w, font.width(row));
        w += 12;
        int h = rows.size() * 10 + 8;
        int sw = Math.max(0, event.getScreenWidth());
        int sh = Math.max(0, event.getScreenHeight());
        // v0.378.0: getX/getY son el raton, no la esquina de la ficha. Hay que
        // pasar por el colocador del juego -el mismo que usa el vanilla- o la
        // ficha se queda abajo y a la izquierda del cursor, medio tapando el
        // objeto, que es lo que se veia en el inventario y en JEI.
        int x = event.getX();
        int y = event.getY();
        try {
            var positioner = event.getTooltipPositioner();
            if (positioner != null && sw > 0 && sh > 0) {
                org.joml.Vector2ic pos = positioner.positionTooltip(sw, sh, x, y, w, h);
                x = pos.x();
                y = pos.y();
            }
        } catch (Throwable ignored) {
        }
        if (sw > 0) x = Math.max(4, Math.min(x, sw - w - 4));
        if (sh > 0) y = Math.max(4, Math.min(y, sh - h - 4));
        com.eternalcraft.gripeverde.client.render.PaperTexture.drawTornSlip(
            g, x, y, w, h, 0xE6D2A6, 0x422810, 252);
        for (int i = 0; i < rows.size(); i++) {
            int color = i == 0 ? INK_TITLE : INK_BODY;
            g.drawString(font, rows.get(i), x + 6, y + 5 + i * 10, color, false);
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
