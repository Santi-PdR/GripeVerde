package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
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

        // Ya no hay boton "Créditos" - esta pantalla ES los créditos
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
        PaperTexture.drawVeil(g, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.OFFICIAL);
        drawOfficialFrame(g);

        int cx = this.width / 2;
        int titleY = panelY + 14;
        String titleStr = DiaryChrome.fitText(this.font, this.title.getString(), Math.max(0, panelW - 32));
        int tw = this.font.width(titleStr);
        g.drawString(this.font, titleStr, cx - tw / 2, titleY, DiaryChrome.INK_TITLE, false);

        String subtitle = "- " + Component.translatable("gripeverde.options.credits.subtitle").getString() + " -";
        subtitle = DiaryChrome.fitText(this.font, subtitle, Math.max(0, panelW - 32));
        int subW = this.font.width(subtitle);
        g.drawString(this.font, subtitle, cx - subW / 2, titleY + 14, DiaryChrome.INK_SUBTITLE, false);

        String flavor = DiaryChrome.fitText(this.font,
            Component.translatable("gripeverde.options.credits.flavor").getString(), Math.max(0, panelW - 32));
        int flavW = this.font.width(flavor);
        g.drawString(this.font, flavor, cx - flavW / 2, titleY + 26, DiaryChrome.INK_FLAVOR, false);

        int divY1 = titleY + 44;
        DiaryChrome.drawInkDividerBetween(g, panelX + 18, panelX + panelW - 18, divY1);

        renderRegistryEntries(g, divY1 + 10);

        int divY2 = panelY + panelH - 40;
        DiaryChrome.drawInkDividerBetween(g, panelX + 18, panelX + panelW - 18, divY2);
        DiaryChrome.drawCornerOrnaments(g, panelX, panelY, panelW, panelH);
        DiaryChrome.drawRegistryTag(g, panelX, panelY, panelW, panelH, "REG. IX");

        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }

        // v0.21.26: sello de cera removido.
        try {
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, panelY - 12);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderRegistryEntries(GuiGraphics g, int startY) {
        int left = panelX + 24;
        int lineH = 11;
        int y = startY;

        // v0.21.25: nombres reales + descripcion que entra en el margen.
        y = drawSection(g, left, y, isEnglish() ? "Studio" : "Estudio", "EternalCraft");
        y += 4;
        y = drawSection(g, left, y, isEnglish() ? "Chronicler" : "Cronista", "S. Pereira");
        y = drawSection(g, left, y, isEnglish() ? "Director" : "Director", "A. Vergil");
        y += 4;
        y = drawSection(g, left, y, isEnglish() ? "City blueprints" : "Planos de la ciudad", isEnglish() ? "Streets, lamps and docks" : "Calles, faroles y muelles");
        y = drawSection(g, left, y, isEnglish() ? "Paper & ink" : "Papel y tinta", isEnglish() ? "Parchment, bindings and wax" : "Pergamino, encuadernaciones y cera");
        y += 8;

        // v0.21.25: version del gabinete visible en creditos.
        String version = "v" + com.eternalcraft.gripeverde.GripeVerdeMod.VERSION;
        int vw = this.font.width(version);
        g.drawString(this.font, version, panelX + (panelW - vw) / 2, y,
            DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 100), false);
        y += 10;

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
        return com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
    }

    @Override public void renderBackground(GuiGraphics g) {}

    @Override public void onClose() { this.minecraft.setScreen(this.lastScreen); }

    // Helper to avoid duplicate init logic
    private static final class PanelHelper {
        static void init(Screen s) {}
    }
}
