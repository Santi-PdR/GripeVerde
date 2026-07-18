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
import java.util.List;

/**
 * MenuAmbience v3 - Sistema de audio ambiental por capas.
 *
 * Capas:
 *   BASE   - loops permanentes (viento, lluvia, ciudad)
 *   EVENTS - sonidos ocasionales (15-40s)
 *   RARE   - sonidos firma (90-240s)
 *   TICK   - sincronizado con el segundero del reloj de bolsillo
 *
 * API publica:
 *   MenuAmbience.start()
 *   MenuAmbience.stop()
 *   MenuAmbience.tick()
 *   MenuAmbience.isActive()
 *   MenuAmbience.setPocketWatchVisible(boolean)
 *   MenuAmbience.onSecondTick() â€” llamado por PocketWatchRenderer
 */
public class MenuAmbience {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final boolean DEBUG_LOGGING = false;

    // TIMERS
    private static final long EVENT_MIN_MS = 15_000L;
    private static final long EVENT_MAX_MS = 40_000L;
    private static final long RARE_MIN_MS  = 90_000L;
    private static final long RARE_MAX_MS  = 240_000L;

    private static final float TICK_VOLUME    = 0.025f;
    private static final float TICK_PITCH_MIN = 0.95f;
    private static final float TICK_PITCH_MAX = 1.05f;

    private static class Entry {
        final RegistryObject<SoundEvent> sound;
        final int weight;
        final float volume;
        final float pitchMin;
        final float pitchMax;

        Entry(RegistryObject<SoundEvent> sound, int weight, float volume,
              float pitchMin, float pitchMax) {
            this.sound = sound;
            this.weight = weight;
            this.volume = volume;
            this.pitchMin = pitchMin;
            this.pitchMax = pitchMax;
        }
    }

    private static class BaseLoop {
        final RegistryObject<SoundEvent> sound;
        final float volume;
        SoundInstance instance;

        BaseLoop(RegistryObject<SoundEvent> sound, float volume) {
            this.sound = sound;
            this.volume = volume;
        }
    }

    private static final List<BaseLoop> BASE_LOOPS   = new ArrayList<>();
    private static final List<Entry>    EVENTS_LAYER = new ArrayList<>();
    private static final List<Entry>    RARE_LAYER   = new ArrayList<>();

    static {
        BASE_LOOPS.add(new BaseLoop(ModSounds.MENU_WIND, 0.06f));
        BASE_LOOPS.add(new BaseLoop(ModSounds.MENU_RAIN, 0.05f));
        BASE_LOOPS.add(new BaseLoop(ModSounds.MENU_CITY, 0.03f));

        EVENTS_LAYER.add(new Entry(ModSounds.MENU_FOOTSTEPS,          40, 0.04f, 0.90f, 1.10f));
        EVENTS_LAYER.add(new Entry(ModSounds.MENU_WOOD_CREAK,         35, 0.05f, 0.85f, 1.15f));
        EVENTS_LAYER.add(new Entry(ModSounds.MENU_DOOR,               15, 0.05f, 0.90f, 1.10f));
        EVENTS_LAYER.add(new Entry(ModSounds.MENU_AMBIENT_OCCASIONAL, 10, 0.06f, 0.95f, 1.05f));

        RARE_LAYER.add(new Entry(ModSounds.MENU_BELL,    40, 0.09f, 0.95f, 1.05f));
        RARE_LAYER.add(new Entry(ModSounds.MENU_THUNDER, 35, 0.10f, 0.95f, 1.05f));
        RARE_LAYER.add(new Entry(ModSounds.MENU_CROWS,   15, 0.07f, 0.90f, 1.10f));
        RARE_LAYER.add(new Entry(ModSounds.MENU_CHAIN,   10, 0.05f, 0.95f, 1.05f));
    }

    private static boolean active = false;
    private static boolean pocketWatchVisible = false;

    private static long lastEventTriggerMs = 0L;
    private static long nextEventDelayMs   = 0L;
    private static long lastRareTriggerMs  = 0L;
    private static long nextRareDelayMs    = 0L;

    private static RegistryObject<SoundEvent> lastEventTriggered = null;
    private static RegistryObject<SoundEvent> lastRareTriggered  = null;
    private static final RandomSource RNG = RandomSource.create();

    public static void setPocketWatchVisible(boolean visible) {
        pocketWatchVisible = visible;
    }

    /**
     * Llamado por PocketWatchRenderer cada vez que el segundero
     * avanza a una nueva posicion. Si el reloj es visible y el
     * sistema esta activo, reproduce el tick.
     */
    public static void onSecondTick() {
        if (!active || !pocketWatchVisible) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        try {
            SoundEvent se = ModSounds.MENU_TICK.get();
            float pitch = randomFloatBetween(TICK_PITCH_MIN, TICK_PITCH_MAX);
            mc.getSoundManager().play(
                SimpleSoundInstance.forUI(se, pitch, TICK_VOLUME));

            if (DEBUG_LOGGING) {
                LOGGER.info("[MenuAmbience] TICK disparado (vol={}, pitch={})",
                    TICK_VOLUME, pitch);
            }
        } catch (Exception e) {
            if (DEBUG_LOGGING) LOGGER.warn("[MenuAmbience] Tick no disponible: {}", e.getMessage());
        }
    }

