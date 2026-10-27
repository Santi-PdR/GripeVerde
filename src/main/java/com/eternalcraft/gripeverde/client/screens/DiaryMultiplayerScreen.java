package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.lang.reflect.Field;

/**
 * Pantalla principal de multiplayer del cronista.
 * Subclasea JoinMultiplayerScreen para preservar toda la logica de lista
 * (favicons, ping, LAN scan, MOTD, tooltips) y solo reemplaza cosmetica + botones.
 */
public class DiaryMultiplayerScreen extends JoinMultiplayerScreen {

    // Server oficial del mod - siempre presente en la lista.
    private static final String PINNED_NAME_EN = "Ghoul Outbreak Official";
    private static final String PINNED_NAME_ES = "Ghoul Outbreak Oficial";
    private static final String PINNED_NAME_KEY = "gripeverde.multiplayer.pinned_server";
    private static final String PINNED_IP   = "Outbreak.exaroton.me:24652";
    private static final String PREVIOUS_PINNED_IP = "servercitoGhouls.exaroton.me:24652";

    private static String localizedPinnedName() {
        return Component.translatable(PINNED_NAME_KEY).getString();
    }

    private static boolean isPinnedName(String name) {
        return PINNED_NAME_EN.equals(name) || PINNED_NAME_ES.equals(name)
            || localizedPinnedName().equals(name);
    }

