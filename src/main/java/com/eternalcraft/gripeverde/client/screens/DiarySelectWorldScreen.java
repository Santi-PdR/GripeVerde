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
 *
 * Los mundos guardados se abrian con la pantalla cruda del juego: fondo de
 * tierra, botones grises y un titulo blanco. Ahora la entrada de un jugador
 * lleva a la misma lista, pero leida como el resto de la casa: escena de noche
 * detras, cabecera de papel con el membrete y los botones del juego vestidos
 * de papel.
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

    // v0.243.0: la pagina de registro. Las medidas de la lista se leen una vez
    // al abrir o al redimensionar; por cuadro solo cambia el desplazamiento.
    private int rowItemHeight;
    private int rowLeft = -1;
    private int rowRight = -1;
    private int listTop = -1;
    private int listBottom = -1;

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
        cacheListMetrics();
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

    /** v0.243.0: medidas de la lista, una vez por apertura o resize. */
    private void cacheListMetrics() {
        this.rowItemHeight = 0;
        this.rowLeft = -1;
        this.rowRight = -1;
        this.listTop = -1;
        this.listBottom = -1;
        if (this.vanillaList == null) return;
        try {
            this.rowItemHeight = readIntField(this.vanillaList, "itemHeight", "f_93387_");
            this.rowLeft = this.vanillaList.getRowLeft();
            this.rowRight = this.rowLeft + this.vanillaList.getRowWidth();
            this.listTop = this.vanillaList.getTop();
            this.listBottom = this.vanillaList.getBottom();
        } catch (Throwable ignored) {
        }
    }

    private static int readIntField(Object target, String officialName, String srgName) {
        try {
            java.lang.reflect.Field f = null;
            Class<?> cls = target.getClass();
            while (cls != null && f == null) {
                try { f = cls.getDeclaredField(officialName); } catch (NoSuchFieldException ignored) {}
                if (f == null) {
                    try { f = cls.getDeclaredField(srgName); } catch (NoSuchFieldException ignored) {}
                }
                if (f == null) cls = cls.getSuperclass();
            }
            if (f != null) {
                f.setAccessible(true);
                return f.getInt(target);
            }
        } catch (Throwable ignored) {
        }
        return 0;
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

    /**
     * v0.248.0: si algo del juego llega a llamar al fondo, dibuja la escena y no
     * la tierra. Antes este metodo quedaba vacio y la lista aparecia sobre el
     * fondo crudo.
     */
    @Override
    public void renderBackground(GuiGraphics g) {
        try {
            QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        try {
            QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
        } catch (Throwable ignored) {
        }

        drawHeader(g);
        drawRegisterPage(g, mouseX, mouseY);
        drawFooterPaper(g);

        // Lista y busqueda del juego, tal cual, encima de la pagina.
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

    /**
     * v0.243.0: la lista vive sobre una pagina de registro, y cada mundo sobre
     * su banda oscura, como las fotos pegadas en la ficha. Sin la banda, el
     * texto blanco del juego se perderia contra el papel.
     */
    private void drawRegisterPage(GuiGraphics g, int mouseX, int mouseY) {
        if (this.vanillaList == null || this.rowLeft < 0 || this.listBottom <= this.listTop) return;
        int px = Math.max(-2, this.rowLeft - 14);
        int pw = Math.min(this.width + 2, this.rowRight + 14) - px;
        int py = Math.max(HEADER_H - 30, this.listTop - 6);
        int ph = Math.min(this.height, this.listBottom + 6) - py;
        if (pw < 40 || ph < 24) return;
        PaperTexture.drawTornSheet(g, px, py, pw, ph, PaperTexture.PaperKind.OFFICIAL);
        // v0.248.0: el canto gastado asienta la pagina sobre la escena.
        if (pw >= 32 && ph >= 32) {
            PaperTexture.drawWornRim(g, px, py, pw, ph);
        }

        int step = this.rowItemHeight > 0 ? this.rowItemHeight : 36;
        double scroll = 0.0;
        try {
            scroll = this.vanillaList.getScrollAmount();
        } catch (Throwable ignored) {
        }
        int count = 0;
        try {
            count = this.vanillaList.children().size();
        } catch (Throwable ignored) {
        }
        // Banda oscura por mundo, recortada a la pagina.
        for (int i = 0; i < count && i < 64; i++) {
            int top = this.listTop + 4 - (int) scroll + i * step;
            int bottom = top + step - 4;
            if (bottom < this.listTop || top > this.listBottom) continue;
            int drawTop = Math.max(top, this.listTop);
            int drawBottom = Math.min(bottom, this.listBottom);
            if (drawBottom <= drawTop) continue;
            g.fill(this.rowLeft - 6, drawTop, this.rowRight + 6, drawBottom, 0x6E2A1C10);
            g.fill(this.rowLeft - 6, drawBottom - 1, this.rowRight + 6, drawBottom, 0x55E8DCB4);
            // v0.251.0: el mundo bajo la mano se aclara, igual que en la lista
            // de posadas, para saber cual se lleva.
            if (mouseX >= this.rowLeft - 6 && mouseX <= this.rowRight + 6
                    && mouseY >= drawTop && mouseY < drawBottom) {
                g.fill(this.rowLeft - 6, drawTop, this.rowRight + 6, drawBottom, 0x1AF3E7C8);
            }
        }
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

    /**
     * v0.251.0: Enter entra al mundo elegido. El boton del juego sigue siendo
     * el que trabaja; aca solo se lo aprieta por el lector.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
                || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
            String want = net.minecraft.network.chat.Component.translatable("selectWorld.select").getString();
            for (DiaryButton button : this.dressed) {
                if (button.visible && button.active && want.equals(button.getMessage().getString())) {
                    try {
                        com.eternalcraft.gripeverde.client.audio.UiSounds.playClick();
                    } catch (Throwable ignored) {
                    }
                    button.onPress();
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
