package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.network.chat.Component;

/**
 * DiaryCreditsScreen v2.0 - Official quarantine registry
 * Professional overhaul: looks like sealed official document with ledger, stamps, hierarchy.
 */
public class DiaryCreditsScreen extends Screen {

    private static final int PANEL_W = 360;
    private static final int PANEL_H = 320;

    private final Screen lastScreen;
    private int panelX, panelY, panelW, panelH;

    public DiaryCreditsScreen(Screen parent) {
        super(Component.translatable("gripeverde.options.credits.title"));
        this.lastScreen = parent;
    }

    @Override
    protected void init() {
        PanelHelper.init(this);
        this.panelW = Math.min(PANEL_W, this.width - 36);
        this.panelH = Math.min(PANEL_H, this.height - 36);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;
        this.clearWidgets();

        int btnW = 220;
        int btnH = 22;
        int btnX = (this.width - btnW) / 2;
        int btnY0 = this.panelY + 92;

        addRenderableWidget(new DiaryButton(
            btnX, btnY0, btnW, btnH,
            Component.translatable("credits_and_attribution.button.credits"),
            DiaryButton.Icon.SCROLL, DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.OPTION_OPEN,
            () -> this.minecraft.setScreen(new WinScreen(false, () -> this.minecraft.setScreen(this)))
        ));

        // Official registry sections as non-interactive labels rendered in render()
        // Second row: Attributions as info only (vanilla licenses screen is separate)
        // We keep only one actionable button + back to respect vanilla transparency.

        int backW = 130;
        int backH = 22;
        int backY = this.panelY + this.panelH - 32;
        addRenderableWidget(new DiaryButton(
            (this.width - backW) / 2, backY, backW, backH,
            Component.translatable("gripeverde.options.button.done"),
            DiaryButton.Icon.DOOR, DiaryButton.Variant.PRIMARY, DiaryButton.SoundProfile.BACK,
            this::onClose
        ));
    }

    @Override
    public void tick() {
        super.tick();
        QuarantineSceneRenderer.tick(1f / 20f);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);

        DiaryChrome.drawPanelShadow(g, panelX, panelY, panelW, panelH);
        PaperTexture.drawClean(g, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.OFFICIAL);
        drawOfficialFrame(g);

        int cx = this.width / 2;
        int titleY = panelY + 14;
        String titleStr = this.title.getString();
        int tw = this.font.width(titleStr);
        g.drawString(this.font, titleStr, cx - tw / 2, titleY, DiaryChrome.INK_TITLE, false);

        String subtitle = "- " + Component.translatable("gripeverde.options.credits.subtitle").getString() + " -";
        int subW = this.font.width(subtitle);
        g.drawString(this.font, subtitle, cx - subW / 2, titleY + 14, DiaryChrome.INK_SUBTITLE, false);

        String flavor = Component.translatable("gripeverde.options.credits.flavor").getString();
        int flavW = this.font.width(flavor);
        g.drawString(this.font, flavor, cx - flavW / 2, titleY + 26, DiaryChrome.INK_FLAVOR, false);

        int divY1 = titleY + 44;
        DiaryChrome.drawInkDividerBetween(g, panelX + 18, panelX + panelW - 18, divY1);

        renderRegistryEntries(g, divY1 + 10);

        int divY2 = panelY + panelH - 40;
        DiaryChrome.drawInkDividerBetween(g, panelX + 18, panelX + panelW - 18, divY2);
        DiaryChrome.drawCornerOrnaments(g, panelX, panelY, panelW, panelH);

        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }
    }

    private void renderRegistryEntries(GuiGraphics g, int startY) {
        int left = panelX + 24;
        int lineH = 11;
        int y = startY;

        // Section helper
        y = drawSection(g, left, y, isEnglish() ? "ESTUDIO" : "ESTUDIO", isEnglish() ? "EternalCraft Studio" : "Estudio EternalCraft");
        y += 4;
        y = drawSection(g, left, y, isEnglish() ? "CHRONICLER" : "CRONISTA", "S. Pereira");
        y = drawSection(g, left, y, isEnglish() ? "DIRECTOR" : "DIRECTOR", "Santi PdR / Alex");
        y += 4;
        y = drawSection(g, left, y, isEnglish() ? "CITY BLUEPRINTS" : "PLANOS DE LA CIUDAD", isEnglish() ? "Victorian skyline, gas lamps, quarantine seals" : "Horizonte victoriano, faroles de gas, sellos");
        y = drawSection(g, left, y, isEnglish() ? "PAPER & INK" : "PAPEL Y TINTA", isEnglish() ? "Parchment panels, ledger bindings, wax" : "Paneles pergamino, encuadernaciones, cera");
        y += 8;

        // Official note
        String note = isEnglish() ? "Sealed under quarantine act 1897" : "Sellado bajo acta de cuarentena 1897";
        int nw = this.font.width(note);
        g.drawString(this.font, note, panelX + (panelW - nw) / 2, y, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 130), false);
    }

    private int drawSection(GuiGraphics g, int x, int y, String label, String value) {
        String line = label + ":  " + value;
        g.drawString(this.font, label + ":", x, y, DiaryTheme.withAlpha(DiaryTheme.INK, 210), false);
        int labelW = this.font.width(label + ":  ");
        g.drawString(this.font, value, x + labelW, y, DiaryTheme.INK_FADED, false);
        return y + 11;
    }

    private void drawOfficialFrame(GuiGraphics g) {
        int border = DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 110);
        g.fill(panelX, panelY, panelX + panelW, panelY + 1, border);
        g.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, border);
        g.fill(panelX, panelY, panelX + 1, panelY + panelH, border);
        g.fill(panelX + panelW - 1, panelY, panelX + panelW, panelY + panelH, border);
        // Inner rivets
        int rivet = DiaryTheme.withAlpha(DiaryTheme.GOLD_TARNISHED, 90);
        g.fill(panelX + 8, panelY + 8, panelX + 10, panelY + 10, rivet);
        g.fill(panelX + panelW - 10, panelY + 8, panelX + panelW - 8, panelY + 10, rivet);
        g.fill(panelX + 8, panelY + panelH - 10, panelX + 10, panelY + panelH - 8, rivet);
        g.fill(panelX + panelW - 10, panelY + panelH - 10, panelX + panelW - 8, panelY + panelH - 8, rivet);
    }

    private boolean isEnglish() {
        try {
            String lang = net.minecraft.client.Minecraft.getInstance().getLanguageManager().getSelected();
            return lang == null || lang.startsWith("en_");
        } catch (Throwable ignored) { return true; }
    }

    @Override public void renderBackground(GuiGraphics g) {}

    @Override public void onClose() { this.minecraft.setScreen(this.lastScreen); }

    // Helper to avoid duplicate init logic
    private static final class PanelHelper {
        static void init(Screen s) {}
    }
}
