package com.eternalcraft.gripeverde;

import com.eternalcraft.gripeverde.client.audio.ModSounds;
import com.eternalcraft.gripeverde.config.ClientConfig;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GripeVerdeMod.MOD_ID)
public class GripeVerdeMod {

    public static final String MOD_ID  = "gripeverde";
    public static final String VERSION = "0.123.0";

    public GripeVerdeMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Registro de SoundEvents custom
        ModSounds.SOUND_EVENTS.register(modBus);

        // Registro de configuracion cliente (persiste en config/gripeverde-client.toml)
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "gripeverde-client.toml");

        // v0.23.0: sin registro manual de ScreenEventHandler. La clase ya se
        // autorregistra con @Mod.EventBusSubscriber(Dist.CLIENT); registrarla
        // aqui duplicaba la entrega de eventos y, al ejecutarse en AMBOS lados,
        // forzaba en servidores dedicados la carga de clases client-only
        // (net.minecraftforge.client.event.*) que no existen alli (crash).
        // Asi el mod queda inerte en servidor, como manda la guia Forge Sides.

        // v0.21.11: al cerrar el juego se escribe la memoria pendiente del cronista.
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(
                com.eternalcraft.gripeverde.client.lore.ChroniclerMemory::flushForShutdown,
                "gripeverde-memory-flush"));
        } catch (Throwable ignored) {}
    }
}
