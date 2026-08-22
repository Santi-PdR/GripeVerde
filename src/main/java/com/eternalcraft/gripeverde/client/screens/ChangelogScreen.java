package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.ExpedientHeader;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
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
    private int contentTotalH = 0;
    private int lastContentW = -1;
    private boolean draggingScrollbar = false;
    private long openedAtMs = 0L;

    // v0.16.0: las marcas del encuadernador y el folio escondido
    private final List<int[]> asteriskRects = new ArrayList<>();
    private static int binderMarksLit = 0;
    private static long binderGlowUntilMs = 0L;
    private int[] hiddenFolioRect = null;

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
        this.openedAtMs = System.currentTimeMillis();
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
        this.contentTotalH = total;
        int visible = contentH();
        // v0.16.0: un poco mas alla del ultimo renglon hay sitio para un folio.
        this.maxScroll = Math.max(0, total - visible) + 28;
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
    private int contentY() { return panelY() + 72; }
    private int contentW() { return Math.max(40, panelW() - PAD_X * 2 - 8); }
    private int contentH() { return Math.max(20, panelH() - 110); }

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

        QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
        float reveal = DiaryChrome.reveal(this.openedAtMs, 260L);
        DiaryChrome.drawPanelScaffold(g, px, py, pw, ph, DiaryTheme.BRONZE);
        com.eternalcraft.gripeverde.client.render.PaperTexture.drawClean(g, px, py, pw, ph, com.eternalcraft.gripeverde.client.render.PaperTexture.PaperKind.ARCHIVE);


        int headerBottom = ExpedientHeader.draw(g, this.font, px, py, pw, this.title, reveal);

        cy = Math.max(cy, headerBottom - 2);
        ch = contentH() - Math.max(0, headerBottom - contentY() - 2);

        this.asteriskRects.clear();
        this.hiddenFolioRect = null;
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
            // v0.16.0: despues del ultimo renglon, un folio que no figura en la cronica.
            int fy = cy - scrollY + this.contentTotalH + 8;
            if (fy >= cy - 12 && fy <= cy + ch + 4) {
                boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
                String hidden = en ? "~ there is a room behind the last folio ~"
                                   : "~ hay una sala detras del ultimo folio ~";
                int hw = this.font.width(hidden);
                int hx = cx + (cw - hw) / 2;
                long pulse = System.currentTimeMillis();
                int alpha = 120 + (int)(Math.sin(pulse / 640.0) * 40);
                g.drawString(this.font, hidden, hx, fy, (alpha << 24) | 0x8E6A35, false);
                g.fill(hx, fy + 11, hx + hw, fy + 12, (Math.min(120, alpha) << 24) | 0x8E6A35);
                this.hiddenFolioRect = new int[]{ hx - 4, fy - 3, hw + 8, 18 };
            }
        }

        g.disableScissor();

        if (maxScroll > 0) {
            DiaryChrome.drawScrollThumb(g, px + pw - 10, cy, cy + ch, this.scrollY, this.maxScroll);
        }

        super.render(g, mouseX, mouseY, partialTick);

        try {
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, py - 12);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {}
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
            // v0.16.0: el encuadernador marco algunos con proposito.
            this.asteriskRects.add(new int[]{ x + indent - 12, y + 2, 8, 10 });

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
        if (button == 0) {
            try {
                // v0.16.0: las marcas del encuadernador
                for (int[] r : this.asteriskRects) {
                    if (mouseX >= r[0] && mouseX <= r[0] + r[2] && mouseY >= r[1] && mouseY <= r[1] + r[3]) {
                        binderMarksLit++;
                        com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                            com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_TICK.get(), 0.12f, 1.9f);
                        if (binderMarksLit >= 3) {
                            binderMarksLit = 0;
                            boolean first = com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                                com.eternalcraft.gripeverde.client.lore.Discoveries.BINDER_MARKS);
                            if (first) binderGlowUntilMs = System.currentTimeMillis() + 5000L;
                            com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                                com.eternalcraft.gripeverde.client.audio.ModSounds.UI_NOTIFICATION.get(), 0.16f, 0.80f);
                        }
                        return true;
                    }
                }
                // v0.16.0: el folio escondido abre la Sala de consulta
                if (this.hiddenFolioRect != null) {
                    int[] r = this.hiddenFolioRect;
                    if (mouseX >= r[0] && mouseX <= r[0] + r[2] && mouseY >= r[1] && mouseY <= r[1] + r[3]) {
                        com.eternalcraft.gripeverde.client.audio.UiSounds.back();
                        this.minecraft.setScreen(new DiaryArchiveScreen(this));
                        return true;
                    }
                }
            } catch (Throwable ignored) {}
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
