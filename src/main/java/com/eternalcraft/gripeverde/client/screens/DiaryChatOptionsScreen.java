package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
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
        PaperTexture.drawTornSheet(g, this.panelX, this.panelY, this.panelW, this.panelH, PaperTexture.PaperKind.CORRESPONDENCE);

        // Titulo
        String titleStr = DiaryChrome.fitCached(this.font,
            DiaryChrome.tr("options.chat.title"), Math.max(0, this.panelW - 28));
        int titleX = this.width / 2;
        int titleY = this.panelY + 10;
        {
        int _tw = DiaryChrome.widthCached(this.font, titleStr);
        g.drawString(this.font, titleStr, titleX - _tw / 2,     titleY,     DiaryChrome.INK_TITLE,  false);
        }

        // Subtitulo
        String subtitle = "- " + DiaryChrome.tr("gripeverde.options.chat.subtitle") + " -";
        // v0.304.0: el subtitulo se cortaba antes de la chapa REG. V.
        subtitle = DiaryChrome.fitCached(this.font, subtitle, Math.max(0, this.panelW - 150));
        int subWC = DiaryChrome.widthCached(this.font, subtitle);
        g.drawString(this.font, subtitle, titleX - subWC / 2, titleY + 12, DiaryChrome.INK_SUBTITLE, false);

        // v0.310.0: en el renglon de la cita va el estado del chat en palabras.
        // Antes iba al pie, justo debajo del boton de volver, y se encimaban.
        String headerLine = "";
        try {
            headerLine = chatSummaryLine();
        } catch (Throwable ignored) {
        }
        if (headerLine.isEmpty()) {
            headerLine = getRotatingChatFlavor();
        }
        String shownLine = DiaryChrome.fitCached(this.font, headerLine, Math.max(0, this.panelW - 150));
        int flvWC = DiaryChrome.widthCached(this.font, shownLine);
        g.drawString(this.font, shownLine, (this.width - flvWC) / 2, titleY + 24,
            DiaryChrome.INK_FLAVOR, false);

        // Divisor superior
        int divY1 = titleY + 42;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY1);

        // Divisor inferior + esquineros
        int divY2 = this.panelY + this.panelH - 38;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);
        DiaryChrome.drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);
        DiaryChrome.drawRegistryTag(g, this.panelX, this.panelY, this.panelW, this.panelH, "REG. V");

        // Lista vanilla sin gradientes blancos ni bandejas extra
        boolean listClipped = false;
        if (this.list != null) {
            // v0.352.0: la lista del juego se recorta a la hoja; sus filas se
            // dimensionan a lo ancho de la pantalla y asomaban fuera del papel.
            try {
                g.enableScissor(this.panelX + 2, this.panelY + 2,
                    this.panelX + this.panelW - 2, this.panelY + this.panelH - 2);
                listClipped = true;
            } catch (Throwable ignored) {
            }
            this.list.render(g, mouseX, mouseY, partialTick);
            // v0.343.0: el gris del juego se acompana al sepia de la hoja.
            DiaryChrome.washVanillaRows(g, this.list);
            // v0.344.0: y encima, la ficha de registro con nuestra letra.
            DiaryChrome.paintOptionRowsInk(g, this.font, this.list);
            DiaryChrome.drawStyledScrollThumbInPanel(g, this.list, this.panelX + this.panelW);
            if (listClipped) {
                try {
                    g.disableScissor();
                } catch (Throwable ignored) {
                }
            }
        }

        // v0.310.0: el resumen subio a la cabecera; aqui solo quedaban el boton
        // de volver y su sombra.

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

    /** v0.282.0: opacidad, ancho y escala del chat, dichos como se leen. */
    private String chatSummaryLine() {
        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        try {
            int opacity = (int) Math.round(this.options.chatOpacity().get() * 100.0);
            int width = (int) Math.round(this.options.chatWidth().get() * 100.0);
            int scale = (int) Math.round(this.options.chatScale().get() * 100.0);
            return (en ? "opacity " : "opacidad ") + opacity + "%"
                + (en ? "  ·  width " : "  ·  ancho ") + width + "%"
                + (en ? "  ·  scale " : "  ·  escala ") + scale + "%";
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
        boolean english = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        return english ? CHAT_FLAVOR_POOL_EN : CHAT_FLAVOR_POOL_ES;
    }

}
