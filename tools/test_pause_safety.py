#!/usr/bin/env python3
"""Run actual Pause layout/header/input/exit methods with GUI/session doubles.

Does not execute full decorative render/tick, real networking, saving, clipboard,
keyboard traversal, glyphs or Minecraft input. CI requires JDK 17 execution.
"""
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method
from test_grid_option_screens import STUBS as BASE_STUBS, PREFIX

ROOT = Path(__file__).resolve().parents[1]
SOURCE = (ROOT / f'src/main/java/{PREFIX}client/screens/DiaryPauseScreen.java').read_text()
STUBS = dict(BASE_STUBS)
STUBS.update({
    'net/minecraft/client/multiplayer/ClientLevel.java': '''package net.minecraft.client.multiplayer;
public class ClientLevel {public int disconnects;public Runnable hook;public void disconnect(){disconnects++;if(hook!=null)hook.run();}}''',
    'net/minecraft/server/MinecraftServer.java': '''package net.minecraft.server;
public class MinecraftServer {public World overworld(){return new World();}public static class World {public long getSeed(){return 123L;}}}''',
    'net/minecraft/client/gui/screens/PauseScreen.java': '''package net.minecraft.client.gui.screens;
import net.minecraft.client.gui.*;import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
public class PauseScreen extends Screen {
 public int parentRenders;public PauseScreen(boolean show){super(net.minecraft.network.chat.Component.literal("pause"));}
 public void render(GuiGraphics g,int x,int y,float partial){parentRenders++;}
 public void onClose(){minecraft.setScreen(null);}
 public boolean mouseClicked(double x,double y,int button){parentClicks++;if(button==0)for(var w:children())if(w.visible&&w.active&&w.isMouseOver(x,y)&&w instanceof DiaryButton b){b.onPress();return true;}return false;}
}''',
    'net/minecraft/client/gui/screens/TitleScreen.java': '''package net.minecraft.client.gui.screens;
public class TitleScreen extends Screen {public TitleScreen(){super(net.minecraft.network.chat.Component.literal("title"));}}''',
    'net/minecraft/client/gui/screens/GenericDirtMessageScreen.java': '''package net.minecraft.client.gui.screens;
public class GenericDirtMessageScreen extends Screen {public GenericDirtMessageScreen(net.minecraft.network.chat.Component text){super(text);}}''',
    PREFIX+'client/screens/DiaryConfirmScreen.java': '''package com.eternalcraft.gripeverde.client.screens;
import net.minecraft.network.chat.Component;import net.minecraft.client.gui.screens.Screen;import java.util.function.Consumer;
public class DiaryConfirmScreen extends Screen {public final Consumer<Boolean> callback;
 public DiaryConfirmScreen(Consumer<Boolean> callback,Component title,Component message,Component yes,Component no,Screen parent){super(title);this.callback=callback;}}
''',
    PREFIX+'client/screens/DiaryOptionsScreen.java': '''package com.eternalcraft.gripeverde.client.screens;
public class DiaryOptionsScreen extends net.minecraft.client.gui.screens.Screen {public DiaryOptionsScreen(net.minecraft.client.gui.screens.Screen parent){super(net.minecraft.network.chat.Component.literal("options"));}}''',
    PREFIX+'client/screens/DiaryDispatchScreen.java': '''package com.eternalcraft.gripeverde.client.screens;
public class DiaryDispatchScreen extends net.minecraft.client.gui.screens.Screen {public DiaryDispatchScreen(net.minecraft.client.gui.screens.Screen parent){super(net.minecraft.network.chat.Component.literal("dispatch"));}}''',
    PREFIX+'client/render/DiaryTheme.java': '''package com.eternalcraft.gripeverde.client.render;
public class DiaryTheme {public static final int INK=1,INK_BLACK=2,SEPIA_DARK=3,SEPIA_MID=4,BRONZE=5;public static int withAlpha(int c,int a){return c;}}''',
    'net/minecraft/client/gui/GuiGraphics.java': '''package net.minecraft.client.gui;
import java.util.*;
public class GuiGraphics {
 public record Text(String value,int x,int y,int width,int height,int color){}
 public record Fill(int x,int y,int right,int bottom){}
 public List<Text> texts=new ArrayList<>();public List<Fill> rectangles=new ArrayList<>();public List<String> events=new ArrayList<>();public List<Integer> dividers=new ArrayList<>();public int fills;
 public static class Pose {double tx,ty,sx=1,sy=1;Deque<double[]> stack=new ArrayDeque<>();
  public void pushPose(){stack.push(new double[]{tx,ty,sx,sy});}public void popPose(){double[] v=stack.pop();tx=v[0];ty=v[1];sx=v[2];sy=v[3];}
  public void translate(double x,double y,double z){tx+=x*sx;ty+=y*sy;}public void scale(float x,float y,float z){sx*=x;sy*=y;}}
 private Pose pose=new Pose();public Pose pose(){return pose;}
 public void drawString(Font font,String text,int x,int y,int color,boolean shadow){if(!text.isEmpty())texts.add(new Text(text,(int)Math.floor(pose.tx+x*pose.sx),(int)Math.floor(pose.ty+y*pose.sy),(int)Math.ceil(font.width(text)*pose.sx),(int)Math.ceil(font.lineHeight*pose.sy),color));}
 public void fill(int x,int y,int right,int bottom,int color){fills++;rectangles.add(new Fill(x,y,right,bottom));}
 public void enableScissor(int x,int y,int r,int b){}public void disableScissor(){}
}''',
})
name = 'net/minecraft/client/Minecraft.java'
STUBS[name] = STUBS[name].replace('public class Minecraft {', '''public class Minecraft {
 public net.minecraft.client.multiplayer.ClientLevel level=new net.minecraft.client.multiplayer.ClientLevel();
 public boolean local;public int clears,savingScreens;public net.minecraft.server.MinecraftServer server;
 public boolean isLocalServer(){return local;}
 public void clearLevel(){clears++;level=null;}
 public void clearLevel(net.minecraft.client.gui.screens.Screen saving){savingScreens++;clearLevel();}
 public net.minecraft.server.MinecraftServer getSingleplayerServer(){return server;}
 public Clipboard keyboardHandler=new Clipboard();public static class Clipboard {public int writes;public void setClipboard(String value){writes++;}}
''')
name = 'net/minecraft/client/gui/components/AbstractWidget.java'
STUBS[name] = STUBS[name].replace('public Component getMessage()', 'public boolean isMouseOver(double mx,double my){return mx>=x&&mx<x+width&&my>=y&&my<y+height;}public Component getMessage()')
name = PREFIX+'client/widgets/DiaryButton.java'
STUBS[name] = STUBS[name].replace('NONE,SCROLL,DOOR,HAND', 'NONE,SCROLL,DOOR,HAND,GEAR,CHAT').replace('PRIMARY,SECONDARY', 'PRIMARY,SECONDARY,DANGER')
name = PREFIX+'client/audio/UiSounds.java'
STUBS[name] = STUBS[name].replace('public static void open(){}', 'public static void type(){}public static void open(){}')
name = PREFIX+'client/audio/ModSounds.java'
STUBS[name] = STUBS[name].replace('public class ModSounds {', 'public class ModSounds {public static Value MENU_WOOD_CREAK=new Value(),UI_CLICK_HEAVY=new Value(),MENU_CROWS=new Value();')
name = PREFIX+'client/lore/Discoveries.java'
STUBS[name] = STUBS[name].replace('public class Discoveries {', 'public class Discoveries {public static final String CANDLE_DARK="candle",ITEM_FEATHER="feather",RAVEN_FEATHER="raven";')
name = PREFIX+'client/lore/ChroniclerMemory.java'
STUBS[name] = STUBS[name].replace('public class ChroniclerMemory {', 'public class ChroniclerMemory {public static boolean hasCollectible(String id){return false;}')
name = PREFIX+'client/lore/Marginalia.java'
STUBS[name] = STUBS[name].replace('public class Marginalia {', 'public class Marginalia {public static final String N_DOCTOR="doctor";')
name = PREFIX+'client/render/PaperTexture.java'
STUBS[name] = STUBS[name].replace('public class PaperTexture {', 'public class PaperTexture {public static void drawTornRim(net.minecraft.client.gui.GuiGraphics g,int x,int y,int w,int h,int color,int edge){}')
name = PREFIX+'client/render/DiaryChrome.java'
STUBS[name] = STUBS[name].replace('public class DiaryChrome {', 'public class DiaryChrome {public static String pauseTitle="Pause";').replace('public static String tr(String key){return key;}', 'public static String tr(String key){return key.equals("gripeverde.pause.title")?pauseTitle:key;}')

