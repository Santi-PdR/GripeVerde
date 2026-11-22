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
    // Vanilla WorldListEntry entra al mundo de inmediato dentro del icono de 32 px.
    private static final int WORLD_ROW_JOIN_HIT_WIDTH = 32;
    // Vanilla's entry hit-test extends 2 px left of getRowLeft() and ends 2 px
    // before getRowLeft() + getRowWidth(); keep that inset aligned with the guard.
    private static final int WORLD_ROW_VANILLA_HITBOX_INSET = 2;
    // Forge draws its experimental-warning icon in the final 36 px of a row.
    private static final int WORLD_ROW_EXPERIMENTAL_WARNING_WIDTH = 36;
    // The row guard uses monotonic elapsed time, unaffected by system-clock changes.
    private static final long WORLD_ROW_DOUBLE_CLICK_GUARD_NANOS = 650_000_000L;

    private final List<DiaryButton> dressed = new ArrayList<>();
    private WorldSelectionList vanillaList;
    private List<EditBox> searchBoxes = new ArrayList<>();
    // v0.270.0: lo que pesa el archivo, medido una vez por apertura.
    private static volatile long archiveBytes = -1L;
    // Si se reabre la hoja antes de terminar el paseo anterior, se descarta el
    // resultado viejo y se evita que siga recorriendo todos los archivos.
    private static final java.util.concurrent.atomic.AtomicLong ARCHIVE_SCAN_GENERATION
        = new java.util.concurrent.atomic.AtomicLong();
    // v0.374.0: se vuelve a medir en cada apertura, en segundo plano, para
    // que un mundo recien creado aparezca con su peso.
    private boolean archiveMeasured = false;

    // v0.243.0: la pagina de registro. Las medidas de la lista se leen una vez
    // al abrir o al redimensionar; por cuadro solo cambia el desplazamiento.
    private int rowItemHeight;
    private int rowLeft = -1;
    private int rowRight = -1;
    private int listTop = -1;
    private int listBottom = -1;
    // v0.390.0: la fila que hay bajo el puntero, preguntada a la propia lista.
    // Con el alto de renglon supuesto, un solo pixel de diferencia movia el clic
    // a la fila de al lado.
    private java.lang.reflect.Method rowTopMethod;
    private int rowStepExact;
    // v0.354.0: la hoja grande alcanza a cubrir el pie de botones.
    private boolean sheetCoversFooter = false;
    // v0.370.0: canto izquierdo y ancho del pliego, para el registro ancho.
    private int sheetLeft = -1;
    private int sheetWidth = -1;
    // v0.372.0: la foto de cada mundo, cargada una vez y guardada.
    private static final java.util.Map<String, net.minecraft.resources.ResourceLocation> WORLD_ICONS
        = new java.util.HashMap<>();
    private static final java.util.Map<String, WorldIconSize> WORLD_ICON_SIZES
        = new java.util.HashMap<>();
    private static final java.util.Map<String, WorldIconFingerprint> WORLD_ICON_FINGERPRINTS
        = new java.util.HashMap<>();
    private static final java.util.concurrent.atomic.AtomicLong WORLD_ICON_TEXTURE_IDS
        = new java.util.concurrent.atomic.AtomicLong();
    private static final java.util.Set<String> WORLD_ICONS_ABSENT = new java.util.HashSet<>();
    private static final int MAX_WORLD_ICON_BYTES = 1024 * 1024;
    private static final int MAX_WORLD_ICON_DIMENSION = 512;
    private record WorldIconSize(int width, int height) { }
    private record WorldIconFingerprint(long size,
                                        java.nio.file.attribute.FileTime modifiedTime) { }
    private record WorldIconData(byte[] bytes, int width, int height,
                                 WorldIconFingerprint fingerprint) { }
    private int iconsThisFrame;
    private boolean worldIconCachePruned;
    // Recuerda el ultimo clic real sobre una fila para detener solamente el
    // doble clic sobre el mismo punto; los mandos de la fila siguen recibiendo
    // sus propios clics aunque se pulsen enseguida.
    private Object lastForwardedEntry;
    private long lastForwardedNanos;
    // v0.374.0: lo que pesa cada mundo, medido con el archivo entero.
    private static final java.util.Map<String, Long> WORLD_SIZES = new java.util.HashMap<>();

    public DiarySelectWorldScreen(Screen parent) {
        super(parent);
    }

    @Override
    protected void init() {
        super.init();
        this.vanillaList = resolveList();
        if (!this.worldIconCachePruned) {
            this.worldIconCachePruned = pruneWorldIconCache();
        }
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
        final long scanGeneration;
        synchronized (WORLD_SIZES) {
            scanGeneration = ARCHIVE_SCAN_GENERATION.incrementAndGet();
        }
        Thread t = new Thread(() -> {
            long total = 0L;
            // v0.374.0: en el mismo paseo se apunta lo que pesa cada mundo, que
            // es lo que le faltaba a la ficha para ser una ficha entera.
            java.util.Map<String, Long> perWorld = new java.util.HashMap<>();
            try (java.util.stream.Stream<java.nio.file.Path> walk = java.nio.file.Files.walk(saves, 6)) {
                java.util.Iterator<java.nio.file.Path> it = walk.iterator();
                int guard = 0;
                while (guard++ < 20000 && scanGeneration == ARCHIVE_SCAN_GENERATION.get()
                        && it.hasNext()) {
                    java.nio.file.Path f = it.next();
                    try {
                        if (java.nio.file.Files.isRegularFile(f)) {
                            long size = java.nio.file.Files.size(f);
                            total += size;
                            java.nio.file.Path rel = saves.relativize(f);
                            if (rel.getNameCount() > 1) {
                                String world = rel.getName(0).toString();
                                perWorld.merge(world, size, Long::sum);
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                }
            } catch (Throwable ignored) {
            }
            synchronized (WORLD_SIZES) {
                if (scanGeneration != ARCHIVE_SCAN_GENERATION.get()) return;
                archiveBytes = total;
                WORLD_SIZES.clear();
                WORLD_SIZES.putAll(perWorld);
            }
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
                int bw = Math.min(260, Math.max(160, this.width - 260));
                box.setWidth(bw);
                box.setHeight(16);
                box.setX((this.width - bw) / 2);
                box.setY(sheetTop() + 8);
                // v0.262.0: el buscador habla como el gabinete.
                box.setSuggestion(Discoveries.isEnglish()
                    ? "search a world" : "buscar un mundo");
            } catch (Throwable ignored) {
            }
        }
        if (this.vanillaList != null) {
            try {
                int top = sheetTop() + 34;
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
            // v0.364.0: si el campo del juego no se deja leer, las fichas se
            // quedaban sin dibujar. El renglon de mundos mide 36 en 1.20.1.
            if (this.rowItemHeight < 16 || this.rowItemHeight > 64) this.rowItemHeight = 36;
            this.rowLeft = this.vanillaList.getRowLeft();
            this.rowRight = this.rowLeft + this.vanillaList.getRowWidth();
            this.listTop = this.vanillaList.getTop();
            this.listBottom = this.vanillaList.getBottom();
        } catch (Throwable ignored) {
        }
        resolveRowTopMethod();
        if (this.rowStepExact >= 16 && this.rowStepExact <= 64) {
            this.rowItemHeight = this.rowStepExact;
        }
    }

    /**
     * v0.390.0: el metodo de la lista que dice donde empieza cada fila.
     *
     * Recibe un indice y devuelve una coordenada; se prueba con 0 y con 1 y la
     * diferencia tiene que dar el alto de renglon. Asi el clic y el dibujo usan
     * la misma geometria que el juego, sin suponer alturas.
     */
    private void resolveRowTopMethod() {
        this.rowTopMethod = null;
        this.rowStepExact = 0;
        if (this.vanillaList == null) return;
        java.lang.reflect.Method best = null;
        int bestValue = Integer.MAX_VALUE;
        int bestStep = 0;
        for (Class<?> c = this.vanillaList.getClass(); c != null && c != Object.class;
             c = c.getSuperclass()) {
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                if (m.getParameterCount() != 1) continue;
                if (m.getParameterTypes()[0] != int.class || m.getReturnType() != int.class) continue;
                if (java.lang.reflect.Modifier.isStatic(m.getModifiers())) continue;
                try {
                    m.setAccessible(true);
                    int a = (Integer) m.invoke(this.vanillaList, 0);
                    int b = (Integer) m.invoke(this.vanillaList, 1);
                    int step = b - a;
                    if (step < 10 || step > 80) continue;
                    if (a < bestValue) {
                        bestValue = a;
                        best = m;
                        bestStep = step;
                    }
                } catch (Throwable ignored) {
                }
            }
        }
        this.rowTopMethod = best;
        if (bestStep > 0) this.rowStepExact = bestStep;
    }

    /** Donde empieza la fila `i`, segun la propia lista. */
    private int rowTopOf(int i) {
        if (this.rowTopMethod == null || this.vanillaList == null) return Integer.MIN_VALUE;
        try {
            return (Integer) this.rowTopMethod.invoke(this.vanillaList, i);
        } catch (Throwable ignored) {
            return Integer.MIN_VALUE;
        }
    }

    /**
     * v0.392.0: una sola cuenta para todos los renglones del registro.
     * La lista devuelve el tope absoluto (incluidos scroll y cabecera); si no
     * se puede invocar el metodo descubierto, se conserva el respaldo antiguo.
     */
    private int rowTopAt(int index, double scroll) {
        int measured = rowTopOf(index);
        if (measured != Integer.MIN_VALUE) return measured;
        return this.listTop + 4 - (int) scroll + index * rowStep();
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
                if (child instanceof EditBox box && box.visible) {
                    // v0.356.0: el buscador pierde su caja negra y pasa a papel.
                    DiaryChrome.dressEditBox(box);
                    boxes.add(box);
                }
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

        // v0.352.0: si la lista se resolvio tarde (o el campo del juego cambio
        // de nombre), las medidas quedaban en -1 y la hoja no se dibujaba: se
        // veian las filas del juego sueltas sobre la noche. Se reintenta aqui.
        if (this.vanillaList == null) {
            this.vanillaList = resolveList();
        }
        if (this.rowLeft < 0 || this.listBottom <= 0) {
            cacheListMetrics();
        }
        drawHeader(g);
        drawRegisterPage(g, mouseX, mouseY);
        drawFooterPaper(g);
        // v0.302.0: la ficha del mundo elegido, con lo que el juego ya sabia y
        // no se veia: el modo y cuando se jugo por ultima vez.
        drawSelectedStrip(g);
        drawKeysHint(g);

        // Lista y busqueda del juego, tal cual, encima de la pagina.
        try {
            if (this.vanillaList != null) {
                this.vanillaList.render(g, mouseX, mouseY, partial);
                // v0.364.0: hoja opaca encima del fondo del juego y, sobre ella,
                // las fichas de la casa.
                paintListPaper(g, mouseX, mouseY);
                // v0.304.0: nombre y ficha de cada mundo, en tinta y legibles.
                paintWorldRowsInk(g);
                // v0.339.0: el juego dibuja su barra gris y su canal negro a la
                // derecha de la lista, y eso partia la hoja en vertical. Se
                // repinta el margen en papel y la aguja pasa a ser de tinta.
                paintScrollMargin(g);
                // v0.364.0: la nota de archivo vacio va despues del papel, si no
                // la tapaba la hoja pegada encima de la lista.
                drawEmptyNote(g);
            }
            for (EditBox box : this.searchBoxes) {
                if (box.visible) {
                    DiaryChrome.paperBehindEditBox(g, box);
                    box.render(g, mouseX, mouseY, partial);
                }
            }
        } catch (Throwable ignored) {
        }

        PaperButtons.syncStates();
        for (DiaryButton button : this.dressed) {
            if (button.visible) button.render(g, mouseX, mouseY, partial);
        }

        // v0.388.0: red de seguridad. Esta hoja no llama a super.render: dibuja a
        // mano las fichas vestidas y el buscador. Si algun control del juego no
        // se pudo vestir, quedaba visible para el raton pero NUNCA dibujado: un
        // boton invisible que igual responde. Aqui se dibuja lo que falte.
        try {
            for (var child : this.children()) {
                if (child == this.vanillaList) continue;
                if (!(child instanceof net.minecraft.client.gui.components.AbstractWidget w)) continue;
                if (!w.visible) continue;
                if (this.dressed.contains(w) || this.searchBoxes.contains(w)) continue;
                w.render(g, mouseX, mouseY, partial);
            }
        } catch (Throwable ignored) {
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
        // v0.339.0: el folio del archivo, en el margen izquierdo de la hoja.
        if (w >= 360) {
            String folio = en ? "sheet 1" : "hoja 1";
            g.drawString(font, folio, 12, 26, DiaryChrome.INK_FLAVOR, false);
        }

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
            // v0.339.0: el recuento, el peso y la fecha caben en una sola
            // linea. Antes el peso caia sobre el canto roto de la cabecera y se
            // leia cortado.
            String foot = "";
            if (archiveBytes >= 0L) foot = formatBytes(archiveBytes, en);
            String date = todayStamp(en);
            if (!date.isEmpty()) foot = foot.isEmpty() ? date : foot + "  ·  " + date;
            String badgeLine = foot.isEmpty() ? badge : badge + "  ·  " + foot;
            String fitted = DiaryChrome.fitText(font, badgeLine,
                Math.max(60, w / 2 - 30));
            int bw = DiaryChrome.widthCached(font, fitted);
            g.drawString(font, fitted, w - 12 - bw, 8, DiaryChrome.INK_SUBTITLE, false);
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

    /**
     * v0.304.0: el juego escribe el nombre y la ficha de cada mundo en blanco y
     * gris fijos; sobre el papel claro no se leian. Se cubre solo la franja de
     * texto -la foto del mundo se respeta- y se reescribe en tinta y sepia.
     */
    private void paintWorldRowsInk(GuiGraphics g) {
        if (this.vanillaList == null || this.rowItemHeight <= 0) return;
        try {
            var children = this.vanillaList.children();
            if (children == null || children.isEmpty()) return;
            int listTop = this.vanillaList.getTop();
            int listBottom = this.vanillaList.getBottom();
            int x1 = this.ledgerLeft();
            int x2 = this.ledgerRight();
            double scroll = 0.0;
            try { scroll = this.vanillaList.getScrollAmount(); } catch (Throwable ignored) {}
            boolean scissored = false;
            try {
                g.enableScissor(Math.max(0, x1), listTop, Math.min(this.width, x2), listBottom);
                scissored = true;
            } catch (Throwable ignored) {
            }
            // v0.372.0: cada ficha lleva la foto del mundo a la izquierda, como
            // las hojas pegadas del archivador.
            int numRight = this.numColumnRight();
            int photoX = numRight + 6;
            int photoSize = 28;
            int dateLeft = x2 - 96;
            int textLeft = photoX + photoSize + 8;
            boolean withPhoto = dateLeft - textLeft >= 90;
            if (!withPhoto) textLeft = numRight + 10;
            int nameRight = (dateLeft > textLeft + 60 ? dateLeft : x2) - 10;
            int textW = Math.max(40, nameRight - textLeft);
            int dateW = Math.max(30, x2 - 10 - dateLeft);
            this.iconsThisFrame = 0;
            boolean english = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
            for (int i = 0; i < children.size(); i++) {
                Object entry = children.get(i);
                net.minecraft.world.level.storage.LevelSummary summary = summaryOf(entry);
                if (summary == null) continue;
                int entryTop = rowTopAt(i, scroll);
                int entryBottom = entryTop + rowStep();
                if (entryBottom < listTop || entryTop > listBottom) continue;
                int drawTop = Math.max(entryTop, listTop);
                int drawBottom = Math.min(entryBottom, listBottom);
                if (drawBottom - drawTop < 12) continue;

                String name = levelTitle(summary);
                if (name.isEmpty()) continue;
                String mode = levelGameMode(summary, english);
                String when = levelStamp(summary);
                String folder = levelFolder(summary);

                if (withPhoto && drawBottom - drawTop >= 26) {
                    // v0.382.0: la foto iba centrada y su marco caia sobre la
                    // raya del renglon. Sube un pixel y se ata al alto real de
                    // la fila: nunca toca la linea de abajo.
                    int photoTop = drawTop + Math.max(1, (drawBottom - drawTop - photoSize) / 2 - 2);
                    if (photoTop + photoSize > drawBottom - 2) {
                        photoTop = Math.max(drawTop + 1, drawBottom - 2 - photoSize);
                    }
                    net.minecraft.resources.ResourceLocation icon = worldIcon(folder);
                    if (icon == null || !drawWorldIcon(g, folder, icon,
                            photoX, photoTop, photoSize)) {
                        g.fill(photoX, photoTop, photoX + photoSize, photoTop + photoSize,
                            0x2A6B5533);
                    }
                    // Marco de la foto pegada.
                    g.fill(photoX - 1, photoTop - 1, photoX + photoSize + 1, photoTop, 0x556B5533);
                    g.fill(photoX - 1, photoTop + photoSize, photoX + photoSize + 1,
                        photoTop + photoSize + 1, 0x556B5533);
                    g.fill(photoX - 1, photoTop, photoX, photoTop + photoSize, 0x556B5533);
                    g.fill(photoX + photoSize, photoTop, photoX + photoSize + 1,
                        photoTop + photoSize, 0x556B5533);
                }
                // v0.370.0: el nombre a la izquierda y la fecha al otro canto
                // del registro, separados por la raya vertical; el modo va en
                // el segundo renglon, en sepia. Nada de fichas pegadas.
                g.drawString(this.font, DiaryChrome.fitText(this.font, name, textW),
                    textLeft, drawTop + 4, 0xFF1A1108, false);
                if (!when.isEmpty() && drawBottom - drawTop >= 14) {
                    String cutWhen = DiaryChrome.fitText(this.font, when, dateW);
                    int ww = DiaryChrome.widthCached(this.font, cutWhen);
                    g.drawString(this.font, cutWhen, x2 - 10 - ww, drawTop + 4, 0xFF8A6B45, false);
                }
                // v0.373.0: la descripcion del renglon: el modo y, si el
                // nombre visible no es el de la carpeta, la carpeta del mundo.
                String sub = mode;
                long weight = 0L;
                if (!folder.isEmpty()) {
                    try {
                        synchronized (WORLD_SIZES) {
                            Long w = WORLD_SIZES.get(folder);
                            weight = w == null ? 0L : w;
                        }
                    } catch (Throwable ignored) {
                    }
                }
                if (weight > 0L) {
                    String size = formatBytes(weight, english);
                    sub = sub.isEmpty() ? size : sub + "  \u00b7  " + size;
                } else if (!folder.isEmpty() && !folder.equals(name)) {
                    sub = sub.isEmpty() ? folder : sub + "  \u00b7  " + folder;
                }
                if (!sub.isEmpty() && drawBottom - drawTop >= 24) {
                    g.drawString(this.font, DiaryChrome.fitText(this.font, sub, textW),
                        textLeft, drawTop + 15, 0xFF6B5533, false);
                }
            }
            if (scissored) {
                try {
                    g.disableScissor();
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /** La LevelSummary de una fila de la lista, buscada por tipo. */
    /**
     * v0.366.0: la ficha de un mundo, buscada por TIPO y no por nombre.
     *
     * Antes se comparaba `getSimpleName()` con "LevelSummary", y en el juego
     * real las clases van ofuscadas: la comparacion fallaba siempre, la ficha
     * no se dibujaba y quedaba a la vista la letra blanca del juego sobre el
     * papel. Ahora se pregunta por la clase de verdad, que el compilador
     * reescribe sola.
     */
    private static net.minecraft.world.level.storage.LevelSummary summaryOf(Object entry) {
        if (entry == null) return null;
        for (Class<?> c = entry.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                if (!net.minecraft.world.level.storage.LevelSummary.class.isAssignableFrom(f.getType())) continue;
                try {
                    f.setAccessible(true);
                    Object v = f.get(entry);
                    if (v instanceof net.minecraft.world.level.storage.LevelSummary s) return s;
                } catch (Throwable ignored) {
                }
            }
        }
        return null;
    }

    /** v0.366.0: nombre visible del mundo (o su carpeta si el juego no lo da). */
    private static String levelTitle(net.minecraft.world.level.storage.LevelSummary s) {
        try {
            String n = s.getLevelName();
            if (n != null && !n.isBlank()) return n;
        } catch (Throwable ignored) {
        }
        try {
            String n = s.getLevelId();
            if (n != null && !n.isBlank()) return n;
        } catch (Throwable ignored) {
        }
        return "";
    }

    /** v0.372.0: la carpeta del mundo, que es donde vive su icon.png. */
    private static String levelFolder(net.minecraft.world.level.storage.LevelSummary s) {
        try {
            String id = s.getLevelId();
            return id == null ? "" : id;
        } catch (Throwable ignored) {
            return "";
        }
    }

    /** v0.366.0: el modo de juego, dicho en la lengua de la casa. */
    private static String levelGameMode(net.minecraft.world.level.storage.LevelSummary s, boolean en) {
        String raw = "";
        try {
            var gm = s.getGameMode();
            if (gm != null) raw = String.valueOf(gm.getName());
        } catch (Throwable ignored) {
        }
        if (raw == null) raw = "";
        raw = raw.toLowerCase(java.util.Locale.ROOT);
        if (raw.contains("creative")) return en ? "creative" : "creativo";
        if (raw.contains("survival")) return en ? "survival" : "supervivencia";
        if (raw.contains("adventure")) return en ? "adventure" : "aventura";
        if (raw.contains("spectator")) return en ? "spectator" : "espectador";
        return raw;
    }

    /** v0.366.0: cuando se jugo por ultima vez. */
    private static String levelStamp(net.minecraft.world.level.storage.LevelSummary s) {
        try {
            long ms = s.getLastPlayed();
            return ms <= 0L ? "" : stamp(ms, false);
        } catch (Throwable ignored) {
            return "";
        }
    }

    /**
     * v0.302.0: debajo de la lista, una tira con la ficha elegida. El juego
     * enseña nombre, version, fecha y modo en el renglon, pero quedaban
     * ilegibles; aqui se dicen en tinta y en una sola linea.
     */
    private void drawSelectedStrip(GuiGraphics g) {
        if (this.vanillaList == null || this.height < 200) return;
        net.minecraft.world.level.storage.LevelSummary summary;
        try {
            summary = summaryOf(this.vanillaList.getSelected());
        } catch (Throwable ignored) {
            return;
        }
        if (summary == null) return;
        boolean en = Discoveries.isEnglish();
        String name = levelTitle(summary);
        String mode = levelGameMode(summary, en);
        String when = levelStamp(summary);
        if (name.isEmpty()) return;

        int bottomY = Integer.MAX_VALUE;
        for (DiaryButton button : this.dressed) bottomY = Math.min(bottomY, button.getY());
        // v0.371.0: la ficha va en una tarjeta pegada bajo el registro, no en
        // una linea suelta: titulo de la ficha, nombre en tinta y, debajo, el
        // modo y la fecha.
        int h = 34;
        int y = this.listBottom + 8;
        if (bottomY != Integer.MAX_VALUE && y + h > bottomY - 6) {
            y = bottomY - 6 - h;
        }
        if (y < this.listBottom + 2) return;
        int w = Math.min(330, Math.max(180, this.width - 120));
        int x = (this.width - w) / 2;
        com.eternalcraft.gripeverde.client.render.PaperTexture.drawTornSlip(g, x, y, w, h,
            0xE6D2A6, 0x422810, 242);
        String cap = en ? "CHOSEN WORLD" : "MUNDO ELEGIDO";
        int cw = DiaryChrome.widthCached(this.font, cap);
        g.drawString(this.font, cap, x + (w - cw) / 2, y + 3, 0x8A6B45, false);
        String shown = DiaryChrome.fitText(this.font, name, w - 16);
        int nw = DiaryChrome.widthCached(this.font, shown);
        g.drawString(this.font, shown, x + (w - nw) / 2, y + 13, 0xFF1A1108, false);
        StringBuilder sb = new StringBuilder();
        if (!mode.isEmpty()) sb.append(mode);
        long weight = 0L;
        try {
            String folder = levelFolder(summary);
            synchronized (WORLD_SIZES) {
                Long bytes = WORLD_SIZES.get(folder);
                weight = bytes == null ? 0L : bytes;
            }
        } catch (Throwable ignored) {
        }
        if (weight > 0L) {
            if (sb.length() > 0) sb.append("  \u00b7  ");
            sb.append(formatBytes(weight, en));
        }
        if (!when.isEmpty()) {
            if (sb.length() > 0) sb.append("  \u00b7  ");
            sb.append(en ? "last " : "ultima vez ").append(when);
        }
        if (sb.length() > 0) {
            String sub = DiaryChrome.fitText(this.font, sb.toString(), w - 16);
            int sw = DiaryChrome.widthCached(this.font, sub);
            g.drawString(this.font, sub, x + (w - sw) / 2, y + 23, 0x6B5533, false);
        }
    }

    /** v0.302.0: "nombre  ·  creativo  ·  ultima vez 18 XI 2026". */
    private String selectedWorldDetail() {
        boolean en = Discoveries.isEnglish();
        try {
            if (this.vanillaList == null) return "";
            // v0.366.0: la ficha se busca por tipo, no por nombre de clase.
            net.minecraft.world.level.storage.LevelSummary summary =
                summaryOf(this.vanillaList.getSelected());
            if (summary == null) return "";
            String name = levelTitle(summary);
            String mode = levelGameMode(summary, en);
            String when = levelStamp(summary);
            StringBuilder sb = new StringBuilder(name);
            if (!mode.isEmpty()) sb.append("  \u00b7  ").append(mode);
            if (!when.isEmpty()) {
                sb.append("  \u00b7  ").append(en ? "last " : "ultima vez ").append(when);
            }
            return sb.toString().trim();
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static String stamp(long millis, boolean en) {
        try {
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTimeInMillis(millis);
            String[] roman = {"I","II","III","IV","V","VI","VII","VIII","IX","X","XI","XII"};
            return c.get(java.util.Calendar.DAY_OF_MONTH) + " "
                + roman[Math.max(0, Math.min(11, c.get(java.util.Calendar.MONTH)))] + " "
                + c.get(java.util.Calendar.YEAR);
        } catch (Throwable ignored) {
            return "";
        }
    }

    /** v0.287.0: el nombre de la ficha elegida, o vacio si no hay ninguna. */
    private String selectedWorldName() {
        try {
            if (this.vanillaList == null) return "";
            // v0.366.0: mismo cambio que en las fichas: por tipo, no por nombre.
            return levelTitle(summaryOf(this.vanillaList.getSelected()));
        } catch (Throwable ignored) {
            return "";
        }
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
    // v0.360.0: la fecha del registro se arma una vez por minuto, no por cuadro.
    private static String stampedDay;
    private static long stampedMinute = Long.MIN_VALUE;

    private static String todayStamp(boolean en) {
        long minute = System.currentTimeMillis() / 60_000L;
        if (minute == stampedMinute && stampedDay != null) return stampedDay;
        String[] roman = {"I","II","III","IV","V","VI","VII","VIII","IX","X","XI","XII"};
        try {
            java.util.Calendar c = java.util.Calendar.getInstance();
            int d = c.get(java.util.Calendar.DAY_OF_MONTH);
            int m = Math.max(0, Math.min(11, c.get(java.util.Calendar.MONTH)));
            stampedDay = d + "\u00b7" + roman[m] + "\u00b7" + c.get(java.util.Calendar.YEAR);
            stampedMinute = minute;
            return stampedDay;
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
        String hint = en ? "Enter or the button opens  ·  Del deletes  ·  Esc back"
            : "Enter o el boton entran  ·  Supr borra  ·  Esc vuelve";
        String cut = DiaryChrome.fitText(this.font, hint, Math.max(60, this.width - 60));
        int hw = DiaryChrome.widthCached(this.font, cut);
        g.drawString(this.font, cut, (this.width - hw) / 2, y, DiaryChrome.INK_FLAVOR, false);
    }

    /**
     * v0.243.0: la lista vive sobre una pagina de registro, y cada mundo sobre
     * su banda oscura, como las fotos pegadas en la ficha. Sin la banda, el
     * texto blanco del juego se perderia contra el papel.
     */
    /** v0.364.0: alto de renglon con respaldo; el del juego no siempre se deja leer. */
    private int rowStep() {
        int step = this.rowItemHeight;
        if (step < 16 || step > 64) step = 36;
        return step;
    }

    /** v0.364.0: la hoja arranca justo bajo la cabecera y va a lo ancho, como Multijugador. */
    private int sheetTop() {
        return HEADER_H - 2;
    }

    private void drawRegisterPage(GuiGraphics g, int mouseX, int mouseY) {
        if (this.vanillaList == null || this.rowLeft < 0 || this.listBottom <= this.listTop) return;
        // v0.364.0: la hoja va a lo ancho del gabinete, con 32 px de margen a
        // cada lado, igual que la de Multijugador. Antes se ceñia a las filas y
        // quedaba un papel estrecho flotando en el centro.
        // v0.380.0: la hoja va a sangre, igual que la de Multijugador, para que
        // las dos pantallas se lean como el mismo libro. Con 32 px de margen las
        // fichas del juego -que el coloca de borde a borde- se salian del papel.
        int margin = 0;
        int px = margin;
        int pw = this.width - margin * 2;
        int py = sheetTop();
        // Baja hasta el pie de botones del juego, para que las dos filas de
        // fichas queden sobre el mismo papel.
        int footerBottom = -1;
        for (DiaryButton b : this.dressed) {
            if (b.visible) footerBottom = Math.max(footerBottom, b.getY() + b.getHeight());
        }
        // v0.366.0: tambien cuentan las fichas del juego que no se vistieron; si
        // no, la hoja terminaba encima de la segunda fila de botones.
        try {
            for (var child : this.children()) {
                if (!(child instanceof net.minecraft.client.gui.components.AbstractWidget w)) continue;
                if (!w.visible || w.getY() <= this.listTop) continue;
                footerBottom = Math.max(footerBottom, w.getY() + w.getHeight());
            }
        } catch (Throwable ignored) {
        }
        int sheetBottom = this.listBottom + 12;
        if (footerBottom > 0) sheetBottom = Math.max(sheetBottom, footerBottom + 10);
        int ph = Math.min(this.height - 2, sheetBottom) - py;
        this.sheetCoversFooter = footerBottom > 0 && ph > 0 && py + ph >= footerBottom + 6;
        if (pw < 60 || ph < 40) return;
        this.sheetLeft = px;
        this.sheetWidth = pw;
        PaperTexture.drawTornSheet(g, px, py, pw, ph, PaperTexture.PaperKind.OFFICIAL);
        if (pw >= 32 && ph >= 32) {
            PaperTexture.drawWornRim(g, px, py, pw, ph);
        }
        DiaryChrome.drawCornerOrnaments(g, px, py, pw, ph);

        // Numero de orden de cada ficha, en el margen izquierdo de la hoja.
        int step = rowStep();
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
        for (int i = 0; i < count && i < 64; i++) {
            int top = rowTopAt(i, scroll);
            int bottom = top + step - 4;
            if (bottom < this.listTop || top > this.listBottom) continue;
            int drawTop = Math.max(top, this.listTop);
            int drawBottom = Math.min(bottom, this.listBottom);
            if (drawBottom - drawTop < 12) continue;
            // v0.370.0: el realce de la fila lo pinta el registro ancho; el tic
            // de la columna estrecha caia en mitad del pliego.
            if (mouseX >= this.ledgerLeft() && mouseX <= this.ledgerRight()
                    && mouseY >= drawTop && mouseY < drawBottom) {
                g.fill(this.ledgerLeft() + 2, drawTop, this.ledgerLeft() + 4, drawBottom, 0xCC4A2810);
            }
        }
    }

    /**
     * v0.364.0: la lista del juego pinta su propio fondo de tierra y escribe en
     * blanco: sobre el papel eso se veia como un rectangulo negro pegado. Se
     * pega encima una hoja opaca y sobre ella van las fichas de la casa, de
     * modo que la pantalla queda como la de Multijugador: un solo papel.
     */
    private void paintListPaper(GuiGraphics g, int mouseX, int mouseY) {
        if (this.rowLeft < 0 || this.listBottom <= this.listTop) return;
        int x1 = this.ledgerLeft();
        int x2 = this.ledgerRight();
        if (x2 - x1 < 60) return;
        // v0.370.0: el juego deja su lista en una columna estrecha y centrada
        // (270 px), asi que el registro quedaba flotando en medio de un pliego
        // enorme. Ahora se cubre TODO el pliego con su propio papel y el
        // registro se escribe de canto a canto, como la hoja de Multijugador.
        int top = this.sheetTop() + 4;
        int bottom = Math.min(this.listBottom + 4, this.height - 2);
        if (bottom - top < 24) return;
        int paper = com.eternalcraft.gripeverde.client.render.PaperTexture.paperColor(
            com.eternalcraft.gripeverde.client.render.PaperTexture.PaperKind.OFFICIAL);
        // Vuelca primero el texto del juego y lo cubre por completo: con un
        // velo translucido quedaban letras fantasma debajo de la tinta nueva.
        g.flush();
        boolean preserveForgeWarning = this.rowRight > this.rowLeft
            && this.rowRight - WORLD_ROW_EXPERIMENTAL_WARNING_WIDTH > x1
            && this.rowRight < x2;
        int warningLeft = this.rowRight - WORLD_ROW_EXPERIMENTAL_WARNING_WIDTH;
        int opaquePaper = com.eternalcraft.gripeverde.client.render.DiaryTheme
            .withAlpha(paper, 255);
        if (preserveForgeWarning) {
            // No pintar encima del icono/tooltip experimental de Forge.
            g.fill(x1, top, warningLeft, bottom, opaquePaper);
            g.fill(this.rowRight, top, x2, bottom, opaquePaper);
        } else {
            g.fill(x1, top, x2, bottom, opaquePaper);
        }
        int numRight = x1 + 34;
        // v0.370.0: los rotulos de las columnas, en el hueco entre el buscador
        // y la primera ficha: el pliego pasa a leerse como un libro de
        // registro y no como una lista suelta.
        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        int capY = this.sheetTop() + 14;
        String capNum = "N\u00b0";
        String capName = en ? "WORLD" : "MUNDO";
        String capDate = en ? "LAST PLAYED" : "ULTIMA VEZ";
        int nw = DiaryChrome.widthCached(this.font, capNum);
        g.drawString(this.font, capNum, numRight - 6 - nw, capY, 0x8A6B45, false);
        int textCapLeft = this.numColumnRight() + 42;
        g.drawString(this.font, DiaryChrome.fitText(this.font, capName,
            Math.max(40, x2 - 120 - textCapLeft)), textCapLeft, capY, 0x8A6B45, false);
        int dateLeftCap = x2 - 10;
        int dw = DiaryChrome.widthCached(this.font, capDate);
        if (dateLeftCap - dw > textCapLeft + 40) {
            g.drawString(this.font, capDate, dateLeftCap - dw, capY, 0x8A6B45, false);
        }
        // Filetes verticales: el margen de los numeros y la columna de fechas,
        // como las rayas de un libro de registro.
        int dateLeft = x2 - 96;
        int bandTop = this.listTop - 2;
        int bandBottom = Math.min(this.listBottom + 2, this.height - 2);
        if (bandBottom > bandTop + 8) {
            g.fill(numRight, bandTop, numRight + 1, bandBottom, 0x226B5533);
            if (dateLeft > numRight + 40) {
                g.fill(dateLeft, bandTop, dateLeft + 1, bandBottom, 0x226B5533);
            }
        }
        int step = rowStep();
        double scroll = 0.0;
        try {
            scroll = this.vanillaList == null ? 0.0 : this.vanillaList.getScrollAmount();
        } catch (Throwable ignored) {
        }
        int count = 0;
        try {
            count = this.vanillaList == null ? 0 : this.vanillaList.children().size();
        } catch (Throwable ignored) {
        }
        // v0.371.0: el mundo marcado lleva su cinta roja en el margen, como la
        // ficha elegida del Arsenal.
        Object chosen = null;
        try {
            chosen = this.vanillaList == null ? null : this.vanillaList.getSelected();
        } catch (Throwable ignored) {
        }
        for (int i = 0; i < count && i < 64; i++) {
            int rowTop = rowTopAt(i, scroll);
            int rowBottom = rowTop + step - 4;
            if (rowBottom < this.listTop || rowTop > this.listBottom) continue;
            int drawTop = Math.max(rowTop, this.listTop);
            int drawBottom = Math.min(rowBottom, this.listBottom);
            if (drawBottom - drawTop < 12) continue;
            boolean marked = false;
            try {
                marked = chosen != null && chosen == this.vanillaList.children().get(i);
            } catch (Throwable ignored) {
            }
            if (marked) {
                g.fill(x1 + 3, drawTop, x1 + 6, drawBottom, 0xFF8C2A1E);
            }
            // Los filetes y velos se dibujan a ambos lados del distintivo
            // experimental para no tapar el icono que Forge añade a la fila.
            int bandLeft = x1 + 6;
            int bandRight = x2 - 6;
            int beforeWarning = preserveForgeWarning ? warningLeft : bandRight;
            int afterWarning = preserveForgeWarning ? this.rowRight : bandRight;
            if ((i & 1) == 1) {
                if (beforeWarning > bandLeft) {
                    g.fill(bandLeft, drawTop, beforeWarning, drawBottom, 0x0E6B5533);
                }
                if (bandRight > afterWarning) {
                    g.fill(afterWarning, drawTop, bandRight, drawBottom, 0x0E6B5533);
                }
            }
            if (beforeWarning > bandLeft) {
                g.fill(bandLeft, drawBottom - 1, beforeWarning, drawBottom, 0x2E6B5533);
            }
            if (bandRight > afterWarning) {
                g.fill(afterWarning, drawBottom - 1, bandRight, drawBottom, 0x2E6B5533);
            }
            if (mouseX >= x1 && mouseX <= x2 && mouseY >= drawTop && mouseY < drawBottom) {
                if (beforeWarning > bandLeft) {
                    g.fill(bandLeft, drawTop, beforeWarning, drawBottom, 0x22F3E7C8);
                }
                if (bandRight > afterWarning) {
                    g.fill(afterWarning, drawTop, bandRight, drawBottom, 0x22F3E7C8);
                }
            }
            if (drawBottom - drawTop >= 14) {
                String idx = (i + 1 < 10 ? "0" : "") + (i + 1);
                int iw = DiaryChrome.widthCached(this.font, idx);
                g.drawString(this.font, idx, numRight - 6 - iw, drawTop + 4, 0x8A6B45, false);
            }
        }
    }

    /** v0.370.0: canto izquierdo del registro: el pliego menos su margen. */
    private int ledgerLeft() {
        int x = this.sheetLeft >= 0 ? this.sheetLeft + 3 : Math.max(0, this.rowLeft - 12);
        return Math.max(0, x);
    }

    /** v0.370.0: canto derecho del registro: el pliego menos su margen. */
    private int ledgerRight() {
        int x = this.sheetLeft >= 0 && this.sheetWidth > 0
            ? this.sheetLeft + this.sheetWidth - 3
            : Math.min(this.width, this.rowRight + 12);
        return Math.min(this.width, x);
    }

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
        // v0.376.0: la nota decia "aun no hay fichas" tambien cuando lo que
        // pasaba era que el buscador no encontraba nada, y eso despistaba.
        String query = "";
        for (EditBox box : this.searchBoxes) {
            try {
                if (box.isVisible()) query = box.getValue().trim();
            } catch (Throwable ignored) {
            }
        }
        String note;
        if (!query.isEmpty()) {
            note = en ? "no record answers to \"" + query + "\""
                : "ninguna ficha responde a \"" + query + "\"";
        } else {
            note = en ? "no records yet  ·  create one below"
                : "aun no hay fichas  ·  crea una abajo";
        }
        String cut = DiaryChrome.fitText(this.font, note,
            Math.max(80, this.ledgerRight() - this.ledgerLeft() - 40));
        int nw = DiaryChrome.widthCached(this.font, cut);
        g.drawString(this.font, cut,
            (this.ledgerLeft() + this.ledgerRight() - nw) / 2,
            (this.listTop + this.listBottom) / 2 - 4, DiaryChrome.INK_SUBTITLE, false);
    }

    /**
     * v0.339.0: el margen del listado. Tapa el canal y el tirador vanilla con
     * papel y dibuja una aguja fina, para que la hoja no quede cortada.
     */
    private void paintScrollMargin(GuiGraphics g) {
        if (this.vanillaList == null || this.rowRight < 0) return;
        if (this.listBottom <= this.listTop) return;
        // v0.370.0: la barra del juego caia en medio del registro ancho, como
        // una raya gris partiendo la hoja. Se tapa con el papel del pliego y la
        // aguja pasa al canto derecho del registro.
        int paper = com.eternalcraft.gripeverde.client.render.PaperTexture.paperColor(
            com.eternalcraft.gripeverde.client.render.PaperTexture.PaperKind.OFFICIAL);
        int coverL = this.rowRight + 2;
        int coverR = Math.min(this.width, this.rowRight + 18);
        if (coverR > coverL) {
            g.fill(coverL, this.listTop, coverR, this.listBottom,
                com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(paper, 255));
        }
        int tx = this.ledgerRight() - 9;
        if (tx <= coverL + 4) return;
        int viewH = this.listBottom - this.listTop;
        int maxScroll = 0;
        try {
            maxScroll = Math.max(0, this.vanillaList.getMaxScroll());
        } catch (Throwable ignored) {
        }
        if (maxScroll <= 0) {
            // Todo cabe: el carril va sin aguja.
            g.fill(tx, this.listTop + 1, tx + 2, this.listBottom - 1, 0x336B5533);
            return;
        }
        double scroll = 0.0;
        try {
            scroll = this.vanillaList.getScrollAmount();
        } catch (Throwable ignored) {
        }
        int contentH = Math.max(1, viewH + maxScroll);
        int thumbH = Math.min(viewH, Math.max(14, (int) ((long) viewH * viewH / contentH)));
        double scrollFraction = Math.max(0.0, Math.min(1.0, scroll / maxScroll));
        int thumbY = this.listTop
            + (int) ((double) (viewH - thumbH) * scrollFraction);
        g.fill(tx, this.listTop + 1, tx + 2, this.listBottom - 1, 0x556B5533);
        g.fill(tx, thumbY, tx + 2, thumbY + thumbH, 0xCC3D2A18);
    }

    /** v0.370.0: el registro es ancho, pero la lista del juego es estrecha. */
    private boolean inLedger(double mouseX, double mouseY) {
        if (this.listTop < 0 || this.listBottom <= this.listTop) return false;
        return mouseY >= this.listTop && mouseY <= this.listBottom
            && mouseX >= this.ledgerLeft() && mouseX <= this.ledgerRight();
    }

    private void releaseWorldIcon(String folder) {
        net.minecraft.resources.ResourceLocation icon = WORLD_ICONS.remove(folder);
        WORLD_ICON_SIZES.remove(folder);
        WORLD_ICON_FINGERPRINTS.remove(folder);
        if (icon != null && this.minecraft != null) {
            try {
                this.minecraft.getTextureManager().release(icon);
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * Libera iconos de mundos borrados o reemplazados. Al reabrir, los iconos
     * ausentes se vuelven a comprobar y los modificados se cargan de nuevo.
     */
    private boolean pruneWorldIconCache() {
        if (this.vanillaList == null || this.minecraft == null) return false;
        final java.nio.file.Path saves;
        try {
            saves = this.minecraft.gameDirectory.toPath().resolve("saves");
        } catch (Throwable ignored) {
            return false;
        }
        java.util.Set<String> liveWorlds = new java.util.HashSet<>();
        try {
            for (Object entry : this.vanillaList.children()) {
                net.minecraft.world.level.storage.LevelSummary summary = summaryOf(entry);
                String folder = levelFolder(summary);
                if (!folder.isEmpty()) liveWorlds.add(folder);
            }
        } catch (Throwable ignored) {
            return false;
        }
        for (String folder : new ArrayList<>(WORLD_ICONS.keySet())) {
            WorldIconFingerprint cached = WORLD_ICON_FINGERPRINTS.get(folder);
            WorldIconFingerprint current = liveWorlds.contains(folder)
                ? worldIconFingerprint(saves.resolve(folder).resolve("icon.png")) : null;
            if (cached != null && cached.equals(current)) continue;
            releaseWorldIcon(folder);
        }
        WORLD_ICONS_ABSENT.clear();
        return true;
    }

    /** Dibuja el icono entero, ajustado al recuadro sin recortar sus bordes. */
    private static boolean drawWorldIcon(GuiGraphics g, String levelId,
                                         net.minecraft.resources.ResourceLocation icon,
                                         int x, int y, int box) {
        if (g == null || levelId == null || icon == null || box <= 0) return false;
        WorldIconSize size = WORLD_ICON_SIZES.get(levelId);
        if (size == null || size.width() <= 0 || size.height() <= 0) return false;
        float fit = box / (float) Math.max(size.width(), size.height());
        int drawWidth = Math.max(1, Math.round(size.width() * fit));
        int drawHeight = Math.max(1, Math.round(size.height() * fit));
        int drawX = x + (box - drawWidth) / 2;
        int drawY = y + (box - drawHeight) / 2;
        g.pose().pushPose();
        try {
            g.pose().translate(drawX, drawY, 0.0F);
            g.pose().scale(drawWidth / (float) size.width(),
                drawHeight / (float) size.height(), 1.0F);
            g.blit(icon, 0, 0, 0, 0, size.width(), size.height(),
                size.width(), size.height());
            return true;
        } catch (Throwable ignored) {
            return false;
        } finally {
            g.pose().popPose();
        }
    }

    private static WorldIconFingerprint worldIconFingerprint(java.nio.file.Path path) {
        if (path == null) return null;
        try {
            java.nio.file.attribute.BasicFileAttributes attributes =
                java.nio.file.Files.readAttributes(path,
                    java.nio.file.attribute.BasicFileAttributes.class);
            if (!attributes.isRegularFile()) return null;
            return new WorldIconFingerprint(attributes.size(), attributes.lastModifiedTime());
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** Lee un PNG de mundo acotado y valida sus dimensiones antes de decodificarlo. */
    private static WorldIconData readWorldIconData(java.nio.file.Path path) {
        if (path == null) return null;
        try {
            WorldIconFingerprint before = worldIconFingerprint(path);
            if (before == null || before.size() < 24L
                    || before.size() > MAX_WORLD_ICON_BYTES) return null;
            byte[] bytes;
            try (java.io.InputStream in = java.nio.file.Files.newInputStream(path)) {
                bytes = in.readNBytes(MAX_WORLD_ICON_BYTES + 1);
            }
            if (bytes.length < 24 || bytes.length > MAX_WORLD_ICON_BYTES
                    || bytes.length != before.size()
                    || (bytes[0] & 0xFF) != 0x89 || bytes[1] != 'P' || bytes[2] != 'N'
                    || bytes[3] != 'G' || (bytes[4] & 0xFF) != 0x0D
                    || (bytes[5] & 0xFF) != 0x0A || (bytes[6] & 0xFF) != 0x1A
                    || (bytes[7] & 0xFF) != 0x0A || readWorldPngInt(bytes, 8) != 13
                    || bytes[12] != 'I' || bytes[13] != 'H'
                    || bytes[14] != 'D' || bytes[15] != 'R') {
                return null;
            }
            int width = readWorldPngInt(bytes, 16);
            int height = readWorldPngInt(bytes, 20);
            if (width <= 0 || height <= 0
                    || width > MAX_WORLD_ICON_DIMENSION
                    || height > MAX_WORLD_ICON_DIMENSION) {
                return null;
            }
            WorldIconFingerprint after = worldIconFingerprint(path);
            if (!before.equals(after)) return null;
            return new WorldIconData(bytes, width, height, before);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static int readWorldPngInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) | ((bytes[offset + 1] & 0xFF) << 16)
            | ((bytes[offset + 2] & 0xFF) << 8) | (bytes[offset + 3] & 0xFF);
    }

    /**
     * v0.372.0: la foto del mundo, sacada del icon.png de su carpeta.
     *
     * El juego la dibuja en su columna estrecha, que aqui queda tapada por el
     * pliego; sin ella las fichas eran solo texto. Se carga una vez por mundo y
     * se guarda como textura dinamica. Con "movimiento reducido" o si el
     * archivo falta, la ficha se queda con su marco vacio.
     */
    private net.minecraft.resources.ResourceLocation worldIcon(String levelId) {
        if (levelId == null || levelId.isEmpty()) return null;
        if (WORLD_ICONS_ABSENT.contains(levelId)) return null;
        net.minecraft.resources.ResourceLocation cached = WORLD_ICONS.get(levelId);
        if (cached != null) return cached;
        if (this.minecraft == null) return null;
        // De a pocas por cuadro: veintidos iconos de golpe son un tiron.
        if (this.iconsThisFrame >= 3) return null;
        this.iconsThisFrame++;
        try {
            java.nio.file.Path path = this.minecraft.gameDirectory.toPath()
                .resolve("saves").resolve(levelId).resolve("icon.png");
            WorldIconData source = readWorldIconData(path);
            if (source == null) {
                WORLD_ICONS_ABSENT.add(levelId);
                return null;
            }
            com.mojang.blaze3d.platform.NativeImage image;
            try (java.io.InputStream in = new java.io.ByteArrayInputStream(source.bytes())) {
                image = com.mojang.blaze3d.platform.NativeImage.read(in);
            }
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            if (imageWidth <= 0 || imageHeight <= 0
                    || imageWidth != source.width() || imageHeight != source.height()) {
                image.close();
                WORLD_ICONS_ABSENT.add(levelId);
                return null;
            }
            net.minecraft.resources.ResourceLocation rl = new net.minecraft.resources.ResourceLocation(
                "gripeverde", "world_icon/"
                    + Long.toHexString(WORLD_ICON_TEXTURE_IDS.incrementAndGet()));
            this.minecraft.getTextureManager()
                .register(rl, new net.minecraft.client.renderer.texture.DynamicTexture(image));
            WORLD_ICONS.put(levelId, rl);
            WORLD_ICON_SIZES.put(levelId, new WorldIconSize(imageWidth, imageHeight));
            WORLD_ICON_FINGERPRINTS.put(levelId, source.fingerprint());
            return rl;
        } catch (Throwable ignored) {
            WORLD_ICON_SIZES.remove(levelId);
            WORLD_ICON_FINGERPRINTS.remove(levelId);
            WORLD_ICONS_ABSENT.add(levelId);
            return null;
        }
    }

    /** v0.372.0: canto derecho del margen donde van los numeros. */
    private int numColumnRight() {
        return this.ledgerLeft() + 34;
    }

    /** v0.372.0: la entrada del registro que cae bajo una altura dada. */
    private Object entryAt(double mouseY) {
        if (this.vanillaList == null || this.listTop < 0 || this.listBottom <= this.listTop) {
            return null;
        }
        int step = rowStep();
        double scroll = 0.0;
        try {
            scroll = this.vanillaList.getScrollAmount();
        } catch (Throwable ignored) {
        }
        // AbstractSelectionList floors y, truncates scroll to int, then subtracts
        // the four-pixel row inset before dividing. Keeping the operations in that
        // order also matches vanilla while a drag leaves fractional scroll behind.
        int rowOffset = (int) Math.floor(mouseY - this.listTop) + (int) scroll - 4;
        int idx = rowOffset / Math.max(1, step);
        if (this.rowTopMethod != null) {
            // v0.390.0: con la geometria de la propia lista, el clic cae en la
            // fila que se ve aunque el juego cambie el alto o meta cabecera.
            int exact = -1;
            try {
                int total = this.vanillaList.children().size();
                int h = this.rowStepExact > 0 ? this.rowStepExact : step;
                for (int i = 0; i < total && i < 128; i++) {
                    int top = rowTopAt(i, scroll);
                    if (mouseY >= top && mouseY < top + h) {
                        exact = i;
                        break;
                    }
                }
            } catch (Throwable ignored) {
            }
            if (exact >= 0) idx = exact;
            else if (rowOffset < 0) return null;
        } else if (rowOffset < 0) {
            return null;
        }
        if (idx < 0) return null;
        try {
            var kids = this.vanillaList.children();
            return idx < kids.size() ? kids.get(idx) : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** v0.370.0: lleva el puntero a la columna de la lista, sin moverlo de fila. */
    private double ledgerX(double mouseX) {
        double lo = this.rowLeft + 2;
        double hi = Math.max(lo, this.rowRight - 2);
        return Math.min(hi, Math.max(lo, mouseX));
    }

    /** Evita la zona del icono y conserva el hitbox horizontal nativo de la fila. */
    private double rowSelectionClickX(double mouseX) {
        if (this.rowRight <= this.rowLeft) return mouseX;
        double safeLeft = this.rowLeft + WORLD_ROW_JOIN_HIT_WIDTH + 1.0;
        double safeRight = this.rowRight - WORLD_ROW_VANILLA_HITBOX_INSET;
        if (safeRight < safeLeft) return safeRight + 1.0;
        return Math.min(safeRight, Math.max(safeLeft, mouseX));
    }

    private void drawFooterPaper(GuiGraphics g) {
        if (this.dressed.isEmpty()) return;
        // v0.354.0: si la hoja grande ya llega hasta los botones, no se pega
        // otro papel encima.
        if (this.sheetCoversFooter) return;
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
        // v0.339.0: el folio se fue a la cabecera; en el pie quedaba detras de
        // las fichas y solo se leia media palabra.
    }

    /** v0.394.0: las teclas del buscador no deben actuar sobre el mundo elegido. */
    private boolean isSearchBoxFocused() {
        for (EditBox box : this.searchBoxes) {
            try {
                if (box != null && box.visible && box.isFocused()) return true;
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    /** Mueve el foco de los buscadores a la lista sin reenviar el clic a la fila. */
    private void focusWorldList() {
        for (EditBox box : this.searchBoxes) {
            if (box != null) box.setFocused(false);
        }
        if (this.vanillaList != null) {
            this.setFocused(this.vanillaList);
            this.vanillaList.setFocused(true);
        }
    }

    /**
     * v0.251.0: Enter entra al mundo elegido. El boton del juego sigue siendo
     * el que trabaja; aca solo se lo aprieta por el lector.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        PaperButtons.syncStates();
        // El campo enfocado recibe Enter/Supr para editar o aceptar su consulta;
        // los atajos de abrir y borrar un mundo no deben adelantarse al widget.
        if (isSearchBoxFocused()) return super.keyPressed(keyCode, scanCode, modifiers);
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
        // v0.374.0: las flechas recorren el registro aunque el foco este en la
        // hoja y no en la lista del juego. El buscador ya recibio sus teclas
        // arriba; aqui solo navega la lista cuando ningun campo tiene el foco.
        if (this.vanillaList != null
                && (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN
                    || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_UP
                    || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN
                    || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP
                    || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_HOME
                    || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_END)) {
            focusWorldList();
            Object before = this.vanillaList.getSelected();
            boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
            if (handled) {
                if (this.vanillaList.getSelected() != before) keepSelectedRowVisible();
                this.lastForwardedEntry = null;
                try {
                    com.eternalcraft.gripeverde.client.audio.UiSounds.tab();
                } catch (Throwable ignored) {
                }
            }
            return handled;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /** Mantiene a la vista la fila que eligio la navegacion vanilla. */
    private void keepSelectedRowVisible() {
        if (this.vanillaList == null || this.listTop < 0 || this.listBottom <= this.listTop) return;
        try {
            var kids = this.vanillaList.children();
            if (kids == null || kids.isEmpty()) return;
            Object selected = this.vanillaList.getSelected();
            int index = -1;
            for (int i = 0; i < kids.size(); i++) {
                if (kids.get(i) == selected) {
                    index = i;
                    break;
                }
            }
            if (index < 0) return;
            double scroll = this.vanillaList.getScrollAmount();
            int rowTop = rowTopAt(index, scroll);
            int rowBottom = rowTop + rowStep();
            double wanted = scroll;
            if (rowTop < this.listTop + 2) {
                wanted += this.listTop + 2 - rowTop;
            } else if (rowBottom > this.listBottom - 2) {
                wanted += rowBottom - (this.listBottom - 2);
            }
            double maxScroll = Math.max(0.0, this.vanillaList.getMaxScroll());
            if (wanted != scroll) {
                this.vanillaList.setScrollAmount(Math.max(0.0, Math.min(wanted, maxScroll)));
            }
        } catch (Throwable ignored) {
        }
    }

    /** v0.270.0: la rueda suena al pasar por la lista. */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // v0.370.0/0.372.0: la rueda funciona en todo el ancho del registro,
        // en una sola llamada para no contar el gesto dos veces.
        boolean handled = super.mouseScrolled(
            inLedger(mouseX, mouseY) ? ledgerX(mouseX) : mouseX, mouseY, delta);
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

    /** Evita que un widget superpuesto caiga por error en la fila de atras. */
    private boolean widgetAt(double mouseX, double mouseY) {
        try {
            for (var child : this.children()) {
                if (child == this.vanillaList) continue;
                if (!(child instanceof net.minecraft.client.gui.components.AbstractWidget widget)) continue;
                if (widget.visible && widget.isMouseOver(mouseX, mouseY)) return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    /** Detecta un control widget real si alguna implementacion lo integra en una fila. */
    private boolean rowControlAt(Object row, double mouseX, double mouseY) {
        if (row == null) return false;
        for (Class<?> type = row.getClass(); type != null && type != Object.class;
             type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (!net.minecraft.client.gui.components.AbstractWidget.class
                        .isAssignableFrom(field.getType())) continue;
                try {
                    field.setAccessible(true);
                    Object value = field.get(row);
                    if (value instanceof net.minecraft.client.gui.components.AbstractWidget widget
                            && widget.visible && widget.isMouseOver(mouseX, mouseY)) {
                        return true;
                    }
                } catch (Throwable ignored) {
                }
            }
        }
        // No se supone una zona de botones por geometria: en 1.20.1 Editar y
        // Borrar viven en el pie, y la franja derecha de la fila tambien puede
        // contener el aviso experimental de Forge.
        return false;
    }

    /**
     * Los botones y Enter son las unicas formas de abrir un mundo. Un doble
     * clic sobre una fila se consume; un clic rapido en otra parte de la misma
     * fila (por ejemplo Editar o Borrar) sigue llegando a su control.
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        PaperButtons.syncStates();
        if (button != 0) {
            if (widgetAt(mouseX, mouseY)) {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            if (this.vanillaList != null && this.listTop >= 0
                    && mouseY >= this.listTop && mouseY <= this.listBottom) {
                try {
                    double listLeft = this.vanillaList.getLeft();
                    double listRight = listLeft + this.vanillaList.getWidth();
                    if (mouseX >= listLeft && mouseX <= listRight) {
                        if (rowControlAt(entryAt(mouseY), mouseX, mouseY)) {
                            return super.mouseClicked(mouseX, mouseY, button);
                        }
                        return true;
                    }
                } catch (Throwable ignored) {
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (widgetAt(mouseX, mouseY)) {
            super.mouseClicked(mouseX, mouseY, button);
            return true;
        }

        // Solo un clic dentro del viewport real de la lista puede seleccionar
        // una fila. El papel y sus margenes no deben sintetizar un clic de fila.
        boolean inBody = this.vanillaList != null && this.listTop >= 0
            && this.listBottom > this.listTop
            && mouseY >= this.listTop && mouseY <= this.listBottom
            && mouseX >= this.vanillaList.getLeft()
            && mouseX <= this.vanillaList.getLeft() + this.vanillaList.getWidth();
        if (!inBody) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        // Coincide con los bordes inclusivos de AbstractSelectionList: su
        // hitbox comienza 2 px antes de rowLeft. Esos píxeles tambien pasan
        // por el guard para no activar el icono vanilla de entrada inmediata.
        if (mouseX < this.rowLeft - WORLD_ROW_VANILLA_HITBOX_INSET
                || mouseX >= this.rowRight) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        Object row = entryAt(mouseY);
        if (row == null) {
            return true;
        }

        // El clic ya está en el hitbox nativo de la fila (incluido su borde);
        // solo se aparta del icono de entrada inmediata de vanilla.
        double clickX = this.rowSelectionClickX(mouseX);

        long now = System.nanoTime();
        boolean rowControl = rowControlAt(row, mouseX, mouseY);
        if (rowControl) {
            // Los controles conservan sus coordenadas reales: nunca se les
            // desplaza el puntero para evitar el icono de entrada del mundo.
            super.mouseClicked(mouseX, mouseY, button);
            this.lastForwardedEntry = null;
            return true;
        }
        Object selected = null;
        try {
            selected = this.vanillaList == null ? null : this.vanillaList.getSelected();
        } catch (Throwable ignored) {
        }
        if (row == selected || (row == this.lastForwardedEntry
                && now - this.lastForwardedNanos < WORLD_ROW_DOUBLE_CLICK_GUARD_NANOS)) {
            // Se consume el doble clic para no abrir por sorpresa, pero el
            // buscador deja de tener el foco como en un clic normal de fila.
            focusWorldList();
            try {
                com.eternalcraft.gripeverde.client.audio.UiSounds.tab();
            } catch (Throwable ignored) {
            }
            return true;
        }

        // Solo se entrega una vez a la lista. El segundo clic sobre esa misma
        // fila se consume arriba para que nunca se entre al mundo por sorpresa.
        super.mouseClicked(clickX, mouseY, button);
        this.lastForwardedEntry = row;
        this.lastForwardedNanos = now;
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.tab();
        } catch (Throwable ignored) {
        }
        return true;
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
