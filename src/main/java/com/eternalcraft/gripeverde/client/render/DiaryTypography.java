package com.eternalcraft.gripeverde.client.render;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Shared type styles for the cabinet's identity text. */
public final class DiaryTypography {
    public static final ResourceLocation DISPLAY_FONT = new ResourceLocation("gripeverde", "diary_display");

    private DiaryTypography() {}

    public static Component display(Component text) {
        return text.copy().withStyle(style -> style.withFont(DISPLAY_FONT));
    }
}
