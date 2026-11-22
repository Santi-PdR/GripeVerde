package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.widgets.DiaryTextField;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

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

    public static void main(String[] args) {
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
        System.out.println("Text field geometry regression: " + checks + " checks passed (no rendering).");
    }
}
