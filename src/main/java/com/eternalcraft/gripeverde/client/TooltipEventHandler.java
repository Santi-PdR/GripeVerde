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

    // Paleta pergamino victoriano
    private static final int BG_TOP    = 0xF2E8DCB4; // pergamino claro
    private static final int BG_BOTTOM = 0xF2C9B78A; // pergamino oscuro
    private static final int BORDER_TOP    = 0xFF6B4226; // tinta media
    private static final int BORDER_BOTTOM = 0xFF422810; // tinta oscura

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltipColor(RenderTooltipEvent.Color event) {
        event.setBackgroundStart(BG_TOP);
        event.setBackgroundEnd(BG_BOTTOM);
        event.setBorderStart(BORDER_TOP);
        event.setBorderEnd(BORDER_BOTTOM);
    }
}