package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.client.widgets.DiarySlider;
import com.eternalcraft.gripeverde.client.widgets.DiaryPointerSlider;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;

/** Real generic slider/vanilla parent, no client, rendering or native input. */
public final class SliderPointerRegression {
    private static int checks;

    private static void check(boolean value, String reason) {
        checks++;
        if (!value) throw new AssertionError(reason);
    }

    private static final class Probe extends DiarySlider {
        private Probe(boolean live, int[] events, Runnable released) {
            this(108, live, events, released);
        }

        private Probe(int width, boolean live, int[] events, Runnable released) {
            super(0, 0, width, 20, 0.25, value -> "Value " + value,
                value -> events[0]++, live, () -> {
                    events[1]++;
                    if (released != null) released.run();
                });
        }

        private double value() {
            return this.value;
        }
    }

    private static final class OtherWidget implements GuiEventListener {
        private int releases;
        private boolean focused;

        @Override
        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        @Override
        public boolean isFocused() {
            return this.focused;
        }

        @Override
        public boolean isMouseOver(double x, double y) {
            return x >= 200 && x < 300 && y >= 0 && y < 40;
        }

        @Override
        public boolean mouseReleased(double x, double y, int button) {
            this.releases++;
            return true;
        }
    }

    private static final class ReleaseContainer implements ContainerEventHandler {
        private final Probe slider;
        private final OtherWidget other = new OtherWidget();
        private GuiEventListener focused;
        private boolean dragging = true;

        private ReleaseContainer(Probe slider) {
            this.slider = slider;
            // Focus ownership only: avoid querying Minecraft's last input type.
            this.focused = slider;
        }

        @Override
        public java.util.List<? extends GuiEventListener> children() {
            return java.util.List.of(this.other, this.slider);
        }

        @Override
        public GuiEventListener getFocused() {
            return this.focused;
        }

        @Override
        public void setFocused(GuiEventListener listener) {
            this.focused = listener;
        }

        @Override
        public boolean isDragging() {
            return this.dragging;
        }

        @Override
        public void setDragging(boolean dragging) {
            this.dragging = dragging;
        }

        @Override
        public boolean mouseReleased(double x, double y, int button) {
            if (DiaryPointerSlider.releaseCaptured(this, x, y, button)) return true;
            // Real vanilla hit-test dispatch, not a reimplementation.
            return ContainerEventHandler.super.mouseReleased(x, y, button);
        }
    }

    private static void capturedRelease() {
        int[] events = new int[2];
        Probe slider = new Probe(true, events, null);
        ReleaseContainer container = new ReleaseContainer(slider);
        slider.onClick(30, 10);
        check(slider.mouseDragged(250, 10, 0, 220, 0), "outside drag reaches real slider");
        check(slider.value() == 1.0, "outside drag clamps at endpoint");
        check(container.mouseReleased(250, 10, 0), "release over another widget captured");
        check(events[0] == 3 && events[1] == 1, "outside release commits last preview once");
        check(container.other.releases == 0 && !container.isDragging(), "capture bypasses hovered widget and clears dragging");
        check(!container.mouseReleased(500, 10, 0), "repeat outside release falls back without another commit");
        check(events[1] == 1, "repeat outside release preserves commit count");

        int[] fallbackEvents = new int[2];
        ReleaseContainer fallback = new ReleaseContainer(new Probe(true, fallbackEvents, null));
        check(fallback.mouseReleased(250, 10, 0), "unowned release uses vanilla hit-test route");
        check(fallback.other.releases == 1 && fallbackEvents[1] == 0, "unowned release belongs to hovered widget");

        int[] disabledEvents = new int[2];
        Probe disabled = new Probe(false, disabledEvents, null);
        ReleaseContainer canceled = new ReleaseContainer(disabled);
        disabled.onClick(30, 10);
        disabled.active = false;
        check(canceled.mouseReleased(250, 10, 0), "disabled owned release consumed");
        check(disabledEvents[0] == 0 && disabledEvents[1] == 0 && canceled.other.releases == 0,
            "disabled capture neither commits nor falls through");
        disabled.active = true;
        check(!disabled.mouseReleased(30, 10, 0), "disabled capture clears gesture");

        int[] deferredEvents = new int[2];
        Probe deferred = new Probe(false, deferredEvents, null);
        ReleaseContainer secondary = new ReleaseContainer(deferred);
        deferred.onClick(30, 10);
        check(secondary.mouseReleased(250, 10, 1), "secondary release keeps vanilla route");
        check(deferredEvents[1] == 0 && secondary.other.releases == 1, "secondary release does not finish left gesture");
        check(secondary.mouseReleased(500, 10, 0), "left capture survives cleared container dragging state");
        check(deferredEvents[0] == 1 && deferredEvents[1] == 1 && secondary.other.releases == 1,
            "deferred value commits once outside all children");
    }

