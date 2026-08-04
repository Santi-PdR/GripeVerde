package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.GripeVerdeMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.resource.PathPackResources;

import java.nio.file.Path;

/**
 * Registra el resource pack built-in "gripeverde_theme" que reemplaza
 * texturas vanilla de interfaces y containers.
 *
 * Activado por defecto pero desactivable desde Options > Resource Packs.
 */
@Mod.EventBusSubscriber(modid = GripeVerdeMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ResourcePackHandler {

    private static final String PACK_ID = "gripeverde_theme";
    private static final String PACK_NAME = "Gripe Verde - Tema Victoriano";

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        var modFile = ModList.get().getModFileById(GripeVerdeMod.MOD_ID).getFile();
        Path packPath = modFile.findResource("resourcepacks/" + PACK_ID);

        var pack = Pack.readMetaAndCreate(
            PACK_ID,
            Component.literal(PACK_NAME),
            true, // required=true => activado por defecto
            (id) -> new PathPackResources(id, true, packPath),
            PackType.CLIENT_RESOURCES,
            Pack.Position.TOP,
            PackSource.BUILT_IN
        );

        event.addRepositorySource((consumer) -> consumer.accept(pack));
    }
}