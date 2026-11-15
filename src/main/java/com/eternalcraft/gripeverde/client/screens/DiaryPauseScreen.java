package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;

import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;

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
        return Math.min(PANEL_MAX_W, Math.max(148, available));
    }
    private int panelH() {
        // Preserve the hotbar and the minimum top margin while adapting the paper to the viewport.
        int available = this.height - MARGIN_TOP - MARGIN_BOTTOM_MIN;
        return Math.max(0, Math.min(PANEL_MAX_H, available));
    }

    private boolean compactPauseLayout() {
        return panelH() < 250 || panelW() < 280;
    }

    private int separatorTopOffset() {
        return panelH() < 250 ? 60 : ORN_SEP_TOP;
    }

    private int separatorBottomOffset() {
        return panelH() < 250 ? 30 : ORN_SEP_BOT_FROM_BOT;
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

    /**
     * v0.178.0: la vela y el cuervo se comprobaban antes que los widgets y
     * devolvian true. En la rejilla de dos columnas de las ventanas bajas la
     * vela cae 10 px dentro del ultimo boton de la izquierda, asi que apagarla
     * se comia el clic de "guia de campo".
     */
    private boolean overAnyButton(double mouseX, double mouseY) {
        for (net.minecraft.client.gui.components.events.GuiEventListener child : this.children()) {
            if (child instanceof net.minecraft.client.gui.components.AbstractWidget widget
                    && widget.visible && widget.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
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

        // Short viewports use a two-column menu so every action stays above the hotbar.
        boolean compactGrid = ph < 250;
        int topSeparator = compactGrid ? 60 : ORN_SEP_TOP;
        int bottomSeparator = compactGrid ? 30 : ORN_SEP_BOT_FROM_BOT;
        int contentTop = py + topSeparator + 6;
        int contentBot = py + ph - bottomSeparator - 6;
        int contentH = Math.max(0, contentBot - contentTop);
        int rows = compactGrid ? 4 : 7;   // v0.188.0: entra el parte de CEDA
        int gap = compactGrid ? 3 : BTN_GAP;
        int bh = (contentH - gap * (rows - 1)) / rows;
        int minButtonH = compactGrid ? 12 : 14;
        if (bh > BTN_H) bh = BTN_H;
        if (bh < minButtonH) { bh = minButtonH; gap = 2; }

        int totalBtnH = bh * rows + gap * (rows - 1);
        int btnStartY = contentTop + Math.max(0, (contentH - totalBtnH) / 2);
        int buttonW = Math.max(96, Math.min(BTN_W, pw - 24));
        int[] buttonX = new int[7];
        int[] buttonY = new int[7];
        int[] buttonWidths = new int[7];
        if (compactGrid) {
            int availableButtonW = Math.max(96, pw - 24);
            int columnGap = Math.min(8, Math.max(2, availableButtonW / 12));
            int columnW = Math.max(48, (availableButtonW - columnGap) / 2);
            for (int i = 0; i < 7; i++) {
                int row = i / 2;
                int column = i % 2;
                buttonWidths[i] = columnW;
                buttonX[i] = px + 12 + column * (columnW + columnGap);
                buttonY[i] = btnStartY + row * (bh + gap);
            }
        } else {
            int buttonXCentered = px + (pw - buttonW) / 2;
            for (int i = 0; i < 7; i++) {
                buttonX[i] = buttonXCentered;
                buttonY[i] = btnStartY + i * (bh + gap);
                buttonWidths[i] = buttonW;
            }
        }

        this.addRenderableWidget(createPauseButton(buttonX[0], buttonY[0], buttonWidths[0], bh,
            "gripeverde.pause.btn.resume", DiaryButton.Icon.HAND, DiaryButton.Variant.PRIMARY, this::resumeGame));

        DiaryButton recordsBtn = createPauseButton(buttonX[1], buttonY[1], buttonWidths[1], bh,
            "gripeverde.pause.btn.records", DiaryButton.Icon.SCROLL, DiaryButton.Variant.SECONDARY, this::openRecords);
        if (this.minecraft.getConnection() == null) recordsBtn.active = false;
        this.addRenderableWidget(recordsBtn);

        DiaryButton statsBtn = createPauseButton(buttonX[2], buttonY[2], buttonWidths[2], bh,
            "gripeverde.pause.btn.statistics", DiaryButton.Icon.CHART, DiaryButton.Variant.SECONDARY, this::openStats);
        if (this.minecraft.player == null) statsBtn.active = false;
        this.addRenderableWidget(statsBtn);

        this.addRenderableWidget(createPauseButton(buttonX[3], buttonY[3], buttonWidths[3], bh,
            "gripeverde.pause.btn.adjustments", DiaryButton.Icon.GEAR, DiaryButton.Variant.SECONDARY, this::openAdjustments));

        String fieldGuideLabel = compactGrid
            ? "gripeverde.pause.btn.field_guide.compact" : "gripeverde.pause.btn.field_guide";
        this.addRenderableWidget(createPauseButton(buttonX[4], buttonY[4], buttonWidths[4], bh,
            fieldGuideLabel, "gripeverde.pause.btn.field_guide",
            DiaryButton.Icon.SCROLL, DiaryButton.Variant.SECONDARY, this::openFieldGuide));

        // v0.188.0: el parte de CEDA se abre tambien desde la pausa, que es donde
        // se lo necesita: escribir el mensaje rapido sin salir de la partida.
        this.addRenderableWidget(createPauseButton(buttonX[5], buttonY[5], buttonWidths[5], bh,
            "gripeverde.pause.btn.ceda", DiaryButton.Icon.CHAT, DiaryButton.Variant.SECONDARY,
            this::openDispatch));

        this.addRenderableWidget(createPauseButton(buttonX[6], buttonY[6], buttonWidths[6], bh,
            "gripeverde.pause.btn.leave", DiaryButton.Icon.DOOR, DiaryButton.Variant.DANGER, this::onLeave));
    }

    /** v0.188.0: el parte de CEDA desde la pausa, sin cerrar la partida. */
    private void openDispatch() {
        this.minecraft.setScreen(new DiaryDispatchScreen(this));
    }

    private DiaryButton createPauseButton(int x, int y, int width, int height, String key,
                                          DiaryButton.Icon icon, DiaryButton.Variant variant, Runnable action) {
        return createPauseButton(x, y, width, height, key, key, icon, variant, action);
    }

    private DiaryButton createPauseButton(int x, int y, int width, int height, String key,
                                          String tooltipKey, DiaryButton.Icon icon,
                                          DiaryButton.Variant variant, Runnable action) {
        Component label = Component.translatable(key);
        DiaryButton button = new DiaryButton(x, y, width, height, label, icon, variant, action);
        button.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
        return button;
    }

    public void tick() {
        if (hasActiveWorld()) return;
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
                boolean overButton = overAnyButton(mouseX, mouseY);
                if (!overButton && mouseX >= candleX - 6 && mouseX <= candleX + 20
                        && mouseY >= candleY - 14 && mouseY <= candleY + 28) {
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
                if (!overButton && mouseX >= ravenX - 4 && mouseX <= ravenX + 24
                        && mouseY >= ravenY - 4 && mouseY <= ravenY + 22) {
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

    private void openFieldGuide() {
        this.minecraft.setScreen(new DiaryFieldGuideScreen(this));
    }

    private void onLeave() {
        if (this.minecraft == null || this.minecraft.level == null) {
            return;
        }

        if (!this.minecraft.isLocalServer()) {
            this.minecraft.setScreen(new DiaryConfirmScreen(accepted -> {
                if (accepted) {
                    disconnectAndReturnToTitle();
                } else {
                    this.minecraft.setScreen(this);
                }
            },
                Component.translatable("gripeverde.pause.leave.confirm.title"),
                Component.translatable("gripeverde.pause.leave.confirm.message"),
                Component.translatable("gripeverde.pause.leave.confirm.accept"),
                Component.translatable("gui.cancel"),
                this));
            return;
        }

        disconnectAndReturnToTitle();
    }

    private void disconnectAndReturnToTitle() {
        if (this.minecraft == null || this.minecraft.level == null) return;

        // In Forge 1.20.1, disconnect through clearLevel and set the title explicitly.
        boolean isLocal = this.minecraft.isLocalServer();
        this.minecraft.level.disconnect();

        if (isLocal) {
            this.minecraft.clearLevel(new GenericDirtMessageScreen(
                Component.translatable("menu.savingLevel")));
        } else {
            this.minecraft.clearLevel();
        }

        // ScreenEventHandler replaces this with DiaryTitleScreen.
        this.minecraft.setScreen(new TitleScreen());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (hasActiveWorld()) {
            renderWorldSafe(g, mouseX, mouseY, partialTick);
            return;
        }
        int overlay = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 180);
        g.fill(0, 0, this.width, this.height, overlay);

        drawVignette(g);

        int px = panelX();
        int py = panelY();
        drawPanel(g, px, py, panelW(), panelH());
        drawPanelTitle(g, px, py);
        if (!compactPauseLayout()) drawDateStamp(g, px, py);
        drawPanelSeparators(g, px, py);

        // Keep decorative art clear of controls in compact windows.
        if (!compactPauseLayout()) drawRaven(g, px + 14, py + 22);

        super.render(g, mouseX, mouseY, partialTick);

        // Elementos que van encima de los botones
        if (!compactPauseLayout()) {
            drawCandle(g, px + 20, py + panelH() - 32);
            drawSignature(g, px, py);
            // v0.185.0: fuera el sello de cera del panel (y sus dos bucles de
            // 615 fills por cuadro); el papel ya no lleva adornos.
        }

        // ── v0.16.0: lo que trae la oscuridad ──
        if (!compactPauseLayout()) {
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
    }

    private boolean hasActiveWorld() {
        return this.minecraft != null && this.minecraft.level != null;
    }

    /**
     * In-world pause overlay: a fixed veil and a small static paper panel.
     * Skips the animated vignette, blood texture, candle, raven, breath,
     * shadow and lore polling so the live world keeps its frame budget.
     */
    private void renderWorldSafe(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0x70000000);

        int px = panelX();
        int py = panelY();
        int pw = panelW();
        int ph = panelH();
        g.fill(px + 4, py + 4, px + pw + 4, py + ph + 4, 0xA0000000);
        g.fill(px, py, px + pw, py + ph, DiaryTheme.SEPIA_DARK);
        g.fill(px + 2, py + 2, px + pw - 2, py + ph - 2, PAPER_LIGHT);
        g.fill(px + 5, py + 5, px + pw - 5, py + 6, DiaryTheme.BRONZE_DARK);
        g.fill(px + 5, py + ph - 6, px + pw - 5, py + ph - 5, DiaryTheme.BRONZE_DARK);

        drawPanelTitle(g, px, py);
        drawDateStamp(g, px, py);
        drawPanelSeparators(g, px, py);
        super.render(g, mouseX, mouseY, partialTick);
    }

    /**
     * v0.189.0: la vinneta se dibujaba con cuarenta pasos de un pixel por lado
     * (ciento sesenta rellenos por cuadro). Con diez bandas de cuatro pixeles se
     * ve igual y cuesta ocho veces menos.
     */
    private void drawVignette(GuiGraphics g) {
        final int steps = 10;
        final int band = 4;
        for (int i = 0; i < steps; i++) {
            float t = (steps - i) / (float) steps;
            int a = (int) (t * t * 70);
            if (a < 1) continue;
            int c = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, a);
            int p0 = i * band;
            int p1 = Math.min(p0 + band, Math.min(this.width, this.height));
            if (p1 <= p0) break;
            g.fill(p0, 0, p1, this.height, c);
            g.fill(this.width - p1, 0, this.width - p0, this.height, c);
            g.fill(0, p0, this.width, p1, c);
            g.fill(0, this.height - p1, this.width, this.height - p0, c);
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

        // 3. Brillo del panel.
        // v0.189.0: era un relleno por cada fila del panel (trescientas llamadas
        // por cuadro). El brillo solo varia en vertical, asi que son dos
        // gradientes: sube hasta el 40% de la altura y vuelve a bajar.
        int peakY = y + Math.max(6, (int) (h * 0.4f));
        int glossTop = y + 4;
        int glossBot = y + h - 4;
        int glossHi = DiaryTheme.withAlpha(PAPER_HIGHLIGHT, 140);
        int glossLo = DiaryTheme.withAlpha(PAPER_HIGHLIGHT, 0);
        if (glossBot - glossTop > 4) {
            int peak = Math.min(Math.max(peakY, glossTop + 1), glossBot - 1);
            if (peak - glossTop > 1) {
                g.fillGradient(x + 4, glossTop, x + w - 4, peak, glossLo, glossHi);
            }
            if (glossBot - peak > 1) {
                g.fillGradient(x + 4, peak, x + w - 4, glossBot, glossHi, glossLo);
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

    /**
     * v0.189.0: la mancha recorria su circulo pixel por pixel (ciento trece
     * rellenos por mancha, dos manchas por cuadro). Ahora cada fila del circulo
     * es un solo tramo: el degradado queda en vertical, que es lo que se ve a
     * este tamano, y son veintidos rellenos entre las dos manchas.
     */
    private void drawBloodSpotWithSplatter(GuiGraphics g, int cx, int cy, int r) {
        for (int dy = -r; dy <= r; dy++) {
            int rr = r * r - dy * dy;
            if (rr <= 0) continue;
            int half = (int) Math.sqrt(rr);
            if (half <= 0) continue;
            float t = 1f - Math.abs(dy) / (float) r;
            int alpha = (int) (t * 140);
            if (alpha < 1) continue;
            int c = DiaryTheme.withAlpha(DiaryTheme.BLOOD_DRIED, alpha);
            g.fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, c);
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
        String title = DiaryChrome.tr("gripeverde.pause.title");
        String subtitle = DiaryChrome.tr("gripeverde.pause.subtitle");

        int centerX = px + panelW() / 2;
        int compactTitleY = compactPauseLayout() ? 14 : 24;
        int subtitleY = py + (compactPauseLayout() ? 39 : 58);
        int titleY = py + compactTitleY;

        int tw = this.font.width(title);
        float titleScale = compactPauseLayout()
            ? Math.min(1.45f, Math.max(1.0f, (panelW() - 36.0f) / Math.max(1, tw)))
            : 1.75f;
        g.pose().pushPose();
        g.pose().translate(centerX, titleY, 0);
        g.pose().scale(titleScale, titleScale, 1f);
        g.drawString(this.font, title, -tw / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();

        int scaledTw = (int)(tw * titleScale);
        int ornY = titleY + 5;
        drawFlourish(g, centerX - scaledTw / 2 - 20, ornY, DiaryTheme.BRONZE, false);
        drawFlourish(g, centerX + scaledTw / 2 + 4, ornY, DiaryTheme.BRONZE, true);

        String shownSubtitle = ellipsizeTo(this.font, subtitle, Math.max(40, panelW() - 28));
        int sw = this.font.width(shownSubtitle);
        g.drawString(this.font, shownSubtitle,
            centerX - sw / 2, subtitleY,
            DiaryTheme.SEPIA_MID, false);
    }

    private static String ellipsizeTo(net.minecraft.client.gui.Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        String ellipsis = "...";
        int room = Math.max(0, maxWidth - font.width(ellipsis));
        return font.plainSubstrByWidth(text, room) + ellipsis;
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
        int sepW = Math.max(24, panelW() - 60);
        int lineC = DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 200);
        int accentC = DiaryTheme.BRONZE;

        drawSepLine(g, centerX, py + separatorTopOffset(), sepW, lineC, accentC);
        drawSepLine(g, centerX, py + panelH() - separatorBottomOffset(), sepW, lineC, accentC);
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
        return com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
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
