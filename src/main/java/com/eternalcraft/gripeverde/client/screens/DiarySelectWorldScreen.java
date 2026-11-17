package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.lore.Discoveries;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperButtons;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * v0.242.0: la puerta de servicio de la portada.
 *
 * Los mundos guardados se abrian con la pantalla cruda del juego: fondo negro,
 * botones grises y un titulo blanco. Ahora la entrada -que sigue siendo un
 * secreto, Ctrl+S o Shift+S en la vigilia- lleva a la misma lista, pero leida
 * como el resto de la casa: escena de noche detras, cabecera de papel con el
 * membrete y los botones del juego vestidos de papel.
 *
 * No se reescribe nada del juego: la lista, la busqueda y cada accion siguen
 * siendo las de Minecraft, con su logica intacta. Lo unico que cambia es la
 * tinta.
 */
public class DiarySelectWorldScreen extends SelectWorldScreen {

    private static final int HEADER_H = 46;

    private final List<DiaryButton> dressed = new ArrayList<>();
    private WorldSelectionList vanillaList;
    private List<EditBox> searchBoxes = new ArrayList<>();

    public DiarySelectWorldScreen(Screen parent) {
        super(parent);
    }

    @Override
    protected void init() {
        super.init();
        this.vanillaList = resolveList();
        this.searchBoxes = collectEditBoxes();
        this.dressed.clear();
        this.dressed.addAll(PaperButtons.dressAll(this));
    }

    private WorldSelectionList resolveList() {
        for (Class<?> c = SelectWorldScreen.class; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (!WorldSelectionList.class.isAssignableFrom(field.getType())) continue;
                try {
                    field.setAccessible(true);
                    Object value = field.get(this);
                    if (value instanceof WorldSelectionList list) return list;
                } catch (Throwable ignored) {
                }
            }
        }
        return null;
    }

    private List<EditBox> collectEditBoxes() {
        List<EditBox> boxes = new ArrayList<>();
        try {
            for (var child : this.children()) {
                if (child instanceof EditBox box && box.visible) boxes.add(box);
            }
        } catch (Throwable ignored) {
        }
        return boxes;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        try {
            QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
        } catch (Throwable ignored) {
        }

        drawHeader(g);
        drawFooterPaper(g);

        // Lista y busqueda del juego, tal cual, encima de la escena.
        try {
            if (this.vanillaList != null) {
                this.vanillaList.render(g, mouseX, mouseY, partial);
            }
            for (EditBox box : this.searchBoxes) {
                if (box.visible) box.render(g, mouseX, mouseY, partial);
            }
        } catch (Throwable ignored) {
        }

        for (DiaryButton button : this.dressed) {
            if (button.visible) button.render(g, mouseX, mouseY, partial);
        }

        try {
            Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {
        }
    }

    private void drawHeader(GuiGraphics g) {
        int w = this.width;
        PaperTexture.drawTornSheet(g, -2, -6, w + 4, HEADER_H, PaperTexture.PaperKind.OFFICIAL);
        Font font = this.font;
        boolean en = Discoveries.isEnglish();
        String title = en ? "SAVED WORLDS" : "MUNDOS GUARDADOS";
        String sub = en ? "- The house archive -" : "- El archivo de la casa -";
        int cx = w / 2;
        g.drawString(font, title, cx - DiaryChrome.widthCached(font, title) / 2, 8,
            DiaryChrome.INK_TITLE, false);
        DiaryChrome.drawPencilRule(g, cx - 70, 18, 140, w * 31 + 7);
        g.drawString(font, sub, cx - DiaryChrome.widthCached(font, sub) / 2, 21,
            DiaryChrome.INK_SUBTITLE, false);
        DiaryChrome.drawRegistryTagLeft(g, 0, -4, "REG. IX");
    }

    /** Papel detras del grupo de botones, sea cual sea el alto que les dio el juego. */
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
        int pad = 8;
        int x = Math.max(-2, left - pad);
        int y = Math.max(0, top - pad);
        int w = Math.min(this.width + 2, right + pad) - x;
        int h = Math.min(this.height + 6, bottom + pad + 2) - y;
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
