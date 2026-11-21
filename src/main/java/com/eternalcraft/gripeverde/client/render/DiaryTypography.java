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
        return widthMemo(font, text);
    }

    public static int width(Font font, Component text) {
        return widthMemo(font, text.getString());
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

    /**
     * v0.193.0: memoria de recortes.
     *
     * fitString se llama varias veces por cuadro en las cabeceras, los rotulos de
     * botones y las listas, y cada llamada mide la cadena por biseccion. El
     * resultado solo depende de la fuente, del ancho y del texto, asi que se
     * guarda y se repite tal cual. La memoria se tira entera cuando cambia la
     * fuente (idioma o pack de recursos) o cuando crece demasiado.
     */
    private static Font fitMemoFont;
    // Ancho y modo como primer nivel, texto como segundo: sin claves armadas a
    // mano, no hay forma de que dos recortes distintos caigan en la misma casilla.
    private static final java.util.HashMap<String, java.util.HashMap<String, String>> FIT_MEMO =
        new java.util.HashMap<>();
    private static final int FIT_MEMO_BUCKET_MAX = 256;
    private static final int FIT_MEMO_BUCKETS_MAX = 48;

    public static String fitString(Font font, String text, int maxWidth, boolean ellipsis) {
        if (text == null || text.isEmpty()) return "";
        if (font == null) return text;
        if (fitMemoFont != font) {
            FIT_MEMO.clear();
            fitMemoFont = font;
        }
        String bucketKey = maxWidth + (ellipsis ? "e" : "p");
        java.util.HashMap<String, String> bucket = FIT_MEMO.get(bucketKey);
        if (bucket == null) {
            if (FIT_MEMO.size() >= FIT_MEMO_BUCKETS_MAX) FIT_MEMO.clear();
            bucket = new java.util.HashMap<>();
            FIT_MEMO.put(bucketKey, bucket);
        }
        String memo = bucket.get(text);
        if (memo != null) return memo;
        String value = fitStringUncached(font, text, maxWidth, ellipsis);
        if (bucket.size() >= FIT_MEMO_BUCKET_MAX) bucket.clear();
        bucket.put(text, value);
        return value;
    }

    private static String fitStringUncached(Font font, String text, int maxWidth, boolean ellipsis) {
        // v0.384.0: la casa no remata lo que escribe con puntos suspensivos. Da
        // igual el modo con el que se pida: se corta en la ultima palabra
        // completa que entra, y punto.
        return com.eternalcraft.gripeverde.client.render.DiaryChrome.shorten(font, text, maxWidth);
    }

    /**
     * v0.186.0: dibuja el texto encogido lo justo para entrar en {@code maxWidth}.
     *
     * Los botones ya recortaban con puntos suspensivos, pero un rotulo como
     * "ABRIR EL GABINETE" quedaba apretado o cortado. Encoger la letra (hasta
     * {@code minScale}) deja la etiqueta entera y centrada, que es lo que se ve
     * ordenado.
     *
     * @return el ancho real que ocupa el texto ya dibujado
     */
    // v0.210.0: el recorte/ajuste de un rotulo pregunta su ancho en cada cuadro
    // (cada boton, cada vez). El ancho de un texto no cambia: se guarda.
    // v0.213.0: la memoria se ata a la fuente en vez de meterla en la clave: al
    // cambiar la fuente (idioma, pack de recursos) se tira entera, y la clave
    // pasa a ser el texto pelado, sin armar una cadena por consulta.
    private static final java.util.HashMap<String, Integer> WIDTH_MEMO = new java.util.HashMap<>();
    private static Font widthMemoFont;

    private static int widthMemo(Font font, String text) {
        if (font != widthMemoFont) {
            WIDTH_MEMO.clear();
            widthMemoFont = font;
        }
        Integer cached = WIDTH_MEMO.get(text);
        if (cached != null) return cached;
        int width = font.width(text);
        if (WIDTH_MEMO.size() >= 512) WIDTH_MEMO.clear();
        WIDTH_MEMO.put(text, width);
        return width;
    }

    public static int drawFitted(GuiGraphics graphics, Font font, String text,
                                 int centerX, int topY, int maxWidth, int color, float minScale) {
        if (text == null || text.isEmpty()) return 0;
        int tw = widthMemo(font, text);
        if (tw <= 0) return 0;
        float scale = tw <= maxWidth ? 1f : Math.max(minScale, maxWidth / (float) tw);
        if (scale >= 1f) {
            graphics.drawString(font, text, centerX - tw / 2, topY, color, false);
            return tw;
        }
        int drawnW = Math.max(1, Math.round(tw * scale));
        float dy = (font.lineHeight - font.lineHeight * scale) / 2f;
        graphics.pose().pushPose();
        graphics.pose().translate(centerX - drawnW / 2f, topY + dy, 0f);
        graphics.pose().scale(scale, scale, 1f);
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
        return drawnW;
    }

    /** Igual que {@link #drawFitted}, pero alineado a la izquierda. */
    public static int drawFittedLeft(GuiGraphics graphics, Font font, String text,
                                     int leftX, int topY, int maxWidth, int color, float minScale) {
        if (text == null || text.isEmpty()) return 0;
        int tw = widthMemo(font, text);
        if (tw <= 0) return 0;
        float scale = tw <= maxWidth ? 1f : Math.max(minScale, maxWidth / (float) tw);
        if (scale >= 1f) {
            graphics.drawString(font, text, leftX, topY, color, false);
            return tw;
        }
        int drawnW = Math.max(1, Math.round(tw * scale));
        float dy = (font.lineHeight - font.lineHeight * scale) / 2f;
        graphics.pose().pushPose();
        graphics.pose().translate(leftX, topY + dy, 0f);
        graphics.pose().scale(scale, scale, 1f);
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
        return drawnW;
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
