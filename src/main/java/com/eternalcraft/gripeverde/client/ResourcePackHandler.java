package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.GripeVerdeMod;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.fml.ModList;

/**
 * Registra el Resource Pack built-in Master Steel como pack OPCIONAL.
 * Usa FolderRepositorySource para escanear resourcepacks/gripeverde_theme.
 * Aparece en "Disponibles" (required=false) con Position.TOP.
 * Si está arriba de Default → Master Steel; si está abajo/desactivado → vanilla 100%.
 * No dibuja overlays.
 */
public class ResourcePackHandler {

    public static void register() {
        // Registrado manualmente desde GripeVerdeMod para compatibilidad con AddPackFindersEvent BUS nuevo
        net.minecraftforge.event.AddPackFindersEvent.BUS.addListener(ResourcePackHandler::onAddPackFinders);
    }

    private static void onAddPackFinders(net.minecraftforge.event.AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;
        try {
            var modFile = ModList.get().getModFileById(GripeVerdeMod.MOD_ID).getFile();
            var packFolder = modFile.findResource("resourcepacks");
            if (packFolder == null || !java.nio.file.Files.exists(packFolder)) return;
            // FolderRepositorySource escaneará subcarpetas (gripeverde_theme) y creará Pack automáticamente vía pack.mcmeta
            var source = new FolderRepositorySource(packFolder, event.getPackType(), PackSource.BUILT_IN);
            event.addRepositorySource(source);
        } catch (Throwable t) {
            System.err.println("[GripeVerde] Failed to register Master Steel pack: " + t);
        }
    }
}
