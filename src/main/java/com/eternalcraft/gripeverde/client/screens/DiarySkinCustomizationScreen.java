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

    // v0.16.0: el espejo de la niebla
    private long mirrorViewUntilMs = 0L;
    private long mirrorViewStartMs = 0L;

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
            int y = gridTop + row * (BTN_H + BTN_GAP_Y);

            DiaryButton btn = new DiaryButton(
                x, y, effectiveBtnW, BTN_H,
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
            armX, armY, effectiveBtnW, BTN_H,
            buildArmLabel(),
            Icon.NONE, Variant.SECONDARY,
            this::toggleArm
        );
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
        PaperTexture.drawClean(gfx, this.panelX, this.panelY, panelW, panelH, PaperTexture.PaperKind.MEDICAL);

        // Titulo
        int cx = this.width / 2;
        int titleY = this.panelY + 12;
        {
            int tw = this.font.width(this.title);
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
        int divY2 = this.panelY + panelH - 38;
        DiaryChrome.drawInkDivider(gfx, this.panelX, divY2, panelW);
        DiaryChrome.drawCornerOrnaments(gfx, this.panelX, this.panelY, panelW, panelH);

        // v0.16.0: el espejo pequeno, arriba a la derecha del panel
        try {
            drawMirror(gfx, mouseX, mouseY);
            long now = System.currentTimeMillis();
            if (now < mirrorViewUntilMs) {
                // Asi te ve la niebla: silueta sin detalles
                float t = (now - mirrorViewStartMs) / 3000f;
                int a = 190;
                if (now - mirrorViewStartMs < 400L) a = (int)(190 * (now - mirrorViewStartMs) / 400L);
                else if (mirrorViewUntilMs - now < 500L) a = (int)(190 * (mirrorViewUntilMs - now) / 500L);
                gfx.fill(0, 0, this.width, this.height, (Math.min(210, a + 20) << 24) | 0x0A0C08);
                int mx = this.width / 2;
                int my = this.height / 2;
                int sil = (a << 24) | 0x1E2A1E;
                gfx.fill(mx - 6, my - 30, mx + 6, my - 16, sil);      // cabeza
                gfx.fill(mx - 10, my - 16, mx + 10, my + 14, sil);    // cuerpo
                gfx.fill(mx - 14, my - 14, mx - 10, my + 4, sil);     // brazo
                gfx.fill(mx + 10, my - 14, mx + 14, my + 4, sil);
                gfx.fill(mx - 8, my + 14, mx - 3, my + 34, sil);      // piernas
                gfx.fill(mx + 3, my + 14, mx + 8, my + 34, sil);
                // Dos ojos verdes apenas
                int eye = (Math.min(255, a + 40) << 24) | 0x5A7038;
                gfx.fill(mx - 3, my - 25, mx - 2, my - 24, eye);
                gfx.fill(mx + 2, my - 25, mx + 3, my - 24, eye);
                String line = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish()
                    ? "this is how the fog sees you" : "asi te ve la niebla";
                int lw = this.font.width(line);
                gfx.drawString(this.font, line, mx - lw / 2, my + 44, (a << 24) | 0x8A9A70, false);
            }
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(gfx, this.width / 2, this.panelY - 12);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(gfx, this.width);
        } catch (Throwable ignored) {}
    }

    private int mirrorX() { return this.panelX + Math.min(PANEL_MAX_W, this.width - 40) - 34; }
    private int mirrorY() { return this.panelY + 12; }

    private void drawMirror(GuiGraphics gfx, int mouseX, int mouseY) {
        int x = mirrorX(), y = mirrorY();
        boolean hover = mouseX >= x - 2 && mouseX <= x + 20 && mouseY >= y - 2 && mouseY <= y + 28;
        // Marco ovalado de bronce
        int frame = hover ? 0xFFB89A6E : 0xFF8E6A35;
        gfx.fill(x + 2, y - 2, x + 16, y, frame);
        gfx.fill(x + 2, y + 24, x + 16, y + 26, frame);
        gfx.fill(x - 2, y + 4, x, y + 20, frame);
        gfx.fill(x + 18, y + 4, x + 20, y + 20, frame);
        // Vidrio: reflejo tenue que respira
        float breath = (float)(Math.sin(System.currentTimeMillis() / 1100.0) * 0.5 + 0.5);
        int glass = 0xC040384A;
        gfx.fill(x, y, x + 18, y + 24, glass);
        gfx.fill(x + 2, y + 2, x + 8, y + 10, com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(0xC8D8E0, 40 + (int)(breath * 30)));
        gfx.fill(x + 4, y + 14, x + 6, y + 20, com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(0xC8D8E0, 26));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= mirrorX() - 2 && mouseX <= mirrorX() + 20
                && mouseY >= mirrorY() - 2 && mouseY <= mirrorY() + 28) {
            try {
                mirrorViewStartMs = System.currentTimeMillis();
                mirrorViewUntilMs = mirrorViewStartMs + 3000L;
                boolean first = com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                    com.eternalcraft.gripeverde.client.lore.Discoveries.MIRROR);
                com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                    com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_AMBIENT_OCCASIONAL.get(),
                    0.20f, first ? 0.58f : 0.74f);
            } catch (Throwable ignored) {}
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
