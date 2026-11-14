package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.lore.ChroniclerMemory;
import com.eternalcraft.gripeverde.client.lore.Marginalia;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.DiaryTypography;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
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
    private static final String[] SECTION_NUMBERS = { "1", "2", "3", "4", "5", "6", "7" };
    private static final int INFECTION_SECTION = 0;
    private static final int TEAM_SECTION = 1;
    private static final int RESCUE_SECTION = SECTION_KEYS.length - 1;

    private static final String[][] ENTRY_KEYS = {
        { "gripeverde.field_guide.body.infection", "gripeverde.field_guide.body.mutation_origins", "gripeverde.field_guide.body.ceda_coverup" },
        { "gripeverde.field_guide.body.pins" },
        { "gripeverde.field_guide.body.downed", "gripeverde.field_guide.body.dead" },
        { "gripeverde.field_guide.body.supplies", "gripeverde.field_guide.body.difficulty",
          "gripeverde.field_guide.body.requirements" },
        { "gripeverde.field_guide.body.regroup" },
        { "gripeverde.field_guide.body.races", "gripeverde.field_guide.body.rarities" },
        { "gripeverde.field_guide.body.rescue_alert", "gripeverde.field_guide.body.rescue_downed",
          "gripeverde.field_guide.body.rescue_dead" }
    };

    private final Screen parent;
    private final List<GuideRow> rows = new ArrayList<>();
    private final List<Integer> sectionOffsets = new ArrayList<>();
    private final List<GuideSectionButton> sectionWidgets = new ArrayList<>();
    private int panelX, panelY, panelW, panelH;
    private int scrollY;
    private int maxScroll;
    private int contentHeight;
    private boolean draggingScrollbar;
    private boolean compactLayout;
    // v0.183.0: el manual tambien respira (12 px de renglon, 7 entre parrafos,
    // 10 entre secciones); los titulos se consultan despues que los botones.
    private static final int LINE_HEIGHT = 12;
    private static final int PARAGRAPH_GAP = 7;
    private static final int SECTION_GAP = 10;

    public DiaryFieldGuideScreen(Screen parent) {
        super(Component.translatable("gripeverde.field_guide.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelW = Math.min(520, Math.max(1, this.width - 16));
        panelH = Math.min(430, Math.max(1, this.height - 16));
        panelX = Math.max(0, (this.width - panelW) / 2);
        panelY = Math.max(0, (this.height - panelH) / 2);
        compactLayout = panelW < 360 || panelH < 240;

        int closeW = Math.min(190, Math.max(1, panelW - (compactLayout ? 16 : 24)));
        int closeH = compactLayout ? 20 : 23;
        int closeBottomInset = compactLayout ? 25 : 31;
        this.addRenderableWidget(new DiaryButton(
            panelX + (panelW - closeW) / 2, panelY + panelH - closeBottomInset, closeW, closeH,
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
        int textWidth = Math.max(1, panelW - 48);
        for (int section = 0; section < SECTION_KEYS.length; section++) {
            boolean emphasized = section == 0 || section == SECTION_KEYS.length - 1;
            append(Component.translatable(SECTION_KEYS[section][0]), textWidth, true, emphasized, 4);
            for (String entry : ENTRY_KEYS[section]) {
                append(Component.translatable(entry), textWidth, false, false,
                    PARAGRAPH_GAP, calloutKind(entry));
            }
            rows.add(new GuideRow(null, false, false, SECTION_GAP));
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
            int headingWidth = Math.max(1, DiaryTypography.width(this.font, DiaryTypography.split(this.font, heading, textWidth).get(0)));
            int targetWidth = Math.max(1, Math.min(panelW - 36, headingWidth + 16));
            GuideSectionButton sectionButton = new GuideSectionButton(
                panelX + 18, viewportTop() + sectionOffsets.get(section) - scrollY,
                targetWidth, LINE_HEIGHT, narration,
                () -> jumpToSection(targetSection));
            this.addWidget(sectionButton);
            sectionWidgets.add(sectionButton);
        }
        positionSectionWidgets();
    }

    private void append(Component text, int width, boolean heading, boolean accent, int gap) {
        append(text, width, heading, accent, gap, 0);
    }

    private void append(Component text, int width, boolean heading, boolean accent,
                        int gap, int callout) {
        for (FormattedCharSequence line : DiaryTypography.split(this.font, text, width)) {
            rows.add(new GuideRow(line, heading, accent, gap, callout));
            gap = 0;
        }
    }

    private static int calloutKind(String key) {
        if (key.endsWith(".pins") || key.endsWith(".rescue_alert")) return 1;
        if (key.endsWith(".downed") || key.endsWith(".rescue_downed")) return 2;
        if (key.endsWith(".dead") || key.endsWith(".rescue_dead")) return 3;
        return 0;
    }

    private static int calloutColor(int callout) {
        return switch (callout) {
            case 1 -> DiaryTheme.GHOUL_EYE;
            case 2 -> DiaryTheme.AMBER_FLAME;
            case 3 -> DiaryTheme.BLOOD_FRESH;
            default -> DiaryTheme.SEPIA_MID;
        };
    }

    private int viewportTop() { return panelY + (compactLayout ? 42 : 48); }
    private int viewportBottom() { return panelY + panelH - (compactLayout ? 30 : 47); }
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
        PaperTexture.drawVeil(g, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.ARCHIVE);
        DiaryChrome.drawCornerOrnaments(g, panelX, panelY, panelW, panelH);
        if (!compactLayout) {
            DiaryChrome.drawRegistryTag(g, panelX, panelY, panelW, panelH, "REG. IV");
        }

        int cx = panelX + panelW / 2;
        String title = fitText(
            Component.translatable("gripeverde.field_guide.title").getString(),
            Math.max(1, panelW - (compactLayout ? 20 : 36)));
        DiaryChrome.drawCenteredString(g, this.font, title, cx, panelY + 9, DiaryChrome.INK_TITLE);

        String subtitleKey = compactLayout && maxScroll > 0
            ? "gripeverde.field_guide.scroll_hint.compact" : "gripeverde.field_guide.subtitle";
        String subtitle = fitText(
            Component.translatable(subtitleKey).getString(), Math.max(1, panelW - 36));
        DiaryChrome.drawCenteredString(g, this.font, subtitle,
            cx, panelY + 23, DiaryChrome.INK_SUBTITLE);
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
            boolean sectionStart = row.heading && row.gapBefore > 0;
            if (sectionStart) sectionIndex++;
            if (row.text != null) {
                boolean headingFocused = row.heading && sectionIndex == focusedSection;
                boolean headingActive = row.heading
                    && (sectionIndex == hoveredSection || headingFocused);
                int color = row.heading
                    ? (headingActive ? DiaryTheme.GHOUL_INK_ACTIVE
                        : row.accent ? DiaryTheme.GHOUL_INK : DiaryTheme.REDCOAT_DARK)
                    : DiaryTheme.INK;
                if (!row.heading && row.callout > 0) {
                    int rail = calloutColor(row.callout);
                    int calloutTop = y - 2;
                    int calloutBottom = y + LINE_HEIGHT - 2;
                    g.fill(panelX + 14, calloutTop, panelX + panelW - 14, calloutBottom,
                        DiaryTheme.withAlpha(rail, 24));
                    g.fill(panelX + 14, calloutTop, panelX + 17, calloutBottom, rail);
                }
                if (sectionStart) {
                    DiaryTypography.draw(g, this.font, SECTION_NUMBERS[sectionIndex], panelX + 20, y, color, false);
                }
                int textX = panelX + (row.heading ? 34 : row.callout > 0 ? 22 : 20);
                int textWidth = DiaryTypography.width(this.font, row.text);
                DiaryTypography.draw(g, this.font, row.text, textX, y, color, false);
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
        if (!compactLayout) {
            String hint = fitText(
                Component.translatable("gripeverde.field_guide.scroll_hint").getString(), panelW - 32);
            int hintW = DiaryTypography.width(this.font, hint);
            DiaryTypography.draw(g, this.font, hint, cx - hintW / 2, panelY + panelH - 42,
                DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 180), false);
        }

        super.render(g, mouseX, mouseY, partialTick);
        com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
    }

    private String fitText(String text, int maxWidth) {
        if (DiaryTypography.width(this.font, text) <= maxWidth) return text;
        return DiaryTypography.fitString(this.font, text, maxWidth);
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
        if (section == INFECTION_SECTION) {
            Marginalia.queueOnce(Marginalia.N_WITNESS);
        } else if (section == TEAM_SECTION) {
            Marginalia.queueOnce(Marginalia.N_CALL);
        } else if (section == RESCUE_SECTION) {
            Marginalia.queueOnce(Marginalia.N_RESCUE);
        }
    }

    private void positionSectionWidgets() {
        for (int section = 0; section < sectionWidgets.size(); section++) {
            GuideSectionButton button = sectionWidgets.get(section);
            button.setX(panelX + 18);
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
                int textX = panelX + 34;
                int hitLeft = row.gapBefore > 0 ? panelX + 18 : textX - 2;
                int textWidth = DiaryTypography.width(this.font, row.text);
                if (mouseX >= hitLeft && mouseX <= textX + textWidth + 2
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
        if (button == 0 && !DiaryChrome.overAnyWidget(this, mouseX, mouseY)) {
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

    /**
     * Keyboard and screen-reader target for a heading. The screen itself draws the
     * printed heading under its viewport scissor; the input target must stay invisible
     * so off-screen sections cannot paint over the guide footer or close button.
     */
    private static final class GuideSectionButton extends AbstractButton {
        private final Runnable action;

        private GuideSectionButton(int x, int y, int width, int height,
                                   Component narration, Runnable action) {
            super(x, y, width, height, narration);
            this.action = action;
        }

        @Override
        public void onPress() {
            action.run();
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            // Heading navigation is silent; the visible text supplies its own feedback.
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // Drawn by DiaryFieldGuideScreen inside the clipped text viewport.
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private static final class GuideRow {
        private final FormattedCharSequence text;
        private final boolean heading;
        private final boolean accent;
        private final int gapBefore;
        private final int callout;

        private GuideRow(FormattedCharSequence text, boolean heading, boolean accent, int gapBefore) {
            this(text, heading, accent, gapBefore, 0);
        }

        private GuideRow(FormattedCharSequence text, boolean heading, boolean accent,
                         int gapBefore, int callout) {
            this.text = text;
            this.heading = heading;
            this.accent = accent;
            this.gapBefore = gapBefore;
            this.callout = callout;
        }
    }
}
