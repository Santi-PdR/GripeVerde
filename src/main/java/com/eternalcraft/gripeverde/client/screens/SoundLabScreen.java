package com.eternalcraft.gripeverde.client.screens;

import com.eternalcraft.gripeverde.client.audio.ModSounds;
import com.eternalcraft.gripeverde.client.render.DiaryTheme;
import com.eternalcraft.gripeverde.client.widgets.SlimButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * SoundLab v2.1 - Ambient Test Panel.
 *
 * Fix Bloque 2.4.1:
 *   - Headers ya no solapan con items (fila propia)
 *   - Status bar usa String.format en lugar de replace
 *   - Layout unificado: DisplayRow como marker header/item
 */
public class SoundLabScreen extends Screen {

    private final Screen parent;

    // Categorías
    private static final int CAT_UI     = 0;
    private static final int CAT_BASE   = 1;
    private static final int CAT_EVENTS = 2;
    private static final int CAT_RARE   = 3;

    // Volúmenes de test
    private static final float TEST_VOL_UI      = 1.0f;
    private static final float TEST_VOL_AMBIENT = 0.5f;

    private static class Entry {
        final int category;
        final String key;
        final RegistryObject<SoundEvent> sound;
        final float volume;

        Entry(int category, String key, RegistryObject<SoundEvent> sound, float volume) {
            this.category = category;
            this.key = key;
            this.sound = sound;
            this.volume = volume;
        }
    }

    // Fila de display: puede ser un header (isHeader=true, entry=null) o un item
    private static class DisplayRow {
        final boolean isHeader;
        final int category;
        final Entry entry;
        final int entryIndex; // índice en la lista `entries`, o -1 si es header

        static DisplayRow header(int cat) { return new DisplayRow(true, cat, null, -1); }
        static DisplayRow item(Entry e, int idx) { return new DisplayRow(false, e.category, e, idx); }

        private DisplayRow(boolean h, int c, Entry e, int i) {
            this.isHeader = h; this.category = c; this.entry = e; this.entryIndex = i;
        }
    }

    private final List<Entry> entries = new ArrayList<>();
    private final List<DisplayRow> rows = new ArrayList<>();

    private int selectedIndex = 0; // índice sobre `entries`
    private int scrollOffset  = 0; // índice sobre `rows`

    // Layout
    private static final int ROW_HEIGHT   = 16;
    private static final int HEADER_HEIGHT= 18;
    private static final int LIST_TOP     = 90;
    private static final int LIST_LEFT    = 60;

    public SoundLabScreen(Screen parent) {
        super(Component.translatable("gripeverde.soundlab.window"));
        this.parent = parent;

        entries.add(new Entry(CAT_UI, "ui.hover",          ModSounds.UI_HOVER,          TEST_VOL_UI));
        entries.add(new Entry(CAT_UI, "ui.click",          ModSounds.UI_CLICK,          TEST_VOL_UI));
        entries.add(new Entry(CAT_UI, "ui.click_heavy",    ModSounds.UI_CLICK_HEAVY,    TEST_VOL_UI));
        entries.add(new Entry(CAT_UI, "ui.back",           ModSounds.UI_BACK,           TEST_VOL_UI));
        entries.add(new Entry(CAT_UI, "ui.open",           ModSounds.UI_OPEN,           TEST_VOL_UI));
        entries.add(new Entry(CAT_UI, "ui.close",          ModSounds.UI_CLOSE,          TEST_VOL_UI));
        entries.add(new Entry(CAT_UI, "ui.modal",          ModSounds.UI_MODAL,          TEST_VOL_UI));
        entries.add(new Entry(CAT_UI, "ui.error",          ModSounds.UI_ERROR,          TEST_VOL_UI));
        entries.add(new Entry(CAT_UI, "ui.tab",            ModSounds.UI_TAB,            TEST_VOL_UI));
        entries.add(new Entry(CAT_UI, "ui.scroll",         ModSounds.UI_SCROLL,         TEST_VOL_UI));
        entries.add(new Entry(CAT_UI, "ui.success",        ModSounds.UI_SUCCESS,        TEST_VOL_UI));
        entries.add(new Entry(CAT_UI, "ui.notification",   ModSounds.UI_NOTIFICATION,   TEST_VOL_UI));
        entries.add(new Entry(CAT_UI, "ui.click_create",   ModSounds.UI_CLICK_CREATE,   TEST_VOL_UI));
        entries.add(new Entry(CAT_UI, "ui.click_destroy",  ModSounds.UI_CLICK_DESTROY,  TEST_VOL_UI));
        entries.add(new Entry(CAT_UI, "ui.respawn",        ModSounds.UI_RESPAWN,        TEST_VOL_UI));
        entries.add(new Entry(CAT_BASE, "menu.wind",       ModSounds.MENU_WIND,       TEST_VOL_AMBIENT));
        entries.add(new Entry(CAT_BASE, "menu.rain",       ModSounds.MENU_RAIN,       TEST_VOL_AMBIENT));
        entries.add(new Entry(CAT_BASE, "menu.city",       ModSounds.MENU_CITY,       TEST_VOL_AMBIENT));
        entries.add(new Entry(CAT_EVENTS, "menu.footsteps",          ModSounds.MENU_FOOTSTEPS,          TEST_VOL_AMBIENT));
        entries.add(new Entry(CAT_EVENTS, "menu.wood_creak",         ModSounds.MENU_WOOD_CREAK,         TEST_VOL_AMBIENT));
        entries.add(new Entry(CAT_EVENTS, "menu.door",               ModSounds.MENU_DOOR,               TEST_VOL_AMBIENT));
        entries.add(new Entry(CAT_EVENTS, "menu.ambient_occasional", ModSounds.MENU_AMBIENT_OCCASIONAL, TEST_VOL_AMBIENT));
        entries.add(new Entry(CAT_RARE, "menu.bell",    ModSounds.MENU_BELL,    TEST_VOL_AMBIENT));
        entries.add(new Entry(CAT_RARE, "menu.thunder", ModSounds.MENU_THUNDER, TEST_VOL_AMBIENT));
        entries.add(new Entry(CAT_RARE, "menu.crows",   ModSounds.MENU_CROWS,   TEST_VOL_AMBIENT));
        entries.add(new Entry(CAT_RARE, "menu.chain",   ModSounds.MENU_CHAIN,   TEST_VOL_AMBIENT));

        buildRows();
    }

