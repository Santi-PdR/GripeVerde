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
 * MenuAmbience v4 - loops con volumen dinámico fluido sin cortes
 * Slider ambiente ahora se comporta igual que música: setDynamicVolume continuo
 */
public class MenuAmbience {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean DEBUG_LOGGING = false;

    private static final long EVENT_MIN_MS = 15_000L;
    private static final long EVENT_MAX_MS = 40_000L;
    private static final long RARE_MIN_MS  = 30_000L;
    private static final long RARE_MAX_MS  = 60_000L;

    private static final float TICK_VOLUME    = 0.025f;
    private static final float TICK_PITCH_MIN = 0.95f;
    private static final float TICK_PITCH_MAX = 1.05f;

    private static class Entry {
        final RegistryObject<SoundEvent> sound;
        final int weight;
        final float volume;
        final float pitchMin, pitchMax;
        Entry(RegistryObject<SoundEvent> s,int w,float vol,float pMin,float pMax){sound=s;weight=w;volume=vol;pitchMin=pMin;pitchMax=pMax;}
    }

    private static class BaseLoop {
        final RegistryObject<SoundEvent> sound;
        final float baseVolume;
        final float stormMul;   // v0.24.0: factor de mezcla en noche de tormenta
        LoopingAmbienceInstance instance;
        BaseLoop(RegistryObject<SoundEvent> s,float vol,float sm){sound=s;baseVolume=vol;stormMul=sm;}
    }

    private static final List<BaseLoop> BASE_LOOPS = new ArrayList<>();
    private static final List<Entry> EVENTS_LAYER = new ArrayList<>();
    private static final List<Entry> RARE_LAYER = new ArrayList<>();

    static {
        // v0.25.1: la lluvia vuelve al master original (ganancia pura, cresta
        // intacta); su mezcla sube 0.30 -> 0.45 para sonar clara sin siseo.
        BASE_LOOPS.add(new BaseLoop(ModSounds.MENU_WIND, 0.35f, 1.25f));
        BASE_LOOPS.add(new BaseLoop(ModSounds.MENU_RAIN, 0.45f, 1.80f));
        BASE_LOOPS.add(new BaseLoop(ModSounds.MENU_CITY, 0.20f, 0.80f));

        EVENTS_LAYER.add(new Entry(ModSounds.MENU_FOOTSTEPS, 45,0.25f,0.90f,1.10f));
        EVENTS_LAYER.add(new Entry(ModSounds.MENU_WOOD_CREAK,20,0.22f,0.85f,1.15f));
        EVENTS_LAYER.add(new Entry(ModSounds.MENU_AMBIENT_OCCASIONAL,15,0.30f,0.95f,1.05f));

        RARE_LAYER.add(new Entry(ModSounds.MENU_BELL,40,0.45f,0.95f,1.05f));
        RARE_LAYER.add(new Entry(ModSounds.MENU_THUNDER,35,0.55f,0.95f,1.05f));
        RARE_LAYER.add(new Entry(ModSounds.MENU_CROWS,40,0.55f,0.90f,1.10f));
        RARE_LAYER.add(new Entry(ModSounds.MENU_CHAIN,10,0.30f,0.95f,1.05f));
        RARE_LAYER.add(new Entry(ModSounds.MENU_AMBIENT_OCCASIONAL,20,0.25f,0.55f,0.75f));
        RARE_LAYER.add(new Entry(ModSounds.MENU_CHAIN,25,0.40f,0.40f,0.55f));
    }

    private static boolean active=false, muted=false, pocketWatchVisible=false;
    private static long lastEventTriggerMs=0L, nextEventDelayMs=0L, lastRareTriggerMs=0L, nextRareDelayMs=0L;
    private static long lastPreviewMs = 0L;
    private static RegistryObject<SoundEvent> lastEventTriggered=null, lastRareTriggered=null;
    private static long crowsPlayingUntilMs=0L;
    public static boolean isCrowsRecentlyTriggered(){return nowMs()<crowsPlayingUntilMs;}
    private static final RandomSource RNG = RandomSource.create();

