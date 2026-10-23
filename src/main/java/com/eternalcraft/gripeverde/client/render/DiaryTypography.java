package com.eternalcraft.gripeverde.client.render;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Style;

/** Shared type styles for the cabinet's identity text. */
public final class DiaryTypography {
    public static final ResourceLocation DISPLAY_FONT =
        ResourceLocation.fromNamespaceAndPath("minecraft", "default");

    // Heading Components are stable for the lifetime of a screen. Reuse the
    // styled copy and measured width between render frames.
    private static Component cachedSource;
    private static Component cachedDisplay;
    private static String cachedPlainText;
    private static Style cachedSourceStyle;
    private static Language cachedLanguage;
    private static Font cachedMeasureFont;
    private static int cachedDisplayWidth = -1;
    private static String cachedLiteralText;
    private static Component cachedLiteralComponent;

    private DiaryTypography() {}

    public static Component display(Component text) {
        return cachedDisplay(text);
    }

    public static Component display(String text) {
        return display(literalComponent(text));
    }

    public static int drawCentered(GuiGraphics graphics, Font font, Component text,
                                   int centerX, int y, int color) {
        Component displayText = cachedDisplay(text);
        if (cachedMeasureFont != font || cachedDisplayWidth < 0) {
            cachedMeasureFont = font;
            cachedDisplayWidth = font.width(displayText);
        }
        int width = cachedDisplayWidth;
        graphics.drawString(font, displayText, centerX - width / 2, y, color, false);
        return width;
    }

    public static int drawCentered(GuiGraphics graphics, Font font, String text,
                                   int centerX, int y, int color) {
        return drawCentered(graphics, font, literalComponent(text), centerX, y, color);
    }

    private static Component cachedDisplay(Component text) {
        Language language = Language.getInstance();
        String plainText = text.getString();
        Style sourceStyle = text.getStyle();
        if (text != cachedSource || language != cachedLanguage
                || !plainText.equals(cachedPlainText) || !sourceStyle.equals(cachedSourceStyle)) {
            cachedSource = text;
            cachedPlainText = plainText;
            cachedSourceStyle = sourceStyle;
            cachedLanguage = language;
            // Keep these headings on Minecraft's dependable default font. A
            // bundled TTF provider rendered tofu for menu headings with some
            // installed font mods/resource packs, while body text stayed legible.
            cachedDisplay = text.copy().withStyle(style -> style.withFont(DISPLAY_FONT));
            cachedMeasureFont = null;
            cachedDisplayWidth = -1;
        }
        return cachedDisplay;
    }

    private static Component literalComponent(String text) {
        if (cachedLiteralComponent == null || !text.equals(cachedLiteralText)) {
            cachedLiteralText = text;
            cachedLiteralComponent = Component.literal(text);
        }
        return cachedLiteralComponent;
    }
}
