package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class DiaryTypography {
    private DiaryTypography() {}

    public static int width(Font font, String text) {
        return font.width(text);
    }

    public static int width(Font font, Component text) {
        return font.width(text.getString());
    }

    public static int width(Font font, FormattedCharSequence text) {
        return font.width(text);
    }

    public static List<FormattedCharSequence> split(Font font, Component text, int maxWidth) {
        return font.split(Component.literal(text.getString()), Math.max(1, maxWidth));
    }

    public static List<FormattedCharSequence> split(Font font, String text, int maxWidth) {
        return split(font, Component.literal(text), maxWidth);
    }

    public static String fitString(Font font, String text, int maxWidth) {
        return fitString(font, text, maxWidth, true);
    }

    public static String fitString(Font font, String text, int maxWidth, boolean ellipsis) {
        if (text == null || text.isEmpty()) return "";
        int available = Math.max(0, maxWidth);
        if (width(font, text) <= available) return text;

        String suffix = ellipsis ? "…" : "";
        StringBuilder prefix = new StringBuilder(text.length());
        for (int offset = 0; offset < text.length();) {
            int codePoint = text.codePointAt(offset);
            int next = offset + Character.charCount(codePoint);
            String candidate = prefix.toString() + new String(Character.toChars(codePoint)) + suffix;
            if (width(font, candidate) > available) break;
            prefix.appendCodePoint(codePoint);
            offset = next;
        }
        return prefix + suffix;
    }

    public static void draw(GuiGraphics graphics, Font font, String text,
                            int x, int y, int color) {
        draw(graphics, font, text, x, y, color, false);
    }

    public static void draw(GuiGraphics graphics, Font font, String text,
                            int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text, x, y, color, shadow);
    }

    public static void draw(GuiGraphics graphics, Font font, Component text,
                            int x, int y, int color) {
        draw(graphics, font, text, x, y, color, false);
    }

    public static void draw(GuiGraphics graphics, Font font, Component text,
                            int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text.getString(), x, y, color, shadow);
    }

    public static void draw(GuiGraphics graphics, Font font, FormattedCharSequence text,
                            int x, int y, int color) {
        draw(graphics, font, text, x, y, color, false);
    }

    public static void draw(GuiGraphics graphics, Font font, FormattedCharSequence text,
                            int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text, x, y, color, shadow);
    }
}
