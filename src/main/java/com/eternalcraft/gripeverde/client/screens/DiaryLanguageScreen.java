package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.FlagRenderer;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DiaryLanguageScreen extends Screen {

    private static final int PANEL_MAX_W = 420;
    private static final int PANEL_MAX_H = 300;
    private static final int PANEL_PAD = 18;
    private static final int FOOTER_GAP = 12;
    private static final int FOOTER_BTN_H = 22;
    private static final int FOOTER_BTN_GAP = 10;
    private static final int LIST_ITEM_H = 20;
    private static final int FLAG_SLOT    = 22;

    // -1 indica primera apertura; cero también es una posición que restaurar.
    private static double lastScrollAmount = -1.0;

    // Tonos de tinta especificos de la lista de idiomas (no van al DiaryChrome
    // porque solo tienen sentido aqui)
    private static final int INK_ACTIVE  = 0xFF1A0F08;
    private static final int INK_PENDING = 0xFF3A230F;
    private static final int INK_NORMAL  = 0xFF6B4A2E;

    private final Screen parent;
    private final Options options;
    private final LanguageManager languageManager;

    private LanguageList languageList;
    private final List<LanguageEntry> languageEntries = new ArrayList<>();
    private DiaryButton unicodeButton;
    private final SmoothScroll smoothScroll = new SmoothScroll(0.58D, 0.22D);
    private boolean applyingLanguage = false;
    private boolean languageReloadFailed;
    private String searchBuffer = "";
    private long searchBufferUntilMs = 0L;

    private String appliedLanguageCode;
    private String pendingLanguageCode;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int listX;
    private int listY;
    private int listW;
    private int listH;

    public DiaryLanguageScreen(Screen parent, Options options, LanguageManager languageManager) {
        super(Component.translatable("gripeverde.options.language.title"));
        this.parent = parent;
        this.options = options;
        this.languageManager = languageManager;
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
        double restoredScroll = prepareLanguageRebuild();

        this.panelW = Math.min(PANEL_MAX_W, this.width - 24);
        this.panelH = Math.min(PANEL_MAX_H, this.height - 24);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        int titleY = this.panelY + PANEL_PAD;
        int footerY = this.panelY + this.panelH - PANEL_PAD - FOOTER_BTN_H;

        this.listX = this.panelX + PANEL_PAD;
        this.listY = titleY + 50;
        this.listW = this.panelW - PANEL_PAD * 2;
        this.listH = footerY - FOOTER_GAP - this.listY;
        this.languageEntries.clear();

        this.languageList = new LanguageList(this.minecraft, this.listX, this.listY, this.listW, this.listH);
        this.addWidget(this.languageList);
        // Al redimensionar prima el scroll de esta pantalla sobre el de otra
        // apertura. Dejar a la lista centrar el idioma solo si nunca se guardó.
        if (restoredScroll >= 0.0) {
            this.languageList.setScrollAmount(restoredScroll);
        }
        this.smoothScroll.snapToCurrent(this.languageList);

        int footerW = this.panelW - PANEL_PAD * 2;
        int leftBtnW = (footerW - FOOTER_BTN_GAP) / 2;
        int rightBtnW = footerW - leftBtnW - FOOTER_BTN_GAP;

        int unicodeX = this.panelX + PANEL_PAD;
        int backX = unicodeX + leftBtnW + FOOTER_BTN_GAP;

        this.unicodeButton = this.addRenderableWidget(new DiaryButton(
                unicodeX, footerY, leftBtnW, FOOTER_BTN_H,
                Component.empty(),
                DiaryButton.Icon.NONE,
                DiaryButton.Variant.SECONDARY,
                this::toggleUnicode
        ));
        updateUnicodeButtonLabel();

        this.addRenderableWidget(new DiaryButton(
                backX, footerY, rightBtnW, FOOTER_BTN_H,
                Component.translatable("gripeverde.options.button.done"),
                DiaryButton.Icon.DOOR,
                DiaryButton.Variant.PRIMARY,
                this::applyAndClose
        ));
    }

    /** Conservar la edición pendiente y el viewport al reconstruir los widgets. */
    private double prepareLanguageRebuild() {
        double restoredScroll = this.languageList != null
            ? this.languageList.getScrollAmount() : lastScrollAmount;
        this.appliedLanguageCode = this.languageManager.getSelected();
        if (this.pendingLanguageCode == null
                || !this.languageManager.getLanguages().containsKey(this.pendingLanguageCode)) {
            this.pendingLanguageCode = this.appliedLanguageCode;
        }
        // El panel puede mantener su ancho máximo mientras cambia el centro
        // de la ventana. Invalidar también las coordenadas de la cabecera.
        this.headerFont = null;
        return restoredScroll;
    }

    @Override
    public void tick() {
        super.tick();
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
        this.smoothScroll.tick(this.languageList);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
        DiaryChrome.drawPanelShadow(g, panelX, panelY, panelW, panelH);
        PaperTexture.drawTornSheet(g, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.ARCHIVE);

        int titleY = this.panelY + PANEL_PAD;
        int cx = this.width / 2;
        {
            refreshHeaderCache();
            g.drawString(this.font, this.headerTitle, this.headerTitleX, titleY, DiaryChrome.INK_TITLE, false);
            g.drawString(this.font, this.headerSubtitle, this.headerSubX, titleY + 12, DiaryChrome.INK_SUBTITLE, false);
            g.drawString(this.font, this.headerFlavor, this.headerFlavorX, titleY + 24, DiaryChrome.INK_FLAVOR, false);

            int divY1 = titleY + 42;
            DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY1);
            // v0.306.0: fuera el folio del pie; caia sobre el boton Volver.

        }

        int frameX = this.listX - 4;
        int frameY = this.listY - 4;
        int frameW = this.listW + 8;
        int frameH = this.listH + 8;

        // v0.232.0: la lista se lee sobre su propio recorte de papel, con los
        // bordes comidos y una sombra corta; el marco de tinta de 1 px se fue
        // con el, porque cerraba la hoja como una cartulina.
        g.fill(frameX + 2, frameY + frameH - 2, frameX + frameW + 2, frameY + frameH + 1, 0x1E302315);
        PaperTexture.drawTornSheet(g, frameX, frameY, frameW, frameH,
            PaperTexture.PaperKind.CORRESPONDENCE);

        if (this.languageList != null) {
            this.languageList.render(g, mouseX, mouseY, partialTick);
        }

        int divY2 = this.panelY + this.panelH - PANEL_PAD - FOOTER_BTN_H - 6;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);
        DiaryChrome.drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);
        DiaryChrome.drawRegistryTag(g, this.panelX, this.panelY, this.panelW, this.panelH, "REG. IV");

        super.render(g, mouseX, mouseY, partialTick);

        drawSearchBuffer(g);
        if (this.applyingLanguage) {
            drawApplyingOverlay(g);
        }

        // v0.16.0
        try {
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, this.panelY - 12);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {}
    }

    // v0.200.0: la cabecera de la ventana se ajustaba, se medía y se centraba en
    // cada cuadro; ahora se arma sólo cuando cambia el título, el idioma o el
    // ancho del panel.
    private Font headerFont;
    private int headerPanelW = -1;
    private String headerTitleSrc = "";
    private String headerTitle = "";
    private int headerTitleX = 0;
    private String headerSubtitle = "";
    private int headerSubX = 0;
    private String headerFlavor = "";
    private int headerFlavorX = 0;

    private void refreshHeaderCache() {
        String titleSrc = this.title.getString();
        if (this.headerFont == this.font && this.headerPanelW == this.panelW
                && this.headerTitleSrc.equals(titleSrc)) {
            return;
        }
        this.headerFont = this.font;
        this.headerPanelW = this.panelW;
        this.headerTitleSrc = titleSrc;
        int limit = Math.max(0, this.panelW - 28);
        this.headerTitle = DiaryChrome.fitText(this.font, titleSrc, limit);
        this.headerTitleX = (this.width - this.font.width(this.headerTitle)) / 2;
        this.headerSubtitle = DiaryChrome.fitText(this.font,
            "- " + DiaryChrome.tr("gripeverde.options.language.subtitle") + " -", limit);
        this.headerSubX = this.width / 2 - this.font.width(this.headerSubtitle) / 2;
        this.headerFlavor = DiaryChrome.fitText(this.font,
            DiaryChrome.tr("gripeverde.options.language.flavor"), limit);
        this.headerFlavorX = this.width / 2 - this.font.width(this.headerFlavor) / 2;
    }

    /**
     * v0.364.0: fuera la cuenta de lenguas del margen («143 lenguas» / «ahora:
     * Esp…»). Se cortaba a media palabra contra el bloque centrado de la
     * cabecera y ensuciaba la hoja; el idioma en uso ya se lee en la lista.
     */

    private void drawSearchBuffer(GuiGraphics g) {
        if (!this.languageReloadFailed
                && (this.searchBuffer.isEmpty() || System.currentTimeMillis() > this.searchBufferUntilMs)) return;
        boolean english = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        String msg = this.languageReloadFailed
            ? (english ? "Reload failed; Done retries" : "Fallo al recargar; Listo reintenta")
            : (english ? "Search: " : "Buscar: ") + this.searchBuffer;
        msg = DiaryChrome.fitText(this.font, msg, Math.max(1, this.panelW - PANEL_PAD * 2 - 14));
        int w = this.font.width(msg) + 14;
        int x = this.panelX + this.panelW - w - PANEL_PAD;
        int y = this.listY - 16;
        g.fill(x, y, x + w, y + 12, 0xAA2A1810);
        g.fill(x, y + 11, x + w, y + 12, 0xAA8E6A35);
        g.drawString(this.font, msg, x + 7, y + 2, 0xFFE8DCB4, false);
    }

    private void drawApplyingOverlay(GuiGraphics g) {
        boolean english = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        String msg = english ? "Applying language..." : "Aplicando idioma...";
        int boxW = Math.max(150, this.font.width(msg) + 34);
        int boxH = 34;
        int bx = (this.width - boxW) / 2;
        int by = (this.height - boxH) / 2;
        g.fill(0, 0, this.width, this.height, 0x66000000);
        g.fill(bx, by, bx + boxW, by + boxH, 0xFFE8D9B4);
        g.fill(bx, by, bx + boxW, by + 1, DiaryChrome.INK_DIVIDER);
        g.fill(bx, by + boxH - 1, bx + boxW, by + boxH, DiaryChrome.INK_DIVIDER);
        g.fill(bx, by, bx + 1, by + boxH, DiaryChrome.INK_DIVIDER);
        g.fill(bx + boxW - 1, by, bx + boxW, by + boxH, DiaryChrome.INK_DIVIDER);
        int mx = bx + (boxW - this.font.width(msg)) / 2;
        int my = by + (boxH - this.font.lineHeight) / 2;
        g.drawString(this.font, msg, mx, my, DiaryChrome.INK_TITLE, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.applyingLanguage) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.applyingLanguage) return true;
        char needle = Character.toLowerCase(codePoint);
        if (!Character.isLetterOrDigit(needle) && needle != '_' && needle != '-') {
            return super.charTyped(codePoint, modifiers);
        }
        long now = System.currentTimeMillis();
        if (now > this.searchBufferUntilMs) {
            this.searchBuffer = "";
        }
        this.searchBuffer += Character.toLowerCase(codePoint);
        this.searchBufferUntilMs = now + 1500L;
        if (selectFirstMatchingLanguage(this.searchBuffer)) {
            UiSounds.playClick();
            return true;
        }
        // If the longer query misses, retry with just the last typed character.
        this.searchBuffer = Character.toString(needle);
        this.searchBufferUntilMs = now + 1500L;
        if (selectFirstMatchingLanguage(this.searchBuffer)) {
            UiSounds.playClick();
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private boolean selectFirstMatchingLanguage(String query) {
        String q = query.toLowerCase(java.util.Locale.ROOT);
        for (LanguageEntry entry : this.languageEntries) {
            String name = entry.info.toComponent().getString().toLowerCase(java.util.Locale.ROOT);
            String code = entry.code.toLowerCase(java.util.Locale.ROOT);
            if (name.startsWith(q) || code.startsWith(q) || name.contains(q)) {
                this.pendingLanguageCode = entry.code;
                this.languageList.selectAndCenter(entry);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.applyingLanguage) return true;
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE && !this.searchBuffer.isEmpty()) {
            this.searchBuffer = this.searchBuffer.substring(0, this.searchBuffer.length() - 1);
            this.searchBufferUntilMs = System.currentTimeMillis() + 1500L;
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
                || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
            if (this.unicodeButton != null && this.getFocused() == this.unicodeButton) {
                if (this.unicodeButton.active && this.unicodeButton.visible) this.unicodeButton.onPress();
                return true;
            }
            this.applyAndClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        // v0.264.0: salir de una hoja suena a puerta, como el resto de la casa.
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.back();
        } catch (Throwable ignored) {
        }
        // Save scroll position for next open
        if (this.languageList != null) {
            lastScrollAmount = this.languageList.getScrollAmount();
        }
        // ESC cancela sin aplicar el idioma pendiente
        this.options.save();
        this.minecraft.setScreen(this.parent);
    }

    private void toggleUnicode() {
        boolean value = !this.options.forceUnicodeFont().get();
        this.options.forceUnicodeFont().set(value);
        this.options.save();
        updateUnicodeButtonLabel();
    }

    private void updateUnicodeButtonLabel() {
        if (this.unicodeButton == null) {
            return;
        }

        boolean enabled = this.options.forceUnicodeFont().get();
        this.unicodeButton.setMessage(
                Component.translatable("options.forceUnicodeFont")
                        .append(": ")
                        .append(Component.translatable(enabled ? "options.on" : "options.off"))
        );
    }

    private void applyAndClose() {
        if (this.applyingLanguage) return;
        if (this.languageList != null) {
            lastScrollAmount = this.languageList.getScrollAmount();
        }

        if (this.pendingLanguageCode != null
                && (this.languageReloadFailed
                    || !Objects.equals(this.pendingLanguageCode, this.languageManager.getSelected()))) {
            this.applyingLanguage = true;
            this.languageReloadFailed = false;
            this.options.languageCode = this.pendingLanguageCode;
            this.languageManager.setSelected(this.pendingLanguageCode);
            this.options.save();
            UiSounds.success();
            // v0.16.0: el cronista nota el cambio de lengua.
            try {
                com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                    com.eternalcraft.gripeverde.client.lore.Marginalia.N_LANGUAGE);
            } catch (Throwable ignored) {}
            try {
                this.minecraft.reloadResourcePacks().whenComplete((ignored, failure) ->
                    this.minecraft.execute(() -> this.finishLanguageReload(failure)));
            } catch (RuntimeException failure) {
                this.finishLanguageReload(failure);
            }
            return;
        }

        this.options.save();
        this.minecraft.setScreen(this.parent);
    }

    private void finishLanguageReload(Throwable failure) {
        this.applyingLanguage = false;
        this.languageReloadFailed = failure != null;
        // Reload completion may arrive after another screen/world took over.
        if (this.minecraft == null || this.minecraft.screen != this) return;
        if (failure != null) {
            UiSounds.error();
            return;
        }
        // Optional audio recovery must not strand the language UI either.
        try {
            com.eternalcraft.gripeverde.client.audio.MenuAmbience.recoverAfterResourceReload();
        } catch (Throwable ignored) {}
        try {
            com.eternalcraft.gripeverde.client.audio.MenuMusic.recoverAfterResourceReload();
        } catch (Throwable ignored) {}
        this.minecraft.setScreen(this.parent);
    }

    private final class LanguageList extends ObjectSelectionList<LanguageEntry> {

        private final int rowW;
        private LanguageList(Minecraft minecraft, int left, int top, int width, int height) {
            super(minecraft, width, height, top, top + height, LIST_ITEM_H);
            this.rowW = width - 10;
            this.setLeftPos(left);
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
            this.setRenderSelection(false);

            LanguageEntry selectedEntry = null;

            for (java.util.Map.Entry<String, LanguageInfo> mapEntry : DiaryLanguageScreen.this.languageManager.getLanguages().entrySet()) {
                LanguageEntry entry = new LanguageEntry(mapEntry.getKey(), mapEntry.getValue());
                DiaryLanguageScreen.this.languageEntries.add(entry);
                this.addEntry(entry);

                if (Objects.equals(mapEntry.getKey(), DiaryLanguageScreen.this.pendingLanguageCode)) {
                    selectedEntry = entry;
                }
            }

            if (selectedEntry != null) {
                this.setSelected(selectedEntry);
                this.centerScrollOn(selectedEntry);
            }

            // v0.16.0: para quien ya encontro bastante, existe una lengua mas.
            // Solo una vez por dia; se esfuma al tocarla.
            try {
                boolean worthy = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.discoveredCount() >= 6;
                long today = java.time.LocalDate.now().toEpochDay();
                boolean taken = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("fog_tongue_day") == (int)today;
                if (worthy && !taken) {
                    LanguageEntry fog = new LanguageEntry();
                    int pos = Math.min(7, this.children().size());
                    this.children().add(pos, fog);
                }
            } catch (Throwable ignored) {}
        }

        @Override
        public void setSelected(LanguageEntry entry) {
            super.setSelected(entry);
            // La navegación vanilla por flechas cambia la selección sin pasar
            // por mouseClicked. Listo debe aplicar esa misma fila visible.
            if (entry != null && !entry.fogTongue && !DiaryLanguageScreen.this.applyingLanguage) {
                DiaryLanguageScreen.this.pendingLanguageCode = entry.code;
            }
        }

        @Override
        public int getRowWidth() {
            return this.rowW;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getRowLeft() + this.getRowWidth() + 4;
        }

        @Override
        public void render(net.minecraft.client.gui.GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            super.render(gfx, mouseX, mouseY, partialTick);
            DiaryChrome.drawScrollThumb(gfx, this.getScrollbarPosition(),
                this.y0, this.y1, this.getScrollAmount(), this.getMaxScroll());
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
            boolean moved = DiaryLanguageScreen.this.smoothScroll.wheel(this, delta, LIST_ITEM_H * 3.4D);
            if (moved) {
                UiSounds.scroll();
            }
            return moved;
        }

        private void selectAndCenter(LanguageEntry entry) {
            this.setSelected(entry);
            this.centerScrollOn(entry);
            DiaryLanguageScreen.this.smoothScroll.snapToCurrent(this);
        }
    }

    private final class LanguageEntry extends ObjectSelectionList.Entry<LanguageEntry> {

        private final String code;
        private final LanguageInfo info;
        private final boolean fogTongue;
        private long lastClickMs = 0L;
        // v0.188.0: la etiqueta y su ancho se arman una vez, no en cada cuadro.
        private final String labelIdle;
        private final String labelPending;
        private final String labelApplied;
        private final int widthIdle;
        private final int widthPending;
        private final int widthApplied;

        private LanguageEntry(String code, LanguageInfo info) {
            this.code = code;
            this.info = info;
            this.fogTongue = false;
            String name = info == null ? "" : info.toComponent().getString();
            this.labelIdle = "  " + name;
            this.labelPending = "> " + name;
            this.labelApplied = "- " + name;
            net.minecraft.client.gui.Font font = DiaryLanguageScreen.this.font;
            this.widthIdle = font == null ? 0 : font.width(this.labelIdle);
            this.widthPending = font == null ? 0 : font.width(this.labelPending);
            this.widthApplied = font == null ? 0 : font.width(this.labelApplied);
        }

        private LanguageEntry() {
            // v0.16.0: la lengua imposible.
            this.code = "fog";
            this.info = null;
            this.fogTongue = true;
            this.labelIdle = "";
            this.labelPending = "";
            this.labelApplied = "";
            this.widthIdle = 0;
            this.widthPending = 0;
            this.widthApplied = 0;
        }

        @Override
        public void render(GuiGraphics g, int index, int top, int left, int rowWidth, int rowHeight,
                           int mouseX, int mouseY, boolean hovered, float partialTick) {

            if (this.fogTongue) {
                // Una lengua que ningun consulado reconoce.
                if (hovered) {
                    g.fill(left + 2, top + 1, left + rowWidth - 2, top + rowHeight - 2, 0x22384028);
                }
                String name = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                    ? "Tongue of the Fog" : "Lengua de la Niebla";
                int pulse = com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.hasActiveWorld()
                    ? 90 : 90 + (int)(Math.sin(System.currentTimeMillis() / 600.0) * 40);
                int w2 = DiaryChrome.widthCached(DiaryLanguageScreen.this.font, name);
                g.drawString(DiaryLanguageScreen.this.font, name,
                    left + FLAG_SLOT + (rowWidth - FLAG_SLOT - w2) / 2,
                    top + (rowHeight - DiaryLanguageScreen.this.font.lineHeight) / 2,
                    (Math.max(30, pulse) << 24) | 0x7A9060, false);
                // Rombo de tinta en lugar de bandera
                int dx = left + 8, dy = top + rowHeight / 2;
                g.fill(dx, dy - 2, dx + 1, dy - 1, 0x885A7038);
                g.fill(dx - 1, dy - 1, dx + 2, dy, 0x885A7038);
                g.fill(dx - 2, dy, dx + 3, dy + 1, 0x885A7038);
                g.fill(dx - 1, dy + 1, dx + 2, dy + 2, 0x885A7038);
                g.fill(dx, dy + 2, dx + 1, dy + 3, 0x885A7038);
                return;
            }

            boolean isPending = Objects.equals(DiaryLanguageScreen.this.pendingLanguageCode, this.code);
            boolean isApplied = Objects.equals(DiaryLanguageScreen.this.appliedLanguageCode, this.code);

            // Fondo de fila seleccionada / hover
            int fill;
            if (isPending) {
                fill = 0x66422810;
            } else if (hovered) {
                fill = 0x33422810;
            } else {
                fill = 0x00000000;
            }
            if (fill != 0) {
                g.fill(left + 2, top + 1, left + rowWidth - 2, top + rowHeight - 2, fill);
            } else if (rowHeight > 10) {
                // v0.189.0: una hebra tenue cierra cada renglon (un solo relleno,
                // solo cuando no esta ni elegido ni bajo el puntero) y la lista
                // de veinticinco lenguas se lee de corrido sin perderse.
                g.fill(left + FLAG_SLOT, top + rowHeight - 2,
                    left + rowWidth - 6, top + rowHeight - 1, 0x1C422810);
            }

            // ── Bandera ──────────────────────────────────────────────────────
            int flagX = left + 3;
            int flagY = top + (rowHeight - FlagRenderer.FLAG_H) / 2;
            FlagRenderer.draw(g, this.code, flagX, flagY);

            // ── Texto (centrado en la zona derecha del slot de bandera) ──────
            int textColor;
            if (isPending) {
                textColor = INK_ACTIVE;
            } else if (isApplied) {
                textColor = INK_PENDING;
            } else {
                textColor = INK_NORMAL;
            }

            // v0.188.0: etiqueta y ancho ya calculados en el constructor.
            String label = isPending ? this.labelPending
                : isApplied ? this.labelApplied : this.labelIdle;
            int textW = isPending ? this.widthPending
                : isApplied ? this.widthApplied : this.widthIdle;
            if (textW == 0) textW = DiaryChrome.widthCached(DiaryLanguageScreen.this.font, label);

            // Zona de texto: desde left+FLAG_SLOT hasta left+rowWidth
            int textZoneLeft  = left + FLAG_SLOT;
            int textZoneWidth = rowWidth - FLAG_SLOT;
            int textX  = textZoneLeft + (textZoneWidth - textW) / 2;
            int textY  = top + (rowHeight - DiaryLanguageScreen.this.font.lineHeight) / 2;

            g.drawString(DiaryLanguageScreen.this.font, label, textX, textY, textColor, false);

            if (isApplied) {
                int markColor = 0xAA422810;
                g.fill(left + rowWidth - 12, top + 5, left + rowWidth - 11, top + 12, markColor);
                g.fill(left + rowWidth - 11, top + 11, left + rowWidth - 7, top + 12, markColor);
            }

            if (hovered || isPending) {
                String codeText = this.code.toUpperCase(java.util.Locale.ROOT);
                // v0.205.0: el ancho del código de la lengua se mide una vez.
                int codeW = DiaryChrome.widthCached(DiaryLanguageScreen.this.font, codeText);
                int codeX = left + rowWidth - codeW - 16;
                if (codeX > textZoneLeft + textW + 8) {
                    int codeColor = isPending ? 0xAA1A0F08 : 0x886B4A2E;
                    g.drawString(DiaryLanguageScreen.this.font, codeText, codeX, textY, codeColor, false);
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                if (this.fogTongue) {
                    // v0.16.0: la lengua imposible no se habla: se esfuma.
                    try {
                        boolean first = com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                            com.eternalcraft.gripeverde.client.lore.Discoveries.FOG_TONGUE);
                        long today = java.time.LocalDate.now().toEpochDay();
                        com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.setCounter("fog_tongue_day", (int)today);
                        com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                            com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_AMBIENT_OCCASIONAL.get(),
                            0.24f, first ? 0.55f : 0.70f);
                        DiaryLanguageScreen.this.languageList.children().remove(this);
                    } catch (Throwable ignored) {}
                    return true;
                }
                long now = System.currentTimeMillis();
                boolean doubleClick = Objects.equals(DiaryLanguageScreen.this.pendingLanguageCode, this.code)
                    && now - this.lastClickMs < 420L;
                this.lastClickMs = now;
                DiaryLanguageScreen.this.pendingLanguageCode = this.code;
                DiaryLanguageScreen.this.languageList.setSelected(this);
                UiSounds.playClick();
                if (doubleClick) {
                    DiaryLanguageScreen.this.applyAndClose();
                }
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            // v0.386.0: el lector de pantalla leia "punto punto punto"; ahora
            // dice el mismo nombre que lleva la fila.
            if (this.fogTongue) {
                return Component.literal(com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                    ? "Tongue of the Fog" : "Lengua de la Niebla");
            }
            return this.info.toComponent();
        }
    }
}
