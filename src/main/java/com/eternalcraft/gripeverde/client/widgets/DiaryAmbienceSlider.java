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
        // v0.358.0: durante el arrastre solo se escucha; el guardado va al soltar.
        MenuAmbience.setUserVolumeLive((float) this.value);
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
            } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_HOME
                    || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_END) {
                // v0.359.0: Inicio y Fin llevan el mando a tope de un golpe.
                this.value = keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_HOME ? 0.0D : 1.0D;
                this.updateMessage();
                this.applyValue();
                // Salto discreto: se persiste al acto, como las flechas.
                MenuAmbience.setUserVolume((float) this.value);
                return true;
            }
            if (dir != 0) {
                this.value = net.minecraft.util.Mth.clamp(this.value + dir * step, 0.0D, 1.0D);
                this.updateMessage();
                this.applyValue();
                // v0.358.0: con el teclado el valor es discreto, se guarda al acto.
                MenuAmbience.setUserVolume((float) this.value);
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
            // v0.358.0: el valor final se persiste una sola vez, al soltar.
            MenuAmbience.setUserVolume((float) this.value);
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

        // v0.364.0: igual que el mando de la musica: etiqueta arriba, carril
        // abajo y mando chico. Era la unica forma de que la letra no se parta.
        g.fill(x, y, x + w, y + h, 0xFFC9B78A);
        PaperTexture.drawWornBorder(g, x, y, w, h, 0x60422810, x * 31 + y * 17 + w);

        int labelY = y + 2;
        int trackY = y + h - 6;
        int trackX1 = x + 6;
        int trackX2 = x + w - 6;
        g.fill(trackX1, trackY, trackX2, trackY + 3, 0x70422810);
        g.fill(trackX1, trackY, trackX2, trackY + 1, 0x39422810);
        for (int i = 0; i <= 4; i++) {
            int tx = trackX1 + (trackX2 - trackX1) * i / 4;
            int tick = (i % 2 == 0) ? 3 : 2;
            g.fill(tx, trackY - tick, tx + 1, trackY, 0x50422810);
        }

        int thumbW = 6;
        int thumbX = x + (int) (this.value * Math.max(1, w - thumbW));
        int thumbTop = trackY - 3;
        int thumbBot = trackY + 6;
        g.fill(thumbX, thumbTop, thumbX + thumbW, thumbBot, hovered ? 0xFF6B3A18 : 0xFF4A2810);
        g.fill(thumbX, thumbTop, thumbX + 1, thumbBot, 0xA0804A22);
        g.fill(thumbX + thumbW - 1, thumbTop, thumbX + thumbW, thumbBot, 0x80200E05);

        int textColor = hovered ? DiaryTheme.INK_BLACK : DiaryTheme.INK;
        net.minecraft.client.gui.Font font = Minecraft.getInstance().font;
        String msg = DiaryChrome.fitCached(font, this.getMessage().getString(), Math.max(8, w - 16));
        int textW = DiaryChrome.widthCached(font, msg);
        int textX = x + (w - textW) / 2;
        g.drawString(font, msg, Math.max(x + 4, textX), labelY, textColor, false);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput out) {
        out.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, this.createNarrationMessage());
    }
}