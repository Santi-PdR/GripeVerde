package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.VictorianSkyline;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public class DiaryLanguageScreen extends Screen {

    private static final int SCENE_OVERLAY = 0x66000000;
    private static final int INK_TITLE     = 0xFF2A1A0A;
    private static final int INK_SUBTITLE  = 0xFF6B4226;
    private static final int INK_DIVIDER   = 0x88422810;
    private static final int SHADOW_COL    = 0x20201409;
    private static final int PANEL_MAX_W = 420;
    private static final int PANEL_MAX_H = 300;
    private static final int PANEL_PAD = 18;
    private static final int TITLE_GAP = 12;
    private static final int FOOTER_GAP = 12;
    private static final int FOOTER_BTN_H = 22;
    private static final int FOOTER_BTN_GAP = 10;
    private static final int LIST_ITEM_H = 20;

    private static final int INK_ACTIVE  = 0xFF1A0F08;
    private static final int INK_PENDING = 0xFF3A230F;
    private static final int INK_NORMAL  = 0xFF6B4A2E;

    private final Screen parent;
    private final Options options;
    private final LanguageManager languageManager;

    private LanguageList languageList;
    private DiaryButton unicodeButton;

    private String appliedLanguageCode;
    private String pendingLanguageCode;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int listX;
    private int listY;
    private int listW;
    private int listH;

    public DiaryLanguageScreen(Screen parent, Options options, LanguageManager languageManager) {
        super(Component.translatable("gripeverde.options.language.title"));
        this.parent = parent;
        this.options = options;
        this.languageManager = languageManager;
    }

    @Override
    protected void init() {
        this.appliedLanguageCode = this.languageManager.getSelected();
        this.pendingLanguageCode = this.appliedLanguageCode;

        this.panelW = Math.min(PANEL_MAX_W, this.width - 24);
        this.panelH = Math.min(PANEL_MAX_H, this.height - 24);
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        int titleY = this.panelY + PANEL_PAD;
        int footerY = this.panelY + this.panelH - PANEL_PAD - FOOTER_BTN_H;

        this.listX = this.panelX + PANEL_PAD;
        this.listY = titleY + 50;  // titulo + subtitulo + flavor + emblema + divisor
        this.listW = this.panelW - PANEL_PAD * 2;
        this.listH = footerY - FOOTER_GAP - this.listY;

        this.languageList = new LanguageList(this.minecraft, this.listX, this.listY, this.listW, this.listH);
        this.addWidget(this.languageList);

        int footerW = this.panelW - PANEL_PAD * 2;
        int leftBtnW = (footerW - FOOTER_BTN_GAP) / 2;
        int rightBtnW = footerW - leftBtnW - FOOTER_BTN_GAP;

        int unicodeX = this.panelX + PANEL_PAD;
        int backX = unicodeX + leftBtnW + FOOTER_BTN_GAP;

        this.unicodeButton = this.addRenderableWidget(new DiaryButton(
                unicodeX, footerY, leftBtnW, FOOTER_BTN_H,
                Component.empty(),
                DiaryButton.Icon.NONE,
                DiaryButton.Variant.SECONDARY,
                this::toggleUnicode
        ));
        updateUnicodeButtonLabel();

        this.addRenderableWidget(new DiaryButton(
                backX, footerY, rightBtnW, FOOTER_BTN_H,
                Component.translatable("gripeverde.options.button.done"),
                DiaryButton.Icon.DOOR,
                DiaryButton.Variant.PRIMARY,
                this::applyAndClose
        ));
    }

    @Override
    public void tick() {
        super.tick();
        float delta = 1.0f / 20.0f;
        AtmosphereRenderer.tick(delta);
        VictorianSkyline.tick(delta);
        LivingAtmosphere.tick(delta);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        AtmosphereRenderer.renderSky(g, this.width, this.height);
        VictorianSkyline.renderBackground(g, this.width, this.height);
        LivingAtmosphere.renderFogLow(g, this.width, this.height);
        VictorianSkyline.renderForeground(g, this.width, this.height);
        LivingAtmosphere.renderStreetLevel(g, this.width, this.height);
        LivingAtmosphere.renderAir(g, this.width, this.height);

        g.fill(0, 0, this.width, this.height, SCENE_OVERLAY);
        PaperTexture.drawClean(g, panelX, panelY, panelW, panelH);

        int titleY = this.panelY + PANEL_PAD;
        int cx = this.width / 2;
        {
            int titleW = this.font.width(this.title);
            int titleX = (this.width - titleW) / 2;
            // Sombra sutil color pergamino oscuro
            g.drawString(this.font, this.title, titleX + 1, titleY + 1, SHADOW_COL, false);
            g.drawString(this.font, this.title, titleX, titleY, INK_TITLE, false);

            // Subtitulo
            Component subtitle = Component.translatable("gripeverde.options.language.subtitle");
            int subWL = this.font.width(subtitle); g.drawString(this.font, subtitle, cx - subWL / 2, titleY + 12, INK_SUBTITLE, false);

            // Flavor
            Component flavor = Component.translatable("gripeverde.options.language.flavor");
            int flvWL = this.font.width(flavor); g.drawString(this.font, flavor, cx - flvWL / 2, titleY + 24, 0xC06B4A2E, false);
            // Divisor superior (con rombo central)
            int divY1 = titleY + 42;
            drawInkDivider(g, this.panelX + 14, this.panelX + this.panelW - 14, divY1);
        }

        int frameX = this.listX - 4;
        int frameY = this.listY - 4;
        int frameW = this.listW + 8;
        int frameH = this.listH + 8;

        g.fill(frameX, frameY, frameX + frameW, frameY + frameH, 0x24100804);
        g.fill(frameX, frameY, frameX + frameW, frameY + 1, 0x5A3A2416);
        g.fill(frameX, frameY + frameH - 1, frameX + frameW, frameY + frameH, 0x5A3A2416);
        g.fill(frameX, frameY, frameX + 1, frameY + frameH, 0x5A3A2416);
        g.fill(frameX + frameW - 1, frameY, frameX + frameW, frameY + frameH, 0x5A3A2416);

        if (this.languageList != null) {
            this.languageList.render(g, mouseX, mouseY, partialTick);

            // Fade top/bottom para suavizar el corte de las filas al scrollear
            int fadeH = 10;
            int paperFull  = 0xFFF3E9CE; // color aproximado al pergamino
            int paperClear = 0x00F3E9CE;
            g.fillGradient(this.listX, this.listY,
                    this.listX + this.listW, this.listY + fadeH,
                    paperFull, paperClear);
            g.fillGradient(this.listX, this.listY + this.listH - fadeH,
                    this.listX + this.listW, this.listY + this.listH,
                    paperClear, paperFull);
        }

        // Divisor inferior antes de los botones + esquineros externos
        int divY2 = this.panelY + this.panelH - PANEL_PAD - FOOTER_BTN_H - 6;
        drawInkDivider(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);
        drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        applyAndClose();
    }

    private void toggleUnicode() {
        boolean value = !this.options.forceUnicodeFont().get();
        this.options.forceUnicodeFont().set(value);
        this.options.save();
        updateUnicodeButtonLabel();
    }

    private void updateUnicodeButtonLabel() {
        if (this.unicodeButton == null) {
            return;
        }

        boolean enabled = this.options.forceUnicodeFont().get();
        this.unicodeButton.setMessage(
                Component.translatable("options.forceUnicodeFont")
                        .append(": ")
                        .append(Component.translatable(enabled ? "options.on" : "options.off"))
        );
    }

    private void applyAndClose() {
        if (this.pendingLanguageCode != null
                && !Objects.equals(this.pendingLanguageCode, this.languageManager.getSelected())) {
            this.languageManager.setSelected(this.pendingLanguageCode);
            this.options.languageCode = this.pendingLanguageCode;
            this.minecraft.reloadResourcePacks();
        }

        this.options.save();
        this.minecraft.setScreen(this.parent);
    }

    // ─── Cosmeticos compartidos con Accesibilidad / Chat ────────────────────

    private static void drawInkDivider(GuiGraphics g, int x1, int x2, int y) {
        int inkFull  = INK_DIVIDER;
        int inkClear = 0x00422810;
        int mid = (x1 + x2) / 2;
        int fade = (x2 - x1) / 8;
        g.fillGradient(x1,        y, x1 + fade,     y + 1, inkClear, inkFull);
        g.fill        (x1 + fade, y, mid - 6,       y + 1, inkFull);
        g.fill        (mid + 6,   y, x2 - fade,     y + 1, inkFull);
        g.fillGradient(x2 - fade, y, x2,            y + 1, inkFull, inkClear);
        drawDivisorLozenge(g, mid, y);
    }

    private static void drawDivisorLozenge(GuiGraphics g, int cx, int cy) {
        int col = 0xC0422810;
        g.fill(cx,     cy - 2, cx + 1, cy - 1, col);
        g.fill(cx - 1, cy - 1, cx + 2, cy,     col);
        g.fill(cx - 2, cy,     cx + 3, cy + 1, col);
        g.fill(cx - 1, cy + 1, cx + 2, cy + 2, col);
        g.fill(cx,     cy + 2, cx + 1, cy + 3, col);
    }

    private static void drawCornerOrnaments(GuiGraphics g, int x, int y, int w, int h) {
        int colOut = 0xC0422810;
        int colIn  = 0x80422810;
        int sOut = 12;
        int sIn  = 5;
        int off  = 3;

        // Marco externo
        g.fill(x,             y,             x + sOut, y + 1,    colOut);
        g.fill(x,             y,             x + 1,    y + sOut, colOut);
        g.fill(x + w - sOut,  y,             x + w,    y + 1,    colOut);
        g.fill(x + w - 1,     y,             x + w,    y + sOut, colOut);
        g.fill(x,             y + h - 1,     x + sOut, y + h,    colOut);
        g.fill(x,             y + h - sOut,  x + 1,    y + h,    colOut);
        g.fill(x + w - sOut,  y + h - 1,     x + w,    y + h,    colOut);
        g.fill(x + w - 1,     y + h - sOut,  x + w,    y + h,    colOut);

        // Marco interno
        int ix = x + off;
        int iy = y + off;
        int iw = w - off * 2;
        int ih = h - off * 2;
        g.fill(ix,             iy,             ix + sIn, iy + 1,    colIn);
        g.fill(ix,             iy,             ix + 1,   iy + sIn,  colIn);
        g.fill(ix + iw - sIn,  iy,             ix + iw,  iy + 1,    colIn);
        g.fill(ix + iw - 1,    iy,             ix + iw,  iy + sIn,  colIn);
        g.fill(ix,             iy + ih - 1,    ix + sIn, iy + ih,   colIn);
        g.fill(ix,             iy + ih - sIn,  ix + 1,   iy + ih,   colIn);
        g.fill(ix + iw - sIn,  iy + ih - 1,    ix + iw,  iy + ih,   colIn);
        g.fill(ix + iw - 1,    iy + ih - sIn,  ix + iw,  iy + ih,   colIn);
    }

    // Emblema del gabinete de Lenguas: fleur-de-lis simplificada (9x11)
    // Simbolo heraldico victoriano de multiples voces
    private static void drawFleurEmblem(GuiGraphics g, int cx, int cy) {
        int col = 0xD0422810;
        int hi  = 0xE01A0A05;
        // Petalo central superior (punta hacia arriba)
        g.fill(cx,     cy,     cx + 1, cy + 1, col);
        g.fill(cx,     cy + 1, cx + 1, cy + 4, col);
        // Base del petalo central
        g.fill(cx - 1, cy + 3, cx + 2, cy + 4, col);
        // Petalos laterales que se abren hacia afuera
        g.fill(cx - 3, cy + 4, cx - 2, cy + 5, col);
        g.fill(cx - 4, cy + 5, cx - 3, cy + 6, col);
        g.fill(cx - 3, cy + 6, cx - 2, cy + 7, col);
        g.fill(cx + 2, cy + 4, cx + 3, cy + 5, col);
        g.fill(cx + 3, cy + 5, cx + 4, cy + 6, col);
        g.fill(cx + 2, cy + 6, cx + 3, cy + 7, col);
        // Cuerpo central bajo la banda
        g.fill(cx - 1, cy + 5, cx + 2, cy + 8, col);
        // Banda horizontal (lazo)
        g.fill(cx - 3, cy + 7, cx + 4, cy + 8, col);
        // Tallo inferior
        g.fill(cx - 1, cy + 8, cx + 2, cy + 10, col);
        g.fill(cx - 2, cy + 10, cx + 3, cy + 11, col);
        // Brillo central
        g.fill(cx,     cy + 6, cx + 1, cy + 7, hi);
    }

    private final class LanguageList extends ObjectSelectionList<LanguageEntry> {

        private final int rowW;
        private LanguageList(Minecraft minecraft, int left, int top, int width, int height) {
            super(minecraft, width, height, top, top + height, LIST_ITEM_H);
            this.rowW = width - 10;
            this.setLeftPos(left);
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
            this.setRenderSelection(false);

            LanguageEntry selectedEntry = null;

            for (java.util.Map.Entry<String, LanguageInfo> mapEntry : DiaryLanguageScreen.this.languageManager.getLanguages().entrySet()) {
                LanguageEntry entry = new LanguageEntry(mapEntry.getKey(), mapEntry.getValue());
                this.addEntry(entry);

                if (Objects.equals(mapEntry.getKey(), DiaryLanguageScreen.this.pendingLanguageCode)) {
                    selectedEntry = entry;
                }
            }

            if (selectedEntry != null) {
                this.setSelected(selectedEntry);
                this.centerScrollOn(selectedEntry);
            }
        }

        @Override
        public int getRowWidth() {
            return this.rowW;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getRowLeft() + this.getRowWidth() + 4;
        }


    }

    private final class LanguageEntry extends ObjectSelectionList.Entry<LanguageEntry> {

        private final String code;
        private final LanguageInfo info;

        private LanguageEntry(String code, LanguageInfo info) {
            this.code = code;
            this.info = info;
        }

        @Override
        public void render(GuiGraphics g, int index, int top, int left, int rowWidth, int rowHeight,
                           int mouseX, int mouseY, boolean hovered, float partialTick) {

            boolean isPending = Objects.equals(DiaryLanguageScreen.this.pendingLanguageCode, this.code);
            boolean isApplied = Objects.equals(DiaryLanguageScreen.this.appliedLanguageCode, this.code);

            int fill;
            if (isPending) {
                fill = 0x66422810;
            } else if (hovered) {
                fill = 0x33422810;
            } else {
                fill = 0x00000000;
            }

            if (fill != 0) {
                g.fill(left + 2, top + 1, left + rowWidth - 2, top + rowHeight - 2, fill);
            }

            int textColor;
            if (isPending) {
                textColor = INK_ACTIVE;
            } else if (isApplied) {
                textColor = INK_PENDING;
            } else {
                textColor = INK_NORMAL;
            }

            String prefix;
            if (isPending) {
                prefix = "> ";
            } else if (isApplied) {
                prefix = "- ";
            } else {
                prefix = "  ";
            }

            Component label = Component.literal(prefix).append(this.info.toComponent());

            int textW = DiaryLanguageScreen.this.font.width(label);
            int textX = left + (rowWidth - textW) / 2;
            int textY = top + (rowHeight - DiaryLanguageScreen.this.font.lineHeight) / 2;

            g.drawString(DiaryLanguageScreen.this.font, label, textX, textY, textColor, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                DiaryLanguageScreen.this.pendingLanguageCode = this.code;
                DiaryLanguageScreen.this.languageList.setSelected(this);
                UiSounds.playClick();
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return this.info.toComponent();
        }
    }
}