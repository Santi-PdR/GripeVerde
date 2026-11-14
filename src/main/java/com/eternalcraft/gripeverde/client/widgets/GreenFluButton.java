package com.eternalcraft.gripeverde.client.widgets;

import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.render.DiaryTypography;
import com.eternalcraft.gripeverde.client.util.EasedValue;
import com.eternalcraft.gripeverde.client.util.MotionPolicy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GreenFluButton extends AbstractButton {

    private final Component title;
    private final Component subtitle;
    private final ResourceLocation iconTexture;
    private final Runnable onClick;
    private boolean wasHovered = false;
    private final EasedValue hoverAnim = new EasedValue(5.5f);
    private int pressTicks = 0;
    private final int wearSeed;
    private final java.util.Random wearRng;

    public GreenFluButton(int x,int y,int w,int h, Component title, Component subtitle, ResourceLocation icon, Runnable onClick){
        super(x,y,w,h,title);
        this.title=title; this.subtitle=subtitle; this.iconTexture=icon; this.onClick=onClick;
        this.wearSeed = (x*29 + y*19 + title.getString().hashCode()) & 0x7fffffff;
        this.wearRng = new java.util.Random(wearSeed);
    }

    @Override public void onPress(){ UiSounds.playClick(); onClick.run(); }
    @Override public void playDownSound(SoundManager sm){}
    @Override public boolean mouseClicked(double mx,double my,int btn){
        if(!MotionPolicy.isReduced()&&active&&visible&&clicked(mx,my)) pressTicks=7;
        return super.mouseClicked(mx,my,btn);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick){
        boolean hovered=isHovered();
        if(hovered&&!wasHovered) UiSounds.playHover();
        wasHovered=hovered;
        boolean motionDisabled = MotionPolicy.isReduced();
        if (motionDisabled) hoverAnim.snapTo(hovered);
        else {
            hoverAnim.setTarget(hovered);
            hoverAnim.tick(partialTick/20f);
        }
        float t=hoverAnim.getEased();
        boolean pressed=!motionDisabled&&pressTicks>0&&active;
        if (motionDisabled) pressTicks=0;
        else if(pressTicks>0) pressTicks--;

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
            int sparkY = motionDisabled ? yDraw+h/2 : yDraw+h/2 + (int)(Math.sin(System.currentTimeMillis()/160.0)*(h/4.2));
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
        String tTitle=ellipsize(font,title.getString(),maxW);
        String tSub=ellipsize(font,subtitle.getString(),maxW);
        int titleCol = hoverAnim.lerpColor(DiaryTheme.BONE_WHITE, DiaryTheme.PAPER);
        int subCol = hoverAnim.lerpColor(DiaryTheme.SEPIA_LIGHT, DiaryTheme.FIELD_SUBTITLE);
        if(!active){ titleCol=DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID,90); subCol=DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID,70); }
        DiaryTypography.draw(g, font, tTitle, textX, yDraw + 8, titleCol);
        DiaryTypography.draw(g, font, tSub, textX, yDraw + 22, subCol);

        // Bottom micro line (steel wear)
        g.fill(x+44,yDraw+h-7,x+w-18,yDraw+h-6,DiaryTheme.withAlpha(DiaryTheme.BRONZE_DARK, 54));
        // Wear dots
        if(active){
            wearRng.setSeed(wearSeed);
            int wearCol = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, 28);
            for(int i=0;i<2;i++){
                if(wearRng.nextInt(100)<40){
                    int wx=x+48+wearRng.nextInt(Math.max(1,w-66));
                    int wy=yDraw+8+wearRng.nextInt(Math.max(1,h-16));
                    g.fill(wx,wy,wx+1,wy+1,wearCol);
                }
            }
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
        String path = iconTexture==null?"":iconTexture.getPath();
        if(path.contains("mods")){
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
        return DiaryTypography.fitString(f, txt, maxW);
    }
    @Override protected void updateWidgetNarration(NarrationElementOutput out){defaultButtonNarrationText(out);}
}
