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
import org.lwjgl.glfw.GLFW;
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
        return DiaryChrome.tr(PINNED_NAME_KEY);
    }

    private static boolean isPinnedName(String name) {
        return PINNED_NAME_EN.equals(name) || PINNED_NAME_ES.equals(name)
            || localizedPinnedName().equals(name);
    }

    private static boolean matchesServerAddress(String address, String expected) {
        return address != null && address.trim().equalsIgnoreCase(expected);
    }

    private static boolean isPinnedAddress(String address) {
        return matchesServerAddress(address, PINNED_IP)
            || matchesServerAddress(address, PREVIOUS_PINNED_IP);
    }

    private static boolean isPinnedServer(ServerData server) {
        return server != null && (isPinnedName(server.name) || isPinnedAddress(server.ip));
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
        // v0.271.0: la hoja de puestos se abre con ruido de papel.
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.open();
        } catch (Throwable ignored) {
        }
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
     * Keeps the official server available at the top of the list.
     * Prefer a saved official address over a built-in name whose address the player edited.
     * If no official address exists, preserve the renamed server and its custom address.
     */
    private void ensurePinnedServer() {
        if (this.vanillaServers == null) return;
        try {
            int pinnedIndex = -1;
            boolean changed = false;

            // Resolve the official endpoint first so a custom-address copy cannot eclipse it.
            for (int i = 0; i < this.vanillaServers.size(); i++) {
                ServerData server = this.vanillaServers.get(i);
                if (server != null && isPinnedAddress(server.ip)) {
                    pinnedIndex = i;
                    if (matchesServerAddress(server.ip, PREVIOUS_PINNED_IP)) {
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
            }

            // Keep a player-renamed/edited official entry if it is the only saved copy.
            if (pinnedIndex < 0) {
                for (int i = 0; i < this.vanillaServers.size(); i++) {
                    ServerData server = this.vanillaServers.get(i);
                    if (server == null || !isPinnedName(server.name)) continue;
                    pinnedIndex = i;
                    String displayName = localizedPinnedName();
                    if (!displayName.equals(server.name)) {
                        server.name = displayName;
                        changed = true;
                    }
                    break;
                }
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
        int btnH = 22;   // v0.188.0: un poco mas de cuerpo, sin tocar la lista
        int gap = 6;
        int rowGap = 12;
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
                Component.translatable("gripeverde.multiplayer.compact.join"), withEnter(labelJoin),
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

            this.diarySelect = createServerActionButton(topStartX, topRowY, wJoin, btnH, labelJoin, withEnter(labelJoin),
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

    /** v0.258.0: el boton de entrar dice tambien con que tecla se entra. */
    private static Component withEnter(Component label) {
        return Component.literal(label.getString() + "  ·  Enter");
    }

    private DiaryButton createServerActionButton(int x, int y, int width, int height, Component visibleLabel,
                                                 Component fullLabel, DiaryButton.Variant variant,
                                                 DiaryButton.SoundProfile soundProfile, Runnable action) {
        DiaryButton button = new DiaryButton(x, y, width, height, visibleLabel, DiaryButton.Icon.NONE,
            variant, soundProfile, action);
        button.setTooltip(Tooltip.create(fullLabel));
        return button;
    }

    // v0.201.0: cabecera memorizada (titulo, subtitulo y sus posiciones).
    private net.minecraft.client.gui.Font headerFont;
    private int headerMaxWidth = -1;
    private String headerTitleSrc = "";
    private String headerSubSrc = "";
    private String headerTitle = "";
    private int headerTitleX = 0;
    private String headerSubtitle = "";
    private int headerSubX = 0;

    private void refreshHeaderCache(int maxWidth) {
        String titleSrc = this.title.getString();
        String subSrc = "- " + DiaryChrome.tr("gripeverde.multiplayer.subtitle") + " -";
        if (this.headerFont == this.font && this.headerMaxWidth == maxWidth
                && this.headerTitleSrc.equals(titleSrc) && this.headerSubSrc.equals(subSrc)) {
            return;
        }
        this.headerFont = this.font;
        this.headerMaxWidth = maxWidth;
        this.headerTitleSrc = titleSrc;
        this.headerSubSrc = subSrc;
        this.headerTitle = ellipsizeTo(titleSrc, maxWidth);
        this.headerSubtitle = ellipsizeTo(subSrc, maxWidth);
        int cx = this.width / 2;
        this.headerTitleX = cx - this.font.width(this.headerTitle) / 2;
        this.headerSubX = cx - this.font.width(this.headerSubtitle) / 2;
    }

    private String ellipsizeTo(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        int room = Math.max(0, maxWidth - this.font.width(ellipsis));
        return this.font.plainSubstrByWidth(text, room) + ellipsis;
    }

    private Component cleanLabel(String translationKey) {
        String text = DiaryChrome.tr(translationKey).trim();
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
        if (this.diarySelect != null) {
            this.diarySelect.active = hasSelection;
            // v0.263.0: el boton dice a donde entra, no solo "Entrar".
            String target = null;
            try {
                var sel = this.vanillaList == null ? null : this.vanillaList.getSelected();
                ServerData sd = sel == null ? null : extractServerDataFromEntry(sel);
                if (sd != null && sd.name != null && !sd.name.isBlank()) target = sd.name;
            } catch (Throwable ignored) {
            }
            boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
            this.diarySelect.setTooltip(Tooltip.create(Component.literal(target == null
                ? DiaryChrome.tr("selectServer.select")
                : (en ? "Join " : "Entrar a ") + target)));
        }
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
        String expected = DiaryChrome.tr(translationKey);
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
        ServerData sd = new ServerData(DiaryChrome.tr("selectServer.defaultName"), "", false);
        this.minecraft.setScreen(new DiaryDirectJoinScreen(this, (accept) -> {
            if (accept) {
                ConnectScreen.startConnecting(this, this.minecraft, ServerAddress.parseString(sd.ip), sd, false);
            } else {
                this.minecraft.setScreen(this);
            }
        }, sd));
    }

    private void onAdd() {
        ServerData sd = new ServerData(DiaryChrome.tr("selectServer.defaultName"), "", false);
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

    // v0.245.0: entrar al atlas de hospedajes firma el libro de visitas.
    private static boolean guestbookSigned;

    @Override
    public void tick() {
        super.tick();
        if (!guestbookSigned) {
            guestbookSigned = true;
            try {
                com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                    com.eternalcraft.gripeverde.client.lore.Discoveries.GUESTBOOK);
            } catch (Throwable ignored) {}
        }
        updateDiaryButtonStates();
        this.smoothScroll.tick(this.vanillaList);
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
    }

    /** v0.271.0: escribir en el buscador suena a pluma. */
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        boolean handled = super.charTyped(codePoint, modifiers);
        if (handled) {
            try {
                com.eternalcraft.gripeverde.client.audio.UiSounds.type();
            } catch (Throwable ignored) {
            }
        }
        return handled;
    }

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
        PaperTexture.drawTornSheet(gfx, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.OFFICIAL);
        DiaryChrome.drawCornerOrnaments(gfx, panelX, panelY, panelW, panelH);
        DiaryChrome.drawRegistryTag(gfx, panelX, panelY, panelW, panelH, "REG. X");
        // v0.271.0: folio del expediente, en la esquina baja izquierda.
        try {
            boolean enFolio = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
            String folio = enFolio ? "sheet 1" : "hoja 1";
            gfx.drawString(this.font, folio, panelX + 14, panelY + panelH - 14,
                DiaryChrome.INK_FLAVOR, false);
        } catch (Throwable ignored) {
        }

        try {
            drawServersFileAtFoot(gfx, panelX, panelY, panelW, panelH,
                com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish());
        } catch (Throwable ignored) {
        }

        // Titulo
        int titleY = panelY + 14;
        int centerX = panelX + panelW / 2;
        // v0.301.0: el subtitulo se comia el recuento y la linea del archivo.
        int headingMaxWidth = Math.max(64, panelW - 220);
        // v0.201.0: el titulo y el subtitulo se recortaban y se medían en cada
        // cuadro para centrarlos. Ahora el recorte y el centro se guardan.
        refreshHeaderCache(headingMaxWidth);
        gfx.drawString(this.font, this.headerTitle, this.headerTitleX, titleY, DiaryChrome.INK_TITLE, false);
        gfx.drawString(this.font, this.headerSubtitle, this.headerSubX, titleY + 12, DiaryChrome.INK_SUBTITLE, false);

        // v0.188.0: cuantos servidores hay anotados, en el margen del titulo.
        drawServerCount(gfx, panelX, panelW, titleY);

        // Divisor con rombo
        DiaryChrome.drawInkDivider(gfx, panelX + 24, titleY + 26, Math.max(0, panelW - 48));
        DiaryChrome.drawDivisorLozenge(gfx, centerX, titleY + 26);

        // v0.241.0: el tramo vacio de la lista se dibuja como renglones de
        // registro, para que la hoja no quede en blanco.
        drawRegisterRules(gfx);
        try {
            drawEmptyNote(gfx);
        } catch (Throwable ignored) {
        }

        // Lista vanilla (con sus favicons, ping, MOTD, etc.)
        if (this.vanillaList != null) {
            try {
                // Aplicar scissor para ocultar LAN header cuando hay servers
                int scissorBottom = computeVisibleBottom();
                if (scissorBottom > 0 && scissorBottom < this.vanillaList.getBottom()) {
                    gfx.enableScissor(0, this.vanillaList.getTop(), this.width, scissorBottom);
                    // Banda sepia detras de cada server (contraste para texto blanco vanilla)
                    drawEntryBackgrounds(gfx, mouseX, mouseY);
                    this.vanillaList.render(gfx, mouseX, mouseY, partial);
                    gfx.disableScissor();
                } else {
                    drawEntryBackgrounds(gfx, mouseX, mouseY);
                    this.vanillaList.render(gfx, mouseX, mouseY, partial);
                }
                // Subrayado sutil bajo cada server
                drawEntryFrames(gfx);
                // v0.303.0: el nombre y el cartel del puesto, en tinta.
                paintServerRowsInk(gfx);
                // Scroll estilizado encima
                DiaryChrome.drawStyledScrollThumbForOptionsList(gfx, this.vanillaList, this.width);
            } catch (Throwable ignored) {}
        }

        // v0.265.0: el divisor inferior se fue. Caia justo sobre la fila de
        // botones de abajo -height-74 contra height-72- y se veia como una
        // raya cruzada entre los botones.

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

        // v0.301.0: aqui se dibujaba a mano el aviso flotante de cada boton y
        // salia dos veces (uno arriba y otro abajo del apretado). Se quito: los
        // botones ya dicen lo que hacen y la hoja no necesita repetirlo.

        // v0.16.0
        try {
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(gfx, this.width);
        } catch (Throwable ignored) {}
    }



    /**
     * v0.251.0: la mano que no quiere soltar el raton. Enter entra al puesto
     * elegido y Supr lo borra, igual que los botones de abajo.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (this.diarySelect != null && this.diarySelect.active) {
                com.eternalcraft.gripeverde.client.audio.UiSounds.playClick();
                onJoin();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_F5) {
            // v0.263.0: F5 vuelve a leer la lista de puestos guardados.
            com.eternalcraft.gripeverde.client.audio.UiSounds.playClick();
            onRefresh();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (this.diaryDelete != null && this.diaryDelete.active) {
                com.eternalcraft.gripeverde.client.audio.UiSounds.clickDestroy();
                onDelete();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private long lastListClickMs = 0L;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Object before = null;
        try {
            before = this.vanillaList == null ? null : this.vanillaList.getSelected();
        } catch (Throwable ignored) {
        }
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        // v0.263.0: dos golpes seguidos sobre el mismo puesto y se entra, sin
        // ir a buscar el boton. El primero solo elige, como siempre.
        if (button == 0 && overServerList(mouseX, mouseY)) {
            Object after = null;
            try {
                after = this.vanillaList == null ? null : this.vanillaList.getSelected();
            } catch (Throwable ignored) {
            }
            long now = System.currentTimeMillis();
            if (after != null && after == before && now - this.lastListClickMs < 320L) {
                this.lastListClickMs = 0L;
                try {
                    com.eternalcraft.gripeverde.client.audio.UiSounds.playClick();
                } catch (Throwable ignored) {
                }
                onJoin();
                return true;
            }
            this.lastListClickMs = now;
        }
        return handled;
    }

    /** v0.263.0: la banda que ocupa la lista de puestos. */
    private boolean overServerList(double mouseX, double mouseY) {
        int top = PANEL_MARGIN_V + LIST_TOP_OFFSET;
        int bottom = this.height - PANEL_MARGIN_V - LIST_BOTTOM_OFFSET;
        return mouseY >= top && mouseY <= bottom
            && mouseX >= PANEL_MARGIN_H && mouseX <= this.width - PANEL_MARGIN_H;
    }

    @Override
    public void renderBackground(GuiGraphics gfx) {
        // Bypass total del dirt vanilla
    }

    @Override
    public void onClose() {
        // v0.263.0: volver tambien suena a puerta, como en el resto del gabinete.
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.back();
        } catch (Throwable ignored) {
        }
        this.minecraft.setScreen(this.parent);
    }

    /**
     * Dibuja una banda sepia oscura detras de cada server (tipo tarjeta victoriana).
     * Da contraste al texto blanco/verde vanilla que sobre pergamino se lee mal.
     */
    /**
     * v0.188.0: el numero de servidores anotados, arriba a la derecha del titulo.
     * Si la lista esta vacia, en cambio, deja dicho como empezar.
     */
    private String countText = "";
    private int countWidth = 0;
    private int countSrc = -1;
    private boolean countEnglish = false;
    private net.minecraft.client.gui.Font countFont;

    private void drawServerCount(GuiGraphics gfx, int panelX, int panelW, int titleY) {
        int count = 0;
        try {
            count = this.vanillaServers == null ? 0 : this.vanillaServers.size();
        } catch (Throwable ignored) {}
        boolean english = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        // v0.201.0: la frase y su ancho se armaban por cuadro; ahora sólo cuando
        // cambia el numero de servidores anotados o el idioma.
        if (this.countSrc != count || this.countEnglish != english || this.countFont != this.font) {
            if (count <= 0) {
                this.countText = english ? "none yet" : "ninguno anotado";
            } else if (count == 1) {
                this.countText = english ? "1 entry" : "1 anotado";
            } else {
                this.countText = english ? count + " entries" : count + " anotados";
            }
            // v0.313.0: cuantas de esas puertas ya se visitaron.
            try {
                int known = com.eternalcraft.gripeverde.client.integration
                    .RecentAddresses.list().size();
                if (known > 0 && this.countSrc != -2) {
                    this.countText = this.countText + "  \u00b7  " + known
                        + (english ? " known" : " conocidas");
                }
            } catch (Throwable ignored) {
            }
            this.countWidth = this.font.width(this.countText);
            this.countSrc = count;
            this.countEnglish = english;
            this.countFont = this.font;
        }
        String text = this.countText;
        int textW = this.countWidth;
        // v0.241.0: el recuento se corre a la izquierda de la chapa REG. X,
        // que ocupaba ese rincón.
        int x = panelX + panelW - 74 - textW;
        int y = titleY + 4;
        if (x < panelX + 20) return;   // panel angosto: no vale la pena
        gfx.drawString(this.font, text, x, y, DiaryChrome.INK_SUBTITLE, false);

        // v0.287.0: el puesto elegido y si ya se entro antes por esa puerta.
        try {
            String picked = "";
            try {
                var sel = this.vanillaList == null ? null : this.vanillaList.getSelected();
                if (sel != null) {
                    var data = sel.getClass().getMethod("getServerData").invoke(sel);
                    Object ip = data.getClass().getField("ip").get(data);
                    if (ip != null) picked = ip.toString();
                }
            } catch (Throwable ignored) {
            }
            if (!picked.isEmpty()) {
                boolean known = com.eternalcraft.gripeverde.client.integration
                    .RecentAddresses.list().contains(picked);
                String line = (english ? "chosen: " : "elegido: ") + picked
                    + (known ? (english ? "  ·  known door" : "  ·  puerta conocida") : "");
                // v0.313.0: el elegido va al filo derecho del pie, para no
                // mezclarse con el folio ni con el peso del archivo.
                String cut = DiaryChrome.fitCached(this.font, line,
                    Math.max(60, panelW / 2 - 10));
                int lw = DiaryChrome.widthCached(this.font, cut);
                int lx = panelX + panelW - 14 - lw;
                int chosenY = this.height - PANEL_MARGIN_V - 12;
                if (this.width >= 360 && chosenY > titleY + 40 && lx > panelX + panelW / 2) {
                    gfx.drawString(this.font, cut, lx, chosenY, DiaryChrome.INK_FLAVOR, false);
                }
            }
        } catch (Throwable ignored) {
        }

        // v0.301.0: el peso del archivo y la fecha se fueron de la cabecera;
        // caian sobre el subtitulo y no cabian. Ahora viven en el pie, junto al
        // folio, donde no tapan nada.
    }

    /** v0.301.0: "servers.dat 81,0 KB · 18·XI·2026", en el pie de la hoja. */
    private void drawServersFileAtFoot(GuiGraphics gfx, int panelX, int panelY, int panelW,
                                       int panelH, boolean english) {
        String detail = serversFileLine(english);
        if (detail.isEmpty()) return;
        // v0.313.0: el peso se queda en la mitad izquierda del pie.
        int room = Math.max(60, panelW / 2 - 70);
        String fitted = DiaryChrome.fitCached(this.font, detail, room);
        int w = DiaryChrome.widthCached(this.font, fitted);
        int x = panelX + 14 + this.font.width(english ? "sheet 1" : "hoja 1") + 12;
        if (x + w > panelX + panelW / 2 - 10) return;
        gfx.drawString(this.font, fitted, x, panelY + panelH - 14, DiaryChrome.INK_FLAVOR, false);
    }

    /** v0.271.0: "servers.dat 412 B · 13·XI·2026". */
    private String serversFileLine(boolean english) {
        String weight = "";
        try {
            java.nio.file.Path f = this.minecraft.gameDirectory.toPath().resolve("servers.dat");
            if (java.nio.file.Files.isRegularFile(f)) {
                long b = java.nio.file.Files.size(f);
                weight = b < 1024L ? b + " B"
                    : String.format(java.util.Locale.ROOT, "%.1f KB", b / 1024.0);
            }
        } catch (Throwable ignored) {
        }
        String[] roman = {"I","II","III","IV","V","VI","VII","VIII","IX","X","XI","XII"};
        String date = "";
        try {
            java.util.Calendar c = java.util.Calendar.getInstance();
            date = c.get(java.util.Calendar.DAY_OF_MONTH) + "·"
                + roman[Math.max(0, Math.min(11, c.get(java.util.Calendar.MONTH)))] + "·"
                + c.get(java.util.Calendar.YEAR);
        } catch (Throwable ignored) {
        }
        if (weight.isEmpty()) return date;
        if (date.isEmpty()) return weight;
        return weight + "  ·  " + date;
    }

    /** v0.271.0: si no hay ningun puesto, la hoja lo dice en el centro. */
    private void drawEmptyNote(GuiGraphics gfx) {
        if (this.vanillaList == null) return;
        try {
            if (!this.vanillaList.children().isEmpty()) return;
        } catch (Throwable ignored) {
            return;
        }
        int top = this.vanillaList.getTop();
        int bottom = this.vanillaList.getBottom();
        if (bottom - top < 30) return;
        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        String note = en ? "no posts noted yet  ·  add one below"
            : "aun no hay puestos  ·  anade uno abajo";
        String cut = DiaryChrome.fitText(this.font, note,
            Math.max(80, this.vanillaList.getRowWidth()));
        int nw = DiaryChrome.widthCached(this.font, cut);
        int cx = this.vanillaList.getRowLeft() + this.vanillaList.getRowWidth() / 2;
        gfx.drawString(this.font, cut, cx - nw / 2, (top + bottom) / 2 - 4,
            DiaryChrome.INK_SUBTITLE, false);
    }

    /** v0.241.0: renglones tenues del registro, detras de la lista. */
    private void drawRegisterRules(GuiGraphics gfx) {
        if (this.vanillaList == null) return;
        try {
            int rowLeft = this.vanillaList.getRowLeft();
            int rowRight = rowLeft + this.vanillaList.getRowWidth();
            int top = this.vanillaList.getTop();
            int bottom = this.vanillaList.getBottom();
            if (rowRight <= rowLeft || bottom <= top) return;
            int ink = 0x14422810;
            int step = Math.max(14, vanillaItemHeight > 0 ? vanillaItemHeight : 26);
            int guard = 0;
            for (int y = top + step; y < bottom - 6 && guard < 64; y += step, guard++) {
                gfx.fill(rowLeft - 4, y, rowRight + 4, y + 1, ink);
            }
        } catch (Throwable ignored) {}
    }

    private void drawEntryBackgrounds(GuiGraphics gfx, int mouseX, int mouseY) {
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

                // v0.241.0: el puesto se monta sobre un recorte de papel, con
                // su margen alrededor; la placa oscura del juego queda adentro
                // como si fuera la foto pegada en la ficha.
                com.eternalcraft.gripeverde.client.render.PaperTexture.drawTornSlip(
                    gfx, rowLeft - 9, drawTop - 1, rowRight - rowLeft + 18,
                    drawBottom - drawTop + 2, 0xE6D2A6, 0x422810, 210);

                // v0.251.0: la fila que esta bajo la mano se aclara, para saber
                // cual se lleva si se aprieta Enter o Supr.
                boolean over = mouseX >= rowLeft - 9 && mouseX <= rowRight + 9
                    && mouseY >= drawTop - 1 && mouseY < drawBottom + 1;
                if (over) {
                    gfx.fill(rowLeft - 9, drawTop - 1, rowRight + 9, drawBottom + 1, 0x18F3E7C8);
                }

                // v0.251.0: el puesto oficial lleva su lacre en el canto izquierdo.
                ServerData sd = extractServerDataFromEntry(entry);
                if (sd != null && isPinnedServer(sd)) {
                    int sealX = rowLeft - 9;
                    gfx.fill(sealX, drawTop - 1, sealX + 3, drawBottom + 1, 0xB05C0A08);
                    gfx.fill(sealX + 1, drawTop + 2, sealX + 2, drawTop + 6, 0x70E8DCB4);
                }
            }
        } catch (Throwable ignored) {}
    }

    /**
     * v0.303.0: el juego escribe el nombre y el cartel de cada puesto en blanco
     * y gris fijos, y sobre el papel claro no se leian. Se cubre solo la franja
     * de texto -el icono se respeta- y se reescriben en tinta, en el mismo sitio.
     */
    private void paintServerRowsInk(GuiGraphics gfx) {
        if (this.vanillaList == null) return;
        int itemHeight = vanillaItemHeight;
        if (itemHeight <= 0) return;
        try {
            var children = this.vanillaList.children();
            if (children == null || children.isEmpty()) return;
            int rowLeft = this.vanillaList.getRowLeft();
            int rowWidth = this.vanillaList.getRowWidth();
            int listTop = this.vanillaList.getTop();
            int listBottom = this.vanillaList.getBottom();
            double scrollAmount = 0.0;
            try { scrollAmount = this.vanillaList.getScrollAmount(); } catch (Throwable ignored) {}
            boolean scissored = false;
            try {
                gfx.enableScissor(rowLeft, listTop, rowLeft + rowWidth, listBottom);
                scissored = true;
            } catch (Throwable ignored) {
            }
            int textLeft = rowLeft + 34;
            int textRight = rowLeft + rowWidth - 46;
            int textW = Math.max(40, textRight - textLeft);
            for (int i = 0; i < children.size(); i++) {
                var entry = children.get(i);
                String cls = entry.getClass().getSimpleName();
                if (cls.contains("LAN")) continue;
                ServerData sd = extractServerDataFromEntry(entry);
                if (sd == null) continue;
                String name = fieldString(sd, "name");
                String ip = fieldString(sd, "ip");
                if (name == null || name.isEmpty()) name = ip;
                if (name == null || name.isEmpty()) continue;
                int entryTop = vanillaListY0 + 4 - (int) scrollAmount + i * itemHeight
                    + vanillaHeaderHeight - 2;
                int entryBottom = entryTop + itemHeight;
                if (entryBottom < listTop || entryTop > listBottom) continue;
                int drawTop = Math.max(entryTop, listTop);
                int drawBottom = Math.min(entryBottom, listBottom);
                if (drawBottom - drawTop < 10) continue;

                int slipTop = drawTop + 1;
                int slipH = Math.max(16, drawBottom - drawTop - 2);
                com.eternalcraft.gripeverde.client.render.PaperTexture.drawTornSlip(
                    gfx, textLeft - 4, slipTop - 1, textW + 10, slipH, 0xE6D2A6, 0x422810, 232);

                String shownName = DiaryChrome.fitText(this.font, name, textW);
                gfx.drawString(this.font, shownName, textLeft, slipTop + 2, 0xFF1A1108, false);

                String motd = motdText(sd);
                String extra = "";
                try {
                    if (ip != null && com.eternalcraft.gripeverde.client.integration
                            .RecentAddresses.list().contains(ip)) {
                        extra = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                            ? "  ·  known door" : "  ·  puerta conocida";
                    }
                } catch (Throwable ignored) {
                }
                String line2 = motd.isEmpty() ? extra.trim() : motd + extra;
                if (!line2.isEmpty() && slipH >= 20) {
                    String shownMotd = DiaryChrome.fitText(this.font, line2, textW);
                    gfx.drawString(this.font, shownMotd, textLeft, slipTop + 13, 0xFF6B5533, false);
                }
            }
            if (scissored) {
                try {
                    gfx.disableScissor();
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static String fieldString(Object target, String fieldName) {
        try {
            java.lang.reflect.Field f = target.getClass().getField(fieldName);
            Object v = f.get(target);
            return v == null ? "" : v.toString();
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static String motdText(Object serverData) {
        try {
            java.lang.reflect.Field f = serverData.getClass().getField("motd");
            Object v = f.get(serverData);
            if (v instanceof net.minecraft.network.chat.Component c) {
                String s = c.getString();
                return s == null ? "" : s.replace('\n', ' ').trim();
            }
            return v == null ? "" : v.toString();
        } catch (Throwable ignored) {
            return "";
        }
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
