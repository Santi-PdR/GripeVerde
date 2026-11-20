package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.GripeVerdeMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltipColor(RenderTooltipEvent.Color event) {
        event.setBackgroundStart(BG_TOP);
        event.setBackgroundEnd(BG_BOTTOM);
        event.setBorderStart(BORDER_TOP);
        event.setBorderEnd(BORDER_BOTTOM);
    }
}
