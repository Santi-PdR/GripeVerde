package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
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
    // v0.266.0: alto real de las fichas y linea del pie, para el recuento.
    private int gridBtnH = BTN_H;
    private int footerDivY = -1;

    public DiarySkinCustomizationScreen(Screen parent, Options options) {
        super(Component.translatable("options.skinCustomisation"));
        this.parent = parent;
        this.options = options;
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
        int panelW = Math.min(PANEL_MAX_W, this.width - 40);
        int panelH = Math.min(PANEL_MAX_H, this.height - 30);
        this.panelX = (this.width  - panelW) / 2;
        this.panelY = (this.height - panelH) / 2;

        // v0.266.0: la rejilla se mide contra la hoja. En ventanas bajas las
        // cuatro filas se salian del papel y tapaban el pie y el boton de
        // volver; ahora el alto de cada ficha y el hueco se ajustan para que
        // entren siempre.
        int rows = 4;
        int gridTop = this.panelY + 74;  // titulo + subtitulo + flavor + divisor
        int footerH = 46;
        int gapY = BTN_GAP_Y;
        int btnH = BTN_H;
        int avail = panelH - 74 - footerH;
        int need = rows * btnH + (rows - 1) * gapY;
        if (need > avail) {
            gapY = 3;
            btnH = Math.max(14, (avail - (rows - 1) * gapY) / rows);
            int used = rows * btnH + (rows - 1) * gapY;
            gridTop = this.panelY + Math.max(54, 74 + Math.max(0, (avail - used) / 2));
        }
        this.gridBtnH = btnH;
        int availableGridW = Math.max(BTN_W, panelW - 28);
        int effectiveBtnW = Math.min(BTN_W, Math.max(110, (availableGridW - BTN_GAP_X) / 2));
        int col1X   = this.panelX + (panelW - (effectiveBtnW * 2 + BTN_GAP_X)) / 2;
        int col2X   = col1X + effectiveBtnW + BTN_GAP_X;

        PlayerModelPart[] parts = PlayerModelPart.values();

        int idx = 0;
        for (PlayerModelPart part : parts) {
            int row = idx / 2;
            int col = idx % 2;
            int x = (col == 0) ? col1X : col2X;
            int y = gridTop + row * (btnH + gapY);

            DiaryButton btn = new DiaryButton(
                x, y, effectiveBtnW, btnH,
                buildPartLabel(part),
                Icon.NONE, Variant.SECONDARY,
                () -> togglePart(part)
            );
            // v0.266.0: cada prenda dice en su aviso que hace.
            btn.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.literal(part.getName().getString() + "  ·  "
                    + (com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                        ? "shown or hidden in the world"
                        : "se ve o se oculta en el mundo"))));
            this.partButtons.put(part, btn);
            this.addRenderableWidget(btn);
            idx++;
        }

        // Boton de mano principal
        int row = idx / 2;
        int col = idx % 2;
        int armX = (col == 0) ? col1X : col2X;
        int armY = gridTop + row * (btnH + gapY);
        this.armButton = new DiaryButton(
            armX, armY, effectiveBtnW, btnH,
            buildArmLabel(),
            Icon.NONE, Variant.SECONDARY,
            this::toggleArm
        );
        this.armButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
            Component.literal(com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                ? "which hand holds the tool" : "que mano lleva la herramienta")));
        this.addRenderableWidget(this.armButton);

        // Boton Volver
        int btnW = 160;
        int btnX = (this.width - btnW) / 2;
        int btnY = this.panelY + panelH - 28;
        this.addRenderableWidget(new DiaryButton(
            btnX, btnY, btnW, BTN_H,
            Component.translatable("gripeverde.options.button.done"),
            Icon.DOOR, Variant.PRIMARY,
            this::onClose
        ));
        this.footerDivY = this.panelY + panelH - 38;
    }

    /**
     * v0.266.0: "5 de 7 prendas puestas". Se cuenta lo que el jugador lleva
     * encima con la misma lista de partes que usa el juego.
     */
    private void drawWornCount(GuiGraphics g, int panelW, int divY) {
        int worn = 0;
        PlayerModelPart[] parts = PlayerModelPart.values();
        for (PlayerModelPart part : parts) {
            if (this.options.isModelPartEnabled(part)) worn++;
        }
        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        String text = en ? worn + " of " + parts.length + " parts worn"
            : worn + " de " + parts.length + " prendas puestas";
        String cut = DiaryChrome.fitText(this.font, text, Math.max(60, panelW - 40));
        int w = DiaryChrome.widthCached(this.font, cut);
        g.drawString(this.font, cut, this.panelX + (panelW - w) / 2, divY - 11,
            DiaryChrome.INK_FLAVOR, false);
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
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        // Escena viva
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(gfx, this.width, this.height);

        // Pergamino
        int panelW = Math.min(PANEL_MAX_W, this.width - 40);
        int panelH = Math.min(PANEL_MAX_H, this.height - 30);
        DiaryChrome.drawPanelShadow(gfx, this.panelX, this.panelY, panelW, panelH);
        PaperTexture.drawTornSheet(gfx, this.panelX, this.panelY, panelW, panelH, PaperTexture.PaperKind.MEDICAL);

        // Titulo
        int cx = this.width / 2;
        int titleY = this.panelY + 12;
        {
            String titleText = DiaryChrome.fitCached(this.font, this.title.getString(), Math.max(0, panelW - 28));
            int tw = DiaryChrome.widthCached(this.font, titleText);
            gfx.drawString(this.font, titleText, cx - tw / 2,     titleY,     DiaryChrome.INK_TITLE,  false);
        }

        // Subtitulo
        int subtitleY = titleY + 12;
        {
            String subC = "- " + DiaryChrome.tr("gripeverde.options.skin.subtitle") + " -";
            subC = DiaryChrome.fitCached(this.font, subC, Math.max(0, panelW - 28));
            int sw = DiaryChrome.widthCached(this.font, subC);
            gfx.drawString(this.font, subC, cx - sw / 2, subtitleY, DiaryChrome.INK_SUBTITLE, false);
        }

        // Flavor
        int flavorY = subtitleY + 13;
        {
            String flvC = DiaryChrome.fitCached(this.font,
                DiaryChrome.tr("gripeverde.options.skin.flavor"), Math.max(0, panelW - 28));
            int fw = DiaryChrome.widthCached(this.font, flvC);
            gfx.drawString(this.font, flvC, cx - fw / 2, flavorY, DiaryChrome.INK_FLAVOR, false);
        }

        // Divisor superior
        int divY1 = flavorY + 12;
        DiaryChrome.drawInkDivider(gfx, this.panelX, divY1, panelW);

        // Widgets (DiaryButtons propios)
        super.render(gfx, mouseX, mouseY, delta);

        // Divisor inferior + esquineros
        int divY2 = this.footerDivY > 0 ? this.footerDivY : this.panelY + panelH - 38;
        DiaryChrome.drawInkDivider(gfx, this.panelX, divY2, panelW);

        // v0.266.0: cuantas prendas van puestas, sobre el pie.
        drawWornCount(gfx, panelW, divY2);
        DiaryChrome.drawCornerOrnaments(gfx, this.panelX, this.panelY, panelW, panelH);
        DiaryChrome.drawRegistryTagLeft(gfx, this.panelX, this.panelY, "REG. VIII");

        // v0.240.0: fuera el espejo de la esquina (el "ojo"): la ficha de
        // aspecto queda con sus botones y nada mas.
        try {
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(gfx, this.width / 2, this.panelY - 12);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(gfx, this.width);
        } catch (Throwable ignored) {}
    }

    @Override
    public void renderBackground(GuiGraphics gfx) {
        // suprimido: la escena viva hace de fondo
    }

    @Override
    public void onClose() {
        // v0.264.0: salir de una hoja suena a puerta, como el resto de la casa.
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.back();
        } catch (Throwable ignored) {
        }
        this.minecraft.setScreen(this.parent);
    }
}
