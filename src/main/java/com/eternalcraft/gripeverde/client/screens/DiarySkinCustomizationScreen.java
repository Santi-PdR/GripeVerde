package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.VictorianSkyline;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton.Icon;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton.Variant;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;

import java.util.EnumMap;
import java.util.Map;

public class DiarySkinCustomizationScreen extends Screen {

    // 6 partes del cuerpo + 1 mano principal = 7 toggles en grid 2x4
    private static final int PANEL_MAX_W = 340;
    private static final int PANEL_MAX_H = 235;

    private static final int BTN_W = 152;
    private static final int BTN_H = 20;
    private static final int BTN_GAP_X = 8;
    private static final int BTN_GAP_Y = 6;

    private final Screen parent;
    private final Options options;

    private final Map<PlayerModelPart, DiaryButton> partButtons = new EnumMap<>(PlayerModelPart.class);
    private DiaryButton armButton;

    private int panelX, panelY;

    public DiarySkinCustomizationScreen(Screen parent, Options options) {
        super(Component.translatable("options.skinCustomisation"));
        this.parent = parent;
        this.options = options;
    }

    @Override
    protected void init() {
        int panelW = Math.min(PANEL_MAX_W, this.width - 40);
        int panelH = Math.min(PANEL_MAX_H, this.height - 30);
        this.panelX = (this.width  - panelW) / 2;
        this.panelY = (this.height - panelH) / 2;

        int gridTop = this.panelY + 74;  // titulo + subtitulo + flavor + divisor
        int col1X   = this.panelX + (PANEL_MAX_W - (BTN_W * 2 + BTN_GAP_X)) / 2;
        int col2X   = col1X + BTN_W + BTN_GAP_X;

        PlayerModelPart[] parts = PlayerModelPart.values();

        int idx = 0;
        for (PlayerModelPart part : parts) {
            int row = idx / 2;
            int col = idx % 2;
            int x = (col == 0) ? col1X : col2X;
            int y = gridTop + row * (BTN_H + BTN_GAP_Y);

            DiaryButton btn = new DiaryButton(
                x, y, BTN_W, BTN_H,
                buildPartLabel(part),
                Icon.NONE, Variant.SECONDARY,
                () -> togglePart(part)
            );
            this.partButtons.put(part, btn);
            this.addRenderableWidget(btn);
            idx++;
        }

        // Boton de mano principal
        int row = idx / 2;
        int col = idx % 2;
        int armX = (col == 0) ? col1X : col2X;
        int armY = gridTop + row * (BTN_H + BTN_GAP_Y);
        this.armButton = new DiaryButton(
            armX, armY, BTN_W, BTN_H,
            buildArmLabel(),
            Icon.NONE, Variant.SECONDARY,
            this::toggleArm
        );
        this.addRenderableWidget(this.armButton);

        // Boton Volver
        int btnW = 160;
        int btnX = (this.width - btnW) / 2;
        int btnY = this.panelY + PANEL_MAX_H - 28;
        this.addRenderableWidget(new DiaryButton(
            btnX, btnY, btnW, BTN_H,
            Component.translatable("gripeverde.options.button.done"),
            Icon.DOOR, Variant.PRIMARY,
            this::onClose
        ));
    }

    private Component buildPartLabel(PlayerModelPart part) {
        boolean enabled = this.options.isModelPartEnabled(part);
        return part.getName()
            .copy()
            .append(": ")
            .append(CommonComponents.optionStatus(enabled));
    }

    private Component buildArmLabel() {
        HumanoidArm arm = this.options.mainHand().get();
        return Component.translatable("options.mainHand")
            .copy()
            .append(": ")
            .append(arm.getCaption());
    }

    private void togglePart(PlayerModelPart part) {
        this.options.toggleModelPart(part, !this.options.isModelPartEnabled(part));
        this.options.save();
        DiaryButton btn = this.partButtons.get(part);
        if (btn != null) {
            btn.setMessage(buildPartLabel(part));
        }
    }

    private void toggleArm() {
        HumanoidArm current = this.options.mainHand().get();
        HumanoidArm next = (current == HumanoidArm.LEFT) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        this.options.mainHand().set(next);
        this.options.save();
        this.armButton.setMessage(buildArmLabel());
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
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        // Escena viva
        AtmosphereRenderer.renderSky(gfx, this.width, this.height);
        VictorianSkyline.renderBackground(gfx, this.width, this.height);
        VictorianSkyline.renderForeground(gfx, this.width, this.height);

        // Overlay
        gfx.fill(0, 0, this.width, this.height, DiaryChrome.OVERLAY_COL);

        // Pergamino
        int panelW = Math.min(PANEL_MAX_W, this.width - 40);
        int panelH = Math.min(PANEL_MAX_H, this.height - 30);
        DiaryChrome.drawPanelShadow(gfx, this.panelX, this.panelY, panelW, panelH);
        PaperTexture.drawClean(gfx, this.panelX, this.panelY, panelW, panelH);

        // Titulo
        int cx = this.width / 2;
        int titleY = this.panelY + 12;
        {
            int tw = this.font.width(this.title);
            gfx.drawString(this.font, this.title, cx - tw / 2 + 1, titleY + 1, DiaryChrome.SHADOW_COL, false);
            gfx.drawString(this.font, this.title, cx - tw / 2,     titleY,     DiaryChrome.INK_TITLE,  false);
        }

        // Subtitulo
        int subtitleY = titleY + 12;
        {
            String subC = "- " + Component.translatable("gripeverde.options.skin.subtitle").getString() + " -";
            int sw = this.font.width(subC);
            gfx.drawString(this.font, subC, cx - sw / 2, subtitleY, DiaryChrome.INK_SUBTITLE, false);
        }

        // Flavor
        int flavorY = subtitleY + 13;
        {
            Component flvC = Component.translatable("gripeverde.options.skin.flavor");
            int fw = this.font.width(flvC);
            gfx.drawString(this.font, flvC, cx - fw / 2, flavorY, DiaryChrome.INK_FLAVOR, false);
        }

        // Divisor superior
        int divY1 = flavorY + 12;
        DiaryChrome.drawInkDivider(gfx, this.panelX, divY1, panelW);

        // Widgets (DiaryButtons propios)
        super.render(gfx, mouseX, mouseY, delta);

        // Divisor inferior + esquineros
        int divY2 = this.panelY + PANEL_MAX_H - 38;
        DiaryChrome.drawInkDivider(gfx, this.panelX, divY2, panelW);
        DiaryChrome.drawCornerOrnaments(gfx, this.panelX, this.panelY, panelW, panelH);
    }

    @Override
    public void renderBackground(GuiGraphics gfx) {
        // suprimido: la escena viva hace de fondo
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}