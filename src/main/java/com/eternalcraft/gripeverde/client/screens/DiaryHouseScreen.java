package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.lore.Discoveries;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiarySlider;
import com.eternalcraft.gripeverde.client.widgets.DiaryToggle;
import com.eternalcraft.gripeverde.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * v0.296.0 - "La casa": una hoja con los interruptores de la casa (musica,
 * ambiente, movimiento, destellos y el reloj de la partida). Son los mismos
 * valores de {@code gripeverde-client.toml}, sin salir del cuaderno.
 *
 * v0.301.0: cada boton explica lo que hace cuando la mano se le acerca, los
 * nombres caben enteros y el cuadro de cifras ya no esta.
 */
@OnlyIn(Dist.CLIENT)
public class DiaryHouseScreen extends Screen {

    private static final int PANEL_MAX_W = 360;
    private static final int PANEL_MAX_H = 320;
    private static final int TOGGLE_H = 22;
    private static final int ROW_H = 26;
    private static final int COL_GAP = 10;

    private final Screen parent;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    /** v0.316.0: paso entre renglones cuando la ventana viene baja. */
    private int rowStep = ROW_H;
    private int titleY;
    private int gridTop;
    private int toggleW;

    private final List<AbstractWidget> widgetsWithHelp = new ArrayList<>();
    private final List<String> helps = new ArrayList<>();

    public DiaryHouseScreen(Screen parent) {
        super(Component.literal("La casa"));
        this.parent = parent;
    }

    private static boolean en() {
        return Discoveries.isEnglish();
    }

    private static Component label(String es, String english) {
        return Component.literal(en() ? english : es);
    }

    private static String help(String es, String english) {
        return en() ? english : es;
    }

