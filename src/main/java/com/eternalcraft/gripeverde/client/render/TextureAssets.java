package com.eternalcraft.gripeverde.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Texturas declaradas del mod.
 *
 * ICON_BTN_MP:   identificador lógico para GreenFluButton (multiplayer), dibujado proceduralmente.
 * ICON_BTN_MODS: identificador lógico para GreenFluButton (mods), dibujado proceduralmente.
 *
 * Nota v0.21.1: BG_TOWN (2.1M) fue eliminado por no tener uso real. El Title usa render
 * procedural (QuarantineSceneRenderer), no textura estática.
 */
@OnlyIn(Dist.CLIENT)
public class TextureAssets {

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
}
