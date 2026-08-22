package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.lore.ChroniclerMemory;
import com.eternalcraft.gripeverde.client.lore.Discoveries;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.ExpedientHeader;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryProgressBar;
import com.eternalcraft.gripeverde.client.widgets.DiaryTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * La Sala de consulta (el Archivo).
 *
 * Detras del ultimo folio de la cronica hay una sala con fichas. Cada ficha
 * es algo que el lector encontro; las que faltan son cosas que todavia no.
 * El gabinete mide al lector por sus fichas.
 *
 * v0.16.0
 */
public class DiaryArchiveScreen extends Screen {

    public static final String DOC_NONE = "";
    public static final String DOC_TELEGRAM = "telegram";
    public static final String DOC_HOLLOWAY = "holloway";
    public static final String DOC_FORBIDDEN = "forbidden";

    private static String initialDoc = DOC_NONE;
    public static void setInitialDoc(String doc) { initialDoc = doc == null ? DOC_NONE : doc; }

    private final Screen parent;

    private int panelX, panelY, panelW, panelH;
    private String viewingDoc = DOC_NONE;
    private int hoverCard = -1;
    private int lastMouseX = -1, lastMouseY = -1;
    private long openedAtMs = 0L;

    // v0.21.11: micro-animacion de las fichas (patron EasedValue de DiaryButton)
    private final com.eternalcraft.gripeverde.client.util.EasedValue cardHover =
        new com.eternalcraft.gripeverde.client.util.EasedValue(5.0f);
    private int animHoverCard = -1;

    // v0.21.11: consulta por nombre/pista/id en la sala de consulta
    private DiaryTextField searchField = null;

    private static final int COLS = 5;
    private static final int CARD_H = 22;
    private static final int CARD_GAP = 5;

