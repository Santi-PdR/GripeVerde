package com.eternalcraft.gripeverde.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Configuracion del cliente (persiste en config/gripeverde-client.toml).
 * v0.10.1: primer valor persistente = musicVolume.
 */
public final class ClientConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue MUSIC_VOLUME;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("audio");
        MUSIC_VOLUME = b
            .comment("Volumen de la musica del menu principal (0.0 - 1.0).")
            .defineInRange("musicVolume", 0.4, 0.0, 1.0);
        b.pop();
        SPEC = b.build();
    }

    private ClientConfig() {}
}