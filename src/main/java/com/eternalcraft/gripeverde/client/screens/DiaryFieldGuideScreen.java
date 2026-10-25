package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.lore.ChroniclerMemory;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

/** Manual de campo: protocolo cooperativo de la cuarentena. */
public final class DiaryFieldGuideScreen extends Screen {

    private static final String[][] SECTION_KEYS = {
        { "gripeverde.field_guide.section.infection" },
        { "gripeverde.field_guide.section.team" },
        { "gripeverde.field_guide.section.downed" },
        { "gripeverde.field_guide.section.expedition" },
        { "gripeverde.field_guide.section.regroup" },
        { "gripeverde.field_guide.section.races" },
        { "gripeverde.field_guide.section.rescue_protocol" }
    };

    private static final String[][] ENTRY_KEYS = {
        { "gripeverde.field_guide.body.infection", "gripeverde.field_guide.body.mutation_origins", "gripeverde.field_guide.body.ceda_coverup" },
        { "gripeverde.field_guide.body.pins" },
        { "gripeverde.field_guide.body.downed", "gripeverde.field_guide.body.dead" },
        { "gripeverde.field_guide.body.supplies", "gripeverde.field_guide.body.difficulty" },
        { "gripeverde.field_guide.body.regroup" },
        { "gripeverde.field_guide.body.races", "gripeverde.field_guide.body.rarities" },
        { "gripeverde.field_guide.body.rescue_alert", "gripeverde.field_guide.body.rescue_downed",
          "gripeverde.field_guide.body.rescue_dead" }
    };

