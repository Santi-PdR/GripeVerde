package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
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
    private DiaryButton cancelButton;
    private boolean justOpened = true;
    private int panelX, panelY, panelW, panelH;
    private int fieldX, fieldW, fieldH, nameY, ipY;
    private int buttonY, cancelButtonY, buttonW, buttonH, buttonGap;
    private boolean compact;
    private boolean stackedButtons;

    public DiaryEditServerScreen(Screen parent, BooleanConsumer callback, ServerData serverData) {
        super(Component.translatable("addServer.title"));
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
        compact = panelH < 164 || panelW < 220;
        stackedButtons = panelW < 220;

        fieldW = Math.max(1, panelW - 40);
        fieldX = panelX + (panelW - fieldW) / 2;
        fieldH = compact ? (stackedButtons ? 14 : 16) : 20;

        // Name field
        nameY = panelY + (compact ? (stackedButtons ? 16 : 18) : 60);
        this.nameEdit = new DiaryTextField(this.font, fieldX, nameY, fieldW, fieldH, Component.translatable("addServer.enterName"));
        this.nameEdit.setMaxLength(64);
        this.nameEdit.setValue(this.serverData.name != null ? this.serverData.name : "");
        this.nameEdit.setResponder(s -> this.updateDoneButton());
        this.addRenderableWidget(this.nameEdit);
        this.setInitialFocus(this.nameEdit);

        // IP field
        ipY = panelY + (compact ? (stackedButtons ? 30 : 36) : 104);
        this.ipEdit = new DiaryTextField(this.font, fieldX, ipY, fieldW, fieldH, Component.translatable("addServer.enterIp"));
        this.ipEdit.setMaxLength(128);
        this.ipEdit.setValue(this.serverData.ip != null ? this.serverData.ip : "");
        this.ipEdit.setResponder(s -> this.updateDoneButton());
        this.addRenderableWidget(this.ipEdit);

        // Botones inferiores
        buttonH = compact ? (stackedButtons ? 11 : 15) : 20;
        buttonGap = compact ? (stackedButtons ? 3 : 6) : 12;
        buttonW = stackedButtons ? Math.max(1, panelW - 32)
            : Math.max(1, (panelW - 32 - buttonGap) / 2);
        int buttonBlockH = buttonH * (stackedButtons ? 2 : 1) + (stackedButtons ? buttonGap : 0);
        buttonY = panelY + panelH - buttonBlockH - (compact ? 5 : 14);
        cancelButtonY = buttonY + (stackedButtons ? buttonH + buttonGap : 0);
        int totalW = buttonW * 2 + buttonGap;
        int startX = stackedButtons ? panelX + (panelW - buttonW) / 2
            : panelX + (panelW - totalW) / 2;

        this.doneButton = new DiaryButton(startX, buttonY, buttonW, buttonH,
                Component.translatable("addServer.add"),
                stackedButtons ? DiaryButton.Icon.SCROLL
                    : (buttonW < 82 ? DiaryButton.Icon.NONE : DiaryButton.Icon.SCROLL), DiaryButton.Variant.PRIMARY,
                this::onDone);
        this.addRenderableWidget(this.doneButton);

        this.cancelButton = new DiaryButton(
                stackedButtons ? startX : startX + buttonW + buttonGap,
                cancelButtonY, buttonW, buttonH,
                Component.translatable("gui.cancel"),
                stackedButtons ? DiaryButton.Icon.DOOR
                    : (buttonW < 82 ? DiaryButton.Icon.NONE : DiaryButton.Icon.DOOR), DiaryButton.Variant.SECONDARY,
                this::onCancel);
        this.addRenderableWidget(this.cancelButton);

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
        String nameVal = this.nameEdit != null ? this.nameEdit.getValue() : "";
        String ipVal = this.ipEdit != null ? this.ipEdit.getValue() : "";
        int nameCursor = this.nameEdit != null ? this.nameEdit.getCursorPosition() : 0;
        int nameSelection = this.nameEdit instanceof DiaryTextField field
            ? field.getSelectionPosition() : nameCursor;
        int ipCursor = this.ipEdit != null ? this.ipEdit.getCursorPosition() : 0;
        int ipSelection = this.ipEdit instanceof DiaryTextField field
            ? field.getSelectionPosition() : ipCursor;
        boolean nameFocused = this.getFocused() == this.nameEdit;
        boolean ipFocused = this.getFocused() == this.ipEdit;
        boolean doneFocused = this.getFocused() == this.doneButton;
        boolean cancelFocused = this.getFocused() == this.cancelButton;
        super.resize(mc, w, h);
        this.nameEdit.setValue(nameVal);
        this.ipEdit.setValue(ipVal);
        this.nameEdit.setCursorPosition(nameCursor);
        this.nameEdit.setHighlightPos(nameSelection);
        this.ipEdit.setCursorPosition(ipCursor);
        this.ipEdit.setHighlightPos(ipSelection);
        if (nameFocused) this.setFocused(this.nameEdit);
        else if (ipFocused) this.setFocused(this.ipEdit);
        else if (doneFocused) this.setFocused(this.doneButton);
        else if (cancelFocused) this.setFocused(this.cancelButton);
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
        DiaryChrome.drawRegistryTag(gfx, panelX, panelY, panelW, panelH, "REG. XI");

        // Titulo
        int titleY = panelY + (compact ? 3 : 18);
        com.eternalcraft.gripeverde.client.render.DiaryTypography.drawCentered(
                gfx, this.font, this.title, this.width / 2, titleY, DiaryChrome.INK_TITLE);

        // Subtitulo
        String subtitle = "- " + Component.translatable("gripeverde.edit.subtitle").getString() + " -";
        int subW = this.font.width(subtitle);
        if (!compact && panelW >= 220) {
            gfx.drawString(this.font, subtitle, (this.width - subW) / 2, titleY + 12, DiaryChrome.INK_SUBTITLE, false);
        }

        // Divisor con rombo
        int dividerY = panelY + (compact ? (stackedButtons ? 14 : 17) : 44);
        int dividerInset = panelW < 220 ? 8 : 12;
        DiaryChrome.drawInkDivider(gfx, panelX + dividerInset, dividerY, Math.max(1, panelW - dividerInset * 2));
        DiaryChrome.drawDivisorLozenge(gfx, this.width / 2, dividerY);

        // Labels de los EditBox
        String nameLbl = NAME_LABEL.getString();
        if (!compact) {
            gfx.drawString(this.font, nameLbl, panelX + 20, panelY + 48, DiaryChrome.INK_SUBTITLE, false);
        }

        String ipLbl = IP_LABEL.getString();
        if (!compact) {
            gfx.drawString(this.font, ipLbl, panelX + 20, panelY + 92, DiaryChrome.INK_SUBTITLE, false);
        }

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
