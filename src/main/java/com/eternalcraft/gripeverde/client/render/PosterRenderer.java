package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PosterRenderer {

    public static void drawQuarantineNotice(GuiGraphics g, int x, int y, int w, int h) {
        Font font = Minecraft.getInstance().font;
        OrnamentRenderer.drawPaperPanel(g, x, y, w, h);
        drawSimpleBorder(g, x, y, w, h, DiaryTheme.INK, DiaryTheme.SEPIA_MID);

        int cx = x + w / 2;
        int cursorY = y + 10;

        DiaryChrome.drawCenteredString(g, font, getRotatingQuarantineHeader(0), cx, cursorY, DiaryTheme.INK);
        cursorY += 12;
        DiaryChrome.drawCenteredString(g, font, getRotatingQuarantineHeader(1), cx, cursorY, DiaryTheme.INK);
        cursorY += 16;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 8;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY, 0);
        g.pose().scale(1.2f, 1.2f, 1f);
        String q = I18n.get("gripeverde.poster.quarantine.title");
        int qw = font.width(q);
        // v0.21.24: tinta oscura sin sombra (se retiro el rojo sombreado).
        g.drawString(font, q, -qw / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();
        cursorY += 18;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 10;

        String[] body = {
            I18n.get("gripeverde.poster.quarantine.body.1"),
            I18n.get("gripeverde.poster.quarantine.body.2"),
            I18n.get("gripeverde.poster.quarantine.body.3"),
            I18n.get("gripeverde.poster.quarantine.body.4"),
            I18n.get("gripeverde.poster.quarantine.body.5"),
            I18n.get("gripeverde.poster.quarantine.body.6"),
            "",
            I18n.get("gripeverde.poster.quarantine.warn.1"),
            I18n.get("gripeverde.poster.quarantine.warn.2"),
            I18n.get("gripeverde.poster.quarantine.warn.3")
        };
        for (String line : body) {
            DiaryChrome.drawCenteredString(g, font, line, cx, cursorY, DiaryTheme.INK_FADED);
            cursorY += 10;
        }

        OrnamentRenderer.drawWaxSeal(g, x + w - 22, y + h - 22, 12);
        drawBloodSplatter(g, x, y, w, h, 0x51A11L);
        drawInkStamp(g, x, y, w, h, 0x57A9AL);
    }

    public static void drawPreventionNotice(GuiGraphics g, int x, int y, int w, int h) {
        Font font = Minecraft.getInstance().font;
        OrnamentRenderer.drawPaperPanel(g, x, y, w, h);
        drawSimpleBorder(g, x, y, w, h, DiaryTheme.INK, DiaryTheme.SEPIA_MID);

        int cx = x + w / 2;
        int cursorY = y + 8;

        DiaryChrome.drawCenteredString(g, font, I18n.get("gripeverde.poster.elixir.author"), cx, cursorY, DiaryTheme.INK);
        cursorY += 12;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 6;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY, 0);
        g.pose().scale(1.2f, 1.2f, 1f);
        String am = I18n.get("gripeverde.poster.elixir.title.1");
        int aw = font.width(am);
        // v0.21.25: tinta oscura sin sombra (consistencia con poster de cuarentena).
        g.drawString(font, am, -aw / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();
        cursorY += 14;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY, 0);
        g.pose().scale(1.4f, 1.4f, 1f);
        String el = I18n.get("gripeverde.poster.elixir.title.2");
        int ew = font.width(el);
        // v0.21.25: tinta oscura sin sombra (consistencia con poster de cuarentena).
        g.drawString(font, el, -ew / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();
        cursorY += 18;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 6;

        String[] body = {
            I18n.get("gripeverde.poster.elixir.body.1"),
            I18n.get("gripeverde.poster.elixir.body.2"),
            "",
            I18n.get("gripeverde.poster.elixir.price"),
            I18n.get("gripeverde.poster.elixir.unit")
        };
        for (String line : body) {
            DiaryChrome.drawCenteredString(g, font, line, cx, cursorY, DiaryTheme.INK_FADED);
            cursorY += 10;
        }

        // v0.16.0: consecuencia permanente. Quien leyo el informe del medico
        // ve desde entonces la verdad estampada sobre el elixir.
        try {
            if (com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("fraude_stamp") > 0) {
                String stamp = "FRAUDE";
                int tw2 = font.width(stamp);
                int sx = cx - tw2 / 2;
                int sy = y + h - 46;
                g.fill(sx - 4, sy - 3, sx + tw2 + 4, sy - 2, 0xFF5C1508);
                g.fill(sx - 4, sy + 10, sx + tw2 + 4, sy + 11, 0xFF5C1508);
                g.fill(sx - 4, sy - 3, sx - 3, sy + 11, 0xFF5C1508);
                g.fill(sx + tw2 + 3, sy - 3, sx + tw2 + 4, sy + 11, 0xFF5C1508);
                g.drawString(font, stamp, sx, sy, 0xFF5C1508, false);
            }
        } catch (Throwable ignored) {}
    }


    /** Panel derecho inferior del menú: rota entre 3 avisos con seed diaria. */
    public static void drawRotatingNoticeRight(GuiGraphics g, int x, int y, int w, int h) {
        long day = System.currentTimeMillis() / 86_400_000L;
        int variant = (int)(Math.abs(day) % 3);
        switch (variant) {
            case 1 -> drawRewardNotice(g, x, y, w, h);
            case 2 -> drawDecreeNotice(g, x, y, w, h);
            default -> drawPreventionNotice(g, x, y, w, h); // Elixir (existente)
        }
    }

    private static void drawRewardNotice(GuiGraphics g, int x, int y, int w, int h) {
        Font font = Minecraft.getInstance().font;
        OrnamentRenderer.drawPaperPanel(g, x, y, w, h);
        drawSimpleBorder(g, x, y, w, h, DiaryTheme.INK, DiaryTheme.SEPIA_MID);

        int cx = x + w / 2;
        int cursorY = y + 10;

        // Encabezado pequeño
        String header = isEnglish() ? "WANTED" : "SE BUSCA";
        int hw = font.width(header);
        g.drawString(font, header, cx - hw / 2, cursorY, DiaryTheme.INK, false);
        cursorY += 11;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 6;

        // Título "PATIENT ZERO" en rojo (escala 1.2 en vez de 1.3 para que quepa)
        g.pose().pushPose();
        g.pose().translate(cx, cursorY + 3, 0);
        g.pose().scale(1.2f, 1.2f, 1f);
        String title = I18n.get("gripeverde.poster.reward.title.short");
        int tw = font.width(title);
        // v0.21.25: tinta oscura sin sombra (consistencia con poster de cuarentena).
        g.drawString(font, title, -tw / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();
        cursorY += 18;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 6;

        // v0.16.0: el retrato del buscado. Quien le sostiene la mirada
        // descubre que el dibujo tambien mira. Solo si el cartel es alto.
        if (h >= 150) {
            drawWantedPortrait(g, cx, cursorY, w);
            cursorY += 30;
        }

        DiaryChrome.drawCenteredString(g, font, I18n.get("gripeverde.poster.reward.body.1"), cx, cursorY, DiaryTheme.INK_FADED);
        cursorY += 10;
        DiaryChrome.drawCenteredString(g, font, I18n.get("gripeverde.poster.reward.body.2"), cx, cursorY, DiaryTheme.INK_FADED);
        cursorY += 14;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 8;

        // Prize compactado en una sola línea
        DiaryChrome.drawCenteredString(g, font, I18n.get("gripeverde.poster.reward.prize.short"), cx, cursorY, DiaryTheme.BLOOD_DRIED);
        cursorY += 10;
        DiaryChrome.drawCenteredString(g, font, I18n.get("gripeverde.poster.reward.prize.unit"), cx, cursorY, DiaryTheme.BRONZE);

        // Sello más pequeño y en posición segura dentro del panel
        OrnamentRenderer.drawWaxSeal(g, x + w - 18, y + h - 18, 7);
        drawBloodSplatter(g, x, y, w, h, 0x5EAADL);
        drawInkStamp(g, x, y, w, h, 0x5EA100L);
    }

    private static void drawDecreeNotice(GuiGraphics g, int x, int y, int w, int h) {
        Font font = Minecraft.getInstance().font;
        OrnamentRenderer.drawPaperPanel(g, x, y, w, h);
        drawSimpleBorder(g, x, y, w, h, DiaryTheme.INK, DiaryTheme.SEPIA_MID);

        int cx = x + w / 2;
        int cursorY = y + 10;

        DiaryChrome.drawCenteredString(g, font, I18n.get("gripeverde.poster.decree.header"), cx, cursorY, DiaryTheme.INK);
        cursorY += 12;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 8;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY + 4, 0);
        g.pose().scale(1.1f, 1.1f, 1f);
        String title = I18n.get("gripeverde.poster.decree.title");
        int tw = font.width(title);
        // v0.21.25: tinta oscura sin sombra (consistencia con poster de cuarentena).
        g.drawString(font, title, -tw / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();
        cursorY += 18;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 10;

        DiaryChrome.drawCenteredString(g, font, I18n.get("gripeverde.poster.decree.body.1"), cx, cursorY, DiaryTheme.INK_FADED);
        cursorY += 10;
        DiaryChrome.drawCenteredString(g, font, I18n.get("gripeverde.poster.decree.body.2"), cx, cursorY, DiaryTheme.INK_FADED);
        cursorY += 18;

        drawThinDivider(g, cx, cursorY, w - 30);
        cursorY += 8;

        g.pose().pushPose();
        g.pose().translate(cx, cursorY + 2, 0);
        g.pose().scale(1.1f, 1.1f, 1f);
        String warn = I18n.get("gripeverde.poster.decree.warn");
        int ww = font.width(warn);
        // v0.21.25: tinta oscura sin sombra (consistencia con poster de cuarentena).
        g.drawString(font, warn, -ww / 2, 0, DiaryTheme.INK, false);
        g.pose().popPose();
    }

    /** Retrato somero del buscado; sus ojos se mueven si el lector lo sabe. */
    private static void drawWantedPortrait(GuiGraphics g, int cx, int topY, int w) {
        int pw = 26, ph = 30;
        int x = cx - pw / 2;
        int y = topY;
        // Marco del retrato
        g.fill(x - 2, y - 2, x + pw + 2, y + ph + 2, DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, 190));
        g.fill(x, y, x + pw, y + ph, 0xFFD8CAB0);
        // Silueta encapuchada
        g.fill(x + 7, y + 12, x + 19, y + 28, 0xFF2A1E10);
        g.fill(x + 9, y + 6, x + 17, y + 14, 0xFF2A1E10);
        g.fill(x + 10, y + 4, x + 16, y + 7, 0xFF2A1E10);
        g.fill(x + 5, y + 16, x + 8, y + 26, 0xFF241810);
        g.fill(x + 18, y + 16, x + 21, y + 26, 0xFF241810);
        // Los ojos: se desplazan solos cada tantos segundos si ya lo sabe.
        boolean knows = false;
        try {
            knows = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.counter("wanted_eyes") > 0;
        } catch (Throwable ignored) {}
        int shift = 0;
        if (knows) {
            long seg = System.currentTimeMillis() / 2600L;
            shift = (int)(seg % 3L) - 1; // -1, 0, 1
        }
        int eye = knows ? 0xFFD4941E : 0xFF141008;
        g.fill(x + 11 + shift, y + 10, x + 12 + shift, y + 11, eye);
        g.fill(x + 14 + shift, y + 10, x + 15 + shift, y + 11, eye);
    }

    private static void drawThinDivider(GuiGraphics g, int cx, int y, int width) {
        int half = width / 2;
        int gap = 4;
        g.fill(cx - half, y, cx - gap, y + 1, DiaryTheme.SEPIA_MID);
        g.fill(cx + gap, y, cx + half, y + 1, DiaryTheme.SEPIA_MID);
        g.fill(cx - 1, y - 1, cx + 2, y + 2, DiaryTheme.OCHRE);
    }

    private static void drawSimpleBorder(GuiGraphics g, int x, int y, int w, int h, int outerColor, int innerColor) {
        g.fill(x, y, x + w, y + 1, outerColor);
        g.fill(x, y + h - 1, x + w, y + h, outerColor);
        g.fill(x, y, x + 1, y + h, outerColor);
        g.fill(x + w - 1, y, x + w, y + h, outerColor);
        g.fill(x + 2, y + 2, x + w - 2, y + 3, innerColor);
        g.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, innerColor);
        g.fill(x + 2, y + 2, x + 3, y + h - 2, innerColor);
        g.fill(x + w - 3, y + 2, x + w - 2, y + h - 2, innerColor);
    }

    // Rotating quarantine notice headers: 2 lines per variant.
    // Localized here because they are flavour variants, not fixed lang keys.
    private static final String[][] QUARANTINE_HEADER_POOL_ES = {
        {"POR ORDEN DE",    "SU MAJESTAD"},
        {"POR MANDATO DE",  "EL PARLAMENTO"},
        {"AVISO OFICIAL",   "DEL CONDESTABLE"},
        {"BANDO PUBLICO",   "DEL MAGISTRADO"},
        {"DECRETO DE",      "LA CORONA"},
        {"POR DISPOSICION", "DEL ALGUACIL MAYOR"},
        {"NOTA DE",         "LA JUNTA SANITARIA"},
        {"POR ORDEN DE",    "LA GUARDIA REAL"}
    };
    private static final String[][] QUARANTINE_HEADER_POOL_EN = {
        {"BY ORDER OF",     "HER MAJESTY"},
        {"BY MANDATE OF",   "PARLIAMENT"},
        {"OFFICIAL NOTICE", "OF THE CONSTABLE"},
        {"PUBLIC NOTICE",   "OF THE MAGISTRATE"},
        {"DECREE OF",       "THE CROWN"},
        {"BY DIRECTION",    "OF THE HIGH BAILIFF"},
        {"NOTICE FROM",     "THE SANITARY BOARD"},
        {"BY ORDER OF",     "THE ROYAL GUARD"}
    };
    private static int quarantineHeaderIndex = -1;
    private static long lastQuarantineRotateMs = 0L;
    private static final long QUARANTINE_ROTATE_MS = 120_000L;
    private static final java.util.Random QUARANTINE_RNG = new java.util.Random();

    private static String getRotatingQuarantineHeader(int lineIdx) {
        long now = System.currentTimeMillis();
        String[][] pool = isEnglish() ? QUARANTINE_HEADER_POOL_EN : QUARANTINE_HEADER_POOL_ES;
        if (quarantineHeaderIndex < 0 || quarantineHeaderIndex >= pool.length
                || (now - lastQuarantineRotateMs) >= QUARANTINE_ROTATE_MS) {
            int prev = quarantineHeaderIndex;
            int next;
            if (pool.length <= 1) {
                next = 0;
            } else {
                do {
                    next = QUARANTINE_RNG.nextInt(pool.length);
                } while (next == prev);
            }
            quarantineHeaderIndex = next;
            lastQuarantineRotateMs = now;
        }
        return pool[quarantineHeaderIndex][lineIdx];
    }

    private static boolean isEnglish() {
        try {
            String lang = Minecraft.getInstance().getLanguageManager().getSelected();
            return lang == null || lang.startsWith("en_") || lang.equals("en_us");
        } catch (Throwable ignored) {
            return false;
        }
    }

    // ============================================================
    // Manchas de sangre en posters (v0.12.2)
    // ============================================================

    /** 30% chance de dibujar 2-4 pequenas manchas de sangre en el poster.
     *  Determinista por seed pasado (el mismo poster siempre tendra o no manchas). */
    public static void drawBloodSplatter(GuiGraphics g, int x, int y, int w, int h, long posterSeed) {
        java.util.Random rnd = new java.util.Random(posterSeed);
        if (rnd.nextInt(10) >= 3) return;  // 70% no dibuja

        int count = 2 + rnd.nextInt(3);
        int blood = 0xFF5C1508;   // dried blood
        int fresh = 0xFF8B1F0F;   // fresh blood

        for (int i = 0; i < count; i++) {
            int bx = x + 10 + rnd.nextInt(Math.max(1, w - 20));
            int by = y + 10 + rnd.nextInt(Math.max(1, h - 20));
            int sz = 1 + rnd.nextInt(3);

            // Blob principal
            for (int dy = 0; dy < sz; dy++) {
                for (int dx = 0; dx < sz; dx++) {
                    if (rnd.nextInt(5) < 4) {  // 80% denso
                        g.fill(bx + dx, by + dy, bx + dx + 1, by + dy + 1,
                               rnd.nextInt(3) == 0 ? fresh : blood);
                    }
                }
            }

            // Salpicaduras alrededor
            int splatter = 2 + rnd.nextInt(3);
            for (int s = 0; s < splatter; s++) {
                int sx = bx + rnd.nextInt(6) - 3;
                int sy = by + rnd.nextInt(6) - 3;
                g.fill(sx, sy, sx + 1, sy + 1, blood);
            }
        }
    }

    // ============================================================
    // Sellos de tinta rotativos (APROBADO/REVOCADO/CENSURADO)
    // ============================================================
    private static final String[] STAMP_LABELS_ES = {
        "APROBADO", "REVOCADO", "CENSURADO", "PENDIENTE", "URGENTE"
    };
    private static final String[] STAMP_LABELS_EN = {
        "APPROVED", "REVOKED", "CENSORED", "PENDING", "URGENT"
    };
    private static final int[] STAMP_COLORS = {
        0xFF5C1508,   // dried blood (revocado, censurado)
        0xFF3D2A18,   // sepia oscuro (aprobado)
        0xFF6B4226    // sepia medio (pendiente)
    };

    /** Dibuja un sello de tinta rotativo si el seed lo permite (40% chance). */
    public static void drawInkStamp(GuiGraphics g, int x, int y, int w, int h, long posterSeed) {
        java.util.Random rnd = new java.util.Random(posterSeed);
        if (rnd.nextInt(10) >= 4) return;  // 60% no dibuja

        String[] labels = isEnglish() ? STAMP_LABELS_EN : STAMP_LABELS_ES;
        String label = labels[rnd.nextInt(labels.length)];
        int col = STAMP_COLORS[rnd.nextInt(STAMP_COLORS.length)];

        Font f = Minecraft.getInstance().font;
        int tw = f.width(label);
        // Posicion: esquina rotativa (0=arriba-izq, 1=arriba-der, 2=abajo-izq, 3=abajo-der)
        int corner = rnd.nextInt(4);
        int sx, sy;
        int pad = 12;
        switch (corner) {
            case 0: sx = x + pad; sy = y + pad + 4; break;
            case 1: sx = x + w - tw - pad; sy = y + pad + 4; break;
            case 2: sx = x + pad; sy = y + h - pad - 6; break;
            default: sx = x + w - tw - pad; sy = y + h - pad - 6; break;
        }

        // Rotacion aproximada: shift horizontal por linea
        g.pose().pushPose();
        g.pose().translate(sx, sy, 0);
        g.pose().translate(tw / 2.0, 3, 0);
        // No podemos rotar sin transformer, simulamos con offset diagonal
        g.pose().translate(-tw / 2.0, -3, 0);

        // Borde rectangular alrededor
        g.fill(-3, -2, tw + 3, -1, col);
        g.fill(-3, 10, tw + 3, 11, col);
        g.fill(-3, -2, -2, 11, col);
        g.fill(tw + 2, -2, tw + 3, 11, col);

        // v0.21.25: texto en color sello sin sombra (consistencia con poster de cuarentena).
        g.drawString(f, label, 0, 0, col, false);

        g.pose().popPose();
    }
}
