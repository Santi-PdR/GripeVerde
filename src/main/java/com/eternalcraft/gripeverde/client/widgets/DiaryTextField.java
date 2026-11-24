package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.lang.reflect.Field;
import java.util.function.BiFunction;

/**
 * Campo de texto estilo diario victoriano.
 * Extiende EditBox vanilla para preservar TODA la logica de edicion
 * (cursor, seleccion, Ctrl+C/V, filter, responder, formatter, hint, etc.).
 *
 * Solo overridea renderWidget() para dibujar sobre pergamino en vez del
 * fondo negro vanilla.
 *
 * Estrategia: usar getters publicos donde existen (getValue, getCursorPosition,
 * getInnerWidth) y reflection SOLO para los pocos campos privados que
 * necesitamos (highlightPos, frame, displayPos, isEditable, hint, formatter),
 * resueltos por nombre oficial/SRG en EditBox.class (sin herencia), validando
 * el tipo esperado. No se adivinan campos por tipo: varios int son ambiguos.
 */
public class DiaryTextField extends EditBox {

    // Reflection cache (solo EditBox, no superclases)
    private static Field FIELD_HIGHLIGHT;
    private static Field FIELD_FRAME;
    private static Field FIELD_DISPLAY_POS;
    private static Field FIELD_IS_EDITABLE;
    private static Field FIELD_HINT;
    private static Field FIELD_FORMATTER;
    private static boolean REFLECTION_INITED = false;

    public DiaryTextField(Font font, int x, int y, int w, int h, Component narration) {
        super(font, x, y, w, h, narration);
        this.setBordered(true);
        this.setTextColor(DiaryTheme.INK);
        this.setTextColorUneditable(DiaryTheme.SEPIA_MID);
        initReflectionOnce();
    }

    /**
     * En EditBox, bordered también controla el inset de clic y getInnerWidth.
     * Nuestro renderer siempre usa 4 px de margen: conservar esa geometría
     * aunque un helper cosmético quite el fondo de los EditBox vanilla.
     * No aparece el borde vanilla, porque renderWidget no llama al del padre.
     */
    @Override
    public void setBordered(boolean bordered) {
        super.setBordered(true);
    }

    private static void initReflectionOnce() {
        if (REFLECTION_INITED) return;
        REFLECTION_INITED = true;

        // Minecraft 1.20.1: nombre oficial primero, luego SRG exacto y tipo.
        FIELD_HIGHLIGHT   = findIntField("highlightPos", "f_94102_");
        FIELD_FRAME       = findIntField("frame",        "f_94095_");
        FIELD_DISPLAY_POS = findIntField("displayPos",   "f_94100_");
        FIELD_IS_EDITABLE = findBoolField("isEditable",  "f_94098_");
        FIELD_HINT        = findObjField("hint",         "f_256828_", Component.class);
        FIELD_FORMATTER   = findObjField("formatter",    "f_94091_",  BiFunction.class);
    }

    private static Field findIntField(String official, String srg) {
        try { Field f = EditBox.class.getDeclaredField(official);
              if (f.getType() == int.class) { f.setAccessible(true); return f; }
        } catch (Throwable ignored) {}
        try { Field f = EditBox.class.getDeclaredField(srg);
              if (f.getType() == int.class) { f.setAccessible(true); return f; }
        } catch (Throwable ignored) {}
        return null;
    }

    private static Field findBoolField(String official, String srg) {
        try { Field f = EditBox.class.getDeclaredField(official);
              if (f.getType() == boolean.class) { f.setAccessible(true); return f; }
        } catch (Throwable ignored) {}
        try { Field f = EditBox.class.getDeclaredField(srg);
              if (f.getType() == boolean.class) { f.setAccessible(true); return f; }
        } catch (Throwable ignored) {}
        return null;
    }

    private static Field findObjField(String official, String srg, Class<?> expected) {
        try { Field f = EditBox.class.getDeclaredField(official);
              if (expected.isAssignableFrom(f.getType())) { f.setAccessible(true); return f; }
        } catch (Throwable ignored) {}
        try { Field f = EditBox.class.getDeclaredField(srg);
              if (expected.isAssignableFrom(f.getType())) { f.setAccessible(true); return f; }
        } catch (Throwable ignored) {}
        return null;
    }

    // v0.215.0: memoria del recorte visible (ver renderWidget).
    private String lastVisibleValue;
    private int lastVisibleDisplayPos = Integer.MIN_VALUE;
    private int lastVisibleInnerW = Integer.MIN_VALUE;
    private String lastVisibleText = "";

    private int readInt(Field f, int fallback) {
        if (f == null) return fallback;
        try { return f.getInt(this); } catch (Throwable ignored) { return fallback; }
    }

    private boolean readBool(Field f, boolean fallback) {
        if (f == null) return fallback;
        try { return f.getBoolean(this); } catch (Throwable ignored) { return fallback; }
    }

