package com.eternalcraft.gripeverde.client.lore;

import com.eternalcraft.gripeverde.client.audio.ModSounds;
import com.eternalcraft.gripeverde.client.audio.UiSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Marginalia del cronista.
 *
 * Notas manuscritas que el cronista deja al margen de sus propios papeles,
 * como REACCION a algo que el lector hizo (no frases aleatorias: las
 * marginales siempre tienen contexto). Se encolan en memoria y se muestran
 * en el proximo papel del gabinete que se abra.
 *
 * v0.16.0
 */
public final class Marginalia {

    private Marginalia() {}

    // ── Ids de nota ──
    public static final String N_READ_YOU     = "read_you";
    public static final String N_LANGUAGE     = "language";
    public static final String N_WHISPER3     = "whisper3";
    public static final String N_KEY_HINT     = "key_hint";
    public static final String N_DRAWER_HINT  = "drawer_hint";
    public static final String N_SMUGGLER_HINT = "smuggler_hint";
    public static final String N_HINGE        = "hinge";
    public static final String N_STREAK3      = "streak3";
    public static final String N_STREAK7      = "streak7";
    public static final String N_DUST         = "dust";
    public static final String N_NIGHT        = "night";
    public static final String N_SILENCE      = "silence";
    public static final String N_DESTROY      = "destroy";
    public static final String N_LETTERS      = "letters";
    public static final String N_ANNIVERSARY  = "anniversary";
    public static final String N_PEREIRA      = "pereira";
    public static final String N_HOLLOWAY     = "holloway";
    public static final String N_READER10     = "reader10";
    public static final String N_CORRECTION   = "correction";
    public static final String N_FIRST_NIGHT  = "first_night";
    public static final String N_OPEN_COUNT   = "open_count";
    public static final String N_MANY_CARDS   = "many_cards";
    public static final String N_ARCHIVE_COMPLETE = "archive_complete";
    // v0.24.0
    public static final String N_STORM        = "storm";
    public static final String N_SEAL         = "seal";
    public static final String N_TUNING       = "tuning";
    // v0.25.0
    public static final String N_FLASH3       = "flash3";
    public static final String N_DROP         = "drop";
    public static final String N_DOCTOR       = "doctor";

    /** Encola una nota (persistente) si no hay ya una igual pendiente. */
    public static synchronized void queue(String noteId) {
        ChroniclerMemory.Data d = ChroniclerMemory.get();
        if (d.pendingNotes.contains(noteId)) return;
        d.pendingNotes.add(noteId);
        ChroniclerMemory.save();
    }

    /** Encola solo si nunca fue encolada antes (marcador en contadores). */
    public static synchronized void queueOnce(String noteId) {
        if (ChroniclerMemory.counter("note_" + noteId) > 0) return;
        ChroniclerMemory.setCounter("note_" + noteId, 1);
        queue(noteId);
    }

    // ── Estado de la nota activa en pantalla ──
    private static String activeId = null;
    private static long activeUntilMs = 0L;
    private static long activeStartMs = 0L;
    private static long lastPollMs = 0L;
    private static String cachedTextId;
    private static boolean cachedTextEnglish;
    private static Font cachedTextFont;
    private static String cachedText = "";
    private static String[] cachedCharacters = new String[0];
    private static int cachedPrefixLength = -1;
    private static String cachedPrefix = "";
    private static int cachedPrefixWidth;

    /** Toma la proxima nota pendiente (throttle: una por apertura de pantalla). */
    public static synchronized void poll() {
        long now = System.currentTimeMillis();
        if (now - lastPollMs < 2500L) return;
        lastPollMs = now;
        if (activeId != null && now < activeUntilMs) return;
        ChroniclerMemory.Data d = ChroniclerMemory.get();
        if (d.pendingNotes.isEmpty()) { activeId = null; return; }
        activeId = d.pendingNotes.remove(0);
        ChroniclerMemory.save();
        activeStartMs = now;
        activeUntilMs = now + 14_000L;
        try {
            // Rasguno de pluma al aparecer la marginal
            UiSounds.playCustom(ModSounds.UI_SCROLL.get(), 0.10f, 0.55f);
        } catch (Throwable ignored) {}
    }

    public static boolean hasActive() {
        return activeId != null && System.currentTimeMillis() < activeUntilMs;
    }

