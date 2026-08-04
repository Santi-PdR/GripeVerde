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
    private static final int SCROLL_SPD = 28;

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
    private double smoothScrollY = 0.0D;
    private double targetScrollY = 0.0D;
    private static int lastScrollY = 0;  // Remembered across reopens
    private int maxScroll = 0;
    private int lastContentW = -1;
    private boolean draggingScrollbar = false;

    public ChangelogScreen(Screen parent) {
        super(Component.literal(pickChangelogTitle()));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.scrollY = lastScrollY;
        this.smoothScrollY = lastScrollY;
        this.targetScrollY = lastScrollY;
        this.loadError = false;
        this.rows.clear();
        this.lastContentW = -1;

        loadAndParse();
        recalcMaxScroll();

        int bx = (this.width - CLOSE_W) / 2;
        int by = this.height - CLOSE_H - 10;
        this.addRenderableWidget(new SlimButton(bx, by, CLOSE_W, CLOSE_H,
            Component.translatable("gripeverde.changelog.button.close"),
            this::onClose));
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
        if (targetScrollY < 0.0D) targetScrollY = 0.0D;
        if (targetScrollY > maxScroll) targetScrollY = maxScroll;
        if (smoothScrollY < 0.0D) smoothScrollY = 0.0D;
        if (smoothScrollY > maxScroll) smoothScrollY = maxScroll;
        scrollY = (int)Math.round(smoothScrollY);
    }

    private void setScrollTarget(double value, boolean snap) {
        targetScrollY = Math.max(0.0D, Math.min(maxScroll, value));
        if (snap) {
            smoothScrollY = targetScrollY;
        }
        scrollY = (int)Math.round(smoothScrollY);
    }

    private void tickSmoothScroll() {
        double diff = targetScrollY - smoothScrollY;
        if (Math.abs(diff) < 0.35D) {
            smoothScrollY = targetScrollY;
        } else {
            smoothScrollY += diff * 0.46D;
        }
        clampScroll();
    }

    private void scrollToSection(boolean next) {
        int cw = contentW();
        int pos = 0;
        int currentTarget = (int)Math.round(targetScrollY);
        int candidate = next ? maxScroll : 0;
        for (Row r : rows) {
            if (r.type == LT.H1) {
                if (next && pos > currentTarget + 4) {
                    candidate = pos;
                    break;
                }
                if (!next && pos < currentTarget - 4) {
                    candidate = pos;
                }
            }
            pos += rowHeight(r, cw);
        }
        setScrollTarget(candidate, false);
    }

    private void scrollToTopOrBottom(boolean bottom) {
        setScrollTarget(bottom ? maxScroll : 0, false);
    }

    private void scrollBy(int d) { setScrollTarget(targetScrollY + d, false); }

    private void scrollByWithSound(int d) {
        double before = targetScrollY;
        scrollBy(d);
        if (Math.abs(before - targetScrollY) > 0.1D) {
            long now = System.currentTimeMillis();
            if (now - lastScrollSoundMs > 120L) {
                com.eternalcraft.gripeverde.client.audio.UiSounds.scroll();
                lastScrollSoundMs = now;
            }
        }
    }

    private boolean overScrollbar(double mouseX, double mouseY) {
        if (maxScroll <= 0) return false;
        int px = panelX(), pw = panelW();
        int cy = contentY(), ch = contentH();
        int bx = px + pw - 8;
        return mouseX >= bx - 3 && mouseX <= bx + 8 && mouseY >= cy && mouseY <= cy + ch;
    }

    private void setScrollFromMouse(double mouseY) {
        if (maxScroll <= 0) return;
        int cy = contentY(), ch = contentH();
        int th = Math.max(20, ch * ch / (ch + maxScroll));
        double t = (mouseY - cy - th / 2.0) / Math.max(1.0, ch - th);
        setScrollTarget(Math.max(0.0, Math.min(1.0, t)) * maxScroll, true);
    }

    private int panelX() { return PANEL_MX; }
    private int panelY() { return PANEL_TOP; }
    private int panelW() { return this.width  - PANEL_MX * 2; }
    private int panelH() { return this.height - PANEL_TOP - PANEL_BOT; }
    private int contentX() { return panelX() + PAD_X; }
    private int contentY() { return panelY() + PAD_Y + 10; }
    private int contentW() { return Math.max(40, panelW() - PAD_X * 2 - 8); }
    private int contentH() { return Math.max(20, panelH() - PAD_Y * 2 - 10); }

    @Override
    public void tick() {
        super.tick();
        tickSmoothScroll();
        com.eternalcraft.gripeverde.client.audio.MenuMusic.tick();
        com.eternalcraft.gripeverde.client.audio.MenuAmbience.tick();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int px = panelX(), py = panelY(), pw = panelW(), ph = panelH();
        int cx = contentX(), cy = contentY(), cw = contentW(), ch = contentH();

        if (cw != lastContentW) {
            recalcMaxScroll();
        }

        g.fill(0, 0, this.width, this.height, 0xD0100D08);

        com.eternalcraft.gripeverde.client.render.PaperTexture.drawClean(g, px, py, pw, ph, com.eternalcraft.gripeverde.client.render.PaperTexture.PaperKind.ARCHIVE);
        g.fill(px,          py,          px + pw, py + 1,      DiaryTheme.withAlpha(DiaryTheme.BRONZE, 180));
        g.fill(px,          py + ph - 1, px + pw, py + ph,     DiaryTheme.withAlpha(DiaryTheme.BRONZE, 180));
        g.fill(px,          py,          px + 1,  py + ph,     DiaryTheme.withAlpha(DiaryTheme.BRONZE, 180));
        g.fill(px + pw - 1, py,          px + pw, py + ph,     DiaryTheme.withAlpha(DiaryTheme.BRONZE, 180));
        drawLedgerDetails(g, px, py, pw, ph, cx, cy, cw, ch);

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

    private void drawLedgerDetails(GuiGraphics g, int px, int py, int pw, int ph,
                                   int cx, int cy, int cw, int ch) {
        int bronzeSoft = DiaryTheme.withAlpha(DiaryTheme.BRONZE, 85);
        int inkSoft = DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 48);
        int redSeal = DiaryTheme.withAlpha(DiaryTheme.BLOOD_DRIED, 140);

        // ── Binding strip with brass rivets ──
        int stripX = px + 9;
        g.fill(stripX, py + 10, stripX + 6, py + ph - 10, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 32));
        g.fill(stripX + 1, py + 10, stripX + 2, py + ph - 10, DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 60));
        for (int y = py + 28; y < py + ph - 22; y += 38) {
            g.fill(stripX + 1, y, stripX + 4, y + 2, bronzeSoft);
            int rivet = DiaryTheme.withAlpha(DiaryTheme.GOLD_TARNISHED, 110);
            g.fill(stripX + 2, y + 1, stripX + 3, y + 2, rivet);
        }

        // ── Marginal rule with archival ticks every 26px ──
        int ruleX = cx + 6;
        g.fill(ruleX, cy - 6, ruleX + 1, cy + ch + 6, DiaryTheme.withAlpha(DiaryTheme.BRONZE, 70));
        for (int y = cy + 10; y < cy + ch; y += 28) {
            g.fill(ruleX - 4, y, ruleX + 5, y + 1, DiaryTheme.withAlpha(DiaryTheme.BRONZE, 55));
        }

        // ── Top archival reference line ──
        boolean en = isEnglishSelected();
        String ref = en ? "ARCHIVE EC-QV/1897 - FOLIO " + (scrollY / 120 + 1) : "ARCHIVO EC-QV/1897 - FOLIO " + (scrollY / 120 + 1);
        int refW = this.font.width(ref);
        int refX = px + pw - refW - 22;
        int refY = py + 6;
        if (pw > 260) {
            g.drawString(this.font, ref, refX, refY, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 95), false);
        }

        // ── Quarantine stamp (official, slightly rotated feel via offset shadow) ──
        int stampW = 96;
        int stampX = px + pw - stampW - 20;
        int stampY = py + 26;
        // Shadow
        g.fill(stampX + 2, stampY + 2, stampX + stampW + 2, stampY + 18, DiaryTheme.withAlpha(0x00000000, 55));
        // Border
        g.fill(stampX, stampY, stampX + stampW, stampY + 1, redSeal);
        g.fill(stampX, stampY + 17, stampX + stampW, stampY + 18, redSeal);
        g.fill(stampX, stampY, stampX + 1, stampY + 18, redSeal);
        g.fill(stampX + stampW - 1, stampY, stampX + stampW, stampY + 18, redSeal);
        // Inner double line
        g.fill(stampX + 3, stampY + 3, stampX + stampW - 3, stampY + 4, DiaryTheme.withAlpha(DiaryTheme.BLOOD_DRIED, 80));
        g.fill(stampX + 3, stampY + 14, stampX + stampW - 3, stampY + 15, DiaryTheme.withAlpha(DiaryTheme.BLOOD_DRIED, 80));
        String stamp = en ? "FILED UNDER SEAL" : "BAJO SELLO";
        int sw = this.font.width(stamp);
        g.drawString(this.font, stamp, stampX + (stampW - sw) / 2, stampY + 6,
            DiaryTheme.withAlpha(DiaryTheme.BLOOD_DRIED, 175), false);

        // ── Bottom pagination & ledger fade ──
        int bottomY = py + ph - 28;
        g.fillGradient(px + 28, bottomY, px + pw / 2, bottomY + 1,
            DiaryTheme.withAlpha(DiaryTheme.BRONZE, 0), DiaryTheme.withAlpha(DiaryTheme.BRONZE, 85));
        g.fillGradient(px + pw / 2, bottomY, px + pw - 28, bottomY + 1,
            DiaryTheme.withAlpha(DiaryTheme.BRONZE, 85), DiaryTheme.withAlpha(DiaryTheme.BRONZE, 0));
        // Small page mark
        if (pw > 200) {
            String folio = en ? "~ folio ~" : "~ folio ~";
            int fw = this.font.width(folio);
            g.drawString(this.font, folio, px + (pw - fw) / 2, bottomY + 6, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 75), false);
        }
    }

    private static boolean isEnglishSelected() {
        String lang = "en_us";
        try { lang = Minecraft.getInstance().getLanguageManager().getSelected(); }
        catch (Throwable ignored) {}
        return lang == null || lang.startsWith("en_") || lang.equals("en_us");
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

            // Nota al margen: asterisco decorativo a la izquierda del H1
            int marginColor = DiaryTheme.withAlpha(DiaryTheme.BRONZE, 180);
            g.drawString(this.font, "*", x + indent - 10, y + 4, marginColor, false);

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

    private long lastScrollSoundMs = 0L;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && overScrollbar(mouseX, mouseY)) {
            this.draggingScrollbar = true;
            setScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingScrollbar) {
            setScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.draggingScrollbar) {
            this.draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollBy((int)(-Math.signum(delta) * SCROLL_SPD));
        // Sonido de pagina: throttle 200ms entre scrolls consecutivos
        long now = System.currentTimeMillis();
        if (now - lastScrollSoundMs > 200L) {
            com.eternalcraft.gripeverde.client.audio.UiSounds.scroll();
            lastScrollSoundMs = now;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_UP        -> { scrollByWithSound(-SCROLL_SPD);     return true; }
            case GLFW.GLFW_KEY_DOWN      -> { scrollByWithSound( SCROLL_SPD);     return true; }
            case GLFW.GLFW_KEY_PAGE_UP   -> { scrollByWithSound(-contentH() / 2); return true; }
            case GLFW.GLFW_KEY_PAGE_DOWN -> { scrollByWithSound( contentH() / 2); return true; }
            case GLFW.GLFW_KEY_HOME      -> { scrollToTopOrBottom(false); return true; }
            case GLFW.GLFW_KEY_END       -> { scrollToTopOrBottom(true);  return true; }
            case GLFW.GLFW_KEY_ESCAPE    -> {
                this.onClose();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean isPauseScreen()    { return false; }
    @Override public boolean shouldCloseOnEsc() { return false; }

    @Override
    public void onClose() {
        lastScrollY = (int)Math.round(this.targetScrollY);
        super.onClose();
    }

    // Rotating titles for the changelog screen, localized so an English client
    // does not open a Spanish chronicle title.
    private static final String[] CHANGELOG_TITLES_ES = {
        "Historial de Cambios",
        "Cronicas del Cronista",
        "Bitacora de la Ciudad",
        "Diario del Superviviente",
        "Registro de Noches"
    };
    private static final String[] CHANGELOG_TITLES_EN = {
        "Change History",
        "The Chronicler's Notes",
        "City Ledger",
        "Survivor's Journal",
        "Register of Nights"
    };
    private static final java.util.Random CHANGELOG_RNG = new java.util.Random();

    private static String pickChangelogTitle() {
        String lang = "en_us";
        try {
            lang = Minecraft.getInstance().getLanguageManager().getSelected();
        } catch (Throwable ignored) {}
        boolean english = lang == null || lang.startsWith("en_") || lang.equals("en_us");
        String[] pool = english ? CHANGELOG_TITLES_EN : CHANGELOG_TITLES_ES;
        return pool[CHANGELOG_RNG.nextInt(pool.length)];
    }
}
