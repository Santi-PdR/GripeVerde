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

    def test_pack_text_geometry_regressions(self):
        for old, new in (
            ('int x1 = left + 33;', 'int x1 = left + 30;'),
            ('int textX = left + 34;', 'int textX = left + 39;'),
            ('int x2 = right - 8;', 'int x2 = right - 26;'),
            ('int textW = x2 - textX - 1;', 'int textW = x2 - textX - 10;'),
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

    def test_pack_mouse_override_is_rejected(self):
        self.assert_rejected(PACK, 'public class DiaryPackSelectionScreen extends PackSelectionScreen {',
                             'public class DiaryPackSelectionScreen extends PackSelectionScreen {\n'
                             '    public boolean mouseClicked(double x, double y, int button) { return false; }')


if __name__ == '__main__':
    unittest.main(verbosity=2)
