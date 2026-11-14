package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.integration.CopyLCompatibility;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryTextField;
import com.eternalcraft.gripeverde.client.widgets.SlimButton;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Parte de guardia de la CEDA, dentro del propio menu del diario.
 *
 * v0.180.0: deja de depender del puente por reflexion que abria la pantalla de
 * CopyL. El formulario es nuestro (pergamino, renglones numerados, aire de
 * sobra) y envia cada renglon al chat de la partida; si CopyL esta instalado,
 * queda un acceso a su panel desde la esquina de la ficha.
 *
 * El borrador vive en un campo estatico para no perder lo escrito al cerrar y
 * volver a abrir la pantalla, y solo se envia lo que el jugador pide: nunca se
 * escribe en el chat por iniciativa propia.
 */
public class DiaryDispatchScreen extends Screen {

    private static final int PANEL_MAX_W = 400;
    private static final int PANEL_MAX_H = 300;
    private static final int MAX_LINES = 8;
    private static final int MAX_LINE_LEN = 200;

    /** Carril del numero de renglon: sin el, los campos quedan pegados al borde. */
    private static final int GUTTER = 34;
    /** Aire vertical entre renglones (12 px de hueco real entre campos de 16). */
    private static final int ROW_H = 16;
    private static final int ROW_GAP = 12;
    private static final int ROW_STEP = ROW_H + ROW_GAP;
    private static final int FOOTER_H = 84;
    /** Medio segundo entre mensajes del envio en tanda, para no caer en spam. */
    private static final int SEND_GAP_TICKS = 10;
    private static final long STATUS_MS = 1600L;

    /** Borrador de la sesion: ocho renglones que sobreviven a cerrar la ficha. */
    private static final String[] DRAFT = new String[MAX_LINES];

    private final Screen parent;
    private final List<DiaryTextField> fields = new ArrayList<>();

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int rowsTop;
    private int rowsBottom;
    private int fieldX;
    private int fieldW;

    private DiaryButton sendButton;
    private DiaryButton sendAllButton;
    private DiaryButton clearButton;

    private int batchCursor = -1;
    private int batchDelay;
    private String statusKey = "";
    private long statusUntil;

    public DiaryDispatchScreen(Screen parent) {
        super(Component.translatable("gripeverde.dispatch.title"));
        this.parent = parent;
    }

    private boolean compactLayout() {
        return this.height < 300;
    }

    private int lineCount() {
        return Math.min(MAX_LINES, this.fields.size());
    }

    @Override
    protected void init() {
        this.panelW = Math.min(PANEL_MAX_W, Math.max(240, this.width - 24));
        this.panelH = Math.min(PANEL_MAX_H, Math.max(190, this.height - 24));
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        int titleY = this.panelY + (compactLayout() ? 10 : 16);
        int dividerY = titleY + 26;
        this.rowsTop = dividerY + 14;
        this.rowsBottom = this.panelY + this.panelH - FOOTER_H;

        int rows = (this.rowsBottom - this.rowsTop) / ROW_STEP;
        rows = Math.max(3, Math.min(MAX_LINES, rows));

        this.fieldX = this.panelX + GUTTER + 8;
        this.fieldW = Math.max(72, this.panelW - GUTTER - 8 - 14);

        this.fields.clear();
        for (int i = 0; i < rows; i++) {
            DiaryTextField field = new DiaryTextField(
                this.font, this.fieldX, this.rowsTop + i * ROW_STEP, this.fieldW, ROW_H,
                Component.translatable("gripeverde.dispatch.line", i + 1));
            field.setMaxLength(MAX_LINE_LEN);
            field.setValue(DRAFT[i] == null ? "" : DRAFT[i]);
            if (i == 0) {
                field.setHint(Component.translatable("gripeverde.dispatch.hint"));
            }
            final int index = i;
            field.setResponder(text -> {
                DRAFT[index] = text;
                this.refreshButtons();
            });
            this.fields.add(field);
            this.addRenderableWidget(field);
        }
        if (!this.fields.isEmpty()) {
            this.setInitialFocus(this.fields.get(0));
        }

        // --- Pie: estado + cuatro botones en dos filas (aire entre ellos) ---
        int footerTop = this.panelY + this.panelH - FOOTER_H;
        int btnInset = Math.min(22, Math.max(12, this.panelW / 16));
        int gap = 10;
        int btnW = (this.panelW - btnInset * 2 - gap) / 2;
        int btnH = compactLayout() ? 18 : 20;
        int row1 = footerTop + 22;
        int row2 = row1 + btnH + gap;

        this.sendButton = new DiaryButton(this.panelX + btnInset, row1, btnW, btnH,
            Component.translatable("gripeverde.dispatch.button.send"),
            DiaryButton.Icon.CHAT, DiaryButton.Variant.PRIMARY, this::sendFocusedLine);
        this.addRenderableWidget(this.sendButton);

        this.sendAllButton = new DiaryButton(this.panelX + btnInset + btnW + gap, row1, btnW, btnH,
            Component.translatable("gripeverde.dispatch.button.send_all"),
            DiaryButton.Icon.SCROLL, DiaryButton.Variant.SECONDARY, this::sendAll);
        this.addRenderableWidget(this.sendAllButton);

        this.clearButton = new DiaryButton(this.panelX + btnInset, row2, btnW, btnH,
            Component.translatable("gripeverde.dispatch.button.clear"),
            DiaryButton.Icon.PALETTE, DiaryButton.Variant.SECONDARY, this::clearAll);
        this.addRenderableWidget(this.clearButton);

        DiaryButton back = new DiaryButton(
            this.panelX + btnInset + btnW + gap, row2, btnW, btnH,
            Component.translatable("gripeverde.dispatch.button.back"),
            DiaryButton.Icon.DOOR, DiaryButton.Variant.SECONDARY, this::onClose);
        this.addRenderableWidget(back);

        // CopyL sigue siendo opcional: si esta, se entra a su panel desde aca.
        if (CopyLCompatibility.isAvailable()) {
            SlimButton panel = new SlimButton(
                this.panelX + this.panelW - 28, this.panelY + 8, 20, 20,
                Component.translatable("gripeverde.dispatch.copyl"),
                () -> CopyLCompatibility.openSettings(this))
                .withIcon(SlimButton.Icon.DISPATCH_NOTE);
            panel.setTooltip(Tooltip.create(Component.translatable("gripeverde.dispatch.copyl")));
            this.addRenderableWidget(panel);
        }

        this.refreshButtons();
    }

