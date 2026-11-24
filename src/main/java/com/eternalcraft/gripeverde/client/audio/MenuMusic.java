package com.eternalcraft.gripeverde.client.audio;


import com.eternalcraft.gripeverde.config.ClientConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MenuMusic v3 - Reproduccion con crossfade + ducking.
 *
 * v0.8.2/v0.8.3:
 *   - Crossfade entre pistas (fade-out de la actual + fade-in de la nueva).
 *   - Ducking automatico cuando MenuAmbience dispara SFX importantes.
 *   - Volumen dinamico ajustable en tiempo real via slider.
 */
public class MenuMusic {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean DEBUG_LOGGING = false;

    // Volumen base del usuario (0.0 - 1.0), ajustado por el slider.
    private static float userVolume = 0.4f;
    private static boolean volumeLoadedFromConfig = false;
    private static final float MAX_VOLUME = 0.8f;

    // Duraciones reales de cada pista (ms)
    private static final long[] TRACK_DURATIONS_MS = {
        160_000L, 90_000L, 177_000L, 160_000L
    };

    // Crossfade
    private static final long CROSSFADE_MS = 4_000L;
    private static final long GAP_MIN_MS = 2_000L;
    private static final long GAP_MAX_MS = 5_000L;

    // Ducking
    private static final float DUCK_FACTOR = 0.35f;     // baja al 35% durante duck
    private static final long DUCK_ATTACK_MS = 300L;    // tiempo para bajar
    private static final long DUCK_RELEASE_MS = 1500L;  // tiempo para volver

    private static final List<RegistryObject<SoundEvent>> PLAYLIST = new ArrayList<>();
    static {
        PLAYLIST.add(ModSounds.MENU_MUSIC_1);
        PLAYLIST.add(ModSounds.MENU_MUSIC_2);
        PLAYLIST.add(ModSounds.MENU_MUSIC_3);
        PLAYLIST.add(ModSounds.MENU_MUSIC_4);
    }

    private static final RandomSource RNG = RandomSource.create();

    /**
     * v0.23.0: temporizadores con reloj monotonico. System.currentTimeMillis()
     * salta si el SO ajusta la hora (NTP, suspension); nanoTime solo avanza.
     */
    private static long nowMs() { return System.nanoTime() / 1_000_000L; }

    private static boolean active = false;
    private static FadingMusicInstance currentInstance = null;
    private static FadingMusicInstance previousInstance = null;
    private static int currentIndex = -1;
    private static long currentStartMs = 0L;
    private static long currentDurationMs = 0L;

    private static long crossfadeStartMs = 0L;
    private static boolean crossfading = false;

    private static long nextTrackAtMs = 0L;
    private static boolean waitingForNext = false;

    // Ducking state
    private static long duckUntilMs = 0L;      // duck activo hasta este momento
    private static long duckStartMs = 0L;
    private static long duckReleaseStartMs = 0L;

    private static final List<Integer> shuffledOrder = new ArrayList<>();
    private static int shuffledPos = 0;

    // v0.16.0 - La pieza fantasma: quien hace girar el gramofono mas alla de su
    // ultima pieza oye lo que el cronista nunca grabo.
    private static int manualSkipsThisSession = 0;
    private static boolean ghostPlayedThisSession = false;
    private static long ghostWhisperAtMs = 0L;

    // === API publica ===
    public static float getUserVolume() {
        if (!volumeLoadedFromConfig) {
            try {
                userVolume = ClientConfig.MUSIC_VOLUME.get().floatValue();
                volumeLoadedFromConfig = true;
            } catch (Exception ignored) {
                // Config aun no cargada; devolvemos el default
            }
        }
        return userVolume;
    }

    /** Update the audible volume while dragging, without writing the config file. */
    public static void setUserVolumeLive(float v) {
        userVolume = Math.max(0f, Math.min(1f, v));
        volumeLoadedFromConfig = true;
    }

