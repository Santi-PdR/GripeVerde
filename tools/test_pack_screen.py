#!/usr/bin/env python3
"""Compile the complete pack screen with GUI/model doubles.

Checks geometry, clipped native icons, footer separation and resize. Does not
run Minecraft, real pack application/resource reload, glyphs or OS input.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_grid_option_screens import STUBS as BASE_STUBS, PREFIX

ROOT = Path(__file__).resolve().parents[1]
SOURCE = (ROOT / f'src/main/java/{PREFIX}client/screens/DiaryPackSelectionScreen.java').read_text()
STUBS = dict(BASE_STUBS)
STUBS.update({
    'net/minecraft/client/gui/components/events/GuiEventListener.java': '''package net.minecraft.client.gui.components.events;
public interface GuiEventListener {default boolean isMouseOver(double x,double y){return false;}}''',
    'net/minecraft/client/gui/components/AbstractButton.java': '''package net.minecraft.client.gui.components;
public class AbstractButton extends AbstractWidget {public AbstractButton(int x,int y,int w,int h,net.minecraft.network.chat.Component text){super(x,y,w,h,text);}}''',
    'net/minecraft/client/gui/components/AbstractSelectionList.java': '''package net.minecraft.client.gui.components;
import java.util.*;import net.minecraft.client.gui.*;import net.minecraft.client.gui.screens.packs.*;
public class AbstractSelectionList<T> extends AbstractWidget {
 public List<T> kids=new ArrayList<>();public double scroll;public int renders,selections;public boolean fail,renderSelection=true,focused;
 public AbstractSelectionList(){super(0,32,200,300,net.minecraft.network.chat.Component.literal("list"));}
 public void updateSize(int w,int h,int top,int bottom){width=w;y=top;height=bottom-top;x=0;}
 public void setLeftPos(int left){x=left;}public int getLeft(){return x;}public int getTop(){return y;}public int getBottom(){return y+height;}
 public int getRowLeft(){return x+2;}public int getRowWidth(){return width;}public int rowTop(int i){return y+17-(int)scroll+i*36;}
 public double getScrollAmount(){return scroll;}public int getMaxScroll(){return Math.max(0,kids.size()*36+17-height);}
 public void setRenderSelection(boolean b){renderSelection=b;}public void setRenderBackground(boolean b){}public void setRenderTopAndBottom(boolean b){}
 public List<T> children(){return kids;}public T getSelected(){return kids.isEmpty()?null:kids.get(0);}public boolean isFocused(){return focused;}
 public boolean isMouseOver(double mx,double my){return mx>=x&&mx<x+width&&my>=y&&my<y+height;}
 public void render(GuiGraphics g,int mx,int my,float partial){renders++;g.enableScissor(x,y,x+width,y+height);
  try {for(int i=0;i<kids.size();i++){int top=rowTop(i);if(top>getBottom())break;if(top+32<y)continue;
   g.fill(x+2,top,x+34,top+32,0xFF123456);g.drawString(new Font(),"NATIVE OVERFLOW TEXT MUST NOT SHOW",x+36,top+1,0xFFFFFFFF,false);}
   if(fail)throw new IllegalStateException("native list");
  }finally{g.disableScissor();}}
}''',
    'net/minecraft/server/packs/repository/PackRepository.java': '''package net.minecraft.server.packs.repository;
public class PackRepository {public java.util.List<Integer> getAvailablePacks(){return java.util.List.of(1,2,3,4,5,6,7);}public java.util.List<String> getSelectedIds(){return java.util.List.of("one","two");}}''',
    'net/minecraft/client/gui/screens/packs/PackSelectionModel.java': '''package net.minecraft.client.gui.screens.packs;
import net.minecraft.network.chat.Component;
public class PackSelectionModel {public static class Entry {
 public boolean chosen;public Component getTitle(){return Component.literal("A long translated pack name");}
 public Component getExtendedDescription(){return Component.literal("Detailed resource pack description with a second line");}
 public boolean isSelected(){return chosen;}public boolean isFixedPosition(){return false;}public boolean isRequired(){return false;}
 public Compatibility getCompatibility(){return new Compatibility();}
 }public static class Compatibility {public boolean isCompatible(){return true;}public Component getDescription(){return Component.literal("incompatible pack");}}}''',
    'net/minecraft/client/gui/screens/packs/PackSelectionScreen.java': '''package net.minecraft.client.gui.screens.packs;
import java.util.*;import java.nio.file.Path;import java.util.function.Consumer;
import net.minecraft.client.gui.components.*;import net.minecraft.client.gui.screens.Screen;import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
public class PackSelectionScreen extends Screen {
 public AbstractSelectionList<PackSelectionModel.Entry> available,selected;public int folders,applies;public Consumer<PackRepository> callback;public PackRepository repo;
 public PackSelectionScreen(PackRepository repo,Consumer<PackRepository> callback,Path dir,Component title){super(title);this.repo=repo;this.callback=callback;}
 protected void init(){available=new AbstractSelectionList<>();selected=new AbstractSelectionList<>();
  available.setLeftPos(width/2-204);selected.setLeftPos(width/2+4);
  for(int i=0;i<100;i++){available.kids.add(new PackSelectionModel.Entry());var e=new PackSelectionModel.Entry();e.chosen=true;selected.kids.add(e);}
  // Deliberately opposite child order: resolver must sort the actual lists.
  addWidget(selected);addWidget(available);
  var open=new Button();open.setX(width/2-154);open.setY(height-48);open.action=()->folders++;addRenderableWidget(open);
  var done=new Button();done.setX(width/2+4);done.setY(height-48);done.action=this::onClose;addRenderableWidget(done);
 }
 public void onClose(){applies++;callback.accept(repo);}
}''',
    'net/minecraft/client/gui/components/MultiLineLabel.java': '''package net.minecraft.client.gui.components;
import net.minecraft.client.gui.*;import net.minecraft.network.chat.Component;
public class MultiLineLabel {String text;int width,lines;Font font;
 public static MultiLineLabel create(Font font,Component text,int width,int lines){var label=new MultiLineLabel();label.font=font;label.text=text.getString();label.width=width;label.lines=lines;return label;}
 public void renderLeftAligned(GuiGraphics g,int x,int y,int lineH,int color){int chars=Math.max(1,width/6);for(int i=0;i<lines&&i*chars<text.length();i++)g.drawString(font,text.substring(i*chars,Math.min(text.length(),(i+1)*chars)),x,y+i*lineH,color,false);}
}''',
    'net/minecraft/client/gui/GuiGraphics.java': '''package net.minecraft.client.gui;
import java.util.*;
public class GuiGraphics {
 public record Text(String value,int x,int y,int width,int height,int color){}
 public record Fill(int x,int y,int right,int bottom,int color){}
 record Pending(Font font,String text,int x,int y,int color){}
 public List<Text> texts=new ArrayList<>();public List<Fill> rectangles=new ArrayList<>();public List<Integer> dividers=new ArrayList<>();public List<String> events=new ArrayList<>();
 public Deque<int[]> clips=new ArrayDeque<>();List<Pending> pending=new ArrayList<>();
 public void drawString(Font f,String text,int x,int y,int color,boolean shadow){pending.add(new Pending(f,text,x,y,color));}
 public void flush(){events.add("flush");for(var p:pending){int x=p.x,y=p.y,r=x+p.font.width(p.text),b=y+p.font.lineHeight;if(!clips.isEmpty()){var c=clips.peek();x=Math.max(x,c[0]);y=Math.max(y,c[1]);r=Math.min(r,c[2]);b=Math.min(b,c[3]);}if(r>x&&b>y)texts.add(new Text(p.text,x,y,r-x,b-y,p.color));}pending.clear();}
 public void enableScissor(int x,int y,int r,int b){if(!clips.isEmpty()){var c=clips.peek();x=Math.max(x,c[0]);y=Math.max(y,c[1]);r=Math.min(r,c[2]);b=Math.min(b,c[3]);}clips.push(new int[]{x,y,r,b});events.add("clip");}
 public void disableScissor(){clips.pop();events.add("unclip");}
 public void fill(int x,int y,int r,int b,int color){if(!clips.isEmpty()){var c=clips.peek();x=Math.max(x,c[0]);y=Math.max(y,c[1]);r=Math.min(r,c[2]);b=Math.min(b,c[3]);}if(r>x&&b>y)rectangles.add(new Fill(x,y,r,b,color));}
}''',
})
name = 'net/minecraft/network/chat/Component.java'
STUBS[name] = STUBS[name].replace('public static Component literal(', 'public static Component empty(){return literal("");}public static Component literal(')
name = 'net/minecraft/client/gui/components/AbstractWidget.java'
STUBS[name] = STUBS[name].replace('public Component getMessage()', 'public int getWidth(){return width;}public void setWidth(int w){width=w;}public void setHeight(int h){height=h;}public Component getMessage()')
name = 'net/minecraft/client/gui/components/Button.java'
STUBS[name] = '''package net.minecraft.client.gui.components;
public class Button extends AbstractButton {public Runnable action=()->{};public Button(){super(0,0,150,20,net.minecraft.network.chat.Component.literal("button"));}public void onPress(){if(active)action.run();}}'''
name = 'net/minecraft/client/Options.java'
STUBS[name] = STUBS[name].replace('public class Options {', 'public class Options {public OptionInstance<Boolean> touchscreen(){return new OptionInstance<>(false);}')
name = PREFIX+'client/render/PaperTexture.java'
STUBS[name] = STUBS[name].replace('OFFICIAL,CORRESPONDENCE,MEDICAL,TECHNICAL', 'OFFICIAL,CORRESPONDENCE,MEDICAL,TECHNICAL,ARCHIVE')
STUBS[name] = STUBS[name].replace('public class PaperTexture {', 'public class PaperTexture {public static int paperColor(PaperKind kind){return 0xE3D5AB;}')
name = PREFIX+'client/render/DiaryChrome.java'
STUBS[name] = STUBS[name].replace('public class DiaryChrome {', '''public class DiaryChrome {
 public static void washVanillaRows(GuiGraphics g,net.minecraft.client.gui.components.AbstractSelectionList<?> list){}
 public static int rowTopOf(Object list,int index){return ((net.minecraft.client.gui.components.AbstractSelectionList<?>)list).rowTop(index);}
 public static int rowStepOf(Object list){return 36;}
 public static int firstVisibleRow(Object list,int count,int top,int step,double scroll){return Math.max(0,Math.min(count,((int)scroll-17)/36));}
''')

name = PREFIX+'client/widgets/DiaryButton.java'
STUBS[name] = STUBS[name].replace('extends AbstractWidget', 'extends AbstractButton')
STUBS[name] = STUBS[name].replace('import net.minecraft.client.gui.components.AbstractWidget;', 'import net.minecraft.client.gui.components.AbstractButton;')
STUBS[PREFIX+'client/render/PaperButtons.java'] = '''package com.eternalcraft.gripeverde.client.render;
import java.util.*;import net.minecraft.client.gui.screens.Screen;import net.minecraft.client.gui.components.*;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
public class PaperButtons {public static void syncStates(){}public static List<DiaryButton> dressAll(Screen screen){
 var out=new ArrayList<DiaryButton>();for(var w:new ArrayList<>(screen.children()))if(w instanceof Button b&&b.visible){
  var d=new DiaryButton(b.x,b.y,b.width,b.height,b.getMessage(),DiaryButton.Icon.NONE,DiaryButton.Variant.SECONDARY,b::onPress);
  b.visible=false;screen.children().add(d);screen.renderables.add(d);out.add(d);
 }return out;}}'''

HARNESS = r'''import java.util.*;import java.nio.file.Path;
import net.minecraft.client.gui.*;import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;import net.minecraft.server.packs.repository.PackRepository;
import com.eternalcraft.gripeverde.client.screens.DiaryPackSelectionScreen;
import com.eternalcraft.gripeverde.client.lore.Discoveries;
public class PackScreenHarness {
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 static boolean overlap(int x,int y,int w,int h,AbstractWidget b){return x<b.x+b.width&&x+w>b.x&&y<b.y+b.height&&y+h>b.y;}
 static List<AbstractWidget> buttons(DiaryPackSelectionScreen s){return s.children().stream().filter(w->w instanceof AbstractButton&&w.visible).toList();}
 static void verify(DiaryPackSelectionScreen s){
  var a=s.available;var b=s.selected;var buttons=buttons(s);check(buttons.size()==2,"missing pack controls");
  check(!a.renderSelection&&!b.renderSelection,"native selection frame still enabled");
  check(a.x+a.width<=b.x&&a.x>=0&&b.x+b.width<=s.width,"pack columns overlap or escape screen");
  check(a.y>=32&&a.getBottom()<=buttons.get(0).y-8,"list covers footer controls");
  for(var w:buttons){check(w.x>=0&&w.x+w.width<=s.width&&w.y>=a.getBottom()&&w.y+w.height<=s.height-22,"button outside safe row");}
  int ar=a.renders,br=b.renders;GuiGraphics g=new GuiGraphics();s.render(g,-1,-1,0);g.flush();
  check(a.renders==ar+1&&b.renders==br+1,"list rendered twice or not at all");check(g.clips.isEmpty(),"pack clip leaked");
  for(var t:g.texts){check(!t.value().startsWith("NATIVE"),"native cached text escaped icon clip");
   check(t.x()>=0&&t.x()+t.width()<=s.width&&t.y()>=0&&t.y()+t.height()<=s.height,"pack text outside screen");
   for(var w:buttons)check(!overlap(t.x(),t.y(),t.width(),t.height(),w),"pack/footer text overlaps a button: "+s.width+"x"+s.height+" "+t.value());
   if(t.y()>=s.height-20)check(t.y()>=buttons.get(0).y+buttons.get(0).height+3,"footer lacks its own row");
  }
  check(g.rectangles.stream().anyMatch(r->r.color()==0xFF123456),"native pack icons were lost");
  for(var r:g.rectangles)if(r.color()==0xCC3D2A18){check(r.y()>=a.y&&r.bottom()<=a.getBottom(),"thumb outside native rail");}
 }
 static void sweep(){int cases=0;
  for(boolean english:new boolean[]{false,true})for(int w:new int[]{160,161,200,320,420,800,1920})for(int h=120;h<=500;h++){
   Discoveries.english=english;var s=new DiaryPackSelectionScreen(new PackRepository(),repo->{},Path.of("packs"),Component.literal("Choose resource packs"));
   s.rebuild(w,h);verify(s);s.available.scroll=35.5;s.selected.scroll=1000.5;verify(s);
   s.resize(s.minecraft,w,h);verify(s);check(s.folders==0&&s.applies==0,"resize applied packs/opened folder");cases++;
  }
  System.out.println("Pack screen: "+cases+" complete-class layout/render/scroll/resize cases passed");
 }
 static void actions(){int[] calls={0};var s=new DiaryPackSelectionScreen(new PackRepository(),repo->calls[0]++,Path.of("packs"),Component.literal("packs"));s.rebuild(160,120);
  Discoveries.english=false;
  s.selected.kids.add(s.available.kids.remove(0));
  GuiGraphics live=new GuiGraphics();s.render(live,0,0,0);live.flush();
  check(live.texts.stream().anyMatch(t->t.value().contains("101 de 200")),"footer still shows committed repository count");
  check(calls[0]==0,"live count committed resource changes");
  var buttons=buttons(s);((com.eternalcraft.gripeverde.client.widgets.DiaryButton)buttons.get(0)).onPress();check(s.folders==1&&calls[0]==0,"folder callback changed");
  ((com.eternalcraft.gripeverde.client.widgets.DiaryButton)buttons.get(1)).onPress();check(s.applies==1&&calls[0]==1,"Done callback changed");
  s.rebuild(800,600);s.selected.focused=true;GuiGraphics focus=new GuiGraphics();s.render(focus,0,0,0);focus.flush();
  check(focus.rectangles.stream().anyMatch(r->r.color()==0x286B5533),"keyboard focus lost its paper marker");
  s.rebuild(800,600);s.available.fail=true;GuiGraphics g=new GuiGraphics();s.render(g,0,0,0);g.flush();check(g.clips.isEmpty(),"exception left native/outer clip active");
  check(s.selected.renders==1,"one list failure skipped the other list");
 }
 public static void main(String[] args){sweep();actions();}
}
'''


class Guards(unittest.TestCase):
    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_pack_screen.py --require-java', (ROOT / path).read_text())

    def test_layout_precedes_button_wrapping(self):
        self.assertLess(SOURCE.index('layoutPackContents();'), SOURCE.index('PaperButtons.dressAll(this)'))
        self.assertNotIn('public boolean mouseClicked(', SOURCE)


class Executable(unittest.TestCase):
    def test_complete_pack_screen(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; required in CI')
        with tempfile.TemporaryDirectory() as tmp:
            sources = dict(STUBS)
            sources[PREFIX+'client/screens/DiaryPackSelectionScreen.java'] = SOURCE
            sources['PackScreenHarness.java'] = HARNESS
            paths = []
            for name, text in sources.items():
                path = Path(tmp) / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            subprocess.run(['java', '-Xmx256m', '-cp', tmp, 'PackScreenHarness'], check=True, timeout=90)


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
