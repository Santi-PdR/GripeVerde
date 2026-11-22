package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Version Diary de PackSelectionScreen: el "archivador de materiales".
 *
 * Reimplementacion visual sobre la pantalla vanilla:
 *  - super.init() conserva las listas y acciones originales, sin cambiar la
 *    seleccion de packs ni el callback de aplicacion.
 *  - Las dos listas se resuelven desde children() y se ordenan por su posicion
 *    real: izquierda=disponibles, derecha=aplicados. No dependen de nombres de
 *    campos ni del orden de reflexion al redimensionar la pantalla.
 *  - El fondo se reemplaza por escena viva y pergamino; listas y controles se
 *    dibujan una sola vez encima de la hoja.
 *  - Las scrollbars siguen siendo las vanilla, vestidas con tinta y papel.
 */
public class DiaryPackSelectionScreen extends PackSelectionScreen {

    // PackSelectionScreen 1.20.1 sets a 13 px header on each list.
    private static final int PACK_LIST_HEADER_HEIGHT = 13;

    private AbstractSelectionList<?> availableList;
    private AbstractSelectionList<?> selectedList;
    // Caché local y acotada: las filas viejas no sobreviven a init ni removed.
    private final java.util.IdentityHashMap<Object, CachedPackDescription> packDescriptions =
        new java.util.IdentityHashMap<>();

    private record CachedPackDescription(Component text, int width, MultiLineLabel label) {
    }
    // v0.264.0: nos quedamos con el archivador para poder contar las fichas.
    private final PackRepository repository;

    public DiaryPackSelectionScreen(PackRepository repository, Consumer<PackRepository> callback,
                                    Path packDir, Component title) {
        super(repository, callback, packDir, title);
        this.repository = repository;
    }

    /**
     * v0.264.0: cuantos materiales estan archivados de cuantos hay en el cajon.
     * El pie del archivador estaba vacio salvo el sello.
     */
    private String archiveCount(boolean english) {
        try {
            int total = this.repository == null ? 0 : this.repository.getAvailablePacks().size();
            int chosen = this.repository == null ? 0 : this.repository.getSelectedIds().size();
            if (total <= 0) return "";
            return english
                ? chosen + " of " + total + " materials filed"
                : chosen + " de " + total + " materiales archivados";
        } catch (Throwable ignored) {
            return "";
        }
    }

    // v0.272.0: la hoja solo suena la primera vez que se abre, no al redimensionar.
    private boolean paperSoundDone = false;
    private final java.util.List<com.eternalcraft.gripeverde.client.widgets.DiaryButton> dressed
        = new java.util.ArrayList<>();

    @Override
    protected void init() {
        // v0.272.0: la hoja se abre con ruido de papel, una vez por apertura.
        if (!this.paperSoundDone) {
            this.paperSoundDone = true;
            try {
                com.eternalcraft.gripeverde.client.audio.UiSounds.open();
            } catch (Throwable ignored) {
            }
        }
        this.availableList = null;
        this.selectedList = null;
        this.dressed.clear();
        this.packDescriptions.clear();
        super.init();
        resolvePackLists();
        if (availableList != null) {
            availableList.setRenderBackground(false);
            availableList.setRenderTopAndBottom(false);
        }
        if (selectedList != null) {
            selectedList.setRenderBackground(false);
            selectedList.setRenderTopAndBottom(false);
        }
        // v0.311.0: los botones del archivador se visten como los de la casa.
        try {
            this.dressed.addAll(com.eternalcraft.gripeverde.client.render.PaperButtons.dressAll(this));
        } catch (Throwable ignored) {
        }
    }

    /**
     * Las listas vanilla están en children(), aunque no en renderables. Se
     * ordenan por su x real (izquierda=disponibles, derecha=aplicados), no por
     * el orden de campos/reflexión, que puede cambiar al reiniciar la pantalla.
     */
    private void resolvePackLists() {
        java.util.List<AbstractSelectionList<?>> lists = new java.util.ArrayList<>();
        try {
            for (var child : this.children()) {
                if (child instanceof AbstractSelectionList<?> list) lists.add(list);
            }
        } catch (Throwable ignored) {
        }
        lists.sort((left, right) -> Integer.compare(left.getLeft(), right.getLeft()));
        if (!lists.isEmpty()) this.availableList = lists.get(0);
        if (lists.size() > 1) this.selectedList = lists.get(1);
    }

