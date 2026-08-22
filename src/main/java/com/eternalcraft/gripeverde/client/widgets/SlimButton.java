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
        g.fill(x + 3, y + 3, x + w - 3, y + 4, DiaryTheme.withAlpha(DiaryTheme.BRONZE, 48));
        g.fill(x + 3, y + h - 4, x + w - 3, y + h - 3, DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 80));
        g.fill(x + 4, y + 4, x + 5, y + 5, DiaryTheme.withAlpha(DiaryTheme.GOLD_TARNISHED, 120));
        g.fill(x + w - 5, y + 4, x + w - 4, y + 5, DiaryTheme.withAlpha(DiaryTheme.GOLD_TARNISHED, 120));

        Font font = Minecraft.getInstance().font;
        String label = ellipsize(font, this.getMessage().getString(), Math.max(8, w - 12));
        int tw = font.width(label);
        int textColor = hoverAnim.lerpColor(DiaryTheme.BONE_WHITE, DiaryTheme.PAPER);
        int tx = x + (w - tw) / 2;
        int ty = y + (h - 8) / 2;
        g.drawString(font, label, tx, ty, textColor, false);
    }

    private static String ellipsize(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        int room = Math.max(0, maxWidth - font.width(ellipsis));
        return font.plainSubstrByWidth(text, room) + ellipsis;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }
}
