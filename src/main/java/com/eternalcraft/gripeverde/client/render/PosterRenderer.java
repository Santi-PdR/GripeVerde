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
    private static final String[] QUARANTINE_WARN_KEYS = {
        "gripeverde.poster.quarantine.warn.1",
        "gripeverde.poster.quarantine.warn.2",
        "gripeverde.poster.quarantine.warn.3"
    };
    // Escala del grabado en el aviso CEDA: el pie del cartel ronda los 36 px y
    // el sello debe entrar entero sin tocar el texto.
    private static final float GHOUL_STAMP_SCALE = 0.5f;
    private static final int[] BORDER_CUT_INSETS = {6, 4, 2, 1, 0};

    public static void drawQuarantineNotice(GuiGraphics g, int x, int y, int w, int h) {
        Font font = Minecraft.getInstance().font;
        OrnamentRenderer.drawPaperPanel(g, x, y, w, h);
        drawSimpleBorder(g, x, y, w, h, DiaryTheme.INK, DiaryTheme.SEPIA_MID);
        drawFoldedPaperCorner(g, x, y, w, h);

        // v0.173.0: el bando se compone en dos bloques. Arriba el texto legal,
        // abajo un pie con el grabado del infectado a la izquierda y la pena a
        // la derecha. Antes el retrato se posaba sobre la ultima linea.
        boolean footerPlate = h >= 180 && w >= 150;
        boolean compact = h < 170;
        int headerStep = compact ? 10 : 11;
        int titleStep = compact ? 14 : 16;
        int lineStep = compact ? 8 : 9;
        int warnStep = compact ? 9 : 10;
        int dividerStep = compact ? 5 : 6;

        int cx = x + w / 2;
        int copyWidth = Math.max(0, w - 28);
        int cursorY = y + (compact ? 5 : 8);

        drawFittedCenteredLine(g, font, getRotatingQuarantineHeader(0), cx, cursorY, DiaryTheme.INK, copyWidth);
        cursorY += headerStep;
        drawFittedCenteredLine(g, font, getRotatingQuarantineHeader(1), cx, cursorY, DiaryTheme.INK, copyWidth);
        cursorY += headerStep;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += dividerStep;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY, 0);
        float titleScale = compact ? 1.1f : 1.2f;
        g.pose().scale(titleScale, titleScale, 1f);
        String q = DiaryChrome.fitText(font, I18n.get("gripeverde.poster.quarantine.title"),
                Math.max(0, (int) (copyWidth / titleScale)));
        int qw = font.width(q);
        // v0.21.24: tinta oscura sin sombra (se retiro el rojo sombreado).
        g.drawString(font, q, -qw / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();
        cursorY += titleStep;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += dividerStep;

        for (String key : QUARANTINE_COPY_KEYS) {
            if (!key.isEmpty()) {
                drawFittedCenteredLine(g, font, I18n.get(key), cx, cursorY, DiaryTheme.INK_FADED, copyWidth);
            }
            cursorY += lineStep;
        }

        if (footerPlate) {
            int bandTop = cursorY + 3;
            int bandHeight = Math.max(0, y + h - 5 - bandTop);
            int plateWidth = Math.max(44, Math.min(58, w - 116));
            int plateHeight = Math.min(56, bandHeight);
            // El grabado necesita altura suficiente para leerse como un rostro;
            // si no la hay, la pena se escribe centrada y sin retrato.
            if (plateHeight >= 44) {
                drawGhoulPlate(g, x + 10, bandTop, plateWidth, plateHeight);
                int warnLeft = x + 16 + plateWidth;
                int warnWidth = Math.max(0, x + w - 8 - warnLeft);
                int warnY = bandTop + Math.max(0, (plateHeight - warnStep * 3) / 2);
                for (int line = 0; line < 3 && warnWidth > 24; line++) {
                    drawFittedCenteredLine(g, font,
                            I18n.get("gripeverde.poster.quarantine.warn." + (line + 1)),
                            warnLeft + warnWidth / 2, warnY, DiaryTheme.REDCOAT_DARK, warnWidth);
                    warnY += warnStep;
                }
            } else {
                drawStackedQuarantineWarning(g, font, cx, cursorY + 3, copyWidth, warnStep);
            }
        } else {
            drawStackedQuarantineWarning(g, font, cx, cursorY + 3, copyWidth, warnStep);
            // El sello de cera solo entra si el ultimo renglon deja aire debajo.
            int warnBottom = cursorY + 3 + warnStep * 3;
            if (warnBottom + 24 <= y + h - 4) {
                OrnamentRenderer.drawWaxSeal(g, x + w - 22, y + h - 22, 12);
            }
        }
        drawBloodSplatter(g, x, y, w, h, 0x51A11L);
    }

    /** Pena del bando cuando no hay pie con grabado. */
    private static void drawStackedQuarantineWarning(GuiGraphics g, Font font, int cx,
                                                     int y, int copyWidth, int warnStep) {
        int warnY = y;
        for (String key : QUARANTINE_WARN_KEYS) {
            drawFittedCenteredLine(g, font, I18n.get(key), cx, warnY, DiaryTheme.REDCOAT_DARK, copyWidth);
            warnY += warnStep;
        }
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
        int authorStep = compact ? 9 : 12;
        int splitStep = compact ? 5 : 6;
        int titleStep1 = compact ? 10 : 14;
        int titleStep2 = compact ? 14 : 18;
        boolean showFraudStamp = false;
        try {
            showFraudStamp = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("fraude_stamp") > 0;
        } catch (Throwable ignored) {}
        // v0.174.0: the label block is centred, so a tall bottle notice no
        // longer floats against the top edge of the sheet.
        int block = authorStep + splitStep + titleStep1 + titleStep2 + splitStep
                + ELIXIR_COPY_KEYS.length * lineStep;
        int cursorY = y + Math.max(compact ? 5 : 8, (h - block) / 2);

        DiaryChrome.drawCenteredString(g, font, I18n.get("gripeverde.poster.elixir.author"), cx, cursorY, DiaryTheme.INK);
        cursorY += authorStep;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += splitStep;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY, 0);
        g.pose().scale(compact ? 1.0f : 1.2f, compact ? 1.0f : 1.2f, 1f);
        String am = I18n.get("gripeverde.poster.elixir.title.1");
        int aw = font.width(am);
        g.drawString(font, am, -aw / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();
        cursorY += titleStep1;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY, 0);
        g.pose().scale(compact ? 1.15f : 1.4f, compact ? 1.15f : 1.4f, 1f);
        String el = I18n.get("gripeverde.poster.elixir.title.2");
        int ew = font.width(el);
        g.drawString(font, el, -ew / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();
        cursorY += titleStep2;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += splitStep;

        for (int index = 0; index < ELIXIR_COPY_KEYS.length; index++) {
            String key = ELIXIR_COPY_KEYS[index];
            if (!key.isEmpty()) {
                // The final line keeps a glyph row above the lower border.
                int lineY = Math.min(cursorY, y + h - 12);
                drawFittedCenteredLine(g, font, I18n.get(key), cx, lineY, DiaryTheme.INK_FADED, copyWidth);
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

        // v0.173.0: la nota se lee como una ficha de campo. Cabecera CEDA en
        // tinta azul, filete, el grabado del infectado y recien despues el
        // parte. En carteles bajos el grabado se omite para no pisar el texto.
        int cx = x + w / 2;
        int textWidth = Math.max(0, w - 20);
        boolean showPlate = h >= 160;
        int captionStep = showPlate ? 18 : 0;
        int plateWidth = Math.max(64, Math.min(w - 24, 108));
        int plateSpace = Math.max(40, Math.min(120, h - 111 - captionStep));
        int slack = showPlate ? Math.max(0, h - 111 - captionStep - plateSpace) : 0;

        int cursorY = y + 9 + Math.min(26, slack / 2);

        DiaryChrome.drawCenteredString(g, font,
                DiaryChrome.fitText(font, I18n.get("gripeverde.poster.ghoul.header"), textWidth),
                cx, cursorY, DiaryTheme.GHOUL_INK);
        cursorY += 11;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 7;

        if (showPlate) {
            drawGhoulPlate(g, cx - plateWidth / 2, cursorY, plateWidth, plateSpace);
            cursorY += plateSpace + 4;

            // Rotulo de dos renglones bajo el retrato, como en un aviso de epoca.
            drawGhoulCaption(g, font, cx, cursorY, textWidth);
            cursorY += captionStep;
        }

        g.pose().pushPose();
        g.pose().translate(cx, cursorY, 0);
        g.pose().scale(1.1f, 1.1f, 1f);
        String title = DiaryChrome.fitText(font, I18n.get("gripeverde.poster.ghoul.title"),
                Math.max(0, (int) (textWidth / 1.1f)));
        int titleWidth = font.width(title);
        g.drawString(font, title, -titleWidth / 2, 0, DiaryTheme.GHOUL_INK_ACTIVE, false);
        g.pose().popPose();
        cursorY += 14;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 7;

        for (String key : GHOUL_COPY_KEYS) {
            drawFittedCenteredLine(g, font, I18n.get(key), cx, cursorY, DiaryTheme.INK_FADED, textWidth);
            cursorY += 10;
        }

        // El aviso y su sello CEDA comparten un pie fijo, sin solaparse: el
        // sello queda en el margen y el texto se limita a su derecha.
        int footerY = y + h - 20;
        drawGhoulEyeStamp(g, x + 19, footerY + 4, GHOUL_STAMP_SCALE);
        int warnX = x + 32;
        int warnWidth = Math.max(0, x + w - 8 - warnX);
        drawFittedCenteredLine(g, font, I18n.get("gripeverde.poster.ghoul.warn"),
                warnX + warnWidth / 2, footerY, DiaryTheme.REDCOAT_DARK, warnWidth);
    }

    /** Rotulo del grabado: cada renglon se centra por separado. */
    private static void drawGhoulCaption(GuiGraphics g, Font font, int cx, int y, int maxWidth) {
        String[] lines = I18n.get("gripeverde.poster.ghoul.caption").split("\n");
        int lineY = y;
        for (String line : lines) {
            DiaryChrome.drawCenteredString(g, font,
                    DiaryChrome.fitText(font, line, maxWidth), cx, lineY, DiaryTheme.SEPIA_MID);
            lineY += 9;
        }
    }

    /** Renglones del parte de campo; se leen de corrido, uno debajo del otro. */
    private static final String[] GHOUL_COPY_KEYS = {
        "gripeverde.poster.ghoul.body.1",
        "gripeverde.poster.ghoul.body.2",
        "gripeverde.poster.ghoul.body.3"
    };

    /**
     * Una placa de imprenta con el retrato de un Ghoul: papel claro, viñeta,
     * rayado de tinta y ojos azules. Todo son formas fijas; no hay animación
     * ni trabajo de render fuera de la portada.
     */
    private static void drawGhoulPlate(GuiGraphics g, int x, int y, int w, int h) {
        int frame = DiaryTheme.GHOUL_INK;
        int shadow = DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 150);

        g.fill(x + 2, y + 3, x + w + 1, y + h + 2, shadow);
        g.fill(x, y, x + w, y + h, frame);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFDCCEBB);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0xFFEFE3C9);

        drawGhoulPortrait(g, x + 3, y + 3, w - 6, h - 6);

        // Esquinas de la placa: pequeños remaches de latón envejecido.
        int brass = DiaryTheme.GOLD_TARNISHED;
        g.fill(x + 1, y + 1, x + 3, y + 3, brass);
        g.fill(x + w - 3, y + 1, x + w - 1, y + 3, brass);
        g.fill(x + 1, y + h - 3, x + 3, y + h - 1, brass);
        g.fill(x + w - 3, y + h - 3, x + w - 1, y + h - 1, brass);
    }

    /**
     * Rostro de un infectado impreso como grabado de epoca: craneo ovalado,
     * mandibula angosta, ojos azules encendidos y levita con cuello abierto.
     * Son formas fijas y se adaptan a la placa recibida; no hay animacion.
     */
    private static void drawGhoulPortrait(GuiGraphics g, int x, int y, int w, int h) {
        int ink = DiaryTheme.GHOUL_INK;
        int deep = DiaryTheme.GHOUL_INK_ACTIVE;
        int skin = 0xFFB9CCC7;
        int skinShade = 0xFF93ABA8;
        int skinDark = 0xFF6F8C8C;
        int bone = 0xFFE4EADB;
        int coat = 0xFF2C4A50;
        int coatLit = 0xFF436567;
        int collar = 0xFFC9D2C2;

        int cx = x + w / 2;
        int headHeight = Math.max(24, (int) (h * 0.60f));
        int headWidth = Math.max(16, (int) (headHeight * 0.72f));
        if (headWidth > (int) (w * 0.54f)) headWidth = Math.max(14, (int) (w * 0.54f));
        if (headWidth > w - 6) headWidth = Math.max(14, w - 6);

        int headX = cx - headWidth / 2;
        int headY = y + Math.max(2, (int) ((h - headHeight) * 0.44f));
        float radiusX = headWidth / 2.0f;
        float radiusY = headHeight * 0.46f;
        int centreY = headY + (int) (radiusY * 1.02f);

        // Levita y hombros: filas que se abren en embudo, con el borde en tinta.
        int coatTop = headY + (int) (headHeight * 0.72f);
        int coatBottom = Math.min(y + h - 2, coatTop + headHeight);
        int coatRows = Math.max(1, coatBottom - coatTop);
        for (int row = 0; row < coatRows; row++) {
            float t = row / (float) coatRows;
            int spread = Math.min((int) (w * 0.47f), (int) (headWidth * 1.18f));
            int half = (int) (headWidth * 0.40f + (spread - headWidth * 0.40f) * (float) Math.pow(t, 0.8));
            int rowY = coatTop + row;
            g.fill(cx - half, rowY, cx + half, rowY + 1, row % 3 == 0 ? coatLit : coat);
            g.fill(cx - half, rowY, cx - half + 1, rowY + 1, deep);
            g.fill(cx + half - 1, rowY, cx + half, rowY + 1, deep);
        }
        // Cuello de camisa abierto y solapas de la levita.
        for (int row = 0; row < coatRows; row++) {
            float t = row / (float) coatRows;
            int openHalf = Math.max(1, (int) (headWidth * 0.20f * (0.35f + 0.65f * t)));
            int rowY = coatTop + row;
            g.fill(cx - openHalf, rowY, cx + openHalf, rowY + 1, collar);
            int offset = (int) (headWidth * 0.20f + headWidth * 0.30f * t);
            g.fill(cx - offset - 1, rowY, cx - offset + 1, rowY + 1, deep);
            g.fill(cx + offset - 1, rowY, cx + offset + 1, rowY + 1, deep);
        }

        // Cuello del infectado, entre la mandibula y el cuello de la camisa.
        int neckHalf = Math.max(3, (int) (headWidth * 0.24f));
        int neckTop = headY + (int) (headHeight * 0.80f);
        for (int row = neckTop; row < coatTop + 2; row++) {
            g.fill(cx - neckHalf, row, cx + neckHalf, row + 1, skinShade);
            g.fill(cx - neckHalf, row, cx - neckHalf + 1, row + 1, skinDark);
            g.fill(cx + neckHalf - 1, row, cx + neckHalf, row + 1, skinDark);
        }

        // Craneo: elipse arriba, mandibula que se angosta hasta el menton.
        int jawStart = centreY + (int) (radiusY * 0.30f);
        int chinY = headY + headHeight;
        int shadowWidth = Math.max(1, (int) (radiusX * 0.14f));
        for (int row = 0; row < headHeight; row++) {
            int rowY = headY + row;
            int half;
            if (rowY < jawStart) {
                float t = (rowY - centreY) / radiusY;
                if (t < -1f || t > 1f) continue;
                half = (int) (radiusX * Math.sqrt(Math.max(0.0, 1.0 - t * t)));
            } else {
                float t = (rowY - jawStart) / (float) Math.max(1, chinY - jawStart);
                half = (int) (radiusX * (1f - 0.62f * t * t));
            }
            if (half <= 0) continue;
            g.fill(cx - half, rowY, cx + half, rowY + 1, skin);
            g.fill(cx - half, rowY, cx - half + shadowWidth, rowY + 1, DiaryTheme.withAlpha(skinShade, 190));
            g.fill(cx - half, rowY, cx - half + 1, rowY + 1, deep);
            g.fill(cx + half - 1, rowY, cx + half, rowY + 1, deep);
        }
        g.fill(cx - Math.max(1, (int) (radiusX * 0.5f)), chinY - 1,
                cx + Math.max(1, (int) (radiusX * 0.5f)), chinY, deep);

        // Pelo ralo y desigual: la infeccion no respeta la linea del cabello.
        int hairEnd = centreY - (int) (radiusY * 0.30f);
        for (int rowY = headY - 1; rowY < hairEnd; rowY++) {
            float t = (rowY - centreY) / radiusY;
            if (t < -1f || t > 1f) continue;
            int half = (int) (radiusX * Math.sqrt(Math.max(0.0, 1.0 - t * t))) + 1;
            g.fill(cx - half, rowY, cx + half, rowY + 1, ink);
        }
        for (int tuft = 0; tuft < 5; tuft++) {
            int tuftX = cx - (int) (radiusX * 0.85f) + tuft * Math.max(2, (int) (radiusX * 0.42f));
            int tuftW = Math.max(1, (int) (radiusX * 0.24f));
            g.fill(tuftX, hairEnd - 1, tuftX + tuftW, hairEnd + (tuft % 2 == 0 ? 0 : 1), ink);
        }
        int sideburnTop = headY + (int) (headHeight * 0.18f);
        for (int row = 0; row < Math.max(2, (int) (headHeight * 0.12f)); row++) {
            g.fill(cx - (int) radiusX - 1, sideburnTop + row, cx - (int) radiusX + 1, sideburnTop + row + 1, ink);
            g.fill(cx + (int) radiusX - 1, sideburnTop + row, cx + (int) radiusX + 1, sideburnTop + row + 1, ink);
        }

        // Orejas.
        int earY = centreY - (int) (radiusY * 0.05f);
        g.fill(headX - 2, earY, headX, earY + 4, skinShade);
        g.fill(headX + headWidth, earY, headX + headWidth + 2, earY + 4, skinShade);
        g.fill(headX - 2, earY, headX - 1, earY + 1, deep);
        g.fill(headX + headWidth + 1, earY, headX + headWidth + 2, earY + 1, deep);

        // Cejas pesadas.
        int browY = centreY - (int) (radiusY * 0.30f);
        g.fill(cx - (int) (radiusX * 0.72f), browY, cx - (int) (radiusX * 0.18f), browY + 1, ink);
        g.fill(cx + (int) (radiusX * 0.18f), browY, cx + (int) (radiusX * 0.72f), browY + 1, ink);

        // Ojos: cuenca, esclerotica azul y brillo central.
        int eyeY = centreY - (int) (radiusY * 0.16f);
        int eyeW = Math.max(3, (int) (radiusX * 0.46f));
        int eyeGap = Math.max(2, (int) (radiusX * 0.16f));
        int socketH = Math.max(3, (int) (radiusY * 0.24f));
        int leftEye = cx - eyeGap - eyeW;
        int rightEye = cx + eyeGap;
        for (int eye = 0; eye < 2; eye++) {
            int ex = eye == 0 ? leftEye : rightEye;
            g.fill(ex - 1, eyeY - 1, ex + eyeW + 1, eyeY + socketH + 1, deep);
            g.fill(ex, eyeY, ex + eyeW, eyeY + socketH, DiaryTheme.GHOUL_EYE);
            g.fill(ex + eyeW / 2, eyeY + 1, ex + eyeW / 2 + 1, eyeY + socketH - 1,
                    DiaryTheme.GHOUL_EYE_GLOW);
            g.fill(ex, eyeY + socketH - 1, ex + eyeW, eyeY + socketH, ink);
        }

        // Rayado del grabado en los pomulos.
        for (int line = 0; line < 3; line++) {
            int hatchY = eyeY + socketH + 2 + line * 2;
            g.fill(cx - (int) (radiusX * 0.95f), hatchY, cx - (int) (radiusX * 0.72f), hatchY + 1,
                    DiaryTheme.withAlpha(skinShade, 150));
            g.fill(cx + (int) (radiusX * 0.72f), hatchY + 1, cx + (int) (radiusX * 0.95f), hatchY + 2,
                    DiaryTheme.withAlpha(skinShade, 150));
        }

        // Nariz: caballete, aletas y sombra inferior.
        int noseTop = eyeY + socketH;
        int noseBottom = noseTop + Math.max(3, (int) (radiusY * 0.30f));
        g.fill(cx - 1, noseTop, cx + 1, noseBottom, DiaryTheme.withAlpha(skinShade, 220));
        g.fill(cx - 3, noseBottom, cx, noseBottom + 1, ink);
        g.fill(cx + 1, noseBottom, cx + 4, noseBottom + 1, ink);
        g.fill(cx - 1, noseBottom - 1, cx + 2, noseBottom, DiaryTheme.withAlpha(skinDark, 200));

        // Boca entreabierta con dientes irregulares: no es una sonrisa.
        int mouthY = noseBottom + Math.max(3, (int) (radiusY * 0.22f));
        int mouthW = Math.max(5, (int) (radiusX * 0.62f));
        int mouthH = Math.max(2, (int) (radiusY * 0.12f));
        g.fill(cx - mouthW / 2, mouthY, cx + mouthW / 2, mouthY + mouthH + 1, ink);
        for (int tooth = 0; tooth < 3; tooth++) {
            int toothX = cx - mouthW / 2 + 1 + tooth * Math.max(2, mouthW / 3);
            g.fill(toothX, mouthY, toothX + 1, mouthY + mouthH, bone);
        }
    }

    /** Sello circular CEDA con un ojo azul; sustituye al ojo suelto anterior. */
    private static void drawGhoulEyeStamp(GuiGraphics g, int cx, int cy, float scale) {
        int radius = Math.max(7, (int) (9 * scale));
        int ring = DiaryTheme.GHOUL_INK;
        int eye = DiaryTheme.GHOUL_EYE;

        // Anillo exterior dibujado por filas para que quede redondo pero firme.
        for (int dy = -radius; dy <= radius; dy++) {
            int span = (int) Math.sqrt(radius * radius - dy * dy);
            int inner = (int) Math.sqrt(Math.max(0, (radius - 2) * (radius - 2) - dy * dy));
            g.fill(cx - span, cy + dy, cx - inner, cy + dy + 1, ring);
            g.fill(cx + inner, cy + dy, cx + span, cy + dy + 1, ring);
        }

        int eyeW = Math.max(4, radius - 1);
        g.fill(cx - eyeW, cy - 1, cx + eyeW, cy + 2, ring);
        g.fill(cx - eyeW + 1, cy, cx + eyeW - 1, cy + 1, eye);
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, ring);
        g.fill(cx, cy - 1, cx + 1, cy, DiaryTheme.GHOUL_EYE_GLOW);
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
        int headerStep = compact ? 10 : 12;
        int splitStep = compact ? 6 : 8;
        int titleStep = compact ? 14 : 17;
        // Full glyph row plus a pixel of breathing room: the old 8 px step let
        // the last instruction touch the divider.
        int lineGap = Math.max(compact ? 9 : 10, font.lineHeight + 1);
        int footerStep = compact ? 7 : 9;

        // v0.174.0: the sheet is composed as one block and centred, so a tall
        // poster no longer leaves the note pinned to the top edge.
        int block = headerStep + splitStep + titleStep
                + RESCUE_COPY_KEYS.length * lineGap + footerStep + 9;
        int cursorY = y + Math.max(compact ? 5 : 8, (h - block) / 2);

        DiaryChrome.drawCenteredString(g, font,
                DiaryChrome.fitText(font, I18n.get("gripeverde.poster.rescue.header"), textWidth),
                cx, cursorY, DiaryTheme.GHOUL_INK);
        cursorY += headerStep;
        drawThinDivider(g, cx, cursorY, w - 28);
        cursorY += splitStep;

        DiaryChrome.drawCenteredString(g, font,
                DiaryChrome.fitText(font, I18n.get("gripeverde.poster.rescue.title"), textWidth),
                cx, cursorY, DiaryTheme.GHOUL_INK_ACTIVE);
        cursorY += titleStep;

        for (String key : RESCUE_COPY_KEYS) {
            drawFittedCenteredLine(g, font, I18n.get(key), cx, cursorY, DiaryTheme.INK_FADED, textWidth);
            cursorY += lineGap;
        }

        drawThinDivider(g, cx, cursorY, w - 28);
        cursorY += footerStep;
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
        // v0.174.0: short posters get tighter steps so the copy stops fitting
        // past the paper edge, and the whole block is centred on taller ones.
        boolean compact = h < 150;
        boolean portrait = h >= 160;
        int headerStep = compact ? 10 : 11;
        int splitStep = compact ? 5 : 6;
        int titleStep = compact ? 16 : 18;
        int bodyStep = compact ? 9 : 10;
        int prizeGap = compact ? 12 : 14;
        int prizeStep = compact ? 9 : 10;
        int block = headerStep + splitStep + titleStep + splitStep
                + (portrait ? 30 : 0) + bodyStep * 2 + prizeGap + prizeStep * 2 + 9;
        int cursorY = y + Math.max(compact ? 6 : 10, (h - block) / 2);

        // Encabezado pequeño
        String header = isEnglish() ? "WANTED" : "SE BUSCA";
        header = DiaryChrome.fitText(font, header, copyWidth);
        int hw = font.width(header);
        g.drawString(font, header, cx - hw / 2, cursorY, DiaryTheme.INK, false);
        cursorY += headerStep;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += splitStep;

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
        cursorY += titleStep;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += splitStep;

        // v0.16.0: el retrato del buscado. Quien le sostiene la mirada
        // descubre que el dibujo tambien mira. Solo si el cartel es alto.
        if (portrait) {
            drawWantedPortrait(g, cx, cursorY, w);
            cursorY += 30;
        }

        drawFittedCenteredLine(g, font, I18n.get("gripeverde.poster.reward.body.1"), cx, cursorY, DiaryTheme.INK_FADED, copyWidth);
        cursorY += bodyStep;
        drawFittedCenteredLine(g, font, I18n.get("gripeverde.poster.reward.body.2"), cx, cursorY, DiaryTheme.INK_FADED, copyWidth);
        cursorY += prizeGap;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += compact ? 6 : 8;

        // Prize compactado en una sola línea
        drawFittedCenteredLine(g, font, I18n.get("gripeverde.poster.reward.prize.short"), cx, cursorY, DiaryTheme.BLOOD_DRIED, copyWidth);
        cursorY += prizeStep;
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
        // v0.174.0: compact steps keep a 125 px block inside a 120 px sheet,
        // and the block is centred when the poster grows.
        boolean compact = h < 150;
        int headerStep = compact ? 10 : 12;
        int splitStep = compact ? 6 : 8;
        int titleStep = compact ? 16 : 18;
        int bodyStep = compact ? 9 : 10;
        int warningGap = compact ? 12 : 18;
        int block = headerStep + splitStep + titleStep + splitStep
                + bodyStep * 2 + warningGap + 12;
        int cursorY = y + Math.max(compact ? 5 : 10, (h - block) / 2);

        drawFittedCenteredLine(g, font, I18n.get("gripeverde.poster.decree.header"), cx, cursorY, DiaryTheme.INK, copyWidth);
        cursorY += headerStep;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += splitStep;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY + 4, 0);
        g.pose().scale(1.1f, 1.1f, 1f);
        String title = DiaryChrome.fitText(font, I18n.get("gripeverde.poster.decree.title"),
                Math.max(0, (int)(copyWidth / 1.1f)));
        int tw = font.width(title);
        // v0.21.25: tinta oscura sin sombra (consistencia con poster de cuarentena).
        g.drawString(font, title, -tw / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();
        cursorY += titleStep;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += compact ? 6 : 10;

        drawFittedCenteredLine(g, font, I18n.get("gripeverde.poster.decree.body.1"), cx, cursorY, DiaryTheme.INK_FADED, copyWidth);
        cursorY += bodyStep;
        drawFittedCenteredLine(g, font, I18n.get("gripeverde.poster.decree.body.2"), cx, cursorY, DiaryTheme.INK_FADED, copyWidth);
        cursorY += warningGap;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += compact ? 5 : 8;

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
