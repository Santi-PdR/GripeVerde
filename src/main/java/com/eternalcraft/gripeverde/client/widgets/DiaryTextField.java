package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.render.DiaryTheme;
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
 * resueltos por scan de fields declarados en EditBox.class (sin herencia)
 * filtrando por tipo esperado. Esto evita colisiones con fields ambiguos.
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
        this.setBordered(false);
        this.setTextColor(DiaryTheme.INK);
        this.setTextColorUneditable(DiaryTheme.SEPIA_MID);
        initReflectionOnce();
    }

    private static void initReflectionOnce() {
        if (REFLECTION_INITED) return;
        REFLECTION_INITED = true;

        // Buscar por nombre oficial primero, luego SRG, luego scan por tipo
        FIELD_HIGHLIGHT   = findIntField("highlightPos", "f_94105_");
        FIELD_FRAME       = findIntField("frame",        "f_94100_");
        FIELD_DISPLAY_POS = findIntField("displayPos",   "f_94103_");
        FIELD_IS_EDITABLE = findBoolField("isEditable",  "f_94101_");
        FIELD_HINT        = findObjField("hint",         "f_169007_", Component.class);
        FIELD_FORMATTER   = findObjField("formatter",    "f_94108_",  BiFunction.class);
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
        int borderColor = focused ? DiaryTheme.REDCOAT_DARK : DiaryTheme.SEPIA_DARK;
        g.fill(x, y, x + w, y + 1, borderColor);
        g.fill(x, y + h - 1, x + w, y + h, borderColor);
        g.fill(x, y, x + 1, y + h, borderColor);
        g.fill(x + w - 1, y, x + w, y + h, borderColor);

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

        String tail = value.substring(displayPos);
        String visible = font.plainSubstrByWidth(tail, innerW);

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
            cursorX = cursorInside ? afterX : afterX;

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
        if (FIELD_FORMATTER == null) {
            return FormattedCharSequence.forward(text, Style.EMPTY);
        }
        try {
            @SuppressWarnings("unchecked")
            BiFunction<String, Integer, FormattedCharSequence> fn =
                    (BiFunction<String, Integer, FormattedCharSequence>) FIELD_FORMATTER.get(this);
            if (fn != null) return fn.apply(text, startPos);
        } catch (Throwable ignored) {}
        return FormattedCharSequence.forward(text, Style.EMPTY);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        boolean handled = super.charTyped(c, modifiers);
        if (handled) {
            // Sonido sutil de pluma al escribir
            com.eternalcraft.gripeverde.client.audio.UiSounds.scroll();
        }
        return handled;
    }
}
