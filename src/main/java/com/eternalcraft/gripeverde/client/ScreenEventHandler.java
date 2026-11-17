package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.GripeVerdeMod;
import com.eternalcraft.gripeverde.client.screens.ChangelogScreen;
import com.eternalcraft.gripeverde.client.screens.DiaryPauseScreen;
import com.eternalcraft.gripeverde.client.screens.DiaryTitleScreen;
import com.eternalcraft.gripeverde.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

/**
 * Escucha eventos de Screens vanilla:
 * - TitleScreen vanilla -> reemplaza por DiaryTitleScreen custom
 * - PauseScreen vanilla -> reemplaza por DiaryPauseScreen custom
 *
 * Nota v0.21.1: se eliminó ThemeManager y el framework de overlays inerte (sin themes registrados desde v0.6.0).
 * El layout útil (Anchor, LayoutRegion, Spacing) se conserva porque DiaryTitleScreen lo usa activamente.
 */
@Mod.EventBusSubscriber(modid = GripeVerdeMod.MOD_ID, value = Dist.CLIENT)
public class ScreenEventHandler {

    private static final Logger LOG = LogManager.getLogger("GripeVerde/ScreenEventHandler");

    private static boolean redirecting = false;
    private static boolean ctrlZeroLatch = false;
    // v0.26.0: la vigilia "vuelve a sentarte" solo al regresar, no en la
    // primera apertura del arranque (ver onArriveTitle).
    private static boolean titleSeenOnce = false;
    // Prevent repeating menu-audio shutdown on every in-world client tick.
    private static boolean worldAudioStopHandled = false;

    // v0.23.0: accesos tolerantes a config no cargada (defaults seguros)
    private static boolean replaceTitleEnabled() {
        try { return ClientConfig.REPLACE_TITLE.get(); } catch (Throwable t) { return true; }
    }
    private static boolean replacePauseEnabled() {
        try { return ClientConfig.REPLACE_PAUSE.get(); } catch (Throwable t) { return true; }
    }
    private static boolean debugShortcutEnabled() {
        try { return ClientConfig.DEBUG_SHORTCUT.get(); } catch (Throwable t) { return false; }
    }

