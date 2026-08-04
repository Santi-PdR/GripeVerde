package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.gui.GuiGraphics;
import java.util.Random;

/**
 * PaperTexture v3.0 - Extreme Polish
 * Different paper stocks per screen, micro imperfections, consistent material library.
 */
public final class PaperTexture {

    private static final long SEED = 189011974L;
    private static int openCount = 0;
    public static void incrementOpenCount() { if (openCount < 12) openCount++; }
    public static int getOpenCount() { return openCount; }

    private static final long BLOOD_SEED = 66613337L;
    private static final long COFFEE_SEED = 0xC0FFEE420L;
    private static final long TORN_SEED   = 0x707E12345L;
    private static final long FIBER_SEED  = 0xF1BE12L;
    private static final long INK_DOT_SEED = 0x1A2B3C4DL;

    public enum PaperKind {
        LEDGER, CORRESPONDENCE, ARCHIVE, TECHNICAL, MEDICAL, OFFICIAL, DEFAULT
    }

    private PaperTexture() {}

    // ── Color base per kind ──
    private static int baseColorFor(PaperKind k) {
        return switch (k) {
            case LEDGER -> 0xFFE9DDC2; // slightly darker, heavy ledger
            case CORRESPONDENCE -> 0xFFEFEBD0; // lighter letter
            case ARCHIVE -> 0xFFE2CF9E; // aged archive
            case TECHNICAL -> 0xFFE6DBC0; // blueprint-ish
            case MEDICAL -> 0xFFECE8C8; // clinical parchment, faint greenish tint handled in burnish
            case OFFICIAL -> 0xFFE8D7B0; // official with slight pink
            case DEFAULT -> DiaryTheme.PAPER;
        };
    }

    private static boolean stainChance(PaperKind k) {
        return k != PaperKind.MEDICAL; // medical is cleaner
    }

    // ── Public API ──
    public static void draw(GuiGraphics g, int x, int y, int w, int h) { draw(g,x,y,w,h,PaperKind.DEFAULT); }
    public static void draw(GuiGraphics g, int x, int y, int w, int h, PaperKind kind) {
        if (w<=0||h<=0) return;
        g.fill(x,y,x+w,y+h, baseColorFor(kind));
        drawVignetteFast(g,x,y,w,h,kind);
        drawStainsOptimized(g,x,y,w,h,kind);
        drawWrinkles(g,x,y,w,h,kind);
        drawBloodMist(g,x,y,w,h,kind);
    }

    public static void drawClean(GuiGraphics g, int x, int y, int w, int h) { drawClean(g,x,y,w,h,PaperKind.DEFAULT); }
    public static void drawClean(GuiGraphics g, int x, int y, int w, int h, PaperKind kind) {
        if (w<=0||h<=0) return;
        g.fill(x,y,x+w,y+h, baseColorFor(kind));
        drawWatermark(g,x,y,w,h,kind);
        drawPaperFibersOptimized(g,x,y,w,h,kind);
        drawVignetteFast(g,x,y,w,h,kind);
        drawStainsOptimized(g,x,y,w,h,kind);
        if (w>=120&&h>=120&&stainChance(kind)) drawCoffeeStain(g,x,y,w,h,kind);
        drawBloodMist(g,x,y,w,h,kind);
        drawFaintRuledMargins(g,x,y,w,h,kind);
        drawCornerPinholes(g,x,y,w,h,kind);
        drawBottomTeaTint(g,x,y,w,h,kind);
        drawPageEdgeBurnish(g,x,y,w,h,kind);
        if (w>=160&&h>=160) {
            if (kind==PaperKind.LEDGER||kind==PaperKind.OFFICIAL) drawSubtleBindingFibers(g,x,y,w,h,kind);
            if (kind==PaperKind.OFFICIAL||kind==PaperKind.ARCHIVE) drawFaintRegistryNotches(g,x,y,w,h,kind);
        }
        drawTornEdges(g,x,y,w,h,kind);
        if (w>=180&&h>=180&&kind!=PaperKind.MEDICAL) drawWaxSeal(g,x,y,w,h,kind);
        if (kind!=PaperKind.CORRESPONDENCE && w>200) drawMicroInkDot(g,x,y,w,h,kind);
    }

