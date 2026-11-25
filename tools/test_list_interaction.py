#!/usr/bin/env python3
"""Execute list clipping, paper paint, pointer selection and server status methods.

GUI/session doubles, not pixels, native input, actual pings or world loading.
The full Forge classes are compiled separately by Gradle in CI.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method
import lint_java

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/screens'
WORLD = (BASE / 'DiarySelectWorldScreen.java').read_text()
MULTI = (BASE / 'DiaryMultiplayerScreen.java').read_text()
STUBS = {
    'net/minecraft/network/chat/Component.java': '''package net.minecraft.network.chat;
public record Component(String text){public static Component translatable(String key){return new Component(key);}public String getString(){return text;}}''',
    'net/minecraft/SharedConstants.java': '''package net.minecraft;
public class SharedConstants {public static Version getCurrentVersion(){return new Version();}public static class Version {public int getProtocolVersion(){return 763;}}}''',
    'net/minecraft/client/multiplayer/ServerData.java': '''package net.minecraft.client.multiplayer;
public class ServerData {public boolean pinged;public long ping;public int protocol=763;}''',
    'net/minecraft/client/gui/GuiGraphics.java': '''package net.minecraft.client.gui;
import java.util.*;
public class GuiGraphics {
 public record Rect(int x,int y,int right,int bottom,int color){}
 public List<Rect> rectangles=new ArrayList<>();public List<String> events=new ArrayList<>();public Deque<int[]> clips=new ArrayDeque<>();
 public void flush(){events.add("flush");}public void enableScissor(int x,int y,int r,int b){if(!clips.isEmpty()){var c=clips.peek();x=Math.max(x,c[0]);y=Math.max(y,c[1]);r=Math.min(r,c[2]);b=Math.min(b,c[3]);}clips.push(new int[]{x,y,r,b});events.add("clip");}
 public void disableScissor(){clips.pop();events.add("unclip");}
 public void fill(int x,int y,int r,int b,int color){if(!clips.isEmpty()){var c=clips.peek();x=Math.max(x,c[0]);y=Math.max(y,c[1]);r=Math.min(r,c[2]);b=Math.min(b,c[3]);}if(r>x&&b>y)rectangles.add(new Rect(x,y,r,b,color));}
 public void drawString(Object font,String text,int x,int y,int color,boolean shadow){}
 public void renderTooltip(Object font,Object text,int x,int y){events.add("tooltip");}
}''',
    'com/eternalcraft/gripeverde/client/render/DiaryChrome.java': '''package com.eternalcraft.gripeverde.client.render;
public class DiaryChrome {public static int widthCached(Object font,String text){return text.length()*6;}public static String fitText(Object font,String text,int width){return text.substring(0,Math.min(text.length(),Math.max(0,width/6)));}}''',
    'com/eternalcraft/gripeverde/client/render/PaperTexture.java': '''package com.eternalcraft.gripeverde.client.render;
public class PaperTexture {public enum PaperKind {OFFICIAL}public static int paperColor(PaperKind kind){return 0xE6D2A6;}}''',
    'com/eternalcraft/gripeverde/client/render/DiaryTheme.java': '''package com.eternalcraft.gripeverde.client.render;
public class DiaryTheme {public static int withAlpha(int color,int alpha){return (alpha<<24)|(color&0xFFFFFF);}}''',
    'com/eternalcraft/gripeverde/client/lore/Discoveries.java': '''package com.eternalcraft.gripeverde.client.lore;
public class Discoveries {public static boolean isEnglish(){return false;}}''',
    'com/eternalcraft/gripeverde/client/audio/UiSounds.java': '''package com.eternalcraft.gripeverde.client.audio;
public class UiSounds {public static void tab(){}}''',
}
HARNESS = r'''import java.util.*;import java.lang.reflect.*;
import net.minecraft.client.gui.GuiGraphics;import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;import com.eternalcraft.gripeverde.client.render.DiaryChrome;
public class ListInteractionHarness {
 static class Font {List<String> split(Component text,int width){return List.of(text.getString());}}
 static class Summary {boolean disabled,experimental;boolean isDisabled(){return disabled;}boolean isLifecycleExperimental(){return experimental;}}
 static class WorldSelectionList {
  static class Entry {Summary summary=new Summary();}
  List<Object> kids=new ArrayList<>();Entry selected;int selections,renders,launches;boolean fail;
  int left=200,right=470,top=100,bottom=220;double scroll;
  WorldSelectionList(){for(int i=0;i<100;i++)kids.add(new Entry());}
  List<Object> children(){return kids;}Entry getSelected(){return selected;}
  void setSelected(Entry entry){selected=entry;selections++;}double getScrollAmount(){return scroll;}
  void render(GuiGraphics g,int x,int y,float partial){renders++;g.enableScissor(left,top,right,bottom);
   try {g.fill(left,top,right,bottom,0xFF000000);if(fail)throw new IllegalStateException("render failed");}
   finally {g.disableScissor();}}
 }
 static class PaperButtons {static void syncStates(){}}
 static class Parent {
  int parentClicks,footerActions;
  public boolean mouseClicked(double x,double y,int button){parentClicks++;if(this instanceof World w){
   if(w.widgetAt(x,y)){footerActions++;return true;}
   if(x>=200&&x<470&&y>=100&&y<220)w.vanillaList.launches++;
  }return false;}
 }
 static class World extends Parent {
  WorldSelectionList vanillaList=new WorldSelectionList();int width=800,height=600,listTop=100,listBottom=220,rowLeft=200,rowRight=470;
  int rowItemHeight=36,rowStepExact;Method rowTopMethod;Font font=new Font();boolean focused;
  int numColumnRight(){return ledgerLeft()+34;}int ledgerLeft(){return 10;}int ledgerRight(){return width-10;}int sheetTop(){return 70;}
  boolean widgetAt(double x,double y){return x>=300&&x<400&&y>=240&&y<260;}
  void focusWorldList(){focused=true;}
  static Summary summaryOf(Object row){return row instanceof WorldSelectionList.Entry e?e.summary:null;}
  __WORLD__
 }
 static class Multiplayer {WorldSelectionList vanillaList=new WorldSelectionList();__MULTI__}
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 static void hidden(boolean multi,boolean fail){
  GuiGraphics g=new GuiGraphics();World w=new World();Multiplayer m=new Multiplayer();
  var list=multi?m.vanillaList:w.vanillaList;list.fail=fail;
  // A caller clip is preserved too, including the exceptional path.
  g.enableScissor(0,0,800,600);
  try {if(multi)m.renderNativeListHidden(g,0);else w.renderNativeListHidden(g,0);check(!fail,"missing exception");}
  catch(IllegalStateException expected){check(fail,"unexpected exception");}
  check(list.renders==1,"native refresh/ping pass skipped or duplicated");
  check(g.rectangles.isEmpty(),"native selection/texture escaped clipping");
  check(g.clips.size()==1,"clip stack leaked");
  check(g.events.get(g.events.size()-2).equals("flush")&&g.events.get(g.events.size()-1).equals("unclip"),"deferred draw escaped clip");
  g.fill(20,20,30,30,1);check(g.rectangles.size()==1,"custom UI still clipped");
 }
 static void pointers(){int cases=0;
  for(int width:new int[]{320,800,1920})for(double scroll:new double[]{0,.5,35.99,1000.5}){
   World w=new World();w.width=width;w.vanillaList.scroll=scroll;
   for(double x:new double[]{0,15,16,50,200,230,319,width-23,width-22,width-1})for(double y:new double[]{99,100,103,104,110,135,136,139,140,141,219,220,221}){
    Object row=w.worldRowAt(x,y);int before=w.vanillaList.selections;var selected=w.vanillaList.selected;
    w.mouseClicked(x,y,0);
    if(row!=null){check(w.vanillaList.selections==before+1&&w.vanillaList.selected==row&&w.focused,"visible row not selected");
     w.mouseClicked(x,y,0);check(w.vanillaList.selected==row,"repeated click changed row");}
    else check(w.vanillaList.selections==before&&w.vanillaList.selected==selected,"blank paper selected a row");
    check(w.vanillaList.launches==0,"pointer reached native join/double-click");cases++;
   }
  }
  World w=new World();var e=(WorldSelectionList.Entry)w.vanillaList.kids.get(0);
  e.summary.disabled=true;w.mouseClicked(50,110,0);check(w.vanillaList.selections==0,"disabled world selected");e.summary.disabled=false;
  for(int button:new int[]{1,2})w.mouseClicked(50,110,button);check(w.vanillaList.selections==0,"secondary click selected a world");
  w.mouseClicked(320,250,0);check(w.footerActions==1,"footer action swallowed");
  check(w.worldRowAt(50,136)==null&&w.worldRowAt(50,140)!=null,"row gap not excluded");
  System.out.println("List pointer: "+cases+" positions plus disabled/secondary/footer cases passed");
 }
 static void paper(){World w=new World();GuiGraphics g=new GuiGraphics();w.paintListPaper(g,50,110);
  check(g.rectangles.stream().anyMatch(r->r.x()==10&&r.right()==790&&r.y()==74&&r.bottom()==224&&r.color()==0xFFE6D2A6),"paper retains transparent native-warning slot");
  check(g.rectangles.stream().anyMatch(r->r.x()==16&&r.right()==784&&r.y()==104&&r.bottom()==136&&r.color()==0x286B5533),"hover wash missing or shifted");
  check(w.vanillaList.selections==0,"hover selected a row");
  g=new GuiGraphics();w.paintListPaper(g,50,138);check(g.rectangles.stream().noneMatch(r->r.color()==0x286B5533),"gap hover highlighted a world");
  var entry=(WorldSelectionList.Entry)w.vanillaList.kids.get(0);entry.summary.experimental=true;
  g=new GuiGraphics();w.drawWorldWarningTooltip(g,684,110);check(g.events.contains("tooltip"),"experimental tooltip lost");
  g=new GuiGraphics();w.drawWorldWarningTooltip(g,684,138);check(!g.events.contains("tooltip"),"warning tooltip in gap");
 }
 static void statuses(){int cases=0;
  for(boolean english:new boolean[]{false,true})for(boolean pinged:new boolean[]{false,true})for(long ping:new long[]{-2,-1,0,42,9999})for(int protocol:new int[]{762,763,764}){
   ServerData d=new ServerData();d.pinged=pinged;d.ping=ping;d.protocol=protocol;
   String label=Multiplayer.serverConnectionLabel(d,english);int color=Multiplayer.serverConnectionColor(d);
   if(!pinged||ping==-2)check(label.equals(english?"Pinging...":"Consultando...")&&color==0xFF6B5533,"pending shown as online/offline");
   else if(ping<0)check(label.equals(english?"Unreachable":"Sin conexión")&&color==0xFF8C2A1E,"offline hidden");
   else if(protocol!=763)check(label.equals("Incompatible")&&color==0xFF8C2A1E,"incompatible hidden");
   else check(label.contains(ping+" ms")&&color==0xFF355326,"online latency missing");cases++;
  }
  check(Multiplayer.serverConnectionLabel(null,true).equals("Pinging..."),"null status failed");
  System.out.println("Server status: "+cases+" state/language cases passed");
 }
 public static void main(String[] args){for(boolean multi:new boolean[]{false,true})for(boolean fail:new boolean[]{false,true})hidden(multi,fail);pointers();paper();statuses();}
}
'''


class Guards(unittest.TestCase):
    def test_ci_requires_java(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_list_interaction.py --require-java', (ROOT / path).read_text())

    def test_native_drawing_is_only_called_inside_hidden_helper(self):
        for source in (WORLD, MULTI):
            self.assertEqual(source.count('this.vanillaList.render('), 1)
            hidden = method(source, 'private void renderNativeListHidden(')
            self.assertIn('g.enableScissor(0, 0, 0, 0);', hidden)
            self.assertIn('try { g.flush(); } finally { g.disableScissor(); }', hidden)
            self.assertIn('renderNativeListHidden(', method(source, 'public void render('))

    def test_new_lint_rejects_pointer_and_overlay_regressions(self):
        path = str(BASE / 'DiarySelectWorldScreen.java')
        checks = (lint_java.check_world_click_suppression_focus, lint_java.check_world_row_click_safety,
                  lint_java.check_world_list_overlay_and_hitbox)
        clean = lint_java.strip_noise(WORLD)
        for check in checks:
            self.assertFalse(check(path, clean))
        for old, new in (('focusWorldList();', 'noop();'), ('this.vanillaList.setSelected(entry);', 'entry.mouseClicked(mouseX, mouseY, button);'),
                         ('mouseX >= ledgerRight() - 12', 'mouseX > ledgerRight()'), ('mouseY < top + rowStep() - 4', 'true'),
                         ('!summaryOf(row).isDisabled()', 'true'), ('g.enableScissor(0, 0, 0, 0);', 'g.enableScissor(0, 0, 800, 600);'),
                         ('g.fill(x1, top, x2, bottom, opaquePaper);', 'g.fill(x1, top, x2 - 36, bottom, opaquePaper);')):
            with self.subTest(old=old):
                mutated = clean.replace(old, new)
                self.assertNotEqual(clean, mutated)
                self.assertTrue(any(check(path, mutated) for check in checks))


class Executable(unittest.TestCase):
    def test_real_methods(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; executable scenarios required in CI')
        world_signatures = ('private Object worldRowAt(', 'public boolean mouseClicked(', 'private void renderNativeListHidden(',
                            'private void paintListPaper(', 'private Object entryAt(', 'private int rowStep(', 'private int rowTopAt(',
                            'private int rowTopOf(', 'private int firstVisibleRow(', 'private void drawWorldWarningTooltip(')
        multi_signatures = ('private void renderNativeListHidden(', 'private static String serverConnectionLabel(',
                            'private static int serverConnectionColor(')
        harness = HARNESS.replace('__WORLD__', '\n'.join(method(WORLD, sig) for sig in world_signatures))
        harness = harness.replace('__MULTI__', '\n'.join(method(MULTI, sig) for sig in multi_signatures))
        with tempfile.TemporaryDirectory() as tmp:
            paths = []
            for name, text in dict(STUBS, **{'ListInteractionHarness.java': harness}).items():
                path = Path(tmp) / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            subprocess.run(['java', '-cp', tmp, 'ListInteractionHarness'], check=True, timeout=60)


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
