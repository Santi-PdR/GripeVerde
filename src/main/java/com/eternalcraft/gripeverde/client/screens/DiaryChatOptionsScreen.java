package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.VictorianSkyline;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.CommonComponents;

public class DiaryChatOptionsScreen extends ChatOptionsScreen {

    private static final int INK_TITLE = 0xFF1A0A04;
    private static final int INK_SUBTITLE = 0xFF3D2817;
    private static final int SHADOW_COLOR = 0x40201409;
    private static final int OVERLAY_COLOR = 0x66000000;

    private static final int PANEL_W = 340;
    private static final int PANEL_H = 330;

    private int panelX, panelY, panelW, panelH;

    public DiaryChatOptionsScreen(Screen parent, Options options) {
        super(parent, options);
    }

    @Override
    protected void init() {
        super.init();

        this.panelW = Math.min(PANEL_W, this.width - 40);
        this.panelH = Math.min(PANEL_H, this.height - 30);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        if (this.list != null) {
            this.list.setRenderBackground(false);
            this.list.setRenderTopAndBottom(false);
            int listTop = this.panelY + 45;
            int listBottom = this.panelY + this.panelH - 45;
            this.list.updateSize(this.width, this.height, listTop, listBottom);
        }

        // Ocultar el boton Done vanilla
        for (GuiEventListener child : this.children()) {
            if (child instanceof Button btn) {
                Component msg = btn.getMessage();
                if (msg != null && msg.getString().equals(CommonComponents.GUI_DONE.getString())) {
                    btn.setX(-9999);
                    btn.setY(-9999);
                    btn.visible = false;
                    btn.active = false;
                    break;
                }
            }
        }

        // Boton Volver estilo diario (mismo label que Options)
        int btnW = 120;
        int btnH = 20;
        int btnX = (this.width - btnW) / 2;
        int btnY = this.panelY + this.panelH - 30;
        this.addRenderableWidget(new DiaryButton(
            btnX, btnY, btnW, btnH,
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
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Escena viva
        AtmosphereRenderer.renderSky(g, this.width, this.height);
        VictorianSkyline.renderBackground(g, this.width, this.height);
        LivingAtmosphere.renderFogLow(g, this.width, this.height);
        VictorianSkyline.renderForeground(g, this.width, this.height);
        LivingAtmosphere.renderStreetLevel(g, this.width, this.height);
        LivingAtmosphere.renderAir(g, this.width, this.height);

        // Overlay
        g.fill(0, 0, this.width, this.height, OVERLAY_COLOR);

        // Pergamino
        PaperTexture.drawClean(g, this.panelX, this.panelY, this.panelW, this.panelH);

        // Titulo
        String titleStr = Component.translatable("options.chat.title").getString();
        int titleX = this.width / 2;
        int titleY = this.panelY + 10;
        g.drawCenteredString(this.font, titleStr, titleX + 1, titleY + 1, SHADOW_COLOR);
        g.drawCenteredString(this.font, titleStr, titleX, titleY, INK_TITLE);

        // Subtitulo
        String subtitle = "- " + Component.translatable("gripeverde.options.chat.subtitle").getString() + " -";
        g.drawCenteredString(this.font, subtitle, titleX + 1, titleY + 13, SHADOW_COLOR);
        g.drawCenteredString(this.font, subtitle, titleX, titleY + 12, INK_SUBTITLE);

        // Divisor superior
        int divY1 = titleY + 26;
        drawInkDivider(g, this.panelX + 24, this.panelX + this.panelW - 24, divY1);

        // Divisor inferior (antes del boton)
        int divY2 = this.panelY + this.panelH - 38;
        drawInkDivider(g, this.panelX + 24, this.panelX + this.panelW - 24, divY2);

        // Esquineros victorianos
        drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);

        // Lista vanilla
        if (this.list != null) {
            this.list.render(g, mouseX, mouseY, partialTick);
        }

        // Widgets propios
        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void renderBackground(GuiGraphics g) {
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    // Divisor horizontal con fade a los costados color tinta
    private static void drawInkDivider(GuiGraphics g, int x1, int x2, int y) {
        int inkFull  = 0x88422810;
        int inkClear = 0x00422810;
        int mid = (x1 + x2) / 2;
        g.fillGradient(x1, y, mid, y + 1, inkClear, inkFull);
        g.fillGradient(mid, y, x2, y + 1, inkFull, inkClear);
    }

    // Esquineros victorianos en L color tinta (8x8 px)
    private static void drawCornerOrnaments(GuiGraphics g, int x, int y, int w, int h) {
        int ink = 0xA0422810;
        int len = 8;
        int th = 1;
        int off = 3;
        // Superior izquierda
        g.fill(x + off, y + off, x + off + len, y + off + th, ink);
        g.fill(x + off, y + off, x + off + th, y + off + len, ink);
        // Superior derecha
        g.fill(x + w - off - len, y + off, x + w - off, y + off + th, ink);
        g.fill(x + w - off - th, y + off, x + w - off, y + off + len, ink);
        // Inferior izquierda
        g.fill(x + off, y + h - off - th, x + off + len, y + h - off, ink);
        g.fill(x + off, y + h - off - len, x + off + th, y + h - off, ink);
        // Inferior derecha
        g.fill(x + w - off - len, y + h - off - th, x + w - off, y + h - off, ink);
        g.fill(x + w - off - th, y + h - off - len, x + w - off, y + h - off, ink);
    }
}