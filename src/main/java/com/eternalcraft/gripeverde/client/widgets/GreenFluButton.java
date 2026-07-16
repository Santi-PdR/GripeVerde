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
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GreenFluButton extends AbstractButton {

    private final Component title;
    private final Component subtitle;
    private final ResourceLocation iconTexture;
    private final Runnable onClick;
    private boolean wasHovered = false;

    // Hover suave (0.0 apagado -> 1.0 encendido)
    private final EasedValue hoverAnim = new EasedValue(6f); // 6.0 = ~170ms

    public GreenFluButton(int x, int y, int w, int h,
                          Component title, Component subtitle,
                          ResourceLocation iconTexture,
                          Runnable onClick) {
        super(x, y, w, h, title);
        this.title = title;
        this.subtitle = subtitle;
        this.iconTexture = iconTexture;
        this.onClick = onClick;
    }

    @Override
    public void onPress() {
        UiSounds.playClick();
        this.onClick.run();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        // Silencia vanilla - UiSounds ya maneja el click
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.isHovered();

        // Sonido solo cuando ENTRA hovered
        if (hovered && !wasHovered) UiSounds.playHover();
        wasHovered = hovered;

        // Actualizar animacion (partialTick esta en ticks 1/20s)
        hoverAnim.setTarget(hovered);
        hoverAnim.tick(partialTick / 20f);
        float t = hoverAnim.getEased();

        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;

        // Fondo: lerp entre oscuro y ligeramente iluminado
        int bgColor = hoverAnim.lerpColor(0xFF15100A, 0xFF2A1810);
        g.fill(x, y, x + w, y + h, bgColor);

        // Borde: lerp entre bronce oscuro y rojo britanico
        int borderColor = hoverAnim.lerpColor(DiaryTheme.BRONZE_DARK, DiaryTheme.REDCOAT);
        g.fill(x, y,         x + w, y + 1,     borderColor);
        g.fill(x, y + h - 1, x + w, y + h,     borderColor);
        g.fill(x, y,         x + 1, y + h,     borderColor);
        g.fill(x + w - 1, y, x + w, y + h,     borderColor);

        // Barra lateral: crece con easing (de 3px de alto minimo a full)
        int barColor = hoverAnim.lerpColor(DiaryTheme.SEPIA_DARK, DiaryTheme.REDCOAT_BRIGHT);
        int barTopReduction = (int) ((1f - t) * ((h - 4) / 2));
        g.fill(x + 2, y + 2 + barTopReduction, x + 5, y + h - 2 - barTopReduction, barColor);

        // Textos: lerp de color
        Font font = Minecraft.getInstance().font;
        int textX = x + 16;
        int titleColor = hoverAnim.lerpColor(DiaryTheme.BONE_WHITE, DiaryTheme.PAPER);
        int subColor   = hoverAnim.lerpColor(DiaryTheme.SEPIA_LIGHT, DiaryTheme.OCHRE);

        g.drawString(font, this.title, textX, y + 8, titleColor, false);
        g.drawString(font, this.subtitle, textX, y + 22, subColor, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }
}