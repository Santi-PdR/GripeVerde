package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.VictorianSkyline;
import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.network.chat.Component;

/**
 * DiaryOnlineOptionsScreen: version ambientada de OnlineOptionsScreen.
 * Como el constructor de vanilla es privado, extendemos SimpleOptionsSubScreen
 * directamente con las 2 opciones basicas de Realms (notificaciones + suscripcion).
 */
public class DiaryOnlineOptionsScreen extends SimpleOptionsSubScreen {

    private static final int PANEL_W = 340;
    private static final int PANEL_H = 240;

    private final SmoothScroll smoothScroll = new SmoothScroll(0.50D, 0.24D);
    private int panelX, panelY, panelW, panelH;

    public DiaryOnlineOptionsScreen(Screen parent, Options options) {
        super(parent,
              options,
              Component.translatable("options.online.title"),
              new OptionInstance<?>[] {
                  options.realmsNotifications(),
                  options.allowServerListing()
              });
    }

    @Override
    protected void init() {
        super.init();
        com.eternalcraft.gripeverde.client.render.PaperTexture.incrementOpenCount();

        this.panelW = Math.min(PANEL_W, this.width - 40);
        this.panelH = Math.min(PANEL_H, this.height - 30);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        if (this.list != null) {
            this.list.setRenderBackground(false);
            this.list.setRenderTopAndBottom(false);
            int listTop = this.panelY + 55;
            int listBottom = this.panelY + this.panelH - 45;
            this.list.updateSize(this.width, this.height, listTop, listBottom);
            this.smoothScroll.snapToCurrent(this.list);
        }

        // Ocultar el boton Done vanilla
        for (GuiEventListener child : this.children()) {
            if (child instanceof Button b) {
                String label = b.getMessage().getString();
                if (label.equalsIgnoreCase("Done") || label.equalsIgnoreCase("Aceptar")
                    || label.equalsIgnoreCase("Listo") || label.equalsIgnoreCase("OK")) {
                    b.visible = false;
                    b.active = false;
                    b.setX(-10000);
                }
            }
        }

        // Boton Volver Diary
        int btnW = 130;
        int btnH = 22;
        int btnX = this.width / 2 - btnW / 2;
        int btnY = this.panelY + this.panelH - btnH - 12;
        this.addRenderableWidget(new DiaryButton(btnX, btnY, btnW, btnH,
            Component.translatable("gripeverde.options.button.done"),
            DiaryButton.Icon.DOOR, DiaryButton.Variant.PRIMARY,
            () -> this.onClose()));
    }

    @Override
    public void tick() {
        super.tick();
        float delta = 1.0f / 20.0f;
        AtmosphereRenderer.tick(delta);
        VictorianSkyline.tick(delta);
        LivingAtmosphere.tick(delta);
        MenuMusic.tick();
        com.eternalcraft.gripeverde.client.audio.MenuAmbience.tick();
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

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Escena viva
        g.fill(0, 0, this.width, this.height, 0xFF000000);
        AtmosphereRenderer.renderSky(g, this.width, this.height);
        VictorianSkyline.renderBackground(g, this.width, this.height);
        VictorianSkyline.renderForeground(g, this.width, this.height);
        LivingAtmosphere.renderStreet(g, this.width, this.height);
                // Overlay
        g.fill(0, 0, this.width, this.height, DiaryChrome.OVERLAY_COL);

        // Pergamino
        DiaryChrome.drawPanelShadow(g, this.panelX, this.panelY, this.panelW, this.panelH);
        PaperTexture.drawClean(g, this.panelX, this.panelY, this.panelW, this.panelH);

        // Titulo
        String titleStr = this.title.getString();
        int titleX = this.width / 2;
        int titleY = this.panelY + 12;
        int tw = this.font.width(titleStr);
        g.drawString(this.font, titleStr, titleX - tw / 2, titleY, DiaryChrome.INK_TITLE, false);

        // Subtitulo diegetico
        String subtitle = "- " + Component.translatable("gripeverde.options.online.subtitle").getString() + " -";
        int subW = this.font.width(subtitle);
        g.drawString(this.font, subtitle, titleX - subW / 2, titleY + 14, DiaryChrome.INK_SUBTITLE, false);
        String flavor = Component.translatable("gripeverde.options.online.flavor").getString();
        int flavorW = this.font.width(flavor);
        g.drawString(this.font, flavor, titleX - flavorW / 2, titleY + 26, DiaryChrome.INK_FLAVOR, false);

        // Divisor
        int divY = titleY + 40;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY);

        // Divisor inferior + esquineros
        int divY2 = this.panelY + this.panelH - 40;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);
        DiaryChrome.drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);

        // Lista vanilla
        if (this.list != null) {
            this.list.render(g, mouseX, mouseY, partialTick);
        }

        // Widgets propios (boton Volver)
        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }
    }
}
