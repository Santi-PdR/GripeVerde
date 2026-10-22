package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
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

    private static final int PANEL_MAX_W = 340;
    private static final int PANEL_MAX_H = 180;
    private static final Component ENTER_IP_LABEL = Component.translatable("addServer.enterIp");

    private final Screen parent;
    private final BooleanConsumer callback;
    private final ServerData serverData;

    private DiaryTextField ipEdit;
    private DiaryButton selectButton;
    private boolean justOpened = true;
    private int panelX, panelY, panelW, panelH;
    private int fieldX, fieldY, fieldW, fieldH, labelY;
    private int buttonY, cancelButtonY, buttonW, buttonH, buttonGap;
    private boolean compact;
    private boolean stackedButtons;

    public DiaryDirectJoinScreen(Screen parent, BooleanConsumer callback, ServerData serverData) {
        super(Component.translatable("selectServer.direct"));
        this.parent = parent;
        this.callback = callback;
        this.serverData = serverData;
    }

    @Override
    protected void init() {
        panelW = Math.max(1, Math.min(PANEL_MAX_W, this.width - 16));
        panelH = Math.max(1, Math.min(PANEL_MAX_H, this.height - 16));
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;
        compact = panelH < 130 || panelW < 220;

        fieldW = Math.max(1, panelW - 40);
        fieldX = panelX + (panelW - fieldW) / 2;
        stackedButtons = panelW < 220;
        fieldH = compact ? (stackedButtons ? 14 : 16) : 20;
        labelY = panelY + (compact ? 0 : 62);
        fieldY = panelY + (compact ? (stackedButtons ? 20 : 31) : 76);

        // IP field
        this.ipEdit = new DiaryTextField(this.font, fieldX, fieldY, fieldW, fieldH, ENTER_IP_LABEL);
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
        buttonH = compact ? (stackedButtons ? 12 : 15) : 20;
        buttonGap = compact ? 6 : 12;
        buttonW = stackedButtons ? Math.max(1, panelW - 32)
            : Math.max(1, (panelW - 32 - buttonGap) / 2);
        int buttonBlockH = buttonH * (stackedButtons ? 2 : 1) + (stackedButtons ? 4 : 0);
        if (compact) {
            int firstFreeY = fieldY + fieldH + 6;
            int lastFreeY = panelY + panelH - buttonBlockH - 6;
            buttonY = firstFreeY + Math.max(0, (lastFreeY - firstFreeY) / 2);
        } else {
            buttonY = panelY + panelH - buttonBlockH - 14;
        }
        cancelButtonY = buttonY + (stackedButtons ? buttonH + 4 : 0);
        int totalW = buttonW * 2 + buttonGap;
        int startX = stackedButtons ? panelX + (panelW - buttonW) / 2
            : panelX + (panelW - totalW) / 2;

        this.selectButton = new DiaryButton(startX, buttonY, buttonW, buttonH,
                Component.translatable("selectServer.select"),
                stackedButtons ? DiaryButton.Icon.HAND
                    : (buttonW < 82 ? DiaryButton.Icon.NONE : DiaryButton.Icon.HAND), DiaryButton.Variant.PRIMARY,
                this::onSelect);
        this.addRenderableWidget(this.selectButton);

        DiaryButton cancel = new DiaryButton(
                stackedButtons ? startX : startX + buttonW + buttonGap,
                cancelButtonY, buttonW, buttonH,
                Component.translatable("gui.cancel"),
                stackedButtons ? DiaryButton.Icon.DOOR
                    : (buttonW < 82 ? DiaryButton.Icon.NONE : DiaryButton.Icon.DOOR), DiaryButton.Variant.SECONDARY,
                this::onCancel);
        this.addRenderableWidget(cancel);

        this.updateSelectButton();
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
        justOpened = false;
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (justOpened) return true; // v0.21.26: ignorar tecla filtrada de la pantalla anterior
        return super.charTyped(codePoint, modifiers);
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
            com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(gfx, this.width, this.height);
                } catch (Throwable ignored) {}

        // Overlay atenuador

        // Panel pergamino
        DiaryChrome.drawPanelShadow(gfx, panelX, panelY, panelW, panelH);
        PaperTexture.drawClean(gfx, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.OFFICIAL);

        // Cosmetica
        DiaryChrome.drawCornerOrnaments(gfx, panelX, panelY, panelW, panelH);
        DiaryChrome.drawRegistryTag(gfx, panelX, panelY, panelW, panelH, "REG. XII");

        // Titulo
        int titleY = panelY + (compact ? 3 : 18);
        com.eternalcraft.gripeverde.client.render.DiaryTypography.drawCentered(
                gfx, this.font, this.title, this.width / 2, titleY, DiaryChrome.INK_TITLE);

        // Subtitulo
        String subtitle = "- " + Component.translatable("gripeverde.direct.subtitle").getString() + " -";
        int subW = this.font.width(subtitle);
        if (!compact && panelW >= 220) {
            gfx.drawString(this.font, subtitle, (this.width - subW) / 2, titleY + 12, DiaryChrome.INK_SUBTITLE, false);
        }

        // Divisor con rombo
        int dividerY = panelY + (compact ? 17 : 44);
        int dividerInset = panelW < 220 ? 8 : 12;
        DiaryChrome.drawInkDivider(gfx, panelX + dividerInset, dividerY, Math.max(1, panelW - dividerInset * 2));
        DiaryChrome.drawDivisorLozenge(gfx, this.width / 2, dividerY);

        // Label IP
        String ipLbl = ENTER_IP_LABEL.getString();
        if (!compact) {
            gfx.drawString(this.font, ipLbl, panelX + 20, labelY, DiaryChrome.INK_SUBTITLE, false);
        }

        // Widgets
        for (var w : this.renderables) {
            if (w instanceof AbstractWidget aw && aw.visible) {
                w.render(gfx, mouseX, mouseY, partial);
            }
        }

        // v0.16.0: aviso de la direccion sin mapa
        try {
            if (System.currentTimeMillis() < this.unmappedWarnUntilMs) {
                String warn = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                    ? "That address is on no map. The door is not there anymore."
                    : "Esa direccion no esta en ningun mapa. La puerta ya no esta ahi.";
                int ww = this.font.width(warn);
                long remain = this.unmappedWarnUntilMs - System.currentTimeMillis();
                int alpha = remain < 900L ? (int)(210 * remain / 900L) : 210;
                gfx.drawString(this.font, warn, (this.width - ww) / 2, panelY + panelH + 10,
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
