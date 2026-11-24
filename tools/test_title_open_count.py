#!/usr/bin/env python3
"""Test actual title opening method with range-enforcing config/reward doubles.

The screen, Forge config disk path, rendering and native input are not run.
CI requires Java 17; structural wiring checks also run without Java.
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
SOURCE = (ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/screens/DiaryTitleScreen.java').read_text()
CONFIG = (ROOT / 'src/main/java/com/eternalcraft/gripeverde/config/ClientConfig.java').read_text()
LIMIT = re.search(r'public static final int MAX_OPEN_COUNT = (\d+);', CONFIG).group(1)

CONFIG_STUB = '''package com.eternalcraft.gripeverde.config;
public class ClientConfig {
    public static final int MAX_OPEN_COUNT = __LIMIT__;
    public static Value OPEN_COUNT = new Value();
    public static class Value {
        public int value, sets, saves, failReads;
        public int get() {
            if (failReads-- > 0) throw new IllegalStateException("config not loaded");
            return value;
        }
        public void set(int value) {
            if (value < 0 || value > MAX_OPEN_COUNT) throw new IllegalArgumentException("outside Forge range");
            this.value = value; sets++;
        }
        public void save() { saves++; }
    }
}
'''
DISCOVERIES = '''package com.eternalcraft.gripeverde.client.lore;
public class Discoveries {
    public static final String ARCHIVE_THINNING = "archive_thinning";
    public static int calls;
    public static boolean discover(String id) {
        if (!ARCHIVE_THINNING.equals(id)) throw new AssertionError("wrong reward");
        calls++; return true;
    }
}
'''
MARGINALIA = '''package com.eternalcraft.gripeverde.client.lore;
public class Marginalia {
    public static final String N_OPEN_COUNT = "open_count";
    public static int calls;
    public static void queueOnce(String id) {
        if (!N_OPEN_COUNT.equals(id)) throw new AssertionError("wrong note");
        calls++;
    }
}
'''
HARNESS = '''import com.eternalcraft.gripeverde.config.ClientConfig;
import com.eternalcraft.gripeverde.client.lore.*;
public class TitleOpenCountHarness {
    private static boolean openCountIncremented;
    __METHOD__
    static void check(boolean value, String reason) { if (!value) throw new AssertionError(reason); }
    public static void main(String[] args) {
        var config = ClientConfig.OPEN_COUNT;
        if (args[0].equals("retry_config")) {
            config.failReads = 1;
            try { recordOpening(); throw new AssertionError("expected unavailable config"); }
            catch (IllegalStateException expected) {}
            check(!openCountIncremented && config.sets == 0 && config.saves == 0, "failed read consumed opening");
            recordOpening();
            check(openCountIncremented && config.value == 1 && config.saves == 1, "opening did not retry after config ready");
            return;
        }
        int initial = Integer.parseInt(args[0]);
        config.value = initial;
        int expected = initial == ClientConfig.MAX_OPEN_COUNT ? initial : initial + 1;
        recordOpening();
        check(openCountIncremented, "opening guard not committed");
        check(config.value == expected, "count must increment/saturate at declared Forge limit");
        int writes = initial == expected ? 0 : 1;
        check(config.sets == writes && config.saves == writes, "incorrect config write count");
        check(Discoveries.calls == (expected >= 50 ? 1 : 0), "50-opening reward threshold changed");
        check(Marginalia.calls == (expected >= 100 ? 1 : 0), "100-opening note threshold changed");
        for (int i = 0; i < 5; i++) recordOpening();
        check(config.value == expected && config.sets == writes && config.saves == writes, "resize/reinit counted again");
        check(Discoveries.calls == (expected >= 50 ? 1 : 0) && Marginalia.calls == (expected >= 100 ? 1 : 0), "reinit repeated reward checks");
    }
}
'''


class Wiring(unittest.TestCase):
    def test_uses_declared_config_limit(self):
        self.assertIn('.defineInRange("openCount", 0, 0, MAX_OPEN_COUNT)', CONFIG)
        body = method(SOURCE, 'private static void recordOpening(')
        self.assertIn('ClientConfig.MAX_OPEN_COUNT', body)
        self.assertIn('if (next != cur)', body)

    def test_init_uses_guarded_helper(self):
        body = method(SOURCE, 'protected void init(')
        self.assertRegex(body, r'try \{\s*recordOpening\(\);\s*\} catch \(Throwable ignored\)')
        self.assertNotIn('OPEN_COUNT.set(', body)
        self.assertIn('private static boolean openCountIncremented', SOURCE)

    def test_ci_requires_execution(self):
        for name in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_title_open_count.py --require-java', (ROOT / name).read_text())


class Executable(unittest.TestCase):
    def test_opening_method(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        cases = ['0', '48', '49', '50', '98', '99', '100', str(int(LIMIT) - 1), LIMIT, 'retry_config']
        with tempfile.TemporaryDirectory() as tmp:
            work = Path(tmp)
            sources = {
                'com/eternalcraft/gripeverde/config/ClientConfig.java': CONFIG_STUB.replace('__LIMIT__', LIMIT),
                'com/eternalcraft/gripeverde/client/lore/Discoveries.java': DISCOVERIES,
                'com/eternalcraft/gripeverde/client/lore/Marginalia.java': MARGINALIA,
                'TitleOpenCountHarness.java': HARNESS.replace('__METHOD__', method(SOURCE, 'private static void recordOpening(')),
            }
            paths = []
            for name, text in sources.items():
                path = work / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            for case in cases:
                with self.subTest(case=case):
                    subprocess.run(['java', '-cp', tmp, 'TitleOpenCountHarness', case], check=True, timeout=15)
            print(f'Title opening count: {len(cases)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
