package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.ExpedientHeader;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.VictorianSkyline;
import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryProgressBar;
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

    private static final int PANEL_W = 520;
    private static final int PANEL_H = 336;
    private static final int RAIL_W = 150;

    private int panelX, panelY, panelW, panelH;
    private final SmoothScroll smoothScroll = new SmoothScroll();
    private long openedAtMs = 0L;

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
        PaperTexture.drawClean(g, this.panelX, this.panelY, this.panelW, this.panelH, PaperTexture.PaperKind.CORRESPONDENCE);
        DiaryChrome.drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);

        int headerBottom = ExpedientHeader.draw(g, this.font, this.panelX, this.panelY, this.panelW,
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "EXPEDIENT V" : "EXPEDIENTE V",
            Component.translatable("options.chat.title"),
            "- " + Component.translatable("gripeverde.options.chat.subtitle").getString() + " -",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "EC-QV/1897 · CORRESPONDENCE" : "EC-QV/1897 · CORRESPONDENCIA",
            getRotatingChatFlavor(),
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "OPEN CHANNEL" : "CANAL ABIERTO",
            DiaryTheme.BRONZE,
            reveal);

        int railX = this.panelX + this.panelW - RAIL_W - 18;
        DiaryChrome.drawInsetCard(g, this.font, railX, headerBottom, RAIL_W, 84,
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "DISTRICT MAIL" : "CORREO DEL DISTRITO",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Public notices" : "Avisos publicos",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "The mailbox keeps unanswered letters apart from the public chat settings." : "El buzon conserva las cartas sin respuesta aparte de los ajustes publicos.",
            DiaryTheme.BRONZE);
        DiaryChrome.drawInsetCard(g, this.font, railX, headerBottom + 92, RAIL_W, 90,
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "DELIVERY RATE" : "RITMO DE ENTREGA",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Readability" : "Legibilidad",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Chat options remain vanilla, but the chamber now separates message controls from the archive surface." : "Las opciones siguen siendo vanilla, pero la camara separa los controles del papel de archivo.",
            DiaryTheme.PLAGUE_SICK);
        DiaryProgressBar.draw(g, railX + 10, headerBottom + 132, RAIL_W - 20, 14,
            Math.min(1f, com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("letters") / 5f),
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Mail" : "Cartas",
            DiaryProgressBar.Style.INK);

        int listFrameX = this.panelX + 18;
        int listFrameW = railX - listFrameX - 16;
        DiaryChrome.drawInnerRail(g, listFrameX, headerBottom - 2, listFrameW, this.panelH - 138, DiaryTheme.BRONZE);

        if (this.list != null) {
            if (reveal < 0.995f) {
                int revealBottom = this.panelY + Math.max(28, (int)(this.panelH * reveal));
                g.enableScissor(this.panelX - 2, this.panelY - 2, this.panelX + this.panelW + 2, revealBottom);
                this.list.render(g, mouseX, mouseY, partialTick);
                DiaryChrome.drawStyledScrollThumbInPanel(g, this.list, railX - 8);
                g.disableScissor();
            } else {
                this.list.render(g, mouseX, mouseY, partialTick);
                DiaryChrome.drawStyledScrollThumbInPanel(g, this.list, railX - 8);
            }
        }

        int divY2 = this.panelY + this.panelH - 38;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);

        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }

        try {
            drawMailbox(g, mouseX, mouseY);
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, this.panelY - 12);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {}
    }

    private int mailboxX() { return this.panelX + this.panelW - 40; }
    private int mailboxY() { return this.panelY + 14; }

    private void drawMailbox(GuiGraphics g, int mouseX, int mouseY) {
        int x = mailboxX(), y = mailboxY();
        boolean hover = mouseX >= x - 2 && mouseX <= x + 24 && mouseY >= y - 2 && mouseY <= y + 20;
        int wood = hover ? 0xFF7A5A2E : 0xFF6B4A2E;
        // Caja de madera con ranura
        g.fill(x, y, x + 22, y + 14, wood);
        g.fill(x, y, x + 22, y + 2, 0xFF8E6A35);
        g.fill(x, y + 12, x + 22, y + 14, 0xFF3D2A18);
        g.fill(x + 5, y + 6, x + 17, y + 8, 0xFF1A0F05);
        g.fill(x + 2, y + 14, x + 4, y + 19, 0xFF3D2E1A);
        g.fill(x + 18, y + 14, x + 20, y + 19, 0xFF3D2E1A);
        // Cartas depositadas: marcas de conteo
        int letters = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("letters");
        for (int i = 0; i < Math.min(8, letters); i++) {
            g.fill(x + 2 + i * 2, y - 4, x + 3 + i * 2, y - 1, 0xFFD8CAB0);
        }
        // La respuesta sellada que nadie abrira
        if (letters >= 5) {
            g.fill(x + 4, y + 2, x + 14, y + 9, 0xFFE6D2A6);
            g.fill(x + 8, y + 4, x + 10, y + 6, 0xFF7A1F18);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= mailboxX() - 2 && mouseX <= mailboxX() + 24
                && mouseY >= mailboxY() - 2 && mouseY <= mailboxY() + 20) {
            try {
                int n = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.bump("letters");
                com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                    com.eternalcraft.gripeverde.client.audio.ModSounds.UI_SCROLL.get(), 0.18f, 0.66f);
                if (n >= 5) {
                    boolean first = com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                        com.eternalcraft.gripeverde.client.lore.Discoveries.LETTERS);
                    if (first) {
                        com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                            com.eternalcraft.gripeverde.client.lore.Marginalia.N_LETTERS);
                    }
                }
            } catch (Throwable ignored) {}
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
