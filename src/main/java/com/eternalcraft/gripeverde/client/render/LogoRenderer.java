package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LogoRenderer {

    private static boolean reducedMotion() {
        return com.eternalcraft.gripeverde.client.util.ClientAccessibility.reducedMotion();
    }

    private static final int CHRONICLE_ENTRIES = 18;

    private static int yearIndex = -1;
    private static long lastYearRotateMs = 0L;
    private static long YEAR_ROTATE_MS = 60_000L;  // Regenerated each rotation, 30-120s range
    private static void rerollRotateInterval() {
        YEAR_ROTATE_MS = 30_000L + (long)(YEAR_RNG.nextDouble() * 90_000L);
    }

    private static final java.util.Random YEAR_RNG = new java.util.Random();

    private static void advanceChronicleRotation() {
        long now = System.currentTimeMillis();
        if (yearIndex < 0 || yearIndex >= CHRONICLE_ENTRIES || (now - lastYearRotateMs) >= YEAR_ROTATE_MS) {
            int previous = yearIndex;
            int next;
            do {
                next = YEAR_RNG.nextInt(CHRONICLE_ENTRIES);
            } while (next == previous);
            yearIndex = next;
            lastYearRotateMs = now;
            rerollRotateInterval();
        }
    }

    /** Public accessor so DiaryTitleScreen can sync signature rotation. */
    public static int getYearIndex() {
        return yearIndex < 0 ? 0 : yearIndex;
    }

    // v0.16.0: el ojo del emblema no esta pintado: sigue al lector.
    private static double watchedMx = -1, watchedMy = -1;
    public static void setWatchedMouse(double mx, double my) {
        watchedMx = mx; watchedMy = my;
    }

    public static void draw(GuiGraphics g, int cx, int topY) {
        Font font = Minecraft.getInstance().font;

        float logoPulse = reducedMotion() ? 0.5f
            : (float)(Math.sin(System.currentTimeMillis() / 820.0) * 0.5 + 0.5);
        int skullY = topY + 6 + (logoPulse > 0.72f ? 1 : 0);
        // Soft shadow behind skull for depth
        int shadowCol = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 70);
        g.fill(cx - 9, skullY + 2, cx + 10, skullY + 30, shadowCol);
        drawTopHatSkull(g, cx, skullY);
        drawDecorativeLine(g, cx, topY + 46, 196);

        String title = I18n.get("gripeverde.brand.name");
        Component displayTitle = DiaryTypography.display(Component.literal(title));
        // v0.21.25: titulo limpio sin sombra.
        g.pose().pushPose();
        g.pose().translate(cx, topY + 60, 0);
        g.pose().scale(1.9f, 1.9f, 1f);
        int tw = font.width(displayTitle);
        g.drawString(font, displayTitle, -tw / 2, 0, DiaryTheme.PAPER, false);
        g.pose().popPose();

        String subtitle = I18n.get("gripeverde.brand.subtitle");
        // v0.21.25: subtitulo limpio sin sombra.
        g.pose().pushPose();
        g.pose().translate(cx, topY + 98, 0);
        g.pose().scale(1.45f, 1.45f, 1f);
        int sw = font.width(subtitle);
        g.drawString(font, subtitle, -sw / 2, 0, DiaryTheme.PLAGUE_TOXIC, false);
        g.pose().popPose();

        // Keep the private chronicle rotation for the signature/easter eggs,
        // but leave the logo area clean: the rotating "Day XX" ticker was
        // noisy and competed with the main menu.
        advanceChronicleRotation();
    }

    private static void drawTopHatSkull(GuiGraphics g, int cx, int topY) {
        g.fill(cx - 8, topY, cx + 9, topY + 12, DiaryTheme.INK);
        g.fill(cx - 8, topY + 10, cx + 9, topY + 12, DiaryTheme.SEPIA_MID);
        g.fill(cx - 12, topY + 12, cx + 13, topY + 15, DiaryTheme.INK);
        g.fill(cx - 7, topY + 1, cx - 5, topY + 8, DiaryTheme.SEPIA_DARK);
        g.fill(cx - 7, topY + 15, cx + 8, topY + 26, DiaryTheme.PAPER);
        g.fill(cx - 6, topY + 14, cx + 7, topY + 15, DiaryTheme.PAPER);
        g.fill(cx - 6, topY + 26, cx + 7, topY + 28, DiaryTheme.PAPER);
        g.fill(cx - 6, topY + 25, cx + 7, topY + 27, DiaryTheme.PAPER_STAINED);
        g.fill(cx - 5, topY + 18, cx - 2, topY + 22, DiaryTheme.INK);
        g.fill(cx + 3, topY + 18, cx + 6, topY + 22, DiaryTheme.INK);
        g.fill(cx + 4, topY + 19, cx + 5, topY + 20, DiaryTheme.PLAGUE_TOXIC);
        int eyePulse = reducedMotion() ? 90
            : 90 + (int)(Math.sin(System.currentTimeMillis() / 420.0) * 45.0);
        g.fill(cx + 3, topY + 18, cx + 7, topY + 22, (Math.max(20, eyePulse) << 24) | 0x5A7038);
        // v0.16.0: la pupila mira hacia el lector (maximo 1px de sesgo).
        int px = 0, py = 0;
        if (watchedMx >= 0) {
            double dx = watchedMx - (cx + 4.5);
            double dy = watchedMy - (topY + 19.5);
            if (Math.abs(dx) > 30) px = dx > 0 ? 1 : -1;
            if (Math.abs(dy) > 30) py = dy > 0 ? 1 : -1;
        }
        g.fill(cx + 4 + px, topY + 19 + py, cx + 5 + px, topY + 20 + py, DiaryTheme.PLAGUE_TOXIC);
        g.fill(cx, topY + 22, cx + 1, topY + 24, DiaryTheme.INK);
        g.fill(cx - 1, topY + 24, cx + 2, topY + 25, DiaryTheme.INK);
        g.fill(cx - 4, topY + 27, cx + 5, topY + 30, DiaryTheme.PAPER);
        g.fill(cx - 3, topY + 27, cx - 2, topY + 30, DiaryTheme.INK);
        g.fill(cx - 1, topY + 27, cx, topY + 30, DiaryTheme.INK);
        g.fill(cx + 1, topY + 27, cx + 2, topY + 30, DiaryTheme.INK);
        g.fill(cx + 3, topY + 27, cx + 4, topY + 30, DiaryTheme.INK);
    }

    private static void drawDecorativeLine(GuiGraphics g, int cx, int y, int totalWidth) {
        int half = totalWidth / 2;
        int gapCenter = 20;
        g.fill(cx - half, y, cx - gapCenter, y + 1, DiaryTheme.SEPIA_LIGHT);
        g.fill(cx - half, y + 2, cx - gapCenter, y + 3, DiaryTheme.SEPIA_MID);
        g.fill(cx + gapCenter, y, cx + half, y + 1, DiaryTheme.SEPIA_LIGHT);
        g.fill(cx + gapCenter, y + 2, cx + half, y + 3, DiaryTheme.SEPIA_MID);
        g.fill(cx - 2, y, cx + 3, y + 3, DiaryTheme.GOLD_TARNISHED);
        g.fill(cx - 3, y + 1, cx + 4, y + 2, DiaryTheme.GOLD_TARNISHED);
        g.fill(cx - 1, y - 1, cx + 2, y, DiaryTheme.GOLD_TARNISHED);
        g.fill(cx - 1, y + 3, cx + 2, y + 4, DiaryTheme.GOLD_TARNISHED);
    }
}
