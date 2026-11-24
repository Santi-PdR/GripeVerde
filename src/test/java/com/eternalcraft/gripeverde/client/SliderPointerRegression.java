package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.client.widgets.DiarySlider;

/** Real generic slider/vanilla parent, no client, rendering or native input. */
public final class SliderPointerRegression {
    private static int checks;

    private static void check(boolean value, String reason) {
        checks++;
        if (!value) throw new AssertionError(reason);
    }

    private static final class Probe extends DiarySlider {
        private Probe(boolean live, int[] events, Runnable released) {
            super(0, 0, 108, 20, 0.25, value -> "Value " + value,
                value -> events[0]++, live, () -> {
                    events[1]++;
                    if (released != null) released.run();
                });
        }

        private double value() {
            return this.value;
        }
    }

    public static void main(String[] args) {
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
        System.out.println("Slider pointer regression: " + checks + " checks passed (real vanilla parent, no client).");
    }
}