    @SuppressWarnings("unchecked")
    private <T> T readObj(Field f, T fallback) {
        if (f == null) return fallback;
        try { Object v = f.get(this); return v == null ? fallback : (T) v; }
        catch (Throwable ignored) { return fallback; }
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (!this.isVisible()) return;

        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;

        boolean focused = this.isFocused();

        // ---- FONDO PERGAMINO ----
        g.fill(x, y, x + w, y + h, DiaryTheme.PAPER_STAINED);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, DiaryTheme.withAlpha(0x00FFEECC, 90));
        g.fill(x, y, x + w, y + 1, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 120));
        g.fill(x, y + h - 1, x + w, y + h, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 60));

        // ---- BORDE ----
        // v0.238.0: el campo tambien lleva el filete a mano; el marco recto
        // delataba la caja igual que en los botones.
        int borderColor = focused ? DiaryTheme.REDCOAT_DARK : DiaryTheme.SEPIA_DARK;
        PaperTexture.drawWornBorder(g, x, y, w, h, borderColor, x * 31 + y * 17 + w);

        g.fill(x + 4, y + h - 4, x + w - 4, y + h - 3, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 38));
        g.fill(x + 3, y + 4, x + 4, y + h - 4, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 45));
        g.fill(x + w - 4, y + 4, x + w - 3, y + h - 4, DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, 55));

        // ---- LECTURA DE ESTADO ----
        Font font = net.minecraft.client.Minecraft.getInstance().font;
        String value       = this.getValue();               // publico
        int cursor         = this.getCursorPosition();      // publico
        int innerW         = this.getInnerWidth();          // publico
        int highlight      = readInt(FIELD_HIGHLIGHT, cursor);
        int frame          = readInt(FIELD_FRAME, 0);
        int displayPos     = readInt(FIELD_DISPLAY_POS, 0);
        boolean editable   = readBool(FIELD_IS_EDITABLE, true);
        Component hint     = readObj(FIELD_HINT, null);

        // Clamps defensivos
        if (displayPos < 0) displayPos = 0;
        if (displayPos > value.length()) displayPos = value.length();

        int textColor = editable ? DiaryTheme.INK : DiaryTheme.SEPIA_MID;

        int innerX = x + 4;
        int innerY = y + (h - 8) / 2;

        // v0.215.0: mientras el texto, el desplazamiento y el ancho no cambien, el
        // recorte visible es el mismo; no se vuelve a partir ni a medir.
        String visible;
        if (value == lastVisibleValue && displayPos == lastVisibleDisplayPos
                && innerW == lastVisibleInnerW) {
            visible = lastVisibleText;
        } else {
            visible = font.plainSubstrByWidth(value.substring(displayPos), innerW);
            lastVisibleValue = value;
            lastVisibleDisplayPos = displayPos;
            lastVisibleInnerW = innerW;
            lastVisibleText = visible;
        }

        int cursorRel = cursor - displayPos;
        boolean cursorInside = cursorRel >= 0 && cursorRel <= visible.length();
        int cursorX = innerX;

        // ---- SELECCION (dibujar ANTES del texto para que quede debajo) ----
        if (cursor != highlight) {
            int selStart = Math.min(cursor, highlight);
            int selEnd   = Math.max(cursor, highlight);
            int selStartRel = Math.max(selStart - displayPos, 0);
            int selEndRel   = Math.min(selEnd   - displayPos, visible.length());
            if (selEndRel > selStartRel) {
                int sx1 = innerX + font.width(visible.substring(0, selStartRel));
                int sx2 = innerX + font.width(visible.substring(0, selEndRel));
                g.fill(sx1, innerY - 1, sx2, innerY + 9, 0x60422810);
            }
        }

        // ---- TEXTO (con formatter si existe) ----
        if (!visible.isEmpty()) {
            String pre  = cursorInside ? visible.substring(0, Math.min(cursorRel, visible.length())) : visible;
            String post = cursorInside && cursorRel < visible.length() ? visible.substring(cursorRel) : "";

            FormattedCharSequence preSeq = formatSafe(pre, displayPos);
            int afterX = g.drawString(font, preSeq, innerX, innerY, textColor, false);
            cursorX = afterX;

            if (!post.isEmpty()) {
                FormattedCharSequence postSeq = formatSafe(post, cursor);
                g.drawString(font, postSeq, cursorX, innerY, textColor, false);
            }
        }

        // ---- HINT ----
        if (value.isEmpty() && hint != null && !focused) {
            g.drawString(font, hint, innerX, innerY, DiaryTheme.SEPIA_MID, false);
        }

        // ---- CURSOR ----
        boolean cursorBlink = focused && ((frame / 6) % 2 == 0);
        if (cursorBlink && cursorInside) {
            boolean cursorAtEnd = cursor >= value.length();
            if (cursorAtEnd) {
                // apend: subrayado grueso
                g.fill(cursorX, innerY + 8, cursorX + 5, innerY + 9, DiaryTheme.INK);
            } else {
                // insert: linea vertical
                g.fill(cursorX, innerY - 1, cursorX + 1, innerY + 9, DiaryTheme.INK);
            }
        }
    }

    private FormattedCharSequence formatSafe(String text, int startPos) {
        // Like vanilla, invoke the current formatter on every request. Its
        // result can depend on external state, not only text/cursor offsets.
        if (FIELD_FORMATTER != null) {
            try {
                @SuppressWarnings("unchecked")
                BiFunction<String, Integer, FormattedCharSequence> fn =
                    (BiFunction<String, Integer, FormattedCharSequence>) FIELD_FORMATTER.get(this);
                FormattedCharSequence seq = fn == null ? null : fn.apply(text, startPos);
                if (seq != null) return seq;
            } catch (Throwable ignored) {}
        }
        return FormattedCharSequence.forward(text, Style.EMPTY);
    }

    /** Text and both selection endpoints, independent of screen geometry/focus. */
    public record EditingState(String value, int cursor, int highlight) {}

    public EditingState snapshotEditingState() {
        int cursor = this.getCursorPosition();
        return new EditingState(this.getValue(), cursor, readInt(FIELD_HIGHLIGHT, cursor));
    }

    /** Restore only after the new field's max length and responders are set. */
    public void restoreEditingState(EditingState state) {
        if (state == null) return;
        this.setValue(state.value());
        this.setCursorPosition(state.cursor());
        this.setHighlightPos(state.highlight());
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        boolean handled = super.charTyped(c, modifiers);
        if (handled) {
            com.eternalcraft.gripeverde.client.audio.UiSounds.type();
        }
        return handled;
    }
}
