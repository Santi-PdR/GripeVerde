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

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Las veinte voces del parte de CEDA.
 *
 * v0.184.0: la ficha se rehizo entera porque no se entendia. Antes habia ocho
 * renglones para escribir a mano, diez ranuras al costado y dos botones de
 * envio; ahora cada renglon de la lista es un mensaje guardado, con su campo de
 * texto y su tecla a la derecha, y nada mas: no hay envio desde la ficha, el
 * mensaje sale con su tecla cuando estas jugando. v0.184.2: las voces pasan de
 * diez a veinte y se reparten en paginas de hasta doce, con rueda, RePag/AvPag
 * y clic derecho en el rotulo para volver.
 *
 * - Clic en la tecla: la ficha escucha; la proxima tecla que aprietes queda
 *   asignada (Esc cancela, Supr la borra, clic derecho tambien la borra).
 * - Autoguardado: cada cambio se escribe en config/gripeverde-dispatch.json
 *   medio segundo despues de la ultima tecla, y al cerrar la ficha.
 * - Si la ventana es baja, las voces se reparten en paginas y el pie dice como
 *   pasar a la siguiente.
 *
 * La importacion desde CopyL sigue en DispatchLibrary y los atajos en
 * DispatchHotkeys; aca solo se edita lo que ya esta guardado.
 */
public class DiaryDispatchScreen extends Screen {

    private static final int PANEL_MAX_W = 520;
    private static final int PANEL_MAX_H = 380;
    private static final int ROW_H = 16;
    private static final int ROW_GAP = 10;
    private static final int ROW_STEP = ROW_H + ROW_GAP;
    private static final int GUTTER = 30;
    private static final int MAX_TEXT = 256;
    private static final long AUTOSAVE_MS = 700L;
    private static final long STATUS_MS = 1500L;

    private final Screen parent;
    private final List<DiaryTextField> fields = new ArrayList<>();
    private final List<Integer> rowSlots = new ArrayList<>();

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int rowsTop;
    private int fieldX;
    private int fieldW;
    private int keyX;
    private int keyW;
    private int perPage;
    private int page;
    private int pageCount;
    private int toggleX;
    private int toggleY;
    private int toggleW;
    // v0.240.0: el pie tiene banda propia; los botones de pagina van en su fila.
    private int footerDivY;
    private int hintY;
    private int countY;
    private int footerBtnY;
    private int footerBtnH;

    private int awaitingSlot = -1;
    private int hoveredSlot = -1;
    private boolean dirty;
    private long lastEditMs;
    private String statusKey = "";
    private long statusUntil;

    public DiaryDispatchScreen(Screen parent) {
        super(Component.translatable("gripeverde.dispatch.title"));
        this.parent = parent;
    }

    private boolean compactLayout() {
        return this.height < 300;
    }

