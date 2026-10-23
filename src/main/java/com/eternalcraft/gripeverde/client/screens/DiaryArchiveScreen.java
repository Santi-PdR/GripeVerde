package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.lore.ChroniclerMemory;
import com.eternalcraft.gripeverde.client.lore.Discoveries;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final int CARD_H = 22;
    private static final int CARD_GAP = 5;
    private static final int CARD_SCROLL_STEP = CARD_H + CARD_GAP;

    private int columns = 5;
    private int cardsTop, cardsBottom, infoY, docsY, cardWidth;
    private int maxCardScroll, maxDocumentScroll;
    private double cardScroll, documentScroll;
    private final Map<String, List<FormattedCharSequence>> wrappedDocumentCache = new HashMap<>();
    private int wrappedDocumentWidth = -1;

    public DiaryArchiveScreen(Screen parent) {
        super(Component.literal(Discoveries.isEnglish() ? "Reading Room" : "Sala de consulta"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelW = Math.max(1, Math.min(440, this.width - 24));
        panelH = Math.max(1, Math.min(404, this.height - 24));
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        columns = panelW < 240 ? 2 : panelW < 340 ? 3 : panelW < 440 ? 4 : 5;
        cardsTop = panelY + 60;
        infoY = panelY + panelH - 70;
        docsY = infoY + 12;
        cardsBottom = Math.max(cardsTop + CARD_H, infoY - 8);
        int innerW = Math.max(columns, panelW - 40);
        cardWidth = Math.max(1, (innerW - (columns - 1) * CARD_GAP) / columns);
        int rows = (Discoveries.ALL.length + columns - 1) / columns;
        int contentH = rows * CARD_SCROLL_STEP - CARD_GAP;
        maxCardScroll = Math.max(0, contentH - (cardsBottom - cardsTop));
        cardScroll = clampScroll(cardScroll, maxCardScroll);
        maxDocumentScroll = Math.max(0, maxDocumentScroll);

        int backW = Math.min(140, Math.max(1, panelW - 24));
        int backH = Math.min(24, panelH);
        this.addRenderableWidget(new DiaryButton(
            panelX + (panelW - backW) / 2, panelY + panelH - backH - 10, backW, backH,
            Component.translatable("gripeverde.options.button.done"),
            DiaryButton.Icon.DOOR, DiaryButton.Variant.PRIMARY,
            DiaryButton.SoundProfile.BACK,
            this::onClose));

        if (!DOC_NONE.equals(initialDoc)) {
            boolean allowed =
                (DOC_TELEGRAM.equals(initialDoc) && ChroniclerMemory.isDiscovered(Discoveries.CODE_1897)) ||
                (DOC_HOLLOWAY.equals(initialDoc) && ChroniclerMemory.isDiscovered(Discoveries.CODE_HOLLOWAY)) ||
                (DOC_FORBIDDEN.equals(initialDoc) && ChroniclerMemory.discoveredCount() >= 38);
            if (allowed) {
                viewingDoc = initialDoc;
                documentScroll = 0;
                maxDocumentScroll = 0;
                onDocumentOpened(initialDoc);
            }
            initialDoc = DOC_NONE;
        }
    }

    @Override
    public void tick() {
        super.tick();
        QuarantineSceneRenderer.tick(1f / 20f);
    }

    @Override
    public void renderBackground(GuiGraphics g) { /* bypass dirt */ }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);

        DiaryChrome.drawPanelShadow(g, panelX, panelY, panelW, panelH);
        PaperTexture.drawClean(g, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.ARCHIVE);
        DiaryChrome.drawCornerOrnaments(g, panelX, panelY, panelW, panelH);
        DiaryChrome.drawRegistryTag(g, panelX, panelY, panelW, panelH, "REG. XIV");

        boolean en = Discoveries.isEnglish();
        int cx = panelX + panelW / 2;

        if (!DOC_NONE.equals(viewingDoc)) {
            renderDocument(g, en);
            super.render(g, mouseX, mouseY, partialTick);
            return;
        }

        // Cabecera
        String titleStr = en ? "Reading Room" : "Sala de consulta";
        com.eternalcraft.gripeverde.client.render.DiaryTypography.drawCentered(
            g, this.font, titleStr, cx, panelY + 10, DiaryChrome.INK_TITLE);
        String sub = en ? "- The chronicler's cards -" : "- Las fichas del cronista -";
        int sw = this.font.width(sub);
        g.drawString(this.font, sub, cx - sw / 2, panelY + 23, DiaryChrome.INK_SUBTITLE, false);
        DiaryChrome.drawInkDivider(g, panelX + 18, panelY + 37, panelW - 36);

        // Progreso + sello de estado
        int found = ChroniclerMemory.discoveredCount();
        int total = Discoveries.ALL.length;
        String prog = en ? ("cards found: " + found + " of " + total)
                         : ("fichas halladas: " + found + " de " + total);
        g.drawString(this.font, prog, panelX + 20, panelY + 44, DiaryChrome.INK_FLAVOR, false);

        String stampTxt;
        int stampCol;
        if (found <= 0) { stampTxt = en ? "PENDING" : "PENDIENTE"; stampCol = 0xFF6B4226; }
        else if (found >= total) { stampTxt = en ? "COMPLETE" : "COMPLETO"; stampCol = 0xFF3D5A28; }
        else { stampTxt = en ? "IN PROGRESS" : "EN CURSO"; stampCol = 0xFF6E1F16; }
        int stw = this.font.width(stampTxt);
        int sx = panelX + panelW - stw - 34;
        int sy = panelY + 42;
        g.fill(sx - 5, sy - 2, sx + stw + 5, sy + 11, DiaryTheme.withAlpha(stampCol, 36));
        g.fill(sx - 5, sy - 2, sx + stw + 5, sy - 1, stampCol);
        g.fill(sx - 5, sy + 10, sx + stw + 5, sy + 11, stampCol);
        g.drawString(this.font, stampTxt, sx, sy, stampCol, false);

        // Grilla de fichas
        int gridX = panelX + 20;
        int innerW = Math.max(columns, panelW - 40);
        hoverCard = -1;
        int viewportH = Math.max(0, cardsBottom - cardsTop);
        if (viewportH > 0) {
            int gridScrollPx = (int) Math.round(cardScroll);
            int rowCount = (Discoveries.ALL.length + columns - 1) / columns;
            int firstRow = Math.max(0, gridScrollPx / CARD_SCROLL_STEP);
            int lastRow = Math.min(rowCount - 1,
                (gridScrollPx + viewportH) / CARD_SCROLL_STEP);
            g.enableScissor(gridX, cardsTop, gridX + innerW, cardsBottom);
            for (int row = firstRow; row <= lastRow; row++) {
                for (int col = 0; col < columns; col++) {
                    int i = row * columns + col;
                    if (i >= Discoveries.ALL.length) break;
                    int x = gridX + col * (cardWidth + CARD_GAP);
                    int y = cardsTop - gridScrollPx + row * CARD_SCROLL_STEP;
                    String id = Discoveries.ALL[i];
                    boolean disc = ChroniclerMemory.isDiscovered(id);
                    boolean hover = mouseX >= x && mouseX < x + cardWidth
                        && mouseY >= y && mouseY < y + CARD_H
                        && mouseY >= cardsTop && mouseY < cardsBottom;
                    if (hover) hoverCard = i;

                    if (disc) {
                        g.fill(x, y + 1, x + cardWidth, y + CARD_H + 1, DiaryTheme.withAlpha(0x00000000, 38));
                        g.fill(x, y, x + cardWidth, y + CARD_H, hover ? 0xFFDCCBA0 : 0xFFD2BE8E);
                        g.fill(x, y, x + cardWidth, y + 1, 0xFF8E6A35);
                        g.fill(x, y + CARD_H - 1, x + cardWidth, y + CARD_H, 0xFF8E6A35);
                        g.fill(x, y, x + 1, y + CARD_H, 0xFF8E6A35);
                        g.fill(x + cardWidth - 1, y, x + cardWidth, y + CARD_H, 0xFF8E6A35);
                        g.fill(x + 3, y + 3, x + 7, y + 7, 0xFF6E1F16);
                        String name = Discoveries.nameFor(id);
                        String fit = truncate(name, cardWidth - 12);
                        g.drawString(this.font, fit, x + 9, y + 8, DiaryTheme.INK, false);
                    } else {
                        g.fill(x, y, x + cardWidth, y + CARD_H, DiaryTheme.withAlpha(0x003D2A18, hover ? 30 : 16));
                        g.fill(x, y, x + cardWidth, y + 1, DiaryTheme.withAlpha(0x008E6A35, 70));
                        g.fill(x, y + CARD_H - 1, x + cardWidth, y + CARD_H, DiaryTheme.withAlpha(0x008E6A35, 70));
                        g.fill(x, y, x + 1, y + CARD_H, DiaryTheme.withAlpha(0x008E6A35, 70));
                        g.fill(x + cardWidth - 1, y, x + cardWidth, y + CARD_H, DiaryTheme.withAlpha(0x008E6A35, 70));
                        int qw = this.font.width("?");
                        g.drawString(this.font, "?", x + (cardWidth - qw) / 2, y + 7,
                            DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, hover ? 150 : 70), false);
                    }
                }
            }
            g.disableScissor();
        }

        // Texto de la ficha bajo el cursor
        if (hoverCard >= 0) {
            String id = Discoveries.ALL[hoverCard];
            if (ChroniclerMemory.isDiscovered(id)) {
                drawC(g, Discoveries.nameFor(id), cx, infoY, DiaryTheme.INK);
            } else {
                drawC(g, Discoveries.hintFor(id), cx, infoY, DiaryChrome.INK_FLAVOR);
            }
        } else {
            boolean complete = found >= total;
            String msg = complete
                ? (en ? "The archive is complete." : "El archivo esta completo.")
                : (en ? "The missing cards are still out there." : "Las fichas que faltan siguen ahi fuera.");
            drawC(g, msg, cx, infoY,
                complete ? DiaryTheme.INK : DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 120));
            if (complete) {
                int mw = this.font.width(msg);
                int sealCx = cx + mw / 2 + 20;
                String sig = "SIGILLUM";
                g.drawString(this.font, sig, sealCx - this.font.width(sig) / 2, infoY - 15, 0xFF8A6A2D, false);
                drawCompletionSeal(g, sealCx, infoY + 6, 8);
            }
        }

        // Papeles sueltos (documentos recuperados)
        DiaryChrome.drawInkDivider(g, panelX + 26, docsY - 4, panelW - 52);
        int docCount = availableDocCount();
        int docW = documentCardWidth(docCount);
        int dx = documentCardX(0, docCount, docW);
        int docY = docsY + 4;
        if (ChroniclerMemory.isDiscovered(Discoveries.CODE_1897)) {
            drawPaperDoc(g, dx, docY, docW,
                en ? "The night of the outbreak" : "La noche del brote", mouseX, mouseY);
            dx += docW + 6;
        }
        if (ChroniclerMemory.isDiscovered(Discoveries.CODE_HOLLOWAY)) {
            drawPaperDoc(g, dx, docY, docW,
                en ? "Dr. Holloway's report" : "Informe del Dr. Holloway", mouseX, mouseY);
            dx += docW + 6;
        }
        if (ChroniclerMemory.discoveredCount() >= 38) {
            drawPaperDoc(g, dx, docY, docW,
                en ? "A black card" : "Una ficha negra", mouseX, mouseY);
        }

        super.render(g, mouseX, mouseY, partialTick);

        // Marginalia del cronista
        com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
        com.eternalcraft.gripeverde.client.lore.Marginalia.draw(g, panelX, panelY, panelW, panelH);

        // v0.21.3/v0.21.5: la cadena de la limpieza (solo texto, sin mecanica nueva)
        if (ChroniclerMemory.isDiscovered(Discoveries.ARCHIVE_THINNING)
                && ChroniclerMemory.isDiscovered(Discoveries.THEME_NEVER_PLAYED)) {
            Discoveries.discover(Discoveries.BG_TOWN_MEMORY);
        }
        if (ChroniclerMemory.isDiscovered(Discoveries.BG_TOWN_MEMORY)
                && ChroniclerMemory.isDiscovered(Discoveries.ARCHIVE_THINNING)
                && ChroniclerMemory.isDiscovered(Discoveries.THEME_NEVER_PLAYED)) {
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

    private void drawPaperDoc(GuiGraphics g, int x, int y, int width,
                              String label, int mx, int my) {
        boolean hover = mx >= x && mx < x + width && my >= y && my < y + 18;
        g.fill(x, y, x + width, y + 18, hover ? 0xFFDCCBA0 : 0xFFCFB98C);
        g.fill(x, y, x + width, y + 1, 0xFF8E6A35);
        g.fill(x, y + 17, x + width, y + 18, 0xFF8E6A35);
        g.fill(x, y, x + 1, y + 18, 0xFF8E6A35);
        g.fill(x + width - 1, y, x + width, y + 18, 0xFF8E6A35);
        String fit = truncate(label, Math.max(1, width - 10));
        g.drawString(this.font, fit, x + 5, y + 5, DiaryTheme.INK, false);
    }

    private int availableDocCount() {
        int count = 0;
        if (ChroniclerMemory.isDiscovered(Discoveries.CODE_1897)) count++;
        if (ChroniclerMemory.isDiscovered(Discoveries.CODE_HOLLOWAY)) count++;
        if (ChroniclerMemory.discoveredCount() >= 38) count++;
        return count;
    }

    private int documentCardWidth(int count) {
        if (count <= 0) return 0;
        return Math.max(1, Math.min(124, (panelW - 52 - (count - 1) * 6) / count));
    }

    private int documentCardX(int index, int count, int width) {
        int totalWidth = count * width + Math.max(0, count - 1) * 6;
        return panelX + (panelW - totalWidth) / 2 + index * (width + 6);
    }

    private void openDocument(String document) {
        viewingDoc = document;
        documentScroll = 0;
        maxDocumentScroll = 0;
        onDocumentOpened(document);
        UiSounds.tab();
    }

    private void onDocumentOpened(String document) {
        if (DOC_HOLLOWAY.equals(document)) {
            Discoveries.discover(Discoveries.CODE_HOLLOWAY);
        } else if (DOC_FORBIDDEN.equals(document)) {
            Discoveries.discover(Discoveries.FORBIDDEN_FOLIO);
        }
    }

    private static double clampScroll(double value, int max) {
        return Math.max(0, Math.min(max, value));
    }

    private void renderDocument(GuiGraphics g, boolean en) {
        int bodyTop = panelY + 22;
        int bodyBottom = Math.max(bodyTop + 1, panelY + panelH - 64);
        int tx = panelX + 34;
        int ty = bodyTop - (int) Math.round(documentScroll);
        int maxW = Math.max(1, panelW - 68);
        int lh = 12;
        g.enableScissor(panelX + 18, bodyTop, panelX + panelW - 18, bodyBottom);

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
        }

        g.disableScissor();
        maxDocumentScroll = Math.max(0, ty + (int) Math.round(documentScroll) - bodyBottom);
        documentScroll = clampScroll(documentScroll, maxDocumentScroll);

        // volver con click en cualquier parte baja
        String back = en ? "~ click below to return to the cards ~" : "~ pulsa abajo para volver a las fichas ~";
        back = truncate(back, Math.max(1, panelW - 24));
        int bw2 = this.font.width(back);
        g.drawString(this.font, back, panelX + (panelW - bw2) / 2, panelY + panelH - 46,
            DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 120), false);
    }

    private int textColor(int c) { return c; }

    private int drawWrapped(GuiGraphics g, int color, int y, int maxW, int x, String text) {
        if (this.wrappedDocumentWidth != maxW) {
            this.wrappedDocumentCache.clear();
            this.wrappedDocumentWidth = maxW;
        }
        List<FormattedCharSequence> lines = this.wrappedDocumentCache.get(text);
        if (lines == null) {
            lines = this.font.split(Component.literal(text), maxW);
            this.wrappedDocumentCache.put(text, lines);
        }
        for (FormattedCharSequence fcs : lines) {
            g.drawString(this.font, fcs, x, y, color, false);
            y += 12;
        }
        return y;
    }

    private void drawC(GuiGraphics g, String s, int cx, int y, int color) {
        int w2 = this.font.width(s);
        g.drawString(this.font, s, cx - w2 / 2, y, color, false);
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
                documentScroll = 0;
                UiSounds.back();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        // Papeles sueltos
        int docCount = availableDocCount();
        int docW = documentCardWidth(docCount);
        int dx = documentCardX(0, docCount, docW);
        int docY = docsY + 4;
        if (ChroniclerMemory.isDiscovered(Discoveries.CODE_1897)) {
            if (mouseX >= dx && mouseX < dx + docW && mouseY >= docY && mouseY < docY + 18) {
                openDocument(DOC_TELEGRAM);
                return true;
            }
            dx += docW + 6;
        }
        if (ChroniclerMemory.isDiscovered(Discoveries.CODE_HOLLOWAY)) {
            if (mouseX >= dx && mouseX < dx + docW && mouseY >= docY && mouseY < docY + 18) {
                openDocument(DOC_HOLLOWAY);
                return true;
            }
            dx += docW + 6;
        }
        if (ChroniclerMemory.discoveredCount() >= 38) {
            if (mouseX >= dx && mouseX < dx + docW && mouseY >= docY && mouseY < docY + 18) {
                openDocument(DOC_FORBIDDEN);
                try {
                    UiSounds.playCustom(com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_CHAIN.get(), 0.22f, 0.5f);
                } catch (Throwable ignored) {}
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!DOC_NONE.equals(viewingDoc)) {
            int bodyTop = panelY + 22;
            int bodyBottom = Math.max(bodyTop + 1, panelY + panelH - 64);
            if (mouseX >= panelX + 18 && mouseX < panelX + panelW - 18
                    && mouseY >= bodyTop && mouseY < bodyBottom) {
                documentScroll = clampScroll(documentScroll - delta * 24, maxDocumentScroll);
                return true;
            }
        } else if (mouseX >= panelX + 18 && mouseX < panelX + panelW - 18
                && mouseY >= cardsTop && mouseY < cardsBottom) {
            cardScroll = clampScroll(cardScroll - delta * CARD_SCROLL_STEP * 3, maxCardScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
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