    public DiaryArchiveScreen(Screen parent) {
        super(Component.literal(Discoveries.isEnglish() ? "Reading Room" : "Sala de consulta"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int desiredW = Math.min(440, this.width - 32);
        int desiredH = Math.min(404, this.height - 28);
        panelW = Math.max(280, desiredW);
        panelH = Math.max(240, desiredH);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        int backW = 140;
        this.addRenderableWidget(new DiaryButton(
            panelX + (panelW - backW) / 2, panelY + panelH - 34, backW, 24,
            Component.translatable("gripeverde.options.button.done"),
            DiaryButton.Icon.DOOR, DiaryButton.Variant.PRIMARY,
            DiaryButton.SoundProfile.BACK,
            this::onClose));

        if (!DOC_NONE.equals(initialDoc)) {
            boolean allowed =
                (DOC_TELEGRAM.equals(initialDoc) && ChroniclerMemory.isDiscovered(Discoveries.CODE_1897)) ||
                (DOC_HOLLOWAY.equals(initialDoc) && ChroniclerMemory.isDiscovered(Discoveries.CODE_HOLLOWAY)) ||
                (DOC_FORBIDDEN.equals(initialDoc) && ChroniclerMemory.discoveredCount() >= 38);
            if (allowed) viewingDoc = initialDoc;
            initialDoc = DOC_NONE;
        }
        this.openedAtMs = System.currentTimeMillis();

        // v0.21.4: un papel cae al abrir la sala de consulta.
        try {
            UiSounds.playCustom(com.eternalcraft.gripeverde.client.audio.ModSounds.UI_PAPER_FALL.get(),
                0.11f, 1.18f);
        } catch (Throwable ignored) {}

        // v0.21.5: campo de consulta del catalogo (esquina superior derecha, junto al titulo).
        int progressBoxX = panelX + panelW - 162;
        this.searchField = new DiaryTextField(this.font, progressBoxX, panelY + 18, 142, 16,
            Component.literal("search"));
        this.searchField.setMaxLength(24);
        this.addRenderableWidget(this.searchField);

        // v0.21.5: marginalia cuando casi no quedan fichas fuera.
        try {
            if (ChroniclerMemory.discoveredCount() >= 44) {
                com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                    com.eternalcraft.gripeverde.client.lore.Marginalia.N_MANY_CARDS);
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void tick() {
        super.tick();
        QuarantineSceneRenderer.tick(1f / 20f);
        // v0.21.11: suavizar el hover de la ficha hacia la ficha bajo el cursor
        boolean same = this.animHoverCard == this.hoverCard;
        this.cardHover.setTarget(this.hoverCard >= 0);
        this.cardHover.tick(1f / 20f);
        if (!same && this.hoverCard >= 0) {
            this.animHoverCard = this.hoverCard;
        }
    }

    @Override
    public void renderBackground(GuiGraphics g) { /* bypass dirt */ }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
        // El campo de consulta no se dibuja sobre los documentos abiertos.
        if (this.searchField != null) {
            this.searchField.visible = DOC_NONE.equals(viewingDoc);
        }

        float reveal = DiaryChrome.reveal(this.openedAtMs, 260L);
        DiaryChrome.renderPanelBase(g, panelX, panelY, panelW, panelH, DiaryTheme.BRONZE, PaperTexture.PaperKind.ARCHIVE);

        boolean en = Discoveries.isEnglish();
        int cx = panelX + panelW / 2;
        int found = ChroniclerMemory.discoveredCount();
        int total = Discoveries.ALL.length;

        if (!DOC_NONE.equals(viewingDoc)) {
            renderDocument(g, en);
            super.render(g, mouseX, mouseY, partialTick);
            return;
        }

        int headerBottom = ExpedientHeader.draw(g, this.font, panelX, panelY, panelW,
            en ? "EXPEDIENT XIV" : "EXPEDIENTE XIV",
            Component.literal(en ? "Reading Room" : "Sala de consulta"),
            en ? "- the chronicler's cards -" : "- las fichas del cronista -",
            en ? "EC-QV/1897 · CONSULTATION FILE" : "EC-QV/1897 · FICHA DE CONSULTA",
            en ? "Recovered observations filed by the same diseased office." : "Observaciones recuperadas y archivadas por la misma oficina enferma.",
            found >= total ? (en ? "COMPLETE" : "COMPLETO") : (en ? "CATALOGUED" : "CATALOGADO"),
            DiaryTheme.BRONZE,
            reveal);

        int progressBoxX = panelX + panelW - 162;

        int gridX = panelX + 18;
        int gridW = (panelX + panelW - 18) - gridX;
        int cw = (gridW - (COLS - 1) * CARD_GAP) / COLS;
        int gridY = headerBottom;
        hoverCard = -1;

        // v0.21.5: la sala filtra el catalogo por nombre, pista o id.
        String q = this.searchField == null ? "" : this.searchField.getValue()
            .toLowerCase(java.util.Locale.ROOT).trim();
        java.util.List<Integer> visible = new java.util.ArrayList<>();
        if (q.isEmpty()) {
            for (int i = 0; i < Discoveries.ALL.length; i++) visible.add(i);
        } else {
            for (int i = 0; i < Discoveries.ALL.length; i++) {
                String id = Discoveries.ALL[i];
                String name = Discoveries.nameFor(id).toLowerCase(java.util.Locale.ROOT);
                String hint = Discoveries.hintFor(id).toLowerCase(java.util.Locale.ROOT);
                if (name.contains(q) || hint.contains(q)
                        || id.toLowerCase(java.util.Locale.ROOT).contains(q)) {
                    visible.add(i);
                }
            }
        }
        for (int k = 0; k < visible.size(); k++) {
            int i = visible.get(k);
            int col = k % COLS;
            int row = k / COLS;
            int x = gridX + col * (cw + CARD_GAP);
            int y = gridY + row * (CARD_H + CARD_GAP);
            String id = Discoveries.ALL[i];
            boolean disc = ChroniclerMemory.isDiscovered(id);
            boolean hover = mouseX >= x && mouseX < x + cw && mouseY >= y && mouseY < y + CARD_H;
            if (hover) hoverCard = i;
            drawArchiveCard(g, x, y, cw, id, disc, hover, i + 1);
        }

        if (this.searchField != null && !q.isEmpty()) {
            String count = visible.size() + " / " + Discoveries.ALL.length;
            g.drawString(this.font, count, progressBoxX, panelY + 38,
                visible.isEmpty() ? 0xFF6E1F16 : DiaryTheme.withAlpha(DiaryTheme.INK, 160), false);
        }

        int infoY = gridY + ((visible.size() + COLS - 1) / COLS) * (CARD_H + CARD_GAP) + 10;
        if (hoverCard >= 0) {
            String id = Discoveries.ALL[hoverCard];
            if (ChroniclerMemory.isDiscovered(id)) {
                DiaryChrome.drawCenteredString(g, this.font, Discoveries.nameFor(id), cx, infoY, DiaryTheme.INK);
            } else {
                DiaryChrome.drawCenteredString(g, this.font, Discoveries.hintFor(id), cx, infoY, DiaryChrome.INK_FLAVOR);
            }
        } else if (q.isEmpty()) {
            // v0.21.13: al completar el archivo, el footer cambia y el sello se muestra.
            boolean complete = found >= total;
            String msg = complete
                ? (en ? "The archive is complete." : "El archivo esta completo.")
                : (en ? "The missing cards are still out there." : "Las fichas que faltan siguen ahi fuera.");
            DiaryChrome.drawCenteredString(g, this.font, msg, cx, infoY,
                complete ? DiaryTheme.INK : DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 120));
            if (complete) {
                int mw = this.font.width(msg);
                int sealCx = cx + mw / 2 + 20;
                String sig = "SIGILLUM";
                g.drawString(this.font, sig, sealCx - this.font.width(sig) / 2, infoY - 15, 0xFF8A6A2D, false);
                drawCompletionSeal(g, sealCx, infoY + 6, 8);
            }
        } else {
            DiaryChrome.drawCenteredString(g, this.font, en ? (visible.isEmpty() ? "Nothing matches the query." : "Only these cards match.")
                       : (visible.isEmpty() ? "Nada coincide con la consulta." : "Solo estas fichas coinciden."),
                cx, infoY, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 160));
        }

        int docsY = infoY + 18;
        int dx = panelX + 24;
        if (ChroniclerMemory.isDiscovered(Discoveries.CODE_1897)) {
            drawPaperDoc(g, dx, docsY + 4, en ? "The night of the outbreak" : "La noche del brote", mouseX, mouseY);
            dx += 130;
        }
        if (ChroniclerMemory.isDiscovered(Discoveries.CODE_HOLLOWAY)) {
            drawPaperDoc(g, dx, docsY + 4, en ? "Dr. Holloway's report" : "Informe del Dr. Holloway", mouseX, mouseY);
            dx += 130;
        }
        if (ChroniclerMemory.discoveredCount() >= 38) {
            drawPaperDoc(g, dx, docsY + 4, en ? "A black card" : "Una ficha negra", mouseX, mouseY);
        }

        super.render(g, mouseX, mouseY, partialTick);
        com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
        com.eternalcraft.gripeverde.client.lore.Marginalia.draw(g, panelX, panelY, panelW, panelH);
        if (ChroniclerMemory.isDiscovered(Discoveries.ARCHIVE_THINNING)
                && ChroniclerMemory.isDiscovered(Discoveries.THEME_NEVER_PLAYED)) {
            // v0.21.3: cuando el archivo confiesa dos fantasmas, pide el tercero.
            Discoveries.discover(Discoveries.BG_TOWN_MEMORY);
        }
        if (ChroniclerMemory.isDiscovered(Discoveries.BG_TOWN_MEMORY)
                && ChroniclerMemory.isDiscovered(Discoveries.ARCHIVE_THINNING)
                && ChroniclerMemory.isDiscovered(Discoveries.THEME_NEVER_PLAYED)) {
            // v0.21.5: cuando confiesan tres, el cuarto recuerda la mesa sin uso.
            Discoveries.discover(Discoveries.BUTTONS_NEVER_PRESSED);
        }
        // v0.21.13: al completar el archivo, el cronista lo sella (una sola vez).
        try {
            if (ChroniclerMemory.discoveredCount() >= Discoveries.ALL.length
                    && ChroniclerMemory.counter("cabinet_complete") == 0) {
                ChroniclerMemory.setCounter("cabinet_complete", 1);
                com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                    com.eternalcraft.gripeverde.client.lore.Marginalia.N_ARCHIVE_COMPLETE);
                com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                    com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_BELL.get(), 0.22f, 0.55f);
            }
        } catch (Throwable ignored) {}
        Discoveries.drawToast(g, this.width);
    }

    private void drawArchiveCard(GuiGraphics g, int x, int y, int w, String id, boolean discovered, boolean hover, int folio) {
        int tabX = x + w - 16;
        // v0.21.11: la ficha bajo el cursor se alza 2px y aclara con easing
        float t = (this.animHoverCard == folio - 1 || (hover && this.animHoverCard < 0))
            ? Math.max(0f, Math.min(1f, this.cardHover.getEased())) : 0f;
        int lift = (int) (t * 2f);
        y -= lift;
        if (discovered) {
            g.fill(x + 1, y + 1, x + w + 1, y + CARD_H + 1, 0x14000000);
            g.fill(x, y, x + w, y + CARD_H, DiaryChrome.lerpColor(hover ? 0xFFE0CFAB : 0xFFD5C298, 0xFFF0E2C2, t));
            g.fill(x, y, x + w, y + 1, 0xFF8E6A35);
            g.fill(x, y + CARD_H - 1, x + w, y + CARD_H, 0xFF8E6A35);
            g.fill(x, y, x + 1, y + CARD_H, 0xFF8E6A35);
            g.fill(x + w - 1, y, x + w, y + CARD_H, 0xFF8E6A35);
            g.fill(tabX, y - 2, tabX + 12, y + 2, DiaryTheme.withAlpha(DiaryTheme.BRONZE, 120));
            g.fill(x + 5, y + 4, x + 11, y + 10, 0xFF6E1F16);
            String fit = truncate(Discoveries.nameFor(id), w - 22);
            g.drawString(this.font, fit, x + 15, y + 7, DiaryTheme.INK, false);
            String folioTxt = String.valueOf(folio);
            int fw = this.font.width(folioTxt);
            g.drawString(this.font, folioTxt, x + w - fw - 5, y + 7, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 150), false);
        } else {
            g.fill(x, y, x + w, y + CARD_H, DiaryTheme.withAlpha(0x003D2A18, hover ? 28 : 14));
            g.fill(x, y, x + w, y + 1, DiaryTheme.withAlpha(0x008E6A35, 60));
            g.fill(x + 4, y + 10, x + w - 4, y + 11, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 20));
            String q = "?";
            int qw = this.font.width(q);
            g.drawString(this.font, q, x + (w - qw) / 2, y + 7, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, hover ? 150 : 70), false);
        }
    }


    /** v0.21.13: sello de cera dorado del archivo completado. */
    private static void drawCompletionSeal(GuiGraphics g, int cx, int cy, int radius) {
        int gold = 0xFFB8860B;
        int goldDark = 0xFF6B4E16;
        int goldHi = 0xFFE8C860;
        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int) Math.sqrt(radius * radius - dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, gold);
        }
        for (int dy = -radius; dy <= 0; dy++) {
            int dx = (int) Math.sqrt(radius * radius - dy * dy);
            g.fill(cx - dx, cy + dy, cx, cy + dy + 1, DiaryTheme.withAlpha(0x00FFE9A0, 70));
        }
        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int) Math.sqrt(radius * radius - dy * dy);
            g.fill(cx - dx, cy + dy, cx - dx + 1, cy + dy + 1, goldDark);
            g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, goldDark);
        }
        int r2 = Math.max(1, radius / 2);
        for (int dy = -r2; dy <= r2; dy++) {
            int dx = r2 - Math.abs(dy);
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, goldHi);
        }
    }

    private void drawPaperDoc(GuiGraphics g, int x, int y, String label, int mx, int my) {
        boolean hover = mx >= x && mx < x + 124 && my >= y && my < y + 18;
        g.fill(x + 1, y + 1, x + 125, y + 19, 0x12000000);
        g.fill(x, y, x + 124, y + 18, hover ? 0xFFDCCBA0 : 0xFFCFB98C);
        g.fill(x, y, x + 124, y + 1, 0xFF8E6A35);
        g.fill(x, y + 17, x + 124, y + 18, 0xFF8E6A35);
        g.fill(x, y, x + 1, y + 18, 0xFF8E6A35);
        g.fill(x + 123, y, x + 124, y + 18, 0xFF8E6A35);
        g.fill(x + 103, y - 2, x + 116, y + 2, DiaryTheme.withAlpha(DiaryTheme.BRONZE, 110));
        String fit = truncate(label, 108);
        g.drawString(this.font, fit, x + 5, y + 5, DiaryTheme.INK, false);
    }

    private void renderDocument(GuiGraphics g, boolean en) {
        int tx = panelX + 34;
        int ty = panelY + 34;
        int maxW = panelW - 68;
        int lh = 12;

        DiaryChrome.drawInsetCard(g, this.font, panelX + 18, panelY + 12, panelW - 36, panelH - 70,
            en ? "RECOVERED PAPER" : "PAPEL RECUPERADO",
            en ? "sealed transcript" : "transcripcion sellada",
            en ? "The reading room isolates each recovered document from the general catalogue." : "La sala de consulta separa cada documento recuperado del catalogo general.",
            DiaryTheme.BRONZE);

        if (DOC_TELEGRAM.equals(viewingDoc)) {
            ty = drawWrapped(g, textColor(0xFF6E1F16), ty, maxW, tx,
                en ? "TELEGRAM - 23 MARCH 1897 - 11:52 PM" : "TELEGRAMA - 23 MARZO 1897 - 11:52 PM");
            ty += 6;
            ty = drawWrapped(g, DiaryTheme.INK, ty, maxW, tx, en
                ? "Last report from the watch post. The bell counted thirteen. The watchman did not answer."
                : "Ultimo parte del puesto de vigia. La campana conto trece. El sereno no respondio.");
            ty += lh;
            ty = drawWrapped(g, DiaryTheme.INK, ty, maxW, tx, en
                ? "Close the line. No one in. No one out. The lamps of the thirteenth floor were lit by themselves."
                : "Cierren la linea. Nadie entra. Nadie sale. Las lamparas del piso trece se encendieron solas.");
            ty += lh;
            ty = drawWrapped(g, textColor(0xFF8E6A35), ty, maxW, tx, en ? "Signed: no one." : "Firmado: nadie.");
        } else if (DOC_HOLLOWAY.equals(viewingDoc)) {
            ty = drawWrapped(g, textColor(0xFF3D5A28), ty, maxW, tx,
                en ? "REPORT OF DR. E. HOLLOWAY - CONFIDENTIAL" : "INFORME DEL DR. E. HOLLOWAY - CONFIDENCIAL");
            ty += 6;
            ty = drawWrapped(g, DiaryTheme.INK, ty, maxW, tx, en
                ? "Day 1: mild fever. Day 3: green under the skin. Day 6: the patient's clocks stop."
                : "Dia 1: fiebre leve. Dia 3: el verde bajo la piel. Dia 6: los relojes del paciente se detienen.");
            ty += lh;
            ty = drawWrapped(g, DiaryTheme.INK, ty, maxW, tx, en
                ? "Day 9: the patient remembers things that never happened to him. The elixir does not cure. It delays the confession."
                : "Dia 9: el paciente recuerda cosas que no vivio. El elixir no cura. Retrasa la confesion.");
            ty += lh;
            ty = drawWrapped(g, textColor(0xFF6E1F16), ty, maxW, tx, en
                ? "(The rest of the page was torn out.)"
                : "(El resto de la pagina fue arrancado.)");
            if (!ChroniclerMemory.isDiscovered(Discoveries.CODE_HOLLOWAY)) {
                Discoveries.discover(Discoveries.CODE_HOLLOWAY);
            }
        } else if (DOC_FORBIDDEN.equals(viewingDoc)) {
            // La ficha negra: casi vacia. Una sola linea.
            ty += 40;
            ty = drawWrapped(g, DiaryTheme.withAlpha(DiaryTheme.INK, 190), ty, maxW, tx, en
                ? "The chronicler does not exist. Only his readers remain."
                : "El cronista no existe. Solo quedan sus lectores.");
            ty += 30;
            String stamp = en ? "SEALED FOREVER" : "SELLADO PARA SIEMPRE";
            int stw = this.font.width(stamp);
            int sx2 = panelX + (panelW - stw) / 2;
            g.fill(sx2 - 6, ty - 3, sx2 + stw + 6, ty + 12, DiaryTheme.withAlpha(0xFF6E1F16, 30));
            g.fill(sx2 - 6, ty - 3, sx2 + stw + 6, ty - 2, 0xFF6E1F16);
            g.fill(sx2 - 6, ty + 11, sx2 + stw + 6, ty + 12, 0xFF6E1F16);
            g.drawString(this.font, stamp, sx2, ty, 0xFF6E1F16, false);
            Discoveries.discover(Discoveries.FORBIDDEN_FOLIO);
        }

        // volver con click en cualquier parte baja
        String back = en ? "~ click below to return to the cards ~" : "~ pulsa abajo para volver a las fichas ~";
        int bw2 = this.font.width(back);
        g.drawString(this.font, back, panelX + (panelW - bw2) / 2, panelY + panelH - 46,
            DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 120), false);
    }

    private int textColor(int c) { return c; }

    private int drawWrapped(GuiGraphics g, int color, int y, int maxW, int x, String text) {
        java.util.List<net.minecraft.util.FormattedCharSequence> lines =
            this.font.split(Component.literal(text), maxW);
        for (net.minecraft.util.FormattedCharSequence fcs : lines) {
            g.drawString(this.font, fcs, x, y, color, false);
            y += 12;
        }
        return y;
    }

    private String truncate(String s, int maxPx) {
        if (this.font.width(s) <= maxPx) return s;
        return this.font.plainSubstrByWidth(s, Math.max(8, maxPx - 8)) + "...";
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!DOC_NONE.equals(viewingDoc)) {
            if (mouseY > panelY + panelH - 56) {
                viewingDoc = DOC_NONE;
                UiSounds.back();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        // Papeles sueltos
        int infoY = panelY + 60 + ((Discoveries.ALL.length + COLS - 1) / COLS) * (CARD_H + CARD_GAP) + 6;
        int docsY = infoY + 16 + 4;
        int dx = panelX + 26;
        boolean en = Discoveries.isEnglish();
        if (ChroniclerMemory.isDiscovered(Discoveries.CODE_1897)) {
            if (mouseX >= dx && mouseX < dx + 124 && mouseY >= docsY && mouseY < docsY + 18) {
                viewingDoc = DOC_TELEGRAM;
                UiSounds.tab();
                return true;
            }
            dx += 130;
        }
        if (ChroniclerMemory.isDiscovered(Discoveries.CODE_HOLLOWAY)) {
            if (mouseX >= dx && mouseX < dx + 124 && mouseY >= docsY && mouseY < docsY + 18) {
                viewingDoc = DOC_HOLLOWAY;
                UiSounds.tab();
                return true;
            }
            dx += 130;
        }
        if (ChroniclerMemory.discoveredCount() >= 38) {
            if (mouseX >= dx && mouseX < dx + 124 && mouseY >= docsY && mouseY < docsY + 18) {
                viewingDoc = DOC_FORBIDDEN;
                try {
                    UiSounds.playCustom(com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_CHAIN.get(), 0.22f, 0.5f);
                } catch (Throwable ignored) {}
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!DOC_NONE.equals(viewingDoc) && keyCode == 256) { // ESC dentro del doc
            viewingDoc = DOC_NONE;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (this.parent != null) Minecraft.getInstance().setScreen(this.parent);
        else Minecraft.getInstance().setScreen(null);
    }

    @Override public boolean isPauseScreen() { return false; }
}
