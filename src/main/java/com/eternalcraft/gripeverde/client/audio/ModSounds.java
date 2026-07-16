package com.eternalcraft.gripeverde.client.audio;

import com.eternalcraft.gripeverde.GripeVerdeMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registro de todos los SoundEvent custom de Gripe Verde.
 * Generado automaticamente — no editar manualmente.
 */
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, GripeVerdeMod.MOD_ID);

    // ── UI ───────────────────────────────────────────────────────────────────
    public static final RegistryObject<SoundEvent> UI_HOVER =
        reg("ui.hover");
    public static final RegistryObject<SoundEvent> UI_CLICK =
        reg("ui.click");
    public static final RegistryObject<SoundEvent> UI_CLICK_HEAVY =
        reg("ui.click_heavy");
    public static final RegistryObject<SoundEvent> UI_BACK =
        reg("ui.back");
    public static final RegistryObject<SoundEvent> UI_OPEN =
        reg("ui.open");
    public static final RegistryObject<SoundEvent> UI_CLOSE =
        reg("ui.close");
    public static final RegistryObject<SoundEvent> UI_MODAL =
        reg("ui.modal");
    public static final RegistryObject<SoundEvent> UI_ERROR =
        reg("ui.error");
    public static final RegistryObject<SoundEvent> UI_TAB =
        reg("ui.tab");
    public static final RegistryObject<SoundEvent> UI_SCROLL =
        reg("ui.scroll");
    public static final RegistryObject<SoundEvent> UI_SUCCESS =
        reg("ui.success");
    public static final RegistryObject<SoundEvent> UI_NOTIFICATION =
        reg("ui.notification");
    public static final RegistryObject<SoundEvent> UI_CLICK_CREATE =
        reg("ui.click_create");
    public static final RegistryObject<SoundEvent> UI_CLICK_DESTROY =
        reg("ui.click_destroy");
    public static final RegistryObject<SoundEvent> UI_RESPAWN =
        reg("ui.respawn");

    // ── AMBIENT BASE (loops) ─────────────────────────────────────────────────
    public static final RegistryObject<SoundEvent> MENU_WIND =
        reg("menu.wind");
    public static final RegistryObject<SoundEvent> MENU_RAIN =
        reg("menu.rain");
    public static final RegistryObject<SoundEvent> MENU_CITY =
        reg("menu.city");

    // ── AMBIENT EVENTS ───────────────────────────────────────────────────────
    public static final RegistryObject<SoundEvent> MENU_FOOTSTEPS =
        reg("menu.footsteps");
    public static final RegistryObject<SoundEvent> MENU_WOOD_CREAK =
        reg("menu.wood_creak");
    public static final RegistryObject<SoundEvent> MENU_DOOR =
        reg("menu.door");
    public static final RegistryObject<SoundEvent> MENU_AMBIENT_OCCASIONAL =
        reg("menu.ambient_occasional");

    // ── AMBIENT RARE ─────────────────────────────────────────────────────────
    public static final RegistryObject<SoundEvent> MENU_BELL =
        reg("menu.bell");
    public static final RegistryObject<SoundEvent> MENU_CROWS =
        reg("menu.crows");
    public static final RegistryObject<SoundEvent> MENU_CHAIN =
        reg("menu.chain");
    public static final RegistryObject<SoundEvent> MENU_THUNDER =
        reg("menu.thunder");

    // -- AMBIENT TICK -------------------------------------------------
    public static final RegistryObject<SoundEvent> MENU_TICK =
        reg("menu.tick");

    // ── Helper ───────────────────────────────────────────────────────────────
    private static RegistryObject<SoundEvent> reg(String path) {
        return SOUND_EVENTS.register(path,
            () -> SoundEvent.createVariableRangeEvent(
                new ResourceLocation(GripeVerdeMod.MOD_ID, path)));
    }
}