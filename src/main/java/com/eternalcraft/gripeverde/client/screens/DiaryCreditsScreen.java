package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * DiaryCreditsScreen v2.0 - Official quarantine registry
 * Professional overhaul: looks like sealed official document with ledger, stamps, hierarchy.
 */
public class DiaryCreditsScreen extends Screen {

    private static final int PANEL_W = 360;
    private static final int PANEL_H = 320;

    private final Screen lastScreen;
    private int panelX, panelY, panelW, panelH;
    private final List<CreditRow> creditRows = new ArrayList<>();
    private int bodyTop, bodyBottom, contentHeight, maxScroll;
    private double scroll, targetScroll;
    private boolean compactLayout;
    private Language cachedHeaderLanguage;
    private Font cachedHeaderFont;
    private String cachedSubtitle = "";
    private int cachedSubtitleWidth;
    private String cachedFlavor = "";
    private int cachedFlavorWidth;

    public DiaryCreditsScreen(Screen parent) {
        super(Component.translatable("gripeverde.options.credits.title"));
        this.lastScreen = parent;
    }

    @Override
    protected void init() {
        this.panelW = Math.max(1, Math.min(PANEL_W, this.width - 36));
        this.panelH = Math.max(1, Math.min(PANEL_H, this.height - 36));
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;
        this.compactLayout = this.panelH < 240;
        this.clearWidgets();

        int dividerY = this.panelY + (this.compactLayout ? 34 : 58);
        this.bodyTop = dividerY + 10;
        this.bodyBottom = Math.max(this.bodyTop + 1, this.panelY + this.panelH - 48);
        rebuildCreditRows();
        this.contentHeight = this.creditRows.stream().mapToInt(CreditRow::height).sum();
        this.maxScroll = Math.max(0, this.contentHeight - (this.bodyBottom - this.bodyTop));
        this.targetScroll = Math.max(0.0, Math.min(this.maxScroll, this.targetScroll));
        this.scroll = Math.max(0.0, Math.min(this.maxScroll, this.scroll));

        // Ya no hay boton "Créditos" - esta pantalla ES los créditos
        int backW = Math.max(1, Math.min(130, this.panelW - 24));
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
        double difference = this.targetScroll - this.scroll;
        if (com.eternalcraft.gripeverde.client.util.ClientAccessibility.reducedMotion()
                || Math.abs(difference) < 0.25D) this.scroll = this.targetScroll;
        else this.scroll += difference * 0.42D;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.maxScroll > 0 && mouseX >= this.panelX && mouseX <= this.panelX + this.panelW
                && mouseY >= this.bodyTop && mouseY <= this.bodyBottom) {
            setScrollTarget(this.targetScroll - delta * 24.0D);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.maxScroll > 0) {
            int page = Math.max(20, (this.bodyBottom - this.bodyTop) - 20);
            switch (keyCode) {
                case GLFW.GLFW_KEY_UP -> { setScrollTarget(this.targetScroll - 22); return true; }
                case GLFW.GLFW_KEY_DOWN -> { setScrollTarget(this.targetScroll + 22); return true; }
                case GLFW.GLFW_KEY_PAGE_UP -> { setScrollTarget(this.targetScroll - page); return true; }
                case GLFW.GLFW_KEY_PAGE_DOWN -> { setScrollTarget(this.targetScroll + page); return true; }
                case GLFW.GLFW_KEY_HOME -> { setScrollTarget(0); return true; }
                case GLFW.GLFW_KEY_END -> { setScrollTarget(this.maxScroll); return true; }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void setScrollTarget(double value) {
        this.targetScroll = Math.max(0.0, Math.min(this.maxScroll, value));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        refreshHeaderTextCache();
        QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);

        DiaryChrome.drawPanelShadow(g, panelX, panelY, panelW, panelH);
        PaperTexture.drawClean(g, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.OFFICIAL);
        drawOfficialFrame(g);

        int cx = this.width / 2;
        int titleY = panelY + (this.compactLayout ? 8 : 14);
        com.eternalcraft.gripeverde.client.render.DiaryTypography.drawCentered(
            g, this.font, this.title, cx, titleY, DiaryChrome.INK_TITLE);

        g.drawString(this.font, this.cachedSubtitle, cx - this.cachedSubtitleWidth / 2,
            titleY + 14, DiaryChrome.INK_SUBTITLE, false);

        if (!this.compactLayout) {
            g.drawString(this.font, this.cachedFlavor, cx - this.cachedFlavorWidth / 2,
                titleY + 26, DiaryChrome.INK_FLAVOR, false);
        }

        int divY1 = panelY + (this.compactLayout ? 34 : 58);
        DiaryChrome.drawInkDividerBetween(g, panelX + 18, panelX + panelW - 18, divY1);

        renderRegistryEntries(g);

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

    private void refreshHeaderTextCache() {
        Language language = Language.getInstance();
        Font font = this.font;
        if (language == this.cachedHeaderLanguage && font == this.cachedHeaderFont) return;

        this.cachedHeaderLanguage = language;
        this.cachedHeaderFont = font;
        this.cachedSubtitle = "- " + Component.translatable("gripeverde.options.credits.subtitle").getString() + " -";
        this.cachedSubtitleWidth = font.width(this.cachedSubtitle);
        this.cachedFlavor = Component.translatable("gripeverde.options.credits.flavor").getString();
        this.cachedFlavorWidth = font.width(this.cachedFlavor);
    }

    private void rebuildCreditRows() {
        boolean english = isEnglish();
        this.creditRows.clear();
        addCreditSection(english ? "Studio" : "Estudio", "EternalCraft", 4);
        addCreditSection(english ? "Chronicler" : "Cronista", "S. Pereira", 0);
        addCreditSection(english ? "Director" : "Director", "A. Vergil", 4);
        addCreditSection(english ? "City blueprints" : "Planos de la ciudad",
            english ? "Streets, lamps and docks" : "Calles, faroles y muelles", 0);
        addCreditSection(english ? "Paper & ink" : "Papel y tinta",
            english ? "Parchment, bindings and wax" : "Pergamino, encuadernaciones y cera", 8);
        addCenteredCredit("v" + com.eternalcraft.gripeverde.GripeVerdeMod.VERSION,
            DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 100), 0);
        addCenteredCredit(english ? "Sealed under quarantine act 1897" : "Sellado bajo acta de cuarentena 1897",
            DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 130), 0);
    }

