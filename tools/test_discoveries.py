#!/usr/bin/env python3
"""Compile the complete Discoveries class against client/audio/memory doubles.

Checks API side effects, queue order and logical rendered text/cache bounds.
No real Minecraft, GPU, audio, resource reload or system clock change is used.
CI requires Java 17; local structural checks work without it.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method

ROOT = Path(__file__).resolve().parents[1]
PATH = ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/lore/Discoveries.java'
SOURCE = PATH.read_text()
CHROME = (ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/render/DiaryChrome.java').read_text()

STUBS = {
    'net/minecraft/client/gui/Font.java': '''package net.minecraft.client.gui;
public class Font {
    public int scale = 1, calls;
    public int width(String text) { calls++; return text.length() * scale; }
}''',
    'net/minecraft/client/gui/GuiGraphics.java': '''package net.minecraft.client.gui;
public class GuiGraphics {
    public java.util.List<String> text = new java.util.ArrayList<>();
    public java.util.List<Integer> positions = new java.util.ArrayList<>();
    public void fill(int a, int b, int c, int d, int color) {}
    public void drawString(Font f, String s, int x, int y, int color, boolean shadow) {
        text.add(s); positions.add(x);
    }
}''',
    'net/minecraft/client/gui/screens/Screen.java': '''package net.minecraft.client.gui.screens;
public class Screen { public int height = 300; }''',
    'net/minecraft/client/Minecraft.java': '''package net.minecraft.client;
public class Minecraft {
    public static Minecraft instance = new Minecraft();
    public static int reads;
    public net.minecraft.client.gui.Font font = new net.minecraft.client.gui.Font();
    public net.minecraft.client.gui.screens.Screen screen;
    public Object level;
    public Options options = new Options();
    public Language language = new Language();
    public static Minecraft getInstance() { reads++; return instance; }
    public Language getLanguageManager() { return language; }
    public static class Options { public boolean renderDebug; }
    public static class Language { public String selected = "en_us"; public String getSelected() { return selected; } }
}''',
    'com/eternalcraft/gripeverde/client/screens/DiaryTitleScreen.java': '''package com.eternalcraft.gripeverde.client.screens;
public class DiaryTitleScreen extends net.minecraft.client.gui.screens.Screen {}''',
    'com/eternalcraft/gripeverde/client/util/MotionPolicy.java': '''package com.eternalcraft.gripeverde.client.util;
public class MotionPolicy { public static boolean isReduced() { return true; } }''',
    'com/eternalcraft/gripeverde/client/render/PaperTexture.java': '''package com.eternalcraft.gripeverde.client.render;
public class PaperTexture {
    public static void drawTornSlip(net.minecraft.client.gui.GuiGraphics g, int x, int y, int w, int h,
                                    int paper, int edge, int alpha) {}
}''',
    'com/eternalcraft/gripeverde/client/audio/ModSounds.java': '''package com.eternalcraft.gripeverde.client.audio;
public class ModSounds {
    public static final ModSounds UI_NOTIFICATION = new ModSounds(), UI_BACK = new ModSounds();
    public Object get() { return this; }
}''',
    'com/eternalcraft/gripeverde/client/audio/UiSounds.java': '''package com.eternalcraft.gripeverde.client.audio;
public class UiSounds {
    public static int plays;
    public static void playCustom(Object sound, float volume, float pitch) { plays++; }
}''',
    'com/eternalcraft/gripeverde/client/audio/MenuMusic.java': '''package com.eternalcraft.gripeverde.client.audio;
public class MenuMusic { public static int ducks; public static void duckFor(long ms) { ducks++; } }''',
    'com/eternalcraft/gripeverde/client/lore/ChroniclerMemory.java': '''package com.eternalcraft.gripeverde.client.lore;
public class ChroniclerMemory {
    public static int reads, marks, seen;
    public static java.util.Set<String> ids = new java.util.HashSet<>();
    public static boolean isDiscovered(String id) { reads++; return ids.contains(id); }
    public static void markDiscovered(String id) { marks++; ids.add(id); }
    public static int discoveredCount() { return seen; }
}''',
}

HARNESS = r'''import java.util.*;
import java.lang.reflect.*;
import com.eternalcraft.gripeverde.client.lore.*;
import com.eternalcraft.gripeverde.client.audio.*;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
public class DiscoveriesHarness {
    static Object field(String name) throws Exception {
        Field f = Discoveries.class.getDeclaredField(name); f.setAccessible(true); return f.get(null);
    }
    static void set(String name, Object value) throws Exception {
        Field f = Discoveries.class.getDeclaredField(name); f.setAccessible(true); f.set(null, value);
    }
    static void check(boolean value, String why) { if (!value) throw new AssertionError(why); }
    static void language(String lang) throws Exception {
        Minecraft.instance.language.selected = lang; set("cachedEnglishUntilMs", 0L);
    }
    static void toast(String name) throws Exception {
        set("toastName", name); set("toastStartMs", 0L); set("toastUntilMs", Long.MAX_VALUE);
    }
    static GuiGraphics draw(int width) {
        GuiGraphics g = new GuiGraphics(); Discoveries.drawToast(g, width); return g;
    }
    static void expect(GuiGraphics g, String header, String name, int width) {
        check(g.text.equals(List.of(header, name)), "wrong visible text: " + g.text);
        for (int i = 0; i < 2; i++) {
            int x = g.positions.get(i);
            check(x >= 0 && x + Minecraft.instance.font.width(g.text.get(i)) <= width - 8,
                  "text exceeds logical toast bounds");
        }
    }
    public static void main(String[] args) throws Exception {
        Minecraft mc = Minecraft.instance;
        switch (args[0]) {
            case "invalid_discovery" -> {
                toast("keep me"); set("toastStartMs", 123L);
                for (String id : new String[] {null, "", " \t\r\n", "\u2003"})
                    check(!Discoveries.discover(id), "invalid id accepted");
                check(ChroniclerMemory.reads == 0 && ChroniclerMemory.marks == 0, "invalid id touched memory");
                check(Minecraft.reads == 0 && UiSounds.plays == 0 && MenuMusic.ducks == 0, "invalid id touched client/audio");
                check(field("toastName").equals("keep me") && (Long)field("toastStartMs") == 123L,
                      "invalid id replaced existing toast");
            }
            case "null_lookups" -> {
                check(Discoveries.nameFor(null).equals("???"), "null name fallback");
                check(Discoveries.hintFor(null).equals("???"), "null hint fallback");
                check(Minecraft.reads == 0 && ChroniclerMemory.reads == 0, "null lookup touched client/memory");
            }
            case "known_and_duplicate" -> {
                check(Discoveries.discover(Discoveries.WATCH_KEY), "new discovery rejected");
                check(ChroniclerMemory.marks == 1 && UiSounds.plays == 2 && MenuMusic.ducks == 1, "new discovery effects");
                check(Discoveries.hasActiveToast(), "new discovery lacks toast");
                Object name = field("toastName"), until = field("toastUntilMs");
                check(!Discoveries.discover(Discoveries.WATCH_KEY), "duplicate discovery accepted");
                check(ChroniclerMemory.marks == 1 && UiSounds.plays == 2 && MenuMusic.ducks == 1, "duplicate effects repeated");
                check(field("toastName").equals(name) && field("toastUntilMs").equals(until), "duplicate reset toast");
            }
            case "unknown_supported" -> {
                check(Discoveries.discover("custom-id"), "nonblank unknown id rejected");
                check(ChroniclerMemory.ids.contains("custom-id"), "unknown id not preserved");
                check(Discoveries.nameFor("custom-id").equals("???") && Discoveries.hintFor("custom-id").equals("???"), "unknown fallback changed");
            }
            case "all_labels" -> {
                for (String lang : new String[] {"en_us", "es_uy"}) {
                    language(lang);
                    for (String id : Discoveries.ALL) {
                        check(!Discoveries.nameFor(id).equals("???"), "missing name: " + id);
                        check(!Discoveries.hintFor(id).equals("???"), "missing hint: " + id);
                    }
                }
            }
            case "count_change" -> {
                toast("Same name"); ChroniclerMemory.seen = 9; expect(draw(300), "NEW FILE  ·  9", "Same name", 300);
                ChroniclerMemory.seen = 10; expect(draw(300), "NEW FILE  ·  10", "Same name", 300);
                ChroniclerMemory.seen = 0; expect(draw(300), "NEW FILE", "Same name", 300);
            }
            case "cache_hit" -> {
                toast("Same name"); ChroniclerMemory.seen = 9; draw(300);
                int calls = mc.font.calls, fits = DiaryChrome.fits;
                for (int i = 0; i < 20; i++) draw(300);
                check(mc.font.calls == calls && DiaryChrome.fits == fits, "cache hit repeated layout work");
            }
            case "language_change" -> {
                toast("Same name"); ChroniclerMemory.seen = 4;
                expect(draw(300), "NEW FILE  ·  4", "Same name", 300);
                language("es_uy"); expect(draw(300), "FICHA NUEVA  ·  4", "Same name", 300);
            }
            case "width_change" -> {
                mc.font.scale = 3; toast("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                expect(draw(300), "NEW FILE", "ABCDEFGHIJKLMNOPQRSTUVWXYZ", 300);
                expect(draw(100), "NEW FILE", "ABCDEFGHIJKLMNOPQRS", 100);
                expect(draw(300), "NEW FILE", "ABCDEFGHIJKLMNOPQRSTUVWXYZ", 300);
            }
            case "font_change" -> {
                toast("Same name"); draw(300); Font old = mc.font; mc.font = new Font(); mc.font.scale = 12;
                expect(draw(120), "NEW", "Same", 120);
                check(mc.font.calls > 0 && field("memToastFont") != old, "new font did not invalidate cache");
            }
            case "name_change" -> {
                toast("first"); draw(300); toast("second"); expect(draw(300), "NEW FILE", "second", 300);
            }
            case "narrow_window" -> {
                toast("test"); check(draw(90).text.isEmpty(), "too narrow toast rendered");
                expect(draw(300), "NEW FILE", "test", 300);
            }
            case "in_game_queue" -> {
                mc.level = new Object(); Discoveries.discover(Discoveries.WATCH_KEY);
                check(((Deque<?>)field("gameQueue")).size() == 1, "game discovery not queued");
                GuiGraphics g = new GuiGraphics(); Discoveries.drawGameToast(g, 300, 200);
                check(g.text.size() == 2 && field("gameName").equals(Discoveries.nameFor(Discoveries.WATCH_KEY)), "queued game toast not drawn");
            }
            case "queue_bounds" -> {
                for (String value : new String[] {null, "", " \t", "\u2003"}) Discoveries.enqueueGameToast(value);
                Deque<?> queue = (Deque<?>)field("gameQueue"); check(queue.isEmpty(), "blank game toast queued");
                for (int i = 0; i < 6; i++) Discoveries.enqueueGameToast("name-" + i);
                check(new ArrayList<>(queue).equals(List.of("name-2", "name-3", "name-4", "name-5")), "queue cap/order changed");
            }
            case "game_visibility_guards" -> {
                mc.level = new Object(); Discoveries.enqueueGameToast("test");
                mc.screen = new net.minecraft.client.gui.screens.Screen();
                Discoveries.drawGameToast(new GuiGraphics(), 300, 200); mc.screen = null;
                mc.options.renderDebug = true; Discoveries.drawGameToast(new GuiGraphics(), 300, 200); mc.options.renderDebug = false;
                Discoveries.drawGameToast(new GuiGraphics(), 219, 200);
                Discoveries.drawGameToast(new GuiGraphics(), 300, 159);
                check(field("gameName") == null && ((Deque<?>)field("gameQueue")).size() == 1, "hidden layout consumed queue");
            }
            default -> throw new AssertionError(args[0]);
        }
    }
}
'''


class Wiring(unittest.TestCase):
    def test_invalid_ids_guard_precedes_memory(self):
        body = method(SOURCE, 'public static boolean discover(')
        self.assertLess(body.index('id.isBlank()'), body.index('ChroniclerMemory.isDiscovered('))

    def test_header_is_part_of_cache_key(self):
        body = method(SOURCE, 'public static void drawToast(')
        self.assertIn('!memToastHeader.equals(header)', body)
        self.assertIn('memToastHeader = header', body)

    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_discoveries.py --require-java', (ROOT / path).read_text())


class Executable(unittest.TestCase):
    def test_complete_discoveries_class(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        cases = ('invalid_discovery', 'null_lookups', 'known_and_duplicate', 'unknown_supported',
                 'all_labels', 'count_change', 'cache_hit', 'language_change', 'width_change',
                 'font_change', 'name_change', 'narrow_window', 'in_game_queue', 'queue_bounds',
                 'game_visibility_guards')
        with tempfile.TemporaryDirectory() as tmp:
            work = Path(tmp)
            sources = dict(STUBS)
            sources['com/eternalcraft/gripeverde/client/lore/Discoveries.java'] = SOURCE
            shorten = method(CHROME, 'public static String shorten(')
            sources['com/eternalcraft/gripeverde/client/render/DiaryChrome.java'] = (
                'package com.eternalcraft.gripeverde.client.render; import net.minecraft.client.gui.Font; '
                'public class DiaryChrome { public static int fits; '
                'public static String fitText(Font font, String text, int width) { fits++; return shorten(font,text,width); }\n'
                + shorten + '\n}')
            sources['DiscoveriesHarness.java'] = HARNESS
            paths = []
            for name, text in sources.items():
                path = work / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            for case in cases:
                with self.subTest(case=case):
                    subprocess.run(['java', '-cp', tmp, 'DiscoveriesHarness', case], check=True, timeout=15)
            print(f'Discoveries: {len(cases)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
