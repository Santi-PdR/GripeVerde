#!/usr/bin/env python3
"""Run real Marginalia layout and DiaryChrome shortening methods with a fake font.

No Minecraft client/rendering/native input is started. This checks text, cache
keys and logical width bounds, not actual glyph rendering or note timing.
CI requires JDK 17; structural checks also run without Java.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method

ROOT = Path(__file__).resolve().parents[1]
SOURCE = (ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/lore/Marginalia.java').read_text()
CHROME = (ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/render/DiaryChrome.java').read_text()

FONT = '''package net.minecraft.client.gui;
public class Font {
    public int scale, calls;
    public Font(int scale) { this.scale = scale; }
    public int width(String text) { calls++; return text.length() * scale; }
}
'''

HARNESS = r'''import java.util.*;
import net.minecraft.client.gui.Font;
public class MarginaliaLayoutHarness {
    static String cachedLayoutText;
    static Font cachedLayoutFont;
    static int cachedLayoutWidth = -1, cachedLayoutLimit = -1, cachedLayoutLinesWidth;
    static List<String> cachedLayoutLines = Collections.emptyList();
    __LAYOUT__
    static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    static void expect(List<String> lines, Font font, int width, int maxLines, String... expected) {
        if (!lines.equals(List.of(expected))) throw new AssertionError(lines + " != " + List.of(expected));
        check(lines.size() <= Math.max(0, maxLines));
        int widest = 0;
        for (String line : lines) {
            check(!line.isEmpty());
            int measured = font.width(line);
            check(measured <= width); widest = Math.max(widest, measured);
        }
        check(widest == cachedLayoutLinesWidth);
    }
    public static void main(String[] args) {
        Font font = new Font(1);
        switch (args[0]) {
            case "basic_wrap" -> {
                expect(layoutLines("one two three", font, 7, 4), font, 7, 4, "one two", "three");
            }
            case "line_limit" -> {
                expect(layoutLines("one two three four", font, 3, 2), font, 3, 2, "one", "two");
            }
            case "long_word" -> {
                expect(layoutLines("abcdefghij", font, 4, 4), font, 4, 4, "abcd");
            }
            case "every_line_fits" -> {
                expect(layoutLines("abcdef ghi jklmnop", font, 3, 4), font, 3, 4, "abc", "ghi", "jkl");
            }
            case "old_note_not_reused" -> {
                Font wide = new Font(2);
                expect(layoutLines("OLD", wide, 10, 1), wide, 10, 1, "OLD");
                expect(layoutLines("ALPHA BETA", wide, 1, 1), wide, 1, 1);
            }
            case "font_change" -> {
                expect(layoutLines("ab cd", font, 5, 4), font, 5, 4, "ab cd");
                Font wide = new Font(2);
                expect(layoutLines("ab cd", wide, 5, 4), wide, 5, 4, "ab", "cd");
            }
            case "cache_hit" -> {
                List<String> first = layoutLines("one two", font, 7, 2);
                int calls = font.calls;
                check(first == layoutLines("one two", font, 7, 2));
                check(font.calls == calls);
            }
            case "immutable_snapshot" -> {
                List<String> first = layoutLines("one two", font, 7, 2);
                try { first.add("injected"); throw new AssertionError(); }
                catch (UnsupportedOperationException expected) {}
                layoutLines("other", font, 7, 2);
                check(first.equals(List.of("one two")));
            }
            case "empty_text" -> {
                for (String text : new String[] {null, "", "   ", "\t\n"}) {
                    expect(layoutLines(text, font, 10, 2), font, 10, 2);
                }
            }
            case "invalid_bounds" -> {
                expect(layoutLines("abc", font, 0, 2), font, 0, 2);
                expect(layoutLines("abc", font, -1, 2), font, -1, 2);
                expect(layoutLines("abc", font, 10, 0), font, 10, 0);
                expect(layoutLines("abc", font, 10, -1), font, 10, -1);
            }
            case "null_font" -> {
                expect(layoutLines("abc", font, 10, 2), font, 10, 2, "abc");
                check(layoutLines("abc", null, 10, 2).isEmpty() && cachedLayoutLinesWidth == 0);
                expect(layoutLines("abc", font, 10, 2), font, 10, 2, "abc");
            }
            case "width_change" -> {
                expect(layoutLines("alpha beta", font, 2, 2), font, 2, 2, "al", "be");
                expect(layoutLines("alpha beta", font, 10, 2), font, 10, 2, "alpha beta");
            }
            case "height_change" -> {
                expect(layoutLines("one two three", font, 3, 2), font, 3, 2, "one", "two");
                expect(layoutLines("one two three", font, 3, 1), font, 3, 1, "one");
                expect(layoutLines("one two three", font, 3, 3), font, 3, 3, "one", "two", "thr");
            }
            case "empty_after_previous" -> {
                layoutLines("previous", font, 20, 4);
                expect(layoutLines("", font, 20, 4), font, 20, 4);
            }
            case "whitespace" -> {
                expect(layoutLines("  one   two  ", font, 7, 2), font, 7, 2, "one two");
            }
            case "exact_fit" -> {
                expect(layoutLines("exact", font, 5, 1), font, 5, 1, "exact");
            }
            default -> throw new AssertionError(args[0]);
        }
    }
}
'''


class Wiring(unittest.TestCase):
    def test_font_is_cache_key(self):
        actual = method(SOURCE, 'private static java.util.List<String> layoutLines(')
        self.assertIn('font == cachedLayoutFont', actual)
        self.assertIn('cachedLayoutFont = font', actual)

    def test_empty_layout_defers_note(self):
        draw = method(SOURCE, 'public static void drawHorizontal(')
        self.assertRegex(draw, r'if \(lines\.isEmpty\(\)\) \{\s*deferActive\(\);')

    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_marginalia_layout.py --require-java', (ROOT / path).read_text())


class Executable(unittest.TestCase):
    def test_layout_methods(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        layout = method(SOURCE, 'private static java.util.List<String> layoutLines(')
        shorten = method(CHROME, 'public static String shorten(')
        cases = ('basic_wrap', 'line_limit', 'long_word', 'every_line_fits', 'old_note_not_reused',
                 'font_change', 'cache_hit', 'immutable_snapshot', 'empty_text', 'invalid_bounds',
                 'null_font', 'width_change', 'height_change', 'empty_after_previous', 'whitespace', 'exact_fit')
        with tempfile.TemporaryDirectory() as tmp:
            work = Path(tmp)
            font_path = work / 'net/minecraft/client/gui/Font.java'
            font_path.parent.mkdir(parents=True)
            font_path.write_text(FONT)
            chrome_path = work / 'com/eternalcraft/gripeverde/client/render/DiaryChrome.java'
            chrome_path.parent.mkdir(parents=True)
            chrome_path.write_text('package com.eternalcraft.gripeverde.client.render; '
                                   'import net.minecraft.client.gui.Font; public class DiaryChrome {\n' + shorten + '\n}')
            harness = work / 'MarginaliaLayoutHarness.java'
            harness.write_text(HARNESS.replace('__LAYOUT__', layout))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp,
                            str(font_path), str(chrome_path), str(harness)], check=True, timeout=60)
            for case in cases:
                with self.subTest(case=case):
                    subprocess.run(['java', '-cp', tmp, 'MarginaliaLayoutHarness', case], check=True, timeout=15)
            print(f'Marginalia layout: {len(cases)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
