package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.ExpedientHeader;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryTextField;
import com.eternalcraft.gripeverde.client.widgets.DiaryToggle;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Version Diary de ControlsScreen (pantalla "Controles" hub).
 *
 * Estrategia Grupo A v0.11.7 (rediseno post-crash E88):
 * REIMPLEMENTACION desde Screen. NO subclasea ControlsScreen porque
 * los widgets vanilla tienen coordenadas hardcoded incompatibles con
 * nuestro panel pergamino.
 *
 * Contenido:
 *   - Sub-hub "Mouse..."          -> DiaryMouseSettingsScreen
 *   - Sub-hub "Atajos..."         -> DiaryKeyBindsScreen
 *   - Toggle "Agacharse"          -> Options.toggleCrouch()
 *   - Toggle "Correr"             -> Options.toggleSprint()
 *   - Toggle "Salto automatico"   -> Options.autoJump()
 *   - Toggle "Pestana admin"      -> Options.operatorItemsTab()
 *   - Boton Volver
 *
 * Los CycleButton se generan a partir de OptionInstance<Boolean> con
 * OptionInstance.createButton, respetando las traducciones vanilla.
 */
public class DiaryControlsScreen extends Screen {

    private static final int PANEL_W = 520;
    private static final int PANEL_H = 278;

    private final Screen lastScreen;
    private final Options options;
    private int panelX, panelY, panelW, panelH;
    private boolean smugglerSeen = false;
    private long openedAtMs = 0L;

    // v0.21.4: busqueda del manual (filtra los puntos de entrada y toggles)
    private final List<AbstractWidget> controlItems = new ArrayList<>();
    private DiaryTextField searchField = null;

