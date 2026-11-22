package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.client.integration.DispatchLibrary;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
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
        resetField("openKey", 344);
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
        check(DispatchLibrary.openKey() == 344, "null open key defaults");
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
    }

    private static void bindingValues(Path root) throws Exception {
        String[] values = {"65", "\"66\"", "65.0", "6.5e1", "65.5", "4294967361", "1e1000",
            "-4294967231", "\"oops\"", "null", "true", "{}", "[]", "-1", "1003", "999"};
        int[] expected = {65, 66, 65, 65, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1003, -1};
        for (boolean copy : new boolean[] {false, true}) {
            Path config = newConfig(root.resolve(copy ? "copy-bindings" : "own-bindings"));
            JsonArray entries = new JsonArray();
            for (int i = 0; i < values.length; i++) {
                if (copy) {
                    entries.add(JsonParser.parseString(values[i]));
                } else {
                    JsonObject slot = new JsonObject();
                    slot.addProperty("message", "Message " + i);
                    slot.add("key", JsonParser.parseString(values[i]));
                    entries.add(slot);
                }
            }
            JsonObject data = new JsonObject();
            data.add(copy ? "keyCodes" : "slots", entries);
            Files.writeString(config.resolve(copy ? "copyl-messages.json" : "gripeverde-dispatch.json"), data.toString());
            for (int i = 0; i < values.length; i++) {
                check(DispatchLibrary.key(i) == expected[i], "exact binding conversion: " + values[i]);
                if (!copy) check(DispatchLibrary.message(i).equals("Message " + i), "invalid binding does not discard text");
            }
        }
        String[] invalidOpenKeys = {"null", "true", "{}", "[]", "65.5", "4294967361", "\"oops\""};
        for (int i = 0; i < invalidOpenKeys.length; i++) {
            Path config = newConfig(root.resolve("open-" + i));
            Files.writeString(config.resolve("gripeverde-dispatch.json"), "{\"openKey\":" + invalidOpenKeys[i] + ",\"slots\":[{\"message\":\"Keep\"}]}");
            check(DispatchLibrary.openKey() == 344, "invalid openKey uses default");
            check(DispatchLibrary.message(0).equals("Keep"), "invalid openKey does not discard messages");
        }
    }

    private static void legacyKeys(Path root) throws Exception {
        for (String file : new String[] {"copyl.json", "lclient.json"}) {
            Path config = newConfig(root.resolve(file));
            Files.writeString(config.resolve(file), "{\"wheelKey\":\"70\"}");
            check(DispatchLibrary.openKey() == 70, "legacy wheelKey accepted: " + file);
        }
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("gripeverde-loading-regression-");
        try {
            ownFields(root);
            copyFields(root);
            bindingValues(root);
            legacyKeys(root);
            System.out.println("Dispatch loading: " + checks + " checks passed");
        } finally {
            try (var files = Files.walk(root)) {
                for (Path path : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }
}
