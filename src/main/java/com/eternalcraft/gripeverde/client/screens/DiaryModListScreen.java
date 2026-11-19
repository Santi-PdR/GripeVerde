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
    // v0.272.0: la hoja solo suena la primera vez que se abre, no al redimensionar.
    private boolean paperSoundDone = false;

    // v0.302.0: lo que el arsenal dice del mod bajo la mano, y donde empieza
    // el papel de los botones, para no escribir encima de ellos.
    private String hoveredModDesc = "";
    private int footerTop = -1;

    @Override
    public void init() {
        // v0.272.0: la hoja se abre con ruido de papel, una vez por apertura.
        if (!this.paperSoundDone) {
            this.paperSoundDone = true;
            try {
                com.eternalcraft.gripeverde.client.audio.UiSounds.open();
            } catch (Throwable ignored) {
            }
        }
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
        dressSearchBox();
    }

    /**
     * v0.266.0: el buscador de Forge pide las cosas a su manera; aca se le pone
     * el texto de la casa cuando esta vacio.
     */
    private void dressSearchBox() {
        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        String hint = en ? "search a mod" : "buscar un mod";
        try {
            for (var child : this.children()) {
                if (child instanceof net.minecraft.client.gui.components.EditBox box) {
                    box.setSuggestion(hint);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /** v0.266.0: cuantas modificaciones lleva cargadas el juego. */
    private String modCount(boolean en) {
        try {
            int count = net.minecraftforge.fml.ModList.get().getMods().size();
            if (count <= 0) return "";
            return en ? count + " modifications loaded" : count + " modificaciones cargadas";
        } catch (Throwable ignored) {
            return "";
        }
    }

    /** v0.278.0: "Forge 47.4.20", dicho sin adornos. */
    private String forgeLine() {
        try {
            String v = net.minecraftforge.versions.forge.ForgeVersion.getVersion();
            if (v == null || v.isBlank()) return "";
            return "Forge " + v;
        } catch (Throwable ignored) {
            return "";
        }
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
     * v0.265.0: el arsenal se lee en tinta. Forge escribe esos nombres en
     * blanco fijo y sobre papel claro no se veian; no se puede cambiar su
     * color, asi que se cubre el renglon con su recorte de papel y se vuelve a
     * escribir el nombre y la version en negro y sepia, en el mismo sitio.
     * Si el dato del mod no se puede leer, la fila se deja como estaba.
     */
    private void paintRowsInk(GuiGraphics g, int mouseX, int mouseY) {
        if (this.list == null || this.itemHeight <= 0) return;
        int left;
        int width;
        int top;
        int bottom;
        int count;
        try {
            left = this.list.getRowLeft();
            width = this.list.getRowWidth();
            top = this.list.getTop();
            bottom = this.list.getBottom();
            count = this.list.children().size();
        } catch (Throwable ignored) {
            return;
        }
        int scroll = 0;
        try {
            scroll = (int) this.list.getScrollAmount();
        } catch (Throwable ignored) {
        }
        Object selected = null;
        try {
            selected = this.list.getSelected();
        } catch (Throwable ignored) {
        }
        net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
        boolean scissored = false;
        try {
            g.enableScissor(left - 12, top - 2, left + width + 12, bottom + 2);
            scissored = true;
        } catch (Throwable ignored) {
        }
        for (int i = 0; i < count && i < 160; i++) {
            int rowTop = top + 2 - scroll + i * this.itemHeight;
            int rowBottom = rowTop + this.itemHeight - 2;
            if (rowBottom < top || rowTop > bottom) continue;
            Object entry;
            try {
                entry = this.list.children().get(i);
            } catch (Throwable ignored) {
                continue;
            }
            String[] info = modNameAndVersion(entry);
            if (info == null) continue;
            boolean chosen = entry == selected;
            boolean hovered = mouseX >= left - 9 && mouseX <= left + width + 9
                && mouseY >= Math.max(rowTop, top) && mouseY < Math.min(rowBottom, bottom);

            PaperTexture.drawTornSlip(g, left - 9, rowTop - 3,
                width + 18, rowBottom - rowTop + 6, 0xE6D2A6, 0x422810, chosen ? 255 : 232);
            if (chosen) {
                g.fill(left - 9, rowTop - 2, left - 6, rowBottom + 2, 0xFF8C2A1E);
            }
            if (hovered) {
                g.fill(left - 9, rowTop - 2, left + width + 9, rowBottom + 2, 0x1CF3E7C8);
            }

            if (hovered) {
                this.hoveredModDesc = modDescription(entry);
            }
            String name = ellipsizeTo(font, info[0], width - 24);
            // v0.306.0: debajo del nombre, la version y el identificador del
            // mod, que es lo que se busca cuando algo falla.
            String id = modId(entry);
            String versionSrc = id.isEmpty() ? info[1] : info[1] + "  \u00b7  " + id;
            String version = ellipsizeTo(font, versionSrc, width - 24);
            g.drawString(font, name, left + 3, rowTop + 2, 0xFF1A1108, false);
            g.drawString(font, version, left + 3, rowTop + 2 + font.lineHeight, 0xFF6B5533, false);
        }
        if (scissored) {
            try {
                g.disableScissor();
            } catch (Throwable ignored) {
            }
        }
    }

    private String ellipsizeTo(net.minecraft.client.gui.Font font, String text, int maxWidth) {
        if (text == null) return "";
        if (maxWidth <= 0 || font.width(text) <= maxWidth) return text == null ? "" : text;
        String dots = "...";
        int room = Math.max(0, maxWidth - font.width(dots));
        return font.plainSubstrByWidth(text, room) + dots;
    }

    /** Nombre y version del mod de una fila, leidos de la entrada de Forge. */
    private static String[] modNameAndVersion(Object entry) {
        try {
            Object info = entry.getClass().getMethod("getInfo").invoke(entry);
            if (info == null) return null;
            Object nameObj = info.getClass().getMethod("getDisplayName").invoke(info);
            Object verObj = info.getClass().getMethod("getVersion").invoke(info);
            String name = stripCodes(nameObj == null ? "" : nameObj.toString());
            String version = stripCodes(verObj == null ? "" : verObj.toString());
            if (name.isEmpty()) return null;
            return new String[] { name, version };
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** v0.306.0: el identificador del mod, si la entrada lo trae. */
    private static String modId(Object entry) {
        try {
            Object info = entry.getClass().getMethod("getInfo").invoke(entry);
            if (info == null) return "";
            Object id = info.getClass().getMethod("getModId").invoke(info);
            return id == null ? "" : stripCodes(id.toString());
        } catch (Throwable ignored) {
            return "";
        }
    }

    /** v0.302.0: la primera linea de la descripcion del mod, si la trae. */
    private static String modDescription(Object entry) {
        try {
            Object info = entry.getClass().getMethod("getInfo").invoke(entry);
            if (info == null) return "";
            Object desc = info.getClass().getMethod("getDescription").invoke(info);
            if (desc == null) return "";
            String text = stripCodes(desc.toString()).replace('\n', ' ').trim();
            while (text.contains("  ")) text = text.replace("  ", " ");
            return text;
        } catch (Throwable ignored) {
            return "";
        }
    }

    /** Quita los codigos de formato (el caracter 167 y lo que le sigue). */
    private static String stripCodes(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == 167) {
                i++;
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        try {
            QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
        } catch (Throwable ignored) {
        }
        drawFooterPaper(g);
        super.render(g, mouseX, mouseY, partial);
        // v0.266.0: el recuento, arriba a la derecha de la hoja.
        try {
            boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
            String count = modCount(en);
            if (!count.isEmpty() && this.width >= 420) {
                String cut = ellipsizeTo(this.font, count, Math.max(80, this.width / 3));
                int cw = this.font.width(cut);
                g.drawString(this.font, cut, this.width - 12 - cw, 6,
                    com.eternalcraft.gripeverde.client.render.DiaryChrome.INK_FLAVOR, false);
            }
            // v0.278.0: debajo, sobre que andamio esta montado el arsenal.
            if (this.width >= 420) {
                String forge = forgeLine();
                if (!forge.isEmpty()) {
                    String cut2 = ellipsizeTo(this.font, forge, Math.max(80, this.width / 3));
                    int fw = this.font.width(cut2);
                    g.drawString(this.font, cut2, this.width - 12 - fw, 17,
                        com.eternalcraft.gripeverde.client.render.DiaryChrome.INK_FLAVOR, false);
                }
            }
            // v0.278.0: la chapa del registro, en el margen izquierdo.
            // v0.301.0: la chapa caia sobre los botones A-Z del arsenal; se
            // corre al filo y se queda sola arriba.
            com.eternalcraft.gripeverde.client.render.DiaryChrome.drawRegistryTagLeft(
                g, -2, -6, "REG. XVII");
            // v0.278.0: el pie de la hoja recuerda como se sale.
            if (this.height >= 200) {
                // v0.301.0: el aviso de salida pisaba el boton de la carpeta de
                // mods; se va al margen derecho del pie.
                String hint = en ? "Esc goes back" : "Esc vuelve";
                int hw = this.font.width(hint);
                if (this.width - 12 - hw > 12) {
                    g.drawString(this.font, hint, this.width - 12 - hw, this.height - 12,
                        com.eternalcraft.gripeverde.client.render.DiaryChrome.INK_FLAVOR, false);
                }
            }
        } catch (Throwable ignored) {
        }
        // v0.265.0: la tinta va despues de Forge, para tapar su letra blanca.
        this.hoveredModDesc = "";
        paintRowsInk(g, mouseX, mouseY);
        // v0.302.0: lo que es ese mod, en una linea sobre el papel de los botones.
        try {
            if (!this.hoveredModDesc.isEmpty() && this.footerTop > 40) {
                String cut = ellipsizeTo(this.font, this.hoveredModDesc,
                    Math.max(80, this.width - 80));
                int cw = this.font.width(cut);
                int y = this.footerTop - 12;
                g.drawString(this.font, cut, (this.width - cw) / 2, y,
                    com.eternalcraft.gripeverde.client.render.DiaryChrome.INK_SUBTITLE, false);
            }
        } catch (Throwable ignored) {
        }
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
        this.footerTop = y;
        PaperTexture.drawTornSheet(g, x, y, w, h, PaperTexture.PaperKind.OFFICIAL);
        // v0.278.0: folio del arsenal, en la esquina del pie.
        try {
            if (h >= 24) {
                boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
                String folio = en ? "sheet 1" : "hoja 1";
                int fw = this.font.width(folio);
                if (x + w - fw - 8 > x + 4) {
                    // v0.302.0: el folio caia sobre el boton de la carpeta; se
                    // sube al filo superior del papel.
                    g.drawString(this.font, folio, x + w - fw - 8, y + 3,
                        com.eternalcraft.gripeverde.client.render.DiaryChrome.INK_FLAVOR, false);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onClose() {
        // v0.268.0: el arsenal tambien se cierra con ruido de puerta.
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.back();
        } catch (Throwable ignored) {
        }
        super.onClose();
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