    @Override
    protected void init() {
        DispatchLibrary.ensureLoaded();
        this.panelW = Math.min(PANEL_MAX_W, Math.max(260, this.width - 24));
        this.panelH = Math.min(PANEL_MAX_H, Math.max(190, this.height - 24));
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        int titleY = this.panelY + (compactLayout() ? 10 : 14);
        this.rowsTop = titleY + 40;
        int rowsBottom = this.panelY + this.panelH - 66 - 8;
        int fit = Math.max(1, (rowsBottom - this.rowsTop) / ROW_STEP);
        this.perPage = Math.min(DispatchLibrary.SLOT_COUNT, Math.min(12, fit));
        this.pageCount = (DispatchLibrary.SLOT_COUNT + this.perPage - 1) / this.perPage;
        if (this.page >= this.pageCount) this.page = 0;

        this.keyW = Math.max(64, Math.min(96, this.panelW / 5));
        this.fieldX = this.panelX + GUTTER + 6;
        this.fieldW = Math.max(72, this.panelW - GUTTER - 18 - this.keyW - 12);
        this.keyX = this.fieldX + this.fieldW + 8;

        this.fields.clear();
        this.rowSlots.clear();
        int first = this.page * this.perPage;
        int last = Math.min(DispatchLibrary.SLOT_COUNT, first + this.perPage);
        for (int slot = first; slot < last; slot++) {
            int row = slot - first;
            int y = this.rowsTop + row * ROW_STEP;
            DiaryTextField field = new DiaryTextField(
                this.font, this.fieldX, y, this.fieldW, ROW_H,
                Component.translatable("gripeverde.dispatch.slot.label", slot + 1));
            field.setMaxLength(MAX_TEXT);
            field.setValue(DispatchLibrary.message(slot));
            field.setHint(Component.translatable("gripeverde.dispatch.slot.empty"));
            final int index = slot;
            field.setResponder(text -> this.onTextChanged(index, text));
            this.fields.add(field);
            this.rowSlots.add(slot);
            this.addRenderableWidget(field);
        }
        if (!this.fields.isEmpty()) {
            this.setInitialFocus(this.fields.get(0));
        }

        // v0.240.0: el pie se ordena en tres lineas -ayuda, recuento y botones-;
        // antes el rotulo de pagina caia debajo del boton de volver y el texto
        // asomaba por detras.
        this.footerDivY = this.panelY + this.panelH - 66;
        this.hintY = this.footerDivY + 4;
        this.countY = this.footerDivY + 15;
        this.footerBtnH = compactLayout() ? 18 : 20;
        this.footerBtnY = this.panelY + this.panelH - 14 - this.footerBtnH;

        int arrowW = 26;
        int arrowH = this.footerBtnH;
        int btnW = Math.max(96, Math.min(150, this.panelW - 2 * arrowW - 60));
        DiaryButton back = new DiaryButton(
            this.panelX + (this.panelW - btnW) / 2, this.footerBtnY, btnW, arrowH,
            Component.translatable("gripeverde.dispatch.button.back"),
            DiaryButton.Icon.DOOR, DiaryButton.Variant.SECONDARY, this::onClose);
        this.addRenderableWidget(back);

        if (this.pageCount > 1) {
            this.addRenderableWidget(new DiaryButton(
                this.panelX + 20, this.footerBtnY, arrowW, arrowH,
                Component.literal("<"), DiaryButton.Icon.NONE, DiaryButton.Variant.SECONDARY,
                () -> this.turnPage(-1)).noEllipsis());
            this.addRenderableWidget(new DiaryButton(
                this.panelX + this.panelW - 20 - arrowW, this.footerBtnY, arrowW, arrowH,
                Component.literal(">"), DiaryButton.Icon.NONE, DiaryButton.Variant.SECONDARY,
                () -> this.turnPage(1)).noEllipsis());
        }

        // El rotulo de pagina va al final de la linea de ayuda, donde no choca.
        String toggle = this.pageLabel();
        this.toggleW = this.font.width(toggle);
        this.toggleX = this.panelX + this.panelW - 20 - this.toggleW;
        this.toggleY = this.hintY;

        if (CopyLCompatibility.isAvailable()) {
            SlimButton panel = new SlimButton(
                this.panelX + this.panelW - 28, this.panelY + 6, 20, 20,
                Component.translatable("gripeverde.dispatch.copyl"),
                () -> CopyLCompatibility.openSettings(this))
                .withIcon(SlimButton.Icon.DISPATCH_NOTE);
            panel.setTooltip(Tooltip.create(Component.translatable("gripeverde.dispatch.copyl")));
            this.addRenderableWidget(panel);
        }
    }

    private String pageLabel() {
        if (this.pageCount <= 1) return "";
        return Component.translatable("gripeverde.dispatch.page",
            this.page + 1, this.pageCount).getString();
    }

    // ------------------------------------------------------------- guardado

