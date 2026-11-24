#!/usr/bin/env python3
"""Run real button mouseClicked methods with a minimal parent/policy double.

These checks concern acceptance and animation state, not rendering/native input.
The Gradle slider regression also tests rejected clicks on the real buttons.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method

ROOT = Path(__file__).resolve().parents[1]
SOURCES = {name: (ROOT / f'src/main/java/com/eternalcraft/gripeverde/client/widgets/{name}.java').read_text()
           for name in ('DiaryButton', 'GreenFluButton')}
HARNESS = '''public class ButtonInputHarness {
    static class MotionPolicy {static boolean reduced;static boolean isReduced(){return reduced;}}
    static class Parent {
        boolean active=true,visible=true;
        int acceptedButton,actions;
        boolean isValidClickButton(int button){return button==acceptedButton;}
        boolean clicked(double x,double y){return x>=0&&x<100&&y>=0&&y<20;}
        public boolean mouseClicked(double x,double y,int button){
            if(!active||!visible||!isValidClickButton(button)||!clicked(x,y))return false;
            actions++;return true;
        }
    }
    static class Probe extends Parent {
        private int __PRESS__,sealTicks;
        private static final int SEAL_TICKS=9;
        __METHOD__
    }
    static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    public static void main(String[] args){
        Probe p=new Probe();String test=args[0];int button=0;double x=50;
        boolean accepted=true,animated=true;
        switch(test){
            case "primary" -> {}
            case "secondary" -> {button=1;accepted=false;animated=false;}
            case "middle" -> {button=2;accepted=false;animated=false;}
            case "negative" -> {button=-1;accepted=false;animated=false;}
            case "outside" -> {x=100;accepted=false;animated=false;}
            case "inactive" -> {p.active=false;accepted=false;animated=false;}
            case "hidden" -> {p.visible=false;accepted=false;animated=false;}
            case "reduced" -> {MotionPolicy.reduced=true;animated=false;}
            case "remapped_secondary" -> {p.acceptedButton=1;button=1;}
            case "remapped_rejects_primary" -> {p.acceptedButton=1;accepted=false;animated=false;}
            default -> throw new AssertionError(test);
        }
        boolean result=p.mouseClicked(x,10,button);
        check(result==accepted,"incorrect acceptance: "+test);
        check(p.actions==(accepted?1:0),"incorrect callback count: "+test);
        check(p.__PRESS__==(animated?7:0)&&p.sealTicks==(animated?9:0),"rejected/silent click animated: "+test);
    }
}
'''
CASES = ('primary', 'secondary', 'middle', 'negative', 'outside', 'inactive', 'hidden', 'reduced',
         'remapped_secondary', 'remapped_rejects_primary')


class Wiring(unittest.TestCase):
    def test_buttons_use_their_own_acceptance_policy(self):
        for source in SOURCES.values():
            self.assertIn('isValidClickButton(btn)', method(source, 'public boolean mouseClicked('))

    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_button_input.py --require-java', (ROOT / path).read_text())


class Executable(unittest.TestCase):
    def test_pointer_animation_guards(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        for name, source in SOURCES.items():
            with tempfile.TemporaryDirectory() as tmp:
                work=Path(tmp)
                press='pressAnimTicks' if name=='DiaryButton' else 'pressTicks'
                text=HARNESS.replace('__PRESS__', press).replace('__METHOD__', method(source, 'public boolean mouseClicked('))
                path=work/'ButtonInputHarness.java'
                path.write_text(text)
                subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, str(path)], check=True, timeout=60)
                for case in CASES:
                    with self.subTest(button=name, case=case):
                        subprocess.run(['java', '-cp', tmp, 'ButtonInputHarness', case], check=True, timeout=15)
        print(f'Button input: {len(SOURCES)*len(CASES)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
