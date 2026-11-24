package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.widgets.DiaryTextField;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Check the real widget's logical inset, including calls from generic helpers.
 * A null font is sufficient for constructor/width checks: no rendering, glyph
 * lookup, real mouse events or Minecraft instance are exercised by this test.
 */
public final class TextFieldGeometryRegression {
    private static int checks;

    private TextFieldGeometryRegression() {
    }

    private static void innerWidth(EditBox field, int expected) {
        int actual = field.getInnerWidth();
        if (actual != expected) {
            throw new AssertionError("Expected inner width " + expected + ", got " + actual);
        }
        checks++;
    }

    private static void check(boolean condition, String reason) {
        if (!condition) throw new AssertionError(reason);
        checks++;
    }

    private static FormattedCharSequence format(DiaryTextField field, String text, int offset) throws Exception {
        Method method = DiaryTextField.class.getDeclaredMethod("formatSafe", String.class, int.class);
        method.setAccessible(true);
        return (FormattedCharSequence) method.invoke(field, text, offset);
    }

    private static String plain(FormattedCharSequence sequence) {
        StringBuilder text = new StringBuilder();
        sequence.accept((index, style, codepoint) -> {
            text.appendCodePoint(codepoint);
            return true;
        });
        return text.toString();
    }

    private static void reflectionBindings() throws Exception {
        String[][] fields = {
            {"FIELD_HIGHLIGHT", "highlightPos"}, {"FIELD_FRAME", "frame"},
            {"FIELD_DISPLAY_POS", "displayPos"}, {"FIELD_IS_EDITABLE", "isEditable"},
            {"FIELD_HINT", "hint"}, {"FIELD_FORMATTER", "formatter"}
        };
        for (String[] names : fields) {
            Field cache = DiaryTextField.class.getDeclaredField(names[0]);
            cache.setAccessible(true);
            Field actual = (Field) cache.get(null);
            check(actual != null && actual.equals(EditBox.class.getDeclaredField(names[1])),
                "Wrong binding for " + names[0]);
        }
    }

    private static void editingState() {
        DiaryTextField field = new DiaryTextField(null, 0, 0, 100, 20, Component.literal("Draft"));
        field.setValue("abcdef");
        field.setCursorPosition(2);
        field.setHighlightPos(5);
        DiaryTextField.EditingState state = field.snapshotEditingState();
        check(state.equals(new DiaryTextField.EditingState("abcdef", 2, 5)), "Forward selection lost");
        DiaryTextField replacement = new DiaryTextField(null, 0, 0, 100, 20, Component.literal("Draft"));
        replacement.restoreEditingState(state);
        check(replacement.snapshotEditingState().equals(state), "Restored selection differs");
        field.setCursorPosition(5);
        field.setHighlightPos(1);
        check(field.snapshotEditingState().equals(new DiaryTextField.EditingState("abcdef", 5, 1)), "Reverse selection lost");
        replacement.setMaxLength(3);
        replacement.restoreEditingState(state);
        check(replacement.snapshotEditingState().equals(new DiaryTextField.EditingState("abc", 2, 3)), "Restore bypassed clamps");
        state = field.snapshotEditingState();
        field.restoreEditingState(null);
        check(field.snapshotEditingState().equals(state), "Null state overwrote draft");
        int[] changes = {0};
        field.setResponder(value -> changes[0]++);
        field.snapshotEditingState();
        check(changes[0] == 0, "Snapshot notified responder");
        field.restoreEditingState(new DiaryTextField.EditingState("abc", 1, 2));
        check(changes[0] > 0, "Restore did not notify validation through setValue");
    }

    private static void formatters() throws Exception {
        DiaryTextField field = new DiaryTextField(null, 0, 0, 100, 20, Component.literal("Format"));
        FormattedCharSequence a = FormattedCharSequence.forward("first", Style.EMPTY);
        FormattedCharSequence b = FormattedCharSequence.forward("second", Style.EMPTY);
        field.setFormatter((text, offset) -> a);
        check(format(field, "same", 0) == a, "Custom formatter ignored");
        field.setFormatter((text, offset) -> b);
        check(format(field, "same", 0) == b, "Formatter replacement cached stale output");
        int[] calls = {0};
        field.setFormatter((text, offset) -> ++calls[0] == 1 ? a : b);
        check(format(field, "same", 0) == a, "First stateful result wrong");
        check(format(field, "same", 0) == b, "Stateful formatter cached stale output");
        field.setFormatter((text, offset) -> null);
        check(plain(format(field, "fallback", 2)).equals("fallback"), "Null formatter result not guarded");
        field.setFormatter((text, offset) -> { throw new IllegalStateException("test failure"); });
        check(plain(format(field, "fallback", 2)).equals("fallback"), "Throwing formatter lost text");
        boolean[] fail = {true};
        field.setFormatter((text, offset) -> {
            if (fail[0]) throw new IllegalStateException("temporary");
            return b;
        });
        check(plain(format(field, "retry", 0)).equals("retry"), "Temporary failure lost text");
        fail[0] = false;
        check(format(field, "retry", 0) == b, "Fallback cache prevented recovery");
    }

    public static void main(String[] args) throws Exception {
        DiaryTextField field = new DiaryTextField(null, 10, 20, 100, 20, Component.literal("Address"));
        innerWidth(field, 92);
        field.setBordered(false);
        innerWidth(field, 92);
        field.setBordered(true);
        innerWidth(field, 92);
        DiaryChrome.dressEditBox(field);
        innerWidth(field, 92);
        field.setWidth(160);
        innerWidth(field, 152);
        field.setBordered(false);
        innerWidth(field, 152);
        field.setWidth(64);
        innerWidth(field, 56);

        // A second instance must not inherit the previous one's dimensions.
        DiaryTextField second = new DiaryTextField(null, 0, 0, 200, 18, Component.literal("Name"));
        innerWidth(second, 192);
        innerWidth(field, 56);

        // The rule belongs only to DiaryTextField; vanilla remains borderless
        // when its own renderer and helpers request it.
        EditBox vanilla = new EditBox(null, 0, 0, 100, 20, Component.literal("Vanilla"));
        innerWidth(vanilla, 92);
        vanilla.setBordered(false);
        innerWidth(vanilla, 100);
        DiaryChrome.dressEditBox(vanilla);
        innerWidth(vanilla, 100);
        reflectionBindings();
        editingState();
        formatters();
        System.out.println("Text field geometry regression: " + checks + " checks passed (no rendering).");
    }
}
