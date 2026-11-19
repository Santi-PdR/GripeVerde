package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.DiaryTypography;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
    // v0.358.0: el aviso final (guardar config) va al soltar, no en cada cuadro.
    private final Runnable onRelease;
    // v0.358.0: mientras se arrastra, vanilla ya llama applyValue() por cada
    // cambio; volver a llamarlo desde onDrag guardaba la config dos veces por
    // cuadro y hacia tirones.
    private boolean dragging;
    // v0.246.0: el mando tambien avisa cuando la mano se le acerca.
    private boolean wasHovered = false;

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
        this(x, y, w, h, initialValue, labelFormatter, onValueChanged, applyOnDrag, null);
    }

    public DiarySlider(int x, int y, int w, int h,
                       double initialValue,
                       DoubleFunction<String> labelFormatter,
                       DoubleConsumer onValueChanged,
                       boolean applyOnDrag,
                       Runnable onRelease) {
        super(x, y, w, h,
              Component.literal(labelFormatter.apply(initialValue)),
              initialValue);
        this.labelFormatter = labelFormatter;
        this.onValueChanged = onValueChanged;
        this.applyOnDrag = applyOnDrag;
        this.onRelease = onRelease;
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.literal(labelFormatter.apply(this.value)));
    }

    /**
     * v0.253.0: el mando suena al soltarlo, como los botones. Antes solo
     * avisaba al acercarse y quedaba mudo al elegir el valor.
     */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean released = super.mouseReleased(mouseX, mouseY, button);
        if (released) {
            this.dragging = false;
            // v0.358.0: al soltar se avisa una sola vez, con el valor final.
            this.fireValueChanged();
            this.fireRelease();
            try {
                com.eternalcraft.gripeverde.client.audio.UiSounds.playClick();
            } catch (Throwable ignored) {
            }
        }
        return released;
    }

    private void fireValueChanged() {
        if (this.onValueChanged != null) this.onValueChanged.accept(this.value);
    }

    private void fireRelease() {
        if (this.onRelease != null) {
            try {
                this.onRelease.run();
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * v0.264.0: con el foco encima, las flechas mueven el mando y RePag/AvPag
     * lo mueven el doble. Antes habia que arrastrarlo con el raton y el
     * teclado no servia para nada.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.active && this.isFocused()) {
            double step = 0.05D;
            int dir = 0;
            if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT) {
                dir = -1;
            } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT) {
                dir = 1;
            } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN) {
                dir = -1;
                step = 0.10D;
            } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP) {
                dir = 1;
                step = 0.10D;
            } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_HOME
                    || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_END) {
                // v0.359.0: Inicio y Fin llevan el mando a tope de un golpe.
                this.value = keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_HOME ? 0.0D : 1.0D;
                this.updateMessage();
                this.dragging = false;
                this.applyValue();
                this.fireRelease();
                try {
                    com.eternalcraft.gripeverde.client.audio.UiSounds.playClick();
                } catch (Throwable ignored) {
                }
                return true;
            }
            if (dir != 0) {
                // v0.264.0: AbstractSliderButton no expone setValue en 1.20.1;
                // el campo value es protegido y se remienda a mano.
                this.value = net.minecraft.util.Mth.clamp(this.value + dir * step, 0.0D, 1.0D);
                this.updateMessage();
                this.dragging = false;
                this.applyValue();
                this.fireRelease();
                try {
                    com.eternalcraft.gripeverde.client.audio.UiSounds.playClick();
                } catch (Throwable ignored) {
                }
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void applyValue() {
        // Con applyOnDrag en falso el valor solo se avisa al soltar.
        if (this.dragging && !this.applyOnDrag) return;
        this.fireValueChanged();
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        this.dragging = true;
        // vanilla ya guarda el valor y llama a applyValue() dentro de onDrag.
        super.onDrag(mouseX, mouseY, dragX, dragY);
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();
        boolean hovered = this.isHoveredOrFocused() && this.active;
        if (hovered && !this.wasHovered) {
            com.eternalcraft.gripeverde.client.audio.UiSounds.playHover();
        }
        this.wasHovered = hovered;

        g.fill(x + 1, y + 1, x + w + 1, y + h + 1, 0x18000000);
        g.fill(x, y, x + w, y + h, 0xFFCDBA90);
        // v0.236.0: el deslizador tambien lleva el filete a mano.
        PaperTexture.drawWornBorder(g, x, y, w, h, 0x88422810, x * 31 + y * 17 + w);

        // v0.359.0: en ventanas bajas el mando de Sonido mide 12 o 14 px y la
        // etiqueta caia encima del carril y de sus muescas. Con poco alto el
        // carril se pega al borde, las muescas bajan y la letra sube.
        boolean squat = h < 18;
        int trackX1 = x + 10;
        int trackX2 = x + w - 10;
        int trackY = squat ? y + h - 2 : y + h - 6;
        if (trackX2 - trackX1 < 12) {
            trackX1 = x + 4;
            trackX2 = Math.max(trackX1 + 1, x + w - 4);
        }
        g.fill(trackX1, trackY, trackX2, trackY + 2, 0x88422810);
        g.fill(trackX1 - 1, trackY - 1, trackX1 + 1, trackY + 3, 0xA06B4A2E);
        g.fill(trackX2 - 1, trackY - 1, trackX2 + 1, trackY + 3, 0xA06B4A2E);

        // Con poco alto no quedan muescas: se comian la etiqueta.
        if (!squat) {
            int graduations = Math.max(4, Math.min(10, w / 34));
            for (int i = 0; i <= graduations; i++) {
                int tx = trackX1 + (trackX2 - trackX1) * i / graduations;
                int hTick = (i % 2 == 0) ? 5 : 3;
                g.fill(tx, trackY - hTick, tx + 1, trackY, 0x70422810);
            }
        }

        int progressX = trackX1 + (int) (this.value * Math.max(1, trackX2 - trackX1));
        g.fill(trackX1, trackY, progressX, trackY + 2, hovered ? 0xC08A6A2D : 0xA06B4A2E);

        int thumbX = progressX - 4;
        int needleTop = squat ? y + 1 : y + 4;
        int needleBot = Math.min(y + h - 1, trackY + (squat ? 2 : 4));
        g.fill(thumbX + 3, needleTop, thumbX + 5, needleBot, hovered ? 0xFF8A6A2D : 0xFF6B4A2E);
        g.fill(thumbX + 2, needleTop + 1, thumbX + 6, needleTop + 4, hovered ? 0xFFD4B070 : 0xFFC9B78A);
        DiaryChrome.drawDivisorLozenge(g, thumbX + 4, needleTop + 2);

        int textColor = hovered ? DiaryTheme.INK_BLACK : DiaryTheme.INK;
        Font font = Minecraft.getInstance().font;
        String msg = ellipsize(font, cachedMessage(), Math.max(8, w - 14));
        int textX = x + w / 2 - DiaryChrome.widthCached(font, msg) / 2;
        int labelY = squat ? y + 1 : y + 4;
        // v0.336.0: la aguja del mando cruzaba la etiqueta y se comia una letra
        // ("Granofono del cronista: 4 %"). Ahora, si la aguja cae sobre el
        // texto, la etiqueta se escribe en dos mitades y queda un hueco limpio.
        int needleLeft = thumbX + 1;
        int needleRight = thumbX + 8;
        int textW = DiaryChrome.widthCached(font, msg);
        boolean crosses = needleRight > textX - 2 && needleLeft < textX + textW + 2;
        if (crosses) {
            int pad = 5;
            String left = font.plainSubstrByWidth(msg, Math.max(0, needleLeft - pad - textX));
            if (left.length() > 0) {
                int lw = font.width(left);
                DiaryTypography.draw(g, font, left, textX, labelY, textColor);
                String right = msg.substring(left.length());
                if (!right.isEmpty()) {
                    int rx = Math.max(needleRight + pad, textX + lw + 2);
                    if (rx + font.width(right) <= x + w - 3) {
                        DiaryTypography.draw(g, font, right, rx, labelY, textColor);
                    }
                }
            } else {
                String right = msg;
                int rx = needleRight + pad;
                if (rx + font.width(right) <= x + w - 3) {
                    DiaryTypography.draw(g, font, right, rx, labelY, textColor);
                }
            }
        } else {
            DiaryTypography.draw(g, font, msg, textX, labelY, textColor);
        }
    }

    // v0.194.0: el texto del slider se resuelve solo cuando cambia.
    private Component cachedMsgComponent;
    private String cachedMsgString = "";

    private String cachedMessage() {
        Component message = this.getMessage();
        if (this.cachedMsgComponent == null || !this.cachedMsgComponent.equals(message)) {
            this.cachedMsgComponent = message;
            this.cachedMsgString = message == null ? "" : message.getString();
        }
        return this.cachedMsgString;
    }

    private static String ellipsize(net.minecraft.client.gui.Font font, String text, int maxWidth) {
        if (DiaryChrome.widthCached(font, text) <= maxWidth) return text;
        return DiaryTypography.fitString(font, text, maxWidth);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput out) {
        out.add(NarratedElementType.TITLE, this.createNarrationMessage());
    }
}
