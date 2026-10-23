package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.locale.Language;
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
    private int backButtonY, backButtonWidth, backButtonHeight;
    private boolean compactLayout;
    private Language cachedHeaderLanguage;
    private Font cachedHeaderFont;
    private String cachedTitle = "";
    private String cachedSubtitle = "";
    private int cachedSubtitleWidth;
    private String cachedFlavor = "";
    private int cachedFlavorWidth;
    private int cachedFlavorWidthLimit = Integer.MIN_VALUE;
    private String cachedFlavorShown = "";

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

        this.panelW = Math.max(1, Math.min(PANEL_W, this.width - 40));
        this.panelH = Math.max(1, Math.min(PANEL_H, this.height - 30));
        this.compactLayout = this.panelH < 190 || this.panelW < 240;
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        if (this.list != null) {
            this.list.setRenderBackground(false);
            this.list.setRenderTopAndBottom(false);
            int listTop = Math.max(0, Math.min(Math.max(0, this.height - 1),
                this.panelY + (this.compactLayout ? 30 : 55)));
            int listBottom = Math.min(this.height,
                this.panelY + this.panelH - (this.compactLayout ? 34 : 45));
            listBottom = Math.max(listTop + 1, listBottom);
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
        this.backButtonWidth = Math.max(1, Math.min(130, Math.min(this.panelW - 16, this.width)));
        this.backButtonHeight = Math.max(1, Math.min(22, Math.min(this.panelH, this.height)));
        int btnX = Math.max(0, Math.min(this.width - this.backButtonWidth,
            (this.width - this.backButtonWidth) / 2));
        this.backButtonY = Math.max(0, Math.min(this.height - this.backButtonHeight,
            this.compactLayout
                ? this.panelY + this.panelH - this.backButtonHeight - 4
                : this.panelY + this.panelH - this.backButtonHeight - 12));
        this.addRenderableWidget(new DiaryButton(btnX, this.backButtonY,
            this.backButtonWidth, this.backButtonHeight,
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
        this.refreshHeaderTextCache();
        // Escena viva
        g.fill(0, 0, this.width, this.height, 0xFF000000);
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
                // Overlay

        // Pergamino
        DiaryChrome.drawPanelShadow(g, this.panelX, this.panelY, this.panelW, this.panelH);
        PaperTexture.drawClean(g, this.panelX, this.panelY, this.panelW, this.panelH, PaperTexture.PaperKind.OFFICIAL);

        // Titulo
        int titleX = this.width / 2;
        int titleY = this.panelY + (this.compactLayout ? 5 : 12);
        com.eternalcraft.gripeverde.client.render.DiaryTypography.drawCentered(
            g, this.font, this.cachedTitle, titleX, titleY, DiaryChrome.INK_TITLE);

        if (!this.compactLayout) {
            g.drawString(this.font, this.cachedSubtitle, titleX - this.cachedSubtitleWidth / 2,
                titleY + 14, DiaryChrome.INK_SUBTITLE, false);
            g.drawString(this.font, this.cachedFlavorShown, titleX - this.cachedFlavorWidth / 2,
                titleY + 26, DiaryChrome.INK_FLAVOR, false);
        }

        int dividerInset = Math.min(14, Math.max(0, (this.panelW - 1) / 2));
        if (this.panelW >= 3) {
            DiaryChrome.drawInkDividerBetween(g, this.panelX + dividerInset,
                this.panelX + this.panelW - dividerInset, titleY + (this.compactLayout ? 18 : 40));
            DiaryChrome.drawInkDividerBetween(g, this.panelX + dividerInset,
                this.panelX + this.panelW - dividerInset, this.backButtonY - 6);
        }

        if (this.panelW >= 48 && this.panelH >= 48) {
            DiaryChrome.drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);
        }
        if (this.panelW >= 200 && this.panelH >= 100) {
            DiaryChrome.drawRegistryTag(g, this.panelX, this.panelY, this.panelW, this.panelH, "REG. VIII");
        }

        // Lista vanilla
        if (this.list != null) {
            this.list.render(g, mouseX, mouseY, partialTick);
        }

        // Widgets propios (boton Volver)
        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }
    }

    private void refreshHeaderTextCache() {
        Language language = Language.getInstance();
        Font font = this.font;
        if (language != this.cachedHeaderLanguage || font != this.cachedHeaderFont) {
            this.cachedHeaderLanguage = language;
            this.cachedHeaderFont = font;
            this.cachedTitle = this.title.getString();
            this.cachedSubtitle = "- "
                + Component.translatable("gripeverde.options.online.subtitle").getString() + " -";
            this.cachedSubtitleWidth = font.width(this.cachedSubtitle);
            this.cachedFlavor = Component.translatable("gripeverde.options.online.flavor").getString();
            this.cachedFlavorWidthLimit = Integer.MIN_VALUE;
        }

        int flavorWidthLimit = Math.max(0, this.panelW - 28);
        if (flavorWidthLimit == this.cachedFlavorWidthLimit) return;
        this.cachedFlavorWidthLimit = flavorWidthLimit;
        String flavorShown = font.width(this.cachedFlavor) <= flavorWidthLimit
            ? this.cachedFlavor
            : font.plainSubstrByWidth(this.cachedFlavor,
                Math.max(0, flavorWidthLimit - font.width("..."))) + "...";
        this.cachedFlavorShown = flavorShown;
        this.cachedFlavorWidth = font.width(flavorShown);
    }
}
