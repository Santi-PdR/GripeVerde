package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.util.EasedValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GreenFluButton extends AbstractButton {

    private final Component title;
    private final Component subtitle;
    private final ResourceLocation iconTexture;
    private final Runnable onClick;
    private boolean wasHovered = false;

    // Hover suave (0.0 apagado -> 1.0 encendido)
    private final EasedValue hoverAnim = new EasedValue(6f); // 6.0 = ~170ms

    public GreenFluButton(int x, int y, int w, int h,
                          Component title, Component subtitle,
                          ResourceLocation iconTexture,
                          Runnable onClick) {
        super(x, y, w, h, title);
        this.title = title;
        this.subtitle = subtitle;
        this.iconTexture = iconTexture;
        this.onClick = onClick;
    }

    @Override
    public void onPress() {
        UiSounds.playClick();
        this.onClick.run();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        // Silencia vanilla - UiSounds ya maneja el click
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.isHovered();

        // Sonido solo cuando ENTRA hovered
        if (hovered && !wasHovered) UiSounds.playHover();
        wasHovered = hovered;

        // Actualizar animacion (partialTick esta en ticks 1/20s)
        hoverAnim.setTarget(hovered);
        hoverAnim.tick(partialTick / 20f);
        float t = hoverAnim.getEased();

        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;

        // Fondo: lerp entre oscuro y ligeramente iluminado, con presion tactil.
        boolean pressed = hovered && this.active
            && com.mojang.blaze3d.platform.InputConstants.isKeyDown(
                org.lwjgl.glfw.GLFW.glfwGetCurrentContext(),
                com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(
                    org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT).getValue());
        int pressOffset = pressed ? 1 : 0;
        y += pressOffset;

        if (!pressed) {
            g.fill(x + 2, y + h, x + w + 1, y + h + 2, DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 120));
        }
        int bgColor = pressed ? 0xFF24140C : hoverAnim.lerpColor(0xFF15100A, 0xFF2A1810);
        g.fill(x, y, x + w, y + h, bgColor);

        // Brillo horizontal superior y velo rojizo en hover.
        g.fill(x + 1, y + 1, x + w - 1, y + 2, DiaryTheme.withAlpha(0x00FFE8C0, 28 + (int)(t * 45)));
        if (t > 0.05f) {
            int glow = DiaryTheme.withAlpha(DiaryTheme.REDCOAT, (int)(t * 32));
            g.fill(x + 1, y + 2, x + w - 1, y + h - 2, glow);
        }

        // Borde: lerp entre bronce oscuro y rojo britanico
        int borderColor = hoverAnim.lerpColor(DiaryTheme.BRONZE_DARK, DiaryTheme.REDCOAT);
        g.fill(x, y,         x + w, y + 1,     borderColor);
        g.fill(x, y + h - 1, x + w, y + h,     borderColor);
        g.fill(x, y,         x + 1, y + h,     borderColor);
        g.fill(x + w - 1, y, x + w, y + h,     borderColor);

        // Esquinas remachadas/minutas para leer mas "placa" que boton plano.
        int corner = hoverAnim.lerpColor(DiaryTheme.BRONZE_DARK, DiaryTheme.GOLD_TARNISHED);
        g.fill(x + 4, y + 4, x + 6, y + 6, corner);
        g.fill(x + w - 6, y + 4, x + w - 4, y + 6, corner);

        // Barra lateral: crece con easing (de 3px de alto minimo a full)
        int barColor = hoverAnim.lerpColor(DiaryTheme.SEPIA_DARK, DiaryTheme.REDCOAT_BRIGHT);
        int barTopReduction = (int) ((1f - t) * ((h - 4) / 2));
        g.fill(x + 2, y + 2 + barTopReduction, x + 5, y + h - 2 - barTopReduction, barColor);
        if (t > 0.2f) {
            int sparkY = y + h / 2 + (int)(Math.sin(System.currentTimeMillis() / 160.0) * (h / 4.0));
            g.fill(x + 6, sparkY, x + 8, sparkY + 1, DiaryTheme.withAlpha(DiaryTheme.AMBER_FLAME, (int)(t * 180)));
        }

        // Icono a mano: regiment/arsenal. Los ResourceLocation quedan como
        // selector semantico aunque el PNG no exista en disco.
        Font font = Minecraft.getInstance().font;
        int iconX = x + 14;
        int iconY = y + 10;
        int iconColor = hoverAnim.lerpColor(DiaryTheme.SEPIA_LIGHT, DiaryTheme.AMBER_FLAME);
        drawButtonIcon(g, iconX, iconY, iconColor, t);
        int sepCol = DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 135);
        g.fill(x + 36, y + 6, x + 37, y + h - 6, sepCol);
        g.fill(x + w - 12, y + 7, x + w - 10, y + 9, DiaryTheme.withAlpha(DiaryTheme.GOLD_TARNISHED, 130));
        g.fill(x + w - 12, y + h - 9, x + w - 10, y + h - 7, DiaryTheme.withAlpha(DiaryTheme.GOLD_TARNISHED, 95));
        g.fill(x + 44, y + h - 7, x + w - 18, y + h - 6, DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 70));

        // Textos: lerp de color + sombra + ellipsis para idiomas largos.
        int textX = x + 44;
        int maxTextW = Math.max(20, w - 52);
        String titleText = ellipsize(font, this.title.getString(), maxTextW);
        String subtitleText = ellipsize(font, this.subtitle.getString(), maxTextW);
        int titleColor = hoverAnim.lerpColor(DiaryTheme.BONE_WHITE, DiaryTheme.PAPER);
        int subColor   = hoverAnim.lerpColor(DiaryTheme.SEPIA_LIGHT, DiaryTheme.OCHRE);

        g.drawString(font, titleText, textX, y + 8, titleColor, false);
        g.drawString(font, subtitleText, textX, y + 22, subColor, false);

        if (this.isFocused() && !this.isMouseOver(mouseX, mouseY) && this.active) {
            g.fill(x - 1, y - 1, x + w + 1, y, DiaryTheme.GOLD_TARNISHED);
            g.fill(x - 1, y + h, x + w + 1, y + h + 1, DiaryTheme.GOLD_TARNISHED);
            g.fill(x - 1, y - 1, x, y + h + 1, DiaryTheme.GOLD_TARNISHED);
            g.fill(x + w, y - 1, x + w + 1, y + h + 1, DiaryTheme.GOLD_TARNISHED);
        }
    }

    private void drawButtonIcon(GuiGraphics g, int x, int y, int color, float hoverT) {
        String path = this.iconTexture == null ? "" : this.iconTexture.getPath();
        int shadow = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 170);
        if (path.contains("mods")) {
            drawArsenalIcon(g, x + 1, y + 1, shadow);
            drawArsenalIcon(g, x, y, color);
        } else {
            drawRegimentIcon(g, x + 1, y + 1, shadow);
            drawRegimentIcon(g, x, y, color);
        }
        if (hoverT > 0.35f) {
            g.fill(x - 2, y + 18, x + 18, y + 19,
                DiaryTheme.withAlpha(DiaryTheme.AMBER_FLAME, (int)(hoverT * 90)));
        }
    }

    private void drawRegimentIcon(GuiGraphics g, int x, int y, int color) {
        // Crossed sabre / banner in 18x18.
        for (int i = 0; i < 13; i++) {
            g.fill(x + 2 + i, y + 14 - i, x + 3 + i, y + 15 - i, color);
            g.fill(x + 4 + i, y + 3 + i, x + 5 + i, y + 4 + i, color);
        }
        g.fill(x + 1, y + 14, x + 5, y + 16, color);
        g.fill(x + 13, y + 14, x + 17, y + 16, color);
        g.fill(x + 8, y + 1, x + 10, y + 7, color);
        g.fill(x + 5, y + 2, x + 13, y + 5, DiaryTheme.withAlpha(color, 150));
    }

    private void drawArsenalIcon(GuiGraphics g, int x, int y, int color) {
        // Crate + gear tooth hint in 18x18.
        g.fill(x + 2, y + 6, x + 15, y + 16, color);
        g.fill(x + 3, y + 7, x + 14, y + 15, DiaryTheme.withAlpha(color, 120));
        g.fill(x + 2, y + 6, x + 15, y + 7, color);
        g.fill(x + 2, y + 11, x + 15, y + 12, color);
        g.fill(x + 8, y + 7, x + 9, y + 16, color);
        g.fill(x + 5, y + 2, x + 12, y + 4, color);
        g.fill(x + 6, y, x + 8, y + 2, color);
        g.fill(x + 10, y, x + 12, y + 2, color);
    }

    private static String ellipsize(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        int room = Math.max(0, maxWidth - font.width(ellipsis));
        return font.plainSubstrByWidth(text, room) + ellipsis;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }
}