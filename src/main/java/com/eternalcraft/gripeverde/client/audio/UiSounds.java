package com.eternalcraft.gripeverde.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * UI sound facade for Gripe Verde.
 *
 * Keeps all UI one-shots behind cooldowns and small pitch variation so hover,
 * click, scroll and confirmation sounds feel tactile without becoming noisy.
 */
public class UiSounds {

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

    private static final float PITCH_BASE  = 1.0f;
    private static final float PITCH_VAR   = 0.05f;

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
        COOLDOWNS.put("clean",    70L);
        COOLDOWNS.put("button_main", 90L);
        COOLDOWNS.put("button_section", 110L);
        COOLDOWNS.put("option",  120L);
        COOLDOWNS.put("page",    100L);
    }

    public static void hover()         { /* hover button sounds disabled */ }
    public static void click()         { buttonMain(); }
    public static void clickHeavy()    { buttonSection(); }
    public static void back()          { safePlay("back", ModSounds.UI_BACK, 0.16f, 0.98f + (float)(Math.random() * 0.03f)); }
    public static void open()          { buttonSection(); }
    public static void close()         { back(); }
    public static void modal()         { buttonSection(); }
    public static void error()         { buttonSection(); }
    public static void tab()           { buttonMain(); }
    public static void scroll()        { safePlay("scroll",  ModSounds.UI_SCROLL,         VOL_SCROLL,  pitchVar()); }
    public static void success()       { buttonSection(); }
    public static void notification()  { buttonSection(); }
    public static void clickCreate()   { buttonMain(); }
    public static void clickDestroy()  { back(); }
    public static void respawn()       { buttonSection(); }

    public static void cleanClick()    { buttonMain(); }

    public static void buttonMain()    { safePlay("button_main", ModSounds.UI_BUTTON_MAIN, 0.18f, 0.98f + (float)(Math.random() * 0.04f)); }
    public static void buttonSection() { safePlay("button_section", ModSounds.UI_BUTTON_SECTION, 0.16f, 1.00f); }

    public static void optionOpen()    { buttonSection(); }

    public static void playRaw(SoundEvent sound, float volume, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    private static void play(String key, SoundEvent sound, float volume, float pitch) {
        long now = System.currentTimeMillis();
        long cd  = COOLDOWNS.getOrDefault(key, 100L);
        if (now - lastPlayed.getOrDefault(key, 0L) < cd) return;
        lastPlayed.put(key, now);

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getSoundManager() == null) return;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    /** Defensive wrapper: tolerates optional/late registry objects. */
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

    public static void playHover()  { hover(); }
    public static void playClick()  { click(); }
    public static void playCustom(SoundEvent s, float v, float p) { playRaw(s, v, p); }
    public static void setHoverSound(SoundEvent s, float v, float p) { /* kept for compatibility */ }
    public static void setClickSound(SoundEvent s, float v, float p) { /* kept for compatibility */ }
}
