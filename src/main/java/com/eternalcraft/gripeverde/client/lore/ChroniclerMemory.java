package com.eternalcraft.gripeverde.client.lore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
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
        // v0.24.0: ultima version de la cronica que el lector ya vio al abrirla.
        // null en memorias viejas (GSON no la rellena); los accesores la sanea.
        public String lastSeenVersion = null;
        // v0.39.0: fallback por pixeles para memorias anteriores.
        public int fieldGuideScrollY = 0;
        // v0.40.0: posicion semantica adaptable al tamaño de ventana.
        public int fieldGuideSection = -1;
        public int fieldGuideProgress = 0;
    }

    private static Data data = null;
    private static boolean readOnlyForNewerSchema;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    private static final String MEMORY_FILE = "gripeverde-memory.json";
    private static final String BACKUP_FILE = "gripeverde-memory.backup.json";
    private static final String TEMP_FILE = "gripeverde-memory.tmp";
    private static final String CORRUPT_FILE = "gripeverde-memory.corrupt.json";

    /**
     * v0.23.1: version del esquema de la memoria. Cuando el esquema crezca hay
     * que subir esta constante y encadenar el paso N -> N+1 en migrate().
     */
    static final int MAX_PENDING_NOTES = 256;
    private static final int MAX_FIELD_GUIDE_SCROLL = 100000;
    private static final int MAX_FIELD_GUIDE_SECTION = 100;
    private static final int MAX_FIELD_GUIDE_PROGRESS = 10000;
    private static final int SCHEMA_VERSION = 3;

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
        // v0.23.1: bounds. Un JSON editado a mano (o corrompido logicamente)
        // puede ser JSON valido y aun asi traer numeros imposibles.
        if (parsed.visitCount < 0L) parsed.visitCount = 0L;
        if (parsed.streak < 0) parsed.streak = 0;
        parsed.fieldGuideScrollY = Math.max(0, Math.min(MAX_FIELD_GUIDE_SCROLL, parsed.fieldGuideScrollY));
        if (parsed.fieldGuideSection < -1 || parsed.fieldGuideSection > MAX_FIELD_GUIDE_SECTION) {
            parsed.fieldGuideSection = -1;
        }
        parsed.fieldGuideProgress = parsed.fieldGuideSection < 0 ? 0
            : Math.max(0, Math.min(MAX_FIELD_GUIDE_PROGRESS, parsed.fieldGuideProgress));
        if (parsed.firstVisitDay < -1L) parsed.firstVisitDay = -1L;
        if (parsed.lastVisitDay < -1L) parsed.lastVisitDay = -1L;
        if (parsed.firstVisitDay < 0L && parsed.lastVisitDay >= 0L) {
            parsed.firstVisitDay = parsed.lastVisitDay;
        }
        parsed.discovered.removeIf(id -> !validId(id));
        parsed.collectibles.removeIf(id -> !validId(id));
        // Limpiar antes de aplicar los limites: los huecos no deben desplazar
        // notas/firmas validas que aparezcan despues en un archivo antiguo.
        parsed.pendingNotes.removeIf(id -> !validId(id));
        parsed.guestbook.removeIf(java.util.Objects::isNull);
        parsed.counters.entrySet().removeIf(entry -> !validId(entry.getKey()) || entry.getValue() == null);
        if (parsed.pendingNotes.size() > MAX_PENDING_NOTES) {
            parsed.pendingNotes = new ArrayList<>(parsed.pendingNotes.subList(0, MAX_PENDING_NOTES));
        }
        if (parsed.guestbook.size() > 4096) {
            parsed.guestbook = new ArrayList<>(parsed.guestbook.subList(0, 4096));
        }
        migrate(parsed);
        return parsed;
    }

    /**
     * Pipeline de migracion versionada (v0.23.1). Cuando el esquema suba,
     * SCHEMA_VERSION crece y aqui se encadenan pasos N -> N+1 idempotentes:
     *
     *   while (parsed.version < SCHEMA_VERSION) {
     *       switch (parsed.version) { case 1: ...; }
     *       parsed.version++;
     *   }
     *
     * Un archivo escrito por una version MAS NUEVA del mod (esquema mayor)
     * se conserva sin tocar y solo deja aviso en el log.
     */
    private static void migrate(Data parsed) {
        if (parsed.version > SCHEMA_VERSION) {
            LOGGER.warn("[GripeVerde] La memoria usa el esquema {} pero este mod entiende {}; se conserva sin migrar",
                parsed.version, SCHEMA_VERSION);
            return;
        }
        // No recorrer miles de millones de pasos desde un version negativo.
        // El primer esquema soportado es 1, tambien para metadatos invalidos.
        parsed.version = Math.max(1, parsed.version);
        while (parsed.version < SCHEMA_VERSION) {
            // v0.40.0 agrega un ancla de capitulo; los datos viejos conservan el fallback por pixeles.
            parsed.version++;
        }
    }

    private static final class NewerSchemaException extends IOException {
        private NewerSchemaException(String version) {
            super("Esquema de memoria no soportado: " + version);
        }
    }

    private static void rejectNewerSchema(JsonElement root) throws NewerSchemaException {
        if (root == null || !root.isJsonObject()) return;
        JsonElement version = root.getAsJsonObject().get("version");
        if (version == null || !version.isJsonPrimitive()) return;
        try {
            // Comprobar antes de deserializar Data: una version futura puede
            // cambiar tambien el tipo de campos que este mod ya conoce.
            if (new BigDecimal(version.getAsString()).compareTo(BigDecimal.valueOf(SCHEMA_VERSION)) > 0) {
                throw new NewerSchemaException(version.getAsString());
            }
        } catch (NumberFormatException invalidVersion) {
            // Los metadatos invalidos siguen la recuperacion habitual de JSON.
        }
    }

    private static void protectNewerSchema(Path path, NewerSchemaException reason) {
        if (!readOnlyForNewerSchema) {
            LOGGER.warn("[GripeVerde] {} en {}. Memoria de solo lectura durante esta sesion; no se guardaran cambios.",
                reason.getMessage(), path);
        }
        readOnlyForNewerSchema = true;
        savePending = false;
    }

    /** Revisar tambien temporal y respaldo antes de truncarlos o reemplazarlos. */
    private static boolean canWriteKnownSchemas() {
        if (readOnlyForNewerSchema) return false;
        if (data != null && data.version > SCHEMA_VERSION) {
            protectNewerSchema(configPath(MEMORY_FILE), new NewerSchemaException(Integer.toString(data.version)));
            return false;
        }
        for (String file : new String[] {MEMORY_FILE, TEMP_FILE, BACKUP_FILE}) {
            Path path = configPath(file);
            if (!Files.isRegularFile(path)) continue;
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                rejectNewerSchema(JsonParser.parseReader(reader));
            } catch (NewerSchemaException newer) {
                protectNewerSchema(path, newer);
                return false;
            } catch (Exception unreadable) {
                // No confundir JSON danado con un esquema conocido mas nuevo.
                // load/writeNow conservan su tratamiento de copias invalidas.
            }
        }
        return true;
    }

    private static Data readData(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            rejectNewerSchema(root);
            return sanitize(GSON.fromJson(root, Data.class));
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
        if (!canWriteKnownSchemas()) return;
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
            } catch (NewerSchemaException newer) {
                protectNewerSchema(primary, newer);
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
            } catch (NewerSchemaException newer) {
                protectNewerSchema(temporary, newer);
                return;
            } catch (Throwable temporaryFailure) {
                LOGGER.warn("[GripeVerde] El temporal de memoria interrumpido no es valido", temporaryFailure);
            }
        }
        if (recovered == null && backupExists) {
            try {
                recovered = readData(backup);
                recoveredFrom = BACKUP_FILE;
            } catch (NewerSchemaException newer) {
                protectNewerSchema(backup, newer);
                return;
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
        if (data == null || readOnlyForNewerSchema) return;
        // Escritura diferida (maximo una cada 2 s); el cierre fuerza lo pendiente.
        savePending = true;
        long now = System.currentTimeMillis();
        if (now - lastSaveMs < 2000L) return;
        lastSaveMs = now;
        scheduleWrite();
    }

    /**
     * v0.23.0: la escritura (archivo + force/fsync) sale del hilo del cliente.
     * Antes ocurria en el hilo de render y podia producir micro-tirones en
     * discos lentos. writeNow() es synchronized, asi que el executor serializa
     * con cualquier escritura en linea y el orden "ultima copia gana" se
     * conserva. Hilo daemon con nombre propio para no retener el JVM.
     */
    private static final java.util.concurrent.ExecutorService WRITER =
        java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "gripeverde-memory-writer");
            t.setDaemon(true);
            return t;
        });

    private static void scheduleWrite() {
        try {
            WRITER.execute(ChroniclerMemory::writeNow);
        } catch (Throwable rejected) {
            // Executor ya en parada (cierre del JVM): escribir en linea como antes.
            writeNow();
        }
    }

    /**
     * Escribe primero un temporal durable, preserva la ultima version valida y
     * reemplaza el JSON principal atomicamente cuando el sistema lo permite.
     */
    private static synchronized void writeNow() {
        if (data == null || !canWriteKnownSchemas()) return;
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
                } catch (NewerSchemaException newer) {
                    protectNewerSchema(primary, newer);
                    return;
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
    public static void flushForShutdown() {
        // No mantener el monitor durante awaitTermination: writeNow tambien
        // lo necesita y el worker no podria terminar mientras esperamos aqui.
        WRITER.shutdown();
        try {
            WRITER.awaitTermination(3L, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        // El worker pudo completar la escritura mientras esperabamos. Leer el
        // pendiente y hacer el ultimo intento bajo el mismo monitor que el.
        synchronized (ChroniclerMemory.class) {
            if (savePending) writeNow();
        }
    }

    // ── Sesiones ────────────────────────────────────────────────────────────

    /** Debe llamarse UNA vez por sesion (init del Title). Calcula saludo, racha, polvo. */
    public static synchronized void beginSession() {
        beginSessionOn(LocalDate.now().toEpochDay());
    }

    /** Called under the memory monitor; explicit day keeps boundary checks deterministic. */
    private static void beginSessionOn(long today) {
        Data d = get();
        if (d.visitCount < Long.MAX_VALUE) d.visitCount++;

        if (d.firstVisitDay < 0) {
            d.firstVisitDay = today;
            d.lastVisitDay = today;
            d.streak = 1;
            sessionGreetingKind = 1;
            sessionAbsenceDays = 0L;
        } else {
            long prev = d.lastVisitDay;
            // Una fecha ausente no demuestra una ausencia larga; una fecha
            // futura (p. ej. por cambio del reloj) tampoco cuenta dias negativos.
            sessionAbsenceDays = prev < 0L ? 0L : Math.max(0L, today - prev);
            if (sessionAbsenceDays == 0L) {
                sessionGreetingKind = 3; // mismo dia o sin ausencia comprobable
            } else if (sessionAbsenceDays == 1L) {
                if (d.streak < Integer.MAX_VALUE) d.streak++;
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

    private static boolean validId(String id) {
        return id != null && !id.isBlank();
    }

    // ── Descubrimientos ─────────────────────────────────────────────────────

    public static synchronized boolean isDiscovered(String id) {
        return validId(id) && get().discovered.contains(id);
    }

    public static synchronized void markDiscovered(String id) {
        if (!validId(id)) return;
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
        return validId(id) && get().collectibles.contains(id);
    }

    public static synchronized void addCollectible(String id) {
        if (!validId(id)) return;
        Data d = get();
        if (d.collectibles.add(id)) save();
    }

    public static synchronized int collectibleCount() {
        return get().collectibles.size();
    }

    // ── Contadores ──────────────────────────────────────────────────────────

    public static synchronized int counter(String key) {
        if (!validId(key)) return 0;
        Integer v = get().counters.get(key);
        return v == null ? 0 : v;
    }

    /** Incrementa un contador persistente y devuelve su nuevo valor. */
    public static synchronized int bump(String key) {
        if (!validId(key)) return 0;
        Data d = get();
        Integer v = d.counters.get(key);
        if (v != null && v == Integer.MAX_VALUE) return Integer.MAX_VALUE;
        int nv = (v == null ? 0 : v) + 1;
        d.counters.put(key, nv);
        save();
        return nv;
    }

    public static synchronized void setCounter(String key, int value) {
        if (!validId(key)) return;
        Data d = get();
        d.counters.put(key, value);
        save();
    }

    // ── v0.24.0: cronica vista ──────────────────────────────────────────────

    /** Ultima version de la cronica cuya entrada se mostro sellada como nueva. */
    public static synchronized String lastSeenVersion() {
        String v = get().lastSeenVersion;
        return v == null ? "" : v;
    }

    public static synchronized void setLastSeenVersion(String v) {
        get().lastSeenVersion = (v == null ? "" : v);
        save();
    }

    // ── v0.40.0: marcador semantico del manual de campo ───────────────────

    public static synchronized int fieldGuideScrollY() {
        return get().fieldGuideScrollY;
    }

    public static synchronized int fieldGuideSection() {
        return get().fieldGuideSection;
    }

    public static synchronized int fieldGuideProgress() {
        return get().fieldGuideProgress;
    }

    public static synchronized void setFieldGuideBookmark(int section, int progress, int scrollY) {
        int safeSection = section < 0 || section > MAX_FIELD_GUIDE_SECTION ? -1 : section;
        int safeProgress = safeSection < 0 ? 0
            : Math.max(0, Math.min(MAX_FIELD_GUIDE_PROGRESS, progress));
        int safeScrollY = Math.max(0, Math.min(MAX_FIELD_GUIDE_SCROLL, scrollY));
        Data d = get();
        if (d.fieldGuideSection != safeSection || d.fieldGuideProgress != safeProgress
                || d.fieldGuideScrollY != safeScrollY) {
            d.fieldGuideSection = safeSection;
            d.fieldGuideProgress = safeProgress;
            d.fieldGuideScrollY = safeScrollY;
            save();
        }
    }
}
