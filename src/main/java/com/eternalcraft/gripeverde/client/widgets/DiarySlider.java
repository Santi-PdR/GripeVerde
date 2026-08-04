package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

/**
 * DiarySlider generico: slider estilizado pergamino con thumb rombo tinta.
 * Callback y label formateable via constructor.
 *
 * Ejemplo:
 *   new DiarySlider(x, y, w, h, initialValue,
 *       v -> String.format("Volumen: %d%%", (int)(v * 100)),
 *       v -> myConfig.setVolume(v)
 *   );
 */
public class DiarySlider extends AbstractSliderButton {

    private final DoubleFunction<String> labelFormatter;
    private final DoubleConsumer onValueChanged;
    private final boolean applyOnDrag;

    public DiarySlider(int x, int y, int w, int h,
                       double initialValue,
                       DoubleFunction<String> labelFormatter,
                       DoubleConsumer onValueChanged) {
        this(x, y, w, h, initialValue, labelFormatter, onValueChanged, true);
    }

    public DiarySlider(int x, int y, int w, int h,
                       double initialValue,
                       DoubleFunction<String> labelFormatter,
                       DoubleConsumer onValueChanged,
                       boolean applyOnDrag) {
        super(x, y, w, h,
              Component.literal(labelFormatter.apply(initialValue)),
              initialValue);
        this.labelFormatter = labelFormatter;
        this.onValueChanged = onValueChanged;
        this.applyOnDrag = applyOnDrag;
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.literal(labelFormatter.apply(this.value)));
    }

    @Override
    protected void applyValue() {
        if (onValueChanged != null) onValueChanged.accept(this.value);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);
        if (applyOnDrag) this.applyValue();
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();

        // Fondo pergamino
        g.fill(x, y, x + w, y + h, 0xFFC9B78A);
        g.fill(x, y, x + w, y + 1, 0x60422810);
        g.fill(x, y + h - 1, x + w, y + h, 0x60422810);
        g.fill(x, y, x + 1, y + h, 0x60422810);
        g.fill(x + w - 1, y, x + w, y + h, 0x60422810);

        // Track interno + progreso tintado para lectura inmediata.
        int trackY1 = y + h / 2 - 1;
        int trackY2 = y + h / 2 + 1;
        int trackX1 = x + 5;
        int trackX2 = x + w - 5;
        int progressX = trackX1 + (int)(this.value * Math.max(1, trackX2 - trackX1));
        g.fill(trackX1, trackY1, trackX2, trackY2, 0x60422810);
        g.fill(trackX1, trackY1, progressX, trackY2, 0xA06B4A2E);
        g.fill(trackX1 - 1, trackY1 - 1, trackX1 + 1, trackY2 + 1, 0x90422810);
        g.fill(trackX2 - 1, trackY1 - 1, trackX2 + 1, trackY2 + 1, 0x90422810);
        for (int i = 1; i < 4; i++) {
            int tx = trackX1 + (trackX2 - trackX1) * i / 4;
            g.fill(tx, trackY1 - 2, tx + 1, trackY2 + 2, 0x70422810);
        }

        // Thumb
        int thumbW = 8;
        int thumbX = x + (int)(this.value * (w - thumbW));
        int thumbY = y + 2;
        int thumbH = h - 4;

        g.fill(thumbX, thumbY, thumbX + thumbW, thumbY + thumbH, 0xC0422810);
        g.fill(thumbX, thumbY, thumbX + 2, thumbY + thumbH, 0xA0703020);
        g.fill(thumbX + thumbW - 2, thumbY, thumbX + thumbW, thumbY + thumbH, 0x80200E05);
        int cx = thumbX + thumbW / 2;
        int cy = thumbY + thumbH / 2;
        DiaryChrome.drawDivisorLozenge(g, cx, cy);

        // Texto
        int textColor = this.isHovered() ? DiaryTheme.INK_BLACK : DiaryTheme.INK;
        String msg = ellipsize(Minecraft.getInstance().font, this.getMessage().getString(), Math.max(8, w - 12));
        int textX = x + w / 2 - Minecraft.getInstance().font.width(msg) / 2;
        int textY = y + (h - 8) / 2;
        g.drawString(Minecraft.getInstance().font, msg, textX, textY, textColor, false);
    }

    private static String ellipsize(net.minecraft.client.gui.Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        int room = Math.max(0, maxWidth - font.width(ellipsis));
        return font.plainSubstrByWidth(text, room) + ellipsis;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput out) {
        out.add(NarratedElementType.TITLE, this.createNarrationMessage());
    }
}