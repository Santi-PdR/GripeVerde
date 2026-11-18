package com.eternalcraft.gripeverde.client.hud;

import com.eternalcraft.gripeverde.GripeVerdeMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * v0.295.0: registra el reloj de la partida arriba de todo lo demas.
 *
 * Va en el bus de MOD, que es donde Forge dispara el registro de capas de la
 * interfaz; por eso esta clase aparte de ScreenEventHandler, que escucha el
 * bus de Forge.
 */
@Mod.EventBusSubscriber(modid = GripeVerdeMod.MOD_ID, value = Dist.CLIENT,
    bus = Mod.EventBusSubscriber.Bus.MOD)
public final class WatchHudEvents {

    private WatchHudEvents() {
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        try {
            event.registerAboveAll("gripeverde_watch",
                (gui, graphics, partialTick, screenWidth, screenHeight) ->
                    WatchHud.render(graphics, screenWidth, screenHeight));
        } catch (Throwable ignored) {
        }
    }
}