    public DiaryControlsScreen(Screen parent, Options options) {
        super(Component.translatable("controls.title"));
        this.lastScreen = parent;
        this.options = options;
        // v0.16.0: tras tres visitas sin senal, el cronista deja la pista.
        try {
            int visits = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.bump("controlsVisits");
            if (visits >= 3 && !com.eternalcraft.gripeverde.client.lore.ChroniclerMemory
                    .isDiscovered(com.eternalcraft.gripeverde.client.lore.Discoveries.SMUGGLER_SIGNAL)) {
                com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                    com.eternalcraft.gripeverde.client.lore.Marginalia.N_SMUGGLER_HINT);
            }
        } catch (Throwable ignored) {}
    }

    @Override
    protected void init() {
        this.panelW = Math.min(PANEL_W, this.width - 40);
        this.panelH = Math.min(PANEL_H, this.height - 30);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        // Grid 2 columnas x 3 filas dentro del panel, con carril lateral de protocolo
        int gridTop = this.panelY + 62;
        int rowH = 24;
        int btnW = 150;
        int btnH = 20;
        int col1X = this.panelX + 22;
        int col2X = col1X + btnW + 10;

        // Fila 0: Mouse... | Atajos...
        DiaryButton mouseBtn = new DiaryButton(
            col1X, gridTop, btnW, btnH,
            Component.translatable("options.mouse_settings"),
            DiaryButton.Icon.NONE, DiaryButton.Variant.SECONDARY,
            () -> this.minecraft.setScreen(new net.minecraft.client.gui.screens.MouseSettingsScreen(this, this.options))
        );
        this.addRenderableWidget(mouseBtn);
        this.controlItems.add(mouseBtn);
        DiaryButton keyBindsBtn = new DiaryButton(
            col2X, gridTop, btnW, btnH,
            Component.translatable("controls.keybinds"),
            DiaryButton.Icon.NONE, DiaryButton.Variant.SECONDARY,
            () -> this.minecraft.setScreen(new net.minecraft.client.gui.screens.controls.KeyBindsScreen(this, this.options))
        );
        this.addRenderableWidget(keyBindsBtn);
        this.controlItems.add(keyBindsBtn);

        // Fila 1: toggleCrouch | toggleSprint
        addDiaryToggle(this.options.toggleCrouch(),
            Component.translatable("key.sneak"),
            v -> Component.translatable(v ? "options.key.toggle" : "options.key.hold"),
            col1X, gridTop + rowH, btnW, btnH);
        addDiaryToggle(this.options.toggleSprint(),
            Component.translatable("key.sprint"),
            v -> Component.translatable(v ? "options.key.toggle" : "options.key.hold"),
            col2X, gridTop + rowH, btnW, btnH);

        // Fila 2: autoJump | operatorItemsTab
        addDiaryToggle(this.options.autoJump(),
            Component.translatable("options.autoJump"),
            col1X, gridTop + rowH * 2, btnW, btnH);
        addDiaryToggle(this.options.operatorItemsTab(),
            Component.translatable("options.operatorItemsTab"),
            col2X, gridTop + rowH * 2, btnW, btnH);

        // v0.21.4: campo de busqueda del manual (filtra los 6 puntos de mando).
        // Se omite en paneles muy pequenos para no invadir el divisor.
        if (this.panelH >= 220) {
            int searchX = col1X;
            int searchW = (col2X + btnW) - searchX;
            int searchY = gridTop + rowH * 3 + 8;
            this.searchField = new DiaryTextField(this.font, searchX, searchY, searchW, 18,
                Component.literal("search"));
            this.searchField.setMaxLength(24);
            this.searchField.setResponder(q -> applyManualSearch(q));
            this.addRenderableWidget(this.searchField);
        }

        // Boton Volver Diary
        int backW = 120;
        int backY = this.panelY + this.panelH - 30;
        this.addRenderableWidget(new DiaryButton(
            (this.width - backW) / 2, backY, backW, btnH,
            Component.translatable("gripeverde.options.button.done"),
            DiaryButton.Icon.DOOR,
            DiaryButton.Variant.PRIMARY,
            () -> this.onClose()
        ));
        this.openedAtMs = System.currentTimeMillis();
    }

    /** Toggle Diary generico con etiqueta base + On/Off por defecto. */
    private void addDiaryToggle(OptionInstance<Boolean> opt, Component baseLabel,
                                int x, int y, int w, int h) {
        this.addRenderableWidget(new DiaryToggle(x, y, w, h, opt, baseLabel));
    }

    /** Toggle Diary con formatter custom para valores (ej: Hold/Toggle). */
    private void addDiaryToggle(OptionInstance<Boolean> opt, Component baseLabel,
                                java.util.function.Function<Boolean, Component> fmt,
                                int x, int y, int w, int h) {
        this.addRenderableWidget(new DiaryToggle(x, y, w, h, opt, baseLabel, fmt, null));
    }

    // v0.21.4: filtro del manual por nombre de entrada o mando.
    private void applyManualSearch(String query) {
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();
        for (AbstractWidget w : this.controlItems) {
            String label = w.getMessage().getString().toLowerCase(Locale.ROOT);
            w.visible = q.isEmpty() || label.contains(q);
        }
    }

    private boolean noManualMatches() {
        if (this.controlItems.isEmpty()) return false;
        boolean anyVisible = false;
        for (AbstractWidget w : this.controlItems) {
            if (w.visible) { anyVisible = true; break; }
        }
        return !anyVisible;
    }

    @Override
    public void tick() {
        super.tick();
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0xFF000000);
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);

        float reveal = DiaryChrome.reveal(this.openedAtMs, 240L);
        DiaryChrome.renderPanelBase(g, this.panelX, this.panelY, this.panelW, this.panelH, DiaryTheme.BRONZE, PaperTexture.PaperKind.TECHNICAL);

        ExpedientHeader.draw(g, this.font, this.panelX, this.panelY, this.panelW, this.title, reveal);






        int divY2 = this.panelY + this.panelH - 38;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);

        if (this.searchField != null) {
            boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
            if (this.searchField.getValue().isEmpty() && !this.searchField.isFocused()) {
                String hint = en ? "type to filter the manual" : "escriba para filtrar el manual";
                g.drawString(this.font, hint, this.searchField.getX() + 6, this.searchField.getY() + 5,
                    DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 170), false);
            }
            if (!this.searchField.getValue().isEmpty() && noManualMatches()) {
                String none = en ? "- no entry matches -" : "- ningun punto coincide -";
                int nx = this.searchField.getX() + (this.searchField.getWidth() - this.font.width(none)) / 2;
                g.drawString(this.font, none, nx, this.searchField.getY() + 24,
                    DiaryTheme.withAlpha(DiaryTheme.INK, 130), false);
            }
        }

        if (reveal < 0.995f) {
            int revealBottom = this.panelY + Math.max(28, (int)(this.panelH * reveal));
            g.enableScissor(this.panelX - 2, this.panelY - 2, this.panelX + this.panelW + 2, revealBottom);
            for (Renderable r : this.renderables) {
                r.render(g, mouseX, mouseY, partialTick);
            }
            g.disableScissor();
        } else {
            for (Renderable r : this.renderables) {
                r.render(g, mouseX, mouseY, partialTick);
            }
        }

        try {
            if (!smugglerSeen
                    && this.options.toggleCrouch().get()
                    && this.options.toggleSprint().get()
                    && !this.options.autoJump().get()
                    && !this.options.operatorItemsTab().get()) {
                smugglerSeen = true;
                boolean first = com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                    com.eternalcraft.gripeverde.client.lore.Discoveries.SMUGGLER_SIGNAL);
                if (first) {
                    com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.addCollectible(
                        com.eternalcraft.gripeverde.client.lore.Discoveries.ITEM_NOTE);
                    com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                        com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_DOOR.get(), 0.18f, 0.62f);
                }
            }
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, this.panelY - 12);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {}
    }

    private String buildControlSummaryTitle() {
        boolean crouch = this.options.toggleCrouch().get();
        boolean sprint = this.options.toggleSprint().get();
        if (com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()) {
            if (crouch && sprint) return "toggle posture engaged";
            if (crouch || sprint) return "mixed field habits";
            return "hands remain direct";
        }
        if (crouch && sprint) return "postura alternada activa";
        if (crouch || sprint) return "habitos de campo mixtos";
        return "las manos siguen directas";
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        // Bypass
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }
}
