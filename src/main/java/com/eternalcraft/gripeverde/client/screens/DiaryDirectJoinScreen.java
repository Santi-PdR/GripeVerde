package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
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

    private int panelWidth() {
        return Math.min(PANEL_MAX_W, Math.max(136, this.width - 24));
    }

    private int panelHeight() {
        return Math.min(PANEL_MAX_H, Math.max(120, this.height - 24));
    }

    private int ipFieldOffset() {
        return Math.min(76, Math.max(54, panelHeight() - 76));
    }

    private static String ellipsizeTo(net.minecraft.client.gui.Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        int room = Math.max(0, maxWidth - font.width(ellipsis));
        return font.plainSubstrByWidth(text, room) + ellipsis;
    }

    public DiaryDirectJoinScreen(Screen parent, BooleanConsumer callback, ServerData serverData) {
        super(Component.translatable("selectServer.direct"));
        this.parent = parent;
        this.callback = callback;
        this.serverData = serverData;
    }

    @Override
    protected void init() {
        int panelW = panelWidth();
        int panelH = panelHeight();
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int fieldW = Math.max(64, panelW - 48);
        int fieldX = panelX + (panelW - fieldW) / 2;
        int fieldH = 20;
        int ipY = panelY + ipFieldOffset();

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
        int btnH = 20;
        int gap = Math.min(12, Math.max(4, panelW - 24 - 112));
        int btnW = Math.max(48, Math.min(100, (panelW - 24 - gap) / 2));
        int btnY = panelY + panelH - btnH - 16;
        int totalW = btnW * 2 + gap;
        int startX = panelX + (panelW - totalW) / 2;

        this.selectButton = new DiaryButton(startX, btnY, btnW, btnH,
                Component.translatable("selectServer.select"),
                DiaryButton.Icon.HAND, DiaryButton.Variant.PRIMARY,
                this::onSelect);
        this.selectButton.setTooltip(Tooltip.create(Component.translatable("selectServer.select")));
        this.addRenderableWidget(this.selectButton);

        DiaryButton cancel = new DiaryButton(startX + btnW + gap, btnY, btnW, btnH,
                Component.translatable("gui.cancel"),
                DiaryButton.Icon.DOOR, DiaryButton.Variant.SECONDARY,
                this::onCancel);
        cancel.setTooltip(Tooltip.create(Component.translatable("gui.cancel")));
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
        int panelW = panelWidth();
        int panelH = panelHeight();
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        DiaryChrome.drawPanelShadow(gfx, panelX, panelY, panelW, panelH);
        PaperTexture.drawTornSheet(gfx, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.OFFICIAL);

        // Cosmetica
        DiaryChrome.drawCornerOrnaments(gfx, panelX, panelY, panelW, panelH);
        DiaryChrome.drawRegistryTag(gfx, panelX, panelY, panelW, panelH, "REG. XII");

        // Titulo
        int titleY = panelY + (panelH < 160 ? 12 : 18);
        int centerX = panelX + panelW / 2;
        String titleStr = ellipsizeTo(this.font, this.title.getString(), Math.max(48, panelW - 28));
        int titleW = DiaryChrome.widthCached(this.font, titleStr);
        gfx.drawString(this.font, titleStr, centerX - titleW / 2, titleY, DiaryChrome.INK_TITLE, false);

        // Subtitulo
        String subtitle = "- " + DiaryChrome.tr("gripeverde.direct.subtitle") + " -";
        subtitle = ellipsizeTo(this.font, subtitle, Math.max(48, panelW - 28));
        int subW = DiaryChrome.widthCached(this.font, subtitle);
        gfx.drawString(this.font, subtitle, centerX - subW / 2, titleY + 12, DiaryChrome.INK_SUBTITLE, false);

        // Divisor con rombo
        DiaryChrome.drawInkDivider(gfx, panelX + 20, titleY + 26, panelW - 40);
        DiaryChrome.drawDivisorLozenge(gfx, centerX, titleY + 26);

        // Label IP
        String ipLbl = ellipsizeTo(this.font, ENTER_IP_LABEL.getString(), Math.max(48, panelW - 48));
        gfx.drawString(this.font, ipLbl, panelX + 24, panelY + ipFieldOffset() - 14, DiaryChrome.INK_SUBTITLE, false);

        // Widgets
        super.render(gfx, mouseX, mouseY, partial);

        // v0.16.0: aviso de la direccion sin mapa
        try {
            if (System.currentTimeMillis() < this.unmappedWarnUntilMs) {
                String warn = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                    ? "That address is on no map. The door is not there anymore."
                    : "Esa direccion no esta en ningun mapa. La puerta ya no esta ahi.";
                warn = ellipsizeTo(this.font, warn, Math.max(48, panelW - 24));
                int ww = DiaryChrome.widthCached(this.font, warn);
                long remain = this.unmappedWarnUntilMs - System.currentTimeMillis();
                int alpha = remain < 900L ? (int)(210 * remain / 900L) : 210;
                int warnY = panelY + Math.min(ipFieldOffset() + 24, panelH - 52);
                gfx.drawString(this.font, warn, centerX - ww / 2, warnY,
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
