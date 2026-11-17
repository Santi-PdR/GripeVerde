package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.PaperButtons;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.gui.ModListScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * v0.244.0: la lista de mods, leida en la casa.
 *
 * Forge trae su propia pantalla de mods -lista oscura, botones de siempre-. No
 * se reescribe: se le cambia el fondo por la escena de la noche, se le visten
 * los botones de papel y se les pone una hoja debajo para que no queden
 * colgando sobre la calle. Todo lo demas -busqueda, configuracion, enlaces- es
 * el de Forge, intacto.
 */
public class DiaryModListScreen extends ModListScreen {

    private final List<DiaryButton> dressed = new ArrayList<>();

    public DiaryModListScreen(Screen parent) {
        super(parent);
    }

    // Forge ensancha init() a public en ModListScreen: el override tiene que
    // mantener ese acceso o el compilador lo rechaza.
    @Override
    public void init() {
        super.init();
        this.dressed.clear();
        this.dressed.addAll(PaperButtons.dressAll(this));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        try {
            QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
        } catch (Throwable ignored) {
        }
        drawFooterPaper(g);
        super.render(g, mouseX, mouseY, partial);
    }

    /** Hoja bajo el grupo de botones, calculada del alto real que les dio Forge. */
    private void drawFooterPaper(GuiGraphics g) {
        if (this.dressed.isEmpty()) return;
        int left = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int top = Integer.MAX_VALUE;
        int bottom = Integer.MIN_VALUE;
        for (DiaryButton button : this.dressed) {
            left = Math.min(left, button.getX());
            right = Math.max(right, button.getX() + button.getWidth());
            top = Math.min(top, button.getY());
            bottom = Math.max(bottom, button.getY() + button.getHeight());
        }
        if (right <= left || bottom <= top) return;
        int x = Math.max(-2, left - 10);
        int y = Math.max(0, top - 8);
        int w = Math.min(this.width + 2, right + 10) - x;
        int h = Math.min(this.height + 6, bottom + 10) - y;
        if (w < 24 || h < 16) return;
        PaperTexture.drawTornSheet(g, x, y, w, h, PaperTexture.PaperKind.OFFICIAL);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        // La escena de la casa hace de fondo.
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
