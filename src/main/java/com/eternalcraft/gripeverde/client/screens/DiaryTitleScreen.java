package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.GripeVerdeMod;
import com.eternalcraft.gripeverde.client.audio.MenuAmbience;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.render.LogoRenderer;
import com.eternalcraft.gripeverde.client.render.PocketWatchRenderer;
import com.eternalcraft.gripeverde.client.render.PosterRenderer;
import com.eternalcraft.gripeverde.client.render.TextureAssets;
import com.eternalcraft.gripeverde.client.render.VictorianSkyline;
import com.eternalcraft.gripeverde.client.theme.Anchor;
import com.eternalcraft.gripeverde.client.theme.LayoutRegion;
import com.eternalcraft.gripeverde.client.theme.Spacing;
import com.eternalcraft.gripeverde.client.widgets.GreenFluButton;
import com.eternalcraft.gripeverde.client.widgets.SlimButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ModListScreen;
import org.lwjgl.glfw.GLFW;

public class DiaryTitleScreen extends Screen {

    private long ticks = 0;

    private static final int BTN_W = 300;
    private static final int BTN_H = 40;

    private static final int BTN_SLIM_W = 130;
    private static final int BTN_SLIM_H = 32;

    private static final int POSTER_W = 180;
    private static final int POSTER_H_MAX = 260;

    private static final int WATCH_RADIUS = 42;

    private static final int POSTER_R_W = 180;
    private static final int POSTER_R_H_MAX = 180;

    // Umbral: por debajo de esta altura efectiva, ocultamos TODO lo decorativo
    private static final int MINIMAL_MODE_THRESHOLD = 290;

    public DiaryTitleScreen() {
        super(Component.translatable("gripeverde.title.window"));
    }

    private boolean isMinimalMode() {
        return this.height < MINIMAL_MODE_THRESHOLD;
    }

    @Override
    protected void init() {
        this.ticks = 0;
        MenuAmbience.start();
        MenuMusic.start();

        LayoutRegion leftCol = leftColumn();

        int btnX = leftCol.x;
        int totalBtnH = BTN_H * 2 + Spacing.GAP_MEDIUM;
        int btnY = leftCol.anchorY(Anchor.BOT_LEFT, totalBtnH)
                   - Spacing.GAP_LARGE;

        this.addRenderableWidget(new GreenFluButton(
            btnX, btnY, BTN_W, BTN_H,
            Component.translatable("gripeverde.title.button.multiplayer"),
            Component.translatable("gripeverde.title.subtitle.multiplayer"),
            TextureAssets.ICON_BTN_MP,
            this::onMultiplayer
        ));

        this.addRenderableWidget(new GreenFluButton(
            btnX, btnY + BTN_H + Spacing.GAP_MEDIUM, BTN_W, BTN_H,
            Component.translatable("gripeverde.title.button.mods"),
            Component.translatable("gripeverde.title.subtitle.mods"),
            TextureAssets.ICON_BTN_MODS,
            this::onMods
        ));

        LayoutRegion rightCol = rightColumn();
        int quitX = rightCol.x + rightCol.w - BTN_SLIM_W;

        if (isMinimalMode()) {
            // Modo minimal: Adjustments arriba de Quit (apilados verticalmente)
            int quitY = rightCol.anchorY(Anchor.BOT_LEFT, BTN_SLIM_H)
                        - Spacing.GAP_LARGE;
            int optionsY = quitY - BTN_SLIM_H - Spacing.GAP_SMALL;

            this.addRenderableWidget(new SlimButton(quitX, optionsY, BTN_SLIM_W, BTN_SLIM_H,
                Component.translatable("gripeverde.title.button.options"),
                this::onOptions));

            this.addRenderableWidget(new SlimButton(quitX, quitY, BTN_SLIM_W, BTN_SLIM_H,
                Component.translatable("gripeverde.title.button.quit"),
                () -> this.minecraft.stop()));
        } else {
            // Modo normal: Adjustments al lado de Quit (horizontales)
            int rightBtnY = rightCol.anchorY(Anchor.BOT_LEFT, BTN_SLIM_H)
                            - Spacing.GAP_LARGE;
            int optionsX = quitX - BTN_SLIM_W - Spacing.GAP_SMALL;

            this.addRenderableWidget(new SlimButton(optionsX, rightBtnY, BTN_SLIM_W, BTN_SLIM_H,
                Component.translatable("gripeverde.title.button.options"),
                this::onOptions));

            this.addRenderableWidget(new SlimButton(quitX, rightBtnY, BTN_SLIM_W, BTN_SLIM_H,
                Component.translatable("gripeverde.title.button.quit"),
                () -> this.minecraft.stop()));
        }

        SlimButton changelogBtn = new SlimButton(
            this.width - 28, 8, 20, 20,
            Component.literal("?"),
            () -> this.minecraft.setScreen(new ChangelogScreen(this)));
        changelogBtn.setTooltip(Tooltip.create(
            Component.translatable("gripeverde.title.button.changelog")));
        this.addRenderableWidget(changelogBtn);

        // --- Audio control buttons (bottom-left, discrete) ---
        int acY = this.height - 28;
        int acX = 8;
        int acW = 36;
        int acH = 20;

        this.addRenderableWidget(new SlimButton(acX, acY, acW, acH,
            Component.literal(MenuAmbience.isMuted() ? "AMB" : "AMB"),
            () -> {
                MenuAmbience.toggleMute();
                this.rebuildWidgets();
            }) {
            @Override
            protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(g, mouseX, mouseY, partialTick);
                if (MenuAmbience.isMuted()) {
                    // Draw strikethrough line to indicate muted
                    int cx = this.getX();
                    int cy = this.getY();
                    int cw = this.width;
                    int ch = this.height;
                    g.fill(cx + 4, cy + ch / 2, cx + cw - 4, cy + ch / 2 + 1, 0xFFCC4444);
                }
            }
        });

