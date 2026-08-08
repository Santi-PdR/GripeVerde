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
 * Registra el resource pack built-in "gripeverde_steel" (Acero de
 * Cuarentena): identidad visual industrial para todos los contenedores e
 * interfaces vanilla del juego.
 *
 * El pack es REQUIRED (required=true): forma parte de la identidad del mod
 * y se activa siempre, sin pasos extra. Es puramente visual: no mueve
 * slots, no cambia coordenadas, no toca hitboxes — solo reemplaza texturas
 * 1:1 (ver tools/steel_pack/ para el pipeline de generacion y validacion).
 */
@Mod.EventBusSubscriber(modid = GripeVerdeMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ResourcePackHandler {

    public static final String PACK_ID = "gripeverde_steel";
    private static final String PACK_NAME = "Gripe Verde — Acero de Cuarentena";

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        try {
            var modFile = ModList.get().getModFileById(GripeVerdeMod.MOD_ID).getFile();
            Path packPath = modFile.findResource("resourcepacks/" + PACK_ID);

            var pack = Pack.readMetaAndCreate(
                PACK_ID,
                Component.literal(PACK_NAME),
                true, // required=true => siempre activo, identidad del mod
                (id) -> new PathPackResources(id, true, packPath),
                PackType.CLIENT_RESOURCES,
                Pack.Position.TOP,
                PackSource.BUILT_IN
            );

            event.addRepositorySource((consumer) -> consumer.accept(pack));
        } catch (Throwable t) {
            System.err.println("[GripeVerde] No se pudo registrar el pack " + PACK_ID + ": " + t);
        }
    }
}
