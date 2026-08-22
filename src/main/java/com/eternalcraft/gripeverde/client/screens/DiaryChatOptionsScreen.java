package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.ExpedientHeader;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
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

    private static final int PANEL_W = 560;
    private static final int PANEL_H = 350;
    private static final int RACK_W = 172;

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
            int listTop = this.panelY + 62;
            int listBottom = this.panelY + this.panelH - 54;
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
        DiaryChrome.renderPanelBase(g, this.panelX, this.panelY, this.panelW, this.panelH, DiaryTheme.BRONZE, PaperTexture.PaperKind.CORRESPONDENCE);



        if (this.list != null) {
            if (reveal < 0.995f) {
                int revealBottom = this.panelY + Math.max(28, (int)(this.panelH * reveal));
                g.enableScissor(this.panelX - 2, this.panelY - 2, this.panelX + this.panelW + 2, revealBottom);
                this.list.render(g, mouseX, mouseY, partialTick);
                DiaryChrome.drawStyledScrollThumbInPanel(g, this.list, rackX - 8);
                g.disableScissor();
            } else {
                this.list.render(g, mouseX, mouseY, partialTick);
                DiaryChrome.drawStyledScrollThumbInPanel(g, this.list, rackX - 8);
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

    private int mailboxX() { return this.panelX + this.panelW - RACK_W + 10; }
    private int mailboxY() { return this.panelY + 104; }


    private void drawMailbox(GuiGraphics g, int mouseX, int mouseY) {
        int x = mailboxX(), y = mailboxY();
        boolean hover = mouseX >= x - 2 && mouseX <= x + 34 && mouseY >= y - 2 && mouseY <= y + 26;
        int wood = hover ? 0xFF7A5A2E : 0xFF6B4A2E;
        g.fill(x, y, x + 30, y + 18, wood);
        g.fill(x, y, x + 30, y + 2, 0xFF8E6A35);
        g.fill(x + 6, y + 8, x + 24, y + 10, 0xFF1A0F05);
        g.fill(x + 3, y + 18, x + 6, y + 24, 0xFF3D2E1A);
        g.fill(x + 24, y + 18, x + 27, y + 24, 0xFF3D2E1A);
        int letters = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("letters");
        for (int i = 0; i < Math.min(6, letters); i++) {
            g.fill(x + 4 + i * 3, y - 4, x + 5 + i * 3, y - 1, 0xFFD8CAB0);
        }
        if (letters >= 5) {
            g.fill(x + 6, y + 3, x + 18, y + 11, 0xFFE6D2A6);
            g.fill(x + 11, y + 5, x + 13, y + 7, 0xFF7A1F18);
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= mailboxX() - 2 && mouseX <= mailboxX() + 34
                && mouseY >= mailboxY() - 2 && mouseY <= mailboxY() + 26) {
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
