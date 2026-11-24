#!/usr/bin/env python3
"""Compile complete Controls/Skin screens against GUI/options/lore doubles.

Tests widget/text/divider geometry, resize, real skin handlers and hidden badges.
No native pixels, actual glyphs, OS input, server settings or disk persistence.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_simple_option_screens import STUBS as BASE_STUBS, PREFIX
from test_language_screen import method

ROOT = Path(__file__).resolve().parents[1]
SOURCES = {name: (ROOT / f'src/main/java/{PREFIX}client/screens/{name}.java').read_text()
           for name in ('DiaryControlsScreen', 'DiarySkinCustomizationScreen')}
TOGGLE = (ROOT / f'src/main/java/{PREFIX}client/widgets/DiaryToggle.java').read_text()
STUBS = dict(BASE_STUBS)
STUBS.update({
    'net/minecraft/network/chat/Component.java': '''package net.minecraft.network.chat;
public class Component {private String value;public Component(String v){value=v;}public static Component literal(String s){return new Component(s);}public static Component translatable(String s){return new Component(s);}
 public String getString(){return value;}public Component copy(){return new Component(value);}public Component append(String s){value+=s;return this;}public Component append(Component s){return append(s.getString());}}''',
    'net/minecraft/client/KeyMapping.java': '''package net.minecraft.client;
public class KeyMapping {public Key getKey(){return new Key();}public static class Key {public String getName(){return "key.keyboard.unknown";}}}''',
    'net/minecraft/client/gui/components/CycleButton.java': 'package net.minecraft.client.gui.components;public class CycleButton {}',
    'net/minecraft/client/gui/screens/MouseSettingsScreen.java': '''package net.minecraft.client.gui.screens;
public class MouseSettingsScreen extends Screen {public MouseSettingsScreen(Screen parent,net.minecraft.client.Options options){super(net.minecraft.network.chat.Component.literal("mouse"));}}''',
    'net/minecraft/client/gui/screens/controls/KeyBindsScreen.java': '''package net.minecraft.client.gui.screens.controls;
public class KeyBindsScreen extends net.minecraft.client.gui.screens.Screen {public KeyBindsScreen(net.minecraft.client.gui.screens.Screen parent,net.minecraft.client.Options options){super(net.minecraft.network.chat.Component.literal("keys"));}}''',
    'net/minecraft/world/entity/HumanoidArm.java': '''package net.minecraft.world.entity;
public enum HumanoidArm {LEFT,RIGHT;public net.minecraft.network.chat.Component getCaption(){return net.minecraft.network.chat.Component.literal(name());}}''',
    'net/minecraft/world/entity/player/PlayerModelPart.java': '''package net.minecraft.world.entity.player;
public enum PlayerModelPart {CAPE,JACKET,LEFT_SLEEVE,RIGHT_SLEEVE,LEFT_PANTS_LEG,RIGHT_PANTS_LEG,HAT;
 public net.minecraft.network.chat.Component getName(){return net.minecraft.network.chat.Component.literal(name());}}''',
    PREFIX+'client/lore/ChroniclerMemory.java': '''package com.eternalcraft.gripeverde.client.lore;
public class ChroniclerMemory {public static int bump(String id){return 1;}public static boolean isDiscovered(String id){return false;}public static void addCollectible(String id){}}''',
})
name = 'net/minecraft/client/Options.java'
STUBS[name] = STUBS[name].replace('public class Options {', '''public class Options {
 public KeyMapping[] keyMappings=new KeyMapping[0];
 private OptionInstance<Boolean> crouch=new OptionInstance<>(false),sprint=new OptionInstance<>(false),jump=new OptionInstance<>(false),operator=new OptionInstance<>(false);
 public OptionInstance<Boolean> toggleCrouch(){return crouch;}public OptionInstance<Boolean> toggleSprint(){return sprint;}
 public OptionInstance<Boolean> autoJump(){return jump;}public OptionInstance<Boolean> operatorItemsTab(){return operator;}
 public java.util.Set<net.minecraft.world.entity.player.PlayerModelPart> parts=java.util.EnumSet.allOf(net.minecraft.world.entity.player.PlayerModelPart.class);
 private OptionInstance<net.minecraft.world.entity.HumanoidArm> hand=new OptionInstance<>(net.minecraft.world.entity.HumanoidArm.RIGHT);
 public OptionInstance<net.minecraft.world.entity.HumanoidArm> mainHand(){return hand;}
 public boolean isModelPartEnabled(net.minecraft.world.entity.player.PlayerModelPart part){return parts.contains(part);}
 public void toggleModelPart(net.minecraft.world.entity.player.PlayerModelPart part,boolean enabled){if(enabled)parts.add(part);else parts.remove(part);}
''')
name = 'net/minecraft/client/Minecraft.java'
STUBS[name] = STUBS[name].replace('public class Minecraft {', '''public class Minecraft {
 public Player player=new Player();public static class Player {public String getModelName(){return "slim";}}
''')
name = 'net/minecraft/network/chat/CommonComponents.java'
STUBS[name] = STUBS[name].replace('public class CommonComponents {', '''public class CommonComponents {
 public static Component optionStatus(boolean value){return Component.literal(value?"ON":"OFF");}''')
name = PREFIX+'client/widgets/DiaryButton.java'
STUBS[name] = STUBS[name].replace('Icon{SCROLL,DOOR,HAND}', 'Icon{NONE,SCROLL,DOOR,HAND}')
name = 'net/minecraft/client/gui/components/AbstractWidget.java'
STUBS[name] = STUBS[name].replace('public Component getMessage()', 'public void setMessage(Component value){message=value;}public Component getMessage()')
name = PREFIX+'client/widgets/DiaryToggle.java'
STUBS[name] = STUBS[name].replace('public class DiaryToggle extends AbstractWidget {', '''public class DiaryToggle extends AbstractWidget {
 public DiaryToggle(int x,int y,int w,int h,net.minecraft.client.OptionInstance<Boolean> option,Component label){this(x,y,w,h,option::get,option::set,label,null,null);}
 public DiaryToggle(int x,int y,int w,int h,net.minecraft.client.OptionInstance<Boolean> option,Component label,Function<Boolean,Component> fmt,Runnable changed){this(x,y,w,h,option::get,option::set,label,fmt,changed);}
''')
name = PREFIX+'client/lore/Discoveries.java'
STUBS[name] = STUBS[name].replace('public class Discoveries {', 'public class Discoveries {public static final String SMUGGLER_SIGNAL="smuggler",ITEM_NOTE="note";')
name = PREFIX+'client/lore/Marginalia.java'
STUBS[name] = STUBS[name].replace('public class Marginalia {', 'public class Marginalia {public static final String N_SMUGGLER_HINT="hint";')
name = PREFIX+'client/audio/UiSounds.java'
STUBS[name] = STUBS[name].replace('public class UiSounds {', 'public class UiSounds {public static void toggle(){}public static void tab(){}')
name = PREFIX+'client/audio/ModSounds.java'
STUBS[name] = STUBS[name].replace('public class ModSounds {', 'public class ModSounds {public static Value MENU_DOOR=new Value();')
name = PREFIX+'client/render/DiaryChrome.java'
STUBS[name] = STUBS[name].replace('public class DiaryChrome {', 'public class DiaryChrome {public static String fitText(Font f,String text,int width){return shorten(f,text,width);}public static void drawRegistryTagLeft(GuiGraphics g,int x,int y,String tag){}')
STUBS[name] = STUBS[name].replace('public static void drawInkDivider(GuiGraphics g,int x,int y,int w){}', 'public static void drawInkDivider(GuiGraphics g,int x,int y,int w){g.dividers.add(y);}')
name = PREFIX+'client/render/PaperTexture.java'
STUBS[name] = STUBS[name].replace('{OFFICIAL,CORRESPONDENCE}', '{OFFICIAL,CORRESPONDENCE,MEDICAL,TECHNICAL}')

HARNESS = r'''import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import com.eternalcraft.gripeverde.client.screens.*;
import com.eternalcraft.gripeverde.client.widgets.*;
import com.eternalcraft.gripeverde.client.lore.Discoveries;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
public class GridOptionHarness {
 static class DiaryTypography {
  static String fitString(Font f,String s,int w){return s.substring(0,Math.min(s.length(),Math.max(0,w/6)));}
  static void draw(GuiGraphics g,Font f,String s,int x,int y,int color){g.drawString(f,s,x,y,color,false);}
 }
 __COMPACT_TEXT__
 static void compactText(){
  int cases=0;
  for(int width=1;width<=180;width++)for(String label:new String[]{"Crouch","Agacharse","A long translated option name"})for(String value:new String[]{"On","Alternar","A long translated state"}){
   GuiGraphics g=new GuiGraphics();renderCompactText(g,new Font(),10,20,width,label,value,1,2);
   for(var t:g.texts)check(t.x()>=10&&t.x()+t.width()<=10+width,"compact toggle text outside widget");
   if(g.texts.size()==2)check(g.texts.get(0).x()+g.texts.get(0).width()<=g.texts.get(1).x(),"compact label/value overlap");cases++;
  }
  System.out.println("Compact toggle text: "+cases+" width/translation cases passed");
 }

 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 static Screen create(String type,Options options,Screen parent){return type.equals("controls")?new DiaryControlsScreen(parent,options):new DiarySkinCustomizationScreen(parent,options);}
 static boolean overlap(int x,int y,int w,int h,AbstractWidget b){return x<b.x+b.width&&x+w>b.x&&y<b.y+b.height&&y+h>b.y;}
 static void verify(Screen s,int expected){
  var widgets=s.children();check(widgets.size()==expected,"missing control");
  for(var a:widgets){
   check(a.width>0&&a.height>0&&a.x>=0&&a.y>=0&&a.x+a.width<=s.width&&a.y+a.height<=s.height,"widget outside screen: "+s.getClass()+" "+s.width+"x"+s.height);
   for(var b:widgets)if(a!=b)check(!overlap(a.x,a.y,a.width,a.height,b),"overlapping controls");
  }
  GuiGraphics g=new GuiGraphics();s.render(g,0,0,0);
  for(var text:g.texts)for(var a:widgets)check(!overlap(text.x(),text.y(),text.width(),text.height(),a),"text on a control: "+text.value()+" "+s.height);
  for(int y:g.dividers)for(var a:widgets)check(y<a.y||y>=a.y+a.height,"divider through control");
 }
 static void geometry(){
  int cases=0;
  for(String type:new String[]{"controls","skin"})for(boolean english:new boolean[]{true,false})for(int width:new int[]{160,161,200,320,364,800,1920})for(int height=120;height<=420;height++)for(boolean worn:new boolean[]{false,true}){
   Discoveries.english=english;Options options=new Options();if(!worn)options.parts.clear();
   Screen s=create(type,options,null);s.rebuild(width,height);verify(s,type.equals("controls")?7:9);
   s.resize(s.minecraft,width,height);verify(s,type.equals("controls")?7:9);check(options.saves==0,"layout saved options");cases++;
  }
  System.out.println("Grid options: "+cases+" complete-class init/render/resize cases passed");
 }
 static void skinActions(){
  var options=new Options();var s=new DiarySkinCustomizationScreen(null,options);s.rebuild(160,120);
  for(int i=0;i<7;i++){var part=PlayerModelPart.values()[i];((DiaryButton)s.children().get(i)).onPress();check(!options.isModelPartEnabled(part)&&options.saves==i+1,"part toggle did not persist exactly once");check(s.children().get(i).getMessage().getString().contains("OFF"),"part label stale");}
  ((DiaryButton)s.children().get(7)).onPress();check(options.mainHand().get()==HumanoidArm.LEFT&&options.saves==8,"hand toggle failed");
  check(s.children().get(7).getMessage().getString().contains("LEFT"),"hand label stale");s.resize(s.minecraft,800,600);
  check(s.children().size()==9,"resize duplicated part buttons");((DiaryButton)s.children().get(0)).onPress();check(options.isModelPartEnabled(PlayerModelPart.CAPE)&&options.saves==9,"rebuilt part map points at stale controls");
 }
 static void controlsActions(){
  var options=new Options();var s=new DiaryControlsScreen(null,options);s.rebuild(160,120);
  ((DiaryToggle)s.children().get(2)).onPress();check(options.toggleCrouch().get(),"crouch toggle mapping changed");
  ((DiaryToggle)s.children().get(3)).onPress();check(options.toggleSprint().get(),"sprint toggle mapping changed");
  ((DiaryToggle)s.children().get(4)).onPress();check(options.autoJump().get(),"auto jump mapping changed");
  ((DiaryToggle)s.children().get(5)).onPress();check(options.operatorItemsTab().get(),"operator tab mapping changed");
  ((DiaryButton)s.children().get(0)).onPress();check(s.minecraft.screen instanceof MouseSettingsScreen,"mouse subpage changed");
  ((DiaryButton)s.children().get(1)).onPress();check(s.minecraft.screen instanceof net.minecraft.client.gui.screens.controls.KeyBindsScreen,"key subpage changed");
 }
 static void hiddenBadge(){
  var s=new DiaryControlsScreen(null,new Options());s.rebuild(800,120);s.render(new GuiGraphics(),0,0,0);
  // 340x112 panel at (230,4); old badge at (242,12), hidden in compact mode.
  check(!s.mouseClicked(242,12,0)&&s.minecraft.transitions==0&&s.parentClicks==1,"hidden badge intercepted title click");
 }
 static void back(String type){var parent=new Screen(Component.literal("parent"));var s=create(type,new Options(),parent);s.rebuild(160,120);((DiaryButton)s.children().get(s.children().size()-1)).onPress();check(s.minecraft.screen==parent,"return button lost parent");}
 public static void main(String[] args){switch(args[0]){case "geometry"->geometry();case "compact_text"->compactText();case "skin"->skinActions();case "controls"->controlsActions();case "hidden_badge"->hiddenBadge();case "back"->back(args[1]);default->throw new AssertionError(args[0]);}}
}
'''


class Wiring(unittest.TestCase):
    def test_toggle_compact_branch_precedes_fixed_plate(self):
        body = method(TOGGLE, 'protected void renderWidget(')
        self.assertLess(body.index('if (w < 150 || h < 18)'), body.index('int plateW = 66'))
        self.assertIn('renderCompactText(', body)

    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_grid_option_screens.py --require-java', (ROOT / path).read_text())


class Executable(unittest.TestCase):
    def test_complete_screens(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        with tempfile.TemporaryDirectory() as tmp:
            sources = dict(STUBS)
            for name, text in SOURCES.items():
                sources[PREFIX+'client/screens/'+name+'.java'] = text
            sources['GridOptionHarness.java'] = HARNESS.replace('__COMPACT_TEXT__', method(TOGGLE, 'private static void renderCompactText('))
            paths = []
            for name, text in sources.items():
                path = Path(tmp)/name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            for args in (('geometry',), ('compact_text',), ('skin',), ('controls',), ('hidden_badge',), ('back', 'skin'), ('back', 'controls')):
                with self.subTest(args=args):
                    subprocess.run(['java', '-Xmx256m', '-cp', tmp, 'GridOptionHarness', *args], check=True, timeout=90)


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
