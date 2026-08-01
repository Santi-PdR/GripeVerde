package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.audio.MenuAmbience;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.render.PaperTexture;
import com.eternalcraft.gripeverde.client.render.PosterRenderer;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Reemplaza la DeathScreen vanilla por un acta de defuncion victoriana.
 *
 * Diseno v0.13.0:
 * - Overlay sepia rojo oscuro (sin escena viva - el jugador esta muerto).
 * - Panel pergamino con cosmetica completa (esquineros, sello, splatter).
 * - N de acta random, fecha/hora real del sistema, testigo del pool.
 * - Causa narrativa del pool + causa vanilla como anotacion al pie.
 * - Confirm cooldown de 20 ticks (respeta patron vanilla).
 * - Hardcore: titulo distinto, N con dagger, sin respawn.
 */
public class DiaryDeathScreen extends Screen {

    private static final int OVERLAY_COL = 0x90200604;
    private static final int PANEL_MAX_W = 380;
    private static final int PANEL_MAX_H = 340;
    private static final int SCREEN_MARGIN = 20;

    private static final int INK_TITLE   = DiaryChrome.INK_TITLE;
    private static final int INK_BODY    = 0xFF3A2416;
    private static final int INK_FADED   = 0xFF7A5A3A;

    private static final String[] CAUSE_POOL = {
        "Sucumbio a la fiebre verde",
        "Colapso pulmonar por vapores",
        "Complicaciones no especificadas",
        "Herida septica sin tratamiento",
        "Ataque de bestia rabiosa",
        "Hallado sin signos vitales",
        "Causas oscuras del padecimiento",
        "Anotado en el registro de desapariciones"
    };

    private static final String[] WITNESS_POOL = {
        "Dr. S. Pereira",
        "Cronista R. Da Rosa",
        "Cap. G. Bandeira",
        "Prof. A. Focco"
    };

    private final Component causeOfDeath;
    private final boolean hardcore;

    private final int deathId;
    private final String causeNarrative;
    private final String witness;
    private final String dateStr;
    private final String timeStr;
    private final int wealth;

    private int cooldownTicks = 0;
    private DiaryButton btnRespawn;
    private DiaryButton btnTitle;

    public DiaryDeathScreen(Component causeOfDeath, boolean hardcore) {
        super(Component.translatable("gripeverde.death.title"));
        this.causeOfDeath = causeOfDeath;
        this.hardcore = hardcore;

        Random r = new Random();
        this.deathId = 1000 + r.nextInt(9000);
        this.causeNarrative = CAUSE_POOL[r.nextInt(CAUSE_POOL.length)];
        this.witness = WITNESS_POOL[r.nextInt(WITNESS_POOL.length)];

        LocalDateTime now = LocalDateTime.now();
        this.dateStr = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        this.timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm"));

        var mc = Minecraft.getInstance();
        this.wealth = (mc.player != null) ? mc.player.experienceLevel : 0;
    }

    @Override
    protected void init() {
        this.cooldownTicks = 20;

        int panelW = Math.min(PANEL_MAX_W, this.width - SCREEN_MARGIN * 2);
        int panelH = Math.min(PANEL_MAX_H, this.height - SCREEN_MARGIN * 2);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int btnW = 140;
        int btnH = 22;
        int btnY = panelY + panelH - 40;

        if (hardcore) {
            btnTitle = new DiaryButton(
                panelX + (panelW - btnW) / 2, btnY, btnW, btnH,
                Component.translatable("gripeverde.death.button.delete_world"),
                DiaryButton.Icon.DOOR, DiaryButton.Variant.DANGER,
                this::onTitleScreen);
            addRenderableWidget(btnTitle);
        } else {
            int gap = 12;
            int totalW = btnW * 2 + gap;
            int startX = panelX + (panelW - totalW) / 2;

            btnRespawn = new DiaryButton(
                startX, btnY, btnW, btnH,
                Component.translatable("gripeverde.death.button.respawn"),
                DiaryButton.Icon.NONE, DiaryButton.Variant.PRIMARY,
                this::onRespawn);
            addRenderableWidget(btnRespawn);

            btnTitle = new DiaryButton(
                startX + btnW + gap, btnY, btnW, btnH,
                Component.translatable("gripeverde.death.button.title"),
                DiaryButton.Icon.DOOR, DiaryButton.Variant.SECONDARY,
                this::onTitleScreen);
            addRenderableWidget(btnTitle);
        }
    }

