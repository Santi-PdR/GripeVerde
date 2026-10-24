package com.eternalcraft.gripeverde.client.util;

import com.eternalcraft.gripeverde.config.ClientConfig;
import net.minecraft.client.Minecraft;

/** Shared client accessibility preferences for animated menu rendering. */
public final class ClientAccessibility {

    private ClientAccessibility() {}

    /**
     * Treat the connection phase as gameplay too: the level may still be null
     * while a player or network connection is already present.
     */
    public static boolean hasActiveGameSession() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft != null && (minecraft.level != null
                || minecraft.player != null
                || minecraft.getConnection() != null);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean reducedMotion() {
        // Keep every custom interface still while the player is in a world or
        // joining a server; the saved preference continues to control menus.
        if (hasActiveGameSession()) return true;
        try {
            return ClientConfig.REDUCED_MOTION.get();
        } catch (IllegalStateException ignored) {
            // A screen can render while client config values are still initializing.
            return false;
        }
    }

    public static boolean reduceFlashes() {
        try {
            return ClientConfig.REDUCE_FLASHES.get();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }
}
