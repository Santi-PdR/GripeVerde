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
import com.eternalcraft.gripeverde.client.screens.DiaryMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ModListScreen;
import org.lwjgl.glfw.GLFW;

public class DiaryTitleScreen extends Screen {

    private long ticks = 0;
    private static boolean openCountIncremented = false;
    private SlimButton skipMusicButton;
    private static boolean cinematicMode = false;
    private static long cinematicHintUntilMs = 0L;

    // ── v0.16.0: la memoria de la vigilia ───────────────────────────────────
    private static boolean memorySessionBegun = false;
    private long sessionStartMs = System.currentTimeMillis();
    private long lastInteractionMs = System.currentTimeMillis();
    private boolean dustThisSession = false;
    private boolean dawnActive = false;
    private static long greetingShownUntilMs = 0L;
    private static String greetingText = null;
    private long wantedPressStartMs = 0L;
    private boolean wantedPressing = false;

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

    private static final int PATINA_COUNT = 26;
    private static final int[] PATINA_X = new int[PATINA_COUNT];
    private static final int[] PATINA_Y = new int[PATINA_COUNT];
    private static final int[] PATINA_KIND = new int[PATINA_COUNT];
    private static int patinaW = -1;
    private static int patinaH = -1;

    public DiaryTitleScreen() {
        super(Component.translatable("gripeverde.title.window"));
    }

    private boolean isCompactLayout() {
        return this.width < 620;
    }

    private boolean isMinimalMode() {
        return this.height < MINIMAL_MODE_THRESHOLD || isCompactLayout();
    }

