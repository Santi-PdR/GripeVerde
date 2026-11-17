package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperButtons;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.stats.StatsCounter;

import java.util.ArrayList;
import java.util.List;

/**
 * v0.247.0: el libro de cuentas, leido en la casa.
 *
 * Igual que el registro de avances: escena de noche detras, cabecera y pie de
 * papel, botones vestidos. La lista de estadisticas, sus pestanas y su busqueda
 * siguen siendo las del juego.
 */
public class DiaryStatsScreen extends StatsScreen {

    private static final int HEADER_H = 40;

    private final List<DiaryButton> dressed = new ArrayList<>();

    public DiaryStatsScreen(Screen parent, StatsCounter stats) {
        super(parent, stats);
    }

    @Override
    protected void init() {
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
        drawHeader(g);
        drawFooterPaper(g);
        super.render(g, mouseX, mouseY, partial);
    }

    private void drawHeader(GuiGraphics g) {
        int w = this.width;
        PaperTexture.drawTornSheet(g, -2, -6, w + 4, HEADER_H, PaperTexture.PaperKind.OFFICIAL);
        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        String title = en ? "THE LEDGER" : "EL LIBRO DE CUENTAS";
        String sub = en ? "- what the night adds up to -" : "- lo que suma la noche -";
        int cx = w / 2;
        g.drawString(this.font, title, cx - DiaryChrome.widthCached(this.font, title) / 2, 6,
            DiaryChrome.INK_TITLE, false);
        g.drawString(this.font, sub, cx - DiaryChrome.widthCached(this.font, sub) / 2, 18,
            DiaryChrome.INK_SUBTITLE, false);
        DiaryChrome.drawRegistryTagLeft(g, 0, -4, "REG. XII");
    }

    private void drawFooterPaper(GuiGraphics g) {
        if (this.dressed.isEmpty()) return;
        int left = Integer.MAX_VALUE, right = Integer.MIN_VALUE;
        int top = Integer.MAX_VALUE, bottom = Integer.MIN_VALUE;
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
        return Minecraft.getInstance().level != null;
    }
}
