package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

/**
 * DiaryPauseScreen v3.5 — papel claro real, cuervo posado y vela animada.
 * Mano zombi removida (se reserva para Death Screen).
 */
public class DiaryPauseScreen extends PauseScreen {

    private static final int PAPER_LIGHT     = 0xFFFAF0DA;
    private static final int PAPER_HIGHLIGHT = 0xFFFDF6E3;

    private static final int PANEL_MAX_W = 320;
    private static final int PANEL_MAX_H = 360;
    // Alto minimo para que quepan todos los ornamentos + botones sin superposicion
    private static final int PANEL_MIN_H = 260;
    private static final int MARGIN_TOP    = 20;
    private static final int MARGIN_BOTTOM_IDEAL = 55;  // hotbar completa
    private static final int MARGIN_BOTTOM_MIN   = 55;  // solo hotbar mesh
    private static final int MARGIN_SIDE   = 20;

    // Ornamentos internos (offsets relativos)
    private static final int ORN_TITLE_TOP    = 24;
    private static final int ORN_SUBTITLE_TOP = 58;
    private static final int ORN_SEP_TOP      = 80;
    private static final int ORN_SEP_BOT_FROM_BOT = 48;
    private static final int ORN_SIGN_FROM_BOT    = 28;

    private int panelW() {
        int available = this.width - MARGIN_SIDE * 2;
        return Math.min(PANEL_MAX_W, available);
    }
    private int panelH() {
        // Siempre respetar MARGIN_TOP y MARGIN_BOTTOM_MIN sin excepciones
        int available = this.height - MARGIN_TOP - MARGIN_BOTTOM_MIN;
        return Math.max(0, Math.min(PANEL_MAX_H, available));
    }

    private static final int BTN_W = 240;
    private static final int BTN_H = 22;
    private static final int BTN_GAP = 4;

    private int animTick = 0;

    // v0.21.27: Random cacheado para texturas (evita crear new Random cada frame)
    private static final java.util.Random PAPER_RNG = new java.util.Random();
    private static final java.util.Random BLOOD_RNG = new java.util.Random();
    private static final java.util.Random BREATH_RNG = new java.util.Random();

    // ── v0.16.0: la pausa responde ──
    private boolean candleOut = false;
    private long candleSmokeUntilMs = 0L;
    private static long nextBreathMs = System.currentTimeMillis() + 20_000L;
    private static long breathUntilMs = 0L;
    private static long breathStartMs = 0L;
    private static int breathCx = 0, breathCy = 0;
    private static long nextShadowMs = 0L;
    private static long shadowUntilMs = 0L;
    private static long shadowStartMs = 0L;

    public DiaryPauseScreen() {
        super(true);
    }

    private int panelX() { return (this.width - panelW()) / 2; }
    private int panelY() {
        // Pegar el panel al margen superior; si sobra espacio, dejarlo abajo para hotbar
        return MARGIN_TOP;
    }

