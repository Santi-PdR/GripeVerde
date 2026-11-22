package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import com.eternalcraft.gripeverde.client.widgets.DiaryTextField;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

/**
 * Conexion directa a un servidor por IP.
 * Reimplementacion completa desde Screen (no subclasea DirectJoinServerScreen vanilla).
 */
public class DiaryDirectJoinScreen extends Screen {

    private static final int PANEL_MAX_W = 340;
    private static final int PANEL_MAX_H = 180;
    private static final Component ENTER_IP_LABEL = Component.translatable("addServer.enterIp");

    private final Screen parent;
    private final BooleanConsumer callback;
    private final ServerData serverData;

    private DiaryTextField ipEdit;
    private DiaryButton selectButton;
    private boolean justOpened = true;

    private int panelWidth() {
        return Math.min(PANEL_MAX_W, Math.max(136, this.width - 24));
    }

    private int panelHeight() {
        return Math.min(PANEL_MAX_H, Math.max(120, this.height - 24));
    }

    private int ipFieldOffset() {
        return Math.min(76, Math.max(54, panelHeight() - 76));
    }

    private static String ellipsizeTo(net.minecraft.client.gui.Font font, String text, int maxWidth){
        // v0.384.0: sin puntos suspensivos; la politica vive en un solo sitio.
        return com.eternalcraft.gripeverde.client.render.DiaryChrome.shorten(font, text, maxWidth);
    }

    public DiaryDirectJoinScreen(Screen parent, BooleanConsumer callback, ServerData serverData) {
        super(Component.translatable("selectServer.direct"));
        this.parent = parent;
        this.callback = callback;
        this.serverData = serverData;
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
        int panelW = panelWidth();
        int panelH = panelHeight();
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int fieldW = Math.max(64, panelW - 48);
        int fieldX = panelX + (panelW - fieldW) / 2;
        int fieldH = 20;
        int ipY = panelY + ipFieldOffset();

        // IP field
        this.ipEdit = new DiaryTextField(this.font, fieldX, ipY, fieldW, fieldH, ENTER_IP_LABEL);
        this.ipEdit.setMaxLength(128);
        String initial = (this.serverData != null && this.serverData.ip != null) ? this.serverData.ip : "";
        // Vanilla usa "lastServer" del options, aca vamos con lo que traiga ServerData
        if (initial.isEmpty()) {
            initial = this.minecraft.options.lastMpIp;
            if (initial == null) initial = "";
        }
        this.ipEdit.setValue(initial);
        this.ipEdit.setResponder(s -> this.updateSelectButton());
        this.addRenderableWidget(this.ipEdit);
        this.setInitialFocus(this.ipEdit);

        // Botones inferiores
        int btnH = 20;
        int gap = Math.min(12, Math.max(4, panelW - 24 - 112));
        int btnW = Math.max(48, Math.min(100, (panelW - 24 - gap) / 2));
        int btnY = panelY + panelH - btnH - 16;
        int totalW = btnW * 2 + gap;
        int startX = panelX + (panelW - totalW) / 2;

        this.selectButton = new DiaryButton(startX, btnY, btnW, btnH,
                Component.translatable("selectServer.select"),
                DiaryButton.Icon.HAND, DiaryButton.Variant.PRIMARY,
                this::onSelect);
        this.selectButton.setTooltip(Tooltip.create(Component.translatable("selectServer.select")));
        this.addRenderableWidget(this.selectButton);

        DiaryButton cancel = new DiaryButton(startX + btnW + gap, btnY, btnW, btnH,
                Component.translatable("gui.cancel"),
                DiaryButton.Icon.DOOR, DiaryButton.Variant.SECONDARY,
                this::onCancel);
        cancel.setTooltip(Tooltip.create(Component.translatable("gui.cancel")));
        this.addRenderableWidget(cancel);

        this.updateSelectButton();
    }

