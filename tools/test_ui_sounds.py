#!/usr/bin/env python3
"""Compile complete UiSounds against registry/config/sound-manager doubles.

No playback or GUI: verify cooldown arithmetic, callback survival and retries.
Deadlines are injected through reflection; no clock changes or timed sleeps.
"""
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method

ROOT = Path(__file__).resolve().parents[1]
PREFIX = 'com/eternalcraft/gripeverde/'
SOURCE = (ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/audio/UiSounds.java').read_text()
STUBS = {
    'net/minecraft/sounds/SoundEvent.java': 'package net.minecraft.sounds; public class SoundEvent {}',
    'net/minecraftforge/registries/RegistryObject.java': '''package net.minecraftforge.registries;
public class RegistryObject<T> {
    public T value;
    public boolean fail;
    public RegistryObject(T value) { this.value=value; }
    public T get() { if (fail) throw new IllegalStateException("registry unavailable"); return value; }
}''',
    'net/minecraft/client/resources/sounds/SimpleSoundInstance.java': '''package net.minecraft.client.resources.sounds;
public class SimpleSoundInstance {
    public final net.minecraft.sounds.SoundEvent event;
    public final float volume,pitch;
    private SimpleSoundInstance(net.minecraft.sounds.SoundEvent event,float volume,float pitch) {
        this.event=event; this.volume=volume; this.pitch=pitch;
    }
    public static SimpleSoundInstance forUI(net.minecraft.sounds.SoundEvent event,float pitch,float volume) {
        return new SimpleSoundInstance(event,volume,pitch);
    }
}''',
    PREFIX+'config/ClientConfig.java': '''package com.eternalcraft.gripeverde.config;
public class ClientConfig {
    public static Value UI_VOLUME=new Value();
    public static class Value {
        public Double value=0.5;
        public boolean fail;
        public Double get() { if (fail) throw new IllegalStateException("config not ready"); return value; }
    }
}''',
    'net/minecraft/client/Minecraft.java': '''package net.minecraft.client;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
public class Minecraft {
    public static Minecraft instance=new Minecraft();
    public SoundManager sounds=new SoundManager();
    public static Minecraft getInstance() { return instance; }
    public SoundManager getSoundManager() { return sounds; }
    public static class SoundManager {
        public java.util.List<SimpleSoundInstance> played=new java.util.ArrayList<>();
        public boolean failNext;
        public void play(SimpleSoundInstance sound) {
            if (failNext) { failNext=false; throw new IllegalStateException("engine reloading"); }
            played.add(sound);
        }
    }
}''',
}
STUBS[PREFIX+'client/audio/ModSounds.java'] = '''package com.eternalcraft.gripeverde.client.audio;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;
public class ModSounds {\n''' + '\n'.join(
    f'public static final RegistryObject<SoundEvent> {name}=new RegistryObject<>(new SoundEvent());'
    for name in sorted(set(re.findall(r'ModSounds\.(UI_\w+)', SOURCE)))
) + '\n}'

HARNESS = r'''import java.lang.reflect.*;
import java.util.*;
import com.eternalcraft.gripeverde.client.audio.*;
import com.eternalcraft.gripeverde.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
public class UiSoundsHarness {
    @SuppressWarnings("unchecked")
    static Map<String,Long> timestamps() throws Exception {
        Field f=UiSounds.class.getDeclaredField("lastPlayed"); f.setAccessible(true);
        return (Map<String,Long>)f.get(null);
    }
    static boolean cooldown(String key,long now,long delay) throws Exception {
        Method m=UiSounds.class.getDeclaredMethod("isCoolingDown",String.class,long.class,long.class);
        m.setAccessible(true); return (Boolean)m.invoke(null,key,now,delay);
    }
    static void check(boolean ok,String why) { if (!ok) throw new AssertionError(why); }
    static void near(float a,float b) { check(Math.abs(a-b)<0.000001f,a+" != "+b); }
    static int actions;
    public static void main(String[] args) throws Exception {
        Minecraft mc=Minecraft.instance;
        Map<String,Long> map=timestamps();
        switch (args[0]) {
            case "first_negative_origin" -> {
                check(!cooldown("new",-1_000_000L,140L),"first sound suppressed at negative nanoTime origin");
                check(!cooldown("new",0L,140L),"first sound suppressed at zero origin");
            }
            case "cooldown_boundary" -> {
                map.put("test",-200L);
                check(cooldown("test",-61L,140L),"cooldown ended early");
                check(!cooldown("test",-60L,140L),"exact deadline not released");
                check(!cooldown("test",-59L,140L),"expired timestamp retained");
            }
            case "click_deduplicated" -> {
                UiSounds.buttonMain(); UiSounds.playClick(); UiSounds.click();
                check(mc.sounds.played.size()==1,"same click family stacked");
            }
            case "family_independence" -> {
                UiSounds.buttonMain(); UiSounds.buttonSection();
                check(mc.sounds.played.size()==2,"independent sound families suppressed");
            }
            case "expired_click" -> {
                UiSounds.playClick(); map.put("button_main",System.nanoTime()/1_000_000L-10_000L);
                UiSounds.playClick(); check(mc.sounds.played.size()==2,"expired click did not play");
            }
            case "back_deduplicated" -> {
                UiSounds.back(); UiSounds.back(); UiSounds.clickDestroy();
                check(mc.sounds.played.size()==1,"button and close back feedback stacked");
            }
            case "expired_back" -> {
                UiSounds.back(); map.put("back",System.nanoTime()/1_000_000L-10_000L);
                UiSounds.back(); check(mc.sounds.played.size()==2,"back retained a second wall-clock gate");
            }
            case "hover_deduplicated" -> {
                UiSounds.playHover(); UiSounds.playHover();
                check(mc.sounds.played.size()==2,"hover lost its paper layer or duplicated feedback");
            }
            case "hover_dispatch_gate" -> {
                map.put("hover_dispatch",System.nanoTime()/1_000_000L+100_000L);
                UiSounds.playHover(); check(mc.sounds.played.isEmpty(),"hover dispatch bypassed global gate");
            }
            case "hover_dispatch_expires" -> {
                map.put("hover_dispatch",System.nanoTime()/1_000_000L-10_000L);
                UiSounds.playHover(); check(mc.sounds.played.size()==2,"expired hover gate suppressed feedback");
            }
            case "no_client_retry" -> {
                Minecraft.instance=null; UiSounds.playClick();
                check(!map.containsKey("button_main"),"missing client consumed cooldown");
                Minecraft.instance=mc; UiSounds.playClick();
                check(mc.sounds.played.size()==1,"missing client blocked immediate retry");
            }
            case "no_manager_retry" -> {
                Minecraft.SoundManager engine=mc.sounds; mc.sounds=null; UiSounds.playClick();
                check(!map.containsKey("button_main"),"missing engine consumed cooldown");
                mc.sounds=engine; UiSounds.playClick();
                check(mc.sounds.played.size()==1,"missing engine blocked immediate retry");
            }
            case "engine_failure_action_retry" -> {
                mc.sounds.failNext=true;
                Runnable action=()->{ UiSounds.playClick(); actions++; };
                action.run(); check(actions==1,"sound failure aborted action");
                check(!map.containsKey("button_main"),"failed playback consumed cooldown");
                action.run(); check(actions==2 && mc.sounds.played.size()==1,"sound failure prevented retry/action");
            }
            case "registry_failure_retry" -> {
                ModSounds.UI_BUTTON_MAIN.fail=true; UiSounds.playClick();
                check(!map.containsKey("button_main"),"failed registry consumed cooldown");
                ModSounds.UI_BUTTON_MAIN.fail=false; UiSounds.playClick();
                check(mc.sounds.played.size()==1,"registry failure prevented retry");
            }
            case "empty_registry" -> {
                ModSounds.UI_BUTTON_MAIN.value=null; UiSounds.playClick();
                check(mc.sounds.played.isEmpty() && !map.containsKey("button_main"),"empty event played");
            }
            case "volume_scaled" -> {
                UiSounds.playClick(); near(mc.sounds.played.get(0).volume,0.18f);
            }
            case "config_fallback" -> {
                ClientConfig.UI_VOLUME.fail=true; UiSounds.playClick();
                near(mc.sounds.played.get(0).volume,0.36f);
            }
            case "raw_volume_no_cooldown" -> {
                SoundEvent event=new SoundEvent();
                UiSounds.playRaw(event,0.8f,1.2f); UiSounds.playCustom(event,0.4f,0.9f);
                check(mc.sounds.played.size()==2 && map.isEmpty(),"raw/custom API gained unintended cooldown");
                SimpleSoundInstance first=mc.sounds.played.get(0);
                check(first.event==event,"raw event changed"); near(first.volume,0.4f); near(first.pitch,1.2f);
            }
            case "raw_null_event" -> {
                UiSounds.playRaw(null,1,1); check(mc.sounds.played.isEmpty(),"null raw event played");
            }
            case "raw_no_client" -> {
                Minecraft.instance=null; UiSounds.playRaw(new SoundEvent(),1,1);
                check(mc.sounds.played.isEmpty(),"raw sound played without client");
            }
            case "raw_no_manager" -> {
                mc.sounds=null; UiSounds.playRaw(new SoundEvent(),1,1);
            }
            case "raw_failure_action_retry" -> {
                mc.sounds.failNext=true;
                Runnable action=()->{ UiSounds.playCustom(new SoundEvent(),1,1); actions++; };
                action.run(); action.run();
                check(actions==2 && mc.sounds.played.size()==1,"raw sound failure aborted actions/retry");
            }
            default -> throw new AssertionError(args[0]);
        }
    }
}
'''
CASES = ('first_negative_origin', 'cooldown_boundary', 'click_deduplicated', 'family_independence',
         'expired_click', 'back_deduplicated', 'expired_back', 'hover_deduplicated', 'hover_dispatch_gate',
         'hover_dispatch_expires', 'no_client_retry', 'no_manager_retry', 'engine_failure_action_retry',
         'registry_failure_retry', 'empty_registry', 'volume_scaled', 'config_fallback',
         'raw_volume_no_cooldown', 'raw_null_event', 'raw_no_client', 'raw_no_manager', 'raw_failure_action_retry')


class Wiring(unittest.TestCase):
    def test_all_cooldowns_are_monotonic(self):
        self.assertNotIn('System.currentTimeMillis()', SOURCE)
        self.assertIn('System.nanoTime()', method(SOURCE, 'public static void playHover('))

    def test_failure_does_not_consume_family_cooldown(self):
        body = method(SOURCE, 'private static void play(')
        self.assertLess(body.index('mc.getSoundManager().play('), body.index('lastPlayed.put('))
        self.assertIn('catch (Exception ignored)', body)
        self.assertNotIn('getOrDefault(key, 0L)', SOURCE)

    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_ui_sounds.py --require-java', (ROOT / path).read_text())


class Executable(unittest.TestCase):
    def test_complete_ui_sound_class(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        with tempfile.TemporaryDirectory() as tmp:
            work = Path(tmp)
            sources = dict(STUBS)
            sources[PREFIX+'client/audio/UiSounds.java'] = SOURCE
            sources['UiSoundsHarness.java'] = HARNESS
            paths = []
            for name, text in sources.items():
                path = work / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            for case in CASES:
                with self.subTest(case=case):
                    subprocess.run(['java', '-cp', tmp, 'UiSoundsHarness', case], check=True, timeout=15)
            print(f'UI sounds: {len(CASES)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
