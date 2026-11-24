#!/usr/bin/env python3
"""Accessibility geometry and sound-screen secret lifecycle regressions.

Compiles the full accessibility class and real extracted sound methods against
GUI/config/audio doubles. No native rendering/input, real sound or Forge saves.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method
from test_server_form_layout import STUBS as GUI_STUBS
from test_house_audio import CONFIG, FORGE

ROOT = Path(__file__).resolve().parents[1]
PREFIX = 'com/eternalcraft/gripeverde/'
ACCESS = (ROOT / f'src/main/java/{PREFIX}client/screens/DiaryAccessibilityOptionsScreen.java').read_text()
SOUND = (ROOT / f'src/main/java/{PREFIX}client/screens/DiarySoundOptionsScreen.java').read_text()
STUBS = dict(GUI_STUBS)
STUBS.update({
    'net/minecraft/client/Options.java': 'package net.minecraft.client;public class Options {}',
    'net/minecraft/client/gui/components/AbstractWidget.java': '''package net.minecraft.client.gui.components;
import net.minecraft.network.chat.Component;import net.minecraft.client.gui.*;
public class AbstractWidget implements Renderable {
 public boolean active=true,visible=true;public int x,y,width,height;public Component message;
 public AbstractWidget(int x,int y,int w,int h,Component label){this.x=x;this.y=y;width=w;height=h;message=label;}
 public int getX(){return x;}public int getY(){return y;}public int getHeight(){return height;}public void setX(int x){this.x=x;}
 public Component getMessage(){return message;}public void render(GuiGraphics g,int x,int y,float tick){}
 public void setTooltip(Tooltip tooltip){}
}''',
    'net/minecraft/client/gui/components/Button.java': '''package net.minecraft.client.gui.components;
public class Button extends AbstractWidget {public Button(){super(0,0,100,20,net.minecraft.network.chat.Component.literal("done"));}}''',
    'net/minecraft/client/gui/components/OptionsList.java': '''package net.minecraft.client.gui.components;
import net.minecraft.client.gui.*;import net.minecraft.network.chat.Component;
public class OptionsList extends AbstractWidget {
 public OptionsList(){super(0,30,800,100,Component.literal("list"));}
 public void setRenderBackground(boolean b){}public void setRenderTopAndBottom(boolean b){}public void setRenderSelection(boolean b){}
 public int getTop(){return y;}public int getBottom(){return y+height;}
 public void updateSize(int w,int h,int top,int bottom){width=w;y=top;height=bottom-top;}
 public java.util.List<Object> children(){return java.util.List.of(1,2,3);}
}''',
    'net/minecraft/client/gui/screens/AccessibilityOptionsScreen.java': '''package net.minecraft.client.gui.screens;
import net.minecraft.client.gui.components.*;import net.minecraft.network.chat.Component;
public class AccessibilityOptionsScreen extends Screen {
 protected OptionsList list;
 public AccessibilityOptionsScreen(Screen parent,net.minecraft.client.Options options){super(Component.literal("access"));}
 protected void init(){list=new OptionsList();addRenderableWidget(list);addRenderableWidget(new Button());}
 public OptionsList testList(){return list;}
}''',
    PREFIX+'client/widgets/DiaryToggle.java': '''package com.eternalcraft.gripeverde.client.widgets;
import java.util.function.*;import net.minecraft.client.gui.components.AbstractWidget;import net.minecraft.network.chat.Component;
public class DiaryToggle extends AbstractWidget {
 public final Supplier<Boolean> getter;public final Consumer<Boolean> setter;
 public DiaryToggle(int x,int y,int w,int h,Supplier<Boolean> getter,Consumer<Boolean> setter,Component label,Function<Boolean,Component> formatter,Runnable changed){super(x,y,w,h,label);this.getter=getter;this.setter=setter;}
 public void onPress(){setter.accept(!getter.get());}
}''',
    PREFIX+'config/ClientConfig.java': CONFIG,
    'net/minecraftforge/common/ForgeConfigSpec.java': FORGE,
    PREFIX+'client/render/QuarantineSceneRenderer.java': '''package com.eternalcraft.gripeverde.client.render;
public class QuarantineSceneRenderer {public static boolean live;public static boolean hasActiveWorld(){return live;}public static void tick(float dt){}public static void renderDiaryBackground(net.minecraft.client.gui.GuiGraphics g,int w,int h){}}''',
    PREFIX+'client/audio/UiSounds.java': '''package com.eternalcraft.gripeverde.client.audio;
public class UiSounds {public static int custom;public static boolean fail;public static Runnable hook;
 public static void open(){}public static void back(){}public static void scroll(){}
 public static void playCustom(Object sound,float volume,float pitch){custom++;if(hook!=null)hook.run();if(fail)throw new IllegalStateException("audio");}
}''',
    PREFIX+'client/audio/ModSounds.java': '''package com.eternalcraft.gripeverde.client.audio;
public class ModSounds {public static Value UI_SUCCESS=new Value(),MENU_CHAIN=new Value(),MENU_AMBIENT_OCCASIONAL=new Value();public static class Value{public Object get(){return this;}}}''',
    PREFIX+'client/audio/MenuMusic.java': '''package com.eternalcraft.gripeverde.client.audio;
public class MenuMusic {public static float volume;public static float getUserVolume(){return volume;}}''',
    PREFIX+'client/lore/Discoveries.java': '''package com.eternalcraft.gripeverde.client.lore;
public class Discoveries {public static final String SOUND_KEEPER="fork",SILENCE_LISTENS="silence",EYE_FOLLOWS="eye";
 public static int calls;public static String last;public static boolean english=true;
 public static boolean isEnglish(){return english;}public static boolean discover(String id){calls++;last=id;return true;}
 public static void drawToast(net.minecraft.client.gui.GuiGraphics g,int w){}
}''',
    PREFIX+'client/lore/Marginalia.java': '''package com.eternalcraft.gripeverde.client.lore;
public class Marginalia {public static final String N_TUNING="fork",N_SILENCE="silence";public static int queued;
 public static void queueOnce(String id){queued++;}public static void poll(){}public static void drawHorizontal(net.minecraft.client.gui.GuiGraphics g,int x,int y){}}
''',
    PREFIX+'client/util/SmoothScroll.java': '''package com.eternalcraft.gripeverde.client.util;
public class SmoothScroll {public int ticks;public void snapToCurrent(Object list){}public void tick(Object list){ticks++;}public boolean wheel(Object list,double delta,double pixels){return true;}}''',
    PREFIX+'client/util/MotionPolicy.java': '''package com.eternalcraft.gripeverde.client.util;
public class MotionPolicy {public static boolean reduced;public static boolean isReduced(){return reduced;}}''',
})
# Add only the drawing/dispatch API required by the complete accessibility class.
name = 'net/minecraft/client/gui/GuiGraphics.java'
STUBS[name] = STUBS[name].replace('public class GuiGraphics {', '''public class GuiGraphics {
 public int fills;public void enableScissor(int x,int y,int r,int b){}public void disableScissor(){}''').replace('public void fill(int x,int y,int right,int bottom,int color){}', 'public void fill(int x,int y,int right,int bottom,int color){fills++;}')
name = 'net/minecraft/client/gui/screens/Screen.java'
STUBS[name] = STUBS[name].replace('public class Screen {', 'public class Screen {public int parentClicks;').replace('public boolean mouseClicked(double x,double y,int button){return false;}', '''public boolean mouseClicked(double x,double y,int button){parentClicks++;return false;}
 public boolean mouseScrolled(double x,double y,double delta){return false;}''')
name = PREFIX+'client/render/DiaryChrome.java'
STUBS[name] = STUBS[name].replace('public class DiaryChrome {', '''public class DiaryChrome {
 public static String fitCached(Font f,String s,int w){return shorten(f,s,w);}
 public static void washVanillaRows(GuiGraphics g,Object list){}
 public static void paintOptionRowsInk(GuiGraphics g,Font f,Object list,int x,int y){}
 public static void drawStyledScrollThumbInPanel(GuiGraphics g,Object list,int x){}''')

HARNESS = r'''import java.util.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import com.eternalcraft.gripeverde.client.screens.DiaryAccessibilityOptionsScreen;
import com.eternalcraft.gripeverde.client.widgets.*;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import com.eternalcraft.gripeverde.client.audio.*;
import com.eternalcraft.gripeverde.client.lore.*;
import com.eternalcraft.gripeverde.client.util.*;
import com.eternalcraft.gripeverde.config.ClientConfig;
public class OptionSafetyHarness {
 static class Parent {int clicks,ticks;public void tick(){ticks++;}public boolean mouseClicked(double x,double y,int button){clicks++;return false;}}
 static class Sound extends Parent {
  AbstractWidget ambienceSlider=new AbstractWidget(100,100,80,20,net.minecraft.network.chat.Component.literal("ambience"));
  boolean forkRinging,silenceWaiting,silenceHeard;long forkRingUntilMs,silenceStartMs;Object reflectedList;
  SmoothScroll smoothScroll=new SmoothScroll();
  __SOUND__
 }
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 static void clicks(Sound s){check(s.mouseClicked(84,110,0),"fork click unhandled");}
 static void heard(Sound s){check(s.silenceHeard&&UiSounds.custom==3&&Discoveries.calls==1&&Discoveries.last.equals("silence"),"silence should fire once with three beats");}
 static void sound(String test){
  Sound s=new Sound();net.minecraft.Util.now=0;
  switch(test){
   case "fork_before_render" -> {clicks(s);check(UiSounds.custom==2&&Discoveries.calls==1,"fresh fork requires a prior render");}
   case "fork_resize" -> {GuiGraphics g=new GuiGraphics();s.drawTuningFork(g,0,0);s.ambienceSlider.x=200;s.ambienceSlider.y=200;
    check(!s.mouseClicked(84,110,0)&&UiSounds.custom==0,"old render hit box remained active");
    check(s.mouseClicked(184,210,0)&&UiSounds.custom==2,"new position not clickable before render");}
   case "fork_repeats", "fork_negative_clock" -> {
    if(test.equals("fork_negative_clock"))net.minecraft.Util.now=-1000;
    clicks(s);long until=s.forkRingUntilMs;for(int i=0;i<10;i++)clicks(s);
    check(UiSounds.custom==2&&Discoveries.calls==1&&s.forkRingUntilMs==until,"fork cooldown repeated effects or extended deadline");
    net.minecraft.Util.now+=699;clicks(s);check(UiSounds.custom==2,"cooldown expired early");
    net.minecraft.Util.now++;clicks(s);check(UiSounds.custom==4&&Discoveries.calls==2,"fork cooldown did not expire");}
   case "fork_live", "fork_hidden", "fork_disabled", "fork_null" -> {
    if(test.equals("fork_live"))QuarantineSceneRenderer.live=true;
    if(test.equals("fork_hidden"))s.ambienceSlider.visible=false;
    if(test.equals("fork_disabled"))s.ambienceSlider.active=false;
    if(test.equals("fork_null"))s.ambienceSlider=null;
    s.ringTuningFork();check(!s.mouseClicked(84,110,0),"unavailable fork swallowed normal click");GuiGraphics g=new GuiGraphics();s.drawTuningFork(g,84,110);
    check(UiSounds.custom==0&&Discoveries.calls==0&&g.fills==0,"unavailable fork sounded/drew/discovered");}
   case "fork_secondary", "fork_outside" -> {
    check(!s.mouseClicked(test.equals("fork_outside")?0:84,110,test.equals("fork_secondary")?1:0),"other click swallowed");check(UiSounds.custom==0&&s.clicks==1,"normal dispatch changed");}
   case "fork_reentrant" -> {UiSounds.hook=s::ringTuningFork;clicks(s);check(UiSounds.custom==2&&Discoveries.calls==1,"reentry repeated fork effects");}
   case "fork_audio_failure" -> {UiSounds.fail=true;clicks(s);check(Discoveries.calls==1&&s.remainingForkRingMs()==700,"optional audio failure lost lore/cooldown");clicks(s);check(UiSounds.custom==1,"audio failure defeated cooldown");}
   case "fork_draw_no_effects" -> {GuiGraphics g=new GuiGraphics();for(int i=0;i<10;i++)s.drawTuningFork(g,84,110);check(UiSounds.custom==0&&Discoveries.calls==0&&g.fills>0,"draw triggered effects or failed to draw");}
   case "fork_reduced" -> {MotionPolicy.reduced=true;clicks(s);GuiGraphics g=new GuiGraphics();s.drawTuningFork(g,84,110);check(UiSounds.custom==2&&g.fills>0,"reduced motion disabled fork function");}
   case "silence_zero", "silence_negative", "silence_once" -> {
    if(test.equals("silence_negative"))net.minecraft.Util.now=-10000;
    s.tick();net.minecraft.Util.now+=2999;s.tick();check(!s.silenceHeard&&UiSounds.custom==0,"silence fired early");
    net.minecraft.Util.now++;s.tick();heard(s);net.minecraft.Util.now+=10000;s.tick();heard(s);
    check(s.ticks==4&&s.smoothScroll.ticks==4,"tick did not preserve parent/scroll updates");}
   case "silence_volume_reset" -> {
    s.tick();net.minecraft.Util.now=2000;MenuMusic.volume=1;s.tick();check(!s.silenceWaiting,"audible music did not cancel timer");
    MenuMusic.volume=0;net.minecraft.Util.now=3000;s.tick();net.minecraft.Util.now=5999;s.tick();check(!s.silenceHeard,"volume reset reused old elapsed time");
    net.minecraft.Util.now=6000;s.tick();heard(s);}
   case "silence_world_reset" -> {
    s.tick();net.minecraft.Util.now=2000;QuarantineSceneRenderer.live=true;s.tick();check(!s.silenceWaiting,"world transition did not reset timer");
    net.minecraft.Util.now=9000;s.tick();check(UiSounds.custom==0&&Discoveries.calls==0,"silence effect fired in a world");
    QuarantineSceneRenderer.live=false;s.tick();net.minecraft.Util.now=11999;s.tick();check(!s.silenceHeard,"world exit reused previous timer");
    net.minecraft.Util.now=12000;s.tick();heard(s);}
   case "silence_loud" -> {MenuMusic.volume=.01f;s.tick();net.minecraft.Util.now=10000;s.tick();check(!s.silenceWaiting&&!s.silenceHeard&&UiSounds.custom==0,"audible music triggered silence");}
   default -> throw new AssertionError(test);
  }
 }
 static boolean overlap(AbstractWidget a,AbstractWidget b){return a.x<b.x+b.width&&a.x+a.width>b.x&&a.y<b.y+b.height&&a.y+a.height>b.y;}
 static void accessSweep(){
  int cases=0;
  for(boolean en:new boolean[]{true,false})for(int width:new int[]{160,161,200,320,404,800,1920})for(int height=120;height<=440;height++){
   Discoveries.english=en;
   var s=new DiaryAccessibilityOptionsScreen(null,new net.minecraft.client.Options());s.rebuild(width,height);
   var list=s.testList();check(list.getTop()>=0&&list.getBottom()<=height&&list.height>=25,"inaccessible list at "+width+"x"+height);
   List<AbstractWidget> controls=s.children().stream().filter(w->w.visible&&w!=list).toList();
   check(controls.size()==3,"missing accessibility controls");
   for(var a:controls){
    check(a.x>=0&&a.y>=0&&a.x+a.width<=width&&a.y+a.height<=height,"control outside viewport");
    check(!overlap(a,list),"list/control overlap at "+width+"x"+height);
    for(var b:controls)if(a!=b)check(!overlap(a,b),"footer controls overlap");
   }
   GuiGraphics g=new GuiGraphics();s.render(g,0,0,0);
   // Tests our text calls and geometry; chrome decorations/real glyphs are doubles.
   for(var t:g.texts)for(var a:controls)check(!(t.x()<a.x+a.width&&t.x()+t.width()>a.x&&t.y()<a.y+a.height&&t.y()+t.height()>a.y),"header text on footer control");
   s.resize(s.minecraft,width,height);check(s.testList().height>=25,"resize lost usable list");cases++;
  }
  System.out.println("Accessibility: "+cases+" complete-class init/render/resize geometry cases passed");
 }
 static void accessToggle(){
  var s=new DiaryAccessibilityOptionsScreen(null,new net.minecraft.client.Options());s.rebuild(160,120);
  var toggles=s.children().stream().filter(w->w instanceof DiaryToggle).map(w->(DiaryToggle)w).toList();
  boolean initial=ClientConfig.REDUCED_MOTION.get();toggles.get(0).onPress();check(ClientConfig.REDUCED_MOTION.get()!=initial&&ClientConfig.REDUCED_MOTION.saves==1,"motion toggle did not persist");
  initial=ClientConfig.REDUCE_FLASHES.get();toggles.get(1).onPress();check(ClientConfig.REDUCE_FLASHES.get()!=initial&&ClientConfig.REDUCE_FLASHES.saves==1,"flashes toggle did not persist");
 }
 static void hiddenEye(){
  var s=new DiaryAccessibilityOptionsScreen(null,new net.minecraft.client.Options());s.rebuild(160,120);s.render(new GuiGraphics(),0,0,0);
  // 160x120 -> panel (12,4), old eye at (34,20). It is now hidden in tight mode.
  check(!s.mouseClicked(34,20,0)&&s.parentClicks==1&&UiSounds.custom==0&&Discoveries.calls==0,"hidden eye kept its hit box");
 }
 public static void main(String[] args){if(args[0].equals("access_sweep"))accessSweep();else if(args[0].equals("access_toggle"))accessToggle();else if(args[0].equals("hidden_eye"))hiddenEye();else sound(args[0]);}
}
'''
CASES = ('fork_before_render', 'fork_resize', 'fork_repeats', 'fork_negative_clock', 'fork_live', 'fork_hidden',
         'fork_disabled', 'fork_null', 'fork_secondary', 'fork_outside', 'fork_reentrant', 'fork_audio_failure',
         'fork_draw_no_effects', 'fork_reduced', 'silence_zero', 'silence_negative', 'silence_once',
         'silence_volume_reset', 'silence_world_reset', 'silence_loud', 'access_toggle', 'hidden_eye')


class Wiring(unittest.TestCase):
    def test_ci_requires_execution(self):
        for name in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_option_screen_safety.py --require-java', (ROOT / name).read_text())

    def test_no_render_cached_hit_box_or_wall_clock_in_sound(self):
        self.assertNotIn('forkRect', SOUND)
        self.assertNotIn('System.currentTimeMillis()', SOUND)


class Executable(unittest.TestCase):
    def test_screen_methods(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        signatures = ('private long remainingForkRingMs(', 'private int[] tuningForkBounds(', 'private void drawTuningFork(',
                      'private void ringTuningFork(', 'public boolean mouseClicked(', 'public void tick(')
        harness = HARNESS.replace('__SOUND__', '\n'.join(method(SOUND, sig) for sig in signatures))
        with tempfile.TemporaryDirectory() as tmp:
            sources = dict(STUBS)
            sources[PREFIX+'client/screens/DiaryAccessibilityOptionsScreen.java'] = ACCESS
            sources['OptionSafetyHarness.java'] = harness
            paths = []
            for name, text in sources.items():
                path = Path(tmp)/name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            for case in CASES+('access_sweep',):
                with self.subTest(case=case):
                    subprocess.run(['java', '-Xmx256m', '-cp', tmp, 'OptionSafetyHarness', case], check=True, timeout=45)
            print(f'Option screens: {len(CASES)} lifecycle scenarios and accessibility geometry sweep passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
