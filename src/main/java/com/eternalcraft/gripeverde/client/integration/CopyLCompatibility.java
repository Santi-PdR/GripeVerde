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
    // CopyL 3.9.1 moved its labels off the custom ttf provider to plain text.
    // Checking which helper is present tells us if a reported tofu/squared
    // glyph screen can still come from the older styled build.
    private static final String TEXT_CLASS =
            "com.santipdr.copyl.client.screen.DispatchText";

    private CopyLCompatibility() {
    }

    public static boolean isAvailable() {
        return ModList.get().isLoaded(MOD_ID);
    }

    /**
     * CopyL version as installed, or an empty string when it cannot be read.
     * Only the spec version is used, never any game or player data.
     */
    public static String version() {
        if (!isAvailable()) return "";
        try {
            return ModList.get().getModContainerById(MOD_ID)
                    .map(container -> container.getModInfo().getVersion().toString())
                    .orElse("");
        } catch (Throwable ignored) {
            return "";
        }
    }

    /**
     * True when the installed CopyL already renders its panels with plain
     * Minecraft text, which removes the custom-font tofu path. Used only to log
     * integration state; the screen itself is always CopyL's.
     */
    public static boolean usesPlainPanelText() {
        if (!isAvailable()) return false;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) loader = CopyLCompatibility.class.getClassLoader();
            Class<?> textType = Class.forName(TEXT_CLASS, false, loader);
            for (java.lang.reflect.Method method : textType.getDeclaredMethods()) {
                if (method.getName().equals("plain")) return true;
            }
            return false;
        } catch (ClassNotFoundException | LinkageError exception) {
            return false;
        }
    }

    /**
     * Leaves a single line in latest.log describing which CopyL text path is
     * installed. The dispatch screens belong to CopyL, so this is the evidence
     * needed before blaming the integration.
     */
    public static void logIntegrationState() {
        if (!isAvailable()) {
            LOGGER.info("CEDA Dispatch is not installed; its panel is skipped.");
            return;
        }
        String installed = version();
        if (usesPlainPanelText()) {
            LOGGER.info("CEDA Dispatch {} draws its panels with plain Minecraft text.", installed);
        } else {
            LOGGER.warn("CEDA Dispatch {} still styles panel text with a custom font; "
                    + "squared glyphs can come from that provider.", installed);
        }
    }

    public static void openSettings(Screen returnTo) {
        if (!isAvailable()) return;
        logIntegrationState();
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
