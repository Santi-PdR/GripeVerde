package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * DiaryCreditsScreen v2.0 - Official quarantine registry
 * Professional overhaul: looks like sealed official document with ledger, stamps, hierarchy.
 */
public class DiaryCreditsScreen extends Screen {

    private static final int PANEL_W = 360;
    private static final int PANEL_H = 320;

    private final Screen lastScreen;
    private int panelX, panelY, panelW, panelH;

    public DiaryCreditsScreen(Screen parent) {
        super(Component.translatable("gripeverde.options.credits.title"));
        this.lastScreen = parent;
    }

    // v0.272.0: la hoja solo suena la primera vez que se abre, no al redimensionar.
    private boolean paperSoundDone = false;

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
        PanelHelper.init(this);
        this.panelW = Math.min(PANEL_W, this.width - 36);
        this.panelH = Math.min(PANEL_H, this.height - 36);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;
        this.clearWidgets();

        // Ya no hay boton "Créditos" - esta pantalla ES los créditos
        // v0.296.0: la casa tiene su propia hoja con los interruptores.
        int backW = 122;
        int houseW = 122;
        int backH = 22;
        int backY = this.panelY + this.panelH - 32;
        boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        addRenderableWidget(new DiaryButton(
            (this.width - backW) / 2 - houseW - 8, backY, houseW, backH,
            Component.literal(en ? "The house" : "La casa"),
            DiaryButton.Icon.GEAR, DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.OPTION_OPEN,
            () -> this.minecraft.setScreen(new DiaryHouseScreen(this))
        ));
        addRenderableWidget(new DiaryButton(
            (this.width - backW) / 2 + 8, backY, backW, backH,
            Component.translatable("gripeverde.options.button.done"),
            DiaryButton.Icon.DOOR, DiaryButton.Variant.PRIMARY, DiaryButton.SoundProfile.BACK,
            this::onClose
        ));
    }

    @Override
    public void tick() {
        super.tick();
        QuarantineSceneRenderer.tick(1f / 20f);
    }

    // v0.202.0: textos de la cabecera (y del pie) medidos una vez.
    private net.minecraft.client.gui.Font textFont;
    private int textPanelW = -1;
    private boolean textEnglish = false;
    private String textTitleSrc = "";
    private String headerTitle = "";
    private int headerTitleW = 0;
    private String headerSubtitle = "";
    private int headerSubW = 0;
    private String headerFlavor = "";
    private int headerFlavorW = 0;
    private String versionText = "";
    private int versionW = 0;
    private String noteText = "";
    private int noteW = 0;
    private final java.util.HashMap<String, Integer> labelWidths = new java.util.HashMap<>();

    private void refreshTextCache() {
        String titleSrc = this.title.getString();
        boolean en = isEnglish();
        if (this.textFont == this.font && this.textPanelW == this.panelW
                && this.textEnglish == en && this.textTitleSrc.equals(titleSrc)) {
            return;
        }
        this.textFont = this.font;
        this.textPanelW = this.panelW;
        this.textEnglish = en;
        this.textTitleSrc = titleSrc;
        int limit = Math.max(0, this.panelW - 32);
        this.headerTitle = DiaryChrome.fitText(this.font, titleSrc, limit);
        this.headerTitleW = this.font.width(this.headerTitle);
        this.headerSubtitle = DiaryChrome.fitText(this.font,
            "- " + DiaryChrome.tr("gripeverde.options.credits.subtitle") + " -", limit);
        this.headerSubW = this.font.width(this.headerSubtitle);
        this.headerFlavor = DiaryChrome.fitText(this.font,
            DiaryChrome.tr("gripeverde.options.credits.flavor"), limit);
        this.headerFlavorW = this.font.width(this.headerFlavor);
        this.versionText = "v" + com.eternalcraft.gripeverde.GripeVerdeMod.VERSION;
        this.versionW = this.font.width(this.versionText);
        this.noteText = en ? "Sealed under quarantine act 1897"
            : "Sellado bajo acta de cuarentena 1897";
        this.noteW = this.font.width(this.noteText);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);

        DiaryChrome.drawPanelShadow(g, panelX, panelY, panelW, panelH);
        // v0.222.0: hoja rasgada, como el resto del cuaderno.
        PaperTexture.drawTornSheet(g, panelX, panelY, panelW, panelH, PaperTexture.PaperKind.OFFICIAL);
        drawOfficialFrame(g);

        int cx = this.width / 2;
        int titleY = panelY + 14;
        // v0.202.0: la cabecera se ajustaba y se medía en cada cuadro; ahora se
        // arma una vez por título, idioma y ancho de panel.
        refreshTextCache();
        g.drawString(this.font, this.headerTitle, cx - this.headerTitleW / 2, titleY, DiaryChrome.INK_TITLE, false);
        g.drawString(this.font, this.headerSubtitle, cx - this.headerSubW / 2, titleY + 14, DiaryChrome.INK_SUBTITLE, false);
        g.drawString(this.font, this.headerFlavor, cx - this.headerFlavorW / 2, titleY + 26, DiaryChrome.INK_FLAVOR, false);

        int divY1 = titleY + 44;
        DiaryChrome.drawInkDividerBetween(g, panelX + 18, panelX + panelW - 18, divY1);

        renderRegistryEntries(g, divY1 + 10);

        // v0.285.0: folio de la hoja, al pie y a la derecha.
        try {
            boolean en = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
            // v0.306.0: fuera el folio del pie: caia sobre los botones de
            // abajo y el contenido ya no deja hueco para el.

        } catch (Throwable ignored) {
        }

        int divY2 = panelY + panelH - 40;
        DiaryChrome.drawInkDividerBetween(g, panelX + 18, panelX + panelW - 18, divY2);
        DiaryChrome.drawCornerOrnaments(g, panelX, panelY, panelW, panelH);
        DiaryChrome.drawRegistryTag(g, panelX, panelY, panelW, panelH, "REG. IX");

        for (Renderable r : this.renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }

        // v0.21.26: sello de cera removido.
        try {
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, panelY - 12);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderRegistryEntries(GuiGraphics g, int startY) {
        int left = panelX + 24;
        int lineH = 11;
        int y = startY;

        // v0.21.25: nombres reales + descripcion que entra en el margen.
        y = drawSection(g, left, y, isEnglish() ? "Studio" : "Estudio", "EternalCraft");
        y += 4;
        y = drawSection(g, left, y, isEnglish() ? "Chronicler" : "Cronista", "S. Pereira");
        y = drawSection(g, left, y, isEnglish() ? "Director" : "Director", "A. Vergil");
        y += 4;
        y = drawSection(g, left, y, isEnglish() ? "City blueprints" : "Planos de la ciudad", isEnglish() ? "Streets, lamps and docks" : "Calles, faroles y muelles");
        y = drawSection(g, left, y, isEnglish() ? "Paper & ink" : "Papel y tinta", isEnglish() ? "Parchment, bindings and wax" : "Pergamino, encuadernaciones y cera");
        y += 8;

        // v0.259.0: de donde sale lo que cuenta el cuaderno. Nada de juegos: lo
        // que el diario afirma tiene fecha y lugar, y aca se anota la lista.
        y = drawSources(g, left, y);
        // v0.268.0: la hora que el cuaderno no se cansa de repetir.
        String hour = isEnglish()
            ? "the outbreak was entered at 11:52"
            : "el brote se anoto a las 11:52";
        hour = DiaryChrome.fitText(this.font, hour, Math.max(60, this.panelW - 32));
        g.drawString(this.font, hour, left, y,
            DiaryTheme.withAlpha(DiaryTheme.REDCOAT_DARK, 150), false);
        y += 11;
        y += 6;

        // v0.21.25: version del gabinete visible en creditos.
        g.drawString(this.font, this.versionText, panelX + (panelW - this.versionW) / 2, y,
            DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 100), false);
        y += 10;

        // v0.268.0: sobre que andamio se lee este cuaderno.
        String platform = "Minecraft 1.20.1  ·  Forge 47.4";
        platform = DiaryChrome.fitText(this.font, platform, Math.max(60, this.panelW - 32));
        int pw2 = DiaryChrome.widthCached(this.font, platform);
        g.drawString(this.font, platform, panelX + (panelW - pw2) / 2, y,
            DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 90), false);
        y += 10;

        // v0.292.0: cuantas fichas y cuantos objetos lleva el lector.
        try {
            int fichas = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.discoveredCount();
            int objetos = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.collectibleCount();
            String prog = isEnglish()
                ? "files read: " + fichas + "  ·  drawer keepsakes: " + objetos
                : "fichas leidas: " + fichas + "  ·  objetos del cajon: " + objetos;
            prog = DiaryChrome.fitText(this.font, prog, Math.max(60, this.panelW - 32));
            int progW = DiaryChrome.widthCached(this.font, prog);
            if (y + 10 < this.panelY + this.panelH - 34) {
                g.drawString(this.font, prog, panelX + (panelW - progW) / 2, y,
                    DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 120), false);
                y += 10;
            }
        } catch (Throwable ignored) {
        }

        // Official note
        g.drawString(this.font, this.noteText, panelX + (panelW - this.noteW) / 2, y,
            DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 130), false);
    }

    /**
     * v0.259.0: las fuentes del cuaderno, en dos renglones que entran en el
     * panel. Si el panel es angosto, cada linea se recorta con la misma medida
     * de la cabecera.
     */
    private int drawSources(GuiGraphics g, int left, int y) {
        boolean en = isEnglish();
        int limit = Math.max(40, panelW - 48);
        g.drawString(this.font, en ? "Sources of the notebook" : "Fuentes del cuaderno",
            left, y, DiaryChrome.INK_TITLE, false);
        y += 12;
        String[] lines = en
            ? new String[] {
                "Ragusa, 1377 - the forty days. Philadelphia, 1793 - the yellow fever.",
                "Hamburg, 1892 - the cholera. Porto, 1899 - the plague. Arabic ghul,",
                "One Thousand and One Nights; the cuirass of 1800's heavy cavalry."
            }
            : new String[] {
                "Ragusa, 1377 - los cuarenta dias. Filadelfia, 1793 - la fiebre amarilla.",
                "Hamburgo, 1892 - el colera. Oporto, 1899 - la peste. El ghul arabe,",
                "Las mil y una noches; la coraza de la caballeria pesada de 1800."
            };
        for (String line : lines) {
            g.drawString(this.font, DiaryChrome.fitText(this.font, line, limit), left, y,
                DiaryTheme.INK_FADED, false);
            y += 10;
        }
        return y;
    }

    private int drawSection(GuiGraphics g, int x, int y, String label, String value) {
        g.drawString(this.font, label + ":", x, y, DiaryTheme.withAlpha(DiaryTheme.INK, 210), false);
        // v0.202.0: el ancho de cada etiqueta no cambia; se mide la primera vez.
        Integer labelW = this.labelWidths.get(label);
        if (labelW == null) {
            labelW = this.font.width(label + ":  ");
            this.labelWidths.put(label, labelW);
        }
        g.drawString(this.font, value, x + labelW, y, DiaryTheme.INK_FADED, false);
        return y + 11;
    }

    private void drawOfficialFrame(GuiGraphics g) {
        int border = DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 110);
        g.fill(panelX, panelY, panelX + panelW, panelY + 1, border);
        g.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, border);
        g.fill(panelX, panelY, panelX + 1, panelY + panelH, border);
        g.fill(panelX + panelW - 1, panelY, panelX + panelW, panelY + panelH, border);
        // Inner rivets
        int rivet = DiaryTheme.withAlpha(DiaryTheme.GOLD_TARNISHED, 90);
        g.fill(panelX + 8, panelY + 8, panelX + 10, panelY + 10, rivet);
        g.fill(panelX + panelW - 10, panelY + 8, panelX + panelW - 8, panelY + 10, rivet);
        g.fill(panelX + 8, panelY + panelH - 10, panelX + 10, panelY + panelH - 8, rivet);
        g.fill(panelX + panelW - 10, panelY + panelH - 10, panelX + panelW - 8, panelY + panelH - 8, rivet);
    }

    private boolean isEnglish() {
        return com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
    }

    @Override public void renderBackground(GuiGraphics g) {}

    @Override public void onClose() {
        // v0.264.0: salir de una hoja suena a puerta, como el resto de la casa.
        try {
            com.eternalcraft.gripeverde.client.audio.UiSounds.back();
        } catch (Throwable ignored) {
        }
        this.minecraft.setScreen(this.lastScreen);
    }

    // Helper to avoid duplicate init logic
    private static final class PanelHelper {
        static void init(Screen s) {}
    }
}
