package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.client.lore.ChroniclerMemory;
import com.eternalcraft.gripeverde.client.lore.Discoveries;
import com.eternalcraft.gripeverde.client.lore.Marginalia;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;

/** Real memory/Gson/filesystem checks for null entries and numeric boundaries. */
public final class MemoryDataRegression {
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

    private static void set(String name, Object value) throws Exception {
        var field = ChroniclerMemory.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    private static Path newConfig(Path game) throws Exception {
        FMLPaths.loadAbsolutePaths(game);
        set("data", null);
        set("readOnlyForNewerSchema", false);
        set("savePending", false);
        set("primaryKnownCorrupt", false);
        // Keep saves throttled: this suite explicitly invokes the real writer
        // after each fixture, without racing queued work against test resets.
        set("lastSaveMs", Long.MAX_VALUE);
        return FMLPaths.CONFIGDIR.get().resolve("gripeverde-memory.json");
    }

    private static void writeNow() throws Exception {
        var method = ChroniclerMemory.class.getDeclaredMethod("writeNow");
        method.setAccessible(true);
        method.invoke(null);
    }

    private static JsonObject read(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static void collectionEntries(Path root) throws Exception {
        Path primary = newConfig(root.resolve("collections"));
        JsonObject source = new JsonObject();
        source.addProperty("version", 3);
        JsonArray ids = new JsonArray();
        ids.add(JsonNull.INSTANCE);
        ids.add("");
        ids.add(" \t ");
        ids.add("kept");
        ids.add("unknown-id");
        source.add("discovered", ids);
        source.add("collectibles", ids.deepCopy());
        JsonArray notes = new JsonArray();
        for (int i = 0; i < 300; i++) notes.add(JsonNull.INSTANCE);
        notes.add("\t");
        for (int i = 0; i < 300; i++) notes.add("note-" + i);
        source.add("pendingNotes", notes);
        JsonArray guestbook = new JsonArray();
        for (int i = 0; i < 4100; i++) guestbook.add(JsonNull.INSTANCE);
        guestbook.add("");
        guestbook.add("  Signed text stays unchanged  ");
        for (int i = 0; i < 4100; i++) guestbook.add("Guest " + i);
        source.add("guestbook", guestbook);
        JsonObject counters = new JsonObject();
        counters.addProperty("", 1);
        counters.addProperty(" \t ", 2);
        counters.add("missing", JsonNull.INSTANCE);
        counters.addProperty("kept", 42);
        counters.addProperty("signed", -9);
        source.add("counters", counters);
        String original = source.toString();
        Files.writeString(primary, original);
        ChroniclerMemory.Data data = ChroniclerMemory.get();
        check(data.discovered.equals(Set.of("kept", "unknown-id")), "null/blank discoveries removed, unknown ids preserved");
        check(data.collectibles.equals(Set.of("kept", "unknown-id")), "null/blank collectibles removed");
        check(ChroniclerMemory.collectibleCount() == 2, "invalid collectible entries do not inflate count");
        check(data.pendingNotes.size() == 256, "notes capped after cleanup");
        check(data.pendingNotes.get(0).equals("note-0"), "valid notes survive leading null entries");
        check(data.pendingNotes.get(255).equals("note-255"), "note order and cap preserved");
        check(data.guestbook.size() == 4096, "guestbook capped after null cleanup");
        check(data.guestbook.get(0).isEmpty(), "empty guestbook text preserved intentionally");
        check(data.guestbook.get(1).equals("  Signed text stays unchanged  "), "guestbook text not trimmed");
        check(data.guestbook.get(4095).equals("Guest 4093"), "guestbook order preserved");
        check(data.counters.size() == 2 && data.counters.get("kept") == 42, "bad counter entries removed without losing valid values");
        check(data.counters.get("signed") == -9, "signed counter values remain supported");
        check(Files.readString(primary).equals(original), "sanitizing load does not rewrite source");
        check(!(Boolean) field("savePending"), "sanitizing load is not an edit");
        writeNow();
        JsonObject persisted = read(primary);
        check(persisted.getAsJsonArray("pendingNotes").get(0).getAsString().equals("note-0"), "cleaned notes persist");
        check(persisted.getAsJsonArray("collectibles").size() == 2, "cleaned collectibles persist");
        check(persisted.getAsJsonObject("counters").size() == 2, "cleaned counters persist");
        check(!Files.exists(primary.resolveSibling("gripeverde-memory.corrupt.json")), "null entries do not classify whole record as corrupt");
    }

    private static void invalidApiIds(Path root) throws Exception {
        Path primary = newConfig(root.resolve("invalid-api"));
        for (String id : new String[] {null, "", " \t\r\n"}) {
            check(!ChroniclerMemory.isDiscovered(id), "invalid discovery lookup false");
            check(!ChroniclerMemory.hasCollectible(id), "invalid collectible lookup false");
            check(ChroniclerMemory.counter(id) == 0, "invalid counter lookup zero");
            check(ChroniclerMemory.bump(id) == 0, "invalid counter increment ignored");
            ChroniclerMemory.markDiscovered(id);
            ChroniclerMemory.addCollectible(id);
            ChroniclerMemory.setCounter(id, 5);
        }
        check(field("data") == null, "invalid ids do not initialize memory");
        check(!Files.exists(primary), "invalid ids create no file");
        check(!(Boolean) field("savePending"), "invalid ids queue no save");
        ChroniclerMemory.markDiscovered("custom-id");
        ChroniclerMemory.addCollectible("custom-id");
        ChroniclerMemory.setCounter("custom-key", -7);
        check(ChroniclerMemory.isDiscovered("custom-id"), "valid unknown discovery supported");
        check(ChroniclerMemory.hasCollectible("custom-id"), "valid unknown collectible supported");
        check(ChroniclerMemory.counter("custom-key") == -7, "generic signed counter supported");
        writeNow();
        check(read(primary).getAsJsonObject("counters").get("custom-key").getAsInt() == -7, "valid generic values persist");
    }

    private static void pendingNoteApis(Path root) throws Exception {
        Path primary = newConfig(root.resolve("note-apis"));
        for (String id : new String[] {null, "", " \t\r\n", "\u2003"}) {
            Marginalia.queue(id);
            Marginalia.queueOnce(id);
        }
        check(field("data") == null, "invalid note ids do not initialize memory");
        check(!(Boolean) field("savePending"), "invalid note ids schedule no save");
        check(!Files.exists(primary), "invalid note ids create no file");

        String id = Marginalia.N_READ_YOU;
        String key = "note_" + id;
        Marginalia.queue(id);
        ChroniclerMemory.Data data = ChroniclerMemory.get();
        check(data.pendingNotes.equals(java.util.List.of(id)), "ordinary note enqueued");
        check(!data.counters.containsKey(key), "ordinary enqueue does not consume once marker");
        writeNow();
        Marginalia.queue(id);
        check(data.pendingNotes.equals(java.util.List.of(id)), "duplicate queue preserves pending order");
        check(!(Boolean) field("savePending"), "duplicate queue schedules no save");
        Marginalia.queueOnce(id);
        check(data.pendingNotes.equals(java.util.List.of(id)), "queueOnce accepts an already pending note without duplication");
        check(ChroniclerMemory.counter(key) == 1, "already pending note receives once marker");
        writeNow();
        Marginalia.queueOnce(id);
        check(!(Boolean) field("savePending"), "repeat queueOnce schedules no save");
        data.pendingNotes.clear();
        Marginalia.queueOnce(id);
        check(data.pendingNotes.isEmpty(), "consumed once marker prevents a second enqueue");
        check(!(Boolean) field("savePending"), "consumed once marker schedules no save");
        // queue remains repeatable after the pending entry has been consumed.
        Marginalia.queue(id);
        check(data.pendingNotes.equals(java.util.List.of(id)), "ordinary queue remains repeatable");
        String customId = "  custom-note  ";
        Marginalia.queue(customId);
        check(data.pendingNotes.get(1).equals(customId), "nonblank unknown ids are preserved without trimming");
        for (int i = 2; i < 256; i++) Marginalia.queue("filler-" + i);
        check(data.pendingNotes.size() == 256, "runtime queue reaches loading cap");
        writeNow();
        String fullFile = Files.readString(primary);
        var originalOrder = java.util.List.copyOf(data.pendingNotes);
        Marginalia.queue("overflow");
        Marginalia.queueOnce(Marginalia.N_LANGUAGE);
        check(data.pendingNotes.equals(originalOrder), "full queue rejects new notes without evicting existing ones");
        check(!data.counters.containsKey("note_" + Marginalia.N_LANGUAGE), "full queue does not consume once marker");
        check(!(Boolean) field("savePending"), "full queue rejection schedules no save");
        check(Files.readString(primary).equals(fullFile), "full queue rejection leaves persisted bytes unchanged");
        // A duplicate is still accepted at capacity, allowing its once marker.
        Marginalia.queueOnce(customId);
        check(data.pendingNotes.equals(originalOrder), "duplicate at capacity keeps exact queue order");
        check(ChroniclerMemory.counter("note_" + customId) == 1, "already pending note can be marked once at capacity");
        writeNow();
        Marginalia.queueOnce(customId);
        check(!(Boolean) field("savePending"), "marked duplicate at capacity is a no-op");
        data.pendingNotes.remove(0);
        Marginalia.queueOnce(Marginalia.N_LANGUAGE);
        check(data.pendingNotes.size() == 256, "previously rejected note can retry after space opens");
        check(data.pendingNotes.get(255).equals(Marginalia.N_LANGUAGE), "retry appends at tail");
        check(ChroniclerMemory.counter("note_" + Marginalia.N_LANGUAGE) == 1, "accepted retry consumes once marker");
        writeNow();
        var expectedNotes = java.util.List.copyOf(data.pendingNotes);
        set("data", null);
        ChroniclerMemory.Data reloaded = ChroniclerMemory.get();
        check(reloaded.pendingNotes.equals(expectedNotes), "runtime cap and order survive real save and reload");
        check(ChroniclerMemory.counter("note_" + Marginalia.N_LANGUAGE) == 1, "retry marker survives real save and reload");
        check(ChroniclerMemory.counter("note_" + customId) == 1, "unknown id marker survives reload unchanged");
        check(!(Boolean) field("savePending"), "reloading accepted notes schedules no save");
    }

    private static void counterBoundaries(Path root) throws Exception {
        Path primary = newConfig(root.resolve("counter-bounds"));
        Files.writeString(primary, "{\"version\":3,\"counters\":{\"max\":2147483647,\"near\":2147483646,\"negative\":-9,\"min\":-2147483648}}");
        ChroniclerMemory.get();
        check(ChroniclerMemory.bump("max") == Integer.MAX_VALUE, "maximum counter saturates rather than wraps");
        check(!(Boolean) field("savePending"), "saturated no-op does not schedule a save");
        check(ChroniclerMemory.bump("near") == Integer.MAX_VALUE, "increment reaches maximum");
        for (int i = 0; i < 5; i++) check(ChroniclerMemory.bump("near") == Integer.MAX_VALUE, "maximum remains stable");
        check(ChroniclerMemory.bump("negative") == -8, "signed counter increments normally");
        check(ChroniclerMemory.bump("min") == Integer.MIN_VALUE + 1, "minimum signed counter increments normally");
        check(ChroniclerMemory.bump("new") == 1, "new counter starts at one");
        ChroniclerMemory.get().counters.put("null-value", null);
        check(ChroniclerMemory.bump("null-value") == 1, "missing/null value starts at one");
        writeNow();
        JsonObject counters = read(primary).getAsJsonObject("counters");
        check(counters.get("max").getAsInt() == Integer.MAX_VALUE, "maximum survives persistence");
        check(counters.get("near").getAsInt() == Integer.MAX_VALUE, "saturated increment survives persistence");
        check(counters.get("negative").getAsInt() == -8, "signed increment survives persistence");
    }

    private static void sessionCase(Path root, String name, long firstDay, long lastDay,
                                    long visits, int streak, long expectedVisits, int expectedStreak,
                                    long expectedAbsence, int expectedKind, boolean dust, boolean anniversary) throws Exception {
        final long today = 20000L;
        Path primary = newConfig(root.resolve("session-" + name));
        JsonObject source = new JsonObject();
        source.addProperty("version", 3);
        source.addProperty("firstVisitDay", firstDay);
        source.addProperty("lastVisitDay", lastDay);
        source.addProperty("visitCount", visits);
        source.addProperty("streak", streak);
        // Existing rewards keep this a memory/filesystem test, with no new
        // discovery toast/audio/client initialization during long streaks.
        JsonArray discovered = new JsonArray();
        discovered.add(Discoveries.STREAK3);
        source.add("discovered", discovered);
        JsonObject counters = new JsonObject();
        counters.addProperty("note_" + Marginalia.N_STREAK7, 1);
        source.add("counters", counters);
        Files.writeString(primary, source.toString());
        var begin = ChroniclerMemory.class.getDeclaredMethod("beginSessionOn", long.class);
        begin.setAccessible(true);
        synchronized (ChroniclerMemory.class) {
            begin.invoke(null, today);
        }
        ChroniclerMemory.Data data = ChroniclerMemory.get();
        check(data.visitCount == expectedVisits, name + ": visits saturate/increment");
        check(data.streak == expectedStreak, name + ": streak saturates/increments/resets");
        check(data.lastVisitDay == today, name + ": last visit advances to current day");
        long expectedFirst = firstDay >= 0 ? firstDay : lastDay >= 0 ? lastDay : today;
        check(data.firstVisitDay == expectedFirst, name + ": first visit is preserved or initialized");
        check(ChroniclerMemory.sessionAbsenceDays() == expectedAbsence, name + ": absence days");
        check(ChroniclerMemory.sessionGreetingKind() == expectedKind, name + ": greeting kind");
        check(data.dustPending == dust, name + ": dust only for a verified week-long absence");
        check(data.anniversaryPending == anniversary, name + ": anniversary remains exact");
        check((Boolean) field("savePending"), name + ": session changes request persistence");
        writeNow();
        set("data", null);
        data = ChroniclerMemory.get();
        check(data.visitCount == expectedVisits && data.streak == expectedStreak, name + ": counts survive real reload");
        check(data.lastVisitDay == today && data.firstVisitDay == expectedFirst, name + ": dates survive real reload");
        check(data.dustPending == dust && data.anniversaryPending == anniversary, name + ": reminders survive reload");
        check(ChroniclerMemory.consumeDust() == dust, name + ": dust consumed only if pending");
        check(!ChroniclerMemory.consumeDust(), name + ": dust consumed once");
        check(ChroniclerMemory.consumeAnniversary() == anniversary, name + ": anniversary consumed only if pending");
        check(!ChroniclerMemory.consumeAnniversary(), name + ": anniversary consumed once");
        writeNow();
    }

    private static void sessionBoundaries(Path root) throws Exception {
        sessionCase(root, "first", -1, -1, 0, 0, 1, 1, 0, 1, false, false);
        sessionCase(root, "near-max", 19000, 19999, Long.MAX_VALUE - 1, Integer.MAX_VALUE - 1,
                    Long.MAX_VALUE, Integer.MAX_VALUE, 1, 2, false, false);
        sessionCase(root, "at-max", 19000, 19999, Long.MAX_VALUE, Integer.MAX_VALUE,
                    Long.MAX_VALUE, Integer.MAX_VALUE, 1, 2, false, false);
        sessionCase(root, "same-day", 19000, 20000, 41, Integer.MAX_VALUE,
                    42, Integer.MAX_VALUE, 0, 3, false, false);
        sessionCase(root, "two-day-gap", 19000, 19998, 41, 9, 42, 1, 2, 2, false, false);
        sessionCase(root, "six-day-gap", 19000, 19994, 41, 9, 42, 1, 6, 2, false, false);
        sessionCase(root, "week-gap", 19000, 19993, 41, 9, 42, 1, 7, 2, true, false);
        sessionCase(root, "anniversary", 19635, 19999, 41, 1, 42, 2, 1, 2, false, true);
        sessionCase(root, "missing-last", 19000, -1, 41, 4, 42, 4, 0, 3, false, false);
        sessionCase(root, "future-last", 19000, Long.MAX_VALUE, 41, 4, 42, 4, 0, 3, false, false);
        sessionCase(root, "future-first", Long.MAX_VALUE, 19999, 41, 1, 42, 2, 1, 2, false, false);
        sessionCase(root, "missing-first", -1, 19999, 41, 1, 42, 2, 1, 2, false, false);
    }

    private static void invalidOldVersions(Path root) throws Exception {
        int index = 0;
        for (int version : new int[] {0, -1, -100, Integer.MIN_VALUE}) {
            Path primary = newConfig(root.resolve("version-" + index++));
            Files.writeString(primary, "{\"version\":" + version + ",\"counters\":{\"kept\":42}}");
            check(ChroniclerMemory.get().version == 3, "invalid old schema normalizes before supported migration");
            check(ChroniclerMemory.counter("kept") == 42, "invalid old version retains progress");
            check(!(Boolean) field("readOnlyForNewerSchema"), "invalid old version is not mistaken for a future schema");
            writeNow();
            check(read(primary).get("version").getAsInt() == 3, "normalized version persists as current schema");
        }
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("gripeverde-memory-data-");
        try {
            collectionEntries(root);
            invalidApiIds(root);
            pendingNoteApis(root);
            counterBoundaries(root);
            sessionBoundaries(root);
            invalidOldVersions(root);
            ChroniclerMemory.flushForShutdown();
            System.out.println("Memory data: " + checks + " checks passed");
        } finally {
            ChroniclerMemory.flushForShutdown();
            try (var files = Files.walk(root)) {
                for (Path path : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }
}
