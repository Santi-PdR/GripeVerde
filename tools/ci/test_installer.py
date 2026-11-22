#!/usr/bin/env python3
"""Static safety checks for the Windows latest-release installer.

The runner is Linux and has no PowerShell host, so these tests enforce the
important transaction order and release-identity checks in the script source.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
SCRIPT = (ROOT / "tools/install-latest.ps1").read_text(encoding="utf-8")


class InstallerSafetyTests(unittest.TestCase):
    def pos(self, token: str) -> int:
        found = SCRIPT.find(token)
        self.assertGreaterEqual(found, 0, f"installer is missing: {token}")
        return found

    def test_target_path_is_the_requested_instance(self):
        self.assertIn(
            "$mods = 'D:\\Minecraft\\SKLauncher\\sklauncher-data-v4\\instances\\test-1\\mods'",
            SCRIPT,
        )

    def test_public_release_api_is_tried_before_credential_lookup(self):
        public_request = self.pos("$release = Invoke-RestMethod -Uri $api -Headers $headers")
        token_lookup = self.pos("$token = Get-GitHubToken")
        self.assertLess(public_request, token_lookup)

    def test_release_tag_filename_and_digest_are_validated(self):
        name_check = self.pos("$nameMatch = [regex]::Match")
        tag_check = self.pos('$release.tag_name -ne "v$version"')
        digest_check = self.pos("$expectedHash -notmatch '^[0-9a-fA-F]{64}$'")
        install = self.pos("Move-Item -LiteralPath $temp -Destination $dest")
        self.assertLess(name_check, install)
        self.assertLess(tag_check, install)
        self.assertLess(digest_check, install)

    def test_size_hash_and_internal_jar_identity_precede_replace(self):
        size_check = self.pos("if ($download.Length -ne $expectedSize)")
        hash_check = self.pos("if ($hash -ne $expectedHash.ToLowerInvariant())")
        manifest_check = self.pos("Implementation-Version:")
        mod_id_check = self.pos("$modIdPresent = [regex]::IsMatch")
        install = self.pos("Move-Item -LiteralPath $temp -Destination $dest")
        for check in (size_check, hash_check, manifest_check, mod_id_check):
            self.assertLess(check, install)

    def test_new_verified_jar_is_moved_before_old_jars_are_deleted(self):
        install = self.pos("Move-Item -LiteralPath $temp -Destination $dest")
        installed_hash_check = self.pos("if ($installedHash -ne $expectedHash.ToLowerInvariant())")
        list_old = self.pos("Get-ChildItem -LiteralPath $mods -File -Filter 'gripeverde-*.jar'")
        remove_old = self.pos("Remove-Item -LiteralPath $jar.FullName")
        self.assertLess(install, installed_hash_check)
        self.assertLess(installed_hash_check, list_old)
        self.assertLess(list_old, remove_old)
        self.assertNotIn("Copy-Item", SCRIPT)

    def test_cleanup_of_legacy_backups_only_happens_after_successful_install(self):
        install = self.pos("Move-Item -LiteralPath $temp -Destination $dest")
        installed_hash_check = self.pos("if ($installedHash -ne $expectedHash.ToLowerInvariant())")
        cleanup = self.pos("Remove-Item -LiteralPath $legacyBackupDir -Recurse -Force")
        self.assertLess(install, installed_hash_check)
        self.assertLess(installed_hash_check, cleanup)
        self.assertIn("gripeverde-jar-backups", SCRIPT)

    def test_temp_file_is_removed_in_finally(self):
        finally_block = SCRIPT.rfind("finally {")
        temp_cleanup = SCRIPT.rfind("Remove-Item -LiteralPath $temp -Force")
        self.assertGreaterEqual(finally_block, 0)
        self.assertLess(finally_block, temp_cleanup)


if __name__ == "__main__":
    unittest.main(verbosity=2)
