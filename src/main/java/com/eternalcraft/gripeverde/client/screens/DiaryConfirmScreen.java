package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
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

    private static final int PANEL_MAX_W = 340;
    private static final int PANEL_MAX_H = 180;

    private final Screen parent;
    // Guardamos el mensaje propio porque ConfirmScreen.message es private
    private final Component ownMessage;
    private MultiLineLabel ownMultilineMessage;

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

        // Botones Diary propios
        int btnW = 100;
        int btnH = 20;
        int panelY = (this.height - PANEL_MAX_H) / 2;
        int btnY = panelY + PANEL_MAX_H - btnH - 16;
        int gap = 12;
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
    }

    @Override
    public void tick() {
        super.tick();
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

        int panelW = Math.min(PANEL_MAX_W, this.width - 40);
        int panelH = Math.min(PANEL_MAX_H, this.height - 30);
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
            DiaryChrome.drawRegistryTag(gfx, panelX, panelY, panelW, panelH, "REG. XIII");

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
     * v0.266.0: la hoja de confirmacion se maneja con el teclado: Enter firma
     * el si y Esc se va sin tocar nada. Antes habia que ir al raton.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
            String yes = this.yesButton == null ? "" : this.yesButton.getString();
            for (var child : this.children()) {
                if (child instanceof DiaryButton button && button.active && button.visible
                        && button.getMessage().getString().equals(yes)) {
                    button.onPress();
                    return true;
                }
            }
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
        // v0.264.0: salir de una hoja suena a puerta, como el resto de la casa.
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.back();
        } catch (Throwable ignored) {
        }
        this.minecraft.setScreen(parent);
    }
}