    public static void start() {
        if (active) stop();

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            if (DEBUG_LOGGING) LOGGER.warn("[MenuAmbience] Minecraft null, abortando start()");
            return;
        }

        active = true;
        pocketWatchVisible = false;

        for (BaseLoop bl : BASE_LOOPS) {
            try {
                SoundEvent se = bl.sound.get();
                bl.instance = buildLoopInstance(se, bl.volume);
                mc.getSoundManager().play(bl.instance);
                if (DEBUG_LOGGING) LOGGER.info("[MenuAmbience] Loop iniciado: {}", se.getLocation());
            } catch (Exception e) {
                if (DEBUG_LOGGING) LOGGER.warn("[MenuAmbience] Loop no disponible: {}", e.getMessage());
                bl.instance = null;
            }
        }

        long now = System.currentTimeMillis();
        lastEventTriggerMs = now;
        nextEventDelayMs   = randomBetween(EVENT_MIN_MS, EVENT_MAX_MS);
        lastRareTriggerMs  = now;
        nextRareDelayMs    = randomBetween(RARE_MIN_MS, RARE_MAX_MS);
        lastEventTriggered = null;
        lastRareTriggered  = null;

        if (DEBUG_LOGGING) {
            LOGGER.info("[MenuAmbience] Ambient iniciado (BASE:{}, EVENTS:{}, RARE:{}, TICK:sync)",
                BASE_LOOPS.size(), EVENTS_LAYER.size(), RARE_LAYER.size());
        }
    }

    public static void stop() {
        if (!active) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        for (BaseLoop bl : BASE_LOOPS) {
            if (bl.instance != null) {
                mc.getSoundManager().stop(bl.instance);
                bl.instance = null;
            }
        }

        active = false;
        pocketWatchVisible = false;
        lastEventTriggered = null;
        lastRareTriggered  = null;

        if (DEBUG_LOGGING) LOGGER.info("[MenuAmbience] Ambient detenido");
    }

    public static void tick() {
        if (!active) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        long now = System.currentTimeMillis();

        if (now - lastEventTriggerMs >= nextEventDelayMs) {
            triggerFrom(mc, EVENTS_LAYER, true);
            lastEventTriggerMs = now;
            nextEventDelayMs   = randomBetween(EVENT_MIN_MS, EVENT_MAX_MS);
        }

        if (now - lastRareTriggerMs >= nextRareDelayMs) {
            triggerFrom(mc, RARE_LAYER, false);
            lastRareTriggerMs = now;
            nextRareDelayMs   = randomBetween(RARE_MIN_MS, RARE_MAX_MS);
        }
    }

    public static boolean isActive() { return active; }

    private static void triggerFrom(Minecraft mc, List<Entry> layer, boolean isEventsLayer) {
        if (layer.isEmpty()) return;

        RegistryObject<SoundEvent> lastTriggered = isEventsLayer ? lastEventTriggered : lastRareTriggered;
        List<Entry> candidates;
        if (layer.size() == 1 || lastTriggered == null) {
            candidates = layer;
        } else {
            candidates = new ArrayList<>();
            for (Entry e : layer) {
                if (e.sound != lastTriggered) candidates.add(e);
            }
            if (candidates.isEmpty()) candidates = layer;
        }

        Entry chosen = weightedRandom(candidates);
        if (chosen == null) return;

        try {
            SoundEvent se = chosen.sound.get();
            float pitch = randomFloatBetween(chosen.pitchMin, chosen.pitchMax);
            mc.getSoundManager().play(
                SimpleSoundInstance.forUI(se, pitch, chosen.volume));

            if (isEventsLayer) lastEventTriggered = chosen.sound;
            else {
                lastRareTriggered  = chosen.sound;
                // Ducking: bajar musica durante sonidos raros importantes
                MenuMusic.duckFor(2500L);
            }

            if (DEBUG_LOGGING) {
                LOGGER.info("[MenuAmbience] {} disparado: {} (vol={}, pitch={})",
                    isEventsLayer ? "EVENT" : "RARE",
                    se.getLocation(), chosen.volume, pitch);
            }
        } catch (Exception e) {
            if (DEBUG_LOGGING) LOGGER.warn("[MenuAmbience] Sonido no disponible, saltando: {}", e.getMessage());
        }
    }

    private static Entry weightedRandom(List<Entry> entries) {
        if (entries.isEmpty()) return null;
        int total = 0;
        for (Entry e : entries) total += e.weight;
        if (total <= 0) return entries.get(0);

        int roll = RNG.nextInt(total);
        int acc  = 0;
        for (Entry e : entries) {
            acc += e.weight;
            if (roll < acc) return e;
        }
        return entries.get(entries.size() - 1);
    }

    private static long randomBetween(long min, long max) {
        if (max <= min) return min;
        return min + (long)(RNG.nextDouble() * (max - min));
    }

    private static float randomFloatBetween(float min, float max) {
        if (max <= min) return min;
        return min + RNG.nextFloat() * (max - min);
    }

    private static SoundInstance buildLoopInstance(SoundEvent sound, float volume) {
        return new SimpleSoundInstance(
            sound.getLocation(),
            SoundSource.AMBIENT,
            volume,
            1.0f,
            RNG,
            true,
            0,
            SoundInstance.Attenuation.NONE,
            0, 0, 0,
            true
        );
    }
}