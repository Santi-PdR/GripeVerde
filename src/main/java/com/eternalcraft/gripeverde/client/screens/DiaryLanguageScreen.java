package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.render.AtmosphereRenderer;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.LivingAtmosphere;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.FlagRenderer;
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

    private static final int PANEL_MAX_W = 420;
    private static final int PANEL_MAX_H = 300;
    private static final int PANEL_PAD = 18;
    private static final int FOOTER_GAP = 12;
    private static final int FOOTER_BTN_H = 22;
    private static final int FOOTER_BTN_GAP = 10;
    private static final int LIST_ITEM_H = 20;
    private static final int FLAG_SLOT    = 22;

    // Remember scroll position across reopens
    private static double lastScrollAmount = 0.0; // px reservados a la izquierda para bandera

    // Tonos de tinta especificos de la lista de idiomas (no van al DiaryChrome
    // porque solo tienen sentido aqui)
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
        this.listY = titleY + 50;
        this.listW = this.panelW - PANEL_PAD * 2;
        this.listH = footerY - FOOTER_GAP - this.listY;

        this.languageList = new LanguageList(this.minecraft, this.listX, this.listY, this.listW, this.listH);
        this.addWidget(this.languageList);
        // Restore previous scroll position
        if (lastScrollAmount > 0) {
            this.languageList.setScrollAmount(lastScrollAmount);
        }

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
        MenuMusic.tick();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Clear framebuffer + transition fade
        g.fill(0, 0, this.width, this.height, 0xFF000000);
        AtmosphereRenderer.renderSky(g, this.width, this.height);
        VictorianSkyline.renderBackground(g, this.width, this.height);
        VictorianSkyline.renderForeground(g, this.width, this.height);
        LivingAtmosphere.renderStreet(g, this.width, this.height);
                g.fill(0, 0, this.width, this.height, DiaryChrome.OVERLAY_COL);
        DiaryChrome.drawPanelShadow(g, panelX, panelY, panelW, panelH);
        PaperTexture.drawClean(g, panelX, panelY, panelW, panelH);

        int titleY = this.panelY + PANEL_PAD;
        int cx = this.width / 2;
        {
            int titleW = this.font.width(this.title);
            int titleX = (this.width - titleW) / 2;
            g.drawString(this.font, this.title, titleX + 1, titleY + 1, DiaryChrome.SHADOW_COL, false);
            g.drawString(this.font, this.title, titleX, titleY, DiaryChrome.INK_TITLE, false);

            String subtitle = "- " + Component.translatable("gripeverde.options.language.subtitle").getString() + " -";
            int subWL = this.font.width(subtitle);
            g.drawString(this.font, subtitle, cx - subWL / 2, titleY + 12, DiaryChrome.INK_SUBTITLE, false);

            Component flavor = Component.translatable("gripeverde.options.language.flavor");
            int flvWL = this.font.width(flavor);
            g.drawString(this.font, flavor, cx - flvWL / 2, titleY + 24, DiaryChrome.INK_FLAVOR, false);

            int divY1 = titleY + 42;
            DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY1);
        }

        int frameX = this.listX - 4;
        int frameY = this.listY - 4;
        int frameW = this.listW + 8;
        int frameH = this.listH + 8;

        // Fondo interno con textura de pergamino (evita cartulina lisa)
        PaperTexture.drawClean(g, frameX, frameY, frameW, frameH);
        // Marco tinta 1px
        g.fill(frameX, frameY, frameX + frameW, frameY + 1, 0x5A3A2416);
        g.fill(frameX, frameY + frameH - 1, frameX + frameW, frameY + frameH, 0x5A3A2416);
        g.fill(frameX, frameY, frameX + 1, frameY + frameH, 0x5A3A2416);
        g.fill(frameX + frameW - 1, frameY, frameX + frameW, frameY + frameH, 0x5A3A2416);

        if (this.languageList != null) {
            this.languageList.render(g, mouseX, mouseY, partialTick);

            int fadeH = 10;
            int paperFull  = 0xFFF3E9CE;
            int paperClear = 0x00F3E9CE;
            g.fillGradient(this.listX, this.listY,
                    this.listX + this.listW, this.listY + fadeH,
                    paperFull, paperClear);
            g.fillGradient(this.listX, this.listY + this.listH - fadeH,
                    this.listX + this.listW, this.listY + this.listH,
                    paperClear, paperFull);
        }

        int divY2 = this.panelY + this.panelH - PANEL_PAD - FOOTER_BTN_H - 6;
        DiaryChrome.drawInkDividerBetween(g, this.panelX + 14, this.panelX + this.panelW - 14, divY2);
        DiaryChrome.drawCornerOrnaments(g, this.panelX, this.panelY, this.panelW, this.panelH);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        // Save scroll position for next open
        if (this.languageList != null) {
            lastScrollAmount = this.languageList.getScrollAmount();
        }
        // ESC cancela sin aplicar el idioma pendiente
        this.options.save();
        this.minecraft.setScreen(this.parent);
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

        @Override
        public void render(net.minecraft.client.gui.GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            super.render(gfx, mouseX, mouseY, partialTick);
            DiaryChrome.drawScrollThumb(gfx, this.getScrollbarPosition(),
                this.y0, this.y1, this.getScrollAmount(), this.getMaxScroll());
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
            // Vanilla usa itemH/2 = 10px por tick, muy lento. Aceleramos 4x.
            this.setScrollAmount(this.getScrollAmount() - delta * LIST_ITEM_H * 2);
            return true;
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

            // Fondo de fila seleccionada / hover
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

            // ── Bandera ──────────────────────────────────────────────────────
            int flagX = left + 3;
            int flagY = top + (rowHeight - FlagRenderer.FLAG_H) / 2;
            FlagRenderer.draw(g, this.code, flagX, flagY);

            // ── Texto (centrado en la zona derecha del slot de bandera) ──────
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

            // Zona de texto: desde left+FLAG_SLOT hasta left+rowWidth
            int textZoneLeft  = left + FLAG_SLOT;
            int textZoneWidth = rowWidth - FLAG_SLOT;
            int textW  = DiaryLanguageScreen.this.font.width(label);
            int textX  = textZoneLeft + (textZoneWidth - textW) / 2;
            int textY  = top + (rowHeight - DiaryLanguageScreen.this.font.lineHeight) / 2;

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