package com.eternalcraft.gripeverde.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;

/**
 * UiSounds v4 Ã¢â‚¬â€ Sistema de audio custom para Gripe Verde.
 *
 * API publica:
 *   UiSounds.hover()           Ã¢â‚¬â€ roce de cuero/tela
 *   UiSounds.click()           Ã¢â‚¬â€ pestillo metalico
 *   UiSounds.clickHeavy()      Ã¢â‚¬â€ cerrojo de hierro
 *   UiSounds.back()            Ã¢â‚¬â€ papel cerrÃƒÂ¡ndose
 *   UiSounds.open()            Ã¢â‚¬â€ bisagra de madera
 *   UiSounds.close()           Ã¢â‚¬â€ cajon cerrÃƒÂ¡ndose
 *   UiSounds.modal()           Ã¢â‚¬â€ vidrio de boticario
 *   UiSounds.error()           Ã¢â‚¬â€ metal que no encaja
 *   UiSounds.tab()             Ã¢â‚¬â€ pagina de libro
 *   UiSounds.scroll()          Ã¢â‚¬â€ micro-tick
 *   UiSounds.success()         Ã¢â‚¬â€ cerrojo satisfactorio
 *   UiSounds.notification()    Ã¢â‚¬â€ nudillo en puerta
 *   UiSounds.clickCreate()     Ã¢â‚¬â€ fosforo encendiendose
 *   UiSounds.clickDestroy()    Ã¢â‚¬â€ cadena cayendo
 *   UiSounds.respawn()         Ã¢â‚¬â€ inhalacion suave
 */
public class UiSounds {

    // Ã¢â€â‚¬Ã¢â€â‚¬ Volumenes por categoria Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    private static final float VOL_HOVER   = 0.06f;
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

    // Ã¢â€â‚¬Ã¢â€â‚¬ Pitch base (variacion aleatoria aplicada en play) Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    private static final float PITCH_BASE  = 1.0f;
    private static final float PITCH_VAR   = 0.05f; // Ã‚Â±5%

    // Ã¢â€â‚¬Ã¢â€â‚¬ Cooldowns en ms Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
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

    // Ã¢â€â‚¬Ã¢â€â‚¬ API publica Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    public static void hover()         { safePlay("hover",   ModSounds.UI_HOVER,          VOL_HOVER,   pitchVar()); }
    public static void click()         { safePlay("click",   ModSounds.UI_CLICK,          VOL_CLICK,   pitchVar()); }
    public static void clickHeavy()    { safePlay("heavy",   ModSounds.UI_CLICK_HEAVY,    VOL_HEAVY,   pitchVar()); }
    public static void back()          { safePlay("back",    ModSounds.UI_BACK,           VOL_BACK,    pitchVar()); }
    public static void open()          { safePlay("open",    ModSounds.UI_OPEN,           VOL_OPEN,    pitchVar()); }
    public static void close()         { safePlay("close",   ModSounds.UI_CLOSE,          VOL_CLOSE,   pitchVar()); }
    public static void modal()         { safePlay("modal",   ModSounds.UI_MODAL,          VOL_MODAL,   PITCH_BASE); }
    public static void error()         { safePlay("error",   ModSounds.UI_ERROR,          VOL_ERROR,   pitchVar()); }
    public static void tab()           { safePlay("tab",     ModSounds.UI_TAB,            VOL_TAB,     pitchVar()); }
    public static void scroll()        { safePlay("scroll",  ModSounds.UI_SCROLL,         VOL_SCROLL,  pitchVar()); }
    public static void success()       { safePlay("success", ModSounds.UI_SUCCESS,       VOL_SUCCESS, PITCH_BASE); }
    public static void notification()  { safePlay("notif",   ModSounds.UI_NOTIFICATION,  VOL_NOTIF,   PITCH_BASE); }
    public static void clickCreate()   { safePlay("create",  ModSounds.UI_CLICK_CREATE,  VOL_CREATE,  PITCH_BASE); }
    public static void clickDestroy()  { safePlay("destroy", ModSounds.UI_CLICK_DESTROY, VOL_DESTROY, PITCH_BASE); }
    public static void respawn()       { safePlay("respawn", ModSounds.UI_RESPAWN,       VOL_RESPAWN, PITCH_BASE); }

    // Ã¢â€â‚¬Ã¢â€â‚¬ Reproduccion directa sin cooldown (para testing en SoundLab) Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    public static void playRaw(SoundEvent sound, float volume, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        mc.getSoundManager().play(
            SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ Internos Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
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

    /**
     * Version defensiva que tolera RegistryObject vacio.
     * Necesario por incompatibilidad con ModernFix RegistryObjectMixin.
     */
    private static void safePlay(String key, net.minecraftforge.registries.RegistryObject<SoundEvent> ro,
                                 float volume, float pitch) {
        if (ro == null) return;
        SoundEvent se;
        try {
            se = ro.get();
        } catch (NullPointerException | IllegalStateException ex) {
            return;
        }
        if (se == null) return;
        play(key, se, volume, pitch);
    }

    private static float pitchVar() {
        return PITCH_BASE + (float)(Math.random() * 2 - 1) * PITCH_VAR;
    }

    // â”€â”€ Aliases retrocompatibles (API v3) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public static void playHover()  { hover(); }
    public static void playClick()  { click(); }
    public static void playCustom(net.minecraft.sounds.SoundEvent s, float v, float p) { playRaw(s, v, p); }
    public static void setHoverSound(net.minecraft.sounds.SoundEvent s, float v, float p) { /* no-op en v4 */ }
    public static void setClickSound(net.minecraft.sounds.SoundEvent s, float v, float p) { /* no-op en v4 */ }
}