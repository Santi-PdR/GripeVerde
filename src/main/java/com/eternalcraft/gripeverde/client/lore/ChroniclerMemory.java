package com.eternalcraft.gripeverde.client.lore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * La memoria del gabinete.
 *
 * Persistencia propia (config/gripeverde-memory.json), separada del TOML de
 * audio, para que los descubrimientos, objetos y habitos del lector queden
 * grabados entre sesiones. El cronista recuerda.
 *
 * v0.16.0 - El pase profundo.
 */
public final class ChroniclerMemory {

    private ChroniclerMemory() {}

    public static class Data {
        public int version = 1;
        public long firstVisitDay = -1L;      // epochDay de la primera apertura
        public long lastVisitDay = -1L;       // epochDay de la ultima apertura
        public long visitCount = 0L;          // aperturas totales del juego
        public int streak = 0;                // noches consecutivas
        public boolean dustPending = false;   // polvo por ausencia larga
        public boolean anniversaryPending = false;
        public Set<String> discovered = new HashSet<>();
        public Set<String> collectibles = new HashSet<>();
        public List<String> pendingNotes = new ArrayList<>();
        public Map<String, Integer> counters = new HashMap<>();
        public List<String> guestbook = new ArrayList<>();
    }

    private static Data data = null;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Resultados del saludo de apertura de sesion (calculado en beginSession)
    private static int sessionGreetingKind = 0; // 0=ninguno 1=primera 2=regreso 3=mismo dia
    private static long sessionAbsenceDays = 0L;

    public static synchronized Data get() {
        if (data == null) load();
        return data;
    }

    private static File memoryFile() {
        try {
            java.nio.file.Path dir = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();
            return new File(dir.toFile(), "gripeverde-memory.json");
        } catch (Throwable t) {
            return new File("config", "gripeverde-memory.json");
        }
    }

    private static synchronized void load() {
        data = new Data();
        File f = memoryFile();
        if (!f.isFile()) return;
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
            Data parsed = GSON.fromJson(reader, Data.class);
            if (parsed != null) {
                data = parsed;
                if (data.discovered == null) data.discovered = new HashSet<>();
                if (data.collectibles == null) data.collectibles = new HashSet<>();
                if (data.pendingNotes == null) data.pendingNotes = new ArrayList<>();
                if (data.counters == null) data.counters = new HashMap<>();
                if (data.guestbook == null) data.guestbook = new ArrayList<>();
            }
        } catch (Throwable t) {
            // Memoria corrupta: el cronista empieza de cero, sin drama.
            data = new Data();
        } finally {
            try { if (reader != null) reader.close(); } catch (Throwable ignored) {}
        }
    }

    public static synchronized void save() {
        if (data == null) return;
        // v0.21.11: escritura diferida (max 1 por 2s). El cierre del juego
        // dispara flushForShutdown() via ShutdownHook para no perder nada.
        savePending = true;
        long now = System.currentTimeMillis();
        if (now - lastSaveMs < 2000L) return;
        lastSaveMs = now;
        savePending = false;
        writeNow();
    }

    /** v0.21.11: escritura inmediata al disco. */
    private static synchronized void writeNow() {
        if (data == null) return;
        File f = memoryFile();
        OutputStreamWriter writer = null;
        try {
            File parent = f.getParentFile();
            if (parent != null && !parent.isDirectory() && !parent.mkdirs()) return;
            writer = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8);
            GSON.toJson(data, writer);
            writer.flush();
        } catch (Throwable ignored) {
        } finally {
            try { if (writer != null) writer.close(); } catch (Throwable ignored) {}
        }
    }

    private static boolean savePending = false;
    private static long lastSaveMs = 0L;

    /** v0.21.11: al cerrar el juego se escribe lo pendiente. */
    public static void flushForShutdown() {
        if (savePending) {
            savePending = false;
            writeNow();
        }
    }

    // ── Sesiones ────────────────────────────────────────────────────────────

    /** Debe llamarse UNA vez por sesion (init del Title). Calcula saludo, racha, polvo. */
    public static synchronized void beginSession() {
        Data d = get();
        long today = LocalDate.now().toEpochDay();
        d.visitCount++;

        if (d.firstVisitDay < 0) {
            d.firstVisitDay = today;
            d.lastVisitDay = today;
            d.streak = 1;
            sessionGreetingKind = 1;
            sessionAbsenceDays = 0L;
        } else {
            long prev = d.lastVisitDay;
            sessionAbsenceDays = today - prev;
            if (sessionAbsenceDays <= 0) {
                sessionGreetingKind = 3; // mismo dia, otra vigilia
            } else if (sessionAbsenceDays == 1L) {
                d.streak++;
                sessionGreetingKind = 2;
            } else {
                d.streak = 1;
                sessionGreetingKind = 2;
                if (sessionAbsenceDays >= 7L) d.dustPending = true;
            }
            d.lastVisitDay = today;

            // Aniversario exacto de la primera apertura
            if (today - d.firstVisitDay == 365L && !d.discovered.contains(Discoveries.ANNIVERSARY)) {
                d.anniversaryPending = true;
            }
            // Racha de tres noches
            if (d.streak >= 3) {
                if (Discoveries.discover(Discoveries.STREAK3)) {
                    Marginalia.queue(Marginalia.N_STREAK3);
                }
            }
            if (d.streak >= 7) {
                Marginalia.queueOnce(Marginalia.N_STREAK7);
            }
        }
        save();
    }

    public static int sessionGreetingKind() { return sessionGreetingKind; }
    public static long sessionAbsenceDays() { return sessionAbsenceDays; }

    /** Consume el polvo pendiente por ausencia (devuelve true una sola vez). */
    public static synchronized boolean consumeDust() {
        Data d = get();
        if (d.dustPending) {
            d.dustPending = false;
            save();
            return true;
        }
        return false;
    }

    public static synchronized boolean consumeAnniversary() {
        Data d = get();
        if (d.anniversaryPending) {
            d.anniversaryPending = false;
            save();
            return true;
        }
        return false;
    }

    // ── Descubrimientos ─────────────────────────────────────────────────────

    public static synchronized boolean isDiscovered(String id) {
        return get().discovered.contains(id);
    }

    public static synchronized void markDiscovered(String id) {
        Data d = get();
        if (d.discovered.add(id)) save();
    }

    public static synchronized int discoveredCount() {
        return get().discovered.size();
    }

    // ── Objetos del cajon ───────────────────────────────────────────────────

    public static synchronized boolean hasCollectible(String id) {
        return get().collectibles.contains(id);
    }

    public static synchronized void addCollectible(String id) {
        Data d = get();
        if (d.collectibles.add(id)) save();
    }

    public static synchronized int collectibleCount() {
        return get().collectibles.size();
    }

    // ── Contadores ──────────────────────────────────────────────────────────

    public static synchronized int counter(String key) {
        Integer v = get().counters.get(key);
        return v == null ? 0 : v;
    }

    /** Incrementa un contador persistente y devuelve su nuevo valor. */
    public static synchronized int bump(String key) {
        Data d = get();
        Integer v = d.counters.get(key);
        int nv = (v == null ? 0 : v) + 1;
        d.counters.put(key, nv);
        save();
        return nv;
    }

    public static synchronized void setCounter(String key, int value) {
        Data d = get();
        d.counters.put(key, value);
        save();
    }
}
