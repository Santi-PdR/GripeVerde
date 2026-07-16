package com.eternalcraft.gripeverde.client.theme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/**
 * Contrato base de un tema visual aplicable a una pantalla.
 */
public interface ScreenTheme {

    String getName();

    boolean appliesTo(Screen screen);

    default void renderBackground(GuiGraphics g, Screen screen, int w, int h, float partialTick) {}
    default void renderFrame(GuiGraphics g, Screen screen, int w, int h, float partialTick) {}
    default void renderDecorations(GuiGraphics g, Screen screen, int w, int h, float partialTick) {}
    default void renderEffects(GuiGraphics g, Screen screen, int w, int h, float partialTick) {}

    default void render(GuiGraphics g, Screen screen, int w, int h, float partialTick) {
        renderBackground(g, screen, w, h, partialTick);
        renderFrame(g, screen, w, h, partialTick);
        renderDecorations(g, screen, w, h, partialTick);
        renderEffects(g, screen, w, h, partialTick);
    }
}