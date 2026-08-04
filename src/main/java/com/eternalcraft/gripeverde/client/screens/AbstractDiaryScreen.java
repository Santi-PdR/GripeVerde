package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Base professional for all Diary (parchment) screens.
 * Ensures:
 *  - Consistent scene tick and background
 *  - Panel shadow + parchment + corner ornaments
 *  - Title / subtitle / flavor pattern
 *  - Responsive clamping for GUI scales 1-4
 */
public abstract class AbstractDiaryScreen extends Screen {

    protected static final int PANEL_MAX_W = 420;
    protected static final int PANEL_MAX_H = 420;
    protected static final int PANEL_MIN_MARGIN = 24;

    protected int panelX, panelY, panelW, panelH;

    protected AbstractDiaryScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        PaperTexture.incrementOpenCount();
        super.init();
        // Childs should calculate panel size via computePanelBounds()
    }

    protected void computePanelBounds(int desiredW, int desiredH) {
        int maxW = Math.min(desiredW, this.width - PANEL_MIN_MARGIN * 2);
        int maxH = Math.min(desiredH, this.height - PANEL_MIN_MARGIN * 2);
        this.panelW = Math.max(200, maxW);
        this.panelH = Math.max(180, maxH);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;
    }

    @Override
    public void tick() {
        super.tick();
        QuarantineSceneRenderer.tick(1f / 20f);
    }

    /** Render parchment scene behind */
    protected void renderSceneBackground(GuiGraphics g) {
        QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
    }

    /** Render panel shadow + parchment */
    protected void renderPanel(GuiGraphics g) {
        DiaryChrome.drawPanelShadow(g, panelX, panelY, panelW, panelH);
        PaperTexture.drawClean(g, panelX, panelY, panelW, panelH);
        DiaryChrome.drawCornerOrnaments(g, panelX, panelY, panelW, panelH);
    }

    protected void renderPanelHeader(GuiGraphics g, Component titleComp, String subtitle, String flavor) {
        int cx = this.width / 2;
        int titleY = panelY + 12;
        String titleStr = titleComp.getString();
        int tw = this.font.width(titleStr);
        g.drawString(this.font, titleStr, cx - tw / 2, titleY, DiaryChrome.INK_TITLE, false);

        if (subtitle != null && !subtitle.isEmpty()) {
            String subWrapped = "- " + subtitle + " -";
            int subW = this.font.width(subWrapped);
            int subY = titleY + 12;
            g.drawString(this.font, subWrapped, cx - subW / 2, subY, DiaryChrome.INK_SUBTITLE, false);
            int divY = subY + 12;
            DiaryChrome.drawInkDivider(g, panelX + 18, divY, panelW - 36);
            if (flavor != null && !flavor.isEmpty()) {
                int flavW = this.font.width(flavor);
                int flavY = divY + 5;
                g.drawString(this.font, flavor, cx - flavW / 2, flavY, DiaryChrome.INK_FLAVOR, false);
            }
        }
    }

    protected void renderPanelFooterDivider(GuiGraphics g, int y) {
        DiaryChrome.drawInkDivider(g, panelX + 18, y, panelW - 36);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        // Bypass vanilla dirt
    }

    protected int clampButtonWidth(int desired, int max) {
        return Math.min(desired, max);
    }
}