    @Override
    protected void init() {
        int px = panelX();
        int py = panelY();
        int pw = panelW();
        int ph = panelH();

        // Zona disponible para los 5 botones (entre los dos separadores)
        int contentTop = py + ORN_SEP_TOP + 6;
        int contentBot = py + ph - ORN_SEP_BOT_FROM_BOT - 6;
        int contentH = Math.max(0, contentBot - contentTop);

        // Calcular altura de boton adaptativa
        // Formula: 5*bh + 4*gap = contentH  =>  bh = (contentH - 4*gap) / 5
        int gap = BTN_GAP;
        int bh = (contentH - gap * 4) / 5;
        if (bh > BTN_H) bh = BTN_H;         // no crecer mas alla del ideal
        if (bh < 14)    { bh = 14; gap = 2; } // piso minimo legible

        int totalBtnH = bh * 5 + gap * 4;
        int btnStartY = contentTop + Math.max(0, (contentH - totalBtnH) / 2);
        int btnX = px + (pw - BTN_W) / 2;

        // Redefinimos referencia local para el resto del metodo
        final int BH = bh;
        final int BG = gap;

        this.addRenderableWidget(new DiaryButton(
            btnX, btnStartY, BTN_W, BH,
            Component.translatable("gripeverde.pause.btn.resume"),
            DiaryButton.Icon.HAND, DiaryButton.Variant.PRIMARY,
            this::resumeGame
        ));

        DiaryButton recordsBtn = new DiaryButton(
            btnX, btnStartY + (BH + BG), BTN_W, BH,
            Component.translatable("gripeverde.pause.btn.records"),
            DiaryButton.Icon.SCROLL, DiaryButton.Variant.SECONDARY,
            this::openRecords
        );
        if (this.minecraft.getConnection() == null) recordsBtn.active = false;
        this.addRenderableWidget(recordsBtn);

        DiaryButton statsBtn = new DiaryButton(
            btnX, btnStartY + (BH + BG) * 2, BTN_W, BH,
            Component.translatable("gripeverde.pause.btn.statistics"),
            DiaryButton.Icon.CHART, DiaryButton.Variant.SECONDARY,
            this::openStats
        );
        if (this.minecraft.player == null) statsBtn.active = false;
        this.addRenderableWidget(statsBtn);

        this.addRenderableWidget(new DiaryButton(
            btnX, btnStartY + (BH + BG) * 3, BTN_W, BH,
            Component.translatable("gripeverde.pause.btn.adjustments"),
            DiaryButton.Icon.GEAR, DiaryButton.Variant.SECONDARY,
            this::openAdjustments
        ));

        this.addRenderableWidget(new DiaryButton(
            btnX, btnStartY + (BH + BG) * 4, BTN_W, BH,
            Component.translatable("gripeverde.pause.btn.leave"),
            DiaryButton.Icon.DOOR, DiaryButton.Variant.DANGER,
            this::onLeave
        ));
    }