    @Override
    protected void init() {
        this.widgetsWithHelp.clear();
        this.helps.clear();
        this.panelW = Math.min(PANEL_MAX_W, this.width - 40);
        this.panelH = Math.min(PANEL_MAX_H, this.height - 24);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;
        this.titleY = this.panelY + 14;
        this.gridTop = this.titleY + 46;
        // v0.316.0: con la ventana baja los renglones se aprietan para que el
        // pestillo de la esquina no aterrice sobre los botones del pie.
        int backTop = this.panelY + this.panelH - 34;
        this.rowStep = Math.max(18, Math.min(ROW_H,
            (backTop - 4 - this.gridTop) / 5));
        this.toggleW = (this.panelW - 32 - COL_GAP) / 2;

        int col1 = this.panelX + 16;
        int col2 = col1 + this.toggleW + COL_GAP;

        addToggle(col1, row(0), ClientConfig.MENU_MUSIC_ENABLED,
            "Musica", "Music",
            help("Enciende o apaga el gramofono de la portada.",
                "Turns the title screen gramophone on or off."));
        addToggle(col2, row(0), ClientConfig.AMBIENCE_ENABLED,
            "Ambiente", "Ambience",
            help("Los ruidos de la casa: maderas, lluvia y pasos lejanos.",
                "The house's noises: timber, rain and faraway steps."));
        addToggle(col1, row(1), ClientConfig.REDUCED_MOTION,
            "Mov. corto", "Short motion",
            help("Menos vaiven en el ambiente y en los avisos.",
                "Less sway in the ambience and in the notices."));
        addToggle(col2, row(1), ClientConfig.REDUCE_FLASHES,
            "Destellos", "Flashes",
            help("Apaga los relampagos de la tormenta. Para ojos sensibles.",
                "Turns off storm lightning. For sensitive eyes."));
        addToggle(col1, row(2), ClientConfig.HUD_WATCH_ENABLED,
            "Reloj", "Watch",
            help("El reloj de bolsillo en la esquina, mientras se juega.",
                "The pocket watch in the corner, while playing."));
        addToggle(col2, row(2), ClientConfig.HUD_WATCH_COORDS,
            "Coords", "Coords",
            help("Anota tambien las coordenadas en ese reloj.",
                "Also writes the coordinates on that watch."));

        int cursor = row(3);
        boolean roomy = this.panelH >= 300;
        if (roomy) {
            DiarySlider music = new DiarySlider(
                col1, cursor, this.toggleW, 20, ClientConfig.MUSIC_VOLUME.get(),
                v -> (en() ? "music " : "musica ") + pct(v),
                v -> ClientConfig.MUSIC_VOLUME.set(v),
                // v0.358.0: el JSON se guarda al soltar, no en cada cuadro.
                true, () -> ClientConfig.MUSIC_VOLUME.save());
            addWithHelp(music, help("Volumen del gramofono de la portada.",
                "Volume of the title screen gramophone."));

            DiarySlider ambience = new DiarySlider(
                col2, cursor, this.toggleW, 20, ClientConfig.AMBIENCE_VOLUME.get(),
                v -> (en() ? "ambience " : "ambiente ") + pct(v),
                v -> ClientConfig.AMBIENCE_VOLUME.set(v),
                true, () -> ClientConfig.AMBIENCE_VOLUME.save());
            addWithHelp(ambience, help("Volumen de los ruidos de la casa.",
                "Volume of the house's noises."));

            cursor += 24;
            DiarySlider ui = new DiarySlider(
                col1, cursor, this.panelW - 32, 20, ClientConfig.UI_VOLUME.get(),
                v -> (en() ? "interface " : "interfaz ") + pct(v),
                v -> ClientConfig.UI_VOLUME.set(v),
                true, () -> ClientConfig.UI_VOLUME.save());
            addWithHelp(ui, help("Volumen de los sellos y las hojas al abrirse.",
                "Volume of the seals and sheets as they open."));
            cursor += 24;
        }

        // v0.316.0: y si aun asi no cabe, el pestillo se pega al pie.
        final int cornerY = Math.min(cursor, this.panelY + this.panelH - 34 - 28);
        DiaryButton corner = new DiaryButton(
            this.panelX + 16, cornerY, this.panelW - 32, TOGGLE_H,
            cornerLabel(),
            DiaryButton.Icon.GEAR, DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.OPTION_OPEN,
            () -> {
                int next = (ClientConfig.HUD_WATCH_CORNER.get() + 1) % 3;
                ClientConfig.HUD_WATCH_CORNER.set(next);
                ClientConfig.HUD_WATCH_CORNER.save();
                this.rebuildWidgets();
            });
        addWithHelp(corner, help("Donde se cuelga el reloj de la partida. Aprieta para cambiarlo.",
            "Where the in-game watch hangs. Click to change it."));

        int backW = Math.min(122, Math.max(64, (this.panelW - 24) / 2 - 6));
        int backY = this.panelY + this.panelH - 32;
        int rowX = (this.width - (backW * 2 + 10)) / 2;
        DiaryButton reset = new DiaryButton(
            rowX, backY, backW, 22,
            label("A su sitio", "Put back"),
            DiaryButton.Icon.SCROLL, DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.BACK,
            this::resetHouse);
        addWithHelp(reset, help("Devuelve pestillos, volumenes y esquina a como vienen de fabrica.",
            "Returns latches, volumes and corner to their factory values."));

        addRenderableWidget(new DiaryButton(
            rowX + backW + 10, backY, backW, 22,
            Component.translatable("gripeverde.options.button.done"),
            DiaryButton.Icon.DOOR, DiaryButton.Variant.PRIMARY, DiaryButton.SoundProfile.BACK,
            this::onClose
        ));
    }

    private void addWithHelp(AbstractWidget widget, String helpLine) {
        this.widgetsWithHelp.add(widget);
        this.helps.add(helpLine);
        addRenderableWidget(widget);
    }

    private static String pct(double value) {
        return Math.round(Math.max(0.0, Math.min(1.0, value)) * 100.0) + "%";
    }

    private int row(int index) {
        return this.gridTop + index * this.rowStep;
    }

    private Component cornerLabel() {
        int corner = ClientConfig.HUD_WATCH_CORNER.get();
        String where = switch (corner) {
            case 1 -> en() ? "top right" : "arriba derecha";
            case 2 -> en() ? "bottom" : "abajo";
            default -> en() ? "top left" : "arriba izquierda";
        };
        // v0.305.0: si hay mapa arriba, se dice, porque el reloj se aparta solo.
        String extra = "";
        try {
            if (net.minecraftforge.fml.ModList.get().isLoaded("journeymap")
                    || net.minecraftforge.fml.ModList.get().isLoaded("xaerominimap")
                    || net.minecraftforge.fml.ModList.get().isLoaded("xaeroworldmap")) {
                extra = en() ? "  (map up)" : "  (con mapa)";
            }
        } catch (Throwable ignored) {
        }
        return label("Esquina: " + where + extra, "Corner: " + where + extra);
    }

