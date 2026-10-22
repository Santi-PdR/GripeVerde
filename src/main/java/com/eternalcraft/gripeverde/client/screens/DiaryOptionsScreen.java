package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiarySlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
    private static final int PAD_SIDES = 28;

    // Overlay que atenua el fondo vivo (para dar peso al pergamino)

    // Panel calculado
    private int panelX, panelY, panelW, panelH;
    private int gridTop, col1X, col2X;
    private int buttonW, buttonH, gapX, gapY, backW, backH;
    private int padTop, subtitleGap, titleH, titleGap, gapBeforeBack, padBottom, padSides;

    // ── v0.16.0: la mesa de trabajo responde ──

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

        configureResponsiveLayout();
        int gridH = GRID_ROWS * buttonH + (GRID_ROWS - 1) * gapY;
        panelH = padTop + titleH + subtitleGap + titleGap + gridH
            + gapBeforeBack + backH + padBottom;

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
        panelY = (this.height - panelH) / 2;

        int cx = this.width / 2;
        this.gridTop = panelY + padTop + titleH + subtitleGap + titleGap;

        this.col1X = cx - buttonW - gapX / 2;
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
        int fovW = buttonW * 2 + gapX;
        int fovY = this.gridTop + row(5) + (buttonH - 20) / 2;
        double fovNorm = (mcOptions.fov().get() - 30.0) / 80.0;
        this.addRenderableWidget(new DiarySlider(fovX, fovY, fovW, 20, fovNorm,
                v -> {
                    int fov = (int) Math.round(30.0 + v * 80.0);
                    return Component.translatable("options.fov").getString() + ": " + fov;
                },
                v -> mcOptions.fov().set((int) Math.round(30.0 + v * 80.0))));

        int backX = cx - backW / 2;
        int backY = panelY + panelH - padBottom - backH;
        this.addRenderableWidget(new DiaryButton(backX, backY, backW, backH,
                Component.translatable("gripeverde.options.button.done"),
                DiaryButton.Icon.DOOR, DiaryButton.Variant.PRIMARY,
                DiaryButton.SoundProfile.BACK,
                this::onBack));
    }

    private void configureResponsiveLayout() {
        boolean compact = this.height < 350;
        boolean veryCompact = this.height < 300;
        buttonH = veryCompact ? 21 : compact ? 24 : BTN_H;
        gapY = veryCompact ? 3 : compact ? 6 : GAP_Y;
        gapX = this.width < 400 ? 8 : GAP_X;
        padTop = veryCompact ? 8 : compact ? 12 : PAD_TOP;
        titleH = TITLE_H;
        subtitleGap = veryCompact ? 7 : compact ? 8 : SUBTITLE_GAP;
        // Keep the flavor line and its divider above the first button row.
        titleGap = compact ? 18 : TITLE_GAP;
        gapBeforeBack = compact ? 12 : GAP_BEFORE_BACK;
        backH = veryCompact ? 20 : compact ? 24 : BACK_H;
        padBottom = veryCompact ? 8 : compact ? 10 : PAD_BOTTOM;
        padSides = Math.min(PAD_SIDES, Math.max(12, (this.width - 24) / 10));
        backW = Math.min(BACK_W, Math.max(96, this.width - 24));

        int maximumPanelW = Math.max(1, this.width - 24);
        int preferredPanelW = BTN_W * 2 + gapX + PAD_SIDES * 2;
        panelW = Math.min(preferredPanelW, maximumPanelW);
        int usableButtonSpace = Math.max(2, panelW - gapX - padSides * 2);
        buttonW = Math.max(1, Math.min(BTN_W, usableButtonSpace / 2));
        panelW = buttonW * 2 + gapX + padSides * 2;
    }

    private int row(int i) {
        return i * (buttonH + gapY);
    }

    // v0.30.0: elipsis fiel para que ninguna cita se salga del papel.
    private static String ellipsizeTo(net.minecraft.client.gui.Font font, String txt, int maxW) {
        if (font.width(txt) <= maxW) return txt;
        String ell = "...";
        int room = Math.max(0, maxW - font.width(ell));
        return font.plainSubstrByWidth(txt, room) + ell;
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
        String lang = currentLanguageCode();
        return lang.startsWith("en_") || lang.equals("en_us");
    }

    private static String[] getFlavorPool() {
        return isEnglishSelected() ? FLAVOR_POOL_EN : FLAVOR_POOL_ES;
    }

    private void addBtn(int x, int y, String translationKey, DiaryButton.Icon icon,
                        DiaryButton.SoundProfile soundProfile, Runnable action) {
        this.addRenderableWidget(new DiaryButton(x, y, buttonW, buttonH,
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

        // Panel pergamino
        DiaryChrome.drawPanelShadow(g, panelX, panelY, panelW, panelH);
        PaperTexture.drawClean(g, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.LEDGER);

        // Titulo en tinta - limpio, sin placa que corte
        int titleY = panelY + padTop;
        {
            com.eternalcraft.gripeverde.client.render.DiaryTypography.drawCentered(
                g, this.font, this.title, this.width / 2, titleY, DiaryTheme.INK);

            // Subtitulo diegetico - sin sombra que divida
            String subtitle = "- " + net.minecraft.network.chat.Component.translatable("gripeverde.options.subtitle").getString() + " -";
            int subW = this.font.width(subtitle);
            int subX = (this.width - subW) / 2;
            int subY = titleY + titleH + 2;
            g.drawString(this.font, subtitle, subX, subY, DiaryChrome.INK_SUBTITLE, false);

            // Rotating flavor quote - tinta muy suave
            // v0.30.0: nunca fuera del papel (elipsis al ancho) y la linea
            // "fix" del huevo Holloway jamas pisa la primera fila de botones.
            int flavorBottom = subY + this.font.lineHeight + 1;
            if (currentFlavor != null) {
                int maxTextW = panelW - 48;
                String flavorShown = ellipsizeTo(this.font, currentFlavor, maxTextW);
                int flavorW = this.font.width(flavorShown);
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
                    g.drawString(this.font, flavorShown, flavorX, flavorY,
                        com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(DiaryChrome.INK_FLAVOR, 110), false);
                    g.fill(flavorX - 2, flavorY + 4, flavorX + flavorW + 2, flavorY + 5,
                        com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(0xFF241608, 120));
                    String fix = isEnglishSelected() ? "(no: the ink will never dry)" : "(no: la tinta ya no seca)";
                    String fixShown = ellipsizeTo(this.font, fix, maxTextW);
                    int fixW = this.font.width(fixShown);
                    // Tope duro: siempre sobre la grilla, nunca debajo.
                    int fixY = Math.min(flavorY + 9, this.gridTop - 11);
                    g.drawString(this.font, fixShown, this.width / 2 - fixW / 2, fixY, DiaryChrome.INK_FLAVOR, false);
                    flavorBottom = fixY + this.font.lineHeight;
                } else {
                    g.drawString(this.font, flavorShown, flavorX, flavorY, DiaryChrome.INK_FLAVOR, false);
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
        super.render(g, mouseX, mouseY, partialTick);

        // v0.16.0: el cajon de la mesa de trabajo
        try {

            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, panelY - 14);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {}
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
