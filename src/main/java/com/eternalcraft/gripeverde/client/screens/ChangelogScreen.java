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
import java.util.BitSet;
import java.util.IdentityHashMap;
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

    private static final class RowLayout {
        final int height;
        final int indent;
        final int prefixWidth;
        final int dropCapWidth;
        final List<FormattedCharSequence> lines;

        RowLayout(int height, int indent, int prefixWidth, int dropCapWidth,
                  List<FormattedCharSequence> lines) {
            this.height = height;
            this.indent = indent;
            this.prefixWidth = prefixWidth;
            this.dropCapWidth = dropCapWidth;
            this.lines = lines;
        }
    }

    private static final class HitRect {
        int x;
        int y;
        int width;
        int height;

        void set(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private final Screen parent;
    private final List<Row> rows = new ArrayList<>();
    private final IdentityHashMap<Row, RowLayout> rowLayouts = new IdentityHashMap<>();
    private boolean loadError = false;
    private int scrollY = 0;
    private double smoothScrollY = 0.0D;
    private double targetScrollY = 0.0D;
    private static int lastScrollY = 0;  // Remembered across reopens
    private int maxScroll = 0;
    private int contentTotalH = 0;
    private int[] rowTops = new int[0];
    private int lastContentW = -1;
    private boolean draggingScrollbar = false;

    // v0.24.0: la cronica se abre donde quedo la pluma (entrada de la version instalada)
    private Row newVersionRow = null;
    private boolean arrivedFreshVersion = false;
    // v0.28.0: si el gabinete avanzo mas de una edicion desde la ultima
    // lectura, TODAS las entradas saltadas amanecen con lacre, no solo la
    // vigente (la pluma, en cambio, escribe unicamente la mas nueva).
    private final java.util.List<Row> freshRows = new java.util.ArrayList<>();
    private static final java.util.regex.Pattern VERSION_HEAD =
        java.util.regex.Pattern.compile("^(?:Version\\s+)?(\\d+)\\.(\\d+)\\.(\\d+)");

    // v0.29.0: la lupa de la bitacora — un cuadrito al pie, a la derecha,
    // donde el lector escribe una palabra y la cronica lo lleva de una
    // aparicion a la siguiente (Enter/Shift+Enter). Esc la cierra y la
    // bitacora olvida la pregunta. Se abre con "/" o Ctrl+F.
    private static final int SEARCH_W = 96;
    private static final int SEARCH_H = 12;
    private static final int SEARCH_MAX = 24;
    private boolean searchOpen = false;
    private final StringBuilder searchText = new StringBuilder();
    private final java.util.List<Integer> searchHits = new java.util.ArrayList<>();
    private final BitSet searchHitRows = new BitSet();
    private int searchHitPos = -1;

    // v0.27.0 (V4): la entrada recien llegada se escribe letra a letra en
    // esta visita, y solo en esta. Un gesto del lector (click) la completa
    // al instante; con movimiento reducido la hoja llega entera.
    // v0.29.0: — el rasguño acompaña por tramos.
    // v0.30.0 (feedback del owner): nada de "una parte si, otra no" — la
    // escritura cubre TODA la bitacora desde la entrada vigente hasta el
    // final: mientras el lector baja, la pluma lo va alcanzando; lo que
    // quedo mas de media pantalla por encima se considera ya impreso
    // (bajar de golpe con la barra jamas deja huecos), y el click sigue
    // completando todo al instante.
    private static final int PEN_CPS = 340;
    // Gracia para que el pliegue de 0.26.0 termine de destapar la hoja
    // antes del primer trazo (la pluma no escribe bajo la cortina).
    private static final long PEN_GRACE_MS = 300L;
    private boolean penActive = false;
    private int penRowStart = -1;
    private int penRowEnd = -1;
    private int penTotalChars = 0;
    private long penStartMs = 0L;
    private int penScratchChars = 0;
    private long lastPenScratchMs = 0L;

    // v0.16.0: las marcas del encuadernador y el folio escondido
    private final List<HitRect> asteriskRects = new ArrayList<>();
    private int visibleAsteriskRectCount = 0;
    private static int binderMarksLit = 0;
    private static long binderGlowUntilMs = 0L;
    private final HitRect hiddenFolioRect = new HitRect();
    private boolean hiddenFolioVisible = false;

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
        jumpToFreshVersion();

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
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("gripeverde", "changelog/" + lang + ".md");
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
                RowLayout cached = contentW == this.lastContentW ? this.rowLayouts.get(row) : null;
                return (cached != null ? cached : buildRowLayout(row, contentW)).height;
            }
        }
    }

    private RowLayout buildRowLayout(Row row, int contentW) {
        int indent = row.type == LT.BULLET ? 10 : 0;
        int prefixWidth = row.type == LT.BULLET ? this.font.width("\u2022 ") : 0;
        int dropCapWidth = 0;
        String text = row.text;
        if (row.type == LT.H1 && row.text.length() > 1) {
            String firstLetter = row.text.substring(0, 1);
            text = row.text.substring(1);
            dropCapWidth = (int) (this.font.width(firstLetter) * 1.6f) + 2;
        }
        int available = Math.max(20, contentW - indent - prefixWidth - dropCapWidth);
        List<FormattedCharSequence> lines = this.font.split(Component.literal(text), available);
        int height = Math.max(1, lines.size()) * lineHeightFor(row.type);
        return new RowLayout(height, indent, prefixWidth, dropCapWidth, lines);
    }

    private void recalcMaxScroll() {
        int cw = contentW();
        this.lastContentW = cw;
        this.rowLayouts.clear();
        this.rowTops = new int[rows.size() + 1];
        int total = 0;
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            this.rowTops[i] = total;
            if (r.type != LT.GAP && r.type != LT.SEPARATOR) {
                this.rowLayouts.put(r, buildRowLayout(r, cw));
            }
            total += rowHeight(r, cw);
        }
        this.rowTops[rows.size()] = total;
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

    /** First row whose bottom lies after the requested content-space offset. */
    private int firstRowAtOrAfter(int contentOffset) {
        int low = 0;
        int high = rows.size();
        while (low < high) {
            int mid = (low + high) >>> 1;
            if (this.rowTops[mid + 1] <= contentOffset) low = mid + 1;
            else high = mid;
        }
        return low;
    }

    private int rowOffset(int index) {
        return index >= 0 && index < this.rowTops.length ? this.rowTops[index] : this.contentTotalH;
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
    private int contentY() { return panelY() + PAD_Y + 10; }
    private int contentW() { return Math.max(40, panelW() - PAD_X * 2 - 8); }
    private int contentH() { return Math.max(20, panelH() - PAD_Y * 2 - 10); }

    @Override
    public void tick() {
        super.tick();
        tickSmoothScroll();
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
    }

    /**
     * v0.24.0: si el gabinete se actualizo desde la ultima lectura, la cronica
     * se abre directamente en la entrada de la version instalada y esa entrada
     * lleva un sello de lacre "NUEVO" durante esta visita.
     */
    private void jumpToFreshVersion() {
        this.newVersionRow = null;
        this.arrivedFreshVersion = false;
        this.freshRows.clear();
        String current;
        try { current = com.eternalcraft.gripeverde.GripeVerdeMod.VERSION; }
        catch (Throwable t) { return; }
        if (current == null || current.isBlank()) return;
        String seen;
        try { seen = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.lastSeenVersion(); }
        catch (Throwable t) { return; }
        if (current.equals(seen)) return;
        // v0.28.0: todas las entradas posteriores a la ultima leida llevan
        // lacre esta visita (si se saltaron ediciones, no solo la vigente).
        int[] seenTriple = parseTriple(seen);
        if (seenTriple != null) {
            for (Row r : rows) {
                if (r.type != LT.H1) continue;
                int[] v = parseTriple(r.text);
                if (v != null && compareTriple(v, seenTriple) > 0) {
                    freshRows.add(r);
                }
            }
        }
        int idx = -1;
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            if (r.type == LT.H1 && r.text.startsWith("Version " + current)) { idx = i; break; }
        }
        if (idx < 0) return; // cronica aun no tiene la entrada; nada que sellar
        int off = rowOffset(idx);
        off = Math.max(0, off - 6);
        this.newVersionRow = rows.get(idx);
        this.arrivedFreshVersion = true;
        preparePen(idx);
        lastScrollY = off;
        setScrollTarget(off, true);
        try {
            com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.setLastSeenVersion(current);
        } catch (Throwable ignored) {}
    }

    /**
     * v0.27.0: prepara la escritura a pluma de la entrada que empieza en la
     * cabecera h1Index (hasta la siguiente cabecera o el fin del documento).
     */
    private void preparePen(int h1Index) {
        penActive = false;
        penRowStart = -1;
        penRowEnd = -1;
        penTotalChars = 0;
        if (reducedMotion()) return; // la hoja llega entera, sin escritura
        if (h1Index < 0 || h1Index >= rows.size()) return;
        // v0.30.0: desde la entrada vigente HASTA EL FIN de la bitacora.
        int end = h1Index + 1;
        int total = rows.get(h1Index).text.length();
        while (end < rows.size()) {
            Row r = rows.get(end);
            if (r.type != LT.GAP && r.type != LT.SEPARATOR) total += r.text.length();
            end++;
        }
        if (total <= 0) return;
        penRowStart = h1Index;
        penRowEnd = end;
        penTotalChars = total;
        penActive = true;
        penStartMs = 0L; // arranca con el primer trazo en pantalla
        penScratchChars = 0;
    }

    private static boolean reducedMotion() {
        return com.eternalcraft.gripeverde.client.util.ClientAccessibility.reducedMotion();
    }

    private static boolean reducedFlashes() {
        return com.eternalcraft.gripeverde.client.util.ClientAccessibility.reduceFlashes();
    }

    // v0.28.0: triple de version. Acepta encabezados "Version X.Y.Z - ..."
    // y numeros desnudos ("X.Y.Z", como GripeVerdeMod.VERSION). null si no
    // se puede leer.
    private static int[] parseTriple(String text) {
        if (text == null) return null;
        try {
            java.util.regex.Matcher m = VERSION_HEAD.matcher(text.trim());
            if (!m.find()) return null;
            return new int[]{ Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)) };
        } catch (Throwable t) { return null; }
    }

    private static int compareTriple(int[] a, int[] b) {
        for (int i = 0; i < 3; i++) {
            if (a[i] != b[i]) return a[i] < b[i] ? -1 : 1;
        }
        return 0;
    }

    /** v0.24.0: sello de lacre junto a la entrada recien llegada. */
    private void drawNewSeal(GuiGraphics g, int x, int y, int maxW) {
        String txt = isEnglishSelected() ? "NEW" : "NUEVO";
        int tw = this.font.width(txt);
        int sx = x + maxW - tw - 12;
        int sy = y - 3;
        int wax = 0xFF7A2415;
        g.fill(sx - 2, sy - 1, sx + tw + 5, sy + 11, wax);
        g.fill(sx - 3, sy, sx + tw + 6, sy + 10, wax);
        g.fill(sx - 1, sy + 1, sx + tw + 4, sy + 9, 0xFF8B2A1C);
        g.fill(sx, sy + 1, sx + 3, sy + 2, 0x60E8A080);
        g.drawString(this.font, txt, sx + 1, sy + 2, 0xFFEAD9B0, false);
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

        int tw = com.eternalcraft.gripeverde.client.render.DiaryTypography.drawCentered(
            g, this.font, this.title, this.width / 2, py + 8, DiaryTheme.AMBER_FLAME);
        int tx = (this.width - tw) / 2;
        g.fill(tx - 4, py + 18, tx + tw + 4, py + 19, DiaryTheme.withAlpha(DiaryTheme.BRONZE, 150));

        this.visibleAsteriskRectCount = 0;
        this.hiddenFolioVisible = false;
        g.enableScissor(cx, cy, cx + cw, cy + ch);

        if (loadError) {
            g.drawString(this.font,
                Component.translatable("gripeverde.changelog.error").getString(),
                cx, cy + 4, DiaryTheme.BLOOD_FRESH, false);
        } else {
            // v0.27.0: presupuesto de tinta de la pluma para esta pasada.
            int penBudget = Integer.MAX_VALUE;
            if (penActive) {
                long nowPen = System.currentTimeMillis();
                if (penStartMs <= 0L) penStartMs = nowPen;
                long ageMs = Math.max(0L, nowPen - penStartMs - PEN_GRACE_MS);
                penBudget = (int) Math.min(penTotalChars, PEN_CPS * ageMs / 1000L);
                // El rasguño quedo de la pluma: un tramo de texto por golpe.
                int writtenNow = Math.min(penBudget, penTotalChars);
                if (writtenNow - penScratchChars >= 42 && nowPen - lastPenScratchMs > 120L) {
                    lastPenScratchMs = nowPen;
                    penScratchChars = writtenNow;
                    try {
                        com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                            com.eternalcraft.gripeverde.client.audio.ModSounds.UI_SCROLL.get(),
                            0.045f, 1.30f + (float) java.util.concurrent.ThreadLocalRandom
                                .current().nextDouble() * 0.25f);
                    } catch (Throwable ignored) {}
                }
            }
            int penConsumed = 0;
            int firstOffset = Math.max(0, scrollY - (penActive ? ch / 2 : 0) - 2);
            int ri = firstRowAtOrAfter(firstOffset);
            int y = cy + rowOffset(ri) - scrollY;
            for (; ri < rows.size(); ri++) {
                if (y > cy + ch + 2) break;
                Row row = rows.get(ri);
                int h = this.rowTops[ri + 1] - this.rowTops[ri];
                boolean inPenEntry = penActive && ri >= penRowStart && ri < penRowEnd;
                if (y + h >= cy - 2 && y <= cy + ch + 2) {
                    if (inPenEntry) {
                        int len = (row.type == LT.GAP || row.type == LT.SEPARATOR)
                            ? 0 : row.text.length();
                        // v0.30.0: lo que quedo mas de media pantalla por
                        // encima de la vista ya anda impreso y no le gasta
                        // tinta al presente (bajar de golpe jamas deja huecos).
                        boolean longGone = (y + h < cy - ch / 2);
                        int vis = longGone ? len
                            : Math.max(0, Math.min(len, penBudget - penConsumed));
                        if (vis >= len) {
                            renderRow(g, row, cx, y, cw);
                            if (freshRows.contains(row)) {
                                drawNewSeal(g, cx, y, cw);
                            }
                        } else {
                            // La pluma aun no llego: se dibuja lo escrito y el
                            // plumín al final del ultimo trazo.
                            if (len == 0) {
                                renderRow(g, row, cx, y, cw);
                            } else if (vis > 0) {
                                Row partial = new Row(row.type, row.text.substring(0, vis));
                                renderRow(g, partial, cx, y, cw);
                                drawPenNib(g, row, cx, y, cw, vis);
                            }
                        }
                        // El pasado lejano no gasta tinta del presente.
                        penConsumed += longGone ? 0 : len;
                    } else {
                        // v0.29.0: la aparicion actual de la lupa se asienta
                        // sobre un fondo tenue; las demas llevan piquito.
                        if (!searchHits.isEmpty() && searchText.length() > 0
                                && row.type != LT.GAP && row.type != LT.SEPARATOR) {
                            boolean isHit = searchHitRows.get(ri);
                            if (isHit) {
                                boolean isCurrent = searchHitPos >= 0
                                    && ri == searchHits.get(searchHitPos);
                                if (isCurrent) {
                                    g.fill(cx - 3, y - 1, cx + cw - 6, y + h - 1,
                                        DiaryTheme.withAlpha(DiaryTheme.BRONZE, 20));
                                }
                                g.fill(cx - 7, y + 1, cx - 4, y + Math.min(h - 2, 9),
                                    DiaryTheme.withAlpha(DiaryTheme.BRONZE,
                                        isCurrent ? 220 : 130));
                            }
                        }
                        renderRow(g, row, cx, y, cw);
                        if (freshRows.contains(row)) {
                            drawNewSeal(g, cx, y, cw);
                        }
                    }
                } else if (inPenEntry) {
                    // v0.30.0: misma regla de piso para filas fuera de vista —
                    // solo gasta tinta lo cercano a la hoja visible.
                    boolean longGone = (y + h < cy - ch / 2);
                    if (!longGone) {
                        penConsumed += (row.type == LT.GAP || row.type == LT.SEPARATOR)
                            ? 0 : row.text.length();
                    }
                }
                y += h;
            }
            if (penActive && penBudget >= penTotalChars) {
                penActive = false; // la tinta seco: escritura terminada
            }
            // v0.16.0: despues del ultimo renglon, un folio que no figura en la cronica.
            int fy = cy - scrollY + this.contentTotalH + 8;
            if (fy >= cy - 12 && fy <= cy + ch + 4) {
                boolean en = isEnglishSelected();
                String hidden = en ? "~ there is a room behind the last folio ~"
                                   : "~ hay una sala detras del ultimo folio ~";
                int hw = this.font.width(hidden);
                int hx = cx + (cw - hw) / 2;
                long pulse = System.currentTimeMillis();
                int alpha = reducedMotion() ? 120 : 120 + (int)(Math.sin(pulse / 640.0) * 40);
                g.drawString(this.font, hidden, hx, fy, (alpha << 24) | 0x8E6A35, false);
                g.fill(hx, fy + 11, hx + hw, fy + 12, (Math.min(120, alpha) << 24) | 0x8E6A35);
                this.hiddenFolioRect.set(hx - 4, fy - 3, hw + 8, 18);
                this.hiddenFolioVisible = true;
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

        // v0.28.0: el folio de la bitacora, centrado al pie del papel.
        // v0.29.0: comparte la franja con la lupa — si no hay sitio para
        // ambos, el folio se corre a la izquierda (nunca se pisan).
        if (!loadError) {
            int pages = Math.max(1, (int) Math.ceil((maxScroll + ch) / (double) ch));
            int cur = Math.max(1, Math.min(pages,
                (int) Math.floor(scrollY / (double) Math.max(1, ch)) + 1));
            String folio = "folio " + cur + " \u00b7 " + pages;
            int fw = this.font.width(folio);
            int fx = px + (pw - fw) / 2;
            int fy = py + ph - 16;
            int searchLeft = px + pw - 14 - SEARCH_W - 44; // lupa + contador
            if (fx + fw + 18 > searchLeft) fx = px + 14;
            int orn = DiaryTheme.withAlpha(DiaryTheme.BRONZE, 110);
            g.fill(fx - 14, fy + 4, fx - 6, fy + 5, orn);
            g.fill(fx + fw + 6, fy + 4, fx + fw + 14, fy + 5, orn);
            g.drawString(this.font, folio, fx, fy,
                DiaryTheme.withAlpha(DiaryTheme.SEPIA_LIGHT, 190), false);
        }

        // v0.29.0: la lupa de la bitacora, al pie a la derecha.
        drawSearchBox(g, px, py, pw, ph);

        super.render(g, mouseX, mouseY, partialTick);

        try {
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, py - 12);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {}
    }


    private static boolean isEnglishSelected() {
        String lang = "en_us";
        try { lang = Minecraft.getInstance().getLanguageManager().getSelected(); }
        catch (Throwable ignored) {}
        return lang == null || lang.startsWith("en_") || lang.equals("en_us");
    }

    /** v0.27.0: el plumín descansa al final del ultimo trazo escrito. */
    private void drawPenNib(GuiGraphics g, Row fullRow, int x, int y, int maxW, int vis) {
        String shown = fullRow.text.substring(0, Math.min(vis, fullRow.text.length()));
        int indent = (fullRow.type == LT.BULLET) ? 10 : 0;
        int lh = lineHeightFor(fullRow.type);
        int extraX = 0;
        int firstLineExtra = 0;
        int available;
        String body = shown;
        if (fullRow.type == LT.BULLET) {
            extraX = this.font.width("\u2022 ");
            available = Math.max(20, maxW - indent - extraX);
        } else if (fullRow.type == LT.H1) {
            if (shown.length() <= 1) {
                // Solo la capital grande: el plumín va pegado a ella.
                int capW = (int)(this.font.width(shown) * 1.6f) + 3;
                drawNibAt(g, x + indent + capW, y + 2);
                return;
            }
            int capW = (int)(this.font.width(shown.substring(0, 1)) * 1.6f) + 2;
            firstLineExtra = capW;
            available = Math.max(20, maxW - indent - capW);
            body = shown.substring(1);
        } else {
            available = Math.max(20, maxW - indent);
        }
        List<FormattedCharSequence> wrapped =
            this.font.split(Component.literal(body), available);
        if (wrapped.isEmpty()) {
            drawNibAt(g, x + indent, y);
            return;
        }
        String last = flatten(wrapped.get(wrapped.size() - 1));
        int lastW = this.font.width(last);
        int nx = x + indent + extraX + lastW
            + ((wrapped.size() == 1) ? firstLineExtra : 0) + 1;
        int ny = y + (wrapped.size() - 1) * lh;
        drawNibAt(g, nx, ny);
    }

    /** v0.29.0: la lupa, abierta o con su pista minima. */
    private void drawSearchBox(GuiGraphics g, int px, int py, int pw, int ph) {
        int sx = searchBoxX();
        int sy = py + ph - 19;
        boolean empty = searchText.length() == 0;
        // Contador de apariciones a la izquierda del cuadro.
        String count;
        if (empty) {
            count = isEnglishSelected() ? "/ or ctrl+f" : "/ o ctrl+f";
        } else if (searchHits.isEmpty()) {
            count = "0";
        } else {
            count = (searchHitPos + 1) + "/" + searchHits.size();
        }
        int cw2 = this.font.width(count);
        g.drawString(this.font, count, sx - 8 - cw2, sy + 3,
            DiaryTheme.withAlpha(DiaryTheme.BRONZE, empty ? 90 : 190), false);
        // v0.30.0: etiqueta de PAPEL clara con letra de tinta — antes era
        // tinta sobre tinta y la letra no se veia (errata reconocida).
        g.fill(sx - 1, sy - 1, sx + SEARCH_W + 1, sy + SEARCH_H + 1,
            DiaryTheme.withAlpha(DiaryTheme.BRONZE, searchOpen ? 200 : 90));
        g.fill(sx, sy, sx + SEARCH_W, sy + SEARCH_H,
            DiaryTheme.withAlpha(DiaryTheme.PAPER, 240));
        boolean en = isEnglishSelected();
        if (empty) {
            if (!searchOpen) {
                g.drawString(this.font, en ? "search" : "buscar", sx + 4, sy + 3,
                    DiaryTheme.withAlpha(DiaryTheme.SEPIA_LIGHT, 110), false);
            }
        } else {
            String shown = this.font.plainSubstrByWidth(searchText.toString(), SEARCH_W - 8);
            g.drawString(this.font, shown, sx + 4, sy + 3, DiaryTheme.INK, false);
        }
        if (searchOpen) {
            String base = this.font.plainSubstrByWidth(searchText.toString(), SEARCH_W - 8);
            int cxp = sx + 4 + this.font.width(base);
            boolean caretOn = reducedMotion() || reducedFlashes()
                || (System.currentTimeMillis() / 530L) % 2 == 0;
            if (caretOn) {
                g.fill(cxp + 1, sy + 2, cxp + 2, sy + SEARCH_H - 2,
                    (200 << 24) | 0x8E6A35);
            }
        }
    }

    private int searchBoxX() {
        return PANEL_MX + panelW() - 14 - SEARCH_W;
    }

    private boolean overSearchBox(double mouseX, double mouseY) {
        int sx = searchBoxX();
        int sy = PANEL_TOP + panelH() - 19;
        return mouseX >= sx - 1 && mouseX <= sx + SEARCH_W + 1
            && mouseY >= sy - 1 && mouseY <= sy + SEARCH_H + 1;
    }

    // Recuenta apariciones (texto simple, sin comodines) y salta a la primera.
    private void searchRecalc() {
        searchHits.clear();
        searchHitRows.clear();
        searchHitPos = -1;
        String needle = searchText.toString().toLowerCase(java.util.Locale.ROOT).trim();
        if (needle.isEmpty()) return;
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            if (r.type == LT.GAP || r.type == LT.SEPARATOR) continue;
            if (r.text.toLowerCase(java.util.Locale.ROOT).contains(needle)) {
                searchHits.add(i);
                searchHitRows.set(i);
            }
        }
        if (!searchHits.isEmpty()) {
            searchHitPos = 0;
            jumpToHit(0);
        }
    }

    private void jumpToHit(int dir) {
        if (searchHits.isEmpty()) return;
        if (dir != 0) {
            searchHitPos = Math.floorMod(searchHitPos + dir, searchHits.size());
            com.eternalcraft.gripeverde.client.audio.UiSounds.scroll();
        }
        int idx = searchHits.get(searchHitPos);
        int off = rowOffset(idx);
        setScrollTarget(Math.max(0, off - contentH() / 3), false);
    }

    private void drawNibAt(GuiGraphics g, int nx, int ny) {
        int a;
        if (reducedMotion() || reducedFlashes()) {
            a = 170; // movimiento o destellos reducidos: tinta quieta
        } else {
            a = 150 + (int)(55 * Math.sin(System.currentTimeMillis() / 170.0));
        }
        a = Math.max(60, Math.min(230, a));
        g.fill(nx + 1, ny, nx + 3, ny + 9, (a << 24) | 0x1A1108);       // caña
        g.fill(nx + 1, ny + 7, nx + 3, ny + 9, (a << 24) | 0x8E6A35);   // punta cargada
    }

    private static String flatten(FormattedCharSequence fcs) {
        StringBuilder sb = new StringBuilder();
        fcs.accept((idx, style, cp) -> { sb.appendCodePoint(cp); return true; });
        return sb.toString();
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

        RowLayout layout = this.rowLayouts.get(row);
        if (layout == null) layout = buildRowLayout(row, maxW);
        int indent = layout.indent;
        int lh = lineHeightFor(row.type);

        if (row.type == LT.BULLET) {
            g.drawString(this.font, "\u2022 ", x + indent, y, color, false);
            int ly = y;
            for (FormattedCharSequence fcs : layout.lines) {
                g.drawString(this.font, fcs, x + indent + layout.prefixWidth, ly, color, false);
                ly += lh;
            }
        } else if (row.type == LT.H1 && row.text.length() > 1) {
            // Drop cap: first letter drawn larger in dark ink
            String firstLetter = row.text.substring(0, 1);
            int capColor = DiaryTheme.INK;

            // Nota al margen: asterisco decorativo a la izquierda del H1
            int marginColor = DiaryTheme.withAlpha(DiaryTheme.BRONZE, 180);
            g.drawString(this.font, "*", x + indent - 10, y + 4, marginColor, false);
            // v0.16.0: el encuadernador marco algunos con proposito.
            if (this.visibleAsteriskRectCount == this.asteriskRects.size()) {
                this.asteriskRects.add(new HitRect());
            }
            this.asteriskRects.get(this.visibleAsteriskRectCount++).set(
                x + indent - 12, y + 2, 8, 10);

            g.pose().pushPose();
            float capScale = 1.6f;
            g.pose().translate(x + indent, y - 1, 0);
            g.pose().scale(capScale, capScale, 1f);
            g.drawString(this.font, firstLetter, 0, 0, capColor, false);
            g.pose().popPose();

            int ly = y;
            for (int wi = 0; wi < layout.lines.size(); wi++) {
                int lx = (wi == 0) ? x + indent + layout.dropCapWidth : x + indent;
                g.drawString(this.font, layout.lines.get(wi), lx, ly, color, false);
                ly += lh;
            }
        } else {
            int ly = y;
            for (FormattedCharSequence fcs : layout.lines) {
                g.drawString(this.font, fcs, x + indent, ly, color, false);
                ly += lh;
            }
        }
    }

    private long lastScrollSoundMs = 0L;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // v0.29.0: tocar la lupa le da la palabra (y no propaga el gesto).
        if (button == 0 && overSearchBox(mouseX, mouseY)) {
            searchOpen = true;
            return true;
        }
        if (button == 0 && overScrollbar(mouseX, mouseY)) {
            this.draggingScrollbar = true;
            setScrollFromMouse(mouseY);
            return true;
        }
        if (button == 0) {
            // v0.27.0: un gesto del lector y la pluma termina de golpe.
            this.penActive = false;
            try {
                // v0.16.0: las marcas del encuadernador
                for (int i = 0; i < this.visibleAsteriskRectCount; i++) {
                    HitRect r = this.asteriskRects.get(i);
                    if (mouseX >= r.x && mouseX <= r.x + r.width
                            && mouseY >= r.y && mouseY <= r.y + r.height) {
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
                if (this.hiddenFolioVisible) {
                    HitRect r = this.hiddenFolioRect;
                    if (mouseX >= r.x && mouseX <= r.x + r.width
                            && mouseY >= r.y && mouseY <= r.y + r.height) {
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
    public boolean charTyped(char codePoint, int modifiers) {
        // v0.29.0: con la lupa abierta, toda letra es pregunta.
        if (searchOpen && searchText.length() < SEARCH_MAX
                && (Character.isLetterOrDigit(codePoint) || codePoint == ' '
                || codePoint == '-' || codePoint == '.' )) {
            searchText.append(codePoint);
            searchRecalc();
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // v0.29.0: la lupa manda mientras esta abierta.
        if (searchOpen) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                // La cruz la cierra y la bitacora olvida la pregunta.
                searchOpen = false;
                searchText.setLength(0);
                searchHits.clear();
                searchHitRows.clear();
                searchHitPos = -1;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                jumpToHit(hasShiftDown() ? -1 : 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (searchText.length() > 0) {
                    searchText.setLength(searchText.length() - 1);
                    searchRecalc();
                }
                return true;
            }
        } else if (keyCode == GLFW.GLFW_KEY_SLASH && modifiers == 0) {
            searchOpen = true;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_F && hasControlDown()) {
            searchOpen = true;
            return true;
        }
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
