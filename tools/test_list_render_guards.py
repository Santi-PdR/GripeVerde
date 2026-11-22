#!/usr/bin/env python3
"""Regression tests for the static pack/list rendering guards (not client tests).

Run: python3 tools/test_list_render_guards.py
These mutate Java source in memory; they neither compile nor launch Minecraft.
"""
from pathlib import Path
import unittest

import lint_java

ROOT = Path(__file__).resolve().parents[1]
PACK = 'src/main/java/com/eternalcraft/gripeverde/client/screens/DiaryPackSelectionScreen.java'
CHROME = 'src/main/java/com/eternalcraft/gripeverde/client/render/DiaryChrome.java'


class ListRenderGuardTests(unittest.TestCase):
    def check_source(self, path, source):
        clean = lint_java.strip_noise(source)
        check = (lint_java.check_pack_selection_single_render if path == PACK
                 else lint_java.check_visible_list_row_iteration)
        return check(path, clean)

    def assert_rejected(self, path, old, new):
        source = (ROOT / path).read_text(encoding='utf-8')
        self.assertEqual(source.count(old), 1, 'Mutation must target exactly one location')
        self.assertFalse(self.check_source(path, source))
        self.assertTrue(self.check_source(path, source.replace(old, new)))

    def test_current_sources_pass(self):
        for path in (PACK, CHROME):
            with self.subTest(path=path):
                self.assertFalse(self.check_source(path, (ROOT / path).read_text(encoding='utf-8')))

    def test_viewport_arithmetic_model(self):
        # Independent brute-force oracle for the index optimization. This is a
        # Python model of vanilla's geometry, NOT execution of the Java helper.
        cases = 0
        for count in (0, 1, 2, 64, 65, 128, 1000):
            for step in (20, 25, 36, 80):
                for header in (0, 13, 80):
                    for top in (0, 32, 100):
                        for height in (24, 120, 300):
                            bottom = top + height
                            max_scroll = max(0, count * step + header - height + 4)
                            for requested in (0, .5, 35.99, 36, 64 * step + .5, max_scroll):
                                scroll = min(requested, max_scroll)
                                first_top = top + 4 - int(scroll) + header
                                first = max(0, min(count, (top - first_top) // step - 1))
                                expected = [i for i in range(count)
                                            if first_top + i * step <= bottom
                                            and first_top + (i + 1) * step - 4 >= top]
                                actual = []
                                for i in range(first, count):
                                    row_top = first_top + i * step
                                    if row_top > bottom:
                                        break
                                    if row_top + step - 4 >= top:
                                        actual.append(i)
                                self.assertEqual(expected, actual,
                                                 (count, step, header, top, height, scroll))
                                cases += 1
        self.assertEqual(cases, 4536)

    def test_guard_suite_runs_in_ci_and_template(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            with self.subTest(path=path):
                workflow = (ROOT / path).read_text(encoding='utf-8')
                self.assertIn('run: python3 tools/test_list_render_guards.py', workflow)

    def test_pack_text_geometry_regressions(self):
        for old, new in (
            ('int x1 = left + 33;', 'int x1 = left + 30;'),
            ('int textX = left + 34;', 'int textX = left + 39;'),
            ('int x2 = right - 8;', 'int x2 = right - 26;'),
            ('int textW = x2 - textX - 1;', 'int textW = x2 - textX - 10;'),
        ):
            with self.subTest(old=old):
                self.assert_rejected(PACK, old, new)

    def test_partial_pack_rows_stay_anchored(self):
        for old, new in (
            ('textX, rowTop + 4,', 'textX, drawTop + 4,'),
            ('textX, rowTop + 15,', 'textX, drawTop + 15,'),
            ('rowBottom - rowTop >= 24', 'drawBottom - drawTop >= 24'),
            ('if (drawBottom <= drawTop) continue;',
             'if (drawBottom - drawTop < 12) continue;'),
            ('g.fill(x1 + 3, rowBottom - 1, x2 - 3, rowBottom,',
             'g.fill(x1 + 3, drawBottom - 1, x2 - 3, drawBottom,'),
        ):
            with self.subTest(old=old):
                self.assert_rejected(PACK, old, new)

    def test_extended_description_is_required(self):
        self.assert_rejected(PACK, 'entry.getExtendedDescription()', 'entry.getDescription()')

    def test_pack_header_height_is_required(self):
        self.assert_rejected(PACK, 'PACK_LIST_HEADER_HEIGHT = 13', 'PACK_LIST_HEADER_HEIGHT = 0')

    def test_pack_row_limit_is_rejected(self):
        self.assert_rejected(PACK, 'i < count; i++', 'i < count && i < 64; i++')

    def test_shared_row_limit_is_rejected(self):
        self.assert_rejected(CHROME, 'i < count; i++', 'i < count && i < 64; i++')

    def test_shared_viewport_exit_is_required(self):
        self.assert_rejected(CHROME, 'if (rowTop > bottom) break;',
                             'if (rowTop > bottom) continue;')

    def test_shared_header_offset_is_required(self):
        self.assert_rejected(CHROME, 'top + 4 - (int) scroll + i * step + headerHeight;',
                             'top + 4 - (int) scroll + i * step;')

    def test_compatibility_warning_conditions_are_preserved(self):
        for old, new in (
            ('entry.getCompatibility().isCompatible()) return false;',
             'entry.getCompatibility().isCompatible()) return true;'),
            ('entry.isFixedPosition() && entry.isRequired()) return false;',
             'entry.isFixedPosition() || entry.isRequired()) return false;'),
            ('list.isMouseOver(mouseX, mouseY)', 'true'),
            ('row.isMouseOver(mouseX, mouseY)', 'false'),
            ('this.minecraft.options.touchscreen().get() || hovered', 'hovered'),
            ('list.getSelected() == child && list.isFocused()', 'list.getSelected() == child'),
        ):
            with self.subTest(old=old):
                self.assert_rejected(PACK, old, new)

    def test_compatibility_warning_is_repainted(self):
        for old, new in (
            ('showPackCompatibilityWarning(list, child, entry, mouseX, mouseY)', 'false'),
            ('compatibilityDescription = entry.getCompatibility().getDescription();',
             'compatibilityDescription = null;'),
            ('MultiLineLabel.create(this.font, compatibilityDescription, textW, 2)',
             'MultiLineLabel.create(this.font, compatibilityDescription, textW, 1)'),
            ('.renderLeftAligned(g, textX, rowTop + 12, 10,',
             '.renderLeftAligned(g, textX, drawTop + 12, 10,'),
        ):
            with self.subTest(old=old):
                self.assert_rejected(PACK, old, new)

    def test_pack_mouse_override_is_rejected(self):
        self.assert_rejected(PACK, 'public class DiaryPackSelectionScreen extends PackSelectionScreen {',
                             'public class DiaryPackSelectionScreen extends PackSelectionScreen {\n'
                             '    public boolean mouseClicked(double x, double y, int button) { return false; }')


if __name__ == '__main__':
    unittest.main(verbosity=2)
