package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.GripeVerdeMod;
import com.eternalcraft.gripeverde.client.screens.ChangelogScreen;
import com.eternalcraft.gripeverde.client.screens.DebugCanvasScreen;
import com.eternalcraft.gripeverde.client.screens.DiaryPauseScreen;
import com.eternalcraft.gripeverde.client.screens.DiaryTitleScreen;
import com.eternalcraft.gripeverde.client.screens.SoundLabScreen;
import com.eternalcraft.gripeverde.client.widgets.MusicVolumeSlider;
import net.minecraft.client.gui.screens.OptionsScreen;
import com.eternalcraft.gripeverde.client.theme.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

/**
 * Escucha eventos de Screens vanilla:
 * - TitleScreen vanilla -> reemplaza por DiaryTitleScreen custom
 * - PauseScreen vanilla -> reemplaza por DiaryPauseScreen custom
 * - Ctrl+T -> abre DebugCanvasScreen
 * - Delega render de overlays al ThemeManager (sin themes registrados en v0.6.0)
 */
@Mod.EventBusSubscriber(modid = GripeVerdeMod.MOD_ID, value = Dist.CLIENT)
public class ScreenEventHandler {

    private static final Logger LOG = LogManager.getLogger("GripeVerde/ScreenEventHandler");

    private static boolean redirecting = false;

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (redirecting) return;

        var newScreen = event.getNewScreen();
        if (newScreen == null) return;

        // Intercept: TitleScreen vanilla -> DiaryTitleScreen custom
        if (newScreen instanceof TitleScreen && !(newScreen instanceof DiaryTitleScreen)) {
            redirecting = true;
            try {
                event.setNewScreen(new DiaryTitleScreen());
            } catch (Throwable t) {
                LOG.error("Failed to replace TitleScreen", t);
            } finally {
                redirecting = false;
            }
            return;
        }

        // Intercept: PauseScreen vanilla -> DiaryPauseScreen custom
        if (newScreen instanceof PauseScreen && !(newScreen instanceof DiaryPauseScreen)) {
            redirecting = true;
            try {
                event.setNewScreen(new DiaryPauseScreen());
            } catch (Throwable t) {
                LOG.error("Failed to replace PauseScreen", t);
            } finally {
                redirecting = false;
            }
            return;
        }
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        var screen = event.getScreen();
        boolean isTitle = (screen instanceof TitleScreen) || (screen instanceof DiaryTitleScreen);
        if (!isTitle) return;

        int key = event.getKeyCode();
        int mods = event.getModifiers();

        if ((mods & GLFW.GLFW_MOD_CONTROL) != 0 && key == GLFW.GLFW_KEY_T) {
            Minecraft.getInstance().setScreen(new DebugCanvasScreen(screen));
            event.setCanceled(true);
        }
    }

    /**
     * Inyecta el slider de volumen de musica en OptionsScreen.
     */
    @SubscribeEvent
    public static void onScreenInitPost(ScreenEvent.Init.Post event) {
        var screen = event.getScreen();
        if (!(screen instanceof OptionsScreen)) return;

        // Posicion: esquina superior derecha, debajo del titulo "Options"
        int sliderW = 150;
        int sliderH = 20;
        int sliderX = screen.width - sliderW - 10;
        int sliderY = 30;

        MusicVolumeSlider slider = new MusicVolumeSlider(sliderX, sliderY, sliderW, sliderH);
        event.addListener(slider);
    }

    /**
     * Delega el overlay de todas las pantallas al ThemeManager.
     * Excluye las pantallas propias del mod (ya tienen su render).
     */
    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        var screen = event.getScreen();

        if (screen instanceof DiaryTitleScreen
                || screen instanceof DiaryPauseScreen
                || screen instanceof ChangelogScreen
                || screen instanceof DebugCanvasScreen
                || screen instanceof SoundLabScreen) {
            return;
        }

        ThemeManager.render(event.getGuiGraphics(), screen,
                screen.width, screen.height, event.getPartialTick());
    }
}