    /**
     * v0.372.0: los nombres y descripciones de los packs en tinta.
     *
     * El juego los pinta en blanco sobre su fondo oscuro; puestos sobre el
     * pergamino quedaban invisibles. Se cubre la franja de texto de la lista,
     * a partir del icono de 32 px (que también aloja las flechas vanilla), y se
     * reescribe el titulo y la descripcion extendida en tinta.
     */
    private void inkPackRows(GuiGraphics g, AbstractSelectionList<?> list, int mouseX, int mouseY) {
        if (g == null || list == null) return;
        int top = list.getTop();
        int bottom = list.getBottom();
        int left = list.getRowLeft();
        int right = left + list.getRowWidth();
        if (bottom - top < 24 || right - left < 90) return;
        // Las flechas vanilla viven sobre el icono de 32 px, no a la derecha.
        // Se tapa desde el fin de ese icono hasta el fin del texto nativo.
        int x1 = left + 33;            // fuera del hitbox incluyente del icono
        int textX = left + 34;
        int x2 = right - 8;
        int textW = x2 - textX - 1;     // 157 px con la fila vanilla de 200 px
        if (textW < 60) return;
        int paper = PaperTexture.paperColor(PaperTexture.PaperKind.ARCHIVE);
        // Resolver geometría y entradas antes de adquirir el recorte gráfico.
        int step = packRowStep(list);
        double scroll = 0.0;
        try {
            scroll = list.getScrollAmount();
        } catch (Throwable ignored) {
        }
        int count = 0;
        java.util.List<Object> children = java.util.List.of();
        try {
            @SuppressWarnings("unchecked")
            java.util.List<Object> kids = (java.util.List<Object>) (java.util.List<?>) list.children();
            children = kids;
            count = children.size();
        } catch (Throwable ignored) {
        }
        int first = DiaryChrome.firstVisibleRow(list, count, top, step, scroll);
        // No tapar la letra vanilla ni dibujar filas si no se pudo activar el
        // recorte. El llamador maneja el fallo; no continuar sin protección.
        g.flush();
        g.enableScissor(x1, top, x2, bottom);
        try {
            g.fill(x1, top, x2, bottom,
                com.eternalcraft.gripeverde.client.render.DiaryTheme.withAlpha(paper, 255));
            for (int i = first; i < count; i++) {
                int measuredTop = com.eternalcraft.gripeverde.client.render.DiaryChrome
                    .rowTopOf(list, i);
                int rowTop = measuredTop != Integer.MIN_VALUE
                    ? measuredTop
                    : top + 4 - (int) scroll + i * step + PACK_LIST_HEADER_HEIGHT;
                if (rowTop > bottom) break;
                int rowBottom = rowTop + step - 4;
                if (rowBottom < top) continue;
                int drawTop = Math.max(rowTop, top);
                int drawBottom = Math.min(rowBottom, bottom);
                if (drawBottom <= drawTop) continue;

                Object child = children.get(i);
                String title = "";
                Component description = Component.empty();
                boolean chosen = false;
                Component compatibilityDescription = null;
                // v0.382.0: la fila del juego no ES una entrada del modelo, la
                // lleva dentro. Con el instanceof directo no se reconocia ninguna
                // fila y los nombres seguian en blanco bajo el papel.
                net.minecraft.client.gui.screens.packs.PackSelectionModel.Entry entry =
                    asPackEntry(child);
                if (entry != null) {
                    try {
                        Component t = entry.getTitle();
                        title = t == null ? "" : t.getString();
                    } catch (Throwable ignored) {
                    }
                    try {
                        Component d = entry.getExtendedDescription();
                        description = d == null ? Component.empty() : d;
                    } catch (Throwable ignored) {
                    }
                    try {
                        chosen = entry.isSelected();
                    } catch (Throwable ignored) {
                    }
                    // La banda opaca también borra el aviso que vanilla acaba
                    // de pintar. Reponerlo con sus condiciones de hover/foco.
                    if (showPackCompatibilityWarning(list, child, entry, mouseX, mouseY)) {
                        title = Component.translatable("pack.incompatible").getString();
                        compatibilityDescription = entry.getCompatibility().getDescription();
                    }
                }
                if (title.isEmpty()) continue;
                if (chosen) {
                    g.fill(x1, drawTop, x1 + 3, drawBottom, 0xFF8C2A1E);
                }
                // v0.385.0: el brillo de la fila bajo la mano se perdia: la tira
                // de papel opaca que tapa el texto del juego lo dejaba debajo.
                // Se vuelve a pintar aqui, despues del papel y antes de la letra.
                boolean over = mouseX >= left - 9 && mouseX <= right + 9
                    && mouseY >= drawTop && mouseY < drawBottom;
                if (over) {
                    g.fill(Math.max(x1, left - 9), drawTop, Math.min(x2, right + 9),
                        drawBottom, 0x22F3E7C8);
                }
                // El texto sigue a la fila, no al borde recortado del viewport.
                // El scissor oculta los glifos fuera de pantalla sin desplazarlos.
                g.drawString(this.font, DiaryChrome.fitCached(this.font, title, textW),
                    textX, rowTop + 1, compatibilityDescription != null ? 0xFF8C2A1E : 0xFF1A1108, false);
                Component shownDescription = compatibilityDescription != null
                    ? compatibilityDescription : description;
                packDescription(child, shownDescription, textW)
                    .renderLeftAligned(g, textX, rowTop + 12, 10,
                        compatibilityDescription != null ? 0xFF8C2A1E : 0xFF6B5533);
                g.fill(x1 + 3, rowBottom - 1, x2 - 3, rowBottom, 0x2E6B5533);
            }
        } finally {
            g.disableScissor();
        }
    }

