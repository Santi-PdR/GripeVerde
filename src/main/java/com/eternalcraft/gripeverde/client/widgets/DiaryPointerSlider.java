package com.eternalcraft.gripeverde.client.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/** Tracks an accepted pointer gesture before allowing a slider to commit it. */
abstract class DiaryPointerSlider extends AbstractSliderButton {
    private boolean pointerPressed;
    private boolean awaitingPointerFocus;

    protected DiaryPointerSlider(int x, int y, int width, int height, Component label, double value) {
        super(x, y, width, height, label, value);
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
        super.onClick(mouseX, mouseY);
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
