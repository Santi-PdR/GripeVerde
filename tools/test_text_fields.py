#!/usr/bin/env python3
"""Run complete DiaryTextField against official-name and Forge-SRG EditBox doubles.

Also run actual server-form resize/key methods with a fake screen lifecycle.
No native input, glyph rendering or Minecraft Screen traversal is exercised.
CI additionally runs TextFieldGeometryRegression against the real EditBox.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method

ROOT = Path(__file__).resolve().parents[1]
PREFIX = 'com/eternalcraft/gripeverde/'
SOURCE = (ROOT / ('src/main/java/'+PREFIX+'client/widgets/DiaryTextField.java')).read_text()
SCREENS = {name: (ROOT / f'src/main/java/{PREFIX}client/screens/{name}.java').read_text()
           for name in ('DiaryDirectJoinScreen', 'DiaryEditServerScreen')}
# Minecraft 1.20.1 EditBox, MCPConfig joined.tsrg blob
# 4434b02cbbed8dd360b320d20b1298045cd4deaa, class epr/C_3464_.
# Separate fixture data, not inferred from production's reflection constants.
FIELDS = {'highlightPos': 'f_94102_', 'frame': 'f_94095_', 'displayPos': 'f_94100_',
          'isEditable': 'f_94098_', 'hint': 'f_256828_', 'formatter': 'f_94091_'}
EDITBOX = '''package net.minecraft.client.gui.components;
import net.minecraft.client.gui.*;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import java.util.function.*;
public class EditBox {
    protected int width,height;
    private int x,y,cursor,max=32;
    private String value="";
    private boolean focused;
    private Consumer<String> responder;
    private int highlightPos,frame,displayPos;
    private boolean isEditable=true;
    private Component hint;
    private BiFunction<String,Integer,FormattedCharSequence> formatter=(s,i)->FormattedCharSequence.forward(s,Style.EMPTY);
    public EditBox(Font font,int x,int y,int w,int h,Component label) { this.x=x; this.y=y; width=w; height=h; }
    public int getX(){return x;} public int getY(){return y;}
    public boolean isVisible(){return true;} public boolean isFocused(){return focused;}
    public void setFocused(boolean value){focused=value;}
    public String getValue(){return value;}
    public int getCursorPosition(){return cursor;}
    public int getInnerWidth(){return width-8;}
    public void setBordered(boolean value){} public void setTextColor(int value){} public void setTextColorUneditable(int value){}
    public void setMaxLength(int max){this.max=max;}
    public void setValue(String value){this.value=value.substring(0,Math.min(max,value.length())); cursor=this.value.length(); highlightPos=cursor; if(responder!=null)responder.accept(this.value);}
    public void setCursorPosition(int value){cursor=Math.max(0,Math.min(this.value.length(),value));}
    public void setHighlightPos(int value){highlightPos=Math.max(0,Math.min(this.value.length(),value));}
    public void setResponder(Consumer<String> responder){this.responder=responder;}
    public void setFormatter(BiFunction<String,Integer,FormattedCharSequence> formatter){this.formatter=formatter;}
    public void setEditable(boolean value){isEditable=value;}
    public void setHint(Component value){hint=value;}
    public void renderWidget(GuiGraphics g,int x,int y,float tick){}
    public boolean charTyped(char c,int mods){return false;}
}
'''
STUBS = {
    'net/minecraft/network/chat/Component.java': '''package net.minecraft.network.chat;
public record Component(String text) { public static Component literal(String text){return new Component(text);} public String getString(){return text;} }''',
    'net/minecraft/network/chat/Style.java': 'package net.minecraft.network.chat; public class Style { public static final Style EMPTY=new Style(); }',
    'net/minecraft/util/FormattedCharSequence.java': '''package net.minecraft.util;
public record FormattedCharSequence(String text) {
    public static FormattedCharSequence forward(String text,net.minecraft.network.chat.Style style){return new FormattedCharSequence(text);}
}''',
    'net/minecraft/client/gui/Font.java': '''package net.minecraft.client.gui;
public class Font {
    public String plainSubstrByWidth(String text,int width){return text.substring(0,Math.max(0,Math.min(text.length(),width)));}
    public int width(String text){return text.length();}
}''',
    'net/minecraft/client/Minecraft.java': '''package net.minecraft.client;
public class Minecraft { public net.minecraft.client.gui.Font font=new net.minecraft.client.gui.Font();
    private static Minecraft instance=new Minecraft(); public static Minecraft getInstance(){return instance;} }''',
    'net/minecraft/client/gui/GuiGraphics.java': '''package net.minecraft.client.gui;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
public class GuiGraphics {
    public List<String> texts=new ArrayList<>(); public List<Integer> colors=new ArrayList<>(); public int fills;
    public void fill(int x,int y,int xx,int yy,int color){fills++;}
    public int drawString(Font font,FormattedCharSequence text,int x,int y,int color,boolean shadow){texts.add(text.text()); colors.add(color); return x+text.text().length();}
    public int drawString(Font font,Component text,int x,int y,int color,boolean shadow){texts.add(text.getString()); colors.add(color); return x+text.getString().length();}
}''',
    PREFIX+'client/render/DiaryTheme.java': '''package com.eternalcraft.gripeverde.client.render;
public class DiaryTheme { public static final int INK=1,SEPIA_MID=2,PAPER_STAINED=3,SEPIA_DARK=4,REDCOAT_DARK=5,PAPER_DARK=6;
    public static int withAlpha(int color,int alpha){return color;} }''',
    PREFIX+'client/render/PaperTexture.java': '''package com.eternalcraft.gripeverde.client.render;
public class PaperTexture { public static void drawWornBorder(net.minecraft.client.gui.GuiGraphics g,int x,int y,int w,int h,int color,int seed){} }''',
    PREFIX+'client/audio/UiSounds.java': 'package com.eternalcraft.gripeverde.client.audio; public class UiSounds { public static void type(){} }',
}

HARNESS = r'''import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import com.eternalcraft.gripeverde.client.widgets.DiaryTextField;
public class TextFieldHarness {
    static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    static DiaryTextField field(){return new DiaryTextField(new Font(),10,20,100,20,Component.literal("test"));}
    static FormattedCharSequence format(DiaryTextField f,String text,int offset)throws Exception{
        Method m=DiaryTextField.class.getDeclaredMethod("formatSafe",String.class,int.class); m.setAccessible(true);
        return (FormattedCharSequence)m.invoke(f,text,offset);
    }
    static void backing(DiaryTextField f,String name,Object value)throws Exception{
        if(namespace.equals("srg")) name=Map.of("frame","f_94095_","displayPos","f_94100_").get(name);
        Field b=EditBox.class.getDeclaredField(name); b.setAccessible(true); b.set(f,value);
    }
    static String namespace;
    public static void main(String[] args)throws Exception{
        namespace=args[1]; DiaryTextField f=field(); String test=args[0];
        switch(test){
            case "reflection_fields" -> {
                Map<String,String> fields=Map.of("FIELD_HIGHLIGHT","highlightPos","FIELD_FRAME","frame","FIELD_DISPLAY_POS","displayPos",
                    "FIELD_IS_EDITABLE","isEditable","FIELD_HINT","hint","FIELD_FORMATTER","formatter");
                Map<String,String> srg=Map.of("highlightPos","f_94102_","frame","f_94095_","displayPos","f_94100_",
                    "isEditable","f_94098_","hint","f_256828_","formatter","f_94091_");
                for(var entry:fields.entrySet()){
                    Field handle=DiaryTextField.class.getDeclaredField(entry.getKey()); handle.setAccessible(true);
                    Field actual=(Field)handle.get(null);
                    String expected=namespace.equals("srg")?srg.get(entry.getValue()):entry.getValue();
                    check(actual!=null && actual.getName().equals(expected),"wrong reflection binding "+entry.getKey());
                }
            }
            case "selection_forward", "selection_reverse", "selection_collapsed" -> {
                f.setValue("abcdef"); int cursor=test.equals("selection_reverse")?5:2;
                int highlight=test.equals("selection_collapsed")?cursor:(cursor==2?5:2);
                f.setCursorPosition(cursor); f.setHighlightPos(highlight);
                var state=f.snapshotEditingState(); check(state.cursor()==cursor && state.highlight()==highlight,"snapshot lost selection orientation");
                DiaryTextField next=field(); next.restoreEditingState(state);
                check(next.snapshotEditingState().equals(state),"selection restore differs");
            }
            case "selection_unicode" -> {
                f.setValue("a\uD83D\uDE00bc"); f.setCursorPosition(3); f.setHighlightPos(1);
                DiaryTextField next=field(); next.restoreEditingState(f.snapshotEditingState());
                check(next.snapshotEditingState().equals(f.snapshotEditingState()),"unicode selection lost");
            }
            case "restore_clamps" -> {
                f.setMaxLength(3); f.restoreEditingState(new DiaryTextField.EditingState("abcdef",6,5));
                check(f.snapshotEditingState().equals(new DiaryTextField.EditingState("abc",3,3)),"restore bypassed widget clamps");
            }
            case "restore_null" -> {
                f.setValue("kept"); var state=f.snapshotEditingState(); f.restoreEditingState(null);
                check(f.snapshotEditingState().equals(state),"null snapshot changed field");
            }
            case "snapshot_no_callback" -> {
                int[] calls={0}; f.setResponder(v->calls[0]++); f.setValue("abcdef"); f.snapshotEditingState();
                check(calls[0]==1,"snapshot fired responder"); f.restoreEditingState(new DiaryTextField.EditingState("abc",1,2));
                check(calls[0]==2 && f.getValue().equals("abc"),"restore failed to notify validation");
            }
            case "formatter_replaced" -> {
                var a=new FormattedCharSequence("old"); var b=new FormattedCharSequence("new");
                f.setFormatter((s,i)->a); check(format(f,"same",0)==a,"old formatter ignored");
                f.setFormatter((s,i)->b); check(format(f,"same",0)==b,"stale formatter cache");
            }
            case "formatter_stateful" -> {
                int[] calls={0}; f.setFormatter((s,i)->new FormattedCharSequence("state"+(++calls[0])));
                check(format(f,"same",0).text().equals("state1"),"first formatter call failed");
                check(format(f,"same",0).text().equals("state2"),"external formatter state was cached");
            }
            case "formatter_null", "formatter_throws" -> {
                f.setFormatter((s,i)->{if(test.equals("formatter_throws"))throw new IllegalStateException("bad formatter");return null;});
                check(format(f,"fallback",2).text().equals("fallback"),"formatter failure did not return plain sequence");
            }
            case "formatter_recovery" -> {
                boolean[] fail={true}; var ok=new FormattedCharSequence("ok");
                f.setFormatter((s,i)->{if(fail[0])throw new IllegalStateException("temporary");return ok;});
                check(format(f,"text",0).text().equals("text"),"failure lost plain text"); fail[0]=false;
                check(format(f,"text",0)==ok,"cached fallback prevented formatter recovery");
            }
            case "formatter_offsets" -> {
                f.setFormatter((s,i)->new FormattedCharSequence(s+":"+i));
                check(format(f,"tail",7).text().equals("tail:7"),"formatter offset changed");
            }
            case "render_hint" -> {
                f.setHint(Component.literal("hint")); GuiGraphics g=new GuiGraphics(); f.renderWidget(g,0,0,0);
                check(g.texts.equals(List.of("hint")),"hint reflection failed");
            }
            case "render_readonly" -> {
                f.setValue("abc"); f.setEditable(false); GuiGraphics g=new GuiGraphics(); f.renderWidget(g,0,0,0);
                check(g.colors.equals(List.of(2)),"editable reflection picked wrong ink");
            }
            case "render_scroll" -> {
                f.setValue("abcdef"); backing(f,"displayPos",2); GuiGraphics g=new GuiGraphics(); f.renderWidget(g,0,0,0);
                check(g.texts.equals(List.of("cdef")),"displayPos did not scroll visible text");
            }
            case "render_blink" -> {
                f.setFocused(true); GuiGraphics on=new GuiGraphics(); f.renderWidget(on,0,0,0);
                backing(f,"frame",6); GuiGraphics off=new GuiGraphics(); f.renderWidget(off,0,0,0);
                check(on.fills==off.fills+1,"cursor frame read wrong backing field");
            }
            default -> throw new AssertionError(test);
        }
    }
}
'''
FORM_HARNESS = r'''import com.eternalcraft.gripeverde.client.widgets.DiaryTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
public class ServerFormHarness {
    static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    static DiaryTextField field(String value){DiaryTextField f=new DiaryTextField(new Font(),0,0,100,20,Component.literal("form"));f.setMaxLength(128);f.setValue(value);return f;}
    static abstract class Parent {
        Object focused; int parentKeys,lastMods;
        abstract void rebuild();
        public void resize(Minecraft mc,int w,int h){rebuild();}
        public boolean keyPressed(int key,int scan,int mods){parentKeys++;lastMods=mods;return key==258;}
        Object getFocused(){return focused;}
        void setFocused(Object value){focused=value;}
    }
    static class Button {boolean active=true;}
    static class Editor extends Parent {
        DiaryTextField nameEdit,ipEdit; Button doneButton=new Button(); int submits;
        void rebuild(){nameEdit=field("saved name");ipEdit=field("saved ip");setFocused(nameEdit);}
        void onDone(){submits++;}
        __EDITOR_RESIZE__
        __EDITOR_KEYS__
    }
    static class Direct extends Parent {
        DiaryTextField ipEdit;
        void rebuild(){ipEdit=field("last ip");setFocused(ipEdit);}
        __DIRECT_RESIZE__
    }
    public static void main(String[] args){
        String test=args[0]; Minecraft mc=Minecraft.getInstance();
        if(test.equals("direct_uninitialized")){
            Direct screen=new Direct();screen.resize(mc,800,600);
            check(screen.ipEdit.getValue().equals("last ip"),"uninitialized resize overwrote initial address");return;
        }
        if(test.equals("editor_uninitialized")){
            Editor screen=new Editor();screen.resize(mc,800,600);
            check(screen.nameEdit.getValue().equals("saved name")&&screen.ipEdit.getValue().equals("saved ip"),"uninitialized resize overwrote initial values");return;
        }
        if(test.startsWith("direct")){
            Direct screen=new Direct();screen.rebuild(); screen.ipEdit.setValue("unsubmitted:25565");
            screen.ipEdit.setCursorPosition(4);screen.ipEdit.setHighlightPos(12); var expected=screen.ipEdit.snapshotEditingState();
            screen.resize(mc,200,120);screen.resize(mc,800,600);
            check(screen.ipEdit.snapshotEditingState().equals(expected),"direct resize lost draft/cursor/selection");return;
        }
        Editor screen=new Editor();screen.rebuild();
        switch(test){
            case "editor_resize_name", "editor_resize_ip" -> {
                screen.nameEdit.setValue("unsaved name");screen.nameEdit.setCursorPosition(2);screen.nameEdit.setHighlightPos(8);
                screen.ipEdit.setValue("unfinished:");screen.ipEdit.setCursorPosition(7);screen.ipEdit.setHighlightPos(3);
                var name=screen.nameEdit.snapshotEditingState();var ip=screen.ipEdit.snapshotEditingState();
                boolean focusIp=test.equals("editor_resize_ip");screen.setFocused(focusIp?screen.ipEdit:screen.nameEdit);
                screen.resize(mc,200,120);screen.resize(mc,800,600);
                check(screen.nameEdit.snapshotEditingState().equals(name)&&screen.ipEdit.snapshotEditingState().equals(ip),"editor resize lost draft/cursor/selection");
                check(screen.getFocused()==(focusIp?screen.ipEdit:screen.nameEdit),"editor resize changed field focus");
                check(screen.submits==0,"resize submitted draft");
            }
            case "tab_name", "tab_ip", "shift_tab_name", "shift_tab_ip" -> {
                screen.setFocused(test.endsWith("ip")?screen.ipEdit:screen.nameEdit);int mods=test.startsWith("shift")?1:0;
                check(screen.keyPressed(258,0,mods)&&screen.parentKeys==1&&screen.lastMods==mods,"Tab did not delegate to vanilla traversal");
                check(screen.submits==0,"Tab submitted form");
            }
            case "enter_name" -> {
                screen.keyPressed(257,0,0);check(screen.getFocused()==screen.ipEdit&&screen.submits==0,"Enter on name did not advance");
            }
            case "enter_ip", "keypad_enter_ip", "enter_invalid" -> {
                screen.setFocused(screen.ipEdit);screen.doneButton.active=!test.equals("enter_invalid");
                screen.keyPressed(test.equals("keypad_enter_ip")?335:257,0,0);
                check(screen.submits==(screen.doneButton.active?1:0),"Enter validation/submit changed");
            }
            default -> throw new AssertionError(test);
        }
    }
}
'''
CASES = ('reflection_fields', 'selection_forward', 'selection_reverse', 'selection_collapsed', 'selection_unicode',
         'restore_clamps', 'restore_null', 'snapshot_no_callback', 'formatter_replaced', 'formatter_stateful',
         'formatter_null', 'formatter_throws', 'formatter_recovery', 'formatter_offsets', 'render_hint',
         'render_readonly', 'render_scroll', 'render_blink')
FORM_CASES = ('direct_resize', 'direct_uninitialized', 'editor_uninitialized', 'editor_resize_name', 'editor_resize_ip', 'tab_name', 'tab_ip',
              'shift_tab_name', 'shift_tab_ip', 'enter_name', 'enter_ip', 'keypad_enter_ip', 'enter_invalid')


class Wiring(unittest.TestCase):
    def test_explicit_srg_bindings(self):
        for official, srg in FIELDS.items():
            self.assertRegex(SOURCE, '"'+official+'",\\s*"'+srg+'"')

    def test_custom_formatter_not_cached(self):
        self.assertNotIn('fmtSeqA', SOURCE)
        body = method(SOURCE, 'private FormattedCharSequence formatSafe(')
        self.assertIn('fn.apply(text, startPos)', body)
        self.assertIn('if (seq != null)', body)

    def test_resize_restores_after_rebuild(self):
        for source in SCREENS.values():
            body = method(source, 'public void resize(')
            self.assertLess(body.index('snapshotEditingState('), body.index('super.resize('))
            self.assertLess(body.index('super.resize('), body.index('restoreEditingState('))

    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_text_fields.py --require-java', (ROOT / path).read_text())


class Executable(unittest.TestCase):
    def test_widget_namespaces_and_form_methods(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        for namespace in ('official', 'srg'):
            with tempfile.TemporaryDirectory() as tmp:
                work = Path(tmp)
                sources = dict(STUBS)
                parent = EDITBOX
                if namespace == 'srg':
                    import re
                    for official, srg in FIELDS.items():
                        parent = re.sub(r'\b'+official+r'\b', srg, parent)
                sources['net/minecraft/client/gui/components/EditBox.java'] = parent
                sources[PREFIX+'client/widgets/DiaryTextField.java'] = SOURCE
                sources['TextFieldHarness.java'] = HARNESS
                forms = FORM_HARNESS.replace('__EDITOR_RESIZE__', method(SCREENS['DiaryEditServerScreen'], 'public void resize('))
                forms = forms.replace('__EDITOR_KEYS__', method(SCREENS['DiaryEditServerScreen'], 'public boolean keyPressed('))
                forms = forms.replace('__DIRECT_RESIZE__', method(SCREENS['DiaryDirectJoinScreen'], 'public void resize('))
                sources['ServerFormHarness.java'] = forms
                paths = []
                for name, text in sources.items():
                    path = work/name
                    path.parent.mkdir(parents=True, exist_ok=True)
                    path.write_text(text)
                    paths.append(str(path))
                subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, *paths], check=True, timeout=60)
                for case in CASES:
                    with self.subTest(namespace=namespace, case=case):
                        subprocess.run(['java', '-cp', tmp, 'TextFieldHarness', case, namespace], check=True, timeout=15)
                for case in FORM_CASES:
                    with self.subTest(namespace=namespace, form=case):
                        subprocess.run(['java', '-cp', tmp, 'ServerFormHarness', case], check=True, timeout=15)
        print(f'Text fields: {2*(len(CASES)+len(FORM_CASES))} executable scenarios passed (official + SRG doubles)')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