    private void updateSelectButton() {
        if (this.selectButton == null || this.ipEdit == null) return;
        String ip = this.ipEdit.getValue() == null ? "" : this.ipEdit.getValue().trim();
        // v0.263.0: una direccion con espacios o simbolos raros no entra; el
        // boton se apaga antes de que el juego intente conectarse a la nada.
        boolean ok = !ip.isEmpty() && DiaryChrome.looksLikeAddress(ip);
        this.selectButton.active = ok;
        this.selectButton.setTooltip(Tooltip.create(Component.literal(ok
            ? (com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                ? "Join " + ip : "Entrar a " + ip)
            : Component.translatable("selectServer.select").getString())));
    }

    private long unmappedWarnUntilMs = 0L;

    // v0.284.0: las direcciones recordadas se pintan como fichas y se tocan.
    private final java.util.List<Integer> recentXs = new java.util.ArrayList<>();
    private final java.util.List<Integer> recentWs = new java.util.ArrayList<>();
    private final java.util.List<String> recentShown = new java.util.ArrayList<>();
    private int recentY = -1;

    private void onSelect() {
        String ip = this.ipEdit.getValue() == null ? "" : this.ipEdit.getValue().trim();
        // v0.16.0: la direccion que no esta en ningun mapa.
        if ("13.13.13.13".equals(ip)) {
            this.unmappedWarnUntilMs = System.currentTimeMillis() + 6000L;
            try {
                boolean first = com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                    com.eternalcraft.gripeverde.client.lore.Discoveries.ADDRESS_13);
                com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                    com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_BELL.get(), 0.20f, 0.50f);
                if (first) {
                    com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                        com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_AMBIENT_OCCASIONAL.get(), 0.20f, 0.58f);
                }
            } catch (Throwable ignored) {}
            return;
        }
        this.serverData.ip = ip;
        // v0.284.0: la casa recuerda por donde se entro.
        try {
            com.eternalcraft.gripeverde.client.integration.RecentAddresses.push(ip);
        } catch (Throwable ignored) {
        }
        this.minecraft.options.lastMpIp = ip;
        this.minecraft.options.save();
        this.callback.accept(true);
    }

    private void onCancel() {
        this.callback.accept(false);
    }

    /** v0.284.0: tocar una direccion recordada la pone en el campo. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.recentY >= 0) {
            for (int i = 0; i < this.recentXs.size(); i++) {
                int x = this.recentXs.get(i);
                int w = this.recentWs.get(i);
                if (mouseX >= x - 1 && mouseX <= x + w + 1
                        && mouseY >= this.recentY - 2 && mouseY <= this.recentY + 11) {
                    try {
                        this.ipEdit.setValue(this.recentShown.get(i));
                        this.setFocused(this.ipEdit);
                        this.ipEdit.setFocused(true);
                        com.eternalcraft.gripeverde.client.audio.UiSounds.type();
                        updateSelectButton();
                    } catch (Throwable ignored) {
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (keyCode == 257 || keyCode == 335) { // ENTER, KP_ENTER
            if (this.selectButton.active) {
                this.onSelect();
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick() {
        if (this.ipEdit != null) this.ipEdit.tick();
        justOpened = false;
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (justOpened) return true; // v0.21.26: ignorar tecla filtrada de la pantalla anterior
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void resize(net.minecraft.client.Minecraft mc, int w, int h) {
        String ipVal = this.ipEdit != null ? this.ipEdit.getValue() : "";
        super.resize(mc, w, h);
        this.ipEdit.setValue(ipVal);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        super.render(gfx, mouseX, mouseY, partial);
        // v0.383.0: el juego primero (fondo, rotulos y campos del vanilla);
        // despues el papel de la casa lo tapa todo y, al final, se repiten
        // las fichas sobre la hoja: asi no queda texto blanco de nadie.
        // Fondo: escena viva del Title
        try {
            com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(gfx, this.width, this.height);
                } catch (Throwable ignored) {}

        // Overlay atenuador

        // Panel pergamino
        int panelW = panelWidth();
        int panelH = panelHeight();
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        DiaryChrome.drawPanelShadow(gfx, panelX, panelY, panelW, panelH);
        PaperTexture.drawTornSheet(gfx, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.OFFICIAL);

        // Cosmetica
        DiaryChrome.drawCornerOrnaments(gfx, panelX, panelY, panelW, panelH);
        DiaryChrome.drawRegistryTag(gfx, panelX, panelY, panelW, panelH, "REG. XII");

        // Titulo
        int titleY = panelY + (panelH < 160 ? 12 : 18);
        int centerX = panelX + panelW / 2;
        String titleStr = ellipsizeTo(this.font, this.title.getString(), Math.max(48, panelW - 28));
        int titleW = DiaryChrome.widthCached(this.font, titleStr);
        gfx.drawString(this.font, titleStr, centerX - titleW / 2, titleY, DiaryChrome.INK_TITLE, false);

        // Subtitulo
        String subtitle = "- " + DiaryChrome.tr("gripeverde.direct.subtitle") + " -";
        subtitle = ellipsizeTo(this.font, subtitle, Math.max(48, panelW - 28));
        int subW = DiaryChrome.widthCached(this.font, subtitle);
        gfx.drawString(this.font, subtitle, centerX - subW / 2, titleY + 12, DiaryChrome.INK_SUBTITLE, false);

        // Divisor con rombo
        DiaryChrome.drawInkDivider(gfx, panelX + 20, titleY + 26, panelW - 40);
        DiaryChrome.drawDivisorLozenge(gfx, centerX, titleY + 26);

        // Label IP
        String ipLbl = ellipsizeTo(this.font, ENTER_IP_LABEL.getString(), Math.max(48, panelW - 48));
        gfx.drawString(this.font, ipLbl, panelX + 24, panelY + ipFieldOffset() - 14, DiaryChrome.INK_SUBTITLE, false);

        // v0.263.0: debajo del campo, como se parte la direccion: anfitrion y
        // puerto. Si no tiene forma de direccion, se dice en rojo y no se entra.
        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        String typed = this.ipEdit == null || this.ipEdit.getValue() == null ? "" : this.ipEdit.getValue().trim();
        int fieldY = panelY + ipFieldOffset();
        int noteY = fieldY + 26;
        int btnTop = panelY + panelH - 20 - 16;
        this.recentXs.clear();
        this.recentWs.clear();
        this.recentShown.clear();
        this.recentY = -1;
        // v0.284.0: si no se escribio nada, la hoja ofrece las ultimas puertas.
        if (typed.isEmpty() && noteY + 10 < btnTop && panelW >= 200) {
            try {
                java.util.List<String> recents =
                    com.eternalcraft.gripeverde.client.integration.RecentAddresses.list();
                if (!recents.isEmpty()) {
                    String head = en ? "recent: " : "recientes: ";
                    // v0.319.0: la fila de recientes se corta en el ancho de la
                    // hoja. Antes, con seis o siete puertas, se salia del papel.
                    // Tambien se acorta la primera direccion si ella sola supera
                    // el ancho; se conserva el valor completo al tocarla.
                    int maxW = Math.max(60, panelW - 48);
                    java.util.List<String> fitAddresses = new java.util.ArrayList<>();
                    java.util.List<String> fitLabels = new java.util.ArrayList<>();
                    int total = DiaryChrome.widthCached(this.font, head);
                    for (String r : recents) {
                        int gapW = fitLabels.isEmpty() ? 0 : 10;
                        int available = maxW - total - gapW;
                        if (available <= 0) break;
                        String label = ellipsizeTo(this.font, r, available);
                        int labelW = DiaryChrome.widthCached(this.font, label);
                        if (label.isEmpty() || labelW <= 0) break;
                        total += gapW + labelW;
                        fitAddresses.add(r);
                        fitLabels.add(label);
                        if (!label.equals(r)) break;
                    }
                    if (!fitLabels.isEmpty()) {
                        int hx = Math.max(panelX + 24, centerX - total / 2);
                        gfx.drawString(this.font, head, hx, noteY, DiaryChrome.INK_FLAVOR, false);
                        int cx2 = hx + DiaryChrome.widthCached(this.font, head);
                        for (int i = 0; i < fitLabels.size(); i++) {
                            if (i > 0) cx2 += 10;
                            String label = fitLabels.get(i);
                            int rw = DiaryChrome.widthCached(this.font, label);
                            gfx.drawString(this.font, label, cx2, noteY, DiaryChrome.INK_SUBTITLE, false);
                            gfx.fill(cx2, noteY + 9, cx2 + rw, noteY + 10,
                                DiaryChrome.INK_DIVIDER);
                            this.recentXs.add(cx2);
                            this.recentWs.add(rw);
                            this.recentShown.add(fitAddresses.get(i));
                            cx2 += rw;
                        }
                        this.recentY = noteY;
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        if (noteY + 10 < btnTop && panelW >= 200 && this.recentY < 0) {
            String note;
            int ink;
            if (typed.isEmpty()) {
                note = en ? "host or host:port" : "anfitrion o anfitrion:puerto";
                ink = DiaryChrome.INK_FLAVOR;
            } else if (!DiaryChrome.looksLikeAddress(typed)) {
                note = en ? "that is not shaped like an address" : "eso no tiene forma de direccion";
                ink = 0xFF8E2A20;
            } else {
                note = DiaryChrome.addressReadout(typed, en);
                ink = DiaryChrome.INK_FLAVOR;
            }
            note = ellipsizeTo(this.font, note, Math.max(48, panelW - 32));
            int nw = DiaryChrome.widthCached(this.font, note);
            gfx.drawString(this.font, note, centerX - nw / 2, noteY, ink, false);
        }

        // v0.349.0: el pie de teclas caia sobre el canto roto de la hoja. Ahora
        // va encima de las fichas y, si el cuerpo no da, se calla.
        if (panelH >= 150) {
            int hintY = panelY + panelH - 20 - 16 - 12;
            if (hintY > noteY + 11) {
                String keys = en ? "Enter joins  ·  Esc back"
                    : "Enter entra  ·  Esc vuelve";
                keys = ellipsizeTo(this.font, keys, Math.max(48, panelW - 32));
                int kw = DiaryChrome.widthCached(this.font, keys);
                gfx.drawString(this.font, keys, centerX - kw / 2, hintY,
                    DiaryChrome.INK_FLAVOR, false);
            }
        }

        // Widgets
        
        // v0.16.0: aviso de la direccion sin mapa
        try {
            if (System.currentTimeMillis() < this.unmappedWarnUntilMs) {
                String warn = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                    ? "That address is on no map. The door is not there anymore."
                    : "Esa direccion no esta en ningun mapa. La puerta ya no esta ahi.";
                warn = ellipsizeTo(this.font, warn, Math.max(48, panelW - 24));
                int ww = DiaryChrome.widthCached(this.font, warn);
                long remain = this.unmappedWarnUntilMs - System.currentTimeMillis();
                int alpha = remain < 900L ? (int)(210 * remain / 900L) : 210;
                int warnY = panelY + Math.min(ipFieldOffset() + 24, panelH - 52);
                gfx.drawString(this.font, warn, centerX - ww / 2, warnY,
                    (alpha << 24) | 0xB8A888, false);
            }
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(gfx, this.width);
        } catch (Throwable ignored) {}
        // v0.383.0: las fichas y los campos, encima de la hoja.
        com.eternalcraft.gripeverde.client.render.DiaryChrome.renderWidgets(
            this, gfx, mouseX, mouseY, partial);
    }

    @Override
    public void renderBackground(GuiGraphics gfx) {
        // Bypass total del dirt vanilla
    }

    @Override
    public void onClose() {
        // v0.264.0: salir de una hoja suena a puerta, como el resto de la casa.
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.back();
        } catch (Throwable ignored) {
        }
        this.callback.accept(false);
    }
}
