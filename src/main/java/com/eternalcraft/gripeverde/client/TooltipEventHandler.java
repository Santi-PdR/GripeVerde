package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.GripeVerdeMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltipColor(RenderTooltipEvent.Color event) {
        event.setBackgroundStart(BG_TOP);
        event.setBackgroundEnd(BG_BOTTOM);
        event.setBorderStart(BORDER_TOP);
        event.setBorderEnd(BORDER_BOTTOM);
    }
}