    private final Screen parent;
    private final List<GuideRow> rows = new ArrayList<>();
    private final List<Integer> sectionOffsets = new ArrayList<>();
    private final List<DiaryButton> sectionWidgets = new ArrayList<>();
    private int panelX, panelY, panelW, panelH;
    private int scrollY;
    private int maxScroll;
    private int contentHeight;
    private boolean draggingScrollbar;
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
            Component.translatable(parent instanceof DiaryPauseScreen
                ? "gripeverde.field_guide.button.return_to_pause"
                : "gripeverde.field_guide.button.close"),
            DiaryButton.Icon.DOOR, DiaryButton.Variant.PRIMARY,
            DiaryButton.SoundProfile.BACK,
            this::onClose));

        buildRows();
    }

    private void buildRows() {
        rows.clear();
        int textWidth = Math.max(148, panelW - 44);
        for (int section = 0; section < SECTION_KEYS.length; section++) {
            boolean emphasized = section == 0 || section == SECTION_KEYS.length - 1;
            append(Component.translatable(SECTION_KEYS[section][0]), textWidth, true, emphasized, 3);
            for (String entry : ENTRY_KEYS[section]) {
                append(Component.translatable(entry), textWidth, false, false, 0);
            }
            rows.add(new GuideRow(null, false, false, 5));
        }

        contentHeight = 0;
        sectionOffsets.clear();
        for (GuideRow row : rows) {
            contentHeight += row.gapBefore + (row.text == null ? 0 : LINE_HEIGHT);
            if (row.heading && row.gapBefore > 0) {
                sectionOffsets.add(contentHeight - (row.text == null ? 0 : LINE_HEIGHT));
            }
        }
        maxScroll = Math.max(0, contentHeight - viewportHeight());
        int savedSection = ChroniclerMemory.fieldGuideSection();
        if (savedSection >= 0 && savedSection < sectionOffsets.size()) {
            int sectionStart = sectionOffsets.get(savedSection);
            int sectionEnd = savedSection + 1 < sectionOffsets.size()
                ? sectionOffsets.get(savedSection + 1) : contentHeight;
            int sectionSpan = Math.max(1, sectionEnd - sectionStart);
            scrollY = sectionStart + (int)Math.round(
                sectionSpan * ChroniclerMemory.fieldGuideProgress() / 10000.0);
        } else {
            // Older memory files have only a pixel offset; convert it on first open.
            scrollY = ChroniclerMemory.fieldGuideScrollY();
        }
        scrollY = Math.max(0, Math.min(maxScroll, scrollY));
        rememberBookmark();

        sectionWidgets.clear();
        for (int section = 0; section < SECTION_KEYS.length; section++) {
            int targetSection = section;
            Component heading = Component.translatable(SECTION_KEYS[section][0]);
            MutableComponent narration = Component.empty().append(heading).append(". ");
            for (String entry : ENTRY_KEYS[section]) {
                narration.append(Component.translatable(entry)).append(". ");
            }
            int headingWidth = Math.max(1, this.font.width(this.font.split(heading, textWidth).get(0)));
            DiaryButton sectionButton = new DiaryButton(
                panelX + 20, viewportTop() + sectionOffsets.get(section) - scrollY,
                headingWidth, LINE_HEIGHT, narration,
                DiaryButton.Icon.NONE, DiaryButton.Variant.SECONDARY,
                DiaryButton.SoundProfile.NONE,
                () -> jumpToSection(targetSection));
            this.addWidget(sectionButton);
            sectionWidgets.add(sectionButton);
        }
        positionSectionWidgets();
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

        String subtitle = fitText(
            Component.translatable("gripeverde.field_guide.subtitle").getString(), panelW - 36);
        g.drawCenteredString(this.font, subtitle, cx, panelY + 23, DiaryChrome.INK_SUBTITLE);
        DiaryChrome.drawInkDivider(g, panelX + 18, panelY + 37, panelW - 36);

        g.fill(cx - 8, panelY + 38, cx - 4, panelY + 40, DiaryTheme.GHOUL_EYE);
        g.fill(cx + 4, panelY + 38, cx + 8, panelY + 40, DiaryTheme.GHOUL_EYE_GLOW);

        int top = viewportTop();
        int bottom = viewportBottom();
        g.enableScissor(panelX + 14, top, panelX + panelW - 14, bottom);
        int y = top - scrollY;
        int hoveredSection = headingAt(mouseX, mouseY);
        int focusedSection = focusedSection();
        int sectionIndex = -1;
        for (GuideRow row : rows) {
            y += row.gapBefore;
            if (row.heading && row.gapBefore > 0) sectionIndex++;
            if (row.text != null) {
                boolean headingFocused = row.heading && sectionIndex == focusedSection;
                boolean headingActive = row.heading
                    && (sectionIndex == hoveredSection || headingFocused);
                int color = row.heading
                    ? (headingActive ? DiaryTheme.GHOUL_EYE_GLOW
                        : row.accent ? DiaryTheme.GHOUL_EYE : DiaryTheme.REDCOAT_DARK)
                    : DiaryTheme.INK;
                int textX = panelX + 20;
                int textWidth = this.font.width(row.text);
                g.drawString(this.font, row.text, textX, y, color, false);
                if (headingActive) {
                    g.fill(textX, y + LINE_HEIGHT - 1, textX + textWidth, y + LINE_HEIGHT, color);
                }
                if (headingFocused) {
                    g.fill(panelX + 15, y + 3, panelX + 18, y + 6, DiaryTheme.GHOUL_EYE_GLOW);
                }
                y += LINE_HEIGHT;
            }
        }
        g.disableScissor();

        if (maxScroll > 0) drawScrollbar(g, top, bottom);
        String hint = fitText(
            Component.translatable("gripeverde.field_guide.scroll_hint").getString(), panelW - 32);
        int hintW = this.font.width(hint);
        g.drawString(this.font, hint, cx - hintW / 2, panelY + panelH - 42,
            DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 180), false);

        super.render(g, mouseX, mouseY, partialTick);
        com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
    }

    private String fitText(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        int room = Math.max(0, maxWidth - this.font.width(ellipsis));
        return this.font.plainSubstrByWidth(text, room) + ellipsis;
    }

    private int scrollbarThumbHeight(int trackHeight) {
        return Math.max(18, trackHeight * trackHeight / Math.max(trackHeight, contentHeight));
    }

    private int scrollbarThumbY(int top, int bottom) {
        int trackHeight = bottom - top;
        int travel = Math.max(0, trackHeight - scrollbarThumbHeight(trackHeight));
        return top + travel * scrollY / Math.max(1, maxScroll);
    }

    private void drawScrollbar(GuiGraphics g, int top, int bottom) {
        int trackX = panelX + panelW - 10;
        int trackHeight = bottom - top;
        int thumbHeight = scrollbarThumbHeight(trackHeight);
        g.fill(trackX, top, trackX + 2, bottom, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 75));
        g.fill(trackX - 1, scrollbarThumbY(top, bottom), trackX + 3,
            scrollbarThumbY(top, bottom) + thumbHeight, DiaryTheme.GHOUL_EYE);
    }

    private boolean overScrollbar(double mouseX, double mouseY) {
        int trackX = panelX + panelW - 10;
        return maxScroll > 0 && mouseX >= trackX - 4 && mouseX <= trackX + 6
            && mouseY >= viewportTop() && mouseY <= viewportBottom();
    }

    private void setScrollFromMouse(double mouseY) {
        int top = viewportTop();
        int trackHeight = viewportHeight();
        int thumbHeight = scrollbarThumbHeight(trackHeight);
        int travel = Math.max(1, trackHeight - thumbHeight);
        double fraction = (mouseY - top - thumbHeight / 2.0) / travel;
        scrollY = (int)Math.round(Math.max(0.0, Math.min(1.0, fraction)) * maxScroll);
        rememberBookmark();
        positionSectionWidgets();
    }

    private void rememberBookmark() {
        if (sectionOffsets.isEmpty()) return;
        int section = 0;
        while (section + 1 < sectionOffsets.size()
                && sectionOffsets.get(section + 1) <= scrollY) {
            section++;
        }
        int sectionStart = sectionOffsets.get(section);
        int sectionEnd = section + 1 < sectionOffsets.size()
            ? sectionOffsets.get(section + 1) : contentHeight;
        int sectionSpan = Math.max(1, sectionEnd - sectionStart);
        double progress = (double)(scrollY - sectionStart) / sectionSpan;
        int progressTenThousandths = (int)Math.round(Math.max(0.0, Math.min(1.0, progress)) * 10000.0);
        ChroniclerMemory.setFieldGuideBookmark(section, progressTenThousandths, scrollY);
    }

    private void positionSectionWidgets() {
        for (int section = 0; section < sectionWidgets.size(); section++) {
            DiaryButton button = sectionWidgets.get(section);
            button.setX(panelX + 20);
            button.setY(viewportTop() + sectionOffsets.get(section) - scrollY);
        }
    }

    private int focusedSection() {
        for (int section = 0; section < sectionWidgets.size(); section++) {
            if (sectionWidgets.get(section).isFocused()) return section;
        }
        return -1;
    }

    private void scrollTo(int target) {
        scrollY = Math.max(0, Math.min(maxScroll, target));
        rememberBookmark();
        positionSectionWidgets();
    }

    private void jumpToSection(int section) {
        if (section >= 0 && section < sectionOffsets.size()) {
            scrollTo(sectionOffsets.get(section));
        }
    }

    /** Returns the section under the pointer, or -1 when it is not on a heading. */
    private int headingAt(double mouseX, double mouseY) {
        if (mouseX < panelX + 16 || mouseX > panelX + panelW - 14
                || mouseY < viewportTop() || mouseY > viewportBottom()) {
            return -1;
        }
        int y = viewportTop() - scrollY;
        int section = -1;
        for (GuideRow row : rows) {
            y += row.gapBefore;
            if (row.heading && row.gapBefore > 0) section++;
            if (row.heading && row.text != null) {
                int textX = panelX + 20;
                int textWidth = this.font.width(row.text);
                if (mouseX >= textX - 2 && mouseX <= textX + textWidth + 2
                        && mouseY >= y && mouseY < y + LINE_HEIGHT) {
                    return section;
                }
            }
            if (row.text != null) y += LINE_HEIGHT;
        }
        return -1;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
            int section = focusedSection();
            if (section >= 0) jumpToSection(section);
            return handled;
        }
        if (keyCode >= GLFW.GLFW_KEY_1
                && keyCode < GLFW.GLFW_KEY_1 + SECTION_KEYS.length) {
            jumpToSection(keyCode - GLFW.GLFW_KEY_1);
            return true;
        }
        if (maxScroll > 0) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_UP -> { scrollTo(scrollY - LINE_HEIGHT * 2); return true; }
                case GLFW.GLFW_KEY_DOWN -> { scrollTo(scrollY + LINE_HEIGHT * 2); return true; }
                case GLFW.GLFW_KEY_PAGE_UP -> { scrollTo(scrollY - viewportHeight()); return true; }
                case GLFW.GLFW_KEY_PAGE_DOWN -> { scrollTo(scrollY + viewportHeight()); return true; }
                case GLFW.GLFW_KEY_HOME -> { scrollTo(0); return true; }
                case GLFW.GLFW_KEY_END -> { scrollTo(maxScroll); return true; }
                default -> { }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int section = headingAt(mouseX, mouseY);
            if (section >= 0) {
                sectionWidgets.get(section).setFocused(true);
                this.setFocused(sectionWidgets.get(section));
                jumpToSection(section);
                return true;
            }
        }
        if (button == 0 && overScrollbar(mouseX, mouseY)) {
            draggingScrollbar = true;
            setScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar && button == 0) {
            setScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingScrollbar && button == 0) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScroll > 0 && mouseX >= panelX && mouseX <= panelX + panelW
                && mouseY >= viewportTop() && mouseY <= viewportBottom()) {
            scrollTo(scrollY - (int)Math.signum(delta) * 18);
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
