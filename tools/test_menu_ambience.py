#!/usr/bin/env python3
"""Compile complete MenuAmbience with deterministic registry/config/audio doubles.

Checks lifecycle ownership, silent/world guards, reload recovery and failures.
No native audio, Minecraft client, actual resource reload or TOML disk I/O.
"""
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method
from test_menu_music import STUBS as MUSIC_STUBS

ROOT = Path(__file__).resolve().parents[1]
SOURCE = (ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/audio/MenuAmbience.java').read_text()
PREFIX = 'com/eternalcraft/gripeverde/'
STUBS = dict(MUSIC_STUBS)
STUBS.update({
    'net/minecraft/util/RandomSource.java': '''package net.minecraft.util;
public class RandomSource {
    public static RandomSource create() { return new RandomSource(); }
    public double nextDouble() { return 0.5; }
    public float nextFloat() { return 0.5f; }
    public int nextInt(int max) { return 0; }
}''',
    'net/minecraft/sounds/SoundSource.java': 'package net.minecraft.sounds; public enum SoundSource { MASTER }',
    'net/minecraft/client/resources/sounds/SoundInstance.java': '''package net.minecraft.client.resources.sounds;
public interface SoundInstance { enum Attenuation { NONE } }''',
    'net/minecraft/client/resources/sounds/SimpleSoundInstance.java': '''package net.minecraft.client.resources.sounds;
public class SimpleSoundInstance {
    public final float volume;
    public SimpleSoundInstance(String loc, net.minecraft.sounds.SoundSource source, float volume, float pitch,
            net.minecraft.util.RandomSource random, boolean loop, int delay, SoundInstance.Attenuation attenuation,
            double x, double y, double z, boolean relative) { this.volume=volume; }
}''',
    PREFIX+'config/ClientConfig.java': '''package com.eternalcraft.gripeverde.config;
public class ClientConfig {
    public static Value<Double> AMBIENCE_VOLUME = new Value<>(1.0);
    public static Value<Boolean> AMBIENCE_ENABLED = new Value<>(true);
    public static class Value<T> {
        public T value;
        public int sets, saves;
        public Value(T value) { this.value=value; }
        public T get() { return value; }
        public void set(T value) { this.value=value; sets++; }
        public void save() { saves++; }
    }
}''',
    PREFIX+'client/audio/LoopingAmbienceInstance.java': '''package com.eternalcraft.gripeverde.client.audio;
public class LoopingAmbienceInstance {
    public float volume;
    public boolean done;
    public int updates;
    public LoopingAmbienceInstance(String loc,float volume) { this.volume=volume; }
    public void setDynamicVolume(float volume) { this.volume=volume; updates++; }
    public void markDone() { done=true; }
}''',
    PREFIX+'client/audio/MenuMusic.java': '''package com.eternalcraft.gripeverde.client.audio;
public class MenuMusic { public static int ducks; public static void duckFor(long ms) { ducks++; } }''',
    PREFIX+'client/lore/Discoveries.java': '''package com.eternalcraft.gripeverde.client.lore;
public class Discoveries {
    public static final String WHISPER_ANSWER="answer", STORM_NIGHT="storm";
    public static int calls;
    public static boolean discover(String id) { calls++; return true; }
}''',
    PREFIX+'client/lore/ChroniclerMemory.java': '''package com.eternalcraft.gripeverde.client.lore;
public class ChroniclerMemory { public static int answers; public static int bump(String id) { return ++answers; } }''',
    PREFIX+'client/lore/Marginalia.java': '''package com.eternalcraft.gripeverde.client.lore;
public class Marginalia {
    public static final String N_WHISPER3="three", N_STORM="storm";
    public static int calls;
    public static void queueOnce(String id) { calls++; }
}''',
    PREFIX+'client/render/LivingAtmosphere.java': '''package com.eternalcraft.gripeverde.client.render;
public class LivingAtmosphere { public static void triggerLightningFlash(float strength) {} }''',
    'net/minecraft/client/Minecraft.java': '''package net.minecraft.client;
public class Minecraft {
    public static Minecraft instance=new Minecraft();
    public Object level;
    public SoundManager sounds=new SoundManager();
    public static Minecraft getInstance() { return instance; }
    public SoundManager getSoundManager() { return sounds; }
    public static class SoundManager {
        public java.util.Set<Object> active=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        public int plays, stops;
        public boolean failNextPlay, failNextStop;
        public void play(Object sound) {
            if (failNextPlay) { failNextPlay=false; throw new IllegalStateException("play failed"); }
            active.add(sound); plays++;
        }
        public void stop(Object sound) {
            stops++;
            if (failNextStop) { failNextStop=false; throw new IllegalStateException("stop failed"); }
            active.remove(sound);
        }
        public boolean isActive(Object sound) { return active.contains(sound); }
    }
}''',
})
# Keep registry symbols synchronized with production, without copying its logic.
STUBS[PREFIX+'client/audio/ModSounds.java'] = '''package com.eternalcraft.gripeverde.client.audio;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;
public class ModSounds {\n''' + '\n'.join(
    f'public static final RegistryObject<SoundEvent> {name}=new RegistryObject<>(new SoundEvent());'
    for name in sorted(set(re.findall(r'ModSounds\.(MENU_\w+)', SOURCE)))
) + '\n}'

HARNESS = r'''import java.lang.reflect.*;
import java.util.*;
import com.eternalcraft.gripeverde.client.audio.*;
import com.eternalcraft.gripeverde.client.lore.*;
import com.eternalcraft.gripeverde.config.ClientConfig;
import net.minecraft.client.Minecraft;
public class AmbienceHarness {
    static Object field(String name) throws Exception {
        Field f=MenuAmbience.class.getDeclaredField(name); f.setAccessible(true); return f.get(null);
    }
    static void set(String name,Object value) throws Exception {
        Field f=MenuAmbience.class.getDeclaredField(name); f.setAccessible(true); f.set(null,value);
    }
    static void check(boolean ok,String why) { if (!ok) throw new AssertionError(why); }
    static void near(float a,float b) { check(Math.abs(a-b)<0.000001f,a+" != "+b); }
    static List<LoopingAmbienceInstance> loops() throws Exception {
        List<LoopingAmbienceInstance> result=new ArrayList<>();
        for (Object loop:(List<?>)field("BASE_LOOPS")) {
            Field f=loop.getClass().getDeclaredField("instance"); f.setAccessible(true);
            if (f.get(loop)!=null) result.add((LoopingAmbienceInstance)f.get(loop));
        }
        return result;
    }
    static void expectLoops(int count) throws Exception { check(loops().size()==count,"wrong owned loop count: "+loops().size()); }
    static void whisper() throws Exception { set("whisperRespondableUntilMs",System.nanoTime()/1_000_000L+100_000L); }
    static void noReward() {
        check(ChroniclerMemory.answers==0 && Discoveries.calls==0 && Marginalia.calls==0,"inaudible/stale whisper rewarded");
    }
    static void stopped(List<LoopingAmbienceInstance> old) throws Exception {
        check(!MenuAmbience.isActive(),"lifecycle still active"); expectLoops(0);
        check(!MenuAmbience.isWhisperOpen() && !MenuAmbience.isCrowsRecentlyTriggered(),"stale audible window");
        for (LoopingAmbienceInstance loop:old) check(loop.done,"retired loop not marked done");
    }
    public static void main(String[] args) throws Exception {
        Minecraft mc=Minecraft.instance;
        String test=args[0];
        set("stormNight",false);
        if (test.equals("start_in_world")) {
            mc.level=new Object(); MenuAmbience.start();
            check(!MenuAmbience.isActive() && mc.sounds.plays==0,"menu ambience started in world"); return;
        }
        if (test.equals("start_at_zero")) {
            MenuAmbience.setUserVolumeLive(0); MenuAmbience.start(); expectLoops(0);
            check(mc.sounds.plays==0,"silent startup allocated loops");
            MenuAmbience.setUserVolumeLive(1); MenuAmbience.tick(); expectLoops(3); return;
        }
        if (test.equals("start_disabled")) {
            ClientConfig.AMBIENCE_ENABLED.set(false); MenuAmbience.start(); expectLoops(0);
            check(MenuAmbience.isMuted(),"disabled config ignored"); return;
        }
        if (test.equals("inactive_clock")) {
            MenuAmbience.setPocketWatchVisible(true); MenuAmbience.onSecondTick();
            check(mc.sounds.plays==0,"inactive clock played"); return;
        }
        MenuAmbience.start(); expectLoops(3);
        List<LoopingAmbienceInstance> old=loops();
        int plays=mc.sounds.plays;
        switch (test) {
            case "start_idempotent" -> {
                MenuAmbience.start(); MenuAmbience.start();
                check(mc.sounds.plays==plays && loops().equals(old),"start replaced live loops");
            }
            case "world_entry", "world_entry_zero", "start_after_world_entry" -> {
                whisper(); set("crowsPlayingUntilMs",Long.MAX_VALUE); mc.level=new Object();
                if (test.equals("world_entry_zero")) MenuAmbience.setUserVolumeLive(0);
                if (test.equals("start_after_world_entry")) MenuAmbience.start(); else MenuAmbience.tick();
                stopped(old); check(mc.sounds.active.isEmpty(),"world entry retained channels");
                mc.level=null; MenuAmbience.setUserVolumeLive(1); MenuAmbience.start(); expectLoops(3);
                check(MenuAmbience.isActive() && mc.sounds.active.size()==3,"menu return could not restart");
            }
            case "stop", "stop_without_client", "stop_failure" -> {
                whisper(); set("crowsPlayingUntilMs",Long.MAX_VALUE);
                if (test.equals("stop_without_client")) Minecraft.instance=null;
                if (test.equals("stop_failure")) mc.sounds.failNextStop=true;
                MenuAmbience.stop(); stopped(old);
                int stops=mc.sounds.stops; MenuAmbience.stop();
                check(mc.sounds.stops==stops,"stop repeated engine calls");
                if (test.equals("stop_failure")) check(stops==3,"one failure skipped other loops");
            }
            case "reload_live", "reload_after_engine_clear", "reload_muted", "reload_zero" -> {
                if (test.equals("reload_after_engine_clear")) mc.sounds.active.clear();
                if (test.equals("reload_muted")) MenuAmbience.setMuted(true);
                if (test.equals("reload_zero")) MenuAmbience.setUserVolumeLive(0);
                MenuAmbience.recoverAfterResourceReload();
                for (LoopingAmbienceInstance loop:old) check(loop.done,"reload dropped owned channel");
                int expected=(test.equals("reload_muted")||test.equals("reload_zero")) ? 0 : 3;
                expectLoops(expected); check(mc.sounds.active.size()==expected,"reload leaked channels");
                MenuAmbience.recoverAfterResourceReload();
                check(mc.sounds.active.size()==expected,"second reload stacked channels");
            }
            case "zero_resume" -> {
                whisper(); MenuAmbience.setUserVolumeLive(0); MenuAmbience.tick(); expectLoops(0);
                check(!MenuAmbience.isWhisperOpen() && mc.sounds.active.isEmpty(),"zero retained sound or whisper");
                for (LoopingAmbienceInstance loop:old) check(loop.done,"zero dropped live loop");
                MenuAmbience.setUserVolumeLive(0.5f); MenuAmbience.tick(); expectLoops(3);
                near(loops().get(0).volume,0.175f);
            }
            case "mute_resume" -> {
                whisper(); MenuAmbience.setMuted(true); expectLoops(0);
                check(!MenuAmbience.isWhisperOpen() && !ClientConfig.AMBIENCE_ENABLED.get(),"mute state not synchronized");
                MenuAmbience.setMuted(false); expectLoops(3);
                check(mc.sounds.active.size()==3 && ClientConfig.AMBIENCE_ENABLED.get(),"unmute failed");
            }
            case "same_mute_persists" -> {
                ClientConfig.AMBIENCE_ENABLED.value=false;
                MenuAmbience.setMuted(false);
                check(ClientConfig.AMBIENCE_ENABLED.get() && ClientConfig.AMBIENCE_ENABLED.saves==1,"same-state reset lost config write");
                check(loops().equals(old) && mc.sounds.plays==plays,"same-state request rebuilt loops");
            }
            case "clock_muted", "clock_zero", "clock_world", "clock_hidden", "clock_normal" -> {
                MenuAmbience.setPocketWatchVisible(!test.equals("clock_hidden"));
                if (test.equals("clock_muted")) MenuAmbience.setMuted(true);
                if (test.equals("clock_zero")) MenuAmbience.setUserVolumeLive(0);
                if (test.equals("clock_world")) mc.level=new Object();
                MenuAmbience.onSecondTick();
                check(mc.sounds.plays==plays+(test.equals("clock_normal")?1:0),"clock guard failed: "+test);
            }
            case "whisper_muted", "whisper_zero", "whisper_world", "whisper_inactive", "whisper_normal" -> {
                if (test.equals("whisper_muted")) MenuAmbience.setMuted(true);
                if (test.equals("whisper_zero")) MenuAmbience.setUserVolumeLive(0);
                if (test.equals("whisper_world")) mc.level=new Object();
                if (test.equals("whisper_inactive")) MenuAmbience.stop();
                whisper(); MenuAmbience.answerWhisper();
                check(!MenuAmbience.isWhisperOpen(),"whisper remained open");
                if (test.equals("whisper_normal")) {
                    check(mc.sounds.plays==plays+2 && ChroniclerMemory.answers==1,"audible whisper did not answer");
                    MenuAmbience.answerWhisper();
                    check(mc.sounds.plays==plays+2 && ChroniclerMemory.answers==1,"whisper answered twice");
                } else { check(mc.sounds.plays==plays,"inaudible whisper played"); noReward(); }
            }
            case "missing_loop_retry" -> {
                mc.sounds.active.remove(old.get(0)); MenuAmbience.tick(); expectLoops(3);
                check(mc.sounds.active.size()==3 && mc.sounds.plays==plays+1,"missing channel not recovered exactly once");
            }
            case "play_failure_retry" -> {
                MenuAmbience.stop(); mc.sounds.failNextPlay=true; MenuAmbience.start(); expectLoops(2);
                MenuAmbience.tick(); expectLoops(3); check(mc.sounds.active.size()==3,"failed play could not retry");
            }
            case "live_volume" -> {
                MenuAmbience.setUserVolumeLive(0.5f); MenuAmbience.tick();
                check(loops().equals(old) && mc.sounds.plays==plays,"gain edit rebuilt loops");
                near(old.get(0).volume,0.175f); near(old.get(1).volume,0.225f);
                check(ClientConfig.AMBIENCE_VOLUME.saves==0,"live gain wrote config");
            }
            default -> throw new AssertionError(test);
        }
    }
}
'''

CASES = ('start_in_world', 'start_at_zero', 'start_disabled', 'inactive_clock', 'start_idempotent',
         'world_entry', 'world_entry_zero', 'start_after_world_entry', 'stop', 'stop_without_client',
         'stop_failure', 'reload_live', 'reload_after_engine_clear', 'reload_muted', 'reload_zero',
         'zero_resume', 'mute_resume', 'same_mute_persists', 'clock_muted', 'clock_zero', 'clock_world',
         'clock_hidden', 'clock_normal', 'whisper_muted', 'whisper_zero', 'whisper_world',
         'whisper_inactive', 'whisper_normal', 'missing_loop_retry', 'play_failure_retry', 'live_volume')


class Wiring(unittest.TestCase):
    def test_world_cleanup_precedes_silent_return(self):
        body = method(SOURCE, 'public static void tick(')
        self.assertLess(body.index('if(mc.level!=null)'), body.index('if(ambVol<0.01f)'))

    def test_reload_retires_owned_loops(self):
        body = method(SOURCE, 'public static void recoverAfterResourceReload(')
        self.assertLess(body.index('stop();'), body.index('start();'))
        self.assertNotIn('bl.instance=null', body)

    def test_stop_clears_state_without_client(self):
        body = method(SOURCE, 'public static void stop(')
        self.assertNotIn('return;', body)
        self.assertIn('stopBaseLoops(Minecraft.getInstance())', body)

    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_menu_ambience.py --require-java', (ROOT / path).read_text())


class Executable(unittest.TestCase):
    def test_complete_ambience_class(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        with tempfile.TemporaryDirectory() as tmp:
            work = Path(tmp)
            sources = dict(STUBS)
            sources[PREFIX+'client/audio/MenuAmbience.java'] = SOURCE
            sources['AmbienceHarness.java'] = HARNESS
            paths = []
            for name, text in sources.items():
                path = work / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            for case in CASES:
                with self.subTest(case=case):
                    subprocess.run(['java', '-cp', tmp, 'AmbienceHarness', case], check=True, timeout=15)
            print(f'Menu ambience: {len(CASES)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
