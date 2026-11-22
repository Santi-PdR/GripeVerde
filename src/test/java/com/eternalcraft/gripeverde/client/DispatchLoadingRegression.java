package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.client.integration.DispatchLibrary;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;

/** Real library/Gson/filesystem tests; isolated config directories, no client. */
public final class DispatchLoadingRegression {
    private static int checks;

    private static void check(boolean condition, String reason) {
        checks++;
        if (!condition) throw new AssertionError(reason);
    }

    private static void resetField(String name, Object value) throws Exception {
        var field = DispatchLibrary.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    private static Path newConfig(Path game) throws Exception {
        FMLPaths.loadAbsolutePaths(game);
        // Simulate a fresh process for each input file. No reset/test API is
        // exposed in production. ensureLoaded initializes all slot arrays.
        resetField("loaded", false);
        resetField("pendingSave", false);
        resetField("needsOriginalBackup", false);
        resetField("openKey", GLFW.GLFW_KEY_RIGHT_ALT);
        resetField("importedFrom", "");
        return FMLPaths.CONFIGDIR.get();
    }

    private static void ownFields(Path root) throws Exception {
        Path config = newConfig(root.resolve("own"));
        Path own = config.resolve("gripeverde-dispatch.json");
        Path copy = config.resolve("copyl-messages.json");
        String source = """
            {"openKey":null,"importedFrom":{},"slots":[
              {"name":"First","message":"Keep first","key":65},
              {"name":null,"message":null,"key":null}, null, 42, [],
              {"name":{},"message":"Keep despite bad name","key":"bogus"},
              {"name":"Keep name","message":{},"key":66},
              {"name":"Last","message":"Keep last","key":67}]}
            """;
        Files.writeString(own, source);
        String obsolete = "{\"messages\":[\"Obsolete CopyL text\"]}";
        Files.writeString(copy, obsolete);
        check(DispatchLibrary.message(0).equals("Keep first"), "own data wins over CopyL");
        check(DispatchLibrary.openKey() == GLFW.GLFW_KEY_RIGHT_ALT, "null open key defaults");
        check(DispatchLibrary.importedFrom().isEmpty(), "malformed provenance defaults");
        check(DispatchLibrary.name(1).equals("Mensaje 2"), "null name defaults");
        check(DispatchLibrary.message(1).isEmpty(), "null message defaults");
        check(DispatchLibrary.key(1) == -1, "null binding defaults");
        for (int slot = 2; slot <= 4; slot++) {
            check(DispatchLibrary.message(slot).isEmpty(), "malformed row defaults without shifting");
            check(DispatchLibrary.key(slot) == -1, "malformed row unbound");
        }
        check(DispatchLibrary.name(5).equals("Mensaje 6"), "object name defaults");
        check(DispatchLibrary.message(5).equals("Keep despite bad name"), "other field in row preserved");
        check(DispatchLibrary.key(5) == -1, "invalid string binding defaults");
        check(DispatchLibrary.name(6).equals("Keep name"), "name with malformed message preserved");
        check(DispatchLibrary.message(6).isEmpty(), "object message defaults");
        check(DispatchLibrary.key(6) == 66, "valid binding in malformed row preserved");
        check(DispatchLibrary.message(7).equals("Keep last"), "later valid row preserved");
        check(DispatchLibrary.key(7) == 67, "later binding preserved");
        check(DispatchLibrary.message(19).isEmpty(), "short slot array fills defaults");
        check(!DispatchLibrary.hasPendingSave(), "loading does not mark pending");
        check(Files.readString(own).equals(source), "loading does not rewrite own file");
        check(Files.readString(copy).equals(obsolete), "loading does not rewrite CopyL");
        DispatchLibrary.updateMessage(8, "New edit");
        check(DispatchLibrary.save(), "can save recovered fields");
        JsonArray slots = JsonParser.parseString(Files.readString(own)).getAsJsonObject().getAsJsonArray("slots");
        check(slots.get(7).getAsJsonObject().get("message").getAsString().equals("Keep last"), "later row survives next save");
        check(Files.readString(copy).equals(obsolete), "save never modifies CopyL");
        check(backups(config).isEmpty(), "readable own file needs no corruption backup");
    }

    private static void copyFields(Path root) throws Exception {
        Path config = newConfig(root.resolve("copy"));
        Path copy = config.resolve("copyl-messages.json");
        String source = """
            {"names":["First",null,{},"Last"],
             "messages":["First text",null,[],"Last text","Beyond names"],
             "keyCodes":[65,null,{},66,67]}
            """;
        Files.writeString(copy, source);
        Path copyConfig = config.resolve("copyl.json");
        String settings = "{\"openKey\":{},\"wheelKey\":70}";
        Files.writeString(copyConfig, settings);
        check(DispatchLibrary.message(0).equals("First text"), "CopyL first message");
        check(DispatchLibrary.name(1).equals("Mensaje 2"), "CopyL null name defaults");
        check(DispatchLibrary.message(1).isEmpty(), "CopyL null text defaults");
        check(DispatchLibrary.key(1) == -1, "CopyL null binding defaults");
        check(DispatchLibrary.message(2).isEmpty() && DispatchLibrary.key(2) == -1, "CopyL malformed fields default");
        check(DispatchLibrary.message(3).equals("Last text"), "CopyL later valid text preserved");
        check(DispatchLibrary.key(3) == 66, "CopyL later valid binding preserved");
        check(DispatchLibrary.message(4).equals("Beyond names"), "unequal CopyL array lengths supported");
        check(DispatchLibrary.name(4).equals("Mensaje 5"), "missing CopyL name defaults");
        check(DispatchLibrary.openKey() == 70, "nonscalar openKey keeps legacy wheelKey fallback");
        check(DispatchLibrary.importedFrom().equals("copyl-messages.json"), "import records source");
        check(Files.isRegularFile(config.resolve("gripeverde-dispatch.json")), "recovered CopyL import persisted");
        check(!DispatchLibrary.hasPendingSave(), "successful import clean");
        check(Files.readString(copy).equals(source), "CopyL source unchanged");
        check(Files.readString(copyConfig).equals(settings), "CopyL config unchanged");
        check(backups(config).isEmpty(), "fresh import needs no corruption backup");
    }

    private static void bindingValues(Path root) throws Exception {
        String[] values = {"65", "\"66\"", "65.0", "6.5e1", "65.5", "4294967361", "1e1000",
            "-4294967231", "\"oops\"", "null", "true", "{}", "[]", "-1", "1003", "999"};
        int[] expected = {65, 66, 65, 65, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1003, -1};
        for (boolean copy : new boolean[] {false, true}) {
            for (int i = 0; i < values.length; i++) {
                // Isolate each value: duplicate-key cleanup must not obscure
                // the exact numeric conversion being tested here.
                Path config = newConfig(root.resolve((copy ? "copy-binding-" : "own-binding-") + i));
                JsonArray entries = new JsonArray();
                if (copy) {
                    entries.add(JsonParser.parseString(values[i]));
                } else {
                    JsonObject slot = new JsonObject();
                    slot.addProperty("message", "Keep text");
                    slot.add("key", JsonParser.parseString(values[i]));
                    entries.add(slot);
                }
                JsonObject data = new JsonObject();
                data.add(copy ? "keyCodes" : "slots", entries);
                Files.writeString(config.resolve(copy ? "copyl-messages.json" : "gripeverde-dispatch.json"), data.toString());
                check(DispatchLibrary.key(0) == expected[i], "exact binding conversion: " + values[i]);
                if (!copy) check(DispatchLibrary.message(0).equals("Keep text"), "invalid binding does not discard text");
            }
        }
        String[] invalidOpenKeys = {"null", "true", "{}", "[]", "65.5", "4294967361", "\"oops\""};
        for (int i = 0; i < invalidOpenKeys.length; i++) {
            Path config = newConfig(root.resolve("open-" + i));
            Files.writeString(config.resolve("gripeverde-dispatch.json"), "{\"openKey\":" + invalidOpenKeys[i] + ",\"slots\":[{\"message\":\"Keep\"}]}");
            check(DispatchLibrary.openKey() == GLFW.GLFW_KEY_RIGHT_ALT, "invalid openKey uses default");
            check(DispatchLibrary.message(0).equals("Keep"), "invalid openKey does not discard messages");
        }
    }

    private static void duplicateBindings(Path root) throws Exception {
        for (boolean copy : new boolean[] {false, true}) {
            Path config = newConfig(root.resolve(copy ? "copy-duplicates" : "own-duplicates"));
            int[] bindings = {65, 65, 70, 1003, 1003, -1, 66, 66};
            int[] expected = {65, -1, -1, 1003, -1, -1, 66, -1};
            JsonArray keys = new JsonArray();
            JsonArray messages = new JsonArray();
            JsonArray slots = new JsonArray();
            for (int i = 0; i < bindings.length; i++) {
                keys.add(bindings[i]);
                messages.add("Text " + i);
                JsonObject slot = new JsonObject();
                slot.addProperty("message", "Text " + i);
                slot.addProperty("key", bindings[i]);
                slots.add(slot);
            }
            JsonObject data = new JsonObject();
            if (copy) {
                data.add("keyCodes", keys);
                data.add("messages", messages);
                // Exercise reservation against the imported key, not the default.
                Files.writeString(config.resolve("copyl.json"), "{\"openKey\":70}");
            } else {
                data.addProperty("openKey", 70);
                data.add("slots", slots);
            }
            Path source = config.resolve(copy ? "copyl-messages.json" : "gripeverde-dispatch.json");
            String original = data.toString();
            Files.writeString(source, original);
            for (int i = 0; i < expected.length; i++) {
                check(DispatchLibrary.key(i) == expected[i], "loaded keys unique and open key reserved");
                check(DispatchLibrary.message(i).equals("Text " + i), "binding repair preserves every message");
            }
            check(DispatchLibrary.openKey() == 70, "loaded opening key preserved");
            check(Files.readString(source).equals(original), "loading never rewrites source file");
            check(!DispatchLibrary.hasPendingSave(), "normalization does not mark an edit");
            check(DispatchLibrary.save(), "normalized bindings can be persisted");
            Path own = config.resolve("gripeverde-dispatch.json");
            JsonArray persisted = JsonParser.parseString(Files.readString(own)).getAsJsonObject().getAsJsonArray("slots");
            for (int i = 0; i < expected.length; i++) {
                check(persisted.get(i).getAsJsonObject().get("key").getAsInt() == expected[i], "saved binding remains canonical");
            }
        }
    }

    private static void legacyKeys(Path root) throws Exception {
        for (String file : new String[] {"copyl.json", "lclient.json"}) {
            Path config = newConfig(root.resolve(file));
            Files.writeString(config.resolve(file), "{\"wheelKey\":\"70\"}");
            check(DispatchLibrary.openKey() == 70, "legacy wheelKey accepted: " + file);
        }
    }

    private static List<Path> backups(Path config) throws Exception {
        try (var files = Files.list(config)) {
            return files.filter(path -> path.getFileName().toString().startsWith("gripeverde-dispatch.json.corrupt-"))
                .filter(path -> path.getFileName().toString().endsWith(".bak")).toList();
        }
    }

    private static void noSaveTemporaryFiles(Path config) throws Exception {
        try (var files = Files.list(config)) {
            check(files.noneMatch(path -> path.getFileName().toString().endsWith(".tmp")), "save temporaries cleaned");
        }
    }

    private static void corruptOriginals(Path root) throws Exception {
        byte[][] originals = {
            "{\"slots\":[".getBytes(StandardCharsets.UTF_8),
            "null".getBytes(StandardCharsets.UTF_8),
            "[1,2]".getBytes(StandardCharsets.UTF_8),
            "\"not an object\"".getBytes(StandardCharsets.UTF_8),
            new byte[] {'{', '"', 'x', '"', ':', (byte) 0xFF}
        };
        for (boolean copy : new boolean[] {false, true}) {
            for (int i = 0; i < originals.length; i++) {
                Path game = root.resolve((copy ? "corrupt-with-copy-" : "corrupt-without-copy-") + i);
                Path config = newConfig(game);
                Path own = config.resolve("gripeverde-dispatch.json");
                Path existingBackup = config.resolve("gripeverde-dispatch.json.corrupt-previous.bak");
                Files.writeString(existingBackup, "Previous backup");
                Files.write(own, originals[i]);
                Path copyFile = config.resolve("copyl-messages.json");
                String copyText = "{\"messages\":[\"Recovered from CopyL\"]}";
                if (copy) Files.writeString(copyFile, copyText);
                check(DispatchLibrary.message(0).equals(copy ? "Recovered from CopyL" : ""), "corrupt own file uses recovery/defaults");
                check(!DispatchLibrary.hasPendingSave(), "load or successful import is clean");
                check(backups(config).size() == (copy ? 2 : 1), "backup created only before replacement");
                if (!copy) check(Arrays.equals(Files.readAllBytes(own), originals[i]), "load without CopyL leaves original bytes intact");
                DispatchLibrary.updateMessage(0, "Edited after recovery");
                check(DispatchLibrary.save(), "save after corrupt load succeeds with backup");
                List<Path> preserved = backups(config);
                check(preserved.size() == 2, "one new backup per damaged original");
                Path backup = preserved.stream().filter(path -> !path.equals(existingBackup)).findFirst().orElseThrow();
                check(Arrays.equals(Files.readAllBytes(backup), originals[i]), "backup preserves exact bytes including invalid UTF-8");
                check(Files.readString(existingBackup).equals("Previous backup"), "older backup not overwritten");
                check(JsonParser.parseString(Files.readString(own)).getAsJsonObject().getAsJsonArray("slots")
                    .get(0).getAsJsonObject().get("message").getAsString().equals("Edited after recovery"), "recovered data saved as valid JSON");
                DispatchLibrary.updateMessage(1, "Another edit");
                check(DispatchLibrary.save(), "subsequent save succeeds");
                check(backups(config).size() == 2, "normal saves do not accumulate backups");
                check(Arrays.equals(Files.readAllBytes(backup), originals[i]), "subsequent save leaves backup unchanged");
                noSaveTemporaryFiles(config);
                newConfig(game);
                check(DispatchLibrary.message(0).equals("Edited after recovery"), "recovered file loads on next process");
                check(backups(config).size() == 2, "loading recovered file creates no extra backup");
                if (copy) check(Files.readString(copyFile).equals(copyText), "recovery never writes CopyL source");
            }
        }
    }

    private static void backupFailure(Path root) throws Exception {
        Path config = newConfig(root.resolve("backup-failure"));
        Path own = config.resolve("gripeverde-dispatch.json");
        Files.createDirectory(own);
        Path marker = own.resolve("keep.txt");
        Files.writeString(marker, "Keep destination");
        Path copy = config.resolve("copyl-messages.json");
        String copyText = "{\"messages\":[\"Recovered in memory\"]}";
        Files.writeString(copy, copyText);
        check(DispatchLibrary.message(0).equals("Recovered in memory"), "failed backup retains imported memory");
        check(DispatchLibrary.hasPendingSave(), "failed backup leaves pending save");
        check(Files.readString(marker).equals("Keep destination"), "failed backup does not replace original path");
        check(backups(config).isEmpty(), "failed backup creates no misleading copy");
        noSaveTemporaryFiles(config);
        check(!DispatchLibrary.save(), "retry still refuses unpreserved original");
        check(DispatchLibrary.hasPendingSave(), "retry failure still pending");
        noSaveTemporaryFiles(config);
        Files.delete(marker);
        Files.delete(own);
        byte[] original = "{\"truncated\":".getBytes(StandardCharsets.UTF_8);
        Files.write(own, original);
        check(DispatchLibrary.save(), "retry succeeds once original can be preserved");
        check(!DispatchLibrary.hasPendingSave(), "successful retry clears pending");
        check(backups(config).size() == 1, "retry preserves original exactly once");
        check(Arrays.equals(Files.readAllBytes(backups(config).get(0)), original), "retry backup has original bytes");
        check(Files.readString(copy).equals(copyText), "failed recovery leaves CopyL untouched");
        noSaveTemporaryFiles(config);
    }

    private static void unreadableOriginal(Path root) throws Exception {
        Path config = newConfig(root.resolve("unreadable-original"));
        if (!Files.getFileStore(config).supportsFileAttributeView("posix")) {
            System.out.println("Unreadable-original permission scenario skipped: no POSIX permissions");
            return;
        }
        Path own = config.resolve("gripeverde-dispatch.json");
        byte[] original = "{\"truncated\":".getBytes(StandardCharsets.UTF_8);
        Files.write(own, original);
        var permissions = Files.getPosixFilePermissions(own);
        try {
            Files.setPosixFilePermissions(own, java.util.Set.of());
            if (Files.isReadable(own)) {
                System.out.println("Unreadable-original permission scenario skipped: process bypasses file permissions");
                return;
            }
            Files.writeString(config.resolve("copyl-messages.json"), "{\"messages\":[\"Recovered in memory\"]}");
            check(DispatchLibrary.message(0).equals("Recovered in memory"), "unreadable original retains recovery in memory");
            check(DispatchLibrary.hasPendingSave(), "copy failure leaves save pending");
            check(Files.exists(own), "copy failure leaves unreadable original present");
            check(backups(config).isEmpty(), "partially created backup cleaned after copy failure");
            noSaveTemporaryFiles(config);
        } finally {
            Files.setPosixFilePermissions(own, permissions);
        }
        check(Arrays.equals(Files.readAllBytes(own), original), "copy failure did not modify original bytes");
        check(DispatchLibrary.save(), "backup retries after read permission restored");
        check(!DispatchLibrary.hasPendingSave(), "permission recovery clears pending");
        check(backups(config).size() == 1, "permission recovery makes one complete backup");
        check(Arrays.equals(Files.readAllBytes(backups(config).get(0)), original), "permission recovery preserves exact bytes");
        noSaveTemporaryFiles(config);
    }

    private static void manuallyRemovedOriginal(Path root) throws Exception {
        Path config = newConfig(root.resolve("removed-original"));
        Path own = config.resolve("gripeverde-dispatch.json");
        Files.writeString(own, "{\"truncated\":");
        DispatchLibrary.ensureLoaded();
        Files.delete(own);
        DispatchLibrary.updateMessage(0, "User removed damaged original");
        check(DispatchLibrary.save(), "explicitly removed original does not block future save");
        check(backups(config).isEmpty(), "no backup possible or needed after explicit removal");
        check(Files.isRegularFile(own), "new file can be created after manual removal");
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("gripeverde-loading-regression-");
        try {
            ownFields(root);
            copyFields(root);
            bindingValues(root);
            legacyKeys(root);
            duplicateBindings(root);
            corruptOriginals(root);
            backupFailure(root);
            unreadableOriginal(root);
            manuallyRemovedOriginal(root);
            System.out.println("Dispatch loading: " + checks + " checks passed");
        } finally {
            try (var files = Files.walk(root)) {
                for (Path path : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }
}
