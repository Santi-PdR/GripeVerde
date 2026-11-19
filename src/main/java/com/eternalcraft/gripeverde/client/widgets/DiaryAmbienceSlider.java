package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.audio.MenuAmbience;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
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
        // v0.267.0: a cero no dice "0%", dice que no se oye.
        if (pct <= 0) {
            String silent = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                ? " (silent)" : " (en silencio)";
            String head = DiaryChrome.tr("gripeverde.options.sound.ambience_slider");
            return Component.literal(head + ":" + silent);
        }
        // v0.23.0: la etiqueta estaba hardcodeada en castellano; la clave
        // traducida ya existia en los 25 locales pero no se utilizaba.
        String txt = DiaryChrome.tr("gripeverde.options.sound.ambience_slider")
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

    /**
     * v0.267.0: el mando tambien se maneja con el teclado. Con el foco encima,
     * las flechas mueven dos puntos y RePag/AvPag diez, aplicando el volumen en
     * el acto para poder oirlo mientras se ajusta.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.active && this.isFocused()) {
            double step = 0.02D;
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
            }
            if (dir != 0) {
                this.value = net.minecraft.util.Mth.clamp(this.value + dir * step, 0.0D, 1.0D);
                this.updateMessage();
                this.applyValue();
                this.livePreview();
                try {
                    com.eternalcraft.gripeverde.client.audio.UiSounds.playClick();
                } catch (Throwable ignored) {
                }
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /** v0.267.0: el aviso de volumen, sin tocar el guardado. */
    private void livePreview() {
        MenuAmbience.setUserVolumeLive((float) this.value);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);
        MenuAmbience.setUserVolumeLive((float) this.value);
    }

    private boolean wasHovered = false;

    /** v0.267.0: el mando avisa al acercarse y golpea al soltarse. */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean released = super.mouseReleased(mouseX, mouseY, button);
        if (released) {
            try {
                com.eternalcraft.gripeverde.client.audio.UiSounds.playClick();
            } catch (Throwable ignored) {
            }
        }
        return released;
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

        g.fill(x, y, x + w, y + h, 0xFFC9B78A);
        // v0.236.0: filete a mano, como el resto de los mandos.
        PaperTexture.drawWornBorder(g, x, y, w, h, 0x60422810, x * 31 + y * 17 + w);

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
        // v0.352.0: el rotulo se mide y, si el mando lo cruza, va en dos mitades.
        net.minecraft.client.gui.Font font = Minecraft.getInstance().font;
        DiaryChrome.drawSliderLabel(g, font, this.getMessage().getString(),
            x, y, w, h, textColor, thumbX, thumbX + thumbW);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput out) {
        out.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, this.createNarrationMessage());
    }
}