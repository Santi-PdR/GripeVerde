#!/usr/bin/env python3
"""Regression tests for release publishing event and version gates."""
from pathlib import Path
import sys
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parent))
from release_gate import release_decision


class ReleaseGateTests(unittest.TestCase):
    version = "0.394.0"
    tag = "v0.394.0"

    def test_pull_request_never_publishes_even_with_matching_marker(self):
        self.assertEqual(
            release_decision("pull_request", "true", self.tag, self.tag, self.version),
            (False, ""),
        )

    def test_manual_dispatch_requires_publish_checkbox(self):
        self.assertEqual(
            release_decision("workflow_dispatch", "false", self.tag, self.tag, self.version),
            (False, ""),
        )

    def test_manual_dispatch_defaults_empty_tag_to_build_version(self):
        self.assertEqual(
            release_decision("workflow_dispatch", "true", "", "stale-marker", self.version),
            (True, self.tag),
        )

    def test_manual_dispatch_accepts_matching_explicit_tag(self):
        self.assertEqual(
            release_decision("workflow_dispatch", "true", self.tag, "", self.version),
            (True, self.tag),
        )

    def test_manual_dispatch_rejects_other_version(self):
        self.assertEqual(
            release_decision("workflow_dispatch", "true", "v0.395.0", "", self.version),
            (False, ""),
        )

    def test_push_uses_release_marker(self):
        self.assertEqual(
            release_decision("push", "false", "", self.tag, self.version),
            (True, self.tag),
        )

    def test_push_skips_when_marker_does_not_match_version(self):
        self.assertEqual(
            release_decision("push", "false", "", "v0.393.0", self.version),
            (False, ""),
        )


if __name__ == "__main__":
    unittest.main(verbosity=2)