    private static boolean reducedMotion() {
        try {
            return com.eternalcraft.gripeverde.config.ClientConfig.REDUCED_MOTION.get();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    @Override
    protected void init() {
        this.ticks = 0;
        // Incrementar contador de aperturas (solo una vez por sesion)
        try {
            if (!openCountIncremented) {
                int cur = com.eternalcraft.gripeverde.config.ClientConfig.OPEN_COUNT.get();
                com.eternalcraft.gripeverde.config.ClientConfig.OPEN_COUNT.set(cur + 1);
                com.eternalcraft.gripeverde.config.ClientConfig.OPEN_COUNT.save();
                if (cur + 1 >= 50) {
                    // v0.21.3: el archivo que se adelgaza (solo texto, sin mecanica nueva)
                    com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                        com.eternalcraft.gripeverde.client.lore.Discoveries.ARCHIVE_THINNING);
                }
                if (cur + 1 >= 100) {
                    // v0.21.5: marginalia de las cien aperturas
                    com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                        com.eternalcraft.gripeverde.client.lore.Marginalia.N_OPEN_COUNT);
                }
                openCountIncremented = true;
            }
        } catch (Throwable ignored) {}

        // ── v0.16.0: la sesion entra en la memoria del gabinete ──
        // (solo una vez por sesion; init() corre de nuevo en cada resize)
        try {
            if (!memorySessionBegun) {
                memorySessionBegun = true;
                com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.beginSession();
                int kind = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.sessionGreetingKind();
                long absence = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.sessionAbsenceDays();
                if (kind == 1) {
                    greetingText = Component.translatable("gripeverde.title.greeting.first").getString();
                    com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                        com.eternalcraft.gripeverde.client.lore.Marginalia.N_FIRST_NIGHT);
                } else if (kind == 2 && absence >= 7L) {
                    greetingText = Component.translatable("gripeverde.title.greeting.return").getString();
                    dustThisSession = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.consumeDust();
                    com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                        com.eternalcraft.gripeverde.client.lore.Discoveries.DUST);
                    com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                        com.eternalcraft.gripeverde.client.lore.Marginalia.N_DUST);
                } else if (kind == 2) {
                    greetingText = Component.translatable("gripeverde.title.greeting.remembers").getString();
                } else if (kind == 3) {
                    greetingText = Component.translatable("gripeverde.title.greeting.vigil").getString();
                }
                if (greetingText != null) greetingShownUntilMs = System.currentTimeMillis() + 12_000L;
                if (com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.consumeAnniversary()) {
                    com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                        com.eternalcraft.gripeverde.client.lore.Discoveries.ANNIVERSARY);
                    com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                        com.eternalcraft.gripeverde.client.lore.Marginalia.N_ANNIVERSARY);
                }
            }
        } catch (Throwable ignored) {}

        MenuAmbience.start();
        MenuMusic.start();

        boolean compactLayout = isCompactLayout();
        LayoutRegion leftCol = leftColumn();

        int btnX = compactLayout
            ? Math.max(8, Math.min(20, this.width / 16))
            : leftCol.x;
        int mainButtonW = compactLayout
            ? Math.max(1, Math.min(BTN_W, this.width - btnX * 2))
            : BTN_W;
        int totalBtnH = BTN_H * 2 + Spacing.GAP_MEDIUM;
        LayoutRegion buttonColumn = compactLayout
            ? LayoutRegion.screen(this.width, this.height).inset(btnX)
            : leftCol;
        int btnY = buttonColumn.anchorY(Anchor.BOT_LEFT, totalBtnH)
                   - Spacing.GAP_LARGE;

        this.addRenderableWidget(new GreenFluButton(
            btnX, btnY, mainButtonW, BTN_H,
            Component.translatable("gripeverde.title.button.multiplayer"),
            Component.literal(pickRotatingSubtitle(true)),
            TextureAssets.ICON_BTN_MP,
            this::onMultiplayer
        ));

        this.addRenderableWidget(new GreenFluButton(
            btnX, btnY + BTN_H + Spacing.GAP_MEDIUM, mainButtonW, BTN_H,
            Component.translatable("gripeverde.title.button.mods"),
            Component.literal(pickRotatingSubtitle(false)),
            TextureAssets.ICON_BTN_MODS,
            this::onMods
        ));

        LayoutRegion rightCol = rightColumn();
        int quitX = rightCol.x + rightCol.w - BTN_SLIM_W;

        if (compactLayout) {
            int slimGap = Spacing.GAP_SMALL;
            int slimW = Math.max(48, Math.min(BTN_SLIM_W,
                (this.width - btnX * 2 - slimGap) / 2));
            int slimRowW = slimW * 2 + slimGap;
            int slimX = (this.width - slimRowW) / 2;
            int slimY = Math.max(8, Math.min(36,
                btnY - BTN_SLIM_H - slimGap));

            this.addRenderableWidget(new SlimButton(slimX, slimY, slimW, BTN_SLIM_H,
                Component.translatable("gripeverde.title.button.options"),
                this::onOptions));

            this.addRenderableWidget(new SlimButton(slimX + slimW + slimGap, slimY, slimW, BTN_SLIM_H,
                Component.translatable("gripeverde.title.button.quit"),
                () -> this.minecraft.stop()));
        } else if (isMinimalMode()) {
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
        // Boton >> (skip musica) queda solo (AMB removido, ahora en Sonidos)
        int skipY = compactLayout ? 8 : this.height - 28;
        int skipX = 8;
        int skipW = 36;
        int skipH = 20;
        this.skipMusicButton = new SlimButton(skipX, skipY, skipW, skipH,
            Component.literal(">>"),
            () -> MenuMusic.skipToNext())
            .withClickSound(com.eternalcraft.gripeverde.client.audio.UiSounds::clickCreate);
        this.skipMusicButton.setTooltip(Tooltip.create(Component.literal(isEnglishSelected()
            ? "Change the chronicler's piece"
            : "Cambiar la pieza del cronista")));
        this.addRenderableWidget(this.skipMusicButton);
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
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1f / 20f);
        try {
            com.eternalcraft.gripeverde.client.lore.HiddenCipher.tick();

            // El tiempo enferma: el segundero ya no disimula.
            if (com.eternalcraft.gripeverde.client.render.PocketWatchRenderer.isTimeSick()) {
                com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                    com.eternalcraft.gripeverde.client.lore.Discoveries.TIME_SICK);
            }
            // v0.25.0: el que mira la tormenta — tres relampagos sin irse de la vigilia.
            if (com.eternalcraft.gripeverde.client.audio.MenuAmbience.isStormNight()) {
                int fc = com.eternalcraft.gripeverde.client.render.LivingAtmosphere.flashCount();
                if (stormFlashBaseline < 0) stormFlashBaseline = fc;
                else if (fc - stormFlashBaseline >= 3) {
                    stormFlashBaseline = fc;
                    if (com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                            com.eternalcraft.gripeverde.client.lore.Discoveries.STORM_WATCHER)) {
                        com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                            com.eternalcraft.gripeverde.client.lore.Marginalia.N_FLASH3);
                    }
                }
            } else {
                stormFlashBaseline = -1;
            }
            // Una noche entera frente al papel: el alba enferma aparece.
            long sessionMin = (System.currentTimeMillis() - sessionStartMs) / 60_000L;
            if (sessionMin >= 20L && !dawnActive) {
                dawnActive = true;
                com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                    com.eternalcraft.gripeverde.client.lore.Discoveries.NIGHT_WHOLE);
                com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                    com.eternalcraft.gripeverde.client.lore.Marginalia.N_NIGHT);
            }
            // Diez hallazgos: el cronista deja de escribir solo.
            if (com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.discoveredCount() >= 10
                    && com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("reader10_marked") == 0) {
                com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.setCounter("reader10_marked", 1);
                com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                    com.eternalcraft.gripeverde.client.lore.Marginalia.N_READER10);
            }
        } catch (Throwable ignored) {}
        if (this.skipMusicButton != null) {
            boolean ready = MenuMusic.isActive() && !MenuMusic.isCrossfading();
            this.skipMusicButton.active = ready;
            this.skipMusicButton.setTooltip(Tooltip.create(Component.literal(
                ready
                    ? (isEnglishSelected() ? "Change the chronicler's piece" : "Cambiar la pieza del cronista")
                    : (isEnglishSelected() ? "The gramophone is turning" : "El gramofono esta girando")
            )));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        lastInteractionMs = System.currentTimeMillis();
        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        if (ctrl && keyCode == GLFW.GLFW_KEY_S) { onSingleplayer(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        lastInteractionMs = System.currentTimeMillis();
        // v0.16.0: el telegrafo oculto oye lo que el lector escribe en la vigilia.
        try {
            if (com.eternalcraft.gripeverde.client.lore.HiddenCipher.onCharTyped(codePoint)) {
                return true;
            }
        } catch (Throwable ignored) {}
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // v0.26.0: los faroles reconocen la mano del lector.
        com.eternalcraft.gripeverde.client.render.LivingAtmosphere.setMouse(mouseX, mouseY);
        // Professional centralized scene renderer
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderTitleScene(g, this.width, this.height);
        drawGlassPatina(g);
        drawDawnIfDue(g);

        if (!cinematicMode) {
            // v0.16.0: el reloj sostenido en la mano ocupa la vigilia entera.
            if (com.eternalcraft.gripeverde.client.render.PocketWatchRenderer.isInspecting()) {
                com.eternalcraft.gripeverde.client.render.PocketWatchRenderer.drawInspection(
                    g, this.width, this.height, GripeVerdeMod.VERSION);
                return;
            }

            if (!isMinimalMode()) {
                drawPosterLeft(g);
                drawPosterRight(g);
                drawPocketWatch(g);
            }

            com.eternalcraft.gripeverde.client.render.LogoRenderer.setWatchedMouse(mouseX, mouseY);
            drawLogo(g);
            drawSignature(g);
            super.render(g, mouseX, mouseY, partialTick);
            drawGreeting(g);
            if (overQuarantinePoster(mouseX, mouseY)) {
                g.renderTooltip(this.font,
                    Component.translatable("gripeverde.poster.quarantine.guide_hint"),
                    mouseX, mouseY);
            }
        } else {
            drawCinematicHint(g);
        }

        // Overlays interactivos (arriba de todo)
        // v0.23.2: es decoracion pura; jamas debe tumbar el render del cliente
        // (en 0.22.0 un AIOOBE aqui crasheaba en bucle la sesion).
        try {
            drawEasterEggMessage(g);
        } catch (Throwable ignored) {}
        // v0.24.0: esquirlas del sello roto; igual de intocables.
        try {
            drawSealShards(g);
        } catch (Throwable ignored) {}
        // Easter egg: relampago verde (suprimido con movimiento reducido).
        if (!reducedMotion() && System.currentTimeMillis() < easterLightningUntilMs) {
            long rem = easterLightningUntilMs - System.currentTimeMillis();
            int a = (int)(50 * rem / 300L);
            if (a > 2) {
                g.fill(0, 0, this.width, this.height, (a << 24) | 0x3A5028);
            }
        }

        drawShootStarMessage(g);

        // ── v0.16.0: capas nuevas de la vigilia ──
        try {
            com.eternalcraft.gripeverde.client.lore.HiddenCipher.drawBuffer(g, this.width, this.height);
            com.eternalcraft.gripeverde.client.lore.HiddenCipher.drawTelegram(g, this.width, this.height);
            com.eternalcraft.gripeverde.client.lore.HiddenCipher.drawPereiraFlash(g, this.width, this.height);
            drawDustIfDue(g);
            drawVigilLine(g);
        } catch (Throwable ignored) {}
    }

    /** El alba enferma: solo si el lector paso la noche entera. */
    private void drawDawnIfDue(GuiGraphics g) {
        if (!dawnActive) return;
        float breath = reducedMotion() ? 0.5f
            : (float)(Math.sin(System.currentTimeMillis() / 9000.0) * 0.5 + 0.5);
        int a = (int)(10 + breath * 10);
        int top = (int)(this.height * 0.62f);
        int bot = this.height;
        g.fill(0, top, this.width, top + Math.max(8, this.height / 10),
            (a << 24) | 0x3A5028);
        g.fill(0, top + this.height / 10, this.width, bot,
            ((a / 2) << 24) | 0x2A3A20);
    }

    /** Polvo por ausencia larga: solo la primera vigilia del regreso. */
    private static final java.util.Random DUST_RNG = new java.util.Random();
    private void drawDustIfDue(GuiGraphics g) {
        if (!dustThisSession) return;
        DUST_RNG.setSeed(0xD05E7L ^ (long)this.width);
        for (int i = 0; i < 40; i++) {
            int x = DUST_RNG.nextInt(Math.max(1, this.width));
            int y = DUST_RNG.nextInt(Math.max(1, this.height));
            int al = 14 + DUST_RNG.nextInt(16);
            g.fill(x, y, x + 1, y + 1, (al << 24) | 0x9A938A);
        }
    }

    /** Saludo de papel, estatico y limitado al menu principal. */
    private void drawGreeting(GuiGraphics g) {
        long now = System.currentTimeMillis();
        if (greetingText == null || now >= greetingShownUntilMs || isMinimalMode()) return;

        int cardW = Math.min(360, this.width - 32);
        if (cardW < 96) return;
        String line = com.eternalcraft.gripeverde.client.render.DiaryChrome
            .fitText(this.font, greetingText, cardW - 24);
        if (line.isEmpty()) return;

        int cardH = 24;
        int x = (this.width - cardW) / 2;
        // La marca del año termina cerca de y=161; esta ficha empieza debajo.
        int y = 168;
        g.fill(x + 2, y + 2, x + cardW + 2, y + cardH + 2,
            com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(
                com.eternalcraft.gripeverde.client.render.DiaryTheme.INK_BLACK, 72));
        g.fill(x, y, x + cardW, y + cardH,
            com.eternalcraft.gripeverde.client.render.DiaryTheme.PAPER_DARK);
        g.fill(x, y, x + cardW, y + 1,
            com.eternalcraft.gripeverde.client.render.DiaryTheme.GOLD_TARNISHED);
        g.fill(x, y + cardH - 1, x + cardW, y + cardH,
            com.eternalcraft.gripeverde.client.render.DiaryTheme.SEPIA_MID);
        int textX = x + (cardW - this.font.width(line)) / 2;
        g.drawString(this.font, line, textX, y + 8,
            com.eternalcraft.gripeverde.client.render.DiaryTheme.SEPIA_DARK, false);
    }

    /** La vigilia nota cuando el lector se queda quieto demasiado tiempo. */
    private void drawVigilLine(GuiGraphics g) {
        long idle = System.currentTimeMillis() - lastInteractionMs;
        if (idle < 60_000L) return;
        String line;
        if (idle < 150_000L) {
            line = Component.translatable("gripeverde.title.vigil.present").getString();
        } else {
            line = Component.translatable("gripeverde.title.vigil.fog").getString();
        }
        line = com.eternalcraft.gripeverde.client.render.DiaryChrome
            .fitText(this.font, line, this.width - 32);
        if (line.isEmpty()) return;
        int pulse = reducedMotion() ? 60 : (int)(Math.sin(System.currentTimeMillis() / 700.0) * 22 + 60);
        int wTxt = this.font.width(line);
        g.drawString(this.font, line, this.width / 2 - wTxt / 2, this.height / 3 + 24,
            (Math.max(20, pulse) << 24) | 0xA8B890, false);
    }

    @Override
    public void removed() {
        // MenuAmbience.stop(); -- REMOVIDO: ambiente persiste en subpantallas (como la musica)
        // MenuMusic.stop() omitido intencionalmente: musica sobrevive a Options/subpantallas
        super.removed();
    }

    private void drawCinematicHint(GuiGraphics g) {
        if (this.width < 420 || System.currentTimeMillis() > cinematicHintUntilMs) return;
        boolean english = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        String hint = english ? "F10 to return" : "F10 para volver";
        int x = this.width - this.font.width(hint) - 10;
        int y = this.height - 16;
        int col = com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(
            com.eternalcraft.gripeverde.client.render.DiaryTheme.SEPIA_LIGHT, 100);
        g.drawString(this.font, hint, x, y, col, false);
    }

    private void drawGlassPatina(GuiGraphics g) {
        if (this.width < 360 || this.height < 240) return;
        ensurePatinaPoints();
        boolean still = reducedMotion();
        long frame = still ? 0L : System.currentTimeMillis() / 120L;
        for (int i = 0; i < PATINA_COUNT; i++) {
            int x = PATINA_X[i];
            int y = PATINA_Y[i];
            int alpha = still ? 14 : 10 + (int)((Math.sin((frame + i * 13) * 0.17) + 1.0) * 7.0);
            int col = (alpha << 24) | 0xC0A06A;
            if (PATINA_KIND[i] == 0) g.fill(x, y, x + 1, y + 4, col);
            else g.fill(x, y, x + 1, y + 1, col);
        }
        for (int i = 0; i < 6; i++) {
            int x = 18 + (i * 97) % Math.max(32, this.width - 36);
            int y = 18 + (i * 53) % Math.max(32, this.height - 70);
            int a = still ? 20 : 20 + (int)(Math.sin(System.currentTimeMillis() / 900.0 + i) * 9.0);
            int col = (Math.max(8, a) << 24) | 0xD8D8C8;
            g.fill(x, y, x + 2, y + 5, col);
            g.fill(x + 1, y + 5, x + 2, y + 8, (Math.max(4, a / 2) << 24) | 0xD8D8C8);
        }
    }

    private void ensurePatinaPoints() {
        if (patinaW == this.width && patinaH == this.height) return;
        patinaW = this.width;
        patinaH = this.height;
        java.util.Random rng = new java.util.Random(0x5A11EEDL ^ (long)this.width * 31L ^ (long)this.height * 17L);
        for (int i = 0; i < PATINA_COUNT; i++) {
            PATINA_X[i] = rng.nextInt(Math.max(1, this.width));
            PATINA_Y[i] = rng.nextInt(Math.max(1, this.height));
            PATINA_KIND[i] = rng.nextInt(4) == 0 ? 0 : 1;
        }
    }


    private int[] quarantinePosterRect() {
        if (cinematicMode || isMinimalMode()) return null;
        LayoutRegion leftCol = leftColumn();
        int totalBtnH = BTN_H * 2 + Spacing.GAP_MEDIUM;
        int maxPosterH = leftCol.h - totalBtnH - Spacing.GAP_LARGE * 2;
        if (maxPosterH < 140) return null;
        return new int[] { leftCol.x, leftCol.y, POSTER_W, Math.min(POSTER_H_MAX, maxPosterH) };
    }

    private boolean overQuarantinePoster(double mouseX, double mouseY) {
        int[] rect = quarantinePosterRect();
        return rect != null && mouseX >= rect[0] && mouseX <= rect[0] + rect[2]
            && mouseY >= rect[1] && mouseY <= rect[1] + rect[3];
    }

    private void drawPosterLeft(GuiGraphics g) {
        int[] rect = quarantinePosterRect();
        if (rect == null) return;
        PosterRenderer.drawQuarantineNotice(g, rect[0], rect[1], rect[2], rect[3]);
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
        long now = System.currentTimeMillis();
        int shakeX = 0, shakeY = 0;
        if (!reducedMotion() && now < skullShakeUntilMs) {
            long remain = skullShakeUntilMs - now;
            float intensity = (float) remain / 600f;
            shakeX = (int)(Math.sin(now / 40.0) * 3 * intensity);
            shakeY = (int)(Math.cos(now / 30.0) * 2 * intensity);
        }
        LogoRenderer.draw(g, this.width / 2 + shakeX, 30 + shakeY);

        // Renderizar particulas verdes cayendo solo cuando el movimiento esta habilitado.
        if (!reducedMotion() && now < eggParticlesUntilMs) {
            float dt = 0.05f;
            for (int p = 0; p < EGG_PARTICLE_COUNT; p++) {
                if (eggParticles[p][4] <= 0f) continue;
                eggParticles[p][0] += eggParticles[p][2] * dt * 60;
                eggParticles[p][1] += eggParticles[p][3] * dt * 60;
                eggParticles[p][4] -= 0.008f;  // fade
                if (eggParticles[p][4] < 0f) eggParticles[p][4] = 0f;

                int alpha = (int)(eggParticles[p][4] * 200);
                if (alpha > 3) {
                    int col = (alpha << 24) | 0x5A7038;  // verde toxico
                    int px = (int) eggParticles[p][0];
                    int py = (int) eggParticles[p][1];
                    g.fill(px, py, px + 2, py + 2, col);
                }
            }
        }
    }

    // Rotating signature pool, synced with LogoRenderer year via yearIndex.
    // Role titles are localized to avoid "Cronista" appearing in English.
    private static final String[] SIGNATURE_POOL_ES = {
        "Dr. S. Pereira",
        "Cronista R. Da Rosa",
        "Cap. G. Bandeira",
        "Prof. A. Focco"
    };
    private static final String[] SIGNATURE_POOL_EN = {
        "Dr. S. Pereira",
        "Chronicler R. Da Rosa",
        "Capt. G. Bandeira",
        "Prof. A. Focco"
    };

    private static String signatureFor(int idx) {
        String[] pool = isEnglishSelected() ? SIGNATURE_POOL_EN : SIGNATURE_POOL_ES;
        return pool[idx % pool.length];
    }

    private void drawSignature(GuiGraphics g) {
        int idx = com.eternalcraft.gripeverde.client.render.LogoRenderer.getYearIndex();
        String signature = signatureFor(idx);
        // v0.16.0: tras oir su nombre en voz alta, el cronista firma a su manera.
        try {
            if (idx == 0 && com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("pereira_offended") > 0) {
                signature = isEnglishSelected()
                    ? "Dr. S. Pereira (you again?)"
                    : "Dr. S. Pereira (¿otra vez tu?)";
            }
        } catch (Throwable ignored) {}
        // Nombre viejo tachado: el anterior del pool
        int oldIdx = (idx + SIGNATURE_POOL_ES.length - 1) % SIGNATURE_POOL_ES.length;
        String oldSig = signatureFor(oldIdx);

        int sw = this.font.width(signature);
        int oldSw = this.font.width(oldSig);
        int y = this.height - 12;

        // En ventanas compactas, conserva solo la firma vigente para que no
        // se cruce con el control de música del borde inferior.
        if (isCompactLayout()) {
            String compactSignature = com.eternalcraft.gripeverde.client.render.DiaryChrome
                .fitText(this.font, signature, this.width - 62);
            if (!compactSignature.isEmpty()) {
                g.drawString(this.font, compactSignature, 48, y, DiaryTheme.SEPIA_LIGHT, false);
            }
            return;
        }

        // ORDEN NUEVO: nombre nuevo a la izquierda, nombre viejo tachado a la derecha
        // Esto se lee mejor: "el nuevo cronista, el anterior tachado"

        // v0.30.0: la firma jamas entra en la franja del gramofo (>>), y el
        // mensaje del dia se lee a su IZQUIERDA, en la misma linea — antes
        // flotaba encima y podia pisar la mesa de botones.
        int newX = Math.max(48, this.width - sw - oldSw - 16);
        int newY = y;
        // v0.21.25: firma limpia sin sombra.
        g.drawString(this.font, signature,
            newX, newY,
            DiaryTheme.SEPIA_LIGHT, false);

        // Nombre viejo tachado (derecha, mas tenue)
        int oldX = this.width - oldSw - 6;
        int oldColor = com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(
            com.eternalcraft.gripeverde.client.render.DiaryTheme.SEPIA_LIGHT, 100);
        g.drawString(this.font, oldSig, oldX, y, oldColor, false);

        // Tachadura sobre nombre viejo
        int strikeY = y + 4;
        g.fill(oldX - 1, strikeY, oldX + oldSw + 1, strikeY + 1,
            com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(0x00000000, 180));
        g.fill(oldX + 2, strikeY - 1, oldX + oldSw - 1, strikeY,
            com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(0x00000000, 90));

    }

    private void onSingleplayer() { this.minecraft.setScreen(new SelectWorldScreen(this)); }
    private void onMultiplayer()  { this.minecraft.setScreen(new DiaryMultiplayerScreen(this)); }
    private void onMods()         { this.minecraft.setScreen(new ModListScreen(this)); }
    private void onOptions() { this.minecraft.setScreen(new DiaryOptionsScreen(this)); }

    @Override public boolean isPauseScreen()    { return false; }
    @Override public boolean shouldCloseOnEsc() { return false; }

    // Rotating subtitles for GreenFluButtons (multiplayer + mods).
    // Keep them language-aware so the Title does not mix Spanish subtitles with
    // English buttons after changing language.
    private static final String[] MULTIPLAYER_SUBTITLES_ES = {
        "Luchen junto a sus hermanos",
        "El regimiento los espera",
        "Nadie sobrevive en soledad",
        "Que su sangre sea la ultima",
        "Marchen al frente",
        "Que nadie cruce solo la niebla",
        "La guardia acepta nuevos nombres",
        "Hay luces en otros barrios",
        "Compartan el pan y la fiebre",
        "Los hospedajes abren sus libretas",
        "Un cordon se sostiene entre todos",
        "Escriban su nombre en la guardia"
    };
    private static final String[] MULTIPLAYER_SUBTITLES_EN = {
        "Fight beside your brothers",
        "The regiment awaits",
        "No one survives alone",
        "May your blood be the last",
        "March to the front",
        "No one crosses the fog alone",
        "The guard accepts new names",
        "There are lights in other districts",
        "Share the bread and the fever",
        "The lodgings open their ledgers",
        "A cordon is held by every hand",
        "Write your name in the watch"
    };
    private static final String[] MODS_SUBTITLES_ES = {
        "Preparen su equipamiento",
        "Revisen los arsenales",
        "El acero no descansa",
        "Que instrumentos tenemos hoy",
        "Reservas de la ultima campana",
        "Inventario de instrumentos prohibidos",
        "Las cajas del sanatorio",
        "Herramientas para una ciudad enferma",
        "Que piezas sostienen esta noche",
        "Cada pieza lleva su sello",
        "Lo que el cordon permite",
        "Cuenten las lamparas antes de salir"
    };
    private static final String[] MODS_SUBTITLES_EN = {
        "Prepare your equipment",
        "Inspect the arsenals",
        "Steel does not rest",
        "What instruments do we have tonight",
        "Stores from the last bell",
        "Inventory of forbidden instruments",
        "The sanatorium crates",
        "Tools for a sickened city",
        "What pieces hold this night together",
        "Every piece bears its seal",
        "What the cordon allows",
        "Count the lamps before leaving"
    };
    private static final java.util.Random SUB_RNG = new java.util.Random();

    private static String cachedMultiSubtitle = null;
    private static String cachedModsSubtitle = null;
    private static String cachedSubtitleLang = null;

    private static boolean isEnglishSelected() {
        return com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
    }

    private static String pickRotatingSubtitle(boolean multiplayer) {
        String lang = net.minecraft.client.Minecraft.getInstance().getLanguageManager().getSelected();
        if (lang == null) lang = "en_us";
        boolean english = isEnglishSelected();
        if (!java.util.Objects.equals(cachedSubtitleLang, lang)) {
            cachedSubtitleLang = lang;
            cachedMultiSubtitle = null;
            cachedModsSubtitle = null;
        }
        String[] pool = multiplayer
            ? (english ? MULTIPLAYER_SUBTITLES_EN : MULTIPLAYER_SUBTITLES_ES)
            : (english ? MODS_SUBTITLES_EN : MODS_SUBTITLES_ES);
        if (multiplayer) {
            if (cachedMultiSubtitle == null) cachedMultiSubtitle = pool[SUB_RNG.nextInt(pool.length)];
            return cachedMultiSubtitle;
        }
        if (cachedModsSubtitle == null) cachedModsSubtitle = pool[SUB_RNG.nextInt(pool.length)];
        return cachedModsSubtitle;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        lastInteractionMs = System.currentTimeMillis();

        if (button == 0) {
            try {
                // ── v0.16.0: la vigilia responde ──
                // Un gesto mientras el susurro sigue en el aire: alguien contesta.
                com.eternalcraft.gripeverde.client.audio.MenuAmbience.answerWhisper();

                // El reloj en la mano se cierra con cualquier gesto.
                if (com.eternalcraft.gripeverde.client.render.PocketWatchRenderer.isInspecting()) {
                    com.eternalcraft.gripeverde.client.render.PocketWatchRenderer.closeInspection();
                    return true;
                }
                int w = this.width, h = this.height;

                // El ojo del emblema no esta pintado: mira.
                int logoCx = w / 2;
                if (mouseX >= logoCx + 1 && mouseX <= logoCx + 9 && mouseY >= 52 && mouseY <= 60) {
                    com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                        com.eternalcraft.gripeverde.client.lore.Discoveries.SKULL_WATCHES);
                }

                // El reloj de bolsillo: detenerlo revela la inscripcion.
                LayoutRegion rightCol = rightColumn();
                int watchCx = rightCol.x + rightCol.w - WATCH_RADIUS - Spacing.GAP_MEDIUM;
                int watchCy = rightCol.y + WATCH_RADIUS + Spacing.GAP_LARGE;
                if (!isMinimalMode()
                        && mouseX >= watchCx - WATCH_RADIUS - 10 && mouseX <= watchCx + WATCH_RADIUS + 10
                        && mouseY >= watchCy - WATCH_RADIUS - 14 && mouseY <= watchCy + WATCH_RADIUS + 22) {
                    handleWatchClick();
                    return true;
                }

                // v0.32.0: el edicto abre la copia de campo sin censura.
                if (overQuarantinePoster(mouseX, mouseY)) {
                    this.minecraft.setScreen(new DiaryFieldGuideScreen(this));
                    return true;
                }

                // El cartel del buscado: sostenerle la mirada.
                if (isWantedPosterShowing()) {
                    int[] rr = wantedPosterRect();
                    if (rr != null && mouseX >= rr[0] && mouseX <= rr[0] + rr[2]
                            && mouseY >= rr[1] && mouseY <= rr[1] + rr[3]) {
                        wantedPressing = true;
                        wantedPressStartMs = System.currentTimeMillis();
                    }
                }

                // Los faroles de la avenida: v0.21.29 easter egg removido.
                // (Las linternas ya no se pueden apagar)

                // El gato del tejado cobra sus deudas despacio.
                int[] catR = com.eternalcraft.gripeverde.client.render.VictorianSkyline.catRect(w, h);
                if (!isMinimalMode() && mouseX >= catR[0] && mouseX <= catR[0] + catR[2]
                        && mouseY >= catR[1] && mouseY <= catR[1] + catR[3]) {
                    handleCatClick();
                    return true;
                }

                // La rata que se detuvo.
                if (com.eternalcraft.gripeverde.client.render.LivingAtmosphere.ratHit(mouseX, mouseY, w, h)) {
                    if (com.eternalcraft.gripeverde.client.render.LivingAtmosphere.isRatStanding()) {
                        com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                            com.eternalcraft.gripeverde.client.lore.Discoveries.RAT_PAUSE);
                    }
                    com.eternalcraft.gripeverde.client.render.LivingAtmosphere.scareRat();
                    return true;
                }

                // La ventana del atico espera respuesta.
                int[] sw = com.eternalcraft.gripeverde.client.render.VictorianSkyline.signalWindowRect(w, h);
                if (mouseX >= sw[0] - 2 && mouseX <= sw[0] + sw[2] + 2
                        && mouseY >= sw[1] - 2 && mouseY <= sw[1] + sw[3] + 2) {
                    com.eternalcraft.gripeverde.client.render.VictorianSkyline.signalWindowClicked();
                    return true;
                }

                // v0.25.0: la lluvia se deja atrapar (nunca sobre la mesa de botones).
                if (mouseY < estimateFirstButtonTop() - 8) {
                    int di = com.eternalcraft.gripeverde.client.render.LivingAtmosphere.rainDropAt(mouseX, mouseY, w, h);
                    if (di >= 0) {
                        com.eternalcraft.gripeverde.client.render.LivingAtmosphere.scareDrop(di);
                        if (com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                                com.eternalcraft.gripeverde.client.lore.Discoveries.DROP_CATCHER)) {
                            com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                                com.eternalcraft.gripeverde.client.lore.Marginalia.N_DROP);
                        }
                        com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                            com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_TICK.get(), 0.10f, 1.9f);
                        return true;
                    }
                }
            } catch (Throwable ignored) {}
        }

        // Zona del logo: rectangulo central superior
        int logoCx = this.width / 2;
        boolean overLogo = mouseX >= logoCx - 100 && mouseX <= logoCx + 100
                        && mouseY >= 30 && mouseY <= 130;
        if (overLogo && button == 0) {
            handleLogoClick();
            // No consumir el evento, dejar pasar al super
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        lastInteractionMs = System.currentTimeMillis();
        if (wantedPressing && button == 0) {
            wantedPressing = false;
            long held = System.currentTimeMillis() - wantedPressStartMs;
            if (held >= 1400L) {
                try {
                    boolean first = com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                        com.eternalcraft.gripeverde.client.lore.Discoveries.WANTED_EYES);
                    if (first) {
                        com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.setCounter("wanted_eyes", 1);
                    }
                    com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                        com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_AMBIENT_OCCASIONAL.get(),
                        0.16f, 0.66f);
                } catch (Throwable ignored) {}
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    // ── v0.16.0: interacciones de la vigilia ────────────────────────────────

    private void handleWatchClick() {
        int clicks = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.bump("watchClicks");
        if (clicks == 1) {
            com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                com.eternalcraft.gripeverde.client.lore.Discoveries.WATCH_INSCRIPTION);
            com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                com.eternalcraft.gripeverde.client.lore.Marginalia.N_KEY_HINT);
            com.eternalcraft.gripeverde.client.render.PocketWatchRenderer.toggleInspection();
            return;
        }
        if (clicks >= 5 && !com.eternalcraft.gripeverde.client.lore.ChroniclerMemory
                .hasCollectible(com.eternalcraft.gripeverde.client.lore.Discoveries.ITEM_KEY)) {
            com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.addCollectible(
                com.eternalcraft.gripeverde.client.lore.Discoveries.ITEM_KEY);
            com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                com.eternalcraft.gripeverde.client.lore.Discoveries.WATCH_KEY);
            com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                com.eternalcraft.gripeverde.client.lore.Marginalia.N_DRAWER_HINT);
            try {
                // La llave cae y suena contra la piedra.
                com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                    com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_TICK.get(), 0.22f, 2.2f);
                com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                    com.eternalcraft.gripeverde.client.audio.ModSounds.UI_CLICK_HEAVY.get(), 0.18f, 1.4f);
            } catch (Throwable ignored) {}
            return;
        }
        com.eternalcraft.gripeverde.client.render.PocketWatchRenderer.toggleInspection();
    }


    private void handleCatClick() {
        int n = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.bump("catClicks");
        try {
            // El gato protesta, a su manera.
            com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_AMBIENT_OCCASIONAL.get(),
                0.14f, 1.65f);
        } catch (Throwable ignored) {}
        if (n >= 7 && !com.eternalcraft.gripeverde.client.lore.ChroniclerMemory
                .hasCollectible(com.eternalcraft.gripeverde.client.lore.Discoveries.ITEM_SCRAP)) {
            com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.addCollectible(
                com.eternalcraft.gripeverde.client.lore.Discoveries.ITEM_SCRAP);
            com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                com.eternalcraft.gripeverde.client.lore.Discoveries.CAT_SCRAP);
        }
    }

    /** El cartel derecho rota a diario; solo el del buscado acepta miradas largas. */
    private boolean isWantedPosterShowing() {
        long day = System.currentTimeMillis() / 86_400_000L;
        return (int)(Math.abs(day) % 3) == 1;
    }

    private int[] wantedPosterRect() {
        if (isMinimalMode()) return null;
        LayoutRegion rightCol = rightColumn();
        int topReserved = (WATCH_RADIUS * 2) + 30 + Spacing.GAP_LARGE;
        int botReserved = BTN_SLIM_H + Spacing.GAP_LARGE * 2;
        int available = rightCol.h - topReserved - botReserved;
        if (available < 120) return null;
        int posterH = Math.min(POSTER_R_H_MAX, available);
        int posterX = rightCol.x + rightCol.w - POSTER_R_W;
        int posterY = rightCol.y + topReserved;
        return new int[]{ posterX, posterY, POSTER_R_W, posterH };
    }
    // Easter egg: click en el logo -> expediente sellado aleatorio
    private static final String[] LOGO_EASTER_EGGS_ES = {
        "El reloj del ministerio acaba de marcar una hora que no existe.",
        "Una carta sin sello respira dentro del cajon del cronista.",
        "La ventana del tercer piso respondio con una luz verde.",
        "Las ratas dejaron una flecha perfecta apuntando al rio.",
        "El nombre del ultimo testigo aparecio escrito antes de llegar.",
        "La campana conto a los ausentes, no a los vivos.",
        "Hay barro fresco bajo una puerta que nadie ha abierto.",
        "El gato del tejado pronuncio un apellido y salto a la niebla."
    };
    private static final String[] LOGO_EASTER_EGGS_EN = {
        "The ministry clock has just struck an hour that does not exist.",
        "An unsealed letter is breathing inside the chronicler's drawer.",
        "The third-floor window answered with a green light.",
        "The rats left a perfect arrow pointing toward the river.",
        "The last witness's name appeared before the witness arrived.",
        "The bell counted the missing, not the living.",
        "Fresh mud waits beneath a door no one has opened.",
        "The rooftop cat spoke a surname and leapt into the fog."
    };

    private long lastLogoClickMs = 0L;
    private String easterEggMessage = null;
    private String easterEggCaseCode = "";
    private long easterEggStartedMs = 0L;
    private long easterEggUntilMs = 0L;

    // v0.24.0: el lacre del telegrama cede ante una mano terca (9 golpes seguidos).
    private int sealTapStreak = 0;
    private long sealTapLastMs = 0L;
    private long sealShardsStartMs = 0L;
    private long sealShardsUntilMs = 0L;
    private final float[][] sealShards = new float[7][4]; // x,y,vx,vy
    private static final java.util.Random SEAL_RNG = new java.util.Random();
    private long easterFlashUntilMs = 0L;
    private long skullShakeUntilMs = 0L;
    // v0.25.0: linea base de relampagos contados desde que la vigilia abrio (storm_watcher).
    private int stormFlashBaseline = -1;
    private long easterLightningUntilMs = 0L;
    // Particulas verdes cayendo del logo tras easter egg
    private static final int EGG_PARTICLE_COUNT = 20;
    private final float[][] eggParticles = new float[EGG_PARTICLE_COUNT][5];  // x, y, vx, vy, alpha
    private long eggParticlesUntilMs = 0L;
    private static final java.util.Random EASTER_RNG = new java.util.Random();
    private static final java.util.Random PARTICLE_RNG = new java.util.Random();

    private void handleLogoClick() {
        long now = System.currentTimeMillis();
        if (now - lastLogoClickMs < 260L) return;
        lastLogoClickMs = now;

        // v0.24.0: golpear el sello una y otra vez, sin dejarlo respirar.
        if (now - sealTapLastMs <= 1500L) sealTapStreak++; else sealTapStreak = 1;
        sealTapLastMs = now;
        if (sealTapStreak >= 9) breakWaxSeal();

        String[] eggPool = isEnglishSelected() ? LOGO_EASTER_EGGS_EN : LOGO_EASTER_EGGS_ES;
        int caseNo = 17 + EASTER_RNG.nextInt(80);
        char letter = (char)('A' + EASTER_RNG.nextInt(6));
        easterEggCaseCode = String.format(java.util.Locale.ROOT, "%02d-%c", caseNo, letter);
        easterEggMessage = eggPool[EASTER_RNG.nextInt(eggPool.length)];
        boolean reduced = reducedMotion();
        easterEggStartedMs = reduced ? now - easterEggMessage.length() * 22L : now;
        easterEggUntilMs = now + 5200L;
        skullShakeUntilMs = reduced ? 0L : now + 520L;
        easterLightningUntilMs = reduced ? 0L : now + 280L;

        if (!reduced) {
            LivingAtmosphere.triggerLightningFlash(1.0f);
            eggParticlesUntilMs = now + 2600L;
            int logoCx = this.width / 2;
            int logoCy = 45;
            for (int p = 0; p < EGG_PARTICLE_COUNT; p++) {
                eggParticles[p][0] = logoCx + (float)(PARTICLE_RNG.nextDouble() * 34 - 17);
                eggParticles[p][1] = logoCy + (float)(PARTICLE_RNG.nextDouble() * 12);
                eggParticles[p][2] = (float)(PARTICLE_RNG.nextDouble() * 1.6 - 0.8);
                eggParticles[p][3] = (float)(PARTICLE_RNG.nextDouble() * 0.9 + 0.8);
                eggParticles[p][4] = 1.0f;
            }
        } else {
            eggParticlesUntilMs = 0L;
        }

        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_CHAIN.get(),
                0.32f, 0.92f);
            com.eternalcraft.gripeverde.client.audio.UiSounds.notification();
        } catch (Throwable ignored) {}
        easterFlashUntilMs = reduced ? 0L : now + 400L;
    }

    /** v0.24.0: el sello de lacre salta en esquirlas; el papel cae. */
    private void breakWaxSeal() {
        sealTapStreak = 0;
        long now = System.currentTimeMillis();
        boolean reduced = reducedMotion();
        int cx = this.width / 2;
        int cy = 45;
        for (int i = 0; i < sealShards.length; i++) {
            sealShards[i][0] = cx + (float)(SEAL_RNG.nextDouble() * 22 - 11);
            sealShards[i][1] = cy + (float)(SEAL_RNG.nextDouble() * 10 - 5);
            sealShards[i][2] = (float)(SEAL_RNG.nextDouble() * 140 - 70);
            sealShards[i][3] = -20f - (float)(SEAL_RNG.nextDouble() * 120);
        }
        sealShardsStartMs = now;
        sealShardsUntilMs = reduced ? 0L : now + 1500L;
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                com.eternalcraft.gripeverde.client.audio.ModSounds.UI_PAPER_FALL.get(), 0.9f, 0.78f);
            com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                com.eternalcraft.gripeverde.client.audio.ModSounds.UI_CLICK_HEAVY.get(), 0.40f, 1.38f);
        } catch (Throwable ignored) {}
        try {
            com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                com.eternalcraft.gripeverde.client.lore.Discoveries.SEAL_BREAKER);
            com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                com.eternalcraft.gripeverde.client.lore.Marginalia.N_SEAL);
        } catch (Throwable ignored) {}
    }

    /** v0.24.0: esquirlas de lacre con gravedad; pura decoracion. */
    private void drawSealShards(net.minecraft.client.gui.GuiGraphics g) {
        long now = System.currentTimeMillis();
        if (sealShardsStartMs == 0L || now >= sealShardsUntilMs) return;
        float t = (now - sealShardsStartMs) / 1000f;
        float fade = (sealShardsUntilMs - now) / 1500f;
        int alpha = (int)(220 * Math.min(1f, fade * 2f));
        if (alpha <= 3) return;
        int wax = (alpha << 24) | 0x7A2415;
        int waxLight = (Math.min(alpha, 140) << 24) | 0xB8402A;
        for (float[] sh : sealShards) {
            float x = sh[0] + sh[2] * t;
            float y = sh[1] + sh[3] * t + 380f * t * t;
            if (y > this.height) continue;
            int px = (int) x, py = (int) y;
            g.fill(px, py, px + 2, py + 2, wax);
            g.fill(px, py, px + 1, py + 1, waxLight);
        }
    }

    private void drawEasterEggMessage(net.minecraft.client.gui.GuiGraphics g) {
        long now = System.currentTimeMillis();

        if (!reducedMotion() && now < easterFlashUntilMs) {
            long remainFlash = easterFlashUntilMs - now;
            int flashAlpha = (int)(54 * remainFlash / 400L);
            if (flashAlpha > 2) {
                g.fill(0, 0, this.width, this.height, (flashAlpha << 24) | 0x4A6428);
            }
        }

        if (easterEggMessage == null || now >= easterEggUntilMs) return;

        long remain = easterEggUntilMs - now;
        int alpha = remain < 900L ? (int)(230 * remain / 900L) : 230;
        alpha = Math.max(0, Math.min(230, alpha));

        int elapsedChars = reducedMotion()
            ? easterEggMessage.length()
            : (int)Math.min(easterEggMessage.length(), Math.max(0, (now - easterEggStartedMs) / 22L));
        String visibleMessage = easterEggMessage.substring(0, elapsedChars);

        int boxW = Math.max(220, Math.min(330, this.width - 96));
        // v0.25.2: jamas mas ancho que la propia pantalla (escala GUI 4).
        boxW = Math.min(boxW, Math.max(120, this.width - 16));
        java.util.List<net.minecraft.util.FormattedCharSequence> lines =
            this.font.split(Component.literal(visibleMessage), boxW - 34);
        // v0.23.2: con la maquina de escribir, el primer fotograma tiene 0 letras y
        // font.split("") devuelve lista VACIA; indexarla crashaba el cliente (AIOOBE
        // reportado en 0.22.0). lineCount jamas puede superar lines.size().
        int lineCount = Math.min(3, lines.size());
        int boxH = 31 + Math.max(1, lineCount) * this.font.lineHeight;

        int buttonTop = estimateFirstButtonTop();
        int safeTop = isMinimalMode() ? 76 : 166;
        // v0.25.2: en escalas GUI 3-4 la banda libre se achica y el orden de los
        // clamps importa — el segundo volvia a clavar el cartel SOBRE la mesa de
        // botones (reporte del owner). Regla: jamas pisar botones, jamas salir
        // por arriba; si no hay banda libre, sube sobre la franja del logo.
        int maxTop = buttonTop - boxH - 10;
        int boxY = (maxTop >= safeTop) ? safeTop + (maxTop - safeTop) / 2
                                       : Math.max(8, maxTop);
        int boxX = (this.width - boxW) / 2;

        int paper = (alpha << 24) | 0xE6D2A6;
        int ink = (alpha << 24) | 0x241608;
        int bronze = (Math.min(205, alpha) << 24) | 0x8E6A35;
        int green = (Math.min(180, alpha) << 24) | 0x5A7038;
        int red = (Math.min(210, alpha) << 24) | 0x6E1F16;
        int shadow = (Math.min(80, alpha / 2) << 24) | 0x000000;

        // Thin telegram slip: sits between logo and lower buttons, never over controls.
        g.fill(boxX + 3, boxY + 4, boxX + boxW + 4, boxY + boxH + 5, shadow);
        g.fill(boxX, boxY, boxX + boxW, boxY + boxH, paper);
        g.fill(boxX, boxY, boxX + boxW, boxY + 1, bronze);
        g.fill(boxX, boxY + boxH - 1, boxX + boxW, boxY + boxH, bronze);
        g.fill(boxX, boxY, boxX + 1, boxY + boxH, bronze);
        g.fill(boxX + boxW - 1, boxY, boxX + boxW, boxY + boxH, bronze);
        g.fill(boxX + 8, boxY + 7, boxX + 20, boxY + 8, green);
        g.fill(boxX + boxW - 20, boxY + 7, boxX + boxW - 8, boxY + 8, green);

        String stamp = isEnglishSelected() ? "CASE " + easterEggCaseCode : "CASO " + easterEggCaseCode;
        g.drawString(this.font, stamp, boxX + 13, boxY + 8, red, false);
        String seal = isEnglishSelected() ? "UNDER SEAL" : "BAJO SELLO";
        int sealW = this.font.width(seal);
        // v0.21.25: texto limpio sin sombra.
        g.drawString(this.font, seal, boxX + boxW - sealW - 13, boxY + 8, DiaryTheme.withAlpha(0xFF241608, alpha), false);
        g.fill(boxX + 12, boxY + 19, boxX + boxW - 12, boxY + 20, (Math.min(115, alpha) << 24) | 0x422810);

        int textY = boxY + 25;
        for (int i = 0; i < lineCount; i++) {
            g.drawString(this.font, lines.get(i), boxX + 15, textY + i * this.font.lineHeight, ink, false);
        }

        int pulse = 80 + (int)(Math.sin(now / 120.0) * 45.0);
        int dot = (Math.min(alpha, Math.max(30, pulse)) << 24) | 0x5A7038;
        g.fill(boxX + boxW - 10, boxY + boxH - 10, boxX + boxW - 6, boxY + boxH - 6, dot);
        if (elapsedChars < easterEggMessage.length()) {
            int cx = boxX + 15 + this.font.width(this.font.plainSubstrByWidth(visibleMessage, boxW - 44));
            g.fill(Math.min(boxX + boxW - 18, cx + 2), boxY + boxH - 13,
                Math.min(boxX + boxW - 16, cx + 4), boxY + boxH - 5, green);
        }
    }

    private int estimateFirstButtonTop() {
        try {
            LayoutRegion leftCol = leftColumn();
            int totalBtnH = BTN_H * 2 + Spacing.GAP_MEDIUM;
            return leftCol.anchorY(Anchor.BOT_LEFT, totalBtnH) - Spacing.GAP_LARGE;
        } catch (Throwable ignored) {
            return this.height - 96;
        }
    }

    private void drawShootStarMessage(net.minecraft.client.gui.GuiGraphics g) {
        if (reducedMotion()) return;
        String ssm = com.eternalcraft.gripeverde.client.render.AtmosphereRenderer.shootStarMessage;
        long untilMs = com.eternalcraft.gripeverde.client.render.AtmosphereRenderer.shootStarMessageUntilMs;
        if (ssm == null) return;
        long now = System.currentTimeMillis();
        if (now >= untilMs) return;
        long remain = untilMs - now;
        int alpha = remain < 800L ? (int)(220 * remain / 800L) : 220;
        int col = (alpha << 24) | 0xE8DCB4;
        int shadowCol = (alpha << 24) | 0x000000;
        int w = this.font.width(ssm);
        // Posicion: arriba del logo (y ~200 para que sea visible)
        int cx = this.width / 2 - w / 2;
        int cy = this.height / 3;
        // v0.21.25: texto limpio sin sombra.
        g.drawString(this.font, ssm, cx, cy, col, false);
    }
}
