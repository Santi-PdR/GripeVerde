package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.VictorianSkyline;
import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton.Icon;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton.Variant;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DiaryAccessibilityOptionsScreen extends AccessibilityOptionsScreen {

    // Panel deseado (se recorta si no cabe en pantalla)
    private static final int PANEL_MAX_W = 380;
    private static final int PANEL_MAX_H = 395;

    // Margen minimo entre panel y borde de pantalla
    private static final int SCREEN_MARGIN = 12;

    // Bandas dentro del panel
    private static final int HEADER_H = 60;   // titulo + subtitulo + flavor + divisor
    private static final int FOOTER_H = 45;   // divisor + boton Volver

    private DiaryButton btnVolver;
    private final SmoothScroll smoothScroll = new SmoothScroll();
    private int panelW, panelH, panelX, panelY;

    public DiaryAccessibilityOptionsScreen(Screen parent, Options options) {
        super(parent, options);
    }

    @Override
    protected void init() {
        String[] flavorPool = getAccFlavorPool();
        accFlavorIndex = ACC_RNG.nextInt(flavorPool.length);
        super.init();

        // Panel adaptativo
        this.panelW = Math.min(PANEL_MAX_W, this.width  - SCREEN_MARGIN * 2);
        this.panelH = Math.min(PANEL_MAX_H, this.height - SCREEN_MARGIN * 2);
        this.panelX = (this.width  - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        // Suprimir fondo y barras de la lista vanilla
        if (this.list != null) {
            this.list.setRenderBackground(false);
            this.list.setRenderTopAndBottom(false);
            this.list.setRenderSelection(false);
            // NO cambiar width - R-VANILLA-LIST-COORDS
            int listTop    = this.panelY + HEADER_H;
            int listBottom = this.panelY + this.panelH - FOOTER_H;
            this.list.updateSize(this.width, this.height, listTop, listBottom);
            this.smoothScroll.snapToCurrent(this.list);
        }

        // Ocultar boton Done vanilla
        for (var child : this.children()) {
            if (child instanceof Button btn) {
                btn.visible = false;
                btn.active  = false;
                btn.setX(this.width + 200);
            }
        }

        // Boton Volver propio
        int btnW = 160;
        int btnH = 20;
        int btnX = (this.width - btnW) / 2;
        int btnY = this.panelY + this.panelH - 28;
        this.btnVolver = new DiaryButton(
            btnX, btnY, btnW, btnH,
            Component.translatable("gripeverde.options.button.done"),
            Icon.DOOR, Variant.PRIMARY,
            this::onClose
        );
        this.addRenderableWidget(this.btnVolver);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        // Escena viva
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.renderDiaryBackground(gfx, this.width, this.height);

        // Panel pergamino
        DiaryChrome.drawPanelShadow(gfx, this.panelX, this.panelY, this.panelW, this.panelH);
        PaperTexture.drawClean(gfx, this.panelX, this.panelY, this.panelW, this.panelH, PaperTexture.PaperKind.OFFICIAL);

        // Titulo
        int cx = this.width / 2;
        int titleY = this.panelY + 12;
        {
        var _title = Component.translatable("options.accessibility.title");
        int _tw = this.font.width(_title);
        gfx.drawString(this.font, _title, cx - _tw / 2,     titleY,     DiaryChrome.INK_TITLE,  false);
        }

        // Subtitulo
        int subtitleY = titleY + 12;
        {
            String subC = "- " + Component.translatable("gripeverde.options.accessibility.subtitle").getString() + " -";
            int subW = this.font.width(subC);
            gfx.drawString(this.font, subC, cx - subW / 2, subtitleY, DiaryChrome.INK_SUBTITLE, false);
        }

        // Flavor
        int flavorY = subtitleY + 13;
        {
            Component flvC = Component.literal(getRotatingAccFlavor());
            int flvW = this.font.width(flvC);
            gfx.drawString(this.font, flvC, cx - flvW / 2, flavorY, DiaryChrome.INK_FLAVOR, false);
        }

        // Divisor superior
        int divY1 = flavorY + 12;
        DiaryChrome.drawInkDivider(gfx, this.panelX, divY1, this.panelW);

        // Lista vanilla sin gradientes blancos ni bandejas extra
        if (this.list != null) {
            this.list.render(gfx, mouseX, mouseY, delta);
            DiaryChrome.drawStyledScrollThumbInPanel(gfx, this.list, this.panelX + this.panelW);
        }

        // Cosmeticos
        int divY2 = this.panelY + this.panelH - 38;
        DiaryChrome.drawInkDivider(gfx, this.panelX, divY2, this.panelW);
        DiaryChrome.drawCornerOrnaments(gfx, this.panelX, this.panelY, this.panelW, this.panelH);

        // Boton Volver
        if (this.btnVolver != null) {
            this.btnVolver.render(gfx, mouseX, mouseY, delta);
        }
    }

    @Override
    public void tick() {
        super.tick();
        com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer.tick(1.0f / 20.0f);
        this.smoothScroll.tick(this.list);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.list != null && mouseY >= this.list.getTop() && mouseY <= this.list.getBottom()) {
            if (this.smoothScroll.wheel(this.list, delta, 34.0D)) {
                com.eternalcraft.gripeverde.client.audio.UiSounds.scroll();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    // Rotating flavor quotes for accessibility options
    private static final String[] ACC_FLAVOR_POOL_ES = {
        "Ajustad los sentidos con prudencia.",
        "No todos ven la ciudad con los mismos ojos.",
        "Algunos oidos ya no escuchan las campanas.",
        "La vista se cansa antes que la fiebre.",
        "Cada sentido cuenta cuando falta uno.",
        "Que nadie quede fuera de la cronica.",
        "Las manos tambien saben leer."
    };
    private static final String[] ACC_FLAVOR_POOL_EN = {
        "Attune the senses with prudence.",
        "Not all see the city with the same eyes.",
        "Some ears no longer hear the bells.",
        "The eyes tire before the fever does.",
        "Every sense counts when one is missing.",
        "Let no one be left outside the chronicle.",
        "The hands know how to read as well."
    };
    private static int accFlavorIndex = -1;
    private static final java.util.Random ACC_RNG = new java.util.Random();

    private static String getRotatingAccFlavor() {
        String[] pool = getAccFlavorPool();
        if (accFlavorIndex < 0 || accFlavorIndex >= pool.length) {
            accFlavorIndex = ACC_RNG.nextInt(pool.length);
        }
        return pool[accFlavorIndex];
    }

    private static String[] getAccFlavorPool() {
        String lang = net.minecraft.client.Minecraft.getInstance().getLanguageManager().getSelected();
        boolean english = lang == null || lang.startsWith("en_") || lang.equals("en_us");
        return english ? ACC_FLAVOR_POOL_EN : ACC_FLAVOR_POOL_ES;
    }

}
