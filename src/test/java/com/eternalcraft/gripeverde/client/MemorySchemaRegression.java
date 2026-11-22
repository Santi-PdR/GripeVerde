package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.client.lore.ChroniclerMemory;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/** Real Gson/memory/filesystem tests for downgrade safety; no client or network. */
public final class MemorySchemaRegression {
    private static final String PRIMARY = "gripeverde-memory.json";
    private static final String TEMP = "gripeverde-memory.tmp";
    private static final String BACKUP = "gripeverde-memory.backup.json";
    private static final String CORRUPT = "gripeverde-memory.corrupt.json";
    private static int checks;

    private static void check(boolean value, String reason) {
        checks++;
        if (!value) throw new AssertionError(reason);
    }

    private static void set(String name, Object value) throws Exception {
        var field = ChroniclerMemory.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    private static boolean flag(String name) throws Exception {
        var field = ChroniclerMemory.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getBoolean(null);
    }

    private static Path newConfig(Path game) throws Exception {
        FMLPaths.loadAbsolutePaths(game);
        // Simulate a fresh process per fixture, without exposing a test/reset API.
        // No asynchronous saves are scheduled in this suite.
        set("data", null);
        set("readOnlyForNewerSchema", false);
        set("savePending", false);
        set("primaryKnownCorrupt", false);
        set("lastSaveMs", 0L);
        return FMLPaths.CONFIGDIR.get();
    }

    private static void writeNow() throws Exception {
        var write = ChroniclerMemory.class.getDeclaredMethod("writeNow");
        write.setAccessible(true);
        write.invoke(null);
    }

    private static String known(int version, int count) {
        return "{\"version\":" + version + ",\"counters\":{\"kept\":" + count + "}}";
    }

    private static Map<String, byte[]> snapshot(Path config) throws Exception {
        Map<String, byte[]> result = new LinkedHashMap<>();
        try (var files = Files.list(config)) {
            for (Path path : files.filter(Files::isRegularFile).sorted().toList()) {
                result.put(path.getFileName().toString(), Files.readAllBytes(path));
            }
        }
        return result;
    }

    private static void unchanged(Path config, Map<String, byte[]> before) throws Exception {
        Map<String, byte[]> after = snapshot(config);
        check(after.keySet().equals(before.keySet()), "no files created, renamed or removed in read-only mode");
        for (String name : before.keySet()) {
            check(Arrays.equals(before.get(name), after.get(name)), "file preserved byte for byte: " + name);
        }
    }

    private static void futureFiles(Path root) throws Exception {
        String[] versions = {"4", "\"99\"", "4294967299", "1e1000"};
        String[] locations = {PRIMARY, TEMP, BACKUP};
        for (int location = 0; location < locations.length; location++) {
            for (int version = 0; version < versions.length; version++) {
                Path config = newConfig(root.resolve("future-" + location + "-" + version));
                Files.writeString(config.resolve(PRIMARY), known(3, 10));
                Files.writeString(config.resolve(TEMP), known(3, 20));
                Files.writeString(config.resolve(BACKUP), known(3, 30));
                Files.writeString(config.resolve(CORRUPT), "Previous corrupt file must survive");
                // Future versions may change the type of known fields. Checking
                // the version must precede binding those fields into Data.
                String future = "{\"version\":" + versions[version]
                    + ",\"counters\":[\"new representation\"],\"discovered\":{},\"futureOnly\":{\"keep\":[1,2,3]}}";
                Files.writeString(config.resolve(locations[location]), future);
                Map<String, byte[]> before = snapshot(config);
                check(ChroniclerMemory.get() != null, "future schema still supplies safe session state");
                check(flag("readOnlyForNewerSchema"), "future primary/temporary/backup activates read-only mode");
                check(!flag("primaryKnownCorrupt"), "future schema is not classified as corrupt");
                ChroniclerMemory.setCounter("ephemeral", 7);
                ChroniclerMemory.markDiscovered("ephemeral");
                ChroniclerMemory.setLastSeenVersion("ephemeral");
                ChroniclerMemory.save();
                writeNow();
                check(!flag("savePending"), "read-only changes do not queue persistence retries");
                unchanged(config, before);
            }
        }
    }

    private static void futureFileAfterLoad(Path root) throws Exception {
        String[] locations = {PRIMARY, TEMP, BACKUP};
        for (int i = 0; i < locations.length; i++) {
            Path config = newConfig(root.resolve("changed-" + i));
            Files.writeString(config.resolve(PRIMARY), known(3, 5));
            check(ChroniclerMemory.get().counters.get("kept") == 5, "known memory loads normally before external change");
            Files.writeString(config.resolve(locations[i]), "{\"version\":99,\"unknown\":[\"do not erase\"]}");
            Map<String, byte[]> before = snapshot(config);
            ChroniclerMemory.get().counters.put("session-only", 8);
            set("savePending", true);
            writeNow();
            check(flag("readOnlyForNewerSchema"), "write preflight detects a newer file introduced after load");
            check(!flag("savePending"), "blocked future writes do not remain pending");
            unchanged(config, before);
            // Removing a file manually does not silently re-enable writes during
            // this process. The owner must restart with compatible files/mod.
            Files.delete(config.resolve(locations[i]));
            before = snapshot(config);
            ChroniclerMemory.save();
            writeNow();
            check(flag("readOnlyForNewerSchema"), "read-only safety stays latched for the session");
            unchanged(config, before);
        }
    }

    private static void knownSchemas(Path root) throws Exception {
        for (int version = 1; version <= 3; version++) {
            Path config = newConfig(root.resolve("known-" + version));
            Path primary = config.resolve(PRIMARY);
            Files.writeString(primary, known(version, 5));
            check(ChroniclerMemory.get().version == 3, "supported schema migration still runs");
            check(!flag("readOnlyForNewerSchema"), "supported schema remains writable");
            ChroniclerMemory.get().counters.put("new", 8);
            writeNow();
            var persisted = JsonParser.parseString(Files.readString(primary)).getAsJsonObject();
            check(persisted.get("version").getAsInt() == 3, "supported save uses migrated version");
            check(persisted.getAsJsonObject("counters").get("kept").getAsInt() == 5, "supported save retains existing progress");
            check(persisted.getAsJsonObject("counters").get("new").getAsInt() == 8, "supported save persists new progress");
            check(Files.isRegularFile(config.resolve(BACKUP)), "supported save still updates backup");
            check(!Files.exists(config.resolve(TEMP)), "supported save leaves no temporary");
        }
        Path config = newConfig(root.resolve("corrupt-with-known-recovery"));
        String corrupt = "{\"truncated\":";
        Files.writeString(config.resolve(PRIMARY), corrupt);
        Files.writeString(config.resolve(TEMP), known(3, 42));
        Files.writeString(config.resolve(BACKUP), known(3, 11));
        check(ChroniclerMemory.get().counters.get("kept") == 42, "known interrupted temporary still recovers");
        check(!flag("readOnlyForNewerSchema"), "corrupt JSON is not confused with future schema");
        check(Files.readString(config.resolve(CORRUPT)).equals(corrupt), "existing corrupt-file preservation still works");
        check(JsonParser.parseString(Files.readString(config.resolve(PRIMARY))).getAsJsonObject()
            .getAsJsonObject("counters").get("kept").getAsInt() == 42, "known recovery persists primary");
        check(JsonParser.parseString(Files.readString(config.resolve(BACKUP))).getAsJsonObject()
            .getAsJsonObject("counters").get("kept").getAsInt() == 11, "known recovery preserves good backup");
    }

    private static void futureInMemory(Path root) throws Exception {
        Path config = newConfig(root.resolve("future-data"));
        ChroniclerMemory.get().version = 99;
        writeNow();
        check(flag("readOnlyForNewerSchema"), "write refuses unsupported schema even if Data was changed directly");
        check(snapshot(config).isEmpty(), "unsupported in-memory data writes no file");
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("gripeverde-memory-schema-");
        try {
            futureFiles(root);
            futureFileAfterLoad(root);
            knownSchemas(root);
            futureInMemory(root);
            ChroniclerMemory.flushForShutdown();
            System.out.println("Memory schemas: " + checks + " checks passed");
        } finally {
            ChroniclerMemory.flushForShutdown();
            try (var files = Files.walk(root)) {
                for (Path path : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }
}
