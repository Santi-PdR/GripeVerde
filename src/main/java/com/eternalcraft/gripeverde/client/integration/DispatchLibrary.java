package com.eternalcraft.gripeverde.client.integration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Los mensajes guardados del parte de CEDA, con importacion desde CopyL.
 *
 * v0.181.0: hasta ahora habia que tener CopyL instalado para conservar los
 * mensajes rapidos (config/copyl-messages.json, diez ranuras con nombre, texto y
 * tecla). Este almacen lee ese archivo una sola vez, se queda con una copia
 * propia en config/gripeverde-dispatch.json y a partir de ahi ya no necesita a
 * CopyL en mods. Nunca escribe ni borra nada de CopyL.
 *
 * Formato propio (mismo espiritu que el de CopyL, para que la copia sea legible):
 * { "importedFrom": "...", "openKey": 344, "slots": [ { "name", "message", "key" } ] }
 */
public final class DispatchLibrary {

    private static final Logger LOGGER = LogManager.getLogger("GripeVerde/DispatchLibrary");

    // v0.185.0: veinte voces en vez de diez. El formato del archivo no cambia:
    // las que ya existian conservan su sitio y las nuevas entran vacias.
    public static final int SLOT_COUNT = 20;
    public static final int UNBOUND = -1;
    private static final int MOUSE_BASE = 1000;
    private static final int MAX_MESSAGE_LENGTH = 256;
    private static final int MAX_NAME_LENGTH = 24;

    private static final String OWN_FILE = "gripeverde-dispatch.json";
    private static final String COPYL_MESSAGES = "copyl-messages.json";
    private static final String COPYL_CONFIG = "copyl.json";
    private static final String LEGACY_CONFIG = "lclient.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String[] names = new String[SLOT_COUNT];
    private static final String[] messages = new String[SLOT_COUNT];
    private static final int[] keys = new int[SLOT_COUNT];
    private static int openKey = GLFW.GLFW_KEY_RIGHT_ALT;
    private static String importedFrom = "";
    private static boolean loaded;
    private static boolean pendingSave;

    private DispatchLibrary() {
    }

    public static synchronized void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        for (int i = 0; i < SLOT_COUNT; i++) {
            names[i] = defaultName(i);
            messages[i] = "";
            keys[i] = UNBOUND;
        }

