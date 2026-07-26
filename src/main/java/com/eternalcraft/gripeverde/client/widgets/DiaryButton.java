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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * DiaryButton — boton estilo tira de papel con icono opcional.
 *
 * Estetica: documento oficial de epoca. Fondo papel claro, borde tinta,
 * texto tinta. Hover intensifica el color y subraya el texto.
 *
 * Variantes:
 *   PRIMARY   -> texto rojo tinta (acciones destacadas)
 *   SECONDARY -> texto negro (por defecto)
 *   DANGER    -> texto rojo oscuro (acciones de salida)
 *
 * Iconos dibujados a mano en Java (sin dependencias externas).
 */
@OnlyIn(Dist.CLIENT)
public class DiaryButton extends AbstractButton {

    public enum Variant { PRIMARY, SECONDARY, DANGER }

    public enum Icon { NONE, HAND, SCROLL, CHART, GEAR, DOOR }

    private final Runnable onClick;
    private final Icon icon;
    private final Variant variant;
    private final EasedValue hoverAnim = new EasedValue(4f);
    private boolean wasHovered = false;

    // Constructor completo
    public DiaryButton(int x, int y, int w, int h,
                       Component label, Icon icon, Variant variant,
                       Runnable onClick) {
        super(x, y, w, h, label);
        this.icon    = icon    != null ? icon    : Icon.NONE;
        this.variant = variant != null ? variant : Variant.SECONDARY;
        this.onClick = onClick;
    }

    // Constructor simple (sin icono, secundario)
    public DiaryButton(int x, int y, int w, int h, Component label, Runnable onClick) {
        this(x, y, w, h, label, Icon.NONE, Variant.SECONDARY, onClick);
    }

