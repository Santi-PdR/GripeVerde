#!/usr/bin/env python3
"""Run actual submit/cancel/validation/key/resize/recent-hit methods with doubles.

No Minecraft Screen dispatch, network, real options saves, history file or lore
I/O. Actual address-parser and RecentAddresses regressions remain in Gradle.
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
SOURCES = {name: (ROOT / f'src/main/java/{PREFIX}client/screens/{name}.java').read_text()
           for name in ('DiaryEditServerScreen', 'DiaryDirectJoinScreen')}
STUBS = {
    'net/minecraft/Util.java': 'package net.minecraft; public class Util {public static long now=10000;public static long getMillis(){return now;}}',
    'net/minecraft/client/Minecraft.java': '''package net.minecraft.client;
public class Minecraft {public Options options=new Options();public static class Options {
    public String lastMpIp="previous.example.org";public int saves;public Runnable saveHook;
    public void save(){saves++;if(saveHook!=null)saveHook.run();}
}}''',
    'net/minecraft/network/chat/Component.java': '''package net.minecraft.network.chat;
public record Component(String text){public static Component literal(String s){return new Component(s);}public static Component translatable(String key){return new Component(key);}public String getString(){return text;}}''',
    'net/minecraft/client/gui/components/Tooltip.java': '''package net.minecraft.client.gui.components;
public class Tooltip {public static Tooltip create(net.minecraft.network.chat.Component text){return new Tooltip();}}''',
    PREFIX+'client/integration/RecentAddresses.java': '''package com.eternalcraft.gripeverde.client.integration;
public class RecentAddresses {public static int pushes;public static String last;public static Runnable hook;
    public static void push(String ip){pushes++;last=ip;if(hook!=null)hook.run();}}''',
    PREFIX+'client/audio/UiSounds.java': '''package com.eternalcraft.gripeverde.client.audio;
public class UiSounds {public static int backs,types,custom;public static void back(){backs++;}public static void type(){types++;}public static void playCustom(Object sound,float v,float p){custom++;}}''',
    PREFIX+'client/audio/ModSounds.java': '''package com.eternalcraft.gripeverde.client.audio;
public class ModSounds {public static Value MENU_BELL=new Value(),MENU_AMBIENT_OCCASIONAL=new Value();public static class Value{public Object get(){return this;}}}''',
    PREFIX+'client/lore/Discoveries.java': '''package com.eternalcraft.gripeverde.client.lore;
public class Discoveries {public static final String ADDRESS_13="13";public static int calls;public static boolean isEnglish(){return true;}public static boolean discover(String id){return ++calls==1;}}''',
}
HARNESS = r'''import java.util.*;
import java.util.function.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Tooltip;
import com.eternalcraft.gripeverde.client.integration.RecentAddresses;
import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.lore.Discoveries;
public class ServerFormsHarness {
    static class DiaryChrome {
        // Validator double: these tests concern final validation wiring, not DNS/parser syntax.
        static boolean looksLikeAddress(String value){if(value==null)return false;String ip=value.trim();return !ip.isEmpty()&&!ip.contains(" ")&&!ip.contains("#")&&!ip.endsWith(":");}
    }
    static class DiaryTextField {
        String value="";boolean active=true,visible=true;int cursor,highlight;Runnable changed;
        String getValue(){return value;}
        void setValue(String value){this.value=value;cursor=highlight=value.length();if(changed!=null)changed.run();}
        void setFocused(boolean focused){}
        record EditingState(String value,int cursor,int highlight){}
        EditingState snapshotEditingState(){return new EditingState(value,cursor,highlight);}
        void restoreEditingState(EditingState state){if(state!=null){setValue(state.value());cursor=state.cursor();highlight=state.highlight();}}
    }
    static class Button {boolean active=true,visible=true;Runnable action;Button(Runnable action){this.action=action;}void onPress(){action.run();}void setTooltip(Tooltip tip){}}
    static class ServerData {String name="Stored name",ip="stored.example.org";}
    static abstract class Parent {
        DiaryTextField nameEdit,ipEdit;Button doneButton,selectButton,cancelButton;
        ServerData serverData=new ServerData();Minecraft minecraft=new Minecraft();
        boolean formCompleted,accepted,callbackFails;int calls,parentKeys,parentClicks;Runnable reenter;
        Consumer<Boolean> callback=value->{calls++;accepted=value;if(reenter!=null)reenter.run();if(callbackFails)throw new IllegalStateException("callback failed");};
        Object focused;
        abstract void submit();abstract void cancel();abstract void update();abstract void close();
        abstract boolean valid();
        void rebuild(){
            nameEdit=new DiaryTextField();ipEdit=new DiaryTextField();
            nameEdit.setValue(serverData==null?"":serverData.name);ipEdit.setValue(serverData==null?"":serverData.ip);
            nameEdit.changed=this::update;ipEdit.changed=this::update;
            doneButton=new Button(this::submit);selectButton=new Button(this::submit);cancelButton=new Button(this::cancel);
            if(this instanceof Direct direct)direct.clearRecentHits();
            setFocused(this instanceof Direct?ipEdit:nameEdit);update();
        }
        void draft(){nameEdit.setValue(" Draft name ");ipEdit.setValue(" play.example.org:25565 ");}
        void setFocused(Object focused){this.focused=focused;}Object getFocused(){return focused;}
        public void resize(Minecraft mc,int w,int h){rebuild();}
        public boolean keyPressed(int key,int scan,int mods){
            parentKeys++;
            if(key==256){close();return true;}
            if((key==257||key==335||key==32)&&focused instanceof Button b&&b.active&&b.visible){b.onPress();return true;}
            return false;
        }
        public boolean mouseClicked(double x,double y,int button){parentClicks++;return false;}
        Button submitButton(){return this instanceof Direct?selectButton:doneButton;}
    }
    static class Editor extends Parent {
        void submit(){onDone();}void cancel(){onCancel();}void close(){onClose();}void update(){updateDoneButton();}boolean valid(){return canSubmit();}
        __EDITOR__
    }
    static class Direct extends Parent {
        long unmappedWarnUntilMs;boolean unmappedWarnShown;
        List<Integer> recentXs=new ArrayList<>(),recentWs=new ArrayList<>();List<String> recentShown=new ArrayList<>();int recentY=-1;
        void submit(){onSelect();}void cancel(){onCancel();}void close(){onClose();}void update(){updateSelectButton();}boolean valid(){return canSubmit();}
        void hit(){recentXs.add(10);recentWs.add(100);recentShown.add("full.recent.example.org:25565");recentY=100;}
        __DIRECT__
    }
    static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    static void accepted(Parent form){
        check(form.calls==1&&form.accepted&&form.formCompleted,"expected one acceptance");
        check(form.serverData.ip.equals("play.example.org:25565"),"address not normalized at commit");
        check(!form.submitButton().active&&!form.cancelButton.active,"completed buttons enabled");
        if(form instanceof Direct){
            check(RecentAddresses.pushes==1&&RecentAddresses.last.equals(form.serverData.ip),"history duplicated/not normalized");
            check(form.minecraft.options.saves==1&&form.minecraft.options.lastMpIp.equals(form.serverData.ip),"last server not saved once");
        }else{
            check(form.serverData.name.equals(" Draft name "),"server name unexpectedly trimmed");
            check(RecentAddresses.pushes==0&&form.minecraft.options.saves==0,"editor touched direct-join history/options");
        }
    }
    static void cancelled(Parent form){
        check(form.calls==1&&!form.accepted&&form.formCompleted,"expected one cancellation");
        check(form.serverData.ip.equals("stored.example.org")&&form.serverData.name.equals("Stored name"),"cancellation mutated server");
        check(RecentAddresses.pushes==0&&form.minecraft.options.saves==0,"cancellation persisted direct-join state");
    }
    public static void main(String[] args){
        String test=args[0];Parent form=args[1].equals("direct")?new Direct():new Editor();
        if(test.equals("cancel_before_init")){form.cancel();check(form.calls==1&&!form.accepted,"uninitialized cancel failed");return;}
        if(test.equals("enter_before_init")){form.keyPressed(257,0,0);check(form.calls==0,"uninitialized Enter submitted");return;}
        form.rebuild();form.draft();
        switch(test){
            case "valid_submit" -> {form.submit();accepted(form);}
            case "invalid_empty_address", "invalid_space_address" -> {
                form.ipEdit.setValue(test.equals("invalid_empty_address")?"":"bad host");form.submit();
                check(form.calls==0&&!form.formCompleted&&!form.submitButton().active,"invalid final input submitted");
                check(form.serverData.ip.equals("stored.example.org"),"invalid submit mutated server");
            }
            case "double_submit", "submit_then_cancel" -> {
                form.submit();if(test.equals("double_submit"))form.submit();else form.cancel();form.close();accepted(form);
            }
            case "cancel_then_submit", "double_cancel", "close_twice" -> {
                if(test.equals("close_twice")){form.close();form.close();check(UiSounds.backs==1,"close repeated sound");}
                else{form.cancel();if(test.equals("cancel_then_submit"))form.submit();else form.cancel();}cancelled(form);
            }
            case "reentrant_accept", "reentrant_cancel" -> {
                form.reenter=()->{form.submit();form.cancel();form.close();};
                if(test.equals("reentrant_accept")){form.submit();accepted(form);}else{form.cancel();cancelled(form);}
            }
            case "callback_throws_once" -> {
                form.callbackFails=true;try{form.submit();throw new AssertionError("callback failure swallowed");}catch(IllegalStateException expected){}
                form.submit();form.cancel();accepted(form);
            }
            case "invalid_then_correct" -> {
                form.ipEdit.setValue("bad host");form.submit();check(form.calls==0&&!form.formCompleted,"invalid attempt completed form");
                form.ipEdit.setValue(" play.example.org:25565 ");form.submit();accepted(form);
            }
            case "stale_button_validity" -> {
                check(form.submitButton().active,"fixture not initially valid");form.ipEdit.value="bad host";form.submitButton().onPress();
                check(form.calls==0&&!form.submitButton().active,"cached active button bypassed validation");
                form.ipEdit.setValue(" play.example.org:25565 ");form.submit();accepted(form);
            }
            case "responder_after_completion" -> {
                form.submit();form.ipEdit.setValue("other.example.org");form.update();
                check(!form.submitButton().active&&!form.cancelButton.active,"late responder revived controls");form.submit();accepted(form);
            }
            case "resize_after_completion" -> {
                form.submit();form.resize(form.minecraft,320,180);check(!form.submitButton().active&&!form.cancelButton.active,"resize revived controls");form.submit();accepted(form);
            }
            case "completed_enter" -> {
                form.submit();int keys=form.parentKeys;check(form.keyPressed(257,0,0)&&form.parentKeys==keys,"completed key delegated to old UI");accepted(form);
            }
            case "focus_cancel_resize", "focus_submit_resize" -> {
                boolean cancel=test.equals("focus_cancel_resize");form.setFocused(cancel?form.cancelButton:form.submitButton());
                form.resize(form.minecraft,320,180);check(form.getFocused()==(cancel?form.cancelButton:form.submitButton()),"footer focus lost on resize");
                form.keyPressed(257,0,0);if(cancel)cancelled(form);else accepted(form);
            }
            case "empty_name" -> {
                form.nameEdit.setValue("");form.submit();check(form.calls==0&&!form.valid(),"empty editor name accepted");
            }
            case "null_server_data" -> {
                form.serverData=null;form.update();form.submit();check(form.calls==0&&!form.valid(),"null direct-join target accepted");
            }
            case "reentry_from_history", "reentry_from_options" -> {
                if(test.equals("reentry_from_history"))RecentAddresses.hook=()->{form.submit();form.close();};
                else form.minecraft.options.saveHook=()->{form.submit();form.close();};
                form.submit();accepted(form);
            }
            case "reserved_repeat", "reserved_expiry", "reserved_negative_clock", "reserved_then_real", "reserved_then_cancel" -> {
                Direct direct=(Direct)form;if(test.equals("reserved_negative_clock"))net.minecraft.Util.now=-10000;
                form.ipEdit.setValue("13.13.13.13");form.submit();
                check(form.calls==0&&!form.formCompleted&&Discoveries.calls==1&&UiSounds.custom==2,"special address should warn, not join");
                check(direct.remainingUnmappedWarningMs()==6000,"warning clock invalid");
                long until=direct.unmappedWarnUntilMs;for(int i=0;i<10;i++)form.submit();
                check(Discoveries.calls==1&&UiSounds.custom==2&&direct.unmappedWarnUntilMs==until,"held Enter repeated warning/sound or extended deadline");
                if(test.equals("reserved_expiry")||test.equals("reserved_negative_clock")){
                    net.minecraft.Util.now+=5999;form.submit();check(Discoveries.calls==1,"cooldown expired early");
                    net.minecraft.Util.now++;form.submit();check(Discoveries.calls==2&&UiSounds.custom==3,"cooldown could not rearm at deadline");
                }else if(test.equals("reserved_then_real")){form.ipEdit.setValue(" play.example.org:25565 ");form.submit();accepted(form);}
                else if(test.equals("reserved_then_cancel")){form.cancel();cancelled(form);}
                else check(RecentAddresses.pushes==0&&form.minecraft.options.saves==0,"special address persisted/joined");
            }
            case "recent_empty", "recent_whitespace", "recent_typed_stale", "recent_hidden", "recent_disabled", "recent_secondary", "recent_resize", "recent_completed" -> {
                Direct direct=(Direct)form;form.ipEdit.setValue(test.equals("recent_whitespace")?"   ":"");direct.hit();
                if(test.equals("recent_typed_stale"))form.ipEdit.setValue("draft.example.org");
                if(test.equals("recent_hidden"))form.ipEdit.visible=false;
                if(test.equals("recent_disabled"))form.ipEdit.active=false;
                if(test.equals("recent_resize")){form.resize(form.minecraft,320,180);check(direct.recentY==-1&&direct.recentShown.isEmpty(),"resize retained hit boxes");}
                if(test.equals("recent_completed"))form.cancel();
                String before=form.ipEdit.getValue();boolean result=direct.mouseClicked(20,105,test.equals("recent_secondary")?1:0);
                boolean eligible=test.equals("recent_empty")||test.equals("recent_whitespace");
                if(eligible){check(result&&form.ipEdit.getValue().equals("full.recent.example.org:25565")&&form.getFocused()==form.ipEdit,"eligible chip lost full address/focus");check(UiSounds.types==1&&form.submitButton().active,"recent did not validate/fill once");}
                else check(form.ipEdit.getValue().equals(before)&&UiSounds.types==0,"stale/unavailable chip overwrote input");
                check(RecentAddresses.pushes==0&&form.minecraft.options.saves==0,"selecting recent unexpectedly persisted");
            }
            default -> throw new AssertionError(test);
        }
    }
}
'''
COMMON = ('valid_submit', 'invalid_empty_address', 'invalid_space_address', 'double_submit', 'submit_then_cancel',
          'cancel_then_submit', 'double_cancel', 'close_twice', 'reentrant_accept', 'reentrant_cancel',
          'callback_throws_once', 'invalid_then_correct', 'stale_button_validity', 'responder_after_completion',
          'resize_after_completion', 'completed_enter', 'focus_cancel_resize', 'focus_submit_resize',
          'cancel_before_init', 'enter_before_init')
DIRECT = ('null_server_data', 'reentry_from_history', 'reentry_from_options', 'reserved_repeat', 'reserved_expiry',
          'reserved_negative_clock', 'reserved_then_real', 'reserved_then_cancel', 'recent_empty', 'recent_whitespace',
          'recent_typed_stale', 'recent_hidden', 'recent_disabled', 'recent_secondary', 'recent_resize', 'recent_completed')


class Wiring(unittest.TestCase):
    def test_final_validation_and_latch_precede_side_effects(self):
        for name, signature in (('DiaryEditServerScreen', 'private void onDone('), ('DiaryDirectJoinScreen', 'private void onSelect(')):
            body = method(SOURCES[name], signature)
            self.assertLess(body.index('this.canSubmit()'), body.index('this.formCompleted = true'))
            self.assertLess(body.index('this.formCompleted = true'), body.index('this.serverData.'))
            self.assertLess(body.index('this.formCompleted = true'), body.index('this.callback.accept(true)'))

    def test_recent_hits_invalidate_on_init_and_render(self):
        source = SOURCES['DiaryDirectJoinScreen']
        for signature in ('protected void init(', 'public void render('):
            self.assertIn('this.clearRecentHits()', method(source, signature))
        self.assertNotIn('System.currentTimeMillis()', source)

    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_server_forms.py --require-java', (ROOT / path).read_text())


class Executable(unittest.TestCase):
    def test_real_form_methods(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        common = ('private boolean canSubmit(', 'private void onCancel(', 'public void onClose(',
                  'public boolean keyPressed(', 'public void resize(')
        editor = '\n'.join(method(SOURCES['DiaryEditServerScreen'], sig) for sig in
                           common+('private void updateDoneButton(', 'private void onDone('))
        direct = '\n'.join(method(SOURCES['DiaryDirectJoinScreen'], sig) for sig in
                           common+('private void updateSelectButton(', 'private void onSelect(',
                                   'private long remainingUnmappedWarningMs(', 'private void clearRecentHits(', 'public boolean mouseClicked('))
        harness = HARNESS.replace('__EDITOR__', editor).replace('__DIRECT__', direct)
        cases = [(case, kind) for case in COMMON for kind in ('editor', 'direct')]
        cases += [('empty_name', 'editor')]+[(case, 'direct') for case in DIRECT]
        with tempfile.TemporaryDirectory() as tmp:
            sources = dict(STUBS)
            sources['ServerFormsHarness.java'] = harness
            paths = []
            for name, text in sources.items():
                path = Path(tmp)/name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            for case, kind in cases:
                with self.subTest(case=case, form=kind):
                    subprocess.run(['java', '-cp', tmp, 'ServerFormsHarness', case, kind], check=True, timeout=15)
            print(f'Server forms: {len(cases)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
