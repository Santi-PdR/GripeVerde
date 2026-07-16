package com.eternalcraft.gripeverde.client.theme;

import net.minecraft.client.gui.GuiGraphics;

public interface UIComponent {
    int preferredWidth();
    int preferredHeight();
    
    void setBounds(int x, int y, int width, int height);
    
    void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);
    
    boolean isVisible();
    void setVisible(boolean visible);
}