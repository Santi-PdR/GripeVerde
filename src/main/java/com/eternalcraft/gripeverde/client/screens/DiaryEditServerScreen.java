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
 * Editor de servidor del cronista.
 * Reimplementacion completa desde Screen (no subclasea EditServerScreen vanilla)
 * para lograr layout limpio sobre pergamino.
 */
public class DiaryEditServerScreen extends Screen {

    private static final int PANEL_MAX_W = 340;
    private static final int PANEL_MAX_H = 220;
    private static final Component NAME_LABEL = Component.translatable("addServer.enterName");
    private static final Component IP_LABEL = Component.translatable("addServer.enterIp");

    private final Screen parent;
    private final BooleanConsumer callback;
    private final ServerData serverData;

    private EditBox nameEdit;
    private EditBox ipEdit;
    private DiaryButton doneButton;

    public DiaryEditServerScreen(Screen parent, BooleanConsumer callback, ServerData serverData) {
        super(Component.translatable("addServer.title"));
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

        // Name field
        int nameY = panelY + 62;
        this.nameEdit = new DiaryTextField(this.font, fieldX, nameY, fieldW, fieldH, Component.translatable("addServer.enterName"));
        this.nameEdit.setMaxLength(64);
        this.nameEdit.setValue(this.serverData.name != null ? this.serverData.name : "");
        this.nameEdit.setResponder(s -> this.updateDoneButton());
        this.addRenderableWidget(this.nameEdit);
        this.setInitialFocus(this.nameEdit);

        // IP field
        int ipY = panelY + 108;
        this.ipEdit = new DiaryTextField(this.font, fieldX, ipY, fieldW, fieldH, Component.translatable("addServer.enterIp"));
        this.ipEdit.setMaxLength(128);
        this.ipEdit.setValue(this.serverData.ip != null ? this.serverData.ip : "");
        this.ipEdit.setResponder(s -> this.updateDoneButton());
        this.addRenderableWidget(this.ipEdit);

        // Botones inferiores
        int btnW = 100;
        int btnH = 20;
        int btnY = panelY + PANEL_MAX_H - btnH - 16;
        int gap = 12;
        int totalW = btnW * 2 + gap;
        int startX = (this.width - totalW) / 2;

        this.doneButton = new DiaryButton(startX, btnY, btnW, btnH,
                Component.translatable("addServer.add"),
                DiaryButton.Icon.SCROLL, DiaryButton.Variant.PRIMARY,
                this::onDone);
        this.addRenderableWidget(this.doneButton);

        DiaryButton cancel = new DiaryButton(startX + btnW + gap, btnY, btnW, btnH,
                Component.translatable("gui.cancel"),
                DiaryButton.Icon.DOOR, DiaryButton.Variant.SECONDARY,
                this::onCancel);
        this.addRenderableWidget(cancel);

        this.updateDoneButton();
    }

    private void updateDoneButton() {
        if (this.doneButton != null && this.ipEdit != null && this.nameEdit != null) {
            this.doneButton.active = !this.ipEdit.getValue().isEmpty() && !this.nameEdit.getValue().isEmpty();
        }
    }

    private void onDone() {
        this.serverData.name = this.nameEdit.getValue();
        this.serverData.ip = this.ipEdit.getValue();
        this.callback.accept(true);
    }

    private void onCancel() {
        this.callback.accept(false);
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
        this.ipEdit.tick();
        try { MenuMusic.tick();
 com.eternalcraft.gripeverde.client.audio.MenuAmbience.tick(); } catch (Throwable ignored) {}
    }

    @Override
    public void resize(net.minecraft.client.Minecraft mc, int w, int h) {
        String nameVal = this.nameEdit != null ? this.nameEdit.getValue() : "";
        String ipVal = this.ipEdit != null ? this.ipEdit.getValue() : "";
        super.resize(mc, w, h);
        this.nameEdit.setValue(nameVal);
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
        gfx.drawString(this.font, titleStr, (this.width - titleW) / 2, titleY, DiaryChrome.INK_TITLE, false);

        // Subtitulo
        String subtitle = "- " + Component.translatable("gripeverde.edit.subtitle").getString() + " -";
        int subW = this.font.width(subtitle);
        gfx.drawString(this.font, subtitle, (this.width - subW) / 2, titleY + 12, DiaryChrome.INK_SUBTITLE, false);

        // Divisor con rombo
        DiaryChrome.drawInkDivider(gfx, panelX + 24, titleY + 26, panelW - 48);
        DiaryChrome.drawDivisorLozenge(gfx, this.width / 2, titleY + 26);

        // Labels de los EditBox
        String nameLbl = NAME_LABEL.getString();
        gfx.drawString(this.font, nameLbl, panelX + 30, panelY + 48, DiaryChrome.INK_SUBTITLE, false);

        String ipLbl = IP_LABEL.getString();
        gfx.drawString(this.font, ipLbl, panelX + 30, panelY + 94, DiaryChrome.INK_SUBTITLE, false);

        // Widgets (EditBox + botones Diary)
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
