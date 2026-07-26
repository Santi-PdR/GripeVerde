package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.VictorianSkyline;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.network.chat.Component;

/**
 * Version Diary de CreditsAndAttributionScreen (hub Creditos).
 *
 * Estrategia Grupo A v0.11.7: reimplementacion desde Screen.
 *
 * En 1.20.1 vanilla no expone LicenseScreen ni tiene una clase
 * "CreditsScreen" separada (solo WinScreen que hace la secuencia
 * completa). Por eso este hub solo ofrece:
 *   Creditos -> WinScreen (secuencia con musica y scroll)
 *   Volver
 *
 * Simple y honesto. Sin inventar pantallas que vanilla no expone
 * publicamente en esta version.
 */
public class DiaryCreditsScreen extends Screen {

    private static final int PANEL_W = 300;
    private static final int PANEL_H = 200;

    private final Screen lastScreen;
    private int panelX, panelY, panelW, panelH;

    public DiaryCreditsScreen(Screen parent) {
        super(Component.translatable("gripeverde.options.credits.title"));
        this.lastScreen = parent;
    }

    @Override
    protected void init() {
        this.panelW = Math.min(PANEL_W, this.width - 40);
        this.panelH = Math.min(PANEL_H, this.height - 30);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        int btnW = 200;
        int btnH = 20;
        int btnX = (this.width - btnW) / 2;
        int btnY0 = this.panelY + 80;

        // Creditos -> WinScreen (secuencia completa vanilla)
        this.addRenderableWidget(new DiaryButton(
            btnX, btnY0, btnW, btnH,
            Component.translatable("credits_and_attribution.button.credits"),
            DiaryButton.Icon.SCROLL,
            DiaryButton.Variant.SECONDARY,
            () -> this.minecraft.setScreen(new WinScreen(false, () -> this.minecraft.setScreen(this)))
        ));

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

    @Override
    public void tick() {
        super.tick();
        float delta = 1.0f / 20.0f;
        AtmosphereRenderer.tick(delta);
        VictorianSkyline.tick(delta);
        LivingAtmosphere.tick(delta);
        MenuMusic.tick();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Escena viva
        // Clear framebuffer + transition fade
        g.fill(0, 0, this.width, this.height, 0xFF000000);
        AtmosphereRenderer.renderSky(g, this.width, this.height);
        VictorianSkyline.renderBackground(g, this.width, this.height);
        LivingAtmosphere.renderFogLow(g, this.width, this.height);
        VictorianSkyline.renderForeground(g, this.width, this.height);
        LivingAtmosphere.renderStreetLevel(g, this.width, this.height);
        LivingAtmosphere.renderAir(g, this.width, this.height);

        // Overlay
        g.fill(0, 0, this.width, this.height, DiaryChrome.OVERLAY_COL);

        // Pergamino
        DiaryChrome.drawPanelShadow(g, this.panelX, this.panelY, this.panelW, this.panelH);
        PaperTexture.drawClean(g, this.panelX, this.panelY, this.panelW, this.panelH);

        // Titulo
        String titleStr = this.title.getString();
        int titleX = this.width / 2;
        int titleY = this.panelY + 10;
        {
        int _tw = this.font.width(titleStr);
        g.drawString(this.font, titleStr, titleX - _tw / 2 + 1, titleY + 1, DiaryChrome.SHADOW_COL, false);
        g.drawString(this.font, titleStr, titleX - _tw / 2,     titleY,     DiaryChrome.INK_TITLE,  false);
        }

        // Subtitulo
        String subtitle = "- " + Component.translatable("gripeverde.options.credits.subtitle").getString() + " -";
        int subWC = this.font.width(subtitle);
        g.drawString(this.font, subtitle, titleX - subWC / 2, titleY + 12, DiaryChrome.INK_SUBTITLE, false);

        // Flavor
        String flavor = Component.translatable("gripeverde.options.credits.flavor").getString();
        int flvWC = this.font.width(flavor);
        g.drawString(this.font, flavor, (this.width - flvWC) / 2, titleY + 24, DiaryChrome.INK_FLAVOR, false);

        // Divisores + esquineros
        int divY1 = titleY + 42;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY1);
        int divY2 = this.panelY + this.panelH - 38;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);
        DiaryChrome.drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);

        // Widgets
        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }
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