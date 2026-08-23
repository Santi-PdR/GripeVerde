package com.eternalcraft.gripeverde.client.lore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
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
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    private static final String MEMORY_FILE = "gripeverde-memory.json";
    private static final String BACKUP_FILE = "gripeverde-memory.backup.json";
    private static final String TEMP_FILE = "gripeverde-memory.tmp";
    private static final String CORRUPT_FILE = "gripeverde-memory.corrupt.json";

    // Resultados del saludo de apertura de sesion (calculado en beginSession)
    private static int sessionGreetingKind = 0; // 0=ninguno 1=primera 2=regreso 3=mismo dia
    private static long sessionAbsenceDays = 0L;

    public static synchronized Data get() {
        if (data == null) load();
        return data;
    }

    private static Path configPath(String fileName) {
        try {
            return net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve(fileName);
        } catch (Throwable t) {
            return Path.of("config", fileName);
        }
    }

    private static Data sanitize(Data parsed) throws IOException {
        if (parsed == null) throw new IOException("El JSON de memoria esta vacio");
        if (parsed.discovered == null) parsed.discovered = new HashSet<>();
        if (parsed.collectibles == null) parsed.collectibles = new HashSet<>();
        if (parsed.pendingNotes == null) parsed.pendingNotes = new ArrayList<>();
        if (parsed.counters == null) parsed.counters = new HashMap<>();
        if (parsed.guestbook == null) parsed.guestbook = new ArrayList<>();
        return parsed;
    }

    private static Data readData(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return sanitize(GSON.fromJson(reader, Data.class));
        }
    }

    private static void preserveCorruptFile(Path primary) {
        if (!Files.isRegularFile(primary)) return;
        Path corrupt = configPath(CORRUPT_FILE);
        try {
            Files.move(primary, corrupt, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException moveFailure) {
            try {
                Files.copy(primary, corrupt, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException copyFailure) {
                LOGGER.warn("[GripeVerde] No se pudo preservar la memoria corrupta", copyFailure);
            }
        }
    }

    private static synchronized void load() {
        data = new Data();
        Path primary = configPath(MEMORY_FILE);
        Path temporary = configPath(TEMP_FILE);
        Path backup = configPath(BACKUP_FILE);
        boolean primaryExists = Files.isRegularFile(primary);
        boolean temporaryExists = Files.isRegularFile(temporary);
        boolean backupExists = Files.isRegularFile(backup);

        if (primaryExists) {
            try {
                data = readData(primary);
                return;
            } catch (Throwable primaryFailure) {
                primaryKnownCorrupt = true;
                LOGGER.warn("[GripeVerde] La memoria principal no es valida; intentando recuperar otra copia", primaryFailure);
            }
        } else if (!temporaryExists && !backupExists) {
            return;
        }

        Data recovered = null;
        String recoveredFrom = null;
        if (temporaryExists) {
            try {
                recovered = readData(temporary);
                recoveredFrom = TEMP_FILE;
            } catch (Throwable temporaryFailure) {
                LOGGER.warn("[GripeVerde] El temporal de memoria interrumpido no es valido", temporaryFailure);
            }
        }
        if (recovered == null && backupExists) {
            try {
                recovered = readData(backup);
                recoveredFrom = BACKUP_FILE;
            } catch (Throwable backupFailure) {
                LOGGER.error("[GripeVerde] El respaldo de memoria tampoco es valido", backupFailure);
            }
        }

        if (primaryExists) preserveCorruptFile(primary);
        if (recovered != null) {
            data = recovered;
            savePending = true;
            writeNow();
            LOGGER.warn("[GripeVerde] Memoria recuperada desde {}", recoveredFrom);
        } else {
            data = new Data();
            LOGGER.error("[GripeVerde] No existe una memoria valida; se iniciara un registro nuevo. Los archivos dañados no se usaron.");
        }
    }

    public static synchronized void save() {
        if (data == null) return;
        // Escritura diferida (maximo una cada 2 s); el cierre fuerza lo pendiente.
        savePending = true;
        long now = System.currentTimeMillis();
        if (now - lastSaveMs < 2000L) return;
        lastSaveMs = now;
        writeNow();
    }

    /**
     * Escribe primero un temporal durable, preserva la ultima version valida y
     * reemplaza el JSON principal atomicamente cuando el sistema lo permite.
     */
    private static synchronized void writeNow() {
        if (data == null) return;
        Path primary = configPath(MEMORY_FILE);
        Path backup = configPath(BACKUP_FILE);
        Path temporary = configPath(TEMP_FILE);
        try {
            Path parent = primary.getParent();
            if (parent != null) Files.createDirectories(parent);

            byte[] json = (GSON.toJson(data) + "\n").getBytes(StandardCharsets.UTF_8);
            try (FileChannel channel = FileChannel.open(temporary,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                ByteBuffer buffer = ByteBuffer.wrap(json);
                while (buffer.hasRemaining()) channel.write(buffer);
                channel.force(true);
            }

            if (!primaryKnownCorrupt && Files.isRegularFile(primary)) {
                try {
                    // Never replace a known-good backup with an externally damaged primary.
                    readData(primary);
                    try {
                        Files.copy(primary, backup, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException backupFailure) {
                        // A blocked backup must not prevent the new atomic save from completing.
                        LOGGER.warn("[GripeVerde] No se pudo actualizar el respaldo de memoria", backupFailure);
                    }
                } catch (Throwable invalidPrimary) {
                    primaryKnownCorrupt = true;
                    LOGGER.warn("[GripeVerde] La memoria principal cambio fuera del juego y no es valida; no se reemplazara el respaldo", invalidPrimary);
                    preserveCorruptFile(primary);
                }
            }

            try {
                Files.move(temporary, primary,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, primary, StandardCopyOption.REPLACE_EXISTING);
            }

            primaryKnownCorrupt = false;
            savePending = false;
        } catch (Throwable failure) {
            savePending = true;
            LOGGER.error("[GripeVerde] No se pudo guardar la memoria del cronista", failure);
            try { Files.deleteIfExists(temporary); } catch (IOException ignored) {}
        }
    }

    private static boolean savePending = false;
    private static boolean primaryKnownCorrupt = false;
    private static long lastSaveMs = 0L;

    /** Al cerrar el juego se escribe todo cambio pendiente. */
    public static synchronized void flushForShutdown() {
        if (savePending) writeNow();
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
        Data data = get();
        int count = 0;
        for (String id : Discoveries.ALL) {
            if (data.discovered.contains(id)) count++;
        }
        return count;
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
