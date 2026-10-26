package com.eternalcraft.gripeverde.client.lore;

import com.eternalcraft.gripeverde.client.audio.ModSounds;
import com.eternalcraft.gripeverde.client.audio.UiSounds;
import com.eternalcraft.gripeverde.client.render.DiaryChrome;
import com.eternalcraft.gripeverde.client.util.MotionPolicy;
import net.minecraft.client.Minecraft;

/**
 * Registro de descubrimientos del gabinete.
 *
 * Cada descubrimiento es una ficha en la Sala de consulta. Al producirse uno
 * nuevo: sello de tinta (toast), sonido de archivo y registro permanente en
 * la memoria del cronista.
 *
 * v0.16.0
 */
public final class Discoveries {

    private Discoveries() {}

    // ── Ids canonicos ──
    public static final String WATCH_INSCRIPTION = "watch_inscription";
    public static final String WATCH_KEY         = "watch_key";
    public static final String POSTER_PEEL       = "poster_peel";
    public static final String WANTED_EYES       = "wanted_eyes";
    public static final String AVENUE_DARK       = "avenue_dark";
    public static final String SIGNAL_WINDOW     = "signal_window";
    public static final String RAT_PAUSE         = "rat_pause";
    public static final String CAT_SCRAP         = "cat_scrap";
    public static final String CODE_1897         = "code_1897";
    public static final String CODE_1152         = "code_1152";
    public static final String CODE_HOLLOWAY     = "code_holloway";
    public static final String CODE_PEREIRA      = "code_pereira";
    public static final String GHOST_TRACK       = "ghost_track";
    public static final String CANDLE_DARK       = "candle_dark";
    public static final String RAVEN_FEATHER     = "raven_feather";
    public static final String BREATH            = "breath";
    public static final String READER_SHADOW     = "reader_shadow";
    public static final String DRAWER_OPEN       = "drawer_open";
    public static final String DRAWER_COMPLETE   = "drawer_complete";
    public static final String SMUGGLER_SIGNAL   = "smuggler_signal";
    public static final String MIRROR            = "mirror";
    public static final String LETTERS           = "letters";
    public static final String EYE_FOLLOWS       = "eye_follows";
    public static final String GUESTBOOK         = "guestbook";
    public static final String FOG_TONGUE        = "fog_tongue";
    public static final String BINDER_MARKS      = "binder_marks";
    public static final String BROKEN_SEAL       = "broken_seal";
    public static final String SILENCE_LISTENS   = "silence_listens";
    public static final String DESTROYER         = "destroyer";
    public static final String ADDRESS_13        = "address_13";
    public static final String WHISPER_ANSWER    = "whisper_answer";
    public static final String KNOCK_BACK        = "knock_back";
    public static final String HINGE             = "hinge";
    public static final String NIGHT_WHOLE       = "night_whole";
    public static final String TIME_SICK         = "time_sick";
    public static final String STREAK3           = "streak3";
    public static final String DUST              = "dust";
    public static final String ANNIVERSARY       = "anniversary";
    public static final String FORBIDDEN_FOLIO   = "forbidden_folio";
    public static final String COFFIN_CART       = "coffin_cart";
    public static final String WEATHERVANE       = "weathervane";
    public static final String SKULL_WATCHES     = "skull_watches";
    // v0.24.0: la tormenta, el lacre y el oido afinado
    public static final String STORM_NIGHT       = "storm_night";
    public static final String SEAL_BREAKER      = "seal_breaker";
    public static final String SOUND_KEEPER      = "sound_keeper";
    // v0.25.0: el que mira la tormenta, la lluvia tocable y el oficio del doctor
    public static final String STORM_WATCHER     = "storm_watcher";
    public static final String DROP_CATCHER      = "drop_catcher";
    public static final String CODE_DOCTOR       = "code_doctor";

    // v0.21.3: fichas de la limpieza (solo texto, sin mecanica nueva)
    public static final String ARCHIVE_THINNING  = "archive_thinning";
    public static final String THEME_NEVER_PLAYED = "theme_never_played";
    public static final String BG_TOWN_MEMORY    = "bg_town_memory";
    // v0.21.5: cuarta confesion de la limpieza
    public static final String BUTTONS_NEVER_PRESSED = "buttons_never_pressed";


    // Objetos del cajon
    public static final String ITEM_KEY    = "item_key";
    public static final String ITEM_FEATHER = "item_feather";
    public static final String ITEM_SCRAP  = "item_scrap";
    public static final String ITEM_SEAL   = "item_seal";
    public static final String ITEM_NOTE   = "item_note";

