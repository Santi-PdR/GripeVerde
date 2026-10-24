package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.util.ClientAccessibility;
import com.eternalcraft.gripeverde.client.util.EasedValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SlimButton extends AbstractButton {

    private final Runnable onClick;
    // v0.26.0: sonido de prensado intercambiable (los botones normales usan
    // el pestillo de laton de siempre; el gramofono toca "pieza nueva").
    private Runnable clickSound = UiSounds::playClick;
    private boolean wasHovered = false;
    private final EasedValue hoverAnim = new EasedValue(6f);
    private Component cachedMessage;
    private Language cachedLanguage;
    private Font cachedFont;
    private int cachedMaxTextWidth = -1;
    private int cachedLabelWidth;
    private String cachedLabel = "";

    public SlimButton(int x, int y, int w, int h, Component label, Runnable onClick) {
        super(x, y, w, h, label);
        this.onClick = onClick;
    }

    public SlimButton withClickSound(Runnable sound) {
        if (sound != null) this.clickSound = sound;
        return this;
    }

    /** Adjusts the hitbox and text centering for responsive menu layouts. */
    public void setVisualHeight(int height) {
        this.height = Math.max(1, height);
    }

    /** Adjusts the complete hitbox for compact grid layouts. */
    public void setVisualBounds(int x, int y, int width, int height) {
        this.setX(x);
        this.setY(y);
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
    }

    @Override
    public void onPress() {
        this.clickSound.run();
        this.onClick.run();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        // Silencia vanilla
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean keyboardFocused = this.active && this.isFocused() && !this.isMouseOver(mouseX, mouseY);
        boolean hovered = this.isHovered() || keyboardFocused;
        if (hovered && !wasHovered) UiSounds.playHover();
        wasHovered = hovered;

        if (ClientAccessibility.reducedMotion()) hoverAnim.setImmediately(hovered);
        else {
            hoverAnim.setTarget(hovered);
            hoverAnim.tickRealtime();
        }

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

        // Keep keyboard focus visible without extending the hitbox or overlapping
        // compact buttons that may be stacked with no gap in very short windows.
        if (keyboardFocused && w > 2 && h > 2) {
            int focus = 0xFFFFE28A;
            g.fill(x + 1, y + 1, x + w - 1, y + 2, focus);
            g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, focus);
            g.fill(x + 1, y + 1, x + 2, y + h - 1, focus);
            g.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, focus);
        }

        Font font = Minecraft.getInstance().font;
        int maxTextWidth = Math.max(8, w - 12);
        this.refreshLabelCache(font, maxTextWidth);
        String label = this.cachedLabel;
        int tw = this.cachedLabelWidth;
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

    private void refreshLabelCache(Font font, int maxWidth) {
        Component message = this.getMessage();
        Language language = Language.getInstance();
        if (message == this.cachedMessage && language == this.cachedLanguage
                && font == this.cachedFont && maxWidth == this.cachedMaxTextWidth) {
            return;
        }

        this.cachedMessage = message;
        this.cachedLanguage = language;
        this.cachedFont = font;
        this.cachedMaxTextWidth = maxWidth;
        this.cachedLabel = ellipsize(font, message.getString(), maxWidth);
        this.cachedLabelWidth = font.width(this.cachedLabel);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }
}
