package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.widgets.SlimButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ChangelogScreen extends Screen {

    private static final int PANEL_MX   = 56;
    private static final int PANEL_TOP  = 34;
    private static final int PANEL_BOT  = 54;
    private static final int PAD_X      = 18;
    private static final int PAD_Y      = 26;
    private static final int CLOSE_W    = 130;
    private static final int CLOSE_H    = 28;
    private static final int SCROLL_SPD = 14;

    private static final int LINE_H    = 11;
    private static final int H1_LINE_H = 16;
    private static final int H2_LINE_H = 13;
    private static final int BOLD_LINE_H = 13;
    private static final int GAP_H     = 10;
    private static final int SEP_H     = 12;

    private enum LT { H1, H2, BULLET, TEXT, GAP, SEPARATOR, BOLD }

    private static final class Row {
        final LT type;
        final String text;
        Row(LT t, String s) { this.type = t; this.text = s; }
    }

    private final Screen parent;
    private final List<Row> rows = new ArrayList<>();
    private boolean loadError = false;
    private int scrollY = 0;
    private static int lastScrollY = 0;  // Remembered across reopens
    private int maxScroll = 0;
    private int lastContentW = -1;

    public ChangelogScreen(Screen parent) {
        super(Component.translatable("gripeverde.changelog.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.scrollY = lastScrollY;
        this.loadError = false;
        this.rows.clear();
        this.lastContentW = -1;

        loadAndParse();
        recalcMaxScroll();

        int bx = (this.width - CLOSE_W) / 2;
        int by = this.height - CLOSE_H - 10;
        this.addRenderableWidget(new SlimButton(bx, by, CLOSE_W, CLOSE_H,
            Component.translatable("gripeverde.changelog.button.close"),
            () -> this.minecraft.setScreen(this.parent)));
    }

    private void loadAndParse() {
        String lang = "en_us";
        try {
            lang = Minecraft.getInstance().getLanguageManager().getSelected();
            if (lang == null || lang.isBlank()) lang = "en_us";
        } catch (Exception ignored) {}

        boolean ok = tryLoad(lang);
        if (!ok && !lang.equals("en_us")) {
            ok = tryLoad("en_us");
        }
        if (!ok) {
            this.loadError = true;
        }
    }

    private boolean tryLoad(String lang) {
        try {
            ResourceLocation loc = new ResourceLocation("gripeverde", "changelog/" + lang + ".md");
            InputStream in = this.minecraft.getResourceManager().open(loc);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    Row r = parseLine(line);
                    if (r != null) rows.add(r);
                } catch (Exception ignoredLine) {
                    // Tolerante a lineas mal formateadas
                }
            }
            br.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Row parseLine(String raw) {
        if (raw == null) return null;
        String s = raw;
        if (s.isBlank())            return new Row(LT.GAP, "");
        if (s.startsWith("---"))    return new Row(LT.SEPARATOR, "");
        if (s.startsWith("# "))     return new Row(LT.GAP, "");
        if (s.startsWith("## "))    return new Row(LT.H1, s.substring(3).trim());
        if (s.startsWith("### "))   return new Row(LT.H2, s.substring(4).trim());
        if (s.startsWith("- "))     return new Row(LT.BULLET, s.substring(2).trim());
        String trimmed = s.trim();
        if (trimmed.length() >= 4 && trimmed.startsWith("**") && trimmed.endsWith("**")) {
            return new Row(LT.BOLD, trimmed.substring(2, trimmed.length() - 2).trim());
        }
        return new Row(LT.TEXT, trimmed);
    }

    private int lineHeightFor(LT t) {
        return switch (t) {
            case H1   -> H1_LINE_H;
            case H2   -> H2_LINE_H;
            case BOLD -> BOLD_LINE_H;
            default   -> LINE_H;
        };
    }

    private int rowHeight(Row row, int contentW) {
        switch (row.type) {
            case GAP:       return GAP_H;
            case SEPARATOR: return SEP_H;
            default: {
                int indent = (row.type == LT.BULLET) ? 10 : 0;
                int prefixW = (row.type == LT.BULLET) ? this.font.width("\u2022 ") : 0;
                int available = Math.max(20, contentW - indent - prefixW);
                List<FormattedCharSequence> wrapped =
                    this.font.split(Component.literal(row.text), available);
                int lines = Math.max(1, wrapped.size());
                return lines * lineHeightFor(row.type);
            }
        }
    }

    private void recalcMaxScroll() {
        int cw = contentW();
        this.lastContentW = cw;
        int total = 0;
        for (Row r : rows) total += rowHeight(r, cw);
        int visible = contentH();
        this.maxScroll = Math.max(0, total - visible);
        clampScroll();
    }

    private void clampScroll() {
        if (scrollY < 0) scrollY = 0;
        if (scrollY > maxScroll) scrollY = maxScroll;
    }

    private void scrollBy(int d) { scrollY += d; clampScroll(); }

    private int panelX() { return PANEL_MX; }
    private int panelY() { return PANEL_TOP; }
    private int panelW() { return this.width  - PANEL_MX * 2; }
    private int panelH() { return this.height - PANEL_TOP - PANEL_BOT; }
    private int contentX() { return panelX() + PAD_X; }
    private int contentY() { return panelY() + PAD_Y + 10; }
    private int contentW() { return Math.max(40, panelW() - PAD_X * 2 - 8); }
    private int contentH() { return Math.max(20, panelH() - PAD_Y * 2 - 10); }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int px = panelX(), py = panelY(), pw = panelW(), ph = panelH();
        int cx = contentX(), cy = contentY(), cw = contentW(), ch = contentH();

        if (cw != lastContentW) {
            recalcMaxScroll();
        }

        g.fill(0, 0, this.width, this.height, 0xD0100D08);

        com.eternalcraft.gripeverde.client.render.PaperTexture.drawClean(g, px, py, pw, ph);
        g.fill(px,          py,          px + pw, py + 1,      DiaryTheme.withAlpha(DiaryTheme.BRONZE, 180));
        g.fill(px,          py + ph - 1, px + pw, py + ph,     DiaryTheme.withAlpha(DiaryTheme.BRONZE, 180));
        g.fill(px,          py,          px + 1,  py + ph,     DiaryTheme.withAlpha(DiaryTheme.BRONZE, 180));
        g.fill(px + pw - 1, py,          px + pw, py + ph,     DiaryTheme.withAlpha(DiaryTheme.BRONZE, 180));

        int tw = this.font.width(this.title);
        int tx = (this.width - tw) / 2;
        g.drawString(this.font, this.title, tx, py + 8, DiaryTheme.AMBER_FLAME, false);
        g.fill(tx - 4, py + 18, tx + tw + 4, py + 19, DiaryTheme.withAlpha(DiaryTheme.BRONZE, 150));

        g.enableScissor(cx, cy, cx + cw, cy + ch);

        if (loadError) {
            g.drawString(this.font,
                Component.translatable("gripeverde.changelog.error").getString(),
                cx, cy + 4, DiaryTheme.BLOOD_FRESH, false);
        } else {
            int y = cy - scrollY;
            for (Row row : rows) {
                int h = rowHeight(row, cw);
                if (y + h >= cy - 2 && y <= cy + ch + 2) {
                    renderRow(g, row, cx, y, cw);
                }
                y += h;
            }
        }

        g.disableScissor();

        if (maxScroll > 0) {
            int bx2 = px + pw - 6;
            int bh  = ch;
            int th  = Math.max(20, bh * bh / (bh + maxScroll));
            int ty2 = cy + (bh - th) * scrollY / maxScroll;
            g.fill(bx2, cy,  bx2 + 3, cy + bh, DiaryTheme.withAlpha(DiaryTheme.PAPER_STAINED, 110));
            g.fill(bx2, ty2, bx2 + 3, ty2 + th, DiaryTheme.withAlpha(DiaryTheme.BRONZE, 190));
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderRow(GuiGraphics g, Row row, int x, int y, int maxW) {
        if (row.type == LT.GAP) return;

        if (row.type == LT.SEPARATOR) {
            int lineY = y + 5;
            int sepX1 = x + 20;
            int sepX2 = x + maxW - 20;
            int sepCx = (sepX1 + sepX2) / 2;
            int sepMid = (sepX1 + sepX2) / 2;
            int inkSep = DiaryTheme.withAlpha(DiaryTheme.BRONZE, 110);
            // Left line with fade
            g.fillGradient(sepX1, lineY, sepMid - 6, lineY + 1,
                DiaryTheme.withAlpha(DiaryTheme.BRONZE, 20), inkSep);
            // Right line with fade
            g.fillGradient(sepMid + 6, lineY, sepX2, lineY + 1,
                inkSep, DiaryTheme.withAlpha(DiaryTheme.BRONZE, 20));
            // Central lozenge (diamond 5x5)
            int lz = DiaryTheme.withAlpha(DiaryTheme.BRONZE, 160);
            g.fill(sepCx,     lineY - 2, sepCx + 1, lineY - 1, lz);
            g.fill(sepCx - 1, lineY - 1, sepCx + 2, lineY,     lz);
            g.fill(sepCx - 2, lineY,     sepCx + 3, lineY + 1, lz);
            g.fill(sepCx - 1, lineY + 1, sepCx + 2, lineY + 2, lz);
            g.fill(sepCx,     lineY + 2, sepCx + 1, lineY + 3, lz);
            return;
        }

        int color = switch (row.type) {
            case H1     -> DiaryTheme.AMBER_FLAME;
            case H2     -> DiaryTheme.SEPIA_LIGHT;
            case BULLET -> DiaryTheme.INK_FADED;
            case BOLD   -> DiaryTheme.AMBER_WARM;
            default     -> DiaryTheme.INK;
        };

        int indent = (row.type == LT.BULLET) ? 10 : 0;
        int lh = lineHeightFor(row.type);

        if (row.type == LT.BULLET) {
            g.drawString(this.font, "\u2022 ", x + indent, y, color, false);
            int prefixW = this.font.width("\u2022 ");
            int available = Math.max(20, maxW - indent - prefixW);
            List<FormattedCharSequence> wrapped =
                this.font.split(Component.literal(row.text), available);
            int ly = y;
            for (FormattedCharSequence fcs : wrapped) {
                g.drawString(this.font, fcs, x + indent + prefixW, ly, color, false);
                ly += lh;
            }
        } else if (row.type == LT.H1 && row.text.length() > 1) {
            // Drop cap: first letter drawn larger in dark ink
            String firstLetter = row.text.substring(0, 1);
            String rest = row.text.substring(1);
            int capColor = DiaryTheme.INK;

            g.pose().pushPose();
            float capScale = 1.6f;
            g.pose().translate(x + indent, y - 1, 0);
            g.pose().scale(capScale, capScale, 1f);
            g.drawString(this.font, firstLetter, 0, 0, capColor, false);
            g.pose().popPose();

            int capW = (int)(this.font.width(firstLetter) * capScale) + 2;
            int available = Math.max(20, maxW - indent - capW);
            List<FormattedCharSequence> wrapped =
                this.font.split(Component.literal(rest), available);
            int ly = y;
            for (int wi = 0; wi < wrapped.size(); wi++) {
                int lx = (wi == 0) ? x + indent + capW : x + indent;
                g.drawString(this.font, wrapped.get(wi), lx, ly, color, false);
                ly += lh;
            }
        } else {
            int available = Math.max(20, maxW - indent);
            List<FormattedCharSequence> wrapped =
                this.font.split(Component.literal(row.text), available);
            int ly = y;
            for (FormattedCharSequence fcs : wrapped) {
                g.drawString(this.font, fcs, x + indent, ly, color, false);
                ly += lh;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollBy((int)(-Math.signum(delta) * SCROLL_SPD));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_UP        -> { scrollBy(-SCROLL_SPD);     return true; }
            case GLFW.GLFW_KEY_DOWN      -> { scrollBy( SCROLL_SPD);     return true; }
            case GLFW.GLFW_KEY_PAGE_UP   -> { scrollBy(-contentH() / 2); return true; }
            case GLFW.GLFW_KEY_PAGE_DOWN -> { scrollBy( contentH() / 2); return true; }
            case GLFW.GLFW_KEY_HOME      -> { scrollY = 0;                return true; }
            case GLFW.GLFW_KEY_END       -> { scrollY = maxScroll;        return true; }
            case GLFW.GLFW_KEY_ESCAPE    -> {
                this.minecraft.setScreen(this.parent);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean isPauseScreen()    { return false; }
    @Override public boolean shouldCloseOnEsc() { return false; }

    @Override
    public void onClose() {
        lastScrollY = this.scrollY;
        super.onClose();
    }
}
