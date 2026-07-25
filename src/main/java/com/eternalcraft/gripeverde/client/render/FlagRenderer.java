package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import java.util.HashMap;
import java.util.Map;

/**
 * Banderas 16x11 px hardcoded. Idiomas sin bandera muestran rombo tinta.
 */
public final class FlagRenderer {

    public static final int FLAG_W = 16;
    public static final int FLAG_H = 11;

    private FlagRenderer() {}

    // ── paleta ───────────────────────────────────────────────────────────────
    private static final int RED       = 0xFFCC2020;
    private static final int RED_D     = 0xFF8B1A1A;
    private static final int WHITE     = 0xFFEEEEEE;
    private static final int BLUE      = 0xFF1E4FA0;
    private static final int BLUE_D    = 0xFF002878;
    private static final int BLUE_SKY  = 0xFF6FADD8;
    private static final int YELLOW    = 0xFFF0C808;
    private static final int GOLD      = 0xFFDDAA00;
    private static final int GREEN     = 0xFF1E7B3E;
    private static final int GREEN_L   = 0xFF44AA44;
    private static final int BLACK     = 0xFF181818;
    private static final int ORANGE    = 0xFFDD7722;
    private static final int BROWN     = 0xFF663311;

    // ── registro ─────────────────────────────────────────────────────────────
    private static final Map<String, int[][]> FLAGS = new HashMap<>();

