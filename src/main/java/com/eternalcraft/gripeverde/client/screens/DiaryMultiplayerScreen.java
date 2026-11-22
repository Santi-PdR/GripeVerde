package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
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
    private boolean serverIconCachePruned;

    // v0.382.0: la imagen y el recuento de cada puesto, una vez por direccion.
    private static final java.util.Map<String, net.minecraft.resources.ResourceLocation> SERVER_ICONS
        = new java.util.HashMap<>();
    private static final java.util.Map<String, Long> SERVER_ICON_RETRY_AT
        = new java.util.HashMap<>();
    private static final long SERVER_ICON_RETRY_DELAY_MS = 1000L;
    private static final int MAX_SERVER_ICON_BYTES = 1024 * 1024;
    private static final int MAX_SERVER_ICON_DIMENSION = 512;
    private static final java.util.concurrent.atomic.AtomicLong SERVER_ICON_TEXTURE_IDS
        = new java.util.concurrent.atomic.AtomicLong();
    /** Dimensiones completas de cada imagen recibida en el ping del servidor. */
    private record ServerIconSize(int width, int height) { }
    private static final java.util.Map<String, ServerIconSize> SERVER_ICON_SIZES
        = new java.util.HashMap<>();
    private int vanillaListY0;
    private int vanillaHeaderHeight;
    // v0.390.0: la fila, preguntada a la propia lista. `itemHeight`, `y0` y
    // `headerHeight` vienen renombrados y, si UNO solo falla, todas las bandas
    // quedan corridas. La lista sabe donde empieza cada fila: se le pregunta por
    // un metodo que recibe un indice y devuelve una coordenada.
    private java.lang.reflect.Method rowTopMethod;
    private int vanillaRowStep;

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
        if (!this.serverIconCachePruned) {
            this.serverIconCachePruned = pruneServerIconCache();
        }
        selectPinnedServerIfNothingIsSelected();
        updateDiaryButtonStates();
    }

    /** Libera iconos de puestos que ya no están guardados en servers.dat. */
    private boolean pruneServerIconCache() {
        if (this.vanillaServers == null || this.minecraft == null) return false;
        java.util.Set<String> liveServers = new java.util.HashSet<>();
        try {
            for (int i = 0; i < this.vanillaServers.size(); i++) {
                ServerData server = this.vanillaServers.get(i);
                String address = server == null ? null : server.ip;
                if (address != null && !address.isEmpty()) liveServers.add(address);
            }
        } catch (Throwable ignored) {
            return false;
        }
        for (String address : new java.util.ArrayList<>(SERVER_ICONS.keySet())) {
            if (liveServers.contains(address)) continue;
            net.minecraft.resources.ResourceLocation icon = SERVER_ICONS.remove(address);
            SERVER_ICON_SIZES.remove(address);
            if (icon != null) {
                try {
                    this.minecraft.getTextureManager().release(icon);
                } catch (Throwable ignored) {
                }
            }
        }
        SERVER_ICON_RETRY_AT.clear();
        return true;
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
            if (method == null) {
                // v0.388.0: si el juego viene con otro nombre, se busca por
                // FORMA: el unico metodo que recibe una ServerList y devuelve
                // nada es el que refresca los puestos. Antes, sin esto, la
                // lista se quedaba sin refrescar y los jugadores y el MOTD
                // salian viejos o vacios.
                for (Class<?> c = ServerSelectionList.class; c != null && c != Object.class;
                     c = c.getSuperclass()) {
                    for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                        if (m.getParameterCount() != 1) continue;
                        if (!m.getParameterTypes()[0].isAssignableFrom(ServerList.class)) continue;
                        if (m.getReturnType() != void.class) continue;
                        if (java.lang.reflect.Modifier.isStatic(m.getModifiers())) continue;
                        method = m;
                        break;
                    }
                    if (method != null) break;
                }
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

    private String ellipsizeTo(String text, int maxWidth){
        // v0.384.0: sin puntos suspensivos; la politica vive en un solo sitio.
        return com.eternalcraft.gripeverde.client.render.DiaryChrome.shorten(this.font, text, maxWidth);
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
                // v0.367.0: se preguntaba por el NOMBRE de la clase ("Online"),
                // que en el juego real va ofuscado: los botones Editar y Borrar
                // salian siempre apagados. Ahora se mira si la fila trae una
                // ficha de servidor, que es lo que de verdad distingue un
                // puesto de una cabecera de red local.
                boolean real = extractServerDataFromEntry(sel) != null;
                canEdit = real;
                canDelete = real;
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
        invalidateServerIconCache();
        refreshList();
    }

    private void invalidateServerIconCache() {
        if (this.minecraft != null) {
            for (net.minecraft.resources.ResourceLocation icon
                    : new java.util.ArrayList<>(SERVER_ICONS.values())) {
                try {
                    this.minecraft.getTextureManager().release(icon);
                } catch (Throwable ignored) {
                }
            }
        }
        SERVER_ICONS.clear();
        SERVER_ICON_SIZES.clear();
        SERVER_ICON_RETRY_AT.clear();
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
        // v0.378.0: la hoja va a sangre por los lados. El juego coloca su
        // lista de puestos de borde a borde y, con el margen de 40, el icono del
        // servidor y el canto de la fila se salian del papel.
        int panelX = 0;
        int panelY = PANEL_MARGIN_V;
        int panelW = this.width;
        int panelH = this.height - PANEL_MARGIN_V * 2;
        DiaryChrome.drawPanelShadow(gfx, panelX, panelY, panelW, panelH);
        PaperTexture.drawTornSheet(gfx, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.OFFICIAL);
        DiaryChrome.drawCornerOrnaments(gfx, panelX, panelY, panelW, panelH);
        DiaryChrome.drawRegistryTag(gfx, panelX, panelY, panelW, panelH, "REG. X");
        // v0.338.0: el folio y la ficha del archivo se fueron al margen de la
        // cabecera. En el pie quedaban debajo de las fichas Editar/Borrar y la
        // fecha se leia a medias sobre el canto del boton.

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

        // v0.338.0: folio y ficha del archivo, en el margen izquierdo de la
        // cabecera, con el ancho que deja el titulo centrado.
        try {
            drawArchiveMargin(gfx, panelX, panelW, titleY);
        } catch (Throwable ignored) {
        }

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
                    // La lista usa texto y texturas en buffers diferidos. Se
                    // asienta antes de cubrirla para que sus MOTD, contadores e
                    // iconos no aparezcan otra vez por encima de la banda.
                    gfx.flush();
                    gfx.disableScissor();
                } else {
                    drawEntryBackgrounds(gfx, mouseX, mouseY);
                    this.vanillaList.render(gfx, mouseX, mouseY, partial);
                    gfx.flush();
                }
                // v0.386.0: el subrayado de cada puesto se pinta dentro de
                // paintServerRowsInk, despues de la banda: antes se dibujaba
                // aqui y la banda de papel lo tapaba entero.
                // v0.303.0: el nombre y el cartel del puesto, en tinta.
                paintServerRowsInk(gfx, mouseX, mouseY);
                gfx.flush();
                // v0.323.0: la cabecera de la red local tambien la escribe el
                // juego en blanco. Sobre el papel no se lee: se cubre y se
                // reescribe en tinta, como las filas.
                try {
                    paintLanHeaderInk(gfx);
                } catch (Throwable ignored) {
                }
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
    private int countKnownSrc = -1;
    /** v0.328.0: hueco medido del ultimo recuento, para invalidar la cache. */
    private int countLimit = Integer.MIN_VALUE;
    private boolean countEnglish = false;
    private net.minecraft.client.gui.Font countFont;

    /** Todas las entradas que afectan al texto deben formar parte de la clave. */
    private void refreshServerCountCache(int count, int known, int limit, boolean english) {
        if (this.countSrc == count && this.countKnownSrc == known && this.countEnglish == english
                && this.countFont == this.font && this.countLimit == limit) return;
        if (count <= 0) {
            this.countText = english ? "none yet" : "ninguno anotado";
        } else if (count == 1) {
            this.countText = english ? "1 entry" : "1 anotado";
        } else {
            this.countText = english ? count + " entries" : count + " anotados";
        }
        // v0.313.0: cuantas de esas puertas ya se visitaron.
        // v0.318.0: si el añadido no cabe entre el titulo y la chapa, se
        // queda fuera. Antes se metia encima del titulo y se leia mezclado.
        String base = this.countText;
        String full = base;
        if (known > 0) {
            full = base + "  \u00b7  " + known
                + (english ? " known" : " conocidas");
        }
        if (this.font.width(full) <= limit) {
            this.countText = full;
        } else if (this.font.width(base) <= limit) {
            this.countText = base;
        } else {
            this.countText = "";
        }
        this.countWidth = this.font.width(this.countText);
        this.countSrc = count;
        this.countEnglish = english;
        this.countFont = this.font;
        this.countLimit = limit;
        this.countKnownSrc = known;
    }

    private void drawServerCount(GuiGraphics gfx, int panelX, int panelW, int titleY) {
        int count = 0;
        try {
            count = this.vanillaServers == null ? 0 : this.vanillaServers.size();
        } catch (Throwable ignored) {}
        boolean english = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        // v0.201.0: la frase y su ancho se armaban por cuadro; ahora sólo cuando
        // cambia el numero de servidores anotados/conocidos o el idioma.
        // v0.328.0: el hueco disponible tambien entra en la cuenta. Si no, al
        // cambiar el ancho de la ventana el recuento se quedaba con el texto
        // viejo (a veces vacio) hasta que cambiara el numero de puestos.
        int hueco = (panelX + panelW - 74)
            - Math.max(panelX + 20,
                this.headerTitleX + this.font.width(this.headerTitle) + 10);
        int knownCount = 0;
        try {
            knownCount = com.eternalcraft.gripeverde.client.integration.RecentAddresses.list().size();
        } catch (Throwable ignored) {}
        this.refreshServerCountCache(count, knownCount, hueco, english);
        String text = this.countText;
        int textW = this.countWidth;
        // v0.241.0: el recuento se corre a la izquierda de la chapa REG. X,
        // que ocupaba ese rincón.
        int x = panelX + panelW - 74 - textW;
        int y = titleY + 4;
        // v0.378.0: el recuento no puede caer sobre el titulo ni sobre la chapa.
        int titleRight = this.headerTitleX + this.font.width(this.headerTitle) + 12;
        if (x < titleRight) x = titleRight;
        if (x + textW > panelX + panelW - 66) return;
        gfx.drawString(this.font, text, x, y, DiaryChrome.INK_SUBTITLE, false);

        // v0.287.0: el puesto elegido y si ya se entro antes por esa puerta.
        try {
            String picked = "";
            try {
                var sel = this.vanillaList == null ? null : this.vanillaList.getSelected();
                if (sel != null) {
                    // v0.367.0: por tipo y de frente, sin getMethod ni getField
                    // por nombre (ofuscados en el juego real).
                    ServerData data = extractServerDataFromEntry(sel);
                    if (data != null && data.ip != null) picked = data.ip;
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
                int chosenY = this.height - PANEL_MARGIN_V - 20;
                // v0.338.0: la linea del elegido no puede entrar bajo la ultima
                // ficha; se mide contra el filo real de los botones.
                int lastRight = footButtonsRight();
                if (this.width >= 360 && chosenY > titleY + 40
                        && lx > lastRight + 10 && lx > panelX + panelW / 2) {
                    gfx.drawString(this.font, cut, lx, chosenY, DiaryChrome.INK_FLAVOR, false);
                }
            }
        } catch (Throwable ignored) {
        }

        // v0.301.0: el peso del archivo y la fecha se fueron de la cabecera;
        // caian sobre el subtitulo y no cabian. Ahora viven en el pie, junto al
        // folio, donde no tapan nada.
    }

    /**
     * v0.338.0: el folio y el peso de servers.dat, en el margen izquierdo de la
     * cabecera, donde no hay fichas ni cantos que los corten.
     */
    private void drawArchiveMargin(GuiGraphics gfx, int panelX, int panelW, int titleY) {
        boolean english = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        int limit = this.headerTitleX - 14 - (panelX + 24);
        if (limit < 70) return;
        String folio = english ? "sheet 1" : "hoja 1";
        gfx.drawString(this.font, DiaryChrome.fitCached(this.font, folio, limit),
            panelX + 24, titleY + 2, DiaryChrome.INK_FLAVOR, false);
        String detail = serversFileLine(english);
        if (detail.isEmpty()) return;
        gfx.drawString(this.font, DiaryChrome.fitCached(this.font, detail, limit),
            panelX + 24, titleY + 14, DiaryChrome.INK_FLAVOR, false);
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
        String date = stampedToday();
        
        if (weight.isEmpty()) return date;
        if (date.isEmpty()) return weight;
        return weight + "  ·  " + date;
    }

    // v0.360.0: la fecha del pie se arma una vez por minuto, no por cuadro.
    private static String stampedDate;
    private static long stampedDateMinute = Long.MIN_VALUE;

    private static String stampedToday() {
        long minute = System.currentTimeMillis() / 60_000L;
        if (minute == stampedDateMinute && stampedDate != null) return stampedDate;
        String[] roman = {"I","II","III","IV","V","VI","VII","VIII","IX","X","XI","XII"};
        try {
            java.util.Calendar c = java.util.Calendar.getInstance();
            stampedDate = c.get(java.util.Calendar.DAY_OF_MONTH) + "\u00b7"
                + roman[Math.max(0, Math.min(11, c.get(java.util.Calendar.MONTH)))] + "\u00b7"
                + c.get(java.util.Calendar.YEAR);
            stampedDateMinute = minute;
        } catch (Throwable ignored) {
            return "";
        }
        return stampedDate;
    }

    /** v0.338.0: hasta donde llegan las fichas del pie, a la derecha. */
    private int footButtonsRight() {
        int right = Integer.MIN_VALUE;
        for (DiaryButton b : new DiaryButton[] { diarySelect, diaryDirect, diaryAdd,
                diaryEdit, diaryDelete, diaryRefresh, diaryCancel }) {
            if (b == null || !b.visible) continue;
            right = Math.max(right, b.getX() + b.getWidth());
        }
        return right == Integer.MIN_VALUE ? 0 : right;
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
                // v0.367.0: la cabecera de red local se reconoce por no traer
                // ficha de servidor, no por el nombre de su clase.
                boolean lan = extractServerDataFromEntry(entry) == null;

                int entryTop = this.rowTopMethod != null
                    ? rowTopOf(i)
                    : y0 + 4 - (int) scrollAmount + i * itemHeight + headerHeight - 2;
                int entryBottom = entryTop + rowStepNow();

                if (entryBottom < listTop || entryTop > listBottom) continue;
                int drawTop = Math.max(entryTop, listTop);
                int drawBottom = Math.min(entryBottom, listBottom);
                if (drawBottom <= drawTop) continue;

                // v0.241.0: el puesto se monta sobre un recorte de papel, con
                // su margen alrededor; la placa oscura del juego queda adentro
                // como si fuera la foto pegada en la ficha.
                com.eternalcraft.gripeverde.client.render.PaperTexture.drawTornSlip(
                    gfx, rowLeft - 9, drawTop - 1, rowRight - rowLeft + 18,
                    drawBottom - drawTop + 2,
                    lan ? 0x33180C : 0xE6D2A6, lan ? 0xF0E2C0 : 0x422810, lan ? 236 : 210);
                if (lan) continue;

                // v0.384.0: el brillo de la fila bajo la mano y el lacre del
                // puesto oficial se pintaban AQUI, y despues la ficha se cubria
                // entera con la banda de papel: o sea que el brillo y el lacre no
                // se veian nunca. Ahora los pinta la banda, despues de taparla.
            }
        } catch (Throwable ignored) {}
    }

    /**
     * v0.303.0: el juego escribe el nombre y el cartel de cada puesto en blanco
     * y gris fijos, y sobre el papel claro no se leian. Se cubre solo la franja
     * de texto -el icono se respeta- y se reescriben en tinta, en el mismo sitio.
     */
    private void paintServerRowsInk(GuiGraphics gfx, int mouseX, int mouseY) {
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
            int textLeft = rowLeft + 42;
            int textRight = rowLeft + rowWidth - 46;
            int textW = Math.max(40, textRight - textLeft);
            for (int i = 0; i < children.size(); i++) {
                var entry = children.get(i);
                ServerData sd = extractServerDataFromEntry(entry);
                if (sd == null) {
                    // v0.389.0: la fila de la red local es la unica que quedaba
                    // como placa oscura del juego, en medio de filas de papel y
                    // con la letra blanca. Se le pone el mismo papel y la misma
                    // tinta que a la cabecera cuando la lista esta vacia.
                    try {
                        int entryTop = this.rowTopMethod != null
                            ? rowTopOf(i)
                            : vanillaListY0 + 4 - (int) scrollAmount + i * itemHeight
                                + vanillaHeaderHeight - 2;
                        int entryBottom = entryTop + rowStepNow();
                        if (entryBottom >= listTop && entryTop <= listBottom) {
                            int drawTop = Math.max(entryTop, listTop);
                            int drawBottom = Math.min(entryBottom, listBottom);
                            if (drawBottom - drawTop >= 12) {
                                int bandLeft = Math.max(0, rowLeft - 9);
                                int bandRight = Math.min(this.width, rowLeft + rowWidth + 9);
                                gfx.fill(bandLeft, drawTop, bandRight, drawBottom, 0xFFE6D2A6);
                                String line = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                                    ? "searching for games on your local network"
                                    : "buscando partidas en tu red local";
                                String cut = DiaryChrome.fitText(this.font, line,
                                    Math.max(40, bandRight - bandLeft - 16));
                                gfx.drawString(this.font, cut, bandLeft + 42, drawTop + 4,
                                    DiaryChrome.INK_SUBTITLE, false);
                                int sepY = drawBottom - 1;
                                gfx.fill(bandLeft, sepY, bandRight, sepY + 1, 0x20422810);
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                    continue;
                }
                // v0.367.0: los campos de ServerData se leen de frente. Con
                // getField("name") y getField("ip") no se encontraba nada en el
                // juego real (los nombres van ofuscados) y la fila salia sin
                // nombre ni direccion.
                String name = sd.name == null ? "" : sd.name;
                String ip = sd.ip == null ? "" : sd.ip;
                if (name == null || name.isEmpty()) name = ip;
                if (name == null || name.isEmpty()) continue;
                int entryTop = this.rowTopMethod != null
                    ? rowTopOf(i)
                    : vanillaListY0 + 4 - (int) scrollAmount + i * itemHeight
                        + vanillaHeaderHeight - 2;
                int entryBottom = entryTop + rowStepNow();
                if (entryBottom < listTop || entryTop > listBottom) continue;
                int drawTop = Math.max(entryTop, listTop);
                int drawBottom = Math.min(entryBottom, listBottom);
                if (drawBottom - drawTop < 10) continue;

                // v0.378.0: la ficha se dibujaba como un trozo de papel encima
                // de la fila que pintaba el juego, y el juego escribia DESPUES
                // su nombre blanco y su MOTD naranja: la fila salia a medias,
                // con el recuento pisando la chapa y el texto fuera del papel.
                // Ahora se cubre la fila entera con el papel de la hoja y la
                // ficha se escribe encima, sin dejar nada del vanilla a la vista.
                int slipTop = drawTop + 1;
                int slipH = Math.max(16, drawBottom - drawTop - 2);
                // v0.384.0: la banda cubre la ficha ENTERA -de canto a canto- y
                // con el mismo papel que la ficha, asi no queda un reborde mas
                // claro a los lados que parezca una raya junto al puesto.
                int bandLeft = Math.max(0, rowLeft - 9);
                int bandRight = Math.min(this.width, rowLeft + rowWidth + 9);
                gfx.fill(bandLeft, drawTop, bandRight, drawBottom, 0xFFE6D2A6);

                // v0.384.0: la fila bajo la mano se aclara y la elegida queda
                // marcada; ambas cosas van despues de la banda, que es la que
                // tapaba el resaltado del juego.
                int rowRight = rowLeft + rowWidth;
                boolean over = mouseX >= rowLeft - 9 && mouseX <= rowRight + 9
                    && mouseY >= drawTop - 1 && mouseY < drawBottom + 1;
                Object chosen = null;
                try {
                    chosen = this.vanillaList.getSelected();
                } catch (Throwable ignored) {
                }
                if (chosen == entry) {
                    gfx.fill(bandLeft, drawTop, bandRight, drawBottom, 0x1E6B5533);
                } else if (over) {
                    gfx.fill(bandLeft, drawTop, bandRight, drawBottom, 0x22F3E7C8);
                }

                // v0.386.0: el filete que separa un puesto del siguiente. Se
                // pinta despues de la banda -que era la que lo tapaba- y con el
                // desvanecido a los dos lados, como antes.
                int sepY = drawBottom - 1;
                if (sepY > drawTop + 6) {
                    int fadeExt = 12;
                    gfx.fill(bandLeft, sepY, bandLeft + fadeExt, sepY + 1, 0x20422810);
                    gfx.fill(bandLeft + fadeExt, sepY, bandRight - fadeExt, sepY + 1, 0x60422810);
                    gfx.fill(bandRight - fadeExt, sepY, bandRight, sepY + 1, 0x20422810);
                }

                // v0.251.0: el puesto oficial lleva su lacre en el canto izquierdo.
                if (isPinnedServer(sd)) {
                    int sealX = rowLeft - 9;
                    gfx.fill(sealX, drawTop, sealX + 3, drawBottom, 0xB05C0A08);
                    gfx.fill(sealX + 1, drawTop + 3, sealX + 2, drawTop + 7, 0x70E8DCB4);
                }
                // Asienta los fondos, resaltados y filetes antes de escribir la
                // imagen y la tinta; cada RenderType tiene su propio buffer.
                gfx.flush();

                // v0.382.0: se acabo el sello con la inicial. El puesto va con
                // su propia imagen (la del servidor) y, si el puesto no tiene
                // ninguna, con el lacre de la casa.
                int box = Math.min(32, Math.max(16, drawBottom - drawTop - 4));
                int boxX = bandLeft + 4;
                int boxY = drawTop + Math.max(1, (drawBottom - drawTop - box) / 2);
                net.minecraft.resources.ResourceLocation favicon = serverIcon(sd);
                if (favicon != null) {
                    // Se escala el PNG completo; pasar `box` como tamaño de
                    // textura recortaba una imagen 64x64 a su cuarto superior.
                    drawServerIcon(gfx, sd, favicon, boxX, boxY, box);
                    gfx.fill(boxX, boxY, boxX + box, boxY + 1, 0x552A1C12);
                    gfx.fill(boxX, boxY + box - 1, boxX + box, boxY + box, 0x552A1C12);
                    gfx.fill(boxX, boxY, boxX + 1, boxY + box, 0x552A1C12);
                    gfx.fill(boxX + box - 1, boxY, boxX + box, boxY + box, 0x552A1C12);
                } else {
                    String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
                    gfx.fill(boxX, boxY, boxX + box, boxY + box, 0xFF8C2A1E);
                    int iw = this.font.width(initial);
                    gfx.drawString(this.font, initial, boxX + (box - iw) / 2,
                        boxY + (box - 7) / 2, 0xFFF3E7C8, false);
                }

                // v0.382.0: los jugadores activos, en el canto derecho de la
                // ficha, como el recuento de un libro de visitas.
                String players = serverPlayers(sd);
                int playersW = players.isEmpty() ? 0 : this.font.width(players);
                int nameLimit = Math.max(40, textW - (playersW > 0 ? playersW + 10 : 0));
                String shownName = DiaryChrome.fitText(this.font, name, nameLimit);
                gfx.drawString(this.font, shownName, textLeft, slipTop + 2, 0xFF1A1108, false);
                if (playersW > 0) {
                    gfx.drawString(this.font, players, textLeft + textW - playersW, slipTop + 2,
                        0xFF6B5533, false);
                }

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
                    // v0.380.0: la descripcion del puesto se sigue doblando por
                    // palabras, pero ahora usa todos los renglones que caben en
                    // la ficha -tres en el alto normal- y no se remata con
                    // puntos suspensivos: la casa no recorta lo que el puesto
                    // cuenta de si mismo.
                    int room = Math.max(1, (slipH - 13) / 9);
                    int maxLines = Math.min(3, room);
                    java.util.List<String> wrapped = wrapToWidth(this.font, line2,
                        Math.max(60, textW), maxLines);
                    for (int r = 0; r < wrapped.size(); r++) {
                        gfx.drawString(this.font, wrapped.get(r), textLeft,
                            slipTop + 13 + r * 9, 0xFF6B5533, false);
                    }
                }
            }
            gfx.flush();
            if (scissored) {
                try {
                    gfx.disableScissor();
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * v0.377.0: dobla un texto por palabras al ancho dado, hasta `maxLines`.
     * Si no cabe en su ancho, sigue en el renglon siguiente en vez de cortarse
     * con puntos suspensivos y perder el final.
     */
    private static java.util.List<String> wrapToWidth(Font font, String text, int maxWidth,
                                                      int maxLines) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (font == null || text == null || text.isEmpty() || maxWidth <= 0 || maxLines <= 0) {
            return out;
        }
        String rest = text.trim();
        while (!rest.isEmpty() && out.size() < maxLines) {
            if (font.width(rest) <= maxWidth) {
                out.add(rest);
                return out;
            }
            int cut = rest.length();
            while (cut > 0 && font.width(rest.substring(0, cut)) > maxWidth) cut--;
            if (cut <= 0) return out;
            int space = rest.lastIndexOf(' ', cut);
            if (space > 0) cut = space;
            String piece = rest.substring(0, cut).trim();
            rest = rest.substring(Math.min(rest.length(), cut)).trim();
            if (!piece.isEmpty()) out.add(piece);
        }
        // v0.380.0: aqui se remataba el ultimo renglon con puntos suspensivos.
        // El puesto sigue contando su historia en la ficha completa (la que se
        // abre al elegirlo), asi que en el renglon no se pone nada: mejor una
        // frase cortada por el canto que un "..." de la casa.
        return out;
    }

    /**
     * v0.323.0: "Buscando partidas en tu red local...", que el juego escribe en
     * blanco sobre el papel claro. Se tapa con un recorte y se reescribe en
     * tinta, solo cuando la lista esta vacia, que es cuando esa cabecera se ve.
     */
    private void paintLanHeaderInk(GuiGraphics gfx) {
        if (this.vanillaList == null || this.vanillaServers == null) return;
        if (this.vanillaServers.size() > 0) return;
        int headerH = vanillaHeaderHeight;
        if (headerH <= 2) return;
        int listTop = this.vanillaList.getTop();
        int listBottom = this.vanillaList.getBottom();
        int rowLeft = this.vanillaList.getRowLeft();
        int rowWidth = this.vanillaList.getRowWidth();
        double scroll = 0.0;
        try {
            scroll = this.vanillaList.getScrollAmount();
        } catch (Throwable ignored) {
        }
        int top = (int) Math.round(vanillaListY0 + 4 - scroll);
        int bottom = top + headerH - 2;
        int drawTop = Math.max(top, listTop);
        int drawBottom = Math.min(bottom, listBottom);
        if (drawBottom - drawTop < 12) return;
        com.eternalcraft.gripeverde.client.render.PaperTexture.drawTornSlip(
            gfx, rowLeft - 9, drawTop - 1, rowWidth + 18,
            drawBottom - drawTop + 2, 0xE6D2A6, 0x422810, 232);
        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        String line = en ? "searching for games on your local network"
            : "buscando partidas en tu red local";
        String cut = DiaryChrome.fitCached(this.font, line, Math.max(40, rowWidth - 8));
        gfx.drawString(this.font, cut, rowLeft + 4, drawTop + 3,
            DiaryChrome.INK_SUBTITLE, false);
    }

    /** Valida el encabezado PNG antes de decodificar un favicon remoto. */
    private static boolean hasSafeServerIconHeader(byte[] bytes) {
        if (bytes == null || bytes.length < 24 || bytes.length > MAX_SERVER_ICON_BYTES) return false;
        if ((bytes[0] & 0xFF) != 0x89 || bytes[1] != 'P' || bytes[2] != 'N'
                || bytes[3] != 'G' || (bytes[4] & 0xFF) != 0x0D
                || (bytes[5] & 0xFF) != 0x0A || (bytes[6] & 0xFF) != 0x1A
                || (bytes[7] & 0xFF) != 0x0A || readPngInt(bytes, 8) != 13
                || bytes[12] != 'I' || bytes[13] != 'H'
                || bytes[14] != 'D' || bytes[15] != 'R') {
            return false;
        }
        int width = readPngInt(bytes, 16);
        int height = readPngInt(bytes, 20);
        return width > 0 && height > 0
            && width <= MAX_SERVER_ICON_DIMENSION && height <= MAX_SERVER_ICON_DIMENSION;
    }

    private static int readPngInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) | ((bytes[offset + 1] & 0xFF) << 16)
            | ((bytes[offset + 2] & 0xFF) << 8) | (bytes[offset + 3] & 0xFF);
    }

    /**
     * La imagen que el servidor manda en su ping. Se limita su tamaño antes de
     * decodificarla y, al dibujarla, se escala la imagen completa al recuadro.
     */
    private net.minecraft.resources.ResourceLocation serverIcon(ServerData data) {
        String key = null;
        try {
            key = data == null ? null : data.ip;
        } catch (Throwable ignored) {
        }
        if (key == null || key.isEmpty()) return null;
        net.minecraft.resources.ResourceLocation cached = SERVER_ICONS.get(key);
        if (cached != null) return cached;
        Long retryAt = SERVER_ICON_RETRY_AT.get(key);
        if (retryAt != null) {
            if (System.currentTimeMillis() < retryAt) return null;
            SERVER_ICON_RETRY_AT.remove(key);
        }
        if (this.minecraft == null) return null;
        try {
            byte[] bytes = null;
            for (java.lang.reflect.Field f : ServerData.class.getDeclaredFields()) {
                if (f.getType() != byte[].class) continue;
                f.setAccessible(true);
                Object v = f.get(data);
                if (v instanceof byte[] b && b.length > 8) {
                    bytes = b;
                    break;
                }
            }
            if (!hasSafeServerIconHeader(bytes)) {
                SERVER_ICON_RETRY_AT.put(key,
                    System.currentTimeMillis() + SERVER_ICON_RETRY_DELAY_MS);
                return null;
            }
            com.mojang.blaze3d.platform.NativeImage image;
            try (java.io.InputStream in = new java.io.ByteArrayInputStream(bytes)) {
                image = com.mojang.blaze3d.platform.NativeImage.read(in);
            }
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            if (imageWidth <= 0 || imageHeight <= 0
                    || imageWidth > MAX_SERVER_ICON_DIMENSION
                    || imageHeight > MAX_SERVER_ICON_DIMENSION
                    || imageWidth != readPngInt(bytes, 16)
                    || imageHeight != readPngInt(bytes, 20)) {
                image.close();
                SERVER_ICON_RETRY_AT.put(key,
                    System.currentTimeMillis() + SERVER_ICON_RETRY_DELAY_MS);
                return null;
            }
            net.minecraft.resources.ResourceLocation rl = new net.minecraft.resources.ResourceLocation(
                "gripeverde", "server_icon/"
                    + Long.toHexString(SERVER_ICON_TEXTURE_IDS.incrementAndGet()));
            this.minecraft.getTextureManager()
                .register(rl, new net.minecraft.client.renderer.texture.DynamicTexture(image));
            SERVER_ICONS.put(key, rl);
            SERVER_ICON_SIZES.put(key, new ServerIconSize(imageWidth, imageHeight));
            SERVER_ICON_RETRY_AT.remove(key);
            return rl;
        } catch (Throwable ignored) {
            SERVER_ICON_RETRY_AT.put(key,
                System.currentTimeMillis() + SERVER_ICON_RETRY_DELAY_MS);
            return null;
        }
    }

    /** Dibuja toda la textura, centrada y ajustada al recuadro de la ficha. */
    private static void drawServerIcon(GuiGraphics gfx, ServerData data,
                                       net.minecraft.resources.ResourceLocation icon,
                                       int x, int y, int box) {
        if (gfx == null || data == null || data.ip == null || icon == null || box <= 0) return;
        ServerIconSize size = SERVER_ICON_SIZES.get(data.ip);
        if (size == null || size.width() <= 0 || size.height() <= 0) return;
        float fit = box / (float) Math.max(size.width(), size.height());
        int drawWidth = Math.max(1, Math.round(size.width() * fit));
        int drawHeight = Math.max(1, Math.round(size.height() * fit));
        int drawX = x + (box - drawWidth) / 2;
        int drawY = y + (box - drawHeight) / 2;
        gfx.pose().pushPose();
        try {
            gfx.pose().translate(drawX, drawY, 0.0F);
            gfx.pose().scale(drawWidth / (float) size.width(),
                drawHeight / (float) size.height(), 1.0F);
            gfx.blit(icon, 0, 0, 0, 0, size.width(), size.height(),
                size.width(), size.height());
        } finally {
            gfx.pose().popPose();
        }
    }

    /**
     * v0.382.0: cuantos jugadores hay en el puesto ("3/20"), leido del propio
     * juego: se busca el texto con forma de recuento entre sus campos, sin
     * depender del nombre del campo.
     */
    private static String serverPlayers(ServerData data) {
        if (data == null) return "";
        try {
            for (java.lang.reflect.Field f : ServerData.class.getDeclaredFields()) {
                if (f.getType() != net.minecraft.network.chat.Component.class) continue;
                f.setAccessible(true);
                Object v = f.get(data);
                if (!(v instanceof net.minecraft.network.chat.Component c)) continue;
                String text = c.getString();
                if (text != null && text.matches("\\d+\\s*/\\s*\\d+")) return text.trim();
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    private static String motdText(ServerData serverData) {
        try {
            if (serverData == null || serverData.motd == null) return "";
            String s = serverData.motd.getString();
            return s == null ? "" : s.replace('\n', ' ').trim();
        } catch (Throwable ignored) {
            return "";
        }
    }

    /**
     * Dibuja un subrayado sutil bajo cada server (estilo tarjeta de biblioteca).
     * No dibuja marco completo - solo una linea horizontal fina de tinta.
     */

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
                if (extractServerDataFromEntry(children.get(i)) == null) {
                    lanIdx = i;
                    break;
                }
            }
            if (lanIdx == -1) return 0; // no hay LAN header, no hay que recortar

            // Y donde empieza la entry LAN (todo abajo se oculta)
            int lanTop = this.rowTopMethod != null
                ? rowTopOf(lanIdx)
                : y0 + 4 - (int) scrollAmount + lanIdx * itemHeight + headerHeight;
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
        // Respaldo por metodo publico: getTop() es y0, siempre.
        if (vanillaListY0 <= 0) {
            try {
                vanillaListY0 = this.vanillaList.getTop();
            } catch (Throwable ignored) {
            }
        }
        resolveRowTopMethod();
        if (vanillaItemHeight <= 0 && vanillaRowStep > 0) vanillaItemHeight = vanillaRowStep;
    }

    /**
     * v0.390.0: busca el metodo de la lista que dice donde empieza una fila.
     *
     * Recibe un indice y devuelve una coordenada: se prueba con 0 y con 1 y la
     * diferencia tiene que dar el alto de renglon. Entre los dos candidatos
     * (arriba y abajo de la fila) se queda el que devuelve el numero menor. Si
     * no aparece ninguno, se sigue con las medidas de siempre.
     */
    private void resolveRowTopMethod() {
        this.rowTopMethod = null;
        this.vanillaRowStep = 0;
        if (this.vanillaList == null) return;
        java.lang.reflect.Method best = null;
        int bestValue = Integer.MAX_VALUE;
        int bestStep = 0;
        for (Class<?> c = this.vanillaList.getClass(); c != null && c != Object.class;
             c = c.getSuperclass()) {
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                if (m.getParameterCount() != 1) continue;
                if (m.getParameterTypes()[0] != int.class || m.getReturnType() != int.class) continue;
                if (java.lang.reflect.Modifier.isStatic(m.getModifiers())) continue;
                try {
                    m.setAccessible(true);
                    int a = (Integer) m.invoke(this.vanillaList, 0);
                    int b = (Integer) m.invoke(this.vanillaList, 1);
                    int step = b - a;
                    if (step < 10 || step > 80) continue;
                    if (a < bestValue) {
                        bestValue = a;
                        best = m;
                        bestStep = step;
                    }
                } catch (Throwable ignored) {
                }
            }
        }
        this.rowTopMethod = best;
        if (bestStep > 0) this.vanillaRowStep = bestStep;
    }

    /** Donde empieza la fila `i`, segun la propia lista. */
    private int rowTopOf(int i) {
        try {
            return (Integer) this.rowTopMethod.invoke(this.vanillaList, i);
        } catch (Throwable ignored) {
            return 0;
        }
    }

    /** Alto de renglon, con lo que diga la lista y, si no, lo cacheado. */
    private int rowStepNow() {
        if (this.vanillaRowStep > 0) return this.vanillaRowStep;
        return vanillaItemHeight > 0 ? vanillaItemHeight : 36;
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
