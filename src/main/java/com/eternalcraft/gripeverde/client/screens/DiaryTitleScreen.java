package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.GripeVerdeMod;
import com.eternalcraft.gripeverde.client.audio.MenuAmbience;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;

import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.DiaryTypography;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.render.LogoRenderer;
import com.eternalcraft.gripeverde.client.render.PocketWatchRenderer;
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
    private boolean skipTooltipInitialized = false;
    private boolean lastSkipTooltipReady = false;
    private boolean lastSkipTooltipEnglish = false;

    // ── v0.16.0: la memoria de la vigilia ───────────────────────────────────
    private static boolean memorySessionBegun = false;
    private long sessionStartMs = System.currentTimeMillis();
    private long lastInteractionMs = System.currentTimeMillis();
    private boolean dustThisSession = false;
    private boolean dawnActive = false;
    private static long greetingShownUntilMs = 0L;
    private static String greetingText = null;
    private static String greetingTranslationKey = null;
    private static boolean greetingEnglish = true;
    private static boolean greetingLanguageInitialized = false;
    private String cachedGreetingSource = null;
    private int cachedGreetingCardWidth = -1;
    private boolean cachedGreetingEnglish = false;
    private net.minecraft.client.gui.Font cachedGreetingFont = null;
    private java.util.List<net.minecraft.util.FormattedCharSequence> cachedGreetingLines = java.util.Collections.emptyList();
    private String cachedGreetingLabel = "";
    private int cachedGreetingLabelLimit = -1;
    private String cachedGreetingHeaderLine = "";
    private String cachedGreetingHeaderSource = null;
    private int cachedGreetingHeaderLimit = -1;
    private net.minecraft.client.gui.Font cachedGreetingHeaderFont = null;
    private int cachedVigilPhase = -1;
    private int cachedVigilWidth = -1;
    private boolean cachedVigilEnglish = false;
    private net.minecraft.client.gui.Font cachedVigilFont = null;
    private String cachedVigilLine = "";
    private int cachedVigilTextWidth = 0;

    // v0.184.0: botones de la portada mas chicos. Dejaban de ser invitacion y
    // pasaban a ser mueble: menos ancho, menos alto y mas aire para el fondo,
    // que es lo que el lector viene a mirar.
    private static final int BTN_W = 264;
    private static final int BTN_H = 34;

    private static final int BTN_SLIM_W = 126;
    private static final int BTN_SLIM_H = 28;

    // v0.188.0: la portada ya no tiene periodicos. La franja izquierda queda del
    // ancho de la mesa de botones y la derecha, del reloj y su cadena.
    private static final int LEFT_COLUMN_W = 180;
    private static final int WATCH_RADIUS = 42;
    private static final int WATCH_COLUMN_W = WATCH_RADIUS * 2 + 20;

    // Umbral: por debajo de esta altura efectiva, ocultamos TODO lo decorativo
    private static final int MINIMAL_MODE_THRESHOLD = 290;

    // Ancla superior del emblema en la portada.
    private static final int LOGO_TOP = 30;

    // Borde inferior de la fila de iconos superiores (20 px de alto en y=8).
    private static final int ICON_ROW_BOTTOM = 28;

    // Por debajo de esta escala el emblema no se lee: en ventanas extremas se
    // omite antes que dibujarlo encima de la mesa de botones.
    private static final float LOGO_MIN_SCALE = 0.5f;

    private int columnCacheWidth = -1;
    private int columnCacheHeight = -1;
    private LayoutRegion cachedLeftColumn;
    private LayoutRegion cachedRightColumn;

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
        this.skipTooltipInitialized = false;
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
                    greetingTranslationKey = "gripeverde.title.greeting.first";
                    greetingText = DiaryChrome.tr(greetingTranslationKey);
                    com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                        com.eternalcraft.gripeverde.client.lore.Marginalia.N_FIRST_NIGHT);
                } else if (kind == 2 && absence >= 7L) {
                    greetingTranslationKey = "gripeverde.title.greeting.return";
                    greetingText = DiaryChrome.tr(greetingTranslationKey);
                    dustThisSession = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.consumeDust();
                    com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                        com.eternalcraft.gripeverde.client.lore.Discoveries.DUST);
                    com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                        com.eternalcraft.gripeverde.client.lore.Marginalia.N_DUST);
                } else if (kind == 2) {
                    greetingTranslationKey = "gripeverde.title.greeting.remembers";
                    greetingText = DiaryChrome.tr(greetingTranslationKey);
                } else if (kind == 3) {
                    greetingTranslationKey = "gripeverde.title.greeting.vigil";
                    greetingText = DiaryChrome.tr(greetingTranslationKey);
                }
                if (greetingText != null) {
                    greetingEnglish = isEnglishSelected();
                    greetingLanguageInitialized = true;
                    greetingShownUntilMs = System.currentTimeMillis() + 8_000L;
                }
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

        int compactRail = compactLayout ? compactSlimRailWidth() : 0;
        int compactEdge = compactInset();
        int mainButtonW = compactLayout
            ? Math.max(60, Math.min(BTN_W, this.width - compactEdge * 2
                - (compactRail > 0 ? compactRail + Spacing.GAP_SMALL : 0)))
            : BTN_W;
        // El grupo (pareja + carril) se centra: a 320 px ocupa justo el ancho
        // interior y en ventanas mas anchas queda sobre el eje del emblema.
        int compactGroupW = mainButtonW
            + (compactRail > 0 ? compactRail + Spacing.GAP_SMALL : 0);
        int btnX = compactLayout
            ? Math.max(compactEdge, (this.width - compactGroupW) / 2)
            : leftCol.x;
        int totalBtnH = BTN_H * 2 + Spacing.GAP_MEDIUM;
        LayoutRegion buttonColumn = compactLayout
            ? LayoutRegion.screen(this.width, this.height).inset(btnX)
            : leftCol;
        // La mesa arranca donde dice buttonTableTop(): es la misma linea que
        // usa el emblema para decidir su escala, asi que no pueden discrepar.
        int btnY = compactLayout
            ? buttonTableTop()
            : buttonColumn.anchorY(Anchor.BOT_LEFT, totalBtnH) - Spacing.GAP_LARGE;

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
            // v0.176.0: Ajustes y Salir suben a un carril a la derecha de la
            // pareja principal, dentro de la misma banda vertical de la mesa.
            // La mesa ya no toca el emblema, asi que no hay solape posible.
            int railW = compactRail;
            int railX = btnX + mainButtonW + Spacing.GAP_SMALL;
            railX = Math.max(btnX, Math.min(railX, this.width - 4 - railW));
            int railStackH = BTN_SLIM_H * 2 + Spacing.GAP_SMALL;
            int railY = btnY + Math.max(0, (totalBtnH - railStackH) / 2);

            this.addRenderableWidget(new SlimButton(railX, railY, railW, BTN_SLIM_H,
                Component.translatable("gripeverde.title.button.options"),
                this::onOptions));

            this.addRenderableWidget(new SlimButton(railX, railY + BTN_SLIM_H + Spacing.GAP_SMALL,
                railW, BTN_SLIM_H,
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

        // v0.221.0: el manual de campo sale de la portada (el lector lo pidio
        // quitar) y en su sitio queda el diario de supervivencia, que es lo que
        // antes era la bitacora: el cuaderno del que sobrevive, no el registro
        // tecnico del mod.
        SlimButton diaryBtn = new SlimButton(
            this.width - 52, 8, 20, 20,
            Component.literal(isEnglishSelected() ? "Diary" : "Diario"),
            () -> {
                try {
                    com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                        com.eternalcraft.gripeverde.client.audio.ModSounds.UI_PAPER_FALL.get(),
                        0.30f, 1.0f);
                } catch (Throwable ignored) {}
                this.minecraft.setScreen(new ChangelogScreen(this, true));
            })
            .withIcon(SlimButton.Icon.OPEN_LEDGER);
        diaryBtn.setTooltip(Tooltip.create(
            Component.literal(isEnglishSelected() ? "Survival diary" : "Diario de supervivencia")));
        this.addRenderableWidget(diaryBtn);

        // v0.180.0: el parte de CEDA ya es nuestro (DiaryDispatchScreen), asi que el
        // acceso esta siempre; CopyL, si esta instalado, se abre desde adentro.
        if (this.width >= 124) {
            SlimButton dispatchBtn = new SlimButton(
                this.width - 76, 8, 20, 20,
                Component.translatable("gripeverde.title.button.copyl"),
                () -> this.minecraft.setScreen(new DiaryDispatchScreen(this)))
                .withIcon(SlimButton.Icon.DISPATCH_NOTE);
            dispatchBtn.setTooltip(Tooltip.create(
                Component.translatable("gripeverde.title.button.copyl")));
            this.addRenderableWidget(dispatchBtn);
        }

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

    /**
     * Borde superior de la mesa de botones principales, con la misma formula
     * que usa {@link #init()}. Es la linea que ningun adorno de la portada
     * puede cruzar.
     */
    private int buttonTableTop() {
        int totalBtnH = BTN_H * 2 + Spacing.GAP_MEDIUM;
        int inset = isCompactLayout() ? compactInset() : Spacing.SCREEN_MARGIN;
        return this.height - inset - totalBtnH - Spacing.GAP_LARGE;
    }

    /** Margen lateral de la portada compacta: el mismo que usan los botones. */
    private int compactInset() {
        return Math.max(8, Math.min(20, this.width / 16));
    }

    /**
     * v0.176.0: ancho del carril donde viven Ajustes y Salir en ventanas
     * estrechas. Antes colgaban de una fila fija en y=36..68, justo encima del
     * emblema (y=30..108 a 427x240): 32 px de botones sobre el escudo en todas
     * las ventanas de menos de 620 px. El carril ocupa como mucho el 28 % del
     * ancho interior para que la pareja principal conserve rotulo y subtitulo.
     */
    private int compactSlimRailWidth() {
        int inner = Math.max(0, this.width - compactInset() * 2);
        return Math.max(40, Math.min(110, inner * 28 / 100));
    }

    /** Escala del emblema: se encoge solo cuando la banda libre se queda corta. */
    private float logoScale() {
        float free = buttonTableTop() - Spacing.GAP_MEDIUM - LOGO_TOP;
        if (free >= LogoRenderer.BLOCK_H) return 1f;
        return Math.max(LOGO_MIN_SCALE, free / (float) LogoRenderer.BLOCK_H);
    }

    /** Borde inferior real del emblema ya escalado. */
    private int logoBottom() {
        return LOGO_TOP + Math.round(LogoRenderer.BLOCK_H * logoScale());
    }

    /**
     * v0.178.0: ningun adorno de la portada puede comerse un clic destinado a un
     * boton. Los puntos calientes (rata, gato, ventana del atico, carteles) se
     * comprobaban antes que los widgets y varios devolvian true: a 427x240 la
     * ventana del atico cae 12 px dentro de "Multijugador" y a 854x480 la rata
     * pasa por dentro de la mesa y de Ajustes/Salir. Se preguntan los widgets
     * reales para no duplicar la geometria de init().
     */
    private boolean overAnyButton(double mouseX, double mouseY) {
        for (net.minecraft.client.gui.components.events.GuiEventListener child : this.children()) {
            if (child instanceof net.minecraft.client.gui.components.AbstractWidget widget
                    && widget.visible && widget.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    private void ensureColumnLayout() {
        if (columnCacheWidth == this.width && columnCacheHeight == this.height
                && cachedLeftColumn != null && cachedRightColumn != null) {
            return;
        }

        columnCacheWidth = this.width;
        columnCacheHeight = this.height;
        LayoutRegion screen = LayoutRegion.screen(this.width, this.height);
        cachedLeftColumn = screen.left(LEFT_COLUMN_W + Spacing.SCREEN_MARGIN * 2)
                .inset(Spacing.SCREEN_MARGIN);
        int colW = WATCH_COLUMN_W + Spacing.SCREEN_MARGIN * 2;
        cachedRightColumn = screen.right(colW).inset(Spacing.SCREEN_MARGIN);
    }

    private LayoutRegion leftColumn() {
        ensureColumnLayout();
        return cachedLeftColumn;
    }

    private LayoutRegion rightColumn() {
        ensureColumnLayout();
        return cachedRightColumn;
    }

    @Override
    public void tick() {
        this.ticks++;
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1f / 20f);
        try {
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
            boolean english = isEnglishSelected();
            this.skipMusicButton.active = ready;
            if (!skipTooltipInitialized || ready != lastSkipTooltipReady
                    || english != lastSkipTooltipEnglish) {
                String tooltip = ready
                    ? (english ? "Change the chronicler's piece" : "Cambiar la pieza del cronista")
                    : (english ? "The gramophone is turning" : "El gramofono esta girando");
                this.skipMusicButton.setTooltip(Tooltip.create(Component.literal(tooltip)));
                lastSkipTooltipReady = ready;
                lastSkipTooltipEnglish = english;
                skipTooltipInitialized = true;
            }
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
        // v0.184.0: fuera el huevo de teclear cifras y fechas en la portada.
        // La vigilia ya no escucha lo que se escribe; los unicos secretos que
        // quedan viven en el fondo (imagenes y rincones de la avenida).
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // v0.26.0: los faroles reconocen la mano del lector.
        com.eternalcraft.gripeverde.client.render.LivingAtmosphere.setMouse(mouseX, mouseY);
        // Professional centralized scene renderer
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderTitleScene(g, this.width, this.height);
        drawDawnIfDue(g);

        if (!cinematicMode) {
            // v0.16.0: el reloj sostenido en la mano ocupa la vigilia entera.
            if (com.eternalcraft.gripeverde.client.render.PocketWatchRenderer.isInspecting()) {
                com.eternalcraft.gripeverde.client.render.PocketWatchRenderer.drawInspection(
                    g, this.width, this.height, GripeVerdeMod.VERSION);
                return;
            }

            if (!isMinimalMode()) {
                // v0.188.0: fuera los periodicos de la portada (bando de
                // cuarentena y "se busca"); queda la ciudad al fondo.
                drawPocketWatch(g);
            }

            com.eternalcraft.gripeverde.client.render.LogoRenderer.setWatchedMouse(mouseX, mouseY);
            drawLogo(g);
            drawSignature(g);
            super.render(g, mouseX, mouseY, partialTick);
            renderChroniclerNote(g);
        } else {
            drawCinematicHint(g);
        }

        // v0.221.0: fuera los adornos de la cabecera. Ya no hay cristal sobre la
        // escena ni huevo que se dispare pulsando el emblema; la vigilia se
        // queda con el cielo, la ciudad y lo que de verdad esta pasando.
        drawShootStarMessage(g);

        // ── v0.16.0: capas nuevas de la vigilia ──
        try {
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
    private static final int RETURN_DUST_COUNT = 40;
    private static final java.util.Random DUST_RNG = new java.util.Random();
    private static final int[] RETURN_DUST_X = new int[RETURN_DUST_COUNT];
    private static final int[] RETURN_DUST_Y = new int[RETURN_DUST_COUNT];
    private static final int[] RETURN_DUST_ALPHA = new int[RETURN_DUST_COUNT];
    private static int returnDustWidth = -1;
    private static int returnDustHeight = -1;

    private void drawDustIfDue(GuiGraphics g) {
        if (!dustThisSession) return;
        if (returnDustWidth != this.width || returnDustHeight != this.height) {
            DUST_RNG.setSeed(0xD05E7L ^ (long)this.width);
            for (int i = 0; i < RETURN_DUST_COUNT; i++) {
                RETURN_DUST_X[i] = DUST_RNG.nextInt(Math.max(1, this.width));
                RETURN_DUST_Y[i] = DUST_RNG.nextInt(Math.max(1, this.height));
                RETURN_DUST_ALPHA[i] = 14 + DUST_RNG.nextInt(16);
            }
            returnDustWidth = this.width;
            returnDustHeight = this.height;
        }
        for (int i = 0; i < RETURN_DUST_COUNT; i++) {
            int x = RETURN_DUST_X[i];
            int y = RETURN_DUST_Y[i];
            int al = RETURN_DUST_ALPHA[i];
            g.fill(x, y, x + 1, y + 1, (al << 24) | 0x9A938A);
        }
    }

    /** Nota de archivo: ficha completa si cabe, o una linea en la cabecera. */
    private void renderChroniclerNote(GuiGraphics g) {
        long now = System.currentTimeMillis();
        if (greetingText == null || now >= greetingShownUntilMs) return;

        boolean english = isEnglishSelected();
        if (greetingTranslationKey != null
                && (!greetingLanguageInitialized || greetingEnglish != english)) {
            greetingText = DiaryChrome.tr(greetingTranslationKey);
            greetingEnglish = english;
            greetingLanguageInitialized = true;
        }

        if (drawChroniclerCard(g, english)) return;
        // v0.179.0: si la ficha no cabe —ventanas estrechas o bajas— el cronista
        // no se calla: deja la misma frase en una linea en la cabecera, entre el
        // control de musica y los iconos de acceso.
        drawGreetingHeader(g);
    }

    /** Intenta la ficha completa; devuelve false cuando no hay hueco. */
    private boolean drawChroniclerCard(GuiGraphics g, boolean english) {
        if (isMinimalMode()) return false;

        // Reserva el hueco entre los dos carteles para que la nota nunca los tape.
        LayoutRegion left = leftColumn();
        LayoutRegion right = rightColumn();
        int centerLane = right.x - (left.x + left.w);
        int cardW = Math.min(390, Math.min(this.width - 32, centerLane - 16));
        if (cardW < 180) return false;

        String registry = "CEDA · 1800";
        int registryW = DiaryTypography.width(this.font, registry);
        int labelLimit = Math.max(48, cardW - registryW - 28);
        if (!java.util.Objects.equals(cachedGreetingSource, greetingText)
                || cachedGreetingCardWidth != cardW
                || cachedGreetingEnglish != english
                || cachedGreetingFont != this.font
                || cachedGreetingLabelLimit != labelLimit) {
            cachedGreetingLines = DiaryTypography.split(this.font, greetingText, cardW - 24);
            String label = DiaryChrome.tr("gripeverde.title.greeting.label");
            cachedGreetingLabel = DiaryTypography.fitString(this.font, label, labelLimit);
            cachedGreetingSource = greetingText;
            cachedGreetingCardWidth = cardW;
            cachedGreetingEnglish = english;
            cachedGreetingFont = this.font;
            cachedGreetingLabelLimit = labelLimit;
        }
        if (cachedGreetingLines.isEmpty()) return false;

        int cardH = 26 + cachedGreetingLines.size() * this.font.lineHeight;
        int x = (this.width - cardW) / 2;
        // La marca del año termina cerca de y=161; la ficha queda debajo.
        // Nunca sobre la mesa de botones: con y fijo en 168 la ficha caia 46 px
        // encima de "Multijugador" en 620x290 y 700x300.
        int y = Math.max(logoBottom() + Spacing.GAP_SMALL, 168);
        int limit = buttonTableTop() - Spacing.GAP_MEDIUM;
        if (y + cardH > limit) {
            if (limit - logoBottom() - Spacing.GAP_SMALL < cardH) return false;
            y = limit - cardH;
        }
        if (y < logoBottom() + Spacing.GAP_SMALL) return false;
        g.fill(x + 2, y + 2, x + cardW + 2, y + cardH + 2,
            DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 72));
        // v0.224.0: la nota del cronista es una hoja suelta y rasgada sobre la mesa.
        PaperTexture.drawTornSheet(
            g, x, y, cardW, cardH, PaperTexture.PaperKind.ARCHIVE);

        DiaryTypography.draw(g, this.font, cachedGreetingLabel, x + 12, y + 5,
            DiaryTheme.SEPIA_MID, false);
        DiaryTypography.draw(g, this.font, registry, x + cardW - registryW - 12, y + 5,
            DiaryTheme.SEPIA_MID, false);
        g.fill(x + 10, y + 17, x + cardW - 10, y + 18, DiaryChrome.INK_DIVIDER);

        int textY = y + 21;
        for (int i = 0; i < cachedGreetingLines.size(); i++) {
            net.minecraft.util.FormattedCharSequence line = cachedGreetingLines.get(i);
            int lineW = DiaryTypography.width(this.font, line);
            DiaryTypography.draw(g, this.font, line, x + (cardW - lineW) / 2,
                textY + i * this.font.lineHeight, DiaryTheme.SEPIA_DARK, false);
        }
        return true;
    }

    /**
     * v0.179.0: la misma frase en una linea cuando la ficha no cabe. La cabecera
     * central estaba libre en todos los tamanos (musica en x 8..44, iconos desde
     * width-76), asi que se recorta a width-160 y se centra en y=10.
     */
    private void drawGreetingHeader(GuiGraphics g) {
        int limitW = Math.max(60, this.width - 160);
        if (cachedGreetingHeaderLimit != limitW
                || this.font != cachedGreetingHeaderFont
                || !java.util.Objects.equals(cachedGreetingHeaderSource, greetingText)) {
            cachedGreetingHeaderLine = DiaryTypography.fitString(this.font, greetingText, limitW);
            cachedGreetingHeaderLimit = limitW;
            cachedGreetingHeaderSource = greetingText;
            cachedGreetingHeaderFont = this.font;
        }
        if (cachedGreetingHeaderLine.isEmpty()) return;
        int lineW = DiaryTypography.width(this.font, cachedGreetingHeaderLine);
        DiaryTypography.draw(g, this.font, cachedGreetingHeaderLine,
            (this.width - lineW) / 2, 10,
            DiaryTheme.withAlpha(DiaryTheme.PAPER, 190), false);
    }

    /** La vigilia nota cuando el lector se queda quieto demasiado tiempo. */
    private void drawVigilLine(GuiGraphics g) {
        long idle = System.currentTimeMillis() - lastInteractionMs;
        if (idle < 60_000L) return;
        int phase = idle < 150_000L ? 0 : 1;
        boolean english = isEnglishSelected();
        if (phase != cachedVigilPhase || this.width != cachedVigilWidth
                || english != cachedVigilEnglish || this.font != cachedVigilFont) {
            String line = Component.translatable(phase == 0
                ? "gripeverde.title.vigil.present"
                : "gripeverde.title.vigil.fog").getString();
            cachedVigilLine = DiaryTypography.fitString(this.font, line, this.width - 32);
            cachedVigilTextWidth = DiaryTypography.width(this.font, cachedVigilLine);
            cachedVigilPhase = phase;
            cachedVigilWidth = this.width;
            cachedVigilEnglish = english;
            cachedVigilFont = this.font;
        }
        if (cachedVigilLine.isEmpty()) return;
        int pulse = reducedMotion() ? 60 : (int)(Math.sin(System.currentTimeMillis() / 700.0) * 22 + 60);
        DiaryTypography.draw(g, this.font, cachedVigilLine,
            this.width / 2 - cachedVigilTextWidth / 2, vigilLineY(),
            (Math.max(20, pulse) << 24) | 0xA8B890, false);
    }

    /**
     * v0.177.0: el susurro de la vigilia nacia en height/3 + 24, que en cualquier
     * ventana de menos de ~410 px de alto cae dentro del emblema (a 427x240
     * cruzaba el filete del ano; a 700x400, el rotulo del dia). Busca la primera
     * banda libre: la que queda entre el emblema y la mesa de botones y, si no
     * hay altura para una linea, el pie sobre la firma.
     */
    private int vigilLineY() {
        int lineH = this.font.lineHeight;
        int y = this.height / 3 + 24;
        // 8 px bajo el emblema: a 560x315 el filete de la fecha termina en 161 y
        // con 4 px el susurro quedaba pegado al rotulo.
        int bandTop = logoBottom() + Spacing.GAP_MEDIUM;
        int bandBottom = buttonTableTop() - Spacing.GAP_SMALL;
        if (y < bandTop || y + lineH > bandBottom) {
            if (bandBottom - bandTop >= lineH) {
                y = Math.max(bandTop, Math.min(y, bandBottom - lineH));
            } else {
                y = this.height - 24;
            }
        }
        return y;
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
        int x = this.width - DiaryTypography.width(this.font, hint) - 10;
        int y = this.height - 16;
        int col = com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(
            com.eternalcraft.gripeverde.client.render.DiaryTheme.SEPIA_LIGHT, 100);
        DiaryTypography.draw(g, this.font, hint, x, y, col, false);
    }




    private void drawPocketWatch(GuiGraphics g) {
        LayoutRegion rightCol = rightColumn();
        int diameter = WATCH_RADIUS * 2 + 30; // + espacio para cadena y label
        int reserved = BTN_SLIM_H + Spacing.GAP_LARGE * 2;

        if (rightCol.h - reserved < diameter) return;

        int cx = rightCol.x + rightCol.w - WATCH_RADIUS - Spacing.GAP_MEDIUM;
        int cy = rightCol.y + WATCH_RADIUS + Spacing.GAP_LARGE;
        // v0.175.0: la cadena colgaba desde y=0 y cruzaba la fila de iconos de
        // arriba a la derecha (crónica, guía, CEDA), que ocupa y 8..28 en el
        // mismo carril. Arranca por debajo de esa fila; si no queda sitio el
        // propio renderer omite la cadena en vez de dibujar un muñón.
        int chainTop = Math.max(rightCol.y, ICON_ROW_BOTTOM + Spacing.GAP_SMALL);
        PocketWatchRenderer.draw(g, cx, cy, chainTop, WATCH_RADIUS, GripeVerdeMod.VERSION);
    }

    private void drawLogo(GuiGraphics g) {
        // v0.175.0: en ventanas bajas el emblema se encoge (nunca se omite si
        // aun se lee) para no caer sobre la mesa de botones. A 427x240 —el
        // tamano por defecto a escala GUI 2— el bloque completo invadia 45 px.
        float logoScale = logoScale();
        if (buttonTableTop() - Spacing.GAP_MEDIUM - LOGO_TOP
                < LogoRenderer.BLOCK_H * LOGO_MIN_SCALE) {
            return;
        }
        LogoRenderer.draw(g, this.width / 2, LOGO_TOP, logoScale);

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

    // v0.203.0: firma compacta memorizada.
    private String cachedCompactSig = "";
    private String cachedCompactSigSrc = "";
    private int cachedCompactSigWidth = -1;
    private net.minecraft.client.gui.Font cachedCompactSigFont;

    private void drawSignature(GuiGraphics g) {
        int idx = com.eternalcraft.gripeverde.client.render.LogoRenderer.getYearIndex();
        String signature = signatureFor(idx);
        // v0.16.0: tras oir su nombre en voz alta, el cronista firma a su manera.
        try {
            if (idx == 0 && com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("pereira_offended") > 0) {
                signature = isEnglishSelected()
                    ? "Dr. S. Pereira (you again?)"
                    : "Dr. S. Pereira (¿otra vez tú?)";
            }
        } catch (Throwable ignored) {}
        // Nombre viejo tachado: el anterior del pool
        int oldIdx = (idx + SIGNATURE_POOL_ES.length - 1) % SIGNATURE_POOL_ES.length;
        String oldSig = signatureFor(oldIdx);

        // v0.203.0: los anchos de las firmas se miden una vez por texto.
        int sw = DiaryChrome.widthCached(this.font, signature);
        int oldSw = DiaryChrome.widthCached(this.font, oldSig);
        int y = this.height - 12;

        // En ventanas compactas, conserva solo la firma vigente para que no
        // se cruce con el control de música del borde inferior.
        if (isCompactLayout()) {
            // v0.203.0: el recorte de la firma compacta se repite en cada cuadro
            // con la misma firma y el mismo ancho; se guarda el resultado.
            int sigLimit = this.width - 62;
            if (this.cachedCompactSigFont != this.font || this.cachedCompactSigWidth != sigLimit
                    || !this.cachedCompactSigSrc.equals(signature)) {
                this.cachedCompactSig = DiaryTypography.fitString(this.font, signature, sigLimit);
                this.cachedCompactSigFont = this.font;
                this.cachedCompactSigWidth = sigLimit;
                this.cachedCompactSigSrc = signature;
            }
            String compactSignature = this.cachedCompactSig;
            if (!compactSignature.isEmpty()) {
                DiaryTypography.draw(g, this.font, compactSignature, 48, y, DiaryTheme.SEPIA_LIGHT, false);
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
        DiaryTypography.draw(g, this.font, signature,
            newX, newY,
            DiaryTheme.SEPIA_LIGHT, false);

        // Nombre viejo tachado (derecha, mas tenue)
        int oldX = this.width - oldSw - 6;
        int oldColor = com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(
            com.eternalcraft.gripeverde.client.render.DiaryTheme.SEPIA_LIGHT, 100);
        DiaryTypography.draw(g, this.font, oldSig, oldX, y, oldColor, false);

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
        "Vayan siempre con los suyos.",
        "El regimiento los espera.",
        "Nadie sobrevive en soledad.",
        "Que la fiebre termine aquí.",
        "La línea del frente los llama.",
        "No crucen solos la niebla.",
        "La guardia aún necesita gente.",
        "Hay faroles encendidos en otros barrios.",
        "Compartan el pan, no la fiebre.",
        "En las posadas apuntan cada llegada.",
        "El cordón se sostiene entre todos.",
        "Dejen su nombre con la guardia."
    };
    private static final String[] MULTIPLAYER_SUBTITLES_EN = {
        "Stay close to your own.",
        "The regiment is waiting.",
        "No one survives alone.",
        "Let the fever end here.",
        "The front line is calling.",
        "Don't cross the fog alone.",
        "The watch still needs hands.",
        "Lamps still burn in other wards.",
        "Share the bread, not the fever.",
        "The inns keep a record of every arrival.",
        "The cordon holds because we do.",
        "Leave your name with the watch."
    };
    private static final String[] MODS_SUBTITLES_ES = {
        "Revisen cada arma antes de salir.",
        "El acero aún está frío.",
        "¿Qué nos queda para esta noche?",
        "Las últimas reservas de la campana.",
        "Instrumentos que conviene no nombrar.",
        "Las cajas del sanatorio siguen cerradas.",
        "Hay herramientas para una ciudad enferma.",
        "¿Qué piezas nos quedan?",
        "Cada pieza lleva su marca.",
        "El cordón decide qué puede salir.",
        "Revisen los faroles antes de partir.",
        "Cuenten las lámparas antes de salir."
    };
    private static final String[] MODS_SUBTITLES_EN = {
        "Check each weapon before you leave.",
        "The steel is still cold.",
        "What have we left for tonight?",
        "The last stores from the bell tower.",
        "Tools best left unnamed.",
        "The sanatorium crates remain shut.",
        "Tools for a city gone ill.",
        "What pieces do we have left?",
        "Every piece bears its mark.",
        "The cordon decides what may pass.",
        "Check the lamps before you leave.",
        "Count the lamps before setting out."
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
                boolean overButton = overAnyButton(mouseX, mouseY);

                // El ojo del emblema no esta pintado: mira.
                int logoCx = w / 2;
                // El ojo vive en el bloque sin escalar a y 52..60; con el
                // emblema encogido hay que llevar el clic a esa misma escala.
                float eyeScale = logoScale();
                int eyeCy = LOGO_TOP + Math.round(56 * eyeScale);
                int eyeHalf = Math.max(3, Math.round(4 * eyeScale));
                if (mouseX >= logoCx + 1 && mouseX <= logoCx + 9
                        && mouseY >= eyeCy - eyeHalf && mouseY <= eyeCy + eyeHalf) {
                    com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                        com.eternalcraft.gripeverde.client.lore.Discoveries.SKULL_WATCHES);
                }

                // El reloj de bolsillo: detenerlo revela la inscripcion.
                LayoutRegion rightCol = rightColumn();
                int watchCx = rightCol.x + rightCol.w - WATCH_RADIUS - Spacing.GAP_MEDIUM;
                int watchCy = rightCol.y + WATCH_RADIUS + Spacing.GAP_LARGE;
                if (!overButton && !isMinimalMode()
                        && mouseX >= watchCx - WATCH_RADIUS - 10 && mouseX <= watchCx + WATCH_RADIUS + 10
                        && mouseY >= watchCy - WATCH_RADIUS - 14 && mouseY <= watchCy + WATCH_RADIUS + 22) {
                    handleWatchClick();
                    return true;
                }

                // Los faroles de la avenida: v0.21.29 easter egg removido.
                // (Las linternas ya no se pueden apagar)

                // El gato del tejado cobra sus deudas despacio.
                int[] catR = com.eternalcraft.gripeverde.client.render.VictorianSkyline.catRect(w, h);
                if (!overButton && !isMinimalMode()
                        && mouseX >= catR[0] && mouseX <= catR[0] + catR[2]
                        && mouseY >= catR[1] && mouseY <= catR[1] + catR[3]) {
                    handleCatClick();
                    return true;
                }

                // La rata que se detuvo.
                if (!overButton
                        && com.eternalcraft.gripeverde.client.render.LivingAtmosphere.ratHit(mouseX, mouseY, w, h)) {
                    if (com.eternalcraft.gripeverde.client.render.LivingAtmosphere.isRatStanding()) {
                        com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                            com.eternalcraft.gripeverde.client.lore.Discoveries.RAT_PAUSE);
                    }
                    com.eternalcraft.gripeverde.client.render.LivingAtmosphere.scareRat();
                    return true;
                }

                // La ventana del atico espera respuesta.
                int[] sw = com.eternalcraft.gripeverde.client.render.VictorianSkyline.signalWindowRect(w, h);
                if (!overButton
                        && mouseX >= sw[0] - 2 && mouseX <= sw[0] + sw[2] + 2
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

        // Zona del logo: rectangulo central superior. Alto real del bloque ya
        // escalado: con una caja fija hasta y=130, en ventanas de 240 px el
        // borde de "Multijugador" (y=116) disparaba tambien el sello del emblema.
        int logoCx = this.width / 2;
        int logoBottom = logoBottom();
        int logoHalf = LogoRenderer.BLOCK_W / 2 + 2;
        boolean overLogo = mouseX >= logoCx - logoHalf && mouseX <= logoCx + logoHalf
                        && mouseY >= LOGO_TOP && mouseY <= logoBottom;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        lastInteractionMs = System.currentTimeMillis();
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
    // Easter egg: click en el logo -> expediente sellado aleatorio
    private static final String[] LOGO_EASTER_EGGS_ES = {
        "El reloj del ministerio acaba de marcar una hora que no existe.",
        "Una carta sin sello respira dentro del cajón del cronista.",
        "La ventana del tercer piso respondio con una luz verde.",
        "Las ratas dejaron una flecha perfecta apuntando al río.",
        "El nombre del último testigo apareció escrito antes de llegar.",
        "La campana conto a los ausentes, no a los vivos.",
        "Hay barro fresco bajo una puerta que nadie ha abierto.",
        "El gato del tejado pronunció un apellido y saltó a la niebla."
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
    private int easterEggIndex = -1;
    private boolean easterEggEnglish = true;
    private String cachedEasterEggMessage = null;
    private int cachedEasterElapsedChars = -1;
    private int cachedEasterBoxWidth = -1;
    private net.minecraft.client.gui.Font cachedEasterFont = null;
    private java.util.List<net.minecraft.util.FormattedCharSequence> cachedEasterLines = java.util.Collections.emptyList();
    private int cachedEasterCursorTextWidth = 0;
    private String cachedShootStarMessage;
    private int cachedShootStarMaxWidth = -1;
    private net.minecraft.client.gui.Font cachedShootStarFont;
    private java.util.List<net.minecraft.util.FormattedCharSequence> cachedShootStarLines = java.util.Collections.emptyList();
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





    /**
     * v0.177.0: la mesa se mide con la misma formula que usa el emblema para
     * escalarse. Antes se derivaba de leftColumn(), que en ventanas estrechas ya
     * no coincide con la mesa: el grupo se centra y el margen lateral no es su x.
     */
    private int estimateFirstButtonTop() {
        try {
            if (buttonTableTop() > 0) return buttonTableTop();
            return this.height - 96;
        } catch (Throwable ignored) {
            return this.height - 96;
        }
    }

    private void drawShootStarMessage(net.minecraft.client.gui.GuiGraphics g) {
        if (reducedMotion()) return;
        long untilMs = com.eternalcraft.gripeverde.client.render.AtmosphereRenderer.shootStarMessageUntilMs;
        long now = System.currentTimeMillis();
        if (now >= untilMs) return;

        com.eternalcraft.gripeverde.client.render.AtmosphereRenderer.refreshShootStarMessageLanguage();
        String ssm = com.eternalcraft.gripeverde.client.render.AtmosphereRenderer.shootStarMessage;
        if (ssm == null) return;

        long remain = untilMs - now;
        int alpha = remain < 800L ? (int)(220 * remain / 800L) : 220;
        int col = (alpha << 24) | 0xE8DCB4;
        int maxWidth = Math.max(40, this.width - 32);
        if (!ssm.equals(cachedShootStarMessage)
                || this.font != cachedShootStarFont
                || maxWidth != cachedShootStarMaxWidth) {
            cachedShootStarLines = DiaryTypography.split(this.font, Component.literal(ssm), maxWidth);
            cachedShootStarMessage = ssm;
            cachedShootStarFont = this.font;
            cachedShootStarMaxWidth = maxWidth;
        }
        java.util.List<net.minecraft.util.FormattedCharSequence> lines = cachedShootStarLines;
        int totalHeight = lines.size() * this.font.lineHeight;
        int top = Math.max(8, Math.min(this.height / 3, this.height - totalHeight - 8));
        for (int i = 0; i < lines.size(); i++) {
            net.minecraft.util.FormattedCharSequence line = lines.get(i);
            int x = Math.max(8, (this.width - DiaryTypography.width(this.font, line)) / 2);
            DiaryTypography.draw(g, this.font, line, x, top + i * this.font.lineHeight, col, false);
        }
    }
}
