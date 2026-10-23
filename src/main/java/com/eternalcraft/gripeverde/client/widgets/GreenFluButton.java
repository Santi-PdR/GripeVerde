package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.util.EasedValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GreenFluButton extends AbstractButton {

    private static boolean reducedMotion() {
        return com.eternalcraft.gripeverde.client.util.ClientAccessibility.reducedMotion();
    }

    private final Component title;
    private final Component subtitle;
    private final boolean arsenalIcon;
    private final Runnable onClick;
    private Language cachedTextLanguage;
    private Component cachedTitleMessage;
    private Component cachedSubtitleMessage;
    private Font cachedTextFont;
    private int cachedTextWidthLimit = -1;
    private String cachedTitleText = "";
    private String cachedSubtitleText = "";
    private boolean wasHovered = false;
    private final EasedValue hoverAnim = new EasedValue(5.5f);
    private int pressTicks = 0;
    private final int wearX1, wearY1, wearX2, wearY2;

    public GreenFluButton(int x,int y,int w,int h, Component title, Component subtitle, ResourceLocation icon, Runnable onClick){
        super(x,y,w,h,title);
        this.title=title; this.subtitle=subtitle; this.onClick=onClick;
        this.arsenalIcon = icon != null && icon.getPath().contains("mods");
        int wearSeed = (x*29 + y*19 + title.getString().hashCode()) & 0x7fffffff;
        java.util.Random wearRng = new java.util.Random(wearSeed);
        if (wearRng.nextInt(100) < 40) {
            this.wearX1 = 48 + wearRng.nextInt(Math.max(1, w - 66));
            this.wearY1 = 8 + wearRng.nextInt(Math.max(1, h - 16));
        } else {
            this.wearX1 = this.wearY1 = -1;
        }
        if (wearRng.nextInt(100) < 40) {
            this.wearX2 = 48 + wearRng.nextInt(Math.max(1, w - 66));
            this.wearY2 = 8 + wearRng.nextInt(Math.max(1, h - 16));
        } else {
            this.wearX2 = this.wearY2 = -1;
        }
    }

    private void refreshTextCache(Font font, int maxWidth) {
        Language language = Language.getInstance();
        if (this.cachedTitleMessage == this.title && this.cachedSubtitleMessage == this.subtitle
                && this.cachedTextLanguage == language && this.cachedTextFont == font
                && this.cachedTextWidthLimit == maxWidth) return;

        this.cachedTitleMessage = this.title;
        this.cachedSubtitleMessage = this.subtitle;
        this.cachedTextLanguage = language;
        this.cachedTextFont = font;
        this.cachedTextWidthLimit = maxWidth;
        this.cachedTitleText = ellipsize(font, this.title.getString(), maxWidth);
        this.cachedSubtitleText = ellipsize(font, this.subtitle.getString(), maxWidth);
    }

    @Override public void onPress(){ UiSounds.playClick(); onClick.run(); }
    @Override public void playDownSound(SoundManager sm){}
    @Override public boolean mouseClicked(double mx,double my,int btn){
        if(active&&visible&&clicked(mx,my)) pressTicks=7;
        return super.mouseClicked(mx,my,btn);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick){
        boolean hovered=isHovered();
        boolean motionReduced = reducedMotion();
        if(hovered&&!wasHovered) UiSounds.playHover();
        wasHovered=hovered;
        if (motionReduced) hoverAnim.setImmediately(hovered);
        else {
            hoverAnim.setTarget(hovered);
            hoverAnim.tickRealtime();
        }
        float t=hoverAnim.getEased();
        boolean pressed=pressTicks>0&&active&&!motionReduced;
        if(pressTicks>0) pressTicks--;

        int x=getX(), y=getY(), w=width, h=height;
        int yDraw=y+(pressed?1:0);

        // Shadow
        if(active && !pressed){
            g.fill(x+2,yDraw+h,x+w+1,yDraw+h+2,DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 88));
            g.fill(x+3,yDraw+h+1,x+w+2,yDraw+h+3,DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 32));
        }

        // Background steel dark with subtle green plague tint
        int bgDark = 0xFF18100C;
        int bgHover = 0xFF2E1E18;
        int bgPressed = 0xFF201410;
        int bg = !active ? 0xFF1C1816 : pressed ? bgPressed : hoverAnim.lerpColor(bgDark, bgHover);
        g.fill(x,yDraw,x+w,yDraw+h,bg);

        // Top brass edge + inner highlight
        int topEdge = hoverAnim.lerpColor(DiaryTheme.BRONZE_DARK, DiaryTheme.REDCOAT);
        g.fill(x,yDraw,x+w,yDraw+1,topEdge);
        g.fill(x+1,yDraw+1,x+w-1,yDraw+2,DiaryTheme.withAlpha(0x00FFE8C0, 22+(int)(t*28)));

        // Bottom burnish
        g.fill(x+1,yDraw+h-2,x+w-1,yDraw+h-1,DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 62));

        // Hover red veil
        if(t>0.04f){
            int veil = DiaryTheme.withAlpha(DiaryTheme.REDCOAT, (int)(t*28));
            g.fill(x+1,yDraw+2,x+w-1,yDraw+h-2,veil);
        }

        // Border
        int border = hoverAnim.lerpColor(DiaryTheme.BRONZE_DARK, DiaryTheme.REDCOAT);
        if(!active) border = DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 70);
        g.fill(x,yDraw,x+w,yDraw+1,border);
        g.fill(x,yDraw+h-1,x+w,yDraw+h,border);
        g.fill(x,yDraw,x+1,yDraw+h,border);
        g.fill(x+w-1,yDraw,x+w,yDraw+h,border);

        // Rivets
        int rivet = hoverAnim.lerpColor(DiaryTheme.BRONZE_DARK, DiaryTheme.GOLD_TARNISHED);
        int rivetOff = DiaryTheme.withAlpha(rivet, 90);
        g.fill(x+4,yDraw+4,x+6,yDraw+6,rivet);
        g.fill(x+w-6,yDraw+4,x+w-4,yDraw+6,rivet);
        g.fill(x+4,yDraw+h-6,x+6,yDraw+h-4,rivetOff);
        g.fill(x+w-6,yDraw+h-6,x+w-4,yDraw+h-4,rivetOff);

        // Side bar with easing growth
        int barCol = hoverAnim.lerpColor(DiaryTheme.SEPIA_DARK, DiaryTheme.REDCOAT_BRIGHT);
        int barRed = (int)((1f-t)*((h-6)/2));
        g.fill(x+2,yDraw+2+barRed,x+5,yDraw+h-2-barRed,barCol);
        // Spark on side bar when hover high
        if(t>0.28f){
            int sparkY = yDraw + h / 2;
            if (!motionReduced) {
                sparkY += (int)(Math.sin(System.currentTimeMillis() / 160.0) * (h / 4.2));
            }
            int sparkCol = DiaryTheme.withAlpha(DiaryTheme.AMBER_FLAME, (int)(t*165));
            g.fill(x+6,sparkY,x+8,sparkY+1,sparkCol);
            g.fill(x+6,sparkY+1,x+7,sparkY+2,DiaryTheme.withAlpha(sparkCol&0x00FFFFFF, (int)(t*80)));
        }

        // Separator
        int sep = DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 118);
        g.fill(x+36,yDraw+6,x+37,yDraw+h-6,sep);
        // Small brass screws on separator line
        g.fill(x+35,yDraw+7,x+38,yDraw+8,DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 60));
        g.fill(x+35,yDraw+h-8,x+38,yDraw+h-7,DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 60));

        // Icon with shadow + color lerped
        Font font=Minecraft.getInstance().font;
        int iconX=x+13, iconY=yDraw+10;
        int iconCol = hoverAnim.lerpColor(DiaryTheme.SEPIA_LIGHT, DiaryTheme.AMBER_FLAME);
        int iconShadow = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 150);
        drawButtonIcon(g,iconX+1,iconY+1,iconShadow,0f);
        drawButtonIcon(g,iconX,iconY,iconCol,t);

        // Text
        int textX=x+44, maxW=Math.max(20,w-54);
        this.refreshTextCache(font, maxW);
        String tTitle=this.cachedTitleText;
        String tSub=this.cachedSubtitleText;
        int titleCol = hoverAnim.lerpColor(DiaryTheme.BONE_WHITE, DiaryTheme.PAPER);
        int subCol = hoverAnim.lerpColor(DiaryTheme.SEPIA_LIGHT, DiaryTheme.OCHRE);
        if(!active){ titleCol=DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID,90); subCol=DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID,70); }
        // Shadow 1px for title
        if(active) g.drawString(font,tTitle,textX+1,yDraw+9,DiaryTheme.withAlpha(DiaryTheme.INK_BLACK,70),false);
        g.drawString(font,tTitle,textX,yDraw+8,titleCol,false);
        g.drawString(font,tSub,textX,yDraw+22,subCol,false);

        // Bottom micro line (steel wear)
        g.fill(x+44,yDraw+h-7,x+w-18,yDraw+h-6,DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 54));
        // Wear dots
        if(active){
            int wearCol = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, 28);
            if (wearX1 >= 0) g.fill(x + wearX1, yDraw + wearY1, x + wearX1 + 1, yDraw + wearY1 + 1, wearCol);
            if (wearX2 >= 0) g.fill(x + wearX2, yDraw + wearY2, x + wearX2 + 1, yDraw + wearY2 + 1, wearCol);
        }

        // Focus ring
        if(isFocused() && !isMouseOver(mouseX,mouseY) && active){
            int gold=DiaryTheme.GOLD_TARNISHED;
            g.fill(x-1,yDraw-1,x+w+1,yDraw,gold);
            g.fill(x-1,yDraw+h,x+w+1,yDraw+h+1,gold);
            g.fill(x-1,yDraw-1,x,yDraw+h+1,gold);
            g.fill(x+w,yDraw-1,x+w+1,yDraw+h+1,gold);
        }

        // Disabled diagonal
        if(!active){
            int diag=DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID,18);
            for(int dx=x+8;dx<x+w-8;dx+=12) g.fill(dx,yDraw+4,dx+1,yDraw+h-4,diag);
        }
    }

    private void drawButtonIcon(GuiGraphics g,int x,int y,int col,float hoverT){
        if(this.arsenalIcon){
            drawArsenalIcon(g,x,y,col);
        } else {
            drawRegimentIcon(g,x,y,col);
        }
        if(hoverT>0.32f){
            g.fill(x-2,y+18,x+18,y+19,DiaryTheme.withAlpha(DiaryTheme.AMBER_FLAME,(int)(hoverT*72)));
        }
    }
    private void drawRegimentIcon(GuiGraphics g,int x,int y,int col){
        for(int i=0;i<13;i++){ g.fill(x+2+i,y+14-i,x+3+i,y+15-i,col); g.fill(x+4+i,y+3+i,x+5+i,y+4+i,col); }
        g.fill(x+1,y+14,x+5,y+16,col); g.fill(x+13,y+14,x+17,y+16,col);
        g.fill(x+8,y+1,x+10,y+7,col); g.fill(x+5,y+2,x+13,y+5,DiaryTheme.withAlpha(col,140));
    }
    private void drawArsenalIcon(GuiGraphics g,int x,int y,int col){
        g.fill(x+2,y+6,x+15,y+16,col);
        g.fill(x+3,y+7,x+14,y+15,DiaryTheme.withAlpha(col,110));
        g.fill(x+2,y+6,x+15,y+7,col); g.fill(x+2,y+11,x+15,y+12,col);
        g.fill(x+8,y+7,x+9,y+16,col); g.fill(x+5,y+2,x+12,y+4,col);
        g.fill(x+6,y,x+8,y+2,col); g.fill(x+10,y,x+12,y+2,col);
    }
    private static String ellipsize(Font f,String txt,int maxW){
        if(f.width(txt)<=maxW) return txt;
        String ell="..."; int room=Math.max(0,maxW-f.width(ell));
        return f.plainSubstrByWidth(txt,room)+ell;
    }
    @Override protected void updateWidgetNarration(NarrationElementOutput out){defaultButtonNarrationText(out);}
}
