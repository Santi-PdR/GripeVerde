package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.util.EasedValue;
import com.eternalcraft.gripeverde.client.util.MotionPolicy;
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

    public enum Icon { NONE, OPEN_LEDGER }

    private final Runnable onClick;
    private Icon icon = Icon.NONE;
    // v0.26.0: sonido de prensado intercambiable (los botones normales usan
    // el pestillo de laton de siempre; el gramofono toca "pieza nueva").
    private Runnable clickSound = UiSounds::playClick;
    private boolean wasHovered = false;
    private final EasedValue hoverAnim = new EasedValue(6f);

    public SlimButton(int x, int y, int w, int h, Component label, Runnable onClick) {
        super(x, y, w, h, label);
        this.onClick = onClick;
    }

    public SlimButton withIcon(Icon icon) {
        this.icon = icon == null ? Icon.NONE : icon;
        return this;
    }

    public SlimButton withClickSound(Runnable sound) {
        if (sound != null) this.clickSound = sound;
        return this;
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
        boolean hovered = this.isHovered();
        if (hovered && !wasHovered) UiSounds.playHover();
        wasHovered = hovered;

        if (MotionPolicy.isReduced()) hoverAnim.snapTo(hovered);
        else {
            hoverAnim.setTarget(hovered);
            hoverAnim.tick(partialTick / 20f);
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

        int textColor = hoverAnim.lerpColor(DiaryTheme.BONE_WHITE, DiaryTheme.PAPER);
        if (icon == Icon.OPEN_LEDGER) {
            drawOpenLedger(g, x + (w - 12) / 2, y + (h - 10) / 2, textColor);
        } else {
            Font font = Minecraft.getInstance().font;
            String label = ellipsize(font, this.getMessage().getString(), Math.max(8, w - 12));
            int tw = font.width(label);
            int tx = x + (w - tw) / 2;
            int ty = y + (h - 8) / 2;
            g.drawString(font, label, tx, ty, textColor, false);
        }
    }

    private static void drawOpenLedger(GuiGraphics g, int x, int y, int color) {
        // Two readable page silhouettes and a spine, using the same pixel-ink language as the diary.
        g.fill(x + 1, y + 1, x + 5, y + 2, color);
        g.fill(x, y + 2, x + 2, y + 8, color);
        g.fill(x + 2, y + 7, x + 5, y + 9, color);
        g.fill(x + 3, y + 3, x + 5, y + 4, color);
        g.fill(x + 3, y + 5, x + 5, y + 6, color);

        g.fill(x + 7, y + 1, x + 11, y + 2, color);
        g.fill(x + 10, y + 2, x + 12, y + 8, color);
        g.fill(x + 7, y + 7, x + 10, y + 9, color);
        g.fill(x + 7, y + 3, x + 9, y + 4, color);
        g.fill(x + 7, y + 5, x + 9, y + 6, color);
        g.fill(x + 5, y, x + 7, y + 10, color);
    }
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
