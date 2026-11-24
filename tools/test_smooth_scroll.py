#!/usr/bin/env python3
"""Execute the real SmoothScroll.java against minimal Minecraft/list test doubles.

Requires a JDK. CI uses --require-java (missing Java is an error); local runs
without a JDK report a skip. No Minecraft client, renderer or real input runs.
Compilation outputs live in a temporary directory, never in the repository.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/util/SmoothScroll.java'

MINECRAFT = '''package net.minecraft.client;
public class Minecraft {
    public static Minecraft instance = new Minecraft();
    public Object level;
    public static Minecraft getInstance() { return instance; }
}
'''
CONFIG = """package com.eternalcraft.gripeverde.config;
public class ClientConfig {
    public static final Value REDUCED_MOTION = new Value();
    public static class Value {
        public boolean value,fail;
        public boolean get(){if(fail)throw new IllegalStateException("not loaded");return value;}
    }
}
"""
LIST = '''package net.minecraft.client.gui.components;
public class AbstractSelectionList<E> {
    private double scroll;
    private int max;
    public AbstractSelectionList(int max) { this.max = max; }
    public double getScrollAmount() { return scroll; }
    public int getMaxScroll() { return max; }
    public void setScrollAmount(double value) { scroll = Math.max(0, Math.min(max, value)); }
    public void setMax(int value) { max = value; setScrollAmount(scroll); }
}
'''
HARNESS = '''import com.eternalcraft.gripeverde.client.util.SmoothScroll;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import com.eternalcraft.gripeverde.config.ClientConfig;

public class ScrollHarness {
    static void at(AbstractSelectionList<?> list, double expected) {
        if (Math.abs(list.getScrollAmount() - expected) > 0.000001) {
            throw new AssertionError("Expected " + expected + ", got " + list.getScrollAmount());
        }
    }
    public static void main(String[] args) {
        SmoothScroll scroll = new SmoothScroll(0.5, 0.1);
        AbstractSelectionList<?> list = new AbstractSelectionList<>(1000);
        switch (args[0]) {
            case "wheel_easing":
                scroll.wheel(list, -1, 100);
                scroll.tick(list); at(list, 50);
                scroll.tick(list); at(list, 75);
                scroll.tick(list); at(list, 87.5);
                for (int i = 0; i < 30; i++) scroll.tick(list);
                at(list, 100);
                break;
            case "idle_drag":
                list.setScrollAmount(200); scroll.attach(list);
                list.setScrollAmount(600); scroll.tick(list); at(list, 600);
                scroll.tick(list); at(list, 600);
                break;
            case "drag_during_easing":
                scroll.wheel(list, -1, 100); scroll.tick(list); at(list, 50);
                list.setScrollAmount(400); scroll.tick(list); at(list, 400);
                scroll.tick(list); at(list, 400);
                break;
            case "wheel_after_drag_before_tick":
                scroll.wheel(list, -1, 100); scroll.tick(list);
                list.setScrollAmount(400); scroll.wheel(list, -1, 100);
                scroll.tick(list); at(list, 450);
                break;
            case "keyboard_scroll":
                scroll.wheel(list, -1, 200); scroll.tick(list); at(list, 100);
                // Simulates ensureVisible/keyboard navigation changing scroll.
                list.setScrollAmount(800); scroll.tick(list); at(list, 800);
                break;
            case "replacement_list_same_position":
                list.setScrollAmount(100); scroll.attach(list);
                scroll.wheel(list, -1, 100);
                AbstractSelectionList<?> replacement = new AbstractSelectionList<>(1000);
                replacement.setScrollAmount(100);
                scroll.tick(replacement); at(replacement, 100); at(list, 100);
                break;
            case "set_target_fresh":
                list.setScrollAmount(100); scroll.setTarget(list, 300);
                scroll.tick(list); at(list, 200);
                scroll.tick(list); at(list, 250);
                break;
            case "set_target_after_drag":
                scroll.attach(list); list.setScrollAmount(400);
                scroll.setTarget(list, 600); scroll.tick(list); at(list, 500);
                break;
            case "world_no_easing":
                Minecraft.instance.level = new Object();
                scroll.wheel(list, -1, 200); scroll.tick(list); at(list, 200);
                list.setScrollAmount(600); scroll.tick(list); at(list, 600);
                scroll.wheel(list, -1, 100); scroll.tick(list); at(list, 700);
                break;
            case "bounds_and_shrinking_list":
                scroll.wheel(list, -1, 10000); scroll.tick(list); at(list, 500);
                list.setMax(10); scroll.tick(list); at(list, 10);
                scroll.wheel(list, 1, 10000); scroll.tick(list); at(list, 5);
                for (int i = 0; i < 20; i++) scroll.tick(list);
                at(list, 0);
                break;
            case "null_list":
                scroll.attach(null); scroll.snapToCurrent(null);
                scroll.setTarget(null, 100); scroll.tick(null);
                if (scroll.wheel(null, 1, 100)) throw new AssertionError("Null wheel consumed");
                scroll.wheel(list, -1, 100); scroll.tick(list); at(list, 50);
                break;
            case "snap_cancels_target":
                scroll.wheel(list, -1, 100); scroll.tick(list);
                scroll.snapToCurrent(list); scroll.tick(list); at(list, 50);
                break;
            case "fractional_external_change":
                scroll.attach(list); list.setScrollAmount(0.125);
                scroll.tick(list); at(list, 0.125);
                break;
            case "accumulated_wheel":
                scroll.wheel(list, -1, 100); scroll.wheel(list, -1, 100);
                scroll.tick(list); at(list, 100);
                scroll.tick(list); at(list, 150);
                break;
            case "reduced_no_easing":
                ClientConfig.REDUCED_MOTION.value = true;
                scroll.wheel(list, -1, 200); scroll.tick(list); at(list, 200);
                scroll.wheel(list, 1, 100); scroll.tick(list); at(list, 100);
                break;
            case "reduced_toggle_midflight":
                scroll.wheel(list, -1, 200); scroll.tick(list); at(list, 100);
                ClientConfig.REDUCED_MOTION.value = true;
                scroll.tick(list); at(list, 200);
                list.setScrollAmount(400); scroll.tick(list); at(list, 400);
                ClientConfig.REDUCED_MOTION.value = false;
                scroll.wheel(list, -1, 100); scroll.tick(list); at(list, 450);
                break;
            case "reduced_set_target":
                ClientConfig.REDUCED_MOTION.value = true;
                scroll.setTarget(list, 300); scroll.tick(list); at(list, 300);
                scroll.setTarget(list, -10); scroll.tick(list); at(list, 0);
                break;
            case "null_minecraft_reduced":
                Minecraft.instance = null; ClientConfig.REDUCED_MOTION.value = true;
                scroll.wheel(list, -1, 100); scroll.tick(list); at(list, 100);
                break;
            case "unloaded_motion_config":
                ClientConfig.REDUCED_MOTION.fail = true;
                scroll.wheel(list, -1, 100); scroll.tick(list); at(list, 50);
                break;
            case "world_unloaded_motion_config":
                Minecraft.instance.level = new Object(); ClientConfig.REDUCED_MOTION.fail = true;
                scroll.wheel(list, -1, 100); scroll.tick(list); at(list, 100);
                break;
            case "null_minecraft":
                Minecraft.instance = null;
                scroll.wheel(list, -1, 100); scroll.tick(list); at(list, 50);
                break;
            default: throw new AssertionError("Unknown test " + args[0]);
        }
    }
}
'''


class Wiring(unittest.TestCase):
    def test_shared_motion_policy(self):
        self.assertIn('if (MotionPolicy.isReduced())', SOURCE.read_text())

    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_smooth_scroll.py --require-java', (ROOT / path).read_text())


class SmoothScrollJavaTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        if not shutil.which('javac') or not shutil.which('java'):
            raise unittest.SkipTest('JDK not installed; Java controller tests not executed')
        cls.temp = tempfile.TemporaryDirectory(prefix='gripeverde-scroll-')
        cls.addClassCleanup(cls.temp.cleanup)
        cls.work = Path(cls.temp.name)
        sources = []
        for name, content in (
            ('net/minecraft/client/Minecraft.java', MINECRAFT),
            ('com/eternalcraft/gripeverde/config/ClientConfig.java', CONFIG),
            ('com/eternalcraft/gripeverde/client/util/MotionPolicy.java',
             (ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/util/MotionPolicy.java').read_text()),
            ('net/minecraft/client/gui/components/AbstractSelectionList.java', LIST),
            ('ScrollHarness.java', HARNESS),
        ):
            path = cls.work / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding='utf-8')
            sources.append(str(path))
        subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8',
                        '-d', str(cls.work), str(SOURCE), *sources], check=True, timeout=60)

    def run_case(self, name):
        subprocess.run(['java', '-cp', str(self.work), 'ScrollHarness', name],
                       check=True, timeout=15)


# One process per scenario isolates the fake Minecraft singleton.
for _case in (
    'wheel_easing', 'idle_drag', 'drag_during_easing', 'wheel_after_drag_before_tick',
    'keyboard_scroll', 'replacement_list_same_position', 'set_target_fresh',
    'set_target_after_drag', 'world_no_easing', 'bounds_and_shrinking_list',
    'null_list', 'snap_cancels_target', 'fractional_external_change',
    'accumulated_wheel', 'null_minecraft', 'reduced_no_easing', 'reduced_toggle_midflight',
    'reduced_set_target', 'null_minecraft_reduced', 'unloaded_motion_config', 'world_unloaded_motion_config',
):
    def _test(self, name=_case):
        self.run_case(name)
    setattr(SmoothScrollJavaTests, 'test_' + _case, _test)

if __name__ == '__main__':
    if '--require-java' in sys.argv:
        sys.argv.remove('--require-java')
        if not shutil.which('javac') or not shutil.which('java'):
            sys.exit('JDK required: install Java 17 to run the controller tests')
    unittest.main(verbosity=2)
