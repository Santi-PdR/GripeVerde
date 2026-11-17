package com.eternalcraft.gripeverde.client.render;

import com.eternalcraft.gripeverde.client.lore.Discoveries;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

/**
 * v0.240.0: el telegrama de defuncion.
 *
 * Antes la pantalla de muerte se forraba con dos franjas de papel de borde a
 * borde: quedaba aparatoso y tapaba el mundo. Ahora es un solo papel chico,
 * arriba y centrado, con el aviso de condolencias tal como lo recibian los
 * familiares: membrete, dos renglones y el sello. Nada mas se toca: ni velo, ni
 * oscurecido, ni animacion.
 *
 * Los botones del juego se visten de papel sin duplicar su logica: el original
 * se oculta y en su lugar se pone un boton de la casa que dispara su misma
 * accion (reaparecer, menu principal, espectador en modo extremo).
 */
public final class DeathPaper {

    private static final int PAPER_W_MAX = 340;
    private static final int PAPER_H = 78;

    private DeathPaper() {
    }

    /** Enganche desde ScreenEvent.Init.Post: solo para la pantalla de muerte. */
    public static void onInit(Screen screen) {
        if (!(screen instanceof DeathScreen)) return;
        try {
            skinButtons(screen);
        } catch (Throwable ignored) {
        }
    }

    private static void skinButtons(Screen screen) {
        List<Button> originals = new ArrayList<>();
        for (var child : screen.children()) {
            if (child instanceof Button button && button.visible && !(child instanceof DiaryButton)) {
                originals.add(button);
            }
        }
        for (Button origin : originals) {
            DiaryButton dressed = new DiaryButton(
                origin.getX(), origin.getY(), origin.getWidth(), origin.getHeight(),
                origin.getMessage(), DiaryButton.Icon.NONE, DiaryButton.Variant.SECONDARY,
                origin::onPress);
            // Solo se oculta el boton del juego si el de papel quedo en su sitio:
            // si algo falla, el jugador conserva los botones de siempre.
            if (register(screen, dressed)) {
                origin.visible = false;
            }
        }
    }

    /** Alta del widget sin pasar por Screen#addRenderableWidget (protegido). */
    private static boolean register(Screen screen, AbstractWidget widget) {
        boolean ok = false;
        try {
            List<Object> renderables = listField(screen, net.minecraft.client.gui.components.Renderable.class);
            if (renderables != null) {
                renderables.add(widget);
                ok = true;
            }
        } catch (Throwable ignored) {
        }
        if (!ok) return false;
        try {
            // Screen#children() devuelve la lista viva pero con comodin: se
            // castea para poder sumar el widget tambien al recorrido de clics.
            @SuppressWarnings("unchecked")
            List<Object> children = (List<Object>) (List<?>) screen.children();
            children.add(widget);
        } catch (Throwable ignored) {
        }
        try {
            List<Object> narratables = listField(screen,
                net.minecraft.client.gui.narration.NarratableEntry.class);
            if (narratables != null) narratables.add(widget);
        } catch (Throwable ignored) {
        }
        return true;
    }

    private static List<Object> listField(Object owner, Class<?> elementType) {
        for (java.lang.reflect.Field field : Screen.class.getDeclaredFields()) {
            if (!List.class.isAssignableFrom(field.getType())) continue;
            if (!(field.getGenericType() instanceof java.lang.reflect.ParameterizedType pt)) continue;
            if (pt.getActualTypeArguments().length != 1) continue;
            if (pt.getActualTypeArguments()[0] != elementType) continue;
            try {
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) field.get(owner);
                return list;
            } catch (Throwable ignored) {
                return null;
            }
        }
        return null;
    }

    public static void render(GuiGraphics g, Screen screen) {
        if (g == null || !(screen instanceof DeathScreen)) return;
        int w = screen.width;
        int h = screen.height;
        if (w < 200 || h < 210) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc != null ? mc.font : null;
        if (font == null) return;
        boolean english = Discoveries.isEnglish();

        int pw = Math.min(PAPER_W_MAX, w - 40);
        int px = (w - pw) / 2;
        int py = 8;
        PaperTexture.drawTornSheet(g, px, py, pw, PAPER_H, PaperTexture.PaperKind.OFFICIAL);

        int cx = w / 2;
        String head = english ? "CEDA - BUREAU OF PORTS" : "CEDA - SANIDAD DE PUERTOS";
        String line1 = english
            ? "Deepest regrets: the bearer is deceased."
            : "Lamento informar el deceso del portador.";
        String line2 = english
            ? "The report remains on file."
            : "El parte queda archivado.";

        int hy = py + 11;
        int hw = DiaryChrome.widthCached(font, head);
        g.drawString(font, head, cx - hw / 2, hy, DiaryChrome.INK_TITLE, false);
        DiaryChrome.drawPencilRule(g, px + 14, hy + 11, pw - 28, px * 31 + py * 17 + 5);

        int l1 = py + 28;
        g.drawString(font, DiaryChrome.fitCached(font, line1, pw - 28),
            cx - DiaryChrome.widthCached(font, DiaryChrome.fitCached(font, line1, pw - 28)) / 2,
            l1, DiaryChrome.INK_SUBTITLE, false);
        int l2 = l1 + 11;
        g.drawString(font, DiaryChrome.fitCached(font, line2, pw - 28),
            cx - DiaryChrome.widthCached(font, DiaryChrome.fitCached(font, line2, pw - 28)) / 2,
            l2, DiaryChrome.INK_SUBTITLE, false);

        // Pie del papel: registro a la izquierda y sello a la derecha.
        int footY = py + PAPER_H - 17;
        DiaryChrome.drawPencilRule(g, px + 14, footY - 5, pw - 28, px * 17 + py * 13 + 3);
        String reg = "EC-QV/1897";
        g.drawString(font, reg, px + 14, footY, DiaryChrome.INK_FLAVOR, false);

        String word = english ? "DECEASED" : "DECESO";
        int ww = DiaryChrome.widthCached(font, word);
        int sx = px + pw - 14 - ww;
        int stamp = DiaryTheme.withAlpha(0x005C0A08, 190);
        int stampInk = DiaryTheme.withAlpha(0x005C0A08, 215);
        g.fill(sx - 3, footY - 2, sx + ww + 3, footY - 1, stamp);
        g.fill(sx - 3, footY + 9, sx + ww + 3, footY + 10, stamp);
        g.fill(sx - 3, footY - 2, sx - 2, footY + 10, stamp);
        g.fill(sx + ww + 2, footY - 2, sx + ww + 3, footY + 10, stamp);
        g.drawString(font, word, sx, footY, stampInk, false);
    }
}
