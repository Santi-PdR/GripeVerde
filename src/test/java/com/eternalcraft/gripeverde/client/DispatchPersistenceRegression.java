package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.client.integration.DispatchLibrary;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/** Real JSON/filesystem tests in an isolated Forge config directory; no client. */
public final class DispatchPersistenceRegression {
    private static int checks;

    private static void check(boolean value, String reason) {
        checks++;
        if (!value) throw new AssertionError(reason);
    }

    private static JsonObject read(Path own) throws Exception {
        return JsonParser.parseString(Files.readString(own)).getAsJsonObject();
    }

    private static void noTemporaryFiles(Path config) throws Exception {
        try (var files = Files.list(config)) {
            check(files.noneMatch(p -> p.getFileName().toString().endsWith(".tmp")), "temporary files cleaned");
        }
    }

    public static void main(String[] args) throws Exception {
        Path game = Files.createTempDirectory("gripeverde-dispatch-regression-");
        try {
            FMLPaths.loadAbsolutePaths(game);
            Path config = FMLPaths.CONFIGDIR.get();
            Path own = config.resolve("gripeverde-dispatch.json");
            // Public save() must also initialize the library before any getter.
            check(DispatchLibrary.save(), "save before first getter");
            check(read(own).getAsJsonArray("slots").size() == 20, "initialized slots");
            check(!DispatchLibrary.hasPendingSave(), "initial save clean");
            String initial = Files.readString(own);
            check(!DispatchLibrary.updateMessage(-1, "invalid"), "negative index ignored");
            check(!DispatchLibrary.updateMessage(20, "invalid"), "out of range ignored");
            check(!DispatchLibrary.updateMessage(0, ""), "unchanged text ignored");
            check(!DispatchLibrary.hasPendingSave(), "no-op stays clean");
            check(DispatchLibrary.updateMessage(0, "Primera línea\nsegunda"), "edit changes memory");
            check(DispatchLibrary.message(0).equals("Primera línea segunda"), "normalizes controls");
            for (int i = 0; i < 100; i++) DispatchLibrary.updateMessage(1, "Mensaje " + i);
            check(Files.readString(own).equals(initial), "typing never writes JSON");
            check(DispatchLibrary.hasPendingSave(), "typing pending");
            check(DispatchLibrary.save(), "flush succeeds");
            check(!DispatchLibrary.hasPendingSave(), "successful flush clean");
            check(read(own).getAsJsonArray("slots").get(1).getAsJsonObject().get("message").getAsString().equals("Mensaje 99"), "last edit persisted");
            noTemporaryFiles(config);

            check(DispatchLibrary.setMessage(2, "Immediate"), "immediate API preserved");
            check(read(own).getAsJsonArray("slots").get(2).getAsJsonObject().get("message").getAsString().equals("Immediate"), "immediate API writes");
            DispatchLibrary.setKey(0, 65);
            check(read(own).getAsJsonArray("slots").get(0).getAsJsonObject().get("key").getAsInt() == 65, "key setter still persists");
            DispatchLibrary.setKey(1, 65);
            check(DispatchLibrary.key(0) == DispatchLibrary.UNBOUND && DispatchLibrary.key(1) == 65, "key uniqueness preserved");
            DispatchLibrary.setOpenKey(65);
            check(DispatchLibrary.key(1) == DispatchLibrary.UNBOUND && read(own).get("openKey").getAsInt() == 65, "open key reserved and saved");

            // Fail deterministically even as root: replacement of a nonempty directory.
            Path backup = config.resolve("previous.json");
            Files.move(own, backup);
            String previous = Files.readString(backup);
            Files.createDirectory(own);
            Path marker = own.resolve("keep.txt");
            Files.writeString(marker, "keep");
            DispatchLibrary.updateMessage(0, "Retry after failure");
            check(!DispatchLibrary.save(), "replacement fails");
            check(DispatchLibrary.hasPendingSave(), "failure remains pending");
            check(DispatchLibrary.message(0).equals("Retry after failure"), "failure retains memory");
            check(Files.readString(marker).equals("keep"), "failed replacement leaves destination intact");
            check(Files.readString(backup).equals(previous), "previous JSON intact");
            noTemporaryFiles(config);
            DispatchLibrary.setKey(3, 66);
            check(DispatchLibrary.hasPendingSave(), "failed binding write also pending");
            noTemporaryFiles(config);
            Files.delete(marker);
            Files.delete(own);
            Files.move(backup, own);
            check(DispatchLibrary.save(), "retry succeeds");
            check(!DispatchLibrary.hasPendingSave(), "retry clears pending");
            check(read(own).getAsJsonArray("slots").get(0).getAsJsonObject().get("message").getAsString().equals("Retry after failure"), "retry persists unsaved message");
            check(read(own).getAsJsonArray("slots").get(3).getAsJsonObject().get("key").getAsInt() == 66, "retry persists unsaved binding");
            noTemporaryFiles(config);

            check(DispatchLibrary.updateMessage(0, ""), "clear marks pending");
            check(DispatchLibrary.save(), "clear saved");
            check(read(own).getAsJsonArray("slots").get(0).getAsJsonObject().get("message").getAsString().isEmpty(), "clear persisted");
            System.out.println("Dispatch persistence: " + checks + " checks passed");
        } finally {
            try (var files = Files.walk(game)) {
                for (Path path : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }
}
