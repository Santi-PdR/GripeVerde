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
        DiaryChrome.drawRegistryTag(g, panelX, panelY, panelW, panelH, "REG. IX");

        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }

        // v0.16.0: el sello que nadie debia romper
        try {
            drawRegistrySeal(g, mouseX, mouseY);
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, panelY - 12);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {}
    }

    private int sealX() { return panelX + panelW - 42; }
    private int sealY() { return panelY + panelH - 70; }

    private void drawRegistrySeal(GuiGraphics g, int mouseX, int mouseY) {
        int x = sealX(), y = sealY();
        boolean broken;
        try {
            broken = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("seal_broken") > 0;
        } catch (Throwable t) { broken = false; }
        boolean hover = mouseX >= x - 9 && mouseX <= x + 9 && mouseY >= y - 9 && mouseY <= y + 9;

        if (!broken) {
            int wax = hover ? 0xFF8B2A1C : 0xFF7A1F18;
            for (int dy = -7; dy <= 7; dy++) {
                int dx = (int)Math.sqrt(Math.max(0, 49 - dy * dy));
                g.fill(x - dx, y + dy, x + dx, y + dy + 1, wax);
            }
            g.fill(x - 3, y - 3, x + 3, y + 3, 0xFF5C1508);
            g.fill(x - 4, y - 5, x - 2, y - 3, 0xFFB85A48);
        } else {
            // Mitades separadas
            for (int dy = -7; dy <= 7; dy++) {
                int dx = (int)Math.sqrt(Math.max(0, 49 - dy * dy));
                g.fill(x - dx - 3, y + dy, x - 1 - 3, y + dy + 1, 0xFF6E1B12);
                g.fill(x + 1 + 3, y + dy, x + dx + 3, y + dy + 1, 0xFF6E1B12);
            }
            // Lo que el sello guardaba
            String hidden = isEnglish()
                ? "The reader, too, shall be remembered."
                : "El lector tambien sera recordado.";
            int hw = this.font.width(hidden);
            g.drawString(this.font, hidden, x - hw / 2, y + 14,
                DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 170), false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= sealX() - 9 && mouseX <= sealX() + 9
                && mouseY >= sealY() - 9 && mouseY <= sealY() + 9) {
            try {
                if (com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("seal_broken") == 0) {
                    com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.setCounter("seal_broken", 1);
                    com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.addCollectible(
                        com.eternalcraft.gripeverde.client.lore.Discoveries.ITEM_SEAL);
                    com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                        com.eternalcraft.gripeverde.client.lore.Discoveries.BROKEN_SEAL);
                    com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                        com.eternalcraft.gripeverde.client.audio.ModSounds.UI_CLICK_DESTROY.get(), 0.24f, 0.66f);
                }
            } catch (Throwable ignored) {}
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
