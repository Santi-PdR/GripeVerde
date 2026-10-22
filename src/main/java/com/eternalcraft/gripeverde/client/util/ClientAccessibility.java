package com.eternalcraft.gripeverde.client.util;

import com.eternalcraft.gripeverde.config.ClientConfig;

/** Shared client accessibility preferences for animated menu rendering. */
public final class ClientAccessibility {

    private ClientAccessibility() {}

    public static boolean reducedMotion() {
        try {
            return ClientConfig.REDUCED_MOTION.get();
        } catch (IllegalStateException ignored) {
            // A screen can render while client config values are still initializing.
            return false;
        }
    }
}
