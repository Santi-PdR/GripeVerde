package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.audio.MenuAmbience;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import com.eternalcraft.gripeverde.client.util.MotionPolicy;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryMusicSlider;
import com.eternalcraft.gripeverde.client.widgets.DiaryAmbienceSlider;
import com.eternalcraft.gripeverde.client.widgets.DiarySlider;
import com.eternalcraft.gripeverde.config.ClientConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.lang.reflect.Field;

/**
 * Subclase visual de SoundOptionsScreen sobre pergamino.
 * Estrategia B-reflection acotada (v0.11.6):
 *  - Se hereda para preservar hooks de mods externos (ej: IMP que inyecta su slider
 *    via ScreenEvent.Init.Post sobre SoundOptionsScreen).
 *  - super.init() se ejecuta normalmente: crea la OptionsList vanilla con todas las filas.
 *  - Reflection acotada sobre el campo privado 'list' para desactivarle fondo y bordes.
 *    Fallback silencioso: si falla, la lista se ve con fondo negro pero funciona.
 *  - Se agrega DiaryMusicSlider como widget flotante debajo de la lista.
 *  - Bypass total de super.render(): dibujamos escena viva + pergamino + cosmetica
 *    + list.render() manual + slider + boton Volver propio.
 *
 * Ver ADDENDUM v0.11.6 en CONTEXTO.md.
 */
public class DiarySoundOptionsScreen extends SoundOptionsScreen {

    private static final Logger LOG = LogUtils.getLogger();

    private static final int PANEL_W = 340;
    private static final int PANEL_H = 380;
    private static final int SLIDER_H = 20;

    private int panelX, panelY, panelW, panelH;
    private OptionsList reflectedList;
    private final SmoothScroll smoothScroll = new SmoothScroll();
    private DiaryMusicSlider musicSlider;
    private DiaryButton skipTrackButton;
    private DiaryButton musicToggleButton;
    private DiaryAmbienceSlider ambienceSlider;
    private DiaryButton ambienceToggleButton;
    private DiarySlider uiVolumeSlider;
    private DiaryButton testUiSoundButton;
    private DiaryButton testAmbienceSoundButton;

    public DiarySoundOptionsScreen(Screen parent, Options options) {
        super(parent, options);
    }

    /** Keeps the options list and sound controls inside short viewports. */
    private boolean compactLayout() {
        return this.panelH < 280;
    }

    private int listTopY() {
        return this.panelY + (compactLayout() ? 34 : 58);
    }

    private int listBottomY() {
        int desiredBottom = this.panelY + this.panelH - (compactLayout() ? 86 : 118);
        return Math.max(listTopY() + 1, desiredBottom);
    }

    private int musicSliderY() {
        // v0.364.0: los mandos de la casa llevan etiqueta arriba y carril abajo,
        // asi que piden 16-20 px de alto. La fila arranca mas arriba para que
        // las tres quepan sin tocar el boton de volver.
        return this.panelY + this.panelH - (compactLayout() ? 78 : 108);
    }

    private int musicSliderHeight() {
        return compactLayout() ? 16 : 20;
    }

    private int ambienceSliderY() {
        return musicSliderY() + musicSliderHeight() + (compactLayout() ? 1 : 2);
    }

    private int ambienceSliderHeight() {
        return compactLayout() ? 16 : 20;
    }

    private int uiSliderY() {
        return ambienceSliderY() + ambienceSliderHeight() + (compactLayout() ? 1 : 2);
    }

    /** v0.336.0: hay aire para la frase del disco sobre los mandos. */
    private boolean flavorFits() {
        int titleY = this.panelY + (compactLayout() ? 4 : 10);
        int flavorY = titleY + (compactLayout() ? 11 : 13);
        return flavorY + this.font.lineHeight <= musicSliderY() - 4;
    }

