package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.client.screens.DiaryConfirmScreen;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.network.chat.Component;

/**
 * Real ConfirmScreen/DiaryButton hook, delay and cancellation, without a client.
 * Deliberately avoids init/render, native clicks and accepted-decision lore I/O.
 */
public final class ConfirmationRegression {
    private static int checks;

    private ConfirmationRegression() {}

    private static void check(boolean condition, String reason) {
        if (!condition) throw new AssertionError(reason);
        checks++;
    }

    private static final class Probe extends DiaryConfirmScreen {
        private Probe(BooleanConsumer callback) {
            // Equal labels expose accidental label-based decision routing.
            super(callback, Component.literal("Title"), Component.literal("Message"),
                Component.literal("Same"), Component.literal("Same"), null);
        }

        private void buildButtons() {
            this.clearWidgets();
            this.addButtons(0);
        }

        private DiaryButton yes() { return (DiaryButton) this.children().get(0); }
        private DiaryButton no() { return (DiaryButton) this.children().get(1); }
    }

    public static void main(String[] args) {
        int[] calls = {0};
        boolean[] accepted = {false};
        Probe focused = new Probe(value -> { calls[0]++; accepted[0] = value; });
        focused.setDelay(2);
        focused.buildButtons();
        check(focused.children().size() == 2, "Duplicate inherited buttons");
        check(!focused.yes().active && !focused.no().active, "Delay before build ignored");
        check(focused.keyPressed(257, 0, 0), "Delayed Enter not consumed");
        check(calls[0] == 0, "Enter bypassed delay");
        focused.setDelay(0);
        check(focused.yes().active && focused.no().active, "Zero delay did not enable");
        focused.setFocused(focused.no());
        check(focused.keyPressed(257, 0, 0), "Focused Enter not handled");
        check(calls[0] == 1 && !accepted[0], "Focused No/equal labels chose Yes");
        focused.no().onPress();
        focused.yes().onPress();
        focused.onClose();
        check(calls[0] == 1, "Completed decision repeated callback");
        check(!focused.yes().active && !focused.no().active, "Completed controls still enabled");
        focused.buildButtons();
        focused.setDelay(-1);
        check(!focused.yes().active && !focused.no().active, "Rebuild revived completed decision");

        int[] escapes = {0};
        Probe escape = new Probe(value -> {
            check(!value, "Escape accepted decision");
            escapes[0]++;
        });
        escape.buildButtons();
        escape.setDelay(20);
        check(escape.keyPressed(256, 0, 0), "Escape during delay not consumed");
        escape.onClose();
        check(escapes[0] == 1, "Cancellation did not report once");

        int[] reentries = {0};
        Probe[] holder = {null};
        holder[0] = new Probe(value -> {
            reentries[0]++;
            holder[0].onClose();
            holder[0].no().onPress();
        });
        holder[0].buildButtons();
        holder[0].onClose();
        check(reentries[0] == 1, "Cancellation callback reentered");

        System.out.println("Confirmation regression: " + checks + " checks passed (real parent/buttons, no client or rendering).");
    }
}
