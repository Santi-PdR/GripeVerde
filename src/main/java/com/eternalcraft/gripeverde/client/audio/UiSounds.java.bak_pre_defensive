package com.eternalcraft.gripeverde.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;

/**
 * UiSounds v4 â€” Sistema de audio custom para Gripe Verde.
 *
 * API publica:
 *   UiSounds.hover()           â€” roce de cuero/tela
 *   UiSounds.click()           â€” pestillo metalico
 *   UiSounds.clickHeavy()      â€” cerrojo de hierro
 *   UiSounds.back()            â€” papel cerrÃ¡ndose
 *   UiSounds.open()            â€” bisagra de madera
 *   UiSounds.close()           â€” cajon cerrÃ¡ndose
 *   UiSounds.modal()           â€” vidrio de boticario
 *   UiSounds.error()           â€” metal que no encaja
 *   UiSounds.tab()             â€” pagina de libro
 *   UiSounds.scroll()          â€” micro-tick
 *   UiSounds.success()         â€” cerrojo satisfactorio
 *   UiSounds.notification()    â€” nudillo en puerta
 *   UiSounds.clickCreate()     â€” fosforo encendiendose
 *   UiSounds.clickDestroy()    â€” cadena cayendo
 *   UiSounds.respawn()         â€” inhalacion suave
 */
public class UiSounds {

    // â”€â”€ Volumenes por categoria â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private static final float VOL_HOVER   = 0.12f;
    private static final float VOL_CLICK   = 0.30f;
    private static final float VOL_HEAVY   = 0.35f;
    private static final float VOL_BACK    = 0.25f;
    private static final float VOL_OPEN    = 0.20f;
    private static final float VOL_CLOSE   = 0.20f;
    private static final float VOL_MODAL   = 0.28f;
    private static final float VOL_ERROR   = 0.22f;
    private static final float VOL_TAB     = 0.20f;
    private static final float VOL_SCROLL  = 0.10f;
    private static final float VOL_SUCCESS = 0.32f;
    private static final float VOL_NOTIF   = 0.22f;
    private static final float VOL_CREATE  = 0.30f;
    private static final float VOL_DESTROY = 0.28f;
    private static final float VOL_RESPAWN = 0.22f;

    // â”€â”€ Pitch base (variacion aleatoria aplicada en play) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private static final float PITCH_BASE  = 1.0f;
    private static final float PITCH_VAR   = 0.05f; // Â±5%

    // â”€â”€ Cooldowns en ms â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private static final Map<String, Long> lastPlayed = new HashMap<>();
    private static final Map<String, Long> COOLDOWNS  = new HashMap<>();

    static {
        COOLDOWNS.put("hover",   120L);
        COOLDOWNS.put("click",   100L);
        COOLDOWNS.put("heavy",   150L);
        COOLDOWNS.put("back",    150L);
        COOLDOWNS.put("open",    300L);
        COOLDOWNS.put("close",   300L);
        COOLDOWNS.put("modal",   500L);
        COOLDOWNS.put("error",   200L);
        COOLDOWNS.put("tab",      80L);
        COOLDOWNS.put("scroll",   40L);
        COOLDOWNS.put("success", 300L);
        COOLDOWNS.put("notif",   500L);
        COOLDOWNS.put("create",  300L);
        COOLDOWNS.put("destroy", 300L);
        COOLDOWNS.put("respawn", 500L);
    }

    // â”€â”€ API publica â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public static void hover()         { play("hover",   ModSounds.UI_HOVER.get(),          VOL_HOVER,   pitchVar()); }
    public static void click()         { play("click",   ModSounds.UI_CLICK.get(),          VOL_CLICK,   pitchVar()); }
    public static void clickHeavy()    { play("heavy",   ModSounds.UI_CLICK_HEAVY.get(),    VOL_HEAVY,   pitchVar()); }
    public static void back()          { play("back",    ModSounds.UI_BACK.get(),            VOL_BACK,    pitchVar()); }
    public static void open()          { play("open",    ModSounds.UI_OPEN.get(),            VOL_OPEN,    pitchVar()); }
    public static void close()         { play("close",   ModSounds.UI_CLOSE.get(),           VOL_CLOSE,   pitchVar()); }
    public static void modal()         { play("modal",   ModSounds.UI_MODAL.get(),           VOL_MODAL,   PITCH_BASE); }
    public static void error()         { play("error",   ModSounds.UI_ERROR.get(),           VOL_ERROR,   pitchVar()); }
    public static void tab()           { play("tab",     ModSounds.UI_TAB.get(),             VOL_TAB,     pitchVar()); }
    public static void scroll()        { play("scroll",  ModSounds.UI_SCROLL.get(),          VOL_SCROLL,  pitchVar()); }
    public static void success()       { play("success", ModSounds.UI_SUCCESS.get(),         VOL_SUCCESS, PITCH_BASE); }
    public static void notification()  { play("notif",   ModSounds.UI_NOTIFICATION.get(),    VOL_NOTIF,   PITCH_BASE); }
    public static void clickCreate()   { play("create",  ModSounds.UI_CLICK_CREATE.get(),    VOL_CREATE,  PITCH_BASE); }
    public static void clickDestroy()  { play("destroy", ModSounds.UI_CLICK_DESTROY.get(),   VOL_DESTROY, PITCH_BASE); }
    public static void respawn()       { play("respawn", ModSounds.UI_RESPAWN.get(),         VOL_RESPAWN, PITCH_BASE); }

    // â”€â”€ Reproduccion directa sin cooldown (para testing en SoundLab) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public static void playRaw(SoundEvent sound, float volume, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        mc.getSoundManager().play(
            SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    // â”€â”€ Internos â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private static void play(String key, SoundEvent sound, float volume, float pitch) {
        long now = System.currentTimeMillis();
        long cd  = COOLDOWNS.getOrDefault(key, 100L);
        if (now - lastPlayed.getOrDefault(key, 0L) < cd) return;
        lastPlayed.put(key, now);

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getSoundManager() == null) return;
        mc.getSoundManager().play(
            SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    private static float pitchVar() {
        return PITCH_BASE + (float)(Math.random() * 2 - 1) * PITCH_VAR;
    }

    // ── Aliases retrocompatibles (API v3) ─────────────────────────────────
    public static void playHover()  { hover(); }
    public static void playClick()  { click(); }
    public static void playCustom(net.minecraft.sounds.SoundEvent s, float v, float p) { playRaw(s, v, p); }
    public static void setHoverSound(net.minecraft.sounds.SoundEvent s, float v, float p) { /* no-op en v4 */ }
    public static void setClickSound(net.minecraft.sounds.SoundEvent s, float v, float p) { /* no-op en v4 */ }
}