    private static boolean isPinnedServer(ServerData server) {
        return server != null && (isPinnedName(server.name)
            || PINNED_IP.equals(server.ip) || PREVIOUS_PINNED_IP.equals(server.ip));
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    private int frameLogCounter = 0;

    private static final int PANEL_MARGIN_H = 40;
    private static final int PANEL_MARGIN_V = 40;
    private static final int LIST_TOP_OFFSET = 50;
    // Espacio inferior reservado para las 2 filas de botones + divisor
    private static final int LIST_BOTTOM_OFFSET = 76;

    private final Screen parent;

    // Referencias resueltas por reflection al vanilla
    private ServerSelectionList vanillaList;
    private ServerList vanillaServers;
    private Button vanillaSelectBtn;
    private Button vanillaEditBtn;
    private Button vanillaDeleteBtn;
    // Cached after list layout changes; render-time row decoration must not scan fields reflectively.
    private int vanillaItemHeight;
    private int vanillaListY0;
    private int vanillaHeaderHeight;

    // Nuestros botones Diary
    private DiaryButton diarySelect;
    private DiaryButton diaryDirect;
    private DiaryButton diaryAdd;
    private DiaryButton diaryEdit;
    private DiaryButton diaryDelete;
    private DiaryButton diaryRefresh;
    private DiaryButton diaryCancel;
    private final SmoothScroll smoothScroll = new SmoothScroll();
    private DiaryButton tooltipCandidate;
    private long tooltipCandidateSince;

    public DiaryMultiplayerScreen(Screen parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        // Resolver referencias vanilla via reflection cascada
        this.vanillaList = resolveField(ServerSelectionList.class, "serverSelectionList", "f_99673_");
        this.vanillaServers = resolveField(ServerList.class, "servers", "f_99677_");
        this.vanillaSelectBtn = resolveField(Button.class, "selectButton", "f_99678_");
        this.vanillaEditBtn = resolveField(Button.class, "editButton", "f_99679_");
        this.vanillaDeleteBtn = resolveField(Button.class, "deleteButton", "f_99680_");

        // Ajustar la lista visual (SIN cambiar width, respetando R-VANILLA-LIST-COORDS)
        if (this.vanillaList != null) {
            try {
                this.vanillaList.setRenderBackground(false);
                this.vanillaList.setRenderTopAndBottom(false);
                // Ajustar solo alto/posicion vertical, dejar width intacto
                int listTop = PANEL_MARGIN_V + LIST_TOP_OFFSET;
                int listBottom = this.height - PANEL_MARGIN_V - LIST_BOTTOM_OFFSET;
                this.vanillaList.updateSize(this.width, this.height, listTop, listBottom);
                this.smoothScroll.snapToCurrent(this.vanillaList);
            } catch (Throwable ignored) {}
        }
        cacheVanillaListMetrics();

        // Ocultar los 7 botones vanilla (los que sabemos + los descubiertos por scan)
        hideVanillaButtons();

        // Crear nuestros 7 DiaryButton
        buildDiaryButtons();

        // Asegurar que el server oficial este siempre presente.
        ensurePinnedServer();
        selectPinnedServerIfNothingIsSelected();
        updateDiaryButtonStates();
    }

    /**
     * Keeps the official server entry available and at the top of the list.
     * Migrates the last built-in address but preserves any address the player edited.
     */
    private void ensurePinnedServer() {
        if (this.vanillaServers == null) return;
        try {
            int pinnedIndex = -1;
            boolean changed = false;
            for (int i = 0; i < this.vanillaServers.size(); i++) {
                ServerData server = this.vanillaServers.get(i);
                if (server == null || (!isPinnedName(server.name)
                        && !PINNED_IP.equals(server.ip)
                        && !PREVIOUS_PINNED_IP.equals(server.ip))) continue;
                pinnedIndex = i;
                if (PREVIOUS_PINNED_IP.equals(server.ip)) {
                    server.ip = PINNED_IP;
                    changed = true;
                }
                if (isPinnedName(server.name)) {
                    String displayName = localizedPinnedName();
                    if (!displayName.equals(server.name)) {
                        server.name = displayName;
                        changed = true;
                    }
                }
                break;
            }

            if (pinnedIndex < 0) {
                this.vanillaServers.add(new ServerData(localizedPinnedName(), PINNED_IP, false), false);
                pinnedIndex = this.vanillaServers.size() - 1;
                changed = true;
            }

            // Keep the official server reachable without scrolling a long list.
            if (pinnedIndex > 0) {
                this.vanillaServers.swap(pinnedIndex, 0);
                changed = true;
            }

            if (changed) {
                this.vanillaServers.save();
                refreshVisibleServerEntries();
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Preselect the official server only when the player has no current selection.
     * Joining still requires an explicit press of the Join button.
     */
    private void selectPinnedServerIfNothingIsSelected() {
        if (this.vanillaList == null || this.vanillaList.getSelected() != null) return;
        try {
            for (var entry : this.vanillaList.children()) {
                ServerData server = extractServerDataFromEntry(entry);
                if (isPinnedServer(server)) {
                    this.vanillaList.setSelected(entry);
                    return;
                }
            }
        } catch (Throwable ignored) {}
    }

    private void refreshVisibleServerEntries() {
        if (this.vanillaList == null || this.vanillaServers == null) return;
        try {
            java.lang.reflect.Method method = null;
            try {
                method = ServerSelectionList.class.getDeclaredMethod(
                    "updateOnlineServers", ServerList.class);
            } catch (NoSuchMethodException ignored) {}
            if (method == null) {
                try {
                    method = ServerSelectionList.class.getDeclaredMethod(
                        "m_99797_", ServerList.class);
                } catch (NoSuchMethodException ignored) {}
            }
            if (method != null) {
                method.setAccessible(true);
                method.invoke(this.vanillaList, this.vanillaServers);
            }
        } catch (Throwable ignored) {}
    }

    private void hideVanillaButtons() {
        // Ocultar CUALQUIER AbstractWidget vanilla (Button, EditBox, CycleButton, etc.)
        // que no sea DiaryButton. Ojo con ServerSelectionList: NO tocar.
        for (var widget : this.renderables) {
            if (widget instanceof AbstractWidget aw && !(widget instanceof DiaryButton)) {
                // Excepcion: no tocar la lista de servers
                if (widget instanceof ServerSelectionList) continue;
                aw.visible = false;
                aw.active = false;
                aw.setX(-10000);
                aw.setY(-10000);
            }
        }
        // Segundo pase sobre children() (algunos widgets estan alli pero no en renderables)
        try {
            for (var child : this.children()) {
                if (child instanceof AbstractWidget aw && !(child instanceof DiaryButton)) {
                    if (child instanceof ServerSelectionList) continue;
                    aw.visible = false;
                    aw.active = false;
                }
            }
        } catch (Throwable ignored) {}
    }

    private void buildDiaryButtons() {
        int btnH = 20;
        int gap = 6;
        int rowGap = 14;
        int panelBottom = this.height - PANEL_MARGIN_V;
        int bottomRowY = panelBottom - btnH - 10;
        int topRowY = bottomRowY - btnH - rowGap;
        int availableRowW = Math.max(136, this.width - PANEL_MARGIN_H * 2 - 24);

        Component labelJoin = cleanLabel("selectServer.select");
        Component labelDirect = cleanLabel("selectServer.direct");
        Component labelAdd = cleanLabel("selectServer.add");
        Component labelEdit = cleanLabel("selectServer.edit");
        Component labelDelete = cleanLabel("selectServer.delete");
        Component labelRefresh = cleanLabel("selectServer.refresh");
        Component labelCancel = cleanLabel("gui.cancel");

        int wJoin = this.font.width(labelJoin) + 28;
        int wDirect = this.font.width(labelDirect) + 28;
        int wAdd = this.font.width(labelAdd) + 28;
        int wEdit = this.font.width(labelEdit) + 28;
        int wDelete = this.font.width(labelDelete) + 28;
        int wRefresh = this.font.width(labelRefresh) + 28;
        int wCancel = this.font.width(labelCancel) + 28;
        int topTotalW = wJoin + wDirect + wAdd + gap * 2;
        int bottomTotalW = wEdit + wDelete + wRefresh + wCancel + gap * 3;
        boolean compact = topTotalW > availableRowW || bottomTotalW > availableRowW;

        if (compact) {
            int compactGap = Math.max(3, Math.min(6, availableRowW / 40));
            int topButtonW = (availableRowW - compactGap * 2) / 3;
            int bottomButtonW = (availableRowW - compactGap * 3) / 4;
            int topStartX = (this.width - availableRowW) / 2;
            int bottomStartX = topStartX;

            this.diarySelect = createServerActionButton(topStartX, topRowY, topButtonW, btnH,
                Component.translatable("gripeverde.multiplayer.compact.join"), labelJoin,
                DiaryButton.Variant.PRIMARY, DiaryButton.SoundProfile.CLEAN, this::onJoin);
            this.diaryDirect = createServerActionButton(topStartX + topButtonW + compactGap, topRowY, topButtonW, btnH,
                Component.translatable("gripeverde.multiplayer.compact.direct"), labelDirect,
                DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.CLEAN, this::onDirect);
            this.diaryAdd = createServerActionButton(topStartX + (topButtonW + compactGap) * 2, topRowY, topButtonW, btnH,
                Component.translatable("gripeverde.multiplayer.compact.add"), labelAdd,
                DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.CLEAN, this::onAdd);

            this.diaryEdit = createServerActionButton(bottomStartX, bottomRowY, bottomButtonW, btnH,
                Component.translatable("gripeverde.multiplayer.compact.edit"), labelEdit,
                DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.CLEAN, this::onEdit);
            this.diaryDelete = createServerActionButton(bottomStartX + bottomButtonW + compactGap, bottomRowY, bottomButtonW, btnH,
                Component.translatable("gripeverde.multiplayer.compact.delete"), labelDelete,
                DiaryButton.Variant.DANGER, DiaryButton.SoundProfile.CLEAN, this::onDelete);
            this.diaryRefresh = createServerActionButton(bottomStartX + (bottomButtonW + compactGap) * 2, bottomRowY, bottomButtonW, btnH,
                Component.translatable("gripeverde.multiplayer.compact.refresh"), labelRefresh,
                DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.CLEAN, this::onRefresh);
            this.diaryCancel = createServerActionButton(bottomStartX + (bottomButtonW + compactGap) * 3, bottomRowY, bottomButtonW, btnH,
                Component.translatable("gripeverde.multiplayer.compact.cancel"), labelCancel,
                DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.BACK, this::onCancel);
        } else {
            int topStartX = (this.width - topTotalW) / 2;
            int bottomStartX = (this.width - bottomTotalW) / 2;

            this.diarySelect = createServerActionButton(topStartX, topRowY, wJoin, btnH, labelJoin, labelJoin,
                DiaryButton.Variant.PRIMARY, DiaryButton.SoundProfile.CLEAN, this::onJoin);
            this.diaryDirect = createServerActionButton(topStartX + wJoin + gap, topRowY, wDirect, btnH, labelDirect, labelDirect,
                DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.CLEAN, this::onDirect);
            this.diaryAdd = createServerActionButton(topStartX + wJoin + gap + wDirect + gap, topRowY, wAdd, btnH, labelAdd, labelAdd,
                DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.CLEAN, this::onAdd);

            this.diaryEdit = createServerActionButton(bottomStartX, bottomRowY, wEdit, btnH, labelEdit, labelEdit,
                DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.CLEAN, this::onEdit);
            this.diaryDelete = createServerActionButton(bottomStartX + wEdit + gap, bottomRowY, wDelete, btnH, labelDelete, labelDelete,
                DiaryButton.Variant.DANGER, DiaryButton.SoundProfile.CLEAN, this::onDelete);
            this.diaryRefresh = createServerActionButton(bottomStartX + wEdit + gap + wDelete + gap, bottomRowY, wRefresh, btnH, labelRefresh, labelRefresh,
                DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.CLEAN, this::onRefresh);
            this.diaryCancel = createServerActionButton(bottomStartX + wEdit + gap + wDelete + gap + wRefresh + gap, bottomRowY, wCancel, btnH, labelCancel, labelCancel,
                DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.BACK, this::onCancel);
        }

        this.addRenderableWidget(this.diarySelect);
        this.addRenderableWidget(this.diaryDirect);
        this.addRenderableWidget(this.diaryAdd);
        this.addRenderableWidget(this.diaryEdit);
        this.addRenderableWidget(this.diaryDelete);
        this.addRenderableWidget(this.diaryRefresh);
        this.addRenderableWidget(this.diaryCancel);

        updateDiaryButtonStates();
        hideVanillaButtons();
    }

    private DiaryButton createServerActionButton(int x, int y, int width, int height, Component visibleLabel,
                                                 Component fullLabel, DiaryButton.Variant variant,
                                                 DiaryButton.SoundProfile soundProfile, Runnable action) {
        DiaryButton button = new DiaryButton(x, y, width, height, visibleLabel, DiaryButton.Icon.NONE,
            variant, soundProfile, action);
        button.setTooltip(Tooltip.create(fullLabel));
        return button;
    }

    private String ellipsizeTo(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        int room = Math.max(0, maxWidth - this.font.width(ellipsis));
        return this.font.plainSubstrByWidth(text, room) + ellipsis;
    }

    private Component cleanLabel(String translationKey) {
        String text = Component.translatable(translationKey).getString().trim();
        while (text.endsWith("...") || text.endsWith("…")) {
            if (text.endsWith("...")) text = text.substring(0, text.length() - 3).trim();
            else text = text.substring(0, text.length() - 1).trim();
        }
        return Component.literal(text);
    }

    private void updateDiaryButtonStates() {
        boolean hasSelection = false;
        boolean canEdit = false;
        boolean canDelete = false;
        if (this.vanillaList != null) {
            var sel = this.vanillaList.getSelected();
            hasSelection = sel != null;
            // Solo permite Edit/Delete si hay una entry seleccionada Y es un server (no LAN header ni network)
            if (sel != null) {
                String cls = sel.getClass().getSimpleName();
                canEdit = cls.contains("Online");
                canDelete = cls.contains("Online");
            }
        }
        if (this.diarySelect != null) this.diarySelect.active = hasSelection;
        if (this.diaryEdit != null) this.diaryEdit.active = canEdit;
        if (this.diaryDelete != null) this.diaryDelete.active = canDelete;
    }

    private void onJoin() {
        // Scan dinamico por Component message (mas robusto que reflection stale)
        Button real = findVanillaButtonByLabel("selectServer.select");
        if (real != null && real.active) {
            real.onPress();
            return;
        }
        // Fallback a reflection si el scan falla
        if (this.vanillaSelectBtn != null && this.vanillaSelectBtn.active) {
            this.vanillaSelectBtn.onPress();
        }
    }

    /** Busca un boton vanilla iterando children() y comparando el Component message.
     *  Mas robusto que reflection porque siempre tiene la referencia fresca. */
    private Button findVanillaButtonByLabel(String translationKey) {
        String expected = Component.translatable(translationKey).getString();
        for (var child : this.children()) {
            if (child instanceof Button b) {
                String label = b.getMessage().getString();
                if (label.equals(expected)) {
                    return b;
                }
            }
        }
        return null;
    }

    private void onDirect() {
        ServerData sd = new ServerData(Component.translatable("selectServer.defaultName").getString(), "", false);
        this.minecraft.setScreen(new DiaryDirectJoinScreen(this, (accept) -> {
            if (accept) {
                ConnectScreen.startConnecting(this, this.minecraft, ServerAddress.parseString(sd.ip), sd, false);
            } else {
                this.minecraft.setScreen(this);
            }
        }, sd));
    }

    private void onAdd() {
        ServerData sd = new ServerData(Component.translatable("selectServer.defaultName").getString(), "", false);
        this.minecraft.setScreen(new DiaryEditServerScreen(this, (accept) -> {
            if (accept && this.vanillaServers != null) {
                this.vanillaServers.add(sd, false);
                this.vanillaServers.save();
                refreshList();
                // refreshList() ya hizo setScreen; no volver a llamar
            } else {
                this.minecraft.setScreen(this);
            }
        }, sd));
    }

    private void onEdit() {
        if (this.vanillaList == null) return;
        var sel = this.vanillaList.getSelected();
        if (sel == null) return;
        ServerData sd = extractServerDataFromEntry(sel);
        if (sd == null) return;
        ServerData copy = new ServerData(sd.name, sd.ip, false);
        this.minecraft.setScreen(new DiaryEditServerScreen(this, (accept) -> {
            if (accept && this.vanillaServers != null) {
                sd.name = copy.name;
                sd.ip = copy.ip;
                this.vanillaServers.save();
                refreshList();
                // refreshList() ya hizo setScreen; no volver a llamar
            } else {
                this.minecraft.setScreen(this);
            }
        }, copy));
    }

    private void onDelete() {
        if (this.vanillaList == null) return;
        var sel = this.vanillaList.getSelected();
        if (sel == null) return;
        ServerData sd = extractServerDataFromEntry(sel);
        if (sd == null) return;
        this.minecraft.setScreen(new DiaryConfirmScreen((accept) -> {
            if (accept && this.vanillaServers != null) {
                this.vanillaServers.remove(sd);
                this.vanillaServers.save();
                refreshList();
                // refreshList() ya hizo setScreen; no volver a llamar
            } else {
                this.minecraft.setScreen(this);
            }
        },
        Component.translatable("selectServer.deleteQuestion"),
        Component.translatable("selectServer.deleteWarning", sd.name),
        Component.translatable("selectServer.deleteButton"),
        Component.translatable("gui.cancel"),
        this));
    }

    private void onRefresh() {
        refreshList();
    }

    private void onCancel() {
        this.minecraft.setScreen(this.parent);
    }

    private void refreshList() {
        // Saved edits are loaded once by vanilla when the replacement screen initializes.
        // Avoid a second synchronous disk read immediately before rebuilding the list.
        this.minecraft.setScreen(new DiaryMultiplayerScreen(this.parent));
    }

    private ServerData extractServerDataFromEntry(Object entry) {
        try {
            Field f = null;
            for (Field cand : entry.getClass().getDeclaredFields()) {
                if (cand.getType().equals(ServerData.class)) { f = cand; break; }
            }
            if (f != null) {
                f.setAccessible(true);
                return (ServerData) f.get(entry);
            }
        } catch (Throwable ignored) {}
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T resolveField(Class<T> type, String officialName, String srgName) {
        try {
            Field f = null;
            try { f = JoinMultiplayerScreen.class.getDeclaredField(officialName); } catch (NoSuchFieldException ignored) {}
            if (f == null) try { f = JoinMultiplayerScreen.class.getDeclaredField(srgName); } catch (NoSuchFieldException ignored) {}
            if (f == null) {
                for (Field cand : JoinMultiplayerScreen.class.getDeclaredFields()) {
                    if (type.isAssignableFrom(cand.getType())) { f = cand; break; }
                }
            }
            if (f != null) {
                f.setAccessible(true);
                Object val = f.get(this);
                if (type.isInstance(val)) return (T) val;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        updateDiaryButtonStates();
        this.smoothScroll.tick(this.vanillaList);
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.vanillaList != null
                && mouseY >= this.vanillaList.getTop() && mouseY <= this.vanillaList.getBottom()) {
            if (this.smoothScroll.wheel(this.vanillaList, delta, 42.0D)) {
                com.eternalcraft.gripeverde.client.audio.UiSounds.scroll();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        // Fondo escena viva
        try {
            com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(gfx, this.width, this.height);
                } catch (Throwable ignored) {}

        // Overlay atenuador

        // Panel pergamino que ocupa casi toda la pantalla
        int panelX = PANEL_MARGIN_H;
        int panelY = PANEL_MARGIN_V;
        int panelW = this.width - PANEL_MARGIN_H * 2;
        int panelH = this.height - PANEL_MARGIN_V * 2;
        DiaryChrome.drawPanelShadow(gfx, panelX, panelY, panelW, panelH);
        PaperTexture.drawClean(gfx, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.OFFICIAL);
        DiaryChrome.drawCornerOrnaments(gfx, panelX, panelY, panelW, panelH);
        DiaryChrome.drawRegistryTag(gfx, panelX, panelY, panelW, panelH, "REG. X");

        // Titulo
        int titleY = panelY + 14;
        int centerX = panelX + panelW / 2;
        int headingMaxWidth = Math.max(64, panelW - 32);
        String titleStr = ellipsizeTo(this.title.getString(), headingMaxWidth);
        int titleW = this.font.width(titleStr);
        gfx.drawString(this.font, titleStr, centerX - titleW / 2, titleY, DiaryChrome.INK_TITLE, false);

        // Subtitulo
        String subtitle = "- " + Component.translatable("gripeverde.multiplayer.subtitle").getString() + " -";
        subtitle = ellipsizeTo(subtitle, headingMaxWidth);
        int subW = this.font.width(subtitle);
        gfx.drawString(this.font, subtitle, centerX - subW / 2, titleY + 12, DiaryChrome.INK_SUBTITLE, false);

        // Divisor con rombo
        DiaryChrome.drawInkDivider(gfx, panelX + 24, titleY + 26, Math.max(0, panelW - 48));
        DiaryChrome.drawDivisorLozenge(gfx, centerX, titleY + 26);

        // Lista vanilla (con sus favicons, ping, MOTD, etc.)
        if (this.vanillaList != null) {
            try {
                // Aplicar scissor para ocultar LAN header cuando hay servers
                int scissorBottom = computeVisibleBottom();
                if (scissorBottom > 0 && scissorBottom < this.vanillaList.getBottom()) {
                    gfx.enableScissor(0, this.vanillaList.getTop(), this.width, scissorBottom);
                    // Banda sepia detras de cada server (contraste para texto blanco vanilla)
                    drawEntryBackgrounds(gfx);
                    this.vanillaList.render(gfx, mouseX, mouseY, partial);
                    gfx.disableScissor();
                } else {
                    drawEntryBackgrounds(gfx);
                    this.vanillaList.render(gfx, mouseX, mouseY, partial);
                }
                // Subrayado sutil bajo cada server
                drawEntryFrames(gfx);
                // Scroll estilizado encima
                DiaryChrome.drawStyledScrollThumbForOptionsList(gfx, this.vanillaList, this.width);
            } catch (Throwable ignored) {}
        }

        // Divisor inferior antes de los botones - con rombo central
        int dividerY = this.height - LIST_BOTTOM_OFFSET + 2;
        DiaryChrome.drawInkDivider(gfx, panelX + 24, dividerY, panelW - 48);
        DiaryChrome.drawDivisorLozenge(gfx, this.width / 2, dividerY);

        // Vineta sepia perimetral (marco interno del pergamino)
        int vinAlpha = 0x30422810;
        int inset = 3;
        gfx.fill(panelX + inset, panelY + inset, panelX + panelW - inset, panelY + inset + 1, vinAlpha);
        gfx.fill(panelX + inset, panelY + panelH - inset - 1, panelX + panelW - inset, panelY + panelH - inset, vinAlpha);
        gfx.fill(panelX + inset, panelY + inset, panelX + inset + 1, panelY + panelH - inset, vinAlpha);
        gfx.fill(panelX + panelW - inset - 1, panelY + inset, panelX + panelW - inset, panelY + panelH - inset, vinAlpha);

        // Widgets Diary (nuestros botones)
        DiaryButton hoveredTooltip = null;
        DiaryButton focusedTooltip = null;
        for (var w : this.renderables) {
            if (w instanceof DiaryButton db && db.visible) {
                db.render(gfx, mouseX, mouseY, partial);
                if (db.getTooltip() != null) {
                    if (db.isMouseOver(mouseX, mouseY)) {
                        hoveredTooltip = db;
                    } else if (focusedTooltip == null && db.isFocused()) {
                        focusedTooltip = db;
                    }
                }
            }
        }

        // This screen draws its buttons manually, so restore delayed tooltips for mouse and keyboard users.
        DiaryButton tooltipTarget = hoveredTooltip != null ? hoveredTooltip : focusedTooltip;
        int tooltipX = hoveredTooltip != null ? mouseX : tooltipTarget != null ? tooltipTarget.getX() + tooltipTarget.getWidth() / 2 : mouseX;
        int tooltipY = hoveredTooltip != null ? mouseY : tooltipTarget != null ? tooltipTarget.getY() + tooltipTarget.getHeight() / 2 : mouseY;
        if (tooltipTarget != this.tooltipCandidate) {
            this.tooltipCandidate = tooltipTarget;
            this.tooltipCandidateSince = Util.getMillis();
        } else if (tooltipTarget != null && Util.getMillis() - this.tooltipCandidateSince >= 500L) {
            gfx.renderTooltip(this.font, tooltipTarget.getTooltip().toCharSequence(this.minecraft), tooltipX, tooltipY);
        }

        // v0.16.0
        try {
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(gfx, this.width);
        } catch (Throwable ignored) {}
    }



    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderBackground(GuiGraphics gfx) {
        // Bypass total del dirt vanilla
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    /**
     * Dibuja una banda sepia oscura detras de cada server (tipo tarjeta victoriana).
     * Da contraste al texto blanco/verde vanilla que sobre pergamino se lee mal.
     */
    private void drawEntryBackgrounds(GuiGraphics gfx) {
        if (this.vanillaList == null) return;
        try {
            var children = this.vanillaList.children();
            if (children == null || children.isEmpty()) return;

            int itemHeight = vanillaItemHeight;
            int y0 = vanillaListY0;
            int headerHeight = vanillaHeaderHeight;

            if (itemHeight <= 0) return;

            int rowLeft = this.vanillaList.getRowLeft();
            int rowRight = rowLeft + this.vanillaList.getRowWidth();
            int listTop = this.vanillaList.getTop();
            int listBottom = this.vanillaList.getBottom();
            double scrollAmount = 0.0;
            try { scrollAmount = this.vanillaList.getScrollAmount(); } catch (Throwable ignored) {}

            for (int i = 0; i < children.size(); i++) {
                var entry = children.get(i);
                String cls = entry.getClass().getSimpleName();
                if (cls.contains("LAN")) continue; // no dibujar bg detras de LAN

                int entryTop = y0 + 4 - (int) scrollAmount + i * itemHeight + headerHeight - 2;
                int entryBottom = entryTop + itemHeight;

                if (entryBottom < listTop || entryTop > listBottom) continue;
                int drawTop = Math.max(entryTop, listTop);
                int drawBottom = Math.min(entryBottom, listBottom);
                if (drawBottom <= drawTop) continue;

                // Banda sepia semi-transparente (contraste sin agredir al pergamino)
                int bandColor = 0x583A2416;
                int bandEdge  = 0x183A2416;
                // Extension lateral para envolver bien el favicon
                int extL = 8;
                int extR = 4;

                // Fade lateral izquierdo
                gfx.fillGradient(rowLeft - extL, drawTop, rowLeft, drawBottom, bandEdge, bandColor);
                // Cuerpo central solido
                gfx.fill(rowLeft, drawTop, rowRight, drawBottom, bandColor);
                // Fade lateral derecho
                gfx.fillGradient(rowRight, drawTop, rowRight + extR, drawBottom, bandColor, bandEdge);
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Dibuja un subrayado sutil bajo cada server (estilo tarjeta de biblioteca).
     * No dibuja marco completo - solo una linea horizontal fina de tinta.
     */
    private void drawEntryFrames(GuiGraphics gfx) {
        if (this.vanillaList == null) return;
        try {
            var children = this.vanillaList.children();
            if (children == null || children.isEmpty()) return;

            int itemHeight = vanillaItemHeight;
            int y0 = vanillaListY0;
            int headerHeight = vanillaHeaderHeight;

            if (itemHeight <= 0) return;

            int rowLeft = this.vanillaList.getRowLeft();
            int rowWidth = this.vanillaList.getRowWidth();
            int rowRight = rowLeft + rowWidth;
            int listTop = this.vanillaList.getTop();
            int listBottom = this.vanillaList.getBottom();
            double scrollAmount = 0.0;
            try {
                scrollAmount = this.vanillaList.getScrollAmount();
            } catch (Throwable ignored) {}

            for (int i = 0; i < children.size(); i++) {
                var entry = children.get(i);
                String cls = entry.getClass().getSimpleName();
                // Solo subrayar servers reales (no LAN header ni network servers de scan)
                if (cls.contains("LAN")) continue;

                int entryTop = y0 + 4 - (int) scrollAmount + i * itemHeight + headerHeight;
                int entryBottom = entryTop + itemHeight - 4;

                if (entryBottom < listTop || entryTop > listBottom) continue;
                int drawBottom = Math.min(entryBottom, listBottom);
                if (drawBottom <= listTop) continue;

                // Subrayado tinta sutil - una linea de 1px con fade a los costados
                int ink = 0x60422810;
                int fadeIn = 0x20422810;
                int fadeExt = 12;
                // Fade lateral izquierdo
                gfx.fill(rowLeft - fadeExt, drawBottom + 1, rowLeft, drawBottom + 2, fadeIn);
                // Linea central solida
                gfx.fill(rowLeft, drawBottom + 1, rowRight, drawBottom + 2, ink);
                // Fade lateral derecho
                gfx.fill(rowRight, drawBottom + 1, rowRight + fadeExt, drawBottom + 2, fadeIn);
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Calcula la coord Y hasta la cual se debe mostrar la lista (recorta LAN header
     * cuando hay servers reales). Devuelve 0 si no hay que recortar.
     */
    private int computeVisibleBottom() {
        if (this.vanillaList == null || this.vanillaServers == null) return 0;
        if (this.vanillaServers.size() == 0) return 0; // sin servers, mostrar todo
        try {
            var children = this.vanillaList.children();
            if (children == null) return 0;
            int itemHeight = vanillaItemHeight;
            int y0 = vanillaListY0;
            int headerHeight = vanillaHeaderHeight;
            if (itemHeight <= 0) return 0;
            double scrollAmount = 0.0;
            try { scrollAmount = this.vanillaList.getScrollAmount(); } catch (Throwable ignored) {}

            // Buscar el indice de la primera entry LAN
            int lanIdx = -1;
            for (int i = 0; i < children.size(); i++) {
                String cls = children.get(i).getClass().getSimpleName();
                if (cls.contains("LAN")) {
                    lanIdx = i;
                    break;
                }
            }
            if (lanIdx == -1) return 0; // no hay LAN header, no hay que recortar

            // Y donde empieza la entry LAN (todo abajo se oculta)
            int lanTop = y0 + 4 - (int) scrollAmount + lanIdx * itemHeight + headerHeight;
            return Math.max(lanTop - 2, this.vanillaList.getTop());
        } catch (Throwable ignored) {}
        return 0;
    }

    /**
     * Reflect once after the list is laid out, then reuse the row metrics while rendering.
     * These dimensions change on screen resize, not on every frame or scroll event.
     */
    private void cacheVanillaListMetrics() {
        if (this.vanillaList == null) {
            vanillaItemHeight = 0;
            vanillaListY0 = 0;
            vanillaHeaderHeight = 0;
            return;
        }
        vanillaItemHeight = readIntField(this.vanillaList, "itemHeight", "f_93387_");
        vanillaListY0 = readIntField(this.vanillaList, "y0", "f_93390_");
        vanillaHeaderHeight = readIntField(this.vanillaList, "headerHeight", "f_93395_");
    }

    private static int readIntField(Object target, String officialName, String srgName) {
        try {
            java.lang.reflect.Field f = null;
            Class<?> cls = target.getClass();
            // Buscar en toda la jerarquia (los fields estan en la superclase)
            while (cls != null && f == null) {
                try { f = cls.getDeclaredField(officialName); } catch (NoSuchFieldException ignored) {}
                if (f == null) {
                    try { f = cls.getDeclaredField(srgName); } catch (NoSuchFieldException ignored) {}
                }
                if (f == null) cls = cls.getSuperclass();
            }
            if (f != null) {
                f.setAccessible(true);
                return f.getInt(target);
            }
        } catch (Throwable ignored) {}
        return 0;
    }
}