    @Override
    public void onPress() {
        if (this.onClick != null) this.onClick.run();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        UiSounds.playClick();
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;

        boolean hovered = this.isHoveredOrFocused() && this.active;

        // Detect pressed state: mouse button held while hovered
        boolean pressed = hovered && this.active
            && com.mojang.blaze3d.platform.InputConstants.isKeyDown(
                   org.lwjgl.glfw.GLFW.glfwGetCurrentContext(),
                   com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(
                       org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT).getValue());

        // Sonido de hover una sola vez al entrar
        if (hovered && !wasHovered) {
            UiSounds.playHover();
        }
        wasHovered = hovered;

        // Animacion de hover
        hoverAnim.setTarget(hovered);
        hoverAnim.tick(partialTick / 20f);
        float t = hoverAnim.getEased();

        // Pressed offset: button sinks 1px down when clicked
        int pressedOffset = pressed ? 1 : 0;
        y += pressedOffset;

        // -------------------- FONDO --------------------
        // Sombra externa: smaller when pressed (button sinks into paper)
        if (!pressed) {
            int outerShadow = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 110);
            g.fill(x + 2, y + h, x + w + 1, y + h + 2, outerShadow);
            int outerShadowSoft = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 50);
            g.fill(x + 3, y + h + 1, x + w + 2, y + h + 3, outerShadowSoft);
        } else {
            int outerShadow = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 60);
            g.fill(x + 1, y + h, x + w, y + h + 1, outerShadow);
        }

        // Fondo base: darker when pressed for tactile feel
        int bgHover = 0xFFB09668; // ocre sucio
        int bgPressed = 0xFFA08858; // aun mas oscuro
        int bgTarget = pressed ? bgPressed : bgHover;
        int bg = pressed
            ? bgPressed
            : hoverAnim.lerpColor(DiaryTheme.PAPER_STAINED, bgHover);
        g.fill(x, y, x + w, y + h, bg);

        // Highlight superior (papel iluminado desde arriba)
        int topHighlight = DiaryTheme.withAlpha(0x00FFEECC, 90);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, topHighlight);

        // Sombra interna arriba (grieta del borde)
        int innerShadowTop = DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 70);
        g.fill(x, y, x + w, y + 1, innerShadowTop);

        // Sombra interna abajo (papel apoyado, arruga)
        int innerShadowBot = DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 100);
        g.fill(x, y + h - 2, x + w, y + h - 1, innerShadowBot);

        // -------------------- BORDE --------------------
        int borderColor = hoverAnim.lerpColor(
            DiaryTheme.SEPIA_DARK,
            this.active ? getVariantAccent() : DiaryTheme.SEPIA_MID
        );

        // Borde exterior
        drawBorder(g, x, y, w, h, borderColor);

        // -------------------- CONTENIDO --------------------
        Font font = Minecraft.getInstance().font;
        String text = this.getMessage().getString();

        // Color del texto segun variante y hover
        int textColor = getTextColor(t);
        if (!this.active) {
            textColor = DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 120);
        }

        // Layout interno: icono a la izquierda si existe
        int textStartX;
        int contentY = y + (h - font.lineHeight) / 2;

        if (this.icon != Icon.NONE) {
            int iconX = x + 8;
            int iconY = y + (h - 10) / 2;
            drawIcon(g, iconX, iconY, textColor);
            textStartX = iconX + 12 + 6; // icono (12) + gap (6)
        } else {
            // Sin icono: texto centrado
            int tw = font.width(text);
            textStartX = x + (w - tw) / 2;
        }

        // Texto
        g.drawString(font, text, textStartX, contentY, textColor, false);

        // Subrayado en hover
        if (t > 0.1f && this.active) {
            int tw = font.width(text);
            int underlineY = contentY + font.lineHeight;
            int underlineAlpha = (int)(t * 200);
            int underlineColor = DiaryTheme.withAlpha(getVariantAccent(), underlineAlpha);
            g.fill(textStartX, underlineY, textStartX + tw, underlineY + 1, underlineColor);
        }
    }

    // ------------------------------------------------------------
    // Helpers de color
    // ------------------------------------------------------------

    private int getVariantAccent() {
        switch (this.variant) {
            case PRIMARY: return DiaryTheme.REDCOAT;
            case DANGER:  return DiaryTheme.REDCOAT_DARK;
            case SECONDARY:
            default:      return DiaryTheme.INK;
        }
    }

    private int getTextColor(float hoverT) {
        int base;
        int hoverColor;
        switch (this.variant) {
            case PRIMARY:
                base       = DiaryTheme.REDCOAT_DARK;
                hoverColor = DiaryTheme.REDCOAT_BRIGHT;
                break;
            case DANGER:
                base       = DiaryTheme.REDCOAT_DARK;
                hoverColor = DiaryTheme.REDCOAT;
                break;
            case SECONDARY:
            default:
                base       = DiaryTheme.INK;
                hoverColor = DiaryTheme.INK_BLACK;
                break;
        }
        return hoverAnim.lerpColor(base, hoverColor);
    }

    // ------------------------------------------------------------
    // Borde perimetral (linea de 1px)
    // ------------------------------------------------------------

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x,         y,         x + w,     y + 1,     color); // top
        g.fill(x,         y + h - 1, x + w,     y + h,     color); // bottom
        g.fill(x,         y,         x + 1,     y + h,     color); // left
        g.fill(x + w - 1, y,         x + w,     y + h,     color); // right
    }

    // ------------------------------------------------------------
    // Iconos (dibujados en 12x10, coord esquina superior izq)
    // ------------------------------------------------------------

    private void drawIcon(GuiGraphics g, int x, int y, int color) {
        switch (this.icon) {
            case HAND:   drawHand(g, x, y, color);   break;
            case SCROLL: drawScroll(g, x, y, color); break;
            case CHART:  drawChart(g, x, y, color);  break;
            case GEAR:   drawGear(g, x, y, color);   break;
            case DOOR:   drawDoor(g, x, y, color);   break;
            case NONE:
            default: break;
        }
    }

    // Mano senalando con dedo indice (12x10)
    private void drawHand(GuiGraphics g, int x, int y, int color) {
        // Palma
        g.fill(x + 4, y + 3, x + 9, y + 8, color);
        // Dedo indice
        g.fill(x + 9, y + 4, x + 12, y + 6, color);
        // Punta del dedo
        g.fill(x + 11, y + 3, x + 12, y + 7, color);
        // Muneca
        g.fill(x, y + 4, x + 4, y + 7, color);
    }

    // Pergamino enrollado (12x10)
    private void drawScroll(GuiGraphics g, int x, int y, int color) {
        // Rollo izquierdo
        g.fill(x, y + 2, x + 2, y + 8, color);
        g.fill(x + 1, y + 1, x + 3, y + 9, color);
        // Cuerpo central (mas claro)
        g.fill(x + 2, y + 3, x + 10, y + 7, DiaryTheme.withAlpha(color, 180));
        // Lineas de texto
        g.fill(x + 3, y + 4, x + 9, y + 5, color);
        g.fill(x + 3, y + 6, x + 8, y + 7, color);
        // Rollo derecho
        g.fill(x + 10, y + 2, x + 12, y + 8, color);
        g.fill(x + 9, y + 1, x + 11, y + 9, color);
    }

    // Grafico de barras (12x10)
    private void drawChart(GuiGraphics g, int x, int y, int color) {
        // Barras de distintas alturas
        g.fill(x + 1, y + 6, x + 3, y + 9, color);
        g.fill(x + 4, y + 3, x + 6, y + 9, color);
        g.fill(x + 7, y + 5, x + 9, y + 9, color);
        g.fill(x + 10, y + 2, x + 12, y + 9, color);
        // Base
        g.fill(x, y + 9, x + 12, y + 10, color);
    }

    // Engranaje (12x10)
    private void drawGear(GuiGraphics g, int x, int y, int color) {
        // Cuerpo central
        g.fill(x + 3, y + 2, x + 9, y + 8, color);
        // Dientes
        g.fill(x + 4, y,     x + 5, y + 2, color); // arriba izq
        g.fill(x + 7, y,     x + 8, y + 2, color); // arriba der
        g.fill(x + 4, y + 8, x + 5, y + 10, color); // abajo izq
        g.fill(x + 7, y + 8, x + 8, y + 10, color); // abajo der
        g.fill(x,     y + 4, x + 3, y + 5, color); // izq arriba
        g.fill(x,     y + 6, x + 3, y + 7, color); // no ideal pero simetrico
        g.fill(x + 9, y + 4, x + 12, y + 5, color); // der arriba
        g.fill(x + 9, y + 6, x + 12, y + 7, color); // der abajo
        // Hueco central
        g.fill(x + 5, y + 4, x + 7, y + 6, DiaryTheme.PAPER);
    }

    // Puerta arqueada (12x10)
    private void drawDoor(GuiGraphics g, int x, int y, int color) {
        // Cuerpo de la puerta
        g.fill(x + 2, y + 3, x + 10, y + 10, color);
        // Arco superior
        g.fill(x + 3, y + 1, x + 9, y + 3, color);
        g.fill(x + 4, y,     x + 8, y + 1, color);
        // Interior mas claro (perspectiva)
        g.fill(x + 4, y + 4, x + 8, y + 9, DiaryTheme.withAlpha(color, 100));
        // Pomo
        g.fill(x + 8, y + 6, x + 9, y + 7, color);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }
}