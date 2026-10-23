package com.eternalcraft.gripeverde.client.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Shared high-contrast keyboard focus treatment for custom diary controls. */
@OnlyIn(Dist.CLIENT)
final class DiaryFocus {

    private DiaryFocus() {}

    static void drawKeyboardRing(GuiGraphics graphics, int x, int y, int width, int height,
                                 boolean keyboardFocused) {
        if (!keyboardFocused || width < 6 || height < 6) return;

        int ink = 0xFF32190D;
        int gold = 0xFFFFD978;
        drawBorder(graphics, x + 1, y + 1, width - 2, height - 2, ink);
        drawBorder(graphics, x + 2, y + 2, width - 4, height - 4, gold);
    }

    private static void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }
}
