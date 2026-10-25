package com.eternalcraft.gripeverde.client.lore;

import com.eternalcraft.gripeverde.client.audio.ModSounds;
import com.eternalcraft.gripeverde.client.audio.UiSounds;
import net.minecraft.client.Minecraft;
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

 
    /** True while a live session owns the screen; lore waits for the title menu. */
    private static boolean isLiveWorld() {
        try {
            Minecraft mc = Minecraft.getInstance();
            return mc != null && mc.level != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

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

    /** Toma la proxima nota pendiente (throttle: una por apertura de pantalla). */
    public static synchronized void poll() {
        if (isLiveWorld()) return;
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
        if (isLiveWorld() || !hasActive()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) return;
        long now = System.currentTimeMillis();
        long age = now - activeStartMs;
        long remain = activeUntilMs - now;
        int alpha = 200;
        if (age < 900L) alpha = (int)(200 * age / 900L);
        else if (remain < 1200L) alpha = (int)(200 * remain / 1200L);
        alpha = Math.max(0, Math.min(200, alpha));
        if (alpha <= 4) return;

        String text = textFor(activeId);
        if (text == null) return;

        // La marginal vive JUSTO fuera del panel, en el margen izquierdo,
        // escrita en vertical ascendente como nota al margen.
        int maxLen = Math.min(text.length(), Math.max(12, panelH / 6));
        String shown = text.substring(0, (int)Math.min(maxLen, text.length() * Math.min(1.0, age / 2200.0) + 1));
        if (shown.isEmpty()) return;

        int ink = (alpha << 24) | 0x3D2A18;
        int x = panelX - 8;
        int yStart = panelY + panelH - 22;
        // texto vertical: una letra por fila
        int lines = Math.min(shown.length(), (panelH - 30) / 7);
        for (int i = 0; i < lines; i++) {
            char c = shown.charAt(i);
            g.drawString(mc.font, String.valueOf(c), x, yStart - i * 7, ink, false);
        }
        // pequeno trazo de pluma debajo
        if (lines > 0) {
            int ty = yStart - lines * 7;
            g.fill(x, ty + 2, x + 5, ty + 3, (alpha / 2 << 24) | 0x3D2A18);
        }
    }

    /** Variante horizontal para pantallas sin margen izquierdo suficiente. */
    public static void drawHorizontal(GuiGraphics g, int cx, int y) {
        if (isLiveWorld() || !hasActive()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) return;
        long now = System.currentTimeMillis();
        long age = now - activeStartMs;
        long remain = activeUntilMs - now;
        int alpha = 190;
        if (age < 900L) alpha = (int)(190 * age / 900L);
        else if (remain < 1200L) alpha = (int)(190 * remain / 1200L);
        if (alpha <= 4) return;
        String text = textFor(activeId);
        if (text == null) return;

        int chars = (int)Math.min(text.length(), text.length() * Math.min(1.0, age / 2000.0) + 1);
        String shown = text.substring(0, chars);
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int maxTextWidth = Math.max(0, screenWidth - 48);
        int textWidth = mc.font.width(shown);
        if (textWidth > maxTextWidth) {
            String ellipsis = "...";
            int ellipsisWidth = mc.font.width(ellipsis);
            shown = maxTextWidth <= ellipsisWidth
                ? mc.font.plainSubstrByWidth(shown, maxTextWidth)
                : mc.font.plainSubstrByWidth(shown, maxTextWidth - ellipsisWidth) + ellipsis;
            textWidth = mc.font.width(shown);
        }
        if (shown.isEmpty()) return;

        int cardWidth = textWidth + 12;
        int cardX = Math.max(6, Math.min(cx - cardWidth / 2, screenWidth - cardWidth - 6));
        int cardY = Math.max(2, y - 3);
        int paper = (Math.min(220, alpha + 24) << 24) | 0x00E8DCB4;
        int edge = (Math.max(0, alpha / 2) << 24) | 0x00735936;
        int ink = (alpha << 24) | 0x003D2A18;

        // A small paper strip keeps the chronicler's note readable over the
        // dark title scene and contained on narrow windows.
        g.fill(cardX, cardY, cardX + cardWidth, cardY + 13, paper);
        g.fill(cardX, cardY, cardX + cardWidth, cardY + 1, edge);
        g.fill(cardX, cardY + 12, cardX + cardWidth, cardY + 13, edge);
        g.drawString(mc.font, shown, cardX + 6, cardY + 3, ink, false);
    }

    private static String textFor(String id) {
        boolean en = Discoveries.isEnglish();
        if (id == null) return null;
        switch (id) {
            case N_READ_YOU:     return en ? "Someone has been reading my files. I know." : "Alguien ha estado leyendo mis fichas. Lo sé.";
            case N_LANGUAGE:     return en ? "You write in another tongue now. I still understand." : "Ahora escribes en otra lengua. Igual te entiendo.";
            case N_WHISPER3:     return en ? "The third one asked about you. Answer with three knocks." : "El tercero preguntó por ti. Responde con tres golpes.";
            case N_KEY_HINT:     return en ? "The small key hangs from time, not from a pocket." : "La llave pequeña cuelga del tiempo, no de un bolsillo.";
            case N_DRAWER_HINT:  return en ? "The lower drawer accepts a small key." : "El cajón de abajo acepta una llave pequeña.";
            case N_SMUGGLER_HINT: return en ? "Crouch and sprint, both set to toggle. The rest, still. That is how contraband enters." : "Agacharse y correr, ambos en alternar. Lo demás, quieto. Así entra el contrabando.";
            case N_HINGE:        return en ? "The hinge complains now. Oil it with fewer openings." : "La bisagra ya se queja. Menos aperturas la calmarían.";
            case N_STREAK3:      return en ? "Three nights in a row. The plague is not studied this hard." : "Tres noches seguidas. La peste no se estudia con tanto ahínco.";
            case N_STREAK7:      return en ? "Seven nights. I no longer know if you write, or the fever does." : "Siete noches. Ya no sé si escribes tú, o la fiebre.";
            case N_DUST:         return en ? "Dust does not ask why anyone returns." : "El polvo no pregunta por qué vuelve nadie.";
            case N_NIGHT:        return en ? "A whole night over the papers. The dawn is sick too." : "Una noche entera frente a los papeles. El alba también enferma.";
            case N_SILENCE:      return en ? "Silence has noise too, if one stays still." : "El silencio también tiene ruido, si uno se queda quieto.";
            case N_DESTROY:      return en ? "The cabinet remembers what you destroy." : "El gabinete recuerda lo que destruyes.";
            case N_LETTERS:      return en ? "Write no more. No one answers here." : "No escribas más. Nadie responde aquí.";
            case N_ANNIVERSARY:  return en ? "A year since you first opened the cabinet. No one else ever did." : "Hace un año abriste el gabinete por primera vez. Nadie más lo abrió.";
            case N_PEREIRA:      return en ? "How do you know that name? Forget it. Forget it now." : "¿Cómo conoces ese nombre? Olvídalo. Olvídalo ahora.";
            case N_HOLLOWAY:     return en ? "I read what Holloway wrote. I wish I had not." : "Leí lo que escribió Holloway. Ojalá no lo hubiera hecho.";
            case N_READER10:     return en ? "Ten findings. I no longer write alone." : "Diez hallazgos. Ya no escribo solo.";
            case N_CORRECTION:   return en ? "The ink has not yet dried. No: the ink is no longer drying." : "La tinta aún no secó. No: la tinta ya no se seca.";
            case N_FIRST_NIGHT:  return en ? "First night. Everything you touch will remember you." : "Primera noche. Todo lo que toques te recordará.";
            case N_OPEN_COUNT:   return en ? "A hundred openings. The cabinet keeps the hours for you now." : "Cien aperturas. El gabinete ya lleva las horas por ti.";
            case N_MANY_CARDS: {
                // v0.24.0: la cifra sigue al cajon, que ya no siempre tiene cuarenta y pico.
                int cards = Discoveries.ALL.length - 3;
                return en ? cards + " cards. Soon the drawer will be empty of secrets."
                          : cards + " fichas. Pronto el cajón estará vacío de secretos.";
            }
            case N_ARCHIVE_COMPLETE: {
                int total = Discoveries.ALL.length;
                return en ? total + " cards. The drawer is empty now. Seal it."
                          : total + " fichas. El cajón ya está vacío. Séllalo.";
            }
            case N_STORM:        return en ? "Tonight the sky reads the files aloud." : "Esta noche el cielo lee las fichas en voz alta.";
            case N_SEAL:         return en ? "The wax was old. It was asking to be broken." : "El lacre era viejo. Pedía a gritos que lo rompieran.";
            case N_TUNING:       return en ? "The room now sounds like itself. At last." : "La sala ya suena a sí misma. Por fin.";
            case N_FLASH3:       return en ? "Three flashes and you did not blink. The chronicler counted." : "Tres relámpagos y no parpadeaste. El cronista contó.";
            case N_DROP:         return en ? "Rain can be held for a second. Then it goes back to the sky." : "La lluvia puede sostenerse un segundo. Luego vuelve al cielo.";
            case N_DOCTOR:       return en ? "The doctor's signature keeps arriving on forms nobody sends." : "La firma del doctor sigue llegando en formularios que nadie envía.";
            default: return null;
        }
    }
}
