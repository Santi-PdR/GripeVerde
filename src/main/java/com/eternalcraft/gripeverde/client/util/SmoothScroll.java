package com.eternalcraft.gripeverde.client.util;

import net.minecraft.client.gui.components.AbstractSelectionList;

/**
 * Small inertial scroll controller for Diary-styled menus.
 *
 * Vanilla lists jump directly to the wheel delta. This keeps the target exact,
 * but eases the visible amount every tick so long menus feel fluid and modern.
 */
public final class SmoothScroll {

    private final double easing;
    private final double snapThreshold;
    private double target = 0.0;
    private boolean initialized = false;
    private AbstractSelectionList<?> attachedList;
    private double lastApplied = 0.0;

    public SmoothScroll() {
        this(0.42D, 0.30D);
    }

    public SmoothScroll(double easing) {
        this(easing, 0.30D);
    }

    public SmoothScroll(double easing, double snapThreshold) {
        this.easing = Math.max(0.10D, Math.min(0.85D, easing));
        this.snapThreshold = Math.max(0.05D, snapThreshold);
    }

    public void attach(AbstractSelectionList<?> list) {
        if (list == null) return;
        double current = list.getScrollAmount();
        if (!this.initialized || this.attachedList != list
                || Double.compare(current, this.lastApplied) != 0) {
            // La barra, el teclado o ensureVisible pueden mover la lista sin
            // pasar por wheel. Ese cambio cancela la inercia anterior.
            this.snapToCurrent(list);
        }
    }

    public void snapToCurrent(AbstractSelectionList<?> list) {
        if (list == null) return;
        this.attachedList = list;
        this.lastApplied = list.getScrollAmount();
        this.target = clamp(this.lastApplied, list);
        this.initialized = true;
    }

    public void setTarget(AbstractSelectionList<?> list, double target) {
        if (list == null) return;
        attach(list);
        this.target = clamp(target, list);
    }

    public boolean wheel(AbstractSelectionList<?> list, double delta, double pixelsPerNotch) {
        if (list == null) return false;
        attach(list);
        this.target = clamp(this.target - delta * pixelsPerNotch, list);
        return true;
    }

    public void tick(AbstractSelectionList<?> list) {
        if (list == null) return;
        attach(list);

        if (MotionPolicy.isReduced()) {
            // Reduced motion applies to functional scrolling too, including a
            // pending animation when the preference changes mid-flight.
            // The shared policy also keeps live-world scrolling immediate.
            this.target = clamp(this.target, list);
            list.setScrollAmount(this.target);
            this.lastApplied = list.getScrollAmount();
            this.target = this.lastApplied;
            return;
        }

        double current = list.getScrollAmount();
        double diff = this.target - current;
        double next;
        if (Math.abs(diff) < this.snapThreshold) {
            next = this.target;
        } else {
            next = current + diff * this.easing;
        }

        list.setScrollAmount(next);
        double actual = list.getScrollAmount();
        this.lastApplied = actual;

        // If the list clamped us, clamp the target too so the easing does not
        // keep pushing against an impossible boundary.
        if (Math.abs(actual - next) > 0.75D) {
            this.target = actual;
        }
    }

    private static double clamp(double value, AbstractSelectionList<?> list) {
        if (list == null) return 0.0;
        int max = Math.max(0, list.getMaxScroll());
        if (value < 0.0) return 0.0;
        if (value > max) return max;
        return value;
    }
}
