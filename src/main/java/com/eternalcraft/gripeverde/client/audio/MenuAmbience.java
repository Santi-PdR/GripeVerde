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
        LoopingAmbienceInstance instance;
        BaseLoop(RegistryObject<SoundEvent> s,float vol){sound=s;baseVolume=vol;}
    }

    private static final List<BaseLoop> BASE_LOOPS = new ArrayList<>();
    private static final List<Entry> EVENTS_LAYER = new ArrayList<>();
    private static final List<Entry> RARE_LAYER = new ArrayList<>();

    static {
        BASE_LOOPS.add(new BaseLoop(ModSounds.MENU_WIND, 0.35f));
        BASE_LOOPS.add(new BaseLoop(ModSounds.MENU_RAIN, 0.30f));
        BASE_LOOPS.add(new BaseLoop(ModSounds.MENU_CITY, 0.20f));

        EVENTS_LAYER.add(new Entry(ModSounds.MENU_FOOTSTEPS, 40,0.25f,0.90f,1.10f));
        EVENTS_LAYER.add(new Entry(ModSounds.MENU_WOOD_CREAK,35,0.30f,0.85f,1.15f));
        EVENTS_LAYER.add(new Entry(ModSounds.MENU_DOOR,15,0.35f,0.90f,1.10f));
        EVENTS_LAYER.add(new Entry(ModSounds.MENU_AMBIENT_OCCASIONAL,10,0.30f,0.95f,1.05f));

        RARE_LAYER.add(new Entry(ModSounds.MENU_BELL,40,0.45f,0.95f,1.05f));
        RARE_LAYER.add(new Entry(ModSounds.MENU_THUNDER,35,0.55f,0.95f,1.05f));
        RARE_LAYER.add(new Entry(ModSounds.MENU_CROWS,40,0.55f,0.90f,1.10f));
        RARE_LAYER.add(new Entry(ModSounds.MENU_CHAIN,10,0.30f,0.95f,1.05f));
        RARE_LAYER.add(new Entry(ModSounds.MENU_AMBIENT_OCCASIONAL,20,0.25f,0.55f,0.75f));
        RARE_LAYER.add(new Entry(ModSounds.MENU_CHAIN,25,0.40f,0.40f,0.55f));
    }

    private static boolean active=false, muted=false, pocketWatchVisible=false;
    private static long lastEventTriggerMs=0L, nextEventDelayMs=0L, lastRareTriggerMs=0L, nextRareDelayMs=0L;
    private static RegistryObject<SoundEvent> lastEventTriggered=null, lastRareTriggered=null;
    private static long crowsPlayingUntilMs=0L;
    public static boolean isCrowsRecentlyTriggered(){return System.currentTimeMillis()<crowsPlayingUntilMs;}
    private static final RandomSource RNG = RandomSource.create();

    public static void recoverAfterResourceReload(){
        Minecraft mc=Minecraft.getInstance(); if(mc==null||mc.level!=null) return;
        for(BaseLoop bl:BASE_LOOPS) bl.instance=null;
        active=false; lastAppliedVolume=-1f; start();
    }
    public static void setPocketWatchVisible(boolean v){pocketWatchVisible=v;}
    public static void onSecondTick(){
        if(!active||!pocketWatchVisible) return;
        Minecraft mc=Minecraft.getInstance(); if(mc==null) return;
        try{
            SoundEvent se=ModSounds.MENU_TICK.get();
            float pitch=randomFloatBetween(TICK_PITCH_MIN,TICK_PITCH_MAX);
            mc.getSoundManager().play(SimpleSoundInstance.forUI(se,pitch,TICK_VOLUME*getUserVolume()));
        }catch(Exception e){ if(DEBUG_LOGGING) LOGGER.warn("[MenuAmbience] Tick no disponible"); }
    }

    public static void setMuted(boolean m){
        if(muted==m) return; muted=m;
        try{ com.eternalcraft.gripeverde.config.ClientConfig.AMBIENCE_ENABLED.set(!m); com.eternalcraft.gripeverde.config.ClientConfig.AMBIENCE_ENABLED.save(); }catch(Exception ignored){}
        if(!active) return;
        Minecraft mc=Minecraft.getInstance(); if(mc==null) return;
        if(muted){
            for(BaseLoop bl:BASE_LOOPS){ if(bl.instance!=null){ mc.getSoundManager().stop(bl.instance); bl.instance=null; } }
        }else{
            for(BaseLoop bl:BASE_LOOPS){
                try{
                    SoundEvent se=bl.sound.get();
                    bl.instance=buildLoopInstance(se, bl.baseVolume*getUserVolume());
                    mc.getSoundManager().play(bl.instance);
                }catch(Exception e){ bl.instance=null; }
            }
        }
    }

    private static float userVolume=1.0f;
    private static boolean userVolumeLoadedFromConfig=false;
    public static float getUserVolume(){
        if(!userVolumeLoadedFromConfig){
            try{ userVolume=com.eternalcraft.gripeverde.config.ClientConfig.AMBIENCE_VOLUME.get().floatValue(); userVolumeLoadedFromConfig=true; }catch(Exception ignored){}
        }
        return userVolume;
    }
    public static void setUserVolume(float v){
        userVolume=Math.max(0f,Math.min(1f,v)); userVolumeLoadedFromConfig=true;
        try{ com.eternalcraft.gripeverde.config.ClientConfig.AMBIENCE_VOLUME.set((double)userVolume); com.eternalcraft.gripeverde.config.ClientConfig.AMBIENCE_VOLUME.save(); }catch(Exception ignored){}
        // No rebuild aquí, updateLoopVolumes lo hace fluido
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
                    float finalVol=bl.baseVolume*vol;
                    bl.instance.setDynamicVolume(finalVol);
                }
            }
        }
    }

    public static boolean isMuted(){return muted;}
    public static void toggleMute(){setMuted(!muted);}

    public static void start(){
        try{ muted=!com.eternalcraft.gripeverde.config.ClientConfig.AMBIENCE_ENABLED.get(); }catch(Exception ignored){}
        if(active){ if(DEBUG_LOGGING) LOGGER.info("[MenuAmbience] start ignorado ya activo"); return; }
        Minecraft mc=Minecraft.getInstance(); if(mc==null) return;
        active=true; pocketWatchVisible=false;
        if(!muted){
            for(BaseLoop bl:BASE_LOOPS){
                try{
                    SoundEvent se=bl.sound.get();
                    bl.instance=buildLoopInstance(se, bl.baseVolume*getUserVolume());
                    mc.getSoundManager().play(bl.instance);
                }catch(Exception e){ bl.instance=null; }
            }
        }
        long now=System.currentTimeMillis();
        lastEventTriggerMs=now; nextEventDelayMs=randomBetween(EVENT_MIN_MS,EVENT_MAX_MS);
        lastRareTriggerMs=now; nextRareDelayMs=randomBetween(RARE_MIN_MS,RARE_MAX_MS);
        lastEventTriggered=null; lastRareTriggered=null;
        lastAppliedVolume=getUserVolume();
    }

    public static void stop(){
        if(!active) return;
        Minecraft mc=Minecraft.getInstance(); if(mc==null) return;
        for(BaseLoop bl:BASE_LOOPS){ if(bl.instance!=null){ mc.getSoundManager().stop(bl.instance); bl.instance=null; } }
        active=false; pocketWatchVisible=false; lastEventTriggered=null; lastRareTriggered=null;
    }

    public static void tick(){
        float ambVol=getUserVolume();
        if(ambVol<0.01f){
            if(active&&!muted){
                Minecraft mc=Minecraft.getInstance();
                if(mc!=null){ for(BaseLoop bl:BASE_LOOPS){ if(bl.instance!=null){ mc.getSoundManager().stop(bl.instance); bl.instance=null; } } }
            }
            return;
        }
        updateLoopVolumes();
        if(!active||muted) return;
        Minecraft mc=Minecraft.getInstance(); if(mc==null) return;
        ensureBaseLoopsAlive(mc, ambVol);
        long now=System.currentTimeMillis();
        if(now-lastEventTriggerMs>=nextEventDelayMs){ triggerFrom(mc,EVENTS_LAYER,true); lastEventTriggerMs=now; nextEventDelayMs=randomBetween(EVENT_MIN_MS,EVENT_MAX_MS); }
        if(now-lastRareTriggerMs>=nextRareDelayMs){ triggerFrom(mc,RARE_LAYER,false); lastRareTriggerMs=now; nextRareDelayMs=randomBetween(RARE_MIN_MS,RARE_MAX_MS); }
    }

    public static boolean isActive(){return active;}

    private static void ensureBaseLoopsAlive(Minecraft mc,float ambVol){
        if(!active||muted||ambVol<0.01f) return;
        for(BaseLoop bl:BASE_LOOPS){
            try{
                boolean missing=bl.instance==null||!mc.getSoundManager().isActive(bl.instance);
                if(missing){
                    float finalVol=bl.baseVolume*ambVol;
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
        try{
            SoundEvent se=chosen.sound.get();
            float pitch=randomFloatBetween(chosen.pitchMin,chosen.pitchMax);
            mc.getSoundManager().play(SimpleSoundInstance.forUI(se,pitch,chosen.volume*getUserVolume()));
            if(isEvents) lastEventTriggered=chosen.sound;
            else{
                lastRareTriggered=chosen.sound;
                MenuMusic.duckFor(2500L);
                if(chosen.sound==ModSounds.MENU_CROWS) crowsPlayingUntilMs=System.currentTimeMillis()+4000L;
                else if(chosen.sound==ModSounds.MENU_THUNDER) com.eternalcraft.gripeverde.client.render.LivingAtmosphere.triggerLightningFlash(1.0f);
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

    private static LoopingAmbienceInstance buildLoopInstance(SoundEvent sound,float volume){
        return new LoopingAmbienceInstance(sound.getLocation(), volume);
    }
}
