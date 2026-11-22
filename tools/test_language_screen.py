#!/usr/bin/env python3
"""Test language-screen rebuild/selection logic without launching Minecraft.

Wiring checks run without Java. With JDK 17, compile the actual Java methods
extracted from DiaryLanguageScreen, with small substitutes for GUI/services.
This is not a test of the entire screen, real focus dispatch or rendering.
CI uses --require-java so the executable tests cannot be silently skipped.
"""
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
PATH = ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/screens/DiaryLanguageScreen.java'
NOISE = re.compile(r'//[^\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'')


def method(source, signature):
    """Slice an actual method, counting braces outside comments/literals."""
    clean = NOISE.sub(lambda m: ''.join('\n' if c == '\n' else ' ' for c in m[0]), source)
    start = clean.index(signature)
    opening = clean.index('{', start + len(signature))
    depth = 0
    for end in range(opening, len(clean)):
        if clean[end] == '{':
            depth += 1
        elif clean[end] == '}':
            depth -= 1
            if depth == 0:
                return source[start:end + 1]
    raise AssertionError('Unclosed method: ' + signature)


HARNESS = '''import java.util.*;
public class DiaryLanguageScreen {
    static double lastScrollAmount = -1.0;
    String appliedLanguageCode, pendingLanguageCode;
    LanguageManager languageManager = new LanguageManager();
    LanguageList languageList;
    boolean applyingLanguage;
    Font font = new Font(), headerFont;
    int width = 800, panelW = 420, headerPanelW = -1;
    Component title = new Component("Languages");
    String headerTitleSrc = "", headerTitle = "", headerSubtitle = "", headerFlavor = "";
    int headerTitleX, headerSubX, headerFlavorX;
    static class Font { int width(String s) { return s.length(); } }
    record Component(String value) { String getString() { return value; } }
    static class DiaryChrome {
        static String fitText(Font font, String text, int width) { return text; }
        static String tr(String key) { return key; }
    }
    static class LanguageManager {
        String selected = "en_us";
        Map<String, Object> languages = new HashMap<>(Map.of("en_us", 1, "es_es", 2, "fr_fr", 3));
        String getSelected() { return selected; }
        Map<String, Object> getLanguages() { return languages; }
    }
    static class LanguageEntry {
        String code; boolean fogTongue;
        LanguageEntry(String code, boolean fog) { this.code = code; this.fogTongue = fog; }
    }
    static class BaseList {
        double scroll;
        LanguageEntry selected;
        double getScrollAmount() { return scroll; }
        public void setSelected(LanguageEntry entry) { selected = entry; }
    }
    private final class LanguageList extends BaseList {
        __SELECTION__
    }
    __REBUILD__
    __HEADER__
    static void eq(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) throw new AssertionError(expected + " != " + actual);
    }
    public static void main(String[] args) {
        DiaryLanguageScreen screen = new DiaryLanguageScreen();
        switch (args[0]) {
            case "first_open":
                eq(-1.0, screen.prepareLanguageRebuild());
                eq("en_us", screen.appliedLanguageCode); eq("en_us", screen.pendingLanguageCode);
                break;
            case "pending_resize":
                lastScrollAmount = 64;
                screen.languageList = screen.new LanguageList(); screen.languageList.scroll = 128.5;
                screen.pendingLanguageCode = "es_es";
                eq(128.5, screen.prepareLanguageRebuild());
                eq("es_es", screen.pendingLanguageCode); eq("en_us", screen.languageManager.getSelected());
                break;
            case "zero_scroll_resize":
                lastScrollAmount = 80;
                screen.languageList = screen.new LanguageList();
                eq(0.0, screen.prepareLanguageRebuild());
                break;
            case "zero_scroll_reopen":
                lastScrollAmount = 0;
                eq(0.0, screen.prepareLanguageRebuild());
                break;
            case "removed_pending_language":
                screen.pendingLanguageCode = "missing";
                screen.prepareLanguageRebuild(); eq("en_us", screen.pendingLanguageCode);
                break;
            case "applied_refresh":
                screen.pendingLanguageCode = "fr_fr"; screen.languageManager.selected = "es_es";
                screen.prepareLanguageRebuild();
                eq("es_es", screen.appliedLanguageCode); eq("fr_fr", screen.pendingLanguageCode);
                break;
            case "keyboard_selection":
                screen.pendingLanguageCode = "en_us";
                screen.languageList = screen.new LanguageList();
                LanguageEntry french = new LanguageEntry("fr_fr", false);
                screen.languageList.setSelected(french);
                eq(french, screen.languageList.selected); eq("fr_fr", screen.pendingLanguageCode);
                eq("en_us", screen.languageManager.getSelected());
                break;
            case "null_selection":
                screen.pendingLanguageCode = "es_es";
                screen.languageList = screen.new LanguageList(); screen.languageList.setSelected(null);
                eq("es_es", screen.pendingLanguageCode); eq(null, screen.languageList.selected);
                break;
            case "fog_selection":
                screen.pendingLanguageCode = "es_es";
                screen.languageList = screen.new LanguageList();
                screen.languageList.setSelected(new LanguageEntry("fog", true));
                eq("es_es", screen.pendingLanguageCode);
                break;
            case "applying_selection":
                screen.pendingLanguageCode = "es_es"; screen.applyingLanguage = true;
                screen.languageList = screen.new LanguageList();
                screen.languageList.setSelected(new LanguageEntry("fr_fr", false));
                eq("es_es", screen.pendingLanguageCode);
                break;
            case "header_resize_capped_panel":
                screen.refreshHeaderCache();
                eq((800 - screen.font.width(screen.headerTitle)) / 2, screen.headerTitleX);
                screen.width = 1000; screen.prepareLanguageRebuild(); screen.refreshHeaderCache();
                eq(420, screen.panelW);
                eq((1000 - screen.font.width(screen.headerTitle)) / 2, screen.headerTitleX);
                eq(500 - screen.font.width(screen.headerSubtitle) / 2, screen.headerSubX);
                eq(500 - screen.font.width(screen.headerFlavor) / 2, screen.headerFlavorX);
                break;
            default: throw new AssertionError(args[0]);
        }
    }
}
'''


class LanguageWiringTests(unittest.TestCase):
    def test_rebuild_precedes_list_replacement_and_keeps_zero_scroll(self):
        source = PATH.read_text(encoding='utf-8')
        init = method(source, 'protected void init()')
        self.assertLess(init.index('double restoredScroll = prepareLanguageRebuild();'),
                        init.index('this.languageList = new LanguageList('))
        self.assertIn('if (restoredScroll >= 0.0)', init)
        self.assertIn('this.languageList.setScrollAmount(restoredScroll);', init)
        self.assertLess(init.index('this.languageList.setScrollAmount(restoredScroll);'),
                        init.index('this.smoothScroll.snapToCurrent(this.languageList);'))
        self.assertIn('lastScrollAmount = -1.0;', source)
        self.assertNotIn('this.pendingLanguageCode = this.appliedLanguageCode;', init)

    def test_escape_cancels_and_done_applies(self):
        source = PATH.read_text(encoding='utf-8')
        close = method(source, 'public void onClose()')
        self.assertNotIn('applyAndClose()', close)
        self.assertNotIn('this.languageManager.setSelected(', close)
        self.assertIn('this.minecraft.setScreen(this.parent);', close)
        apply = method(source, 'private void applyAndClose()')
        self.assertIn('this.languageManager.setSelected(this.pendingLanguageCode);', apply)
        self.assertIn('this.options.languageCode = this.pendingLanguageCode;', apply)

    def test_ci_requires_executable_tests(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_language_screen.py --require-java',
                          (ROOT / path).read_text(encoding='utf-8'))


class LanguageJavaTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        if not shutil.which('javac') or not shutil.which('java'):
            raise unittest.SkipTest('JDK not installed; language Java methods not executed')
        source = PATH.read_text(encoding='utf-8')
        harness = HARNESS.replace('__SELECTION__', method(source, 'public void setSelected(LanguageEntry entry)'))
        harness = harness.replace('__REBUILD__', method(source, 'private double prepareLanguageRebuild()'))
        harness = harness.replace('__HEADER__', method(source, 'private void refreshHeaderCache()'))
        cls.temp = tempfile.TemporaryDirectory(prefix='gripeverde-language-')
        cls.addClassCleanup(cls.temp.cleanup)
        cls.work = Path(cls.temp.name)
        path = cls.work / 'DiaryLanguageScreen.java'
        path.write_text(harness, encoding='utf-8')
        subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8',
                        '-d', str(cls.work), str(path)], check=True, timeout=60)

    def run_case(self, name):
        subprocess.run(['java', '-cp', str(self.work), 'DiaryLanguageScreen', name],
                       check=True, timeout=15)


for _case in (
    'first_open', 'pending_resize', 'zero_scroll_resize', 'zero_scroll_reopen',
    'removed_pending_language', 'applied_refresh', 'keyboard_selection',
    'null_selection', 'fog_selection', 'applying_selection', 'header_resize_capped_panel',
):
    def _test(self, name=_case):
        self.run_case(name)
    setattr(LanguageJavaTests, 'test_' + _case, _test)

if __name__ == '__main__':
    if '--require-java' in sys.argv:
        sys.argv.remove('--require-java')
        if not shutil.which('javac') or not shutil.which('java'):
            sys.exit('JDK required: install Java 17 to run the language method tests')
    unittest.main(verbosity=2)
