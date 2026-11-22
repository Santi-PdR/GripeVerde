package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.client.lore.ChroniclerMemory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/** Real memory/executor/JSON checks without launching the Minecraft client. */
public final class MemoryShutdownRegression {
    private static int checks;

    private static void check(boolean value, String reason) {
        checks++;
        if (!value) throw new AssertionError(reason);
    }

    private static Object field(String name) throws Exception {
        var field = ChroniclerMemory.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    private static JsonObject read(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    public static void main(String[] args) throws Exception {
        Path game = Files.createTempDirectory("gripeverde-memory-shutdown-");
        try {
            FMLPaths.loadAbsolutePaths(game);
            Path config = FMLPaths.CONFIGDIR.get();
            Path primary = config.resolve("gripeverde-memory.json");
            check(ChroniclerMemory.get() != null, "memory initializes without a client");
            check(!Files.exists(primary), "reading fresh memory creates no file");
            // Hold the actual writer monitor until both edits are present. The
            // first save queues work; that worker must acquire this monitor.
            synchronized (ChroniclerMemory.class) {
                ChroniclerMemory.setCounter("queued", 42);
                ChroniclerMemory.setLastSeenVersion("shutdown-regression");
            }
            ChroniclerMemory.flushForShutdown();
            ExecutorService writer = (ExecutorService) field("WRITER");
            check(writer.isShutdown(), "shutdown rejects new queued work");
            check(writer.awaitTermination(5, TimeUnit.SECONDS), "worker terminates");
            check(!(Boolean) field("savePending"), "shutdown leaves no pending write");
            JsonObject persisted = read(primary);
            check(persisted.getAsJsonObject("counters").get("queued").getAsInt() == 42, "queued counter persisted");
            check(persisted.get("lastSeenVersion").getAsString().equals("shutdown-regression"), "latest queued edit persisted");
            check(!Files.exists(config.resolve("gripeverde-memory.tmp")), "completed save leaves no temporary");

            // Late changes cannot queue on the stopped executor, but the
            // normal throttle/fallback and a second flush must still save them.
            ChroniclerMemory.setCounter("after_shutdown", 7);
            ChroniclerMemory.flushForShutdown();
            persisted = read(primary);
            check(persisted.getAsJsonObject("counters").get("after_shutdown").getAsInt() == 7, "late edit persisted after shutdown");
            check(persisted.getAsJsonObject("counters").get("queued").getAsInt() == 42, "late save retains earlier edit");
            check(!(Boolean) field("savePending"), "late flush clean");
            byte[] before = Files.readAllBytes(primary);
            ChroniclerMemory.flushForShutdown();
            check(Arrays.equals(before, Files.readAllBytes(primary)), "repeated clean flush preserves JSON");
            System.out.println("Memory shutdown persistence: " + checks + " checks passed");
        } finally {
            ChroniclerMemory.flushForShutdown();
            try (var files = Files.walk(game)) {
                for (Path path : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }
}
