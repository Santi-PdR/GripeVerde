package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Slider estilizado pergamino para el volumen de la musica del mod (MenuMusic).
 * Reemplaza al viejo MusicVolumeSlider (que quedo huerfano en v0.11.0).
 * Persiste automaticamente via MenuMusic.setUserVolume() -> ClientConfig.MUSIC_VOLUME.
 *
 * Look: track sepia oscuro sobre pergamino, thumb con relieves de tinta victoriana
 * y rombo central identico al del scroll estilizado (DiaryChrome).
 */
public class DiaryMusicSlider extends AbstractSliderButton {

    public DiaryMusicSlider(int x, int y, int w, int h) {
        super(x, y, w, h, buildLabel(MenuMusic.getUserVolume()), MenuMusic.getUserVolume());
    }

    private static Component buildLabel(double v) {
        int pct = (int) Math.round(v * 100.0);
        String txt = Component.translatable("gripeverde.options.sound.music_slider").getString()
                + ": " + pct + "%";
        return Component.literal(txt);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(buildLabel(this.value));
    }

    @Override
    protected void applyValue() {
        MenuMusic.setUserVolume((float) this.value);
    }

    /**
     * Override para aplicar el volumen tambien durante el arrastre.
     * Vanilla solo llama applyValue() al soltar el mouse (onRelease).
     */
    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);
        this.applyValue();
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();
        boolean hovered = this.isHoveredOrFocused() && this.active;

        g.fill(x + 1, y + 1, x + w + 1, y + h + 1, 0x18000000);
        g.fill(x, y, x + w, y + h, 0xFFD8C7A0);
        g.fill(x, y, x + w, y + 2, 0xA06B4A2E);
        g.fill(x, y + h - 1, x + w, y + h, 0x88422810);
        g.fill(x, y, x + 1, y + h, 0x88422810);
        g.fill(x + w - 1, y, x + w, y + h, 0x88422810);

        int trackX1 = x + 12;
        int trackX2 = x + w - 12;
        int trackY = y + h - 6;
        g.fill(trackX1, trackY, trackX2, trackY + 2, 0x88422810);
        for (int i = 0; i <= 10; i++) {
            int tx = trackX1 + (trackX2 - trackX1) * i / 10;
            int ht = (i % 5 == 0) ? 6 : 3;
            g.fill(tx, trackY - ht, tx + 1, trackY, 0x70422810);
        }
        int progressX = trackX1 + (int)(this.value * Math.max(1, trackX2 - trackX1));
        g.fill(trackX1, trackY, progressX, trackY + 2, hovered ? 0xE0D4941E : 0xC0A8783C);

        int thumbX = progressX - 4;
        g.fill(thumbX + 3, y + 6, thumbX + 5, trackY + 4, 0xFF6B4A2E);
        g.fill(thumbX + 1, y + 4, thumbX + 7, y + 9, hovered ? 0xFFE8DCB4 : 0xFFD8C7A0);
        DiaryChrome.drawDivisorLozenge(g, thumbX + 4, y + 6);

        String label = this.getMessage().getString();
        int tw = Minecraft.getInstance().font.width(label);
        g.drawString(Minecraft.getInstance().font, label, x + (w - tw) / 2, y + 4,
            hovered ? DiaryTheme.INK_BLACK : DiaryTheme.INK, false);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput out) {
        out.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, this.createNarrationMessage());
    }
}