#!/usr/bin/env python3
"""Exercise actual world-screen navigation methods against GUI/list doubles.

No Minecraft world load/delete, actual focus traversal, pixels or OS input.
Measured row tops include headers, fractional scroll, and optional row gaps.
CI requires JDK 17; source guards also run without it.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method

ROOT = Path(__file__).resolve().parents[1]
SOURCE = (ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/screens/DiarySelectWorldScreen.java').read_text()
STUBS = {
    'org/lwjgl/glfw/GLFW.java': '''package org.lwjgl.glfw;
public class GLFW {public static final int GLFW_KEY_ENTER=257,GLFW_KEY_KP_ENTER=335,GLFW_KEY_DELETE=261,
 GLFW_KEY_DOWN=264,GLFW_KEY_UP=265,GLFW_KEY_PAGE_DOWN=267,GLFW_KEY_PAGE_UP=266,GLFW_KEY_HOME=268,GLFW_KEY_END=269;}''',
    'net/minecraft/network/chat/Component.java': '''package net.minecraft.network.chat;
public record Component(String value){public static Component translatable(String key){return new Component(key);}public String getString(){return value;}}''',
    'net/minecraft/client/gui/components/AbstractButton.java': '''package net.minecraft.client.gui.components;
import net.minecraft.network.chat.Component;
public class AbstractButton {public boolean active=true,visible=true;public int presses;public Component label;public Runnable action;
 public AbstractButton(String label){this.label=Component.translatable(label);}public Component getMessage(){return label;}
 public void onPress(){presses++;if(action!=null)action.run();}
 public boolean keyPressed(int key,int scan,int mods){if(active&&visible&&(key==257||key==335||key==32)){onPress();return true;}return false;}}''',
    'com/eternalcraft/gripeverde/client/audio/UiSounds.java': '''package com.eternalcraft.gripeverde.client.audio;
public class UiSounds {public static int clicks,destroys,tabs;public static void playClick(){clicks++;}public static void clickDestroy(){destroys++;}public static void tab(){tabs++;}}''',
}
HARNESS = r'''import java.util.*;
import java.lang.reflect.*;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;
import com.eternalcraft.gripeverde.client.audio.UiSounds;
public class WorldNavigationHarness {
 static class EditBox {boolean visible=true,focused;boolean isFocused(){return focused;}void setFocused(boolean focus){focused=focus;}}
 static class DiaryButton extends AbstractButton {DiaryButton(String label){super(label);}}
 static class PaperButtons {static int syncs;static void syncStates(){syncs++;}}
 public static class FakeList {
  List<Object> kids=new ArrayList<>();Object selected;int top=100,bottom=220,step=36,header,gap,calls,sets;double scroll,max=1000;
  FakeList(int count){for(int i=0;i<count;i++)kids.add(new Object());if(count>0)selected=kids.get(0);}
  List<Object> children(){return kids;}Object getSelected(){return selected;}
  double getScrollAmount(){return scroll;}int getMaxScroll(){return (int)max;}
  void setScrollAmount(double value){sets++;scroll=Math.max(0,Math.min(value,max));}
  void setFocused(boolean focused){}
  public int rowTop(int index){calls++;return top+4+header-(int)scroll+index*step+(index/10)*gap;}
 }
 static class Parent {
  Object focused;int parentKeys;boolean parentHandled;Runnable dispatch;
  Object getFocused(){return focused;}void setFocused(Object value){focused=value;}
  public boolean keyPressed(int key,int scan,int mods){parentKeys++;if(dispatch!=null)dispatch.run();
   if(focused instanceof AbstractButton b)return b.keyPressed(key,scan,mods);return parentHandled;}
 }
 static class World extends Parent {
  FakeList vanillaList;List<EditBox> searchBoxes=new ArrayList<>();List<DiaryButton> dressed=new ArrayList<>();
  int listTop=100,listBottom=220,rowItemHeight=36,rowStepExact;Method rowTopMethod;
  __METHODS__
  void measure()throws Exception {rowStepExact=vanillaList.step;rowTopMethod=FakeList.class.getMethod("rowTop",int.class);}
  void viewport(int top,int bottom){listTop=vanillaList.top=top;listBottom=vanillaList.bottom=bottom;}
 }
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 static World world(int count){World w=new World();w.vanillaList=new FakeList(count);return w;}
 static void buttons(World w){w.dressed.add(new DiaryButton("selectWorld.select"));w.dressed.add(new DiaryButton("selectWorld.delete"));}
 static void idle(World w){for(var b:w.dressed)check(b.presses==0,"unexpected world action");}
 static void keyboard(String test)throws Exception{
  World w=world(20);buttons(w);DiaryButton join=w.dressed.get(0),delete=w.dressed.get(1);
  switch(test){
   case "footer_enter", "footer_keypad", "footer_delete", "footer_inactive", "footer_hidden", "footer_parent_declines" -> {
    DiaryButton edit=new DiaryButton("selectWorld.edit");w.dressed.add(edit);w.setFocused(edit);
    int key=test.equals("footer_keypad")?335:test.equals("footer_delete")?261:257;
    if(test.equals("footer_inactive"))edit.active=false;
    if(test.equals("footer_hidden"))edit.visible=false;
    if(test.equals("footer_parent_declines")){key=261;}
    boolean handled=w.keyPressed(key,0,0);check(join.presses==0&&delete.presses==0,"footer key triggered a different action");
    boolean activation=key!=261&&edit.active&&edit.visible;
    check(edit.presses==(activation?1:0),"focused footer did not own activation");
    if(!edit.active||!edit.visible)check(handled&&w.parentKeys==0,"unavailable footer leaked key");
   }
   case "vanilla_footer_enter" -> {
    AbstractButton custom=new AbstractButton("mod button");w.setFocused(custom);w.keyPressed(257,0,0);idle(w);check(custom.presses==1,"unwrapped button ignored");
   }
   case "focused_join", "focused_delete_enter", "focused_delete_keypad" -> {
    boolean joining=test.equals("focused_join");w.setFocused(joining?join:delete);
    w.keyPressed(test.equals("focused_delete_keypad")?335:257,0,0);
    check(join.presses==(joining?1:0)&&delete.presses==(joining?0:1),"wrong focused action");
   }
   case "list_enter", "list_keypad", "list_delete", "no_focus_enter" -> {
    if(!test.equals("no_focus_enter"))w.setFocused(w.vanillaList);
    boolean deleting=test.equals("list_delete");check(w.keyPressed(deleting?261:test.equals("list_keypad")?335:257,0,0),"shortcut unhandled");
    check(join.presses==(deleting?0:1)&&delete.presses==(deleting?1:0),"list shortcut changed");
   }
   case "search_enter", "search_delete", "search_hidden", "search_not_focused" -> {
    EditBox box=new EditBox();box.focused=!test.equals("search_not_focused");box.visible=!test.equals("search_hidden");w.searchBoxes.add(box);w.setFocused(box);w.parentHandled=true;
    int key=test.equals("search_delete")?261:257;w.keyPressed(key,0,0);
    if(box.visible&&box.focused){idle(w);check(w.parentKeys==1,"search not delegated");}else check(join.presses==1,"unfocused/hidden search blocked shortcut");
   }
   case "join_disabled", "join_hidden", "delete_disabled", "delete_hidden" -> {
    boolean deleting=test.startsWith("delete");DiaryButton target=deleting?delete:join;
    if(test.endsWith("disabled"))target.active=false;else target.visible=false;
    w.keyPressed(deleting?261:257,0,0);idle(w);
   }
   case "arrows_change", "arrows_unchanged", "arrows_unhandled" -> {
    EditBox search=new EditBox();w.searchBoxes.add(search);w.vanillaList.scroll=400;
    w.parentHandled=!test.equals("arrows_unhandled");
    if(test.equals("arrows_change")){w.vanillaList.selected=w.vanillaList.kids.get(12);w.dispatch=()->w.vanillaList.selected=w.vanillaList.kids.get(0);}
    else w.vanillaList.selected=w.vanillaList.kids.get(12);
    w.keyPressed(265,0,0);idle(w);check(w.getFocused()==w.vanillaList,"arrows did not focus list");
    if(test.equals("arrows_change")){check(w.vanillaList.scroll==2,"arrow-up scrolled further away from selected row");}
    else check(w.vanillaList.scroll==400,"unchanged/unhandled key scrolled");
   }
   default -> throw new AssertionError(test);
  }
  check(PaperButtons.syncs==1,"button states not synchronized before key dispatch");
 }
 static void scroll(String test)throws Exception{
  World w=world(100);w.measure();w.vanillaList.max=4000;
  switch(test){
   case "above" -> {w.vanillaList.scroll=100;w.keepSelectedRowVisible();check(w.vanillaList.scroll==2,"above row should reduce scroll");}
   case "below" -> {w.vanillaList.selected=w.vanillaList.kids.get(5);w.keepSelectedRowVisible();check(w.vanillaList.scroll==102,"below row should increase scroll");}
   case "visible" -> {w.vanillaList.selected=w.vanillaList.kids.get(1);w.keepSelectedRowVisible();check(w.vanillaList.sets==0,"visible row moved");}
   case "fractional" -> {w.vanillaList.scroll=100.75;w.keepSelectedRowVisible();check(w.vanillaList.scroll==2.75,"fractional scroll normalization changed");}
   case "header" -> {w.vanillaList.header=80;w.vanillaList.scroll=200;w.keepSelectedRowVisible();check(w.vanillaList.scroll==82,"measured header ignored");}
   case "upper_clamp" -> {w.vanillaList.scroll=400;w.vanillaList.top=90;w.keepSelectedRowVisible();check(w.vanillaList.scroll==0,"negative target not clamped");}
   case "lower_clamp" -> {w.vanillaList.selected=w.vanillaList.kids.get(99);w.vanillaList.max=500;w.keepSelectedRowVisible();check(w.vanillaList.scroll==500,"max target not clamped");}
   case "tall_row" -> {w.viewport(100,120);w.vanillaList.selected=w.vanillaList.kids.get(5);w.keepSelectedRowVisible();double once=w.vanillaList.scroll;
    w.keepSelectedRowVisible();check(w.vanillaList.scroll==once&&w.rowTopAt(5,once)==102,"tall row oscillates instead of top-aligning");}
   case "unknown_selection" -> {w.vanillaList.selected=new Object();w.keepSelectedRowVisible();check(w.vanillaList.sets==0,"unknown row moved scroll");}
   case "empty" -> {w.vanillaList.kids.clear();w.keepSelectedRowVisible();check(w.vanillaList.sets==0,"empty list moved scroll");}
   case "invalid_viewport" -> {w.listBottom=w.listTop;w.keepSelectedRowVisible();check(w.vanillaList.sets==0,"invalid viewport moved scroll");}
   case "null_list" -> {w.vanillaList=null;w.keepSelectedRowVisible();}
   default -> throw new AssertionError(test);
  }
 }
 static void hit(String test)throws Exception{
  World w=world(10000);w.measure();
  w.vanillaList.header=80;w.vanillaList.gap=3;
  switch(test){
   case "after_128", "last", "boundary", "gap", "fractional", "header_space", "no_measurement", "null_list" -> {
    if(test.equals("null_list")){w.vanillaList=null;check(w.entryAt(120)==null,"null list returned row");return;}
    if(test.equals("header_space")){check(w.entryAt(110)==null,"header space mapped to row");return;}
    int index=test.equals("last")?9999:512;w.vanillaList.scroll=index*36+(index/10)*3+80+.75;
    int top=w.vanillaList.rowTop(index);double y=top+5;
    if(test.equals("boundary"))y=top+36;
    if(test.equals("gap")){index=509;w.vanillaList.scroll=index*36+(index/10)*3+80;top=w.vanillaList.rowTop(index);y=top+36;}
    if(test.equals("fractional"))y=top+.5;
    if(test.equals("no_measurement")){w.rowTopMethod=null;w.vanillaList.header=w.vanillaList.gap=0;w.vanillaList.scroll=index*36+.75;top=w.vanillaList.rowTop(index);y=top+.5;}
    w.vanillaList.calls=0;Object got=w.entryAt(y);
    Object expected=test.equals("gap")?null:w.vanillaList.kids.get(index+(test.equals("boundary")?1:0));
    check(got==expected,"hit test resolved wrong row: "+test);check(w.vanillaList.calls<=16,"hit test scanned list linearly");
   }
   default -> throw new AssertionError(test);
  }
 }
 static void sweep()throws Exception{
  int cases=0;
  for(boolean measured:new boolean[]{false,true})for(int step:new int[]{20,36,64})for(int header:new int[]{0,13,80})for(int count:new int[]{0,1,65,129,500}){
   if(!measured&&header!=0)continue;
   World w=world(count);w.rowItemHeight=w.vanillaList.step=step;w.vanillaList.header=header;if(measured)w.measure();
   for(double scroll:new double[]{0,.5,36.9,128*step+.5,499*step}){
    w.vanillaList.scroll=scroll;
    for(double y=100;y<220;y+=.5){
     int expected=-1;
     // Independent linear oracle, no screen helper and no binary search.
     for(int i=0;i<count;i++){int top=104+header-(int)scroll+i*step;if(y>=top&&y<top+step){expected=i;break;}}
     Object got=w.entryAt(y);check(got==(expected<0?null:w.vanillaList.kids.get(expected)),"row oracle mismatch");cases++;
    }
   }
  }
  System.out.println("World row lookup: "+cases+" oracle cases passed");
 }
 static void visibleSweep()throws Exception{
  int cases=0;
  for(boolean measured:new boolean[]{false,true})for(int count:new int[]{0,1,64,65,128,10000})for(int step:new int[]{20,36,64})for(int header:new int[]{0,13,80}){
   if(!measured&&header!=0)continue;
   World w=world(count);w.rowItemHeight=w.vanillaList.step=step;w.vanillaList.header=header;if(measured)w.measure();
   for(double scroll:new double[]{0,.5,64*step+.9,128*step+.5,9999*step}){
    w.vanillaList.scroll=scroll;w.vanillaList.calls=0;int first=w.firstVisibleRow(count,scroll);
    check(w.vanillaList.calls<=14,"first visible search is linear");
    List<Integer> expected=new ArrayList<>(),actual=new ArrayList<>();
    for(int i=0;i<count;i++){int top=104+header-(int)scroll+i*step;if(top<=220&&top+step-4>=100)expected.add(i);}
    for(int i=first;i<count;i++){int top=w.rowTopAt(i,scroll);if(top>220)break;if(top+step-4>=100)actual.add(i);}
    check(actual.equals(expected),"visible-row range dropped a row or retained offscreen decoration");cases++;
   }
  }
  System.out.println("World visible rows: "+cases+" oracle cases passed");
 }
 public static void main(String[] args)throws Exception{
  if(args[0].equals("sweep"))sweep();else if(args[0].equals("visible_sweep"))visibleSweep();else if(args[0].startsWith("key:"))keyboard(args[0].substring(4));
  else if(args[0].startsWith("scroll:"))scroll(args[0].substring(7));else hit(args[0].substring(4));
 }
}
'''
KEYS = ('footer_enter', 'footer_keypad', 'footer_delete', 'footer_inactive', 'footer_hidden', 'footer_parent_declines',
        'vanilla_footer_enter', 'focused_join', 'focused_delete_enter', 'focused_delete_keypad', 'list_enter',
        'list_keypad', 'list_delete', 'no_focus_enter', 'search_enter', 'search_delete', 'search_hidden',
        'search_not_focused', 'join_disabled', 'join_hidden', 'delete_disabled', 'delete_hidden',
        'arrows_change', 'arrows_unchanged', 'arrows_unhandled')
SCROLL = ('above', 'below', 'visible', 'fractional', 'header', 'upper_clamp', 'lower_clamp', 'tall_row',
          'unknown_selection', 'empty', 'invalid_viewport', 'null_list')
HITS = ('after_128', 'last', 'boundary', 'gap', 'fractional', 'header_space', 'no_measurement', 'null_list')


class Wiring(unittest.TestCase):
    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_world_navigation.py --require-java', (ROOT / path).read_text())

    def test_render_loops_visit_visible_rows_without_archive_caps(self):
        for sig in ('private void paintWorldRowsInk(', 'private void drawRegisterPage(', 'private void paintListPaper('):
            body = method(SOURCE, sig)
            self.assertIn('firstVisibleRow(', body)
            self.assertNotIn('i < 64', body)
            self.assertIn(') break;', body)

    def test_exact_lookup_not_capped(self):
        body = method(SOURCE, 'private Object entryAt(')
        self.assertNotIn('i < 128', body)
        self.assertIn('while (lo < hi)', body)


class Executable(unittest.TestCase):
    def test_navigation_methods(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        signatures = ('private boolean isSearchBoxFocused(', 'private void focusWorldList(', 'public boolean keyPressed(',
                      'private void keepSelectedRowVisible(', 'private Object entryAt(', 'private int rowStep(',
                      'private int rowTopAt(', 'private int rowTopOf(', 'private int firstVisibleRow(')
        harness = HARNESS.replace('__METHODS__', '\n'.join(method(SOURCE, sig) for sig in signatures))
        cases = ['key:'+case for case in KEYS]+['scroll:'+case for case in SCROLL]+['hit:'+case for case in HITS]+['sweep', 'visible_sweep']
        with tempfile.TemporaryDirectory() as tmp:
            sources = dict(STUBS)
            sources['WorldNavigationHarness.java'] = harness
            paths = []
            for name, text in sources.items():
                path = Path(tmp)/name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            for case in cases:
                with self.subTest(case=case):
                    subprocess.run(['java', '-cp', tmp, 'WorldNavigationHarness', case], check=True, timeout=30)
            print(f'World navigation: {len(cases)-2} scenarios plus row-lookup and visible-row sweeps passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
