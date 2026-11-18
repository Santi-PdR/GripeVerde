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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * v0.296.0 - "La casa": una hoja con los interruptores de la casa (musica,
 * ambiente, movimiento, destellos y el reloj de la partida). Son los mismos
 * valores de {@code gripeverde-client.toml}, sin salir del cuaderno.
 */
@OnlyIn(Dist.CLIENT)
public class DiaryHouseScreen extends Screen {

    private static final int PANEL_MAX_W = 360;
    private static final int PANEL_MAX_H = 316;
    private static final int TOGGLE_H = 22;
    private static final int ROW_H = 26;
    private static final int COL_GAP = 10;

    private final Screen parent;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int titleY;
    private int gridTop;
    private int toggleW;

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

    @Override
    protected void init() {
        this.panelW = Math.min(PANEL_MAX_W, this.width - 40);
        this.panelH = Math.min(PANEL_MAX_H, this.height - 24);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;
        this.titleY = this.panelY + 14;
        this.gridTop = this.titleY + 46;
        this.toggleW = (this.panelW - 32 - COL_GAP) / 2;

        int col1 = this.panelX + 16;
        int col2 = col1 + this.toggleW + COL_GAP;

        addToggle(col1, row(0), ClientConfig.MENU_MUSIC_ENABLED,
            "Musica de la casa", "House music");
        addToggle(col2, row(0), ClientConfig.AMBIENCE_ENABLED,
            "Ambiente", "Ambience");
        addToggle(col1, row(1), ClientConfig.REDUCED_MOTION,
            "Movimiento corto", "Short motion");
        addToggle(col2, row(1), ClientConfig.REDUCE_FLASHES,
            "Menos destellos", "Fewer flashes");
        addToggle(col1, row(2), ClientConfig.HUD_WATCH_ENABLED,
            "Reloj en partida", "Watch in game");
        addToggle(col2, row(2), ClientConfig.HUD_WATCH_COORDS,
            "Coordenadas", "Coordinates");

        // v0.297.0: los volumenes de la casa, con la misma rueda que el FOV.
        int cursor = row(3);
        boolean roomy = this.panelH >= 290;
        if (roomy) {
            addRenderableWidget(new DiarySlider(
                col1, cursor, this.toggleW, 20, ClientConfig.MUSIC_VOLUME.get(),
                v -> (en() ? "music " : "musica ") + pct(v),
                v -> {
                    ClientConfig.MUSIC_VOLUME.set(v);
                    ClientConfig.MUSIC_VOLUME.save();
                }
            ));
            addRenderableWidget(new DiarySlider(
                col2, cursor, this.toggleW, 20, ClientConfig.AMBIENCE_VOLUME.get(),
                v -> (en() ? "ambience " : "ambiente ") + pct(v),
                v -> {
                    ClientConfig.AMBIENCE_VOLUME.set(v);
                    ClientConfig.AMBIENCE_VOLUME.save();
                }
            ));
            cursor += 24;
            addRenderableWidget(new DiarySlider(
                col1, cursor, this.panelW - 32, 20, ClientConfig.UI_VOLUME.get(),
                v -> (en() ? "interface " : "interfaz ") + pct(v),
                v -> {
                    ClientConfig.UI_VOLUME.set(v);
                    ClientConfig.UI_VOLUME.save();
                }
            ));
            cursor += 24;
        }

        final int cornerY = cursor;
        addRenderableWidget(new DiaryButton(
            this.panelX + 16, cornerY, this.panelW - 32, TOGGLE_H,
            cornerLabel(),
            DiaryButton.Icon.GEAR, DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.OPTION_OPEN,
            () -> {
                int next = (ClientConfig.HUD_WATCH_CORNER.get() + 1) % 3;
                ClientConfig.HUD_WATCH_CORNER.set(next);
                ClientConfig.HUD_WATCH_CORNER.save();
                this.rebuildWidgets();
            }
        ));

        int backW = 122;
        int backY = this.panelY + this.panelH - 32;
        addRenderableWidget(new DiaryButton(
            (this.width - backW) / 2 - 130, backY, 122, 22,
            label("Todo a su sitio", "Put it all back"),
            DiaryButton.Icon.SCROLL, DiaryButton.Variant.SECONDARY, DiaryButton.SoundProfile.BACK,
            this::resetHouse
        ));
        addRenderableWidget(new DiaryButton(
            (this.width - backW) / 2 + 8, backY, backW, 22,
            Component.translatable("gripeverde.options.button.done"),
            DiaryButton.Icon.DOOR, DiaryButton.Variant.PRIMARY, DiaryButton.SoundProfile.BACK,
            this::onClose
        ));
    }

    private static String pct(double value) {
        return Math.round(Math.max(0.0, Math.min(1.0, value)) * 100.0) + "%";
    }

    private int row(int index) {
        return this.gridTop + index * ROW_H;
    }

    private Component cornerLabel() {
        int corner = ClientConfig.HUD_WATCH_CORNER.get();
        String where = switch (corner) {
            case 1 -> en() ? "top right" : "arriba a la derecha";
            case 2 -> en() ? "bottom" : "abajo";
            default -> en() ? "top left" : "arriba a la izquierda";
        };
        return label("Esquina del reloj: " + where, "Watch corner: " + where);
    }

    private void addToggle(int x, int y, net.minecraftforge.common.ForgeConfigSpec.BooleanValue value,
                           String es, String english) {
        addRenderableWidget(new DiaryToggle(
            x, y, this.toggleW, TOGGLE_H,
            value::get,
            v -> {
                value.set(v);
                value.save();
            },
            label(es, english),
            (v) -> v ? Component.literal(en() ? "on" : "si") : Component.literal(en() ? "off" : "no"),
            null
        ));
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

        String folio = en() ? "sheet XIX  ·  Esc back" : "hoja XIX  ·  Esc vuelve";
        int folioW = DiaryChrome.widthCached(this.font, folio);
        g.drawString(this.font, folio,
            this.panelX + this.panelW - 12 - folioW,
            this.panelY + this.panelH - 12, DiaryChrome.INK_SUBTITLE, false);
        String reg = en() ? "REG. XIX" : "REG. XIX";
        g.drawString(this.font, reg, this.panelX + 12, this.panelY + this.panelH - 12,
            DiaryChrome.INK_SUBTITLE, false);
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
