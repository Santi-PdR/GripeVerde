package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/**
 * Slider vertical/horizontal para controlar el volumen de la musica del menu.
 * Persiste en memoria durante la sesion (v0.8.1). Persistencia en disco: futuro.
 */
public class MusicVolumeSlider extends AbstractSliderButton {

    public MusicVolumeSlider(int x, int y, int width, int height) {
        super(x, y, width, height,
            Component.literal("Menu Music: " + Math.round(MenuMusic.getUserVolume() * 100) + "%"),
            MenuMusic.getUserVolume());
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.literal("Menu Music: " + Math.round(this.value * 100) + "%"));
    }

    @Override
    protected void applyValue() {
        MenuMusic.setUserVolume((float) this.value);
    }
}