    private int returnButtonY() {
        return this.panelY + this.panelH - (compactLayout() ? 21 : 30);
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
        super.init();

        this.panelW = Math.max(1, Math.min(PANEL_W, this.width - 24));
        this.panelH = Math.max(120, Math.min(PANEL_H, this.height - 16));
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = Math.max(0, (this.height - this.panelH) / 2);

        // Reflection acotada: obtener y estilizar la OptionsList privada
        this.reflectedList = findListField();
        if (this.reflectedList != null) {
            this.reflectedList.setRenderBackground(false);
            this.reflectedList.setRenderTopAndBottom(false);

            int listTop = listTopY();
            int listBottom = listBottomY();
            this.reflectedList.updateSize(this.width, this.height, listTop, listBottom);
            this.smoothScroll.snapToCurrent(this.reflectedList);
        }

        // Ocultar boton Done vanilla (SoundOptionsScreen lo agrega en init con lambda)
        for (GuiEventListener child : this.children()) {
            if (child instanceof Button btn) {
                Component msg = btn.getMessage();
                if (msg != null && msg.getString().equals(CommonComponents.GUI_DONE.getString())) {
                    btn.setX(-9999);
                    btn.setY(-9999);
                    btn.visible = false;
                    btn.active = false;
                    break;
                }
            }
        }

        // Slider propio de musica del mod, debajo de la lista
        int sliderW = Math.max(1, this.panelW - 40);
        int sliderX = this.panelX + 20;
        int sliderY = musicSliderY();
        int musicHeight = musicSliderHeight();
        int toggleTrackW = sliderW >= 100 ? Math.min(46, Math.max(38, sliderW / 6)) : 0;
        int skipTrackW = sliderW >= 160 ? Math.min(44, Math.max(32, sliderW / 7)) : 0;
        int musicControlGap = toggleTrackW > 0 ? 4 : 0;
        int skipTrackGap = skipTrackW > 0 ? 4 : 0;
        int musicSliderW = Math.max(1, sliderW - toggleTrackW - skipTrackW - musicControlGap - skipTrackGap);
        this.musicSlider = new DiaryMusicSlider(sliderX, sliderY, musicSliderW, musicHeight);
        this.musicSlider.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.translatable("gripeverde.options.sound.music_slider.tooltip")));
        this.addRenderableWidget(this.musicSlider);

        int musicControlX = sliderX + musicSliderW + musicControlGap;
        if (toggleTrackW > 0) {
            this.musicToggleButton = new DiaryButton(
                musicControlX, sliderY, toggleTrackW, musicHeight,
                musicToggleLabel(), DiaryButton.Icon.NONE, DiaryButton.Variant.SECONDARY,
                DiaryButton.SoundProfile.DEFAULT, this::toggleMenuMusic);
            refreshMenuMusicToggleButton();
            this.addRenderableWidget(this.musicToggleButton);
            musicControlX += toggleTrackW + skipTrackGap;
        }
        if (skipTrackW > 0) {
            this.skipTrackButton = new DiaryButton(
                musicControlX, sliderY, skipTrackW, musicHeight,
                Component.translatable("gripeverde.options.sound.next_track_button"),
                DiaryButton.Icon.NONE, DiaryButton.Variant.SECONDARY,
                DiaryButton.SoundProfile.DEFAULT, MenuMusic::skipToNext);
            this.skipTrackButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.translatable("gripeverde.options.sound.next_track_button.tooltip")));
            this.skipTrackButton.active = MenuMusic.isActive() && !MenuMusic.isCrossfading();
            this.addRenderableWidget(this.skipTrackButton);
        }

        // El ambiente y los efectos SFX quedan separados para ajustar cada capa.
        int compactH = ambienceSliderHeight();
        int ambSliderY = ambienceSliderY();
        int ambTestW = sliderW >= 180 ? Math.min(48, Math.max(40, sliderW / 8)) : 0;
        int ambToggleW = Math.min(82, Math.max(1, (sliderW - (ambTestW > 0 ? ambTestW + 8 : 0)) / 3));
        int ambGap = ambTestW > 0 ? 4 : Math.min(4, Math.max(0, sliderW - ambToggleW - 1));
        int ambTestGap = ambTestW > 0 ? 4 : 0;
        int ambSliderW = Math.max(1, sliderW - ambToggleW - ambTestW - ambGap - ambTestGap);
        int ambSliderX = sliderX;
        this.ambienceSlider = new DiaryAmbienceSlider(ambSliderX, ambSliderY, ambSliderW, compactH);
        this.ambienceSlider.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.translatable("gripeverde.options.sound.ambience_slider.tooltip")));
        this.addRenderableWidget(this.ambienceSlider);

        this.ambienceToggleButton = new DiaryButton(
            ambSliderX + ambSliderW + ambGap, ambSliderY, ambToggleW, compactH,
            ambienceToggleLabel(), DiaryButton.Icon.NONE, DiaryButton.Variant.SECONDARY,
            DiaryButton.SoundProfile.DEFAULT, () -> {
                MenuAmbience.toggleMute();
                refreshAmbienceToggleButton();
            }
        );
        refreshAmbienceToggleButton();
        this.addRenderableWidget(this.ambienceToggleButton);
        if (ambTestW > 0) {
            this.testAmbienceSoundButton = new DiaryButton(
                ambSliderX + ambSliderW + ambGap + ambToggleW + ambTestGap, ambSliderY,
                ambTestW, compactH, Component.translatable("gripeverde.options.sound.test_button"),
                DiaryButton.Icon.NONE, DiaryButton.Variant.SECONDARY,
                DiaryButton.SoundProfile.NONE, MenuAmbience::testSound);
            this.testAmbienceSoundButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.translatable("gripeverde.options.sound.ambience_test.tooltip")));
            this.testAmbienceSoundButton.active =
                !com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.hasActiveWorld();
            this.addRenderableWidget(this.testAmbienceSoundButton);
        }

        int uiSliderY = uiSliderY();
        int testButtonW = sliderW < 48
            ? Math.max(1, sliderW / 2)
            : Math.min(58, Math.max(24, sliderW / 3));
        int buttonGap = Math.min(6, Math.max(0, sliderW - testButtonW - 1));
        int uiSliderW = Math.max(1, sliderW - testButtonW - buttonGap);
        this.uiVolumeSlider = new DiarySlider(
            sliderX, uiSliderY, uiSliderW, compactH,
            ClientConfig.UI_VOLUME.get(),
            value -> DiaryChrome.tr("gripeverde.options.sound.ui_slider")
                + ": " + (int) Math.round(value * 100.0) + "%",
            // v0.363.0: a tiempo real: el volumen de interfaz se mueve mientras
            // arrastras, para poder oirlo con el boton de prueba.
            value -> ClientConfig.UI_VOLUME.set(value),
            true,
            // v0.358.0: se guarda al soltar el mando, una sola vez.
            () -> ClientConfig.UI_VOLUME.save()
        );
        this.uiVolumeSlider.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.translatable("gripeverde.options.sound.ui_slider.tooltip")));
        this.addRenderableWidget(this.uiVolumeSlider);

        this.testUiSoundButton = new DiaryButton(
            sliderX + uiSliderW + buttonGap, uiSliderY, testButtonW, compactH,
            Component.translatable("gripeverde.options.sound.test_button"),
            DiaryButton.Icon.NONE, DiaryButton.Variant.SECONDARY,
            DiaryButton.SoundProfile.NONE, com.eternalcraft.gripeverde.client.audio.UiSounds::testSound
        );
        this.testUiSoundButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.translatable("gripeverde.options.sound.test_button.tooltip")));
        this.addRenderableWidget(this.testUiSoundButton);

        // Boton Volver estilo diario
        boolean compact = compactLayout();
        int btnW = Math.max(1, Math.min(120, this.panelW - 24));
        int btnH = compact ? 18 : 20;
        int btnX = this.panelX + (this.panelW - btnW) / 2;
        int btnY = returnButtonY();
        this.addRenderableWidget(new DiaryButton(
            btnX, btnY, btnW, btnH,
            Component.translatable("gripeverde.options.button.done"),
            DiaryButton.Icon.DOOR,
            DiaryButton.Variant.PRIMARY,
            () -> this.onClose()
        ));
    }

    /**
     * Reflection defensiva sobre SoundOptionsScreen.list (private).
     * Estrategia: nombre oficial (dev) -> nombre SRG real (runtime) -> scan por tipo.
     * SRG real 1.20.1: f_244146_ (extraido de forge-1.20.1-srg.jar).
     * Ver R-SRG-EXTRACT, R-SRG-RUNTIME.
     */
    private OptionsList findListField() {
        // Intento 1 y 2: por nombre conocido
        String[] candidates = { "list", "f_244146_" };
        for (String name : candidates) {
            try {
                Field f = SoundOptionsScreen.class.getDeclaredField(name);
                f.setAccessible(true);
                Object val = f.get(this);
                if (val instanceof OptionsList ol) {
                    LOG.info("[DiarySound] list resuelto via campo '{}'", name);
                    return ol;
                }
            } catch (Throwable ignored) {}
        }
        // Intento 3 (fallback): scan por tipo. Robusto ante futuros renames.
        try {
            for (Field f : SoundOptionsScreen.class.getDeclaredFields()) {
                if (f.getType().equals(OptionsList.class)) {
                    f.setAccessible(true);
                    Object val = f.get(this);
                    if (val instanceof OptionsList ol) {
                        LOG.info("[DiarySound] list resuelto por scan de tipo, campo '{}'", f.getName());
                        return ol;
                    }
                }
            }
        } catch (Throwable ignored) {}
        LOG.warn("[DiarySound] no se pudo resolver campo 'list' por ningun metodo; lista mantendra fondo vanilla");
        return null;
    }

    // v0.16.0: el silencio escucha
    private long silenceStartMs = 0L;
    private boolean silenceHeard = false;

    // v0.24.0: el diapason del gabinete (secreto al margen del regulador de ambiente)
    private int[] forkRect = null;
    private long forkRingUntilMs = 0L;

    @Override
    public void tick() {
        super.tick();
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
        this.smoothScroll.tick(this.reflectedList);

        // Si el gramofono queda del todo en cero y el lector se queda, algo late.
        if (com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.hasActiveWorld()) {
            // The record-room easter egg belongs to the title menu, never to a live session.
            silenceStartMs = 0L;
            return;
        }
        try {
            if (!silenceHeard && com.eternalcraft.gripeverde.client.audio.MenuMusic.getUserVolume() <= 0.004f) {
                long now = System.currentTimeMillis();
                if (silenceStartMs == 0L) silenceStartMs = now;
                if (now - silenceStartMs >= 3000L) {
                    silenceHeard = true;
                    boolean first = com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                        com.eternalcraft.gripeverde.client.lore.Discoveries.SILENCE_LISTENS);
                    if (first) {
                        com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                            com.eternalcraft.gripeverde.client.lore.Marginalia.N_SILENCE);
                    }
                    // Un latido grave, tres veces.
                    for (int i = 0; i < 3; i++) {
                        com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                            com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_CHAIN.get(),
                            0.10f, 0.34f + i * 0.02f);
                    }
                }
            } else if (!silenceHeard) {
                silenceStartMs = 0L;
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.reflectedList != null
                && mouseY >= this.reflectedList.getTop() && mouseY <= this.reflectedList.getBottom()) {
            if (this.smoothScroll.wheel(this.reflectedList, delta, 34.0D)) {
                com.eternalcraft.gripeverde.client.audio.UiSounds.scroll();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private static String percentText(double value) {
        return Math.round(Math.max(0.0, Math.min(1.0, value)) * 100.0) + "%";
    }

    private Component ambienceToggleLabel() {
        return Component.translatable(MenuAmbience.isMuted()
            ? "gripeverde.options.sound.ambience_off"
            : "gripeverde.options.sound.ambience_on");
    }

    private Component musicToggleLabel() {
        return Component.translatable(MenuMusic.isMusicEnabled()
            ? "gripeverde.options.sound.music_toggle.pause"
            : "gripeverde.options.sound.music_toggle.resume");
    }

    private void toggleMenuMusic() {
        boolean enabled = !MenuMusic.isMusicEnabled();
        ClientConfig.MENU_MUSIC_ENABLED.set(enabled);
        ClientConfig.MENU_MUSIC_ENABLED.save();
        if (enabled) MenuMusic.start();
        else MenuMusic.stop();
        refreshMenuMusicToggleButton();
    }

    private void refreshMenuMusicToggleButton() {
        if (this.musicToggleButton == null) return;
        this.musicToggleButton.setMessage(musicToggleLabel());
        this.musicToggleButton.active =
            !com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.hasActiveWorld();
    }

    private void refreshAmbienceToggleButton() {
        if (this.ambienceToggleButton == null) return;
        boolean muted = MenuAmbience.isMuted();
        this.ambienceToggleButton.setMessage(ambienceToggleLabel());
        this.ambienceToggleButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.translatable(muted
                ? "gripeverde.options.sound.ambience_off.tooltip"
                : "gripeverde.options.sound.ambience_on.tooltip")));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        refreshMenuMusicToggleButton();
        if (this.skipTrackButton != null) {
            this.skipTrackButton.active = MenuMusic.isActive() && !MenuMusic.isCrossfading()
                && !com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.hasActiveWorld();
        }
        if (this.testAmbienceSoundButton != null) {
            this.testAmbienceSoundButton.active =
                !com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.hasActiveWorld();
        }
        // Shared background renderer draws the menu scene or a fixed live-world veil.
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);

        // --- Pergamino ---
        DiaryChrome.drawPanelShadow(g, this.panelX, this.panelY, this.panelW, this.panelH);
        PaperTexture.drawTornSheet(g, this.panelX, this.panelY, this.panelW, this.panelH, PaperTexture.PaperKind.LEDGER);

        // --- Titulo ---
        String titleStr = DiaryChrome.fitCached(this.font,
            DiaryChrome.tr("options.sounds.title"), Math.max(0, this.panelW - 28));
        int titleX = this.width / 2;
        boolean compact = compactLayout();
        int titleY = this.panelY + (compact ? 4 : 10);
        {
        int _tw = DiaryChrome.widthCached(this.font, titleStr);
        g.drawString(this.font, titleStr, titleX - _tw / 2,     titleY,     DiaryChrome.INK_TITLE,  false);
        }

        // v0.336.0: el disco suelto solo se escribe si el flavor no cabe; si
        // cabe, la frase ya lo dice y las dos lineas se pisaban.
        try {
            if (!flavorFits() && MenuMusic.isMusicEnabled() && MenuMusic.isActive()) {
                String track = (com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                    ? "playing: " : "suena: ")
                    + MenuMusic.getCurrentTrackNumber() + "/" + MenuMusic.getTrackCount();
                // v0.301.0: el disco sonando se pegaba a la chapa REG. II. Va
                // a la izquierda, con su ancho medido para no llegar a la chapa.
                String cut = DiaryChrome.fitCached(this.font, track,
                    Math.max(40, this.panelW - 150));
                g.drawString(this.font, cut, this.panelX + 16, titleY + 12,
                    DiaryChrome.INK_FLAVOR, false);
            }
        } catch (Throwable ignored) {
        }

        // v0.184.0: fuera los renglones "Niveles: ..." y "Niveles de Minecraft: ...".
        // El usuario no los quiere; queda el titulo y, si hay aire, el disco sonando.

        // --- Flavor ---
        // v0.30.0: solo si hay aire real sobre los sliders; jamas pisado,
        // jamas al medio. Si el panel es bajo, se omite.
        int currentTrack = MenuMusic.getCurrentTrackNumber();
        String flavor;
        if (currentTrack > 0) {
            flavor = Component.translatable(
                MenuMusic.isCrossfading()
                    ? "gripeverde.options.sound.track_transition"
                    : "gripeverde.options.sound.track_now_playing",
                currentTrack, MenuMusic.getTrackCount()).getString();
        } else {
            flavor = Component.translatable(MenuMusic.isActive()
                ? "gripeverde.options.sound.track_between"
                : "gripeverde.options.sound.track_paused").getString();
        }
        int sliderTop = musicSliderY();
        int flavorY = titleY + (compact ? 11 : 13);
        if (flavorFits()) {
            String flavorShown = DiaryChrome.fitCached(this.font, flavor, Math.max(0, this.panelW - 32));
            int flvWC = DiaryChrome.widthCached(this.font, flavorShown);
            g.drawString(this.font, flavorShown, (this.width - flvWC) / 2, flavorY, DiaryChrome.INK_FLAVOR, false);
        }

        // --- Divisor superior ---
        int divY1 = compact ? this.panelY + 28 : titleY + 32;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY1);

        // --- Lista vanilla sin gradientes blancos ni bandejas extra ---
        // v0.352.0: la lista se recorta a la hoja. El juego la dimensiona a lo
        // ancho de la pantalla y sus filas asomaban fuera del papel, como
        // manchas grises sobre la ciudad.
        boolean listClipped = false;
        if (this.reflectedList != null) {
            try {
                g.enableScissor(this.panelX + 2, this.panelY + 2,
                    this.panelX + this.panelW - 2, this.panelY + this.panelH - 2);
                listClipped = true;
            } catch (Throwable ignored) {
            }
            this.reflectedList.render(g, mouseX, mouseY, partialTick);
            // v0.343.0: el gris del juego se acompana al sepia de la hoja.
            try {
                DiaryChrome.washVanillaRows(g, (AbstractSelectionList<?>) this.reflectedList);
                // v0.344.0: y encima, la ficha de registro con nuestra letra.
                DiaryChrome.paintOptionRowsInk(g, this.font,
                    (AbstractSelectionList<?>) this.reflectedList);
            } catch (Throwable ignored) {
            }
            DiaryChrome.drawStyledScrollThumbInPanel(g,
                (AbstractSelectionList<?>) this.reflectedList, this.panelX + this.panelW);
            if (listClipped) {
                try {
                    g.disableScissor();
                } catch (Throwable ignored) {
                }
            }
        }

        // --- Divisor entre lista y slider ---
        int divY2 = compact ? musicSliderY() - 4 : this.panelY + this.panelH - 114;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);

        // --- Divisor inferior + esquineros ---
        // v0.364.0: con los mandos de 20 px el filete del pie caia sobre el
        // ultimo carril; baja a la franja libre entre el mando y la ficha.
        int divY3 = compact ? returnButtonY() - 2 : this.panelY + this.panelH - 42;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY3);
        DiaryChrome.drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);
        // v0.267.0: la hoja de sonido tambien lleva su chapa.
        DiaryChrome.drawRegistryTag(g, this.panelX, this.panelY, this.panelW, this.panelH, "REG. II");

        // The soundtrack controls stay static in a live world; only enabled state is refreshed.
        if (this.skipTrackButton != null) {
            this.skipTrackButton.active = MenuMusic.isActive() && !MenuMusic.isCrossfading();
        }

        // --- Slider + boton (renderables propios) ---
        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }

        // v0.285.0: las tres capas, medidas y dichas; y el folio al pie.
        try {
            boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
            int musicPct = (int) Math.round(
                com.eternalcraft.gripeverde.config.ClientConfig.MUSIC_VOLUME.get() * 100.0);
            int ambPct = (int) Math.round(
                com.eternalcraft.gripeverde.config.ClientConfig.AMBIENCE_VOLUME.get() * 100.0);
            int uiPct = (int) Math.round(
                com.eternalcraft.gripeverde.config.ClientConfig.UI_VOLUME.get() * 100.0);
            boolean musicOff = !MenuMusic.isMusicEnabled() || musicPct <= 0;
            boolean ambOff = MenuAmbience.isMuted() || ambPct <= 0;
            String line;
            // v0.377.0: el pie se cortaba con puntos suspensivos. Ahora se
            // prueban tres formas -larga, media y corta- y se usa la primera
            // que entra entera: siempre se lee completo.
            String lineMid;
            String lineShort;
            String lineTiny;
            if (musicOff && ambOff && uiPct <= 0) {
                line = en ? "the cabinet is silent" : "el gabinete esta en silencio";
                lineMid = line;
                lineShort = line;
                lineTiny = en ? "silent" : "silencio";
            } else {
                line = (en ? "music " : "musica ")
                    + (menuMusicOffLabel(musicOff, musicPct, en))
                    + (en ? "  ·  ambience " : "  ·  ambiente ")
                    + (menuAmbienceOffLabel(ambOff, ambPct, en))
                    + (en ? "  ·  ui " : "  ·  interfaz ") + uiPct + "%";
                lineMid = (en ? "mus " : "mus ")
                    + (menuMusicOffLabel(musicOff, musicPct, en))
                    + (en ? " · amb " : " · amb ")
                    + (menuAmbienceOffLabel(ambOff, ambPct, en))
                    + (en ? " · ui " : " · ui ") + uiPct + "%";
                lineShort = (en ? "mus " : "mus ")
                    + (musicOff ? "-" : musicPct + "%")
                    + (en ? " · amb " : " · amb ")
                    + (ambOff ? "-" : ambPct + "%")
                    + " · ui " + uiPct + "%";
                // v0.380.0: cuarta forma, solo cifras. Cuando el pie se queda
                // sin sitio se usa esta antes que recortar con puntos.
                lineTiny = (musicOff ? "-" : musicPct + "%")
                    + " · " + (ambOff ? "-" : ambPct + "%")
                    + " · " + uiPct + "%";
            }
            // v0.301.0: la linea de las tres capas caia sobre las ruedas y el
            // boton de volver. Ahora comparte el pie con el folio, y se recorta
            // para no llegar a el.
            String folio = en ? "sheet II  ·  Esc back" : "hoja II  ·  Esc vuelve";
            int fw = DiaryChrome.widthCached(this.font, folio);
            // v0.350.0: el pie compartia linea con Volver, pero a doce pixeles
            // del borde, sobre el canto roto. Sube seis y sigue a los lados del
            // boton, que vive entre panelH-30 y panelH-10.
            int footY = this.panelY + this.panelH - 18;
            int footRight = this.panelX + this.panelW - 14;
            // v0.336.0: el pie comparte la linea con el boton Volver, que vive
            // centrado. El resumen se escribe a su izquierda y el folio a su
            // derecha, cada uno con el ancho que de verdad le queda.
            boolean roomy = !compactLayout() && this.panelW >= 250;
            boolean folioSolo = !compactLayout() && this.panelH >= 190;
            int btW = Math.max(1, Math.min(120, this.panelW - 24));
            int btL = this.panelX + (this.panelW - btW) / 2;
            int btR = btL + btW;
            if (roomy) {
                int leftLimit = (btL - 8) - (this.panelX + 14);
                if (leftLimit >= 60) {
                    String cut = fitWhole(line, lineMid, lineShort, lineTiny, leftLimit);
                    g.drawString(this.font, cut, this.panelX + 14, footY,
                        DiaryChrome.INK_FLAVOR, false);
                }
                int rightLimit = footRight - (btR + 8);
                if (rightLimit >= fw) {
                    g.drawString(this.font, folio, footRight - fw, footY,
                        DiaryChrome.INK_FLAVOR, false);
                }
            } else if (folioSolo && footRight - fw > btR + 8) {
                g.drawString(this.font, folio, footRight - fw, footY,
                    DiaryChrome.INK_FLAVOR, false);
            }
        } catch (Throwable ignored) {
        }

        // v0.24.0: el diapason guarda el tono de la sala
        try {
            drawTuningFork(g, mouseX, mouseY);
        } catch (Throwable ignored) {}

        // v0.16.0
        try {
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, this.panelY - 12);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {}
    }

    /**
     * v0.377.0: la primera de las formas que entra entera en el ancho dado.
     * v0.380.0: hay una cuarta forma, de solo cifras, y si tampoco cabe no se
     * escribe nada: el pie del Sonido no se recorta nunca con puntos
     * suspensivos, que era lo que se veia como "musica 40% · amb 100% · ui 6...".
     */
    private String fitWhole(String longest, String middle, String shortest, String tiny, int limit) {
        if (limit <= 0) return "";
        if (DiaryChrome.widthCached(this.font, longest) <= limit) return longest;
        if (middle != null && DiaryChrome.widthCached(this.font, middle) <= limit) return middle;
        if (shortest != null && DiaryChrome.widthCached(this.font, shortest) <= limit) return shortest;
        if (tiny != null && DiaryChrome.widthCached(this.font, tiny) <= limit) return tiny;
        return "";
    }

    /** v0.285.0: "40%" o "en silencio", segun lo que haya. */
    private String menuMusicOffLabel(boolean off, int pct, boolean en) {
        if (off) return en ? "silent" : "en silencio";
        return pct + "%";
    }

    private String menuAmbienceOffLabel(boolean off, int pct, boolean en) {
        if (off) return en ? "silent" : "en silencio";
        return pct + "%";
    }

    /** v0.24.0: un diapason al margen; pulsado, afina la sala entera. */
    private void drawTuningFork(GuiGraphics g, int mouseX, int mouseY) {
        if (this.ambienceSlider == null) { this.forkRect = null; return; }
        int fx = this.ambienceSlider.getX() - 16;
        int fy = this.ambienceSlider.getY() + this.ambienceSlider.getHeight() / 2;
        long now = System.currentTimeMillis();
        boolean hot = mouseX >= fx - 4 && mouseX <= fx + 5 && mouseY >= fy - 8 && mouseY <= fy + 5;
        boolean ringing = now < this.forkRingUntilMs;
        if (ringing && !MotionPolicy.isReduced())
            fx += (int) Math.round(Math.sin((this.forkRingUntilMs - now) * 0.55) * 1.0);
        int metal = (hot || ringing) ? 0xFFD8B56A : 0xFF8A6A3D;
        int dimm  = (hot || ringing) ? 0xFF6B4E22 : 0xFF54401F;
        int spread = (hot || ringing) ? 1 : 0;
        g.fill(fx - 1, fy - 1, fx + 1, fy + 4, metal);                   // mango
        g.fill(fx - 2 - spread, fy - 6, fx - 1 - spread, fy - 1, metal); // brazo izquierdo
        g.fill(fx + 1 + spread, fy - 6, fx + 2 + spread, fy - 1, metal); // brazo derecho
        g.fill(fx - 2 - spread, fy - 6, fx - 1 - spread, fy - 5, dimm);  // puntas
        g.fill(fx + 1 + spread, fy - 6, fx + 2 + spread, fy - 5, dimm);
        if (ringing) {
            float rr = (this.forkRingUntilMs - now) / 700f; // 1 -> 0
            int ra = (int)(120 * rr);
            int rad = 3 + (int)((1f - rr) * 7f);
            int rc = (ra << 24) | 0xD8B56A;
            g.fill(fx - rad, fy - 2 - rad, fx + rad, fy - 1 - rad, rc);
            g.fill(fx - rad, fy - 1 + rad, fx + rad, fy + rad, rc);
            g.fill(fx - rad, fy - 2 - rad, fx - rad + 1, fy + rad, rc);
            g.fill(fx + rad - 1, fy - 2 - rad, fx + rad, fy + rad, rc);
        }
        this.forkRect = new int[]{ fx - 6, fy - 9, 14, 17 };
    }

    /** v0.24.0: la prueba de tono del gabinete. */
    private void ringTuningFork() {
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                com.eternalcraft.gripeverde.client.audio.ModSounds.UI_SUCCESS.get(), 0.55f, 0.62f);
            com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                com.eternalcraft.gripeverde.client.audio.ModSounds.UI_SUCCESS.get(), 0.28f, 0.94f);
        } catch (Throwable ignored) {}
        try {
            boolean first = com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                com.eternalcraft.gripeverde.client.lore.Discoveries.SOUND_KEEPER);
            if (first) {
                com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                    com.eternalcraft.gripeverde.client.lore.Marginalia.N_TUNING);
            }
        } catch (Throwable ignored) {}
        this.forkRingUntilMs = System.currentTimeMillis() + 700L;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.forkRect != null
                && mouseX >= this.forkRect[0] && mouseX <= this.forkRect[0] + this.forkRect[2]
                && mouseY >= this.forkRect[1] && mouseY <= this.forkRect[1] + this.forkRect[3]) {
            ringTuningFork();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        // Bypass del fondo dirt vanilla
    }

    @Override
    public void onClose() {
        // v0.267.0: cerrar la hoja de sonido suena a puerta, como el resto.
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.back();
        } catch (Throwable ignored) {
        }
        super.onClose();
    }
}
