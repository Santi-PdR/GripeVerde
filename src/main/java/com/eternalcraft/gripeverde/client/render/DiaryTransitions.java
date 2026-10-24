package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/** A brief, low-cost fade between front-end diary screens. */
public final class DiaryTransitions {

    private static final String PREFIX = "com.eternalcraft.gripeverde.client.screens.";
    private static final long FADE_MS = 75L;
    private static Screen current;
    private static long openedAtMs;

    private DiaryTransitions() {}

    /** Kept for the existing screen-opening hook; no navigation stack is needed. */
    public static void noteOpening(Screen from, Screen to) {
        if (com.eternalcraft.gripeverde.client.util.ClientAccessibility.hasActiveGameSession()
                || to == null || !to.getClass().getName().startsWith(PREFIX)) {
            current = null;
        }
    }

    /** A short opacity-only fade; gameplay interfaces never receive an overlay. */
    public static void render(Screen screen, GuiGraphics graphics) {
        if (screen == null || com.eternalcraft.gripeverde.client.util.ClientAccessibility.hasActiveGameSession()
                || !screen.getClass().getName().startsWith(PREFIX)) {
            current = null;
            return;
        }

        long now = System.currentTimeMillis();
        if (screen != current) {
            current = screen;
            openedAtMs = now;
        }
        long elapsed = now - openedAtMs;
        if (elapsed < 0L || elapsed >= FADE_MS) return;

        int alpha = (int) (18L * (FADE_MS - elapsed) / FADE_MS);
        if (alpha > 0) {
            graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), (alpha << 24) | 0x1A1108);
        }
    }
}
