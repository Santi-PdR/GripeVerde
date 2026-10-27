package com.eternalcraft.gripeverde.client.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;

/** Optional bridge to CEDA Dispatch (CopyL), with no compile-time dependency. */
public final class CopyLCompatibility {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String MOD_ID = "copyl";
    private static final String SETTINGS_CLASS =
            "com.santipdr.copyl.client.screen.CopyLSettingsScreen";

    private CopyLCompatibility() {
    }

    public static boolean isAvailable() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static void openSettings(Screen returnTo) {
        if (!isAvailable()) return;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) loader = CopyLCompatibility.class.getClassLoader();
            Class<?> settingsType = Class.forName(SETTINGS_CLASS, true, loader);
            Constructor<?> constructor = settingsType.getConstructor(Screen.class);
            Object settings = constructor.newInstance(returnTo);
            if (!(settings instanceof Screen screen)) {
                throw new ReflectiveOperationException("CopyL settings type is not a Minecraft screen");
            }
            Minecraft.getInstance().setScreen(screen);
        } catch (ReflectiveOperationException | LinkageError exception) {
            LOGGER.warn("Could not open the optional CEDA Dispatch settings screen.", exception);
        }
    }
}
