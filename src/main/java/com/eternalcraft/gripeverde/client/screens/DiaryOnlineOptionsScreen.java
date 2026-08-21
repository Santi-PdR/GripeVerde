package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.ExpedientHeader;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryProgressBar;
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

    private static final int PANEL_W = 500;
    private static final int PANEL_H = 258;
    private static final int STATION_W = 164;

    private final SmoothScroll smoothScroll = new SmoothScroll(0.50D, 0.24D);
    private final Options ownOptions;
    private int panelX, panelY, panelW, panelH;
    private long openedAtMs = 0L;

    public DiaryOnlineOptionsScreen(Screen parent, Options options) {
        super(parent,
              options,
              Component.translatable("options.online.title"),
              new OptionInstance<?>[] {
                  options.realmsNotifications(),
                  options.allowServerListing()
              });
        this.ownOptions = options;
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
            int listTop = this.panelY + 92;
            int listBottom = this.panelY + this.panelH - 52;
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
        this.openedAtMs = System.currentTimeMillis();
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

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0xFF000000);
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);

        float reveal = DiaryChrome.reveal(this.openedAtMs, 240L);
        DiaryChrome.drawPanelScaffold(g, this.panelX, this.panelY, this.panelW, this.panelH, DiaryTheme.BRONZE);
        PaperTexture.drawClean(g, this.panelX, this.panelY, this.panelW, this.panelH, PaperTexture.PaperKind.OFFICIAL);
        DiaryChrome.drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);

        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        int headerBottom = ExpedientHeader.draw(g, this.font, this.panelX, this.panelY, this.panelW,
            en ? "EXPEDIENT VIII" : "EXPEDIENTE VIII",
            this.title,
            "- " + Component.translatable("gripeverde.options.online.subtitle").getString() + " -",
            en ? "EC-QV/1897 · SIGNAL OFFICE" : "EC-QV/1897 · OFICINA DE SENALES",
            Component.translatable("gripeverde.options.online.flavor").getString(),
            en ? "RELAY OPEN" : "RELE ABIERTO",
            DiaryTheme.BRONZE,
            reveal);

        int stationX = this.panelX + this.panelW - STATION_W - 18;
        int stationY = headerBottom - 2;
        drawSignalStation(g, stationX, stationY, STATION_W, this.panelH - 122);

        int boardX = this.panelX + 18;
        int boardW = stationX - boardX - 14;
        DiaryChrome.drawInnerRail(g, boardX, headerBottom - 2, boardW, this.panelH - 122, DiaryTheme.BRONZE);
        g.drawString(this.font, en ? "COMMUNICATION BOARD" : "TABLERO DE COMUNICACION", boardX + 10, headerBottom + 6, DiaryTheme.BRONZE, false);
        g.drawString(this.font, en ? "What leaves the district and what remains sealed." : "Lo que sale del distrito y lo que permanece sellado.", boardX + 10, headerBottom + 18, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 148), false);

        if (this.list != null) {
            if (reveal < 0.995f) {
                int revealBottom = this.panelY + Math.max(28, (int)(this.panelH * reveal));
                g.enableScissor(this.panelX - 2, this.panelY - 2, this.panelX + this.panelW + 2, revealBottom);
                this.list.render(g, mouseX, mouseY, partialTick);
                DiaryChrome.drawStyledScrollThumbInPanel(g, this.list, stationX - 8);
                g.disableScissor();
            } else {
                this.list.render(g, mouseX, mouseY, partialTick);
                DiaryChrome.drawStyledScrollThumbInPanel(g, this.list, stationX - 8);
            }
        }

        int divY2 = this.panelY + this.panelH - 40;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);
        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }
    }

    private void drawSignalStation(GuiGraphics g, int x, int y, int w, int h) {
        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        DiaryChrome.drawInnerRail(g, x, y, w, h, DiaryTheme.PLAGUE_SICK);
        g.drawString(this.font, en ? "RELAY STATION" : "ESTACION DE RELE", x + 10, y + 8, DiaryTheme.BRONZE, false);
        g.drawString(this.font, en ? "public / private" : "publico / privado", x + 10, y + 20, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 150), false);

        // poste y lineas de señal
        int mastX = x + w - 28;
        g.fill(mastX, y + 34, mastX + 3, y + h - 18, 0xFF3D2E1A);
        g.fill(mastX - 10, y + 42, mastX + 11, y + 43, 0xFF8E6A35);
        g.fill(mastX - 14, y + 58, mastX + 15, y + 59, 0xFF6B4A2E);
        g.fill(mastX - 18, y + 76, mastX + 19, y + 77, DiaryTheme.withAlpha(DiaryTheme.PLAGUE_SICK, 120));

        DiaryChrome.drawValueRow(g, this.font, x + 10, y + 44, w - 44,
            en ? "notices" : "avisos", this.ownOptions.realmsNotifications().get() ? (en ? "open" : "abiertos") : (en ? "closed" : "cerrados"), DiaryTheme.BRONZE);
        DiaryChrome.drawValueRow(g, this.font, x + 10, y + 64, w - 44,
            en ? "listing" : "listado", this.ownOptions.allowServerListing().get() ? (en ? "public" : "publico") : (en ? "sealed" : "sellado"), DiaryTheme.PLAGUE_SICK);
        DiaryProgressBar.draw(g, x + 10, y + h - 38, w - 20, 14,
            computePublicSignal(), en ? "signal spread" : "alcance", DiaryProgressBar.Style.STEEL);
        g.drawString(this.font,
            en ? "district traffic remains under your hand" : "el trafico del distrito permanece bajo vuestra mano",
            x + 10, y + h - 20, DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 132), false);
    }

    private float computePublicSignal() {
        float v = 0f;
        if (this.ownOptions.realmsNotifications().get()) v += 0.5f;
        if (this.ownOptions.allowServerListing().get()) v += 0.5f;
        return v;
    }
}
