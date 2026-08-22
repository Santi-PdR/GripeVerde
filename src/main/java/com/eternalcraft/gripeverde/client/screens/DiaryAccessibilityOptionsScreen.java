package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.ExpedientHeader;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton.Icon;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton.Variant;
import com.eternalcraft.gripeverde.client.widgets.DiaryProgressBar;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DiaryAccessibilityOptionsScreen extends AccessibilityOptionsScreen {

    // Panel deseado (se recorta si no cabe en pantalla)
    private static final int PANEL_MAX_W = 530;
    private static final int PANEL_MAX_H = 395;
    private static final int RAIL_W = 154;

    // Margen minimo entre panel y borde de pantalla
    private static final int SCREEN_MARGIN = 12;

    // Bandas dentro del panel
    private static final int HEADER_H = 60;   // titulo + subtitulo + flavor + divisor
    private static final int FOOTER_H = 45;   // divisor + boton Volver

    private DiaryButton btnVolver;
    private final SmoothScroll smoothScroll = new SmoothScroll();
    private int panelW, panelH, panelX, panelY;
    private long openedAtMs = 0L;

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
        this.openedAtMs = System.currentTimeMillis();
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(gfx, this.width, this.height);

        float reveal = DiaryChrome.reveal(this.openedAtMs, 240L);
        DiaryChrome.renderPanelBase(gfx, this.panelX, this.panelY, this.panelW, this.panelH, DiaryTheme.PLAGUE_SICK, PaperTexture.PaperKind.OFFICIAL);

        int headerBottom = ExpedientHeader.draw(gfx, this.font, this.panelX, this.panelY, this.panelW, Component.translatable("options.accessibility.title"), reveal);

        int railX = this.panelX + this.panelW - RAIL_W - 18;
        DiaryChrome.drawInsetCard(gfx, this.font, railX, headerBottom, RAIL_W, 84,
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "PERCEPTION" : "PERCEPCION",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Observation profile" : "Perfil de observacion",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Assistive options are kept apart from the main list so their purpose is immediately legible." : "Las ayudas quedan separadas del cuerpo principal para que su proposito se lea al instante.",
            DiaryTheme.PLAGUE_SICK);
        DiaryChrome.drawInsetCard(gfx, this.font, railX, headerBottom + 92, RAIL_W, 86,
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "READABILITY" : "LEGIBILIDAD",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Clear states" : "Estados claros",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Hover, scroll and focus are isolated visually without changing any vanilla value." : "Hover, scroll y foco se separan visualmente sin alterar ningun valor vanilla.",
            DiaryTheme.BRONZE);
        int listRight = railX - 8;
        DiaryChrome.drawInnerRail(gfx, this.panelX + 18, headerBottom - 2, listRight - (this.panelX + 18), this.panelH - 138, DiaryTheme.BRONZE);
        if (this.list != null) {
            if (reveal < 0.995f) {
                int revealBottom = this.panelY + Math.max(28, (int)(this.panelH * reveal));
                gfx.enableScissor(this.panelX - 2, this.panelY - 2, this.panelX + this.panelW + 2, revealBottom);
                this.list.render(gfx, mouseX, mouseY, delta);
                DiaryChrome.drawStyledScrollThumbInPanel(gfx, this.list, listRight);
                gfx.disableScissor();
            } else {
                this.list.render(gfx, mouseX, mouseY, delta);
                DiaryChrome.drawStyledScrollThumbInPanel(gfx, this.list, listRight);
            }
        }

        int divY2 = this.panelY + this.panelH - 38;
        DiaryChrome.drawInkDivider(gfx, this.panelX, divY2, this.panelW);
        if (this.btnVolver != null) {
            this.btnVolver.render(gfx, mouseX, mouseY, delta);
        }

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
        // Parpadeo ocasional
        long now = System.currentTimeMillis();
        boolean blink = (now % 6200L) < 180L;
        if (blink) {
            gfx.fill(ex - 6, ey - 2, ex + 6, ey + 2, lid);
            return;
        }
        // Iris que sigue al lector
        double dx = mouseX - ex, dy = mouseY - ey;
        int px = 0, py = 0;
        if (Math.abs(dx) > 12) px = dx > 0 ? 2 : -2;
        else if (Math.abs(dx) > 4) px = dx > 0 ? 1 : -1;
        if (Math.abs(dy) > 12) py = dy > 0 ? 1 : -1;
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
        String lang = net.minecraft.client.Minecraft.getInstance().getLanguageManager().getSelected();
        boolean english = lang == null || lang.startsWith("en_") || lang.equals("en_us");
        return english ? ACC_FLAVOR_POOL_EN : ACC_FLAVOR_POOL_ES;
    }

}
