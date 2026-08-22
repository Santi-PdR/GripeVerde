package com.eternalcraft.gripeverde.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Configuracion del cliente (persiste en config/gripeverde-client.toml).
 * v0.10.1: primer valor persistente = musicVolume.
 */
public final class ClientConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue MUSIC_VOLUME;
    public static final ForgeConfigSpec.BooleanValue AMBIENCE_ENABLED;
    public static final ForgeConfigSpec.DoubleValue AMBIENCE_VOLUME;
    public static final ForgeConfigSpec.IntValue OPEN_COUNT;
    public static final ForgeConfigSpec.BooleanValue REDUCED_MOTION;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("audio");
        MUSIC_VOLUME = b
            .comment("Volumen de la musica del menu principal (0.0 - 1.0).")
            .defineInRange("musicVolume", 0.4, 0.0, 1.0);
        AMBIENCE_ENABLED = b
            .comment("Habilitar sonidos ambientales del menu principal.")
            .define("ambienceEnabled", true);
        AMBIENCE_VOLUME = b
            .comment("Volumen de los sonidos ambientales del menu (0.0 - 1.0).")
            .defineInRange("ambienceVolume", 1.0, 0.0, 1.0);
        OPEN_COUNT = b
            .comment("Numero de veces que se abrio el juego (contador persistente).")
            .defineInRange("openCount", 0, 0, 999999);
        b.pop();
        b.push("visual");
        REDUCED_MOTION = b
            .comment("Reducir el movimiento del ambiente del menu (accesibilidad).")
            .define("reducedMotion", false);
        b.pop();
        SPEC = b.build();
    }

    private ClientConfig() {}
}
