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

        // Track interno
        int trackY1 = y + h / 2 - 1;
        int trackY2 = y + h / 2 + 1;
        g.fill(x + 3, trackY1, x + w - 3, trackY2, 0x60422810);

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
        String msg = this.getMessage().getString();
        int textX = x + w / 2 - Minecraft.getInstance().font.width(msg) / 2;
        int textY = y + (h - 8) / 2;
        g.drawString(Minecraft.getInstance().font, msg, textX + 1, textY + 1, DiaryChrome.SHADOW_COL, false);
        g.drawString(Minecraft.getInstance().font, msg, textX, textY, textColor, false);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput out) {
        out.add(NarratedElementType.TITLE, this.createNarrationMessage());
    }
}