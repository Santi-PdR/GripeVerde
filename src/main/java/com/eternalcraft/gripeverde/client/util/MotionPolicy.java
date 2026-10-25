package com.eternalcraft.gripeverde.client.util;

import com.eternalcraft.gripeverde.config.ClientConfig;
import net.minecraft.client.Minecraft;

/** Shared policy for motion-sensitive client UI. */
public final class MotionPolicy {
    private MotionPolicy() {}

    /**
     * Keeps custom menu controls static during a live session, even when the
     * user's reduced-motion preference is off. The title menu retains its
     * atmospheric feedback because no world is loaded there.
     */
    public static boolean isReduced() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.level != null) return true;
        try {
            return ClientConfig.REDUCED_MOTION.get();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }
}
