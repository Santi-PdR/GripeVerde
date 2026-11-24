#!/usr/bin/env python3
"""Run actual language apply/completion/key/status methods with controlled futures.

Screen, options, language manager, audio and rendering are doubles. No real
resource reload, native input, font switch, pack fallback or rollback is tested.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method

ROOT = Path(__file__).resolve().parents[1]
SOURCE = (ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/screens/DiaryLanguageScreen.java').read_text()
PREFIX = 'com/eternalcraft/gripeverde/'
STUBS = {
    'org/lwjgl/glfw/GLFW.java': '''package org.lwjgl.glfw;
public class GLFW {public static final int GLFW_KEY_ENTER=257,GLFW_KEY_KP_ENTER=335,GLFW_KEY_ESCAPE=256,GLFW_KEY_BACKSPACE=259;}''',
    PREFIX+'client/audio/UiSounds.java': '''package com.eternalcraft.gripeverde.client.audio;
public class UiSounds {public static int successes,errors,backs;public static void success(){successes++;}public static void error(){errors++;}public static void back(){backs++;}}''',
    PREFIX+'client/lore/Marginalia.java': '''package com.eternalcraft.gripeverde.client.lore;
public class Marginalia {public static final String N_LANGUAGE="language";public static void queueOnce(String id){}}''',
    PREFIX+'client/lore/Discoveries.java': '''package com.eternalcraft.gripeverde.client.lore;
public class Discoveries {public static boolean english=true;public static boolean isEnglish(){return english;}}''',
}
for _name in ('MenuAmbience', 'MenuMusic'):
    STUBS[PREFIX+f'client/audio/{_name}.java'] = ('package com.eternalcraft.gripeverde.client.audio; public class '+_name+''' {
        public static int recoveries;public static boolean fail;
        public static void recoverAfterResourceReload(){recoveries++;if(fail)throw new IllegalStateException("audio failure");}
    }''')
HARNESS = r'''import java.util.*;
import java.util.concurrent.*;
import com.eternalcraft.gripeverde.client.audio.MenuAmbience;
import com.eternalcraft.gripeverde.client.audio.MenuMusic;
import com.eternalcraft.gripeverde.client.audio.UiSounds;
class Parent {
    Object focused;int delegated;
    Object getFocused(){return focused;}
    public boolean keyPressed(int key,int scan,int mods){delegated++;return false;}
}
public class LanguageReloadHarness extends Parent {
    static double lastScrollAmount=-1;
    boolean applyingLanguage,languageReloadFailed;
    String pendingLanguageCode="es_es",searchBuffer="";
    long searchBufferUntilMs;
    static final int PANEL_PAD=18;
    int panelX,panelW=420,listY=80;
    Object parent=new Object();
    Options options=new Options();LanguageManager languageManager=new LanguageManager();
    LanguageList languageList=new LanguageList();Minecraft minecraft=new Minecraft();
    Font font=new Font();DiaryButton unicodeButton=new DiaryButton();
    int toggles;
    LanguageReloadHarness(){minecraft.screen=this;unicodeButton.action=()->toggles++;}
    static class Options {String languageCode="en_us";int saves;void save(){saves++;}}
    static class LanguageManager {String selected="en_us";int sets;String getSelected(){return selected;}void setSelected(String code){selected=code;sets++;}}
    static class LanguageList {double getScrollAmount(){return 48.5;}}
    static class DiaryButton {boolean active=true,visible=true;Runnable action;void onPress(){action.run();}}
    static class Minecraft {
        Object screen;int reloads,navigations;boolean synchronousFailure;
        CompletableFuture<Void> future=new CompletableFuture<>();List<Runnable> queue=new ArrayList<>();
        CompletableFuture<Void> reloadResourcePacks(){reloads++;if(synchronousFailure)throw new IllegalStateException("cannot launch reload");return future;}
        void execute(Runnable task){queue.add(task);}
        void drain(){while(!queue.isEmpty())queue.remove(0).run();}
        void setScreen(Object screen){this.screen=screen;navigations++;}
    }
    static class Font {int width(String text){return text.length();}}
    static class GuiGraphics {List<String> texts=new ArrayList<>();void fill(int x,int y,int xx,int yy,int color){}void drawString(Font font,String text,int x,int y,int color,boolean shadow){texts.add(text);}}
    static class DiaryChrome {static String fitText(Font font,String text,int width){return text.substring(0,Math.min(text.length(),width));}}
    __METHODS__
    static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    void failReload(){minecraft.future.completeExceptionally(new IllegalStateException("pack failure"));minecraft.drain();}
    void expectFailure(){
        check(!applyingLanguage&&languageReloadFailed,"failed reload left blocking overlay");
        check(minecraft.screen==this&&minecraft.navigations==0,"failed reload navigated");
        check(UiSounds.errors==1&&MenuMusic.recoveries==0&&MenuAmbience.recoveries==0,"failure did not isolate recovery");
    }
    void expectSuccess(){
        check(!applyingLanguage&&!languageReloadFailed,"success retained pending/failed state");
        check(minecraft.screen==parent&&minecraft.navigations==1,"successful reload did not close exactly once");
        check(MenuMusic.recoveries==1&&MenuAmbience.recoveries==1,"successful recovery missed channel");
    }
    public static void main(String[] args){
        LanguageReloadHarness s=new LanguageReloadHarness();String test=args[0];
        switch(test){
            case "starts_pending" -> {
                s.applyAndClose();check(s.applyingLanguage&&!s.languageReloadFailed,"apply did not lock pending UI");
                check(s.minecraft.reloads==1&&s.minecraft.navigations==0,"apply closed before future");
                check(s.options.languageCode.equals("es_es")&&s.languageManager.selected.equals("es_es"),"language preference not selected");
                check(lastScrollAmount==48.5&&s.options.saves==1,"apply lost scroll/save");
            }
            case "double_apply" -> {s.applyAndClose();s.applyAndClose();check(s.minecraft.reloads==1&&s.options.saves==1,"pending apply repeated");}
            case "success", "immediate_success" -> {
                if(test.equals("immediate_success"))s.minecraft.future=CompletableFuture.completedFuture(null);
                s.applyAndClose();if(test.equals("success"))s.minecraft.future.complete(null);
                check(s.applyingLanguage&&s.minecraft.navigations==0&&s.minecraft.queue.size()==1,"completion bypassed client executor");
                s.minecraft.drain();s.expectSuccess();
            }
            case "future_failure" -> {s.applyAndClose();s.failReload();s.expectFailure();}
            case "retry_same_language" -> {
                s.applyAndClose();s.failReload();s.expectFailure();
                check(s.languageManager.selected.equals(s.pendingLanguageCode),"fixture must already have requested selection");
                s.minecraft.future=new CompletableFuture<>();s.applyAndClose();
                check(s.applyingLanguage&&!s.languageReloadFailed&&s.minecraft.reloads==2,"same-language failure could not retry");
                s.minecraft.future.complete(null);s.minecraft.drain();s.expectSuccess();
            }
            case "synchronous_failure", "retry_after_sync_failure" -> {
                s.minecraft.synchronousFailure=true;s.applyAndClose();s.expectFailure();
                if(test.equals("retry_after_sync_failure")){
                    s.minecraft.synchronousFailure=false;s.applyAndClose();check(s.minecraft.reloads==2,"sync failure could not retry");
                    s.minecraft.future.complete(null);s.minecraft.drain();s.expectSuccess();
                }
            }
            case "left_before_success", "left_before_failure" -> {
                s.applyAndClose();Object destination=new Object();s.minecraft.screen=destination;
                if(test.equals("left_before_success"))s.minecraft.future.complete(null);else s.minecraft.future.completeExceptionally(new IllegalStateException("late failure"));
                s.minecraft.drain();check(!s.applyingLanguage,"detached completion retained pending state");
                check(s.minecraft.screen==destination&&s.minecraft.navigations==0,"completion overwrote another screen");
                check(MenuMusic.recoveries==0&&MenuAmbience.recoveries==0&&UiSounds.errors==0,"detached completion played/restarted audio");
            }
            case "ambience_recovery_failure", "music_recovery_failure" -> {
                MenuAmbience.fail=test.startsWith("ambience");MenuMusic.fail=test.startsWith("music");
                s.applyAndClose();s.minecraft.future.complete(null);s.minecraft.drain();s.expectSuccess();
            }
            case "same_language", "no_pending_language" -> {
                s.pendingLanguageCode=test.equals("same_language")?"en_us":null;s.applyAndClose();
                check(s.minecraft.reloads==0&&s.minecraft.screen==s.parent&&s.options.saves==1,"unchanged language unexpectedly reloaded");
            }
            case "unicode_enter", "unicode_keypad", "unicode_disabled", "unicode_hidden" -> {
                s.focused=s.unicodeButton;if(test.equals("unicode_disabled"))s.unicodeButton.active=false;if(test.equals("unicode_hidden"))s.unicodeButton.visible=false;
                check(s.keyPressed(test.equals("unicode_keypad")?335:257,0,0),"Unicode Enter not consumed");
                check(s.toggles==((s.unicodeButton.active&&s.unicodeButton.visible)?1:0),"Unicode target not honored");
                check(s.minecraft.reloads==0&&s.minecraft.navigations==0,"Unicode Enter applied language/closed screen");
            }
            case "applying_blocks_keys" -> {
                s.applyAndClose();s.focused=s.unicodeButton;
                check(s.keyPressed(257,0,0)&&s.keyPressed(256,0,0)&&s.keyPressed(259,0,0),"pending input not blocked");
                check(s.toggles==0&&s.minecraft.reloads==1&&s.minecraft.navigations==0,"pending key changed action/state");
            }
            case "default_enter" -> {s.keyPressed(257,0,0);check(s.minecraft.reloads==1&&s.applyingLanguage,"default Enter no longer applies");}
            case "failure_notice", "failure_notice_spanish" -> {
                s.applyAndClose();s.failReload();s.panelW=80;
                com.eternalcraft.gripeverde.client.lore.Discoveries.english=!test.endsWith("spanish");
                GuiGraphics g=new GuiGraphics();s.drawSearchBuffer(g);
                check(g.texts.size()==1&&g.texts.get(0).length()<=30,"failure status absent or not fitted");
                check(g.texts.get(0).startsWith(test.endsWith("spanish")?"Fallo":"Reload"),"wrong failure hint language");
            }
            case "failure_escape" -> {
                s.applyAndClose();s.failReload();s.onClose();
                check(s.minecraft.screen==s.parent&&UiSounds.backs==1,"failure UI could not cancel");
            }
            default -> throw new AssertionError(test);
        }
    }
}
'''
CASES = ('starts_pending', 'double_apply', 'success', 'immediate_success', 'future_failure', 'retry_same_language',
         'synchronous_failure', 'retry_after_sync_failure', 'left_before_success', 'left_before_failure',
         'ambience_recovery_failure', 'music_recovery_failure', 'same_language', 'no_pending_language',
         'unicode_enter', 'unicode_keypad', 'unicode_disabled', 'unicode_hidden', 'applying_blocks_keys',
         'default_enter', 'failure_notice', 'failure_notice_spanish', 'failure_escape')


class Wiring(unittest.TestCase):
    def test_completion_covers_failures_and_client_executor(self):
        body = method(SOURCE, 'private void applyAndClose(')
        self.assertIn('.whenComplete(', body)
        self.assertIn('this.minecraft.execute(', body)
        self.assertIn('this.finishLanguageReload(failure)', body)
        self.assertIn('this.languageReloadFailed', body)

    def test_completion_checks_current_screen_before_navigation(self):
        body = method(SOURCE, 'private void finishLanguageReload(')
        self.assertLess(body.index('this.applyingLanguage = false'), body.index('this.minecraft.screen != this'))
        self.assertLess(body.index('this.minecraft.screen != this'), body.index('recoverAfterResourceReload('))
        self.assertLess(body.index('this.minecraft.screen != this'), body.index('this.minecraft.setScreen('))

    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_language_reload.py --require-java', (ROOT / path).read_text())


class Executable(unittest.TestCase):
    def test_language_reload_lifecycle(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        signatures = ('private void applyAndClose(', 'private void finishLanguageReload(', 'public boolean keyPressed(',
                      'public void onClose(', 'private void drawSearchBuffer(')
        harness = HARNESS.replace('__METHODS__', '\n'.join(method(SOURCE, sig) for sig in signatures))
        with tempfile.TemporaryDirectory() as tmp:
            sources = dict(STUBS)
            sources['LanguageReloadHarness.java'] = harness
            paths = []
            for name, text in sources.items():
                path = Path(tmp)/name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            for case in CASES:
                with self.subTest(case=case):
                    subprocess.run(['java', '-cp', tmp, 'LanguageReloadHarness', case], check=True, timeout=15)
            print(f'Language reload: {len(CASES)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