    /**
     * Registra un descubrimiento. Si es nuevo: toast + sonido + memoria.
     * Devuelve true si fue nuevo.
     */
    public static boolean discover(String id) {
        if (ChroniclerMemory.isDiscovered(id)) return false;
        ChroniclerMemory.markDiscovered(id);
        toastName = nameFor(id);
        toastUntilMs = System.currentTimeMillis() + 4600L;
        toastStartMs = System.currentTimeMillis();
        try {
            // Sonido de archivo: campanilla + papel, grave y seco.
            UiSounds.playCustom(ModSounds.UI_NOTIFICATION.get(), 0.16f, 0.62f);
            UiSounds.playCustom(ModSounds.UI_BACK.get(), 0.14f, 0.55f);
            // v0.21.5: la musica cede mientras el archivo habla.
            com.eternalcraft.gripeverde.client.audio.MenuMusic.duckFor(2500L);
        } catch (Throwable ignored) {}
        return true;
    }

    // ── Toast ──
    private static String toastName = null;
    private static long toastUntilMs = 0L;
    private static long toastStartMs = 0L;

    public static boolean hasActiveToast() {
        return toastName != null && System.currentTimeMillis() < toastUntilMs;
    }

    /** Dibuja el sello de descubrimiento arriba a la derecha. Llamar al final del render. */
    public static void drawToast(net.minecraft.client.gui.GuiGraphics g, int screenW) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level != null || mc.font == null || !hasActiveToast()) return;
        long now = System.currentTimeMillis();
        long remain = toastUntilMs - now;
        long age = now - toastStartMs;
        boolean reducedMotion = MotionPolicy.isReduced();
        int alpha = 232;
        if (!reducedMotion) {
            if (age < 180L) alpha = (int)(232 * age / 180L);
            else if (remain < 260L) alpha = (int)(232 * remain / 260L);
        }
        alpha = Math.max(0, Math.min(232, alpha));

        boolean english = isEnglish();
        String header = english ? "NEW FILE" : "FICHA NUEVA";
        String name = toastName == null ? "" : toastName;
        int maxToastWidth = Math.min(224, screenW - 16);
        if (maxToastWidth < 76) return;

        int contentWidth = maxToastWidth - 26;
        String visibleHeader = DiaryChrome.fitText(mc.font, header, contentWidth);
        String visibleName = DiaryChrome.fitText(mc.font, name, contentWidth);
        int bw = Math.min(maxToastWidth,
            Math.max(104, Math.max(mc.font.width(visibleHeader), mc.font.width(visibleName)) + 26));
        int x = screenW - bw - 8;
        int y = 8;

        int paper = (alpha << 24) | 0xE6D2A6;
        int ink = (alpha << 24) | 0x241608;
        int red = (Math.min(215, alpha) << 24) | 0x6E1F16;
        int bronze = (Math.min(190, alpha) << 24) | 0x8E6A35;

        g.fill(x + 2, y + 2, x + bw + 2, y + 26, (Math.min(60, alpha / 3) << 24) | 0x000000);
        g.fill(x, y, x + bw, y + 24, paper);
        g.fill(x, y, x + bw, y + 1, bronze);
        g.fill(x, y + 23, x + bw, y + 24, bronze);
        g.fill(x, y, x + 1, y + 24, bronze);
        g.fill(x + bw - 1, y, x + bw, y + 24, bronze);
        // Sello de lacre reducido para una sola ficha.
        g.fill(x + 5, y + 8, x + 13, y + 16, red);
        g.fill(x + 7, y + 10, x + 11, y + 14, (Math.min(255, alpha) << 24) | 0x8B2A1C);
        g.fill(x + 6, y + 9, x + 8, y + 10, (Math.min(160, alpha) << 24) | 0xFFAA88);

        g.drawString(mc.font, visibleHeader, x + 18, y + 3, red, false);
        g.drawString(mc.font, visibleName, x + 18, y + 13, ink, false);
    }

    // ── Nombres (ES/EN) ──
    public static String nameFor(String id) {
        boolean en = isEnglish();
        switch (id) {
            case WATCH_INSCRIPTION: return en ? "The inscription beneath the glass" : "La inscripción bajo el cristal";
            case WATCH_KEY:         return en ? "A small key falls from time" : "Una llave pequeña cae del tiempo";
            case POSTER_PEEL:       return en ? "What the edict was hiding" : "Lo que el edicto escondía";
            case WANTED_EYES:       return en ? "The wrong face" : "El rostro equivocado";
            case AVENUE_DARK:       return en ? "The avenue goes dark" : "La avenida a oscuras";
            case SIGNAL_WINDOW:     return en ? "The window that answered" : "La ventana que respondió";
            case RAT_PAUSE:         return en ? "The rat that stopped" : "La rata que se detuvo";
            case CAT_SCRAP:         return en ? "The cat's gift" : "El regalo del gato";
            case CODE_1897:         return en ? "The night of the outbreak" : "La noche del brote";
            case CODE_1152:         return en ? "The hour that does not exist" : "La hora que no existe";
            case CODE_HOLLOWAY:     return en ? "The doctor's confession" : "La confesión del médico";
            case CODE_PEREIRA:      return en ? "A name spoken aloud" : "Un nombre dicho en voz alta";
            case GHOST_TRACK:       return en ? "The piece never recorded" : "La pieza nunca grabada";
            case CANDLE_DARK:       return en ? "What the dark reads" : "Lo que lee la oscuridad";
            case RAVEN_FEATHER:     return en ? "The raven's toll" : "El peaje del cuervo";
            case BREATH:            return en ? "Breath on the other side" : "Un aliento del otro lado";
            case READER_SHADOW:     return en ? "A second shadow" : "Una segunda sombra";
            case DRAWER_OPEN:       return en ? "The cabinet drawer" : "El cajón del gabinete";
            case DRAWER_COMPLETE:   return en ? "The drawer is complete" : "El cajón está completo";
            case SMUGGLER_SIGNAL:   return en ? "The smuggler's signal" : "La señal del contrabandista";
            case MIRROR:            return en ? "How the fog sees you" : "Cómo te ve la niebla";
            case LETTERS:           return en ? "The unanswered letters" : "Las cartas sin respuesta";
            case EYE_FOLLOWS:       return en ? "Perception watches back" : "La percepción también mira";
            case GUESTBOOK:         return en ? "Signed, in the visitor's book" : "Firmado en el libro de visitas";
            case FOG_TONGUE:        return en ? "The impossible tongue" : "La lengua imposible";
            case BINDER_MARKS:      return en ? "The binder's marks" : "Las marcas del encuadernador";
            case BROKEN_SEAL:       return en ? "The broken seal" : "El sello roto";
            case SILENCE_LISTENS:   return en ? "Silence listens" : "El silencio escucha";
            case DESTROYER:         return en ? "The cabinet remembers" : "El gabinete recuerda";
            case ADDRESS_13:        return en ? "The unmapped address" : "La dirección sin mapa";
            case WHISPER_ANSWER:    return en ? "Someone answered" : "Alguien respondió";
            case KNOCK_BACK:        return en ? "Three knocks, answered" : "Tres golpes, respondidos";
            case HINGE:             return en ? "The complaining hinge" : "La bisagra que se queja";
            case NIGHT_WHOLE:       return en ? "A whole night, reading" : "Una noche entera, leyendo";
            case TIME_SICK:         return en ? "Time grows sick" : "El tiempo enferma";
            case STREAK3:           return en ? "Three nights in a row" : "Tres noches seguidas";
            case DUST:              return en ? "The dust does not ask" : "El polvo no pregunta";
            case ANNIVERSARY:       return en ? "One year of the cabinet" : "Un año del gabinete";
            case FORBIDDEN_FOLIO:   return en ? "What the chronicler never wrote" : "Lo que el cronista no escribió";
            case COFFIN_CART:       return en ? "The undertaker's cart" : "El coche del sepulturero";
            case WEATHERVANE:       return en ? "The vane obeys the wind" : "La veleta obedece al viento";
            case SKULL_WATCHES:     return en ? "The emblem watches you" : "El emblema te mira";
            case STORM_NIGHT:       return en ? "The night the sky broke" : "La noche en que el cielo se rompió";
            case SEAL_BREAKER:      return en ? "The wax did not hold" : "El lacre no resistió";
            case SOUND_KEEPER:      return en ? "The cabinet, in tune" : "El gabinete, afinado";
            case STORM_WATCHER:     return en ? "The storm watcher" : "El que mira la tormenta";
            case DROP_CATCHER:      return en ? "Catching the rain" : "Atrapar la lluvia";
            case CODE_DOCTOR:       return en ? "The doctor still signs" : "El doctor aún firma";
            case ARCHIVE_THINNING:  return en ? "The archive that grows thinner" : "El archivo que se adelgaza";
            case THEME_NEVER_PLAYED: return en ? "The theme that never played" : "El tema que nunca sonó";
            case BG_TOWN_MEMORY:    return en ? "The street that never existed" : "La calle que nunca existió";
            case BUTTONS_NEVER_PRESSED: return en ? "The buttons never pressed" : "Los botones que nunca se pulsaron";
            default: return "???";
        }
    }

    /** Pista breve para fichas sin descubrir (Sala de consulta). */
    public static String hintFor(String id) {
        boolean en = isEnglish();
        switch (id) {
            case WATCH_INSCRIPTION: return en ? "Stop the swing. Read what hangs." : "Detén el vaivén. Lee lo que cuelga.";
            case WATCH_KEY:         return en ? "Time keeps more than hours." : "El tiempo guarda más que horas.";
            case POSTER_PEEL:       return en ? "The edict is pasted over something." : "El edicto está pegado sobre algo.";
            case WANTED_EYES:       return en ? "Hold the gaze of the wanted man." : "Sostén la mirada del buscado.";
            case AVENUE_DARK:       return en ? "The gas can be refused." : "El gas puede rechazarse.";
            case SIGNAL_WINDOW:     return en ? "A light is waiting for an answer." : "Una luz espera respuesta.";
            case RAT_PAUSE:         return en ? "Not every crossing is a hurry." : "No todo cruce es una huida.";
            case CAT_SCRAP:         return en ? "The roof cat pays its debts slowly." : "El gato del tejado paga sus deudas despacio.";
            case CODE_1897:         return en ? "Some years deserve to be spoken." : "Hay años que merecen decirse.";
            case CODE_1152:         return en ? "Speak the hour of the outbreak." : "Di en voz alta la hora del brote.";
            case CODE_HOLLOWAY:     return en ? "Name the doctor of the elixir." : "Nombra al médico del elixir.";
            case CODE_PEREIRA:      return en ? "Say the chronicler's name to his face." : "Di el nombre del cronista frente a él.";
            case GHOST_TRACK:       return en ? "Turn the gramophone past its last piece." : "Gira el gramófono más allá de su última pieza.";
            case CANDLE_DARK:       return en ? "Put out the flame in the field pause." : "Apaga la llama de la pausa.";
            case RAVEN_FEATHER:     return en ? "The raven keeps what it is owed." : "El cuervo cobra lo que se le debe.";
            case BREATH:            return en ? "Stay quiet behind the barricade." : "Quédate quieto tras la barricada.";
            case READER_SHADOW:     return en ? "Find enough, and be found." : "Encuentra suficientes pistas y deja que te encuentren.";
            case DRAWER_OPEN:       return en ? "The desk keeps a drawer for small things." : "La mesa guarda un cajón para cosas pequeñas.";
            case DRAWER_COMPLETE:   return en ? "Five small things, one drawer." : "Cinco cosas pequeñas, un cajón.";
            case SMUGGLER_SIGNAL:   return en ? "Hands, keys and field habits hide a sign." : "Manos, llaves y costumbres esconden una señal.";
            case MIRROR:            return en ? "Dress and bearing include a mirror." : "El porte incluye un espejo.";
            case LETTERS:           return en ? "Write to the district that does not answer." : "Escribe al barrio que no responde.";
            case EYE_FOLLOWS:       return en ? "Perception aids watch their watcher." : "Las ayudas de percepción vigilan a quien vigila.";
            case GUESTBOOK:         return en ? "The atlas of lodgings keeps a register." : "El atlas de hospederías lleva un registro.";
            case FOG_TONGUE:        return en ? "Some tongues are only spoken by the found." : "Hay lenguas que solo hablan los encontrados.";
            case BINDER_MARKS:      return en ? "The chronicle's margins keep marks." : "Los márgenes de la crónica guardan marcas.";
            case BROKEN_SEAL:       return en ? "Attributions end under a seal." : "Las atribuciones terminan bajo un sello.";
            case SILENCE_LISTENS:   return en ? "Lower the gramophone until nothing is left." : "Baja el gramófono hasta que no quede nada.";
            case DESTROYER:         return en ? "Destroy what the cabinet asks you to destroy." : "Destruye lo que el gabinete te pide destruir.";
            case ADDRESS_13:        return en ? "There is a door with no map." : "Hay una puerta sin mapa.";
            case WHISPER_ANSWER:    return en ? "When the room whispers, do not wait." : "Cuando la sala susurre, no esperes.";
            case KNOCK_BACK:        return en ? "Knock on the working desk, politely." : "Golpea la mesa de trabajo con educación.";
            case HINGE:             return en ? "Open the cabinet often enough." : "Abre el gabinete suficientes veces.";
            case NIGHT_WHOLE:       return en ? "Stay until the night ends." : "Quédate hasta que la noche termine.";
            case TIME_SICK:         return en ? "The second hand is unwell. Watch it longer." : "El segundero está enfermo. Obsérvalo más.";
            case STREAK3:           return en ? "Return tomorrow. And the day after." : "Vuelve mañana. Y pasado mañana.";
            case DUST:              return en ? "Leave. For a long while. Come back." : "Vete. Por mucho tiempo. Vuelve.";
            case ANNIVERSARY:       return en ? "A year is a long quarantine." : "Un año es una cuarentena larga.";
            case FORBIDDEN_FOLIO:   return en ? "Find almost everything." : "Encuentra casi todo.";
            case COFFIN_CART:       return en ? "Wait after the bells." : "Espera después de las campanas.";
            case WEATHERVANE:       return en ? "Watch the roofs when the wind rises." : "Mira los tejados cuando el viento suba.";
            case SKULL_WATCHES:     return en ? "The emblem's eye is not painted." : "El ojo del emblema no está pintado.";
            case STORM_NIGHT:       return en ? "Some nights the sky leans on the window." : "Hay noches en que el cielo se apoya en la ventana.";
            case SEAL_BREAKER:      return en ? "The telegram seal gives in to a stubborn hand." : "El sello del telegrama cede ante una mano terca.";
            case SOUND_KEEPER:      return en ? "The tuning fork hums where the sliders sleep." : "El diapasón zumba donde duermen los reguladores.";
            case STORM_WATCHER:     return en ? "On storm nights, stay for the third flash." : "En noches de tormenta, quédate para el tercer relámpago.";
            case DROP_CATCHER:      return en ? "The rain falls close enough to touch it." : "La lluvia cae lo bastante cerca como para tocarla.";
            case CODE_DOCTOR:       return en ? "The elixir had a doctor. The office remains." : "El elixir tuvo un médico. El oficio sigue en pie.";
            case ARCHIVE_THINNING:  return en ? "Open the cabinet again and again, until it gives up its old papers." : "Abre el gabinete una y otra vez, hasta que entregue sus papeles viejos.";
            case THEME_NEVER_PLAYED: return en ? "Speak the word of the cleansing." : "Pronuncia la palabra de la limpieza.";
            case BG_TOWN_MEMORY:    return en ? "When the archive confesses two ghosts, ask for the third." : "Cuando el archivo confiese dos fantasmas, pide el tercero.";
            case BUTTONS_NEVER_PRESSED: return en ? "When three ghosts confess, the fourth remembers the unused desk." : "Cuando tres fantasmas confiesen, el cuarto recuerda la mesa sin uso.";
            default: return "???";
        }
    }

    /** Cache corto (250ms) para no consultar el LanguageManager decenas de
     *  veces por frame; el cambio de idioma recarga resources y expira solo. */
    private static boolean cachedEnglish = true;
    private static long cachedEnglishUntilMs = 0L;

    public static boolean isEnglish() {
        long now = System.currentTimeMillis();
        if (now < cachedEnglishUntilMs) return cachedEnglish;
        boolean result;
        try {
            String lang = Minecraft.getInstance().getLanguageManager().getSelected();
            result = lang == null || !"es_es".equals(lang);
        } catch (Throwable t) {
            result = true;
        }
        cachedEnglish = result;
        cachedEnglishUntilMs = now + 250L;
        return result;
    }

    /** Lista ordenada de todos los ids, para la Sala de consulta. */
    public static final String[] ALL = {
        WATCH_INSCRIPTION, WATCH_KEY, POSTER_PEEL, WANTED_EYES, AVENUE_DARK,
        SIGNAL_WINDOW, RAT_PAUSE, CAT_SCRAP, CODE_1897,
        CODE_1152, CODE_HOLLOWAY, CODE_PEREIRA, GHOST_TRACK, CANDLE_DARK,
        RAVEN_FEATHER, BREATH, READER_SHADOW, DRAWER_OPEN, DRAWER_COMPLETE,
        SMUGGLER_SIGNAL, MIRROR, LETTERS, EYE_FOLLOWS, GUESTBOOK,
        FOG_TONGUE, BINDER_MARKS, BROKEN_SEAL, SILENCE_LISTENS, DESTROYER,
        ADDRESS_13, WHISPER_ANSWER, KNOCK_BACK, HINGE, NIGHT_WHOLE,
        TIME_SICK, STREAK3, DUST, ANNIVERSARY, FORBIDDEN_FOLIO,
        COFFIN_CART, WEATHERVANE, SKULL_WATCHES,
        ARCHIVE_THINNING, THEME_NEVER_PLAYED, BG_TOWN_MEMORY,
        BUTTONS_NEVER_PRESSED,
        STORM_NIGHT, SEAL_BREAKER, SOUND_KEEPER,
        STORM_WATCHER, DROP_CATCHER, CODE_DOCTOR
    };
}