    private void addCreditSection(String label, String value, int gapAfter) {
        int contentW = Math.max(1, this.panelW - 48);
        String labelText = label + ":";
        int valueRoom = contentW - this.font.width(labelText + "  ");
        boolean inline = valueRoom > 0 && this.font.width(value) <= valueRoom;
        int wrapWidth = Math.max(1, inline ? valueRoom : contentW - 8);
        List<FormattedCharSequence> lines = splitLines(value, wrapWidth);
        this.creditRows.add(new CreditRow(labelText, lines, inline, false, 11, gapAfter,
            DiaryTheme.withAlpha(DiaryTheme.INK, 210), DiaryTheme.INK_FADED));
    }

    private void addCenteredCredit(String text, int color, int gapAfter) {
        List<FormattedCharSequence> lines = splitLines(text, Math.max(1, this.panelW - 48));
        this.creditRows.add(new CreditRow(null, lines, false, true, 10, gapAfter, color, color));
    }

    private List<FormattedCharSequence> splitLines(String text, int width) {
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (FormattedCharSequence line : this.font.split(Component.literal(text), width)) {
            lines.add(line);
        }
        if (lines.isEmpty()) lines.add(FormattedCharSequence.EMPTY);
        return lines;
    }

    private void renderRegistryEntries(GuiGraphics g) {
        int left = panelX + 24;
        int y = bodyTop - (int) Math.round(scroll);
        int viewportHeight = Math.max(1, bodyBottom - bodyTop);
        g.enableScissor(panelX + 18, bodyTop, panelX + panelW - 18, bodyBottom);
        for (CreditRow row : this.creditRows) {
            if (y + row.height() >= bodyTop && y < bodyBottom) {
                if (row.centered()) {
                    for (int i = 0; i < row.lines().size(); i++) {
                        g.drawCenteredString(this.font, row.lines().get(i), this.width / 2,
                            y + i * row.lineHeight(), row.valueColor());
                    }
                } else {
                    if (row.inline()) {
                        g.drawString(this.font, row.label(), left, y, row.labelColor(), false);
                        int valueX = left + this.font.width(row.label() + "  ");
                        for (int i = 0; i < row.lines().size(); i++) {
                            g.drawString(this.font, row.lines().get(i), valueX,
                                y + i * row.lineHeight(), row.valueColor(), false);
                        }
                    } else {
                        g.drawString(this.font, row.label(), left, y, row.labelColor(), false);
                        for (int i = 0; i < row.lines().size(); i++) {
                            g.drawString(this.font, row.lines().get(i), left + 8,
                                y + row.lineHeight() + i * row.lineHeight(), row.valueColor(), false);
                        }
                    }
                }
            }
            y += row.height();
        }
        g.disableScissor();

        if (this.maxScroll > 0) {
            int trackX = this.panelX + this.panelW - 10;
            g.fill(trackX, bodyTop, trackX + 2, bodyBottom, DiaryTheme.withAlpha(DiaryTheme.INK, 35));
            int thumbH = Math.max(10, viewportHeight * viewportHeight / Math.max(viewportHeight, this.contentHeight));
            int thumbTravel = Math.max(0, viewportHeight - thumbH);
            int thumbY = bodyTop + (int) Math.round(thumbTravel * (this.scroll / this.maxScroll));
            g.fill(trackX, thumbY, trackX + 2, thumbY + thumbH,
                DiaryTheme.withAlpha(DiaryTheme.GOLD_TARNISHED, 190));
        }
    }

    private record CreditRow(String label, List<FormattedCharSequence> lines, boolean inline, boolean centered,
                             int lineHeight, int gapAfter, int labelColor, int valueColor) {
        private int height() {
            int textLines = this.centered ? this.lines.size()
                : this.lines.size() + (this.inline ? 0 : 1);
            return textLines * this.lineHeight + this.gapAfter;
        }
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

}
