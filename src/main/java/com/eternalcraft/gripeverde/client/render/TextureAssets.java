package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Texturas declaradas del mod.
 *
 * BG_TOWN:       unica textura existente en disco.
 * ICON_BTN_MP:   referenciada en DiaryTitleScreen y DebugCanvasScreen; PNG pendiente (v0.6.0 Fase 3).
 * ICON_BTN_MODS: referenciada en DiaryTitleScreen; PNG pendiente (v0.6.0 Fase 3).
 *
 * Las demas constantes fueron eliminadas en Fase 0 por ser codigo muerto
 * (sin consumidor externo y sin PNG en disco).
 */
@OnlyIn(Dist.CLIENT)
public class TextureAssets {

    public static final ResourceLocation BG_TOWN =
        new ResourceLocation("gripeverde", "textures/gui/title/bg_town.png");

    public static final ResourceLocation ICON_BTN_MP =
        new ResourceLocation("gripeverde", "textures/gui/title/icon_mp.png");

    public static final ResourceLocation ICON_BTN_MODS =
        new ResourceLocation("gripeverde", "textures/gui/title/icon_mods.png");

    public static boolean exists(ResourceLocation location) {
        try {
            var manager = Minecraft.getInstance().getResourceManager();
            return manager.getResource(location).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasBgTown() {
        return exists(BG_TOWN);
    }
}