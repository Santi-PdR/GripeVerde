package com.eternalcraft.gripeverde.client.audio;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * Instancia de sonido con volumen dinamico controlable desde fuera.
 * Permite crossfade y ducking en tiempo real sin cortar el sample.
 */
public class FadingMusicInstance extends AbstractSoundInstance implements TickableSoundInstance {

    private boolean done = false;
    private float dynamicVolume = 0f;
    private final long durationMs;
    private final long startMs;

    public FadingMusicInstance(SoundEvent sound, float initialVolume, long durationMs) {
        // v0.184.0: canal MASTER en vez de MUSIC. La banda sonora del mod ya no se
        // apaga ni se atenua con el slider "Musica" de Minecraft: manda el slider
        // propio del cronista y, por encima, el volumen maestro general.
        super(sound.getLocation(), SoundSource.MASTER, RandomSource.create());
        this.volume = initialVolume;
        this.dynamicVolume = initialVolume;
        this.pitch = 1.0f;
        this.looping = false;
        this.delay = 0;
        this.attenuation = Attenuation.NONE;
        this.relative = true;
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
        this.durationMs = durationMs;
        // v0.23.0: reloj monotonico; los saltos de hora del SO ya no alargan pistas.
        this.startMs = System.nanoTime() / 1_000_000L;
    }

    public void setDynamicVolume(float v) {
        this.dynamicVolume = Math.max(0f, Math.min(1f, v));
        this.volume = this.dynamicVolume;
    }

    public float getDynamicVolume() { return dynamicVolume; }

    public long getElapsedMs() { return System.nanoTime() / 1_000_000L - startMs; }

    public long getRemainingMs() { return Math.max(0, durationMs - getElapsedMs()); }

    public void markDone() { this.done = true; }

    @Override
    public boolean isStopped() { return done; }

    @Override
    public void tick() {
        // El volumen se actualiza desde MenuMusic.tick(). Aqui solo verificamos si expiro.
        if (getElapsedMs() > durationMs + 1000L) {
            done = true;
        }
    }
}
