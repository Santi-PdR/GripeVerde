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
import net.minecraft.network.chat.Component;

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
    // v0.270.0: lo que pesa el archivo, medido una vez por apertura.
    private static long archiveBytes = -1L;
    private static boolean archiveMeasured = false;

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
        // v0.254.0: el fondo es este bug. La lista del juego, si nadie le dice
        // lo contrario, pinta SU fondo -el de la pantalla- cada cuadro, y lo
        // pinta encima de la cabecera y del pie de papel. Por eso se veia la
        // pantalla pelada con los botones vestidos: el papel estaba debajo.
        // En multijugador ya estaba apagado; aca faltaba.
        if (this.vanillaList != null) {
            try {
                this.vanillaList.setRenderBackground(false);
                this.vanillaList.setRenderTopAndBottom(false);
            } catch (Throwable ignored) {
            }
        }
        this.searchBoxes = collectEditBoxes();
        this.dressed.clear();
        this.dressed.addAll(PaperButtons.dressAll(this));
        layoutAroundPaper();
        cacheListMetrics();
        // v0.270.0: la hoja se abre con ruido de papel.
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.open();
        } catch (Throwable ignored) {
        }
        measureArchiveAsync();
    }

    /** v0.270.0: pesa la carpeta de mundos en un hilo aparte, una sola vez. */
    private void measureArchiveAsync() {
        if (archiveMeasured) return;
        archiveMeasured = true;
        final java.nio.file.Path saves;
        try {
            saves = this.minecraft.gameDirectory.toPath().resolve("saves");
        } catch (Throwable ignored) {
            return;
        }
        Thread t = new Thread(() -> {
            long total = 0L;
            try (java.util.stream.Stream<java.nio.file.Path> walk = java.nio.file.Files.walk(saves, 6)) {
                java.util.Iterator<java.nio.file.Path> it = walk.iterator();
                int guard = 0;
                while (it.hasNext() && guard++ < 20000) {
                    java.nio.file.Path f = it.next();
                    try {
                        if (java.nio.file.Files.isRegularFile(f)) {
                            total += java.nio.file.Files.size(f);
                        }
                    } catch (Throwable ignored) {
                    }
                }
            } catch (Throwable ignored) {
            }
            archiveBytes = total;
        }, "gripeverde-archive-size");
        t.setDaemon(true);
        try {
            t.start();
        } catch (Throwable ignored) {
        }
    }

    /** v0.270.0: 1,4 MB / 812 KB, sin decimales de mas. */
    private static String formatBytes(long bytes, boolean en) {
        if (bytes < 1024L) return bytes + (en ? " B" : " B");
        double kb = bytes / 1024.0;
        if (kb < 1024.0) return String.format(java.util.Locale.ROOT, "%.0f KB", kb);
        return String.format(java.util.Locale.ROOT, "%.1f MB", kb / 1024.0);
    }

    /**
     * v0.254.0: la cabecera tiene que poder leerse.
     *
     * El juego pone su caja de busqueda a la altura de la cabecera de papel y
     * la lista arranca pegada arriba, asi que el subtitulo se cruzaba con la
     * caja. Ahora la caja baja hasta el borde de la cabecera, la lista empieza
     * debajo de ella y termina donde empiezan los botones: nada se pisa.
     */
    private void layoutAroundPaper() {
        int footerTop = this.height - 56;
        for (DiaryButton button : this.dressed) {
            footerTop = Math.min(footerTop, button.getY() - 10);
        }
        for (EditBox box : this.searchBoxes) {
            try {
                box.setY(HEADER_H + 3);
                // v0.262.0: el buscador habla como el gabinete.
                box.setSuggestion(Discoveries.isEnglish()
                    ? "search a world" : "buscar un mundo");
            } catch (Throwable ignored) {
            }
        }
        if (this.vanillaList != null) {
            try {
                int top = HEADER_H + 24;
                int bottom = Math.max(top + 24, footerTop);
                this.vanillaList.updateSize(this.width, this.height, top, bottom);
            } catch (Throwable ignored) {
            }
        }
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
        drawEmptyNote(g);
        drawKeysHint(g);

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
        DiaryChrome.drawRegistryTagLeft(g, 0, -4, "REG. XVIII");

        // v0.261.0: cuantos mundos hay anotados, a la derecha de la cabecera.
        if (w >= 380) {
            int count = 0;
            try {
                count = this.vanillaList == null ? 0 : this.vanillaList.children().size();
            } catch (Throwable ignored) {
            }
            String badge;
            if (count <= 0) badge = en ? "none yet" : "ninguno";
            else if (count == 1) badge = en ? "1 world" : "1 mundo";
            else badge = en ? count + " worlds" : count + " mundos";
            // v0.270.0: si hay filtro puesto, el recuento dice cuanto se ve.
            String filter = currentFilter();
            if (!filter.isEmpty()) {
                badge = en ? "showing " + count : "se ven " + count;
            }
            int bw = DiaryChrome.widthCached(font, badge);
            g.drawString(font, badge, w - 12 - bw, 8, DiaryChrome.INK_SUBTITLE, false);
            // v0.270.0: debajo, lo que pesa el archivo y la fecha de hoy.
            String foot = "";
            if (archiveBytes >= 0L) foot = formatBytes(archiveBytes, en);
            String date = todayStamp(en);
            if (!date.isEmpty()) foot = foot.isEmpty() ? date : foot + "  ·  " + date;
            if (!foot.isEmpty()) {
                int fw = DiaryChrome.widthCached(font, foot);
                if (w - 12 - fw > cx + 90) {
                    // v0.301.0: el peso y la fecha bajaban justo sobre el
                    // subtitulo centrado; ahora van una linea mas abajo.
                    g.drawString(font, foot, w - 12 - fw, 32, DiaryChrome.INK_FLAVOR, false);
                }
            }
            // v0.287.0: la ficha elegida, dicha en la cabecera.
            try {
                String picked = filter.isEmpty() ? selectedWorldName() : "";
                if (!picked.isEmpty()) {
                    String lineSel = (en ? "chosen: " : "elegido: ") + picked;
                    String cutSel = DiaryChrome.fitText(font, lineSel, Math.max(60, w / 2 - 20));
                    int selW = DiaryChrome.widthCached(font, cutSel);
                    if (this.height >= 220 && w / 2 - selW / 2 - 10 > 12) {
                        g.drawString(font, cutSel, w / 2 - selW / 2, 44, DiaryChrome.INK_FLAVOR, false);
                    }
                }
            } catch (Throwable ignored) {
            }
            if (!filter.isEmpty()) {
                String what = (en ? "search: " : "busca: ") + filter;
                String cut = DiaryChrome.fitText(font, what, Math.max(60, w - 40));
                int tw = DiaryChrome.widthCached(font, cut);
                g.drawString(font, cut, (w - tw) / 2, 33, DiaryChrome.INK_FLAVOR, false);
            }
        }
    }

    /** v0.287.0: el nombre de la ficha elegida, o vacio si no hay ninguna. */
    private String selectedWorldName() {
        try {
            if (this.vanillaList == null) return "";
            var entry = this.vanillaList.getSelected();
            if (entry == null) return "";
            // El renglon del juego guarda el nombre del mundo en su LevelSummary.
            for (java.lang.reflect.Field f : entry.getClass().getDeclaredFields()) {
                if (!f.getType().getSimpleName().equals("LevelSummary")) continue;
                f.setAccessible(true);
                Object summary = f.get(entry);
                if (summary == null) continue;
                Object name = summary.getClass().getMethod("getLevelName").invoke(summary);
                return name == null ? "" : name.toString();
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    /** v0.270.0: lo que hay escrito en el buscador del juego, recortado. */
    private String currentFilter() {
        for (EditBox box : this.searchBoxes) {
            try {
                if (box == null || !box.isVisible()) continue;
                String v = box.getValue();
                if (v != null && !v.isBlank()) {
                    v = v.trim();
                    return v.length() > 24 ? v.substring(0, 24) : v;
                }
            } catch (Throwable ignored) {
            }
        }
        return "";
    }

    /** v0.270.0: la fecha de hoy en numeros romanos, como el registro. */
    private static String todayStamp(boolean en) {
        String[] roman = {"I","II","III","IV","V","VI","VII","VIII","IX","X","XI","XII"};
        try {
            java.util.Calendar c = java.util.Calendar.getInstance();
            int d = c.get(java.util.Calendar.DAY_OF_MONTH);
            int m = Math.max(0, Math.min(11, c.get(java.util.Calendar.MONTH)));
            return d + "·" + roman[m] + "·" + c.get(java.util.Calendar.YEAR);
        } catch (Throwable ignored) {
            return "";
        }
    }

    /** v0.268.0: pulsa un boton vestido por su clave de traduccion. */
    private boolean pressVanilla(String translationKey) {
        String want = Component.translatable(translationKey).getString();
        for (DiaryButton button : this.dressed) {
            if (button.visible && button.active && want.equals(button.getMessage().getString())) {
                button.onPress();
                return true;
            }
        }
        return false;
    }

    /**
     * v0.261.0: al pie, entre la lista y los botones, el recordatorio de teclas.
     * Se escribe solo si hay banda libre y se recorta al ancho del papel.
     */
    private void drawKeysHint(GuiGraphics g) {
        if (this.dressed.isEmpty() || this.height < 220) return;
        int bottom = Integer.MAX_VALUE;
        for (DiaryButton button : this.dressed) bottom = Math.min(bottom, button.getY());
        int y = bottom - 12;
        if (y <= this.listBottom + 8) return;
        boolean en = Discoveries.isEnglish();
        String hint = en ? "Enter opens  ·  Del deletes  ·  Esc back"
            : "Enter entra  ·  Supr borra  ·  Esc vuelve";
        String cut = DiaryChrome.fitText(this.font, hint, Math.max(60, this.width - 60));
        int hw = DiaryChrome.widthCached(this.font, cut);
        g.drawString(this.font, cut, (this.width - hw) / 2, y, DiaryChrome.INK_FLAVOR, false);
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
            // v0.301.0: la banda era casi del color del papel y las lineas
            // grises de cada mundo (version, fecha, modo) desaparecian.
            g.fill(this.rowLeft - 6, drawTop, this.rowRight + 6, drawBottom, 0xC82A1C12);
            g.fill(this.rowLeft - 6, drawBottom - 1, this.rowRight + 6, drawBottom, 0x55E8DCB4);
            // v0.251.0: el mundo bajo la mano se aclara, igual que en la lista
            // de posadas, para saber cual se lleva.
            if (mouseX >= this.rowLeft - 6 && mouseX <= this.rowRight + 6
                    && mouseY >= drawTop && mouseY < drawBottom) {
                g.fill(this.rowLeft - 6, drawTop, this.rowRight + 6, drawBottom, 0x2EF3E7C8);
            }
        }
    }

    /** Papel detras del grupo de botones, sea cual sea el alto que les dio el juego. */
    /** v0.269.0: si el archivo esta vacio, la pagina lo dice en vez de quedarse muda. */
    private void drawEmptyNote(GuiGraphics g) {
        if (this.vanillaList == null || this.rowLeft < 0) return;
        try {
            if (!this.vanillaList.children().isEmpty()) return;
        } catch (Throwable ignored) {
            return;
        }
        if (this.listBottom - this.listTop < 28) return;
        boolean en = Discoveries.isEnglish();
        String note = en ? "no records yet  ·  create one below"
            : "aun no hay fichas  ·  crea una abajo";
        String cut = DiaryChrome.fitText(this.font, note, Math.max(80, this.rowRight - this.rowLeft));
        int nw = DiaryChrome.widthCached(this.font, cut);
        g.drawString(this.font, cut, (this.rowLeft + this.rowRight - nw) / 2,
            (this.listTop + this.listBottom) / 2 - 4, DiaryChrome.INK_SUBTITLE, false);
    }

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
        // v0.270.0: folio del archivo, en la esquina del pie.
        if (h >= 22) {
            boolean en = Discoveries.isEnglish();
            String folio = (en ? "sheet 1" : "hoja 1");
            int fw = DiaryChrome.widthCached(this.font, folio);
            if (x + w - fw - 8 > x + 4) {
                g.drawString(this.font, folio, x + w - fw - 8, y + h - 12,
                    DiaryChrome.INK_FLAVOR, false);
            }
        }
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
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE) {
            // v0.261.0: Supr borra el mundo elegido; el que pregunta sigue
            // siendo el juego, con su confirmacion vestida.
            String want = net.minecraft.network.chat.Component.translatable("selectWorld.delete").getString();
            for (DiaryButton button : this.dressed) {
                if (button.visible && button.active && want.equals(button.getMessage().getString())) {
                    try {
                        com.eternalcraft.gripeverde.client.audio.UiSounds.clickDestroy();
                    } catch (Throwable ignored) {
                    }
                    button.onPress();
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /** v0.270.0: la rueda suena al pasar por la lista. */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        boolean handled = super.mouseScrolled(mouseX, mouseY, delta);
        if (handled && this.listTop >= 0 && this.listBottom > this.listTop
                && mouseY >= this.listTop && mouseY <= this.listBottom) {
            try {
                com.eternalcraft.gripeverde.client.audio.UiSounds.scroll();
            } catch (Throwable ignored) {
            }
        }
        return handled;
    }

    /** v0.270.0: escribir en el buscador suena a pluma. */
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        boolean handled = super.charTyped(codePoint, modifiers);
        if (handled) {
            try {
                com.eternalcraft.gripeverde.client.audio.UiSounds.type();
            } catch (Throwable ignored) {
            }
        }
        return handled;
    }

    /** v0.270.0: elegir un mundo con el raton tiene su golpe. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean onRow = button == 0 && this.listTop >= 0 && this.listBottom > this.listTop
            && this.rowLeft >= 0
            && mouseX >= this.rowLeft - 6 && mouseX <= this.rowRight + 6
            && mouseY >= this.listTop && mouseY <= this.listBottom;
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (onRow) {
            try {
                com.eternalcraft.gripeverde.client.audio.UiSounds.tab();
            } catch (Throwable ignored) {
            }
        }
        return handled;
    }

    @Override
    public void onClose() {
        // v0.270.0: se cierra con el ruido de la puerta, como el resto.
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.back();
        } catch (Throwable ignored) {
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
