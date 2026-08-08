package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.VictorianSkyline;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiarySlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import com.eternalcraft.gripeverde.client.screens.DiaryAccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.ChatOptionsScreen;
import com.eternalcraft.gripeverde.client.screens.DiaryChatOptionsScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import com.eternalcraft.gripeverde.client.screens.DiarySkinCustomizationScreen;
import com.eternalcraft.gripeverde.client.screens.DiarySoundOptionsScreen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
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
        panelW = BTN_W * 2 + GAP_X + PAD_SIDES * 2;
        panelH = PAD_TOP + TITLE_H + SUBTITLE_GAP + TITLE_GAP + gridH + GAP_BEFORE_BACK + BACK_H + PAD_BOTTOM + DRAWER_H;

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
        this.gridTop = panelY + PAD_TOP + TITLE_H + SUBTITLE_GAP + TITLE_GAP;

        this.col1X = cx - BTN_W - GAP_X / 2;
        this.col2X = cx + GAP_X / 2;

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

    private static boolean isEnglishSelected() {
        String lang = currentLanguageCode();
        return lang.startsWith("en_") || lang.equals("en_us");
    }

    private static String[] getFlavorPool() {
        return isEnglishSelected() ? FLAVOR_POOL_EN : FLAVOR_POOL_ES;
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

        // Panel pergamino
        DiaryChrome.drawPanelShadow(g, panelX, panelY, panelW, panelH);
        PaperTexture.drawClean(g, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.LEDGER);

        // Titulo en tinta - limpio, sin placa que corte
        int titleY = panelY + PAD_TOP;
        {
            int titleW = this.font.width(this.title);
            int titleX = (this.width - titleW) / 2;
            g.drawString(this.font, this.title, titleX, titleY, DiaryTheme.INK, false);

            // Subtitulo diegetico - sin sombra que divida
            String subtitle = "- " + net.minecraft.network.chat.Component.translatable("gripeverde.options.subtitle").getString() + " -";
            int subW = this.font.width(subtitle);
            int subX = (this.width - subW) / 2;
            int subY = titleY + TITLE_H + 2;
            g.drawString(this.font, subtitle, subX, subY, DiaryChrome.INK_SUBTITLE, false);

            // Rotating flavor quote - tinta muy suave
            if (currentFlavor != null) {
                int flavorW = this.font.width(currentFlavor);
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
                    g.drawString(this.font, currentFlavor, flavorX, flavorY,
                        com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(DiaryChrome.INK_FLAVOR, 110), false);
                    g.fill(flavorX - 2, flavorY + 4, flavorX + flavorW + 2, flavorY + 5,
                        com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(0xFF241608, 120));
                    String fix = isEnglishSelected() ? "(no: the ink will never dry)" : "(no: la tinta ya no seca)";
                    int fixW = this.font.width(fix);
                    g.drawString(this.font, fix, this.width / 2 - fixW / 2, flavorY + 9, DiaryChrome.INK_FLAVOR, false);
                } else {
                    g.drawString(this.font, currentFlavor, flavorX, flavorY, DiaryChrome.INK_FLAVOR, false);
                }
            }

            // Unico divisor horizontal DEBAJO del flavor (no entre titulo y subtitulo)
            int divY1;
            if (currentFlavor != null) {
                int flavorY = subY + this.font.lineHeight + 1;
                divY1 = flavorY + this.font.lineHeight + 4;
            } else {
                divY1 = subY + this.font.lineHeight + 4;
            }
            DiaryChrome.drawInkDivider(g, panelX + 24, divY1, panelW - 48);

            // Divisor horizontal antes del boton Volver
            int backY = panelY + panelH - PAD_BOTTOM - DRAWER_H - BACK_H;
            int divY2 = backY - 10;
            DiaryChrome.drawInkDivider(g, panelX + 24, divY2, panelW - 48);

            drawRowBands(g, this.gridTop, this.col1X, this.col2X);

            // Esquineros victorianos sutiles
            DiaryChrome.drawCornerOrnaments(g, panelX, panelY, panelW, panelH);
        }

        // Widgets
        super.render(g, mouseX, mouseY, partialTick);

        // v0.16.0: el cajon de la mesa de trabajo
        try {
            drawDrawer(g, mouseX, mouseY);
            processKnockBack();
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, panelY - 14);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {}
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

    private void drawRowBands(GuiGraphics g, int gridTop, int col1X, int col2X) {
        int rowW = BTN_W * 2 + GAP_X;
        int x = col1X - 6;
        int w = rowW + 12;
        for (int i = 0; i < GRID_ROWS; i++) {
            int y = gridTop + row(i) - 2;
            // Banda muy sutil, casi imperceptible, solo para agrupar visualmente
            int alpha = (i % 2 == 0) ? 0x0A000000 : 0x05000000;
            g.fill(x, y, x + w, y + BTN_H + 4, alpha);
        }
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
        private void openTelemetry() {
        this.minecraft.setScreen(
            new net.minecraft.client.gui.screens.telemetry.TelemetryInfoScreen(this, this.minecraft.options)
        );
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