    public void tick() {
        animTick++;
        // v0.16.0: el aliento del otro lado del vidrio.
        try {
            long now = System.currentTimeMillis();
            if (breathUntilMs == 0L && now >= nextBreathMs) {
                breathStartMs = now;
                breathUntilMs = now + 4600L;
                breathCx = panelX() + 24 + BREATH_RNG.nextInt(Math.max(10, panelW() - 48));
                breathCy = panelY() + 20 + BREATH_RNG.nextInt(Math.max(10, panelH() - 60));
                nextBreathMs = now + 26_000L + BREATH_RNG.nextLong() % 20_000L;
                if (nextBreathMs < now) nextBreathMs = now + 30_000L;
                com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                    com.eternalcraft.gripeverde.client.lore.Discoveries.BREATH);
            }
            if (breathUntilMs > 0L && now >= breathUntilMs) breathUntilMs = 0L;

            // La segunda sombra: solo para quien ya encontro bastante.
            if (com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.discoveredCount() >= 12) {
                if (shadowUntilMs == 0L && nextShadowMs > 0L && now >= nextShadowMs) {
                    shadowStartMs = now;
                    shadowUntilMs = now + 2400L;
                    nextShadowMs = 0L;
                    com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                        com.eternalcraft.gripeverde.client.lore.Discoveries.READER_SHADOW);
                }
                if (shadowUntilMs > 0L && now >= shadowUntilMs) {
                    shadowUntilMs = 0L;
                    nextShadowMs = now + 90_000L + (long)(BREATH_RNG.nextDouble() * 120_000L);
                }
                if (nextShadowMs == 0L && shadowUntilMs == 0L) {
                    nextShadowMs = now + 60_000L + (long)(BREATH_RNG.nextDouble() * 90_000L);
                }
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            try {
                int px = panelX(), py = panelY();
                // La vela: apagarla revela lo que lee la oscuridad.
                int candleX = px + 20, candleY = py + panelH() - 32;
                if (mouseX >= candleX - 6 && mouseX <= candleX + 20 && mouseY >= candleY - 14 && mouseY <= candleY + 28) {
                    candleOut = !candleOut;
                    if (candleOut) {
                        candleSmokeUntilMs = System.currentTimeMillis() + 2600L;
                        com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                            com.eternalcraft.gripeverde.client.lore.Discoveries.CANDLE_DARK);
                        com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                            com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_WOOD_CREAK.get(), 0.13f, 1.6f);
                    } else {
                        com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                            com.eternalcraft.gripeverde.client.audio.ModSounds.UI_CLICK_HEAVY.get(), 0.12f, 1.2f);
                    }
                    return true;
                }
                // El cuervo cobra su peaje.
                int ravenX = px + 14, ravenY = py + 22;
                if (mouseX >= ravenX - 4 && mouseX <= ravenX + 24 && mouseY >= ravenY - 4 && mouseY <= ravenY + 22) {
                    int n = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.bump("ravenClicks");
                    com.eternalcraft.gripeverde.client.audio.UiSounds.playCustom(
                        com.eternalcraft.gripeverde.client.audio.ModSounds.MENU_CROWS.get(), 0.22f, 1.45f);
                    if (n >= 5 && !com.eternalcraft.gripeverde.client.lore.ChroniclerMemory
                            .hasCollectible(com.eternalcraft.gripeverde.client.lore.Discoveries.ITEM_FEATHER)) {
                        com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.addCollectible(
                            com.eternalcraft.gripeverde.client.lore.Discoveries.ITEM_FEATHER);
                        com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                            com.eternalcraft.gripeverde.client.lore.Discoveries.RAVEN_FEATHER);
                    }
                    return true;
                }
            } catch (Throwable ignored) {}
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void resumeGame() {
        this.minecraft.setScreen(null);
    }

    private void openRecords() {
        ClientPacketListener conn = this.minecraft.getConnection();
        if (conn != null) {
            this.minecraft.setScreen(new AdvancementsScreen(conn.getAdvancements()));
        }
    }

    private void openStats() {
        if (this.minecraft.player != null) {
            this.minecraft.setScreen(new StatsScreen(this, this.minecraft.player.getStats()));
        }
    }

    private void openAdjustments() {
        this.minecraft.setScreen(new DiaryOptionsScreen(this));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0 && keyCode == GLFW.GLFW_KEY_0) {
            onLeave();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void onLeave() {
        if (this.minecraft == null || this.minecraft.level == null) {
            return;
        }

        // En Forge 1.20.1, Minecraft.disconnect() no es publico:
        // usamos clearLevel() y llamamos setScreen explicitamente al final,
        // que es lo que faltaba en la version anterior y causaba el freeze.
        boolean isLocal = this.minecraft.isLocalServer();
        this.minecraft.level.disconnect();

        if (isLocal) {
            this.minecraft.clearLevel(new GenericDirtMessageScreen(
                Component.translatable("menu.savingLevel")));
        } else {
            this.minecraft.clearLevel();
        }

        // Forzar la vuelta al TitleScreen explicitamente.
        // ScreenEventHandler lo intercepta y lo reemplaza por DiaryTitleScreen.
        this.minecraft.setScreen(new TitleScreen());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int overlay = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 180);
        g.fill(0, 0, this.width, this.height, overlay);

        drawVignette(g);

        int px = panelX();
        int py = panelY();
        drawPanel(g, px, py, panelW(), panelH());
        drawPanelTitle(g, px, py);
        drawDateStamp(g, px, py);
        drawPanelSeparators(g, px, py);

        // Elementos decorativos
        drawRaven(g, px + 14, py + 22);

        super.render(g, mouseX, mouseY, partialTick);

        // Elementos que van encima de los botones
        drawCandle(g, px + 20, py + panelH() - 32);
        drawSignature(g, px, py);
        drawWaxSeal(g, px + panelW() - 28, py + panelH() - 26);

        // ── v0.16.0: lo que trae la oscuridad ──
        try {
            if (candleOut) {
                // El panel queda en penumbra
                g.fill(px + 2, py + 2, px + panelW() - 2, py + panelH() - 2,
                    DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 96));
                // Solo a oscuras se lee esto
                long now = System.currentTimeMillis();
                int pulse = 60 + (int)(Math.sin(now / 650.0) * 24);
                boolean en = isEnglishSelected();
                String dark = en ? "the darkness reads too" : "la oscuridad tambien lee";
                int dw = this.font.width(dark);
                g.drawString(this.font, dark, px + (panelW() - dw) / 2, py + panelH() / 2,
                    (pulse << 24) | 0x8A9A70, false);
            }
            // El aliento en el vidrio
            long now = System.currentTimeMillis();
            if (breathUntilMs > now) {
                long age = now - breathStartMs;
                long remain = breathUntilMs - now;
                int a = 26;
                if (age < 1400L) a = (int)(26 * age / 1400L);
                else if (remain < 1400L) a = (int)(26 * remain / 1400L);
                if (a > 1) {
                    for (int r = 12; r > 2; r -= 3) {
                        int alpha = a * (13 - r) / 10;
                        if (alpha < 1) continue;
                        int col = (alpha << 24) | 0xD8E0D8;
                        g.fill(breathCx - r, breathCy - r / 2, breathCx + r, breathCy + r / 2, col);
                    }
                }
            }
            // La segunda sombra
            if (shadowUntilMs > now) {
                long age = now - shadowStartMs;
                int a = 40;
                if (age < 500L) a = (int)(40 * age / 500L);
                else if (shadowUntilMs - now < 500L) a = (int)(40 * (shadowUntilMs - now) / 500L);
                int sx = px + panelW() - 46;
                int sy = py + 30;
                int col = (a << 24) | 0x000000;
                // Silueta: cabeza + hombros, inclinada sobre algo
                g.fill(sx + 6, sy, sx + 14, sy + 8, col);
                g.fill(sx, sy + 8, sx + 22, sy + 26, col);
                g.fill(sx + 2, sy + 26, sx + 20, sy + 30, col);
            }
            com.eternalcraft.gripeverde.client.lore.Marginalia.poll();
            com.eternalcraft.gripeverde.client.lore.Marginalia.drawHorizontal(g, this.width / 2, py - 12);
            com.eternalcraft.gripeverde.client.lore.Discoveries.drawToast(g, this.width);
        } catch (Throwable ignored) {}
    }

    private void drawVignette(GuiGraphics g) {
        int steps = 40;
        for (int i = 0; i < steps; i++) {
            float t = (float)(steps - i) / steps;
            int a = (int)(t * t * 70);
            int c = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, a);
            g.fill(i, 0, i + 1, this.height, c);
            g.fill(this.width - i - 1, 0, this.width - i, this.height, c);
            g.fill(0, i, this.width, i + 1, c);
            g.fill(0, this.height - i - 1, this.width, this.height - i, c);
        }
    }

    // ================================================================
    // PANEL — FIX papel claro real (orden de render corregido)
    // ================================================================

    private void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
        // Sombra
        int shadowColor = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 200);
        g.fill(x + 5, y + 5, x + w + 5, y + h + 5, shadowColor);

        // Base opaca — papel crema claro
        g.fill(x, y, x + w, y + h, PAPER_LIGHT);

        // ORDEN NUEVO: manchas primero, brillo despues (brillo debe estar encima)

        // 1. Manchas de sangre — solo 2, sutiles
        drawBloodSpotWithSplatter(g, x + w - 50, y + h - 90, 6);
        drawBloodSpotWithSplatter(g, x + w - 30, y + 30, 4);

        // 2. Textura minima (puntitos sepia)
        drawPaperTextureMinimal(g, x, y, w, h);

        // 3. Capa de brillo AGRESIVA (alpha 200, cubre todo el panel)
        for (int i = 0; i < h; i++) {
            float t = 1f - Math.abs(i - h * 0.4f) / (h * 0.7f);
            if (t < 0) t = 0;
            int alpha = (int)(t * 140);
            if (alpha > 0) {
                int c = DiaryTheme.withAlpha(PAPER_HIGHLIGHT, alpha);
                g.fill(x + 4, y + i, x + w - 4, y + i + 1, c);
            }
        }

        // 4. Marco perimetral (siempre al final para que no lo cubra el brillo)
        drawDoubleBorder(g, x, y, w, h);
        drawCornerOrnaments(g, x, y, w, h);
    }

    private void drawPaperTextureMinimal(GuiGraphics g, int x, int y, int w, int h) {
        PAPER_RNG.setSeed(x * 31L + y * 17L);
        for (int i = 0; i < 15; i++) {
            if (w <= 8 || h <= 8) continue;
            int px = x + 4 + PAPER_RNG.nextInt(w - 8);
            int py = y + 4 + PAPER_RNG.nextInt(h - 8);
            int alpha = 6 + PAPER_RNG.nextInt(8);
            int color = DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, alpha);
            g.fill(px, py, px + 1, py + 1, color);
        }
    }

    private void drawBloodSpotWithSplatter(GuiGraphics g, int cx, int cy, int r) {
        for (int dy = -r; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                int d2 = dx * dx + dy * dy;
                if (d2 <= r * r) {
                    float t = 1f - (float) Math.sqrt(d2) / r;
                    int alpha = (int)(t * 140);
                    int c = DiaryTheme.withAlpha(DiaryTheme.BLOOD_DRIED, alpha);
                    g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, c);
                }
            }
        }
        g.fill(cx - 2, cy - 2, cx + 2, cy + 2, DiaryTheme.BLOOD_DRIED);

        BLOOD_RNG.setSeed(cx * 7L + cy * 13L);
        for (int i = 0; i < 10; i++) {
            int sx = cx + BLOOD_RNG.nextInt(r * 3) - r;
            int sy = cy + BLOOD_RNG.nextInt(r * 3) - r;
            int size = 1 + BLOOD_RNG.nextInt(2);
            int alpha = 100 + BLOOD_RNG.nextInt(100);
            int c = DiaryTheme.withAlpha(DiaryTheme.BLOOD_DRIED, alpha);
            g.fill(sx, sy, sx + size, sy + size, c);
        }
    }

    private void drawDoubleBorder(GuiGraphics g, int x, int y, int w, int h) {
        int outer = DiaryTheme.SEPIA_DARK;
        int inner = DiaryTheme.BRONZE_DARK;
        drawRect(g, x, y, x + w, y + h, outer);
        drawRect(g, x + 2, y + 2, x + w - 2, y + h - 2, inner);
    }

    private void drawRect(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private void drawCornerOrnaments(GuiGraphics g, int x, int y, int w, int h) {
        int size = 10;
        int c = DiaryTheme.GOLD_TARNISHED;
        g.fill(x - 1, y - 1, x + size, y + 1, c);
        g.fill(x - 1, y - 1, x + 1, y + size, c);
        g.fill(x + w - size, y - 1, x + w + 1, y + 1, c);
        g.fill(x + w - 1, y - 1, x + w + 1, y + size, c);
        g.fill(x - 1, y + h - 1, x + size, y + h + 1, c);
        g.fill(x - 1, y + h - size, x + 1, y + h + 1, c);
        g.fill(x + w - size, y + h - 1, x + w + 1, y + h + 1, c);
        g.fill(x + w - 1, y + h - size, x + w + 1, y + h + 1, c);
    }

    // ================================================================
    // TITULO / FECHA / SEPARADORES / FIRMA / SELLO
    // ================================================================

    private void drawPanelTitle(GuiGraphics g, int px, int py) {
        String title = Component.translatable("gripeverde.pause.title").getString();
        String subtitle = Component.translatable("gripeverde.pause.subtitle").getString();

        int centerX = px + panelW() / 2;
        int titleY = py + 24;
        int subtitleY = py + 58;

        int tw = this.font.width(title);
        // v0.21.25: tinta limpia sin sombra doble.
        g.pose().pushPose();
        g.pose().translate(centerX, titleY, 0);
        g.pose().scale(1.75f, 1.75f, 1f);
        g.drawString(this.font, title, -tw / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();

        int scaledTw = (int)(tw * 1.75f);
        int ornY = titleY + 5;
        drawFlourish(g, centerX - scaledTw / 2 - 20, ornY, DiaryTheme.BRONZE, false);
        drawFlourish(g, centerX + scaledTw / 2 + 4, ornY, DiaryTheme.BRONZE, true);

        int sw = this.font.width(subtitle);
        g.drawString(this.font, subtitle,
            centerX - sw / 2, subtitleY,
            DiaryTheme.SEPIA_MID, false);
    }

    private void drawDateStamp(GuiGraphics g, int px, int py) {
        String date = "23 · III · 1897";
        int dw = this.font.width(date);
        int dx = px + panelW() - dw - 12;
        int dy = py + 12;
        g.drawString(this.font, date, dx, dy, DiaryTheme.SEPIA_DARK, false);
    }

    private void drawFlourish(GuiGraphics g, int x, int y, int color, boolean mirror) {
        if (!mirror) {
            g.fill(x,      y + 3, x + 8,  y + 4, color);
            g.fill(x + 8,  y + 2, x + 10, y + 3, color);
            g.fill(x + 10, y + 1, x + 12, y + 2, color);
            g.fill(x + 12, y + 3, x + 14, y + 4, color);
            g.fill(x + 14, y + 4, x + 16, y + 5, color);
            g.fill(x + 8, y + 5, x + 10, y + 7, color);
        } else {
            g.fill(x + 8,  y + 3, x + 16, y + 4, color);
            g.fill(x + 6,  y + 2, x + 8,  y + 3, color);
            g.fill(x + 4,  y + 1, x + 6,  y + 2, color);
            g.fill(x + 2,  y + 3, x + 4,  y + 4, color);
            g.fill(x,      y + 4, x + 2,  y + 5, color);
            g.fill(x + 6, y + 5, x + 8, y + 7, color);
        }
    }

    private void drawPanelSeparators(GuiGraphics g, int px, int py) {
        int centerX = px + panelW() / 2;
        int sepW = panelW() - 60;
        int lineC = DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 200);
        int accentC = DiaryTheme.BRONZE;

        drawSepLine(g, centerX, py + ORN_SEP_TOP, sepW, lineC, accentC);
        drawSepLine(g, centerX, py + panelH() - ORN_SEP_BOT_FROM_BOT, sepW, lineC, accentC);
    }

    private void drawSepLine(GuiGraphics g, int cx, int y, int width, int lineC, int accentC) {
        int half = width / 2;
        int gap = 6;
        g.fill(cx - half, y, cx - gap, y + 1, lineC);
        g.fill(cx + gap, y, cx + half, y + 1, lineC);
        g.fill(cx - 2, y - 1, cx + 3, y + 2, accentC);
        g.fill(cx - 1, y - 2, cx + 2, y + 3, accentC);
    }

    private void drawSignature(GuiGraphics g, int px, int py) {
        Component sig = Component.literal(isEnglishSelected()
                ? "Signed: S. Pereira, M.D."
                : "Firmado: S. Pereira, Medico")
            .withStyle(ChatFormatting.ITALIC);
        int centerX = px + panelW() / 2;
        int sw = this.font.width(sig);
        int sy = py + panelH() - ORN_SIGN_FROM_BOT;
        // v0.21.25: firma limpia sin sombra.
        g.drawString(this.font, sig, centerX - sw / 2, sy, DiaryTheme.SEPIA_DARK, false);
        // v0.16.0: tras bastantes hallazgos, la firma reconoce al lector.
        try {
            if (com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.discoveredCount() >= 10) {
                String add = isEnglishSelected() ? "- and of whoever reads -" : "- y de quien lee -";
                int aw = this.font.width(add);
                g.drawString(this.font, add, centerX - aw / 2, sy - 10,
                    DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 130), false);
            }
        } catch (Throwable ignored) {}
    }

    private static boolean isEnglishSelected() {
        try {
            String lang = net.minecraft.client.Minecraft.getInstance().getLanguageManager().getSelected();
            return lang == null || lang.startsWith("en_") || lang.equals("en_us");
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void drawWaxSeal(GuiGraphics g, int cx, int cy) {
        int r = 14;
        for (int dy = -r; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                if (dx * dx + dy * dy <= r * r) {
                    g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, DiaryTheme.WAX_SEAL);
                }
            }
        }
        for (int dy = -r; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                int d2 = dx * dx + dy * dy;
                if (d2 <= r * r && d2 >= (r - 2) * (r - 2)) {
                    g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, DiaryTheme.BLOOD_DRIED);
                }
            }
        }
        g.fill(cx - 7, cy - 7, cx - 3, cy - 4, DiaryTheme.withAlpha(DiaryTheme.PAPER, 90));

        String year = "1897";
        int tw = this.font.width(year);
        g.drawString(this.font, year, cx - tw / 2, cy - 3, DiaryTheme.PAPER, false);
    }

    // ================================================================
    // CUERVO POSADO — esquina superior izquierda del panel
    // ================================================================

    private void drawRaven(GuiGraphics g, int x, int y) {
        // Cuervo pequeño, silueta lateral, mirando a la derecha
        // Tamaño: ~20x18 px

        int black = 0xFF0A0503;
        int darkGray = 0xFF1F1408;
        int highlight = DiaryTheme.SEPIA_DARK;
        int eye = 0xFFD4941E;  // ojo ambar

        // Cuerpo principal (elipse aproximada)
        g.fill(x + 2, y + 6, x + 14, y + 14, black);
        g.fill(x + 3, y + 5, x + 15, y + 6, black);
        g.fill(x + 4, y + 14, x + 14, y + 15, black);
        g.fill(x + 5, y + 15, x + 13, y + 16, black);

        // Cabeza (arriba del cuerpo)
        g.fill(x + 10, y + 2, x + 17, y + 8, black);
        g.fill(x + 11, y + 1, x + 16, y + 2, black);

        // Pico (apuntando a la derecha)
        g.fill(x + 17, y + 4, x + 20, y + 6, black);
        g.fill(x + 18, y + 5, x + 20, y + 5, darkGray);

        // Ojo (punto ambar)
        g.fill(x + 14, y + 4, x + 15, y + 5, eye);

        // Ala plegada (detalle en el cuerpo)
        g.fill(x + 5, y + 8, x + 12, y + 9, darkGray);
        g.fill(x + 6, y + 10, x + 13, y + 11, darkGray);

        // Cola (extremo derecho del cuerpo)
        g.fill(x, y + 8, x + 4, y + 11, black);
        g.fill(x - 1, y + 9, x + 2, y + 10, black);

        // Patas (dos lineas cortas debajo)
        g.fill(x + 6, y + 16, x + 7, y + 18, black);
        g.fill(x + 10, y + 16, x + 11, y + 18, black);
        // Garras
        g.fill(x + 5, y + 18, x + 8, y + 19, black);
        g.fill(x + 9, y + 18, x + 12, y + 19, black);
    }

    // ================================================================
    // VELA CON LLAMA ANIMADA — esquina inferior izquierda del panel
    // ================================================================

    private void drawCandle(GuiGraphics g, int x, int y) {
        // Vela de sebo con llama animada
        // Tamaño total: ~14x28 (candelero + vela + llama + halo)
        // Coord (x,y) es la base del candelero

        int candleWax = DiaryTheme.CANDLE_WAX;    // amarillento pálido
        int candleShadow = 0xFFA88B5C;             // sombra de la cera
        int wick = 0xFF1A0F05;                     // pabilo negro
        int flameCore = 0xFFFFAA33;                // ámbar brillante
        int flameOuter = 0xFFD4941E;               // ámbar mediano
        int flameHalo = DiaryTheme.AMBER_GLOW;     // halo cálido
        int metalBase = DiaryTheme.BRONZE_DARK;    // base del candelero
        int metalHighlight = DiaryTheme.BRONZE;

        // BASE DEL CANDELERO (plato metálico)
        g.fill(x, y + 20, x + 14, y + 24, metalBase);
        g.fill(x + 1, y + 19, x + 13, y + 20, metalBase);
        g.fill(x + 2, y + 24, x + 12, y + 26, metalBase);
        // Highlight en la base
        g.fill(x + 2, y + 20, x + 8, y + 21, metalHighlight);

        // COLUMNA/PIE del candelero
        g.fill(x + 5, y + 14, x + 9, y + 20, metalBase);
        g.fill(x + 6, y + 14, x + 7, y + 20, metalHighlight);

        // CERA DE LA VELA (cuerpo blanco)
        g.fill(x + 4, y + 6, x + 10, y + 14, candleWax);
        // Sombra derecha de la cera
        g.fill(x + 8, y + 6, x + 10, y + 14, candleShadow);
        // Chorretones de cera (goteos)
        g.fill(x + 3, y + 10, x + 4, y + 13, candleWax);
        g.fill(x + 10, y + 12, x + 11, y + 14, candleWax);

        // PABILO (mecha)
        g.fill(x + 6, y + 4, x + 7, y + 6, wick);

        // v0.16.0: vela apagada = humo breve, nada de llama.
        if (candleOut) {
            long nowSmoke = System.currentTimeMillis();
            if (nowSmoke < candleSmokeUntilMs) {
                float t = (nowSmoke % 700L) / 700f;
                int sx = x + 6 + (int)(Math.sin(nowSmoke / 210.0) * 2);
                int a = (int)(46 * (1f - t));
                for (int i = 0; i < 4; i++) {
                    g.fill(sx + i, y - 4 - i * 3 - (int)(t * 6), sx + i + 1, y - 2 - i * 3 - (int)(t * 6),
                        (Math.max(4, a - i * 8) << 24) | 0x909090);
                }
            }
            // Mecha negra sin llama
            g.fill(x + 6, y + 3, x + 7, y + 5, wick);
            return;
        }

        // LLAMA ANIMADA — parpadeo sutil
        int flameFrame = (animTick / 4) % 4;

        // Halo (cálido, envuelve todo) — v0.21.27: optimizado con fillCircle
        int haloAlpha = 60 + (flameFrame * 15);
        // Halo exterior suave
        for (int r = 8; r > 2; r -= 3) {
            float t = 1f - (float)r / 8f;
            int a = (int)(t * haloAlpha);
            if (a <= 2) continue;
            int col = DiaryTheme.withAlpha(flameHalo, a);
            for (int dy = -r; dy <= r; dy++) {
                int dx = (int)Math.sqrt(Math.max(0, r * r - dy * dy));
                g.fill(x + 6 - dx, y + 1 + dy, x + 6 + dx + 1, y + 1 + dy + 1, col);
            }
        }

        // Llama exterior (forma de gota)
        int flameJitter = (flameFrame == 1 || flameFrame == 3) ? 1 : 0;
        g.fill(x + 5 + flameJitter, y - 2, x + 8 + flameJitter, y + 4, flameOuter);
        g.fill(x + 4 + flameJitter, y,     x + 9 + flameJitter, y + 3, flameOuter);
        g.fill(x + 6 + flameJitter, y - 3, x + 7 + flameJitter, y - 2, flameOuter);

        // Llama interior (nucleo brillante)
        g.fill(x + 6 + flameJitter, y - 1, x + 7 + flameJitter, y + 3, flameCore);
        g.fill(x + 5 + flameJitter, y + 1, x + 8 + flameJitter, y + 3, flameCore);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    /**
     * Sobreescribe renderBackground vanilla para que NO dibuje el overlay oscuro
     * de Screen sobre el mundo. Nosotros ya dibujamos nuestro propio fondo en render().
     * Sin este override, PauseScreen (super) dibuja un overlay traslucido negro
     * que oscurece nuestro panel.
     */
    @Override
    public void renderBackground(GuiGraphics g) {
        // Vacio intencionalmente. Nuestro render() ya maneja el fondo.
    }
}
