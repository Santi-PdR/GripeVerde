package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.ExpedientHeader;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryProgressBar;
import com.eternalcraft.gripeverde.client.widgets.DiarySlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.eternalcraft.gripeverde.client.screens.DiaryAccessibilityOptionsScreen;
import com.eternalcraft.gripeverde.client.screens.DiaryChatOptionsScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import com.eternalcraft.gripeverde.client.screens.DiarySkinCustomizationScreen;
import com.eternalcraft.gripeverde.client.screens.DiarySoundOptionsScreen;
import net.minecraft.network.chat.Component;

/**
 * DiaryOptionsScreen: reemplazo victoriano de OptionsScreen.
 * Fondo: Title vivo detras (skyline + atmosfera) + overlay oscuro + panel de pergamino.
 *
 * Fallbacks (abren OptionsScreen vanilla):
 *   - Telemetria, En linea, Paquetes de recursos, Creditos: firma no trivial en 1.20.1.
 */
public class DiaryOptionsScreen extends Screen {

    // Rotating flavor quotes from the chronicler. Localized here because they
    // are rotating flavour text, not fixed button labels.
    private static final String[] FLAVOR_POOL_ES = {
        "Esta noche el gas arde bajo.",
        "Los registros guardan mas de lo que muestran.",
        "El viento trae rumores del este.",
        "Alguien movio las cortinas del tercer piso.",
        "La tinta todavia no seco.",
        "Hay un olor nuevo en el aire, dulce y enfermo.",
        "Los relojes del barrio no coinciden.",
        "Otra pagina en blanco espera."
    };
    private static final String[] FLAVOR_POOL_EN = {
        "Tonight the gas burns low.",
        "The records keep more than they show.",
        "The wind brings rumours from the east.",
        "Someone moved the third-floor curtains.",
        "The ink has not yet dried.",
        "There is a new scent in the air, sweet and sickly.",
        "The neighbourhood clocks do not agree.",
        "Another blank page waits."
    };
    private static int flavorIndex = -1;
    private static String flavorLang = null;
    private String currentFlavor;

    private final Screen parent;

    // Botones del grid
    private static final int BTN_W = 178;
    private static final int BTN_H = 28;
    private static final int GAP_X = 14;
    private static final int GAP_Y = 8;

    // Boton Volver
    private static final int BACK_W = 172;
    private static final int BACK_H = 26;

    // Interior del panel
    private static final int PAD_TOP = 18;
    private static final int SUBTITLE_GAP = 11;
    private static final int TITLE_H = 12;
    private static final int TITLE_GAP = 14;
    private static final int GRID_ROWS = 6;
    private static final int GAP_BEFORE_BACK = 16;
    private static final int PAD_BOTTOM = 18;
    private static final int PAD_SIDES = 24;
    private static final int INFO_RAIL_W = 152;
    private static final int INFO_RAIL_GAP = 18;

    // Panel calculado
    private int panelX, panelY, panelW, panelH;
    private int gridTop, col1X, col2X;
    private int railX, railY, railW;
    private long openedAtMs = 0L;

    // ── v0.16.0: la mesa de trabajo responde ──
    private static final long[] knockTimes = new long[6];
    private static int knockCount = 0;
    private static long knockBackAtMs = 0L;
    private static boolean knockBackPlayed = false;
    private static boolean drawerOpen = false;
    private static float drawerAnim = 0f;
    private static final int DRAWER_H = 26;

    public DiaryOptionsScreen(Screen parent) {
        super(Component.translatable("gripeverde.options.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        com.eternalcraft.gripeverde.client.render.PaperTexture.incrementOpenCount();
        // Rotate flavor quote each time options screen opens, respecting language.
        String[] flavorPool = getFlavorPool();
        String currentLang = currentLanguageCode();
        if (!java.util.Objects.equals(flavorLang, currentLang)) {
            flavorLang = currentLang;
            flavorIndex = -1;
        }
        flavorIndex = (flavorIndex + 1) % flavorPool.length;
        currentFlavor = flavorPool[flavorIndex];

        int gridH = GRID_ROWS * BTN_H + (GRID_ROWS - 1) * GAP_Y;
        int gridW = BTN_W * 2 + GAP_X;
        panelW = Math.min(560, this.width - 24);
        panelW = Math.max(gridW + INFO_RAIL_W + INFO_RAIL_GAP + PAD_SIDES * 2, panelW);
        panelW = Math.min(panelW, this.width - 24);
        panelH = PAD_TOP + TITLE_H + SUBTITLE_GAP + TITLE_GAP + gridH + GAP_BEFORE_BACK + BACK_H + PAD_BOTTOM + DRAWER_H + 22;

        // v0.16.0: la bisagra cuenta las veces que el gabinete se abre.
        try {
            int opens = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.bump("cabinetOpens");
            if (opens == 25) {
                com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                    com.eternalcraft.gripeverde.client.lore.Discoveries.HINGE);
                com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                    com.eternalcraft.gripeverde.client.lore.Marginalia.N_HINGE);
            }
            if (opens >= 25) {
                com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                    com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_WOOD_CREAK.get(), 0.20f, 0.55f);
            }
        } catch (Throwable ignored) {}

        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        int cx = this.width / 2;
        this.gridTop = panelY + PAD_TOP + TITLE_H + SUBTITLE_GAP + TITLE_GAP + 22;

