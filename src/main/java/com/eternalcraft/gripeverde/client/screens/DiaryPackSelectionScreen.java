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

    public DiaryPackSelectionScreen(PackRepository repository, Consumer<PackRepository> callback,
                                    Path packDir, Component title) {
        super(repository, callback, packDir, title);
    }

    @Override
    protected void init() {
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
        PaperTexture.drawSheet(g, bx, by, bw, bh, PaperTexture.PaperKind.ARCHIVE);
        DiaryChrome.drawCornerOrnaments(g, bx, by, bw, bh);

        // Cabecera de seccion (el titulo vanilla se dibuja en y=16 sobre el pergamino)
        int cx = this.width / 2;
        String subtitle = "- " + DiaryChrome.tr("gripeverde.options.packs.subtitle") + " -";
        subtitle = DiaryChrome.fitText(this.font, subtitle, Math.max(0, bw - 48));
        int sw = this.font.width(subtitle);
        g.drawString(this.font, subtitle, cx - sw / 2, by + 26, DiaryChrome.INK_SUBTITLE, false);
        DiaryChrome.drawInkDivider(g, bx + 24, by + 40, bw - 48);

        // Chapa de registro del archivador
        DiaryChrome.drawRegistryTag(g, bx, by, bw, bh, "REG. XV");

        // Pie: sello institucional
        String footer = "EC-QV/1897";
        int fw = this.font.width(footer);
        g.drawString(this.font, footer, cx - fw / 2, by + bh - 30, DiaryChrome.INK_FLAVOR, false);
        DiaryChrome.drawInkDivider(g, bx + 24, by + bh - 36, bw - 48);
    }
}
