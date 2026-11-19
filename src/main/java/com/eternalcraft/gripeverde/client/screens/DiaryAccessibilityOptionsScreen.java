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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
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
    private DiaryToggle reducedMotionToggle;
    private DiaryToggle reduceFlashesToggle;
    private final SmoothScroll smoothScroll = new SmoothScroll();
    private int panelW, panelH, panelX, panelY;

    public DiaryAccessibilityOptionsScreen(Screen parent, Options options) {
        super(parent, options);
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
        String[] flavorPool = getAccFlavorPool();
        accFlavorIndex = ACC_RNG.nextInt(flavorPool.length);
        super.init();

        // Panel adaptativo
        this.panelW = Math.min(PANEL_MAX_W, this.width  - SCREEN_MARGIN * 2);
        this.panelH = Math.min(PANEL_MAX_H, this.height - SCREEN_MARGIN * 2);
        this.panelX = (this.width  - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

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

        // v0.23.0: ayuda de fotosensibilidad separada (solo destellos/relampagos,
        // sin congelar movimiento). WCAG 2.3.1 recomienda control especifico.
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
        PaperTexture.drawTornSheet(gfx, this.panelX, this.panelY, this.panelW, this.panelH, PaperTexture.PaperKind.OFFICIAL);

        // Titulo
        int cx = this.width / 2;
        int titleY = this.panelY + 12;
        {
        String _title = DiaryChrome.fitCached(this.font,
            DiaryChrome.tr("options.accessibility.title"), Math.max(0, this.panelW - 32));
        int _tw = DiaryChrome.widthCached(this.font, _title);
        gfx.drawString(this.font, _title, cx - _tw / 2,     titleY,     DiaryChrome.INK_TITLE,  false);
        }

        // Subtitulo
        int subtitleY = titleY + 12;
        {
            String subC = "- " + DiaryChrome.tr("gripeverde.options.accessibility.subtitle") + " -";
            subC = DiaryChrome.fitCached(this.font, subC, Math.max(0, this.panelW - 32));
            int subW = DiaryChrome.widthCached(this.font, subC);
            gfx.drawString(this.font, subC, cx - subW / 2, subtitleY, DiaryChrome.INK_SUBTITLE, false);
        }

        // Flavor
        int flavorY = subtitleY + 13;
        {
            String flvC = DiaryChrome.fitCached(this.font, getRotatingAccFlavor(), Math.max(0, this.panelW - 32));
            int flvW = DiaryChrome.widthCached(this.font, flvC);
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

        // v0.282.0: cuantos ajustes tiene la hoja y como se sale, en el margen.
        try {
            boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
            int count = this.list == null ? 0 : this.list.children().size();
            String res = (en ? count + " settings" : count + " ajustes")
                + (en ? "  ·  Esc goes back" : "  ·  Esc vuelve");
            // v0.301.0: la linea se dibujaba justo debajo de la chapa REG. VI y
            // se leian las dos encimadas. Ahora va al margen izquierdo.
            String cut = DiaryChrome.fitCached(this.font, res, Math.max(60, this.panelW - 120));
            gfx.drawString(this.font, cut, this.panelX + 14,
                this.panelY + 8, DiaryChrome.INK_FLAVOR, false);
        } catch (Throwable ignored) {
        }

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
            drawPerceptionEye(gfx, mouseX, mouseY);
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(gfx, this.width / 2, this.panelY - 12);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(gfx, this.width);
        } catch (Throwable ignored) {}
    }

    private int eyeX() { return this.panelX + 22; }
    private int eyeY() { return this.panelY + 16; }

    private void drawPerceptionEye(GuiGraphics gfx, int mouseX, int mouseY) {
        int ex = eyeX(), ey = eyeY();
        boolean hover = mouseX >= ex - 8 && mouseX <= ex + 8 && mouseY >= ey - 6 && mouseY <= ey + 6;
        // Contorno almendrado
        int lid = hover ? 0xFF3D2A18 : 0xFF654025;
        gfx.fill(ex - 6, ey - 1, ex + 6, ey + 1, 0xFFF0E5C8);
        gfx.fill(ex - 4, ey - 3, ex + 4, ey - 2, lid);
        gfx.fill(ex - 4, ey + 2, ex + 4, ey + 3, lid);
        gfx.fill(ex - 7, ey - 1, ex - 6, ey + 1, lid);
        gfx.fill(ex + 6, ey - 1, ex + 7, ey + 1, lid);
        boolean reduced = ClientConfig.REDUCED_MOTION.get()
            || com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.hasActiveWorld();
        // Parpadeo ocasional, desactivado cuando el lector reduce el movimiento.
        long now = System.currentTimeMillis();
        boolean blink = !reduced && (now % 6200L) < 180L;
        if (blink) {
            gfx.fill(ex - 6, ey - 2, ex + 6, ey + 2, lid);
            return;
        }
        // Iris que sigue al lector
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= eyeX() - 8 && mouseX <= eyeX() + 8
                && mouseY >= eyeY() - 6 && mouseY <= eyeY() + 6) {
            try {
                boolean first = com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                    com.eternalcraft.gripeverde.client.lore.Discoveries.EYE_FOLLOWS);
                com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                    com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_AMBIENT_OCCASIONAL.get(),
                    0.18f, first ? 0.62f : 0.80f);
            } catch (Throwable ignored) {}
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
        boolean english = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        return english ? ACC_FLAVOR_POOL_EN : ACC_FLAVOR_POOL_ES;
    }


    @Override
    public void onClose() {
        // v0.264.0: salir de una hoja suena a puerta, como el resto de la casa.
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.back();
        } catch (Throwable ignored) {
        }
        super.onClose();
    }
}
