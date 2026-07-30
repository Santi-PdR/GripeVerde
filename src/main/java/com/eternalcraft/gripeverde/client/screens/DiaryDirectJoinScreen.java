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
import net.minecraft.client.gui.components.EditBox;
import com.eternalcraft.gripeverde.client.widgets.DiaryTextField;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

/**
 * Conexion directa a un servidor por IP.
 * Reimplementacion completa desde Screen (no subclasea DirectJoinServerScreen vanilla).
 */
public class DiaryDirectJoinScreen extends Screen {

    private static final int PANEL_MAX_W = 340;
    private static final int PANEL_MAX_H = 180;
    private static final Component ENTER_IP_LABEL = Component.translatable("addServer.enterIp");

    private final Screen parent;
    private final BooleanConsumer callback;
    private final ServerData serverData;

    private DiaryTextField ipEdit;
    private DiaryButton selectButton;

    public DiaryDirectJoinScreen(Screen parent, BooleanConsumer callback, ServerData serverData) {
        super(Component.translatable("selectServer.direct"));
        this.parent = parent;
        this.callback = callback;
        this.serverData = serverData;
    }

    @Override
    protected void init() {
        int panelW = Math.min(PANEL_MAX_W, this.width - 40);
        int panelH = Math.min(PANEL_MAX_H, this.height - 30);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int fieldW = PANEL_MAX_W - 60;
        int fieldX = panelX + 30;
        int fieldH = 20;
        int ipY = panelY + 76;

        // IP field
        this.ipEdit = new DiaryTextField(this.font, fieldX, ipY, fieldW, fieldH, ENTER_IP_LABEL);
        this.ipEdit.setMaxLength(128);
        String initial = (this.serverData != null && this.serverData.ip != null) ? this.serverData.ip : "";
        // Vanilla usa "lastServer" del options, aca vamos con lo que traiga ServerData
        if (initial.isEmpty()) {
            initial = this.minecraft.options.lastMpIp;
            if (initial == null) initial = "";
        }
        this.ipEdit.setValue(initial);
        this.ipEdit.setResponder(s -> this.updateSelectButton());
        this.addRenderableWidget(this.ipEdit);
        this.setInitialFocus(this.ipEdit);

        // Botones inferiores
        int btnW = 100;
        int btnH = 20;
        int btnY = panelY + PANEL_MAX_H - btnH - 16;
        int gap = 12;
        int totalW = btnW * 2 + gap;
        int startX = (this.width - totalW) / 2;

        this.selectButton = new DiaryButton(startX, btnY, btnW, btnH,
                Component.translatable("selectServer.select"),
                DiaryButton.Icon.HAND, DiaryButton.Variant.PRIMARY,
                this::onSelect);
        this.addRenderableWidget(this.selectButton);

        DiaryButton cancel = new DiaryButton(startX + btnW + gap, btnY, btnW, btnH,
                Component.translatable("gui.cancel"),
                DiaryButton.Icon.DOOR, DiaryButton.Variant.SECONDARY,
                this::onCancel);
        this.addRenderableWidget(cancel);

        this.updateSelectButton();
    }

    private void updateSelectButton() {
        if (this.selectButton != null && this.ipEdit != null) {
            this.selectButton.active = !this.ipEdit.getValue().isEmpty();
        }
    }

    private void onSelect() {
        this.serverData.ip = this.ipEdit.getValue();
        this.minecraft.options.lastMpIp = this.ipEdit.getValue();
        this.minecraft.options.save();
        this.callback.accept(true);
    }

    private void onCancel() {
        this.callback.accept(false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (keyCode == 257 || keyCode == 335) { // ENTER, KP_ENTER
            if (this.selectButton.active) {
                this.onSelect();
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick() {
        if (this.ipEdit != null) this.ipEdit.tick();
        try { MenuMusic.tick(); } catch (Throwable ignored) {}
    }

    @Override
    public void resize(net.minecraft.client.Minecraft mc, int w, int h) {
        String ipVal = this.ipEdit != null ? this.ipEdit.getValue() : "";
        super.resize(mc, w, h);
        this.ipEdit.setValue(ipVal);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        // Fondo: escena viva del Title
        try {
            AtmosphereRenderer.renderSky(gfx, this.width, this.height);
            VictorianSkyline.renderBackground(gfx, this.width, this.height);
                VictorianSkyline.renderForeground(gfx, this.width, this.height);
        LivingAtmosphere.renderStreet(gfx, this.width, this.height);
                } catch (Throwable ignored) {}

        // Overlay atenuador
        gfx.fill(0, 0, this.width, this.height, DiaryChrome.OVERLAY_COL);

        // Panel pergamino
        int panelW = Math.min(PANEL_MAX_W, this.width - 40);
        int panelH = Math.min(PANEL_MAX_H, this.height - 30);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        DiaryChrome.drawPanelShadow(gfx, panelX, panelY, panelW, panelH);
        PaperTexture.drawClean(gfx, panelX, panelY, panelW, panelH);

        // Cosmetica
        DiaryChrome.drawCornerOrnaments(gfx, panelX, panelY, panelW, panelH);

        // Titulo
        int titleY = panelY + 18;
        String titleStr = this.title.getString();
        int titleW = this.font.width(titleStr);
        gfx.drawString(this.font, titleStr, (this.width - titleW) / 2 + 1, titleY + 1, DiaryChrome.SHADOW_COL, false);
        gfx.drawString(this.font, titleStr, (this.width - titleW) / 2, titleY, DiaryChrome.INK_TITLE, false);

        // Subtitulo
        String subtitle = "- " + Component.translatable("gripeverde.direct.subtitle").getString() + " -";
        int subW = this.font.width(subtitle);
        gfx.drawString(this.font, subtitle, (this.width - subW) / 2, titleY + 12, DiaryChrome.INK_SUBTITLE, false);

        // Divisor con rombo
        DiaryChrome.drawInkDivider(gfx, panelX + 24, titleY + 26, panelW - 48);
        DiaryChrome.drawDivisorLozenge(gfx, this.width / 2, titleY + 26);

        // Label IP
        String ipLbl = ENTER_IP_LABEL.getString();
        gfx.drawString(this.font, ipLbl, panelX + 30, panelY + 62, DiaryChrome.INK_SUBTITLE, false);

        // Widgets
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
        this.callback.accept(false);
    }
}
