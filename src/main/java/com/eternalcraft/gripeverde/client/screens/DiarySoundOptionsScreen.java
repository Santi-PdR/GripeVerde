package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.lore.Discoveries;
import com.eternalcraft.gripeverde.client.render.ExpedientHeader;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiarySlider;
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
 *  - Se agrega DiarySlider (musica) como widget flotante debajo de la lista.
 *  - Bypass total de super.render(): dibujamos escena viva + pergamino + cosmetica
 *    + list.render() manual + slider + boton Volver propio.
 *
 * Ver ADDENDUM v0.11.6 en CONTEXTO.md.
 */
public class DiarySoundOptionsScreen extends SoundOptionsScreen {

    private static final Logger LOG = LogUtils.getLogger();

    private static final int PANEL_W = 540;
    private static final int PANEL_H = 392;
    private static final int SLIDER_H = 20;
    private static final int RAIL_W = 156;

    private int panelX, panelY, panelW, panelH;
    private OptionsList reflectedList;
    private final SmoothScroll smoothScroll = new SmoothScroll();
    private DiarySlider musicSlider;
    private DiarySlider ambienceSlider;
    private long openedAtMs = 0L;

    public DiarySoundOptionsScreen(Screen parent, Options options) {
        super(parent, options);
    }

    @Override
    protected void init() {
        super.init();

        this.panelW = Math.min(PANEL_W, this.width - 40);
        this.panelH = Math.min(PANEL_H, this.height - 30);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        // Reflection acotada: obtener y estilizar la OptionsList privada
        this.reflectedList = findListField();
        if (this.reflectedList != null) {
            this.reflectedList.setRenderBackground(false);
            this.reflectedList.setRenderTopAndBottom(false);

            int listTop = this.panelY + 58;
            int listBottom = this.panelY + this.panelH - 100; // deja hueco para 2 sliders + divisor + boton
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

        // Slider propio de musica del mod, debajo de la lista / fuera del carril lateral
        int sliderW = this.panelW - RAIL_W - 58;
        int sliderX = this.panelX + 22;
        int sliderY = this.panelY + this.panelH - 82;
        this.musicSlider = new DiarySlider(sliderX, sliderY, sliderW, SLIDER_H,
            MenuMusic.getUserVolume(),
            v -> {
                int pct = (int) Math.round(v * 100.0);
                return Component.translatable("gripeverde.options.sound.music_slider").getString()
                    + ": " + pct + "%";
            },
            v -> MenuMusic.setUserVolume((float) v));
        this.addRenderableWidget(this.musicSlider);

        // Slider de ambiente: mas pequeno, debajo del de musica
        int ambSliderH = 14;
        int ambSliderW = sliderW * 3 / 4;  // 75% del ancho del principal
        int ambSliderX = sliderX + (sliderW - ambSliderW) / 2;
        int ambSliderY = sliderY + SLIDER_H + 2;
        this.ambienceSlider = new DiarySlider(ambSliderX, ambSliderY, ambSliderW, ambSliderH,
            com.eternalcraft.gripeverde.client.audio.MenuAmbience.getUserVolume(),
            v -> {
                int pct = (int) Math.round(v * 100.0);
                return Component.translatable("gripeverde.options.sound.ambience_slider").getString()
                    + ": " + pct + "%";
            },
            v -> com.eternalcraft.gripeverde.client.audio.MenuAmbience.setUserVolume((float) v));
        this.addRenderableWidget(this.ambienceSlider);

        // Boton Volver estilo diario
        int btnW = 132;
        int btnH = 20;
        int btnX = sliderX + (sliderW - btnW) / 2;
        int btnY = this.panelY + this.panelH - 30;
        this.addRenderableWidget(new DiaryButton(
            btnX, btnY, btnW, btnH,
            Component.translatable("gripeverde.options.button.done"),
            DiaryButton.Icon.DOOR,
            DiaryButton.Variant.PRIMARY,
            () -> this.onClose()
        ));
        this.openedAtMs = System.currentTimeMillis();
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

    @Override
    public void tick() {
        super.tick();
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
        this.smoothScroll.tick(this.reflectedList);

        // Si el gramofono queda del todo en cero y el lector se queda, algo late.
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

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0xFF000000);
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);

        float reveal = DiaryChrome.reveal(this.openedAtMs, 260L);
        DiaryChrome.renderPanelBase(g, this.panelX, this.panelY, this.panelW, this.panelH, DiaryTheme.AMBER_FLAME, PaperTexture.PaperKind.LEDGER);

        ExpedientHeader.draw(g, this.font, this.panelX, this.panelY, this.panelW, Component.translatable("options.sounds.title"), reveal);







        if (this.reflectedList != null) {
            if (reveal < 0.995f) {
                int revealBottom = this.panelY + Math.max(28, (int)(this.panelH * reveal));
                g.enableScissor(this.panelX - 2, this.panelY - 2, this.panelX + this.panelW + 2, revealBottom);
                this.reflectedList.render(g, mouseX, mouseY, partialTick);
                DiaryChrome.drawStyledScrollThumbInPanel(g, (AbstractSelectionList<?>) this.reflectedList, this.panelX + this.panelW - 26);
                g.disableScissor();
            } else {
                this.reflectedList.render(g, mouseX, mouseY, partialTick);
                DiaryChrome.drawStyledScrollThumbInPanel(g, (AbstractSelectionList<?>) this.reflectedList, this.panelX + this.panelW - 26);
            }
        }

        int divY2 = this.panelY + this.panelH - 88;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);
        int divY3 = this.panelY + this.panelH - 38;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY3);

        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }

        try {
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, this.panelY - 12);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {}
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        // Bypass del fondo dirt vanilla
    }
}
