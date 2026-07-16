package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PosterRenderer {

    public static void drawQuarantineNotice(GuiGraphics g, int x, int y, int w, int h) {
        Font font = Minecraft.getInstance().font;
        OrnamentRenderer.drawPaperPanel(g, x, y, w, h);
        drawSimpleBorder(g, x, y, w, h, DiaryTheme.INK, DiaryTheme.SEPIA_MID);

        int cx = x + w / 2;
        int cursorY = y + 10;

        drawCenteredText(g, font, I18n.get("gripeverde.poster.quarantine.by_order"), cx, cursorY, DiaryTheme.INK);
        cursorY += 12;
        drawCenteredText(g, font, I18n.get("gripeverde.poster.quarantine.her_majesty"), cx, cursorY, DiaryTheme.INK);
        cursorY += 16;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 8;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY, 0);
        g.pose().scale(1.4f, 1.4f, 1f);
        String q = I18n.get("gripeverde.poster.quarantine.title");
        int qw = font.width(q);
        g.drawString(font, q, -qw / 2 + 1, 1, DiaryTheme.SEPIA_DARK, false);
        g.drawString(font, q, -qw / 2, 0, DiaryTheme.BLOOD_DRIED, false);
        g.pose().popPose();
        cursorY += 20;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 10;

        String[] body = {
            I18n.get("gripeverde.poster.quarantine.body.1"),
            I18n.get("gripeverde.poster.quarantine.body.2"),
            I18n.get("gripeverde.poster.quarantine.body.3"),
            I18n.get("gripeverde.poster.quarantine.body.4"),
            I18n.get("gripeverde.poster.quarantine.body.5"),
            I18n.get("gripeverde.poster.quarantine.body.6"),
            "",
            I18n.get("gripeverde.poster.quarantine.warn.1"),
            I18n.get("gripeverde.poster.quarantine.warn.2"),
            I18n.get("gripeverde.poster.quarantine.warn.3")
        };
        for (String line : body) {
            drawCenteredText(g, font, line, cx, cursorY, DiaryTheme.INK_FADED);
            cursorY += 10;
        }

        OrnamentRenderer.drawWaxSeal(g, x + w - 22, y + h - 22, 12);
    }

    public static void drawPreventionNotice(GuiGraphics g, int x, int y, int w, int h) {
        Font font = Minecraft.getInstance().font;
        OrnamentRenderer.drawPaperPanel(g, x, y, w, h);
        drawSimpleBorder(g, x, y, w, h, DiaryTheme.INK, DiaryTheme.SEPIA_MID);

        int cx = x + w / 2;
        int cursorY = y + 8;

        drawCenteredText(g, font, I18n.get("gripeverde.poster.elixir.author"), cx, cursorY, DiaryTheme.INK);
        cursorY += 12;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 6;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY, 0);
        g.pose().scale(1.2f, 1.2f, 1f);
        String am = I18n.get("gripeverde.poster.elixir.title.1");
        int aw = font.width(am);
        g.drawString(font, am, -aw / 2 + 1, 1, DiaryTheme.SEPIA_DARK, false);
        g.drawString(font, am, -aw / 2, 0, DiaryTheme.BRONZE, false);
        g.pose().popPose();
        cursorY += 14;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY, 0);
        g.pose().scale(1.4f, 1.4f, 1f);
        String el = I18n.get("gripeverde.poster.elixir.title.2");
        int ew = font.width(el);
        g.drawString(font, el, -ew / 2 + 1, 1, DiaryTheme.SEPIA_DARK, false);
        g.drawString(font, el, -ew / 2, 0, DiaryTheme.BLOOD_DRIED, false);
        g.pose().popPose();
        cursorY += 18;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 6;

        String[] body = {
            I18n.get("gripeverde.poster.elixir.body.1"),
            I18n.get("gripeverde.poster.elixir.body.2"),
            "",
            I18n.get("gripeverde.poster.elixir.price"),
            I18n.get("gripeverde.poster.elixir.unit")
        };
        for (String line : body) {
            drawCenteredText(g, font, line, cx, cursorY, DiaryTheme.INK_FADED);
            cursorY += 10;
        }
    }

    
    /** Panel derecho inferior del menú: rota entre 3 avisos con seed diaria. */
    public static void drawRotatingNoticeRight(GuiGraphics g, int x, int y, int w, int h) {
        long day = System.currentTimeMillis() / 86_400_000L;
        int variant = (int)(Math.abs(day) % 3);
        switch (variant) {
            case 1 -> drawRewardNotice(g, x, y, w, h);
            case 2 -> drawDecreeNotice(g, x, y, w, h);
            default -> drawPreventionNotice(g, x, y, w, h); // Elixir (existente)
        }
    }

    private static void drawRewardNotice(GuiGraphics g, int x, int y, int w, int h) {
        Font font = Minecraft.getInstance().font;
        OrnamentRenderer.drawPaperPanel(g, x, y, w, h);
        drawSimpleBorder(g, x, y, w, h, DiaryTheme.INK, DiaryTheme.SEPIA_MID);

        int cx = x + w / 2;
        int cursorY = y + 10;

        // Encabezado pequeño
        String header = "WANTED";
        int hw = font.width(header);
        g.drawString(font, header, cx - hw / 2, cursorY, DiaryTheme.INK, false);
        cursorY += 11;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 6;

        // Título "PATIENT ZERO" en rojo (escala 1.2 en vez de 1.3 para que quepa)
        g.pose().pushPose();
        g.pose().translate(cx, cursorY + 3, 0);
        g.pose().scale(1.2f, 1.2f, 1f);
        String title = I18n.get("gripeverde.poster.reward.title.short");
        int tw = font.width(title);
        g.drawString(font, title, -tw / 2 + 1, 1, DiaryTheme.SEPIA_DARK, false);
        g.drawString(font, title, -tw / 2, 0, DiaryTheme.BLOOD_DRIED, false);
        g.pose().popPose();
        cursorY += 18;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 8;

        drawCenteredText(g, font, I18n.get("gripeverde.poster.reward.body.1"), cx, cursorY, DiaryTheme.INK_FADED);
        cursorY += 10;
        drawCenteredText(g, font, I18n.get("gripeverde.poster.reward.body.2"), cx, cursorY, DiaryTheme.INK_FADED);
        cursorY += 14;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 8;

        // Prize compactado en una sola línea
        drawCenteredText(g, font, I18n.get("gripeverde.poster.reward.prize.short"), cx, cursorY, DiaryTheme.BLOOD_DRIED);
        cursorY += 10;
        drawCenteredText(g, font, I18n.get("gripeverde.poster.reward.prize.unit"), cx, cursorY, DiaryTheme.BRONZE);

        // Sello más pequeño y en posición segura dentro del panel
        OrnamentRenderer.drawWaxSeal(g, x + w - 18, y + h - 18, 7);
    }

    private static void drawDecreeNotice(GuiGraphics g, int x, int y, int w, int h) {
        Font font = Minecraft.getInstance().font;
        OrnamentRenderer.drawPaperPanel(g, x, y, w, h);
        drawSimpleBorder(g, x, y, w, h, DiaryTheme.INK, DiaryTheme.SEPIA_MID);

        int cx = x + w / 2;
        int cursorY = y + 10;

        drawCenteredText(g, font, I18n.get("gripeverde.poster.decree.header"), cx, cursorY, DiaryTheme.INK);
        cursorY += 12;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 8;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY + 4, 0);
        g.pose().scale(1.2f, 1.2f, 1f);
        String title = I18n.get("gripeverde.poster.decree.title");
        int tw = font.width(title);
        g.drawString(font, title, -tw / 2 + 1, 1, DiaryTheme.SEPIA_DARK, false);
        g.drawString(font, title, -tw / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();
        cursorY += 20;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 10;

        drawCenteredText(g, font, I18n.get("gripeverde.poster.decree.body.1"), cx, cursorY, DiaryTheme.INK_FADED);
        cursorY += 10;
        drawCenteredText(g, font, I18n.get("gripeverde.poster.decree.body.2"), cx, cursorY, DiaryTheme.INK_FADED);
        cursorY += 18;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 8;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY + 2, 0);
        g.pose().scale(1.1f, 1.1f, 1f);
        String warn = I18n.get("gripeverde.poster.decree.warn");
        int ww = font.width(warn);
        g.drawString(font, warn, -ww / 2 + 1, 1, DiaryTheme.SEPIA_DARK, false);
        g.drawString(font, warn, -ww / 2, 0, DiaryTheme.BLOOD_DRIED, false);
        g.pose().popPose();
    }