    private void onTextChanged(int slot, String text) {
        DispatchLibrary.setMessage(slot, text);
        this.dirty = true;
        this.lastEditMs = Util.getMillis();
        // v0.245.0: la primera voz escrita es una carta al barrio que no
        // responde; queda anotada en el archivo.
        if (text != null && !text.isBlank()) {
            try {
                com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                    com.eternalcraft.gripeverde.client.lore.Discoveries.LETTERS);
            } catch (Throwable ignored) {}
        }
    }

    private void saveIfNeeded(boolean force) {
        if (!this.dirty) return;
        long now = Util.getMillis();
        if (!force && now - this.lastEditMs < AUTOSAVE_MS) return;
        this.dirty = false;
        DispatchLibrary.save();
        this.setStatus("gripeverde.dispatch.saved");
    }

    private void setStatus(String key) {
        this.statusKey = key;
        this.statusUntil = Util.getMillis() + STATUS_MS;
    }

    // v0.196.0: el numero de cada voz y el nombre de su tecla se piden una vez.
    // Antes se armaban con String.format y con keyName(...).getString() por cada
    // renglon visible en cada cuadro.
    private static final String[] SLOT_NUMBERS = new String[DispatchLibrary.SLOT_COUNT];
    static {
        for (int i = 0; i < SLOT_NUMBERS.length; i++) {
            int n = i + 1;
            SLOT_NUMBERS[i] = n < 10 ? "0" + n : Integer.toString(n);
        }
    }
    private String[] keyLabels;
    private int[] keyBindings;

    private String keyLabel(int slot) {
        if (this.keyLabels == null) {
            this.keyLabels = new String[DispatchLibrary.SLOT_COUNT];
            this.keyBindings = new int[DispatchLibrary.SLOT_COUNT];
            java.util.Arrays.fill(this.keyBindings, Integer.MIN_VALUE);
        }
        if (slot < 0 || slot >= this.keyLabels.length) {
            return DispatchLibrary.keyName(slot).getString();
        }
        int binding = DispatchLibrary.key(slot);
        if (this.keyLabels[slot] == null || this.keyBindings[slot] != binding) {
            this.keyBindings[slot] = binding;
            this.keyLabels[slot] = DispatchLibrary.keyName(slot).getString();
        }
        return this.keyLabels[slot];
    }

    @Override
    public void tick() {
        for (DiaryTextField field : this.fields) {
            field.tick();
        }
        this.saveIfNeeded(false);
    }

    // ---------------------------------------------------------------- clics

    /** Fila visible bajo el puntero, como indice de la lista de campos. */
    private int rowAt(double mouseX, double mouseY) {
        if (mouseX < this.keyX || mouseX > this.keyX + this.keyW) return -1;
        int offset = (int) mouseY - this.rowsTop;
        if (offset < 0) return -1;
        int row = offset / ROW_STEP;
        if (row < 0 || row >= this.fields.size()) return -1;
        if (offset % ROW_STEP > ROW_H) return -1;
        return row;
    }

    private boolean overToggle(double mouseX, double mouseY) {
        if (this.pageCount <= 1) return false;
        return mouseX >= this.toggleX - 4 && mouseX <= this.toggleX + this.toggleW + 4
            && mouseY >= this.toggleY - 3 && mouseY <= this.toggleY + 11;
    }

    /** v0.185.0: pasar de pagina sin apuntar al rotulo. */
    private void turnPage(int delta) {
        if (this.pageCount <= 1) return;
        this.page = Math.floorMod(this.page + delta, this.pageCount);
        this.awaitingSlot = -1;
        this.rebuildWidgets();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.pageCount > 1) {
            this.turnPage(delta < 0 ? 1 : -1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.overToggle(mouseX, mouseY)) {
            this.turnPage(button == 1 ? -1 : 1);
            return true;
        }
        int row = this.rowAt(mouseX, mouseY);
        if (row >= 0) {
            int slot = this.rowSlots.get(row);
            if (button == 1) {
                DispatchLibrary.setKey(slot, DispatchLibrary.UNBOUND);
                this.saveIfNeeded(true);
                this.setStatus("gripeverde.dispatch.slot.cleared");
            } else if (button == 0) {
                this.awaitingSlot = slot;
                this.setStatus("gripeverde.dispatch.slot.listen");
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.awaitingSlot >= 0) {
            if (keyCode == 256) { // Esc: cancela
                this.awaitingSlot = -1;
                this.setStatus("");
                return true;
            }
            if (keyCode == 261) { // Supr: borra la tecla
                DispatchLibrary.setKey(this.awaitingSlot, DispatchLibrary.UNBOUND);
                this.awaitingSlot = -1;
                this.saveIfNeeded(true);
                this.setStatus("gripeverde.dispatch.slot.cleared");
                return true;
            }
            DispatchLibrary.setKey(this.awaitingSlot, keyCode);
            this.awaitingSlot = -1;
            this.saveIfNeeded(true);
            this.setStatus("gripeverde.dispatch.slot.bound");
            return true;
        }
        // v0.185.0: Ctrl+1..9/0 manda esa voz y Ctrl+Shift+1..9/0 las voces 11..20.
        // No es envio en lote: cada atajo manda una sola, igual que su tecla.
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0
                && keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            int digit = keyCode - GLFW.GLFW_KEY_0;
            int slot = digit == 0 ? 9 : digit - 1;
            if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) slot += 10;
            if (DispatchHotkeys.send(this.minecraft, slot)) {
                this.setStatus("gripeverde.dispatch.slot.sent");
            }
            return true;
        }
        if ((keyCode == 266 || keyCode == 267) && this.pageCount > 1) { // RePag / AvPag
            this.turnPage(keyCode == 266 ? -1 : 1);
            return true;
        }
        if (keyCode == 257 && !this.fields.isEmpty()) { // Enter: siguiente campo
            int current = this.fields.indexOf(this.getFocused());
            if (current >= 0) {
                this.setFocused(this.fields.get((current + 1) % this.fields.size()));
                return true;
            }
        }
        if (keyCode == 258 && !this.fields.isEmpty()) { // Tab
            int current = this.fields.indexOf(this.getFocused());
            int step = (modifiers & 1) != 0 ? -1 : 1;
            int next = current < 0 ? 0 : Math.floorMod(current + step, this.fields.size());
            this.setFocused(this.fields.get(next));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // -------------------------------------------------------------- dibujo

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        try {
            com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer
                .renderDiaryBackground(gfx, this.width, this.height);
        } catch (Throwable ignored) {
        }

        DiaryChrome.drawPanelShadow(gfx, this.panelX, this.panelY, this.panelW, this.panelH);
        PaperTexture.drawTornSheet(gfx, this.panelX, this.panelY, this.panelW, this.panelH,
            PaperTexture.PaperKind.OFFICIAL);
        DiaryChrome.drawCornerOrnaments(gfx, this.panelX, this.panelY, this.panelW, this.panelH);

        int titleY = this.panelY + (compactLayout() ? 10 : 14);
        int centerX = this.panelX + this.panelW / 2;
        String title = ellipsize(DiaryChrome.tr("gripeverde.dispatch.title"),
            Math.max(64, this.panelW - 60));
        gfx.drawString(this.font, title, centerX - DiaryChrome.widthCached(this.font, title) / 2, titleY,
            DiaryChrome.INK_TITLE, false);

        String subtitle = ellipsize(
            "- " + DiaryChrome.tr("gripeverde.dispatch.subtitle") + " -",
            Math.max(64, this.panelW - 40));
        gfx.drawString(this.font, subtitle, centerX - DiaryChrome.widthCached(this.font, subtitle) / 2, titleY + 12,
            DiaryChrome.INK_SUBTITLE, false);

        DiaryChrome.drawInkDivider(gfx, this.panelX + 18, titleY + 30, this.panelW - 36);

        // Numeros y teclas de cada renglon visible.
        this.hoveredSlot = this.rowAt(mouseX, mouseY) >= 0
            ? this.rowSlots.get(this.rowAt(mouseX, mouseY)) : -1;
        for (int i = 0; i < this.fields.size(); i++) {
            DiaryTextField field = this.fields.get(i);
            int slot = this.rowSlots.get(i);
            int y = field.getY();

            // v0.185.0: el numero dice de un vistazo si esa voz ya tiene mensaje.
            boolean hasText = !DispatchLibrary.message(slot).isBlank();
            String number = slot >= 0 && slot < SLOT_NUMBERS.length
                ? SLOT_NUMBERS[slot] : Integer.toString(slot + 1);
            gfx.drawString(this.font, number, this.fieldX - 6 - DiaryChrome.widthCached(this.font, number), y + 4,
                hasText ? DiaryChrome.INK_TITLE : 0x70887766, false);

            boolean listening = this.awaitingSlot == slot;
            boolean hover = this.hoveredSlot == slot;
            gfx.fill(this.keyX, y, this.keyX + this.keyW, y + ROW_H,
                hover || listening ? 0x38C89058 : 0x18FFFFFF);
            gfx.fill(this.keyX, y, this.keyX + this.keyW, y + 1,
                listening ? DiaryChrome.INK_TITLE : DiaryChrome.INK_DIVIDER);
            gfx.fill(this.keyX, y + ROW_H - 1, this.keyX + this.keyW, y + ROW_H,
                DiaryChrome.INK_DIVIDER);

            String key = listening ? "..." : keyLabel(slot);
            boolean bound = listening || DispatchLibrary.isBound(slot);
            String shown = ellipsize(key, this.keyW - 8);
            gfx.drawString(this.font, shown,
                this.keyX + (this.keyW - DiaryChrome.widthCached(this.font, shown)) / 2, y + 4,
                bound ? DiaryChrome.INK_TITLE : 0x99887766, false);
        }

        DiaryChrome.drawInkDivider(gfx, this.panelX + 18, this.footerDivY, this.panelW - 36);

        String note = this.footerNote();
        int noteRoom = Math.max(40, this.panelW - 40 - (this.pageCount > 1 ? this.toggleW + 10 : 0));
        gfx.drawString(this.font, ellipsize(note, noteRoom),
            this.panelX + 20, this.hintY, DiaryChrome.INK_SUBTITLE, false);
        String count = Component.translatable("gripeverde.dispatch.slot.count",
            DispatchLibrary.countWithText(), DispatchLibrary.SLOT_COUNT).getString()
            + "  \u00b7  Ctrl+1..0";
        gfx.drawString(this.font, ellipsize(count, Math.max(40, this.panelW - 40)),
            this.panelX + 20, this.countY, DiaryChrome.INK_FLAVOR, false);

        if (this.pageCount > 1) {
            int color = this.overToggle(mouseX, mouseY) ? DiaryChrome.INK_TITLE : DiaryChrome.INK_FLAVOR;
            gfx.drawString(this.font, this.pageLabel(), this.toggleX, this.toggleY, color, false);
        }

        super.render(gfx, mouseX, mouseY, partial);
    }

    private String footerNote() {
        if (!this.statusKey.isEmpty() && Util.getMillis() < this.statusUntil) {
            return DiaryChrome.tr(this.statusKey);
        }
        return DiaryChrome.tr("gripeverde.dispatch.slot.hint");
    }

    @Override
    public void renderBackground(GuiGraphics gfx) {
        // Sin el fondo de tierra del vanilla: la escena del diario ya se dibujo.
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.awaitingSlot = -1;
        this.saveIfNeeded(true);
        UiSounds.back();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private String ellipsize(String text, int maxWidth) {
        if (text == null) return "";
        if (this.font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        int room = Math.max(0, maxWidth - this.font.width(ellipsis));
        return this.font.plainSubstrByWidth(text, room) + ellipsis;
    }
}
