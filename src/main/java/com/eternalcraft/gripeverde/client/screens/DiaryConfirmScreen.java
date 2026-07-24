package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.VictorianSkyline;
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

    private static final int PANEL_W = 340;
    private static final int PANEL_H = 180;

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
        int panelY = (this.height - PANEL_H) / 2;
        int btnY = panelY + PANEL_H - btnH - 16;
        int gap = 12;
        int totalW = btnW * 2 + gap;
        int startX = (this.width - totalW) / 2;

        DiaryButton diaryYes = new DiaryButton(startX, btnY, btnW, btnH,
                this.yesButton, DiaryButton.Icon.NONE, DiaryButton.Variant.PRIMARY,
                () -> this.callback.accept(true));
        DiaryButton diaryNo = new DiaryButton(startX + btnW + gap, btnY, btnW, btnH,
                this.noButton, DiaryButton.Icon.DOOR, DiaryButton.Variant.SECONDARY,
                () -> this.callback.accept(false));

        this.addRenderableWidget(diaryYes);
        this.addRenderableWidget(diaryNo);
    }

    @Override
    public void tick() {
        super.tick();
        // R-DIARY-TICK-MUSIC: mantener musica del cronista viva en subpantallas
        try { MenuMusic.tick(); } catch (Throwable ignored) {}
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        // Fondo: escena viva del Title
        try {
            AtmosphereRenderer.renderSky(gfx, this.width, this.height);
            VictorianSkyline.renderBackground(gfx, this.width, this.height);
            LivingAtmosphere.renderFogLow(gfx, this.width, this.height);
            VictorianSkyline.renderForeground(gfx, this.width, this.height);
            LivingAtmosphere.renderStreetLevel(gfx, this.width, this.height);
            LivingAtmosphere.renderAir(gfx, this.width, this.height);
        } catch (Throwable ignored) {
            // Si algo del pipeline falla, seguimos con overlay negro
        }

        // Overlay para atenuar la escena
        gfx.fill(0, 0, this.width, this.height, DiaryChrome.OVERLAY_COL);

        // Panel pergamino
        int panelX = (this.width - PANEL_W) / 2;
        int panelY = (this.height - PANEL_H) / 2;
        PaperTexture.drawClean(gfx, panelX, panelY, PANEL_W, PANEL_H);

        // Cosmetica
        DiaryChrome.drawCornerOrnaments(gfx, panelX, panelY, PANEL_W, PANEL_H);

        // Titulo
        int titleY = panelY + 18;
        String titleStr = this.title.getString();
        int titleW = this.font.width(titleStr);
        gfx.drawString(this.font, titleStr, (this.width - titleW) / 2 + 1, titleY + 1, DiaryChrome.SHADOW_COL, false);
        gfx.drawString(this.font, titleStr, (this.width - titleW) / 2, titleY, DiaryChrome.INK_TITLE, false);

        // Divisor con rombo
        DiaryChrome.drawInkDivider(gfx, panelX + 24, titleY + 14, PANEL_W - 48);
        DiaryChrome.drawDivisorLozenge(gfx, this.width / 2, titleY + 14);

        // Mensaje multilinea (propio, no toco el vanilla)
        if (this.ownMultilineMessage == null && this.ownMessage != null) {
            this.ownMultilineMessage = MultiLineLabel.create(this.font, this.ownMessage, PANEL_W - 40);
        }
        if (this.ownMultilineMessage != null) {
            int msgY = titleY + 28;
            this.ownMultilineMessage.renderCentered(gfx, this.width / 2, msgY, this.font.lineHeight + 2, DiaryChrome.INK_SUBTITLE);
        }

        // Widgets propios (botones Diary y cualquier otro visible)
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
