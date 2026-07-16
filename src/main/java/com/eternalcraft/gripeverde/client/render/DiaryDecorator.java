package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class DiaryDecorator {

    private DiaryDecorator() {}

    // FONDOS
    public static void drawPaperBackground(GuiGraphics g, int screenW, int screenH) {
        PaperTexture.draw(g, 0, 0, screenW, screenH);
    }

    public static void drawBloodyBackground(GuiGraphics g, int screenW, int screenH) {
        PaperTexture.drawBloody(g, 0, 0, screenW, screenH);
    }

    public static void drawFramedPaper(GuiGraphics g, int screenW, int screenH) {
        g.fill(0, 0, screenW, screenH, DiaryTheme.INK_BLACK);
        int margin = Math.min(screenW, screenH) / 40;
        PaperTexture.draw(g, margin, margin, screenW - margin * 2, screenH - margin * 2);
    }

    // TITULOS
    /** Titulo principal - sombra + quill stroke debajo. Sin manchas detras. */
    public static void drawTitle(GuiGraphics g, String text, int cx, int y, int color) {
        Minecraft mc = Minecraft.getInstance();
        int tw = mc.font.width(text);

        // Sombra 2px
        g.drawString(mc.font, text, cx - tw / 2 + 1, y + 1, DiaryTheme.withAlpha(DiaryTheme.INK, 180), false);
        // Texto principal
        g.drawString(mc.font, text, cx - tw / 2, y, color, false);

        // Trazo de pluma debajo
        InkEffects.quillStroke(g, cx - tw / 2 - 8, y + 12, cx + tw / 2 + 8, color);
    }

    /** Titulo con mancha de tinta al costado (opcional, no automatico). */
    public static void drawTitleWithBlot(GuiGraphics g, String text, int cx, int y, int color) {
        Minecraft mc = Minecraft.getInstance();
        int tw = mc.font.width(text);

        // Pequenia mancha al costado izquierdo del titulo
        InkEffects.inkBlot(g, cx - tw / 2 - 15, y + 4, 4);

        g.drawString(mc.font, text, cx - tw / 2 + 1, y + 1, DiaryTheme.withAlpha(DiaryTheme.INK, 180), false);
        g.drawString(mc.font, text, cx - tw / 2, y, color, false);
        InkEffects.quillStroke(g, cx - tw / 2 - 8, y + 12, cx + tw / 2 + 8, color);
    }

    public static void drawTitleSimple(GuiGraphics g, String text, int cx, int y, int color) {
        Minecraft mc = Minecraft.getInstance();
        int tw = mc.font.width(text);
        g.drawString(mc.font, text, cx - tw / 2 + 1, y + 1, DiaryTheme.withAlpha(DiaryTheme.INK, 120), false);
        g.drawString(mc.font, text, cx - tw / 2, y, color, false);
        InkEffects.handwrittenUnderline(g, cx - tw / 2 - 4, y + 11, cx + tw / 2 + 4, color);
    }

    public static void drawSubtitle(GuiGraphics g, String text, int cx, int y) {
        Minecraft mc = Minecraft.getInstance();
        int tw = mc.font.width(text);
        g.drawString(mc.font, text, cx - tw / 2, y, DiaryTheme.INK_FADED, false);
    }

    public static void drawSectionHeader(GuiGraphics g, String text, int x, int y, int color) {
        Minecraft mc = Minecraft.getInstance();
        int textColor = DiaryTheme.BLOOD_DRIED;
        int fleuronColor = DiaryTheme.BLOOD_DRIED;

        InkEffects.drawDiamond(g, x + 4, y + 4, 3, fleuronColor);
        g.drawString(mc.font, text, x + 14, y, textColor, false);
        int tw = mc.font.width(text);
        InkEffects.drawDiamond(g, x + 14 + tw + 6, y + 4, 3, fleuronColor);

        int lineCol = DiaryTheme.withAlpha(DiaryTheme.INK_FADED, 120);
        g.fill(x, y + 11, x + 14 + tw + 12, y + 12, lineCol);
    }

    // BOTONES
    public static void drawStampedButton(GuiGraphics g, int x, int y, int w, int h,
                                          String text, boolean hovered, boolean primary) {
        int bgCol = hovered
                ? DiaryTheme.withAlpha(DiaryTheme.INK, 220)
                : DiaryTheme.withAlpha(DiaryTheme.INK_FADED, 180);
        int borderCol = primary ? DiaryTheme.BLOOD_DRIED : DiaryTheme.INK;
        int textCol = hovered ? DiaryTheme.BONE_WHITE : DiaryTheme.PAPER;

        g.fill(x, y, x + w, y + h, bgCol);
        drawStampBorder(g, x, y, w, h, borderCol);

        Minecraft mc = Minecraft.getInstance();
        int tw = mc.font.width(text);
        int tx = x + (w - tw) / 2;
        int ty = y + (h - 8) / 2;
        g.drawString(mc.font, text, tx, ty, textCol, false);

        if (hovered) {
            InkEffects.handwrittenUnderline(g, tx, ty + 10, tx + tw, textCol);
        }
    }

    private static void drawStampBorder(GuiGraphics g, int x, int y, int w, int h, int col) {
        g.fill(x + 2, y, x + w - 2, y + 1, col);
        g.fill(x + 2, y + h - 1, x + w - 2, y + h, col);
        g.fill(x, y + 2, x + 1, y + h - 2, col);
        g.fill(x + w - 1, y + 2, x + w, y + h - 2, col);
        g.fill(x + 1, y + 1, x + 2, y + 2, col);
        g.fill(x + w - 2, y + 1, x + w - 1, y + 2, col);
        g.fill(x + 1, y + h - 2, x + 2, y + h - 1, col);
        g.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, col);
    }

    // MARCOS
    public static void drawDoubleFrame(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
        g.fill(x + 3, y + 3, x + w - 3, y + 4, color);
        g.fill(x + 3, y + h - 4, x + w - 3, y + h - 3, color);
        g.fill(x + 3, y + 3, x + 4, y + h - 3, color);
        g.fill(x + w - 4, y + 3, x + w - 3, y + h - 3, color);
    }

    public static void drawCornerFleurons(GuiGraphics g, int x, int y, int w, int h, int color) {
        InkEffects.drawDiamond(g, x + 4, y + 4, 3, color);
        InkEffects.drawDiamond(g, x + w - 5, y + 4, 3, color);
        InkEffects.drawDiamond(g, x + 4, y + h - 5, 3, color);
        InkEffects.drawDiamond(g, x + w - 5, y + h - 5, 3, color);
    }

    // TEXTOS
    public static void drawMarginNote(GuiGraphics g, String text, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        g.drawString(mc.font, text, x, y, DiaryTheme.INK_FADED, false);
    }

    public static void drawParagraph(GuiGraphics g, String text, int x, int y, int maxWidth) {
        Minecraft mc = Minecraft.getInstance();
        for (var line : mc.font.split(net.minecraft.network.chat.Component.literal(text), maxWidth)) {
            g.drawString(mc.font, line, x, y, DiaryTheme.INK, false);
            y += 10;
        }
    }

    public static void drawParagraphColored(GuiGraphics g, String text, int x, int y, int maxWidth, int color) {
        Minecraft mc = Minecraft.getInstance();
        for (var line : mc.font.split(net.minecraft.network.chat.Component.literal(text), maxWidth)) {
            g.drawString(mc.font, line, x, y, color, false);
            y += 10;
        }
    }
}