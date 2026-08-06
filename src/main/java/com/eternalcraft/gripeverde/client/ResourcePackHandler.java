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
 * Registra el resource pack built-in "gripeverde_theme" que provee las texturas
 * de interfaces de acero victoriano para Minecraft 1.20.1.
 *
 * Configurado con:
 *  - required = false: completamente desactivable por el usuario.
 *  - Position.TOP: activado por defecto arriba, pero si el usuario lo mueve debajo
 *    del paquete vanilla (Default), vuelve de inmediato al aspecto vanilla 100%.
 *  - PackSource.BUILT_IN: aparece categorizado correctamente en la lista de paquetes.
 */
@Mod.EventBusSubscriber(modid = GripeVerdeMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ResourcePackHandler {

    private static final String PACK_ID = "gripeverde_theme";
    private static final String PACK_NAME = "Gripe Verde - Acero Victoriano";

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        var modFile = ModList.get().getModFileById(GripeVerdeMod.MOD_ID).getFile();
        Path packPath = modFile.findResource("resourcepacks/" + PACK_ID);

        var pack = Pack.readMetaAndCreate(
            PACK_ID,
            Component.literal(PACK_NAME),
            false,
            (id) -> new PathPackResources(id, true, packPath),
            PackType.CLIENT_RESOURCES,
            Pack.Position.TOP,
            PackSource.BUILT_IN
        );

        event.addRepositorySource((consumer) -> consumer.accept(pack));
    }
}