    // v0.26.0: al volver a la vigilia desde otra mesa (o caer en ella por la
    // via vanilla), el gabinete "vuelve a sentarte". La primera apertura del
    // arranque no suena: para eso ya esta toda la obertura del menu.
    private static void onArriveTitle() {
        if (titleSeenOnce) {
            com.eternalcraft.gripeverde.client.audio.UiSounds.respawn();
        }
        titleSeenOnce = true;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        // Detener el audio del gabinete solo en el borde de entrada al mundo.
        // El cliente emite ticks durante toda la partida; repetir dos stop()
        // protegidos en cada tick no aporta nada una vez apagados los sonidos.
        if (mc.level != null) {
            if (!worldAudioStopHandled) {
                worldAudioStopHandled = true;
                try { com.eternalcraft.gripeverde.client.audio.MenuMusic.stop(); }
                catch (Throwable ignored) {}
                try { com.eternalcraft.gripeverde.client.audio.MenuAmbience.stop(); }
                catch (Throwable ignored) {}
            }
        } else if (worldAudioStopHandled) {
            worldAudioStopHandled = false;
        }

        // v0.23.0: el atajo de depuracion Ctrl+0 queda desactivado por defecto.
        if (!debugShortcutEnabled()) {
            ctrlZeroLatch = false;
            return;
        }
        if (mc.level == null || mc.getWindow() == null) {
            ctrlZeroLatch = false;
            return;
        }
        long window = mc.getWindow().getWindow();
        boolean ctrlDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
            || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
        boolean zeroDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_0) == GLFW.GLFW_PRESS;
        if (ctrlDown && zeroDown) {
            if (!ctrlZeroLatch) {
                ctrlZeroLatch = true;
                leaveWorldSilently(mc);
            }
        } else {
            ctrlZeroLatch = false;
        }
    }

    private static void leaveWorldSilently(Minecraft mc) {
        try {
            if (mc.level == null) return;
            boolean isLocal = mc.isLocalServer();
            mc.level.disconnect();
            if (isLocal) {
                mc.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
            } else {
                mc.clearLevel();
            }
            mc.setScreen(new TitleScreen());
        } catch (Throwable t) {
            LOG.error("Failed to leave world via admin shortcut", t);
        }
    }

    // v0.26.0: el pasar de las hojas entre pantallas del gabinete.
    @SubscribeEvent
    public static void onScreenInitPost(ScreenEvent.Init.Post event) {
        // v0.240.0: los botones de la pantalla de muerte se visten de papel.
        try {
            com.eternalcraft.gripeverde.client.render.DeathPaper.onInit(event.getScreen());
        } catch (Throwable ignored) {}
    }

    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        Minecraft mc = Minecraft.getInstance();
        // v0.237.0: el parte de defuncion se pega tambien en partida, que es
        // donde aparece la pantalla de muerte.
        try {
            com.eternalcraft.gripeverde.client.render.DeathPaper.render(
                event.getGuiGraphics(), event.getScreen());
        } catch (Throwable ignored) {}
        if (mc != null && mc.level != null) return;
        try {
            com.eternalcraft.gripeverde.client.render.DiaryTransitions.render(event.getScreen(), event.getGuiGraphics());
        } catch (Throwable ignored) {}
    }

    /**
     * v0.234.0: el inventario y los contenedores llevan el canto gastado.
     *
     * No se pueden repintar (su panel es una textura vanilla y el mod no usa
     * mixins), asi que se les marca el borde: mordiscos de tinta de un pixel
     * hacia adentro y sombra suelta hacia afuera, derivados de la geometria
     * del panel. Es estatico: no hay animacion nueva en partida.
     */
    @SubscribeEvent
    public static void onContainerRenderPost(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen)) {
            return;
        }
        try {
            com.eternalcraft.gripeverde.client.render.PaperTexture.drawWornRim(
                event.getGuiGraphics(), screen.getGuiLeft(), screen.getGuiTop(),
                screen.getXSize(), screen.getYSize());
        } catch (Throwable ignored) {}
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        // v0.27.0: la mesa que se va, para decidir el sentido del pliegue.
        final net.minecraft.client.gui.screens.Screen fromScreen = event.getCurrentScreen();
        // Feedback sonoro discreto por familia de pantalla.
        try {
            var openingScreen = event.getNewScreen();
            if (openingScreen instanceof com.eternalcraft.gripeverde.client.screens.DiaryOptionsScreen) {
                // v0.21.24: sin sonido de puerta al abrir el gabinete (pedido del usuario).
            } else if (openingScreen instanceof ChangelogScreen) {
                com.eternalcraft.gripeverde.client.audio.UiSounds.tab();
            } else if (openingScreen instanceof com.eternalcraft.gripeverde.client.screens.DiaryLanguageScreen) {
                com.eternalcraft.gripeverde.client.audio.UiSounds.tab();
            } else if (openingScreen instanceof com.eternalcraft.gripeverde.client.screens.DiaryMultiplayerScreen) {
                com.eternalcraft.gripeverde.client.audio.UiSounds.open();
            } else if (openingScreen instanceof com.eternalcraft.gripeverde.client.screens.DiarySoundOptionsScreen) {
                com.eternalcraft.gripeverde.client.audio.UiSounds.modal();
            } else if (openingScreen instanceof com.eternalcraft.gripeverde.client.screens.DiaryTitleScreen) {
                onArriveTitle();
            }
        } catch (Throwable ignored) {}
        if (redirecting) return;

        var newScreen = event.getNewScreen();
        if (newScreen == null) return;

        // Intercept: TitleScreen vanilla -> DiaryTitleScreen custom
        // v0.23.0: comparacion por CLASE EXACTA. Antes era instanceof, lo que
        // barria cualquier Title custom de otro mod (FancyMenu y derivadas son
        // subclases). Si otro mod ya personalizo el menu, Gripe Verde cede.
        // Ademas el reemplazo puede apagarse por config (integration.*).
        if (newScreen.getClass() == TitleScreen.class && replaceTitleEnabled()) {
            redirecting = true;
            try {
                event.setNewScreen(new DiaryTitleScreen());
                // El reemplazo no dispara otro Opening: la llegada a la
                // vigilia por esta via tambien cuenta (arranque o regreso).
                onArriveTitle();
            } catch (Throwable t) {
                LOG.error("Failed to replace TitleScreen", t);
            } finally {
                redirecting = false;
            }
            noteTransition(fromScreen, event);
            return;
        }

        // Intercept: PauseScreen vanilla -> DiaryPauseScreen custom
        if (newScreen.getClass() == PauseScreen.class && replacePauseEnabled()) {
            redirecting = true;
            try {
                event.setNewScreen(new DiaryPauseScreen());
            } catch (Throwable t) {
                LOG.error("Failed to replace PauseScreen", t);
            } finally {
                redirecting = false;
            }
            noteTransition(fromScreen, event);
            return;
        }

        noteTransition(fromScreen, event);
    }

    // v0.27.0: avisa al pliegue tras el redirect definitivo, con la mesa
    // vieja y la nueva ya resueltas. Ahi se decide si la hoja baja o sube.
    private static void noteTransition(net.minecraft.client.gui.screens.Screen from,
                                       ScreenEvent.Opening event) {
        try {
            com.eternalcraft.gripeverde.client.render.DiaryTransitions.noteOpening(
                from, event.getNewScreen());
        } catch (Throwable ignored) {}
    }
}