HARNESS = r'''import java.util.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import com.eternalcraft.gripeverde.client.screens.*;
import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import com.eternalcraft.gripeverde.client.render.*;
import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.lore.*;
public class PauseSafetyHarness {
 static class Pause extends PauseScreen {
  __CONSTANTS__
  boolean paperSoundDone,candleOut,leaveConfirmationOpen,disconnecting;long seedCopiedUntilMs,candleSmokeUntilMs;
  Font headerFont;int headerPanelW=-1,headerTitleW,headerSubW;boolean headerCompact,headerMinimal;
  String headerTitleSrc="",headerSubSrc="",headerTitleShown="",headerSubtitle="";float headerScale=1;
  Pause(){super(true);}
  // Factory double: layout/handlers are real, button drawing/sound are not.
  DiaryButton createPauseButton(int x,int y,int w,int h,String key,DiaryButton.Icon icon,DiaryButton.Variant variant,Runnable action){return new DiaryButton(x,y,w,h,Component.translatable(key),icon,variant,action);}
  __METHODS__
 }
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 static boolean overlap(int x,int y,int w,int h,AbstractWidget b){return x<b.x+b.width&&x+w>b.x&&y<b.y+b.height&&y+h>b.y;}
 static void verify(Pause p){
  var widgets=p.children();check(widgets.size()==4,"missing pause action");
  int px=p.panelX(),py=p.panelY(),pw=p.panelW(),ph=p.panelH();
  check(py>=0&&py+ph<=p.height-55,"paper covers hotbar");
  for(var a:widgets){check(a.x>=px&&a.x+a.width<=px+pw&&a.y>=py&&a.y+a.height<=py+ph,"pause control outside paper: "+p.width+"x"+p.height);
   for(var b:widgets)if(a!=b)check(!overlap(a.x,a.y,a.width,a.height,b),"pause controls overlap");}
  GuiGraphics g=new GuiGraphics();p.renderWorldSafe(g,0,0,0);check(p.parentRenders>=1,"world-safe render skipped widgets");
  for(var text:g.texts){check(text.x()>=px&&text.x()+text.width()<=px+pw&&text.y()>=py&&text.y()+text.height()<=py+ph,"pause header outside paper");
   for(var a:widgets)check(!overlap(text.x(),text.y(),text.width(),text.height(),a),"pause header overlaps controls");}
  GuiGraphics separators=new GuiGraphics();p.drawPanelSeparators(separators,px,py);
  for(var r:separators.rectangles)for(var a:widgets)check(!overlap(r.x(),r.y(),r.right()-r.x(),r.bottom()-r.y(),a),"pause separator intersects control");
 }
 static void geometry(){int cases=0;
  for(String title:new String[]{"Paused", "Una pausa con una traduccion larga para el registro"})for(int w:new int[]{160,161,200,280,320,800,1920})for(int h=120;h<=500;h++){
   DiaryChrome.pauseTitle=title;Pause p=new Pause();p.rebuild(w,h);verify(p);p.resize(p.minecraft,w,h);verify(p);cases++;
  }
  // Same font/width/compact flag across the minimal/nonminimal breakpoint.
  Pause p=new Pause();for(int h:new int[]{190,210,190,325,190}){p.resize(p.minecraft,800,h);verify(p);}
  System.out.println("Pause geometry: "+cases+" init/header/world-safe-render/resize cases plus cache transitions passed");
 }
 static void clickCenter(Pause p,int index){var b=p.children().get(index);check(p.mouseClicked(b.x+b.width/2.0,b.y+b.height/2.0,0),"button click unhandled");}
 static void scenario(String test){
  Pause p=new Pause();p.rebuild(800,600);p.minecraft.screen=p;ClientLevel original=p.minecraft.level;
  switch(test){
   case "resume"->{clickCenter(p,0);check(p.minecraft.screen==null,"resume did not close pause");}
   case "options"->{clickCenter(p,1);check(p.minecraft.screen instanceof DiaryOptionsScreen,"adjustments target changed");}
   case "dispatch"->{clickCenter(p,2);check(p.minecraft.screen instanceof DiaryDispatchScreen,"CEDA target changed");}
   case "world_hidden_decorations"->{
    p.minecraft.server=new net.minecraft.server.MinecraftServer();
    int px=p.panelX(),py=p.panelY(),pw=p.panelW(),ph=p.panelH();
    double[][] points={{px+pw-50,py+ph-90},{px+pw-30,py+30},{px+14,py+22},{px+20,py+ph-32},{px+pw/2,py+45}};
    for(var point:points)p.mouseClicked(point[0],point[1],0);
    check(UiSounds.custom==0&&Discoveries.calls==0&&Marginalia.queued==0&&!p.candleOut&&p.minecraft.keyboardHandler.writes==0,"hidden world decoration produced effects");}
   case "removed_seed_line"->{
    p.minecraft.level=null;p.minecraft.server=new net.minecraft.server.MinecraftServer();
    p.mouseClicked(p.panelX()+p.panelW()/2,p.panelY()+50,0);
    check(p.minecraft.keyboardHandler.writes==0,"removed status line still copied seed");}
   case "compact_hidden_raven", "compact_hidden_candle"->{
    p.rebuild(800,210);p.minecraft.level=null;int px=p.panelX(),py=p.panelY();
    double x=test.endsWith("raven")?px+14:px+20,y=test.endsWith("raven")?py+22:py+p.panelH()-32;
    p.mouseClicked(x,y,0);check(UiSounds.custom==0&&!p.candleOut,"hidden compact decoration remained interactive");}
   case "remote_accept", "remote_cancel", "duplicate_prompt", "changed_world_accept", "changed_world_cancel", "world_gone"->{
    p.onLeave();check(p.minecraft.screen instanceof DiaryConfirmScreen&&p.leaveConfirmationOpen,"remote leave skipped confirmation");
    var confirm=(DiaryConfirmScreen)p.minecraft.screen;
    if(test.equals("duplicate_prompt")){int transitions=p.minecraft.transitions;p.onLeave();check(p.minecraft.transitions==transitions&&p.minecraft.screen==confirm,"duplicate confirmation created");return;}
    boolean accept=!test.endsWith("cancel");
    ClientLevel next=null;
    if(test.startsWith("changed_world")){next=new ClientLevel();p.minecraft.level=next;p.minecraft.screen=new TitleScreen();}
    if(test.equals("world_gone")){p.minecraft.level=null;p.minecraft.screen=new TitleScreen();}
    Screen current=p.minecraft.screen;confirm.callback.accept(accept);
    if(test.startsWith("changed_world")||test.equals("world_gone")){check(p.minecraft.screen==current&&original.disconnects==0&&(next==null||next.disconnects==0),"stale decision changed another session/navigation");}
    else if(accept){check(original.disconnects==1&&p.minecraft.clears==1&&p.minecraft.savingScreens==0&&p.minecraft.screen instanceof TitleScreen,"remote accept did not disconnect once");}
    else {check(original.disconnects==0&&p.minecraft.screen==p&&!p.leaveConfirmationOpen,"cancel did not return to pause");p.onLeave();check(p.leaveConfirmationOpen,"cancel prevented retry");}
   }
   case "local_leave", "local_reentry", "duplicate_disconnect"->{
    p.minecraft.local=true;if(test.equals("local_reentry"))original.hook=p::disconnectAndReturnToTitle;
    p.onLeave();if(test.equals("duplicate_disconnect"))p.disconnectAndReturnToTitle();
    check(original.disconnects==1&&p.minecraft.clears==1&&p.minecraft.savingScreens==1&&p.minecraft.screen instanceof TitleScreen,"local disconnect did not save/return exactly once");}
   case "no_world"->{p.minecraft.level=null;p.onLeave();check(p.minecraft.transitions==0,"no-world leave navigated");}
   case "null_client"->{p.minecraft=null;p.onLeave();p.disconnectAndReturnToTitle();}
   case "secondary_world_click"->{p.mouseClicked(p.panelX()+14,p.panelY()+22,1);check(p.parentClicks==1&&UiSounds.custom==0,"secondary click intercepted");}
   default->throw new AssertionError(test);
  }
 }
 public static void main(String[] args){if(args[0].equals("geometry"))geometry();else scenario(args[0]);}
}
'''
CASES = ('resume', 'options', 'dispatch', 'world_hidden_decorations', 'removed_seed_line', 'compact_hidden_raven', 'compact_hidden_candle',
         'remote_accept', 'remote_cancel', 'duplicate_prompt', 'changed_world_accept', 'changed_world_cancel', 'world_gone',
         'local_leave', 'local_reentry', 'duplicate_disconnect', 'no_world', 'null_client', 'secondary_world_click')


