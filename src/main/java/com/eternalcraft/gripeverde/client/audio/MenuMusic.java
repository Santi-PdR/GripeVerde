package com.eternalcraft.gripeverde.client.audio;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MenuMusic v1 - Reproduccion secuencial de pistas de musica del Title.
 *
 * v0.8.0:
 *   - Playlist de 4 pistas.
 *   - Orden aleatorio (shuffle).
 *   - Cuando termina una pista, arranca la siguiente tras breve pausa.
 *   - Sin crossfade, sin ducking.
 *   - Tolerante a RegistryObject vacio (workaround ModernFix).
 *
 * API:
 *   MenuMusic.start()
 *   MenuMusic.stop()
 *   MenuMusic.tick()
 */
public class MenuMusic {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean DEBUG_LOGGING = false;

    private static final float MUSIC_VOLUME = 0.6f;

    // Pausa entre pistas (ms)
    private static final long GAP_MIN_MS = 3_000L;
    private static final long GAP_MAX_MS = 8_000L;

    // Duraciones reales de cada pista, medidas al convertir (ms)
    // Sirven de fallback cuando el SoundManager no reporta fin.
    // music_1: 2:40 = 160s | music_2: 1:30 = 90s
    // music_3: 2:57 = 177s | music_4: 2:40 = 160s
    private static final long[] TRACK_DURATIONS_MS = {
        160_000L, 90_000L, 177_000L, 160_000L
    };

    private static final List<RegistryObject<SoundEvent>> PLAYLIST = new ArrayList<>();
    static {
        PLAYLIST.add(ModSounds.MENU_MUSIC_1);
        PLAYLIST.add(ModSounds.MENU_MUSIC_2);
        PLAYLIST.add(ModSounds.MENU_MUSIC_3);
        PLAYLIST.add(ModSounds.MENU_MUSIC_4);
    }

    private static final RandomSource RNG = RandomSource.create();

    private static boolean active = false;
    private static SoundInstance currentInstance = null;
    private static int currentIndex = -1;
    private static long currentStartMs = 0L;
    private static long currentDurationMs = 0L;
    private static long nextTrackAtMs = 0L;
    private static boolean waitingForNext = false;

    private static final List<Integer> shuffledOrder = new ArrayList<>();
    private static int shuffledPos = 0;

    public static void start() {
        if (active) stop();

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        active = true;
        rebuildShuffle();
        playNextTrack();

        if (DEBUG_LOGGING) LOGGER.info("[MenuMusic] Iniciado");
    }

    public static void stop() {
        if (!active) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc != null && currentInstance != null) {
            try { mc.getSoundManager().stop(currentInstance); } catch (Exception ignored) {}
        }

        active = false;
        currentInstance = null;
        currentIndex = -1;
        waitingForNext = false;

        if (DEBUG_LOGGING) LOGGER.info("[MenuMusic] Detenido");
    }

    public static void tick() {
        if (!active) return;

        long now = System.currentTimeMillis();

        // Estamos esperando el gap entre pistas
        if (waitingForNext) {
            if (now >= nextTrackAtMs) {
                waitingForNext = false;
                playNextTrack();
            }
            return;
        }

        // Estamos reproduciendo. Verificar si termino.
        if (currentInstance == null) {
            queueNextWithGap();
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        boolean stillPlaying;
        try {
            stillPlaying = mc.getSoundManager().isActive(currentInstance);
        } catch (Exception e) {
            stillPlaying = false;
        }

        // Doble check por duracion estimada
        boolean expired = (now - currentStartMs) >= currentDurationMs + 500L;

        if (!stillPlaying || expired) {
            currentInstance = null;
            queueNextWithGap();
        }
    }

    private static void queueNextWithGap() {
        long gap = GAP_MIN_MS + (long)(RNG.nextDouble() * (GAP_MAX_MS - GAP_MIN_MS));
        nextTrackAtMs = System.currentTimeMillis() + gap;
        waitingForNext = true;

        if (DEBUG_LOGGING) LOGGER.info("[MenuMusic] Proxima pista en {} ms", gap);
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
            if (DEBUG_LOGGING) LOGGER.warn("[MenuMusic] RegistryObject vacio para pista {}", currentIndex + 1);
            queueNextWithGap();
            return;
        }
        if (se == null) { queueNextWithGap(); return; }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        try {
            currentInstance = new SimpleSoundInstance(
                se.getLocation(),
                SoundSource.AMBIENT,
                MUSIC_VOLUME,
                1.0f,
                RNG,
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0, 0, 0,
                true
            );
            mc.getSoundManager().play(currentInstance);

            currentStartMs = System.currentTimeMillis();
            currentDurationMs = TRACK_DURATIONS_MS[currentIndex];

            if (DEBUG_LOGGING) {
                LOGGER.info("[MenuMusic] Pista {}: {} ({} ms)",
                    currentIndex + 1, se.getLocation(), currentDurationMs);
            }
        } catch (Exception e) {
            if (DEBUG_LOGGING) LOGGER.warn("[MenuMusic] Error al reproducir: {}", e.getMessage());
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