    static {
        // ==== INGLES (todas las variantes vanilla) ====
        FLAGS.put("en_us",   flagUS());
        FLAGS.put("en_gb",   unionJack());
        FLAGS.put("en_ca",   flagCanada());
        FLAGS.put("en_au",   flagAustralia());
        FLAGS.put("en_nz",   flagNewZealand());
        FLAGS.put("en_pt",   flagPirate());   // English Pirate
        FLAGS.put("en_ud",   flagUS());       // Upside-down = US
        FLAGS.put("enp",     flagPirate());   // alias posible

        // ==== ESPANOL (todas las variantes vanilla) ====
        FLAGS.put("es_es",   flagSpain());
        FLAGS.put("es_mx",   flagMexico());
        FLAGS.put("es_ar",   flagArgentina());
        FLAGS.put("es_cl",   flagChile());
        FLAGS.put("es_uy",   flagUruguay());
        FLAGS.put("es_ve",   flagVenezuela());
        FLAGS.put("es_ec",   flagEcuador());
        FLAGS.put("es_419",  flagLatinAmerica()); // Espanol latinoamericano
        FLAGS.put("es_an",   flagAndalusia());    // Andaluz
        FLAGS.put("es_co",   flagColombia());
        FLAGS.put("es_pe",   flagPeru());
        FLAGS.put("es_bo",   flagBolivia());
        FLAGS.put("es_py",   flagParaguay());
        FLAGS.put("es_cr",   flagCostaRica());
        FLAGS.put("es_do",   flagDominicanRep());
        FLAGS.put("es_gt",   flagGuatemala());
        FLAGS.put("es_hn",   flagHonduras());
        FLAGS.put("es_ni",   flagNicaragua());
        FLAGS.put("es_pa",   flagPanama());
        FLAGS.put("es_sv",   flagElSalvador());
        FLAGS.put("es_pr",   flagPuertoRico());
        FLAGS.put("es_cu",   flagCuba());

        // ==== PORTUGUES ====
        FLAGS.put("pt_br",   flagBrazil());
        FLAGS.put("pt_pt",   flagPortugal());

        // ==== FRANCES ====
        FLAGS.put("fr_fr",   vBands(BLUE, WHITE, RED));
        FLAGS.put("fr_ca",   flagQuebec());

        // ==== ALEMAN ====
        FLAGS.put("de_de",   hBands(BLACK, RED, GOLD));
        FLAGS.put("de_at",   hBands(RED, WHITE, RED));
        FLAGS.put("de_ch",   flagSwiss());

        // ==== ITALIANO / NEERLANDES ====
        FLAGS.put("it_it",   vBands(GREEN, WHITE, RED));
        FLAGS.put("nl_nl",   hBands(RED, WHITE, BLUE));
        FLAGS.put("nl_be",   vBands(BLACK, YELLOW, RED));
        FLAGS.put("ga_ie",   vBands(GREEN, WHITE, ORANGE));

        // ==== NORDICOS ====
        FLAGS.put("sv_se",   nordicCross(BLUE, YELLOW));
        FLAGS.put("da_dk",   nordicCross(RED, WHITE));
        FLAGS.put("no_no",   flagNorway());
        FLAGS.put("nb_no",   flagNorway());
        FLAGS.put("nn_no",   flagNorway());
        FLAGS.put("fi_fi",   nordicCross(WHITE, BLUE));
        FLAGS.put("is_is",   flagIceland());
        FLAGS.put("fo_fo",   nordicCross(WHITE, RED));

        // ==== ESTE EUROPEO ====
        FLAGS.put("ru_ru",   hBands(WHITE, BLUE, RED));
        FLAGS.put("uk_ua",   h2(BLUE, YELLOW));
        FLAGS.put("be_by",   hBands(RED, GREEN, RED));
        FLAGS.put("pl_pl",   h2(WHITE, RED));
        FLAGS.put("cs_cz",   flagCzechia());
        FLAGS.put("sk_sk",   hBands(WHITE, BLUE, RED));
        FLAGS.put("hu_hu",   hBands(RED, WHITE, GREEN));
        FLAGS.put("ro_ro",   vBands(BLUE, YELLOW, RED));
        FLAGS.put("mo_md",   vBands(BLUE, YELLOW, RED));
        FLAGS.put("bg_bg",   hBands(WHITE, GREEN, RED));
        FLAGS.put("hr_hr",   hBands(RED, WHITE, BLUE));
        FLAGS.put("sr_sp",   hBands(RED, BLUE, WHITE));
        FLAGS.put("sl_si",   hBands(WHITE, BLUE, RED));
        FLAGS.put("bs_ba",   flagBosnia());
        FLAGS.put("sq_al",   flagAlbania());

        // ==== BALTICO ====
        FLAGS.put("lv_lv",   flagLatvia());
        FLAGS.put("lt_lt",   hBands(YELLOW, GREEN, RED));
        FLAGS.put("et_ee",   hBands(BLUE_SKY, BLACK, WHITE));

        // ==== GRIEGO / TURCO ====
        FLAGS.put("el_gr",   flagGreece());
        FLAGS.put("tr_tr",   flagTurkey());

        // ==== ASIA ORIENTAL ====
        FLAGS.put("ja_jp",   flagJapan());
        FLAGS.put("ko_kr",   flagKorea());
        FLAGS.put("zh_cn",   flagChina());
        FLAGS.put("zh_tw",   flagTaiwan());
        FLAGS.put("zh_hk",   flagChina());

        // ==== ASIA SUDESTE ====
        FLAGS.put("vi_vn",   flagVietnam());
        FLAGS.put("th_th",   flagThailand());
        FLAGS.put("id_id",   h2(RED, WHITE));
        FLAGS.put("fil_ph",  flagPhilippines());
        FLAGS.put("ms_my",   flagMalaysia());

        // ==== OTRAS ====
        FLAGS.put("he_il",   flagIsrael());
        FLAGS.put("ar_sa",   hBands(GREEN, GREEN, GREEN));
        FLAGS.put("fa_ir",   hBands(GREEN, WHITE, RED));
        FLAGS.put("af_za",   flagSouthAfrica());
        FLAGS.put("ka_ge",   flagGeorgia());
        FLAGS.put("hy_am",   hBands(RED, BLUE, ORANGE));
        FLAGS.put("az_az",   hBands(BLUE_SKY, RED, GREEN));
        FLAGS.put("kk_kz",   flagKazakhstan());
        FLAGS.put("mn_mn",   flagMongolia());

        // ==== REGIONALES ESPANA / UK ====
        FLAGS.put("ca_es",   flagCatalonia());
        FLAGS.put("eu_es",   flagBasque());
        FLAGS.put("gl_es",   flagGalicia());
        FLAGS.put("cy_gb",   flagWales());
        FLAGS.put("gd_gb",   flagScotland());
    }

    // ── API ──────────────────────────────────────────────────────────────────
    public static void draw(GuiGraphics gfx, String code, int x, int y) {
        int[][] flag = code != null ? FLAGS.get(code.toLowerCase()) : null;
        if (flag != null) drawGrid(gfx, flag, x, y);
        else drawFallback(gfx, x, y);
    }

    public static boolean has(String code) {
        return code != null && FLAGS.containsKey(code.toLowerCase());
    }

