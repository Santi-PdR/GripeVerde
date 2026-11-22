#!/usr/bin/env python3
"""Run real hotkey polling methods with simulated client/window/input state.

Does not launch Minecraft, use native GLFW, connect to a server or test OS focus.
CI requires JDK 17. This tests action decisions, not the chat network protocol.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method

ROOT = Path(__file__).resolve().parents[1]
SOURCE = (ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/DispatchHotkeys.java').read_text()

HARNESS = '''import java.util.*;
public class DispatchHotkeyHarness {
    static final boolean[] slotDown = new boolean[20];
    static final int[] observedSlotKeys = new int[20];
    static boolean openDown, suspended;
    static int observedOpenKey = Integer.MIN_VALUE, sends, opens, reads;
    static { Arrays.fill(observedSlotKeys, Integer.MIN_VALUE); }
    static boolean suspendedByCopyL() { return suspended; }
    static boolean send(Minecraft mc, int slot) { sends++; return true; }
    static final Set<Integer> downKeys = new HashSet<>();
    static final Set<Integer> downButtons = new HashSet<>();
    static class DispatchLibrary {
        static final int SLOT_COUNT = 20, UNBOUND = -1;
        static int opening = 346;
        static int[] keys = new int[20];
        static { Arrays.fill(keys, UNBOUND); keys[0] = 65; }
        static int openKey() { return opening; }
        static int key(int slot) { return keys[slot]; }
        static boolean isKeyboard(int key) { return key >= 32 && key <= 348; }
        static boolean isMouse(int key) { return key >= 1000 && key <= 1007; }
        static int mouseButton(int key) { return key - 1000; }
    }
    static class InputConstants {
        static boolean isKeyDown(long window, int key) { reads++; return downKeys.contains(key); }
    }
    static class GLFW {
        static final int GLFW_PRESS = 1;
        static int glfwGetMouseButton(long window, int button) { reads++; return downButtons.contains(button) ? 1 : 0; }
    }
    static class TickEvent {
        enum Phase { START, END }
        static class ClientTickEvent {
            Phase phase;
            ClientTickEvent(Phase p) { phase = p; }
        }
    }
    static class Window { long getWindow() { return 42; } }
    static class Player { Object connection = new Object(); }
    static class DiaryDispatchScreen { DiaryDispatchScreen(Object parent) {} }
    static class Minecraft {
        static Minecraft instance = new Minecraft();
        boolean active = true;
        Object screen, overlay;
        Window window = new Window();
        Player player = new Player();
        static Minecraft getInstance() { return instance; }
        Window getWindow() { return window; }
        boolean isWindowActive() { return active; }
        Object getOverlay() { return overlay; }
        void setScreen(Object value) { screen = value; opens++; }
    }
    __METHODS__
    static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    static void tick() { onClientTick(new TickEvent.ClientTickEvent(TickEvent.Phase.END)); }
    static void press(int key) { downKeys.add(key); tick(); }
    static void release(int key) { downKeys.remove(key); tick(); }
    public static void main(String[] args) {
        Minecraft mc = Minecraft.instance;
        switch (args[0]) {
            case "press_hold_release":
                tick(); press(65); tick(); tick(); check(sends == 1);
                release(65); press(65); check(sends == 2); break;
            case "startup_held":
                press(65); tick(); check(sends == 0);
                release(65); press(65); check(sends == 1); break;
            case "inactive":
                tick(); int before = reads; mc.active = false;
                downKeys.add(65); downKeys.add(346); tick(); tick();
                check(sends == 0 && opens == 0 && reads == before); break;
            case "focus_resume_held":
                tick(); mc.active = false; tick(); downKeys.add(65);
                mc.active = true; tick(); tick(); check(sends == 0);
                release(65); press(65); check(sends == 1); break;
            case "focus_resume_open_held":
                tick(); mc.active = false; tick(); downKeys.add(346);
                mc.active = true; tick(); tick(); check(opens == 0);
                release(346); press(346); check(opens == 1); break;
            case "overlay":
                tick(); int overlayBaseline = reads; mc.overlay = new Object();
                downKeys.add(65); downKeys.add(346); tick();
                check(sends == 0 && opens == 0 && reads == overlayBaseline); break;
            case "overlay_resume_held":
                tick(); mc.overlay = new Object(); tick(); downKeys.add(65);
                mc.overlay = null; tick(); tick(); check(sends == 0);
                release(65); press(65); check(sends == 1); break;
            case "copyl_suspended":
                tick(); int copyBaseline = reads; suspended = true; press(65);
                check(sends == 0 && reads == copyBaseline);
                suspended = false; tick(); check(sends == 0);
                release(65); press(65); check(sends == 1); break;
            case "screen_blocks":
                tick(); mc.screen = new Object(); press(65);
                check(sends == 0); mc.screen = null; tick(); check(sends == 0);
                release(65); press(65); check(sends == 1); break;
            case "opening_blocks_send":
                tick(); downKeys.add(65); press(346);
                check(opens == 1 && sends == 0 && mc.screen instanceof DiaryDispatchScreen); break;
            case "rebind_held":
                tick(); DispatchLibrary.keys[0] = 66; press(66);
                check(sends == 0); release(66); press(66); check(sends == 1); break;
            case "mouse_resume_held":
                DispatchLibrary.keys[0] = 1003; tick(); mc.active = false; tick();
                downButtons.add(3); mc.active = true; tick(); check(sends == 0);
                downButtons.remove(3); tick(); downButtons.add(3); tick(); check(sends == 1); break;
            case "start_phase_ignored":
                downKeys.add(65); onClientTick(new TickEvent.ClientTickEvent(TickEvent.Phase.START));
                check(reads == 0 && sends == 0); break;
            case "no_player":
                tick(); mc.player = null; press(65); check(sends == 0); break;
            case "no_connection":
                tick(); mc.player.connection = null; press(65); check(sends == 0); break;
            case "missing_window_resume":
                tick(); mc.window = null; tick(); downKeys.add(65);
                mc.window = new Window(); tick(); check(sends == 0);
                release(65); press(65); check(sends == 1); break;
            case "missing_client_resume":
                tick(); Minecraft.instance = null; tick(); downKeys.add(65);
                Minecraft.instance = mc; tick(); check(sends == 0);
                release(65); press(65); check(sends == 1); break;
            default: throw new AssertionError(args[0]);
        }
    }
}
'''


class Wiring(unittest.TestCase):
    def test_focus_guard_precedes_polling(self):
        body = method(SOURCE, 'public static void onClientTick(')
        for guard in ('!minecraft.isWindowActive()', 'minecraft.getOverlay() != null'):
            self.assertLess(body.index(guard), body.index('pollOpen(minecraft)'))
        self.assertIn('reset();', body)

    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_dispatch_hotkeys.py --require-java', (ROOT / path).read_text())

    def test_logout_resets(self):
        self.assertIn('reset();', method(SOURCE, 'public static void onLoggingOut('))


class Executable(unittest.TestCase):
    def test_polling_methods(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; executable checks required in CI')
        signatures = ('public static void onClientTick(', 'private static void pollOpen(',
                      'private static void pollSlots(', 'private static boolean isDown(',
                      'private static void reset(')
        methods = '\n'.join(method(SOURCE, sig) for sig in signatures)
        cases = ('press_hold_release', 'startup_held', 'inactive', 'focus_resume_held',
                 'focus_resume_open_held', 'overlay', 'overlay_resume_held', 'copyl_suspended',
                 'screen_blocks', 'opening_blocks_send', 'rebind_held', 'mouse_resume_held',
                 'start_phase_ignored', 'no_player', 'no_connection', 'missing_window_resume',
                 'missing_client_resume')
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / 'DispatchHotkeyHarness.java'
            path.write_text(HARNESS.replace('__METHODS__', methods))
            subprocess.run(['javac', '--release', '17', '-d', tmp, str(path)], check=True, timeout=60)
            for case in cases:
                with self.subTest(case=case):
                    subprocess.run(['java', '-cp', tmp, 'DispatchHotkeyHarness', case], check=True, timeout=15)
            print(f'Dispatch hotkeys: {len(cases)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
