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

    private DiaryTextField nameEdit;
    private DiaryTextField ipEdit;
    private DiaryButton doneButton;
    private boolean justOpened = true;

    private int panelWidth() {
        return Math.min(PANEL_MAX_W, Math.max(136, this.width - 24));
    }

    private int panelHeight() {
        return Math.min(PANEL_MAX_H, Math.max(120, this.height - 24));
    }

    private boolean compactLayout() {
        return panelHeight() < 190;
    }

    private int nameFieldOffset() {
        return compactLayout() ? 52 : 62;
    }

    private int ipFieldOffset() {
        return compactLayout() ? 88 : 108;
    }

    private static String ellipsizeTo(net.minecraft.client.gui.Font font, String text, int maxWidth){
        // v0.384.0: sin puntos suspensivos; la politica vive en un solo sitio.
        return com.eternalcraft.gripeverde.client.render.DiaryChrome.shorten(font, text, maxWidth);
    }

    public DiaryEditServerScreen(Screen parent, BooleanConsumer callback, ServerData serverData) {
        super(Component.translatable("addServer.title"));
        this.parent = parent;
        this.callback = callback;
        this.serverData = serverData;
    }

    // v0.272.0: la hoja solo suena la primera vez que se abre, no al redimensionar.
    private boolean paperSoundDone = false;

    @Override
    protected void init() {
        // v0.272.0: la hoja se abre con ruido de papel, una vez por apertura.
        if (!this.paperSoundDone) {
            this.paperSoundDone = true;
            try {
                com.eternalcraft.gripeverde.client.audio.UiSounds.open();
            } catch (Throwable ignored) {
            }
        }
        int panelW = panelWidth();
        int panelH = panelHeight();
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int fieldInset = Math.min(24, Math.max(12, (panelW - 64) / 2));
        int fieldW = Math.max(64, panelW - fieldInset * 2);
        int fieldX = panelX + (panelW - fieldW) / 2;
        int fieldH = compactLayout() ? 18 : 20;

        // Name field
        int nameY = panelY + nameFieldOffset();
        this.nameEdit = new DiaryTextField(this.font, fieldX, nameY, fieldW, fieldH, Component.translatable("addServer.enterName"));
        this.nameEdit.setMaxLength(64);
        this.nameEdit.setValue(this.serverData.name != null ? this.serverData.name : "");
        this.nameEdit.setResponder(s -> this.updateDoneButton());
        this.addRenderableWidget(this.nameEdit);
        this.setInitialFocus(this.nameEdit);

        // IP field
        int ipY = panelY + ipFieldOffset();
        this.ipEdit = new DiaryTextField(this.font, fieldX, ipY, fieldW, fieldH, Component.translatable("addServer.enterIp"));
        this.ipEdit.setMaxLength(128);
        this.ipEdit.setValue(this.serverData.ip != null ? this.serverData.ip : "");
        this.ipEdit.setResponder(s -> this.updateDoneButton());
        this.addRenderableWidget(this.ipEdit);

        // Botones inferiores
        int btnH = compactLayout() ? 18 : 20;
        int gap = Math.min(12, Math.max(4, panelW - 24 - 112));
        int btnW = Math.max(48, Math.min(100, (panelW - 24 - gap) / 2));
        int btnY = panelY + panelH - btnH - (compactLayout() ? 12 : 16);
        int totalW = btnW * 2 + gap;
        int startX = panelX + (panelW - totalW) / 2;

        this.doneButton = new DiaryButton(startX, btnY, btnW, btnH,
                Component.translatable("addServer.add"),
                DiaryButton.Icon.SCROLL, DiaryButton.Variant.PRIMARY,
                this::onDone);
        this.doneButton.setTooltip(Tooltip.create(Component.translatable("addServer.add")));
        this.addRenderableWidget(this.doneButton);

        DiaryButton cancel = new DiaryButton(startX + btnW + gap, btnY, btnW, btnH,
                Component.translatable("gui.cancel"),
                DiaryButton.Icon.DOOR, DiaryButton.Variant.SECONDARY,
                this::onCancel);
        cancel.setTooltip(Tooltip.create(Component.translatable("gui.cancel")));
        this.addRenderableWidget(cancel);

        this.updateDoneButton();
    }

    private void updateDoneButton() {
        if (this.doneButton != null && this.ipEdit != null && this.nameEdit != null) {
            // v0.263.0: la ficha solo se firma si los dos campos tienen algo y
            // la direccion tiene forma de direccion.
            this.doneButton.active = !this.nameEdit.getValue().isEmpty()
                && DiaryChrome.looksLikeAddress(this.ipEdit.getValue());
        }
    }

    /**
     * Enter moves from name to address, then submits a valid form. Tab and
     * Shift+Tab retain vanilla traversal through fields and footer buttons.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean onName = this.nameEdit != null && this.getFocused() == this.nameEdit;
        boolean onIp = this.ipEdit != null && this.getFocused() == this.ipEdit;
        // Leave Tab/Shift+Tab to Screen's traversal, including footer buttons.
        if ((onName || onIp) && (keyCode == 257 || keyCode == 335)) {
            if (onName) {
                this.setFocused(this.ipEdit);
                return true;
            }
            if (this.doneButton != null && this.doneButton.active) {
                this.onDone();
                return true;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void onDone() {
        this.serverData.name = this.nameEdit.getValue();
        // Guardar exactamente la dirección normalizada que validó el botón.
        this.serverData.ip = this.ipEdit.getValue().trim();
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
        DiaryTextField.EditingState nameState = this.nameEdit == null ? null : this.nameEdit.snapshotEditingState();
        DiaryTextField.EditingState ipState = this.ipEdit == null ? null : this.ipEdit.snapshotEditingState();
        boolean focusedIp = this.ipEdit != null && this.getFocused() == this.ipEdit;
        super.resize(mc, w, h);
        this.nameEdit.restoreEditingState(nameState);
        this.ipEdit.restoreEditingState(ipState);
        if (focusedIp) this.setFocused(this.ipEdit);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        // v0.383.0: primero el juego (fondo, rotulos y campos del vanilla);
        // despues el papel de la casa tapa todo y las fichas se repiten encima.
        super.render(gfx, mouseX, mouseY, partial);
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
        DiaryChrome.drawRegistryTag(gfx, panelX, panelY, panelW, panelH, "REG. XI");

        // Titulo
        int titleY = panelY + (compactLayout() ? 10 : 18);
        int centerX = panelX + panelW / 2;
        String titleStr = ellipsizeTo(this.font, this.title.getString(), Math.max(48, panelW - 28));
        int titleW = DiaryChrome.widthCached(this.font, titleStr);
        gfx.drawString(this.font, titleStr, centerX - titleW / 2, titleY, DiaryChrome.INK_TITLE, false);

        // Subtitulo
        String subtitle = "- " + DiaryChrome.tr("gripeverde.edit.subtitle") + " -";
        subtitle = ellipsizeTo(this.font, subtitle, Math.max(48, panelW - 28));
        int subW = DiaryChrome.widthCached(this.font, subtitle);
        gfx.drawString(this.font, subtitle, centerX - subW / 2, titleY + 12, DiaryChrome.INK_SUBTITLE, false);

        // Divisor con rombo
        DiaryChrome.drawInkDivider(gfx, panelX + 20, titleY + 26, panelW - 40);
        DiaryChrome.drawDivisorLozenge(gfx, centerX, titleY + 26);

        // Labels de los EditBox
        int fieldInset = Math.min(24, Math.max(12, (panelW - 64) / 2));
        int labelWidth = Math.max(48, panelW - fieldInset * 2);
        String nameLbl = ellipsizeTo(this.font, NAME_LABEL.getString(), labelWidth);
        int nameLabelOffset = nameFieldOffset() - (compactLayout() ? 12 : 14);
        gfx.drawString(this.font, nameLbl, panelX + fieldInset, panelY + nameLabelOffset, DiaryChrome.INK_SUBTITLE, false);

        String ipLbl = ellipsizeTo(this.font, IP_LABEL.getString(), labelWidth);
        int ipLabelOffset = ipFieldOffset() - 14;
        gfx.drawString(this.font, ipLbl, panelX + fieldInset, panelY + ipLabelOffset, DiaryChrome.INK_SUBTITLE, false);

        // v0.263.0: bajo la direccion, como se parte en anfitrion y puerto; y
        // si falta algo, se dice que falta en vez de dejar el boton apagado
        // sin explicacion.
        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        int fieldH = compactLayout() ? 18 : 20;
        int btnH = compactLayout() ? 18 : 20;
        int btnY = panelY + panelH - btnH - (compactLayout() ? 12 : 16);
        String ipTyped = this.ipEdit == null || this.ipEdit.getValue() == null
            ? "" : this.ipEdit.getValue().trim();
        String nameTyped = this.nameEdit == null || this.nameEdit.getValue() == null
            ? "" : this.nameEdit.getValue().trim();
        int readoutY = panelY + ipFieldOffset() + fieldH + 3;
        String missing;
        if (nameTyped.isEmpty()) {
            missing = en ? "the name is missing" : "falta el nombre";
        } else if (ipTyped.isEmpty()) {
            missing = en ? "the address is missing" : "falta la direccion";
        } else if (!DiaryChrome.looksLikeAddress(ipTyped)) {
            missing = en ? "that is not shaped like an address" : "eso no tiene forma de direccion";
        } else {
            missing = "";
        }
        if (missing.isEmpty() && readoutY + 10 < btnY && panelW >= 200) {
            String readout = ellipsizeTo(this.font, DiaryChrome.addressReadout(ipTyped, en),
                Math.max(48, panelW - 32));
            int rw = DiaryChrome.widthCached(this.font, readout);
            gfx.drawString(this.font, readout, centerX - rw / 2, readoutY,
                DiaryChrome.INK_FLAVOR, false);
        } else if (!missing.isEmpty() && readoutY + 10 < btnY) {
            String warn = ellipsizeTo(this.font, missing, Math.max(48, panelW - 32));
            int ww = DiaryChrome.widthCached(this.font, warn);
            gfx.drawString(this.font, warn, centerX - ww / 2, readoutY, 0xFF8E2A20, false);
        }

        // v0.349.0: el pie de teclas caia sobre el canto roto de la hoja. Ahora
        // va encima de las fichas y, si el cuerpo no da, se calla.
        if (!compactLayout()) {
            int hintY = panelY + panelH - btnH - 16 - 12;
            if (hintY > readoutY + 11) {
                String keys = en ? "Tab changes field  ·  Enter signs  ·  Esc back"
                    : "Tab cambia de campo  ·  Enter firma  ·  Esc vuelve";
                keys = ellipsizeTo(this.font, keys, Math.max(48, panelW - 32));
                int kw = DiaryChrome.widthCached(this.font, keys);
                gfx.drawString(this.font, keys, centerX - kw / 2, hintY,
                    DiaryChrome.INK_FLAVOR, false);
            }
        }

        // v0.383.0: las fichas y los campos, encima de la hoja: super.render()
        // va al principio del metodo, asi el juego no deja nada encima del papel.
        com.eternalcraft.gripeverde.client.render.DiaryChrome.renderWidgets(
            this, gfx, mouseX, mouseY, partial);
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
        this.callback.accept(false);
    }
}
