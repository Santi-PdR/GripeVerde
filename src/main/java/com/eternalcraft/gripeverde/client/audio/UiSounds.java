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
    // Audible at default game settings while remaining below notification/music volume.
    private static final float VOL_HOVER_MAIN    = 0.055f;
    private static final float VOL_HOVER_SECTION = 0.045f;
    private static final float VOL_CLICK_MAIN    = 0.36f;
    private static final float VOL_CLICK_SECTION = 0.30f;
    private static final float VOL_BACK          = 0.28f;
    private static final float VOL_SCROLL        = 0.14f;
    private static final float VOL_NOTIF         = 0.30f;

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
        // v0.26.0: llaves propias para los legacy recableados a sus eventos
        COOLDOWNS.put("clean", 90L);
        COOLDOWNS.put("click_create", 150L);
        COOLDOWNS.put("respawn", 300L);
        COOLDOWNS.put("error", 400L);
        // v0.29.0: la llave "knock" se retiro junto con silenceKnock.
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

    // ── Legacy compat ──
    // v0.26.0 (V2 de REVISION_MOD, resto): cada ayudante suena con el evento
    // que se grabo para el. Estaban registrados y con pieza en disco desde el
    // principio; solo faltaba el cable. Los alias puros (click, heavy,
    // success, destroy) se quedan: sus eventos ya suenan por otros caminos
    // (UI_CLICK_HEAVY y UI_CLICK_DESTROY se tocan directo desde las pantallas,
    // UI_SUCCESS es el diapason de la sala de sonido).
    public static void click() { buttonMain(); }
    public static void clickHeavy() { buttonSection(); }
    public static void open() {
        safePlay("open", ModSounds.UI_OPEN, VOL_CLICK_SECTION, 0.97f + rand(0.05f));
    }
    public static void close() {
        safePlay("close", ModSounds.UI_CLOSE, VOL_BACK, 0.96f + rand(0.06f));
    }
    public static void modal() {
        safePlay("modal", ModSounds.UI_MODAL, VOL_CLICK_SECTION, 0.96f + rand(0.04f));
    }
    public static void error() {
        safePlay("error", ModSounds.UI_ERROR, 0.16f, 0.88f + rand(0.06f));
    }
    public static void tab() {
        safePlay("tab", ModSounds.UI_TAB, VOL_CLICK_MAIN, 0.99f + rand(0.04f));
    }
    public static void success() { buttonSection(); }
    public static void clickCreate() {
        safePlay("click_create", ModSounds.UI_CLICK_CREATE, VOL_CLICK_MAIN, 1.00f + rand(0.04f));
    }
    public static void clickDestroy() { back(); }
    public static void respawn() {
        safePlay("respawn", ModSounds.UI_RESPAWN, VOL_BACK, 0.92f + rand(0.06f));
    }
    public static void cleanClick() {
        safePlay("clean", ModSounds.UI_BUTTON_CLEAN, VOL_CLICK_MAIN, 1.00f + rand(0.03f));
    }
    public static void optionOpen() {
        // Las opciones que abren otra mesa son "open" un pelo mas agudo.
        safePlay("open", ModSounds.UI_OPEN, VOL_CLICK_SECTION, 1.02f + rand(0.04f));
    }

    // v0.29.0: silenceKnock retirado (pedido expreso del owner: nada de
    // sonido al tocar en cualquier lado). UI_ERROR sigue cableado en error().

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
        // v0.23.0: reloj monotonico para los cooldowns (nanoTime, no se ajusta con el SO).
        long now = System.nanoTime() / 1_000_000L;
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
        return (float)(java.util.concurrent.ThreadLocalRandom.current().nextDouble() * range);
    }
}
