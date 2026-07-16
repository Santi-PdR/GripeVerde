package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.util.EasedValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SlimButton extends AbstractButton {

    private final Runnable onClick;
    private boolean wasHovered = false;
    private final EasedValue hoverAnim = new EasedValue(6f);

    public SlimButton(int x, int y, int w, int h, Component label, Runnable onClick) {
        super(x, y, w, h, label);
        this.onClick = onClick;
    }

    @Override
    public void onPress() {
        UiSounds.playClick();
        this.onClick.run();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        // Silencia vanilla
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.isHovered();
        if (hovered && !wasHovered) UiSounds.playHover();
        wasHovered = hovered;

        hoverAnim.setTarget(hovered);
        hoverAnim.tick(partialTick / 20f);

        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;

        int bg = hoverAnim.lerpColor(0xFF120A05, 0xFF2A1810);
        int border = hoverAnim.lerpColor(DiaryTheme.BRONZE_DARK, DiaryTheme.REDCOAT);

        g.fill(x, y, x + w, y + h, bg);
        g.fill(x, y,         x + w, y + 1,     border);
        g.fill(x, y + h - 1, x + w, y + h,     border);
        g.fill(x, y,         x + 1, y + h,     border);
        g.fill(x + w - 1, y, x + w, y + h,     border);

        Font font = Minecraft.getInstance().font;
        Component label = this.getMessage();
        int tw = font.width(label);
        int textColor = hoverAnim.lerpColor(DiaryTheme.BONE_WHITE, DiaryTheme.PAPER);
        g.drawString(font, label, x + (w - tw) / 2, y + (h - 8) / 2, textColor, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }
}