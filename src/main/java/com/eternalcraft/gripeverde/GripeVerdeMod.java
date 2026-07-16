package com.eternalcraft.gripeverde;

import com.eternalcraft.gripeverde.client.ScreenEventHandler;
import com.eternalcraft.gripeverde.client.audio.ModSounds;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GripeVerdeMod.MOD_ID)
public class GripeVerdeMod {

    public static final String MOD_ID  = "gripeverde";
    public static final String VERSION = "0.6.0";

    public GripeVerdeMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Registro de SoundEvents custom
        ModSounds.SOUND_EVENTS.register(modBus);

        // Eventos de pantalla
        MinecraftForge.EVENT_BUS.register(new ScreenEventHandler());
    }
}