        this.addRenderableWidget(new SlimButton(acX + acW + 4, acY, acW, acH,
            Component.literal(">>"),
            () -> MenuMusic.skipToNext()));
    }

    private LayoutRegion leftColumn() {
        LayoutRegion screen = LayoutRegion.screen(this.width, this.height);
        return screen.left(POSTER_W + Spacing.SCREEN_MARGIN * 2)
                     .inset(Spacing.SCREEN_MARGIN);
    }

    private LayoutRegion rightColumn() {
        LayoutRegion screen = LayoutRegion.screen(this.width, this.height);
        int colW = Math.max(WATCH_RADIUS * 2, POSTER_R_W) + Spacing.SCREEN_MARGIN * 2;
        return screen.right(colW).inset(Spacing.SCREEN_MARGIN);
    }

    @Override
    public void tick() {
        this.ticks++;
        float delta = 1f / 20f;
        AtmosphereRenderer.tick(delta);
        VictorianSkyline.tick(delta);
        LivingAtmosphere.tick(delta);
        MenuMusic.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        if (ctrl && keyCode == GLFW.GLFW_KEY_S) { onSingleplayer(); return true; }
        if (ctrl && keyCode == GLFW.GLFW_KEY_T) {
            this.minecraft.setScreen(new DebugCanvasScreen(this));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        MenuAmbience.tick();
        MenuAmbience.setPocketWatchVisible(!isMinimalMode());

        AtmosphereRenderer.renderSky(g, this.width, this.height);
        VictorianSkyline.renderBackground(g, this.width, this.height);
        LivingAtmosphere.renderFogLow(g, this.width, this.height);
        VictorianSkyline.renderForeground(g, this.width, this.height);
        LivingAtmosphere.renderStreetLevel(g, this.width, this.height);
        LivingAtmosphere.renderAir(g, this.width, this.height);
        LivingAtmosphere.renderFlash(g, this.width, this.height);
        LivingAtmosphere.renderVignette(g, this.width, this.height);

        if (!isMinimalMode()) {
            drawPosterLeft(g);
            drawPosterRight(g);
            drawPocketWatch(g);
        }

        drawLogo(g);
        drawSignature(g);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void removed() {
        MenuAmbience.stop();
        // MenuMusic.stop() omitido intencionalmente: musica sobrevive a Options/subpantallas
        super.removed();
    }

    private void drawPosterLeft(GuiGraphics g) {
        LayoutRegion leftCol = leftColumn();
        int totalBtnH = BTN_H * 2 + Spacing.GAP_MEDIUM;
        int maxPosterH = leftCol.h - totalBtnH - Spacing.GAP_LARGE * 2;

        if (maxPosterH < 140) return;

        int posterH = Math.min(POSTER_H_MAX, maxPosterH);
        PosterRenderer.drawQuarantineNotice(g, leftCol.x, leftCol.y, POSTER_W, posterH);
    }

    private void drawPocketWatch(GuiGraphics g) {
        LayoutRegion rightCol = rightColumn();
        int diameter = WATCH_RADIUS * 2 + 30; // + espacio para cadena y label
        int reserved = BTN_SLIM_H + Spacing.GAP_LARGE * 2;

        if (rightCol.h - reserved < diameter) return;

        int cx = rightCol.x + rightCol.w - WATCH_RADIUS - Spacing.GAP_MEDIUM;
        int cy = rightCol.y + WATCH_RADIUS + Spacing.GAP_LARGE;
        PocketWatchRenderer.draw(g, cx, cy, 0, WATCH_RADIUS, GripeVerdeMod.VERSION);
    }

    private void drawPosterRight(GuiGraphics g) {
        LayoutRegion rightCol = rightColumn();
        int topReserved = (WATCH_RADIUS * 2) + 30 + Spacing.GAP_LARGE;
        int botReserved = BTN_SLIM_H + Spacing.GAP_LARGE * 2;
        int available = rightCol.h - topReserved - botReserved;

        if (available < 120) return;

        int posterH = Math.min(POSTER_R_H_MAX, available);
        int posterX = rightCol.x + rightCol.w - POSTER_R_W;
        int posterY = rightCol.y + topReserved;

        PosterRenderer.drawRotatingNoticeRight(g, posterX, posterY, POSTER_R_W, posterH);
    }

    private void drawLogo(GuiGraphics g) {
        LogoRenderer.draw(g, this.width / 2, 30);
    }

    private void drawSignature(GuiGraphics g) {
        String signature = "Signed: S. Pereira - MDCCCXCVII";
        int sw = this.font.width(signature);
        g.drawString(this.font, signature,
            this.width - sw - 6, this.height - 12,
            DiaryTheme.SEPIA_LIGHT, false);
    }

    private void onSingleplayer() { this.minecraft.setScreen(new SelectWorldScreen(this)); }
    private void onMultiplayer()  { this.minecraft.setScreen(new JoinMultiplayerScreen(this)); }
    private void onMods()         { this.minecraft.setScreen(new ModListScreen(this)); }
    private void onOptions() { this.minecraft.setScreen(new DiaryOptionsScreen(this)); }

    @Override public boolean isPauseScreen()    { return false; }
    @Override public boolean shouldCloseOnEsc() { return false; }
}