#!/usr/bin/env python3
"""Exercise the real multiplayer count-cache method with a fake font.

No Minecraft client, rendering, server list, DNS or connection is started.
CI requires JDK 17; local wiring checks also run without Java.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method

ROOT = Path(__file__).resolve().parents[1]
SOURCE = (ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/screens/DiaryMultiplayerScreen.java').read_text()

HARNESS = '''
public class ServerCountHarness {
    String countText = "";
    int countWidth, countSrc = -1, countKnownSrc = -1, countLimit = Integer.MIN_VALUE;
    boolean countEnglish;
    Font font = new Font(1), countFont, headerFont;
    int width = 260, headerMaxWidth = -1, headerCenterX = Integer.MIN_VALUE;
    int headerTitleX, headerSubX;
    String headerTitleSrc = "", headerSubSrc = "", headerTitle = "", headerSubtitle = "";
    record Component(String text) { String getString() { return text; } }
    Component title = new Component("Servers");
    static class DiaryChrome {
        static String subtitle = "Saved servers";
        static String tr(String key) { return subtitle; }
    }
    String ellipsizeTo(String text, int maxWidth) {
        return text.substring(0, Math.min(text.length(), Math.max(0, maxWidth / font.scale)));
    }
    static class Font {
        int scale, calls;
        Font(int scale) { this.scale = scale; }
        int width(String text) { calls++; return scale * text.length(); }
    }
    __METHOD__
    __HEADER__
    static void check(boolean condition) { if (!condition) throw new AssertionError(); }
    void expect(String text) {
        if (!countText.equals(text)) throw new AssertionError(countText + " != " + text);
        check(countWidth == text.length() * font.scale);
    }
    public static void main(String[] args) {
        ServerCountHarness s = new ServerCountHarness();
        switch (args[0]) {
            case "known_changes":
                s.refreshServerCountCache(4, 0, 100, true); s.expect("4 entries");
                s.refreshServerCountCache(4, 1, 100, true); s.expect("4 entries  ·  1 known");
                s.refreshServerCountCache(4, 2, 100, true); s.expect("4 entries  ·  2 known");
                s.refreshServerCountCache(4, 0, 100, true); s.expect("4 entries"); break;
            case "cache_hit":
                s.refreshServerCountCache(4, 2, 100, true);
                int calls = s.font.calls;
                for (int i = 0; i < 50; i++) s.refreshServerCountCache(4, 2, 100, true);
                check(s.font.calls == calls); s.expect("4 entries  ·  2 known"); break;
            case "count_changes":
                s.refreshServerCountCache(1, 1, 100, true); s.expect("1 entry  ·  1 known");
                s.refreshServerCountCache(3, 1, 100, true); s.expect("3 entries  ·  1 known"); break;
            case "language_changes":
                s.refreshServerCountCache(2, 1, 100, true); s.expect("2 entries  ·  1 known");
                s.refreshServerCountCache(2, 1, 100, false); s.expect("2 anotados  ·  1 conocidas");
                s.refreshServerCountCache(2, 1, 100, true); s.expect("2 entries  ·  1 known"); break;
            case "width_changes":
                s.refreshServerCountCache(2, 1, 100, true); s.expect("2 entries  ·  1 known");
                s.refreshServerCountCache(2, 1, 9, true); s.expect("2 entries");
                s.refreshServerCountCache(2, 1, 8, true); s.expect("");
                s.refreshServerCountCache(2, 1, 100, true); s.expect("2 entries  ·  1 known"); break;
            case "font_changes":
                s.refreshServerCountCache(2, 0, 15, true); s.expect("2 entries");
                s.font = new Font(2);
                s.refreshServerCountCache(2, 0, 15, true); s.expect("");
                s.refreshServerCountCache(2, 0, 100, true); s.expect("2 entries"); break;
            case "empty_and_single":
                s.refreshServerCountCache(0, 0, 100, true); s.expect("none yet");
                s.refreshServerCountCache(0, 0, 100, false); s.expect("ninguno anotado");
                s.refreshServerCountCache(1, 0, 100, false); s.expect("1 anotado"); break;
            case "negative_space":
                s.refreshServerCountCache(3, 2, -10, false); s.expect("");
                check(s.countKnownSrc == 2 && s.countLimit == -10); break;
            case "hidden_known_updates":
                s.refreshServerCountCache(2, 1, 9, true); s.expect("2 entries");
                s.refreshServerCountCache(2, 3, 9, true); s.expect("2 entries");
                check(s.countKnownSrc == 3);
                s.refreshServerCountCache(2, 3, 100, true); s.expect("2 entries  ·  3 known"); break;
            case "exact_fit":
                s.refreshServerCountCache(2, 1, "2 entries  ·  1 known".length(), true);
                s.expect("2 entries  ·  1 known"); break;
            case "header_center_changes":
                s.refreshHeaderCache(64);
                int oldTitleX = s.headerTitleX, oldSubX = s.headerSubX;
                s.width = 280; s.refreshHeaderCache(64);
                check(s.headerTitleX == oldTitleX + 10 && s.headerSubX == oldSubX + 10); break;
            case "header_cache_hit":
                s.refreshHeaderCache(64); int headerCalls = s.font.calls;
                s.refreshHeaderCache(64); check(s.font.calls == headerCalls); break;
            case "header_font_changes":
                s.refreshHeaderCache(64); s.font = new Font(2); s.refreshHeaderCache(64);
                check(s.headerTitleX == s.width / 2 - "Servers".length()); break;
            case "header_title_changes":
                s.refreshHeaderCache(64); s.title = new Component("Other"); s.refreshHeaderCache(64);
                check(s.headerTitle.equals("Other") && s.headerTitleX == s.width / 2 - 2); break;
            case "header_subtitle_changes":
                s.refreshHeaderCache(64); DiaryChrome.subtitle = "Other"; s.refreshHeaderCache(64);
                check(s.headerSubtitle.equals("- Other -") && s.headerSubX == s.width / 2 - 4); break;
            case "header_width_changes":
                s.refreshHeaderCache(64); s.refreshHeaderCache(4);
                check(s.headerTitle.equals("Serv") && s.headerTitleX == s.width / 2 - 2); break;
            default: throw new AssertionError(args[0]);
        }
    }
}
'''


class Wiring(unittest.TestCase):
    def test_recents_read_before_refresh(self):
        body = method(SOURCE, 'private void drawServerCount(')
        self.assertIn('RecentAddresses.list().size()', body)
        self.assertLess(body.index('RecentAddresses.list().size()'), body.index('this.refreshServerCountCache('))
        self.assertIn('this.refreshServerCountCache(count, knownCount, hueco, english)', body)

    def test_known_count_is_a_cache_key(self):
        body = method(SOURCE, 'private void refreshServerCountCache(')
        self.assertIn('this.countKnownSrc == known', body)
        self.assertIn('this.countKnownSrc = known', body)

    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_server_count.py --require-java', (ROOT / path).read_text())


class Executable(unittest.TestCase):
    def test_cache_method(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        actual = method(SOURCE, 'private void refreshServerCountCache(')
        header = method(SOURCE, 'private void refreshHeaderCache(')
        cases = ('known_changes', 'cache_hit', 'count_changes', 'language_changes', 'width_changes',
                 'font_changes', 'empty_and_single', 'negative_space', 'hidden_known_updates', 'exact_fit',
                 'header_center_changes', 'header_cache_hit', 'header_font_changes', 'header_title_changes',
                 'header_subtitle_changes', 'header_width_changes')
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / 'ServerCountHarness.java'
            path.write_text(HARNESS.replace('__METHOD__', actual).replace('__HEADER__', header))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, str(path)], check=True, timeout=60)
            for case in cases:
                with self.subTest(case=case):
                    subprocess.run(['java', '-cp', tmp, 'ServerCountHarness', case], check=True, timeout=15)
            print(f'Multiplayer header/count cache: {len(cases)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