    private boolean hasText() {
        for (DiaryTextField field : this.fields) {
            if (!field.getValue().trim().isEmpty()) return true;
        }
        return false;
    }

    private boolean connected() {
        return this.minecraft != null
            && this.minecraft.player != null
            && this.minecraft.player.connection != null;
    }

    private void refreshButtons() {
        boolean text = this.hasText();
        if (this.sendButton != null) this.sendButton.active = text;
        if (this.sendAllButton != null) this.sendAllButton.active = text;
        if (this.clearButton != null) this.clearButton.active = text;
    }

    private void setStatus(String key) {
        this.statusKey = key;
        this.statusUntil = Util.getMillis() + STATUS_MS;
    }

    private int focusedIndex() {
        for (int i = 0; i < this.fields.size(); i++) {
            if (this.fields.get(i).isFocused()) return i;
        }
        return -1;
    }

    private int firstWithText(int from) {
        for (int i = Math.max(0, from); i < this.fields.size(); i++) {
            if (!this.fields.get(i).getValue().trim().isEmpty()) return i;
        }
        return -1;
    }

    /** Envia un renglon; devuelve false si estaba vacio o no hay conexion. */
    private boolean sendLine(int index) {
        if (index < 0 || index >= this.fields.size()) return false;
        DiaryTextField field = this.fields.get(index);
        String text = field.getValue().trim();
        if (text.isEmpty()) return false;
        if (!this.connected()) {
            this.setStatus("gripeverde.dispatch.offline");
            return false;
        }
        this.minecraft.player.connection.sendChat(text);
        field.setValue("");
        DRAFT[index] = "";
        return true;
    }

    private void sendFocusedLine() {
        int index = this.focusedIndex();
        if (index < 0 || this.fields.get(index).getValue().trim().isEmpty()) {
            index = this.firstWithText(0);
        }
        if (this.sendLine(index)) {
            this.setStatus("gripeverde.dispatch.sent");
            int next = firstWithText(index + 1);
            if (next >= 0) this.setFocused(this.fields.get(next));
        }
        this.refreshButtons();
    }

    private void sendAll() {
        if (this.firstWithText(0) < 0) {
            this.setStatus("gripeverde.dispatch.empty");
            return;
        }
        this.batchCursor = 0;
        this.batchDelay = 0;
        this.setStatus("gripeverde.dispatch.queued");
    }

    private void clearAll() {
        this.batchCursor = -1;
        for (int i = 0; i < this.fields.size(); i++) {
            this.fields.get(i).setValue("");
            DRAFT[i] = "";
        }
        UiSounds.back();
        this.refreshButtons();
        this.setFocused(this.fields.get(0));
    }

    private void stopBatch() {
        this.batchCursor = -1;
        this.batchDelay = 0;
    }

