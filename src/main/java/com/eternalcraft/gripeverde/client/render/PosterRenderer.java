package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PosterRenderer {

    // Reused title-screen copy keys avoid allocating transient arrays on each render.
    private static final String[] QUARANTINE_COPY_KEYS = {
        "gripeverde.poster.quarantine.body.1", "gripeverde.poster.quarantine.body.2",
        "gripeverde.poster.quarantine.body.3", "gripeverde.poster.quarantine.body.4",
        "gripeverde.poster.quarantine.body.5", "gripeverde.poster.quarantine.body.6", "",
        "gripeverde.poster.quarantine.warn.1", "gripeverde.poster.quarantine.warn.2",
        "gripeverde.poster.quarantine.warn.3"
    };
    private static final String[] ELIXIR_COPY_KEYS = {
        "gripeverde.poster.elixir.body.1", "gripeverde.poster.elixir.body.2", "",
        "gripeverde.poster.elixir.price", "gripeverde.poster.elixir.unit"
    };
    private static final String[] RESCUE_COPY_KEYS = {
        "gripeverde.poster.rescue.body.1", "gripeverde.poster.rescue.body.2",
        "gripeverde.poster.rescue.body.3", "gripeverde.poster.rescue.body.4",
        "gripeverde.poster.rescue.body.5"
    };
    private static final int[] BORDER_CUT_INSETS = {6, 4, 2, 1, 0};

    public static void drawQuarantineNotice(GuiGraphics g, int x, int y, int w, int h) {
        Font font = Minecraft.getInstance().font;
        OrnamentRenderer.drawPaperPanel(g, x, y, w, h);
        drawSimpleBorder(g, x, y, w, h, DiaryTheme.INK, DiaryTheme.SEPIA_MID);
        drawFoldedPaperCorner(g, x, y, w, h);

        int cx = x + w / 2;
        int copyWidth = Math.max(0, w - 28);
        int cursorY = y + 10;

        drawFittedCenteredLine(g, font, getRotatingQuarantineHeader(0), cx, cursorY, DiaryTheme.INK, copyWidth);
        cursorY += 12;
        drawFittedCenteredLine(g, font, getRotatingQuarantineHeader(1), cx, cursorY, DiaryTheme.INK, copyWidth);
        cursorY += 16;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 8;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY, 0);
        g.pose().scale(1.2f, 1.2f, 1f);
        String q = DiaryChrome.fitText(font, I18n.get("gripeverde.poster.quarantine.title"), Math.max(0, (int)(copyWidth / 1.2f)));
        int qw = font.width(q);
        // v0.21.24: tinta oscura sin sombra (se retiro el rojo sombreado).
        g.drawString(font, q, -qw / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();
        cursorY += 18;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 10;

        for (String key : QUARANTINE_COPY_KEYS) {
            if (!key.isEmpty()) {
                drawFittedCenteredLine(g, font, I18n.get(key), cx, cursorY, DiaryTheme.INK_FADED, copyWidth);
            }
            cursorY += 10;
        }

        if (h >= 220 && w >= 150) {
            // A static period-style portrait gives the quarantine order a clear Ghoul identity.
            drawQuarantineGhoulPortrait(g, x + w - 42, y + h - 68);
        } else {
            OrnamentRenderer.drawWaxSeal(g, x + w - 22, y + h - 22, 12);
        }
        drawBloodSplatter(g, x, y, w, h, 0x51A11L);
        drawInkStamp(g, x, y, w, h, 0x57A9AL);
    }

    /** Still ink portrait of a living, blue-eyed Ghoul on the quarantine broadside. */
    private static void drawQuarantineGhoulPortrait(GuiGraphics g, int x, int y) {
        int ink = DiaryTheme.GHOUL_INK;
        int skin = 0xFF718781;
        int shade = 0xFF4C625F;
        int coat = 0xFF293B3A;

        // Cropped shoulders and collar, like a rough medical engraving.
        g.fill(x + 3, y + 25, x + 22, y + 38, ink);
        g.fill(x + 5, y + 27, x + 20, y + 36, coat);
        g.fill(x + 9, y + 24, x + 16, y + 29, skin);
        g.fill(x + 7, y + 29, x + 11, y + 32, 0xFFD4C6A0);
        g.fill(x + 15, y + 29, x + 19, y + 32, 0xFFD4C6A0);

        // Uneven hairline and a pale, still-living face.
        g.fill(x + 6, y + 3, x + 17, y + 6, ink);
        g.fill(x + 4, y + 6, x + 19, y + 10, ink);
        g.fill(x + 5, y + 9, x + 18, y + 23, skin);
        g.fill(x + 3, y + 12, x + 6, y + 18, ink);
        g.fill(x + 17, y + 12, x + 20, y + 17, shade);
        g.fill(x + 6, y + 8, x + 10, y + 12, shade);
        g.fill(x + 14, y + 8, x + 18, y + 11, shade);
        g.fill(x + 7, y + 13, x + 10, y + 15, DiaryTheme.GHOUL_EYE_GLOW);
        g.fill(x + 14, y + 13, x + 17, y + 15, DiaryTheme.GHOUL_EYE_GLOW);
        g.fill(x + 8, y + 13, x + 9, y + 16, ink);
        g.fill(x + 15, y + 13, x + 16, y + 16, ink);
        g.fill(x + 11, y + 16, x + 13, y + 19, shade);
        g.fill(x + 9, y + 21, x + 15, y + 23, ink);

        // Cross-hatching keeps it printed, not a bright game icon.
        g.fill(x + 1, y + 7, x + 3, y + 8, DiaryTheme.SEPIA_MID);
        g.fill(x + 20, y + 6, x + 22, y + 7, DiaryTheme.SEPIA_MID);
        g.fill(x + 1, y + 23, x + 4, y + 24, DiaryTheme.SEPIA_MID);
        g.fill(x + 21, y + 23, x + 24, y + 24, DiaryTheme.SEPIA_MID);
    }

    public static void drawPreventionNotice(GuiGraphics g, int x, int y, int w, int h) {
        Font font = Minecraft.getInstance().font;
        OrnamentRenderer.drawPaperPanel(g, x, y, w, h);
        drawSimpleBorder(g, x, y, w, h, DiaryTheme.INK, DiaryTheme.SEPIA_MID);
        drawFoldedPaperCorner(g, x, y, w, h);

        int cx = x + w / 2;
        int copyWidth = Math.max(0, w - 28);
        boolean compact = h < 160;
        int lineStep = compact ? 8 : 10;
        boolean showFraudStamp = false;
        try {
            showFraudStamp = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("fraude_stamp") > 0;
        } catch (Throwable ignored) {}
        int cursorY = y + (compact ? 6 : 8);

        DiaryChrome.drawCenteredString(g, font, I18n.get("gripeverde.poster.elixir.author"), cx, cursorY, DiaryTheme.INK);
        cursorY += compact ? 9 : 12;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += compact ? 5 : 6;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY, 0);
        g.pose().scale(compact ? 1.0f : 1.2f, compact ? 1.0f : 1.2f, 1f);
        String am = I18n.get("gripeverde.poster.elixir.title.1");
        int aw = font.width(am);
        g.drawString(font, am, -aw / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();
        cursorY += compact ? 10 : 14;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY, 0);
        g.pose().scale(compact ? 1.15f : 1.4f, compact ? 1.15f : 1.4f, 1f);
        String el = I18n.get("gripeverde.poster.elixir.title.2");
        int ew = font.width(el);
        g.drawString(font, el, -ew / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();
        cursorY += compact ? 14 : 18;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += compact ? 5 : 6;

        for (String key : ELIXIR_COPY_KEYS) {
            if (!key.isEmpty()) {
                drawFittedCenteredLine(g, font, I18n.get(key), cx, cursorY, DiaryTheme.INK_FADED, copyWidth);
            }
            cursorY += lineStep;
        }

        // Place the mark immediately after the bottle line. Pinning it to the
        // card's bottom edge can put it back on top of the copy in short layouts.
        if (showFraudStamp) {
            String stamp = isEnglish() ? "FRAUD" : "FRAUDE";
            int stampWidth = font.width(stamp);
            int stampX = Math.max(x + 4, Math.min(cx - stampWidth / 2, x + w - stampWidth - 4));
            // Keep the mark in a real footer directly below the bottle line.
            // If the paper is too short, omit it instead of covering the copy.
            int stampY = cursorY + 4;
            int stampBottom = stampY + 11;
            if (stampBottom <= y + h - 3) {
                g.fill(stampX - 4, stampY - 3, stampX + stampWidth + 4, stampY - 2, 0xFF5C1508);
                g.fill(stampX - 4, stampY + 10, stampX + stampWidth + 4, stampY + 11, 0xFF5C1508);
                g.fill(stampX - 4, stampY - 3, stampX - 3, stampY + 11, 0xFF5C1508);
                g.fill(stampX + stampWidth + 3, stampY - 3, stampX + stampWidth + 4, stampY + 11, 0xFF5C1508);
                g.drawString(font, stamp, stampX, stampY, 0xFF5C1508, false);
            }
        }
    }


    /** Panel derecho inferior del menú: rota entre 5 avisos con seed diaria. */
    public static void drawRotatingNoticeRight(GuiGraphics g, int x, int y, int w, int h) {
        long day = System.currentTimeMillis() / 86_400_000L;
        int variant = (int) Math.floorMod(day, 5L);
        switch (variant) {
            case 1 -> drawRewardNotice(g, x, y, w, h);
            case 2 -> drawDecreeNotice(g, x, y, w, h);
            case 3 -> drawGhoulWarning(g, x, y, w, h);
            case 4 -> drawRescueNotice(g, x, y, w, h);
            default -> drawPreventionNotice(g, x, y, w, h); // Elixir
        }
    }

    /**
     * Informe CEDA de lectura rápida. Solo aparece en el cartel estático
     * de la pantalla principal; nunca se dibuja durante una expedición.
     */
    private static void drawGhoulWarning(GuiGraphics g, int x, int y, int w, int h) {
        Font font = Minecraft.getInstance().font;
        OrnamentRenderer.drawPaperPanel(g, x, y, w, h);
        drawSimpleBorder(g, x, y, w, h, DiaryTheme.GHOUL_INK, DiaryTheme.GHOUL_EYE);
        drawFoldedPaperCorner(g, x, y, w, h);

        int cx = x + w / 2;
        int textWidth = Math.max(0, w - 20);
        int cursorY = y + 10;

        DiaryChrome.drawCenteredString(g, font,
                DiaryChrome.fitText(font, I18n.get("gripeverde.poster.ghoul.header"), textWidth),
                cx, cursorY, DiaryTheme.GHOUL_INK);
        cursorY += 12;
        drawThinDivider(g, cx, cursorY, w - 28);
        cursorY += 8;

        DiaryChrome.drawCenteredString(g, font,
                DiaryChrome.fitText(font, I18n.get("gripeverde.poster.ghoul.title"), textWidth),
                cx, cursorY, DiaryTheme.GHOUL_INK_ACTIVE);
        cursorY += 17;

        DiaryChrome.drawCenteredString(g, font,
                DiaryChrome.fitText(font, I18n.get("gripeverde.poster.ghoul.body.1"), textWidth),
                cx, cursorY, DiaryTheme.INK_FADED);
        cursorY += 12;

        DiaryChrome.drawCenteredString(g, font,
                DiaryChrome.fitText(font, I18n.get("gripeverde.poster.ghoul.body.2"), textWidth),
                cx, cursorY, DiaryTheme.INK_FADED);
        cursorY += 12;

        DiaryChrome.drawCenteredString(g, font,
                DiaryChrome.fitText(font, I18n.get("gripeverde.poster.ghoul.body.3"), textWidth),
                cx, cursorY, DiaryTheme.INK_FADED);
        cursorY += 12;

        drawThinDivider(g, cx, cursorY + 1, w - 28);
        cursorY += 10;
        DiaryChrome.drawCenteredString(g, font,
                DiaryChrome.fitText(font, I18n.get("gripeverde.poster.ghoul.warn"), textWidth),
                cx, cursorY, DiaryTheme.REDCOAT_DARK);

        // Sello de ojo dibujado con formas fijas: tinta y acento azul, sin animación.
        int eyeY = y + h - 18;
        g.fill(cx - 7, eyeY - 3, cx + 8, eyeY + 3, DiaryTheme.GHOUL_INK);
        g.fill(cx - 4, eyeY - 2, cx + 5, eyeY + 2, DiaryTheme.GHOUL_EYE);
        g.fill(cx - 1, eyeY - 2, cx + 2, eyeY + 3, DiaryTheme.GHOUL_INK_ACTIVE);
        g.fill(cx, eyeY - 1, cx + 1, eyeY, DiaryTheme.GHOUL_EYE_GLOW);
        g.fill(cx - 10, eyeY, cx - 7, eyeY + 1, DiaryTheme.SEPIA_MID);
        g.fill(cx + 8, eyeY, cx + 11, eyeY + 1, DiaryTheme.SEPIA_MID);
    }

    /** Rescue guidance shown only on the static title-screen notice. */
    private static void drawRescueNotice(GuiGraphics g, int x, int y, int w, int h) {
        Font font = Minecraft.getInstance().font;
        OrnamentRenderer.drawPaperPanel(g, x, y, w, h);
        drawSimpleBorder(g, x, y, w, h, DiaryTheme.GHOUL_INK, DiaryTheme.GHOUL_EYE);
        drawFoldedPaperCorner(g, x, y, w, h);

        int cx = x + w / 2;
        int textWidth = Math.max(0, w - 14);
        boolean compact = h < 160;
        int cursorY = y + (compact ? 6 : 9);

        DiaryChrome.drawCenteredString(g, font,
                DiaryChrome.fitText(font, I18n.get("gripeverde.poster.rescue.header"), textWidth),
                cx, cursorY, DiaryTheme.GHOUL_INK);
        cursorY += compact ? 10 : 12;
        drawThinDivider(g, cx, cursorY, w - 28);
        cursorY += compact ? 6 : 8;

        DiaryChrome.drawCenteredString(g, font,
                DiaryChrome.fitText(font, I18n.get("gripeverde.poster.rescue.title"), textWidth),
                cx, cursorY, DiaryTheme.GHOUL_INK_ACTIVE);
        cursorY += compact ? 14 : 17;

        // Keep a full glyph row plus a pixel of breathing room; the former
        // compact 8 px step let the last instruction touch the divider.
        int lineGap = Math.max(10, font.lineHeight + 1);
        for (String key : RESCUE_COPY_KEYS) {
            drawFittedCenteredLine(g, font, I18n.get(key), cx, cursorY, DiaryTheme.INK_FADED, textWidth);
            cursorY += lineGap;
        }

        drawThinDivider(g, cx, cursorY, w - 28);
        cursorY += compact ? 7 : 9;
        DiaryChrome.drawCenteredString(g, font,
                DiaryChrome.fitText(font, I18n.get("gripeverde.poster.rescue.footer"), textWidth),
                cx, cursorY, DiaryTheme.REDCOAT_DARK);
    }

    private static void drawRewardNotice(GuiGraphics g, int x, int y, int w, int h) {
        Font font = Minecraft.getInstance().font;
        OrnamentRenderer.drawPaperPanel(g, x, y, w, h);
        drawSimpleBorder(g, x, y, w, h, DiaryTheme.INK, DiaryTheme.SEPIA_MID);
        drawFoldedPaperCorner(g, x, y, w, h);

        int cx = x + w / 2;
        int copyWidth = Math.max(0, w - 28);
        int cursorY = y + 10;

        // Encabezado pequeño
        String header = isEnglish() ? "WANTED" : "SE BUSCA";
        header = DiaryChrome.fitText(font, header, copyWidth);
        int hw = font.width(header);
        g.drawString(font, header, cx - hw / 2, cursorY, DiaryTheme.INK, false);
        cursorY += 11;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 6;

        // Título "PATIENT ZERO" en rojo (escala 1.2 en vez de 1.3 para que quepa)
        g.pose().pushPose();
        g.pose().translate(cx, cursorY + 3, 0);
        g.pose().scale(1.2f, 1.2f, 1f);
        String title = DiaryChrome.fitText(font, I18n.get("gripeverde.poster.reward.title.short"),
                Math.max(0, (int)(copyWidth / 1.2f)));
        int tw = font.width(title);
        // v0.21.25: tinta oscura sin sombra (consistencia con poster de cuarentena).
        g.drawString(font, title, -tw / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();
        cursorY += 18;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 6;

        // v0.16.0: el retrato del buscado. Quien le sostiene la mirada
        // descubre que el dibujo tambien mira. Solo si el cartel es alto.
        if (h >= 160) {
            drawWantedPortrait(g, cx, cursorY, w);
            cursorY += 30;
        }

        drawFittedCenteredLine(g, font, I18n.get("gripeverde.poster.reward.body.1"), cx, cursorY, DiaryTheme.INK_FADED, copyWidth);
        cursorY += 10;
        drawFittedCenteredLine(g, font, I18n.get("gripeverde.poster.reward.body.2"), cx, cursorY, DiaryTheme.INK_FADED, copyWidth);
        cursorY += 14;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 8;

        // Prize compactado en una sola línea
        drawFittedCenteredLine(g, font, I18n.get("gripeverde.poster.reward.prize.short"), cx, cursorY, DiaryTheme.BLOOD_DRIED, copyWidth);
        cursorY += 10;
        drawFittedCenteredLine(g, font, I18n.get("gripeverde.poster.reward.prize.unit"), cx, cursorY, DiaryTheme.BRONZE, copyWidth);

        // At the shortest supported height the footer copy reaches the lower corner.
        // Keep the wax mark only when a full seal can sit below the last line.
        if (h >= 130) {
            OrnamentRenderer.drawWaxSeal(g, x + w - 18, y + h - 18, 7);
        }
        drawBloodSplatter(g, x, y, w, h, 0x5EAADL);
        drawInkStamp(g, x, y, w, h, 0x5EA100L);
    }

    private static void drawDecreeNotice(GuiGraphics g, int x, int y, int w, int h) {
        Font font = Minecraft.getInstance().font;
        OrnamentRenderer.drawPaperPanel(g, x, y, w, h);
        drawSimpleBorder(g, x, y, w, h, DiaryTheme.INK, DiaryTheme.SEPIA_MID);
        drawFoldedPaperCorner(g, x, y, w, h);

        int cx = x + w / 2;
        int copyWidth = Math.max(0, w - 28);
        int cursorY = y + 10;

        drawFittedCenteredLine(g, font, I18n.get("gripeverde.poster.decree.header"), cx, cursorY, DiaryTheme.INK, copyWidth);
        cursorY += 12;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 8;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY + 4, 0);
        g.pose().scale(1.1f, 1.1f, 1f);
        String title = DiaryChrome.fitText(font, I18n.get("gripeverde.poster.decree.title"),
                Math.max(0, (int)(copyWidth / 1.1f)));
        int tw = font.width(title);
        // v0.21.25: tinta oscura sin sombra (consistencia con poster de cuarentena).
        g.drawString(font, title, -tw / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();
        cursorY += 18;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 10;

        drawFittedCenteredLine(g, font, I18n.get("gripeverde.poster.decree.body.1"), cx, cursorY, DiaryTheme.INK_FADED, copyWidth);
        cursorY += 10;
        drawFittedCenteredLine(g, font, I18n.get("gripeverde.poster.decree.body.2"), cx, cursorY, DiaryTheme.INK_FADED, copyWidth);
        cursorY += 18;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 8;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY + 2, 0);
        g.pose().scale(1.1f, 1.1f, 1f);
        String warn = DiaryChrome.fitText(font, I18n.get("gripeverde.poster.decree.warn"),
                Math.max(0, (int)(copyWidth / 1.1f)));
        int ww = font.width(warn);
        // v0.21.25: tinta oscura sin sombra (consistencia con poster de cuarentena).
        g.drawString(font, warn, -ww / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();

        // El decreto real lleva un sello fijo, grabado en cera y con corona.
        drawRoyalWaxSeal(g, x + w - 22, y + h - 22, 8);
    }

    private static void drawRoyalWaxSeal(GuiGraphics g, int cx, int cy, int radius) {
        OrnamentRenderer.drawWaxSeal(g, cx, cy, radius);
        int crown = DiaryTheme.CANDLE_WAX;
        g.fill(cx - 5, cy + 1, cx + 6, cy + 3, crown);
        g.fill(cx - 4, cy - 2, cx + 5, cy, crown);
        g.fill(cx - 5, cy - 4, cx - 3, cy - 1, crown);
        g.fill(cx - 1, cy - 5, cx + 1, cy - 1, crown);
        g.fill(cx + 3, cy - 4, cx + 5, cy - 1, crown);
        g.fill(cx - 6, cy + 3, cx + 7, cy + 4, DiaryTheme.GOLD_TARNISHED);
    }

    /** Retrato somero del buscado; sus ojos se mueven si el lector lo sabe. */
    private static void drawWantedPortrait(GuiGraphics g, int cx, int topY, int w) {
        int pw = 26, ph = 30;
        int x = cx - pw / 2;
        int y = topY;
        // Marco del retrato
        g.fill(x - 2, y - 2, x + pw + 2, y + ph + 2, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 190));
        g.fill(x, y, x + pw, y + ph, 0xFFD8CAB0);
        // Silueta encapuchada
        g.fill(x + 7, y + 12, x + 19, y + 28, 0xFF2A1E10);
        g.fill(x + 9, y + 6, x + 17, y + 14, 0xFF2A1E10);
        g.fill(x + 10, y + 4, x + 16, y + 7, 0xFF2A1E10);
        g.fill(x + 5, y + 16, x + 8, y + 26, 0xFF241810);
        g.fill(x + 18, y + 16, x + 21, y + 26, 0xFF241810);
        // Los ojos: se desplazan solos cada tantos segundos si ya lo sabe.
        boolean knows = false;
        try {
            knows = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("wanted_eyes") > 0;
        } catch (Throwable ignored) {}
        int shift = 0;
        if (knows) {
            long seg = System.currentTimeMillis() / 2600L;
            shift = (int)(seg % 3L) - 1; // -1, 0, 1
        }
        int eye = knows ? 0xFFD4941E : 0xFF141008;
        g.fill(x + 11 + shift, y + 10, x + 12 + shift, y + 11, eye);
        g.fill(x + 14 + shift, y + 10, x + 15 + shift, y + 11, eye);
    }

    private static void drawFittedCenteredLine(GuiGraphics g, Font font, String text,
                                                int cx, int y, int color, int maxWidth) {
        DiaryChrome.drawCenteredString(g, font,
                DiaryChrome.fitText(font, text, Math.max(0, maxWidth)), cx, y, color);
    }

    private static void drawThinDivider(GuiGraphics g, int cx, int y, int width) {
        int half = width / 2;
        int gap = 4;
        g.fill(cx - half, y, cx - gap, y + 1, DiaryTheme.SEPIA_MID);
        g.fill(cx + gap, y, cx + half, y + 1, DiaryTheme.SEPIA_MID);
        g.fill(cx - 1, y - 1, cx + 2, y + 2, DiaryTheme.OCHRE);
    }

    private static void drawFoldedPaperCorner(GuiGraphics g, int x, int y, int w, int h) {
        if (w < 50 || h < 60) return;
        int fold = 11;
        int right = x + w - 2;
        for (int row = 0; row < fold; row++) {
            int start = x + w - fold + row;
            int top = y + 1 + row;
            if (start < right) {
                g.fill(start, top, right, top + 1, DiaryTheme.PAPER_STAINED);
                g.fill(start, top, start + 1, top + 1, DiaryTheme.PAPER_DARK);
            }
        }

        // A few uneven fibres break the machine-cut lower edge without moving the text.
        g.fill(x + 13, y + h - 2, x + 18, y + h - 1, DiaryTheme.PAPER_STAINED);
        g.fill(x + 18, y + h - 1, x + 22, y + h, DiaryTheme.PAPER_STAINED);
        g.fill(x + w - 31, y + h - 1, x + w - 25, y + h, DiaryTheme.PAPER_STAINED);
    }

    private static void drawSimpleBorder(GuiGraphics g, int x, int y, int w, int h, int outerColor, int innerColor) {
        if (w < 14 || h < 14) {
            g.fill(x, y, x + w, y + 1, outerColor);
            g.fill(x, y + h - 1, x + w, y + h, outerColor);
            g.fill(x, y, x + 1, y + h, outerColor);
            g.fill(x + w - 1, y, x + w, y + h, outerColor);
            return;
        }

        // The ink follows the same shallow, stepped cuts as the paper itself.
        for (int row = 0; row < BORDER_CUT_INSETS.length; row++) {
            int inset = BORDER_CUT_INSETS[row];
            g.fill(x + inset, y + row, x + w - inset, y + row + 1, outerColor);
            g.fill(x + inset, y + h - row - 1, x + w - inset, y + h - row, outerColor);
        }
        g.fill(x, y + 4, x + 1, y + h - 4, outerColor);
        g.fill(x + w - 1, y + 4, x + w, y + h - 4, outerColor);

        for (int row = 0; row < BORDER_CUT_INSETS.length; row++) {
            int inset = BORDER_CUT_INSETS[row] + 2;
            g.fill(x + inset, y + 2 + row, x + w - inset, y + 3 + row, innerColor);
            g.fill(x + inset, y + h - 3 - row, x + w - inset, y + h - 2 - row, innerColor);
        }
        g.fill(x + 2, y + 6, x + 3, y + h - 6, innerColor);
        g.fill(x + w - 3, y + 6, x + w - 2, y + h - 6, innerColor);
    }

    // Rotating quarantine notice headers: 2 lines per variant.
    // Localized here because they are flavour variants, not fixed lang keys.
    private static final String[][] QUARANTINE_HEADER_POOL_ES = {
        {"POR ORDEN DE",    "SU MAJESTAD"},
        {"POR MANDATO DE",  "EL PARLAMENTO"},
        {"AVISO OFICIAL",   "DEL CONDESTABLE"},
        {"BANDO PUBLICO",   "DEL MAGISTRADO"},
        {"DECRETO DE",      "LA CORONA"},
        {"POR DISPOSICION", "DEL ALGUACIL MAYOR"},
        {"NOTA DE",         "LA JUNTA SANITARIA"},
        {"POR ORDEN DE",    "LA GUARDIA REAL"}
    };
    private static final String[][] QUARANTINE_HEADER_POOL_EN = {
        {"BY ORDER OF",     "HER MAJESTY"},
        {"BY MANDATE OF",   "PARLIAMENT"},
        {"OFFICIAL NOTICE", "OF THE CONSTABLE"},
        {"PUBLIC NOTICE",   "OF THE MAGISTRATE"},
        {"DECREE OF",       "THE CROWN"},
        {"BY DIRECTION",    "OF THE HIGH BAILIFF"},
        {"NOTICE FROM",     "THE SANITARY BOARD"},
        {"BY ORDER OF",     "THE ROYAL GUARD"}
    };
    private static int quarantineHeaderIndex = -1;
    private static long lastQuarantineRotateMs = 0L;
    private static final long QUARANTINE_ROTATE_MS = 120_000L;
    private static final java.util.Random QUARANTINE_RNG = new java.util.Random();

    private static String getRotatingQuarantineHeader(int lineIdx) {
        long now = System.currentTimeMillis();
        String[][] pool = isEnglish() ? QUARANTINE_HEADER_POOL_EN : QUARANTINE_HEADER_POOL_ES;
        if (quarantineHeaderIndex < 0 || quarantineHeaderIndex >= pool.length
                || (now - lastQuarantineRotateMs) >= QUARANTINE_ROTATE_MS) {
            int prev = quarantineHeaderIndex;
            int next;
            if (pool.length <= 1) {
                next = 0;
            } else {
                do {
                    next = QUARANTINE_RNG.nextInt(pool.length);
                } while (next == prev);
            }
            quarantineHeaderIndex = next;
            lastQuarantineRotateMs = now;
        }
        return pool[quarantineHeaderIndex][lineIdx];
    }

    private static boolean isEnglish() {
        return com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
    }

    // ============================================================
    // Manchas de sangre en posters (v0.12.2)
    // ============================================================

    /** Draws the same sparse, seeded flecks without allocating a Random per menu frame. */
    public static void drawBloodSplatter(GuiGraphics g, int x, int y, int w, int h, long posterSeed) {
        int sample = 0;
        if (boundedSample(posterSeed, sample++, 10) >= 3) return; // 70% clean paper

        int count = 2 + boundedSample(posterSeed, sample++, 3);
        int blood = 0xFF5C1508;
        int fresh = 0xFF8B1F0F;
        for (int i = 0; i < count; i++) {
            int bx = x + 10 + boundedSample(posterSeed, sample++, Math.max(1, w - 20));
            int by = y + 10 + boundedSample(posterSeed, sample++, Math.max(1, h - 20));
            int size = 1 + boundedSample(posterSeed, sample++, 3);

            for (int dy = 0; dy < size; dy++) {
                for (int dx = 0; dx < size; dx++) {
                    if (boundedSample(posterSeed, sample++, 5) < 4) {
                        g.fill(bx + dx, by + dy, bx + dx + 1, by + dy + 1,
                                boundedSample(posterSeed, sample++, 3) == 0 ? fresh : blood);
                    }
                }
            }

            int splatter = 2 + boundedSample(posterSeed, sample++, 3);
            for (int s = 0; s < splatter; s++) {
                int sx = bx + boundedSample(posterSeed, sample++, 6) - 3;
                int sy = by + boundedSample(posterSeed, sample++, 6) - 3;
                g.fill(sx, sy, sx + 1, sy + 1, blood);
            }
        }
    }

    /** Stateless bounded pseudo-random sample; title-screen calls allocate no RNG or buffer. */
    private static int boundedSample(long seed, int index, int bound) {
        long value = seed + 0x9E3779B97F4A7C15L * (index + 1L);
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return (int) Long.remainderUnsigned(value, bound);
    }

    // ============================================================
    // Sellos de tinta rotativos (APROBADO/REVOCADO/CENSURADO)
    // ============================================================
    private static final String[] STAMP_LABELS_ES = {
        "APROBADO", "REVOCADO", "CENSURADO", "PENDIENTE", "URGENTE"
    };
    private static final String[] STAMP_LABELS_EN = {
        "APPROVED", "REVOKED", "CENSORED", "PENDING", "URGENT"
    };
    private static final int[] STAMP_COLORS = {
        0xFF5C1508,   // dried blood (revocado, censurado)
        0xFF3D2A18,   // sepia oscuro (aprobado)
        0xFF6B4226    // sepia medio (pendiente)
    };

    /** Draws a seeded ink mark in the lower margin, never on top of the notice copy. */
    public static void drawInkStamp(GuiGraphics g, int x, int y, int w, int h, long posterSeed) {
        if (h < 130 || w < 64) return;

        // Stable seed arithmetic keeps the 40% chance and appearance without a per-frame RNG.
        long mixed = posterSeed ^ (posterSeed >>> 17) ^ (posterSeed >>> 31);
        if (Math.floorMod(mixed, 10L) >= 4L) return;

        String[] labels = isEnglish() ? STAMP_LABELS_EN : STAMP_LABELS_ES;
        String label = labels[(int) Math.floorMod(mixed >>> 4, (long) labels.length)];
        int col = STAMP_COLORS[(int) Math.floorMod(mixed >>> 8, (long) STAMP_COLORS.length)];

        Font f = Minecraft.getInstance().font;
        label = DiaryChrome.fitText(f, label, Math.max(0, w - 28));
        if (label.isBlank()) return;
        int tw = f.width(label);
        int sx = x + 12;
        int sy = y + h - 18;

        g.pose().pushPose();
        g.pose().translate(sx, sy, 0);
        g.fill(-3, -2, tw + 3, -1, col);
        g.fill(-3, 10, tw + 3, 11, col);
        g.fill(-3, -2, -2, 11, col);
        g.fill(tw + 2, -2, tw + 3, 11, col);
        g.drawString(f, label, 0, 0, col, false);
        g.pose().popPose();
    }
}
