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
        // Incrementar contador de aperturas (solo una vez por sesion)
        try {
            if (!openCountIncremented) {
                int cur = com.eternalcraft.gripeverde.config.ClientConfig.OPEN_COUNT.get();
                com.eternalcraft.gripeverde.config.ClientConfig.OPEN_COUNT.set(cur + 1);
                com.eternalcraft.gripeverde.config.ClientConfig.OPEN_COUNT.save();
                openCountIncremented = true;
            }
        } catch (Throwable ignored) {}
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
            Component.literal(pickRotatingSubtitle(MULTIPLAYER_SUBTITLES)),
            TextureAssets.ICON_BTN_MP,
            this::onMultiplayer
        ));

        this.addRenderableWidget(new GreenFluButton(
            btnX, btnY + BTN_H + Spacing.GAP_MEDIUM, BTN_W, BTN_H,
            Component.translatable("gripeverde.title.button.mods"),
            Component.literal(pickRotatingSubtitle(MODS_SUBTITLES)),
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
        // Boton >> (skip musica) queda solo (AMB removido, ahora en Sonidos)
        int skipY = this.height - 28;
        int skipX = 8;
        int skipW = 36;
        int skipH = 20;
        this.addRenderableWidget(new SlimButton(skipX, skipY, skipW, skipH,
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

        // Overlays interactivos (arriba de todo)
        drawEasterEggMessage(g);
        // Easter egg: relampago verde
        if (System.currentTimeMillis() < easterLightningUntilMs) {
            long rem = easterLightningUntilMs - System.currentTimeMillis();
            int a = (int)(50 * rem / 300L);
            if (a > 2) {
                g.fill(0, 0, this.width, this.height, (a << 24) | 0x3A5028);
            }
        }

        drawShootStarMessage(g);
    }

    @Override
    public void removed() {
        // MenuAmbience.stop(); -- REMOVIDO: ambiente persiste en subpantallas (como la musica)
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
        long now = System.currentTimeMillis();
        int shakeX = 0, shakeY = 0;
        if (now < skullShakeUntilMs) {
            long remain = skullShakeUntilMs - now;
            float intensity = (float) remain / 600f;
            shakeX = (int)(Math.sin(now / 40.0) * 3 * intensity);
            shakeY = (int)(Math.cos(now / 30.0) * 2 * intensity);
        }
        LogoRenderer.draw(g, this.width / 2 + shakeX, 30 + shakeY);

        // Renderizar particulas verdes cayendo
        if (now < eggParticlesUntilMs) {
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

    // Rotating signature pool, synced with LogoRenderer year via yearIndex
    private static final String[] SIGNATURE_POOL = {
        "Dr. S. Pereira",
        "Cronista R. Da Rosa",
        "Cap. G. Bandeira",
        "Prof. A. Focco"
    };

    private void drawSignature(GuiGraphics g) {
        int idx = com.eternalcraft.gripeverde.client.render.LogoRenderer.getYearIndex();
        String signature = SIGNATURE_POOL[idx % SIGNATURE_POOL.length];
        // Nombre viejo tachado: el anterior del pool
        int oldIdx = (idx + SIGNATURE_POOL.length - 1) % SIGNATURE_POOL.length;
        String oldSig = SIGNATURE_POOL[oldIdx];

        int sw = this.font.width(signature);
        int oldSw = this.font.width(oldSig);
        int y = this.height - 12;

        // ORDEN NUEVO: nombre nuevo a la izquierda, nombre viejo tachado a la derecha
        // Esto se lee mejor: "el nuevo cronista, el anterior tachado"

        // Firma nueva (izquierda) con temblor sutil
        double jitterX = Math.sin(System.currentTimeMillis() / 250.0) * 0.5
                       + Math.sin(System.currentTimeMillis() / 87.0) * 0.3;
        double jitterY = Math.cos(System.currentTimeMillis() / 310.0) * 0.4;
        int newX = this.width - sw - oldSw - 16 + (int)Math.round(jitterX);
        int newY = y + (int)Math.round(jitterY);
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

        // Mensaje del dia real (arriba de todo, alineado a la derecha del nombre viejo)
        String dayMsg = getDayMessage();
        if (!dayMsg.isEmpty()) {
            int dmw = this.font.width(dayMsg);
            int dmAlpha = com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(
                com.eternalcraft.gripeverde.client.render.DiaryTheme.SEPIA_LIGHT, 90);
            g.drawString(this.font, dayMsg,
                this.width - dmw - 6, y - 10, dmAlpha, false);
        }
    }

    private void onSingleplayer() { this.minecraft.setScreen(new SelectWorldScreen(this)); }
    private void onMultiplayer()  { this.minecraft.setScreen(new DiaryMultiplayerScreen(this)); }
    private void onMods()         { this.minecraft.setScreen(new ModListScreen(this)); }
    private void onOptions() { this.minecraft.setScreen(new DiaryOptionsScreen(this)); }

    @Override public boolean isPauseScreen()    { return false; }
    @Override public boolean shouldCloseOnEsc() { return false; }

    // Rotating subtitles for GreenFluButtons (multiplayer + mods)
    private static final String[] MULTIPLAYER_SUBTITLES = {
        "Luchen junto a sus hermanos",
        "El regimiento los espera",
        "Nadie sobrevive en soledad",
        "Que su sangre sea la ultima",
        "Marchen al frente"
    };
    private static final String[] MODS_SUBTITLES = {
        "Preparen su equipamiento",
        "Revisen los arsenales",
        "El acero no descansa",
        "Que instrumentos tenemos hoy",
        "Reservas de la ultima campana"
    };
    private static final java.util.Random SUB_RNG = new java.util.Random();

    // Cache: elegir subtitulo UNA vez por sesion (no reelegir en cada init)
    private static String cachedMultiSubtitle = null;
    private static String cachedModsSubtitle = null;

    private static String pickRotatingSubtitle(String[] pool) {
        if (pool == MULTIPLAYER_SUBTITLES) {
            if (cachedMultiSubtitle == null) {
                cachedMultiSubtitle = pool[SUB_RNG.nextInt(pool.length)];
            }
            return cachedMultiSubtitle;
        }
        if (pool == MODS_SUBTITLES) {
            if (cachedModsSubtitle == null) {
                cachedModsSubtitle = pool[SUB_RNG.nextInt(pool.length)];
            }
            return cachedModsSubtitle;
        }
        return pool[SUB_RNG.nextInt(pool.length)];
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
    // Easter egg: doble-click en el logo -> mensaje aleatorio
    private static final String[] LOGO_EASTER_EGGS = {
        "El cronista los observa.",
        "No abran mas puertas esta noche.",
        "Han golpeado tres veces.",
        "El gato del tejado sabe algo.",
        "La ventana del tercer piso vuelve a encenderse.",
        "Dicen que hoy la campana sono sola.",
        "Los faroles arden mas de lo habitual.",
        "El aire hoy sabe a metal."
    };
    private long lastLogoClickMs = 0L;
    private String easterEggMessage = null;
    private long easterEggUntilMs = 0L;
    private long easterFlashUntilMs = 0L;
    private long skullShakeUntilMs = 0L;
    private long easterLightningUntilMs = 0L;
    // Particulas verdes cayendo del logo tras easter egg
    private static final int EGG_PARTICLE_COUNT = 20;
    private final float[][] eggParticles = new float[EGG_PARTICLE_COUNT][5];  // x, y, vx, vy, alpha
    private long eggParticlesUntilMs = 0L;
    private static final java.util.Random EASTER_RNG = new java.util.Random();

    private void handleLogoClick() {
        long now = System.currentTimeMillis();
        if (now - lastLogoClickMs < 500L) {
            // Doble-click detectado
            easterEggMessage = LOGO_EASTER_EGGS[EASTER_RNG.nextInt(LOGO_EASTER_EGGS.length)];
            easterEggUntilMs = now + 5000L;
            skullShakeUntilMs = now + 600L;
            easterLightningUntilMs = now + 300L;
            // Forzar un relampago instantaneo en la atmosfera
            try {
                java.lang.reflect.Field flashField = com.eternalcraft.gripeverde.client.render.LivingAtmosphere.class.getDeclaredField("lightningFlash");
                flashField.setAccessible(true);
                flashField.setFloat(null, 1f);
            } catch (Throwable ignored) {}
            eggParticlesUntilMs = now + 2000L;
            // Inicializar particulas cayendo desde el skull
            int logoCx = this.width / 2;
            int logoCy = 45;
            for (int p = 0; p < EGG_PARTICLE_COUNT; p++) {
                eggParticles[p][0] = logoCx + (float)(Math.random() * 30 - 15);
                eggParticles[p][1] = logoCy + (float)(Math.random() * 10);
                eggParticles[p][2] = (float)(Math.random() * 2 - 1);   // vx
                eggParticles[p][3] = (float)(Math.random() * 1 + 1);   // vy (positivo = cae)
                eggParticles[p][4] = 1.0f;  // alpha
            }
            // Sonido + flash coordinado
            try {
                com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                    com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_CROWS.get(),
                    0.6f, 1.2f);
            } catch (Throwable ignored) {}
            easterFlashUntilMs = now + 400L;
        }
        lastLogoClickMs = now;
    }

    private void drawEasterEggMessage(net.minecraft.client.gui.GuiGraphics g) {
        long now = System.currentTimeMillis();

        // Flash amber breve al momento del doble-click
        if (now < easterFlashUntilMs) {
            long remainFlash = easterFlashUntilMs - now;
            int flashAlpha = (int)(80 * remainFlash / 400L);
            if (flashAlpha > 2) {
                g.fill(0, 0, this.width, this.height,
                    (flashAlpha << 24) | 0xD4941E);
            }
        }

        if (easterEggMessage == null || now >= easterEggUntilMs) return;
        // Fade out en los ultimos 1000ms
        long remain = easterEggUntilMs - now;
        int alpha = remain < 1000L ? (int)(255 * remain / 1000L) : 255;
        int color = (alpha << 24) | 0xD4941E;
        int shadowCol = (alpha << 24) | 0x000000;

        // Texto escalado adaptativo (menos en escalas altas para no salirse)
        int w = this.font.width(easterEggMessage);
        float eeScale = this.width < 500 ? 1.0f : (this.width < 800 ? 1.3f : 1.8f);
        int cx = this.width / 2;
        int cy = this.height / 2;
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().scale(eeScale, eeScale, 1f);
        g.drawString(this.font, easterEggMessage, -w / 2 + 1, 1, shadowCol, false);
        g.drawString(this.font, easterEggMessage, -w / 2, 0, color, false);
        g.pose().popPose();
    }

    // Cache del mensaje del dia: solo se recalcula si cambio la fecha
    private static String cachedDayMsg = null;
    private static java.time.LocalDate cachedDayMsgDate = null;

    /** Mensaje del cronista segun el dia real de la semana + mes especial (cacheado). */
    private static String getDayMessage() {
        java.time.LocalDate today = java.time.LocalDate.now();
        if (cachedDayMsg != null && today.equals(cachedDayMsgDate)) {
            return cachedDayMsg;
        }
        cachedDayMsgDate = today;
        int month = today.getMonthValue();
        int dow = today.getDayOfWeek().getValue();

        // Mensajes especiales por mes
        String monthBonus = "";
        if (month == 10) monthBonus = " ~ los muertos caminan";
        else if (month == 12) monthBonus = " ~ noche de Adviento";
        else if (month == 1) monthBonus = " ~ ano nuevo, misma peste";

        String base;
        switch (dow) {
            case 1: base = "Lunes ~ otra semana en cuarentena"; break;
            case 2: base = "Martes ~ dia de lluvia fina"; break;
            case 3: base = "Miercoles ~ sin correspondencia"; break;
            case 4: base = "Jueves ~ el mercado cerro"; break;
            case 5: base = "Viernes ~ que Dios se apiade"; break;
            case 6: base = "Sabado ~ descanso obligado"; break;
            case 7: base = "Domingo ~ las campanas suenan solas"; break;
            default: base = "";
        }
        cachedDayMsg = base + monthBonus;
        return cachedDayMsg;
    }


    private void drawShootStarMessage(net.minecraft.client.gui.GuiGraphics g) {
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
        g.drawString(this.font, ssm, cx + 1, cy + 1, shadowCol, false);
        g.drawString(this.font, ssm, cx, cy, col, false);
    }
}