    @Override
    public void tick() {
        for (DiaryTextField field : this.fields) {
            field.tick();
        }
        if (this.batchCursor >= 0) {
            if (!this.connected()) {
                this.stopBatch();
                this.setStatus("gripeverde.dispatch.offline");
            } else if (this.batchDelay > 0) {
                this.batchDelay--;
            } else {
                int next = this.firstWithText(this.batchCursor);
                if (next < 0) {
                    this.stopBatch();
                    this.setStatus("gripeverde.dispatch.sent");
                } else {
                    this.sendLine(next);
                    this.batchCursor = next + 1;
                    this.batchDelay = SEND_GAP_TICKS;
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 258 /* GLFW_KEY_TAB */ && !this.fields.isEmpty()) {
            int current = this.focusedIndex();
            int step = (modifiers & 1) != 0 ? -1 : 1; // Shift+Tab vuelve atras
            int next = current < 0 ? 0 : Math.floorMod(current + step, this.fields.size());
            this.setFocused(this.fields.get(next));
            return true;
        }
        if (keyCode == 257 /* GLFW_KEY_ENTER */ || keyCode == 335 /* keypad enter */) {
            this.sendFocusedLine();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        try {
            com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer
                .renderDiaryBackground(gfx, this.width, this.height);
        } catch (Throwable ignored) {
        }

        DiaryChrome.drawPanelShadow(gfx, this.panelX, this.panelY, this.panelW, this.panelH);
        PaperTexture.drawClean(gfx, this.panelX, this.panelY, this.panelW, this.panelH,
            PaperTexture.PaperKind.OFFICIAL);
        DiaryChrome.drawCornerOrnaments(gfx, this.panelX, this.panelY, this.panelW, this.panelH);
        DiaryChrome.drawRegistryTag(gfx, this.panelX, this.panelY, this.panelW, this.panelH, "REG. XII");

        int titleY = this.panelY + (compactLayout() ? 10 : 16);
        int centerX = this.panelX + this.panelW / 2;
        String title = ellipsize(this.font, Component.translatable("gripeverde.dispatch.title").getString(),
            Math.max(64, this.panelW - 60));
        gfx.drawString(this.font, title, centerX - this.font.width(title) / 2, titleY,
            DiaryChrome.INK_TITLE, false);

        String subtitle = ellipsize(this.font,
            "- " + Component.translatable("gripeverde.dispatch.subtitle").getString() + " -",
            Math.max(64, this.panelW - 40));
        gfx.drawString(this.font, subtitle, centerX - this.font.width(subtitle) / 2, titleY + 12,
            DiaryChrome.INK_SUBTITLE, false);

        DiaryChrome.drawInkDivider(gfx, this.panelX + 18, titleY + 26, this.panelW - 36);
        DiaryChrome.drawDivisorLozenge(gfx, centerX, titleY + 26);

        // Numeros de renglon, alineados a la derecha del carril de la izquierda.
        for (int i = 0; i < this.fields.size(); i++) {
            String number = String.format("%02d", i + 1);
            int numberX = this.fieldX - 6 - this.font.width(number);
            gfx.drawString(this.font, number, numberX, this.fields.get(i).getY() + 4,
                DiaryChrome.INK_SUBTITLE, false);
        }

        int footerTop = this.panelY + this.panelH - FOOTER_H;
        DiaryChrome.drawInkDivider(gfx, this.panelX + 18, footerTop, this.panelW - 36);

        String note;
        int noteColor = DiaryChrome.INK_SUBTITLE;
        if (!this.statusKey.isEmpty() && Util.getMillis() < this.statusUntil) {
            note = Component.translatable(this.statusKey).getString();
        } else if (!this.connected()) {
            note = Component.translatable("gripeverde.dispatch.offline").getString();
            noteColor = DiaryChrome.INK_FLAVOR;
        } else {
            note = Component.translatable("gripeverde.dispatch.ready").getString();
        }
        note = ellipsize(this.font, note, Math.max(64, this.panelW - 36));
        gfx.drawString(this.font, note, centerX - this.font.width(note) / 2, footerTop + 7,
            noteColor, false);

        super.render(gfx, mouseX, mouseY, partial);
    }

    @Override
    public void renderBackground(GuiGraphics gfx) {
        // Sin el fondo de tierra del vanilla: el pergamino tapa todo.
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.stopBatch();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private static String ellipsize(net.minecraft.client.gui.Font font, String text, int maxWidth) {
        if (text == null) return "";
        if (font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        int room = Math.max(0, maxWidth - font.width(ellipsis));
        return font.plainSubstrByWidth(text, room) + ellipsis;
    }
}