    /**
     * v0.23.1: reloj monotonico (nanoTime) para la programacion de eventos,
     * susurros, campanas y cuervos; estable frente a ajustes de hora del SO.
     * Sus API publicas de tiempo (lastBellMs) ahora devuelven este reloj; su
     * unico consumidor (LivingAtmosphere) usa el mismo reloj.
     */
    private static long nowMs() { return System.nanoTime() / 1_000_000L; }

    // ── v0.24.0: Noche de tormenta ──────────────────────────────────────────
    // Algunas sesiones el gabinete amanece bajo una tormenta cerrada: la lluvia
    // manda en la mezcla, los truenos se suceden y la calle casi no se ve.
    // El sorteo se hace una sola vez por sesion de juego (primer acceso).
    private static final float STORM_CHANCE = 0.14f;
    private static final long STORM_RARE_MIN_MS = 14_000L;
    private static final long STORM_RARE_MAX_MS = 26_000L;
    private static Boolean stormNight = null;
    private static boolean stormAnnounced = false;

    /** true si esta sesion del gabinete es una noche de tormenta. */
    public static boolean isStormNight(){
        if(stormNight==null){
            stormNight = RNG.nextFloat() < STORM_CHANCE;
        }
        return Boolean.TRUE.equals(stormNight);
    }

    private static long rareDelay(){
        return isStormNight()
            ? randomBetween(STORM_RARE_MIN_MS, STORM_RARE_MAX_MS)
            : randomBetween(RARE_MIN_MS, RARE_MAX_MS);
    }

    /** Ganancia efectiva de un loop base, con el factor de tormenta aplicado. */
    private static float loopGain(BaseLoop bl){
        return bl.baseVolume * getUserVolume() * (isStormNight() ? bl.stormMul : 1f);
    }

    // ── v0.16.0: interacciones de oido ──────────────────────────────────────
    // Ventana en la que un susurro puede responderse con un gesto.
    private static long whisperRespondableUntilMs=0L;
    // Momento en que doblaron las campanas (el coche del sepulturero sale despues).
    private static long lastBellMs=0L;

    public static boolean isWhisperOpen(){ return nowMs()<whisperRespondableUntilMs; }

