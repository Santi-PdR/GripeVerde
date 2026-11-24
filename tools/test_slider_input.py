#!/usr/bin/env python3
"""Exercise the actual shared pointer class and three sliders' input methods.

Parent double follows Minecraft 1.20.1 AbstractWidget/AbstractSliderButton:
release accepts any left button, onClick/onDrag apply changed values, and the
container assigns focus after clicking (including false -> true on reselect).
This does not launch Minecraft or test OS focus, screen dispatch or rendering.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method

ROOT = Path(__file__).resolve().parents[1]
WIDGETS = ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/widgets'
BASE = (WIDGETS / 'DiaryPointerSlider.java').read_text()
SOURCES = {name: (WIDGETS / f'{name}.java').read_text()
           for name in ('DiarySlider', 'DiaryMusicSlider', 'DiaryAmbienceSlider')}

PARENT = '''package net.minecraft.client.gui.components;
import net.minecraft.network.chat.Component;
public abstract class AbstractSliderButton {
    public boolean active = true, visible = true;
    private boolean focused, canChangeValue;
    protected double value;
    private int x, y, width, height;
    public static int vanillaClicks;
    public AbstractSliderButton(int x, int y, int w, int h, Component label, double value) {
        this.x=x; this.y=y; width=w; height=h; this.value=value;
    }
    public void setMessage(Component c) {}
    public void setFocused(boolean f) { focused=f; canChangeValue=f; }
    public boolean isFocused() { return focused; }
    protected boolean isValidClickButton(int b) { return b==0; }
    public boolean mouseClicked(double x, double y, int b) {
        if (!active || !visible || !isValidClickButton(b) || x<this.x || x>=this.x+width || y<this.y || y>=this.y+height) return false;
        onClick(x,y); return true;
    }
    public boolean mouseReleased(double x, double y, int b) {
        if (!isValidClickButton(b)) return false;
        onRelease(x,y); return true;
    }
    public boolean mouseDragged(double x, double y, int b, double dx, double dy) {
        if (!isValidClickButton(b)) return false;
        onDrag(x,y,dx,dy); return true;
    }
    public void onClick(double x, double y) { setValueFromMouse(x); }
    protected void onDrag(double x, double y, double dx, double dy) { setValueFromMouse(x); }
    private void setValueFromMouse(double x) {
        double old=value; value=net.minecraft.util.Mth.clamp((x-(this.x+4))/(width-8),0,1);
        if (old!=value) applyValue(); updateMessage();
    }
    public void onRelease(double x, double y) { vanillaClicks++; }
    public boolean keyPressed(int code, int scan, int modifiers) {
        // Like vanilla, this fallback does not inspect active/visible.
        if (canChangeValue && (code==262 || code==263)) {
            double old=value; value=net.minecraft.util.Mth.clamp(value+(code==262 ? 0.01 : -0.01),0,1);
            if (old!=value) applyValue(); updateMessage(); return true;
        }
        return false;
    }
    protected abstract void applyValue();
    protected abstract void updateMessage();
}
'''

GENERIC = '''package com.eternalcraft.gripeverde.client.widgets;
import net.minecraft.network.chat.Component;
import java.util.function.*;
public class DiarySlider extends DiaryPointerSlider {
    private final DoubleFunction<String> labelFormatter = v -> "value " + v;
    private final DoubleConsumer onValueChanged;
    private final boolean applyOnDrag;
    private final Runnable onRelease;
    public DiarySlider(DoubleConsumer changed, boolean live, Runnable released) {
        super(0,0,108,20,Component.literal("test"),0.25);
        onValueChanged=changed; applyOnDrag=live; onRelease=released;
    }
    __METHODS__
}
'''
AUDIO = '''package com.eternalcraft.gripeverde.client.widgets;
import net.minecraft.network.chat.Component;
import com.eternalcraft.gripeverde.client.audio.__MANAGER__;
public class __CLASS__ extends DiaryPointerSlider {
    public __CLASS__() { super(0,0,108,20,Component.literal("test"),0.25); }
    private static Component buildLabel(double value) { return Component.literal("value " + value); }
    __METHODS__
}
'''

HARNESS = r'''package com.eternalcraft.gripeverde.client.widgets;
import com.eternalcraft.gripeverde.client.audio.*;
import net.minecraft.client.gui.components.AbstractSliderButton;
public class SliderInputHarness {
    static int changed, released;
    static boolean reenter;
    static DiaryPointerSlider target;
    static void check(boolean value, String why) { if (!value) throw new AssertionError(why); }
    static DiaryPointerSlider create(String kind, boolean live) {
        return switch(kind) {
            case "generic" -> new DiarySlider(v -> changed++, live, () -> {
                released++;
                if (reenter) check(!target.mouseReleased(50,10,0), "reentrant release committed");
            });
            case "music" -> new DiaryMusicSlider();
            case "ambience" -> new DiaryAmbienceSlider();
            default -> throw new AssertionError(kind);
        };
    }
    static int applications(String kind) {
        return switch(kind) { case "generic" -> changed; case "music" -> MenuMusic.live; default -> MenuAmbience.live; };
    }
    static int commits(String kind) {
        return switch(kind) { case "generic" -> released; case "music" -> MenuMusic.saved; default -> MenuAmbience.saved; };
    }
    static void press(DiaryPointerSlider slider, double x) {
        check(slider.mouseClicked(x,10,0), "valid press rejected");
        // Mirrors AbstractContainerEventHandler.setFocused, even on same child.
        slider.setFocused(false); slider.setFocused(true);
    }
    static void finish(DiaryPointerSlider slider, String kind) {
        check(slider.mouseReleased(80,10,0), "own release rejected");
        check(commits(kind)==1 && UiSounds.clicks==1, "own release must commit/sound once");
        check(AbstractSliderButton.vanillaClicks==0, "vanilla and diary click both emitted");
        int calls=applications(kind);
        check(!slider.mouseReleased(80,10,0), "repeated release accepted");
        check(commits(kind)==1 && UiSounds.clicks==1 && applications(kind)==calls, "repeated release had effects");
    }
    public static void main(String[] args) {
        String test=args[0], kind=args[1];
        DiaryPointerSlider s=create(kind,!test.startsWith("deferred")); target=s;
        switch(test) {
            case "bare_release" -> {
                check(!s.mouseReleased(50,10,0), "release without press accepted");
                check(commits(kind)==0 && applications(kind)==0 && UiSounds.clicks==0 && AbstractSliderButton.vanillaClicks==0,
                      "bare release had effects");
            }
            case "bare_drag" -> {
                check(!s.mouseDragged(80,10,0,10,0), "drag without press accepted");
                check(applications(kind)==0 && commits(kind)==0, "bare drag had effects");
            }
            case "rejected_presses" -> {
                check(!s.mouseClicked(50,10,1), "secondary press accepted");
                check(!s.mouseClicked(-1,10,0), "outside press accepted");
                s.active=false; check(!s.mouseClicked(50,10,0), "inactive press accepted"); s.active=true;
                s.visible=false; check(!s.mouseClicked(50,10,0), "hidden press accepted"); s.visible=true;
                check(!s.mouseReleased(50,10,0) && commits(kind)==0 && applications(kind)==0, "rejected press armed gesture");
            }
            case "click" -> {
                press(s,50); check(applications(kind)==1, "initial click did not preview once"); finish(s,kind);
            }
            case "drag" -> {
                press(s,30); int calls=applications(kind);
                check(s.mouseDragged(80,10,0,50,0), "own drag rejected");
                check(applications(kind)==calls+1, "drag preview duplicated or missing");
                check(commits(kind)==0, "drag persisted before release"); finish(s,kind);
            }
            case "unchanged_drag" -> {
                press(s,29); check(applications(kind)==0, "unchanged click applied value");
                check(s.mouseDragged(29,10,0,0,0), "unchanged drag rejected");
                check(applications(kind)==0, "unchanged drag duplicated preview"); finish(s,kind);
            }
            case "refocus_same" -> {
                s.setFocused(true); press(s,50); finish(s,kind);
            }
            case "focus_loss" -> {
                press(s,50); int calls=applications(kind); s.setFocused(false);
                check(!s.mouseReleased(50,10,0) && !s.mouseDragged(80,10,0,30,0), "focus loss retained gesture");
                check(commits(kind)==0 && applications(kind)==calls && UiSounds.clicks==0, "focus loss committed");
            }
            case "inactive_release", "hidden_release" -> {
                press(s,50); if (test.equals("inactive_release")) s.active=false; else s.visible=false;
                check(!s.mouseReleased(50,10,0), "disabled release accepted"); s.active=s.visible=true;
                check(!s.mouseReleased(50,10,0) && commits(kind)==0 && UiSounds.clicks==0, "disabled gesture not cleared");
            }
            case "inactive_drag" -> {
                press(s,50); s.active=false;
                check(!s.mouseDragged(80,10,0,30,0), "disabled drag accepted"); s.active=true;
                check(!s.mouseReleased(50,10,0) && commits(kind)==0, "disabled drag retained gesture");
            }
            case "secondary_during_drag" -> {
                press(s,50); int calls=applications(kind);
                check(!s.mouseDragged(80,10,1,30,0) && !s.mouseReleased(80,10,1), "secondary event accepted");
                check(applications(kind)==calls && commits(kind)==0, "secondary event changed value"); finish(s,kind);
            }
            case "keyboard" -> {
                s.setFocused(true); check(s.keyPressed(262,0,0), "right key not handled");
                check(applications(kind)==1 && commits(kind)==1, "key must apply/commit once");
                check(!s.mouseReleased(50,10,0) && commits(kind)==1, "keyboard armed mouse release");
            }
            case "keyboard_cancels_pointer" -> {
                press(s,50); check(s.keyPressed(262,0,0), "right key not handled during pointer gesture");
                check(commits(kind)==1, "key failed to commit"); int calls=applications(kind);
                check(!s.mouseReleased(50,10,0) && commits(kind)==1 && applications(kind)==calls, "mouse release repeated keyboard commit");
            }
            case "inactive_key", "hidden_key", "unfocused_key" -> {
                s.setFocused(true);
                if (test.equals("inactive_key")) s.active=false;
                else if (test.equals("hidden_key")) s.visible=false;
                else s.setFocused(false);
                for (int key : new int[] {262,263,266,267,268,269})
                    check(!s.keyPressed(key,0,0), "noninteractive key consumed: " + key);
                check(applications(kind)==0 && commits(kind)==0 && UiSounds.clicks==0, "noninteractive key had effects");
                s.active=s.visible=true; s.setFocused(true);
                check(s.keyPressed(262,0,0) && applications(kind)==1 && commits(kind)==1, "reenabled keyboard stopped working");
            }
            case "deferred_click" -> {
                press(s,50); check(changed==0 && released==0, "deferred initial click applied early");
                finish(s,kind); check(changed==1, "deferred click must apply exactly once");
            }
            case "deferred_drag" -> {
                press(s,30); s.mouseDragged(80,10,0,50,0);
                check(changed==0 && released==0, "deferred drag applied early");
                finish(s,kind); check(changed==1, "deferred drag must apply exactly once");
            }
            case "deferred_keyboard" -> {
                s.setFocused(true); check(s.keyPressed(262,0,0), "deferred keyboard not handled");
                check(changed==1 && released==1, "deferred keyboard not committed immediately");
            }
            case "reentrant_release" -> {
                reenter=true; press(s,50); finish(s,kind); check(released==1, "reentrant commit repeated callback");
            }
            default -> throw new AssertionError(test);
        }
    }
}
'''


def slider_source(name, source):
    signatures = ['protected void updateMessage(', 'protected void commitPointerValue(',
                  'public boolean keyPressed(', 'protected void applyValue(']
    if name == 'DiarySlider':
        signatures += ['private void fireValueChanged(', 'private void fireRelease(']
        template = GENERIC
    else:
        template = AUDIO.replace('__CLASS__', name).replace('__MANAGER__', 'MenuMusic' if name == 'DiaryMusicSlider' else 'MenuAmbience')
    return template.replace('__METHODS__', '\n'.join(method(source, sig) for sig in signatures))


class Wiring(unittest.TestCase):
    def test_all_sliders_share_pointer_guard(self):
        for name, source in SOURCES.items():
            self.assertIn(f'class {name} extends DiaryPointerSlider', source)
            self.assertNotIn('boolean mouseReleased(', source)
            self.assertNotIn('void onDrag(', source)

    def test_press_precedes_initial_apply(self):
        body = method(BASE, 'public void onClick(')
        self.assertLess(body.index('pointerPressed = true'), body.index('super.onClick('))
        self.assertIn('isPointerPressed() && !this.applyOnDrag', SOURCES['DiarySlider'])

    def test_keyboard_guard_precedes_parent_fallback(self):
        for source in SOURCES.values():
            body = method(source, 'public boolean keyPressed(')
            self.assertIn('!this.active || !this.visible || !this.isFocused()', body)
            self.assertLess(body.index('return false;'), body.index('super.keyPressed('))

    def test_ci_requires_execution(self):
        for name in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_slider_input.py --require-java', (ROOT / name).read_text())


class Executable(unittest.TestCase):
    def test_pointer_and_keyboard_methods(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        common = ('bare_release', 'bare_drag', 'rejected_presses', 'click', 'drag', 'unchanged_drag',
                  'refocus_same', 'focus_loss', 'inactive_release', 'hidden_release', 'inactive_drag',
                  'secondary_during_drag', 'keyboard', 'keyboard_cancels_pointer',
                  'inactive_key', 'hidden_key', 'unfocused_key')
        cases = [(case, kind) for case in common for kind in ('generic', 'music', 'ambience')]
        cases += [(case, 'generic') for case in ('deferred_click', 'deferred_drag', 'deferred_keyboard', 'reentrant_release')]
        with tempfile.TemporaryDirectory() as tmp:
            work = Path(tmp)
            prefix = 'com/eternalcraft/gripeverde/client/'
            sources = {
                prefix + 'widgets/DiaryPointerSlider.java': BASE,
                prefix + 'widgets/SliderInputHarness.java': HARNESS,
                'net/minecraft/client/gui/components/AbstractSliderButton.java': PARENT,
                'net/minecraft/network/chat/Component.java': 'package net.minecraft.network.chat; public class Component { public static Component literal(String s) { return new Component(); } }',
                'net/minecraft/util/Mth.java': 'package net.minecraft.util; public class Mth { public static double clamp(double v,double min,double max) { return Math.max(min,Math.min(max,v)); } }',
                'org/lwjgl/glfw/GLFW.java': 'package org.lwjgl.glfw; public class GLFW { public static final int GLFW_KEY_LEFT=263, GLFW_KEY_RIGHT=262, GLFW_KEY_PAGE_DOWN=267, GLFW_KEY_PAGE_UP=266, GLFW_KEY_HOME=268, GLFW_KEY_END=269; }',
                prefix + 'audio/UiSounds.java': 'package com.eternalcraft.gripeverde.client.audio; public class UiSounds { public static int clicks; public static void playClick() { clicks++; } }',
            }
            for name, source in SOURCES.items():
                sources[prefix + f'widgets/{name}.java'] = slider_source(name, source)
            for name in ('MenuMusic', 'MenuAmbience'):
                sources[prefix + f'audio/{name}.java'] = ('package com.eternalcraft.gripeverde.client.audio; public class ' + name + ' { public static int live,saved; public static void setUserVolumeLive(float v) { live++; } public static void setUserVolume(float v) { saved++; } }')
            paths = []
            for name, text in sources.items():
                path = work / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
            for case, kind in cases:
                with self.subTest(case=case, kind=kind):
                    subprocess.run(['java', '-cp', tmp, 'com.eternalcraft.gripeverde.client.widgets.SliderInputHarness', case, kind], check=True, timeout=15)
            print(f'Slider input: {len(cases)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
