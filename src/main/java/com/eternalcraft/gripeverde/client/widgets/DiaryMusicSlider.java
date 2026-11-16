package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
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
        String txt = DiaryChrome.tr("gripeverde.options.sound.music_slider")
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
        MenuMusic.setUserVolumeLive((float) this.value);
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();

        // Fondo: banda pergamino oscuro (track del slider)
        g.fill(x, y, x + w, y + h, 0xFFC9B78A);            // PAPER_STAINED
        // v0.236.0: filete a mano, como el resto de los mandos.
        PaperTexture.drawWornBorder(g, x, y, w, h, 0x60422810, x * 31 + y * 17 + w);

        // Track interno (banda mas oscura donde corre el thumb)
        int trackY1 = y + h / 2 - 1;
        int trackY2 = y + h / 2 + 1;
        g.fill(x + 3, trackY1, x + w - 3, trackY2, 0x60422810);
        for (int i = 1; i < 4; i++) {
            int tx = x + 3 + (w - 6) * i / 4;
            g.fill(tx, trackY1 - 2, tx + 1, trackY2 + 2, 0x70422810);
        }

        // Thumb: rombo tinta victoriana
        int thumbW = 8;
        int thumbX = x + (int) (this.value * (w - thumbW));
        int thumbY = y + 2;
        int thumbH = h - 4;

        // Cuerpo del thumb
        g.fill(thumbX, thumbY, thumbX + thumbW, thumbY + thumbH, 0xC0422810);
        // Relieve claro izq
        g.fill(thumbX, thumbY, thumbX + 2, thumbY + thumbH, 0xA0703020);
        // Relieve oscuro der
        g.fill(thumbX + thumbW - 2, thumbY, thumbX + thumbW, thumbY + thumbH, 0x80200E05);
        // Rombo central
        int cx = thumbX + thumbW / 2;
        int cy = thumbY + thumbH / 2;
        DiaryChrome.drawDivisorLozenge(g, cx, cy);

        // Texto centrado con sombra suave
        int textColor = this.isHovered() ? DiaryTheme.INK_BLACK : DiaryTheme.INK;
        String msg = this.getMessage().getString();
        // v0.204.0: el rotulo se media en cada cuadro; el ancho se guarda.
        net.minecraft.client.gui.Font font = Minecraft.getInstance().font;
        int textX = x + w / 2 - DiaryChrome.widthCached(font, msg) / 2;
        int textY = y + (h - 8) / 2;
        g.drawString(font, msg, textX, textY, textColor, false);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput out) {
        out.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, this.createNarrationMessage());
    }
}