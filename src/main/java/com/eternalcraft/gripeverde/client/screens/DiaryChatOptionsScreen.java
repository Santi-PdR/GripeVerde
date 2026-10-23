package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class DiaryChatOptionsScreen extends ChatOptionsScreen {

    private static final int PANEL_W = 340;
    private static final int PANEL_H = 330;

    private int panelX, panelY, panelW, panelH;
    private int backButtonY, backButtonWidth, backButtonHeight;
    private boolean compactLayout;
    private final SmoothScroll smoothScroll = new SmoothScroll();
    private Language cachedHeaderLanguage;
    private Font cachedHeaderFont;
    private String cachedTitle = "";
    private String cachedSubtitle = "";
    private int cachedSubtitleWidth;
    private String cachedFlavor = "";
    private int cachedFlavorWidth;

    public DiaryChatOptionsScreen(Screen parent, Options options) {
        super(parent, options);
    }

    @Override
    protected void init() {
        String[] flavorPool = getChatFlavorPool();
        chatFlavorIndex = CHAT_RNG.nextInt(flavorPool.length);
        super.init();

        this.panelW = Math.max(1, Math.min(PANEL_W, this.width - 40));
        this.panelH = Math.max(1, Math.min(PANEL_H, this.height - 30));
        this.compactLayout = this.panelH < 190 || this.panelW < 240;
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        if (this.list != null) {
            this.list.setRenderBackground(false);
            this.list.setRenderTopAndBottom(false);
            int listTop = Math.max(0, Math.min(Math.max(0, this.height - 1),
                this.panelY + (this.compactLayout ? 30 : 45)));
            int listBottom = Math.min(this.height,
                this.panelY + this.panelH - (this.compactLayout ? 34 : 45));
            listBottom = Math.max(listTop + 1, listBottom);
            this.list.updateSize(this.width, this.height, listTop, listBottom);
            this.smoothScroll.snapToCurrent(this.list);
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

        // Boton Volver estilo diario
        this.backButtonWidth = Math.max(1, Math.min(120, Math.min(this.panelW - 16, this.width)));
        this.backButtonHeight = Math.max(1, Math.min(20, Math.min(this.panelH, this.height)));
        int btnX = Math.max(0, Math.min(this.width - this.backButtonWidth,
            (this.width - this.backButtonWidth) / 2));
        this.backButtonY = Math.max(0, Math.min(this.height - this.backButtonHeight,
            this.compactLayout
                ? this.panelY + this.panelH - this.backButtonHeight - 4
                : this.panelY + this.panelH - 30));
        this.addRenderableWidget(new DiaryButton(
            btnX, this.backButtonY, this.backButtonWidth, this.backButtonHeight,
            Component.translatable("gripeverde.options.button.done"),
            DiaryButton.Icon.DOOR,
            DiaryButton.Variant.PRIMARY,
            () -> this.onClose()
        ));
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
        // Clear framebuffer + transition fade
        g.fill(0, 0, this.width, this.height, 0xFF000000);
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
                // Overlay

        // Pergamino
        DiaryChrome.drawPanelShadow(g, this.panelX, this.panelY, this.panelW, this.panelH);
        PaperTexture.drawClean(g, this.panelX, this.panelY, this.panelW, this.panelH, PaperTexture.PaperKind.CORRESPONDENCE);

        // Titulo
        int titleX = this.width / 2;
        int titleY = this.panelY + (this.compactLayout ? 5 : 10);
        com.eternalcraft.gripeverde.client.render.DiaryTypography.drawCentered(
            g, this.font, this.cachedTitle, titleX, titleY, DiaryChrome.INK_TITLE);

        if (!this.compactLayout) {
            g.drawString(this.font, this.cachedSubtitle, titleX - this.cachedSubtitleWidth / 2,
                titleY + 12, DiaryChrome.INK_SUBTITLE, false);

            g.drawString(this.font, this.cachedFlavor, (this.width - this.cachedFlavorWidth) / 2,
                titleY + 24, DiaryChrome.INK_FLAVOR, false);
        }

        int dividerInset = Math.min(14, Math.max(0, (this.panelW - 1) / 2));
        if (this.panelW >= 3) {
            DiaryChrome.drawInkDividerBetween(g, this.panelX + dividerInset,
                this.panelX + this.panelW - dividerInset, titleY + (this.compactLayout ? 18 : 42));
            DiaryChrome.drawInkDividerBetween(g, this.panelX + dividerInset,
                this.panelX + this.panelW - dividerInset, this.backButtonY - 6);
        }
        if (this.panelW >= 48 && this.panelH >= 48) {
            DiaryChrome.drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);
        }
        if (this.panelW >= 200 && this.panelH >= 100) {
            DiaryChrome.drawRegistryTag(g, this.panelX, this.panelY, this.panelW, this.panelH, "REG. V");
        }

        // Lista vanilla sin gradientes blancos ni bandejas extra
        if (this.list != null) {
            this.list.render(g, mouseX, mouseY, partialTick);
            DiaryChrome.drawStyledScrollThumbInPanel(g, this.list, this.panelX + this.panelW);
        }

        // Widgets propios
        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }

        // v0.21.26: buzón removido.
        try {
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, this.panelY - 12);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    private void refreshHeaderTextCache() {
        Language language = Language.getInstance();
        Font font = this.font;
        if (language == this.cachedHeaderLanguage && font == this.cachedHeaderFont) return;

        this.cachedHeaderLanguage = language;
        this.cachedHeaderFont = font;
        this.cachedTitle = Component.translatable("options.chat.title").getString();
        this.cachedSubtitle = "- "
            + Component.translatable("gripeverde.options.chat.subtitle").getString() + " -";
        this.cachedSubtitleWidth = font.width(this.cachedSubtitle);
        this.cachedFlavor = getRotatingChatFlavor();
        this.cachedFlavorWidth = font.width(this.cachedFlavor);
    }

    // Rotating flavor quotes for chat options
    private static final String[] CHAT_FLAVOR_POOL_ES = {
        "De la palabra escrita entre extranos.",
        "Todo mensaje llega tarde en tiempos de peste.",
        "Los rumores viajan mas rapido que la fiebre.",
        "Hablad bajo, las paredes escuchan.",
        "El silencio tambien es una forma de correspondencia.",
        "Las cartas se pierden, los gritos no.",
        "Cuidad vuestra tinta; escasea."
    };
    private static final String[] CHAT_FLAVOR_POOL_EN = {
        "Of the written word between strangers.",
        "Every message arrives late in plague time.",
        "Rumours travel faster than fever.",
        "Speak softly; the walls listen.",
        "Silence is also a form of correspondence.",
        "Letters get lost; screams do not.",
        "Guard your ink; it is scarce."
    };
    private static int chatFlavorIndex = -1;
    private static final java.util.Random CHAT_RNG = new java.util.Random();

    private static String getRotatingChatFlavor() {
        String[] pool = getChatFlavorPool();
        if (chatFlavorIndex < 0 || chatFlavorIndex >= pool.length) {
            chatFlavorIndex = CHAT_RNG.nextInt(pool.length);
        }
        return pool[chatFlavorIndex];
    }

    private static String[] getChatFlavorPool() {
        String lang = net.minecraft.client.Minecraft.getInstance().getLanguageManager().getSelected();
        boolean english = lang == null || lang.startsWith("en_") || lang.equals("en_us");
        return english ? CHAT_FLAVOR_POOL_EN : CHAT_FLAVOR_POOL_ES;
    }

}
