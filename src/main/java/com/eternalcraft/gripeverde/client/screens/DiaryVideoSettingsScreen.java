package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.ExpedientHeader;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryProgressBar;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.lang.reflect.Field;

/**
 * Version Diary de VideoSettingsScreen.
 * Mantiene la lista vanilla intacta y solo reorganiza su presentacion.
 */
public class DiaryVideoSettingsScreen extends VideoSettingsScreen {

    private static final Logger LOG = LogUtils.getLogger();
    private static final int PANEL_W = 540;
    private static final int PANEL_H = 390;
    private static final int RAIL_W = 156;

    private final SmoothScroll smoothScroll = new SmoothScroll();
    private final Options ownOptions;
    private OptionsList reflectedList;
    private int panelX, panelY, panelW, panelH;
    private long openedAtMs = 0L;

    public DiaryVideoSettingsScreen(Screen parent, Options options) {
        super(parent, options);
        this.ownOptions = options;
    }

    @Override
    protected void init() {
        super.init();
        this.panelW = Math.min(PANEL_W, this.width - 32);
        this.panelH = Math.min(PANEL_H, this.height - 28);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        this.reflectedList = findListField();
        if (this.reflectedList != null) {
            this.reflectedList.setRenderBackground(false);
            this.reflectedList.setRenderTopAndBottom(false);
            int listTop = this.panelY + 66;
            int listBottom = this.panelY + this.panelH - 54;
            this.reflectedList.updateSize(this.width, this.height, listTop, listBottom);
            this.smoothScroll.snapToCurrent(this.reflectedList);
        }

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

        int btnW = 132;
        int btnH = 20;
        int btnX = this.panelX + ((this.panelW - RAIL_W - 40) - btnW) / 2 + 20;
        int btnY = this.panelY + this.panelH - 30;
        this.addRenderableWidget(new DiaryButton(btnX, btnY, btnW, btnH,
            Component.translatable("gripeverde.options.button.done"),
            DiaryButton.Icon.DOOR, DiaryButton.Variant.PRIMARY,
            this::onClose));
        this.openedAtMs = System.currentTimeMillis();
    }

    private OptionsList findListField() {
        Class<?> cls = VideoSettingsScreen.class;
        while (cls != null) {
            try {
                for (Field f : cls.getDeclaredFields()) {
                    if (OptionsList.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        Object val = f.get(this);
                        if (val instanceof OptionsList ol) return ol;
                    }
                }
            } catch (Throwable ignored) {}
            cls = cls.getSuperclass();
        }
        LOG.warn("[DiaryVideo] no se encontro OptionsList en VideoSettingsScreen");
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1f / 20f);
        this.smoothScroll.tick(this.reflectedList);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.reflectedList != null && mouseY >= this.reflectedList.getTop() && mouseY <= this.reflectedList.getBottom()) {
            if (this.smoothScroll.wheel(this.reflectedList, delta, 34.0D)) {
                com.eternalcraft.gripeverde.client.audio.UiSounds.scroll();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
        float reveal = DiaryChrome.reveal(this.openedAtMs, 240L);
        DiaryChrome.renderPanelBase(g, this.panelX, this.panelY, this.panelW, this.panelH, DiaryChrome.STEEL_MID, PaperTexture.PaperKind.TECHNICAL);

        int headerBottom = ExpedientHeader.draw(g, this.font, this.panelX, this.panelY, this.panelW, Component.translatable("options.videoTitle"), reveal);

        int railX = this.panelX + this.panelW - RAIL_W - 18;
        // v0.21.4: el aparato se agrupa por secciones (render / calidad / rendimiento)





        if (this.panelH >= 330) {
            int perfY = headerBottom + 182;

            DiaryProgressBar.draw(g, railX + 10, perfY + 46, RAIL_W - 20, 14,
                normalizedFps(), com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish() ? "Pacing" : "Ritmo", DiaryProgressBar.Style.PLAGUE);
        }

        int listRight = railX - 8;

        if (this.reflectedList != null) {
            if (reveal < 0.995f) {
                int revealBottom = this.panelY + Math.max(28, (int)(this.panelH * reveal));
                g.enableScissor(this.panelX - 2, this.panelY - 2, this.panelX + this.panelW + 2, revealBottom);
                this.reflectedList.render(g, mouseX, mouseY, partialTick);
                DiaryChrome.drawStyledScrollThumbInPanel(g, (AbstractSelectionList<?>) this.reflectedList, listRight);
                g.disableScissor();
            } else {
                this.reflectedList.render(g, mouseX, mouseY, partialTick);
                DiaryChrome.drawStyledScrollThumbInPanel(g, (AbstractSelectionList<?>) this.reflectedList, listRight);
            }
        }

        int divY = this.panelY + this.panelH - 38;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY);
        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }
    }

    /** v0.21.4: medidor de rendimiento (FPS del cliente, defensivo). */
    private float normalizedFps() {
        try {
            int fps = this.minecraft.getFps();
            return Math.max(0f, Math.min(1f, fps / 60f));
        } catch (Throwable ignored) {
            return 0.5f;
        }
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        // bypass dirt
    }
}
