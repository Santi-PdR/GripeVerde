package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.VictorianSkyline;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import com.eternalcraft.gripeverde.client.screens.DiaryAccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.ChatOptionsScreen;
import com.eternalcraft.gripeverde.client.screens.DiaryChatOptionsScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
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

    private final Screen parent;

    // Botones del grid
    private static final int BTN_W = 160;
    private static final int BTN_H = 22;
    private static final int GAP_X = 10;
    private static final int GAP_Y = 6;

    // Boton Volver
    private static final int BACK_W = 130;
    private static final int BACK_H = 22;

    // Interior del panel
    private static final int PAD_TOP = 16;
    private static final int SUBTITLE_GAP = 11;
    private static final int TITLE_H = 12;
    private static final int TITLE_GAP = 14;
    private static final int GRID_ROWS = 6;
    private static final int GAP_BEFORE_BACK = 16;
    private static final int PAD_BOTTOM = 16;
    private static final int PAD_SIDES = 24;

    // Overlay que atenua el fondo vivo (para dar peso al pergamino)
    private static final int SCENE_OVERLAY = 0x66000000; // negro 40%

    // Panel calculado
    private int panelX, panelY, panelW, panelH;

    public DiaryOptionsScreen(Screen parent) {
        super(Component.translatable("gripeverde.options.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int gridH = GRID_ROWS * BTN_H + (GRID_ROWS - 1) * GAP_Y;
        panelW = BTN_W * 2 + GAP_X + PAD_SIDES * 2;
        panelH = PAD_TOP + TITLE_H + SUBTITLE_GAP + TITLE_GAP + gridH + GAP_BEFORE_BACK + BACK_H + PAD_BOTTOM;

        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        int cx = this.width / 2;
        int gridTop = panelY + PAD_TOP + TITLE_H + SUBTITLE_GAP + TITLE_GAP;

        int col1X = cx - BTN_W - GAP_X / 2;
        int col2X = cx + GAP_X / 2;

        addBtn(col1X, gridTop + row(0), "gripeverde.options.button.skin", this::openSkin);
        addBtn(col2X, gridTop + row(0), "gripeverde.options.button.sound", this::openSound);
        addBtn(col1X, gridTop + row(1), "gripeverde.options.button.video", this::openVideo);
        addBtn(col2X, gridTop + row(1), "gripeverde.options.button.controls", this::openControls);
        addBtn(col1X, gridTop + row(2), "gripeverde.options.button.language", this::openLanguage);
        addBtn(col2X, gridTop + row(2), "gripeverde.options.button.chat", this::openChat);
        addBtn(col1X, gridTop + row(3), "gripeverde.options.button.resourcepacks", this::openResourcePacks);
        addBtn(col2X, gridTop + row(3), "gripeverde.options.button.accessibility", this::openAccessibility);
        addBtn(col1X, gridTop + row(4), "gripeverde.options.button.telemetry", this::openTelemetry);
        addBtn(col2X, gridTop + row(4), "gripeverde.options.button.credits", this::openCredits);
        addBtn(col1X, gridTop + row(5), "gripeverde.options.button.online", this::openOnline);

        int backX = cx - BACK_W / 2;
        int backY = panelY + panelH - PAD_BOTTOM - BACK_H;
        this.addRenderableWidget(new DiaryButton(backX, backY, BACK_W, BACK_H,
                Component.translatable("gripeverde.options.button.done"),
                DiaryButton.Icon.DOOR, DiaryButton.Variant.PRIMARY,
                this::onBack));
    }

    private static int row(int i) {
        return i * (BTN_H + GAP_Y);
    }

    private void addBtn(int x, int y, String translationKey, Runnable action) {
        this.addRenderableWidget(new DiaryButton(x, y, BTN_W, BTN_H,
                Component.translatable(translationKey), action));
    }

    @Override
    public void tick() {
        super.tick();
        // Mantener la atmosfera viva mientras esta pantalla esta abierta.
        float delta = 1.0f / 20.0f;
        AtmosphereRenderer.tick(delta);
        VictorianSkyline.tick(delta);
        LivingAtmosphere.tick(delta);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Fondo vivo: mismo pipeline del Title, sin flash/vignette (no compiten con el pergamino).
        AtmosphereRenderer.renderSky(g, this.width, this.height);
        VictorianSkyline.renderBackground(g, this.width, this.height);
        LivingAtmosphere.renderFogLow(g, this.width, this.height);
        VictorianSkyline.renderForeground(g, this.width, this.height);
        LivingAtmosphere.renderStreetLevel(g, this.width, this.height);
        LivingAtmosphere.renderAir(g, this.width, this.height);

        // Overlay oscuro para dar peso al pergamino
        g.fill(0, 0, this.width, this.height, SCENE_OVERLAY);

        // Panel pergamino
        PaperTexture.drawClean(g, panelX, panelY, panelW, panelH);

        // Titulo en tinta
        int titleY = panelY + PAD_TOP;
        {
            int titleW = this.font.width(this.title);
            int titleX = (this.width - titleW) / 2;
            g.drawString(this.font, this.title, titleX + 1, titleY + 1, 0x40201409, false);
            g.drawString(this.font, this.title, titleX, titleY, DiaryTheme.INK, false);

            // Subtitulo diegetico
            net.minecraft.network.chat.Component subtitle =
                    net.minecraft.network.chat.Component.translatable("gripeverde.options.subtitle");
            int subW = this.font.width(subtitle);
            int subX = (this.width - subW) / 2;
            int subY = titleY + TITLE_H + 2;
            g.drawString(this.font, subtitle, subX, subY, 0xFF6B4A2E, false);

            // Divisor horizontal bajo el subtitulo (con fade a los costados)
            int divY1 = subY + this.font.lineHeight + 3;
            drawInkDivider(g, panelX + 24, panelX + panelW - 24, divY1);

            // Divisor horizontal antes del boton Volver
            int backY = panelY + panelH - PAD_BOTTOM - BACK_H;
            int divY2 = backY - 8;
            drawInkDivider(g, panelX + 24, panelX + panelW - 24, divY2);

            // Esquineros victorianos
            drawCornerOrnaments(g, panelX, panelY, panelW, panelH);
        }

        // Widgets
        super.render(g, mouseX, mouseY, partialTick);
    }

    // Divisor horizontal con fade a los costados color tinta
    private static void drawInkDivider(net.minecraft.client.gui.GuiGraphics g, int x1, int x2, int y) {
        int inkFull  = 0x88422810;
        int inkClear = 0x00422810;
        int mid = (x1 + x2) / 2;
        g.fillGradient(x1, y, mid, y + 1, inkClear, inkFull);
        g.fillGradient(mid, y, x2, y + 1, inkFull, inkClear);
    }

    // Esquineros victorianos en L color tinta (8x8 px)
    private static void drawCornerOrnaments(net.minecraft.client.gui.GuiGraphics g,
                                            int x, int y, int w, int h) {
        int ink = 0xA0422810;
        int len = 8;
        int th = 1;
        int m = 4; // margen desde el borde

        // Esquina superior izquierda
        g.fill(x + m, y + m, x + m + len, y + m + th, ink);
        g.fill(x + m, y + m, x + m + th, y + m + len, ink);

        // Esquina superior derecha
        g.fill(x + w - m - len, y + m, x + w - m, y + m + th, ink);
        g.fill(x + w - m - th, y + m, x + w - m, y + m + len, ink);

        // Esquina inferior izquierda
        g.fill(x + m, y + h - m - th, x + m + len, y + h - m, ink);
        g.fill(x + m, y + h - m - len, x + m + th, y + h - m, ink);

        // Esquina inferior derecha
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
        this.minecraft.setScreen(new SkinCustomizationScreen(this, this.minecraft.options));
    }
    private void openSound() {
        this.minecraft.setScreen(new SoundOptionsScreen(this, this.minecraft.options));
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
        this.minecraft.setScreen(new ControlsScreen(this, this.minecraft.options));
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
        this.minecraft.setScreen(
            net.minecraft.client.gui.screens.OnlineOptionsScreen
                .createOnlineOptionsScreen(this.minecraft, this, this.minecraft.options)
        );
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
            new net.minecraft.client.gui.screens.CreditsAndAttributionScreen(this)
        );
    }
}