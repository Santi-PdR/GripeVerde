package com.eternalcraft.gripeverde.client.lore;

import com.eternalcraft.gripeverde.client.audio.ModSounds;
import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.util.MotionPolicy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;

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
    // v0.141.0
    public static final String N_RESCUE       = "rescue";
    public static final String N_WITNESS      = "witness";
    public static final String N_CALL         = "call";

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
    private static long lastDeferredMs = 0L;
    private static String cachedLayoutText;
    private static int cachedLayoutWidth = -1;
    private static int cachedLayoutLimit = -1;
    private static int cachedLayoutLinesWidth = 0;
    private static java.util.List<String> cachedLayoutLines = java.util.Collections.emptyList();

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
        lastDeferredMs = 0L;
        try {
            // Rasguno de pluma al aparecer la marginal
            UiSounds.playCustom(ModSounds.UI_SCROLL.get(), 0.10f, 0.55f);
        } catch (Throwable ignored) {}
    }

    public static boolean hasActive() {
        return activeId != null && System.currentTimeMillis() < activeUntilMs;
    }

    /** Keeps a note alive while a cramped layout or a discovery seal hides it. */
    public static synchronized void deferActive() {
        if (activeId == null) return;
        long now = System.currentTimeMillis();
        if (lastDeferredMs > 0L) activeUntilMs += Math.max(0L, now - lastDeferredMs);
        lastDeferredMs = now;
    }

    private static java.util.List<String> layoutLines(String text, Font font, int maxWidth, int maxLines) {
        if (text.equals(cachedLayoutText) && maxWidth == cachedLayoutWidth && maxLines == cachedLayoutLimit) {
            return cachedLayoutLines;
        }
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split("\\s+")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (line.length() > 0 && font.width(candidate) > maxWidth) {
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            } else {
                if (line.length() > 0) line.append(' ');
                line.append(word);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        if (lines.size() > maxLines) {
            lines = new java.util.ArrayList<>(lines.subList(0, maxLines));
            String ellipsis = "...";
            String last = lines.get(maxLines - 1);
            int lastWidth = Math.max(0, maxWidth - font.width(ellipsis));
            lines.set(maxLines - 1, font.plainSubstrByWidth(last, lastWidth) + ellipsis);
        }
        int widest = 0;
        for (String line2 : lines) widest = Math.max(widest, font.width(line2));
        cachedLayoutText = text;
        cachedLayoutWidth = maxWidth;
        cachedLayoutLimit = maxLines;
        cachedLayoutLines = java.util.Collections.unmodifiableList(lines);
        cachedLayoutLinesWidth = widest;
        return cachedLayoutLines;
    }

    /** Draws a legible, word-wrapped chronicler note above the paper panel. */
    public static void drawHorizontal(GuiGraphics g, int cx, int y) {
        if (isLiveWorld() || !hasActive()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) return;
        long now = System.currentTimeMillis();
        long age = now - activeStartMs;
        long remain = activeUntilMs - now;
        boolean reducedMotion = MotionPolicy.isReduced();
        int alpha = 190;
        if (!reducedMotion) {
            if (age < 900L) alpha = (int)(190 * age / 900L);
            else if (remain < 1200L) alpha = (int)(190 * remain / 1200L);
        }
        alpha = Math.max(0, Math.min(190, alpha));
        if (alpha <= 4) return;
        String text = textFor(activeId);
        if (text == null) return;

        String shown = text.trim();
        if (shown.isEmpty()) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int maxTextWidth = Math.max(1, screenWidth - 48);
        int lineHeight = mc.font.lineHeight;
        int cardBottom = y + 10;
        int clearTop = Discoveries.hasActiveToast() ? 36 : 2;
        int availableHeight = cardBottom - clearTop;
        if (availableHeight < lineHeight + 6) {
            deferActive();
            return;
        }
        int maxLines = Math.max(1, Math.min(4, (availableHeight - 6) / lineHeight));
        java.util.List<String> lines = layoutLines(shown, mc.font, maxTextWidth, maxLines);
        if (lines.isEmpty()) return;

        // v0.211.0: el ancho del bloque ya se midio al repartir las lineas.
        int textWidth = cachedLayoutLinesWidth;
        int cardWidth = Math.min(screenWidth - 8, textWidth + 20);
        int cardHeight = lines.size() * lineHeight + 6;
        int cardX = Math.max(4, Math.min(cx - cardWidth / 2, screenWidth - cardWidth - 4));
        int cardY = Math.max(clearTop, cardBottom - cardHeight);
        lastDeferredMs = 0L;
        int paper = (Math.min(220, alpha + 24) << 24) | 0x00E8DCB4;
        int edge = (Math.max(0, alpha / 2) << 24) | 0x00735936;
        int ink = (alpha << 24) | 0x003D2A18;
        int wax = (Math.min(205, alpha) << 24) | 0x006E1F16;

        // A restrained CEDA seal and paper rule frame the note without motion.
        g.fill(cardX, cardY, cardX + cardWidth, cardY + cardHeight, paper);
        // v0.227.0: la nota ya no es un rectangulo. Arriba y abajo el papel se
        // rompe en mordiscos parejos; los lados quedan apenas marcados.
        int tearSeed = cardX * 31 + cardWidth * 17;
        int at = 0;
        int seg = Math.max(5, cardWidth / 9);
        int n = 1;
        while (at < cardWidth) {
            int len = Math.min(cardWidth - at, seg + Math.floorMod(tearSeed + n * 7919, seg));
            int notch = 1 + Math.floorMod(tearSeed + n * 40503, 2);
            g.fill(cardX + at, cardY + 1 - notch, cardX + at + len, cardY + 1, edge);
            g.fill(cardX + at, cardY + cardHeight - 1, cardX + at + len,
                cardY + cardHeight - 1 + notch, edge);
            at += len;
            n++;
        }
        g.fill(cardX, cardY + 1, cardX + 1, cardY + cardHeight - 1, edge);
        g.fill(cardX + cardWidth - 1, cardY + 1, cardX + cardWidth, cardY + cardHeight - 1, edge);
        g.fill(cardX + 5, cardY + 5, cardX + 9, cardY + 9, wax);
        g.fill(cardX + 6, cardY + 6, cardX + 8, cardY + 7, (alpha << 24) | 0x00A74A38);
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(mc.font, lines.get(i), cardX + 13, cardY + 3 + i * lineHeight, ink, false);
        }
    }

    private static String textFor(String id) {
        boolean en = Discoveries.isEnglish();
        if (id == null) return null;
        switch (id) {
case N_READ_YOU:     return en ? "Someone has been going through my papers. I saw the pages move." : "Alguien anda hojeando mis papeles. Vi las hojas moverse.";
case N_LANGUAGE:     return en ? "You speak to me in another tongue now. I know your voice all the same." : "Ahora me hablas en otra lengua. Aun así, reconozco tu voz.";
case N_WHISPER3:     return en ? "He asked for you again, the third one." : "El tercero volvió a preguntar por ti.";
case N_KEY_HINT:     return en ? "That little key is tied to the clock. Look where the hands meet." : "La llavecita está atada al reloj. Mira dónde se cruzan las agujas.";
case N_DRAWER_HINT:  return en ? "The bottom drawer has been waiting for that little key." : "El cajón de abajo lleva tiempo esperando esa llavecita.";
case N_SMUGGLER_HINT: return en ? "Duck low and hurry through. Then let the rest of the house go quiet. That is how the contraband slips in." : "Agáchate y cruza de prisa. Luego deja que el resto de la casa vuelva al silencio. Así se cuela el contrabando.";
case N_HINGE:        return en ? "The hinge has started to groan. I must have opened that drawer too often." : "La bisagra empezó a quejarse. Debí abrir demasiado ese cajón.";
case N_STREAK3:      return en ? "Third night you have stayed. Even the fever would have given in by now." : "Ya es la tercera noche que te quedas. Hasta la fiebre se habría rendido.";
case N_STREAK7:      return en ? "A week of nights. I can no longer tell your hand from the fever’s." : "Una semana de noches. Ya no distingo tu letra de la fiebre.";
case N_DUST:         return en ? "Dust never asks what brought anyone back." : "El polvo no pregunta qué te trajo de vuelta.";
case N_NIGHT:        return en ? "We stayed with the papers until morning. Even the dawn looks ill." : "Nos quedamos con estos papeles hasta el alba. También ella parece enferma.";
case N_SILENCE:      return en ? "Stay still long enough, and the quiet begins to answer." : "Si te quedas quieto, el silencio acaba respondiendo.";
case N_DESTROY:      return en ? "You can burn every page; this house will still remember." : "Puedes quemar cada hoja; esta casa seguirá recordándolo.";
case N_LETTERS:      return en ? "Please, no more letters. No one is coming back to answer." : "Por favor, no escribas más cartas. Nadie va a volver para contestarlas.";
case N_ANNIVERSARY:  return en ? "A year ago you first came through that door. I thought I would never see you again." : "Hace un año cruzaste esa puerta por primera vez. Creí que no volvería a verte.";
case N_PEREIRA:      return en ? "How did you learn that name? Stop asking. Forget you heard it." : "¿De dónde sacaste ese nombre? No preguntes más. Olvida que lo oíste.";
case N_HOLLOWAY:     return en ? "I read Holloway’s letter. I should have left it sealed." : "Leí la carta de Holloway. Debí dejarla cerrada.";
case N_READER10:     return en ? "Ten secrets between us. I thought I was alone at this desk." : "Ya compartimos diez secretos. Creí que me quedaría solo en este escritorio.";
case N_CORRECTION:   return en ? "The ink should have dried by now. Why is it still wet?" : "La tinta ya tendría que haberse secado. ¿Por qué sigue húmeda?";
case N_FIRST_NIGHT:  return en ? "Your first night in this room. I can already see your hand in the dust." : "Es tu primera noche en esta habitación. Ya veo tu rastro en el polvo.";
case N_OPEN_COUNT:   return en ? "You have come here a hundred times. I have stopped counting the hours." : "Has vuelto cien veces. Dejé de contar las horas.";
            case N_MANY_CARDS: {
                int cards = Discoveries.ALL.length - 3;
                return en ? "I have tucked away " + cards + " secrets. At this rate, the drawer will soon be empty."
                          : "He guardado " + cards + " secretos. A este paso, el cajón pronto se quedará vacío.";
            }
            case N_ARCHIVE_COMPLETE: {
                int total = Discoveries.ALL.length;
                return en ? "I have nothing left to hide. " + total + " secrets, all found. Close the drawer now."
                          : "Ya no me queda nada por esconder. Has encontrado los " + total + " secretos. Cierra el cajón.";
            }
case N_STORM:        return en ? "Tonight the storm is speaking through the papers." : "Esta noche la tormenta habla a través de los papeles.";
case N_SEAL:         return en ? "That wax was old and brittle. I only had to press." : "Ese lacre estaba viejo y quebradizo. Solo tuve que apretar.";
case N_TUNING:       return en ? "The room sounds right again. I had missed that." : "La habitación vuelve a sonar como debe. Echaba de menos ese ruido.";
case N_FLASH3:       return en ? "You watched three flashes without closing your eyes. I counted them." : "Viste tres relámpagos sin cerrar los ojos. Los conté.";
case N_DROP:         return en ? "For a moment, the rain rests in your palm. Then it climbs back to the clouds." : "Por un instante, la lluvia descansa en tu mano. Luego regresa a las nubes.";
case N_DOCTOR:       return en ? "There is the doctor’s hand again, on a letter no one sent." : "Otra vez la letra del doctor, en una carta que nadie envió.";
case N_RESCUE:       return en ? "The thing dragged the stable boy into the mud before we could reach him. We heard his cry and turned back together. I have never run so fast toward something I feared."
                                  : "La criatura arrastró al mozo de las caballerizas antes de que pudiéramos alcanzarlo. Oímos su grito y volvimos juntos. Nunca había corrido tan deprisa hacia algo que me daba tanto miedo.";
case N_WITNESS:      return en ? "She was still breathing when we found her. Then the fever took hold, and she ran through the rain as if the wound did not hurt. I still cannot call them dead."
                                  : "Cuando la encontramos, todavía respiraba. Después la fiebre se apoderó de ella y cruzó la lluvia corriendo, como si la herida no doliera. Todavía no puedo llamarlos muertos.";
case N_CALL:         return en ? "That cry carried across the whole town. We left our bundles where they lay and ran back for him. I had never seen strangers turn around so quickly for one another."
                                  : "Ese grito se oyó por todo el pueblo. Dejamos los bultos donde estaban y volvimos corriendo por él. Nunca había visto a unos desconocidos regresar tan deprisa por los demás.";
            default: return null;
        }
    }
}
