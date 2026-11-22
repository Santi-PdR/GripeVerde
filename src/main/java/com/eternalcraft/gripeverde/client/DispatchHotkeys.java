package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.GripeVerdeMod;
import com.eternalcraft.gripeverde.client.integration.CopyLCompatibility;
import com.eternalcraft.gripeverde.client.integration.DispatchLibrary;
import com.eternalcraft.gripeverde.client.screens.DiaryDispatchScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

/**
 * Atajos del parte de CEDA: la tecla de cada ranura envia su mensaje y la tecla
 * de apertura levanta la ficha, igual que hacia CopyL.
 *
 * v0.181.0. Se apaga solo mientras CopyL este instalado, porque los dos mods
 * leerian la misma tecla y el mensaje saldria dos veces; el dia que CopyL salga
 * de la carpeta de mods, esto queda a cargo. Se sondea el estado fisico del
 * teclado (no KeyMapping) para no tocar options.txt ni duplicar entradas en
 * Controles, y se guarda el estado anterior de cada tecla para que mantenerla
 * apretada no dispare una tanda de mensajes.
 */
@Mod.EventBusSubscriber(modid = GripeVerdeMod.MOD_ID, value = Dist.CLIENT)
public final class DispatchHotkeys {

    private static final int MAX_OUTGOING = 256;

    private static final boolean[] slotDown = new boolean[DispatchLibrary.SLOT_COUNT];
    private static final int[] observedSlotKeys = new int[DispatchLibrary.SLOT_COUNT];
    private static boolean openDown;
    private static int observedOpenKey = Integer.MIN_VALUE;

    static {
        Arrays.fill(observedSlotKeys, Integer.MIN_VALUE);
    }

    private DispatchHotkeys() {
    }

    /** CopyL instalado = el se queda con las teclas hasta que se lo saque de mods. */
    public static boolean suspendedByCopyL() {
        return CopyLCompatibility.isAvailable();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null) {
            reset();
            return;
        }

        // No sondear teclas fuera del juego ni durante una recarga superpuesta.
        // reset hace que al regresar una tecla ya mantenida sea el estado base,
        // no una pulsacion nueva: hay que soltarla y volverla a pulsar.
        if (suspendedByCopyL() || !minecraft.isWindowActive() || minecraft.getOverlay() != null) {
            reset();
            return;
        }

        pollOpen(minecraft);
        pollSlots(minecraft);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        reset();
    }

    private static void pollOpen(Minecraft minecraft) {
        int binding = DispatchLibrary.openKey();
        boolean down = isDown(minecraft, binding);
        if (observedOpenKey != binding) {
            observedOpenKey = binding;
            openDown = down;
            return;
        }
        if (down && !openDown && minecraft.screen == null) {
            minecraft.setScreen(new DiaryDispatchScreen(null));
        }
        openDown = down;
    }

    private static void pollSlots(Minecraft minecraft) {
        boolean canSend = minecraft.player != null
            && minecraft.player.connection != null
            && minecraft.screen == null;

        for (int slot = 0; slot < DispatchLibrary.SLOT_COUNT; slot++) {
            int binding = DispatchLibrary.key(slot);
            boolean down = binding != DispatchLibrary.UNBOUND && isDown(minecraft, binding);
            if (observedSlotKeys[slot] != binding) {
                observedSlotKeys[slot] = binding;
                slotDown[slot] = down;
                continue;
            }
            if (canSend && down && !slotDown[slot]) {
                send(minecraft, slot);
            }
            slotDown[slot] = down;
        }
    }

    /** Envia una ranura: "/comando" va como comando y el resto como chat. */
    public static boolean send(Minecraft minecraft, int slot) {
        if (minecraft == null || minecraft.player == null || minecraft.player.connection == null) {
            return false;
        }
        String message = DispatchLibrary.message(slot);
        if (message == null || message.isBlank()) return false;
        if (message.length() > MAX_OUTGOING) {
            message = message.substring(0, MAX_OUTGOING);
        }
        if (message.startsWith("/") && message.length() > 1) {
            minecraft.player.connection.sendCommand(message.substring(1));
        } else {
            minecraft.player.connection.sendChat(message);
        }
        return true;
    }

    private static boolean isDown(Minecraft minecraft, int binding) {
        if (binding == DispatchLibrary.UNBOUND) return false;
        long window = minecraft.getWindow().getWindow();
        if (DispatchLibrary.isKeyboard(binding)) {
            return InputConstants.isKeyDown(window, binding);
        }
        if (DispatchLibrary.isMouse(binding)) {
            return GLFW.glfwGetMouseButton(window, DispatchLibrary.mouseButton(binding)) == GLFW.GLFW_PRESS;
        }
        return false;
    }

    private static void reset() {
        openDown = false;
        observedOpenKey = Integer.MIN_VALUE;
        Arrays.fill(slotDown, false);
        Arrays.fill(observedSlotKeys, Integer.MIN_VALUE);
    }
}
