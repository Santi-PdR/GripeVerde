package com.eternalcraft.gripeverde.client.render;

import com.eternalcraft.gripeverde.client.widgets.DiaryButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

/**
 * v0.242.0: vestir de papel los botones de una pantalla que no es nuestra.
 *
 * El boton del juego se oculta y en su lugar se pone uno de la casa que
 * dispara su misma accion. Nada de duplicar la logica: si el alta del sustituto
 * fallara, el original se queda visible y la pantalla sigue funcionando.
 */
public final class PaperButtons {

    /**
     * v0.253.0: de quien es cada boton vestido.
     *
     * El sustituto dispara la accion del original, asi que necesita saber si el
     * original esta habilitado: en la lista de mundos, "Jugar" esta apagado
     * hasta que hay una seleccion, y un vestido siempre encendido mandaba la
     * accion igual. Con este mapa se copia el estado en cada cuadro.
     */
    private static final java.util.Map<DiaryButton, Button> ORIGINALS = new java.util.WeakHashMap<>();

    private PaperButtons() {
    }

    /** v0.253.0: copia enabled/disabled del original a cada boton vestido. */
    public static void syncStates() {
        try {
            ORIGINALS.forEach((dressed, origin) -> {
                try {
                    dressed.active = origin.active;
                } catch (Throwable ignored) {
                }
            });
        } catch (Throwable ignored) {
        }
    }

    /** Viste todos los botones visibles de la pantalla. Devuelve los puestos. */
    public static List<DiaryButton> dressAll(Screen screen) {
        List<DiaryButton> dressed = new ArrayList<>();
        if (screen == null) return dressed;
        List<Button> originals = new ArrayList<>();
        try {
            for (var child : screen.children()) {
                if (child instanceof Button button && button.visible
                        && !(child instanceof DiaryButton)) {
                    originals.add(button);
                }
            }
        } catch (Throwable ignored) {
        }
        for (Button origin : originals) {
            try {
                DiaryButton dressedButton = new DiaryButton(
                    origin.getX(), origin.getY(), origin.getWidth(), origin.getHeight(),
                    origin.getMessage(), DiaryButton.Icon.NONE, DiaryButton.Variant.SECONDARY,
                    () -> {
                        // v0.253.0: nada de disparar una accion apagada.
                        if (origin.active) origin.onPress();
                    });
                if (register(screen, dressedButton)) {
                    origin.visible = false;
                    dressedButton.active = origin.active;
                    ORIGINALS.put(dressedButton, origin);
                    dressed.add(dressedButton);
                }
            } catch (Throwable ignored) {
            }
        }
        return dressed;
    }

    /** Oculta los widgets que no son botones (cajas de texto vanidas, etc.). */
    public static void hideNonButtons(Screen screen) {
        if (screen == null) return;
        try {
            for (AbstractWidget widget : collectNonButtonWidgets(screen)) {
                widget.visible = false;
                widget.active = false;
            }
        } catch (Throwable ignored) {
        }
    }

    private static List<AbstractWidget> collectNonButtonWidgets(Screen screen) {
        List<AbstractWidget> out = new ArrayList<>();
        try {
            for (var child : screen.children()) {
                if (child instanceof AbstractWidget widget && !(child instanceof Button)) {
                    out.add(widget);
                }
            }
        } catch (Throwable ignored) {
        }
        return out;
    }

    /** Alta del widget sin pasar por Screen#addRenderableWidget (protegido). */
    public static boolean register(Screen screen, AbstractWidget widget) {
        boolean ok = false;
        try {
            List<Object> renderables = listField(screen, net.minecraft.client.gui.components.Renderable.class);
            if (renderables != null) {
                renderables.add(widget);
                ok = true;
            }
        } catch (Throwable ignored) {
        }
        if (!ok) return false;
        try {
            @SuppressWarnings("unchecked")
            List<Object> children = (List<Object>) (List<?>) screen.children();
            children.add(widget);
        } catch (Throwable ignored) {
        }
        try {
            List<Object> narratables = listField(screen,
                net.minecraft.client.gui.narration.NarratableEntry.class);
            if (narratables != null) narratables.add(widget);
        } catch (Throwable ignored) {
        }
        return true;
    }

    private static List<Object> listField(Object owner, Class<?> elementType) {
        for (java.lang.reflect.Field field : Screen.class.getDeclaredFields()) {
            if (!List.class.isAssignableFrom(field.getType())) continue;
            if (!(field.getGenericType() instanceof java.lang.reflect.ParameterizedType pt)) continue;
            if (pt.getActualTypeArguments().length != 1) continue;
            if (pt.getActualTypeArguments()[0] != elementType) continue;
            try {
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) field.get(owner);
                return list;
            } catch (Throwable ignored) {
                return null;
            }
        }
        return null;
    }
}