    /** El lector responde al susurro (click en la vigilia dentro de la ventana). */
    public static void answerWhisper(){
        // v0.29.0 (pedido expreso del owner): sin golpe sordo al tocar — el
        // silencio queda en silencio. Solo el susurro abierto contesta.
        if(!isWhisperOpen()) return;
        whisperRespondableUntilMs=0L;
        Minecraft mc=Minecraft.getInstance();
        if(mc==null||mc.level!=null||!active||muted||getUserVolume()<0.01f) return;
        try{
            // El susurro responde: dos voces, una mas grave.
            mc.getSoundManager().play(ambientOneShot(ModSounds.MENU_AMBIENT_OCCASIONAL.get(),0.50f,0.20f*getUserVolume()));
            mc.getSoundManager().play(ambientOneShot(ModSounds.MENU_AMBIENT_OCCASIONAL.get(),0.38f,0.16f*getUserVolume()));
        }catch(Exception ignored){}
        try{
            int n = com.eternalcraft.gripeverde.client.lore.ChroniclerMemory.bump("whisperAnswers");
            com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                com.eternalcraft.gripeverde.client.lore.Discoveries.WHISPER_ANSWER);
            if(n>=3){
                com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                    com.eternalcraft.gripeverde.client.lore.Marginalia.N_WHISPER3);
            }
        }catch(Throwable ignored){}
    }

    public static long lastBellMs(){ return lastBellMs; }

    public static void recoverAfterResourceReload(){
        Minecraft mc=Minecraft.getInstance(); if(mc==null||mc.level!=null) return;
        // A reload callback can arrive while old channels are still alive.
        // Retire owned loops before replacing their references.
        stop();
        lastAppliedVolume=-1f;
        start();
    }
    public static void setPocketWatchVisible(boolean v){pocketWatchVisible=v;}
    public static void onSecondTick(){
        if(!active||muted||!pocketWatchVisible||getUserVolume()<0.01f) return;
        Minecraft mc=Minecraft.getInstance(); if(mc==null||mc.level!=null) return;
        try{
            SoundEvent se=ModSounds.MENU_TICK.get();
            float pitch=randomFloatBetween(TICK_PITCH_MIN,TICK_PITCH_MAX);
            mc.getSoundManager().play(ambientOneShot(se,pitch,TICK_VOLUME*getUserVolume()));
        }catch(Exception e){ if(DEBUG_LOGGING) LOGGER.warn("[MenuAmbience] Tick no disponible"); }
    }

    public static void setMuted(boolean m){
        boolean changed=muted!=m;
        muted=m;
        // Persist even if the cache already matches (e.g. factory reset).
        try{ com.eternalcraft.gripeverde.config.ClientConfig.AMBIENCE_ENABLED.set(!m); com.eternalcraft.gripeverde.config.ClientConfig.AMBIENCE_ENABLED.save(); }catch(Exception ignored){}
        if(muted) whisperRespondableUntilMs=0L;
        if(!changed||!active) return;
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null&&mc.level!=null){ stop(); return; }
        if(muted) stopBaseLoops(mc);
        else if(mc!=null) ensureBaseLoopsAlive(mc,getUserVolume());
    }

    private static float userVolume=1.0f;
    private static boolean userVolumeLoadedFromConfig=false;
    public static float getUserVolume(){
        if(!userVolumeLoadedFromConfig){
            try{ userVolume=com.eternalcraft.gripeverde.config.ClientConfig.AMBIENCE_VOLUME.get().floatValue(); userVolumeLoadedFromConfig=true; }catch(Exception ignored){}
        }
        return userVolume;
    }
    /** Update the ambience volume while dragging, without writing the config file. */
    public static void setUserVolumeLive(float v) {
        userVolume = Math.max(0f, Math.min(1f, v));
        userVolumeLoadedFromConfig = true;
    }

    /** Persist the final slider value once the player releases the control. */
    public static void setUserVolume(float v) {
        setUserVolumeLive(v);
        try {
            com.eternalcraft.gripeverde.config.ClientConfig.AMBIENCE_VOLUME.set((double) userVolume);
            com.eternalcraft.gripeverde.config.ClientConfig.AMBIENCE_VOLUME.save();
        } catch (Exception ignored) {}
        // No rebuild here; updateLoopVolumes keeps the audio smooth.
    }

    // Volumen fluido sin cortes
    private static float lastAppliedVolume=-1f;
    private static void updateLoopVolumes(){
        float vol=getUserVolume();
        if(lastAppliedVolume<0f){ lastAppliedVolume=vol; return; }
        float diff=Math.abs(vol-lastAppliedVolume);
        // Actualizar dinámicamente si cambio >1% sin reconstruir
        if(diff>0.005f){
            lastAppliedVolume=vol;
            if(!active||muted) return;
            for(BaseLoop bl:BASE_LOOPS){
                if(bl.instance!=null){
                    bl.instance.setDynamicVolume(loopGain(bl));
                }
            }
        }
    }

    public static boolean isMuted(){return muted;}
    public static void toggleMute(){setMuted(!muted);}

    /** Plays one short sample through the same Ambient channel and volume as menu ambience. */
    public static void testSound() {
        long now = nowMs();
        if (now - lastPreviewMs < 600L) return;
        lastPreviewMs = now;
        if (muted || getUserVolume() < 0.01f) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level != null) return;
        try {
            SoundEvent sample = ModSounds.MENU_WOOD_CREAK.get();
            mc.getSoundManager().play(ambientOneShot(sample, 0.98f, 0.48f * getUserVolume()));
            MenuMusic.duckFor(1_000L);
        } catch (Throwable ignored) {}
    }

    public static void start(){
        Minecraft mc=Minecraft.getInstance();
        if(mc==null) return;
        if(mc.level!=null){ stop(); return; }
        if(active){ if(DEBUG_LOGGING) LOGGER.info("[MenuAmbience] start ignorado ya activo"); return; }
        try{ muted=!com.eternalcraft.gripeverde.config.ClientConfig.AMBIENCE_ENABLED.get(); }catch(Exception ignored){}
        active=true; pocketWatchVisible=false;
        ensureBaseLoopsAlive(mc,getUserVolume());
        long now=nowMs();
        lastEventTriggerMs=now; nextEventDelayMs=randomBetween(EVENT_MIN_MS,EVENT_MAX_MS);
        lastRareTriggerMs=now; nextRareDelayMs=rareDelay();
        lastEventTriggered=null; lastRareTriggered=null;
        lastAppliedVolume=getUserVolume();
    }

    private static void stopBaseLoops(Minecraft mc){
        for(BaseLoop bl:BASE_LOOPS){
            LoopingAmbienceInstance old=bl.instance;
            bl.instance=null;
            if(old==null) continue;
            old.markDone();
            try{ if(mc!=null) mc.getSoundManager().stop(old); }catch(Exception ignored){}
        }
    }

    public static void stop(){
        // Clear ownership even during client teardown or a sound-engine failure.
        active=false; pocketWatchVisible=false; lastEventTriggered=null; lastRareTriggered=null;
        whisperRespondableUntilMs=0L;
        crowsPlayingUntilMs=0L;
        stopBaseLoops(Minecraft.getInstance());
    }

    public static void tick(){
        Minecraft mc=Minecraft.getInstance(); if(mc==null) return;
        // v0.24.0: la calle del gabinete calla cuando el lector entra a un mundo
        // (antes los loops seguian sonando bajo el gameplay). Al volver al
        // titulo, DiaryTitleScreen.init() llama a start() y todo se rearma.
        if(mc.level!=null){ stop(); return; }
        float ambVol=getUserVolume();
        if(ambVol<0.01f){
            whisperRespondableUntilMs=0L;
            stopBaseLoops(mc);
            return;
        }
        updateLoopVolumes();
        // v0.24.0: la tormenta se presenta una sola vez, con o sin sonido.
        if(isStormNight() && !stormAnnounced){
            stormAnnounced=true;
            try{
                com.eternalcraft.gripeverde.client.lore.Discoveries.discover(
                    com.eternalcraft.gripeverde.client.lore.Discoveries.STORM_NIGHT);
                com.eternalcraft.gripeverde.client.lore.Marginalia.queueOnce(
                    com.eternalcraft.gripeverde.client.lore.Marginalia.N_STORM);
            }catch(Throwable ignored){}
        }
        if(!active||muted) return;
        ensureBaseLoopsAlive(mc, ambVol);
        long now=nowMs();
        if(now-lastEventTriggerMs>=nextEventDelayMs){ triggerFrom(mc,EVENTS_LAYER,true); lastEventTriggerMs=now; nextEventDelayMs=randomBetween(EVENT_MIN_MS,EVENT_MAX_MS); }
        if(now-lastRareTriggerMs>=nextRareDelayMs){ triggerFrom(mc,RARE_LAYER,false); lastRareTriggerMs=now; nextRareDelayMs=rareDelay(); }
    }

    public static boolean isActive(){return active;}

    private static void ensureBaseLoopsAlive(Minecraft mc,float ambVol){
        if(!active||muted||ambVol<0.01f) return;
        for(BaseLoop bl:BASE_LOOPS){
            try{
                boolean missing=bl.instance==null||!mc.getSoundManager().isActive(bl.instance);
                if(missing){
                    float finalVol=loopGain(bl);
                    if(finalVol>0.001f){
                        bl.instance=buildLoopInstance(bl.sound.get(), finalVol);
                        mc.getSoundManager().play(bl.instance);
                    }
                }
            }catch(Throwable ignored){ bl.instance=null; }
        }
    }

    private static void triggerFrom(Minecraft mc,List<Entry> layer,boolean isEvents){
        if(layer.isEmpty()) return;
        RegistryObject<SoundEvent> last=isEvents?lastEventTriggered:lastRareTriggered;
        List<Entry> cands;
        if(layer.size()==1||last==null) cands=layer;
        else{
            cands=new ArrayList<>();
            for(Entry e:layer) if(e.sound!=last) cands.add(e);
            if(cands.isEmpty()) cands=layer;
        }
        Entry chosen=weightedRandom(cands); if(chosen==null) return;
        // v0.24.0: en noche de tormenta el cielo manda; casi todo lo raro es trueno.
        if(!isEvents && isStormNight() && RNG.nextFloat()<0.80f){
            for(Entry e:layer){ if(e.sound==ModSounds.MENU_THUNDER){ chosen=e; break; } }
        }
        try{
            SoundEvent se=chosen.sound.get();
            float pitch=randomFloatBetween(chosen.pitchMin,chosen.pitchMax);
            mc.getSoundManager().play(ambientOneShot(se,pitch,chosen.volume*getUserVolume()));
            if(isEvents) lastEventTriggered=chosen.sound;
            else{
                lastRareTriggered=chosen.sound;
                MenuMusic.duckFor(2500L);
                if(chosen.sound==ModSounds.MENU_CROWS) crowsPlayingUntilMs=nowMs()+4000L;
                else if(chosen.sound==ModSounds.MENU_THUNDER) com.eternalcraft.gripeverde.client.render.LivingAtmosphere.triggerLightningFlash(1.0f);
                else if(chosen.sound==ModSounds.MENU_BELL) lastBellMs=nowMs();
                else if(chosen.sound==ModSounds.MENU_AMBIENT_OCCASIONAL && chosen.pitchMax<0.8f){
                    // Un susurro: puede responderse durante unos segundos.
                    whisperRespondableUntilMs=nowMs()+3000L;
                }
            }
        }catch(Exception e){ if(DEBUG_LOGGING) LOGGER.warn("[MenuAmbience] Sonido no disponible"); }
    }

    private static Entry weightedRandom(List<Entry> entries){
        if(entries.isEmpty()) return null;
        int total=0; for(Entry e:entries) total+=e.weight;
        if(total<=0) return entries.get(0);
        int roll=RNG.nextInt(total); int acc=0;
        for(Entry e:entries){ acc+=e.weight; if(roll<acc) return e; }
        return entries.get(entries.size()-1);
    }
    private static long randomBetween(long min,long max){ if(max<=min) return min; return min+(long)(RNG.nextDouble()*(max-min)); }
    private static float randomFloatBetween(float min,float max){ if(max<=min) return min; return min+RNG.nextFloat()*(max-min); }

    /** One-shot no posicional en el canal MASTER, igual que el bucle del ambiente. */
    private static SimpleSoundInstance ambientOneShot(SoundEvent sound,float pitch,float volume) {
        // v0.184.0: MASTER; el ambiente del mod no depende del slider vanilla "Ambiente".
        return new SimpleSoundInstance(sound.getLocation(), SoundSource.MASTER, volume, pitch,
            RandomSource.create(), false, 0, SoundInstance.Attenuation.NONE,
            0.0, 0.0, 0.0, true);
    }

    private static LoopingAmbienceInstance buildLoopInstance(SoundEvent sound,float volume){
        return new LoopingAmbienceInstance(sound.getLocation(), volume);
    }
}