    /** Dos líneas como vanilla; recalcular solo si cambia texto, estilo o ancho. */
    private MultiLineLabel packDescription(Object child, Component text, int width) {
        CachedPackDescription cached = this.packDescriptions.get(child);
        if (cached != null && cached.width() == width && cached.text().equals(text)) {
            return cached.label();
        }
        if (this.packDescriptions.size() >= 256) this.packDescriptions.clear();
        MultiLineLabel label = MultiLineLabel.create(this.font, text, width, 2);
        this.packDescriptions.put(child, new CachedPackDescription(text.copy(), width, label));
        return label;
    }

    @Override
    public void removed() {
        this.packDescriptions.clear();
        super.removed();
    }

    /** Mismas condiciones visuales que PackEntry.render de vanilla 1.20.1. */
    private boolean showPackCompatibilityWarning(AbstractSelectionList<?> list, Object child,
            net.minecraft.client.gui.screens.packs.PackSelectionModel.Entry entry,
            int mouseX, int mouseY) {
        if (entry.getCompatibility().isCompatible()) return false;
        if (entry.isFixedPosition() && entry.isRequired()) return false;
        boolean hovered = list.isMouseOver(mouseX, mouseY)
            && child instanceof GuiEventListener row && row.isMouseOver(mouseX, mouseY);
        return this.minecraft.options.touchscreen().get() || hovered
            || (list.getSelected() == child && list.isFocused());
    }

