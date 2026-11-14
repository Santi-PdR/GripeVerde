package com.eternalcraft.gripeverde.client.audio;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * Loop de ambiente con volumen dinámico para slider fluido sin cortes.
 * Similar a FadingMusicInstance pero en loop infinito.
 */
public class LoopingAmbienceInstance extends AbstractSoundInstance implements TickableSoundInstance {

    private boolean done = false;
    private float dynamicVolume = 0f;

    public LoopingAmbienceInstance(ResourceLocation loc, float initialVolume) {
        // v0.184.0: canal MASTER para que el slider "Ambiente" de vanilla no toque el mod.
        super(loc, SoundSource.MASTER, RandomSource.create());
        this.volume = initialVolume;
        this.dynamicVolume = initialVolume;
        this.pitch = 1.0f;
        this.looping = true;
        this.delay = 0;
        this.attenuation = Attenuation.NONE;
        this.relative = true;
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
    }

    public void setDynamicVolume(float v) {
        this.dynamicVolume = Math.max(0f, Math.min(1f, v));
        this.volume = this.dynamicVolume;
    }

    @Override
    public boolean isStopped() { return done; }

    public void markDone() { this.done = true; }

    @Override
    public void tick() {
        // Volumen se actualiza desde MenuAmbience.tick() via setDynamicVolume
    }
}
