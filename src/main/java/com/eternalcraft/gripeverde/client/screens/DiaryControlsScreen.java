package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryToggle;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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

    private static final int PANEL_W = 340;
    private static final int PANEL_H = 260;

    private final Screen lastScreen;
    private final Options options;
    private int panelX, panelY, panelW, panelH;
    private boolean smugglerSeen = false;

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

        // Grid 2 columnas x 3 filas dentro del panel
        int gridTop = this.panelY + 50;
        int rowH = 24;
        int btnW = 150;
        int btnH = 20;
        int col1X = this.panelX + (this.panelW / 2) - btnW - 4;
        int col2X = this.panelX + (this.panelW / 2) + 4;

        // Fila 0: Mouse... | Atajos...
        this.addRenderableWidget(new DiaryButton(
            col1X, gridTop, btnW, btnH,
            Component.translatable("options.mouse_settings"),
            DiaryButton.Icon.NONE, DiaryButton.Variant.SECONDARY,
            () -> this.minecraft.setScreen(new net.minecraft.client.gui.screens.MouseSettingsScreen(this, this.options))
        ));
        this.addRenderableWidget(new DiaryButton(
            col2X, gridTop, btnW, btnH,
            Component.translatable("controls.keybinds"),
            DiaryButton.Icon.NONE, DiaryButton.Variant.SECONDARY,
            () -> this.minecraft.setScreen(new net.minecraft.client.gui.screens.controls.KeyBindsScreen(this, this.options))
        ));

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

    @Override
    public void tick() {
        super.tick();
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
                // Overlay

        // Pergamino
        DiaryChrome.drawPanelShadow(g, this.panelX, this.panelY, this.panelW, this.panelH);
        PaperTexture.drawClean(g, this.panelX, this.panelY, this.panelW, this.panelH, PaperTexture.PaperKind.TECHNICAL);

        // Titulo
        String titleStr = DiaryChrome.fitText(this.font, this.title.getString(), Math.max(0, this.panelW - 28));
        int titleX = this.width / 2;
        int titleY = this.panelY + 10;
        {
        int _tw = this.font.width(titleStr);
        g.drawString(this.font, titleStr, titleX - _tw / 2,     titleY,     DiaryChrome.INK_TITLE,  false);
        }

        // Subtitulo
        String subtitle = "- " + Component.translatable("gripeverde.options.controls.subtitle").getString() + " -";
        subtitle = DiaryChrome.fitText(this.font, subtitle, Math.max(0, this.panelW - 28));
        int subWC = this.font.width(subtitle);
        g.drawString(this.font, subtitle, titleX - subWC / 2, titleY + 12, DiaryChrome.INK_SUBTITLE, false);

        // Flavor
        String flavor = DiaryChrome.fitText(this.font,
            Component.translatable("gripeverde.options.controls.flavor").getString(), Math.max(0, this.panelW - 28));
        int flvWC = this.font.width(flavor);
        g.drawString(this.font, flavor, (this.width - flvWC) / 2, titleY + 24, DiaryChrome.INK_FLAVOR, false);

        // Divisores + esquineros
        int divY1 = titleY + 42;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY1);
        int divY2 = this.panelY + this.panelH - 38;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);
        DiaryChrome.drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);
        DiaryChrome.drawRegistryTag(g, this.panelX, this.panelY, this.panelW, this.panelH, "REG. III");

        // Widgets
        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }

        // v0.16.0: la senal del contrabandista.
        // Agacharse y correr en "alternar"; el resto, quieto.
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

    @Override
    public void renderBackground(GuiGraphics g) {
        // Bypass
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }
}
