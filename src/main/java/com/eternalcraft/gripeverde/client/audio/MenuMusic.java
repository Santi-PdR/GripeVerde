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
    private static final boolean DEBUG_LOGGING = true;

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
    private static long duckReleaseStartMs = 0L;

    private static final List<Integer> shuffledOrder = new ArrayList<>();
    private static int shuffledPos = 0;

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

    public static void setUserVolume(float v) {
        userVolume = Math.max(0f, Math.min(1f, v));
        volumeLoadedFromConfig = true;
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
        long now = System.currentTimeMillis();
        long newUntil = now + durationMs;
        if (newUntil > duckUntilMs) {
            duckUntilMs = newUntil;
            duckReleaseStartMs = 0L;
        }
    }

    public static void start() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        // Si ya esta activo Y la musica esta sonando realmente, ignorar
        if (active && currentInstance != null && mc.getSoundManager().isActive(currentInstance)) {
            if (DEBUG_LOGGING) LOGGER.info("[MenuMusic v3] start() ignorado, ya esta sonando");
            return;
        }

        // Si llegamos aqui es porque o no esta activo, o la instancia murio (ej: al salir de un server)
        if (DEBUG_LOGGING && active) LOGGER.info("[MenuMusic v3] Reiniciando porque la instancia previa no existe o no suena");
        
        stop(); // Limpieza de seguridad
        if (mc == null) return;

        active = true;
        rebuildShuffle();
        playNextTrack();

        if (DEBUG_LOGGING) LOGGER.info("[MenuMusic v3] Iniciado");
    }

    public static void stop() {
        if (!active) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            if (currentInstance != null) {
                try { mc.getSoundManager().stop(currentInstance); currentInstance.markDone(); } catch (Exception ignored) {}
            }
            if (previousInstance != null) {
                try { mc.getSoundManager().stop(previousInstance); previousInstance.markDone(); } catch (Exception ignored) {}
            }
        }

        active = false;
        currentInstance = null;
        previousInstance = null;
        currentIndex = -1;
        waitingForNext = false;
        crossfading = false;
        duckUntilMs = 0L;

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

        if (!active) return;

        long now = System.currentTimeMillis();

        // Aplicar volumen (con crossfade + ducking) a instancias activas
        updateVolumes(now);

        // Gestionar crossfade activo
        if (crossfading) {
            long crossElapsed = now - crossfadeStartMs;
            if (crossElapsed >= CROSSFADE_MS) {
                // Crossfade termino: eliminar la instancia anterior
                if (previousInstance != null) {
                    try { mc.getSoundManager().stop(previousInstance); previousInstance.markDone(); } catch (Exception ignored) {}
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

        // Si no hay instancia actual, pasamos a la siguiente
        if (currentInstance == null || currentInstance.isStopped()) {
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
        if (!active || crossfading) return;
        if (currentInstance == null || currentInstance.isStopped()) {
            // No hay pista sonando; arrancar una nueva directamente
            playNextTrack();
            return;
        }
        startCrossfadeToNext();
        if (DEBUG_LOGGING) LOGGER.info("[MenuMusic v3] Skip manual solicitado");
    }

    /**
     * Devuelve true si hay un crossfade en curso (para que la UI ignore clicks).
     */
    public static boolean isCrossfading() {
        return crossfading;
    }
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

        // Fase attack (baja hacia DUCK_FACTOR)
        if (now < duckUntilMs) {
            // Estamos en periodo de duck activo, mantener bajo
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
        nextTrackAtMs = System.currentTimeMillis() + gap;
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
            currentStartMs = System.currentTimeMillis();
            currentDurationMs = TRACK_DURATIONS_MS[nextIndex];

            crossfadeStartMs = System.currentTimeMillis();
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

            currentStartMs = System.currentTimeMillis();
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