package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Confirmacion generica del cronista.
 * Reemplaza el fondo dirt vanilla + botones grises por pergamino + DiaryButtons.
 * Reutilizable para: Delete server, Delete pack, y cualquier futura confirmacion.
 */
public class DiaryConfirmScreen extends ConfirmScreen {

    private static final int PANEL_MAX_W = 340;
    private static final int PANEL_MAX_H = 180;

    private final Screen parent;
    // Guardamos el mensaje propio porque ConfirmScreen.message es private
    private final Component ownMessage;
    private MultiLineLabel ownMultilineMessage;
    private DiaryButton diaryYes;
    private DiaryButton diaryNo;
    private int decisionDelay;
    private boolean decisionMade;

    public DiaryConfirmScreen(BooleanConsumer callback, Component title, Component message, Screen parent) {
        super(callback, title, message);
        this.parent = parent;
        this.ownMessage = message;
    }

    public DiaryConfirmScreen(BooleanConsumer callback, Component title, Component message,
                              Component yesLabel, Component noLabel, Screen parent) {
        super(callback, title, message, yesLabel, noLabel);
        this.parent = parent;
        this.ownMessage = message;
    }

    // v0.272.0: la hoja solo suena la primera vez que se abre, no al redimensionar.
    private boolean paperSoundDone = false;

    @Override
    protected void init() {
        // v0.272.0: la hoja se abre con ruido de papel, una vez por apertura.
        if (!this.paperSoundDone) {
            this.paperSoundDone = true;
            try {
                com.eternalcraft.gripeverde.client.audio.UiSounds.open();
                UiSounds.notification();
            } catch (Throwable ignored) {
            }
        }
        this.ownMultilineMessage = null;
        super.init();
    }

    private int panelWidth() {
        return Math.min(PANEL_MAX_W, Math.max(1, this.width - 40));
    }

    private int panelHeight() {
        return Math.min(PANEL_MAX_H, Math.max(1, this.height - 30));
    }

    /** Use the parent's supported hook: no hidden duplicate buttons/reflection. */
    @Override
    protected void addButtons(int ignoredY) {
        // Botones Diary propios
        int gap = 12;
        int btnW = Math.max(1, Math.min(100, (this.panelWidth() - 32 - gap) / 2));
        int btnH = 20;
        int panelH = this.panelHeight();
        int panelY = (this.height - panelH) / 2;
        int btnY = panelY + panelH - btnH - 16;
        int totalW = btnW * 2 + gap;
        int startX = (this.width - totalW) / 2;

        this.diaryYes = new DiaryButton(startX, btnY, btnW, btnH,
                this.yesButton, DiaryButton.Icon.NONE, DiaryButton.Variant.PRIMARY,
                () -> this.finishDecision(true));
        this.diaryNo = new DiaryButton(startX + btnW + gap, btnY, btnW, btnH,
                this.noButton, DiaryButton.Icon.DOOR, DiaryButton.Variant.SECONDARY,
                () -> this.finishDecision(false));
        this.addRenderableWidget(this.diaryYes);
        this.addRenderableWidget(this.diaryNo);
        this.updateDecisionButtons();
    }

    private void updateDecisionButtons() {
        boolean enabled = !this.decisionMade && this.decisionDelay == 0;
        if (this.diaryYes != null) this.diaryYes.active = enabled;
        if (this.diaryNo != null) this.diaryNo.active = enabled;
    }

    @Override
    public void setDelay(int ticks) {
        this.decisionDelay = Math.max(0, ticks);
        this.updateDecisionButtons();
    }

