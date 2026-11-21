package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.GripeVerdeMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Viste el fondo de las fichas sin reconstruir su contenido.
 *
 * Minecraft conserva y dibuja las lineas originales del tooltip en su capa
 * normal (z=400). Asi siguen intactos los colores, estilos, fuentes especiales
 * y componentes graficos de los objetos; tambien quedan por encima de los
 * iconos del inventario. No se cancela RenderTooltipEvent.Pre ni se aplanan
 * los textos a cadenas simples.
 */
@Mod.EventBusSubscriber(modid = GripeVerdeMod.MOD_ID, value = Dist.CLIENT)
public final class TooltipEventHandler {

    private static final int BG_TOP = 0xF8EFE0C8;
    private static final int BG_BOTTOM = 0xF0D4A98A;
    private static final int BORDER_TOP = 0xFF7A5A32;
    private static final int BORDER_BOTTOM = 0xFF3D2310;

    private TooltipEventHandler() {
    }

    /** Solo se personaliza el papel; el juego conserva integro el contenido. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltipColor(RenderTooltipEvent.Color event) {
        event.setBackgroundStart(BG_TOP);
        event.setBackgroundEnd(BG_BOTTOM);
        event.setBorderStart(BORDER_TOP);
        event.setBorderEnd(BORDER_BOTTOM);
    }
}
