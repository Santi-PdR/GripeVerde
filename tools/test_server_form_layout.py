#!/usr/bin/env python3
"""Compile both complete form classes and record widget/text geometry.

Uses GUI/font/services doubles, not Minecraft rendering, actual glyphs or OS
input. Sweep includes all height breakpoints and the supported 160x120 minimum.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_confirm_screen import STUBS as GUI_STUBS
from test_server_forms import STUBS as SERVICE_STUBS, SOURCES, PREFIX

ROOT = Path(__file__).resolve().parents[1]
STUBS = dict(GUI_STUBS)
STUBS.update(SERVICE_STUBS)
STUBS.update({
    'net/minecraft/client/gui/Font.java': '''package net.minecraft.client.gui;
public class Font {public int lineHeight=9;public int width(String text){return text.length()*6;}}''',
    'net/minecraft/client/gui/GuiGraphics.java': '''package net.minecraft.client.gui;
import java.util.*;
public class GuiGraphics {
    public record Text(String value,int x,int y,int width,int height,int color){}
    public List<Text> texts=new ArrayList<>();
    public void drawString(Font font,String text,int x,int y,int color,boolean shadow){if(!text.isEmpty())texts.add(new Text(text,x,y,font.width(text),font.lineHeight,color));}
    public void fill(int x,int y,int right,int bottom,int color){}
}''',
    'net/minecraft/client/gui/screens/Screen.java': '''package net.minecraft.client.gui.screens;
import java.util.*;import net.minecraft.client.gui.*;import net.minecraft.client.gui.components.*;import net.minecraft.network.chat.Component;
public class Screen {
    public int width=800,height=600;public Font font=new Font();public Component title;
    public net.minecraft.client.Minecraft minecraft=new net.minecraft.client.Minecraft();
    private List<AbstractWidget> children=new ArrayList<>();private Object focused;
    public Screen(Component title){this.title=title;}
    protected void init(){}
    public void rebuild(int w,int h){width=w;height=h;children.clear();focused=null;init();}
    public void resize(net.minecraft.client.Minecraft mc,int w,int h){rebuild(w,h);}
    public List<AbstractWidget> children(){return children;}
    protected <T extends AbstractWidget>T addRenderableWidget(T child){children.add(child);return child;}
    public Object getFocused(){return focused;}public void setFocused(Object focus){focused=focus;}
    public void setInitialFocus(AbstractWidget focus){setFocused(focus);}
    public void tick(){}
    public void render(GuiGraphics g,int x,int y,float partial){}
    public void renderBackground(GuiGraphics g){}
    public void onClose(){}
    public boolean keyPressed(int key,int scan,int mods){return false;}
    public boolean charTyped(char c,int mods){return false;}
    public boolean mouseClicked(double x,double y,int button){return false;}
}''',
    'net/minecraft/client/multiplayer/ServerData.java': '''package net.minecraft.client.multiplayer;
public class ServerData {public String name,ip;public ServerData(String name,String ip,boolean lan){this.name=name;this.ip=ip;}}''',
    PREFIX+'client/widgets/DiaryButton.java': '''package com.eternalcraft.gripeverde.client.widgets;
import net.minecraft.client.gui.components.*;import net.minecraft.network.chat.Component;
public class DiaryButton extends AbstractWidget {
    public enum Icon{SCROLL,DOOR,HAND} public enum Variant{PRIMARY,SECONDARY}
    private final Runnable action;
    public DiaryButton(int x,int y,int w,int h,Component label,Icon icon,Variant variant,Runnable action){super(x,y,w,h,label);this.action=action;}
    public void onPress(){action.run();}public void setTooltip(Tooltip tooltip){}
}''',
    PREFIX+'client/widgets/DiaryTextField.java': '''package com.eternalcraft.gripeverde.client.widgets;
import java.util.function.Consumer;import net.minecraft.client.gui.*;import net.minecraft.client.gui.components.*;import net.minecraft.network.chat.Component;
public class DiaryTextField extends AbstractWidget {
    private String value="";private Consumer<String> responder;
    public DiaryTextField(Font font,int x,int y,int w,int h,Component label){super(x,y,w,h,label);}
    public String getValue(){return value;}public void setValue(String s){value=s;if(responder!=null)responder.accept(s);}
    public void setResponder(Consumer<String> responder){this.responder=responder;}public void setMaxLength(int max){}
    public void setFocused(boolean focus){}public void tick(){}
    public record EditingState(String value){}
    public EditingState snapshotEditingState(){return new EditingState(value);}
    public void restoreEditingState(EditingState state){if(state!=null)setValue(state.value());}
}''',
    PREFIX+'client/audio/UiSounds.java': '''package com.eternalcraft.gripeverde.client.audio;
public class UiSounds {public static void open(){}public static void back(){}public static void type(){}public static void playCustom(Object sound,float v,float p){}}''',
    PREFIX+'client/lore/Discoveries.java': '''package com.eternalcraft.gripeverde.client.lore;
public class Discoveries {public static final String ADDRESS_13="13";public static boolean english=true;
    public static boolean isEnglish(){return english;}public static boolean discover(String id){return true;}
    public static void drawToast(net.minecraft.client.gui.GuiGraphics g,int width){}
}''',
    PREFIX+'client/integration/RecentAddresses.java': '''package com.eternalcraft.gripeverde.client.integration;
import java.util.*;public class RecentAddresses {
    public static void push(String ip){}public static List<String> list(){return List.of("recent.example.org:25565","another.example.org","long.recent.address.example.org");}
}''',
    PREFIX+'client/render/QuarantineSceneRenderer.java': '''package com.eternalcraft.gripeverde.client.render;
public class QuarantineSceneRenderer {public static void tick(float dt){}public static void renderDiaryBackground(net.minecraft.client.gui.GuiGraphics g,int w,int h){}}''',
    PREFIX+'client/render/DiaryChrome.java': '''package com.eternalcraft.gripeverde.client.render;
import net.minecraft.client.gui.*;
public class DiaryChrome {
    public static final int INK_TITLE=1,INK_SUBTITLE=2,INK_FLAVOR=3,INK_DIVIDER=4;
    public static void drawPanelShadow(GuiGraphics g,int x,int y,int w,int h){}
    public static void drawCornerOrnaments(GuiGraphics g,int x,int y,int w,int h){}
    public static void drawRegistryTag(GuiGraphics g,int x,int y,int w,int h,String tag){}
    public static void drawInkDivider(GuiGraphics g,int x,int y,int w){}
    public static void drawDivisorLozenge(GuiGraphics g,int x,int y){}
    public static String shorten(Font font,String text,int w){return text.substring(0,Math.min(text.length(),Math.max(0,w/6)));}
    public static int widthCached(Font font,String text){return font.width(text);}
    public static String tr(String key){return key;}
    public static boolean looksLikeAddress(String ip){return !ip.isBlank()&&!ip.contains(" ");}
    public static String addressReadout(String ip,boolean english){return "host: "+ip;}
    public static void renderWidgets(net.minecraft.client.gui.screens.Screen screen,GuiGraphics g,int x,int y,float partial){}
}''',
})
# Existing GUI doubles include a ConfirmScreen/ChroniclerMemory not needed here,
# but compile independently; include only the files our two classes depend on.
for name in ('net/minecraft/client/gui/screens/ConfirmScreen.java',
             PREFIX+'client/lore/ChroniclerMemory.java', PREFIX+'client/lore/Marginalia.java'):
    STUBS.pop(name, None)

HARNESS = r'''import java.util.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.multiplayer.ServerData;
import com.eternalcraft.gripeverde.client.screens.*;
import com.eternalcraft.gripeverde.client.widgets.*;
import com.eternalcraft.gripeverde.client.lore.Discoveries;
public class ServerFormLayoutHarness {
    static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    static boolean overlap(int x,int y,int w,int h,int x2,int y2,int w2,int h2){return x<x2+w2&&x+w>x2&&y<y2+h2&&y+h>y2;}
    static void checkLayout(Screen screen,boolean warning){
        List<AbstractWidget> widgets=screen.children();
        int fields=0,buttons=0;
        for(int i=0;i<widgets.size();i++){
            AbstractWidget a=widgets.get(i);if(a instanceof DiaryTextField)fields++;if(a instanceof DiaryButton)buttons++;
            check(a.x>=0&&a.y>=0&&a.x+a.width<=screen.width&&a.y+a.height<=screen.height,"widget outside screen at "+screen.width+"x"+screen.height);
            for(int j=0;j<i;j++){AbstractWidget b=widgets.get(j);check(!overlap(a.x,a.y,a.width,a.height,b.x,b.y,b.width,b.height),"widgets overlap at "+screen.width+"x"+screen.height);}
        }
        check(fields==(screen instanceof DiaryEditServerScreen?2:1)&&buttons==2,"missing form controls");
        GuiGraphics g=new GuiGraphics();screen.render(g,0,0,0);
        int warningCount=0;
        for(int i=0;i<g.texts.size();i++){
            var text=g.texts.get(i);
            check(text.x()>=0&&text.y()>=0&&text.x()+text.width()<=screen.width&&text.y()+text.height()<=screen.height,"text outside screen: "+text.value());
            if(text.value().startsWith("That address")||text.value().startsWith("Esa direccion"))warningCount++;
            for(var widget:widgets)check(!overlap(text.x(),text.y(),text.width(),text.height(),widget.x,widget.y,widget.width,widget.height),"text on control at "+screen.width+"x"+screen.height+": "+text.value());
            for(int j=0;j<i;j++){var other=g.texts.get(j);check(!overlap(text.x(),text.y(),text.width(),text.height(),other.x(),other.y(),other.width(),other.height()),"text overlaps at "+screen.width+"x"+screen.height+": "+text.value()+" / "+other.value());}
        }
        check(warningCount==(warning?1:0),"expected warning exactly once while active");
    }
    static void warn(Screen screen){
        var field=screen.children().stream().filter(w->w instanceof DiaryTextField).map(w->(DiaryTextField)w).findFirst().orElseThrow();
        field.setValue("13.13.13.13");
        screen.children().stream().filter(w->w instanceof DiaryButton).map(w->(DiaryButton)w).findFirst().orElseThrow().onPress();
    }
    public static void main(String[] args){
        int checks=0;
        for(boolean english:new boolean[]{true,false})for(int w:new int[]{160,161,200,320,364,800,1920})for(int h=120;h<=320;h++){
            Discoveries.english=english;
            for(String input:new String[]{"", "play.example.org:25565", "bad host"}){
                Screen editor=new DiaryEditServerScreen(null,b->{throw new AssertionError("layout submitted editor");},new ServerData("Draft server name",input,false));
                editor.rebuild(w,h);checkLayout(editor,false);checks++;
                Screen direct=new DiaryDirectJoinScreen(null,b->{throw new AssertionError("layout submitted direct join");},new ServerData("unused",input,false));
                direct.rebuild(w,h);
                if(input.isEmpty())((DiaryTextField)direct.children().get(0)).setValue("");
                checkLayout(direct,false);checks++;
                net.minecraft.Util.now=10000;warn(direct);checkLayout(direct,true);checks++;
                // Rebuild by real resize while the warning timer is in flight.
                direct.resize(direct.minecraft,w,h);checkLayout(direct,true);checks++;
                net.minecraft.Util.now=15100;checkLayout(direct,true);checks++;
                net.minecraft.Util.now=16000;checkLayout(direct,false);checks++;
            }
        }
        System.out.println("Server form layout: "+checks+" complete-class geometry cases passed");
    }
}
'''


class Wiring(unittest.TestCase):
    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_server_form_layout.py --require-java', (ROOT / path).read_text())


class Executable(unittest.TestCase):
    def test_complete_classes(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        with tempfile.TemporaryDirectory() as tmp:
            sources = dict(STUBS)
            for name, source in SOURCES.items():
                sources[PREFIX+'client/screens/'+name+'.java'] = source
            sources['ServerFormLayoutHarness.java'] = HARNESS
            paths = []
            for name, text in sources.items():
                path = Path(tmp)/name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            subprocess.run(['java', '-Xmx256m', '-cp', tmp, 'ServerFormLayoutHarness'], check=True, timeout=60)


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