    /** Persist the final slider value once the player releases the control. */
    public static void setUserVolume(float v) {
        setUserVolumeLive(v);
        try {
            ClientConfig.MUSIC_VOLUME.set((double) userVolume);
            ClientConfig.MUSIC_VOLUME.save();
        } catch (Exception ignored) {
            // Config aun no cargada; se persistira en el proximo cambio
        }
        // El volumen se aplicara en el proximo tick, sin cortar.
    }

    /**
     * Llamado por MenuAmbience cuando dispara un SFX importante.
     * La musica bajara temporalmente para no taparlo.
     */
    public static void duckFor(long durationMs) {
        long now = nowMs();
        long newUntil = now + durationMs;
        if (newUntil > duckUntilMs) {
            if (duckUntilMs <= now) duckStartMs = now;
            duckUntilMs = newUntil;
            duckReleaseStartMs = 0L;
        }
    }

    /** Rebuilds menu music after resource reloads without leaving the menu. */
    public static void recoverAfterResourceReload() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level != null) return;
        start();
    }

    /**
     * v0.23.0: la banda sonora vanilla del menu calla mientras el gabinete esta
     * abierto. En 1.20.1 MusicManager dispara Musics.MENU a los 20-600 ticks en
     * cualquier pantalla sin mundo y sonaba ENCIMA de las pistas del mod.
     * Se revisa cada tick porque vanilla reprograma la siguiente pista solo.
     */
    private static void suppressVanillaMenuMusic(Minecraft mc) {
        try {
            var manager = mc.getMusicManager();
            var situational = mc.getSituationalMusic();
            if (situational != null && manager.isPlayingMusic(situational)) {
                manager.stopPlaying();
            }
        } catch (Throwable ignored) {}
    }

    /**
     * v0.23.0 watchdog: detecta instancias fantasma tras una recarga del motor
     * de sonido (F3+T, cambio de resourcepacks). Si el motor mata la instancia,
     * nadie vuelve a llamar a su tick() y done queda en false; sin esta guarda
     * la musica del menu quedaba muda para siempre hasta cambiar de pantalla.
     */
    private static boolean instanceGoneDead(Minecraft mc) {
        if (currentInstance == null) return false;
        long elapsed = currentInstance.getElapsedMs();
        if (currentDurationMs > 0L && elapsed > currentDurationMs + 2_000L) return true;
        if (elapsed > 1_500L) {
            try {
                return !mc.getSoundManager().isActive(currentInstance);
            } catch (Throwable t) {
                return false;
            }
        }
        return false;
    }

    public static void start() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        // Sound options are also reachable from a world: never start menu music there.
        if (mc.level != null) {
            stop();
            return;
        }
        if (!isMusicEnabled()) {
            stop();
            suppressVanillaMenuMusic(mc);
            return;
        }

        // Si ya esta activo Y la musica esta sonando realmente, ignorar
        if (active && currentInstance != null && mc.getSoundManager().isActive(currentInstance)) {
            if (DEBUG_LOGGING) LOGGER.info("[MenuMusic v3] start() ignorado, ya esta sonando");
            return;
        }

        // Si llegamos aqui es porque o no esta activo, o la instancia murio (ej: al salir de un server)
        if (DEBUG_LOGGING && active) LOGGER.info("[MenuMusic v3] Reiniciando porque la instancia previa no existe o no suena");

        stop(); // Limpieza de seguridad
        if (mc == null) return;

        suppressVanillaMenuMusic(mc);

        active = true;
        manualSkipsThisSession = 0;
        ghostPlayedThisSession = false;
        ghostWhisperAtMs = 0L;
        rebuildShuffle();
        playNextTrack();

        if (DEBUG_LOGGING) LOGGER.info("[MenuMusic v3] Iniciado");
    }

    private static void retireInstance(Minecraft mc, FadingMusicInstance instance) {
        if (instance == null) return;
        // The engine may be unavailable during teardown/reload. Retire the
        // tickable instance regardless, so an engine failure cannot orphan it.
        instance.markDone();
        try { if (mc != null) mc.getSoundManager().stop(instance); } catch (Exception ignored) {}
    }

    public static void stop() {
        Minecraft mc = Minecraft.getInstance();
        retireInstance(mc, currentInstance);
        retireInstance(mc, previousInstance);

        active = false;
        currentInstance = null;
        previousInstance = null;
        currentIndex = -1;
        waitingForNext = false;
        nextTrackAtMs = 0L;
        ghostWhisperAtMs = 0L;
        crossfading = false;
        duckUntilMs = 0L;
        duckStartMs = 0L;
        duckReleaseStartMs = 0L;

        if (DEBUG_LOGGING) LOGGER.info("[MenuMusic v3] Detenido");
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        // Seguridad: si el jugador entro a un mundo, detener musica del menu
        if (mc.level != null) {
            if (active) {
                if (DEBUG_LOGGING) LOGGER.info("[MenuMusic v3] Jugador en mundo, auto-stop");
                stop();
            }
            return;
        }

        if (!isMusicEnabled()) {
            if (active) stop();
            suppressVanillaMenuMusic(mc);
            return;
        }
        if (!active) return;

        // La vieja banda sonora no toca mientras el gramofono del gabinete gira.
        suppressVanillaMenuMusic(mc);

        long now = nowMs();

        // Susurro diferido de la pieza fantasma
        if (ghostWhisperAtMs > 0L && now >= ghostWhisperAtMs) {
            ghostWhisperAtMs = 0L;
            try {
                UiSounds.playCustom(ModSounds.MENU_AMBIENT_OCCASIONAL.get(), 0.18f, 0.44f);
                UiSounds.playCustom(ModSounds.MENU_TICK.get(), 0.06f, 0.40f);
            } catch (Throwable ignored) {}
        }

        // Aplicar volumen (con crossfade + ducking) a instancias activas
        updateVolumes(now);

        // Gestionar crossfade activo
        if (crossfading) {
            long crossElapsed = now - crossfadeStartMs;
            if (crossElapsed >= CROSSFADE_MS) {
                // Crossfade termino: eliminar la instancia anterior
                if (previousInstance != null) {
                    retireInstance(mc, previousInstance);
                    previousInstance = null;
                }
                crossfading = false;
            }
        }

        // Si estamos esperando el gap y llego el momento, arrancamos la proxima
        if (waitingForNext) {
            if (now >= nextTrackAtMs) {
                waitingForNext = false;
                playNextTrack();
            }
            return;
        }

        // Si no hay instancia actual (o el motor la dejo fantasma), pasamos a la siguiente
        if (currentInstance == null || currentInstance.isStopped() || instanceGoneDead(mc)) {
            currentInstance = null;
            queueNextWithGap();
            return;
        }

        // Detectar si debemos iniciar crossfade (faltan pocos ms para el fin)
        long remaining = currentInstance.getRemainingMs();
        if (!crossfading && remaining <= CROSSFADE_MS && remaining > 0) {
            startCrossfadeToNext();
        }
    }


    /**
     * Fuerza crossfade inmediato a la siguiente pista.
     * Ignora el click si ya hay crossfade en curso o no hay musica activa.
     */
    public static void skipToNext() {
        // Input can arrive before the next tick stops menu music on world entry.
        // Guard here too: never advance the playlist or trigger the ghost piece.
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level != null || !active || crossfading) return;
        manualSkipsThisSession++;
        // La pieza fantasma: tras agotar las cuatro piezas, la quinta vuelta
        // del gramofono trae lo que el cronista nunca grabo.
        if (manualSkipsThisSession >= 5 && !ghostPlayedThisSession) {
            ghostPlayedThisSession = true;
            playGhostPiece();
            return;
        }
        if (currentInstance == null || currentInstance.isStopped()) {
            // No hay pista sonando; arrancar una nueva directamente
            playNextTrack();
            return;
        }
        startCrossfadeToNext();
        if (DEBUG_LOGGING) LOGGER.info("[MenuMusic v3] Skip manual solicitado");
    }

    /**
     * La pieza fantasma. Silencio deliberado + un susurro unico, marcado como
     * descubrimiento. Luego el gramofono vuelve a su cauce.
     */
    private static void playGhostPiece() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        // Detener la pieza actual de golpe: el gramofono se queda sin cuerda.
        if (currentInstance != null) {
            retireInstance(mc, currentInstance);
        }
        currentInstance = null;
        previousInstance = null;
        crossfading = false;

        try {
            com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                com.eternalcraft.gripeverde.client.lore.Discoveries.GHOST_TRACK);
        } catch (Throwable ignored) {}

        // Susurro unico, muy bajo, un poco desafinado.
        try {
            UiSounds.playCustom(ModSounds.MENU_AMBIENT_OCCASIONAL.get(), 0.22f, 0.52f);
        } catch (Throwable ignored) {}

        // Marcar el susurro diferido y volver a pista normal tras una pausa larga.
        ghostWhisperAtMs = nowMs() + 1500L;
        nextTrackAtMs = nowMs() + 7000L;
        waitingForNext = true;
    }

    /**
     * Devuelve true si hay un crossfade en curso (para que la UI ignore clicks).
     */
    public static boolean isCrossfading() {
        return crossfading;
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean isMusicEnabled() {
        try {
            return ClientConfig.MENU_MUSIC_ENABLED.get();
        } catch (IllegalStateException ignored) {
            return true;
        }
    }

    /** One-based playlist position, or 0 while no regular record is playing. */
    public static int getCurrentTrackNumber() {
        return active && currentInstance != null && !currentInstance.isStopped() && currentIndex >= 0
            ? currentIndex + 1
            : 0;
    }

    public static int getTrackCount() {
        return PLAYLIST.size();
    }

    public static boolean isDucking() {
        long now = nowMs();
        return duckUntilMs > now || (duckReleaseStartMs > 0L && now - duckReleaseStartMs < DUCK_RELEASE_MS);
    }

    /** 0..1 donde 1 = volumen normal y 0 = completamente apagado. */

    // === Internos ===

    private static void updateVolumes(long now) {
        float baseVol = getUserVolume() * MAX_VOLUME;

        // Calcular factor de ducking (0.0 - 1.0, donde 1.0 = normal)
        float duckMultiplier = computeDuckMultiplier(now);
        float finalBaseVol = baseVol * duckMultiplier;

        // Crossfade: si esta activo, la nueva sube y la vieja baja
        if (crossfading && previousInstance != null && currentInstance != null) {
            long crossElapsed = now - crossfadeStartMs;
            float t = Math.max(0f, Math.min(1f, (float) crossElapsed / (float) CROSSFADE_MS));
            currentInstance.setDynamicVolume(finalBaseVol * t);
            previousInstance.setDynamicVolume(finalBaseVol * (1f - t));
        } else if (currentInstance != null) {
            currentInstance.setDynamicVolume(finalBaseVol);
        }
    }

    private static float computeDuckMultiplier(long now) {
        if (duckUntilMs <= 0L) return 1f;

        // Fase attack (baja hacia DUCK_FACTOR) con rampa corta, no golpe seco.
        if (now < duckUntilMs) {
            long attackElapsed = Math.max(0L, now - duckStartMs);
            if (attackElapsed < DUCK_ATTACK_MS) {
                float t = (float) attackElapsed / (float) DUCK_ATTACK_MS;
                return 1f + (DUCK_FACTOR - 1f) * t;
            }
            return DUCK_FACTOR;
        }

        // Fase release (vuelve de DUCK_FACTOR a 1.0)
        if (duckReleaseStartMs == 0L) {
            duckReleaseStartMs = duckUntilMs;
        }
        long releaseElapsed = now - duckReleaseStartMs;
        if (releaseElapsed >= DUCK_RELEASE_MS) {
            duckUntilMs = 0L;
            duckReleaseStartMs = 0L;
            return 1f;
        }
        float t = (float) releaseElapsed / (float) DUCK_RELEASE_MS;
        return DUCK_FACTOR + (1f - DUCK_FACTOR) * t;
    }

    private static void queueNextWithGap() {
        long gap = GAP_MIN_MS + (long)(RNG.nextDouble() * (GAP_MAX_MS - GAP_MIN_MS));
        nextTrackAtMs = nowMs() + gap;
        waitingForNext = true;

        if (DEBUG_LOGGING) LOGGER.info("[MenuMusic v3] Proxima pista en {} ms", gap);
    }

    private static void startCrossfadeToNext() {
        if (shuffledPos >= shuffledOrder.size()) rebuildShuffle();
        if (shuffledOrder.isEmpty()) return;

        int nextIndex = shuffledOrder.get(shuffledPos);
        shuffledPos++;

        RegistryObject<SoundEvent> ro = PLAYLIST.get(nextIndex);
        if (ro == null) return;

        SoundEvent se;
        try { se = ro.get(); } catch (Exception e) { return; }
        if (se == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        try {
            FadingMusicInstance nextInst = new FadingMusicInstance(se, 0.01f, TRACK_DURATIONS_MS[nextIndex]);
            mc.getSoundManager().play(nextInst);

            // La actual pasa a "previous", la nueva es "current"
            previousInstance = currentInstance;
            currentInstance = nextInst;
            currentIndex = nextIndex;
            currentStartMs = nowMs();
            currentDurationMs = TRACK_DURATIONS_MS[nextIndex];

            crossfadeStartMs = nowMs();
            crossfading = true;

            if (DEBUG_LOGGING) LOGGER.info("[MenuMusic v3] Crossfade a pista {}", nextIndex + 1);
        } catch (Exception e) {
            if (DEBUG_LOGGING) LOGGER.warn("[MenuMusic v3] Error en crossfade: {}", e.getMessage());
        }
    }

    private static void playNextTrack() {
        if (shuffledPos >= shuffledOrder.size()) rebuildShuffle();
        if (shuffledOrder.isEmpty()) return;

        currentIndex = shuffledOrder.get(shuffledPos);
        shuffledPos++;

        RegistryObject<SoundEvent> ro = PLAYLIST.get(currentIndex);
        if (ro == null) { queueNextWithGap(); return; }

        SoundEvent se;
        try {
            se = ro.get();
        } catch (NullPointerException | IllegalStateException ex) {
            if (DEBUG_LOGGING) LOGGER.warn("[MenuMusic v3] RegistryObject vacio para pista {}", currentIndex + 1);
            queueNextWithGap();
            return;
        }
        if (se == null) { queueNextWithGap(); return; }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        try {
            float baseVol = getUserVolume() * MAX_VOLUME;
            currentInstance = new FadingMusicInstance(se, baseVol, TRACK_DURATIONS_MS[currentIndex]);
            mc.getSoundManager().play(currentInstance);

            // Un salto manual tambien puede arrancar durante el silencio entre
            // pistas. Cancelar esa espera solo tras reproducir con exito: de lo
            // contrario su temporizador arrancaria otra pista encima de esta.
            waitingForNext = false;
            nextTrackAtMs = 0L;
            currentStartMs = nowMs();
            currentDurationMs = TRACK_DURATIONS_MS[currentIndex];

            if (DEBUG_LOGGING) {
                LOGGER.info("[MenuMusic v3] Pista {}: {} ({} ms)",
                    currentIndex + 1, se.getLocation(), currentDurationMs);
            }
        } catch (Exception e) {
            if (DEBUG_LOGGING) LOGGER.warn("[MenuMusic v3] Error al reproducir: {}", e.getMessage());
            currentInstance = null;
            queueNextWithGap();
        }
    }

    private static void rebuildShuffle() {
        shuffledOrder.clear();
        for (int i = 0; i < PLAYLIST.size(); i++) shuffledOrder.add(i);
        Collections.shuffle(shuffledOrder);
        shuffledPos = 0;
    }
}
