package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.DiaryTypography;
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

    public enum Icon { NONE, OPEN_LEDGER, DISPATCH_NOTE, FILING }

    private final Runnable onClick;
    private Icon icon = Icon.NONE;
    // v0.26.0: sonido de prensado intercambiable (los botones normales usan
    // el pestillo de laton de siempre; el gramofono toca "pieza nueva").
    private Runnable clickSound = UiSounds::playClick;
    private boolean wasHovered = false;
    // v0.244.0: sello de lacre al apretar, igual que los botones grandes.
    private static final int SEAL_TICKS = 9;
    private int sealTicks = 0;
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean hit = super.mouseClicked(mouseX, mouseY, button);
        if (hit && this.active && this.visible && !MotionPolicy.isReduced()) {
            this.sealTicks = SEAL_TICKS;
        }
        return hit;
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

        // v0.244.0: el sello se abre y se apaga; en movimiento reducido no existe.
        if (MotionPolicy.isReduced()) {
            this.sealTicks = 0;
        } else if (this.sealTicks > 0) {
            PaperTexture.drawSealRing(g, x, y, w, h, 1f - (this.sealTicks / (float) SEAL_TICKS));
            this.sealTicks--;
        }

        int bg = hoverAnim.lerpColor(0xFF120A05, 0xFF2A1810);
        int border = hoverAnim.lerpColor(DiaryTheme.BRONZE_DARK, DiaryTheme.REDCOAT);

        g.fill(x, y, x + w, y + h, bg);
        // v0.235.0: la pestaña oscura tambien lleva el filete a mano.
        PaperTexture.drawWornBorder(g, x, y, w, h, border, x * 31 + y * 17 + w);
        g.fill(x + 3, y + 3, x + w - 3, y + 4, DiaryTheme.withAlpha(DiaryTheme.BRONZE, 48));
        g.fill(x + 3, y + h - 4, x + w - 3, y + h - 3, DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 80));
        g.fill(x + 4, y + 4, x + 5, y + 5, DiaryTheme.withAlpha(DiaryTheme.GOLD_TARNISHED, 120));
        g.fill(x + w - 5, y + 4, x + w - 4, y + 5, DiaryTheme.withAlpha(DiaryTheme.GOLD_TARNISHED, 120));

        int textColor = hoverAnim.lerpColor(DiaryTheme.BONE_WHITE, DiaryTheme.PAPER);
        if (icon == Icon.OPEN_LEDGER) {
            drawOpenLedger(g, x + (w - 12) / 2, y + (h - 10) / 2, textColor);
        } else if (icon == Icon.DISPATCH_NOTE) {
            drawDispatchNote(g, x + (w - 12) / 2, y + (h - 12) / 2, textColor);
        } else if (icon == Icon.FILING) {
            drawFileDrawer(g, x + (w - 12) / 2, y + (h - 12) / 2, textColor);
        } else {
            Font font = Minecraft.getInstance().font;
            // v0.186.0: encoje la letra antes que recortarla; asi la etiqueta
            // entra entera y centrada en cualquier idioma.
            String label = this.getMessage().getString();
            int ty = y + (h - font.lineHeight) / 2;
            int tw = DiaryChrome.widthCached(font, label);
            int room = Math.max(8, w - 10);
            if (tw > room) {
                DiaryTypography.drawFitted(g, font, label, x + w / 2, ty, room, textColor, 0.62f);
            } else {
                DiaryTypography.draw(g, font, label, x + (w - tw) / 2, ty, textColor);
            }
        }

        // Static keyboard focus ring for compact icon buttons; no motion in or out of a world.
        if (this.isFocused() && !this.isMouseOver(mouseX, mouseY) && this.active) {
            int focusGold = 0xFFD0A04A;
            int focusSoft = DiaryTheme.withAlpha(focusGold, 70);
            g.fill(x - 1, y - 1, x + w + 1, y, focusGold);
            g.fill(x - 1, y + h, x + w + 1, y + h + 1, focusGold);
            g.fill(x - 1, y - 1, x, y + h + 1, focusGold);
            g.fill(x + w, y - 1, x + w + 1, y + h + 1, focusGold);
            g.fill(x - 2, y - 2, x + w + 2, y - 1, focusSoft);
            g.fill(x - 2, y + h + 1, x + w + 2, y + h + 2, focusSoft);
        }
    }

    private static void drawDispatchNote(GuiGraphics g, int x, int y, int color) {
        // Folded CEDA note with three short ink lines; fixed pixels, no extra animation.
        g.fill(x + 1, y, x + 9, y + 1, color);
        g.fill(x, y + 1, x + 10, y + 8, color);
        g.fill(x + 2, y + 8, x + 9, y + 10, color);
        g.fill(x + 7, y + 1, x + 9, y + 3, DiaryTheme.REDCOAT);
        int ink = DiaryTheme.INK;
        g.fill(x + 2, y + 3, x + 8, y + 4, ink);
        g.fill(x + 2, y + 5, x + 8, y + 6, ink);
        g.fill(x + 2, y + 7, x + 6, y + 8, ink);
    }

    /** v0.241.0: cajon del gabinete, con su tirador y su etiqueta. */
    private static void drawFileDrawer(GuiGraphics g, int x, int y, int color) {
        g.fill(x + 1, y, x + 11, y + 1, color);
        g.fill(x, y + 1, x + 1, y + 9, color);
        g.fill(x + 11, y + 1, x + 12, y + 9, color);
        g.fill(x + 1, y + 8, x + 11, y + 9, color);
        g.fill(x + 4, y + 5, x + 8, y + 6, color);
        g.fill(x, y + 10, x + 12, y + 11, color);
        g.fill(x + 5, y + 2, x + 7, y + 3, DiaryTheme.INK);
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

    private static String ellipsize(Font font, String text, int maxWidth) {
        if (DiaryChrome.widthCached(font, text) <= maxWidth) return text;
        return DiaryTypography.fitString(font, text, maxWidth);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }
}
