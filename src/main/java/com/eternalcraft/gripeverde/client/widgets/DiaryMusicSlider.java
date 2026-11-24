package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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
public class DiaryMusicSlider extends DiaryPointerSlider {

    public DiaryMusicSlider(int x, int y, int w, int h) {
        super(x, y, w, h, buildLabel(MenuMusic.getUserVolume()), MenuMusic.getUserVolume());
    }

    private static Component buildLabel(double v) {
        int pct = (int) Math.round(v * 100.0);
        // v0.267.0: a cero no dice "0%", dice que no se oye.
        if (pct <= 0) {
            String silent = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                ? " (silent)" : " (en silencio)";
            String head = DiaryChrome.tr("gripeverde.options.sound.music_slider");
            return Component.literal(head + ":" + silent);
        }
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
        // v0.358.0: durante el arrastre solo se escucha; guardar en cada cuadro
        // escribia el JSON de config decenas de veces por segundo y daba tirones.
        MenuMusic.setUserVolumeLive((float) this.value);
    }

    /**
     * v0.267.0: el mando tambien se maneja con el teclado. Con el foco encima,
     * las flechas mueven dos puntos y RePag/AvPag diez, aplicando el volumen en
     * el acto para poder oirlo mientras se ajusta.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.active || !this.visible || !this.isFocused()) {
            this.cancelPointerGesture();
            return false;
        }
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
                if (!this.updateKeyboardValue(
                        keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_HOME ? 0.0D : 1.0D)) return true;
                this.applyValue();
                // Salto discreto: se persiste al acto, como las flechas.
                MenuMusic.setUserVolume((float) this.value);
                return true;
            }
            if (dir != 0) {
                if (!this.updateKeyboardValue(this.value + dir * step)) return true;
                this.applyValue();
                // v0.358.0: con el teclado el valor es discreto, se guarda al acto.
                MenuMusic.setUserVolume((float) this.value);
                try {
                    com.eternalcraft.gripeverde.client.audio.UiSounds.playClick();
                } catch (Throwable ignored) {
                }
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean wasHovered = false;

    /** v0.267.0: el mando avisa al acercarse y golpea al soltarse. */
    @Override
    protected void commitPointerValue() {
        // Persistir solo al finalizar una pulsacion propia, no al recibir
        // una liberacion ajena o repetida del contenedor.
        MenuMusic.setUserVolume((float) this.value);
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.playClick();
        } catch (Throwable ignored) {
        }
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

        // v0.364.0: la ficha lleva la etiqueta ARRIBA y el carril ABAJO, con un
        // mando chico que corre por el carril. Antes el mando era una barra de
        // todo el alto que cruzaba la letra y la partia en dos mitades.
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
        Font font = Minecraft.getInstance().font;
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