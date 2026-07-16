package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IconButton extends AbstractButton {

    public enum IconType { GEAR, WORLD, BOOK, BOX }

    private final IconType type;
    private final Runnable onClick;
    private boolean wasHovered = false;

    public IconButton(int x, int y, int size, IconType type, Component tooltip, Runnable onClick) {
        super(x, y, size, size, tooltip);
        this.type = type;
        this.onClick = onClick;
        this.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip));
    }

    @Override
    public void onPress() {
        UiSounds.playClick();
        this.onClick.run();
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.isHovered();

        if (hovered && !wasHovered) UiSounds.playHover();
        wasHovered = hovered;

        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;

        // Marco tipo medallon de bronce
        int frameColor = hovered ? DiaryTheme.GOLD_TARNISHED : DiaryTheme.BRONZE_DARK;
        int fillColor  = hovered ? 0xFF3D2E1A : 0xFF1A1108;

        g.fill(x, y, x + w, y + h, fillColor);
        // Borde
        g.fill(x, y,         x + w, y + 1,     frameColor);
        g.fill(x, y + h - 1, x + w, y + h,     frameColor);
        g.fill(x, y,         x + 1, y + h,     frameColor);
        g.fill(x + w - 1, y, x + w, y + h,     frameColor);
        // Puntos en las esquinas (medallon)
        g.fill(x + 1, y + 1, x + 2, y + 2, DiaryTheme.GOLD_TARNISHED);
        g.fill(x + w - 2, y + 1, x + w - 1, y + 2, DiaryTheme.GOLD_TARNISHED);
        g.fill(x + 1, y + h - 2, x + 2, y + h - 1, DiaryTheme.GOLD_TARNISHED);
        g.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, DiaryTheme.GOLD_TARNISHED);

        int cx = x + w / 2;
        int cy = y + h / 2;
        int iconColor = hovered ? DiaryTheme.GOLD_TARNISHED : DiaryTheme.SEPIA_LIGHT;

        switch (type) {
            case GEAR:  drawWrench(g, cx, cy, iconColor); break;
            case WORLD: drawGlobe(g, cx, cy, iconColor); break;
            case BOOK:  drawTome(g, cx, cy, iconColor); break;
            case BOX:   drawTrunk(g, cx, cy, iconColor); break;
        }
    }

    /** Llave inglesa antigua */
    private void drawWrench(GuiGraphics g, int cx, int cy, int color) {
        // Mango diagonal
        g.fill(cx - 5, cy + 3, cx - 3, cy + 6, color);
        g.fill(cx - 4, cy + 1, cx - 2, cy + 4, color);
        g.fill(cx - 3, cy - 1, cx - 1, cy + 2, color);
        g.fill(cx - 2, cy - 3, cx,     cy,     color);
        // Cabeza (llave abierta)
        g.fill(cx - 1, cy - 5, cx + 4, cy - 3, color);
        g.fill(cx + 2, cy - 5, cx + 4, cy - 1, color);
        // Hueco de la llave
        g.fill(cx + 3, cy - 4, cx + 4, cy - 3, 0xFF0A0503);
    }

    /** Globo terraqueo con meridianos */
    private void drawGlobe(GuiGraphics g, int cx, int cy, int color) {
        // Circulo
        int r = 5;
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int)Math.sqrt(r * r - dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
        // Meridiano vertical
        g.fill(cx, cy - r, cx + 1, cy + r + 1, 0xFF0A0503);
        // Ecuador
        g.fill(cx - r, cy, cx + r + 1, cy + 1, 0xFF0A0503);
        // Base del globo
        g.fill(cx - 2, cy + r, cx + 3, cy + r + 2, color);
        g.fill(cx - 1, cy + r + 2, cx + 2, cy + r + 3, color);
    }

    /** Libro con broche */
    private void drawTome(GuiGraphics g, int cx, int cy, int color) {
        // Cuerpo del libro
        g.fill(cx - 5, cy - 6, cx + 6, cy + 6, color);
        // Lomo (linea vertical mas oscura)
        g.fill(cx - 5, cy - 6, cx - 4, cy + 6, 0xFF3D2E1A);
        // Paginas (linea horizontal blanca)
        g.fill(cx - 4, cy - 5, cx + 5, cy - 4, DiaryTheme.PAPER);
        g.fill(cx - 4, cy + 4, cx + 5, cy + 5, DiaryTheme.PAPER);
        // Broche dorado
        g.fill(cx + 4, cy - 2, cx + 7, cy + 2, DiaryTheme.GOLD_TARNISHED);
        g.fill(cx + 5, cy - 1, cx + 6, cy + 1, 0xFF0A0503);
        // Detalle del broche
        g.fill(cx + 3, cy - 3, cx + 5, cy - 1, DiaryTheme.GOLD_TARNISHED);
        g.fill(cx + 3, cy + 1, cx + 5, cy + 3, DiaryTheme.GOLD_TARNISHED);
    }

    /** Baul con herrajes */
    private void drawTrunk(GuiGraphics g, int cx, int cy, int color) {
        // Cuerpo del baul
        g.fill(cx - 6, cy - 3, cx + 7, cy + 6, color);
        // Tapa curva
        g.fill(cx - 5, cy - 5, cx + 6, cy - 3, color);
        g.fill(cx - 6, cy - 4, cx + 7, cy - 3, color);
        // Herrajes (bandas metalicas)
        g.fill(cx - 6, cy - 1, cx + 7, cy,     DiaryTheme.BRONZE);
        g.fill(cx - 6, cy + 3, cx + 7, cy + 4, DiaryTheme.BRONZE);
        // Cerradura central
        g.fill(cx - 1, cy - 4, cx + 2, cy - 1, DiaryTheme.GOLD_TARNISHED);
        g.fill(cx,     cy - 3, cx + 1, cy - 2, 0xFF0A0503);
        // Remaches en las esquinas
        g.fill(cx - 5, cy - 2, cx - 4, cy - 1, DiaryTheme.GOLD_TARNISHED);
        g.fill(cx + 5, cy - 2, cx + 6, cy - 1, DiaryTheme.GOLD_TARNISHED);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }
}