        int contentLeft = panelX + PAD_SIDES;
        this.col1X = contentLeft;
        this.col2X = this.col1X + BTN_W + GAP_X;
        this.railW = INFO_RAIL_W;
        this.railX = panelX + panelW - PAD_SIDES - railW;
        this.railY = panelY + 92;

        addBtn(this.col1X, this.gridTop + row(0), "gripeverde.options.button.skin", DiaryButton.Icon.PALETTE, DiaryButton.SoundProfile.OPTION_OPEN, this::openSkin);
        addBtn(this.col2X, this.gridTop + row(0), "gripeverde.options.button.sound", DiaryButton.Icon.SOUND, DiaryButton.SoundProfile.OPTION_OPEN, this::openSound);
        addBtn(this.col1X, this.gridTop + row(1), "gripeverde.options.button.video", DiaryButton.Icon.EYE, DiaryButton.SoundProfile.OPTION_OPEN, this::openVideo);
        addBtn(this.col2X, this.gridTop + row(1), "gripeverde.options.button.controls", DiaryButton.Icon.CONTROLS, DiaryButton.SoundProfile.OPTION_OPEN, this::openControls);
        addBtn(this.col1X, this.gridTop + row(2), "gripeverde.options.button.language", DiaryButton.Icon.LANGUAGE, DiaryButton.SoundProfile.OPTION_OPEN, this::openLanguage);
        addBtn(this.col2X, this.gridTop + row(2), "gripeverde.options.button.chat", DiaryButton.Icon.CHAT, DiaryButton.SoundProfile.OPTION_OPEN, this::openChat);
        addBtn(this.col1X, this.gridTop + row(3), "gripeverde.options.button.resourcepacks", DiaryButton.Icon.PACK, DiaryButton.SoundProfile.OPTION_OPEN, this::openResourcePacks);
        addBtn(this.col2X, this.gridTop + row(3), "gripeverde.options.button.accessibility", DiaryButton.Icon.ACCESS, DiaryButton.SoundProfile.OPTION_OPEN, this::openAccessibility);
        addBtn(this.col1X, this.gridTop + row(4), "gripeverde.options.button.online", DiaryButton.Icon.ONLINE, DiaryButton.SoundProfile.OPTION_OPEN, this::openOnline);
        addBtn(this.col2X, this.gridTop + row(4), "gripeverde.options.button.credits", DiaryButton.Icon.CREDITS, DiaryButton.SoundProfile.OPTION_OPEN, this::openCredits);

        // Slider de campo de vision (como el de OptionsScreen vanilla: 30-110, por defecto 70)
        // OJO: la clave vanilla "options.fov" es solo "FOV" (sin placeholder), por eso el
        // numero se agrega explicitamente al label: "FOV: 70" / "Campo de vision: 70".
        var mcOptions = Minecraft.getInstance().options;
        int fovX = this.col1X;
        int fovW = BTN_W * 2 + GAP_X;
        int fovY = this.gridTop + row(5) + (BTN_H - 20) / 2;
        double fovNorm = (mcOptions.fov().get() - 30.0) / 80.0;
        this.addRenderableWidget(new DiarySlider(fovX, fovY, fovW, 20, fovNorm,
                v -> {
                    int fov = (int) Math.round(30.0 + v * 80.0);
                    return Component.translatable("options.fov").getString() + ": " + fov;
                },
                v -> mcOptions.fov().set((int) Math.round(30.0 + v * 80.0))));

