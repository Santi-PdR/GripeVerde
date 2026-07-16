package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.CandleLight;
import com.eternalcraft.gripeverde.client.render.DiaryDecorator;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.InkEffects;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.TextureAssets;
import com.eternalcraft.gripeverde.client.widgets.GreenFluButton;
import com.eternalcraft.gripeverde.client.widgets.SlimButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class DebugCanvasScreen extends Screen {

    private final Screen parent;
    private long ticks = 0;

    private static final String[] SWATCH_NAMES = {
        "PAPER","INK","SEPIA_LIGHT","SEPIA_DARK","BRONZE","BRONZE_DARK",
        "OCHRE","AMBER_FLAME","REDCOAT","REDCOAT_BRIGHT","BLOOD_FRESH","BLOOD_DRIED",
        "WAX_SEAL","CANDLE_WAX","GOLD_TARNISHED","BONE_WHITE","PLAGUE_TOXIC","PLAGUE_GREEN","SICK_GREEN"
    };
    private static final int[] SWATCH_COLORS = {
        DiaryTheme.PAPER, DiaryTheme.INK, DiaryTheme.SEPIA_LIGHT, DiaryTheme.SEPIA_DARK,
        DiaryTheme.BRONZE, DiaryTheme.BRONZE_DARK, DiaryTheme.OCHRE, DiaryTheme.AMBER_FLAME,
        DiaryTheme.REDCOAT, DiaryTheme.REDCOAT_BRIGHT, DiaryTheme.BLOOD_FRESH, DiaryTheme.BLOOD_DRIED,
        DiaryTheme.WAX_SEAL, DiaryTheme.CANDLE_WAX, DiaryTheme.GOLD_TARNISHED, DiaryTheme.BONE_WHITE,
        DiaryTheme.PLAGUE_TOXIC, DiaryTheme.PLAGUE_GREEN, DiaryTheme.SICK_GREEN
    };

    public DebugCanvasScreen(Screen parent) {
        super(Component.translatable("gripeverde.catalog.window"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new SlimButton(
            this.width - 180, 30, 150, 28,
            Component.translatable("gripeverde.catalog.button.sound_lab"),
            () -> this.minecraft.setScreen(new SoundLabScreen(this))
        ));
        this.addRenderableWidget(new SlimButton(
            20, this.height - 45, 120, 28,
            Component.translatable("gripeverde.catalog.button.back"),
            () -> this.minecraft.setScreen(parent)
        ));
        int demoY = this.height - 130;
        this.addRenderableWidget(new GreenFluButton(
            330, demoY, 250, 36,
            Component.translatable("gripeverde.catalog.demo.primary"),
            Component.translatable("gripeverde.catalog.demo.primary.sub"),
            TextureAssets.ICON_BTN_MP,
            () -> {}
        ));
        this.addRenderableWidget(new SlimButton(
            330, demoY + 45, 250, 26,
            Component.translatable("gripeverde.catalog.demo.slim"),
            () -> {}
        ));
    }

    @Override
    public void tick() { this.ticks++; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        PaperTexture.draw(g, 0, 0, this.width, this.height);

        DiaryDecorator.drawTitle(g, I18n.get("gripeverde.catalog.title"),
            this.width / 2, 15, DiaryTheme.INK);
        DiaryDecorator.drawSubtitle(g, I18n.get("gripeverde.catalog.subtitle"),
            this.width / 2, 32);

        InkEffects.ornateSeparator(g, this.width / 2 - 200, 48, this.width / 2 + 200, DiaryTheme.BLOOD_DRIED);

        int col1X = 30;
        int y = 70;
        DiaryDecorator.drawSectionHeader(g, I18n.get("gripeverde.catalog.section.palette"), col1X, y, DiaryTheme.INK);
        y += 20;
        for (int i = 0; i < SWATCH_NAMES.length; i++) {
            y = drawSwatch(g, col1X, y, SWATCH_NAMES[i], SWATCH_COLORS[i]);
        }

        int col2X = 330;
        int col2Y = 70;
        DiaryDecorator.drawSectionHeader(g, I18n.get("gripeverde.catalog.section.buttons_legacy"), col2X, col2Y, DiaryTheme.INK);
        col2Y += 20;

        boolean h1 = isHovered(mouseX, mouseY, col2X, col2Y, 180, 22);
        DiaryDecorator.drawStampedButton(g, col2X, col2Y, 180, 22,
            I18n.get("gripeverde.catalog.button.demo_legacy.1"), h1, true);
        col2Y += 30;
        boolean h2 = isHovered(mouseX, mouseY, col2X, col2Y, 180, 22);
        DiaryDecorator.drawStampedButton(g, col2X, col2Y, 180, 22,
            I18n.get("gripeverde.catalog.button.demo_legacy.2"), h2, false);
        col2Y += 30;
        boolean h3 = isHovered(mouseX, mouseY, col2X, col2Y, 180, 22);
        DiaryDecorator.drawStampedButton(g, col2X, col2Y, 180, 22,
            I18n.get("gripeverde.catalog.button.demo_legacy.3"), h3, false);
        col2Y += 40;

        DiaryDecorator.drawSectionHeader(g, I18n.get("gripeverde.catalog.section.effects"), col2X, col2Y, DiaryTheme.INK);
        col2Y += 20;

        InkEffects.bloodSplatter(g, col2X + 30, col2Y + 20, 6, 1234L);
        g.drawString(this.font, I18n.get("gripeverde.catalog.effect.blood_splatter"), col2X + 80, col2Y + 17, DiaryTheme.INK, false);
        col2Y += 45;

        InkEffects.waxSeal(g, col2X + 30, col2Y + 12, 14, "1897");
        g.drawString(this.font, I18n.get("gripeverde.catalog.effect.wax_seal"), col2X + 80, col2Y + 9, DiaryTheme.INK, false);
        col2Y += 35;

        CandleLight.draw(g, col2X + 30, col2Y + 12, 8, this.ticks);
        g.drawString(this.font, I18n.get("gripeverde.catalog.effect.candle"), col2X + 80, col2Y + 9, DiaryTheme.INK, false);
        col2Y += 30;

        InkEffects.smearedInk(g, col2X + 30, col2Y + 6, 4);
        g.drawString(this.font, I18n.get("gripeverde.catalog.effect.smeared_ink"), col2X + 80, col2Y + 9, DiaryTheme.INK, false);

        int col3X = 620;
        int col3Y = 70;
        DiaryDecorator.drawSectionHeader(g, I18n.get("gripeverde.catalog.section.dossier"), col3X, col3Y, DiaryTheme.INK);
        col3Y += 25;

        int dossierW = 240;
        int dossierH = 90;
        g.fill(col3X, col3Y, col3X + dossierW, col3Y + dossierH, DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, 60));
        DiaryDecorator.drawDoubleFrame(g, col3X, col3Y, dossierW, dossierH, DiaryTheme.INK_FADED);
        DiaryDecorator.drawCornerFleurons(g, col3X, col3Y, dossierW, dossierH, DiaryTheme.BLOOD_DRIED);

        String[] dossierLines = {
            I18n.get("gripeverde.catalog.dossier.line.1"),
            I18n.get("gripeverde.catalog.dossier.line.2"),
            I18n.get("gripeverde.catalog.dossier.line.3")
        };
        int ty = col3Y + 15;
        for (String line : dossierLines) {
            g.drawString(this.font, line, col3X + 12, ty, DiaryTheme.INK, false);
            ty += 12;
        }

        col3Y += dossierH + 20;
        DiaryDecorator.drawSectionHeader(g, I18n.get("gripeverde.catalog.section.strokes"), col3X, col3Y, DiaryTheme.INK);
        col3Y += 25;

        g.drawString(this.font, I18n.get("gripeverde.catalog.stroke.handwritten"), col3X, col3Y, DiaryTheme.INK, false);
        InkEffects.handwrittenUnderline(g, col3X, col3Y + 12, col3X + 240, DiaryTheme.INK);
        col3Y += 25;
        g.drawString(this.font, I18n.get("gripeverde.catalog.stroke.quill"), col3X, col3Y, DiaryTheme.INK, false);
        InkEffects.quillStroke(g, col3X, col3Y + 12, col3X + 240, DiaryTheme.BLOOD_DRIED);
        col3Y += 25;
        g.drawString(this.font, I18n.get("gripeverde.catalog.stroke.torn"), col3X, col3Y, DiaryTheme.INK, false);
        InkEffects.tornEdge(g, col3X, col3Y + 20, col3X + 240, DiaryTheme.INK, 999L);

        int col3BottomY = this.height - 170;
        DiaryDecorator.drawSectionHeader(g, I18n.get("gripeverde.catalog.section.widgets"), col3X, col3BottomY, DiaryTheme.INK);
        g.drawString(this.font, I18n.get("gripeverde.catalog.widget.hint.1"), col3X, col3BottomY + 18, DiaryTheme.INK_FADED, false);
        g.drawString(this.font, I18n.get("gripeverde.catalog.widget.hint.2"), col3X, col3BottomY + 30, DiaryTheme.INK_FADED, false);
        g.drawString(this.font, I18n.get("gripeverde.catalog.widget.hint.3"), col3X, col3BottomY + 42, DiaryTheme.INK_FADED, false);

        g.drawString(this.font, I18n.get("gripeverde.catalog.widget.demos"), 330, this.height - 145, DiaryTheme.INK, false);

        String hint = I18n.get("gripeverde.catalog.hint.dev");
        g.drawString(this.font, hint, this.width - this.font.width(hint) - 8, this.height - 12, DiaryTheme.SEPIA_DARK, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private int drawSwatch(GuiGraphics g, int x, int y, String name, int color) {
        g.fill(x, y, x + 20, y + 10, color);
        g.fill(x, y, x + 20, y + 1, DiaryTheme.INK);
        g.fill(x, y + 9, x + 20, y + 10, DiaryTheme.INK);
        g.fill(x, y, x + 1, y + 10, DiaryTheme.INK);
        g.fill(x + 19, y, x + 20, y + 10, DiaryTheme.INK);
        g.drawString(this.font, name, x + 25, y + 1, DiaryTheme.INK, false);
        g.drawString(this.font, String.format("#%08X", color), x + 145, y + 1, DiaryTheme.SEPIA_DARK, false);
        return y + 12;
    }

    private boolean isHovered(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override public boolean isPauseScreen() { return false; }
}