package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.ExpedientHeader;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import com.eternalcraft.gripeverde.client.widgets.DiaryTextField;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

/**
 * Conexion directa a un servidor por IP.
 * Reimplementacion completa desde Screen (no subclasea DirectJoinServerScreen vanilla).
 */
public class DiaryDirectJoinScreen extends Screen {

    private static final int PANEL_MAX_W = 430;
    private static final int PANEL_MAX_H = 206;
    private static final Component ENTER_IP_LABEL = Component.translatable("addServer.enterIp");

    private final Screen parent;
    private final BooleanConsumer callback;
    private final ServerData serverData;

    private DiaryTextField ipEdit;
    private DiaryButton selectButton;
    private int panelX, panelY, panelW, panelH;
    private long openedAtMs = 0L;

    public DiaryDirectJoinScreen(Screen parent, BooleanConsumer callback, ServerData serverData) {
        super(Component.translatable("selectServer.direct"));
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

        int fieldW = this.panelW - 170;
        int fieldX = this.panelX + 24;
        int fieldH = 20;
        int ipY = this.panelY + 102;

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
        int btnY = this.panelY + this.panelH - btnH - 16;
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
        this.openedAtMs = System.currentTimeMillis();
    }

    private void updateSelectButton() {
        if (this.selectButton != null && this.ipEdit != null) {
            this.selectButton.active = !this.ipEdit.getValue().isEmpty();
        }
    }

    private long unmappedWarnUntilMs = 0L;

    private void onSelect() {
        String ip = this.ipEdit.getValue() == null ? "" : this.ipEdit.getValue().trim();
        // v0.16.0: la direccion que no esta en ningun mapa.
        if ("13.13.13.13".equals(ip)) {
            this.unmappedWarnUntilMs = System.currentTimeMillis() + 6000L;
            try {
                boolean first = com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                    com.eternalcraft.gripeverde.client.lore.Discoveries.ADDRESS_13);
                com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                    com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_BELL.get(), 0.20f, 0.50f);
                if (first) {
                    com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                        com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_AMBIENT_OCCASIONAL.get(), 0.20f, 0.58f);
                }
            } catch (Throwable ignored) {}
            return;
        }
        this.serverData.ip = ip;
        this.minecraft.options.lastMpIp = ip;
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
        try { MenuMusic.tick();
 com.eternalcraft.gripeverde.client.audio.MenuAmbience.tick(); } catch (Throwable ignored) {}
    }

    @Override
    public void resize(net.minecraft.client.Minecraft mc, int w, int h) {
        String ipVal = this.ipEdit != null ? this.ipEdit.getValue() : "";
        super.resize(mc, w, h);
        this.ipEdit.setValue(ipVal);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        try {
            com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(gfx, this.width, this.height);
        } catch (Throwable ignored) {}

        float reveal = DiaryChrome.reveal(this.openedAtMs, 220L);
        DiaryChrome.renderPanelBase(gfx, this.panelX, this.panelY, this.panelW, this.panelH, DiaryTheme.BRONZE, PaperTexture.PaperKind.OFFICIAL);

        int headerBottom = ExpedientHeader.draw(gfx, this.font, this.panelX, this.panelY, this.panelW, this.title, reveal);

        int noteX = this.panelX + this.panelW - 136;


        String ipLbl = ENTER_IP_LABEL.getString();
        gfx.drawString(this.font, ipLbl, this.panelX + 24, headerBottom + 10, DiaryChrome.INK_SUBTITLE, false);
        gfx.fill(this.panelX + 24, headerBottom + 24, noteX - 12, headerBottom + 25, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 42));
        gfx.fill(this.panelX + 24, headerBottom + 58, noteX - 12, headerBottom + 59, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 24));
        gfx.drawString(this.font,
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "No map is consulted until the line is accepted." : "Ningun mapa se consulta hasta aceptar la linea.",
            this.panelX + 24, headerBottom + 44, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 150), false);

        for (var w : this.renderables) {
            if (w instanceof AbstractWidget aw && aw.visible) {
                w.render(gfx, mouseX, mouseY, partial);
            }
        }

        try {
            if (System.currentTimeMillis() < this.unmappedWarnUntilMs) {
                String warn = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                    ? "That address is on no map. The door is not there anymore."
                    : "Esa direccion no esta en ningun mapa. La puerta ya no esta ahi.";
                int ww = this.font.width(warn);
                long remain = this.unmappedWarnUntilMs - System.currentTimeMillis();
                int alpha = remain < 900L ? (int)(210 * remain / 900L) : 210;
                gfx.drawString(this.font, warn, (this.width - ww) / 2, this.panelY + this.panelH + 10,
                    (alpha << 24) | 0xB8A888, false);
            }
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(gfx, this.width);
        } catch (Throwable ignored) {}
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
