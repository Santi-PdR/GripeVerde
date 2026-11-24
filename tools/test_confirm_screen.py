#!/usr/bin/env python3
"""Run full DiaryConfirmScreen with screen/widget/lore/render doubles.

Checks callback safety, keyboard target, delay/rebuild and layout invalidation.
Does not launch Minecraft, render pixels or delete/disconnect anything.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method

ROOT = Path(__file__).resolve().parents[1]
PREFIX = 'com/eternalcraft/gripeverde/'
SOURCE = (ROOT / ('src/main/java/'+PREFIX+'client/screens/DiaryConfirmScreen.java')).read_text()
STUBS = {
    'it/unimi/dsi/fastutil/booleans/BooleanConsumer.java': 'package it.unimi.dsi.fastutil.booleans; public interface BooleanConsumer {void accept(boolean value);}',
    'org/lwjgl/glfw/GLFW.java': 'package org.lwjgl.glfw; public class GLFW {public static final int GLFW_KEY_ENTER=257,GLFW_KEY_KP_ENTER=335,GLFW_KEY_ESCAPE=256;}',
    'net/minecraft/network/chat/Component.java': '''package net.minecraft.network.chat;
public record Component(String text) { public static Component literal(String text){return new Component(text);} public String getString(){return text;} }''',
    'net/minecraft/client/gui/Font.java': '''package net.minecraft.client.gui;
public class Font {public int lineHeight=9;public int width(String text){return text.length();}}''',
    'net/minecraft/client/gui/GuiGraphics.java': '''package net.minecraft.client.gui;
public class GuiGraphics {public void drawString(Font f,String text,int x,int y,int color,boolean shadow){}}''',
    'net/minecraft/client/Minecraft.java': '''package net.minecraft.client;
import net.minecraft.client.gui.screens.Screen;
public class Minecraft {public Screen screen; public int transitions; public void setScreen(Screen screen){this.screen=screen;transitions++;}}''',
    'net/minecraft/client/gui/components/Renderable.java': '''package net.minecraft.client.gui.components;
public interface Renderable {void render(net.minecraft.client.gui.GuiGraphics g,int x,int y,float tick);}''',
    'net/minecraft/client/gui/components/AbstractWidget.java': '''package net.minecraft.client.gui.components;
import net.minecraft.network.chat.Component;
public class AbstractWidget implements Renderable {
    public boolean active=true,visible=true;public int x,y,width,height;public Component message;
    public AbstractWidget(int x,int y,int w,int h,Component label){this.x=x;this.y=y;width=w;height=h;message=label;}
    public Component getMessage(){return message;}
    public void render(net.minecraft.client.gui.GuiGraphics g,int x,int y,float tick){}
}''',
    'net/minecraft/client/gui/components/MultiLineLabel.java': '''package net.minecraft.client.gui.components;
import net.minecraft.client.gui.*; import net.minecraft.network.chat.Component;
public class MultiLineLabel {
    public static int creations,lastWidth;
    public static MultiLineLabel create(Font font,Component text,int width){creations++;lastWidth=width;return new MultiLineLabel();}
    public void renderCentered(GuiGraphics g,int x,int y,int step,int color){}
}''',
    'net/minecraft/client/gui/screens/Screen.java': '''package net.minecraft.client.gui.screens;
import java.util.*;import net.minecraft.client.gui.*;import net.minecraft.client.gui.components.*;import net.minecraft.network.chat.Component;
public class Screen {
    public int width=800,height=600;public Font font=new Font();public Component title;
    public net.minecraft.client.Minecraft minecraft=new net.minecraft.client.Minecraft();
    protected List<Renderable> renderables=new ArrayList<>();private List<AbstractWidget> children=new ArrayList<>();private Object focused;
    public Screen(Component title){this.title=title;}
    protected void init(){}
    public void rebuild(int width,int height){this.width=width;this.height=height;renderables.clear();children.clear();focused=null;init();}
    public void resize(net.minecraft.client.Minecraft mc,int width,int height){rebuild(width,height);}
    public List<AbstractWidget> children(){return children;}
    protected <T extends AbstractWidget>T addRenderableWidget(T child){renderables.add(child);children.add(child);return child;}
    public Object getFocused(){return focused;}public void setFocused(Object focused){this.focused=focused;}
    public void tick(){}
    public void render(GuiGraphics g,int x,int y,float tick){}
    public void renderBackground(GuiGraphics g){}
    public void onClose(){}
    public boolean keyPressed(int key,int scan,int mods){
        if(key==32&&focused instanceof com.eternalcraft.gripeverde.client.widgets.DiaryButton b&&b.active&&b.visible){b.onPress();return true;}
        return false;
    }
}''',
    'net/minecraft/client/gui/screens/ConfirmScreen.java': '''package net.minecraft.client.gui.screens;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;import net.minecraft.network.chat.Component;
public class ConfirmScreen extends Screen {
    protected final BooleanConsumer callback;protected Component yesButton,noButton;
    public int vanillaButtons;
    public ConfirmScreen(BooleanConsumer callback,Component title,Component message){this(callback,title,message,Component.literal("yes"),Component.literal("no"));}
    public ConfirmScreen(BooleanConsumer callback,Component title,Component message,Component yes,Component no){super(title);this.callback=callback;yesButton=yes;noButton=no;}
    protected void init(){super.init();addButtons(100);}
    protected void addButtons(int y){vanillaButtons+=2;}
    public void setDelay(int ticks){}
    public boolean keyPressed(int key,int scan,int mods){if(key==256){callback.accept(false);return true;}return super.keyPressed(key,scan,mods);}
}''',
    PREFIX+'client/widgets/DiaryButton.java': '''package com.eternalcraft.gripeverde.client.widgets;
import net.minecraft.client.gui.components.AbstractWidget;import net.minecraft.network.chat.Component;
public class DiaryButton extends AbstractWidget {
    public enum Icon{NONE,DOOR} public enum Variant{PRIMARY,SECONDARY}
    private final Runnable action;
    public DiaryButton(int x,int y,int w,int h,Component label,Icon icon,Variant variant,Runnable action){super(x,y,w,h,label);this.action=action;}
    public void onPress(){action.run();}
}''',
    PREFIX+'client/audio/UiSounds.java': '''package com.eternalcraft.gripeverde.client.audio;
public class UiSounds {public static int opens,notifications,backs,custom;
    public static void open(){opens++;}public static void notification(){notifications++;}public static void back(){backs++;}
    public static void playCustom(Object sound,float v,float p){custom++;}}
''',
    PREFIX+'client/audio/ModSounds.java': '''package com.eternalcraft.gripeverde.client.audio;
public class ModSounds {public static Value UI_CLICK_DESTROY=new Value();public static class Value{public Object get(){return this;}}}''',
    PREFIX+'client/lore/ChroniclerMemory.java': '''package com.eternalcraft.gripeverde.client.lore;
public class ChroniclerMemory {public static int destroys;public static int bump(String key){return ++destroys;}public static int counter(String key){return destroys;}}''',
    PREFIX+'client/lore/Discoveries.java': '''package com.eternalcraft.gripeverde.client.lore;
public class Discoveries {public static final String DESTROYER="destroyer";public static int calls;
    public static void discover(String id){calls++;}public static boolean isEnglish(){return true;}
    public static void drawToast(net.minecraft.client.gui.GuiGraphics g,int width){}}
''',
    PREFIX+'client/lore/Marginalia.java': '''package com.eternalcraft.gripeverde.client.lore;
public class Marginalia {public static final String N_DESTROY="destroy";public static int calls;public static void queueOnce(String id){calls++;}}''',
    PREFIX+'client/render/DiaryChrome.java': '''package com.eternalcraft.gripeverde.client.render;
import net.minecraft.client.gui.*;
public class DiaryChrome {
    public static final int INK_TITLE=1,INK_SUBTITLE=2;
    public static void drawPanelShadow(GuiGraphics g,int x,int y,int w,int h){}
    public static void drawCornerOrnaments(GuiGraphics g,int x,int y,int w,int h){}
    public static void drawRegistryTag(GuiGraphics g,int x,int y,int w,int h,String tag){}
    public static void drawInkDivider(GuiGraphics g,int x,int y,int w){}
    public static void drawDivisorLozenge(GuiGraphics g,int x,int y){}
    public static String fitCached(Font font,String text,int w){return text;}
    public static int widthCached(Font font,String text){return font.width(text);}
}''',
    PREFIX+'client/render/PaperTexture.java': '''package com.eternalcraft.gripeverde.client.render;
public class PaperTexture {public enum PaperKind{OFFICIAL}public static void drawTornSheet(net.minecraft.client.gui.GuiGraphics g,int x,int y,int w,int h,PaperKind kind){}}''',
    PREFIX+'client/render/DiaryTheme.java': 'package com.eternalcraft.gripeverde.client.render; public class DiaryTheme {public static int withAlpha(int color,int alpha){return color;}}',
    PREFIX+'client/render/QuarantineSceneRenderer.java': '''package com.eternalcraft.gripeverde.client.render;
public class QuarantineSceneRenderer {public static void tick(float delta){}public static void renderDiaryBackground(net.minecraft.client.gui.GuiGraphics g,int w,int h){}}''',
}
HARNESS = r'''import com.eternalcraft.gripeverde.client.screens.DiaryConfirmScreen;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.lore.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public class ConfirmHarness {
    static void check(boolean ok,String reason){if(!ok)throw new AssertionError(reason);}
    static DiaryButton yes(DiaryConfirmScreen screen){return (DiaryButton)screen.children().get(0);}
    static DiaryButton no(DiaryConfirmScreen screen){return (DiaryButton)screen.children().get(1);}
    static int calls;static boolean decision;
    static void callback(boolean accepted){calls++;decision=accepted;}
    static void cancelled(){check(calls==1&&!decision,"expected one cancellation");check(ChroniclerMemory.destroys==0&&UiSounds.custom==0,"cancel recorded destruction");}
    static void accepted(){check(calls==1&&decision,"expected one acceptance");check(ChroniclerMemory.destroys==1&&UiSounds.custom==1,"acceptance repeated/lost lore");}
    public static void main(String[] args){
        String test=args[0];Screen parent=new Screen(Component.literal("parent"));
        DiaryConfirmScreen screen=new DiaryConfirmScreen(ConfirmHarness::callback,Component.literal("title"),Component.literal("long message"),parent);
        screen.minecraft.screen=screen;
        if(test.equals("delay_before_init"))screen.setDelay(2);
        screen.rebuild(800,600);
        switch(test){
            case "two_owned_buttons" -> {check(screen.children().size()==2&&screen.vanillaButtons==0,"hidden duplicate buttons created");}
            case "enter_default", "keypad_default" -> {screen.keyPressed(test.equals("keypad_default")?335:257,0,0);accepted();}
            case "enter_focused_no", "keypad_focused_no", "space_focused_no" -> {
                screen.setFocused(no(screen));screen.keyPressed(test.equals("space_focused_no")?32:test.equals("keypad_focused_no")?335:257,0,0);cancelled();
            }
            case "equal_labels" -> {
                screen=new DiaryConfirmScreen(ConfirmHarness::callback,Component.literal("title"),Component.literal("message"),Component.literal("same"),Component.literal("same"),parent);
                screen.rebuild(800,600);screen.setFocused(no(screen));screen.keyPressed(257,0,0);cancelled();
            }
            case "hidden_target", "disabled_target" -> {
                screen.setFocused(no(screen));if(test.equals("hidden_target"))no(screen).visible=false;else no(screen).active=false;
                screen.keyPressed(257,0,0);check(calls==0,"invalid focused No fell through to Yes");
            }
            case "escape", "close", "escape_during_delay" -> {
                if(test.equals("escape_during_delay"))screen.setDelay(10);
                if(test.equals("close"))screen.onClose();else screen.keyPressed(256,0,0);
                cancelled();check(screen.minecraft.screen==parent&&UiSounds.backs==1,"close fallback/back failed");
                screen.onClose();screen.keyPressed(257,0,0);cancelled();
            }
            case "double_accept", "accept_then_close" -> {
                DiaryButton old=yes(screen);old.onPress();
                if(test.equals("accept_then_close"))screen.onClose();else old.onPress();
                screen.keyPressed(335,0,0);accepted();check(!yes(screen).active&&!no(screen).active,"decision did not disable buttons");
            }
            case "double_cancel" -> {DiaryButton old=no(screen);old.onPress();old.onPress();yes(screen).onPress();cancelled();}
            case "reentrant_accept", "reentrant_cancel" -> {
                final DiaryConfirmScreen[] holder={null};
                holder[0]=new DiaryConfirmScreen(value->{callback(value);holder[0].onClose();holder[0].keyPressed(257,0,0);},Component.literal("title"),Component.literal("message"),parent);
                holder[0].rebuild(800,600);
                if(test.equals("reentrant_accept")){yes(holder[0]).onPress();accepted();}else{holder[0].onClose();cancelled();}
            }
            case "callback_navigation" -> {
                final DiaryConfirmScreen[] holder={null};Screen destination=new Screen(Component.literal("other"));
                holder[0]=new DiaryConfirmScreen(value->{callback(value);holder[0].minecraft.setScreen(destination);},Component.literal("title"),Component.literal("message"),parent);
                holder[0].rebuild(800,600);holder[0].minecraft.screen=holder[0];holder[0].onClose();cancelled();
                check(holder[0].minecraft.screen==destination,"close overwrote callback destination");
            }
            case "callback_throws_once" -> {
                final DiaryConfirmScreen failing=new DiaryConfirmScreen(value->{callback(value);throw new IllegalStateException("callback failed");},Component.literal("title"),Component.literal("message"),parent);
                failing.rebuild(800,600);try{yes(failing).onPress();throw new AssertionError("callback failure swallowed");}catch(IllegalStateException expected){}
                yes(failing).onPress();failing.onClose();accepted();
            }
            case "delay_before_init", "delay_after_init" -> {
                if(test.equals("delay_after_init"))screen.setDelay(2);
                check(!yes(screen).active&&!no(screen).active,"delay did not disable owned buttons");
                screen.keyPressed(257,0,0);yes(screen).onPress();check(calls==0,"delay bypassed");
                screen.tick();check(!yes(screen).active,"delay ended one tick early");screen.tick();
                check(yes(screen).active&&no(screen).active,"delay did not expire");screen.keyPressed(257,0,0);accepted();
            }
            case "delay_survives_resize" -> {
                screen.setDelay(3);screen.tick();screen.rebuild(320,180);
                check(!yes(screen).active,"resize bypassed delay");screen.tick();check(!yes(screen).active,"resize shortened delay");screen.tick();
                check(yes(screen).active,"resize restarted delay");
            }
            case "zero_negative_delay" -> {screen.setDelay(4);screen.setDelay(0);check(yes(screen).active,"zero delay did not enable");screen.setDelay(-1);check(no(screen).active,"negative delay stuck");}
            case "resolved_survives_resize" -> {yes(screen).onPress();screen.rebuild(320,180);screen.setDelay(0);check(!yes(screen).active&&!no(screen).active,"resize/delay revived completed decision");yes(screen).onPress();accepted();}
            case "reflow_on_resize" -> {
                GuiGraphics g=new GuiGraphics();screen.render(g,0,0,0);int oldWidth=MultiLineLabel.lastWidth,creates=MultiLineLabel.creations;
                screen.render(g,0,0,0);check(MultiLineLabel.creations==creates,"unchanged message rewrapped each frame");
                screen.rebuild(240,180);screen.render(g,0,0,0);
                check(MultiLineLabel.creations==creates+1&&MultiLineLabel.lastWidth<oldWidth,"message cache survived resize");
            }
            case "resize_focus_no", "resize_focus_yes" -> {
                screen.setFocused(test.endsWith("no")?no(screen):yes(screen));
                screen.resize(screen.minecraft,320,180);
                check(screen.getFocused()==(test.endsWith("no")?no(screen):yes(screen)),"resize lost decision focus");
                screen.keyPressed(257,0,0);if(test.endsWith("no"))cancelled();else accepted();
            }
            case "notification_once" -> {screen.rebuild(320,180);screen.rebuild(800,600);check(UiSounds.opens==1&&UiSounds.notifications==1,"resize repeated opening feedback");}
            case "compact_layout" -> {
                screen.rebuild(160,120);DiaryButton a=yes(screen),b=no(screen);
                check(a.x>=0&&a.y>=0&&b.x+b.width<=160&&b.y+b.height<=120,"compact controls left window");
                check(a.x+a.width<b.x,"compact controls overlap");
            }
            default -> throw new AssertionError(test);
        }
    }
}
'''
CASES = ('two_owned_buttons', 'enter_default', 'keypad_default', 'enter_focused_no', 'keypad_focused_no',
         'space_focused_no', 'equal_labels', 'hidden_target', 'disabled_target', 'escape', 'close',
         'escape_during_delay', 'double_accept', 'accept_then_close', 'double_cancel', 'reentrant_accept',
         'reentrant_cancel', 'callback_navigation', 'callback_throws_once', 'delay_before_init',
         'delay_after_init', 'delay_survives_resize', 'zero_negative_delay', 'resolved_survives_resize',
         'reflow_on_resize', 'notification_once', 'compact_layout', 'resize_focus_no', 'resize_focus_yes')


class Wiring(unittest.TestCase):
    def test_supported_button_hook_no_reflection(self):
        self.assertIn('protected void addButtons(', SOURCE)
        self.assertNotIn('getDeclaredField', SOURCE)
        self.assertNotIn('getMessage().getString().equals', SOURCE)

    def test_latch_precedes_side_effects(self):
        body = method(SOURCE, 'private void finishDecision(')
        self.assertLess(body.index('this.decisionMade = true'), body.index('ChroniclerMemory.bump('))
        self.assertLess(body.index('this.decisionMade = true'), body.index('this.callback.accept('))

    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_confirm_screen.py --require-java', (ROOT / path).read_text())


class Executable(unittest.TestCase):
    def test_complete_confirmation_class(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        with tempfile.TemporaryDirectory() as tmp:
            sources = dict(STUBS)
            sources[PREFIX+'client/screens/DiaryConfirmScreen.java'] = SOURCE
            sources['ConfirmHarness.java'] = HARNESS
            paths = []
            for name, text in sources.items():
                path = Path(tmp)/name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            for case in CASES:
                with self.subTest(case=case):
                    subprocess.run(['java', '-cp', tmp, 'ConfirmHarness', case], check=True, timeout=15)
            print(f'Confirmation: {len(CASES)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
