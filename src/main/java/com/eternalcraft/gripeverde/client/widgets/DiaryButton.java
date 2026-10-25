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
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * DiaryButton v3.0 - Extreme Polish
 * Cada estado tiene identidad: normal / hover / pressed / disabled / focus
 * Detalles: sombra expulsada, highlight papel, sombra interna arruga, pinholes, wear, rivets, underline tinta.
 */
@OnlyIn(Dist.CLIENT)
public class DiaryButton extends AbstractButton {

    public enum Variant { PRIMARY, SECONDARY, DANGER }
    public enum SoundProfile { DEFAULT, CLEAN, OPTION_OPEN, BACK, NONE }
    public enum Icon { NONE, HAND, SCROLL, CHART, GEAR, DOOR, PALETTE, SOUND, EYE, CONTROLS, LANGUAGE, CHAT, PACK, ACCESS, ONLINE, CREDITS }

    private final Runnable onClick;
    private final Icon icon;
    private final Variant variant;
    private final SoundProfile soundProfile;
    private boolean allowEllipsis = true;
    private final EasedValue hoverAnim = new EasedValue(4.5f);
    private boolean wasHovered = false;
    private int pressAnimTicks = 0;
    private final int wearSeed;
    private final java.util.Random wearRng;

    public DiaryButton(int x, int y, int w, int h, Component label, Icon icon, Variant variant, Runnable onClick) {
        this(x,y,w,h,label,icon,variant,SoundProfile.DEFAULT,onClick);
    }
    public DiaryButton(int x, int y, int w, int h, Component label, Icon icon, Variant variant, SoundProfile sp, Runnable onClick) {
        super(x,y,w,h,label);
        this.icon = icon!=null?icon:Icon.NONE;
        this.variant = variant!=null?variant:Variant.SECONDARY;
        this.soundProfile = sp!=null?sp:SoundProfile.DEFAULT;
        this.onClick = onClick;
        this.wearSeed = (x*31 + y*17 + w*13 + h*7) ^ label.getString().hashCode();
        this.wearRng = new java.util.Random(wearSeed);
    }
    public DiaryButton(int x,int y,int w,int h,Component label,Runnable onClick){this(x,y,w,h,label,Icon.NONE,Variant.SECONDARY,onClick);}
    public DiaryButton noEllipsis(){this.allowEllipsis=false;return this;}

