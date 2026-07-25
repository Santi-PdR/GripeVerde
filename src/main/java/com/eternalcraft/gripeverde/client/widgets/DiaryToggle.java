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

import java.util.function.Function;

/**
 * Toggle booleano estilo diario. Envuelve un OptionInstance<Boolean>
 * y persiste inmediatamente al cambiar.
 *
 * Look:
 *  - Fondo pergamino con highlight/shadow como DiaryButton
 *  - Label izquierda + separador + valor derecha
 *  - Indicador circular relleno (ON) o vacio (OFF) a la derecha del valor
 *  - Hover ilumina y subraya
 *  - Click alterna y reproduce sonido UI
 */
public class DiaryToggle extends AbstractButton {

    private final OptionInstance<Boolean> option;
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
        super(x, y, w, h, baseLabel);
        this.option = option;
        this.baseLabel = baseLabel;
        this.valueFormatter = valueFormatter != null ? valueFormatter : DiaryToggle::defaultFormatter;
        this.onChanged = onChanged;
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

    @Override
    public void onPress() {
        boolean current = this.option.get();
        this.option.set(!current);
        Minecraft.getInstance().options.save();
        if (this.onChanged != null) this.onChanged.run();
    }

    @Override
    public void playDownSound(SoundManager sm) {
        UiSounds.playClick();
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
        hoverAnim.tick(partialTick / 20f);
        float t = hoverAnim.getEased();

        boolean value = this.option.get();

        // -------- SOMBRA EXTERNA --------
        int outerShadow = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 110);
        g.fill(x + 2, y + h, x + w + 1, y + h + 2, outerShadow);
        int outerShadowSoft = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 50);
        g.fill(x + 3, y + h + 1, x + w + 2, y + h + 3, outerShadowSoft);

        // -------- FONDO PERGAMINO --------
        int bgBase = DiaryTheme.PAPER_STAINED;
        int bgHover = 0xFFB09668;
        int bg = hoverAnim.lerpColor(bgBase, bgHover);
        g.fill(x, y, x + w, y + h, bg);

        // Highlight superior
        int topHighlight = DiaryTheme.withAlpha(0x00FFEECC, 90);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, topHighlight);

        // Sombras internas
        int innerShadowTop = DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 70);
        g.fill(x, y, x + w, y + 1, innerShadowTop);
        int innerShadowBot = DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 100);
        g.fill(x, y + h - 2, x + w, y + h - 1, innerShadowBot);

        // -------- BORDE --------
        int accent = value ? DiaryTheme.REDCOAT : DiaryTheme.SEPIA_MID;
        int borderColor = hoverAnim.lerpColor(DiaryTheme.SEPIA_DARK, this.active ? accent : DiaryTheme.SEPIA_MID);
        g.fill(x, y, x + w, y + 1, borderColor);
        g.fill(x, y + h - 1, x + w, y + h, borderColor);
        g.fill(x, y, x + 1, y + h, borderColor);
        g.fill(x + w - 1, y, x + w, y + h, borderColor);

        // -------- CONTENIDO --------
        Font font = Minecraft.getInstance().font;

        Component valLabel = this.valueFormatter.apply(value);
        String labelStr = this.baseLabel.getString();
        String valStr = valLabel.getString();
        String sep = ": ";

        int sepW = font.width(sep);
        int valW = font.width(valStr);
        int indicatorGap = 6;
        int indicatorR = 3;
        int indicatorW = indicatorR * 2 + 1;
        int paddingH = 8; // margen interno horizontal
        int availableW = w - paddingH * 2;

        // Intentar dibujar con indicador
        boolean showIndicator = true;
        int labelW = font.width(labelStr);
        int totalW = labelW + sepW + valW + indicatorGap + indicatorW;

        if (totalW > availableW) {
            // Primer intento: omitir indicador
            showIndicator = false;
            totalW = labelW + sepW + valW;
        }

        if (totalW > availableW) {
            // Segundo intento: truncar el label con font.plainSubstrByWidth
            String ellipsis = "...";
            int elpW = font.width(ellipsis);
            int reservedW = sepW + valW + elpW;
            int maxLabelW = availableW - reservedW;
            if (maxLabelW < 0) maxLabelW = 0;
            String truncated = font.plainSubstrByWidth(labelStr, maxLabelW);
            labelStr = truncated + ellipsis;
            labelW = font.width(labelStr);
            totalW = labelW + sepW + valW;
        }

        int contentY = y + (h - font.lineHeight) / 2;
        int startX = x + (w - totalW) / 2;

        int labelColor = hoverAnim.lerpColor(DiaryTheme.INK, DiaryTheme.INK_BLACK);
        if (!this.active) labelColor = DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 120);

        int valColor;
        if (!this.active) {
            valColor = DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 120);
        } else if (value) {
            valColor = hoverAnim.lerpColor(DiaryTheme.REDCOAT_DARK, DiaryTheme.REDCOAT);
        } else {
            valColor = DiaryTheme.SEPIA_MID;
        }

        // Label
        g.drawString(font, labelStr, startX, contentY, labelColor, false);
        // Separator
        g.drawString(font, sep, startX + labelW, contentY, labelColor, false);
        // Value
        g.drawString(font, valStr, startX + labelW + sepW, contentY, valColor, false);

        // -------- INDICADOR CIRCULAR (solo si cabe) --------
        if (showIndicator) {
            int indX = startX + labelW + sepW + valW + indicatorGap;
            int indY = contentY + font.lineHeight / 2 - 1;
            drawIndicator(g, indX + indicatorR, indY, indicatorR, value, valColor);
        }

        // -------- SUBRAYADO HOVER --------
        if (t > 0.1f && this.active) {
            int underlineY = contentY + font.lineHeight;
            int underlineAlpha = (int)(t * 180);
            int underlineColor = DiaryTheme.withAlpha(accent, underlineAlpha);
            g.fill(startX + labelW + sepW, underlineY, startX + labelW + sepW + valW, underlineY + 1, underlineColor);
        }
    }

    /** Circulo relleno (ON) o solo contorno (OFF). */
    private void drawIndicator(GuiGraphics g, int cx, int cy, int r, boolean filled, int color) {
        for (int dy = -r; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                int d2 = dx * dx + dy * dy;
                if (d2 <= r * r) {
                    boolean edge = d2 >= (r - 1) * (r - 1);
                    if (filled || edge) {
                        g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                    }
                }
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }
}