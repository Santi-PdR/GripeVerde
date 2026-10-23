package com.eternalcraft.gripeverde.client.render;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Shared type styles for the cabinet's identity text. */
public final class DiaryTypography {
    public static final ResourceLocation DISPLAY_FONT =
        ResourceLocation.fromNamespaceAndPath("gripeverde", "diary_display");

    private DiaryTypography() {}

    public static Component display(Component text) {
        return text.copy().withStyle(style -> style.withFont(DISPLAY_FONT));
    }

    public static Component display(String text) {
        return display(Component.literal(text));
    }

    public static int drawCentered(GuiGraphics graphics, Font font, Component text,
                                   int centerX, int y, int color) {
        Component displayText = display(text);
        int width = font.width(displayText);
        graphics.drawString(font, displayText, centerX - width / 2, y, color, false);
        return width;
    }

    public static int drawCentered(GuiGraphics graphics, Font font, String text,
                                   int centerX, int y, int color) {
        return drawCentered(graphics, font, Component.literal(text), centerX, y, color);
    }
}
