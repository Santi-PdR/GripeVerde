package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.widgets.SlimButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
    private static final int PAD_Y      = 30;
    private static final int CLOSE_W    = 130;
    private static final int CLOSE_H    = 28;
    private static final int SCROLL_SPD = 28;

    // v0.182.0: la cronica respira. Cada renglon gana un pixel, los titulos dos y
    // el hueco entre parrafos tres; el filete de version pasa de 12 a 16 para que
    // las entradas no se toquen.
    private static final int LINE_H    = 12;
    private static final int H1_LINE_H = 18;
    private static final int H2_LINE_H = 14;
    private static final int BOLD_LINE_H = 14;
    private static final int GAP_H     = 13;
    private static final int SEP_H     = 16;

    private enum LT { H1, H2, BULLET, TEXT, GAP, SEPARATOR, BOLD }

    /**
     * v0.199.0: las lineas en que se parte un renglon solo dependen de su texto
     * y del ancho disponible. Se guardan y se reusan en todos los cuadros, y
     * tambien al medir el alto del renglon, que hacia el mismo corte.
     */
    private static final class WrapCache {
        private String source;
        private int width = -1;
        private List<FormattedCharSequence> lines;

        List<FormattedCharSequence> of(Font font, String text, int available) {
            if (this.lines != null && this.width == available && this.source == text) {
                return this.lines;
            }
            List<FormattedCharSequence> wrapped = font.split(Component.literal(text), available);
            this.source = text;
            this.width = available;
            this.lines = wrapped;
            return wrapped;
        }
    }

    private static final class Row {
        final LT type;
        final String text;
        /** El renglon entero: lo que miden los altos y lo que escribe el cuerpo. */
        final WrapCache whole = new WrapCache();
        /** El cuerpo del H1, sin la capital grande. */
        final WrapCache body = new WrapCache();
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
    private int penWrittenChars = 0;
    private long penLastUpdateMs = 0L;
    private long penGraceRemainingMs = PEN_GRACE_MS;
    private long penRateRemainder = 0L;
    private int penScratchChars = 0;
    private long lastPenScratchMs = 0L;

    // v0.184.0: fuera los secretos de clic (marcas del encuadernador y folio
    // escondido). Los unicos huevos que quedan son los del fondo.

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
        this.rowHeightCacheW = -1;   // v0.184.0: la cronica cambio, alto nuevo
        this.lastContentW = -1;

        loadAndParse();
        recalcMaxScroll();
        jumpToFreshVersion();

        int closeW = Math.max(84, Math.min(CLOSE_W, panelW() - 24));
        int bx = panelX() + (panelW() - closeW) / 2;
        int by = this.height - CLOSE_H - 10;
        this.addRenderableWidget(new SlimButton(bx, by, closeW, CLOSE_H,
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

    // v0.184.0: alto de cada renglon memorizado. Antes, para saber donde cae el
    // scroll, se volvia a partir el texto de TODA la cronica en cada frame:
    // miles de cortes de linea por cuadro en la cronica completa. Ahora se
    // mide una vez por ancho de hoja y se guarda.
    private int[] rowHeightCache = new int[0];
    private int rowHeightCacheW = -1;

    // v0.199.0: el folio del pie se armaba y se medía en cada cuadro; ahora se
    // guarda y sólo se rehace cuando cambia la página o la fuente.
    private String countSearchText = "";
    private int countSearchW = 0;
    private int countSearchHits = -1;
    private int countSearchPos = -2;
    private boolean countSearchEmpty = false;
    private Font countSearchFont;

    private String cachedFolio = "";
    private int cachedFolioWidth = 0;
    private int cachedFolioCur = -1;
    private int cachedFolioPages = -1;
    private Font cachedFolioFont;
    private boolean foliosReady = false;

    private void ensureRowHeights(int contentW) {
        if (this.rowHeightCacheW == contentW && this.rowHeightCache.length == rows.size()) return;
        int n = rows.size();
        int[] cache = new int[n];
        for (int i = 0; i < n; i++) cache[i] = rowHeight(rows.get(i), contentW);
        this.rowHeightCache = cache;
        this.rowHeightCacheW = contentW;
    }

    private int heightAt(int index, int contentW) {
        ensureRowHeights(contentW);
        if (index < 0 || index >= this.rowHeightCache.length) return 0;
        return this.rowHeightCache[index];
    }

    private int rowHeight(Row row, int contentW) {
        switch (row.type) {
            case GAP:       return GAP_H;
            case SEPARATOR: return SEP_H;
            default: {
                int indent = (row.type == LT.BULLET) ? 10 : 0;
                int prefixW = (row.type == LT.BULLET) ? DiaryChrome.widthCached(this.font, "\u2022 ") : 0;
                int available = Math.max(20, contentW - indent - prefixW);
                int lines = Math.max(1, row.whole.of(this.font, row.text, available).size());
                return lines * lineHeightFor(row.type);
            }
        }
    }

    private void recalcMaxScroll() {
        int cw = contentW();
        this.lastContentW = cw;
        int total = 0;
        ensureRowHeights(cw);
        for (int h : this.rowHeightCache) total += h;
        this.contentTotalH = total;
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
        if (snap || com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.hasActiveWorld()) {
            smoothScrollY = targetScrollY;
        }
        scrollY = (int)Math.round(smoothScrollY);
    }

    private void tickSmoothScroll() {
        if (com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.hasActiveWorld()) {
            smoothScrollY = targetScrollY;
            clampScroll();
            return;
        }
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

    /** v0.182.0: en ventanas angostas el margen fijo se comia el panel. */
    private int panelMx() { return Math.max(12, Math.min(PANEL_MX, this.width / 8)); }
    private int panelX() { return panelMx(); }
    private int panelY() { return PANEL_TOP; }
    private int panelW() { return Math.max(120, this.width - panelMx() * 2); }
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
        int cw = contentW();
        int off = 0;
        for (int i = 0; i < idx; i++) off += heightAt(i, cw);
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
        penWrittenChars = 0;
        penLastUpdateMs = 0L;
        penGraceRemainingMs = PEN_GRACE_MS;
        penRateRemainder = 0L;
        penScratchChars = 0;
        lastPenScratchMs = 0L;
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
    }

    private boolean isPenCursorVisible(int progress, int cx, int cy, int ch, int cw) {
        if (penRowStart < 0 || penRowEnd <= penRowStart) return false;
        int rowY = cy - scrollY;
        for (int i = 0; i < penRowStart; i++) rowY += heightAt(i, cw);

        int consumed = 0;
        for (int i = penRowStart; i < penRowEnd; i++) {
            Row row = rows.get(i);
            int height = heightAt(i, cw);
            int length = (row.type == LT.GAP || row.type == LT.SEPARATOR)
                ? 0 : row.text.length();
            if (length > 0 && progress < consumed + length) {
                return rowY + height >= cy && rowY <= cy + ch;
            }
            consumed += length;
            rowY += height;
        }
        return false;
    }

    private static boolean reducedMotion() {
        try {
            return com.eternalcraft.gripeverde.config.ClientConfig.REDUCED_MOTION.get()
                || com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.hasActiveWorld();
        } catch (Throwable t) { return false; }
    }

    private static boolean reducedFlashes() {
        try {
            return com.eternalcraft.gripeverde.config.ClientConfig.REDUCE_FLASHES.get()
                || com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.hasActiveWorld();
        } catch (Throwable t) { return false; }
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

    /**
     * v0.184.0: sello de lacre de la entrada recien llegada.
     *
     * Antes se dibujaba pegado al borde derecho y por encima del renglon; en
     * ventanas bajas el recorte de la hoja le comia la mitad de arriba y se
     * leia "NUEV". Ahora el sello se mide completo y se mete a la fuerza
     * dentro del area visible, asi siempre se lee entero.
     */
    private void drawFreshStamp(GuiGraphics g, int x, int y, int maxW, int clipTop, int clipBottom) {
        String txt = isEnglishSelected() ? "NEW" : "NUEVO";
        int tw = DiaryChrome.widthCached(this.font, txt);
        int w = tw + 9;
        int h = 12;
        int sx = x + maxW - w - 6;
        int sy = y - 3;
        if (sy + h > clipBottom - 1) sy = clipBottom - 1 - h;
        if (sy < clipTop + 1) sy = clipTop + 1;
        if (sy + h > clipBottom - 1) return;   // no cabe entero: mejor sin sello que cortado
        int wax = 0xFF7A2415;
        g.fill(sx, sy, sx + w, sy + h, wax);
        g.fill(sx + 1, sy + 1, sx + w - 1, sy + h - 1, 0xFF8B2A1C);
        g.fill(sx + 1, sy + 1, sx + 4, sy + 2, 0x60E8A080);
        g.drawString(this.font, txt, sx + (w - tw) / 2, sy + 2, 0xFFEAD9B0, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int px = panelX(), py = panelY(), pw = panelW(), ph = panelH();
        int cx = contentX(), cy = contentY(), cw = contentW(), ch = contentH();

        if (cw != lastContentW) {
            recalcMaxScroll();
        }

        g.fill(0, 0, this.width, this.height, 0xD0100D08);

        com.eternalcraft.gripeverde.client.render.PaperTexture.drawSheet(g, px, py, pw, ph, com.eternalcraft.gripeverde.client.render.PaperTexture.PaperKind.ARCHIVE);
        g.fill(px,          py,          px + pw, py + 1,      DiaryTheme.withAlpha(DiaryTheme.BRONZE, 180));
        g.fill(px,          py + ph - 1, px + pw, py + ph,     DiaryTheme.withAlpha(DiaryTheme.BRONZE, 180));
        g.fill(px,          py,          px + 1,  py + ph,     DiaryTheme.withAlpha(DiaryTheme.BRONZE, 180));
        g.fill(px + pw - 1, py,          px + pw, py + ph,     DiaryTheme.withAlpha(DiaryTheme.BRONZE, 180));

        int tw = com.eternalcraft.gripeverde.client.render.DiaryTypography.width(this.font, this.title);
        int tx = (this.width - tw) / 2;
        g.drawString(this.font, this.title, tx, py + 8, DiaryTheme.AMBER_FLAME, false);
        g.fill(tx - 4, py + 18, tx + tw + 4, py + 19, DiaryTheme.withAlpha(DiaryTheme.BRONZE, 150));

        g.enableScissor(cx, cy, cx + cw, cy + ch);

        if (loadError) {
            g.drawString(this.font,
                DiaryChrome.tr("gripeverde.changelog.error"),
                cx, cy + 4, DiaryTheme.BLOOD_FRESH, false);
        } else {
            // Progress only while the next letter is on the visible sheet. A delayed
            // frame is capped so the nib never jumps across unseen paragraphs.
            int penBudget = penActive ? penWrittenChars : Integer.MAX_VALUE;
            boolean penCursorVisible = false;
            if (penActive) {
                long nowPen = System.currentTimeMillis();
                if (penLastUpdateMs <= 0L) penLastUpdateMs = nowPen;
                long elapsed = Math.max(0L, Math.min(100L, nowPen - penLastUpdateMs));
                penLastUpdateMs = nowPen;
                penCursorVisible = isPenCursorVisible(
                    penWrittenChars, cx, cy, ch, cw);
                if (penCursorVisible) {
                    long graceUsed = Math.min(elapsed, penGraceRemainingMs);
                    penGraceRemainingMs -= graceUsed;
                    elapsed -= graceUsed;
                    if (elapsed > 0L) {
                        long rate = elapsed * PEN_CPS + penRateRemainder;
                        penWrittenChars += (int)(rate / 1000L);
                        penRateRemainder = rate % 1000L;
                    }
                }
                penBudget = Math.min(penTotalChars, penWrittenChars);

                // A quiet scratch only accompanies letters currently being written in view.
                int writtenNow = Math.min(penBudget, penTotalChars);
                if (penCursorVisible && writtenNow - penScratchChars >= 72
                        && nowPen - lastPenScratchMs > 220L) {
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
            int y = cy - scrollY;
            for (int ri = 0; ri < rows.size(); ri++) {
                Row row = rows.get(ri);
                int h = heightAt(ri, cw);
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
                                drawFreshStamp(g, cx, y, cw, cy, cy + ch);
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
                            boolean isHit = searchHits.contains(ri);
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
                            drawFreshStamp(g, cx, y, cw, cy, cy + ch);
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
            if (!foliosReady || cachedFolioCur != cur || cachedFolioPages != pages
                    || cachedFolioFont != this.font) {
                cachedFolio = "folio " + cur + " \u00b7 " + pages;
                cachedFolioWidth = this.font.width(cachedFolio);
                cachedFolioCur = cur;
                cachedFolioPages = pages;
                cachedFolioFont = this.font;
                foliosReady = true;
            }
            String folio = cachedFolio;
            int fw = cachedFolioWidth;
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
        return com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
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
            extraX = DiaryChrome.widthCached(this.font, "\u2022 ");
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
        // v0.200.0: la frase del contador se armaba y se medida en cada cuadro.
        if (this.countSearchEmpty != empty || this.countSearchHits != searchHits.size()
                || this.countSearchPos != searchHitPos || this.countSearchFont != this.font) {
            this.countSearchEmpty = empty;
            this.countSearchHits = searchHits.size();
            this.countSearchPos = searchHitPos;
            this.countSearchFont = this.font;
            if (empty) {
                this.countSearchText = isEnglishSelected() ? "/ or ctrl+f" : "/ o ctrl+f";
            } else if (searchHits.isEmpty()) {
                this.countSearchText = "0";
            } else {
                this.countSearchText = (searchHitPos + 1) + "/" + searchHits.size();
            }
            this.countSearchW = this.font.width(this.countSearchText);
        }
        String count = this.countSearchText;
        int cw2 = this.countSearchW;
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
            boolean caretOn = reducedFlashes()
                || (System.currentTimeMillis() / 530L) % 2 == 0;
            if (caretOn) {
                g.fill(cxp + 1, sy + 2, cxp + 2, sy + SEARCH_H - 2,
                    (200 << 24) | 0x8E6A35);
            }
        }
    }

    private int searchBoxX() {
        return panelX() + panelW() - 14 - SEARCH_W;
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
        searchHitPos = -1;
        String needle = searchText.toString().toLowerCase(java.util.Locale.ROOT).trim();
        if (needle.isEmpty()) return;
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            if (r.type == LT.GAP || r.type == LT.SEPARATOR) continue;
            if (r.text.toLowerCase(java.util.Locale.ROOT).contains(needle)) {
                searchHits.add(i);
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
        int cw = contentW();
        int off = 0;
        for (int i = 0; i < idx; i++) off += heightAt(i, cw);
        setScrollTarget(Math.max(0, off - contentH() / 3), false);
    }

    private void drawNibAt(GuiGraphics g, int nx, int ny) {
        int a;
        if (reducedFlashes()) {
            a = 170; // destellos reducidos: tinta quieta
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

        int indent = (row.type == LT.BULLET) ? 10 : 0;
        int lh = lineHeightFor(row.type);

        if (row.type == LT.BULLET) {
            g.drawString(this.font, "\u2022 ", x + indent, y, color, false);
            int prefixW = DiaryChrome.widthCached(this.font, "\u2022 ");
            int available = Math.max(20, maxW - indent - prefixW);
            List<FormattedCharSequence> wrapped = row.whole.of(this.font, row.text, available);
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
            List<FormattedCharSequence> wrapped = row.body.of(this.font, rest, available);
            int ly = y;
            for (int wi = 0; wi < wrapped.size(); wi++) {
                int lx = (wi == 0) ? x + indent + capW : x + indent;
                g.drawString(this.font, wrapped.get(wi), lx, ly, color, false);
                ly += lh;
            }
        } else {
            int available = Math.max(20, maxW - indent);
            List<FormattedCharSequence> wrapped = row.whole.of(this.font, row.text, available);
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
        if (button == 0 && !DiaryChrome.overAnyWidget(this, mouseX, mouseY)) {
            // v0.27.0: un gesto del lector y la pluma termina de golpe.
            this.penActive = false;
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
        // v0.182.0: sin cronica que recorrer, la rueda no hace ruido ni se queda el gesto.
        if (this.maxScroll <= 0) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }
        double before = this.targetScrollY;
        scrollBy((int)(-Math.signum(delta) * SCROLL_SPD));
        if (Math.abs(before - this.targetScrollY) < 0.1D) {
            return true;
        }
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
        boolean english = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        String[] pool = english ? CHANGELOG_TITLES_EN : CHANGELOG_TITLES_ES;
        return pool[CHANGELOG_RNG.nextInt(pool.length)];
    }
}
