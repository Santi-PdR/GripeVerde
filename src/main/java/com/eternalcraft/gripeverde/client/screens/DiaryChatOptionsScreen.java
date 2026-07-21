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
    private static final int SHADOW_COLOR = 0x20201409;
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
        int subWC = this.font.width(subtitle); g.drawString(this.font, subtitle, titleX - subWC / 2, titleY + 12, INK_SUBTITLE, false);

        // Flavor del cronista bajo el subtitulo
        String flavor = Component.translatable("gripeverde.options.chat.flavor").getString();
        int flvWC = this.font.width(flavor); g.drawString(this.font, flavor, (this.width - flvWC) / 2, titleY + 24, 0xC06B4A2E, false);
        int divY1 = titleY + 42;
        // Divisor superior
        drawInkDivider(g, this.panelX + 14, this.panelX + this.panelW - 14, divY1);


        // Divisor inferior (antes del boton) + firma
        int divY2 = this.panelY + this.panelH - 38;
        drawInkDivider(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);
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

    // Divisor horizontal con fade suave + rombo central. Extendido hasta cerca de los esquineros.
    private static void drawInkDivider(GuiGraphics g, int x1, int x2, int y) {
        int inkFull  = 0x88422810;
        int inkClear = 0x00422810;
        int mid = (x1 + x2) / 2;
        int fade = (x2 - x1) / 8; // fade mas corto -> divisor mas presente
        g.fillGradient(x1,        y, x1 + fade,     y + 1, inkClear, inkFull);
        g.fill        (x1 + fade, y, mid - 6,       y + 1, inkFull);
        g.fill        (mid + 6,   y, x2 - fade,     y + 1, inkFull);
        g.fillGradient(x2 - fade, y, x2,            y + 1, inkFull, inkClear);
        drawDivisorLozenge(g, mid, y);
    }

    // Esquineros victorianos DOBLES: marco externo (12px) + marco interno (5px)
    private static void drawCornerOrnaments(GuiGraphics g, int x, int y, int w, int h) {
        int colOut = 0xC0422810;
        int colIn  = 0x80422810;
        int sOut = 12;
        int sIn  = 5;
        int off  = 3;

        // === Marco EXTERNO ===
        // Superior izquierda
        g.fill(x,             y,             x + sOut, y + 1,    colOut);
        g.fill(x,             y,             x + 1,    y + sOut, colOut);
        // Superior derecha
        g.fill(x + w - sOut,  y,             x + w,    y + 1,    colOut);
        g.fill(x + w - 1,     y,             x + w,    y + sOut, colOut);
        // Inferior izquierda
        g.fill(x,             y + h - 1,     x + sOut, y + h,    colOut);
        g.fill(x,             y + h - sOut,  x + 1,    y + h,    colOut);
        // Inferior derecha
        g.fill(x + w - sOut,  y + h - 1,     x + w,    y + h,    colOut);
        g.fill(x + w - 1,     y + h - sOut,  x + w,    y + h,    colOut);

        // === Marco INTERNO ===
        int ix = x + off;
        int iy = y + off;
        int iw = w - off * 2;
        int ih = h - off * 2;
        // Superior izquierda
        g.fill(ix,             iy,             ix + sIn, iy + 1,    colIn);
        g.fill(ix,             iy,             ix + 1,   iy + sIn,  colIn);
        // Superior derecha
        g.fill(ix + iw - sIn,  iy,             ix + iw,  iy + 1,    colIn);
        g.fill(ix + iw - 1,    iy,             ix + iw,  iy + sIn,  colIn);
        // Inferior izquierda
        g.fill(ix,             iy + ih - 1,    ix + sIn, iy + ih,   colIn);
        g.fill(ix,             iy + ih - sIn,  ix + 1,   iy + ih,   colIn);
        // Inferior derecha
        g.fill(ix + iw - sIn,  iy + ih - 1,    ix + iw,  iy + ih,   colIn);
        g.fill(ix + iw - 1,    iy + ih - sIn,  ix + iw,  iy + ih,   colIn);
    }

    // Rombo tipografico victoriano centrado sobre un divisor
    private static void drawDivisorLozenge(GuiGraphics g, int cx, int cy) {
        int col = 0xC0422810;
        g.fill(cx,     cy - 2, cx + 1, cy - 1, col);
        g.fill(cx - 1, cy - 1, cx + 2, cy,     col);
        g.fill(cx - 2, cy,     cx + 3, cy + 1, col);
        g.fill(cx - 1, cy + 1, cx + 2, cy + 2, col);
        g.fill(cx,     cy + 2, cx + 1, cy + 3, col);
    }

    // Emblema del gabinete de Correspondencia: cruz de Malta compacta (9x9)
    // Version acotada para no invadir el area de la lista
    private static void drawSectionEmblem(GuiGraphics g, int cx, int cy) {
        int col = 0xD0422810;
        int hi  = 0xE01A0A05;
        // Brazo superior
        g.fill(cx,     cy,     cx + 1, cy + 1, col);
        g.fill(cx - 1, cy + 1, cx + 2, cy + 2, col);
        // Brazo inferior
        g.fill(cx,     cy + 7, cx + 1, cy + 8, col);
        g.fill(cx - 1, cy + 6, cx + 2, cy + 7, col);
        // Brazo izquierdo
        g.fill(cx - 4, cy + 3, cx - 3, cy + 5, col);
        g.fill(cx - 3, cy + 2, cx - 2, cy + 6, col);
        // Brazo derecho
        g.fill(cx + 3, cy + 3, cx + 4, cy + 5, col);
        g.fill(cx + 2, cy + 2, cx + 3, cy + 6, col);
        // Centro (rombo)
        g.fill(cx,     cy + 2, cx + 1, cy + 7, col);
        g.fill(cx - 1, cy + 3, cx + 2, cy + 6, col);
        g.fill(cx - 2, cy + 4, cx + 3, cy + 5, col);
        // Punto brillante central
        g.fill(cx,     cy + 4, cx + 1, cy + 5, hi);
    }
}