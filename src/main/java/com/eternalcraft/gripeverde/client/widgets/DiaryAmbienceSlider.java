package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.audio.MenuAmbience;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Slider estilizado pergamino para el volumen de los sonidos ambientales.
 * Reemplaza al boton AMB del Title. Persiste via ClientConfig.AMBIENCE_VOLUME.
 */
public class DiaryAmbienceSlider extends AbstractSliderButton {

    public DiaryAmbienceSlider(int x, int y, int w, int h) {
        super(x, y, w, h, buildLabel(MenuAmbience.getUserVolume()), MenuAmbience.getUserVolume());
    }

    private static Component buildLabel(double v) {
        int pct = (int) Math.round(v * 100.0);
        // v0.23.0: la etiqueta estaba hardcodeada en castellano; la clave
        // traducida ya existia en los 25 locales pero no se utilizaba.
        String txt = Component.translatable("gripeverde.options.sound.ambience_slider").getString()
                + ": " + pct + "%";
        return Component.literal(txt);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(buildLabel(this.value));
    }

    @Override
    protected void applyValue() {
        MenuAmbience.setUserVolume((float) this.value);
    }

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

        g.fill(x, y, x + w, y + h, 0xFFC9B78A);
        g.fill(x, y, x + w, y + 1, 0x60422810);
        g.fill(x, y + h - 1, x + w, y + h, 0x60422810);
        g.fill(x, y, x + 1, y + h, 0x60422810);
        g.fill(x + w - 1, y, x + w, y + h, 0x60422810);

        int trackY1 = y + h / 2 - 1;
        int trackY2 = y + h / 2 + 1;
        g.fill(x + 3, trackY1, x + w - 3, trackY2, 0x60422810);
        for (int i = 1; i < 4; i++) {
            int tx = x + 3 + (w - 6) * i / 4;
            g.fill(tx, trackY1 - 2, tx + 1, trackY2 + 2, 0x70422810);
        }

        int thumbW = 8;
        int thumbX = x + (int) (this.value * (w - thumbW));
        int thumbY = y + 2;
        int thumbH = h - 4;

        g.fill(thumbX, thumbY, thumbX + thumbW, thumbY + thumbH, 0xC0422810);
        g.fill(thumbX, thumbY, thumbX + 2, thumbY + thumbH, 0xA0703020);
        g.fill(thumbX + thumbW - 2, thumbY, thumbX + thumbW, thumbY + thumbH, 0x80200E05);
        int cx = thumbX + thumbW / 2;
        int cy = thumbY + thumbH / 2;
        DiaryChrome.drawDivisorLozenge(g, cx, cy);

        int textColor = this.isHovered() ? DiaryTheme.INK_BLACK : DiaryTheme.INK;
        String msg = this.getMessage().getString();
        int textX = x + w / 2 - Minecraft.getInstance().font.width(msg) / 2;
        int textY = y + (h - 8) / 2;
        g.drawString(Minecraft.getInstance().font, msg, textX, textY, textColor, false);
        DiaryFocus.drawKeyboardRing(g, x, y, w, h,
            this.active && this.isFocused() && !this.isMouseOver(mouseX, mouseY));
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput out) {
        out.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, this.createNarrationMessage());
    }
}