    /**
     * Pinta listas y controles una sola vez, ya encima de la hoja.
     * Las listas no son parte de Screen.renderables en esta pantalla vanilla;
     * se recorren explicitamente y se omiten si alguna version las agrega ahi.
     */
    private void renderContentsOnPage(GuiGraphics g, int mouseX, int mouseY, float partial) {
        for (AbstractSelectionList<?> list : java.util.Arrays.asList(
                this.availableList, this.selectedList)) {
            if (list == null) continue;
            try {
                list.render(g, mouseX, mouseY, partial);
            } catch (Throwable ignored) {
            }
        }
        for (var renderable : this.renderables) {
            if (renderable == this.availableList || renderable == this.selectedList) continue;
            try {
                renderable.render(g, mouseX, mouseY, partial);
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * v0.382.0: saca la entrada del modelo de una fila del archivador. Primero
     * se prueba el tipo directo y, si no, se busca dentro la entrada -la fila
     * del juego la guarda en un campo-, sin depender de nombres ofuscados.
     */
    private static net.minecraft.client.gui.screens.packs.PackSelectionModel.Entry
            asPackEntry(Object row) {
        if (row == null) return null;
        if (row instanceof net.minecraft.client.gui.screens.packs.PackSelectionModel.Entry e) {
            return e;
        }
        for (Class<?> c = row.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                if (!net.minecraft.client.gui.screens.packs.PackSelectionModel.Entry.class
                        .isAssignableFrom(f.getType())) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    Object v = f.get(row);
                    if (v instanceof net.minecraft.client.gui.screens.packs.PackSelectionModel.Entry e) {
                        return e;
                    }
                } catch (Throwable ignored) {
                }
            }
        }
        return null;
    }

    /** v0.372.0: alto de renglon de una lista del juego, con respaldo. */
    private static int packRowStep(Object list) {
        // v0.391.0: primero se le pregunta a la lista; el nombre del campo cambia.
        int measured = com.eternalcraft.gripeverde.client.render.DiaryChrome.rowStepOf(list);
        if (measured >= 16 && measured <= 64) return measured;
        for (Class<?> c = list.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (String name : new String[] {"itemHeight", "f_93387_"}) {
                try {
                    java.lang.reflect.Field f = c.getDeclaredField(name);
                    f.setAccessible(true);
                    int v = f.getInt(list);
                    if (v >= 16 && v <= 64) return v;
                } catch (Throwable ignored) {
                }
            }
        }
        return 36;
    }

    /** v0.377.0: los dos cajones de materiales, acotados al area visible. */
    private void drawPackColumns(GuiGraphics g) {
        drawPackColumn(g, this.availableList);
        drawPackColumn(g, this.selectedList);
    }

    private void drawPackColumn(GuiGraphics g, AbstractSelectionList<?> list) {
        if (g == null || list == null) return;
        int left = list.getLeft();
        int top = list.getTop();
        int bottom = list.getBottom();
        int w = list.getWidth();
        int h = bottom - top;
        if (w < 80 || h < 60) return;
        int x1 = Math.max(2, left - 6);
        int x2 = Math.min(this.width - 2, left + w + 6);
        int y1 = Math.max(2, top - 16);
        int y2 = Math.min(this.height - 2, bottom + 6);
        if (x2 <= x1 || y2 <= y1) return;
        int paper = PaperTexture.paperColor(PaperTexture.PaperKind.ARCHIVE);
        g.fill(x1, y1, x2, y2, com.eternalcraft.gripeverde.client.render.DiaryTheme
            .withAlpha(paper, 246));
        // Canto de tinta, fino, como el de una ficha grande.
        g.fill(x1, y1, x2, y1 + 1, 0x552A1C12);
        g.fill(x1, y2 - 1, x2, y2, 0x552A1C12);
        g.fill(x1, y1, x1 + 1, y2, 0x552A1C12);
        g.fill(x2 - 1, y1, x2, y2, 0x552A1C12);
    }

    /**
     * La pasada opaca de la cabecera y la tinta de filas tapan los encabezados
     * vanilla. Los rótulos propios se pintan al final, sobre la franja reservada
     * encima de la primera fila, para que las dos columnas sigan identificadas.
     */
    private void drawPackColumnHeadings(GuiGraphics g) {
        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        drawPackColumnHeading(g, this.availableList, en ? "AVAILABLE" : "DISPONIBLES");
        drawPackColumnHeading(g, this.selectedList, en ? "APPLIED" : "ARCHIVADOS");
    }

    private void drawPackColumnHeading(GuiGraphics g, AbstractSelectionList<?> list,
                                       String heading) {
        if (g == null || list == null) return;
        int x1 = Math.max(2, list.getLeft() - 6);
        int x2 = Math.min(this.width - 2, list.getLeft() + list.getWidth() + 6);
        int top = Math.max(2, list.getTop());
        int bottom = Math.min(this.height - 2, top + PACK_LIST_HEADER_HEIGHT);
        if (x2 - x1 < 80 || bottom - top < 10) return;

        // Borra el encabezado blanco del juego sin invadir la primera fila;
        // el pixel de cada borde lateral queda intacto.
        g.fill(x1 + 1, top, x2 - 1, bottom, 0xFFE3D5AB);
        String shown = DiaryChrome.fitCached(this.font, heading, x2 - x1 - 16);
        int textWidth = DiaryChrome.widthCached(this.font, shown);
        g.drawString(this.font, shown, x1 + (x2 - x1 - textWidth) / 2,
            top + 2, DiaryChrome.INK_SUBTITLE, false);
        g.fill(x1 + 8, bottom - 1, x2 - 8, bottom, 0x2E6B5533);
    }

    /**
     * v0.311.0: el juego dibuja el titulo del archivador en blanco, y sobre el
     * papel claro no se lee. Se cubre esa franja con su recorte y se reescribe
     * en tinta, como en el resto de las hojas.
     */
    /** v0.382.0: el juego no debe pintar su tierra: la hoja la pone drawPage. */
    @Override
    public void renderBackground(GuiGraphics g) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        // No llamar super.render(): PackSelectionScreen ya dibuja listas, titulo
        // y botonera antes de que el pergamino los tape. Dejar esa primera pasada
        // producia texto doble visible a traves del papel. La escena y el papel
        // se pintan primero; despues se dibuja cada lista y control una sola vez.
        try {
            drawPage(g);
        } catch (Throwable ignored) {
        }
        g.flush();
        com.eternalcraft.gripeverde.client.render.PaperButtons.syncStates();
        try {
            renderContentsOnPage(g, mouseX, mouseY, partial);
        } catch (Throwable ignored) {
        }
        g.flush();
        // Las columnas dibujadas por drawPackColumns ya tienen marco propio;
        // no se superpone un segundo marco de lista, que duplicaba los cantos.
        try {
            if (this.availableList != null) {
                DiaryChrome.washVanillaRows(g, this.availableList);
            }
            if (this.selectedList != null) {
                DiaryChrome.washVanillaRows(g, this.selectedList);
            }
            // v0.372.0: los nombres de los packs los escribe el juego en blanco
            // fijo y sobre el papel no se leian. Se cubre la franja de texto de
            // cada lista y se reescribe en tinta y sepia, como en Mundos.
            inkPackRows(g, this.availableList, mouseX, mouseY);
            inkPackRows(g, this.selectedList, mouseX, mouseY);
        } catch (Throwable ignored) {
        }
        try {
            String title = this.title.getString();
            int limit = Math.max(80, this.width - 200);
            String cut = com.eternalcraft.gripeverde.client.render.DiaryChrome
                .fitCached(this.font, title, limit);
            int w = com.eternalcraft.gripeverde.client.render.DiaryChrome
                .widthCached(this.font, cut);
            int cx = this.width / 2;
            // v0.353.0: la ficha suelta bajo el titulo se veia como una caja
            // pegada encima de la hoja. Ahora se limpia toda la franja de
            // cabecera (de borde a borde del papel) y el titulo se escribe en
            // tinta dentro de ella, como en Multijugador.
            // v0.364.0: la franja era translucida y baja, y el titulo y el
            // subtitulo del juego se dibujaban encima y por debajo de ella: se
            // veian las dos letras superpuestas. Ahora la franja es opaca, tapa
            // los dos renglones del juego y los reescribe en tinta.
            // Asienta el subtitulo de la hoja antes de cubrir la cabecera
            // vanilla y escribir una sola pareja de titulo y subtitulo.
            g.flush();
            int bandTop = 6;
            int bandBottom = 42;
            int bandLeft = 14;
            int bandRight = this.width - 14;
            if (bandRight - bandLeft > 80 && bandBottom > bandTop + 10) {
                g.fill(bandLeft, bandTop, bandRight, bandBottom, 0xFFE3D5AB);
                g.fill(bandLeft, bandBottom - 1, bandRight, bandBottom, 0x552A1C12);
            }
            int y = bandTop + 4;
            g.drawString(this.font, cut, cx - w / 2, y,
                com.eternalcraft.gripeverde.client.render.DiaryChrome.INK_TITLE, false);
            boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
            String sub = en
                ? "Drop packs here  -  what the house wears"
                : "Suelta aqui tus recursos  -  lo que la casa se pone";
            String subCut = com.eternalcraft.gripeverde.client.render.DiaryChrome
                .fitCached(this.font, sub, Math.max(60, bandRight - bandLeft - 8));
            int subW = com.eternalcraft.gripeverde.client.render.DiaryChrome
                .widthCached(this.font, subCut);
            g.drawString(this.font, subCut, cx - subW / 2, y + 13,
                com.eternalcraft.gripeverde.client.render.DiaryChrome.INK_SUBTITLE, false);
            // Los rótulos de las columnas se restauran después de todas las
            // capas opacas que tapan el encabezado vanilla.
            g.flush();
            drawPackColumnHeadings(g);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void tick() {
        super.tick();
        QuarantineSceneRenderer.tick(1.0f / 20.0f);
    }

    /**
     * v0.382.0: la hoja del archivador. Antes vivia en renderBackground, pero
     * PackSelectionScreen pinta su propio fondo de tierra DESPUES de esa
     * llamada: la hoja se dibujaba y la tapaba la tierra, y por eso el
     * archivador salia sobre piedra. Ahora se pinta desde render(), encima de
     * todo, y despues se repiten las filas y los botones que tapa.
     */
    private void drawPage(GuiGraphics g) {
        // v0.380.0: aqui se dibujaban los cajones ANTES de la escena y de la
        // hoja grande, asi que la hoja los tapaba y el archivador salia sin
        // cajones. Ahora van al final de este metodo, encima del papel y debajo
        // de las filas que pinta el juego, que es el orden que se buscaba.
        // Escena viva detras (barata, sin foco)
        QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);

        // Hoja de archivo (blotter) sobre la que viven las dos listas
        int bx = 10;
        int by = 6;
        int bw = this.width - 20;
        int bh = this.height - 20;
        int cx = this.width / 2;
        // v0.232.0: la hoja grande bajo las dos listas ya no es un rectangulo.
        PaperTexture.drawTornSheet(g, bx, by, bw, bh, PaperTexture.PaperKind.ARCHIVE);
        DiaryChrome.drawCornerOrnaments(g, bx, by, bw, bh);

        // La cabecera (titulo y una sola indicacion de arrastre) se pinta al
        // final de render(), despues de las listas, para sustituir la de vanilla.
        DiaryChrome.drawInkDivider(g, bx + 24, by + 40, bw - 48);

        // Chapa de registro del archivador
        DiaryChrome.drawRegistryTag(g, bx, by, bw, bh, "REG. XVI");

        // Pie: sello institucional y, cuando hay ancho, el recuento del cajon.
        String footer = "EC-QV/1897";
        int fw = DiaryChrome.widthCached(this.font, footer);
        DiaryChrome.drawInkDivider(g, bx + 24, by + bh - 36, bw - 48);
        String count = archiveCount(com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish());
        boolean wide = bw >= 420 && !count.isEmpty();
        if (wide) {
            String cut = DiaryChrome.fitCached(this.font, count, Math.max(0, bw - 200));
            g.drawString(this.font, cut, bx + 24, by + bh - 30, DiaryChrome.INK_FLAVOR, false);
            g.drawString(this.font, footer, bx + bw - 24 - fw, by + bh - 30,
                DiaryChrome.INK_FLAVOR, false);
        } else {
            g.drawString(this.font, footer, cx - fw / 2, by + bh - 30, DiaryChrome.INK_FLAVOR, false);
        }

        // v0.280.0: folio del cajon y recordatorio de salida, en el pie.
        boolean enP = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        String folio = enP ? "sheet 1" : "hoja 1";
        String hint = enP ? "Esc goes back" : "Esc vuelve";
        boolean room = bw >= 360;
        // v0.311.0: si los botones vestidos llegan a esa franja, se callan.
        int topDeBoton = Integer.MAX_VALUE;
        for (com.eternalcraft.gripeverde.client.widgets.DiaryButton b : this.dressed) {
            if (b.visible) topDeBoton = Math.min(topDeBoton, b.getY());
        }
        if (topDeBoton != Integer.MAX_VALUE && by + bh - 19 + 10 > topDeBoton - 4) {
            room = false;
        }
        if (room) {
            g.drawString(this.font, folio, bx + 24, by + bh - 19, DiaryChrome.INK_FLAVOR, false);
            int hw = DiaryChrome.widthCached(this.font, hint);
            g.drawString(this.font, hint, bx + bw - 24 - hw, by + bh - 19,
                DiaryChrome.INK_FLAVOR, false);
        }

        // v0.380.0: los dos cajones del archivador, sobre la hoja y bajo las
        // filas del juego. Se dibujan aqui, al final, para que se vean.
        try {
            drawPackColumns(g);
        } catch (Throwable ignored) {
        }
    }

    // v0.272.0: cerrar la hoja suena a puerta, una vez, despues de dibujar.
    @Override
    public void onClose() {
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.back();
        } catch (Throwable ignored) {
        }
        super.onClose();
    }
}
