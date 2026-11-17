package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.PaperButtons;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.gui.ModListScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * v0.244.0: la lista de mods, leida en la casa.
 *
 * Forge trae su propia pantalla de mods -lista oscura, botones de siempre-. No
 * se reescribe: se le cambia el fondo por la escena de la noche, se le visten
 * los botones de papel y se les pone una hoja debajo para que no queden
 * colgando sobre la calle. Todo lo demas -busqueda, configuracion, enlaces- es
 * el de Forge, intacto.
 */
public class DiaryModListScreen extends ModListScreen {

    private final List<DiaryButton> dressed = new ArrayList<>();
    private AbstractSelectionList<?> list;
    private int itemHeight;

    public DiaryModListScreen(Screen parent) {
        super(parent);
    }

    // Forge ensancha init() a public en ModListScreen: el override tiene que
    // mantener ese acceso o el compilador lo rechaza.
    @Override
    public void init() {
        super.init();
        // v0.255.0: la lista de Forge tambien pinta su fondo por cuadro; se le
        // apaga para que no tape la escena ni las fichas.
        this.list = findList();
        if (this.list != null) {
            try {
                this.list.setRenderBackground(false);
                this.list.setRenderTopAndBottom(false);
            } catch (Throwable ignored) {
            }
            this.itemHeight = readItemHeight(this.list);
        }
        this.dressed.clear();
        this.dressed.addAll(PaperButtons.dressAll(this));
    }

    private AbstractSelectionList<?> findList() {
        try {
            for (var child : this.children()) {
                if (child instanceof AbstractSelectionList<?> list) return list;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static int readItemHeight(Object list) {
        try {
            for (Class<?> c = list.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                    if (f.getType() != int.class) continue;
                    if (!f.getName().equals("itemHeight") && !f.getName().equals("f_93387_")) continue;
                    f.setAccessible(true);
                    return f.getInt(list);
                }
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }

    /**
     * v0.255.0: cada mod se apoya en su recorte de papel, igual que las fichas
     * del archivo y los puestos de la lista de servidores. La geometria se lee
     * de la lista de Forge; si algun dato falta, no se dibuja nada.
     */
    private void drawRowSlips(GuiGraphics g, int mouseX, int mouseY) {
        if (this.list == null || this.itemHeight <= 0) return;
        try {
            int left = this.list.getRowLeft();
            int right = left + this.list.getRowWidth();
            int top = this.list.getTop();
            int bottom = this.list.getBottom();
            double scroll = this.list.getScrollAmount();
            int count = this.list.children().size();
            for (int i = 0; i < count && i < 96; i++) {
                int rowTop = top + 2 - (int) scroll + i * this.itemHeight;
                int rowBottom = rowTop + this.itemHeight - 2;
                if (rowBottom < top || rowTop > bottom) continue;
                int drawTop = Math.max(rowTop, top);
                int drawBottom = Math.min(rowBottom, bottom);
                if (drawBottom - drawTop < 8) continue;
                PaperTexture.drawTornSlip(g, left - 9, drawTop - 1,
                    right - left + 18, drawBottom - drawTop + 2, 0xE6D2A6, 0x422810, 205);
                if (mouseX >= left - 9 && mouseX <= right + 9
                        && mouseY >= drawTop && mouseY < drawBottom) {
                    g.fill(left - 9, drawTop, right + 9, drawBottom, 0x18F3E7C8);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        try {
            QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
        } catch (Throwable ignored) {
        }
        drawRowSlips(g, mouseX, mouseY);
        drawFooterPaper(g);
        super.render(g, mouseX, mouseY, partial);
    }

    /** Hoja bajo el grupo de botones, calculada del alto real que les dio Forge. */
    private void drawFooterPaper(GuiGraphics g) {
        if (this.dressed.isEmpty()) return;
        int left = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int top = Integer.MAX_VALUE;
        int bottom = Integer.MIN_VALUE;
        for (DiaryButton button : this.dressed) {
            left = Math.min(left, button.getX());
            right = Math.max(right, button.getX() + button.getWidth());
            top = Math.min(top, button.getY());
            bottom = Math.max(bottom, button.getY() + button.getHeight());
        }
        if (right <= left || bottom <= top) return;
        int x = Math.max(-2, left - 10);
        int y = Math.max(0, top - 8);
        int w = Math.min(this.width + 2, right + 10) - x;
        int h = Math.min(this.height + 6, bottom + 10) - y;
        if (w < 24 || h < 16) return;
        PaperTexture.drawTornSheet(g, x, y, w, h, PaperTexture.PaperKind.OFFICIAL);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        // La escena de la casa hace de fondo.
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