    // ── render interno ───────────────────────────────────────────────────────
    private static void drawGrid(GuiGraphics gfx, int[][] grid, int ox, int oy) {
        for (int r = 0; r < FLAG_H; r++) {
            for (int c = 0; c < FLAG_W; c++) {
                int color = grid[r][c];
                if ((color >>> 24) == 0) continue;
                gfx.fill(ox + c, oy + r, ox + c + 1, oy + r + 1, color);
            }
        }
        int border = 0x60000000;
        gfx.fill(ox,           oy,            ox + FLAG_W,     oy + 1,          border);
        gfx.fill(ox,           oy + FLAG_H-1, ox + FLAG_W,     oy + FLAG_H,     border);
        gfx.fill(ox,           oy,            ox + 1,          oy + FLAG_H,     border);
        gfx.fill(ox + FLAG_W-1, oy,           ox + FLAG_W,     oy + FLAG_H,     border);
    }

    private static void drawFallback(GuiGraphics gfx, int x, int y) {
        int cx = x + FLAG_W / 2;
        int cy = y + FLAG_H / 2;
        int c = 0xFF6B4A2E;
        gfx.fill(cx,   cy-2, cx+1, cy-1, c);
        gfx.fill(cx-1, cy-1, cx+2, cy,   c);
        gfx.fill(cx-2, cy,   cx+3, cy+1, c);
        gfx.fill(cx-1, cy+1, cx+2, cy+2, c);
        gfx.fill(cx,   cy+2, cx+1, cy+3, c);
    }

    // ── helpers ──────────────────────────────────────────────────────────────
    private static int[][] blank() { return new int[FLAG_H][FLAG_W]; }

    private static void row(int[][] g, int r, int x0, int x1, int c) {
        for (int x = x0; x < x1 && x < FLAG_W; x++) g[r][x] = c;
    }
    private static void col(int[][] g, int c, int y0, int y1, int color) {
        for (int y = y0; y < y1 && y < FLAG_H; y++) g[y][c] = color;
    }
    private static void fill(int[][] g, int x0, int y0, int x1, int y1, int color) {
        for (int y = y0; y < y1; y++)
            for (int x = x0; x < x1; x++)
                if (y < FLAG_H && x < FLAG_W) g[y][x] = color;
    }

