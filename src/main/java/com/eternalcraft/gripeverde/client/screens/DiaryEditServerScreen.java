package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.ExpedientHeader;
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

    private static final int PANEL_MAX_W = 452;
    private static final int PANEL_MAX_H = 232;
    private static final Component NAME_LABEL = Component.translatable("addServer.enterName");
    private static final Component IP_LABEL = Component.translatable("addServer.enterIp");

    private final Screen parent;
    private final BooleanConsumer callback;
    private final ServerData serverData;

    private EditBox nameEdit;
    private EditBox ipEdit;
    private DiaryButton doneButton;
    private int panelX, panelY, panelW, panelH;
    private long openedAtMs = 0L;

    public DiaryEditServerScreen(Screen parent, BooleanConsumer callback, ServerData serverData) {
        super(Component.translatable("addServer.title"));
        this.parent = parent;
        this.callback = callback;
        this.serverData = serverData;
    }

    @Override
    protected void init() {
        this.panelW = Math.min(PANEL_MAX_W, this.width - 34);
        this.panelH = Math.min(PANEL_MAX_H, this.height - 28);
        this.panelX = (this.width - panelW) / 2;
        this.panelY = (this.height - panelH) / 2;

        int fieldW = this.panelW - 176;
        int fieldX = this.panelX + 24;
        int fieldH = 20;

        // Name field
        int nameY = this.panelY + 98;
        this.nameEdit = new DiaryTextField(this.font, fieldX, nameY, fieldW, fieldH, Component.translatable("addServer.enterName"));
        this.nameEdit.setMaxLength(64);
        this.nameEdit.setValue(this.serverData.name != null ? this.serverData.name : "");
        this.nameEdit.setResponder(s -> this.updateDoneButton());
        this.addRenderableWidget(this.nameEdit);
        this.setInitialFocus(this.nameEdit);

        // IP field
        int ipY = this.panelY + 144;
        this.ipEdit = new DiaryTextField(this.font, fieldX, ipY, fieldW, fieldH, Component.translatable("addServer.enterIp"));
        this.ipEdit.setMaxLength(128);
        this.ipEdit.setValue(this.serverData.ip != null ? this.serverData.ip : "");
        this.ipEdit.setResponder(s -> this.updateDoneButton());
        this.addRenderableWidget(this.ipEdit);

        // Botones inferiores
        int btnW = 100;
        int btnH = 20;
        int btnY = this.panelY + this.panelH - btnH - 16;
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
        this.openedAtMs = System.currentTimeMillis();
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
        try {
            com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(gfx, this.width, this.height);
        } catch (Throwable ignored) {}

        float reveal = DiaryChrome.reveal(this.openedAtMs, 220L);
        DiaryChrome.drawPanelScaffold(gfx, this.panelX, this.panelY, this.panelW, this.panelH, DiaryTheme.BRONZE);
        PaperTexture.drawClean(gfx, this.panelX, this.panelY, this.panelW, this.panelH, PaperTexture.PaperKind.OFFICIAL);
        DiaryChrome.drawCornerOrnaments(gfx, this.panelX, this.panelY, this.panelW, this.panelH);

        int headerBottom = ExpedientHeader.draw(gfx, this.font, this.panelX, this.panelY, this.panelW,
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "EXPEDIENT XI" : "EXPEDIENTE XI",
            this.title,
            "- " + Component.translatable("gripeverde.edit.subtitle").getString() + " -",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "EC-QV/1897 · FOREIGN ENTRY" : "EC-QV/1897 · ENTRADA EXTRANJERA",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "A server ledger written by hand and archived under one seal." : "Una ficha de servidor escrita a mano y archivada bajo un mismo sello.",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "FORM" : "FORMULARIO",
            DiaryTheme.BRONZE,
            reveal);

        int noteX = this.panelX + this.panelW - 142;
        DiaryChrome.drawInsetCard(gfx, this.font, noteX, headerBottom, 122, 94,
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "ENTRY" : "FICHA",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Registry card" : "Tarjeta de registro",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Name and address remain on the same sheet, as every foreign lodging ought to." : "Nombre y direccion permanecen en la misma hoja, como corresponde a todo hospedaje extranjero.",
            DiaryTheme.BRONZE);

        gfx.drawString(this.font, NAME_LABEL.getString(), this.panelX + 24, headerBottom + 10, DiaryChrome.INK_SUBTITLE, false);
        gfx.fill(this.panelX + 24, headerBottom + 24, noteX - 12, headerBottom + 25, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 42));
        gfx.drawString(this.font, IP_LABEL.getString(), this.panelX + 24, headerBottom + 56, DiaryChrome.INK_SUBTITLE, false);
        gfx.fill(this.panelX + 24, headerBottom + 70, noteX - 12, headerBottom + 71, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 24));

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
