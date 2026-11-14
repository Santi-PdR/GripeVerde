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

    private static final int COLS = 5;
    private static final int CARD_H = 22;
    // v0.183.0: dos pixeles mas entre tarjetas y los botones se consultan antes
    // que los papeles sueltos y que la banda de cierre del documento abierto.
    private static final int CARD_GAP = 7;

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
        PaperTexture.drawVeil(g, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.ARCHIVE);
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
        String titleStr = DiaryChrome.fitText(this.font, en ? "Reading Room" : "Sala de consulta",
            Math.max(0, panelW - 32));
        int tw = this.font.width(titleStr);
        g.drawString(this.font, titleStr, cx - tw / 2, panelY + 10, DiaryChrome.INK_TITLE, false);
        String sub = DiaryChrome.fitText(this.font,
            en ? "- The chronicler's cards -" : "- Las fichas del cronista -", Math.max(0, panelW - 32));
        int sw = this.font.width(sub);
        g.drawString(this.font, sub, cx - sw / 2, panelY + 23, DiaryChrome.INK_SUBTITLE, false);
        DiaryChrome.drawInkDivider(g, panelX + 18, panelY + 37, panelW - 36);

        // Progreso + sello de estado
        int found = ChroniclerMemory.discoveredCount();
        int total = Discoveries.ALL.length;
        String prog = DiaryChrome.fitText(this.font, en ? ("cards found: " + found + " of " + total)
                         : ("fichas halladas: " + found + " de " + total), Math.max(0, panelW - 40));
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
        int innerW = panelW - 40;
        int cw = (innerW - (COLS - 1) * CARD_GAP) / COLS;
        int gridY = panelY + 60;
        hoverCard = -1;
        for (int i = 0; i < Discoveries.ALL.length; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int x = gridX + col * (cw + CARD_GAP);
            int y = gridY + row * (CARD_H + CARD_GAP);
            String id = Discoveries.ALL[i];
            boolean disc = ChroniclerMemory.isDiscovered(id);
            boolean hover = mouseX >= x && mouseX < x + cw && mouseY >= y && mouseY < y + CARD_H;
            if (hover) hoverCard = i;

            if (disc) {
                g.fill(x, y + 1, x + cw, y + CARD_H + 1, DiaryTheme.withAlpha(0x00000000, 38));
                g.fill(x, y, x + cw, y + CARD_H, hover ? 0xFFDCCBA0 : 0xFFD2BE8E);
                g.fill(x, y, x + cw, y + 1, 0xFF8E6A35);
                g.fill(x, y + CARD_H - 1, x + cw, y + CARD_H, 0xFF8E6A35);
                g.fill(x, y, x + 1, y + CARD_H, 0xFF8E6A35);
                g.fill(x + cw - 1, y, x + cw, y + CARD_H, 0xFF8E6A35);
                // marca de lacre
                g.fill(x + 3, y + 3, x + 7, y + 7, 0xFF6E1F16);
                String name = Discoveries.nameFor(id);
                String fit = truncate(name, cw - 12);
                g.drawString(this.font, fit, x + 9, y + 8, DiaryTheme.INK, false);
            } else {
                g.fill(x, y, x + cw, y + CARD_H, DiaryTheme.withAlpha(0x003D2A18, hover ? 30 : 16));
                g.fill(x, y, x + cw, y + 1, DiaryTheme.withAlpha(0x008E6A35, 70));
                g.fill(x, y + CARD_H - 1, x + cw, y + CARD_H, DiaryTheme.withAlpha(0x008E6A35, 70));
                g.fill(x, y, x + 1, y + CARD_H, DiaryTheme.withAlpha(0x008E6A35, 70));
                g.fill(x + cw - 1, y, x + cw, y + CARD_H, DiaryTheme.withAlpha(0x008E6A35, 70));
                String q = "?";
                int qw = this.font.width(q);
                g.drawString(this.font, q, x + (cw - qw) / 2, y + 7, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, hover ? 150 : 70), false);
            }
        }

        // Texto de la ficha bajo el cursor
        int infoY = gridY + ((Discoveries.ALL.length + COLS - 1) / COLS) * (CARD_H + CARD_GAP) + 6;
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
                ? (en ? "The archive is complete." : "El archivo está completo.")
                : (en ? "The missing cards are still out there." : "Las fichas que faltan siguen ahí fuera.");
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
        int docsY = infoY + 16;
        DiaryChrome.drawInkDivider(g, panelX + 26, docsY - 4, panelW - 52);
        int dx = panelX + 26;
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

        // Preserve notes when the archive panel leaves no readable margin.
        if (panelY >= 20) {
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, panelY - 14);
        } else {
            com.eternalcraft.gripeverde.client.lore.Marginalia.deferActive();
        }

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

    private void drawPaperDoc(GuiGraphics g, int x, int y, String label, int mx, int my) {
        boolean hover = mx >= x && mx < x + 124 && my >= y && my < y + 18;
        int paper = hover ? 0xFFDCCBA0 : 0xFFCFB98C;
        g.fill(x + 1, y + 2, x + 124, y + 19, DiaryTheme.withAlpha(0xFF302315, 30));
        g.fill(x, y, x + 124, y + 18, paper);
        g.fill(x, y, x + 124, y + 1, 0xFF8E6A35);
        g.fill(x, y + 17, x + 124, y + 18, 0xFF8E6A35);
        g.fill(x, y, x + 1, y + 18, 0xFF8E6A35);
        g.fill(x + 123, y, x + 124, y + 18, 0xFF8E6A35);

        // Desgaste fijo, dibujado solo en las fichas del archivo.
        g.fill(x + 3, y + 4, x + 4, y + 5, DiaryTheme.withAlpha(0xFF704B24, 105));
        g.fill(x + 3, y + 10, x + 4, y + 11, DiaryTheme.withAlpha(0xFF704B24, 80));
        g.fill(x + 6, y + 15, x + 18, y + 16, DiaryTheme.withAlpha(0xFF704B24, 45));
        g.fill(x + 20, y + 15, x + 25, y + 16, DiaryTheme.withAlpha(0xFF704B24, 28));

        // Esquina doblada con una sombra corta; permanece estática y fuera del texto.
        g.fill(x + 115, y, x + 123, y + 1, 0xFF8E6A35);
        g.fill(x + 116, y + 1, x + 123, y + 2, 0xFFE0D0A8);
        g.fill(x + 117, y + 2, x + 123, y + 3, 0xFFD0BC91);
        g.fill(x + 118, y + 3, x + 123, y + 4, 0xFFB89E70);
        g.fill(x + 119, y + 4, x + 123, y + 5, 0xFF8E6A35);
        g.fill(x + 116, y + 2, x + 117, y + 5, DiaryTheme.withAlpha(0xFF704B24, 115));

        String fit = truncate(label, 106);
        g.drawString(this.font, fit, x + 5, y + 5, DiaryTheme.INK, false);
    }

    private void renderDocument(GuiGraphics g, boolean en) {
        int tx = panelX + 34;
        int ty = panelY + 22;
        int maxW = panelW - 68;
        int lh = 12;

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
        if (DiaryChrome.overAnyWidget(this, mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
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
