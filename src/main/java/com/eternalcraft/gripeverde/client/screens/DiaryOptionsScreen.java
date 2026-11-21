package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.DiaryTypography;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiarySlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import com.eternalcraft.gripeverde.client.screens.DiaryAccessibilityOptionsScreen;
import com.eternalcraft.gripeverde.client.screens.DiaryChatOptionsScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import com.eternalcraft.gripeverde.client.screens.DiarySkinCustomizationScreen;
import com.eternalcraft.gripeverde.client.screens.DiarySoundOptionsScreen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
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
    private static final int BASE_BTN_W = 178;
    private static final int BASE_BTN_H = 28;
    private static final int BASE_GAP_X = 14;
    private static final int BASE_GAP_Y = 8;

    // Boton Volver
    private static final int BASE_BACK_W = 172;
    private static final int BASE_BACK_H = 26;

    // Interior del panel
    private static final int BASE_PAD_TOP = 18;
    private static final int BASE_SUBTITLE_GAP = 11;
    private static final int TITLE_H = 12;
    private static final int BASE_TITLE_GAP = 14;
    private static final int GRID_ROWS = 6;
    private static final int BASE_GAP_BEFORE_BACK = 16;
    private static final int BASE_PAD_BOTTOM = 18;
    private static final int BASE_PAD_SIDES = 28;

    // Per-window layout values keep the cabinet inside small screens.
    private int btnW, btnH, gapX, gapY, backW, backH;
    private int padTop, subtitleGap, titleGap, gapBeforeBack, padBottom, padSides;
    private boolean compactLayout;

    // Overlay que atenua el fondo vivo (para dar peso al pergamino)

    // Panel calculado
    private int panelX, panelY, panelW, panelH;
    private int gridTop, col1X, col2X;

    // ── v0.16.0: la mesa de trabajo responde ──

    public DiaryOptionsScreen(Screen parent) {
        super(Component.translatable("gripeverde.options.title"));
        this.parent = parent;
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
        // Rotate flavor quote each time options screen opens, respecting language.
        String[] flavorPool = getFlavorPool();
        String currentLang = currentLanguageCode();
        if (!java.util.Objects.equals(flavorLang, currentLang)) {
            flavorLang = currentLang;
            flavorIndex = -1;
        }
        flavorIndex = (flavorIndex + 1) % flavorPool.length;
        currentFlavor = flavorPool[flavorIndex];

        compactLayout = this.height < 360 || this.width < 480;
        btnH = this.height < 220 ? 17 : (compactLayout ? 20 : BASE_BTN_H);
        gapY = this.height < 220 ? 2 : (compactLayout ? 3 : BASE_GAP_Y);
        gapX = compactLayout ? 8 : BASE_GAP_X;
        backH = this.height < 220 ? 18 : (compactLayout ? 20 : BASE_BACK_H);
        padTop = this.height < 220 ? 4 : (compactLayout ? 8 : BASE_PAD_TOP);
        subtitleGap = this.height < 220 ? 3 : (compactLayout ? 6 : BASE_SUBTITLE_GAP);
        titleGap = this.height < 220 ? 3 : (compactLayout ? 6 : BASE_TITLE_GAP);
        gapBeforeBack = this.height < 220 ? 4 : (compactLayout ? 5 : BASE_GAP_BEFORE_BACK);
        padBottom = this.height < 220 ? 4 : (compactLayout ? 8 : BASE_PAD_BOTTOM);
        padSides = compactLayout ? 10 : BASE_PAD_SIDES;

        int nominalPanelW = BASE_BTN_W * 2 + gapX + padSides * 2;
        panelW = Math.min(nominalPanelW, Math.max(180, this.width - 16));
        padSides = Math.min(padSides, Math.max(6, (panelW - 140 - gapX) / 2));
        btnW = Math.max(64, Math.min(BASE_BTN_W, (panelW - padSides * 2 - gapX) / 2));
        backW = Math.min(BASE_BACK_W, Math.max(96, panelW - 32));

        int gridH = GRID_ROWS * btnH + (GRID_ROWS - 1) * gapY;
        // v0.301.0: la cita del cronista se dibujaba fuera de la cuenta de la
        // cabecera y caia sobre la primera fila de botones. Ahora tiene su
        // renglon, como el subtitulo.
        int headerH = compactLayout
            ? TITLE_H + 2 + this.font.lineHeight + titleGap
            : TITLE_H + subtitleGap + this.font.lineHeight + titleGap;
        panelH = padTop + headerH + gridH + gapBeforeBack + backH + padBottom;

        // v0.16.0: la bisagra cuenta las veces que el gabinete se abre.
        try {
            int opens = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.bump("cabinetOpens");
            if (opens == 25) {
                com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                    com.eternalcraft.gripeverde.client.lore.Discoveries.HINGE);
                com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                    com.eternalcraft.gripeverde.client.lore.Marginalia.N_HINGE);
            }
            // v0.21.26: sin sonido de madera al abrir el gabinete.
        } catch (Throwable ignored) {}

        panelX = (this.width - panelW) / 2;
        panelY = Math.max(this.height < 220 ? 4 : 8, (this.height - panelH) / 2);

        int cx = this.width / 2;
        this.gridTop = panelY + padTop + headerH;

        this.col1X = cx - btnW - gapX / 2;
        this.col2X = cx + gapX / 2;

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
        int fovW = btnW * 2 + gapX;
        int fovH = compactLayout ? 18 : 20;
        int fovY = this.gridTop + row(5) + (btnH - fovH) / 2;
        double fovNorm = (mcOptions.fov().get() - 30.0) / 80.0;
        DiarySlider fovSlider = new DiarySlider(fovX, fovY, fovW, fovH, fovNorm,
                v -> {
                    int fov = (int) Math.round(30.0 + v * 80.0);
                    return DiaryChrome.tr("options.fov") + ": " + fov;
                },
                v -> mcOptions.fov().set((int) Math.round(30.0 + v * 80.0)));
        fovSlider.setTooltip(Tooltip.create(Component.translatable("options.fov")));
        this.addRenderableWidget(fovSlider);

        int backX = cx - backW / 2;
        int backY = panelY + panelH - padBottom - backH;
        this.addRenderableWidget(new DiaryButton(backX, backY, backW, backH,
                Component.translatable("gripeverde.options.button.done"),
                DiaryButton.Icon.DOOR, DiaryButton.Variant.PRIMARY,
                DiaryButton.SoundProfile.BACK,
                this::onBack));

        // v0.325.0: La casa se abre desde aqui, en plena partida. Antes habia
        // que salir al titulo para tocar el reloj o los volumenes de la casa.
        // Comparte el pie con Volver solo si sobra ancho; si no, no se agrega.
        try {
            boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
            int houseW = 84;
            int houseX = backX - 8 - houseW;
            if (houseX > panelX + 6) {
                DiaryButton house = new DiaryButton(houseX, backY, houseW, backH,
                    Component.literal(en ? "The house" : "La casa"),
                    DiaryButton.Icon.SCROLL, DiaryButton.Variant.SECONDARY,
                    DiaryButton.SoundProfile.OPTION_OPEN,
                    () -> this.minecraft.setScreen(new DiaryHouseScreen(this)));
                house.setTooltip(Tooltip.create(Component.literal(
                    en ? "what the house keeps on" : "lo que la casa tiene puesto")));
                this.addRenderableWidget(house);
            }
        } catch (Throwable ignored) {
        }
    }

    private int row(int i) {
        return i * (btnH + gapY);
    }

    // v0.30.0: elipsis fiel para que ninguna cita se salga del papel.
    private static String ellipsizeTo(net.minecraft.client.gui.Font font, String txt, int maxW) {
        return DiaryTypography.fitString(font, txt, maxW);
    }

    private static String currentLanguageCode() {
        try {
            String lang = net.minecraft.client.Minecraft.getInstance().getLanguageManager().getSelected();
            return lang == null ? "en_us" : lang;
        } catch (Throwable ignored) {
            return "en_us";
        }
    }

    private static boolean isEnglishSelected() {
        return com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
    }

    private static String[] getFlavorPool() {
        return isEnglishSelected() ? FLAVOR_POOL_EN : FLAVOR_POOL_ES;
    }

    private void addBtn(int x, int y, String translationKey, DiaryButton.Icon icon,
                        DiaryButton.SoundProfile soundProfile, Runnable action) {
        DiaryButton button = new DiaryButton(x, y, btnW, btnH,
                Component.translatable(translationKey), icon, DiaryButton.Variant.SECONDARY,
                soundProfile, action);
        button.setTooltip(Tooltip.create(Component.translatable(translationKey)));
        this.addRenderableWidget(button);
    }

    @Override
    public void tick() {
        super.tick();
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
    }

    // v0.194.0: la cabecera de la pantalla se recorta una vez por fuente y ancho,
    // no en cada cuadro (la traduccion tampoco se resuelve en cada cuadro).
    private net.minecraft.client.gui.Font headerFont;
    private int headerPanelW = -1;
    private String headerTitleSrc = "";
    private String headerTitleOut = "";
    private String headerSubtitleOut = "";

    private String headerTitleFitted() {
        String title = this.title.getString();
        if (this.headerFont != this.font || this.headerPanelW != this.panelW
                || !this.headerTitleSrc.equals(title)) {
            this.headerFont = this.font;
            this.headerPanelW = this.panelW;
            this.headerTitleSrc = title;
            int maxW = Math.max(40, panelW - 24);
            this.headerTitleOut = DiaryTypography.fitString(this.font, title, maxW);
            String sub = "- " + net.minecraft.network.chat.Component
                .translatable("gripeverde.options.subtitle").getString() + " -";
            this.headerSubtitleOut = DiaryTypography.fitString(this.font, sub, maxW);
        }
        return this.headerTitleOut;
    }

    private String headerSubtitleFitted() {
        headerTitleFitted();
        return this.headerSubtitleOut;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        // v0.383.0: el juego primero (fondo, rotulos y campos del vanilla);
        // despues el papel de la casa lo tapa todo y, al final, se repiten
        // las fichas sobre la hoja: asi no queda texto blanco de nadie.
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);

        // Panel pergamino
        DiaryChrome.drawPanelShadow(g, panelX, panelY, panelW, panelH);
        // v0.222.0: hoja rasgada, como el resto del cuaderno.
        PaperTexture.drawTornSheet(g, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.LEDGER);

        // Titulo en tinta - limpio, sin placa que corte
        int titleY = panelY + padTop;
        {
            Component shownTitle = Component.literal(headerTitleFitted());
            int titleW = DiaryTypography.width(this.font, shownTitle);
            int titleX = (this.width - titleW) / 2;
            DiaryTypography.draw(g, this.font, shownTitle, titleX, titleY, DiaryTheme.INK, false);

            // Subtitulo diegetico - sin sombra que divida
            String subtitle = headerSubtitleFitted();
            int subW = DiaryTypography.width(this.font, subtitle);
            int subX = (this.width - subW) / 2;
            int subY = titleY + TITLE_H + 2;
            DiaryTypography.draw(g, this.font, subtitle, subX, subY, DiaryChrome.INK_SUBTITLE, false);

            // Rotating flavor quote - tinta muy suave
            // v0.30.0: nunca fuera del papel (elipsis al ancho) y la linea
            // "fix" del huevo Holloway jamas pisa la primera fila de botones.
            int flavorBottom = subY + this.font.lineHeight + 1;
            // v0.339.0: la frase del cronista ("Esta noche el gas arde bajo") se
            // escribia aunque no hubiera aire y caia sobre el primer renglon de
            // botones. Ahora solo se escribe si cabe entera sobre la grilla.
            int flavorTop = subY + this.font.lineHeight + 1;
            boolean flavorFits = flavorTop + this.font.lineHeight <= this.gridTop - 6;
            if (currentFlavor != null && !compactLayout && flavorFits) {
                int maxTextW = panelW - 48;
                String flavorShown = DiaryTypography.fitString(this.font, currentFlavor, maxTextW);
                int flavorW = DiaryTypography.width(this.font, flavorShown);
                int flavorX = (this.width - flavorW) / 2;
                int flavorY = subY + this.font.lineHeight + 1;
                // v0.16.0: el cronista corrige su propia letra tras leer a Holloway.
                boolean corrected = false;
                try {
                    corrected = (currentFlavor.contains("tinta") || currentFlavor.contains("ink"))
                        && com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.isDiscovered(
                            com.eternalcraft.gripeverde.client.lore.Discoveries.CODE_HOLLOWAY);
                } catch (Throwable ignored) {}
                if (corrected) {
                    DiaryTypography.draw(g, this.font, flavorShown, flavorX, flavorY,
                        DiaryChrome.INK_FLAVOR, false);
                    g.fill(flavorX - 2, flavorY + 4, flavorX + flavorW + 2, flavorY + 5,
                        com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(0xFF241608, 120));
                    String fix = isEnglishSelected() ? "(no: the ink will never dry)" : "(no: la tinta ya no seca)";
                    String fixShown = DiaryTypography.fitString(this.font, fix, maxTextW);
                    int fixW = DiaryTypography.width(this.font, fixShown);
                    // Tope duro: siempre sobre la grilla, nunca debajo.
                    int fixY = flavorY + this.font.lineHeight + 3;
                    // v0.339.0: si la correccion no cabe bajo la frase, se calla.
                    if (fixY + this.font.lineHeight <= this.gridTop - 6) {
                        DiaryTypography.draw(g, this.font, fixShown, this.width / 2 - fixW / 2, fixY, DiaryChrome.INK_FLAVOR, false);
                        flavorBottom = fixY + this.font.lineHeight;
                    } else {
                        flavorBottom = flavorY + this.font.lineHeight;
                    }
                } else {
                    DiaryTypography.draw(g, this.font, flavorShown, flavorX, flavorY, DiaryChrome.INK_FLAVOR, false);
                    flavorBottom = flavorY + this.font.lineHeight;
                }
            }

            // Unico divisor horizontal DEBAJO del flavor (y del fix si existe),
            // jamas por encima de la grilla de botones.
            int divY1 = Math.min(flavorBottom + 4, this.gridTop - 2);
            DiaryChrome.drawInkDivider(g, panelX + 24, divY1, panelW - 48);

            // Divisor horizontal antes del boton Volver
            int backY = panelY + panelH - padBottom - backH;
            int divY2 = backY - 10;
            DiaryChrome.drawInkDivider(g, panelX + 24, divY2, panelW - 48);

            // Esquineros victorianos sutiles
            DiaryChrome.drawCornerOrnaments(g, panelX, panelY, panelW, panelH);
            DiaryChrome.drawRegistryTag(g, panelX, panelY, panelW, panelH, "REG. I");
        }

        // Widgets
        
        // v0.16.0: el cajon de la mesa de trabajo
        try {

            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, panelY - 14);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {}
        // v0.383.0: las fichas y los campos, encima de la hoja.
        com.eternalcraft.gripeverde.client.render.DiaryChrome.renderWidgets(
            this, g, mouseX, mouseY, partialTick);
    }




    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
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
    private static void drawCornerOrnaments(net.minecraft.client.gui.GuiGraphics g,
                                            int x, int y, int w, int h) {
        int ink = 0x70422810;
        int len = 10;
        int th = 1;
        int m = 6;
        g.fill(x + m, y + m, x + m + len, y + m + th, ink);
        g.fill(x + m, y + m, x + m + th, y + m + len, ink);
        g.fill(x + w - m - len, y + m, x + w - m, y + m + th, ink);
        g.fill(x + w - m - th, y + m, x + w - m, y + m + len, ink);
        g.fill(x + m, y + h - m - th, x + m + len, y + h - m, ink);
        g.fill(x + m, y + h - m - len, x + m + th, y + h - m, ink);
        g.fill(x + w - m - len, y + h - m - th, x + w - m, y + h - m, ink);
        g.fill(x + w - m - th, y + h - m - len, x + w - m, y + h - m, ink);
    }

    @Override
    public void onClose() {
        // v0.264.0: salir de una hoja suena a puerta, como el resto de la casa.
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.back();
        } catch (Throwable ignored) {
        }
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
        // Intento abrir EmbeddiumVideoOptionsScreen via reflection.
        // Si Embeddium no esta o algo falla, fallback a VideoSettingsScreen vanilla.
        try {
            Class<?> pagesCls = Class.forName("me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages");
            Class<?> screenCls = Class.forName("org.embeddedt.embeddium.gui.EmbeddiumVideoOptionsScreen");

            java.util.List<Object> pages = new java.util.ArrayList<>();
            pages.add(pagesCls.getMethod("general").invoke(null));
            pages.add(pagesCls.getMethod("quality").invoke(null));
            pages.add(pagesCls.getMethod("performance").invoke(null));
            pages.add(pagesCls.getMethod("advanced").invoke(null));

            java.lang.reflect.Constructor<?> ctor = screenCls.getConstructor(
                    net.minecraft.client.gui.screens.Screen.class, java.util.List.class);
            net.minecraft.client.gui.screens.Screen embScreen =
                    (net.minecraft.client.gui.screens.Screen) ctor.newInstance(this, pages);
            this.minecraft.setScreen(embScreen);
        } catch (Throwable t) {
            // Fallback: vanilla
            this.minecraft.setScreen(new VideoSettingsScreen(this, this.minecraft.options));
        }
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
                new DiaryPackSelectionScreen(
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
