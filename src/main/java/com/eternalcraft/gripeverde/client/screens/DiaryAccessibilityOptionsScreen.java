package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.VictorianSkyline;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton.Icon;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton.Variant;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DiaryAccessibilityOptionsScreen extends AccessibilityOptionsScreen {

    // Panel deseado (se recorta si no cabe en pantalla)
    private static final int PANEL_MAX_W = 340;
    private static final int PANEL_MAX_H = 395;

    // Margen minimo entre panel y borde de pantalla
    private static final int SCREEN_MARGIN = 12;

    // Bandas dentro del panel
    private static final int HEADER_H = 60;   // titulo + subtitulo + flavor + divisor
    private static final int FOOTER_H = 45;   // divisor + boton Volver

    private DiaryButton btnVolver;
    private int panelW, panelH, panelX, panelY;

    public DiaryAccessibilityOptionsScreen(Screen parent, Options options) {
        super(parent, options);
    }

    @Override
    protected void init() {
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
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        // Escena viva
        AtmosphereRenderer.renderSky(gfx, this.width, this.height);
        VictorianSkyline.renderBackground(gfx, this.width, this.height);
        LivingAtmosphere.renderFogLow(gfx, this.width, this.height);
        VictorianSkyline.renderForeground(gfx, this.width, this.height);
        LivingAtmosphere.renderStreetLevel(gfx, this.width, this.height);
        LivingAtmosphere.renderAir(gfx, this.width, this.height);

        // Overlay
        gfx.fill(0, 0, this.width, this.height, DiaryChrome.OVERLAY_COL);

        // Panel pergamino
        PaperTexture.drawClean(gfx, this.panelX, this.panelY, this.panelW, this.panelH);

        // Titulo
        int cx = this.width / 2;
        int titleY = this.panelY + 12;
        gfx.drawCenteredString(this.font,
            Component.translatable("options.accessibility.title"),
            cx + 1, titleY + 1, DiaryChrome.SHADOW_COL);
        gfx.drawCenteredString(this.font,
            Component.translatable("options.accessibility.title"),
            cx, titleY, DiaryChrome.INK_TITLE);

        // Subtitulo
        int subtitleY = titleY + 12;
        {
            Component subC = Component.translatable("gripeverde.options.accessibility.subtitle");
            int subW = this.font.width(subC);
            gfx.drawString(this.font, subC, cx - subW / 2, subtitleY, DiaryChrome.INK_SUBTITLE, false);
        }

        // Flavor
        int flavorY = subtitleY + 13;
        {
            Component flvC = Component.translatable("gripeverde.options.accessibility.flavor");
            int flvW = this.font.width(flvC);
            gfx.drawString(this.font, flvC, cx - flvW / 2, flavorY, DiaryChrome.INK_FLAVOR, false);
        }

        // Divisor superior
        int divY1 = flavorY + 12;
        DiaryChrome.drawInkDivider(gfx, this.panelX, divY1, this.panelW);

        // Lista vanilla (scroll queda vanilla, deuda tecnica documentada)
        if (this.list != null) {
            this.list.render(gfx, mouseX, mouseY, delta);
        }

        // Cosmeticos
        int divY2 = this.panelY + this.panelH - 38;
        DiaryChrome.drawInkDivider(gfx, this.panelX, divY2, this.panelW);
        DiaryChrome.drawCornerOrnaments(gfx, this.panelX, this.panelY, this.panelW, this.panelH);

        // Boton Volver
        if (this.btnVolver != null) {
            this.btnVolver.render(gfx, mouseX, mouseY, delta);
        }
    }

    @Override
    public void tick() {
        super.tick();
        float delta = 1.0f / 20.0f;
        AtmosphereRenderer.tick(delta);
        VictorianSkyline.tick(delta);
        LivingAtmosphere.tick(delta);
    }
}