    private void buildRows() {
        rows.clear();
        int lastCat = -1;
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            if (e.category != lastCat) {
                rows.add(DisplayRow.header(e.category));
                lastCat = e.category;
            }
            rows.add(DisplayRow.item(e, i));
        }
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new SlimButton(
            this.width - 160, this.height - 45, 130, 28,
            Component.translatable("gripeverde.soundlab.button.stop_all"),
            () -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc != null) mc.getSoundManager().stop();
            }
        ));

        this.addRenderableWidget(new SlimButton(
            20, this.height - 45, 120, 28,
            Component.translatable("gripeverde.soundlab.button.back"),
            () -> this.minecraft.setScreen(parent)
        ));
    }

    private void playSelected() {
        if (entries.isEmpty()) return;
        Entry e = entries.get(selectedIndex);
        try {
            SoundEvent se = e.sound.get();
            Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(se, 1.0f, e.volume));
        } catch (Exception ignored) {}
    }

    private void moveSelection(int delta) {
        if (entries.isEmpty()) return;
        selectedIndex = (selectedIndex + delta + entries.size()) % entries.size();
        ensureVisible();
    }

    private int getRowIndexForEntry(int entryIdx) {
        for (int i = 0; i < rows.size(); i++) {
            DisplayRow r = rows.get(i);
            if (!r.isHeader && r.entryIndex == entryIdx) return i;
        }
        return 0;
    }

    private void ensureVisible() {
        int rowIdx = getRowIndexForEntry(selectedIndex);
        int visibleRows = getVisibleRows();
        // Incluir el header que precede al item si existe
        int desiredTop = rowIdx;
        if (rowIdx > 0 && rows.get(rowIdx - 1).isHeader) {
            desiredTop = rowIdx - 1;
        }
        if (desiredTop < scrollOffset) {
            scrollOffset = desiredTop;
        } else if (rowIdx >= scrollOffset + visibleRows) {
            scrollOffset = rowIdx - visibleRows + 1;
        }
        clampScroll();
    }

    private int getVisibleRows() {
        int available = this.height - LIST_TOP - 70;
        return Math.max(1, available / ROW_HEIGHT);
    }

    private void clampScroll() {
        int maxScroll = Math.max(0, rows.size() - getVisibleRows());
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_RIGHT:
            case GLFW.GLFW_KEY_DOWN:
                moveSelection(1);
                return true;
            case GLFW.GLFW_KEY_LEFT:
            case GLFW.GLFW_KEY_UP:
                moveSelection(-1);
                return true;
            case GLFW.GLFW_KEY_ENTER:
            case GLFW.GLFW_KEY_KP_ENTER:
                playSelected();
                return true;
            case GLFW.GLFW_KEY_ESCAPE:
                this.minecraft.setScreen(parent);
                return true;
            case GLFW.GLFW_KEY_PAGE_DOWN:
                moveSelection(getVisibleRows());
                return true;
            case GLFW.GLFW_KEY_PAGE_UP:
                moveSelection(-getVisibleRows());
                return true;
            case GLFW.GLFW_KEY_HOME:
                selectedIndex = 0;
                scrollOffset = 0;
                return true;
            case GLFW.GLFW_KEY_END:
                selectedIndex = entries.size() - 1;
                ensureVisible();
                return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollOffset -= (int) Math.signum(delta);
        clampScroll();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int visibleRows = getVisibleRows();
            int y = LIST_TOP;
            for (int i = 0; i < visibleRows; i++) {
                int rIdx = scrollOffset + i;
                if (rIdx >= rows.size()) break;
                DisplayRow r = rows.get(rIdx);
                int h = r.isHeader ? HEADER_HEIGHT : ROW_HEIGHT;
                if (!r.isHeader &&
                    mouseY >= y && mouseY < y + h &&
                    mouseX >= LIST_LEFT && mouseX < this.width - 40) {
                    selectedIndex = r.entryIndex;
                    playSelected();
                    return true;
                }
                y += h;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0xFF15100A);

        String title = I18n.get("gripeverde.soundlab.title");
        int tw = this.font.width(title);
        g.drawString(this.font, title, (this.width - tw) / 2, 24, DiaryTheme.PAPER, false);

        String sub = I18n.get("gripeverde.soundlab.subtitle");
        int sw = this.font.width(sub);
        g.drawString(this.font, sub, (this.width - sw) / 2, 40, DiaryTheme.SEPIA_LIGHT, false);

        String help = I18n.get("gripeverde.soundlab.help");
        int hw = this.font.width(help);
        g.drawString(this.font, help, (this.width - hw) / 2, 58, DiaryTheme.SEPIA_LIGHT, false);

        // Lista con headers en su propia fila
        int visibleRows = getVisibleRows();
        int y = LIST_TOP;
        for (int i = 0; i < visibleRows; i++) {
            int rIdx = scrollOffset + i;
            if (rIdx >= rows.size()) break;
            DisplayRow r = rows.get(rIdx);

            if (r.isHeader) {
                String catLabel = getCategoryLabel(r.category);
                g.drawString(this.font, catLabel, LIST_LEFT - 30, y + 5,
                    DiaryTheme.AMBER_FLAME, false);
                y += HEADER_HEIGHT;
            } else {
                boolean selected = (r.entryIndex == selectedIndex);
                if (selected) {
                    g.fill(LIST_LEFT - 4, y, this.width - 30, y + ROW_HEIGHT - 2, 0x40FFDD88);
                }
                int textColor = selected ? DiaryTheme.PAPER : DiaryTheme.SEPIA_LIGHT;
                g.drawString(this.font, "* " + r.entry.key, LIST_LEFT + 8, y + 4, textColor, false);

                String playLabel = "[" + I18n.get("gripeverde.soundlab.button.play") + "]";
                int plw = this.font.width(playLabel);
                g.drawString(this.font, playLabel, this.width - plw - 40, y + 4, textColor, false);
                y += ROW_HEIGHT;
            }
        }

        // Barra de scroll
        if (rows.size() > visibleRows) {
            int trackTop = LIST_TOP;
            int trackHeight = this.height - LIST_TOP - 70;
            int trackX = this.width - 20;
            g.fill(trackX, trackTop, trackX + 3, trackTop + trackHeight, 0x30FFFFFF);
            int thumbHeight = Math.max(20, trackHeight * visibleRows / rows.size());
            int maxScroll = rows.size() - visibleRows;
            int thumbY = trackTop + (maxScroll > 0
                ? (trackHeight - thumbHeight) * scrollOffset / maxScroll
                : 0);
            g.fill(trackX, thumbY, trackX + 3, thumbY + thumbHeight, 0xB0FFDD88);
        }

        // Status bar — concatenación directa (evita 'Format error' con % en el texto)
        if (!entries.isEmpty()) {
            String tpl = I18n.get("gripeverde.soundlab.status.selected");
            String selected = tpl + entries.get(selectedIndex).key;
            g.drawString(this.font, selected, 20, this.height - 62,
                DiaryTheme.SEPIA_LIGHT, false);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private String getCategoryLabel(int cat) {
        switch (cat) {
            case CAT_UI:     return I18n.get("gripeverde.soundlab.category.ui");
            case CAT_BASE:   return I18n.get("gripeverde.soundlab.category.base");
            case CAT_EVENTS: return I18n.get("gripeverde.soundlab.category.events");
            case CAT_RARE:   return I18n.get("gripeverde.soundlab.category.rare");
            default: return "";
        }
    }

    @Override public boolean isPauseScreen() { return false; }
}