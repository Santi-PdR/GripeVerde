package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LogoRenderer {

    // Rotating chronicle note pool. Kept localized so language changes do not
    // leave the Title half Spanish, half English.
    private static final String[] YEAR_POOL_ES = {
        "~ Dia 47 ~ Aun no encuentro respuestas ~",
        "~ Dia 62 ~ Sobrevivir es todo lo que queda ~",
        "~ Dia 83 ~ La ciudad respira apenas ~",
        "~ Dia 91 ~ Ya no cuento a los muertos ~",
        "~ Dia 104 ~ El silencio pesa mas que el humo ~",
        "~ Dia 118 ~ Aun no cede la fiebre ~",
        "~ Dia 133 ~ Escribo para no olvidar ~",
        "~ Dia 147 ~ El aire sabe a hierro ~",
        "~ Dia 162 ~ Nadie llama a las puertas ~",
        "~ Dia 178 ~ Persisto ~",
        "~ Dia 195 ~ Otro invierno adentro ~",
        "~ Dia 210 ~ La pluma aun obedece ~",
        "~ Dia 226 ~ Los faroles vigilan ~",
        "~ Dia 241 ~ El hierro tambien enferma ~",
        "~ Dia 259 ~ La lluvia no limpia nada ~",
        "~ Dia 273 ~ Nadie recuerda el sol ~",
        "~ Dia 288 ~ La campana no duerme ~",
        "~ Dia 301 ~ Bajo sello y cuarentena ~"
    };
    private static final String[] YEAR_POOL_EN = {
        "~ Day 47 ~ I still find no answers ~",
        "~ Day 62 ~ Survival is all that remains ~",
        "~ Day 83 ~ The city barely breathes ~",
        "~ Day 91 ~ I no longer count the dead ~",
        "~ Day 104 ~ Silence weighs more than smoke ~",
        "~ Day 118 ~ The fever still will not yield ~",
        "~ Day 133 ~ I write so I do not forget ~",
        "~ Day 147 ~ The air tastes of iron ~",
        "~ Day 162 ~ No one knocks anymore ~",
        "~ Day 178 ~ I persist ~",
        "~ Day 195 ~ Another winter indoors ~",
        "~ Day 210 ~ The pen still obeys ~",
        "~ Day 226 ~ The lamps keep watch ~",
        "~ Day 241 ~ Even iron grows sick ~",
        "~ Day 259 ~ The rain cleanses nothing ~",
        "~ Day 273 ~ No one remembers the sun ~",
        "~ Day 288 ~ The bell does not sleep ~",
        "~ Day 301 ~ Under seal and quarantine ~"
    };

    private static int yearIndex = -1;
    private static long lastYearRotateMs = 0L;
    private static long YEAR_ROTATE_MS = 60_000L;  // Regenerated each rotation, 30-120s range
    private static void rerollRotateInterval() {
        YEAR_ROTATE_MS = 30_000L + (long)(YEAR_RNG.nextDouble() * 90_000L);
    }

    private static final java.util.Random YEAR_RNG = new java.util.Random();

    private static String getCurrentYear() {
        String lang = Minecraft.getInstance().getLanguageManager().getSelected();
        boolean english = lang == null || lang.startsWith("en_") || lang.equals("en_us");
        String[] pool = english ? YEAR_POOL_EN : YEAR_POOL_ES;
        long now = System.currentTimeMillis();
        if (yearIndex < 0 || yearIndex >= pool.length || (now - lastYearRotateMs) >= YEAR_ROTATE_MS) {
            int prev = yearIndex;
            int next;
            if (pool.length <= 1) {
                next = 0;
            } else {
                do {
                    next = YEAR_RNG.nextInt(pool.length);
                } while (next == prev);
            }
            yearIndex = next;
            lastYearRotateMs = now;
            rerollRotateInterval();
        }
        return pool[yearIndex];
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

        float logoPulse = (float)(Math.sin(System.currentTimeMillis() / 820.0) * 0.5 + 0.5);
        int skullY = topY + 6 + (logoPulse > 0.72f ? 1 : 0);
        // Soft shadow behind skull for depth
        int shadowCol = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 70);
        g.fill(cx - 9, skullY + 2, cx + 10, skullY + 30, shadowCol);
        drawTopHatSkull(g, cx, skullY);
        drawDecorativeLine(g, cx, topY + 46, 196);

        String title = I18n.get("gripeverde.brand.name");
        g.pose().pushPose();
        g.pose().translate(cx, topY + 60, 0);
        g.pose().scale(1.9f, 1.9f, 1f);
        int tw = font.width(title);
        g.drawString(font, title, -tw / 2 + 1, 1, DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 85), false);
        g.drawString(font, title, -tw / 2, 0, DiaryTheme.PAPER, false);
        g.pose().popPose();

        String subtitle = I18n.get("gripeverde.brand.subtitle");
        g.pose().pushPose();
        g.pose().translate(cx, topY + 98, 0);
        g.pose().scale(1.45f, 1.45f, 1f);
        int sw = font.width(subtitle);
        g.drawString(font, subtitle, -sw / 2 + 1, 1, DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 65), false);
        g.drawString(font, subtitle, -sw / 2, 0, DiaryTheme.PLAGUE_TOXIC, false);
        g.pose().popPose();

        String year = getCurrentYear();
        int yw = font.width(year);
        int yearY = topY + 122;
        int stripPad = 8;
        g.fill(cx - yw / 2 - stripPad, yearY - 1, cx + yw / 2 + stripPad, yearY + 9, DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, 28));
        g.drawString(font, year, cx - yw / 2, yearY, DiaryTheme.withAlpha(DiaryTheme.SEPIA_LIGHT, 200), false);
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
        int eyePulse = 90 + (int)(Math.sin(System.currentTimeMillis() / 420.0) * 45.0);
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