        int backX = cx - BACK_W / 2;
        int backY = panelY + panelH - PAD_BOTTOM - DRAWER_H - BACK_H;
        // Nota: el cajon ocupa la franja inferior DRAWER_H del panel.
        this.addRenderableWidget(new DiaryButton(backX, backY, BACK_W, BACK_H,
                Component.translatable("gripeverde.options.button.done"),
                DiaryButton.Icon.DOOR, DiaryButton.Variant.PRIMARY,
                DiaryButton.SoundProfile.BACK,
                this::onBack));
        this.openedAtMs = System.currentTimeMillis();
    }

    private static int row(int i) {
        return i * (BTN_H + GAP_Y);
    }

    private static String currentLanguageCode() {
        try {
            String lang = net.minecraft.client.Minecraft.getInstance().getLanguageManager().getSelected();
            return lang == null ? "en_us" : lang;
        } catch (Throwable ignored) {
            return "en_us";
        }
    }

    private static String[] getFlavorPool() {
        return com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? FLAVOR_POOL_EN : FLAVOR_POOL_ES;
    }

    private void addBtn(int x, int y, String translationKey, DiaryButton.Icon icon,
                        DiaryButton.SoundProfile soundProfile, Runnable action) {
        this.addRenderableWidget(new DiaryButton(x, y, BTN_W, BTN_H,
                Component.translatable(translationKey), icon, DiaryButton.Variant.SECONDARY,
                soundProfile, action));
    }

    @Override
    public void tick() {
        super.tick();
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);

        float reveal = DiaryChrome.reveal(this.openedAtMs, 260L);
        DiaryChrome.renderPanelBase(g, panelX, panelY, panelW, panelH, DiaryTheme.PLAGUE_GREEN, PaperTexture.PaperKind.LEDGER);

        String reference = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "EC-QV/1897 · CENTRAL DESK" : "EC-QV/1897 · MESA CENTRAL";
        String subtitle = "- " + Component.translatable("gripeverde.options.subtitle").getString() + " -";
        String status = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "ACTIVE" : "ACTIVO";
        int headerBottom = ExpedientHeader.draw(g, this.font, panelX, panelY, panelW,
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "EXPEDIENT I" : "EXPEDIENTE I",
            this.title,
            subtitle,
            reference,
            currentFlavor,
            status,
            DiaryTheme.PLAGUE_SICK,
            reveal);

        int contentLeft = this.col1X - 10;
        int contentRight = this.col2X + BTN_W + 10;
        int contentW = contentRight - contentLeft;

        drawOptionsGroup(g, contentLeft, this.gridTop - 16, contentW, row(3) + BTN_H + 8,
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "OPERATIVE SURFACES" : "SUPERFICIES OPERATIVAS",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Appearance, sound, vision and correspondence." : "Aspecto, sonido, vision y correspondencia.",
            DiaryTheme.BRONZE);
        drawOptionsGroup(g, contentLeft, this.gridTop + row(3) - 8, contentW, row(2) + BTN_H + 10,
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "DISTRICT RECORDS" : "REGISTROS DEL DISTRITO",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "External routes, public notices and sealed attributions." : "Rutas externas, avisos publicos y atribuciones selladas.",
            DiaryTheme.PLAGUE_SICK);
        drawOptionsGroup(g, contentLeft, this.gridTop + row(5) - 8, contentW, BTN_H + 20,
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "OPTICAL CALIBRATION" : "CALIBRACION OPTICA",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Field of view remains within arm's reach." : "El campo de vision queda al alcance de la mano.",
            DiaryTheme.AMBER_FLAME);

        // Carril lateral de estado
        drawOptionsRail(g, reveal);

        int backY = panelY + panelH - PAD_BOTTOM - DRAWER_H - BACK_H;
        int divY2 = backY - 10;
        DiaryChrome.drawInkDivider(g, panelX + 24, divY2, panelW - 48);

        // Reveal documental del panel
        if (reveal < 0.995f) {
            int revealBottom = panelY + Math.max(34, (int)(panelH * reveal));
            g.enableScissor(panelX - 2, panelY - 2, panelX + panelW + 2, revealBottom);
            super.render(g, mouseX, mouseY, partialTick);
            g.disableScissor();
        } else {
            super.render(g, mouseX, mouseY, partialTick);
        }

        try {
            drawDrawer(g, mouseX, mouseY);
            processKnockBack();
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, panelY - 14);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {}
    }

    private void drawOptionsGroup(GuiGraphics g, int x, int y, int w, int h, String label, String body, int accent) {
        DiaryChrome.drawInnerRail(g, x, y, w, h, accent);
        g.drawString(this.font, label, x + 10, y + 8, accent, false);
        g.drawString(this.font, body, x + 10, y + 19, DiaryChrome.INK_FLAVOR, false);
        g.fill(x + 10, y + 31, x + w - 10, y + 32, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 48));
    }

    private void drawOptionsRail(GuiGraphics g, float reveal) {
        int found = 0;
        int total = com.eternalcraft.gripeverde.client.lore.Discoveries.ALL.length;
        try { found = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.discoveredCount(); } catch (Throwable ignored) {}
        String locale = currentLanguageCode().toUpperCase(java.util.Locale.ROOT);
        String locTitle = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "ACTIVE TONGUE" : "LENGUA ACTIVA";
        String locBody = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Current registry and spoken district." : "Registro actual y distrito en uso.";
        DiaryChrome.drawInsetCard(g, this.font, railX, railY, railW, 66, locTitle, locale, locBody, DiaryTheme.BRONZE);

        int meterY = railY + 74;
        DiaryChrome.drawInsetCard(g, this.font, railX, meterY, railW, 86,
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "ACOUSTIC STATE" : "ESTADO ACUSTICO",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "music & room tone" : "musica y ambiente",
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Music and room tone are carried through the whole cabinet." : "La musica y la resonancia atraviesan todo el gabinete.",
            DiaryTheme.PLAGUE_SICK);
        DiaryProgressBar.draw(g, railX + 10, meterY + 38, railW - 20, 12,
            MenuMusic.getUserVolume(), com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Gramophone" : "Gramofono", DiaryProgressBar.Style.AMBER);
        DiaryProgressBar.draw(g, railX + 10, meterY + 56, railW - 20, 12,
            com.eternalcraft.gripeverde.client.audio.MenuAmbience.getUserVolume(),
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Room tone" : "Ambiente", DiaryProgressBar.Style.PLAGUE);

        int archiveY = meterY + 94;
        DiaryChrome.drawInsetCard(g, this.font, railX, archiveY, railW, 88,
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "ARCHIVE" : "ARCHIVO",
            (com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Reading room progress" : "Progreso de la sala de consulta"),
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "The cabinet keeps count of every recovered card." : "El gabinete lleva la cuenta de cada ficha recuperada.",
            DiaryTheme.REDCOAT);
        DiaryProgressBar.draw(g, railX + 10, archiveY + 42, railW - 20, 14,
            total <= 0 ? 0f : (found / (float) total), found + " / " + total, DiaryProgressBar.Style.ALERT);
        DiaryChrome.drawValueRow(g, this.font, railX + 10, archiveY + 63, railW - 20,
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Section count" : "Cantidad de secciones", "10", DiaryTheme.INK);

        int sealY = archiveY + 96;
        DiaryChrome.drawValueRow(g, this.font, railX + 10, sealY + 4, railW - 20,
            com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "GUI scale" : "Escala GUI", String.valueOf(this.minecraft.options.guiScale().get()), DiaryTheme.BRONZE);
    }

    private int drawerX() { return panelX + panelW - PAD_SIDES - 92; }
    private int drawerY() { return panelY + panelH - PAD_BOTTOM - DRAWER_H + 5; }

    private void drawDrawer(GuiGraphics g, int mouseX, int mouseY) {
        int x = drawerX(), y = drawerY(), w = 92, h = 16;
        boolean hasKey = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory
            .hasCollectible(com.eternalcraft.gripeverde.client.lore.Discoveries.ITEM_KEY);
        drawerAnim += (drawerOpen ? 0.18f : -0.18f);
        drawerAnim = Math.max(0f, Math.min(1f, drawerAnim));

        int slide = (int)(drawerAnim * 16f);
        // Hueco oscuro cuando abre
        if (slide > 0) {
            g.fill(x, y, x + w, y + slide, 0xFF120A05);
        }
        // Frente del cajon
        int fy = y + slide;
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= fy && mouseY <= fy + h;
        int wood = hover ? 0xFF8E6A35 : 0xFF7A5A2E;
        g.fill(x, fy, x + w, fy + h, wood);
        g.fill(x, fy, x + w, fy + 1, 0xFFB89A6E);
        g.fill(x, fy + h - 1, x + w, fy + h, 0xFF3D2A18);
        g.fill(x, fy, x + 1, fy + h, 0xFF3D2A18);
        g.fill(x + w - 1, fy, x + w, fy + h, 0xFF3D2A18);
        // Tirador
        g.fill(x + w / 2 - 5, fy + h / 2 - 1, x + w / 2 + 5, fy + h / 2 + 2, 0xFFC8A83E);
        if (!drawerOpen) {
            if (hasKey) {
                // Cerradura abierta: el ojo del lector ya sabe
                g.fill(x + w / 2 - 1, fy + h / 2 - 4, x + w / 2 + 1, fy + h / 2 - 1, 0xFF241608);
            } else {
                // Cerradura con llave ausente
                g.fill(x + w / 2 - 1, fy + h / 2 - 5, x + w / 2 + 1, fy + h / 2 - 2, 0xFF241608);
                g.fill(x + w / 2, fy + h / 2 - 2, x + w / 2 + 1, fy + h / 2 + 1, 0xFF241608);
            }
        } else {
            // Contenido: los objetos pequenos encontrados
            drawDrawerContents(g, x, y);
        }
    }

    private void drawDrawerContents(GuiGraphics g, int x, int y) {
        var mem = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.get();
        int[] xs = { x + 8, x + 26, x + 44, x + 62, x + 78 };
        String[] ids = {
            com.eternalcraft.gripeverde.client.lore.Discoveries.ITEM_KEY,
            com.eternalcraft.gripeverde.client.lore.Discoveries.ITEM_FEATHER,
            com.eternalcraft.gripeverde.client.lore.Discoveries.ITEM_SCRAP,
            com.eternalcraft.gripeverde.client.lore.Discoveries.ITEM_SEAL,
            com.eternalcraft.gripeverde.client.lore.Discoveries.ITEM_NOTE,
        };
        for (int i = 0; i < ids.length; i++) {
            boolean has = mem.collectibles.contains(ids[i]);
            int cx = xs[i];
            if (!has) {
                g.fill(cx, y + 6, cx + 8, y + 7, com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(0xFFB89A6E, 60));
                continue;
            }
            switch (i) {
                case 0: // llave
                    g.fill(cx + 1, y + 4, cx + 3, y + 6, 0xFFC8A83E);
                    g.fill(cx + 2, y + 6, cx + 3, y + 11, 0xFFC8A83E);
                    g.fill(cx + 3, y + 9, cx + 5, y + 10, 0xFFC8A83E);
                    break;
                case 1: // pluma
                    g.fill(cx + 1, y + 10, cx + 6, y + 5, 0xFF1F1408);
                    g.fill(cx + 2, y + 8, cx + 7, y + 3, 0xFF3D2A18);
                    break;
                case 2: // jiron con letras
                    g.fill(cx, y + 4, cx + 8, y + 10, 0xFFD8CAB0);
                    g.fill(cx + 1, y + 6, cx + 6, y + 7, 0xFF3D2A18);
                    break;
                case 3: // sello roto
                    g.fill(cx + 1, y + 4, cx + 7, y + 10, 0xFF7A1F18);
                    g.fill(cx + 3, y + 6, cx + 5, y + 8, 0xFFB85A48);
                    break;
                case 4: // nota
                    g.fill(cx, y + 3, cx + 8, y + 11, 0xFFE6D2A6);
                    g.fill(cx + 1, y + 5, cx + 7, y + 6, 0xFF3D2A18);
                    g.fill(cx + 1, y + 7, cx + 6, y + 8, 0xFF3D2A18);
                    break;
            }
        }
    }

    private void processKnockBack() {
        if (knockBackAtMs > 0 && !knockBackPlayed && System.currentTimeMillis() >= knockBackAtMs) {
            knockBackPlayed = true;
            knockBackAtMs = 0L;
            try {
                // Dos golpes de vuelta, al otro lado de la madera.
                com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                    com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_WOOD_CREAK.get(), 0.26f, 0.42f);
                com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                    com.eternalcraft.gripeverde.client.audio.ModSounds.UI_CLICK_HEAVY.get(), 0.20f, 0.50f);
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            try {
                // El cajon
                int x = drawerX(), y = drawerY(), w = 92, h = DRAWER_H;
                if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h + 16) {
                    boolean hasKey = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory
                        .hasCollectible(com.eternalcraft.gripeverde.client.lore.Discoveries.ITEM_KEY);
                    if (drawerOpen) {
                        drawerOpen = false;
                        com.eternalcraft.gripeverde.client.audio.UiSounds.back();
                        return true;
                    }
                    if (hasKey) {
                        drawerOpen = true;
                        boolean first = com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                            com.eternalcraft.gripeverde.client.lore.Discoveries.DRAWER_OPEN);
                        if (first) com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                            com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_WOOD_CREAK.get(), 0.22f, 0.80f);
                        if (com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.collectibleCount() >= 5) {
                            com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                                com.eternalcraft.gripeverde.client.lore.Discoveries.DRAWER_COMPLETE);
                        }
                    } else {
                        // Sin llave: un golpe sordo y una pista
                        com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                            com.eternalcraft.gripeverde.client.audio.ModSounds.UI_CLICK_HEAVY.get(), 0.18f, 0.60f);
                        com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                            com.eternalcraft.gripeverde.client.lore.Marginalia.N_KEY_HINT);
                    }
                    return true;
                }

            } catch (Throwable ignored) {}
        }
        boolean consumed = super.mouseClicked(mouseX, mouseY, button);
        // v0.16.0: tres golpes sobre la madera desnuda: algo responde.
        // Solo cuentan los golpes que no tocaron ningun tirador ni papel.
        if (button == 0 && !consumed) {
            try {
                if (mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= panelY && mouseY <= panelY + panelH) {
                    long now = System.currentTimeMillis();
                    if (knockCount > 0 && now - knockTimes[knockCount - 1] > 1400L) knockCount = 0;
                    if (knockCount < knockTimes.length) knockTimes[knockCount++] = now;
                    if (knockCount >= 3) {
                        knockCount = 0;
                        boolean first = com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                            com.eternalcraft.gripeverde.client.lore.Discoveries.KNOCK_BACK);
                        knockBackAtMs = now + 700L;
                        knockBackPlayed = false;
                        if (first) com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                            com.eternalcraft.gripeverde.client.lore.Marginalia.N_READ_YOU);
                    }
                }
            } catch (Throwable ignored) {}
        }
        return consumed;
    }
    // Divisor horizontal con fade a los costados color tinta - limpio
    private static void drawInkDivider(net.minecraft.client.gui.GuiGraphics g, int x1, int x2, int y) {
        int inkFull  = 0x66422810;
        int inkClear = 0x00422810;
        int mid = (x1 + x2) / 2;
        g.fillGradient(x1, y, mid, y + 1, inkClear, inkFull);
        g.fillGradient(mid, y, x2, y + 1, inkFull, inkClear);
    }

    // Esquineros victorianos en L color tinta sutil
    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    private void onBack() {
        this.onClose();
    }

    // ---- Aperturas de sub-pantallas ----

    private void openSkin() {
        this.minecraft.setScreen(new DiarySkinCustomizationScreen(this, this.minecraft.options));
    }
    private void openSound() {
        this.minecraft.setScreen(new DiarySoundOptionsScreen(this, this.minecraft.options));
    }
    private void openVideo() {
        this.minecraft.setScreen(new DiaryVideoSettingsScreen(this, this.minecraft.options));
    }
    private void openControls() {
        this.minecraft.setScreen(new DiaryControlsScreen(this, this.minecraft.options));
    }
    private void openLanguage() {
        this.minecraft.setScreen(new DiaryLanguageScreen(
                this,
                this.minecraft.options,
                this.minecraft.getLanguageManager()
        ));
    }
    private void openChat() {
        this.minecraft.setScreen(new DiaryChatOptionsScreen(this, this.minecraft.options));
    }
    private void openAccessibility() {
        this.minecraft.setScreen(new DiaryAccessibilityOptionsScreen(this, this.minecraft.options));
    }
        private void openOnline() {
        this.minecraft.setScreen(new DiaryOnlineOptionsScreen(this, this.minecraft.options));
    }
        private void openResourcePacks() {
        net.minecraft.server.packs.repository.PackRepository repo =
                this.minecraft.getResourcePackRepository();

        java.util.function.Consumer<net.minecraft.server.packs.repository.PackRepository> callback =
                (r) -> {
                    this.minecraft.options.updateResourcePacks(r);
                    this.minecraft.setScreen(this);
                };

        java.nio.file.Path dir = this.minecraft.getResourcePackDirectory();
        net.minecraft.network.chat.Component title =
                net.minecraft.network.chat.Component.translatable("resourcePack.title");

        this.minecraft.setScreen(
                new net.minecraft.client.gui.screens.packs.PackSelectionScreen(
                        repo, callback, dir, title
                )
        );
    }
        private void openCredits() {
        this.minecraft.setScreen(
            new DiaryCreditsScreen(this)
        );
    }
}
