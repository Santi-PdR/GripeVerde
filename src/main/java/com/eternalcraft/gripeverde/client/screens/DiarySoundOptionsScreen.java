package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryMusicSlider;
import com.eternalcraft.gripeverde.client.widgets.DiaryAmbienceSlider;
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
    private boolean compactLayout;
    private OptionsList reflectedList;
    private final SmoothScroll smoothScroll = new SmoothScroll();
    private DiaryMusicSlider musicSlider;
    private DiaryAmbienceSlider ambienceSlider;

    public DiarySoundOptionsScreen(Screen parent, Options options) {
        super(parent, options);
    }

    @Override
    protected void init() {
        super.init();

        this.panelW = Math.max(1, Math.min(PANEL_W, this.width - 40));
        this.panelH = Math.max(1, Math.min(PANEL_H, this.height - 30));
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;
        this.compactLayout = this.panelH < 300;

        // Reflection acotada: obtener y estilizar la OptionsList privada
        this.reflectedList = findListField();
        if (this.reflectedList != null) {
            this.reflectedList.setRenderBackground(false);
            this.reflectedList.setRenderTopAndBottom(false);

            int listTop = this.panelY + (this.compactLayout ? 38 : 58);
            int listBottom = Math.max(listTop + 1, this.panelY + this.panelH
                - (this.compactLayout ? 84 : 100)); // reserva sliders y boton sin colapsar la lista
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
        int sliderY = this.panelY + this.panelH - (this.compactLayout ? 76 : 82);
        this.musicSlider = new DiaryMusicSlider(sliderX, sliderY, sliderW, SLIDER_H);
        this.addRenderableWidget(this.musicSlider);

        // Slider de ambiente: mas pequeno, debajo del de musica
        int ambSliderH = 14;
        int ambSliderW = Math.max(1, sliderW * 3 / 4);  // 75% del ancho del principal
        int ambSliderX = sliderX + (sliderW - ambSliderW) / 2;
        int ambSliderY = sliderY + SLIDER_H + 2;
        this.ambienceSlider = new DiaryAmbienceSlider(ambSliderX, ambSliderY, ambSliderW, ambSliderH);
        this.addRenderableWidget(this.ambienceSlider);

        // Boton Volver estilo diario
        int btnW = Math.max(1, Math.min(120, this.panelW - 24));
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

    // v0.16.0: el silencio escucha
    private long silenceStartMs = 0L;
    private boolean silenceHeard = false;

    // v0.24.0: el diapason del gabinete (secreto al margen del regulador de ambiente)
    private int forkHitX, forkHitY, forkHitW, forkHitH;
    private boolean forkHitboxValid;
    private long forkRingUntilMs = 0L;

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
        // --- Escena viva atenuada ---
        // Clear framebuffer + transition fade
        g.fill(0, 0, this.width, this.height, 0xFF000000);
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
                // --- Overlay oscuro ---

        // --- Pergamino ---
        DiaryChrome.drawPanelShadow(g, this.panelX, this.panelY, this.panelW, this.panelH);
        PaperTexture.drawClean(g, this.panelX, this.panelY, this.panelW, this.panelH, PaperTexture.PaperKind.LEDGER);

        // --- Titulo ---
        int titleX = this.width / 2;
        int titleY = this.panelY + 10;
        com.eternalcraft.gripeverde.client.render.DiaryTypography.drawCentered(
            g, this.font, Component.translatable("options.sounds.title"),
            titleX, titleY, DiaryChrome.INK_TITLE);

        // --- Subtitulo ---
        String subtitle = "- " + Component.translatable("gripeverde.options.sound.subtitle").getString() + " -";
        int subWC = this.font.width(subtitle);
        g.drawString(this.font, subtitle, titleX - subWC / 2, titleY + 12, DiaryChrome.INK_SUBTITLE, false);

        // --- Flavor ---
        // v0.30.0: solo si hay aire real sobre los sliders; jamas pisado,
        // jamas al medio. Si el panel es bajo, se omite.
        String flavor = Component.translatable("gripeverde.options.sound.flavor").getString();
        int sliderTop = this.panelY + this.panelH - 82;
        if (!this.compactLayout && titleY + 24 + this.font.lineHeight <= sliderTop - 6) {
            String flavorShown = this.font.width(flavor) <= this.panelW - 32 ? flavor
                : this.font.plainSubstrByWidth(flavor, Math.max(0, this.panelW - 32 - this.font.width("..."))) + "...";
            int flvWC = this.font.width(flavorShown);
            g.drawString(this.font, flavorShown, (this.width - flvWC) / 2, titleY + 24, DiaryChrome.INK_FLAVOR, false);
        }

        // --- Divisor superior ---
        int divY1 = this.panelY + (this.compactLayout ? 32 : 52);
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY1);

        // --- Lista vanilla sin gradientes blancos ni bandejas extra ---
        if (this.reflectedList != null) {
            this.reflectedList.render(g, mouseX, mouseY, partialTick);
            DiaryChrome.drawStyledScrollThumbInPanel(g,
                (AbstractSelectionList<?>) this.reflectedList, this.panelX + this.panelW);
        }

        // --- Divisor entre lista y slider ---
        int divY2 = this.panelY + this.panelH - (this.compactLayout ? 82 : 88);
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);

        // --- Divisor inferior + esquineros ---
        int divY3 = this.panelY + this.panelH - 38;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY3);
        DiaryChrome.drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);
        DiaryChrome.drawRegistryTag(g, this.panelX, this.panelY, this.panelW, this.panelH, "REG. II");

        // --- Slider + boton (renderables propios) ---
        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
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

    /** v0.24.0: un diapason al margen; pulsado, afina la sala entera. */
    private void drawTuningFork(GuiGraphics g, int mouseX, int mouseY) {
        if (this.ambienceSlider == null) { this.forkHitboxValid = false; return; }
        int fx = this.ambienceSlider.getX() - 16;
        int fy = this.ambienceSlider.getY() + this.ambienceSlider.getHeight() / 2;
        long now = System.currentTimeMillis();
        boolean reducedMotion = com.eternalcraft.gripeverde.client.util.ClientAccessibility.reducedMotion();
        boolean hot = mouseX >= fx - 4 && mouseX <= fx + 5 && mouseY >= fy - 8 && mouseY <= fy + 5;
        boolean ringing = now < this.forkRingUntilMs;
        if (ringing && !reducedMotion) fx += (int) Math.round(Math.sin((this.forkRingUntilMs - now) * 0.55) * 1.0);
        int metal = (hot || ringing) ? 0xFFD8B56A : 0xFF8A6A3D;
        int dimm  = (hot || ringing) ? 0xFF6B4E22 : 0xFF54401F;
        int spread = (hot || ringing) ? 1 : 0;
        g.fill(fx - 1, fy - 1, fx + 1, fy + 4, metal);                   // mango
        g.fill(fx - 2 - spread, fy - 6, fx - 1 - spread, fy - 1, metal); // brazo izquierdo
        g.fill(fx + 1 + spread, fy - 6, fx + 2 + spread, fy - 1, metal); // brazo derecho
        g.fill(fx - 2 - spread, fy - 6, fx - 1 - spread, fy - 5, dimm);  // puntas
        g.fill(fx + 1 + spread, fy - 6, fx + 2 + spread, fy - 5, dimm);
        if (ringing) {
            float rr = reducedMotion ? 0.65f : (this.forkRingUntilMs - now) / 700f; // 1 -> 0
            int ra = reducedMotion ? 78 : (int)(120 * rr);
            int rad = reducedMotion ? 6 : 3 + (int)((1f - rr) * 7f);
            int rc = (ra << 24) | 0xD8B56A;
            g.fill(fx - rad, fy - 2 - rad, fx + rad, fy - 1 - rad, rc);
            g.fill(fx - rad, fy - 1 + rad, fx + rad, fy + rad, rc);
            g.fill(fx - rad, fy - 2 - rad, fx - rad + 1, fy + rad, rc);
            g.fill(fx + rad - 1, fy - 2 - rad, fx + rad, fy + rad, rc);
        }
        this.forkHitX = fx - 6;
        this.forkHitY = fy - 9;
        this.forkHitW = 14;
        this.forkHitH = 17;
        this.forkHitboxValid = true;
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
        if (button == 0 && this.forkHitboxValid
                && mouseX >= this.forkHitX && mouseX <= this.forkHitX + this.forkHitW
                && mouseY >= this.forkHitY && mouseY <= this.forkHitY + this.forkHitH) {
            ringTuningFork();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        // Bypass del fondo dirt vanilla
    }
}