    // Backward compat overloads (no kind)
    public static void drawVignetteFast(GuiGraphics g,int x,int y,int w,int h){drawVignetteFast(g,x,y,w,h,PaperKind.DEFAULT);}
    public static void drawStainsOptimized(GuiGraphics g,int x,int y,int w,int h){drawStainsOptimized(g,x,y,w,h,PaperKind.DEFAULT);}
    public static void drawWrinkles(GuiGraphics g,int x,int y,int w,int h){drawWrinkles(g,x,y,w,h,PaperKind.DEFAULT);}
    public static void drawBloodMist(GuiGraphics g,int x,int y,int w,int h){drawBloodMist(g,x,y,w,h,PaperKind.DEFAULT);}
    public static void drawCoffeeStain(GuiGraphics g,int x,int y,int w,int h){drawCoffeeStain(g,x,y,w,h,PaperKind.DEFAULT);}
    public static void drawWaxSeal(GuiGraphics g,int x,int y,int w,int h){drawWaxSeal(g,x,y,w,h,PaperKind.DEFAULT);}
    public static void drawTornEdges(GuiGraphics g,int x,int y,int w,int h){drawTornEdges(g,x,y,w,h,PaperKind.DEFAULT);}
    public static void drawPaperFibersOptimized(GuiGraphics g,int x,int y,int w,int h){drawPaperFibersOptimized(g,x,y,w,h,PaperKind.DEFAULT);}
    public static void drawWatermark(GuiGraphics g,int x,int y,int w,int h){drawWatermark(g,x,y,w,h,PaperKind.DEFAULT);}
    public static void drawFaintRuledMargins(GuiGraphics g,int x,int y,int w,int h){drawFaintRuledMargins(g,x,y,w,h,PaperKind.DEFAULT);}
    public static void drawCornerPinholes(GuiGraphics g,int x,int y,int w,int h){drawCornerPinholes(g,x,y,w,h,PaperKind.DEFAULT);}
    public static void drawBottomTeaTint(GuiGraphics g,int x,int y,int w,int h){drawBottomTeaTint(g,x,y,w,h,PaperKind.DEFAULT);}
    public static void drawPageEdgeBurnish(GuiGraphics g,int x,int y,int w,int h){drawPageEdgeBurnish(g,x,y,w,h,PaperKind.DEFAULT);}
    public static void drawSubtleBindingFibers(GuiGraphics g,int x,int y,int w,int h){drawSubtleBindingFibers(g,x,y,w,h,PaperKind.DEFAULT);}
    public static void drawFaintRegistryNotches(GuiGraphics g,int x,int y,int w,int h){drawFaintRegistryNotches(g,x,y,w,h,PaperKind.DEFAULT);}

