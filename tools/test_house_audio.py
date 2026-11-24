#!/usr/bin/env python3
"""Test real house slider factories/reset and real audio volume-cache methods.

The slider, Forge values, filesystem and sound engine are doubles. Tests cover
callback wiring, live state versus persisted state, labels, rebuild and reset;
they do not launch a screen, play audio or verify real config disk writes.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method

ROOT = Path(__file__).resolve().parents[1]
HOUSE = (ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/screens/DiaryHouseScreen.java').read_text()
AUDIO = {name: (ROOT / f'src/main/java/com/eternalcraft/gripeverde/client/audio/{name}.java').read_text()
         for name in ('MenuMusic', 'MenuAmbience')}

CONFIG = '''package com.eternalcraft.gripeverde.config;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
public class ClientConfig {
    public static Value<Double> MUSIC_VOLUME = new Value<>(0.4), AMBIENCE_VOLUME = new Value<>(1.0), UI_VOLUME = new Value<>(0.6);
    public static Value<Integer> HUD_WATCH_CORNER = new Value<>(2);
    public static BooleanValue MENU_MUSIC_ENABLED = new BooleanValue(false), AMBIENCE_ENABLED = new BooleanValue(false),
        REDUCED_MOTION = new BooleanValue(true), REDUCE_FLASHES = new BooleanValue(true), HUD_WATCH_ENABLED = new BooleanValue(false),
        REPLACE_PAUSE = new BooleanValue(false), REPLACE_TITLE = new BooleanValue(false);
    public static class Value<T> {
        public T value;
        public int sets, saves;
        public Value(T value) { this.value=value; }
        public T get() { return value; }
        public void set(T value) { this.value=value; sets++; }
        public void save() { saves++; }
    }
}
'''
FORGE = '''package net.minecraftforge.common;
public class ForgeConfigSpec {
    public static class BooleanValue {
        public boolean value;
        public int saves;
        public BooleanValue(boolean value) { this.value=value; }
        public void set(boolean value) { this.value=value; }
        public void save() { saves++; }
    }
}
'''
SLIDER = '''package com.eternalcraft.gripeverde.client.widgets;
import java.util.function.*;
public class DiarySlider {
    public double value;
    public final boolean live;
    private final DoubleFunction<String> label;
    private final DoubleConsumer changed;
    private final Runnable released;
    public DiarySlider(int x,int y,int w,int h,double value,DoubleFunction<String> label,
                       DoubleConsumer changed,boolean live,Runnable released) {
        this.value=value; this.label=label; this.changed=changed; this.live=live; this.released=released;
    }
    public void move(double value) { this.value=value; if (live) changed.accept(value); }
    public void release() { changed.accept(value); released.run(); }
    public String label() { return label.apply(value); }
}
'''

HARNESS = '''package com.eternalcraft.gripeverde.client.screens;
import com.eternalcraft.gripeverde.client.audio.*;
import com.eternalcraft.gripeverde.client.widgets.DiarySlider;
import com.eternalcraft.gripeverde.config.ClientConfig;
public class HouseAudioHarness {
    static boolean english = true;
    static boolean en() { return english; }
    int rebuilds;
    void rebuildWidgets() { rebuilds++; }
    __METHODS__
    static void check(boolean value,String why) { if (!value) throw new AssertionError(why); }
    static void near(double actual,double expected) { check(Math.abs(actual-expected)<0.000001,actual+" != "+expected); }
    static double volume(String kind) { return kind.equals("music") ? MenuMusic.getUserVolume() : MenuAmbience.getUserVolume(); }
    static ClientConfig.Value<Double> config(String kind) { return kind.equals("music") ? ClientConfig.MUSIC_VOLUME : ClientConfig.AMBIENCE_VOLUME; }
    static DiarySlider slider(String kind) { return kind.equals("music") ? createMusicSlider(10,20,100) : createAmbienceSlider(10,20,100); }
    static void resetAssertions(HouseAudioHarness house) {
        near(MenuMusic.getUserVolume(),0.4); near(MenuAmbience.getUserVolume(),1.0);
        near(ClientConfig.MUSIC_VOLUME.get(),0.4); near(ClientConfig.AMBIENCE_VOLUME.get(),1.0);
        near(ClientConfig.UI_VOLUME.get(),1.0);
        check(ClientConfig.MUSIC_VOLUME.saves==1 && ClientConfig.AMBIENCE_VOLUME.saves==1 && ClientConfig.UI_VOLUME.saves==1,
              "reset must persist each volume once");
        check(ClientConfig.HUD_WATCH_CORNER.get()==0 && ClientConfig.HUD_WATCH_CORNER.saves==1,"corner reset changed");
        check(ClientConfig.MENU_MUSIC_ENABLED.value && ClientConfig.AMBIENCE_ENABLED.value && ClientConfig.HUD_WATCH_ENABLED.value
            && ClientConfig.REPLACE_PAUSE.value && ClientConfig.REPLACE_TITLE.value,"reset failed to enable default switches");
        check(!ClientConfig.REDUCED_MOTION.value && !ClientConfig.REDUCE_FLASHES.value,"reset failed to clear reduced effects");
        check(house.rebuilds==1,"reset must rebuild once after updating state");
        near(createMusicSlider(0,0,100).value,0.4); near(createAmbienceSlider(0,0,100).value,1.0);
    }
    public static void main(String[] args) {
        // Cache both channels before touching the house: this is the failing
        // production scenario, not a first-load config getter coincidence.
        near(MenuMusic.getUserVolume(),0.4); near(MenuAmbience.getUserVolume(),1.0);
        String test=args[0],kind=args[1];
        HouseAudioHarness house=new HouseAudioHarness();
        if (test.startsWith("reset")) {
            if (test.equals("reset_after_drag")) {
                createMusicSlider(0,0,100).move(0.2); createAmbienceSlider(0,0,100).move(0.3);
            } else {
                MenuMusic.setUserVolumeLive(0.2f); MenuAmbience.setUserVolumeLive(0.3f);
            }
            if (test.equals("reset_labels")) english=false;
            house.resetHouse(); resetAssertions(house);
            if (test.equals("reset_labels")) {
                check(createMusicSlider(0,0,100).label().equals("musica 40%"),"music reset label stale");
                check(createAmbienceSlider(0,0,100).label().equals("ambiente 100%"),"ambience reset label stale");
            }
            return;
        }
        double original=volume(kind);
        var config=config(kind); var otherConfig=config(kind.equals("music") ? "ambience" : "music");
        DiarySlider control=slider(kind);
        check(control.live,"house slider must preview while dragging"); near(control.value,original);
        switch(test) {
            case "initial_cache" -> {
                config.value=0.8;
                near(slider(kind).value,original);
                check(config.sets==0 && config.saves==0,"building slider must not persist");
            }
            case "live_no_save" -> {
                control.move(0.2); near(volume(kind),0.2); near(config.get(),original);
                check(config.sets==0 && config.saves==0,"preview must not write config");
            }
            case "release" -> {
                control.move(0.65); control.release(); near(volume(kind),0.65); near(config.get(),0.65);
                check(config.sets==1 && config.saves==1,"release persists once");
                near(slider(kind).value,0.65);
            }
            case "rebuild" -> {
                control.move(0.35); DiarySlider rebuilt=slider(kind); near(rebuilt.value,0.35);
                check(config.saves==0,"rebuild must not persist preview");
                rebuilt.release(); near(config.get(),0.35); check(config.saves==1,"rebuilt control cannot commit cached value");
            }
            case "round_trip" -> {
                control.move(0); control.release(); near(volume(kind),0); near(config.get(),0);
                control.move(1); control.release(); near(volume(kind),1); near(config.get(),1);
                check(config.sets==2 && config.saves==2,"two commits must persist twice");
            }
            case "zero_volume" -> {
                control.move(0); near(volume(kind),0); near(config.get(),original);
                control.release(); near(config.get(),0); check(config.saves==1,"muted value not persisted");
            }
            default -> throw new AssertionError(test);
        }
        check(otherConfig.sets==0 && otherConfig.saves==0,"one channel changed the other config");
    }
}
'''


class Wiring(unittest.TestCase):
    def test_init_uses_live_factories(self):
        body = method(HOUSE, 'protected void init(')
        self.assertIn('createMusicSlider(col1, cursor, this.toggleW)', body)
        self.assertIn('createAmbienceSlider(col2, cursor, this.toggleW)', body)
        self.assertNotIn('ClientConfig.MUSIC_VOLUME.set(', body)
        self.assertNotIn('ClientConfig.AMBIENCE_VOLUME.set(', body)

    def test_reset_updates_audio_before_rebuild(self):
        body = method(HOUSE, 'private void resetHouse(')
        for name in ('MenuMusic', 'MenuAmbience'):
            self.assertLess(body.index(name + '.setUserVolume('), body.index('this.rebuildWidgets('))

    def test_ci_requires_execution(self):
        for name in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_house_audio.py --require-java', (ROOT / name).read_text())


class Executable(unittest.TestCase):
    def test_house_callbacks_and_audio_volume_cache(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        cases = [(case, kind) for case in ('initial_cache', 'live_no_save', 'release', 'rebuild', 'round_trip', 'zero_volume')
                 for kind in ('music', 'ambience')]
        cases += [(case, 'music') for case in ('reset', 'reset_after_drag', 'reset_labels')]
        with tempfile.TemporaryDirectory() as tmp:
            work=Path(tmp)
            prefix='com/eternalcraft/gripeverde/'
            signatures=('private static DiarySlider createMusicSlider(', 'private static DiarySlider createAmbienceSlider(',
                        'private void resetHouse(', 'private static void setBool(', 'private static String pct(')
            sources={
                prefix+'config/ClientConfig.java': CONFIG,
                prefix+'client/widgets/DiarySlider.java': SLIDER,
                'net/minecraftforge/common/ForgeConfigSpec.java': FORGE,
                prefix+'client/screens/HouseAudioHarness.java': HARNESS.replace('__METHODS__', '\n'.join(method(HOUSE, sig) for sig in signatures)),
            }
            for name, source in AUDIO.items():
                declarations=('private static float userVolume=0.4f; private static boolean volumeLoadedFromConfig;'
                              if name=='MenuMusic' else 'private static float userVolume=1.0f; private static boolean userVolumeLoadedFromConfig;')
                methods='\n'.join(method(source,sig) for sig in ('public static float getUserVolume(',
                                       'public static void setUserVolumeLive(', 'public static void setUserVolume('))
                sources[prefix+f'client/audio/{name}.java'] = ('package com.eternalcraft.gripeverde.client.audio; '
                    'import com.eternalcraft.gripeverde.config.ClientConfig; public class '+name+' { '+declarations+methods+' }')
            paths=[]
            for name,text in sources.items():
                path=work/name; path.parent.mkdir(parents=True,exist_ok=True); path.write_text(text); paths.append(str(path))
            subprocess.run(['javac','--release','17','-encoding','UTF-8','-d',tmp,*paths],check=True,timeout=60)
            for case,kind in cases:
                with self.subTest(case=case,kind=kind):
                    subprocess.run(['java','-cp',tmp,'com.eternalcraft.gripeverde.client.screens.HouseAudioHarness',case,kind],check=True,timeout=15)
            print(f'House audio: {len(cases)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
