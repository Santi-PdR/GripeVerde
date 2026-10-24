package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.util.ClientAccessibility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.locale.Language;
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
    private Language cachedTextLanguage;
    private Component cachedTextMessage;
    private Font cachedTextFont;
    private int cachedTextWidthLimit = -1;
    private String cachedText = "";
    private int cachedTextMeasuredWidth;

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

    private void refreshTextCache(Font font, int maxWidth) {
        Language language = Language.getInstance();
        if (this.cachedTextLanguage != language) {
            this.cachedTextLanguage = language;
            this.updateMessage();
        }

        Component message = this.getMessage();
        if (message == this.cachedTextMessage && font == this.cachedTextFont
                && maxWidth == this.cachedTextWidthLimit) return;

        this.cachedTextMessage = message;
        this.cachedTextFont = font;
        this.cachedTextWidthLimit = maxWidth;
        String text = message.getString();
        this.cachedTextMeasuredWidth = font.width(text);
        if (this.cachedTextMeasuredWidth > maxWidth) {
            String ellipsis = "...";
            int room = Math.max(0, maxWidth - font.width(ellipsis));
            text = font.plainSubstrByWidth(text, room) + ellipsis;
            this.cachedTextMeasuredWidth = font.width(text);
        }
        this.cachedText = text;
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
        boolean hovered = this.isHoveredOrFocused() && this.active;

        if (ClientAccessibility.hasActiveGameSession()) {
            renderGameplayVersion(g, mouseX, mouseY, x, y, w, h, hovered);
            return;
        }

        g.fill(x + 1, y + 1, x + w + 1, y + h + 1, 0x18000000);
        g.fill(x, y, x + w, y + h, 0xFFCDBA90);
        g.fill(x, y, x + w, y + 1, 0x88422810);
        g.fill(x, y + h - 1, x + w, y + h, 0x88422810);
        g.fill(x, y, x + 1, y + h, 0x88422810);
        g.fill(x + w - 1, y, x + w, y + h, 0x88422810);

        int trackX1 = x + 10;
        int trackX2 = x + w - 10;
        int trackY = y + h - 6;
        g.fill(trackX1, trackY, trackX2, trackY + 2, 0x88422810);
        g.fill(trackX1 - 1, trackY - 1, trackX1 + 1, trackY + 3, 0xA06B4A2E);
        g.fill(trackX2 - 1, trackY - 1, trackX2 + 1, trackY + 3, 0xA06B4A2E);

        int graduations = Math.max(4, Math.min(10, w / 34));
        for (int i = 0; i <= graduations; i++) {
            int tx = trackX1 + (trackX2 - trackX1) * i / graduations;
            int hTick = (i % 2 == 0) ? 5 : 3;
            g.fill(tx, trackY - hTick, tx + 1, trackY, 0x70422810);
        }

        int progressX = trackX1 + (int) (this.value * Math.max(1, trackX2 - trackX1));
        g.fill(trackX1, trackY, progressX, trackY + 2, hovered ? 0xC08A6A2D : 0xA06B4A2E);

        int thumbX = progressX - 4;
        int needleTop = y + 4;
        int needleBot = trackY + 4;
        g.fill(thumbX + 3, needleTop, thumbX + 5, needleBot, hovered ? 0xFF8A6A2D : 0xFF6B4A2E);
        g.fill(thumbX + 2, needleTop + 1, thumbX + 6, needleTop + 4, hovered ? 0xFFD4B070 : 0xFFC9B78A);
        DiaryChrome.drawDivisorLozenge(g, thumbX + 4, needleTop + 2);

        int textColor = hovered ? DiaryTheme.INK_BLACK : DiaryTheme.INK;
        Font font = Minecraft.getInstance().font;
        this.refreshTextCache(font, Math.max(8, w - 14));
        int textX = x + w / 2 - this.cachedTextMeasuredWidth / 2;
        g.drawString(font, this.cachedText, textX, y + 4, textColor, false);
        DiaryFocus.drawKeyboardRing(g, x, y, w, h,
            this.active && this.isFocused() && !this.isMouseOver(mouseX, mouseY));
    }

    private void renderGameplayVersion(GuiGraphics g, int mouseX, int mouseY,
                                       int x, int y, int w, int h, boolean hovered) {
        int edge = hovered ? DiaryTheme.BRONZE_DARK : 0x88422810;
        g.fill(x, y, x + w, y + h, 0xFFCDBA90);
        g.fill(x, y, x + w, y + 1, edge);
        g.fill(x, y + h - 1, x + w, y + h, edge);

        int inset = Math.min(10, Math.max(2, w / 4));
        int trackX1 = x + inset;
        int trackX2 = Math.max(trackX1, x + w - inset);
        int trackY = y + h - 6;
        g.fill(trackX1, trackY, trackX2, trackY + 2, 0x88422810);

        int progressX = trackX1 + (int) (this.value * Math.max(1, trackX2 - trackX1));
        int progressColor = hovered ? 0xC08A6A2D : 0xA06B4A2E;
        if (progressX > trackX1) g.fill(trackX1, trackY, progressX, trackY + 2, progressColor);

        int thumbX = Math.max(trackX1, Math.min(trackX2, progressX));
        g.fill(thumbX - 1, y + 4, thumbX + 1, trackY + 4,
            hovered ? 0xFF8A6A2D : 0xFF6B4A2E);
        g.fill(thumbX - 3, y + 4, thumbX + 3, y + 6,
            hovered ? 0xFFD4B070 : 0xFFC9B78A);

        Font font = Minecraft.getInstance().font;
        this.refreshTextCache(font, Math.max(8, w - 14));
        int textColor = hovered ? DiaryTheme.INK_BLACK : DiaryTheme.INK;
        int textX = x + w / 2 - this.cachedTextMeasuredWidth / 2;
        g.drawString(font, this.cachedText, textX, y + 4, textColor, false);
        DiaryFocus.drawKeyboardRing(g, x, y, w, h,
            this.active && this.isFocused() && !this.isMouseOver(mouseX, mouseY));
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput out) {
        out.add(NarratedElementType.TITLE, this.createNarrationMessage());
    }
}
