package com.eternalcraft.gripeverde.client.render;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Texturas declaradas del mod.
 *
 * ICON_BTN_MP:   identificador lógico para GreenFluButton (multiplayer). El
 *                ResourceLocation NO apunta a un PNG real: GreenFluButton lee
 *                el path solo como discriminador (ver drawButtonIcon).
 * ICON_BTN_MODS: idem para el boton de mods (path contiene "mods").
 *
 * Nota v0.21.1: BG_TOWN (2.1M) fue eliminado por no tener uso real. El Title usa render
 * procedural (QuarantineSceneRenderer), no textura estática.
 * Nota v0.21.9: exists() eliminado (no tenia llamadas).
 */
@OnlyIn(Dist.CLIENT)
public class TextureAssets {

    public static final ResourceLocation ICON_BTN_MP =
        new ResourceLocation("gripeverde", "textures/gui/title/icon_mp.png");

    public static final ResourceLocation ICON_BTN_MODS =
        new ResourceLocation("gripeverde", "textures/gui/title/icon_mods.png");
}
