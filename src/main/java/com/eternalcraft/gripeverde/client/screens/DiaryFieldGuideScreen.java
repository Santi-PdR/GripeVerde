package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** Manual de campo: protocolo cooperativo de la cuarentena. */
public final class DiaryFieldGuideScreen extends Screen {

    private static final String[][] SECTION_KEYS = {
        { "gripeverde.field_guide.section.infection" },
        { "gripeverde.field_guide.section.team" },
        { "gripeverde.field_guide.section.downed" },
        { "gripeverde.field_guide.section.expedition" },
        { "gripeverde.field_guide.section.regroup" }
    };

    private static final String[][] ENTRY_KEYS = {
        { "gripeverde.field_guide.body.infection" },
        { "gripeverde.field_guide.body.pins" },
        { "gripeverde.field_guide.body.downed", "gripeverde.field_guide.body.dead" },
        { "gripeverde.field_guide.body.supplies", "gripeverde.field_guide.body.difficulty" },
        { "gripeverde.field_guide.body.regroup" }
    };

    private final Screen parent;
    private final List<GuideRow> rows = new ArrayList<>();
    private int panelX, panelY, panelW, panelH;
    private int scrollY;
    private int maxScroll;
    private int contentHeight;
    private static final int LINE_HEIGHT = 10;

    public DiaryFieldGuideScreen(Screen parent) {
        super(Component.translatable("gripeverde.field_guide.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelW = Math.min(520, Math.max(196, this.width - 24));
        panelH = Math.min(430, Math.max(150, this.height - 24));
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        int closeW = Math.min(190, panelW - 24);
        this.addRenderableWidget(new DiaryButton(
            panelX + (panelW - closeW) / 2, panelY + panelH - 31, closeW, 23,
            Component.translatable("gripeverde.field_guide.button.close"),
            DiaryButton.Icon.DOOR, DiaryButton.Variant.PRIMARY,
            DiaryButton.SoundProfile.BACK,
            this::onClose));

        buildRows();
    }

    private void buildRows() {
        rows.clear();
        int textWidth = Math.max(148, panelW - 44);
        for (int section = 0; section < SECTION_KEYS.length; section++) {
            append(Component.translatable(SECTION_KEYS[section][0]), textWidth, true, section == 0, 3);
            for (String entry : ENTRY_KEYS[section]) {
                append(Component.translatable(entry), textWidth, false, false, 0);
            }
            rows.add(new GuideRow(null, false, false, 5));
        }

        contentHeight = 0;
        for (GuideRow row : rows) {
            contentHeight += row.gapBefore + (row.text == null ? 0 : LINE_HEIGHT);
        }
        maxScroll = Math.max(0, contentHeight - viewportHeight());
        scrollY = Math.min(scrollY, maxScroll);
    }

    private void append(Component text, int width, boolean heading, boolean accent, int gap) {
        for (FormattedCharSequence line : this.font.split(text, width)) {
            rows.add(new GuideRow(line, heading, accent, gap));
            gap = 0;
        }
    }

    private int viewportTop() { return panelY + 48; }
    private int viewportBottom() { return panelY + panelH - 47; }
    private int viewportHeight() { return Math.max(1, viewportBottom() - viewportTop()); }

    @Override
    public void tick() {
        super.tick();
        QuarantineSceneRenderer.tick(1f / 20f);
    }

    @Override
    public void renderBackground(GuiGraphics g) { /* bypass dirt */ }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
        DiaryChrome.drawPanelShadow(g, panelX, panelY, panelW, panelH);
        PaperTexture.drawClean(g, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.ARCHIVE);
        DiaryChrome.drawCornerOrnaments(g, panelX, panelY, panelW, panelH);
        DiaryChrome.drawRegistryTag(g, panelX, panelY, panelW, panelH, "REG. IV");

        int cx = panelX + panelW / 2;
        g.drawCenteredString(this.font, Component.translatable("gripeverde.field_guide.title"),
            cx, panelY + 9, DiaryChrome.INK_TITLE);

        String subtitle = this.font.substrByWidth(
            Component.translatable("gripeverde.field_guide.subtitle").getString(), panelW - 36);
        g.drawCenteredString(this.font, subtitle, cx, panelY + 23, DiaryChrome.INK_SUBTITLE);
        DiaryChrome.drawInkDivider(g, panelX + 18, panelY + 37, panelW - 36);

        g.fill(cx - 8, panelY + 38, cx - 4, panelY + 40, DiaryTheme.GHOUL_EYE);
        g.fill(cx + 4, panelY + 38, cx + 8, panelY + 40, DiaryTheme.GHOUL_EYE_GLOW);

        int top = viewportTop();
        int bottom = viewportBottom();
        g.enableScissor(panelX + 14, top, panelX + panelW - 14, bottom);
        int y = top - scrollY;
        for (GuideRow row : rows) {
            y += row.gapBefore;
            if (row.text != null) {
                int color = row.heading
                    ? (row.accent ? DiaryTheme.GHOUL_EYE : DiaryTheme.REDCOAT_DARK)
                    : DiaryTheme.INK;
                g.drawString(this.font, row.text, panelX + 20, y, color, false);
                y += LINE_HEIGHT;
            }
        }
        g.disableScissor();

        if (maxScroll > 0) drawScrollbar(g, top, bottom);
        String hint = this.font.substrByWidth(
            Component.translatable("gripeverde.field_guide.scroll_hint").getString(), panelW - 32);
        int hintW = this.font.width(hint);
        g.drawString(this.font, hint, cx - hintW / 2, panelY + panelH - 42,
            DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 180), false);

        super.render(g, mouseX, mouseY, partialTick);
        com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
    }

    private void drawScrollbar(GuiGraphics g, int top, int bottom) {
        int trackX = panelX + panelW - 10;
        int trackH = bottom - top;
        g.fill(trackX, top, trackX + 2, bottom, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 75));
        int thumbH = Math.max(18, trackH * trackH / Math.max(trackH, contentHeight));
        int travel = Math.max(0, trackH - thumbH);
        int thumbY = top + travel * scrollY / Math.max(1, maxScroll);
        g.fill(trackX - 1, thumbY, trackX + 3, thumbY + thumbH, DiaryTheme.GHOUL_EYE);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScroll > 0 && mouseX >= panelX && mouseX <= panelX + panelW
                && mouseY >= viewportTop() && mouseY <= viewportBottom()) {
            scrollY = Math.max(0, Math.min(maxScroll, scrollY - (int)Math.signum(delta) * 18));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private static final class GuideRow {
        private final FormattedCharSequence text;
        private final boolean heading;
        private final boolean accent;
        private final int gapBefore;

        private GuideRow(FormattedCharSequence text, boolean heading, boolean accent, int gapBefore) {
            this.text = text;
            this.heading = heading;
            this.accent = accent;
            this.gapBefore = gapBefore;
        }
    }
}
