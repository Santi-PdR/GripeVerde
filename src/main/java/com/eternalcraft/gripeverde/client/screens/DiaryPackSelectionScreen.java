package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Version Diary de PackSelectionScreen: el "archivador de materiales".
 *
 * Reimplementacion visual sobre la pantalla vanilla (misma estrategia que
 * DiarySoundOptionsScreen):
 *  - super.init() crea las dos listas vanilla y los botones (Done, abrir
 *    carpeta) — NO se mueve ni se toca su logica de seleccion.
 *  - Reflexion acotada sobre las dos listas para quitarles el fondo dirt.
 *    Fallback silencioso: si falla, las listas conservan su fondo vanilla.
 *  - El fondo se reemplaza por escena viva + pergamino de archivo + marco de
 *    acero; el titulo vanilla (y=16) queda sobre el pergamino.
 *  - Las scrollbars de las listas son las vanilla, ya estilizadas en acero
 *    por el resource pack "gripeverde_steel" (widget/scroller.png).
 */
public class DiaryPackSelectionScreen extends PackSelectionScreen {

    private AbstractSelectionList<?> availableList;
    private AbstractSelectionList<?> selectedList;
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
        super.init();
        // Localizar las dos listas por nombre o por tipo (AbstractSelectionList)
        try {
            Field[] fields = PackSelectionScreen.class.getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                Object o = f.get(this);
                if (!(o instanceof AbstractSelectionList<?> list)) continue;
                if ("availableList".equals(f.getName()) || availableList == null) {
                    availableList = list;
                } else {
                    selectedList = list;
                }
            }
        } catch (Throwable ignored) {
            // fallback: listas con fondo vanilla (funciona igual)
        }
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
     * v0.311.0: el juego dibuja el titulo del archivador en blanco, y sobre el
     * papel claro no se lee. Se cubre esa franja con su recorte y se reescribe
     * en tinta, como en el resto de las hojas.
     */
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        super.render(g, mouseX, mouseY, partial);
        // v0.346.0: las dos listas del archivador se acompasan al papel, como
        // las fichas de Sonido y Accesibilidad. Solo se tine la fila: la letra
        // del juego y sus mandos quedan como estan.
        try {
            if (this.availableList != null) {
                DiaryChrome.washVanillaRows(g, this.availableList);
                DiaryChrome.washVanillaListBox(g, this.availableList);
            }
            if (this.selectedList != null) {
                DiaryChrome.washVanillaRows(g, this.selectedList);
                DiaryChrome.washVanillaListBox(g, this.selectedList);
            }
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
            int bandTop = 8;
            int bandBottom = 26;
            int bandLeft = 14;
            int bandRight = this.width - 14;
            if (bandRight - bandLeft > 80 && bandBottom > bandTop + 10) {
                g.fill(bandLeft, bandTop, bandRight, bandBottom, 0xE6E4D7AE);
                g.fill(bandLeft, bandBottom - 1, bandRight, bandBottom, 0x552A1C12);
            }
            int y = bandTop + (bandBottom - bandTop - 8) / 2;
            g.drawString(this.font, cut, cx - w / 2, y,
                com.eternalcraft.gripeverde.client.render.DiaryChrome.INK_TITLE, false);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void tick() {
        super.tick();
        QuarantineSceneRenderer.tick(1.0f / 20.0f);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        // Escena viva detras (barata, sin foco)
        QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);

        // Hoja de archivo (blotter) sobre la que viven las dos listas
        int bx = 10;
        int by = 6;
        int bw = this.width - 20;
        int bh = this.height - 20;
        // v0.232.0: la hoja grande bajo las dos listas ya no es un rectangulo.
        PaperTexture.drawTornSheet(g, bx, by, bw, bh, PaperTexture.PaperKind.ARCHIVE);
        DiaryChrome.drawCornerOrnaments(g, bx, by, bw, bh);

        // Cabecera de seccion (el titulo vanilla se dibuja en y=16 sobre el pergamino)
        int cx = this.width / 2;
        String subtitle = "- " + DiaryChrome.tr("gripeverde.options.packs.subtitle") + " -";
        subtitle = DiaryChrome.fitCached(this.font, subtitle, Math.max(0, bw - 48));
        int sw = DiaryChrome.widthCached(this.font, subtitle);
        g.drawString(this.font, subtitle, cx - sw / 2, by + 26, DiaryChrome.INK_SUBTITLE, false);
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