public static void drawVersionPanel(GuiGraphics g, int x, int y, int w, int h, String version) {
        Font font = Minecraft.getInstance().font;
        g.fill(x, y, x + w, y + h, 0xFF15100A);
        drawSimpleBorder(g, x, y, w, h, DiaryTheme.BRONZE, DiaryTheme.SEPIA_DARK);

        int cx = x + w / 2;

        String vol = I18n.get("gripeverde.panel.version.header");
        int vw = font.width(vol);
        g.drawString(font, vol, cx - vw / 2, y + 12, DiaryTheme.SEPIA_LIGHT, false);

        drawThinDivider(g, cx, y + 26, w - 40);

        g.pose().pushPose();
        g.pose().translate(cx, y + h / 2 + 4, 0);
        g.pose().scale(3.5f, 3.5f, 1f);
        int nw = font.width(version);
        g.drawString(font, version, -nw / 2 + 1, 1, DiaryTheme.SEPIA_DARK, false);
        g.drawString(font, version, -nw / 2, 0, DiaryTheme.BRONZE, false);
        g.pose().popPose();

        drawThinDivider(g, cx, y + h - 26, w - 40);

        String ad = I18n.get("gripeverde.panel.version.footer");
        int aw = font.width(ad);
        g.drawString(font, ad, cx - aw / 2, y + h - 16, DiaryTheme.SEPIA_LIGHT, false);
    }

    private static void drawCenteredText(GuiGraphics g, Font f, String s, int cx, int y, int color) {
        int tw = f.width(s);
        g.drawString(f, s, cx - tw / 2, y, color, false);
    }

    private static void drawThinDivider(GuiGraphics g, int cx, int y, int width) {
        int half = width / 2;
        int gap = 4;
        g.fill(cx - half, y, cx - gap, y + 1, DiaryTheme.SEPIA_MID);
        g.fill(cx + gap, y, cx + half, y + 1, DiaryTheme.SEPIA_MID);
        g.fill(cx - 1, y - 1, cx + 2, y + 2, DiaryTheme.OCHRE);
    }

    private static void drawSimpleBorder(GuiGraphics g, int x, int y, int w, int h, int outerColor, int innerColor) {
        g.fill(x, y, x + w, y + 1, outerColor);
        g.fill(x, y + h - 1, x + w, y + h, outerColor);
        g.fill(x, y, x + 1, y + h, outerColor);
        g.fill(x + w - 1, y, x + w, y + h, outerColor);
        g.fill(x + 2, y + 2, x + w - 2, y + 3, innerColor);
        g.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, innerColor);
        g.fill(x + 2, y + 2, x + 3, y + h - 2, innerColor);
        g.fill(x + w - 3, y + 2, x + w - 2, y + h - 2, innerColor);
    }
}