    private void finishDecision(boolean accepted) {
        if (this.decisionMade || (accepted && this.decisionDelay > 0)) return;
        // Latch before lore/audio/callbacks: reentry cannot repeat destruction.
        this.decisionMade = true;
        this.updateDecisionButtons();
        if (accepted) {
            try {
                int n = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.bump("destroys");
                com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                    com.eternalcraft.gripeverde.client.audio.ModSounds.UI_CLICK_DESTROY.get(), 0.26f, 0.72f);
                if (n >= 3) {
                    com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                        com.eternalcraft.gripeverde.client.lore.Discoveries.DESTROYER);
                    com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                        com.eternalcraft.gripeverde.client.lore.Marginalia.N_DESTROY);
                }
            } catch (Throwable ignored) {}
        }
        this.callback.accept(accepted);
    }

    @Override
    public void resize(net.minecraft.client.Minecraft mc, int width, int height) {
        boolean focusedYes = this.diaryYes != null && this.getFocused() == this.diaryYes;
        boolean focusedNo = this.diaryNo != null && this.getFocused() == this.diaryNo;
        super.resize(mc, width, height);
        if (focusedNo) this.setFocused(this.diaryNo);
        else if (focusedYes) this.setFocused(this.diaryYes);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.decisionDelay > 0 && --this.decisionDelay == 0) this.updateDecisionButtons();
        // Mantener escena, musica y ambiente en un unico tick fijo.
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        // v0.250.0: por partes. Si un adorno falla, la hoja y los botones ya
        // estan puestos: nunca se ve la confirmacion cruda del juego.
        try {
            com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer
                .renderDiaryBackground(gfx, this.width, this.height);
        } catch (Throwable ignored) {
        }

        int panelW = this.panelWidth();
        int panelH = this.panelHeight();
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        try {
            DiaryChrome.drawPanelShadow(gfx, panelX, panelY, panelW, panelH);
        } catch (Throwable ignored) {
        }
        try {
            PaperTexture.drawTornSheet(gfx, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.OFFICIAL);
        } catch (Throwable ignored) {
        }

        try {
            DiaryChrome.drawCornerOrnaments(gfx, panelX, panelY, panelW, panelH);
            DiaryChrome.drawRegistryTag(gfx, panelX, panelY, panelW, panelH, "REG. XIV");

            int titleY = panelY + 18;
            String titleStr = DiaryChrome.fitCached(this.font, this.title.getString(), Math.max(0, panelW - 48));
            int titleW = DiaryChrome.widthCached(this.font, titleStr);
            gfx.drawString(this.font, titleStr, (this.width - titleW) / 2, titleY, DiaryChrome.INK_TITLE, false);
            DiaryChrome.drawInkDivider(gfx, panelX + 24, titleY + 14, panelW - 48);
            DiaryChrome.drawDivisorLozenge(gfx, this.width / 2, titleY + 14);

            if (this.ownMultilineMessage == null && this.ownMessage != null) {
                this.ownMultilineMessage = MultiLineLabel.create(this.font, this.ownMessage, Math.max(1, panelW - 40));
            }
            if (this.ownMultilineMessage != null) {
                int msgY = titleY + 28;
                this.ownMultilineMessage.renderCentered(gfx, this.width / 2, msgY,
                    this.font.lineHeight + 2, DiaryChrome.INK_SUBTITLE);
            }

            if (com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("destroys") >= 3) {
                String line = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                    ? "The cabinet remembers what you destroy."
                    : "El gabinete recuerda lo que destruyes.";
                line = DiaryChrome.fitCached(this.font, line, Math.max(0, panelW - 32));
                int lw = DiaryChrome.widthCached(this.font, line);
                gfx.drawString(this.font, line, (this.width - lw) / 2, panelY + panelH - 42,
                    com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(0xFF6E1F16, 150), false);
            }
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(gfx, this.width);
        } catch (Throwable ignored) {
        }

        try {
            for (var w : this.renderables) {
                if (w instanceof AbstractWidget aw && aw.visible) {
                    w.render(gfx, mouseX, mouseY, partial);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * Enter activates the focused decision (Yes only as the no-focus default).
     * Escape always cancels, including during the activation delay.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.decisionMade) return true;
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
            if (this.decisionDelay > 0) return true;
            // Enter follows the focused decision, not a translated label match.
            DiaryButton target = this.getFocused() == this.diaryNo ? this.diaryNo : this.diaryYes;
            if (target != null && target.active && target.visible) target.onPress();
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics gfx) {
        // Bypass total del dirt vanilla
    }

    @Override
    public void onClose() {
        if (this.decisionMade) return;
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.back();
        } catch (Throwable ignored) {
        }
        this.finishDecision(false);
        // A callback may navigate elsewhere. Only supply the old parent fallback
        // when it left this confirmation on screen.
        if (this.minecraft != null && this.minecraft.screen == this) this.minecraft.setScreen(this.parent);
    }
}
