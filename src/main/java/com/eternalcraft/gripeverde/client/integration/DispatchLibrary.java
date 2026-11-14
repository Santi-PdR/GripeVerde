package com.eternalcraft.gripeverde.client.integration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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

    // v0.184.2: dieciseis voces en vez de diez. El formato del archivo no cambia:
    // las que ya existian conservan su sitio y las nuevas entran vacias.
    public static final int SLOT_COUNT = 16;
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
            if (root.has("openKey") && root.get("openKey").isJsonPrimitive()) {
                openKey = sanitize(root.get("openKey").getAsInt(), GLFW.GLFW_KEY_RIGHT_ALT);
            }
            importedFrom = root.has("importedFrom") ? root.get("importedFrom").getAsString() : "";
            JsonArray slots = root.has("slots") && root.get("slots").isJsonArray()
                ? root.getAsJsonArray("slots") : new JsonArray();
            for (int i = 0; i < SLOT_COUNT && i < slots.size(); i++) {
                JsonObject slot = slots.get(i).getAsJsonObject();
                names[i] = normalizeName(slot.has("name") ? slot.get("name").getAsString() : null, i);
                messages[i] = normalizeMessage(slot.has("message") ? slot.get("message").getAsString() : "");
                keys[i] = sanitize(slot.has("key") ? slot.get("key").getAsInt() : UNBOUND, UNBOUND);
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
                JsonArray rawNames = root.has("names") && root.get("names").isJsonArray()
                    ? root.getAsJsonArray("names") : new JsonArray();
                JsonArray rawMessages = root.has("messages") && root.get("messages").isJsonArray()
                    ? root.getAsJsonArray("messages") : new JsonArray();
                JsonArray rawKeys = root.has("keyCodes") && root.get("keyCodes").isJsonArray()
                    ? root.getAsJsonArray("keyCodes") : new JsonArray();
                for (int i = 0; i < SLOT_COUNT; i++) {
                    if (i < rawNames.size()) names[i] = normalizeName(rawNames.get(i).getAsString(), i);
                    if (i < rawMessages.size()) messages[i] = normalizeMessage(rawMessages.get(i).getAsString());
                    if (i < rawKeys.size()) keys[i] = sanitize(rawKeys.get(i).getAsInt(), UNBOUND);
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
                    openKey = sanitize(root.get("openKey").getAsInt(), GLFW.GLFW_KEY_RIGHT_ALT);
                } else if (root.has("wheelKey") && root.get("wheelKey").isJsonPrimitive()) {
                    openKey = sanitize(root.get("wheelKey").getAsInt(), GLFW.GLFW_KEY_RIGHT_ALT);
                }
            } catch (Exception exception) {
                LOGGER.warn("No pude leer {}: {}", copyConfig, exception.getMessage());
            }
        }

        if (imported) {
            save();
        }
    }

    // ---------------------------------------------------------------- escritura

    public static synchronized boolean save() {
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
        try {
            Files.createDirectories(own.getParent());
            try (Writer writer = Files.newBufferedWriter(own, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
            return true;
        } catch (Exception exception) {
            LOGGER.warn("No pude guardar {}: {}", own, exception.getMessage());
            return false;
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
        messages[slot] = normalizeMessage(text);
        return save();
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
        if (isMouse(binding)) {
            return InputConstants.Type.MOUSE.getOrCreate(mouseButton(binding)).getDisplayName();
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
        return cleaned.length() <= MAX_NAME_LENGTH ? cleaned : cleaned.substring(0, MAX_NAME_LENGTH);
    }

    private static String normalizeMessage(String value) {
        String cleaned = clean(value);
        return cleaned.length() <= MAX_MESSAGE_LENGTH ? cleaned : cleaned.substring(0, MAX_MESSAGE_LENGTH);
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
