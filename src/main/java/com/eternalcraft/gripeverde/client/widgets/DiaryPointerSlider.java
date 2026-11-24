package com.eternalcraft.gripeverde.client.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.network.chat.Component;

/** Tracks an accepted pointer gesture before allowing a slider to commit it. */
public abstract class DiaryPointerSlider extends AbstractSliderButton {
    private boolean pointerPressed;
    private boolean awaitingPointerFocus;

    protected DiaryPointerSlider(int x, int y, int width, int height, Component label, double value) {
        super(x, y, width, height, label, value);
    }

    /**
     * Route release to the focused slider that owns the press, even outside its
     * bounds. Vanilla otherwise routes release to the child under the cursor.
     * True also consumes a canceled (hidden/disabled) gesture, so it cannot fall
     * through to an unrelated widget. Secondary/unowned releases remain vanilla.
     */
    public static boolean releaseCaptured(ContainerEventHandler owner, double mouseX, double mouseY, int button) {
        if (!(owner.getFocused() instanceof DiaryPointerSlider slider)
                || !slider.pointerPressed || !slider.isValidClickButton(button)) return false;
        owner.setDragging(false);
        slider.mouseReleased(mouseX, mouseY, button);
        return true;
    }

    protected final boolean isPointerPressed() {
        return this.pointerPressed;
    }

    protected final void cancelPointerGesture() {
        this.pointerPressed = false;
        this.awaitingPointerFocus = false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        // Set before vanilla applies the initial click, not just the first drag.
        this.pointerPressed = true;
        this.awaitingPointerFocus = true;
        if (this.getWidth() <= 8) this.applyNarrowPointerValue(mouseX);
        else super.onClick(mouseX, mouseY);
    }

    private void applyNarrowPointerValue(double mouseX) {
        // Vanilla divides by width - 8: at 8px the center becomes NaN and
        // below 8px the direction reverses. Use the whole narrow control.
        double next = net.minecraft.util.Mth.clamp(
            (mouseX - this.getX()) / Math.max(1, this.getWidth()), 0.0D, 1.0D);
        double previous = this.value;
        this.value = next;
        if (previous != next) this.applyValue();
        this.updateMessage();
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        if (this.getWidth() <= 8) this.applyNarrowPointerValue(mouseX);
        else super.onDrag(mouseX, mouseY, dragX, dragY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!this.pointerPressed || !this.isValidClickButton(button)) return false;
        if (!this.active || !this.visible) {
            this.cancelPointerGesture();
            return false;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!this.pointerPressed || !this.isValidClickButton(button)) return false;
        // Clear first: repeated/reentrant release events cannot commit twice.
        this.cancelPointerGesture();
        if (!this.active || !this.visible) return false;
        boolean released = super.mouseReleased(mouseX, mouseY, button);
        if (released) this.commitPointerValue();
        return released;
    }

    @Override
    public void setFocused(boolean focused) {
        // ContainerEventHandler assigns focus AFTER mouseClicked and can send
        // false -> true even when reselecting this same widget. Preserve that
        // handoff, but cancel a subsequent genuine focus loss.
        if (focused) this.awaitingPointerFocus = false;
        else if (!this.awaitingPointerFocus) this.cancelPointerGesture();
        super.setFocused(focused);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        // Vanilla emits its own click here. commitPointerValue supplies the
        // diary sound; do not play both or perform side effects on bare release.
    }

    protected abstract void commitPointerValue();
}
