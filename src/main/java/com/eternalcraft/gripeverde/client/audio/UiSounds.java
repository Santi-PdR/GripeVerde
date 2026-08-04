package com.eternalcraft.gripeverde.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * UiSounds v2.0 - Professional acoustic identity
 * Each button family has distinct material:
 *  - MAIN (primary actions): brass latch, metal closure
 *  - SECTION (open sub-screen): wooden drawer, paper modal
 *  - BACK (return): soft parchment close
 *  - HOVER: paper rustle, very subtle
 *  - SCROLL: paper slide
 * Volumes tuned for victorian gabinete: not aggressive, tactile.
 */
public class UiSounds {

    // Tuned volumes (base before user settings)
    private static final float VOL_HOVER_MAIN    = 0.045f;
    private static final float VOL_HOVER_SECTION = 0.035f;
    private static final float VOL_CLICK_MAIN    = 0.22f;
    private static final float VOL_CLICK_SECTION = 0.18f;
    private static final float VOL_BACK          = 0.20f;
    private static final float VOL_SCROLL        = 0.08f;
    private static final float VOL_NOTIF         = 0.20f;

    private static final Map<String, Long> lastPlayed = new HashMap<>();
    private static final Map<String, Long> COOLDOWNS  = new HashMap<>();

    static {
        COOLDOWNS.put("hover_main", 180L);
        COOLDOWNS.put("hover_section", 220L);
        COOLDOWNS.put("button_main", 90L);
        COOLDOWNS.put("button_section", 120L);
        COOLDOWNS.put("back", 140L);
        COOLDOWNS.put("scroll", 60L);
        COOLDOWNS.put("notif", 400L);
        // Legacy keys for compat
        COOLDOWNS.put("hover", 180L);
        COOLDOWNS.put("click", 100L);
        COOLDOWNS.put("heavy", 150L);
        COOLDOWNS.put("open", 300L);
        COOLDOWNS.put("close", 300L);
        COOLDOWNS.put("modal", 500L);
        COOLDOWNS.put("tab", 80L);
        COOLDOWNS.put("success", 300L);
    }

    // ── Primary API ──
    public static void buttonMain() {
        safePlay("button_main", ModSounds.UI_BUTTON_MAIN, VOL_CLICK_MAIN, 0.98f + rand(0.04f));
    }

    public static void buttonSection() {
        safePlay("button_section", ModSounds.UI_BUTTON_SECTION, VOL_CLICK_SECTION, 0.99f + rand(0.03f));
    }

    public static void back() {
        safePlay("back", ModSounds.UI_BACK, VOL_BACK, 0.97f + rand(0.05f));
    }

    public static void toggle() {
        // Toggle feels like small brass switch
        safePlay("button_section", ModSounds.UI_BUTTON_SECTION, VOL_CLICK_SECTION*0.92f, 1.08f + rand(0.06f));
    }

    public static void type() {
        // Typing is paper tap
        safePlay("scroll", ModSounds.UI_SCROLL, 0.055f, 0.92f + rand(0.12f));
    }

    public static void hoverMain() {
        safePlay("hover_main", ModSounds.UI_HOVER, VOL_HOVER_MAIN, 0.95f + rand(0.10f));
    }

    public static void hoverSection() {
        safePlay("hover_section", ModSounds.UI_HOVER, VOL_HOVER_SECTION, 0.88f + rand(0.08f));
    }

    public static void scroll() {
        safePlay("scroll", ModSounds.UI_SCROLL, VOL_SCROLL, 0.96f + rand(0.08f));
    }

    public static void notification() {
        safePlay("notif", ModSounds.UI_NOTIFICATION, VOL_NOTIF, 0.98f + rand(0.04f));
    }

    // ── Legacy compat (mapped to new) ──
    public static void hover() { /* kept for compat, but specific hover via playHover */ }
    public static void click() { buttonMain(); }
    public static void clickHeavy() { buttonSection(); }
    public static void open() { buttonSection(); }
    public static void close() { back(); }
    public static void modal() { buttonSection(); }
    public static void error() { buttonSection(); }
    public static void tab() { buttonMain(); }
    public static void success() { buttonSection(); }
    public static void clickCreate() { buttonMain(); }
    public static void clickDestroy() { back(); }
    public static void respawn() { buttonSection(); }
    public static void cleanClick() { buttonMain(); }
    public static void optionOpen() { buttonSection(); }

    public static void playRaw(SoundEvent sound, float volume, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    public static void playCustom(SoundEvent s, float v, float p) { playRaw(s, v, p); }

    // ── Hover dispatcher used by buttons ──
    // DiaryButton = main, GreenFlu = main, other subtle = section
    public static void playHover() {
        // Default hover is main family (most buttons are DiaryButton)
        hoverMain();
    }

    public static void playClick() {
        buttonMain();
    }

    // ── Internal helpers ──
    private static void play(String key, SoundEvent sound, float volume, float pitch) {
        long now = System.currentTimeMillis();
        long cd = COOLDOWNS.getOrDefault(key, 100L);
        long last = lastPlayed.getOrDefault(key, 0L);
        if (now - last < cd) return;
        lastPlayed.put(key, now);
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getSoundManager() == null) return;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    private static void safePlay(String key, net.minecraftforge.registries.RegistryObject<SoundEvent> ro,
                                 float volume, float pitch) {
        if (ro == null) return;
        SoundEvent se;
        try { se = ro.get(); } catch (Exception ex) { return; }
        if (se == null) return;
        play(key, se, volume, pitch);
    }

    private static float rand(float range) {
        return (float)(Math.random() * range);
    }

    public static void setHoverSound(SoundEvent s, float v, float p) { }
    public static void setClickSound(SoundEvent s, float v, float p) { }
}
