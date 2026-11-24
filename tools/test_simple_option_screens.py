#!/usr/bin/env python3
"""Compile full Chat/Online classes with GUI/options doubles.

Checks geometry, translated footer replacement, drawing order and navigation.
Sound skip eligibility uses the extracted helper plus source-wiring checks;
complete music-engine lifecycle is tested separately in test_menu_music.py.
No Minecraft pixels, real input traversal, Realms calls or disk saves.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method
from test_option_screen_safety import STUBS as BASE_STUBS, PREFIX

ROOT = Path(__file__).resolve().parents[1]
SOURCES = {name: (ROOT / f'src/main/java/{PREFIX}client/screens/{name}.java').read_text()
           for name in ('DiaryChatOptionsScreen', 'DiaryOnlineOptionsScreen', 'DiarySoundOptionsScreen')}
STUBS = dict(BASE_STUBS)
STUBS.update({
    'net/minecraft/client/gui/components/events/GuiEventListener.java': 'package net.minecraft.client.gui.components.events;public interface GuiEventListener {}',
    'net/minecraft/client/OptionInstance.java': '''package net.minecraft.client;
public class OptionInstance<T>{private T value;public OptionInstance(T value){this.value=value;}public T get(){return value;}public void set(T v){value=v;}}''',
    'net/minecraft/client/Options.java': '''package net.minecraft.client;
public class Options {
 public int saves;public void save(){saves++;}
 private OptionInstance<Double> opacity=new OptionInstance<>(.75),width=new OptionInstance<>(.5),scale=new OptionInstance<>(1.0);
 private OptionInstance<Boolean> realms=new OptionInstance<>(true),listing=new OptionInstance<>(false);
 public OptionInstance<Double> chatOpacity(){return opacity;}public OptionInstance<Double> chatWidth(){return width;}public OptionInstance<Double> chatScale(){return scale;}
 public OptionInstance<Boolean> realmsNotifications(){return realms;}public OptionInstance<Boolean> allowServerListing(){return listing;}
}''',
    'net/minecraft/client/Minecraft.java': '''package net.minecraft.client;
import net.minecraft.client.gui.screens.Screen;
public class Minecraft {public Screen screen;public int transitions;public Options options=new Options();
 public void setScreen(Screen next){if(screen!=null)screen.removed();screen=next;transitions++;}}
''',
    'net/minecraft/network/chat/CommonComponents.java': '''package net.minecraft.network.chat;
public class CommonComponents {public static Component GUI_DONE=Component.literal("Done");}''',
    'net/minecraft/client/gui/screens/Screen.java': '''package net.minecraft.client.gui.screens;
import java.util.*;import net.minecraft.client.gui.*;import net.minecraft.client.gui.components.*;import net.minecraft.network.chat.Component;
public class Screen {
 public int width=800,height=600;public Font font=new Font();public Component title;public int parentClicks,parentWheels;
 public net.minecraft.client.Minecraft minecraft=new net.minecraft.client.Minecraft();
 public List<Renderable> renderables=new ArrayList<>();private List<AbstractWidget> children=new ArrayList<>();
 public Screen(Component title){this.title=title;}protected void init(){}
 public void rebuild(int w,int h){width=w;height=h;renderables.clear();children.clear();init();}
 public void resize(net.minecraft.client.Minecraft mc,int w,int h){rebuild(w,h);}
 public List<AbstractWidget> children(){return children;}
 protected <T extends AbstractWidget>T addRenderableWidget(T child){renderables.add(child);children.add(child);return child;}
 protected <T extends AbstractWidget>T addWidget(T child){children.add(child);return child;}
 public void tick(){}public void render(GuiGraphics g,int x,int y,float partial){}public void renderBackground(GuiGraphics g){}
 public void removed(){}public void onClose(){}
 public boolean mouseClicked(double x,double y,int button){parentClicks++;return false;}
 public boolean mouseScrolled(double x,double y,double delta){parentWheels++;return false;}
}''',
    'net/minecraft/client/gui/screens/SimpleOptionsSubScreen.java': '''package net.minecraft.client.gui.screens;
import net.minecraft.client.*;import net.minecraft.client.gui.components.*;import net.minecraft.network.chat.*;
public class SimpleOptionsSubScreen extends Screen {
 protected final Screen lastScreen;protected final Options options;protected OptionsList list;
 public SimpleOptionsSubScreen(Screen parent,Options options,Component title,OptionInstance<?>[] opts){super(title);lastScreen=parent;this.options=options;}
 protected void init(){list=new OptionsList();addWidget(list);Button done=new Button();done.message=CommonComponents.GUI_DONE;addRenderableWidget(done);}
 public OptionsList testList(){return list;}
 public void onClose(){minecraft.setScreen(lastScreen);}public void removed(){options.save();}
}''',
    'net/minecraft/client/gui/screens/ChatOptionsScreen.java': '''package net.minecraft.client.gui.screens;
public class ChatOptionsScreen extends SimpleOptionsSubScreen {
 public ChatOptionsScreen(Screen parent,net.minecraft.client.Options options){super(parent,options,net.minecraft.network.chat.Component.literal("Chat"),new net.minecraft.client.OptionInstance<?>[0]);}
}''',
    PREFIX+'client/audio/MenuMusic.java': '''package com.eternalcraft.gripeverde.client.audio;
public class MenuMusic {public static boolean active,cross;public static boolean isActive(){return active;}public static boolean isCrossfading(){return cross;}}''',
})
name = PREFIX+'client/util/SmoothScroll.java'
STUBS[name] = STUBS[name].replace('public class SmoothScroll {', 'public class SmoothScroll {public SmoothScroll(){}public SmoothScroll(double easing,double threshold){}')
name = 'net/minecraft/client/gui/components/AbstractWidget.java'
STUBS[name] = STUBS[name].replace('implements Renderable', 'implements Renderable,net.minecraft.client.gui.components.events.GuiEventListener').replace('public void setX(int x){this.x=x;}', 'public void setX(int x){this.x=x;}public void setY(int y){this.y=y;}')
name = 'net/minecraft/client/gui/components/OptionsList.java'
STUBS[name] = STUBS[name].replace('public class OptionsList extends AbstractWidget {', '''public class OptionsList extends AbstractWidget {
 public int renders;public void render(GuiGraphics g,int x,int y,float tick){renders++;g.events.add("list");}''')
name = 'net/minecraft/client/gui/GuiGraphics.java'
STUBS[name] = STUBS[name].replace('public class GuiGraphics {', '''public class GuiGraphics {
 public java.util.List<String> events=new java.util.ArrayList<>();public java.util.List<Integer> dividers=new java.util.ArrayList<>();''')
name = PREFIX+'client/render/PaperTexture.java'
STUBS[name] = STUBS[name].replace('{OFFICIAL}', '{OFFICIAL,CORRESPONDENCE}')
name = PREFIX+'client/render/DiaryChrome.java'
STUBS[name] = STUBS[name].replace('public class DiaryChrome {', '''public class DiaryChrome {
 public static void drawInkDividerBetween(GuiGraphics g,int left,int right,int y){g.dividers.add(y);}''').replace('public static void paintOptionRowsInk(GuiGraphics g,Font f,Object list,int x,int y){}', 'public static void paintOptionRowsInk(GuiGraphics g,Font f,Object list,int x,int y){g.events.add("ink");}')

HARNESS = r'''import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.*;
import com.eternalcraft.gripeverde.client.screens.*;
import com.eternalcraft.gripeverde.client.render.QuarantineSceneRenderer;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.lore.Discoveries;
public class SimpleOptionHarness {
 static class Sound {__SKIP__}
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 static SimpleOptionsSubScreen screen(String type,Screen parent,Options options){return type.equals("chat")?new DiaryChatOptionsScreen(parent,options):new DiaryOnlineOptionsScreen(parent,options);}
 static boolean overlaps(int x,int y,int w,int h,AbstractWidget b){return x<b.x+b.width&&x+w>b.x&&y<b.y+b.height&&y+h>b.y;}
 static void geometry(){
  int cases=0;
  for(String type:new String[]{"chat","online"})for(boolean en:new boolean[]{true,false})for(int width:new int[]{160,161,200,320,380,800,1920})for(int height=120;height<=420;height++){
   Discoveries.english=en;var s=screen(type,null,new Options());s.rebuild(width,height);var list=s.testList();
   check(list.height>=25&&list.y>=0&&list.getBottom()<=height,"invalid list viewport: "+type+" "+height);
   var buttons=s.children().stream().filter(w->w.visible&&w!=list).toList();check(buttons.size()==1,"wrong footer count");var back=buttons.get(0);
   check(back.x>=0&&back.y>=0&&back.x+back.width<=width&&back.y+back.height<=height,"return button outside screen");
   check(!overlaps(back.x,back.y,back.width,back.height,list),"list overlaps return button");
   GuiGraphics g=new GuiGraphics();s.render(g,0,0,0);
   check(list.renders==1&&g.events.equals(List.of("list","ink")),"list render/ink order wrong");
   for(var text:g.texts){check(!overlaps(text.x(),text.y(),text.width(),text.height(),list),"header text overlaps list");check(!overlaps(text.x(),text.y(),text.width(),text.height(),back),"header text overlaps footer");}
   for(int y:g.dividers)check(y<=list.getTop()||y>=list.getBottom(),"divider cuts through vanilla options");
   s.resize(s.minecraft,width,height);check(s.testList().height>=25&&s.children().stream().filter(w->w.visible&&w!=s.testList()).count()==1,"resize duplicated footer/lost list");cases++;
  }
  System.out.println("Simple option screens: "+cases+" complete-class geometry/render/resize cases passed");
 }
 static void translated(String type,String label){
  CommonComponents.GUI_DONE=Component.literal(label);var s=screen(type,null,new Options());s.rebuild(800,600);
  var original=s.children().stream().filter(w->w instanceof Button).findFirst().orElseThrow();
  check(!original.visible&&!original.active,"translated vanilla Done was not hidden: "+label);
  check(s.children().stream().filter(w->w.visible&&w!=s.testList()).count()==1,"translated footer duplicated");
 }
 static void navigation(String type){
  var parent=new Screen(Component.literal("parent"));var options=new Options();var s=screen(type,parent,options);s.minecraft.screen=s;
  s.rebuild(160,120);s.onClose();check(s.minecraft.screen==parent&&s.minecraft.transitions==1,"close did not return to parent");
  check(options.saves==1,"normal screen removal did not persist vanilla options once");
 }
 static void skip(){
  Sound s=new Sound();int cases=0;
  for(boolean active:new boolean[]{false,true})for(boolean cross:new boolean[]{false,true})for(boolean world:new boolean[]{false,true}){
   MenuMusic.active=active;MenuMusic.cross=cross;QuarantineSceneRenderer.live=world;
   check(s.canSkipMenuTrack()==(active&&!cross&&!world),"skip eligibility ignored live-world/playback state");cases++;
  }
  System.out.println("Skip eligibility: "+cases+" state combinations passed");
 }
 public static void main(String[] args){if(args[0].equals("geometry"))geometry();else if(args[0].equals("skip"))skip();else if(args[0].equals("navigation"))navigation(args[1]);else translated(args[1],args[2]);}
}
'''


class Wiring(unittest.TestCase):
    def test_ci_requires_execution(self):
        for name in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_simple_option_screens.py --require-java', (ROOT / name).read_text())

    def test_skip_assignments_share_world_guard(self):
        sound = SOURCES['DiarySoundOptionsScreen']
        import re
        assignments = re.findall(r'this\.skipTrackButton\.active\s*=\s*([^;]+);', sound)
        self.assertEqual(['this.canSkipMenuTrack()', 'this.canSkipMenuTrack()'], assignments)


class Executable(unittest.TestCase):
    def test_screens(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        with tempfile.TemporaryDirectory() as tmp:
            sources = dict(STUBS)
            for name in ('DiaryChatOptionsScreen', 'DiaryOnlineOptionsScreen'):
                sources[PREFIX+'client/screens/'+name+'.java'] = SOURCES[name]
            sources['SimpleOptionHarness.java'] = HARNESS.replace('__SKIP__', method(SOURCES['DiarySoundOptionsScreen'], 'private boolean canSkipMenuTrack('))
            paths = []
            for name, text in sources.items():
                path = Path(tmp)/name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            cases = [('geometry',), ('skip',)]
            cases += [('navigation', kind) for kind in ('chat', 'online')]
            cases += [('translated', kind, label) for kind in ('chat', 'online') for label in ('Done', 'Listo', 'Fertig', 'Terminé', '完了', 'Готово')]
            for case in cases:
                with self.subTest(case=case):
                    subprocess.run(['java', '-Xmx256m', '-cp', tmp, 'SimpleOptionHarness', *case], check=True, timeout=60)
            print('Simple options: 12 translated footer cases, 2 navigation cases and 8 skip states passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
