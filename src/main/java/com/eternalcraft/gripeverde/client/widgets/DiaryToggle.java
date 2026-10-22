package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.util.EasedValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Toggle booleano estilo diario. Admite opciones vanilla y valores externos
 * persistentes, y anuncia etiqueta + estado al narrador.
 *
 * Look:
 *  - Fondo pergamino con highlight/shadow como DiaryButton
 *  - Label izquierda + separador + valor derecha
 *  - Indicador circular relleno (ON) o vacio (OFF) a la derecha del valor
 *  - Hover ilumina y subraya
 *  - Click alterna y reproduce sonido UI
 */
public class DiaryToggle extends AbstractButton {

    private final Supplier<Boolean> valueGetter;
    private final Consumer<Boolean> valueSetter;
    private final Component baseLabel;
    private final Function<Boolean, Component> valueFormatter;
    private final Runnable onChanged; // opcional, se invoca despues del set()

    private final EasedValue hoverAnim = new EasedValue(6f);
    private boolean wasHovered = false;

    public DiaryToggle(int x, int y, int w, int h,
                       OptionInstance<Boolean> option,
                       Component baseLabel,
                       Function<Boolean, Component> valueFormatter,
                       Runnable onChanged) {
        this(x, y, w, h,
            option::get,
            value -> {
                option.set(value);
                Minecraft.getInstance().options.save();
            },
            baseLabel, valueFormatter, onChanged);
    }

    /** Constructor para valores booleanos que no pertenecen a Options de Minecraft. */
    public DiaryToggle(int x, int y, int w, int h,
                       Supplier<Boolean> valueGetter,
                       Consumer<Boolean> valueSetter,
                       Component baseLabel,
                       Function<Boolean, Component> valueFormatter,
                       Runnable onChanged) {
        super(x, y, w, h, baseLabel);
        this.valueGetter = valueGetter;
        this.valueSetter = valueSetter;
        this.baseLabel = baseLabel;
        this.valueFormatter = valueFormatter != null ? valueFormatter : DiaryToggle::defaultFormatter;
        this.onChanged = onChanged;
        updateNarrationMessage();
    }

    /** Constructor simple: usa el formatter por defecto (On/Off traducidos). */
    public DiaryToggle(int x, int y, int w, int h,
                       OptionInstance<Boolean> option,
                       Component baseLabel) {
        this(x, y, w, h, option, baseLabel, DiaryToggle::defaultFormatter, null);
    }

    private static Component defaultFormatter(Boolean v) {
        return Component.translatable(v ? "options.on" : "options.off");
    }

    private void updateNarrationMessage() {
        this.setMessage(Component.empty()
            .append(this.baseLabel)
            .append(": ")
            .append(this.valueFormatter.apply(this.valueGetter.get())));
    }

    @Override
    public void onPress() {
        boolean current = this.valueGetter.get();
        this.valueSetter.accept(!current);
        updateNarrationMessage();
        if (this.onChanged != null) this.onChanged.run();
    }

