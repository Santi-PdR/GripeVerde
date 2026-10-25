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
    public static final ForgeConfigSpec.DoubleValue UI_VOLUME;
    public static final ForgeConfigSpec.IntValue OPEN_COUNT;
    public static final ForgeConfigSpec.BooleanValue REDUCED_MOTION;
    public static final ForgeConfigSpec.BooleanValue REDUCE_FLASHES;
    public static final ForgeConfigSpec.BooleanValue REPLACE_TITLE;
    public static final ForgeConfigSpec.BooleanValue REPLACE_PAUSE;
    public static final ForgeConfigSpec.BooleanValue DEBUG_SHORTCUT;

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
        UI_VOLUME = b
            .comment("Volumen de los efectos de interfaz del mod (0.0 - 1.0).")
            .defineInRange("uiVolume", 1.0, 0.0, 1.0);
        OPEN_COUNT = b
            .comment("Numero de veces que se abrio el juego (contador persistente).")
            .defineInRange("openCount", 0, 0, 999999);
        b.pop();
        b.push("visual");
        REDUCED_MOTION = b
            .comment("Reducir el movimiento del ambiente del menu (accesibilidad).")
            .define("reducedMotion", false);
        REDUCE_FLASHES = b
            .comment("Reducir destellos de tormenta (relampagos) sin congelar el resto de la escena (accesibilidad, fotosensibilidad).")
            .define("reduceFlashes", false);
        b.pop();
        b.push("integration");
        REPLACE_TITLE = b
            .comment("Reemplazar la TitleScreen vanilla por la pantalla Diary (false cede ante otros mods de menu).")
            .define("replaceTitleScreen", true);
        REPLACE_PAUSE = b
            .comment("Reemplazar la PauseScreen vanilla por la pantalla Diary.")
            .define("replacePauseScreen", true);
        b.pop();
        b.push("debug");
        DEBUG_SHORTCUT = b
            .comment("Habilitar el atajo de depuracion Ctrl+0 (salir del mundo sin confirmar). Desactivado por defecto.")
            .define("debugShortcut", false);
        b.pop();
        SPEC = b.build();
    }

    private ClientConfig() {}
}