    private static void narrowGeometry() {
        for (int width : new int[] {1, 4, 8, 9, 12}) {
            int[] events = new int[2];
            Probe slider = new Probe(width, true, events, null);
            slider.onClick(width / 2.0, 10);
            check(slider.value() == 0.5, "center click is finite midpoint at width " + width);
            check(slider.mouseDragged(-100, 10, 0, -100, 0), "left narrow drag accepted");
            check(slider.value() == 0.0, "left narrow drag clamps to zero, not one");
            check(slider.mouseDragged(200, 10, 0, 300, 0), "right narrow drag accepted");
            check(slider.value() == 1.0, "right narrow drag clamps to one, not zero");
            check(slider.mouseReleased(200, 10, 0), "narrow release accepted");
            check(events[1] == 1, "narrow release commits once");
        }
    }

    private static void keyboardGuards() throws Exception {
        int[] events = new int[2];
        Probe slider = new Probe(true, events, null);
        // Prime vanilla's retained editing state without invoking setFocused(true),
        // which queries Minecraft.getLastInputType. No native focus is exercised.
        var focus = net.minecraft.client.gui.components.AbstractWidget.class.getDeclaredField("focused");
        focus.setAccessible(true);
        var editing = net.minecraft.client.gui.components.AbstractSliderButton.class.getDeclaredField("canChangeValue");
        editing.setAccessible(true);
        for (int state = 0; state < 3; state++) {
            slider.active = state != 0;
            slider.visible = state != 1;
            focus.setBoolean(slider, state != 2);
            editing.setBoolean(slider, true);
            for (int key : new int[] {262, 263, 266, 267, 268, 269}) {
                check(!slider.keyPressed(key, 0, 0), "noninteractive keyboard input ignored");
            }
            check(slider.value() == 0.25 && events[0] == 0 && events[1] == 0,
                "hidden/inactive/unfocused slider cannot change through vanilla fallback");
        }
        slider.active = slider.visible = true;
        focus.setBoolean(slider, true);
        check(slider.keyPressed(262, 0, 0), "reenabled focused keyboard still works");
        check(events[0] == 1 && events[1] == 1, "reenabled key applies and commits once");
        check(!slider.mouseReleased(50, 10, 0), "keyboard edit does not arm pointer gesture");
    }

    public static void main(String[] args) throws Exception {
        int[] liveEvents = new int[2];
        Probe live = new Probe(true, liveEvents, null);
        check(!live.mouseReleased(50, 10, 0), "bare release must not invoke vanilla sound/client or commit");
        check(!live.mouseDragged(80, 10, 0, 30, 0), "bare drag ignored");
        check(liveEvents[0] == 0 && liveEvents[1] == 0, "bare events have no callbacks");
        // onClick bypasses AbstractWidget.mouseClicked's Minecraft sound-manager
        // lookup, but uses real AbstractSliderButton value mapping/apply hooks.
        live.onClick(50, 10);
        check(liveEvents[0] == 1 && liveEvents[1] == 0, "accepted click previews once");
        check(live.mouseDragged(80, 10, 0, 30, 0), "own drag accepted");
        check(liveEvents[0] == 2, "real vanilla drag applies once");
        check(live.value() == 0.76, "real vanilla handle geometry used");
        check(live.mouseReleased(80, 10, 0), "own release accepted without vanilla client access");
        check(liveEvents[0] == 3 && liveEvents[1] == 1, "release supplies final value and one commit");
        check(!live.mouseReleased(80, 10, 0), "repeated release ignored");
        check(liveEvents[0] == 3 && liveEvents[1] == 1, "repeated release has no callbacks");

        int[] deferredEvents = new int[2];
        Probe[] deferred = new Probe[1];
        deferred[0] = new Probe(false, deferredEvents,
            () -> check(!deferred[0].mouseReleased(80, 10, 0), "reentrant release ignored"));
        deferred[0].onClick(30, 10);
        check(deferredEvents[0] == 0, "deferred click does not apply early");
        check(deferred[0].mouseDragged(80, 10, 0, 50, 0), "deferred own drag accepted");
        check(deferredEvents[0] == 0 && deferredEvents[1] == 0, "deferred drag does not apply early");
        check(!deferred[0].mouseReleased(80, 10, 1), "secondary release does not end left gesture");
        check(deferred[0].mouseReleased(80, 10, 0), "deferred own release accepted");
        check(deferredEvents[0] == 1 && deferredEvents[1] == 1, "deferred value committed exactly once");

        int[] disabledEvents = new int[2];
        Probe disabled = new Probe(false, disabledEvents, null);
        disabled.onClick(50, 10);
        disabled.active = false;
        check(!disabled.mouseReleased(50, 10, 0), "disabled gesture not committed");
        disabled.active = true;
        check(!disabled.mouseReleased(50, 10, 0), "disabled gesture remains canceled after re-enable");
        check(disabledEvents[0] == 0 && disabledEvents[1] == 0, "disabled release has no callbacks");
        disabled.onClick(80, 10);
        disabled.visible = false;
        check(!disabled.mouseDragged(90, 10, 0, 10, 0), "hidden drag canceled");
        disabled.visible = true;
        check(!disabled.mouseReleased(90, 10, 0), "hidden drag cannot commit after show");
        check(disabledEvents[0] == 0 && disabledEvents[1] == 0, "hidden gesture has no callbacks");
        narrowGeometry();
        capturedRelease();
        keyboardGuards();
        System.out.println("Slider pointer regression: " + checks + " checks passed (real vanilla parent, no client).");
    }
}
