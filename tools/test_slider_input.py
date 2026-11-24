#!/usr/bin/env python3
"""Exercise the actual shared pointer class and three sliders' input methods.

Parent double follows Minecraft 1.20.1 AbstractWidget/AbstractSliderButton:
release accepts any left button, onClick/onDrag apply changed values, and the
container assigns focus after clicking (including false -> true on reselect).
Screen release methods are extracted too; container dispatch is simulated.
This does not launch Minecraft or test OS focus, rendering or native input.
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

SCREENS = {name: (ROOT / f'src/main/java/com/eternalcraft/gripeverde/client/screens/{name}.java').read_text()
           for name in ('DiaryOptionsScreen', 'DiaryHouseScreen', 'DiarySoundOptionsScreen')}

LISTENER = """package net.minecraft.client.gui.components.events;
public interface GuiEventListener {
    void setFocused(boolean focused);
    boolean isFocused();
    default boolean mouseReleased(double x,double y,int button) { return false; }
    default boolean isMouseOver(double x,double y) { return false; }
}
"""
CONTAINER = """package net.minecraft.client.gui.components.events;
public interface ContainerEventHandler {
    GuiEventListener getFocused();
    void setDragging(boolean dragging);
}
"""
SCREEN = """package net.minecraft.client.gui.screens;
import net.minecraft.client.gui.components.events.*;
public class Screen implements ContainerEventHandler {
    public java.util.List<GuiEventListener> widgets = new java.util.ArrayList<>();
    public GuiEventListener focused;
    public boolean dragging;
    public GuiEventListener getFocused() { return focused; }
    public void setDragging(boolean value) { dragging=value; }
    public boolean mouseReleased(double x,double y,int button) {
        // Minecraft 1.20.1 ContainerEventHandler: hit-test release, not capture.
        setDragging(false);
        return widgets.stream().filter(w -> w.isMouseOver(x,y)).findFirst()
            .filter(w -> w.mouseReleased(x,y,button)).isPresent();
    }
}
"""

PARENT = '''package net.minecraft.client.gui.components;
import net.minecraft.network.chat.Component;
public abstract class AbstractSliderButton implements net.minecraft.client.gui.components.events.GuiEventListener {
    public boolean active = true, visible = true;
    private boolean focused, canChangeValue;
    protected double value;
    private int x, y, width, height;
    public static int vanillaClicks;
    public AbstractSliderButton(int x, int y, int w, int h, Component label, double value) {
        this.x=x; this.y=y; width=w; height=h; this.value=value;
    }
    public boolean isMouseOver(double x,double y) {
        return active && visible && x>=this.x && x<this.x+width && y>=this.y && y<this.y+height;
    }
    public int getX() { return x; }
    public int getWidth() { return width; }
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
    public DiarySlider(DoubleConsumer changed, boolean live, Runnable released, int width) {
        super(0,0,width,20,Component.literal("test"),0.25);
        onValueChanged=changed; applyOnDrag=live; onRelease=released;
    }
    __METHODS__
}
'''
AUDIO = '''package com.eternalcraft.gripeverde.client.widgets;
import net.minecraft.network.chat.Component;
import com.eternalcraft.gripeverde.client.audio.__MANAGER__;
public class __CLASS__ extends DiaryPointerSlider {
    public __CLASS__(int width) { super(0,0,width,20,Component.literal("test"),0.25); }
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
    static DiaryPointerSlider create(String kind, boolean live, int width) {
        return switch(kind) {
            case "generic" -> new DiarySlider(v -> changed++, live, () -> {
                released++;
                if (reenter) check(!target.mouseReleased(50,10,0), "reentrant release committed");
            }, width);
            case "music" -> new DiaryMusicSlider(width);
            case "ambience" -> new DiaryAmbienceSlider(width);
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
    static class OtherWidget implements net.minecraft.client.gui.components.events.GuiEventListener {
        int releases;
        public void setFocused(boolean focused) {}
        public boolean isFocused() { return false; }
        public boolean isMouseOver(double x,double y) { return x>=200 && x<300 && y>=0 && y<40; }
        public boolean mouseReleased(double x,double y,int button) { releases++; return true; }
    }
    static void capture(DiaryPointerSlider s,String kind,String test,String screenName) {
        net.minecraft.client.gui.screens.Screen screen=switch(screenName) {
            case "DiaryOptionsScreen" -> new com.eternalcraft.gripeverde.client.screens.DiaryOptionsScreen();
            case "DiaryHouseScreen" -> new com.eternalcraft.gripeverde.client.screens.DiaryHouseScreen();
            case "DiarySoundOptionsScreen" -> new com.eternalcraft.gripeverde.client.screens.DiarySoundOptionsScreen();
            default -> throw new AssertionError(screenName);
        };
        OtherWidget other=new OtherWidget();
        screen.widgets.add(other); screen.widgets.add(s); screen.focused=s;
        if (test.equals("capture_no_press")) {
            check(screen.mouseReleased(250,10,0) && other.releases==1 && commits(kind)==0, "unowned release did not fall back");
            return;
        }
        press(s,50); screen.dragging=true;
        if (test.equals("capture_focus_loss")) {
            s.setFocused(false); screen.focused=other;
            check(screen.mouseReleased(250,10,0) && other.releases==1 && commits(kind)==0, "lost-focus gesture captured");
            return;
        }
        if (test.equals("capture_inactive") || test.equals("capture_hidden")) {
            if (test.equals("capture_inactive")) s.active=false; else s.visible=false;
            check(screen.mouseReleased(250,10,0), "canceled owned release not consumed");
            check(other.releases==0 && commits(kind)==0 && !screen.dragging, "canceled release leaked into another widget");
            s.active=s.visible=true;
            check(!s.mouseReleased(50,10,0), "canceled capture retained gesture"); return;
        }
        if (test.equals("capture_secondary")) {
            check(screen.mouseReleased(250,10,1) && other.releases==1, "secondary release should follow parent");
            check(commits(kind)==0, "secondary release committed left gesture");
        }
        boolean outside=test.equals("capture_outside") || test.equals("capture_repeat");
        double x=outside ? 500 : 250;
        check(s.mouseDragged(x,10,0,x-50,0), "owned drag outside rejected");
        if (test.equals("capture_deferred")) check(changed==0 && released==0, "deferred capture applied early");
        check(screen.mouseReleased(x,10,0), "outside owned release not captured");
        check(commits(kind)==1 && !screen.dragging && UiSounds.clicks==1, "captured release must commit once and end dragging");
        int expectedOther=test.equals("capture_secondary") ? 1 : 0;
        check(other.releases==expectedOther, "owned release reached foreign hovered widget");
        check(AbstractSliderButton.vanillaClicks==0, "captured release emitted vanilla sound");
        if (test.equals("capture_deferred")) check(changed==1, "deferred captured value not applied once");
        if (test.equals("capture_repeat")) {
            check(!screen.mouseReleased(x,10,0) && commits(kind)==1 && UiSounds.clicks==1, "repeat outside release committed twice");
        }
    }
    public static void main(String[] args) throws Exception {
        String test=args[0], kind=args[1];
        int width=switch(test) { case "width_one" -> 1; case "width_four" -> 4; case "width_eight" -> 8;
            case "width_nine" -> 9; case "width_twelve" -> 12; default -> 108; };
        DiaryPointerSlider s=create(kind,!test.contains("deferred"),width); target=s;
        if (test.startsWith("capture_")) { capture(s,kind,test,args[2]); return; }
        switch(test) {
            case "width_one", "width_four", "width_eight", "width_nine", "width_twelve" -> {
                java.lang.reflect.Field value=AbstractSliderButton.class.getDeclaredField("value"); value.setAccessible(true);
                press(s,width/2.0);
                check(value.getDouble(s)==0.5, "center click must be finite midpoint at width " + width);
                check(s.mouseDragged(-100,10,0,-100,0) && value.getDouble(s)==0.0, "left drag reversed or nonfinite at width " + width);
                check(s.mouseDragged(200,10,0,300,0) && value.getDouble(s)==1.0, "right drag reversed or nonfinite at width " + width);
                finish(s,kind);
            }
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
            case "keyboard_endpoints" -> {
                s.setFocused(true); check(s.keyPressed(268,0,0), "Home not handled");
                check(applications(kind)==1 && commits(kind)==1, "Home must commit changed value");
                int sounds=UiSounds.clicks;
                for (int i=0;i<10;i++) for (int key:new int[] {268,263,267})
                    check(s.keyPressed(key,0,0), "endpoint key must remain consumed");
                check(applications(kind)==1 && commits(kind)==1 && UiSounds.clicks==sounds, "zero endpoint repeated no-op callbacks");
                check(s.keyPressed(269,0,0), "End not handled"); sounds=UiSounds.clicks;
                for (int i=0;i<10;i++) for (int key:new int[] {269,262,266})
                    check(s.keyPressed(key,0,0), "endpoint key must remain consumed");
                check(applications(kind)==2 && commits(kind)==2 && UiSounds.clicks==sounds, "one endpoint repeated no-op callbacks");
            }
            case "keyboard_repeated_step" -> {
                s.setFocused(true);
                for (int i=0;i<100;i++) s.keyPressed(262,0,0);
                int apps=applications(kind), writes=commits(kind), sounds=UiSounds.clicks;
                for (int i=0;i<20;i++) check(s.keyPressed(262,0,0), "saturated arrow must be consumed");
                check(applications(kind)==apps && commits(kind)==writes && UiSounds.clicks==sounds, "held arrow kept applying at maximum");
                for (int i=0;i<100;i++) s.keyPressed(263,0,0);
                apps=applications(kind); writes=commits(kind); sounds=UiSounds.clicks;
                for (int i=0;i<20;i++) check(s.keyPressed(263,0,0), "saturated arrow must be consumed");
                check(applications(kind)==apps && commits(kind)==writes && UiSounds.clicks==sounds, "held arrow kept applying at minimum");
            }
            case "keyboard_finishes_same_value" -> {
                press(s,0); int apps=applications(kind);
                check(s.keyPressed(268,0,0), "Home not handled during pointer gesture");
                check(applications(kind)==apps+1 && commits(kind)==1, "unchanged key must finish pending pointer gesture once");
                check(!s.mouseReleased(0,10,0), "key left pointer gesture armed");
                check(s.keyPressed(268,0,0) && applications(kind)==apps+1 && commits(kind)==1, "finished gesture repeated endpoint commit");
            }
            case "deferred_keyboard_same_value" -> {
                press(s,0); check(changed==0 && released==0, "deferred pointer preview applied early");
                check(s.keyPressed(268,0,0), "deferred Home not handled");
                check(changed==1 && released==1, "same-value key discarded pending deferred edit");
                check(!s.mouseReleased(0,10,0), "deferred edit remained armed after keyboard commit");
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

    def test_all_slider_screens_capture_before_parent(self):
        screen_dir = ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/screens'
        import re
        users = {path.stem for path in screen_dir.glob('*.java')
                 if re.search(r'new Diary(?:Music|Ambience)?Slider\(', path.read_text())}
        self.assertEqual(users, set(SCREENS))
        for source in SCREENS.values():
            body = method(source, 'public boolean mouseReleased(')
            self.assertLess(body.index('DiaryPointerSlider.releaseCaptured('), body.index('super.mouseReleased('))

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
                  'inactive_key', 'hidden_key', 'unfocused_key',
                  'width_one', 'width_four', 'width_eight', 'width_nine', 'width_twelve',
                  'keyboard_endpoints', 'keyboard_repeated_step', 'keyboard_finishes_same_value')
        cases = [(case, kind) for case in common for kind in ('generic', 'music', 'ambience')]
        cases += [(case, 'generic') for case in ('deferred_click', 'deferred_drag', 'deferred_keyboard', 'reentrant_release', 'deferred_keyboard_same_value')]
        capture_cases = [(case, kind, screen) for case in ('capture_outside', 'capture_over_other',
                         'capture_no_press', 'capture_focus_loss', 'capture_inactive', 'capture_hidden',
                         'capture_secondary', 'capture_repeat')
                         for kind in ('generic', 'music', 'ambience') for screen in SCREENS]
        capture_cases += [('capture_deferred', 'generic', screen) for screen in SCREENS]
        with tempfile.TemporaryDirectory() as tmp:
            work = Path(tmp)
            prefix = 'com/eternalcraft/gripeverde/client/'
            sources = {
                prefix + 'widgets/DiaryPointerSlider.java': BASE,
                prefix + 'widgets/SliderInputHarness.java': HARNESS,
                'net/minecraft/client/gui/components/AbstractSliderButton.java': PARENT,
                'net/minecraft/client/gui/components/events/GuiEventListener.java': LISTENER,
                'net/minecraft/client/gui/components/events/ContainerEventHandler.java': CONTAINER,
                'net/minecraft/client/gui/screens/Screen.java': SCREEN,
                'net/minecraft/network/chat/Component.java': 'package net.minecraft.network.chat; public class Component { public static Component literal(String s) { return new Component(); } }',
                'net/minecraft/util/Mth.java': 'package net.minecraft.util; public class Mth { public static double clamp(double v,double min,double max) { return Math.max(min,Math.min(max,v)); } }',
                'org/lwjgl/glfw/GLFW.java': 'package org.lwjgl.glfw; public class GLFW { public static final int GLFW_KEY_LEFT=263, GLFW_KEY_RIGHT=262, GLFW_KEY_PAGE_DOWN=267, GLFW_KEY_PAGE_UP=266, GLFW_KEY_HOME=268, GLFW_KEY_END=269; }',
                prefix + 'audio/UiSounds.java': 'package com.eternalcraft.gripeverde.client.audio; public class UiSounds { public static int clicks; public static void playClick() { clicks++; } }',
            }
            for name, source in SCREENS.items():
                body = method(source, 'public boolean mouseReleased(')
                sources[prefix + f'screens/{name}.java'] = ('package com.eternalcraft.gripeverde.client.screens; '
                    'import com.eternalcraft.gripeverde.client.widgets.DiaryPointerSlider; public class ' + name
                    + ' extends net.minecraft.client.gui.screens.Screen { ' + body + ' }')
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
            for case, kind, screen in capture_cases:
                with self.subTest(case=case, kind=kind, screen=screen):
                    subprocess.run(['java', '-cp', tmp, 'com.eternalcraft.gripeverde.client.widgets.SliderInputHarness', case, kind, screen], check=True, timeout=15)
            print(f'Slider input: {len(cases) + len(capture_cases)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
