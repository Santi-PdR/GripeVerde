#!/usr/bin/env python3
"""Shell runner contract with fake executables; no nested real checks/builds."""
from pathlib import Path
import os
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
SCRIPT = ROOT / 'tools/check-local.sh'


class ShellRunner(unittest.TestCase):
    def run_script(self, args=(), java=False, javac=False, fail=''):
        with tempfile.TemporaryDirectory() as tmp:
            work = Path(tmp)
            log = work/'calls'
            for name in ('python3', 'git', 'bash')+tuple(n for n, enabled in (('java', java), ('javac', javac)) if enabled):
                path = work/name
                path.write_text('#!/bin/sh\nprintf "%s\\n" "'+name+' $*" >> "$CALLS"\n'
                                'if [ "${FAIL_STEP:-}" = "$1" ]; then exit 7; fi\n')
                path.chmod(0o755)
            env = dict(os.environ, PATH=tmp, CALLS=str(log), FAIL_STEP=fail)
            result = subprocess.run(['/bin/bash', str(SCRIPT), *args], cwd=tmp, env=env,
                                    capture_output=True, text=True, timeout=15)
            return result, log.read_text().splitlines() if log.exists() else []

    def test_syntax(self):
        subprocess.run(['/bin/bash', '-n', str(SCRIPT)], check=True)

    def test_help_has_no_side_effects(self):
        result, calls = self.run_script(('--help',))
        self.assertEqual(result.returncode, 0)
        self.assertIn('--require-java', result.stdout)
        self.assertFalse(calls)

    def test_unknown_option_fails(self):
        result, calls = self.run_script(('--wrong',))
        self.assertEqual(result.returncode, 2)
        self.assertFalse(calls)

    def test_no_java_is_explicitly_partial(self):
        result, calls = self.run_script()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn('parcial', result.stderr)
        self.assertEqual(len(calls), 6)
        self.assertIn('python3 -m unittest discover -s tools -p test_*.py', calls)
        self.assertEqual(calls[-1], 'git diff --check')
        self.assertFalse(any(c.startswith('bash ') for c in calls))

    def test_require_java_fails_before_checks(self):
        for java, javac in ((False, False), (True, False), (False, True)):
            with self.subTest(java=java, javac=javac):
                result, calls = self.run_script(('--require-java',), java=java, javac=javac)
                self.assertNotEqual(result.returncode, 0)
                self.assertIn('Falta JDK', result.stderr)
                self.assertFalse(calls)

    def test_java_mode_runs_suites(self):
        result, calls = self.run_script(('--require-java',), java=True, javac=True)
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn('JDK detectado', result.stdout)
        self.assertEqual(len(calls), 6)

    def test_build_requires_java(self):
        result, calls = self.run_script(('--build',))
        self.assertNotEqual(result.returncode, 0)
        self.assertFalse(calls)

    def test_build_is_last_and_explicit(self):
        result, calls = self.run_script(('--build',), java=True, javac=True)
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(len(calls), 7)
        self.assertEqual(calls[-1], 'bash ./gradlew --no-daemon build')

    def test_failed_check_stops_and_does_not_build(self):
        result, calls = self.run_script(('--build',), java=True, javac=True, fail='tools/lint_java.py')
        self.assertEqual(result.returncode, 7)
        self.assertEqual(len(calls), 2)
        self.assertFalse(any(c.startswith('bash ') for c in calls))

    def test_failed_build_propagates(self):
        result, calls = self.run_script(('--build',), java=True, javac=True, fail='./gradlew')
        self.assertEqual(result.returncode, 7)
        self.assertIn('bash ./gradlew --no-daemon build', calls)
        self.assertNotIn('terminadas', result.stdout)


if __name__ == '__main__':
    unittest.main()