    @Override public void onPress(){ if(onClick!=null) onClick.run(); }
    @Override public void playDownSound(SoundManager sm){
        switch(soundProfile){
            case CLEAN -> UiSounds.cleanClick();
            case OPTION_OPEN -> UiSounds.optionOpen();
            case BACK -> UiSounds.back();
            case NONE -> {}
            default -> UiSounds.playClick();
        }
    }
    @Override public boolean mouseClicked(double mx,double my,int btn){
        if(active&&visible&&clicked(mx,my)) pressAnimTicks=7;
        return super.mouseClicked(mx,my,btn);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick){
        int x=getX(), y=getY(), w=width, h=height;
        Minecraft mc = Minecraft.getInstance();
        boolean inWorld = mc != null && mc.level != null;
        boolean hovered = isHoveredOrFocused() && active;
        boolean pressed = !inWorld && pressAnimTicks>0 && active;
        if(inWorld) pressAnimTicks=0;
        else if(pressAnimTicks>0) pressAnimTicks--;

        if(hovered && !wasHovered) UiSounds.playHover();
        wasHovered=hovered;

        float t;
        if(inWorld) {
            // No easing during a live session: hover feedback changes in one frame.
            t = hovered ? 1f : 0f;
        } else {
            hoverAnim.setTarget(hovered);
            hoverAnim.tick(partialTick/20f);
            t = hoverAnim.getEased();
        }

        int pressedOffset = pressed?1:0;
        int yDraw = y+pressedOffset;

        // ── Shadows ──
        if(!active){
            // Disabled: no shadow, flat
        } else if(!pressed){
            int outer = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 90);
            int soft = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 38);
            g.fill(x+1,yDraw+h,x+w,yDraw+h+1,outer);
            g.fill(x+2,yDraw+h+1,x+w+1,yDraw+h+2,soft);
        } else {
            int outer = DiaryTheme.withAlpha(DiaryTheme.INK_BLACK, 48);
            g.fill(x+1,yDraw+h,x+w-1,yDraw+h+1,outer);
        }

        // ── Background with micro variation ──
        int basePaper = DiaryTheme.PAPER_STAINED;
        // Slight per-instance tint variation from wearSeed to avoid uniform template
        int tintShift = (Math.abs(wearSeed)%7)-3; // -3..+3
        int bgBase = shiftColor(basePaper, tintShift);
        int bgHover = 0xFFB89A6E;
        int bgPressed = 0xFFA08254;
        int bgDisabled = DiaryTheme.withAlpha(0xFFD8CAB0, 210);
        int bg;
        if(!active) bg = bgDisabled;
        else if(pressed) bg = bgPressed;
        else bg = inWorld ? (hovered ? bgHover : bgBase) : hoverAnim.lerpColor(bgBase, bgHover);
        g.fill(x,yDraw,x+w,yDraw+h,bg);

        // Paper fiber highlight top 1px
        int topHl = DiaryTheme.withAlpha(0x00FFEEDD, active?84:40);
        g.fill(x+1,yDraw+1,x+w-1,yDraw+2,topHl);

        // Inner shadows for depth
        if(active){
            int innerTop = DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, pressed?88:58);
            int innerBot = DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, pressed?118:88);
            g.fill(x,yDraw,x+w,yDraw+1,innerTop);
            g.fill(x,yDraw+h-2,x+w,yDraw+h-1,innerBot);
            if(pressed){
                // Pressed has stronger inner bevel top
                g.fill(x+1,yDraw+1,x+w-1,yDraw+2,DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK,68));
            }
        } else {
            // Disabled inner flat
            g.fill(x,yDraw,x+w,yDraw+1,DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID,28));
        }

        // ── Border with variant accent ──
        int accent = getVariantAccent();
        int borderCol;
        if(!active) borderCol = DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 90);
        else borderCol = inWorld ? (hovered ? accent : DiaryTheme.SEPIA_DARK)
                : hoverAnim.lerpColor(DiaryTheme.SEPIA_DARK, accent);
        drawBorder(g,x,yDraw,w,h,borderCol);

        // Double inner border for primary
        if(variant==Variant.PRIMARY && active){
            int inner = DiaryTheme.withAlpha(accent, pressed?70:45);
            g.fill(x+2,yDraw+2,x+w-2,yDraw+3,inner);
            g.fill(x+2,yDraw+h-3,x+w-2,yDraw+h-2,inner);
        }

        // Rivets / pinholes
        int pin = DiaryTheme.withAlpha(DiaryTheme.SEPIA_DARK, active?88:42);
        g.fill(x+3,yDraw+3,x+5,yDraw+4,pin);
        g.fill(x+w-5,yDraw+3,x+w-3,yDraw+4,pin);
        g.fill(x+3,yDraw+h-4,x+5,yDraw+h-3,pin);
        g.fill(x+w-5,yDraw+h-4,x+w-3,yDraw+h-3,pin);
        // Wear micro dots near edges (1px) based on seed
        if(active && !inWorld){
            int wear = DiaryTheme.withAlpha(DiaryTheme.PAPER_DARK, 34);
            wearRng.setSeed(wearSeed);
            for(int i=0;i<2;i++){
                int wx = x+6+wearRng.nextInt(Math.max(1,w-12));
                int wy = yDraw+4+wearRng.nextInt(Math.max(1,h-8));
                if(wearRng.nextInt(100)<32) g.fill(wx,wy,wx+1,wy+1,wear);
            }
        }

        // Focus ring (keyboard)
        if(isFocused() && !isMouseOver(mouseX,mouseY) && active){
            int focusGold = 0xFFD0A04A;
            int focusSoft = DiaryTheme.withAlpha(focusGold, 70);
            g.fill(x-1,yDraw-1,x+w+1,yDraw,focusGold);
            g.fill(x-1,yDraw+h,x+w+1,yDraw+h+1,focusGold);
            g.fill(x-1,yDraw-1,x,yDraw+h+1,focusGold);
            g.fill(x+w,yDraw-1,x+w+1,yDraw+h+1,focusGold);
            // Outer soft glow
            g.fill(x-2,yDraw-2,x+w+2,yDraw-1,focusSoft);
            g.fill(x-2,yDraw+h+1,x+w+2,yDraw+h+2,focusSoft);
        }

        // ── Content ──
        Font font = mc.font;
        String text = getMessage().getString();
        int textColor = getTextColor(t);
        if(!active) textColor = DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 110);

        int textStartX, maxTextW;
        int contentY = yDraw + (h - font.lineHeight)/2;
        if(icon!=Icon.NONE){
            int iconX = x+9;
            int iconY = yDraw + (h-10)/2;
            drawIcon(g,iconX,iconY,textColor);
            textStartX = iconX+12+6;
            maxTextW = Math.max(8, x+w-10-textStartX);
        } else {
            maxTextW = Math.max(8,w-18);
            text = fitText(font,text,maxTextW);
            int tw = font.width(text);
            textStartX = x+(w-tw)/2;
        }
        text = fitText(font,text,maxTextW);
        // Text shadow 1px for active (ink bleed)
        if(active){
            g.drawString(font,text,textStartX+1,contentY+1,DiaryTheme.withAlpha(DiaryTheme.INK_BLACK,40),false);
        }
        g.drawString(font,text,textStartX,contentY,textColor,false);

        // Underline on hover (ink stroke)
        if(!inWorld && t>0.08f && active){
            int tw = font.width(text);
            int uy = contentY+font.lineHeight;
            int uAlpha = (int)(t*190);
            int uCol = DiaryTheme.withAlpha(accent, uAlpha);
            // Slight wavy underline to feel hand-drawn
            for(int i=0;i<tw;i++){
                int yOff = (i%3==0)?0: (i%3==1)?1:0;
                g.fill(textStartX+i, uy+yOff, textStartX+i+1, uy+yOff+1, uCol);
            }
        }

        // Disabled crosshatch faint
        if(!active && !inWorld){
            int cross = DiaryTheme.withAlpha(DiaryTheme.SEPIA_MID, 22);
            for(int cx=x+4;cx<x+w-4;cx+=8){
                g.fill(cx,yDraw+4,cx+1,yDraw+h-4,cross);
            }
        }
    }

    private String fitText(Font f,String txt,int maxW){
        if(f.width(txt)<=maxW) return txt;
        if(!allowEllipsis) return f.plainSubstrByWidth(txt, Math.max(0,maxW));
        String ell="...";
        int room=Math.max(0,maxW-f.width(ell));
        return f.plainSubstrByWidth(txt,room)+ell;
    }
    private int shiftColor(int col,int shift){
        int a=(col>>>24)&0xFF;
        int r=Math.max(0,Math.min(255,((col>>>16)&0xFF)+shift));
        int g=Math.max(0,Math.min(255,((col>>>8)&0xFF)+shift));
        int b=Math.max(0,Math.min(255,(col&0xFF)+shift));
        return (a<<24)|(r<<16)|(g<<8)|b;
    }
    private int getVariantAccent(){
        return switch(variant){
            case PRIMARY -> DiaryTheme.REDCOAT;
            case DANGER -> DiaryTheme.REDCOAT_DARK;
            default -> DiaryTheme.INK;
        };
    }
    private int getTextColor(float hoverT){
        int base, hov;
        switch(variant){
            case PRIMARY -> { base=DiaryTheme.REDCOAT_DARK; hov=DiaryTheme.REDCOAT_BRIGHT; }
            case DANGER -> { base=DiaryTheme.REDCOAT_DARK; hov=DiaryTheme.REDCOAT; }
            default -> { base=DiaryTheme.INK; hov=DiaryTheme.INK_BLACK; }
        }
        Minecraft mc = Minecraft.getInstance();
        if(mc != null && mc.level != null) return hoverT >= 0.5f ? hov : base;
        return hoverAnim.lerpColor(base,hov);
    }
    private void drawBorder(GuiGraphics g,int x,int y,int w,int h,int col){
        g.fill(x,y,x+w,y+1,col);
        g.fill(x,y+h-1,x+w,y+h,col);
        g.fill(x,y,x+1,y+h,col);
        g.fill(x+w-1,y,x+w,y+h,col);
    }

    // ── Icons: hand-drawn engraved style ──
    private void drawIcon(GuiGraphics g,int x,int y,int col){
        switch(icon){
            case HAND -> drawHand(g,x,y,col);
            case SCROLL -> drawScroll(g,x,y,col);
            case CHART -> drawChart(g,x,y,col);
            case GEAR -> drawGear(g,x,y,col);
            case DOOR -> drawDoor(g,x,y,col);
            case PALETTE -> drawPalette(g,x,y,col);
            case SOUND -> drawSound(g,x,y,col);
            case EYE -> drawEye(g,x,y,col);
            case CONTROLS -> drawControls(g,x,y,col);
            case LANGUAGE -> drawLanguage(g,x,y,col);
            case CHAT -> drawChat(g,x,y,col);
            case PACK -> drawPack(g,x,y,col);
            case ACCESS -> drawAccess(g,x,y,col);
            case ONLINE -> drawOnline(g,x,y,col);
            case CREDITS -> drawCredits(g,x,y,col);
            default -> {}
        }
    }
    private void drawHand(GuiGraphics g,int x,int y,int c){ g.fill(x+4,y+3,x+9,y+8,c); g.fill(x+9,y+4,x+12,y+6,c); g.fill(x+11,y+3,x+12,y+7,c); g.fill(x,y+4,x+4,y+7,c); }
    private void drawScroll(GuiGraphics g,int x,int y,int c){ g.fill(x,y+2,x+2,y+8,c); g.fill(x+1,y+1,x+3,y+9,c); g.fill(x+2,y+3,x+10,y+7,DiaryTheme.withAlpha(c,180)); g.fill(x+3,y+4,x+9,y+5,c); g.fill(x+3,y+6,x+8,y+7,c); g.fill(x+10,y+2,x+12,y+8,c); g.fill(x+9,y+1,x+11,y+9,c); }
    private void drawChart(GuiGraphics g,int x,int y,int c){ g.fill(x+1,y+6,x+3,y+9,c); g.fill(x+4,y+3,x+6,y+9,c); g.fill(x+7,y+5,x+9,y+9,c); g.fill(x+10,y+2,x+12,y+9,c); g.fill(x,y+9,x+12,y+10,c); }
    private void drawGear(GuiGraphics g,int x,int y,int c){ g.fill(x+3,y+2,x+9,y+8,c); g.fill(x+4,y,x+5,y+2,c); g.fill(x+7,y,x+8,y+2,c); g.fill(x+4,y+8,x+5,y+10,c); g.fill(x+7,y+8,x+8,y+10,c); g.fill(x,y+4,x+3,y+5,c); g.fill(x,y+6,x+3,y+7,c); g.fill(x+9,y+4,x+12,y+5,c); g.fill(x+9,y+6,x+12,y+7,c); g.fill(x+5,y+4,x+7,y+6,DiaryTheme.PAPER); }
    private void drawDoor(GuiGraphics g,int x,int y,int c){ g.fill(x+2,y+3,x+10,y+10,c); g.fill(x+3,y+1,x+9,y+3,c); g.fill(x+4,y,x+8,y+1,c); g.fill(x+4,y+4,x+8,y+9,DiaryTheme.withAlpha(c,100)); g.fill(x+8,y+6,x+9,y+7,c); }
    private void drawPalette(GuiGraphics g,int x,int y,int c){ g.fill(x+2,y+2,x+10,y+8,c); g.fill(x+4,y,x+8,y+2,c); g.fill(x+8,y+6,x+12,y+9,c); g.fill(x+4,y+4,x+6,y+6,DiaryTheme.PAPER); }
    private void drawSound(GuiGraphics g,int x,int y,int c){ g.fill(x+1,y+4,x+4,y+7,c); g.fill(x+4,y+2,x+7,y+9,c); g.fill(x+8,y+3,x+9,y+8,c); g.fill(x+10,y+1,x+11,y+10,DiaryTheme.withAlpha(c,150)); }
    private void drawEye(GuiGraphics g,int x,int y,int c){ g.fill(x+1,y+4,x+11,y+6,c); g.fill(x+3,y+2,x+9,y+8,DiaryTheme.withAlpha(c,120)); g.fill(x+5,y+4,x+7,y+6,DiaryTheme.INK_BLACK); }
    private void drawControls(GuiGraphics g,int x,int y,int c){ g.fill(x+1,y+3,x+11,y+8,c); g.fill(x+2,y+2,x+5,y+3,c); g.fill(x+7,y+2,x+10,y+3,c); g.fill(x+3,y+5,x+5,y+7,DiaryTheme.PAPER); g.fill(x+8,y+5,x+10,y+7,DiaryTheme.PAPER); }
    private void drawLanguage(GuiGraphics g,int x,int y,int c){ g.fill(x+1,y+2,x+11,y+9,c); g.fill(x+2,y+3,x+10,y+4,DiaryTheme.PAPER); g.fill(x+3,y+5,x+8,y+6,DiaryTheme.PAPER); g.fill(x+3,y+7,x+9,y+8,DiaryTheme.withAlpha(DiaryTheme.PAPER,180)); }
    private void drawChat(GuiGraphics g,int x,int y,int c){ g.fill(x+1,y+2,x+11,y+8,c); g.fill(x+3,y+8,x+5,y+10,c); g.fill(x+3,y+4,x+9,y+5,DiaryTheme.PAPER); g.fill(x+3,y+6,x+7,y+7,DiaryTheme.PAPER); }
    private void drawPack(GuiGraphics g,int x,int y,int c){ g.fill(x+2,y+2,x+10,y+9,c); g.fill(x+1,y+4,x+9,y+10,DiaryTheme.withAlpha(c,130)); g.fill(x+4,y+3,x+8,y+4,DiaryTheme.PAPER); }
    private void drawAccess(GuiGraphics g,int x,int y,int c){ g.fill(x+5,y+1,x+7,y+3,c); g.fill(x+2,y+4,x+10,y+5,c); g.fill(x+5,y+5,x+7,y+10,c); g.fill(x+3,y+8,x+5,y+10,c); g.fill(x+7,y+8,x+9,y+10,c); }
    private void drawOnline(GuiGraphics g,int x,int y,int c){ g.fill(x+5,y+1,x+7,y+10,c); g.fill(x+1,y+4,x+11,y+6,c); g.fill(x+2,y+2,x+10,y+3,DiaryTheme.withAlpha(c,120)); g.fill(x+2,y+7,x+10,y+8,DiaryTheme.withAlpha(c,120)); }
    private void drawCredits(GuiGraphics g,int x,int y,int c){ g.fill(x+3,y+1,x+9,y+9,c); g.fill(x+2,y+2,x+10,y+8,DiaryTheme.withAlpha(c,100)); g.fill(x+5,y+3,x+7,y+6,DiaryTheme.PAPER); g.fill(x+5,y+7,x+7,y+8,DiaryTheme.PAPER); }

    @Override protected void updateWidgetNarration(NarrationElementOutput out){defaultButtonNarrationText(out);}
}
