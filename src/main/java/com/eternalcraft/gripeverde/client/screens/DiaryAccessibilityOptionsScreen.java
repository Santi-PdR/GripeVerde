package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.render.VictorianSkyline;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton.Icon;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton.Variant;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DiaryAccessibilityOptionsScreen extends AccessibilityOptionsScreen {

    // ── Dimensiones del panel (igual que Chat) ────────────────────────────────
    private static final int PANEL_W = 340;
    private static final int PANEL_H = 395;

    // ── Colores tinta ─────────────────────────────────────────────────────────
    private static final int INK_TITLE    = 0xFF2A1A0A;
    private static final int INK_SUBTITLE = 0xFF6B4226;
    private static final int INK_DIVIDER  = 0x88422810;
    private static final int SHADOW_COL   = 0x20201409;
    private static final int OVERLAY_COL  = 0x66000000;

    // ── Botón propio ──────────────────────────────────────────────────────────
    private DiaryButton btnVolver;

    public DiaryAccessibilityOptionsScreen(Screen parent, Options options) {
        super(parent, options);
    }

    @Override
    protected void init() {
        super.init();

        // Panel centrado
        int panelX = (this.width  - PANEL_W) / 2;
        int panelY = (this.height - PANEL_H) / 2;

        // ── Suprimir fondo y barras de la lista vanilla ───────────────────────
        if (this.list != null) {
            this.list.setRenderBackground(false);
            this.list.setRenderTopAndBottom(false);
            this.list.setRenderSelection(false);
            // NO cambiar width — R-VANILLA-LIST-COORDS
            int listTop    = panelY + 60;
            int listBottom = panelY + PANEL_H - 45;
            this.list.updateSize(this.width, this.height, listTop, listBottom);
        }

        // ── Ocultar botón Done vanilla ────────────────────────────────────────
        for (var child : this.children()) {
            if (child instanceof Button btn) {
                btn.visible = false;
                btn.active  = false;
                btn.setX(this.width + 200);
            }
        }

        // ── Botón Volver propio ───────────────────────────────────────────────
        int btnW = 160;
        int btnH = 20;
        int btnX = (this.width - btnW) / 2;
        int btnY = panelY + PANEL_H - 28;
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
        // ── 1. Escena viva del Title ──────────────────────────────────────────
        AtmosphereRenderer.renderSky(gfx, this.width, this.height);
        VictorianSkyline.renderBackground(gfx, this.width, this.height);
        LivingAtmosphere.renderFogLow(gfx, this.width, this.height);
        VictorianSkyline.renderForeground(gfx, this.width, this.height);
        LivingAtmosphere.renderStreetLevel(gfx, this.width, this.height);
        LivingAtmosphere.renderAir(gfx, this.width, this.height);

        // ── 2. Overlay oscuro ─────────────────────────────────────────────────
        gfx.fill(0, 0, this.width, this.height, OVERLAY_COL);

        // ── 3. Panel pergamino ────────────────────────────────────────────────
        int panelX = (this.width  - PANEL_W) / 2;
        int panelY = (this.height - PANEL_H) / 2;
        PaperTexture.drawClean(gfx, panelX, panelY, PANEL_W, PANEL_H);


        // ── 5. Título ─────────────────────────────────────────────────────────
        int cx = this.width / 2;
        int titleY = panelY + 12;
        // sombra translúcida (R-SHADOW-STYLE)
        gfx.drawCenteredString(this.font,
            Component.translatable("options.accessibility.title"),
            cx + 1, titleY + 1, SHADOW_COL);
        gfx.drawCenteredString(this.font,
            Component.translatable("options.accessibility.title"),
            cx, titleY, INK_TITLE);

        // ── 6. Subtítulo ──────────────────────────────────────────────────────
        int subtitleY = titleY + 12;
        {
            Component subC = Component.translatable("gripeverde.options.accessibility.subtitle");
            int subW = this.font.width(subC);
            gfx.drawString(this.font, subC, cx - subW / 2, subtitleY, INK_SUBTITLE, false);
        }

        // ── 6b. Flavor text del cronista ──────────────────────────────────────
        int flavorY = subtitleY + 13;
        {
            Component flvC = Component.translatable("gripeverde.options.accessibility.flavor");
            int flvW = this.font.width(flvC);
            gfx.drawString(this.font, flvC, cx - flvW / 2, flavorY, 0xC06B4A2E, false);
        }

        // ── 7. Divisor superior ───────────────────────────────────────────────
        int divY1 = flavorY + 12;
        drawInkDivider(gfx, panelX, divY1, PANEL_W);

        // ── 7b. Emblema del gabinete: ojo victoriano bajo el divisor ──────────
        // ── 9. Dibujar la lista directamente (contiene sus propios entries/widgets)
        if (this.list != null) {
            this.list.render(gfx, mouseX, mouseY, delta);
        }
        // ── 10. Cosmeticos que van sobre la lista: divisor inferior + esquineros + firma
        int divY2 = panelY + PANEL_H - 38;
        drawInkDivider(gfx, panelX, divY2, PANEL_W);
        drawCornerOrnaments(gfx, panelX, panelY, PANEL_W, PANEL_H);
        // ── 11. Dibujar solo nuestro boton Volver
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

    // ── Helpers visuales (misma gramática que DiaryOptionsScreen) ─────────────

    private void drawInkDivider(GuiGraphics gfx, int x, int y, int w) {
        int fade = w / 6;
        int cx   = x + w / 2;
        // Dos mitades con hueco central de 6px donde ira el rombo
        gfx.fillGradient(x,            y, x + fade,     y + 1, 0x00000000, INK_DIVIDER);
        gfx.fill        (x + fade,     y, cx - 6,       y + 1, INK_DIVIDER);
        gfx.fill        (cx + 6,       y, x + w - fade, y + 1, INK_DIVIDER);
        gfx.fillGradient(x + w - fade, y, x + w,        y + 1, INK_DIVIDER, 0x00000000);
        // Rombo victoriano en el centro
        drawDivisorLozenge(gfx, cx, y);
    }

    private void drawCornerOrnaments(GuiGraphics gfx, int x, int y, int w, int h) {
        int colOut = 0xC0422810; // marco externo mas opaco
        int colIn  = 0x80422810; // marco interno mas tenue
        int sOut = 12;           // trazo externo (grueso)
        int sIn  = 5;            // trazo interno (fino)
        int off  = 3;            // separacion entre externo e interno

        // === Marco EXTERNO (esquineros grandes) ===
        // Superior izquierda
        gfx.fill(x,          y,          x + sOut, y + 1,    colOut);
        gfx.fill(x,          y,          x + 1,    y + sOut, colOut);
        // Superior derecha
        gfx.fill(x + w - sOut, y,        x + w,    y + 1,    colOut);
        gfx.fill(x + w - 1,    y,        x + w,    y + sOut, colOut);
        // Inferior izquierda
        gfx.fill(x,          y + h - 1,  x + sOut, y + h,    colOut);
        gfx.fill(x,          y + h - sOut, x + 1,  y + h,    colOut);
        // Inferior derecha
        gfx.fill(x + w - sOut, y + h - 1, x + w,   y + h,    colOut);
        gfx.fill(x + w - 1,    y + h - sOut, x + w, y + h,   colOut);

        // === Marco INTERNO (esquineros chicos, desplazados hacia adentro) ===
        int ix = x + off;
        int iy = y + off;
        int iw = w - off * 2;
        int ih = h - off * 2;
        // Superior izquierda
        gfx.fill(ix,          iy,          ix + sIn, iy + 1,   colIn);
        gfx.fill(ix,          iy,          ix + 1,   iy + sIn, colIn);
        // Superior derecha
        gfx.fill(ix + iw - sIn, iy,        ix + iw,  iy + 1,   colIn);
        gfx.fill(ix + iw - 1,   iy,        ix + iw,  iy + sIn, colIn);
        // Inferior izquierda
        gfx.fill(ix,          iy + ih - 1, ix + sIn, iy + ih,  colIn);
        gfx.fill(ix,          iy + ih - sIn, ix + 1, iy + ih,  colIn);
        // Inferior derecha
        gfx.fill(ix + iw - sIn, iy + ih - 1, ix + iw, iy + ih, colIn);
        gfx.fill(ix + iw - 1,   iy + ih - sIn, ix + iw, iy + ih, colIn);
    }

    // Rombo tipografico victoriano centrado sobre un divisor
    private void drawDivisorLozenge(GuiGraphics gfx, int cx, int cy) {
        int col = 0xC0422810;
        // rombo 5x5 pixel-art
        gfx.fill(cx,     cy - 2, cx + 1, cy - 1, col);
        gfx.fill(cx - 1, cy - 1, cx + 2, cy,     col);
        gfx.fill(cx - 2, cy,     cx + 3, cy + 1, col);
        gfx.fill(cx - 1, cy + 1, cx + 2, cy + 2, col);
        gfx.fill(cx,     cy + 2, cx + 1, cy + 3, col);
    }

    // Emblema del gabinete de Accesibilidad: estrella cuadruple (guia, orientacion)
    // Simbolo tipografico victoriano estilo asterisco de sello, 13x13
    private void drawStarEmblem(GuiGraphics gfx, int cx, int cy) {
        int col = 0xD0422810;
        int hi  = 0xE01A0A05;
        // Punta arriba
        gfx.fill(cx,     cy,     cx + 1, cy + 1, col);
        gfx.fill(cx,     cy + 1, cx + 1, cy + 2, col);
        gfx.fill(cx - 1, cy + 2, cx + 2, cy + 3, col);
        // Punta abajo (espejo)
        gfx.fill(cx,     cy + 10, cx + 1, cy + 11, col);
        gfx.fill(cx,     cy + 9,  cx + 1, cy + 10, col);
        gfx.fill(cx - 1, cy + 8,  cx + 2, cy + 9,  col);
        // Punta izquierda
        gfx.fill(cx - 5, cy + 5,  cx - 4, cy + 6,  col);
        gfx.fill(cx - 4, cy + 5,  cx - 3, cy + 6,  col);
        gfx.fill(cx - 3, cy + 4,  cx - 2, cy + 7,  col);
        // Punta derecha
        gfx.fill(cx + 5, cy + 5,  cx + 6, cy + 6,  col);
        gfx.fill(cx + 4, cy + 5,  cx + 5, cy + 6,  col);
        gfx.fill(cx + 3, cy + 4,  cx + 4, cy + 7,  col);
        // Nucleo central (rombo)
        gfx.fill(cx,     cy + 3, cx + 1, cy + 4, col);
        gfx.fill(cx - 1, cy + 4, cx + 2, cy + 5, col);
        gfx.fill(cx - 2, cy + 5, cx + 3, cy + 6, col);
        gfx.fill(cx - 1, cy + 6, cx + 2, cy + 7, col);
        gfx.fill(cx,     cy + 7, cx + 1, cy + 8, col);
        // Punto brillante centro
        gfx.fill(cx,     cy + 5, cx + 1, cy + 6, hi);
    }
}