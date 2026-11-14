package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.DispatchHotkeys;
import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.integration.CopyLCompatibility;
import com.eternalcraft.gripeverde.client.integration.DispatchLibrary;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryTextField;
import com.eternalcraft.gripeverde.client.widgets.SlimButton;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Parte de guardia de la CEDA, dentro del propio menu del diario.
 *
 * v0.180.0: deja de depender del puente por reflexion que abria la pantalla de
 * CopyL. El formulario es nuestro (pergamino, renglones numerados, aire de
 * sobra) y envia cada renglon al chat de la partida.
 *
 * v0.181.0: la ficha tambien muestra las diez ranuras del parte (nombre, tecla y
 * texto) que antes vivian solo en CopyL. Se importan una vez desde
 * config/copyl-messages.json a config/gripeverde-dispatch.json, de modo que
 * CopyL puede salir de la carpeta de mods sin perder nada. Las ranuras se
 * envian con su tecla (DispatchHotkeys) mientras CopyL no este instalado.
 *
 * El borrador del formulario vive en un campo estatico para no perder lo escrito
 * al cerrar y volver a abrir la pantalla, y solo se envia lo que el jugador pide:
 * nunca se escribe en el chat por iniciativa propia.
 */
public class DiaryDispatchScreen extends Screen {

    private static final int PANEL_MAX_W = 520;
    /** 380 de alto: 56 de cabecera + 84 de pie dejan sitio a los ocho renglones. */
    private static final int PANEL_MAX_H = 380;
    private static final int MAX_LINES = 8;
    private static final int MAX_LINE_LEN = 200;
    /** Bajo esta anchura la ficha no abre la columna de ranuras: usa la tira. */
    private static final int COLUMN_MIN_W = 420;

    /** Carril del numero de renglon: sin el, los campos quedan pegados al borde. */
    private static final int GUTTER = 34;
    /** Aire vertical entre renglones (12 px de hueco real entre campos de 16). */
    private static final int ROW_H = 16;
    private static final int ROW_GAP = 12;
    private static final int ROW_STEP = ROW_H + ROW_GAP;
    private static final int SLOT_STEP = 22;
    private static final int CHIP_H = 16;
    private static final int FOOTER_H = 84;
    /** Medio segundo entre mensajes del envio en tanda, para no caer en spam. */
    private static final int SEND_GAP_TICKS = 10;
    private static final long STATUS_MS = 2200L;

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

    private boolean slotColumn;
    private int slotsX;
    private int slotsW;
    private int chipsY;
    private int chipW;
    private int chipGap = 3;

    private DiaryButton sendButton;
    private DiaryButton sendAllButton;
    private DiaryButton clearButton;

    private int batchCursor = -1;
    private int batchDelay;
    private String statusKey = "";
    private long statusUntil;
    private int awaitingSlot = -1;
    private int hoveredSlot = -1;

    public DiaryDispatchScreen(Screen parent) {
        super(Component.translatable("gripeverde.dispatch.title"));
        this.parent = parent;
    }