    private void onRespawn() {
        if (cooldownTicks > 0) return;
        var mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.respawn();
        }
        mc.setScreen(null);
    }

    private void onTitleScreen() {
        if (cooldownTicks > 0) return;
        var mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.level.disconnect();
        }
        mc.clearLevel();
        mc.setScreen(new TitleScreen());
    }

    @Override
    public void tick() {
        super.tick();
        if (cooldownTicks > 0) cooldownTicks--;

        if (btnRespawn != null) btnRespawn.active = (cooldownTicks == 0);
        if (btnTitle != null) btnTitle.active = (cooldownTicks == 0);

        MenuMusic.tick();
        MenuAmbience.tick();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        // 1. Fondo oscuro sepia rojo (sin escena viva)
        gfx.fill(0, 0, this.width, this.height, OVERLAY_COL);

        int panelW = Math.min(PANEL_MAX_W, this.width - SCREEN_MARGIN * 2);
        int panelH = Math.min(PANEL_MAX_H, this.height - SCREEN_MARGIN * 2);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        // 2. Sombra proyectada del panel
        DiaryChrome.drawPanelShadow(gfx, panelX, panelY, panelW, panelH);

        // 3. Panel pergamino
        PaperTexture.draw(gfx, panelX, panelY, panelW, panelH);

        // 4. Manchas de sangre (deja fondo dramatico)
        PosterRenderer.drawBloodSplatter(gfx, panelX, panelY, panelW, panelH, (long) deathId);

        // 5. Esquineros victorianos
        DiaryChrome.drawCornerOrnaments(gfx, panelX, panelY, panelW, panelH);

        // 6. Contenido del acta
        int cx = panelX + panelW / 2;
        int cy = panelY + 22;

        // Titulo: "Acta de Defuncion" o "Acta de Defuncion Definitiva"
        String titleKey = hardcore ? "gripeverde.death.title_hardcore" : "gripeverde.death.title";
        Component titleComp = Component.translatable(titleKey);
        int titleW = this.font.width(titleComp);
        gfx.drawString(this.font, titleComp, cx - titleW / 2, cy, INK_TITLE, false);
        // Sombra suave
        gfx.drawString(this.font, titleComp, cx - titleW / 2 + 1, cy + 1, 0x40201409, false);

        // Numero de acta
        cy += 14;
        String prefix = hardcore ? "N.\u00ba \u2020" : "N.\u00ba ";
        String numStr = prefix + String.format("%04d", deathId);
        int numW = this.font.width(numStr);
        gfx.drawString(this.font, numStr, cx - numW / 2, cy, INK_BODY, false);

        // Divisor
        cy += 12;
        DiaryChrome.drawInkDivider(gfx, panelX + 30, cy, panelW - 60);

        // Cuerpo del acta
        cy += 10;
        drawFieldLine(gfx, panelX + 30, cy,
            Component.translatable("gripeverde.death.field.cause"), causeNarrative);
        cy += 12;
        drawFieldLine(gfx, panelX + 30, cy,
            Component.translatable("gripeverde.death.field.date"), dateStr);
        cy += 12;
        drawFieldLine(gfx, panelX + 30, cy,
            Component.translatable("gripeverde.death.field.time"), timeStr);
        cy += 12;
        drawFieldLine(gfx, panelX + 30, cy,
            Component.translatable("gripeverde.death.field.witness"), witness);
        cy += 12;
        drawFieldLine(gfx, panelX + 30, cy,
            Component.translatable("gripeverde.death.field.wealth"),
            wealth + " " + Component.translatable("gripeverde.death.currency").getString());

        // Anotacion tecnica (causa vanilla si existe)
        if (causeOfDeath != null && !causeOfDeath.getString().isEmpty()) {
            cy += 16;
            Component annotation = Component.translatable("gripeverde.death.field.annotation");
            gfx.drawString(this.font, annotation, panelX + 30, cy, INK_FADED, false);
            cy += 10;
            String vanillaCause = causeOfDeath.getString();
            if (vanillaCause.length() > 60) {
                vanillaCause = vanillaCause.substring(0, 57) + "...";
            }
            Component italic = Component.literal(vanillaCause).withStyle(ChatFormatting.ITALIC);
            gfx.drawString(this.font, italic, panelX + 30, cy, INK_FADED, false);
        }

        // 7. Sello de cera roja (helper posiciona internamente en esquina inferior derecha)
        PaperTexture.drawWaxSeal(gfx, panelX, panelY, panelW, panelH);

        // 8. Widgets (botones)
        super.render(gfx, mouseX, mouseY, partialTick);

        // 9. Indicador de cooldown si activo
        if (cooldownTicks > 0) {
            String cdText = "..." + (cooldownTicks / 4 + 1);
            int cdW = this.font.width(cdText);
            gfx.drawString(this.font, cdText, cx - cdW / 2, panelY + panelH - 16, INK_FADED, false);
        }
    }

    private void drawFieldLine(GuiGraphics gfx, int x, int y, Component label, String value) {
        String labelStr = label.getString();
        gfx.drawString(this.font, labelStr, x, y, INK_FADED, false);
        int lw = this.font.width(labelStr);
        gfx.drawString(this.font, value, x + lw + 4, y, INK_BODY, false);
    }
}