    /**
     * Dibuja la marginal activa en el margen del panel indicado.
     * (x, y) = esquina superior izquierda del panel; w = ancho.
     */
    public static void draw(GuiGraphics g, int panelX, int panelY, int panelW, int panelH) {
        if (!hasActive()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) return;
        long now = System.currentTimeMillis();
        long age = now - activeStartMs;
        long remain = activeUntilMs - now;
        boolean reducedMotion = com.eternalcraft.gripeverde.client.util.ClientAccessibility.reducedMotion();
        int alpha = 200;
        if (!reducedMotion) {
            if (age < 900L) alpha = (int)(200 * age / 900L);
            else if (remain < 1200L) alpha = (int)(200 * remain / 1200L);
        }
        alpha = Math.max(0, Math.min(200, alpha));
        if (alpha <= 4) return;

        String text = refreshTextCache(mc.font);
        if (text == null) return;

        // La marginal vive JUSTO fuera del panel, en el margen izquierdo,
        // escrita en vertical ascendente como nota al margen.
        int maxLen = Math.min(text.length(), Math.max(12, panelH / 6));
        int visibleLength = reducedMotion ? maxLen
            : (int)Math.min(maxLen, text.length() * Math.min(1.0, age / 2200.0) + 1);
        if (visibleLength <= 0) return;

        int ink = (alpha << 24) | 0x3D2A18;
        int x = panelX - 8;
        int yStart = panelY + panelH - 22;
        // texto vertical: una letra por fila
        int lines = Math.min(visibleLength, (panelH - 30) / 7);
        for (int i = 0; i < lines; i++) {
            g.drawString(mc.font, cachedCharacters[i], x, yStart - i * 7, ink, false);
        }
        // pequeno trazo de pluma debajo
        if (lines > 0) {
            int ty = yStart - lines * 7;
            g.fill(x, ty + 2, x + 5, ty + 3, (alpha / 2 << 24) | 0x3D2A18);
        }
    }

    /** Variante horizontal para pantallas sin margen izquierdo suficiente. */
    public static void drawHorizontal(GuiGraphics g, int cx, int y) {
        if (!hasActive()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) return;
        long now = System.currentTimeMillis();
        long age = now - activeStartMs;
        long remain = activeUntilMs - now;
        boolean reducedMotion = com.eternalcraft.gripeverde.client.util.ClientAccessibility.reducedMotion();
        int alpha = 190;
        if (!reducedMotion) {
            if (age < 900L) alpha = (int)(190 * age / 900L);
            else if (remain < 1200L) alpha = (int)(190 * remain / 1200L);
        }
        if (alpha <= 4) return;
        String text = refreshTextCache(mc.font);
        if (text == null) return;
        int chars = reducedMotion ? text.length()
            : (int)Math.min(text.length(), text.length() * Math.min(1.0, age / 2000.0) + 1);
        String shown = visiblePrefix(chars);
        g.drawString(mc.font, shown, cx - cachedPrefixWidth / 2, y, (alpha << 24) | 0x3D2A18, false);
    }

    /** Reutiliza el texto y cada glifo hasta que cambie nota, idioma o fuente. */
    private static String refreshTextCache(Font font) {
        boolean en = Discoveries.isEnglish();
        if (activeId == null) return null;
        if (activeId.equals(cachedTextId) && cachedTextEnglish == en && cachedTextFont == font) {
            return cachedText;
        }

        cachedTextId = activeId;
        cachedTextEnglish = en;
        cachedTextFont = font;
        cachedText = textFor(activeId, en);
        cachedCharacters = new String[cachedText == null ? 0 : cachedText.length()];
        if (cachedText != null) {
            for (int i = 0; i < cachedText.length(); i++) {
                cachedCharacters[i] = String.valueOf(cachedText.charAt(i));
            }
        }
        cachedPrefixLength = -1;
        cachedPrefix = "";
        cachedPrefixWidth = 0;
        return cachedText;
    }

    private static String visiblePrefix(int length) {
        int safeLength = Math.max(0, Math.min(length, cachedText.length()));
        if (cachedPrefixLength != safeLength) {
            cachedPrefixLength = safeLength;
            cachedPrefix = cachedText.substring(0, safeLength);
            cachedPrefixWidth = cachedTextFont.width(cachedPrefix);
        }
        return cachedPrefix;
    }

