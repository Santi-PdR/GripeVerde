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

    // v0.272.0: la hoja solo suena la primera vez que se abre, no al redimensionar.
    private boolean paperSoundDone = false;
    // v0.273.0: cuantas teclas comparten dos acciones y cuantas no tienen ninguna.
    private int conflictCount = 0;
    private int unassignedCount = 0;
    private String conflictSample = "";
    private int keyAuditTicks = 0;

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
        this.panelW = Math.min(PANEL_W, this.width - 40);
        this.panelH = Math.min(PANEL_H, this.height - 30);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        // Grid 2 columnas x 3 filas dentro del panel
        int gridTop = this.panelY + 50;
        int rowH = 24;
        // v0.264.0: 150 fijos se salian del panel en ventanas angostas; ahora
        // la rejilla se mide contra el panel y nunca lo desborda.
        int btnW = Math.max(96, Math.min(150, (this.panelW - 40) / 2));
        int btnH = 20;
        int col1X = this.panelX + (this.panelW / 2) - btnW - 4;
        int col2X = this.panelX + (this.panelW / 2) + 4;

        // Fila 0: Mouse... | Atajos...
        DiaryButton mouseHub = new DiaryButton(
            col1X, gridTop, btnW, btnH,
            Component.translatable("options.mouse_settings"),
            DiaryButton.Icon.NONE, DiaryButton.Variant.SECONDARY,
            () -> this.minecraft.setScreen(new net.minecraft.client.gui.screens.MouseSettingsScreen(this, this.options))
        );
        // v0.264.0: las dos hojas de dentro se anuncian antes de entrar.
        mouseHub.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.literal(com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                ? "sensitivity, invert mouse, raw input"
                : "sensibilidad, invertir el raton, entrada directa")));
        this.addRenderableWidget(mouseHub);

        DiaryButton keysHub = new DiaryButton(
            col2X, gridTop, btnW, btnH,
            Component.translatable("controls.keybinds"),
            DiaryButton.Icon.NONE, DiaryButton.Variant.SECONDARY,
            () -> this.minecraft.setScreen(new net.minecraft.client.gui.screens.controls.KeyBindsScreen(this, this.options))
        );
        keysHub.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.literal(com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                ? "every key of the game, reassignable"
                : "todas las teclas del juego, reasignables")));
        this.addRenderableWidget(keysHub);

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

    /** v0.273.0: cuenta teclas repetidas y teclas sin asignar, veinte veces por segundo. */
    private void auditKeys() {
        java.util.HashMap<String, Integer> seen = new java.util.HashMap<>();
        int unassigned = 0;
        String sample = "";
        try {
            for (net.minecraft.client.KeyMapping mapping : this.options.keyMappings) {
                if (mapping == null) continue;
                String name = mapping.getKey().getName();
                if (name == null || "key.keyboard.unknown".equals(name)) {
                    unassigned++;
                    continue;
                }
                if (name.startsWith("key.mouse")) continue;   // dos botones del raton no chocan
                int n = seen.merge(name, 1, Integer::sum);
                if (n == 2 && sample.length() < 28) {
                    String pretty = DiaryChrome.tr(name);
                    if (sample.isEmpty()) sample = pretty;
                }
            }
        } catch (Throwable ignored) {
            return;
        }
        int conflicts = 0;
        for (int n : seen.values()) if (n > 1) conflicts += n - 1;
        this.conflictCount = conflicts;
        this.unassignedCount = unassigned;
        this.conflictSample = sample;
    }

    @Override
    public void tick() {
        if (--this.keyAuditTicks <= 0) {
            this.keyAuditTicks = 20;
            auditKeys();
        }
        super.tick();
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
                // Overlay

        // Pergamino
        DiaryChrome.drawPanelShadow(g, this.panelX, this.panelY, this.panelW, this.panelH);
        PaperTexture.drawTornSheet(g, this.panelX, this.panelY, this.panelW, this.panelH, PaperTexture.PaperKind.TECHNICAL);

        // Titulo
        String titleStr = DiaryChrome.fitCached(this.font, this.title.getString(), Math.max(0, this.panelW - 28));
        int titleX = this.width / 2;
        int titleY = this.panelY + 10;
        {
        int _tw = DiaryChrome.widthCached(this.font, titleStr);
        g.drawString(this.font, titleStr, titleX - _tw / 2,     titleY,     DiaryChrome.INK_TITLE,  false);
        }

        // Subtitulo
        String subtitle = "- " + DiaryChrome.tr("gripeverde.options.controls.subtitle") + " -";
        subtitle = DiaryChrome.fitCached(this.font, subtitle, Math.max(0, this.panelW - 28));
        int subWC = DiaryChrome.widthCached(this.font, subtitle);
        g.drawString(this.font, subtitle, titleX - subWC / 2, titleY + 12, DiaryChrome.INK_SUBTITLE, false);

        // Flavor
        String flavor = DiaryChrome.fitCached(this.font,
            DiaryChrome.tr("gripeverde.options.controls.flavor"), Math.max(0, this.panelW - 28));
        int flvWC = DiaryChrome.widthCached(this.font, flavor);
        g.drawString(this.font, flavor, (this.width - flvWC) / 2, titleY + 24, DiaryChrome.INK_FLAVOR, false);

        // Divisores + esquineros
        int divY1 = titleY + 42;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY1);
        int divY2 = this.panelY + this.panelH - 38;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);
        DiaryChrome.drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);
        DiaryChrome.drawRegistryTag(g, this.panelX, this.panelY, this.panelW, this.panelH, "REG. III");

        // v0.273.0: el estado de las teclas, en el margen izquierdo de la hoja.
        try {
            boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
            String line1 = this.conflictCount <= 0
                ? (en ? "no clashes" : "sin choques")
                : (en ? this.conflictCount + " clashes" : this.conflictCount + " choques");
            int col1 = this.conflictCount <= 0 ? DiaryChrome.INK_FLAVOR : 0xFF8E2A20;
            int x1 = this.panelX + 12;
            int y1 = this.panelY + 8;
            int limit = Math.max(40, this.width / 2 - 60);
            String fit1 = DiaryChrome.fitCached(this.font, line1, limit);
            g.drawString(this.font, fit1, x1, y1, col1, false);
            boolean overBadge = mouseX >= x1 - 2 && mouseX <= x1 + DiaryChrome.widthCached(this.font, fit1) + 2
                && mouseY >= y1 - 2 && mouseY <= y1 + 12;
            if (overBadge && this.conflictCount > 0 && !this.conflictSample.isEmpty()) {
                g.drawString(this.font, this.conflictSample, x1, y1 + 20, 0xFF5C0A08, false);
            }
            if (this.unassignedCount > 0) {
                String line2 = en ? this.unassignedCount + " unbound"
                    : this.unassignedCount + " sin tecla";
                String fit2 = DiaryChrome.fitCached(this.font, line2, limit);
                g.drawString(this.font, fit2, x1, y1 + 10, 0xFF8E2A20, false);
            }
        } catch (Throwable ignored) {
        }

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

    /** v0.273.0: el aviso de choques lleva a la lista de teclas. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.panelW >= 220) {
            int x1 = this.panelX + 12;
            int y1 = this.panelY + 8;
            if (mouseX >= x1 - 2 && mouseX <= x1 + Math.max(70, this.width / 2 - 60)
                    && mouseY >= y1 - 2 && mouseY <= y1 + 20) {
                try {
                    com.eternalcraft.gripeverde.client.audio.UiSounds.tab();
                } catch (Throwable ignored) {
                }
                this.minecraft.setScreen(
                    new net.minecraft.client.gui.screens.controls.KeyBindsScreen(this, this.options));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        // v0.264.0: salir de una hoja suena a puerta, como el resto de la casa.
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.back();
        } catch (Throwable ignored) {
        }
        this.minecraft.setScreen(this.lastScreen);
    }
}
