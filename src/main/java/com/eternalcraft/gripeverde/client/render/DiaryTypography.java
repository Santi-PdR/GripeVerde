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
        if (width(font, suffix) > available) return "";
        // v0.185.0: busqueda binaria sobre los puntos de codigo. Antes se armaba
        // la cadena caracter por caracter y se medía cada intento: cuadratico por
        // rotulo, y esto se llama varias veces por cuadro en las cabeceras.
        int[] points = text.codePoints().toArray();
        int lo = 0;
        int hi = points.length;
        while (lo < hi) {
            int mid = (lo + hi + 1) >>> 1;
            String candidate = new String(points, 0, mid) + suffix;
            if (width(font, candidate) <= available) lo = mid; else hi = mid - 1;
        }
        return new String(points, 0, lo) + suffix;
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
