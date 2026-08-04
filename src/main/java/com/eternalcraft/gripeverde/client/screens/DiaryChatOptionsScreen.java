package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.VictorianSkyline;
import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class DiaryChatOptionsScreen extends ChatOptionsScreen {

    private static final int PANEL_W = 340;
    private static final int PANEL_H = 330;

    private int panelX, panelY, panelW, panelH;
    private final SmoothScroll smoothScroll = new SmoothScroll();

    public DiaryChatOptionsScreen(Screen parent, Options options) {
        super(parent, options);
    }

    @Override
    protected void init() {
        String[] flavorPool = getChatFlavorPool();
        chatFlavorIndex = CHAT_RNG.nextInt(flavorPool.length);
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
        // Clear framebuffer + transition fade
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
        String titleStr = Component.translatable("options.chat.title").getString();
        int titleX = this.width / 2;
        int titleY = this.panelY + 10;
        {
        int _tw = this.font.width(titleStr);
        g.drawString(this.font, titleStr, titleX - _tw / 2,     titleY,     DiaryChrome.INK_TITLE,  false);
        }

        // Subtitulo
        String subtitle = "- " + Component.translatable("gripeverde.options.chat.subtitle").getString() + " -";
        int subWC = this.font.width(subtitle);
        g.drawString(this.font, subtitle, titleX - subWC / 2, titleY + 12, DiaryChrome.INK_SUBTITLE, false);

        // Flavor rotativo
        String flavor = getRotatingChatFlavor();
        int flvWC = this.font.width(flavor);
        g.drawString(this.font, flavor, (this.width - flvWC) / 2, titleY + 24, DiaryChrome.INK_FLAVOR, false);

        // Divisor superior
        int divY1 = titleY + 42;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY1);

        // Divisor inferior + esquineros
        int divY2 = this.panelY + this.panelH - 38;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);
        DiaryChrome.drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);

        // Lista vanilla sin gradientes blancos ni bandejas extra
        if (this.list != null) {
            this.list.render(g, mouseX, mouseY, partialTick);
            DiaryChrome.drawStyledScrollThumbInPanel(g, this.list, this.panelX + this.panelW);
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
