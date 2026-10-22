package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton.Icon;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton.Variant;
import com.eternalcraft.gripeverde.client.widgets.DiaryToggle;
import com.eternalcraft.gripeverde.config.ClientConfig;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class DiaryAccessibilityOptionsScreen extends AccessibilityOptionsScreen {

    // Panel deseado (se recorta si no cabe en pantalla)
    private static final int PANEL_MAX_W = 380;
    private static final int PANEL_MAX_H = 395;

    // Margen minimo entre panel y borde de pantalla
    private static final int SCREEN_MARGIN = 12;

    // Bandas dentro del panel
    private static final int HEADER_H = 60;   // titulo + subtitulo + flavor + divisor
    private static final int FOOTER_H = 112;  // divisor + 2 toggles propios + Volver

    private DiaryButton btnVolver;
    private PerceptionEyeButton perceptionEye;
    private DiaryToggle reducedMotionToggle;
    private DiaryToggle reduceFlashesToggle;
    private final SmoothScroll smoothScroll = new SmoothScroll();
    private int panelW, panelH, panelX, panelY;

    public DiaryAccessibilityOptionsScreen(Screen parent, Options options) {
        super(parent, options);
    }

    @Override
    protected void init() {
        String[] flavorPool = getAccFlavorPool();
        accFlavorIndex = ACC_RNG.nextInt(flavorPool.length);
        super.init();

        // Panel adaptativo
        this.panelW = Math.min(PANEL_MAX_W, this.width  - SCREEN_MARGIN * 2);
        this.panelH = Math.min(PANEL_MAX_H, this.height - SCREEN_MARGIN * 2);
        this.panelX = (this.width  - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        this.perceptionEye = new PerceptionEyeButton(
            this.panelX + 13, this.panelY + 9,
            Component.translatable("gripeverde.options.accessibility.eye")
        );
        this.perceptionEye.setTooltip(Tooltip.create(
            Component.translatable("gripeverde.options.accessibility.eye.tooltip")));
        this.addRenderableWidget(this.perceptionEye);

        // Suprimir fondo y barras de la lista vanilla
        if (this.list != null) {
            this.list.setRenderBackground(false);
            this.list.setRenderTopAndBottom(false);
            this.list.setRenderSelection(false);
            // NO cambiar width - R-VANILLA-LIST-COORDS
            int listTop    = this.panelY + HEADER_H;
            int listBottom = this.panelY + this.panelH - FOOTER_H;
            this.list.updateSize(this.width, this.height, listTop, listBottom);
            this.smoothScroll.snapToCurrent(this.list);
        }

        // Ocultar boton Done vanilla
        for (var child : this.children()) {
            if (child instanceof Button btn) {
                btn.visible = false;
                btn.active  = false;
                btn.setX(this.width + 200);
            }
        }

        // Opciones propias del mod: visibles junto a las opciones de accesibilidad vanilla.
        int toggleW = Math.min(300, this.panelW - 32);
        int toggleX = (this.width - toggleW) / 2;
        int toggleY = this.panelY + this.panelH - 100;
        this.reducedMotionToggle = new DiaryToggle(
            toggleX, toggleY, toggleW, 24,
            () -> ClientConfig.REDUCED_MOTION.get(),
            value -> {
                ClientConfig.REDUCED_MOTION.set(value);
                ClientConfig.REDUCED_MOTION.save();
            },
            Component.translatable("gripeverde.options.accessibility.reduced_motion"),
            value -> Component.translatable(value ? "options.on" : "options.off"),
            null
        );
        this.reducedMotionToggle.setTooltip(Tooltip.create(Component.translatable(
            "gripeverde.options.accessibility.reduced_motion.tooltip")));
        this.addRenderableWidget(this.reducedMotionToggle);

        // v0.23.0: ayuda de fotosensibilidad separada (relampagos y destellos de
        // pantalla completa, sin congelar el resto del movimiento).
        int flashesY = this.panelY + this.panelH - 66;
        this.reduceFlashesToggle = new DiaryToggle(
            toggleX, flashesY, toggleW, 24,
            () -> ClientConfig.REDUCE_FLASHES.get(),
            value -> {
                ClientConfig.REDUCE_FLASHES.set(value);
                ClientConfig.REDUCE_FLASHES.save();
            },
            Component.translatable("gripeverde.options.accessibility.reduce_flashes"),
            value -> Component.translatable(value ? "options.on" : "options.off"),
            null
        );
        this.reduceFlashesToggle.setTooltip(Tooltip.create(Component.translatable(
            "gripeverde.options.accessibility.reduce_flashes.tooltip")));
        this.addRenderableWidget(this.reduceFlashesToggle);

        // Boton Volver propio
        int btnW = 160;
        int btnH = 20;
        int btnX = (this.width - btnW) / 2;
        int btnY = this.panelY + this.panelH - 28;
        this.btnVolver = new DiaryButton(
            btnX, btnY, btnW, btnH,
            Component.translatable("gripeverde.options.button.done"),
            Icon.DOOR, Variant.PRIMARY,
            this::onClose
        );
        this.addRenderableWidget(this.btnVolver);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        // Escena viva
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(gfx, this.width, this.height);

        // Panel pergamino
        DiaryChrome.drawPanelShadow(gfx, this.panelX, this.panelY, this.panelW, this.panelH);
        PaperTexture.drawClean(gfx, this.panelX, this.panelY, this.panelW, this.panelH, PaperTexture.PaperKind.OFFICIAL);

        // Titulo
        int cx = this.width / 2;
        int titleY = this.panelY + 12;
        var _title = Component.translatable("options.accessibility.title");
        com.eternalcraft.gripeverde.client.render.DiaryTypography.drawCentered(
            gfx, this.font, _title, cx, titleY, DiaryChrome.INK_TITLE);

        // Subtitulo
        int subtitleY = titleY + 12;
        {
            String subC = "- " + Component.translatable("gripeverde.options.accessibility.subtitle").getString() + " -";
            int subW = this.font.width(subC);
            gfx.drawString(this.font, subC, cx - subW / 2, subtitleY, DiaryChrome.INK_SUBTITLE, false);
        }

        // Flavor
        int flavorY = subtitleY + 13;
        {
            Component flvC = Component.literal(getRotatingAccFlavor());
            int flvW = this.font.width(flvC);
            gfx.drawString(this.font, flvC, cx - flvW / 2, flavorY, DiaryChrome.INK_FLAVOR, false);
        }

        // Divisor superior
        int divY1 = flavorY + 12;
        DiaryChrome.drawInkDivider(gfx, this.panelX, divY1, this.panelW);

        // Lista vanilla sin gradientes blancos ni bandejas extra
        if (this.list != null) {
            this.list.render(gfx, mouseX, mouseY, delta);
            DiaryChrome.drawStyledScrollThumbInPanel(gfx, this.list, this.panelX + this.panelW);
        }

        // Cosmeticos
        int divY2 = this.panelY + this.panelH - FOOTER_H + 4;
        DiaryChrome.drawInkDivider(gfx, this.panelX, divY2, this.panelW);
        DiaryChrome.drawCornerOrnaments(gfx, this.panelX, this.panelY, this.panelW, this.panelH);
        DiaryChrome.drawRegistryTag(gfx, this.panelX, this.panelY, this.panelW, this.panelH, "REG. VI");

        // Controles del pie
        if (this.reducedMotionToggle != null) {
            this.reducedMotionToggle.render(gfx, mouseX, mouseY, delta);
        }
        if (this.reduceFlashesToggle != null) {
            this.reduceFlashesToggle.render(gfx, mouseX, mouseY, delta);
        }
        if (this.btnVolver != null) {
            this.btnVolver.render(gfx, mouseX, mouseY, delta);
        }

        // v0.16.0: el ojo de la percepcion, que mira a quien mira
        try {
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(gfx, this.width / 2, this.panelY - 12);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(gfx, this.width);
        } catch (Throwable ignored) {}
    }

    private final class PerceptionEyeButton extends AbstractButton {
        private PerceptionEyeButton(int x, int y, Component label) {
            super(x, y, 18, 14, label);
        }

        @Override
        public void onPress() {
            try {
                boolean first = com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                    com.eternalcraft.gripeverde.client.lore.Discoveries.EYE_FOLLOWS);
                com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                    com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_AMBIENT_OCCASIONAL.get(),
                    0.18f, first ? 0.62f : 0.80f);
            } catch (Throwable ignored) {}
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            // La nota secreta conserva su sonido propio.
        }

        @Override
        protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            int ex = this.getX() + this.width / 2;
            int ey = this.getY() + this.height / 2;
            boolean hovered = this.isHoveredOrFocused();
            int lid = hovered ? 0xFF3D2A18 : 0xFF654025;
            gfx.fill(ex - 6, ey - 1, ex + 6, ey + 1, 0xFFF0E5C8);
            gfx.fill(ex - 4, ey - 3, ex + 4, ey - 2, lid);
            gfx.fill(ex - 4, ey + 2, ex + 4, ey + 3, lid);
            gfx.fill(ex - 7, ey - 1, ex - 6, ey + 1, lid);
            gfx.fill(ex + 6, ey - 1, ex + 7, ey + 1, lid);
            boolean reduced = ClientConfig.REDUCED_MOTION.get();
            long now = System.currentTimeMillis();
            if (!reduced && (now % 6200L) < 180L) {
                gfx.fill(ex - 6, ey - 2, ex + 6, ey + 2, lid);
            } else {
                double dx = mouseX - ex, dy = mouseY - ey;
                int px = 0, py = 0;
                if (!reduced) {
                    if (Math.abs(dx) > 12) px = dx > 0 ? 2 : -2;
                    else if (Math.abs(dx) > 4) px = dx > 0 ? 1 : -1;
                    if (Math.abs(dy) > 12) py = dy > 0 ? 1 : -1;
                }
                gfx.fill(ex - 2 + px, ey - 2 + py, ex + 2 + px, ey + 2 + py, 0xFF8E6A35);
                gfx.fill(ex - 1 + px, ey - 1 + py, ex + 1 + px, ey + 1 + py, 0xFF1A0F05);
                gfx.fill(ex - 2 + px, ey - 2 + py, ex - 1 + px, ey - 1 + py, 0xFFD8CAB0);
            }
            if (this.isFocused() && !this.isHovered() && this.width > 2 && this.height > 2) {
                int focus = 0xFFFFE28A;
                gfx.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, focus);
                gfx.fill(this.getX(), this.getY() + this.height - 1,
                    this.getX() + this.width, this.getY() + this.height, focus);
                gfx.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, focus);
                gfx.fill(this.getX() + this.width - 1, this.getY(),
                    this.getX() + this.width, this.getY() + this.height, focus);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput out) {
            this.defaultButtonNarrationText(out);
        }
    }

    @Override
    public void tick() {
        super.tick();
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
        this.smoothScroll.tick(this.list);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.list != null && mouseY >= this.list.getTop() && mouseY <= this.list.getBottom()) {
            if (this.smoothScroll.wheel(this.list, delta, 34.0D)) {
                com.eternalcraft.gripeverde.client.audio.UiSounds.scroll();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    // Rotating flavor quotes for accessibility options
    private static final String[] ACC_FLAVOR_POOL_ES = {
        "Ajustad los sentidos con prudencia.",
        "No todos ven la ciudad con los mismos ojos.",
        "Algunos oidos ya no escuchan las campanas.",
        "La vista se cansa antes que la fiebre.",
        "Cada sentido cuenta cuando falta uno.",
        "Que nadie quede fuera de la cronica.",
        "Las manos tambien saben leer."
    };
    private static final String[] ACC_FLAVOR_POOL_EN = {
        "Attune the senses with prudence.",
        "Not all see the city with the same eyes.",
        "Some ears no longer hear the bells.",
        "The eyes tire before the fever does.",
        "Every sense counts when one is missing.",
        "Let no one be left outside the chronicle.",
        "The hands know how to read as well."
    };
    private static int accFlavorIndex = -1;
    private static final java.util.Random ACC_RNG = new java.util.Random();

    private static String getRotatingAccFlavor() {
        String[] pool = getAccFlavorPool();
        if (accFlavorIndex < 0 || accFlavorIndex >= pool.length) {
            accFlavorIndex = ACC_RNG.nextInt(pool.length);
        }
        return pool[accFlavorIndex];
    }

    private static String[] getAccFlavorPool() {
        String lang = net.minecraft.client.Minecraft.getInstance().getLanguageManager().getSelected();
        boolean english = lang == null || lang.startsWith("en_") || lang.equals("en_us");
        return english ? ACC_FLAVOR_POOL_EN : ACC_FLAVOR_POOL_ES;
    }

}
