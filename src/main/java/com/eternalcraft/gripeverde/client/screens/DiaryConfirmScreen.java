package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.ExpedientHeader;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Confirmacion generica del cronista.
 * Reemplaza el fondo dirt vanilla + botones grises por pergamino + DiaryButtons.
 * Reutilizable para: Delete server, Delete pack, y cualquier futura confirmacion.
 */
public class DiaryConfirmScreen extends ConfirmScreen {

    private static final int PANEL_MAX_W = 420;
    private static final int PANEL_MAX_H = 196;

    private final Screen parent;
    // Guardamos el mensaje propio porque ConfirmScreen.message es private
    private final Component ownMessage;
    private MultiLineLabel ownMultilineMessage;
    private int panelX, panelY, panelW, panelH;
    private long openedAtMs = 0L;

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

    @Override
    protected void init() {
        super.init();
        UiSounds.notification();

        // Ocultar botones vanilla via reflection sobre exitButtons
        try {
            Field f = null;
            // Cascada: nombre oficial -> SRG -> scan por tipo (List)
            try { f = ConfirmScreen.class.getDeclaredField("exitButtons"); } catch (NoSuchFieldException ignored) {}
            if (f == null) try { f = ConfirmScreen.class.getDeclaredField("f_169251_"); } catch (NoSuchFieldException ignored) {}
            if (f == null) {
                for (Field cand : ConfirmScreen.class.getDeclaredFields()) {
                    if (cand.getType().equals(List.class)) { f = cand; break; }
                }
            }
            if (f != null) {
                f.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<Button> exit = (List<Button>) f.get(this);
                for (Button b : exit) {
                    b.visible = false;
                    b.active = false;
                    b.setX(-10000);
                }
            }
        } catch (Throwable t) {
            // Silencioso. Fallback: botones vanilla quedan visibles.
        }

        this.panelW = Math.min(PANEL_MAX_W, this.width - 34);
        this.panelH = Math.min(PANEL_MAX_H, this.height - 28);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        // Botones Diary propios
        int btnW = 112;
        int btnH = 20;
        int btnY = this.panelY + this.panelH - btnH - 16;
        int gap = 14;
        int totalW = btnW * 2 + gap;
        int startX = (this.width - totalW) / 2;

        DiaryButton diaryYes = new DiaryButton(startX, btnY, btnW, btnH,
                this.yesButton, DiaryButton.Icon.NONE, DiaryButton.Variant.PRIMARY,
                () -> {
                    // v0.16.0: el gabinete cuenta lo que el lector destruye.
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
                    this.callback.accept(true);
                });
        DiaryButton diaryNo = new DiaryButton(startX + btnW + gap, btnY, btnW, btnH,
                this.noButton, DiaryButton.Icon.DOOR, DiaryButton.Variant.SECONDARY,
                () -> this.callback.accept(false));

        this.addRenderableWidget(diaryYes);
        this.addRenderableWidget(diaryNo);
        this.openedAtMs = System.currentTimeMillis();
    }

    @Override
    public void tick() {
        super.tick();
        // R-DIARY-TICK-MUSIC: mantener musica del cronista viva en subpantallas
        try { MenuMusic.tick();
 com.eternalcraft.gripeverde.client.audio.MenuAmbience.tick(); } catch (Throwable ignored) {}
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        try {
            com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(gfx, this.width, this.height);
        } catch (Throwable ignored) {}

        float reveal = DiaryChrome.reveal(this.openedAtMs, 220L);
        DiaryChrome.renderPanelBase(gfx, this.panelX, this.panelY, this.panelW, this.panelH, DiaryTheme.REDCOAT_DARK, PaperTexture.PaperKind.OFFICIAL);

        int headerBottom = ExpedientHeader.draw(gfx, this.font, this.panelX, this.panelY, this.panelW, this.title, reveal);

        if (this.ownMultilineMessage == null && this.ownMessage != null) {
            this.ownMultilineMessage = MultiLineLabel.create(this.font, this.ownMessage, this.panelW - 74);
        }
        int msgY = headerBottom + 8;
        if (this.ownMultilineMessage != null) {
            this.ownMultilineMessage.renderCentered(gfx, this.width / 2, msgY, this.font.lineHeight + 2, DiaryTheme.INK);
        }
        try {
            if (com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("destroys") >= 3) {
                String line = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                    ? "The cabinet remembers what you destroy."
                    : "El gabinete recuerda lo que destruyes.";
                int lw = this.font.width(line);
                gfx.drawString(this.font, line, (this.width - lw) / 2, this.panelY + this.panelH - 44,
                    DiaryTheme.withAlpha(0xFF6E1F16, 150), false);
            }
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(gfx, this.width);
        } catch (Throwable ignored) {}

        for (var w : this.renderables) {
            if (w instanceof AbstractWidget aw && aw.visible) {
                w.render(gfx, mouseX, mouseY, partial);
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics gfx) {
        // Bypass total del dirt vanilla
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