class Wiring(unittest.TestCase):
    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_pause_safety.py --require-java', (ROOT / path).read_text())

    def test_live_input_guard_matches_render_path(self):
        body = method(SOURCE, 'public boolean mouseClicked(')
        self.assertLess(body.index('if (hasActiveWorld())'), body.index('bloodSpotAt('))
        self.assertIn('renderWorldSafe(', method(SOURCE, 'public void render('))


class Executable(unittest.TestCase):
    def test_pause_methods(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        signatures = ('private int panelW(', 'private int panelH(', 'private int panelX(', 'private int panelY(',
                      'private boolean minimalPauseLayout(', 'private boolean compactPauseLayout(', 'private int separatorTopOffset(',
                      'private int separatorBottomOffset(', 'protected void init(', 'private boolean overAnyButton(',
                      'private int bloodSpotAt(', 'public boolean mouseClicked(', 'private void resumeGame(',
                      'private void openAdjustments(', 'private void openDispatch(', 'private void onLeave(',
                      'private void disconnectAndReturnToTitle(', 'private boolean hasActiveWorld(', 'private void renderWorldSafe(',
                      'private void drawPanelTitle(', 'private static String ellipsizeTo(', 'private void drawDateStamp(',
                      'private void drawFlourish(', 'private void drawPanelSeparators(', 'private void drawSepLine(')
        constants = '\n'.join(re.findall(r'private static final int [A-Z_]+\s*=\s*[^;]+;', SOURCE))
        harness = HARNESS.replace('__CONSTANTS__', constants).replace('__METHODS__', '\n'.join(method(SOURCE, sig) for sig in signatures))
        with tempfile.TemporaryDirectory() as tmp:
            sources = dict(STUBS)
            sources['PauseSafetyHarness.java'] = harness
            paths = []
            for name, text in sources.items():
                path = Path(tmp)/name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            for case in CASES+('geometry',):
                with self.subTest(case=case):
                    subprocess.run(['java', '-Xmx256m', '-cp', tmp, 'PauseSafetyHarness', case], check=True, timeout=60)
            print(f'Pause safety: {len(CASES)} lifecycle/input scenarios and geometry sweep passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