    private static int[][] hBands(int top, int mid, int bot) {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) {
            int c = r < 4 ? top : r < 7 ? mid : bot;
            row(g, r, 0, FLAG_W, c);
        }
        return g;
    }
    private static int[][] h2(int top, int bot) {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, r < 5 ? top : bot);
        return g;
    }
    private static int[][] vBands(int left, int mid, int right) {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) {
            row(g, r, 0,  5,      left);
            row(g, r, 5,  11,     mid);
            row(g, r, 11, FLAG_W, right);
        }
        return g;
    }

    // ── BANDERAS ─────────────────────────────────────────────────────────────

    private static int[][] flagUS() {
        int[][] g = blank();
        int[] cols = {RED,WHITE,RED,WHITE,RED,WHITE,RED,WHITE,RED,WHITE,RED};
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, cols[r]);
        fill(g, 0, 0, 7, 6, BLUE);
        g[1][1]=WHITE; g[1][3]=WHITE; g[1][5]=WHITE;
        g[3][2]=WHITE; g[3][4]=WHITE;
        g[4][1]=WHITE; g[4][3]=WHITE; g[4][5]=WHITE;
        return g;
    }

    private static int[][] unionJack() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, BLUE);
        for (int i = 0; i < FLAG_H; i++) {
            int x = i * (FLAG_W - 1) / (FLAG_H - 1);
            g[i][x] = WHITE;
            g[i][FLAG_W-1-x] = WHITE;
        }
        row(g, 4, 0, FLAG_W, WHITE); row(g, 5, 0, FLAG_W, WHITE); row(g, 6, 0, FLAG_W, WHITE);
        col(g, 7, 0, FLAG_H, WHITE); col(g, 8, 0, FLAG_H, WHITE);
        row(g, 5, 0, FLAG_W, RED);
        col(g, 7, 0, FLAG_H, RED); col(g, 8, 0, FLAG_H, RED);
        return g;
    }

    private static int[][] flagCanada() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) {
            row(g, r, 0, 4,  RED);
            row(g, r, 4, 12, WHITE);
            row(g, r, 12, FLAG_W, RED);
        }
        g[2][7]=RED; g[2][8]=RED;
        g[3][6]=RED; g[3][7]=RED; g[3][8]=RED; g[3][9]=RED;
        g[4][5]=RED; g[4][6]=RED; g[4][7]=RED; g[4][8]=RED; g[4][9]=RED; g[4][10]=RED;
        g[5][7]=RED; g[5][8]=RED;
        g[6][6]=RED; g[6][7]=RED; g[6][8]=RED; g[6][9]=RED;
        g[7][7]=RED; g[7][8]=RED;
        g[8][7]=RED; g[8][8]=RED;
        return g;
    }

    private static int[][] flagAustralia() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, BLUE);
        fill(g, 0, 0, 7, 5, BLUE_D);
        row(g, 2, 0, 7, WHITE);
        col(g, 3, 0, 5, WHITE);
        g[8][3] = WHITE;
        g[7][3] = WHITE; g[9][3] = WHITE; g[8][2] = WHITE; g[8][4] = WHITE;
        g[3][11] = WHITE; g[5][13] = WHITE; g[8][11] = WHITE; g[6][14] = WHITE;
        return g;
    }

    private static int[][] flagNewZealand() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, BLUE);
        fill(g, 0, 0, 7, 5, BLUE_D);
        row(g, 2, 0, 7, WHITE);
        col(g, 3, 0, 5, WHITE);
        g[3][11] = RED; g[5][14] = RED; g[7][11] = RED; g[8][13] = RED;
        return g;
    }

    private static int[][] flagPirate() {
        // Negro con calavera blanca simplificada
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, BLACK);
        // craneo: circulo blanco
        int cx = 8, cy = 4;
        for (int r = cy-2; r <= cy+1; r++)
            for (int c = cx-2; c <= cx+1; c++)
                if (r < FLAG_H && c < FLAG_W) g[r][c] = WHITE;
        // ojos negros
        g[cy-1][cx-1] = BLACK; g[cy-1][cx] = BLACK;
        // huesos cruzados
        g[7][6] = WHITE; g[7][9] = WHITE;
        g[8][5] = WHITE; g[8][10] = WHITE;
        g[9][4] = WHITE; g[9][11] = WHITE;
        return g;
    }

    // ---- Espana y America hispana ----

    private static int[][] flagSpain() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) {
            int c;
            if (r < 3) c = RED;
            else if (r < 8) c = YELLOW;
            else c = RED;
            row(g, r, 0, FLAG_W, c);
        }
        return g;
    }

    private static int[][] flagMexico() {
        int[][] g = vBands(GREEN, WHITE, RED);
        int cx = 8, cy = 5;
        g[cy][cx] = BROWN;
        g[cy-1][cx] = BROWN;
        g[cy][cx-1] = BROWN; g[cy][cx+1] = BROWN;
        return g;
    }

    private static int[][] flagArgentina() {
        int[][] g = hBands(BLUE_SKY, WHITE, BLUE_SKY);
        int cx = 8, cy = 5;
        g[cy][cx] = YELLOW;
        g[cy-1][cx] = YELLOW; g[cy+1][cx] = YELLOW;
        g[cy][cx-1] = YELLOW; g[cy][cx+1] = YELLOW;
        return g;
    }

    private static int[][] flagChile() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) {
            int c = r < 5 ? WHITE : RED;
            row(g, r, 0, FLAG_W, c);
        }
        fill(g, 0, 0, 6, 5, BLUE);
        // estrella centrada en (3, 2)
        g[2][3] = WHITE;
        g[1][3] = WHITE; g[3][3] = WHITE;
        g[2][2] = WHITE; g[2][4] = WHITE;
        return g;
    }

    private static int[][] flagUruguay() {
        int[][] g = blank();
        // Alterna blanco / azul cielo pero simplificada a 5 franjas visibles
        int[] rowsColor = {WHITE, BLUE_SKY, WHITE, BLUE_SKY, WHITE, WHITE, BLUE_SKY, WHITE, BLUE_SKY, WHITE, BLUE_SKY};
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, rowsColor[r]);
        // canton blanco 6x5 arriba izq
        fill(g, 0, 0, 6, 5, WHITE);
        // sol amarillo
        int cx = 3, cy = 2;
        g[cy][cx] = YELLOW;
        g[cy-1][cx] = YELLOW; g[cy+1][cx] = YELLOW;
        g[cy][cx-1] = YELLOW; g[cy][cx+1] = YELLOW;
        return g;
    }

    private static int[][] flagVenezuela() {
        int[][] g = hBands(YELLOW, BLUE, RED);
        g[5][4] = WHITE; g[5][7] = WHITE; g[5][10] = WHITE;
        return g;
    }

    private static int[][] flagEcuador() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) {
            int c;
            if (r < 6)      c = YELLOW; // amarillo ocupa mitad (50%)
            else if (r < 9) c = BLUE;   // azul 25%
            else            c = RED;    // rojo 25%
            row(g, r, 0, FLAG_W, c);
        }
        return g;
    }

    private static int[][] flagLatinAmerica() {
        // Verde/blanco/rojo generico con tinte hispano
        return vBands(GREEN_L, WHITE, RED);
    }

    private static int[][] flagAndalusia() {
        // Verde/blanco/verde
        return hBands(GREEN, WHITE, GREEN);
    }

    private static int[][] flagColombia() {
        // Amarillo (mitad) / azul / rojo
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) {
            int c;
            if (r < 5) c = YELLOW;
            else if (r < 8) c = BLUE;
            else c = RED;
            row(g, r, 0, FLAG_W, c);
        }
        return g;
    }

    private static int[][] flagPeru() {
        // Rojo/blanco/rojo vertical
        return vBands(RED, WHITE, RED);
    }

    private static int[][] flagBolivia() {
        // Rojo/amarillo/verde horizontal
        return hBands(RED, YELLOW, GREEN);
    }

    private static int[][] flagParaguay() {
        // Rojo/blanco/azul horizontal con emblema central
        int[][] g = hBands(RED, WHITE, BLUE);
        // emblema pequeno en la banda blanca
        g[5][8] = YELLOW;
        return g;
    }

    private static int[][] flagCostaRica() {
        // Azul/blanco/rojo(doble)/blanco/azul
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) {
            int c;
            if (r < 2) c = BLUE;
            else if (r < 4) c = WHITE;
            else if (r < 7) c = RED;
            else if (r < 9) c = WHITE;
            else c = BLUE;
            row(g, r, 0, FLAG_W, c);
        }
        return g;
    }

    private static int[][] flagDominicanRep() {
        // Cruz blanca dividiendo azul / rojo (cuadrantes)
        int[][] g = blank();
        // Cuadrantes
        fill(g, 0, 0, 8, 5, BLUE);
        fill(g, 8, 0, FLAG_W, 5, RED);
        fill(g, 0, 6, 8, FLAG_H, RED);
        fill(g, 8, 6, FLAG_W, FLAG_H, BLUE);
        // cruz blanca
        row(g, 5, 0, FLAG_W, WHITE);
        col(g, 7, 0, FLAG_H, WHITE); col(g, 8, 0, FLAG_H, WHITE);
        return g;
    }

    private static int[][] flagGuatemala() {
        // Azul/blanco/azul vertical con emblema
        int[][] g = vBands(BLUE_SKY, WHITE, BLUE_SKY);
        g[5][8] = GREEN;
        return g;
    }

    private static int[][] flagHonduras() {
        // Azul/blanco/azul horizontal con 5 estrellas
        int[][] g = hBands(BLUE_SKY, WHITE, BLUE_SKY);
        g[5][5] = BLUE_SKY; g[5][7] = BLUE_SKY;
        g[5][8] = BLUE_SKY; g[5][10] = BLUE_SKY;
        return g;
    }

    private static int[][] flagNicaragua() {
        // Azul/blanco/azul horizontal con emblema central
        int[][] g = hBands(BLUE_SKY, WHITE, BLUE_SKY);
        g[5][8] = BLUE;
        return g;
    }

    private static int[][] flagPanama() {
        // 4 cuadrantes: blanco/rojo/azul/blanco
        int[][] g = blank();
        fill(g, 0, 0, 8, 5, WHITE);
        fill(g, 8, 0, FLAG_W, 5, RED);
        fill(g, 0, 5, 8, FLAG_H, BLUE);
        fill(g, 8, 5, FLAG_W, FLAG_H, WHITE);
        // estrellitas
        g[2][3] = BLUE; g[8][12] = RED;
        return g;
    }

    private static int[][] flagElSalvador() {
        // Azul/blanco/azul horizontal con emblema central
        int[][] g = hBands(BLUE_SKY, WHITE, BLUE_SKY);
        g[5][8] = RED;
        return g;
    }

    private static int[][] flagPuertoRico() {
        // 5 franjas rojo/blanco + triangulo azul con estrella
        int[][] g = blank();
        int[] cols = {RED,WHITE,RED,WHITE,RED,WHITE,RED,WHITE,RED,WHITE,RED};
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, cols[r]);
        // triangulo azul izq
        for (int r = 0; r < FLAG_H; r++) {
            int w = r <= FLAG_H / 2 ? r + 1 : FLAG_H - r;
            row(g, r, 0, w, BLUE);
        }
        // estrella blanca
        g[5][2] = WHITE;
        return g;
    }

    private static int[][] flagCuba() {
        // 5 franjas azul/blanco + triangulo rojo con estrella blanca
        int[][] g = blank();
        int[] rowsColor = {BLUE, WHITE, BLUE, WHITE, BLUE, BLUE, BLUE, WHITE, BLUE, WHITE, BLUE};
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, rowsColor[r]);
        // triangulo rojo izq
        for (int r = 0; r < FLAG_H; r++) {
            int w = r <= FLAG_H / 2 ? r + 1 : FLAG_H - r;
            row(g, r, 0, w, RED);
        }
        // estrella blanca
        g[5][2] = WHITE;
        return g;
    }

    // ---- Otros ----

    private static int[][] flagQuebec() {
        // Azul con cruz blanca + 4 flores de lis blancas (simplificadas)
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, BLUE);
        row(g, 5, 0, FLAG_W, WHITE);
        col(g, 7, 0, FLAG_H, WHITE); col(g, 8, 0, FLAG_H, WHITE);
        // flores
        g[2][3] = WHITE; g[2][12] = WHITE;
        g[8][3] = WHITE; g[8][12] = WHITE;
        return g;
    }

    private static int[][] flagBrazil() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, GREEN);
        for (int r = 1; r < FLAG_H - 1; r++) {
            int margin = Math.abs(r - 5) + 1;
            row(g, r, margin, FLAG_W - margin, YELLOW);
        }
        fill(g, 6, 4, 11, 7, BLUE);
        return g;
    }

    private static int[][] flagPortugal() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) {
            row(g, r, 0, 6, GREEN);
            row(g, r, 6, FLAG_W, RED);
        }
        g[5][6] = YELLOW; g[5][5] = YELLOW; g[5][7] = YELLOW;
        g[4][6] = YELLOW; g[6][6] = YELLOW;
        return g;
    }

    private static int[][] flagSwiss() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, RED);
        fill(g, 6, 4, 10, 7, WHITE);
        fill(g, 7, 3, 9, 8, WHITE);
        return g;
    }

    private static int[][] nordicCross(int bg, int cross) {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, bg);
        for (int r = 4; r <= 6; r++) row(g, r, 0, FLAG_W, cross);
        for (int r = 0; r < FLAG_H; r++) { g[r][5] = cross; g[r][6] = cross; }
        return g;
    }

    private static int[][] flagNorway() {
        int[][] g = nordicCross(RED, WHITE);
        row(g, 5, 0, FLAG_W, BLUE);
        for (int r = 0; r < FLAG_H; r++) g[r][5] = BLUE;
        return g;
    }

    private static int[][] flagIceland() {
        int[][] g = nordicCross(BLUE, WHITE);
        row(g, 5, 0, FLAG_W, RED);
        for (int r = 0; r < FLAG_H; r++) g[r][5] = RED;
        return g;
    }

    private static int[][] flagCzechia() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, r < FLAG_H / 2 ? WHITE : RED);
        for (int r = 0; r < FLAG_H; r++) {
            int w = r <= FLAG_H / 2 ? r + 1 : FLAG_H - r;
            row(g, r, 0, w, BLUE);
        }
        return g;
    }

    private static int[][] flagBosnia() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, BLUE);
        for (int r = 0; r < FLAG_H; r++) {
            int start = r;
            int end = Math.min(r + 6, FLAG_W);
            row(g, r, start, end, YELLOW);
        }
        return g;
    }

    private static int[][] flagAlbania() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, RED);
        g[4][7] = BLACK; g[4][8] = BLACK;
        g[5][6] = BLACK; g[5][7] = BLACK; g[5][8] = BLACK; g[5][9] = BLACK;
        g[6][7] = BLACK; g[6][8] = BLACK;
        return g;
    }

    private static int[][] flagLatvia() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) {
            int c = (r < 4 || r >= 7) ? RED_D : WHITE;
            row(g, r, 0, FLAG_W, c);
        }
        return g;
    }

    private static int[][] flagGreece() {
        int[][] g = blank();
        int[] rowsColor = {BLUE, WHITE, BLUE, WHITE, BLUE, WHITE, BLUE, WHITE, BLUE, WHITE, BLUE};
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, rowsColor[r]);
        fill(g, 0, 0, 6, 5, BLUE);
        row(g, 2, 0, 6, WHITE);
        col(g, 2, 0, 5, WHITE);
        return g;
    }

    private static int[][] flagTurkey() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, RED);
        int cx = 6, cy = 5;
        for (int r = cy-3; r <= cy+3; r++)
            for (int c = cx-3; c <= cx+3; c++) {
                if (r < 0 || r >= FLAG_H || c < 0 || c >= FLAG_W) continue;
                int dr = r-cy, dc = c-cx;
                if (dr*dr + dc*dc <= 9) g[r][c] = WHITE;
            }
        for (int r = cy-2; r <= cy+2; r++)
            for (int c = cx-1; c <= cx+3; c++) {
                if (r < 0 || r >= FLAG_H || c < 0 || c >= FLAG_W) continue;
                int dr = r-cy, dc = c-(cx+2);
                if (dr*dr + dc*dc <= 5) g[r][c] = RED;
            }
        g[5][11] = WHITE; g[4][11] = WHITE; g[6][11] = WHITE;
        g[5][10] = WHITE; g[5][12] = WHITE;
        return g;
    }

    private static int[][] flagJapan() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, WHITE);
        int cx = 8, cy = 5;
        for (int r = 0; r < FLAG_H; r++)
            for (int c = 0; c < FLAG_W; c++) {
                int dr = r-cy, dc = c-cx;
                if (dr*dr + dc*dc <= 9) g[r][c] = RED;
            }
        return g;
    }

    private static int[][] flagKorea() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, WHITE);
        int cx = 8, cy = 5;
        for (int r = 0; r < FLAG_H; r++)
            for (int c = 0; c < FLAG_W; c++) {
                int dr = r-cy, dc = c-cx;
                if (dr*dr + dc*dc <= 9) {
                    g[r][c] = r < cy ? RED : BLUE;
                }
            }
        g[1][2] = BLACK; g[1][3] = BLACK;
        g[9][12] = BLACK; g[9][13] = BLACK;
        return g;
    }

    private static int[][] flagChina() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, RED);
        g[2][3] = YELLOW; g[3][2] = YELLOW; g[3][3] = YELLOW; g[3][4] = YELLOW;
        g[4][3] = YELLOW; g[2][2] = YELLOW; g[2][4] = YELLOW;
        g[1][6] = YELLOW; g[2][7] = YELLOW; g[4][7] = YELLOW; g[5][6] = YELLOW;
        return g;
    }

    private static int[][] flagTaiwan() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, RED);
        fill(g, 0, 0, 7, 5, BLUE);
        g[2][3] = WHITE;
        g[1][3] = WHITE; g[3][3] = WHITE;
        g[2][2] = WHITE; g[2][4] = WHITE;
        return g;
    }

    private static int[][] flagVietnam() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, RED);
        int cx = 8, cy = 5;
        g[cy][cx-2] = YELLOW; g[cy][cx-1] = YELLOW; g[cy][cx] = YELLOW;
        g[cy][cx+1] = YELLOW; g[cy][cx+2] = YELLOW;
        g[cy-1][cx] = YELLOW; g[cy+1][cx] = YELLOW;
        g[cy-2][cx-1] = YELLOW; g[cy-2][cx+1] = YELLOW;
        g[cy+2][cx-1] = YELLOW; g[cy+2][cx+1] = YELLOW;
        return g;
    }

    private static int[][] flagThailand() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) {
            int c;
            if (r < 2)      c = RED;
            else if (r < 4) c = WHITE;
            else if (r < 7) c = BLUE_D;
            else if (r < 9) c = WHITE;
            else            c = RED;
            row(g, r, 0, FLAG_W, c);
        }
        return g;
    }

    private static int[][] flagPhilippines() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, r < FLAG_H / 2 ? BLUE : RED);
        for (int r = 0; r < FLAG_H; r++) {
            int w = r <= FLAG_H / 2 ? r + 1 : FLAG_H - r;
            row(g, r, 0, w, WHITE);
        }
        g[5][2] = YELLOW;
        return g;
    }

    private static int[][] flagMalaysia() {
        int[][] g = blank();
        int[] cols = {RED,WHITE,RED,WHITE,RED,WHITE,RED,WHITE,RED,WHITE,RED};
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, cols[r]);
        fill(g, 0, 0, 7, 6, BLUE);
        g[2][3] = YELLOW; g[3][3] = YELLOW; g[3][2] = YELLOW;
        return g;
    }

    private static int[][] flagIsrael() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, WHITE);
        row(g, 1, 0, FLAG_W, BLUE);
        row(g, 9, 0, FLAG_W, BLUE);
        int cx = 8, cy = 5;
        g[cy][cx] = BLUE;
        g[cy-1][cx] = BLUE; g[cy+1][cx] = BLUE;
        g[cy][cx-1] = BLUE; g[cy][cx+1] = BLUE;
        return g;
    }

    private static int[][] flagSouthAfrica() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) {
            int c;
            if (r < 4)      c = RED;
            else if (r < 7) c = WHITE;
            else            c = BLUE;
            row(g, r, 0, FLAG_W, c);
        }
        for (int r = 4; r < 7; r++) row(g, r, 6, FLAG_W, GREEN);
        for (int r = 0; r < FLAG_H; r++) {
            int w = r <= FLAG_H / 2 ? r + 1 : FLAG_H - r;
            row(g, r, 0, w, BLACK);
        }
        return g;
    }

    private static int[][] flagGeorgia() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, WHITE);
        row(g, 4, 0, FLAG_W, RED); row(g, 5, 0, FLAG_W, RED); row(g, 6, 0, FLAG_W, RED);
        col(g, 7, 0, FLAG_H, RED); col(g, 8, 0, FLAG_H, RED);
        g[1][2] = RED; g[1][13] = RED;
        g[9][2] = RED; g[9][13] = RED;
        return g;
    }

    private static int[][] flagKazakhstan() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, BLUE_SKY);
        int cx = 8, cy = 5;
        g[cy][cx] = YELLOW;
        g[cy-1][cx] = YELLOW; g[cy+1][cx] = YELLOW;
        g[cy][cx-1] = YELLOW; g[cy][cx+1] = YELLOW;
        return g;
    }

    private static int[][] flagMongolia() {
        int[][] g = vBands(RED, BLUE_SKY, RED);
        g[3][2] = YELLOW; g[5][2] = YELLOW; g[7][2] = YELLOW;
        return g;
    }

    private static int[][] flagCatalonia() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, YELLOW);
        for (int r = 1; r < FLAG_H; r += 2) row(g, r, 0, FLAG_W, RED);
        return g;
    }

    private static int[][] flagBasque() {
        // Ikurrina: fondo rojo + X verde (San Andres) + cruz blanca (+ ) encima
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, RED);
        // X verde diagonal
        for (int i = 0; i < FLAG_H; i++) {
            int x = i * (FLAG_W - 1) / (FLAG_H - 1);
            g[i][x] = GREEN;
            if (x + 1 < FLAG_W) g[i][x + 1] = GREEN;
            g[i][FLAG_W - 1 - x] = GREEN;
            if (FLAG_W - 2 - x >= 0) g[i][FLAG_W - 2 - x] = GREEN;
        }
        // cruz blanca + encima
        row(g, 5, 0, FLAG_W, WHITE);
        col(g, 7, 0, FLAG_H, WHITE); col(g, 8, 0, FLAG_H, WHITE);
        return g;
    }

    private static int[][] flagGalicia() {
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, WHITE);
        for (int i = 0; i < FLAG_H; i++) {
            int x = i * (FLAG_W - 1) / (FLAG_H - 1);
            if (x < FLAG_W) g[i][x] = BLUE;
            if (x + 1 < FLAG_W) g[i][x + 1] = BLUE;
        }
        return g;
    }

    private static int[][] flagWales() {
        // Blanco arriba, verde abajo, con "dragon" rojo (rectangulo simplificado)
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, r < FLAG_H / 2 ? WHITE : GREEN);
        fill(g, 6, 3, 10, 7, RED);
        return g;
    }

    private static int[][] flagScotland() {
        // Azul con cruz de San Andres blanca (X)
        int[][] g = blank();
        for (int r = 0; r < FLAG_H; r++) row(g, r, 0, FLAG_W, BLUE);
        for (int i = 0; i < FLAG_H; i++) {
            int x = i * (FLAG_W - 1) / (FLAG_H - 1);
            g[i][x] = WHITE;
            g[i][FLAG_W-1-x] = WHITE;
        }
        return g;
    }
}