        Path own = configDir().resolve(OWN_FILE);
        if (Files.exists(own)) {
            if (readOwn(own)) return;
            LOGGER.warn("No pude leer {}; intento importar de CopyL.", own);
        }
        importFromCopyL();
    }

    private static Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    // ---------------------------------------------------------------- lectura

    private static boolean readOwn(Path own) {
        try (Reader reader = Files.newBufferedReader(own, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            openKey = readBinding(root.get("openKey"), GLFW.GLFW_KEY_RIGHT_ALT);
            importedFrom = readText(root.get("importedFrom"), "");
            JsonArray slots = readArray(root, "slots");
            for (int i = 0; i < SLOT_COUNT && i < slots.size(); i++) {
                JsonElement entry = slots.get(i);
                if (!entry.isJsonObject()) continue;
                JsonObject slot = entry.getAsJsonObject();
                names[i] = normalizeName(readText(slot.get("name"), null), i);
                messages[i] = normalizeMessage(readText(slot.get("message"), ""));
                keys[i] = readBinding(slot.get("key"), UNBOUND);
            }
            return true;
        } catch (Exception exception) {
            LOGGER.warn("No pude leer {}: {}", own, exception.getMessage());
            return false;
        }
    }

    /**
     * Lee copyl-messages.json (y el openKey de copyl.json / lclient.json) una sola
     * vez y guarda la copia propia. Si no hay nada de CopyL, deja las ranuras
     * vacias con sus nombres por defecto y no escribe ningun archivo ajeno.
     */
    private static void importFromCopyL() {
        Path copyMessages = configDir().resolve(COPYL_MESSAGES);
        boolean imported = false;
        if (Files.exists(copyMessages)) {
            try (Reader reader = Files.newBufferedReader(copyMessages, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray rawNames = readArray(root, "names");
                JsonArray rawMessages = readArray(root, "messages");
                JsonArray rawKeys = readArray(root, "keyCodes");
                for (int i = 0; i < SLOT_COUNT; i++) {
                    if (i < rawNames.size()) names[i] = normalizeName(readText(rawNames.get(i), null), i);
                    if (i < rawMessages.size()) messages[i] = normalizeMessage(readText(rawMessages.get(i), ""));
                    if (i < rawKeys.size()) keys[i] = readBinding(rawKeys.get(i), UNBOUND);
                }
                imported = true;
                importedFrom = COPYL_MESSAGES;
                LOGGER.info("Mensajes del parte importados desde {} ({} ranuras con texto).",
                    copyMessages, countWithText());
            } catch (Exception exception) {
                LOGGER.warn("No pude importar {}: {}", copyMessages, exception.getMessage());
            }
        }

        Path copyConfig = configDir().resolve(COPYL_CONFIG);
        if (!Files.exists(copyConfig)) copyConfig = configDir().resolve(LEGACY_CONFIG);
        if (Files.exists(copyConfig)) {
            try (Reader reader = Files.newBufferedReader(copyConfig, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (root.has("openKey") && root.get("openKey").isJsonPrimitive()) {
                    openKey = readBinding(root.get("openKey"), GLFW.GLFW_KEY_RIGHT_ALT);
                } else if (root.has("wheelKey")) {
                    openKey = readBinding(root.get("wheelKey"), GLFW.GLFW_KEY_RIGHT_ALT);
                }
            } catch (Exception exception) {
                LOGGER.warn("No pude leer {}: {}", copyConfig, exception.getMessage());
            }
        }

        if (imported) {
            save();
        }
    }

    // Un campo roto no debe descartar las otras ranuras ni provocar una
    // reimportacion que reemplace mensajes propios por una copia mas antigua.
    private static JsonArray readArray(JsonObject root, String field) {
        JsonElement value = root.get(field);
        return value != null && value.isJsonArray() ? value.getAsJsonArray() : new JsonArray();
    }

    private static String readText(JsonElement value, String fallback) {
        return value != null && value.isJsonPrimitive() ? value.getAsString() : fallback;
    }

    private static int readBinding(JsonElement value, int fallback) {
        if (value == null || !value.isJsonPrimitive() || value.getAsJsonPrimitive().isBoolean()) return fallback;
        try {
            // Gson.getAsInt trunca fracciones y desborda enteros grandes.
            return sanitize(new BigDecimal(value.getAsString()).intValueExact(), fallback);
        } catch (NumberFormatException | ArithmeticException exception) {
            return fallback;
        }
    }

    // ---------------------------------------------------------------- escritura

    public static synchronized boolean save() {
        ensureLoaded();
        pendingSave = true;
        Path own = configDir().resolve(OWN_FILE);
        JsonObject root = new JsonObject();
        root.addProperty("importedFrom", importedFrom);
        root.addProperty("openKey", openKey);
        JsonArray slots = new JsonArray();
        for (int i = 0; i < SLOT_COUNT; i++) {
            JsonObject slot = new JsonObject();
            slot.addProperty("name", names[i]);
            slot.addProperty("message", messages[i]);
            slot.addProperty("key", keys[i]);
            slots.add(slot);
        }
        root.add("slots", slots);
        Path temporary = null;
        try {
            Files.createDirectories(own.getParent());
            temporary = Files.createTempFile(own.getParent(), OWN_FILE + ".", ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
            // No truncar el archivo anterior antes de terminar de serializar.
            try {
                Files.move(temporary, own, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, own, StandardCopyOption.REPLACE_EXISTING);
            }
            pendingSave = false;
            return true;
        } catch (Exception exception) {
            LOGGER.warn("No pude guardar {}: {}", own, exception.getMessage());
            return false;
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (Exception exception) {
                    LOGGER.warn("No pude limpiar {}: {}", temporary, exception.getMessage());
                }
            }
        }
    }

    // ---------------------------------------------------------------- consulta

    public static synchronized String name(int slot) {
        ensureLoaded();
        return valid(slot) ? names[slot] : defaultName(slot);
    }

    public static synchronized String message(int slot) {
        ensureLoaded();
        return valid(slot) ? messages[slot] : "";
    }

    public static synchronized int key(int slot) {
        ensureLoaded();
        return valid(slot) ? keys[slot] : UNBOUND;
    }

    public static synchronized int openKey() {
        ensureLoaded();
        return openKey;
    }

    public static synchronized String importedFrom() {
        ensureLoaded();
        return importedFrom;
    }

    public static synchronized int countWithText() {
        int count = 0;
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (messages[i] != null && !messages[i].isBlank()) count++;
        }
        return count;
    }

    public static synchronized void setKey(int slot, int keyCode) {
        ensureLoaded();
        if (!valid(slot)) return;
        int next = sanitize(keyCode, UNBOUND);
        // La tecla que abre la ficha queda reservada, como en CopyL; y una tecla
        // solo puede pertenecer a una ranura.
        if (next == openKey) next = UNBOUND;
        if (next != UNBOUND) {
            for (int i = 0; i < SLOT_COUNT; i++) {
                if (i != slot && keys[i] == next) keys[i] = UNBOUND;
            }
        }
        keys[slot] = next;
        save();
    }

    /** Guarda un mensaje nuevo en la ranura y la deja con texto listo para enviar. */
    public static synchronized boolean setMessage(int slot, String text) {
        ensureLoaded();
        if (!valid(slot)) return false;
        updateMessage(slot, text);
        return save();
    }

    /** Actualiza la memoria sin E/S; la pantalla agrupa las pulsaciones. */
    public static synchronized boolean updateMessage(int slot, String text) {
        ensureLoaded();
        if (!valid(slot)) return false;
        String next = normalizeMessage(text);
        if (next.equals(messages[slot])) return false;
        messages[slot] = next;
        pendingSave = true;
        return true;
    }

    /** Sobrevive al cierre de la pantalla si una escritura falla. */
    public static synchronized boolean hasPendingSave() {
        ensureLoaded();
        return pendingSave;
    }

    public static synchronized void setOpenKey(int keyCode) {
        ensureLoaded();
        openKey = sanitize(keyCode, UNBOUND);
        if (openKey != UNBOUND) {
            for (int i = 0; i < SLOT_COUNT; i++) {
                if (keys[i] == openKey) keys[i] = UNBOUND;
            }
        }
        save();
    }

    // ---------------------------------------------------------------- ayudas

    public static boolean isBound(int slot) {
        return key(slot) != UNBOUND;
    }

    /** Texto corto de la tecla asignada, o un guion cuando la ranura no tiene. */
    public static Component keyName(int slot) {
        int key = key(slot);
        if (key == UNBOUND) return Component.translatable("gripeverde.dispatch.slot.nokey");
        return keyNameOf(key);
    }

    public static Component keyNameOf(int binding) {
        if (binding == UNBOUND) return Component.translatable("gripeverde.dispatch.slot.nokey");
        boolean english = com.eternalcraft.gripeverde.client.lore.Discoveries.isEnglish();
        if (isMouse(binding)) {
            int button = mouseButton(binding);
            // v0.249.0: los botones laterales se nombran como los cuenta la mano,
            // no como los cuenta GLFW, para que "clic 4" signifique lo mismo.
            if (button >= GLFW.GLFW_MOUSE_BUTTON_4) {
                return Component.literal((english ? "Side " : "Lateral ") + (button + 1));
            }
            return InputConstants.Type.MOUSE.getOrCreate(button).getDisplayName();
        }
        // v0.249.0: el 1 del numpad no es el 1 de la fila: se rotula distinto.
        if (binding >= GLFW.GLFW_KEY_KP_0 && binding <= GLFW.GLFW_KEY_KP_9) {
            return Component.literal("NUM " + (binding - GLFW.GLFW_KEY_KP_0));
        }
        return InputConstants.Type.KEYSYM.getOrCreate(binding).getDisplayName();
    }

    public static boolean isKeyboard(int binding) {
        return binding >= GLFW.GLFW_KEY_SPACE && binding <= GLFW.GLFW_KEY_LAST;
    }

    public static boolean isMouse(int binding) {
        int button = binding - MOUSE_BASE;
        return button >= GLFW.GLFW_MOUSE_BUTTON_1 && button <= GLFW.GLFW_MOUSE_BUTTON_LAST;
    }

    public static int encodeMouse(int button) {
        if (button < GLFW.GLFW_MOUSE_BUTTON_1 || button > GLFW.GLFW_MOUSE_BUTTON_LAST) return UNBOUND;
        return MOUSE_BASE + button;
    }

    public static int mouseButton(int binding) {
        return isMouse(binding) ? binding - MOUSE_BASE : -1;
    }

    public static int sanitize(int binding, int fallback) {
        if (binding == UNBOUND) return UNBOUND;
        if (isKeyboard(binding) || isMouse(binding)) return binding;
        return fallback;
    }

    private static boolean valid(int slot) {
        return slot >= 0 && slot < SLOT_COUNT;
    }

    private static String defaultName(int index) {
        return "Mensaje " + (index + 1);
    }

    private static String normalizeName(String value, int index) {
        String cleaned = clean(value).trim();
        if (cleaned.isEmpty()) return defaultName(index);
        return truncateClean(cleaned, MAX_NAME_LENGTH);
    }

    private static String normalizeMessage(String value) {
        return truncateClean(clean(value), MAX_MESSAGE_LENGTH);
    }

    /** Respeta el limite UTF-16 del chat sin partir un par sustituto ya limpio. */
    private static String truncateClean(String cleaned, int limit) {
        if (cleaned.length() <= limit) return cleaned;
        int end = limit;
        if (end > 0 && Character.isHighSurrogate(cleaned.charAt(end - 1))) end--;
        return cleaned.substring(0, end);
    }

    /** Una sola linea, sin controles: lo mismo que exige el chat. */
    private static String clean(String value) {
        if (value == null || value.isEmpty()) return "";
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\r' || c == '\n' || Character.isISOControl(c)) {
                out.append(' ');
                continue;
            }
            if (Character.isHighSurrogate(c)) {
                if (i + 1 < value.length() && Character.isLowSurrogate(value.charAt(i + 1))) {
                    out.append(c).append(value.charAt(++i));
                } else {
                    out.append('?');
                }
                continue;
            }
            if (Character.isLowSurrogate(c)) {
                out.append('?');
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }
}