    @Override
    public void playDownSound(SoundManager sm) {
        UiSounds.toggle();
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;

        boolean hovered = this.isHoveredOrFocused() && this.active;
        if (hovered && !wasHovered) UiSounds.playHover();
        wasHovered = hovered;

        hoverAnim.setTarget(hovered);
        hoverAnim.tickRealtime();
        float t = hoverAnim.getEased();
        boolean value = this.valueGetter.get();

        int outerShadow = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 96);
        g.fill(x + 2, y + h, x + w + 1, y + h + 2, outerShadow);
        g.fill(x + 3, y + h + 1, x + w + 2, y + h + 3, DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 42));

        int bgBase = DiaryTheme.PAPER_STAINED;
        int bgHover = 0xFFB09668;
        int bg = hoverAnim.lerpColor(bgBase, bgHover);
        g.fill(x, y, x + w, y + h, bg);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, DiaryTheme.withAlpha(0x00FFEECC, 90));
        g.fill(x, y, x + w, y + 1, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 70));
        g.fill(x, y + h - 2, x + w, y + h - 1, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 100));

        int accent = value ? DiaryTheme.REDCOAT : DiaryTheme.SEPIA_MID;
        int borderColor = hoverAnim.lerpColor(DiaryTheme.SEPIA_DARK, this.active ? accent : DiaryTheme.SEPIA_MID);
        g.fill(x, y, x + w, y + 1, borderColor);
        g.fill(x, y + h - 1, x + w, y + h, borderColor);
        g.fill(x, y, x + 1, y + h, borderColor);
        g.fill(x + w - 1, y, x + w, y + h, borderColor);

        Font font = Minecraft.getInstance().font;
        Component valLabel = this.valueFormatter.apply(value);
        String labelStr = this.baseLabel.getString();
        String valStr = valLabel.getString();
        int plateW = 66;
        int paddingH = 10;
        int maxLabelW = Math.max(24, w - plateW - paddingH * 2 - 8);
        if (font.width(labelStr) > maxLabelW) {
            String ellipsis = "...";
            int room = Math.max(0, maxLabelW - font.width(ellipsis));
            labelStr = font.plainSubstrByWidth(labelStr, room) + ellipsis;
        }

        int contentY = y + (h - font.lineHeight) / 2;
        int labelColor = hoverAnim.lerpColor(DiaryTheme.INK, DiaryTheme.INK_BLACK);
        if (!this.active) labelColor = DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 120);
        g.drawString(font, labelStr, x + paddingH, contentY, labelColor, false);

        // placa mecanica a la derecha
        int plateX = x + w - plateW - 8;
        int plateY = y + 4;
        int plateH = h - 8;
        g.fill(plateX, plateY, plateX + plateW, plateY + plateH, 0xFFE3D5B2);
        g.fill(plateX, plateY, plateX + plateW, plateY + 1, DiaryTheme.withAlpha(DiaryTheme.BRONZE, 130));
        g.fill(plateX, plateY + plateH - 1, plateX + plateW, plateY + plateH, DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 150));
        g.fill(plateX, plateY, plateX + 1, plateY + plateH, DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 120));
        g.fill(plateX + plateW - 1, plateY, plateX + plateW, plateY + plateH, DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 120));
        g.fill(plateX + 5, plateY + 4, plateX + plateW - 18, plateY + 5, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 40));
        g.fill(plateX + 5, plateY + plateH - 5, plateX + plateW - 18, plateY + plateH - 4, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 30));

        int slotX = plateX + plateW - 16;
        int slotY = plateY + 3;
        int slotH = plateH - 6;
        g.fill(slotX, slotY, slotX + 8, slotY + slotH, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 120));
        if (value) {
            g.fill(slotX + 1, slotY + 1, slotX + 7, slotY + slotH / 2 + 1, DiaryTheme.REDCOAT);
            g.fill(slotX + 2, slotY + 2, slotX + 6, slotY + slotH / 2, DiaryTheme.withAlpha(0x00FFCCB0, 80));
        } else {
            g.fill(slotX + 1, slotY + slotH / 2, slotX + 7, slotY + slotH - 1, DiaryTheme.SEPIA_MID);
        }

        int valColor = !this.active
            ? DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 120)
            : (value ? hoverAnim.lerpColor(DiaryTheme.REDCOAT_DARK, DiaryTheme.REDCOAT) : DiaryTheme.SEPIA_MID);
        int valW = font.width(valStr);
        g.drawString(font, valStr, plateX + Math.max(5, (plateW - 16 - valW) / 2), contentY, valColor, false);

        if (t > 0.1f && this.active) {
            int underlineY = contentY + font.lineHeight;
            int underlineAlpha = (int) (t * 180);
            g.fill(x + paddingH, underlineY, x + paddingH + Math.min(maxLabelW, font.width(labelStr)), underlineY + 1,
                DiaryTheme.withAlpha(accent, underlineAlpha));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }
}