    private static String textFor(String id, boolean en) {
        if (id == null) return null;
        switch (id) {
            case N_READ_YOU:     return en ? "Someone has been reading my files. I know." : "Alguien ha estado leyendo mis fichas. Lo se.";
            case N_LANGUAGE:     return en ? "You write in another tongue now. I still understand." : "Ahora escribes en otra lengua. Igual te entiendo.";
            case N_WHISPER3:     return en ? "The third one asked about you. Answer with three knocks." : "El tercero pregunto por ti. Responde con tres golpes.";
            case N_KEY_HINT:     return en ? "The small key hangs from time, not from a pocket." : "La llave pequena cuelga del tiempo, no de un bolsillo.";
            case N_DRAWER_HINT:  return en ? "The lower drawer accepts a small key." : "El cajon de abajo acepta una llave pequena.";
            case N_SMUGGLER_HINT: return en ? "Crouch and sprint, both set to toggle. The rest, still. That is how contraband enters." : "Agacharse y correr, ambos en alternar. Lo demas, quieto. Asi entra el contrabando.";
            case N_HINGE:        return en ? "The hinge complains now. Oil it with fewer openings." : "La bisagra ya se queja. Menos aperturas la calmarian.";
            case N_STREAK3:      return en ? "Three nights in a row. The plague is not studied this hard." : "Tres noches seguidas. La peste no se estudia con tanto ahinco.";
            case N_STREAK7:      return en ? "Seven nights. I no longer know if you write, or the fever does." : "Siete noches. Ya no se si escribes tu, o la fiebre.";
            case N_DUST:         return en ? "Dust does not ask why anyone returns." : "El polvo no pregunta por que vuelve nadie.";
            case N_NIGHT:        return en ? "A whole night over the papers. The dawn is sick too." : "Una noche entera frente a los papeles. El alba tambien enferma.";
            case N_SILENCE:      return en ? "Silence has noise too, if one stays still." : "El silencio tambien tiene ruido, si uno se queda quieto.";
            case N_DESTROY:      return en ? "The cabinet remembers what you destroy." : "El gabinete recuerda lo que destruyes.";
            case N_LETTERS:      return en ? "Write no more. No one answers here." : "No escribas mas. Nadie responde aqui.";
            case N_ANNIVERSARY:  return en ? "A year since you first opened the cabinet. No one else ever did." : "Hace un ano abriste el gabinete por primera vez. Nadie mas lo abrio.";
            case N_PEREIRA:      return en ? "How do you know that name? Forget it. Forget it now." : "¿Como conoces ese nombre? Olvidalo. Olvidalo ahora.";
            case N_HOLLOWAY:     return en ? "I read what Holloway wrote. I wish I had not." : "Lei lo que escribio Holloway. Ojala no lo hubiera hecho.";
            case N_READER10:     return en ? "Ten findings. I no longer write alone." : "Diez hallazgos. Ya no escribo solo.";
            case N_CORRECTION:   return en ? "The ink has not yet dried. No: the ink is no longer drying." : "La tinta aun no seco. No: la tinta ya no seca.";
            case N_FIRST_NIGHT:  return en ? "First night. Everything you touch will remember you." : "Primera noche. Todo lo que toques te recordara.";
            case N_OPEN_COUNT:   return en ? "A hundred openings. The cabinet keeps the hours for you now." : "Cien aperturas. El gabinete ya lleva las horas por ti.";
            case N_MANY_CARDS: {
                // v0.24.0: la cifra sigue al cajon, que ya no siempre tiene cuarenta y pico.
                int cards = Discoveries.ALL.length - 3;
                return en ? cards + " cards. Soon the drawer will be empty of secrets."
                          : cards + " fichas. Pronto el cajon estara vacio de secretos.";
            }
            case N_ARCHIVE_COMPLETE: {
                int total = Discoveries.ALL.length;
                return en ? total + " cards. The drawer is empty now. Seal it."
                          : total + " fichas. El cajon ya esta vacio. Sellalo.";
            }
            case N_STORM:        return en ? "Tonight the sky reads the files aloud." : "Esta noche el cielo lee las fichas en voz alta.";
            case N_SEAL:         return en ? "The wax was old. It was asking to be broken." : "El lacre era viejo. Pedia a gritos que lo rompieran.";
            case N_TUNING:       return en ? "The room now sounds like itself. At last." : "La sala ya suena a si misma. Por fin.";
            case N_FLASH3:       return en ? "Three flashes and you did not blink. The chronicler counted." : "Tres relampagos y no parpadeaste. El cronista conto.";
            case N_DROP:         return en ? "Rain can be held for a second. Then it goes back to the sky." : "La lluvia puede sostenerse un segundo. Luego vuelve al cielo.";
            case N_DOCTOR:       return en ? "The doctor's signature keeps arriving on forms nobody sends." : "La firma del doctor sigue llegando en formularios que nadie envia.";
            default: return null;
        }
    }
}
