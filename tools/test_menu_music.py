#!/usr/bin/env python3
"""Compile complete MenuMusic against fake registry, config and sound engine.

Timers are advanced through reflected deadlines, not sleeps. These are state
machine/lifecycle checks, not actual audio playback or crossfade listening.
CI requires JDK 17; structural guards run locally without it.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method

ROOT = Path(__file__).resolve().parents[1]
SOURCE = (ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/audio/MenuMusic.java').read_text()

STUBS = {
    'org/slf4j/Logger.java': '''package org.slf4j;
public class Logger { public void info(String s, Object... args) {} public void warn(String s, Object... args) {} }''',
    'com/mojang/logging/LogUtils.java': '''package com.mojang.logging;
public class LogUtils { public static org.slf4j.Logger getLogger() { return new org.slf4j.Logger(); } }''',
    'net/minecraft/sounds/SoundEvent.java': '''package net.minecraft.sounds;
public class SoundEvent { public String getLocation() { return "test:sound"; } }''',
    'net/minecraft/util/RandomSource.java': '''package net.minecraft.util;
public class RandomSource {
    public static RandomSource create() { return new RandomSource(); }
    public double nextDouble() { return 0.5; }
}''',
    'net/minecraftforge/registries/RegistryObject.java': '''package net.minecraftforge.registries;
public class RegistryObject<T> {
    public T value;
    public boolean fail;
    public RegistryObject(T value) { this.value = value; }
    public T get() { if (fail) throw new IllegalStateException("registry unavailable"); return value; }
}''',
    'com/eternalcraft/gripeverde/config/ClientConfig.java': '''package com.eternalcraft.gripeverde.config;
public class ClientConfig {
    public static Value<Double> MUSIC_VOLUME = new Value<>(0.4);
    public static Value<Boolean> MENU_MUSIC_ENABLED = new Value<>(true);
    public static class Value<T> {
        T value;
        public Value(T value) { this.value = value; }
        public T get() { return value; }
        public void set(T value) { this.value = value; }
        public void save() {}
    }
}''',
    'com/eternalcraft/gripeverde/client/audio/ModSounds.java': '''package com.eternalcraft.gripeverde.client.audio;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;
public class ModSounds {
    public static final RegistryObject<SoundEvent> MENU_MUSIC_1 = new RegistryObject<>(new SoundEvent());
    public static final RegistryObject<SoundEvent> MENU_MUSIC_2 = new RegistryObject<>(new SoundEvent());
    public static final RegistryObject<SoundEvent> MENU_MUSIC_3 = new RegistryObject<>(new SoundEvent());
    public static final RegistryObject<SoundEvent> MENU_MUSIC_4 = new RegistryObject<>(new SoundEvent());
    public static final RegistryObject<SoundEvent> MENU_AMBIENT_OCCASIONAL = new RegistryObject<>(new SoundEvent());
    public static final RegistryObject<SoundEvent> MENU_TICK = new RegistryObject<>(new SoundEvent());
}''',
    'com/eternalcraft/gripeverde/client/audio/FadingMusicInstance.java': '''package com.eternalcraft.gripeverde.client.audio;
public class FadingMusicInstance {
    public long elapsed = 2000, duration;
    public boolean stopped;
    public float volume;
    public FadingMusicInstance(net.minecraft.sounds.SoundEvent sound, float volume, long duration) {
        this.volume = volume; this.duration = duration;
    }
    public void markDone() { stopped = true; }
    public boolean isStopped() { return stopped; }
    public long getElapsedMs() { return elapsed; }
    public long getRemainingMs() { return Math.max(0, duration - elapsed); }
    public void setDynamicVolume(float volume) { this.volume = volume; }
}''',
    'com/eternalcraft/gripeverde/client/audio/UiSounds.java': '''package com.eternalcraft.gripeverde.client.audio;
public class UiSounds { public static void playCustom(Object sound, float volume, float pitch) {} }''',
    'com/eternalcraft/gripeverde/client/lore/Discoveries.java': '''package com.eternalcraft.gripeverde.client.lore;
public class Discoveries {
    public static final String GHOST_TRACK = "ghost_track";
    public static int calls;
    public static boolean discover(String id) { calls++; return true; }
}''',
    'net/minecraft/client/Minecraft.java': '''package net.minecraft.client;
import com.eternalcraft.gripeverde.client.audio.FadingMusicInstance;
public class Minecraft {
    public static Minecraft instance = new Minecraft();
    public Object level;
    public SoundManager sounds = new SoundManager();
    public MusicManager music = new MusicManager();
    public static Minecraft getInstance() { return instance; }
    public SoundManager getSoundManager() { return sounds; }
    public MusicManager getMusicManager() { return music; }
    public Object getSituationalMusic() { return new Object(); }
    public static class MusicManager {
        public boolean isPlayingMusic(Object music) { return false; }
        public void stopPlaying() {}
    }
    public static class SoundManager {
        public java.util.Set<FadingMusicInstance> active = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        public int plays, stops;
        public boolean failNext;
        public void play(FadingMusicInstance sound) {
            if (failNext) { failNext = false; throw new IllegalStateException("play failed"); }
            active.add(sound); plays++;
        }
        public void stop(FadingMusicInstance sound) { active.remove(sound); stops++; }
        public boolean isActive(FadingMusicInstance sound) { return active.contains(sound); }
    }
}''',
}

HARNESS = r'''import java.lang.reflect.*;
import com.eternalcraft.gripeverde.client.audio.*;
import com.eternalcraft.gripeverde.client.lore.Discoveries;
import com.eternalcraft.gripeverde.config.ClientConfig;
import net.minecraft.client.Minecraft;
public class MenuMusicHarness {
    static Object field(String name) throws Exception {
        Field f = MenuMusic.class.getDeclaredField(name); f.setAccessible(true); return f.get(null);
    }
    static void set(String name, Object value) throws Exception {
        Field f = MenuMusic.class.getDeclaredField(name); f.setAccessible(true); f.set(null, value);
    }
    static void check(boolean value, String reason) { if (!value) throw new AssertionError(reason); }
    static FadingMusicInstance current() throws Exception { return (FadingMusicInstance)field("currentInstance"); }
    static void gap() throws Exception {
        MenuMusic.start(); FadingMusicInstance old = current();
        Minecraft.instance.sounds.stop(old); old.markDone(); MenuMusic.tick();
        check(current() == null && (Boolean)field("waitingForNext"), "track ending must enter gap");
        check(Minecraft.instance.sounds.active.isEmpty(), "ended track remains active in fixture");
    }
    static void expectOneTrack() throws Exception {
        check(current() != null && Minecraft.instance.sounds.active.size() == 1, "expected exactly one tracked record");
        check(!(Boolean)field("waitingForNext") && (Long)field("nextTrackAtMs") == 0L, "successful start retained obsolete gap");
        check(MenuMusic.getCurrentTrackNumber() >= 1 && MenuMusic.getCurrentTrackNumber() <= 4, "track number invalid");
    }
    public static void main(String[] args) throws Exception {
        Minecraft mc = Minecraft.instance;
        switch (args[0]) {
            case "skip_in_gap" -> {
                gap(); MenuMusic.skipToNext(); expectOneTrack();
                int plays = mc.sounds.plays; Object sound = current();
                // The old deadline would now be expired, but is no longer armed.
                set("nextTrackAtMs", 0L); MenuMusic.tick(); MenuMusic.tick();
                check(mc.sounds.plays == plays && current() == sound && mc.sounds.active.size() == 1,
                      "old gap timer started an overlapping untracked record");
            }
            case "gap_expires" -> {
                gap(); set("nextTrackAtMs", 0L); MenuMusic.tick(); expectOneTrack();
                int plays = mc.sounds.plays; MenuMusic.tick(); check(mc.sounds.plays == plays, "gap starts more than one track");
            }
            case "skip_failure_retry" -> {
                gap(); mc.sounds.failNext = true; MenuMusic.skipToNext();
                check(current() == null && (Boolean)field("waitingForNext"), "failed start lost gap retry");
                check(mc.sounds.active.isEmpty(), "failed start left a playing instance");
                MenuMusic.skipToNext(); expectOneTrack();
            }
            case "registry_failure_retry" -> {
                gap();
                ModSounds.MENU_MUSIC_1.fail = ModSounds.MENU_MUSIC_2.fail = true;
                ModSounds.MENU_MUSIC_3.fail = ModSounds.MENU_MUSIC_4.fail = true;
                MenuMusic.skipToNext();
                check(current() == null && (Boolean)field("waitingForNext"), "registry failure lost retry");
                ModSounds.MENU_MUSIC_1.fail = ModSounds.MENU_MUSIC_2.fail = false;
                ModSounds.MENU_MUSIC_3.fail = ModSounds.MENU_MUSIC_4.fail = false;
                MenuMusic.skipToNext(); expectOneTrack();
            }
            case "ghost_pause_skip" -> {
                MenuMusic.start(); set("manualSkipsThisSession", 4); MenuMusic.skipToNext();
                check(current() == null && (Boolean)field("waitingForNext") && Discoveries.calls == 1, "ghost pause not entered");
                MenuMusic.skipToNext(); expectOneTrack();
                int plays = mc.sounds.plays; set("nextTrackAtMs", 0L); MenuMusic.tick();
                check(mc.sounds.plays == plays && Discoveries.calls == 1, "ghost pause restarted a track/reward");
            }
            case "crossfade_ignores_skip" -> {
                MenuMusic.start(); MenuMusic.skipToNext();
                check(MenuMusic.isCrossfading() && mc.sounds.active.size() == 2, "normal skip must crossfade");
                int plays = mc.sounds.plays; MenuMusic.skipToNext();
                check(mc.sounds.plays == plays && mc.sounds.active.size() == 2, "skip during crossfade started third track");
                set("crossfadeStartMs", System.nanoTime() / 1000000L - 5000L); MenuMusic.tick();
                check(!MenuMusic.isCrossfading() && mc.sounds.active.size() == 1 && field("previousInstance") == null,
                      "completed crossfade did not release previous track");
            }
            case "inactive_skip" -> {
                MenuMusic.skipToNext(); check(mc.sounds.plays == 0 && !MenuMusic.isActive(), "inactive skip started music");
            }
            case "stop_in_gap" -> {
                gap(); MenuMusic.stop(); set("nextTrackAtMs", 0L); MenuMusic.tick();
                check(!MenuMusic.isActive() && !(Boolean)field("waitingForNext") && mc.sounds.active.isEmpty(), "stop retained scheduled playback");
            }
            case "world_entry" -> {
                MenuMusic.start(); mc.level = new Object(); MenuMusic.tick();
                check(!MenuMusic.isActive() && mc.sounds.active.isEmpty(), "menu music leaked into world");
            }
            case "disabled_during_gap" -> {
                gap(); ClientConfig.MENU_MUSIC_ENABLED.set(false); set("nextTrackAtMs", 0L); MenuMusic.tick();
                check(!MenuMusic.isActive() && mc.sounds.active.isEmpty(), "disabled music restarted after gap");
            }
            case "regular_start" -> {
                MenuMusic.start(); expectOneTrack(); int plays = mc.sounds.plays;
                MenuMusic.start(); check(mc.sounds.plays == plays, "repeated start duplicated active track");
                MenuMusic.stop(); check(mc.sounds.active.isEmpty(), "stop did not clean up track");
            }
            default -> throw new AssertionError(args[0]);
        }
    }
}
'''


class Wiring(unittest.TestCase):
    def test_success_clears_gap_after_play(self):
        body = method(SOURCE, 'private static void playNextTrack(')
        self.assertLess(body.index('mc.getSoundManager().play(currentInstance)'), body.index('waitingForNext = false'))
        self.assertIn('nextTrackAtMs = 0L', body)

    def test_failure_keeps_retry(self):
        body = method(SOURCE, 'private static void playNextTrack(')
        self.assertIn('currentInstance = null;\n            queueNextWithGap();', body)

    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_menu_music.py --require-java', (ROOT / path).read_text())


class Executable(unittest.TestCase):
    def test_complete_music_class(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        cases = ('skip_in_gap', 'gap_expires', 'skip_failure_retry', 'registry_failure_retry',
                 'ghost_pause_skip', 'crossfade_ignores_skip', 'inactive_skip', 'stop_in_gap',
                 'world_entry', 'disabled_during_gap', 'regular_start')
        with tempfile.TemporaryDirectory() as tmp:
            work = Path(tmp)
            sources = dict(STUBS)
            sources['com/eternalcraft/gripeverde/client/audio/MenuMusic.java'] = SOURCE
            sources['MenuMusicHarness.java'] = HARNESS
            paths = []
            for name, text in sources.items():
                path = work / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            for case in cases:
                with self.subTest(case=case):
                    subprocess.run(['java', '-cp', tmp, 'MenuMusicHarness', case], check=True, timeout=15)
            print(f'Menu music: {len(cases)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
