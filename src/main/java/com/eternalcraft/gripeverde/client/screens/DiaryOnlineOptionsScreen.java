package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
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
        super.init();

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
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
                // Overlay

        // Pergamino
        DiaryChrome.drawPanelShadow(g, this.panelX, this.panelY, this.panelW, this.panelH);
        PaperTexture.drawTornSheet(g, this.panelX, this.panelY, this.panelW, this.panelH, PaperTexture.PaperKind.OFFICIAL);

        // Titulo
        String titleStr = DiaryChrome.fitCached(this.font, this.title.getString(), Math.max(0, this.panelW - 28));
        int titleX = this.width / 2;
        int titleY = this.panelY + 12;
        int tw = DiaryChrome.widthCached(this.font, titleStr);
        g.drawString(this.font, titleStr, titleX - tw / 2, titleY, DiaryChrome.INK_TITLE, false);

        // Subtitulo diegetico
        String subtitle = "- " + DiaryChrome.tr("gripeverde.options.online.subtitle") + " -";
        // v0.304.0: el subtitulo se cortaba antes de la chapa REG. VIII.
        subtitle = DiaryChrome.fitCached(this.font, subtitle, Math.max(0, this.panelW - 150));
        int subW = DiaryChrome.widthCached(this.font, subtitle);
        g.drawString(this.font, subtitle, titleX - subW / 2, titleY + 14, DiaryChrome.INK_SUBTITLE, false);
        // v0.310.0: el resumen de lo que queda encendido sube al renglon de la
        // cita; al pie caia sobre el boton de volver.
        String headerLine = "";
        try {
            headerLine = onlineSummaryLine();
        } catch (Throwable ignored) {
        }
        if (headerLine.isEmpty()) {
            headerLine = DiaryChrome.tr("gripeverde.options.online.flavor");
        }
        String shown = DiaryChrome.fitCached(this.font, headerLine, Math.max(0, this.panelW - 150));
        int flavorW = DiaryChrome.widthCached(this.font, shown);
        g.drawString(this.font, shown, titleX - flavorW / 2, titleY + 26, DiaryChrome.INK_FLAVOR, false);

        // Divisor
        int divY = titleY + 40;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY);

        // Divisor inferior + esquineros
        int divY2 = this.panelY + this.panelH - 40;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);
        DiaryChrome.drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);
        DiaryChrome.drawRegistryTag(g, this.panelX, this.panelY, this.panelW, this.panelH, "REG. VIII");

        // Lista vanilla
        if (this.list != null) {
            this.list.render(g, mouseX, mouseY, partialTick);
        }

        // v0.310.0: el resumen vive en la cabecera; aqui queda el boton solo.

        // Widgets propios (boton Volver)
        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }
    }

    /** v0.282.0: avisos de realms y listado en servidores, en claro. */
    private String onlineSummaryLine() {
        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        try {
            boolean realms = this.options.realmsNotifications().get();
            boolean listing = this.options.allowServerListing().get();
            return (en ? "realms news: " : "avisos de realms: ")
                + (realms ? (en ? "on" : "si") : (en ? "off" : "no"))
                + (en ? "  ·  public listing: " : "  ·  listado publico: ")
                + (listing ? (en ? "on" : "si") : (en ? "off" : "no"));
        } catch (Throwable ignored) {
            return "";
        }
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
