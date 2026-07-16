package com.eternalcraft.gripeverde.client.theme;

import java.util.HashMap;
import java.util.Map;

public class ScreenLayout {
    private final int screenWidth;
    private final int screenHeight;
    private final Map<Anchor, LayoutRegion> regions = new HashMap<>();

    public ScreenLayout(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        calculateRegions();
    }

    private void calculateRegions() {
        int m = Spacing.SCREEN_MARGIN;
        int safeW = screenWidth - (m * 2);
        int safeH = screenHeight - (m * 2);

        int colW = safeW / 3;
        int rowH = safeH / 3;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int x = m + (col * colW);
                int y = m + (row * rowH);
                Anchor anchor = getAnchorFor(row, col);
                regions.put(anchor, new LayoutRegion(x, y, colW, rowH));
            }
        }
    }

    private Anchor getAnchorFor(int row, int col) {
        if (row == 0) return col == 0 ? Anchor.TOP_LEFT    : (col == 1 ? Anchor.TOP_CENTER : Anchor.TOP_RIGHT);
        if (row == 1) return col == 0 ? Anchor.MID_LEFT    : (col == 1 ? Anchor.MID_CENTER : Anchor.MID_RIGHT);
        return             col == 0 ? Anchor.BOT_LEFT    : (col == 1 ? Anchor.BOT_CENTER : Anchor.BOT_RIGHT);
    }

    public LayoutRegion getRegion(Anchor anchor) {
        return regions.getOrDefault(anchor, new LayoutRegion(0, 0, screenWidth, screenHeight));
    }
}