    private void addToggle(int x, int y, net.minecraftforge.common.ForgeConfigSpec.BooleanValue value,
                           String es, String english, String helpLine) {
        DiaryToggle toggle = new DiaryToggle(
            x, y, this.toggleW, TOGGLE_H,
            value::get,
            v -> {
                value.set(v);
                value.save();
            },
            label(es, english),
            (v) -> v ? Component.literal(en() ? "on" : "si") : Component.literal(en() ? "off" : "no"),
            null);
        addWithHelp(toggle, helpLine);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        QuarantineSceneRenderer.renderDiaryBackground(g, this.width, this.height);
        DiaryChrome.drawPanelShadow(g, this.panelX, this.panelY, this.panelW, this.panelH);
        PaperTexture.drawTornSheet(g, this.panelX, this.panelY, this.panelW, this.panelH,
            PaperTexture.PaperKind.OFFICIAL);

        String title = en() ? "The house" : "La casa";
        int titleW = DiaryChrome.widthCached(this.font, title);
        g.drawString(this.font, title, this.width / 2 - titleW / 2, this.titleY,
            DiaryChrome.INK_TITLE, false);
        String sub = en() ? "- what the house keeps on -" : "- lo que la casa tiene puesto -";
        int subW = DiaryChrome.widthCached(this.font, sub);
        g.drawString(this.font, sub, this.width / 2 - subW / 2, this.titleY + 14,
            DiaryChrome.INK_SUBTITLE, false);
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 18, this.panelX + this.panelW - 18,
            this.titleY + 40);

        super.render(g, mouseX, mouseY, partialTick);

        // v0.301.0: la linea que explica el boton que se esta mirando.
        String helpLine = null;
        for (int i = 0; i < this.widgetsWithHelp.size(); i++) {
            AbstractWidget w = this.widgetsWithHelp.get(i);
            if (w.visible && w.isMouseOver(mouseX, mouseY)) {
                helpLine = this.helps.get(i);
                break;
            }
        }
        // v0.316.0: la franja sobre los botones es una sola. Con el raton en
        // un pestillo la ocupa la explicacion; si no, el folio y el registro.
        // Antes el folio caia justo encima del boton Volver.
        int stripY = this.panelY + this.panelH - 46;
        if (helpLine != null) {
            String fitted = DiaryChrome.fitText(this.font, helpLine, Math.max(60, this.panelW - 40));
            int hw = DiaryChrome.widthCached(this.font, fitted);
            g.drawString(this.font, fitted, this.width / 2 - hw / 2, stripY,
                DiaryChrome.INK_SUBTITLE, false);
        } else {
            String folio = en() ? "sheet XIX  ·  Esc back" : "hoja XIX  ·  Esc vuelve";
            int folioW = DiaryChrome.widthCached(this.font, folio);
            g.drawString(this.font, folio, this.panelX + this.panelW - 12 - folioW, stripY,
                DiaryChrome.INK_SUBTITLE, false);
            g.drawString(this.font, "REG. XIX", this.panelX + 12, stripY,
                DiaryChrome.INK_SUBTITLE, false);
        }
    }

    /** v0.297.0: vuelve a los valores de fabrica de la casa (los del archivo). */
    private void resetHouse() {
        setBool(ClientConfig.MENU_MUSIC_ENABLED, true);
        setBool(ClientConfig.AMBIENCE_ENABLED, true);
        setBool(ClientConfig.REDUCED_MOTION, false);
        setBool(ClientConfig.REDUCE_FLASHES, false);
        setBool(ClientConfig.HUD_WATCH_ENABLED, true);
        setBool(ClientConfig.HUD_WATCH_COORDS, true);
        ClientConfig.HUD_WATCH_CORNER.set(0);
        ClientConfig.HUD_WATCH_CORNER.save();
        ClientConfig.MUSIC_VOLUME.set(0.4);
        ClientConfig.MUSIC_VOLUME.save();
        ClientConfig.AMBIENCE_VOLUME.set(1.0);
        ClientConfig.AMBIENCE_VOLUME.save();
        ClientConfig.UI_VOLUME.set(1.0);
        ClientConfig.UI_VOLUME.save();
        this.rebuildWidgets();
    }

    private static void setBool(net.minecraftforge.common.ForgeConfigSpec.BooleanValue value, boolean v) {
        value.set(v);
        value.save();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
