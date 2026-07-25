package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.VictorianSkyline;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryMusicSlider;
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
    private static final int PANEL_H = 340;
    private static final int SLIDER_H = 20;

    private int panelX, panelY, panelW, panelH;
    private OptionsList reflectedList;
    private DiaryMusicSlider musicSlider;

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

            int listTop = this.panelY + 45;
            int listBottom = this.panelY + this.panelH - 75; // deja hueco para slider + divisor + boton
            this.reflectedList.updateSize(this.width, this.height, listTop, listBottom);
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
        int sliderW = this.panelW - 40;
        int sliderX = this.panelX + 20;
        int sliderY = this.panelY + this.panelH - 60;
        this.musicSlider = new DiaryMusicSlider(sliderX, sliderY, sliderW, SLIDER_H);
        this.addRenderableWidget(this.musicSlider);

        // Boton Volver estilo diario
        int btnW = 120;
        int btnH = 20;
        int btnX = (this.width - btnW) / 2;
        int btnY = this.panelY + this.panelH - 30;
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

    @Override
    public void tick() {
        super.tick();
        float delta = 1.0f / 20.0f;
        AtmosphereRenderer.tick(delta);
        VictorianSkyline.tick(delta);
        LivingAtmosphere.tick(delta);
        MenuMusic.tick();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // --- Escena viva atenuada ---
        AtmosphereRenderer.renderSky(g, this.width, this.height);
        VictorianSkyline.renderBackground(g, this.width, this.height);
        LivingAtmosphere.renderFogLow(g, this.width, this.height);
        VictorianSkyline.renderForeground(g, this.width, this.height);
        LivingAtmosphere.renderStreetLevel(g, this.width, this.height);
        LivingAtmosphere.renderAir(g, this.width, this.height);

        // --- Overlay oscuro ---
        g.fill(0, 0, this.width, this.height, DiaryChrome.OVERLAY_COL);

        // --- Pergamino ---
        DiaryChrome.drawPanelShadow(g, this.panelX, this.panelY, this.panelW, this.panelH);
        PaperTexture.drawClean(g, this.panelX, this.panelY, this.panelW, this.panelH);

        // --- Titulo ---
        String titleStr = Component.translatable("options.sounds.title").getString();
        int titleX = this.width / 2;
        int titleY = this.panelY + 10;
        {
        int _tw = this.font.width(titleStr);
        g.drawString(this.font, titleStr, titleX - _tw / 2 + 1, titleY + 1, DiaryChrome.SHADOW_COL, false);
        g.drawString(this.font, titleStr, titleX - _tw / 2,     titleY,     DiaryChrome.INK_TITLE,  false);
        }

        // --- Subtitulo ---
        String subtitle = "- " + Component.translatable("gripeverde.options.sound.subtitle").getString() + " -";
        int subWC = this.font.width(subtitle);
        g.drawString(this.font, subtitle, titleX - subWC / 2, titleY + 12, DiaryChrome.INK_SUBTITLE, false);

        // --- Flavor ---
        String flavor = Component.translatable("gripeverde.options.sound.flavor").getString();
        int flvWC = this.font.width(flavor);
        g.drawString(this.font, flavor, (this.width - flvWC) / 2, titleY + 24, DiaryChrome.INK_FLAVOR, false);

        // --- Divisor superior ---
        int divY1 = titleY + 42;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY1);

        // --- Lista vanilla + fades ---
        if (this.reflectedList != null) {
            this.reflectedList.render(g, mouseX, mouseY, partialTick);
            DiaryChrome.drawListFades(g, this.panelX + 6, this.panelX + this.panelW - 6,
                this.reflectedList.getTop(), this.reflectedList.getBottom(), 12);
            DiaryChrome.drawStyledScrollThumbInPanel(g,
                (AbstractSelectionList<?>) this.reflectedList, this.panelX + this.panelW);
        }

        // --- Divisor entre lista y slider ---
        int divY2 = this.panelY + this.panelH - 65;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);

        // --- Divisor inferior + esquineros ---
        int divY3 = this.panelY + this.panelH - 38;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY3);
        DiaryChrome.drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);

        // --- Slider + boton (renderables propios) ---
        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        // Bypass del fondo dirt vanilla
    }
}