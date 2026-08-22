package com.eternalcraft.gripeverde;

import com.eternalcraft.gripeverde.client.ScreenEventHandler;
import com.eternalcraft.gripeverde.client.audio.ModSounds;
import com.eternalcraft.gripeverde.config.ClientConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GripeVerdeMod.MOD_ID)
public class GripeVerdeMod {

    public static final String MOD_ID  = "gripeverde";
    public static final String VERSION = "0.21.18";

    public GripeVerdeMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Registro de SoundEvents custom
        ModSounds.SOUND_EVENTS.register(modBus);

        // Registro de configuracion cliente (persiste en config/gripeverde-client.toml)
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "gripeverde-client.toml");

        // Eventos de pantalla
        MinecraftForge.EVENT_BUS.register(new ScreenEventHandler());

        // v0.21.11: al cerrar el juego se escribe la memoria pendiente del cronista.
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(
                com.eternalcraft.gripeverde.client.lore.ChroniclerMemory::flushForShutdown,
                "gripeverde-memory-flush"));
        } catch (Throwable ignored) {}
    }
}