    // ── Detailed per-kind helpers ──
    private static void drawFaintRuledMargins(GuiGraphics g,int x,int y,int w,int h, PaperKind k){
        if (w<130||h<90) return;
        int line, line2;
        if (k==PaperKind.CORRESPONDENCE) {
            line = DiaryTheme.withAlpha(0x006B9AC8, 18); // faint blue for letters
            line2 = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, 10);
        } else if (k==PaperKind.TECHNICAL) {
            line = DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 16);
            line2 = DiaryTheme.withAlpha(0x004A6B8A, 14);
        } else {
            line = DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 20);
            line2 = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, 12);
        }
        g.fill(x+18,y+22,x+w-18,y+23,line);
        g.fill(x+18,y+h-24,x+w-18,y+h-23,line2);
        g.fill(x+22,y+18,x+23,y+h-18,line2);
        g.fill(x+w-23,y+18,x+w-22,y+h-18,line2);
        // Technical adds vertical grid faint
        if (k==PaperKind.TECHNICAL && w>200) {
            int grid = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, 8);
            for (int gx=x+40; gx<x+w-40; gx+=32) g.fill(gx,y+22,gx+1,y+h-23,grid);
        }
    }

    private static void drawCornerPinholes(GuiGraphics g,int x,int y,int w,int h, PaperKind k){
        if (w<90||h<70) return;
        int pinAlpha = k==PaperKind.ARCHIVE ? 38 : 26;
        int pin = DiaryTheme.withAlpha(DiaryTheme.INK, pinAlpha);
        int[][] pts = {{x+12,y+12},{x+w-13,y+12},{x+12,y+h-13},{x+w-13,y+h-13}};
        for (int[] pt: pts) g.fill(pt[0],pt[1],pt[0]+2,pt[1]+2,pin);
        // Archive has double pinholes (filed twice)
        if (k==PaperKind.ARCHIVE) {
            int pin2 = DiaryTheme.withAlpha(DiaryTheme.INK, 18);
            for (int[] pt: pts) g.fill(pt[0]+3,pt[1]+3,pt[0]+5,pt[1]+5,pin2);
        }
    }

    private static void drawBottomTeaTint(GuiGraphics g,int x,int y,int w,int h, PaperKind k){
        if (h<80) return;
        int tintH = Math.max(14, h/7);
        int bottomAlpha = k==PaperKind.ARCHIVE ? 26 : k==PaperKind.MEDICAL ? 10 : 18;
        int topC = 0x00000000;
        int botC = DiaryTheme.withAlpha(k==PaperKind.MEDICAL ? 0x006B8A6B : 0x006B4A2E, bottomAlpha);
        // Medical has greenish tea at top too (disinfectant stain)
        if (k==PaperKind.MEDICAL) {
            g.fillGradient(x, y, x+w, y+12, DiaryTheme.withAlpha(0x006B8A6B,8), 0x00000000);
        }
        g.fillGradient(x, y+h-tintH, x+w, y+h, topC, botC);
    }

    private static void drawPageEdgeBurnish(GuiGraphics g,int x,int y,int w,int h, PaperKind k){
        if (w<80||h<70) return;
        int warm = DiaryTheme.withAlpha(k==PaperKind.MEDICAL ? 0x005A8A6B : 0x00806030, k==PaperKind.ARCHIVE?20:14);
        int cool = DiaryTheme.withAlpha(0x00FFFFFF, 10);
        g.fill(x+2,y+2,x+w-2,y+3,cool);
        g.fill(x+3,y+h-4,x+w-3,y+h-3,warm);
        // Archive has stronger edge darken on right (shelf wear)
        if (k==PaperKind.ARCHIVE) {
            g.fill(x+w-5,y+4,x+w-3,y+h-4, DiaryTheme.withAlpha(0x00806030,12));
        }
    }

    private static void drawSubtleBindingFibers(GuiGraphics g,int x,int y,int w,int h, PaperKind k){
        Random rnd = new Random(SEED ^ 0xB1D1L ^ k.ordinal());
        int col = DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, k==PaperKind.LEDGER?13:10);
        int count = Math.max(4, Math.min(k==PaperKind.LEDGER?12:8, h/32));
        int left = x+10;
        for (int i=0;i<count;i++){
            int yy = y+18+rnd.nextInt(Math.max(1,h-36));
            g.fill(left,yy,left+5+rnd.nextInt(4),yy+1,col);
        }
    }

    private static void drawFaintRegistryNotches(GuiGraphics g,int x,int y,int w,int h, PaperKind k){
        int col = DiaryTheme.withAlpha(DiaryTheme.INK, k==PaperKind.OFFICIAL?18:12);
        for (int i=0;i<2;i++){
            int yy = y+h/3+i*h/5;
            g.fill(x+w-10,yy,x+w-6,yy+1,col);
            if (k==PaperKind.OFFICIAL) g.fill(x+w-9,yy+1,x+w-7,yy+3,DiaryTheme.withAlpha(DiaryTheme.INK,10));
        }
    }

    private static void drawVignetteFast(GuiGraphics g,int x,int y,int w,int h, PaperKind k){
        int band = Math.max(4, Math.min(12, Math.min(w,h)/18));
        int topAlpha = k==PaperKind.ARCHIVE?62:52;
        int midAlpha = k==PaperKind.CORRESPONDENCE?16:20;
        int top = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, topAlpha);
        int mid = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, midAlpha);
        g.fillGradient(x,y,x+w,y+band,top,mid);
        g.fillGradient(x,y+h-band,x+w,y+h,mid,top);
        g.fillGradient(x,y+band,x+band,y+h-band,top,mid);
        g.fillGradient(x+w-band,y+band,x+w,y+h-band,mid,top);
    }

    private static void drawStainsOptimized(GuiGraphics g,int x,int y,int w,int h, PaperKind k){
        if (w<=0||h<=0) return;
        Random rnd = new Random(SEED ^ k.ordinal());
        int area = w*h;
        int baseCount = switch(k){
            case ARCHIVE -> 7;
            case LEDGER -> 5;
            case OFFICIAL -> 4;
            case CORRESPONDENCE -> 3;
            case MEDICAL -> 1;
            case TECHNICAL -> 2;
            default -> 4;
        };
        int count = Math.min(10, Math.max(baseCount, area/40000) + Math.min(2, openCount/2));
        for (int i=0;i<count;i++){
            int cx = x+rnd.nextInt(Math.max(1,w));
            int cy = y+rnd.nextInt(Math.max(1,h));
            int radius = 3+rnd.nextInt( k==PaperKind.ARCHIVE?11:8 );
            int alpha = 10+rnd.nextInt( k==PaperKind.ARCHIVE?28:20 );
            int col = (rnd.nextInt(4)==0) ? DiaryTheme.withAlpha(DiaryTheme.INK_FADED, alpha) : DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, alpha);
            fillCircleFast(g,cx,cy,radius,DiaryTheme.withAlpha(col & 0x00FFFFFF, alpha));
            fillCircleFast(g,cx,cy,radius+2,DiaryTheme.withAlpha(col & 0x00FFFFFF, alpha/4));
        }
    }

    private static void drawWrinkles(GuiGraphics g,int x,int y,int w,int h, PaperKind k){
        Random rnd = new Random(SEED+1+k.ordinal());
        int lines = Math.max(2, Math.min(k==PaperKind.ARCHIVE?7:5, h/70));
        for (int i=0;i<lines;i++){
            int wy = y+rnd.nextInt(Math.max(1,h));
            int startX = x+rnd.nextInt(Math.max(1,w/3));
            int len = w/3+rnd.nextInt(Math.max(1,w/2));
            int endX = Math.min(x+w, startX+len);
            if (endX<=startX) continue;
            int alpha = 14+rnd.nextInt( k==PaperKind.ARCHIVE?20:14 );
            g.fill(startX,wy,endX,wy+1,DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, alpha));
        }
    }

    private static void drawBloodMist(GuiGraphics g,int x,int y,int w,int h, PaperKind k){
        Random rnd = new Random(BLOOD_SEED ^ k.ordinal());
        int drops = Math.min(k==PaperKind.MEDICAL?6:3, Math.max(1,(w*h)/140000));
        if (k==PaperKind.MEDICAL) drops+=1;
        for (int i=0;i<drops;i++){
            int cx=x+rnd.nextInt(Math.max(1,w));
            int cy=y+rnd.nextInt(Math.max(1,h));
            int alpha = 20+rnd.nextInt(24);
            int col = DiaryTheme.withAlpha(DiaryTheme.BLOOD_DRIED, alpha);
            g.fill(cx,cy,cx+1,cy+1,col);
        }
    }

    private static void drawCoffeeStain(GuiGraphics g,int x,int y,int w,int h, PaperKind k){
        if (w<60||h<60) return;
        Random rnd = new Random(COFFEE_SEED ^ k.ordinal());
        int cx = x+20+rnd.nextInt(Math.max(1,w-40));
        int cy = y+20+rnd.nextInt(Math.max(1,h-40));
        int size = 9+rnd.nextInt(7);
        // Medical has disinfectant green, not coffee brown
        if (k==PaperKind.MEDICAL) {
            fillCircleFast(g,cx,cy,size,DiaryTheme.withAlpha(0x005A8A6B,22));
        } else {
            fillCircleFast(g,cx,cy,size,DiaryTheme.withAlpha(0x00543818,26));
            fillCircleFast(g,cx,cy,size+2,DiaryTheme.withAlpha(0x00A8783C,10));
        }
    }

    private static void drawWaxSeal(GuiGraphics g,int px,int py,int pw,int ph, PaperKind k){
        if (pw<80||ph<80) return;
        int cx = px+pw-22;
        int cy = py+ph-22;
        int r = 7;
        // Seal color varies: official red deeper, archive brown, ledger brass
        int sealBase = switch(k){
            case OFFICIAL -> 0xFF7A1F18;
            case ARCHIVE -> 0xFF6B3D1A;
            case LEDGER -> 0xFF8C5A2E;
            default -> DiaryTheme.WAX_SEAL;
        };
        fillCircleFast(g,cx+1,cy+1,r+1,DiaryTheme.withAlpha(0x00000000,62));
        fillCircleFast(g,cx,cy,r,sealBase);
        fillCircleFast(g,cx-1,cy-1,r-2, DiaryTheme.withAlpha(DiaryTheme.BLOOD_FRESH, 180));
        g.fill(cx-2,cy-3,cx,cy-2,DiaryTheme.withAlpha(0x00FFAA88,70));
        // Small letter in seal for official
        if (k==PaperKind.OFFICIAL) {
            g.fill(cx-1,cy-1,cx,cy+1,DiaryTheme.withAlpha(0x00000000,90));
            g.fill(cx,cy-1,cx+1,cy,DiaryTheme.withAlpha(0x00000000,90));
        }
    }

    private static void drawTornEdges(GuiGraphics g,int x,int y,int w,int h, PaperKind k){
        Random rnd = new Random(TORN_SEED ^ k.ordinal());
        int col = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, k==PaperKind.ARCHIVE?92:78);
        int notches = k==PaperKind.ARCHIVE?3:2;
        for (int i=0;i<notches;i++){
            g.fill(x+rnd.nextInt(6),y+rnd.nextInt(3),x+rnd.nextInt(6)+1,y+rnd.nextInt(3)+1,col);
            g.fill(x+w-6+rnd.nextInt(6),y+rnd.nextInt(3),x+w-5+rnd.nextInt(6)+1,y+1,col);
            g.fill(x+rnd.nextInt(6),y+h-3+rnd.nextInt(3),x+1,y+h-2,col);
            g.fill(x+w-6+rnd.nextInt(6),y+h-3+rnd.nextInt(3),x+w-5,y+h-2,col);
        }
    }

    private static void drawPaperFibersOptimized(GuiGraphics g,int x,int y,int w,int h, PaperKind k){
        Random rnd = new Random(FIBER_SEED ^ k.ordinal());
        int base = switch(k){
            case ARCHIVE -> 90;
            case LEDGER -> 70;
            case TECHNICAL -> 50;
            case CORRESPONDENCE -> 35;
            case MEDICAL -> 20;
            default -> 60;
        };
        int count = Math.max(20, Math.min(120, (w*h)/1600 + base/2));
        int col1 = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, k==PaperKind.MEDICAL?10:12);
        for (int i=0;i<count;i++){
            int px = x+rnd.nextInt(Math.max(1,w));
            int py = y+rnd.nextInt(Math.max(1,h));
            int len = 2+rnd.nextInt(2);
            g.fill(px,py,px+len,py+1,col1);
        }
    }

    private static void drawWatermark(GuiGraphics g,int x,int y,int w,int h, PaperKind k){
        if (w<110||h<110) return;
        int cx = x+w/2;
        int cy = y+h/2;
        int col = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, k==PaperKind.OFFICIAL?14: k==PaperKind.ARCHIVE?18 : 12);
        int s=10;
        int gap=4;
        int ex=cx-s-gap/2;
        int ey=cy-s;
        g.fill(ex,ey,ex+s,ey+2,col);
        g.fill(ex,ey+s-1,ex+s,ey+s+1,col);
        g.fill(ex,ey+s*2-2,ex+s,ey+s*2,col);
        g.fill(ex,ey,ex+2,ey+s*2,col);
        int cxl=cx+gap/2;
        g.fill(cxl,ey,cxl+s,ey+2,col);
        g.fill(cxl,ey+s*2-2,cxl+s,ey+s*2,col);
        g.fill(cxl,ey,cxl+2,ey+s*2,col);
        // Medical adds + cross faint
        if (k==PaperKind.MEDICAL) {
            int mcx = cx;
            int mcy = cy+18;
            int crossCol = DiaryTheme.withAlpha(0x005A8A6B,10);
            g.fill(mcx-1,mcy-4,mcx+2,mcy+5,crossCol);
            g.fill(mcx-5,mcy-1,mcx+6,mcy+2,crossCol);
        }
    }

    private static void drawMicroInkDot(GuiGraphics g,int x,int y,int w,int h, PaperKind k){
        Random rnd = new Random(INK_DOT_SEED ^ (x*31L + y*17L + k.ordinal()*13));
        if (rnd.nextInt(100) < 72) return; // 28% chance
        int px = x + w - 18 - rnd.nextInt(22);
        int py = y + 18 + rnd.nextInt(Math.max(1,h-60));
        int col = DiaryTheme.withAlpha(DiaryTheme.INK, 32+rnd.nextInt(28));
        g.fill(px,py,px+1,py+1,col);
        if (rnd.nextBoolean()) g.fill(px+1,py,px+2,py+1,DiaryTheme.withAlpha(DiaryTheme.INK,18));
    }

    // ── Bloody old API ──
    public static void drawBloody(GuiGraphics g,int x,int y,int w,int h){ drawBloody(g,x,y,w,h,PaperKind.DEFAULT); }
    public static void drawBloody(GuiGraphics g,int x,int y,int w,int h, PaperKind k){
        draw(g,x,y,w,h,k);
        Random rnd = new Random(BLOOD_SEED+42+k.ordinal());
        int splatters=2+rnd.nextInt(2);
        for(int i=0;i<splatters;i++){
            int cx=x+40+rnd.nextInt(Math.max(1,w-80));
            int cy=y+40+rnd.nextInt(Math.max(1,h-80));
            int size=4+rnd.nextInt(3);
            fillCircleFast(g,cx,cy,size+1,DiaryTheme.BLOOD_DRIED);
            fillCircleFast(g,cx,cy,size,DiaryTheme.BLOOD_FRESH);
        }
    }

    public static void drawSoftBlob(GuiGraphics g,int cx,int cy,int r,int col){
        if (r<=0) return;
        int a = (col>>>24)&0xFF; if(a==0)a=80;
        int rgb=col&0x00FFFFFF;
        fillCircleFast(g,cx,cy,r,(a<<24)|rgb);
        if(r>2) fillCircleFast(g,cx,cy,r+1,((a/3)<<24)|rgb);
    }
    public static void drawSolidBlob(GuiGraphics g,int cx,int cy,int r,int col){fillCircleFast(g,cx,cy,r,col);}
    private static void fillCircleFast(GuiGraphics g,int cx,int cy,int r,int c){
        if(r<=0)return;
        for(int dy=-r;dy<=r;dy++){
            int dx=(int)Math.sqrt((double)r*r-(double)dy*dy);
            if(dx<0)continue;
            g.fill(cx-dx,cy+dy,cx+dx+1,cy+dy+1,c);
        }
    }
}
