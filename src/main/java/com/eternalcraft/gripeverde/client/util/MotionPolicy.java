package com.eternalcraft.gripeverde.client.util;

import com.eternalcraft.gripeverde.config.ClientConfig;

/** Shared policy for motion-sensitive client UI. */
public final class MotionPolicy {
    private MotionPolicy() {}

    public static boolean isReduced() {
        try {
            return ClientConfig.REDUCED_MOTION.get();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }
}