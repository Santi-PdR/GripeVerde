package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.GripeVerdeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Capa visual ligera para containers vanilla.
 *
 * No dibuja texto encima del vanilla: antes de que la pantalla se renderice,
 * mueve los labels vanilla fuera del area visible; despues restaura sus
 * coordenadas y dibuja el mismo Component con sombra. Asi se respeta cualquier
 * idioma sin efecto de duplicado.
 */
@Mod.EventBusSubscriber(modid = GripeVerdeMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ContainerOverlayHandler {

    private static final int TEXT_LIGHT = 0xFFECE6D2;
    private static final int HIDDEN_POS = -10000;
    private static final int ENDER_PURPLE = 0xFF9B43D6;
    private static final int ENDER_GREEN = 0xFF56D6A0;
    private static final int ENDER_DIM = 0xAA3A1F5E;
    private static final int ENCHANT_TEXT = 0xFFC8B7F0;
    private static final int ENCHANT_SHADOW = 0xFF120B20;
    private static final int ENCHANT_LEVEL = 0xFF91FF42;

    private static final String[] TITLE_X_NAMES = { "titleLabelX", "f_97728_" };
    private static final String[] TITLE_Y_NAMES = { "titleLabelY", "f_97729_" };
    private static final String[] INVENTORY_X_NAMES = { "inventoryLabelX", "f_97730_" };
    private static final String[] INVENTORY_Y_NAMES = { "inventoryLabelY", "f_97731_" };
    private static final String[] IMAGE_HEIGHT_NAMES = { "imageHeight", "f_97727_" };

    private static final Map<AbstractContainerScreen<?>, LabelState> HIDDEN_LABELS = new IdentityHashMap<>();

    private ContainerOverlayHandler() {}

    private static boolean supportsScreen(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return false;
        String className = screen.getClass().getName();
        // Only vanilla inventory screens are safe to reflect/move labels on.
        // Modded backpacks and server custom containers often subclass
        // AbstractContainerScreen with different private fields/layouts.
        return className.startsWith("net.minecraft.client.gui.screens.inventory.");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        Screen screen = event.getScreen();
        if (!supportsScreen(screen)) return;
        if (!(screen instanceof AbstractContainerScreen<?> container)) return;

        LabelState state = LabelState.capture(container);
        if (!state.canHide()) return;

        HIDDEN_LABELS.put(container, state);
        state.hide(container);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        if (!supportsScreen(screen)) return;
        if (!(screen instanceof AbstractContainerScreen<?> container)) return;

        LabelState state = HIDDEN_LABELS.remove(container);
        if (state == null) return;
        state.restore(container);

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        GuiGraphics gfx = event.getGuiGraphics();

        int left = container.getGuiLeft();
        int top = container.getGuiTop();

        int titleX = state.titleX;
        int titleY = state.titleY;
        int inventoryX = state.inventoryX;
        int inventoryY = state.inventoryY;

        // InventoryScreen es un caso especial: vanilla ubica "Crafting" a la
        // derecha del modelo. Si la coordenada capturada viene en 0/8 por algun
        // mapeo raro, usamos la posicion real del layout vanilla.
        if (isInventoryScreen(screen)) {
            titleX = 97;
            titleY = 8;
            inventoryX = 8;
            inventoryY = 84;
        }

        if (isGrindstoneScreen(screen)) {
            drawScaledLabel(gfx, font, screen.getTitle(), left + titleX, top + titleY - 1, 0.95f);
        } else {
            drawLabel(gfx, font, screen.getTitle(), left + titleX, top + titleY);
        }

        if (isEnderChestTitle(screen.getTitle())) {
            drawEnderChestDetails(gfx, left, top, state.imageHeight);
        }

        if (isEnchantmentScreen(screen)) {
            drawEnchantmentTextWithShadow(gfx, font, container, left, top);
            drawEnchantingSparkles(gfx, left, top);
        }

    }

    private static boolean isEnderChestTitle(Component title) {
        return title.getContents() instanceof TranslatableContents translatable
            && "container.enderchest".equals(translatable.getKey());
    }

    private static boolean isInventoryScreen(Screen screen) {
        return "InventoryScreen".equals(screen.getClass().getSimpleName());
    }

    private static boolean isEnchantmentScreen(Screen screen) {
        return "EnchantmentScreen".equals(screen.getClass().getSimpleName());
    }

    private static boolean isGrindstoneScreen(Screen screen) {
        return "GrindstoneScreen".equals(screen.getClass().getSimpleName());
    }


    private static void drawLabel(GuiGraphics gfx, Font font, Component text, int x, int y) {
        gfx.drawString(font, text, x, y, TEXT_LIGHT, true);
    }

    private static void drawScaledLabel(GuiGraphics gfx, Font font, Component text, int x, int y, float scale) {
        gfx.pose().pushPose();
        gfx.pose().translate(x, y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(font, text, 0, 0, TEXT_LIGHT, true);
        gfx.pose().popPose();
    }


    private static Field findIntField(Class<?> type, String... names) {
        for (String name : names) {
            Class<?> c = type;
            while (c != null) {
                try {
                    Field f = c.getDeclaredField(name);
                    if (f.getType() == int.class) {
                        f.setAccessible(true);
                        return f;
                    }
                } catch (Throwable ignored) {
                    // Try superclass / next candidate.
                }
                c = c.getSuperclass();
            }
        }
        return null;
    }

    private static Field findInventoryTitleField(Class<?> type, Object target) {
        Class<?> c = type;
        while (c != null) {
            for (Field f : c.getDeclaredFields()) {
                if (!Component.class.isAssignableFrom(f.getType())) continue;
                try {
                    f.setAccessible(true);
                    Object value = f.get(target);
                    if (value instanceof Component component && isInventoryTitle(component)) {
                        return f;
                    }
                } catch (Throwable ignored) {
                    // Try next field.
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }

    private static boolean isInventoryTitle(Component component) {
        return component.getContents() instanceof TranslatableContents translatable
            && "container.inventory".equals(translatable.getKey());
    }

    private static int getInt(Field field, Object target, int fallback) {
        if (field == null) return fallback;
        try {
            return field.getInt(target);
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private static Component getComponent(Field field, Object target) {
        if (field == null) return Component.empty();
        try {
            Object value = field.get(target);
            return value instanceof Component component ? component : Component.empty();
        } catch (Throwable ignored) {
            return Component.empty();
        }
    }

    private static void setComponent(Field field, Object target, Component value) {
        if (field == null) return;
        try {
            field.set(target, value);
        } catch (Throwable ignored) {
            // If reflection fails, do not crash the client.
        }
    }

    private static void setInt(Field field, Object target, int value) {
        if (field == null) return;
        try {
            field.setInt(target, value);
        } catch (Throwable ignored) {
            // If reflection fails, do not crash the client.
        }
    }

    private static void drawEnchantmentTextWithShadow(
        GuiGraphics gfx,
        Font font,
        AbstractContainerScreen<?> container,
        int left,
        int top
    ) {
        try {
            AbstractContainerMenu menu = container.getMenu();
            int[] costs = readFirstIntArray(menu, 3);
            long seed = readBestSeed(menu);

            Class<?> namesClass = Class.forName("net.minecraft.client.gui.screens.inventory.EnchantmentNames");
            Object names = namesClass.getMethod("getInstance").invoke(null);
            namesClass.getMethod("initSeed", long.class).invoke(names, seed);

            for (int row = 0; row < 3; row++) {
                int cost = costs != null && row < costs.length ? costs[row] : 1;
                if (cost <= 0) continue;

                int textX = left + 62;
                int textY = top + 16 + row * 19;
                Object randomName = namesClass.getMethod("getRandomName", Font.class, int.class).invoke(names, font, 86);
                if (randomName instanceof FormattedText formatted) {
                    gfx.drawWordWrap(font, formatted, textX + 1, textY + 1, 85, ENCHANT_SHADOW);
                    gfx.drawWordWrap(font, formatted, textX, textY, 85, ENCHANT_TEXT);
                }

                String level = Integer.toString(cost);
                int levelX = left + 168 - font.width(level);
                int levelY = textY + 7;
                gfx.drawString(font, level, levelX + 1, levelY + 1, ENCHANT_SHADOW, false);
                gfx.drawString(font, level, levelX, levelY, ENCHANT_LEVEL, false);
            }
        } catch (Throwable ignored) {
            // Enchanting internals are vanilla/private. If they change, keep the GUI stable.
        }
    }

    private static void drawEnchantingSparkles(GuiGraphics gfx, int left, int top) {
        long tick = System.currentTimeMillis() / 180L;
        int[][] points = {
            {70, 18, 0xFF9E4DFF}, {95, 20, 0xFFFFC654}, {119, 18, 0xFF66FFD7},
            {151, 22, 0xFFB165FF}, {72, 35, 0xFF66FFD7}, {132, 34, 0xFFFFC654},
            {160, 43, 0xFF9E4DFF}, {83, 55, 0xFFB165FF}, {113, 62, 0xFF66FFD7},
            {148, 65, 0xFFFFC654}
        };
        for (int i = 0; i < points.length; i++) {
            if (((tick + i) & 3L) == 0L) continue;
            int x = left + points[i][0];
            int y = top + points[i][1] + (int) (((tick + i) % 3L) - 1L);
            int color = points[i][2];
            gfx.fill(x, y, x + 2, y + 1, color);
            if (((tick + i) & 1L) == 0L) {
                gfx.fill(x + 1, y - 1, x + 2, y, color & 0x88FFFFFF);
            }
        }
    }

    private static int[] readFirstIntArray(Object target, int expectedLength) {
        Class<?> c = target.getClass();
        while (c != null) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getType() != int[].class) continue;
                try {
                    f.setAccessible(true);
                    Object value = f.get(target);
                    if (value instanceof int[] arr && arr.length == expectedLength) return arr;
                } catch (Throwable ignored) {
                    // Try next field.
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }

    private static long readBestSeed(Object target) {
        for (String name : new String[] { "getEnchantmentSeed", "m_39469_", "m_39471_" }) {
            try {
                var method = target.getClass().getMethod(name);
                if (method.getReturnType() == int.class || method.getReturnType() == long.class) {
                    Object value = method.invoke(target);
                    if (value instanceof Number number) return number.longValue();
                }
            } catch (Throwable ignored) {
                // Try next candidate.
            }
        }
        Class<?> c = target.getClass();
        while (c != null) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getType() != int.class && f.getType() != long.class) continue;
                try {
                    f.setAccessible(true);
                    long value = ((Number) f.get(target)).longValue();
                    if (value > 0) return value;
                } catch (Throwable ignored) {
                    // Try next field.
                }
            }
            c = c.getSuperclass();
        }
        return 0L;
    }

    private static void drawEnderChestDetails(GuiGraphics gfx, int left, int top, int imageHeight) {
        // Pequenas motas ender encima de la textura generic_54 existente. Esto
        // diferencia Ender Chest sin tocar el look aprobado de cofres/barriles.
        int[][] motes = {
            {151, 10, 2, ENDER_PURPLE},
            {160, 18, 1, ENDER_GREEN},
            {143, 27, 1, ENDER_PURPLE},
            {166, 35, 2, ENDER_DIM},
            {154, imageHeight - 82, 1, ENDER_GREEN},
            {164, imageHeight - 71, 1, ENDER_PURPLE},
            {146, imageHeight - 62, 2, ENDER_DIM}
        };

        for (int[] mote : motes) {
            int x = left + mote[0];
            int y = top + mote[1];
            int size = mote[2];
            int color = mote[3];
            gfx.fill(x, y, x + size, y + size, color);
            if (size == 2) {
                gfx.fill(x - 1, y + 1, x, y + 2, color & 0x88FFFFFF);
                gfx.fill(x + 2, y, x + 3, y + 1, color & 0x88FFFFFF);
            }
        }
    }

    private static final class LabelState {
        private final Field titleXField;
        private final Field titleYField;
        private final Field inventoryXField;
        private final Field inventoryYField;
        private final Field inventoryTitleField;
        private final int titleX;
        private final int titleY;
        private final int inventoryX;
        private final int inventoryY;
        private final int imageHeight;
        private final Component inventoryTitle;

        private LabelState(
            Field titleXField,
            Field titleYField,
            Field inventoryXField,
            Field inventoryYField,
            Field inventoryTitleField,
            int titleX,
            int titleY,
            int inventoryX,
            int inventoryY,
            int imageHeight,
            Component inventoryTitle
        ) {
            this.titleXField = titleXField;
            this.titleYField = titleYField;
            this.inventoryXField = inventoryXField;
            this.inventoryYField = inventoryYField;
            this.inventoryTitleField = inventoryTitleField;
            this.titleX = titleX;
            this.titleY = titleY;
            this.inventoryX = inventoryX;
            this.inventoryY = inventoryY;
            this.imageHeight = imageHeight;
            this.inventoryTitle = inventoryTitle;
        }

        private static LabelState capture(AbstractContainerScreen<?> container) {
            Class<?> type = container.getClass();
            Field titleXField = findIntField(type, TITLE_X_NAMES);
            Field titleYField = findIntField(type, TITLE_Y_NAMES);
            Field inventoryXField = findIntField(type, INVENTORY_X_NAMES);
            Field inventoryYField = findIntField(type, INVENTORY_Y_NAMES);
            Field inventoryTitleField = findInventoryTitleField(type, container);
            Field imageHeightField = findIntField(type, IMAGE_HEIGHT_NAMES);

            int imageHeight = getInt(imageHeightField, container, 166);
            return new LabelState(
                titleXField,
                titleYField,
                inventoryXField,
                inventoryYField,
                inventoryTitleField,
                getInt(titleXField, container, 8),
                getInt(titleYField, container, 6),
                getInt(inventoryXField, container, 8),
                getInt(inventoryYField, container, imageHeight - 94),
                imageHeight,
                getComponent(inventoryTitleField, container)
            );
        }

        private boolean canHide() {
            return titleYField != null && inventoryYField != null;
        }

        private void hide(AbstractContainerScreen<?> container) {
            setInt(titleXField, container, HIDDEN_POS);
            setInt(titleYField, container, HIDDEN_POS);
            setInt(inventoryXField, container, HIDDEN_POS);
            setInt(inventoryYField, container, HIDDEN_POS);
            setComponent(inventoryTitleField, container, Component.empty());
        }

        private void restore(AbstractContainerScreen<?> container) {
            setInt(titleXField, container, titleX);
            setInt(titleYField, container, titleY);
            setInt(inventoryXField, container, inventoryX);
            setInt(inventoryYField, container, inventoryY);
            setComponent(inventoryTitleField, container, inventoryTitle);
        }
    }
}