    private boolean compactLayout() {
        return this.height < 300;
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

        this.slotColumn = this.panelW >= COLUMN_MIN_W;
        this.fieldX = this.panelX + GUTTER + 8;
        int available = this.panelW - (GUTTER + 8) - 14;
        if (this.slotColumn) {
            this.fieldW = Math.max(150, available * 58 / 100);
            this.slotsW = Math.max(110, available - this.fieldW - 14);
            this.slotsX = this.fieldX + this.fieldW + 14;
            this.chipsY = 0;
        } else {
            this.fieldW = Math.max(72, available);
            this.slotsW = 0;
            this.chipsY = this.rowsTop;
            this.chipW = Math.max(12, (this.fieldW - (DispatchLibrary.SLOT_COUNT - 1) * this.chipGap)
                / DispatchLibrary.SLOT_COUNT);
        }

        int composerTop = this.slotColumn ? this.rowsTop : this.rowsTop + CHIP_H + 6;
        int rows = (this.rowsBottom - composerTop) / ROW_STEP;
        rows = Math.max(1, Math.min(MAX_LINES, rows));

        this.fields.clear();
        for (int i = 0; i < rows; i++) {
            DiaryTextField field = new DiaryTextField(
                this.font, this.fieldX, composerTop + i * ROW_STEP, this.fieldW, ROW_H,
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

    private int firstEmpty() {
        for (int i = 0; i < this.fields.size(); i++) {
            if (this.fields.get(i).getValue().trim().isEmpty()) return i;
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
        if (!this.fields.isEmpty()) this.setFocused(this.fields.get(0));
    }

    private void stopBatch() {
        this.batchCursor = -1;
        this.batchDelay = 0;
    }

    // ------------------------------------------------------- ranuras de CEDA

    private void sendSlot(int slot) {
        if (DispatchLibrary.message(slot).isBlank()) {
            this.setStatus("gripeverde.dispatch.empty");
            return;
        }
        if (DispatchHotkeys.send(this.minecraft, slot)) {
            this.setStatus("gripeverde.dispatch.sent");
        } else {
            this.setStatus("gripeverde.dispatch.offline");
        }
    }

    private void loadSlotIntoComposer(int slot) {
        String text = DispatchLibrary.message(slot);
        if (text.isBlank()) {
            this.setStatus("gripeverde.dispatch.empty");
            return;
        }
        int target = this.firstEmpty();
        if (target < 0) target = Math.max(0, this.focusedIndex());
        if (target < 0 || this.fields.isEmpty()) return;
        DiaryTextField field = this.fields.get(target);
        field.setValue(text.length() > MAX_LINE_LEN ? text.substring(0, MAX_LINE_LEN) : text);
        this.setFocused(field);
        this.setStatus("gripeverde.dispatch.slot.loaded");
    }

    private void saveComposerIntoSlot(int slot) {
        int index = this.focusedIndex();
        if (index < 0 || this.fields.get(index).getValue().trim().isEmpty()) {
            index = this.firstWithText(0);
        }
        if (index < 0) {
            this.setStatus("gripeverde.dispatch.empty");
            return;
        }
        DispatchLibrary.setMessage(slot, this.fields.get(index).getValue());
        this.setStatus("gripeverde.dispatch.slot.saved");
    }

    /** Rectangulo de la ranura bajo el puntero, o -1. */
    private int slotAt(double mouseX, double mouseY) {
        if (this.slotColumn) {
            if (mouseX < this.slotsX || mouseX > this.slotsX + this.slotsW) return -1;
            int offset = (int) (mouseY - this.rowsTop);
            if (offset < 0) return -1;
            int index = offset / SLOT_STEP;
            if (index < 0 || index >= DispatchLibrary.SLOT_COUNT) return -1;
            if (offset % SLOT_STEP > CHIP_H) return -1;
            return index;
        }
        if (this.chipW <= 0) return -1;
        if (mouseY < this.chipsY || mouseY > this.chipsY + CHIP_H) return -1;
        for (int i = 0; i < DispatchLibrary.SLOT_COUNT; i++) {
            int x = this.fieldX + i * (this.chipW + this.chipGap);
            if (mouseX >= x && mouseX <= x + this.chipW) return i;
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int slot = this.slotAt(mouseX, mouseY);
        if (slot >= 0) {
            if (button == 1) {
                this.awaitingSlot = slot;
                this.setStatus("gripeverde.dispatch.slot.listen");
            } else if (button == 0) {
                if (hasControlDown()) {
                    this.saveComposerIntoSlot(slot);
                } else if (hasShiftDown()) {
                    this.loadSlotIntoComposer(slot);
                } else {
                    this.sendSlot(slot);
                }
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
        if (this.awaitingSlot >= 0) {
            if (keyCode == 256) { // Escape cancela sin tocar la ranura
                this.awaitingSlot = -1;
                this.setStatus("");
                return true;
            }
            if (keyCode == 261) { // Supr borra la tecla de la ranura
                DispatchLibrary.setKey(this.awaitingSlot, DispatchLibrary.UNBOUND);
                this.awaitingSlot = -1;
                this.setStatus("gripeverde.dispatch.slot.cleared");
                return true;
            }
            DispatchLibrary.setKey(this.awaitingSlot, keyCode);
            this.awaitingSlot = -1;
            this.setStatus("gripeverde.dispatch.slot.bound");
            return true;
        }
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

        this.hoveredSlot = this.slotAt(mouseX, mouseY);
        if (this.slotColumn) {
            this.renderSlotColumn(gfx);
        } else {
            this.renderSlotChips(gfx);
        }

        int footerTop = this.panelY + this.panelH - FOOTER_H;
        DiaryChrome.drawInkDivider(gfx, this.panelX + 18, footerTop, this.panelW - 36);

        String note;
        int noteColor = DiaryChrome.INK_SUBTITLE;
        if (!this.statusKey.isEmpty() && Util.getMillis() < this.statusUntil) {
            note = Component.translatable(this.statusKey).getString();
        } else if (this.awaitingSlot >= 0) {
            note = Component.translatable("gripeverde.dispatch.slot.listen").getString();
            noteColor = DiaryChrome.INK_FLAVOR;
        } else if (this.hoveredSlot >= 0) {
            note = Component.translatable("gripeverde.dispatch.slot.hint").getString();
        } else if (!this.connected()) {
            note = Component.translatable("gripeverde.dispatch.offline").getString();
            noteColor = DiaryChrome.INK_FLAVOR;
        } else {
            note = this.readyNote();
        }
        note = ellipsize(this.font, note, Math.max(64, this.panelW - 36));
        gfx.drawString(this.font, note, centerX - this.font.width(note) / 2, footerTop + 7,
            noteColor, false);

        super.render(gfx, mouseX, mouseY, partial);
    }

    private String readyNote() {
        if (DispatchHotkeys.suspendedByCopyL()) {
            return Component.translatable("gripeverde.dispatch.ready.copyl").getString();
        }
        int saved = DispatchLibrary.countWithText();
        if (saved > 0) {
            return Component.translatable("gripeverde.dispatch.ready.slots", saved).getString();
        }
        return Component.translatable("gripeverde.dispatch.ready").getString();
    }

    private void renderSlotColumn(GuiGraphics gfx) {
        for (int slot = 0; slot < DispatchLibrary.SLOT_COUNT; slot++) {
            int y = this.rowsTop + slot * SLOT_STEP;
            boolean hover = this.hoveredSlot == slot;
            boolean listening = this.awaitingSlot == slot;
            int fill = hover || listening ? DiaryThemeTint.HOVER : DiaryThemeTint.CELL;
            gfx.fill(this.slotsX, y, this.slotsX + this.slotsW, y + CHIP_H, fill);
            gfx.fill(this.slotsX, y, this.slotsX + this.slotsW, y + 1,
                listening ? DiaryChrome.INK_TITLE : DiaryChrome.INK_DIVIDER);
            gfx.fill(this.slotsX, y + CHIP_H - 1, this.slotsX + this.slotsW, y + CHIP_H,
                DiaryChrome.INK_DIVIDER);

            String keyName = DispatchLibrary.keyName(slot).getString();
            int keyW = this.font.width(keyName);
            String name = ellipsize(this.font, DispatchLibrary.name(slot),
                Math.max(24, this.slotsW - keyW - 14));
            gfx.drawString(this.font, name, this.slotsX + 5, y + 4, DiaryChrome.INK_TITLE, false);
            gfx.drawString(this.font, keyName, this.slotsX + this.slotsW - keyW - 5, y + 4,
                DispatchLibrary.isBound(slot) ? DiaryChrome.INK_SUBTITLE : DiaryThemeTint.FAINT, false);
        }
    }

    private void renderSlotChips(GuiGraphics gfx) {
        for (int slot = 0; slot < DispatchLibrary.SLOT_COUNT; slot++) {
            int x = this.fieldX + slot * (this.chipW + this.chipGap);
            boolean hover = this.hoveredSlot == slot;
            boolean listening = this.awaitingSlot == slot;
            boolean filled = !DispatchLibrary.message(slot).isBlank();
            gfx.fill(x, this.chipsY, x + this.chipW, this.chipsY + CHIP_H,
                hover || listening ? DiaryThemeTint.HOVER : DiaryThemeTint.CELL);
            gfx.fill(x, this.chipsY, x + this.chipW, this.chipsY + 1,
                listening ? DiaryChrome.INK_TITLE : DiaryChrome.INK_DIVIDER);
            gfx.fill(x, this.chipsY, x + 1, this.chipsY + CHIP_H,
                DispatchLibrary.isBound(slot) ? DiaryChrome.INK_SUBTITLE : DiaryChrome.INK_DIVIDER);
            String label = String.format("%02d", slot + 1);
            gfx.drawString(this.font, label, x + (this.chipW - this.font.width(label)) / 2,
                this.chipsY + 4, filled ? DiaryChrome.INK_TITLE : DiaryThemeTint.FAINT, false);
        }
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
        this.awaitingSlot = -1;
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

    /** Tintes propios de la ficha, sobre el pergamino de DiaryTheme. */
    private static final class DiaryThemeTint {
        private static final int CELL = 0x14FFFFFF;
        private static final int HOVER = 0x38C89058;
        private static final int FAINT = 0x99887766;

        private DiaryThemeTint() {
        }
    }
}
