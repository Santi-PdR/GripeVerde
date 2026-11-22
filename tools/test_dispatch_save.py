#!/usr/bin/env python3
"""Exercise actual dispatch-screen save methods with a fake clock/store.

No rendering or Minecraft event dispatch. Full JSON/filesystem coverage is in
DispatchPersistenceRegression (Gradle check). CI requires JDK 17 for this suite.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method

ROOT = Path(__file__).resolve().parents[1]
SOURCE = (ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/screens/DiaryDispatchScreen.java').read_text()

HARNESS = '''
class BaseScreen { public void removed() {} }
public class DispatchSaveHarness extends BaseScreen {
    static final long AUTOSAVE_MS = 700L, SAVE_RETRY_MS = 3000L, STATUS_MS = 1500L;
    long lastEditMs, nextSaveAttemptMs, statusUntil;
    String statusKey = "";
    static class Util {
        static long now = 10000;
        static long getMillis() { return now; }
    }
    static class DispatchLibrary {
        static boolean pending, fail;
        static int saves;
        static String message = "";
        static boolean updateMessage(int slot, String text) {
            if (message.equals(text)) return false;
            message = text; pending = true; return true;
        }
        static boolean hasPendingSave() { return pending; }
        static boolean save() { saves++; if (fail) return false; pending = false; return true; }
    }
    __METHODS__
    static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    public static void main(String[] args) {
        DispatchSaveHarness s = new DispatchSaveHarness();
        switch (args[0]) {
            case "clean":
                s.saveIfNeeded(true); check(DispatchLibrary.saves == 0); break;
            case "typing":
                s.onTextChanged(0, "a"); Util.now += 600; s.saveIfNeeded(false);
                check(DispatchLibrary.saves == 0);
                s.onTextChanged(0, "ab"); Util.now += 699; s.saveIfNeeded(false);
                check(DispatchLibrary.saves == 0);
                Util.now++; s.saveIfNeeded(false);
                check(DispatchLibrary.saves == 1 && !DispatchLibrary.pending);
                check(s.statusKey.equals("gripeverde.dispatch.saved")); break;
            case "no_op":
                s.onTextChanged(0, "a"); Util.now += 500; s.onTextChanged(0, "a");
                check(s.lastEditMs == 10000); Util.now += 200; s.saveIfNeeded(false);
                check(DispatchLibrary.saves == 1); break;
            case "force_close":
                s.onTextChanged(0, "a"); s.saveIfNeeded(true);
                check(DispatchLibrary.saves == 1 && !DispatchLibrary.pending); break;
            case "failure":
                s.onTextChanged(0, "a"); DispatchLibrary.fail = true; Util.now += 700;
                s.saveIfNeeded(false); check(DispatchLibrary.pending);
                check(s.statusKey.equals("gripeverde.dispatch.save_failed"));
                for (int i = 0; i < 59; i++) { Util.now += 50; s.saveIfNeeded(false); }
                check(DispatchLibrary.saves == 1);
                Util.now += 50; s.saveIfNeeded(false); check(DispatchLibrary.saves == 2); break;
            case "retry_success":
                s.onTextChanged(0, "a"); DispatchLibrary.fail = true; s.saveIfNeeded(true);
                DispatchLibrary.fail = false; Util.now += 3000; s.saveIfNeeded(false);
                check(!DispatchLibrary.pending && DispatchLibrary.saves == 2);
                check(s.statusKey.equals("gripeverde.dispatch.saved")); break;
            case "force_retry":
                s.onTextChanged(0, "a"); DispatchLibrary.fail = true; s.saveIfNeeded(true);
                DispatchLibrary.fail = false; s.saveIfNeeded(true);
                check(!DispatchLibrary.pending && DispatchLibrary.saves == 2); break;
            case "reopen":
                s.onTextChanged(0, "a"); DispatchLibrary.fail = true; s.saveIfNeeded(true);
                DispatchLibrary.fail = false; s = new DispatchSaveHarness(); s.saveIfNeeded(false);
                check(!DispatchLibrary.pending && DispatchLibrary.saves == 2); break;
            case "binding_failure":
                DispatchLibrary.pending = true; DispatchLibrary.fail = true;
                s.setSaveStatus("gripeverde.dispatch.slot.bound");
                check(s.statusKey.equals("gripeverde.dispatch.save_failed"));
                s.saveIfNeeded(false); check(DispatchLibrary.saves == 0);
                Util.now += 3000; s.saveIfNeeded(false); check(DispatchLibrary.saves == 1); break;
            case "binding_success":
                s.setSaveStatus("gripeverde.dispatch.slot.bound");
                check(s.statusKey.equals("gripeverde.dispatch.slot.bound"));
                check(DispatchLibrary.saves == 0); break;
            case "edit_during_backoff":
                s.onTextChanged(0, "a"); DispatchLibrary.fail = true; s.saveIfNeeded(true);
                Util.now += 1000; s.onTextChanged(0, "ab"); Util.now += 700; s.saveIfNeeded(false);
                check(DispatchLibrary.saves == 1); Util.now += 1300;
                DispatchLibrary.fail = false; s.saveIfNeeded(false);
                check(DispatchLibrary.saves == 2 && DispatchLibrary.message.equals("ab")); break;
            case "external_remove":
                s.onTextChanged(0, "a"); s.removed();
                check(DispatchLibrary.saves == 1 && !DispatchLibrary.pending); break;
            case "remove_after_failed_close":
                s.onTextChanged(0, "a"); DispatchLibrary.fail = true;
                s.saveIfNeeded(true); s.removed();
                check(DispatchLibrary.saves == 1 && DispatchLibrary.pending); break;
            case "remove_clean":
                s.removed(); check(DispatchLibrary.saves == 0); break;
            default: throw new AssertionError(args[0]);
        }
    }
}
'''


class Wiring(unittest.TestCase):
    def test_close_flushes(self):
        self.assertIn('this.saveIfNeeded(true)', method(SOURCE, 'public void onClose()'))

    def test_tick_retries(self):
        self.assertIn('this.saveIfNeeded(false)', method(SOURCE, 'public void tick()'))

    def test_actions_do_not_claim_saved_unconditionally(self):
        for sig in ('public boolean mouseClicked(', 'public boolean keyPressed('):
            body = method(SOURCE, sig)
            self.assertNotIn('this.setStatus("gripeverde.dispatch.slot.bound")', body)
            self.assertNotIn('this.setStatus("gripeverde.dispatch.slot.cleared")', body)
        self.assertNotIn('DispatchLibrary.setMessage(', SOURCE)


class Executable(unittest.TestCase):
    def test_screen_save_methods(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        names = ('private void onTextChanged(', 'private void saveIfNeeded(',
                 'private void setSaveStatus(', 'private void setStatus(', 'public void removed()')
        methods = '\n'.join(method(SOURCE, name) for name in names)
        with tempfile.TemporaryDirectory() as tmp:
            source = Path(tmp) / 'DispatchSaveHarness.java'
            source.write_text(HARNESS.replace('__METHODS__', methods))
            # Lore is unrelated to saving; keep its exact call compilable.
            lore = Path(tmp) / 'com/eternalcraft/gripeverde/client/lore/Discoveries.java'
            lore.parent.mkdir(parents=True)
            lore.write_text('package com.eternalcraft.gripeverde.client.lore; public class Discoveries { '
                            'public static final String LETTERS = "letters"; public static void discover(String s) {} }')
            subprocess.run(['javac', '--release', '17', '-d', tmp, str(source), str(lore)], check=True)
            cases = ('clean', 'typing', 'no_op', 'force_close', 'failure', 'retry_success',
                     'force_retry', 'reopen', 'binding_failure', 'binding_success', 'edit_during_backoff',
                     'external_remove', 'remove_after_failed_close', 'remove_clean')
            for case in cases:
                with self.subTest(case=case):
                    subprocess.run(['java', '-cp', tmp, 'DispatchSaveHarness', case], check=True)
            print(f'Dispatch screen: {len(cases)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
