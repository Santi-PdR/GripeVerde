package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.lore.ChroniclerMemory;
import com.eternalcraft.gripeverde.client.lore.Discoveries;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * v0.299.0 - "El cuadro de la casa": lo que la casa ya sabe del lector, en una
 * hoja. Las mismas cifras que el archivo guarda, con un boton para copiarlas.
 */
@OnlyIn(Dist.CLIENT)
public class DiaryHouseLedgerScreen extends Screen {

    private static final int PANEL_MAX_W = 340;
    private static final int PANEL_MAX_H = 270;
    private static final int LINE_H = 14;

    private final Screen parent;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int titleY;
    private final List<String> lines = new ArrayList<>();
    private boolean copied = false;

    public DiaryHouseLedgerScreen(Screen parent) {
        super(Component.literal("El cuadro"));
        this.parent = parent;
    }

    private static boolean en() {
        return Discoveries.isEnglish();
    }

    @Override
    protected void init() {
        this.panelW = Math.min(PANEL_MAX_W, this.width - 40);
        this.panelH = Math.min(PANEL_MAX_H, this.height - 24);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;
        this.titleY = this.panelY + 14;
        this.copied = false;
        buildLines();

        int backW = 110;
        int copyW = 130;
        int gap = 8;
        int backY = this.panelY + this.panelH - 32;
        addRenderableWidget(new DiaryButton(
            (this.width - copyW - gap - backW) / 2, backY, copyW, 22,
            Component.literal(en() ? "Copy the ledger" : "Copiar el cuadro"),
            DiaryButton.Icon.SCROLL, DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.BACK,
            this::copyLedger
        ));
        addRenderableWidget(new DiaryButton(
            (this.width - copyW - gap - backW) / 2 + copyW + gap, backY, backW, 22,
            Component.translatable("gripeverde.options.button.done"),
            DiaryButton.Icon.DOOR, DiaryButton.Variant.PRIMARY, DiaryButton.SoundProfile.BACK,
            this::onClose
        ));
    }

    private void copyLedger() {
        if (this.minecraft == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("GripeVerde ").append(com.eternalcraft.gripeverde.GripeVerdeMod.VERSION).append('\n');
        for (String line : this.lines) sb.append(line).append('\n');
        this.minecraft.keyboardHandler.setClipboard(sb.toString());
        this.copied = true;
    }

    private void buildLines() {
        this.lines.clear();
        int fichas = 0;
        int objetos = 0;
        long visitas = 0L;
        int racha = 0;
        long primera = -1L;
        long ultima = -1L;
        String version = "";
        try {
            ChroniclerMemory.Data data = ChroniclerMemory.get();
            fichas = ChroniclerMemory.discoveredCount();
            objetos = ChroniclerMemory.collectibleCount();
            if (data != null) {
                visitas = data.visitCount;
                racha = data.streak;
                primera = data.firstVisitDay;
                ultima = data.lastVisitDay;
            }
            version = ChroniclerMemory.lastSeenVersion();
        } catch (Throwable ignored) {
        }
        boolean en = en();
        this.lines.add(lab(en, "fichas leidas", "files read", fichas));
        this.lines.add(lab(en, "objetos del cajon", "drawer keepsakes", objetos));
        this.lines.add(lab(en, "veces que se abrio el cuaderno", "times the notebook opened", visitas));
        this.lines.add(lab(en, "noches seguidas", "nights in a row", racha));
        this.lines.add(lab(en, "dias desde la primera vez", "days since the first time", daysBetween(primera, ultima)));
        this.lines.add(lab(en, "ultima cronica leida", "last chronicle read",
            version == null || version.isBlank() ? "-" : version));
    }

    private static long daysBetween(long primera, long ultima) {
        if (primera < 0L || ultima < 0L) return 0L;
        return Math.max(0L, ultima - primera);
    }

    private static String lab(boolean en, String es, String english, long value) {
        return (en ? english : es) + ": " + value;
    }

    private static String lab(boolean en, String es, String english, String value) {
        return (en ? english : es) + ": " + value;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
        DiaryChrome.drawPanelShadow(g, this.panelX, this.panelY, this.panelW, this.panelH);
        PaperTexture.drawTornSheet(g, this.panelX, this.panelY, this.panelW, this.panelH,
            PaperTexture.PaperKind.OFFICIAL);

        String title = en() ? "The house ledger" : "El cuadro de la casa";
        int titleW = DiaryChrome.widthCached(this.font, title);
        g.drawString(this.font, title, this.width / 2 - titleW / 2, this.titleY,
            DiaryChrome.INK_TITLE, false);
        String sub = en() ? "- what the house already knows -" : "- lo que la casa ya sabe -";
        int subW = DiaryChrome.widthCached(this.font, sub);
        g.drawString(this.font, sub, this.width / 2 - subW / 2, this.titleY + 14,
            DiaryChrome.INK_SUBTITLE, false);
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 18, this.panelX + this.panelW - 18,
            this.titleY + 40);

        int y = this.titleY + 50;
        int limit = Math.max(60, this.panelW - 36);
        for (String line : this.lines) {
            if (y + LINE_H > this.panelY + this.panelH - 44) break;
            String fitted = DiaryChrome.fitText(this.font, line, limit);
            g.drawString(this.font, fitted, this.panelX + 18, y, DiaryChrome.INK_TITLE, false);
            y += LINE_H;
        }

        if (this.copied) {
            String done = en() ? "copied" : "copiado";
            int dw = DiaryChrome.widthCached(this.font, done);
            g.drawString(this.font, done, this.panelX + this.panelW - 18 - dw,
                this.panelY + this.panelH - 44, DiaryChrome.INK_SUBTITLE, false);
        }

        super.render(g, mouseX, mouseY, partialTick);

        String folio = en() ? "sheet XX  ·  Esc back" : "hoja XX  ·  Esc vuelve";
        int folioW = DiaryChrome.widthCached(this.font, folio);
        g.drawString(this.font, folio, this.panelX + this.panelW - 12 - folioW,
            this.panelY + this.panelH - 12, DiaryChrome.INK_SUBTITLE, false);
        g.drawString(this.font, "REG. XX", this.panelX + 12, this.panelY + this.panelH - 12,
            DiaryChrome.INK_SUBTITLE, false);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
