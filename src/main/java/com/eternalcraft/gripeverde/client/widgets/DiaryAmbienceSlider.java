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
        String txt = "Ambiente del Cronista: " + pct + "%";
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
        boolean hovered = this.isHoveredOrFocused() && this.active;

        g.fill(x + 1, y + 1, x + w + 1, y + h + 1, 0x14000000);
        g.fill(x, y, x + w, y + h, 0xFFD2C49F);
        g.fill(x, y, x + w, y + 1, 0x88422810);
        g.fill(x, y + h - 1, x + w, y + h, 0x88422810);
        g.fill(x, y, x + 1, y + h, 0x88422810);
        g.fill(x + w - 1, y, x + w, y + h, 0x88422810);

        int trackX1 = x + 10;
        int trackX2 = x + w - 10;
        int trackY = y + h - 5;
        g.fill(trackX1, trackY, trackX2, trackY + 2, 0x88422810);
        for (int i = 0; i <= 8; i++) {
            int tx = trackX1 + (trackX2 - trackX1) * i / 8;
            int ht = (i % 4 == 0) ? 5 : 3;
            g.fill(tx, trackY - ht, tx + 1, trackY, 0x705A7038);
        }
        int progressX = trackX1 + (int)(this.value * Math.max(1, trackX2 - trackX1));
        g.fill(trackX1, trackY, progressX, trackY + 2, hovered ? 0xD07A9A48 : 0xB05A7038);

        int thumbX = progressX - 4;
        g.fill(thumbX + 3, y + 4, thumbX + 5, trackY + 4, 0xFF3D2E1A);
        g.fill(thumbX + 1, y + 3, thumbX + 7, y + 8, hovered ? 0xFFD8E0B8 : 0xFFCAD6A8);
        DiaryChrome.drawDivisorLozenge(g, thumbX + 4, y + 5);

        int textColor = hovered ? DiaryTheme.INK_BLACK : DiaryTheme.INK;
        String msg = this.getMessage().getString();
        int textX = x + w / 2 - Minecraft.getInstance().font.width(msg) / 2;
        int textY = y + 2;
        g.drawString(Minecraft.getInstance().font, msg, textX, textY, textColor, false);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput out) {
        out.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, this.createNarrationMessage());
    }
}