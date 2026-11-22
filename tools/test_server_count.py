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
    Font font = new Font(1), countFont;
    static class Font {
        int scale, calls;
        Font(int scale) { this.scale = scale; }
        int width(String text) { calls++; return scale * text.length(); }
    }
    __METHOD__
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
        cases = ('known_changes', 'cache_hit', 'count_changes', 'language_changes', 'width_changes',
                 'font_changes', 'empty_and_single', 'negative_space', 'hidden_known_updates', 'exact_fit')
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / 'ServerCountHarness.java'
            path.write_text(HARNESS.replace('__METHOD__', actual))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, str(path)], check=True, timeout=60)
            for case in cases:
                with self.subTest(case=case):
                    subprocess.run(['java', '-cp', tmp, 'ServerCountHarness', case], check=True, timeout=15)
            print(f'Server count cache: {len(cases)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
