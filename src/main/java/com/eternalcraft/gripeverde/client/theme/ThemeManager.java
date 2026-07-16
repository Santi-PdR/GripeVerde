package com.eternalcraft.gripeverde.client.theme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

/**
 * ThemeManager — registry de temas visuales aplicables a pantallas.
 *
 * En v0.6.0 no hay themes registrados. Las pantallas custom del mod
 * vanilla via ScreenEventHandler, no via overlay.
 *
 * Este sistema se conserva para uso futuro (HUDs, notificaciones, etc.).
 */
public class ThemeManager {

    private static final List<ScreenTheme> THEMES = new ArrayList<>();

    // No hay init() con registros. THEMES queda vacia intencionalmente.

    public static ScreenTheme findTheme(Screen screen) {
        if (screen == null) return null;
        for (ScreenTheme t : THEMES) {
            if (t.appliesTo(screen)) return t;
        }
        return null;
    }

    public static void render(GuiGraphics g, Screen screen, int w, int h, float partialTick) {
        ScreenTheme theme = findTheme(screen);
        if (theme != null) {
            theme.render(g, screen, w, h, partialTick);
        }
    }

    public static List<String> listRegistered() {
        List<String> names = new ArrayList<>();
        for (ScreenTheme t : THEMES) names.add(t.getName());
        return names;
    }
}
