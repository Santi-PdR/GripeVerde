#!/usr/bin/env python3
"""Resolve whether the current GitHub Actions event may publish a release."""
from __future__ import annotations

import os
import re
from pathlib import Path
from typing import Tuple


def release_decision(
    event_name: str,
    publish_release_input: str,
    release_tag_input: str,
    marker: str,
    version: str,
) -> Tuple[bool, str]:
    """Return (publish, tag), enforcing event and version gates."""
    expected_tag = f"v{version}"

    if event_name == "pull_request":
        return False, ""

    if event_name == "workflow_dispatch":
        if publish_release_input.strip().lower() != "true":
            return False, ""
        selected_tag = release_tag_input.strip() or expected_tag
    else:
        selected_tag = marker.strip()

    if selected_tag != expected_tag:
        return False, ""
    return True, selected_tag


def main() -> int:
    build_gradle = Path(os.environ.get("BUILD_GRADLE", "build.gradle"))
    marker_file = Path(os.environ.get("RELEASE_MARKER_FILE", "release-tag.txt"))
    output_file = os.environ.get("GITHUB_OUTPUT")
    event_name = os.environ.get("EVENT_NAME", "")

    if not event_name:
        raise SystemExit("EVENT_NAME is required")
    if not output_file:
        raise SystemExit("GITHUB_OUTPUT is required")

    build_text = build_gradle.read_text(encoding="utf-8")
    version_match = re.search(r"^\s*version\s*=\s*'([^']+)'", build_text, re.MULTILINE)
    if not version_match:
        raise SystemExit(f"Could not read version from {build_gradle}")
    version = version_match.group(1)

    marker = ""
    if marker_file.is_file():
        lines = marker_file.read_text(encoding="utf-8").splitlines()
        marker = lines[0].strip() if lines else ""

    publish, tag = release_decision(
        event_name=event_name,
        publish_release_input=os.environ.get("PUBLISH_RELEASE_INPUT", "false"),
        release_tag_input=os.environ.get("RELEASE_TAG_INPUT", ""),
        marker=marker,
        version=version,
    )

    with open(output_file, "a", encoding="utf-8") as output:
        output.write(f"publish={str(publish).lower()}\n")
        if publish:
            output.write(f"tag={tag}\n")

    print(f"event={event_name} marker={tag or 'disabled'} version={version}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
