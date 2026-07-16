package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LogoRenderer {

    public static void draw(GuiGraphics g, int cx, int topY) {
        Font font = Minecraft.getInstance().font;

        drawTopHatSkull(g, cx, topY + 6);
        drawDecorativeLine(g, cx, topY + 46, 180);

        String title = I18n.get("gripeverde.brand.name");
        g.pose().pushPose();
        g.pose().translate(cx, topY + 60, 0);
        g.pose().scale(2.2f, 2.2f, 1f);
        int tw = font.width(title);
        g.drawString(font, title, -tw / 2 + 1, 1, 0xFF1A0F08, false);
        g.drawString(font, title, -tw / 2, 0, DiaryTheme.PAPER, false);
        g.pose().popPose();

        drawOrnamentalDivider(g, cx, topY + 92);

        String subtitle = I18n.get("gripeverde.brand.subtitle");
        g.pose().pushPose();
        g.pose().translate(cx, topY + 100, 0);
        g.pose().scale(1.8f, 1.8f, 1f);
        int sw = font.width(subtitle);
        g.drawString(font, subtitle, -sw / 2 + 1, 1, 0xFF1A2810, false);
        g.drawString(font, subtitle, -sw / 2, 0, DiaryTheme.PLAGUE_TOXIC, false);
        g.pose().popPose();

        String year = I18n.get("gripeverde.brand.year");
        int yw = font.width(year);
        g.drawString(font, year, cx - yw / 2, topY + 128, DiaryTheme.SEPIA_LIGHT, false);
    }

    private static void drawTopHatSkull(GuiGraphics g, int cx, int topY) {
        g.fill(cx - 8, topY, cx + 9, topY + 12, DiaryTheme.INK);
        g.fill(cx - 8, topY + 10, cx + 9, topY + 12, DiaryTheme.SEPIA_MID);
        g.fill(cx - 12, topY + 12, cx + 13, topY + 15, DiaryTheme.INK);
        g.fill(cx - 7, topY + 1, cx - 5, topY + 8, DiaryTheme.SEPIA_DARK);
        g.fill(cx - 7, topY + 15, cx + 8, topY + 26, DiaryTheme.PAPER);
        g.fill(cx - 6, topY + 14, cx + 7, topY + 15, DiaryTheme.PAPER);
        g.fill(cx - 6, topY + 26, cx + 7, topY + 28, DiaryTheme.PAPER);
        g.fill(cx - 6, topY + 25, cx + 7, topY + 27, DiaryTheme.PAPER_STAINED);
        g.fill(cx - 5, topY + 18, cx - 2, topY + 22, DiaryTheme.INK);
        g.fill(cx + 3, topY + 18, cx + 6, topY + 22, DiaryTheme.INK);
        g.fill(cx + 4, topY + 19, cx + 5, topY + 20, DiaryTheme.PLAGUE_TOXIC);
        g.fill(cx, topY + 22, cx + 1, topY + 24, DiaryTheme.INK);
        g.fill(cx - 1, topY + 24, cx + 2, topY + 25, DiaryTheme.INK);
        g.fill(cx - 4, topY + 27, cx + 5, topY + 30, DiaryTheme.PAPER);
        g.fill(cx - 3, topY + 27, cx - 2, topY + 30, DiaryTheme.INK);
        g.fill(cx - 1, topY + 27, cx, topY + 30, DiaryTheme.INK);
        g.fill(cx + 1, topY + 27, cx + 2, topY + 30, DiaryTheme.INK);
        g.fill(cx + 3, topY + 27, cx + 4, topY + 30, DiaryTheme.INK);
    }

    private static void drawDecorativeLine(GuiGraphics g, int cx, int y, int totalWidth) {
        int half = totalWidth / 2;
        int gapCenter = 20;
        g.fill(cx - half, y, cx - gapCenter, y + 1, DiaryTheme.SEPIA_LIGHT);
        g.fill(cx - half, y + 2, cx - gapCenter, y + 3, DiaryTheme.SEPIA_MID);
        g.fill(cx + gapCenter, y, cx + half, y + 1, DiaryTheme.SEPIA_LIGHT);
        g.fill(cx + gapCenter, y + 2, cx + half, y + 3, DiaryTheme.SEPIA_MID);
        g.fill(cx - 2, y, cx + 3, y + 3, DiaryTheme.GOLD_TARNISHED);
        g.fill(cx - 3, y + 1, cx + 4, y + 2, DiaryTheme.GOLD_TARNISHED);
        g.fill(cx - 1, y - 1, cx + 2, y, DiaryTheme.GOLD_TARNISHED);
        g.fill(cx - 1, y + 3, cx + 2, y + 4, DiaryTheme.GOLD_TARNISHED);
    }

    private static void drawOrnamentalDivider(GuiGraphics g, int cx, int y) {
        int half = 120;
        g.fill(cx - half, y, cx - 15, y + 1, DiaryTheme.SEPIA_LIGHT);
        g.fill(cx + 15, y, cx + half, y + 1, DiaryTheme.SEPIA_LIGHT);
        g.fill(cx - 15, y - 2, cx - 12, y + 3, DiaryTheme.OCHRE);
        g.fill(cx - 12, y - 1, cx - 10, y + 2, DiaryTheme.OCHRE);
        g.fill(cx + 12, y - 2, cx + 15, y + 3, DiaryTheme.OCHRE);
        g.fill(cx + 10, y - 1, cx + 12, y + 2, DiaryTheme.OCHRE);
        g.fill(cx - 1, y - 1, cx + 2, y + 2, DiaryTheme.GOLD_TARNISHED);
    }
}